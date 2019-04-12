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
package org.apache.commons.geometry.euclidean.oned;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partition.AbstractBSPTree;
import org.apache.commons.geometry.core.partition.region.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.partition.region.AbstractRegionBSPTree.AbstractRegionNode;

/** Binary space partitioning (BSP) tree representing a region in one dimensional
 * Euclidean space.
 */
public final class RegionBSPTree1D extends AbstractRegionBSPTree<Vector1D, RegionBSPTree1D.RegionNode1D> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190405L;

    /** Create a new region representing the entire number line.
     */
    public RegionBSPTree1D() {
        this(true);
    }

    /** Create a new region. If {@code full} is true, then the region will
     * represent the entire number line. Otherwise, it will be empty.
     * @param full whether or not the region should contain the entire
     *      number line or be empty
     */
    public RegionBSPTree1D(boolean full) {
        super(full);
    }

    /** Add an interval to this region.
     * @param interval
     */
    public void add(final Interval interval) {
        union(interval.toTree());
    }

    /** Classify a point with respect to the region. This is
     * a convenience overload of {@link #classify(Vector1D)} for
     * use in one dimension.
     * @param x the point to classify
     * @return the location of the point with respect to the region
     * @see #classify(Vector1D)
     */
    public RegionLocation classify(final double x) {
        return classify(Vector1D.of(x));
    }

    public boolean contains(final double x) {
        return contains(Vector1D.of(x));
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode1D createNode() {
        return new RegionNode1D(this);
    }

    /** BSP tree node for one dimensional Euclidean space.
     */
    public static final class RegionNode1D extends AbstractRegionNode<Vector1D, RegionNode1D> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190405L;

        /** Simple constructor.
         * @param tree the owning tre instance
         */
        private RegionNode1D(AbstractBSPTree<Vector1D, RegionNode1D> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode1D getSelf() {
            return this;
        }
    }
}
