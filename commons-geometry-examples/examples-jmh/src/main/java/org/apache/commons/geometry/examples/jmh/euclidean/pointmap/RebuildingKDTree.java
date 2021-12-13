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
        int n = endIdx - startIdx;
        if (n < 2) {
            return startIdx;
        } else if (n == 2) {
            final int bIdx = endIdx - 1;

            final double a = cutDimension.getCoordinate(nodes.get(startIdx).key);
            final double b = cutDimension.getCoordinate(nodes.get(bIdx).key);
            if (a <= b) {
                return startIdx;
            } else {
                swap(nodes, startIdx, bIdx);
                return bIdx;
            }
        } else {
            return findMedianStart(nodes, startIdx, endIdx, cutDimension);
        }
    }

    /** Find the starting index of the node median value in the given list. The list is
     * partially sorted and value less than the median come before the returned index while
     * values greater than or equal come after.
     * @param nodes list of node
     * @param startIdx sublist start index (inclusive)
     * @param endIdx sublist end index (exclusive)
     * @param cutDimension cut dimension
     * @return index of the median in the specific sublist of {@code nodes} along the cut dimension
     */
    protected int findMedianStart(final List<KDTreeNode<V>> nodes, final int startIdx, final int endIdx,
            final CutDimension cutDimension) {
        int k = startIdx + ((endIdx - startIdx) / 2);
        int low = startIdx;
        int high = endIdx - 1;
        int lowTemp;
        int highTemp;
        double x;
        while (low < high) {
            x = cutDimension.getCoordinate(nodes.get(k).key);
            lowTemp = low;
            highTemp = high;
            do {
                while (cutDimension.getCoordinate(nodes.get(lowTemp).key) < x) {
                    ++lowTemp;
                }
                while (cutDimension.getCoordinate(nodes.get(highTemp).key) > x) {
                    --highTemp;
                }

                if (lowTemp <= highTemp) {
                    swap(nodes, lowTemp, highTemp);

                    ++lowTemp;
                    --highTemp;
                }
            } while (lowTemp <= highTemp);

            if (k < lowTemp) {
                // search low part
                high = highTemp;
            }
            if (k > highTemp) {
                // search high part
                low = lowTemp;
            }
        }

        // back up to the start of the median value
        x = cutDimension.getCoordinate(nodes.get(k).key);
        while (k > startIdx &&
                cutDimension.getCoordinate(nodes.get(k - 1).key) == x) {
            --k;
        }

        return k;
    }

    /** Construct a comparator the sorts by the given cut dimension.
     * @param cutDimension cut dimension to sort by
     * @return comparator along the cut dimension
     */
    protected Comparator<KDTreeNode<V>> comparator(final CutDimension cutDimension) {
        return (a, b) -> Double.compare(cutDimension.getCoordinate(a.key), cutDimension.getCoordinate(b.key));
    }

    /** Swap two elements in {@code list}.
     * @param <T> List element type
     * @param list input list
     * @param aIdx index of the first element
     * @param bIdx index of the second element
     */
    private static <T> void swap(final List<T> list, final int aIdx, final int bIdx) {
        final T temp = list.get(aIdx);
        list.set(aIdx, list.get(bIdx));
        list.set(bIdx, temp);
    }
}
