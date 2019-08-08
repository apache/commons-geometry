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

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Interface representing a generic rotation in 3-dimensional Euclidean
 * space.
 */
public interface Rotation3D extends Transform<Vector3D> {

    /** Apply this rotation to the given argument. Since rotations do
     * not affect vector magnitudes, this method can be applied to
     * both points and vectors.
     * @param vec the point or vector to rotate
     * @return a new instance representing the rotated point or vector
     */
    @Override
    Vector3D apply(Vector3D vec);

    /** Get the axis of rotation as a normalized {@link Vector3D}.
     *
     * <p>All 3-dimensional rotations and sequences of rotations can be reduced
     * to a single rotation around one axis. This method returns that axis.
     *
     * @return the axis of rotation
     * @see #getAngle()
     */
    Vector3D getAxis();

    /** Get the angle of rotation in radians.
     *
     * <p>All 3-dimensional rotations and sequences of rotations can be reduced
     * to a single rotation around one axis. This method returns the angle of
     * rotation around that axis.
     *
     * @return angle of rotation in radians.
     * @see #getAxis()
     */
    double getAngle();

    /** Get the inverse rotation.
     * @return the inverse rotation.
     */
    Rotation3D inverse();
}
