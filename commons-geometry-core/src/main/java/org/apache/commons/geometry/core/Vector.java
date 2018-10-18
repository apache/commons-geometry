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

import org.apache.commons.geometry.core.exception.IllegalNormException;

/** Interface representing a vector in a vector space or displacement vectors
 * in an affine space.
 *
 * <p>This interface uses self-referencing generic parameters to ensure
 * that implementations are only used with instances of their own type.
 * This removes the need for casting inside of methods in order to access
 * implementation-specific data, such as coordinate values.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Vector_space">Vector space</a>
 * @see <a href="https://en.wikipedia.org/wiki/Affine_space">Affine space</a>
 *
 * @param <V> Vector implementation type
 */
public interface Vector<V extends Vector<V>> extends Spatial {

    /** Get the zero (null) vector of the space.
     * @return zero vector of the space
     */
    V getZero();

    /** Get the L<sub>2</sub> norm (commonly known as the Euclidean norm) for the vector.
     * This corresponds to the common notion of vector magnitude or length and
     * is defined as the square root of the sum of the squares of all vector components.
     * @see <a href="http://mathworld.wolfram.com/L2-Norm.html">L2 Norm</a>
     * @return L<sub>2</sub> norm for the vector
     */
    double getNorm();

    /** Get the square of the L<sub>2</sub> norm (also known as the Euclidean norm)
     * for the vector. This is equal to the sum of the squares of all vector components.
     * @see #getNorm()
     * @return square of the L<sub>2</sub> norm for the vector
     */
    double getNormSq();

    /** Returns a vector with the same direction but with the given
     * norm. This is equivalent to calling {@code vec.normalize().scalarMultiply(mag)}
     * but without the intermediate vector.
     * @param norm The vector norm
     * @return a vector with the same direction as the current instance but the given norm
     */
    V withNorm(double norm);

    /** Add a vector to the instance.
     * @param v vector to add
     * @return a new vector
     */
    V add(V v);

    /** Add a scaled vector to the instance.
     * @param factor scale factor to apply to v before adding it
     * @param v vector to add
     * @return a new vector
     */
    V add(double factor, V v);

    /** Subtract a vector from the instance.
     * @param v vector to subtract
     * @return a new vector
     */
    V subtract(V v);

    /** Subtract a scaled vector from the instance.
     * @param factor scale factor to apply to v before subtracting it
     * @param v vector to subtract
     * @return a new vector
     */
    V subtract(double factor, V v);

    /** Get the negation of the instance.
     * @return a new vector which is the negation of the instance
     */
    V negate();

    /** Get a normalized vector aligned with the instance. The returned
     * vector has a magnitude of 1.
     * @return a new normalized vector
     * @exception IllegalNormException if the norm is zero, NaN, or infinite
     */
    V normalize();

    /** Multiply the instance by a scalar.
     * @param a scalar
     * @return a new vector
     */
    V scalarMultiply(double a);

    /** Compute the distance between the instance and another vector.
     * @param v second vector
     * @return the distance between the instance and v
     */
    double distance(V v);

    /** Compute the square of the distance between the instance and another vector.
     * <p>Calling this method is equivalent to calling:
     * <code>q.subtract(p).getNormSq()</code> except that no intermediate
     * vector is built</p>
     * @see #getNormSq()
     * @param v second vector
     * @return the square of the distance between the instance and p
     */
    double distanceSq(V v);

    /** Compute the dot-product of the instance and another vector.
     * @param v second vector
     * @return the dot product this &middot; v
     */
    double dotProduct(V v);

    /** Compute the angular separation between two vectors in radians.
     * @param v other vector
     * @return angular separation between this instance and v in radians
     * @exception IllegalNormException if either vector has a zero, NaN, or infinite norm
     */
    double angle(V v);
}
