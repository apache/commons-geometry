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

import org.apache.commons.geometry.euclidean.EuclideanVector;
import org.apache.commons.numbers.arrays.LinearCombination;

/** This class represents a vector in two-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public final class Vector2D extends Cartesian2D implements EuclideanVector<Point2D, Vector2D> {

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
    private static final long serialVersionUID = 1746839897232305304L;

    /** Error message when norms are zero. */
    private static final String ZERO_NORM_MSG = "Norm is zero";

    /** Simple constructor.
     * @param x abscissa (first coordinate)
     * @param y ordinate (second coordinate)
     */
    public Vector2D(double x, double y) {
        super(x, y);
    }

    /** Get the vector coordinates as a dimension 2 array.
     * @return vector coordinates
     * @see #Cartesian2D(double[])
     */
    @Override
    public double[] toArray() {
        return new double[] { x, y };
    }

    /** {@inheritDoc} */
    @Override
    public Point2D asPoint() {
        return new Point2D(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getZero() {
        return ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public double getNorm1() {
        return Math.abs(x) + Math.abs(y);
    }

    /** {@inheritDoc} */
    @Override
    public double getNorm() {
        return Math.sqrt ((x * x) + (y * y));
    }

    /** {@inheritDoc} */
    @Override
    public double getNormSq() {
        return (x * x) + (y * y);
    }

    /** {@inheritDoc} */
    @Override
    public double getNormInf() {
        return Math.max(Math.abs(x), Math.abs(y));
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
    public Vector2D normalize() throws IllegalStateException {
        double n = getNorm();
        if (n == 0) {
            throw new IllegalStateException(ZERO_NORM_MSG);
        }
        return scalarMultiply(1 / n);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D scalarMultiply(double a) {
        return new Vector2D(a * x, a * y);
    }

    /** {@inheritDoc} */
    @Override
    public double distance1(Vector2D v) {
        double dx = Math.abs(x - v.x);
        double dy = Math.abs(y - v.y);
        return dx + dy;
    }

    /** {@inheritDoc} */
    @Override
    public double distance(Vector2D v) {
        return euclideanDistance(v);
    }

    /** {@inheritDoc} */
    @Override
    public double distanceInf(Vector2D v) {
        double dx = Math.abs(x - v.x);
        double dy = Math.abs(y - v.y);
        return Math.max(dx, dy);
    }

    /** {@inheritDoc} */
    @Override
    public double distanceSq(Vector2D v) {
        double dx = x - v.x;
        double dy = y - v.y;
        return (dx * dx) + (dy * dy);
    }

    /** {@inheritDoc} */
    @Override
    public double dotProduct(Vector2D v) {
        return LinearCombination.value(x, v.x, y, v.y);
    }

    /** Compute the angular separation in radians between this vector
     * and the given vector.
     * <p>This method computes the angular separation between the two
     * vectors using the dot product for well separated vectors and the
     * cross product for almost aligned vectors. This allows to have a
     * good accuracy in all cases, even for vectors very close to each
     * other.</p>
     *
     * @param v vector to compute the angle with
     * @return angular separation between this vector and v in radians
     * @exception IllegalArgumentException if either vector has a zero norm
     */
    public double angle(Vector2D v) throws IllegalArgumentException {
        double normProduct = getNorm() * v.getNorm();
        if (normProduct == 0) {
            throw new IllegalArgumentException(ZERO_NORM_MSG);
        }

        double dot = dotProduct(v);
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
        final double x1 = p2.x - p1.x;
        final double y1 = y - p1.y;
        final double x2 = x - p1.x;
        final double y2 = p2.y - p1.y;
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
        return "{" + x + "; " + y + "}";
    }

    /** Computes the dot product between to vectors. This method simply
     * calls {@code v1.dotProduct(v2)}.
     * @param v1 first vector
     * @param v2 second vector
     * @return the dot product
     * @see {@link #dotProduct(Vector2D)}
     */
    public static double dotProduct(Vector2D v1, Vector2D v2) {
        return v1.dotProduct(v2);
    }

    /** Computes the angle in radians between two vectors. This method
     * simply calls {@code v1.angle(v2)}.
     * @param v1 first vector
     * @param v2 second vector
     * @return the angle between the vectors in radians
     * @see {@link #angle(Vector2D)}
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

    /** Returns a vector instance with the given coordinate values.
     * @param value vector coordinates
     * @return vector instance
     */
    public static Vector2D of(Cartesian2D value) {
        return new Vector2D(value.x, value.y);
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
        return new Vector2D(a * c.x, a * c.y);
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
                LinearCombination.value(a1, c1.x, a2, c2.x),
                LinearCombination.value(a1, c1.y, a2, c2.y));
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
                LinearCombination.value(a1, c1.x, a2, c2.x, a3, c3.x),
                LinearCombination.value(a1, c1.y, a2, c2.y, a3, c3.y));
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
                LinearCombination.value(a1, c1.x, a2, c2.x, a3, c3.x, a4, c4.x),
                LinearCombination.value(a1, c1.y, a2, c2.y, a3, c3.y, a4, c4.y));
    }
}
