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

import java.io.Serializable;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** This class represents a point on the 2-sphere.
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class Point2S implements Point<Point2S>, Serializable {

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

    // CHECKSTYLE: stop ConstantName
    /** A point with all coordinates set to NaN. */
    public static final Point2S NaN = new Point2S(Double.NaN, Double.NaN, Vector3D.NaN);
    // CHECKSTYLE: resume ConstantName

    /** Serializable UID. */
    private static final long serialVersionUID = 20180710L;

    /** Azimuthal angle in the x-y plane. */
    private final double azimuth;

    /** Polar angle. */
    private final double polar;

    /** Corresponding 3D normalized vector. */
    private final Vector3D vector;

    /** Build a point from its internal components.
     * @param azimuth azimuthal angle in the x-y plane
     * @param polar polar angle
     * @param vector corresponding vector; if null, the vector is computed
     */
    private Point2S(final double azimuth, final double polar, final Vector3D vector) {
        this.azimuth = SphericalCoordinates.normalizeAzimuth(azimuth);
        this.polar = SphericalCoordinates.normalizePolar(polar);
        this.vector = (vector != null) ? vector : SphericalCoordinates.toCartesian(1.0, azimuth, polar);
    }

    /** Get the azimuthal angle in the x-y plane in radians.
     * @return azimuthal angle in the x-y plane
     * @see Point2S#of(double, double)
     */
    public double getAzimuth() {
        return azimuth;
    }

    /** Get the polar angle in radians.
     * @return polar angle
     * @see Point2S#of(double, double)
     */
    public double getPolar() {
        return polar;
    }

    /** Get the corresponding normalized vector in the 3D Euclidean space.
     * @return normalized vector
     */
    public Vector3D getVector() {
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

    /** Get the opposite of the instance.
     * @return a new vector which is opposite to the instance
     */
    public Point2S negate() {
        return new Point2S(-azimuth, Math.PI - polar, vector.negate());
    }

    /** {@inheritDoc} */
    @Override
    public double distance(final Point2S point) {
        return distance(this, point);
    }

    /** Compute the distance (angular separation) between two points.
     * @param p1 first vector
     * @param p2 second vector
     * @return the angular separation between p1 and p2
     */
    public static double distance(Point2S p1, Point2S p2) {
        return p1.vector.angle(p2.vector);
    }

    /**
     * Test for the equality of two points on the 2-sphere.
     * <p>
     * If all coordinates of two points are exactly the same, and none are
     * <code>Double.NaN</code>, the two points are considered to be equal.
     * </p>
     * <p>
     * <code>NaN</code> coordinates are considered to affect globally the vector
     * and be equals to each other - i.e, if either (or all) coordinates of the
     * 2D vector are equal to <code>Double.NaN</code>, the 2D vector is equal to
     * {@link #NaN}.
     * </p>
     *
     * @param other Object to test for equality to this
     * @return true if two points on the 2-sphere objects are equal, false if
     *         object is null, not an instance of S2Point, or
     *         not equal to this S2Point instance
     *
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Point2S) {
            final Point2S rhs = (Point2S) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return (azimuth == rhs.azimuth) && (polar == rhs.polar);
        }
        return false;
    }

    /**
     * Get a hashCode for the 2D vector.
     * <p>
     * All NaN values have the same hash code.</p>
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(getAzimuth(), getPolar());
    }

    /** Build a vector from its spherical coordinates
     * @param azimuth azimuthal angle in the x-y plane
     * @param polar polar angle
     * @return point instance with the given coordinates
     * @see #getAzimuth()
     * @see #getPolar()
     */
    public static Point2S of(final double azimuth, final double polar) {
        return new Point2S(azimuth, polar, null);
    }

    /** Build a point from its underlying 3D vector
     * @param vector 3D vector
     * @return point instance with the coordinates determined by the given 3D vector
     * @exception IllegalStateException if vector norm is zero
     */
    public static Point2S fromVector(final Vector3D vector) {
        final SphericalCoordinates coords = SphericalCoordinates.fromCartesian(vector);

        return new Point2S(coords.getAzimuth(), coords.getPolar(), vector.normalize());
    }

    /** Parses the given string and returns a new point instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return point instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Point2S parse(String str) {
        return SimpleTupleFormat.getDefault().parse(str, Point2S::of);
    }
}
