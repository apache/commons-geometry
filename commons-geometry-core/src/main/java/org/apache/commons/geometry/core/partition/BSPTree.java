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
public interface BSPTree<P extends Point<P>, T> {

    /** Get the root node of the tree.
     * @return the root node of the tree
     */
    Node<P, T> getRoot();

    void visit(BSPTreeVisitor<P, T> visitor);

    Node<P, T> findNode(P pt);

    /** Interface for Binary Space Partitioning (BSP) tree nodes.
     * @param <P> Point type
     * @param <T> Node attribute type. This is the type of the data stored with
     *      each node in the tree.
     */
    public static interface Node<P extends Point<P>, T> {

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

        T getAttribute();

        void setAttribute(T attribute);

        boolean isLeaf();

        boolean isPlus();

        boolean isMinus();

        boolean insertCut(Hyperplane<P> cutter);

        void visit(BSPTreeVisitor<P, T> visitor);

        Node<P, T> findNode(P pt);
    }
}
