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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class ConvexAreaTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFull() {
        // act
        ConvexArea area = ConvexArea.full();

        // assert
        Assert.assertTrue(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(0.0, area.getBoundarySize(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assert.assertNull(area.getBarycenter());
        Assert.assertNull(area.getBounds());
    }

    @Test
    public void testBoundaryStream() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION);
        ConvexArea area = ConvexArea.fromBounds(line);

        // act
        List<LineConvexSubset> segments = area.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(1, segments.size());
        LineConvexSubset segment = segments.get(0);
        Assert.assertNull(segment.getStartPoint());
        Assert.assertNull(segment.getEndPoint());
        Assert.assertSame(line, segment.getLine());
    }

    @Test
    public void testBoundaryStream_full() {
        // arrange
        ConvexArea area = ConvexArea.full();

        // act
        List<LineConvexSubset> segments = area.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testToTree() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(1, 0), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(1, 1), PlaneAngleRadians.PI, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(0, 1), -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
                );

        // act
        RegionBSPTree2D tree = area.toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(1, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), tree.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testToTree_full() {
        // arrange
        ConvexArea area = ConvexArea.full();

        // act
        RegionBSPTree2D tree = area.toTree();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
    }

    @Test
    public void testTransform_full() {
        // arrange
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(3);
        ConvexArea area = ConvexArea.full();

        // act
        ConvexArea transformed = area.transform(transform);

        // assert
        Assert.assertSame(area, transformed);
    }

    @Test
    public void testTransform_infinite() {
        // arrange
        AffineTransformMatrix2D mat = AffineTransformMatrix2D
                .createRotation(Vector2D.of(0, 1), PlaneAngleRadians.PI_OVER_TWO)
                .scale(Vector2D.of(3, 2));

        ConvexArea area = ConvexArea.fromBounds(
                Lines.fromPointAndAngle(Vector2D.ZERO, 0.25 * PlaneAngleRadians.PI, TEST_PRECISION),
                Lines.fromPointAndAngle(Vector2D.ZERO, -0.25 * PlaneAngleRadians.PI, TEST_PRECISION));

        // act
        ConvexArea transformed = area.transform(mat);

        // assert
        Assert.assertNotSame(area, transformed);

        List<LinePath> paths = transformed.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        List<LineConvexSubset> segments = paths.get(0).getElements();
        Assert.assertEquals(2, segments.size());

        LineConvexSubset firstSegment = segments.get(0);
        Assert.assertNull(firstSegment.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 2), firstSegment.getEndPoint(), TEST_EPS);
        Assert.assertEquals(Math.atan2(2, 3), firstSegment.getLine().getAngle(), TEST_EPS);

        LineConvexSubset secondSegment = segments.get(1);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 2), secondSegment.getStartPoint(), TEST_EPS);
        Assert.assertNull(secondSegment.getEndPoint());
        Assert.assertEquals(Math.atan2(2, -3), secondSegment.getLine().getAngle(), TEST_EPS);
    }

    @Test
    public void testTransform_finite() {
        // arrange
        AffineTransformMatrix2D mat = AffineTransformMatrix2D.createScale(Vector2D.of(1, 2));

        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 1), Vector2D.of(2, 1),
                    Vector2D.of(2, 2), Vector2D.of(1, 2)
                ), TEST_PRECISION);

        // act
        ConvexArea transformed = area.transform(mat);

        // assert
        Assert.assertNotSame(area, transformed);

        List<LineConvexSubset> segments = transformed.getBoundaries();
        Assert.assertEquals(4, segments.size());

        Assert.assertEquals(2, transformed.getSize(), TEST_EPS);
        Assert.assertEquals(6, transformed.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 3), transformed.getBarycenter(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.BOUNDARY,
                Vector2D.of(1, 2), Vector2D.of(2, 2), Vector2D.of(2, 4), Vector2D.of(1, 4));
        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.INSIDE, transformed.getBarycenter());
    }

    @Test
    public void testTransform_finite_withSingleReflection() {
        // arrange
        AffineTransformMatrix2D mat = AffineTransformMatrix2D.createScale(Vector2D.of(-1, 2));

        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 1), Vector2D.of(2, 1),
                    Vector2D.of(2, 2), Vector2D.of(1, 2)
                ), TEST_PRECISION);

        // act
        ConvexArea transformed = area.transform(mat);

        // assert
        Assert.assertNotSame(area, transformed);

        List<LineConvexSubset> segments = transformed.getBoundaries();
        Assert.assertEquals(4, segments.size());

        Assert.assertEquals(2, transformed.getSize(), TEST_EPS);
        Assert.assertEquals(6, transformed.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1.5, 3), transformed.getBarycenter(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.BOUNDARY,
                Vector2D.of(-1, 2), Vector2D.of(-2, 2), Vector2D.of(-2, 4), Vector2D.of(-1, 4));
        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.INSIDE, transformed.getBarycenter());
    }

    @Test
    public void testTransform_finite_withDoubleReflection() {
        // arrange
        AffineTransformMatrix2D mat = AffineTransformMatrix2D.createScale(Vector2D.of(-1, -2));

        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 1), Vector2D.of(2, 1),
                    Vector2D.of(2, 2), Vector2D.of(1, 2)
                ), TEST_PRECISION);

        // act
        ConvexArea transformed = area.transform(mat);

        // assert
        Assert.assertNotSame(area, transformed);

        List<LineConvexSubset> segments = transformed.getBoundaries();
        Assert.assertEquals(4, segments.size());

        Assert.assertEquals(2, transformed.getSize(), TEST_EPS);
        Assert.assertEquals(6, transformed.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1.5, -3), transformed.getBarycenter(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.BOUNDARY,
                Vector2D.of(-1, -2), Vector2D.of(-2, -2), Vector2D.of(-2, -4), Vector2D.of(-1, -4));
        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.INSIDE, transformed.getBarycenter());
    }

    @Test
    public void testGetVertices_full() {
        // arrange
        ConvexArea area = ConvexArea.full();

        // act/assert
        Assert.assertEquals(0, area.getVertices().size());
    }

    @Test
    public void testGetVertices_twoParallelLines() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(0, -1), 0.0, TEST_PRECISION)
                );

        // act/assert
        Assert.assertEquals(0, area.getVertices().size());
    }

    @Test
    public void testGetVertices_infiniteWithVertices() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(0, -1), 0.0, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(1, 0), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
                );

        // act
        List<Vector2D> vertices = area.getVertices();

        // assert
        Assert.assertEquals(2, vertices.size());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -1), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), vertices.get(1), TEST_EPS);
    }

    @Test
    public void testGetVertices_finite() {
        // arrange
        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.Unit.PLUS_X,
                    Vector2D.Unit.PLUS_Y
                ), TEST_PRECISION);

        // act
        List<Vector2D> vertices = area.getVertices();

        // assert
        Assert.assertEquals(3, vertices.size());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_X, vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_Y, vertices.get(2), TEST_EPS);
    }

    @Test
    public void testGetBounds_infinite() {
        // act/assert
        Assert.assertNull(ConvexArea.full().getBounds());
        Assert.assertNull(ConvexArea.fromBounds(
                Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)).getBounds());
    }

    @Test
    public void testGetBounds_square() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(-1, -1), 2, 1));

        // act
        Bounds2D bounds = area.getBounds();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, -1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 0), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testProject_full() {
        // arrange
        ConvexArea area = ConvexArea.full();

        // act/assert
        Assert.assertNull(area.project(Vector2D.ZERO));
        Assert.assertNull(area.project(Vector2D.Unit.PLUS_X));
    }

    @Test
    public void testProject_halfSpace() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(
                Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 1), area.project(Vector2D.of(1, 1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2), area.project(Vector2D.of(-2, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -3), area.project(Vector2D.of(1, -3)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -4), area.project(Vector2D.of(-2, -4)), TEST_EPS);
    }

    @Test
    public void testProject_square() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.ZERO, 1, 1));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), area.project(Vector2D.of(1, 1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), area.project(Vector2D.of(2, 2)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, area.project(Vector2D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, area.project(Vector2D.of(-1, -1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 0.5), area.project(Vector2D.of(0.1, 0.5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.2, 1), area.project(Vector2D.of(0.2, 0.9)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0), area.project(Vector2D.of(0.5, 0.5)), TEST_EPS);
    }

    @Test
    public void testTrim_full() {
        // arrange
        ConvexArea area = ConvexArea.full();
        Segment segment = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        LineConvexSubset trimmed = area.trim(segment);

        // assert
        Assert.assertSame(segment, trimmed);
    }

    @Test
    public void testTrim_halfSpace() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION));
        LineConvexSubset segment = Lines.fromPoints(Vector2D.Unit.MINUS_Y, Vector2D.Unit.PLUS_Y, TEST_PRECISION).span();

        // act
        LineConvexSubset trimmed = area.trim(segment);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, trimmed.getStartPoint(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(trimmed.getSubspaceEnd());
    }

    @Test
    public void testTrim_square() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.ZERO, 1, 1));
        LineConvexSubset segment = Lines.fromPoints(Vector2D.of(0.5, 0), Vector2D.of(0.5, 1), TEST_PRECISION).span();

        // act
        LineConvexSubset trimmed = area.trim(segment);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0), trimmed.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 1), trimmed.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testTrim_segmentOutsideOfRegion() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.ZERO, 1, 1));
        LineConvexSubset segment = Lines.fromPoints(Vector2D.of(-0.5, 0), Vector2D.of(-0.5, 1), TEST_PRECISION).span();

        // act
        LineConvexSubset trimmed = area.trim(segment);

        // assert
        Assert.assertNull(trimmed);
    }

    @Test
    public void testTrim_segmentDirectlyOnBoundaryOfRegion() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.ZERO, 1, 1));
        LineConvexSubset segment = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION).span();

        // act
        LineConvexSubset trimmed = area.trim(segment);

        // assert
        Assert.assertNull(trimmed);
    }

    @Test
    public void testSplit_full() {
        // arrange
        ConvexArea input = ConvexArea.full();

        Line splitter = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);

        // act
        Split<ConvexArea> split = input.split(splitter);

        // act
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        ConvexArea minus = split.getMinus();
        Assert.assertFalse(minus.isFull());
        Assert.assertFalse(minus.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(minus.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(minus.getSize());
        Assert.assertNull(minus.getBarycenter());

        List<LineConvexSubset> minusSegments = minus.getBoundaries();
        Assert.assertEquals(1, minusSegments.size());
        Assert.assertEquals(splitter, minusSegments.get(0).getLine());

        ConvexArea plus = split.getPlus();
        Assert.assertFalse(plus.isFull());
        Assert.assertFalse(plus.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(plus.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(plus.getSize());
        Assert.assertNull(plus.getBarycenter());

        List<LineConvexSubset> plusSegments = plus.getBoundaries();
        Assert.assertEquals(1, plusSegments.size());
        Assert.assertEquals(splitter, plusSegments.get(0).getLine().reverse());
    }

    @Test
    public void testSplit_halfSpace_split() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));
        Line splitter = Lines.fromPointAndAngle(Vector2D.ZERO, 0.25 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        ConvexArea minus = split.getMinus();
        Assert.assertFalse(minus.isFull());
        Assert.assertFalse(minus.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(minus.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(minus.getSize());
        Assert.assertNull(minus.getBarycenter());

        Assert.assertEquals(2, minus.getBoundaries().size());

        ConvexArea plus = split.getPlus();
        Assert.assertFalse(plus.isFull());
        Assert.assertFalse(plus.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(plus.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(plus.getSize());
        Assert.assertNull(plus.getBarycenter());

        Assert.assertEquals(2, plus.getBoundaries().size());
    }

    @Test
    public void testSplit_halfSpace_splitOnBoundary() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));
        Line splitter = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(area, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_halfSpace_splitOnBoundaryWithReversedSplitter() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));
        Line splitter = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).reverse();

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(area, split.getPlus());
    }

    @Test
    public void testSplit_square_split() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 2, 1));
        Line splitter = Lines.fromPointAndAngle(Vector2D.of(2, 1), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        ConvexArea minus = split.getMinus();
        Assert.assertFalse(minus.isFull());
        Assert.assertFalse(minus.isEmpty());

        Assert.assertEquals(4, minus.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(1, minus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 1.5), minus.getBarycenter(), TEST_EPS);

        Assert.assertEquals(4, minus.getBoundaries().size());

        ConvexArea plus = split.getPlus();
        Assert.assertFalse(plus.isFull());
        Assert.assertFalse(plus.isEmpty());

        Assert.assertEquals(4, plus.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(1, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2.5, 1.5), plus.getBarycenter(), TEST_EPS);

        Assert.assertEquals(4, plus.getBoundaries().size());
    }

    @Test
    public void testSplit_square_splitOnVertices() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        Line splitter = Lines.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION);

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        ConvexArea minus = split.getMinus();
        Assert.assertFalse(minus.isFull());
        Assert.assertFalse(minus.isEmpty());

        Assert.assertEquals(2 + Math.sqrt(2), minus.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.5, minus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4.0 / 3.0, 5.0 / 3.0), minus.getBarycenter(), TEST_EPS);

        Assert.assertEquals(3, minus.getBoundaries().size());

        ConvexArea plus = split.getPlus();
        Assert.assertFalse(plus.isFull());
        Assert.assertFalse(plus.isEmpty());

        Assert.assertEquals(2 + Math.sqrt(2), plus.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.5, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(5.0 / 3.0, 4.0 / 3.0), plus.getBarycenter(), TEST_EPS);

        Assert.assertEquals(3, plus.getBoundaries().size());
    }

    @Test
    public void testSplit_square_splitOnVerticesWithReversedSplitter() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        Line splitter = Lines.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).reverse();

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        ConvexArea minus = split.getMinus();
        Assert.assertFalse(minus.isFull());
        Assert.assertFalse(minus.isEmpty());

        Assert.assertEquals(2 + Math.sqrt(2), minus.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.5, minus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(5.0 / 3.0, 4.0 / 3.0), minus.getBarycenter(), TEST_EPS);

        Assert.assertEquals(3, minus.getBoundaries().size());

        ConvexArea plus = split.getPlus();
        Assert.assertFalse(plus.isFull());
        Assert.assertFalse(plus.isEmpty());

        Assert.assertEquals(2 + Math.sqrt(2), plus.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.5, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4.0 / 3.0, 5.0 / 3.0), plus.getBarycenter(), TEST_EPS);

        Assert.assertEquals(3, plus.getBoundaries().size());
    }

    @Test
    public void testSplit_square_entirelyOnMinus() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        Line splitter = Lines.fromPoints(Vector2D.of(3, 1), Vector2D.of(3, 2), TEST_PRECISION);

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());
        Assert.assertSame(area, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_square_onMinusBoundary() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        Line splitter = Lines.fromPoints(Vector2D.of(2, 1), Vector2D.of(2, 2), TEST_PRECISION);

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());
        Assert.assertSame(area, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_square_entirelyOnPlus() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        Line splitter = Lines.fromPoints(Vector2D.of(0, 1), Vector2D.of(0, 2), TEST_PRECISION);

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());
        Assert.assertNull(split.getMinus());
        Assert.assertSame(area, split.getPlus());
    }

    @Test
    public void testSplit_square_onPlusBoundary() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        Line splitter = Lines.fromPoints(Vector2D.of(1, 1), Vector2D.of(1, 2), TEST_PRECISION);

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());
        Assert.assertNull(split.getMinus());
        Assert.assertSame(area, split.getPlus());
    }

    @Test
    public void testSplit_fannedLines() {
        // arrange
        Line a = Lines.fromPointAndDirection(
                Vector2D.of(0.00600526260605261, -0.3392565140336253),
                Vector2D.of(0.9998433697734339, 0.017698472253402094), TEST_PRECISION);
        Line b = Lines.fromPointAndDirection(
                Vector2D.of(-0.05020576603061953, 1.7524758059156824),
                Vector2D.of(0.9995898847600798, 0.02863672965494457), TEST_PRECISION);

        ConvexArea area = ConvexArea.fromBounds(a, b.reverse());

        Line splitter = Lines.fromPointAndDirection(
                Vector2D.of(0.01581855191043128, -2.5270731411451215),
                Vector2D.of(0.999980409069402, 0.006259510954681248), TEST_PRECISION);

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());
        Assert.assertSame(area, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testLinecast_full() {
        // arrange
        ConvexArea area = ConvexArea.full();

        // act/assert
        LinecastChecker2D.with(area)
            .expectNothing()
            .whenGiven(Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));

        LinecastChecker2D.with(area)
            .expectNothing()
            .whenGiven(Lines.segmentFromPoints(Vector2D.Unit.MINUS_X, Vector2D.Unit.PLUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast() {
        // arrange
        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO, Vector2D.of(1, 0),
                    Vector2D.of(1, 1), Vector2D.of(0, 1)
                ), TEST_PRECISION);

        // act/assert
        LinecastChecker2D.with(area)
            .expectNothing()
            .whenGiven(Lines.fromPoints(Vector2D.of(0, 5), Vector2D.of(1, 6), TEST_PRECISION));

        LinecastChecker2D.with(area)
            .expect(Vector2D.ZERO, Vector2D.Unit.MINUS_X)
            .and(Vector2D.ZERO, Vector2D.Unit.MINUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Lines.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION));

        LinecastChecker2D.with(area)
            .expect(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Lines.segmentFromPoints(Vector2D.of(0.5, 0.5), Vector2D.of(1, 1), TEST_PRECISION));
    }

    @Test
    public void testToString() {
        // arrange
        ConvexArea area = ConvexArea.full();

        // act
        String str = area.toString();

        // assert
        Assert.assertTrue(str.contains("ConvexArea"));
        Assert.assertTrue(str.contains("boundaries= "));
    }

    @Test
    public void testConvexPolygonFromVertices_notEnoughUniqueVertices() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        Pattern unclosedPattern = Pattern.compile("Cannot construct convex polygon from unclosed path.*");
        Pattern notEnoughElementsPattern =
                Pattern.compile("Cannot construct convex polygon from path with less than 3 elements.*");
        Pattern nonConvexPattern = Pattern.compile("Cannot construct convex polygon from non-convex path.*");

        Pattern singleVertexPattern =
                Pattern.compile("Unable to create line path; only a single unique vertex provided.*");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromVertices(Arrays.asList(), precision);
        }, IllegalArgumentException.class, unclosedPattern);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromVertices(Arrays.asList(Vector2D.ZERO), precision);
        }, IllegalStateException.class, singleVertexPattern);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromVertices(Arrays.asList(Vector2D.ZERO, Vector2D.of(1e-4, 1e-4)), precision);
        }, IllegalStateException.class, singleVertexPattern);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromVertices(Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X), precision);
        }, IllegalArgumentException.class, notEnoughElementsPattern);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromVertices(
                    Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, 1e-4)), precision);
        }, IllegalArgumentException.class, notEnoughElementsPattern);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromVertices(
                    Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, -1)), precision);
        }, IllegalArgumentException.class, nonConvexPattern);
    }

    @Test
    public void testConvexPolygonFromVertices_triangle() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 2);
        Vector2D p1 = Vector2D.of(2, 2);
        Vector2D p2 = Vector2D.of(2, 3);

        // act
        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(p0, p1, p2), TEST_PRECISION);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(0.5, area.getSize(), TEST_EPS);
        Assert.assertEquals(2 + Math.sqrt(2), area.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.centroid(p0, p1, p2), area.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testConvexPolygonFromVertices_square_closeRequired() {
        // act
        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.Unit.PLUS_X,
                    Vector2D.of(1, 1),
                    Vector2D.of(0, 1)
                ), TEST_PRECISION);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(1, area.getSize(), TEST_EPS);
        Assert.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testConvexPolygonFromVertices_square_closeNotRequired() {
        // act
        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.Unit.PLUS_X,
                    Vector2D.of(1, 1),
                    Vector2D.of(0, 1),
                    Vector2D.ZERO
                ), TEST_PRECISION);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(1, area.getSize(), TEST_EPS);
        Assert.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testConvexPolygonFromVertices_handlesDuplicatePoints() {
        // arrange
        double eps = 1e-3;
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        // act
        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.of(1e-4, 1e-4),
                    Vector2D.Unit.PLUS_X,
                    Vector2D.of(1, 1e-4),
                    Vector2D.of(1, 1),
                    Vector2D.of(0, 1),
                    Vector2D.of(1e-4, 1),
                    Vector2D.of(1e-4, 1e-4)
                ), precision);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(1, area.getSize(), eps);
        Assert.assertEquals(4, area.getBoundarySize(), eps);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getBarycenter(), eps);
    }

    @Test
    public void testConvexPolygonFromPath() {
        // act
        ConvexArea area = ConvexArea.convexPolygonFromPath(LinePath.fromVertexLoop(
                Arrays.asList(
                        Vector2D.ZERO,
                        Vector2D.Unit.PLUS_X,
                        Vector2D.of(1, 1),
                        Vector2D.Unit.PLUS_Y
                ), TEST_PRECISION));

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(1, area.getSize(), TEST_EPS);
        Assert.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testConvexPolygonFromVertices_notConvex() {
        // arrange
        Pattern msgPattern = Pattern.compile("Cannot construct convex polygon from non-convex path.*");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromVertices(Arrays.asList(
                        Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(2, 0)
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class, msgPattern);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromVertices(Arrays.asList(
                        Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, -1)
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class, msgPattern);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromVertices(
                    Arrays.asList(
                            Vector2D.ZERO,
                            Vector2D.Unit.PLUS_Y,
                            Vector2D.of(1, 1),
                            Vector2D.Unit.PLUS_X
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class, msgPattern);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromVertices(Arrays.asList(
                        Vector2D.ZERO, Vector2D.of(2, 0),
                        Vector2D.of(2, 2), Vector2D.of(1, 1),
                        Vector2D.of(1.5, 1)
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class, msgPattern);
    }

    @Test
    public void testConvexPolygonFromPath_invalidPaths() {
        // arrange
        Pattern unclosedPattern = Pattern.compile("Cannot construct convex polygon from unclosed path.*");
        Pattern notEnoughElementsPattern =
                Pattern.compile("Cannot construct convex polygon from path with less than 3 elements.*");
        Pattern nonConvexPattern = Pattern.compile("Cannot construct convex polygon from non-convex path.*");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromPath(LinePath.empty());
        }, IllegalArgumentException.class, unclosedPattern);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromPath(LinePath.fromVertices(
                    Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X), TEST_PRECISION));
        }, IllegalArgumentException.class, unclosedPattern);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromPath(LinePath.fromVertices(
                    Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.ZERO), TEST_PRECISION));
        }, IllegalArgumentException.class, notEnoughElementsPattern);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.convexPolygonFromPath(LinePath.fromVertexLoop(
                    Arrays.asList(
                            Vector2D.ZERO,
                            Vector2D.Unit.PLUS_Y,
                            Vector2D.of(1, 1),
                            Vector2D.Unit.PLUS_X
                    ), TEST_PRECISION));
        }, IllegalArgumentException.class, nonConvexPattern);
    }

    @Test
    public void testFromBounds_noLines() {
        // act
        ConvexArea area = ConvexArea.fromBounds(Collections.emptyList());

        // assert
        Assert.assertSame(ConvexArea.full(), area);
    }

    @Test
    public void testFromBounds_singleLine() {
        // arrange
        Line line = Lines.fromPoints(Vector2D.of(0, 1), Vector2D.of(1, 3), TEST_PRECISION);

        // act
        ConvexArea area = ConvexArea.fromBounds(line);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assert.assertNull(area.getBarycenter());

        List<LineConvexSubset> segments = area.getBoundaries();
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(line, segments.get(0).getLine());

        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.INSIDE, Vector2D.of(-1, 1), Vector2D.of(0, 2));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.BOUNDARY, Vector2D.of(0, 1), Vector2D.of(2, 5));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.OUTSIDE, Vector2D.ZERO, Vector2D.of(2, 3));
    }

    @Test
    public void testFromBounds_twoLines() {
        // arrange
        Line a = Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        Line b = Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        ConvexArea area = ConvexArea.fromBounds(a, b);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assert.assertNull(area.getBarycenter());

        List<LineConvexSubset> segments = area.getBoundaries();
        Assert.assertEquals(2, segments.size());

        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.INSIDE, Vector2D.of(-1, -1));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(-1, 0), Vector2D.of(0, -1));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.OUTSIDE,
                Vector2D.of(-1, 1), Vector2D.of(1, 1), Vector2D.of(1, -1));
    }

    @Test
    public void testFromBounds_triangle() {
        // arrange
        Line a = Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        Line b = Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI, TEST_PRECISION);
        Line c = Lines.fromPointAndAngle(Vector2D.of(-2, 0), -0.25 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        ConvexArea area = ConvexArea.fromBounds(a, b, c);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(4 + (2 * Math.sqrt(2)), area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(2, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2.0 / 3.0, -2.0 / 3.0), area.getBarycenter(), TEST_EPS);

        List<LineConvexSubset> segments = area.getBoundaries();
        Assert.assertEquals(3, segments.size());

        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.INSIDE, Vector2D.of(-0.5, -0.5));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(-1, 0), Vector2D.of(0, -1));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.OUTSIDE,
                Vector2D.of(-1, 1), Vector2D.of(1, 1), Vector2D.of(1, -1), Vector2D.of(-2, -2));
    }

    @Test
    public void testFromBounds_square() {
        // arrange
        List<Line> square = createSquareBoundingLines(Vector2D.ZERO, 1, 1);

        // act
        ConvexArea area = ConvexArea.fromBounds(square);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(1, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getBarycenter(), TEST_EPS);

        List<LineConvexSubset> segments = area.getBoundaries();
        Assert.assertEquals(4, segments.size());

        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.INSIDE, Vector2D.of(0.5, 0.5));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(1, 1),
                Vector2D.of(0.5, 0), Vector2D.of(0.5, 1),
                Vector2D.of(0, 0.5), Vector2D.of(1, 0.5));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.OUTSIDE,
                Vector2D.of(-1, -1), Vector2D.of(2, 2));
    }

    @Test
    public void testFromBounds_square_extraLines() {
        // arrange
        List<Line> extraLines = new ArrayList<>();
        extraLines.add(Lines.fromPoints(Vector2D.of(10, 10), Vector2D.of(10, 11), TEST_PRECISION));
        extraLines.add(Lines.fromPoints(Vector2D.of(-10, 10), Vector2D.of(-10, 9), TEST_PRECISION));
        extraLines.add(Lines.fromPoints(Vector2D.of(0, 10), Vector2D.of(-1, 11), TEST_PRECISION));
        extraLines.addAll(createSquareBoundingLines(Vector2D.ZERO, 1, 1));

        // act
        ConvexArea area = ConvexArea.fromBounds(extraLines);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(1, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getBarycenter(), TEST_EPS);

        List<LineConvexSubset> segments = area.getBoundaries();
        Assert.assertEquals(4, segments.size());

        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.INSIDE, Vector2D.of(0.5, 0.5));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(1, 1),
                Vector2D.of(0.5, 0), Vector2D.of(0.5, 1),
                Vector2D.of(0, 0.5), Vector2D.of(1, 0.5));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.OUTSIDE,
                Vector2D.of(-1, -1), Vector2D.of(2, 2));
    }

    @Test
    public void testFromBounds_square_duplicateLines() {
        // arrange
        List<Line> duplicateLines = new ArrayList<>();
        duplicateLines.addAll(createSquareBoundingLines(Vector2D.ZERO, 1, 1));
        duplicateLines.addAll(createSquareBoundingLines(Vector2D.ZERO, 1, 1));

        // act
        ConvexArea area = ConvexArea.fromBounds(duplicateLines);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(1, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getBarycenter(), TEST_EPS);

        List<LineConvexSubset> segments = area.getBoundaries();
        Assert.assertEquals(4, segments.size());

        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.INSIDE, Vector2D.of(0.5, 0.5));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(1, 1),
                Vector2D.of(0.5, 0), Vector2D.of(0.5, 1),
                Vector2D.of(0, 0.5), Vector2D.of(1, 0.5));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.OUTSIDE,
                Vector2D.of(-1, -1), Vector2D.of(2, 2));
    }

    @Test
    public void testFromBounds_duplicateLines_similarOrientation() {
        // arrange
        Line a = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line b = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line c = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act
        ConvexArea area = ConvexArea.fromBounds(a, b, c);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assert.assertNull(area.getBarycenter());

        List<LineConvexSubset> segments = area.getBoundaries();
        Assert.assertEquals(1, segments.size());

        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.BOUNDARY, Vector2D.of(0, 1), Vector2D.of(1, 1), Vector2D.of(-1, 1));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.INSIDE, Vector2D.of(0, 2), Vector2D.of(1, 2), Vector2D.of(-1, 2));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.OUTSIDE, Vector2D.of(0, 0), Vector2D.of(1, 0), Vector2D.of(-1, 0));
    }

    @Test
    public void testFromBounds_duplicateLines_differentOrientation() {
        // arrange
        Line a = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line b = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);
        Line c = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromBounds(a, b, c);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromBounds_boundsDoNotProduceAConvexRegion() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromBounds(Arrays.asList(
                        Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION),
                        Lines.fromPointAndAngle(Vector2D.of(0, -1), PlaneAngleRadians.PI, TEST_PRECISION),
                        Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
                    ));
        }, IllegalArgumentException.class);
    }

    private static List<Line> createSquareBoundingLines(final Vector2D lowerLeft, final double width, final double height) {
        final Vector2D lowerRight = Vector2D.of(lowerLeft.getX() + width, lowerLeft.getY());
        final Vector2D upperRight = Vector2D.of(lowerLeft.getX() + width, lowerLeft.getY() + height);
        final Vector2D upperLeft = Vector2D.of(lowerLeft.getX(), lowerLeft.getY() + height);

        return Arrays.asList(
                    Lines.fromPoints(lowerLeft, lowerRight, TEST_PRECISION),
                    Lines.fromPoints(upperRight, upperLeft, TEST_PRECISION),
                    Lines.fromPoints(lowerRight, upperRight, TEST_PRECISION),
                    Lines.fromPoints(upperLeft, lowerLeft, TEST_PRECISION)
                );
    }
}
