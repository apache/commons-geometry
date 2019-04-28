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
import java.util.function.BiConsumer;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partition.AbstractBSPTree;
import org.apache.commons.geometry.core.partition.region.AbstractRegionBSPTree;
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
     * {@link Interval}s, arranged in order of ascending min value.
     * @return list of {@link Interval}s representing this region in order of
     *      ascending min value
     */
    public List<Interval> toIntervals() {

        final List<Interval> intervals = new ArrayList<>();

        visitInsideIntervals((min, max) -> {
            // flip the hyperplanes if needed since there's no
            // guarantee that the inside will be on the minus side
            // of the hyperplane (for example, if the region is complemented)

            if (min != null && min.isPositiveFacing()) {
                min = min.reverse();
            }
            if (max != null && !max.isPositiveFacing()) {
                max = max.reverse();
            }

            intervals.add(Interval.of(min, max));
        });

        intervals.sort(INTERVAL_COMPARATOR);

        return intervals;
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
    protected EuclideanRegionProperties<Vector1D> computeRegionProperties() {
        RegionPropertiesVisitor visitor = new RegionPropertiesVisitor();

        visitInsideIntervals(visitor);

        return visitor.getRegionProperties();
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

    /** Class for calculating general properties for a {@link RegionBSPTree1D}.
     */
    private static class RegionPropertiesVisitor implements BiConsumer<OrientedPoint, OrientedPoint>
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
        public EuclideanRegionProperties<Vector1D> getRegionProperties() {
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

            return new EuclideanRegionProperties<>(size, barycenter);
        }
    }
}
