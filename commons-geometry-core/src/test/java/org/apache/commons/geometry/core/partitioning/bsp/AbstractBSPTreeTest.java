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
package org.apache.commons.geometry.core.partitioning.bsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.BoundarySource;
import org.apache.commons.geometry.core.partitioning.bsp.BSPTree.FindNodeCutRule;
import org.apache.commons.geometry.core.partitioning.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partitioning.test.TestBSPTree;
import org.apache.commons.geometry.core.partitioning.test.TestLine;
import org.apache.commons.geometry.core.partitioning.test.TestLineSegment;
import org.apache.commons.geometry.core.partitioning.test.TestLineSegmentCollection;
import org.apache.commons.geometry.core.partitioning.test.TestPoint2D;
import org.apache.commons.geometry.core.partitioning.test.TestTransform2D;
import org.apache.commons.geometry.core.partitioning.test.TestBSPTree.TestNode;
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
    public void testInsertCut_cutExistsInTree_sameOrientation() {
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
    public void testInsertCut_cutExistsInTree_oppositeOrientation() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode node = tree.getRoot()
                .cut(TestLine.X_AXIS)
                    .getMinus()
                        .cut(TestLine.Y_AXIS)
                        .getPlus()
                            .cut(new TestLine(0, 2, 2, 0));

        // act
        boolean result = node.insertCut(new TestLine(0, 3, 0, 2));

        // assert
        Assert.assertTrue(result);
        PartitionTestUtils.assertIsInternalNode(node);
    }

    @Test
    public void testInsertCut_createRegionWithThicknessOfHyperplane() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestNode node = tree.getRoot()
                .cut(TestLine.X_AXIS)
                    .getMinus();

        // act
        boolean result = node.insertCut(new TestLine(0, 0, -1, 0));

        // assert
        Assert.assertTrue(result);

        Assert.assertSame(tree.getRoot().getPlus(), tree.findNode(new TestPoint2D(0, -1e-2)));
        Assert.assertSame(node.getMinus(), tree.findNode(new TestPoint2D(0, 0)));
        Assert.assertSame(node.getPlus(), tree.findNode(new TestPoint2D(0, 1e-2)));
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
            Assert.assertSame(root, tree.findNode(pt, FindNodeCutRule.NODE));
        }

        for (TestPoint2D pt : testPoints) {
            Assert.assertSame(root, tree.findNode(pt, FindNodeCutRule.MINUS));
        }

        for (TestPoint2D pt : testPoints) {
            Assert.assertSame(root, tree.findNode(pt, FindNodeCutRule.PLUS));
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

        FindNodeCutRule cutBehavior = FindNodeCutRule.NODE;

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

        FindNodeCutRule cutBehavior = FindNodeCutRule.MINUS;

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

        FindNodeCutRule cutBehavior = FindNodeCutRule.PLUS;

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
    public void testInsert_hyperplaneSubset_concaveRegion() {
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
    public void testInsert_boundarySource() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        TestLineSegment a = new TestLineSegment(-1, -1, 1, -1);
        TestLineSegment b = new TestLineSegment(1, -1, 0, 0);
        TestLineSegment c = new TestLineSegment(0, 0, 1, 1);
        TestLineSegment d = new TestLineSegment(1, 1, -1, 1);
        TestLineSegment e = new TestLineSegment(-1, 1, -1, -1);

        BoundarySource<TestLineSegment> src = () -> Arrays.asList(a, b, c, d, e).stream();

        // act
        tree.insert(src);

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
    public void testInsert_boundarySource_emptySource() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        BoundarySource<TestLineSegment> src = () -> new ArrayList<TestLineSegment>().stream();

        // act
        tree.insert(src);

        // assert
        Assert.assertEquals(1, tree.count());
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
    public void testVisit_defaultOrder() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        TestNode root = tree.getRoot();
        TestNode minus = root.getMinus();
        TestNode plus = root.getPlus();
        TestNode minusMinus = minus.getMinus();
        TestNode minusPlus = minus.getPlus();

        List<TestNode> nodes = new ArrayList<>();

        // act
        tree.accept(node -> {
            nodes.add(node);
            return BSPTreeVisitor.Result.CONTINUE;
        });

        // assert
        Assert.assertEquals(
                Arrays.asList(root, minus, minusMinus, minusPlus, plus),
                nodes);
    }

    @Test
    public void testVisit_specifiedOrder() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        TestNode root = tree.getRoot();
        TestNode minus = root.getMinus();
        TestNode plus = root.getPlus();
        TestNode minusMinus = minus.getMinus();
        TestNode minusPlus = minus.getPlus();

        // act/assert
        TestVisitor plusMinusNode = new TestVisitor(BSPTreeVisitor.Order.PLUS_MINUS_NODE);
        tree.accept(plusMinusNode);
        Assert.assertEquals(
                Arrays.asList(plus, minusPlus, minusMinus, minus, root),
                plusMinusNode.getVisited());

        TestVisitor plusNodeMinus = new TestVisitor(BSPTreeVisitor.Order.PLUS_NODE_MINUS);
        tree.accept(plusNodeMinus);
        Assert.assertEquals(
                Arrays.asList(plus, root, minusPlus, minus, minusMinus),
                plusNodeMinus.getVisited());

        TestVisitor minusPlusNode = new TestVisitor(BSPTreeVisitor.Order.MINUS_PLUS_NODE);
        tree.accept(minusPlusNode);
        Assert.assertEquals(
                Arrays.asList(minusMinus, minusPlus, minus, plus, root),
                minusPlusNode.getVisited());

        TestVisitor minusNodePlus = new TestVisitor(BSPTreeVisitor.Order.MINUS_NODE_PLUS);
        tree.accept(minusNodePlus);
        Assert.assertEquals(
                Arrays.asList(minusMinus, minus, minusPlus, root, plus),
                minusNodePlus.getVisited());

        TestVisitor nodeMinusPlus = new TestVisitor(BSPTreeVisitor.Order.NODE_MINUS_PLUS);
        tree.accept(nodeMinusPlus);
        Assert.assertEquals(
                Arrays.asList(root, minus, minusMinus, minusPlus, plus),
                nodeMinusPlus.getVisited());

        TestVisitor nodePlusMinus = new TestVisitor(BSPTreeVisitor.Order.NODE_PLUS_MINUS);
        tree.accept(nodePlusMinus);
        Assert.assertEquals(
                Arrays.asList(root, plus, minus, minusPlus, minusMinus),
                nodePlusMinus.getVisited());
    }

    @Test
    public void testVisit_nullVisitOrderSkipsSubtree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        TestNode root = tree.getRoot();
        TestNode plus = root.getPlus();
        TestNode minus = root.getMinus();

        TestVisitor visitor = new TestVisitor(BSPTreeVisitor.Order.NODE_MINUS_PLUS) {
            @Override
            public Order visitOrder(TestNode node) {
                if (node == minus) {
                    return null;
                }
                return super.visitOrder(node);
            }
        };

        // act
        tree.accept(visitor);

        // assert
        Assert.assertEquals(
                Arrays.asList(root, plus),
                visitor.getVisited());
    }

    @Test
    public void testVisit_noneVisitOrderSkipsSubtree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        TestNode root = tree.getRoot();
        TestNode plus = root.getPlus();
        TestNode minus = root.getMinus();

        TestVisitor visitor = new TestVisitor(BSPTreeVisitor.Order.NODE_MINUS_PLUS) {
            @Override
            public Order visitOrder(TestNode node) {
                if (node == minus) {
                    return Order.NONE;
                }
                return super.visitOrder(node);
            }
        };

        // act
        tree.accept(visitor);

        // assert
        Assert.assertEquals(
                Arrays.asList(root, plus),
                visitor.getVisited());
    }

    @Test
    public void testVisit_visitorReturnsNull_terminatesEarly() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        TestNode root = tree.getRoot();
        TestNode minus = root.getMinus();
        TestNode minusMinus = minus.getMinus();
        TestNode minusPlus = minus.getPlus();

        TestVisitor visitor = new TestVisitor(BSPTreeVisitor.Order.MINUS_PLUS_NODE) {
            @Override
            public Result visit(TestNode node) {
                super.visit(node);

                if (node == minus) {
                    return null;
                }
                return Result.CONTINUE;
            }
        };

        // act
        tree.accept(visitor);

        // assert
        Assert.assertEquals(
                Arrays.asList(minusMinus, minusPlus, minus),
                visitor.getVisited());
    }

    @Test
    public void testVisit_visitorReturnsTerminate_terminatesEarly() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        TestNode root = tree.getRoot();
        TestNode minus = root.getMinus();
        TestNode minusMinus = minus.getMinus();
        TestNode minusPlus = minus.getPlus();

        TestVisitor visitor = new TestVisitor(BSPTreeVisitor.Order.MINUS_PLUS_NODE) {
            @Override
            public Result visit(TestNode node) {
                super.visit(node);

                if (node == minus) {
                    return Result.TERMINATE;
                }
                return Result.CONTINUE;
            }
        };

        // act
        tree.accept(visitor);

        // assert
        Assert.assertEquals(
                Arrays.asList(minusMinus, minusPlus, minus),
                visitor.getVisited());
    }

    @Test
    public void testVisit_earlyTerminationPermutations() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        TestNode root = tree.getRoot();
        TestNode minus = root.getMinus();
        TestNode plus = root.getPlus();
        TestNode minusMinus = minus.getMinus();
        TestNode minusPlus = minus.getPlus();

        // act/assert
        TestVisitor plusMinusNode = new TestVisitor(BSPTreeVisitor.Order.PLUS_MINUS_NODE).withTerminationNode(minus);
        tree.accept(plusMinusNode);
        Assert.assertEquals(
                Arrays.asList(plus, minusPlus, minusMinus, minus),
                plusMinusNode.getVisited());

        TestVisitor plusNodeMinus = new TestVisitor(BSPTreeVisitor.Order.PLUS_NODE_MINUS).withTerminationNode(minus);
        tree.accept(plusNodeMinus);
        Assert.assertEquals(
                Arrays.asList(plus, root, minusPlus, minus),
                plusNodeMinus.getVisited());

        TestVisitor minusPlusNode = new TestVisitor(BSPTreeVisitor.Order.MINUS_PLUS_NODE).withTerminationNode(minus);
        tree.accept(minusPlusNode);
        Assert.assertEquals(
                Arrays.asList(minusMinus, minusPlus, minus),
                minusPlusNode.getVisited());

        TestVisitor minusNodePlus = new TestVisitor(BSPTreeVisitor.Order.MINUS_NODE_PLUS).withTerminationNode(minus);
        tree.accept(minusNodePlus);
        Assert.assertEquals(
                Arrays.asList(minusMinus, minus),
                minusNodePlus.getVisited());

        TestVisitor nodeMinusPlus = new TestVisitor(BSPTreeVisitor.Order.NODE_MINUS_PLUS).withTerminationNode(minus);
        tree.accept(nodeMinusPlus);
        Assert.assertEquals(
                Arrays.asList(root, minus),
                nodeMinusPlus.getVisited());

        TestVisitor nodePlusMinus = new TestVisitor(BSPTreeVisitor.Order.NODE_PLUS_MINUS).withTerminationNode(minus);
        tree.accept(nodePlusMinus);
        Assert.assertEquals(
                Arrays.asList(root, plus, minus),
                nodePlusMinus.getVisited());
    }

    @Test
    public void testVisit_visitNode() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        TestNode root = tree.getRoot();
        TestNode minus = root.getMinus();
        TestNode minusMinus = minus.getMinus();
        TestNode minusPlus = minus.getPlus();

        List<TestNode> nodes = new ArrayList<>();

        // act
        minus.accept(node -> {
            nodes.add(node);
            return BSPTreeVisitor.Result.CONTINUE;
        });

        // assert
        Assert.assertEquals(
                Arrays.asList(minus, minusMinus, minusPlus),
                nodes);
    }

    @Test
    public void testNodesIterable_emptyTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        List<TestNode> nodes = new ArrayList<>();

        // act
        for (TestNode node : tree.nodes()) {
            nodes.add(node);
        }

        // assert
        Assert.assertEquals(1, nodes.size());
        Assert.assertSame(tree.getRoot(), nodes.get(0));
    }

    @Test
    public void testNodesIterable_multipleNodes() {
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
        for (TestNode node : tree.nodes()) {
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
    public void testNodesIterable_iteratorThrowsNoSuchElementExceptionAtEnd() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        Iterator<TestNode> it = tree.nodes().iterator();
        it.next();

        // act
        try {
            it.next();
            Assert.fail("Operation should have thrown an exception");
        } catch (NoSuchElementException exc) {
            // expected
        }
    }

    @Test
    public void testSubtreeNodesIterable_singleNodeSubtree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestNode node = tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS)
                .getMinus();

        List<TestNode> nodes = new ArrayList<>();
        // act
        for (TestNode n : node.nodes()) {
            nodes.add(n);
        }

        // assert
        Assert.assertEquals(1, nodes.size());
        Assert.assertSame(node, nodes.get(0));
    }

    @Test
    public void testSubtreeNodesIterable_multipleNodeSubtree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestNode node = tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        List<TestNode> nodes = new ArrayList<>();
        // act
        for (TestNode n : node.nodes()) {
            nodes.add(n);
        }

        // assert
        Assert.assertEquals(3, nodes.size());
        Assert.assertSame(node, nodes.get(0));
        Assert.assertSame(node.getMinus(), nodes.get(1));
        Assert.assertSame(node.getPlus(), nodes.get(2));
    }

    @Test
    public void testNodeTrim() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.Y_AXIS)
            .getPlus()
                .cut(new TestLine(new TestPoint2D(0, 0), new TestPoint2D(1, 1)))
                .getPlus()
                    .cut(new TestLine(new TestPoint2D(1.5, 1.5), new TestPoint2D(2, 1)));

        TestNode root = tree.getRoot();
        TestNode plus = root.getPlus();
        TestNode plusMinus = plus.getMinus();
        TestNode plusPlusPlus = plus.getPlus().getPlus();

        TestLineSegment xAxisSeg = TestLine.X_AXIS.span();
        TestLineSegment shortSeg = new TestLineSegment(new TestPoint2D(2, 0), new TestPoint2D(2, 2));

        // act/assert
        Assert.assertSame(xAxisSeg, root.trim(xAxisSeg));
        Assert.assertSame(shortSeg, root.trim(shortSeg));

        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                (TestLineSegment) plus.trim(xAxisSeg));
        Assert.assertSame(shortSeg, plus.trim(shortSeg));

        Assert.assertNull(plusMinus.trim(xAxisSeg));
        Assert.assertNull(plusMinus.trim(shortSeg));

        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, 3, TestLine.X_AXIS),
                (TestLineSegment) plusPlusPlus.trim(xAxisSeg));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(new TestPoint2D(2, 0), new TestPoint2D(2, 1)),
                (TestLineSegment) plusPlusPlus.trim(shortSeg));
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
        Assert.assertSame(node, resultNode);

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

        Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(p.getX(), p.getY() + 2));

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

        Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(p.getX(), p.getY() + 2));

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

        Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(0.5 * p.getX(), p.getY() + 2));

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

        Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(-p.getX(), p.getY()));

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

        Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(p.getX(), -p.getY()));

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

        Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(-p.getX(), -p.getY()));

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

    @Test
    public void testSplitIntoTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        TestBSPTree minus = new TestBSPTree();
        TestBSPTree plus = new TestBSPTree();

        TestLine splitter = new TestLine(new TestPoint2D(0,  0), new TestPoint2D(-1, 1));

        // act
        tree.splitIntoTrees(splitter, minus, plus);

        // assert
        TestLineSegment splitSegment = new TestLineSegment(Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, splitter);

        Assert.assertEquals(5, tree.count());
        Assert.assertEquals(2, tree.height());

        Assert.assertEquals(5, minus.count());
        Assert.assertEquals(2, minus.height());

        List<TestLineSegment> minusSegments = getLineSegments(minus);
        Assert.assertEquals(2, minusSegments.size());
        PartitionTestUtils.assertSegmentsEqual(splitSegment, minusSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(Double.NEGATIVE_INFINITY, 0, TestLine.X_AXIS),
                minusSegments.get(1));

        Assert.assertEquals(7, plus.count());
        Assert.assertEquals(3, plus.height());

        List<TestLineSegment> plusSegments = getLineSegments(plus);
        Assert.assertEquals(3, plusSegments.size());
        PartitionTestUtils.assertSegmentsEqual(splitSegment, plusSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                plusSegments.get(1));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.Y_AXIS),
                plusSegments.get(2));
    }

    @Test
    public void testSplitIntoTree_minusOnly() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        TestBSPTree minus = new TestBSPTree();

        TestLine splitter = new TestLine(new TestPoint2D(0,  0), new TestPoint2D(-1, 1));

        // act
        tree.splitIntoTrees(splitter, minus, null);

        // assert
        TestLineSegment splitSegment = new TestLineSegment(Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, splitter);

        Assert.assertEquals(5, tree.count());
        Assert.assertEquals(2, tree.height());

        Assert.assertEquals(5, minus.count());
        Assert.assertEquals(2, minus.height());

        List<TestLineSegment> minusSegments = getLineSegments(minus);
        Assert.assertEquals(2, minusSegments.size());
        PartitionTestUtils.assertSegmentsEqual(splitSegment, minusSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(Double.NEGATIVE_INFINITY, 0, TestLine.X_AXIS),
                minusSegments.get(1));
    }

    @Test
    public void testSplitIntoTree_plusOnly() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        TestBSPTree plus = new TestBSPTree();

        TestLine splitter = new TestLine(new TestPoint2D(0,  0), new TestPoint2D(-1, 1));

        // act
        tree.splitIntoTrees(splitter, null, plus);

        // assert
        TestLineSegment splitSegment = new TestLineSegment(Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, splitter);

        Assert.assertEquals(5, tree.count());
        Assert.assertEquals(2, tree.height());

        Assert.assertEquals(7, plus.count());
        Assert.assertEquals(3, plus.height());

        List<TestLineSegment> plusSegments = getLineSegments(plus);
        Assert.assertEquals(3, plusSegments.size());
        PartitionTestUtils.assertSegmentsEqual(splitSegment, plusSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                plusSegments.get(1));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.Y_AXIS),
                plusSegments.get(2));
    }

    private void assertNodesCopiedRecursive(final TestNode orig, final TestNode copy) {
        Assert.assertNotSame(orig, copy);

        Assert.assertEquals(orig.getCut(), copy.getCut());

        if (orig.isLeaf()) {
            Assert.assertNull(copy.getMinus());
            Assert.assertNull(copy.getPlus());
        } else {
            Assert.assertNotSame(orig.getMinus(), copy.getMinus());
            Assert.assertNotSame(orig.getPlus(), copy.getPlus());

            assertNodesCopiedRecursive(orig.getMinus(), copy.getMinus());
            assertNodesCopiedRecursive(orig.getPlus(), copy.getPlus());
        }

        Assert.assertEquals(orig.depth(), copy.depth());
        Assert.assertEquals(orig.count(), copy.count());
    }

    private static List<TestLineSegment> getLineSegments(TestBSPTree tree) {
        return StreamSupport.stream(tree.nodes().spliterator(), false)
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
                TestNode node = tree.findNode(pt, FindNodeCutRule.NODE);

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
            Assert.assertSame(msg, expectedNode, transformedTree.findNode(transformedPt, FindNodeCutRule.NODE));
        }
    }

    private static class TestVisitor implements BSPTreeVisitor<TestPoint2D, TestNode> {

        private final Order order;

        private TestNode terminationNode;

        private final List<TestNode> visited = new ArrayList<>();

        TestVisitor(Order order) {
            this.order = order;
        }

        public TestVisitor withTerminationNode(TestNode newTerminationNode) {
            this.terminationNode = newTerminationNode;

            return this;
        }

        @Override
        public Result visit(TestNode node) {
            visited.add(node);

            return (terminationNode == node) ?
                    Result.TERMINATE :
                    Result.CONTINUE;
        }

        @Override
        public Order visitOrder(TestNode node) {
            return order;
        }

        public List<TestNode> getVisited() {
            return visited;
        }
    }
}
