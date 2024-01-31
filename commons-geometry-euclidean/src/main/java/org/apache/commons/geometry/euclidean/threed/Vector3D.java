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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.UnaryOperator;

import org.apache.commons.geometry.core.internal.DoubleFunction3N;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.EuclideanVectorSum;
import org.apache.commons.geometry.euclidean.MultiDimensionalEuclideanVector;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.numbers.core.Precision;

/** This class represents vectors and points in three-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public class Vector3D extends MultiDimensionalEuclideanVector<Vector3D> {

    /** Zero (null) vector (coordinates: 0, 0, 0). */
    public static final Vector3D ZERO = new Vector3D(0, 0, 0);

    /** A vector with all coordinates set to NaN. */
    public static final Vector3D NaN = new Vector3D(Double.NaN, Double.NaN, Double.NaN);

    /** A vector with all coordinates set to positive infinity. */
    public static final Vector3D POSITIVE_INFINITY =
        new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /** A vector with all coordinates set to negative infinity. */
    public static final Vector3D NEGATIVE_INFINITY =
        new Vector3D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    /** Comparator that sorts vectors in component-wise ascending order.
     * Vectors are only considered equal if their coordinates match exactly.
     * Null arguments are evaluated as being greater than non-null arguments.
     */
    public static final Comparator<Vector3D> COORDINATE_ASCENDING_ORDER = (a, b) -> {
        int cmp = 0;

        if (a != null && b != null) {
            cmp = Double.compare(a.getX(), b.getX());
            if (cmp == 0) {
                cmp = Double.compare(a.getY(), b.getY());
                if (cmp == 0) {
                    cmp = Double.compare(a.getZ(), b.getZ());
                }
            }
        } else if (a != null) {
            cmp = -1;
        } else if (b != null) {
            cmp = 1;
        }

        return cmp;
    };

    /** X coordinate value (abscissa). */
    private final double x;

    /** Y coordinate value (ordinate). */
    private final double y;

    /** Z coordinate value (height). */
    private final double z;

    /** Simple constructor.
     * Build a vector from its coordinates
     * @param x x coordinate value
     * @param y y coordinate value
     * @param z z coordinate value
     */
    private Vector3D(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Return the x coordinate value (abscissa) of the instance.
     * @return the x coordinate value
     */
    public double getX() {
        return x;
    }

    /** Return the y coordinate value (ordinate) of the instance.
     * @return the y coordinate value
     */
    public double getY() {
        return y;
    }

    /** Returns the z coordinate value (height) of the instance.
     * @return the z coordinate value
     */
    public double getZ() {
        return z;
    }

    /** Get the coordinates for this instance as a dimension 3 array.
     * @return the coordinates for this instance
     */
    public double[] toArray() {
        return new double[]{x, y, z};
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return 3;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNaN() {
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && (Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinite() {
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getZero() {
        return ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D vectorTo(final Vector3D v) {
        return v.subtract(this);
    }

    /** {@inheritDoc} */
    @Override
    public Unit directionTo(final Vector3D v) {
        return vectorTo(v).normalize();
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D lerp(final Vector3D p, final double t) {
        return Sum.create()
                .addScaled(1.0 - t, this)
                .addScaled(t, p).get();
    }

    /** {@inheritDoc} */
    @Override
    public double norm() {
        return Vectors.norm(x, y, z);
    }

    /** {@inheritDoc} */
    @Override
    public double normSq() {
        return Vectors.normSq(x, y, z);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D withNorm(final double magnitude) {
        final double m = magnitude / getCheckedNorm();

        return new Vector3D(
                    m * x,
                    m * y,
                    m * z
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D add(final Vector3D v) {
        return new Vector3D(
                    x + v.x,
                    y + v.y,
                    z + v.z
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D add(final double factor, final Vector3D v) {
        return new Vector3D(
                    x + (factor * v.x),
                    y + (factor * v.y),
                    z + (factor * v.z)
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D subtract(final Vector3D v) {
        return new Vector3D(
                    x - v.x,
                    y - v.y,
                    z - v.z
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D subtract(final double factor, final Vector3D v) {
        return new Vector3D(
                    x - (factor * v.x),
                    y - (factor * v.y),
                    z - (factor * v.z)
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D negate() {
        return new Vector3D(-x, -y, -z);
    }

    /** {@inheritDoc} */
    @Override
    public Unit normalize() {
        return Unit.from(x, y, z);
    }

    /** {@inheritDoc} */
    @Override
    public Unit normalizeOrNull() {
        return Unit.tryCreateNormalized(x, y, z, false);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D multiply(final double a) {
        return new Vector3D(a * x, a * y, a * z);
    }

    /** {@inheritDoc} */
    @Override
    public double distance(final Vector3D v) {
        return Vectors.norm(
                x - v.x,
                y - v.y,
                z - v.z
            );
    }

    /** {@inheritDoc} */
    @Override
    public double distanceSq(final Vector3D v) {
        return Vectors.normSq(
                x - v.x,
                y - v.y,
                z - v.z
            );
    }

    /** {@inheritDoc}
     * <p>
     * The implementation uses specific multiplication and addition
     * algorithms to preserve accuracy and reduce cancellation effects.
     * It should be very accurate even for nearly orthogonal vectors.
     * </p>
     * @see org.apache.commons.numbers.core.Sum
     */
    @Override
    public double dot(final Vector3D v) {
        return Vectors.linearCombination(
                x, v.x,
                y, v.y,
                z, v.z);
    }

    /** {@inheritDoc}
     * <p>This method computes the angular separation between two
     * vectors using the dot product for well separated vectors and the
     * cross product for almost aligned vectors. This allows to have a
     * good accuracy in all cases, even for vectors very close to each
     * other.</p>
     */
    @Override
    public double angle(final Vector3D v) {
        final double normProduct = getCheckedNorm() * v.getCheckedNorm();

        final double dot = dot(v);
        final double threshold = normProduct * 0.99;
        if ((dot < -threshold) || (dot > threshold)) {
            // the vectors are almost aligned, compute using the sine
            final Vector3D cross = cross(v);
            if (dot >= 0) {
                return Math.asin(cross.norm() / normProduct);
            }
            return Math.PI - Math.asin(cross.norm() / normProduct);
        }

        // the vectors are sufficiently separated to use the cosine
        return Math.acos(dot / normProduct);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D project(final Vector3D base) {
        return getComponent(base, false, Vector3D::new);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D reject(final Vector3D base) {
        return getComponent(base, true, Vector3D::new);
    }

    /** {@inheritDoc}
     * <p>There are an infinite number of normalized vectors orthogonal
     * to the instance. This method picks up one of them almost
     * arbitrarily. It is useful when one needs to compute a reference
     * frame with one of the axes in a predefined direction. The
     * following example shows how to build a frame having the k axis
     * aligned with the known vector u :
     *
     * <pre><code>
     *   Vector3D k = u.normalize();
     *   Vector3D i = k.orthogonal();
     *   Vector3D j = k.cross(i);
     * </code></pre>
     * @return a unit vector orthogonal to the instance
     * @throws IllegalArgumentException if the norm of the instance
     *      is zero, NaN, or infinite
     */
    @Override
    public Vector3D.Unit orthogonal() {
        final double threshold = 0.6 * getCheckedNorm();

        final double inverse;
        if (Math.abs(x) <= threshold) {
            inverse  = 1 / Vectors.norm(y, z);
            return new Unit(0, inverse * z, -inverse * y);
        } else if (Math.abs(y) <= threshold) {
            inverse  = 1 / Vectors.norm(x, z);
            return new Unit(-inverse * z, 0, inverse * x);
        }
        inverse  = 1 / Vectors.norm(x, y);
        return new Unit(inverse * y, -inverse * x, 0);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D.Unit orthogonal(final Vector3D dir) {
        return dir.getComponent(this, true, Vector3D.Unit::from);
    }

    /** Compute the cross-product of the instance with another vector.
     * @param v other vector
     * @return the cross product this ^ v as a new Vector3D
     */
    public Vector3D cross(final Vector3D v) {
        return new Vector3D(Vectors.linearCombination(y, v.z, -z, v.y),
                            Vectors.linearCombination(z, v.x, -x, v.z),
                            Vectors.linearCombination(x, v.y, -y, v.x));
    }

    /** Convenience method to apply a function to this vector. This
     * can be used to transform the vector inline with other methods.
     * @param fn the function to apply
     * @return the transformed vector
     */
    public Vector3D transform(final UnaryOperator<Vector3D> fn) {
        return fn.apply(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean eq(final Vector3D vec, final Precision.DoubleEquivalence precision) {
        return precision.eq(x, vec.x) &&
                precision.eq(y, vec.y) &&
                precision.eq(z, vec.z);
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
            return 642;
        }
        int result = 1;
        result = 31 * result + Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        return result;
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
     * @return true if two Vector3D objects are equal, false if
     *         object is null, not an instance of Vector3D, or
     *         not equal to this Vector3D instance
     *
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Vector3D) {
            final Vector3D rhs = (Vector3D) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return Double.compare(x, rhs.x) == 0 &&
                    Double.compare(y, rhs.y) == 0 &&
                    Double.compare(z, rhs.z) == 0;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleTupleFormat.getDefault().format(x, y, z);
    }

    /** Returns a component of the current instance relative to the given base
     * vector. If {@code reject} is true, the vector rejection is returned; otherwise,
     * the projection is returned.
     * @param base The base vector
     * @param reject If true, the rejection of this instance from {@code base} is
     *      returned. If false, the projection of this instance onto {@code base}
     *      is returned.
     * @param factory factory function used to build the final vector
     * @param <V> Vector implementation type
     * @return The projection or rejection of this instance relative to {@code base},
     *      depending on the value of {@code reject}.
     * @throws IllegalArgumentException if {@code base} has a zero, NaN,
     *      or infinite norm
     */
    private <V extends Vector3D> V getComponent(final Vector3D base, final boolean reject,
                                                final DoubleFunction3N<V> factory) {
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
        final double projZ = scale * base.z;

        if (reject) {
            return factory.apply(x - projX, y - projY, z - projZ);
        }

        return factory.apply(projX, projY, projZ);
    }

    /** Returns a vector with the given coordinate values.
     * @param x x coordinate value
     * @param y y coordinate value
     * @param z z coordinate value
     * @return vector instance
     */
    public static Vector3D of(final double x, final double y, final double z) {
        return new Vector3D(x, y, z);
    }

    /** Creates a vector from the coordinates in the given 3-element array.
     * @param v coordinates array
     * @return new vector
     * @exception IllegalArgumentException if the array does not have 3 elements
     */
    public static Vector3D of(final double[] v) {
        if (v.length != 3) {
            throw new IllegalArgumentException("Dimension mismatch: " + v.length + " != 3");
        }
        return new Vector3D(v[0], v[1], v[2]);
    }

    /** Parses the given string and returns a new vector instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return vector instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Vector3D parse(final String str) {
        return SimpleTupleFormat.getDefault().parse(str, Vector3D::new);
    }

    /** Return a vector containing the maximum component values from all input vectors.
     * @param first first vector
     * @param more additional vectors
     * @return a vector containing the maximum component values from all input vectors
     */
    public static Vector3D max(final Vector3D first, final Vector3D... more) {
        return computeMax(first, Arrays.asList(more).iterator());
    }

    /** Return a vector containing the maximum component values from all input vectors.
     * @param vecs input vectors
     * @return a vector containing the maximum component values from all input vectors
     * @throws IllegalArgumentException if the argument does not contain any vectors
     */
    public static Vector3D max(final Iterable<Vector3D> vecs) {
        final Iterator<Vector3D> it = vecs.iterator();
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
    private static Vector3D computeMax(final Vector3D first, final Iterator<? extends Vector3D> more) {
        double x = first.getX();
        double y = first.getY();
        double z = first.getZ();

        Vector3D vec;
        while (more.hasNext()) {
            vec = more.next();

            x = Math.max(x, vec.getX());
            y = Math.max(y, vec.getY());
            z = Math.max(z, vec.getZ());
        }

        return Vector3D.of(x, y, z);
    }

    /** Return a vector containing the minimum component values from all input vectors.
     * @param first first vector
     * @param more additional vectors
     * @return a vector containing the minimum component values from all input vectors
     */
    public static Vector3D min(final Vector3D first, final Vector3D... more) {
        return computeMin(first, Arrays.asList(more).iterator());
    }

    /** Return a vector containing the minimum component values from all input vectors.
     * @param vecs input vectors
     * @return a vector containing the minimum component values from all input vectors
     * @throws IllegalArgumentException if the argument does not contain any vectors
     */
    public static Vector3D min(final Iterable<Vector3D> vecs) {
        final Iterator<Vector3D> it = vecs.iterator();
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
    private static Vector3D computeMin(final Vector3D first, final Iterator<? extends Vector3D> more) {
        double x = first.getX();
        double y = first.getY();
        double z = first.getZ();

        Vector3D vec;
        while (more.hasNext()) {
            vec = more.next();

            x = Math.min(x, vec.getX());
            y = Math.min(y, vec.getY());
            z = Math.min(z, vec.getZ());
        }

        return Vector3D.of(x, y, z);
    }

    /** Compute the centroid of the given points. The centroid is the arithmetic mean position of a set
     * of points.
     * @param first first point
     * @param more additional points
     * @return the centroid of the given points
     */
    public static Vector3D centroid(final Vector3D first, final Vector3D... more) {
        return computeCentroid(first, Arrays.asList(more).iterator());
    }

    /** Compute the centroid of the given points. The centroid is the arithmetic mean position of a set
     * of points.
     * @param pts the points to compute the centroid of
     * @return the centroid of the given points
     * @throws IllegalArgumentException if the argument contains no points
     */
    public static Vector3D centroid(final Iterable<Vector3D> pts) {
        final Iterator<Vector3D> it = pts.iterator();
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
    private static Vector3D computeCentroid(final Vector3D first, final Iterator<? extends Vector3D> more) {
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
    public static final class Unit extends Vector3D {
        /** Unit vector (coordinates: 1, 0, 0). */
        public static final Unit PLUS_X  = new Unit(1d, 0d, 0d);
        /** Negation of unit vector (coordinates: -1, 0, 0). */
        public static final Unit MINUS_X = new Unit(-1d, 0d, 0d);
        /** Unit vector (coordinates: 0, 1, 0). */
        public static final Unit PLUS_Y  = new Unit(0d, 1d, 0d);
        /** Negation of unit vector (coordinates: 0, -1, 0). */
        public static final Unit MINUS_Y = new Unit(0d, -1d, 0d);
        /** Unit vector (coordinates: 0, 0, 1). */
        public static final Unit PLUS_Z  = new Unit(0d, 0d, 1d);
        /** Negation of unit vector (coordinates: 0, 0, -1). */
        public static final Unit MINUS_Z = new Unit(0d, 0d, -1d);

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
         * @param x x coordinate value
         * @param y x coordinate value
         * @param z x coordinate value
         */
        private Unit(final double x, final double y, final double z) {
            super(x, y, z);
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
        public Vector3D withNorm(final double mag) {
            return multiply(mag);
        }

        /** {@inheritDoc} */
        @Override
        public Unit negate() {
            return new Unit(-getX(), -getY(), -getZ());
        }

        /** Create a normalized vector.
         * @param x Vector coordinate.
         * @param y Vector coordinate.
         * @param z Vector coordinate.
         * @return a vector whose norm is 1.
         * @throws IllegalArgumentException if the norm of the given value is zero, NaN,
         *      or infinite
         */
        public static Unit from(final double x, final double y, final double z) {
            return tryCreateNormalized(x, y, z, true);
        }

        /** Create a normalized vector.
         * @param v Vector.
         * @return a vector whose norm is 1.
         * @throws IllegalArgumentException if the norm of the given value is zero, NaN,
         *      or infinite
         */
        public static Unit from(final Vector3D v) {
            return v instanceof Unit ?
                (Unit) v :
                from(v.getX(), v.getY(), v.getZ());
        }

        /** Attempt to create a normalized vector from the given coordinate values. If {@code throwOnFailure}
         * is true, an exception is thrown if a normalized vector cannot be created. Otherwise, null
         * is returned.
         * @param x x coordinate
         * @param y y coordinate
         * @param z z coordinate
         * @param throwOnFailure if true, an exception will be thrown if a normalized vector cannot be created
         * @return normalized vector or null if one cannot be created and {@code throwOnFailure}
         *      is false
         * @throws IllegalArgumentException if the computed norm is zero, NaN, or infinite
         */
        private static Unit tryCreateNormalized(final double x, final double y, final double z,
                final boolean throwOnFailure) {

            // Compute the inverse norm directly. If the result is a non-zero real number,
            // then we can go ahead and construct the unit vector immediately. If not,
            // we'll do some extra work for edge cases.
            final double norm = Math.sqrt((x * x) + (y * y) + (z * z));
            final double normInv = 1.0 / norm;
            if (Vectors.isRealNonZero(normInv)) {
                return new Unit(
                        x * normInv,
                        y * normInv,
                        z * normInv);
            }

            // Direct computation did not work. Try scaled versions of the coordinates
            // to handle overflow and underflow.
            final double scaledX;
            final double scaledY;
            final double scaledZ;

            final double maxCoord = Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z));
            if (maxCoord > UNSCALED_MAX) {
                scaledX = x * SCALE_DOWN_FACTOR;
                scaledY = y * SCALE_DOWN_FACTOR;
                scaledZ = z * SCALE_DOWN_FACTOR;
            } else {
                scaledX = x * SCALE_UP_FACTOR;
                scaledY = y * SCALE_UP_FACTOR;
                scaledZ = z * SCALE_UP_FACTOR;
            }

            final double scaledNormInv = 1.0 / Math.sqrt(
                    (scaledX * scaledX) +
                    (scaledY * scaledY) +
                    (scaledZ * scaledZ));

            if (Vectors.isRealNonZero(scaledNormInv)) {
                return new Unit(
                        scaledX * scaledNormInv,
                        scaledY * scaledNormInv,
                        scaledZ * scaledNormInv);
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
    public static final class Sum extends EuclideanVectorSum<Vector3D> {
        /** X component sum. */
        private final org.apache.commons.numbers.core.Sum xsum;
        /** Y component sum. */
        private final org.apache.commons.numbers.core.Sum ysum;
        /** Z component sum. */
        private final org.apache.commons.numbers.core.Sum zsum;

        /** Construct a new instance with the given initial value.
         * @param initial initial value
         */
        Sum(final Vector3D initial) {
            this.xsum = org.apache.commons.numbers.core.Sum.of(initial.x);
            this.ysum = org.apache.commons.numbers.core.Sum.of(initial.y);
            this.zsum = org.apache.commons.numbers.core.Sum.of(initial.z);
        }

        /** {@inheritDoc} */
        @Override
        public Sum add(final Vector3D vec) {
            xsum.add(vec.x);
            ysum.add(vec.y);
            zsum.add(vec.z);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Sum addScaled(final double scale, final Vector3D vec) {
            xsum.addProduct(scale, vec.x);
            ysum.addProduct(scale, vec.y);
            zsum.addProduct(scale, vec.z);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3D get() {
            return Vector3D.of(
                    xsum.getAsDouble(),
                    ysum.getAsDouble(),
                    zsum.getAsDouble());
        }

        /** Create a new instance with an initial value set to the {@link Vector3D#ZERO zero vector}.
         * @return new instance set to zero
         */
        public static Sum create() {
            return new Sum(Vector3D.ZERO);
        }

        /** Construct a new instance with an initial value set to the argument.
         * @param initial initial sum value
         * @return new instance
         */
        public static Sum of(final Vector3D initial) {
            return new Sum(initial);
        }

        /** Construct a new instance from multiple values.
         * @param first first vector
         * @param more additional vectors
         * @return new instance
         */
        public static Sum of(final Vector3D first, final Vector3D... more) {
            final Sum s = new Sum(first);
            for (final Vector3D v : more) {
                s.add(v);
            }
            return s;
        }
    }
}
