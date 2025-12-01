/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.spherical.twod;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.partitioning.BoundarySource;

/** Extension of the {@link BoundarySource} interface for spherical 2D
 * space.
 */
@FunctionalInterface
public interface BoundarySource2S extends BoundarySource<GreatArc> {

    /** Return a {@link BoundaryList2S} containing the boundaries in this instance.
     * @return a {@link BoundaryList2S} containing the boundaries in this instance
     */
    default BoundaryList2S toList() {
        final List<GreatArc> boundaries = boundaryStream()
                .collect(Collectors.toList());

        return new BoundaryList2S(boundaries);
    }

    /** Return a BSP tree constructed from the boundaries contained in this
     * instance. The default implementation creates a new, empty tree
     * and inserts the boundaries from this instance.
     * @return a BSP tree constructed from the boundaries in this instance
     */
    default RegionBSPTree2S toTree() {
        final RegionBSPTree2S tree = RegionBSPTree2S.empty();
        tree.insert(this);

        return tree;
    }

    /** Return a {@link BoundarySource2S} instance containing the given boundaries.
     * @param boundaries boundaries to include in the boundary source
     * @return a boundary source containing the given boundaries
     */
    static BoundarySource2S of(final GreatArc... boundaries) {
        return of(Arrays.asList(boundaries));
    }

    /** Return a {@link BoundarySource2S} instance containing the given boundaries. The given
     * collection is used directly as the source of the line subsets; no copy is made.
     * @param boundaries boundaries to include in the boundary source
     * @return a boundary source containing the given boundaries
     */
    static BoundarySource2S of(final Collection<GreatArc> boundaries) {
        return boundaries::stream;
    }
}
