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
package org.apache.commons.geometry.spherical.oned;

import org.apache.commons.geometry.core.Transform;

/** Implementation of the {@link Transform} interface for spherical 1D points.
 *
 * <p>Similar to the Euclidean 1D
 * {@link org.apache.commons.geometry.euclidean.oned.AffineTransformMatrix1D AffineTransformMatrix1D},
 * this class performs transformations using an internal 1D affine transformation matrix. In the
 * Euclidean case, the matrix contains a scale factor and a translation. Here, the matrix contains
 * a scale/negation factor that takes the values -1 or +1, and a rotation value. This restriction on
 * the allowed values in the matrix is required in order to fulfill the geometric requirements
 * of the {@link Transform} interface. For example, if arbitrary scaling is allowed, the point {@code 0.5pi}
 * could be scaled by 4 to {@code 2pi}, which is equivalent to {@code 0pi}. However, if the inverse scaling
 * of {@code 1/4} is applied to {@code 0pi}, the result is {@code 0pi} and not {@code 0.5pi}. This breaks
 * the {@link Transform} requirement that transforms be inversible.
 * </p>
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class Transform1S implements Transform<Point1S> {
    /** Static instance representing the identity transform. */
    private static final Transform1S IDENTITY = new Transform1S(1, 0);

    /** Static instance that negates azimuth values. */
    private static final Transform1S NEGATION = new Transform1S(-1, 0);

    /** Value to scale the point azimuth by. This will only be +1/-1. */
    private final double scale;

    /** Value to rotate the point azimuth by. */
    private final double rotate;

    /** Construct a new instance from its transform components.
     * @param scale scale value for the transform; must only be +1 or -1
     * @param rotate rotation value
     */
    private Transform1S(final double scale, final double rotate) {
        this.scale = scale;
        this.rotate = rotate;
    }

    /** Return true if the transform negates the azimuth values of transformed
     * points, regardless of any rotation applied subsequently.
     * @return true if the transform negates the azimuth values of transformed
     *      points
     * @see #preservesOrientation()
     */
    public boolean isNegation() {
        return scale <= 0;
    }

    /** Get the rotation value applied by this instance, in radians.
     * @return the rotation value applied by this instance, in radians.
     */
    public double getRotation() {
        return rotate;
    }

    /** {@inheritDoc} */
    @Override
    public Point1S apply(final Point1S pt) {
        final double az = pt.getAzimuth();
        final double resultAz = (az * scale) + rotate;

        return Point1S.of(resultAz);
    }

    /** {@inheritDoc} */
    @Override
    public boolean preservesOrientation() {
        return !isNegation();
    }

    /** Return a new transform created by pre-multiplying this instance by a transform
     * producing a rotation with the given angle.
     * @param angle angle to rotate, in radians
     * @return a new transform created by pre-multiplying this instance by a transform
     *      producing a rotation with the given angle
     * @see #createRotation(double)
     */
    public Transform1S rotate(final double angle) {
        return premultiply(createRotation(angle));
    }

    /** Return a new transform created by pre-multiplying this instance by a transform
     * that negates azimuth values.
     * @return a new transform created by pre-multiplying this instance by a transform
     *      that negates azimuth values
     */
    public Transform1S negate() {
        return premultiply(createNegation());
    }

    /** Multiply the underlying matrix of this instance by that of the argument, eg,
     * {@code other * this}. The returned transform performs the equivalent of
     * {@code other} followed by {@code this}.
     * @param other transform to multiply with
     * @return a new transform computed by multiplying the matrix of this
     *      instance by that of the argument
     */
    public Transform1S multiply(final Transform1S other) {
        return multiply(this, other);
    }

    /** Multiply the underlying matrix of the argument by that of this instance, eg,
     * {@code this * other}. The returned transform performs the equivalent of {@code this}
     * followed by {@code other}.
     * @param other transform to multiply with
     * @return a new transform computed by multiplying the matrix of the
     *      argument by that of this instance
     */
    public Transform1S premultiply(final Transform1S other) {
        return multiply(other, this);
    }

    /** Return a transform that is the inverse of the current instance. The returned transform
     * will undo changes applied by this instance.
     * @return a transform that is the inverse of the current instance
     */
    public Transform1S inverse() {
        final double invScale = 1.0 / scale;

        final double resultScale = invScale;
        final double resultRotate = -(rotate * invScale);

        return new Transform1S(resultScale, resultRotate);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = (result * prime) + Double.hashCode(scale);
        result = (result * prime) + Double.hashCode(rotate);

        return result;
    }

    /**
     * Return true if the given object is an instance of {@link Transform1S}
     * and all transform element values are exactly equal.
     * @param obj object to test for equality with the current instance
     * @return true if all transform elements are exactly equal; otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Transform1S)) {
            return false;
        }
        final Transform1S other = (Transform1S) obj;

        return Double.compare(scale, other.scale) == 0 &&
                Double.compare(rotate, other.rotate) == 0;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(this.getClass().getSimpleName())
            .append("[negate= ")
            .append(isNegation())
            .append(", rotate= ")
            .append(getRotation())
            .append("]");

        return sb.toString();
    }

    /** Return a transform instance representing the identity transform.
     * @return a transform instance representing the identity transform
     */
    public static Transform1S identity() {
        return IDENTITY;
    }

    /** Return a transform instance that negates azimuth values.
     * @return a transform instance that negates azimuth values.
     */
    public static Transform1S createNegation() {
        return NEGATION;
    }

    /** Return a transform instance that performs a rotation with the given
     * angle.
     * @param angle angle of the rotation, in radians
     * @return a transform instance that performs a rotation with the given
     *      angle
     */
    public static Transform1S createRotation(final double angle) {
        return new Transform1S(1, angle);
    }

    /** Multiply two transforms together as matrices.
     * @param a first transform
     * @param b second transform
     * @return the transform computed as {@code a x b}
     */
    private static Transform1S multiply(final Transform1S a, final Transform1S b) {

        // calculate the matrix elements
        final double resultScale = a.scale * b.scale;
        final double resultRotate = (a.scale * b.rotate) + a.rotate;

        return new Transform1S(resultScale, resultRotate);
    }
}
