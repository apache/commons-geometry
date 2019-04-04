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
package org.apache.commons.geometry.core.partition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.partition.BSPTree.NodeCutRule;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestBSPTree;
import org.apache.commons.geometry.core.partition.test.TestBSPTree.TestNode;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestLineSegment;
import org.apache.commons.geometry.core.partition.test.TestLineSegmentCollection;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Test;

public class AbstractBSPTreeTest {

    @Test
    public void testInitialization() {
        // act
        TestBSPTree tree = new TestBSPTree();

        // assert
        TestNode root = tree.getRoot();

        Assert.assertNotNull(root);
        Assert.assertNull(root.getParent());

        PartitionTestUtils.assertIsLeafNode(root);
        Assert.assertFalse(root.isPlus());
        Assert.assertFalse(root.isMinus());

        Assert.assertSame(tree, root.getTree());
    }

    @Test
    public void testNodeStateGetters() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode root = tree.getRoot();
        root.cut(TestLine.X_AXIS);

        TestNode plus = root.getPlus();
        TestNode minus = root.getMinus();

        // act/assert
        Assert.assertFalse(root.isLeaf());
        Assert.assertTrue(root.isInternal());
        Assert.assertFalse(root.isPlus());
        Assert.assertFalse(root.isMinus());

        Assert.assertTrue(plus.isLeaf());
        Assert.assertFalse(plus.isInternal());
        Assert.assertTrue(plus.isPlus());
        Assert.assertFalse(plus.isMinus());

        Assert.assertTrue(minus.isLeaf());
        Assert.assertFalse(minus.isInternal());
        Assert.assertFalse(minus.isPlus());
        Assert.assertTrue(minus.isMinus());
    }

    @Test
    public void testInsertCut() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestLine line = TestLine.X_AXIS;

        // act
        boolean result = tree.getRoot().insertCut(line);

        // assert
        Assert.assertTrue(result);

        TestNode root = tree.getRoot();
        PartitionTestUtils.assertIsInternalNode(root);

        Assert.assertSame(line, root.getCut().getHyperplane());

        PartitionTestUtils.assertIsLeafNode(root.getMinus());
        PartitionTestUtils.assertIsLeafNode(root.getPlus());
    }

    @Test
    public void testInsertCut_fitsCutterToCell() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS)
                .getPlus();

        // act
        boolean result = node.insertCut(new TestLine(0.5, 1.5, 1.5, 0.5));

        // assert
        Assert.assertTrue(result);
        PartitionTestUtils.assertIsInternalNode(node);

        TestLineSegment segment = (TestLineSegment) node.getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), segment.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), segment.getEndPoint());
    }

    @Test
    public void testInsertCut_doesNotPassThroughCell_intersects() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus();

        // act
        boolean result = node.insertCut(new TestLine(-2, 0, 0, -2));

        // assert
        Assert.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    public void testInsertCut_doesNotPassThroughCell_parallel() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus();

        // act
        boolean result = node.insertCut(new TestLine(0, -1, 1, -1));

        // assert
        Assert.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    public void testInsertCut_doesNotPassThroughCell_removesExistingChildren() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus()
                        .cut(new TestLine(0, 2, 2, 0));

        // act
        boolean result = node.insertCut(new TestLine(-2, 0, 0, -2));

        // assert
        Assert.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    public void testInsertCut_cutExistsInTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode node = tree.getRoot()
                .cut(TestLine.X_AXIS)
                    .getMinus()
                        .cut(TestLine.Y_AXIS)
                        .getPlus()
                            .cut(new TestLine(0, 2, 2, 0));

        // act
        boolean result = node.insertCut(new TestLine(0, 2, 0, 3));

        // assert
        Assert.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    public void testClearCut_cutExists() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        boolean result = node.clearCut();

        // assert
        Assert.assertTrue(result);
        Assert.assertTrue(node.isLeaf());
        Assert.assertNull(node.getPlus());
        Assert.assertNull(node.getMinus());
    }

    @Test
    public void testClearCut_cutDoesNotExist() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                .cut(TestLine.Y_AXIS)
                .getMinus();

        // act
        boolean result = node.clearCut();

        // assert
        Assert.assertFalse(result);
        Assert.assertTrue(node.isLeaf());
        Assert.assertNull(node.getPlus());
        Assert.assertNull(node.getMinus());
    }

    @Test
    public void testClearCut_root_fullTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                .cut(TestLine.Y_AXIS)
                .getMinus();

        // act
        boolean result = tree.getRoot().clearCut();

        // assert
        Assert.assertTrue(result);
        Assert.assertTrue(node.isLeaf());
        Assert.assertNull(node.getPlus());
        Assert.assertNull(node.getMinus());

        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testClearCut_root_emptyTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestNode node = tree.getRoot();

        // act
        boolean result = node.clearCut();

        // assert
        Assert.assertFalse(result);
        Assert.assertTrue(node.isLeaf());
        Assert.assertNull(node.getPlus());
        Assert.assertNull(node.getMinus());

        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testFindNode_emptyTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestNode root = tree.getRoot();

        List<TestPoint2D> testPoints = Arrays.asList(
                    new TestPoint2D(0, 0),
                    new TestPoint2D(1, 0),
                    new TestPoint2D(1, 1),
                    new TestPoint2D(0, 1),
                    new TestPoint2D(-1, 1),
                    new TestPoint2D(-1, 0),
                    new TestPoint2D(-1, -1),
                    new TestPoint2D(0, -1),
                    new TestPoint2D(1, -1)
                );

        // act/assert
        for (TestPoint2D pt : testPoints) {
            Assert.assertSame(root, tree.findNode(pt));
        }

        for (TestPoint2D pt : testPoints) {
            Assert.assertSame(root, tree.findNode(pt, NodeCutRule.NODE));
        }

        for (TestPoint2D pt : testPoints) {
            Assert.assertSame(root, tree.findNode(pt, NodeCutRule.MINUS));
        }

        for (TestPoint2D pt : testPoints) {
            Assert.assertSame(root, tree.findNode(pt, NodeCutRule.PLUS));
        }
    }

    @Test
    public void testFindNode_singleArg() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        tree.getRoot()
                .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus()
                        .cut(new TestLine(0, 2, 2, 0));

        TestNode root = tree.getRoot();
        TestNode minusY = root.getPlus();

        TestNode yCut = root.getMinus();
        TestNode minusXPlusY = yCut.getMinus();

        TestNode diagonalCut = yCut.getPlus();
        TestNode underDiagonal = diagonalCut.getPlus();
        TestNode aboveDiagonal = diagonalCut.getMinus();

        // act/assert
        Assert.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(0, 0)));

        Assert.assertSame(underDiagonal, tree.findNode(new TestPoint2D(1, 0)));
        Assert.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(1, 1)));
        Assert.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(0, 1)));
        Assert.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 1)));
        Assert.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 0)));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(-1, -1)));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(0, -1)));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(1, -1)));

        Assert.assertSame(underDiagonal, tree.findNode(new TestPoint2D(0.5, 0.5)));
        Assert.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(3, 3)));
    }

    @Test
    public void testFindNode_nodeCutBehavior() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        tree.getRoot()
                .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus()
                        .cut(new TestLine(0, 2, 2, 0));

        TestNode root = tree.getRoot();
        TestNode minusY = root.getPlus();

        TestNode yCut = root.getMinus();
        TestNode minusXPlusY = yCut.getMinus();

        TestNode diagonalCut = yCut.getPlus();
        TestNode underDiagonal = diagonalCut.getPlus();
        TestNode aboveDiagonal = diagonalCut.getMinus();

        NodeCutRule cutBehavior = NodeCutRule.NODE;

        // act/assert
        Assert.assertSame(root, tree.findNode(new TestPoint2D(0, 0), cutBehavior));

        Assert.assertSame(root, tree.findNode(new TestPoint2D(1, 0), cutBehavior));
        Assert.assertSame(diagonalCut, tree.findNode(new TestPoint2D(1, 1), cutBehavior));
        Assert.assertSame(yCut, tree.findNode(new TestPoint2D(0, 1), cutBehavior));
        Assert.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 1), cutBehavior));
        Assert.assertSame(root, tree.findNode(new TestPoint2D(-1, 0), cutBehavior));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(-1, -1), cutBehavior));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(0, -1), cutBehavior));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(1, -1), cutBehavior));

        Assert.assertSame(underDiagonal, tree.findNode(new TestPoint2D(0.5, 0.5), cutBehavior));
        Assert.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(3, 3), cutBehavior));
    }

    @Test
    public void testFindNode_minusCutBehavior() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        tree.getRoot()
                .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus()
                        .cut(new TestLine(0, 2, 2, 0));

        TestNode root = tree.getRoot();
        TestNode minusY = root.getPlus();

        TestNode yCut = root.getMinus();
        TestNode minusXPlusY = yCut.getMinus();

        TestNode diagonalCut = yCut.getPlus();
        TestNode underDiagonal = diagonalCut.getPlus();
        TestNode aboveDiagonal = diagonalCut.getMinus();

        NodeCutRule cutBehavior = NodeCutRule.MINUS;

        // act/assert
        Assert.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(0, 0), cutBehavior));

        Assert.assertSame(underDiagonal, tree.findNode(new TestPoint2D(1, 0), cutBehavior));
        Assert.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(1, 1), cutBehavior));
        Assert.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(0, 1), cutBehavior));
        Assert.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 1), cutBehavior));
        Assert.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 0), cutBehavior));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(-1, -1), cutBehavior));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(0, -1), cutBehavior));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(1, -1), cutBehavior));

        Assert.assertSame(underDiagonal, tree.findNode(new TestPoint2D(0.5, 0.5), cutBehavior));
        Assert.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(3, 3), cutBehavior));
    }

    @Test
    public void testFindNode_plusCutBehavior() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        tree.getRoot()
                .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus()
                        .cut(new TestLine(0, 2, 2, 0));

        TestNode root = tree.getRoot();
        TestNode minusY = root.getPlus();

        TestNode yCut = root.getMinus();
        TestNode minusXPlusY = yCut.getMinus();

        TestNode diagonalCut = yCut.getPlus();
        TestNode underDiagonal = diagonalCut.getPlus();
        TestNode aboveDiagonal = diagonalCut.getMinus();

        NodeCutRule cutBehavior = NodeCutRule.PLUS;

        // act/assert
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(0, 0), cutBehavior));

        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(1, 0), cutBehavior));
        Assert.assertSame(underDiagonal, tree.findNode(new TestPoint2D(1, 1), cutBehavior));
        Assert.assertSame(underDiagonal, tree.findNode(new TestPoint2D(0, 1), cutBehavior));
        Assert.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 1), cutBehavior));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(-1, 0), cutBehavior));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(-1, -1), cutBehavior));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(0, -1), cutBehavior));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(1, -1), cutBehavior));

        Assert.assertSame(underDiagonal, tree.findNode(new TestPoint2D(0.5, 0.5), cutBehavior));
        Assert.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(3, 3), cutBehavior));
    }

    @Test
    public void testInsert_convex_emptyTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        // act
        tree.insert(new TestLineSegment(1, 0, 1, 1));

        // assert
        TestNode root = tree.getRoot();
        Assert.assertFalse(root.isLeaf());
        Assert.assertTrue(root.getMinus().isLeaf());
        Assert.assertTrue(root.getPlus().isLeaf());

        TestLineSegment seg = (TestLineSegment) root.getCut();
        PartitionTestUtils.assertPointsEqual(
                new TestPoint2D(1, Double.NEGATIVE_INFINITY),
                seg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(
                new TestPoint2D(1, Double.POSITIVE_INFINITY),
                seg.getEndPoint());
    }

    @Test
    public void testInsert_convex_noSplit() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        tree.insert(new TestLineSegment(0.5, 1.5, 1.5, 0.5));

        // assert
        TestNode root = tree.getRoot();
        Assert.assertFalse(root.isLeaf());

        TestNode node = tree.findNode(new TestPoint2D(0.5, 0.5));
        TestLineSegment seg = (TestLineSegment) node.getParent().getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), seg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), seg.getEndPoint());

        Assert.assertTrue(tree.getRoot().getPlus().isLeaf());
        Assert.assertTrue(tree.getRoot().getMinus().getMinus().isLeaf());
    }

    @Test
    public void testInsert_convex_split() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        tree.insert(new TestLineSegment(-0.5, 2.5, 2.5, -0.5));

        // assert
        TestNode root = tree.getRoot();
        Assert.assertFalse(root.isLeaf());

        TestNode plusXPlusY = tree.getRoot().getMinus().getPlus();
        TestLineSegment plusXPlusYSeg = (TestLineSegment) plusXPlusY.getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), plusXPlusYSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), plusXPlusYSeg.getEndPoint());

        TestNode minusY = tree.getRoot().getPlus();
        TestLineSegment minusYSeg = (TestLineSegment) minusY.getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), minusYSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(
                new TestPoint2D(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), minusYSeg.getEndPoint());

        TestNode minusXPlusY = tree.getRoot().getMinus().getMinus();
        TestLineSegment minusXPlusYSeg = (TestLineSegment) minusXPlusY.getCut();

        PartitionTestUtils.assertPointsEqual(
                new TestPoint2D(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), minusXPlusYSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), minusXPlusYSeg.getEndPoint());
    }

    @Test
    public void testInsert_convexList_convexRegion() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestLineSegment a = new TestLineSegment(0, 0, 1, 0);
        TestLineSegment b = new TestLineSegment(1, 0, 0, 1);
        TestLineSegment c = new TestLineSegment(0, 1, 0, 0);

        // act
        tree.insert(Arrays.asList(a, b, c));

        // assert
        List<TestLineSegment> segments = getLineSegments(tree);

        Assert.assertEquals(3, segments.size());

        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                segments.get(0));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(-Math.sqrt(0.5), Double.POSITIVE_INFINITY, new TestLine(1, 0, 0, 1)),
                segments.get(1));
        PartitionTestUtils.assertSegmentsEqual(c, segments.get(2));
    }

    @Test
    public void testInsert_convexList_concaveRegion() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestLineSegment a = new TestLineSegment(-1, -1, 1, -1);
        TestLineSegment b = new TestLineSegment(1, -1, 0, 0);
        TestLineSegment c = new TestLineSegment(0, 0, 1, 1);
        TestLineSegment d = new TestLineSegment(1, 1, -1, 1);
        TestLineSegment e = new TestLineSegment(-1, 1, -1, -1);

        // act
        tree.insert(Arrays.asList(a, b, c, d, e));

        // assert
        List<TestLineSegment> segments = getLineSegments(tree);

        Assert.assertEquals(5, segments.size());

        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new TestLine(-1, -1, 1, -1)),
                segments.get(0));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(-Math.sqrt(2), Double.POSITIVE_INFINITY, new TestLine(1, -1, 0, 0)),
                segments.get(1));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(-1, 1, -1, -1),
                segments.get(2));

        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(0, Double.POSITIVE_INFINITY, new TestLine(0, 0, 1, 1)),
                segments.get(3));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(1, 1, -1, 1),
                segments.get(4));
    }

    @Test
    public void testInsert_subhyperplane_concaveRegion() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestLineSegment a = new TestLineSegment(-1, -1, 1, -1);
        TestLineSegment b = new TestLineSegment(1, -1, 0, 0);
        TestLineSegment c = new TestLineSegment(0, 0, 1, 1);
        TestLineSegment d = new TestLineSegment(1, 1, -1, 1);
        TestLineSegment e = new TestLineSegment(-1, 1, -1, -1);

        TestLineSegmentCollection coll = new TestLineSegmentCollection(
                Arrays.asList(a, b, c, d, e));

        // act
        tree.insert(coll);

        // assert
        List<TestLineSegment> segments = getLineSegments(tree);

        Assert.assertEquals(5, segments.size());

        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, new TestLine(-1, -1, 1, -1)),
                segments.get(0));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(-Math.sqrt(2), Double.POSITIVE_INFINITY, new TestLine(1, -1, 0, 0)),
                segments.get(1));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(-1, 1, -1, -1),
                segments.get(2));

        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(0, Double.POSITIVE_INFINITY, new TestLine(0, 0, 1, 1)),
                segments.get(3));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(1, 1, -1, 1),
                segments.get(4));
    }

    @Test
    public void testCount() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        // act/assert
        Assert.assertEquals(1, tree.count());
        Assert.assertEquals(1, tree.getRoot().count());

        tree.getRoot().insertCut(TestLine.X_AXIS);
        Assert.assertEquals(1, tree.getRoot().getMinus().count());
        Assert.assertEquals(1, tree.getRoot().getPlus().count());
        Assert.assertEquals(3, tree.count());

        tree.getRoot().getPlus().insertCut(TestLine.Y_AXIS);
        Assert.assertEquals(1, tree.getRoot().getMinus().count());
        Assert.assertEquals(3, tree.getRoot().getPlus().count());
        Assert.assertEquals(5, tree.count());

        tree.getRoot().getMinus().insertCut(TestLine.Y_AXIS);
        Assert.assertEquals(3, tree.getRoot().getMinus().count());
        Assert.assertEquals(3, tree.getRoot().getPlus().count());
        Assert.assertEquals(7, tree.count());

        tree.getRoot().getMinus().insertCut(new TestLine(new TestPoint2D(-1, -1), new TestPoint2D(1, -1)));
        Assert.assertEquals(1, tree.getRoot().getMinus().count());
        Assert.assertEquals(3, tree.getRoot().getPlus().count());
        Assert.assertEquals(5, tree.count());
    }

    @Test
    public void testHeight() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        // act/assert
        Assert.assertEquals(0, tree.height());
        Assert.assertEquals(0, tree.getRoot().height());

        tree.getRoot().insertCut(TestLine.X_AXIS);
        Assert.assertEquals(0, tree.getRoot().getMinus().height());
        Assert.assertEquals(0, tree.getRoot().getPlus().height());
        Assert.assertEquals(1, tree.height());

        tree.getRoot().getPlus().insertCut(TestLine.Y_AXIS);
        Assert.assertEquals(0, tree.getRoot().getMinus().height());
        Assert.assertEquals(1, tree.getRoot().getPlus().height());
        Assert.assertEquals(2, tree.height());

        tree.getRoot().getMinus().insertCut(TestLine.Y_AXIS);
        Assert.assertEquals(1, tree.getRoot().getMinus().height());
        Assert.assertEquals(1, tree.getRoot().getPlus().height());
        Assert.assertEquals(2, tree.height());

        tree.getRoot().getMinus().clearCut();
        Assert.assertEquals(0, tree.getRoot().getMinus().height());
        Assert.assertEquals(1, tree.getRoot().getPlus().height());
        Assert.assertEquals(2, tree.height());

        tree.getRoot().getPlus().getPlus()
            .insertCut(new TestLine(new TestPoint2D(0, -1), new TestPoint2D(1, -1)));

        Assert.assertEquals(0, tree.getRoot().getMinus().height());
        Assert.assertEquals(2, tree.getRoot().getPlus().height());
        Assert.assertEquals(3, tree.height());
    }

    @Test
    public void testDepth() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode root = tree.getRoot();
        root.cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS);

        // act/assert
        Assert.assertEquals(0, root.depth());

        Assert.assertEquals(1, root.getPlus().depth());

        Assert.assertEquals(1, root.getMinus().depth());
        Assert.assertEquals(2, root.getMinus().getPlus().depth());
        Assert.assertEquals(2, root.getMinus().getMinus().depth());
    }

    @Test
    public void testIterable_emptyTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        List<TestNode> nodes = new ArrayList<>();

        // act
        for (TestNode node : tree)
        {
            nodes.add(node);
        }

        // assert
        Assert.assertEquals(1, nodes.size());
        Assert.assertSame(tree.getRoot(), nodes.get(0));
    }

    @Test
    public void testIterable_multipleNodes() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode root = tree.getRoot();
        root.cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                 .getParent()
                 .getPlus()
                     .cut(TestLine.Y_AXIS);

        List<TestNode> nodes = new ArrayList<>();

        // act
        for (TestNode node : tree)
        {
            nodes.add(node);
        }

        // assert
        Assert.assertEquals(7, nodes.size());
        Assert.assertSame(root, nodes.get(0));

        Assert.assertSame(root.getMinus(), nodes.get(1));
        Assert.assertSame(root.getMinus().getMinus(), nodes.get(2));
        Assert.assertSame(root.getMinus().getPlus(), nodes.get(3));

        Assert.assertSame(root.getPlus(), nodes.get(4));
        Assert.assertSame(root.getPlus().getMinus(), nodes.get(5));
        Assert.assertSame(root.getPlus().getPlus(), nodes.get(6));
    }

    @Test
    public void testStream_emptyTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        // act
        List<TestNode> nodes = tree.stream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(1, nodes.size());
        Assert.assertSame(tree.getRoot(), nodes.get(0));
    }

    @Test
    public void testStream_multipleNodes() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode root = tree.getRoot();
        root.cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                 .getParent()
                 .getPlus()
                     .cut(TestLine.Y_AXIS);

        // act
        List<TestNode> nodes = tree.stream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(7, nodes.size());
        Assert.assertSame(root, nodes.get(0));

        Assert.assertSame(root.getMinus(), nodes.get(1));
        Assert.assertSame(root.getMinus().getMinus(), nodes.get(2));
        Assert.assertSame(root.getMinus().getPlus(), nodes.get(3));

        Assert.assertSame(root.getPlus(), nodes.get(4));
        Assert.assertSame(root.getPlus().getMinus(), nodes.get(5));
        Assert.assertSame(root.getPlus().getPlus(), nodes.get(6));
    }

    @Test
    public void testNodeIterable_singleNodeSubtree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestNode node = tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS)
                .getMinus();

        List<TestNode> nodes = new ArrayList<>();
        // act
        for (TestNode n : node)
        {
            nodes.add(n);
        }

        // assert
        Assert.assertEquals(1, nodes.size());
        Assert.assertSame(node, nodes.get(0));
    }

    @Test
    public void testNodeIterable_multipleNodeSubtree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestNode node = tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        List<TestNode> nodes = new ArrayList<>();
        // act
        for (TestNode n : node)
        {
            nodes.add(n);
        }

        // assert
        Assert.assertEquals(3, nodes.size());
        Assert.assertSame(node, nodes.get(0));
        Assert.assertSame(node.getMinus(), nodes.get(1));
        Assert.assertSame(node.getPlus(), nodes.get(2));
    }

    @Test
    public void testNodeStream_singleNodeSubtree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestNode node = tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS)
                .getMinus();

        // act
        List<TestNode> nodes = node.stream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(1, nodes.size());
        Assert.assertSame(node, nodes.get(0));
    }

    @Test
    public void testNodeStream_multipleNodeSubtree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestNode node = tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        List<TestNode> nodes = node.stream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(3, nodes.size());
        Assert.assertSame(node, nodes.get(0));
        Assert.assertSame(node.getMinus(), nodes.get(1));
        Assert.assertSame(node.getPlus(), nodes.get(2));
    }

    @Test
    public void testCopy_rootOnly() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        // act
        TestBSPTree copy = new TestBSPTree();
        copy.copy(tree);

        // assert
        Assert.assertNotSame(tree, copy);
        Assert.assertNotSame(tree.getRoot(), copy.getRoot());

        Assert.assertEquals(tree.count(), copy.count());
    }

    @Test
    public void testCopy_withCuts() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        TestBSPTree copy = new TestBSPTree();
        copy.copy(tree);

        // assert
        Assert.assertNotSame(tree, copy);

        assertNodesCopiedRecursive(tree.getRoot(), copy.getRoot());

        Assert.assertEquals(tree.count(), copy.count());
    }

    @Test
    public void testCopy_changesToOneTreeDoNotAffectCopy() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        TestBSPTree copy = new TestBSPTree();
        copy.copy(tree);
        tree.getRoot().clearCut();

        // assert
        Assert.assertEquals(1, tree.count());
        Assert.assertEquals(5, copy.count());
    }

    @Test
    public void testCopy_instancePassedAsArgument() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        tree.copy(tree);

        // assert
        Assert.assertEquals(5, tree.count());
    }

    @Test
    public void testExtract_singleNodeTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestBSPTree result = new TestBSPTree();
        result.getRoot().insertCut(TestLine.X_AXIS);

        // act
        result.extract(tree.getRoot());

        // assert
        Assert.assertNotSame(tree.getRoot(), result.getRoot());
        Assert.assertEquals(1, tree.count());
        Assert.assertEquals(1, result.count());

        PartitionTestUtils.assertTreeStructure(tree);
        PartitionTestUtils.assertTreeStructure(result);
    }

    @Test
    public void testExtract_clearsExistingNodesInCallingTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestBSPTree result = new TestBSPTree();
        result.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        // act
        result.extract(tree.getRoot());

        // assert
        Assert.assertNotSame(tree.getRoot(), result.getRoot());
        Assert.assertEquals(1, tree.count());
        Assert.assertEquals(1, result.count());

        PartitionTestUtils.assertTreeStructure(tree);
        PartitionTestUtils.assertTreeStructure(result);
    }

    @Test
    public void testExtract_internalNode() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(1, 2), new TestPoint2D(2, 1)),
                    new TestLineSegment(new TestPoint2D(-1, 2), new TestPoint2D(-2, 1)),
                    new TestLineSegment(new TestPoint2D(0, -2), new TestPoint2D(1, -2))
                ));

        TestBSPTree result = new TestBSPTree();

        // act
        result.extract(tree.getRoot().getPlus());

        // assert
        Assert.assertEquals(7, result.count());

        List<TestLineSegment> resultSegments = getLineSegments(result);
        Assert.assertEquals(3, resultSegments.size());

        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                resultSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(Double.NEGATIVE_INFINITY, 0, TestLine.Y_AXIS),
                resultSegments.get(1));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(0, Double.POSITIVE_INFINITY, new TestLine(new TestPoint2D(0, -2), new TestPoint2D(1, -2))),
                resultSegments.get(2));

        Assert.assertEquals(13, tree.count());

        List<TestLineSegment> inputSegment = getLineSegments(tree);
        Assert.assertEquals(6, inputSegment.size());

        PartitionTestUtils.assertTreeStructure(tree);
        PartitionTestUtils.assertTreeStructure(result);
    }

    @Test
    public void testExtract_leafNode() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(1, 2), new TestPoint2D(2, 1)),
                    new TestLineSegment(new TestPoint2D(-1, 2), new TestPoint2D(-2, 1)),
                    new TestLineSegment(new TestPoint2D(0, -2), new TestPoint2D(1, -2))
                ));

        TestPoint2D pt = new TestPoint2D(1, 1);

        TestNode node = tree.findNode(pt);
        TestBSPTree result = new TestBSPTree();

        // act
        result.extract(node);

        // assert
        TestNode resultNode = result.findNode(pt);
        Assert.assertNotNull(resultNode);
        Assert.assertNotSame(node, resultNode);

        Assert.assertEquals(7, result.count());

        List<TestLineSegment> resultSegments = getLineSegments(result);
        Assert.assertEquals(3, resultSegments.size());

        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                resultSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.Y_AXIS),
                resultSegments.get(1));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(new TestPoint2D(0, 3), new TestPoint2D(3, 0)),
                resultSegments.get(2));

        Assert.assertEquals(13, tree.count());

        List<TestLineSegment> inputSegment = getLineSegments(tree);
        Assert.assertEquals(6, inputSegment.size());

        PartitionTestUtils.assertTreeStructure(tree);
        PartitionTestUtils.assertTreeStructure(result);
    }

    @Test
    public void testExtract_extractFromSameTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(1, 2), new TestPoint2D(2, 1)),
                    new TestLineSegment(new TestPoint2D(-1, 2), new TestPoint2D(-2, 1)),
                    new TestLineSegment(new TestPoint2D(0, -2), new TestPoint2D(1, -2))
                ));

        TestPoint2D pt = new TestPoint2D(1, 1);

        TestNode node = tree.findNode(pt);

        // act
        tree.extract(node);

        // assert
        TestNode resultNode = tree.findNode(pt);
        Assert.assertNotNull(resultNode);
        Assert.assertNotSame(node, resultNode);

        Assert.assertEquals(7, tree.count());

        List<TestLineSegment> resultSegments = getLineSegments(tree);
        Assert.assertEquals(3, resultSegments.size());

        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                resultSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.Y_AXIS),
                resultSegments.get(1));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(new TestPoint2D(0, 3), new TestPoint2D(3, 0)),
                resultSegments.get(2));

        PartitionTestUtils.assertTreeStructure(tree);
    }

    @Test
    public void testTransform_singleNodeTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        Transform<TestPoint2D> t = (p) -> new TestPoint2D(p.getX(), p.getY() + 2);

        // act
        tree.transform(t);

        // assert
        Assert.assertEquals(1, tree.count());
        Assert.assertTrue(tree.getRoot().isLeaf());
    }

    @Test
    public void testTransform_singleCut() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().insertCut(TestLine.X_AXIS);

        Transform<TestPoint2D> t = (p) -> new TestPoint2D(p.getX(), p.getY() + 2);

        // act
        tree.transform(t);

        // assert
        Assert.assertEquals(3, tree.count());

        List<TestLineSegment> segments = getLineSegments(tree);
        Assert.assertEquals(1, segments.size());

        TestLineSegment seg = segments.get(0);

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.NEGATIVE_INFINITY, 2), seg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.POSITIVE_INFINITY, 2), seg.getEndPoint());
    }

    @Test
    public void testTransform_multipleCuts() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(-1, -1), new TestPoint2D(1, 1)),
                    new TestLineSegment(new TestPoint2D(3, 1), new TestPoint2D(3, 2))
                ));

        Transform<TestPoint2D> t = (p) -> new TestPoint2D(0.5 * p.getX(), p.getY() + 2);

        // act
        tree.transform(t);

        // assert
        Assert.assertEquals(9, tree.count());

        List<TestLineSegment> segments = getLineSegments(tree);
        Assert.assertEquals(4, segments.size());

        TestLineSegment segment1 = segments.get(0);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.NEGATIVE_INFINITY, 2), segment1.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.POSITIVE_INFINITY, 2), segment1.getEndPoint());

        TestLineSegment segment2 = segments.get(1);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), segment2.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), segment2.getEndPoint());

        TestLineSegment segment3 = segments.get(2);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(1.5, 2), segment3.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(1.5, 5), segment3.getEndPoint());

        TestLineSegment segment4 = segments.get(3);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), segment4.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), segment4.getEndPoint());
    }

    @Test
    public void testTransform_xAxisReflection() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(0, 3), new TestPoint2D(3, 0))
                ));

        Transform<TestPoint2D> t = (p) -> new TestPoint2D(-p.getX(), p.getY());

        Map<TestPoint2D, TestNode> pointNodeMap = createPointNodeMap(tree, -5, 5);

        // act
        tree.transform(t);

        // assert
        checkTransformedPointNodeMap(tree, t, pointNodeMap);

        List<TestLineSegment> segments = getLineSegments(tree);
        Assert.assertEquals(4, segments.size());
    }

    @Test
    public void testTransform_yAxisReflection() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(0, 3), new TestPoint2D(3, 0))
                ));

        Transform<TestPoint2D> t = (p) -> new TestPoint2D(p.getX(), -p.getY());

        Map<TestPoint2D, TestNode> pointNodeMap = createPointNodeMap(tree, -5, 5);

        // act
        tree.transform(t);

        // assert
        checkTransformedPointNodeMap(tree, t, pointNodeMap);

        List<TestLineSegment> segments = getLineSegments(tree);
        Assert.assertEquals(4, segments.size());
    }

    @Test
    public void testTransform_xAndYAxisReflection() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(0, 3), new TestPoint2D(3, 0))
                ));

        Transform<TestPoint2D> t = (p) -> new TestPoint2D(-p.getX(), -p.getY());

        Map<TestPoint2D, TestNode> pointNodeMap = createPointNodeMap(tree, -5, 5);

        // act
        tree.transform(t);

        // assert
        checkTransformedPointNodeMap(tree, t, pointNodeMap);

        List<TestLineSegment> segments = getLineSegments(tree);
        Assert.assertEquals(4, segments.size());
    }

    @Test
    public void testTreeString() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        // act
        String str = tree.treeString();

        // assert
        String[] lines = str.split("\n");
        Assert.assertEquals(5, lines.length);
        Assert.assertTrue(lines[0].startsWith("TestNode[cut= TestLineSegment"));
        Assert.assertTrue(lines[1].startsWith("    [-] TestNode[cut= TestLineSegment"));
        Assert.assertEquals("        [-] TestNode[cut= null]", lines[2]);
        Assert.assertEquals("        [+] TestNode[cut= null]", lines[3]);
        Assert.assertEquals("    [+] TestNode[cut= null]", lines[4]);
    }

    @Test
    public void testTreeString_emptyTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        // act
        String str = tree.treeString();

        // assert
        Assert.assertEquals("TestNode[cut= null]", str);
    }

    @Test
    public void testTreeString_reachesMaxDepth() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS)
            .getMinus().cut(new TestLine(new TestPoint2D(-2, 1), new TestPoint2D(0, 1)));

        // act
        String str = tree.treeString(1);

        // assert
        String[] lines = str.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertTrue(lines[0].startsWith("TestNode[cut= TestLineSegment"));
        Assert.assertTrue(lines[1].startsWith("    [-] TestNode[cut= TestLineSegment"));
        Assert.assertEquals("        ...", lines[2]);
        Assert.assertEquals("    [+] TestNode[cut= null]", lines[3]);
    }

    @Test
    public void testTreeString_zeroMaxDepth() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS)
            .getMinus().cut(new TestLine(new TestPoint2D(-2, 1), new TestPoint2D(0, 1)));

        // act
        String str = tree.treeString(0);

        // assert
        String[] lines = str.split("\n");
        Assert.assertEquals(2, lines.length);
        Assert.assertTrue(lines[0].startsWith("TestNode[cut= TestLineSegment"));
        Assert.assertTrue(lines[1].startsWith("    ..."));
    }

    @Test
    public void testTreeString_negativeMaxDepth() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS)
            .getMinus().cut(new TestLine(new TestPoint2D(-2, 1), new TestPoint2D(0, 1)));

        // act
        String str = tree.treeString(-1);

        // assert
        Assert.assertEquals("", str);
    }

    @Test
    public void testToString() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().insertCut(TestLine.Y_AXIS);

        // act
        String str = tree.toString();

        // assert
        String msg = "Unexpected toString() representation: " + str;

        Assert.assertTrue(msg, str.contains("TestBSPTree"));
        Assert.assertTrue(msg, str.contains("count= 3"));
        Assert.assertTrue(msg, str.contains("height= 1"));
    }

    @Test
    public void testNodeToString() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS);

        // act
        String str = tree.getRoot().toString();

        // assert
        Assert.assertTrue(str.contains("TestNode"));
        Assert.assertTrue(str.contains("cut= TestLineSegment"));
    }

    private void assertNodesCopiedRecursive(final TestNode orig, final TestNode copy) {
        Assert.assertNotSame(orig, copy);

        Assert.assertEquals(orig.getCut(), copy.getCut());

        if (!orig.isLeaf())
        {
            Assert.assertNotSame(orig.getMinus(), copy.getMinus());
            Assert.assertNotSame(orig.getPlus(), copy.getPlus());

            assertNodesCopiedRecursive(orig.getMinus(), copy.getMinus());
            assertNodesCopiedRecursive(orig.getPlus(), copy.getPlus());
        }
        else {
            Assert.assertNull(copy.getMinus());
            Assert.assertNull(copy.getPlus());
        }

        Assert.assertEquals(orig.depth(), copy.depth());
        Assert.assertEquals(orig.count(), copy.count());
    }

    private static List<TestLineSegment> getLineSegments(TestBSPTree tree) {
        return tree.stream()
            .filter(BSPTree.Node::isInternal)
            .map(n -> (TestLineSegment) n.getCut())
            .collect(Collectors.toList());
    }

    /** Create a map of points to the nodes that they resolve to in the
     * given tree.
     */
    private static Map<TestPoint2D, TestNode> createPointNodeMap(TestBSPTree tree, int min, int max) {
        Map<TestPoint2D, TestNode> map = new HashMap<>();

        for (int x = min; x <= max; ++x) {
            for (int y = min; y <= max; ++y) {
                TestPoint2D pt = new TestPoint2D(x, y);
                TestNode node = tree.findNode(pt, NodeCutRule.NODE);

                map.put(pt, node);
            }
        }

        return map;
    }

    /** Check that transformed points resolve to the same tree nodes that were found when the original
     * points were resolved in the untransformed tree.
     * @param transformed
     * @param transform
     * @param pointNodeMap
     */
    private static void checkTransformedPointNodeMap(TestBSPTree transformedTree, Transform<TestPoint2D> transform,
            Map<TestPoint2D, TestNode> pointNodeMap) {

        for (TestPoint2D pt : pointNodeMap.keySet()) {
            TestNode expectedNode = pointNodeMap.get(pt);
            TestPoint2D transformedPt = transform.apply(pt);

            String msg = "Expected transformed point " + transformedPt + " to resolve to node " + expectedNode;
            Assert.assertSame(msg, expectedNode, transformedTree.findNode(transformedPt, NodeCutRule.NODE));
        }
    }

}
