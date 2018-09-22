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

    /** Linearly interpolates between this vector and the given vector using the equation
     * {@code V = (1 - t)*A + t*B}, where {@code A} is the current vector and {@code B}
     * is the given vector. This means that if {@code t = 0}, a vector equal to the current
     * vector will be returned. If {@code t = 1}, a vector equal to the argument will be returned.
     * The {@code t} parameter is not constrained to the range {@code [0, 1]}, meaning that
     * linear extrapolation can also be performed with this method.
     * @param v other vector
     * @param t interpolation parameter
     * @return interpolated or extrapolated vector
     */
    V lerp(V v, double t);
}
