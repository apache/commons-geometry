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
package org.apache.commons.geometry.core.partition.test;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.RegionCutRule;

/**  Region BSP Tree implementation class for testing purposes.
 */
public final class TestRegionBSPTree extends AbstractRegionBSPTree<TestPoint2D, TestRegionBSPTree.TestRegionNode> {

    public TestRegionBSPTree() {
        this(true);
    }

    public TestRegionBSPTree(final boolean full) {
        super(full);
    }

    /**
     * Expose the direct node cut method for easier creation of test tree structures.
     */
    public void cutNode(final TestRegionNode node, final HyperplaneConvexSubset<TestPoint2D> cut) {
        super.setNodeCut(node, cut, getSubtreeInitializer(RegionCutRule.MINUS_INSIDE));
    }

    /** {@inheritDoc} */
    @Override
    protected TestRegionNode createNode() {
        return new TestRegionNode(this);
    }

    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<TestPoint2D> computeRegionSizeProperties() {
     // return a set of stub values
        return new RegionSizeProperties<>(1234, new TestPoint2D(12, 34));
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(TestPoint2D pt) {
        return classify(pt) != RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public Split<TestRegionBSPTree> split(Hyperplane<TestPoint2D> splitter) {
        return split(splitter, new TestRegionBSPTree(), new TestRegionBSPTree());
    }

    /** BSP Tree node class for {@link TestRegionBSPTree}.
     */
    public static final class TestRegionNode
        extends AbstractRegionBSPTree.AbstractRegionNode<TestPoint2D, TestRegionNode> {

        protected TestRegionNode(AbstractBSPTree<TestPoint2D, TestRegionNode> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        protected TestRegionNode getSelf() {
            return this;
        }
    }
}
