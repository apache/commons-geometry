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
import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.twod.PolarCoordinates;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** This class represents a point on the 1-sphere.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class Point1S implements Point<Point1S>, Serializable {

    /** A point with coordinates set to {@code 0*pi}. */
    public static final Point1S ZERO_PI = new Point1S(Geometry.ZERO_PI);

    /** A point with coordinates set to {@code pi/2}. */
    public static final Point1S HALF_PI = new Point1S(Geometry.HALF_PI);

    /** A point with coordinates set to {@code pi}. */
    public static final Point1S PI = new Point1S(Geometry.PI);

    /** A point with coordinates set to {@code 3*pi/2}. */
    public static final Point1S THREE_HALVES_PI = new Point1S(Geometry.MINUS_HALF_PI);

    // CHECKSTYLE: stop ConstantName
    /** A point with all coordinates set to NaN. */
    public static final Point1S NaN = new Point1S(Double.NaN);
    // CHECKSTYLE: resume ConstantName

    /** Serializable UID. */
    private static final long serialVersionUID = 20180710L;

    /** Azimuthal angle in radians. */
    private final double azimuth;

    /** Corresponding 2D normalized vector. */
    private final Vector2D vector;

    /** Build a point from its internal components.
     * @param azimuth azimuthal angle
     */
    private Point1S(final double azimuth) {
        this.azimuth  = PolarCoordinates.normalizeAzimuth(azimuth);
        this.vector = Double.isFinite(azimuth) ? PolarCoordinates.toCartesian(1.0, azimuth) : Vector2D.NaN;
    }

    /** Get the azimuthal angle in radians.
     * @return azimuthal angle
     * @see Point1S#of(double)
     */
    public double getAzimuth() {
        return azimuth;
    }

    /** Get the corresponding normalized vector in the 2D Euclidean space.
     * @return normalized vector
     */
    public Vector2D getVector() {
        return vector;
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
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Point1S) {
            final Point1S rhs = (Point1S) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return azimuth == rhs.azimuth;
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
        return 1759 * Double.hashCode(azimuth);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(getAzimuth());
    }

    /** Creates a new point instance from the given azimuthal coordinate value.
     * @param azimuth azimuthal angle in radians
     * @return point instance with the given azimuth coordinate value
     * @see #getAzimuth()
     */
    public static Point1S of(final double azimuth) {
        return new Point1S(azimuth);
    }

    /** Parses the given string and returns a new point instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return point instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Point1S parse(final String str) {
        return SimpleTupleFormat.getDefault().parse(str, Point1S::new);
    }

    /** Compute the shortest distance (angular separation) between two points. The returned
     * value is in the range {@code [0, pi]}
     * @param p1 first point
     * @param p2 second point
     * @return the angular separation between p1 and p2, in the range {@code [0, pi]}.
     */
    public static double distance(final Point1S p1, final Point1S p2) {
        return p1.vector.angle(p2.vector);
    }
}
