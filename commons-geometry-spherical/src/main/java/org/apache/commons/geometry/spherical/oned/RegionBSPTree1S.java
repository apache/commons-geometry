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

import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree;

/** BSP tree representing regions in 1D spherical space.
 */
public class RegionBSPTree1S extends AbstractRegionBSPTree<Point1S, RegionBSPTree1S.RegionNode1S> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190817L;

    /** Create a new, empty instance.
     */
    public RegionBSPTree1S() {
        this(false);
    }

    /** Create a new region. If {@code full} is true, then the region will
     * represent the entire circle. Otherwise, it will be empty.
     * @param full whether or not the region should contain the entire
     *      circle or be empty
     */
    public RegionBSPTree1S(boolean full) {
        super(full);
    }

    /** Return a deep copy of this instance.
     * @return a deep copy of this instance.
     * @see {@link #copy(org.apache.commons.geometry.core.partitioning.bsp.BSPTree)}
     */
    public RegionBSPTree1S copy() {
        RegionBSPTree1S result = RegionBSPTree1S.empty();
        result.copy(this);

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Split<RegionBSPTree1S> split(final Hyperplane<Point1S> splitter) {
        return split(splitter, RegionBSPTree1S.empty(), RegionBSPTree1S.empty());
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
        return new RegionNode1S(this);
    }

    /** Return a new, empty BSP tree.
     * @return a new, empty BSP tree.
     */
    public static RegionBSPTree1S empty() {
        return new RegionBSPTree1S(false);
    }

    /** Return a new, full BSP tree. The returned tree represents the
     * full space.
     * @return a new, full BSP tree.
     */
    public static RegionBSPTree1S full() {
        return new RegionBSPTree1S(true);
    }

    /** Return a new BSP tree representing the same region as the given angular interval.
     * @param interval the input interval
     * @return a new BSP tree representing the same region as the given angular interval
     */
    public static RegionBSPTree1S fromInterval(final AngularInterval interval) {
        final OrientedPoint1S minBoundary = interval.getMinBoundary();
        final OrientedPoint1S maxBoundary = interval.getMaxBoundary();

        final RegionBSPTree1S tree = full();

        RegionNode1S node = tree.getRoot();

        if (minBoundary != null) {
            tree.cutNode(node, minBoundary.span());

            node = node.getMinus();
        }

        if (maxBoundary != null) {
            tree.cutNode(node, maxBoundary.span());
        }

        return tree;
    }

    /** BSP tree node for one dimensional spherical space.
     */
    public static final class RegionNode1S extends AbstractRegionBSPTree.AbstractRegionNode<Point1S, RegionNode1S> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190817L;

        /** Simple constructor.
         * @param tree the owning tree instance
         */
        protected RegionNode1S(final AbstractBSPTree<Point1S, RegionNode1S> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode1S getSelf() {
            return this;
        }
    }
}
