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
package org.apache.commons.geometry.examples.jmh.euclidean.pointmap;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;

/** Candidate point map that stores entries in a modified octree data structure.
 * The split point for the octree child nodes is variable and is placed at the
 * centroid of the points stored in the node at the time it is split.
* @param <V> map value type
*/
public class VariableSplitOctree<V> extends AbstractMap<Vector3D, V> {

    /** Precision context. */
    private final Precision.DoubleEquivalence precision;

    /** Root of the tree. */
    private VariableSplitOctreeNode<V> root;

    /** Size of the tree. */
    private int entryCount;

    /**
     * Create an instance.
     *
     * @param precision the precision
     */
    public VariableSplitOctree(final Precision.DoubleEquivalence precision) {
        this.precision = precision;
        this.root = new VariableSplitOctreeNode<>(this);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return entryCount;
    }

    /** {@inheritDoc} */
    @Override
    public V put(final Vector3D key, final V value) {
        Objects.requireNonNull(key);
        if (!key.isFinite()) {
            throw new IllegalArgumentException("Keys must be finite");
        }

        final Vector3DEntry<V> entry = root.getEntry(key);
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
        final Vector3DEntry<V> entry = root.getEntry((Vector3D) key);
        return entry != null ?
                entry.getValue() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        final Vector3DEntry<V> entry = root.removeEntry((Vector3D) key);
        if (entry != null) {
            entryRemoved();
            return entry.getValue();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Vector3D, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    /** Method called when a new entry is added to the tree.
     */
    private void entryAdded() {
        ++entryCount;
    }

    /** Method called when an entry is removed from the tree.
     */
    private void entryRemoved() {
        --entryCount;
    }

    /** Octree node class.
     * @param <V> Value type
     */
    private static final class VariableSplitOctreeNode<V> {

        /** X negative octant flag. */
        private static final int XNEG = 1 << 5;

        /** X positive octant flag. */
        private static final int XPOS = 1 << 4;

        /** Y negative octant flag. */
        private static final int YNEG = 1 << 3;

        /** Y positive octant flag. */
        private static final int YPOS = 1 << 2;

        /** Z negative octant flag. */
        private static final int ZNEG = 1 << 1;

        /** Z positive octant flag. */
        private static final int ZPOS = 1;

        /** Octant location flags for child nodes. */
        private static final int[] CHILD_LOCATIONS = {
            XNEG | YNEG | ZNEG,
            XNEG | YNEG | ZPOS,
            XNEG | YPOS | ZNEG,
            XNEG | YPOS | ZPOS,

            XPOS | YNEG | ZNEG,
            XPOS | YNEG | ZPOS,
            XPOS | YPOS | ZNEG,
            XPOS | YPOS | ZPOS
        };

        /** Max entries per node. */
        private static final int MAX_ENTRIES = 16;

        /** Number of children for internal nodes. */
        private static final int NUM_CHILDREN = 8;

        /** Owning map. */
        private final VariableSplitOctree<V> map;

        /** Child nodes. */
        private List<VariableSplitOctreeNode<V>> children;

        /** Points stored in the node; this will only be populated for leaf nodes. */
        private List<Vector3DEntry<V>> entries = new ArrayList<>(MAX_ENTRIES);

        /** The split point of the node; will be {@code null} for leaf nodes. */
        private Vector3D splitPoint;

        VariableSplitOctreeNode(final VariableSplitOctree<V> map) {
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
        public void insertEntry(final Vector3D key, final V value) {
            if (isLeaf()) {
                if (entries.size() < MAX_ENTRIES) {
                    // we have an open spot here so just add the entry
                    entries.add(new Vector3DEntry<>(key, value));
                    return;
                }

                // no available entries; split the node and add to a child
                splitNode();
            }

            // non-leaf node
            // determine the relative location of the key
            final int loc = getLocation(key);

            // insert into the first child that can contain the key
            for (int i = 0; i < NUM_CHILDREN; ++i) {
                if (testChildLocation(i, loc)) {
                    getOrCreateChild(i).insertEntry(key, value);
                    break;
                }
            }
        }

        /** Get the entry matching the given key or {@code null} if not found.
         * @param key key to search for
         * @return the entry matching the given key or {@code null} if not found
         */
        public Vector3DEntry<V> getEntry(final Vector3D key) {
            if (isLeaf()) {
                // check the list of entries for a match
                for (final Vector3DEntry<V> entry : entries) {
                    if (key.eq(entry.getKey(), map.precision)) {
                        return entry;
                    }
                }
                // not found
                return null;
            }

            // delegate to each child that could possibly contain the
            // point or an equivalent point
            final int loc = getLocation(key);
            for (int i = 0; i < NUM_CHILDREN; ++i) {
                if (testChildLocation(i, loc)) {
                    final Vector3DEntry<V> entry = getEntryInChild(i, key);
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
         * @return the value previously mapped to the key or {@code null} if no
         *       value was mapped
         */
        public Vector3DEntry<V> removeEntry(final Vector3D key) {
            if (isLeaf()) {
                // check the existing entries for a match
                final Iterator<Vector3DEntry<V>> it = entries.iterator();
                while (it.hasNext()) {
                    final Vector3DEntry<V> entry = it.next();
                    if (key.eq(entry.getKey(), map.precision)) {
                        it.remove();
                        return entry;
                    }
                }

                // not found
                return null;
            }

            // look through children
            final int loc = getLocation(key);
            for (int i = 0; i < NUM_CHILDREN; ++i) {
                if (testChildLocation(i, loc)) {
                    final Vector3DEntry<V> entry = removeFromChild(i, key);
                    if (entry != null) {

                        checkMakeLeaf();

                        return entry;
                    }
                }
            }

            // not found
            return null;
        }

        /** Get the given entry in the child at {@code idx} or {@code null} if not found.
         * @param idx child index
         * @param key key to search for
         * @return entry matching {@code key} in child or {@code null} if not found
         */
        private Vector3DEntry<V> getEntryInChild(final int idx, final Vector3D key) {
            final VariableSplitOctreeNode<V> child = children.get(idx);
            if (child != null) {
                return child.getEntry(key);
            }
            return null;
        }

        /** Remove the given key from the child at {@code idx}.
         * @param idx index of the child
         * @param key key to remove
         * @return entry removed from the child or {@code null} if not found
         */
        private Vector3DEntry<V> removeFromChild(final int idx, final Vector3D key) {
            final VariableSplitOctreeNode<V> child = children.get(idx);
            if (child != null) {
                return child.removeEntry(key);
            }
            return null;
        }

        /** Split the node and place all entries into the new child nodes.
         * This node becomes an internal node.
         */
        private void splitNode() {
            splitPoint = computeCentroid();

            children = new ArrayList<>(NUM_CHILDREN);
            // add null placeholders entries for children these will be replaced
            // with actual nodes
            for (int i = 0; i < NUM_CHILDREN; ++i) {
                children.add(null);
            }

            for (final Vector3DEntry<V> entry : entries) {
                moveToChild(entry);
            }

            entries.clear();
        }

        /** Attempt to condense the subtree rooted at this internal node by converting
         * it to a leaf if no children contain entries.
         */
        private void checkMakeLeaf() {
         // go through all children and remove empty ones
            boolean empty = true;
            for (int i = 0; i < NUM_CHILDREN; ++i) {
                final VariableSplitOctreeNode<V> child = children.get(i);
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
        private void moveToChild(final Vector3DEntry<V> entry) {
            final int loc = getLocation(entry.getKey());

            for (int i = 0; i < NUM_CHILDREN; ++i) {
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
        private VariableSplitOctreeNode<V> getOrCreateChild(final int idx) {
            VariableSplitOctreeNode<V> child = children.get(idx);
            if (child == null) {
                child = new VariableSplitOctreeNode<>(map);
                children.set(idx, child);
            }
            return child;
        }

        /** Get an int encoding the location of {@code pt} relative to the
         * node split point.
         * @param pt point to determine the relative location of
         * @return encoded point location
         */
        private int getLocation(final Vector3D pt) {
            int loc = getLocationValue(
                    map.precision.compare(pt.getX(), splitPoint.getX()),
                    XNEG,
                    XPOS);
            loc |= getLocationValue(
                    map.precision.compare(pt.getY(), splitPoint.getY()),
                    YNEG,
                    YPOS);
            loc |= getLocationValue(
                    map.precision.compare(pt.getZ(), splitPoint.getZ()),
                    ZNEG,
                    ZPOS);

            return loc;
        }

        /** Get the encoded location value for the given comparison value.
         * @param cmp comparison result
         * @param neg negative flag
         * @param pos positive flag
         * @return encoded location value
         */
        private int getLocationValue(final int cmp, final int neg, final int pos) {
            if (cmp < 0) {
                return neg;
            } else if (cmp > 0) {
                return pos;
            }
            return neg | pos;
        }

        /** Return true if the child node at {@code childIdx} matches the given
         * encoded point location.
         * @param childIdx child index to test
         * @param loc encoded relative point location
         * @return true if the child node a {@code childIdx} matches the location
         */
        private boolean testChildLocation(final int childIdx, final int loc) {
            final int childLoc = CHILD_LOCATIONS[childIdx];
            return (childLoc & loc) == childLoc;
        }

        /** Compute the centroid of all points currently in the node.
         * @return centroid of the node points
         */
        private Vector3D computeCentroid() {
            Vector3D.Sum sum = Vector3D.Sum.create();
            for (Vector3DEntry<V> entry : entries) {
                sum.add(entry.getKey());
            }

            return sum.get().multiply(1.0 / entries.size());
        }
    }
}
