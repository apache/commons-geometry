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
package org.apache.commons.geometry.core.internal;

import java.util.AbstractCollection;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.numbers.core.Precision;

/** Abstract base class for 1D {@link PointMap} implementations. This class delegates
 * entry storage to an internal {@link TreeMap} instance. Simple methods, such as
 * {@link Map#size()} are directly implemented here but subclasses must provide their
 * own logic for manipulating the map entries.
 * @param <P> Point type
 * @param <V> Value type
 */
public abstract class AbstractPointMap1D<P extends Point<P>, V>
    implements PointMap<P, V> {

    /** Precision context. */
    private final Precision.DoubleEquivalence precision;

    /** Underlying map. */
    private final NavigableMap<P, V> map;

    /** Construct a new instance that uses the given precision and coordinate accessor
     * function to sort elements.
     * @param precision precision object used for floating point comparisons
     * @param coordinateFn function used to obtain coordinate values from point instance
     */
    protected AbstractPointMap1D(
            final Precision.DoubleEquivalence precision,
            final ToDoubleFunction<P> coordinateFn) {
        this.precision = precision;
        this.map = new TreeMap<>(
                (a, b) -> precision.compare(coordinateFn.applyAsDouble(a), coordinateFn.applyAsDouble(b)));
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
    public boolean containsValue(final Object value) {
        return map.containsValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public Entry<P, V> getEntry(final P key) {
        return exportEntry(getEntryInternal(key));
    }

    /** {@inheritDoc} */
    @Override
    public V put(final P key, final V value) {
        GeometryInternalUtils.requireFinite(key);
        return putInternal(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(final Map<? extends P, ? extends V> m) {
        for (final Entry<? extends P, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /** {@inheritDoc} */
    @Override
    public Collection<V> values() {
        return map.values();
    }

    /** {@inheritDoc} */
    @Override
    public Entry<P, V> nearestEntry(final P pt) {
        GeometryInternalUtils.requireFinite(pt);

        final Iterator<Entry<P, V>> it = nearToFarIterator(pt);
        return it.hasNext() ?
                it.next() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public Entry<P, V> farthestEntry(final P pt) {
        GeometryInternalUtils.requireFinite(pt);

        final Iterator<Entry<P, V>> it = farToNearIterator(pt);
        return it.hasNext() ?
                it.next() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Entry<P, V>> entriesNearToFar(final P pt) {
        GeometryInternalUtils.requireFinite(pt);

        return new AbstractEntryCollection() {
            @Override
            public Iterator<Entry<P, V>> iterator() {
                return nearToFarIterator(pt);
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Entry<P, V>> entriesFarToNear(final P pt) {
        GeometryInternalUtils.requireFinite(pt);

        return new AbstractEntryCollection() {
            @Override
            public Iterator<Entry<P, V>> iterator() {
                return farToNearIterator(pt);
            }
        };
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

    /** Get the raw {@link Map.Entry} for the given key from the underlying map.
     * @param key map key
     * @return entry for the given key or {@code null} if not found
     */
    protected abstract Map.Entry<P, V> getEntryInternal(P key);

    /** Add or update the entry for the given key/value pair.
     * @param key entry key
     * @param value entry value
     * @return the value of the previous entry for {@code key} or {@code null}
     *      if no such entry exists
     */
    protected abstract V putInternal(P key, V value);

    /** Get the underlying map instance.
     * @return map instance
     */
    protected NavigableMap<P, V> getMap() {
        return map;
    }

    /** Return a {@link Map.Entry} instance containing the values from the
     * argument and suitable for direct use by external users. The returned
     * entry supports use of the {@link Map.Entry#setValue(Object)} method.
     * @param entry entry to export
     * @return entry instance suitable for direct user by callers
     */
    protected Entry<P, V> exportEntry(final Entry<P, V> entry) {
        return entry != null ?
                new MutableEntryWrapper(entry) :
                null;
    }

    /** Get the configured precision for the instance.
     * @return precision object
     */
    protected Precision.DoubleEquivalence getPrecision() {
        return precision;
    }

    /** Get an iterator for accessing map entries in order of nearest to farthest
     * from {@code pt}.
     * @param pt reference point
     * @return iterator for accessing map entries in order of nearest to farthest
     * f        from {@code pt}.
     */
    protected abstract Iterator<Entry<P, V>> nearToFarIterator(P pt);

    /** Get an iterator for accessing map entries in order of farthest to nearest
     * from {@code pt}.
     * @param pt reference point
     * @return iterator for accessing map entries in order of farthest to nearest
     * f        from {@code pt}.
     */
    protected abstract Iterator<Entry<P, V>> farToNearIterator(P pt);

    /** {@link Map.Entry} subclass that adds support for the {@link Map.Entry#setValue(Object)}.
     */
    private final class MutableEntryWrapper extends SimpleEntry<P, V> {

        /** Serializable UID. */
        private static final long serialVersionUID = 20220317L;

        /** Construct a new instance representing the same mapping as the argument.
         * @param entry target entry
         */
        MutableEntryWrapper(final Entry<? extends P, ? extends V> entry) {
            super(entry);
        }

        /** {@inheritDoc} */
        @Override
        public V setValue(final V value) {
            // replace the value in the map
            map.replace(getKey(), value);

            // set the local value
            return super.setValue(value);
        }
    }

    /** Abstract type representing a collection over the entries in this map.
     */
    private abstract class AbstractEntryCollection extends AbstractCollection<Entry<P, V>> {

        /** {@inheritDoc} */
        @Override
        public int size() {
            return AbstractPointMap1D.this.size();
        }
    }
}
