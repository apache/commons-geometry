package org.apache.commons.geometry.core.partition;

import org.apache.commons.geometry.core.partition.BSPTree.Node;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.StubBSPTree;
import org.apache.commons.geometry.core.partition.test.StubHyperplane;
import org.apache.commons.geometry.core.partition.test.StubPoint;
import org.junit.Assert;
import org.junit.Test;

public class AbstractBSPTreeTest {

    @Test
    public void scratch() {
        StubBSPTree<Integer> tree = new StubBSPTree<Integer>();
        StubHyperplane hyper = new StubHyperplane(new StubPoint(2), true);

        tree.getRoot().insertCut(hyper);

        Node<StubPoint, Integer> node = tree.findNode(new StubPoint(4));
        node.setAttribute(1);

        System.out.println(node);

        PartitionTestUtils.printTree(tree);
    }

    @Test
    public void testInitialization() {
        // act
        StubBSPTree<Integer> tree = new StubBSPTree<Integer>();

        // assert
        Node<StubPoint, Integer> root = tree.getRoot();

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
        StubBSPTree<Integer> tree = new StubBSPTree<Integer>();
        StubHyperplane hyper = new StubHyperplane(new StubPoint(2), true);

        // act
        boolean result = tree.getRoot().insertCut(hyper);

        // assert
        Assert.assertTrue(result);

        Node<StubPoint, Integer> root = tree.getRoot();
        assertIsInternalNode(root);
        Assert.assertSame(hyper, root.getCut().getHyperplane());
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
