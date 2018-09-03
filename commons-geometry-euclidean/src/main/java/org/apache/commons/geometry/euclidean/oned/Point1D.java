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

import org.apache.commons.geometry.core.internal.DoubleFunction1N;
import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.EuclideanPoint;
import org.apache.commons.numbers.arrays.LinearCombination;

/** This class representing a point in one-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public final class Point1D extends Cartesian1D implements EuclideanPoint<Point1D, Vector1D> {

    /** Origin (coordinates: 0). */
    public static final Point1D ZERO = new Point1D(0.0);

    /** Unit (coordinates: 1). */
    public static final Point1D ONE  = new Point1D(1.0);

    /** Negative unit (coordinates: 1). */
    public static final Point1D MINUS_ONE  = new Point1D(-1.0);

    // CHECKSTYLE: stop ConstantName
    /** A vector with all coordinates set to NaN. */
    public static final Point1D NaN = new Point1D(Double.NaN);
    // CHECKSTYLE: resume ConstantName

    /** A point with all coordinates set to positive infinity. */
    public static final Point1D POSITIVE_INFINITY =
        new Point1D(Double.POSITIVE_INFINITY);

    /** A point with all coordinates set to negative infinity. */
    public static final Point1D NEGATIVE_INFINITY =
        new Point1D(Double.NEGATIVE_INFINITY);

    /** Serializable UID. */
    private static final long serialVersionUID = 20180710L;

    /** Factory for delegating instance creation. */
    private static DoubleFunction1N<Point1D> FACTORY = new DoubleFunction1N<Point1D>() {

        /** {@inheritDoc} */
        @Override
        public Point1D apply(double n) {
            return new Point1D(n);
        }
    };

    /** Simple constructor.
     * @param x abscissa (coordinate value)
     */
    private Point1D(double x) {
        super(x);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D asVector() {
        return Vector1D.of(getX());
    }

    /** {@inheritDoc} */
    @Override
    public double distance(Point1D p) {
        return Math.abs(p.getX() - getX());
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D subtract(Point1D p) {
        return Vector1D.of(getX() - p.getX());
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D vectorTo(Point1D p) {
        return p.subtract(this);
    }

    /** {@inheritDoc} */
    @Override
    public Point1D lerp(Point1D p, double t) {
        return vectorCombination(1.0 - t, this, t, p);
    }

    /** {@inheritDoc} */
    @Override
    public Point1D add(Vector1D v) {
        return new Point1D(getX() + v.getX());
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
            return 7785;
        }
        return 997 * Double.hashCode(getX());
    }

    /**
     * Test for the equality of two points.
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
     * @return true if the two point objects are equal, false if
     *         object is null, not an instance of Point1D, or
     *         not equal to this Point1D instance
     *
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof Point1D) {
            final Point1D rhs = (Point1D) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return getX() == rhs.getX();
        }
        return false;
    }

    /** Returns a point with the given coordinate value.
     * @param x point coordinate
     * @return point instance
     */
    public static Point1D of(double x) {
        return new Point1D(x);
    }

    /** Returns a point instance with the given coordinate value.
     * @param value point coordinate
     * @return point instance
     */
    public static Point1D of(Cartesian1D value) {
        return new Point1D(value.getX());
    }

    /** Parses the given string and returns a new point instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return point instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Point1D parse(String str) throws IllegalArgumentException {
        return SimpleTupleFormat.getDefault().parse(str, FACTORY);
    }

    /** Linearly interpolates between the two given points. This methods simply
     * calls {@code a.lerp(b, t)}.
     * @param a first point
     * @param b second point
     * @param t interpolation parameter
     * @return the interpolated point
     * @see #lerp(Point1D, double)
     */
    public static Point1D lerp(Point1D a, Point1D b, double t) {
        return a.lerp(b, t);
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
     * @see Vector1D#linearCombination(double, Cartesian1D)
     */
    public static Point1D vectorCombination(double a, Cartesian1D c) {
        return new Point1D(a * c.getX());
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
     * @see Vector1D#linearCombination(double, Cartesian1D, double, Cartesian1D)
     */
    public static Point1D vectorCombination(double a1, Cartesian1D c1, double a2, Cartesian1D c2) {
        return new Point1D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX()));
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
     * @see Vector1D#linearCombination(double, Cartesian1D, double, Cartesian1D, double, Cartesian1D)
     */
    public static Point1D vectorCombination(double a1, Cartesian1D c1, double a2, Cartesian1D c2,
            double a3, Cartesian1D c3) {
        return new Point1D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX()));
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
     * @see Vector1D#linearCombination(double, Cartesian1D, double, Cartesian1D, double, Cartesian1D, double, Cartesian1D)
     */
    public static Point1D vectorCombination(double a1, Cartesian1D c1, double a2, Cartesian1D c2,
            double a3, Cartesian1D c3, double a4, Cartesian1D c4) {
        return new Point1D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX(), a4, c4.getX()));
    }
}
