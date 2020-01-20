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
package org.apache.commons.geometry.core;

import java.util.function.UnaryOperator;

/** Interface representing geometric transforms in a space, i.e. mappings from points to points.
 * Implementations <em>must</em> fulfill a set of requirements, listed below, that preserve the
 * consistency of partitionings on the space. Transforms that do not meet these requirements, while
 * potentially valid mathematically, cannot be expected to produce correct results with algorithms
 * that use this interface.
 *
 * <ol>
 *      <li>Transforms must represent functions that are <em>one-to-one</em> and <em>onto</em> (i.e.
 *      <a href="https://en.wikipedia.org/wiki/Bijection">bijections</a>). This means that every point
 *      in the space must be mapped to exactly one other point in the space. This also implies that the
 *      function is invertible.</li>
 *      <li>Transforms must preserve <a href="https://en.wikipedia.org/wiki/Collinearity">collinearity</a>.
 *      This means that if a set of points lie on a common hyperplane before the transform, then they must
 *      also lie on a common hyperplane after the transform. For example, if the Euclidean 2D points {@code a},
 *      {@code b}, and {@code c} lie on line {@code L}, then the transformed points {@code a'}, {@code b'}, and
 *      {@code c'} must lie on line {@code L'}, where {@code L'} is the transformed form of the line.</li>
 *      <li>Transforms must preserve the concept of
 *      <a href="https://en.wikipedia.org/wiki/Parallel_(geometry)">parallelism</a> defined for the space.
 *      This means that hyperplanes that are parallel before the transformation must remain parallel afterwards,
 *      and hyperplanes that intersect must also intersect afterwards. For example, a transform that causes parallel
 *      lines to converge to a single point in Euclidean space (such as the projective transforms used to create
 *      perspective viewpoints in 3D graphics) would not meet this requirement. However, a transform that turns
 *      a square into a rhombus with no right angles would fulfill the requirement, since the two pairs of parallel
 *      lines forming the square remain parallel after the transformation.
 *      </li>
 * </ol>
 *
 * <p>Transforms that meet the above requirements in Euclidean space (and other affine spaces) are known as
 * <a href="https://en.wikipedia.org/wiki/Affine_transformation">affine transforms</a>. Common affine transforms
 * include translation, scaling, rotation, reflection, and any compositions thereof.
 * </p>
 *
 * @param <P> Point implementation type
 * @see <a href="https://en.wikipedia.org/wiki/Geometric_transformation">Geometric Transformation</a>
 */
public interface Transform<P extends Point<P>> extends UnaryOperator<P> {

    /** Get an instance representing the inverse transform.
     * @return an instance representing the inverse transform
     */
    Transform<P> inverse();

    /** Return true if the transform preserves the orientation of the space.
     * For example, in Euclidean 2D space, this will be true for translations,
     * rotations, and scalings but will be false for reflections.
     * @return true if the transform preserves the orientation of the space
     * @see <a href="https://en.wikipedia.org/wiki/Orientation_(vector_space)">Orientation</a>
     */
    boolean preservesOrientation();
}
