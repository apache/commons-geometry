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

public abstract class AbstractBSPTreeOutputSupport<P extends Point<P>, N extends AbstractNode<P, N>> {

    /** The tree that the merge operation output will be written to. All existing content
     * is this tree is overwritten.
     */
    private AbstractBSPTree<P, N> outputTree;

    protected void setOutputTree(final AbstractBSPTree<P, N> outputTree) {
        this.outputTree = outputTree;
    }

    protected AbstractBSPTree<P, N> getOutputTree() {
        return outputTree;
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
            outputTree.copySubtree(node, outputNode);

            return outputNode;
        }

        return node;
    }
}
