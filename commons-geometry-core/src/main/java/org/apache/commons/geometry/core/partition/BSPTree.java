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
 * @param <N> Node type
 */
public interface BSPTree<P extends Point<P>, N extends BSPTree.Node<P, N>> {

    /** Get the root node of the tree.
     * @return the root node of the tree
     */
    N getRoot();

    /** Return the node representing the smallest cell that contains the given point.
     * If the point lies directly on the cut subhyperplane of an internal node, then
     * that internal node is returned. Otherwise, a leaf node is returned.
     * @param pt point to check
     * @return the node containing the point
     */
    N findNode(P pt);

    /** Insert a subhyperplane into the tree.
     * @param sub the subhyperplane to insert into the tree
     */
    void insert(SubHyperplane<P> sub);

    /** Insert a convex subhyperplane into the tree.
     * @param convexSub the convex subhyperplane to insert into the tree
     */
    void insert(ConvexSubHyperplane<P> convexSub);

    /** Insert a set of convex subhyperplanes into the tree.
     * @param convexSubs iterable containing a collection of subhyperplanes
     *      to insert into the tree
     */
    void insert(Iterable<ConvexSubHyperplane<P>> convexSubs);

    /** Call the given {@link BSPTreeVisitor} with each node from the
     * tree.
     * @param visitor visitor call with each tree node
     */
    void visit(BSPTreeVisitor<P, N> visitor);

    /** Return the total number of nodes in the tree.
     * @return
     */
    int count();

    /** Return an iterable for iterating through the nodes of the tree.
     * @return an iterable for iterating through the nodes of the tree
     */
    Iterable<N> nodes();

    /** Return an iterable for iterating through the leaf nodes of the tree.
     * @return an iterable for iterating through the leaf nodes of the tree
     */
    Iterable<N> leafNodes();

    /** Return an iterable for iterating through the cut (internal) nodes of the tree.
     * @return an iterable for iterating through the cut nodes of the tree
     */
    Iterable<N> cutNodes();

    /** Return a deep copy of this instance.
     * @return a deep copy of this instance
     */
    BSPTree<P, N> copy();

    /** Interface for Binary Space Partitioning (BSP) tree nodes.
     * @param <P> Point type
     */
    public static interface Node<P extends Point<P>, N extends Node<P, N>> {

        /** Get the {@link BSPTree} that owns the node.
         * @return the owning tree
         */
        BSPTree<P, N> getTree();

        /** Get the depth of the node in the tree. The root node of the tree
         * has a depth of 0.
         * @return the depth of the node in the tree
         */
        int depth();

        /** Return the total number of nodes in the child tree rooted at this
         * node. The count includes the current node.
         * @return
         */
        int count();

        /** Get the parent of the node. This will be null if the node is the
         * root of the tree.
         * @return the parent node for this instance
         */
        N getParent();

        /** Get the cut for the node. This is a convex subhyperplane that splits
         * the region for the cell into two disjoint regions, namely the plus and
         * minus regions. This will be null for leaf nodes.
         * @see #getPlus()
         * @see #getMinus()
         * @return the cut subhyperplane for the cell
         */
        ConvexSubHyperplane<P> getCut();

        /** Get the node for the minus region of the cell. This will be null if the
         * node has not been cut, ie if it is a leaf node.
         * @return the node for the minus region of the cell
         */
        N getMinus();

        /** Get the node for the plus region of the cell. This will be null if the
         * node has not been cut, ie if it is a leaf node.
         * @return the node for the plus region of the cell
         */
        N getPlus();

        /** Return true if the node is a leaf node, meaning that it has no
         * binary partitioner (aka, cut) and therefore no child nodes.
         * @return true if the node is a leaf node
         */
        boolean isLeaf();

        /** Return true if the node has a parent and is the parent's minus
         * child.
         * @return true if the node is the minus child of its parent
         */
        boolean isMinus();

        /** Return true if the node has a parent and is the parent's plus
         * child.
         * @return true if the node is the plus child of its parent
         */
        boolean isPlus();

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

        /** Remove the cut from this node. Returns true if the node previously had a cut.
         * @return true if the node had a cut before the call to this method
         */
        boolean clearCut();

        /** Cut this node with the given hyperplane. The same node is returned, regardless of
         * the outcome of the cut operation. If the operation succeeded, then the node will
         * have plus and minus child nodes.
         * @param cutter the hyperplane to cut the node's region with
         * @return this node
         * @see #insertCut(Hyperplane)
         */
        N cut(Hyperplane<P> cutter);
    }
}
