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
package org.apache.commons.geometry.euclidean;

import org.apache.commons.geometry.core.Transform;

/** Interface representing affine transforms in Euclidean space. An affine transform is one that preserves
 * points, straight lines, planes, and sets of parallel lines. Common affine transforms include translation,
 * rotation, scaling, reflection and any compositions thereof.
 *
 * @param <V> Vector implementation type
 * @see <a href="https://en.wikipedia.org/wiki/Affine_transformation">Affine Transformation</a>
 */
public interface AffineTransform<V extends EuclideanVector<V>> extends Transform<V> {

    /** Apply this transform to the given vector, ignoring translations.
    *
    * <p>This method can be used to transform vector instances representing displacements between points.
    * For example, if {@code v} represents the difference between points {@code p1} and {@code p2},
    * then {@code transform.applyVector(v)} will represent the difference between {@code p1} and {@code p2}
    * after {@code transform} is applied.
    * </p>
    *
    * @param vec the vector to transform
    * @return the new, transformed vector
    */
    V applyVector(V vec);
}
