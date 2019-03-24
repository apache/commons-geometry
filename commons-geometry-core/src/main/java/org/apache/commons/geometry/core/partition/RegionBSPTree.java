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

import java.util.List;

import org.apache.commons.geometry.core.Point;

/** {@link BSPTree} specialized for representing regions of space. For example, this
 * class can be used to represent polygons in Euclidean 2D space and polyhedrons
 * in Euclidean 3D space.
 * @param <P> Point implementation type
 */
public class RegionBSPTree<P extends Point<P>> extends AbstractBSPTree<P, RegionBSPTree.RegionNode<P>> {

    /** Serializable UID */
    private static final long serialVersionUID = 1L;

    /** Construct a new region covering the entire space.
     */
    public RegionBSPTree() {
        this(true);
    }

    /** Construct a new region will the given boolean determining whether or not the
     * region will be full (including the entire space) or empty (excluding the entire
     * space).
     * @param full if true, the region will cover the entire space, otherwise it will
     *      be empty
     */
    public RegionBSPTree(final boolean full) {
        getRoot().setLocation(full ? RegionLocation.INSIDE : RegionLocation.OUTSIDE);
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree<P> copy() {
        return (RegionBSPTree<P>) super.copy();
    }

    /** Change this region into its complement. All inside nodes become outside
     * nodes and vice versa. The orientation of the cut subhyperplanes is not modified.
     */
    public void complement() {
        complementRecursive(getRoot());
    }

    /** Recursively switch all inside nodes to outside nodes and vice versa.
     * @param node the node at the root of the subtree to switch
     */
    private void complementRecursive(final RegionNode<P> node) {
        if (node != null)
        {
            final RegionLocation newLoc = (node.getLocationValue() == RegionLocation.INSIDE)
                    ? RegionLocation.OUTSIDE
                    : RegionLocation.INSIDE;

            node.setLocation(newLoc);

            complementRecursive(node.getMinus());
            complementRecursive(node.getPlus());
        }
    }

    /** Return true if the region is empty, i.e. if no node in the tree
     * has a location of {@link RegionLocation#INSIDE}.
     * @return true if the region does not have an inside
     */
    public boolean isEmpty() {
        return !hasNodeWithLocationRecursive(getRoot(), RegionLocation.INSIDE);
    }

    /** Return true if the region is full, i.e. if no node in the tree
     * has a location of {@link RegionLocation#OUTSIDE}. Trees with this
     * property cover the entire space.
     * @return true if the region does not have an outside
     */
    public boolean isFull() {
        return !hasNodeWithLocationRecursive(getRoot(), RegionLocation.OUTSIDE);
    }

    /** Return true if any node in the subtree rooted at the given node has a location with the
     * given value.
     * @param node the node at the root of the subtree to search
     * @param location the location to find
     * @return true if any node in the subtree has the given location
     */
    private boolean hasNodeWithLocationRecursive(final RegionNode<P> node, final RegionLocation location) {
        if (node == null) {
            return false;
        }

        return node.getLocation() == location ||
                hasNodeWithLocationRecursive(node.getMinus(), location) ||
                hasNodeWithLocationRecursive(node.getPlus(), location);
    }

    /** Classify a point with respect to the region.
     * @param point the point to classify
     * @return the classification of the point with respect to the region
     */
    public RegionLocation classify(final P point) {
        return classifyRecursive(getRoot(), point);
    }

    /** Recursively classify a point with respect to the region.
     * @param node the node to classify against
     * @param point the point to classify
     * @return the classification of the point with respect to the region rooted
     *      at the given node
     */
    private RegionLocation classifyRecursive(final RegionNode<P> node, final P point) {
        if (node.isLeaf()) {
            // the point is in a leaf, so the classification is just the leaf location
            return node.getLocation();
        }
        else {
            final Side side = node.getCutHyperplane().classify(point);

            if (side == Side.MINUS) {
                return classifyRecursive(node.getMinus(), point);
            }
            else if (side == Side.PLUS) {
                return classifyRecursive(node.getPlus(), point);
            }
            else {
                // the point is on the cut boundary; classify against both child
                // subtrees and see if we end up with the same result or not
                RegionLocation minusLoc = classifyRecursive(node.getMinus(), point);
                RegionLocation plusLoc = classifyRecursive(node.getPlus(), point);

                if (minusLoc == plusLoc) {
                    return minusLoc;
                }
                return RegionLocation.BOUNDARY;
            }
        }
    }

    public void union(final RegionBSPTree<P> other) {
        new UnionTreeMerger<P>().merge(this, other, this);
    }

    public void unionOf(final RegionBSPTree<P> regionA, final RegionBSPTree<P> regionB) {
        new UnionTreeMerger<P>().merge(regionA, regionB, this);
    }

    /** {@inheritDoc} */
    @Override
    protected RegionBSPTree<P> createTree() {
        return new RegionBSPTree<P>();
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode<P> createNode() {
        return new RegionNode<P>(this);
    }

    /** {@inheritDoc} */
    @Override
    protected void copyNodeProperties(final RegionNode<P> src, final RegionNode<P> dst) {
        dst.setLocation(src.getLocationValue());
    }

    /** Compute the portion of the node's cut subhyperplane that lies on the boundary of
     * the region.
     * @param node the node to compute the cut subhyperplane boundary of
     * @return object representing the portions of the node's cut subhyperplane that lie
     *      on the region's boundary
     */
    private RegionCutBoundary<P> computeBoundary(final RegionNode<P> node) {
        if (node.isLeaf()) {
            // no boundary for leaf nodes; they are either entirely in or
            // entirely out
            return null;
        }

        ConvexSubHyperplane<P> sub = node.getCut();

        // find the portions of the node cut sub-hyperplane that touch inside and
        // outside cells in the minus sub-tree
        SubHyperplane.Builder<P> minusInBuilder = sub.builder();
        SubHyperplane.Builder<P> minusOutBuilder = sub.builder();

        characterizeSubHyperplane(sub, node.getMinus(), minusInBuilder, minusOutBuilder);

        List<ConvexSubHyperplane<P>> minusIn = minusInBuilder.build().toConvex();
        List<ConvexSubHyperplane<P>> minusOut = minusOutBuilder.build().toConvex();

        // create the result boundary builders
        SubHyperplane.Builder<P> insideFacing = sub.builder();
        SubHyperplane.Builder<P> outsideFacing = sub.builder();

        if (!minusIn.isEmpty()) {
            // Add to the boundary anything that touches an inside cell in the minus sub-tree
            // and an outside cell in the plus sub-tree. These portions are oriented with their
            // plus side pointing to the outside of the region.
            for (ConvexSubHyperplane<P> minusInFragment : minusIn) {
                characterizeSubHyperplane(minusInFragment, node.getPlus(), null, outsideFacing);
            }
        }

        if (!minusOut.isEmpty()) {
            // Add to the boundary anything that touches an outside cell in the minus sub-tree
            // and an inside cell in the plus sub-tree. These portions are oriented with their
            // plus side pointing to the inside of the region.
            for (ConvexSubHyperplane<P> minusOutFragment : minusOut) {
                characterizeSubHyperplane(minusOutFragment, node.getPlus(), insideFacing, null);
            }
        }

        return new RegionCutBoundary<P>(insideFacing.build(), outsideFacing.build());
    }

    /** Recursive method to characterize a convex subhyperplane with respect to the region's
     * boundaries.
     * @param sub the subhyperplane to characterize
     * @param node the node to characterize the subhyperplane against
     * @param in the builder that will receive the portions of the subhyperplane that lie in the inside
     *      of the region; may be null
     * @param out the builder that will receive the portions of the subhyperplane that lie on the outside
     *      of the region; may be null
     */
    private void characterizeSubHyperplane(final ConvexSubHyperplane<P> sub, final RegionNode<P> node,
            final SubHyperplane.Builder<P> in, final SubHyperplane.Builder<P> out) {

        if (sub != null) {
            if (node.isLeaf()) {
                if (node.isInside() && in != null) {
                    in.add(sub);
                }
                else if (node.isOutside() && out != null) {
                    out.add(sub);
                }
            }
            else {
                ConvexSubHyperplane.Split<P> split = sub.split(node.getCutHyperplane());

                characterizeSubHyperplane(split.getPlus(), node.getPlus(), in, out);
                characterizeSubHyperplane(split.getMinus(), node.getMinus(), in, out);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void initChildNode(final RegionNode<P> parent, final RegionNode<P> child, final boolean isPlus) {
        super.initChildNode(parent, child, isPlus);

        child.setLocation(isPlus ? RegionLocation.OUTSIDE : RegionLocation.INSIDE);
    }

    /** {@link BSPTree.Node} implementation for use with {@link RegionBSPTree}s.
     * @param <P> Point implementation type
     */
    public static class RegionNode<P extends Point<P>> extends AbstractBSPTree.AbstractNode<P, RegionNode<P>> {

        /** Serializable UID */
        private static final long serialVersionUID = 1L;

        /** The location for the node. This will only be set on leaf nodes. */
        private RegionLocation location;

        /** Object representing the part of the node cut subhyperplane that lies on the
         * region boundary. This is calculated lazily and is only present on internal nodes.
         */
        private RegionCutBoundary<P> cutBoundary;

        /** Simple constructor.
         * @param tree owning tree instance
         */
        protected RegionNode(AbstractBSPTree<P, RegionNode<P>> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        public RegionBSPTree<P> getTree() {
            // cast to our parent tree type
            return (RegionBSPTree<P>) super.getTree();
        }

        /** Get the location of the node. This value will only be non-null for
         * leaf nodes.
         * @return the location of the node; will be null for internal nodes
         */
        public RegionLocation getLocation() {
            return isLeaf() ? location : null;
        }

        /** True if the node is a leaf node and has a location of {@link RegionLocation#INSIDE}.
         * @return true if the node is a leaf node and has a location of
         *      {@link RegionLocation#INSIDE}
         */
        public boolean isInside() {
            return getLocation() == RegionLocation.INSIDE;
        }

        /** True if the node is a leaf node and has a location of {@link RegionLocation#OUTSIDE}.
         * @return true if the node is a leaf node and has a location of
         *      {@link RegionLocation#OUTSIDE}
         */
        public boolean isOutside() {
            return getLocation() == RegionLocation.OUTSIDE;
        }

        /** Get the portion of the node's cut subhyperplane that lies on the boundary of the
         * region.
         * @return the portion of the node's cut subhyperplane that lies on the boundary of
         *      the region
         */
        public RegionCutBoundary<P> getCutBoundary() {
            if (!isLeaf()) {
                checkTreeUpdated();

                if (cutBoundary == null) {
                    cutBoundary = getTree().computeBoundary(this);
                }
            }

            return cutBoundary;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName())
                .append("[cut= ")
                .append(getCut())
                .append(", location= ")
                .append(getLocation())
                .append("]");

            return sb.toString();
        }

        /** {@inheritDoc} */
        @Override
        protected void treeUpdated() {
            super.treeUpdated();

            // null any computed boundary value since it is no longer valid
            cutBoundary = null;
        }

        /** Set the location attribute for the node.
         * @param location the location attribute for the node
         */
        private void setLocation(final RegionLocation location) {
            this.location = location;
        }

        /** Get the value of the location property, unmodified based on the
         * node's leaf state.
         * @return the value of the location property
         */
        protected RegionLocation getLocationValue() {
            return location;
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode<P> getSelf() {
            return this;
        }
    }

    private abstract static class TreeMerger<P extends Point<P>> {

        private RegionBSPTree<P> outputTree;

        public RegionBSPTree<P> merge(final RegionBSPTree<P> inputTree1, final RegionBSPTree<P> inputTree2,
                final RegionBSPTree<P> outputTree) {

            this.outputTree = outputTree;

            final RegionNode<P> root1 = inputTree1.getRoot();
            final RegionNode<P> root2 = inputTree2.getRoot();

            final RegionNode<P> outputRoot = mergeRecursive(root1, root2);
            condense(outputRoot);

            outputTree.setRoot(outputRoot);

            return outputTree;
        }

        private RegionNode<P> mergeRecursive(final RegionNode<P> node1, final RegionNode<P> node2) {

            if (node1.isLeaf() || node2.isLeaf()) {
                return mergeCell(node1, node2);
            }
            else {
                final RegionNode<P> partitioned = splitSubtree(node2, node1.getCut());

                final RegionNode<P> minus = mergeRecursive(node1.getMinus(), partitioned.getMinus());

                final RegionNode<P> plus = mergeRecursive(node1.getPlus(), partitioned.getPlus());

                final RegionNode<P> outputNode = outputNode(node1);
                outputNode.setCutState(node1.getCut(), minus, plus);

                return outputNode;
            }
        }

        private RegionNode<P> splitSubtree(final RegionNode<P> node, final ConvexSubHyperplane<P> partitioner) {
            if (node.isLeaf()) {
                return splitSubtreeLeaf(node, partitioner);
            }
            return splitSubtreeCut(node, partitioner);
        }

        private RegionNode<P> splitSubtreeLeaf(final RegionNode<P> node, final ConvexSubHyperplane<P> partitioner) {
           final RegionNode<P> parent = outputNode();
           parent.setCutState(partitioner, outputNode(node), outputNode(node));

           return parent;
        }

        private RegionNode<P> splitSubtreeCut(final RegionNode<P> node, final ConvexSubHyperplane<P> partitioner) {
            // split the partitioner and node cut with each other's hyperplanes to determine their relative positions
            final ConvexSubHyperplane.Split<P> partitionerSplit = partitioner.split(node.getCutHyperplane());
            final ConvexSubHyperplane.Split<P> nodeCutSplit = node.getCut().split(partitioner.getHyperplane());

            final Side partitionerSplitSide = partitionerSplit.getSide();
            final Side nodeCutSplitSide = nodeCutSplit.getSide();

            final RegionNode<P> result = outputNode();

            RegionNode<P> resultMinus;
            RegionNode<P> resultPlus;

            if (partitionerSplitSide == Side.PLUS) {
                if (nodeCutSplitSide == Side.PLUS) {
                    // partitioner is on node cut plus side, node cut is on partitioner plus side
                    final RegionNode<P> nodePlusSplit = splitSubtree(node.getPlus(), partitioner);

                    resultMinus = nodePlusSplit.getMinus();

                    resultPlus = outputNode(node);
                    resultPlus.setCutState(node.getCut(), outputSubtree(node.getMinus()), nodePlusSplit.getPlus());
                }
                else {
                    // partitioner is on node cut plus side, node cut is on partitioner minus side
                    final RegionNode<P> nodePlusSplit = splitSubtree(node.getPlus(), partitioner);

                    resultMinus = outputNode(node);
                    resultMinus.setCutState(node.getCut(), outputSubtree(node.getMinus()), nodePlusSplit.getMinus());

                    resultPlus = nodePlusSplit.getPlus();
                }
            }
            else if (partitionerSplitSide == Side.MINUS) {
                if (nodeCutSplitSide == Side.MINUS) {
                    // partitioner is on node cut minus side, node cut is on partitioner minus side
                    final RegionNode<P> nodeMinusSplit = splitSubtree(node.getMinus(), partitioner);

                    resultMinus = outputNode(node);
                    resultMinus.setCutState(node.getCut(), nodeMinusSplit.getMinus(), outputSubtree(node.getPlus()));

                    resultPlus = nodeMinusSplit.getPlus();
                }
                else {
                    // partitioner is on node cut minus side, node cut is on partitioner plus side
                    final RegionNode<P> nodeMinusSplit = splitSubtree(node.getMinus(), partitioner);

                    resultMinus = nodeMinusSplit.getMinus();

                    resultPlus = outputNode(node);
                    resultPlus.setCutState(node.getCut(), nodeMinusSplit.getPlus(), outputSubtree(node.getPlus()));
                }
            }
            else if (partitionerSplitSide == Side.BOTH) {
                // partitioner and node cut split each other
                final RegionNode<P> nodeMinusSplit = splitSubtree(node.getMinus(), partitionerSplit.getMinus());
                final RegionNode<P> nodePlusSplit = splitSubtree(node.getPlus(), partitionerSplit.getPlus());

                resultMinus = outputNode(node);
                resultMinus.setCutState(nodeCutSplit.getMinus(), nodeMinusSplit.getMinus(), nodePlusSplit.getMinus());

                resultPlus = outputNode(node);
                resultPlus.setCutState(nodeCutSplit.getPlus(), nodeMinusSplit.getPlus(), nodePlusSplit.getPlus());
            }
            else {
                // partitioner and node cut are parallel or anti-parallel
                final boolean sameOrientation = partitioner.getHyperplane().sameOrientation(node.getCutHyperplane());

                resultMinus = outputSubtree(sameOrientation ? node.getMinus() : node.getPlus());
                resultPlus = outputSubtree(sameOrientation ? node.getPlus() : node.getMinus());
            }


            result.setCutState(partitioner, resultMinus, resultPlus);

            return result;
        }

        protected RegionLocation condense(final RegionNode<P> node) {
            if (node.isLeaf()) {
                return node.getLocation();
            }

            final RegionLocation minusLocation = condense(node.getMinus());
            final RegionLocation plusLocation = condense(node.getPlus());

            if (minusLocation != null && plusLocation != null && minusLocation == plusLocation) {
                node.setLocation(minusLocation);
                node.clearCut();

                return minusLocation;
            }

            return null;
        }

        protected RegionNode<P> outputNode() {
            return new RegionNode<P>(outputTree);
        }

        protected RegionNode<P> outputNode(final RegionNode<P> node) {
            final RegionNode<P> copy = outputNode();
            outputTree.copyNodeProperties(node, copy);

            return copy;
        }

        protected RegionNode<P> outputSubtree(final RegionNode<P> node) {
            if (node.getTree() != outputTree) {
                final RegionNode<P> outputNode = outputNode();
                outputTree.copyRecursive(node, outputNode);

                return outputNode;
            }

            return node;
        }

        protected RegionNode<P> mergeCell(final RegionNode<P> node1, final RegionNode<P> node2) {
            final RegionNode<P> result = mergeCellInternal(node1, node2);
            return outputSubtree(result);
        }

        protected abstract RegionNode<P> mergeCellInternal(final RegionNode<P> node1, final RegionNode<P> node2);
    }

    private static class UnionTreeMerger<P extends Point<P>> extends TreeMerger<P> {

        @Override
        protected RegionNode<P> mergeCellInternal(RegionNode<P> node1, RegionNode<P> node2) {
            if (node1.isLeaf()) {
                return node1.isInside() ? node1 : node2;
            }
            else {
                return node2.isInside() ? node2 : node1;
            }
        }
    }
}
