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
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.numbers.core.Precision;

/** <p>
 * Class representing a sequence of axis-angle rotations. These types of
 * rotations are commonly called <em>Euler angles</em>, <em>Tait-Bryan angles</em>,
 * or <em>Cardan angles</em> depending on the properties of the rotation sequence and
 * the particular use case. A sequence of three rotations around at least two different
 * axes is sufficient to represent any rotation or orientation in 3 dimensional space.
 * However, in order to unambiguously represent the rotation, the following information
 * must be provided along with the rotation angles:
 * <ul>
 *      <li><strong>Axis sequence</strong> - The axes that the rotation angles are associated with and
 *      in what order they occur.
 *      </li>
 *      <li><strong>Reference frame</strong> - The reference frame used to define the position of the rotation
 *      axes. This can either be <em>relative (intrinsic)</em> or <em>absolute (extrinsic)</em>. A relative
 *      reference frame defines the rotation axes from the point of view of the "thing" being rotated.
 *      Thus, each rotation after the first occurs around an axis that very well may have been
 *      moved from its original position by a previous rotation. A good example of this is an
 *      airplane: the pilot steps through a sequence of rotations, each time moving the airplane
 *      around its own up/down, left/right, and front/back axes, regardless of how the airplane
 *      is oriented at the time. In contrast, an absolute reference frame is fixed and does not
 *      move with each rotation.
 *      </li>
 *      <li><strong>Rotation direction</strong> - This defines the rotation direction that angles are measured in.
 *      This library uses <em>right-handed rotations</em> exclusively. This means that the direction of rotation
 *      around an axis is the same as the curl of one's fingers when the right hand is placed on the axis
 *      with the thumb pointing in the axis direction.
 *      </li>
 * </ul>
 *
 * <p>
 * Computations involving multiple rotations are generally very complicated when using axis-angle sequences. Therefore, it is recommended
 * to only use this class to represent angles and orientations when needed in this form, and to use {@link QuaternionRotation}
 * for everything else. Quaternions are much easier to work with and avoid many of the problems of axis-angle sequence representations,
 * such as <a href="https://en.wikipedia.org/wiki/Gimbal_lock">gimbal lock</a>.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Euler_angles">Euler Angles</a>
 * @see QuaternionRotation
 */
public final class AxisAngleSequence implements Serializable {

    /** Serializable identifier*/
    private static final long serialVersionUID = 20181125L;

    /** Reference frame for defining axis positions. */
    private final AxisReferenceFrame referenceFrame;

    /** Axis sequence. */
    private final AxisSequence axisSequence;

    /** Angle around the first rotation axis, in radians. */
    private final double angle1;

    /** Angle around the second rotation axis, in radians. */
    private final double angle2;

    /** Angle around the third rotation axis, in radians. */
    private final double angle3;

    /** Construct an instance from its component parts.
     * @param referenceFrame the axis reference frame
     * @param axisSequence the axis rotation sequence
     * @param angle1 angle around the first axis in radians
     * @param angle2 angle around the second axis in radians
     * @param angle3 angle around the third axis in radians
     */
    public AxisAngleSequence(final AxisReferenceFrame referenceFrame, final AxisSequence axisSequence, final double angle1,
            final double angle2, final double angle3) {
        this.referenceFrame = referenceFrame;
        this.axisSequence = axisSequence;

        this.angle1 = angle1;
        this.angle2 = angle2;
        this.angle3 = angle3;
    }

    /** Get the axis reference frame. This defines the position of the rotation axes.
     * @return the axis reference frame
     */
    public AxisReferenceFrame getReferenceFrame() {
        return referenceFrame;
    }

    /** Get the rotation axis sequence.
     * @return the rotation axis sequence
     */
    public AxisSequence getAxisSequence() {
        return axisSequence;
    }

    /** Get the angle of rotation around the first axis, in radians.
     * @return angle of rotation around the first axis, in radians
     */
    public double getAngle1() {
        return angle1;
    }

    /** Get the angle of rotation around the second axis, in radians.
     * @return angle of rotation around the second axis, in radians
     */
    public double getAngle2() {
        return angle2;
    }

    /** Get the angle of rotation around the thrid axis, in radians.
     * @return angle of rotation around the thrid axis, in radians
     */
    public double getAngle3() {
        return angle3;
    }

    /** Get the rotation angles as a 3-element array.
     * @return an array containing the 3 rotation angles
     */
    public double[] getAngles() {
        return new double[] { angle1, angle2, angle3 };
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return 107 * (199 * Objects.hash(referenceFrame, axisSequence)) +
                (7 * Double.hashCode(angle1)) +
                (11 * Double.hashCode(angle2)) +
                (19 * Double.hashCode(angle3));
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AxisAngleSequence)) {
            return false;
        }

        final AxisAngleSequence other = (AxisAngleSequence) obj;

        return this.referenceFrame == other.referenceFrame &&
                this.axisSequence == other.axisSequence &&
                Precision.equals(this.angle1, other.angle1) &&
                Precision.equals(this.angle2, other.angle2) &&
                Precision.equals(this.angle3, other.angle3);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[referenceFrame=")
            .append(referenceFrame)
            .append(", axisSequence=")
            .append(axisSequence)
            .append(", angles=")
            .append(Arrays.toString(getAngles()))
            .append(']');

        return sb.toString();
    }

    /** Create a new instance with a reference frame of {@link AxisReferenceFrame#RELATIVE}.
     * @param axisSequence the axis rotation sequence
     * @param angle1 angle around the first axis in radians
     * @param angle2 angle around the second axis in radians
     * @param angle3 angle around the third axis in radians
     * @return a new instance with a relative reference frame
     */
    public static AxisAngleSequence createRelative(final AxisSequence axisSequence, final double angle1,
            final double angle2, final double angle3) {
        return new AxisAngleSequence(AxisReferenceFrame.RELATIVE, axisSequence, angle1, angle2, angle3);
    }

    /** Create a new instance with a reference frame of {@link AxisReferenceFrame#ABSOLUTE}.
     * @param axisSequence the axis rotation sequence
     * @param angle1 angle around the first axis in radians
     * @param angle2 angle around the second axis in radians
     * @param angle3 angle around the third axis in radians
     * @return a new instance with an absolute reference frame
     */
    public static AxisAngleSequence createAbsolute(final AxisSequence axisSequence, final double angle1,
            final double angle2, final double angle3) {
        return new AxisAngleSequence(AxisReferenceFrame.ABSOLUTE, axisSequence, angle1, angle2, angle3);
    }
}
