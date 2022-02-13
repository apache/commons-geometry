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

import java.util.Map;

import org.apache.commons.geometry.core.Point;

/** {@link Map} type that uses points as keys.
 * @param <P> Point type
 * @param <V> Value type
 */
public interface PointMap<P extends Point<P>, V> extends Map<P, V> {

    /** Get the map key equivalent to {@code pt} or null if no such key exists.
     * @param pt point to fetch the corresponding key for
     * @return map key equivalent to {@code pt} or null if no such key
     *      exists
     */
    P resolveKey(P pt);

    /** Get the map entry with a key equivalent to {@code pt} or null
     * if no such entry exists.
     * @param pt point to fetch the map entry for
     * @return map entry for the given point or null if no such entry
     *      exists
     */
    Map.Entry<P, V> resolveEntry(P pt);
}
