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


import org.apache.commons.geometry.core.exception.IllegalNormException;
import org.apache.commons.geometry.core.internal.DoubleFunction3N;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.MultiDimensionalEuclideanVector;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.numbers.arrays.LinearCombination;

/** This class represents vectors and points in three-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public class Vector3D extends MultiDimensionalEuclideanVector<Vector3D> {

    /** Zero (null) vector (coordinates: 0, 0, 0). */
    public static final Vector3D ZERO   = new Vector3D(0, 0, 0);

    /** First canonical vector (coordinates: 1, 0, 0). */
    public static final Vector3D PLUS_X = new UnitVector(1, 0, 0);

    /** Opposite of the first canonical vector (coordinates: -1, 0, 0). */
    public static final Vector3D MINUS_X = new UnitVector(-1, 0, 0);

    /** Second canonical vector (coordinates: 0, 1, 0). */
    public static final Vector3D PLUS_Y = new UnitVector(0, 1, 0);

    /** Opposite of the second canonical vector (coordinates: 0, -1, 0). */
    public static final Vector3D MINUS_Y = new UnitVector(0, -1, 0);

    /** Third canonical vector (coordinates: 0, 0, 1). */
    public static final Vector3D PLUS_Z = new UnitVector(0, 0, 1);

    /** Opposite of the third canonical vector (coordinates: 0, 0, -1).  */
    public static final Vector3D MINUS_Z = new UnitVector(0, 0, -1);

    // CHECKSTYLE: stop ConstantName
    /** A vector with all coordinates set to NaN. */
    public static final Vector3D NaN = new Vector3D(Double.NaN, Double.NaN, Double.NaN);
    // CHECKSTYLE: resume ConstantName

    /** A vector with all coordinates set to positive infinity. */
    public static final Vector3D POSITIVE_INFINITY =
        new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /** A vector with all coordinates set to negative infinity. */
    public static final Vector3D NEGATIVE_INFINITY =
        new Vector3D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    /** Serializable version identifier */
    private static final long serialVersionUID = 20180903L;

    /** Abscissa (first coordinate value) */
    private final double x;

    /** Ordinate (second coordinate value) */
    private final double y;

    /** Height (third coordinate value)*/
    private final double z;

    /** Simple constructor.
     * Build a vector from its coordinates
     * @param x abscissa
     * @param y ordinate
     * @param z height
     */
    private Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Returns the abscissa (first coordinate) value of the instance.
     * @return the abscissa
     */
    public double getX() {
        return x;
    }

    /** Returns the ordinate (second coordinate) value of the instance.
     * @return the ordinate
     */
    public double getY() {
        return y;
    }

    /** Returns the height (third coordinate) value of the instance.
     * @return the height
     */
    public double getZ() {
        return z;
    }

    /** Get the coordinates for this instance as a dimension 3 array.
     * @return the coordinates for this instance
     */
    public double[] toArray() {
        return new double[] { x, y, z };
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
    public Vector3D getZero() {
        return ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D vectorTo(Vector3D v) {
        return v.subtract(this);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D directionTo(Vector3D v) {
        return normalize(
                v.x - x,
                v.y - y,
                v.z - z);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D lerp(Vector3D p, double t) {
        return linearCombination(1.0 - t, this, t, p);
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
    public Vector3D withNorm(double magnitude) {
        final double invNorm = 1.0 / getCheckedNorm();

        return new Vector3D(
                    magnitude * x * invNorm,
                    magnitude * y * invNorm,
                    magnitude * z * invNorm
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D add(Vector3D v) {
        return new Vector3D(
                    x + v.x,
                    y + v.y,
                    z + v.z
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D add(double factor, Vector3D v) {
        return new Vector3D(
                    x + (factor * v.x),
                    y + (factor * v.y),
                    z + (factor * v.z)
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D subtract(Vector3D v) {
        return new Vector3D(
                    x - v.x,
                    y - v.y,
                    z - v.z
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D subtract(double factor, Vector3D v) {
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
    public Vector3D normalize() {
        return normalize(x, y, z);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D multiply(double a) {
        return new Vector3D(a * x, a * y, a * z);
    }

    /** {@inheritDoc} */
    @Override
    public double distance(Vector3D v) {
        return Vectors.norm(
                x - v.x,
                y - v.y,
                z - v.z
            );
    }

    /** {@inheritDoc} */
    @Override
    public double distanceSq(Vector3D v) {
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
     * @see LinearCombination#value(double, double, double, double, double, double)
     */
    @Override
    public double dot(Vector3D v) {
        return LinearCombination.value(x, v.x, y, v.y, z, v.z);
    }

    /** {@inheritDoc}
     * <p>This method computes the angular separation between two
     * vectors using the dot product for well separated vectors and the
     * cross product for almost aligned vectors. This allows to have a
     * good accuracy in all cases, even for vectors very close to each
     * other.</p>
     */
    @Override
    public double angle(Vector3D v) {
        double normProduct = getCheckedNorm() * v.getCheckedNorm();

        double dot = dot(v);
        double threshold = normProduct * 0.99;
        if ((dot < -threshold) || (dot > threshold)) {
            // the vectors are almost aligned, compute using the sine
            Vector3D cross = cross(v);
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
    public Vector3D project(Vector3D base) {
        return getComponent(base, false, Vector3D::new);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D reject(Vector3D base) {
        return getComponent(base, true, Vector3D::new);
    }

    /** {@inheritDoc}
     * <p>There are an infinite number of normalized vectors orthogonal
     * to the instance. This method picks up one of them almost
     * arbitrarily. It is useful when one needs to compute a reference
     * frame with one of the axes in a predefined direction. The
     * following example shows how to build a frame having the k axis
     * aligned with the known vector u :
     * <pre><code>
     *   Vector3D k = u.normalize();
     *   Vector3D i = k.orthogonal();
     *   Vector3D j = k.crossProduct(i);
     * </code></pre>
     * @return a unit vector orthogonal to the instance
     * @throws IllegalNormException if the norm of the instance is zero, NaN,
     *  or infinite
     */
    @Override
    public Vector3D orthogonal() {
        double threshold = 0.6 * getCheckedNorm();

        if (Math.abs(x) <= threshold) {
            double inverse  = 1 / Math.sqrt(y * y + z * z);
            return new Vector3D(0, inverse * z, -inverse * y);
        } else if (Math.abs(y) <= threshold) {
            double inverse  = 1 / Math.sqrt(x * x + z * z);
            return new Vector3D(-inverse * z, 0, inverse * x);
        }
        double inverse  = 1 / Math.sqrt(x * x + y * y);
        return new Vector3D(inverse * y, -inverse * x, 0);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D orthogonal(Vector3D dir) {
        return dir.getComponent(this, true, Vector3D::normalize);
    }

    /** Compute the cross-product of the instance with another vector.
     * @param v other vector
     * @return the cross product this ^ v as a new Vector3D
     */
    public Vector3D cross(final Vector3D v) {
        return new Vector3D(LinearCombination.value(y, v.z, -z, v.y),
                            LinearCombination.value(z, v.x, -x, v.z),
                            LinearCombination.value(x, v.y, -y, v.x));
    }

    /** Apply the given transform to this vector, returning the result as a
     * new vector instance.
     * @param transform the transform to apply
     * @return a new, transformed vector
     * @see AffineTransformMatrix3D#apply(Vector3D)
     */
    public Vector3D transform(AffineTransformMatrix3D transform) {
        return transform.apply(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Vector3D vec, final DoublePrecisionContext precision) {
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
        return 643 * (164 * Double.hashCode(x) +  3 * Double.hashCode(y) +  Double.hashCode(z));
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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Vector3D) {
            final Vector3D rhs = (Vector3D) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return (x == rhs.x) && (y == rhs.y) && (z == rhs.z);
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
     * @return The projection or rejection of this instance relative to {@code base},
     *      depending on the value of {@code reject}.
     * @throws IllegalNormException if {@code base} has a zero, NaN, or infinite norm
     */
    private Vector3D getComponent(Vector3D base, boolean reject, DoubleFunction3N<Vector3D> factory) {
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
     * @param x abscissa (first coordinate value)
     * @param y abscissa (second coordinate value)
     * @param z height (third coordinate value)
     * @return vector instance
     */
    public static Vector3D of(double x, double y, double z) {
        return new Vector3D(x, y, z);
    }

    /** Creates a vector from the coordinates in the given 3-element array.
     * @param v coordinates array
     * @return new vector
     * @exception IllegalArgumentException if the array does not have 3 elements
     */
    public static Vector3D of(double[] v) {
        if (v.length != 3) {
            throw new IllegalArgumentException("Dimension mismatch: " + v.length + " != 3");
        }
        return new Vector3D(v[0], v[1], v[2]);
    }

    /** Returns a normalized vector derived from the given values.
     * @param x abscissa (first coordinate value)
     * @param y ordinate (second coordinate value)
     * @param z height (third coordinate value)
     * @return normalized vector instance
     * @throws IllegalNormException if the norm of the given values is zero, NaN, or infinite
     */
    public static Vector3D normalize(final double x, final double y, final double z) {
        final double norm = Vectors.checkedNorm(Vectors.norm(x, y, z));
        final double invNorm = 1.0 / norm;

        return new UnitVector(x * invNorm, y * invNorm, z * invNorm);
    }

    /** Parses the given string and returns a new vector instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return vector instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Vector3D parse(String str) {
        return SimpleTupleFormat.getDefault().parse(str, Vector3D::new);
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
    public static Vector3D linearCombination(double a, Vector3D c) {
        return c.multiply(a);
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
    public static Vector3D linearCombination(double a1, Vector3D v1, double a2, Vector3D v2) {
        return new Vector3D(
                LinearCombination.value(a1, v1.x, a2, v2.x),
                LinearCombination.value(a1, v1.y, a2, v2.y),
                LinearCombination.value(a1, v1.z, a2, v2.z));
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
    public static Vector3D linearCombination(double a1, Vector3D v1, double a2, Vector3D v2,
            double a3, Vector3D v3) {
        return new Vector3D(
                LinearCombination.value(a1, v1.x, a2, v2.x, a3, v3.x),
                LinearCombination.value(a1, v1.y, a2, v2.y, a3, v3.y),
                LinearCombination.value(a1, v1.z, a2, v2.z, a3, v3.z));
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
    public static Vector3D linearCombination(double a1, Vector3D v1, double a2, Vector3D v2,
            double a3, Vector3D v3, double a4, Vector3D v4) {
        return new Vector3D(
                LinearCombination.value(a1, v1.x, a2, v2.x, a3, v3.x, a4, v4.x),
                LinearCombination.value(a1, v1.y, a2, v2.y, a3, v3.y, a4, v4.y),
                LinearCombination.value(a1, v1.z, a2, v2.z, a3, v3.z, a4, v4.z));
    }

    /** Private class used to represent unit vectors. This allows optimizations to be performed for certain
     * operations.
     */
    private static final class UnitVector extends Vector3D {

        /** Serializable version identifier */
        private static final long serialVersionUID = 20180903L;

        /** Simple constructor. Callers are responsible for ensuring that the given
         * values represent a normalized vector.
         * @param x abscissa (first coordinate value)
         * @param y ordinate (second coordinate value)
         * @param z height (third coordinate value)
         */
        private UnitVector(final double x, final double y, final double z) {
            super(x, y, z);
        }

        /** {@inheritDoc} */
        @Override
        public double norm() {
            return 1;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3D normalize() {
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3D withNorm(final double mag) {
            return multiply(mag);
        }
    }
}
