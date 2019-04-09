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

import org.apache.commons.geometry.core.partition.AbstractBSPTree;
import org.apache.commons.geometry.core.partition.region.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.partition.region.AbstractRegionBSPTree.AbstractRegionNode;

public final class RegionBSPTree1D extends AbstractRegionBSPTree<Vector1D, RegionBSPTree1D.RegionNode1D> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190405L;

    public RegionBSPTree1D() {
        this(true);
    }

    public RegionBSPTree1D(boolean full) {
        super(full);
    }

    public void insert(final Interval interval) {
        if (interval.isFull()) {
            setFull();
        }
        else if (!interval.isEmpty()) {
            final double min = interval.getMin();
            final double max = interval.getMax();

            if (Double.isFinite(min)) {
                final OrientedPoint minPt = OrientedPoint.createPositiveFacing(interval.getMin(), interval.getPrecision());
                insert(minPt);
            }

            if (Double.isFinite(max)) {
                final OrientedPoint maxPt = OrientedPoint.createNegativeFacing(interval.getMax(), interval.getPrecision());
                insert(maxPt);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    protected RegionBSPTree1D createTree() {
        return new RegionBSPTree1D();
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode1D createNode() {
        return new RegionNode1D(this);
    }

    public static final class RegionNode1D extends AbstractRegionNode<Vector1D, RegionNode1D> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190405L;

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
