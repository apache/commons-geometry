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
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTreeSubLine.Builder;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class RegionBSPTreeSubLineTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Line DEFAULT_TEST_LINE =
            Line.fromPointAndDirection(Vector2D.of(0, 1), Vector2D.Unit.PLUS_X, TEST_PRECISION);

    @Test
    public void testCtor_lineOnly() {
        // act
        RegionBSPTreeSubLine sub = new RegionBSPTreeSubLine(DEFAULT_TEST_LINE);

        // assert
        Assert.assertSame(DEFAULT_TEST_LINE, sub.getLine());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());

        Assert.assertFalse(sub.isFull());
        Assert.assertTrue(sub.isEmpty());
        Assert.assertFalse(sub.isInfinite());
        Assert.assertTrue(sub.isFinite());
    }

    @Test
    public void testCtor_lineAndBoolean() {
        // act
        RegionBSPTreeSubLine sub = new RegionBSPTreeSubLine(DEFAULT_TEST_LINE, true);

        // assert
        Assert.assertSame(DEFAULT_TEST_LINE, sub.getLine());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());

        Assert.assertTrue(sub.isFull());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertTrue(sub.isInfinite());
        Assert.assertFalse(sub.isFinite());
    }

    @Test
    public void testCtor_lineAndRegion() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.full();

        // act
        RegionBSPTreeSubLine sub = new RegionBSPTreeSubLine(DEFAULT_TEST_LINE, tree);

        // assert
        Assert.assertSame(DEFAULT_TEST_LINE, sub.getLine());
        Assert.assertSame(tree, sub.getSubspaceRegion());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());

        Assert.assertTrue(sub.isFull());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertTrue(sub.isInfinite());
        Assert.assertFalse(sub.isFinite());
    }

    @Test
    public void testToConvex_full() {
        // arrange
        RegionBSPTreeSubLine sub = new RegionBSPTreeSubLine(DEFAULT_TEST_LINE, true);

        // act
        List<ConvexSubLine> segments = sub.toConvex();

        // assert
        Assert.assertEquals(1, segments.size());

        ConvexSubLine seg = segments.get(0);
        Assert.assertTrue(seg.isFull());
    }

    @Test
    public void testToConvex_empty() {
        // arrange
        RegionBSPTreeSubLine sub = new RegionBSPTreeSubLine(DEFAULT_TEST_LINE, false);

        // act
        List<ConvexSubLine> segments = sub.toConvex();

        // assert
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testToConvex_finiteAndInfiniteSegments() {
        // arrange
        RegionBSPTreeSubLine sub = new RegionBSPTreeSubLine(DEFAULT_TEST_LINE, false);
        RegionBSPTree1D tree = sub.getSubspaceRegion();
        tree.add(Interval.max(-2.0, TEST_PRECISION));
        tree.add(Interval.of(-1, 2, TEST_PRECISION));

        // act
        List<ConvexSubLine> segments = sub.toConvex();

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
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line otherLine = Line.fromPointAndAngle(Vector2D.of(0, 1), 1e-11, TEST_PRECISION);

        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(line);

        // act
        subline.add(ConvexSubLine.fromInterval(line, 2, 4));
        subline.add(ConvexSubLine.fromInterval(otherLine, 1, 3));
        subline.add(Segment.fromPoints(Vector2D.of(-4, 1), Vector2D.of(-1, 1), TEST_PRECISION));

        // assert
        Assert.assertFalse(subline.isFull());
        Assert.assertFalse(subline.isEmpty());

        List<ConvexSubLine> segments = subline.toConvex();

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-4, 1), segments.get(0).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEndPoint(), TEST_EPS);
    }

    @Test
    public void testAdd_subLine() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        RegionBSPTreeSubLine a = new RegionBSPTreeSubLine(line);
        RegionBSPTree1D aTree = a.getSubspaceRegion();
        aTree.add(Interval.max(-3, TEST_PRECISION));
        aTree.add(Interval.of(1, 2, TEST_PRECISION));

        RegionBSPTreeSubLine b = new RegionBSPTreeSubLine(line);
        RegionBSPTree1D bTree = b.getSubspaceRegion();
        bTree.add(Interval.of(2, 4, TEST_PRECISION));
        bTree.add(Interval.of(-4, -2, TEST_PRECISION));

        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(line);

        int aTreeCount = aTree.count();
        int bTreeCount = bTree.count();

        // act
        subline.add(a);
        subline.add(b);

        // assert
        Assert.assertFalse(subline.isFull());
        Assert.assertFalse(subline.isEmpty());

        List<ConvexSubLine> segments = subline.toConvex();

        Assert.assertEquals(2, segments.size());

        Assert.assertNull(segments.get(0).getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEndPoint(), TEST_EPS);

        Assert.assertEquals(aTreeCount, aTree.count());
        Assert.assertEquals(bTreeCount, bTree.count());
    }

    @Test
    public void testAdd_argumentsFromDifferentLine() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line otherLine = Line.fromPointAndAngle(Vector2D.of(0, 1), 1e-2, TEST_PRECISION);

        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(line);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            subline.add(ConvexSubLine.fromInterval(otherLine, 0, 1));
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            subline.add(new RegionBSPTreeSubLine(otherLine));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testSplit_both_anglePositive() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Line.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(line, subRegion);

        Line splitter = Line.fromPointAndAngle(Vector2D.of(1, 0), 0.1 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Split<RegionBSPTreeSubLine> split = subline.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        List<ConvexSubLine> minusSegments = split.getMinus().toConvex();
        Assert.assertEquals(1, minusSegments.size());
        checkFiniteSegment(minusSegments.get(0), Vector2D.ZERO, Vector2D.of(1, 0));

        List<ConvexSubLine> plusSegments = split.getPlus().toConvex();
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

        Line line = Line.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(line, subRegion);

        Line splitter = Line.fromPointAndAngle(Vector2D.of(1, 0), -0.9 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Split<RegionBSPTreeSubLine> split = subline.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        List<ConvexSubLine> minusSegments = split.getMinus().toConvex();
        Assert.assertEquals(2, minusSegments.size());
        checkFiniteSegment(minusSegments.get(0), Vector2D.of(1, 0), Vector2D.of(2, 0));
        checkFiniteSegment(minusSegments.get(1), Vector2D.of(3, 0), Vector2D.of(4, 0));

        List<ConvexSubLine> plusSegments = split.getPlus().toConvex();
        Assert.assertEquals(1, plusSegments.size());
        checkFiniteSegment(plusSegments.get(0), Vector2D.ZERO, Vector2D.of(1, 0));
    }

    @Test
    public void testSplit_intersection_plusOnly() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Line.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(line, subRegion);

        Line splitter = Line.fromPointAndAngle(Vector2D.of(-1, 0), 0.1 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Split<RegionBSPTreeSubLine> split = subline.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(subline, split.getPlus());
    }

    @Test
    public void testSplit_intersection_minusOnly() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Line.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(line, subRegion);

        Line splitter = Line.fromPointAndAngle(Vector2D.of(10, 0), 0.1 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Split<RegionBSPTreeSubLine> split = subline.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(subline, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallel_plus() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Line.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(line, subRegion);

        Line splitter = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act
        Split<RegionBSPTreeSubLine> split = subline.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(subline, split.getPlus());
    }

    @Test
    public void testSplit_parallel_minus() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Line.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(line, subRegion);

        Line splitter = Line.fromPointAndAngle(Vector2D.of(0, -1), 0.0, TEST_PRECISION);

        // act
        Split<RegionBSPTreeSubLine> split = subline.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(subline, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_coincident_sameDirection() {
        // arrange
        RegionBSPTree1D subRegion = RegionBSPTree1D.empty();
        subRegion.add(Interval.of(0,  2, TEST_PRECISION));
        subRegion.add(Interval.of(3,  4, TEST_PRECISION));

        Line line = Line.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(line, subRegion);

        Line splitter = Line.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);

        // act
        Split<RegionBSPTreeSubLine> split = subline.split(splitter);

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

        Line line = Line.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(line, subRegion);

        Line splitter = Line.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Split<RegionBSPTreeSubLine> split = subline.split(splitter);

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

        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(Line.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION));
        subline.getSubspaceRegion().add(Interval.of(0, 1, TEST_PRECISION));
        subline.getSubspaceRegion().add(Interval.min(3, TEST_PRECISION));

        // act
        RegionBSPTreeSubLine transformed = subline.transform(mat);

        // assert
        Assert.assertNotSame(subline, transformed);

        List<ConvexSubLine> originalSegments = subline.toConvex();
        Assert.assertEquals(2, originalSegments.size());
        checkFiniteSegment(originalSegments.get(0), Vector2D.ZERO, Vector2D.Unit.PLUS_X);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 0), originalSegments.get(1).getStartPoint(), TEST_EPS);
        Assert.assertNull(originalSegments.get(1).getEndPoint());

        List<ConvexSubLine> transformedSegments = transformed.toConvex();
        Assert.assertEquals(2, transformedSegments.size());
        checkFiniteSegment(transformedSegments.get(0), Vector2D.of(3, 2), Vector2D.of(3, 4));
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 8), transformedSegments.get(1).getStartPoint(), TEST_EPS);
        Assert.assertNull(transformedSegments.get(1).getEndPoint());
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        AffineTransformMatrix2D mat = AffineTransformMatrix2D.createScale(Vector2D.of(-1, 2));

        RegionBSPTreeSubLine subline = new RegionBSPTreeSubLine(Line.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION));
        subline.getSubspaceRegion().add(Interval.of(0, 1, TEST_PRECISION));

        // act
        RegionBSPTreeSubLine transformed = subline.transform(mat);

        // assert
        Assert.assertNotSame(subline, transformed);

        List<ConvexSubLine> originalSegments = subline.toConvex();
        Assert.assertEquals(1, originalSegments.size());
        checkFiniteSegment(originalSegments.get(0), Vector2D.of(0, 1), Vector2D.of(1, 1));

        List<ConvexSubLine> transformedSegments = transformed.toConvex();
        Assert.assertEquals(1, transformedSegments.size());
        checkFiniteSegment(transformedSegments.get(0), Vector2D.of(0, 2), Vector2D.of(-1, 2));
    }

    @Test
    public void testBuilder_instanceMethod() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Builder builder = new RegionBSPTreeSubLine(line).builder();

        // act
        RegionBSPTreeSubLine subline = builder.build();

        // assert
        Assert.assertFalse(subline.isFull());
        Assert.assertTrue(subline.isEmpty());

        List<ConvexSubLine> segments = subline.toConvex();
        Assert.assertEquals(0, segments.size());

        Assert.assertSame(line, subline.getLine());
        Assert.assertSame(line, subline.getHyperplane());
        Assert.assertSame(TEST_PRECISION, subline.getPrecision());
    }

    @Test
    public void testBuilder_createEmpty() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        Builder builder = new Builder(line);

        // act
        RegionBSPTreeSubLine subline = builder.build();

        // assert
        Assert.assertFalse(subline.isFull());
        Assert.assertTrue(subline.isEmpty());

        List<ConvexSubLine> segments = subline.toConvex();
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testBuilder_addConvex() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line otherLine = Line.fromPointAndAngle(Vector2D.of(0, 1), 1e-11, TEST_PRECISION);

        Builder builder = new Builder(line);

        // act
        builder.add(ConvexSubLine.fromInterval(line, 2, 4));
        builder.add(ConvexSubLine.fromInterval(otherLine, 1, 3));
        builder.add(Segment.fromPoints(Vector2D.of(-4, 1), Vector2D.of(-1, 1), TEST_PRECISION));

        RegionBSPTreeSubLine subline = builder.build();

        // assert
        Assert.assertFalse(subline.isFull());
        Assert.assertFalse(subline.isEmpty());

        List<ConvexSubLine> segments = subline.toConvex();

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-4, 1), segments.get(0).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEndPoint(), TEST_EPS);
    }

    @Test
    public void testBuilder_addNonConvex() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        RegionBSPTreeSubLine a = new RegionBSPTreeSubLine(line);
        RegionBSPTree1D aTree = a.getSubspaceRegion();
        aTree.add(Interval.max(-3, TEST_PRECISION));
        aTree.add(Interval.of(1, 2, TEST_PRECISION));

        RegionBSPTreeSubLine b = new RegionBSPTreeSubLine(line);
        RegionBSPTree1D bTree = b.getSubspaceRegion();
        bTree.add(Interval.of(2, 4, TEST_PRECISION));
        bTree.add(Interval.of(-4, -2, TEST_PRECISION));

        Builder builder = new Builder(line);

        int aTreeCount = aTree.count();
        int bTreeCount = bTree.count();

        // act
        builder.add(a);
        builder.add(b);

        RegionBSPTreeSubLine subline = builder.build();

        // assert
        Assert.assertFalse(subline.isFull());
        Assert.assertFalse(subline.isEmpty());

        List<ConvexSubLine> segments = subline.toConvex();

        Assert.assertEquals(2, segments.size());

        Assert.assertNull(segments.get(0).getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEndPoint(), TEST_EPS);

        Assert.assertEquals(aTreeCount, aTree.count());
        Assert.assertEquals(bTreeCount, bTree.count());
    }

    @Test
    public void testBuilder_argumentsFromDifferentLine() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line otherLine = Line.fromPointAndAngle(Vector2D.of(0, 1), 1e-2, TEST_PRECISION);

        Builder builder = new Builder(line);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add(ConvexSubLine.fromInterval(otherLine, 0, 1));
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            builder.add(new RegionBSPTreeSubLine(otherLine));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_unknownSubLineType() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        SubLine unknownType = new SubLine(line) {
            @Override
            public boolean isInfinite() {
                return false;
            }

            @Override
            public boolean isFinite() {
                return true;
            }

            @Override
            public List<? extends ConvexSubHyperplane<Vector2D>> toConvex() {
                return null;
            }

            @Override
            public HyperplaneBoundedRegion<Vector1D> getSubspaceRegion() {
                return null;
            }

            @Override
            public Split<? extends SubHyperplane<Vector2D>> split(Hyperplane<Vector2D> splitter) {
                return null;
            }

            @Override
            public SubHyperplane<Vector2D> transform(Transform<Vector2D> transform) {
                return null;
            }

            @Override
            public Vector2D closest(Vector2D point) {
                return null;
            }

            @Override
            public boolean isFull() {
                return false;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public double getSize() {
                return 0;
            }

            @Override
            RegionLocation classifyAbscissa(double abscissa) {
                return null;
            }
        };

        Builder builder = new Builder(line);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add(unknownType);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testToString() {
        // arrange
        RegionBSPTreeSubLine sub = new RegionBSPTreeSubLine(DEFAULT_TEST_LINE);

        // act
        String str = sub.toString();

        // assert
        Assert.assertTrue(str.contains("SubLine[lineOrigin= "));
        Assert.assertTrue(str.contains(", lineDirection= "));
        Assert.assertTrue(str.contains(", region= "));
    }

    private static void checkFiniteSegment(ConvexSubLine segment, Vector2D start, Vector2D end) {
        Assert.assertFalse(segment.isInfinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }
}
