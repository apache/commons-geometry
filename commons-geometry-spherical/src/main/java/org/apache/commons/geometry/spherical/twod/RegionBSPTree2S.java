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
package org.apache.commons.geometry.spherical.twod;

import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree;

/** BSP tree representing regions in 1D spherical space.
 */
public class RegionBSPTree2S extends AbstractRegionBSPTree<Point2S, RegionBSPTree2S.RegionNode2S> {

    /** Serializable UID */
    private static final long serialVersionUID = 20191005L;

    /** Create a new, empty instance.
     */
    public RegionBSPTree2S() {
        this(false);
    }

    /** Create a new region. If {@code full} is true, then the region will
     * represent the entire 2-sphere. Otherwise, it will be empty.
     * @param full whether or not the region should contain the entire
     *      2-sphere or be empty
     */
    public RegionBSPTree2S(boolean full) {
        super(full);
    }

    /** {@inheritDoc} */
    @Override
    public Split<RegionBSPTree2S> split(Hyperplane<Point2S> splitter) {
        return split(splitter, empty(), empty());
    }

    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<Point2S> computeRegionSizeProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode2S createNode() {
        return new RegionNode2S(this);
    }

    /** Return a new, empty BSP tree.
     * @return a new, empty BSP tree.
     */
    public static RegionBSPTree2S empty() {
        return new RegionBSPTree2S(false);
    }

    /** Return a new, full BSP tree. The returned tree represents the
     * full space.
     * @return a new, full BSP tree.
     */
    public static RegionBSPTree2S full() {
        return new RegionBSPTree2S(true);
    }

    /** BSP tree node for two dimensional spherical space.
     */
    public static final class RegionNode2S extends AbstractRegionBSPTree.AbstractRegionNode<Point2S, RegionNode2S> {

        /** Serializable UID */
        private static final long serialVersionUID = 20191005L;

        /** Simple constructor.
         * @param tree tree owning the instance.
         */
        protected RegionNode2S(final AbstractBSPTree<Point2S, RegionNode2S> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode2S getSelf() {
            return this;
        }
    }
}
