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
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

public final class SubPlane extends AbstractSubPlane<RegionBSPTree2D> {

    private final RegionBSPTree2D region;

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
    public Region<Vector2D> getSubspaceRegion() {
        return region;
    }

    public static class SubPlaneBuilder implements SubHyperplane.Builder<Vector3D> {

        private final Plane plane;

        public SubPlaneBuilder(final Plane plane) {
            this.plane = plane;
        }

        @Override
        public void add(SubHyperplane<Vector3D> sub) {
            // TODO Auto-generated method stub

        }

        @Override
        public void add(ConvexSubHyperplane<Vector3D> sub) {
            // TODO Auto-generated method stub

        }

        @Override
        public SubHyperplane<Vector3D> build() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
