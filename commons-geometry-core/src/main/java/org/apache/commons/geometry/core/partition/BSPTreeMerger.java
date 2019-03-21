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

/** Interface used during BSP tree merge operations to how cells are merged.
 */
@FunctionalInterface
public interface BSPTreeMerger<P extends Point<P>, N extends BSPTree.Node<P, N>> {

    /** Merge a node from the target tree instance with a node from the tree being
     * merged in. The return value will replace {@code targetNode} in the target tree.
     * @param targetNode the node in the target tree
     * @param mergeNode the node being merged
     * @return the new node to replace {@code targetNode} in the target tree
     */
    N merge(N targetNode, N mergeNode);
}
