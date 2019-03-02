package org.apache.commons.geometry.core.partition;

import org.apache.commons.geometry.core.partition.BSPTree.Node;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestBSPTree;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestLineSegment;
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
    public void testExtract() {
        // act/assert
        TestBSPTree tree = createDiamond();

       PartitionTestUtils.printTree(tree);

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

    /** Create a BSP tree with a diamond-shaped region and labelled nodes.
     * @return
     */
    private static TestBSPTree createDiamond() {
        TestBSPTree tree = new TestBSPTree();

        Node<TestPoint2D, String> root = tree.getRoot().attr("root");

        root.cut(TestLine.X_AXIS);
        Node<TestPoint2D, String> minusY = root.getPlus().attr("minusY");
        Node<TestPoint2D, String> plusY = root.getMinus().attr("plusY");

        minusY.cut(TestLine.Y_AXIS);
        Node<TestPoint2D, String> minusYPlusX = minusY.getPlus().attr("minusYPlusX");
        Node<TestPoint2D, String> minusYMinusX = minusY.getMinus().attr("minusYMinusX");

        return tree;
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
