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
package org.apache.commons.geometry.euclidean.internal;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.euclidean.EuclideanVector;
import org.apache.commons.numbers.core.Precision;

public abstract class AbstractMultiDimensionalPointMap<P extends EuclideanVector<P>, V>
    extends AbstractMap<P, V>
    implements PointMap<P, V> {

    /** Max entries per node. */
    private static final int MAX_ENTRIES_PER_NODE = 16;

    /** Precision context. */
    final Precision.DoubleEquivalence precision;

    /** Function used to construct new node instances. */
    final Function<AbstractMultiDimensionalPointMap<P, V>, Node<P, V>> nodeFactory;

    /** Root of the tree. */
    private Node<P, V> root;

    /** Size of the tree. */
    private int entryCount;

    protected AbstractMultiDimensionalPointMap(
            final Function<AbstractMultiDimensionalPointMap<P, V>, Node<P, V>> nodeFactory,
            final Precision.DoubleEquivalence precision) {
        this.precision = precision;
        this.nodeFactory = nodeFactory;
        this.root = nodeFactory.apply(this);
    }

    /** {@inheritDoc} */
    @Override
    public P resolveKey(final P pt) {
        final Entry<P, V> entry = root.getEntry(pt);
        return entry != null ?
                entry.getKey() :
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
        Objects.requireNonNull(key);
        if (!key.isFinite()) {
            throw new IllegalArgumentException("Keys must be finite");
        }

        final Entry<P, V> entry = root.getEntry(key);
        if (entry == null) {
            root.insertEntry(key, value);
            entryAdded();

            return null;
        }

        final V prev = entry.getValue();
        entry.setValue(value);

        return prev;
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        @SuppressWarnings("unchecked")
        final Entry<P, V> entry = root.getEntry((P) key);
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
    public Set<Entry<P, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    /** Construct a new node instance.
     * @return the new node instance
     */
    Node<P, V> createNode() {
        return nodeFactory.apply(this);
    }

    /** Method called when a new entry is added to the tree.
     */
    void entryAdded() {
        ++entryCount;
    }

    /** Method called when an entry is removed from the tree.
     */
    void entryRemoved() {
        --entryCount;
    }

    public static abstract class Node<P extends EuclideanVector<P>, V> {

        /** Owning map. */
        private final AbstractMultiDimensionalPointMap<P, V> map;

        /** Child nodes. */
        private List<Node<P, V>> children;

        /** Points stored in the node; this will only be populated for leaf nodes. */
        private List<Entry<P, V>> entries = new ArrayList<>(MAX_ENTRIES_PER_NODE);

        /** The split point of the node; will be null for leaf nodes. */
        private P splitPoint;

        protected Node(final AbstractMultiDimensionalPointMap<P, V> map) {
            this.map = map;
        }

        /** Return true if the node is a leaf.
         * @return true if the node is a leaf
         */
        public boolean isLeaf() {
            return splitPoint == null;
        }

        /** Return true if this node is a leaf node and contains no entries.
         * @return true if this node is a leaf node and contains no entries
         */
        public boolean isEmpty() {
            return isLeaf() && entries.isEmpty();
        }

        /** Insert a new entry containing the given key and value. No check
         * is made as to whether or not an entry already exists for the key.
         * @param key key to insert
         * @param value value to insert
         */
        public void insertEntry(final P key, final V value) {
            if (isLeaf()) {
                if (entries.size() < MAX_ENTRIES_PER_NODE) {
                    // we have an open spot here so just add the entry
                    entries.add(new SimpleEntry<>(key, value));
                    return;
                }

                // no available entries; split the node and add to a child
                splitNode();
            }

            // non-leaf node
            // determine the relative location of the key
            final int loc = getLocation(key, splitPoint);

            // insert into the first child that can contain the key
            for (int i = 0; i < children.size(); ++i) {
                if (testChildLocation(i, loc)) {
                    getOrCreateChild(i).insertEntry(key, value);
                    break;
                }
            }
        }

        /** Get the entry matching the given key or null if not found.
         * @param key key to search for
         * @return the entry matching the given key or null if not found
         */
        public Entry<P, V> getEntry(final P key) {
            if (isLeaf()) {
                // check the list of entries for a match
                for (final Entry<P, V> entry : entries) {
                    if (key.eq(entry.getKey(), map.precision)) {
                        return entry;
                    }
                }
                // not found
                return null;
            }

            // delegate to each child that could possibly contain the
            // point or an equivalent point
            final int loc = getLocation(key, splitPoint);
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
                    if (key.eq(entry.getKey(), map.precision)) {
                        it.remove();
                        return entry;
                    }
                }

                // not found
                return null;
            }

            // look through children
            final int loc = getLocation(key, splitPoint);
            for (int i = 0; i < children.size(); ++i) {
                if (testChildLocation(i, loc)) {
                    final Entry<P, V> entry = removeFromChild(i, key);
                    if (entry != null) {

                        checkMakeLeaf();

                        return entry;
                    }
                }
            }

            // not found
            return null;
        }

        /** Get the precision context for the instance.
         * @return precision context for the instance
         */
        protected Precision.DoubleEquivalence getPrecision() {
            return map.precision;
        }

        /** Get the number of children required for each node. This will vary by dimension.
         * @return number of required children for each node
         */
        protected abstract int getNodeChildCount();

        /** Compute the node split point based on the given entry list.
         * @param entries entries contained in the node being split
         * @return the computed split point
         */
        protected abstract P computeSplitPoint(final List<Entry<P, V>> entries);

        /** Get an int encoding the location of {@code pt} relative to the
         * node split point.
         * @param pt point to determine the relative location of
         * @param split node split point
         * @return encoded point location
         */
        protected abstract int getLocation(final P pt, final P split);

        /** Return true if the child node at {@code childIdx} matches the given
         * encoded point location.
         * @param childIdx child index to test
         * @param loc encoded relative point location
         * @return true if the child node a {@code childIdx} matches the location
         */
        protected abstract boolean testChildLocation(final int childIdx, final int loc);

        /** Get the given entry in the child at {@code idx} or null if not found.
         * @param idx child index
         * @param key key to search for
         * @return entry matching {@code key} in child or null if not found
         */
        private Entry<P, V> getEntryInChild(final int idx, final P key) {
            final Node<P, V> child = children.get(idx);
            if (child != null) {
                return child.getEntry(key);
            }
            return null;
        }

        /** Remove the given key from the child at {@code idx}.
         * @param idx index of the child
         * @param key key to remove
         * @return entry removed from the child or null if not found
         */
        private Entry<P, V> removeFromChild(final int idx, final P key) {
            final Node<P, V> child = children.get(idx);
            if (child != null) {
                final Entry<P, V> entry = child.removeEntry(key);
                if (entry != null) {

                }

                return entry;
            }
            return null;
        }

        /** Split the node and place all entries into the new child nodes.
         * This node becomes an internal node.
         */
        private void splitNode() {
            splitPoint = computeSplitPoint(entries);

            final int childCount = getNodeChildCount();

            children = new ArrayList<>(childCount);
            // add null placeholders entries for children; these will be replaced
            // with actual nodes as needed
            for (int i = 0; i < childCount; ++i) {
                children.add(null);
            }

            for (final Entry<P, V> entry : entries) {
                moveToChild(entry);
            }

            entries.clear();
        }

        /** Attempt to condense the subtree rooted at this internal node by converting
         * it to a leaf if no children contain entries.
         */
        private void checkMakeLeaf() {
            boolean empty = true;
            for (final Node<P, V> child : children) {
                if (child != null && !child.isEmpty()) {
                    empty = false;
                    break;
                }
            }

            if (empty) {
                makeLeaf();
            }
        }

        /** Make this node a leaf node.
         */
        private void makeLeaf() {
            splitPoint = null;
            children = null;
        }

        /** Move the previously created entry to a child node.
         * @param entry entry to mode
         */
        private void moveToChild(final Entry<P, V> entry) {
            final int loc = getLocation(entry.getKey(), splitPoint);

            final int numChildren = children.size();
            for (int i = 0; i < numChildren; ++i) {
                // place the entry in the first child that contains it
                if (testChildLocation(i, loc)) {
                    getOrCreateChild(i).entries.add(entry);
                    break;
                }
            }
        }

        /** Get the child node at the given index, creating it if needed.
         * @param idx index of the child node
         * @return child node at the given index
         */
        private Node<P, V> getOrCreateChild(final int idx) {
            Node<P, V> child = children.get(idx);
            if (child == null) {
                child = map.nodeFactory.apply(map);
                children.set(idx, child);
            }
            return child;
        }

        /** Get the encoded location value for the given comparison value.
         * @param cmp comparison result
         * @param neg negative flag
         * @param pos positive flag
         * @return encoded location value
         */
        public static int getLocationValue(final int cmp, final int neg, final int pos) {
            if (cmp < 0) {
                return neg;
            } else if (cmp > 0) {
                return pos;
            }
            return neg | pos;
        }
    }

    private static final class EntrySet<P extends EuclideanVector<P>, V>
        extends AbstractSet<Map.Entry<P, V>> {

        /** Owning map. */
        private final AbstractMultiDimensionalPointMap<P, V> map;

        EntrySet(final AbstractMultiDimensionalPointMap<P, V> map) {
            this.map = map;
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<Entry<P, V>> iterator() {
            // TODO Auto-generated method stub
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return map.size();
        }
    }
}
