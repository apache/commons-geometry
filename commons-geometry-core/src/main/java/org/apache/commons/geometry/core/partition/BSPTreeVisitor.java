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

/** Interface for visiting the nodes in a {@link BSPTree} or {@link BSPSubtree}.
 * @param <P> Point implementation type
 * @param <N> BSP tree node implementation type
 */
@FunctionalInterface
public interface BSPTreeVisitor<P extends Point<P>, N extends BSPTree.Node<P, N>> {

    /** Enum used to specify the order in which visitors should visit the nodes
     * in the tree.
     */
    enum NodeVisitOrder {

        /** Indicates that the visitor should first visit the plus sub-tree, then
         * the minus sub-tree and then the current node.
         */
        PLUS_MINUS_NODE,

        /** Indicates that the visitor should first visit the plus sub-tree, then
         * the current node, and then the minus sub-tree.
         */
        PLUS_NODE_MINUS,

        /** Indicates that the visitor should first visit the minus sub-tree, then
         * the plus sub-tree, and then the current node.
         */
        MINUS_PLUS_NODE,

        /** Indicates that the visitor should first visit the minus sub-tree, then the
         * current node, and then the plus sub-tree.
         */
        MINUS_NODE_PLUS,

        /** Indicates that the visitor should first visit the current node, then the
         * plus sub-tree, and then the minus sub-tree.
         */
        NODE_PLUS_MINUS,

        /** Indicates that the visitor should first visit the current node, then the
         * minus sub-tree, and then the plus sub-tree.
         */
        NODE_MINUS_PLUS;
    }

    /** Visit a node in a BSP tree. This method is called for both internal nodes and
     * leaf nodes.
     * @param node the node being visited
     */
    void visit(N node);

    /** Determine the visit order for the given internal node. This is called for each
     * internal node before {@link #visit(BSPTree.Node)} is called. Returning null from
     * this method skips the subtree rooted at the given node. This method is not called
     * on leaf nodes.
     * @param internalNode the internal node to determine the visit order for
     * @return the order that the subtree rooted at the given node should be visited
     */
    default NodeVisitOrder visitOrder(final N internalNode) {
        return NodeVisitOrder.NODE_MINUS_PLUS;
    }
}
