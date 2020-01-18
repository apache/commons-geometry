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

/** This interface represents <em>inversible affine transforms</em> in a space. Common examples of this type of
 * transform in Euclidean space include scalings, translations, and rotations.
 *
 * <h2>Implementation Note</h2>
 * <p>Implementations are responsible for ensuring that they meet the geometric
 * requirements for this interface. These requirements are:
 * <ol>
 *      <li>The transform must be <a href="https://en.wikipedia.org/wiki/Affine_transformation">affine</a>.
 *      In basic terms, this means that the transform must retain the "straightness" and "parallelness" of
 *      lines and planes (or whatever is an equivalent concept for the space). For example, a translation or
 *      rotation in Euclidean 3D space meets this requirement because all lines that are parallel before the
 *      transform remain parallel afterwards. However, a projective transform that causes previously parallel
 *      lines to meet at a single point does not.
 *      </li>
 *      <li>The transform must be <em>inversible</em>. An inverse transform must exist that will return
 *      the original point if given the transformed point. In other words, for a transform {@code t}, there
 *      must exist an inverse {@code inv} such that {@code inv.apply(t.apply(pt))} returns a point equal to
 *      the input point {@code pt}.
 *      </li>
 * </ol>
 * Implementations that do not meet these requirements cannot be expected to produce correct results in
 * algorithms that use this interface.
 *
 * @param <P> Point implementation type
 * @see <a href="https://en.wikipedia.org/wiki/Affine_transformation">Affine Transformation</a>
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
