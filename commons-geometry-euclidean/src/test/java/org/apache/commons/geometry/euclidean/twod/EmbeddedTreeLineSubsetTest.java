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
package org.apache.commons.geometry.euclidean.twod;

import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddedTreeLineSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Line DEFAULT_TEST_LINE =
            Lines.fromPointAndDirection(Vector2D.of(0, 1), Vector2D.Unit.PLUS_X, TEST_PRECISION);

    @Test
    public void testCtor_lineOnly() {
        // act
        EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE);

        // assert
        Assert.assertSame(DEFAULT_TEST_LINE, sub.getLine());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());

        Assert.assertFalse(sub.isFull());
        Assert.assertTrue(sub.isEmpty());
        Assert.assertFalse(sub.isInfinite());
        Assert.assertTrue(sub.isFinite());

        Assert.assertEquals(0, sub.getSize(), TEST_EPS);
        Assert.assertNull(sub.getBarycenter());
    }

    @Test
    public void testCtor_lineAndBoolean() {
        // act
        EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE, true);

        // assert
        Assert.assertSame(DEFAULT_TEST_LINE, sub.getLine());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());

        Assert.assertTrue(sub.isFull());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertTrue(sub.isInfinite());
        Assert.assertFalse(sub.isFinite());

        GeometryTestUtils.assertPositiveInfinity(sub.getSize());
        Assert.assertNull(sub.getBarycenter());
    }

    @Test
    public void testCtor_lineAndRegion() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.full();

        // act
        EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE, tree);

        // assert
        Assert.assertSame(DEFAULT_TEST_LINE, sub.getLine());
        Assert.assertSame(tree, sub.getSubspaceRegion());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());

        Assert.assertTrue(sub.isFull());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertTrue(sub.isInfinite());
        Assert.assertFalse(sub.isFinite());

        GeometryTestUtils.assertPositiveInfinity(sub.getSize());
        Assert.assertNull(sub.getBarycenter());
    }

    @Test
    public void testToConvex_full() {
        // arrange
        EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE, true);

        // act
        List<LineConvexSubset> segments = sub.toConvex();

        // assert
        Assert.assertEquals(1, segments.size());

        LineConvexSubset seg = segments.get(0);
        Assert.assertTrue(seg.isFull());
    }

    @Test
    public void testToConvex_empty() {
        // arrange
        EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE, false);

        // act
        List<LineConvexSubset> segments = sub.toConvex();

        // assert
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testToConvex_finiteAndInfiniteSegments() {
        // arrange
        EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE, false);
        RegionBSPTree1D tree = sub.getSubspaceRegion();
        tree.add(Interval.max(-2.0, TEST_PRECISION));
        tree.add(Interval.of(-1, 2, TEST_PRECISION));

        // act
        List<LineConvexSubset> segments = sub.toConvex();

        // assert
        Assert.assertEquals(2, segments.size());

        Assert.assertNull(segments.get(0).getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), segments.get(1).getEndPoint(), TEST_EPS);
    }

    @Test
    public void testAdd_lineSegment() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line otherLine = Lines.fromPointAndAngle(Vector2D.of(0, 1), 1e-11, TEST_PRECISION);

        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line);

        // act
        subset.add(Lines.subsetFromInterval(line, 2, 4));
        subset.add(Lines.subsetFromInterval(otherLine, 1, 3));
        subset.add(Lines.segmentFromPoints(Vector2D.of(-3, 1), Vector2D.of(-1, 1), TEST_PRECISION));

        // assert
        Assert.assertFalse(subset.isFull());
        Assert.assertFalse(subset.isEmpty());

        List<LineConvexSubset> segments = subset.toConvex();

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), segments.get(0).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEndPoint(), TEST_EPS);

        Assert.assertEquals(5, subset.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.7, 1), subset.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testAdd_subset() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        EmbeddedTreeLineSubset a = new EmbeddedTreeLineSubset(line);
        RegionBSPTree1D aTree = a.getSubspaceRegion();
        aTree.add(Interval.max(-3, TEST_PRECISION));
        aTree.add(Interval.of(1, 2, TEST_PRECISION));

        EmbeddedTreeLineSubset b = new EmbeddedTreeLineSubset(line);
        RegionBSPTree1D bTree = b.getSubspaceRegion();
        bTree.add(Interval.of(2, 4, TEST_PRECISION));
        bTree.add(Interval.of(-4, -2, TEST_PRECISION));

        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line);

        int aTreeCount = aTree.count();
        int bTreeCount = bTree.count();

        // act
        subset.add(a);
        subset.add(b);

        // assert
        Assert.assertFalse(subset.isFull());
        Assert.assertFalse(subset.isEmpty());

        List<LineConvexSubset> segments = subset.toConvex();

        Assert.assertEquals(2, segments.size());

        Assert.assertNull(segments.get(0).getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEndPoint(), TEST_EPS);

        Assert.assertEquals(aTreeCount, aTree.count());
        Assert.assertEquals(bTreeCount, bTree.count());

        GeometryTestUtils.assertPositiveInfinity(subset.getSize());
        Assert.assertNull(subset.getBarycenter());
    }

    @Test
    public void testAdd_argumentsFromDifferentLine() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line otherLine = Lines.fromPointAndAngle(Vector2D.of(0, 1), 1e-2, TEST_PRECISION);

        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            subset.add(Lines.subsetFromInterval(otherLine, 0, 1));
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            subset.add(new EmbeddedTreeLineSubset(otherLine));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testGetBounds_noBounds() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(1, 0), 0.25 * Math.PI, TEST_PRECISION);

        EmbeddedTreeLineSubset full = new EmbeddedTreeLineSubset(line, RegionBSPTree1D.full());
        EmbeddedTreeLineSubset empty = new EmbeddedTreeLineSubset(line, RegionBSPTree1D.empty());
        EmbeddedTreeLineSubset halfFull = new EmbeddedTreeLineSubset(line, Interval.min(1.0, TEST_PRECISION).toTree());

        // act/assert
        Assert.assertNull(full.getBounds());
        Assert.assertNull(empty.getBounds());
        Assert.assertNull(halfFull.getBounds());
    }

    @Test
    public void testGetBounds_hasBounds() {
        // arrange
        Line line = Lines.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, false);

        double sqrt2 = Math.sqrt(2);
        subset.getSubspaceRegion().add(Interval.of(-2 * sqrt2, -sqrt2, TEST_PRECISION));
        subset.getSubspaceRegion().add(Interval.of(0, sqrt2, TEST_PRECISION));

        // act
        Bounds2D bounds = subset.getBounds();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, -2), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testSplit_both_anglePositive() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        Line splitter = Lines.fromPointAndAngle(Vector2D.of(1, 0), 0.1 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        List<LineConvexSubset> minusSegments = split.getMinus().toConvex();
        Assert.assertEquals(1, minusSegments.size());
        checkFiniteSegment(minusSegments.get(0), Vector2D.ZERO, Vector2D.of(1, 0));

        List<LineConvexSubset> plusSegments = split.getPlus().toConvex();
        Assert.assertEquals(2, plusSegments.size());
        checkFiniteSegment(plusSegments.get(0), Vector2D.of(1, 0), Vector2D.of(2, 0));
        checkFiniteSegment(plusSegments.get(1), Vector2D.of(3, 0), Vector2D.of(4, 0));
    }

    @Test
    public void testSplit_both_angleNegative() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        Line splitter = Lines.fromPointAndAngle(Vector2D.of(1, 0), -0.9 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        List<LineConvexSubset> minusSegments = split.getMinus().toConvex();
        Assert.assertEquals(2, minusSegments.size());
        checkFiniteSegment(minusSegments.get(0), Vector2D.of(1, 0), Vector2D.of(2, 0));
        checkFiniteSegment(minusSegments.get(1), Vector2D.of(3, 0), Vector2D.of(4, 0));

        List<LineConvexSubset> plusSegments = split.getPlus().toConvex();
        Assert.assertEquals(1, plusSegments.size());
        checkFiniteSegment(plusSegments.get(0), Vector2D.ZERO, Vector2D.of(1, 0));
    }

    @Test
    public void testSplit_intersection_plusOnly() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        Line splitter = Lines.fromPointAndAngle(Vector2D.of(-1, 0), 0.1 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(subset, split.getPlus());
    }

    @Test
    public void testSplit_intersection_minusOnly() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        Line splitter = Lines.fromPointAndAngle(Vector2D.of(10, 0), 0.1 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(subset, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallel_plus() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        Line splitter = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act
        Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(subset, split.getPlus());
    }

    @Test
    public void testSplit_parallel_minus() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        Line splitter = Lines.fromPointAndAngle(Vector2D.of(0, -1), 0.0, TEST_PRECISION);

        // act
        Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(subset, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_coincident_sameDirection() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        Line splitter = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);

        // act
        Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_coincident_oppositeDirection() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        Line splitter = Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix2D mat = AffineTransformMatrix2D
                .createRotation(Vector2D.of(0, 1), PlaneAngleRadians.PI_OVER_TWO)
                .scale(Vector2D.of(3, 2));

        EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION));
        subset.getSubspaceRegion().add(Interval.of(0, 1, TEST_PRECISION));
        subset.getSubspaceRegion().add(Interval.min(3, TEST_PRECISION));

        // act
        EmbeddedTreeLineSubset transformed = subset.transform(mat);

        // assert
        Assert.assertNotSame(subset, transformed);

        List<LineConvexSubset> originalSegments = subset.toConvex();
        Assert.assertEquals(2, originalSegments.size());
        checkFiniteSegment(originalSegments.get(0), Vector2D.ZERO, Vector2D.Unit.PLUS_X);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 0), originalSegments.get(1).getStartPoint(), TEST_EPS);
        Assert.assertNull(originalSegments.get(1).getEndPoint());

        List<LineConvexSubset> transformedSegments = transformed.toConvex();
        Assert.assertEquals(2, transformedSegments.size());
        checkFiniteSegment(transformedSegments.get(0), Vector2D.of(3, 2), Vector2D.of(3, 4));
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 8), transformedSegments.get(1).getStartPoint(), TEST_EPS);
        Assert.assertNull(transformedSegments.get(1).getEndPoint());
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        AffineTransformMatrix2D mat = AffineTransformMatrix2D.createScale(Vector2D.of(-1, 2));

        EmbeddedTreeLineSubset subset =
                new EmbeddedTreeLineSubset(Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION));
        subset.getSubspaceRegion().add(Interval.of(0, 1, TEST_PRECISION));

        // act
        EmbeddedTreeLineSubset transformed = subset.transform(mat);

        // assert
        Assert.assertNotSame(subset, transformed);

        List<LineConvexSubset> originalSegments = subset.toConvex();
        Assert.assertEquals(1, originalSegments.size());
        checkFiniteSegment(originalSegments.get(0), Vector2D.of(0, 1), Vector2D.of(1, 1));

        List<LineConvexSubset> transformedSegments = transformed.toConvex();
        Assert.assertEquals(1, transformedSegments.size());
        checkFiniteSegment(transformedSegments.get(0), Vector2D.of(0, 2), Vector2D.of(-1, 2));
    }

    @Test
    public void testToString() {
        // arrange
        EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE);

        // act
        String str = sub.toString();

        // assert
        Assert.assertTrue(str.contains("EmbeddedTreeLineSubset[lineOrigin= "));
        Assert.assertTrue(str.contains(", lineDirection= "));
        Assert.assertTrue(str.contains(", region= "));
    }

    private static void checkFiniteSegment(LineConvexSubset segment, Vector2D start, Vector2D end) {
        Assert.assertFalse(segment.isInfinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }
}
