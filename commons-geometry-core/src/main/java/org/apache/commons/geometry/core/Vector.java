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

/** Interface representing a vector in a vector space. The most common
 * use of this interface is to represent displacement vectors in an affine
 * space.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Vector_space">Vector space</a>
 * @see <a href="https://en.wikipedia.org/wiki/Affine_space">Affine space</a>
 *
 * @see AffinePoint
 *
 * @param <V> Vector implementation type
 */
public interface Vector<V extends Vector<V>> extends Spatial {

    /** Get the zero (null) vector of the space.
     * @return zero vector of the space
     */
    V getZero();

    /** Get the L<sub>1</sub> norm for the vector. This is defined as the
     * sum of the absolute values of all vector components.
     *
     * @see <a href="http://mathworld.wolfram.com/L1-Norm.html">L1 Norm</a>
     * @return L<sub>1</sub> norm for the vector
     */
    double getNorm1();

    /** Get the L<sub>2</sub> norm (commonly known as the Euclidean norm) for the vector.
     * This corresponds to the common notion of vector magnitude or length.
     * This is defined as the square root of the sum of the squares of all vector components.
     * @see <a href="http://mathworld.wolfram.com/L2-Norm.html">L2 Norm</a>
     * @return Euclidean norm for the vector
     */
    double getNorm();

    /** Get the square of the L<sub>2</sub> norm (also known as the Euclidean norm)
     * for the vector. This is equal to the sum of the squares of all vector components.
     * @see #getNorm()
     * @return square of the Euclidean norm for the vector
     */
    double getNormSq();

    /** Get the L<sub>&infin;</sub> norm for the vector. This is defined as the
     * maximum of the absolute values of all vector components.
     * @see <a href="http://mathworld.wolfram.com/L-Infinity-Norm.html">L<sub>&infin;</sub> Norm</a>
     * @return L<sub>&infin;</sub> norm for the vector
     */
    double getNormInf();

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
     * @exception IllegalStateException if the norm is zero
     */
    V normalize() throws IllegalStateException;

    /** Multiply the instance by a scalar.
     * @param a scalar
     * @return a new vector
     */
    V scalarMultiply(double a);

    /** Compute the distance between the instance and another vector according to the L<sub>1</sub> norm.
     * <p>Calling this method is equivalent to calling:
     * <code>q.subtract(p).getNorm1()</code> except that no intermediate
     * vector is built</p>
     * @see #getNorm1()
     * @param v second vector
     * @return the distance between the instance and p according to the L<sub>1</sub> norm
     */
    double distance1(V v);

    /** Compute the distance between the instance and another vector.
     * @param v second vector
     * @return the distance between the instance and v
     */
    double distance(V v);

    /** Compute the distance between the instance and another vector according to the L<sub>&infin;</sub> norm.
     * <p>Calling this method is equivalent to calling:
     * <code>q.subtract(p).getNormInf()</code> except that no intermediate
     * vector is built</p>
     * @see #getNormInf()
     * @param v second vector
     * @return the distance between the instance and p according to the L<sub>&infin;</sub> norm
     */
    double distanceInf(V v);

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
}
