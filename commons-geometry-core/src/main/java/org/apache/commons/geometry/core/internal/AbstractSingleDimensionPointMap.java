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
package org.apache.commons.geometry.core.internal;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.collection.PointMap;

/** Abstract base class for single-dimension points maps. This class delegates
 * all storage and retrieval operations to an underlying {@link TreeMap} instance.
 * @param <P> Point type
 * @param <V> Value type
 */
public abstract class AbstractSingleDimensionPointMap<P extends Point<P>, V>
    implements PointMap<P, V> {

    /** Underlying tree map. */
    private final TreeMap<P, V> map;

    /** Construct a new instance using the given comparator.
     * @param comparator key comparator
     */
    protected AbstractSingleDimensionPointMap(final Comparator<P> comparator) {
        this.map = new TreeMap<>(comparator);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return map.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(final Object key) {
        return map.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        return map.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public V put(final P key, final V value) {
        GeometryInternalUtils.validatePointMapKey(key);
        return map.put(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        return map.remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(final Map<? extends P, ? extends V> m) {
        // if the input is another point map, then we know that the keys
        // are valid and we can insert them using the standard treemap
        // insertion; otherwise, we need to insert one key at a time
        if (m instanceof AbstractSingleDimensionPointMap) {
            map.putAll(m);
        } else {
            for (final Map.Entry<? extends P, ? extends V> entry : m.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        map.clear();
    }

    /** {@inheritDoc} */
    @Override
    public Set<P> keySet() {
        return map.keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<V> values() {
        return map.values();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<P, V>> entrySet() {
        return map.entrySet();
    }

    /** {@inheritDoc} */
    @Override
    public P resolveKey(final P pt) {
        final Map.Entry<P, V> entry = resolveEntry(pt);
        return entry != null ?
                entry.getKey() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public Map.Entry<P, V> resolveEntry(final P pt) {
        final Map.Entry<P, V> floor = map.floorEntry(pt);
        if (floor != null &&
                map.comparator().compare(floor.getKey(), pt) == 0) {
            return floor;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return map.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        return map.equals(obj);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return map.toString();
    }

    /** Get the underlying map instance.
     * @return underlying map instance
     */
    protected TreeMap<P, V> getMap() {
        return map;
    }
}
