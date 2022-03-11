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

import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import org.apache.commons.geometry.core.internal.AbstractPointMap1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.core.Precision;

/** Internal {@link org.apache.commons.geometry.core.collection.PointMap PointMap}
 * implementation for Euclidean 1D space.
 * @param <V> Map value type
 */
final class PointMap1DImpl<V>
    extends AbstractPointMap1D<Vector1D, V> {

    /** Construct a new instance using the given precision context to determine
     * floating point equality.
     * @param precision precision context
     */
    PointMap1DImpl(final Precision.DoubleEquivalence precision) {
        super((a, b) -> precision.compare(a.getX(), b.getX()));
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(final Object key) {
        return getMap().containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        return getMap().get(key);
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        return getMap().remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        getMap().clear();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Vector1D> keySet() {
        return getMap().keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Vector1D, V>> entrySet() {
        return getMap().entrySet();
    }

    /** {@inheritDoc} */
    @Override
    protected Map.Entry<Vector1D, V> getEntryInternal(final Vector1D key) {
        final NavigableMap<Vector1D, V> map = getMap();
        final Map.Entry<Vector1D, V> floor = map.floorEntry(key);
        if (floor != null &&
                map.comparator().compare(floor.getKey(), key) == 0) {
            return floor;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected V putInternal(final Vector1D key, final V value) {
        return getMap().put(key, value);
    }
}
