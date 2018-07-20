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

/** Represents a vector in a Euclidean space of any dimension.
 *
 * @param <P> Point implementation type
 * @param <V> Vector implementation type
 */
public interface EuclideanVector<P extends EuclideanPoint<P, V>, V extends EuclideanVector<P, V>> extends Vector<V> {

    /** Returns a point with the same coordinates as this vector.
     * This is equivalent to the expression {@code P = Z + v}, where
     * {@code v} is this vector, {@code Z} is the zero point, and
     * {@code P} is the returned point.
     * @return point with the same coordinates as this vector
     */
    P asPoint();

    /** Returns the magnitude (i.e. length) of the vector. This is
     * the same value as returned by {@link #getNorm()}.
     * @return the magnitude, or length, of the vector
     * @see #getNorm()
     */
    double getMagnitude();

    /** Returns the squared magnitude of the vector. This is the
     * same value as returned by {@link #getNormSq()}.
     * @return the squared magnitude of the vector
     * @see #getMagnitude()
     * @see #getNormSq()
     */
    double getMagnitudeSq();

    /** Returns a vector with the same direction but with the given
     * magnitude. This is equivalent to calling {@code vec.normalize().scalarMultiply(mag)}
     * but without the intermediate vector.
     * @param magnitude The vector magnitude
     * @return a vector with the same direction as the current instance but the given magnitude
     */
    V withMagnitude(double magnitude);
}
