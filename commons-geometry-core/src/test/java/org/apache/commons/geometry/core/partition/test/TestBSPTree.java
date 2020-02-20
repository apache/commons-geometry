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

import org.apache.commons.geometry.core.partitioning.BoundarySource;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;

/** BSP Tree implementation class for testing purposes.
 */
public class TestBSPTree extends AbstractBSPTree<TestPoint2D, TestBSPTree.TestNode> {

    /** {@inheritDoc} */
    @Override
    protected TestNode createNode() {
        return new TestNode(this);
    }

    public void insert(final SubHyperplane<TestPoint2D> sub) {
        insert(sub.toConvex());
    }

    public void insert(final ConvexSubHyperplane<TestPoint2D> sub) {
        insert(sub, root -> { });
    }

    public void insert(final Iterable<? extends ConvexSubHyperplane<TestPoint2D>> subs) {
        subs.forEach(this::insert);
    }

    public void insert(final BoundarySource<TestLineSegment> src) {
        src.boundaryStream().forEach(this::insert);
    }

    /** {@inheritDoc}
     *
     * <p>Exposed as public for testing.</p>
     */
    @Override
    public void splitIntoTrees(final Hyperplane<TestPoint2D> splitter,
            final AbstractBSPTree<TestPoint2D, TestBSPTree.TestNode> minus,
            final AbstractBSPTree<TestPoint2D, TestBSPTree.TestNode> plus) {

        super.splitIntoTrees(splitter, minus, plus);
    }

    /** BSP Tree node class for {@link TestBSPTree}.
     */
    public static class TestNode extends AbstractBSPTree.AbstractNode<TestPoint2D, TestNode> {
        public TestNode(AbstractBSPTree<TestPoint2D, TestNode> tree) {
            super(tree);
        }

        /** Cut this node with the given hyperplane. If the hyperplane intersects the node's region,
         * then the node becomes an internal node with two child leaf node. If the hyperplane does
         * not intersect the node's region, then the node is made a leaf node. The same node is
         * returned, regardless of the outcome of the cut operation.
         * @param cutter hyperplane to cut the node with
         * @return this node
         */
        public TestNode cut(final Hyperplane<TestPoint2D> cutter) {
            insertCut(cutter);

            return this;
        }

        public boolean insertCut(final Hyperplane<TestPoint2D> cutter) {
            return ((TestBSPTree) getTree()).cutNode(getSelf(), cutter, root -> { });
        }

        public boolean clearCut() {
            return ((TestBSPTree) getTree()).removeNodeCut(getSelf());
        }

        /** {@inheritDoc} */
        @Override
        protected TestNode getSelf() {
            return this;
        }
    }
}
