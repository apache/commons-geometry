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

import java.util.function.UnaryOperator;

import org.apache.commons.geometry.euclidean.AbstractAffineTransformMatrix;
import org.apache.commons.geometry.euclidean.internal.Matrices;
import org.apache.commons.geometry.euclidean.internal.Vectors;

/** Class using a matrix to represent affine transformations in 1 dimensional Euclidean space.
*
* <p>Instances of this class use a 2x2 matrix for all transform operations.
* The last row of this matrix is always set to the values <code>[0 1]</code> and so
* is not stored. Hence, the methods in this class that accept or return arrays always
* use arrays containing 2 elements, instead of 4.
* </p>
*/
public final class AffineTransformMatrix1D extends AbstractAffineTransformMatrix<Vector1D, AffineTransformMatrix1D> {
    /** The number of internal matrix elements. */
    private static final int NUM_ELEMENTS = 2;

    /** String used to start the transform matrix string representation. */
    private static final String MATRIX_START = "[ ";

    /** String used to end the transform matrix string representation. */
    private static final String MATRIX_END = " ]";

    /** String used to separate elements in the matrix string representation. */
    private static final String ELEMENT_SEPARATOR = ", ";

    /** Shared transform set to the identity matrix. */
    private static final AffineTransformMatrix1D IDENTITY_INSTANCE = new AffineTransformMatrix1D(1, 0);

    /** Transform matrix entry <code>m<sub>0,0</sub></code>. */
    private final double m00;
    /** Transform matrix entry <code>m<sub>0,1</sub></code>. */
    private final double m01;

    /**
     * Simple constructor; sets all internal matrix elements.
     * @param m00 matrix entry <code>m<sub>0,0</sub></code>
     * @param m01 matrix entry <code>m<sub>0,1</sub></code>
     */
    private AffineTransformMatrix1D(final double m00, final double m01) {
        this.m00 = m00;
        this.m01 = m01;
    }

    /** Return a 2 element array containing the variable elements from the
     * internal transformation matrix. The elements are in row-major order.
     * The array indices map to the internal matrix as follows:
     * <pre>
     *      [
     *          arr[0],   arr[1],
     *          0         1
     *      ]
     * </pre>
     * @return 2 element array containing the variable elements from the
     *      internal transformation matrix
     */
    public double[] toArray() {
        return new double[] {
            m00, m01
        };
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D apply(final Vector1D vec) {
        return Vector1D.of(applyX(vec.getX()));
    }

    /** Apply this transform to the given point coordinate and return the transformed
     * x value. The return value is equal to <code>(x * m<sub>00</sub>) + m<sub>01</sub></code>.
     * @param x x coordinate value
     * @return transformed x coordinate value
     * @see #apply(Vector1D)
     */
    public double applyX(final double x) {
        return applyVectorX(x) + m01;
    }

    /** {@inheritDoc}
     * @see #applyDirection(Vector1D)
     */
    @Override
    public Vector1D applyVector(final Vector1D vec) {
        return Vector1D.of(applyVectorX(vec.getX()));
    }

    /** Apply this transform to the given vector coordinate, ignoring translations, and
     * return the transformed x value. The return value is equal to <code>x * m<sub>00</sub></code>.
     * @param x x coordinate value
     * @return transformed x coordinate value
     * @see #applyVector(Vector1D)
     */
    public double applyVectorX(final double x) {
        return x * m00;
    }

    /** {@inheritDoc}
     * @see #applyVector(Vector1D)
     */
    @Override
    public Vector1D.Unit applyDirection(final Vector1D vec) {
        return Vector1D.Unit.from(applyVectorX(vec.getX()));
    }

    /** {@inheritDoc} */
    @Override
    public double determinant() {
        return m00;
    }

    /** {@inheritDoc}
     *
     * <p><strong>Example</strong>
     * <pre>
     *      [ a, b ]   [ a, 0 ]
     *      [ 0, 1 ] &rarr; [ 0, 1 ]
     * </pre>
     */
    @Override
    public AffineTransformMatrix1D linear() {
        return new AffineTransformMatrix1D(m00, 0.0);
    }

    /** {@inheritDoc}
     *
     * <p>In the one dimensional case, this is exactly the same as {@link #linear()}.</p>
     *
     * <p><strong>Example</strong>
     * <pre>
     *      [ a, b ]   [ a, 0 ]
     *      [ 0, 1 ] &rarr; [ 0, 1 ]
     * </pre>
     */
    @Override
    public AffineTransformMatrix1D linearTranspose() {
        return linear();
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
    public AffineTransformMatrix1D translate(final Vector1D translation) {
        return translate(translation.getX());
    }

    /** Get a new transform containing the result of applying a translation logically after
     * the transformation represented by the current instance. This is achieved by
     * creating a new translation transform and pre-multiplying it with the current
     * instance. In other words, the returned transform contains the matrix
     * <code>B * A</code>, where <code>A</code> is the current matrix and <code>B</code>
     * is the matrix representing the given translation.
     * @param x translation in the x direction
     * @return a new transform containing the result of applying a translation to
     *      the current instance
     */
    public AffineTransformMatrix1D translate(final double x) {
        return new AffineTransformMatrix1D(m00, m01 + x);
    }

    /** Get a new transform containing the result of applying a scale operation
     * logically after the transformation represented by the current instance.
     * This is achieved by creating a new scale transform and pre-multiplying it with the current
     * instance. In other words, the returned transform contains the matrix
     * <code>B * A</code>, where <code>A</code> is the current matrix and <code>B</code>
     * is the matrix representing the given scale operation.
     * @param scaleFactor vector containing scale factors for each axis
     * @return a new transform containing the result of applying a scale operation to
     *      the current instance
     */
    public AffineTransformMatrix1D scale(final Vector1D scaleFactor) {
        return scale(scaleFactor.getX());
    }

    /** Get a new transform containing the result of applying a scale operation
     * logically after the transformation represented by the current instance.
     * This is achieved by creating a new scale transform and pre-multiplying it with the current
     * instance. In other words, the returned transform contains the matrix
     * <code>B * A</code>, where <code>A</code> is the current matrix and <code>B</code>
     * is the matrix representing the given scale operation.
     * @param x scale factor
     * @return a new transform containing the result of applying a scale operation to
     *      the current instance
     */
    public AffineTransformMatrix1D scale(final double x) {
        return new AffineTransformMatrix1D(m00 * x, m01 * x);
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
    public AffineTransformMatrix1D multiply(final AffineTransformMatrix1D m) {
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
    public AffineTransformMatrix1D premultiply(final AffineTransformMatrix1D m) {
        return multiply(m, this);
    }

    /** {@inheritDoc}
     *
     * @throws IllegalStateException if the matrix cannot be inverted
     */
    @Override
    public AffineTransformMatrix1D inverse() {

        final double det = Matrices.checkDeterminantForInverse(determinant());

        Matrices.checkElementForInverse(m01);

        final double invDet = 1.0 / det;

        final double c01 = -(this.m01 * invDet);

        return new AffineTransformMatrix1D(invDet, c01);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = (result * prime) + Double.hashCode(m00);
        result = (result * prime) + Double.hashCode(m01);

        return result;
    }

    /**
     * Return true if the given object is an instance of {@link AffineTransformMatrix1D}
     * and all matrix element values are exactly equal.
     * @param obj object to test for equality with the current instance
     * @return true if all transform matrix elements are exactly equal; otherwise false
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AffineTransformMatrix1D)) {
            return false;
        }
        final AffineTransformMatrix1D other = (AffineTransformMatrix1D) obj;

        return Double.compare(this.m00, other.m00) == 0 &&
                Double.compare(this.m01, other.m01) == 0;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(MATRIX_START)

            .append(m00)
            .append(ELEMENT_SEPARATOR)
            .append(m01)

            .append(MATRIX_END);

        return sb.toString();
    }

    /** Get a new transform with the given matrix elements. The array must contain 2 elements.
     * The first element in the array represents the scale factor for the transform and the
     * second represents the translation.
     * @param arr 2-element array containing values for the variable entries in the
     *      transform matrix
     * @return a new transform initialized with the given matrix values
     * @throws IllegalArgumentException if the array does not have 2 elements
     */
    public static AffineTransformMatrix1D of(final double... arr) {
        if (arr.length != NUM_ELEMENTS) {
            throw new IllegalArgumentException("Dimension mismatch: " + arr.length + " != " + NUM_ELEMENTS);
        }

        return new AffineTransformMatrix1D(arr[0], arr[1]);
    }

    /** Construct a new transform representing the given function. The function is sampled at
     * the points zero and one and a matrix is created to perform the transformation.
     * @param fn function to create a transform matrix from
     * @return a transform matrix representing the given function
     * @throws IllegalArgumentException if the given function does not represent a valid
     *      affine transform
     */
    public static AffineTransformMatrix1D from(final UnaryOperator<Vector1D> fn) {
        final Vector1D tOne = fn.apply(Vector1D.Unit.PLUS);
        final Vector1D tZero = fn.apply(Vector1D.ZERO);

        final double scale = tOne.subtract(tZero).getX();
        final double translate = tZero.getX();

        final AffineTransformMatrix1D mat =  AffineTransformMatrix1D.of(scale, translate);

        final double det = mat.determinant();
        if (!Vectors.isRealNonZero(det)) {
            throw new IllegalArgumentException("Transform function is invalid: matrix determinant is " + det);
        }

        return mat;
    }

    /** Get the transform representing the identity matrix. This transform does not
     * modify point or vector values when applied.
     * @return transform representing the identity matrix
     */
    public static AffineTransformMatrix1D identity() {
        return IDENTITY_INSTANCE;
    }

    /** Get a transform representing the given translation.
     * @param translation vector containing translation values for each axis
     * @return a new transform representing the given translation
     */
    public static AffineTransformMatrix1D createTranslation(final Vector1D translation) {
        return createTranslation(translation.getX());
    }

    /** Get a transform representing the given translation.
     * @param x translation in the x direction
     * @return a new transform representing the given translation
     */
    public static AffineTransformMatrix1D createTranslation(final double x) {
        return new AffineTransformMatrix1D(1, x);
    }

    /** Get a transform representing a scale operation.
     * @param factor vector containing the scale factor
     * @return a new transform representing a scale operation
     */
    public static AffineTransformMatrix1D createScale(final Vector1D factor) {
        return createScale(factor.getX());
    }

    /** Get a transform representing a scale operation.
     * @param factor scale factor
     * @return a new transform representing a scale operation
     */
    public static AffineTransformMatrix1D createScale(final double factor) {
        return new AffineTransformMatrix1D(factor, 0);
    }

    /** Multiply two transform matrices together.
     * @param a first transform
     * @param b second transform
     * @return the transform computed as {@code a x b}
     */
    private static AffineTransformMatrix1D multiply(final AffineTransformMatrix1D a,
            final AffineTransformMatrix1D b) {

        // calculate the matrix elements
        final double c00 = a.m00 * b.m00;
        final double c01 = (a.m00 * b.m01) + a.m01;

        return new AffineTransformMatrix1D(c00, c01);
    }
}
