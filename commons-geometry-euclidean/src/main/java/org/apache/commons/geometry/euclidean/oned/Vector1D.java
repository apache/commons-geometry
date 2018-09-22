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
package org.apache.commons.geometry.euclidean.oned;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.exception.IllegalNormException;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.EuclideanVector;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.numbers.arrays.LinearCombination;

/** This class represents a vector in one-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public class Vector1D extends Cartesian1D implements EuclideanVector<Point1D, Vector1D> {

    /** Zero vector (coordinates: 0). */
    public static final Vector1D ZERO = new Vector1D(0.0);

    /** Unit vector (coordinates: 1). */
    public static final Vector1D ONE  = new UnitVector(1.0);

    /** Negation of unit vector (coordinates: -1). */
    public static final Vector1D MINUS_ONE = new UnitVector(-1.0);

    // CHECKSTYLE: stop ConstantName
    /** A vector with all coordinates set to NaN. */
    public static final Vector1D NaN = new Vector1D(Double.NaN);
    // CHECKSTYLE: resume ConstantName

    /** A vector with all coordinates set to positive infinity. */
    public static final Vector1D POSITIVE_INFINITY =
        new Vector1D(Double.POSITIVE_INFINITY);

    /** A vector with all coordinates set to negative infinity. */
    public static final Vector1D NEGATIVE_INFINITY =
        new Vector1D(Double.NEGATIVE_INFINITY);

    /** Serializable UID. */
    private static final long serialVersionUID = 20180710L;

    /** Simple constructor.
     * @param x abscissa (coordinate value)
     */
    private Vector1D(double x) {
        super(x);
    }

    /** {@inheritDoc} */
    @Override
    public Point1D asPoint() {
        return Point1D.of(getX());
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D lerp(Vector1D p, double t) {
        return linearCombination(1.0 - t, this, t, p);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D getZero() {
        return ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public double getNorm1() {
        return Vectors.norm1(getX());
    }

    /** {@inheritDoc} */
    @Override
    public double getNorm() {
        return Vectors.norm(getX());
    }

    /** {@inheritDoc} */
    @Override
    public double getNormSq() {
        return Vectors.normSq(getX());
    }

    /** {@inheritDoc} */
    @Override
    public double getNormInf() {
        return Vectors.normInf(getX());
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D withNorm(double magnitude) {
        getCheckedNorm(); // validate our norm value

        return (getX() > 0.0)? new Vector1D(magnitude) : new Vector1D(-magnitude);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D add(Vector1D v) {
        return new Vector1D(getX() + v.getX());
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D add(double factor, Vector1D v) {
        return new Vector1D(getX() + (factor * v.getX()));
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D subtract(Vector1D v) {
        return new Vector1D(getX() - v.getX());
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D subtract(double factor, Vector1D v) {
        return new Vector1D(getX() - (factor * v.getX()));
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D negate() {
        return new Vector1D(-getX());
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D normalize() {
        return normalize(getX());
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D scalarMultiply(double a) {
        return new Vector1D(a * getX());
    }

    /** {@inheritDoc} */
    @Override
    public double distance1(Vector1D v) {
        return Vectors.norm1(getX() - v.getX());
    }

    /** {@inheritDoc} */
    @Override
    public double distance(Vector1D v) {
        return Vectors.norm(getX() - v.getX());
    }

    /** {@inheritDoc} */
    @Override
    public double distanceInf(Vector1D v) {
        return Vectors.normInf(getX() - v.getX());
    }

    /** {@inheritDoc} */
    @Override
    public double distanceSq(Vector1D v) {
        return Vectors.normSq(getX() - v.getX());
    }

    /** {@inheritDoc} */
    @Override
    public double dotProduct(Vector1D v) {
        return getX() * v.getX();
    }

    /** {@inheritDoc}
     * <p>For the one-dimensional case, this method returns 0 if the vector x values have
     * the same sign and {@code pi} if they are opposite.</p>
     */
    @Override
    public double angle(final Vector1D v) {
        // validate the norm values
        getCheckedNorm();
        v.getCheckedNorm();

        final double sig1 = Math.signum(getX());
        final double sig2 = Math.signum(v.getX());

        // the angle is 0 if the x value signs are the same and pi if not
        return (sig1 == sig2) ? 0.0 : Geometry.PI;
    }

    /**
     * Get a hashCode for the vector.
     * <p>All NaN values have the same hash code.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        if (isNaN()) {
            return 857;
        }
        return 403 * Double.hashCode(getX());
    }

    /**
     * Test for the equality of two vectors.
     * <p>
     * If all coordinates of two vectors are exactly the same, and none are
     * <code>Double.NaN</code>, the two vectors are considered to be equal.
     * </p>
     * <p>
     * <code>NaN</code> coordinates are considered to globally affect the vector
     * and be equal to each other - i.e, if either (or all) coordinates of the
     * vector are equal to <code>Double.NaN</code>, the vector is equal to
     * {@link #NaN}.
     * </p>
     *
     * @param other Object to test for equality to this
     * @return true if two vector objects are equal, false if
     *         object is null, not an instance of Vector1D, or
     *         not equal to this Vector1D instance
     *
     */
    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (other instanceof Vector1D) {
            final Vector1D rhs = (Vector1D) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return getX() == rhs.getX();
        }
        return false;
    }

    /** Returns the vector norm value, throwing an {@link IllegalNormException} if the value
     * is not real (ie, NaN or infinite) or zero.
     * @return the vector norm value, guaranteed to be real and non-zero
     * @throws IllegalNormException if the vector norm is zero, NaN, or infinite
     */
    private double getCheckedNorm() {
        return Vectors.checkedNorm(getNorm());
    }

    /** Returns a vector with the given coordinate value.
     * @param x vector coordinate
     * @return vector instance
     */
    public static Vector1D of(double x) {
        return new Vector1D(x);
    }

    /** Returns a normalized vector derived from the given value.
     * @param x abscissa (first coordinate value)
     * @return normalized vector instance
     * @throws IllegalNormException if the norm of the given value is zero, NaN, or infinite
     */
    public static Vector1D normalize(final double x) {
        Vectors.checkedNorm(Vectors.norm(x));

        return (x > 0.0) ? ONE : MINUS_ONE;
    }

    /** Parses the given string and returns a new vector instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return vector instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Vector1D parse(String str) {
        return SimpleTupleFormat.getDefault().parse(str, Vector1D::new);
    }

    /** Returns a vector consisting of the linear combination of the inputs.
     * <p>
     * A linear combination is the sum of all of the inputs multiplied by their
     * corresponding scale factors. All inputs are interpreted as vectors. If points
     * are to be passed, they should be viewed as representing the vector from the
     * zero point to the given point.
     * </p>
     *
     * @param a scale factor for first coordinate
     * @param c first coordinate
     * @return vector with coordinates calculated by {@code a * c}
     */
    public static Vector1D linearCombination(double a, Cartesian1D c) {
        return new Vector1D(a * c.getX());
    }

    /** Returns a vector consisting of the linear combination of the inputs.
     * <p>
     * A linear combination is the sum of all of the inputs multiplied by their
     * corresponding scale factors. All inputs are interpreted as vectors. If points
     * are to be passed, they should be viewed as representing the vector from the
     * zero point to the given point.
     * </p>
     *
     * @param a1 scale factor for first coordinate
     * @param c1 first coordinate
     * @param a2 scale factor for second coordinate
     * @param c2 second coordinate
     * @return vector with coordinates calculated by {@code (a1 * c1) + (a2 * c2)}
     */
    public static Vector1D linearCombination(double a1, Cartesian1D c1, double a2, Cartesian1D c2) {
        return new Vector1D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX()));
    }

    /** Returns a vector consisting of the linear combination of the inputs.
     * <p>
     * A linear combination is the sum of all of the inputs multiplied by their
     * corresponding scale factors. All inputs are interpreted as vectors. If points
     * are to be passed, they should be viewed as representing the vector from the
     * zero point to the given point.
     * </p>
     *
     * @param a1 scale factor for first coordinate
     * @param c1 first coordinate
     * @param a2 scale factor for second coordinate
     * @param c2 second coordinate
     * @param a3 scale factor for third coordinate
     * @param c3 third coordinate
     * @return vector with coordinates calculated by {@code (a1 * c1) + (a2 * c2) + (a3 * c3)}
     */
    public static Vector1D linearCombination(double a1, Cartesian1D c1, double a2, Cartesian1D c2,
            double a3, Cartesian1D c3) {
        return new Vector1D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX()));
    }

    /** Returns a vector consisting of the linear combination of the inputs.
     * <p>
     * A linear combination is the sum of all of the inputs multiplied by their
     * corresponding scale factors. All inputs are interpreted as vectors. If points
     * are to be passed, they should be viewed as representing the vector from the
     * zero point to the given point.
     * </p>
     *
     * @param a1 scale factor for first coordinate
     * @param c1 first coordinate
     * @param a2 scale factor for second coordinate
     * @param c2 second coordinate
     * @param a3 scale factor for third coordinate
     * @param c3 third coordinate
     * @param a4 scale factor for fourth coordinate
     * @param c4 fourth coordinate
     * @return point with coordinates calculated by {@code (a1 * c1) + (a2 * c2) + (a3 * c3) + (a4 * c4)}
     */
    public static Vector1D linearCombination(double a1, Cartesian1D c1, double a2, Cartesian1D c2,
            double a3, Cartesian1D c3, double a4, Cartesian1D c4) {
        return new Vector1D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX(), a4, c4.getX()));
    }

    /** Private class used to represent unit vectors. This allows optimizations to be performed for certain
     * operations.
     */
    private static final class UnitVector extends Vector1D {

        /** Serializable version identifier */
        private static final long serialVersionUID = 20180903L;

        /** Simple constructor. Callers are responsible for ensuring that the given
         * values represent a normalized vector.
         * @param x abscissa (first coordinate value)
         */
        private UnitVector(final double x) {
            super(x);
        }

        /** {@inheritDoc} */
        @Override
        public Vector1D normalize() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Vector1D withNorm(final double mag) {
            return scalarMultiply(mag);
        }
    }
}
