package org.apache.commons.geometry.core.partition.bsp;

import java.util.Arrays;

import org.apache.commons.geometry.core.partition.bsp.AttributeBSPTree;
import org.apache.commons.geometry.core.partition.bsp.AttributeBSPTree.AttributeNode;
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
    public void testExtract() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> tree = new AttributeBSPTree<TestPoint2D, String>();
        tree.insert(Arrays.asList(
                new TestLineSegment(new TestPoint2D(-1, -1), new TestPoint2D(1, 1)),
                new TestLineSegment(new TestPoint2D(-1, 1), new TestPoint2D(1, -1)),

                new TestLineSegment(new TestPoint2D(-1, 3), new TestPoint2D(1, 3)),
                new TestLineSegment(new TestPoint2D(3, 1), new TestPoint2D(3, -1)),
                new TestLineSegment(new TestPoint2D(1, -3), new TestPoint2D(-1, -3)),
                new TestLineSegment(new TestPoint2D(-3, -1), new TestPoint2D(-3, 1))
                ));

        AttributeNode<TestPoint2D, String> root = tree.getRoot();

        root.attr("R");
        root.getMinus().attr("A");
        root.getPlus().attr("B");

        root.getMinus().getMinus().forEach(n -> n.attr("a"));
        root.getMinus().getPlus().forEach(n -> n.attr("b"));

        root.getPlus().getPlus().forEach(n -> n.attr("c"));
        root.getPlus().getMinus().forEach(n -> n.attr("d"));

        AttributeBSPTree<TestPoint2D, String> result = new AttributeBSPTree<>();

        // act
        result.extract(tree.findNode(new TestPoint2D(0, 1)));

        // assert
        Assert.assertEquals(7, result.count());
        Assert.assertEquals(15, tree.count());

        // check result tree attributes
        AttributeNode<TestPoint2D, String> resultRoot = result.getRoot();
        Assert.assertEquals("R", resultRoot.getAttribute());
        Assert.assertEquals("A", resultRoot.getMinus().getAttribute());
        Assert.assertEquals("B", resultRoot.getPlus().getAttribute());

        Assert.assertEquals("a", resultRoot.getMinus().getMinus().getAttribute());
        Assert.assertEquals("b", resultRoot.getMinus().getPlus().getAttribute());

        Assert.assertEquals(2, resultRoot.getMinus().height());
        Assert.assertEquals(0, resultRoot.getPlus().height());

        PartitionTestUtils.assertTreeStructure(result);

        // check original tree attributes
        Assert.assertEquals("R", root.getAttribute());
        Assert.assertEquals("A", root.getMinus().getAttribute());
        Assert.assertEquals("B", root.getPlus().getAttribute());

        Assert.assertEquals("a", root.getMinus().getMinus().getAttribute());
        Assert.assertEquals("b", root.getMinus().getPlus().getAttribute());
        Assert.assertEquals("c", root.getPlus().getPlus().getAttribute());
        Assert.assertEquals("d", root.getPlus().getMinus().getAttribute());

        Assert.assertEquals(2, root.getMinus().height());
        Assert.assertEquals(2, root.getPlus().height());

        PartitionTestUtils.assertTreeStructure(tree);
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
