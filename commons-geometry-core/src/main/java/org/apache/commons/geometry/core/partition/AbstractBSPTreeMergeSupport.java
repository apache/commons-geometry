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
package org.apache.commons.geometry.core.partition;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.partition.AbstractBSPTree.AbstractNode;

/** Class containing the basic algorithm for merging two {@link AbstractBSPTree}
 * instances. Subclasses must override the {@link #mergeLeaf(AbstractNode, AbstractNode)} method
 * to implement the merging logic for their particular use case. The remainder of the
 * algorithm is independent of the use case.
 *
 * <p>This class does not expose any public methods so that subclasses can present their own
 * public API, tailored to the specific types being worked with. In particular, most subclasses
 * will want to restrict the tree types used with the algorithm, which is difficult to implement
 * cleanly at this level.</p>
 *
 * <p>This class maintains state during the merging process and is therefore
 * not thread-safe.</p>
 */
public abstract class AbstractBSPTreeMergeSupport<P extends Point<P>, N extends AbstractNode<P, N>> {

    /** The tree that the merge operation output will be written to. All existing content
     * is this tree is overwritten at the end of the merge process.
     */
    private AbstractBSPTree<P, N> outputTree;

    /** Perform a merge operation with the two input trees and store the result in the output tree. The
     * output tree may be one of the input trees, in which case, the tree is modified in place.
     * @param inputTree1 first input tree
     * @param inputTree2 second input tree
     * @param outputTree output tree
     */
    protected void performMerge(final AbstractBSPTree<P, N> inputTree1, final AbstractBSPTree<P, N> inputTree2,
            final AbstractBSPTree<P, N> outputTree) {

        this.outputTree = outputTree;

        final N root1 = inputTree1.getRoot();
        final N root2 = inputTree2.getRoot();

        final N outputRoot = performMergeRecursive(root1, root2);
        outputTree.setRoot(outputRoot);
    }

    /** Recursively merge two nodes.
     * @param node1 node from the first input tree
     * @param node2 node from the second input tree
     * @return a merged node
     */
    private N performMergeRecursive(final N node1, final N node2) {

        if (node1.isLeaf() || node2.isLeaf()) {
            // delegate to the mergeLeaf method if we can no longer continue
            // merging recursively
            final N merged = mergeLeaf(node1, node2);

            // copy the merged node to the output if needed (in case mergeLeaf
            // returned one of the input nodes directly)
            return outputSubtree(merged);
        }
        else {
            final N partitioned = splitSubtree(node2, node1.getCut());

            final N minus = performMergeRecursive(node1.getMinus(), partitioned.getMinus());

            final N plus = performMergeRecursive(node1.getPlus(), partitioned.getPlus());

            final N outputNode = outputNode(node1);
            outputNode.setCutState(node1.getCut(), minus, plus);

            return outputNode;
        }
    }

    /** Split the subtree rooted at the given node by a partitioning convex subhyperplane
     * defined on the same region as the node.
     * @param node the root node of the subtree to split; the node may be a leaf node
     * @param partitioner partitioning convex subhyperplane
     * @return node containing the split subtree
     */
    private N splitSubtree(final N node, final ConvexSubHyperplane<P> partitioner) {
        if (node.isLeaf()) {
            return splitSubtreeLeaf(node, partitioner);
        }
        return splitSubtreeCut(node, partitioner);
    }

    /** Split the given leaf node by a partitioning convex subhyperplane defined on the
     * same region.
     * @param node the leaf node to split
     * @param partitioner partitioning convex subhyperplane
     * @return node containing the split subtree
     */
    private N splitSubtreeLeaf(final N node, final ConvexSubHyperplane<P> partitioner) {
        // in this case, we just create a new parent node with the partitioner as its
        // cut and two copies of the original node as children
       final N parent = outputNode();
       parent.setCutState(partitioner, outputNode(node), outputNode(node));

       return parent;
    }

    /** Split the non-singleton subtree rooted at the given node by a partitioning convex subhyperplane
     * defined on the same region as the node.
     * @param node node the root node of the subtree to split; the node may not be a leaf node
     * @param partitioner partitioning convex subhyperplane
     * @return node containing the split subtree
     */
    private N splitSubtreeCut(final N node, final ConvexSubHyperplane<P> partitioner) {
        // split the partitioner and node cut with each other's hyperplanes to determine their relative positions
        final ConvexSubHyperplane.Split<P> partitionerSplit = partitioner.split(node.getCutHyperplane());
        final ConvexSubHyperplane.Split<P> nodeCutSplit = node.getCut().split(partitioner.getHyperplane());

        final SplitLocation partitionerSplitSide = partitionerSplit.getLocation();
        final SplitLocation nodeCutSplitSide = nodeCutSplit.getLocation();

        final N result = outputNode();

        N resultMinus;
        N resultPlus;

        if (partitionerSplitSide == SplitLocation.PLUS) {
            if (nodeCutSplitSide == SplitLocation.PLUS) {
                // partitioner is on node cut plus side, node cut is on partitioner plus side
                final N nodePlusSplit = splitSubtree(node.getPlus(), partitioner);

                resultMinus = nodePlusSplit.getMinus();

                resultPlus = outputNode(node);
                resultPlus.setCutState(node.getCut(), outputSubtree(node.getMinus()), nodePlusSplit.getPlus());
            }
            else {
                // partitioner is on node cut plus side, node cut is on partitioner minus side
                final N nodePlusSplit = splitSubtree(node.getPlus(), partitioner);

                resultMinus = outputNode(node);
                resultMinus.setCutState(node.getCut(), outputSubtree(node.getMinus()), nodePlusSplit.getMinus());

                resultPlus = nodePlusSplit.getPlus();
            }
        }
        else if (partitionerSplitSide == SplitLocation.MINUS) {
            if (nodeCutSplitSide == SplitLocation.MINUS) {
                // partitioner is on node cut minus side, node cut is on partitioner minus side
                final N nodeMinusSplit = splitSubtree(node.getMinus(), partitioner);

                resultMinus = outputNode(node);
                resultMinus.setCutState(node.getCut(), nodeMinusSplit.getMinus(), outputSubtree(node.getPlus()));

                resultPlus = nodeMinusSplit.getPlus();
            }
            else {
                // partitioner is on node cut minus side, node cut is on partitioner plus side
                final N nodeMinusSplit = splitSubtree(node.getMinus(), partitioner);

                resultMinus = nodeMinusSplit.getMinus();

                resultPlus = outputNode(node);
                resultPlus.setCutState(node.getCut(), nodeMinusSplit.getPlus(), outputSubtree(node.getPlus()));
            }
        }
        else if (partitionerSplitSide == SplitLocation.BOTH) {
            // partitioner and node cut split each other
            final N nodeMinusSplit = splitSubtree(node.getMinus(), partitionerSplit.getMinus());
            final N nodePlusSplit = splitSubtree(node.getPlus(), partitionerSplit.getPlus());

            resultMinus = outputNode(node);
            resultMinus.setCutState(nodeCutSplit.getMinus(), nodeMinusSplit.getMinus(), nodePlusSplit.getMinus());

            resultPlus = outputNode(node);
            resultPlus.setCutState(nodeCutSplit.getPlus(), nodeMinusSplit.getPlus(), nodePlusSplit.getPlus());
        }
        else {
            // partitioner and node cut are parallel or anti-parallel
            final boolean sameOrientation = partitioner.getHyperplane().similarOrientation(node.getCutHyperplane());

            resultMinus = outputSubtree(sameOrientation ? node.getMinus() : node.getPlus());
            resultPlus = outputSubtree(sameOrientation ? node.getPlus() : node.getMinus());
        }

        result.setCutState(partitioner, resultMinus, resultPlus);

        return result;
    }

    /** Create a new node in the output tree. The node is associated with the output tree but
     * is not attached to a parent node.
     * @return a new node associated with the output tree but not yet attached to a parent
     */
    protected N outputNode() {
        return outputTree.createNode();
    }

    /** Create a new node in the output tree with the same non-structural properties as the given
     * node. Non-structural properties are properties other than parent, children, or cut. The
     * returned node is associated with the output tree but is not attached to a parent node.
     * Note that this method only copies the given node and <strong>not</strong> any of its children.
     * @param node the input node to copy properties from
     * @return a new node in the output tree
     */
    protected N outputNode(final N node) {
        final N copy = outputNode();
        outputTree.copyNodeProperties(node, copy);

        return copy;
    }

    /** Place the subtree rooted at the given input node into the output tree. The subtree
     * is copied if needed.
     * @param node the root of the subtree to copy
     * @return a subtree in the output tree
     */
    protected N outputSubtree(final N node) {
        // create a copy of the node if it's not already in the output tree
        if (node.getTree() != outputTree) {
            final N outputNode = outputNode();
            outputTree.copyRecursive(node, outputNode);

            return outputNode;
        }

        return node;
    }

    /** Merge a leaf node from one input with a subtree from another.
     * <p>When this method is called, one or both of the given nodes will be a leaf node.
     * This method is expected to return a node representing the merger of the two given
     * nodes. The way that the returned node is determined defines the overall behavior of
     * the merge operation.
     * </p>
     * <p>The return value can be one of the two input nodes or a completely different one.</p>
     * @param node1 node from the first input tree
     * @param node2 node from the second input tree
     * @return node representing the merger of the two input nodes
     */
    protected abstract N mergeLeaf(final N node1, final N node2);
}
