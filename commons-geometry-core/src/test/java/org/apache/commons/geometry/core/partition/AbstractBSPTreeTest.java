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
import java.util.List;

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
        Assert.assertFalse(root.isPlus());
        Assert.assertFalse(root.isMinus());

        Assert.assertTrue(plus.isLeaf());
        Assert.assertTrue(plus.isPlus());
        Assert.assertFalse(plus.isMinus());

        Assert.assertTrue(minus.isLeaf());
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

        // act/assert
        Assert.assertSame(root, tree.findNode(new TestPoint2D(0, 0)));

        Assert.assertSame(root, tree.findNode(new TestPoint2D(1, 0)));
        Assert.assertSame(root, tree.findNode(new TestPoint2D(1, 1)));
        Assert.assertSame(root, tree.findNode(new TestPoint2D(0, 1)));
        Assert.assertSame(root, tree.findNode(new TestPoint2D(-1, 1)));
        Assert.assertSame(root, tree.findNode(new TestPoint2D(-1, 0)));
        Assert.assertSame(root, tree.findNode(new TestPoint2D(-1, -1)));
        Assert.assertSame(root, tree.findNode(new TestPoint2D(0, -1)));
        Assert.assertSame(root, tree.findNode(new TestPoint2D(1, -1)));
    }

    @Test
    public void testFindNode_populatedTree() {
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
        Assert.assertSame(root, tree.findNode(new TestPoint2D(0, 0)));

        Assert.assertSame(root, tree.findNode(new TestPoint2D(1, 0)));
        Assert.assertSame(diagonalCut, tree.findNode(new TestPoint2D(1, 1)));
        Assert.assertSame(yCut, tree.findNode(new TestPoint2D(0, 1)));
        Assert.assertSame(minusXPlusY, tree.findNode(new TestPoint2D(-1, 1)));
        Assert.assertSame(root, tree.findNode(new TestPoint2D(-1, 0)));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(-1, -1)));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(0, -1)));
        Assert.assertSame(minusY, tree.findNode(new TestPoint2D(1, -1)));

        Assert.assertSame(underDiagonal, tree.findNode(new TestPoint2D(0.5, 0.5)));
        Assert.assertSame(aboveDiagonal, tree.findNode(new TestPoint2D(3, 3)));
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
    public void testNodes_emptyTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        List<TestNode> nodes = new ArrayList<>();

        // act
        for (TestNode node : tree.nodes())
        {
            nodes.add(node);
        }

        // assert
        Assert.assertEquals(1, nodes.size());
        Assert.assertSame(tree.getRoot(), nodes.get(0));
    }

    @Test
    public void testNodes_multipleNodes() {
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
        for (TestNode node : tree.nodes())
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
    public void testLeafNodes_emptyTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        List<TestNode> nodes = new ArrayList<>();

        // act
        for (TestNode node : tree.leafNodes())
        {
            nodes.add(node);
        }

        // assert
        Assert.assertEquals(1, nodes.size());
        Assert.assertSame(tree.getRoot(), nodes.get(0));
    }

    @Test
    public void testLeafNodes_multipleNodes() {
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
        for (TestNode node : tree.leafNodes())
        {
            nodes.add(node);
        }

        // assert
        Assert.assertEquals(4, nodes.size());

        Assert.assertSame(root.getMinus().getMinus(), nodes.get(0));
        Assert.assertSame(root.getMinus().getPlus(), nodes.get(1));

        Assert.assertSame(root.getPlus().getMinus(), nodes.get(2));
        Assert.assertSame(root.getPlus().getPlus(), nodes.get(3));
    }

    @Test
    public void testCutNodes_emptyTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        List<TestNode> nodes = new ArrayList<>();

        // act
        for (TestNode node : tree.cutNodes())
        {
            nodes.add(node);
        }

        // assert
        Assert.assertEquals(0, nodes.size());
    }

    @Test
    public void testCutNodes_multipleNodes() {
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
        for (TestNode node : tree.cutNodes())
        {
            nodes.add(node);
        }

        // assert
        Assert.assertEquals(3, nodes.size());
        Assert.assertSame(root, nodes.get(0));

        Assert.assertSame(root.getMinus(), nodes.get(1));
        Assert.assertSame(root.getPlus(), nodes.get(2));
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

    private static List<TestLineSegment> getLineSegments(TestBSPTree tree) {
        List<TestLineSegment> list = new ArrayList<>();

        tree.visit(node -> {
            if (!node.isLeaf()) {
                list.add((TestLineSegment) node.getCut());
            }
        });

        return list;
    }
}
