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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partition.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partition.bsp.AbstractRegionBSPTree;

/** Binary space partitioning (BSP) tree representing a region in one dimensional
 * Euclidean space.
 */
public final class RegionBSPTree1D extends AbstractRegionBSPTree<Vector1D, RegionBSPTree1D.RegionNode1D> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190405L;

    /** Comparator used to sort BoundaryPairs by ascending location.  */
    private static final Comparator<BoundaryPair> BOUNDARY_PAIR_COMPARATOR = (BoundaryPair a, BoundaryPair b) -> {
        return Double.compare(a.getMinValue(), b.getMinValue());
    };

    /** Create a new region representing the entire number line.
     */
    public RegionBSPTree1D() {
        this(true);
    }

    /** Create a new region. If {@code full} is true, then the region will
     * represent the entire number line. Otherwise, it will be empty.
     * @param full whether or not the region should contain the entire
     *      number line or be empty
     */
    public RegionBSPTree1D(boolean full) {
        super(full);
    }

    /** Add an interval to this region. The resulting region will be the
     * union of the interval and the region represented by this instance.
     * @param interval the interval to add
     */
    public void add(final Interval interval) {
        union(interval.toTree());
    }

    /** Classify a point location with respect to the region. This is
     * a convenience overload of {@link #classify(Vector1D)} for
     * use in one dimension.
     * @param x the point to classify
     * @return the location of the point with respect to the region
     * @see #classify(Vector1D)
     */
    public RegionLocation classify(final double x) {
        return classify(Vector1D.of(x));
    }

    /** Return true if the given point location is on the inside or boundary
     * of the region. This is a convenience overload of {@link Interval#contains(Vector1D)}
     * for use in one dimension.
     * @param x the location to test
     * @return true if the location is on the inside or boundary of the region
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
   public Vector1D project(Vector1D pt) {
       // use our custom projector so that we can disambiguate points that are
       // actually equidistant from the target point
       final BoundaryProjector1D projector = new BoundaryProjector1D(pt);
       visit(projector);

       return projector.getProjected();
   }

    /** Get the minimum value on the inside of the region; returns {@link Double#NEGATIVE_INFINITY}
     * if the region does not have a minimum value and {@link Double#POSITIVE_INFINITE} if
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

    /** Convert the the region represented by this tree into a list of separate
     * {@link Interval}s, arranged in order of ascending min value.
     * @return list of {@link Interval}s representing this region in order of
     *      ascending min value
     */
    public List<Interval> toIntervals() {

        final List<BoundaryPair> boundaryPairs = new ArrayList<>();

        visitInsideIntervals((min, max) -> {
            boundaryPairs.add(new BoundaryPair(min, max));
        });

        boundaryPairs.sort(BOUNDARY_PAIR_COMPARATOR);

        final List<Interval> intervals = new ArrayList<>();

        BoundaryPair start = null;
        BoundaryPair end = null;

        for (BoundaryPair current : boundaryPairs) {
            if (start == null) {
                start = current;
                end = current;
            }
            else if (Objects.equals(end.getMax(), current.getMin())) {
                // these intervals should be merged
                end = current;
            }
            else {
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
        for (RegionNode1D node : this) {
            if (node.isInside()) {
                visitNodeInterval(node, visitor);
            }
        }
    }

    /** Determine the min/max boundaries for the convex region represented by the given node and pass
     * the values to the visitor function.
     * @param node the node to compute the interval for
     * @param visitor the object that will receive the min and max boundaries for the node's
     *      convex region
     */
    private void visitNodeInterval(final RegionNode1D node, final BiConsumer<OrientedPoint, OrientedPoint> visitor) {
        OrientedPoint min = null;
        OrientedPoint max = null;

        OrientedPoint pt;
        RegionNode1D child = node;
        RegionNode1D parent;

        while ((parent = child.getParent()) != null) {
            pt = (OrientedPoint) parent.getCutHyperplane();

            if ((pt.isPositiveFacing() && child.isMinus()) ||
                    (!pt.isPositiveFacing() && child.isPlus())) {

                // max side; check for a new max
                if (max == null || pt.getLocation() < max.getLocation()) {
                    max = pt;
                }
            }
            else {
                // min side; check for a new min
                if (min == null || pt.getLocation() > min.getLocation()) {
                    min = pt;
                }
            }

            child = parent;
        }

        visitor.accept(min, max);
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode1D createNode() {
        return new RegionNode1D(this);
    }

    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<Vector1D> computeRegionSizeProperties() {
        RegionSizePropertiesVisitor visitor = new RegionSizePropertiesVisitor();

        visitInsideIntervals(visitor);

        return visitor.getRegionSizeProperties();
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

    /** Construct a new instance from the given collection of intervals.
     * @param intervals the intervals to populate the region with
     * @return a new instance constructed from the given collection of intervals
     */
    public static RegionBSPTree1D fromIntervals(Iterable<Interval> intervals) {
        RegionBSPTree1D tree = new RegionBSPTree1D(false);

        for (Interval interval : intervals) {
            tree.add(interval);
        }

        return tree;
    }

    /** Construct a new instance from the given intervals.
     * @param intervals the intervals to populate the region with
     * @return a new instance constructed from the given intervals
     */
    public static RegionBSPTree1D fromIntervals(Interval ... intervals) {
        return fromIntervals(Arrays.asList(intervals));
    }

    /** BSP tree node for one dimensional Euclidean space.
     */
    public static final class RegionNode1D extends AbstractRegionBSPTree.AbstractRegionNode<Vector1D, RegionNode1D> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190405L;

        /** Simple constructor.
         * @param tree the owning tre instance
         */
        private RegionNode1D(AbstractBSPTree<Vector1D, RegionNode1D> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode1D getSelf() {
            return this;
        }
    }

    /** Internal class containing pairs of interval boundaries.
     */
    private static final class BoundaryPair implements Comparable<BoundaryPair> {

        /** The min boundary */
        private final OrientedPoint min;

        /** The max boundary */
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

        /** {@inheritDoc} */
        @Override
        public int compareTo(BoundaryPair other) {
            return Double.compare(this.getMinValue(), other.getMinValue());
        }
    }

    /** Class used to project points onto the region boundary.
     */
    private static final class BoundaryProjector1D extends BoundaryProjector<Vector1D, RegionNode1D> {

        /** Serializable UID */
        private static final long serialVersionUID = 1L;

        /** Simple constructor.
         * @param point the point to project onto the region's boundary
         */
        public BoundaryProjector1D(Vector1D point) {
            super(point);
        }

        /** {@inheritDoc} */
        @Override
        protected Vector1D disambiguateClosestPoint(final Vector1D target, final Vector1D a, final Vector1D b) {
            final int cmp = Vector1D.STRICT_ASCENDING_ORDER.compare(a, b);

            if (target.isInfinite() && target.getX() > 0) {
                // return the largest value (closest to +Infinity)
                return cmp < 0 ? b : a;
            }

            // return the smallest value
            return cmp < 0 ? a : b;
        }
    }

    /** Internal class for calculating size-related properties for a {@link RegionBSPTree1D}.
     */
    private static final class RegionSizePropertiesVisitor implements BiConsumer<OrientedPoint, OrientedPoint>
    {
        /** Number of inside intervals visited. */
        private int count = 0;

        /** Total computed size of all inside regions. */
        private double size = 0;

        /** Raw sum of the barycenters of each inside interval. */
        private double rawBarycenterSum = 0;

        /** The sum of the barycenter of each inside interval, scaled by the size of the interval. */
        private double scaledBarycenterSum = 0;

        /** {@inheritDoc} */
        @Override
        public void accept(OrientedPoint min, OrientedPoint max) {
            ++count;

            final double minLoc = (min != null) ? min.getLocation() : Double.NEGATIVE_INFINITY;
            final double maxLoc = (max != null) ? max.getLocation() : Double.POSITIVE_INFINITY;

            final double intervalSize = maxLoc - minLoc;
            final double intervalBarycenter = 0.5 * (maxLoc + minLoc);

            size += intervalSize;
            rawBarycenterSum += intervalBarycenter;
            scaledBarycenterSum += intervalSize * intervalBarycenter;
        }

        /** Get the computed properties for the region. This must only be called after
         * every inside interval has been visited.
         * @return properties for the region
         */
        public RegionSizeProperties<Vector1D> getRegionSizeProperties() {
            Vector1D barycenter = null;

            if (count > 0 && Double.isFinite(size)) {
                if (size > 0.0) {
                    // use the scaled sum if we have a non-zero size
                    barycenter = Vector1D.of(scaledBarycenterSum / size);
                }
                else {
                    // use the raw sum if we don't have a size; this will be
                    // the case if the region only contains points with zero size
                    barycenter = Vector1D.of(rawBarycenterSum / count);
                }
            }

            return new RegionSizeProperties<>(size, barycenter);
        }
    }
}
