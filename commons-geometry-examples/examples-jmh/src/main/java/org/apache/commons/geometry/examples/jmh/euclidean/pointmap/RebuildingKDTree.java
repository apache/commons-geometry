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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision.DoubleEquivalence;

/** {@link KDTree} subclass that rebuilds the tree periodically in an
 * attempt to restrict the overall height of the tree.
 * @param <V> Value type
 */
public class RebuildingKDTree<V> extends KDTree<V> {

    /** Default rebuild maximum. */
    private static final int DEFAULT_REBUILD_MAX = 16;

    /** Maximum size of the tree before it is rebuilt. */
    private int rebuildMax = DEFAULT_REBUILD_MAX;

    /** Minimum size of the tree before it is rebuilt. */
    private int rebuildMin;

    /** Construct a new instance.
     * @param precision precision context
     */
    public RebuildingKDTree(final DoubleEquivalence precision) {
        super(precision);
    }

    /** {@inheritDoc} */
    @Override
    public V put(final Vector3D key, final V value) {
        final V result = super.put(key, value);

        if (size() >= rebuildMax) {
            rebuild();
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public V remove(final Object key) {
        final V result = super.remove(key);

        if (size() <= rebuildMin) {
            rebuild();
        }

        return result;
    }

    /**
     * Rebuild the tree, attempting to reduce the tree depth.
     */
    public void rebuild() {
        int n = size();
        if (n > 0) {
            // construct an array list containing all of the tree nodes
            final List<KDTreeNode<V>> nodes = collectNodes();

            // rebuild recursively and set the new root
            final KDTreeNode<V> newRoot = rebuildRecursive(nodes, 0, n, 0);

            setRoot(newRoot);
        }

        rebuildMax = Math.max(DEFAULT_REBUILD_MAX, 2 * n);
        rebuildMin = n / 2;
    }

    /** Get a list containing all nodes in the tree. The node connections are all cleared.
     * @return list containing all nodes in the tree
     */
    protected List<KDTreeNode<V>> collectNodes() {
        final List<KDTreeNode<V>> nodes = new ArrayList<>(size());
        collectNodesRecursive(getRoot(), nodes);

        return nodes;
    }

    /** Add nodes in the subtree rooted at {@code curr} to {@code nodes}. The node connection
     * references are all cleared.
     * @param curr subtree root node
     * @param nodes node list
     */
    protected void collectNodesRecursive(final KDTreeNode<V> curr, final List<KDTreeNode<V>> nodes) {
        if (curr != null) {
            collectNodesRecursive(curr.left, nodes);
            nodes.add(curr);
            collectNodesRecursive(curr.right, nodes);

            curr.parent = null;
            curr.left = null;
            curr.right = null;
        }
    }

    /** Recursively rebuild the tree using the specified node sublist.
     * @param nodes node list
     * @param startIdx sub list start index (inclusive)
     * @param endIdx sub list end index (exclusive)
     * @param depth node depth
     * @return the root of the subtree containing the nodes between {@code startIdx} and {@code endIdx}
     */
    protected KDTreeNode<V> rebuildRecursive(final List<KDTreeNode<V>> nodes, final int startIdx, final int endIdx,
            final int depth) {
        final CutDimension cutDimension = getCutDimensionForDepth(depth);

        final KDTreeNode<V> node;
        if ((endIdx - startIdx) < 2) {
            // only a single node here
            node = nodes.get(startIdx);
        } else {
            final int splitIdx = partition(nodes, startIdx, endIdx, cutDimension);

            node = nodes.get(splitIdx);

            if (startIdx < splitIdx) {
                node.left = rebuildRecursive(nodes, startIdx, splitIdx, depth + 1);
                node.left.parent = node;
            }

            if (splitIdx < endIdx - 1) {
                node.right = rebuildRecursive(nodes, splitIdx + 1, endIdx, depth + 1);
                node.right.parent = node;
            }
        }

        node.cutDimension = cutDimension;

        return node;
    }

    /** Partition the given sublist into values below the median and values above. The
     * index of the median is returned.
     * @param nodes node list
     * @param startIdx start index (inclusive) of the sublist to partition
     * @param endIdx end index (exclusive) of the sublist to partition
     * @param cutDimension cut dimension
     * @return index of the sublist median
     */
    protected int partition(final List<KDTreeNode<V>> nodes, final int startIdx, final int endIdx,
            final CutDimension cutDimension) {
        if (startIdx < endIdx - 1) {
            final List<KDTreeNode<V>> sublist = nodes.subList(startIdx, endIdx);
            final Comparator<KDTreeNode<V>> comp = comparator(cutDimension);

            sublist.sort(comp);

            int medianIdx = startIdx + ((endIdx - startIdx) / 2);

            final double medianValue = cutDimension.getCoordinate(nodes.get(medianIdx).key);

            // make sure that the median index is the first entry in the list with the
            // median value
            while (medianIdx > startIdx &&
                    cutDimension.getCoordinate(nodes.get(medianIdx - 1).key) >= medianValue) {
                --medianIdx;
            }

            return medianIdx;
        }

        return startIdx;
    }

    /** Construct a comparator the sorts by the given cut dimension.
     * @param cutDimension cut dimension to sort by
     * @return comparator along the cut dimension
     */
    protected Comparator<KDTreeNode<V>> comparator(final CutDimension cutDimension) {
        return (a, b) -> Double.compare(cutDimension.getCoordinate(a.key), cutDimension.getCoordinate(b.key));
    }
}
