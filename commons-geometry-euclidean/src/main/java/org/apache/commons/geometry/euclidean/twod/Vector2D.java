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

import org.apache.commons.geometry.core.exception.IllegalNormException;
import org.apache.commons.geometry.core.internal.DoubleFunction2N;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.MultiDimensionalEuclideanVector;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.numbers.arrays.LinearCombination;

/** This class represents vectors and points in two-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public class Vector2D extends MultiDimensionalEuclideanVector<Vector2D> {

    /** Zero vector (coordinates: 0, 0). */
    public static final Vector2D ZERO   = new Vector2D(0, 0);

    /** Unit vector pointing in the direction of the positive x-axis. */
    public static final Vector2D PLUS_X = new UnitVector(1, 0);

    /** Unit vector pointing in the direction of the negative x-axis. */
    public static final Vector2D MINUS_X = new UnitVector(-1, 0);

    /** Unit vector pointing in the direction of the positive y-axis. */
    public static final Vector2D PLUS_Y = new UnitVector(0, 1);

    /** Unit vector pointing in the direction of the negative y-axis. */
    public static final Vector2D MINUS_Y = new UnitVector(0, -1);

    // CHECKSTYLE: stop ConstantName
    /** A vector with all coordinates set to NaN. */
    public static final Vector2D NaN = new Vector2D(Double.NaN, Double.NaN);
    // CHECKSTYLE: resume ConstantName

    /** A vector with all coordinates set to positive infinity. */
    public static final Vector2D POSITIVE_INFINITY =
        new Vector2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /** A vector with all coordinates set to negative infinity. */
    public static final Vector2D NEGATIVE_INFINITY =
        new Vector2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    /** Serializable UID */
    private static final long serialVersionUID = 20180710L;

    /** Abscissa (first coordinate) */
    private final double x;

    /** Ordinate (second coordinate) */
    private final double y;

    /** Simple constructor.
     * @param x abscissa (first coordinate)
     * @param y ordinate (second coordinate)
     */
    private Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Returns the abscissa (first coordinate value) of the instance.
     * @return the abscissa
     */
    public double getX() {
        return x;
    }

    /** Returns the ordinate (second coordinate value) of the instance.
     * @return the ordinate
     */
    public double getY() {
        return y;
    }

    /** Get the coordinates for this instance as a dimension 2 array.
     * @return coordinates for this instance
     */
    public double[] toArray() {
        return new double[] { x, y };
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNaN() {
        return Double.isNaN(x) || Double.isNaN(y);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && (Double.isInfinite(x) || Double.isInfinite(y));
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D vectorTo(Vector2D v) {
        return v.subtract(this);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D directionTo(Vector2D v) {
        return normalize(
                    v.x - x,
                    v.y - y
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D lerp(Vector2D p, double t) {
        return linearCombination(1.0 - t, this, t, p);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getZero() {
        return ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public double norm() {
        return Vectors.norm(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public double normSq() {
        return Vectors.normSq(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D withNorm(double magnitude) {
        final double invNorm = 1.0 / getCheckedNorm();

        return new Vector2D(
                    magnitude * x * invNorm,
                    magnitude * y * invNorm
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D add(Vector2D v) {
        return new Vector2D(x + v.x, y + v.y);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D add(double factor, Vector2D v) {
        return new Vector2D(x + (factor * v.x), y + (factor * v.y));
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D subtract(Vector2D v) {
        return new Vector2D(x - v.x, y - v.y);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D subtract(double factor, Vector2D v) {
        return new Vector2D(x - (factor * v.x), y - (factor * v.y));
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D negate() {
        return new Vector2D(-x, -y);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D normalize() {
        return normalize(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D multiply(double a) {
        return new Vector2D(a * x, a * y);
    }

    /** {@inheritDoc} */
    @Override
    public double distance(Vector2D v) {
        return Vectors.norm(x - v.x, y - v.y);
    }

    /** {@inheritDoc} */
    @Override
    public double distanceSq(Vector2D v) {
        return Vectors.normSq(x - v.x, y - v.y);
    }

    /** {@inheritDoc} */
    @Override
    public double dot(Vector2D v) {
        return LinearCombination.value(x, v.x, y, v.y);
    }

    /** {@inheritDoc}
     * <p>This method computes the angular separation between the two
     * vectors using the dot product for well separated vectors and the
     * cross product for almost aligned vectors. This allows to have a
     * good accuracy in all cases, even for vectors very close to each
     * other.</p>
     */
    @Override
    public double angle(Vector2D v) {
        double normProduct = getCheckedNorm() * v.getCheckedNorm();

        double dot = dot(v);
        double threshold = normProduct * 0.9999;
        if ((dot < -threshold) || (dot > threshold)) {
            // the vectors are almost aligned, compute using the sine
            final double n = Math.abs(LinearCombination.value(x, v.y, -y, v.x));
            if (dot >= 0) {
                return Math.asin(n / normProduct);
            }
            return Math.PI - Math.asin(n / normProduct);
        }

        // the vectors are sufficiently separated to use the cosine
        return Math.acos(dot / normProduct);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D project(Vector2D base) {
        return getComponent(base, false, Vector2D::new);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D reject(Vector2D base) {
        return getComponent(base, true, Vector2D::new);
    }

    /** {@inheritDoc}
     * The returned vector is computed by rotating the current instance {@code pi/2} radians
     * counterclockwise around the origin and normalizing. For example, if this method is
     * called on a vector pointing along the positive x-axis, then a unit vector representing
     * the positive y-axis is returned.
     * @return a unit vector orthogonal to the current instance
     * @throws IllegalNormException if the norm of the current instance is zero, NaN,
     *  or infinite
     */
    @Override
    public Vector2D orthogonal() {
        return normalize(-y, x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D orthogonal(Vector2D dir) {
        return dir.getComponent(this, true, Vector2D::normalize);
    }

    /** Compute the signed area of the parallelogram with sides formed by this instance
     * and the given vector.
     *
     * <p>The parallelogram in question can be visualized by taking the current instance as the
     * first side and placing {@code v} at the end of it to create the second. The other sides
     * are formed by lines parallel to these two vectors. If {@code v} points to the <em>left</em> of
     * the current instance (ie, the parallelogram is wound counter-clockwise), then the
     * returned area is positive. If {@code v} points to the <em>right</em> of the current instance,
     * (ie, the parallelogram is wound clockwise), then the returned area is negative. If
     * the vectors are collinear (ie, they lie on the same line), then 0 is returned. The area of
     * the triangle formed by the two vectors is exactly half of the returned value.
     * @param v vector representing the second side of the constructed parallelogram
     * @return the signed area of the parallelogram formed by this instance and the given vector
     */
    public double signedArea(final Vector2D v) {
        return LinearCombination.value(
                x, v.y,
                -y, v.x);
    }

    /** Apply the given transform to this vector, returning the result as a
     * new vector instance.
     * @param transform the transform to apply
     * @return a new, transformed vector
     * @see AffineTransformMatrix2D#apply(Vector2D)
     */
    public Vector2D transform(AffineTransformMatrix2D transform) {
        return transform.apply(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean eq(final Vector2D vec, final DoublePrecisionContext precision) {
        return precision.eq(x, vec.x) &&
                precision.eq(y, vec.y);
    }

    /**
     * Get a hashCode for the 2D coordinates.
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
        return 122 * (76 * Double.hashCode(x) +  Double.hashCode(y));
    }

    /**
     * Test for the equality of two vector instances.
     * <p>
     * If all coordinates of two vectors are exactly the same, and none are
     * <code>Double.NaN</code>, the two instances are considered to be equal.
     * </p>
     * <p>
     * <code>NaN</code> coordinates are considered to globally affect the vector
     * and be equal to each other - i.e, if either (or all) coordinates of the
     * vector are equal to <code>Double.NaN</code>, the vector is equal to
     * {@link #NaN}.
     * </p>
     *
     * @param other Object to test for equality to this
     * @return true if two Vector2D objects are equal, false if
     *         object is null, not an instance of Vector2D, or
     *         not equal to this Vector2D instance
     *
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Vector2D) {
            final Vector2D rhs = (Vector2D) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return (x == rhs.x) && (y == rhs.y);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(x, y);
    }

    /** Returns a component of the current instance relative to the given base
     * vector. If {@code reject} is true, the vector rejection is returned; otherwise,
     * the projection is returned.
     * @param base The base vector
     * @param reject If true, the rejection of this instance from {@code base} is
     *      returned. If false, the projection of this instance onto {@code base}
     *      is returned.
     * @param factory factory function used to build the final vector
     * @return The projection or rejection of this instance relative to {@code base},
     *      depending on the value of {@code reject}.
     * @throws IllegalNormException if {@code base} has a zero, NaN, or infinite norm
     */
    private Vector2D getComponent(Vector2D base, boolean reject, DoubleFunction2N<Vector2D> factory) {
        final double aDotB = dot(base);

        // We need to check the norm value here to ensure that it's legal. However, we don't
        // want to incur the cost or floating point error of getting the actual norm and then
        // multiplying it again to get the square norm. So, we'll just check the squared norm
        // directly. This will produce the same error result as checking the actual norm since
        // Math.sqrt(0.0) == 0.0, Math.sqrt(Double.NaN) == Double.NaN and
        // Math.sqrt(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY.
        final double baseMagSq = Vectors.checkedNorm(base.normSq());

        final double scale = aDotB / baseMagSq;

        final double projX = scale * base.x;
        final double projY = scale * base.y;

        if (reject) {
            return factory.apply(x - projX, y - projY);
        }

        return factory.apply(projX, projY);
    }

    /** Returns a vector with the given coordinate values.
     * @param x abscissa (first coordinate value)
     * @param y abscissa (second coordinate value)
     * @return vector instance
     */
    public static Vector2D of(double x, double y) {
        return new Vector2D(x, y);
    }

    /** Creates a vector from the coordinates in the given 2-element array.
     * @param v coordinates array
     * @return new vector
     * @exception IllegalArgumentException if the array does not have 2 elements
     */
    public static Vector2D of(double[] v) {
        if (v.length != 2) {
            throw new IllegalArgumentException("Dimension mismatch: " + v.length + " != 2");
        }
        return new Vector2D(v[0], v[1]);
    }

    /** Returns a normalized vector derived from the given values.
     * @param x abscissa (first coordinate value)
     * @param y ordinate (second coordinate value)
     * @return normalized vector instance
     * @throws IllegalNormException if the norm of the given values is zero, NaN, or infinite
     */
    public static Vector2D normalize(final double x, final double y) {
        final double norm = Vectors.checkedNorm(Vectors.norm(x, y));
        final double invNorm = 1.0 / norm;

        return new UnitVector(x * invNorm, y * invNorm);
    }

    /** Parses the given string and returns a new vector instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return vector instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Vector2D parse(String str) {
        return SimpleTupleFormat.getDefault().parse(str, Vector2D::new);
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
    public static Vector2D linearCombination(double a, Vector2D c) {
        return new Vector2D(a * c.x, a * c.y);
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
    public static Vector2D linearCombination(double a1, Vector2D v1, double a2, Vector2D v2) {
        return new Vector2D(
                LinearCombination.value(a1, v1.x, a2, v2.x),
                LinearCombination.value(a1, v1.y, a2, v2.y));
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
    public static Vector2D linearCombination(double a1, Vector2D v1, double a2, Vector2D v2,
            double a3, Vector2D v3) {
        return new Vector2D(
                LinearCombination.value(a1, v1.x, a2, v2.x, a3, v3.x),
                LinearCombination.value(a1, v1.y, a2, v2.y, a3, v3.y));
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
    public static Vector2D linearCombination(double a1, Vector2D v1, double a2, Vector2D v2,
            double a3, Vector2D v3, double a4, Vector2D v4) {
        return new Vector2D(
                LinearCombination.value(a1, v1.x, a2, v2.x, a3, v3.x, a4, v4.x),
                LinearCombination.value(a1, v1.y, a2, v2.y, a3, v3.y, a4, v4.y));
    }

    /** Private class used to represent unit vectors. This allows optimizations to be performed for certain
     * operations.
     */
    private static final class UnitVector extends Vector2D {

        /** Serializable version identifier */
        private static final long serialVersionUID = 20180903L;

        /** Simple constructor. Callers are responsible for ensuring that the given
         * values represent a normalized vector.
         * @param x abscissa (first coordinate value)
         * @param y abscissa (second coordinate value)
         */
        private UnitVector(final double x, final double y) {
            super(x, y);
        }

        /** {@inheritDoc} */
        @Override
        public double norm() {
            return 1;
        }

        /** {@inheritDoc} */
        @Override
        public Vector2D normalize() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Vector2D withNorm(final double mag) {
            return multiply(mag);
        }
    }
}
