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

import java.util.List;

import org.apache.commons.geometry.core.RegionEmbedding;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Interface representing a subset of points in a plane in Euclidean 3D space. Instances
 * may represent finite, infinite, convex, non-convex, and/or disjoint regions of the plane.
 */
public interface PlaneSubset extends HyperplaneSubset<Vector3D> {

    /** Get the plane containing this subset. This is equivalent to {@link #getHyperplane()}.
     * @return the plane containing this subset
     * @see #getHyperplane()
     */
    Plane getPlane();

    /** {@inheritDoc} */
    @Override
    Plane getHyperplane();

    /** {@inheritDoc} */
    @Override
    List<PlaneConvexSubset> toConvex();

    /** Return a list of triangles representing the same subset region as this instance. An
     * {@link IllegalStateException} is thrown if the subset has infinite size and therefore
     * cannot be converted to triangles. If the subset has zero size (is empty), an empty list is
     * returned.
     * @return a list of triangles representing the same subset region as this instance
     * @throws IllegalStateException if the subset has infinite size and therefore cannot
     *      be converted to triangles
     */
    List<Triangle3D> toTriangles();

    /** Get a {@link Bounds3D} object defining an axis-aligned bounding box containing all
     * vertices for this subset. Null is returned if the subset is infinite or does not
     * contain any vertices.
     * @return the bounding box for this instance or null if no valid bounds could be determined
     */
    Bounds3D getBounds();

    /** Return an object containing the plane subset as an embedded 2D subspace region.
     * @return an object containing the plane subset as an embedded 2D subspace region
     */
    PlaneSubset.Embedded getEmbedded();

    /** Get the unique intersection of this plane subset with the given line. Null is
     * returned if no unique intersection point exists (ie, the line and plane are
     * parallel or coincident) or the line does not intersect the plane subset.
     * @param line line to intersect with this plane subset
     * @return the unique intersection point between the line and this plane subset
     *      or null if no such point exists.
     * @see Plane#intersection(Line3D)
     */
    Vector3D intersection(Line3D line);

    /** Get the unique intersection of this plane subset with the given line subset. Null
     * is returned if the underlying line and plane do not have a unique intersection
     * point (ie, they are parallel or coincident) or the intersection point is unique
     * but is not contained in both the line subset and plane subset.
     * @param lineSubset line subset to intersect with
     * @return the unique intersection point between this plane subset and the argument or
     *      null if no such point exists.
     * @see Plane#intersection(Line3D)
     */
    Vector3D intersection(LineConvexSubset3D lineSubset);

    /** Interface used to represent plane subsets as embedded 2D subspace regions.
     */
    interface Embedded extends RegionEmbedding<Vector3D, Vector2D> {

        /** Get the plane embedding the subspace region.
         * @return the plane embedding the subspace region
         */
        EmbeddingPlane getPlane();

        /** {@inheritDoc} */
        @Override
        HyperplaneBoundedRegion<Vector2D> getSubspaceRegion();
    }
}
