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
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;

/** Interface for Binary Space Partitioning (BSP) trees. BSP trees are spatial data
 * structures that recursively subdivide a space using hyperplanes. They can be used
 * for a wide variety of purposes, such as representing arbitrary polytopes, storing
 * data for fast spatial lookups, determining the correct rendering order for objects
 * in a 3D scene, and so on.
 *
 * <p>This interface contains a number of methods for extracting information from existing
 * trees, but it does not include methods for constructing trees or modifying tree structure.
 * This is due to the large number of possible use cases for BSP trees. Each use case is likely
 * to have its own specific methods and rules for tree construction, making it difficult to define
 * a single API at this level. Thus, it is left to implementation classes to define their own
 * API for tree construction and modification.</p>
 *
 * @param <P> Point implementation type
 * @param <N> Node implementation type
 * @see <a href="https://en.wikipedia.org/wiki/Binary_space_partitioning">Binary space partitioning</a>
 */
public interface BSPTree<P extends Point<P>, N extends BSPTree.Node<P, N>>
    extends BSPSubtree<P, N> {

    /** Enum specifying possible behaviors when a point used to locate a node
     * falls directly on the cut subhyperplane of an internal node.
     */
    enum FindNodeCutRule {

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
     * cut is chosen. This is equivalent to {@code subtree.findNode(pt, FindNodeCutRule.MINUS)}
     * and always returns a leaf node.
     * @param pt test point used to locate a node in the tree
     * @return leaf node containing the point on its interior or boundary
     * @see #findNode(Point, FindNodeCutRule)
     */
    default N findNode(P pt) {
        return findNode(pt, FindNodeCutRule.MINUS);
    }

    /** Find a node in this subtree containing the given point on it interior or boundary. The
     * search should always return a leaf node except in the cases where the given point lies
     * exactly on the cut subhyperplane of an internal node. In this case, it is unclear whether
     * the search should continue with the minus child, the plus child, or end with the internal
     * node. The {@code cutRule} argument specifies what should happen in this case.
     * <ul>
     *      <li>{@link FindNodeCutRule#MINUS} - continue the search in the minus subtree</li>
     *      <li>{@link FindNodeCutRule#PLUS} - continue the search in the plus subtree</li>
     *      <li>{@link FindNodeCutRule#NODE} - stop the search and return the internal node</li>
     * </ul>
     * @param pt test point used to locate a node in the tree
     * @param cutRule value used to determine the search behavior when the test point lies
     *      exactly on the cut subhyperplane of an internal node
     * @return node containing the point on its interior or boundary
     * @see #findNode(Point)
     */
    N findNode(P pt, FindNodeCutRule cutRule);

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

        /** Trim the given subhyperplane to the region defined by this node by cutting
         * the argument with the cut hyperplanes (binary partitioners) of all parent nodes
         * up to the root. Null is returned if the subhyperplane lies outside of the region
         * defined by the node.
         * @param sub the convex subhyperplane to trim
         * @return the trimmed convex subhyperplane or null if no part of the argument lies
         *      within the node's region
         */
        ConvexSubHyperplane<P> trim(ConvexSubHyperplane<P> sub);
    }
}
