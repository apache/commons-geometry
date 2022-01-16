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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.internal.GeometryInternalUtils;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.numbers.core.Sum;

public class BucketLeafKDTree<V> extends AbstractMap<Vector3D, V> {
    /** Token used in debug tree strings. */
    private static final String TREE_STRING_EQ_TOKEN = " => ";

    /** Number of map entries stored in leaf nodes before splitting. */
    private static final int BUCKET_SIZE = 10;

    /** Precision context. */
    private final Precision.DoubleEquivalence precision;

    /** Root node; not null. */
    private Node<V> root;

    /** Construct a new instance with the given precision.
     * @param precision object used to determine floating point equality between dimension
     *      coordinates
     */
    public BucketLeafKDTree(final Precision.DoubleEquivalence precision) {
        this.precision = precision;
        this.root = new BucketNode<>(this, null);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return root.size();
    }

    /** {@inheritDoc} */
    @Override
    public V put(final Vector3D key, final V value) {
        validateKey(key);

        final Vector3DEntry<V> entry = root.findOrInsert(key).getEntry();
        final V prevValue = entry.getValue();
        entry.setValue(value);

        return prevValue;
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        final Vector3DEntry<V> entry = root.find((Vector3D) key);
        return entry != null ?
                entry.getValue() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        final Vector3DEntry<V> entry = root.remove((Vector3D) key);
        return entry != null ?
                entry.getValue() :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Vector3D, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    /** Return a string representation of this tree for debugging purposes.
     * @return a string representation of this tree
     */
    public String treeString() {
        final StringBuilder sb = new StringBuilder();

        root.treeString(0, sb);

        return sb.toString();
    }

    /** Throw an exception if {@code key} is invalid.
     * @param key map key
     */
    private void validateKey(final Vector3D key) {
        Objects.requireNonNull(key);
        if (!key.isFinite()) {
            throw new IllegalArgumentException("Map key must be finite; was " + key);
        }
    }

    /** Enum containing possible node split dimensions. */
    private enum Dimension {

        /** X dimension. */
        X(Vector3D::getX),

        /** Y dimension. */
        Y(Vector3D::getY),

        /** Z dimension. */
        Z(Vector3D::getZ);

        /** Value extraction function. */
        private final ToDoubleFunction<Vector3D> valueFn;

        Dimension(final ToDoubleFunction<Vector3D> valueFn) {
            this.valueFn = valueFn;
        }

        /** Get the value of the given point along this dimension.
         * @param pt point
         * @return dimension coordinate
         */
        public double getValue(final Vector3D pt) {
            return valueFn.applyAsDouble(pt);
        }
    }

    /** Abstract base class for bucket kd-tree nodes.
     * @param <V> Value type
     */
    private abstract static class Node<V> {

        /** Owning tree. */
        private final BucketLeafKDTree<V> tree;

        /** Parent node; null for root node. */
        private CutNode<V> parent;

        Node(final BucketLeafKDTree<V> tree, final CutNode<V> parent) {
            this.tree = tree;
            this.parent = parent;
        }

        public BucketLeafKDTree<V> getTree() {
            return tree;
        }

        public CutNode<V> getParent() {
            return parent;
        }

        /** Return the map entry equivalent to the given key or null if not
         * found.
         * @param key key to search for
         * @return entry equivalent to {@code key} or null if not found
         */
        public abstract Vector3DEntry<V> find(Vector3D key);

        /** Return the map entry equivalent to the given key. An entry is created
         * if one does not exist.
         * @param key map key
         * @return result of the operation
         */
        public abstract FindOrInsertResult<V> findOrInsert(Vector3D key);

        /** Remove the map entry equivalent to the given key or null if one
         * was not found.
         * @param key map key
         * @return the removed entry or null if not found
         */
        public abstract Vector3DEntry<V> remove(Vector3D key);

        public abstract int size();

        /** Return a string representation of the subtree rooted at this node.
         * @param depth depth of this node
         * @param sb string builder to append content to
         */
        public void treeString(final int depth, final StringBuilder sb) {
            for (int i = 0; i < depth; ++i) {
                sb.append("    ");
            }

            String label = parent == null ?
                    "*" :
                    isLeftChild() ? "L" : "R";

            sb.append("[")
                .append(label);
        }

        /** Insert an existing entry into the subtree rooted at this node. The
         * entry is guaranteed not to exist in the subtree.
         * @param entry
         */
        protected abstract void insertExisting(Vector3DEntry<V> entry);

        /** Complete a tree restructuring. Node should perform whatever internal operations
         * are needed to continue with standard functionality.
         */
        protected abstract void completeRestructure();

        /** Return true if the entry and key are equivalent according to the map
         * precision context.
         * @param entry map entry
         * @param key key value
         * @return true if the entry and key are equivalent
         */
        protected boolean eq(final Vector3DEntry<V> entry, final Vector3D key) {
            return entry.getKey().eq(key, tree.precision);
        }

        /** Compare the given values using the precision context of the map.
         * @param a first value
         * @param b second value
         * @return comparison result
         */
        protected int eqCompare(final double a, final double b) {
            return tree.precision.compare(a, b);
        }

        /** Return true if this node is the left child of its parent.
         * @return true if this node is the left child of its parent
         */
        protected boolean isLeftChild() {
            return parent != null && GeometryInternalUtils.sameInstance(parent.left, this);
        }

        /** Return true if this node is the root of the tree.
         * @return true if this node is the root of the tree
         */
        protected boolean isRoot() {
            return parent == null && GeometryInternalUtils.sameInstance(tree.root, this);
        }

        /**
         * Replace this node with the given one.
         * @param node
         */
        protected void replaceSelf(final Node<V> node) {
            if (isRoot()) {
                if (node != null) {
                    tree.root = node;
                } else {
                    tree.root = new BucketNode<>(tree, null);
                }
            } else if (isLeftChild()) {
                parent.left = node;
            } else {
                parent.right = node;
            }
        }
    }

    /** Internal tree node containing a single entry and a cut dimension.
     * @param <V> Value type
     */
    private static final class CutNode<V> extends Node<V> {

        /** Left child node; may be null. */
        private Node<V> left;

        /** Right child node; may be null. */
        private Node<V> right;

        /** Cut dimension; not null. */
        private final Dimension cutDimension;

        /** Value in the cut dimension that this node splits on. */
        private final double cutValue;

        /** Number of map entries in the subtree rooted at this node. */
        private int subtreeEntryCount;

        CutNode(final BucketLeafKDTree<V> tree, final CutNode<V> parent, final Dimension cutDimension,
                final double cutValue) {
            super(tree, parent);

            this.cutDimension = cutDimension;
            this.cutValue = cutValue;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3DEntry<V> find(final Vector3D key) {
            // pull the coordinates for the node cut dimension
            final double keyCoord = cutDimension.getValue(key);

            // use a strict comparison first
            final int strictCmp = Double.compare(keyCoord, cutValue);
            Vector3DEntry<V> entry = findByComparison(key, strictCmp);

            if (entry == null) {
                // not found using a strict comparison; check the fuzzy comparison
                // and search any sides we didn't check yet
                final int eqCmp = eqCompare(keyCoord, cutValue);
                if (strictCmp < 0 && eqCmp >= 0) {
                    // didn't search right previously; do so now
                    return findRight(key);
                } else if (strictCmp > 0 && eqCmp <= 0) {
                    // didn't search left previously; do so now
                    return findLeft(key);
                }
            }

            return entry;
        }

        private Vector3DEntry<V> findByComparison(final Vector3D key, final int cmp) {
            if (cmp < 0) {
                return findLeft(key);
            } else if (cmp > 0) {
                return findRight(key);
            } else {
                // The matching node (if any) could be on either side of the cut so we'll
                // need to search both subtrees. Since points with cut dimension coordinates
                // are always inserted into the right subtree,
                // search that subtree first.
                final Vector3DEntry<V> rightResult = findRight(key);
                if (rightResult != null) {
                    return rightResult;
                }

                return findLeft(key);
            }
        }

        private Vector3DEntry<V> findLeft(final Vector3D key) {
            return left != null ?
                    left.find(key) :
                    null;
        }

        private Vector3DEntry<V> findRight(final Vector3D key) {
            return right != null ?
                    right.find(key) :
                    null;
        }

        /** {@inheritDoc} */
        @Override
        public FindOrInsertResult<V> findOrInsert(final Vector3D key) {
            final FindOrInsertResult<V> result = findOrInsertInternal(key);
            if (result.isNewEntry()) {
                ++subtreeEntryCount;
            }
            return result;
        }

        public FindOrInsertResult<V> findOrInsertInternal(final Vector3D key) {
            // pull the coordinates for the node cut dimension
            final double keyCoord = cutDimension.getValue(key);

            // perform an equivalence comparison
            final int eqCmp = eqCompare(keyCoord, cutValue);

            if (eqCmp < 0) {
                // we definitely belong in the left subtree
                return getOrCreateLeft().findOrInsert(key);
            } else if (eqCmp > 0) {
                // we definitely belong in the right subtree
                return getOrCreateRight().findOrInsert(key);
            } else {
                // We are not equivalent and we straddle the cut line for this node,
                // meaning that an existing node equivalent to the key could be on either
                // side of the cut. Perform a strict comparison to determine where the
                // node would be inserted if we were inserting. Then check the opposite
                // subtree for an existing node and if not found, go ahead and try inserting
                // into the target subtree.
                final int strictCmp = Double.compare(keyCoord, cutValue);
                if (strictCmp < 0) {
                    // insertion, if needed, would be performed in the left subtree, so
                    // check the right subtree first
                    final Vector3DEntry<V> rightExisting = right != null ?
                            right.find(key) :
                            null;
                    if (rightExisting != null) {
                        return FindOrInsertResult.existingEntry(rightExisting);
                    }

                    return getOrCreateLeft().findOrInsert(key);
                } else {
                    // insertion, if needed, would be performed in the right subtree, so
                    // check the left subtree first
                    final Vector3DEntry<V> leftExisting = left != null ?
                            left.find(key) :
                            null;
                    if (leftExisting != null) {
                        return FindOrInsertResult.existingEntry(leftExisting);
                    }

                    return getOrCreateRight().findOrInsert(key);
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public Vector3DEntry<V> remove(final Vector3D key) {
            final Vector3DEntry<V> entry = removeInternal(key);
            if (entry != null) {
                --subtreeEntryCount;

                if (subtreeEntryCount < 1) {
                    replaceSelf(null);
                }
            }

            return entry;
        }

        public Vector3DEntry<V> removeInternal(final Vector3D key) {
            // pull the coordinates for the node cut dimension
            final double keyCoord = cutDimension.getValue(key);

            // perform an equivalence comparison
            final int eqcmp = eqCompare(keyCoord, cutValue);

            if (eqcmp < 0) {
                return left != null ?
                        left.remove(key) :
                        null;
            } else if (eqcmp > 0) {
                return right != null ?
                        right.remove(key) :
                        null;
            } else {
                // Not equivalent; the matching node (if any) could be on either
                // side of the cut so we'll need to search both subtrees. Use the side
                // closest to the key as the first to search.
                final Node<V> first;
                final Node<V> second;
                if (Double.compare(keyCoord, cutValue) < 0) {
                    first = left;
                    second = right;
                } else {
                    first = right;
                    second = left;
                }

                final Vector3DEntry<V> firstRemoveResult = first != null ?
                        first.remove(key) :
                        null;
                if (firstRemoveResult != null) {
                    return firstRemoveResult;
                }

                return second != null ?
                        second.remove(key) :
                        null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public void insertExisting(final Vector3DEntry<V> newEntry) {
            // pull the coordinates for the node cut dimension
            final double newCoord = cutDimension.getValue(newEntry.getKey());

            // perform an equivalence comparison
            final int eqCmp = eqCompare(newCoord, cutValue);

            if (eqCmp < 0) {
                // we definitely belong in the left subtree
                getOrCreateLeft().insertExisting(newEntry);
            } else if (eqCmp > 0) {
                // we definitely belong in the right subtree
                getOrCreateRight().insertExisting(newEntry);
            } else {
                // We are not equivalent and we straddle the cut line for this node,
                // meaning that an existing node equivalent to the key could be on either
                // side of the cut. Perform a strict comparison to determine where the
                // node would be inserted if we were inserting. Then check, search the
                // opposite subtree for an existing node and if not found, go ahead and
                // try inserting into the target subtree.
                final int strictCmp = Double.compare(newCoord, cutValue);
                if (strictCmp < 0) {
                    getOrCreateLeft().insertExisting(newEntry);
                } else {
                    getOrCreateRight().insertExisting(newEntry);
                }
            }

            // we've definitely inserted and entry into our subtree, so update our count
            ++subtreeEntryCount;
        }

        /** {@inheritDoc} */
        @Override
        protected void completeRestructure() {
            if (left != null) {
                left.completeRestructure();
            }
            if (right != null) {
                right.completeRestructure();
            }
        }

        @Override
        public int size() {
            return subtreeEntryCount;
        }

        /** Get or create the left child node.
         * @return left child node
         */
        private Node<V> getOrCreateLeft() {
            if (left == null) {
                left = new BucketNode<>(getTree(), this);
            }
            return left;
        }

        /** Get or create the right child node.
         * @return right child node
         */
        private Node<V> getOrCreateRight() {
            if (right == null) {
                right = new BucketNode<>(getTree(), this);
            }
            return right;
        }

        /** {@inheritDoc} */
        @Override
        public void treeString(final int depth, final StringBuilder sb) {
            super.treeString(depth, sb);

            sb.append(" | ")
                .append(cutDimension)
                .append("] ")
                .append(cutValue)
                .append("\n");

            if (left != null) {
                left.treeString(depth + 1, sb);
            }
            if (right != null) {
                right.treeString(depth + 1, sb);
            }
        }
    }

    /** Leaf node class containing multiple entries.
     * @param <V> Value type
     */
    private static final class BucketNode<V> extends Node<V> {

        /** Minimum dimension check size. */
        private static final int MIN_SORT_SIZE = 4;

        /** List of entries. */
        private List<Vector3DEntry<V>> entries = new ArrayList<>(BUCKET_SIZE);

        /** Dimension used to sort the entries. */
        private Dimension sortDimension;

        /** The entry size that a max dimension check will be performed. */
        private int sortDimensionCheckSize = MIN_SORT_SIZE;

        BucketNode(final BucketLeafKDTree<V> tree, final CutNode<V> parent) {
            super(tree, parent);
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return entries.size();
        }

        /** {@inheritDoc} */
        @Override
        public Vector3DEntry<V> find(final Vector3D key) {
            if (sortDimension != null) {
                // sorted search
                final double keyCoord = sortDimension.getValue(key);
                return binarySearch(key, keyCoord, 0, entries.size());
            } else {
                // unsorted; search sequentially
                final int size = entries.size();
                for (int i = 0; i < size; ++i) {
                    final Vector3DEntry<V> entry = entries.get(i);
                    if (eq(entry, key)) {
                        return entry;
                    }
                }

                return null;
            }
        }

        private Vector3DEntry<V> binarySearch(final Vector3D key, final double keyCoord,
                final int startIdx, final int endIdx) {
            final int diff = endIdx - startIdx;
            if (diff >= MIN_SORT_SIZE) {
                final int idx = startIdx + (diff / 2);

                final Vector3DEntry<V> entry = entries.get(idx);
                final double entryCoord = sortDimension.getValue(entry.getKey());

                final int cmp = eqCompare(keyCoord, entryCoord);
                if (cmp < 0) {
                    // search the low portion
                    return binarySearch(key, keyCoord, startIdx, idx);
                } else if (cmp > 0) {
                    // search the high portion
                    return binarySearch(key, keyCoord, idx + 1, endIdx);
                } else if (eq(entry, key)) {
                    // this is the entry we want
                    return entry;
                } else {
                    // search nearby entries
                    Vector3DEntry<V> lowEntry = sortedLinearSearchDec(key, keyCoord, startIdx, idx);
                    if (lowEntry != null) {
                        return lowEntry;
                    }

                    return sortedLinearSearchInc(key, keyCoord, idx + 1, endIdx);
                }
            } else {
                return sortedLinearSearchInc(key, keyCoord, startIdx, endIdx);
            }
        }

        private Vector3DEntry<V> sortedLinearSearchInc(final Vector3D key, final double keyCoord, final int lowIdx,
                final int highIdx) {
            for (int i = lowIdx; i < highIdx; ++i) {
                final Vector3DEntry<V> entry = entries.get(i);
                final double entryCoord = sortDimension.getValue(entry.getKey());

                final int cmp = eqCompare(keyCoord, entryCoord);
                if (cmp == 0) {
                    if (eq(entry, key)) {
                        return entry;
                    }
                } else if (cmp < 0) {
                    break;
                }
            }

            return null;
        }

        private Vector3DEntry<V> sortedLinearSearchDec(final Vector3D key, final double keyCoord, final int lowIdx,
                final int highIdx) {
            for (int i = highIdx - 1; i >= lowIdx; --i) {
                final Vector3DEntry<V> entry = entries.get(i);
                final double entryCoord = sortDimension.getValue(entry.getKey());

                final int cmp = eqCompare(keyCoord, entryCoord);
                if (cmp == 0) {
                    if (eq(entry, key)) {
                        return entry;
                    }
                } else if (cmp > 0) {
                    break;
                }
            }

            return null;
        }

        /** {@inheritDoc} */
        @Override
        public FindOrInsertResult<V> findOrInsert(final Vector3D key) {
            final Vector3DEntry<V> existingEntry = find(key);
            if (existingEntry != null) {
                return FindOrInsertResult.existingEntry(existingEntry);
            } else {
                // we need to create the entry
                final Vector3DEntry<V> newEntry = new Vector3DEntry<>(key, null);

                if (entries.size() < BUCKET_SIZE) {
                    // we have space to fit the new entry
                    insertNew(newEntry);
                } else {
                    // we need to split
                    final CutNode<V> splitNode = split();

                    splitNode.insertExisting(newEntry);

                    // let child nodes complete
                    splitNode.completeRestructure();

                    // replace ourselves with the new split subtree
                    replaceSelf(splitNode);
                }

                return FindOrInsertResult.newEntry(newEntry);
            }
        }

        /** {@inheritDoc} */
        @Override
        public Vector3DEntry<V> remove(final Vector3D key) {
            final Iterator<Vector3DEntry<V>> it = entries.iterator();
            while (it.hasNext()) {
                final Vector3DEntry<V> entry = it.next();
                if (eq(entry, key)) {
                    it.remove();

                    return entry;
                }
            }

            return null;
        }

        /** {@inheritDoc} */
        @Override
        public void insertExisting(final Vector3DEntry<V> entry) {
            entries.add(entry);
        }

        /** {@inheritDoc} */
        @Override
        public void treeString(final int depth, final StringBuilder sb) {
            super.treeString(depth, sb);

            String entryStr = entries.stream()
                    .map(e -> e.getKey() + TREE_STRING_EQ_TOKEN + e.getValue())
                    .collect(Collectors.joining(", "));

            sb.append("] [")
                .append(entryStr)
                .append("]\n");
        }

        /** {@inheritDoc} */
        @Override
        protected void completeRestructure() {
            // set to defaults
            sortDimension = null;
            sortDimensionCheckSize = MIN_SORT_SIZE;

            // compute the best sort dimension check size
            int size = entries.size();
            while (sortDimensionCheckSize < size) {
                sortDimensionCheckSize *= 2;
            }

            // sort if needed
            if (size >= MIN_SORT_SIZE) {
                resort();
            }
        }

        /** Insert a new entry into the node. This method assumes that space already
         * exists to contain the entry.
         * @param entry entry to insert
         */
        private void insertNew(final Vector3DEntry<V> entry) {
            if (sortDimension != null) {
                // already sorted; insert into the correct sorted position
                final int insertIdx = findClosestSorted(entry);
                entries.add(insertIdx, entry);
            } else {
                // insert unsorted
                entries.add(entry);

                // sort if we just passed the threshold for sorting
                if (entries.size() >= sortDimensionCheckSize) {
                    resort();

                    // reset for the next size check
                    sortDimensionCheckSize *= 2;
                }
            }
        }

        /** Compute a new sorting dimension for the current set of entries and
         * resort them if needed.
         */
        private void resort() {
            final Bounds3D bounds = getBounds();
            final Dimension newDim = determineSplitDimension(bounds);

            if (newDim != sortDimension) {
                sortDimension = newDim;

                Collections.sort(entries, this::compareEntries);
            }
        }

        /** Find the index of the entry closest to the given search entry in the
         * current sort dimension.
         * @param search entry to search for
         * @return index of the entry closest to the argument in the current sort
         *      dimension
         */
        private int findClosestSorted(final Vector3DEntry<V> search) {
            final int idx = Collections.binarySearch(entries, search, this::compareEntries);
            return idx >= 0 ?
                    idx :
                    -idx - 1;
        }

        /** Compare two entries using the current sort dimension.
         * @param a first entry
         * @param b second entry
         * @return
         */
        private int compareEntries(final Vector3DEntry<V> a, final Vector3DEntry<V> b) {
            return Double.compare(
                    sortDimension.getValue(a.getKey()),
                    sortDimension.getValue(b.getKey()));
        }

        /** Split this instance into a {@link CutNode} and child nodes.
         * @return the new cut node that should replace this instance
         */
        private CutNode<V> split() {
            final Bounds3D bounds = getBounds();
            final Dimension dim = determineSplitDimension(bounds);

            // get the centroid in the split dimension
            final double splitValue = getCentroidValue(dim);

            // create the subtree root node on the median
            final CutNode<V> subtree = new CutNode<>(getTree(), getParent(), dim, splitValue);

            // insert the other entries
            for (final Vector3DEntry<V> entry : entries) {
                subtree.insertExisting(entry);
            }

            // clean up
            entries.clear();

            return subtree;
        }

        /** Get the bounds for all entries currently in the node.
         * @return bounds of the entry keys for this node
         */
        private Bounds3D getBounds() {
            final Bounds3D.Builder boundsBuilder = Bounds3D.builder();
            for (Vector3DEntry<V> entry : entries) {
                boundsBuilder.add(entry.getKey());
            }

            return boundsBuilder.build();
        }

        /** Determine the best splitting dimension to use for the given bounds.
         * @param bounds entry key bounds
         * @return split cutting dimension
         */
        private Dimension determineSplitDimension(final Bounds3D bounds) {
            final Vector3D diff = bounds.getDiagonal();

            if (diff.getX() > diff.getY()) {
                if (diff.getX() > diff.getZ()) {
                    return Dimension.X;
                } else {
                    return Dimension.Z;
                }
            } else if (diff.getY() > diff.getZ()) {
                return Dimension.Y;
            } else {
                return Dimension.Z;
            }
        }

        /** Get the centroid of the entries along with given dimension.
         * @param dim dimension
         * @return the centroid of the entries along with given dimension.
         */
        private double getCentroidValue(final Dimension dim) {
            final Sum sum = Sum.create();
            for (final Vector3DEntry<V> entry : entries) {
                sum.add(dim.getValue(entry.getKey()));
            }

            return sum.getAsDouble() / entries.size();
        }
    }

    /** Class encapsulating the result of a find-or-insert operation.
     * @param <V> Value type
     */
    private static final class FindOrInsertResult<V> {
        /** Map entry. */
        private final Vector3DEntry<V> entry;

        /** Flag indicating if the entry is new. */
        private final boolean newEntry;

        FindOrInsertResult(final Vector3DEntry<V> entry, final boolean newEntry) {
            this.entry = entry;
            this.newEntry = newEntry;
        }

        /** Return the map entry.
         * @return map entry
         */
        public Vector3DEntry<V> getEntry() {
            return entry;
        }

        /** Return true if the map entry is new.
         * @return true if the map entry is new
         */
        public boolean isNewEntry() {
            return newEntry;
        }

        /** Construct a new result instance representing an existing map entry.
         * @param <V> Value type
         * @param entry existing entry
         * @return instance representing an existing map entry
         */
        public static <V> FindOrInsertResult<V> existingEntry(final Vector3DEntry<V> entry) {
            return new FindOrInsertResult<>(entry, false);
        }

        /** Construct a new result instance representing a new map entry.
         * @param <V> Value type
         * @param entry new map entry
         * @return instance representing a new map entry
         */
        public static <V> FindOrInsertResult<V> newEntry(final Vector3DEntry<V> entry) {
            return new FindOrInsertResult<>(entry, true);
        }
    }
}
