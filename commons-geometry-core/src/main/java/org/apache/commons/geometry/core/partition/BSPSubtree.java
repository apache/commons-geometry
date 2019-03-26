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

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.geometry.core.Point;

/** Interface for types that form the root of BSP subtrees. This includes trees
 * themselves as well as each node in a tree.
 * @param <P> Point implementation type
 * @param <N> Node implementation type
 */
public interface BSPSubtree<P extends Point<P>, N extends BSPTree.Node<P, N>> extends Iterable<N> {

    /** Return the total number of nodes in the subtree.
     * @return
     */
    int count();

    /** The height of the subtree, ie the length of the longest downward path from
     * the subtree root to a leaf node.
     * @return the height of the subtree.
     */
    int height();

    /** Call the given {@link BSPTreeVisitor} with each node from the
     * subtree.
     * @param visitor visitor call with each subtree node
     */
    void visit(BSPTreeVisitor<P, N> visitor);

    /** Create a stream over the nodes in this subtree.
     * @return a stream for accessing the nodes in this subtree
     */
    default Stream<N> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
