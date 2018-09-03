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

/** Interface representing a vector in a vector space with two or more
 * dimensions.
 *
 * @param <V> Vector implementation type
 */
public interface MultiDimensionalVector<V extends MultiDimensionalVector<V>> extends Vector<V> {

    /** Get the projection of the instance onto the given base vector. The returned
     * vector is parallel to {@code base}. Vector projection and rejection onto
     * a given base are related by the equation
     * <code>
     *      <strong>v</strong> = <strong>v<sub>projection</sub></strong> + <strong>v<sub>rejection</sub></strong>
     * </code>
     * @param base
     * @return the vector projection of the instance onto {@code base}
     * @exception IllegalStateException if the norm of the base vector is zero
     * @see #reject(MultiDimensionalVector)
     */
    V project(V base) throws IllegalStateException;

    /** Get the rejection of the instance from the given base vector. The returned
     * vector is orthogonal to {@code base}. This operation can be interpreted as
     * returning the orthogonal projection of the instance onto the hyperplane
     * orthogonal to {@code base}. Vector projection and rejection onto
     * a given base are related by the equation
     * <code>
     *      <strong>v</strong> = <strong>v<sub>projection</sub></strong> + <strong>v<sub>rejection</sub></strong>
     * </code>
     * @param base
     * @return the vector rejection of the instance from {@code base}
     * @exception IllegalStateException if the norm of the base vector is zero
     * @see #project(MultiDimensionalVector)
     */
    V reject(V base) throws IllegalStateException;

    /** Compute the angular separation in radians between two vectors.
     * @param v other vector
     * @return angular separation between this instance and v in radians
     * @exception IllegalStateException if either vector has a zero norm
     */
    double angle(V v) throws IllegalStateException;
}
