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
import java.util.function.Function;

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

    /** Object used to create new nodes for the tree. */
    private final NodeFactory<P, N> nodeFactory;

    /** The root node for the tree. */
    private final N root;

    /** Construct a new instance that uses the given factory object to produce
     * tree nodes.
     * @param nodeFactory object used to create nodes for this instance
     */
    protected AbstractBSPTree(final NodeFactory<P, N> nodeFactory) {
        this.nodeFactory = nodeFactory;
        this.root = nodeFactory.apply(this);
    }

    /** {@inheritDoc} */
    @Override
    public N getRoot() {
        return root;
    }

    /** {@inheritDoc} */
    @Override
    public int count() {
        return root.count();
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

    /** Create a new node for this tree. The returned node is empty.
     * @return a new node for this tree
     */
    protected N createNode(N parent) {
        N node = nodeFactory.apply(this);

        node.setParent(parent);

        final int parentDepth = (parent != null) ? parent.depth() : 0;
        node.setDepth(parentDepth + 1);

        return node;
    }

    /** Create a new tree instance.
     * @return a new tree instance
     */
    protected abstract AbstractBSPTree<P, N> createTree();

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
            clearNode(node);
            return false;
        }

        cutNode(node, cut);
        return true;
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
            cutNode(node, trimmed);
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

    protected void cutNode(final N node, final ConvexSubHyperplane<P> cut) {
        node.setCut(cut);

        node.setMinus(createNode(node));
        node.setPlus(createNode(node));

        onCutChange(node);
    }

    protected void clearNode(final N node) {
        node.setCut(null);

        node.setMinus(null);
        node.setPlus(null);

        onCutChange(node);
    }

    protected void onCutChange(final N changed) {
        N current = changed;
        while (current != null && handleCutChange(current)) {
            current = current.getParent();
        }
    }

    protected boolean handleCutChange(final N node) {
        final int prev = node.count();

        node.setCount(-1); // flag as invalid

        return prev > -1;
    }

    /** Abstract implementation of {@link BSPTree.Node}. This class is intended for use with
     * {@link AbstractBSPTree} and delegates tree mutation methods into the parent tree
     * class, where the logic can be easily overridden or extended.
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

        /** The node lying on the plus side of the cut subhyperplane; this will be null
         * for leaf nodes.
         */
        private N plus;

        /** The node lying on the minus side of the cut subhyperplane; this will be null
         * for leaf nodes.
         */
        private N minus;

        /** The total number of nodes in the subtree rooted at this node. This will be
         * set to -1 when the value needs to be computed.
         */
        private int count = -1;

        /** The depth of this node in the tree. */
        private int depth = 0;

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
            return depth;
        }

        /** {@inheritDoc} */
        @Override
        public int count() {
            if (count < 0) {
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

        protected void setDepth(final int depth) {
            this.depth = depth;
        }

        protected void setCount(final int count) {
            this.count = count;
        }

        /** Set the parent node for the instance.
         * @param parent the parent node for the instance
         */
        protected void setParent(final N parent) {
            this.parent = parent;
        }

        /** Set the minus child for this instance.
         * @param minus the minus child for this instance
         */
        protected void setMinus(final N minus) {
            this.minus = minus;
        }

        /** Set the plus child for this instance.
         * @param plus the plus child for this instance
         */
        protected void setPlus(final N plus) {
            this.plus = plus;
        }

        /** Set the cut (ie, binary partitioner) for the node.
         * @param cut the cut for the node
         */
        protected void setCut(final ConvexSubHyperplane<P> cut) {
            this.cut = cut;
        }

        /** Get a reference to the current instance, cast to type N.
         * @return a reference to the current instance, as type N.
         */
        protected abstract N getSelf();
    }
}
