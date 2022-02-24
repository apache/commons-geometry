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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.geometry.core.internal.GeometryInternalUtils;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.oned.PointMap1S;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;

/** Internal implementation of {@link PointMap1S}.
 * @param <V> Map value type
 */
final class PointMap1SImpl<V> implements PointMap1S<V> {

    /** Precision context used to determine floating point equality. */
    private final Precision.DoubleEquivalence precision;

    /** Underlying map. */
    private final NavigableMap<Point1S, V> map;

    /** Minimum key in the map, or null if not known. */
    private Point1S minKey;

    /** Maximum key in the map, or null if not known. */
    private Point1S maxKey;

    /** Construct a new instance using the given precision object to determine
     * floating point equality.
     * @param precision object used to determine floating point equality
     */
    PointMap1SImpl(final Precision.DoubleEquivalence precision) {
        this.precision = precision;
        this.map = new TreeMap<>((a, b) ->
            Double.compare(a.getNormalizedAzimuth(), b.getNormalizedAzimuth()));
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
        return getKey(resolveEntry(pt));
    }

    /** {@inheritDoc} */
    @Override
    public Map.Entry<Point1S, V> resolveEntry(final Point1S pt) {
        final Map.Entry<Point1S, V> floor = map.floorEntry(pt);
        if (floor != null && keyEq(pt, floor)) {
            return floor;
        } else {
            final Map.Entry<Point1S, V> ceiling = map.ceilingEntry(pt);
            if (ceiling != null && keyEq(pt, ceiling)) {
                return ceiling;
            } else if (pt.getNormalizedAzimuth() < Math.PI) {
                if (wrapsLowToHigh(pt)) {
                    return map.lastEntry();
                }
            } else if (wrapsHighToLow(pt)) {
                return map.firstEntry();
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(final Object key) {
        return resolveKey((Point1S) key) != null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        return getValue(resolveEntry((Point1S) key));
    }

    /** {@inheritDoc} */
    @Override
    public V put(final Point1S key, final V value) {
        GeometryInternalUtils.validatePointMapKey(key);

        final Map.Entry<Point1S, V> entry = resolveEntry(key);
        if (entry != null) {
            return map.put(entry.getKey(), value);
        }

        final V result = map.put(key, value);
        mapUpdated();

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        final V result = map.remove(key);
        mapUpdated();

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(final Map<? extends Point1S, ? extends V> m) {
        // insert entries one at a time to ensure that we remain consistent
        // with our configured precision
        for (final Map.Entry<? extends Point1S, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        map.clear();
        mapUpdated();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Point1S> keySet() {
        return new KeySet();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<V> values() {
        return map.values();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Point1S, V>> entrySet() {
        return new EntrySet();
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

    /** Method called when the map is updated.
     */
    private void mapUpdated() {
        minKey = null;
        maxKey = null;
    }

    /** Return true if {@code pt} is directly equivalent to the key for {@code entry},
     * without considering wrap around.
     * @param pt point
     * @param entry map entry
     * @return true if {@code pt} is directly equivalent to the key for {@code entry},
     *      without considering wrap around
     */
    private boolean keyEq(final Point1S pt, final Map.Entry<Point1S, V> entry) {
        return precision.eq(pt.getNormalizedAzimuth(), entry.getKey().getNormalizedAzimuth());
    }

    /** Return true if the given point wraps around the zero point from high to low
     * and is equivalent to the first point in the map.
     * @param pt point to check
     * @return true if the normalized azimuth of {@code pt} plus 2pi is equivalent
     *      to the first key in the map
     */
    private boolean wrapsHighToLow(final Point1S pt) {
        if (map.size() > 0) {
            if (minKey == null) {
                minKey = map.firstKey();
            }

            final double adjustedAz = pt.getNormalizedAzimuth() - Angle.TWO_PI;
            return precision.eq(adjustedAz, minKey.getNormalizedAzimuth());
        }

        return false;
    }

    /** Return true if the given point wraps around the zero point from low to high
     * and is equivalent to the last point in the map.
     * @param pt point to check
     * @return true if the normalized azimuth of {@code pt} minus 2pi is equivalent
     *      to the first key in the map
     */
    private boolean wrapsLowToHigh(final Point1S pt) {
        if (map.size() > 0) {
            if (maxKey == null) {
                maxKey = map.lastKey();
            }

            final double adjustedAz = pt.getNormalizedAzimuth() + Angle.TWO_PI;
            return precision.eq(adjustedAz, maxKey.getNormalizedAzimuth());
        }

        return false;
    }

    /** Null-safe method to get the value from a map entry.
     * @param <K> Key type
     * @param entry map entry
     * @return map key or null if {@code entry} is null
     */
    private static <K> K getKey(final Map.Entry<K, ?> entry) {
        return entry != null ?
                entry.getKey() :
                null;
    }

    /** Null-safe method to get the value from a map entry.
     * @param <V> Value type
     * @param entry map entry
     * @return map value or null if {@code entry} is null
     */
    private static <V> V getValue(final Map.Entry<?, V> entry) {
        return entry != null ?
                entry.getValue() :
                null;
    }

    /** Key set view of the map.
     */
    private final class KeySet
        extends AbstractSet<Point1S> {

        /** {@inheritDoc} */
        @Override
        public boolean contains(final Object obj) {
            return PointMap1SImpl.this.containsKey(obj);
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return PointMap1SImpl.this.size();
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<Point1S> iterator() {
            return new MapIterator<>(map.keySet().iterator());
        }
    }

    /** Entry set view of the map.
     */
    private final class EntrySet
        extends AbstractSet<Map.Entry<Point1S, V>> {

        /** {@inheritDoc} */
        @Override
        public boolean contains(final Object obj) {
            if (obj instanceof Map.Entry) {
                final Map.Entry<?, ?> search = (Map.Entry<?, ?>) obj;
                final Object key = search.getKey();

                final Map.Entry<Point1S, V> actual = resolveEntry((Point1S) key);
                if (actual != null) {
                    return actual.getKey().eq((Point1S) search.getKey(), precision) &&
                            Objects.equals(actual.getValue(), search.getValue());
                }
            }
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return PointMap1SImpl.this.size();
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<Entry<Point1S, V>> iterator() {
            return new MapIterator<>(map.entrySet().iterator());
        }
    }

    /** Iterator for iterating through elements in the map.
     * @param <E> Element type
     */
    private final class MapIterator<E>
        implements Iterator<E> {

        /** Underlying iterator. */
        private final Iterator<E> it;

        /** Construct a new instance that wraps the given iterator.
         * @param it underlying iterator
         */
        MapIterator(final Iterator<E> it) {
            this.it = it;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        /** {@inheritDoc} */
        @Override
        public E next() {
            return it.next();
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            it.remove();
            mapUpdated();
        }
    }
}
