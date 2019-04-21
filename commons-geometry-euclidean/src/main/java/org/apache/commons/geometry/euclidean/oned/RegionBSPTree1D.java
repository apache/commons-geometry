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
import java.util.ListIterator;
import java.util.function.BiConsumer;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partition.AbstractBSPTree;
import org.apache.commons.geometry.core.partition.region.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.internal.AbstractEuclideanRegionBSPTree;

/** Binary space partitioning (BSP) tree representing a region in one dimensional
 * Euclidean space.
 */
public final class RegionBSPTree1D extends AbstractEuclideanRegionBSPTree<Vector1D, RegionBSPTree1D.RegionNode1D> {

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

    /** Convert the the region represented by this tree into a list of separate
     * {@link Interval}s, arranged in order of ascending min value. The returned
     * intervals are all assigned the given {@link DoublePrecisionContext}.
     *
     * <p>The number of intervals returned can vary depending on the value of
     * {@code precision}. This is due to the fact that adjacent intervals in
     * the returned list are merged into a single interval if one or both of
     * the following conditions are met:
     *  <ol>
     *      <li>The distance between the min values of the intervals is equal to zero
     *      as evaluated by {@code precision}</li>
     *      <li>The distance between the max values of the intervals is equal to zero
     *      as evaluated by {@code precision}.</li>
     *  </ol>
     * This merging operation prevents superfluous intervals from appearing in the returned
     * list. For example, consider the intervals {@code [0.98, 0.99], [1.0, 2.0]}. If
     * {@code precision} evaluates {@code 0.98} and {@code 1.0} as not equal, then two separate
     * intervals are returned. However, if {@code 0.98} and {@code 1.0} do evaluate as
     * equal, then no information about the region is added by keeping two separate intervals
     * with this precision context, because all points between {@code 0.98} and {@code 1.0}
     * will be evaluated as being on the region boundary. Therefore, the single interval
     * {@code [0.98, 2.0]} is returned.
     * </p>
     * @param precision object used to determine floating point equality
     * @return list of {@link Interval}s representing this region in order of
     *      ascending min value
     */
    public List<Interval> toIntervals(final DoublePrecisionContext precision) {

        final List<Interval> intervals = new ArrayList<>();

        visitInsideIntervals((min, max) -> {
           intervals.add(Interval.of(min, max, precision));
        });

        return sortAndMergeIntervals(intervals, precision);
    }

    /** Sort the given intervals and merge ones that contain endpoints that point in the same direction
     * and are equivalent according to the given precision context.
     * @param intervals the intervals to sort and merge
     * @param precision object used to determine floating point equality
     * @return sorted and merged intervals
     */
    private List<Interval> sortAndMergeIntervals(final List<Interval> intervals, final DoublePrecisionContext precision) {
        if (!intervals.isEmpty()) {
            intervals.sort(INTERVAL_COMPARATOR);

            // combine intervals based on the given precision
            ListIterator<Interval> it = intervals.listIterator();

            Interval prev;
            Interval cur;

            // merge min edges by moving forward through the list
            prev = it.next();
            while (it.hasNext()) {
                cur = it.next();

                if (precision.eq(prev.getMin(), cur.getMin()) ||
                        precision.eq(prev.getMax(), cur.getMax())) {

                    // remove the first entry in the comparison
                    it.previous();
                    it.previous();
                    it.remove();

                    // replace the second entry in the comparison with the merged
                    // interval
                    it.next();
                    prev = Interval.of(prev.getMin(), cur.getMax(), precision);
                    it.set(prev);
                }
                else {
                    prev = cur;
                }
            }
        }

        return intervals;
    }

    /** Compute the min/max intervals for all interior convex regions in the tree and
     * pass the values to the given visitor function.
     * @param visitor the object that will receive the calculated min and max for each
     *      insides node's convex region
     */
    private void visitInsideIntervals(final BiConsumer<Double, Double> visitor) {
        for (RegionNode1D node : this) {
            if (node.isInside()) {
                visitNodeInterval(node, visitor);
            }
        }
    }

    /** Compute the min/max interval for the convex region represented by the given node and pass
     * the values to the given visitor function.
     * @param node the node to compute the interval for
     * @param visitor the object that will receive the calculated min and max for the node's
     *      convex region
     */
    private void visitNodeInterval(final RegionNode1D node, final BiConsumer<Double, Double> visitor) {
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

        visitor.accept(min, max);
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode1D createNode() {
        return new RegionNode1D(this);
    }

    /** {@inheritDoc} */
    @Override
    protected EuclideanRegionProperties<Vector1D> computeRegionProperties() {
        RegionPropertiesVisitor visitor = new RegionPropertiesVisitor();

        visitInsideIntervals(visitor);

        return visitor.getRegionProperties();
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

    private static class RegionPropertiesVisitor implements BiConsumer<Double, Double>
    {
        private int count = 0;

        private double size = 0;
        private double sum = 0;

        private double lastMin;

        /** {@inheritDoc} */
        @Override
        public void accept(Double min, Double max) {
            ++count;

            final double intervalSize = max - min;
            final double intervalBarycenter = 0.5 * (max - min);

            size += intervalSize;
            sum += intervalSize * intervalBarycenter;

            lastMin = min;
        }

        public EuclideanRegionProperties<Vector1D> getRegionProperties() {
            Vector1D barycenter = null;

            if (count > 0 && Double.isFinite(size)) {
                if (size > 0.0) {
                    barycenter = Vector1D.of(sum / size);
                }
                else {
                    barycenter = Vector1D.of(lastMin);
                }
            }

            return new EuclideanRegionProperties<>(size, barycenter);
        }
    }
}
