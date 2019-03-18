package org.apache.commons.geometry.core.partition;

import java.util.Arrays;

import org.apache.commons.geometry.core.partition.RegionBSPTree.RegionNode;
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
    public void testInitialization() {
        // assert
        Assert.assertNotNull(root);
        Assert.assertNull(root.getParent());

        PartitionTestUtils.assertIsLeafNode(root);
        Assert.assertFalse(root.isPlus());
        Assert.assertFalse(root.isMinus());

        Assert.assertSame(tree, root.getTree());
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
}
