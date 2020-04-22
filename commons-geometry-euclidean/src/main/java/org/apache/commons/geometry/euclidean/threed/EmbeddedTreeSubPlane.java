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
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;

/** Class representing an arbitrary subset of a plane using a {@link RegionBSPTree2D}.
 * This class can represent convex, non-convex, finite, infinite, and empty regions.
 *
 * <p>This class is mutable and <em>not</em> thread safe.</p>
 */
public final class EmbeddedTreeSubPlane extends SubPlane {
    /** The 2D region representing the area on the plane. */
    private final RegionBSPTree2D region;

    /** Construct a new, empty subplane for the given plane.
     * @param plane plane defining the subplane
     */
    public EmbeddedTreeSubPlane(final Plane plane) {
        this(plane, false);
    }

    /** Construct a new subplane for the given plane. If {@code full}
     * is true, then the subplane will cover the entire plane; otherwise,
     * it will be empty.
     * @param plane plane defining the subplane
     * @param full if true, the subplane will cover the entire space;
     *      otherwise it will be empty
     */
    public EmbeddedTreeSubPlane(final Plane plane, boolean full) {
        this(plane, new RegionBSPTree2D(full));
    }

    /** Construct a new instance from its defining plane and subspace region.
     * @param plane plane defining the subplane
     * @param region subspace region for the subplane
     */
    public EmbeddedTreeSubPlane(final Plane plane, final RegionBSPTree2D region) {
        super(plane);

        this.region = region;
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexSubPlane> toConvex() {
        final List<ConvexArea> areas = region.toConvex();

        final Plane plane = getPlane();
        final List<ConvexSubPlane> facets = new ArrayList<>(areas.size());

        for (final ConvexArea area : areas) {
            facets.add(ConvexSubPlane.fromConvexArea(plane, area));
        }

        return facets;
    }

    /** {@inheritDoc}
     *
     * <p>In all cases, the current instance is not modified. However, In order to avoid
     * unnecessary copying, this method will use the current instance as the split value when
     * the instance lies entirely on the plus or minus side of the splitter. For example, if
     * this instance lies entirely on the minus side of the splitter, the subplane
     * returned by {@link Split#getMinus()} will be this instance. Similarly, {@link Split#getPlus()}
     * will return the current instance if it lies entirely on the plus side. Callers need to make
     * special note of this, since this class is mutable.</p>
     */
    @Override
    public Split<EmbeddedTreeSubPlane> split(final Hyperplane<Vector3D> splitter) {
        return splitInternal(splitter, this, (p, r) -> new EmbeddedTreeSubPlane(p, (RegionBSPTree2D) r));
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree2D getSubspaceRegion() {
        return region;
    }

    /** {@inheritDoc} */
    @Override
    public EmbeddedTreeSubPlane transform(final Transform<Vector3D> transform) {
        final Plane.SubspaceTransform subTransform = getPlane().subspaceTransform(transform);

        final RegionBSPTree2D tRegion = RegionBSPTree2D.empty();
        tRegion.copy(region);
        tRegion.transform(subTransform.getTransform());

        return new EmbeddedTreeSubPlane(subTransform.getPlane(), tRegion);
    }

    /** Add a convex subplane to this instance.
     * @param subplane convex subplane to add
     * @throws IllegalArgumentException if the given subplane is not from
     *      a plane equivalent to this instance
     */
    public void add(final ConvexSubPlane subplane) {
        validatePlane(subplane.getPlane());

        region.add(subplane.getSubspaceRegion());
    }

    /** Add a subplane to this instance.
     * @param subplane subplane to add
     * @throws IllegalArgumentException if the given subplane is not from
     *      a plane equivalent to this instance
     */
    public void add(final EmbeddedTreeSubPlane subplane) {
        validatePlane(subplane.getPlane());

        region.union(subplane.getSubspaceRegion());
    }

    /** Validate that the given plane is equivalent to the plane
     * defining this subplane.
     * @param inputPlane plane to validate
     * @throws IllegalArgumentException if the given plane is not equivalent
     *      to the plane for this instance
     */
    private void validatePlane(final Plane inputPlane) {
        final Plane plane = getPlane();

        if (!plane.eq(inputPlane, plane.getPrecision())) {
            throw new IllegalArgumentException("Argument is not on the same " +
                    "plane. Expected " + plane + " but was " +
                    inputPlane);
        }
    }

    /** {@link SubHyperplane.Builder} implementation for sublines.
     */
    public static class Builder implements SubHyperplane.Builder<Vector3D> {

        /** Subplane instance created by this builder. */
        private final EmbeddedTreeSubPlane subplane;

        /** Construct a new instance for building subplane region for the given plane.
         * @param plane the underlying plane for the subplane region
         */
        public Builder(final Plane plane) {
            this.subplane = new EmbeddedTreeSubPlane(plane);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final SubHyperplane<Vector3D> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final ConvexSubHyperplane<Vector3D> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public EmbeddedTreeSubPlane build() {
            return subplane;
        }

        /** Internal method for adding subhyperplanes to this builder.
         * @param sub the subhyperplane to add; either convex or non-convex
         */
        private void addInternal(final SubHyperplane<Vector3D> sub) {
            if (sub instanceof ConvexSubPlane) {
                subplane.add((ConvexSubPlane) sub);
            } else if (sub instanceof EmbeddedTreeSubPlane) {
                subplane.add((EmbeddedTreeSubPlane) sub);
            } else {
                throw new IllegalArgumentException("Unsupported subhyperplane type: " + sub.getClass().getName());
            }
        }
    }
}
