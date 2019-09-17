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

import org.apache.commons.geometry.core.exception.IllegalNormException;

/** Base class for affine transform matrices in Euclidean space.
 *
 * @param <V> Vector/point implementation type defining the space.
 */
public abstract class AbstractAffineTransformMatrix<V extends EuclideanVector<V>>
    implements EuclideanTransform<V> {

    /** Apply this transform to the given vector, ignoring translations and normalizing the
     * result. This is equivalent to {@code transform.applyVector(vec).normalize()} but without
     * the intermediate vector instance.
     *
     * @param vec the vector to transform
     * @return the new, transformed unit vector
     * @throws IllegalNormException if the transformed vector coordinates cannot be normalized
     * @see #applyVector(EuclideanVector)
     */
    public abstract V applyDirection(V vec);

    /** Get the determinant of the matrix.
     * @return the determinant of the matrix
     */
    public abstract double determinant();

    /** {@inheritDoc}
     *
     * <p>This method returns true if the determinant of the matrix is positive.</p>
     */
    @Override
    public boolean preservesOrientation() {
        return determinant() > 0.0;
    }
}
