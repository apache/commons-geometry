package org.apache.commons.geometry.core.partition;

import org.apache.commons.geometry.core.partition.AttributeBSPTree.AttributeNode;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Test;

public class AttributeBSPTreeTest {

    @Test
    public void testInitialization() {
        // act
        AttributeBSPTree<TestPoint2D, String> tree = new AttributeBSPTree<TestPoint2D, String>();

        // assert
        AttributeNode<TestPoint2D, String> root = tree.getRoot();

        Assert.assertNotNull(root);
        Assert.assertNull(root.getParent());
        Assert.assertNull(root.getAttribute());

        PartitionTestUtils.assertIsLeafNode(root);
        Assert.assertFalse(root.isPlus());
        Assert.assertFalse(root.isMinus());

        Assert.assertSame(tree, root.getTree());
    }

    @Test
    public void testSetAttribute_node() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> tree = new AttributeBSPTree<TestPoint2D, String>();
        AttributeNode<TestPoint2D, String> root = tree.getRoot();

        // act
        root.setAttribute("a");

        // assert
        Assert.assertEquals("a", root.getAttribute());
    }

    @Test
    public void testAttr_node() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> tree = new AttributeBSPTree<TestPoint2D, String>();
        AttributeNode<TestPoint2D, String> root = tree.getRoot();

        // act
        AttributeNode<TestPoint2D, String> result = root.attr("a");

        // assert
        Assert.assertSame(root, result);
        Assert.assertEquals("a", root.getAttribute());
    }

    @Test
    public void testNodeToString() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> tree = new AttributeBSPTree<TestPoint2D, String>();
        tree.getRoot().cut(TestLine.X_AXIS).attr("abc");

        // act
        String str = tree.getRoot().toString();

        // assert
        Assert.assertTrue(str.contains("AttributeNode"));
        Assert.assertTrue(str.contains("cut= TestLineSegment"));
        Assert.assertTrue(str.contains("attribute= abc"));
    }
}
