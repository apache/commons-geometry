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
package org.apache.commons.geometry.spherical.oned;

import java.util.List;

import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree;

public class RegionBSPTree1S extends AbstractRegionBSPTree<Point1S, RegionBSPTree1S.RegionNode1S> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190817L;

    public RegionBSPTree1S(boolean full) {
        super(full);
    }

    /** {@inheritDoc} */
    @Override
    public List<AngularInterval> toConvex() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Split<? extends HyperplaneBoundedRegion<Point1S>> split(Hyperplane<Point1S> splitter) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<Point1S> computeRegionSizeProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode1S createNode() {
        // TODO Auto-generated method stub
        return null;
    }

    /** BSP tree node for one dimensional spherical space.
     */
    public static final class RegionNode1S extends AbstractRegionBSPTree.AbstractRegionNode<Point1S, RegionNode1S> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190817L;

        protected RegionNode1S(final AbstractBSPTree<Point1S, RegionNode1S> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode1S getSelf() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
