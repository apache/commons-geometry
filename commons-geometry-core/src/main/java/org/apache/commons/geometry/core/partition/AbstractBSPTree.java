/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.core.partition;

import java.io.Serializable;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.geometry.core.Point;

/** Abstract class for Binary Space Partitioning (BSP) tree implementations.
 * @param <P> Point implementation type
 * @param <T> Node implementation type
 */
public abstract class AbstractBSPTree<P extends Point<P>, N extends AbstractBSPTree.AbstractNode<P, N>> implements BSPTree<P, N>, Serializable {

    /** Interface for objects that construct instances of {@link AbstractNode}.
     * @param <P> Point implementation type
     * @param <T> Node attribute type
     */
    protected static interface NodeFactory<P extends Point<P>, N extends AbstractBSPTree.AbstractNode<P, N>>
        extends Function<AbstractBSPTree<P, N>, N> {
    }

    /** Serializable UID */
    private static final long serialVersionUID = 20190225L;

    /** Integer value set on various node fields when a value is unknown. */
    private static final int UNKNOWN_VALUE = -1;

    /** The root node for the tree. */
    private N root;

    /** The current modification version for the tree structure. This is incremented each time
     * a structural change occurs in the tree and is used to determine when cached values
     * must be recomputed.
     */
    private long version = 0L;

    /** {@inheritDoc} */
    @Override
    public N getRoot() {
        if (root == null) {
            setRoot(createNode());
        }
        return root;
    }

    /** Set the root node for the tree.
     * @return
     */
    protected void setRoot(final N root) {
        this.root = root;

        this.root.setParent(null);
        this.root.setDepth(0);

        incrementVersion();
    }

    /** {@inheritDoc} */
    @Override
    public int count() {
        return getRoot().count();
    }

    /** {@inheritDoc} */
    @Override
    public void visit(final BSPTreeVisitor<P, N> visitor) {
        visit(getRoot(), visitor);
    }

    /** {@inheritDoc} */
    @Override
    public N findNode(final P pt) {
        return findNode(getRoot(), pt);
    }

    /** {@inheritDoc} */
    @Override
    public void insert(final SubHyperplane<P> sub) {
        insert(sub.toConvex());
    }

    /** {@inheritDoc} */
    @Override
    public void insert(final ConvexSubHyperplane<P> convexSub) {
        insertRecursive(getRoot(), convexSub,
                convexSub.getHyperplane().wholeHyperplane());
    }

    /** {@inheritDoc} */
    @Override
    public void insert(final Iterable<ConvexSubHyperplane<P>> convexSubs) {
        for (ConvexSubHyperplane<P> convexSub : convexSubs) {
            insert(convexSub);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<N> nodes() {
        return new Iterable<N>() {
            /** {@inheritDoc} */
            @Override
            public Iterator<N> iterator() {
                return new NodeIterator<P, N>(AbstractBSPTree.this);
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<N> leafNodes() {
        return new Iterable<N>() {
            /** {@inheritDoc} */
            @Override
            public Iterator<N> iterator() {
                final NodeIterator<P, N> iterator = new NodeIterator<P, N>(AbstractBSPTree.this);
                return new FilteredNodeIteratorWrapper<P, N>(iterator, n -> n.isLeaf());
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<N> internalNodes() {
        return new Iterable<N>() {
            /** {@inheritDoc} */
            @Override
            public Iterator<N> iterator() {
                final NodeIterator<P, N> iterator = new NodeIterator<P, N>(AbstractBSPTree.this);
                return new FilteredNodeIteratorWrapper<P, N>(iterator, n -> !n.isLeaf());
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public AbstractBSPTree<P, N> copy() {
        AbstractBSPTree<P, N> copy = createTree();
        copyRecursive(getRoot(), copy.getRoot());

        return copy;
    }

    /** Recursively copy a subtree.
     * @param src the node representing the source subtree
     * @param dst the node representing the destination subtree
     * @return the copied node, ie {@code dst}
     */
    protected N copyRecursive(final N src, final N dst) {

        ConvexSubHyperplane<P> cut = null;
        N minus = null;
        N plus = null;

        if (!src.isLeaf()) {
            final AbstractBSPTree<P, N> dstTree = dst.getTree();

            cut = src.getCut();
            minus = copyRecursive(src.getMinus(), dstTree.createNode());
            plus = copyRecursive(src.getPlus(), dstTree.createNode());
        }

        dst.setCutState(cut, minus, plus);

        copyNodeProperties(src, dst);

        return dst;
    }

    /** Create a new tree instance.
     * @return a new tree instance
     */
    protected abstract AbstractBSPTree<P, N> createTree();

    /** Create a new node for this tree
     * @return a new node for this tree
     */
    protected abstract N createNode();

    /** Copy node properties from {@code src} to {@code dst}. This method
     * is used when performing a deep copy of the tree and can be used by
     * subclasses to copy non-structural node properties.
     * @param src source node
     * @param dst destination node
     */
    protected void copyNodeProperties(final N src, final N dst) {
    }

    /** Find the smallest node in the tree containing the point, starting
     * at the given node.
     * @param start the node to begin the search with
     * @param pt the point to check
     * @return the smallest node in the tree containing the point
     */
    protected N findNode(final N start, final P pt) {
        Hyperplane<P> hyper = start.getCutHyperplane();
        if (hyper != null) {
            Side side = hyper.classify(pt);

            if (side == Side.PLUS) {
                return findNode(start.getPlus(), pt);
            }
            else if (side == Side.MINUS) {
                return findNode(start.getMinus(), pt);
            }
        }
        return start;
    }

    /** Visit the nodes in the tree, starting at the given node.
     * @param node the node to begin the visit process
     * @param visitor the visitor to pass nodes to
     */
    protected void visit(final N node, BSPTreeVisitor<P, N> visitor) {
        // simple recursive implementation of this; we'll probably
        // want to change this later
        if (node != null) {
            visitor.visit(node);

            if (!node.isLeaf()) {
                visit(node.getMinus(), visitor);
                visit(node.getPlus(), visitor);
            }
        }
    }

    /** Insert a cut into the given node. The node becomes a leaf node if the hyperplane
     * does not intersect the node's region and a parent node with two new children
     * if it does.
     * @param node the node to cut
     * @param cutter the hyperplane to cut the node with
     * @return true if the node was cut; otherwise fasel
     */
    protected boolean insertCut(final N node, final Hyperplane<P> cutter) {
        // cut the hyperplane using all hyperplanes from this node up
        // to the root
        ConvexSubHyperplane<P> cut = fitToCell(node, cutter.wholeHyperplane());
        if (cut == null || cut.isEmpty()) {
            // insertion failed; the node was not cut
            setNodeCut(node, null);
            return false;
        }

        setNodeCut(node, cut);
        return true;
    }

    /** Remove the cut from the given node. Returns true if the node had a cut before
     * the call to this method.
     * @param node the node to remove the cut from
     * @return true if the node previously had a cut
     */
    protected boolean clearCut(final N node) {
        boolean hadCut = node.getCut() != null;
        setNodeCut(node, null);

        return hadCut;
    }

    /** Fit the subhyperplane in the region defined by the given node. This method cuts the
     * given subhyperplane with the binary partitioners of all parent nodes up to the root.
     * @param node the node representing the region to fit the subhyperplane to
     * @param sub the subhyperplane to fit into the cell
     * @return the subhyperplane fit to the cell
     */
    protected ConvexSubHyperplane<P> fitToCell(final N node, final ConvexSubHyperplane<P> sub) {

        ConvexSubHyperplane<P> result = sub;

        N parentNode = node.getParent();
        N currentNode = node;

        while (parentNode != null && result != null) {
            ConvexSubHyperplane.Split<P> split = result.split(parentNode.getCutHyperplane());

            result = currentNode.isPlus() ? split.getPlus() : split.getMinus();

            currentNode = parentNode;
            parentNode = parentNode.getParent();
        }

        return result;
    }

    /** Recursively insert a convex subhyperplane into the tree at the given node.
     * @param node the node to begin insertion with
     * @param insert the convex subhyperplane to insert
     * @param trimmed convex subhyperplane containing the result of splitting the entire
     *      space with each hyperplane from this node to the root
     */
    protected void insertRecursive(final N node, final ConvexSubHyperplane<P> insert,
            final ConvexSubHyperplane<P> trimmed) {
        if (node.isLeaf()) {
            setNodeCut(node, trimmed);
        }
        else {
            final ConvexSubHyperplane.Split<P> insertSplit = insert.split(node.getCutHyperplane());

            final ConvexSubHyperplane<P> minus = insertSplit.getMinus();
            final ConvexSubHyperplane<P> plus = insertSplit.getPlus();

            if (minus != null || plus != null) {
                final ConvexSubHyperplane.Split<P> trimmedSplit = trimmed.split(node.getCutHyperplane());

                if (minus != null) {
                    insertRecursive(node.getMinus(), minus, trimmedSplit.getMinus());
                }
                if (plus != null) {
                    insertRecursive(node.getPlus(), plus, trimmedSplit.getPlus());
                }
            }
        }
    }

    /** Set the cut subhyperplane for the given node. If {@code cut} is null, any existing child nodes
     * are removed. If {@code cut} is not null, two new child nodes are created.
     * @param node
     * @param cut
     */
    protected void setNodeCut(final N node, final ConvexSubHyperplane<P> cut) {
        N plus = null;;
        N minus = null;

        if (cut != null) {
            minus = createNode();
            initChildNode(node, minus, false);

            plus = createNode();
            initChildNode(node, plus, true);
        }

        node.setCutState(cut, minus, plus);

        // a structural change occurred, so increment the tree version
        incrementVersion();
    }

    /** Method called to initialize a new child node. Subclasses can use this method to
     * set initial attributes on the node.
     * @param parent the parent node
     * @param child the new child node
     * @param isPlus true if the child will be assigned as the parent's plus child;
     *      false if it will be the parent's minus child
     */
    protected void initChildNode(final N parent, final N child, final boolean isPlus) {
    }

    /** Increment the version of the tree. This method should be called anytime structural
     * changes occur to the tree.
     */
    protected void incrementVersion() {
        ++version;
    }

    /** Get the current structural version of the tree. This is incremented each time the
     * tree structure changes and can be used by nodes to allow caching of computed values.
     * @return the current version of the tree structure
     */
    protected long getVersion() {
        return version;
    }

    /** Abstract implementation of {@link BSPTree.Node}. This class is intended for use with
     * {@link AbstractBSPTree} and delegates tree mutation methods back to the parent tree object.
     * @param <P> Point implementation type
     * @param <T> Node implementation type
     */
    public static abstract class AbstractNode<P extends Point<P>, N extends AbstractNode<P, N>> implements BSPTree.Node<P, N>, Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 20190225L;

        /** The owning tree instance */
        private final AbstractBSPTree<P, N> tree;

        /** The parent node; this will be null for the tree root node */
        private N parent;

        /** The subhyperplane cutting the node's region; this will be null for leaf nodes */
        private ConvexSubHyperplane<P> cut;

        /** The node lying on the minus side of the cut subhyperplane; this will be null
         * for leaf nodes.
         */
        private N minus;

        /** The node lying on the plus side of the cut subhyperplane; this will be null
         * for leaf nodes.
         */
        private N plus;

        /** The current version of the node. This is set to track the tree's version
         * and is used to detect when certain values need to be recomputed due to
         * structural changes in the tree.
         */
        private long version = -1L;

        /** The depth of this node in the tree. This will be zero for the root node and
         * {@link AbstractBSPTree#UNKNOWN_VALUE} when the value needs to be computed.
         */
        private int depth = UNKNOWN_VALUE;

        /** The total number of nodes in the subtree rooted at this node. This will be
         * set to {@link AbstractBSPTree#UNKNOWN_VALUE} when the value needs
         * to be computed.
         */
        private int count = UNKNOWN_VALUE;

        /** Simple constructor.
         * @param tree the tree instance that owns this node
         */
        protected AbstractNode(final AbstractBSPTree<P, N> tree) {
            this.tree = tree;
        }

        /** {@inheritDoc} */
        @Override
        public AbstractBSPTree<P, N> getTree() {
            return tree;
        }

        /** {@inheritDoc} */
        @Override
        public int depth() {
            if (depth == UNKNOWN_VALUE) {
                // calculate our depth based on our parent's depth, if
                // possible
                if (parent != null) {
                    int parentDepth = parent.depth();
                    if (parentDepth != UNKNOWN_VALUE) {
                        depth = parentDepth + 1;
                    }
                }
            }
            return depth;
        }

        /** {@inheritDoc} */
        @Override
        public int count() {
            checkTreeUpdated();

            if (count == UNKNOWN_VALUE) {
                count = 1;

                if (!isLeaf()) {
                    count += minus.count() + plus.count();
                }
            }

            return count;
        }

        /** {@inheritDoc} */
        @Override
        public N getParent() {
            return parent;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isLeaf() {
            return cut == null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isPlus() {
            return parent != null && parent.getPlus() == this;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isMinus() {
            return parent != null && parent.getMinus() == this;
        }

        /** {@inheritDoc} */
        @Override
        public ConvexSubHyperplane<P> getCut() {
            return cut;
        }

        /** {@inheritDoc} */
        @Override
        public N getPlus() {
            return plus;
        }

        /** {@inheritDoc} */
        @Override
        public N getMinus() {
            return minus;
        }

        /** {@inheritDoc} */
        @Override
        public boolean insertCut(final Hyperplane<P> cutter) {
            return tree.insertCut(getSelf(), cutter);
        }

        /** {@inheritDoc} */
        @Override
        public boolean clearCut() {
            return tree.clearCut(getSelf());
        }

        /** {@inheritDoc} */
        @Override
        public N cut(final Hyperplane<P> cutter) {
            this.insertCut(cutter);

            return getSelf();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName())
                .append("[cut= ")
                .append(getCut())
                .append("]");

            return sb.toString();
        }

        /** Get the hyperplane for the instance's cut. Returns null if
         * no cut exists.
         * @return the hyperplane for the node's cut or null if no cut
         *      exists
         */
        protected Hyperplane<P> getCutHyperplane() {
            return (cut != null) ? cut.getHyperplane() : null;
        }

        /** Set the depth of the node in the tree.
         * @param depth the depth of the node in the tree
         */
        protected void setDepth(final int depth) {
            this.depth = depth;
        }

        /** Set the parent node for the instance.
         * @param parent the parent node for the instance
         */
        protected void setParent(final N parent) {
            this.parent = parent;
        }

        /** Set the cut state of node. The arguments should either be all null or all
         * non-null.
         * @param cut the new cut subhyperplane for the node
         * @param minus the new minus child for the node
         * @param plus the new plus child for the node
         */
        protected void setCutState(final ConvexSubHyperplane<P> cut, final N minus, final N plus) {
            this.cut = cut;

            final N self = getSelf();

            // get the child depth now if we know it offhand, otherwise set it to the unknown value
            // and have the child pull it when needed
            final int childDepth = (depth != UNKNOWN_VALUE) ? depth + 1 : UNKNOWN_VALUE;

            if (minus != null) {
                minus.setParent(self);
                plus.setDepth(childDepth);
            }
            this.minus = minus;

            if (plus != null) {
                plus.setParent(self);
                plus.setDepth(childDepth);
            }
            this.plus = plus;
        }

        /** Check if any updates have occurred in the tree since the last
         * call to this method and call {@link #treeUpdated()} if so.
         */
        protected void checkTreeUpdated() {
            final long treeVersion = tree.getVersion();

            if (version != treeVersion) {
                // the tree structure changed somewhere
                treeUpdated();

                // store the current version
                version = treeVersion;
            }
        }

        /** Method called from {@link #checkTreeUpdated()} when updates
         * are detected in the tree. This method should clear out any
         * computed properties that rely on the structure of the tree
         * and prepare them for recalculation.
         */
        protected void treeUpdated() {
            count = UNKNOWN_VALUE;
        }

        /** Get a reference to the current instance, cast to type N.
         * @return a reference to the current instance, as type N.
         */
        protected abstract N getSelf();
    }

    /** Class for iterating through the nodes in a BSP tree.
     * @param <P> Point implementation type
     * @param <N> Node implementation type
     */
    public static class NodeIterator<P extends Point<P>, N extends AbstractNode<P, N>> implements Iterator<N> {

        /** The current node stack */
        private final Deque<N> stack = new LinkedList<>();

        /** Create a new instance for iterating over the nodes in the given tree.
         * @param tree the tree to iterate
         */
        public NodeIterator(final AbstractBSPTree<P, N> tree) {
            stack.push(tree.getRoot());
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        /** {@inheritDoc} */
        @Override
        public N next() {
            if (stack.isEmpty()) {
                throw new NoSuchElementException();
            }

            final N result = stack.pop();

            if (result != null && !result.isLeaf()) {
                stack.push(result.getPlus());
                stack.push(result.getMinus());
            }

            return result;
        }
    }

    /** Class that wraps filtering functionality around an underlying node iterator.
     * @param <P> Point implementation type
     * @param <N> Node implementation type
     */
    public static class FilteredNodeIteratorWrapper<P extends Point<P>, N extends AbstractNode<P, N>> implements Iterator<N> {

        /** The iterator to filter */
        private final Iterator<N> iterator;

        /** The filter function */
        private final Predicate<N> predicate;

        /** The next node to return */
        private N next;

        /** Construct a new instance that applies the given predicate function to the elements in the
         * iterator. Only elements that pass the predicate are returned.
         * @param iterator iterator to wrap
         * @param predicate predicate function to apply to nodes
         */
        public FilteredNodeIteratorWrapper(final Iterator<N> iterator, final Predicate<N> predicate) {
            this.iterator = iterator;
            this.predicate = predicate;

            advance();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return next != null;
        }

        /** {@inheritDoc} */
        @Override
        public N next() {
            if (next == null) {
                throw new NoSuchElementException();
            }

            N result = next;

            advance();

            return result;
        }

        /** Advance to the next filtered node in the iterable.
         */
        private void advance() {
            next = null;
            while (iterator.hasNext() &&
                    (next = iterator.next()) != null &&
                    !predicate.test(next)) {
                // the node didn't pass the test; advance to the
                // next one
                next = null;
            }
        }
    }
}
