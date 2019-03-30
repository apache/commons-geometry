package org.apache.commons.geometry.core.partition;

import java.util.Arrays;

import org.apache.commons.geometry.core.partition.AttributeBSPTree.AttributeNode;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestLineSegment;
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
    public void testInitialNodeValue_null() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> tree = new AttributeBSPTree<TestPoint2D, String>();
        tree.getRoot().cut(TestLine.X_AXIS);

        // act/assert
        Assert.assertNull(tree.getRoot().getAttribute());
        Assert.assertNull(tree.getRoot().getPlus().getAttribute());
        Assert.assertNull(tree.getRoot().getMinus().getAttribute());
    }

    @Test
    public void testInitialNodeValue_givenValue() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> tree = new AttributeBSPTree<TestPoint2D, String>("a");
        tree.getRoot().cut(TestLine.X_AXIS);

        // act/assert
        Assert.assertEquals("a", tree.getRoot().getAttribute());
        Assert.assertEquals("a", tree.getRoot().getPlus().getAttribute());
        Assert.assertEquals("a", tree.getRoot().getMinus().getAttribute());
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
    public void testCopy_rootOnly() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> tree = new AttributeBSPTree<TestPoint2D, String>();
        tree.getRoot().attr("abc");

        // act
        AttributeBSPTree<TestPoint2D, String> copy = new AttributeBSPTree<>();
        copy.copy(tree);

        // assert
        Assert.assertEquals(1, copy.count());
        Assert.assertEquals("abc", copy.getRoot().getAttribute());
    }

    @Test
    public void testCopy_withCuts() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> tree = new AttributeBSPTree<TestPoint2D, String>();
        tree.insert(Arrays.asList(
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1))
                ));

        tree.findNode(new TestPoint2D(1, 1)).attr("a");
        tree.findNode(new TestPoint2D(-1, 1)).attr("b");
        tree.findNode(new TestPoint2D(0, -1)).attr("c");

        // act
        AttributeBSPTree<TestPoint2D, String> copy = new AttributeBSPTree<>();
        copy.copy(tree);

        // assert
        Assert.assertEquals(5, copy.count());
        Assert.assertEquals("a", copy.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("b", copy.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("c", copy.findNode(new TestPoint2D(0, -1)).getAttribute());
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
