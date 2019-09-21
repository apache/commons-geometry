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
import java.util.function.BiConsumer;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
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
    public Split<RegionBSPTree1S> split(final Hyperplane<Point1S> splitter) {
        return split(splitter, RegionBSPTree1S.empty(), RegionBSPTree1S.empty());
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

        final List<BoundaryPair> boundaryPairs = new ArrayList<>();

        visitInsideIntervals((min, max) -> {
            boundaryPairs.add(new BoundaryPair(min, max));
        });

        boundaryPairs.sort(BOUNDARY_PAIR_COMPARATOR);

        int boundaryPairCount = boundaryPairs.size();
        int boundaryIdx = 0;

        // check for wrap-around
        if (boundaryPairCount > 1) {
            BoundaryPair min = boundaryPairs.get(boundaryPairCount - 1);
            BoundaryPair max = boundaryPairs.get(0);

            if (min.getMax() == null && max.getMin() == null) {
                boundaryPairs.set(boundaryPairCount - 1, new BoundaryPair(min.getMin(), max.getMax()));

                ++boundaryIdx; // skip the first entry
            }
        }

        final List<AngularInterval> intervals = new ArrayList<>();

        BoundaryPair start = null;
        BoundaryPair end = null;
        BoundaryPair current = null;

        for (; boundaryIdx < boundaryPairCount; ++boundaryIdx) {
            current = boundaryPairs.get(boundaryIdx);

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

    /** Compute the min/max intervals for all interior convex regions in the tree and
     * pass the values to the given visitor function.
     * @param visitor the object that will receive the calculated min and max boundary for each
     *      insides node's convex region
     */
    private void visitInsideIntervals(final BiConsumer<CutAngle, CutAngle> visitor) {
        for (RegionNode1S node : this) {
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
    private void visitNodeInterval(final RegionNode1S node, final BiConsumer<CutAngle, CutAngle> visitor) {
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

        visitor.accept(min, max);
    }


    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<Point1S> computeRegionSizeProperties() {
        // TODO Auto-generated method stub
        return null;
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

    /** BSP tree node for one dimensional spherical space.
     */
    public static final class RegionNode1S extends AbstractRegionBSPTree.AbstractRegionNode<Point1S, RegionNode1S> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190817L;

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
            return (min != null) ? min.getPoint().getNormalizedAzimuth() : 0;
        }
    }
}
