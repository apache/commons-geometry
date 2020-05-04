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
package org.apache.commons.geometry.core.partitioning;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;

/** Abstract base class for hyperplane subset implementations that embed a lower-dimension region through
 * an embedding hyperplane.
 * @param <P> Point implementation type
 * @param <S> Subspace point implementation type
 * @param <H> Hyperplane containing the embedded subspace
 */
public abstract class AbstractEmbeddingHyperplaneSubset<
    P extends Point<P>,
    S extends Point<S>,
    H extends EmbeddingHyperplane<P, S>> implements HyperplaneSubset<P> {

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
    public boolean isInfinite() {
        return Double.isInfinite(getSize());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinite() {
        return Double.isFinite(getSize());
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return getSubspaceRegion().getSize();
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(P point) {
        final H hyperplane = getHyperplane();

        if (hyperplane.contains(point)) {
            final S subPoint = hyperplane.toSubspace(point);

            return getSubspaceRegion().classify(subPoint);
        }
        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public P closest(P point) {
        final H hyperplane = getHyperplane();

        final P projected = hyperplane.project(point);
        final S subProjected = hyperplane.toSubspace(projected);

        final Region<S> region = getSubspaceRegion();
        if (region.contains(subProjected)) {
            return projected;
        }

        final S subRegionBoundary = region.project(subProjected);
        if (subRegionBoundary != null) {
            return hyperplane.toSpace(subRegionBoundary);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public abstract H getHyperplane();

    /** Return the embedded subspace region for this instance.
     * @return the embedded subspace region for this instance
     */
    public abstract HyperplaneBoundedRegion<S> getSubspaceRegion();
}
