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

import org.apache.commons.geometry.core.internal.DoubleFunction3N;
import org.apache.commons.geometry.euclidean.AbstractAffineTransformMatrix;
import org.apache.commons.geometry.euclidean.exception.NonInvertibleTransformException;
import org.apache.commons.geometry.euclidean.internal.Matrices;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.arrays.LinearCombination;
import org.apache.commons.numbers.core.Precision;

/** Class using a matrix to represent affine transformations in 3 dimensional Euclidean space.
 *
 * <p>Instances of this class use a 4x4 matrix for all transform operations.
 * The last row of this matrix is always set to the values <code>[0 0 0 1]</code> and so
 * is not stored. Hence, the methods in this class that accept or return arrays always
 * use arrays containing 12 elements, instead of 16.
 * </p>
 */
public final class AffineTransformMatrix3D extends AbstractAffineTransformMatrix<Vector3D>
    implements Transform3D, Serializable {

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
    private static final AffineTransformMatrix3D IDENTITY_INSTANCE = new AffineTransformMatrix3D(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0
            );

    /** Transform matrix entry <code>m<sub>0,0</sub></code> */
    private final double m00;
    /** Transform matrix entry <code>m<sub>0,1</sub></code> */
    private final double m01;
    /** Transform matrix entry <code>m<sub>0,2</sub></code> */
    private final double m02;
    /** Transform matrix entry <code>m<sub>0,3</sub></code> */
    private final double m03;

    /** Transform matrix entry <code>m<sub>1,0</sub></code> */
    private final double m10;
    /** Transform matrix entry <code>m<sub>1,1</sub></code> */
    private final double m11;
    /** Transform matrix entry <code>m<sub>1,2</sub></code> */
    private final double m12;
    /** Transform matrix entry <code>m<sub>1,3</sub></code> */
    private final double m13;

    /** Transform matrix entry <code>m<sub>2,0</sub></code> */
    private final double m20;
    /** Transform matrix entry <code>m<sub>2,1</sub></code> */
    private final double m21;
    /** Transform matrix entry <code>m<sub>2,2</sub></code> */
    private final double m22;
    /** Transform matrix entry <code>m<sub>2,3</sub></code> */
    private final double m23;

    /**
     * Package-private constructor; sets all internal matrix elements.
     * @param m00 matrix entry <code>m<sub>0,0</sub></code>
     * @param m01 matrix entry <code>m<sub>0,1</sub></code>
     * @param m02 matrix entry <code>m<sub>0,2</sub></code>
     * @param m03 matrix entry <code>m<sub>0,3</sub></code>
     * @param m10 matrix entry <code>m<sub>1,0</sub></code>
     * @param m11 matrix entry <code>m<sub>1,1</sub></code>
     * @param m12 matrix entry <code>m<sub>1,2</sub></code>
     * @param m13 matrix entry <code>m<sub>1,3</sub></code>
     * @param m20 matrix entry <code>m<sub>2,0</sub></code>
     * @param m21 matrix entry <code>m<sub>2,1</sub></code>
     * @param m22 matrix entry <code>m<sub>2,2</sub></code>
     * @param m23 matrix entry <code>m<sub>2,3</sub></code>
     */
    private AffineTransformMatrix3D(
            final double m00, final double m01, final double m02, final double m03,
            final double m10, final double m11, final double m12, final double m13,
            final double m20, final double m21, final double m22, final double m23) {

        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;

        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;

        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
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

    /** Apply this transform to the given point, returning the result as a new instance.
     *
     * <p>The transformed point is computed by creating a 4-element column vector from the
     * coordinates in the input and setting the last element to 1. This is then multiplied with the
     * 4x4 transform matrix to produce the transformed point. The {@code 1} in the last position
     * is ignored.
     * <pre>
     *      [ m00  m01  m02  m03 ]     [ x ]     [ x']
     *      [ m10  m11  m12  m13 ]  *  [ y ]  =  [ y']
     *      [ m20  m21  m22  m23 ]     [ z ]     [ z']
     *      [ 0    0    0    1   ]     [ 1 ]     [ 1 ]
     * </pre>
     */
    @Override
    public Vector3D apply(final Vector3D pt) {
        final double x = pt.getX();
        final double y = pt.getY();
        final double z = pt.getZ();

        final double resultX = LinearCombination.value(m00, x, m01, y, m02, z) + m03;
        final double resultY = LinearCombination.value(m10, x, m11, y, m12, z) + m13;
        final double resultZ = LinearCombination.value(m20, x, m21, y, m22, z) + m23;

        return Vector3D.of(resultX, resultY, resultZ);
    }

    /** {@inheritDoc}
     *
     *  <p>The transformed vector is computed by creating a 4-element column vector from the
     * coordinates in the input and setting the last element to 0. This is then multiplied with the
     * 4x4 transform matrix to produce the transformed vector. The {@code 0} in the last position
     * is ignored.
     * <pre>
     *      [ m00  m01  m02  m03 ]     [ x ]     [ x']
     *      [ m10  m11  m12  m13 ]  *  [ y ]  =  [ y']
     *      [ m20  m21  m22  m23 ]     [ z ]     [ z']
     *      [ 0    0    0    1   ]     [ 0 ]     [ 0 ]
     * </pre>
     *
     * @see #applyDirection(Vector3D)
     */
    @Override
    public Vector3D applyVector(final Vector3D vec) {
        return applyVector(vec, Vector3D::of);
    }

    /** {@inheritDoc}
     * @see #applyVector(Vector3D)
     */
    @Override
    public Vector3D applyDirection(final Vector3D vec) {
        return applyVector(vec, Vector3D::normalize);
    }

    /** {@inheritDoc} */
    @Override
    public double determinant() {
        return Matrices.determinant(
                m00, m01, m02,
                m10, m11, m12,
                m20, m21, m22
            );
    }

    /** {@inheritDoc}
    *
    * <p>This simply returns the current instance.</p>
    */
   @Override
   public AffineTransformMatrix3D toMatrix() {
       return this;
   }

    /** Apply a translation to the current instance, returning the result as a new transform.
     * @param translation vector containing the translation values for each axis
     * @return a new transform containing the result of applying a translation to
     *      the current instance
     */
    public AffineTransformMatrix3D translate(final Vector3D translation) {
        return translate(translation.getX(), translation.getY(), translation.getZ());
    }

    /** Apply a translation to the current instance, returning the result as a new transform.
     * @param x translation in the x direction
     * @param y translation in the y direction
     * @param z translation in the z direction
     * @return a new transform containing the result of applying a translation to
     *      the current instance
     */
    public AffineTransformMatrix3D translate(final double x, final double y, final double z) {
        return new AffineTransformMatrix3D(
                    m00, m01, m02, m03 + x,
                    m10, m11, m12, m13 + y,
                    m20, m21, m22, m23 + z
                );
    }

    /** Apply a scale operation to the current instance, returning the result as a new transform.
     * @param factor the scale factor to apply to all axes
     * @return a new transform containing the result of applying a scale operation to
     *      the current instance
     */
    public AffineTransformMatrix3D scale(final double factor) {
        return scale(factor, factor, factor);
    }

    /** Apply a scale operation to the current instance, returning the result as a new transform.
     * @param scaleFactors vector containing scale factors for each axis
     * @return a new transform containing the result of applying a scale operation to
     *      the current instance
     */
    public AffineTransformMatrix3D scale(final Vector3D scaleFactors) {
        return scale(scaleFactors.getX(), scaleFactors.getY(), scaleFactors.getZ());
    }

    /** Apply a scale operation to the current instance, returning the result as a new transform.
     * @param x scale factor for the x axis
     * @param y scale factor for the y axis
     * @param z scale factor for the z axis
     * @return a new transform containing the result of applying a scale operation to
     *      the current instance
     */
    public AffineTransformMatrix3D scale(final double x, final double y, final double z) {
        return new AffineTransformMatrix3D(
                    m00 * x, m01 * x, m02 * x, m03 * x,
                    m10 * y, m11 * y, m12 * y, m13 * y,
                    m20 * z, m21 * z, m22 * z, m23 * z
                );
    }

    /** Apply a rotation to the current instance, returning the result as a new transform.
     * @param rotation the rotation to apply
     * @return a new transform containing the result of applying a rotation to the
     *      current instance
     * @see QuaternionRotation#toTransformMatrix()
     */
    public AffineTransformMatrix3D rotate(final QuaternionRotation rotation) {
        return multiply(rotation.toMatrix(), this);
    }

    /** Apply a rotation around the given center point to the current instance, returning the result
     * as a new transform. This is achieved by translating the center point to the origin, applying
     * the rotation, and then translating back.
     * @param center the center of rotation
     * @param rotation the rotation to apply
     * @return a new transform containing the result of applying a rotation about the given center
     *      point to the current instance
     * @see QuaternionRotation#toTransformMatrix()
     */
    public AffineTransformMatrix3D rotate(final Vector3D center, final QuaternionRotation rotation) {
        return multiply(createRotation(center, rotation), this);
    }

    /** Get a new transform created by multiplying this instance by the argument.
     * This is equivalent to the expression {@code A * M} where {@code A} is the
     * current transform matrix and {@code M} is the given transform matrix. In
     * terms of transformations, applying the returned matrix is equivalent to
     * applying {@code M} and <em>then</em> applying {@code A}. In other words,
     * the rightmost transform is applied first.
     *
     * @param m the transform to multiply with
     * @return the result of multiplying the current instance by the given
     *      transform matrix
     */
    public AffineTransformMatrix3D multiply(final AffineTransformMatrix3D m) {
        return multiply(this, m);
    }

    /** Get a new transform created by multiplying the argument by this instance.
     * This is equivalent to the expression {@code M * A} where {@code A} is the
     * current transform matrix and {@code M} is the given transform matrix. In
     * terms of transformations, applying the returned matrix is equivalent to
     * applying {@code A} and <em>then</em> applying {@code M}. In other words,
     * the rightmost transform is applied first.
     *
     * @param m the transform to multiply with
     * @return the result of multiplying the given transform matrix by the current
     *      instance
     */
    public AffineTransformMatrix3D premultiply(final AffineTransformMatrix3D m) {
        return multiply(m, this);
    }

    /** Get a new transform representing the inverse of the current instance.
     * @return inverse transform
     * @throws NonInvertibleTransformException if the transform matrix cannot be inverted
     */
    public AffineTransformMatrix3D inverse() {

        // Our full matrix is 4x4 but we can significantly reduce the amount of computations
        // needed here since we know that our last row is [0 0 0 1].

        final double det = determinant();

        if (!Vectors.isRealNonZero(det)) {
            throw new NonInvertibleTransformException("Transform is not invertible; matrix determinant is " + det);
        }

        // validate the remaining matrix elements that were not part of the determinant
        validateElementForInverse(m03);
        validateElementForInverse(m13);
        validateElementForInverse(m23);

        // compute the necessary elements of the cofactor matrix
        // (we need all but the last column)

        final double invDet = 1.0 / det;

        final double c00 = invDet * Matrices.determinant(m11, m12, m21, m22);
        final double c01 = - invDet * Matrices.determinant(m10, m12, m20, m22);
        final double c02 = invDet * Matrices.determinant(m10, m11, m20, m21);

        final double c10 = - invDet * Matrices.determinant(m01, m02, m21, m22);
        final double c11 = invDet * Matrices.determinant(m00, m02, m20, m22);
        final double c12 = - invDet * Matrices.determinant(m00, m01, m20, m21);

        final double c20 = invDet * Matrices.determinant(m01, m02, m11, m12);
        final double c21 = - invDet * Matrices.determinant(m00, m02, m10, m12);
        final double c22 = invDet * Matrices.determinant(m00, m01, m10, m11);

        final double c30 = - invDet * Matrices.determinant(
                    m01, m02, m03,
                    m11, m12, m13,
                    m21, m22, m23
                );
        final double c31 = invDet * Matrices.determinant(
                    m00, m02, m03,
                    m10, m12, m13,
                    m20, m22, m23
                );
        final double c32 = - invDet * Matrices.determinant(
                    m00, m01, m03,
                    m10, m11, m13,
                    m20, m21, m23
                );

        return new AffineTransformMatrix3D(
                    c00, c10, c20, c30,
                    c01, c11, c21, c31,
                    c02, c12, c22, c32
                );
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = (result * prime) + (Double.hashCode(m00) - Double.hashCode(m01) + Double.hashCode(m02) - Double.hashCode(m03));
        result = (result * prime) + (Double.hashCode(m10) - Double.hashCode(m11) + Double.hashCode(m12) - Double.hashCode(m13));
        result = (result * prime) + (Double.hashCode(m20) - Double.hashCode(m21) + Double.hashCode(m22) - Double.hashCode(m23));

        return result;
    }

    /**
     * Return true if the given object is an instance of {@link AffineTransformMatrix3D}
     * and all matrix element values are exactly equal.
     * @param obj object to test for equality with the current instance
     * @return true if all transform matrix elements are exactly equal; otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AffineTransformMatrix3D)) {
            return false;
        }

        final AffineTransformMatrix3D other = (AffineTransformMatrix3D) obj;

        return Precision.equals(this.m00, other.m00) &&
                Precision.equals(this.m01, other.m01) &&
                Precision.equals(this.m02, other.m02) &&
                Precision.equals(this.m03, other.m03) &&

                Precision.equals(this.m10, other.m10) &&
                Precision.equals(this.m11, other.m11) &&
                Precision.equals(this.m12, other.m12) &&
                Precision.equals(this.m13, other.m13) &&

                Precision.equals(this.m20, other.m20) &&
                Precision.equals(this.m21, other.m21) &&
                Precision.equals(this.m22, other.m22) &&
                Precision.equals(this.m23, other.m23);
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

    /** Multiplies the given vector by the 3x3 linear transformation matrix contained in the
     * upper-right corner of the affine transformation matrix. This applies all transformation
     * operations except for translations. The computed coordinates are passed to the given
     * factory function.
     * @param <T> factory output type
     * @param vec the vector to transform
     * @param factory the factory instance that will be passed the transformed coordinates
     * @return the factory return value
     */
    private <T> T applyVector(final Vector3D vec, final DoubleFunction3N<T> factory) {
        final double x = vec.getX();
        final double y = vec.getY();
        final double z = vec.getZ();

        final double resultX = LinearCombination.value(m00, x, m01, y, m02, z);
        final double resultY = LinearCombination.value(m10, x, m11, y, m12, z);
        final double resultZ = LinearCombination.value(m20, x, m21, y, m22, z);

        return factory.apply(resultX, resultY, resultZ);
    }

    /** Get a new transform with the given matrix elements. The array must contain 12 elements.
     * @param arr 12-element array containing values for the variable entries in the
     *      transform matrix
     * @return a new transform initialized with the given matrix values
     * @throws IllegalArgumentException if the array does not have 12 elements
     */
    public static AffineTransformMatrix3D of(final double ... arr) {
        if (arr.length != NUM_ELEMENTS) {
            throw new IllegalArgumentException("Dimension mismatch: " + arr.length + " != " + NUM_ELEMENTS);
        }

        return new AffineTransformMatrix3D(
                    arr[0], arr[1], arr[2], arr[3],
                    arr[4], arr[5], arr[6], arr[7],
                    arr[8], arr[9], arr[10], arr[11]
                );
    }

    /** Get a new transform create from the given column vectors. The returned transform
     * does not include any translation component.
     * @param u first column vector; this corresponds to the first basis vector
     *      in the coordinate frame
     * @param v second column vector; this corresponds to the second basis vector
     *      in the coordinate frame
     * @param w third column vector; this corresponds to the third basis vector
     *      in the coordinate frame
     * @return a new transform with the given column vectors
     */
    public static AffineTransformMatrix3D fromColumnVectors(final Vector3D u, final Vector3D v, final Vector3D w) {
        return fromColumnVectors(u, v, w, Vector3D.ZERO);
    }

    /** Get a new transform created from the given column vectors.
     * @param u first column vector; this corresponds to the first basis vector
     *      in the coordinate frame
     * @param v second column vector; this corresponds to the second basis vector
     *      in the coordinate frame
     * @param w third column vector; this corresponds to the third basis vector
     *      in the coordinate frame
     * @param t fourth column vector; this corresponds to the translation of the transform
     * @return a new transform with the given column vectors
     */
    public static AffineTransformMatrix3D fromColumnVectors(final Vector3D u, final Vector3D v, final Vector3D w, final Vector3D t) {
        return new AffineTransformMatrix3D(
                    u.getX(), v.getX(), w.getX(), t.getX(),
                    u.getY(), v.getY(), w.getY(), t.getY(),
                    u.getZ(), v.getZ(), w.getZ(), t.getZ()
                );
    }

    /** Get the transform representing the identity matrix. This transform does not
     * modify point or vector values when applied.
     * @return transform representing the identity matrix
     */
    public static AffineTransformMatrix3D identity() {
        return IDENTITY_INSTANCE;
    }

    /** Create a transform representing the given translation.
     * @param translation vector containing translation values for each axis
     * @return a new transform representing the given translation
     */
    public static AffineTransformMatrix3D createTranslation(final Vector3D translation) {
        return createTranslation(translation.getX(), translation.getY(), translation.getZ());
    }

    /** Create a transform representing the given translation.
     * @param x translation in the x direction
     * @param y translation in the y direction
     * @param z translation in the z direction
     * @return a new transform representing the given translation
     */
    public static AffineTransformMatrix3D createTranslation(final double x, final double y, final double z) {
        return new AffineTransformMatrix3D(
                    1, 0, 0, x,
                    0, 1, 0, y,
                    0, 0, 1, z
                );
    }

    /** Create a transform representing a scale operation with the given scale factor applied to all axes.
     * @param factor scale factor to apply to all axes
     * @return a new transform representing a uniform scaling in all axes
     */
    public static AffineTransformMatrix3D createScale(final double factor) {
        return createScale(factor, factor, factor);
    }

    /** Create a transform representing a scale operation.
     * @param factors vector containing scale factors for each axis
     * @return a new transform representing a scale operation
     */
    public static AffineTransformMatrix3D createScale(final Vector3D factors) {
        return createScale(factors.getX(), factors.getY(), factors.getZ());
    }

    /** Create a transform representing a scale operation.
     * @param x scale factor for the x axis
     * @param y scale factor for the y axis
     * @param z scale factor for the z axis
     * @return a new transform representing a scale operation
     */
    public static AffineTransformMatrix3D createScale(final double x, final double y, final double z) {
        return new AffineTransformMatrix3D(
                    x, 0, 0, 0,
                    0, y, 0, 0,
                    0, 0, z, 0
                );
    }

    /** Create a transform representing a rotation about the given center point. This is achieved by translating
     * the center to the origin, applying the rotation, and then translating back.
     * @param center the center of rotation
     * @param rotation the rotation to apply
     * @return a new transform representing a rotation about the given center point
     * @see QuaternionRotation#toTransformMatrix()
     */
    public static AffineTransformMatrix3D createRotation(final Vector3D center, final QuaternionRotation rotation) {
        return createTranslation(center.negate())
                .rotate(rotation)
                .translate(center);
    }

    /** Multiply two transform matrices together and return the result as a new transform instance.
     * @param a first transform
     * @param b second transform
     * @return the transform computed as {@code a x b}
     */
    private static AffineTransformMatrix3D multiply(final AffineTransformMatrix3D a, final AffineTransformMatrix3D b) {

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

        return new AffineTransformMatrix3D(
                    c00, c01, c02, c03,
                    c10, c11, c12, c13,
                    c20, c21, c22, c23
                );
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
