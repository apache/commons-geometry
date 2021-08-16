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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.UnaryOperator;

import org.apache.commons.geometry.core.internal.DoubleFunction2N;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.EuclideanVectorSum;
import org.apache.commons.geometry.euclidean.MultiDimensionalEuclideanVector;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.numbers.core.Precision;

/** This class represents vectors and points in two-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public class Vector2D extends MultiDimensionalEuclideanVector<Vector2D> {

    /** Zero vector (coordinates: 0, 0). */
    public static final Vector2D ZERO = new Vector2D(0, 0);

    /** A vector with all coordinates set to NaN. */
    public static final Vector2D NaN = new Vector2D(Double.NaN, Double.NaN);

    /** A vector with all coordinates set to positive infinity. */
    public static final Vector2D POSITIVE_INFINITY =
        new Vector2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /** A vector with all coordinates set to negative infinity. */
    public static final Vector2D NEGATIVE_INFINITY =
        new Vector2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    /** Comparator that sorts vectors in component-wise ascending order.
     * Vectors are only considered equal if their coordinates match exactly.
     * Null arguments are evaluated as being greater than non-null arguments.
     */
    public static final Comparator<Vector2D> COORDINATE_ASCENDING_ORDER = (a, b) -> {
        int cmp = 0;

        if (a != null && b != null) {
            cmp = Double.compare(a.getX(), b.getX());
            if (cmp == 0) {
                cmp = Double.compare(a.getY(), b.getY());
            }
        } else if (a != null) {
            cmp = -1;
        } else if (b != null) {
            cmp = 1;
        }

        return cmp;
    };

    /** Abscissa (first coordinate). */
    private final double x;

    /** Ordinate (second coordinate). */
    private final double y;

    /** Simple constructor.
     * @param x abscissa (first coordinate)
     * @param y ordinate (second coordinate)
     */
    private Vector2D(final double x, final double y) {
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
        return new double[]{x, y};
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
    public boolean isFinite() {
        return Double.isFinite(x) && Double.isFinite(y);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D vectorTo(final Vector2D v) {
        return v.subtract(this);
    }

    /** {@inheritDoc} */
    @Override
    public Unit directionTo(final Vector2D v) {
        return vectorTo(v).normalize();
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D lerp(final Vector2D p, final double t) {
        return Sum.create()
                .addScaled(1.0 - t, this)
                .addScaled(t, p).get();
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
    public Vector2D withNorm(final double magnitude) {
        final double invNorm = 1.0 / getCheckedNorm();

        return new Vector2D(
                    magnitude * x * invNorm,
                    magnitude * y * invNorm
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D add(final Vector2D v) {
        return new Vector2D(x + v.x, y + v.y);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D add(final double factor, final Vector2D v) {
        return new Vector2D(x + (factor * v.x), y + (factor * v.y));
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D subtract(final Vector2D v) {
        return new Vector2D(x - v.x, y - v.y);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D subtract(final double factor, final Vector2D v) {
        return new Vector2D(x - (factor * v.x), y - (factor * v.y));
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D negate() {
        return new Vector2D(-x, -y);
    }

    /** {@inheritDoc} */
    @Override
    public Unit normalize() {
        return Unit.from(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public Unit normalizeOrNull() {
        return Unit.tryCreateNormalized(x, y, false);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D multiply(final double a) {
        return new Vector2D(a * x, a * y);
    }

    /** {@inheritDoc} */
    @Override
    public double distance(final Vector2D v) {
        return Vectors.norm(x - v.x, y - v.y);
    }

    /** {@inheritDoc} */
    @Override
    public double distanceSq(final Vector2D v) {
        return Vectors.normSq(x - v.x, y - v.y);
    }

    /** {@inheritDoc} */
    @Override
    public double dot(final Vector2D v) {
        return Vectors.linearCombination(x, v.x, y, v.y);
    }

    /** {@inheritDoc}
     * <p>This method computes the angular separation between the two
     * vectors using the dot product for well separated vectors and the
     * cross product for almost aligned vectors. This allows to have a
     * good accuracy in all cases, even for vectors very close to each
     * other.</p>
     */
    @Override
    public double angle(final Vector2D v) {
        final double normProduct = getCheckedNorm() * v.getCheckedNorm();

        final double dot = dot(v);
        final double threshold = normProduct * 0.9999;
        if ((dot < -threshold) || (dot > threshold)) {
            // the vectors are almost aligned, compute using the sine
            final double n = Math.abs(Vectors.linearCombination(x, v.y, -y, v.x));
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
    public Vector2D project(final Vector2D base) {
        return getComponent(base, false, Vector2D::new);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D reject(final Vector2D base) {
        return getComponent(base, true, Vector2D::new);
    }

    /** {@inheritDoc}
     * The returned vector is computed by rotating the current instance {@code pi/2} radians
     * counterclockwise around the origin and normalizing. For example, if this method is
     * called on a vector pointing along the positive x-axis, then a unit vector representing
     * the positive y-axis is returned.
     * @return a unit vector orthogonal to the current instance
     * @throws IllegalArgumentException if the norm of the current instance is zero, NaN, or infinite
     */
    @Override
    public Vector2D.Unit orthogonal() {
        return Unit.from(-y, x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D.Unit orthogonal(final Vector2D dir) {
        return dir.getComponent(this, true, Vector2D.Unit::from);
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
        return Vectors.linearCombination(
                x, v.y,
                -y, v.x);
    }

    /** Convenience method to apply a function to this vector. This
     * can be used to transform the vector inline with other methods.
     * @param fn the function to apply
     * @return the transformed vector
     */
    public Vector2D transform(final UnaryOperator<Vector2D> fn) {
        return fn.apply(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean eq(final Vector2D vec, final Precision.DoubleEquivalence precision) {
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
        return 122 * (76 * Double.hashCode(x) + Double.hashCode(y));
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
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Vector2D) {
            final Vector2D rhs = (Vector2D) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return Double.compare(x, rhs.x) == 0 &&
                    Double.compare(y, rhs.y) == 0;
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
     * @param <T> Vector implementation type
     * @return The projection or rejection of this instance relative to {@code base},
     *      depending on the value of {@code reject}.
     * @throws IllegalArgumentException if {@code base} has a zero, NaN, or infinite norm
     */
    private <T extends Vector2D> T getComponent(final Vector2D base, final boolean reject,
            final DoubleFunction2N<T> factory) {
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
    public static Vector2D of(final double x, final double y) {
        return new Vector2D(x, y);
    }

    /** Creates a vector from the coordinates in the given 2-element array.
     * @param v coordinates array
     * @return new vector
     * @exception IllegalArgumentException if the array does not have 2 elements
     */
    public static Vector2D of(final double[] v) {
        if (v.length != 2) {
            throw new IllegalArgumentException("Dimension mismatch: " + v.length + " != 2");
        }
        return new Vector2D(v[0], v[1]);
    }

    /** Parses the given string and returns a new vector instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return vector instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Vector2D parse(final String str) {
        return SimpleTupleFormat.getDefault().parse(str, Vector2D::new);
    }

    /** Return a vector containing the maximum component values from all input vectors.
     * @param first first vector
     * @param more additional vectors
     * @return a vector containing the maximum component values from all input vectors
     */
    public static Vector2D max(final Vector2D first, final Vector2D... more) {
        return computeMax(first, Arrays.asList(more).iterator());
    }

    /** Return a vector containing the maximum component values from all input vectors.
     * @param vecs input vectors
     * @return a vector containing the maximum component values from all input vectors
     * @throws IllegalArgumentException if the argument does not contain any vectors
     */
    public static Vector2D max(final Iterable<Vector2D> vecs) {
        final Iterator<Vector2D> it = vecs.iterator();
        if (!it.hasNext()) {
            throw new IllegalArgumentException("Cannot compute vector max: no vectors given");
        }

        return computeMax(it.next(), it);
    }

    /** Internal method for computing a max vector.
     * @param first first vector
     * @param more iterator with additional vectors
     * @return vector containing the maximum component values of all input vectors
     */
    private static Vector2D computeMax(final Vector2D first, final Iterator<? extends Vector2D> more) {
        double x = first.getX();
        double y = first.getY();

        Vector2D vec;
        while (more.hasNext()) {
            vec = more.next();

            x = Math.max(x, vec.getX());
            y = Math.max(y, vec.getY());
        }

        return Vector2D.of(x, y);
    }

    /** Return a vector containing the minimum component values from all input vectors.
     * @param first first vector
     * @param more more vectors
     * @return a vector containing the minimum component values from all input vectors
     */
    public static Vector2D min(final Vector2D first, final Vector2D... more) {
        return computeMin(first, Arrays.asList(more).iterator());
    }

    /** Return a vector containing the minimum component values from all input vectors.
     * @param vecs input vectors
     * @return a vector containing the minimum component values from all input vectors
     * @throws IllegalArgumentException if the argument does not contain any vectors
     */
    public static Vector2D min(final Iterable<Vector2D> vecs) {
        final Iterator<Vector2D> it = vecs.iterator();
        if (!it.hasNext()) {
            throw new IllegalArgumentException("Cannot compute vector min: no vectors given");
        }

        return computeMin(it.next(), it);
    }

    /** Internal method for computing a min vector.
     * @param first first vector
     * @param more iterator with additional vectors
     * @return vector containing the minimum component values of all input vectors
     */
    private static Vector2D computeMin(final Vector2D first, final Iterator<? extends Vector2D> more) {
        double x = first.getX();
        double y = first.getY();

        Vector2D vec;
        while (more.hasNext()) {
            vec = more.next();

            x = Math.min(x, vec.getX());
            y = Math.min(y, vec.getY());
        }

        return Vector2D.of(x, y);
    }

    /** Compute the centroid of the given points. The centroid is the arithmetic mean position of a set
     * of points.
     * @param first first point
     * @param more additional points
     * @return the centroid of the given points
     */
    public static Vector2D centroid(final Vector2D first, final Vector2D... more) {
        return computeCentroid(first, Arrays.asList(more).iterator());
    }

    /** Compute the centroid of the given points. The centroid is the arithmetic mean position of a set
     * of points.
     * @param pts the points to compute the centroid of
     * @return the centroid of the given points
     * @throws IllegalArgumentException if the argument contains no points
     */
    public static Vector2D centroid(final Iterable<Vector2D> pts) {
        final Iterator<Vector2D> it = pts.iterator();
        if (!it.hasNext()) {
            throw new IllegalArgumentException("Cannot compute centroid: no points given");
        }

        return computeCentroid(it.next(), it);
    }

    /** Internal method for computing the centroid of a set of points.
     * @param first first point
     * @param more iterator with additional points
     * @return the centroid of the point set
     */
    private static Vector2D computeCentroid(final Vector2D first, final Iterator<? extends Vector2D> more) {
        final Sum sum = Sum.of(first);
        int count = 1;

        while (more.hasNext()) {
            sum.add(more.next());
            ++count;
        }

        return sum.get().multiply(1.0 / count);
    }

    /**
     * Represents unit vectors.
     * This allows optimizations for certain operations.
     */
    public static final class Unit extends Vector2D {
        /** Unit vector (coordinates: 1, 0). */
        public static final Unit PLUS_X  = new Unit(1d, 0d);
        /** Negation of unit vector (coordinates: -1, 0). */
        public static final Unit MINUS_X = new Unit(-1d, 0d);
        /** Unit vector (coordinates: 0, 1). */
        public static final Unit PLUS_Y  = new Unit(0d, 1d);
        /** Negation of unit vector (coordinates: 0, -1). */
        public static final Unit MINUS_Y = new Unit(0d, -1d);

        /** Maximum coordinate value for computing normalized vectors
         * with raw, unscaled values.
         */
        private static final double UNSCALED_MAX = 0x1.0p+500;

        /** Factor used to scale up coordinate values in order to produce
         * normalized coordinates without overflow or underflow.
         */
        private static final double SCALE_UP_FACTOR = 0x1.0p+600;

        /** Factor used to scale down coordinate values in order to produce
         * normalized coordinates without overflow or underflow.
         */
        private static final double SCALE_DOWN_FACTOR = 0x1.0p-600;

        /** Simple constructor. Callers are responsible for ensuring that the given
         * values represent a normalized vector.
         * @param x abscissa (first coordinate value)
         * @param y abscissa (second coordinate value)
         */
        private Unit(final double x, final double y) {
            super(x, y);
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
        public Unit normalizeOrNull() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Vector2D.Unit orthogonal() {
            return new Unit(-getY(), getX());
        }

        /** {@inheritDoc} */
        @Override
        public Vector2D withNorm(final double mag) {
            return multiply(mag);
        }

        /** {@inheritDoc} */
        @Override
        public Unit negate() {
            return new Unit(-getX(), -getY());
        }

        /** Create a normalized vector.
         * @param x Vector coordinate.
         * @param y Vector coordinate.
         * @return a vector whose norm is 1.
         * @throws IllegalArgumentException if the norm of the given value is zero, NaN,
         *      or infinite
         */
        public static Unit from(final double x, final double y) {
            return tryCreateNormalized(x, y, true);
        }

        /** Create a normalized vector.
         * @param v Vector.
         * @return a vector whose norm is 1.
         * @throws IllegalArgumentException if the norm of the given value is zero, NaN,
         *      or infinite
         */
        public static Unit from(final Vector2D v) {
            return v instanceof Unit ?
                (Unit) v :
                from(v.getX(), v.getY());
        }

        /** Attempt to create a normalized vector from the given coordinate values. If {@code throwOnFailure}
         * is true, an exception is thrown if a normalized vector cannot be created. Otherwise, null
         * is returned.
         * @param x x coordinate
         * @param y y coordinate
         * @param throwOnFailure if true, an exception will be thrown if a normalized vector cannot be created
         * @return normalized vector or null if one cannot be created and {@code throwOnFailure}
         *      is false
         * @throws IllegalArgumentException if the computed norm is zero, NaN, or infinite
         */
        private static Unit tryCreateNormalized(final double x, final double y, final boolean throwOnFailure) {

            // Compute the inverse norm directly. If the result is a non-zero real number,
            // then we can go ahead and construct the unit vector immediately. If not,
            // we'll do some extra work for edge cases.
            final double norm = Vectors.norm(x, y);
            final double normInv = 1.0 / norm;
            if (Vectors.isRealNonZero(normInv)) {
                return new Unit(
                        x * normInv,
                        y * normInv);
            }

            // Direct computation did not work. Try scaled versions of the coordinates
            // to handle overflow and underflow.
            final double scaledX;
            final double scaledY;

            final double maxCoord = Math.max(Math.abs(x), Math.abs(y));
            if (maxCoord > UNSCALED_MAX) {
                scaledX = x * SCALE_DOWN_FACTOR;
                scaledY = y * SCALE_DOWN_FACTOR;
            } else {
                scaledX = x * SCALE_UP_FACTOR;
                scaledY = y * SCALE_UP_FACTOR;
            }

            final double scaledNormInv = 1.0 / Vectors.norm(scaledX, scaledY);

            if (Vectors.isRealNonZero(scaledNormInv)) {
                return new Unit(
                        scaledX * scaledNormInv,
                        scaledY * scaledNormInv);
            } else if (throwOnFailure) {
                throw Vectors.illegalNorm(norm);
            }
            return null;
        }
    }

    /** Class used to create high-accuracy sums of vectors. Each vector component is
     * summed using an instance of {@link org.apache.commons.numbers.core.Sum}.
     *
     * <p>This class is mutable and not thread-safe.
     * @see org.apache.commons.numbers.core.Sum
     */
    public static final class Sum extends EuclideanVectorSum<Vector2D> {
        /** X component sum. */
        private final org.apache.commons.numbers.core.Sum xsum;
        /** Y component sum. */
        private final org.apache.commons.numbers.core.Sum ysum;

        /** Construct a new instance with the given initial value.
         * @param initial initial value
         */
        Sum(final Vector2D initial) {
            this.xsum = org.apache.commons.numbers.core.Sum.of(initial.x);
            this.ysum = org.apache.commons.numbers.core.Sum.of(initial.y);
        }

        /** {@inheritDoc} */
        @Override
        public Sum add(final Vector2D vec) {
            xsum.add(vec.x);
            ysum.add(vec.y);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Sum addScaled(final double scale, final Vector2D vec) {
            xsum.addProduct(scale, vec.x);
            ysum.addProduct(scale, vec.y);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Vector2D get() {
            return Vector2D.of(
                    xsum.getAsDouble(),
                    ysum.getAsDouble());
        }

        /** Create a new instance with an initial value set to the {@link Vector2D#ZERO zero vector}.
         * @return new instance set to zero
         */
        public static Sum create() {
            return new Sum(Vector2D.ZERO);
        }

        /** Construct a new instance with an initial value set to the argument.
         * @param initial initial sum value
         * @return new instance
         */
        public static Sum of(final Vector2D initial) {
            return new Sum(initial);
        }

        /** Construct a new instance from multiple values.
         * @param first first vector
         * @param more additional vectors
         * @return new instance
         */
        public static Sum of(final Vector2D first, final Vector2D... more) {
            final Sum s = new Sum(first);
            for (final Vector2D v : more) {
                s.add(v);
            }
            return s;
        }
    }
}
