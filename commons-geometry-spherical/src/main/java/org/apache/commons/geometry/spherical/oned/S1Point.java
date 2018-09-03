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

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.twod.PolarCoordinates;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** This class represents a point on the 1-sphere.
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class S1Point implements Point<S1Point>, Serializable {

    // CHECKSTYLE: stop ConstantName
    /** A point with all coordinates set to NaN. */
    public static final S1Point NaN = new S1Point(Double.NaN);
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
    private S1Point(final double azimuth) {
        this.azimuth  = PolarCoordinates.normalizeAzimuth(azimuth);
        this.vector = Double.isFinite(azimuth) ? Vector2D.ofPolar(1.0, azimuth) : Vector2D.NaN;
    }

    /** Get the azimuthal angle in radians.
     * @return azimuthal angle
     * @see S1Point#of(double)
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
    public double distance(final S1Point point) {
        return distance(this, point);
    }

    /** Compute the distance (angular separation) between two points.
     * @param p1 first vector
     * @param p2 second vector
     * @return the angular separation between p1 and p2
     */
    public static double distance(S1Point p1, S1Point p2) {
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

        if (other instanceof S1Point) {
            final S1Point rhs = (S1Point) other;
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
    public static S1Point of(double azimuth) {
        return new S1Point(azimuth);
    }

    /** Parses the given string and returns a new point instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return point instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static S1Point parse(String str) throws IllegalArgumentException {
        return SimpleTupleFormat.getDefault().parse(str, S1Point::new);
    }
}
