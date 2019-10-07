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
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.spherical.oned.RegionBSPTree1S;

/** Class representing an arbitrary region of a great circle.
 */
public final class SubGreatCircle extends AbstractSubGreatCircle {

    /** {@inheritDoc} */
    private static final long serialVersionUID = 20191005L;

    /** The 1D region on the great circle */
    private final RegionBSPTree1S region;

    /** Construct a new, empty subhyperplane for the given great circle.
     * @param greatCircle great circle defining this instance
     */
    public SubGreatCircle(final GreatCircle greatCircle) {
        this(greatCircle, false);
    }

    public SubGreatCircle(final GreatCircle greatCircle, final boolean full) {
        this(greatCircle, new RegionBSPTree1S(full));
    }

    public SubGreatCircle(final GreatCircle greatCircle, final RegionBSPTree1S region) {
        super(greatCircle);

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
                .map(i -> GreatArc.fromInterval(getCircle(), i))
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public Split<? extends SubHyperplane<Point2S>> split(Hyperplane<Point2S> splitter) {
        // TODO Auto-generated method stub
        return null;
    }

    /** Add an arc to this instance.
     * @param arc arc to add
     * @throws IllegalArgumentException if the given arc is not from
     *      a great circle equivalent to this instance
     */
    public void add(final GreatArc arc) {
        validateGreatCircle(arc.getCircle());

        region.add(arc.getSubspaceRegion());
    }

    /** Add the region represented by the given subcircle to this instance.
     * The argument is not modified.
     * @param subcircle subcircle to add
     * @throws IllegalArgumentException if the given subcircle is not from
     *      a great circle equivalent to this instance
     */
    public void add(final SubGreatCircle subcircle) {
        validateGreatCircle(subcircle.getCircle());

        region.union(subcircle.getSubspaceRegion());
    }

    /** Validate that the given great circle is equivalent to the circle
     * defining this instance.
     * @param inputCircle the great circle to validate
     * @throws IllegalArgumentException if the argument is not equivalent
     *      to the great circle for this instance
     */
    private void validateGreatCircle(final GreatCircle inputCircle) {
        final GreatCircle circle = getCircle();

        if (!circle.eq(inputCircle)) {
            throw new IllegalArgumentException("Argument is not on the same " +
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
            }
            else if (sub instanceof SubGreatCircle) {
                subcircle.add((SubGreatCircle) sub);
            }
            else {
                throw new IllegalArgumentException("Unsupported subhyperplane type: " + sub.getClass().getName());
            }
        }
    }
}
