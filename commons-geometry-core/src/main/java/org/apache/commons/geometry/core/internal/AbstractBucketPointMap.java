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

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.numbers.core.Precision;

/** Abstract tree-based {@link PointMap} implementation that stores entries in bucket nodes
 * that are split once a certain entry count threshold is reached. The main goal of this class
 * is to provide a generic, multidimensional implementation that maintains reasonable performance
 * regardless of point count and insertion order. Like other tree data structures, performance
 * is tied closely to tree depth, which can vary depending on insertion order for a given set of
 * points. In order to help maintain performance in cases of non-optimal point insertion order,
 * this class uses a strategy of "point folding", implemented as follows:
 * <ul>
 *  <li>Two separate tree roots are maintained by the map: a primary root and a secondary root.</li>
 *  <li>Entries are added to the primary root until the it reaches its capacity and is split using
 *      an algorithm specific to the space and dimension. At this point, the populated primary root
 *      becomes the secondary root and a new, empty primary root is created.</li>
 *  <li>Points are inserted into the new primary root as before. However, for each new point inserted,
 *      an existing point is removed from the secondary root and inserted into the primary root.</li>
 *  <li>Points are moved from the secondary root and inserted into the primary root in this way until the
 *      secondary root is empty. At this point, the primary root becomes the secondary root and another
 *      primary root is created.</li>
 * </ul>
 * In this way, previously inserted points can apply a balancing influence on the low levels of the tree
 * as new points are inserted.
 *
 * <p>This class is <em>not</em> thread-safe.</p>
 * @param <P> Point type
 * @param <V> Map value type
 */
public abstract class AbstractBucketPointMap<P extends Point<P>, V>
    extends AbstractMap<P, V>
    implements PointMap<P, V> {

    /** Interface for constructing new {@link BucketNode} instances.
     * @param <P> Point type
     * @param <V> Map value type
     */
    @FunctionalInterface
    public interface BucketNodeFactory<P extends Point<P>, V>  {

        /** Create a new {@link BucketNode} instance.
         * @param map owning map
         * @param parent parent node; will be null for the tree root
         * @param childIndex index of the new node in its parent child list; will be {@code -1}
         *      if the new node does not have a parent
         * @return the newly created node
         */
        BucketNode<P, V> createNode(AbstractBucketPointMap<P, V> map, BucketNode<P, V> parent, int childIndex);
    }

    /** Child index used when no node parent exists. */
    private static final int DEFAULT_CHILD_INDEX = -1;

    /** Function used to construct new node instances. */
    private final BucketNodeFactory<P, V> nodeFactory;

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

    /** Cached entry set; instances are stateless so we need only one. */
    private EntrySet entrySetInstance;

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
            final BucketNodeFactory<P, V> nodeFactory,
            final int maxNodeEntryCount,
            final int nodeChildCount,
            final Precision.DoubleEquivalence precision) {
        this.nodeFactory = nodeFactory;
        this.maxNodeEntryCount = maxNodeEntryCount;
        this.nodeChildCount = nodeChildCount;
        this.precision = precision;
        this.root = nodeFactory.createNode(this, null, DEFAULT_CHILD_INDEX);
    }

    /** {@inheritDoc} */
    @Override
    public Entry<P, V> getEntry(final P pt) {
        return findEntryByPoint(pt);
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
        GeometryInternalUtils.requireFinite(key);

        final Entry<P, V> entry = findEntryByPoint(key);
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
        if (entrySetInstance == null) {
            entrySetInstance = new EntrySet();
        }
        return entrySetInstance;
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        root = createNode(null, DEFAULT_CHILD_INDEX);
        secondaryRoot = null;
    }

    /** {@inheritDoc} */
    @Override
    public Entry<P, V> nearestEntry(final P pt) {
        GeometryInternalUtils.requireFinite(pt);

        DistancedValue<Entry<P, V>> result = root.findNearestEntry(pt);

        if (secondaryRoot != null) {
            final DistancedValue<Entry<P, V>> secondaryResult = secondaryRoot.findNearestEntry(pt);
            result = getNearest(secondaryResult, result);
        }

        return result != null ?
                result.getValue() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public Entry<P, V> farthestEntry(final P pt) {
        GeometryInternalUtils.requireFinite(pt);

        DistancedValue<Entry<P, V>> result = root.findFarthestEntry(pt);

        if (secondaryRoot != null) {
            final DistancedValue<Entry<P, V>> secondaryResult = secondaryRoot.findFarthestEntry(pt);
            result = getFarthest(secondaryResult, result);
        }

        return result != null ?
                result.getValue() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Entry<P, V>> entriesNearToFar(final P pt) {
        GeometryInternalUtils.requireFinite(pt);
        return new AbstractEntryCollection() {
            @Override
            public Iterator<Entry<P, V>> iterator() {
                return new NearToFarIterator<>(AbstractBucketPointMap.this, pt);
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
                return new FarToNearIterator<>(AbstractBucketPointMap.this, pt);
            }
        };
    }

    /** Get the configured precision for the instance.
     * @return precision object
     */
    protected Precision.DoubleEquivalence getPrecision() {
        return precision;
    }

    /** Return true if the given points are equivalent using the precision
     * configured for the map.
     * @param a first point
     * @param b second point
     * @return true if the given points are equivalent
     */
    protected abstract boolean pointsEq(P a, P b);

    /** Compare two points to determine a consistent ordering when other comparison
     * criteria consider them equal.
     * @param a first point
     * @param b second point
     * @return integer comparison result
     */
    protected abstract int disambiguatePointComparison(P a, P b);

    /** Construct a new node instance.
     * @param parent parent node; will be null for the tree root
     * @param childIndex index of the node in its parent child list
     * @return the new node instance
     */
    private BucketNode<P, V> createNode(final BucketNode<P, V> parent, final int childIndex) {
        return nodeFactory.createNode(this, parent, childIndex);
    }

    /** Method called when a new entry is added to the tree.
     */
    private void entryAdded() {
        ++version;

        if (!root.isLeaf() && secondaryRoot == null) {
            secondaryRoot = root;
            root = createNode(null, DEFAULT_CHILD_INDEX);
        }

        migrateSecondaryEntry();
        checkRemoveSecondaryRoot();
    }

    /** Method called when an entry is removed from the tree.
     */
    private void entryRemoved() {
        ++version;

        checkRemoveSecondaryRoot();
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
    @SuppressWarnings("unchecked")
    private List<BucketNode<P, V>> createNodeChildList() {
        return Arrays.asList(new BucketNode[nodeChildCount]);
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

            final Entry<P, V> entry = secondaryRoot.removeEntryAlongIndexPath(idx);
            if (entry != null) {
                root.insertEntry(entry);
            }
        }
    }

    /** Remove the secondary root if empty.
     */
    private void checkRemoveSecondaryRoot() {
        if (secondaryRoot != null && secondaryRoot.isEmpty()) {
            secondaryRoot.destroy();
            secondaryRoot = null;
        }
    }

    /** Get the argument with the smallest distance value.
     * @param a first entry; may be null
     * @param b second entry; may be null
     * @return argument with the smallest distance value
     */
    private DistancedValue<Entry<P, V>> getNearest(
            final DistancedValue<Entry<P, V>> a,
            final DistancedValue<Entry<P, V>> b) {
        return compareEntries(a, b, Double.POSITIVE_INFINITY) < 0 ?
                a :
                b;
    }

    /** Get the argument with the largest distance value.
     * @param a first entry; may be null
     * @param b second entry; may be null
     * @return argument with the largest distance value
     */
    private DistancedValue<Entry<P, V>> getFarthest(
            final DistancedValue<Entry<P, V>> a,
            final DistancedValue<Entry<P, V>> b) {
        return compareEntries(a, b, 0d) > 0 ?
                a :
                b;
    }

    /** Compare two entries with distance values. If either entry is null, the distance
     * for that entry is taken to be {@code nullDistance}.
     * @param a first entry; may be null
     * @param b second entry; may be null
     * @param nullDistance distance used for null arguments
     * @return integer comparison result
     */
    private int compareEntries(
            final DistancedValue<Entry<P, V>> a,
            final DistancedValue<Entry<P, V>> b,
            final double nullDistance) {
        final double aDist = a != null ? a.getDistance() : nullDistance;
        final double bDist = b != null ? b.getDistance() : nullDistance;

        int cmp = Double.compare(aDist, bDist);
        if (cmp == 0 &&
                a != null &&
                b != null) {
            return disambiguatePointComparison(a.getValue().getKey(), b.getValue().getKey());
        }
        return cmp;
    }

    /** Return true if {@code dist} is within the specified maximum value, meaning if it
     * is less than or equal to {@code maxDist} using the configured precision context.
     * @param dist distance to test
     * @param maxDist maximum distance
     * @return true if {@code dist} is within the given maximum
     */
    private boolean distanceIsWithinMax(final double dist, final double maxDist) {
        return precision.lte(dist, maxDist);
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

        /** Index of this node in its parent child list. */
        private int childIndex;

        /** Child nodes; elements may be null. */
        private List<BucketNode<P, V>> children;

        /** Entries stored in the node; will be null for non-leaf nodes. */
        private List<Entry<P, V>> entries;

        /** Number of entries in this subtree. */
        private int entryCount;

        /** Construct a new instance.
         * @param map owning map
         * @param parent parent node or null if the tree root
         * @param childIndex index of this node in its parent, or {@code -1}
         *      if no parent exists
         */
        protected BucketNode(
                final AbstractBucketPointMap<P, V> map,
                final BucketNode<P, V> parent,
                final int childIndex) {
            this.map = map;
            this.parent = parent;
            this.childIndex = childIndex;

            // pull an entry list from the parent map; this will make
            // this node a leaf initially
            this.entries = map.createEntryList();
        }

        /** Get the index of this node in its parent, or {@code -1} if this
         * node does not have a parent.
         * @return index of this node in its parent, or {@code -1} if this
         *      node does not have a parent
         */
        public int getChildIndex() {
            return childIndex;
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
                return null;
            }

            // internal node; delegate to each child that could possibly contain
            // the point or an equivalent point
            return findEntryInChildren(key);
        }

        /** Find and return the map entry matching the given key in the node's children.
         * This method must only be called on internal nodes.
         * @param key point key
         * @return entry matching the given key or null if not found
         */
        private Entry<P, V> findEntryInChildren(final P key) {
            final int loc = getSearchLocation(key);
            for (int i = 0; i < children.size(); ++i) {
                if (testChildLocation(i, loc)) {
                    final Entry<P, V> entry = getEntryInChild(i, key);
                    if (entry != null) {
                        return entry;
                    }
                }
            }
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
                return null;
            }

            // internal node; delegate to each child
            return findEntryByValueInChildren(value);
        }

        /** Find the first entry in the child subtrees for this node with the given value or null
         * if not found. This method must only be called on internal nodes.
         * @param value value to search for
         * @return the first entry in the child subtrees with the given value or null if not found
         */
        private Entry<P, V> findEntryByValueInChildren(final Object value) {
            for (final BucketNode<P, V> child : children) {
                if (child != null) {
                    final Entry<P, V> childResult = child.findEntryByValue(value);
                    if (childResult != null) {
                        return childResult;
                    }
                }
            }
            return null;
        }

        /** Insert a new entry into the subtree, returning the new size of the
         * subtree. No check is made as to whether or not the entry already exists.
         * @param entry entry to insert
         */
        public void insertEntry(final Entry<P, V> entry) {
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

                return null;
            }

            // internal node; delegate to each child
            return removeEntryFromChildren(key);
        }

        /** Remove the given key from the node's child subtrees, returning the previously
         * mapped entry. This method must only be called on internal nodes.
         * @param key key to remove
         * @return the value previously mapped to the key or null if no
         *       value was mapped
         */
        private Entry<P, V> removeEntryFromChildren(final P key) {
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
            return null;
        }

        /** Find the nearest entry to {@code refPt} in the subtree rooted at this node, or null if
         * the subtree is empty.
         * @param refPt reference point
         * @return nearest entry to {@code refPt} in the subtree rooted at this node, or null if no
         *      such entry exists
         */
        public DistancedValue<Entry<P, V>> findNearestEntry(final P refPt) {
            return findNearestEntry(refPt, Double.POSITIVE_INFINITY);
        }

        /** Find the nearest entry to {@code refPt} within the maximum distance specified in the subtree
         * rooted at this node, or null if no such entry exists.
         * @param refPt reference point
         * @param maxDist maximum search distance
         * @return nearest entry to {@code refPt} within the maximum distance specified in the subtree
         *      rooted at this node, or null if no such entry exists.
         */
        private DistancedValue<Entry<P, V>> findNearestEntry(final P refPt, final double maxDist) {
            if (isLeaf()) {
                // leaf node; look through the stored entries
                DistancedValue<Entry<P, V>> closest = null;

                for (final Entry<P, V> entry : entries) {
                    final DistancedValue<Entry<P, V>> entryWithDist = DistancedValue.of(
                            entry,
                            entry.getKey().distance(refPt));

                    if (map.distanceIsWithinMax(entryWithDist.getDistance(), maxDist)) {
                        closest = map.getNearest(entryWithDist, closest);
                    }
                }

                return closest;
            }

            // internal node;
            return findNearestEntryInChildren(refPt, maxDist);
        }

        /** Find the nearest entry to {@code refPt} within the maximum distance specified in the child subtrees
         * rooted at this node, or null if no such entry exists. This method must only be call on internal
         * nodes.
         * @param refPt reference point
         * @param maxDist maximum search distance
         * @return nearest entry to {@code refPt} within the maximum distance specified in the subtree
         *      rooted at this node, or null if no such entry exists.
         */
        private DistancedValue<Entry<P, V>> findNearestEntryInChildren(final P refPt, final double maxDist) {
            // look through children in order of increasing minimum distance from the
            // reference point
            final List<DistancedValue<BucketNode<P, V>>> sortedNodeList = new ArrayList<>(map.nodeChildCount);

            final int loc = getInsertLocation(refPt);
            for (int i = 0; i < children.size(); ++i) {
                final BucketNode<P, V> child = children.get(i);
                if (child != null) {
                    final double minChildDist = getMinChildDistance(i, refPt, loc);
                    if (map.distanceIsWithinMax(minChildDist, maxDist)) {
                        sortedNodeList.add(DistancedValue.of(child, minChildDist));
                    }
                }
            }

            Collections.sort(sortedNodeList, DistancedValue.ascendingDistance());

            DistancedValue<Entry<P, V>> closest = null;
            for (final DistancedValue<BucketNode<P, V>> nodeValue : sortedNodeList) {
                if (closest != null &&
                        map.distanceIsWithinMax(closest.getDistance(), nodeValue.getDistance())) {
                    // no more child nodes can contain anything closer so we can stop looking
                    break;
                }

                final DistancedValue<Entry<P, V>> entry = nodeValue.getValue()
                        .findNearestEntry(refPt, maxDist);
                closest = map.getNearest(entry, closest);
            }

            return closest;
        }

        /** Find the farthest entry from {@code refPt} within the subtree rooted at this node..
         * @param refPt reference point
         * @return farthest entry from {@code refPt} in the subtree rooted at this node, or null
         *      if no such entry exists.
         */
        public DistancedValue<Entry<P, V>> findFarthestEntry(final P refPt) {
            if (isLeaf()) {
                // leaf node; look through the stored entries
                DistancedValue<Entry<P, V>> farthest = null;

                for (final Entry<P, V> entry : entries) {
                    final DistancedValue<Entry<P, V>> entryWithDist = DistancedValue.of(
                            entry,
                            entry.getKey().distance(refPt));

                    farthest = map.getFarthest(entryWithDist, farthest);
                }

                return farthest;
            } else {
                // internal node; look through children in order of decreasing maximum distance
                // distance from the reference point
                final List<DistancedValue<BucketNode<P, V>>> sortedNodeList = new ArrayList<>(map.nodeChildCount);

                final int loc = getInsertLocation(refPt);
                for (int i = 0; i < children.size(); ++i) {
                    final BucketNode<P, V> child = children.get(i);
                    if (child != null) {
                        final double maxChildDist = getMaxChildDistance(i, refPt, loc);
                        sortedNodeList.add(DistancedValue.of(child, maxChildDist));
                    }
                }

                Collections.sort(sortedNodeList, DistancedValue.descendingDistance());

                DistancedValue<Entry<P, V>> farthest = null;
                for (final DistancedValue<BucketNode<P, V>> nodeValue : sortedNodeList) {
                    if (farthest != null &&
                            map.precision.gt(farthest.getDistance(), nodeValue.getDistance())) {
                        break;
                    }

                    final DistancedValue<Entry<P, V>> entry = nodeValue.getValue()
                            .findFarthestEntry(refPt);
                    farthest = map.getFarthest(entry, farthest);
                }

                return farthest;
            }
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
         * @param idx target child index
         * @return removed entry
         */
        public Entry<P, V> removeEntryAlongIndexPath(final int idx) {
            if (isLeaf()) {
                if (!entries.isEmpty()) {
                    // remove the last entry in the list
                    final Entry<P, V> entry = entries.remove(entries.size() - 1);
                    subtreeEntryRemoved();

                    return entry;
                }
            } else {
                final int childCount = children.size();
                final int delta = idx < (map.nodeChildCount / 2) ?
                        +1 :
                        -1;

                for (int n = 0, i = idx;
                        n < childCount;
                        ++n, i += delta) {
                    final int adjustedIndex = (i + childCount) % childCount;

                    final Entry<P, V> entry = removeEntryAlongIndexPathFromChild(adjustedIndex, idx);
                    if (entry != null) {
                        return entry;
                    }
                }
            }

            return null;
        }

        /** Remove an entry in a leaf node lying on the given index path, starting at the child at the
         * given index.
         * @param childIdx child index to remove from
         * @param idx index path
         * @return removed entry
         */
        private Entry<P, V> removeEntryAlongIndexPathFromChild(final int childIdx, final int idx) {
            final BucketNode<P, V> child = children.get(childIdx);
            if (child != null) {
                final Entry<P, V> entry = child.removeEntryAlongIndexPath(idx);
                if (entry != null) {
                    // destroy and remove the child if empty
                    if (child.isEmpty()) {
                        child.destroy();
                        children.set(childIdx, null);
                    }

                    subtreeEntryRemoved();

                    return entry;
                }
            }

            return null;
        }

        /** Destroy this node. The node must not be used after this method is called.
         */
        public void destroy() {
            this.map = null;
            this.parent = null;
            this.childIndex = DEFAULT_CHILD_INDEX;
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
        public Iterator<Entry<P, V>> iterator() {
            return iterator(0);
        }

        /** Return an iterator for accessing the entries stored in this node, starting at the given
         * index. The {@code remove()} method of the returned iterator correctly updates the tree state.
         * This method must only be called on leaf nodes.
         * @param idx starting index for the iterator
         * @return iterator for accessing the entries stored in this node, starting with the entry at
         *      the given index
         */
        private Iterator<Entry<P, V>> iterator(final int idx) {
            final List<Entry<P, V>> iteratedList = idx == 0 ?
                    entries :
                    entries.subList(idx, entries.size());

            final Iterator<Entry<P, V>> it = iteratedList.iterator();

            return new Iterator<Entry<P, V>>() {

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

        /** Get an integer encoding the insert location of {@code pt} relative to the
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

        /** Get the minimum distance from {@code pt} to the split boundary for the child at
         * the given index.
         * @param childIdx child index
         * @param pt point to compute the minimum distance from
         * @param ptLoc encoded relative point location
         * @return minimum distance from {@code pt} to the split boundary for the indicated child
         */
        protected abstract double getMinChildDistance(int childIdx, P pt, int ptLoc);

        /** Get the maximum distance from {@code pt} to the region for the child
         * child node at the specified index. A value of {@code Double#POSITIVE_INFINITY}
         * should be returned if there is no maximum.
         * @param childIdx index of the child in question
         * @param pt reference point
         * @param ptLoc encoded relative point location
         * @return maximum distance from {@code pt} to the node region
         */
        protected abstract double getMaxChildDistance(int childIdx, P pt, int ptLoc);

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

        /** Get the parent node or null if one does not exist.
         * @return parent node or null if one does not exist.
         */
        protected BucketNode<P, V> getParent() {
            return parent;
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
                child = map.createNode(this, idx);
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

        /** Get the maximum distance value from {@code n} to either {@code a} or {@code b}.
         * @param n test coordinate
         * @param a first coordinate
         * @param b second coordinate
         * @return maximum distance from {@code n} to {@code a} or {@code b}
         */
        public static double getMaxDistance(final double n, final double a, final double b) {
            return Math.max(
                    Math.abs(n - a),
                    Math.abs(n - b));
        }
    }

    /** Set view of the map entries.
     */
    private final class EntrySet
        extends AbstractSet<Entry<P, V>> {

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(final Object obj) {
            if (obj instanceof Entry) {
                final Entry<?, ?> search = (Entry<?, ?>) obj;
                final Object key = search.getKey();

                final Entry<P, V> actual = findEntry(key);
                if (actual != null) {
                    return pointsEq(actual.getKey(), (P) search.getKey()) &&
                            Objects.equals(actual.getValue(), search.getValue());
                }
            }
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<Entry<P, V>> iterator() {
            return new EntryIterator();
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return AbstractBucketPointMap.this.size();
        }
    }

    /** Iterator for iterating through each entry in the map.
     */
    private final class EntryIterator
        implements Iterator<Entry<P, V>> {

        /** Size of the owning map. */
        private int size;

        /** Iterator that produces the next entry to be returned. */
        private Iterator<Entry<P, V>> nextEntryIterator;

        /** Index of the entry that will be returned next from the iterator. */
        private int nextIdx;

        /** Expected map modification version. */
        private int expectedVersion;

        /** Simple constructor.
         */
        EntryIterator() {
            this.size = AbstractBucketPointMap.this.size();

            updateExpectedVersion();
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            if (nextEntryIterator == null ||
                    !nextEntryIterator.hasNext()) {
                nextEntryIterator = findIterator();
            }

            return nextEntryIterator != null;
        }

        /** {@inheritDoc} */
        @Override
        public Entry<P, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            checkVersion();

            final Entry<P, V> result = nextEntryIterator.next();
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
            if (secondaryRoot != null) {
                final Iterator<Entry<P, V>> secondaryIt = findIteratorRecursive(secondaryRoot, offset);
                if (secondaryIt != null) {
                    return secondaryIt;
                }

                offset += secondaryRoot.getEntryCount();
            }

            return findIteratorRecursive(root, offset);
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
            if (expectedVersion != version) {
                throw new ConcurrentModificationException();
            }
        }

        /** Update the expected modification version of the map. This must be called
         * whenever the map is changed through this instance.
         */
        private void updateExpectedVersion() {
            expectedVersion = version;
        }
    }

    /** Abstract type representing a collection over the entries in this map.
     */
    private abstract class AbstractEntryCollection extends AbstractCollection<Entry<P, V>> {

        /** {@inheritDoc} */
        @Override
        public int size() {
            return AbstractBucketPointMap.this.size();
        }
    }

    /** Abstract base class for iterators that returned entries in order of distance relative
     * to a reference point.
     * @param <P> Point type
     * @param <V> Value type
     */
    private abstract static class AbstractDistanceOrderIterator<P extends Point<P>, V>
        implements Iterator<Entry<P, V>> {

        /** Owning map. */
        private final AbstractBucketPointMap<P, V> map;

        /** The expected modification version of the map. */
        private final int expectedVersion;

        /** Distance order reference point. */
        private final P refPt;

        /** Queue of nodes remaining to be visited. */
        private final PriorityQueue<DistancedValue<BucketNode<P, V>>> nodes;

        /** Queue of entries waiting to be returned. */
        private final PriorityQueue<DistancedValue<Entry<P, V>>> entries;

        /** The next entry to be returned from the iterator. */
        private Entry<P, V> nextEntry;

        /** Construct a new instance for ordering map entries by distance in reference
         * to {@code refPt}.
         * @param map owning map
         * @param refPt reference point used to determine distance
         * @param rootDistance distance value to use for the root nodes
         * @param nodeComparator node comparator
         * @param entryComparator entry comparator
         */
        AbstractDistanceOrderIterator(
                final AbstractBucketPointMap<P, V> map,
                final P refPt,
                final double rootDistance,
                final Comparator<DistancedValue<BucketNode<P, V>>> nodeComparator,
                final Comparator<DistancedValue<Entry<P, V>>> entryComparator) {

            this.map = map;
            this.expectedVersion = map.version;

            this.refPt = refPt;

            this.nodes = new PriorityQueue<>(nodeComparator);
            this.entries = new PriorityQueue<>(entryComparator);

            this.nodes.add(DistancedValue.of(map.root, rootDistance));
            if (map.secondaryRoot != null) {
                this.nodes.add(DistancedValue.of(map.secondaryRoot, rootDistance));
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return nextEntry != null;
        }

        /** {@inheritDoc} */
        @Override
        public Entry<P, V> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            checkVersion();

            final Entry<P, V> result = nextEntry;
            queueNextEntry();

            return result;
        }

        /** Queue the next entry to be returned from the iterator. This must be
         * called after initialization to prepare the first return value.
         */
        void queueNextEntry() {
            while (requiresTreeTraversal()) {
                final DistancedValue<BucketNode<P, V>> nodeEntry = nodes.remove();
                final BucketNode<P, V> node = nodeEntry.getValue();

                if (node.isLeaf()) {
                    // add the entries to the entries queue
                    queueLeafEntries(node);
                } else {
                    // queue the child nodes
                    queueChildren(nodeEntry, refPt);
                }
            }

            nextEntry = entries.isEmpty() ?
                    null :
                    entries.remove().getValue();
        }

        /** Return true if the given entry is ready to be returned based on the values in the
         * next queue node.
         * @param entry next entry in the queue
         * @param nextNode next node in the queue
         * @param precision precision context to use for floating point comparisons
         * @return true if the given entry can be returned from the iterator
         */
        abstract boolean canReturnEntry(
                DistancedValue<Entry<P, V>> entry,
                DistancedValue<BucketNode<P, V>> nextNode,
                Precision.DoubleEquivalence precision);

        /** Queue the child nodes of the argument.
         * @param nodeEntry distance entry containing the parent node
         * @param pt reference point
         */
        abstract void queueChildren(DistancedValue<BucketNode<P, V>> nodeEntry, P pt);

        /** Add a node and its computed distance to the queue.
         * @param nodeEntry node entry to add
         */
        void queue(final DistancedValue<BucketNode<P, V>> nodeEntry) {
            nodes.add(nodeEntry);
        }

        /** Add all entries in the given leaf node to the entries queue.
         * @param node leaf node
         */
        private void queueLeafEntries(final BucketNode<P, V> node) {
            for (final Entry<P, V> entry : node.entries) {
                final double dist = entry.getKey().distance(refPt);
                entries.add(DistancedValue.of(entry, dist));
            }
        }

        /** Return true if the tree needs to be traversed more in order to determine the next
         * entry to return.
         * @return true if the tree needs to be traversed more to determine the next entry to
         *      return
         */
        private boolean requiresTreeTraversal() {
            return !nodes.isEmpty() &&
                    (entries.isEmpty() || !canReturnEntry(entries.peek(), nodes.peek(), map.precision));
        }

        /** Throw a {@link ConcurrentModificationException} if the map version does
         * not match the expected version.
         */
        private void checkVersion() {
            if (expectedVersion != map.version) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /** Iterator that returns map entries in order of increasing distance from a specified point.
     * @param <P> Point type
     * @param <V> Value type
     */
    private static final class NearToFarIterator<P extends Point<P>, V>
        extends AbstractDistanceOrderIterator<P, V> {

        /** Construct a new iterator instance for the given map and reference point.
         * @param map owning map
         * @param refPt reference point
         */
        NearToFarIterator(final AbstractBucketPointMap<P, V> map, final P refPt) {
            super(map,
                    refPt,
                    0d,
                    DistancedValue.ascendingDistance(),
                    (a, b) -> map.compareEntries(a, b, Double.POSITIVE_INFINITY));

            queueNextEntry();
        }

        /** {@inheritDoc} */
        @Override
        protected boolean canReturnEntry(
                final DistancedValue<Entry<P, V>> nextEntry,
                final DistancedValue<BucketNode<P, V>> nextNode,
                final Precision.DoubleEquivalence precision) {
            return precision.lt(nextEntry.getDistance(), nextNode.getDistance());
        }

        /** {@inheritDoc} */
        @Override
        void queueChildren(final DistancedValue<BucketNode<P, V>> nodeEntry, final P refPt) {
            final BucketNode<P, V> node = nodeEntry.getValue();

            final int loc = node.getInsertLocation(refPt);

            final int childCount = node.children.size();
            for (int i = 0; i < childCount; ++i) {
                final BucketNode<P, V> child = node.children.get(i);
                if (child != null) {
                    final double childDist = node.getMinChildDistance(i, refPt, loc);

                    queue(DistancedValue.of(child, childDist));
                }
            }
        }
    }

    /** Iterator that returns map entries in order of decreasing distance from a specified point.
     * @param <P> Point type
     * @param <V> Value type
     */
    private static final class FarToNearIterator<P extends Point<P>, V>
        extends AbstractDistanceOrderIterator<P, V> {

        /** Construct a new iterator instance for the given map and reference point.
         * @param map owning map
         * @param refPt reference point
         */
        FarToNearIterator(final AbstractBucketPointMap<P, V> map, final P refPt) {
            super(map,
                    refPt,
                    Double.POSITIVE_INFINITY,
                    DistancedValue.descendingDistance(),
                    (a, b) -> -map.compareEntries(a, b, 0d));

            queueNextEntry();
        }

        /** {@inheritDoc} */
        @Override
        protected boolean canReturnEntry(
                final DistancedValue<Entry<P, V>> nextEntry,
                final DistancedValue<BucketNode<P, V>> nextNode,
                final Precision.DoubleEquivalence precision) {
            return precision.gt(nextEntry.getDistance(), nextNode.getDistance());
        }

        /** {@inheritDoc} */
        @Override
        void queueChildren(final DistancedValue<BucketNode<P, V>> nodeEntry, final P refPt) {
            final BucketNode<P, V> node = nodeEntry.getValue();
            final double nodeDist = nodeEntry.getDistance();

            final int loc = node.getInsertLocation(refPt);

            final int childCount = node.children.size();
            for (int i = 0; i < childCount; ++i) {
                final BucketNode<P, V> child = node.children.get(i);
                if (child != null) {
                    // use the minimum of distance from the parent and the child since the child
                    // cannot contain anything that is not also in the parent
                    final double childDist = Math.min(nodeDist, node.getMaxChildDistance(i, refPt, loc));

                    queue(DistancedValue.of(child, childDist));
                }
            }
        }
    }
}
