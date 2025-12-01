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
package org.apache.commons.geometry.core.partitioning.bsp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree.SubtreeInitializer;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree.AbstractRegionNode;

/** Class encapsulating logic for building regions by inserting boundaries into a BSP
 * tree containing structural cuts, i.e. cuts where both sides of the cut have the same region
 * location. This technique only produces accurate results when the inserted boundaries define
 * the entire surface of the region. However, for valid input boundaries, significant performance
 * improvements can be achieved due to the reduced height of the tree, especially where large
 * numbers of boundaries are involved and/or the defined region is convex.
 *
 * <h2>Implementation Notes</h2>
 *
 * <p>This class constructs regions in two phases: (1) <em>partition insertion</em> and (2) <em>boundary insertion</em>.
 * Instances begin in the <em>partition insertion</em> phase. Here, partitions can be inserted into the empty tree
 * using the standard BSP insertion logic. The {@link RegionCutRule#INHERIT INHERIT} cut rule is used so that the
 * represented region remains empty even as partitions are inserted.
 * </p>
 *
 * <p>The instance moves into the <em>boundary insertion</em> phase when the caller inserts the first region boundary.
 * Attempting to insert a partition after this point results in an {@code IllegalStateException}. This ensures that
 * partitioning cuts are always located higher up the tree than boundary cuts.</p>
 *
 * <p>After all boundaries are inserted, the tree undergoes final processing to ensure that the region is consistent
 * and that unnecessary nodes are removed.</p>
 *
 * <p>This class does not expose any public methods so that subclasses can present their own
 * public API, tailored to the specific types being worked with. In particular, most subclasses
 * will want to restrict the tree types used with the algorithm, which is difficult to implement
 * cleanly at this level.</p>
 * @param <P> Point implementation type
 * @param <N> BSP tree node implementation type
 */
public abstract class AbstractPartitionedRegionBuilder<
    P extends Point<P>,
    N extends AbstractRegionNode<P, N>> {

    /** Comparator for sorting nodes with the deepest nodes first. */
    private static final Comparator<BSPTree.Node<?, ?>> DEEPEST_FIRST_ORDER =
        (a, b) -> Integer.compare(b.depth(), a.depth());

    /** Tree being constructed. */
    private final AbstractRegionBSPTree<P, N> tree;

    /** Subtree initializer for inserted boundaries. */
    private final SubtreeInitializer<N> subtreeInit;

    /** Flag indicating whether or not partitions may still be inserted into the tree. */
    private boolean insertingPartitions = true;

    /** Set of all internal nodes used as partitioning nodes. */
    private final Set<N> partitionNodes = new HashSet<>();

    /** Construct a new instance that builds a partitioned region in the given tree. The tree must
     * be empty.
     * @param tree tree to build the region in; must be empty
     * @throws IllegalArgumentException if the tree is not empty
     */
    protected AbstractPartitionedRegionBuilder(final AbstractRegionBSPTree<P, N> tree) {
        this(checkTree(tree), false);
    }

    /**
     * Private constructor.
     * @param tree tree to build the region in; must be empty
     * @param ignored Ignored value.
     */
    private AbstractPartitionedRegionBuilder(final AbstractRegionBSPTree<P, N> tree, boolean ignored) {
        this.tree = tree;
        this.subtreeInit = tree.getSubtreeInitializer(RegionCutRule.MINUS_INSIDE);
    }

    /**
     * Check the tree is empty.
     *
     * <p>This method exists to raise an exception before invocation of the
     * private constructor; this mitigates Finalizer attacks
     * (see SpotBugs CT_CONSTRUCTOR_THROW).
     *
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     * @param tree tree to build the region in; must be empty
     * @return the tree
     * @throws IllegalArgumentException if the tree is not empty
     */
    private static <P extends Point<P>, N extends AbstractRegionNode<P, N>>
            AbstractRegionBSPTree<P, N> checkTree(final AbstractRegionBSPTree<P, N> tree) {
        if (!tree.isEmpty()) {
            throw new IllegalArgumentException("Tree must be empty");
        }
        return tree;
    }

    /** Internal method to build and return the tree representing the final partitioned region.
     * @return the partitioned region
     */
    protected AbstractRegionBSPTree<P, N> buildInternal() {
        // condense to combine homogenous leaf nodes
        tree.condense();

        // propagate region interiors to partitioned nodes that have not received
        // a boundary
        if (propagateRegionInterior()) {
            // condense again since some leaf nodes changed
            tree.condense();
        }

        return tree;
    }

    /** Internal method to insert a partition into the tree.
     * @param partition partition to insert
     * @throws IllegalStateException if a boundary has previously been inserted
     */
    protected void insertPartitionInternal(final HyperplaneConvexSubset<P> partition) {
        ensureInsertingPartitions();

        tree.insert(partition, RegionCutRule.INHERIT);
    }

    /** Internal method to insert a region boundary into the tree.
     * @param boundary boundary to insert
     */
    protected void insertBoundaryInternal(final HyperplaneConvexSubset<P> boundary) {
        if (insertingPartitions) {
            // switch to inserting boundaries; place all current internal nodes into
            // a set for easy identification
            for (final N node : tree.nodes()) {
                if (node.isInternal()) {
                    partitionNodes.add(node);
                }
            }

            insertingPartitions = false;
        }

        insertBoundaryRecursive(tree.getRoot(), boundary, boundary.getHyperplane().span(),
            (leaf, cut) -> tree.setNodeCut(leaf, cut, subtreeInit));
    }

    /** Insert a region boundary into the tree.
     * @param node node to insert into
     * @param insert the hyperplane convex subset to insert
     * @param trimmed version of the hyperplane convex subset filling the entire space of {@code node}
     * @param leafFn function to apply to leaf nodes
     */
    private void insertBoundaryRecursive(final N node, final HyperplaneConvexSubset<P> insert,
            final HyperplaneConvexSubset<P> trimmed, final BiConsumer<N, HyperplaneConvexSubset<P>> leafFn) {
        if (node.isLeaf()) {
            leafFn.accept(node, trimmed);
        } else {
            insertBoundaryRecursiveInternalNode(node, insert, trimmed, leafFn);
        }
    }

    /** Recursive boundary insertion method for internal nodes.
     * @param node node to insert into
     * @param insert the hyperplane convex subset to insert
     * @param trimmed version of the hyperplane convex subset filling the entire space of {@code node}
     * @param leafFn function to apply to leaf nodes
     * @see #insertBoundaryRecursive(AbstractRegionNode, HyperplaneConvexSubset, HyperplaneConvexSubset, BiConsumer)
     */
    private void insertBoundaryRecursiveInternalNode(final N node, final HyperplaneConvexSubset<P> insert,
            final HyperplaneConvexSubset<P> trimmed, final BiConsumer<N, HyperplaneConvexSubset<P>> leafFn) {

        final Split<? extends HyperplaneConvexSubset<P>> insertSplit =
                insert.split(node.getCutHyperplane());

        final HyperplaneConvexSubset<P> minus = insertSplit.getMinus();
        final HyperplaneConvexSubset<P> plus = insertSplit.getPlus();

        if (minus == null && plus == null && isPartitionNode(node)) {
            // the inserted boundary lies directly on a partition; proceed down the tree with the
            // rest of the insertion algorithm but instead of cutting the final leaf nodes, just
            // set the location

            // remove this node from the set of partition nodes since this is now a boundary cut
            partitionNodes.remove(node);

            final boolean sameOrientation = node.getCutHyperplane().similarOrientation(insert.getHyperplane());
            final N insertMinus = sameOrientation ? node.getMinus() : node.getPlus();
            final N insertPlus = sameOrientation ? node.getPlus() : node.getMinus();

            insertBoundaryRecursive(insertMinus, insert, trimmed,
                (leaf, cut) -> leaf.setLocation(RegionLocation.INSIDE));

            insertBoundaryRecursive(insertPlus, insert, trimmed,
                (leaf, cut) -> leaf.setLocation(RegionLocation.OUTSIDE));

        } else if (minus != null || plus != null) {
            final Split<? extends HyperplaneConvexSubset<P>> trimmedSplit =
                    trimmed.split(node.getCutHyperplane());

            final HyperplaneConvexSubset<P> trimmedMinus = trimmedSplit.getMinus();
            final HyperplaneConvexSubset<P> trimmedPlus = trimmedSplit.getPlus();

            if (minus != null) {
                insertBoundaryRecursive(node.getMinus(), minus, trimmedMinus, leafFn);
            }
            if (plus != null) {
                insertBoundaryRecursive(node.getPlus(), plus, trimmedPlus, leafFn);
            }
        }
    }

    /** Propagate the region interior to partitioned leaf nodes that have not had a boundary
     * inserted.
     * @return true if any nodes were changed
     */
    private boolean propagateRegionInterior() {
        final List<N> outsidePartitionedLeaves = getOutsidePartitionedLeaves();
        outsidePartitionedLeaves.sort(DEEPEST_FIRST_ORDER);

        int changeCount = 0;

        N parent;
        N sibling;
        for (final N leaf : outsidePartitionedLeaves) {
            parent = leaf.getParent();

            // check if the parent cut touches the inside anywhere on the side opposite of
            // this leaf; if so, then this node should also be inside
            sibling = leaf.isMinus() ?
                    parent.getPlus() :
                    parent.getMinus();

            if (touchesInside(parent.getCut(), sibling)) {
                leaf.setLocation(RegionLocation.INSIDE);

                ++changeCount;
            }
        }

        return changeCount > 0;
    }

    /** Return a list containing all outside leaf nodes that have a parent marked as a partition node.
     * @return a list containing all outside leaf nodes that have a parent marked as a partition node
     */
    private List<N> getOutsidePartitionedLeaves() {
        final List<N> result = new ArrayList<>();

        final N root = tree.getRoot();
        collectOutsidePartitionedLeavesRecursive(root, false, result);

        return result;
    }

   /** Recursively collect all outside leaf nodes that have a parent marked as a partition node.
    * @param node root of the subtree to collect nodes from
    * @param parentIsPartitionNode true if the parent of {@code node} is a partition node
    * @param result list of accumulated results
    */
    private void collectOutsidePartitionedLeavesRecursive(final N node, final boolean parentIsPartitionNode,
            final List<N> result) {
        if (node != null) {
            if (parentIsPartitionNode && node.isOutside()) {
                result.add(node);
            }

            final boolean partitionNode = isPartitionNode(node);

            collectOutsidePartitionedLeavesRecursive(node.getMinus(), partitionNode, result);
            collectOutsidePartitionedLeavesRecursive(node.getPlus(), partitionNode, result);
        }
    }

    /** Return true if {@code sub} touches an inside leaf node anywhere in the subtree rooted at {@code node}.
     * @param sub convex subset to check
     * @param node root node of the subtree to test against
     * @return true if {@code sub} touches an inside leaf node anywhere in the subtree rooted at {@code node}
     */
    private boolean touchesInside(final HyperplaneConvexSubset<P> sub, final N node) {
        if (sub != null) {
            if (node.isLeaf()) {
                return node.isInside();
            } else {
                final Split<? extends HyperplaneConvexSubset<P>> split = sub.split(node.getCutHyperplane());

                return touchesInside(split.getMinus(), node.getMinus()) ||
                        touchesInside(split.getPlus(), node.getPlus());

            }
        }

        return false;
    }

    /** Return true if the given node is marked as a partition node.
     * @param node node to check
     * @return true if the given node is marked as a partition node
     */
    private boolean isPartitionNode(final N node) {
        return partitionNodes.contains(node);
    }

    /** Throw an exception if the instance is no longer accepting partitions.
     * @throws IllegalStateException if the instance is no longer accepting partitions
     */
    private void ensureInsertingPartitions() {
        if (!insertingPartitions) {
            throw new IllegalStateException("Cannot insert partitions after boundaries have been inserted");
        }
    }
}
