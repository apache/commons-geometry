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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.BoundarySource;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree.RegionSizeProperties;
import org.apache.commons.geometry.core.partitioning.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partitioning.test.TestLine;
import org.apache.commons.geometry.core.partitioning.test.TestLineSegment;
import org.apache.commons.geometry.core.partitioning.test.TestLineSegmentCollection;
import org.apache.commons.geometry.core.partitioning.test.TestPoint2D;
import org.apache.commons.geometry.core.partitioning.test.TestRegionBSPTree;
import org.apache.commons.geometry.core.partitioning.test.TestRegionBSPTree.TestRegionNode;
import org.apache.commons.geometry.core.partitioning.test.TestTransform2D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AbstractRegionBSPTreeTest {

    private TestRegionBSPTree tree;

    private TestRegionNode root;

    @BeforeEach
    public void setup() {
        tree = new TestRegionBSPTree();
        root = tree.getRoot();
    }

    @Test
    public void testDefaultConstructor() {
        // assert
        Assertions.assertNotNull(root);
        Assertions.assertNull(root.getParent());

        PartitionTestUtils.assertIsLeafNode(root);
        Assertions.assertFalse(root.isPlus());
        Assertions.assertFalse(root.isMinus());

        Assertions.assertSame(tree, root.getTree());

        Assertions.assertEquals(RegionLocation.INSIDE, root.getLocation());
    }

    @Test
    public void testParameterizedConstructor_true() {
        // act
        tree = new TestRegionBSPTree(true);
        root = tree.getRoot();

        // assert
        Assertions.assertNotNull(root);
        Assertions.assertNull(root.getParent());

        PartitionTestUtils.assertIsLeafNode(root);
        Assertions.assertFalse(root.isPlus());
        Assertions.assertFalse(root.isMinus());

        Assertions.assertSame(tree, root.getTree());

        Assertions.assertEquals(RegionLocation.INSIDE, root.getLocation());
    }

    @Test
    public void testParameterizedConstructor_false() {
        // act
        tree = new TestRegionBSPTree(false);
        root = tree.getRoot();

        // assert
        Assertions.assertNotNull(root);
        Assertions.assertNull(root.getParent());

        PartitionTestUtils.assertIsLeafNode(root);
        Assertions.assertFalse(root.isPlus());
        Assertions.assertFalse(root.isMinus());

        Assertions.assertSame(tree, root.getTree());

        Assertions.assertEquals(RegionLocation.OUTSIDE, root.getLocation());
    }

    @Test
    public void testInsert_hyperplaneSubsets_mixedCutRules() {
        // act/assert
        checkMixedCutRuleInsertion(segs -> {
            tree.insert(new TestLineSegmentCollection(Collections.singletonList(segs[0])), RegionCutRule.PLUS_INSIDE);
            tree.insert(new TestLineSegmentCollection(Collections.singletonList(segs[1]))); // default rule
            tree.insert(new TestLineSegmentCollection(Collections.singletonList(segs[2])), RegionCutRule.PLUS_INSIDE);
            tree.insert(new TestLineSegmentCollection(Collections.singletonList(segs[3])), RegionCutRule.MINUS_INSIDE);
            tree.insert(new TestLineSegmentCollection(Collections.singletonList(segs[4])), RegionCutRule.INHERIT);
        });

    }

    @Test
    public void testInsert_hyperplaneConvexSubsets_mixedCutRules() {
        // act/assert
        checkMixedCutRuleInsertion(segs -> {
            tree.insert(segs[0], RegionCutRule.PLUS_INSIDE);
            tree.insert(segs[1]); // default rule
            tree.insert(segs[2], RegionCutRule.PLUS_INSIDE);
            tree.insert(segs[3], RegionCutRule.MINUS_INSIDE);
            tree.insert(segs[4], RegionCutRule.INHERIT);
        });
    }

    @Test
    public void testInsert_hyperplaneConvexSubsetList_mixedCutRules() {
        // act/assert
        checkMixedCutRuleInsertion(segs -> {
            tree.insert(Collections.singletonList(segs[0]), RegionCutRule.PLUS_INSIDE);
            tree.insert(Collections.singletonList(segs[1])); // default rule
            tree.insert(Collections.singletonList(segs[2]), RegionCutRule.PLUS_INSIDE);
            tree.insert(Collections.singletonList(segs[3]), RegionCutRule.MINUS_INSIDE);
            tree.insert(Collections.singletonList(segs[4]), RegionCutRule.INHERIT);
        });
    }

    @Test
    public void testInsert_boundarySource_mixedCutRules() {
        // arrange
        final Function<TestLineSegment, BoundarySource<TestLineSegment>> factory = seg -> () -> Stream.of(seg);

        // act/assert
        checkMixedCutRuleInsertion(segs -> {
            tree.insert(factory.apply(segs[0]), RegionCutRule.PLUS_INSIDE);
            tree.insert(factory.apply(segs[1])); // default rule
            tree.insert(factory.apply(segs[2]), RegionCutRule.PLUS_INSIDE);
            tree.insert(factory.apply(segs[3]), RegionCutRule.MINUS_INSIDE);
            tree.insert(factory.apply(segs[4]), RegionCutRule.INHERIT);
        });
    }

    /** Helper function to check the insertion of hyperplane subsets using different region cut rules.
     * @param fn
     */
    private void checkMixedCutRuleInsertion(final Consumer<TestLineSegment[]> fn) {
        // arrange
        final TestLineSegment bottom = new TestLineSegment(new TestPoint2D(1, 0), new TestPoint2D(0, 0));
        final TestLineSegment right = new TestLineSegment(new TestPoint2D(1, 0), new TestPoint2D(1, 1));
        final TestLineSegment top = new TestLineSegment(new TestPoint2D(0, 1), new TestPoint2D(1, 1));
        final TestLineSegment left = new TestLineSegment(new TestPoint2D(0, 1), new TestPoint2D(0, 0));
        final TestLineSegment diag = new TestLineSegment(new TestPoint2D(0, 0), new TestPoint2D(1, 1));

        tree = emptyTree();

        // act
        fn.accept(new TestLineSegment[] {
            bottom,
            right,
            top,
            left,
            diag
        });

        // assert
        TestRegionNode node = tree.getRoot();
        Assertions.assertEquals(RegionLocation.OUTSIDE, node.getLocation());

        Assertions.assertEquals(RegionLocation.OUTSIDE, node.getMinus().getLocation());
        Assertions.assertEquals(RegionLocation.INSIDE, node.getPlus().getLocation());

        node = node.getPlus();
        Assertions.assertEquals(RegionLocation.INSIDE, node.getMinus().getLocation());
        Assertions.assertEquals(RegionLocation.OUTSIDE, node.getPlus().getLocation());

        node = node.getMinus();
        Assertions.assertEquals(RegionLocation.OUTSIDE, node.getMinus().getLocation());
        Assertions.assertEquals(RegionLocation.INSIDE, node.getPlus().getLocation());

        node = node.getPlus();
        Assertions.assertEquals(RegionLocation.INSIDE, node.getMinus().getLocation());
        Assertions.assertEquals(RegionLocation.OUTSIDE, node.getPlus().getLocation());

        node = node.getMinus();
        Assertions.assertEquals(RegionLocation.INSIDE, node.getMinus().getLocation());
        Assertions.assertEquals(RegionLocation.INSIDE, node.getPlus().getLocation());
    }

    @Test
    public void testGetLocation_emptyRoot() {
        // act/assert
        Assertions.assertEquals(RegionLocation.INSIDE, root.getLocation());
    }

    @Test
    public void testGetLocation_singleCut() {
        // arrange
        root.insertCut(TestLine.X_AXIS);

        // act/assert
        Assertions.assertEquals(RegionLocation.INSIDE, root.getLocation());
        Assertions.assertFalse(root.isInside());
        Assertions.assertFalse(root.isOutside());

        final TestRegionNode minus = root.getMinus();
        Assertions.assertEquals(RegionLocation.INSIDE, minus.getLocation());
        Assertions.assertTrue(minus.isInside());
        Assertions.assertFalse(minus.isOutside());

        final TestRegionNode plus = root.getPlus();
        Assertions.assertEquals(RegionLocation.OUTSIDE, plus.getLocation());
        Assertions.assertFalse(plus.isInside());
        Assertions.assertTrue(plus.isOutside());
    }

    @Test
    public void testGetLocation_multipleCuts() {
        // arrange
        tree.insert(Arrays.asList(
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1)),
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, -1))));

        // act/assert
        Assertions.assertEquals(RegionLocation.INSIDE, root.getLocation());

        final TestRegionNode plus = root.getPlus();
        Assertions.assertEquals(RegionLocation.OUTSIDE, plus.getLocation());

        final TestRegionNode plusPlus = plus.getPlus();
        Assertions.assertEquals(RegionLocation.OUTSIDE, plusPlus.getLocation());

        final TestRegionNode plusMinus = plus.getMinus();
        Assertions.assertEquals(RegionLocation.INSIDE, plusMinus.getLocation());

        final TestRegionNode minus = root.getMinus();
        Assertions.assertEquals(RegionLocation.INSIDE, minus.getLocation());

        final TestRegionNode minusPlus = minus.getPlus();
        Assertions.assertEquals(RegionLocation.OUTSIDE, minusPlus.getLocation());

        final TestRegionNode minusMinus = minus.getMinus();
        Assertions.assertEquals(RegionLocation.INSIDE, minusMinus.getLocation());
    }

    @Test
    public void testSetLocation() {
        // arrange
        tree = emptyTree();
        tree.insert(TestLine.Y_AXIS.span());

        final TestRegionNode node = tree.getRoot().getMinus();

        // act
        node.setLocation(RegionLocation.OUTSIDE);

        // assert
        Assertions.assertEquals(RegionLocation.OUTSIDE, node.getLocation());
        Assertions.assertTrue(tree.isEmpty());
    }

    @Test
    public void testSetLocation_invalidatesRegionProperties() {
        // arrange
        tree = emptyTree();
        tree.insert(TestLine.Y_AXIS.span());

        final TestRegionNode node = tree.getRoot().getMinus();

        final RegionSizeProperties<TestPoint2D> prevProps = tree.getRegionSizeProperties();

        // act
        node.setLocation(RegionLocation.OUTSIDE);

        // assert
        Assertions.assertNotSame(prevProps, tree.getRegionSizeProperties());
    }

    @Test
    public void testSetLocation_noChange_doesNotInvalidateTree() {
        // arrange
        tree = emptyTree();
        tree.insert(TestLine.Y_AXIS.span());

        final TestRegionNode node = tree.getRoot().getMinus();

        final RegionSizeProperties<TestPoint2D> prevProps = tree.getRegionSizeProperties();

        // act
        node.setLocation(RegionLocation.INSIDE);

        // assert
        Assertions.assertSame(prevProps, tree.getRegionSizeProperties());
    }

    @Test
    public void testSetLocation_invalidArgs() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> root.setLocation(null),
                IllegalArgumentException.class, "Invalid node location: null");
        GeometryTestUtils.assertThrowsWithMessage(() -> root.setLocation(RegionLocation.BOUNDARY),
                IllegalArgumentException.class, "Invalid node location: BOUNDARY");
    }

    @Test
    public void testCondense() {
        // arrange
        tree = emptyTree();
        tree.insert(TestLine.Y_AXIS.span(), RegionCutRule.MINUS_INSIDE);
        tree.insert(TestLine.X_AXIS.span(), RegionCutRule.INHERIT);

        // act
        final boolean result = tree.condense();

        // assert
        Assertions.assertTrue(result);

        Assertions.assertEquals(3, tree.count());
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.getRoot().getLocation());
        Assertions.assertEquals(RegionLocation.INSIDE, tree.getRoot().getMinus().getLocation());
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.getRoot().getPlus().getLocation());
    }

    @Test
    public void testCondense_alreadyCondensed() {
        // arrange
        tree = emptyTree();
        tree.insert(TestLine.Y_AXIS.span(), RegionCutRule.MINUS_INSIDE);

        // act
        final boolean result = tree.condense();

        // assert
        Assertions.assertFalse(result);

        Assertions.assertEquals(3, tree.count());
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.getRoot().getLocation());
        Assertions.assertEquals(RegionLocation.INSIDE, tree.getRoot().getMinus().getLocation());
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.getRoot().getPlus().getLocation());
    }

    @Test
    public void testCondense_invalidatesTreeWhenChanged() {
        // arrange
        tree = emptyTree();
        tree.insert(TestLine.Y_AXIS.span(), RegionCutRule.MINUS_INSIDE);
        tree.insert(TestLine.X_AXIS.span(), RegionCutRule.INHERIT);

        final RegionSizeProperties<TestPoint2D> prevProps = tree.getRegionSizeProperties();

        // act
        final boolean result = tree.condense();

        // assert
        Assertions.assertTrue(result);

        Assertions.assertNotSame(prevProps, tree.getRegionSizeProperties());
    }

    @Test
    public void testCondense_doesNotInvalidateTreeWhenNotChanged() {
        // arrange
        tree = emptyTree();

        final RegionSizeProperties<TestPoint2D> prevProps = tree.getRegionSizeProperties();

        // act
        final boolean result = tree.condense();

        // assert
        Assertions.assertFalse(result);

        Assertions.assertSame(prevProps, tree.getRegionSizeProperties());
    }

    @Test
    public void testCut_nodeMethod() {
        // arrange
        tree = emptyTree();

        // act
        tree.getRoot().cut(TestLine.X_AXIS, RegionCutRule.PLUS_INSIDE)
            .getPlus()
                .cut(TestLine.Y_AXIS, RegionCutRule.MINUS_INSIDE)
                .getMinus()
                    .cut(new TestLine(TestPoint2D.ZERO, new TestPoint2D(-1, -1)), RegionCutRule.INHERIT);

        // assert
        TestRegionNode node = tree.getRoot();
        Assertions.assertEquals(RegionLocation.OUTSIDE, node.getLocation());

        Assertions.assertEquals(RegionLocation.OUTSIDE, node.getMinus().getLocation());
        Assertions.assertEquals(RegionLocation.INSIDE, node.getPlus().getLocation());

        node = node.getPlus();
        Assertions.assertEquals(RegionLocation.INSIDE, node.getMinus().getLocation());
        Assertions.assertEquals(RegionLocation.OUTSIDE, node.getPlus().getLocation());

        node = node.getMinus();
        Assertions.assertEquals(RegionLocation.INSIDE, node.getMinus().getLocation());
        Assertions.assertEquals(RegionLocation.INSIDE, node.getPlus().getLocation());
    }

    @Test
    public void testBoundaries_fullAndEmpty() {
        // act/assert
        tree.setFull();
        Assertions.assertFalse(tree.boundaries().iterator().hasNext());

        tree.setEmpty();
        Assertions.assertFalse(tree.boundaries().iterator().hasNext());
    }

    @Test
    public void testBoundaries_finite() {
        // arrange
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));

        // act
        final List<TestLineSegment> segments = new ArrayList<>();
        for (final HyperplaneConvexSubset<TestPoint2D> sub : tree.boundaries()) {
            segments.add((TestLineSegment) sub);
        }

        // assert
        Assertions.assertEquals(4, segments.size());

        assertContainsSegment(segments, new TestPoint2D(0, 0), new TestPoint2D(1, 0));
        assertContainsSegment(segments, new TestPoint2D(1, 0), new TestPoint2D(1, 1));
        assertContainsSegment(segments, new TestPoint2D(1, 1), new TestPoint2D(0, 1));
        assertContainsSegment(segments, new TestPoint2D(0, 1), new TestPoint2D(0, 0));
    }

    @Test
    public void testBoundaries_finite_inverted() {
        // arrange
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));
        tree.complement();

        // act
        final List<TestLineSegment> segments = new ArrayList<>();
        for (final HyperplaneConvexSubset<TestPoint2D> sub : tree.boundaries()) {
            segments.add((TestLineSegment) sub);
        }

        // assert
        Assertions.assertEquals(4, segments.size());

        assertContainsSegment(segments, new TestPoint2D(0, 0), new TestPoint2D(0, 1));
        assertContainsSegment(segments, new TestPoint2D(0, 1), new TestPoint2D(1, 1));
        assertContainsSegment(segments, new TestPoint2D(1, 1), new TestPoint2D(1, 0));
        assertContainsSegment(segments, new TestPoint2D(1, 0), new TestPoint2D(0, 0));
    }

    @Test
    public void testGetBoundaries_fullAndEmpty() {
        // act/assert
        tree.setFull();
        Assertions.assertEquals(0, tree.getBoundaries().size());

        tree.setEmpty();
        Assertions.assertEquals(0, tree.getBoundaries().size());
    }

    @Test
    public void testGetBoundaries_finite() {
        // arrange
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));

        // act
        final List<TestLineSegment> segments = new ArrayList<>();
        for (final HyperplaneConvexSubset<TestPoint2D> sub : tree.getBoundaries()) {
            segments.add((TestLineSegment) sub);
        }

        // assert
        Assertions.assertEquals(4, segments.size());

        assertContainsSegment(segments, new TestPoint2D(0, 0), new TestPoint2D(1, 0));
        assertContainsSegment(segments, new TestPoint2D(1, 0), new TestPoint2D(1, 1));
        assertContainsSegment(segments, new TestPoint2D(1, 1), new TestPoint2D(0, 1));
        assertContainsSegment(segments, new TestPoint2D(0, 1), new TestPoint2D(0, 0));
    }

    @Test
    public void testGetBoundaries_finite_inverted() {
        // arrange
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));
        tree.complement();

        // act
        final List<TestLineSegment> segments = new ArrayList<>();
        for (final HyperplaneConvexSubset<TestPoint2D> sub : tree.getBoundaries()) {
            segments.add((TestLineSegment) sub);
        }

        // assert
        Assertions.assertEquals(4, segments.size());

        assertContainsSegment(segments, new TestPoint2D(0, 0), new TestPoint2D(0, 1));
        assertContainsSegment(segments, new TestPoint2D(0, 1), new TestPoint2D(1, 1));
        assertContainsSegment(segments, new TestPoint2D(1, 1), new TestPoint2D(1, 0));
        assertContainsSegment(segments, new TestPoint2D(1, 0), new TestPoint2D(0, 0));
    }

    @Test
    public void testClassify() {
        // arrange
        insertSkewedBowtie(tree);

        // act/assert
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(3, 1)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-3, -1)));

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-3, 1)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(3, -1)));

        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 5)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, -5)));

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(5, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(3, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(2, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(1, 0)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(0, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-1, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-2, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-3, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, 0)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-5, 0)));
    }

    @Test
    public void testClassify_emptyTree() {
        // act/assert
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(TestPoint2D.ZERO));
    }

    @Test
    public void testClassify_NaN() {
        // act/assert
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(0, Double.NaN)));
    }

    @Test
    public void testContains() {
        // arrange
        insertSkewedBowtie(tree);

        // act/assert
        Assertions.assertTrue(tree.contains(new TestPoint2D(3, 1)));
        Assertions.assertTrue(tree.contains(new TestPoint2D(-3, -1)));

        Assertions.assertFalse(tree.contains(new TestPoint2D(-3, 1)));
        Assertions.assertFalse(tree.contains(new TestPoint2D(3, -1)));

        Assertions.assertTrue(tree.contains(new TestPoint2D(4, 5)));
        Assertions.assertTrue(tree.contains(new TestPoint2D(-4, -5)));

        Assertions.assertFalse(tree.contains(new TestPoint2D(5, 0)));

        Assertions.assertTrue(tree.contains(new TestPoint2D(4, 0)));
        Assertions.assertTrue(tree.contains(new TestPoint2D(3, 0)));
        Assertions.assertTrue(tree.contains(new TestPoint2D(2, 0)));
        Assertions.assertTrue(tree.contains(new TestPoint2D(1, 0)));
        Assertions.assertTrue(tree.contains(new TestPoint2D(0, 0)));
        Assertions.assertTrue(tree.contains(new TestPoint2D(-1, 0)));
        Assertions.assertTrue(tree.contains(new TestPoint2D(-2, 0)));
        Assertions.assertTrue(tree.contains(new TestPoint2D(-3, 0)));
        Assertions.assertTrue(tree.contains(new TestPoint2D(-4, 0)));

        Assertions.assertFalse(tree.contains(new TestPoint2D(-5, 0)));
    }

    @Test
    public void testSetFull() {
        // arrange
        insertSkewedBowtie(tree);

        // act
        tree.setFull();

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(TestPoint2D.ZERO));
        Assertions.assertTrue(tree.contains(TestPoint2D.ZERO));
    }

    @Test
    public void testSetEmpty() {
        // arrange
        insertSkewedBowtie(tree);

        // act
        tree.setEmpty();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(TestPoint2D.ZERO));
        Assertions.assertFalse(tree.contains(TestPoint2D.ZERO));
    }

    @Test
    public void testGetRegionSizeProperties_cachesValueBasedOnVersion() {
        // act
        final RegionSizeProperties<TestPoint2D> first = tree.getRegionSizeProperties();
        final RegionSizeProperties<TestPoint2D> second = tree.getRegionSizeProperties();
        tree.getRoot().cut(TestLine.X_AXIS);
        final RegionSizeProperties<TestPoint2D> third = tree.getRegionSizeProperties();

        // assert
        Assertions.assertSame(first, second);
        Assertions.assertNotSame(second, third);

        Assertions.assertEquals(1234, first.getSize(), PartitionTestUtils.EPS);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(12, 34), first.getCentroid());
    }

    @Test
    public void testGetSize() {
        // act/assert
        // make sure our stub value is pulled
        Assertions.assertEquals(1234, tree.getSize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetCentroid() {
        // act/assert
        // make sure our stub value is pulled
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(12, 34), tree.getCentroid());
    }

    @Test
    public void testGetBoundarySize_fullAndEmpty() {
        // act/assert
        Assertions.assertEquals(0.0, fullTree().getBoundarySize(), PartitionTestUtils.EPS);
        Assertions.assertEquals(0.0, emptyTree().getBoundarySize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetBoundarySize_infinite() {
        // arrange
        final TestRegionBSPTree halfPos = new TestRegionBSPTree(true);
        halfPos.getRoot().cut(TestLine.X_AXIS);

        final TestRegionBSPTree halfPosComplement = new TestRegionBSPTree(true);
        halfPosComplement.complement(halfPos);

        // act/assert
        Assertions.assertEquals(Double.POSITIVE_INFINITY, halfPos.getBoundarySize(), PartitionTestUtils.EPS);
        Assertions.assertEquals(Double.POSITIVE_INFINITY, halfPosComplement.getBoundarySize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetBoundarySize_alignedCuts() {
        // arrange
        final TestPoint2D p0 = TestPoint2D.ZERO;
        final TestPoint2D p1 = new TestPoint2D(0, 3);

        TestRegionNode node = tree.getRoot();

        tree.cutNode(node, new TestLineSegment(p0, p1));
        node = node.getMinus();

        tree.cutNode(node, new TestLineSegment(0, 0, new TestLine(p1, new TestPoint2D(-1, 3))));
        node = node.getMinus();

        tree.cutNode(node, new TestLineSegment(p1, p0));
        node = node.getMinus();

        tree.cutNode(node, new TestLineSegment(0, 0, new TestLine(p0, new TestPoint2D(1, 3))));

        // act/assert
        Assertions.assertEquals(6, tree.getBoundarySize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetBoundarySize_box() {
        // arrange
        insertBox(tree, new TestPoint2D(2, 2), new TestPoint2D(4, 1));

        // act/assert
        Assertions.assertEquals(6.0, tree.getBoundarySize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetBoundarySize_boxComplement() {
        // arrange
        insertBox(tree, new TestPoint2D(2, 2), new TestPoint2D(4, 1));
        tree.complement();

        // act/assert
        Assertions.assertEquals(6.0, tree.getBoundarySize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetBoundarySize_recomputesAfterChange() {
        // arrange
        insertBox(tree, new TestPoint2D(2, 2), new TestPoint2D(4, 1));

        // act
        final double first = tree.getBoundarySize();
        tree.insert(new TestLineSegment(new TestPoint2D(3, 1), new TestPoint2D(3, 2)));

        final double second = tree.getBoundarySize();
        final double third = tree.getBoundarySize();

        // assert
        Assertions.assertEquals(6.0, first, PartitionTestUtils.EPS);
        Assertions.assertEquals(4.0, second, PartitionTestUtils.EPS);
        Assertions.assertEquals(4.0, third, PartitionTestUtils.EPS);
    }

    @Test
    public void testGetCutBoundary_emptyTree() {
        // act
        final RegionCutBoundary<TestPoint2D> boundary = root.getCutBoundary();

        // assert
        Assertions.assertNull(boundary);
    }

    @Test
    public void testGetCutBoundary_singleCut() {
        // arrange
        tree.insert(new TestLineSegment(new TestPoint2D(0, 0), new TestPoint2D(1, 0)));

        // act
        final RegionCutBoundary<TestPoint2D> boundary = root.getCutBoundary();

        // assert
        Assertions.assertTrue(boundary.getInsideFacing().isEmpty());

        assertCutBoundarySegment(boundary.getOutsideFacing(),
                new TestPoint2D(Double.NEGATIVE_INFINITY, 0.0), new TestPoint2D(Double.POSITIVE_INFINITY, 0.0));
    }

    @Test
    public void testGetCutBoundary_singleCut_leafNode() {
        // arrange
        tree.insert(new TestLineSegment(new TestPoint2D(0, 0), new TestPoint2D(1, 0)));

        // act
        final RegionCutBoundary<TestPoint2D> boundary = root.getMinus().getCutBoundary();

        // assert
        Assertions.assertNull(boundary);
    }

    @Test
    public void testGetCutBoundary_singleCorner() {
        // arrange
        tree.insert(new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)));
        tree.insert(new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1)));

        // act/assert
        final RegionCutBoundary<TestPoint2D> rootBoundary = root.getCutBoundary();

        Assertions.assertTrue(rootBoundary.getInsideFacing().isEmpty());
        assertCutBoundarySegment(rootBoundary.getOutsideFacing(),
                new TestPoint2D(Double.NEGATIVE_INFINITY, 0.0), TestPoint2D.ZERO);

        final RegionCutBoundary<TestPoint2D> childBoundary = tree.getRoot().getMinus().getCutBoundary();
        Assertions.assertTrue(childBoundary.getInsideFacing().isEmpty());
        assertCutBoundarySegment(childBoundary.getOutsideFacing(),
                TestPoint2D.ZERO, new TestPoint2D(0.0, Double.POSITIVE_INFINITY));
    }

    @Test
    public void testGetCutBoundary_leafNode() {
        // arrange
        tree.insert(new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)));
        tree.insert(new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1)));

        // act/assert
        Assertions.assertNull(root.getPlus().getCutBoundary());
        Assertions.assertNull(root.getMinus().getMinus().getCutHyperplane());
        Assertions.assertNull(root.getMinus().getPlus().getCutHyperplane());
    }

    @Test
    public void testFullEmpty_fullTree() {
        // act/assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertEquals(RegionLocation.INSIDE, tree.getRoot().getLocation());
    }

    @Test
    public void testFullEmpty_emptyTree() {
        // arrange
        tree.complement();

        // act/assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.getRoot().getLocation());
    }

    @Test
    public void testTransform_noCuts() {
        // arrange
        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(p.getX(), p.getY() + 2));

        // act
        tree.transform(t);

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.INSIDE, TestPoint2D.ZERO);
    }

    @Test
    public void testTransform_singleCut() {
        // arrange
        tree.getRoot().insertCut(TestLine.X_AXIS);

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(p.getX(), p.getY() + 2));

        // act
        tree.transform(t);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(0, -1), TestPoint2D.ZERO, new TestPoint2D(0, 1));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.BOUNDARY, new TestPoint2D(0, 2));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.INSIDE,
                new TestPoint2D(0, 3), new TestPoint2D(0, 4));
    }

    @Test
    public void testTransform_multipleCuts() {
        // arrange
        insertSkewedBowtie(tree);

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(0.5 * p.getX(), p.getY() + 5));

        // act
        tree.transform(t);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.INSIDE,
                new TestPoint2D(0, 5), new TestPoint2D(-1, 4), new TestPoint2D(1, 6));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.BOUNDARY,
                new TestPoint2D(-2, 4), new TestPoint2D(2, 6));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(-3, 5), new TestPoint2D(3, 5));
    }

    @Test
    public void testTransform_xAxisReflection() {
        // arrange
        insertSkewedBowtie(tree);

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(-p.getX(), p.getY()));

        // act
        tree.transform(t);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.INSIDE,
                TestPoint2D.ZERO, new TestPoint2D(-1, 1), new TestPoint2D(1, -1));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.BOUNDARY,
                new TestPoint2D(0, 1), new TestPoint2D(0, -1),
                new TestPoint2D(-4, 0), new TestPoint2D(4, 0));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(1, 1), new TestPoint2D(-1, -1));
    }

    @Test
    public void testTransform_yAxisReflection() {
        // arrange
        insertSkewedBowtie(tree);

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(p.getX(), -p.getY()));

        // act
        tree.transform(t);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.INSIDE,
                TestPoint2D.ZERO, new TestPoint2D(1, -1), new TestPoint2D(-1, 1));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.BOUNDARY,
                new TestPoint2D(0, 1), new TestPoint2D(0, -1),
                new TestPoint2D(-4, 0), new TestPoint2D(4, 0));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(-1, -1), new TestPoint2D(1, 1));
    }

    @Test
    public void testTransform_xAndYAxisReflection() {
        // arrange
        insertSkewedBowtie(tree);

        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(-p.getX(), -p.getY()));

        // act
        tree.transform(t);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.INSIDE,
                TestPoint2D.ZERO, new TestPoint2D(1, 1), new TestPoint2D(-1, -1));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.BOUNDARY,
                new TestPoint2D(0, 1), new TestPoint2D(0, -1),
                new TestPoint2D(-4, 0), new TestPoint2D(4, 0));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(-1, 1), new TestPoint2D(1, -1));
    }

    @Test
    public void testTransform_resetsCutBoundary() {
        // arrange
        insertSkewedBowtie(tree);

        final TestRegionNode node = tree.findNode(new TestPoint2D(1, 1)).getParent();


        final Transform<TestPoint2D> t = new TestTransform2D(p -> new TestPoint2D(0.5 * p.getX(), p.getY() + 5));

        // act
        final RegionCutBoundary<TestPoint2D> origBoundary = node.getCutBoundary();

        tree.transform(t);

        final RegionCutBoundary<TestPoint2D> resultBoundary = node.getCutBoundary();

        // assert
        Assertions.assertNotSame(origBoundary, resultBoundary);

        assertCutBoundarySegment(origBoundary.getOutsideFacing(), new TestPoint2D(4, 5), new TestPoint2D(-1, 0));

        assertCutBoundarySegment(resultBoundary.getOutsideFacing(), new TestPoint2D(2, 10), new TestPoint2D(-0.5, 5));
    }

    @Test
    public void testComplement_rootOnly() {
        // act
        tree.complement();

        // assert
        Assertions.assertTrue(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(RegionLocation.OUTSIDE, root.getLocation());
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(TestPoint2D.ZERO));
    }

    @Test
    public void testComplement_singleCut() {
        // arrange
        root.insertCut(TestLine.X_AXIS);

        // act
        tree.complement();

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(RegionLocation.OUTSIDE, root.getMinus().getLocation());
        Assertions.assertEquals(RegionLocation.INSIDE, root.getPlus().getLocation());

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(0, 1)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(TestPoint2D.ZERO));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(0, -1)));
    }

    @Test
    public void testComplement_skewedBowtie() {
        // arrange
        insertSkewedBowtie(tree);

        // act
        tree.complement();

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(3, 1)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-3, -1)));

        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-3, 1)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(3, -1)));

        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 5)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, -5)));

        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(5, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(3, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(2, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(1, 0)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(0, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-1, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-2, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-3, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, 0)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-5, 0)));
    }

    @Test
    public void testComplement_addCutAfterComplement() {
        // arrange
        tree.insert(new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)));
        tree.complement();

        // act
        tree.insert(new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1)));

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(TestPoint2D.ZERO));

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(1, 1)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-1, 1)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(1, -1)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-1, -1)));
    }

    @Test
    public void testComplement_clearCutAfterComplement() {
        // arrange
        tree.insert(Arrays.asList(
                    new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                    new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1))
                ));
        tree.complement();

        // act
        root.getMinus().clearCut();

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(TestPoint2D.ZERO));

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(1, 1)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-1, 1)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(1, -1)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-1, -1)));
    }

    @Test
    public void testComplement_clearRootAfterComplement() {
        // arrange
        tree.insert(Arrays.asList(
                    new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                    new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1))
                ));
        tree.complement();

        // act
        root.clearCut();

        // assert
        Assertions.assertTrue(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(TestPoint2D.ZERO));
    }

    @Test
    public void testComplement_isReversible_root() {
        // act
        tree.complement();
        tree.complement();

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertTrue(tree.isFull());

        Assertions.assertEquals(RegionLocation.INSIDE, root.getLocation());
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(TestPoint2D.ZERO));
    }

    @Test
    public void testComplement_isReversible_skewedBowtie() {
        // arrange
        insertSkewedBowtie(tree);

        // act
        tree.complement();
        tree.complement();

        // assert
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(3, 1)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-3, -1)));

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-3, 1)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(3, -1)));

        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 5)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, -5)));

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(5, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(3, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(2, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(1, 0)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(0, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-1, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-2, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-3, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, 0)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-5, 0)));
    }

    @Test
    public void testComplement_getCutBoundary() {
        // arrange
        tree.insert(Arrays.asList(
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1))));
        tree.complement();

        // act
        final RegionCutBoundary<TestPoint2D> xAxisBoundary = root.getCutBoundary();
        final RegionCutBoundary<TestPoint2D> yAxisBoundary = root.getMinus().getCutBoundary();

        // assert
        Assertions.assertTrue(xAxisBoundary.getOutsideFacing().isEmpty());
        Assertions.assertFalse(xAxisBoundary.getInsideFacing().isEmpty());

        final List<HyperplaneConvexSubset<TestPoint2D>> xAxisInsideFacing = xAxisBoundary.getInsideFacing();
        Assertions.assertEquals(1, xAxisInsideFacing.size());

        final TestLineSegment xAxisSeg = (TestLineSegment) xAxisInsideFacing.get(0);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.NEGATIVE_INFINITY, 0), xAxisSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, xAxisSeg.getEndPoint());

        Assertions.assertTrue(yAxisBoundary.getOutsideFacing().isEmpty());
        Assertions.assertFalse(yAxisBoundary.getInsideFacing().isEmpty());

        final List<HyperplaneConvexSubset<TestPoint2D>> yAxisInsideFacing = yAxisBoundary.getInsideFacing();
        Assertions.assertEquals(1, yAxisInsideFacing.size());

        final TestLineSegment yAxisSeg = (TestLineSegment) yAxisInsideFacing.get(0);
        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, yAxisSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, Double.POSITIVE_INFINITY), yAxisSeg.getEndPoint());
    }

    @Test
    public void testComplementOf_rootOnly() {
        // arrange
        final TestRegionBSPTree other = fullTree();
        insertSkewedBowtie(other);

        // act
        other.complement(tree);

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertTrue(tree.isFull());

        Assertions.assertEquals(RegionLocation.INSIDE, root.getLocation());
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(TestPoint2D.ZERO));

        Assertions.assertTrue(other.isEmpty());
        Assertions.assertFalse(other.isFull());

        Assertions.assertEquals(RegionLocation.OUTSIDE, other.getRoot().getLocation());
        Assertions.assertEquals(RegionLocation.OUTSIDE, other.classify(TestPoint2D.ZERO));
    }

    @Test
    public void testComplementOf_skewedBowtie() {
        // arrange
        insertSkewedBowtie(tree);

        final TestRegionBSPTree other = fullTree();

        // act
        other.complement(tree);

        // assert
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(3, 1)));
        Assertions.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-3, -1)));

        Assertions.assertFalse(other.isEmpty());
        Assertions.assertFalse(other.isFull());

        Assertions.assertEquals(RegionLocation.OUTSIDE, other.classify(new TestPoint2D(3, 1)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, other.classify(new TestPoint2D(-3, -1)));

        Assertions.assertEquals(RegionLocation.INSIDE, other.classify(new TestPoint2D(-3, 1)));
        Assertions.assertEquals(RegionLocation.INSIDE, other.classify(new TestPoint2D(3, -1)));

        Assertions.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(4, 5)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(-4, -5)));

        Assertions.assertEquals(RegionLocation.INSIDE, other.classify(new TestPoint2D(5, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(4, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(3, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(2, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(1, 0)));
        Assertions.assertEquals(RegionLocation.OUTSIDE, other.classify(new TestPoint2D(0, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(-1, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(-2, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(-3, 0)));
        Assertions.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(-4, 0)));
        Assertions.assertEquals(RegionLocation.INSIDE, other.classify(new TestPoint2D(-5, 0)));
    }

    @Test
    public void testCopy() {
        // arrange
        insertSkewedBowtie(tree);

        // act
        final TestRegionBSPTree copy = fullTree();
        copy.copy(tree);

        // assert
        Assertions.assertNotSame(tree, copy);
        Assertions.assertEquals(tree.count(), copy.count());

        final List<RegionLocation> origLocations = new ArrayList<>();
        tree.nodes().forEach(n -> origLocations.add(n.getLocation()));

        final List<RegionLocation> copyLocations = new ArrayList<>();
        copy.nodes().forEach(n -> copyLocations.add(n.getLocation()));

        Assertions.assertEquals(origLocations, copyLocations);
    }

    @Test
    public void testExtract() {
        // arrange
        insertSkewedBowtie(tree);

        final TestRegionBSPTree result = fullTree();

        final TestPoint2D pt = new TestPoint2D(2, 2);

        // act
        result.extract(tree.findNode(pt));

        // assert
        PartitionTestUtils.assertPointLocations(result, RegionLocation.INSIDE,
                new TestPoint2D(0, 0.5), new TestPoint2D(2, 2));
        PartitionTestUtils.assertPointLocations(result, RegionLocation.OUTSIDE,
                new TestPoint2D(-2, 2),
                new TestPoint2D(-2, -2), new TestPoint2D(0, -0.5), new TestPoint2D(-2, 2));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.INSIDE,
                new TestPoint2D(0, 0.5), new TestPoint2D(2, 2),
                new TestPoint2D(-2, -2), new TestPoint2D(0, -0.5));
        PartitionTestUtils.assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(2, -2), new TestPoint2D(-2, 2));
    }

    @Test
    public void testExtract_complementedTree() {
        // arrange
        insertSkewedBowtie(tree);
        tree.complement();

        final TestRegionBSPTree result = fullTree();

        final TestPoint2D pt = new TestPoint2D(2, 2);

        // act
        result.extract(tree.findNode(pt));

        // assert
        PartitionTestUtils.assertPointLocations(result, RegionLocation.OUTSIDE,
                new TestPoint2D(0, 0.5), new TestPoint2D(2, 2));
        PartitionTestUtils.assertPointLocations(result, RegionLocation.INSIDE,
                new TestPoint2D(-2, 2),
                new TestPoint2D(-2, -2), new TestPoint2D(0, -0.5), new TestPoint2D(-2, 2));

        PartitionTestUtils.assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(0, 0.5), new TestPoint2D(2, 2),
                new TestPoint2D(-2, -2), new TestPoint2D(0, -0.5));
        PartitionTestUtils.assertPointLocations(tree, RegionLocation.INSIDE,
                new TestPoint2D(2, -2), new TestPoint2D(-2, 2));
    }



    @Test
    public void testProject_emptyAndFull() {
        // arrange
        final TestRegionBSPTree full = fullTree();
        final TestRegionBSPTree empty = emptyTree();

        // act/assert
        Assertions.assertNull(full.project(new TestPoint2D(0, 0)));
        Assertions.assertNull(empty.project(new TestPoint2D(-1, 1)));
    }

    @Test
    public void testProject_halfSpace() {
        // arrange
        tree.getRoot().cut(TestLine.X_AXIS);

        // act/assert
        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, tree.project(TestPoint2D.ZERO));

        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, tree.project(new TestPoint2D(0, 7)));
        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, tree.project(new TestPoint2D(0, -7)));

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(4, 0), tree.project(new TestPoint2D(4, 10)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-5, 0), tree.project(new TestPoint2D(-5, -2)));
    }

    @Test
    public void testProject_box() {
        // arrange
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));

        // act/assert
        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, tree.project(TestPoint2D.ZERO));
        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, tree.project(new TestPoint2D(-1, -4)));

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(1, 1), tree.project(new TestPoint2D(2, 9)));

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.5, 1), tree.project(new TestPoint2D(0.5, 3)));
    }

    @Test
    public void testSplit_empty() {
        // arrange
        tree = emptyTree();

        // act
        final Split<TestRegionBSPTree> split = tree.split(TestLine.X_AXIS);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_full() {
        // arrange
        tree = fullTree();

        // act
        final Split<TestRegionBSPTree> split = tree.split(TestLine.X_AXIS);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final TestRegionBSPTree minus = split.getMinus();
        PartitionTestUtils.assertPointLocations(minus, RegionLocation.INSIDE,
                new TestPoint2D(-1, 1), new TestPoint2D(0, 1), new TestPoint2D(1, 1));
        PartitionTestUtils.assertPointLocations(minus, RegionLocation.BOUNDARY,
                new TestPoint2D(-1, 0), new TestPoint2D(0, 0), new TestPoint2D(1, 0));
        PartitionTestUtils.assertPointLocations(minus, RegionLocation.OUTSIDE,
                new TestPoint2D(-1, -1), new TestPoint2D(0, -1), new TestPoint2D(1, -1));

        final TestRegionBSPTree plus = split.getPlus();
        PartitionTestUtils.assertPointLocations(plus, RegionLocation.OUTSIDE,
                new TestPoint2D(-1, 1), new TestPoint2D(0, 1), new TestPoint2D(1, 1));
        PartitionTestUtils.assertPointLocations(plus, RegionLocation.BOUNDARY,
                new TestPoint2D(-1, 0), new TestPoint2D(0, 0), new TestPoint2D(1, 0));
        PartitionTestUtils.assertPointLocations(plus, RegionLocation.INSIDE,
                new TestPoint2D(-1, -1), new TestPoint2D(0, -1), new TestPoint2D(1, -1));
    }

    @Test
    public void testSplit_halfSpace() {
        // arrange
        tree.getRoot().insertCut(TestLine.X_AXIS);

        final TestLine splitter = TestLine.Y_AXIS;

        // act
        final Split<TestRegionBSPTree> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final TestRegionBSPTree minus = split.getMinus();
        PartitionTestUtils.assertPointLocations(minus, RegionLocation.INSIDE, new TestPoint2D(-1, 1));
        PartitionTestUtils.assertPointLocations(minus, RegionLocation.OUTSIDE,
                new TestPoint2D(1, 1), new TestPoint2D(-1, -1), new TestPoint2D(1, -1));

        final TestRegionBSPTree plus = split.getPlus();
        PartitionTestUtils.assertPointLocations(plus, RegionLocation.INSIDE, new TestPoint2D(1, 1));
        PartitionTestUtils.assertPointLocations(plus, RegionLocation.OUTSIDE,
                new TestPoint2D(-1, 1), new TestPoint2D(-1, -1), new TestPoint2D(1, -1));
    }

    @Test
    public void testSplit_box() {
        // arrange
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));

        final TestLine splitter = new TestLine(new TestPoint2D(1, 0), new TestPoint2D(0, 1));

        // act
        final Split<TestRegionBSPTree> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final TestRegionBSPTree minus = split.getMinus();
        PartitionTestUtils.assertPointLocations(minus, RegionLocation.INSIDE, new TestPoint2D(0.25, 0.25));
        PartitionTestUtils.assertPointLocations(minus, RegionLocation.BOUNDARY,
                new TestPoint2D(0.5, 0), new TestPoint2D(0, 0.5));
        PartitionTestUtils.assertPointLocations(minus, RegionLocation.OUTSIDE,
                new TestPoint2D(1, 0.5), new TestPoint2D(0.5, 1), new TestPoint2D(0.75, 0.75));

        final TestRegionBSPTree plus = split.getPlus();
        PartitionTestUtils.assertPointLocations(plus, RegionLocation.INSIDE, new TestPoint2D(0.75, 0.75));
        PartitionTestUtils.assertPointLocations(plus, RegionLocation.OUTSIDE,
                new TestPoint2D(0.5, 0), new TestPoint2D(0, 0.5), new TestPoint2D(0.25, 0.25));
        PartitionTestUtils.assertPointLocations(plus, RegionLocation.BOUNDARY,
                new TestPoint2D(1, 0.5), new TestPoint2D(0.5, 1));
    }

    @Test
    public void testSplit_box_onMinusOnly() {
        // arrange
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));

        final TestLine splitter = new TestLine(new TestPoint2D(2, 0), new TestPoint2D(1, 1));

        // act
        final Split<TestRegionBSPTree> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        final TestRegionBSPTree minus = split.getMinus();
        PartitionTestUtils.assertPointLocations(minus, RegionLocation.INSIDE, new TestPoint2D(0.5, 0.5));
        PartitionTestUtils.assertPointLocations(minus, RegionLocation.BOUNDARY,
                new TestPoint2D(0.5, 0), new TestPoint2D(0, 0.5),
                new TestPoint2D(1, 0.5), new TestPoint2D(0.5, 1));

        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_box_onPlusOnly() {
        // arrange
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));

        final TestLine splitter = new TestLine(new TestPoint2D(0, 0), new TestPoint2D(-1, 1));

        // act
        final Split<TestRegionBSPTree> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());

        final TestRegionBSPTree plus = split.getPlus();
        PartitionTestUtils.assertPointLocations(plus, RegionLocation.INSIDE, new TestPoint2D(0.5, 0.5));
        PartitionTestUtils.assertPointLocations(plus, RegionLocation.BOUNDARY,
                new TestPoint2D(0.5, 0), new TestPoint2D(0, 0.5),
                new TestPoint2D(1, 0.5), new TestPoint2D(0.5, 1));
    }

    @Test
    public void testToString() {
        // arrange
        tree.getRoot().cut(TestLine.X_AXIS);

        // act
        final String str = tree.toString();

        // assert
        Assertions.assertEquals("TestRegionBSPTree[count= 3, height= 1]", str);
        Assertions.assertTrue(tree.getRoot().toString().contains("TestRegionNode"));
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

    private static void assertCutBoundarySegment(final List<HyperplaneConvexSubset<TestPoint2D>> boundaries,
            final TestPoint2D start, final TestPoint2D end) {
        Assertions.assertFalse(boundaries.isEmpty(), "Expected boundary to not be empty");

        Assertions.assertEquals(1, boundaries.size());

        final TestLineSegment segment = (TestLineSegment) boundaries.get(0);
        PartitionTestUtils.assertPointsEqual(start, segment.getStartPoint());
        PartitionTestUtils.assertPointsEqual(end, segment.getEndPoint());
    }

    private static void assertContainsSegment(final List<TestLineSegment> boundaries, final TestPoint2D start,
            final TestPoint2D end) {
        boolean found = false;
        for (final TestLineSegment seg : boundaries) {
            final TestPoint2D startPt = seg.getStartPoint();
            final TestPoint2D endPt = seg.getEndPoint();

            if (PartitionTestUtils.PRECISION.eq(start.getX(), startPt.getX()) &&
                    PartitionTestUtils.PRECISION.eq(start.getY(), startPt.getY()) &&
                    PartitionTestUtils.PRECISION.eq(end.getX(), endPt.getX()) &&
                    PartitionTestUtils.PRECISION.eq(end.getY(), endPt.getY())) {
                found = true;
                break;
            }
        }

        Assertions.assertTrue(found, "Expected to find segment start= " + start + ", end= " + end);
    }

    private static TestRegionBSPTree emptyTree() {
        return new TestRegionBSPTree(false);
    }

    private static TestRegionBSPTree fullTree() {
        return new TestRegionBSPTree(true);
    }
}
