package org.apache.commons.geometry.core.partition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.partition.BSPTree.Node;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestBSPTree;
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
        Node<TestPoint2D, String> root = tree.getRoot();

        Assert.assertNotNull(root);
        Assert.assertNull(root.getParent());
        Assert.assertNull(root.getAttribute());

        assertIsLeafNode(root);
        Assert.assertFalse(root.isPlus());
        Assert.assertFalse(root.isMinus());

        Assert.assertSame(tree, root.getTree());
    }

    @Test
    public void testNodeStateGetters() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        Node<TestPoint2D, String> root = tree.getRoot();
        root.cut(TestLine.X_AXIS);

        Node<TestPoint2D, String> plus = root.getPlus();
        Node<TestPoint2D, String> minus = root.getMinus();

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
    public void testSetAttribute_node() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        Node<TestPoint2D, String> root = tree.getRoot();

        // act
        root.setAttribute("a");

        // assert
        Assert.assertEquals("a", root.getAttribute());
    }

    @Test
    public void testAttr_node() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        Node<TestPoint2D, String> root = tree.getRoot();

        // act
        Node<TestPoint2D, String> result = root.attr("a");

        // assert
        Assert.assertSame(root, result);
        Assert.assertEquals("a", root.getAttribute());
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

        Node<TestPoint2D, String> root = tree.getRoot();
        assertIsInternalNode(root);

        Assert.assertSame(line, root.getCut().getHyperplane());

        assertIsLeafNode(root.getMinus());
        assertIsLeafNode(root.getPlus());
    }

    @Test
    public void testInsertCut_fitsCutterToCell() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        Node<TestPoint2D, String> node = tree.getRoot()
            .cut(TestLine.X_AXIS)
            .getMinus()
                .cut(TestLine.Y_AXIS)
                .getPlus();

        // act
        boolean result = node.insertCut(new TestLine(0.5, 1.5, 1.5, 0.5));

        // assert
        Assert.assertTrue(result);
        assertIsInternalNode(node);

        TestLineSegment segment = (TestLineSegment) node.getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), segment.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), segment.getEndPoint());
    }

    @Test
    public void testInsertCut_doesNotPassThroughCell_intersects() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        Node<TestPoint2D, String> node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus();

        // act
        boolean result = node.insertCut(new TestLine(-2, 0, 0, -2));

        // assert
        Assert.assertFalse(result);
        assertIsLeafNode(node);
    }

    @Test
    public void testInsertCut_doesNotPassThroughCell_parallel() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        Node<TestPoint2D, String> node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus();

        // act
        boolean result = node.insertCut(new TestLine(0, -1, 1, -1));

        // assert
        Assert.assertFalse(result);
        assertIsLeafNode(node);
    }

    @Test
    public void testInsertCut_doesNotPassThroughCell_removesExistingChildren() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        Node<TestPoint2D, String> node = tree.getRoot()
            .cut(TestLine.X_AXIS)
                .getMinus()
                    .cut(TestLine.Y_AXIS)
                    .getPlus()
                        .cut(new TestLine(0, 2, 2, 0));

        // act
        boolean result = node.insertCut(new TestLine(-2, 0, 0, -2));

        // assert
        Assert.assertFalse(result);
        assertIsLeafNode(node);
    }

    @Test
    public void testInsertCut_cutExistsInTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        Node<TestPoint2D, String> node = tree.getRoot()
                .cut(TestLine.X_AXIS)
                    .getMinus()
                        .cut(TestLine.Y_AXIS)
                        .getPlus()
                            .cut(new TestLine(0, 2, 2, 0));

        // act
        boolean result = node.insertCut(new TestLine(0, 2, 0, 3));

        // assert
        Assert.assertFalse(result);
        assertIsLeafNode(node);
    }

    @Test
    public void testFindNode_emptyTree() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        Node<TestPoint2D, String> root = tree.getRoot();

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

        Node<TestPoint2D, String> root = tree.getRoot().attr("root");
        Node<TestPoint2D, String> minusY = root.getPlus().attr("minusY");

        Node<TestPoint2D, String> yCut = root.getMinus().attr("yCut");
        Node<TestPoint2D, String> minusXPlusY = yCut.getMinus().attr("minusXPlusY");

        Node<TestPoint2D, String> diagonalCut = yCut.getPlus().attr("diagonalCut");
        Node<TestPoint2D, String> underDiagonal = diagonalCut.getPlus().attr("underDiagonal");
        Node<TestPoint2D, String> aboveDiagonal = diagonalCut.getMinus().attr("aboveDiagonal");

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
        Node<TestPoint2D, String> root = tree.getRoot();
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
        Node<TestPoint2D, String> root = tree.getRoot();
        Assert.assertFalse(root.isLeaf());

        Node<TestPoint2D, String> node = tree.findNode(new TestPoint2D(0.5, 0.5));
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
        Node<TestPoint2D, String> root = tree.getRoot();
        Assert.assertFalse(root.isLeaf());

        Node<TestPoint2D, String> plusXPlusY = tree.getRoot().getMinus().getPlus();
        TestLineSegment plusXPlusYSeg = (TestLineSegment) plusXPlusY.getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), plusXPlusYSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), plusXPlusYSeg.getEndPoint());

        Node<TestPoint2D, String> minusY = tree.getRoot().getPlus();
        TestLineSegment minusYSeg = (TestLineSegment) minusY.getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), minusYSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(
                new TestPoint2D(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), minusYSeg.getEndPoint());

        Node<TestPoint2D, String> minusXPlusY = tree.getRoot().getMinus().getMinus();
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
    public void testNodeToString() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        tree.getRoot().cut(TestLine.X_AXIS).attr("abc");

        // act
        String str = tree.getRoot().toString();

        // assert
        Assert.assertTrue(str.contains("SimpleNode"));
        Assert.assertTrue(str.contains("cut= TestLineSegment"));
        Assert.assertTrue(str.contains("attribute= abc"));
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

    private static void assertIsInternalNode(Node<?, ?> node) {
        Assert.assertNotNull(node.getCut());
        Assert.assertNotNull(node.getMinus());
        Assert.assertNotNull(node.getPlus());

        Assert.assertFalse(node.isLeaf());
    }

    private static void assertIsLeafNode(Node<?, ?> node) {
        Assert.assertNull(node.getCut());
        Assert.assertNull(node.getMinus());
        Assert.assertNull(node.getPlus());

        Assert.assertTrue(node.isLeaf());
    }
}
