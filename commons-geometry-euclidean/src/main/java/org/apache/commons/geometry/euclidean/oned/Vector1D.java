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

import java.util.Comparator;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.exception.IllegalNormException;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanVector;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.numbers.arrays.LinearCombination;

/** This class represents vectors and points in one-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public class Vector1D extends EuclideanVector<Vector1D> {

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

    /** Comparator that sorts vectors in component-wise ascending order.
     * Vectors are only considered equal if their components match exactly.
     */
    public static final Comparator<Vector1D> STRICT_ASCENDING_ORDER = (a, b) ->
        Double.compare(a.getX(), b.getX());

    /** Serializable UID. */
    private static final long serialVersionUID = 20180710L;

    /** Abscissa (coordinate value). */
    private final double x;

    /** Simple constructor.
     * @param x abscissa (coordinate value)
     */
    private Vector1D(double x) {
        this.x = x;
    }

    /**
     * Returns the abscissa (coordinate value) of the instance.
     * @return the abscissa value
     */
    public double getX() {
        return x;
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNaN() {
        return Double.isNaN(x);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && Double.isInfinite(x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D vectorTo(Vector1D v) {
        return v.subtract(this);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D directionTo(Vector1D v) {
        return normalize(v.x - x);
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
    public double norm() {
        return Vectors.norm(x);
    }

    /** {@inheritDoc} */
    @Override
    public double normSq() {
        return Vectors.normSq(x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D withNorm(double magnitude) {
        getCheckedNorm(); // validate our norm value
        return (x > 0.0)? new Vector1D(magnitude) : new Vector1D(-magnitude);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D add(Vector1D v) {
        return new Vector1D(x + v.x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D add(double factor, Vector1D v) {
        return new Vector1D(x + (factor * v.x));
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D subtract(Vector1D v) {
        return new Vector1D(x - v.x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D subtract(double factor, Vector1D v) {
        return new Vector1D(x - (factor * v.x));
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D negate() {
        return new Vector1D(-x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D normalize() {
        return normalize(x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D multiply(double a) {
        return new Vector1D(a * x);
    }

    /** {@inheritDoc} */
    @Override
    public double distance(Vector1D v) {
        return Vectors.norm(x - v.x);
    }

    /** {@inheritDoc} */
    @Override
    public double distanceSq(Vector1D v) {
        return Vectors.normSq(x - v.x);
    }

    /** {@inheritDoc} */
    @Override
    public double dot(Vector1D v) {
        return x * v.x;
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

        final double sig1 = Math.signum(x);
        final double sig2 = Math.signum(v.x);

        // the angle is 0 if the x value signs are the same and pi if not
        return (sig1 == sig2) ? 0.0 : Geometry.PI;
    }

    /** Apply the given transform to this vector, returning the result as a
     * new vector instance.
     * @param transform the transform to apply
     * @return a new, transformed vector
     * @see AffineTransformMatrix1D#apply(Vector1D)
     */
    public Vector1D transform(AffineTransformMatrix1D transform) {
        return transform.apply(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Vector1D vec, final DoublePrecisionContext precision) {
        return precision.eq(x, vec.x);
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
        return 403 * Double.hashCode(x);
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

            return x == rhs.x;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(x);
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
     * corresponding scale factors.
     * </p>
     *
     * @param a scale factor for first coordinate
     * @param c first coordinate
     * @return vector with coordinates calculated by {@code a * c}
     */
    public static Vector1D linearCombination(double a, Vector1D c) {
        return new Vector1D(a * c.x);
    }

    /** Returns a vector consisting of the linear combination of the inputs.
     * <p>
     * A linear combination is the sum of all of the inputs multiplied by their
     * corresponding scale factors.
     * </p>
     *
     * @param a1 scale factor for first coordinate
     * @param v1 first coordinate
     * @param a2 scale factor for second coordinate
     * @param v2 second coordinate
     * @return vector with coordinates calculated by {@code (a1 * v1) + (a2 * v2)}
     */
    public static Vector1D linearCombination(double a1, Vector1D v1, double a2, Vector1D v2) {
        return new Vector1D(
                LinearCombination.value(a1, v1.x, a2, v2.x));
    }

    /** Returns a vector consisting of the linear combination of the inputs.
     * <p>
     * A linear combination is the sum of all of the inputs multiplied by their
     * corresponding scale factors.
     * </p>
     *
     * @param a1 scale factor for first coordinate
     * @param v1 first coordinate
     * @param a2 scale factor for second coordinate
     * @param v2 second coordinate
     * @param a3 scale factor for third coordinate
     * @param v3 third coordinate
     * @return vector with coordinates calculated by {@code (a1 * v1) + (a2 * v2) + (a3 * v3)}
     */
    public static Vector1D linearCombination(double a1, Vector1D v1, double a2, Vector1D v2,
            double a3, Vector1D v3) {
        return new Vector1D(
                LinearCombination.value(a1, v1.x, a2, v2.x, a3, v3.x));
    }

    /** Returns a vector consisting of the linear combination of the inputs.
     * <p>
     * A linear combination is the sum of all of the inputs multiplied by their
     * corresponding scale factors.
     * </p>
     *
     * @param a1 scale factor for first coordinate
     * @param v1 first coordinate
     * @param a2 scale factor for second coordinate
     * @param v2 second coordinate
     * @param a3 scale factor for third coordinate
     * @param v3 third coordinate
     * @param a4 scale factor for fourth coordinate
     * @param v4 fourth coordinate
     * @return point with coordinates calculated by {@code (a1 * v1) + (a2 * v2) + (a3 * v3) + (a4 * v4)}
     */
    public static Vector1D linearCombination(double a1, Vector1D v1, double a2, Vector1D v2,
            double a3, Vector1D v3, double a4, Vector1D v4) {
        return new Vector1D(
                LinearCombination.value(a1, v1.x, a2, v2.x, a3, v3.x, a4, v4.x));
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
        public double norm() {
            return 1;
        }

        /** {@inheritDoc} */
        @Override
        public Vector1D normalize() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Vector1D withNorm(final double mag) {
            return multiply(mag);
        }
    }
}
