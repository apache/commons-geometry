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
package org.apache.commons.geometry.core.partition.bsp;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.partition.bsp.AbstractBSPTree.AbstractNode;

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
public abstract class AbstractBSPTreeMergeSupport<P extends Point<P>, N extends AbstractNode<P, N>>
    extends AbstractBSPTreeSplitSupport<P, N> {

    /** Perform a merge operation with the two input trees and store the result in the output tree. The
     * output tree may be one of the input trees, in which case, the tree is modified in place.
     * @param inputTree1 first input tree
     * @param inputTree2 second input tree
     * @param outputTree output tree
     */
    protected void performMerge(final AbstractBSPTree<P, N> inputTree1, final AbstractBSPTree<P, N> inputTree2,
            final AbstractBSPTree<P, N> outputTree) {
        setOutputTree(outputTree);

        final N root1 = inputTree1.getRoot();
        final N root2 = inputTree2.getRoot();

        final N outputRoot = performMergeRecursive(root1, root2);
        getOutputTree().setRoot(outputRoot);
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
            outputNode.setSubtree(node1.getCut(), minus, plus);

            return outputNode;
        }
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
