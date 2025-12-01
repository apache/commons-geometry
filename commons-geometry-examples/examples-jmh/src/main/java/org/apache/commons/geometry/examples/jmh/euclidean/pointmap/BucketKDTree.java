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
package org.apache.commons.geometry.examples.jmh.euclidean.pointmap;

import java.util.AbstractMap;
import java.util.ArrayList;
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

/**
 * KD-tree implementation that buffers vector entries at leaf nodes
 * and then splits on the median of the buffer contents when enough
 * nodes are collected. This guarantees that the tree has at least some
 * branching.
 * @param <V> Value type
 */
public class BucketKDTree<V> extends AbstractMap<Vector3D, V> {

    /** Token used in debug tree strings. */
    private static final String TREE_STRING_EQ_TOKEN = " => ";

    /** Number of map entries stored in leaf nodes before splitting. */
    private static final int ENTRY_BUFFER_SIZE = 10;

    /** Precision context. */
    private final Precision.DoubleEquivalence precision;

    /** Root node; not null. */
    private Node<V> root;

    /** Tree node count. */
    private int nodeCount;

    /** Construct a new instance with the given precision.
     * @param precision object used to determine floating point equality between dimension
     *      coordinates
     */
    public BucketKDTree(final Precision.DoubleEquivalence precision) {
        this.precision = precision;
        this.root = new BucketNode<>(this, null);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return nodeCount;
    }

    /** {@inheritDoc} */
    @Override
    public V put(final Vector3D key, final V value) {
        validateKey(key);

        final FindOrInsertResult<V> result = root.findOrInsert(key);
        if (result.isNewEntry()) {
            ++nodeCount;
        }

        final Vector3DEntry<V> entry = result.getEntry();
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
        if (entry != null) {
            --nodeCount;

            root.condense();

            return entry.getValue();
        }
        return null;
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

    /** Enum containing possible node cut dimensions. */
    private enum CutDimension {

        /** X dimension. */
        X(Vector3D::getX),

        /** Y dimension. */
        Y(Vector3D::getY),

        /** Z dimension. */
        Z(Vector3D::getZ);

        /** Coordinate extraction function. */
        private final ToDoubleFunction<Vector3D> coordinateFn;

        CutDimension(final ToDoubleFunction<Vector3D> coordinateFn) {
            this.coordinateFn = coordinateFn;
        }

        /** Get the coordinate for this dimension.
         * @param pt point
         * @return dimension coordinate
         */
        public double getCoordinate(final Vector3D pt) {
            return coordinateFn.applyAsDouble(pt);
        }
    }

    /** Interface representing a reference to single entry in the tree.
     * @param <V> Value type
     */
    private interface EntryReference<V> {

        /** Get the entry.
         * @return the entry
         */
        Vector3DEntry<V> getEntry();

        /** Remove the referenced entry from the tree.
         * @return the removed entry
         */
        Vector3DEntry<V> remove();
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

    /** Abstract base class for bucket kd-tree nodes.
     * @param <V> Value type
     */
    private abstract static class Node<V> {

        /** Owning tree. */
        protected final BucketKDTree<V> tree;

        /** Parent node; null for root node. */
        protected CutNode<V> parent;

        /** True if the node needs to be condensed. */
        protected boolean requiresCondense;

        Node(final BucketKDTree<V> tree, final CutNode<V> parent) {
            this.tree = tree;
            this.parent = parent;
        }

        /** Return the map entry equivalent to the given key or {@code null} if not
         * found.
         * @param key key to search for
         * @return entry equivalent to {@code key} or {@code null} if not found
         */
        public abstract Vector3DEntry<V> find(Vector3D key);

        /** Return the map entry equivalent to the given key. An entry is created
         * if one does not exist.
         * @param key map key
         * @return result of the operation
         */
        public abstract FindOrInsertResult<V> findOrInsert(Vector3D key);

        /** Find the entry in the subtree rooted at this node containing the minimum value measured
         * along the cut dimension.
         * @param dim dimension to measure along
         * @return the entry containing the minimum value measured along the cut dimension
         */
        public abstract EntryReference<V> findMin(CutDimension dim);

        /** Insert an entry from another portion of the tree into the subtree
         * rooted at this node.
         * @param entry entry to insert
         */
        public abstract void insertExisting(Vector3DEntry<V> entry);

        /** Remove the map entry equivalent to the given key or {@code null} if one
         * was not found.
         * @param key map key
         * @return the removed entry or {@code null} if not found
         */
        public abstract Vector3DEntry<V> remove(Vector3D key);

        /** Mark this node and its ancestors as requiring condensing.
         */
        public void markRequiresCondense() {
            Node<V> node = this;
            while (node != null && !node.requiresCondense) {
                node.requiresCondense = true;
                node = node.parent;
            }
        }

        /** Condense the subtree rooted at this node if possible.
         */
        public abstract void condense();

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

        /** Validate the state of the tree.
         */
        public abstract void validate();

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

        /** Replace this node with the given node.
         * @param node replacement node; may be {@code null}
         */
        protected void replaceSelf(final Node<V> node) {
            if (parent == null) {
                // this is the root
                if (node != null) {
                    node.parent = null;
                }

                tree.root = node;
            } else {
                if (node != null) {
                    node.parent = parent;
                }

                if (isLeftChild()) {
                    parent.left = node;
                } else {
                    parent.right = node;
                }
            }
        }

        /** Return true if this node is the left child of its parent.
         * @return true if this node is the left child of its parent
         */
        protected boolean isLeftChild() {
            return parent != null && GeometryInternalUtils.sameInstance(parent.left, this);
        }
    }

    /** Internal tree node containing a single entry and a cut dimension.
     * @param <V> Value type
     */
    private static final class CutNode<V> extends Node<V> {

        /** Map entry. */
        private Vector3DEntry<V> entry;

        /** Left child node; may be {@code null}. */
        private Node<V> left;

        /** Right child node; may be {@code null}. */
        private Node<V> right;

        /** Cut dimension; not null. */
        private CutDimension cutDimension;

        CutNode(final BucketKDTree<V> tree, final CutNode<V> parent, final Vector3DEntry<V> entry,
                final CutDimension cutDimension) {
            super(tree, parent);

            this.entry = entry;
            this.cutDimension = cutDimension;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3DEntry<V> find(final Vector3D key) {
            // pull the coordinates for the node cut dimension
            final double nodeCoord = cutDimension.getCoordinate(entry.getKey());
            final double keyCoord = cutDimension.getCoordinate(key);

            // perform an equivalence comparison
            final int eqcmp = eqCompare(keyCoord, nodeCoord);

            if (eqcmp < 0) {
                return left != null ?
                        left.find(key) :
                        null;
            } else if (eqcmp > 0) {
                return right != null ?
                        right.find(key) :
                        null;
            } else if (eq(entry, key)) {
                // our entry key is equivalent to the search key
                return entry;
            } else {
                // Not equivalent; the matching node (if any) could be on either
                // side of the cut so we'll need to search both subtrees. Since points with
                // cut dimension coordinates are always inserted into the right subtree,
                // search that subtree first.
                final Vector3DEntry<V> rightSearchResult = right != null ?
                        right.find(key) :
                        null;
                if (rightSearchResult != null) {
                    return rightSearchResult;
                }

                return left != null ?
                        left.find(key) :
                        null;
            }
        }

        /** {@inheritDoc} */
        @Override
        public FindOrInsertResult<V> findOrInsert(final Vector3D key) {
            // pull the coordinates for the node cut dimension
            final double nodeCoord = cutDimension.getCoordinate(entry.getKey());
            final double keyCoord = cutDimension.getCoordinate(key);

            // perform an equivalence comparison
            final int eqCmp = eqCompare(keyCoord, nodeCoord);

            if (eqCmp < 0) {
                // we definitely belong in the left subtree
                return getOrCreateLeft().findOrInsert(key);
            } else if (eqCmp > 0) {
                // we definitely belong in the right subtree
                return getOrCreateRight().findOrInsert(key);
            } else if (eq(entry, key)) {
                // our entry key is equivalent to the search key
                return FindOrInsertResult.existingEntry(entry);
            } else {
                // We are not equivalent and we straddle the cut line for this node,
                // meaning that an existing node equivalent to the key could be on either
                // side of the cut. Perform a strict comparison to determine where the
                // node would be inserted if we were inserting. Then check the opposite
                // subtree for an existing node and if not found, go ahead and try inserting
                // into the target subtree.
                final int strictCmp = Double.compare(keyCoord, nodeCoord);
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
            // pull the coordinates for the node cut dimension
            final double nodeCoord = cutDimension.getCoordinate(entry.getKey());
            final double keyCoord = cutDimension.getCoordinate(key);

            // perform an equivalence comparison
            final int eqcmp = eqCompare(keyCoord, nodeCoord);

            if (eqcmp < 0) {
                return left != null ?
                        left.remove(key) :
                        null;
            } else if (eqcmp > 0) {
                return right != null ?
                        right.remove(key) :
                        null;
            } else if (eq(entry, key)) {
                return removeOwnEntry();
            } else {
                // Not equivalent; the matching node (if any) could be on either
                // side of the cut so we'll need to search both subtrees. Use the side
                // closest to the key as the first to search.
                final Node<V> first;
                final Node<V> second;
                if (Double.compare(keyCoord, nodeCoord) < 0) {
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

        /** Remove the entry for this cut node and replace it with an appropriate
         * value from a child node.
         * @return the entry removed from this node.
         */
        private Vector3DEntry<V> removeOwnEntry() {
            final Vector3DEntry<V> result = entry;
            entry = null;

            if (right != null) {
                entry = right.findMin(cutDimension).remove();
            } else if (left != null) {
                // swap left and right subtrees; the replacement entry will
                // contain the minimum of the entire subtree
                entry = left.findMin(cutDimension).remove();

                right = left;
                left = null;
            }

            return result;
        }

        /** {@inheritDoc} */
        @Override
        public EntryReference<V> findMin(final CutDimension dim) {
            if (cutDimension.equals(dim)) {
                // this node splits on the dimensions we're searching for, so we
                // only need to search the left subtree
                if (left == null) {
                    return new CutNodeEntryReference<>(this);
                }
                return left.findMin(cutDimension);
            } else {
                // search both subtrees for the minimum value
                final EntryReference<V> leftMin = left != null ?
                        left.findMin(dim) :
                        null;
                final EntryReference<V> rightMin = right != null ?
                        right.findMin(dim) :
                        null;

                return minResult(
                        leftMin,
                        new CutNodeEntryReference<>(this),
                        rightMin,
                        dim);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void condense() {
            if (requiresCondense) {
                // condense children
                if (left != null) {
                    left.condense();
                }
                if (right != null) {
                    right.condense();
                }

                if (entry == null) {
                    // no entries in this subtree; remove completely
                    replaceSelf(null);
                } else if (right == null && left == null) {
                    // no more children; convert to a bucket node
                    final BucketNode<V> bucket = new BucketNode<>(tree, null);
                    bucket.entries.add(entry);

                    replaceSelf(bucket);
                }
            }

            requiresCondense = false;
        }

        /** {@inheritDoc} */
        @Override
        public void insertExisting(final Vector3DEntry<V> newEntry) {
            // pull the coordinates for the node cut dimension
            final double nodeCoord = cutDimension.getCoordinate(entry.getKey());
            final double newCoord = cutDimension.getCoordinate(newEntry.getKey());

            // perform an equivalence comparison
            final int eqCmp = eqCompare(newCoord, nodeCoord);

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
                final int strictCmp = Double.compare(newCoord, nodeCoord);
                if (strictCmp < 0) {
                    getOrCreateLeft().insertExisting(newEntry);
                } else {
                    getOrCreateRight().insertExisting(newEntry);
                }
            }
        }

        /** Get or create the left child node.
         * @return left child node
         */
        private Node<V> getOrCreateLeft() {
            if (left == null) {
                left = new BucketNode<>(tree, this);
            }
            return left;
        }

        /** Get or create the right child node.
         * @return right child node
         */
        private Node<V> getOrCreateRight() {
            if (right == null) {
                right = new BucketNode<>(tree, this);
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
                .append(entry.getKey())
                .append(TREE_STRING_EQ_TOKEN)
                .append(entry.getValue())
                .append("\n");

            if (left != null) {
                left.treeString(depth + 1, sb);
            }
            if (right != null) {
                right.treeString(depth + 1, sb);
            }
        }

        /** {@inheritDoc} */
        @Override
        public void validate() {
            if (entry == null) {
                throw new IllegalArgumentException("Cut node entry cannot be null");
            }
            if (left == null && right == null) {
                throw new IllegalArgumentException("Cut node cannot be leaf: " + entry.getKey());
            }

            if (left != null) {
                left.validate();
            }

            if (right != null) {
                right.validate();
            }
        }
    }

    /** {@link EntryReference} implementation representing the entry from a {@link CutNode}.
     * @param <V> Value type
     */
    private static final class CutNodeEntryReference<V> implements EntryReference<V> {

        /** Cut node instance. */
        private final CutNode<V> node;

        CutNodeEntryReference(final CutNode<V> node) {
            this.node = node;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3DEntry<V> getEntry() {
            return node.entry;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3DEntry<V> remove() {
            final Vector3DEntry<V> result = node.entry;
            node.removeOwnEntry();

            return result;
        }
    }

    /** Leaf node class containing multiple entries.
     * @param <V> Value type
     */
    private static final class BucketNode<V> extends Node<V> {

        /** List of entries. */
        private List<Vector3DEntry<V>> entries = new ArrayList<>(ENTRY_BUFFER_SIZE);

        BucketNode(final BucketKDTree<V> tree, final CutNode<V> parent) {
            super(tree, parent);
        }

        /** {@inheritDoc} */
        @Override
        public Vector3DEntry<V> find(final Vector3D key) {
            for (Vector3DEntry<V> entry : entries) {
                if (eq(entry, key)) {
                    return entry;
                }
            }
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public FindOrInsertResult<V> findOrInsert(final Vector3D key) {
            Vector3DEntry<V> entry = find(key);
            if (entry != null) {
                return FindOrInsertResult.existingEntry(entry);
            } else {
                // we need to create the entry
                entry = new Vector3DEntry<>(key, null);

                if (entries.size() < ENTRY_BUFFER_SIZE) {
                    entries.add(entry);
                } else {
                    // we need to split
                    final CutNode<V> splitNode = split();

                    replaceSelf(splitNode);

                    splitNode.insertExisting(entry);
                }

                return FindOrInsertResult.newEntry(entry);
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

                    checkCondense();

                    return entry;
                }
            }

            return null;
        }

        /** {@inheritDoc} */
        @Override
        public EntryReference<V> findMin(final CutDimension dim) {
            int idx = 0;
            int minIdx = -1;
            double minCoord = Double.POSITIVE_INFINITY;

            for (final Vector3DEntry<V> entry : entries) {
                final double coord = dim.getCoordinate(entry.getKey());
                if (minIdx < 0 || coord < minCoord) {
                    minIdx = idx;
                    minCoord = coord;
                }

                ++idx;
            }

            return new BucketNodeEntryReference<>(this, minIdx);
        }

        /** Check if this node requires condensing and mark it if so.
         */
        private void checkCondense() {
            if (entries.isEmpty() && parent != null) {
                markRequiresCondense();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void condense() {
            if (requiresCondense) {
                replaceSelf(null);
            }

            requiresCondense = false;
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
        public void validate() {
            if (entries.isEmpty() && parent != null) {
                throw new IllegalArgumentException("Non-root bucket node entry list is empty");
            }
        }

        /** Split this instance into a {@link CutNode} and child nodes.
         * @return the new cut node that should replace this instance
         */
        private CutNode<V> split() {
            final CutDimension dim = determineCutDimension();

            // find the median
            entries.sort((a, b) ->
                Double.compare(dim.getCoordinate(a.getKey()), dim.getCoordinate(b.getKey())));
            final int medianIdx = entries.size() / 2;
            final Vector3DEntry<V> median = entries.get(medianIdx);

            // create the subtree root node on the median
            final CutNode<V> subtree = new CutNode<>(tree, null, median, dim);

            // insert the other entries
            for (int i = 0; i < entries.size(); ++i) {
                if (i != medianIdx) {
                    subtree.insertExisting(entries.get(i));
                }
            }

            // clean up
            entries.clear();

            return subtree;
        }

        /** Determine the best cutting dimension to use for the current set of entries.
         * @return split cutting dimension
         */
        private CutDimension determineCutDimension() {
            final Bounds3D.Builder boundsBuilder = Bounds3D.builder();
            for (Vector3DEntry<V> entry : entries) {
                boundsBuilder.add(entry.getKey());
            }

            final Bounds3D bounds = boundsBuilder.build();
            final Vector3D diff = bounds.getDiagonal();

            if (diff.getX() > diff.getY()) {
                if (diff.getX() > diff.getZ()) {
                    return CutDimension.X;
                } else {
                    return CutDimension.Z;
                }
            } else if (diff.getY() > diff.getZ()) {
                return CutDimension.Y;
            } else {
                return CutDimension.Z;
            }
        }
    }

    /** {@link EntryReference} implementation representing the entry from a {@link BucketNode}.
     * @param <V> Value type
     */
    private static final class BucketNodeEntryReference<V> implements EntryReference<V> {

        /** Bucket node reference. */
        private final BucketNode<V> node;

        /** Entry index. */
        private final int idx;

        BucketNodeEntryReference(final BucketNode<V> node, final int idx) {
            this.node = node;
            this.idx = idx;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3DEntry<V> getEntry() {
            return node.entries.get(idx);
        }

        /** {@inheritDoc} */
        @Override
        public Vector3DEntry<V> remove() {
            final Vector3DEntry<V> result = node.entries.remove(idx);
            node.checkCondense();

            return result;
        }
    }

    /** Return the node containing the minimum value along the given cut dimension.
     * @param <V> Value type
     * @param a first node
     * @param b second node
     * @param c third node
     * @param cutDimension search dimension
     * @return minimum node
     */
    private static <V> EntryReference<V> minResult(final EntryReference<V> a, final EntryReference<V> b,
            final EntryReference<V> c,
            final CutDimension cutDimension) {
        final EntryReference<V> tempMin = minResult(a, b, cutDimension);
        return minResult(tempMin, c, cutDimension);
    }

    /** Return the node containing the minimum value along the given cut dimension. If one
     * argument is {@code null}, the other argument is returned.
     * @param <V> Value type
     * @param a first node
     * @param b second node
     * @param cutDimension search dimension
     * @return minimum node
     */
    private static <V> EntryReference<V> minResult(final EntryReference<V> a, final EntryReference<V> b,
            final CutDimension cutDimension) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        final double aCoord = cutDimension.getCoordinate(a.getEntry().getKey());
        final double bCoord = cutDimension.getCoordinate(b.getEntry().getKey());

        return aCoord < bCoord ? a : b;
    }
}
