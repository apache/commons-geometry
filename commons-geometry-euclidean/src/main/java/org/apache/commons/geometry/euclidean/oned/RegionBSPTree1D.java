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
package org.apache.commons.geometry.euclidean.oned;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.RegionCutRule;

/** Binary space partitioning (BSP) tree representing a region in one dimensional
 * Euclidean space.
 */
public final class RegionBSPTree1D extends AbstractRegionBSPTree<Vector1D, RegionBSPTree1D.RegionNode1D> {
    /** Comparator used to sort BoundaryPairs by ascending location.  */
    private static final Comparator<BoundaryPair> BOUNDARY_PAIR_COMPARATOR =
        (a, b) -> Double.compare(a.getMinValue(), b.getMinValue());

    /** Create a new, empty region.
     */
    public RegionBSPTree1D() {
        this(false);
    }

    /** Create a new region. If {@code full} is true, then the region will
     * represent the entire number line. Otherwise, it will be empty.
     * @param full whether or not the region should contain the entire
     *      number line or be empty
     */
    public RegionBSPTree1D(boolean full) {
        super(full);
    }

    /** Return a deep copy of this instance.
     * @return a deep copy of this instance.
     * @see #copy(org.apache.commons.geometry.core.partitioning.bsp.BSPTree)
     */
    public RegionBSPTree1D copy() {
        final RegionBSPTree1D result = RegionBSPTree1D.empty();
        result.copy(this);

        return result;
    }

    /** Add an interval to this region. The resulting region will be the
     * union of the interval and the region represented by this instance.
     * @param interval the interval to add
     */
    public void add(final Interval interval) {
        union(intervalToTree(interval));
    }

    /** Classify a point location with respect to the region.
     * @param x the point to classify
     * @return the location of the point with respect to the region
     * @see #classify(Point)
     */
    public RegionLocation classify(final double x) {
        return classify(Vector1D.of(x));
    }

    /** Return true if the given point location is on the inside or boundary
     * of the region.
     * @param x the location to test
     * @return true if the location is on the inside or boundary of the region
     * @see #contains(Point)
     */
    public boolean contains(final double x) {
        return contains(Vector1D.of(x));
    }

    /** {@inheritDoc}
    *
    *  <p>This method simply returns 0 because boundaries in one dimension do not
    *  have any size.</p>
    */
    @Override
    public double getBoundarySize() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D project(final Vector1D pt) {
        // use our custom projector so that we can disambiguate points that are
        // actually equidistant from the target point
        final BoundaryProjector1D projector = new BoundaryProjector1D(pt);
        accept(projector);

        return projector.getProjected();
    }

    /** {@inheritDoc}
     *
     * <p>When splitting trees representing single points with a splitter lying directly
     * on the point, the result point is placed on one side of the splitter based on its
     * orientation: if the splitter is positive-facing, the point is placed on the plus
     * side of the split; if the splitter is negative-facing, the point is placed on the
     * minus side of the split.</p>
     */
    @Override
    public Split<RegionBSPTree1D> split(final Hyperplane<Vector1D> splitter) {
        return split(splitter, RegionBSPTree1D.empty(), RegionBSPTree1D.empty());
    }

    /** Get the minimum value on the inside of the region; returns {@link Double#NEGATIVE_INFINITY}
     * if the region does not have a minimum value and {@link Double#POSITIVE_INFINITY} if
     * the region is empty.
     * @return the minimum value on the inside of the region
     */
    public double getMin() {
        double min = Double.POSITIVE_INFINITY;

        RegionNode1D node = getRoot();
        OrientedPoint pt;

        while (!node.isLeaf()) {
            pt = (OrientedPoint) node.getCutHyperplane();

            min = pt.getLocation();
            node = pt.isPositiveFacing() ? node.getMinus() : node.getPlus();
        }

        return node.isInside() ? Double.NEGATIVE_INFINITY : min;
    }

    /** Get the maximum value on the inside of the region; returns {@link Double#POSITIVE_INFINITY}
     * if the region does not have a maximum value and {@link Double#NEGATIVE_INFINITY} if
     * the region is empty.
     * @return the maximum value on the inside of the region
     */
    public double getMax() {
        double max = Double.NEGATIVE_INFINITY;

        RegionNode1D node = getRoot();
        OrientedPoint pt;

        while (!node.isLeaf()) {
            pt = (OrientedPoint) node.getCutHyperplane();

            max = pt.getLocation();
            node = pt.isPositiveFacing() ? node.getPlus() : node.getMinus();
        }

        return node.isInside() ? Double.POSITIVE_INFINITY : max;
    }

    /** Convert the region represented by this tree into a list of separate
     * {@link Interval}s, arranged in order of ascending min value.
     * @return list of {@link Interval}s representing this region in order of
     *      ascending min value
     */
    public List<Interval> toIntervals() {

        final List<BoundaryPair> boundaryPairs = new ArrayList<>();

        visitInsideIntervals((min, max) -> boundaryPairs.add(new BoundaryPair(min, max)));
        boundaryPairs.sort(BOUNDARY_PAIR_COMPARATOR);

        final List<Interval> intervals = new ArrayList<>();

        BoundaryPair start = null;
        BoundaryPair end = null;

        for (final BoundaryPair current : boundaryPairs) {
            if (start == null) {
                start = current;
                end = current;
            } else if (Objects.equals(end.getMax(), current.getMin())) {
                // these intervals should be merged
                end = current;
            } else {
                // these intervals should not be merged
                intervals.add(createInterval(start, end));

                // queue up the next pair
                start = current;
                end = current;
            }
        }

        if (start != null && end != null) {
            intervals.add(createInterval(start, end));
        }

        return intervals;
    }

    /** Create an interval instance from the min boundary from the start boundary pair and
     * the max boundary from the end boundary pair. The hyperplane directions are adjusted
     * as needed.
     * @param start starting boundary pair
     * @param end ending boundary pair
     * @return an interval created from the min boundary of the given start pair and the
     *      max boundary from the given end pair
     */
    private Interval createInterval(final BoundaryPair start, final BoundaryPair end) {
        OrientedPoint min = start.getMin();
        OrientedPoint max = end.getMax();

        // flip the hyperplanes if needed since there's no
        // guarantee that the inside will be on the minus side
        // of the hyperplane (for example, if the region is complemented)

        if (min != null && min.isPositiveFacing()) {
            min = min.reverse();
        }
        if (max != null && !max.isPositiveFacing()) {
            max = max.reverse();
        }

        return Interval.of(min, max);
    }

    /** Compute the min/max intervals for all interior convex regions in the tree and
     * pass the values to the given visitor function.
     * @param visitor the object that will receive the calculated min and max boundary for each
     *      insides node's convex region
     */
    private void visitInsideIntervals(final BiConsumer<OrientedPoint, OrientedPoint> visitor) {
        for (final RegionNode1D node : nodes()) {
            if (node.isInside()) {
                node.visitNodeInterval(visitor);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode1D createNode() {
        return new RegionNode1D(this);
    }

    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<Vector1D> computeRegionSizeProperties() {
        final RegionSizePropertiesVisitor visitor = new RegionSizePropertiesVisitor();

        visitInsideIntervals(visitor);

        return visitor.getRegionSizeProperties();
    }

    /** Returns true if the given transform would result in a swapping of the interior
     * and exterior of the region if applied.
     *
     * <p>This method always returns false since no swapping of this kind occurs in
     * 1D.</p>
     */
    @Override
    protected boolean swapsInsideOutside(final Transform<Vector1D> transform) {
        return false;
    }

    /** Return a new {@link RegionBSPTree1D} instance containing the entire space.
     * @return a new {@link RegionBSPTree1D} instance containing the entire space
     */
    public static RegionBSPTree1D full() {
        return new RegionBSPTree1D(true);
    }

    /** Return a new, empty {@link RegionBSPTree1D} instance.
     * @return a new, empty {@link RegionBSPTree1D} instance
     */
    public static RegionBSPTree1D empty() {
        return new RegionBSPTree1D(false);
    }

    /** Construct a new instance from one or more intervals. The returned tree
     * represents the same region as the union of all of the input intervals.
     * @param interval the input interval
     * @param more additional intervals to add to the region
     * @return a new instance representing the same region as the union
     *      of all of the given intervals
     */
    public static RegionBSPTree1D from(final Interval interval, final Interval... more) {
        final RegionBSPTree1D tree = intervalToTree(interval);

        for (final Interval additional : more) {
            tree.add(additional);
        }

        return tree;
    }

    /** Construct a new instance from the given collection of intervals.
     * @param intervals the intervals to populate the region with
     * @return a new instance constructed from the given collection of intervals
     */
    public static RegionBSPTree1D from(final Iterable<Interval> intervals) {
        final RegionBSPTree1D tree = new RegionBSPTree1D(false);

        for (final Interval interval : intervals) {
            tree.add(interval);
        }

        return tree;
    }

    /** Return a tree representing the same region as the given interval.
     * @param interval interval to create a tree from
     * @return a tree representing the same region as the given interval
     */
    private static RegionBSPTree1D intervalToTree(final Interval interval) {
        final OrientedPoint minBoundary = interval.getMinBoundary();
        final OrientedPoint maxBoundary = interval.getMaxBoundary();

        final RegionBSPTree1D tree = full();

        RegionNode1D node = tree.getRoot();

        if (minBoundary != null) {
            tree.setNodeCut(node, minBoundary.span(), tree.getSubtreeInitializer(RegionCutRule.MINUS_INSIDE));

            node = node.getMinus();
        }

        if (maxBoundary != null) {
            tree.setNodeCut(node, maxBoundary.span(), tree.getSubtreeInitializer(RegionCutRule.MINUS_INSIDE));
        }

        return tree;
    }

    /** BSP tree node for one dimensional Euclidean space.
     */
    public static final class RegionNode1D extends AbstractRegionBSPTree.AbstractRegionNode<Vector1D, RegionNode1D> {
        /** Simple constructor.
         * @param tree the owning tree instance
         */
        private RegionNode1D(AbstractBSPTree<Vector1D, RegionNode1D> tree) {
            super(tree);
        }

        /** Get the region represented by this node. The returned region contains
         * the entire area contained in this node, regardless of the attributes of
         * any child nodes.
         * @return the region represented by this node
         */
        public Interval getNodeRegion() {
            final NodeRegionVisitor visitor = new NodeRegionVisitor();
            visitNodeInterval(visitor);

            return visitor.getInterval();
        }

        /** Determine the min/max boundaries for the convex region represented by this node and pass
         * the values to the visitor function.
         * @param visitor the object that will receive the min and max boundaries for the node's
         *      convex region
         */
        private void visitNodeInterval(final BiConsumer<OrientedPoint, OrientedPoint> visitor) {
            OrientedPoint min = null;
            OrientedPoint max = null;

            OrientedPoint pt;
            RegionNode1D child = this;
            RegionNode1D parent;

            while ((min == null || max == null) && (parent = child.getParent()) != null) {
                pt = (OrientedPoint) parent.getCutHyperplane();

                if ((pt.isPositiveFacing() && child.isMinus()) ||
                        (!pt.isPositiveFacing() && child.isPlus())) {

                    if (max == null) {
                        max = pt;
                    }
                } else if (min == null) {
                    min = pt;
                }

                child = parent;
            }

            visitor.accept(min, max);
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode1D getSelf() {
            return this;
        }
    }

    /** Internal class containing pairs of interval boundaries.
     */
    private static final class BoundaryPair {

        /** The min boundary. */
        private final OrientedPoint min;

        /** The max boundary. */
        private final OrientedPoint max;

        /** Simple constructor.
         * @param min min boundary hyperplane
         * @param max max boundary hyperplane
         */
        BoundaryPair(final OrientedPoint min, final OrientedPoint max) {
            this.min = min;
            this.max = max;
        }

        /** Get the minimum boundary hyperplane.
         * @return the minimum boundary hyperplane.
         */
        public OrientedPoint getMin() {
            return min;
        }

        /** Get the maximum boundary hyperplane.
         * @return the maximum boundary hyperplane.
         */
        public OrientedPoint getMax() {
            return max;
        }

        /** Get the minumum value of the interval or {@link Double#NEGATIVE_INFINITY}
         * if no minimum value exists.
         * @return the minumum value of the interval or {@link Double#NEGATIVE_INFINITY}
         *      if no minimum value exists.
         */
        public double getMinValue() {
            return (min != null) ? min.getLocation() : Double.NEGATIVE_INFINITY;
        }
    }

    /** Class used to project points onto the region boundary.
     */
    private static final class BoundaryProjector1D extends BoundaryProjector<Vector1D, RegionNode1D> {
        /** Simple constructor.
         * @param point the point to project onto the region's boundary
         */
        BoundaryProjector1D(Vector1D point) {
            super(point);
        }

        /** {@inheritDoc} */
        @Override
        protected Vector1D disambiguateClosestPoint(final Vector1D target, final Vector1D a, final Vector1D b) {
            final int cmp = Vector1D.COORDINATE_ASCENDING_ORDER.compare(a, b);

            if (target.isInfinite() && target.getX() > 0) {
                // return the largest value (closest to +Infinity)
                return cmp < 0 ? b : a;
            }

            // return the smallest value
            return cmp < 0 ? a : b;
        }
    }

    /** Internal class for calculating the region of a single tree node.
     */
    private static final class NodeRegionVisitor implements BiConsumer<OrientedPoint, OrientedPoint> {

        /** The min boundary for the region. */
        private OrientedPoint min;

        /** The max boundary for the region. */
        private OrientedPoint max;

        /** {@inheritDoc} */
        @Override
        public void accept(final OrientedPoint minBoundary, final OrientedPoint maxBoundary) {
            // reverse the oriented point directions if needed
            this.min = (minBoundary != null && minBoundary.isPositiveFacing()) ? minBoundary.reverse() : minBoundary;
            this.max = (maxBoundary != null && !maxBoundary.isPositiveFacing()) ? maxBoundary.reverse() : maxBoundary;
        }

        /** Return the computed interval.
         * @return the computed interval.
         */
        public Interval getInterval() {
            return Interval.of(min, max);
        }
    }

    /** Internal class for calculating size-related properties for a {@link RegionBSPTree1D}.
     */
    private static final class RegionSizePropertiesVisitor implements BiConsumer<OrientedPoint, OrientedPoint> {
        /** Number of inside intervals visited. */
        private int count;

        /** Total computed size of all inside regions. */
        private double size;

        /** Raw sum of the centroids of each inside interval. */
        private double rawCentroidSum;

        /** The sum of the centroids of each inside interval, scaled by the size of the interval. */
        private double scaledCentroidSum;

        /** {@inheritDoc} */
        @Override
        public void accept(final OrientedPoint min, final OrientedPoint max) {
            ++count;

            final double minLoc = (min != null) ? min.getLocation() : Double.NEGATIVE_INFINITY;
            final double maxLoc = (max != null) ? max.getLocation() : Double.POSITIVE_INFINITY;

            final double intervalSize = maxLoc - minLoc;
            final double intervalCentroid = 0.5 * (maxLoc + minLoc);

            size += intervalSize;
            rawCentroidSum += intervalCentroid;
            scaledCentroidSum += intervalSize * intervalCentroid;
        }

        /** Get the computed properties for the region. This must only be called after
         * every inside interval has been visited.
         * @return properties for the region
         */
        public RegionSizeProperties<Vector1D> getRegionSizeProperties() {
            Vector1D centroid = null;

            if (count > 0 && Double.isFinite(size)) {
                if (size > 0.0) {
                    // use the scaled sum if we have a non-zero size
                    centroid = Vector1D.of(scaledCentroidSum / size);
                } else {
                    // use the raw sum if we don't have a size; this will be
                    // the case if the region only contains points with zero size
                    centroid = Vector1D.of(rawCentroidSum / count);
                }
            }

            return new RegionSizeProperties<>(size, centroid);
        }
    }
}
