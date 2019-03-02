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
 * @param <T> Node attribute type
 */
public abstract class AbstractBSPTree<P extends Point<P>, T> implements BSPTree<P, T>, Serializable {

    /** Interface for objects that construct instances of {@link SimpleNode}.
     * @param <P> Point implementation type
     * @param <T> Node attribute type
     */
    protected static interface SimpleNodeFactory<P extends Point<P>, T>
        extends Function<AbstractBSPTree<P, T>, SimpleNode<P, T>> {
    }

    /** Serializable UID */
    private static final long serialVersionUID = 20190225L;

    /** Object used to create new nodes for the tree. */
    private final SimpleNodeFactory<P, T> nodeFactory;

    /** The root node for the tree. */
    private final SimpleNode<P, T> root;

    /** Default constructor.
     */
    protected AbstractBSPTree()
    {
        this(SimpleNode::new);
    }

    /** Construct a new instance that uses the given factory object to produce
     * tree nodes.
     * @param nodeFactory object used to create nodes for this instance
     */
    protected AbstractBSPTree(final SimpleNodeFactory<P, T> nodeFactory) {
        this.nodeFactory = nodeFactory;
        this.root = nodeFactory.apply(this);
    }

    /** {@inheritDoc} */
    @Override
    public Node<P, T> getRoot() {
        return getRootNode();
    }

    /** {@inheritDoc} */
    @Override
    public void visit(final BSPTreeVisitor<P, T> visitor) {
        visit(getRoot(), visitor);
    }

    /** {@inheritDoc} */
    @Override
    public Node<P, T> findNode(final P pt) {
        return findNode(getRootNode(), pt);
    }

    /** {@inheritDoc} */
    @Override
    public AbstractBSPTree<P, T> extract(final Node<P, T> node) {
        return extract(node, null);
    }

    /** {@inheritDoc} */
    @Override
    public AbstractBSPTree<P, T> extract(final Node<P, T> node, final T emptyAttr) {
        if (node.getTree() != this) {
            throw new IllegalArgumentException("Cannot extract node: node does not belong to this tree");
        }

        final AbstractBSPTree<P, T> tree = createTree();
        final SimpleNode<P, T> start = (SimpleNode<P, T>) node;

        SimpleNode<P, T> newStart = extractPathToRoot(start, tree, emptyAttr);
        copySubTree(start, newStart, tree);

        return tree;
    }

    /** Create a new node for this tree. The returned node is empty.
     * @return a new node for this tree
     */
    protected SimpleNode<P, T> createNode() {
        return nodeFactory.apply(this);
    }

    /** Copy the node attribute from {@code src} to {@code dest}.
     * @param src the source node
     * @param dst the destination node
     */
    protected void copyNodeAttribute(final SimpleNode<P, T> src, final SimpleNode<P, T> dst) {
        dst.setAttribute(src.getAttribute());
    }

    /** Create a new tree instance.
     * @return a new tree instance
     */
    protected abstract AbstractBSPTree<P, T> createTree();

    /** Get the root node as a {@link SimpleNode} instance.
     * @return the root node
     */
    protected SimpleNode<P, T> getRootNode() {
        return root;
    }

    /** Create a copy of the nodes from the given start node in this tree into the given new tree
     * instance.
     * @param start the start node in this tree
     * @param tree the tree to contain the copy of the path
     * @param emptyAttr attribute value for any new nodes
     * @return the new leaf node in {@code tree}
     */
    protected SimpleNode<P, T> extractPathToRoot(final SimpleNode<P, T> start,
            final AbstractBSPTree<P, T> tree, final T emptyAttr) {

        final SimpleNode<P, T> newStart = tree.createNode();

        SimpleNode<P, T> cur = start;
        SimpleNode<P, T> newCur = newStart;

        while (cur.parent != null) {
            // create the parent-child connections
            final SimpleNode<P, T> parent = cur.parent;
            final SimpleNode<P, T> newParent = tree.createNode();

            final SimpleNode<P, T> newSibling = tree.createNode();
            newSibling.setAttribute(emptyAttr);

            SimpleNode<P, T> newPlus;
            SimpleNode<P, T> newMinus;

            if (cur.isPlus()) {
                newPlus = newCur;
                newMinus = newSibling;
            }
            else {
                newPlus = newSibling;
                newMinus = newCur;
            }

            newParent.setCut(parent.getCut(), newPlus, newMinus);

            // copy attributes at this level
            copyNodeAttribute(cur, newCur);

            // next iteration
            cur = parent;
            newCur = newParent;
        }
        copyNodeAttribute(cur, newCur);

        return newStart;
    }

    /** Copy a subtree from this tree into the destination tree.
     * @param src the node representing the root of the subtree in this instance
     * @param dst the node representing the root of the subtree in the destintation instance
     * @param dstTree the destination tree instance
     */
    protected void copySubTree(final SimpleNode<P, T> src, final SimpleNode<P, T> dst,
            final AbstractBSPTree<P, T> dstTree) {
        copyNodeAttribute(src, dst);

        if (src.isLeaf()) {
            dst.setCut(null, null, null);
        }
        else {
            final SimpleNode<P, T> minus = src.getMinus();
            final SimpleNode<P, T> newMinus = dstTree.createNode();
            copySubTree(minus, newMinus, dstTree);

            final SimpleNode<P, T> plus = src.getPlus();
            final SimpleNode<P, T> newPlus = dstTree.createNode();
            copySubTree(minus, newMinus, dstTree);

            dst.setCut(src.getCut(), newPlus, newMinus);
       }
    }

    /** Find the smallest node in the tree containing the point, starting
     * at the given node.
     * @param start the node to begin the search with
     * @param pt the point to check
     * @return the smallest node in the tree containing the point
     */
    protected SimpleNode<P, T> findNode(SimpleNode<P, T> start, P pt) {
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
    protected void visit(final Node<P, T> node, BSPTreeVisitor<P, T> visitor) {
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
    protected boolean insertCut(final SimpleNode<P, T> node, final Hyperplane<P> cutter) {
        // cut the hyperplane using all hyperplanes from this node up
        // to the root
        ConvexSubHyperplane<P> cut = fitToCell(node, cutter.wholeHyperplane());
        if (cut == null || cut.isEmpty()) {
            // insertion failed; the node was not cut
            node.setCut(null, null, null);
            return false;
        }

        node.setCut(cut, createNode(), createNode());
        return true;
    }

    /** Fit the subhyperplane in the region defined by the given node. This method cuts the
     * given subhyperplane with the binary partitioners of all parent nodes up to the root.
     * @param node the node representing the region to fit the subhyperplane to
     * @param sub the subhyperplane to fit into the cell
     * @return the subhyperplane fit to the cell
     */
    protected ConvexSubHyperplane<P> fitToCell(final SimpleNode<P, T> node, final ConvexSubHyperplane<P> sub) {

        ConvexSubHyperplane<P> result = sub;

        SimpleNode<P, T> parentNode = node.getParent();
        SimpleNode<P, T> currentNode = node;

        while (parentNode != null && result != null) {
            SplitConvexSubHyperplane<P> split = result.split(parentNode.getCutHyperplane());

            result = currentNode.isPlus() ? split.getPlus() : split.getMinus();

            currentNode = parentNode;
            parentNode = parentNode.getParent();
        }

        return result;
    }

    /** Simple implementation of {@link BSPTree.Node}. This class is intended for use with
     * {@link AbstractBSPTree} and delegates tree mutation methods into the parent tree
     * class, where the logic can be easily overridden or extended.
     * @param <P> Point implementation type
     * @param <T> Node attribute type
     */
    public static class SimpleNode<P extends Point<P>, T> implements BSPTree.Node<P, T>, Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 20190225L;

        /** The owning tree instance */
        private final AbstractBSPTree<P, T> tree;

        /** The parent node; this will be null for the tree root node */
        private SimpleNode<P, T> parent;

        /** The subhyperplane cutting the node's region; this will be null for leaf nodes */
        private ConvexSubHyperplane<P> cut;

        /** The node lying on the plus side of the cut subhyperplane; this will be null
         * for leaf nodes.
         */
        private SimpleNode<P, T> plus;

        /** The node lying on the minus side of the cut subhyperplane; this will be null
         * for leaf nodes.
         */
        private SimpleNode<P, T> minus;

        /** The node attribute */
        private T attribute;

        /** Simple constructor.
         * @param tree the tree instance that owns this node
         */
        public SimpleNode(final AbstractBSPTree<P, T> tree) {
            this.tree = tree;
        }

        /** {@inheritDoc} */
        @Override
        public AbstractBSPTree<P, T> getTree() {
            return tree;
        }

        /** {@inheritDoc} */
        @Override
        public SimpleNode<P, T> getParent() {
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
        public SimpleNode<P, T> getPlus() {
            return plus;
        }

        /** {@inheritDoc} */
        @Override
        public SimpleNode<P, T> getMinus() {
            return minus;
        }

        /** {@inheritDoc} */
        @Override
        public void visit(final BSPTreeVisitor<P, T> visitor) {
            tree.visit(this, visitor);
        }

        /** {@inheritDoc} */
        @Override
        public T getAttribute() {
            return attribute;
        }

        /** {@inheritDoc} */
        @Override
        public void setAttribute(T attribute) {
            this.attribute = attribute;
        }

        /** {@inheritDoc} */
        @Override
        public boolean insertCut(final Hyperplane<P> cutter) {
            return tree.insertCut(this, cutter);
        }

        /** {@inheritDoc} */
        @Override
        public SimpleNode<P, T> cut(final Hyperplane<P> cutter) {
            this.insertCut(cutter);

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public SimpleNode<P, T> attr(final T attribute) {
            setAttribute(attribute);

            return this;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName())
                .append("[cut= ")
                .append(getCut())
                .append(", attribute= ")
                .append(attribute)
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

        /** Set the cut state for the node. The arguments must either all be null or all be
         * non-null.
         * @param cut the binary partitioner for the node
         * @param plus the plus child node
         * @param minus the minus child node
         */
        protected void setCut(ConvexSubHyperplane<P> cut, SimpleNode<P, T> plus, SimpleNode<P, T> minus) {
            this.cut = cut;

            if (plus != null) {
                plus.parent = this;
            }
            this.plus = plus;

            if (minus != null) {
                minus.parent = this;
            }
            this.minus = minus;
        }
    }
}
