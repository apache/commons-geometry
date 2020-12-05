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

import java.util.Arrays;
import java.util.function.Supplier;

import org.apache.commons.geometry.core.partitioning.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partitioning.test.TestLine;
import org.apache.commons.geometry.core.partitioning.test.TestLineSegment;
import org.apache.commons.geometry.core.partitioning.test.TestPoint2D;
import org.apache.commons.geometry.core.partitioning.test.TestRegionBSPTree;
import org.junit.jupiter.api.Test;

public class AbstractRegionBSPTreeBooleanTest {

    @Test
    public void testUnion_singleNodeTrees() {
        // act/assert
        unionChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        unionChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();
    }

    @Test
    public void testUnion_simpleCrossingCuts() {
        // act/assert
        unionChecker(AbstractRegionBSPTreeBooleanTest::xAxisTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(false)
            .empty(false)
            .count(3)
            .check();

        unionChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::xAxisTree)
            .full(false)
            .empty(false)
            .count(3)
            .check();

        unionChecker(AbstractRegionBSPTreeBooleanTest::yAxisTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::yAxisTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(AbstractRegionBSPTreeBooleanTest::xAxisTree, AbstractRegionBSPTreeBooleanTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .outside(new TestPoint2D(1, -1))
            .boundary(TestPoint2D.ZERO)
            .count(5)
            .check(t -> {
                final TestLineSegment seg = (TestLineSegment) t.getRoot().getPlus().getCut();

                PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.0, Double.NEGATIVE_INFINITY), seg.getStartPoint());
                PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, seg.getEndPoint());
            });
    }

    @Test
    public void testUnion_mixedCutRules() {
        // arrange
        final Supplier<TestRegionBSPTree> r1 = () -> {
            final TestRegionBSPTree tree = new TestRegionBSPTree(false);
            tree.insert(TestLine.X_AXIS.span(), RegionCutRule.PLUS_INSIDE);
            tree.insert(new TestLine(new TestPoint2D(5, 0), new TestPoint2D(5, 1)).span(), RegionCutRule.INHERIT);

            return tree;
        };

        final Supplier<TestRegionBSPTree> r2 = () -> {
            final TestRegionBSPTree tree = new TestRegionBSPTree(false);
            tree.insert(TestLine.Y_AXIS.span(), RegionCutRule.MINUS_INSIDE);

            return tree;
        };

        // act/assert
        // Note that the tree node count is different from other tests because one input tree is not condensed.
        // However, the resulting regions are equivalent.
        unionChecker(r1, r2)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, -1), new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .outside(new TestPoint2D(1, 1), new TestPoint2D(10, 10))
            .boundary(TestPoint2D.ZERO)
            .count(7)
            .check(t -> {
                final TestLineSegment seg = (TestLineSegment) t.getRoot().getMinus().getMinus().getCut();

                PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, seg.getStartPoint());
                PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.0, Double.POSITIVE_INFINITY), seg.getEndPoint());
            });
    }

    @Test
    public void testUnion_boxTreeWithSingleCutTree() {
        // arrange
        final Supplier<TestRegionBSPTree> boxFactory = () -> {
            final TestRegionBSPTree box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        final Supplier<TestRegionBSPTree> horizontalFactory = () -> {
            final TestRegionBSPTree horizontal = fullTree();
            horizontal.getRoot().insertCut(new TestLine(new TestPoint2D(2, 2), new TestPoint2D(0, 2)));

            return horizontal;
        };

        // act/assert
        unionChecker(horizontalFactory, boxFactory)
            .count(3)
            .inside(TestPoint2D.ZERO, new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(0, 3), new TestPoint2D(3, 3))
            .boundary(new TestPoint2D(-1, 2), new TestPoint2D(3, 2))
            .check();
    }

    @Test
    public void testUnion_treeWithComplement() {
        // arrange
        final Supplier<TestRegionBSPTree> treeFactory = () -> {
            final TestRegionBSPTree t = fullTree();
            insertSkewedBowtie(t);

            return t;
        };
        final Supplier<TestRegionBSPTree> complementFactory = () -> {
            final TestRegionBSPTree t = treeFactory.get();
            t.complement();

            return t;
        };

        // act/assert
        unionChecker(treeFactory, complementFactory)
            .full(true)
            .empty(false)
            .count(1)
            .check();
    }

    @Test
    public void testIntersection_singleNodeTrees() {
        // act/assert
        intersectionChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();
    }

    @Test
    public void testIntersection_simpleCrossingCuts() {
        // act/assert
        intersectionChecker(AbstractRegionBSPTreeBooleanTest::xAxisTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::xAxisTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(AbstractRegionBSPTreeBooleanTest::yAxisTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .outside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        intersectionChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .outside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        intersectionChecker(AbstractRegionBSPTreeBooleanTest::xAxisTree, AbstractRegionBSPTreeBooleanTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(-1, 1))
            .outside(new TestPoint2D(1, 1), new TestPoint2D(1, -1), new TestPoint2D(-1, -1))
            .boundary(TestPoint2D.ZERO)
            .count(5)
            .check(t -> {
                final TestLineSegment seg = (TestLineSegment) t.getRoot().getMinus().getCut();

                PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, seg.getStartPoint());
                PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.0, Double.POSITIVE_INFINITY), seg.getEndPoint());
            });
    }

    @Test
    public void testIntersection_boxTreeWithSingleCutTree() {
        // arrange
        final Supplier<TestRegionBSPTree> boxFactory = () -> {
            final TestRegionBSPTree box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        final Supplier<TestRegionBSPTree> horizontalFactory = () -> {
            final TestRegionBSPTree horizontal = fullTree();
            horizontal.getRoot().insertCut(new TestLine(new TestPoint2D(2, -2), new TestPoint2D(0, -2)));

            return horizontal;
        };

        // act/assert
        intersectionChecker(horizontalFactory, boxFactory)
            .inside(new TestPoint2D(1, -3))
            .outside(new TestPoint2D(1, -1), new TestPoint2D(-1, -3),
                    new TestPoint2D(1, -5), new TestPoint2D(3, -3))
            .boundary(new TestPoint2D(0, -2), new TestPoint2D(2, -2),
                    new TestPoint2D(0, -4), new TestPoint2D(2, -4))
            .count(9)
            .check();
    }

    @Test
    public void testIntersection_treeWithComplement() {
        // arrange
        final Supplier<TestRegionBSPTree> treeFactory = () -> {
            final TestRegionBSPTree t = fullTree();
            insertSkewedBowtie(t);

            return t;
        };
        final Supplier<TestRegionBSPTree> complementFactory = () -> {
            final TestRegionBSPTree t = treeFactory.get();
            t.complement();

            return t;
        };

        // act/assert
        intersectionChecker(treeFactory, complementFactory)
            .full(false)
            .empty(true)
            .count(1)
            .check();
    }

    @Test
    public void testDifference_singleNodeTrees() {
        // act/assert
        differenceChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        differenceChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();
    }

    @Test
    public void testDifference_simpleCrossingCuts() {
        // act/assert
        differenceChecker(AbstractRegionBSPTreeBooleanTest::xAxisTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(0, 1))
            .outside(new TestPoint2D(0, -1))
            .boundary(TestPoint2D.ZERO)
            .count(3)
            .check();

        differenceChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::xAxisTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(AbstractRegionBSPTreeBooleanTest::yAxisTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        differenceChecker(AbstractRegionBSPTreeBooleanTest::xAxisTree, AbstractRegionBSPTreeBooleanTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(1, -1), new TestPoint2D(-1, -1))
            .boundary(TestPoint2D.ZERO)
            .count(5)
            .check(t -> {
                final TestLineSegment seg = (TestLineSegment) t.getRoot().getMinus().getCut();

                PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, seg.getStartPoint());
                PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.0, Double.POSITIVE_INFINITY), seg.getEndPoint());
            });
    }

    @Test
    public void testDifference_boxTreeWithSingleCutTree() {
        // arrange
        final Supplier<TestRegionBSPTree> boxFactory = () -> {
            final TestRegionBSPTree box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        final Supplier<TestRegionBSPTree> horizontalFactory = () -> {
            final TestRegionBSPTree horizontal = fullTree();
            horizontal.getRoot().insertCut(new TestLine(new TestPoint2D(2, -2), new TestPoint2D(0, -2)));

            return horizontal;
        };

        // act/assert
        differenceChecker(horizontalFactory, boxFactory)
            .inside(new TestPoint2D(-1, -3), new TestPoint2D(-1, -5),
                    new TestPoint2D(1, -5), new TestPoint2D(3, -5),
                    new TestPoint2D(4, -3))
            .outside(new TestPoint2D(1, -1), new TestPoint2D(1, -1),
                    new TestPoint2D(3, -1), new TestPoint2D(1, -3))
            .boundary(new TestPoint2D(0, -2), new TestPoint2D(0, -4),
                    new TestPoint2D(2, -4), new TestPoint2D(2, -2))
            .count(9)
            .check();
    }

    @Test
    public void testDifference_treeWithCopy() {
        // arrange
        final Supplier<TestRegionBSPTree> treeFactory = () -> {
            final TestRegionBSPTree t = fullTree();
            insertSkewedBowtie(t);

            return t;
        };

        // act/assert
        differenceChecker(treeFactory, treeFactory)
            .full(false)
            .empty(true)
            .count(1)
            .check();
    }

    @Test
    public void testXor_singleNodeTrees() {
        // act/assert
        xorChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        xorChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        xorChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        xorChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();
    }

    @Test
    public void testXor_simpleCrossingCuts() {
        // act/assert
        xorChecker(AbstractRegionBSPTreeBooleanTest::xAxisTree, AbstractRegionBSPTreeBooleanTest::emptyTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(0, 1))
            .outside(new TestPoint2D(0, -1))
            .boundary(TestPoint2D.ZERO)
            .count(3)
            .check();

        xorChecker(AbstractRegionBSPTreeBooleanTest::emptyTree, AbstractRegionBSPTreeBooleanTest::xAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(0, 1))
            .outside(new TestPoint2D(0, -1))
            .boundary(TestPoint2D.ZERO)
            .count(3)
            .check();

        xorChecker(AbstractRegionBSPTreeBooleanTest::yAxisTree, AbstractRegionBSPTreeBooleanTest::fullTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        xorChecker(AbstractRegionBSPTreeBooleanTest::fullTree, AbstractRegionBSPTreeBooleanTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        xorChecker(AbstractRegionBSPTreeBooleanTest::xAxisTree, AbstractRegionBSPTreeBooleanTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(-1, -1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(1, -1))
            .boundary(TestPoint2D.ZERO)
            .count(7)
            .check(t -> {
                final TestLineSegment minusSeg = (TestLineSegment) t.getRoot().getMinus().getCut();

                PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, minusSeg.getStartPoint());
                PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.0, Double.POSITIVE_INFINITY), minusSeg.getEndPoint());

                final TestLineSegment plusSeg = (TestLineSegment) t.getRoot().getPlus().getCut();

                PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.0, Double.NEGATIVE_INFINITY), plusSeg.getStartPoint());
                PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, plusSeg.getEndPoint());
            });
    }

    @Test
    public void testXor_boxTreeWithSingleCutTree() {
        // arrange
        final Supplier<TestRegionBSPTree> boxFactory = () -> {
            final TestRegionBSPTree box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        final Supplier<TestRegionBSPTree> horizontalFactory = () -> {
            final TestRegionBSPTree horizontal = fullTree();
            horizontal.getRoot().insertCut(new TestLine(new TestPoint2D(2, -2), new TestPoint2D(0, -2)));

            return horizontal;
        };

        // act/assert
        xorChecker(horizontalFactory, boxFactory)
            .inside(new TestPoint2D(-1, -3), new TestPoint2D(-1, -5),
                    new TestPoint2D(1, -5), new TestPoint2D(3, -5),
                    new TestPoint2D(4, -3), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(3, -1), new TestPoint2D(1, -3),
                    new TestPoint2D(1, 1), new TestPoint2D(5, -1))
            .boundary(new TestPoint2D(0, -2), new TestPoint2D(0, -4),
                    new TestPoint2D(2, -4), new TestPoint2D(2, -2),
                    TestPoint2D.ZERO, new TestPoint2D(2, 0))
            .count(15)
            .check();
    }

    @Test
    public void testXor_treeWithComplement() {
        // arrange
        final Supplier<TestRegionBSPTree> treeFactory = () -> {
            final TestRegionBSPTree t = fullTree();
            insertSkewedBowtie(t);

            return t;
        };
        final Supplier<TestRegionBSPTree> complementFactory = () -> {
            final TestRegionBSPTree t = treeFactory.get();
            t.complement();

            return t;
        };

        // act/assert
        xorChecker(treeFactory, complementFactory)
            .full(true)
            .empty(false)
            .count(1)
            .check();
    }

    private static TestRegionBSPTree emptyTree() {
        return new TestRegionBSPTree(false);
    }

    private static TestRegionBSPTree fullTree() {
        return new TestRegionBSPTree(true);
    }

    private static TestRegionBSPTree xAxisTree() {
        final TestRegionBSPTree tree = fullTree();
        tree.getRoot().cut(TestLine.X_AXIS);

        return tree;
    }

    private static TestRegionBSPTree yAxisTree() {
        final TestRegionBSPTree tree = fullTree();
        tree.getRoot().cut(TestLine.Y_AXIS);

        return tree;
    }

    private static void insertBox(final TestRegionBSPTree tree, final TestPoint2D upperLeft,
            final TestPoint2D lowerRight) {
        final TestPoint2D upperRight = new TestPoint2D(lowerRight.getX(), upperLeft.getY());
        final TestPoint2D lowerLeft = new TestPoint2D(upperLeft.getX(), lowerRight.getY());

        tree.insert(Arrays.asList(
                    new TestLineSegment(lowerRight, upperRight),
                    new TestLineSegment(upperRight, upperLeft),
                    new TestLineSegment(upperLeft, lowerLeft),
                    new TestLineSegment(lowerLeft, lowerRight)
                ));
    }

    private static void insertSkewedBowtie(final TestRegionBSPTree tree) {
        tree.insert(Arrays.asList(
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),

                new TestLineSegment(new TestPoint2D(4, 0), new TestPoint2D(4, 1)),
                new TestLineSegment(new TestPoint2D(-4, 0), new TestPoint2D(-4, -1)),

                new TestLineSegment(new TestPoint2D(4, 5), new TestPoint2D(-1, 0)),
                new TestLineSegment(new TestPoint2D(-4, -5), new TestPoint2D(1, 0))));
    }

    private static MergeChecker unionChecker(
            final Supplier<TestRegionBSPTree> r1,
            final Supplier<TestRegionBSPTree> r2) {

        final MergeChecker.Operation constOperation = (a, b) -> {
            final TestRegionBSPTree result = fullTree();
            result.union(a, b);

            return result;
        };

        final MergeChecker.Operation inPlaceOperation = (a, b) -> {
            a.union(b);
            return a;
        };

        return new MergeChecker(r1, r2, constOperation, inPlaceOperation);
    }

    private static MergeChecker intersectionChecker(
            final Supplier<TestRegionBSPTree> tree1Factory,
            final Supplier<TestRegionBSPTree> tree2Factory) {

        final MergeChecker.Operation constOperation = (a, b) -> {
            final TestRegionBSPTree result = fullTree();
            result.intersection(a, b);
            return result;
        };

        final MergeChecker.Operation inPlaceOperation = (a, b) -> {
            a.intersection(b);
            return a;
        };

        return new MergeChecker(tree1Factory, tree2Factory, constOperation, inPlaceOperation);
    }

    private static MergeChecker differenceChecker(
            final Supplier<TestRegionBSPTree> tree1Factory,
            final Supplier<TestRegionBSPTree> tree2Factory) {

        final MergeChecker.Operation constOperation = (a, b) -> {
            final TestRegionBSPTree result = fullTree();
            result.difference(a, b);
            return result;
        };

        final MergeChecker.Operation inPlaceOperation = (a, b) -> {
            a.difference(b);
            return a;
        };

        return new MergeChecker(tree1Factory, tree2Factory, constOperation, inPlaceOperation);
    }

    private static MergeChecker xorChecker(
            final Supplier<TestRegionBSPTree> tree1Factory,
            final Supplier<TestRegionBSPTree> tree2Factory) {

        final MergeChecker.Operation constOperation = (a, b) -> {
            final TestRegionBSPTree result = fullTree();
            result.xor(a, b);
            return result;
        };

        final MergeChecker.Operation inPlaceOperation = (a, b) -> {
            a.xor(b);
            return a;
        };

        return new MergeChecker(tree1Factory, tree2Factory, constOperation, inPlaceOperation);
    }
}
