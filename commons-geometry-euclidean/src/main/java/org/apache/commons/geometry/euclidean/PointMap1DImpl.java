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

import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.geometry.core.internal.GeometryInternalUtils;
import org.apache.commons.geometry.euclidean.oned.PointMap1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.core.Precision;

/** Internal implementation of {@link PointMap1D}.
 * @param <V> Map value type
 */
final class PointMap1DImpl<V>
    implements PointMap1D<V> {

    /** Underlying map. */
    private final NavigableMap<Vector1D, V> map;

    /** Construct a new instance using the given precision context to determine
     * floating point equality.
     * @param precision precision context
     */
    PointMap1DImpl(final Precision.DoubleEquivalence precision) {
        this.map = new TreeMap<>((a, b) -> precision.compare(a.getX(), b.getX()));
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
    public V put(final Vector1D key, final V value) {
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
    public void putAll(final Map<? extends Vector1D, ? extends V> m) {
        // insert entries one at a time to ensure that we remain consistent
        // with our configured precision
        for (final Map.Entry<? extends Vector1D, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        map.clear();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Vector1D> keySet() {
        return map.keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<V> values() {
        return map.values();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Vector1D, V>> entrySet() {
        return map.entrySet();
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D resolveKey(final Vector1D pt) {
        final Map.Entry<Vector1D, V> entry = resolveEntry(pt);
        return entry != null ?
                entry.getKey() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public Map.Entry<Vector1D, V> resolveEntry(final Vector1D pt) {
        final Map.Entry<Vector1D, V> floor = map.floorEntry(pt);
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
}
