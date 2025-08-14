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
package org.apache.commons.geometry.euclidean.twod;

import org.apache.commons.geometry.core.Spatial;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.numbers.angle.Angle;

/** Class representing <a href="https://en.wikipedia.org/wiki/Polar_coordinate_system">polar coordinates</a>
 * in 2 dimensional Euclidean space.
 *
 * <p>Polar coordinates are defined by a distance from a reference point
 * and an angle from a reference direction. The distance value is called
 * the radial coordinate, or <em>radius</em>, and the angle is called the angular coordinate,
 * or <em>azimuth</em>. This class follows the standard
 * mathematical convention of using the positive x-axis as the reference
 * direction and measuring positive angles counter-clockwise, toward the
 * positive y-axis. The origin is used as the reference point. Polar coordinate
 * are related to Cartesian coordinates as follows:
 * <pre>
 * x = r * cos(&theta;)
 * y = r * sin(&theta;)
 *
 * r = &radic;(x^2 + y^2)
 * &theta; = atan2(y, x)
 * </pre>
 * where <em>r</em> is the radius and <em>&theta;</em> is the azimuth of the polar coordinates.
 *
 * <p>In order to ensure the uniqueness of coordinate sets, coordinate values
 * are normalized so that {@code radius} is in the range {@code [0, +Infinity)}
 * and {@code azimuth} is in the range {@code [0, 2pi)}.</p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Polar_coordinate_system">Polar Coordinate System</a>
 */
public final class PolarCoordinates implements Spatial {
    /** Radius value. */
    private final double radius;

    /** Azimuth angle in radians. */
    private final double azimuth;

    /** Simple constructor. Input values are normalized.
     * @param radius Radius value.
     * @param azimuth Azimuth angle in radians.
     */
    private PolarCoordinates(final double radius, final double azimuth) {
        double rad = radius;
        double az = azimuth;

        if (rad < 0) {
            // negative radius; flip the angles
            rad = Math.abs(radius);
            az += Math.PI;
        }

        this.radius = rad;
        this.azimuth = normalizeAzimuth(az);
    }

    /** Return the radius value. The value will be greater than or equal to 0.
     * @return radius value
     */
    public double getRadius() {
        return radius;
    }

    /** Return the azimuth angle in radians. The value will be
     * in the range {@code [0, 2pi)}.
     * @return azimuth value in radians.
     */
    public double getAzimuth() {
        return azimuth;
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNaN() {
        return Double.isNaN(radius) || Double.isNaN(azimuth);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && (Double.isInfinite(radius) || Double.isInfinite(azimuth));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinite() {
        return Double.isFinite(radius) && Double.isFinite(azimuth);
    }

    /** Convert this set of polar coordinates to Cartesian coordinates.
     * @return A 2-dimensional vector with an equivalent set of
     *      coordinates in Cartesian form
     */
    public Vector2D toCartesian() {
        return toCartesian(radius, azimuth);
    }

    /** Get a hashCode for this set of polar coordinates.
     * <p>All NaN values have the same hash code.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        if (isNaN()) {
            return 191;
        }
        return 449 * (76 * Double.hashCode(radius) + Double.hashCode(azimuth));
    }

    /** Test for the equality of two sets of polar coordinates.
     * <p>
     * If all values of two sets of coordinates are exactly the same, and none are
     * <code>Double.NaN</code>, the two sets are considered to be equal.
     * </p>
     * <p>
     * <code>NaN</code> values are considered to globally affect the coordinates
     * and be equal to each other - i.e, if either (or all) values of the
     * coordinate set are equal to <code>Double.NaN</code>, the set as a whole is
     * considered to equal <code>NaN</code>.
     * </p>
     *
     * @param other Object to test for equality to this
     * @return true if two PolarCoordinates objects are equal, false if
     *         object is {@code null}, not an instance of PolarCoordinates, or
     *         not equal to this PolarCoordinates instance
     *
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof PolarCoordinates) {
            final PolarCoordinates rhs = (PolarCoordinates) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return Double.compare(radius, rhs.radius) == 0 &&
                    Double.compare(azimuth, rhs.azimuth) == 0;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(radius, azimuth);
    }

    /** Return a new instance with the given polar coordinate values.
     * The values are normalized so that {@code radius} lies in the range {@code [0, +Infinity)}
     * and {@code azimuth} in the range {@code [0, 2pi)}.
     * @param radius Radius value.
     * @param azimuth Azimuth angle in radians.
     * @return new {@link PolarCoordinates} instance
     */
    public static PolarCoordinates of(final double radius, final double azimuth) {
        return new PolarCoordinates(radius, azimuth);
    }

    /** Convert the given Cartesian coordinates to polar form.
     * @param x X coordinate value
     * @param y Y coordinate value
     * @return polar coordinates equivalent to the given Cartesian coordinates
     */
    public static PolarCoordinates fromCartesian(final double x, final double y) {
        final double azimuth = Math.atan2(y, x);
        final double radius = Math.hypot(x, y);

        return new PolarCoordinates(radius, azimuth);
    }

    /** Convert the given Cartesian coordinates to polar form.
     * @param vec vector containing Cartesian coordinates
     * @return polar coordinates equivalent to the given Cartesian coordinates
     */
    public static PolarCoordinates fromCartesian(final Vector2D vec) {
        return fromCartesian(vec.getX(), vec.getY());
    }

    /** Convert the given polar coordinates to Cartesian form.
     * @param radius Radius value.
     * @param azimuth Azimuth angle in radians.
     * @return A 2-dimensional vector with an equivalent set of
     *      coordinates in Cartesian form
     */
    public static Vector2D toCartesian(final double radius, final double azimuth) {
        final double x = radius * Math.cos(azimuth);
        final double y = radius * Math.sin(azimuth);

        return Vector2D.of(x, y);
    }

    /** Parse the given string and return a new polar coordinates instance. The parsed
     * coordinates are normalized as in the {@link #of(double, double)} method. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param input the string to parse
     * @return new {@link PolarCoordinates} instance
     * @throws IllegalArgumentException if the string format is invalid.
     */
    public static PolarCoordinates parse(final String input) {
        return SimpleTupleFormat.getDefault().parse(input, PolarCoordinates::new);
    }

    /** Normalize an azimuth value to be within the range {@code [0, 2pi)}.
     * @param azimuth azimuth value in radians
     * @return equivalent azimuth value in the range {@code [0, 2pi)}.
     */
    public static double normalizeAzimuth(final double azimuth) {
        if (Double.isFinite(azimuth)) {
            return Angle.Rad.WITHIN_0_AND_2PI.applyAsDouble(azimuth);
        }

        return azimuth;
    }
}
