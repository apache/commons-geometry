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

import java.io.Serializable;
import java.text.ParsePosition;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.Spatial;
import org.apache.commons.geometry.core.util.AbstractCoordinateParser;
import org.apache.commons.geometry.core.util.Coordinates;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Class representing a set of polar coordinates in 2 dimensional
 * Euclidean space. Coordinates are normalized so that {@code radius}
 * is in the range {@code [0, +infinity)} and {@code azimuth} is in the
 * range {@code (-pi, pi]}.
 */
public class PolarCoordinates implements Spatial, Serializable {

    /** Serializable version UID */
    private static final long serialVersionUID = 20180630L;

    /** Shared parser/formatter instance **/
    private static final PolarCoordinatesParser PARSER = new PolarCoordinatesParser();

    /** Radius value */
    private final double radius;

    /** Azimuth angle in radians. */
    private final double azimuth;

    /** Simple constructor. Input values are normalized.
     * @param radius Radius value.
     * @param azimuth Azimuth angle in radians.
     */
    private PolarCoordinates(double radius, double azimuth) {
        if (radius < 0) {
            // negative radius; flip the angles
            radius = Math.abs(radius);
            azimuth += Geometry.PI;
        }

        if (Double.isFinite(azimuth) && (azimuth <= Geometry.MINUS_PI || azimuth > Geometry.PI)) {
            azimuth = PlaneAngleRadians.normalizeBetweenMinusPiAndPi(azimuth);

            // azimuth is now in the range [-pi, pi] but we want it to be in the range
            // (-pi, pi] in order to have completely unique coordinates
            if (azimuth <= -Geometry.PI) {
                azimuth += Geometry.TWO_PI;
            }
        }

        this.radius = radius;
        this.azimuth = azimuth;
    }

    /** Return the radius value. The value will be greater than or equal to 0.
     * @return radius value
     */
    public double getRadius() {
        return radius;
    }

    /** Return the azimuth angle in radians. The value will be
     * in the range {@code (-pi, pi]}.
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

    /** Convert this set of polar coordinates to Cartesian coordinates.
     * The Cartesian coordinates are computed and passed to the given
     * factory instance. The factory's return value is returned.
     * @param factory Factory instance that will be passed the computed Cartesian coordinates
     * @return the value returned by the given factory when passed Cartesian
     *      coordinates equivalent to this set of polar coordinates.
     */
    public <T> T toCartesian(final Coordinates.Factory2D<T> factory) {
        return toCartesian(radius, azimuth, factory);
    }

    /** Convert this set of polar coordinates to a 2-dimensional
     * vector.
     * @return A 2-dimensional vector with an equivalent set of
     *      coordinates.
     */
    public Vector2D toVector() {
        return toCartesian(Vector2D.getFactory());
    }

    /** Convert this set of polar coordinates to a 2-dimensional
     * point.
     * @return A 2-dimensional point with an equivalent set of
     *      coordinates.
     */
    public Point2D toPoint() {
        return toCartesian(Point2D.getFactory());
    }

    /**
     * Get a hashCode for this set of polar coordinates.
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
     * coordinate set are equal to <code>Double.NaN</code>, the set is equal to
     * {@link #NaN}.
     * </p>
     *
     * @param other Object to test for equality to this
     * @return true if two PolarCoordinates objects are equal, false if
     *         object is null, not an instance of PolarCoordinates, or
     *         not equal to this PolarCoordinates instance
     *
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof PolarCoordinates) {
            final PolarCoordinates rhs = (PolarCoordinates) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return (radius == rhs.radius) && (azimuth == rhs.azimuth);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return PARSER.format(this);
    }

    /** Return a new polar coordinate instance with the given values.
     * The values are normalized so that radius lies in the range {@code [0, +infinity)}
     * and azimuth in the range {@code (-pi, pi]}.
     * @param radius Radius value.
     * @param azimuth Azimuth angle in radians.
     * @return
     */
    public static PolarCoordinates of(double radius, double azimuth) {
        return new PolarCoordinates(radius, azimuth);
    }

    /** Convert the given Cartesian coordinates to polar form.
     * @param x X coordinate value
     * @param y Y coordinate value
     * @return polar coordinates equivalent to the given Cartesian coordinates
     */
    public static PolarCoordinates ofCartesian(final double x, final double y) {
        final double azimuth = Math.atan2(y, x);
        final double radius = Math.hypot(x, y);

        return new PolarCoordinates(radius, azimuth);
    }

    /** Parse the given string and return a new polar coordinates instance. The parsed
     * coordinates are normalized so that radius is within the range {@code [0, +infinity)}
     * and azimuth is within the range {@code (-pi, pi]}. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param input the string to parse
     * @return new {@link PolarCoordinates} instance
     * @throws IllegalArgumentException if the string format is invalid.
     */
    public static PolarCoordinates parse(String input) {
        return PARSER.parse(input);
    }

    /** Convert the given set of polar coordinates to Cartesian coordinates.
     * The Cartesian coordinates are computed and passed to the given
     * factory instance. The factory's return value is returned.
     * @param radius Radius value
     * @param azimuth Azimuth value in radians
     * @param factory Factory instance that will be passed the computed Cartesian coordinates
     * @param <T> Type returned by the factory
     * @return the value returned by the factory when passed Cartesian
     *      coordinates equivalent to the given set of polar coordinates.
     */
    public static <T> T toCartesian(final double radius, final double azimuth, final Coordinates.Factory2D<T> factory) {
        final double x = radius * Math.cos(azimuth);
        final double y = radius * Math.sin(azimuth);

        return factory.create(x, y);
    }

    /** Parser and formatter class for polar coordinates. */
    private static class PolarCoordinatesParser extends AbstractCoordinateParser {

        /** String prefix for the radius value. */
        private static final String RADIUS_PREFIX = "r=";

        /** String prefix for the azimuth value. */
        private static final String AZIMUTH_PREFIX = "az=";

        /** Simple constructor. */
        private PolarCoordinatesParser() {
            super(",", "(", ")");
        }

        /** Return a standardized string representation of the given set of polar
         * coordinates.
         * @param polar coordinates to format
         * @return a standard string representation of the polar coordinates
         */
        public String format(PolarCoordinates polar) {
            final StringBuilder sb = new StringBuilder();

            sb.append(getPrefix());

            sb.append(RADIUS_PREFIX);
            sb.append(polar.getRadius());

            sb.append(getSeparator());
            sb.append(" ");

            sb.append(AZIMUTH_PREFIX);
            sb.append(polar.getAzimuth());

            sb.append(getSuffix());

            return sb.toString();
        }

        /** Parse the given string and return a set of standardized polar coordinates.
         * @param str the string to parse
         * @return polar coordinates
         */
        public PolarCoordinates parse(String str) {
            final ParsePosition pos = new ParsePosition(0);

            readPrefix(str, pos);

            consumeWhitespace(str, pos);
            readSequence(str, RADIUS_PREFIX, pos);
            final double radius = readCoordinateValue(str, pos);

            consumeWhitespace(str, pos);
            readSequence(str, AZIMUTH_PREFIX, pos);
            final double azimuth = readCoordinateValue(str, pos);

            readSuffix(str, pos);
            endParse(str, pos);

            return new PolarCoordinates(radius, azimuth);
        }
    }
}
