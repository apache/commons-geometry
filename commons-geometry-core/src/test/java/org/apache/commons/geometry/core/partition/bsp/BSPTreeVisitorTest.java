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
package org.apache.commons.geometry.core.partition.bsp;

import org.apache.commons.geometry.core.partition.bsp.BSPTreeVisitor.ClosestFirstVisitor;
import org.apache.commons.geometry.core.partition.bsp.BSPTreeVisitor.FarthestFirstVisitor;
import org.apache.commons.geometry.core.partition.bsp.BSPTreeVisitor.Order;
import org.apache.commons.geometry.core.partition.test.TestBSPTree;
import org.apache.commons.geometry.core.partition.test.TestBSPTree.TestNode;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Test;

public class BSPTreeVisitorTest {

    @Test
    public void testDefaultVisitOrder() {
        // arrange
        BSPTreeVisitor<TestPoint2D, TestNode> visitor = n -> {};

        // act/assert
        Assert.assertEquals(Order.NODE_MINUS_PLUS, visitor.visitOrder(null));
    }

    @Test
    public void testClosestFirst() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestNode root = tree.getRoot();
        root.cut(TestLine.X_AXIS);
        root.getMinus().cut(TestLine.Y_AXIS);
        root.getPlus().cut(TestLine.Y_AXIS);

        // act
        checkClosestFirst(new TestPoint2D(1, 1), root, Order.MINUS_NODE_PLUS);
        checkClosestFirst(new TestPoint2D(1, 1), root.getMinus(), Order.PLUS_NODE_MINUS);
        checkClosestFirst(new TestPoint2D(1, 1), root.getPlus(), Order.PLUS_NODE_MINUS);

        checkClosestFirst(new TestPoint2D(-1, 1), root, Order.MINUS_NODE_PLUS);
        checkClosestFirst(new TestPoint2D(-1, 1), root.getMinus(), Order.MINUS_NODE_PLUS);
        checkClosestFirst(new TestPoint2D(-1, 1), root.getPlus(), Order.MINUS_NODE_PLUS);

        checkClosestFirst(new TestPoint2D(-1, -1), root, Order.PLUS_NODE_MINUS);
        checkClosestFirst(new TestPoint2D(-1, -1), root.getMinus(), Order.MINUS_NODE_PLUS);
        checkClosestFirst(new TestPoint2D(-1, -1), root.getPlus(), Order.MINUS_NODE_PLUS);

        checkClosestFirst(new TestPoint2D(1, -1), root, Order.PLUS_NODE_MINUS);
        checkClosestFirst(new TestPoint2D(1, -1), root.getMinus(), Order.PLUS_NODE_MINUS);
        checkClosestFirst(new TestPoint2D(1, -1), root.getPlus(), Order.PLUS_NODE_MINUS);

        checkClosestFirst(TestPoint2D.ZERO, root.getPlus(), Order.MINUS_NODE_PLUS);
        checkClosestFirst(TestPoint2D.ZERO, root.getPlus(), Order.MINUS_NODE_PLUS);
        checkClosestFirst(TestPoint2D.ZERO, root.getPlus(), Order.MINUS_NODE_PLUS);
    }

    @Test
    public void testFarthestFirst() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestNode root = tree.getRoot();
        root.cut(TestLine.X_AXIS);
        root.getMinus().cut(TestLine.Y_AXIS);
        root.getPlus().cut(TestLine.Y_AXIS);

        // act
        checkFarthestFirst(new TestPoint2D(1, 1), root, Order.PLUS_NODE_MINUS);
        checkFarthestFirst(new TestPoint2D(1, 1), root.getMinus(), Order.MINUS_NODE_PLUS);
        checkFarthestFirst(new TestPoint2D(1, 1), root.getPlus(), Order.MINUS_NODE_PLUS);

        checkFarthestFirst(new TestPoint2D(-1, 1), root, Order.PLUS_NODE_MINUS);
        checkFarthestFirst(new TestPoint2D(-1, 1), root.getMinus(), Order.PLUS_NODE_MINUS);
        checkFarthestFirst(new TestPoint2D(-1, 1), root.getPlus(), Order.PLUS_NODE_MINUS);

        checkFarthestFirst(new TestPoint2D(-1, -1), root, Order.MINUS_NODE_PLUS);
        checkFarthestFirst(new TestPoint2D(-1, -1), root.getMinus(), Order.PLUS_NODE_MINUS);
        checkFarthestFirst(new TestPoint2D(-1, -1), root.getPlus(), Order.PLUS_NODE_MINUS);

        checkFarthestFirst(new TestPoint2D(1, -1), root, Order.MINUS_NODE_PLUS);
        checkFarthestFirst(new TestPoint2D(1, -1), root.getMinus(), Order.MINUS_NODE_PLUS);
        checkFarthestFirst(new TestPoint2D(1, -1), root.getPlus(), Order.MINUS_NODE_PLUS);

        checkFarthestFirst(TestPoint2D.ZERO, root.getPlus(), Order.MINUS_NODE_PLUS);
        checkFarthestFirst(TestPoint2D.ZERO, root.getPlus(), Order.MINUS_NODE_PLUS);
        checkFarthestFirst(TestPoint2D.ZERO, root.getPlus(), Order.MINUS_NODE_PLUS);
    }

    private static void checkClosestFirst(TestPoint2D target, TestNode node, Order order) {
        ClosestFirstStubVisitor visitor = new ClosestFirstStubVisitor(target);

        Assert.assertSame(target, visitor.getTarget());
        Assert.assertEquals(order, visitor.visitOrder(node));
    }

    private static void checkFarthestFirst(TestPoint2D target, TestNode node, Order order) {
        FarthestFirstStubVisitor visitor = new FarthestFirstStubVisitor(target);

        Assert.assertSame(target, visitor.getTarget());
        Assert.assertEquals(order, visitor.visitOrder(node));
    }

    private static class ClosestFirstStubVisitor extends ClosestFirstVisitor<TestPoint2D, TestNode> {

        private static final long serialVersionUID = 1L;

        public ClosestFirstStubVisitor(TestPoint2D target) {
            super(target);
        }

        @Override
        public void visit(TestNode node) {
        }
    }

    private static class FarthestFirstStubVisitor extends FarthestFirstVisitor<TestPoint2D, TestNode> {

        private static final long serialVersionUID = 1L;

        public FarthestFirstStubVisitor(TestPoint2D target) {
            super(target);
        }

        @Override
        public void visit(TestNode node) {
        }
    }
}