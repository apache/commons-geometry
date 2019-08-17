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
package org.apache.commons.geometry.core.partitioning;

import org.apache.commons.geometry.core.Point;


/** This interface represents an inversible affine transform in a space.
 * <p>Inversible affine transform include for example scalings,
 * translations, rotations.</p>

 * <p>Transforms are dimension-specific. The consistency rules between
 * the three {@code apply} methods are the following ones for a
 * transformed defined for dimension D:</p>
 * <ul>
 *   <li>
 *     the transform can be applied to a point in the
 *     D-dimension space using its {@link #apply(Point)}
 *     method
 *   </li>
 *   <li>
 *     the transform can be applied to a (D-1)-dimension
 *     hyperplane in the D-dimension space using its
 *     {@link #apply(Hyperplane_Old)} method
 *   </li>
 *   <li>
 *     the transform can be applied to a (D-2)-dimension
 *     sub-hyperplane in a (D-1)-dimension hyperplane using
 *     its {@link #apply(SubHyperplane_Old, Hyperplane_Old, Hyperplane_Old)}
 *     method
 *   </li>
 * </ul>

 * @param <P> Point type defining the embedding space.
 * @param <S> Point type defining the embedded sub-space.
 */
public interface Transform_Old<P extends Point<P>, S extends Point<S>> {

    /** Transform a point of a space.
     * @param point point to transform
     * @return a new object representing the transformed point
     */
    P apply(P point);

    /** Transform an hyperplane of a space.
     * @param hyperplane hyperplane to transform
     * @return a new object representing the transformed hyperplane
     */
    Hyperplane_Old<P> apply(Hyperplane_Old<P> hyperplane);

    /** Transform a sub-hyperplane embedded in an hyperplane.
     * @param sub sub-hyperplane to transform
     * @param original hyperplane in which the sub-hyperplane is
     * defined (this is the original hyperplane, the transform has
     * <em>not</em> been applied to it)
     * @param transformed hyperplane in which the sub-hyperplane is
     * defined (this is the transformed hyperplane, the transform
     * <em>has</em> been applied to it)
     * @return a new object representing the transformed sub-hyperplane
     */
    SubHyperplane_Old<S> apply(SubHyperplane_Old<S> sub, Hyperplane_Old<P> original, Hyperplane_Old<P> transformed);

}
