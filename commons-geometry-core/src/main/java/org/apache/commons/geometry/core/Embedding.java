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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/** This interface defines mappings between a space and one of its subspaces.

 * <p>Subspaces are the lower-dimension subsets of a space. For example,
 * in an n-dimension space, the subspaces are the (n-1) dimension space,
 * the (n-2) dimension space, and so on. This interface can be used regardless
 * of the difference in number of dimensions between the space and the target
 * subspace. For example, a line in 3D Euclidean space can use this interface
 * to map directly from 3D Euclidean space to 1D Euclidean space (ie, the location
 * along the line).</p>
 *
 * @param <P> Point type defining the embedding space.
 * @param <S> Point type defining the embedded subspace.
 */
public interface Embedding<P extends Point<P>, S extends Point<S>> {

    /** Transform a space point into a subspace point.
     * @param point n-dimension point of the space
     * @return lower-dimension point of the subspace corresponding to
     *      the specified space point
     * @see #toSpace
     */
    S toSubspace(P point);

    /** Transform a collection of space points into subspace points.
     * @param points collection of n-dimension points to transform
     * @return collection of transformed lower-dimension points.
     * @see #toSubspace(Point)
     */
    default List<S> toSubspace(final Collection<P> points) {
        return points.stream().map(this::toSubspace).collect(Collectors.toList());
    }

    /** Transform a subspace point into a space point.
     * @param point lower-dimension point of the subspace
     * @return n-dimension point of the space corresponding to the
     *      specified subspace point
     * @see #toSubspace(Point)
     */
    P toSpace(S point);

    /** Transform a collection of subspace points into space points.
     * @param points collection of lower-dimension points to transform
     * @return collection of transformed n-dimension points.
     * @see #toSpace(Point)
     */
    default List<P> toSpace(final Collection<S> points) {
        return points.stream().map(this::toSpace).collect(Collectors.toList());
    }
}
