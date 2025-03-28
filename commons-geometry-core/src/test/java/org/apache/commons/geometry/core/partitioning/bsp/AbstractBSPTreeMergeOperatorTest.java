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
package org.apache.commons.geometry.core.partitioning.bsp;

import java.util.stream.StreamSupport;

import org.apache.commons.geometry.core.partitioning.test.AttributeBSPTree;
import org.apache.commons.geometry.core.partitioning.test.AttributeBSPTree.AttributeNode;
import org.apache.commons.geometry.core.partitioning.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partitioning.test.TestLine;
import org.apache.commons.geometry.core.partitioning.test.TestPoint2D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AbstractBSPTreeMergeOperatorTest {

    @Test
    void testMerge_singleNodeTreeWithSingleNodeTree() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().setAttribute("A");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().setAttribute("B");

        final AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assertions.assertEquals(1, a.count());
        Assertions.assertEquals(1, b.count());
        Assertions.assertEquals(1, c.count());

        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("B", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("B", b.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
        PartitionTestUtils.assertTreeStructure(c);
    }

    @Test
    void testMerge_singleNodeTreeWithMultiNodeTree() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().setAttribute("B");

        final AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assertions.assertEquals(3, a.count());
        Assertions.assertEquals(1, b.count());
        Assertions.assertEquals(3, c.count());

        Assertions.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("B", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("B", b.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("Ba", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("Ba", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("BA", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("BA", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
        PartitionTestUtils.assertTreeStructure(c);
    }

    @Test
    void testMerge_multiNodeTreeWithSingleNodeTree() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().setAttribute("A");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        final AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assertions.assertEquals(1, a.count());
        Assertions.assertEquals(3, b.count());
        Assertions.assertEquals(3, c.count());

        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("b", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("B", b.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("Ab", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("Ab", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
        PartitionTestUtils.assertTreeStructure(c);
    }

    @Test
    void testMerge_cutsIntersect() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(TestLine.Y_AXIS)
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        final AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assertions.assertEquals(3, a.count());
        Assertions.assertEquals(3, b.count());
        Assertions.assertEquals(7, c.count());

        Assertions.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("B", b.findNode(new TestPoint2D(1, 0)).getAttribute());
        Assertions.assertEquals("b", b.findNode(new TestPoint2D(-1, 0)).getAttribute());

        Assertions.assertEquals("aB", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("ab", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("Ab", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
        PartitionTestUtils.assertTreeStructure(c);
    }

    @Test
    void testMerge_cutsParallel() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        final AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assertions.assertEquals(3, a.count());
        Assertions.assertEquals(3, b.count());
        Assertions.assertEquals(3, c.count());

        Assertions.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("b", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("B", b.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("ab", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("ab", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
        PartitionTestUtils.assertTreeStructure(c);
    }

    @Test
    void testMerge_cutsAntiParallel() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(new TestLine(new TestPoint2D(1, 0), TestPoint2D.ZERO))
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        final AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assertions.assertEquals(3, a.count());
        Assertions.assertEquals(3, b.count());
        Assertions.assertEquals(3, c.count());

        Assertions.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("B", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("b", b.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("aB", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("aB", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("Ab", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("Ab", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
        PartitionTestUtils.assertTreeStructure(c);
    }

    @Test
    void testMerge_cutOnPlusSide_parallel() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(new TestLine(new TestPoint2D(0, -2), new TestPoint2D(1, -2)))
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        final AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assertions.assertEquals(3, a.count());
        Assertions.assertEquals(3, b.count());
        Assertions.assertEquals(5, c.count());

        Assertions.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("b", b.findNode(new TestPoint2D(0, -1)).getAttribute());
        Assertions.assertEquals("B", b.findNode(new TestPoint2D(0, -3)).getAttribute());

        Assertions.assertEquals("ab", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("ab", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("Ab", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("Ab", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(-1, -3)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(1, -3)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
        PartitionTestUtils.assertTreeStructure(c);
    }

    @Test
    void testMerge_cutOnPlusSide_antiParallel() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(new TestLine(new TestPoint2D(1, -2), new TestPoint2D(0, -2)))
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        final AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assertions.assertEquals(3, a.count());
        Assertions.assertEquals(3, b.count());
        Assertions.assertEquals(5, c.count());

        Assertions.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("B", b.findNode(new TestPoint2D(0, -1)).getAttribute());
        Assertions.assertEquals("b", b.findNode(new TestPoint2D(0, -3)).getAttribute());

        Assertions.assertEquals("aB", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("aB", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        Assertions.assertEquals("Ab", c.findNode(new TestPoint2D(-1, -3)).getAttribute());
        Assertions.assertEquals("Ab", c.findNode(new TestPoint2D(1, -3)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
        PartitionTestUtils.assertTreeStructure(c);
    }

    @Test
    void testMerge_cutOnMinusSide_parallel() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(new TestLine(new TestPoint2D(0, 2), new TestPoint2D(1, 2)))
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        final AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assertions.assertEquals(3, a.count());
        Assertions.assertEquals(3, b.count());
        Assertions.assertEquals(5, c.count());

        Assertions.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("B", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("b", b.findNode(new TestPoint2D(0, 3)).getAttribute());

        Assertions.assertEquals("aB", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("aB", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("AB", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        Assertions.assertEquals("ab", c.findNode(new TestPoint2D(-1, 3)).getAttribute());
        Assertions.assertEquals("ab", c.findNode(new TestPoint2D(1, 3)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
        PartitionTestUtils.assertTreeStructure(c);
    }

    @Test
    void testMerge_cutOnMinusSide_antiParallel() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(new TestLine(new TestPoint2D(1, 2), new TestPoint2D(0, 2)))
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        final AttributeBSPTree<TestPoint2D, String> c = new AttributeBSPTree<>();

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, c);

        // assert
        Assertions.assertEquals(3, a.count());
        Assertions.assertEquals(3, b.count());
        Assertions.assertEquals(5, c.count());

        Assertions.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("b", b.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("B", b.findNode(new TestPoint2D(0, 3)).getAttribute());

        Assertions.assertEquals("ab", c.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("ab", c.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("Ab", c.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("Ab", c.findNode(new TestPoint2D(1, -1)).getAttribute());

        Assertions.assertEquals("aB", c.findNode(new TestPoint2D(-1, 3)).getAttribute());
        Assertions.assertEquals("aB", c.findNode(new TestPoint2D(1, 3)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
        PartitionTestUtils.assertTreeStructure(c);
    }

    @Test
    void testMerge_outputIsFirstInput() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(TestLine.Y_AXIS)
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, a);

        // assert
        Assertions.assertEquals(7, a.count());
        Assertions.assertEquals(3, b.count());

        Assertions.assertEquals("B", b.findNode(new TestPoint2D(1, 0)).getAttribute());
        Assertions.assertEquals("b", b.findNode(new TestPoint2D(-1, 0)).getAttribute());

        Assertions.assertEquals("aB", a.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("ab", a.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("Ab", a.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("AB", a.findNode(new TestPoint2D(1, -1)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
    }

    @Test
    void testMerge_outputIsSecondInput() {
        // arrange
        final AttributeBSPTree<TestPoint2D, String> a = new AttributeBSPTree<>();
        a.getRoot().cut(TestLine.X_AXIS)
            .getPlus().attr("A")
            .getParent()
            .getMinus().attr("a");

        final AttributeBSPTree<TestPoint2D, String> b = new AttributeBSPTree<>();
        b.getRoot().cut(TestLine.Y_AXIS)
            .getPlus().attr("B")
            .getParent()
            .getMinus().attr("b");

        final TestMergeOperator mergeOp = new TestMergeOperator();

        // act
        mergeOp.apply(a, b, b);

        // assert
        Assertions.assertEquals(3, a.count());
        Assertions.assertEquals(7, b.count());

        Assertions.assertEquals("a", a.findNode(new TestPoint2D(0, 1)).getAttribute());
        Assertions.assertEquals("A", a.findNode(new TestPoint2D(0, -1)).getAttribute());

        Assertions.assertEquals("aB", b.findNode(new TestPoint2D(1, 1)).getAttribute());
        Assertions.assertEquals("ab", b.findNode(new TestPoint2D(-1, 1)).getAttribute());
        Assertions.assertEquals("Ab", b.findNode(new TestPoint2D(-1, -1)).getAttribute());
        Assertions.assertEquals("AB", b.findNode(new TestPoint2D(1, -1)).getAttribute());

        PartitionTestUtils.assertTreeStructure(a);
        PartitionTestUtils.assertTreeStructure(b);
    }

    private static final class TestMergeOperator extends
        AbstractBSPTreeMergeOperator<TestPoint2D, AttributeNode<TestPoint2D, String>> {

        /** Perform the test merge operation with the given arguments.
         * @param input1
         * @param input2
         * @param output
         */
        public void apply(final AttributeBSPTree<TestPoint2D, String> input1, final AttributeBSPTree<TestPoint2D, String> input2,
                          final AttributeBSPTree<TestPoint2D, String> output) {
            performMerge(input1, input2, output);
        }

        /** {@inheritDoc} */
        @Override
        protected AttributeNode<TestPoint2D, String> mergeLeaf(final AttributeNode<TestPoint2D, String> node1,
                                                               final AttributeNode<TestPoint2D, String> node2) {

            final AttributeNode<TestPoint2D, String> leaf = node1.isLeaf() ? node1 : node2;
            final AttributeNode<TestPoint2D, String> subtree = node1.isInternal() ? node1 : node2;

            final String attr = leaf.getAttribute();

            final AttributeNode<TestPoint2D, String> output = outputSubtree(subtree);
            StreamSupport.stream(output.nodes().spliterator(), false)
                .filter(BSPTree.Node::isLeaf)
                .forEach(n -> n.setAttribute(attr + n.getAttribute()));

            return output;
        }
    }
}
