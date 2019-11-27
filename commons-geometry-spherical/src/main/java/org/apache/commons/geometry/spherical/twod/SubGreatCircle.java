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
package org.apache.commons.geometry.spherical.twod;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.GeometryException;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.spherical.oned.CutAngle;
import org.apache.commons.geometry.spherical.oned.RegionBSPTree1S;

/** Class representing an arbitrary region of a great circle.
 */
public final class SubGreatCircle extends AbstractSubGreatCircle {
    /** The 1D region on the great circle. */
    private final RegionBSPTree1S region;

    /** Construct a new, empty subhyperplane for the given great circle.
     * @param greatCircle great circle defining this instance
     */
    public SubGreatCircle(final GreatCircle greatCircle) {
        this(greatCircle, false);
    }

    /** Construct a new sub-region for the given great circle. If {@code full}
     * is true, then the region will cover the entire circle; otherwise,
     * it will be empty.
     * @param circle great circle that the sub-region will belong to
     * @param full if true, the sub-region will cover the entire circle;
     *      otherwise it will be empty
     */
    public SubGreatCircle(final GreatCircle circle, final boolean full) {
        this(circle, new RegionBSPTree1S(full));
    }

    /** Construct a new instance from its defining great circle and subspace region.
     * @param circle great circle that the sub-region will belong to
     * @param region subspace region
     */
    public SubGreatCircle(final GreatCircle circle, final RegionBSPTree1S region) {
        super(circle);

        this.region = region;
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree1S getSubspaceRegion() {
        return region;
    }

    /** {@inheritDoc} */
    @Override
    public SubGreatCircle transform(final Transform<Point2S> transform) {
        final GreatCircle circle = getCircle().transform(transform);

        return new SubGreatCircle(circle, region.copy());
    }

    /** {@inheritDoc} */
    @Override
    public List<GreatArc> toConvex() {
        return region.toIntervals().stream()
                .flatMap(i -> i.toConvex().stream())
                .map(i -> GreatArc.fromInterval(getCircle(), i))
                .collect(Collectors.toList());
    }

    /** {@inheritDoc}
     *
     * <p>In all cases, the current instance is not modified. However, In order to avoid
     * unnecessary copying, this method will use the current instance as the split value when
     * the instance lies entirely on the plus or minus side of the splitter. For example, if
     * this instance lies entirely on the minus side of the splitter, the subplane
     * returned by {@link Split#getMinus()} will be this instance. Similarly, {@link Split#getPlus()}
     * will return the current instance if it lies entirely on the plus side. Callers need to make
     * special note of this, since this class is mutable.</p>
     */
    @Override
    public Split<SubGreatCircle> split(final Hyperplane<Point2S> splitter) {

        final GreatCircle splitterCircle = (GreatCircle) splitter;
        final GreatCircle thisCircle = getCircle();

        final Point2S intersection = splitterCircle.intersection(thisCircle);

        SubGreatCircle minus = null;
        SubGreatCircle plus = null;

        if (intersection != null) {
            final CutAngle subSplitter = CutAngle.createPositiveFacing(
                    thisCircle.toSubspace(intersection), splitterCircle.getPrecision());

            final Split<RegionBSPTree1S> subSplit = region.splitDiameter(subSplitter);
            final SplitLocation subLoc = subSplit.getLocation();

            if (subLoc == SplitLocation.MINUS) {
                minus = this;
            } else if (subLoc == SplitLocation.PLUS) {
                plus = this;
            } else if (subLoc == SplitLocation.BOTH) {
                minus = new SubGreatCircle(thisCircle, subSplit.getMinus());
                plus =  new SubGreatCircle(thisCircle, subSplit.getPlus());
            }
        }

        return new Split<>(minus, plus);
    }

    /** Add an arc to this instance.
     * @param arc arc to add
     * @throws GeometryException if the given arc is not from
     *      a great circle equivalent to this instance
     */
    public void add(final GreatArc arc) {
        validateGreatCircle(arc.getCircle());

        region.add(arc.getSubspaceRegion());
    }

    /** Add the region represented by the given subcircle to this instance.
     * The argument is not modified.
     * @param subcircle subcircle to add
     * @throws GeometryException if the given subcircle is not from
     *      a great circle equivalent to this instance
     */
    public void add(final SubGreatCircle subcircle) {
        validateGreatCircle(subcircle.getCircle());

        region.union(subcircle.getSubspaceRegion());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append('[')
            .append("circle= ")
            .append(getCircle())
            .append(", region= ")
            .append(region)
            .append(']');

        return sb.toString();
    }

    /** Validate that the given great circle is equivalent to the circle
     * defining this instance.
     * @param inputCircle the great circle to validate
     * @throws GeometryException if the argument is not equivalent
     *      to the great circle for this instance
     */
    private void validateGreatCircle(final GreatCircle inputCircle) {
        final GreatCircle circle = getCircle();

        if (!circle.eq(inputCircle)) {
            throw new GeometryException("Argument is not on the same " +
                    "great circle. Expected " + circle + " but was " +
                    inputCircle);
        }
    }

    /** {@link Builder} implementation for subcircles.
     */
    public static final class SubGreatCircleBuilder implements SubHyperplane.Builder<Point2S> {

        /** SubGreatCircle instance created by this builder. */
        private final SubGreatCircle subcircle;

        /** Construct a new instance for building regions for the given great circle.
         * @param circle the underlying great circle for the region
         */
        public SubGreatCircleBuilder(final GreatCircle circle) {
            this.subcircle = new SubGreatCircle(circle);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final SubHyperplane<Point2S> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final ConvexSubHyperplane<Point2S> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public SubGreatCircle build() {
            return subcircle;
        }

        /** Internal method for adding subhyperplanes to this builder.
         * @param sub the subhyperplane to add; either convex or non-convex
         */
        private void addInternal(final SubHyperplane<Point2S> sub) {
            if (sub instanceof GreatArc) {
                subcircle.add((GreatArc) sub);
            } else if (sub instanceof SubGreatCircle) {
                subcircle.add((SubGreatCircle) sub);
            } else {
                throw new IllegalArgumentException("Unsupported subhyperplane type: " + sub.getClass().getName());
            }
        }
    }
}
