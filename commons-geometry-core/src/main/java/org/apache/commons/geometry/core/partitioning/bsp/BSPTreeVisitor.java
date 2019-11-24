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
package org.apache.commons.geometry.core.partitioning.bsp;

import java.io.Serializable;

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
    enum Order {

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
    default Order visitOrder(final N internalNode) {
        return Order.NODE_MINUS_PLUS;
    }

    /** Abstract class for {@link BSPTreeVisitor} implementations that base their visit
     * ordering on a target point.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    abstract class TargetPointVisitor<P extends Point<P>, N extends BSPTree.Node<P, N>>
        implements BSPTreeVisitor<P, N>, Serializable {

        /** Serializable UID. */
        private static final long serialVersionUID = 20190504L;

        /** Point serving as the target of the traversal. */
        private final P target;

        /** Simple constructor.
         * @param target the point serving as the target for the tree traversal
         */
        public TargetPointVisitor(final P target) {
            this.target = target;
        }

        /** Get the target point for the tree traversal.
         * @return the target point for the tree traversal
         */
        public P getTarget() {
            return target;
        }
    }

    /** {@link BSPTreeVisitor} base class that orders tree nodes so that nodes closest to the target point are
     * visited first. This is done by choosing {@link Order#MINUS_NODE_PLUS}
     * when the target point lies on the minus side of the node's cut hyperplane and {@link Order#PLUS_NODE_MINUS}
     * when it lies on the plus side. The order {@link Order#MINUS_NODE_PLUS} order is used when
     * the target point lies directly on the node's cut hyerplane and no child node is closer than the other.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    abstract class ClosestFirstVisitor<P extends Point<P>, N extends BSPTree.Node<P, N>>
        extends TargetPointVisitor<P, N> {

        /** Serializable UID. */
        private static final long serialVersionUID = 20190504L;

        /** Simple constructor.
         * @param target the point serving as the target for the traversal
         */
        public ClosestFirstVisitor(final P target) {
            super(target);
        }

        /** {@inheritDoc} */
        @Override
        public Order visitOrder(N node) {
            if (node.getCutHyperplane().offset(getTarget()) > 0.0) {
                return Order.PLUS_NODE_MINUS;
            }
            return Order.MINUS_NODE_PLUS;
        }
    }

    /** {@link BSPTreeVisitor} base class that orders tree nodes so that nodes farthest from the target point
     * are traversed first. This is done by choosing {@link Order#PLUS_NODE_MINUS}
     * when the target point lies on the minus side of the node's cut hyperplane and {@link Order#MINUS_NODE_PLUS}
     * when it lies on the plus side. The order {@link Order#MINUS_NODE_PLUS} order is used when
     * the target point lies directly on the node's cut hyerplane and no child node is closer than the other.
     * @param <P> Point implementation type
     * @param <N> BSP tree node implementation type
     */
    abstract class FarthestFirstVisitor<P extends Point<P>, N extends BSPTree.Node<P, N>>
        extends TargetPointVisitor<P, N> {

        /** Serializable UID. */
        private static final long serialVersionUID = 20190504L;

        /** Simple constructor.
         * @param target the point serving as the target for the traversal
         */
        public FarthestFirstVisitor(final P target) {
            super(target);
        }

        /** {@inheritDoc} */
        @Override
        public Order visitOrder(N node) {
            if (node.getCutHyperplane().offset(getTarget()) < 0.0) {
                return Order.PLUS_NODE_MINUS;
            }
            return Order.MINUS_NODE_PLUS;
        }
    }
}
