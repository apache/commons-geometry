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
import java.util.function.UnaryOperator;

import org.apache.commons.numbers.angle.PlaneAngleRadians;
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
     * Vectors are only considered equal if their coordinates match exactly.
     * Null arguments are evaluated as being greater than non-null arguments.
     */
    public static final Comparator<Vector1D> COORDINATE_ASCENDING_ORDER = (a, b) -> {
        int cmp = 0;

        if (a != null && b != null) {
            cmp = Double.compare(a.getX(), b.getX());
        } else if (a != null) {
            cmp = -1;
        } else if (b != null) {
            cmp = 1;
        }

        return cmp;
    };

    /** Abscissa (coordinate value). */
    private final double x;

    /** Simple constructor.
     * @param x abscissa (coordinate value)
     */
    private Vector1D(final double x) {
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
    public boolean isFinite() {
        return Double.isFinite(x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D vectorTo(final Vector1D v) {
        return v.subtract(this);
    }

    /** {@inheritDoc} */
    @Override
    public Unit directionTo(final Vector1D v) {
        return vectorTo(v).normalize();
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D lerp(final Vector1D p, final double t) {
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
    public Vector1D withNorm(final double magnitude) {
        getCheckedNorm(); // validate our norm value
        return (x > 0.0) ? new Vector1D(magnitude) : new Vector1D(-magnitude);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D add(final Vector1D v) {
        return new Vector1D(x + v.x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D add(final double factor, final Vector1D v) {
        return new Vector1D(x + (factor * v.x));
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D subtract(final Vector1D v) {
        return new Vector1D(x - v.x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D subtract(final double factor, final Vector1D v) {
        return new Vector1D(x - (factor * v.x));
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D negate() {
        return new Vector1D(-x);
    }

    /** {@inheritDoc} */
    @Override
    public Unit normalize() {
        return Unit.from(x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D multiply(final double a) {
        return new Vector1D(a * x);
    }

    /** {@inheritDoc} */
    @Override
    public double distance(final Vector1D v) {
        return Vectors.norm(x - v.x);
    }

    /** {@inheritDoc} */
    @Override
    public double distanceSq(final Vector1D v) {
        return Vectors.normSq(x - v.x);
    }

    /** {@inheritDoc} */
    @Override
    public double dot(final Vector1D v) {
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
        return (sig1 == sig2) ? 0.0 : PlaneAngleRadians.PI;
    }

    /** Convenience method to apply a function to this vector. This
     * can be used to transform the vector inline with other methods.
     * @param fn the function to apply
     * @return the transformed vector
     */
    public Vector1D transform(final UnaryOperator<Vector1D> fn) {
        return fn.apply(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean eq(final Vector1D vec, final DoublePrecisionContext precision) {
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
    public boolean equals(final Object other) {

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
    public static Vector1D of(final double x) {
        return new Vector1D(x);
    }

    /** Parses the given string and returns a new vector instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return vector instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Vector1D parse(final String str) {
        return SimpleTupleFormat.getDefault().parse(str, Vector1D::new);
    }

    /** Returns a vector consisting of the linear combination of the inputs.
     * <p>
     * A linear combination is the sum of all of the inputs multiplied by their
     * corresponding scale factors.
     * </p>
     *
     * @param a scale factor for first vector
     * @param c first vector
     * @return vector calculated by {@code a * c}
     */
    public static Vector1D linearCombination(final double a, final Vector1D c) {
        return new Vector1D(a * c.x);
    }

    /** Returns a vector consisting of the linear combination of the inputs.
     * <p>
     * A linear combination is the sum of all of the inputs multiplied by their
     * corresponding scale factors.
     * </p>
     *
     * @param a1 scale factor for first vector
     * @param v1 first vector
     * @param a2 scale factor for second vector
     * @param v2 second vector
     * @return vector calculated by {@code (a1 * v1) + (a2 * v2)}
     */
    public static Vector1D linearCombination(final double a1, final Vector1D v1,
            final double a2, final Vector1D v2) {

        return new Vector1D(
                LinearCombination.value(a1, v1.x, a2, v2.x));
    }

    /** Returns a vector consisting of the linear combination of the inputs.
     * <p>
     * A linear combination is the sum of all of the inputs multiplied by their
     * corresponding scale factors.
     * </p>
     *
     * @param a1 scale factor for first vector
     * @param v1 first vector
     * @param a2 scale factor for second vector
     * @param v2 second vector
     * @param a3 scale factor for third vector
     * @param v3 third vector
     * @return vector calculated by {@code (a1 * v1) + (a2 * v2) + (a3 * v3)}
     */
    public static Vector1D linearCombination(final double a1, final Vector1D v1,
            final double a2, final Vector1D v2,
            final double a3, final Vector1D v3) {

        return new Vector1D(
                LinearCombination.value(a1, v1.x, a2, v2.x, a3, v3.x));
    }

    /** Returns a vector consisting of the linear combination of the inputs.
     * <p>
     * A linear combination is the sum of all of the inputs multiplied by their
     * corresponding scale factors.
     * </p>
     *
     * @param a1 scale factor for first vector
     * @param v1 first vector
     * @param a2 scale factor for second vector
     * @param v2 second vector
     * @param a3 scale factor for third vector
     * @param v3 third vector
     * @param a4 scale factor for fourth vector
     * @param v4 fourth vector
     * @return vector calculated by {@code (a1 * v1) + (a2 * v2) + (a3 * v3) + (a4 * v4)}
     */
    public static Vector1D linearCombination(final double a1, final Vector1D v1,
            final double a2, final Vector1D v2,
            final double a3, final Vector1D v3,
            final double a4, final Vector1D v4) {

        return new Vector1D(
                LinearCombination.value(a1, v1.x, a2, v2.x, a3, v3.x, a4, v4.x));
    }

    /**
     * Represent unit vectors.
     * This allows optimizations to be performed for certain operations.
     */
    public static final class Unit extends Vector1D {
        /** Unit vector (coordinates: 1). */
        public static final Unit PLUS  = new Unit(1d);
        /** Negation of unit vector (coordinates: -1). */
        public static final Unit MINUS = new Unit(-1d);

        /** Simple constructor. Callers are responsible for ensuring that the given
         * values represent a normalized vector.
         * @param x abscissa (first coordinate value)
         */
        private Unit(final double x) {
            super(x);
        }

        /**
         * Creates a normalized vector.
         *
         * @param x Vector coordinate.
         * @return a vector whose norm is 1.
         * @throws org.apache.commons.geometry.core.exception.IllegalNormException if the norm of the given value is
         *      zero, NaN, or infinite
         */
        public static Unit from(double x) {
            Vectors.checkedNorm(Vectors.norm(x));
            return x > 0 ? PLUS : MINUS;
        }

        /**
         * Creates a normalized vector.
         *
         * @param v Vector.
         * @return a vector whose norm is 1.
         * @throws org.apache.commons.geometry.core.exception.IllegalNormException if the norm of the given value is
         *      zero, NaN, or infinite
         */
        public static Unit from(Vector1D v) {
            return v instanceof Unit ?
                (Unit) v :
                from(v.getX());
        }

        /** {@inheritDoc} */
        @Override
        public double norm() {
            return 1;
        }

        /** {@inheritDoc} */
        @Override
        public double normSq() {
            return 1;
        }

        /** {@inheritDoc} */
        @Override
        public Unit normalize() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Vector1D withNorm(final double mag) {
            return multiply(mag);
        }

        /** {@inheritDoc} */
        @Override
        public Vector1D negate() {
            return this == PLUS ? MINUS : PLUS;
        }
    }
}
