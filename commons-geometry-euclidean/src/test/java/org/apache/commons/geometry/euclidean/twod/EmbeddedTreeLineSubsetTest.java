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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class EmbeddedTreeLineSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Line DEFAULT_TEST_LINE =
            Lines.fromPointAndDirection(Vector2D.of(0, 1), Vector2D.Unit.PLUS_X, TEST_PRECISION);

    @Test
    public void testCtor_lineOnly() {
        // act
        final EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE);

        // assert
        Assertions.assertSame(DEFAULT_TEST_LINE, sub.getLine());
        Assertions.assertSame(TEST_PRECISION, sub.getPrecision());

        Assertions.assertFalse(sub.isFull());
        Assertions.assertTrue(sub.isEmpty());
        Assertions.assertFalse(sub.isInfinite());
        Assertions.assertTrue(sub.isFinite());

        Assertions.assertEquals(0, sub.getSize(), TEST_EPS);
        Assertions.assertNull(sub.getCentroid());
    }

    @Test
    public void testCtor_lineAndBoolean() {
        // act
        final EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE, true);

        // assert
        Assertions.assertSame(DEFAULT_TEST_LINE, sub.getLine());
        Assertions.assertSame(TEST_PRECISION, sub.getPrecision());

        Assertions.assertTrue(sub.isFull());
        Assertions.assertFalse(sub.isEmpty());
        Assertions.assertTrue(sub.isInfinite());
        Assertions.assertFalse(sub.isFinite());

        GeometryTestUtils.assertPositiveInfinity(sub.getSize());
        Assertions.assertNull(sub.getCentroid());
    }

    @Test
    public void testCtor_lineAndRegion() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.full();

        // act
        final EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE, tree);

        // assert
        Assertions.assertSame(DEFAULT_TEST_LINE, sub.getLine());
        Assertions.assertSame(tree, sub.getSubspaceRegion());
        Assertions.assertSame(TEST_PRECISION, sub.getPrecision());

        Assertions.assertTrue(sub.isFull());
        Assertions.assertFalse(sub.isEmpty());
        Assertions.assertTrue(sub.isInfinite());
        Assertions.assertFalse(sub.isFinite());

        GeometryTestUtils.assertPositiveInfinity(sub.getSize());
        Assertions.assertNull(sub.getCentroid());
    }

    @Test
    public void testToConvex_full() {
        // arrange
        final EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE, true);

        // act
        final List<LineConvexSubset> segments = sub.toConvex();

        // assert
        Assertions.assertEquals(1, segments.size());

        final LineConvexSubset seg = segments.get(0);
        Assertions.assertTrue(seg.isFull());
    }

    @Test
    public void testToConvex_empty() {
        // arrange
        final EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE, false);

        // act
        final List<LineConvexSubset> segments = sub.toConvex();

        // assert
        Assertions.assertEquals(0, segments.size());
    }

    @Test
    public void testToConvex_finiteAndInfiniteSegments() {
        // arrange
        final EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE, false);
        final RegionBSPTree1D tree = sub.getSubspaceRegion();
        tree.add(Interval.max(-2.0, TEST_PRECISION));
        tree.add(Interval.of(-1, 2, TEST_PRECISION));

        // act
        final List<LineConvexSubset> segments = sub.toConvex();

        // assert
        Assertions.assertEquals(2, segments.size());

        Assertions.assertNull(segments.get(0).getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), segments.get(1).getEndPoint(), TEST_EPS);
    }

    @Test
    public void testAdd_lineSegment() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        final Line otherLine = Lines.fromPointAndAngle(Vector2D.of(0, 1), 1e-11, TEST_PRECISION);

        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line);

        // act
        subset.add(Lines.subsetFromInterval(line, 2, 4));
        subset.add(Lines.subsetFromInterval(otherLine, 1, 3));
        subset.add(Lines.segmentFromPoints(Vector2D.of(-3, 1), Vector2D.of(-1, 1), TEST_PRECISION));

        // assert
        Assertions.assertFalse(subset.isFull());
        Assertions.assertFalse(subset.isEmpty());

        final List<LineConvexSubset> segments = subset.toConvex();

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), segments.get(0).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEndPoint(), TEST_EPS);

        Assertions.assertEquals(5, subset.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.7, 1), subset.getCentroid(), TEST_EPS);
    }

    @Test
    public void testAdd_subset() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        final EmbeddedTreeLineSubset a = new EmbeddedTreeLineSubset(line);
        final RegionBSPTree1D aTree = a.getSubspaceRegion();
        aTree.add(Interval.max(-3, TEST_PRECISION));
        aTree.add(Interval.of(1, 2, TEST_PRECISION));

        final EmbeddedTreeLineSubset b = new EmbeddedTreeLineSubset(line);
        final RegionBSPTree1D bTree = b.getSubspaceRegion();
        bTree.add(Interval.of(2, 4, TEST_PRECISION));
        bTree.add(Interval.of(-4, -2, TEST_PRECISION));

        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line);

        final int aTreeCount = aTree.count();
        final int bTreeCount = bTree.count();

        // act
        subset.add(a);
        subset.add(b);

        // assert
        Assertions.assertFalse(subset.isFull());
        Assertions.assertFalse(subset.isEmpty());

        final List<LineConvexSubset> segments = subset.toConvex();

        Assertions.assertEquals(2, segments.size());

        Assertions.assertNull(segments.get(0).getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEndPoint(), TEST_EPS);

        Assertions.assertEquals(aTreeCount, aTree.count());
        Assertions.assertEquals(bTreeCount, bTree.count());

        GeometryTestUtils.assertPositiveInfinity(subset.getSize());
        Assertions.assertNull(subset.getCentroid());
    }

    @Test
    public void testAdd_argumentsFromDifferentLine() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        final Line otherLine = Lines.fromPointAndAngle(Vector2D.of(0, 1), 1e-2, TEST_PRECISION);

        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> subset.add(Lines.subsetFromInterval(otherLine, 0, 1)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> subset.add(new EmbeddedTreeLineSubset(otherLine)));
    }

    @Test
    public void testGetBounds_noBounds() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(1, 0), 0.25 * Math.PI, TEST_PRECISION);

        final EmbeddedTreeLineSubset full = new EmbeddedTreeLineSubset(line, RegionBSPTree1D.full());
        final EmbeddedTreeLineSubset empty = new EmbeddedTreeLineSubset(line, RegionBSPTree1D.empty());
        final EmbeddedTreeLineSubset halfFull = new EmbeddedTreeLineSubset(line, Interval.min(1.0, TEST_PRECISION).toTree());

        // act/assert
        Assertions.assertNull(full.getBounds());
        Assertions.assertNull(empty.getBounds());
        Assertions.assertNull(halfFull.getBounds());
    }

    @Test
    public void testGetBounds_hasBounds() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, false);

        final double sqrt2 = Math.sqrt(2);
        subset.getSubspaceRegion().add(Interval.of(-2 * sqrt2, -sqrt2, TEST_PRECISION));
        subset.getSubspaceRegion().add(Interval.of(0, sqrt2, TEST_PRECISION));

        // act
        final Bounds2D bounds = subset.getBounds();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, -2), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testSplit_both_anglePositive() {
        // arrange
        final RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        final Line splitter = Lines.fromPointAndAngle(Vector2D.of(1, 0), 0.1 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final List<LineConvexSubset> minusSegments = split.getMinus().toConvex();
        Assertions.assertEquals(1, minusSegments.size());
        checkFiniteSegment(minusSegments.get(0), Vector2D.ZERO, Vector2D.of(1, 0));

        final List<LineConvexSubset> plusSegments = split.getPlus().toConvex();
        Assertions.assertEquals(2, plusSegments.size());
        checkFiniteSegment(plusSegments.get(0), Vector2D.of(1, 0), Vector2D.of(2, 0));
        checkFiniteSegment(plusSegments.get(1), Vector2D.of(3, 0), Vector2D.of(4, 0));
    }

    @Test
    public void testSplit_both_angleNegative() {
        // arrange
        final RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        final Line splitter = Lines.fromPointAndAngle(Vector2D.of(1, 0), -0.9 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final List<LineConvexSubset> minusSegments = split.getMinus().toConvex();
        Assertions.assertEquals(2, minusSegments.size());
        checkFiniteSegment(minusSegments.get(0), Vector2D.of(1, 0), Vector2D.of(2, 0));
        checkFiniteSegment(minusSegments.get(1), Vector2D.of(3, 0), Vector2D.of(4, 0));

        final List<LineConvexSubset> plusSegments = split.getPlus().toConvex();
        Assertions.assertEquals(1, plusSegments.size());
        checkFiniteSegment(plusSegments.get(0), Vector2D.ZERO, Vector2D.of(1, 0));
    }

    @Test
    public void testSplit_intersection_plusOnly() {
        // arrange
        final RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        final Line splitter = Lines.fromPointAndAngle(Vector2D.of(-1, 0), 0.1 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(subset, split.getPlus());
    }

    @Test
    public void testSplit_intersection_minusOnly() {
        // arrange
        final RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        final Line splitter = Lines.fromPointAndAngle(Vector2D.of(10, 0), 0.1 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assertions.assertSame(subset, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallel_plus() {
        // arrange
        final RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        final Line splitter = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act
        final Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(subset, split.getPlus());
    }

    @Test
    public void testSplit_parallel_minus() {
        // arrange
        final RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        final Line splitter = Lines.fromPointAndAngle(Vector2D.of(0, -1), 0.0, TEST_PRECISION);

        // act
        final Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assertions.assertSame(subset, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_coincident_sameDirection() {
        // arrange
        final RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        final Line splitter = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);

        // act
        final Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_coincident_oppositeDirection() {
        // arrange
        final RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(line, subRegion);

        final Line splitter = Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Split<EmbeddedTreeLineSubset> split = subset.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testTransform() {
        // arrange
        final AffineTransformMatrix2D mat = AffineTransformMatrix2D
                .createRotation(Vector2D.of(0, 1), PlaneAngleRadians.PI_OVER_TWO)
                .scale(Vector2D.of(3, 2));

        final EmbeddedTreeLineSubset subset = new EmbeddedTreeLineSubset(Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION));
        subset.getSubspaceRegion().add(Interval.of(0, 1, TEST_PRECISION));
        subset.getSubspaceRegion().add(Interval.min(3, TEST_PRECISION));

        // act
        final EmbeddedTreeLineSubset transformed = subset.transform(mat);

        // assert
        Assertions.assertNotSame(subset, transformed);

        final List<LineConvexSubset> originalSegments = subset.toConvex();
        Assertions.assertEquals(2, originalSegments.size());
        checkFiniteSegment(originalSegments.get(0), Vector2D.ZERO, Vector2D.Unit.PLUS_X);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 0), originalSegments.get(1).getStartPoint(), TEST_EPS);
        Assertions.assertNull(originalSegments.get(1).getEndPoint());

        final List<LineConvexSubset> transformedSegments = transformed.toConvex();
        Assertions.assertEquals(2, transformedSegments.size());
        checkFiniteSegment(transformedSegments.get(0), Vector2D.of(3, 2), Vector2D.of(3, 4));
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 8), transformedSegments.get(1).getStartPoint(), TEST_EPS);
        Assertions.assertNull(transformedSegments.get(1).getEndPoint());
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        final AffineTransformMatrix2D mat = AffineTransformMatrix2D.createScale(Vector2D.of(-1, 2));

        final EmbeddedTreeLineSubset subset =
                new EmbeddedTreeLineSubset(Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION));
        subset.getSubspaceRegion().add(Interval.of(0, 1, TEST_PRECISION));

        // act
        final EmbeddedTreeLineSubset transformed = subset.transform(mat);

        // assert
        Assertions.assertNotSame(subset, transformed);

        final List<LineConvexSubset> originalSegments = subset.toConvex();
        Assertions.assertEquals(1, originalSegments.size());
        checkFiniteSegment(originalSegments.get(0), Vector2D.of(0, 1), Vector2D.of(1, 1));

        final List<LineConvexSubset> transformedSegments = transformed.toConvex();
        Assertions.assertEquals(1, transformedSegments.size());
        checkFiniteSegment(transformedSegments.get(0), Vector2D.of(0, 2), Vector2D.of(-1, 2));
    }

    @Test
    public void testToString() {
        // arrange
        final EmbeddedTreeLineSubset sub = new EmbeddedTreeLineSubset(DEFAULT_TEST_LINE);

        // act
        final String str = sub.toString();

        // assert
        Assertions.assertTrue(str.contains("EmbeddedTreeLineSubset[lineOrigin= "));
        Assertions.assertTrue(str.contains(", lineDirection= "));
        Assertions.assertTrue(str.contains(", region= "));
    }

    private static void checkFiniteSegment(final LineConvexSubset segment, final Vector2D start, final Vector2D end) {
        Assertions.assertFalse(segment.isInfinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }
}
