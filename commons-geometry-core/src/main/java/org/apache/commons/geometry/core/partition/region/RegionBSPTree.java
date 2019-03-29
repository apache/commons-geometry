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
package org.apache.commons.geometry.core.partition.region;

import java.util.List;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.partition.AbstractBSPTree;
import org.apache.commons.geometry.core.partition.AbstractBSPTreeMergeSupport;
import org.apache.commons.geometry.core.partition.BSPTree;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Side;
import org.apache.commons.geometry.core.partition.SubHyperplane;

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

    /** Change this region into its complement. All inside nodes become outside
     * nodes and vice versa. The orientation of the cut subhyperplanes is not modified.
     */
    public void complement() {
        complementRecursive(getRoot());
    }

    /** Set this instance to be the complement of the given tree. The argument
     * is not modified.
     * @param tree the tree to become the complement of
     */
    public void complementOf(final RegionBSPTree<P> tree) {
        copyRecursive(tree.getRoot(), getRoot());
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

    /** Compute the union of this instance and the given region, storing the result back in
     * this instance. The argument is not modified.
     * @param other the tree to compute the union with
     */
    public void union(final RegionBSPTree<P> other) {
        new UnionOperator<P>().apply(this, other, this);
    }

    /** Compute the union of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the union operation
     * @param b second argument to the union operation
     */
    public void unionOf(final RegionBSPTree<P> a, final RegionBSPTree<P> b) {
        new UnionOperator<P>().apply(a, b, this);
    }

    /** Compute the intersection of this instance and the given region, storing the result back in
     * this instance. The argument is not modified.
     * @param other the tree to compute the intersection with
     */
    public void intersection(final RegionBSPTree<P> other) {
        new IntersectionOperator<P>().apply(this, other, this);
    }

    /** Compute the intersection of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the intersection operation
     * @param b second argument to the intersection operation
     */
    public void intersectionOf(final RegionBSPTree<P> a, final RegionBSPTree<P> b) {
        new IntersectionOperator<P>().apply(a, b, this);
    }

    /** Compute the difference of this instance and the given region, storing the result back in
     * this instance. The argument is not modified.
     * @param other the tree to compute the difference with
     */
    public void difference(final RegionBSPTree<P> other) {
        new DifferenceOperator<P>().apply(this, other, this);
    }

    /** Compute the difference of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the difference operation
     * @param b second argument to the difference operation
     */
    public void differenceOf(final RegionBSPTree<P> a, final RegionBSPTree<P> b) {
        new DifferenceOperator<P>().apply(a, b, this);
    }

    /** Compute the symmetric difference (xor) of this instance and the given region, storing the result back in
     * this instance. The argument is not modified.
     * @param other the tree to compute the symmetric difference with
     */
    public void xor(final RegionBSPTree<P> other) {
        new XorOperator<P>().apply(this, other, this);
    }

    /** Compute the symmetric difference (xor) of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the symmetric difference operation
     * @param b second argument to the symmetric difference operation
     */
    public void xorOf(final RegionBSPTree<P> a, final RegionBSPTree<P> b) {
        new XorOperator<P>().apply(a, b, this);
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

    /** Class containing the basic algorithm for merging region BSP trees.
     * @param <P> Point implementation type
     */
    public abstract static class RegionMergeOperator<P extends Point<P>> extends AbstractBSPTreeMergeSupport<P, RegionNode<P>> {

        /** Merge two input trees, storing the output in the third. The output tree can be one of the
         * input tree.
         * @param inputTree1 first input tree
         * @param inputTree2 second input tree
         * @param outputTree
         */
        public void apply(final RegionBSPTree<P> inputTree1, final RegionBSPTree<P> inputTree2,
                final RegionBSPTree<P> outputTree) {

            this.performMerge(inputTree1, inputTree2, outputTree);
            condense(outputTree.getRoot());
        }

        /** Recursively condense nodes that have children with homogenous location attributes
         * (eg, both inside, both outside) into single nodes.
         * @param node the root of the subtree to condense
         * @return the location of the successfully condensed subtree or null if no condensing was
         *      able to be performed
         */
        private RegionLocation condense(final RegionNode<P> node) {
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
    }

    /** Class for performing boolean union operations on region trees.
     * @param <P> Point implementation type
     */
    public static class UnionOperator<P extends Point<P>> extends RegionMergeOperator<P> {

        /** {@inheritDoc} */
        @Override
        protected RegionNode<P> mergeLeaf(final RegionNode<P> node1, final RegionNode<P> node2) {
            if (node1.isLeaf()) {
                return node1.isInside() ? node1 : node2;
            }

            // call again with flipped arguments
            return mergeLeaf(node2, node1);
        }
    }

    /** Class for performing boolean intersection operations on region trees.
     * @param <P> Point implementation type
     */
    public static class IntersectionOperator<P extends Point<P>> extends RegionMergeOperator<P> {

        /** {@inheritDoc} */
        @Override
        protected RegionNode<P> mergeLeaf(final RegionNode<P> node1, final RegionNode<P> node2) {
            if (node1.isLeaf()) {
                return node1.isInside() ? node2 : node1;
            }

            // call again with flipped arguments
            return mergeLeaf(node2, node1);
        }
    }

    /** Class for performing boolean difference operations on region trees.
     * @param <P> Point implementation type
     */
    public static class DifferenceOperator<P extends Point<P>> extends RegionMergeOperator<P> {

        /** {@inheritDoc} */
        @Override
        protected RegionNode<P> mergeLeaf(final RegionNode<P> node1, final RegionNode<P> node2) {
            // a region is included if it belongs in tree1 and is not in tree2

            if (node1.isInside()) {
                // this region is inside of tree1, so only include subregions that are
                // not in tree2, ie include everything in node2's complement
                final RegionNode<P> output = outputSubtree(node2);
                output.getTree().complementRecursive(output);

                return output;
            }
            else if (node2.isInside()) {
                // this region is inside of tree2 and so cannot be in the result region
                final RegionNode<P> output = outputNode();
                output.setLocation(RegionLocation.OUTSIDE);

                return output;
            }

            // this region is not in tree2, so we can include everything in tree1
            return node1;
        }
    }

    /** Class for performing boolean symmetric difference (xor) operations on region trees.
     * @param <P> Point implementation type
     */
    public static class XorOperator<P extends Point<P>> extends RegionMergeOperator<P> {

        /** {@inheritDoc} */
        @Override
        protected RegionNode<P> mergeLeaf(final RegionNode<P> node1, final RegionNode<P> node2) {
            // a region is included if it belongs in tree1 and is not in tree2 OR
            // it belongs in tree2 and is not in tree1

            if (node1.isLeaf()) {
                if (node1.isInside()) {
                    // this region is inside node1, so only include subregions that are
                    // not in node2, ie include everything in node2's complement
                    final RegionNode<P> output = outputSubtree(node2);
                    output.getTree().complementRecursive(output);

                    return output;
                }
                else {
                    // this region is not in node1, so only include subregions that
                    // in node2
                    return node2;
                }
            }

            // the operation is symmetric, so perform the same operation but with the
            // nodes flipped
            return mergeLeaf(node2, node1);
        }
    }
}
