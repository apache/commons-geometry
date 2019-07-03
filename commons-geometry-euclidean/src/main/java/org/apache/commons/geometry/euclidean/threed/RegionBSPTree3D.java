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

import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partition.bsp.AbstractRegionBSPTree;

public class RegionBSPTree3D extends AbstractRegionBSPTree<Vector3D, RegionBSPTree3D.RegionNode3D> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190702L;

    /** Create a new, empty region.
     */
    public RegionBSPTree3D() {
        this(false);
    }

    /** Create a new region. If {@code full} is true, then the region will
     * represent the entire 3D space. Otherwise, it will be empty.
     * @param full whether or not the region should contain the entire
     *      3D space or be empty
     */
    public RegionBSPTree3D(boolean full) {
        super(full);
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexVolume> toConvex() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Split<RegionBSPTree3D> split(final Hyperplane<Vector3D> splitter) {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<Vector3D> computeRegionSizeProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode3D createNode() {
        return new RegionNode3D(this);
    }

    public static class RegionNode3D extends AbstractRegionBSPTree.AbstractRegionNode<Vector3D, RegionNode3D> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190702L;

        /** Simple constructor.
         * @param tree the owning tree instance
         */
        protected RegionNode3D(AbstractBSPTree<Vector3D, RegionNode3D> tree) {
            super(tree);
        }

        /** Get the region represented by this node. The returned region contains
         * the entire area contained in this node, regardless of the attributes of
         * any child nodes.
         * @return the region represented by this node
         */
        public ConvexVolume getNodeRegion() {
            // TODO
            return null;
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode3D getSelf() {
            return this;
        }
    }
}
