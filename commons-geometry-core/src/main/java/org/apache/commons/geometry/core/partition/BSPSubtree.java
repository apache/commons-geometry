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

import java.util.List;

import org.apache.commons.geometry.core.Point;

/** Interface for types that form the root of BSP subtrees. This includes trees
 * themselves as well as each node in a tree.
 * @param <P> Point implementation type
 * @param <N> Node implementation type
 */
public interface BSPSubtree<P extends Point<P>, N extends BSPTree.Node<P, N>> {

    /** Enum specifying possible behaviors when a point used to locate a node
     * falls directly on the cut subhyperplane of an internal node.
     */
    public static enum FindNodeCutBehavior {

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

    N findNode(P pt);

    N findNode(P pt, FindNodeCutBehavior cutBehavior);

    /** Call the given {@link BSPTreeVisitor} with each node from the
     * subtree.
     * @param visitor visitor call with each subtree node
     */
    void visit(BSPTreeVisitor<P, N> visitor);

    /** Return the total number of nodes in the subtree.
     * @return
     */
    int count();

    /** The height of the subtree, ie the length of the longest downward path from
     * the subtree root to a leaf node.
     * @return the height of the subtree.
     */
    int height();

    /** Return an iterable for iterating through the nodes of the subtree.
     * @return an iterable for iterating through the nodes of the subtree
     */
    Iterable<N> nodes();

    /** Return a list containing all nodes in the subtree.
     * @return list containing all nodes in the subtree
     */
    List<N> nodeList();

    /** Return an iterable for iterating through the leaf nodes of the subtree.
     * @return an iterable for iterating through the leaf nodes of the subtree
     */
    Iterable<N> leafNodes();

    /** Return a list containing all leaf nodes in the subtree.
     * @return list containing all leaf nodes in the subtree
     */
    List<N> leafNodeList();

    /** Return an iterable for iterating through the internal nodes of the subtree.
     * @return an iterable for iterating through the internal nodes of the subtree
     */
    Iterable<N> internalNodes();

    /** Return a list containing all internal nodes in the subtree.
     * @return list containing all internal nodes in the subtree
     */
    List<N> internalNodeList();
}
