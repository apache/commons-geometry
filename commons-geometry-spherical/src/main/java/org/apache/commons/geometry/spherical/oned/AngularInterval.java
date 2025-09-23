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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;

/** Class representing an angular interval of size greater than zero to {@code 2pi}. The interval is
 * defined by two azimuth angles: a min and a max. The interval starts at the min azimuth angle and
 * contains all points in the direction of increasing azimuth angles up to max.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public class AngularInterval implements HyperplaneBoundedRegion<Point1S> {
    /** The minimum boundary of the interval. */
    private final CutAngle minBoundary;

    /** The maximum boundary of the interval. */
    private final CutAngle maxBoundary;

    /** Point halfway between the min and max boundaries. */
    private final Point1S midpoint;

    /** Flag set to true if the interval wraps around the {@code 0/2pi} point. */
    private final boolean wraps;

    /** Construct a new instance representing the angular region between the given
     * min and max azimuth boundaries. The arguments must be either all finite or all
     * null (to indicate the full space). If the boundaries are finite, then the min
     * boundary azimuth value must be numerically less than the max boundary. Callers are
     * responsible for enforcing these constraints. No validation is performed.
     * @param minBoundary minimum boundary for the interval
     * @param maxBoundary maximum boundary for the interval
     */
    AngularInterval(final CutAngle minBoundary, final CutAngle maxBoundary) {

        this.minBoundary = minBoundary;
        this.maxBoundary = maxBoundary;

        Point1S midpointVal = null;
        boolean wrapsVal = false;

        if (minBoundary != null && maxBoundary != null) {
            midpointVal = Point1S.of(0.5 * (minBoundary.getAzimuth() + maxBoundary.getAzimuth()));

            // The interval wraps zero if the max boundary lies on the other side of zero than
            // the min. This is a more reliable way to compute the wrapping flag than direct
            // comparison of the normalized azimuths since this approach takes into account
            // azimuths that are equivalent to zero.
            wrapsVal = minBoundary.classify(maxBoundary.getPoint()) == HyperplaneLocation.PLUS;
        }

        this.midpoint =  midpointVal;
        this.wraps = wrapsVal;
    }

    /** Get the minimum azimuth angle for the interval, or {@code 0}
     * if the interval is full.
     * @return the minimum azimuth angle for the interval or {@code 0}
     *      if the interval represents the full space.
     */
    public double getMin() {
        return (minBoundary != null) ?
                minBoundary.getAzimuth() :
                0.0;
    }

    /** Get the minimum boundary for the interval, or {@code null} if the
     * interval represents the full space.
     * @return the minimum point for the interval or {@code null} if
     *      the interval represents the full space
     */
    public CutAngle getMinBoundary() {
        return minBoundary;
    }

    /** Get the maximum azimuth angle for the interval, or {@code 2pi} if
     * the interval represents the full space.
     * @return the maximum azimuth angle for the interval or {@code 2pi} if
     *      the interval represents the full space.
     */
    public double getMax() {
        return (maxBoundary != null) ?
                maxBoundary.getAzimuth() :
                Angle.TWO_PI;
    }

    /** Get the maximum point for the interval. This will be {@code null} if the
     * interval represents the full space.
     * @return the maximum point for the interval or {@code null} if
     *      the interval represents the full space
     */
    public CutAngle getMaxBoundary() {
        return maxBoundary;
    }

    /** Get the midpoint of the interval or {@code null} if the interval represents
     *  the full space.
     * @return the midpoint of the interval or {@code null} if the interval represents
     *      the full space
     * @see #getCentroid()
     */
    public Point1S getMidPoint() {
        return midpoint;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        // minBoundary and maxBoundary are either both null or both not null
        return minBoundary == null;
    }

    /** {@inheritDoc}
     *
     * <p>This method always returns false.</p>
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return getMax() - getMin();
    }

    /** {@inheritDoc}
     *
     * <p>This method simply returns 0 because boundaries in one dimension do not
     *  have any size.</p>
     */
    @Override
    public double getBoundarySize() {
        return 0;
    }

    /** {@inheritDoc}
     *
     * <p>This method is an alias for {@link #getMidPoint()}.</p>
     * @see #getMidPoint()
     */
    @Override
    public Point1S getCentroid() {
        return getMidPoint();
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final Point1S pt) {
        if (!isFull()) {
            final HyperplaneLocation minLoc = minBoundary.classify(pt);
            final HyperplaneLocation maxLoc = maxBoundary.classify(pt);

            if (minLoc == HyperplaneLocation.ON || maxLoc == HyperplaneLocation.ON) {
                return RegionLocation.BOUNDARY;
            } else if ((!wraps && (minLoc == HyperplaneLocation.PLUS || maxLoc == HyperplaneLocation.PLUS)) ||
                    (wraps && minLoc == HyperplaneLocation.PLUS && maxLoc == HyperplaneLocation.PLUS)) {
                return RegionLocation.OUTSIDE;
            }
        }
        return RegionLocation.INSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public Point1S project(final Point1S pt) {
        if (!isFull()) {
            final double minDist = minBoundary.getPoint().distance(pt);
            final double maxDist = maxBoundary.getPoint().distance(pt);

            return (minDist <= maxDist) ?
                    minBoundary.getPoint() :
                    maxBoundary.getPoint();
        }
        return null;
    }

    /** Return true if the interval wraps around the zero/{@code 2pi} point. In this
     * case, the max boundary azimuth is less than that of the min boundary when both
     * values are normalized to the range {@code [0, 2pi)}.
     * @return true if the interval wraps around the zero/{@code 2pi} point
     */
    public boolean wrapsZero() {
        return wraps;
    }

    /** Return a new instance transformed by the argument. If the transformed size
     * of the interval is greater than or equal to 2pi, then an interval representing
     * the full space is returned.
     * @param transform transform to apply
     * @return a new instance transformed by the argument
     */
    public AngularInterval transform(final Transform<Point1S> transform) {
        return transform(this, transform, AngularInterval::of);
    }

    /** {@inheritDoc}
    *
    * <p>This method returns instances of {@link RegionBSPTree1S} instead of
    * {@link AngularInterval} since it is possible for a convex angular interval
    * to be split into disjoint regions by a single hyperplane. These disjoint
    * regions cannot be represented by this class and require the use of a BSP
    * tree.</p>
    *
    * @see RegionBSPTree1S#split(Hyperplane)
    */
    @Override
    public Split<RegionBSPTree1S> split(final Hyperplane<Point1S> splitter) {
        return toTree().split(splitter);
    }

    /** Return a {@link RegionBSPTree1S} instance representing the same region
     * as this instance.
     * @return a BSP tree representing the same region as this instance
     */
    public RegionBSPTree1S toTree() {
        return RegionBSPTree1S.fromInterval(this);
    }

    /** Return a list of convex intervals comprising this region.
     * @return a list of convex intervals comprising this region
     * @see Convex
     */
    public List<AngularInterval.Convex> toConvex() {
        if (isConvex(minBoundary, maxBoundary)) {
            return Collections.singletonList(new Convex(minBoundary, maxBoundary));
        }

        final CutAngle midPos = CutAngles.createPositiveFacing(midpoint, minBoundary.getPrecision());
        final CutAngle midNeg = CutAngles.createNegativeFacing(midpoint, maxBoundary.getPrecision());

        return Arrays.asList(
                    new Convex(minBoundary, midPos),
                    new Convex(midNeg, maxBoundary)
                );
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[min= ")
            .append(getMin())
            .append(", max= ")
            .append(getMax())
            .append(']');

        return sb.toString();
    }

    /** Return an instance representing the full space. The returned instance contains all
     * possible azimuth angles.
     * @return an interval representing the full space
     */
    public static AngularInterval.Convex full() {
        return Convex.FULL;
    }

    /** Return an instance representing the angular interval between the given min and max azimuth
     * values. The max value is adjusted to be numerically above the min value, even if the resulting
     * azimuth value is greater than or equal to {@code 2pi}. An instance representing the full space
     * is returned if either point is infinite or min and max are equivalent as evaluated by the
     * given precision context.
     * @param min min azimuth value
     * @param max max azimuth value
     * @param precision precision context used to compare floating point values
     * @return a new instance resulting the angular region between the given min and max azimuths
     * @throws IllegalArgumentException if either azimuth is infinite or NaN
     */
    public static AngularInterval of(final double min, final double max,
            final Precision.DoubleEquivalence precision) {
        return of(Point1S.of(min), Point1S.of(max), precision);
    }

    /** Return an instance representing the angular interval between the given min and max azimuth
     * points. The max point is adjusted to be numerically above the min point, even if the resulting
     * azimuth value is greater than or equal to {@code 2pi}. An instance representing the full space
     * is returned if either point is infinite or min and max are equivalent as evaluated by the
     * given precision context.
     * @param min min azimuth value
     * @param max max azimuth value
     * @param precision precision context used to compare floating point values
     * @return a new instance resulting the angular region between the given min and max points
     * @throws IllegalArgumentException if either azimuth is infinite or NaN
     */
    public static AngularInterval of(final Point1S min, final Point1S max,
            final Precision.DoubleEquivalence precision) {
        return createInterval(min, max, precision, AngularInterval::new, Convex.FULL);
    }

    /** Return an instance representing the angular interval between the given oriented points.
     * The negative-facing point is used as the minimum boundary and the positive-facing point is
     * adjusted to be above the minimum. The arguments can be given in any order. The full space
     * is returned if the points are equivalent or are oriented in the same direction.
     * @param a first oriented point
     * @param b second oriented point
     * @return an instance representing the angular interval between the given oriented points
     * @throws IllegalArgumentException if either argument is infinite or NaN
     */
    public static AngularInterval of(final CutAngle a, final CutAngle b) {
        return createInterval(a, b, AngularInterval::new, Convex.FULL);
    }

    /** Internal method to create an interval between the given min and max points. The max point
     * is adjusted to be numerically above the min point, even if the resulting
     * azimuth value is greater than or equal to {@code 2pi}. The full instance argument
     * is returned if either point is infinite or min and max are equivalent as evaluated by the
     * given precision context.
     * @param min min azimuth value
     * @param max max azimuth value
     * @param precision precision context used to compare floating point values
     * @param factory factory object used to create new instances; this object is passed the validated
     *      min (negative-facing) cut and the max (positive-facing) cut, in that order
     * @param <T> Angular interval implementation type
     * @param fullSpace instance returned if the interval should represent the full space
     * @return a new instance resulting the angular region between the given min and max points
     * @throws IllegalArgumentException if either azimuth is infinite or NaN
     */
    private static <T extends AngularInterval> T createInterval(final Point1S min, final Point1S max,
            final Precision.DoubleEquivalence precision,
            final BiFunction<? super CutAngle, ? super CutAngle, T> factory, final T fullSpace) {

        validateIntervalValues(min, max);

        // return the full space if either point is infinite or the points are equivalent
        if (min.eq(max, precision)) {
            return fullSpace;
        }

        final Point1S adjustedMax = max.above(min);

        return factory.apply(
                    CutAngles.createNegativeFacing(min, precision),
                    CutAngles.createPositiveFacing(adjustedMax, precision)
                );
    }

    /** Internal method to create a new interval instance from the given cut angles.
     * The negative-facing point is used as the minimum boundary and the positive-facing point is
     * adjusted to be above the minimum. The arguments can be given in any order. The full space
     * argument is returned if the points are equivalent or are oriented in the same direction.
     * @param a first cut point
     * @param b second cut point
     * @param factory factory object used to create new instances; this object is passed the validated
     *      min (negative-facing) cut and the max (positive-facing) cut, in that order
     * @param fullSpace instance returned if the interval should represent the full space
     * @param <T> Angular interval implementation type
     * @return a new interval instance created from the given cut angles
     * @throws IllegalArgumentException if either argument is infinite or NaN
     */
    private static <T extends AngularInterval> T createInterval(final CutAngle a, final CutAngle b,
            final BiFunction<? super CutAngle, ? super CutAngle, T> factory, final T fullSpace) {

        final Point1S aPoint = a.getPoint();
        final Point1S bPoint = b.getPoint();

        validateIntervalValues(aPoint, bPoint);

        if (a.isPositiveFacing() == b.isPositiveFacing() ||
                aPoint.eq(bPoint, a.getPrecision()) ||
                bPoint.eq(aPoint, b.getPrecision())) {
            // points are equivalent or facing in the same direction
            return fullSpace;
        }

        final CutAngle min = a.isPositiveFacing() ? b : a;
        final CutAngle max = a.isPositiveFacing() ? a : b;
        final CutAngle adjustedMax = CutAngles.createPositiveFacing(
                max.getPoint().above(min.getPoint()),
                max.getPrecision());

        return factory.apply(min, adjustedMax);
    }

    /** Validate that the given points can be used to specify an angular interval.
     * @param a first point
     * @param b second point
     * @throws IllegalArgumentException if either point is infinite NaN
     */
    private static void validateIntervalValues(final Point1S a, final Point1S b) {
        if (!a.isFinite() || !b.isFinite()) {
            throw new IllegalArgumentException(MessageFormat.format("Invalid angular interval: [{0}, {1}]",
                    a.getAzimuth(), b.getAzimuth()));
        }
    }

    /** Return true if the given cut angles define a convex region. By convention, the
     * precision context from the min cut is used for the floating point comparison.
     * @param min min (negative-facing) cut angle
     * @param max max (positive-facing) cut angle
     * @return true if the given cut angles define a convex region
     */
    private static boolean isConvex(final CutAngle min, final CutAngle max) {
        if (min != null && max != null) {
            final double dist = max.getAzimuth() - min.getAzimuth();
            final Precision.DoubleEquivalence precision = min.getPrecision();
            return precision.lte(dist, Math.PI);
        }

        return true;
    }

    /** Internal transform method that transforms the given instance, using the factory
     * method to create a new instance if needed.
     * @param interval interval to transform
     * @param transform transform to apply
     * @param factory object used to create new instances
     * @param <T> Angular interval implementation type
     * @return a transformed instance
     */
    private static <T extends AngularInterval> T transform(final T interval,
            final Transform<Point1S> transform,
            final BiFunction<? super CutAngle, ? super CutAngle, T> factory) {

        if (!interval.isFull()) {
            final CutAngle tMin = interval.getMinBoundary().transform(transform);
            final CutAngle tMax = interval.getMaxBoundary().transform(transform);

            return factory.apply(tMin, tMax);
        }

        return interval;
    }

    /** Class representing an angular interval with the additional property that the
     * region is convex. By convex, it is meant that the shortest path between any
     * two points in the region is also contained entirely in the region. If there is
     * a tie for shortest path, then it is sufficient that at least one lie entirely
     * within the region. For spherical 1D space, this means that the angular interval
     * is either completely full or has a length less than or equal to {@code pi}.
     */
    public static final class Convex extends AngularInterval {
        /** Interval instance representing the full space. */
        private static final Convex FULL = new Convex(null, null);

        /** Construct a new convex instance from its boundaries and midpoint. No validation
         * of the argument is performed. Callers are responsible for ensuring that the size
         * of interval is less than or equal to {@code pi}.
         * @param minBoundary minimum boundary for the interval
         * @param maxBoundary maximum boundary for the interval
         * @throws IllegalArgumentException if the interval is not convex
         */
        Convex(final CutAngle minBoundary, final CutAngle maxBoundary) {
            super(minBoundary, maxBoundary);

            if (!isConvex(minBoundary, maxBoundary)) {
                throw new IllegalArgumentException(MessageFormat.format("Interval is not convex: [{0}, {1}]",
                        minBoundary.getAzimuth(), maxBoundary.getAzimuth()));
            }
        }

        /** {@inheritDoc} */
        @Override
        public List<AngularInterval.Convex> toConvex() {
            return Collections.singletonList(this);
        }

        /** {@inheritDoc} */
        @Override
        public Convex transform(final Transform<Point1S> transform) {
            return AngularInterval.transform(this, transform, Convex::of);
        }

        /** Split the instance along a circle diameter.The diameter is defined by the given split point and
         * its reversed antipodal point.
         * @param splitter split point defining one side of the split diameter
         * @return result of the split operation
         */
        public Split<Convex> splitDiameter(final CutAngle splitter) {

            final CutAngle opposite = CutAngles.fromPointAndDirection(
                    splitter.getPoint().antipodal(),
                    !splitter.isPositiveFacing(),
                    splitter.getPrecision());

            if (isFull()) {
                final Convex minus = of(splitter, opposite);
                final Convex plus = of(splitter.reverse(), opposite.reverse());

                return new Split<>(minus, plus);
            }

            final CutAngle minBoundary = getMinBoundary();
            final CutAngle maxBoundary = getMaxBoundary();

            final Point1S posPole = Point1S.of(splitter.getPoint().getAzimuth() + Angle.PI_OVER_TWO);

            final int minLoc = minBoundary.getPrecision().compare(Angle.PI_OVER_TWO,
                    posPole.distance(minBoundary.getPoint()));
            final int maxLoc = maxBoundary.getPrecision().compare(Angle.PI_OVER_TWO,
                    posPole.distance(maxBoundary.getPoint()));

            final boolean positiveFacingSplit = splitter.isPositiveFacing();

            // assume a positive orientation of the splitter for region location
            // purposes and adjust later
            Convex pos = null;
            Convex neg = null;

            if (minLoc > 0) {
                // min is on the pos side

                if (maxLoc >= 0) {
                    // max is directly on the splitter or on the pos side
                    pos = this;
                } else {
                    // min is on the pos side and max is on the neg side
                    final CutAngle posCut = positiveFacingSplit ?
                            opposite.reverse() :
                            opposite;
                    pos = of(minBoundary, posCut);

                    final CutAngle negCut = positiveFacingSplit ?
                            opposite :
                            opposite.reverse();
                    neg = of(negCut, maxBoundary);
                }
            } else if (minLoc < 0) {
                // min is on the neg side

                if (maxLoc <= 0) {
                    // max is directly on the splitter or on the neg side
                    neg = this;
                } else {
                    // min is on the neg side and max is on the pos side
                    final CutAngle posCut = positiveFacingSplit ?
                            splitter.reverse() :
                            splitter;
                    pos = of(maxBoundary, posCut);

                    final CutAngle negCut = positiveFacingSplit ?
                            splitter :
                            splitter.reverse();
                    neg = of(negCut, minBoundary);
                }
            } else {
                // min is directly on the splitter; determine whether it was on the main split
                // point or its antipodal point
                if (splitter.getPoint().distance(minBoundary.getPoint()) < Angle.PI_OVER_TWO) {
                    // on main splitter; interval will be located on pos side of split
                    pos = this;
                } else {
                    // on antipodal point; interval will be located on neg side of split
                    neg = this;
                }
            }

            // adjust for the actual orientation of the splitter
            final Convex minus = positiveFacingSplit ? neg : pos;
            final Convex plus = positiveFacingSplit ? pos : neg;

            return new Split<>(minus, plus);
        }

        /** Return an instance representing the convex angular interval between the given min and max azimuth
         * values. The max value is adjusted to be numerically above the min value, even if the resulting
         * azimuth value is greater than or equal to {@code 2pi}. An instance representing the full space
         * is returned if either point is infinite or min and max are equivalent as evaluated by the
         * given precision context.
         * @param min min azimuth value
         * @param max max azimuth value
         * @param precision precision context used to compare floating point values
         * @return a new instance resulting the angular region between the given min and max azimuths
         * @throws IllegalArgumentException if either azimuth is infinite or NaN, or the given angular
         *      interval is not convex (meaning it has a size of greater than {@code pi})
         */
        public static Convex of(final double min, final double max, final Precision.DoubleEquivalence precision) {
            return of(Point1S.of(min), Point1S.of(max), precision);
        }

        /** Return an instance representing the convex angular interval between the given min and max azimuth
         * points. The max point is adjusted to be numerically above the min point, even if the resulting
         * azimuth value is greater than or equal to {@code 2pi}. An instance representing the full space
         * is returned if either point is infinite or min and max are equivalent as evaluated by the
         * given precision context.
         * @param min min azimuth value
         * @param max max azimuth value
         * @param precision precision context used to compare floating point values
         * @return a new instance resulting the angular region between the given min and max points
         * @throws IllegalArgumentException if either azimuth is infinite or NaN, or the given angular
         *      interval is not convex (meaning it has a size of greater than {@code pi})
         */
        public static Convex of(final Point1S min, final Point1S max, final Precision.DoubleEquivalence precision) {
            return createInterval(min, max, precision, Convex::new, FULL);
        }

        /** Return an instance representing the convex angular interval between the given oriented points.
         * The negative-facing point is used as the minimum boundary and the positive-facing point is
         * adjusted to be above the minimum. The arguments can be given in any order. The full space
         * is returned if the points are equivalent or are oriented in the same direction.
         * @param a first oriented point
         * @param b second oriented point
         * @return an instance representing the angular interval between the given oriented points
         * @throws IllegalArgumentException if either azimuth is infinite or NaN, or the given angular
         *      interval is not convex (meaning it has a size of greater than {@code pi})
         */
        public static Convex of(final CutAngle a, final CutAngle b) {
            return createInterval(a, b, Convex::new, FULL);
        }
    }
}
