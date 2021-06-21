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

import java.util.function.UnaryOperator;

import org.apache.commons.geometry.core.internal.DoubleFunction2N;
import org.apache.commons.geometry.euclidean.AbstractAffineTransformMatrix;
import org.apache.commons.geometry.euclidean.internal.Matrices;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.geometry.euclidean.twod.rotation.Rotation2D;

/** Class using a matrix to represent affine transformations in 2 dimensional Euclidean space.
*
* <p>Instances of this class use a 3x3 matrix for all transform operations.
* The last row of this matrix is always set to the values <code>[0 0 1]</code> and so
* is not stored. Hence, the methods in this class that accept or return arrays always
* use arrays containing 6 elements, instead of 9.
* </p>
*/
public final class AffineTransformMatrix2D extends AbstractAffineTransformMatrix<Vector2D, AffineTransformMatrix2D> {
    /** The number of internal matrix elements. */
    private static final int NUM_ELEMENTS = 6;

    /** String used to start the transform matrix string representation. */
    private static final String MATRIX_START = "[ ";

    /** String used to end the transform matrix string representation. */
    private static final String MATRIX_END = " ]";

    /** String used to separate elements in the matrix string representation. */
    private static final String ELEMENT_SEPARATOR = ", ";

    /** String used to separate rows in the matrix string representation. */
    private static final String ROW_SEPARATOR = "; ";

    /** Shared transform set to the identity matrix. */
    private static final AffineTransformMatrix2D IDENTITY_INSTANCE = new AffineTransformMatrix2D(
                1, 0, 0,
                0, 1, 0
            );

    /** Transform matrix entry <code>m<sub>0,0</sub></code>. */
    private final double m00;
    /** Transform matrix entry <code>m<sub>0,1</sub></code>. */
    private final double m01;
    /** Transform matrix entry <code>m<sub>0,2</sub></code>. */
    private final double m02;

    /** Transform matrix entry <code>m<sub>1,0</sub></code>. */
    private final double m10;
    /** Transform matrix entry <code>m<sub>1,1</sub></code>. */
    private final double m11;
    /** Transform matrix entry <code>m<sub>1,2</sub></code>. */
    private final double m12;

    /**
     * Simple constructor; sets all internal matrix elements.
     * @param m00 matrix entry <code>m<sub>0,0</sub></code>
     * @param m01 matrix entry <code>m<sub>0,1</sub></code>
     * @param m02 matrix entry <code>m<sub>0,2</sub></code>
     * @param m10 matrix entry <code>m<sub>1,0</sub></code>
     * @param m11 matrix entry <code>m<sub>1,1</sub></code>
     * @param m12 matrix entry <code>m<sub>1,2</sub></code>
     */
    private AffineTransformMatrix2D(
            final double m00, final double m01, final double m02,
            final double m10, final double m11, final double m12) {

        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;

        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
    }

    /** Return a 6 element array containing the variable elements from the
     * internal transformation matrix. The elements are in row-major order.
     * The array indices map to the internal matrix as follows:
     * <pre>
     *      [
     *          arr[0],   arr[1],   arr[2],
     *          arr[3],   arr[4],   arr[5],
     *          0         0         1
     *      ]
     * </pre>
     * @return 6 element array containing the variable elements from the
     *      internal transformation matrix
     */
    public double[] toArray() {
        return new double[] {
            m00, m01, m02,
            m10, m11, m12
        };
    }

    /** Apply this transform to the given point, returning the result as a new instance.
    *
    * <p>The transformed point is computed by creating a 3-element column vector from the
    * coordinates in the input and setting the last element to 1. This is then multiplied with the
    * 3x3 transform matrix to produce the transformed point. The {@code 1} in the last position
    * is ignored.
    * <pre>
    *      [ m00  m01  m02 ]     [ x ]     [ x']
    *      [ m10  m11  m12 ]  *  [ y ]  =  [ y']
    *      [ 0    0    1   ]     [ 1 ]     [ 1 ]
    * </pre>
    */
    @Override
    public Vector2D apply(final Vector2D pt) {
        final double x = pt.getX();
        final double y = pt.getY();

        return Vector2D.of(
                applyX(x, y),
                applyY(x, y));
    }

    /** Apply this transform to the given point coordinates and return the transformed
     * x value. The return value is equal to
     * <code>(x * m<sub>00</sub>) + (y * m<sub>01</sub>) + m<sub>02</sub></code>.
     * @param x x coordinate value
     * @param y y coordinate value
     * @return transformed x coordinate value
     * @see #apply(Vector2D)
     */
    public double applyX(final double x, final double y) {
        return applyVectorX(x, y) + m02;
    }

    /** Apply this transform to the given point coordinates and return the transformed
     * y value. The return value is equal to
     * <code>(x * m<sub>10</sub>) + (y * m<sub>11</sub>) + m<sub>12</sub></code>.
     * @param x x coordinate value
     * @param y y coordinate value
     * @return transformed y coordinate value
     * @see #apply(Vector2D)
     */
    public double applyY(final double x, final double y) {
        return applyVectorY(x, y) + m12;
    }

    /** {@inheritDoc}
    *
    *  <p>The transformed vector is computed by creating a 3-element column vector from the
    * coordinates in the input and setting the last element to 0. This is then multiplied with the
    * 3x3 transform matrix to produce the transformed vector. The {@code 0} in the last position
    * is ignored.
    * <pre>
    *      [ m00  m01  m02 ]     [ x ]     [ x']
    *      [ m10  m11  m12 ]  *  [ y ]  =  [ y']
    *      [ 0    0    1   ]     [ 0 ]     [ 0 ]
    * </pre>
    *
    * @see #applyDirection(Vector2D)
    */
    @Override
    public Vector2D applyVector(final Vector2D vec) {
        return applyVector(vec, Vector2D::of);
    }

    /** Apply this transform to the given vector coordinates, ignoring translations, and
     * return the transformed x value. The return value is equal to
     * <code>(x * m<sub>00</sub>) + (y * m<sub>01</sub>)</code>.
     * @param x x coordinate value
     * @param y y coordinate value
     * @return transformed x coordinate value
     * @see #applyVector(Vector2D)
     */
    public double applyVectorX(final double x, final double y) {
        return Vectors.linearCombination(m00, x, m01, y);
    }

    /** Apply this transform to the given vector coordinates, ignoring translations, and
     * return the transformed y value. The return value is equal to
     * <code>(x * m<sub>10</sub>) + (y * m<sub>11</sub>)</code>.
     * @param x x coordinate value
     * @param y y coordinate value
     * @return transformed y coordinate value
     * @see #applyVector(Vector2D)
     */
    public double applyVectorY(final double x, final double y) {
        return Vectors.linearCombination(m10, x, m11, y);
    }

    /** {@inheritDoc}
     * @see #applyVector(Vector2D)
     */
    @Override
    public Vector2D.Unit applyDirection(final Vector2D vec) {
        return applyVector(vec, Vector2D.Unit::from);
    }

    /** {@inheritDoc} */
    @Override
    public double determinant() {
        return Matrices.determinant(
                m00, m01,
                m10, m11
            );
    }

    /** {@inheritDoc}
     *
     * <p><strong>Example</strong>
     * <pre>
     *      [ a, b, c ]   [ a, b, 0 ]
     *      [ d, e, f ] &rarr; [ d, e, 0 ]
     *      [ 0, 0, 1 ]   [ 0, 0, 1 ]
     * </pre>
     */
    @Override
    public AffineTransformMatrix2D linear() {
        return new AffineTransformMatrix2D(
                m00, m01, 0.0,
                m10, m11, 0.0);
    }

    /** {@inheritDoc}
     *
     * <p><strong>Example</strong>
     * <pre>
     *      [ a, b, c ]   [ a, d, 0 ]
     *      [ d, e, f ] &rarr; [ b, e, 0 ]
     *      [ 0, 0, 1 ]   [ 0, 0, 1 ]
     * </pre>
     */
    @Override
    public AffineTransformMatrix2D linearTranspose() {
        return new AffineTransformMatrix2D(
                m00, m10, 0.0,
                m01, m11, 0.0);
    }

    /** Apply a translation to the current instance, returning the result as a new transform.
     * @param translation vector containing the translation values for each axis
     * @return a new transform containing the result of applying a translation to
     *      the current instance
     */
    public AffineTransformMatrix2D translate(final Vector2D translation) {
        return translate(translation.getX(), translation.getY());
    }

    /** Apply a translation to the current instance, returning the result as a new transform.
     * @param x translation in the x direction
     * @param y translation in the y direction
     * @return a new transform containing the result of applying a translation to
     *      the current instance
     */
    public AffineTransformMatrix2D translate(final double x, final double y) {
        return new AffineTransformMatrix2D(
                    m00, m01, m02 + x,
                    m10, m11, m12 + y
                );
    }

    /** Apply a scale operation to the current instance, returning the result as a new transform.
     * @param factor the scale factor to apply to all axes
     * @return a new transform containing the result of applying a scale operation to
     *      the current instance
     */
    public AffineTransformMatrix2D scale(final double factor) {
        return scale(factor, factor);
    }

    /** Apply a scale operation to the current instance, returning the result as a new transform.
     * @param scaleFactors vector containing scale factors for each axis
     * @return a new transform containing the result of applying a scale operation to
     *      the current instance
     */
    public AffineTransformMatrix2D scale(final Vector2D scaleFactors) {
        return scale(scaleFactors.getX(), scaleFactors.getY());
    }

    /** Apply a scale operation to the current instance, returning the result as a new transform.
     * @param x scale factor for the x axis
     * @param y scale factor for the y axis
     * @return a new transform containing the result of applying a scale operation to
     *      the current instance
     */
    public AffineTransformMatrix2D scale(final double x, final double y) {
        return new AffineTransformMatrix2D(
                m00 * x, m01 * x, m02 * x,
                m10 * y, m11 * y, m12 * y
            );
    }

    /** Apply a <em>counterclockwise</em> rotation to the current instance, returning the result as a
     * new transform.
     * @param angle the angle of counterclockwise rotation in radians
     * @return a new transform containing the result of applying a rotation to the
     *      current instance
     * @see Rotation2D#of(double)
     */
    public AffineTransformMatrix2D rotate(final double angle) {
        return rotate(Rotation2D.of(angle));
    }

    /** Apply a <em>counterclockwise</em> rotation to the current instance, returning the result as a
     *  new transform.
     * @param rotation the rotation to apply
     * @return a new transform containing the result of applying the rotation to the
     *      current instance
     */
    public AffineTransformMatrix2D rotate(final Rotation2D rotation) {
        return multiply(rotation.toMatrix(), this);
    }

    /** Apply a <em>counterclockwise</em> rotation about the given center point to the current instance,
     * returning the result as a new transform. This is accomplished by translating the center to the origin,
     * applying the rotation, and then translating back.
     * @param center the center of rotation
     * @param angle the angle of counterclockwise rotation in radians
     * @return a new transform containing the result of applying a rotation about the given
     *      center point to the current instance
     */
    public AffineTransformMatrix2D rotate(final Vector2D center, final double angle) {
        return multiply(createRotation(center, angle), this);
    }

    /** Apply a <em>counterclockwise</em> rotation about the given center point to the current instance,
     * returning the result as a new transform. This is accomplished by translating the center to the origin,
     * applying the rotation, and then translating back.
     * @param center the center of rotation
     * @param rotation the rotation to apply
     * @return a new transform containing the result of applying a rotation about the given
     *      center point to the current instance
     */
    public AffineTransformMatrix2D rotate(final Vector2D center, final Rotation2D rotation) {
        // use to raw angle method to avoid matrix multiplication
        return rotate(center, rotation.getAngle());
    }

    /** Apply a shear to the current instance, returning the result as a new transform.
     * @param shx multiplier by which coordinates are shifted along the positive x-axis as a factor of their
     *      y coordinate; a value of 0 indicates no shift along the x-axis
     * @param shy multiplier by which coordinates are shifted along the positive y-axis as a factor of their
     *      x coordinate; a value of 0 indicates no shift along the y-axis
     * @return a new transform containing the result of applying a shear to the current instance
     */
    public AffineTransformMatrix2D shear(final double shx, final double shy) {
        return multiply(createShear(shx, shy), this);
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
    public AffineTransformMatrix2D multiply(final AffineTransformMatrix2D m) {
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
    public AffineTransformMatrix2D premultiply(final AffineTransformMatrix2D m) {
        return multiply(m, this);
    }

    /** {@inheritDoc}
    *
    * @throws IllegalStateException if the matrix cannot be inverted
    */
    @Override
    public AffineTransformMatrix2D inverse() {

        // Our full matrix is 3x3 but we can significantly reduce the amount of computations
        // needed here since we know that our last row is [0 0 1].

        final double det = Matrices.checkDeterminantForInverse(determinant());

        // validate the remaining matrix elements that were not part of the determinant
        Matrices.checkElementForInverse(m02);
        Matrices.checkElementForInverse(m12);

        // compute the necessary elements of the cofactor matrix
        // (we need all but the last column)

        final double invDet = 1.0 / det;

        final double c00 = invDet * m11;
        final double c01 = -invDet * m10;

        final double c10 = -invDet * m01;
        final double c11 = invDet * m00;

        final double c20 = invDet * Matrices.determinant(m01, m02, m11, m12);
        final double c21 = -invDet * Matrices.determinant(m00, m02, m10, m12);

        return new AffineTransformMatrix2D(
                    c00, c10, c20,
                    c01, c11, c21
                );
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = (result * prime) + (Double.hashCode(m00) - Double.hashCode(m01) + Double.hashCode(m02));
        result = (result * prime) + (Double.hashCode(m10) - Double.hashCode(m11) + Double.hashCode(m12));

        return result;
    }

    /**
     * Return true if the given object is an instance of {@link AffineTransformMatrix2D}
     * and all matrix element values are exactly equal.
     * @param obj object to test for equality with the current instance
     * @return true if all transform matrix elements are exactly equal; otherwise false
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AffineTransformMatrix2D)) {
            return false;
        }

        final AffineTransformMatrix2D other = (AffineTransformMatrix2D) obj;

        return Double.compare(this.m00, other.m00) == 0 &&
                Double.compare(this.m01, other.m01) == 0 &&
                Double.compare(this.m02, other.m02) == 0 &&

                Double.compare(this.m10, other.m10) == 0 &&
                Double.compare(this.m11, other.m11) == 0 &&
                Double.compare(this.m12, other.m12) == 0;
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
            .append(ROW_SEPARATOR)

            .append(m10)
            .append(ELEMENT_SEPARATOR)
            .append(m11)
            .append(ELEMENT_SEPARATOR)
            .append(m12)

            .append(MATRIX_END);

        return sb.toString();
    }

    /** Multiplies the given vector by the 2x2 linear transformation matrix contained in the
     * upper-right corner of the affine transformation matrix. This applies all transformation
     * operations except for translations. The computed coordinates are passed to the given
     * factory function.
     * @param <T> factory output type
     * @param vec the vector to transform
     * @param factory the factory instance that will be passed the transformed coordinates
     * @return the factory return value
     */
    private <T> T applyVector(final Vector2D vec, final DoubleFunction2N<T> factory) {
        final double x = vec.getX();
        final double y = vec.getY();

        return factory.apply(
                applyVectorX(x, y),
                applyVectorY(x, y));
    }

    /** Get a new transform with the given matrix elements. The array must contain 6 elements.
     * @param arr 6-element array containing values for the variable entries in the
     *      transform matrix
     * @return a new transform initialized with the given matrix values
     * @throws IllegalArgumentException if the array does not have 6 elements
     */
    public static AffineTransformMatrix2D of(final double... arr) {
        if (arr.length != NUM_ELEMENTS) {
            throw new IllegalArgumentException("Dimension mismatch: " + arr.length + " != " + NUM_ELEMENTS);
        }

        return new AffineTransformMatrix2D(
                    arr[0], arr[1], arr[2],
                    arr[3], arr[4], arr[5]
                );
    }

    /** Construct a new transform representing the given function. The function is sampled at
     * the origin and along each axis and a matrix is created to perform the transformation.
     * @param fn function to create a transform matrix from
     * @return a transform matrix representing the given function
     * @throws IllegalArgumentException if the given function does not represent a valid
     *      affine transform
     */
    public static AffineTransformMatrix2D from(final UnaryOperator<Vector2D> fn) {
        final Vector2D tPlusX = fn.apply(Vector2D.Unit.PLUS_X);
        final Vector2D tPlusY = fn.apply(Vector2D.Unit.PLUS_Y);
        final Vector2D tZero = fn.apply(Vector2D.ZERO);

        final Vector2D u = tPlusX.subtract(tZero);
        final Vector2D v = tPlusY.subtract(tZero);

        final AffineTransformMatrix2D mat =  AffineTransformMatrix2D.fromColumnVectors(u, v, tZero);

        final double det = mat.determinant();
        if (!Vectors.isRealNonZero(det)) {
            throw new IllegalArgumentException("Transform function is invalid: matrix determinant is " + det);
        }

        return mat;
    }

    /** Get a new transform create from the given column vectors. The returned transform
     * does not include any translation component.
     * @param u first column vector; this corresponds to the first basis vector
     *      in the coordinate frame
     * @param v second column vector; this corresponds to the second basis vector
     *      in the coordinate frame
     * @return a new transform with the given column vectors
     */
    public static AffineTransformMatrix2D fromColumnVectors(final Vector2D u, final Vector2D v) {
        return fromColumnVectors(u, v, Vector2D.ZERO);
    }

    /** Get a new transform created from the given column vectors.
     * @param u first column vector; this corresponds to the first basis vector
     *      in the coordinate frame
     * @param v second column vector; this corresponds to the second basis vector
     *      in the coordinate frame
     * @param t third column vector; this corresponds to the translation of the transform
     * @return a new transform with the given column vectors
     */
    public static AffineTransformMatrix2D fromColumnVectors(final Vector2D u, final Vector2D v, final Vector2D t) {
        return new AffineTransformMatrix2D(
                    u.getX(), v.getX(), t.getX(),
                    u.getY(), v.getY(), t.getY()
                );
    }

    /** Get the transform representing the identity matrix. This transform does not
     * modify point or vector values when applied.
     * @return transform representing the identity matrix
     */
    public static AffineTransformMatrix2D identity() {
        return IDENTITY_INSTANCE;
    }

    /** Create a transform representing the given translation.
     * @param translation vector containing translation values for each axis
     * @return a new transform representing the given translation
     */
    public static AffineTransformMatrix2D createTranslation(final Vector2D translation) {
        return createTranslation(translation.getX(), translation.getY());
    }

    /** Create a transform representing the given translation.
     * @param x translation in the x direction
     * @param y translation in the y direction
     * @return a new transform representing the given translation
     */
    public static AffineTransformMatrix2D createTranslation(final double x, final double y) {
        return new AffineTransformMatrix2D(
                    1, 0, x,
                    0, 1, y
                );
    }

    /** Create a transform representing a scale operation with the given scale factor applied to all axes.
     * @param factor scale factor to apply to all axes
     * @return a new transform representing a uniform scaling in all axes
     */
    public static AffineTransformMatrix2D createScale(final double factor) {
        return createScale(factor, factor);
    }

    /** Create a transform representing a scale operation.
     * @param factors vector containing scale factors for each axis
     * @return a new transform representing a scale operation
     */
    public static AffineTransformMatrix2D createScale(final Vector2D factors) {
        return createScale(factors.getX(), factors.getY());
    }

    /** Create a transform representing a scale operation.
     * @param x scale factor for the x axis
     * @param y scale factor for the y axis
     * @return a new transform representing a scale operation
     */
    public static AffineTransformMatrix2D createScale(final double x, final double y) {
        return new AffineTransformMatrix2D(
                    x, 0, 0,
                    0, y, 0
                );
    }

    /** Create a transform representing a <em>counterclockwise</em> rotation of {@code angle}
     * radians around the origin.
     * @param angle the angle of rotation in radians
     * @return a new transform representing the rotation
     * @see Rotation2D#toMatrix()
     */
    public static AffineTransformMatrix2D createRotation(final double angle) {
        return Rotation2D.of(angle).toMatrix();
    }

    /** Create a transform representing a <em>counterclockwise</em> rotation of {@code angle}
     * radians around the given center point. This is accomplished by translating the center point
     * to the origin, applying the rotation, and then translating back.
     * @param center the center of rotation
     * @param angle the angle of rotation in radians
     * @return a new transform representing the rotation about the given center
     */
    public static AffineTransformMatrix2D createRotation(final Vector2D center, final double angle) {
        // it's possible to do this using Rotation2D to create the rotation matrix but we
        // can avoid the matrix multiplications by simply doing everything in-line here
        final double x = center.getX();
        final double y = center.getY();

        final double sin = Math.sin(angle);
        final double cos = Math.cos(angle);

        return new AffineTransformMatrix2D(
                cos, -sin, (-x * cos) + (y * sin) + x,
                sin, cos, (-x * sin) - (y * cos) + y
            );
    }

    /** Create a transform representing a <em>counterclockwise</em> rotation around the given center point.
     * This is accomplished by translating the center point to the origin, applying the rotation, and then
     * translating back.
     * @param center the center of rotation
     * @param rotation the rotation to apply
     * @return a new transform representing the rotation about the given center
     */
    public static AffineTransformMatrix2D createRotation(final Vector2D center, final Rotation2D rotation) {
        return createRotation(center, rotation.getAngle());
    }

    /** Create a transform representing a shear operation. The returned instance contains the
     * matrix values
     * <pre>
     *      [ 1,    shx,  0 ]
     *      [ shy,  1,    0 ]
     *      [ 0,    0,    0 ]
     * </pre>
     * @param shx multiplier by which coordinates are shifted along the positive x-axis as a factor of their
     *      y coordinate; a value of 0 indicates no shift along the x-axis
     * @param shy multiplier by which coordinates are shifted along the positive y-axis as a factor of their
     *      x coordinate; a value of 0 indicates no shift along the y-axis
     * @return a new transform representing the shear operation
     */
    public static AffineTransformMatrix2D createShear(final double shx, final double shy) {
        return new AffineTransformMatrix2D(
                    1, shx, 0,
                    shy, 1, 0
                );
    }

    /** Multiply two transform matrices together.
     * @param a first transform
     * @param b second transform
     * @return the transform computed as {@code a x b}
     */
    private static AffineTransformMatrix2D multiply(final AffineTransformMatrix2D a,
            final AffineTransformMatrix2D b) {

        final double c00 = Vectors.linearCombination(a.m00, b.m00, a.m01, b.m10);
        final double c01 = Vectors.linearCombination(a.m00, b.m01, a.m01, b.m11);
        final double c02 = Vectors.linearCombination(a.m00, b.m02, a.m01, b.m12) + a.m02;

        final double c10 = Vectors.linearCombination(a.m10, b.m00, a.m11, b.m10);
        final double c11 = Vectors.linearCombination(a.m10, b.m01, a.m11, b.m11);
        final double c12 = Vectors.linearCombination(a.m10, b.m02, a.m11, b.m12) + a.m12;

        return new AffineTransformMatrix2D(
                    c00, c01, c02,
                    c10, c11, c12
                );
    }
}
