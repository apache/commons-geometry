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
import org.apache.commons.geometry.core.internal.DoubleFunction1N;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** This class represents a point on the 1-sphere.
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class S1Point implements Point<S1Point>, Serializable {

   // CHECKSTYLE: stop ConstantName
    /** A vector with all coordinates set to NaN. */
    public static final S1Point NaN = new S1Point(Double.NaN, Vector2D.NaN);
    // CHECKSTYLE: resume ConstantName

    /** Serializable UID. */
    private static final long serialVersionUID = 20180710L;

    /** Factory for delegating instance creation. */
    private static DoubleFunction1N<S1Point> FACTORY = new DoubleFunction1N<S1Point>() {

        /** {@inheritDoc} */
        @Override
        public S1Point apply(double n) {
            return S1Point.of(n);
        }
    };

    /** Azimuthal angle in radians \( \alpha \). */
    private final double alpha;

    /** Corresponding 2D normalized vector. */
    private final Vector2D vector;

    /** Build a point from its internal components.
     * @param alpha azimuthal angle \( \alpha \)
     * @param vector corresponding vector
     */
    private S1Point(final double alpha, final Vector2D vector) {
        this.alpha  = alpha;
        this.vector = vector;
    }

    /** Get the azimuthal angle in radians \( \alpha \).
     * @return azimuthal angle \( \alpha \)
     * @see #S1Point(double)
     */
    public double getAlpha() {
        return alpha;
    }

    /** Get the corresponding normalized vector in the 2D euclidean space.
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
        return Double.isNaN(alpha);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && Double.isInfinite(alpha);
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

            return alpha == rhs.alpha;
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
        return 1759 * Double.hashCode(alpha);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(getAlpha());
    }

    /** Creates a new point instance from the given azimuthal coordinate value.
     * @param alpha azimuthal angle in radians \( \alpha \)
     * @return point instance with the given azimuth coordinate value
     * @see #getAlpha()
     */
    public static S1Point of(double alpha) {
        double normalizedAlpha = PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(alpha);
        Vector2D vector = Vector2D.of(Math.cos(normalizedAlpha), Math.sin(normalizedAlpha));

        return new S1Point(normalizedAlpha, vector);
    }

    /** Parses the given string and returns a new point instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return point instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static S1Point parse(String str) throws IllegalArgumentException {
        return SimpleTupleFormat.getDefault().parse(str, FACTORY);
    }
}
