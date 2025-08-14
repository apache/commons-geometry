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

import java.util.Comparator;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.core.Precision;

/** This class represents a point on the 2-sphere.
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class Point2S implements Point<Point2S> {

    /** +I (coordinates: ( azimuth = 0, polar = pi/2 )). */
    public static final Point2S PLUS_I = new Point2S(0, 0.5 * Math.PI, Vector3D.Unit.PLUS_X);

    /** +J (coordinates: ( azimuth = pi/2, polar = pi/2 ))). */
    public static final Point2S PLUS_J = new Point2S(0.5 * Math.PI, 0.5 * Math.PI, Vector3D.Unit.PLUS_Y);

    /** +K (coordinates: ( azimuth = any angle, polar = 0 )). */
    public static final Point2S PLUS_K = new Point2S(0, 0, Vector3D.Unit.PLUS_Z);

    /** -I (coordinates: ( azimuth = pi, polar = pi/2 )). */
    public static final Point2S MINUS_I = new Point2S(Math.PI, 0.5 * Math.PI, Vector3D.Unit.MINUS_X);

    /** -J (coordinates: ( azimuth = 3pi/2, polar = pi/2 )). */
    public static final Point2S MINUS_J = new Point2S(1.5 * Math.PI, 0.5 * Math.PI, Vector3D.Unit.MINUS_Y);

    /** -K (coordinates: ( azimuth = any angle, polar = pi )). */
    public static final Point2S MINUS_K = new Point2S(0, Math.PI, Vector3D.Unit.MINUS_Z);

    /** A point with all coordinates set to NaN. */
    public static final Point2S NaN = new Point2S(Double.NaN, Double.NaN, null);

    /** Comparator that sorts points in component-wise ascending order, first sorting
     * by polar value and then by azimuth value. Points are only considered equal if
     * their components match exactly. Null arguments are evaluated as being greater
     * than non-null arguments.
     */
    public static final Comparator<Point2S> POLAR_AZIMUTH_ASCENDING_ORDER = (a, b) -> {
        int cmp = 0;

        if (a != null && b != null) {
            cmp = Double.compare(a.getPolar(), b.getPolar());

            if (cmp == 0) {
                cmp = Double.compare(a.getAzimuth(), b.getAzimuth());
            }
        } else if (a != null) {
            cmp = -1;
        } else if (b != null) {
            cmp = 1;
        }

        return cmp;
    };
    /** Azimuthal angle in the x-y plane. */
    private final double azimuth;

    /** Polar angle. */
    private final double polar;

    /** Corresponding 3D normalized vector. */
    private final Vector3D.Unit vector;

    /** Build a point from its internal components.
     * @param azimuth azimuthal angle in the x-y plane
     * @param polar polar angle
     * @param vector corresponding vector; if {@code null}, the vector is computed
     */
    private Point2S(final double azimuth, final double polar, final Vector3D.Unit vector) {
        this.azimuth = SphericalCoordinates.normalizeAzimuth(azimuth);
        this.polar = SphericalCoordinates.normalizePolar(polar);
        this.vector = (vector != null) ?
                vector :
                computeVector(azimuth, polar);
    }

    /** Get the azimuth angle in the x-y plane in the range {@code [0, 2pi)}.
     * @return azimuth angle in the x-y plane in the range {@code [0, 2pi)}.
     * @see Point2S#of(double, double)
     */
    public double getAzimuth() {
        return azimuth;
    }

    /** Get the polar angle in the range {@code [0, pi)}.
     * @return polar angle in the range {@code [0, pi)}.
     * @see Point2S#of(double, double)
     */
    public double getPolar() {
        return polar;
    }

    /** Get the corresponding normalized vector in 3D Euclidean space.
     * This value will be {@code null} if the spherical coordinates of the point
     * are infinite or NaN.
     * @return normalized vector
     */
    public Vector3D.Unit getVector() {
        return vector;
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNaN() {
        return Double.isNaN(azimuth) || Double.isNaN(polar);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && (Double.isInfinite(azimuth) || Double.isInfinite(polar));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinite() {
        return Double.isFinite(azimuth) && Double.isFinite(polar);
    }

    /** Get the point exactly opposite this point on the sphere. The returned
     * point is {@code pi} distance away from the current instance.
     * @return the point exactly opposite this point on the sphere
     */
    public Point2S antipodal() {
        return from(vector.negate());
    }

    /** {@inheritDoc} */
    @Override
    public double distance(final Point2S point) {
        return distance(this, point);
    }

    /** Spherically interpolate a point along the shortest arc between this point and
     * the given point. The parameter {@code t} controls the interpolation and is expected
     * to be in the range {@code [0, 1]}, with {@code 0} returning a point equivalent to the
     * current instance {@code 1} returning a point equivalent to the given instance. If the
     * points are antipodal, then an arbitrary arc is chosen from the infinite number available.
     * @param other other point to interpolate with
     * @param t interpolation parameter
     * @return spherically interpolated point
     * @see QuaternionRotation#slerp(QuaternionRotation)
     * @see QuaternionRotation#createVectorRotation(Vector3D, Vector3D)
     */
    public Point2S slerp(final Point2S other, final double t) {
        final QuaternionRotation start = QuaternionRotation.identity();
        final QuaternionRotation end = QuaternionRotation.createVectorRotation(getVector(), other.getVector());

        final QuaternionRotation quat = start.slerp(end).apply(t);

        return Point2S.from(quat.apply(getVector()));
    }

    /** Return true if this point should be considered equivalent to the argument using the
     * given precision context. This will be true if the distance between the points is
     * equivalent to zero as evaluated by the precision context.
     * @param point point to compare with
     * @param precision precision context used to perform floating point comparisons
     * @return true if this point should be considered equivalent to the argument using the
     *      given precision context
     */
    public boolean eq(final Point2S point, final Precision.DoubleEquivalence precision) {
        return precision.eqZero(distance(point));
    }

    /** Get a hashCode for the point.
     * .
     * <p>All NaN values have the same hash code.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        if (isNaN()) {
            return 542;
        }
        return 134 * (37 * Double.hashCode(azimuth) +  Double.hashCode(polar));
    }

    /** Test for the equality of two points.
     *
     * <p>If all spherical coordinates of two points are exactly the same, and none are
     * <code>Double.NaN</code>, the two points are considered to be equal. Note
     * that the comparison is made using the azimuth and polar coordinates only; the
     * corresponding 3D vectors are not compared. This is significant at the poles,
     * where an infinite number of points share the same underlying 3D vector but may
     * have different spherical coordinates. For example, the points {@code (0, 0)}
     * and {@code (1, 0)} (both located at a pole but with different azimuths) will
     * <em>not</em> be considered equal by this method, even though they share the
     * exact same underlying 3D vector.</p>
     *
     * <p>
     * <code>NaN</code> coordinates are considered to affect the point globally
     * and be equals to each other - i.e, if either (or all) coordinates of the
     * point are equal to <code>Double.NaN</code>, the point is equal to
     * {@link #NaN}.
     * </p>
     *
     * @param other Object to test for equality to this
     * @return true if two points on the 2-sphere objects are exactly equal, false if
     *         object is {@code null}, not an instance of Point2S, or
     *         not equal to this Point2S instance
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Point2S)) {
            return false;
        }

        final Point2S rhs = (Point2S) other;
        if (rhs.isNaN()) {
            return this.isNaN();
        }

        return Double.compare(azimuth, rhs.azimuth) == 0 &&
                Double.compare(polar, rhs.polar) == 0;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(getAzimuth(), getPolar());
    }

    /** Build a vector from its spherical coordinates.
     * @param azimuth azimuthal angle in the x-y plane
     * @param polar polar angle
     * @return point instance with the given coordinates
     * @see #getAzimuth()
     * @see #getPolar()
     */
    public static Point2S of(final double azimuth, final double polar) {
        return new Point2S(azimuth, polar, null);
    }

    /** Build a point from its underlying 3D vector.
     * @param vector 3D vector
     * @return point instance with the coordinates determined by the given 3D vector
     * @exception IllegalStateException if vector norm is zero
     */
    public static Point2S from(final Vector3D vector) {
        final SphericalCoordinates coords = SphericalCoordinates.fromCartesian(vector);

        return new Point2S(coords.getAzimuth(), coords.getPolar(), vector.normalize());
    }

    /** Parses the given string and returns a new point instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return point instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Point2S parse(final String str) {
        return SimpleTupleFormat.getDefault().parse(str, Point2S::of);
    }

    /** Compute the distance (angular separation) between two points.
     * @param p1 first vector
     * @param p2 second vector
     * @return the angular separation between p1 and p2
     */
    public static double distance(final Point2S p1, final Point2S p2) {
        return p1.vector.angle(p2.vector);
    }

    /** Compute the 3D Euclidean vector associated with the given spherical coordinates.
     * {@code null} is returned if the coordinates are infinite or NaN.
     * @param azimuth azimuth value
     * @param polar polar value
     * @return the 3D Euclidean vector associated with the given spherical coordinates
     *      or {@code null} if either of the arguments are infinite or NaN.
     */
    private static Vector3D.Unit computeVector(final double azimuth, final double polar) {
        if (Double.isFinite(azimuth) && Double.isFinite(polar)) {
            return SphericalCoordinates.toCartesian(1, azimuth, polar).normalize();
        }
        return null;
    }
}
