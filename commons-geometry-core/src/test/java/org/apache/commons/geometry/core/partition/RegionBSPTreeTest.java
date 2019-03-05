package org.apache.commons.geometry.core.partition;

import org.apache.commons.geometry.core.partition.RegionBSPTree.RegionNode;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Test;

public class RegionBSPTreeTest {
    @Test
    public void testInitialization() {
        // act
        RegionBSPTree<TestPoint2D> tree = new RegionBSPTree<>();

        // assert
        RegionNode<TestPoint2D> root = tree.getRoot();

        Assert.assertNotNull(root);
        Assert.assertNull(root.getParent());

        PartitionTestUtils.assertIsLeafNode(root);
        Assert.assertFalse(root.isPlus());
        Assert.assertFalse(root.isMinus());

        Assert.assertSame(tree, root.getTree());
    }
}
