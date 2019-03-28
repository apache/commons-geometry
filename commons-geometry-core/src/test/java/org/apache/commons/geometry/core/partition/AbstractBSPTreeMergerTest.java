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

import org.apache.commons.geometry.core.partition.AttributeBSPTree.AttributeNode;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Test;

public class AbstractBSPTreeMergerTest {

    @Test
    public void testMerge() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(TestLine.Y_AXIS)
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        TestMerger merger = new TestMerger();

        // act
        merger.merge(a, b, c);

        // assert
        Assert.assertEquals(7, c.count());

        Assert.assertEquals("aB", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("ab", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());
    }

    private static class TestMerger extends AbstractBSPTreeMerger<TestPoint2D, AttributeNode<TestPoint2D, String>> {

        /** {@inheritDoc} */
        @Override
        protected AttributeNode<TestPoint2D, String> mergeLeaf(AttributeNode<TestPoint2D, String> node1,
                AttributeNode<TestPoint2D, String> node2) {

            final AttributeNode<TestPoint2D, String> leaf = node1.isLeaf() ? node1 : node2;
            final AttributeNode<TestPoint2D, String> subtree = node1.isInternal() ? node1 : node2;

            String attr = leaf.getAttribute();

            AttributeNode<TestPoint2D, String> output = outputSubtree(subtree);
            output.stream().filter(BSPTree.Node::isLeaf).forEach(n -> n.setAttribute(attr + n.getAttribute()));

            return output;
        }
    }
}
