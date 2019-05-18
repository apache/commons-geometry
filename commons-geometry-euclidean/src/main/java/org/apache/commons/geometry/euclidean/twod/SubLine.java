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
package org.apache.commons.geometry.euclidean.twod;

import java.util.List;

import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;;

public class SubLine extends AbstractSubLine<RegionBSPTree1D> {

    /** The 1D region representing the area on the line */
    private final RegionBSPTree1D region;

    private SubLine(final Line line, final RegionBSPTree1D region) {
        super(line);

        this.region = region;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return Double.isInfinite(region.getSize());
    }

    /** {@inheritDoc} */
    @Override
    public List<LineSegment> toConvex() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree1D getSubspaceRegion() {
        RegionBSPTree1D copy = new RegionBSPTree1D();
        copy.copy(region);

        return copy;
    }

    public static final class SubLineBuilder implements SubHyperplane.Builder<Vector2D>{

        @Override
        public void add(SubHyperplane<Vector2D> sub) {
            // TODO Auto-generated method stub

        }

        @Override
        public void add(ConvexSubHyperplane<Vector2D> sub) {
            // TODO Auto-generated method stub

        }

        @Override
        public SubLine build() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
