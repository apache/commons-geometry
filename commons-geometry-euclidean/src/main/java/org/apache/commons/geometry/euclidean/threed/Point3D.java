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

import org.apache.commons.geometry.core.internal.SimpleTupleFormat;
import org.apache.commons.geometry.euclidean.EuclideanPoint;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.numbers.arrays.LinearCombination;

/** This class represents a point in three-dimensional Euclidean space.
 * Instances of this class are guaranteed to be immutable.
 */
public final class Point3D extends Cartesian3D implements EuclideanPoint<Point3D, Vector3D> {

    /** Zero point (coordinates: 0, 0, 0). */
    public static final Point3D ZERO   = new Point3D(0, 0, 0);

    // CHECKSTYLE: stop ConstantName
    /** A point with all coordinates set to NaN. */
    public static final Point3D NaN = new Point3D(Double.NaN, Double.NaN, Double.NaN);
    // CHECKSTYLE: resume ConstantName

    /** A point with all coordinates set to positive infinity. */
    public static final Point3D POSITIVE_INFINITY =
        new Point3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /** A point with all coordinates set to negative infinity. */
    public static final Point3D NEGATIVE_INFINITY =
        new Point3D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    /** Serializable version identifier. */
    private static final long serialVersionUID = 20180710L;

    /** Simple constructor.
     * Build a point from its coordinates
     * @param x abscissa
     * @param y ordinate
     * @param z height
     */
    private Point3D(double x, double y, double z) {
        super(x, y, z);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D asVector() {
        return Vector3D.of(getX(), getY(), getZ());
    }

    /** {@inheritDoc} */
    @Override
    public double distance(Point3D p) {
        return Vectors.norm(
                getX() - p.getX(),
                getY() - p.getY(),
                getZ() - p.getZ());
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D subtract(Point3D p) {
        return Vector3D.of(
                    getX() - p.getX(),
                    getY() - p.getY(),
                    getZ() - p.getZ()
                );
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D vectorTo(Point3D p) {
        return p.subtract(this);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D directionTo(Point3D p) {
        return Vector3D.normalize(
                p.getX() - getX(),
                p.getY() - getY(),
                p.getZ() - getZ());
    }

    /** {@inheritDoc} */
    @Override
    public Point3D lerp(Point3D p, double t) {
        return vectorCombination(1.0 - t, this, t, p);
    }

    /** {@inheritDoc} */
    @Override
    public Point3D add(Vector3D v) {
        return new Point3D(
                    getX() + v.getX(),
                    getY() + v.getY(),
                    getZ() + v.getZ()
                );
    }

    /**
     * Get a hashCode for the point.
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
     * @return true if two Point3D objects are equal, false if
     *         object is null, not an instance of Point3D, or
     *         not equal to this Point3D instance
     *
     */
    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (other instanceof Point3D) {
            final Point3D rhs = (Point3D) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return (getX() == rhs.getX()) && (getY() == rhs.getY()) && (getZ() == rhs.getZ());
        }
        return false;
    }

    /** Returns a point with the given coordinate values
     * @param x abscissa (first coordinate value)
     * @param y ordinate (second coordinate value)
     * @param z height (third coordinate value)
     * @return point instance
     */
    public static Point3D of(double x, double y, double z) {
        return new Point3D(x, y, z);
    }

    /** Creates a point from the coordinates in the given 3-element array.
     * @param p coordinates array
     * @return new point
     * @exception IllegalArgumentException if the array does not have 3 elements
     */
    public static Point3D ofArray(double[] p) {
        if (p.length != 3) {
            throw new IllegalArgumentException("Dimension mismatch: " + p.length + " != 3");
        }
        return new Point3D(p[0], p[1], p[2]);
    }

    /** Create a point from a set of spherical coordinates.
     * @param radius the spherical radius value
     * @param azimuth the angle in the x-y plane measured in radians counter-clockwise from the
     *      positive x axis.
     * @param polar the angle with the positive z axis in radians.
     * @return a point instance with the given set of spherical coordinates
     */
    public static Point3D ofSpherical(double radius, double azimuth, double polar) {
        return SphericalCoordinates.toCartesian(radius, azimuth, polar, Point3D::new);
    }

    /** Parses the given string and returns a new point instance. The expected string
     * format is the same as that returned by {@link #toString()}.
     * @param str the string to parse
     * @return point instance represented by the string
     * @throws IllegalArgumentException if the given string has an invalid format
     */
    public static Point3D parse(String str) {
        return SimpleTupleFormat.getDefault().parse(str, Point3D::new);
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
    public static Point3D vectorCombination(double a, Cartesian3D c) {
        return new Point3D(a * c.getX(), a * c.getY(), a * c.getZ());
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
    public static Point3D vectorCombination(double a1, Cartesian3D c1, double a2, Cartesian3D c2) {
        return new Point3D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY()),
                LinearCombination.value(a1, c1.getZ(), a2, c2.getZ()));
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
    public static Point3D vectorCombination(double a1, Cartesian3D c1, double a2, Cartesian3D c2,
            double a3, Cartesian3D c3) {
        return new Point3D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY(), a3, c3.getY()),
                LinearCombination.value(a1, c1.getZ(), a2, c2.getZ(), a3, c3.getZ()));
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
    public static Point3D vectorCombination(double a1, Cartesian3D c1, double a2, Cartesian3D c2,
            double a3, Cartesian3D c3, double a4, Cartesian3D c4) {
        return new Point3D(
                LinearCombination.value(a1, c1.getX(), a2, c2.getX(), a3, c3.getX(), a4, c4.getX()),
                LinearCombination.value(a1, c1.getY(), a2, c2.getY(), a3, c3.getY(), a4, c4.getY()),
                LinearCombination.value(a1, c1.getZ(), a2, c2.getZ(), a3, c3.getZ(), a4, c4.getZ()));
    }
}
