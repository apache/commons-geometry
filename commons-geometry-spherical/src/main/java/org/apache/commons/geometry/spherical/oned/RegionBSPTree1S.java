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
package org.apache.commons.geometry.spherical.oned;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** BSP tree representing regions in 1D spherical space.
 */
public class RegionBSPTree1S extends AbstractRegionBSPTree<Point1S, RegionBSPTree1S.RegionNode1S> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190817L;

    /** Comparator used to sort BoundaryPairs by ascending azimuth.  */
    private static final Comparator<BoundaryPair> BOUNDARY_PAIR_COMPARATOR = (BoundaryPair a, BoundaryPair b) -> {
        return Double.compare(a.getMinValue(), b.getMinValue());
    };

    /** Create a new, empty instance.
     */
    public RegionBSPTree1S() {
        this(false);
    }

    /** Create a new region. If {@code full} is true, then the region will
     * represent the entire circle. Otherwise, it will be empty.
     * @param full whether or not the region should contain the entire
     *      circle or be empty
     */
    public RegionBSPTree1S(boolean full) {
        super(full);
    }

    /** Return a deep copy of this instance.
     * @return a deep copy of this instance.
     * @see {@link #copy(org.apache.commons.geometry.core.partitioning.bsp.BSPTree)}
     */
    public RegionBSPTree1S copy() {
        RegionBSPTree1S result = RegionBSPTree1S.empty();
        result.copy(this);

        return result;
    }

    /** Add an interval to this region. The resulting region will be the
     * union of the interval and the region represented by this instance.
     * @param interval the interval to add
     */
    public void add(final AngularInterval interval) {
        union(fromInterval(interval));
    }

    /** {@inheritDoc} */
    @Override
    public Point1S project(final Point1S pt) {
        final BoundaryProjector1S projector = new BoundaryProjector1S(pt);
        accept(projector);

        return projector.getProjected();
    }

    /** {@inheritDoc}
     *
     * <p>Each interval of the region is transformed individually and the
     * results are unioned. If the size of any transformed interval is greater
     * than or equal to 2pi, then the region is set to the full space.</p>
     */
    @Override
    public void transform(final Transform<Point1S> transform) {
        if (!isFull() && !isEmpty()) {
            // transform each interval individually to handle wrap-around
            final List<AngularInterval> intervals = toIntervals();

            setEmpty();

            for (AngularInterval interval : intervals) {
                union(interval.transform(transform).toTree());
            }
        }
    }

    /** {@inheritDoc}
     *
     * <p>It is important to note that split operations occur according to the rules of the
     * {@link CutAngle} hyperplane class. In this class, the continuous circle is viewed
     * as a non-circular segment of the number line in the range {@code [0, 2pi)}. Hyperplanes
     * are placed along this line and partition it into the segments {@code [0, x]}
     * and {@code [x, 2pi)}, where {@code x} is the location of the hyperplane. For example,
     * a positive-facing {@link CutAngle} instance with an azimuth of {@code 0.5pi} has
     * a minus side consisting of the angles {@code [0, 0.5pi]} and a plus side consisting of
     * the angles {@code [0.5pi, 2pi)}. Similarly, a positive-facing {@link CutAngle} with
     * an azimuth of {@code 0pi} has a plus side of {@code [0, 2pi)} (the full space) and
     * a minus side that is completely empty (since no points exist in our domain that are
     * less than zero). These rules can result in somewhat non-intuitive behavior at times.
     * For example, splitting a non-empty region with a hyperplane at {@code 0pi} is
     * essentially a no-op, since the region will either lie entirely on the plus or minus
     * side of the hyperplane (depending on the hyperplane's orientation) regardless of the actual
     * content of the region. In these situations, a copy of the tree is returned on the
     * appropriate side of the split.</p>
     *
     * @see CutAngle
     * @see #splitDiameter(CutAngle)
     */
    @Override
    public Split<RegionBSPTree1S> split(final Hyperplane<Point1S> splitter) {
        // Handle the special case where the cut is on the azimuth equivalent to zero;
        // In this case, it is not possible for any points to lie between it and zero.
        if (!isEmpty() && splitter.classify(Point1S.ZERO) == HyperplaneLocation.ON) {
            CutAngle cut = (CutAngle) splitter;
            if (cut.isPositiveFacing()) {
                return new Split<>(null, copy());
            }
            else {
                return new Split<>(copy(), null);
            }
        }

        return split(splitter, RegionBSPTree1S.empty(), RegionBSPTree1S.empty());
    }

    /** Split the instance along a circle diameter.The diameter is defined by the given
     * split point and its reversed antipodal point.
     * @param splitter split point defining one side of the split diameter
     * @return result of the split operation
     */
    public Split<RegionBSPTree1S> splitDiameter(final CutAngle splitter) {

        final CutAngle opposite = CutAngle.fromPointAndDirection(
                splitter.getPoint().antipodal(),
                !splitter.isPositiveFacing(),
                splitter.getPrecision());

        final double plusPoleOffset = splitter.isPositiveFacing() ?
                +Geometry.HALF_PI :
                -Geometry.HALF_PI;
        final Point1S plusPole = Point1S.of(splitter.getAzimuth() + plusPoleOffset);

        final boolean zeroOnPlusSide = splitter.getPrecision()
                .lte(plusPole.distance(Point1S.ZERO), Geometry.HALF_PI);

        Split<RegionBSPTree1S> firstSplit = split(splitter);
        Split<RegionBSPTree1S> secondSplit = split(opposite);

        RegionBSPTree1S minus = RegionBSPTree1S.empty();
        RegionBSPTree1S plus = RegionBSPTree1S.empty();

        if (zeroOnPlusSide) {
            // zero wrap-around needs to be handled on the plus side of the split
            safeUnion(plus, firstSplit.getPlus());
            safeUnion(plus, secondSplit.getPlus());

            minus = firstSplit.getMinus();
            if (minus != null) {
                minus = minus.split(opposite).getMinus();
            }
        }
        else {
            // zero wrap-around needs to be handled on the minus side of the split
            safeUnion(minus, firstSplit.getMinus());
            safeUnion(minus, secondSplit.getMinus());

            plus = firstSplit.getPlus();
            if (plus != null) {
                plus = plus.split(opposite).getPlus();
            }
        }

        return new Split<>(
                (minus != null && !minus.isEmpty()) ? minus : null,
                (plus != null && !plus.isEmpty()) ? plus : null);
    }


    /** Convert the region represented by this tree into a list of separate
     * {@link AngularInterval}s, arranged in order of ascending min value.
     * @return list of {@link AngularInterval}s representing this region in order of
     *      ascending min value
     */
    public List<AngularInterval> toIntervals() {
        if (isFull()) {
            return Collections.singletonList(AngularInterval.full());
        }

        final List<BoundaryPair> insideBoundaryPairs = new ArrayList<>();
        for (RegionNode1S node : this) {
            if (node.isInside()) {
                insideBoundaryPairs.add(getNodeBoundaryPair(node));
            }
        }

        insideBoundaryPairs.sort(BOUNDARY_PAIR_COMPARATOR);

        int boundaryPairCount = insideBoundaryPairs.size();
        int boundaryIdx = 0;

        // check for wrap-around
        if (boundaryPairCount > 1) {
            BoundaryPair min = insideBoundaryPairs.get(boundaryPairCount - 1);
            BoundaryPair max = insideBoundaryPairs.get(0);

            if (min.getMax() == null && max.getMin() == null) {
                insideBoundaryPairs.set(boundaryPairCount - 1, new BoundaryPair(min.getMin(), max.getMax()));

                ++boundaryIdx; // skip the first entry
            }
        }

        final List<AngularInterval> intervals = new ArrayList<>();

        BoundaryPair start = null;
        BoundaryPair end = null;
        BoundaryPair current = null;

        for (; boundaryIdx < boundaryPairCount; ++boundaryIdx) {
            current = insideBoundaryPairs.get(boundaryIdx);

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
    private AngularInterval createInterval(final BoundaryPair start, final BoundaryPair end) {
        CutAngle min = start.getMin();
        CutAngle max = end.getMax();

        DoublePrecisionContext precision = (min != null) ? min.getPrecision() : max.getPrecision();

        // flip the hyperplanes if needed since there's no
        // guarantee that the inside will be on the minus side
        // of the hyperplane (for example, if the region is complemented)

        if (min != null) {
            if (min.isPositiveFacing()) {
                min = min.reverse();
            }
        }
        else {
            min = CutAngle.createNegativeFacing(Geometry.ZERO_PI, precision);
        }

        if (max != null) {
            if (!max.isPositiveFacing()) {
                max = max.reverse();
            }
        }
        else {
            max = CutAngle.createPositiveFacing(Geometry.TWO_PI, precision);
        }

        return AngularInterval.of(min, max);
    }

    /** Return the min/max boundary pair for the convex region represented by the given node.
     * @param node the node to compute the interval for
     */
    private BoundaryPair getNodeBoundaryPair(final RegionNode1S node) {
        CutAngle min = null;
        CutAngle max = null;

        CutAngle pt;
        RegionNode1S child = node;
        RegionNode1S parent;

        while ((min == null || max == null) && (parent = child.getParent()) != null) {
            pt = (CutAngle) parent.getCutHyperplane();

            if ((pt.isPositiveFacing() && child.isMinus()) ||
                    (!pt.isPositiveFacing() && child.isPlus())) {

                if (max == null) {
                    max = pt;
                }
            }
            else if (min == null){
                min = pt;
            }

            child = parent;
        }

        return new BoundaryPair(min, max);
    }

    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<Point1S> computeRegionSizeProperties() {
        if (isFull()) {
            return new RegionSizeProperties<>(Geometry.TWO_PI, null);
        }

        double size = 0;
        double scaledBarycenterSum = 0;

        double intervalSize;

        for (AngularInterval interval : toIntervals()) {
            intervalSize = interval.getSize();

            size += intervalSize;
            scaledBarycenterSum += intervalSize * interval.getBarycenter().getNormalizedAzimuth();
        }

        final Point1S barycenter = size > 0 ?
                Point1S.of(scaledBarycenterSum / size) :
                null;

        return new RegionSizeProperties<>(size, barycenter);
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode1S createNode() {
        return new RegionNode1S(this);
    }

    /** Return a new, empty BSP tree.
     * @return a new, empty BSP tree.
     */
    public static RegionBSPTree1S empty() {
        return new RegionBSPTree1S(false);
    }

    /** Return a new, full BSP tree. The returned tree represents the
     * full space.
     * @return a new, full BSP tree.
     */
    public static RegionBSPTree1S full() {
        return new RegionBSPTree1S(true);
    }

    /** Return a new BSP tree representing the same region as the given angular interval.
     * @param interval the input interval
     * @return a new BSP tree representing the same region as the given angular interval
     */
    public static RegionBSPTree1S fromInterval(final AngularInterval interval) {
        final CutAngle minBoundary = interval.getMinBoundary();
        final CutAngle maxBoundary = interval.getMaxBoundary();

        final RegionBSPTree1S tree = full();

        if (minBoundary != null) {
            tree.insert(minBoundary.span());
        }

        if (maxBoundary != null) {
            tree.insert(maxBoundary.span());
        }

        return tree;
    }

    /** Perform a union operation with {@code target} and {@code input}, storing the result
     * in {@code target}; does nothing if {@code input} is null.
     * @param target target tree
     * @param input input tree
     */
    private static void safeUnion(final RegionBSPTree1S target, final RegionBSPTree1S input) {
        if (input != null) {
            target.union(input);
        }
    }

    /** BSP tree node for one dimensional spherical space.
     */
    public static final class RegionNode1S extends AbstractRegionBSPTree.AbstractRegionNode<Point1S, RegionNode1S> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190922L;

        /** Simple constructor.
         * @param tree the owning tree instance
         */
        protected RegionNode1S(final AbstractBSPTree<Point1S, RegionNode1S> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode1S getSelf() {
            return this;
        }
    }

    /** Internal class containing pairs of interval boundaries.
     */
    private static final class BoundaryPair {

        /** The min boundary */
        private final CutAngle min;

        /** The max boundary */
        private final CutAngle max;

        /** Simple constructor.
         * @param min min boundary hyperplane
         * @param max max boundary hyperplane
         */
        BoundaryPair(final CutAngle min, final CutAngle max) {
            this.min = min;
            this.max = max;
        }

        /** Get the minimum boundary hyperplane.
         * @return the minimum boundary hyperplane.
         */
        public CutAngle getMin() {
            return min;
        }

        /** Get the maximum boundary hyperplane.
         * @return the maximum boundary hyperplane.
         */
        public CutAngle getMax() {
            return max;
        }

        /** Get the minumum value of the interval or zero if no minimum value exists.
         * @return the minumum value of the interval or zero
         *      if no minimum value exists.
         */
        public double getMinValue() {
            return (min != null) ? min.getNormalizedAzimuth() : 0;
        }
    }

    /** Class used to project points onto the region boundary.
     */
    private static final class BoundaryProjector1S extends BoundaryProjector<Point1S, RegionNode1S> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190926L;

        /** Simple constructor.
         * @param point the point to project onto the region's boundary
         */
        public BoundaryProjector1S(final Point1S point) {
            super(point);
        }

        /** {@inheritDoc} */
        @Override
        protected boolean isPossibleClosestCut(final SubHyperplane<Point1S> cut, final Point1S target, final double minDist) {
            // since the space wraps around, consider any cut as possibly being the closest
            return true;
        }

        /** {@inheritDoc} */
        @Override
        protected Point1S disambiguateClosestPoint(final Point1S target, final Point1S a, final Point1S b) {
            // prefer the point with the smaller normalize azimuth value
            return a.getNormalizedAzimuth() < b.getNormalizedAzimuth() ? a : b;
        }
    }
}
