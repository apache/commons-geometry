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

import org.apache.commons.geometry.core.MultiDimensionalVector;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.core.util.Vectors;
import org.apache.commons.geometry.euclidean.EuclideanVector;
import org.apache.commons.numbers.arrays.LinearCombination;

/** This class represents a vector in two-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public final class Vector2D extends Cartesian2D implements EuclideanVector<Point2D, Vector2D>, MultiDimensionalVector<Vector2D> {

    /** Zero vector (coordinates: 0, 0). */
    public static final Vector2D ZERO   = new Vector2D(0, 0);

    /** Unit vector pointing in the direction of the positive x-axis. */
    public static final Vector2D PLUS_X = new Vector2D(1, 0);

    /** Unit vector pointing in the direction of the negative x-axis. */
    public static final Vector2D MINUS_X = new Vector2D(-1, 0);

    /** Unit vector pointing in the direction of the positive y-axis. */
    public static final Vector2D PLUS_Y = new Vector2D(0, 1);

    /** Unit vector pointing in the direction of the negative y-axis. */
    public static final Vector2D MINUS_Y = new Vector2D(0, -1);

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

    /** Simple constructor.
     * @param x abscissa (first coordinate)
     * @param y ordinate (second coordinate)
     */
    private Vector2D(double x, double y) {
        super(x, y);
    }

    /** Get the vector coordinates as a dimension 2 array.
     * @return vector coordinates
     */
    @Override
    public double[] toArray() {
        return new double[] { getX(), getY() };
    }

    /** {@inheritDoc} */
    @Override
    public Point2D asPoint() {
        return Point2D.of(getX(), getY());
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
    public double getNorm1() {
        return Vectors.norm1(getX(), getY());
    }

    /** {@inheritDoc} */
    @Override
    public double getNorm() {
        return Vectors.norm(getX(), getY());
    }

    /** {@inheritDoc} */
    @Override
    public double getNormSq() {
        return Vectors.normSq(getX(), getY());
    }

    /** {@inheritDoc} */
    @Override
    public double getNormInf() {
        return Vectors.normInf(getX(), getY());
    }

    /** {@inheritDoc} */
    @Override
    public double getMagnitude() {
        return getNorm();
    }

    /** {@inheritDoc} */
    @Override
    public double getMagnitudeSq() {
        return getNormSq();
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D withMagnitude(double magnitude) {
        final double invNorm = 1.0 / getNonZeroNorm();

        return new Vector2D(
                    magnitude * getX() * invNorm,
                    magnitude * getY() * invNorm
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D add(Vector2D v) {
        return new Vector2D(getX() + v.getX(), getY() + v.getY());
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D add(double factor, Vector2D v) {
        return new Vector2D(getX() + (factor * v.getX()), getY() + (factor * v.getY()));
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D subtract(Vector2D v) {
        return new Vector2D(getX() - v.getX(), getY() - v.getY());
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D subtract(double factor, Vector2D v) {
        return new Vector2D(getX() - (factor * v.getX()), getY() - (factor * v.getY()));
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D negate() {
        return new Vector2D(-getX(), -getY());
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D normalize() throws IllegalStateException {
        return scalarMultiply(1.0 / getNonZeroNorm());
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D scalarMultiply(double a) {
        return new Vector2D(a * getX(), a * getY());
    }

    /** {@inheritDoc} */
    @Override
    public double distance1(Vector2D v) {
        return Vectors.norm1(getX() - v.getX(), getY() - v.getY());
    }

    /** {@inheritDoc} */
    @Override
    public double distance(Vector2D v) {
        return Vectors.norm(getX() - v.getX(), getY() - v.getY());
    }

    /** {@inheritDoc} */
    @Override
    public double distanceInf(Vector2D v) {
        return Vectors.normInf(getX() - v.getX(), getY() - v.getY());
    }

    /** {@inheritDoc} */
    @Override
    public double distanceSq(Vector2D v) {
        return Vectors.normSq(getX() - v.getX(), getY() - v.getY());
    }

    /** {@inheritDoc} */
    @Override
    public double dotProduct(Vector2D v) {
        return LinearCombination.value(getX(), v.getX(), getY(), v.getY());
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D project(Vector2D base) throws IllegalStateException {
        return getComponent(base, false);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D reject(Vector2D base) throws IllegalStateException {
        return getComponent(base, true);
    }

    /** {@inheritDoc}
     * <p>This method computes the angular separation between the two
     * vectors using the dot product for well separated vectors and the
     * cross product for almost aligned vectors. This allows to have a
     * good accuracy in all cases, even for vectors very close to each
     * other.</p>
     */
    @Override
    public double angle(Vector2D v) throws IllegalStateException {
        double normProduct = getNonZeroNorm() * v.getNonZeroNorm();

        double dot = dotProduct(v);
        double threshold = normProduct * 0.9999;
        if ((dot < -threshold) || (dot > threshold)) {
            // the vectors are almost aligned, compute using the sine
            final double n = Math.abs(LinearCombination.value(getX(), v.getY(), -getY(), v.getX()));
            if (dot >= 0) {
                return Math.asin(n / normProduct);
            }
            return Math.PI - Math.asin(n / normProduct);
        }

        // the vectors are sufficiently separated to use the cosine
        return Math.acos(dot / normProduct);
    }

    /**
     * Compute the cross-product of the instance and the given vector.
     * <p>
     * The cross product can be used to determine the location of a point
     * with regard to the line formed by (p1, p2) and is calculated as:
     * \[
     *    P = (x_2 - x_1)(y_3 - y_1) - (y_2 - y_1)(x_3 - x_1)
     * \]
     * with \(p3 = (x_3, y_3)\) being this instance.
     * <p>
     * If the result is 0, the points are collinear, i.e. lie on a single straight line L;
     * if it is positive, this point lies to the left, otherwise to the right of the line
     * formed by (p1, p2).
     *
     * @param p1 first point of the line
     * @param p2 second point of the line
     * @return the cross-product
     *
     * @see <a href="http://mathworld.wolfram.com/CrossProduct.html">Cross product (Mathworld)</a>
     */
    public double crossProduct(final Vector2D p1, final Vector2D p2) {
        final double x1 = p2.getX() - p1.getX();
        final double y1 = getY() - p1.getY();
        final double x2 = getX() - p1.getX();
        final double y2 = p2.getY() - p1.getY();
        return LinearCombination.value(x1, y1, -x2, y2);
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
        return 122 * (76 * Double.hashCode(getX()) +  Double.hashCode(getY()));
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

            return (getX() == rhs.getX()) && (getY() == rhs.getY());
        }
        return false;
    }

    /** Returns the vector norm, throwing an IllegalStateException if the norm is zero.
     * @return the non-zero norm value
     * @throws IllegalStateException if the norm is zero
     */
    private double getNonZeroNorm() throws IllegalStateException {
        final double n = getNorm();
        if (n == 0) {
            throw new IllegalStateException("Norm is zero");
        }

        return n;
    }

    /** Returns a component of the current instance relative to the given base
     * vector. If {@code reject} is true, the vector rejection is returned; otherwise,
     * the projection is returned.
     * @param base The base vector
     * @param reject If true, the rejection of this instance from {@code base} is
     *      returned. If false, the projection of this instance onto {@code base}
     *      is returned.
     * @return The projection or rejection of this instance relative to {@code base},
     *      depending on the value of {@code reject}.
     * @throws IllegalStateException if {@code base} has a zero norm
     */
    private Vector2D getComponent(Vector2D base, boolean reject) throws IllegalStateException {
        final double aDotB = dotProduct(base);

        final double baseMagSq = base.getNormSq();
        if (baseMagSq == 0.0) {
            throw new IllegalStateException("Invalid base vector: norm is zero");
        }

        final double scale = aDotB / baseMagSq;

        final double projX = scale * base.getX();
        final double projY = scale * base.getY();

        if (reject) {
            return new Vector2D(getX() - projX, getY() - projY);
        }

        return new Vector2D(projX, projY);
    }

    /** Computes the dot product between to vectors. This method simply
     * calls {@code v1.dotProduct(v2)}.
     * @param v1 first vector
     * @param v2 second vector
     * @return the dot product
     * @see #dotProduct(Vector2D)
     */
    public static double dotProduct(Vector2D v1, Vector2D v2) {
        return v1.dotProduct(v2);
    }

    /** Projects the given vector onto {@code base}. This method simply
     * calls {@code v.project(base)}.
     * @param v vector to project
     * @param base the base vector to project onto
     * @return the projected vector
     * @see #project(Vector2D)
     */
    public static Vector2D project(Vector2D v, Vector2D base) {
        return v.project(base);
    }

    /** Returns the vector rejection of {@code v} from {@code base}. This
     * method simply calls {@code v.reject(base)}.
     * @param v vector to reject
     * @param base the base vector to reject from
     * @return the vector rejection
     * @see #reject(Vector2D)
     */
    public static Vector2D reject(Vector2D v, Vector2D base) {
        return v.reject(base);
    }

    /** Computes the angle in radians between two vectors. This method
     * simply calls {@code v1.angle(v2)}.
     * @param v1 first vector
     * @param v2 second vector
     * @return the angle between the vectors in radians
     * @see #angle(Vector2D)
     */
    public static double angle(Vector2D v1, Vector2D v2) {
        return v1.angle(v2);
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
    public static Vector2D ofArray(double[] v) {
        if (v.length != 2) {
            throw new IllegalArgumentException("Dimension mismatch: " + v.length + " != 2");
        }
        return new Vector2D(v[0], v[1]);
    }

    /** Return a vector with coordinates equivalent to the given set of polar coordinates.
     * @param radius The polar coordinate radius value.
     * @param azimuth The polar coordinate azimuth angle in radians.
     * @return vector instance with coordinates equivalent to the given polar coordinates.
     */
    public static Vector2D ofPolar(final double radius, final double azimuth) {
        return PolarCoordinates.toCartesian(radius, azimuth, Vector2D::new);
    }

    /** Parses the given string and returns a new vector instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return vector instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Vector2D parse(String str) throws IllegalArgumentException {
        return SimpleTupleFormat.getDefault().parse(str, Vector2D::new);
    }

    /** Linearly interpolates between the two given vectors. This methods simply
     * calls {@code a.lerp(b, t)}.
     * @param a first vector
     * @param b second vector
     * @param t interpolation parameter
     * @return the interpolated vector
     * @see #lerp(Vector2D, double)
     */
    public static Vector2D lerp(Vector2D a, Vector2D b, double t) {
        return a.lerp(b, t);
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
    public static Vector2D linearCombination(double a, Cartesian2D c) {
        return new Vector2D(a * c.getX(), a * c.getY());
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
    public static Vector2D linearCombination(double a1, Cartesian2D c1, double a2, Cartesian2D c2) {
        return new Vector2D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY()));
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
    public static Vector2D linearCombination(double a1, Cartesian2D c1, double a2, Cartesian2D c2,
            double a3, Cartesian2D c3) {
        return new Vector2D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY(), a3, c3.getY()));
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
    public static Vector2D linearCombination(double a1, Cartesian2D c1, double a2, Cartesian2D c2,
            double a3, Cartesian2D c3, double a4, Cartesian2D c4) {
        return new Vector2D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX(), a4, c4.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY(), a3, c3.getY(), a4, c4.getY()));
    }
}
