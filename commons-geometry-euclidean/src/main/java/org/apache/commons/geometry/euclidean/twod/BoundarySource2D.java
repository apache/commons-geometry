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
package org.apache.commons.geometry.euclidean.twod;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.partitioning.BoundarySource;

/** Extension of the {@link BoundarySource} interface for Euclidean 2D space.
 */
public interface BoundarySource2D extends BoundarySource<LineConvexSubset>, Linecastable2D {

    /** Return a {@link BoundaryList2D} containing the boundaries in this instance.
     * @return a {@link BoundaryList2D} containing the boundaries in this instance
     */
    default BoundaryList2D toList() {
        final List<LineConvexSubset> boundaries = boundaryStream()
                .collect(Collectors.toList());

        return new BoundaryList2D(boundaries);
    }

    /** Return a BSP tree constructed from the boundaries contained in this instance. This is
     * a convenience method for quickly constructing BSP trees and may produce unbalanced trees
     * with unacceptable performance characteristics when used with large numbers of boundaries.
     * In these cases, alternate tree construction approaches should be used, such as
     * {@link RegionBSPTree2D.PartitionedRegionBuilder2D}.
     * @return a BSP tree constructed from the boundaries in this instance
     * @see RegionBSPTree2D#partitionedRegionBuilder()
     */
    default RegionBSPTree2D toTree() {
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(this);

        return tree;
    }

    /** {@inheritDoc} */
    @Override
    default List<LinecastPoint2D> linecast(final LineConvexSubset subset) {
        return new BoundarySourceLinecaster2D(this).linecast(subset);
    }

    /** {@inheritDoc} */
    @Override
    default LinecastPoint2D linecastFirst(final LineConvexSubset subset) {
        return new BoundarySourceLinecaster2D(this).linecastFirst(subset);
    }

    /** Get a {@link Bounds2D} object defining the axis-aligned box containing all vertices
     * in the boundaries for this instance. Null is returned if any boundaries are infinite
     * or no vertices were found.
     * @return the bounding box for this instance or null if no valid bounds could be determined
     */
    default Bounds2D getBounds() {
        return new BoundarySourceBoundsBuilder2D().getBounds(this);
    }

    /** Return a {@link BoundarySource2D} instance containing the given boundaries.
     * @param boundaries line subsets to include in the boundary source
     * @return a boundary source containing the given boundaries
     */
    static BoundarySource2D from(final LineConvexSubset... boundaries) {
        return from(Arrays.asList(boundaries));
    }

    /** Return a {@link BoundarySource2D} instance containing the given boundaries. The given
     * collection is used directly as the source of the line subsets; no copy is made.
     * @param boundaries line subsets to include in the boundary source
     * @return a boundary source containing the given boundaries
     */
    static BoundarySource2D from(final Collection<LineConvexSubset> boundaries) {
        return boundaries::stream;
    }
}
