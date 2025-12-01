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

/** Defines different types of rotation axis sequences.
 */
public enum AxisSequenceType {

    /** Represents Euler angles, which consist of axis sequences
     * in the pattern <em>ABA</em>. For example, the sequences {@code ZXZ}, {@code XYX}, etc.
     * fit this definition. Other types of sequences that do not match this
     * pattern are often called "Euler angles" in common usage. However, this enum
     * value is intended to represent only those sequences that match exactly, ie "proper" Euler angles.
     * @see <a href="https://en.wikipedia.org/wiki/Euler_angles#Proper_Euler_angles">Proper Euler angles</a>
     */
    EULER,

    /** Represents Tait-Bryan angles, which consist of axis sequences
     * in the pattern <em>ABC</em>. For example, the sequences {@code XYZ}, {@code ZXY},
     * etc. fit this definition. Tait-Bryan angles are also called Cardan angles.
     * @see <a href="https://en.wikipedia.org/wiki/Euler_angles#Tait%E2%80%93Bryan_angles">Tait-Bryan angles</a>
     */
    TAIT_BRYAN
}
