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

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Transform;

/** Abstract class for Binary Space Partitioning (BSP) tree implementations.
 * @param <P> Point implementation type
 * @param <T> Node implementation type
 */
public abstract class AbstractBSPTree<P extends Point<P>, N extends AbstractBSPTree.AbstractNode<P, N>>
    implements BSPTree<P, N>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190330L;

    /** The default number of levels to print when creating a string representation of the tree */
    private static final int DEFAULT_TREE_STRING_MAX_DEPTH = 8;

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
    public int height() {
        return getRoot().height();
    }

    /** {@inheritDoc} */
    @Override
    public void visit(final BSPTreeVisitor<P, N> visitor) {
        visit(getRoot(), visitor);
    }

    /** {@inheritDoc} */
    @Override
    public N findNode(final P pt, final NodeCutRule cutBehavior) {
        return findNode(getRoot(), pt, cutBehavior);
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

    /** Return an iterator over the nodes in the tree */
    @Override
    public Iterator<N> iterator() {
        return new NodeIterator<>(getRoot());
    }

    /** {@inheritDoc} */
    @Override
    public void copy(final BSPTree<P, N> src) {
        copyRecursive(src.getRoot(), getRoot());
    }

    /** {@inheritDoc} */
    @Override
    public void extract(final N node) {
        // copy downward
        final N extracted = createNode();
        copyRecursive(node, extracted);

        // extract upward
        final N newRoot = extractParentPath(node, extracted);

        // set the root of this tree
        setRoot(newRoot);
    }

    /** {@inheritDoc} */
    @Override
    public void transform(final Transform<P> transform) {
        final boolean swapChildren = shouldTransformSwapChildren(transform);
        transformRecursive(getRoot(), transform, swapChildren);

        // increment the version to invalidate any computed properties that might
        // depend on the cuts
        incrementVersion();
    }

    /** Get a simple string representation of the tree structure. The returned string contains
     * the tree structure down to the default max depth of {@value #DEFAULT_TREE_STRING_MAX_DEPTH}.
     * @return a string representation of the tree
     */
    public String treeString() {
        return treeString(DEFAULT_TREE_STRING_MAX_DEPTH);
    }

    /** Get a simple string representation of the tree structure. The returned string contains
     * the tree structure down to {@code maxDepth}.
     * @return a string representation of the tree
     */
    public String treeString(final int maxDepth) {
        BSPTreePrinter<P, N> printer = new BSPTreePrinter<>(maxDepth);
        visit(printer);

        return printer.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return new StringBuilder()
                .append(getClass().getSimpleName())
                .append("[count= ")
                .append(count())
                .append(", height= ")
                .append(height())
                .append("]")
                .toString();
    }

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

    /** Method called to initialize a new child node. Subclasses can use this method to
     * set initial attributes on the node.
     * @param parent the parent node
     * @param child the new child node
     * @param isPlus true if the child will be assigned as the parent's plus child;
     *      false if it will be the parent's minus child
     */
    protected void initChildNode(final N parent, final N child, final boolean isPlus) {
    }

    /** Recursively copy a subtree.
     * @param src the node representing the source subtree
     * @param dst the node representing the destination subtree
     * @return the copied node, ie {@code dst}
     */
    protected N copyRecursive(final N src, final N dst) {
        if (src != dst) {
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
        }

        return dst;
    }

    /** Extract the path from {@code src} to the root of its tree and
     * set it as the parent path of {@code dst}. Leaf nodes created during
     * the extraction are given the same node properties as their counterparts
     * in the source tree but without the cuts and child nodes. The properties
     * of {@code dst} are not modified, with the exception of its parent node
     * reference.
     * @param src the source node to copy the parent path from
     * @param dst the destination node to place under the extracted path
     * @return the root node of the extracted path
     */
    private N extractParentPath(final N src, final N dst) {
        N dstParent = dst;
        N dstChild;
        N dstOtherChild;

        N srcChild = src;
        N srcParent = srcChild.getParent();

        while (srcParent != null) {
            dstChild = dstParent;

            dstParent = createNode();
            copyNodeProperties(srcParent, dstParent);

            dstOtherChild = createNode();

            if (srcChild.isMinus()) {
                copyNodeProperties(srcParent.getPlus(), dstOtherChild);

                dstParent.setCutState(srcParent.getCut(), dstChild, dstOtherChild);
            }
            else {
                copyNodeProperties(srcParent.getMinus(), dstOtherChild);

                dstParent.setCutState(srcParent.getCut(), dstOtherChild, dstChild);
            }

            srcChild = srcParent;
            srcParent = srcChild.getParent();
        }

        return dstParent;
    }

    /** Find the smallest node in the tree containing the point, starting
     * at the given node.
     * @param start the node to begin the search with
     * @param pt the point to check
     * @param cutBehavior value determining the search behavior when the test point
     *      lies directly on the cut subhyperplane of an internal node
     * @return the smallest node in the tree containing the point
     */
    private N findNode(final N start, final P pt, final NodeCutRule cutBehavior) {
        Hyperplane<P> cutHyper = start.getCutHyperplane();
        if (cutHyper != null) {
            HyperplaneLocation cutLoc = cutHyper.classify(pt);

            final boolean onPlusSide = cutLoc == HyperplaneLocation.PLUS;
            final boolean onMinusSide = cutLoc == HyperplaneLocation.MINUS;
            final boolean onCut = !onPlusSide && !onMinusSide;

            if (onMinusSide || (onCut && cutBehavior == NodeCutRule.MINUS)) {
                return findNode(start.getMinus(), pt, cutBehavior);
            }
            else if (onPlusSide || (onCut && cutBehavior == NodeCutRule.PLUS)) {
                return findNode(start.getPlus(), pt, cutBehavior);
            }
        }
        return start;
    }

    /** Visit the nodes in the tree, starting at the given node.
     * @param node the node to begin the visit process
     * @param visitor the visitor to pass nodes to
     */
    private void visit(final N node, BSPTreeVisitor<P, N> visitor) {
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
    private boolean insertCut(final N node, final Hyperplane<P> cutter) {
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
    private boolean clearCut(final N node) {
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
    private ConvexSubHyperplane<P> fitToCell(final N node, final ConvexSubHyperplane<P> sub) {

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
    private void insertRecursive(final N node, final ConvexSubHyperplane<P> insert,
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
    private void setNodeCut(final N node, final ConvexSubHyperplane<P> cut) {
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

    /** Transform the subtree rooted as {@code node} recursively.
     * @param node the root node of the subtree to transform
     * @param t the transform to apply
     * @param swapChildren if true, the plus and minus child nodes of each internal node
     *      will be swapped; this should be the case when the transform is a reflection
     */
    private void transformRecursive(final N node, final Transform<P> t, final boolean swapChildren) {
        if (node.isInternal()) {
            // transform our cut
            final ConvexSubHyperplane<P> transformedCut = node.getCut().transform(t);

            // transform our children
            transformRecursive(node.getMinus(), t, swapChildren);
            transformRecursive(node.getPlus(), t, swapChildren);

            final N transformedMinus = swapChildren ? node.getPlus() : node.getMinus();
            final N transformedPlus = swapChildren ? node.getMinus() : node.getPlus();

            // set our new state
            node.setCutState(transformedCut, transformedMinus, transformedPlus);
        }
    }

    /** Return true if the given transform should swap the plus and minus child nodes when
     * applied to the tree. This will be the case for transforms that represent reflections.
     * @param transform the transform to test
     * @return true if the transform should swap the plus and minus child nodes in the tree
     */
    private boolean shouldTransformSwapChildren(final Transform<P> transform) {
        final Hyperplane<P> hyperplane = getRoot().getCutHyperplane();

        if (hyperplane != null) {
            final P plusPt = hyperplane.plusPoint();

            // we should swap if a point on the plus side of the hyperplane is on the minus
            // side after the transformation
            final P transformedPlusPt = transform.apply(plusPt);
            final Hyperplane<P> transformedHyperplane = hyperplane.transform(transform);

            return transformedHyperplane.classify(transformedPlusPt) == HyperplaneLocation.MINUS;
        }

        return false;
    }


    /** Increment the version of the tree. This method should be called any time structural
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

        /** The height of the subtree rooted at this node. This will
         * be set to {@link AbstractBSPTree#UNKNOWN_VALUE} when the value needs
         * to be computed.
         */
        private int height = UNKNOWN_VALUE;

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
        public int height() {
            checkTreeUpdated();

            if (height == UNKNOWN_VALUE) {
                if (isLeaf()) {
                    height = 0;
                }
                else {
                    height = Math.max(getMinus().height(), getPlus().height()) + 1;
                }
            }

            return height;
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
        public Iterator<N> iterator() {
            return new NodeIterator<P, N>(getSelf());
        }

        /** {@inheritDoc} */
        @Override
        public void visit(final BSPTreeVisitor<P, N> visitor) {
            tree.visit(getSelf(), visitor);
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
        public boolean isInternal() {
            return cut != null;
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
        public Hyperplane<P> getCutHyperplane() {
            return (cut != null) ? cut.getHyperplane() : null;
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
            height = UNKNOWN_VALUE;
        }

        /** Get a reference to the current instance, cast to type N.
         * @return a reference to the current instance, as type N.
         */
        protected abstract N getSelf();
    }

    /** Class for iterating through the nodes in a BSP subtree.
     * @param <P> Point implementation type
     * @param <N> Node implementation type
     */
    public static class NodeIterator<P extends Point<P>, N extends AbstractNode<P, N>> implements Iterator<N> {

        /** The current node stack */
        private final Deque<N> stack = new LinkedList<>();

        /** Create a new instance for iterating over the nodes in the given subtree.
         * @param subtreeRoot the root node of the subtree to iterate
         */
        public NodeIterator(final N subtreeRoot) {
            stack.push(subtreeRoot);
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
}
