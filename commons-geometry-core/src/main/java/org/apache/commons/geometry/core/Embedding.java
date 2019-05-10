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

import org.apache.commons.geometry.core.partition.Hyperplane;

/** This interface defines mappers between a space and one of its subspaces.

 * <p>Subspaces are the lower dimensions subsets of a n-dimensions
 * space. The (n-1)-dimension subspaces are specific subspaces known
 * as {@link Hyperplane hyperplanes}. This interface can be used regardless
 * of the dimensions differences. For example, a line in 3D Euclidean space
 * can map directly from 3 dimensions to 1.</p>

 * <p>In the 3D Euclidean space, hyperplanes are 2D planes, and the 1D
 * subspaces are lines.</p>

 * @param <P> Point type defining the embedding space.
 * @param <S> Point type defining the embedded subspace.

 * @see Hyperplane
 */
public interface Embedding<P extends Point<P>, S extends Point<S>> {

    /** Transform a space point into a subspace point.
     * @param point n-dimension point of the space
     * @return (n-1)-dimension point of the subspace corresponding to
     *  the specified space point
     * @see #toSpace
     */
    S toSubspace(P point);

    /** Transform a sub-space point into a space point.
     * @param point (n-1)-dimension point of the sub-space
     * @return n-dimension point of the space corresponding to the
     * specified sub-space point
     * @see #toSubspace(Point)
     */
    P toSpace(S point);
}
