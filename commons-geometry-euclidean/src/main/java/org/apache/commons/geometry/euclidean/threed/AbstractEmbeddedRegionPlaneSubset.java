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

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.internal.HyperplaneSubsets;
import org.apache.commons.geometry.euclidean.twod.BoundarySource2D;
import org.apache.commons.geometry.euclidean.twod.Bounds2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Base class for {@link PlaneSubset} implementations that use an embedded subspace region
 * to define their plane subsets.
 */
abstract class AbstractEmbeddedRegionPlaneSubset extends AbstractPlaneSubset implements PlaneSubset.Embedded {

    /** The plane containing the embedded region. */
    private final EmbeddingPlane plane;

    /** Construct a new instance in the given plane.
     * @param plane plane containing the subset
     */
    AbstractEmbeddedRegionPlaneSubset(final EmbeddingPlane plane) {
        this.plane = plane;
    }

    /** {@inheritDoc} */
    @Override
    public EmbeddingPlane getPlane() {
        return plane;
    }

    /** {@inheritDoc} */
    @Override
    public EmbeddingPlane getHyperplane() {
        return plane;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        return getSubspaceRegion().isFull();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return getSubspaceRegion().isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return getSubspaceRegion().getSize();
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getBarycenter() {
        final Vector2D subspaceBarycenter = getSubspaceRegion().getBarycenter();
        if (subspaceBarycenter != null) {
            return getPlane().toSpace(subspaceBarycenter);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D toSpace(final Vector2D pt) {
        return plane.toSpace(pt);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D toSubspace(final Vector3D pt) {
        return plane.toSubspace(pt);
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final Vector3D pt) {
        return HyperplaneSubsets.classifyAgainstEmbeddedRegion(pt, plane, getSubspaceRegion());
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D closest(final Vector3D pt) {
        return HyperplaneSubsets.closestToEmbeddedRegion(pt, plane, getSubspaceRegion());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[plane= ")
            .append(getPlane())
            .append(", subspaceRegion= ")
            .append(getSubspaceRegion())
            .append(']');

        return sb.toString();
    }

    /** Compute 3D bounds from a subspace boundary source.
     * @param src subspace boundary source
     * @return 3D bounds from the given embedded subspace boundary source or null
     *      if no valid bounds could be determined
     */
    protected Bounds3D getBoundsFromSubspace(final BoundarySource2D src) {
        final Bounds2D subspaceBounds = src.getBounds();
        if (subspaceBounds != null) {
            final Vector3D min = plane.toSpace(subspaceBounds.getMin());
            final Vector3D max = plane.toSpace(subspaceBounds.getMax());

            return Bounds3D.builder()
                    .add(min)
                    .add(max)
                    .build();
        }

        return null;
    }
}
