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

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

public final class SubPlane extends AbstractSubPlane<RegionBSPTree2D> {

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
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Split<SubPlane> split(Hyperplane<Vector3D> splitter) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree2D getSubspaceRegion() {
        return region;
    }

    /** {@inheritDoc} */
    @Override
    public SubPlane transform(final Transform<Vector3D> transform) {

        return null;
    }

    public void add(final ConvexSubPlane subplane) {
        validatePlane(subplane.getPlane());

        region.add(subplane.getSubspaceRegion());
    }

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

    public static class SubPlaneBuilder implements SubHyperplane.Builder<Vector3D> {

        private final Plane plane;

        public SubPlaneBuilder(final Plane plane) {
            this.plane = plane;
        }

        /** {@inheritDoc} */
        @Override
        public void add(SubHyperplane<Vector3D> sub) {
            // TODO Auto-generated method stub

        }

        /** {@inheritDoc} */
        @Override
        public void add(ConvexSubHyperplane<Vector3D> sub) {
            // TODO Auto-generated method stub

        }

        /** {@inheritDoc} */
        @Override
        public SubHyperplane<Vector3D> build() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
