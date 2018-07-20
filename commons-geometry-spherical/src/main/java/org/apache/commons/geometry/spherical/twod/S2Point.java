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
import org.apache.commons.geometry.core.internal.DoubleFunction2N;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** This class represents a point on the 2-sphere.
 * <p>
 * We use the mathematical convention to use the azimuthal angle \( \theta \)
 * in the x-y plane as the first coordinate, and the polar angle \( \varphi \)
 * as the second coordinate (see <a
 * href="http://mathworld.wolfram.com/SphericalCoordinates.html">Spherical
 * Coordinates</a> in MathWorld).
 * </p>
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class S2Point implements Point<S2Point>, Serializable {

    /** +I (coordinates: \( \theta = 0, \varphi = \pi/2 \)). */
    public static final S2Point PLUS_I = new S2Point(0, 0.5 * Math.PI, Vector3D.PLUS_X);

    /** +J (coordinates: \( \theta = \pi/2, \varphi = \pi/2 \))). */
    public static final S2Point PLUS_J = new S2Point(0.5 * Math.PI, 0.5 * Math.PI, Vector3D.PLUS_Y);

    /** +K (coordinates: \( \theta = any angle, \varphi = 0 \)). */
    public static final S2Point PLUS_K = new S2Point(0, 0, Vector3D.PLUS_Z);

    /** -I (coordinates: \( \theta = \pi, \varphi = \pi/2 \)). */
    public static final S2Point MINUS_I = new S2Point(Math.PI, 0.5 * Math.PI, Vector3D.MINUS_X);

    /** -J (coordinates: \( \theta = 3\pi/2, \varphi = \pi/2 \)). */
    public static final S2Point MINUS_J = new S2Point(1.5 * Math.PI, 0.5 * Math.PI, Vector3D.MINUS_Y);

    /** -K (coordinates: \( \theta = any angle, \varphi = \pi \)). */
    public static final S2Point MINUS_K = new S2Point(0, Math.PI, Vector3D.MINUS_Z);

    // CHECKSTYLE: stop ConstantName
    /** A vector with all coordinates set to NaN. */
    public static final S2Point NaN = new S2Point(Double.NaN, Double.NaN, Vector3D.NaN);
    // CHECKSTYLE: resume ConstantName

    /** Serializable UID. */
    private static final long serialVersionUID = 20180710L;

    /** Factory for delegating instance creation. */
    private static DoubleFunction2N<S2Point> FACTORY = new DoubleFunction2N<S2Point>() {

        /** {@inheritDoc} */
        @Override
        public S2Point apply(double n1, double n2) {
            return S2Point.of(n1, n2);
        }
    };

    /** Azimuthal angle \( \theta \) in the x-y plane. */
    private final double theta;

    /** Polar angle \( \varphi \). */
    private final double phi;

    /** Corresponding 3D normalized vector. */
    private final Vector3D vector;

    /** Build a point from its internal components.
     * @param theta azimuthal angle \( \theta \) in the x-y plane
     * @param phi polar angle \( \varphi \)
     * @param vector corresponding vector
     */
    private S2Point(final double theta, final double phi, final Vector3D vector) {
        this.theta  = theta;
        this.phi    = phi;
        this.vector = vector;
    }

    /** Get the azimuthal angle \( \theta \) in the x-y plane.
     * @return azimuthal angle \( \theta \) in the x-y plane
     * @see S2Point#of(double, double)
     */
    public double getTheta() {
        return theta;
    }

    /** Get the polar angle \( \varphi \).
     * @return polar angle \( \varphi \)
     * @see S2Point#of(double, double)
     */
    public double getPhi() {
        return phi;
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
        return Double.isNaN(theta) || Double.isNaN(phi);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && (Double.isInfinite(theta) || Double.isInfinite(phi));
    }

    /** Get the opposite of the instance.
     * @return a new vector which is opposite to the instance
     */
    public S2Point negate() {
        return new S2Point(-theta, Math.PI - phi, vector.negate());
    }

    /** {@inheritDoc} */
    @Override
    public double distance(final S2Point point) {
        return distance(this, point);
    }

    /** Compute the distance (angular separation) between two points.
     * @param p1 first vector
     * @param p2 second vector
     * @return the angular separation between p1 and p2
     */
    public static double distance(S2Point p1, S2Point p2) {
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

        if (other instanceof S2Point) {
            final S2Point rhs = (S2Point) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return (theta == rhs.theta) && (phi == rhs.phi);
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
        return 134 * (37 * Double.hashCode(theta) +  Double.hashCode(phi));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(getTheta(), getPhi());
    }

    /** Build a vector from its spherical coordinates
     * @param theta azimuthal angle \( \theta \) in the x-y plane
     * @param phi polar angle \( \varphi \)
     * @return point instance with the given coordinates
     * @see #getTheta()
     * @see #getPhi()
     * @exception IllegalArgumentException if \( \varphi \) is not in the [\( 0; \pi \)] range
     */
    public static S2Point of(final double theta, final double phi) {
        return new S2Point(theta, phi, vector(theta, phi));
    }

    /** Build a point from its underlying 3D vector
     * @param vector 3D vector
     * @return point instance with the coordinates determined by the given 3D vector
     * @exception IllegalArgumentException if vector norm is zero
     */
    public static S2Point of(final Vector3D vector) {
        return new S2Point(Math.atan2(vector.getY(), vector.getX()),
                Vector3D.PLUS_Z.angle(vector),
                vector.normalize());
    }

    /** Build the normalized vector corresponding to spherical coordinates.
     * @param theta azimuthal angle \( \theta \) in the x-y plane
     * @param phi polar angle \( \varphi \)
     * @return normalized vector
     * @exception IllegalArgumentException if \( \varphi \) is not in the [\( 0; \pi \)] range
     */
    private static Vector3D vector(final double theta, final double phi)
       throws IllegalArgumentException {

        if (phi < 0 || phi > Math.PI) {
            throw new IllegalArgumentException(phi + " is out of [" + 0 + ", " + Math.PI + "] range");
        }

        final double cosTheta = Math.cos(theta);
        final double sinTheta = Math.sin(theta);
        final double cosPhi   = Math.cos(phi);
        final double sinPhi   = Math.sin(phi);

        return Vector3D.of(cosTheta * sinPhi, sinTheta * sinPhi, cosPhi);
    }

    /** Parses the given string and returns a new point instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return point instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static S2Point parse(String str) throws IllegalArgumentException {
        return SimpleTupleFormat.getDefault().parse(str, FACTORY);
    }
}
