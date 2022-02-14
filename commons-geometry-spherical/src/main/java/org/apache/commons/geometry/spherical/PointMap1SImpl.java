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
package org.apache.commons.geometry.spherical;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.geometry.core.internal.AbstractSingleDimensionPointMap;
import org.apache.commons.geometry.core.internal.GeometryInternalUtils;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.oned.PointMap1S;
import org.apache.commons.numbers.core.Precision;

public class PointMap1SImpl<V> implements PointMap1S<V> {

    /** Underlying tree map. */
    private final TreeMap<Point1S, V> map;

    /** Construct a new instance using the given comparator.
     * @param comparator key comparator
     */
    protected PointMap1SImpl(final Precision.DoubleEquivalence precision) {
        this.map = new TreeMap<>((a, b) -> precision.compare(a.getNormalizedAzimuth(), b.getNormalizedAzimuth()));
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
    public Point1S resolveKey(final Point1S pt) {
        final Map.Entry<Point1S, V> entry = resolveEntry(pt);
        return entry != null ?
                entry.getKey() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public Map.Entry<Point1S, V> resolveEntry(final Point1S pt) {
        final Map.Entry<Point1S, V> floor = map.floorEntry(pt);
        if (floor != null &&
                map.comparator().compare(floor.getKey(), pt) == 0) {
            return floor;
        }
        return null;
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
    public V put(final Point1S key, final V value) {
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
    public void putAll(final Map<? extends Point1S, ? extends V> m) {
        // if the input is another point map, then we know that the keys
        // are valid and we can insert them using the standard treemap
        // insertion; otherwise, we need to insert one key at a time
        if (m instanceof AbstractSingleDimensionPointMap) {
            map.putAll(m);
        } else {
            for (final Map.Entry<? extends Point1S, ? extends V> entry : m.entrySet()) {
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
    public Set<Point1S> keySet() {
        return map.keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<V> values() {
        return map.values();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Point1S, V>> entrySet() {
        return map.entrySet();
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
}
