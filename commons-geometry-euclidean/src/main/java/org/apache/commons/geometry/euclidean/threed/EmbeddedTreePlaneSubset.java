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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.rotation.Rotation2D;

/** Class representing an arbitrary subset of a plane using a {@link RegionBSPTree2D}.
 * This class can represent convex, non-convex, finite, infinite, and empty regions.
 *
 * <p>This class is mutable and <em>not</em> thread safe.</p>
 */
public final class EmbeddedTreePlaneSubset extends AbstractEmbeddedRegionPlaneSubset {

    /** The 2D region representing the area on the plane. */
    private final RegionBSPTree2D region;

    /** Construct a new, empty plane subset for the given plane.
     * @param plane plane containing the subset
     */
    public EmbeddedTreePlaneSubset(final EmbeddingPlane plane) {
        this(plane, false);
    }

    /** Construct a new subset for the given plane. If {@code full}
     * is true, then the subset will cover the entire plane; otherwise,
     * it will be empty.
     * @param plane plane containing the subset
     * @param full if true, the subset will cover the entire space;
     *      otherwise it will be empty
     */
    public EmbeddedTreePlaneSubset(final EmbeddingPlane plane, final boolean full) {
        this(plane, new RegionBSPTree2D(full));
    }

    /** Construct a new instance from its defining plane and subspace region.
     * @param plane plane containing the subset
     * @param region subspace region for the plane subset
     */
    public EmbeddedTreePlaneSubset(final EmbeddingPlane plane, final RegionBSPTree2D region) {
        super(plane);

        this.region = region;
    }

    /** {@inheritDoc} */
    @Override
    public PlaneSubset.Embedded getEmbedded() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree2D getSubspaceRegion() {
        return region;
    }

    /** {@inheritDoc} */
    @Override
    public List<PlaneConvexSubset> toConvex() {
        final List<ConvexArea> areas = region.toConvex();

        final List<PlaneConvexSubset> facets = new ArrayList<>(areas.size());

        for (final ConvexArea area : areas) {
            facets.add(Planes.subsetFromConvexArea(getPlane(), area));
        }

        return facets;
    }

    /** {@inheritDoc} */
    @Override
    public List<Triangle3D> toTriangles() {
        final EmbeddingPlane plane = getPlane();
        final List<Triangle3D> triangles = new ArrayList<>();

        List<Vector3D> vertices;
        for (final ConvexArea area : region.toConvex()) {
            if (area.isInfinite()) {
                throw new IllegalStateException("Cannot convert infinite plane subset to triangles: " + this);
            }

            vertices = plane.toSpace(area.getVertices());

            triangles.addAll(Planes.convexPolygonToTriangleFan(plane, vertices));
        }

        return triangles;
    }

    /** {@inheritDoc} */
    @Override
    public Bounds3D getBounds() {
        return getBoundsFromSubspace(region);
    }

    /** {@inheritDoc}
     *
     * <p>In all cases, the current instance is not modified. However, In order to avoid
     * unnecessary copying, this method will use the current instance as the split value when
     * the instance lies entirely on the plus or minus side of the splitter. For example, if
     * this instance lies entirely on the minus side of the splitter, the plane subset
     * returned by {@link Split#getMinus()} will be this instance. Similarly, {@link Split#getPlus()}
     * will return the current instance if it lies entirely on the plus side. Callers need to make
     * special note of this, since this class is mutable.</p>
     */
    @Override
    public Split<EmbeddedTreePlaneSubset> split(final Hyperplane<Vector3D> splitter) {
        return Planes.subspaceSplit((Plane) splitter, this,
            (p, r) -> new EmbeddedTreePlaneSubset(p, (RegionBSPTree2D) r));
    }

    /** {@inheritDoc} */
    @Override
    public EmbeddedTreePlaneSubset transform(final Transform<Vector3D> transform) {
        final EmbeddingPlane.SubspaceTransform subTransform =
                getPlane().getEmbedding().subspaceTransform(transform);

        final RegionBSPTree2D tRegion = RegionBSPTree2D.empty();
        tRegion.copy(region);
        tRegion.transform(subTransform.getTransform());

        return new EmbeddedTreePlaneSubset(subTransform.getPlane(), tRegion);
    }

    /** Add a plane convex subset to this instance.
     * @param subset plane convex subset to add
     * @throws IllegalArgumentException if the given plane subset is not from
     *      a plane equivalent to this instance
     */
    public void add(final PlaneConvexSubset subset) {
        Planes.validatePlanesEquivalent(getPlane(), subset.getPlane());

        final PlaneConvexSubset.Embedded embedded = subset.getEmbedded();
        final Rotation2D rot = getEmbeddedRegionRotation(embedded);

        final ConvexArea subspaceArea = embedded.getSubspaceRegion();

        final ConvexArea toAdd = rot != null ?
                subspaceArea.transform(rot) :
                subspaceArea;

        region.add(toAdd);
    }

    /** Add a plane subset to this instance.
     * @param subset plane subset to add
     * @throws IllegalArgumentException if the given plane subset is not from
     *      a plane equivalent to this instance
     */
    public void add(final EmbeddedTreePlaneSubset subset) {
        Planes.validatePlanesEquivalent(getPlane(), subset.getPlane());

        final RegionBSPTree2D otherTree = subset.getSubspaceRegion();
        final Rotation2D rot = getEmbeddedRegionRotation(subset);

        final RegionBSPTree2D regionToAdd;
        if (rot != null) {
            // we need to transform the subspace region before adding
            regionToAdd = otherTree.copy();
            regionToAdd.transform(rot);
        } else {
            regionToAdd = otherTree;
        }

        region.union(regionToAdd);
    }

    /** Construct a rotation transform used to transform the subspace of the given embedded region plane
     * subset into the subspace of this instance. Returns null if no transform is needed. This method must only
     * be called with embedded regions that share an equivalent plane with this instance, meaning that the
     * planes have the same origin point and normal
     * @param embedded the embedded region plane subset to compare with the current instance
     * @return a rotation transform to convert from the subspace of the argument into the current subspace; returns
     *      null if no such transform is needed
     */
    private Rotation2D getEmbeddedRegionRotation(final PlaneSubset.Embedded embedded) {
        // check if we need to apply a rotation to the given embedded subspace
        final EmbeddingPlane thisPlane = getPlane();
        final EmbeddingPlane otherPlane = embedded.getPlane();

        final DoublePrecisionContext precision = thisPlane.getPrecision();

        final double uDot = thisPlane.getU().dot(otherPlane.getU());
        if (!precision.eq(uDot, 1.0)) {
            final Vector2D otherPlaneU = thisPlane.toSubspace(otherPlane.getOrigin().add(otherPlane.getU()));
            final double angle = Math.atan2(otherPlaneU.getY(), otherPlaneU.getX());

            return Rotation2D.of(angle);
        }

        return null;
    }
}
