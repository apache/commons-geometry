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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import javax.script.AbstractScriptEngine;

import org.apache.commons.geometry.core.internal.GeometryInternalUtils;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.examples.jmh.euclidean.pointmap.KDTree.KDTreeNode;
import org.apache.commons.numbers.core.Precision;

/**
 * KD-tree implementation that buffers vector entries at leaf nodes
 * and then splits on the median of the buffer contents when enough
 * nodes are collected. This guarantees that the has at least some
 * branching.
 * @param <V> Value type
 */
public class BucketKDTree<V> extends AbstractMap<Vector3D, V> {

    /** Enum containing possible node cut dimensions. */
    enum CutDimension {

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

    /** Array of cut dimensions; pull these eagerly to avoid having to call values() constantly. */
    private static final CutDimension[] CUT_DIMENSIONS = CutDimension.values();

    /** Number of map entries stored in leaf nodes before splitting. */
    private static final int ENTRY_BUFFER_SIZE = 10;

    /** Precision context. */
    private final Precision.DoubleEquivalence precision;

    /** Root node; not null. */
    private AbstractNode<V> root;

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

        final Vector3DEntry<V> entry = root.findOrInsert(key);
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
        // TODO
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Vector3D, V>> entrySet() {
        throw new UnsupportedOperationException();
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

    static abstract class AbstractNode<V> {

        final BucketKDTree<V> tree;

        private EntryNode<V> parent;

        AbstractNode(final BucketKDTree<V> tree, final EntryNode<V> parent) {
            this.tree = tree;
            this.parent = parent;
        }

        protected boolean eq(final Vector3DEntry<V> entry, final Vector3D key) {
            return entry.getKey().eq(key, tree.precision);
        }

        protected int eqCompare(final double a, final double b) {
            return tree.precision.compare(a, b);
        }

        protected void replaceSelf(final AbstractNode<V> node) {
            if (parent == null) {
                // this is the root
                node.parent = null;
                tree.root = node;
            } else {
                node.parent = parent;
                if (GeometryInternalUtils.sameInstance(parent.left, this)) {
                    parent.left = node;
                } else {
                    parent.right = node;
                }
            }
        }

        abstract Vector3DEntry<V> find(Vector3D key);

        abstract Vector3DEntry<V> findOrInsert(Vector3D key);

        abstract Vector3DEntry<V> remove(Vector3D key);

        abstract Vector3DEntry<V> findMin(CutDimension dim);

        abstract void insert(Vector3DEntry<V> entry);
    }

    static final class EntryNode<V> extends AbstractNode<V> {

        private Vector3DEntry<V> entry;

        /** Left child node; not null. */
        private AbstractNode<V> left;

        /** Right child node; not null. */
        private AbstractNode<V> right;

        /** Cut dimension; not null. */
        private CutDimension cutDimension;

        EntryNode(final BucketKDTree<V> tree, final EntryNode<V> parent, final Vector3DEntry<V> entry,
                final CutDimension cutDimension) {
            super(tree, parent);

            this.entry = entry;
            this.cutDimension = cutDimension;
        }

        /** {@inheritDoc} */
        @Override
        Vector3DEntry<V> find(final Vector3D key) {
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
            } else {
                // check if we are equivalent to the point for this node
                if (eq(entry, key)) {
                    return entry;
                }

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
        Vector3DEntry<V> findOrInsert(final Vector3D key) {
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
            } else {
                // check if we are equivalent to the point for this node
                if (eq(entry, key)) {
                    return entry;
                }

                // We are not equivalent and we straddle the cut line for this node,
                // meaning that an existing node equivalent to the key could be on either
                // side of the cut. Perform a strict comparison to determine where the
                // node would be inserted if we were inserting. Then check, search the
                // opposite subtree for an existing node and if not found, go ahead and
                // try inserting into the target subtree.
                final int strictCmp = Double.compare(keyCoord, nodeCoord);
                if (strictCmp < 0) {
                    // insertion, if needed, would be performed in the left subtree, so
                    // check the right subtree first
                    final Vector3DEntry<V> rightExisting = right != null ?
                            right.find(key) :
                            null;
                    if (rightExisting != null) {
                        return rightExisting;
                    }

                    return getOrCreateLeft().findOrInsert(key);
                } else {
                    // insertion, if needed, would be performed in the right subtree, so
                    // check the left subtree first
                    final Vector3DEntry<V> leftExisting = left != null ?
                            left.find(key) :
                            null;
                    if (leftExisting != null) {
                        return leftExisting;
                    }

                    return getOrCreateRight().findOrInsert(key);
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        Vector3DEntry<V> remove(final Vector3D key) {
            // TODO Auto-generated method stub
            return null;
        }

        /** {@inheritDoc} */
        @Override
        Vector3DEntry<V> findMin(final CutDimension dim) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        void insert(final Vector3DEntry<V> newEntry) {
            // pull the coordinates for the node cut dimension
            final double nodeCoord = cutDimension.getCoordinate(entry.getKey());
            final double newCoord = cutDimension.getCoordinate(newEntry.getKey());

            // perform an equivalence comparison
            final int eqCmp = eqCompare(newCoord, nodeCoord);

            if (eqCmp < 0) {
                // we definitely belong in the left subtree
                getOrCreateLeft().insert(newEntry);
            } else if (eqCmp > 0) {
                // we definitely belong in the right subtree
                getOrCreateRight().insert(newEntry);
            } else {
                // We are not equivalent and we straddle the cut line for this node,
                // meaning that an existing node equivalent to the key could be on either
                // side of the cut. Perform a strict comparison to determine where the
                // node would be inserted if we were inserting. Then check, search the
                // opposite subtree for an existing node and if not found, go ahead and
                // try inserting into the target subtree.
                final int strictCmp = Double.compare(newCoord, nodeCoord);
                if (strictCmp < 0) {
                    getOrCreateLeft().insert(newEntry);
                } else {
                    getOrCreateRight().insert(newEntry);
                }
            }
        }

        AbstractNode<V> getOrCreateLeft() {
            if (left == null) {
                left = new BucketNode<>(tree, this);
            }
            return left;
        }

        AbstractNode<V> getOrCreateRight() {
            if (right == null) {
                right = new BucketNode<>(tree, this);
            }
            return right;
        }
    }

    static final class BucketNode<V> extends AbstractNode<V> {

        private List<Vector3DEntry<V>> entries = new ArrayList<>(ENTRY_BUFFER_SIZE);

        BucketNode(final BucketKDTree<V> tree, final EntryNode<V> parent) {
            super(tree, parent);
        }

        /** {@inheritDoc} */
        @Override
        Vector3DEntry<V> find(final Vector3D key) {
            for (Vector3DEntry<V> entry : entries) {
                if (eq(entry, key)) {
                    return entry;
                }
            }
            return null;
        }

        /** {@inheritDoc} */
        @Override
        Vector3DEntry<V> findOrInsert(final Vector3D key) {
            Vector3DEntry<V> entry = find(key);
            if (entry == null) {
                // we need to create the entry
                entry = new Vector3DEntry<>(key, null);

                if (entries.size() < ENTRY_BUFFER_SIZE) {
                    entries.add(entry);
                } else {
                    // we need to split
                    final EntryNode<V> splitNode = split();

                    replaceSelf(splitNode);

                    splitNode.insert(entry);
                }
            }

            return entry;
        }

        /** {@inheritDoc} */
        @Override
        Vector3DEntry<V> remove(final Vector3D key) {
            // TODO Auto-generated method stub
            return null;
        }

        /** {@inheritDoc} */
        @Override
        Vector3DEntry<V> findMin(final CutDimension dim) {
            // TODO Auto-generated method stub
            return null;
        }

        /** {@inheritDoc} */
        @Override
        void insert(final Vector3DEntry<V> entry) {
            entries.add(entry);
        }

        private EntryNode<V> split() {
            final CutDimension dim = determineCutDimension();

            entries.sort((a, b) ->
                Double.compare(dim.getCoordinate(a.getKey()), dim.getCoordinate(b.getKey())));
            final Vector3DEntry<V> median = entries.get(entries.size() / 2);

            final EntryNode<V> node = new EntryNode<>(tree, null, median, dim);

            entries.clear();

            return node;
        }

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
                } else if (diff.getY() > diff.getZ()) {
                    return CutDimension.Y;
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
}
