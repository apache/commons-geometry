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

import org.apache.commons.geometry.core.util.Coordinates;
import org.apache.commons.geometry.core.util.SimpleCoordinateFormat;
import org.apache.commons.geometry.euclidean.EuclideanVector;
import org.apache.commons.numbers.arrays.LinearCombination;

/** This class represents a vector in three-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public final class Vector3D extends Cartesian3D implements EuclideanVector<Point3D, Vector3D> {

    /** Zero (null) vector (coordinates: 0, 0, 0). */
    public static final Vector3D ZERO   = Vector3D.of(0, 0, 0);

    /** First canonical vector (coordinates: 1, 0, 0). */
    public static final Vector3D PLUS_X = Vector3D.of(1, 0, 0);

    /** Opposite of the first canonical vector (coordinates: -1, 0, 0). */
    public static final Vector3D MINUS_X = Vector3D.of(-1, 0, 0);

    /** Second canonical vector (coordinates: 0, 1, 0). */
    public static final Vector3D PLUS_Y = Vector3D.of(0, 1, 0);

    /** Opposite of the second canonical vector (coordinates: 0, -1, 0). */
    public static final Vector3D MINUS_Y = Vector3D.of(0, -1, 0);

    /** Third canonical vector (coordinates: 0, 0, 1). */
    public static final Vector3D PLUS_Z = Vector3D.of(0, 0, 1);

    /** Opposite of the third canonical vector (coordinates: 0, 0, -1).  */
    public static final Vector3D MINUS_Z = Vector3D.of(0, 0, -1);

 // CHECKSTYLE: stop ConstantName
    /** A vector with all coordinates set to NaN. */
    public static final Vector3D NaN = Vector3D.of(Double.NaN, Double.NaN, Double.NaN);
    // CHECKSTYLE: resume ConstantName

    /** A vector with all coordinates set to positive infinity. */
    public static final Vector3D POSITIVE_INFINITY =
        Vector3D.of(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /** A vector with all coordinates set to negative infinity. */
    public static final Vector3D NEGATIVE_INFINITY =
        Vector3D.of(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    /** Serializable UID */
    private static final long serialVersionUID = 3695385854431542858L;

    /** Error message when norms are zero. */
    private static final String ZERO_NORM_MSG = "Norm is zero";

    /** Factory for delegating instance creation. */
    private static Coordinates.Factory3D<Vector3D> FACTORY = new Coordinates.Factory3D<Vector3D>() {

        /** {@inheritDoc} */
        @Override
        public Vector3D create(double a1, double a2, double a3) {
            return new Vector3D(a1, a2, a3);
        }
    };

    /** Simple constructor.
     * Build a vector from its coordinates
     * @param x abscissa
     * @param y ordinate
     * @param z height
     */
    private Vector3D(double x, double y, double z) {
        super(x, y, z);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getZero() {
        return ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public Point3D asPoint() {
        return Point3D.of(getX(), getY(), getZ());
    }

    /** {@inheritDoc} */
    @Override
    public double getNorm1() {
        return Math.abs(getX()) + Math.abs(getY()) + Math.abs(getZ());
    }

    /** {@inheritDoc} */
    @Override
    public double getNorm() {
        // there are no cancellation problems here, so we use the straightforward formula
        final double x = getX();
        final double y = getY();
        final double z = getZ();
        return Math.sqrt ((x * x) + (y * y) + (z * z));
    }

    /** {@inheritDoc} */
    @Override
    public double getNormSq() {
        // there are no cancellation problems here, so we use the straightforward formula
        final double x = getX();
        final double y = getY();
        final double z = getZ();
        return (x * x) + (y * y) + (z * z);
    }

    /** {@inheritDoc} */
    @Override
    public double getNormInf() {
        return Math.max(Math.max(Math.abs(getX()), Math.abs(getY())), Math.abs(getZ()));
    }

    /** Get the azimuth of the vector.
     * @return azimuth (&alpha;) of the vector, between -&pi; and +&pi;
     */
    public double getAlpha() {
        return Math.atan2(getY(), getX());
    }

    /** Get the elevation of the vector.
     * @return elevation (&delta;) of the vector, between -&pi;/2 and +&pi;/2
     */
    public double getDelta() {
        return Math.asin(getZ() / getNorm());
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D add(Vector3D v) {
        return Vector3D.of(
                    getX() + v.getX(),
                    getY() + v.getY(),
                    getZ() + v.getZ()
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D add(double factor, Vector3D v) {
        return Vector3D.of(
                    getX() + (factor * v.getX()),
                    getY() + (factor * v.getY()),
                    getZ() + (factor * v.getZ())
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D subtract(Vector3D v) {
        return Vector3D.of(
                    getX() - v.getX(),
                    getY() - v.getY(),
                    getZ() - v.getZ()
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D subtract(double factor, Vector3D v) {
        return Vector3D.of(
                    getX() - (factor * v.getX()),
                    getY() - (factor * v.getY()),
                    getZ() - (factor * v.getZ())
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D negate() {
        return Vector3D.of(-getX(), -getY(), -getZ());
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D normalize() throws IllegalStateException {
        double s = getNorm();
        if (s == 0) {
            throw new IllegalStateException(ZERO_NORM_MSG);
        }
        return scalarMultiply(1 / s);
    }

    /** Get a vector orthogonal to the instance.
     * <p>There are an infinite number of normalized vectors orthogonal
     * to the instance. This method picks up one of them almost
     * arbitrarily. It is useful when one needs to compute a reference
     * frame with one of the axes in a predefined direction. The
     * following example shows how to build a frame having the k axis
     * aligned with the known vector u :
     * <pre><code>
     *   Vector3D k = u.normalize();
     *   Vector3D i = k.orthogonal();
     *   Vector3D j = Vector3D.crossProduct(k, i);
     * </code></pre>
     * @return a new normalized vector orthogonal to the instance
     * @exception IllegalStateException if the norm of the instance is zero
     */
    public Vector3D orthogonal() throws IllegalStateException {
        double threshold = 0.6 * getNorm();
        if (threshold == 0) {
            throw new IllegalStateException(ZERO_NORM_MSG);
        }

        final double x = getX();
        final double y = getY();
        final double z = getZ();

        if (Math.abs(x) <= threshold) {
            double inverse  = 1 / Math.sqrt(y * y + z * z);
            return Vector3D.of(0, inverse * z, -inverse * y);
        } else if (Math.abs(y) <= threshold) {
            double inverse  = 1 / Math.sqrt(x * x + z * z);
            return Vector3D.of(-inverse * z, 0, inverse * x);
        }
        double inverse  = 1 / Math.sqrt(x * x + y * y);
        return Vector3D.of(inverse * y, -inverse * x, 0);
    }

    /** Compute the angular separation between two vectors.
     * <p>This method computes the angular separation between two
     * vectors using the dot product for well separated vectors and the
     * cross product for almost aligned vectors. This allows to have a
     * good accuracy in all cases, even for vectors very close to each
     * other.</p>
     * @param v other vector
     * @return angular separation between this instance and v
     * @exception IllegalStateException if either vector has a zero norm
     */
    public double angle(Vector3D v) throws IllegalStateException {
        double normProduct = getNorm() * v.getNorm();
        if (normProduct == 0) {
            throw new IllegalStateException(ZERO_NORM_MSG);
        }

        double dot = dotProduct(v);
        double threshold = normProduct * 0.9999;
        if ((dot < -threshold) || (dot > threshold)) {
            // the vectors are almost aligned, compute using the sine
            Vector3D cross = crossProduct(v);
            if (dot >= 0) {
                return Math.asin(cross.getNorm() / normProduct);
            }
            return Math.PI - Math.asin(cross.getNorm() / normProduct);
        }

        // the vectors are sufficiently separated to use the cosine
        return Math.acos(dot / normProduct);
    }

    /** Compute the cross-product of the instance with another vector.
     * @param v other vector
     * @return the cross product this ^ v as a new Cartesian3D
     */
    public Vector3D crossProduct(final Vector3D v) {
        return Vector3D.of(LinearCombination.value(getY(), v.getZ(), -getZ(), v.getY()),
                            LinearCombination.value(getZ(), v.getX(), -getX(), v.getZ()),
                            LinearCombination.value(getX(), v.getY(), -getY(), v.getX()));
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D scalarMultiply(double a) {
        return Vector3D.of(a * getX(), a * getY(), a * getZ());
    }

    /** {@inheritDoc} */
    @Override
    public double distance1(Vector3D v) {
        double dx = Math.abs(v.getX() - getX());
        double dy = Math.abs(v.getY() - getY());
        double dz = Math.abs(v.getZ() - getZ());

        return dx + dy + dz;
    }

    /** {@inheritDoc} */
    @Override
    public double distance(Vector3D v) {
        return euclideanDistance(v);
    }

    /** {@inheritDoc} */
    @Override
    public double distanceInf(Vector3D v) {
        double dx = Math.abs(v.getX() - getX());
        double dy = Math.abs(v.getY() - getY());
        double dz = Math.abs(v.getZ() - getZ());

        return Math.max(Math.max(dx, dy), dz);
    }

    /** {@inheritDoc} */
    @Override
    public double distanceSq(Vector3D v) {
        double dx = v.getX() - getX();
        double dy = v.getY() - getY();
        double dz = v.getZ() - getZ();

        return (dx * dx) + (dy * dy) + (dz * dz);
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
    public double dotProduct(Vector3D v) {
        return LinearCombination.value(getX(), v.getX(), getY(), v.getY(), getZ(), v.getZ());
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
        return 643 * (164 * Double.hashCode(getX()) +  3 * Double.hashCode(getY()) +  Double.hashCode(getZ()));
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

            return (getX() == rhs.getX()) && (getY() == rhs.getY()) && (getZ() == rhs.getZ());
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleCoordinateFormat.getVectorFormat().format(getX(), getY(), getZ());
    }

    /** Computes the dot product between to vectors. This method simply
     * calls {@code v1.dotProduct(v2)}.
     * @param v1 first vector
     * @param v2 second vector
     * @return the dot product
     * @see #dotProduct(Vector3D)
     */
    public static double dotProduct(Vector3D v1, Vector3D v2) {
        return v1.dotProduct(v2);
    }

    /** Computes the angle in radians between two vectors. This method
     * simply calls {@code v1.angle(v2)}.
     * @param v1 first vector
     * @param v2 second vector
     * @return the angle between the vectors in radians
     * @see #angle(Vector3D)
     */
    public static double angle(Vector3D v1, Vector3D v2) {
        return v1.angle(v2);
    }

    /** Computes the cross product between two vectors. This method simply
     * calls {@code v1.crossProduct(v2)}.
     * @param v1 first vector
     * @param v2 second vector
     * @return the computed cross product vector
     * @see #crossProduct(Vector3D)
     */
    public static Vector3D crossProduct(Vector3D v1, Vector3D v2) {
        return v1.crossProduct(v2);
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

    /** Returns a vector instance with the given coordinate values.
     * @param value vector coordinates
     * @return vector instance
     */
    public static Vector3D of(Cartesian3D value) {
        return new Vector3D(value.getX(), value.getY(), value.getZ());
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

    /** Builds a vector from its azimuthal coordinates
     * @param alpha azimuth (&alpha;) around Z
     *              (0 is +X, &pi;/2 is +Y, &pi; is -X and 3&pi;/2 is -Y)
     * @param delta elevation (&delta;) above (XY) plane, from -&pi;/2 to +&pi;/2
     * @see #getAlpha()
     * @see #getDelta()
     * @return new vector instance with the given azimuthal coordinates
     */
    public static Vector3D fromSpherical(double alpha, double delta) {
        double cosDelta = Math.cos(delta);
        double x = Math.cos(alpha) * cosDelta;
        double y = Math.sin(alpha) * cosDelta;
        double z = Math.sin(delta);

        return new Vector3D(x, y, z);
    }

    /** Parses the given string and returns a new vector instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return vector instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Vector3D parse(String str) throws IllegalArgumentException {
        return SimpleCoordinateFormat.getVectorFormat().parse(str, FACTORY);
    }

    /** Returns a factory object that can be used to created new vector instances.
     * @return vector factory instance
     */
    public static Coordinates.Factory3D<Vector3D> getFactory() {
        return FACTORY;
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
    public static Vector3D linearCombination(double a, Cartesian3D c) {
        return new Vector3D(a * c.getX(), a * c.getY(), a * c.getZ());
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
    public static Vector3D linearCombination(double a1, Cartesian3D c1, double a2, Cartesian3D c2) {
        return new Vector3D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY()),
                LinearCombination.value(a1, c1.getZ(), a2, c2.getZ()));
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
    public static Vector3D linearCombination(double a1, Cartesian3D c1, double a2, Cartesian3D c2,
            double a3, Cartesian3D c3) {
        return new Vector3D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY(), a3, c3.getY()),
                LinearCombination.value(a1, c1.getZ(), a2, c2.getZ(), a3, c3.getZ()));
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
    public static Vector3D linearCombination(double a1, Cartesian3D c1, double a2, Cartesian3D c2,
            double a3, Cartesian3D c3, double a4, Cartesian3D c4) {
        return new Vector3D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX(), a4, c4.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY(), a3, c3.getY(), a4, c4.getY()),
                LinearCombination.value(a1, c1.getZ(), a2, c2.getZ(), a3, c3.getZ(), a4, c4.getZ()));
    }
}
