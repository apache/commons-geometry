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
package org.apache.commons.geometry.euclidean.threed;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.geometry.core.partitioning.BoundarySource;

/** Extension of the {@link BoundarySource} interface for Euclidean 3D
 * space.
 */
public interface BoundarySource3D extends BoundarySource<Facet> {

    /** Construct a new BSP tree from the boundaries contained in this
     * instance.
     * @return a new BSP tree constructed from the boundaries in this
     *      instance
     * @see RegionBSPTree3D#from(BoundarySource)
     */
    default RegionBSPTree3D toTree() {
        return RegionBSPTree3D.from(this);
    }

    /** Return a {@link BoundarySource3D} instance containing the given boundaries.
     * @param boundaries boundaries to include in the boundary source
     * @return a boundary source containing the given boundaries
     */
    static BoundarySource3D from(final Facet... boundaries) {
        return from(Arrays.asList(boundaries));
    }

    /** Return a {@link BoundarySource3D} instance containing the given boundaries. The given
     * collection is used directly as the source of the subplanes; no copy is made.
     * @param boundaries boundaries to include in the boundary source
     * @return a boundary source containing the given boundaries
     */
    static BoundarySource3D from(final Collection<Facet> boundaries) {
        return boundaries::stream;
    }
}
