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

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.geometry.core.internal.DoubleFunction3N;
import org.apache.commons.geometry.euclidean.exception.NonInvertibleTransformException;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.numbers.arrays.LinearCombination;

/** Class representing an affine transformation in 3 dimensional Euclidean space.
 *
 * <p>Instances of this class use a 4x4 matrix internally for all transform operations.
 * The last row of this matrix is always set to the values <code>[0 0 0 1]</code> and so
 * is not stored. Hence, the methods in this class that accept or return arrays always
 * use arrays containing 12 elements, instead of 16.
 * </p>
 */
public final class AffineTransform3D implements Serializable {

    /** Serializable version identifier */
    private static final long serialVersionUID = 20180923L;

    /** The number of internal matrix elements */
    private static final int NUM_ELEMENTS = 12;

    /** String used to start the transform matrix string representation */
    private static final String MATRIX_START = "[ ";

    /** String used to end the transform matrix string representation */
    private static final String MATRIX_END = " ]";

    /** String used to separate elements in the matrix string representation */
    private static final String ELEMENT_SEPARATOR = ", ";

    /** String used to separate rows in the matrix string representation */
    private static final String ROW_SEPARATOR = "; ";

    /** Shared transform set to the identity matrix. */
    private static final AffineTransform3D IDENTITY = new AffineTransform3D();

    /** Transform matrix entry <code>m<sub>0,0</sub></code> */
    private double m00 = 1.0;
    /** Transform matrix entry <code>m<sub>0,1</sub></code> */
    private double m01 = 0.0;
    /** Transform matrix entry <code>m<sub>0,2</sub></code> */
    private double m02 = 0.0;
    /** Transform matrix entry <code>m<sub>0,3</sub></code> */
    private double m03 = 0.0;

    /** Transform matrix entry <code>m<sub>1,0</sub></code> */
    private double m10 = 0.0;
    /** Transform matrix entry <code>m<sub>1,1</sub></code> */
    private double m11 = 1.0;
    /** Transform matrix entry <code>m<sub>1,2</sub></code> */
    private double m12 = 0.0;
    /** Transform matrix entry <code>m<sub>1,3</sub></code> */
    private double m13 = 0.0;

    /** Transform matrix entry <code>m<sub>2,0</sub></code> */
    private double m20 = 0.0;
    /** Transform matrix entry <code>m<sub>2,1</sub></code> */
    private double m21 = 0.0;
    /** Transform matrix entry <code>m<sub>2,2</sub></code> */
    private double m22 = 1.0;
    /** Transform matrix entry <code>m<sub>2,3</sub></code> */
    private double m23 = 0.0;

    /** Simple constructor. The internal matrix elements are initialized
     * to the identity matrix.
     */
    private AffineTransform3D() {
    }

    /** Return a 12 element array containing the variable elements from the
     * internal transformation matrix. The elements are in row-major order.
     * The array indices map to the internal matrix as follows:
     * <pre>
     *      [
     *          arr[0],   arr[1],   arr[2],   arr[3]
     *          arr[4],   arr[5],   arr[6],   arr[7],
     *          arr[8],   arr[9],   arr[10],  arr[11],
     *          0         0         0         1
     *      ]
     * </pre>
     * @return 12 element array containing the variable elements from the
     *      internal transformation matrix
     */
    public double[] toArray() {
        return new double[] {
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23
        };
    }

    /** Apply this transform to the given point. A new point is returned.
     * @param pt the point to transform
     * @return the new, transformed point
     */
    public Point3D applyTo(final Point3D pt) {
        return applyTo(pt, Point3D::of);
    }

    /** Apply this transform to the given vector. A new vector is returned.
     * @param vec the vector to transform
     * @return the new, transformed vector
     */
    public Vector3D applyTo(final Vector3D vec) {
        return applyTo(vec, Vector3D::of);
    }

    /** Get a new transform containing the result of applying a translation logically after
     * the transformation represented by the current instance. This is achieved by
     * creating a new translation transform and pre-multiplying it with the current
     * instance. In other words, the returned transform contains the matrix
     * <code>B * A</code>, where <code>A</code> is the current matrix and <code>B</code>
     * is the matrix representing the given translation.
     * @param translation vector containing the translation values for each axis
     * @return a new transform containing the result of applying a translation to
     *      the current instance
     */
    public AffineTransform3D translate(final Vector3D translation) {
        return translate(translation.getX(), translation.getY(), translation.getZ());
    }

    /** Get a new transform containing the result of applying a translation logically after
     * the transformation represented by the current instance. This is achieved by
     * creating a new translation transform and pre-multiplying it with the current
     * instance. In other words, the returned transform contains the matrix
     * <code>B * A</code>, where <code>A</code> is the current matrix and <code>B</code>
     * is the matrix representing the given translation.
     * @param x translation in the x direction
     * @param y translation in the y direction
     * @param z translation in the z direction
     * @return a new transform containing the result of applying a translation to
     *      the current instance
     */
    public AffineTransform3D translate(final double x, final double y, final double z) {
        final AffineTransform3D result = createTranslation(x, y, z);

        return multiply(result, this, result);
    }

    /** Get a new transform containing the result of applying a scale operation of the
     * given value in all axes logically after the transformation represented by the current instance.
     * This is achieved by creating a new scale transform and pre-multiplying it with the current
     * instance. In other words, the returned transform contains the matrix
     * <code>B * A</code>, where <code>A</code> is the current matrix and <code>B</code>
     * is the matrix representing the given scale operation.
     * @param factor the scale factor to apply to all axes
     * @return a new transform containing the result of applying a scale operation to
     *      the current instance
     */
    public AffineTransform3D scale(final double factor) {
        return scale(factor, factor, factor);
    }

    /** Get a new transform containing the result of applying a scale operation
     * logically after the transformation represented by the current instance.
     * This is achieved by creating a new scale transform and pre-multiplying it with the current
     * instance. In other words, the returned transform contains the matrix
     * <code>B * A</code>, where <code>A</code> is the current matrix and <code>B</code>
     * is the matrix representing the given scale operation.
     * @param scaleFactors vector containing scale factors for each axis
     * @return a new transform containing the result of applying a scale operation to
     *      the current instance
     */
    public AffineTransform3D scale(final Vector3D scaleFactors) {
        return scale(scaleFactors.getX(), scaleFactors.getY(), scaleFactors.getZ());
    }

    /** Get a new transform containing the result of applying a scale operation
     * logically after the transformation represented by the current instance.
     * This is achieved by creating a new scale transform and pre-multiplying it with the current
     * instance. In other words, the returned transform contains the matrix
     * <code>B * A</code>, where <code>A</code> is the current matrix and <code>B</code>
     * is the matrix representing the given scale operation.
     * @param x scale factor for the x axis
     * @param y scale factor for the y axis
     * @param z scale factor for the z axis
     * @return a new transform containing the result of applying a scale operation to
     *      the current instance
     */
    public AffineTransform3D scale(final double x, final double y, final double z) {
        final AffineTransform3D result = createScale(x, y, z);

        return multiply(result, this, result);
    }

    /** Get a new transform created by multiplying the given transform with the current
     * instance. The computed value is <code>A * B</code> where <code>A</code> is the matrix
     * of the current instance and <code>B</code> is the matrix of the given instance.
     * @param b the other transform to multiply with
     * @return the result of multiplying this transform with {@code b}
     */
    public AffineTransform3D multiply(final AffineTransform3D b) {
        return multiply(this, b, new AffineTransform3D());
    }

    /** Get a new transform representing the inverse of the current instance.
     * @return inverse transform
     * @throws NonInvertibleTransformException if the transform matrix cannot be inverted
     */
    public AffineTransform3D getInverse() {

        // compute the determinant of the matrix
        final double det = determinant(
                    m00, m01, m02,
                    m10, m11, m12,
                    m20, m21, m22
                );

        if (!Vectors.isRealNonZero(det)) {
            throw new NonInvertibleTransformException("Transform is not invertible; matrix determinant is " + det);
        }

        // validate the remaining matrix elements that were not part of the determinant
        validateElementForInverse(m03);
        validateElementForInverse(m13);
        validateElementForInverse(m23);

        // compute the necessary elements of the cofactor matrix
        // (we need all but the last column)
        final double c00 = determinant(m11, m12, m21, m22);
        final double c01 = - determinant(m10, m12, m20, m22);
        final double c02 = determinant(m10, m11, m20, m21);

        final double c10 = - determinant(m01, m02, m21, m22);
        final double c11 = determinant(m00, m02, m20, m22);
        final double c12 = - determinant(m00, m01, m20, m21);

        final double c20 = determinant(m01, m02, m11, m12);
        final double c21 = - determinant(m00, m02, m10, m12);
        final double c22 = determinant(m00, m01, m10, m11);

        final double c30 = - determinant(
                    m01, m02, m03,
                    m11, m12, m13,
                    m21, m22, m23
                );
        final double c31 = determinant(
                    m00, m02, m03,
                    m10, m12, m13,
                    m20, m22, m23
                );
        final double c32 = - determinant(
                    m00, m01, m03,
                    m10, m11, m13,
                    m20, m21, m23
                );

        // the final answer is the adjugate matrix (the transpose of the cofactor matrix)
        // multiplied by the inverse of the determinant
        final double invDet = 1.0 / det;

        AffineTransform3D inverse = new AffineTransform3D();
        inverse.m00 = invDet * c00;
        inverse.m01 = invDet * c10;
        inverse.m02 = invDet * c20;
        inverse.m03 = invDet * c30;

        inverse.m10 = invDet * c01;
        inverse.m11 = invDet * c11;
        inverse.m12 = invDet * c21;
        inverse.m13 = invDet * c31;

        inverse.m20 = invDet * c02;
        inverse.m21 = invDet * c12;
        inverse.m22 = invDet * c22;
        inverse.m23 = invDet * c32;

        return inverse;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = (result * prime) + (Double.hashCode(m00) + Double.hashCode(m01) + Double.hashCode(m02) + Double.hashCode(m03));
        result = (result * prime) + (Double.hashCode(m10) + Double.hashCode(m11) + Double.hashCode(m12) + Double.hashCode(m13));
        result = (result * prime) + (Double.hashCode(m20) + Double.hashCode(m21) + Double.hashCode(m22) + Double.hashCode(m23));

        return result;
    }

    /**
     * Return true if the given object is an instance of {@link AffineTransform3D}
     * and all matrix element values are exactly equal.
     * @param obj object to test for equality with the current instance
     * @return true if all transform matrix elements are exactly equal; otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AffineTransform3D)) {
            return false;
        }

        AffineTransform3D other = (AffineTransform3D) obj;

        return Arrays.equals(toArray(), other.toArray());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(MATRIX_START)

            .append(m00)
            .append(ELEMENT_SEPARATOR)
            .append(m01)
            .append(ELEMENT_SEPARATOR)
            .append(m02)
            .append(ELEMENT_SEPARATOR)
            .append(m03)
            .append(ROW_SEPARATOR)

            .append(m10)
            .append(ELEMENT_SEPARATOR)
            .append(m11)
            .append(ELEMENT_SEPARATOR)
            .append(m12)
            .append(ELEMENT_SEPARATOR)
            .append(m13)
            .append(ROW_SEPARATOR)

            .append(m20)
            .append(ELEMENT_SEPARATOR)
            .append(m21)
            .append(ELEMENT_SEPARATOR)
            .append(m22)
            .append(ELEMENT_SEPARATOR)
            .append(m23)

            .append(MATRIX_END);

        return sb.toString();
    }

    /** Multiply two transform matrices together, storing the result in a third instance. The result is computed
     * completely and then stored into the output transform, meaning that the output transform can be the same
     * as one of the inputs.
     * @param a first transform
     * @param b second transform
     * @param c output transform; may be one of {@code a} or {@code b}
     * @return the output matrix given in {@code c}, which contains the result of multiplying {@code a} and {@code b}
     */
    private AffineTransform3D multiply(final AffineTransform3D a, final AffineTransform3D b, final AffineTransform3D c) {

        // calculate the matrix elements
        final double c00 = LinearCombination.value(a.m00, b.m00, a.m01, b.m10, a.m02, b.m20);
        final double c01 = LinearCombination.value(a.m00, b.m01, a.m01, b.m11, a.m02, b.m21);
        final double c02 = LinearCombination.value(a.m00, b.m02, a.m01, b.m12, a.m02, b.m22);
        final double c03 = LinearCombination.value(a.m00, b.m03, a.m01, b.m13, a.m02, b.m23) + a.m03;

        final double c10 = LinearCombination.value(a.m10, b.m00, a.m11, b.m10, a.m12, b.m20);
        final double c11 = LinearCombination.value(a.m10, b.m01, a.m11, b.m11, a.m12, b.m21);
        final double c12 = LinearCombination.value(a.m10, b.m02, a.m11, b.m12, a.m12, b.m22);
        final double c13 = LinearCombination.value(a.m10, b.m03, a.m11, b.m13, a.m12, b.m23) + a.m13;

        final double c20 = LinearCombination.value(a.m20, b.m00, a.m21, b.m10, a.m22, b.m20);
        final double c21 = LinearCombination.value(a.m20, b.m01, a.m21, b.m11, a.m22, b.m21);
        final double c22 = LinearCombination.value(a.m20, b.m02, a.m21 , b.m12, a.m22, b.m22);
        final double c23 = LinearCombination.value(a.m20, b.m03, a.m21 , b.m13, a.m22, b.m23) + a.m23;

        // assign to the output
        c.m00 = c00;
        c.m01 = c01;
        c.m02 = c02;
        c.m03 = c03;

        c.m10 = c10;
        c.m11 = c11;
        c.m12 = c12;
        c.m13 = c13;

        c.m20 = c20;
        c.m21 = c21;
        c.m22 = c22;
        c.m23 = c23;

        return c;
    }

    /** Apply the transform to the given set of Cartesian coordinates. The transformed
     * coordinates are passed to the given factory function and its return value is
     * returned.
     * @param <T> Type returned by {@code factory}
     * @param coords coordinates to transform
     * @param factory function accepting transformed coordinates and returning a value
     * @return the return value from {@code factory}
     */
    private <T> T applyTo(final Cartesian3D coords, DoubleFunction3N<T> factory) {
        final double x = coords.getX();
        final double y = coords.getY();
        final double z = coords.getZ();

        final double resultX = LinearCombination.value(m00, x, m01, y, m02, z) + m03;
        final double resultY = LinearCombination.value(m10, x, m11, y, m12, z) + m13;
        final double resultZ = LinearCombination.value(m20, x, m21, y, m22, z) + m23;

        return factory.apply(resultX, resultY, resultZ);
    }

    /** Get a new transform with the given matrix elements. The array must contain 12 elements.
     * @param arr 12-element array containing values for the variable entries in the
     *      transform matrix
     * @return a new transform initialized with the given matrix values
     * @throws IllegalArgumentException if the array does not have 12 elements
     */
    public static AffineTransform3D of(final double ... arr) {
        if (arr.length != NUM_ELEMENTS) {
            throw new IllegalArgumentException("Dimension mismatch: " + arr.length + " != " + NUM_ELEMENTS);
        }

        AffineTransform3D result = new AffineTransform3D();

        result.m00 = arr[0];
        result.m01 = arr[1];
        result.m02 = arr[2];
        result.m03 = arr[3];

        result.m10 = arr[4];
        result.m11 = arr[5];
        result.m12 = arr[6];
        result.m13 = arr[7];

        result.m20 = arr[8];
        result.m21 = arr[9];
        result.m22 = arr[10];
        result.m23 = arr[11];

        return result;
    }

    /** Get the transform representing the identity matrix. This transform does not
     * modify point or vector values when applied.
     * @return transform representing the identity matrix
     */
    public static AffineTransform3D identity() {
        return IDENTITY;
    }

    /** Get a transform representing the given translation.
     * @param translation vector containing translation values for each axis
     * @return a new transform representing the given translation
     */
    public static AffineTransform3D createTranslation(final Vector3D translation) {
        return createTranslation(translation.getX(), translation.getY(), translation.getZ());
    }

    /** Get a transform representing the given translation.
     * @param x translation in the x direction
     * @param y translation in the y direction
     * @param z translation in the z direction
     * @return a new transform representing the given translation
     */
    public static AffineTransform3D createTranslation(final double x, final double y, final double z) {
        final AffineTransform3D transform = new AffineTransform3D();

        transform.m03 = x;
        transform.m13 = y;
        transform.m23 = z;

        return transform;
    }

    /** Get a transform representing a scale operation with the given scale factor applied to all axes.
     * @param factor scale factor to apply to all axes
     * @return a new transform representing a uniform scaling in all axes
     */
    public static AffineTransform3D createScale(final double factor) {
        return createScale(factor, factor, factor);
    }

    /** Get a transform representing a scale operation.
     * @param factors vector containing scale factors for each axis
     * @return a new transform representing a scale operation
     */
    public static AffineTransform3D createScale(final Vector3D factors) {
        return createScale(factors.getX(), factors.getY(), factors.getZ());
    }

    /** Get a transform representing a scale operation.
     * @param x scale factor for the x axis
     * @param y scale factor for the y axis
     * @param z scale factor for the z axis
     * @return a new transform representing a scale operation
     */
    public static AffineTransform3D createScale(final double x, final double y, final double z) {
        final AffineTransform3D transform = new AffineTransform3D();

        transform.m00 = x;
        transform.m11 = y;
        transform.m22 = z;

        return transform;
    }

    /** Compute the determinant of the 2x2 matrix represented by the given values.
     * @param a00 matrix entry <code>a<sub>0,0</sub></code>
     * @param a01 matrix entry <code>a<sub>0,1</sub></code>
     * @param a10 matrix entry <code>a<sub>1,0</sub></code>
     * @param a11 matrix entry <code>a<sub>1,1</sub></code>
     * @return computed 2x2 matrix determinant
     */
    private static double determinant(
            final double a00, final double a01,
            final double a10, final double a11) {

        return (a00 * a11) - (a01 * a10);
    }

    /** Compute the determinant of the 3x3 matrix represented by the given values.
     * @param a00 matrix entry <code>a<sub>0,0</sub></code>
     * @param a01 matrix entry <code>a<sub>0,1</sub></code>
     * @param a02 matrix entry <code>a<sub>0,2</sub></code>
     * @param a10 matrix entry <code>a<sub>1,0</sub></code>
     * @param a11 matrix entry <code>a<sub>1,1</sub></code>
     * @param a12 matrix entry <code>a<sub>1,2</sub></code>
     * @param a20 matrix entry <code>a<sub>2,0</sub></code>
     * @param a21 matrix entry <code>a<sub>2,1</sub></code>
     * @param a22 matrix entry <code>a<sub>2,2</sub></code>
     * @return computed 3x3 matrix determinant
     */
    private static double determinant(
            final double a00, final double a01, final double a02,
            final double a10, final double a11, final double a12,
            final double a20, final double a21, final double a22) {

        return ((a00 * a11 * a22) + (a01 * a12 * a20) + (a02 * a10 * a21)) -
                ((a00 * a12 * a21) + (a01 * a10 * a22) + (a02 * a11 * a20));
    }

    /** Checks that the given matrix element is valid for use in calculation of
     * a matrix inverse. Throws a {@link NonInvertibleTransformException} if not.
     * @param element matrix entry to check
     * @throws NonInvertibleTransformException if the element is not valid for use
     *  in calculating a matrix inverse, ie if it is NaN or infinite.
     */
    private static void validateElementForInverse(final double element) {
        if (!Double.isFinite(element)) {
            throw new NonInvertibleTransformException("Transform is not invertible; invalid matrix element: " + element);
        }
    }
}
