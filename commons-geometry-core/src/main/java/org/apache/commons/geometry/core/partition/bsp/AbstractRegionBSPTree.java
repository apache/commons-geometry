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
package org.apache.commons.geometry.core.partition.bsp;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Spatial;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.HyperplaneLocation;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SplitLocation;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.core.partition.bsp.BSPTreeVisitor.ClosestFirstVisitor;

/** {@link BSPTree} specialized for representing regions of space. For example, this
 * class can be used to represent polygons in Euclidean 2D space and polyhedrons
 * in Euclidean 3D space.
 * @param <P> Point implementation type
 */
public abstract class AbstractRegionBSPTree<P extends Point<P>, N extends AbstractRegionBSPTree.AbstractRegionNode<P, N>>
    extends AbstractBSPTree<P, N> implements Region<P> {

    /** Serializable UID */
    private static final long serialVersionUID = 1L;

    /** Value used to indicate an unknown size. */
    private static final double UNKNOWN_SIZE = -1.0;

    /** The region boundary size; this is computed when requested and then cached. */
    private double boundarySize = UNKNOWN_SIZE;

    /** The current size properties for the region. */
    private RegionSizeProperties<P> regionSizeProperties;

    /** Construct a new region will the given boolean determining whether or not the
     * region will be full (including the entire space) or empty (excluding the entire
     * space).
     * @param full if true, the region will cover the entire space, otherwise it will
     *      be empty
     */
    protected AbstractRegionBSPTree(final boolean full) {
        getRoot().setLocation(full ? RegionLocation.INSIDE : RegionLocation.OUTSIDE);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return !hasNodeWithLocationRecursive(getRoot(), RegionLocation.INSIDE);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        return !hasNodeWithLocationRecursive(getRoot(), RegionLocation.OUTSIDE);
    }

    /** Return true if any node in the subtree rooted at the given node has a location with the
     * given value.
     * @param node the node at the root of the subtree to search
     * @param location the location to find
     * @return true if any node in the subtree has the given location
     */
    private boolean hasNodeWithLocationRecursive(final AbstractRegionNode<P, N> node, final RegionLocation location) {
        if (node == null) {
            return false;
        }

        return node.getLocation() == location ||
                hasNodeWithLocationRecursive(node.getMinus(), location) ||
                hasNodeWithLocationRecursive(node.getPlus(), location);
    }

    /** Modify this instance so that it contains the entire space.
     * @see #isFull()
     */
    public void setFull() {
        final N root = getRoot();

        root.clearCut();
        root.setLocation(RegionLocation.INSIDE);
    }

    /** Modify this instance so that is is completely empty.
     * @see #isEmpty()
     */
    public void setEmpty() {
        final N root = getRoot();

        root.clearCut();
        root.setLocation(RegionLocation.OUTSIDE);
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return getRegionSizeProperties().getSize();
    }

    /** {@inheritDoc} */
    @Override
    public double getBoundarySize() {
        if (boundarySize < 0.0) {
            double sum = 0.0;

            RegionCutBoundary<P> boundary;
            for (AbstractRegionNode<P, N> node : this) {
                boundary = node.getCutBoundary();
                if (boundary != null) {
                    sum += boundary.getInsideFacing().getSize();
                    sum += boundary.getOutsideFacing().getSize();
                }
            }

            boundarySize = sum;
        }

        return boundarySize;
    }

    /** {@inheritDoc} */
    @Override
    public P project(P pt) {
        final BoundaryProjector<P, N> projector = new BoundaryProjector<>(pt);
        accept(projector);

        return projector.getProjected();
    }

    /** {@inheritDoc} */
    @Override
    public P getBarycenter() {
        return getRegionSizeProperties().getBarycenter();
    }

    /** Get the size-related properties for the region. The value is computed
     * lazily and cached.
     * @return the size-related properties for the region
     */
    protected RegionSizeProperties<P> getRegionSizeProperties() {
        if (regionSizeProperties == null) {
            regionSizeProperties = computeRegionSizeProperties();
        }

        return regionSizeProperties;
    }

    /** Compute the size-related properties of the region.
     */
    protected abstract RegionSizeProperties<P> computeRegionSizeProperties();

    /** {@inheritDoc}
     *
     * <p>If the point is {@link Spatial#isNaN() NaN}, then
     * {@link RegionLocation#OUTSIDE} is returned.</p>
     */
    @Override
    public RegionLocation classify(final P point) {
        if (point.isNaN()) {
            return RegionLocation.OUTSIDE;
        }

        return classifyRecursive(getRoot(), point);
    }

    /** Recursively classify a point with respect to the region.
     * @param node the node to classify against
     * @param point the point to classify
     * @return the classification of the point with respect to the region rooted
     *      at the given node
     */
    private RegionLocation classifyRecursive(final AbstractRegionNode<P, N> node, final P point) {
        if (node.isLeaf()) {
            // the point is in a leaf, so the classification is just the leaf location
            return node.getLocation();
        }
        else {
            final HyperplaneLocation cutLoc = node.getCutHyperplane().classify(point);

            if (cutLoc == HyperplaneLocation.MINUS) {
                return classifyRecursive(node.getMinus(), point);
            }
            else if (cutLoc == HyperplaneLocation.PLUS) {
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
    public void complement(final AbstractRegionBSPTree<P, N> tree) {
        copyRecursive(tree.getRoot(), getRoot());
        complementRecursive(getRoot());
    }

    /** Recursively switch all inside nodes to outside nodes and vice versa.
     * @param node the node at the root of the subtree to switch
     */
    private void complementRecursive(final AbstractRegionNode<P, N> node) {
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
    public void union(final AbstractRegionBSPTree<P, N> other) {
        new UnionOperator<P, N>().apply(this, other, this);
    }

    /** Compute the union of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the union operation
     * @param b second argument to the union operation
     */
    public void union(final AbstractRegionBSPTree<P, N> a, final AbstractRegionBSPTree<P, N> b) {
        new UnionOperator<P, N>().apply(a, b, this);
    }

    /** Compute the intersection of this instance and the given region, storing the result back in
     * this instance. The argument is not modified.
     * @param other the tree to compute the intersection with
     */
    public void intersection(final AbstractRegionBSPTree<P, N> other) {
        new IntersectionOperator<P, N>().apply(this, other, this);
    }

    /** Compute the intersection of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the intersection operation
     * @param b second argument to the intersection operation
     */
    public void intersection(final AbstractRegionBSPTree<P, N> a, final AbstractRegionBSPTree<P, N> b) {
        new IntersectionOperator<P, N>().apply(a, b, this);
    }

    /** Compute the difference of this instance and the given region, storing the result back in
     * this instance. The argument is not modified.
     * @param other the tree to compute the difference with
     */
    public void difference(final AbstractRegionBSPTree<P, N> other) {
        new DifferenceOperator<P, N>().apply(this, other, this);
    }

    /** Compute the difference of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the difference operation
     * @param b second argument to the difference operation
     */
    public void difference(final AbstractRegionBSPTree<P, N> a, final AbstractRegionBSPTree<P, N> b) {
        new DifferenceOperator<P, N>().apply(a, b, this);
    }

    /** Compute the symmetric difference (xor) of this instance and the given region, storing the result back in
     * this instance. The argument is not modified.
     * @param other the tree to compute the symmetric difference with
     */
    public void xor(final AbstractRegionBSPTree<P, N> other) {
        new XorOperator<P, N>().apply(this, other, this);
    }

    /** Compute the symmetric difference (xor) of the two regions passed as arguments and store the result in
     * this instance. Any nodes currently existing in this instance are removed.
     * @param a first argument to the symmetric difference operation
     * @param b second argument to the symmetric difference operation
     */
    public void xor(final AbstractRegionBSPTree<P, N> a, final AbstractRegionBSPTree<P, N> b) {
        new XorOperator<P, N>().apply(a, b, this);
    }

    /** Condense this tree by removing redundant subtrees.
     *
     * <p>This operation can be used to reduce the total number of nodes in the
     * tree after performing node manipulations. For example, if two sibling leaf
     * nodes both represent the same {@link RegionLocation}, then there is no reason
     * from the perspective of the geometric region to retain both nodes. They are
     * therefore both merged into their parent node. This method performs this
     * simplification process.
     * </p>
     */
    protected void condense() {
        condenseRecursive(getRoot());
    }

    /** Recursively condense nodes that have children with homogenous location attributes
     * (eg, both inside, both outside) into single nodes.
     * @param node the root of the subtree to condense
     * @return the location of the successfully condensed subtree or null if no condensing was
     *      able to be performed
     */
    private RegionLocation condenseRecursive(final N node) {
        if (node.isLeaf()) {
            return node.getLocation();
        }

        final RegionLocation minusLocation = condenseRecursive(node.getMinus());
        final RegionLocation plusLocation = condenseRecursive(node.getPlus());

        if (minusLocation != null && plusLocation != null && minusLocation == plusLocation) {
            node.setLocation(minusLocation);
            node.clearCut();

            return minusLocation;
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected void copyNodeProperties(final N src, final N dst) {
        dst.setLocation(src.getLocationValue());
    }

    /** Compute the portion of the node's cut subhyperplane that lies on the boundary of
     * the region.
     * @param node the node to compute the cut subhyperplane boundary of
     * @return object representing the portions of the node's cut subhyperplane that lie
     *      on the region's boundary
     */
    private RegionCutBoundary<P> computeBoundary(final N node) {
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

        List<? extends ConvexSubHyperplane<P>> minusIn = minusInBuilder.build().toConvex();
        List<? extends ConvexSubHyperplane<P>> minusOut = minusOutBuilder.build().toConvex();

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
    private void characterizeSubHyperplane(final ConvexSubHyperplane<P> sub, final AbstractRegionNode<P, N> node,
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
                final Split<? extends ConvexSubHyperplane<P>> split = sub.split(node.getCutHyperplane());

                // Continue further on down the subtree with the same subhyperplane if the
                // subhyperplane lies directly on the current node's cut
                if (split.getLocation() == SplitLocation.NEITHER) {
                    characterizeSubHyperplane(sub, node.getPlus(), in, out);
                    characterizeSubHyperplane(sub, node.getMinus(), in, out);
                }
                else {
                    characterizeSubHyperplane(split.getPlus(), node.getPlus(), in, out);
                    characterizeSubHyperplane(split.getMinus(), node.getMinus(), in, out);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void initChildNode(final N parent, final N child, final boolean isPlus) {
        super.initChildNode(parent, child, isPlus);

        child.setLocation(isPlus ? RegionLocation.OUTSIDE : RegionLocation.INSIDE);
    }

    /** {@inheritDoc} */
    @Override
    protected void incrementVersion() {
        super.incrementVersion();

        invalidateRegionProperties();
    }

    /**
     * Invalidate properties computed for the region. The properties should be recomputed
     * when next requested.
     */
    protected void invalidateRegionProperties() {
        boundarySize = UNKNOWN_SIZE;
        regionSizeProperties = null;
    }

    /** {@link BSPTree.Node} implementation for use with {@link AbstractRegionBSPTree}s.
     * @param <P> Point implementation type
     */
    public abstract static class AbstractRegionNode<P extends Point<P>, N extends AbstractRegionNode<P, N>>
        extends AbstractBSPTree.AbstractNode<P, N> {

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
        protected AbstractRegionNode(AbstractBSPTree<P, N> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        public AbstractRegionBSPTree<P, N> getTree() {
            // cast to our parent tree type
            return (AbstractRegionBSPTree<P, N>) super.getTree();
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
                    cutBoundary = getTree().computeBoundary(getSelf());
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
        protected void setLocation(final RegionLocation location) {
            this.location = location;
        }

        /** Get the value of the location property, unmodified based on the
         * node's leaf state.
         * @return the value of the location property
         */
        protected RegionLocation getLocationValue() {
            return location;
        }
    }

    /** Class containing the basic algorithm for merging region BSP trees.
     * @param <P> Point implementation type
     */
    public abstract static class RegionMergeOperator<P extends Point<P>, N extends AbstractRegionNode<P, N>> extends AbstractBSPTreeMergeSupport<P, N> {

        /** Merge two input trees, storing the output in the third. The output tree can be one of the
         * input trees. The output tree is condensed before the method returns.
         * @param inputTree1 first input tree
         * @param inputTree2 second input tree
         * @param outputTree
         */
        public void apply(final AbstractRegionBSPTree<P, N> inputTree1, final AbstractRegionBSPTree<P, N> inputTree2,
                final AbstractRegionBSPTree<P, N> outputTree) {

            this.performMerge(inputTree1, inputTree2, outputTree);

            outputTree.condense();
        }
    }

    /** Class for performing boolean union operations on region trees.
     * @param <P> Point implementation type
     */
    public static class UnionOperator<P extends Point<P>, N extends AbstractRegionNode<P, N>> extends RegionMergeOperator<P, N> {

        /** {@inheritDoc} */
        @Override
        protected N mergeLeaf(final N node1, final N node2) {
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
    public static class IntersectionOperator<P extends Point<P>, N extends AbstractRegionNode<P, N>> extends RegionMergeOperator<P, N> {

        /** {@inheritDoc} */
        @Override
        protected N mergeLeaf(final N node1, final N node2) {
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
    public static class DifferenceOperator<P extends Point<P>, N extends AbstractRegionNode<P, N>> extends RegionMergeOperator<P, N> {

        /** {@inheritDoc} */
        @Override
        protected N mergeLeaf(final N node1, final N node2) {
            // a region is included if it belongs in tree1 and is not in tree2

            if (node1.isInside()) {
                // this region is inside of tree1, so only include subregions that are
                // not in tree2, ie include everything in node2's complement
                final N output = outputSubtree(node2);
                output.getTree().complementRecursive(output);

                return output;
            }
            else if (node2.isInside()) {
                // this region is inside of tree2 and so cannot be in the result region
                final N output = outputNode();
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
    public static class XorOperator<P extends Point<P>, N extends AbstractRegionNode<P, N>> extends RegionMergeOperator<P, N> {

        /** {@inheritDoc} */
        @Override
        protected N mergeLeaf(final N node1, final N node2) {
            // a region is included if it belongs in tree1 and is not in tree2 OR
            // it belongs in tree2 and is not in tree1

            if (node1.isLeaf()) {
                if (node1.isInside()) {
                    // this region is inside node1, so only include subregions that are
                    // not in node2, ie include everything in node2's complement
                    final N output = outputSubtree(node2);
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

    /** Class used to compute the point on the region's boundary that is closest to a target point.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    protected static class BoundaryProjector<P extends Point<P>, N extends AbstractRegionNode<P, N>>
        extends ClosestFirstVisitor<P, N> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190504L;

        /** The projected point. */
        private P projected;

        /** The current closest distance to the boundary found. */
        private double minDist = -1.0;

        /** Simple constructor.
         * @param point the point to project onto the region's boundary
         */
        public BoundaryProjector(final P point) {
            super(point);
        }

        /** {@inheritDoc} */
        @Override
        public void visit(final N node) {
            final P point = getTarget();

            if (node.isInternal() && (minDist < 0.0 || node.getCutHyperplane().offset(point) <= minDist)) {
                RegionCutBoundary<P> boundary = node.getCutBoundary();
                final P boundaryPt = boundary.closest(point);

                final double dist = boundaryPt.distance(point);
                final int cmp = Double.compare(dist, minDist);

                if (minDist < 0.0 || cmp < 0) {
                    projected = boundaryPt;
                    minDist = dist;
                }
                else if (cmp == 0) {
                    // the two points are the _exact_ same distance from the reference point, so use
                    // a separate method to disambiguate them
                    projected = disambiguateClosestPoint(point, projected, boundaryPt);
                }
            }
        }

        /** Method used to determine which of points {@code a} and {@code b} should be considered
         * as the "closest" point to {@code target} when the points are exactly equidistant.
         * @param target the target point
         * @param a first point to consider
         * @param b second point to consider
         * @return which of {@code a} or {@code b} should be considered as the one closest to
         *      {@code target}
         */
        protected P disambiguateClosestPoint(final P target, final P a, final P b) {
            return a;
        }

        /** Get the projected point on the region's boundary, or null if no point could be found.
         * @return the projected point on the region's boundary
         */
        public P getProjected() {
            return projected;
        }
    }

    /** Class containing the primary size-related properties of a region. These properties
     * are typically computed at the same time, so this class serves to encapsulate the result
     * of the combined computation.
     * @param <P> Point implementation type
     */
    protected static class RegionSizeProperties <P extends Point<P>> implements Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 20190428L;

        /** The size of the region */
        private final double size;

        /** The barycenter of the region */
        private final P barycenter;

        /** Simple constructor.
         * @param size the region size
         * @param barycenter the region barycenter
         */
        public RegionSizeProperties(final double size, final P barycenter) {
            this.size = size;
            this.barycenter = barycenter;
        }

        /** Get the size of the region.
         * @return the size of the region
         */
        public double getSize() {
            return size;
        }

        /** Get the barycenter of the region.
         * @return the barycenter of the region
         */
        public P getBarycenter() {
            return barycenter;
        }
    }
}
