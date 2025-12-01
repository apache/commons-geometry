/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.euclidean.threed.rotation;

/** Enum defining the possible reference frames for locating axis
 * positions during a rotation sequence.
 */
public enum AxisReferenceFrame {

    /** Defines a relative reference frame for a rotation sequence. Sequences
     * with this type of reference frame are called <em>intrinsic rotations</em>.
     *
     * <p>
     * When using a relative reference frame, each successive axis
     * is located relative to the "thing" being rotated and not to some
     * external frame of reference. For example, say that a rotation sequence
     * is defined around the {@code x}, {@code y}, and {@code z} axes in
     * that order. The first rotation will occur around the standard {@code x}
     * axis. The second rotation, however, will occur around the {@code y}
     * axis after it has been rotated by the first rotation; we can call this
     * new axis {@code y'}. Similarly, the third rotation will occur around
     * {@code z''}, which may or may not match the original {@code z} axis.
     * A good real-world example of this type of situation is an airplane,
     * where a pilot makes a sequence of rotations in order, with each rotation
     * using the airplane's own up/down, left/right, back/forward directions
     * as the frame of reference.
     * </p>
     */
    RELATIVE,

    /** Defines an absolute reference frame for a rotation sequence. Sequences
     * with this type of reference frame are called <em>extrinsic rotations</em>.
     *
     * <p>
     * In contrast with the relative reference frame, the absolute reference frame
     * remains fixed throughout a rotation sequence, with each rotation axis not
     * affected by the rotations.
     * </p>
     */
    ABSOLUTE
}
