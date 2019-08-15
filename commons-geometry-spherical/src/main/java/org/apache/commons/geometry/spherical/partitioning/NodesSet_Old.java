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
package org.apache.commons.geometry.spherical.partitioning;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.geometry.core.Point;

/** Set of {@link BSPTree_Old BSP tree} nodes.
 * @see BoundaryAttribute_Old
 * @param <P> Point type defining the space
 */
public class NodesSet_Old<P extends Point<P>> implements Iterable<BSPTree_Old<P>> {

    /** List of sub-hyperplanes. */
    private final List<BSPTree_Old<P>> list;

    /** Simple constructor.
     */
    public NodesSet_Old() {
        list = new ArrayList<>();
    }

    /** Add a node if not already known.
     * @param node node to add
     */
    public void add(final BSPTree_Old<P> node) {

        for (final BSPTree_Old<P> existing : list) {
            if (node == existing) {
                // the node is already known, don't add it
                return;
            }
        }

        // the node was not known, add it
        list.add(node);

    }

    /** Add nodes if they are not already known.
     * @param iterator nodes iterator
     */
    public void addAll(final Iterable<BSPTree_Old<P>> iterator) {
        for (final BSPTree_Old<P> node : iterator) {
            add(node);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<BSPTree_Old<P>> iterator() {
        return list.iterator();
    }

}
