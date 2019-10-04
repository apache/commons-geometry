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

import java.io.Serializable;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing an angular interval. The interval is defined by two azimuth angles: a min and
 * a max. The interval starts at the min azimuth angle and contains all points in the direction of
 * increasing azimuth angles up to max.
 *
 * <p>This class is guaranteed to be immutable.</p>
 */
public class AngularInterval implements HyperplaneBoundedRegion<Point1S>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190817L;

    /** Interval instance representing the full space. */
    private static final AngularInterval FULL = new AngularInterval(null, null, null);

    /** The minimum boundary of the interval. */
    private final CutAngle minBoundary;

    /** The maximum boundary of the interval. */
    private final CutAngle maxBoundary;

    /** Point halfway between the min and max boundaries. */
    private final Point1S midpoint;

    /** Construct a new instance representing the angular region between the given
     * min and max azimuth boundaries. The arguments must be either all finite or all
     * null (to indicate the full space). If the boundaries are finite, then the min
     * boundary azimuth value must be numerically less than the max boundary. Callers are
     * responsible for enforcing these constraints. No validation is performed.
     * @param minBoundary minimum boundary for the interval
     * @param maxBoundary maximum boundary for the interval
     * @param midpoint the midpoint between the boundaries
     */
    private AngularInterval(final CutAngle minBoundary, final CutAngle maxBoundary,
            final Point1S midpoint) {

        this.minBoundary = minBoundary;
        this.maxBoundary = maxBoundary;
        this.midpoint = midpoint;
    }

    /** Get the minimum azimuth angle for the interval, or {@code 0}
     * if the interval is full.
     * @return the minimum azimuth angle for the interval or {@code 0}
     *      if the interval represents the full space.
     */
    public double getMin() {
        return (minBoundary != null) ?
                minBoundary.getAzimuth() :
                Geometry.ZERO_PI;
    }

    /** Get the minimum boundary for the interval, or null if the
     * interval represents the full space.
     * @return the minimum point for the interval or null if
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
                Geometry.TWO_PI;
    }

    /** Get the maximum point for the interval. This will be null if the
     * interval represents the full space.
     * @return the maximum point for the interval or null if
     *      the interval represents the full space
     */
    public CutAngle getMaxBoundary() {
        return maxBoundary;
    }

    /** Get the midpoint of the interval or null if the interval represents
     *  the full space.
     * @return the midpoint of the interval or null if the interval represents
     *      the full space
     * @see #getBarycenter()
     */
    public Point1S getMidpoint() {
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
     * <p>This method is an alias for {@link #getMidpoint()}.</p>
     * @see #getMidpoint()
     */
    @Override
    public Point1S getBarycenter() {
        return getMidpoint();
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final Point1S pt) {
        if (!isFull()) {
            final HyperplaneLocation minLoc = minBoundary.classify(pt);
            final HyperplaneLocation maxLoc = maxBoundary.classify(pt);

            final boolean wraps = wrapsZero();

            if ((!wraps && (minLoc == HyperplaneLocation.PLUS || maxLoc == HyperplaneLocation.PLUS)) ||
                    (wraps && minLoc == HyperplaneLocation.PLUS && maxLoc == HyperplaneLocation.PLUS)) {
                return RegionLocation.OUTSIDE;
            }
            else if (minLoc == HyperplaneLocation.ON || maxLoc == HyperplaneLocation.ON) {
                return RegionLocation.BOUNDARY;
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
     * values are normalized to the range {@code {0, 2pi)}.
     * @return true if the interval wraps around the zero/{@code 2pi} point
     */
    public boolean wrapsZero() {
        if (!isFull()) {
            final double minNormAz = minBoundary.getPoint().getNormalizedAzimuth();
            final double maxNormAz = maxBoundary.getPoint().getNormalizedAzimuth();

            return maxNormAz < minNormAz;
        }
        return false;
    }

    /** Return a new instance transformed by the argument. If the transformed size
     * of the interval is greater than or equal to 2pi, then an interval representing
     * the full space is returned.
     * @param transform transform to apply
     * @return a new instance transformed by the argument
     */
    public AngularInterval transform(final Transform<Point1S> transform) {
        if (!isFull()) {
            final CutAngle tMin = minBoundary.transform(transform);
            final CutAngle tMax = maxBoundary.transform(transform);

            return of(tMin, tMax);
        }

        return this;
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
    public static AngularInterval full() {
        return FULL;
    }

    /** Return an instance representing the angular interval between the given min and max azimuth
     * values. The max value is adjusted to be numerically above the min value, even if the resulting
     * azimuth value is greater than or equal to {@code 2pi}. An instance representing the full space
     * is returned if either point is infinite or min and max are equivalent as evaluated by the
     * given precision context.
     * @param min min azimuth value
     * @param max max azimuth value
     * @param precision precision precision context used to compare floating point values
     * @return a new instance resulting the angular region between the given min and max azimuths
     * @throws IllegalArgumentException if either azimuth is infinite or NaN
     */
    public static AngularInterval of(final double min, final double max, final DoublePrecisionContext precision) {
        return of(Point1S.of(min), Point1S.of(max), precision);
    }

    /** Return an instance representing the angular interval between the given min and max azimuth
     * points. The max point is adjusted to be numerically above the min point, even if the resulting
     * azimuth value is greater than or equal to {@code 2pi}. An instance representing the full space
     * is returned if either point is infinite or min and max are equivalent as evaluated by the
     * given precision context.
     * @param min min azimuth value
     * @param max max azimuth value
     * @param precision precision precision context used to compare floating point values
     * @return a new instance resulting the angular region between the given min and max points
     * @throws IllegalArgumentException if either azimuth is infinite or NaN
     */
    public static AngularInterval of(final Point1S min, final Point1S max, final DoublePrecisionContext precision) {
        validateIntervalValues(min, max);

        // return the full space if either point is infinite or the points are equivalent
        if (min.eq(max, precision)) {
            return full();
        }

        final Point1S adjustedMax = max.above(min);
        final double midAz = 0.5 * (adjustedMax.getAzimuth() + min.getAzimuth());

        return new AngularInterval(
                    CutAngle.createNegativeFacing(min, precision),
                    CutAngle.createPositiveFacing(adjustedMax, precision),
                    Point1S.of(midAz)
                );
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
        final Point1S aPoint = a.getPoint();
        final Point1S bPoint = b.getPoint();

        validateIntervalValues(aPoint, bPoint);

        if (a.isPositiveFacing() == b.isPositiveFacing() ||
                aPoint.eq(bPoint, a.getPrecision()) ||
                bPoint.eq(aPoint, b.getPrecision())) {
            // points are equivalent or facing in the same direction
            return full();
        }

        final CutAngle min = a.isPositiveFacing() ? b : a;
        final CutAngle max = a.isPositiveFacing() ? a : b;
        final CutAngle adjustedMax = CutAngle.createPositiveFacing(
                max.getPoint().above(min.getPoint()),
                max.getPrecision());

        final Point1S mid = Point1S.of(0.5 * (adjustedMax.getAzimuth() + min.getAzimuth()));

        return new AngularInterval(min, adjustedMax, mid);
    }

    /** Validate that the given points can be used to specify an angular interval.
     * @param a first point
     * @param b second point
     * @throws IllegalArgumentException if either point is infinite NaN
     */
    private static void validateIntervalValues(final Point1S a, final Point1S b) {
        if (!a.isFinite() || !b.isFinite()) {
            throw new IllegalArgumentException("Invalid angular interval: [" + a.getAzimuth() +
                    ", " + b.getAzimuth() + "]");
        }
    }
}
