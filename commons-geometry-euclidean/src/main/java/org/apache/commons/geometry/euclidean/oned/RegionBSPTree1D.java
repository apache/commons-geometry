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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partition.AbstractBSPTree;
import org.apache.commons.geometry.core.partition.region.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.partition.region.AbstractRegionBSPTree.AbstractRegionNode;
import org.apache.commons.geometry.core.partition.region.RegionCutBoundary;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Binary space partitioning (BSP) tree representing a region in one dimensional
 * Euclidean space.
 */
public final class RegionBSPTree1D extends AbstractRegionBSPTree<Vector1D, RegionBSPTree1D.RegionNode1D> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190405L;

    /** Comparator used to sort OrientedPoints in ascending location.  */
    private static final Comparator<OrientedPoint> ORIENTED_POINT_COMPARATOR = (OrientedPoint a, OrientedPoint b) -> {
        return Double.compare(a.getLocation().getX(), b.getLocation().getX());
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

    /** Convert the the region represented by this tree into a list
     * of separate {@link Interval}s.
     * @return list of {@link Interval}s representing this region
     */
    public List<Interval> toIntervals(final DoublePrecisionContext precision) {

        if (isEmpty()) {
            return Collections.emptyList();
        }
        else if (isFull()) {
            return Arrays.asList(Interval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, precision));
        }

        return computeIntervals(precision);
    }

    private List<Interval> computeIntervals(final DoublePrecisionContext precision) {
        final List<OrientedPoint> boundaries = getPointBoundaries();
        boundaries.sort(ORIENTED_POINT_COMPARATOR);

        final List<Interval> intervals = new ArrayList<>();

        OrientedPoint min = null;
        OrientedPoint max = null;

        for (OrientedPoint pt : boundaries)
        {
            if (min == null) {
                if (pt.isPositiveFacing()) {
                    intervals.add(Interval.of(Vector1D.NEGATIVE_INFINITY, pt.getLocation(), precision));
                }
                else {
                    min = pt;
                }
            }
            else {
                intervals.add(Interval.of(min.getLocation(), pt.getLocation(), precision));

                min = null;
            }
        }

        if (min != null) {
            intervals.add(Interval.of(min.getLocation(), Vector1D.POSITIVE_INFINITY, precision));
        }

        return intervals;
    }

    private List<OrientedPoint> getPointBoundaries() {
        final List<OrientedPoint> pointBoundaries = new ArrayList<>();

        for (RegionNode1D node : this)
        {
            if (node.isInternal())
            {
                final RegionCutBoundary<Vector1D> boundary = node.getCutBoundary();

                final OrientedPoint outsideFacing = (OrientedPoint) boundary.getOutsideFacing();
                if (outsideFacing != null)
                {
                    pointBoundaries.add(outsideFacing);
                }
            }
        }

        return pointBoundaries;
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode1D createNode() {
        return new RegionNode1D(this);
    }

    /** BSP tree node for one dimensional Euclidean space.
     */
    public static final class RegionNode1D extends AbstractRegionNode<Vector1D, RegionNode1D> {

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
}
