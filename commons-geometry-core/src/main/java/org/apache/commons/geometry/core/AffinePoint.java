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

/** Interface that adds affine space operations to the base {@link Point}
 * interface. Affine spaces consist of points and displacement vectors
 * representing translations between points. Since this interface extends
 * {@link Point}, the represented space is both affine and metric.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Affine_space">Affine space</a>
 * @see <a href="https://en.wikipedia.org/wiki/Metric_space">Metric space</a>
 * @see Point
 *
 * @param <P> Point implementation type
 * @param <V> Vector implementation type
 */
public interface AffinePoint<P extends AffinePoint<P, V>, V extends Vector<V>> extends Point<P> {

    /** Returns the displacement vector from this point to p.
     * @param p second point
     * @return The displacement vector from this point to p.
     */
    V subtract(P p);

    /** Returns the point resulting from adding the given displacement
     * vector to this point.
     * @param v displacement vector
     * @return point resulting from displacing this point by v
     */
    P add(V v);
}
