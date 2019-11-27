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

import java.util.Comparator;
import java.util.Objects;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.PolarCoordinates;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.PlaneAngle;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** This class represents a point on the 1-sphere, or in other words, an
 * azimuth angle on a circle. The value of the azimuth angle is not normalized
 * by default, meaning that instances can be constructed representing negative
 * values or values greater than {@code 2pi}. However, instances separated by a
 * multiple of {@code 2pi} are considered equivalent for most methods, with the
 * exceptions being {@link #equals(Object)} and {@link #hashCode()}, where the
 * azimuth values must match exactly in order for instances to be considered
 * equal.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class Point1S implements Point<Point1S> {

    /** A point with coordinates set to {@code 0*pi}. */
    public static final Point1S ZERO = Point1S.of(0.0);

    /** A point with coordinates set to {@code pi}. */
    public static final Point1S PI = Point1S.of(Geometry.PI);

    // CHECKSTYLE: stop ConstantName
    /** A point with all coordinates set to NaN. */
    public static final Point1S NaN = Point1S.of(Double.NaN);
    // CHECKSTYLE: resume ConstantName

    /** Comparator that sorts points by normalized azimuth in ascending order.
     * Points are only considered equal if their normalized azimuths match exactly.
     * Null arguments are evaluated as being greater than non-null arguments.
     * @see #getNormalizedAzimuth()
     */
    public static final Comparator<Point1S> NORMALIZED_AZIMUTH_ASCENDING_ORDER = (a, b) -> {
        int cmp = 0;

        if (a != null && b != null) {
            cmp = Double.compare(a.getNormalizedAzimuth(), b.getNormalizedAzimuth());
        } else if (a != null) {
            cmp = -1;
        } else if (b != null) {
            cmp = 1;
        }

        return cmp;
    };

    /** Azimuthal angle in radians. */
    private final double azimuth;

    /** Normalized azimuth value in the range {@code [0, 2pi)}. */
    private final double normalizedAzimuth;

    /** Build a point from its internal components.
     * @param azimuth azimuth angle
     * @param normalizedAzimuth azimuth angle normalized to the range {@code [0, 2pi)}
     */
    private Point1S(final double azimuth, final double normalizedAzimuth) {
        this.azimuth  = azimuth;
        this.normalizedAzimuth = normalizedAzimuth;
    }

    /** Get the azimuth angle in radians. This value is not normalized and
     * can be any floating point number.
     * @return azimuth angle
     * @see Point1S#of(double)
     */
    public double getAzimuth() {
        return azimuth;
    }

    /** Get the azimuth angle normalized to the range {@code [0, 2pi)}.
     * @return the azimuth angle normalized to the range {@code [0, 2pi)}.
     */
    public double getNormalizedAzimuth() {
        return normalizedAzimuth;
    }

    /** Get the normalized vector corresponding to this azimuth angle in 2D Euclidean space.
     * @return normalized vector
     */
    public Vector2D getVector() {
        if (isFinite()) {
            return PolarCoordinates.toCartesian(1, azimuth);
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNaN() {
        return Double.isNaN(azimuth);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && Double.isInfinite(azimuth);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinite() {
        return Double.isFinite(azimuth);
    }

    /** {@inheritDoc}
     *
     * <p>The returned value is the shortest angular distance between
     * the two points, in the range {@code [0, pi]}.</p>
     */
    @Override
    public double distance(final Point1S point) {
        return distance(this, point);
    }

    /** Return the signed distance (angular separation) between this instance and the
     * given point in the range {@code [-pi, pi)}. If {@code p1} is the current instance,
     * {@code p2} the given point, and {@code d} the signed distance, then
     * {@code p1.getAzimuth() + d} is an angle equivalent to {@code p2.getAzimuth()}.
     * @param point point to compute the signed distance to
     * @return the signed distance between this instance and the given point in the range
     *      {@code [-pi, pi)}
     */
    public double signedDistance(final Point1S point) {
        return signedDistance(this, point);
    }

    /** Return an equivalent point with an azimuth value at or above the given base.
     * The returned point has an azimuth value in the range {@code [base, base + 2pi)}.
     * @param base point to place this instance's azimuth value above
     * @return a point equivalent to the current instance but with an azimuth
     *      value in the range {@code [base, base + 2pi)}
     * @throws GeometryValueException if the azimuth value is NaN or infinite and
     *      cannot be normalized
     */
    public Point1S above(final Point1S base) {
        return normalize(base.getAzimuth() + Geometry.PI);
    }

    /** Return an equivalent point with an azimuth value strictly below the given base.
     * The returned point has an azimuth value in the range {@code [base - 2pi, base)}.
     * @param base point to place this instance's azimuth value below
     * @return a point equivalent to the current instance but with an azimuth
     *      value in the range {@code [base - 2pi, base)}
     * @throws GeometryValueException if the azimuth value is NaN or infinite and
     *      cannot be normalized
     */
    public Point1S below(final Point1S base) {
        return normalize(base.getAzimuth() - Geometry.PI);
    }

    /** Normalize this point around the given center point. The azimuth value of
     * the returned point is in the range {@code [center - pi, center + pi)}.
     * @param center point to center this instance around
     * @return a point equivalent to this instance but with an azimuth value
     *      in the range {@code [center - pi, center + pi)}.
     * @throws GeometryValueException if the azimuth value is NaN or infinite and
     *      cannot be normalized
     */
    public Point1S normalize(final Point1S center) {
        return normalize(center.getAzimuth());
    }

    /** Return an equivalent point with an azimuth value normalized around the given center
     * angle. The azimuth value of the returned point is in the range
     * {@code [center - pi, center + pi)}.
     * @param center angle to center this instance around
     * @return a point equivalent to this instance but with an azimuth value
     *      in the range {@code [center - pi, center + pi)}.
     * @throws GeometryValueException if the azimuth value is NaN or infinite and
     *      cannot be normalized
     */
    public Point1S normalize(final double center) {
        if (isFinite()) {
            final double az = PlaneAngleRadians.normalize(azimuth, center);
            return new Point1S(az, normalizedAzimuth);
        }
        throw new GeometryValueException("Cannot normalize azimuth value: " + azimuth);
    }

    /** Get the point exactly opposite this point on the circle, {@code pi} distance away.
     * The azimuth of the antipodal point is in the range {@code [0, 2pi)}.
     * @return the point exactly opposite this point on the circle
     */
    public Point1S antipodal() {
        double az = normalizedAzimuth + Geometry.PI;
        if (az >= Geometry.TWO_PI) {
            az -= Geometry.TWO_PI;
        }

        return Point1S.of(az);
    }

    /** Return true if this instance is equivalent to the argument. The points are
     * considered equivalent if the shortest angular distance between them is equal to
     * zero as evaluated by the given precision context. This means that points that differ
     * in azimuth by multiples of {@code 2pi} are considered equivalent.
     * @param other point to compare with
     * @param precision precision context used for floating point comparisons
     * @return true if this instance is equivalent to the argument
     */
    public boolean eq(final Point1S other, final DoublePrecisionContext precision) {
        final double dist = signedDistance(other);
        return precision.eqZero(dist);
    }

    /**
     * Get a hashCode for the point. Points normally must have exactly the
     * same azimuth angles in order to have the same hash code. Points
     * will angles that differ by multiples of {@code 2pi} will not
     * necessarily have the same hash code.
     *
     * <p>All NaN values have the same hash code.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        if (isNaN()) {
            return 542;
        }
        return 1759 * Objects.hash(azimuth, normalizedAzimuth);
    }

    /** Test for the exact equality of two points on the 1-sphere.
     *
     * <p>If all coordinates of the given points are exactly the same, and none are
     * <code>Double.NaN</code>, the points are considered to be equal. Points with
     * azimuth values separated by multiples of {@code 2pi} are <em>not</em> considered
     * equal.</p>
     *
     * <p><code>NaN</code> coordinates are considered to affect globally the vector
     * and be equals to each other - i.e, if either (or all) coordinates of the
     * point are equal to <code>Double.NaN</code>, the point is equal to
     * {@link #NaN}.</p>
     *
     * @param other Object to test for equality to this
     * @return true if two points on the 1-sphere objects are exactly equal, false if
     *         object is null, not an instance of Point1S, or
     *         not equal to this Point1S instance
     *
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Point1S) {
            final Point1S rhs = (Point1S) other;

            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return Double.compare(azimuth, rhs.azimuth) == 0 &&
                    Double.compare(normalizedAzimuth, rhs.normalizedAzimuth) == 0;
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(getAzimuth());
    }

    /** Create a new point instance from the given azimuth angle.
     * @param azimuth azimuth angle in radians
     * @return point instance with the given azimuth angle
     * @see #getAzimuth()
     */
    public static Point1S of(final double azimuth) {
        final double normalizedAzimuth = PolarCoordinates.normalizeAzimuth(azimuth);

        return new Point1S(azimuth, normalizedAzimuth);
    }

    /** Create a new point instance from the given azimuth angle.
     * @param azimuth azimuth azimuth angle in radians
     * @return point instance with the given azimuth angle
     * @see #getAzimuth()
     */
    public static Point1S of(final PlaneAngle azimuth) {
        return of(azimuth.toRadians());
    }

    /** Create a new point instance from the given Euclidean 2D vector. The returned point
     * will have an azimuth value equal to the angle between the positive x-axis and the
     * given vector, measured in a counter-clockwise direction.
     * @param vector 3D vector to create the point from
     * @return a new point instance with an azimuth value equal to the angle between the given
     *      vector and the positive x-axis, measured in a counter-clockwise direction
     */
    public static Point1S from(final Vector2D vector) {
        final PolarCoordinates polar = PolarCoordinates.fromCartesian(vector);
        final double az = polar.getAzimuth();

        return new Point1S(az, az);
    }

    /** Create a new point instance containing an azimuth value equal to that of the
     * given set of polar coordinates.
     * @param polar polar coordinates to convert to a point
     * @return a new point instance containing an azimuth value equal to that of
     *      the given set of polar coordinates.
     */
    public static Point1S from(final PolarCoordinates polar) {
        final double az = polar.getAzimuth();

        return new Point1S(az, az);
    }

    /** Parse the given string and returns a new point instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return point instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Point1S parse(final String str) {
        return SimpleTupleFormat.getDefault().parse(str, az -> Point1S.of(az));
    }

    /** Compute the signed shortest distance (angular separation) between two points. The return
     * value is in the range {@code [-pi, pi)} and is such that {@code p1.getAzimuth() + d}
     * (where {@code d} is the signed distance) is an angle equivalent to {@code p2.getAzimuth()}.
     * @param p1 first point
     * @param p2 second point
     * @return the signed angular separation between p1 and p2, in the range {@code [-pi, pi)}.
     */
    public static double signedDistance(final Point1S p1, final Point1S p2) {
        double dist = p2.normalizedAzimuth - p1.normalizedAzimuth;
        if (dist < -Geometry.PI) {
            dist += Geometry.TWO_PI;
        }
        if (dist >= Geometry.PI) {
            dist -= Geometry.TWO_PI;
        }
        return dist;
    }

    /** Compute the shortest distance (angular separation) between two points. The returned
     * value is in the range {@code [0, pi]}. This method is equal to the absolute value of
     * the {@link #signedDistance(Point1S, Point1S) signed distance}.
     * @param p1 first point
     * @param p2 second point
     * @return the angular separation between p1 and p2, in the range {@code [0, pi]}.
     * @see #signedDistance(Point1S, Point1S)
     */
    public static double distance(final Point1S p1, final Point1S p2) {
        return Math.abs(signedDistance(p1, p2));
    }
}
