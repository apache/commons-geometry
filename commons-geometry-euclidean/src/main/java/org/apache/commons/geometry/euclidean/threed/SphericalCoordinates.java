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
package org.apache.commons.geometry.euclidean.threed;

import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.apache.commons.geometry.core.Spatial;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.geometry.euclidean.twod.PolarCoordinates;

/** Class representing <a href="https://en.wikipedia.org/wiki/Spherical_coordinate_system">spherical coordinates</a>
 * in 3 dimensional Euclidean space.
 *
 * <p>Spherical coordinates for a point are defined by three values:
 * <ol>
 *  <li><em>Radius</em> - The distance from the point to a fixed referenced point.</li>
 *  <li><em>Azimuth angle</em> - The angle measured from a fixed reference direction in a plane to
 * the orthogonal projection of the point on that plane.</li>
 *  <li><em>Polar angle</em> - The angle measured from a fixed zenith direction to the point. The zenith
 *direction must be orthogonal to the reference plane.</li>
 * </ol>
 * This class follows the convention of using the origin as the reference point; the positive x-axis as the
 * reference direction for the azimuth angle, measured in the x-y plane with positive angles moving counter-clockwise
 * toward the positive y-axis; and the positive z-axis as the zenith direction. Spherical coordinates are
 * related to Cartesian coordinates as follows:
 * <pre>
 * x = r cos(&theta;) sin(&Phi;)
 * y = r sin(&theta;) sin(&Phi;)
 * z = r cos(&Phi;)
 *
 * r = &radic;(x^2 + y^2 + z^2)
 * &theta; = atan2(y, x)
 * &Phi; = acos(z/r)
 * </pre>
 * where <em>r</em> is the radius, <em>&theta;</em> is the azimuth angle, and <em>&Phi;</em> is the polar angle
 * of the spherical coordinates.
 *
 * <p>There are numerous, competing conventions for the symbols used to represent spherical coordinate values. For
 * example, the mathematical convention is to use <em>(r, &theta;, &Phi;)</em> to represent radius, azimuth angle, and
 * polar angle, whereas the physics convention flips the angle values and uses <em>(r, &Phi;, &theta;)</em>. As such,
 * this class avoids the use of these symbols altogether in favor of the less ambiguous formal names of the values,
 * e.g. {@code radius}, {@code azimuth}, and {@code polar}.</p>
 *
 * <p>In order to ensure the uniqueness of coordinate sets, coordinate values
 * are normalized so that {@code radius} is in the range {@code [0, +Infinity)},
 * {@code azimuth} is in the range {@code [0, 2pi)}, and {@code polar} is in the
 * range {@code [0, pi]}.</p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Spherical_coordinate_system">Spherical Coordinate System</a>
 */
public final class SphericalCoordinates implements Spatial {
    /** Radius value. */
    private final double radius;

    /** Azimuth angle in radians. */
    private final double azimuth;

    /** Polar angle in radians. */
    private final double polar;

    /** Simple constructor. The given inputs are normalized.
     * @param radius Radius value.
     * @param azimuth Azimuth angle in radians.
     * @param polar Polar angle in radians.
     */
    private SphericalCoordinates(double radius, double azimuth, double polar) {
        if (radius < 0) {
            // negative radius; flip the angles
            radius = Math.abs(radius);
            azimuth += PlaneAngleRadians.PI;
            polar += PlaneAngleRadians.PI;
        }

        this.radius = radius;
        this.azimuth = normalizeAzimuth(azimuth);
        this.polar = normalizePolar(polar);
    }

    /** Return the radius value. The value is in the range {@code [0, +Infinity)}.
     * @return the radius value
     */
    public double getRadius() {
        return radius;
    }

    /** Return the azimuth angle in radians. This is the angle in the x-y plane measured counter-clockwise from
     * the positive x axis. The angle is in the range {@code [0, 2pi)}.
     * @return the azimuth angle in radians
     */
    public double getAzimuth() {
        return azimuth;
    }

    /** Return the polar angle in radians. This is the angle the coordinate ray makes with the positive z axis.
     * The angle is in the range {@code [0, pi]}.
     * @return the polar angle in radians
     */
    public double getPolar() {
        return polar;
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return 3;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNaN() {
        return Double.isNaN(radius) || Double.isNaN(azimuth) || Double.isNaN(polar);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && (Double.isInfinite(radius) || Double.isInfinite(azimuth) || Double.isInfinite(polar));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinite() {
        return Double.isFinite(radius) && Double.isFinite(azimuth) && Double.isFinite(polar);
    }

    /** Convert this set of spherical coordinates to a Cartesian form.
     * @return A 3-dimensional vector with an equivalent set of
     *      Cartesian coordinates.
     */
    public Vector3D toVector() {
        return toCartesian(radius, azimuth, polar);
    }

    /** Get a hashCode for this set of spherical coordinates.
     * <p>All NaN values have the same hash code.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        if (isNaN()) {
            return 127;
        }
        return 449 * (79 * Double.hashCode(radius) + Double.hashCode(azimuth) + Double.hashCode(polar));
    }

    /** Test for the equality of two sets of spherical coordinates.
     * <p>
     * If all values of two sets of coordinates are exactly the same, and none are
     * <code>Double.NaN</code>, the two sets are considered to be equal.
     * </p>
     * <p>
     * <code>NaN</code> values are considered to globally affect the coordinates
     * and be equal to each other - i.e, if any (or all) values of the
     * coordinate set are equal to <code>Double.NaN</code>, the set as a whole
     * is considered to equal NaN.
     * </p>
     *
     * @param other Object to test for equality to this
     * @return true if two SphericalCoordinates objects are equal, false if
     *         object is null, not an instance of SphericalCoordinates, or
     *         not equal to this SphericalCoordinates instance
     *
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof SphericalCoordinates) {
            final SphericalCoordinates rhs = (SphericalCoordinates) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return (radius == rhs.radius) && (azimuth == rhs.azimuth) && (polar == rhs.polar);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(radius, azimuth, polar);
    }

    /** Return a new instance with the given spherical coordinate values. The values are normalized
     * so that {@code radius} lies in the range {@code [0, +Infinity)}, {@code azimuth} lies in the range
     * {@code [0, 2pi)}, and {@code polar} lies in the range {@code [0, +pi]}.
     * @param radius the length of the line segment from the origin to the coordinate point.
     * @param azimuth the angle in the x-y plane, measured in radians counter-clockwise
     *      from the positive x-axis.
     * @param polar the angle in radians between the positive z-axis and the ray from the origin
     *      to the coordinate point.
     * @return a new {@link SphericalCoordinates} instance representing the same point as the given set of
     *      spherical coordinates.
     */
    public static SphericalCoordinates of(final double radius, final double azimuth, final double polar) {
        return new SphericalCoordinates(radius, azimuth, polar);
    }

    /** Convert the given set of Cartesian coordinates to spherical coordinates.
     * @param x X coordinate value
     * @param y Y coordinate value
     * @param z Z coordinate value
     * @return a set of spherical coordinates equivalent to the given Cartesian coordinates
     */
    public static SphericalCoordinates fromCartesian(final double x, final double y, final double z) {
        final double radius = Vectors.norm(x, y, z);
        final double azimuth = Math.atan2(y, x);

        // default the polar angle to 0 when the radius is 0
        final double polar = (radius > 0.0) ? Math.acos(z / radius) : 0.0;

        return new SphericalCoordinates(radius, azimuth, polar);
    }

    /** Convert the given set of Cartesian coordinates to spherical coordinates.
     * @param vec vector containing Cartesian coordinates to convert
     * @return a set of spherical coordinates equivalent to the given Cartesian coordinates
     */
    public static SphericalCoordinates fromCartesian(final Vector3D vec) {
        return fromCartesian(vec.getX(), vec.getY(), vec.getZ());
    }

    /** Convert the given set of spherical coordinates to Cartesian coordinates.
     * @param radius The spherical radius value.
     * @param azimuth The spherical azimuth angle in radians.
     * @param polar The spherical polar angle in radians.
     * @return A 3-dimensional vector with an equivalent set of
     *      Cartesian coordinates.
     */
    public static Vector3D toCartesian(final double radius, final double azimuth, final double polar) {
        final double xyLength = radius * Math.sin(polar);

        final double x = xyLength * Math.cos(azimuth);
        final double y = xyLength * Math.sin(azimuth);
        final double z = radius * Math.cos(polar);

        return Vector3D.of(x, y, z);
    }

    /** Parse the given string and return a new {@link SphericalCoordinates} instance. The parsed
     * coordinate values are normalized as in the {@link #of(double, double, double)} method.
     * The expected string format is the same as that returned by {@link #toString()}.
     * @param input the string to parse
     * @return new {@link SphericalCoordinates} instance
     * @throws IllegalArgumentException if the string format is invalid.
     */
    public static SphericalCoordinates parse(String input) {
        return SimpleTupleFormat.getDefault().parse(input, SphericalCoordinates::new);
    }

    /** Normalize an azimuth value to be within the range {@code [0, 2pi)}. This
     * is exactly equivalent to {@link PolarCoordinates#normalizeAzimuth(double)}.
     * @param azimuth azimuth value in radians
     * @return equivalent azimuth value in the range {@code [0, 2pi)}.
     * @see PolarCoordinates#normalizeAzimuth(double)
     */
    public static double normalizeAzimuth(double azimuth) {
        return PolarCoordinates.normalizeAzimuth(azimuth);
    }

    /** Normalize a polar value to be within the range {@code [0, +pi]}. Since the
     * polar angle is the angle between two vectors (the zenith direction and the
     * point vector), the sign of the angle is not significant as in the azimuth angle.
     * For example, a polar angle of {@code -pi/2} and one of {@code +pi/2} will both
     * normalize to {@code pi/2}.
     * @param polar polar value in radians
     * @return equalivalent polar value in the range {@code [0, +pi]}
     */
    public static double normalizePolar(double polar) {
        // normalize the polar angle; this is the angle between the polar vector and the point ray
        // so it is unsigned (unlike the azimuth) and should be in the range [0, pi]
        if (Double.isFinite(polar)) {
            polar = Math.abs(PlaneAngleRadians.normalizeBetweenMinusPiAndPi(polar));
        }

        return polar;
    }
}
