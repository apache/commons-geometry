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

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.BoundarySource;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;

/** Interface for Binary Space Partitioning (BSP) trees.
 * @param <P> Point implementation type
 * @param <N> Node implementation type
 */
public interface BSPTree<P extends Point<P>, N extends BSPTree.Node<P, N>>
    extends BSPSubtree<P, N> {

    /** Enum specifying possible behaviors when a point used to locate a node
     * falls directly on the cut subhyperplane of an internal node.
     */
    enum NodeCutRule {

        /** Choose the minus child of the internal node and continue searching.
         * This behavior will result in a leaf node always being returned by the
         * node search.
         */
        MINUS,

        /** Choose the plus child of the internal node and continue searching.
         * This behavior will result in a leaf node always being returned by the
         * node search.
         */
        PLUS,

        /** Choose the internal node and stop searching. This behavior may result
         * in non-leaf nodes being returned by the node search.
         */
        NODE
    }

    /** Get the root node of the tree.
     * @return the root node of the tree
     */
    N getRoot();

    /** Find a node in this subtree containing the given point or its interior or boundary.
     * When a point lies directly on the cut of an internal node, the minus child of the
     * cut is chosen. This is equivalent to {@code subtree.findNode(pt, NodeCutRule.MINUS)}
     * and always returns a leaf node.
     * @param pt test point used to locate a node in the tree
     * @return leaf node containing the point on its interior or boundary
     * @see #findNode(Point, NodeCutRule)
     */
    default N findNode(P pt) {
        return findNode(pt, NodeCutRule.MINUS);
    }

    /** Find a node in this subtree containing the given point on it interior or boundary. The
     * search should always return a leaf node except in the cases where the given point lies
     * exactly on the cut subhyperplane of an internal node. In this case, it is unclear whether
     * the search should continue with the minus child, the plus child, or end with the internal
     * node. The {@code cutRule} argument specifies what should happen in this case.
     * <ul>
     *      <li>{@link NodeCutRule#MINUS} - continue the search in the minus subtree</li>
     *      <li>{@link NodeCutRule#PLUS} - continue the search in the plus subtree</li>
     *      <li>{@link NodeCutRule#NODE} - stop the search and return the internal node</li>
     * </ul>
     * @param pt test point used to locate a node in the tree
     * @param cutRule value used to determine the search behavior when the test point lies
     *      exactly on the cut subhyperplane of an internal node
     * @return node containing the point on its interior or boundary
     * @see #findNode(Point)
     */
    N findNode(P pt, NodeCutRule cutRule);

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
    void insert(Iterable<? extends ConvexSubHyperplane<P>> convexSubs);

    /** Insert all convex subhyperplanes from the given source into the tree.
     * @param boundarySrc source of boundary convex subhyperplanes to insert
     *      into the tree
     */
    void insert(BoundarySource<? extends ConvexSubHyperplane<P>> boundarySrc);

    /** Make the current instance a deep copy of the argument.
     * @param src the tree to copy
     */
    void copy(BSPTree<P, N> src);

    /** Set this instance to the region represented by the given node. The
     * given node could have come from this tree or a different tree.
     * @param node the node to extract
     */
    void extract(N node);

    /** Transform this tree. Each cut subhyperplane in the tree is transformed in place using
     * the given {@link Transform}.
     * @param transform the transform to apply
     */
    void transform(Transform<P> transform);

    /** Interface for Binary Space Partitioning (BSP) tree nodes.
     * @param <P> Point type
     * @param <N> BSP tree node implementation type
     */
    interface Node<P extends Point<P>, N extends Node<P, N>> extends BSPSubtree<P, N> {

        /** Get the {@link BSPTree} that owns the node.
         * @return the owning tree
         */
        BSPTree<P, N> getTree();

        /** Get the depth of the node in the tree. The root node of the tree
         * has a depth of 0.
         * @return the depth of the node in the tree
         */
        int depth();

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

        /** Get the hyperplane belonging to the node cut, if it exists.
         * @return the hyperplane belonging to the node cut, or null if
         *      the node does not have a cut
         * @see #getCut()
         */
        Hyperplane<P> getCutHyperplane();

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
         * binary partitioner (aka "cut") and therefore no child nodes.
         * @return true if the node is a leaf node
         */
        boolean isLeaf();

        /** Return true if the node is an internal node, meaning that is
         * has a binary partitioner (aka "cut") and therefore two child nodes.
         * @return true if the node is an internal node
         */
        boolean isInternal();

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
         * this node's region, then the node's cut is set to the {@link ConvexSubHyperplane}
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
