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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.geometry.core.internal.AbstractPointMap1D;
import org.apache.commons.geometry.core.internal.DistancedValue;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;

/** Internal {@link org.apache.commons.geometry.core.collection.PointMap PointMap}
 * implementation for 1D spherical space. This class uses a {@link NavigableMap}
 * internally with special logic to handle wrap around.
 * @param <V> Map value type
 */
final class PointMap1SImpl<V>
    extends AbstractPointMap1D<Point1S, V> {

    /** Minimum key in the map, or {@code null} if not known. */
    private Point1S minKey;

    /** Maximum key in the map, or {@code null} if not known. */
    private Point1S maxKey;

    /** Modification version of the map. */
    private int version;

    /** Construct a new instance using the given precision object to determine
     * floating point equality.
     * @param precision object used to determine floating point equality
     */
    PointMap1SImpl(final Precision.DoubleEquivalence precision) {
        super(precision, Point1S::getNormalizedAzimuth);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(final Object key) {
        return getEntryInternal((Point1S) key) != null;
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        return getValue(getEntryInternal((Point1S) key));
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        final Entry<Point1S, V> entry = getEntryInternal((Point1S) key);
        if (entry != null) {
            final V result = getMap().remove(entry.getKey());

            mapUpdated();

            return result;
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        getMap().clear();
        mapUpdated();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Point1S> keySet() {
        return new KeySet();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Point1S, V>> entrySet() {
        return new EntrySet();
    }

    /** {@inheritDoc} */
    @Override
    protected Iterator<Entry<Point1S, V>> nearToFarIterator(final Point1S pt) {
        return new NearToFarIterator(pt);
    }

    /** {@inheritDoc} */
    @Override
    protected Iterator<Entry<Point1S, V>> farToNearIterator(final Point1S pt) {
        return new NearToFarIterator(pt.antipodal());
    }

    /** {@inheritDoc} */
    @Override
    protected Entry<Point1S, V> getEntryInternal(final Point1S pt) {
        final NavigableMap<Point1S, V> map = getMap();

        final Entry<Point1S, V> floor = map.floorEntry(pt);
        if (floor != null && keyEq(pt, floor)) {
            return floor;
        } else {
            if (pt.getNormalizedAzimuth() < Math.PI) {
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
    protected V putInternal(final Point1S key, final V value) {
        final NavigableMap<Point1S, V> map = getMap();

        final Entry<Point1S, V> entry = getEntryInternal(key);
        if (entry != null) {
            return map.put(entry.getKey(), value);
        }

        final V result = map.put(key, value);
        mapUpdated();

        return result;
    }

    /** Method called when the map is updated.
     */
    private void mapUpdated() {
        minKey = null;
        maxKey = null;

        ++version;
    }

    /** Return true if {@code pt} is directly equivalent to the key for {@code entry},
     * without considering wrap around.
     * @param pt point
     * @param entry map entry
     * @return true if {@code pt} is directly equivalent to the key for {@code entry},
     *      without considering wrap around
     */
    private boolean keyEq(final Point1S pt, final Entry<Point1S, V> entry) {
        return getPrecision().eq(pt.getNormalizedAzimuth(), entry.getKey().getNormalizedAzimuth());
    }

    /** Return true if the given point wraps around the zero point from high to low
     * and is equivalent to the first point in the map.
     * @param pt point to check
     * @return true if the normalized azimuth of {@code pt} plus 2pi is equivalent
     *      to the first key in the map
     */
    private boolean wrapsHighToLow(final Point1S pt) {
        if (size() > 0) {
            if (minKey == null) {
                minKey = getMap().firstKey();
            }

            final double adjustedAz = pt.getNormalizedAzimuth() - Angle.TWO_PI;
            return getPrecision().eq(adjustedAz, minKey.getNormalizedAzimuth());
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
        if (size() > 0) {
            if (maxKey == null) {
                maxKey = getMap().lastKey();
            }

            final double adjustedAz = pt.getNormalizedAzimuth() + Angle.TWO_PI;
            return getPrecision().eq(adjustedAz, maxKey.getNormalizedAzimuth());
        }

        return false;
    }

    /** Null-safe method to get the value from a map entry.
     * @param <V> Value type
     * @param entry map entry
     * @return map value or {@code null} if {@code entry} is {@code null}
     */
    private static <V> V getValue(final Entry<?, V> entry) {
        return entry != null ?
                entry.getValue() :
                null;
    }

    /** Key set view of the map.
     */
    private final class KeySet
        extends AbstractSet<Point1S> {

        /** Create an instance. */
        KeySet() {
            // Allow package level instantiation
        }

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
            return new MapIterator<>(getMap().keySet().iterator());
        }
    }

    /** Entry set view of the map.
     */
    private final class EntrySet
        extends AbstractSet<Entry<Point1S, V>> {

        /** Create an instance. */
        EntrySet() {
            // Allow package level instantiation
        }

        /** {@inheritDoc} */
        @Override
        public boolean contains(final Object obj) {
            if (obj instanceof Entry) {
                final Entry<?, ?> search = (Entry<?, ?>) obj;
                final Object key = search.getKey();

                final Entry<Point1S, V> actual = getEntry((Point1S) key);
                if (actual != null) {
                    return actual.getKey().eq((Point1S) search.getKey(), getPrecision()) &&
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
            return new MapIterator<>(getMap().entrySet().iterator());
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

    /** Iterator that returns the entries in order of ascending distance from
     * a given reference point.
     */
    private final class NearToFarIterator
        implements Iterator<Entry<Point1S, V>> {

        /** Reference point to measure distances against. */
        private final Point1S refPt;

        /** Low entry iterator. */
        private Iterator<Entry<Point1S, V>> low;

        /** High entry iterator. */
        private Iterator<Entry<Point1S, V>> high;

        /** Low-valued entry. */
        private DistancedValue<Entry<Point1S, V>> lowEntry;

        /** High-valued entry. */
        private DistancedValue<Entry<Point1S, V>> highEntry;

        /** Number of entries remaining to retrieve from each iterator. */
        private int remaining;

        /** Expected modification version of the map. */
        private final int expectedVersion;

        /** Construct a new instance with the given reference point.
         * @param refPt reference point
         */
        NearToFarIterator(final Point1S refPt) {
            this.refPt = refPt;

            this.low = getMap().descendingMap().tailMap(refPt, false)
                    .entrySet().iterator();
            this.high = getMap().tailMap(refPt).entrySet().iterator();

            this.remaining = getMap().size();

            this.expectedVersion = version;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            if (lowEntry == null && remaining > 0) {
                lowEntry = getNextEntry(low);

                if (lowEntry == null) {
                    low = getMap().descendingMap().entrySet().iterator();

                    lowEntry = getNextEntry(low);
                }
            }
            if (highEntry == null && remaining > 0) {
                highEntry = getNextEntry(high);

                if (highEntry == null) {
                    high = getMap().entrySet().iterator();

                    highEntry = getNextEntry(high);
                }
            }

            return lowEntry != null || highEntry != null;
        }

        /** {@inheritDoc} */
        @Override
        public Entry<Point1S, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            checkVersion();

            final DistancedValue<Entry<Point1S, V>> result;
            if (lowEntry != null &&
                    (highEntry == null || lowEntry.getDistance() <= highEntry.getDistance())) {
                result = lowEntry;
                lowEntry = null;
            } else {
                result = highEntry;
                highEntry = null;
            }

            return result != null ?
                    result.getValue() :
                    null;
        }

        /** Get a {@link DistancedValue} instance containing the next entry from the given
         * iterator.
         * @param it iterator to get the next value from
         * @return distanced value containing the next entry from the iterator or {@code null} if the iterator
         *      does not contain any more elements
         */
        private DistancedValue<Entry<Point1S, V>> getNextEntry(final Iterator<Entry<Point1S, V>> it) {
            if (it.hasNext()) {
                --remaining;

                final Entry<Point1S, V> entry = it.next();
                return DistancedValue.of(entry, refPt.distance(entry.getKey()));
            }
            return null;
        }

        /** Throw a {@link ConcurrentModificationException} if the map version does
         * not match the expected version.
         */
        private void checkVersion() {
            if (expectedVersion != version) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
