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
package org.apache.commons.geometry.spherical.twod;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;

/** Implementation of the {@link Transform} interface for spherical 2D points.
 *
 * <p>This class uses an {@link AffineTransformMatrix3D} to perform spherical point transforms
 * in Euclidean 3D space.</p>
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class Transform2S implements Transform<Point2S> {
    /** Static instance representing the identity transform. */
    private static final Transform2S IDENTITY = new Transform2S(AffineTransformMatrix3D.identity());

    /** Static transform instance that reflects across the x-y plane. */
    private static final AffineTransformMatrix3D XY_PLANE_REFLECTION = AffineTransformMatrix3D.createScale(1, 1, -1);

    /** Euclidean transform matrix underlying the spherical transform. */
    private final AffineTransformMatrix3D euclideanTransform;

    /** Construct a new instance from its underlying Euclidean transform.
     * @param euclideanTransform underlying Euclidean transform
     */
    private Transform2S(final AffineTransformMatrix3D euclideanTransform) {
        this.euclideanTransform = euclideanTransform;
    }

    /** Get the Euclidean transform matrix underlying the spherical transform.
     * @return the Euclidean transform matrix underlying the spherical transform
     */
    public AffineTransformMatrix3D getEuclideanTransform() {
        return euclideanTransform;
    }

    /** {@inheritDoc} */
    @Override
    public Point2S apply(final Point2S pt) {
        final Vector3D vec = pt.getVector();
        return Point2S.from(euclideanTransform.apply(vec));
    }

    /** {@inheritDoc} */
    @Override
    public boolean preservesOrientation() {
        return euclideanTransform.preservesOrientation();
    }

    /** Return a new instance representing the inverse transform operation
     * of this instance.
     * @return a transform representing the inverse of this instance
     */
    public Transform2S inverse() {
        return new Transform2S(euclideanTransform.inverse());
    }

    /** Apply a rotation of {@code angle} radians around the given point to this instance.
     * @param pt point to rotate around
     * @param angle rotation angle in radians
     * @return transform resulting from applying the specified rotation to this instance
     */
    public Transform2S rotate(final Point2S pt, final double angle) {
        return premultiply(createRotation(pt, angle));
    }

    /** Apply a rotation of {@code angle} radians around the given 3D axis to this instance.
     * @param axis 3D axis of rotation
     * @param angle rotation angle in radians
     * @return transform resulting from applying the specified rotation to this instance
     */
    public Transform2S rotate(final Vector3D axis, final double angle) {
        return premultiply(createRotation(axis, angle));
    }

    /** Apply the given quaternion rotation to this instance.
     * @param quaternion quaternion rotation to apply
     * @return transform resulting from applying the specified rotation to this instance
     */
    public Transform2S rotate(final QuaternionRotation quaternion) {
        return premultiply(createRotation(quaternion));
    }

    /** Apply a reflection across the equatorial plane defined by the given pole point
     * to this instance.
     * @param pole pole point defining the equatorial reflection plane
     * @return transform resulting from applying the specified reflection to this instance
     */
    public Transform2S reflect(final Point2S pole) {
        return premultiply(createReflection(pole));
    }

    /** Apply a reflection across the equatorial plane defined by the given pole vector
     * to this instance.
     * @param poleVector pole vector defining the equatorial reflection plane
     * @return transform resulting from applying the specified reflection to this instance
     */
    public Transform2S reflect(final Vector3D poleVector) {
        return premultiply(createReflection(poleVector));
    }

    /** Multiply the underlying Euclidean transform of this instance by that of the argument, eg,
     * {@code other * this}. The returned transform performs the equivalent of
     * {@code other} followed by {@code this}.
     * @param other transform to multiply with
     * @return a new transform computed by multiplying the matrix of this
     *      instance by that of the argument
     * @see AffineTransformMatrix3D#multiply(AffineTransformMatrix3D)
     */
    public Transform2S multiply(final Transform2S other) {
        return multiply(this, other);
    }

    /** Multiply the underlying Euclidean transform matrix of the argument by that of this instance, eg,
     * {@code this * other}. The returned transform performs the equivalent of {@code this}
     * followed by {@code other}.
     * @param other transform to multiply with
     * @return a new transform computed by multiplying the matrix of the
     *      argument by that of this instance
     * @see AffineTransformMatrix3D#premultiply(AffineTransformMatrix3D)
     */
    public Transform2S premultiply(final Transform2S other) {
        return multiply(other, this);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return euclideanTransform.hashCode();
    }

    /**
     * Return true if the given object is an instance of {@link Transform2S}
     * and the underlying Euclidean transform matrices are exactly equal.
     * @param obj object to test for equality with the current instance
     * @return true if the underlying transform matrices are exactly equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Transform2S)) {
            return false;
        }
        final Transform2S other = (Transform2S) obj;

        return euclideanTransform.equals(other.euclideanTransform);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(this.getClass().getSimpleName())
            .append("[euclideanTransform= ")
            .append(getEuclideanTransform())
            .append("]");

        return sb.toString();
    }

    /** Return an instance representing the identity transform. This transform is guaranteed
     * to return an <em>equivalent</em> (ie, co-located) point for any input point. However, the
     * points are not guaranteed to contain exactly equal coordinates. For example, at the poles, an
     * infinite number of points exist that vary only in the azimuth coordinate. When one of these
     * points is transformed by this identity transform, the returned point may contain a different
     * azimuth value from the input, but it will still represent the same location in space.
     * @return an instance representing the identity transform
     */
    public static Transform2S identity() {
        return IDENTITY;
    }

    /** Create a transform that rotates the given angle around {@code pt}.
     * @param pt point to rotate around
     * @param angle angle of rotation in radians
     * @return a transform that rotates the given angle around {@code pt}
     */
    public static Transform2S createRotation(final Point2S pt, final double angle) {
        return createRotation(pt.getVector(), angle);
    }

    /** Create a transform that rotates the given angle around {@code axis}.
     * @param axis 3D axis of rotation
     * @param angle angle of rotation in radians
     * @return a transform that rotates the given angle {@code axis}
     */
    public static Transform2S createRotation(final Vector3D axis, final double angle) {
        return createRotation(QuaternionRotation.fromAxisAngle(axis, angle));
    }

    /** Create a transform that performs the given 3D rotation.
     * @param quaternion quaternion instance representing the 3D rotation
     * @return a transform that performs the given 3D rotation
     */
    public static Transform2S createRotation(final QuaternionRotation quaternion) {
        return new Transform2S(quaternion.toMatrix());
    }

    /** Create a transform that performs a reflection across the equatorial plane
     * defined by the given pole point.
     * @param pole pole point defining the equatorial reflection plane
     * @return a transform that performs a reflection across the equatorial plane
     *      defined by the given pole point
     */
    public static Transform2S createReflection(final Point2S pole) {
        return createReflection(pole.getVector());
    }

    /** Create a transform that performs a reflection across the equatorial plane
     * defined by the given pole point.
     * @param poleVector pole vector defining the equatorial reflection plane
     * @return a transform that performs a reflection across the equatorial plane
     *      defined by the given pole point
     */
    public static Transform2S createReflection(final Vector3D poleVector) {
        final QuaternionRotation quat = QuaternionRotation.createVectorRotation(poleVector, Vector3D.Unit.PLUS_Z);

        final AffineTransformMatrix3D matrix = quat.toMatrix()
                .premultiply(XY_PLANE_REFLECTION)
                .premultiply(quat.inverse().toMatrix());

        return new Transform2S(matrix);
    }

    /** Multiply the Euclidean transform matrices of the arguments together.
     * @param a first transform
     * @param b second transform
     * @return the transform computed as {@code a x b}
     */
    private static Transform2S multiply(final Transform2S a, final Transform2S b) {

        final AffineTransformMatrix3D aMat = a.euclideanTransform;
        final AffineTransformMatrix3D bMat = b.euclideanTransform;

        return new Transform2S(aMat.multiply(bMat));
    }
}
