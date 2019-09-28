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
package org.apache.commons.geometry.euclidean.threed.rotation;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.exception.IllegalNormException;
import org.apache.commons.geometry.core.internal.GeometryInternalError;
import org.apache.commons.geometry.euclidean.internal.Vectors;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.arrays.LinearCombination;
import org.apache.commons.numbers.quaternion.Quaternion;
import org.apache.commons.numbers.quaternion.Slerp;

/**
 * Class using a unit-length quaternion to represent
 * <a href="https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation">rotations</a>
 * in 3-dimensional Euclidean space.
 * The underlying quaternion is in <em>positive polar form</em>: It is normalized and has a
 * non-negative scalar component ({@code w}).
 *
 * @see Quaternion
 */
public final class QuaternionRotation implements Rotation3D, Serializable {

    /** Serializable version identifier */
    private static final long serialVersionUID = 20181018L;

    /** Threshold value for the dot product of antiparallel vectors. If the dot product of two vectors is
     * less than this value, (adjusted for the lengths of the vectors), then the vectors are considered to be
     * antiparallel (ie, negations of each other).
     */
    private static final double ANTIPARALLEL_DOT_THRESHOLD = 2.0e-15 - 1.0;

    /** Threshold value used to identify singularities when converting from quaternions to
     * axis angle sequences.
     */
    private static final double AXIS_ANGLE_SINGULARITY_THRESHOLD = 0.9999999999;

    /** Instance used to represent the identity rotation, ie a rotation with
     * an angle of zero.
     */
    private static final QuaternionRotation IDENTITY_INSTANCE = of(Quaternion.ONE);

    /** Unit-length quaternion instance in positive polar form. */
    private final Quaternion quat;

    /** Simple constructor. The given quaternion is converted to positive polar form.
     * @param quat quaternion instance
     * @throws IllegalStateException if the the norm of the given components is zero,
     *                              NaN, or infinite
     */
    private QuaternionRotation(final Quaternion quat) {
        this.quat = quat.positivePolarForm();
    }

    /** Get the underlying quaternion instance.
     * @return the quaternion instance
     */
    public Quaternion getQuaternion() {
        return quat;
    }

    /**
     * Get the axis of rotation as a normalized {@link Vector3D}. The rotation axis
     * is not well defined when the rotation is the identity rotation, ie it has a
     * rotation angle of zero. In this case, the vector representing the positive
     * x-axis is returned.
     *
     * @return the axis of rotation
     */
    @Override
    public Vector3D getAxis() {
        // the most straightforward way to check if we have a normalizable
        // vector is to just try to normalize it and see if we fail
        try {
            return Vector3D.Unit.from(quat.getX(), quat.getY(), quat.getZ());
        }
        catch (IllegalNormException exc) {
            return Vector3D.Unit.PLUS_X;
        }
    }

    /**
     * Get the angle of rotation in radians. The returned value is in the range 0
     * through {@code pi}.
     *
     * @return The rotation angle in the range {@code [0, pi]}.
     */
    @Override
    public double getAngle() {
        return 2 * Math.acos(quat.getW());
    }

    /**
     * Get the inverse of this rotation. The returned rotation has the same
     * rotation angle but the opposite rotation axis. If {@code r.apply(u)}
     * is equal to {@code v}, then {@code r.negate().apply(v)} is equal
     * to {@code u}.
     *
     * @return the negation (inverse) of the rotation
     */
    @Override
    public QuaternionRotation inverse() {
        return new QuaternionRotation(quat.conjugate());
    }

    /**
     * Apply this rotation to the given vector.
     *
     * @param v vector to rotate
     * @return the rotated vector
     */
    @Override
    public Vector3D apply(final Vector3D v) {
        final double qw = quat.getW();
        final double qx = quat.getX();
        final double qy = quat.getY();
        final double qz = quat.getZ();

        final double x = v.getX();
        final double y = v.getY();
        final double z = v.getZ();

        // calculate the Hamilton product of the quaternion and vector
        final double iw = -(qx * x) - (qy * y) - (qz * z);
        final double ix = (qw * x) + (qy * z) - (qz * y);
        final double iy = (qw * y) + (qz * x) - (qx * z);
        final double iz = (qw * z) + (qx * y) - (qy * x);

        // calculate the Hamilton product of the intermediate vector and
        // the inverse quaternion

        return Vector3D.of(
                    (iw * -qx) + (ix * qw) + (iy * -qz) - (iz * -qy),
                    (iw * -qy) - (ix * -qz) + (iy * qw) + (iz * -qx),
                    (iw * -qz) + (ix * -qy) - (iy * -qx) + (iz * qw)
                );
    }

    /** {@inheritDoc}
     *
     * <p>This method simply calls {@code apply(vec)} since rotations treat
     * points and vectors similarly.</p>
     */
    @Override
    public Vector3D applyVector(final Vector3D vec) {
        return apply(vec);
    }

    /** {@inheritDoc}
     *
     * <p>This method simply returns true since rotations always preserve the orientation
     * of the space.</p>
     */
    @Override
    public boolean preservesOrientation() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public AffineTransformMatrix3D toMatrix() {

        final double qw = quat.getW();
        final double qx = quat.getX();
        final double qy = quat.getY();
        final double qz = quat.getZ();

        // pre-calculate products that we'll need
        final double xx = qx * qx;
        final double xy = qx * qy;
        final double xz = qx * qz;
        final double xw = qx * qw;

        final double yy = qy * qy;
        final double yz = qy * qz;
        final double yw = qy * qw;

        final double zz = qz * qz;
        final double zw = qz * qw;

        final double m00 = 1.0 - (2.0 * (yy + zz));
        final double m01 = 2.0 * (xy - zw);
        final double m02 = 2.0 * (xz + yw);
        final double m03 = 0.0;

        final double m10 = 2.0 * (xy + zw);
        final double m11 = 1.0 - (2.0 * (xx + zz));
        final double m12 = 2.0 * (yz - xw);
        final double m13 = 0.0;

        final double m20 = 2.0 * (xz - yw);
        final double m21 = 2.0 * (yz + xw);
        final double m22 = 1.0 - (2.0 * (xx + yy));
        final double m23 = 0.0;

        return AffineTransformMatrix3D.of(
                    m00, m01, m02, m03,
                    m10, m11, m12, m13,
                    m20, m21, m22, m23
                );
    }

    /**
     * Multiply this instance by the given argument, returning the result as
     * a new instance. This is equivalent to the expression {@code t * q} where
     * {@code q} is the argument and {@code t} is this instance.
     *
     * <p>
     * Multiplication of quaternions behaves similarly to transformation
     * matrices in regard to the order that operations are performed.
     * For example, if <code>q<sub>1</sub></code> and <code>q<sub>2</sub></code> are unit
     * quaternions, then the quaternion <code>q<sub>r</sub> = q<sub>1</sub>*q<sub>2</sub></code>
     * will give the effect of applying the rotation in <code>q<sub>2</sub></code> followed
     * by the rotation in <code>q<sub>1</sub></code>. In other words, the rightmost element
     * in the multiplication is applied first.
     * </p>
     *
     * @param q quaternion to multiply with the current instance
     * @return the result of multiplying this quaternion by the argument
     */
    public QuaternionRotation multiply(final QuaternionRotation q) {
        final Quaternion product = quat.multiply(q.quat);
        return new QuaternionRotation(product);
    }

    /** Multiply the argument by this instance, returning the result as
     * a new instance. This is equivalent to the expression {@code q * t} where
     * {@code q} is the argument and {@code t} is this instance.
     *
     * <p>
     * Multiplication of quaternions behaves similarly to transformation
     * matrices in regard to the order that operations are performed.
     * For example, if <code>q<sub>1</sub></code> and <code>q<sub>2</sub></code> are unit
     * quaternions, then the quaternion <code>q<sub>r</sub> = q<sub>1</sub>*q<sub>2</sub></code>
     * will give the effect of applying the rotation in <code>q<sub>2</sub></code> followed
     * by the rotation in <code>q<sub>1</sub></code>. In other words, the rightmost element
     * in the multiplication is applied first.
     * </p>
     *
     * @param q quaternion to multiply by the current instance
     * @return the result of multiplying the argument by the current instance
     */
    public QuaternionRotation premultiply(final QuaternionRotation q) {
        return q.multiply(this);
    }

    /**
     * Creates a {@link Slerp} transform.
     *
     * @param end End rotation of the interpolation.
     * @return the transform.
     */
    public Slerp slerp(QuaternionRotation end) {
        return new Slerp(quat, end.quat);
    }

    /** Get a sequence of axis-angle rotations that produce an overall rotation equivalent to this instance.
     *
     * <p>
     * In most cases, the returned rotation sequence will be unique. However, at points of singularity
     * (second angle equal to {@code 0} or {@code -pi} for Euler angles and {@code +pi/2} or {@code -pi/2}
     * for Tait-Bryan angles), there are an infinite number of possible sequences that produce the same result.
     * In these cases, the result is returned that leaves the last rotation equal to 0 (in the case of a relative
     * reference frame) or the first rotation equal to 0 (in the case of an absolute reference frame).
     * </p>
     *
     * @param frame the reference frame used to interpret the positions of the rotation axes
     * @param axes the sequence of rotation axes
     * @return a sequence of axis-angle rotations equivalent to this rotation
     */
    public AxisAngleSequence toAxisAngleSequence(final AxisReferenceFrame frame, final AxisSequence axes) {
        if (frame == null) {
            throw new IllegalArgumentException("Axis reference frame cannot be null");
        }
        if (axes == null) {
            throw new IllegalArgumentException("Axis sequence cannot be null");
        }

        double[] angles = getAngles(frame, axes);

        return new AxisAngleSequence(frame, axes, angles[0], angles[1], angles[2]);
    }

    /** Get a sequence of axis-angle rotations that produce an overall rotation equivalent to this instance.
     * Each rotation axis is interpreted relative to the rotated coordinate frame (ie, intrinsic rotation).
     * @param axes the sequence of rotation axes
     * @return a sequence of relative axis-angle rotations equivalent to this rotation
     * @see #toAxisAngleSequence(AxisReferenceFrame, AxisSequence)
     */
    public AxisAngleSequence toRelativeAxisAngleSequence(final AxisSequence axes) {
        return toAxisAngleSequence(AxisReferenceFrame.RELATIVE, axes);
    }

    /** Get a sequence of axis-angle rotations that produce an overall rotation equivalent to this instance.
     * Each rotation axis is interpreted as part of an absolute, unmoving coordinate frame (ie, extrinsic rotation).
     * @param axes the sequence of rotation axes
     * @return a sequence of absolute axis-angle rotations equivalent to this rotation
     * @see #toAxisAngleSequence(AxisReferenceFrame, AxisSequence)
     */
    public AxisAngleSequence toAbsoluteAxisAngleSequence(final AxisSequence axes) {
        return toAxisAngleSequence(AxisReferenceFrame.ABSOLUTE, axes);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return quat.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QuaternionRotation)) {
            return false;
        }

        QuaternionRotation other = (QuaternionRotation) obj;
        return Objects.equals(this.quat, other.quat);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return quat.toString();
    }

    /** Get a sequence of angles around the given axes that produce a rotation equivalent
     * to this instance.
     * @param frame the reference frame used to define the positions of the axes
     * @param axes the axis sequence
     * @return a sequence of angles around the given axes that produce a rotation equivalent
     *      to this instance
     */
    private double[] getAngles(final AxisReferenceFrame frame, final AxisSequence axes) {

        AxisSequenceType sequenceType = axes.getType();

        final Vector3D axis1 = axes.getAxis1();
        final Vector3D axis2 = axes.getAxis2();
        final Vector3D axis3 = axes.getAxis3();

        if (frame == AxisReferenceFrame.RELATIVE) {
            if (sequenceType == AxisSequenceType.TAIT_BRYAN) {
                return getRelativeTaitBryanAngles(axis1, axis2, axis3);
            }
            else if (sequenceType == AxisSequenceType.EULER) {
                return getRelativeEulerAngles(axis1, axis2, axis3);
            }
        }
        else if (frame == AxisReferenceFrame.ABSOLUTE) {
            if (sequenceType == AxisSequenceType.TAIT_BRYAN) {
                return getAbsoluteTaitBryanAngles(axis1, axis2, axis3);
            }
            else if (sequenceType == AxisSequenceType.EULER) {
                return getAbsoluteEulerAngles(axis1, axis2, axis3);
            }
        }

        // all possibilities should have been covered above
        throw new GeometryInternalError();
    }

    /** Get a sequence of angles around the given Tait-Bryan axes that produce a rotation equivalent
     * to this instance. The axes are interpreted as being relative to the rotated coordinate frame.
     * @param axis1 first Tait-Bryan axis
     * @param axis2 second Tait-Bryan axis
     * @param axis3 third Tait-Bryan axis
     * @return a sequence of rotation angles around the relative input axes that produce a rotation equivalent
     *      to this instance
     */
    private double[] getRelativeTaitBryanAngles(final Vector3D axis1, final Vector3D axis2, final Vector3D axis3) {

        // We can use geometry to get the first and second angles pretty easily here by analyzing the positions
        // of the transformed rotation axes. The third angle is trickier but we can get it by treating it as
        // if it were the first rotation in the inverse (which it would be).

        final Vector3D vec3 = apply(axis3);
        final Vector3D invVec1 = inverse().apply(axis1);

        final double angle2Sin = vec3.dot(axis2.cross(axis3));

        if (angle2Sin < -AXIS_ANGLE_SINGULARITY_THRESHOLD ||
                angle2Sin > AXIS_ANGLE_SINGULARITY_THRESHOLD) {

            final Vector3D vec2 = apply(axis2);

            final double angle1TanY = vec2.dot(axis1.cross(axis2));
            final double angle1TanX = vec2.dot(axis2);

            final double angle2 = angle2Sin > AXIS_ANGLE_SINGULARITY_THRESHOLD ? Geometry.HALF_PI : Geometry.MINUS_HALF_PI;

            return new double[] {
                Math.atan2(angle1TanY, angle1TanX),
                angle2,
                Geometry.ZERO_PI
            };
        }

        final Vector3D  crossAxis13 = axis1.cross(axis3);

        final double angle1TanY = vec3.dot(crossAxis13);
        final double angle1TanX = vec3.dot(axis3);

        final double angle3TanY = invVec1.dot(crossAxis13);
        final double angle3TanX = invVec1.dot(axis1);

        return new double[] {
            Math.atan2(angle1TanY, angle1TanX),
            Math.asin(angle2Sin),
            Math.atan2(angle3TanY, angle3TanX)
        };
    }

    /** Get a sequence of angles around the given Tait-Bryan axes that produce a rotation equivalent
     * to this instance. The axes are interpreted as being part of an absolute (unmoving) coordinate frame.
     * @param axis1 first Tait-Bryan axis
     * @param axis2 second Tait-Bryan axis
     * @param axis3 third Tait-Bryan axis
     * @return a sequence of rotation angles around the absolute input axes that produce a rotation equivalent
     *      to this instance
     */
    private double[] getAbsoluteTaitBryanAngles(final Vector3D axis1, final Vector3D axis2, final Vector3D axis3) {
        // A relative axis-angle rotation sequence is equivalent to an absolute one with the rotation
        // sequence reversed, meaning we can reuse our relative logic here.
        return reverseArray(getRelativeTaitBryanAngles(axis3, axis2, axis1));
    }

    /** Get a sequence of angles around the given Euler axes that produce a rotation equivalent
     * to this instance. The axes are interpreted as being relative to the rotated coordinate frame.
     * @param axis1 first Euler axis
     * @param axis2 second Euler axis
     * @param axis3 third Euler axis
     * @return a sequence of rotation angles around the relative input axes that produce a rotation equivalent
     *      to this instance
     */
    private double[] getRelativeEulerAngles(final Vector3D axis1, final Vector3D axis2, final Vector3D axis3) {

        // Use the same overall approach as with the Tait-Bryan angles: get the first two angles by looking
        // at the transformed rotation axes and the third by using the inverse.

        final Vector3D crossAxis = axis1.cross(axis2);

        final Vector3D vec1 = apply(axis1);
        final Vector3D invVec1 = inverse().apply(axis1);

        final double angle2Cos = vec1.dot(axis1);

        if (angle2Cos < -AXIS_ANGLE_SINGULARITY_THRESHOLD ||
                angle2Cos > AXIS_ANGLE_SINGULARITY_THRESHOLD) {

            final Vector3D vec2 = apply(axis2);

            final double angle1TanY = vec2.dot(crossAxis);
            final double angle1TanX = vec2.dot(axis2);

            final double angle2 = angle2Cos > AXIS_ANGLE_SINGULARITY_THRESHOLD ? Geometry.ZERO_PI : Geometry.PI;

            return new double[] {
                Math.atan2(angle1TanY, angle1TanX),
                angle2,
                Geometry.ZERO_PI
            };
        }

        final double angle1TanY = vec1.dot(axis2);
        final double angle1TanX = -vec1.dot(crossAxis);

        final double angle3TanY = invVec1.dot(axis2);
        final double angle3TanX = invVec1.dot(crossAxis);

        return new double[] {
            Math.atan2(angle1TanY, angle1TanX),
            Math.acos(angle2Cos),
            Math.atan2(angle3TanY, angle3TanX)
        };
    }

    /** Get a sequence of angles around the given Euler axes that produce a rotation equivalent
     * to this instance. The axes are interpreted as being part of an absolute (unmoving) coordinate frame.
     * @param axis1 first Euler axis
     * @param axis2 second Euler axis
     * @param axis3 third Euler axis
     * @return a sequence of rotation angles around the absolute input axes that produce a rotation equivalent
     *      to this instance
     */
    private double[] getAbsoluteEulerAngles(final Vector3D axis1, final Vector3D axis2, final Vector3D axis3) {
        // A relative axis-angle rotation sequence is equivalent to an absolute one with the rotation
        // sequence reversed, meaning we can reuse our relative logic here.
        return reverseArray(getRelativeEulerAngles(axis3, axis2, axis1));
    }

    /** Create a new instance from the given quaternion. The quaternion is normalized and
     * converted to positive polar form (ie, with w &gt;= 0).
     *
     * @param quat the quaternion to use for the rotation
     * @return a new instance built from the given quaternion.
     * @throws IllegalStateException if the the norm of the given components is zero,
     *                              NaN, or infinite
     * @see Quaternion#normalize()
     * @see Quaternion#positivePolarForm()
     */
    public static QuaternionRotation of(Quaternion quat) {
        return new QuaternionRotation(quat);
    }

    /**
     * Create a new instance from the given quaternion values. The inputs are
     * normalized and converted to positive polar form (ie, with w &gt;= 0).
     *
     * @param w quaternion scalar component
     * @param x first quaternion vectorial component
     * @param y second quaternion vectorial component
     * @param z third quaternion vectorial component
     * @return a new instance containing the normalized quaterion components
     * @throws IllegalStateException if the the norm of the given components is zero,
     *                              NaN, or infinite
     * @see Quaternion#normalize()
     * @see Quaternion#positivePolarForm()
     */
    public static QuaternionRotation of(final double w,
                                        final double x,
                                        final double y,
                                        final double z) {
        return of(Quaternion.of(w, x, y, z));
    }

    /** Return an instance representing a rotation of zero.
     * @return instance representing a rotation of zero.
     */
    public static QuaternionRotation identity() {
        return IDENTITY_INSTANCE;
    }

    /** Create a new instance representing a rotation of {@code angle} radians around
     * {@code axis}.
     *
     * <p>
     * Rotation direction follows the right-hand rule, meaning that if one
     * places their right hand such that the thumb points in the direction of the vector,
     * the curl of the fingers indicates the direction of rotation.
     * </p>
     *
     * <p>
     * Note that the returned quaternion will represent the defined rotation but the values
     * returned by {@link #getAxis()} and {@link #getAngle()} may not match the ones given here.
     * This is because the axis and angle are normalized such that the axis has unit length,
     * and the angle lies in the range {@code [0, pi]}. Depending on the inputs, the axis may
     * need to be inverted in order for the angle to lie in this range.
     * </p>
     *
     * @param axis the axis of rotation
     * @param angle angle of rotation in radians
     * @return a new instance representing the defined rotation
     *
     * @throws IllegalNormException if the given axis cannot be normalized
     * @throws IllegalArgumentException if the angle is NaN or infinite
     */
    public static QuaternionRotation fromAxisAngle(final Vector3D axis, final double angle) {
        // reference formula:
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/angleToQuaternion/index.htm
        final Vector3D normAxis = axis.normalize();

        if (!Double.isFinite(angle)) {
            throw new IllegalArgumentException("Invalid angle: " + angle);
        }

        final double halfAngle = 0.5 * angle;
        final double sinHalfAngle = Math.sin(halfAngle);

        final double w = Math.cos(halfAngle);
        final double x = sinHalfAngle * normAxis.getX();
        final double y = sinHalfAngle * normAxis.getY();
        final double z = sinHalfAngle * normAxis.getZ();

        return of(w, x, y, z);
    }

    /** Return an instance that rotates the first vector to the second.
     *
     * <p>Except for a possible scale factor, if the returned instance is
     * applied to vector {@code u}, it will produce the vector {@code v}. There are an
     * infinite number of such rotations; this method chooses the one with the smallest
     * associated angle, meaning the one whose axis is orthogonal to the {@code (u, v)}
     * plane. If {@code u} and {@code v} are collinear, an arbitrary rotation axis is
     * chosen.</p>
     *
     * @param u origin vector
     * @param v target vector
     * @return a new instance that rotates {@code u} to point in the direction of {@code v}
     * @throws IllegalNormException if either vector has a norm of zero, NaN, or infinity
     */
    public static QuaternionRotation createVectorRotation(final Vector3D u, final Vector3D v) {

        double normProduct  = Vectors.checkedNorm(u) * Vectors.checkedNorm(v);
        double dot = u.dot(v);

        if (dot < ANTIPARALLEL_DOT_THRESHOLD * normProduct) {
            // Special case where u1 = -u2:
            // create a pi angle rotation around
            // an arbitrary unit vector orthogonal to u1
            final Vector3D axis = u.orthogonal();

            return of(0,
                      axis.getX(),
                      axis.getY(),
                      axis.getZ());
        }

        // General case:
        // (u1, u2) defines a plane so rotate around the normal of the plane

        // w must equal cos(theta/2); we can calculate this directly using values
        // we already have with the identity cos(theta/2) = sqrt((1 + cos(theta)) / 2)
        // and the fact that dot = norm(u1) * norm(u2) * cos(theta).
        final double w = Math.sqrt(0.5 * (1.0 + (dot / normProduct)));

        // The cross product u1 x u2 must be normalized and then multiplied by
        // sin(theta/2) in order to set the vectorial part of the quaternion. To
        // accomplish this, we'll use the following:
        //
        // 1) norm(a x b) = norm(a) * norm(b) * sin(theta)
        // 2) sin(theta/2) = sqrt((1 - cos(theta)) / 2)
        //
        // Our full, combined normalization and sine half angle term factor then becomes:
        //
        // sqrt((1 - cos(theta)) / 2) / (norm(u1) * norm(u2) * sin(theta))
        //
        // This can be simplified to the expression below.
        final double vectorialScaleFactor = 1.0 / (2.0 * w * normProduct);
        final Vector3D axis = u.cross(v);

        return of(w,
                  vectorialScaleFactor * axis.getX(),
                  vectorialScaleFactor * axis.getY(),
                  vectorialScaleFactor * axis.getZ());
    }

    /** Return an instance that rotates the basis defined by the first two vectors into the basis
     * defined by the second two.
     *
     * <p>
     * The given basis vectors do not have to be directly orthogonal. A right-handed orthonormal
     * basis is created from each pair by normalizing the first vector, making the second vector
     * orthogonal to the first, and then taking the cross product. A rotation is then calculated
     * that rotates the first to the second.
     * </p>
     *
     * @param u1 first vector of the source basis
     * @param u2 second vector of the source basis
     * @param v1 first vector of the target basis
     * @param v2 second vector of the target basis
     * @return an instance that rotates the source basis to the target basis
     * @throws IllegalNormException if any of the input vectors cannot be normalized
     *      or the vectors defining either basis are colinear
     */
    public static QuaternionRotation createBasisRotation(final Vector3D u1, final Vector3D u2,
            final Vector3D v1, final Vector3D v2) {

        // calculate orthonormalized bases
        final Vector3D a = u1.normalize();
        final Vector3D b = a.orthogonal(u2);
        final Vector3D c = a.cross(b);

        final Vector3D d = v1.normalize();
        final Vector3D e = d.orthogonal(v2);
        final Vector3D f = d.cross(e);

        // create an orthogonal rotation matrix representing the change of basis; this matrix will
        // be the multiplication of the matrix composed of the column vectors d, e, f and the
        // inverse of the matrix composed of the column vectors a, b, c (which is simply the transpose since
        // it's orthogonal).
        final double m00 = LinearCombination.value(d.getX(), a.getX(), e.getX(), b.getX(), f.getX(), c.getX());
        final double m01 = LinearCombination.value(d.getX(), a.getY(), e.getX(), b.getY(), f.getX(), c.getY());
        final double m02 = LinearCombination.value(d.getX(), a.getZ(), e.getX(), b.getZ(), f.getX(), c.getZ());

        final double m10 = LinearCombination.value(d.getY(), a.getX(), e.getY(), b.getX(), f.getY(), c.getX());
        final double m11 = LinearCombination.value(d.getY(), a.getY(), e.getY(), b.getY(), f.getY(), c.getY());
        final double m12 = LinearCombination.value(d.getY(), a.getZ(), e.getY(), b.getZ(), f.getY(), c.getZ());

        final double m20 = LinearCombination.value(d.getZ(), a.getX(), e.getZ(), b.getX(), f.getZ(), c.getX());
        final double m21 = LinearCombination.value(d.getZ(), a.getY(), e.getZ(), b.getY(), f.getZ(), c.getY());
        final double m22 = LinearCombination.value(d.getZ(), a.getZ(), e.getZ(), b.getZ(), f.getZ(), c.getZ());


        return orthogonalRotationMatrixToQuaternion(
                    m00, m01, m02,
                    m10, m11, m12,
                    m20, m21, m22
                );
    }

    /** Create a new instance equivalent to the given sequence of axis-angle rotations.
     * @param sequence the axis-angle rotation sequence to convert to a quaternion rotation
     * @return instance representing a rotation equivalent to the given axis-angle sequence
     */
    public static QuaternionRotation fromAxisAngleSequence(final AxisAngleSequence sequence) {
        final AxisSequence axes = sequence.getAxisSequence();

        final QuaternionRotation q1 = fromAxisAngle(axes.getAxis1(), sequence.getAngle1());
        final QuaternionRotation q2 = fromAxisAngle(axes.getAxis2(), sequence.getAngle2());
        final QuaternionRotation q3 = fromAxisAngle(axes.getAxis3(), sequence.getAngle3());

        if (sequence.getReferenceFrame() == AxisReferenceFrame.ABSOLUTE) {
            return q3.multiply(q2).multiply(q1);
        }

        return q1.multiply(q2).multiply(q3);
    }

    /** Create an instance from an orthogonal rotation matrix.
     *
     * @param m00 matrix entry <code>m<sub>0,0</sub></code>
     * @param m01 matrix entry <code>m<sub>0,1</sub></code>
     * @param m02 matrix entry <code>m<sub>0,2</sub></code>
     * @param m10 matrix entry <code>m<sub>1,0</sub></code>
     * @param m11 matrix entry <code>m<sub>1,1</sub></code>
     * @param m12 matrix entry <code>m<sub>1,2</sub></code>
     * @param m20 matrix entry <code>m<sub>2,0</sub></code>
     * @param m21 matrix entry <code>m<sub>2,1</sub></code>
     * @param m22 matrix entry <code>m<sub>2,2</sub></code>
     * @return an instance representing the same 3D rotation as the given matrix
     */
    private static QuaternionRotation orthogonalRotationMatrixToQuaternion(
            final double m00, final double m01, final double m02,
            final double m10, final double m11, final double m12,
            final double m20, final double m21, final double m22) {

        // reference formula:
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/

        // The overall approach here is to take the equations for converting a quaternion to
        // a matrix along with the fact that 1 = x^2 + y^2 + z^2 + w^2 for a normalized quaternion
        // and solve for the various terms. This can theoretically be done using just the diagonal
        // terms from the matrix. However, there are a few issues with this:
        // 1) The term that we end up taking the square root of may be negative.
        // 2) It's ambiguous as to whether we should use a plus or minus for the value of the
        //    square root.
        // We'll address these concerns by only calculating a single term from one of the diagonal
        // elements and then calculate the rest from the non-diagonals, which do not involve
        // a square root. This solves the first issue since we can make sure to choose a diagonal
        // element that will not cause us to take a square root of a negative number. The second
        // issue is solved since only the relative signs between the quaternion terms are important
        // (q and -q represent the same 3D rotation). It therefore doesn't matter whether we choose
        // a plus or minus for our initial square root solution.

        final double trace = m00 + m11 + m22;

        double w;
        double x;
        double y;
        double z;

        if (trace > 0.0) {
            // let s = 4*w
            final double s = 2.0 * Math.sqrt(1.0 + trace);
            final double sinv = 1.0 / s;

            x = (m21 - m12) * sinv;
            y = (m02 - m20) * sinv;
            z = (m10 - m01) * sinv;
            w = 0.25 * s;
        }
        else if ((m00 > m11) && (m00 > m22)) {
            // let s = 4*x
            final double s = 2.0 * Math.sqrt(1.0 + m00 - m11 - m22);
            final double sinv = 1.0 / s;

            x = 0.25 * s;
            y = (m01 + m10) * sinv;
            z = (m02 + m20) * sinv;
            w = (m21 - m12) * sinv;
        }
        else if (m11 > m22) {
            // let s = 4*y
            final double s = 2.0 * Math.sqrt(1.0 + m11 - m00 - m22);
            final double sinv = 1.0 / s;

            x = (m01 + m10) * sinv;
            y = 0.25 * s;
            z = (m21 + m12) * sinv;
            w = (m02 - m20) * sinv;
        }
        else {
            // let s = 4*z
            final double s = 2.0 * Math.sqrt(1.0 + m22 - m00 - m11);
            final double sinv = 1.0 / s;

            x = (m02 + m20) * sinv;
            y = (m21 + m12) * sinv;
            z = 0.25 * s;
            w = (m10 - m01) * sinv;
        }

        return of(w, x, y, z);
    }

    /** Reverse the elements in {@code arr}. The array is returned.
     * @param arr the array to reverse
     * @return the input array with the elements reversed
     */
    private static double[] reverseArray(final double[] arr) {

        final int len = arr.length;
        double temp;

        int i;
        int j;

        for (i=0, j=len-1; i < len / 2; ++i, --j) {
            temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }

        return arr;
    }
}
