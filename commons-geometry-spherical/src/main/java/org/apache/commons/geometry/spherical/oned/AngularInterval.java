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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.ConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing an angular interval. The interval is defined by two azimuth angles: a min and
 * a max. The interval starts at the min azimuth angle and contains all points in the direction of
 * increasing azimuth angles up to max.
 *
 * <p>This class is guaranteed to be immutable.</p>
 */
public class AngularInterval implements ConvexHyperplaneBoundedRegion<Point1S>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190817L;

    /** Interval instance representing the full space. */
    private static final AngularInterval FULL = new AngularInterval(null, null, null);

    /** The minimum boundary of the interval. */
    private final OrientedPoint1S minBoundary;

    /** The maximum boundary of the interval. */
    private final OrientedPoint1S maxBoundary;

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
    private AngularInterval(final OrientedPoint1S minBoundary, final OrientedPoint1S maxBoundary,
            final Point1S midpoint) {

        this.minBoundary = minBoundary;
        this.maxBoundary = maxBoundary;
        this.midpoint = midpoint;
    }

    /** Get the minimum azimuth angle for the interval. This value will
     * be {@link Double#NEGATIVE_INFINITY} if the interval represents the
     * full space.
     * @return the minimum azimuth angle for the interval or {@link Double#NEGATIVE_INFINITY}
     *      if the interval represents the full space.
     */
    public double getMin() {
        return minBoundary != null ?
                minBoundary.getAzimuth() :
                Double.NEGATIVE_INFINITY;
    }

    /** Get the minimum boundary for the interval. This will be null if the
     * interval represents the full space.
     * @return the minimum point for the interval or null if
     *      the interval represents the full space
     */
    public OrientedPoint1S getMinBoundary() {
        return minBoundary;
    }

    /** Get the maximum azimuth angle for the interval. This value will
     * be {@link Double#POSITIVE_INFINITY} if the interval represents the
     * full space.
     * @return the maximum azimuth angle for the interval or {@link Double#POSITIVE_INFINITY}
     *      if the interval represents the full space.
     */
    public double getMax() {
        return maxBoundary != null ?
                maxBoundary.getAzimuth() :
                Double.POSITIVE_INFINITY;
    }

    /** Get the maximum point for the interval. This will be null if the
     * interval represents the full space.
     * @return the maximum point for the interval or null if
     *      the interval represents the full space
     */
    public OrientedPoint1S getMaxBoundary() {
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
    public List<AngularInterval> toConvex() {
        return Arrays.asList(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
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
            // Classify using the closest boundary. We need to do this since the
            // boundary hyperplanes split the space into plus and minus sections
            // around the hyperplane location but also implicitly at the point
            // pi distance away. Since we know that the boundaries are not at the
            // same location (since otherwise, the interval would be full), we
            // are guaranteed that the midpoint of the interval is less than pi
            // from each boundary. Therefore, we can classify the pt by classifying
            // it against the single boundary that it is closest to.
            OrientedPoint1S testBoundary = midpoint.signedDistance(pt) < 0 ?
                    minBoundary :
                    maxBoundary;

            final HyperplaneLocation loc = testBoundary.classify(pt);
            if (HyperplaneLocation.ON == loc) {
                return RegionLocation.BOUNDARY;
            }
            else if (HyperplaneLocation.PLUS == loc) {
                return RegionLocation.OUTSIDE;
            }

        }
        return RegionLocation.INSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public Point1S project(final Point1S pt) {
        if (!isFull()) {
            final double minOffset = minBoundary.offset(pt);
            final double maxOffset = maxBoundary.offset(pt);

            return (minOffset <= maxOffset) ?
                    minBoundary.getPoint() :
                    maxBoundary.getPoint();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public AngularInterval transform(final Transform<Point1S> transform) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Split<AngularInterval> split(final Hyperplane<Point1S> splitter) {
        // TODO Auto-generated method stub
        return null;
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
     * is returned if min and max are equivalent, as evaluated by the given precision context.
     * @param min min azimuth value
     * @param max max azimuth value
     * @param precision precision precision context used to compare floating point values
     * @return a new instance resulting the angular region between the given min and max azimuths
     * @throws IllegalArgumentException if either azimuth is NaN or infinite
     */
    public static AngularInterval of(final double min, final double max, final DoublePrecisionContext precision) {
        return of(Point1S.of(min), Point1S.of(max), precision);
    }

    /** Return an instance representing the angular interval between the given min and max azimuth
     * points. The max point is adjusted to be numerically above the min point, even if the resulting
     * azimuth value is greater than or equal to {@code 2pi}. An instance representing the full space
     * is returned if min and max are equivalent, as evaluated by the given precision context.
     * @param min min azimuth value
     * @param max max azimuth value
     * @param precision precision precision context used to compare floating point values
     * @return a new instance resulting the angular region between the given min and max points
     * @throws IllegalArgumentException if either azimuth is NaN or infinite
     */
    public static AngularInterval of(final Point1S min, final Point1S max, final DoublePrecisionContext precision) {

        // validate input values
        if (!min.isFinite() || !max.isFinite()) {

            throw new IllegalArgumentException("Invalid interval values: [" + min.getAzimuth() +
                    ", " + max.getAzimuth() + "]");
        }

        // return the full space if the points are equivalent
        if (min.eq(max, precision)) {
            return full();
        }

        final Point1S adjustedMax = max.above(min);
        final double midAz = 0.5 * (adjustedMax.getAzimuth() + min.getAzimuth());

        return new AngularInterval(
                    OrientedPoint1S.createNegativeFacing(min, precision),
                    OrientedPoint1S.createPositiveFacing(adjustedMax, precision),
                    Point1S.of(midAz)
                );
    }
}
