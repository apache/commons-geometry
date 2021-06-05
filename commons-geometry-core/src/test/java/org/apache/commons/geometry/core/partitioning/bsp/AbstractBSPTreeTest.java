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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.BoundarySource;
import org.apache.commons.geometry.core.partitioning.bsp.BSPTree.FindNodeCutRule;
import org.apache.commons.geometry.core.partitioning.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partitioning.test.TestBSPTree;
import org.apache.commons.geometry.core.partitioning.test.TestBSPTree.TestNode;
import org.apache.commons.geometry.core.partitioning.test.TestLine;
import org.apache.commons.geometry.core.partitioning.test.TestLineSegment;
import org.apache.commons.geometry.core.partitioning.test.TestLineSegmentCollection;
import org.apache.commons.geometry.core.partitioning.test.TestPoint2D;
import org.apache.commons.geometry.core.partitioning.test.TestTransform2D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AbstractBSPTreeTest {

    @Test
    void testInitialization() {
        // act
        final TestBSPTree tree = new TestBSPTree();

        // assert
        final TestNode root = tree.getRoot();

        Assertions.assertNotNull(root);
        Assertions.assertNull(root.getParent());

        PartitionTestUtils.assertIsLeafNode(root);
        Assertions.assertFalse(root.isPlus());
        Assertions.assertFalse(root.isMinus());

        Assertions.assertSame(tree, root.getTree());
    }

    @Test
    void testNodeStateGetters() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode root = tree.getRoot();
        root.cut(TestLine.X_AXIS);

        final TestNode plus = root.getPlus();
        final TestNode minus = root.getMinus();

        // act/assert
        Assertions.assertFalse(root.isLeaf());
        Assertions.assertTrue(root.isInternal());
        Assertions.assertFalse(root.isPlus());
        Assertions.assertFalse(root.isMinus());

        Assertions.assertTrue(plus.isLeaf());
        Assertions.assertFalse(plus.isInternal());
        Assertions.assertTrue(plus.isPlus());
        Assertions.assertFalse(plus.isMinus());

        Assertions.assertTrue(minus.isLeaf());
        Assertions.assertFalse(minus.isInternal());
        Assertions.assertFalse(minus.isPlus());
        Assertions.assertTrue(minus.isMinus());
    }

    @Test
    void testInsertCut() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        final TestLine line = TestLine.X_AXIS;

        // act
        final boolean result = tree.getRoot().insertCut(line);

        // assert
        Assertions.assertTrue(result);

        final TestNode root = tree.getRoot();
        PartitionTestUtils.assertIsInternalNode(root);

        Assertions.assertSame(line, root.getCut().getHyperplane());

        PartitionTestUtils.assertIsLeafNode(root.getMinus());
        PartitionTestUtils.assertIsLeafNode(root.getPlus());
    }

    @Test
    void testInsertCut_fitsCutterToCell() {
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
        Assertions.assertTrue(result);
        PartitionTestUtils.assertIsInternalNode(node);

        final TestLineSegment segment = (TestLineSegment) node.getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), segment.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), segment.getEndPoint());
    }

    @Test
    void testInsertCut_doesNotPassThroughCell_intersects() {
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
        Assertions.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    void testInsertCut_doesNotPassThroughCell_parallel() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus();

        // act
        final boolean result = node.insertCut(new TestLine(0, -1, 1, -1));

        // assert
        Assertions.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    void testInsertCut_doesNotPassThroughCell_removesExistingChildren() {
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
        Assertions.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    void testInsertCut_cutExistsInTree_sameOrientation() {
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
        Assertions.assertFalse(result);
        PartitionTestUtils.assertIsLeafNode(node);
    }

    @Test
    void testInsertCut_cutExistsInTree_oppositeOrientation() {
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
        Assertions.assertTrue(result);
        PartitionTestUtils.assertIsInternalNode(node);
    }

    @Test
    void testInsertCut_createRegionWithThicknessOfHyperplane() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
                .cut(TestLine.X_AXIS)
                    .getMinus();

        // act
        final boolean result = node.insertCut(new TestLine(0, 0, -1, 0));

        // assert
        Assertions.assertTrue(result);

        Assertions.assertSame(tree.getRoot().getPlus(), tree.findNode(new TestPoint2D(0, -1e-2)));
        Assertions.assertSame(node.getMinus(), tree.findNode(new TestPoint2D(0, 0)));
        Assertions.assertSame(node.getPlus(), tree.findNode(new TestPoint2D(0, 1e-2)));
    }

    @Test
    void testClearCut_cutExists() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        final boolean result = node.clearCut();

        // assert
        Assertions.assertTrue(result);
        Assertions.assertTrue(node.isLeaf());
        Assertions.assertNull(node.getPlus());
        Assertions.assertNull(node.getMinus());
    }

    @Test
    void testClearCut_cutDoesNotExist() {
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
        Assertions.assertFalse(result);
        Assertions.assertTrue(node.isLeaf());
        Assertions.assertNull(node.getPlus());
        Assertions.assertNull(node.getMinus());
    }

    @Test
    void testClearCut_root_fullTree() {
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
        Assertions.assertTrue(result);
        Assertions.assertTrue(node.isLeaf());
        Assertions.assertNull(node.getPlus());
        Assertions.assertNull(node.getMinus());

        Assertions.assertEquals(1, tree.count());
    }

    @Test
    void testClearCut_root_emptyTree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        final TestNode node = tree.getRoot();

        // act
        final boolean result = node.clearCut();

        // assert
        Assertions.assertFalse(result);
        Assertions.assertTrue(node.isLeaf());
        Assertions.assertNull(node.getPlus());
        Assertions.assertNull(node.getMinus());

        Assertions.assertEquals(1, tree.count());
    }

    @Test
    void testFindNode_emptyTree() {
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
            Assertions.assertSame(root, tree.findNode(pt));
        }

        for (final TestPoint2D pt : testPoints) {
            Assertions.assertSame(root, tree.findNode(pt, FindNodeCutRule.NODE));
        }

        for (final TestPoint2D pt : testPoints) {
            Assertions.assertSame(root, tree.findNode(pt, FindNodeCutRule.MINUS));
        }

        for (final TestPoint2D pt : testPoints) {
            Assertions.assertSame(root, tree.findNode(pt, FindNodeCutRule.PLUS));
        }
    }

    @Test
    void testFindNode_singleArg() {
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
        Assertions.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(0, 0)));

        Assertions.assertSame(underDiagonal, tree.findNode(new TestPoint2D(1, 0)));
        Assertions.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(1, 1)));
        Assertions.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(0, 1)));
        Assertions.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 1)));
        Assertions.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 0)));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(-1, -1)));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(0, -1)));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(1, -1)));

        Assertions.assertSame(underDiagonal, tree.findNode(new TestPoint2D(0.5, 0.5)));
        Assertions.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(3, 3)));
    }

    @Test
    void testFindNode_nodeCutBehavior() {
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
        Assertions.assertSame(root, tree.findNode(new TestPoint2D(0, 0), cutBehavior));

        Assertions.assertSame(root, tree.findNode(new TestPoint2D(1, 0), cutBehavior));
        Assertions.assertSame(diagonalCut, tree.findNode(new TestPoint2D(1, 1), cutBehavior));
        Assertions.assertSame(yCut, tree.findNode(new TestPoint2D(0, 1), cutBehavior));
        Assertions.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 1), cutBehavior));
        Assertions.assertSame(root, tree.findNode(new TestPoint2D(-1, 0), cutBehavior));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(-1, -1), cutBehavior));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(0, -1), cutBehavior));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(1, -1), cutBehavior));

        Assertions.assertSame(underDiagonal, tree.findNode(new TestPoint2D(0.5, 0.5), cutBehavior));
        Assertions.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(3, 3), cutBehavior));
    }

    @Test
    void testFindNode_minusCutBehavior() {
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
        Assertions.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(0, 0), cutBehavior));

        Assertions.assertSame(underDiagonal, tree.findNode(new TestPoint2D(1, 0), cutBehavior));
        Assertions.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(1, 1), cutBehavior));
        Assertions.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(0, 1), cutBehavior));
        Assertions.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 1), cutBehavior));
        Assertions.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 0), cutBehavior));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(-1, -1), cutBehavior));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(0, -1), cutBehavior));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(1, -1), cutBehavior));

        Assertions.assertSame(underDiagonal, tree.findNode(new TestPoint2D(0.5, 0.5), cutBehavior));
        Assertions.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(3, 3), cutBehavior));
    }

    @Test
    void testFindNode_plusCutBehavior() {
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
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(0, 0), cutBehavior));

        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(1, 0), cutBehavior));
        Assertions.assertSame(underDiagonal, tree.findNode(new TestPoint2D(1, 1), cutBehavior));
        Assertions.assertSame(underDiagonal, tree.findNode(new TestPoint2D(0, 1), cutBehavior));
        Assertions.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 1), cutBehavior));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(-1, 0), cutBehavior));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(-1, -1), cutBehavior));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(0, -1), cutBehavior));
        Assertions.assertSame(minusY, tree.findNode(new TestPoint2D(1, -1), cutBehavior));

        Assertions.assertSame(underDiagonal, tree.findNode(new TestPoint2D(0.5, 0.5), cutBehavior));
        Assertions.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(3, 3), cutBehavior));
    }

    @Test
    void testInsert_convex_emptyTree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        // act
        tree.insert(new TestLineSegment(1, 0, 1, 1));

        // assert
        final TestNode root = tree.getRoot();
        Assertions.assertFalse(root.isLeaf());
        Assertions.assertTrue(root.getMinus().isLeaf());
        Assertions.assertTrue(root.getPlus().isLeaf());

        final TestLineSegment seg = (TestLineSegment) root.getCut();
        PartitionTestUtils.assertPointsEqual(
                new TestPoint2D(1, Double.NEGATIVE_INFINITY),
                seg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(
                new TestPoint2D(1, Double.POSITIVE_INFINITY),
                seg.getEndPoint());
    }

    @Test
    void testInsert_convex_noSplit() {
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
        Assertions.assertFalse(root.isLeaf());

        final TestNode node = tree.findNode(new TestPoint2D(0.5, 0.5));
        final TestLineSegment seg = (TestLineSegment) node.getParent().getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), seg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), seg.getEndPoint());

        Assertions.assertTrue(tree.getRoot().getPlus().isLeaf());
        Assertions.assertTrue(tree.getRoot().getMinus().getMinus().isLeaf());
    }

    @Test
    void testInsert_convex_split() {
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
        Assertions.assertFalse(root.isLeaf());

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
    void testInsert_convexList_convexRegion() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestLineSegment a = new TestLineSegment(0, 0, 1, 0);
        final TestLineSegment b = new TestLineSegment(1, 0, 0, 1);
        final TestLineSegment c = new TestLineSegment(0, 1, 0, 0);

        // act
        tree.insert(Arrays.asList(a, b, c));

        // assert
        final List<TestLineSegment> segments = getLineSegments(tree);

        Assertions.assertEquals(3, segments.size());

        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                segments.get(0));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(-Math.sqrt(0.5), Double.POSITIVE_INFINITY, new TestLine(1, 0, 0, 1)),
                segments.get(1));
        PartitionTestUtils.assertSegmentsEqual(c, segments.get(2));
    }

    @Test
    void testInsert_convexList_concaveRegion() {
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

        Assertions.assertEquals(5, segments.size());

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
    void testInsert_hyperplaneSubset_concaveRegion() {
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

        Assertions.assertEquals(5, segments.size());

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
    void testInsert_boundarySource() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestLineSegment a = new TestLineSegment(-1, -1, 1, -1);
        final TestLineSegment b = new TestLineSegment(1, -1, 0, 0);
        final TestLineSegment c = new TestLineSegment(0, 0, 1, 1);
        final TestLineSegment d = new TestLineSegment(1, 1, -1, 1);
        final TestLineSegment e = new TestLineSegment(-1, 1, -1, -1);

        final BoundarySource<TestLineSegment> src = () -> Stream.of(a, b, c, d, e);

        // act
        tree.insert(src);

        // assert
        final List<TestLineSegment> segments = getLineSegments(tree);

        Assertions.assertEquals(5, segments.size());

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
    void testInsert_boundarySource_emptySource() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final BoundarySource<TestLineSegment> src = Stream::empty;

        // act
        tree.insert(src);

        // assert
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    void testCount() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        // act/assert
        Assertions.assertEquals(1, tree.count());
        Assertions.assertEquals(1, tree.getRoot().count());

        tree.getRoot().insertCut(TestLine.X_AXIS);
        Assertions.assertEquals(1, tree.getRoot().getMinus().count());
        Assertions.assertEquals(1, tree.getRoot().getPlus().count());
        Assertions.assertEquals(3, tree.count());

        tree.getRoot().getPlus().insertCut(TestLine.Y_AXIS);
        Assertions.assertEquals(1, tree.getRoot().getMinus().count());
        Assertions.assertEquals(3, tree.getRoot().getPlus().count());
        Assertions.assertEquals(5, tree.count());

        tree.getRoot().getMinus().insertCut(TestLine.Y_AXIS);
        Assertions.assertEquals(3, tree.getRoot().getMinus().count());
        Assertions.assertEquals(3, tree.getRoot().getPlus().count());
        Assertions.assertEquals(7, tree.count());

        tree.getRoot().getMinus().insertCut(new TestLine(new TestPoint2D(-1, -1), new TestPoint2D(1, -1)));
        Assertions.assertEquals(1, tree.getRoot().getMinus().count());
        Assertions.assertEquals(3, tree.getRoot().getPlus().count());
        Assertions.assertEquals(5, tree.count());
    }

    @Test
    void testHeight() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        // act/assert
        Assertions.assertEquals(0, tree.height());
        Assertions.assertEquals(0, tree.getRoot().height());

        tree.getRoot().insertCut(TestLine.X_AXIS);
        Assertions.assertEquals(0, tree.getRoot().getMinus().height());
        Assertions.assertEquals(0, tree.getRoot().getPlus().height());
        Assertions.assertEquals(1, tree.height());

        tree.getRoot().getPlus().insertCut(TestLine.Y_AXIS);
        Assertions.assertEquals(0, tree.getRoot().getMinus().height());
        Assertions.assertEquals(1, tree.getRoot().getPlus().height());
        Assertions.assertEquals(2, tree.height());

        tree.getRoot().getMinus().insertCut(TestLine.Y_AXIS);
        Assertions.assertEquals(1, tree.getRoot().getMinus().height());
        Assertions.assertEquals(1, tree.getRoot().getPlus().height());
        Assertions.assertEquals(2, tree.height());

        tree.getRoot().getMinus().clearCut();
        Assertions.assertEquals(0, tree.getRoot().getMinus().height());
        Assertions.assertEquals(1, tree.getRoot().getPlus().height());
        Assertions.assertEquals(2, tree.height());

        tree.getRoot().getPlus().getPlus()
            .insertCut(new TestLine(new TestPoint2D(0, -1), new TestPoint2D(1, -1)));

        Assertions.assertEquals(0, tree.getRoot().getMinus().height());
        Assertions.assertEquals(2, tree.getRoot().getPlus().height());
        Assertions.assertEquals(3, tree.height());
    }

    @Test
    void testDepth() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestNode root = tree.getRoot();
        root.cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS);

        // act/assert
        Assertions.assertEquals(0, root.depth());

        Assertions.assertEquals(1, root.getPlus().depth());

        Assertions.assertEquals(1, root.getMinus().depth());
        Assertions.assertEquals(2, root.getMinus().getPlus().depth());
        Assertions.assertEquals(2, root.getMinus().getMinus().depth());
    }

    @Test
    void testVisit_defaultOrder() {
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
        Assertions.assertEquals(
                Arrays.asList(root, minus, minusMinus, minusPlus, plus),
                nodes);
    }

    @Test
    void testVisit_specifiedOrder() {
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
        Assertions.assertEquals(
                Arrays.asList(plus, minusPlus, minusMinus, minus, root),
                plusMinusNode.getVisited());

        final TestVisitor plusNodeMinus = new TestVisitor(BSPTreeVisitor.Order.PLUS_NODE_MINUS);
        tree.accept(plusNodeMinus);
        Assertions.assertEquals(
                Arrays.asList(plus, root, minusPlus, minus, minusMinus),
                plusNodeMinus.getVisited());

        final TestVisitor minusPlusNode = new TestVisitor(BSPTreeVisitor.Order.MINUS_PLUS_NODE);
        tree.accept(minusPlusNode);
        Assertions.assertEquals(
                Arrays.asList(minusMinus, minusPlus, minus, plus, root),
                minusPlusNode.getVisited());

        final TestVisitor minusNodePlus = new TestVisitor(BSPTreeVisitor.Order.MINUS_NODE_PLUS);
        tree.accept(minusNodePlus);
        Assertions.assertEquals(
                Arrays.asList(minusMinus, minus, minusPlus, root, plus),
                minusNodePlus.getVisited());

        final TestVisitor nodeMinusPlus = new TestVisitor(BSPTreeVisitor.Order.NODE_MINUS_PLUS);
        tree.accept(nodeMinusPlus);
        Assertions.assertEquals(
                Arrays.asList(root, minus, minusMinus, minusPlus, plus),
                nodeMinusPlus.getVisited());

        final TestVisitor nodePlusMinus = new TestVisitor(BSPTreeVisitor.Order.NODE_PLUS_MINUS);
        tree.accept(nodePlusMinus);
        Assertions.assertEquals(
                Arrays.asList(root, plus, minus, minusPlus, minusMinus),
                nodePlusMinus.getVisited());
    }

    @Test
    void testVisit_nullVisitOrderSkipsSubtree() {
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
        Assertions.assertEquals(
                Arrays.asList(root, plus),
                visitor.getVisited());
    }

    @Test
    void testVisit_noneVisitOrderSkipsSubtree() {
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
        Assertions.assertEquals(
                Arrays.asList(root, plus),
                visitor.getVisited());
    }

    @Test
    void testVisit_visitorReturnsNull_terminatesEarly() {
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
        Assertions.assertEquals(
                Arrays.asList(minusMinus, minusPlus, minus),
                visitor.getVisited());
    }

    @Test
    void testVisit_visitorReturnsTerminate_terminatesEarly() {
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
        Assertions.assertEquals(
                Arrays.asList(minusMinus, minusPlus, minus),
                visitor.getVisited());
    }

    @Test
    void testVisit_earlyTerminationPermutations() {
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
        Assertions.assertEquals(
                Arrays.asList(plus, minusPlus, minusMinus, minus),
                plusMinusNode.getVisited());

        final TestVisitor plusNodeMinus = new TestVisitor(BSPTreeVisitor.Order.PLUS_NODE_MINUS).withTerminationNode(minus);
        tree.accept(plusNodeMinus);
        Assertions.assertEquals(
                Arrays.asList(plus, root, minusPlus, minus),
                plusNodeMinus.getVisited());

        final TestVisitor minusPlusNode = new TestVisitor(BSPTreeVisitor.Order.MINUS_PLUS_NODE).withTerminationNode(minus);
        tree.accept(minusPlusNode);
        Assertions.assertEquals(
                Arrays.asList(minusMinus, minusPlus, minus),
                minusPlusNode.getVisited());

        final TestVisitor minusNodePlus = new TestVisitor(BSPTreeVisitor.Order.MINUS_NODE_PLUS).withTerminationNode(minus);
        tree.accept(minusNodePlus);
        Assertions.assertEquals(
                Arrays.asList(minusMinus, minus),
                minusNodePlus.getVisited());

        final TestVisitor nodeMinusPlus = new TestVisitor(BSPTreeVisitor.Order.NODE_MINUS_PLUS).withTerminationNode(minus);
        tree.accept(nodeMinusPlus);
        Assertions.assertEquals(
                Arrays.asList(root, minus),
                nodeMinusPlus.getVisited());

        final TestVisitor nodePlusMinus = new TestVisitor(BSPTreeVisitor.Order.NODE_PLUS_MINUS).withTerminationNode(minus);
        tree.accept(nodePlusMinus);
        Assertions.assertEquals(
                Arrays.asList(root, plus, minus),
                nodePlusMinus.getVisited());
    }

    @Test
    void testVisit_visitNode() {
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
        Assertions.assertEquals(
                Arrays.asList(minus, minusMinus, minusPlus),
                nodes);
    }

    @Test
    void testNodesIterable_emptyTree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        final List<TestNode> nodes = new ArrayList<>();

        // act
        for (final TestNode node : tree.nodes()) {
            nodes.add(node);
        }

        // assert
        Assertions.assertEquals(1, nodes.size());
        Assertions.assertSame(tree.getRoot(), nodes.get(0));
    }

    @Test
    void testNodesIterable_multipleNodes() {
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
        Assertions.assertEquals(7, nodes.size());
        Assertions.assertSame(root, nodes.get(0));

        Assertions.assertSame(root.getMinus(), nodes.get(1));
        Assertions.assertSame(root.getMinus().getMinus(), nodes.get(2));
        Assertions.assertSame(root.getMinus().getPlus(), nodes.get(3));

        Assertions.assertSame(root.getPlus(), nodes.get(4));
        Assertions.assertSame(root.getPlus().getMinus(), nodes.get(5));
        Assertions.assertSame(root.getPlus().getPlus(), nodes.get(6));
    }


    @Test
    void testNodesIterable_iteratorThrowsNoSuchElementExceptionAtEnd() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final Iterator<TestNode> it = tree.nodes().iterator();
        it.next();

        // act
        try {
            it.next();
            Assertions.fail("Operation should have thrown an exception");
        } catch (final NoSuchElementException exc) {
            // expected
        }
    }

    @Test
    void testSubtreeNodesIterable_singleNodeSubtree() {
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
        Assertions.assertEquals(1, nodes.size());
        Assertions.assertSame(node, nodes.get(0));
    }

    @Test
    void testSubtreeNodesIterable_multipleNodeSubtree() {
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
        Assertions.assertEquals(3, nodes.size());
        Assertions.assertSame(node, nodes.get(0));
        Assertions.assertSame(node.getMinus(), nodes.get(1));
        Assertions.assertSame(node.getPlus(), nodes.get(2));
    }

    @Test
    void testNodeTrim() {
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
        Assertions.assertSame(xAxisSeg, root.trim(xAxisSeg));
        Assertions.assertSame(shortSeg, root.trim(shortSeg));

        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                (TestLineSegment) plus.trim(xAxisSeg));
        Assertions.assertSame(shortSeg, plus.trim(shortSeg));

        Assertions.assertNull(plusMinus.trim(xAxisSeg));
        Assertions.assertNull(plusMinus.trim(shortSeg));

        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, 3, TestLine.X_AXIS),
                (TestLineSegment) plusPlusPlus.trim(xAxisSeg));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(new TestPoint2D(2, 0), new TestPoint2D(2, 1)),
                (TestLineSegment) plusPlusPlus.trim(shortSeg));
    }

    @Test
    void testCopy_rootOnly() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        // act
        final TestBSPTree copy = new TestBSPTree();
        copy.copy(tree);

        // assert
        Assertions.assertNotSame(tree, copy);
        Assertions.assertNotSame(tree.getRoot(), copy.getRoot());

        Assertions.assertEquals(tree.count(), copy.count());
    }

    @Test
    void testCopy_withCuts() {
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
        Assertions.assertNotSame(tree, copy);
        assertNodesCopiedRecursive(tree.getRoot(), copy.getRoot());
        Assertions.assertEquals(tree.count(), copy.count());
    }

    @Test
    void testCopy_changesToOneTreeDoNotAffectCopy() {
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
        Assertions.assertEquals(1, tree.count());
        Assertions.assertEquals(5, copy.count());
    }

    @Test
    void testCopy_instancePassedAsArgument() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS);

        // act
        tree.copy(tree);

        // assert
        Assertions.assertEquals(5, tree.count());
    }

    @Test
    void testExtract_singleNodeTree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestBSPTree result = new TestBSPTree();
        result.getRoot().insertCut(TestLine.X_AXIS);

        // act
        result.extract(tree.getRoot());

        // assert
        Assertions.assertNotSame(tree.getRoot(), result.getRoot());
        Assertions.assertEquals(1, tree.count());
        Assertions.assertEquals(1, result.count());

        PartitionTestUtils.assertTreeStructure(tree);
        PartitionTestUtils.assertTreeStructure(result);
    }

    @Test
    void testExtract_clearsExistingNodesInCallingTree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final TestBSPTree result = new TestBSPTree();
        result.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        // act
        result.extract(tree.getRoot());

        // assert
        Assertions.assertNotSame(tree.getRoot(), result.getRoot());
        Assertions.assertEquals(1, tree.count());
        Assertions.assertEquals(1, result.count());

        PartitionTestUtils.assertTreeStructure(tree);
        PartitionTestUtils.assertTreeStructure(result);
    }

    @Test
    void testExtract_internalNode() {
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
        Assertions.assertEquals(7, result.count());

        final List<TestLineSegment> resultSegments = getLineSegments(result);
        Assertions.assertEquals(3, resultSegments.size());

        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                resultSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(Double.NEGATIVE_INFINITY, 0, TestLine.Y_AXIS),
                resultSegments.get(1));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(0, Double.POSITIVE_INFINITY, new TestLine(new TestPoint2D(0, -2), new TestPoint2D(1, -2))),
                resultSegments.get(2));

        Assertions.assertEquals(13, tree.count());

        final List<TestLineSegment> inputSegment = getLineSegments(tree);
        Assertions.assertEquals(6, inputSegment.size());

        PartitionTestUtils.assertTreeStructure(tree);
        PartitionTestUtils.assertTreeStructure(result);
    }

    @Test
    void testExtract_leafNode() {
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
        Assertions.assertNotNull(resultNode);
        Assertions.assertNotSame(node, resultNode);

        Assertions.assertEquals(7, result.count());

        final List<TestLineSegment> resultSegments = getLineSegments(result);
        Assertions.assertEquals(3, resultSegments.size());

        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                resultSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.Y_AXIS),
                resultSegments.get(1));
        PartitionTestUtils.assertSegmentsEqual(
                new TestLineSegment(new TestPoint2D(0, 3), new TestPoint2D(3, 0)),
                resultSegments.get(2));

        Assertions.assertEquals(13, tree.count());

        final List<TestLineSegment> inputSegment = getLineSegments(tree);
        Assertions.assertEquals(6, inputSegment.size());

        PartitionTestUtils.assertTreeStructure(tree);
        PartitionTestUtils.assertTreeStructure(result);
    }

    @Test
    void testExtract_extractFromSameTree() {
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
        Assertions.assertNotNull(resultNode);
        Assertions.assertSame(node, resultNode);

        Assertions.assertEquals(7, tree.count());

        final List<TestLineSegment> resultSegments = getLineSegments(tree);
        Assertions.assertEquals(3, resultSegments.size());

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
    void testTransform_singleNodeTree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(p.getX(), p.getY() + 2));

        // act
        tree.transform(t);

        // assert
        Assertions.assertEquals(1, tree.count());
        Assertions.assertTrue(tree.getRoot().isLeaf());
    }

    @Test
    void testTransform_singleCut() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().insertCut(TestLine.X_AXIS);

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(p.getX(), p.getY() + 2));

        // act
        tree.transform(t);

        // assert
        Assertions.assertEquals(3, tree.count());

        final List<TestLineSegment> segments = getLineSegments(tree);
        Assertions.assertEquals(1, segments.size());

        final TestLineSegment seg = segments.get(0);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.NEGATIVE_INFINITY, 2), seg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.POSITIVE_INFINITY, 2), seg.getEndPoint());
    }

    @Test
    void testTransform_multipleCuts() {
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
        Assertions.assertEquals(9, tree.count());

        final List<TestLineSegment> segments = getLineSegments(tree);
        Assertions.assertEquals(4, segments.size());

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
    void testTransform_xAxisReflection() {
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
        Assertions.assertEquals(4, segments.size());
    }

    @Test
    void testTransform_yAxisReflection() {
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
        Assertions.assertEquals(4, segments.size());
    }

    @Test
    void testTransform_xAndYAxisReflection() {
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
        Assertions.assertEquals(4, segments.size());
    }

    @Test
    void testTreeString() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS);

        // act
        final String str = tree.treeString();

        // assert
        final String[] lines = str.split("\n");
        Assertions.assertEquals(5, lines.length);
        Assertions.assertTrue(lines[0].startsWith("TestNode[cut= TestLineSegment"));
        Assertions.assertTrue(lines[1].startsWith("    [-] TestNode[cut= TestLineSegment"));
        Assertions.assertEquals("        [-] TestNode[cut= null]", lines[2]);
        Assertions.assertEquals("        [+] TestNode[cut= null]", lines[3]);
        Assertions.assertEquals("    [+] TestNode[cut= null]", lines[4]);
    }

    @Test
    void testTreeString_emptyTree() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();

        // act
        final String str = tree.treeString();

        // assert
        Assertions.assertEquals("TestNode[cut= null]", str);
    }

    @Test
    void testTreeString_reachesMaxDepth() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS)
            .getMinus().cut(new TestLine(new TestPoint2D(-2, 1), new TestPoint2D(0, 1)));

        // act
        final String str = tree.treeString(1);

        // assert
        final String[] lines = str.split("\n");
        Assertions.assertEquals(4, lines.length);
        Assertions.assertTrue(lines[0].startsWith("TestNode[cut= TestLineSegment"));
        Assertions.assertTrue(lines[1].startsWith("    [-] TestNode[cut= TestLineSegment"));
        Assertions.assertEquals("        ...", lines[2]);
        Assertions.assertEquals("    [+] TestNode[cut= null]", lines[3]);
    }

    @Test
    void testTreeString_zeroMaxDepth() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS)
            .getMinus().cut(new TestLine(new TestPoint2D(-2, 1), new TestPoint2D(0, 1)));

        // act
        final String str = tree.treeString(0);

        // assert
        final String[] lines = str.split("\n");
        Assertions.assertEquals(2, lines.length);
        Assertions.assertTrue(lines[0].startsWith("TestNode[cut= TestLineSegment"));
        Assertions.assertTrue(lines[1].startsWith("    ..."));
    }

    @Test
    void testTreeString_negativeMaxDepth() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS)
            .getMinus().cut(TestLine.Y_AXIS)
            .getMinus().cut(new TestLine(new TestPoint2D(-2, 1), new TestPoint2D(0, 1)));

        // act
        final String str = tree.treeString(-1);

        // assert
        Assertions.assertEquals("", str);
    }

    @Test
    void testToString() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().insertCut(TestLine.Y_AXIS);

        // act
        final String str = tree.toString();

        // assert
        final String msg = "Unexpected toString() representation: " + str;

        Assertions.assertTrue(str.contains("TestBSPTree"), msg);
        Assertions.assertTrue(str.contains("count= 3"), msg);
        Assertions.assertTrue(str.contains("height= 1"), msg);
    }

    @Test
    void testNodeToString() {
        // arrange
        final TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS);

        // act
        final String str = tree.getRoot().toString();

        // assert
        Assertions.assertTrue(str.contains("TestNode"));
        Assertions.assertTrue(str.contains("cut= TestLineSegment"));
    }

    @Test
    void testSplitIntoTree() {
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

        Assertions.assertEquals(5, tree.count());
        Assertions.assertEquals(2, tree.height());

        Assertions.assertEquals(5, minus.count());
        Assertions.assertEquals(2, minus.height());

        final List<TestLineSegment> minusSegments = getLineSegments(minus);
        Assertions.assertEquals(2, minusSegments.size());
        PartitionTestUtils.assertSegmentsEqual(splitSegment, minusSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(Double.NEGATIVE_INFINITY, 0, TestLine.X_AXIS),
                minusSegments.get(1));

        Assertions.assertEquals(7, plus.count());
        Assertions.assertEquals(3, plus.height());

        final List<TestLineSegment> plusSegments = getLineSegments(plus);
        Assertions.assertEquals(3, plusSegments.size());
        PartitionTestUtils.assertSegmentsEqual(splitSegment, plusSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                plusSegments.get(1));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.Y_AXIS),
                plusSegments.get(2));
    }

    @Test
    void testSplitIntoTree_minusOnly() {
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

        Assertions.assertEquals(5, tree.count());
        Assertions.assertEquals(2, tree.height());

        Assertions.assertEquals(5, minus.count());
        Assertions.assertEquals(2, minus.height());

        final List<TestLineSegment> minusSegments = getLineSegments(minus);
        Assertions.assertEquals(2, minusSegments.size());
        PartitionTestUtils.assertSegmentsEqual(splitSegment, minusSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(Double.NEGATIVE_INFINITY, 0, TestLine.X_AXIS),
                minusSegments.get(1));
    }

    @Test
    void testSplitIntoTree_plusOnly() {
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

        Assertions.assertEquals(5, tree.count());
        Assertions.assertEquals(2, tree.height());

        Assertions.assertEquals(7, plus.count());
        Assertions.assertEquals(3, plus.height());

        final List<TestLineSegment> plusSegments = getLineSegments(plus);
        Assertions.assertEquals(3, plusSegments.size());
        PartitionTestUtils.assertSegmentsEqual(splitSegment, plusSegments.get(0));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.X_AXIS),
                plusSegments.get(1));
        PartitionTestUtils.assertSegmentsEqual(new TestLineSegment(0, Double.POSITIVE_INFINITY, TestLine.Y_AXIS),
                plusSegments.get(2));
    }

    private void assertNodesCopiedRecursive(final TestNode orig, final TestNode copy) {
        Assertions.assertNotSame(orig, copy);

        Assertions.assertEquals(orig.getCut(), copy.getCut());

        if (orig.isLeaf()) {
            Assertions.assertNull(copy.getMinus());
            Assertions.assertNull(copy.getPlus());
        } else {
            Assertions.assertNotSame(orig.getMinus(), copy.getMinus());
            Assertions.assertNotSame(orig.getPlus(), copy.getPlus());

            assertNodesCopiedRecursive(orig.getMinus(), copy.getMinus());
            assertNodesCopiedRecursive(orig.getPlus(), copy.getPlus());
        }

        Assertions.assertEquals(orig.depth(), copy.depth());
        Assertions.assertEquals(orig.count(), copy.count());
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
     * @param transformedTree
     * @param transform
     * @param pointNodeMap
     */
    private static void checkTransformedPointNodeMap(final TestBSPTree transformedTree, final Transform<TestPoint2D> transform,
                                                     final Map<TestPoint2D, TestNode> pointNodeMap) {

        for (final Map.Entry<TestPoint2D, TestNode> entry : pointNodeMap.entrySet()) {
            final TestNode expectedNode = entry.getValue();
            final TestPoint2D transformedPt = transform.apply(entry.getKey());

            final String msg = "Expected transformed point " + transformedPt + " to resolve to node " + expectedNode;
            Assertions.assertSame(expectedNode, transformedTree.findNode(transformedPt, FindNodeCutRule.NODE), msg);
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
