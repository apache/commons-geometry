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
        Node<TestPoint2D, Integer> root = tree.getRoot();

        Assert.assertNotNull(root);
        Assert.assertNull(root.getParent());
        Assert.assertNull(root.getAttribute());

        assertIsLeafNode(root);
        Assert.assertFalse(root.isPlus());
        Assert.assertFalse(root.isMinus());

        Assert.assertSame(tree, root.getTree());
    }

    @Test
    public void testInsertCut() {
        // arrange
        TestBSPTree tree = new TestBSPTree();
        TestLine line = new TestLine(0, 0, 1, 0);

        // act
        boolean result = tree.getRoot().insertCut(line);

        // assert
        Assert.assertTrue(result);

        Node<TestPoint2D, Integer> root = tree.getRoot();
        assertIsInternalNode(root);

        Assert.assertSame(line, root.getCut().getHyperplane());

        assertIsLeafNode(root.getMinus());
        assertIsLeafNode(root.getPlus());
    }

    @Test
    public void testInsertCut_fitsCutterToCell() {
        // arrange
        TestBSPTree tree = new TestBSPTree();

        Node<TestPoint2D, Integer> node = tree.getRoot()
            .cut(new TestLine(0, 0, 1, 0))
            .getMinus()
                .cut(new TestLine(0, 1, 0, 2))
                .getPlus()
                    .attr(1);

        // act
        boolean result = node.insertCut(new TestLine(0, 2, 2, 0));

        // assert
        Assert.assertTrue(result);

        TestLineSegment segment = (TestLineSegment) node.getCut();

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 2), segment.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(2, 0), segment.getEndPoint());
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
