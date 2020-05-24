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

import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;

/** Interface representing a finite or infinite convex subset of points in a plane in Euclidean 3D
 * space.
 */
public interface PlaneConvexSubset extends PlaneSubset, HyperplaneConvexSubset<Vector3D> {

    /** {@inheritDoc} */
    @Override
    PlaneConvexSubset reverse();

    /** {@inheritDoc} */
    @Override
    PlaneConvexSubset transform(Transform<Vector3D> transform);

    /** {@inheritDoc} */
    @Override
    Split<PlaneConvexSubset> split(Hyperplane<Vector3D> splitter);

    /** {@inheritDoc} */
    @Override
    PlaneConvexSubset.Embedded getEmbedded();

    /** Get the vertices for the convex subset in a counter-clockwise order as viewed looking down the plane
     * normal. Each vertex in the returned list is unique. If the boundary of the subset is closed, the start
     * vertex is <em>not</em> repeated at the end of the list.
     *
     * <p>It is important to note that, in general, the list of vertices returned by this method
     * is not sufficient to completely characterize the subset. For example, a simple triangle
     * has 3 vertices, but an infinite area constructed from two parallel lines and two lines that
     * intersect between them will also have 3 vertices. It is also possible for non-empty subsets to
     * contain no vertices at all. For example, a subset with no boundaries (representing the full
     * plane), a subset with a single boundary (ie, a half-plane), or a subset with two parallel boundaries will
     * not contain any vertices.</p>
     * @return the list of vertices for the plane convex subset in a counter-clockwise order as viewed looking
     *      down the plane normal
     */
    List<Vector3D> getVertices();

    /** {@inheritDoc}
     *
     * <p>This method simply returns a singleton list containing this object.</p>
     */
    @Override
    default List<PlaneConvexSubset> toConvex() {
        return Collections.singletonList(this);
    }

    /** Interface used to represent plane convex subsets as embedded 2D subspace regions.
     */
    interface Embedded extends PlaneSubset.Embedded {

        /** {@inheritDoc} */
        @Override
        ConvexArea getSubspaceRegion();
    }
}
