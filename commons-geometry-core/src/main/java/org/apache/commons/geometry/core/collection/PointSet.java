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
package org.apache.commons.geometry.core.collection;

import java.util.Set;

import org.apache.commons.geometry.core.Point;

/** {@link Set} containing {@link Point} values. This interface is intended for
 * use in cases where effectively equivalent (but not necessarily equal) points must
 * be considered as equal by the set. As such, this interface breaks the strict contract
 * for {@link Set} where membership is consistent with {@link Object#equals(Object)}.
 * @param <P> Point type
 */
public interface PointSet<P extends Point<P>> extends Set<P> {

    /** Get the set entry equivalent to {@code pt} or null if no
     * such entry exists.
     * @param pt point to find an equivalent for
     * @return set entry equivalent to {@code pt} or null if
     *      no such entry exists
     */
    P get(P pt);
}
