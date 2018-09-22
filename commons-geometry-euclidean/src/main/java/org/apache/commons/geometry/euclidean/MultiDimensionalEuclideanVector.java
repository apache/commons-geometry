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

import org.apache.commons.geometry.core.Vector;
import org.apache.commons.geometry.core.exception.IllegalNormException;

/**
 * Euclidean vector extension interface with methods applicable to spaces of
 * two or more dimensions.
 *
 * @param <P> Point implementation type
 * @param <V> Vector implementation type
 */
public interface MultiDimensionalEuclideanVector<P extends EuclideanPoint<P, V>, V extends MultiDimensionalEuclideanVector<P, V>>
        extends EuclideanVector<P, V> {

    /** Get the projection of the instance onto the given base vector. The returned
     * vector is parallel to {@code base}. Vector projection and rejection onto
     * a given base are related by the equation
     * <code>
     *      <strong>v</strong> = <strong>v<sub>projection</sub></strong> + <strong>v<sub>rejection</sub></strong>
     * </code>
     * @param base base vector
     * @return the vector projection of the instance onto {@code base}
     * @exception IllegalNormException if the norm of the base vector is zero, NaN, or infinite
     * @see #reject(Vector)
     */
    V project(V base);

    /** Get the rejection of the instance from the given base vector. The returned
     * vector is orthogonal to {@code base}. This operation can be interpreted as
     * returning the orthogonal projection of the instance onto the hyperplane
     * orthogonal to {@code base}. Vector projection and rejection onto
     * a given base are related by the equation
     * <code>
     *      <strong>v</strong> = <strong>v<sub>projection</sub></strong> + <strong>v<sub>rejection</sub></strong>
     * </code>
     * @param base base vector
     * @return the vector rejection of the instance from {@code base}
     * @exception IllegalNormException if the norm of the base vector is zero, NaN, or infinite
     * @see #project(Vector)
     */
    V reject(V base);

    /** Get a unit vector orthogonal to the instance.
     * @return a unit vector orthogonal to the current instance
     * @throws IllegalNormException if the norm of the current instance is zero, NaN,
     *  or infinite
     */
    public V orthogonal();

    /** Get a unit vector orthogonal to the current vector and pointing in the direction
     * of {@code dir}. This method is equivalent to calling {@code dir.reject(vec).normalize()}
     * except that no intermediate vector object is produced.
     * @param dir the direction to use for generating the orthogonal vector
     * @return unit vector orthogonal to the current vector and pointing in the direction of
     *      {@code dir} that does not lie along the current vector
     * @throws IllegalNormException if either vector norm is zero, NaN or infinite,
     *      or the given vector is collinear with this vector.
     */
    public V orthogonal(V dir);
}