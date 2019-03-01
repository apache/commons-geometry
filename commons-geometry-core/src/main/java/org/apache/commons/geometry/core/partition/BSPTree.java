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

/** Interface for Binary Space Partitioning (BSP) trees.
 * @param <P> Point type
 * @param <T> Node attribute type. This is the type of the data stored with
 *      each node in the tree.
 */
public interface BSPTree<P extends Point<P>, T> extends BSPTreeTraversal<P, T> {

    /** Get the root node of the tree.
     * @return the root node of the tree
     */
    Node<P, T> getRoot();

    /** Return the node representing the smallest cell that contains the given point.
     * If the point lies directly on the cut subhyperplane of an internal node, then
     * that internal node is returned. Otherwise, a leaf node is returned.
     * @param pt point to check
     * @return the node containing the point
     */
    Node<P, T> findNode(P pt);

    /** Interface for Binary Space Partitioning (BSP) tree nodes.
     * @param <P> Point type
     * @param <T> Node attribute type. This is the type of the data stored with
     *      each node in the tree.
     */
    public static interface Node<P extends Point<P>, T> extends BSPTreeTraversal<P, T> {

        /** Get the {@link BSPTree} that owns the node.
         * @return the owning tree
         */
        BSPTree<P, T> getTree();

        /** Get the parent of the node. This will be null if the node is the
         * root of the tree.
         * @return the parent node for this instance
         */
        Node<P, T> getParent();

        /** Get the cut for the node. This is a convex subhyperplane that splits
         * the region for the cell into two disjoint regions, namely the plus and
         * minus regions. This will be null for leaf nodes.
         * @see #getPlus()
         * @see #getMinus()
         * @return the cut subhyperplane for the cell
         */
        ConvexSubHyperplane<P> getCut();

        /** Get the node for the plus region of the cell. This will be null if the
         * node has not been cut, ie if it is a leaf node.
         * @return the node for the plus region of the cell
         */
        Node<P, T> getPlus();

        /** Get the node for the minus region of the cell. This will be null if the
         * node has not been cut, ie if it is a leaf node.
         * @return the node for the minus region of the cell
         */
        Node<P, T> getMinus();

        /** Get the attribute associated with this node.
         * @return the attribute associated with this node
         */
        T getAttribute();

        /** Set the attribute associated with this node.
         * @param attribute the attribute to associate with this node
         */
        void setAttribute(T attribute);

        /** Return true if the node is a leaf node, meaning that it has no
         * binary partitioner (aka, cut) and therefore no child nodes.
         * @return true if the node is a leaf node
         */
        boolean isLeaf();

        /** Return true if the node has a parent and is the parent's plus
         * child.
         * @return true if the node is the plus child of its parent
         */
        boolean isPlus();

        /** Return true if the node has a parent and is the parent's minus
         * child.
         * @return true if the node is the minus child of its parent
         */
        boolean isMinus();

        /** Insert a cut into this node. If the given hyperplane intersects
         * this node's region, then the node's cut is set to the {@link ConvexSubHyerplane}
         * representing the intersection, new plus and minus child leaf nodes
         * are assigned, and true is returned. If the hyperplane does not intersect
         * the node's region, then the node's cut and plus and minus child references
         * are all set to null (ie, it becomes a leaf node) and false is returned. In
         * either case, any existing cut and/or child nodes are removed by this method.
         * @param cutter the hyperplane to cut the node's region with
         * @return true if the cutting hyperplane intersected the node's region, resulting
         *      in the creation of new child nodes
         */
        boolean insertCut(Hyperplane<P> cutter);

        /** Cut this node with the given hyperplane. The same node is returned, regardless of
         * the outcome of the cut operation. If the operation succeeded, then the node will
         * have plus and minus child nodes.
         * @param cutter the hyperplane to cut the node's region with
         * @return this node
         * @see #insertCut(Hyperplane)
         */
        Node<P, T> cut(Hyperplane<P> cutter);

        /** Set the attribute for this node. The node is returned.
         * @param attribute attribute to set for the node
         * @return the node instance
         */
        Node<P, T> attr(T attribute);
    }
}
