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
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
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

    /** Primary tree root. */
    private BucketNode<P, V> root;

    /** Secondary tree root. */
    private BucketNode<P, V> secondaryRoot;

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
        return getKey(findEntryByPoint(pt));
    }

    /** {@inheritDoc} */
    @Override
    public Map.Entry<P, V> resolveEntry(final P pt) {
        return exportEntry(findEntryByPoint(pt));
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return root.getEntryCount() +
                (secondaryRoot != null ? secondaryRoot.getEntryCount() : 0);
    }

    /** {@inheritDoc} */
    @Override
    public V put(final P key, final V value) {
        GeometryInternalUtils.validatePointMapKey(key);

        Entry<P, V> entry = findEntryByPoint(key);
        if (entry != null) {
            return entry.setValue(value);
        }

        root.insertEntry(new SimpleEntry<>(key, value));
        entryAdded();

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        return getValue(findEntry(key));
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        @SuppressWarnings("unchecked")
        final Entry<P, V> entry = removeEntryByPoint((P) key);
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
        return root.findEntryByValue(value) != null ||
                (secondaryRoot != null && secondaryRoot.findEntryByValue(value) != null);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<P, V>> entrySet() {
        return new EntrySet<>(this);
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        root = createNode(null);
        secondaryRoot = null;
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
        ++version;

        if (!root.isLeaf() && secondaryRoot == null) {
            secondaryRoot = root;
            root = createNode(null);
        }

        migrateSecondaryEntry();
        checkSecondaryRoot();
    }

    /** Method called when an entry is removed from the tree.
     */
    private void entryRemoved() {
        ++version;

        checkSecondaryRoot();
    }

    /** Create a list for storing map entries.
     * @return list for storing map entries
     */
    private List<Entry<P, V>> createEntryList() {
        return new ArrayList<>(maxNodeEntryCount);
    }

    /** Create a list for storing node children. The returned list contains
     * {@code nodeChildCount} number of {@code null} entries.
     * @return list for storing node children
     */
    private List<BucketNode<P, V>> createNodeChildList() {
        final List<BucketNode<P, V>> childList =  new ArrayList<>(nodeChildCount);
        for (int i = 0; i < nodeChildCount; ++i) {
            childList.add(null);
        }
        return childList;
    }

    /** Get the entry for the given key or null if not found.
     * @param key key to search for
     * @return entry for the given key or null if not found
     */
    @SuppressWarnings("unchecked")
    private Entry<P, V> findEntry(final Object key) {
        return findEntryByPoint((P) key);
    }

    /** Find the entry for the given point or null if one does not
     * exist.
     * @param pt point to find the entry for
     * @return entry for the given point or null if one does not exist
     */
    private Entry<P, V> findEntryByPoint(final P pt) {
        Entry<P, V> entry = null;
        if (pt.isFinite()) {
            entry = root.findEntry(pt);
            if (entry == null && secondaryRoot != null) {
                entry = secondaryRoot.findEntry(pt);
            }
        }
        return entry;
    }

    /** Remove and return the entry for the given point or null
     * if no such entry exists.
     * @param pt point to remove the entry for
     * @return the removed entry or null if not found
     */
    private Entry<P, V> removeEntryByPoint(final P pt) {
        Entry<P, V> entry = null;
        if (pt.isFinite()) {
            entry = root.removeEntry(pt);
            if (entry == null && secondaryRoot != null) {
                entry = secondaryRoot.removeEntry(pt);
            }
        }
        return entry;
    }

    /** Move an entry from the secondary root (if present) to the primary root. This process
     * reintroduces points from a previous insertion back into higher levels of the root tree,
     * thereby giving the root tree more balanced split points.
     */
    private void migrateSecondaryEntry() {
        if (secondaryRoot != null) {
            final int offset = version % nodeChildCount;
            final boolean even = (offset & 1) > 0;
            final int idx = even ?
                    offset / 2 :
                    nodeChildCount - 1 - (offset / 2);

            final Entry<P, V> entry = secondaryRoot.removeEntryAlongChildIndexPath(idx);
            if (entry != null) {
                root.insertEntry(entry);
            }
        }
    }

    /** Remove the secondary root if empty.
     */
    private void checkSecondaryRoot() {
        if (secondaryRoot != null && secondaryRoot.isEmpty()) {
            secondaryRoot.destroy();
            secondaryRoot = null;
        }
    }

    /** Return the key for the argument or {@code null} if {@code entry}
     * is {@code null}.
     * @param <K> Key type
     * @param entry entry to return the key for; may be null
     * @return key for the argument or null if the argument is null
     */
    private static <K> K getKey(final Entry<K, ?> entry) {
        return entry != null ?
                entry.getKey() :
                null;
    }

    /** Return the value for the argument or {@code null} if {@code entry}
     * is {@code null}.
     * @param <V> Value type
     * @param entry entry to return the value for; may be null
     * @return value for the argument or null if the argument is null
     */
    private static <V> V getValue(final Entry<?, V> entry) {
        return entry != null ?
                entry.getValue() :
                null;
    }

    /** Export an entry from the map, making it immutable.
     * @param <K> Key type
     * @param <V> Value type
     * @param entry entry to export; may be null
     * @return immutable entry containing the same key and value as
     *      the argument or null if the argument is null
     */
    private static <K, V> Entry<K, V> exportEntry(final Entry<K, V> entry) {
        return entry != null ?
                new SimpleImmutableEntry<>(entry) :
                null;
    }

    /** Spatial partitioning node type that stores map entries in a list until
     * a threshold is reached, at which point the node is split.
     * @param <P> Point type
     * @param <V> Value type
     */
    protected abstract static class BucketNode<P extends Point<P>, V>
            implements Iterable<Entry<P, V>> {

        /** Owning map. */
        private AbstractBucketPointMap<P, V> map;

        /** Parent node. */
        private BucketNode<P, V> parent;

        /** Child nodes. */
        private List<BucketNode<P, V>> children;

        /** Entries stored in the node; will be null for non-leaf nodes. */
        private List<Entry<P, V>> entries;

        /** Number of entries in this subtree. */
        private int entryCount;

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
            this.entries = map.createEntryList();
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
            return entryCount < 1;
        }

        /** Get the number of map entries in the subtree rooted at this node.
         * @return number of map entries in the subtree rooted at this node
         */
        public int getEntryCount() {
            return entryCount;
        }

        /** Find and return the map entry matching the given key.
         * @param key point key
         * @return entry matching the given key or null if not found
         */
        public Entry<P, V> findEntry(final P key) {
            if (isLeaf()) {
                // leaf node; check the list of entries for a match
                for (final Entry<P, V> entry : entries) {
                    if (map.pointsEq(key, entry.getKey())) {
                        return entry;
                    }
                }
            } else {
                // internal node; delegate to each child that could possibly contain
                // the point or an equivalent point
                final int loc = getSearchLocation(key);
                for (int i = 0; i < children.size(); ++i) {
                    if (testChildLocation(i, loc)) {
                        final Entry<P, V> entry = getEntryInChild(i, key);
                        if (entry != null) {
                            return entry;
                        }
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
                // leaf node; check the list of entries for a match
                for (final Entry<P, V> entry : entries) {
                    if (Objects.equals(entry.getValue(), value)) {
                        return entry;
                    }
                }
            } else {
                // internal node; delegate to each child
                for (final BucketNode<P, V> child : children) {
                    if (child != null) {
                        final Entry<P, V> childResult = child.findEntryByValue(value);
                        if (childResult != null) {
                            return childResult;
                        }
                    }
                }
            }

            // not found
            return null;
        }

        /** Insert a new entry into the subtree, returning the new size of the
         * subtree. No check is made as to whether or not the entry already exists.
         * @param entry entry to insert
         */
        public void insertEntry(final Map.Entry<P, V> entry) {
            if (isLeaf()) {
                if (entries.size() < map.maxNodeEntryCount) {
                    // we have an open spot here so just add the entry
                    append(entry);
                    return;
                }

                // no available entries; split the node and add the new
                // entry to a child
                splitNode();
            }

            // insert into the first matching child
            final int loc = getInsertLocation(entry.getKey());

            for (int i = 0; i < children.size(); ++i) {
                if (testChildLocation(i, loc)) {
                    getOrCreateChild(i).insertEntry(entry);

                    // update the subtree state
                    subtreeEntryAdded();
                    break;
                }
            }
        }

        /** Remove the given key, returning the previously mapped entry.
         * @param key key to remove
         * @return the value previously mapped to the key or null if no
         *       value was mapped
         */
        public Entry<P, V> removeEntry(final P key) {
            if (isLeaf()) {
                // leaf node; check the existing entries for a match
                final Iterator<Entry<P, V>> it = entries.iterator();
                while (it.hasNext()) {
                    final Entry<P, V> entry = it.next();
                    if (map.pointsEq(key, entry.getKey())) {
                        it.remove();

                        // update the subtree state
                        subtreeEntryRemoved();

                        return entry;
                    }
                }
            } else {
                // internal node; look through children
                final int loc = getSearchLocation(key);
                for (int i = 0; i < children.size(); ++i) {
                    if (testChildLocation(i, loc)) {
                        final Entry<P, V> entry = removeFromChild(i, key);
                        if (entry != null) {
                            // update the subtree state
                            subtreeEntryRemoved();

                            return entry;
                        }
                    }
                }
            }

            // not found
            return null;
        }

        /** Append an entry to the entry list for this node. This method must
         * only be called on leaf nodes.
         * @param entry entry to append
         */
        public void append(final Entry<P, V> entry) {
            entries.add(entry);
            subtreeEntryAdded();
        }

        /** Remove an entry in a leaf node lying on the given child index path.
         * @param childIdx target child index
         * @return removed entry
         */
        public Entry<P, V> removeEntryAlongChildIndexPath(final int childIdx) {
            if (isLeaf()) {
                if (!entries.isEmpty()) {
                    // remove the last entry in the list
                    final Entry<P, V> entry = entries.remove(entries.size() - 1);
                    subtreeEntryRemoved();

                    return entry;
                }
            } else {
                final int childCount = children.size();
                final int delta = childIdx < (map.nodeChildCount / 2) ?
                        +1 :
                        -1;

                for (int n = 0, i = childIdx;
                        n < childCount;
                        ++n, i += delta) {
                    final int adjustedIndex = (i + childCount) % childCount;

                    final BucketNode<P, V> child = children.get(adjustedIndex);
                    if (child != null) {
                        final Entry<P, V> entry = child.removeEntryAlongChildIndexPath(childIdx);
                        if (entry != null) {
                            // destroy and remove the child if empty
                            if (child.isEmpty()) {
                                child.destroy();
                                children.set(adjustedIndex, null);
                            }

                            subtreeEntryRemoved();

                            return entry;
                        }
                    }
                }
            }

            return null;
        }

        /** Destroy this node. The node must not be used after this method is called.
         */
        public void destroy() {
            this.map = null;
            this.parent = null;
            this.children = null;
            this.entries = null;
            this.entryCount = 0;
        }

        /** Return true if this node has been destroyed.
         * @return true if this node has been destroyed
         */
        public boolean isDestroyed() {
            return map == null;
        }

        /** Return an iterator for accessing the entries stored in this node. The {@code remove()}
         * method of the returned iterator correctly updates the tree state. This method must only
         * be called on leaf nodes.
         * @return iterator for accessing the entries stored in this node
         */
        @Override
        public Iterator<Map.Entry<P, V>> iterator() {
            return iterator(0);
        }

        /** Return an iterator for accessing the entries stored in this node, starting at the given
         * index. The {@code remove()} method of the returned iterator correctly updates the tree state.
         * This method must only be called on leaf nodes.
         * @param idx starting index for the iterator
         * @return iterator for accessing the entries stored in this node, starting with the entry at
         *      the given index
         */
        private Iterator<Map.Entry<P, V>> iterator(final int idx) {
            final List<Map.Entry<P, V>> iteratedList = idx == 0 ?
                    entries :
                    entries.subList(idx, entries.size());

            final Iterator<Map.Entry<P, V>> it = iteratedList.iterator();

            return new Iterator<Map.Entry<P, V>>() {

                @Override
                public boolean hasNext() {
                    return !isDestroyed() && it.hasNext();
                }

                @Override
                public Entry<P, V> next() {
                    return it.next();
                }

                @Override
                public void remove() {
                    it.remove();

                    // store the owning map since we may be destroyed as part of
                    // entry removal
                    final AbstractBucketPointMap<P, V> owningMap = map;

                    // navigate up the tree and perform updates
                    BucketNode<P, V> current = BucketNode.this;
                    while (current != null) {
                        current.subtreeEntryRemoved();

                        current = current.parent;
                    }

                    // notify the owning map
                    owningMap.entryRemoved();
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

        /** Make this node a leaf node, using the given list of entries.
         * @param leafEntries list of map entries to use for the node
         */
        protected void makeLeaf(final List<Entry<P, V>> leafEntries) {
            children = null;
            entries = leafEntries;
        }

        /** Split the node and place all entries into the new child nodes.
         * This node becomes an internal node.
         */
        protected void splitNode() {
            computeSplit();

            children = map.createNodeChildList();

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

        /** Method called when an entry is added to the subtree represented
         * by this node.
         */
        private void subtreeEntryAdded() {
            ++entryCount;
        }

        /** Method called when an entry is removed from the subtree represented
         * by this node. If the subtree is an empty internal node, it is converted
         * to a leaf node.
         */
        private void subtreeEntryRemoved() {
            --entryCount;

            final int condenseThreshold = map.maxNodeEntryCount / 2;

            if (!isLeaf() && entryCount <= condenseThreshold) {
                final List<Entry<P, V>> subtreeEntries = map.createEntryList();

                if (!isEmpty() &&
                        (parent == null || parent.getEntryCount() > condenseThreshold)) {
                    collectSubtreeEntriesRecursive(subtreeEntries, false);
                }

                makeLeaf(subtreeEntries);
            }
        }

        /** Add all map entries in the subtree rooted at this node to {@code entryList}. If {@code destroyNode}
         * is true, the node is destroyed after its entries are added to the list.
         * @param entryList list to add entries to
         * @param destroyNode if true, the node will be destroyed after its entries are added to the list
         */
        private void collectSubtreeEntriesRecursive(final List<Entry<P, V>> entryList, final boolean destroyNode) {
            if (isLeaf()) {
                entryList.addAll(entries);
            } else {
                for (final BucketNode<P, V> child : children) {
                    if (child != null) {
                        child.collectSubtreeEntriesRecursive(entryList, true);
                    }
                }
            }

            if (destroyNode) {
                destroy();
            }
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

        /** Construct a new instance for the given map.
         * @param map map instance
         */
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

        /** Size of the owning map. */
        private int size;

        /** Iterator that produces the next entry to be returned. */
        private Iterator<Map.Entry<P, V>> nextEntryIterator;

        /** Index of the entry that will be returned next from the iterator. */
        private int nextIdx;

        /** Expected map modification version. */
        private int expectedVersion;

        /** Construct a new instance for the given map.
         * @param map map instance
         */
        EntryIterator(final AbstractBucketPointMap<P, V> map) {
            this.map = map;
            this.size = map.size();

            updateExpectedVersion();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return nextIdx < size;
        }

        /** {@inheritDoc} */
        @Override
        public Entry<P, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            checkVersion();

            if (nextEntryIterator == null ||
                    !nextEntryIterator.hasNext()) {
                nextEntryIterator = findIterator();
            }

            final Map.Entry<P, V> result = nextEntryIterator.next();
            ++nextIdx;

            return result;
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            if (nextEntryIterator == null) {
                throw new IllegalStateException("Cannot remove: no entry has yet been returned");
            }

            nextEntryIterator.remove();
            --nextIdx;
            --size;

            updateExpectedVersion();
        }

        /** Find the next entry iterator in the map.
         * @return next map entry iterator
         */
        private Iterator<Entry<P, V>> findIterator() {
            int offset = 0;
            if (map.secondaryRoot != null) {
                final Iterator<Entry<P, V>> secondaryIt = findIteratorRecursive(map.secondaryRoot, offset);
                if (secondaryIt != null) {
                    return secondaryIt;
                }

                offset += map.secondaryRoot.getEntryCount();
            }

            return findIteratorRecursive(map.root, offset);
        }

        /** Find the next map entry iterator recursively in the subtree rooted at {@code node}.
         * @param node root of the subtree to obtain an iterator in
         * @param offset index offset of the first entry in the subtree
         * @return the next map entry iterator or null if no leaf nodes in the subtree contain the
         *      entry at {@code nextIdx}
         */
        private Iterator<Entry<P, V>> findIteratorRecursive(final BucketNode<P, V> node, final int offset) {
            if (nextIdx >= offset && nextIdx < offset + node.getEntryCount()) {
                if (node.isLeaf()) {
                    return node.iterator(nextIdx - offset);
                } else {
                    return findIteratorInNodeChildren(node, offset);
                }
            }

            return null;
        }

        /** Find the next map entry iterator in the children of {@code node}.
         * @param node root of the subtree to obtain an iterator in
         * @param offset index offset of the first entry in the subtree
         * @return the next map entry iterator or null if no leaf nodes in the subtree contain the
         *      entry at {@code nextIdx}
         */
        private Iterator<Entry<P, V>> findIteratorInNodeChildren(final BucketNode<P, V> node, final int offset) {
            final int childCount = node.children.size();

            int currentOffset = offset;
            for (int i = 0; i < childCount; ++i) {
                final BucketNode<P, V> child = node.children.get(i);
                if (child != null) {
                    Iterator<Entry<P, V>> childIt = findIteratorRecursive(child, currentOffset);
                    if (childIt != null) {
                        return childIt;
                    }

                    currentOffset += child.getEntryCount();
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
