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
package org.apache.commons.geometry.core.partition.bsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.ConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SplitLocation;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.core.partition.bsp.AbstractRegionBSPTree.AbstractRegionNode;
import org.apache.commons.geometry.core.partition.bsp.AbstractRegionBSPTree.RegionSizeProperties;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestLineSegment;
import org.apache.commons.geometry.core.partition.test.TestLineSegmentCollection;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractRegionBSPTreeTest {

    private TestRegionBSPTree tree;

    private TestRegionNode root;

    @Before
    public void setup() {
        tree = new TestRegionBSPTree();
        root = tree.getRoot();
    }

    @Test
    public void testDefaultConstructor() {
        // assert
        Assert.assertNotNull(root);
        Assert.assertNull(root.getParent());

        PartitionTestUtils.assertIsLeafNode(root);
        Assert.assertFalse(root.isPlus());
        Assert.assertFalse(root.isMinus());

        Assert.assertSame(tree, root.getTree());

        Assert.assertEquals(RegionLocation.INSIDE, root.getLocation());
    }

    @Test
    public void testParameterizedConstructor_true() {
        // act
        tree = new TestRegionBSPTree(true);
        root = tree.getRoot();

        // assert
        Assert.assertNotNull(root);
        Assert.assertNull(root.getParent());

        PartitionTestUtils.assertIsLeafNode(root);
        Assert.assertFalse(root.isPlus());
        Assert.assertFalse(root.isMinus());

        Assert.assertSame(tree, root.getTree());

        Assert.assertEquals(RegionLocation.INSIDE, root.getLocation());
    }

    @Test
    public void testParameterizedConstructor_false() {
        // act
        tree = new TestRegionBSPTree(false);
        root = tree.getRoot();

        // assert
        Assert.assertNotNull(root);
        Assert.assertNull(root.getParent());

        PartitionTestUtils.assertIsLeafNode(root);
        Assert.assertFalse(root.isPlus());
        Assert.assertFalse(root.isMinus());

        Assert.assertSame(tree, root.getTree());

        Assert.assertEquals(RegionLocation.OUTSIDE, root.getLocation());
    }

    @Test
    public void testGetLocation_emptyRoot() {
        // act/assert
        Assert.assertEquals(RegionLocation.INSIDE, root.getLocation());
    }

    @Test
    public void testGetLocation_singleCut() {
        // arrange
        root.insertCut(TestLine.X_AXIS);

        // act/assert
        Assert.assertNull(root.getLocation());
        Assert.assertEquals(RegionLocation.INSIDE, root.getMinus().getLocation());
        Assert.assertEquals(RegionLocation.OUTSIDE, root.getPlus().getLocation());
    }

    @Test
    public void testGetLocation_multipleCuts() {
        // arrange
        tree.insert(Arrays.asList(
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1)),
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, -1))));

        // act/assert
        Assert.assertNull(root.getLocation());

        TestRegionNode plus = root.getPlus();
        Assert.assertNull(plus.getLocation());

        TestRegionNode plusPlus = plus.getPlus();
        Assert.assertEquals(RegionLocation.OUTSIDE, plusPlus.getLocation());

        TestRegionNode plusMinus = plus.getMinus();
        Assert.assertEquals(RegionLocation.INSIDE, plusMinus.getLocation());

        TestRegionNode minus = root.getMinus();
        Assert.assertNull(minus.getLocation());

        TestRegionNode minusPlus = minus.getPlus();
        Assert.assertEquals(RegionLocation.OUTSIDE, minusPlus.getLocation());

        TestRegionNode minusMinus = minus.getMinus();
        Assert.assertEquals(RegionLocation.INSIDE, minusMinus.getLocation());
    }

    @Test
    public void testGetLocation_resetsLocationWhenNodeCleared() {
        // arrange
        tree.insert(Arrays.asList(
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1)),
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, -1))));

        // act
        root.getPlus().clearCut();
        root.getMinus().clearCut();

        // assert
        Assert.assertNull(root.getLocation());

        Assert.assertEquals(RegionLocation.INSIDE, root.getMinus().getLocation());
        Assert.assertEquals(RegionLocation.OUTSIDE, root.getPlus().getLocation());
    }

    @Test
    public void testGetLocation_resetRoot() {
        // arrange
        tree.insert(Arrays.asList(
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1)),
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, -1))));

        TestRegionNode root = tree.getRoot();

        // act
        root.clearCut();

        // assert
        Assert.assertEquals(RegionLocation.INSIDE, root.getLocation());
    }

    @Test
    public void testBoundaries_fullAndEmpty() {
        // act/assert
        tree.setFull();
        Assert.assertFalse(tree.boundaries().iterator().hasNext());

        tree.setEmpty();
        Assert.assertFalse(tree.boundaries().iterator().hasNext());
    }

    @Test
    public void testBoundaries_finite() {
        // arrange
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));

        // act
        List<TestLineSegment> segments = new ArrayList<>();
        for (ConvexSubHyperplane<TestPoint2D> sub : tree.boundaries()) {
            segments.add((TestLineSegment) sub);
        }

        // assert
        Assert.assertEquals(4, segments.size());

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
        List<TestLineSegment> segments = new ArrayList<>();
        for (ConvexSubHyperplane<TestPoint2D> sub : tree.boundaries()) {
            segments.add((TestLineSegment) sub);
        }

        // assert
        Assert.assertEquals(4, segments.size());

        assertContainsSegment(segments, new TestPoint2D(0, 0), new TestPoint2D(0, 1));
        assertContainsSegment(segments, new TestPoint2D(0, 1), new TestPoint2D(1, 1));
        assertContainsSegment(segments, new TestPoint2D(1, 1), new TestPoint2D(1, 0));
        assertContainsSegment(segments, new TestPoint2D(1, 0), new TestPoint2D(0, 0));
    }

    @Test
    public void testGetBoundaries_fullAndEmpty() {
        // act/assert
        tree.setFull();
        Assert.assertEquals(0, tree.getBoundaries().size());

        tree.setEmpty();
        Assert.assertEquals(0, tree.getBoundaries().size());
    }

    @Test
    public void testGetBoundaries_finite() {
        // arrange
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));

        // act
        List<TestLineSegment> segments = new ArrayList<>();
        for (ConvexSubHyperplane<TestPoint2D> sub : tree.getBoundaries()) {
            segments.add((TestLineSegment) sub);
        }

        // assert
        Assert.assertEquals(4, segments.size());

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
        List<TestLineSegment> segments = new ArrayList<>();
        for (ConvexSubHyperplane<TestPoint2D> sub : tree.getBoundaries()) {
            segments.add((TestLineSegment) sub);
        }

        // assert
        Assert.assertEquals(4, segments.size());

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
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(3, 1)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-3, -1)));

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-3, 1)));
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(3, -1)));

        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 5)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, -5)));

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(5, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(3, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(2, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(1, 0)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(0, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-1, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-2, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-3, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, 0)));
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-5, 0)));
    }

    @Test
    public void testClassify_emptyTree() {
        // act/assert
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(TestPoint2D.ZERO));
    }

    @Test
    public void testClassify_NaN() {
        // act/assert
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(0, Double.NaN)));
    }

    @Test
    public void testContains() {
        // arrange
        insertSkewedBowtie(tree);

        // act/assert
        Assert.assertTrue(tree.contains(new TestPoint2D(3, 1)));
        Assert.assertTrue(tree.contains(new TestPoint2D(-3, -1)));

        Assert.assertFalse(tree.contains(new TestPoint2D(-3, 1)));
        Assert.assertFalse(tree.contains(new TestPoint2D(3, -1)));

        Assert.assertTrue(tree.contains(new TestPoint2D(4, 5)));
        Assert.assertTrue(tree.contains(new TestPoint2D(-4, -5)));

        Assert.assertFalse(tree.contains(new TestPoint2D(5, 0)));

        Assert.assertTrue(tree.contains(new TestPoint2D(4, 0)));
        Assert.assertTrue(tree.contains(new TestPoint2D(3, 0)));
        Assert.assertTrue(tree.contains(new TestPoint2D(2, 0)));
        Assert.assertTrue(tree.contains(new TestPoint2D(1, 0)));
        Assert.assertTrue(tree.contains(new TestPoint2D(0, 0)));
        Assert.assertTrue(tree.contains(new TestPoint2D(-1, 0)));
        Assert.assertTrue(tree.contains(new TestPoint2D(-2, 0)));
        Assert.assertTrue(tree.contains(new TestPoint2D(-3, 0)));
        Assert.assertTrue(tree.contains(new TestPoint2D(-4, 0)));

        Assert.assertFalse(tree.contains(new TestPoint2D(-5, 0)));
    }

    @Test
    public void testSetFull() {
        // arrange
        insertSkewedBowtie(tree);

        // act
        tree.setFull();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(TestPoint2D.ZERO));
        Assert.assertTrue(tree.contains(TestPoint2D.ZERO));
    }

    @Test
    public void testSetEmpty() {
        // arrange
        insertSkewedBowtie(tree);

        // act
        tree.setEmpty();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(TestPoint2D.ZERO));
        Assert.assertFalse(tree.contains(TestPoint2D.ZERO));
    }

    @Test
    public void testGetRegionSizeProperties_cachesValueBasedOnVersion() {
        // act
        RegionSizeProperties<TestPoint2D> first = tree.getRegionSizeProperties();
        RegionSizeProperties<TestPoint2D> second = tree.getRegionSizeProperties();
        tree.getRoot().cut(TestLine.X_AXIS);
        RegionSizeProperties<TestPoint2D> third = tree.getRegionSizeProperties();

        // assert
        Assert.assertSame(first, second);
        Assert.assertNotSame(second, third);

        Assert.assertEquals(1, first.getSize(), PartitionTestUtils.EPS);
        Assert.assertSame(TestPoint2D.ZERO, first.getBarycenter());
    }

    @Test
    public void testGetSize() {
        // act/assert
        // make sure our stub value is pulled
        Assert.assertEquals(1, tree.getSize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetBarycenter() {
        // act/assert
        // make sure our stub value is pulled
        Assert.assertSame(TestPoint2D.ZERO, tree.getBarycenter());
    }

    @Test
    public void testGetBoundarySize_fullAndEmpty() {
        // act/assert
        Assert.assertEquals(0.0, fullTree().getBoundarySize(), PartitionTestUtils.EPS);
        Assert.assertEquals(0.0, emptyTree().getBoundarySize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetBoundarySize_infinite() {
        // arrange
        TestRegionBSPTree halfPos = new TestRegionBSPTree(true);
        halfPos.getRoot().cut(TestLine.X_AXIS);

        TestRegionBSPTree halfPosComplement = new TestRegionBSPTree(true);
        halfPosComplement.complement(halfPos);

        // act/assert
        Assert.assertEquals(Double.POSITIVE_INFINITY, halfPos.getBoundarySize(), PartitionTestUtils.EPS);
        Assert.assertEquals(Double.POSITIVE_INFINITY, halfPosComplement.getBoundarySize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetBoundarySize_alignedCuts() {
        // arrange
        TestPoint2D p0 = TestPoint2D.ZERO;
        TestPoint2D p1 = new TestPoint2D(0, 3);

        TestRegionNode node = tree.getRoot();

        tree.cutNode(node, new TestLineSegment(p0, p1));
        node = node.getMinus();

        tree.cutNode(node, new TestLineSegment(0, 0, new TestLine(p1, new TestPoint2D(-1, 3))));
        node = node.getMinus();

        tree.cutNode(node, new TestLineSegment(p1, p0));
        node = node.getMinus();

        tree.cutNode(node, new TestLineSegment(0, 0, new TestLine(p0, new TestPoint2D(1, 3))));

        // act/assert
        Assert.assertEquals(6, tree.getBoundarySize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetBoundarySize_box() {
        // arrange
        insertBox(tree, new TestPoint2D(2, 2), new TestPoint2D(4, 1));

        // act/assert
        Assert.assertEquals(6.0, tree.getBoundarySize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetBoundarySize_boxComplement() {
        // arrange
        insertBox(tree, new TestPoint2D(2, 2), new TestPoint2D(4, 1));
        tree.complement();

        // act/assert
        Assert.assertEquals(6.0, tree.getBoundarySize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testGetBoundarySize_recomputesAfterChange() {
        // arrange
        insertBox(tree, new TestPoint2D(2, 2), new TestPoint2D(4, 1));

        // act
        double first = tree.getBoundarySize();
        tree.insert(new TestLineSegment(new TestPoint2D(3, 1), new TestPoint2D(3, 2)));

        double second = tree.getBoundarySize();
        double third = tree.getBoundarySize();

        // assert
        Assert.assertEquals(6.0, first, PartitionTestUtils.EPS);
        Assert.assertEquals(4.0, second, PartitionTestUtils.EPS);
        Assert.assertEquals(4.0, third, PartitionTestUtils.EPS);
    }

    @Test
    public void testGetCutBoundary_emptyTree() {
        // act
        RegionCutBoundary<TestPoint2D> boundary = root.getCutBoundary();

        // assert
        Assert.assertNull(boundary);
    }

    @Test
    public void tetsGetCutBoundary_singleCut() {
        // arrange
        tree.insert(new TestLineSegment(new TestPoint2D(0, 0), new TestPoint2D(1, 0)));

        // act
        RegionCutBoundary<TestPoint2D> boundary = root.getCutBoundary();

        // assert
        Assert.assertTrue(boundary.getInsideFacing().isEmpty());

        assertCutBoundarySegment(boundary.getOutsideFacing(),
                new TestPoint2D(Double.NEGATIVE_INFINITY, 0.0), new TestPoint2D(Double.POSITIVE_INFINITY, 0.0));
    }

    @Test
    public void tetsGetCutBoundary_singleCut_leafNode() {
        // arrange
        tree.insert(new TestLineSegment(new TestPoint2D(0, 0), new TestPoint2D(1, 0)));

        // act
        RegionCutBoundary<TestPoint2D> boundary = root.getMinus().getCutBoundary();

        // assert
        Assert.assertNull(boundary);
    }

    @Test
    public void tetsGetCutBoundary_singleCorner() {
        // arrange
        tree.insert(new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)));
        tree.insert(new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1)));

        // act/assert
        RegionCutBoundary<TestPoint2D> rootBoundary = root.getCutBoundary();

        Assert.assertTrue(rootBoundary.getInsideFacing().isEmpty());
        assertCutBoundarySegment(rootBoundary.getOutsideFacing(),
                new TestPoint2D(Double.NEGATIVE_INFINITY, 0.0), TestPoint2D.ZERO);

        RegionCutBoundary<TestPoint2D> childBoundary = tree.getRoot().getMinus().getCutBoundary();
        Assert.assertTrue(childBoundary.getInsideFacing().isEmpty());
        assertCutBoundarySegment(childBoundary.getOutsideFacing(),
                TestPoint2D.ZERO, new TestPoint2D(0.0, Double.POSITIVE_INFINITY));
    }

    @Test
    public void tetsGetCutBoundary_leafNode() {
        // arrange
        tree.insert(new TestLineSegment(new TestPoint2D(-1, 0), new TestPoint2D(1, 0)));
        tree.insert(new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1)));

        // act/assert
        Assert.assertNull(root.getPlus().getCutBoundary());
        Assert.assertNull(root.getMinus().getMinus().getCutHyperplane());
        Assert.assertNull(root.getMinus().getPlus().getCutHyperplane());
    }

    @Test
    public void testFullEmpty_fullTree() {
        // act/assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
        Assert.assertEquals(RegionLocation.INSIDE, tree.getRoot().getLocation());
    }

    @Test
    public void testFullEmpty_emptyTree() {
        // arrange
        tree.complement();

        // act/assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.getRoot().getLocation());
    }

    @Test
    public void testTransform_noCuts() {
        // arrange
        Transform<TestPoint2D> t = p -> new TestPoint2D(p.getX(), p.getY() + 2);

        // act
        tree.transform(t);

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        assertPointLocations(tree, RegionLocation.INSIDE, TestPoint2D.ZERO);
    }

    @Test
    public void testTransform_singleCut() {
        // arrange
        tree.getRoot().insertCut(TestLine.X_AXIS);

        Transform<TestPoint2D> t = p -> new TestPoint2D(p.getX(), p.getY() + 2);

        // act
        tree.transform(t);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(0, -1), TestPoint2D.ZERO, new TestPoint2D(0, 1));

        assertPointLocations(tree, RegionLocation.BOUNDARY, new TestPoint2D(0, 2));

        assertPointLocations(tree, RegionLocation.INSIDE,
                new TestPoint2D(0, 3), new TestPoint2D(0, 4));
    }

    @Test
    public void testTransform_multipleCuts() {
        // arrange
        insertSkewedBowtie(tree);

        Transform<TestPoint2D> t = p -> new TestPoint2D(0.5 * p.getX(), p.getY() + 5);

        // act
        tree.transform(t);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        assertPointLocations(tree, RegionLocation.INSIDE,
                new TestPoint2D(0, 5), new TestPoint2D(-1, 4), new TestPoint2D(1, 6));

        assertPointLocations(tree, RegionLocation.BOUNDARY,
                new TestPoint2D(-2, 4), new TestPoint2D(2, 6));

        assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(-3, 5), new TestPoint2D(3, 5));
    }

    @Test
    public void testTransform_xAxisReflection() {
        // arrange
        insertSkewedBowtie(tree);

        Transform<TestPoint2D> t = p -> new TestPoint2D(-p.getX(), p.getY());

        // act
        tree.transform(t);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        assertPointLocations(tree, RegionLocation.INSIDE,
                TestPoint2D.ZERO, new TestPoint2D(-1, 1), new TestPoint2D(1, -1));

        assertPointLocations(tree, RegionLocation.BOUNDARY,
                new TestPoint2D(0, 1), new TestPoint2D(0, -1),
                new TestPoint2D(-4, 0), new TestPoint2D(4, 0));

        assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(1, 1), new TestPoint2D(-1, -1));
    }

    @Test
    public void testTransform_yAxisReflection() {
        // arrange
        insertSkewedBowtie(tree);

        Transform<TestPoint2D> t = p -> new TestPoint2D(p.getX(), -p.getY());

        // act
        tree.transform(t);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        assertPointLocations(tree, RegionLocation.INSIDE,
                TestPoint2D.ZERO, new TestPoint2D(1, -1), new TestPoint2D(-1, 1));

        assertPointLocations(tree, RegionLocation.BOUNDARY,
                new TestPoint2D(0, 1), new TestPoint2D(0, -1),
                new TestPoint2D(-4, 0), new TestPoint2D(4, 0));

        assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(-1, -1), new TestPoint2D(1, 1));
    }

    @Test
    public void testTransform_xAndYAxisReflection() {
        // arrange
        insertSkewedBowtie(tree);

        Transform<TestPoint2D> t = p -> new TestPoint2D(-p.getX(), -p.getY());

        // act
        tree.transform(t);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        assertPointLocations(tree, RegionLocation.INSIDE,
                TestPoint2D.ZERO, new TestPoint2D(1, 1), new TestPoint2D(-1, -1));

        assertPointLocations(tree, RegionLocation.BOUNDARY,
                new TestPoint2D(0, 1), new TestPoint2D(0, -1),
                new TestPoint2D(-4, 0), new TestPoint2D(4, 0));

        assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(-1, 1), new TestPoint2D(1, -1));
    }

    @Test
    public void testTransform_resetsCutBoundary() {
        // arrange
        insertSkewedBowtie(tree);

        TestRegionNode node = tree.findNode(new TestPoint2D(1, 1)).getParent();


        Transform<TestPoint2D> t = p -> new TestPoint2D(0.5 * p.getX(), p.getY() + 5);

        // act
        RegionCutBoundary<TestPoint2D> origBoundary = node.getCutBoundary();

        tree.transform(t);

        RegionCutBoundary<TestPoint2D> resultBoundary = node.getCutBoundary();

        // assert
        Assert.assertNotSame(origBoundary, resultBoundary);

        assertCutBoundarySegment(origBoundary.getOutsideFacing(), new TestPoint2D(4, 5), new TestPoint2D(-1, 0));

        assertCutBoundarySegment(resultBoundary.getOutsideFacing(), new TestPoint2D(2, 10), new TestPoint2D(-0.5, 5));
    }

    @Test
    public void testComplement_rootOnly() {
        // act
        tree.complement();

        // assert
        Assert.assertTrue(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(RegionLocation.OUTSIDE, root.getLocation());
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(TestPoint2D.ZERO));
    }

    @Test
    public void testComplement_singleCut() {
        // arrange
        root.insertCut(TestLine.X_AXIS);

        // act
        tree.complement();

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(RegionLocation.OUTSIDE, root.getMinus().getLocation());
        Assert.assertEquals(RegionLocation.INSIDE, root.getPlus().getLocation());

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(0, 1)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(TestPoint2D.ZERO));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(0, -1)));
    }

    @Test
    public void testComplement_skewedBowtie() {
        // arrange
        insertSkewedBowtie(tree);

        // act
        tree.complement();

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(3, 1)));
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-3, -1)));

        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-3, 1)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(3, -1)));

        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 5)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, -5)));

        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(5, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(3, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(2, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(1, 0)));
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(0, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-1, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-2, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-3, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, 0)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-5, 0)));
    }

    @Test
    public void testComplement_addCutAfterComplement() {
        // arrange
        tree.insert(new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)));
        tree.complement();

        // act
        tree.insert(new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1)));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(TestPoint2D.ZERO));

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(1, 1)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-1, 1)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(1, -1)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-1, -1)));
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
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(TestPoint2D.ZERO));

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(1, 1)));
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-1, 1)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(1, -1)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-1, -1)));
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
        Assert.assertTrue(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(TestPoint2D.ZERO));
    }

    @Test
    public void testComplement_isReversible_root() {
        // act
        tree.complement();
        tree.complement();

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertTrue(tree.isFull());

        Assert.assertEquals(RegionLocation.INSIDE, root.getLocation());
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(TestPoint2D.ZERO));
    }

    @Test
    public void testComplement_isReversible_skewedBowtie() {
        // arrange
        insertSkewedBowtie(tree);

        // act
        tree.complement();
        tree.complement();

        // assert
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(3, 1)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-3, -1)));

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-3, 1)));
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(3, -1)));

        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 5)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, -5)));

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(5, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(4, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(3, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(2, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(1, 0)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(0, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-1, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-2, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-3, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, tree.classify(new TestPoint2D(-4, 0)));
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(new TestPoint2D(-5, 0)));
    }

    @Test
    public void testComplement_getCutBoundary() {
        // arrange
        tree.insert(Arrays.asList(
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)),
                new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(0, 1))));
        tree.complement();

        // act
        RegionCutBoundary<TestPoint2D> xAxisBoundary = root.getCutBoundary();
        RegionCutBoundary<TestPoint2D> yAxisBoundary = root.getMinus().getCutBoundary();

        // assert
        Assert.assertTrue(xAxisBoundary.getOutsideFacing().isEmpty());
        Assert.assertFalse(xAxisBoundary.getInsideFacing().isEmpty());

        TestLineSegmentCollection xAxisInsideFacing = (TestLineSegmentCollection) xAxisBoundary.getInsideFacing();
        Assert.assertEquals(1, xAxisInsideFacing.getLineSegments().size());

        TestLineSegment xAxisSeg = xAxisInsideFacing.getLineSegments().get(0);
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(Double.NEGATIVE_INFINITY, 0), xAxisSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, xAxisSeg.getEndPoint());

        Assert.assertTrue(yAxisBoundary.getOutsideFacing().isEmpty());
        Assert.assertFalse(yAxisBoundary.getInsideFacing().isEmpty());

        TestLineSegmentCollection yAxisInsideFacing = (TestLineSegmentCollection) yAxisBoundary.getInsideFacing();
        Assert.assertEquals(1, yAxisInsideFacing.getLineSegments().size());

        TestLineSegment yAxisSeg = yAxisInsideFacing.getLineSegments().get(0);
        PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, yAxisSeg.getStartPoint());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, Double.POSITIVE_INFINITY), yAxisSeg.getEndPoint());
    }

    @Test
    public void testComplementOf_rootOnly() {
        // arrange
        TestRegionBSPTree other = fullTree();
        insertSkewedBowtie(other);

        // act
        other.complement(tree);

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertTrue(tree.isFull());

        Assert.assertEquals(RegionLocation.INSIDE, root.getLocation());
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(TestPoint2D.ZERO));

        Assert.assertTrue(other.isEmpty());
        Assert.assertFalse(other.isFull());

        Assert.assertEquals(RegionLocation.OUTSIDE, other.getRoot().getLocation());
        Assert.assertEquals(RegionLocation.OUTSIDE, other.classify(TestPoint2D.ZERO));
    }

    @Test
    public void testComplementOf_skewedBowtie() {
        // arrange
        insertSkewedBowtie(tree);

        TestRegionBSPTree other = fullTree();

        // act
        other.complement(tree);

        // assert
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(3, 1)));
        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(new TestPoint2D(-3, -1)));

        Assert.assertFalse(other.isEmpty());
        Assert.assertFalse(other.isFull());

        Assert.assertEquals(RegionLocation.OUTSIDE, other.classify(new TestPoint2D(3, 1)));
        Assert.assertEquals(RegionLocation.OUTSIDE, other.classify(new TestPoint2D(-3, -1)));

        Assert.assertEquals(RegionLocation.INSIDE, other.classify(new TestPoint2D(-3, 1)));
        Assert.assertEquals(RegionLocation.INSIDE, other.classify(new TestPoint2D(3, -1)));

        Assert.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(4, 5)));
        Assert.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(-4, -5)));

        Assert.assertEquals(RegionLocation.INSIDE, other.classify(new TestPoint2D(5, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(4, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(3, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(2, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(1, 0)));
        Assert.assertEquals(RegionLocation.OUTSIDE, other.classify(new TestPoint2D(0, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(-1, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(-2, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(-3, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, other.classify(new TestPoint2D(-4, 0)));
        Assert.assertEquals(RegionLocation.INSIDE, other.classify(new TestPoint2D(-5, 0)));
    }

    @Test
    public void testCopy() {
        // arrange
        insertSkewedBowtie(tree);

        // act
        TestRegionBSPTree copy = fullTree();
        copy.copy(tree);

        // assert
        Assert.assertNotSame(tree, copy);
        Assert.assertEquals(tree.count(), copy.count());

        List<RegionLocation> origLocations = new ArrayList<>();
        tree.forEach(n -> origLocations.add(n.getLocationValue()));

        List<RegionLocation> copyLocations = new ArrayList<>();
        copy.forEach(n -> copyLocations.add(n.getLocationValue()));

        Assert.assertEquals(origLocations, copyLocations);
    }

    @Test
    public void testExtract() {
        // arrange
        insertSkewedBowtie(tree);

        TestRegionBSPTree result = fullTree();

        TestPoint2D pt = new TestPoint2D(2, 2);

        // act
        result.extract(tree.findNode(pt));

        // assert
        assertPointLocations(result, RegionLocation.INSIDE,
                new TestPoint2D(0, 0.5), new TestPoint2D(2, 2));
        assertPointLocations(result, RegionLocation.OUTSIDE,
                new TestPoint2D(-2, 2),
                new TestPoint2D(-2, -2), new TestPoint2D(0, -0.5), new TestPoint2D(-2, 2));

        assertPointLocations(tree, RegionLocation.INSIDE,
                new TestPoint2D(0, 0.5), new TestPoint2D(2, 2),
                new TestPoint2D(-2, -2), new TestPoint2D(0, -0.5));
        assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(2, -2), new TestPoint2D(-2, 2));
    }

    @Test
    public void testExtract_complementedTree() {
        // arrange
        insertSkewedBowtie(tree);
        tree.complement();

        TestRegionBSPTree result = fullTree();

        TestPoint2D pt = new TestPoint2D(2, 2);

        // act
        result.extract(tree.findNode(pt));

        // assert
        assertPointLocations(result, RegionLocation.OUTSIDE,
                new TestPoint2D(0, 0.5), new TestPoint2D(2, 2));
        assertPointLocations(result, RegionLocation.INSIDE,
                new TestPoint2D(-2, 2),
                new TestPoint2D(-2, -2), new TestPoint2D(0, -0.5), new TestPoint2D(-2, 2));

        assertPointLocations(tree, RegionLocation.OUTSIDE,
                new TestPoint2D(0, 0.5), new TestPoint2D(2, 2),
                new TestPoint2D(-2, -2), new TestPoint2D(0, -0.5));
        assertPointLocations(tree, RegionLocation.INSIDE,
                new TestPoint2D(2, -2), new TestPoint2D(-2, 2));
    }


    @Test
    public void testUnion_singleNodeTrees() {
        // act/assert
        unionChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        unionChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();
    }

    @Test
    public void testUnion_simpleCrossingCuts() {
        // act/assert
        unionChecker(AbstractRegionBSPTreeTest::xAxisTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(false)
            .count(3)
            .check();

        unionChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::xAxisTree)
            .full(false)
            .empty(false)
            .count(3)
            .check();

        unionChecker(AbstractRegionBSPTreeTest::yAxisTree, AbstractRegionBSPTreeTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::yAxisTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(AbstractRegionBSPTreeTest::xAxisTree, AbstractRegionBSPTreeTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .outside(new TestPoint2D(1, -1))
            .boundary(TestPoint2D.ZERO)
            .count(5)
            .check(tree -> {
                TestLineSegment seg = (TestLineSegment) tree.getRoot().getPlus().getCut();

                PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.0, Double.NEGATIVE_INFINITY), seg.getStartPoint());
                PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, seg.getEndPoint());
            });
    }

    @Test
    public void testUnion_boxTreeWithSingleCutTree() {
        // arrange
        Supplier<TestRegionBSPTree> boxFactory = () -> {
            TestRegionBSPTree box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        Supplier<TestRegionBSPTree> horizonalFactory = () -> {
            TestRegionBSPTree horizontal = fullTree();
            horizontal.getRoot().insertCut(new TestLine(new TestPoint2D(2, 2), new TestPoint2D(0, 2)));

            return horizontal;
        };

        // act/assert
        unionChecker(horizonalFactory, boxFactory)
            .count(3)
            .inside(TestPoint2D.ZERO, new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(0, 3), new TestPoint2D(3, 3))
            .boundary(new TestPoint2D(-1, 2), new TestPoint2D(3, 2))
            .check();
    }

    @Test
    public void testUnion_treeWithComplement() {
        // arrange
        Supplier<TestRegionBSPTree> treeFactory = () -> {
            TestRegionBSPTree tree = fullTree();
            insertSkewedBowtie(tree);

            return tree;
        };
        Supplier<TestRegionBSPTree> complementFactory = () -> {
            TestRegionBSPTree tree = treeFactory.get();
            tree.complement();

            return tree;
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
        intersectionChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();
    }

    @Test
    public void testIntersection_simpleCrossingCuts() {
        // act/assert
        intersectionChecker(AbstractRegionBSPTreeTest::xAxisTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::xAxisTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(AbstractRegionBSPTreeTest::yAxisTree, AbstractRegionBSPTreeTest::fullTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .outside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        intersectionChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .outside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        intersectionChecker(AbstractRegionBSPTreeTest::xAxisTree, AbstractRegionBSPTreeTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(-1, 1))
            .outside(new TestPoint2D(1, 1), new TestPoint2D(1, -1), new TestPoint2D(-1, -1))
            .boundary(TestPoint2D.ZERO)
            .count(5)
            .check(tree -> {
                TestLineSegment seg = (TestLineSegment) tree.getRoot().getMinus().getCut();

                PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, seg.getStartPoint());
                PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.0, Double.POSITIVE_INFINITY), seg.getEndPoint());
            });
    }

    @Test
    public void testIntersection_boxTreeWithSingleCutTree() {
        // arrange
        Supplier<TestRegionBSPTree> boxFactory = () -> {
            TestRegionBSPTree box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        Supplier<TestRegionBSPTree> horizonalFactory = () -> {
            TestRegionBSPTree horizontal = fullTree();
            horizontal.getRoot().insertCut(new TestLine(new TestPoint2D(2, -2), new TestPoint2D(0, -2)));

            return horizontal;
        };

        // act/assert
        intersectionChecker(horizonalFactory, boxFactory)
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
        Supplier<TestRegionBSPTree> treeFactory = () -> {
            TestRegionBSPTree tree = fullTree();
            insertSkewedBowtie(tree);

            return tree;
        };
        Supplier<TestRegionBSPTree> complementFactory = () -> {
            TestRegionBSPTree tree = treeFactory.get();
            tree.complement();

            return tree;
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
        differenceChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        differenceChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();
    }

    @Test
    public void testDifference_simpleCrossingCuts() {
        // act/assert
        differenceChecker(AbstractRegionBSPTreeTest::xAxisTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(0, 1))
            .outside(new TestPoint2D(0, -1))
            .boundary(TestPoint2D.ZERO)
            .count(3)
            .check();

        differenceChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::xAxisTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(AbstractRegionBSPTreeTest::yAxisTree, AbstractRegionBSPTreeTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        differenceChecker(AbstractRegionBSPTreeTest::xAxisTree, AbstractRegionBSPTreeTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(1, -1), new TestPoint2D(-1, -1))
            .boundary(TestPoint2D.ZERO)
            .count(5)
            .check(tree -> {
                TestLineSegment seg = (TestLineSegment) tree.getRoot().getMinus().getCut();

                PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, seg.getStartPoint());
                PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.0, Double.POSITIVE_INFINITY), seg.getEndPoint());
            });
    }

    @Test
    public void testDifference_boxTreeWithSingleCutTree() {
        // arrange
        Supplier<TestRegionBSPTree> boxFactory = () -> {
            TestRegionBSPTree box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        Supplier<TestRegionBSPTree> horizonalFactory = () -> {
            TestRegionBSPTree horizontal = fullTree();
            horizontal.getRoot().insertCut(new TestLine(new TestPoint2D(2, -2), new TestPoint2D(0, -2)));

            return horizontal;
        };

        // act/assert
        differenceChecker(horizonalFactory, boxFactory)
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
        Supplier<TestRegionBSPTree> treeFactory = () -> {
            TestRegionBSPTree tree = fullTree();
            insertSkewedBowtie(tree);

            return tree;
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
        xorChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        xorChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        xorChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        xorChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();
    }

    @Test
    public void testXor_simpleCrossingCuts() {
        // act/assert
        xorChecker(AbstractRegionBSPTreeTest::xAxisTree, AbstractRegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(0, 1))
            .outside(new TestPoint2D(0, -1))
            .boundary(TestPoint2D.ZERO)
            .count(3)
            .check();

        xorChecker(AbstractRegionBSPTreeTest::emptyTree, AbstractRegionBSPTreeTest::xAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(0, 1))
            .outside(new TestPoint2D(0, -1))
            .boundary(TestPoint2D.ZERO)
            .count(3)
            .check();

        xorChecker(AbstractRegionBSPTreeTest::yAxisTree, AbstractRegionBSPTreeTest::fullTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        xorChecker(AbstractRegionBSPTreeTest::fullTree, AbstractRegionBSPTreeTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        xorChecker(AbstractRegionBSPTreeTest::xAxisTree, AbstractRegionBSPTreeTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(-1, -1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(1, -1))
            .boundary(TestPoint2D.ZERO)
            .count(7)
            .check(tree -> {
                TestLineSegment minusSeg = (TestLineSegment) tree.getRoot().getMinus().getCut();

                PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, minusSeg.getStartPoint());
                PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.0, Double.POSITIVE_INFINITY), minusSeg.getEndPoint());

                TestLineSegment plusSeg = (TestLineSegment) tree.getRoot().getPlus().getCut();

                PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.0, Double.NEGATIVE_INFINITY), plusSeg.getStartPoint());
                PartitionTestUtils.assertPointsEqual(TestPoint2D.ZERO, plusSeg.getEndPoint());
            });
    }

    @Test
    public void testXor_boxTreeWithSingleCutTree() {
        // arrange
        Supplier<TestRegionBSPTree> boxFactory = () -> {
            TestRegionBSPTree box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        Supplier<TestRegionBSPTree> horizonalFactory = () -> {
            TestRegionBSPTree horizontal = fullTree();
            horizontal.getRoot().insertCut(new TestLine(new TestPoint2D(2, -2), new TestPoint2D(0, -2)));

            return horizontal;
        };

        // act/assert
        xorChecker(horizonalFactory, boxFactory)
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
        Supplier<TestRegionBSPTree> treeFactory = () -> {
            TestRegionBSPTree tree = fullTree();
            insertSkewedBowtie(tree);

            return tree;
        };
        Supplier<TestRegionBSPTree> complementFactory = () -> {
            TestRegionBSPTree tree = treeFactory.get();
            tree.complement();

            return tree;
        };

        // act/assert
        xorChecker(treeFactory, complementFactory)
            .full(true)
            .empty(false)
            .count(1)
            .check();
    }

    @Test
    public void testProject_emptyAndFull() {
        // arrange
        TestRegionBSPTree full = fullTree();
        TestRegionBSPTree empty = emptyTree();

        // act/assert
        Assert.assertNull(full.project(new TestPoint2D(0, 0)));
        Assert.assertNull(empty.project(new TestPoint2D(-1, 1)));
    }

    @Test
    public void testProject_halfSpace() {
        // arrange
        TestRegionBSPTree tree = fullTree();
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
        TestRegionBSPTree tree = fullTree();
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
        TestRegionBSPTree tree = emptyTree();

        // act
        Split<TestRegionBSPTree> split = tree.split(TestLine.X_AXIS);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_full() {
        // arrange
        TestRegionBSPTree tree = fullTree();

        // act
        Split<TestRegionBSPTree> split = tree.split(TestLine.X_AXIS);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        TestRegionBSPTree minus = split.getMinus();
        assertPointLocations(minus, RegionLocation.INSIDE,
                new TestPoint2D(-1, 1), new TestPoint2D(0, 1), new TestPoint2D(1, 1));
        assertPointLocations(minus, RegionLocation.BOUNDARY,
                new TestPoint2D(-1, 0), new TestPoint2D(0, 0), new TestPoint2D(1, 0));
        assertPointLocations(minus, RegionLocation.OUTSIDE,
                new TestPoint2D(-1, -1), new TestPoint2D(0, -1), new TestPoint2D(1, -1));

        TestRegionBSPTree plus = split.getPlus();
        assertPointLocations(plus, RegionLocation.OUTSIDE,
                new TestPoint2D(-1, 1), new TestPoint2D(0, 1), new TestPoint2D(1, 1));
        assertPointLocations(plus, RegionLocation.BOUNDARY,
                new TestPoint2D(-1, 0), new TestPoint2D(0, 0), new TestPoint2D(1, 0));
        assertPointLocations(plus, RegionLocation.INSIDE,
                new TestPoint2D(-1, -1), new TestPoint2D(0, -1), new TestPoint2D(1, -1));
    }

    @Test
    public void testSplit_halfSpace() {
        // arrange
        TestRegionBSPTree tree = fullTree();
        tree.getRoot().insertCut(TestLine.X_AXIS);

        TestLine splitter = TestLine.Y_AXIS;

        // act
        Split<TestRegionBSPTree> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        TestRegionBSPTree minus = split.getMinus();
        assertPointLocations(minus, RegionLocation.INSIDE, new TestPoint2D(-1, 1));
        assertPointLocations(minus, RegionLocation.OUTSIDE,
                new TestPoint2D(1, 1), new TestPoint2D(-1, -1), new TestPoint2D(1, -1));

        TestRegionBSPTree plus = split.getPlus();
        assertPointLocations(plus, RegionLocation.INSIDE, new TestPoint2D(1, 1));
        assertPointLocations(plus, RegionLocation.OUTSIDE,
                new TestPoint2D(-1, 1), new TestPoint2D(-1, -1), new TestPoint2D(1, -1));
    }

    @Test
    public void testSplit_box() {
        // arrange
        TestRegionBSPTree tree = fullTree();
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));

        TestLine splitter = new TestLine(new TestPoint2D(1, 0), new TestPoint2D(0, 1));

        // act
        Split<TestRegionBSPTree> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        TestRegionBSPTree minus = split.getMinus();
        assertPointLocations(minus, RegionLocation.INSIDE, new TestPoint2D(0.25, 0.25));
        assertPointLocations(minus, RegionLocation.BOUNDARY,
                new TestPoint2D(0.5, 0), new TestPoint2D(0, 0.5));
        assertPointLocations(minus, RegionLocation.OUTSIDE,
                new TestPoint2D(1, 0.5), new TestPoint2D(0.5, 1), new TestPoint2D(0.75, 0.75));

        TestRegionBSPTree plus = split.getPlus();
        assertPointLocations(plus, RegionLocation.INSIDE, new TestPoint2D(0.75, 0.75));
        assertPointLocations(plus, RegionLocation.OUTSIDE,
                new TestPoint2D(0.5, 0), new TestPoint2D(0, 0.5), new TestPoint2D(0.25, 0.25));
        assertPointLocations(plus, RegionLocation.BOUNDARY,
                new TestPoint2D(1, 0.5), new TestPoint2D(0.5, 1));
    }

    @Test
    public void testSplit_box_onMinusOnly() {
        // arrange
        TestRegionBSPTree tree = fullTree();
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));

        TestLine splitter = new TestLine(new TestPoint2D(2, 0), new TestPoint2D(1, 1));

        // act
        Split<TestRegionBSPTree> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        TestRegionBSPTree minus = split.getMinus();
        assertPointLocations(minus, RegionLocation.INSIDE, new TestPoint2D(0.5, 0.5));
        assertPointLocations(minus, RegionLocation.BOUNDARY,
                new TestPoint2D(0.5, 0), new TestPoint2D(0, 0.5),
                new TestPoint2D(1, 0.5), new TestPoint2D(0.5, 1));

        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_box_onPlusOnly() {
        // arrange
        TestRegionBSPTree tree = fullTree();
        insertBox(tree, new TestPoint2D(0, 1), new TestPoint2D(1, 0));

        TestLine splitter = new TestLine(new TestPoint2D(0, 0), new TestPoint2D(-1, 1));

        // act
        Split<TestRegionBSPTree> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());

        TestRegionBSPTree plus = split.getPlus();
        assertPointLocations(plus, RegionLocation.INSIDE, new TestPoint2D(0.5, 0.5));
        assertPointLocations(plus, RegionLocation.BOUNDARY,
                new TestPoint2D(0.5, 0), new TestPoint2D(0, 0.5),
                new TestPoint2D(1, 0.5), new TestPoint2D(0.5, 1));
    }

    @Test
    public void testToString() {
        // arrange
        TestRegionBSPTree tree = fullTree();
        tree.getRoot().cut(TestLine.X_AXIS);

        // act
        String str = tree.toString();

        // assert
        Assert.assertEquals("TestRegionBSPTree[count= 3, height= 1]", str);
        Assert.assertTrue(tree.getRoot().toString().contains("TestRegionNode"));
    }

    /** Assert that all given points lie in the expected location of the region.
     * @param tree region tree
     * @param points points to test
     * @param location expected location of all points
     */
    private static void assertPointLocations(final TestRegionBSPTree tree, final RegionLocation location,
            final TestPoint2D ... points) {
        assertPointLocations(tree, location, Arrays.asList(points));
    }

    /** Assert that all given points lie in the expected location of the region.
     * @param tree region tree
     * @param points points to test
     * @param location expected location of all points
     */
    private static void assertPointLocations(final TestRegionBSPTree tree, final RegionLocation location,
            final List<TestPoint2D> points) {

        for (TestPoint2D p : points) {
            Assert.assertEquals("Unexpected location for point " + p, location, tree.classify(p));
        }
    }

    private static MergeChecker unionChecker(
            final Supplier<TestRegionBSPTree> r1,
            final Supplier<TestRegionBSPTree> r2) {

        MergeOperation constOperation = (a, b) -> {
            TestRegionBSPTree result = fullTree();
            result.union(a, b);
            return result;
        };

        MergeOperation inPlaceOperation = (a, b) -> {
            a.union(b);
            return a;
        };

        return new MergeChecker(r1, r2, constOperation, inPlaceOperation);
    }

    private static MergeChecker intersectionChecker(
            final Supplier<TestRegionBSPTree> tree1Factory,
            final Supplier<TestRegionBSPTree> tree2Factory) {

        MergeOperation constOperation = (a, b) -> {
            TestRegionBSPTree result = fullTree();
            result.intersection(a, b);
            return result;
        };

        MergeOperation inPlaceOperation = (a, b) -> {
            a.intersection(b);
            return a;
        };

        return new MergeChecker(tree1Factory, tree2Factory, constOperation, inPlaceOperation);
    }

    private static MergeChecker differenceChecker(
            final Supplier<TestRegionBSPTree> tree1Factory,
            final Supplier<TestRegionBSPTree> tree2Factory) {

        MergeOperation constOperation = (a, b) -> {
            TestRegionBSPTree result = fullTree();
            result.difference(a, b);
            return result;
        };

        MergeOperation inPlaceOperation = (a, b) -> {
            a.difference(b);
            return a;
        };

        return new MergeChecker(tree1Factory, tree2Factory, constOperation, inPlaceOperation);
    }

    private static MergeChecker xorChecker(
            final Supplier<TestRegionBSPTree> tree1Factory,
            final Supplier<TestRegionBSPTree> tree2Factory) {

        MergeOperation constOperation = (a, b) -> {
            TestRegionBSPTree result = fullTree();
            result.xor(a, b);
            return result;
        };

        MergeOperation inPlaceOperation = (a, b) -> {
            a.xor(b);
            return a;
        };

        return new MergeChecker(tree1Factory, tree2Factory, constOperation, inPlaceOperation);
    }

    private static void insertBox(final TestRegionBSPTree tree, final TestPoint2D upperLeft, final TestPoint2D lowerRight) {
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

    private static void assertCutBoundarySegment(final SubHyperplane<TestPoint2D> boundary, final TestPoint2D start, final TestPoint2D end) {
        Assert.assertFalse("Expected boundary to not be empty", boundary.isEmpty());

        TestLineSegmentCollection segmentCollection = (TestLineSegmentCollection) boundary;
        Assert.assertEquals(1, segmentCollection.getLineSegments().size());

        TestLineSegment segment = segmentCollection.getLineSegments().get(0);
        PartitionTestUtils.assertPointsEqual(start, segment.getStartPoint());
        PartitionTestUtils.assertPointsEqual(end, segment.getEndPoint());
    }

    private static void assertContainsSegment(final List<TestLineSegment> boundaries, final TestPoint2D start, final TestPoint2D end) {
        boolean found = false;
        for (TestLineSegment seg : boundaries) {
            TestPoint2D startPt = seg.getStartPoint();
            TestPoint2D endPt = seg.getEndPoint();

            if (PartitionTestUtils.PRECISION.eq(start.getX(), startPt.getX()) &&
                    PartitionTestUtils.PRECISION.eq(start.getY(), startPt.getY()) &&
                    PartitionTestUtils.PRECISION.eq(end.getX(), endPt.getX()) &&
                    PartitionTestUtils.PRECISION.eq(end.getY(), endPt.getY())) {
                found = true;
                break;
            }
        }

        Assert.assertTrue("Expected to find segment start= " + start + ", end= " + end , found);
    }

    private static TestRegionBSPTree emptyTree() {
        return new TestRegionBSPTree(false);
    }

    private static TestRegionBSPTree fullTree() {
        return new TestRegionBSPTree(true);
    }

    private static TestRegionBSPTree xAxisTree() {
        TestRegionBSPTree tree = fullTree();
        tree.getRoot().cut(TestLine.X_AXIS);

        return tree;
    }

    private static TestRegionBSPTree yAxisTree() {
        TestRegionBSPTree tree = fullTree();
        tree.getRoot().cut(TestLine.Y_AXIS);

        return tree;
    }

    /** Helper interface used when testing tree merge operations.
     */
    @FunctionalInterface
    private static interface MergeOperation {
        TestRegionBSPTree apply(TestRegionBSPTree tree1, TestRegionBSPTree tree2);
    }

    /** Helper class with a fluent API used to construct assert conditions on tree merge operations.
     */
    private static class MergeChecker {

        /** First tree in the merge operation */
        private final Supplier<TestRegionBSPTree> tree1Factory;

        /** Second tree in the merge operation */
        private final Supplier<TestRegionBSPTree> tree2Factory;

        /** Merge operation that does not modify either input tree */
        private final MergeOperation constOperation;

        /** Merge operation that stores the result in the first input tree
         * and leaves the second one unmodified.
         */
        private final MergeOperation inPlaceOperation;

        /** If true, the resulting tree will be printed to stdout to help with
         * debugging.
         */
        private boolean print;

        /** The expected node count of the merged tree */
        private int expectedCount = -1;

        /** The expected full state of the merged tree */
        private boolean expectedFull = false;

        /** The expected empty state of the merged tree */
        private boolean expectedEmpty = false;

        /** Points expected to lie in the inside of the region */
        private List<TestPoint2D> insidePoints = new ArrayList<>();

        /** Points expected to lie on the outside of the region */
        private List<TestPoint2D> outsidePoints = new ArrayList<>();

        /** Points expected to lie on the  boundary of the region */
        private List<TestPoint2D> boundaryPoints = new ArrayList<>();

        /** Construct a new instance that will verify the output of performing the given merge operation
         * on the input trees.
         * @param tree1 first tree in the merge operation
         * @param tree2 second tree in the merge operation
         * @param constOperation object that performs the merge operation in a form that
         *      leaves both argument unmodified
         * @param inPlaceOperation object that performs the merge operation in a form
         *      that stores the result in the first input tree and leaves the second
         *      input unchanged.
         */
        public MergeChecker(
                final Supplier<TestRegionBSPTree> tree1Factory,
                final Supplier<TestRegionBSPTree> tree2Factory,
                final MergeOperation constOperation,
                final MergeOperation inPlaceOperation) {

            this.tree1Factory = tree1Factory;
            this.tree2Factory = tree2Factory;
            this.constOperation = constOperation;
            this.inPlaceOperation = inPlaceOperation;
        }

        /** Set the expected node count of the merged tree
         * @param expectedCount the expected node count of the merged tree
         * @return this instance
         */
        public MergeChecker count(final int expectedCount) {
            this.expectedCount = expectedCount;
            return this;
        }

        /** Set the expected full state of the merged tree.
         * @param expectedFull the expected full state of the merged tree.
         * @return this instance
         */
        public MergeChecker full(final boolean expectedFull) {
            this.expectedFull = expectedFull;
            return this;
        }

        /** Set the expected empty state of the merged tree.
         * @param expectedEmpty the expected empty state of the merged tree.
         * @return this instance
         */
        public MergeChecker empty(final boolean expectedEmpty) {
            this.expectedEmpty = expectedEmpty;
            return this;
        }

        /** Add points expected to be on the inside of the merged region.
         * @param points point expected to be on the inside of the merged
         *      region
         * @return this instance
         */
        public MergeChecker inside(TestPoint2D ... points) {
            insidePoints.addAll(Arrays.asList(points));
            return this;
        }

        /** Add points expected to be on the outside of the merged region.
         * @param points point expected to be on the outside of the merged
         *      region
         * @return this instance
         */
        public MergeChecker outside(TestPoint2D ... points) {
            outsidePoints.addAll(Arrays.asList(points));
            return this;
        }

        /** Add points expected to be on the boundary of the merged region.
         * @param points point expected to be on the boundary of the merged
         *      region
         * @return this instance
         */
        public MergeChecker boundary(TestPoint2D ... points) {
            boundaryPoints.addAll(Arrays.asList(points));
            return this;
        }

        /** Set the flag for printing the merged tree to stdout before performing assertions.
         * This can be useful for debugging tests.
         * @param print if set to true, the merged tree will be printed to stdout
         * @return this instance
         */
        @SuppressWarnings("unused")
        public MergeChecker print(final boolean print) {
            this.print = print;
            return this;
        }

        /** Perform the merge operation and verify the output.
         */
        public void check() {
            check(null);
        }

        /** Perform the merge operation and verify the output. The given consumer
         * is passed the merge result and can be used to perform extra assertions.
         * @param assertions consumer that will be passed the merge result; may
         *      be null
         */
        public void check(final Consumer<TestRegionBSPTree> assertions) {
            checkConst(assertions);
            checkInPlace(assertions);
        }

        private void checkConst(final Consumer<TestRegionBSPTree> assertions) {
            checkInternal(false, constOperation, assertions);
        }

        private void checkInPlace(final Consumer<TestRegionBSPTree> assertions) {
            checkInternal(true, inPlaceOperation, assertions);
        }

        private void checkInternal(final boolean inPlace, final MergeOperation operation,
                final Consumer<TestRegionBSPTree> assertions) {

            final TestRegionBSPTree tree1 = tree1Factory.get();
            final TestRegionBSPTree tree2 = tree2Factory.get();

            // store the number of nodes in each tree before the operation
            final int tree1BeforeCount = tree1.count();
            final int tree2BeforeCount = tree2.count();

            // perform the operation
            final TestRegionBSPTree result = operation.apply(tree1, tree2);

            if (print) {
                System.out.println((inPlace ? "In Place" : "Const") + " Result:");
                System.out.println(result.treeString());
            }

            // verify the internal consistency of all of the involved trees
            PartitionTestUtils.assertTreeStructure(tree1);
            PartitionTestUtils.assertTreeStructure(tree2);
            PartitionTestUtils.assertTreeStructure(result);

            // check full/empty status
            Assert.assertEquals("Unexpected tree 'full' property", expectedFull, result.isFull());
            Assert.assertEquals("Unexpected tree 'empty' property", expectedEmpty, result.isEmpty());

            // check the node count
            if (expectedCount > -1) {
                Assert.assertEquals("Unexpected node count", expectedCount, result.count());
            }

            // check in place or not
            if (inPlace) {
                Assert.assertSame("Expected merge operation to be in place", tree1, result);
            }
            else {
                Assert.assertNotSame("Expected merge operation to return a new instance", tree1, result);

                // make sure that tree1 wasn't modified
                Assert.assertEquals("Tree 1 node count should not have changed", tree1BeforeCount, tree1.count());
            }

            // make sure that tree2 wasn't modified
            Assert.assertEquals("Tree 2 node count should not have changed", tree2BeforeCount, tree2.count());

            // check region point locations
            assertPointLocations(result, RegionLocation.INSIDE, insidePoints);
            assertPointLocations(result, RegionLocation.OUTSIDE, outsidePoints);
            assertPointLocations(result, RegionLocation.BOUNDARY, boundaryPoints);

            // pass the result to the given function for any additional assertions
            if (assertions != null) {
                assertions.accept(result);
            }
        }
    }

    private static class TestRegionBSPTree extends AbstractRegionBSPTree<TestPoint2D, TestRegionNode> {

        private static final long serialVersionUID = 20190405L;

        TestRegionBSPTree() {
            this(true);
        }

        TestRegionBSPTree(final boolean full) {
            super(full);
        }

        /**
         * Expose the direct node cut method for easier creation of test tree structures.
         */
        @Override
        public void cutNode(final TestRegionNode node, final ConvexSubHyperplane<TestPoint2D> cut) {
            super.cutNode(node, cut);
        }

        @Override
        protected TestRegionNode createNode() {
            return new TestRegionNode(this);
        }

        @Override
        protected RegionSizeProperties<TestPoint2D> computeRegionSizeProperties() {
            // return a set of stub values
            return new RegionSizeProperties<>(1, TestPoint2D.ZERO);
        }

        @Override
        public boolean contains(TestPoint2D pt) {
            return classify(pt) != RegionLocation.OUTSIDE;
        }

        @Override
        public List<? extends ConvexHyperplaneBoundedRegion<TestPoint2D>> toConvex() {
            return null;
        }

        @Override
        public Split<TestRegionBSPTree> split(Hyperplane<TestPoint2D> splitter) {
            return split(splitter, new TestRegionBSPTree(), new TestRegionBSPTree());
        }
    }

    private static class TestRegionNode extends AbstractRegionNode<TestPoint2D, TestRegionNode> {

        private static final long serialVersionUID = 20190405L;

        protected TestRegionNode(AbstractBSPTree<TestPoint2D, TestRegionNode> tree) {
            super(tree);
        }

        @Override
        protected TestRegionNode getSelf() {
            return this;
        }
    }
}
