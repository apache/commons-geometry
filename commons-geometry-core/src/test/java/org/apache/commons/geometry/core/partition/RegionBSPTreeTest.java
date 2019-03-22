package org.apache.commons.geometry.core.partition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

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
    public void testCopy() {
        // arrange
        insertSkewedBowtie(tree);

        // act
        RegionBSPTree<TestPoint2D> copy = tree.copy();

        // assert
        Assert.assertNotSame(tree, copy);
        Assert.assertEquals(tree.count(), copy.count());

        List<RegionLocation> origLocations = new ArrayList<>();
        tree.nodes().forEach(n -> origLocations.add(n.getLocationValue()));

        List<RegionLocation> copyLocations = new ArrayList<>();
        copy.nodes().forEach(n -> copyLocations.add(n.getLocationValue()));

        Assert.assertEquals(origLocations, copyLocations);
    }

    @Test
    public void testUnion_noCuts() {
        // act/assert
        unionChecker(emptyTree(), emptyTree())
            .full(false)
            .empty(true)
            .count(1)
            .check();

        unionChecker(fullTree(), emptyTree())
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(emptyTree(), fullTree())
            .full(true)
            .empty(false)
            .count(1)
            .check();

        unionChecker(fullTree(), fullTree())
            .full(true)
            .empty(false)
            .count(1)
            .check();
    }

    @Test
    public void testUnion_singleCutTreeWithNoCuts() {
        // arrange
        RegionBSPTree<TestPoint2D> horizontal = fullTree();
        horizontal.getRoot().cut(TestLine.X_AXIS);

        RegionBSPTree<TestPoint2D> vertical = fullTree();
        vertical.getRoot().cut(TestLine.Y_AXIS);

        // act
        unionChecker(horizontal, emptyTree())
            .count(3)
            .check();

        unionChecker(emptyTree(), horizontal)
            .count(3)
            .check();

        unionChecker(horizontal, fullTree())
            .full(true)
            .count(1)
            .check();

        unionChecker(fullTree(), horizontal)
            .full(true)
            .count(1)
            .check();
    }

    @Test
    public void testUnion_singleCutBothTrees() {
        // arrange
        RegionBSPTree<TestPoint2D> horizontal = fullTree();
        horizontal.getRoot().cut(TestLine.X_AXIS);

        RegionBSPTree<TestPoint2D> vertical = fullTree();
        vertical.getRoot().cut(TestLine.Y_AXIS);

        // act/assert
        unionChecker(horizontal, vertical)
            .inside(new TestPoint2D(-1, 1), new TestPoint2D(-1, -1), new TestPoint2D(1, 1))
            .outside(new TestPoint2D(1, -1))
            .boundary(TestPoint2D.ZERO)
            .count(5)
            .print(true)
            .check();
    }

    private MergeChecker unionChecker(final RegionBSPTree<TestPoint2D> r1, final RegionBSPTree<TestPoint2D> r2) {
        return new MergeChecker(r1, r2, RegionBSPTree::union)
                .inPlace(false);
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

    /** Helper interface used when testing tree merge operations.
     */
    @FunctionalInterface
    private static interface MergeOperation {
        RegionBSPTree<TestPoint2D> merge(RegionBSPTree<TestPoint2D> tree1, RegionBSPTree<TestPoint2D> tree2);
    }

    /** Helper class with a fluent API used to construct assert conditions on tree merge operations.
     */
    private static class MergeChecker {

        /** First tree in the merge operation */
        private final RegionBSPTree<TestPoint2D> tree1;

        /** Second tree in the merge operation */
        private final RegionBSPTree<TestPoint2D> tree2;

        /** The merge operation itself */
        private final MergeOperation operation;

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

        /** If true, the tree is expected to be modified in place. Otherwise, a new tree
         * is expected to be returned with both inputs being unmodified
         */
        private boolean expectedInPlace = false;

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
         * @param operation object performing the merge operation itself
         */
        public MergeChecker(final RegionBSPTree<TestPoint2D> tree1, final RegionBSPTree<TestPoint2D> tree2,
                final MergeOperation operation) {

            this.tree1 = tree1;
            this.tree2 = tree2;
            this.operation = operation;
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

        /** Set whether or not the merge operation is expected to be in-place or not,
         * ie whether it returns a brand new tree or simply modifies the first input.
         * @param expectedInPlace the expected in-place status of the operation
         * @return this instance
         */
        public MergeChecker inPlace(final boolean expectedInPlace) {
            this.expectedInPlace = expectedInPlace;
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

            // store the number of nodes in each tree before the operation
            final int tree1BeforeCount = tree1.count();
            final long tree1BeforeVersion = tree1.getVersion();

            final int tree2BeforeCount = tree2.count();
            final long tree2BeforeVersion = tree2.getVersion();

            // perform the operation
            final RegionBSPTree<TestPoint2D> result = operation.merge(tree1, tree2);

            if (print) {
                PartitionTestUtils.printTree(result);
            }

            // check full/empty status
            Assert.assertEquals("Expected tree to be full", expectedFull, result.isFull());
            Assert.assertEquals("Expected tree to be empty", expectedEmpty, result.isEmpty());

            // check the node count
            if (expectedCount > -1) {
                Assert.assertEquals("Unexpected node count", expectedCount, result.count());
            }

            // check depth properties
            checkNodeDepths(result);

            // check in place or not
            if (expectedInPlace) {
                Assert.assertSame("Expected merge operation to be in place", tree1, result);
            }
            else {
                Assert.assertNotSame("Expected merge operation to return a new instance", tree1, result);

                // make sure that tree1 wasn't modified
                Assert.assertEquals("Tree 1 node count should not have changed", tree1BeforeCount, tree1.count());
                Assert.assertEquals("Tree 1 version should not have changed", tree1BeforeVersion, tree1.getVersion());
            }

            // make sure that tree2 wasn't modified
            Assert.assertEquals("Tree 2 node count should not have changed", tree2BeforeCount, tree2.count());
            Assert.assertEquals("Tree 2 version should not have changed", tree2BeforeVersion, tree2.getVersion());

            // check region point locations
            assertPointLocations(result, insidePoints, RegionLocation.INSIDE);
            assertPointLocations(result, outsidePoints, RegionLocation.OUTSIDE);
            assertPointLocations(result, boundaryPoints, RegionLocation.BOUNDARY);

            // pass the result to the given function for any additional assertions
            if (assertions != null) {
                assertions.accept(result);
            }
        }

        /** Check that all nodes in the given tree have a correct 'depth' property. This can be an issue
         * when nodes are moved around the way they are in the merge operation.
         * @param tree
         */
        private void checkNodeDepths(final RegionBSPTree<TestPoint2D> tree) {
            checkNodeDepthsRecursive(tree.getRoot(), 0);
        }

        /** Recursively check that the given nodes and all child nodes have a correct depth property.
         * @param node
         * @param expectedDepth
         */
        private void checkNodeDepthsRecursive(final RegionNode<TestPoint2D> node, final int expectedDepth) {
            if (node != null) {
                Assert.assertEquals("Node has an incorrect depth property", node.depth(), expectedDepth);

                checkNodeDepthsRecursive(node.getPlus(), expectedDepth + 1);
                checkNodeDepthsRecursive(node.getMinus(), expectedDepth + 1);
            }
        }

        /** Assert that all points in the given list lie in the expected location of the region.
         * @param tree region tree
         * @param points points to test
         * @param location expected location of all points
         */
        private void assertPointLocations(final RegionBSPTree<TestPoint2D> tree, final List<TestPoint2D> points,
                final RegionLocation location) {

            for (TestPoint2D p : points) {
                Assert.assertEquals("Unexpected location for point " + p, location, tree.classify(p));
            }
        }
    }
}
