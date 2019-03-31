package org.apache.commons.geometry.core.partition.region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.core.partition.region.RegionBSPTree.RegionNode;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestLineSegment;
import org.apache.commons.geometry.core.partition.test.TestLineSegmentCollection;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RegionBSPTreeTest {

    private RegionBSPTree<TestPoint2D> tree;

    private RegionNode<TestPoint2D> root;

    @Before
    public void setup() {
        tree = new RegionBSPTree<>();
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
        tree = new RegionBSPTree<>(true);
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
        tree = new RegionBSPTree<>(false);
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

        RegionNode<TestPoint2D> plus = root.getPlus();
        Assert.assertNull(plus.getLocation());

        RegionNode<TestPoint2D> plusPlus = plus.getPlus();
        Assert.assertEquals(RegionLocation.OUTSIDE, plusPlus.getLocation());

        RegionNode<TestPoint2D> plusMinus = plus.getMinus();
        Assert.assertEquals(RegionLocation.INSIDE, plusMinus.getLocation());

        RegionNode<TestPoint2D> minus = root.getMinus();
        Assert.assertNull(minus.getLocation());

        RegionNode<TestPoint2D> minusPlus = minus.getPlus();
        Assert.assertEquals(RegionLocation.OUTSIDE, minusPlus.getLocation());

        RegionNode<TestPoint2D> minusMinus = minus.getMinus();
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

        RegionNode<TestPoint2D> root = tree.getRoot();

        // act
        root.clearCut();

        // assert
        Assert.assertEquals(RegionLocation.INSIDE, root.getLocation());
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
        RegionBSPTree<TestPoint2D> other = fullTree();
        insertSkewedBowtie(other);

        // act
        other.complementOf(tree);

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

        RegionBSPTree<TestPoint2D> other = fullTree();

        // act
        other.complementOf(tree);

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
        RegionBSPTree<TestPoint2D> copy = fullTree();
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

        RegionBSPTree<TestPoint2D> result = fullTree();

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

        RegionBSPTree<TestPoint2D> result = fullTree();

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
        unionChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        unionChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::emptyTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();
    }

    @Test
    public void testUnion_simpleCrossingCuts() {
        // act/assert
        unionChecker(RegionBSPTreeTest::xAxisTree, RegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(false)
            .count(3)
            .check();

        unionChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::xAxisTree)
            .full(false)
            .empty(false)
            .count(3)
            .check();

        unionChecker(RegionBSPTreeTest::yAxisTree, RegionBSPTreeTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::yAxisTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(RegionBSPTreeTest::xAxisTree, RegionBSPTreeTest::yAxisTree)
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
        Supplier<RegionBSPTree<TestPoint2D>> boxFactory = () -> {
            RegionBSPTree<TestPoint2D> box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        Supplier<RegionBSPTree<TestPoint2D>> horizonalFactory = () -> {
            RegionBSPTree<TestPoint2D> horizontal = fullTree();
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
        Supplier<RegionBSPTree<TestPoint2D>> treeFactory = () -> {
            RegionBSPTree<TestPoint2D> tree = fullTree();
            insertSkewedBowtie(tree);

            return tree;
        };
        Supplier<RegionBSPTree<TestPoint2D>> complementFactory = () -> {
            RegionBSPTree<TestPoint2D> tree = treeFactory.get();
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
        intersectionChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();
    }

    @Test
    public void testIntersection_simpleCrossingCuts() {
        // act/assert
        intersectionChecker(RegionBSPTreeTest::xAxisTree, RegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::xAxisTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        intersectionChecker(RegionBSPTreeTest::yAxisTree, RegionBSPTreeTest::fullTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .outside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        intersectionChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .outside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        intersectionChecker(RegionBSPTreeTest::xAxisTree, RegionBSPTreeTest::yAxisTree)
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
        Supplier<RegionBSPTree<TestPoint2D>> boxFactory = () -> {
            RegionBSPTree<TestPoint2D> box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        Supplier<RegionBSPTree<TestPoint2D>> horizonalFactory = () -> {
            RegionBSPTree<TestPoint2D> horizontal = fullTree();
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
        Supplier<RegionBSPTree<TestPoint2D>> treeFactory = () -> {
            RegionBSPTree<TestPoint2D> tree = fullTree();
            insertSkewedBowtie(tree);

            return tree;
        };
        Supplier<RegionBSPTree<TestPoint2D>> complementFactory = () -> {
            RegionBSPTree<TestPoint2D> tree = treeFactory.get();
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
        differenceChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::emptyTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        differenceChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();
    }

    @Test
    public void testDifference_simpleCrossingCuts() {
        // act/assert
        differenceChecker(RegionBSPTreeTest::xAxisTree, RegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(0, 1))
            .outside(new TestPoint2D(0, -1))
            .boundary(TestPoint2D.ZERO)
            .count(3)
            .check();

        differenceChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::xAxisTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(RegionBSPTreeTest::yAxisTree, RegionBSPTreeTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        differenceChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        differenceChecker(RegionBSPTreeTest::xAxisTree, RegionBSPTreeTest::yAxisTree)
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
        Supplier<RegionBSPTree<TestPoint2D>> boxFactory = () -> {
            RegionBSPTree<TestPoint2D> box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        Supplier<RegionBSPTree<TestPoint2D>> horizonalFactory = () -> {
            RegionBSPTree<TestPoint2D> horizontal = fullTree();
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
        Supplier<RegionBSPTree<TestPoint2D>> treeFactory = () -> {
            RegionBSPTree<TestPoint2D> tree = fullTree();
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
        xorChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();

        xorChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::emptyTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        xorChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::fullTree)
            .full(true)
            .empty(false)
            .count(1)
            .check();

        xorChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::fullTree)
            .full(false)
            .empty(true)
            .count(1)
            .check();
    }

    @Test
    public void testXor_simpleCrossingCuts() {
        // act/assert
        xorChecker(RegionBSPTreeTest::xAxisTree, RegionBSPTreeTest::emptyTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(0, 1))
            .outside(new TestPoint2D(0, -1))
            .boundary(TestPoint2D.ZERO)
            .count(3)
            .check();

        xorChecker(RegionBSPTreeTest::emptyTree, RegionBSPTreeTest::xAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(0, 1))
            .outside(new TestPoint2D(0, -1))
            .boundary(TestPoint2D.ZERO)
            .count(3)
            .check();

        xorChecker(RegionBSPTreeTest::yAxisTree, RegionBSPTreeTest::fullTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        xorChecker(RegionBSPTreeTest::fullTree, RegionBSPTreeTest::yAxisTree)
            .full(false)
            .empty(false)
            .inside(new TestPoint2D(1, 1), new TestPoint2D(1, -1))
            .outside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1))
            .boundary(new TestPoint2D(0, 1), new TestPoint2D(0, -1))
            .count(3)
            .check();

        xorChecker(RegionBSPTreeTest::xAxisTree, RegionBSPTreeTest::yAxisTree)
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
        Supplier<RegionBSPTree<TestPoint2D>> boxFactory = () -> {
            RegionBSPTree<TestPoint2D> box = fullTree();
            insertBox(box, TestPoint2D.ZERO, new TestPoint2D(2, -4));
            return box;
        };

        Supplier<RegionBSPTree<TestPoint2D>> horizonalFactory = () -> {
            RegionBSPTree<TestPoint2D> horizontal = fullTree();
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
        Supplier<RegionBSPTree<TestPoint2D>> treeFactory = () -> {
            RegionBSPTree<TestPoint2D> tree = fullTree();
            insertSkewedBowtie(tree);

            return tree;
        };
        Supplier<RegionBSPTree<TestPoint2D>> complementFactory = () -> {
            RegionBSPTree<TestPoint2D> tree = treeFactory.get();
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
    public void testToString() {
        // arrange
        RegionBSPTree<TestPoint2D> tree = fullTree();
        tree.getRoot().cut(TestLine.X_AXIS);

        // act
        String str = tree.toString();

        // assert
        Assert.assertEquals("RegionBSPTree[count= 3, height= 1]", str);
    }

    /** Assert that all given points lie in the expected location of the region.
     * @param tree region tree
     * @param points points to test
     * @param location expected location of all points
     */
    private static void assertPointLocations(final RegionBSPTree<TestPoint2D> tree, final RegionLocation location,
            final TestPoint2D ... points) {
        assertPointLocations(tree, location, Arrays.asList(points));
    }

    /** Assert that all given points lie in the expected location of the region.
     * @param tree region tree
     * @param points points to test
     * @param location expected location of all points
     */
    private static void assertPointLocations(final RegionBSPTree<TestPoint2D> tree, final RegionLocation location,
            final List<TestPoint2D> points) {

        for (TestPoint2D p : points) {
            Assert.assertEquals("Unexpected location for point " + p, location, tree.classify(p));
        }
    }

    private static MergeChecker unionChecker(
            final Supplier<RegionBSPTree<TestPoint2D>> r1,
            final Supplier<RegionBSPTree<TestPoint2D>> r2) {

        MergeOperation constOperation = (a, b) -> {
            RegionBSPTree<TestPoint2D> result = fullTree();
            result.unionOf(a, b);
            return result;
        };

        MergeOperation inPlaceOperation = (a, b) -> {
            a.union(b);
            return a;
        };

        return new MergeChecker(r1, r2, constOperation, inPlaceOperation);
    }

    private static MergeChecker intersectionChecker(
            final Supplier<RegionBSPTree<TestPoint2D>> tree1Factory,
            final Supplier<RegionBSPTree<TestPoint2D>> tree2Factory) {

        MergeOperation constOperation = (a, b) -> {
            RegionBSPTree<TestPoint2D> result = fullTree();
            result.intersectionOf(a, b);
            return result;
        };

        MergeOperation inPlaceOperation = (a, b) -> {
            a.intersection(b);
            return a;
        };

        return new MergeChecker(tree1Factory, tree2Factory, constOperation, inPlaceOperation);
    }

    private static MergeChecker differenceChecker(
            final Supplier<RegionBSPTree<TestPoint2D>> tree1Factory,
            final Supplier<RegionBSPTree<TestPoint2D>> tree2Factory) {

        MergeOperation constOperation = (a, b) -> {
            RegionBSPTree<TestPoint2D> result = fullTree();
            result.differenceOf(a, b);
            return result;
        };

        MergeOperation inPlaceOperation = (a, b) -> {
            a.difference(b);
            return a;
        };

        return new MergeChecker(tree1Factory, tree2Factory, constOperation, inPlaceOperation);
    }

    private static MergeChecker xorChecker(
            final Supplier<RegionBSPTree<TestPoint2D>> tree1Factory,
            final Supplier<RegionBSPTree<TestPoint2D>> tree2Factory) {

        MergeOperation constOperation = (a, b) -> {
            RegionBSPTree<TestPoint2D> result = fullTree();
            result.xorOf(a, b);
            return result;
        };

        MergeOperation inPlaceOperation = (a, b) -> {
            a.xor(b);
            return a;
        };

        return new MergeChecker(tree1Factory, tree2Factory, constOperation, inPlaceOperation);
    }

    private static void insertBox(final RegionBSPTree<TestPoint2D> tree, final TestPoint2D upperLeft, final TestPoint2D lowerRight) {
        final TestPoint2D upperRight = new TestPoint2D(lowerRight.getX(), upperLeft.getY());
        final TestPoint2D lowerLeft = new TestPoint2D(upperLeft.getX(), lowerRight.getY());

        tree.insert(Arrays.asList(
                    new TestLineSegment(lowerRight, upperRight),
                    new TestLineSegment(upperRight, upperLeft),
                    new TestLineSegment(upperLeft, lowerLeft),
                    new TestLineSegment(lowerLeft, lowerRight)
                ));
    }

    private static void insertSkewedBowtie(final RegionBSPTree<TestPoint2D> tree) {
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

    private static RegionBSPTree<TestPoint2D> emptyTree() {
        return new RegionBSPTree<>(false);
    }

    private static RegionBSPTree<TestPoint2D> fullTree() {
        return new RegionBSPTree<>(true);
    }

    private static RegionBSPTree<TestPoint2D> xAxisTree() {
        RegionBSPTree<TestPoint2D> tree = fullTree();
        tree.getRoot().cut(TestLine.X_AXIS);

        return tree;
    }

    private static RegionBSPTree<TestPoint2D> yAxisTree() {
        RegionBSPTree<TestPoint2D> tree = fullTree();
        tree.getRoot().cut(TestLine.Y_AXIS);

        return tree;
    }

    /** Helper interface used when testing tree merge operations.
     */
    @FunctionalInterface
    private static interface MergeOperation {
        RegionBSPTree<TestPoint2D> apply(RegionBSPTree<TestPoint2D> tree1, RegionBSPTree<TestPoint2D> tree2);
    }

    /** Helper class with a fluent API used to construct assert conditions on tree merge operations.
     */
    private static class MergeChecker {

        /** First tree in the merge operation */
        private final Supplier<RegionBSPTree<TestPoint2D>> tree1Factory;

        /** Second tree in the merge operation */
        private final Supplier<RegionBSPTree<TestPoint2D>> tree2Factory;

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
                final Supplier<RegionBSPTree<TestPoint2D>> tree1Factory,
                final Supplier<RegionBSPTree<TestPoint2D>> tree2Factory,
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
        public void check(final Consumer<RegionBSPTree<TestPoint2D>> assertions) {
            checkConst(assertions);
            checkInPlace(assertions);
        }

        private void checkConst(final Consumer<RegionBSPTree<TestPoint2D>> assertions) {
            checkInternal(false, constOperation, assertions);
        }

        private void checkInPlace(final Consumer<RegionBSPTree<TestPoint2D>> assertions) {
            checkInternal(true, inPlaceOperation, assertions);
        }

        private void checkInternal(final boolean inPlace, final MergeOperation operation,
                final Consumer<RegionBSPTree<TestPoint2D>> assertions) {

            final RegionBSPTree<TestPoint2D> tree1 = tree1Factory.get();
            final RegionBSPTree<TestPoint2D> tree2 = tree2Factory.get();

            // store the number of nodes in each tree before the operation
            final int tree1BeforeCount = tree1.count();
            final int tree2BeforeCount = tree2.count();

            // perform the operation
            final RegionBSPTree<TestPoint2D> result = operation.apply(tree1, tree2);

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
}
