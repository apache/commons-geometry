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

import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Enum containing rotation axis sequences for use in defining 3 dimensional rotations.
 */
public enum AxisSequence {

    /** Set of Tait-Bryan angles around the <strong>X</strong>, <strong>Y</strong>, and
     * <strong>Z</strong> axes in that order.
     */
    XYZ(AxisSequenceType.TAIT_BRYAN, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z),

    /** Set of Tait-Bryan angles around the <strong>X</strong>, <strong>Z</strong>, and
     * <strong>Y</strong> axes in that order.
     */
    XZY(AxisSequenceType.TAIT_BRYAN, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_Y),

    /** Set of Tait-Bryan angles around the <strong>Y</strong>, <strong>X</strong>, and
     * <strong>Z</strong> axes in that order.
     */
    YXZ(AxisSequenceType.TAIT_BRYAN, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z),

    /** Set of Tait-Bryan angles around the <strong>Y</strong>, <strong>Z</strong>, and
     * <strong>X</strong> axes in that order.
     */
    YZX(AxisSequenceType.TAIT_BRYAN, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X),

    /** Set of Cardan angles.
     * this ordered set of rotations is around Z, then around X, then
     * around Y
     */
    ZXY(AxisSequenceType.TAIT_BRYAN, Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y),

    /** Set of Tait-Bryan angles around the <strong>Z</strong>, <strong>Y</strong>, and
     * <strong>X</strong> axes in that order.
     */
    ZYX(AxisSequenceType.TAIT_BRYAN, Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X),

    /** Set of Euler angles around the <strong>X</strong>, <strong>Y</strong>, and
     * <strong>X</strong> axes in that order.
     */
    XYX(AxisSequenceType.EULER, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X),

    /** Set of Euler angles around the <strong>X</strong>, <strong>Z</strong>, and
     * <strong>X</strong> axes in that order.
     */
    XZX(AxisSequenceType.EULER, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X),

    /** Set of Euler angles around the <strong>Y</strong>, <strong>X</strong>, and
     * <strong>Y</strong> axes in that order.
     */
    YXY(AxisSequenceType.EULER, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y),

    /** Set of Euler angles around the <strong>Y</strong>, <strong>Z</strong>, and
     * <strong>Y</strong> axes in that order.
     */
    YZY(AxisSequenceType.EULER, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_Y),

    /** Set of Euler angles around the <strong>Z</strong>, <strong>X</strong>, and
     * <strong>Z</strong> axes in that order.
     */
    ZXZ(AxisSequenceType.EULER, Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z),

    /** Set of Euler angles around the <strong>Z</strong>, <strong>Y</strong>, and
     * <strong>Z</strong> axes in that order.
     */
    ZYZ(AxisSequenceType.EULER, Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z);

    /** The type of axis sequence. */
    private final AxisSequenceType type;

    /** Axis of the first rotation. */
    private final Vector3D axis1;

    /** Axis of the second rotation. */
    private final Vector3D axis2;

    /** Axis of the third rotation. */
    private final Vector3D axis3;

    /** Simple constructor.
     * @param type the axis sequence type
     * @param axis1 first rotation axis
     * @param axis2 second rotation axis
     * @param axis3 third rotation axis
     */
    AxisSequence(final AxisSequenceType type, final Vector3D axis1, final Vector3D axis2, final Vector3D axis3) {
        this.type = type;

        this.axis1 = axis1;
        this.axis2 = axis2;
        this.axis3 = axis3;
    }

    /** Get the axis sequence type.
     * @return the axis sequence type
     */
    public AxisSequenceType getType() {
        return type;
    }

    /** Get the first rotation axis.
     * @return the first rotation axis
     */
    public Vector3D getAxis1() {
        return axis1;
    }

    /** Get the second rotation axis.
     * @return the second rotation axis
     */
    public Vector3D getAxis2() {
        return axis2;
    }

    /** Get the third rotation axis.
     * @return the third rotation axis
     */
    public Vector3D getAxis3() {
        return axis3;
    }

    /** Get an array containing the 3 rotation axes in order.
     * @return a 3-element array containing the rotation axes in order
     */
    public Vector3D[] toArray() {
        return new Vector3D[]{axis1, axis2, axis3};
    }
}
