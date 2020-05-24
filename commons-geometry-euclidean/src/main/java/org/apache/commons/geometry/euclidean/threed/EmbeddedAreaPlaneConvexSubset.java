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

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Internal implementation of {@link PlaneConvexSubset} that uses an embedded
 * {@link ConvexArea} to represent the subspace region. This class is capable of
 * representing regions of infinite size.
 */
final class EmbeddedAreaPlaneConvexSubset extends AbstractEmbeddedRegionPlaneSubset
    implements PlaneConvexSubset, PlaneConvexSubset.Embedded {

    /** The embedded 2D area. */
    private final ConvexArea area;

    /** Create a new instance from its component parts.
     * @param plane plane the the convex area is embedded in
     * @param area the embedded convex area
     */
    EmbeddedAreaPlaneConvexSubset(final EmbeddingPlane plane, final ConvexArea area) {
        super(plane);

        this.area = area;
    }

    /** {@inheritDoc} */
    @Override
    public PlaneConvexSubset.Embedded getEmbedded() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexArea getSubspaceRegion() {
        return area;
    }

    /** {@inheritDoc} */
    @Override
    public List<Vector3D> getVertices() {
        return getPlane().toSpace(area.getVertices());
    }

    /** {@inheritDoc} */
    @Override
    public Bounds3D getBounds() {
        return getBoundsFromSubspace(area);
    }

    /** {@inheritDoc} */
    @Override
    public List<Triangle3D> toTriangles() {
        if (isInfinite()) {
            throw new IllegalStateException("Cannot convert infinite plane subset to triangles: " + this);
        }

        final EmbeddingPlane plane = getPlane();
        final List<Vector3D> vertices = plane.toSpace(area.getVertices());

        return Planes.convexPolygonToTriangleFan(plane, vertices);
    }

    /** {@inheritDoc} */
    @Override
    public EmbeddedAreaPlaneConvexSubset transform(final Transform<Vector3D> transform) {
        final EmbeddingPlane.SubspaceTransform st = getPlane().subspaceTransform(transform);
        final ConvexArea tArea = area.transform(st.getTransform());

        return new EmbeddedAreaPlaneConvexSubset(st.getPlane().getEmbedding(), tArea);
    }

    /** {@inheritDoc} */
    @Override
    public EmbeddedAreaPlaneConvexSubset reverse() {
        final EmbeddingPlane plane = getPlane();
        final EmbeddingPlane rPlane = plane.reverse();

        final Vector2D rU = rPlane.toSubspace(plane.toSpace(Vector2D.Unit.PLUS_X));
        final Vector2D rV = rPlane.toSubspace(plane.toSpace(Vector2D.Unit.PLUS_Y));

        final AffineTransformMatrix2D transform =
                AffineTransformMatrix2D.fromColumnVectors(rU, rV);

        return new EmbeddedAreaPlaneConvexSubset(rPlane, area.transform(transform));
    }

    /** {@inheritDoc} */
    @Override
    public Split<PlaneConvexSubset> split(final Hyperplane<Vector3D> splitter) {
        // delegate back to the Planes factory method so that it has a chance to decide
        // on the best possible implementation for the given area
        return Planes.subspaceSplit((Plane) splitter, this,
            (p, r) -> Planes.subsetFromConvexArea(p, (ConvexArea) r));
    }
}
