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

import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.EuclideanPoint;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.numbers.arrays.LinearCombination;

/** This class represents a point in two-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public final class Point2D extends Cartesian2D implements EuclideanPoint<Point2D, Vector2D> {

    /** Origin (coordinates: 0, 0). */
    public static final Point2D ZERO   = new Point2D(0, 0);

 // CHECKSTYLE: stop ConstantName
    /** A point with all coordinates set to NaN. */
    public static final Point2D NaN = new Point2D(Double.NaN, Double.NaN);
    // CHECKSTYLE: resume ConstantName

    /** A point with all coordinates set to positive infinity. */
    public static final Point2D POSITIVE_INFINITY =
        new Point2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /** A point with all coordinates set to negative infinity. */
    public static final Point2D NEGATIVE_INFINITY =
        new Point2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    /** Serializable UID. */
    private static final long serialVersionUID = 20180710L;

    /** Simple constructor.
     * Build a point from its coordinates
     * @param x abscissa
     * @param y ordinate
     */
    private Point2D(double x, double y) {
        super(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D asVector() {
        return Vector2D.of(getX(), getY());
    }

    /** {@inheritDoc} */
    @Override
    public double distance(Point2D p) {
        return Vectors.norm(getX() - p.getX(), getY() - p.getY());
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D subtract(Point2D p) {
        return Vector2D.of(getX() - p.getX(), getY() - p.getY());
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D vectorTo(Point2D p) {
        return p.subtract(this);
    }

    /** {@inheritDoc} */
    @Override
    public Point2D lerp(Point2D p, double t) {
        return vectorCombination(1.0 - t, this, t, p);
    }

    /** {@inheritDoc} */
    @Override
    public Point2D add(Vector2D v) {
        return new Point2D(getX() + v.getX(), getY() + v.getY());
    }

    /**
     * Get a hashCode for this point.
     * <p>All NaN values have the same hash code.</p>
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

    /** Test for the equality of two points.
     * <p>
     * If all coordinates of two points are exactly the same, and none are
     * <code>Double.NaN</code>, the two points are considered to be equal.
     * </p>
     * <p>
     * <code>NaN</code> coordinates are considered to globally affect the point
     * and be equal to each other - i.e, if either (or all) coordinates of the
     * point are equal to <code>Double.NaN</code>, the point is equal to
     * {@link #NaN}.
     * </p>
     *
     * @param other Object to test for equality to this
     * @return true if two Point2D objects are equal, false if
     *         object is null, not an instance of Point2D, or
     *         not equal to this Point2D instance
     *
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Point2D) {
            final Point2D rhs = (Point2D) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return (getX() == rhs.getX()) && (getY() == rhs.getY());
        }
        return false;
    }

    /** Returns a point with the given coordinate values
     * @param x abscissa (first coordinate value)
     * @param y ordinate (second coordinate value)
     * @return point instance
     */
    public static Point2D of(double x, double y) {
        return new Point2D(x, y);
    }

    /** Returns a point with the coordinates from the given 2-element array.
     * @param p coordinates array
     * @return new point
     * @exception IllegalArgumentException if the array does not have 2 elements
     */
    public static Point2D ofArray(double[] p) {
        if (p.length != 2) {
            throw new IllegalArgumentException("Dimension mismatch: " + p.length + " != 2");
        }
        return new Point2D(p[0], p[1]);
    }

    /**Return a point with coordinates equivalent to the given set of polar coordinates.
     * @param radius The polar coordinate radius value.
     * @param azimuth The polar coordinate azimuth angle in radians.
     * @return point instance with coordinates equivalent to the given polar coordinates.
     */
    public static Point2D ofPolar(final double radius, final double azimuth) {
        return PolarCoordinates.toCartesian(radius, azimuth, Point2D::new);
    }

    /** Parses the given string and returns a new point instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return point instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Point2D parse(String str) {
        return SimpleTupleFormat.getDefault().parse(str, Point2D::new);
    }

    /** Returns a point with coordinates calculated by multiplying each input coordinate
     * with its corresponding factor and adding the results.
     *
     * <p>This is equivalent
     * to converting all input coordinates to vectors, scaling and adding the
     * vectors (a linear combination), and adding the result to the zero point.
     * This method, however, does not create any intermediate objects.
     * </p>
     * <p>
     * The name of this method was chosen to emphasize the fact that the operation
     * should be viewed as occurring in vector space, since addition and scalar
     * multiplication are not defined directly for points.
     * </p>
     *
     * @param a scale factor for first coordinate
     * @param c first coordinate
     * @return point with coordinates calculated by {@code a * c}
     */
    public static Point2D vectorCombination(double a, Cartesian2D c) {
        return new Point2D(a * c.getX(), a * c.getY());
    }

    /** Returns a point with coordinates calculated by multiplying each input coordinate
     * with its corresponding factor and adding the results.
     *
     * <p>This is equivalent
     * to converting all input coordinates to vectors, scaling and adding the
     * vectors (a linear combination), and adding the result to the zero point.
     * This method, however, does not create any intermediate objects.
     * </p>
     * <p>
     * The name of this method was chosen to emphasize the fact that the operation
     * should be viewed as occurring in vector space, since addition and scalar
     * multiplication are not defined directly for points.
     * </p>
     *
     * @param a1 scale factor for first coordinate
     * @param c1 first coordinate
     * @param a2 scale factor for second coordinate
     * @param c2 second coordinate
     * @return point with coordinates calculated by {@code (a1 * c1) + (a2 * c2)}
     */
    public static Point2D vectorCombination(double a1, Cartesian2D c1, double a2, Cartesian2D c2) {
        return new Point2D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY()));
    }

    /** Returns a point with coordinates calculated by multiplying each input coordinate
     * with its corresponding factor and adding the results.
     *
     * <p>This is equivalent
     * to converting all input coordinates to vectors, scaling and adding the
     * vectors (a linear combination), and adding the result to the zero point.
     * This method, however, does not create any intermediate objects.
     * </p>
     * <p>
     * The name of this method was chosen to emphasize the fact that the operation
     * should be viewed as occurring in vector space, since addition and scalar
     * multiplication are not defined directly for points.
     * </p>
     *
     * @param a1 scale factor for first coordinate
     * @param c1 first coordinate
     * @param a2 scale factor for second coordinate
     * @param c2 second coordinate
     * @param a3 scale factor for third coordinate
     * @param c3 third coordinate
     * @return point with coordinates calculated by {@code (a1 * c1) + (a2 * c2) + (a3 * c3)}
     */
    public static Point2D vectorCombination(double a1, Cartesian2D c1, double a2, Cartesian2D c2,
            double a3, Cartesian2D c3) {
        return new Point2D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY(), a3, c3.getY()));
    }

    /** Returns a point with coordinates calculated by multiplying each input coordinate
     * with its corresponding factor and adding the results.
     *
     * <p>This is equivalent
     * to converting all input coordinates to vectors, scaling and adding the
     * vectors (a linear combination), and adding the result to the zero point.
     * This method, however, does not create any intermediate objects.
     * </p>
     * <p>
     * The name of this method was chosen to emphasize the fact that the operation
     * should be viewed as occurring in vector space, since addition and scalar
     * multiplication are not defined directly for points.
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
    public static Point2D vectorCombination(double a1, Cartesian2D c1, double a2, Cartesian2D c2,
            double a3, Cartesian2D c3, double a4, Cartesian2D c4) {
        return new Point2D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX(), a4, c4.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY(), a3, c3.getY(), a4, c4.getY()));
    }
}
