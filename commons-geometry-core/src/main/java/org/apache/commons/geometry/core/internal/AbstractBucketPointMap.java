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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.numbers.core.Precision;

/** Abstract tree-based {@link PointMap} implementation that stores entries in bucket nodes
 * that are split once a certain entry count threshold is reached.
 * @param <P> Point type
 * @param <V> Map value type
 */
public abstract class AbstractBucketPointMap<P extends Point<P>, V>
    extends AbstractMap<P, V>
    implements PointMap<P, V> {

    /** Function used to construct new node instances. */
    private final BiFunction<AbstractBucketPointMap<P, V>, BucketNode<P, V>, BucketNode<P, V>> nodeFactory;

    /** Maximum number of entries stored per node before the node is split. */
    private final int maxNodeEntryCount;

    /** Number of child nodes for each non-leaf node. */
    private final int nodeChildCount;

    /** Precision context. */
    private final Precision.DoubleEquivalence precision;

    /** Root of the tree. */
    private BucketNode<P, V> root;

    /** Size of the tree. */
    private int entryCount;

    /** Version counter, used to track tree modifications. */
    private int version;

    /** Construct a new instance.
     * @param nodeFactory object used to construct new node instances
     * @param maxNodeEntryCount maximum number of map entries per node before
     *      the node is split
     * @param nodeChildCount number of child nodes per internal node
     * @param precision precision object used for floating point comparisons
     */
    protected AbstractBucketPointMap(
            final BiFunction<AbstractBucketPointMap<P, V>, BucketNode<P, V>, BucketNode<P, V>> nodeFactory,
            final int maxNodeEntryCount,
            final int nodeChildCount,
            final Precision.DoubleEquivalence precision) {
        this.nodeFactory = nodeFactory;
        this.maxNodeEntryCount = maxNodeEntryCount;
        this.nodeChildCount = nodeChildCount;
        this.precision = precision;
        this.root = nodeFactory.apply(this, null);
    }

    /** {@inheritDoc} */
    @Override
    public P resolveKey(final P pt) {
        final Entry<P, V> entry = root.findEntry(pt);
        return entry != null ?
                entry.getKey() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public Map.Entry<P, V> resolveEntry(final P pt) {
        final Map.Entry<P, V> entry = root.findEntry(pt);
        // return an immutable view if found
        return entry != null ?
                new AbstractMap.SimpleImmutableEntry<>(entry) :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return entryCount;
    }

    /** {@inheritDoc} */
    @Override
    public V put(final P key, final V value) {
        GeometryInternalUtils.validatePointMapKey(key);

        Entry<P, V> entry = root.findEntry(key);
        if (entry != null) {
            return entry.setValue(value);
        }

        root.insertEntry(key, value);
        entryAdded();

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        final Entry<P, V> entry = findEntry(key);
        return entry != null ?
                entry.getValue() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        @SuppressWarnings("unchecked")
        final Entry<P, V> entry = root.removeEntry((P) key);
        if (entry != null) {
            entryRemoved();
            return entry.getValue();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(final Object key) {
        return findEntry(key) != null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(final Object value) {
        return root.findEntryByValue(value) != null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<P, V>> entrySet() {
        return new EntrySet<>(this);
    }

    /** Return true if the given points are equivalent using the precision
     * configured for the map.
     * @param a first point
     * @param b second point
     * @return true if the given points are equivalent
     */
    protected abstract boolean pointsEq(P a, P b);

    /** Get the configured precision for the instance.
     * @return precision object
     */
    protected Precision.DoubleEquivalence getPrecision() {
        return precision;
    }

    /** Construct a new node instance.
     * @param parent parent node; will be null or the tree root
     * @return the new node instance
     */
    private BucketNode<P, V> createNode(final BucketNode<P, V> parent) {
        return nodeFactory.apply(this, parent);
    }

    /** Method called when a new entry is added to the tree.
     */
    private void entryAdded() {
        ++entryCount;
        ++version;
    }

    /** Method called when an entry is removed from the tree.
     */
    private void entryRemoved() {
        --entryCount;
        ++version;
    }

    /** Get a list for storing map entries.
     * @return list for storing map entries
     */
    private List<Entry<P, V>> getEntryList() {
        return new ArrayList<>(maxNodeEntryCount);
    }

    /** Get a list for storing node children.
     * @return list for storing node children
     */
    private List<BucketNode<P, V>> getNodeChildList() {
        return new ArrayList<>(Collections.nCopies(nodeChildCount, null));
    }

    /** Get the entry for the given key or null if not found.
     * @param key key to search for
     * @return entry for the given key or null if not found
     */
    @SuppressWarnings("unchecked")
    private Entry<P, V> findEntry(final Object key) {
        final P pt = (P) key;
        return pt.isFinite() ?
                root.findEntry(pt) :
                null;
    }

    /** Spatial partitioning node type that stores map entries in a list until
     * a threshold is reached, at which point the node is split.
     * @param <P> Point type
     * @param <V> Value type
     */
    protected abstract static class BucketNode<P extends Point<P>, V>
        implements Iterable<Map.Entry<P, V>> {

        /** Owning map. */
        private final AbstractBucketPointMap<P, V> map;

        /** Parent node. */
        private final BucketNode<P, V> parent;

        /** Child nodes. */
        private List<BucketNode<P, V>> children;

        /** Entries stored in the node; will be null for non-leaf nodes. */
        private List<Entry<P, V>> entries;

        /** Construct a new instance.
         * @param map owning map
         * @param parent parent node or null if the tree root
         */
        protected BucketNode(
                final AbstractBucketPointMap<P, V> map,
                final BucketNode<P, V> parent) {
            this.map = map;
            this.parent = parent;

            // pull an entry list from the parent map; this will make
            // this node a leaf initially
            this.entries = map.getEntryList();
        }

        /**
         * Return true if this node is a leaf node.
         * @return true if this node is a leaf node
         */
        public boolean isLeaf() {
            return entries != null;
        }

        /**
         * Return true if the subtree rooted at this node does not
         * contain any map entries.
         * @return true if the subtree root at this node is empty
         */
        public boolean isEmpty() {
            return isLeaf() && entries.isEmpty();
        }

        /** Insert a new entry into the subtree. No check is made as to whether or not the
         * entry already exists.
         * @param key key to insert
         * @param value value to insert
         */
        public void insertEntry(final P key, final V value) {
            if (isLeaf()) {
                if (entries.size() < map.maxNodeEntryCount) {
                    // we have an open spot here so just add the entry
                    append(new SimpleEntry<>(key, value));
                    return;
                }

                // no available entries; split the node and add the new
                // entry to a child
                splitNode();
            }

            // insert into the first matching child
            final int loc = getInsertLocation(key);

            for (int i = 0; i < children.size(); ++i) {
                if (testChildLocation(i, loc)) {
                    getOrCreateChild(i).insertEntry(key, value);
                }
            }
        }

        /**
         * Append an entry to the entry list for this node. This method must
         * only be called on leaf nodes.
         * @param entry entry to append
         */
        public void append(final Entry<P, V> entry) {
            entries.add(entry);
        }

        /** Find and return the map entry matching the given key.
         * @param key point key
         * @return entry matching the given key or null if not found
         */
        public Entry<P, V> findEntry(final P key) {
            if (isLeaf()) {
                // check the list of entries for a match
                for (final Entry<P, V> entry : entries) {
                    if (map.pointsEq(key, entry.getKey())) {
                        return entry;
                    }
                }
                // not found
                return null;
            }

            // delegate to each child that could possibly contain the
            // point or an equivalent point
            final int loc = getSearchLocation(key);
            for (int i = 0; i < children.size(); ++i) {
                if (testChildLocation(i, loc)) {
                    final Entry<P, V> entry = getEntryInChild(i, key);
                    if (entry != null) {
                        return entry;
                    }
                }
            }

            // not found
            return null;
        }

        /** Find the first entry in the tree with the given value or null if not found.
         * @param value value to search for
         * @return the first entry in the tree with the given value or null if not found
         */
        public Entry<P, V> findEntryByValue(final Object value) {
            if (isLeaf()) {
                // check the list of entries for a match
                for (final Entry<P, V> entry : entries) {
                    if (Objects.equals(entry.getValue(), value)) {
                        return entry;
                    }
                }
                // not found
                return null;
            }

            // delegate to each child
            for (final BucketNode<P, V> child : children) {
                if (child != null) {
                    final Entry<P, V> childResult = child.findEntryByValue(value);
                    if (childResult != null) {
                        return childResult;
                    }
                }
            }

            // not found
            return null;
        }

        /** Remove the given key, returning the previously mapped entry.
         * @param key key to remove
         * @return the value previously mapped to the key or null if no
         *       value was mapped
         */
        public Entry<P, V> removeEntry(final P key) {
            if (isLeaf()) {
                // check the existing entries for a match
                final Iterator<Entry<P, V>> it = entries.iterator();
                while (it.hasNext()) {
                    final Entry<P, V> entry = it.next();
                    if (map.pointsEq(key, entry.getKey())) {
                        it.remove();
                        return entry;
                    }
                }

                // not found
                return null;
            }

            // look through children
            final int loc = getSearchLocation(key);
            for (int i = 0; i < children.size(); ++i) {
                if (testChildLocation(i, loc)) {
                    final Entry<P, V> entry = removeFromChild(i, key);
                    if (entry != null) {

                        // an entry was removed; check if the subtree can be
                        // condensed before returning
                        condenseSubtree();

                        return entry;
                    }
                }
            }

            // not found
            return null;
        }

        /** Attempt to condense the subtree rooted at this node by converting
         * it to a leaf if no children contain entries.
         */
        public void condenseSubtree() {
            boolean empty = true;
            for (final BucketNode<P, V> child : children) {
                if (child != null && !child.isEmpty()) {
                    empty = false;
                    break;
                }
            }

            if (empty) {
                makeLeaf();
            }
        }

        /** Return an iterator for accessing the node entries. The {@code remove()}
         * method of the returned iterator correctly updates the tree state.
         * @return iterator for accessing the node entries
         */
        @Override
        public Iterator<Map.Entry<P, V>> iterator() {
            final Iterator<Map.Entry<P, V>> it = entries.iterator();

            return new Iterator<Map.Entry<P, V>>() {

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Entry<P, V> next() {
                    return it.next();
                }

                @Override
                public void remove() {
                    it.remove();

                    // notify the owning map
                    map.entryRemoved();

                    // navigate up the tree and see if any subtrees need
                    // to be condensed
                    if (entries.isEmpty()) {
                        BucketNode<P, V> current = parent;
                        while (current != null) {
                            current.condenseSubtree();

                            current = current.parent;
                        }
                    }
                }
            };
        }

        /** Compute the split for this node from the current set
         * of map entries. Subclasses are responsible for managing
         * the storage of the split.
         */
        protected abstract void computeSplit();

        /** Get an int encoding the search locations of {@code pt} relative to the
         * node split. The return value must include all possible locations of
         * {@code pt} and equivalent points.
         * @param pt point to determine the relative location of
         * @return encoded search location
         */
        protected abstract int getSearchLocation(P pt);

        /** Get an int encoding the insert location of {@code pt} relative to the
         * node split. The return value must be strict and only include the single
         * location where {@code pt} should be inserted.
         * @param pt point to determine the insert location of
         * @return encoded insert location
         */
        protected abstract int getInsertLocation(P pt);

        /** Return true if the child node at {@code childIdx} matches the given
         * encoded point location.
         * @param childIdx child index to test
         * @param loc encoded relative point location
         * @return true if the child node a {@code childIdx} matches the location
         */
        protected abstract boolean testChildLocation(int childIdx, int loc);

        /** Make this node a leaf node.
         */
        protected void makeLeaf() {
            children = null;
            entries = map.getEntryList();
        }

        /** Split the node and place all entries into the new child nodes.
         * This node becomes an internal node.
         */
        protected void splitNode() {
            computeSplit();

            children = map.getNodeChildList();

            for (final Entry<P, V> entry : entries) {
                moveToChild(entry);
            }

            entries = null;
        }

        /** Get the precision context for the instance.
         * @return precision context for the instance
         */
        protected Precision.DoubleEquivalence getPrecision() {
            return map.precision;
        }

        /** Get the given entry in the child at {@code idx} or null if not found.
         * @param idx child index
         * @param key key to search for
         * @return entry matching {@code key} in child or null if not found
         */
        private Entry<P, V> getEntryInChild(final int idx, final P key) {
            final BucketNode<P, V> child = children.get(idx);
            if (child != null) {
                return child.findEntry(key);
            }
            return null;
        }

        /** Remove the given key from the child at {@code idx}.
         * @param idx index of the child
         * @param key key to remove
         * @return entry removed from the child or null if not found
         */
        private Entry<P, V> removeFromChild(final int idx, final P key) {
            final BucketNode<P, V> child = children.get(idx);
            if (child != null) {
                return child.removeEntry(key);
            }
            return null;
        }

        /** Move the previously created entry to a child node.
         * @param entry entry to mode
         */
        private void moveToChild(final Entry<P, V> entry) {
            final int loc = getInsertLocation(entry.getKey());

            final int numChildren = children.size();
            for (int i = 0; i < numChildren; ++i) {
                // place the entry in the first child that contains it
                if (testChildLocation(i, loc)) {
                    getOrCreateChild(i).append(entry);
                    break;
                }
            }
        }

        /** Get the child node at the given index, creating it if needed.
         * @param idx index of the child node
         * @return child node at the given index
         */
        private BucketNode<P, V> getOrCreateChild(final int idx) {
            BucketNode<P, V> child = children.get(idx);
            if (child == null) {
                child = map.createNode(this);
                children.set(idx, child);
            }
            return child;
        }

        /** Get an encoded search location value for the given comparison result. If
         * {@code cmp} is {@code 0}, then the bitwise OR of {@code neg} and {@code pos}
         * is returned, indicating that both spaces are valid search locations. Otherwise,
         * {@code neg} is returned for negative {@code cmp} values and {@code pos} for
         * positive ones. This location value is to be used during entry searches,
         * when comparisons must be loose and all possible locations included.
         * @param cmp comparison result
         * @param neg negative flag
         * @param pos positive flag
         * @return encoded search location value
         */
        public static int getSearchLocationValue(final int cmp, final int neg, final int pos) {
            if (cmp < 0) {
                return neg;
            } else if (cmp > 0) {
                return pos;
            }
            return neg | pos;
        }

        /** Get an insert location value for the given comparison result. If {@code cmp}
         * is less than or equal to {@code 0}, then {@code neg} is returned. Otherwise,
         * {@code pos} is returned. This location value is to be used during entry inserts,
         * where comparisons must be strict.
         * @param cmp comparison result
         * @param neg negative flag
         * @param pos positive flag
         * @return encoded insert location value
         */
        public static int getInsertLocationValue(final int cmp, final int neg, final int pos) {
            return cmp <= 0 ?
                    neg :
                    pos;
        }
    }

    /** Set view of the map entries.
     * @param <P> Point type
     * @param <V> Value type
     */
    private static final class EntrySet<P extends Point<P>, V>
        extends AbstractSet<Map.Entry<P, V>> {

        /** Owning map. */
        private final AbstractBucketPointMap<P, V> map;

        EntrySet(final AbstractBucketPointMap<P, V> map) {
            this.map = map;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(final Object obj) {
            if (obj instanceof Map.Entry) {
                final Map.Entry<?, ?> search = (Map.Entry<?, ?>) obj;
                final Object key = search.getKey();

                final Map.Entry<P, V> actual = map.findEntry(key);
                if (actual != null) {
                    return map.pointsEq(actual.getKey(), (P) search.getKey()) &&
                            Objects.equals(actual.getValue(), search.getValue());
                }
            }
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<Entry<P, V>> iterator() {
            return new EntryIterator<>(map);
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return map.size();
        }
    }

    /** Iterator for iterating through each entry in the map.
     * @param <P> Point type
     * @param <V> Value type
     */
    private static final class EntryIterator<P extends Point<P>, V>
        implements Iterator<Map.Entry<P, V>> {

        /** Owning map. */
        private final AbstractBucketPointMap<P, V> map;

        /** Queue of nodes waiting to be visited. */
        private final Deque<BucketNode<P, V>> nodeQueue = new ArrayDeque<>();

        /** Iterator that returned the previous entry. */
        private Iterator<Map.Entry<P, V>> prevEntryIterator;

        /** Iterator that produces the next entry to be returned. */
        private Iterator<Map.Entry<P, V>> nextEntryIterator;

        /** Expected map modification version. */
        private int expectedVersion;

        /** Construct a new instance for the given map.
         * @param map map instance
         */
        EntryIterator(final AbstractBucketPointMap<P, V> map) {
            this.map = map;
            this.nodeQueue.add(map.root);

            updateExpectedVersion();
            queueNextEntry();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return nextEntryIterator != null;
        }

        /** {@inheritDoc} */
        @Override
        public Entry<P, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            checkVersion();

            final Map.Entry<P, V> result = nextEntryIterator.next();

            prevEntryIterator = nextEntryIterator;

            queueNextEntry();

            return result;
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            if (prevEntryIterator == null) {
                throw new IllegalStateException("Cannot remove: no entry has yet been returned");
            }

            prevEntryIterator.remove();
            updateExpectedVersion();
        }

        /** Prepare the next entry to be returned by the iterator.
         */
        private void queueNextEntry() {
            if (nextEntryIterator == null || !nextEntryIterator.hasNext()) {
                final BucketNode<P, V> node = findNonEmptyLeafNode();
                if (node != null) {
                    nextEntryIterator = node.iterator();
                } else {
                    nextEntryIterator = null;
                }
            }
        }

        /** Find and return the next non-empty leaf node in the map.
         * @return the next non-empty leaf node or null if one cannot
         *      be found
         */
        private BucketNode<P, V> findNonEmptyLeafNode() {
            while (!nodeQueue.isEmpty()) {
                final BucketNode<P, V> node = nodeQueue.removeFirst();

                if (!node.isLeaf()) {
                    // internal node; add non-null children to the queue
                    for (final BucketNode<P, V> child : node.children) {
                        if (child != null) {
                            nodeQueue.add(child);
                        }
                    }
                } else if (!node.isEmpty()) {
                    // return the non-empty iterator
                    return node;
                }
            }

            return null;
        }

        /** Throw a {@link ConcurrentModificationException} if the map version does
         * not match the expected version.
         */
        private void checkVersion() {
            if (map.version != expectedVersion) {
                throw new ConcurrentModificationException();
            }
        }

        /** Update the expected modification version of the map. This must be called
         * whenever the map is changed through this instance.
         */
        private void updateExpectedVersion() {
            expectedVersion = map.version;
        }
    }
}
