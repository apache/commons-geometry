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

/** Base class for affine transform matrices in Euclidean space.
 *
 * @param <V> Vector/point implementation type defining the space.
 * @param <M> Matrix transform implementation type.
 */
public abstract class AbstractAffineTransformMatrix<
        V extends EuclideanVector<V>,
        M extends AbstractAffineTransformMatrix<V, M>>
    implements EuclideanTransform<V> {

    /** Apply this transform to the given vector, ignoring translations and normalizing the
     * result. This is equivalent to {@code transform.applyVector(vec).normalize()} but without
     * the intermediate vector instance.
     *
     * @param vec the vector to transform
     * @return the new, transformed unit vector
     * @throws IllegalArgumentException if the transformed vector coordinates cannot be normalized
     * @see #applyVector(EuclideanVector)
     */
    public abstract V applyDirection(V vec);

    /** Get the determinant of the matrix.
     * @return the determinant of the matrix
     */
    public abstract double determinant();

    /** {@inheritDoc}
     * @throws IllegalStateException if the matrix cannot be inverted
     */
    @Override
    public abstract M inverse();

    /** Return a matrix containing only the linear portion of this transform.
     * The returned instance contains the same matrix elements as this instance
     * but with the translation component set to zero.
     * @return a matrix containing only the linear portion of this transform
     */
    public abstract M linear();

    /** Return a matrix containing the transpose of the linear portion of this transform.
     * The returned instance is linear, meaning it has a translation component of zero.
     * @return a matrix containing the transpose of the linear portion of this transform
     */
    public abstract M linearTranspose();

    /** Return a transform suitable for transforming normals. The returned matrix is
     * the inverse transpose of the linear portion of this instance, i.e.
     * <code>N = (L<sup>-1</sup>)<sup>T</sup></code>, where <code>L</code> is the linear portion
     * of this instance and <code>N</code> is the returned matrix. Note that normals
     * transformed with the returned matrix may be scaled during transformation and require
     * normalization.
     * @return a transform suitable for transforming normals
     * @throws IllegalStateException if the matrix cannot be inverted
     * @see <a href="https://en.wikipedia.org/wiki/Normal_(geometry)#Transforming_normals">Transforming normals</a>
     */
    public M normalTransform() {
        return inverse().linearTranspose();
    }

    /** {@inheritDoc}
     *
     * <p>This method returns true if the determinant of the matrix is positive.</p>
     */
    @Override
    public boolean preservesOrientation() {
        return determinant() > 0.0;
    }
}
