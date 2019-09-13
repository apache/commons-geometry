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

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.IllegalNormException;

/** Interface representing an affine transform matrix in Euclidean space.
 * Rotation, scaling, and translation are examples of affine transformations.
 *
 * @param <V> Vector/point implementation type defining the space.
 * @param <S> Point type defining the embedded sub-space.
 * @see <a href="https://en.wikipedia.org/wiki/Affine_transformation">Affine transformation</a>
 */
public interface AffineTransformMatrix<V extends EuclideanVector<V>, S extends Point<S>> extends Transform<V> {

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
    * @see #applyDirection(EuclideanVector)
    */
    V applyVector(V vec);

    /** Apply this transform to the given vector, ignoring translations and normalizing the
     * result. This is equivalent to {@code transform.applyVector(vec).normalize()} but without
     * the intermediate vector instance.
     *
     * @param vec the vector to transform
     * @return the new, transformed unit vector
     * @throws IllegalNormException if the transformed vector coordinates cannot be normalized
     * @see #applyVector(EuclideanVector)
     */
    V applyDirection(V vec);

    /** Get the determinant of the matrix.
     * @return the determinant of the matrix
     */
    double determinant();

    /** {@inheritDoc} */
    @Override
    default public boolean preservesOrientation() {
        // orientation is preserved only with non-negative determinants
        return determinant() > 0.0;
    }
}
