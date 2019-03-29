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

public class AbstractBSPTreeMergeSupportTest {

    @Test
    public void testMerge_singleNodeTreeWithSingleNodeTree() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().setAttribute("A");

        AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().setAttribute("B");

        AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assert.assertEquals(1, a.count());
        Assert.assertEquals(1, b.count());
        Assert.assertEquals(1, c.count());

        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("B", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("B", b.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("AB", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());
    }

    @Test
    public void testMerge_singleNodeTreeWithMultiNodeTree() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().setAttribute("B");

        AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assert.assertEquals(3, a.count());
        Assert.assertEquals(1, b.count());
        Assert.assertEquals(3, c.count());

        Assert.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("B", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("B", b.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("Ba", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("Ba", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("BA", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("BA", c.findNode(new TestPoint2D(1, -1)).getAttribute());
    }

    @Test
    public void testMerge_multiNodeTreeWithSingleNodeTree() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().setAttribute("A");

        AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assert.assertEquals(1, a.count());
        Assert.assertEquals(3, b.count());
        Assert.assertEquals(3, c.count());

        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("b", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("B", b.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());
    }

    @Test
    public void testMerge_cutsIntersect() {
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

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assert.assertEquals(3, a.count());
        Assert.assertEquals(3, b.count());
        Assert.assertEquals(7, c.count());

        Assert.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("B", b.findNode(new TestPoint2D(1, 0)).getAttribute());
        Assert.assertEquals("b", b.findNode(new TestPoint2D(-1, 0)).getAttribute());

        Assert.assertEquals("aB", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("ab", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());
    }

    @Test
    public void testMerge_cutsParallel() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assert.assertEquals(3, a.count());
        Assert.assertEquals(3, b.count());
        Assert.assertEquals(3, c.count());

        Assert.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("b", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("B", b.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("ab", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("ab", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());
    }

    @Test
    public void testMerge_cutsAntiParallel() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(new TestLine(new TestPoint2D(1, 0), TestPoint2D.ZERO))
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assert.assertEquals(3, a.count());
        Assert.assertEquals(3, b.count());
        Assert.assertEquals(3, c.count());

        Assert.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("B", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("b", b.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("aB", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("aB", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(1, -1)).getAttribute());
    }

    @Test
    public void testMerge_cutOnPlusSide_parallel() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(new TestLine(new TestPoint2D(0, -2), new TestPoint2D(1, -2)))
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assert.assertEquals(3, a.count());
        Assert.assertEquals(3, b.count());
        Assert.assertEquals(5, c.count());

        Assert.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("b", b.findNode(new TestPoint2D(0, -1)).getAttribute());
        Assert.assertEquals("B", b.findNode(new TestPoint2D(0, -3)).getAttribute());

        Assert.assertEquals("ab", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("ab", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        Assert.assertEquals("AB", c.findNode(new TestPoint2D(-1, -3)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(1, -3)).getAttribute());
    }

    @Test
    public void testMerge_cutOnPlusSide_antiParallel() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(new TestLine(new TestPoint2D(1, -2), new TestPoint2D(0, -2)))
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assert.assertEquals(3, a.count());
        Assert.assertEquals(3, b.count());
        Assert.assertEquals(5, c.count());

        Assert.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("B", b.findNode(new TestPoint2D(0, -1)).getAttribute());
        Assert.assertEquals("b", b.findNode(new TestPoint2D(0, -3)).getAttribute());

        Assert.assertEquals("aB", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("aB", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(-1, -3)).getAttribute());
        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(1, -3)).getAttribute());
    }

    @Test
    public void testMerge_cutOnMinusSide_parallel() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(new TestLine(new TestPoint2D(0, 2), new TestPoint2D(1, 2)))
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assert.assertEquals(3, a.count());
        Assert.assertEquals(3, b.count());
        Assert.assertEquals(5, c.count());

        Assert.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("B", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("b", b.findNode(new TestPoint2D(0, 3)).getAttribute());

        Assert.assertEquals("aB", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("aB", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        Assert.assertEquals("ab", c.findNode(new TestPoint2D(-1, 3)).getAttribute());
        Assert.assertEquals("ab", c.findNode(new TestPoint2D(1, 3)).getAttribute());
    }

    @Test
    public void testMerge_cutOnMinusSide_antiParallel() {
        // arrange
        AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(new TestLine(new TestPoint2D(1, 2), new TestPoint2D(0, 2)))
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assert.assertEquals(3, a.count());
        Assert.assertEquals(3, b.count());
        Assert.assertEquals(5, c.count());

        Assert.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("b", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("B", b.findNode(new TestPoint2D(0, 3)).getAttribute());

        Assert.assertEquals("ab", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("ab", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("Ab", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        Assert.assertEquals("aB", c.findNode(new TestPoint2D(-1, 3)).getAttribute());
        Assert.assertEquals("aB", c.findNode(new TestPoint2D(1, 3)).getAttribute());
    }

    @Test
    public void testMerge_outputIsFirstInput() {
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

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, a);

        // assert
        Assert.assertEquals(7, a.count());
        Assert.assertEquals(3, b.count());

        Assert.assertEquals("B", b.findNode(new TestPoint2D(1, 0)).getAttribute());
        Assert.assertEquals("b", b.findNode(new TestPoint2D(-1, 0)).getAttribute());

        Assert.assertEquals("aB", a.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("ab", a.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("Ab", a.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("AB", a.findNode(new TestPoint2D(1, -1)).getAttribute());
    }

    @Test
    public void testMerge_outputIsSecondInput() {
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

        TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, b);

        // assert
        Assert.assertEquals(3, a.count());
        Assert.assertEquals(7, b.count());

        Assert.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assert.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assert.assertEquals("aB", b.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assert.assertEquals("ab", b.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assert.assertEquals("Ab", b.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assert.assertEquals("AB", b.findNode(new TestPoint2D(1, -1)).getAttribute());
    }

    private static class TestMergeOperator extends AbstractBSPTreeMergeSupport<TestPoint2D, AttributeNode<TestPoint2D, String>> {

        /** Perform the test merge operation with the given arguments.
         * @param input1
         * @param input2
         * @param output
         */
        public void apply(AttributeBSPTree<TestPoint2D, String> input1, AttributeBSPTree<TestPoint2D, String> input2,
                AttributeBSPTree<TestPoint2D, String> output) {
            performMerge(input1, input2, output);
        }

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
