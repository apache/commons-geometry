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
        final TestBSPTree tree = new TestBSPTree();

        // assert
        final TestNode root = tree.getRoot();

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
        final TestBSPTree tree = new TestBSPTree();

        final TestNode root = tree.getRoot();
        root.cut(TestLine.X_AXIS);

        final TestNode plus = root.getPlus();
        final TestNode minus = root.getMinus();

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
        final TestBSPTree tree = new TestBSPTree();
        final TestLine line = TestLine.X_AXIS;

        // act
        final boolean result = tree.getRoot().insertCut(line);

        // assert
        Assert.assertTrue(result);

        final TestNode root = tree.getRoot();
        PartitionTestUtils.assertIsInternalNode(root);

        Assert.assertSame(line, root.getCut().getHyperplane());

        PartitionTestUtils.assertIsLeafNode(root.getMinus());
        PartitionTestUtils.assertIsLeafNode(root.getPlus());
    }

    @Test
    public void testInsertCut_fitsCutterToCell() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS)
                .getPlus();

        // act
        final boolean result = node.insertCut(new TestLine(0.5, 1.5, 1.5, 0.5));

        // assert
        Assert.assertTrue(result);
        PartitionTestUtils.assertIsInternalNode(node);

        final TestLineSegment segment = (TestLineSegment) node.getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), segment.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), segment.getEndPoint());
    }

    @Test
    public void testInsertCut_doesNotPassThroughCell_intersects() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus();

        // act
        final boolean result = node.insertCut(new TestLine(-2, 0, 0, -2));

        // assert
        Assert.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    public void testInsertCut_doesNotPassThroughCell_parallel() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus();

        // act
        final boolean result = node.insertCut(new TestLine(0, -1, 1, -1));

        // assert
        Assert.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    public void testInsertCut_doesNotPassThroughCell_removesExistingChildren() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus()
                        .cut(new TestLine(0, 2, 2, 0));

        // act
        final boolean result = node.insertCut(new TestLine(-2, 0, 0, -2));

        // assert
        Assert.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    public void testInsertCut_cutExistsInTree_sameOrientation() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
                .cut(TestLine.X_AXIS)
                    .getMinus()
                        .cut(TestLine.Y_AXIS)
                        .getPlus()
                            .cut(new TestLine(0, 2, 2, 0));

        // act
        final boolean result = node.insertCut(new TestLine(0, 2, 0, 3));

        // assert
        Assert.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    public void testInsertCut_cutExistsInTree_oppositeOrientation() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
                .cut(TestLine.X_AXIS)
                    .getMinus()
                        .cut(TestLine.Y_AXIS)
                        .getPlus()
                            .cut(new TestLine(0, 2, 2, 0));

        // act
        final boolean result = node.insertCut(new TestLine(0, 3, 0, 2));

        // assert
        Assert.assertTrue(result);
        PartitionTestUtils.assertIsInternalNode(node);
    }

    @Test
    public void testInsertCut_createRegionWithThicknessOfHyperplane() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
                .cut(TestLine.X_AXIS)
                    .getMinus();

        // act
        final boolean result = node.insertCut(new TestLine(0, 0, -1, 0));

        // assert
        Assert.assertTrue(result);

        Assert.assertSame(tree.getRoot().getPlus(), tree.findNode(new TestPoint2D(0, -1e-2)));
        Assert.assertSame(node.getMinus(), tree.findNode(new TestPoint2D(0, 0)));
        Assert.assertSame(node.getPlus(), tree.findNode(new TestPoint2D(0, 1e-2)));
    }

    @Test
    public void testClearCut_cutExists() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        final boolean result = node.clearCut();

        // assert
        Assert.assertTrue(result);
        Assert.assertTrue(node.isLeaf());
        Assert.assertNull(node.getPlus());
        Assert.assertNull(node.getMinus());
    }

    @Test
    public void testClearCut_cutDoesNotExist() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                .cut(TestLine.Y_AXIS)
                .getMinus();

        // act
        final boolean result = node.clearCut();

        // assert
        Assert.assertFalse(result);
        Assert.assertTrue(node.isLeaf());
        Assert.assertNull(node.getPlus());
        Assert.assertNull(node.getMinus());
    }

    @Test
    public void testClearCut_root_fullTree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                .cut(TestLine.Y_AXIS)
                .getMinus();

        // act
        final boolean result = tree.getRoot().clearCut();

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
        final TestBSPTree tree = new TestBSPTree();
        final TestNode node = tree.getRoot();

        // act
        final boolean result = node.clearCut();

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
        final TestBSPTree tree = new TestBSPTree();
        final TestNode root = tree.getRoot();

        final List<TestPoint2D> testPoints = Arrays.asList(
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
        for (final TestPoint2D pt : testPoints) {
            Assert.assertSame(root, tree.findNode(pt));
        }

        for (final TestPoint2D pt : testPoints) {
            Assert.assertSame(root, tree.findNode(pt, FindNodeCutRule.NODE));
        }

        for (final TestPoint2D pt : testPoints) {
            Assert.assertSame(root, tree.findNode(pt, FindNodeCutRule.MINUS));
        }

        for (final TestPoint2D pt : testPoints) {
            Assert.assertSame(root, tree.findNode(pt, FindNodeCutRule.PLUS));
        }
    }

    @Test
    public void testFindNode_singleArg() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        tree.getRoot()
                .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus()
                        .cut(new TestLine(0, 2, 2, 0));

        final TestNode root = tree.getRoot();
        final TestNode minusY = root.getPlus();

        final TestNode yCut = root.getMinus();
        final TestNode minusXPlusY = yCut.getMinus();

        final TestNode diagonalCut = yCut.getPlus();
        final TestNode underDiagonal = diagonalCut.getPlus();
        final TestNode aboveDiagonal = diagonalCut.getMinus();

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
        final TestBSPTree tree = new TestBSPTree();

        tree.getRoot()
                .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus()
                        .cut(new TestLine(0, 2, 2, 0));

        final TestNode root = tree.getRoot();
        final TestNode minusY = root.getPlus();

        final TestNode yCut = root.getMinus();
        final TestNode minusXPlusY = yCut.getMinus();

        final TestNode diagonalCut = yCut.getPlus();
        final TestNode underDiagonal = diagonalCut.getPlus();
        final TestNode aboveDiagonal = diagonalCut.getMinus();

        final FindNodeCutRule cutBehavior = FindNodeCutRule.NODE;

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
        final TestBSPTree tree = new TestBSPTree();

        tree.getRoot()
                .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus()
                        .cut(new TestLine(0, 2, 2, 0));

        final TestNode root = tree.getRoot();
        final TestNode minusY = root.getPlus();

        final TestNode yCut = root.getMinus();
        final TestNode minusXPlusY = yCut.getMinus();

        final TestNode diagonalCut = yCut.getPlus();
        final TestNode underDiagonal = diagonalCut.getPlus();
        final TestNode aboveDiagonal = diagonalCut.getMinus();

        final FindNodeCutRule cutBehavior = FindNodeCutRule.MINUS;

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
        final TestBSPTree tree = new TestBSPTree();

        tree.getRoot()
                .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus()
                        .cut(new TestLine(0, 2, 2, 0));

        final TestNode root = tree.getRoot();
        final TestNode minusY = root.getPlus();

        final TestNode yCut = root.getMinus();
        final TestNode minusXPlusY = yCut.getMinus();

        final TestNode diagonalCut = yCut.getPlus();
        final TestNode underDiagonal = diagonalCut.getPlus();
        final TestNode aboveDiagonal = diagonalCut.getMinus();

        final FindNodeCutRule cutBehavior = FindNodeCutRule.PLUS;

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
        final TestBSPTree tree = new TestBSPTree();

        // act
        tree.insert(new TestLineSegment(1, 0, 1, 1));

        // assert
        final TestNode root = tree.getRoot();
        Assert.assertFalse(root.isLeaf());
        Assert.assertTrue(root.getMinus().isLeaf());
        Assert.assertTrue(root.getPlus().isLeaf());

        final TestLineSegment seg = (TestLineSegment) root.getCut();
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
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        tree.insert(new TestLineSegment(0.5, 1.5, 1.5, 0.5));

        // assert
        final TestNode root = tree.getRoot();
        Assert.assertFalse(root.isLeaf());

        final TestNode node = tree.findNode(new TestPoint2D(0.5, 0.5));
        final TestLineSegment seg = (TestLineSegment) node.getParent().getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), seg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), seg.getEndPoint());

        Assert.assertTrue(tree.getRoot().getPlus().isLeaf());
        Assert.assertTrue(tree.getRoot().getMinus().getMinus().isLeaf());
    }

    @Test
    public void testInsert_convex_split() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        tree.insert(new TestLineSegment(-0.5, 2.5, 2.5, -0.5));

        // assert
        final TestNode root = tree.getRoot();
        Assert.assertFalse(root.isLeaf());

        final TestNode plusXPlusY = tree.getRoot().getMinus().getPlus();
        final TestLineSegment plusXPlusYSeg = (TestLineSegment) plusXPlusY.getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), plusXPlusYSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), plusXPlusYSeg.getEndPoint());

        final TestNode minusY = tree.getRoot().getPlus();
        final TestLineSegment minusYSeg = (TestLineSegment) minusY.getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), minusYSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(
                new TestPoint2D(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), minusYSeg.getEndPoint());

        final TestNode minusXPlusY = tree.getRoot().getMinus().getMinus();
        final TestLineSegment minusXPlusYSeg = (TestLineSegment) minusXPlusY.getCut();

        PartitionTestUtils.assertPointsEqual(
                new TestPoint2D(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), minusXPlusYSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), minusXPlusYSeg.getEndPoint());
    }

    @Test
    public void testInsert_convexList_convexRegion() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestLineSegment a = new TestLineSegment(0, 0, 1, 0);
        final TestLineSegment b = new TestLineSegment(1, 0, 0, 1);
        final TestLineSegment c = new TestLineSegment(0, 1, 0, 0);

        // act
        tree.insert(Arrays.asList(a, b, c));

        // assert
        final List<TestLineSegment> segments = getLineSegments(tree);

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
        final TestBSPTree tree = new TestBSPTree();

        final TestLineSegment a = new TestLineSegment(-1, -1, 1, -1);
        final TestLineSegment b = new TestLineSegment(1, -1, 0, 0);
        final TestLineSegment c = new TestLineSegment(0, 0, 1, 1);
        final TestLineSegment d = new TestLineSegment(1, 1, -1, 1);
        final TestLineSegment e = new TestLineSegment(-1, 1, -1, -1);

        // act
        tree.insert(Arrays.asList(a, b, c, d, e));

        // assert
        final List<TestLineSegment> segments = getLineSegments(tree);

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
        final TestBSPTree tree = new TestBSPTree();

        final TestLineSegment a = new TestLineSegment(-1, -1, 1, -1);
        final TestLineSegment b = new TestLineSegment(1, -1, 0, 0);
        final TestLineSegment c = new TestLineSegment(0, 0, 1, 1);
        final TestLineSegment d = new TestLineSegment(1, 1, -1, 1);
        final TestLineSegment e = new TestLineSegment(-1, 1, -1, -1);

        final TestLineSegmentCollection coll = new TestLineSegmentCollection(
                Arrays.asList(a, b, c, d, e));

        // act
        tree.insert(coll);

        // assert
        final List<TestLineSegment> segments = getLineSegments(tree);

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
        final TestBSPTree tree = new TestBSPTree();

        final TestLineSegment a = new TestLineSegment(-1, -1, 1, -1);
        final TestLineSegment b = new TestLineSegment(1, -1, 0, 0);
        final TestLineSegment c = new TestLineSegment(0, 0, 1, 1);
        final TestLineSegment d = new TestLineSegment(1, 1, -1, 1);
        final TestLineSegment e = new TestLineSegment(-1, 1, -1, -1);

        final BoundarySource<TestLineSegment> src = () -> Arrays.asList(a, b, c, d, e).stream();

        // act
        tree.insert(src);

        // assert
        final List<TestLineSegment> segments = getLineSegments(tree);

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
        final TestBSPTree tree = new TestBSPTree();

        final BoundarySource<TestLineSegment> src = () -> new ArrayList<TestLineSegment>().stream();

        // act
        tree.insert(src);

        // assert
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testCount() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

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
        final TestBSPTree tree = new TestBSPTree();

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
        final TestBSPTree tree = new TestBSPTree();

        final TestNode root = tree.getRoot();
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
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        final TestNode root = tree.getRoot();
        final TestNode minus = root.getMinus();
        final TestNode plus = root.getPlus();
        final TestNode minusMinus = minus.getMinus();
        final TestNode minusPlus = minus.getPlus();

        final List<TestNode> nodes = new ArrayList<>();

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
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        final TestNode root = tree.getRoot();
        final TestNode minus = root.getMinus();
        final TestNode plus = root.getPlus();
        final TestNode minusMinus = minus.getMinus();
        final TestNode minusPlus = minus.getPlus();

        // act/assert
        final TestVisitor plusMinusNode = new TestVisitor(BSPTreeVisitor.Order.PLUS_MINUS_NODE);
        tree.accept(plusMinusNode);
        Assert.assertEquals(
                Arrays.asList(plus, minusPlus, minusMinus, minus, root),
                plusMinusNode.getVisited());

        final TestVisitor plusNodeMinus = new TestVisitor(BSPTreeVisitor.Order.PLUS_NODE_MINUS);
        tree.accept(plusNodeMinus);
        Assert.assertEquals(
                Arrays.asList(plus, root, minusPlus, minus, minusMinus),
                plusNodeMinus.getVisited());

        final TestVisitor minusPlusNode = new TestVisitor(BSPTreeVisitor.Order.MINUS_PLUS_NODE);
        tree.accept(minusPlusNode);
        Assert.assertEquals(
                Arrays.asList(minusMinus, minusPlus, minus, plus, root),
                minusPlusNode.getVisited());

        final TestVisitor minusNodePlus = new TestVisitor(BSPTreeVisitor.Order.MINUS_NODE_PLUS);
        tree.accept(minusNodePlus);
        Assert.assertEquals(
                Arrays.asList(minusMinus, minus, minusPlus, root, plus),
                minusNodePlus.getVisited());

        final TestVisitor nodeMinusPlus = new TestVisitor(BSPTreeVisitor.Order.NODE_MINUS_PLUS);
        tree.accept(nodeMinusPlus);
        Assert.assertEquals(
                Arrays.asList(root, minus, minusMinus, minusPlus, plus),
                nodeMinusPlus.getVisited());

        final TestVisitor nodePlusMinus = new TestVisitor(BSPTreeVisitor.Order.NODE_PLUS_MINUS);
        tree.accept(nodePlusMinus);
        Assert.assertEquals(
                Arrays.asList(root, plus, minus, minusPlus, minusMinus),
                nodePlusMinus.getVisited());
    }

    @Test
    public void testVisit_nullVisitOrderSkipsSubtree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        final TestNode root = tree.getRoot();
        final TestNode plus = root.getPlus();
        final TestNode minus = root.getMinus();

        final TestVisitor visitor = new TestVisitor(BSPTreeVisitor.Order.NODE_MINUS_PLUS) {
            @Override
            public Order visitOrder(final TestNode node) {
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
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        final TestNode root = tree.getRoot();
        final TestNode plus = root.getPlus();
        final TestNode minus = root.getMinus();

        final TestVisitor visitor = new TestVisitor(BSPTreeVisitor.Order.NODE_MINUS_PLUS) {
            @Override
            public Order visitOrder(final TestNode node) {
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
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        final TestNode root = tree.getRoot();
        final TestNode minus = root.getMinus();
        final TestNode minusMinus = minus.getMinus();
        final TestNode minusPlus = minus.getPlus();

        final TestVisitor visitor = new TestVisitor(BSPTreeVisitor.Order.MINUS_PLUS_NODE) {
            @Override
            public Result visit(final TestNode node) {
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
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        final TestNode root = tree.getRoot();
        final TestNode minus = root.getMinus();
        final TestNode minusMinus = minus.getMinus();
        final TestNode minusPlus = minus.getPlus();

        final TestVisitor visitor = new TestVisitor(BSPTreeVisitor.Order.MINUS_PLUS_NODE) {
            @Override
            public Result visit(final TestNode node) {
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
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        final TestNode root = tree.getRoot();
        final TestNode minus = root.getMinus();
        final TestNode plus = root.getPlus();
        final TestNode minusMinus = minus.getMinus();
        final TestNode minusPlus = minus.getPlus();

        // act/assert
        final TestVisitor plusMinusNode = new TestVisitor(BSPTreeVisitor.Order.PLUS_MINUS_NODE).withTerminationNode(minus);
        tree.accept(plusMinusNode);
        Assert.assertEquals(
                Arrays.asList(plus, minusPlus, minusMinus, minus),
                plusMinusNode.getVisited());

        final TestVisitor plusNodeMinus = new TestVisitor(BSPTreeVisitor.Order.PLUS_NODE_MINUS).withTerminationNode(minus);
        tree.accept(plusNodeMinus);
        Assert.assertEquals(
                Arrays.asList(plus, root, minusPlus, minus),
                plusNodeMinus.getVisited());

        final TestVisitor minusPlusNode = new TestVisitor(BSPTreeVisitor.Order.MINUS_PLUS_NODE).withTerminationNode(minus);
        tree.accept(minusPlusNode);
        Assert.assertEquals(
                Arrays.asList(minusMinus, minusPlus, minus),
                minusPlusNode.getVisited());

        final TestVisitor minusNodePlus = new TestVisitor(BSPTreeVisitor.Order.MINUS_NODE_PLUS).withTerminationNode(minus);
        tree.accept(minusNodePlus);
        Assert.assertEquals(
                Arrays.asList(minusMinus, minus),
                minusNodePlus.getVisited());

        final TestVisitor nodeMinusPlus = new TestVisitor(BSPTreeVisitor.Order.NODE_MINUS_PLUS).withTerminationNode(minus);
        tree.accept(nodeMinusPlus);
        Assert.assertEquals(
                Arrays.asList(root, minus),
                nodeMinusPlus.getVisited());

        final TestVisitor nodePlusMinus = new TestVisitor(BSPTreeVisitor.Order.NODE_PLUS_MINUS).withTerminationNode(minus);
        tree.accept(nodePlusMinus);
        Assert.assertEquals(
                Arrays.asList(root, plus, minus),
                nodePlusMinus.getVisited());
    }

    @Test
    public void testVisit_visitNode() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        final TestNode root = tree.getRoot();
        final TestNode minus = root.getMinus();
        final TestNode minusMinus = minus.getMinus();
        final TestNode minusPlus = minus.getPlus();

        final List<TestNode> nodes = new ArrayList<>();

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
        final TestBSPTree tree = new TestBSPTree();
        final List<TestNode> nodes = new ArrayList<>();

        // act
        for (final TestNode node : tree.nodes()) {
            nodes.add(node);
        }

        // assert
        Assert.assertEquals(1, nodes.size());
        Assert.assertSame(tree.getRoot(), nodes.get(0));
    }

    @Test
    public void testNodesIterable_multipleNodes() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode root = tree.getRoot();
        root.cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                 .getParent()
                 .getPlus()
                     .cut(TestLine.Y_AXIS);

        final List<TestNode> nodes = new ArrayList<>();

        // act
        for (final TestNode node : tree.nodes()) {
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
        final TestBSPTree tree = new TestBSPTree();

        final Iterator<TestNode> it = tree.nodes().iterator();
        it.next();

        // act
        try {
            it.next();
            Assert.fail("Operation should have thrown an exception");
        } catch (final NoSuchElementException exc) {
            // expected
        }
    }

    @Test
    public void testSubtreeNodesIterable_singleNodeSubtree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        final TestNode node = tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS)
                .getMinus();

        final List<TestNode> nodes = new ArrayList<>();
        // act
        for (final TestNode n : node.nodes()) {
            nodes.add(n);
        }

        // assert
        Assert.assertEquals(1, nodes.size());
        Assert.assertSame(node, nodes.get(0));
    }

    @Test
    public void testSubtreeNodesIterable_multipleNodeSubtree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        final TestNode node = tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        final List<TestNode> nodes = new ArrayList<>();
        // act
        for (final TestNode n : node.nodes()) {
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
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.Y_AXIS)
            .getPlus()
                .cut(new TestLine(new TestPoint2D(0, 0), new TestPoint2D(1, 1)))
                .getPlus()
                    .cut(new TestLine(new TestPoint2D(1.5, 1.5), new TestPoint2D(2, 1)));

        final TestNode root = tree.getRoot();
        final TestNode plus = root.getPlus();
        final TestNode plusMinus = plus.getMinus();
        final TestNode plusPlusPlus = plus.getPlus().getPlus();

        final TestLineSegment xAxisSeg = TestLine.X_AXIS.span();
        final TestLineSegment shortSeg = new TestLineSegment(new TestPoint2D(2, 0), new TestPoint2D(2, 2));

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
        final TestBSPTree tree = new TestBSPTree();

        // act
        final TestBSPTree copy = new TestBSPTree();
        copy.copy(tree);

        // assert
        Assert.assertNotSame(tree, copy);
        Assert.assertNotSame(tree.getRoot(), copy.getRoot());

        Assert.assertEquals(tree.count(), copy.count());
    }

    @Test
    public void testCopy_withCuts() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        final TestBSPTree copy = new TestBSPTree();
        copy.copy(tree);

        // assert
        Assert.assertNotSame(tree, copy);
        assertNodesCopiedRecursive(tree.getRoot(), copy.getRoot());
        Assert.assertEquals(tree.count(), copy.count());
    }

    @Test
    public void testCopy_changesToOneTreeDoNotAffectCopy() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        final TestBSPTree copy = new TestBSPTree();
        copy.copy(tree);
        tree.getRoot().clearCut();

        // assert
        Assert.assertEquals(1, tree.count());
        Assert.assertEquals(5, copy.count());
    }

    @Test
    public void testCopy_instancePassedAsArgument() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
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
        final TestBSPTree tree = new TestBSPTree();

        final TestBSPTree result = new TestBSPTree();
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
        final TestBSPTree tree = new TestBSPTree();

        final TestBSPTree result = new TestBSPTree();
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
        final TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(1, 2), new TestPoint2D(2, 1)),
                    new TestLineSegment(new TestPoint2D(-1, 2), new TestPoint2D(-2, 1)),
                    new TestLineSegment(new TestPoint2D(0, -2), new TestPoint2D(1, -2))
                ));

        final TestBSPTree result = new TestBSPTree();

        // act
        result.extract(tree.getRoot().getPlus());

        // assert
        Assert.assertEquals(7, result.count());

        final List<TestLineSegment> resultSegments = getLineSegments(result);
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

        final List<TestLineSegment> inputSegment = getLineSegments(tree);
        Assert.assertEquals(6, inputSegment.size());

        PartitionTestUtils.assertTreeStructure(tree);
        PartitionTestUtils.assertTreeStructure(result);
    }

    @Test
    public void testExtract_leafNode() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(1, 2), new TestPoint2D(2, 1)),
                    new TestLineSegment(new TestPoint2D(-1, 2), new TestPoint2D(-2, 1)),
                    new TestLineSegment(new TestPoint2D(0, -2), new TestPoint2D(1, -2))
                ));

        final TestPoint2D pt = new TestPoint2D(1, 1);

        final TestNode node = tree.findNode(pt);
        final TestBSPTree result = new TestBSPTree();

        // act
        result.extract(node);

        // assert
        final TestNode resultNode = result.findNode(pt);
        Assert.assertNotNull(resultNode);
        Assert.assertNotSame(node, resultNode);

        Assert.assertEquals(7, result.count());

        final List<TestLineSegment> resultSegments = getLineSegments(result);
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

        final List<TestLineSegment> inputSegment = getLineSegments(tree);
        Assert.assertEquals(6, inputSegment.size());

        PartitionTestUtils.assertTreeStructure(tree);
        PartitionTestUtils.assertTreeStructure(result);
    }

    @Test
    public void testExtract_extractFromSameTree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(1, 2), new TestPoint2D(2, 1)),
                    new TestLineSegment(new TestPoint2D(-1, 2), new TestPoint2D(-2, 1)),
                    new TestLineSegment(new TestPoint2D(0, -2), new TestPoint2D(1, -2))
                ));

        final TestPoint2D pt = new TestPoint2D(1, 1);

        final TestNode node = tree.findNode(pt);

        // act
        tree.extract(node);

        // assert
        final TestNode resultNode = tree.findNode(pt);
        Assert.assertNotNull(resultNode);
        Assert.assertSame(node, resultNode);

        Assert.assertEquals(7, tree.count());

        final List<TestLineSegment> resultSegments = getLineSegments(tree);
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
        final TestBSPTree tree = new TestBSPTree();

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(p.getX(), p.getY() + 2));

        // act
        tree.transform(t);

        // assert
        Assert.assertEquals(1, tree.count());
        Assert.assertTrue(tree.getRoot().isLeaf());
    }

    @Test
    public void testTransform_singleCut() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().insertCut(TestLine.X_AXIS);

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(p.getX(), p.getY() + 2));

        // act
        tree.transform(t);

        // assert
        Assert.assertEquals(3, tree.count());

        final List<TestLineSegment> segments = getLineSegments(tree);
        Assert.assertEquals(1, segments.size());

        final TestLineSegment seg = segments.get(0);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.NEGATIVE_INFINITY, 2), seg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.POSITIVE_INFINITY, 2), seg.getEndPoint());
    }

    @Test
    public void testTransform_multipleCuts() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(-1, -1), new TestPoint2D(1, 1)),
                    new TestLineSegment(new TestPoint2D(3, 1), new TestPoint2D(3, 2))
                ));

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(0.5 * p.getX(), p.getY() + 2));

        // act
        tree.transform(t);

        // assert
        Assert.assertEquals(9, tree.count());

        final List<TestLineSegment> segments = getLineSegments(tree);
        Assert.assertEquals(4, segments.size());

        final TestLineSegment segment1 = segments.get(0);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.NEGATIVE_INFINITY, 2), segment1.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.POSITIVE_INFINITY, 2), segment1.getEndPoint());

        final TestLineSegment segment2 = segments.get(1);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), segment2.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), segment2.getEndPoint());

        final TestLineSegment segment3 = segments.get(2);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(1.5, 2), segment3.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(1.5, 5), segment3.getEndPoint());

        final TestLineSegment segment4 = segments.get(3);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY), segment4.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), segment4.getEndPoint());
    }

    @Test
    public void testTransform_xAxisReflection() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(0, 3), new TestPoint2D(3, 0))
                ));

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(-p.getX(), p.getY()));

        final Map<TestPoint2D, TestNode> pointNodeMap = createPointNodeMap(tree, -5, 5);

        // act
        tree.transform(t);

        // assert
        checkTransformedPointNodeMap(tree, t, pointNodeMap);

        final List<TestLineSegment> segments = getLineSegments(tree);
        Assert.assertEquals(4, segments.size());
    }

    @Test
    public void testTransform_yAxisReflection() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(0, 3), new TestPoint2D(3, 0))
                ));

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(p.getX(), -p.getY()));

        final Map<TestPoint2D, TestNode> pointNodeMap = createPointNodeMap(tree, -5, 5);

        // act
        tree.transform(t);

        // assert
        checkTransformedPointNodeMap(tree, t, pointNodeMap);

        final List<TestLineSegment> segments = getLineSegments(tree);
        Assert.assertEquals(4, segments.size());
    }

    @Test
    public void testTransform_xAndYAxisReflection() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.insert(Arrays.asList(
                    new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)),
                    new TestLineSegment(new TestPoint2D(0, -1), new TestPoint2D(0, 1)),
                    new TestLineSegment(new TestPoint2D(0, 3), new TestPoint2D(3, 0))
                ));

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(-p.getX(), -p.getY()));

        final Map<TestPoint2D, TestNode> pointNodeMap = createPointNodeMap(tree, -5, 5);

        // act
        tree.transform(t);

        // assert
        checkTransformedPointNodeMap(tree, t, pointNodeMap);

        final List<TestLineSegment> segments = getLineSegments(tree);
        Assert.assertEquals(4, segments.size());
    }

    @Test
    public void testTreeString() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        // act
        final String str = tree.treeString();

        // assert
        final String[] lines = str.split("\n");
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
        final TestBSPTree tree = new TestBSPTree();

        // act
        final String str = tree.treeString();

        // assert
        Assert.assertEquals("TestNode[cut= null]", str);
    }

    @Test
    public void testTreeString_reachesMaxDepth() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS)
            .getMinus().cut(new TestLine(new TestPoint2D(-2, 1), new TestPoint2D(0, 1)));

        // act
        final String str = tree.treeString(1);

        // assert
        final String[] lines = str.split("\n");
        Assert.assertEquals(4, lines.length);
        Assert.assertTrue(lines[0].startsWith("TestNode[cut= TestLineSegment"));
        Assert.assertTrue(lines[1].startsWith("    [-] TestNode[cut= TestLineSegment"));
        Assert.assertEquals("        ...", lines[2]);
        Assert.assertEquals("    [+] TestNode[cut= null]", lines[3]);
    }

    @Test
    public void testTreeString_zeroMaxDepth() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS)
            .getMinus().cut(new TestLine(new TestPoint2D(-2, 1), new TestPoint2D(0, 1)));

        // act
        final String str = tree.treeString(0);

        // assert
        final String[] lines = str.split("\n");
        Assert.assertEquals(2, lines.length);
        Assert.assertTrue(lines[0].startsWith("TestNode[cut= TestLineSegment"));
        Assert.assertTrue(lines[1].startsWith("    ..."));
    }

    @Test
    public void testTreeString_negativeMaxDepth() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS)
            .getMinus().cut(new TestLine(new TestPoint2D(-2, 1), new TestPoint2D(0, 1)));

        // act
        final String str = tree.treeString(-1);

        // assert
        Assert.assertEquals("", str);
    }

    @Test
    public void testToString() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().insertCut(TestLine.Y_AXIS);

        // act
        final String str = tree.toString();

        // assert
        final String msg = "Unexpected toString() representation: " + str;

        Assert.assertTrue(msg, str.contains("TestBSPTree"));
        Assert.assertTrue(msg, str.contains("count= 3"));
        Assert.assertTrue(msg, str.contains("height= 1"));
    }

    @Test
    public void testNodeToString() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS);

        // act
        final String str = tree.getRoot().toString();

        // assert
        Assert.assertTrue(str.contains("TestNode"));
        Assert.assertTrue(str.contains("cut= TestLineSegment"));
    }

    @Test
    public void testSplitIntoTree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        final TestBSPTree minus = new TestBSPTree();
        final TestBSPTree plus = new TestBSPTree();

        final TestLine splitter = new TestLine(new TestPoint2D(0,  0), new TestPoint2D(-1, 1));

        // act
        tree.splitIntoTrees(splitter, minus, plus);

        // assert
        final TestLineSegment splitSegment = new TestLineSegment(Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, splitter);

        Assert.assertEquals(5, tree.count());
        Assert.assertEquals(2, tree.height());

        Assert.assertEquals(5, minus.count());
        Assert.assertEquals(2, minus.height());

        final List<TestLineSegment> minusSegments = getLineSegments(minus);
        Assert.assertEquals(2, minusSegments.size());
        PartitionTestUtils.assertSegmentsEqual(splitSegment, minusSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(Double.NEGATIVE_INFINITY, 0, TestLine.X_AXIS),
                minusSegments.get(1));

        Assert.assertEquals(7, plus.count());
        Assert.assertEquals(3, plus.height());

        final List<TestLineSegment> plusSegments = getLineSegments(plus);
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
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        final TestBSPTree minus = new TestBSPTree();

        final TestLine splitter = new TestLine(new TestPoint2D(0,  0), new TestPoint2D(-1, 1));

        // act
        tree.splitIntoTrees(splitter, minus, null);

        // assert
        final TestLineSegment splitSegment = new TestLineSegment(Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, splitter);

        Assert.assertEquals(5, tree.count());
        Assert.assertEquals(2, tree.height());

        Assert.assertEquals(5, minus.count());
        Assert.assertEquals(2, minus.height());

        final List<TestLineSegment> minusSegments = getLineSegments(minus);
        Assert.assertEquals(2, minusSegments.size());
        PartitionTestUtils.assertSegmentsEqual(splitSegment, minusSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(Double.NEGATIVE_INFINITY, 0, TestLine.X_AXIS),
                minusSegments.get(1));
    }

    @Test
    public void testSplitIntoTree_plusOnly() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        final TestBSPTree plus = new TestBSPTree();

        final TestLine splitter = new TestLine(new TestPoint2D(0,  0), new TestPoint2D(-1, 1));

        // act
        tree.splitIntoTrees(splitter, null, plus);

        // assert
        final TestLineSegment splitSegment = new TestLineSegment(Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, splitter);

        Assert.assertEquals(5, tree.count());
        Assert.assertEquals(2, tree.height());

        Assert.assertEquals(7, plus.count());
        Assert.assertEquals(3, plus.height());

        final List<TestLineSegment> plusSegments = getLineSegments(plus);
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

    private static List<TestLineSegment> getLineSegments(final TestBSPTree tree) {
        return StreamSupport.stream(tree.nodes().spliterator(), false)
            .filter(BSPTree.Node::isInternal)
            .map(n -> (TestLineSegment) n.getCut())
            .collect(Collectors.toList());
    }

    /** Create a map of points to the nodes that they resolve to in the
     * given tree.
     */
    private static Map<TestPoint2D, TestNode> createPointNodeMap(final TestBSPTree tree, final int min, final int max) {
        final Map<TestPoint2D, TestNode> map = new HashMap<>();

        for (int x = min; x <= max; ++x) {
            for (int y = min; y <= max; ++y) {
                final TestPoint2D pt = new TestPoint2D(x, y);
                final TestNode node = tree.findNode(pt, FindNodeCutRule.NODE);

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
    private static void checkTransformedPointNodeMap(final TestBSPTree transformedTree, final Transform<TestPoint2D> transform,
                                                     final Map<TestPoint2D, TestNode> pointNodeMap) {

        for (final TestPoint2D pt : pointNodeMap.keySet()) {
            final TestNode expectedNode = pointNodeMap.get(pt);
            final TestPoint2D transformedPt = transform.apply(pt);

            final String msg = "Expected transformed point " + transformedPt + " to resolve to node " + expectedNode;
            Assert.assertSame(msg, expectedNode, transformedTree.findNode(transformedPt, FindNodeCutRule.NODE));
        }
    }

    private static class TestVisitor implements BSPTreeVisitor<TestPoint2D, TestNode> {

        private final Order order;

        private TestNode terminationNode;

        private final List<TestNode> visited = new ArrayList<>();

        TestVisitor(final Order order) {
            this.order = order;
        }

        public TestVisitor withTerminationNode(final TestNode newTerminationNode) {
            this.terminationNode = newTerminationNode;
            return this;
        }

        @Override
        public Result visit(final TestNode node) {
            visited.add(node);
            return (terminationNode == node) ?
                    Result.TERMINATE :
                    Result.CONTINUE;
        }

        @Override
        public Order visitOrder(final TestNode node) {
            return order;
        }

        public List<TestNode> getVisited() {
            return visited;
        }
    }
}
