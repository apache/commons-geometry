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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Class representing an arbitrary region of a plane. This class can represent
 * both convex and non-convex regions of its underlying plane.
 *
 * <p>This class is mutable and <em>not</em> thread safe.</p>
 */
public final class SubPlane extends AbstractSubPlane<RegionBSPTree2D> implements Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190717L;

    /** The 2D region representing the area on the plane */
    private final RegionBSPTree2D region;

    /** Construct a new, empty subplane for the given plane.
     * @param plane plane defining the subplane
     */
    public SubPlane(final Plane plane) {
        this(plane, false);
    }

    /** Construct a new subplane for the given plane. If {@code full}
     * is true, then the subplane will cover the entire plane; otherwise,
     * it will be empty.
     * @param plane plane defining the subplane
     * @param full if true, the subplane will cover the entire space;
     *      otherwise it will be empty
     */
    public SubPlane(final Plane plane, boolean full) {
        this(plane, new RegionBSPTree2D(full));
    }

    /** Construct a new instance from its defining plane and subspace region.
     * @param plane plane defining the subplane
     * @param region subspace region for the subplane
     */
    public SubPlane(final Plane plane, final RegionBSPTree2D region) {
        super(plane);

        this.region = region;
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexSubPlane> toConvex() {
        final List<ConvexArea> areas = region.toConvex();

        final Plane plane = getPlane();
        final List<ConvexSubPlane> subplanes = new ArrayList<>(areas.size());

        for (ConvexArea area : areas) {
            subplanes.add(ConvexSubPlane.fromConvexArea(plane, area));
        }

        return subplanes;
    }

    /** {@inheritDoc} */
    @Override
    public Split<SubPlane> split(Hyperplane<Vector3D> splitter) {
        return splitInternal(splitter, this, (p, r) -> new SubPlane(p, (RegionBSPTree2D) r));
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree2D getSubspaceRegion() {
        return region;
    }

    /** {@inheritDoc} */
    @Override
    public SubPlane transform(final Transform<Vector3D> transform) {
        final Plane origPlane = getPlane();

        final Vector3D tOrigin = transform.apply(origPlane.getOrigin());
        final Vector3D tU = transform.apply(origPlane.toSpace(Vector2D.PLUS_X)).subtract(tOrigin);
        final Vector3D tV = transform.apply(origPlane.toSpace(Vector2D.PLUS_Y)).subtract(tOrigin);

        final Plane tPlane = Plane.fromPointAndPlaneVectors(tOrigin, tU, tV, getPrecision());

        final AffineTransformMatrix2D subTransform = AffineTransformMatrix2D.fromColumnVectors(
                    tPlane.toSubspace(tU),
                    tPlane.toSubspace(tV))
                .translate(tPlane.toSubspace(tOrigin));

        final RegionBSPTree2D tRegion = RegionBSPTree2D.empty();
        tRegion.copy(region);
        tRegion.transform(subTransform);

        return new SubPlane(tPlane, tRegion);
    }

    /** Add a convex subplane to this instance.
     * @param subplane convex subplane to add
     * @throws IllegalArgumentException if the given convex subplane is not from
     *      a plane equivalent to this instance
     */
    public void add(final ConvexSubPlane subplane) {
        validatePlane(subplane.getPlane());

        region.add(subplane.getSubspaceRegion());
    }

    /** Add a subplane to this instance.
     * @param subplane subplane to add
     * @throws IllegalArgumentException if the given convex subplane is not from
     *      a plane equivalent to this instance
     */
    public void add(final SubPlane subplane) {
        validatePlane(subplane.getPlane());

        region.union(subplane.getSubspaceRegion());
    }

    /** Validate that the given plane is equivalent to the plane
     * defining this subplane.
     * @param inputPlane
     * @throws IllegalArgumentException if the given plane is not equivalent
     *      to the plane for this instance
     */
    private void validatePlane(final Plane inputPlane) {
        final Plane plane = getPlane();

        if (!plane.eq(inputPlane)) {
            throw new IllegalArgumentException("Argument is not on the same " +
                    "plane. Expected " + plane + " but was " +
                    inputPlane);
        }
    }

    /** {@link Builder} implementation for sublines.
     */
    public static class SubPlaneBuilder implements SubHyperplane.Builder<Vector3D> {

        /** Subplane instance created by this builder. */
        private final SubPlane subplane;

        /** Construct a new instance for building subplane region for the given plane.
         * @param plane the underlying plane for the subplane region
         */
        public SubPlaneBuilder(final Plane plane) {
            this.subplane = new SubPlane(plane);
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
        public SubPlane build() {
            return subplane;
        }

        /** Internal method for adding subhyperplanes to this builder.
         * @param sub the subhyperplane to add; either convex or non-convex
         */
        private void addInternal(final SubHyperplane<Vector3D> sub) {
            if (sub instanceof ConvexSubPlane) {
                subplane.add((ConvexSubPlane) sub);
            }
            else if (sub instanceof SubPlane) {
                subplane.add((SubPlane) sub);
            }
            else {
                throw new IllegalArgumentException("Unsupported subhyperplane type: " + sub.getClass().getName());
            }
        }
    }
}
