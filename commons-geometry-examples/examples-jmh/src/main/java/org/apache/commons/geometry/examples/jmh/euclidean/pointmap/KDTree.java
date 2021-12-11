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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import org.apache.commons.geometry.core.internal.GeometryInternalUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;

/**
 * Standard kd-tree implementation with no self-balancing or rebuilding features.
 * @param <V> Value type
 */
public class KDTree<V> extends AbstractMap<Vector3D, V> {

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

    /** Precision context. */
    private final Precision.DoubleEquivalence precision;

    /** Root node; may be null. */
    private KDTreeNode<V> root;

    /** Tree node count. */
    private int nodeCount;

    /** Array of cut dimensions; pull these eagerly to avoid having to call values() constantly. */
    private CutDimension[] cutDimensions = CutDimension.values();

    /** Construct a new instance with the given precision.
     * @param precision object used to determine floating point equality between dimension
     *      coordinates
     */
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
            root = createNode(null, key, 0);
            node = root;
        } else {
            // not the first node; enter into the tree
            node = findOrInsertNodeRecursive(root, key, 0);
        }

        return node.setValue(value);
    }

    /** {@inheritDoc} */
    @Override
    public V get(final Object key) {
        final KDTreeNode<V> node = findNodeRecursive(root, (Vector3D) key);
        return node != null ?
                node.value :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        final KDTreeNode<V> node = findNodeRecursive(root, (Vector3D) key);
        if (node != null) {
            final V prevValue = node.value;

            removeKey(node);
            --nodeCount;

            return prevValue;
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

        treeStringRecursive(root, 0, sb);

        return sb.toString();
    }

    /** Recursively build a string representation of a subtree.
     * @param node subtree root
     * @param depth node depth
     * @param sb string output
     */
    private void treeStringRecursive(final KDTreeNode<V> node, final int depth, final StringBuilder sb) {
        if (node != null) {
            for (int i = 0; i < depth; ++i) {
                sb.append("    ");
            }

            String label = node.parent == null ?
                    "*" :
                    node.isLeftChild() ? "L" : "R";

            sb.append("[")
                .append(label)
                .append(" | ")
                .append(node.cutDimension)
                .append("] ")
                .append(node.key)
                .append(" => ")
                .append(node.value)
                .append("\n");

            treeStringRecursive(node.left, depth + 1, sb);
            treeStringRecursive(node.right, depth + 1, sb);
        }
    }

    /** Get the tree root node.
     * @return tree root node
     */
    protected KDTreeNode<V> getRoot() {
        return root;
    }

    /** Set the tree root.
     * @param root tree root
     */
    protected void setRoot(final KDTreeNode<V> root) {
        this.root = root;
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

    /** Create a new {@link KDTreeNode} for entry into the tree and increment the internal
     * node count.
     * @param parent parent node
     * @param key map key
     * @param depth node depth
     * @return the newly created node
     */
    private KDTreeNode<V> createNode(final KDTreeNode<V> parent, final Vector3D key, final int depth) {
        final KDTreeNode<V> node = new KDTreeNode<>(
                parent,
                key,
                getCutDimensionForDepth(depth));

        ++nodeCount;

        return node;
    }

    /** Find the node in the subtree rooted at {@code node} matching {@code key} or create it if not found.
     * @param node subtree root
     * @param key map key
     * @param depth current node depth
     * @return the existing or newly created node
     */
    private KDTreeNode<V> findOrInsertNodeRecursive(final KDTreeNode<V> node, final Vector3D key, final int depth) {
        // pull the coordinates for the node cut dimension
        final double nodeCoord = node.cutDimension.getCoordinate(node.key);
        final double keyCoord = node.cutDimension.getCoordinate(key);

        // perform an equivalence comparison
        final int eqCmp = precision.compare(keyCoord, nodeCoord);

        final int childDepth = depth + 1;

        if (eqCmp < 0) {
            // we definitely belong in the left subtree
            if (node.left == null) {
                node.left = createNode(node, key, childDepth);
                return node.left;
            } else {
                return findOrInsertNodeRecursive(node.left, key, childDepth);
            }
        } else if (eqCmp > 0) {
            // we definitely belong in the right subtree
            if (node.right == null) {
                node.right = createNode(node, key, childDepth);
                return node.right;
            } else {
                return findOrInsertNodeRecursive(node.right, key, childDepth);
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
            final int strictCmp = Double.compare(keyCoord, nodeCoord);
            if (strictCmp < 0) {
                // insertion, if needed, would be performed in the left subtree, so
                // check the right subtree first
                final KDTreeNode<V> rightExistingNode = findNodeRecursive(node.right, key);
                if (rightExistingNode != null) {
                    return rightExistingNode;
                }

                if (node.left == null) {
                    node.left = createNode(node, key, childDepth);
                    return node.left;
                }

                return findOrInsertNodeRecursive(node.left, key, childDepth);
            } else {
                // insertion, if needed, would be performed in the right subtree, so
                // check the left subtree first
                final KDTreeNode<V> leftExistingNode = findNodeRecursive(node.left, key);
                if (leftExistingNode != null) {
                    return leftExistingNode;
                }

                if (node.right == null) {
                    node.right = createNode(node, key, childDepth);
                    return node.right;
                }

                return findOrInsertNodeRecursive(node.right, key, childDepth);
            }
        }
    }

    /** Find the node with the given {@code key} or null if not found.
     * @param node subtree root
     * @param key map key
     * @return the node matching the given {@code key} or null if not found
     */
    private KDTreeNode<V> findNodeRecursive(final KDTreeNode<V> node, final Vector3D key) {
        if (node != null) {
            // pull the coordinates for the node cut dimension
            final double nodeCoord = node.cutDimension.getCoordinate(node.key);
            final double keyCoord = node.cutDimension.getCoordinate(key);

            // perform an equivalence comparison
            final int eqcmp = precision.compare(keyCoord, nodeCoord);

            if (eqcmp < 0) {
                return findNodeRecursive(node.left, key);
            } else if (eqcmp > 0) {
                return findNodeRecursive(node.right, key);
            } else {
                // check if we are equivalent to the point for this node
                if (key.eq(node.key, precision)) {
                    return node;
                }

                // Not equivalent; the matching node (if any) could be on either
                // side of the cut so we'll need to search both subtrees.
                final KDTreeNode<V> leftSearchResult = findNodeRecursive(node.left, key);
                if (leftSearchResult != null) {
                    return leftSearchResult;
                }

                return findNodeRecursive(node.right, key);
            }
        }
        return null;
    }

    /** Remove the given node from the tree.
     * @param node node to remove
     */
    private void removeKey(final KDTreeNode<V> node) {
        // find a child node to replace this one
        KDTreeNode<V> replacement = null;
        if (node.right != null) {
            replacement = findMin(node.right, node.cutDimension);
        } else if (node.left != null) {
            replacement = findMin(node.left, node.cutDimension);

            // swap left and right subtrees; the replacement will
            // contain the minimum of the entire subtree
            node.right = node.left;
            node.left = null;
        }

        // perform the replacement
        if (replacement != null) {
            node.key = replacement.key;
            node.value = replacement.value;

            removeKey(replacement);
        } else {
            // leaf node; disconnect from the subtree
            if (GeometryInternalUtils.sameInstance(root, node)) {
                this.root = null;
            }

            if (node.parent != null) {
                if (node.isLeftChild()) {
                    node.parent.left = null;
                } else {
                    node.parent.right = null;
                }
            }
        }
    }

    /** Find the node with the minimum value along the given cut dimension.
     * @param node subtree root
     * @param cutDimension cut dimension to search along
     * @return node with the minimum value along the cut dimension or null if {@code node}
     *      is null
     */
    private KDTreeNode<V> findMin(final KDTreeNode<V> node, final CutDimension cutDimension) {
        if (node != null) {
            if (node.isLeaf()) {
                // leaf node; automatically the min
                return node;
            } else if (node.cutDimension.equals(cutDimension)) {
                // this node splits on the dimensions we're searching for, so we
                // only need to search the left subtree
                if (node.left == null) {
                    return node;
                }
                return findMin(node.left, cutDimension);
            } else {
                // this node doesn't split on our target dimension, so search both subtrees
                // and the current node
                return cutDimensionMin(
                            findMin(node.left, cutDimension),
                            node,
                            findMin(node.right, cutDimension),
                            cutDimension);
            }
        }
        return null;
    }

    /** Return the node containing the minimum value along the given cut dimension. Null
     * nodes are ignored.
     * @param a first node
     * @param b second node
     * @param c third node
     * @param cutDimension search dimension
     * @return minimum node
     */
    private KDTreeNode<V> cutDimensionMin(final KDTreeNode<V> a, final KDTreeNode<V> b, final KDTreeNode<V> c,
            final CutDimension cutDimension) {
        final KDTreeNode<V> tempMin = cutDimensionMin(a, b, cutDimension);
        return cutDimensionMin(tempMin, c, cutDimension);
    }

    /** Return the node containing the minimum value along the given cut dimension. If one
     * argument is null, the other argument is returned.
     * @param a first node
     * @param b second node
     * @param cutDimension search dimension
     * @return minimum node
     */
    private KDTreeNode<V> cutDimensionMin(final KDTreeNode<V> a, final KDTreeNode<V> b,
            final CutDimension cutDimension) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        }

        final double aCoord = cutDimension.getCoordinate(a.key);
        final double bCoord = cutDimension.getCoordinate(b.key);

        return aCoord < bCoord ? a : b;
    }

    /** Get the cut dimension for the given node depth.
     * @param depth node depth
     * @return cut dimension instance
     */
    protected CutDimension getCutDimensionForDepth(final int depth) {
        return cutDimensions[depth % cutDimensions.length];
    }

    /** Class representing a node in a KD tree.
     * @param <V> Value type
     */
    static final class KDTreeNode<V> implements Map.Entry<Vector3D, V> {

        /** Parent node; may be null. */
        KDTreeNode<V> parent;

        /** Map key value. */
        Vector3D key;

        /** Map entry value. */
        V value;

        /** Left child; may be null. */
        KDTreeNode<V> left;

        /** Right child; may be null. */
        KDTreeNode<V> right;

        /** Node cut dimension. */
        CutDimension cutDimension;

        /** Construct a new instance.
         * @param parent parent node; may be null
         * @param key map key
         * @param cutDimension cut dimension
         */
        private KDTreeNode(final KDTreeNode<V> parent, final Vector3D key,
                final CutDimension cutDimension) {
            this.parent = parent;
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
        public V setValue(final V newValue) {
            final V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }

        /** Return true if this node is a leaf node.
         * @return true if this node is a leaf node
         */
        public boolean isLeaf() {
            return left == null && right == null;
        }

        /** Return true if this node is the left child of its parent.
         * @return true if this node is the left child of its
         *      parent
         */
        public boolean isLeftChild() {
            return parent != null && GeometryInternalUtils.sameInstance(parent.left, this);
        }
    }
}
