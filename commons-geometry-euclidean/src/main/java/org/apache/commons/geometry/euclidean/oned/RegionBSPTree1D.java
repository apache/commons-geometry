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

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partition.AbstractBSPTree;
import org.apache.commons.geometry.core.partition.region.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Binary space partitioning (BSP) tree representing a region in one dimensional
 * Euclidean space.
 */
public final class RegionBSPTree1D extends AbstractRegionBSPTree<Vector1D, RegionBSPTree1D.RegionNode1D> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190405L;

    /** Comparator used to sort Intervals in ascending location.  */
    private static final Comparator<Interval> INTERVAL_COMPARATOR = (Interval a, Interval b) -> {
        return Double.compare(a.getMin(), b.getMax());
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
     * of separate {@link Interval}s. The intervals are arranged in order of
     * ascending min value.
     * @return list of {@link Interval}s representing this region in order of
     *      ascending min value
     */
    public List<Interval> toIntervals(final DoublePrecisionContext precision) {

        final List<Interval> intervals = new ArrayList<>();

        for (RegionNode1D node : this) {
            if (node.isInside()) {
                intervals.add(nodeToInterval(node, precision));
            }
        }

        intervals.sort(INTERVAL_COMPARATOR);

        return intervals;
    }

    /** Get an {@link Interval} instance representing the same convex region as the given BSP tree
     * node. This is accomplished by taking an interval representing the full number line and trimming
     * the min and max values based on the cut hyperplanes present on the path from the node to the root.
     * @param node the node containing the 1D region to convert to an interval
     * @param precision the precision context to use for the interval
     * @return an interval representing the same convex region as the given BSP tree node
     */
    private Interval nodeToInterval(final RegionNode1D node, final DoublePrecisionContext precision) {
        double min = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;

        OrientedPoint pt;
        RegionNode1D child = node;
        RegionNode1D parent;

        while ((parent = child.getParent()) != null) {
            pt = (OrientedPoint) parent.getCutHyperplane();

            if ((pt.isPositiveFacing() && child.isMinus()) ||
                    (!pt.isPositiveFacing() && child.isPlus())) {
                max = Math.min(max, pt.getLocation().getX());
            }
            else {
                min = Math.max(min, pt.getLocation().getX());
            }

            child = parent;
        }

        return Interval.of(min, max, precision);
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode1D createNode() {
        return new RegionNode1D(this);
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
}
