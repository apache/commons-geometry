/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.core.collection;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.geometry.core.Point;

/** {@link Set} containing {@link Point} values. This interface is intended for
 * use in cases where effectively equivalent (but not necessarily equal) points must
 * be considered as equal by the set. As such, this interface breaks the strict contract
 * for {@link Set} where membership is consistent with {@link Object#equals(Object)}.
 *
 * <p><strong>Distance Ordering</strong></p>
 * <p>For methods such as {@link #nearest(Point)} and {@link #nearToFar(Point)}
 * that order elements by distance, implementations are free to choose the criteria used to
 * break ties in distance. For example, if points {@code A} and {@code B} are at equal distances
 * from {@code P}, implementations may choose to return either {@code A} or {@code B} for
 * {@code map.nearest(P)}.
 * </p>
 * @param <P> Point type
 */
public interface PointSet<P extends Point<P>> extends Set<P> {

    /** Get the element equivalent to {@code pt} or {@code null} if no
     * such an element exists.
     * @param pt point to find an equivalent for
     * @return set entry equivalent to {@code pt} or {@code null} if
     *      no such entry exists
     */
    P get(P pt);

    /** Get the element from the set nearest to {@code pt} or {@code null}
     * if the set is empty.
     * @param pt reference point
     * @return the element from the set nearest to {@code pt} or {@code null}
     *      if the set is empty
     */
    P nearest(P pt);

    /** Get the element from the set farthest to {@code pt} or {@code null}
     * if the set is empty.
     * @param pt reference point
     * @return the element from the set farthest to {@code pt} or {@code null}
     *      if the set is empty
     */
    P farthest(P pt);

    /** Get a collection containing the set elements in order of increasing
     * distance from {@code pt}.
     * @param pt reference point
     * @return collection containing the set elements in order of increasing
     *      distance from {@code pt}
     */
    Collection<P> nearToFar(P pt);

    /** Get a collection containing the set elements in order of decreasing
     * distance from {@code pt}.
     * @param pt reference point
     * @return collection containing the set elements in order of decreasing
     *      distance from {@code pt}
     */
    Collection<P> farToNear(P pt);
}
