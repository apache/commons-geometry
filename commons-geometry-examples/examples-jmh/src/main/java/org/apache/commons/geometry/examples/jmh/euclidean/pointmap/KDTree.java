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
 */package org.apache.commons.geometry.examples.jmh.euclidean.pointmap;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;

/**
 * Standard kd-tree implementation with no self-balancing or rebuilding features.
 * @param <V> Value type
 */
public class KDTree<V> extends AbstractMap<Vector3D, V> {

    private enum CutDimension {
        X(Vector3D::getX),
        Y(Vector3D::getY),
        Z(Vector3D::getZ);

        private final ToDoubleFunction<Vector3D> coordinateFn;

        CutDimension(final ToDoubleFunction<Vector3D> coordinateFn) {
            this.coordinateFn = coordinateFn;
        }

        public double getCoordinate(final Vector3D v) {
            return coordinateFn.applyAsDouble(v);
        }
    }

    /** Precision context. */
    private final Precision.DoubleEquivalence precision;

    private KDTreeNode<V> root;

    private int nodeCount;

    /** Array of cut dimensions; pull these eagerly to avoid having to call values() constantly. */
    private CutDimension[] cutDimensions = CutDimension.values();

    public KDTree(final Precision.DoubleEquivalence precision) {
        this.precision = precision;
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

        final KDTreeNode<V> node;

        if (root == null) {
            // first node; enter as the root
            root = createNode(key, 0);
            node = root;
        } else {
            // not the first node; enter into the tree
            node = getOrInsertNodeRecursive(root, key, 0);
        }

        return node.setValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        final KDTreeNode<V> node = getNodeRecursive(root, (Vector3D) key);
        return node != null ?
                node.value :
                null;
    }

    private KDTreeNode<V> getOrInsertNodeRecursive(final KDTreeNode<V> node, final Vector3D key, final int depth) {
        // pull the coordinates for the node cut dimension
        final double nodeCoord = node.cutDimension.getCoordinate(node.key);
        final double keyCoord = node.cutDimension.getCoordinate(key);

        // perform an equivalence comparison
        final int eqCmp = precision.compare(keyCoord, nodeCoord);

        final int childDepth = depth + 1;

        if (eqCmp < 0) {
            // we definitely belong in the left subtree
            if (node.left == null) {
                node.left = createNode(key, childDepth);
                return node.left;
            } else {
                return getOrInsertNodeRecursive(node.left, key, childDepth);
            }
        } else if (eqCmp > 0 ){
            // we definitely belong in the right subtree
            if (node.right == null) {
                node.right = createNode(key, childDepth);
                return node.right;
            } else {
                return getOrInsertNodeRecursive(node.right, key, childDepth);
            }
        } else {
            // check if we are equivalent to the point for this node
            if (key.eq(node.key, precision)) {
                return node;
            }

            // We are not equivalent and we straddle the cut line for this node,
            // meaning that an existing node equivalent to the key could be on either
            // side of the cut. Perform a strict comparison to determine where the
            // node would be inserted if we were inserting. Then check, search the
            // opposite subtree for an existing node and if not found, go ahead and
            // try inserting into the target subtree.
            final int strictCmp = compareStrict(keyCoord, nodeCoord);
            if (strictCmp < 0) {
                // insertion, if needed, would be performed in the left subtree, so
                // check the right subtree first
                final KDTreeNode<V> rightExistingNode = getNodeRecursive(node.right, key);
                if (rightExistingNode != null) {
                    return rightExistingNode;
                }

                if (node.left == null) {
                    node.left = createNode(key, childDepth);
                    return node.left;
                }

                return getOrInsertNodeRecursive(node.left, key, childDepth);
            } else {
                // insertion, if needed, would be performed in the right subtree, so
                // check the left subtree first
                final KDTreeNode<V> leftExistingNode = getNodeRecursive(node.left, key);
                if (leftExistingNode != null) {
                    return leftExistingNode;
                }

                if (node.right == null) {
                    node.right = createNode(key, childDepth);
                    return node.right;
                }

                return getOrInsertNodeRecursive(node.right, key, childDepth);
            }
        }
    }

    private KDTreeNode<V> getNodeRecursive(final KDTreeNode<V> node, final Vector3D key) {
        if (node != null) {
            // pull the coordinates for the node cut dimension
            final double nodeCoord = node.cutDimension.getCoordinate(node.key);
            final double keyCoord = node.cutDimension.getCoordinate(key);

            // perform an equivalence comparison
            final int eqcmp = precision.compare(keyCoord, nodeCoord);

            if (eqcmp < 0) {
                return getNodeRecursive(node.left, key);
            } else if (eqcmp > 0 ){
                return getNodeRecursive(node.right, key);
            } else {
                // check if we are equivalent to the point for this node
                if (key.eq(node.key, precision)) {
                    return node;
                }

                // Not equivalent; we'll the matching node (if any) could be on either
                // side of the cut so we'll need to search both subtrees.
                final KDTreeNode<V> leftSearchResult = getNodeRecursive(node.left, key);
                if (leftSearchResult != null) {
                    return leftSearchResult;
                }

                return getNodeRecursive(node.right, key);
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Entry<Vector3D, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    private void validateKey(final Vector3D key) {
        Objects.requireNonNull(key);
        if (!key.isFinite()) {
            throw new IllegalArgumentException("Map key must be finite; was " + key);
        }
    }

    /** Create a new {@link KDTreeNode} for entry into the tree and increment the internal
     * node count.
     * @param key
     * @param depth
     * @return
     */
    private KDTreeNode<V> createNode(final Vector3D key, final int depth) {
        final KDTreeNode<V> node = new KDTreeNode<>(
                getCutDimensionForDepth(depth),
                key);

        ++nodeCount;

        return node;
    }

    private CutDimension getCutDimensionForDepth(final int depth) {
        return cutDimensions[depth % cutDimensions.length];
    }

    private int compareStrict(final double a, final double b) {
        return Double.compare(a, b);
    }

    private static final class KDTreeNode<V> implements Map.Entry<Vector3D, V> {

        final CutDimension cutDimension;

        final Vector3D key;

        V value;

        KDTreeNode<V> left;

        KDTreeNode<V> right;

        KDTreeNode(final CutDimension cutDimension, final Vector3D key) {
            this.cutDimension = cutDimension;
            this.key = key;
        }

        /** {@inheritDoc} */
        @Override
        public Vector3D getKey() {
            return key;
        }

        /** {@inheritDoc} */
        @Override
        public V getValue() {
            return value;
        }

        /** {@inheritDoc} */
        @Override
        public V setValue(final V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }
}
