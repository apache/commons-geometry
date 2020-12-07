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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConvexAreaTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFull() {
        // act
        final ConvexArea area = ConvexArea.full();

        // assert
        Assertions.assertTrue(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        Assertions.assertEquals(0.0, area.getBoundarySize(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assertions.assertNull(area.getCentroid());
        Assertions.assertNull(area.getBounds());
    }

    @Test
    public void testBoundaryStream() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION);
        final ConvexArea area = ConvexArea.fromBounds(line);

        // act
        final List<LineConvexSubset> segments = area.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(1, segments.size());
        final LineConvexSubset segment = segments.get(0);
        Assertions.assertNull(segment.getStartPoint());
        Assertions.assertNull(segment.getEndPoint());
        Assertions.assertSame(line, segment.getLine());
    }

    @Test
    public void testBoundaryStream_full() {
        // arrange
        final ConvexArea area = ConvexArea.full();

        // act
        final List<LineConvexSubset> segments = area.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(0, segments.size());
    }

    @Test
    public void testToTree() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(1, 0), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(1, 1), PlaneAngleRadians.PI, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(0, 1), -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
                );

        // act
        final RegionBSPTree2D tree = area.toTree();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(1, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), tree.getCentroid(), TEST_EPS);
    }

    @Test
    public void testToTree_full() {
        // arrange
        final ConvexArea area = ConvexArea.full();

        // act
        final RegionBSPTree2D tree = area.toTree();

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
    }

    @Test
    public void testTransform_full() {
        // arrange
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(3);
        final ConvexArea area = ConvexArea.full();

        // act
        final ConvexArea transformed = area.transform(transform);

        // assert
        Assertions.assertSame(area, transformed);
    }

    @Test
    public void testTransform_infinite() {
        // arrange
        final AffineTransformMatrix2D mat = AffineTransformMatrix2D
                .createRotation(Vector2D.of(0, 1), PlaneAngleRadians.PI_OVER_TWO)
                .scale(Vector2D.of(3, 2));

        final ConvexArea area = ConvexArea.fromBounds(
                Lines.fromPointAndAngle(Vector2D.ZERO, 0.25 * PlaneAngleRadians.PI, TEST_PRECISION),
                Lines.fromPointAndAngle(Vector2D.ZERO, -0.25 * PlaneAngleRadians.PI, TEST_PRECISION));

        // act
        final ConvexArea transformed = area.transform(mat);

        // assert
        Assertions.assertNotSame(area, transformed);

        final List<LinePath> paths = transformed.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final List<LineConvexSubset> segments = paths.get(0).getElements();
        Assertions.assertEquals(2, segments.size());

        final LineConvexSubset firstSegment = segments.get(0);
        Assertions.assertNull(firstSegment.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 2), firstSegment.getEndPoint(), TEST_EPS);
        Assertions.assertEquals(Math.atan2(2, 3), firstSegment.getLine().getAngle(), TEST_EPS);

        final LineConvexSubset secondSegment = segments.get(1);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 2), secondSegment.getStartPoint(), TEST_EPS);
        Assertions.assertNull(secondSegment.getEndPoint());
        Assertions.assertEquals(Math.atan2(2, -3), secondSegment.getLine().getAngle(), TEST_EPS);
    }

    @Test
    public void testTransform_finite() {
        // arrange
        final AffineTransformMatrix2D mat = AffineTransformMatrix2D.createScale(Vector2D.of(1, 2));

        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 1), Vector2D.of(2, 1),
                    Vector2D.of(2, 2), Vector2D.of(1, 2)
                ), TEST_PRECISION);

        // act
        final ConvexArea transformed = area.transform(mat);

        // assert
        Assertions.assertNotSame(area, transformed);

        final List<LineConvexSubset> segments = transformed.getBoundaries();
        Assertions.assertEquals(4, segments.size());

        Assertions.assertEquals(2, transformed.getSize(), TEST_EPS);
        Assertions.assertEquals(6, transformed.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 3), transformed.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.BOUNDARY,
                Vector2D.of(1, 2), Vector2D.of(2, 2), Vector2D.of(2, 4), Vector2D.of(1, 4));
        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.INSIDE, transformed.getCentroid());
    }

    @Test
    public void testTransform_finite_withSingleReflection() {
        // arrange
        final AffineTransformMatrix2D mat = AffineTransformMatrix2D.createScale(Vector2D.of(-1, 2));

        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 1), Vector2D.of(2, 1),
                    Vector2D.of(2, 2), Vector2D.of(1, 2)
                ), TEST_PRECISION);

        // act
        final ConvexArea transformed = area.transform(mat);

        // assert
        Assertions.assertNotSame(area, transformed);

        final List<LineConvexSubset> segments = transformed.getBoundaries();
        Assertions.assertEquals(4, segments.size());

        Assertions.assertEquals(2, transformed.getSize(), TEST_EPS);
        Assertions.assertEquals(6, transformed.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1.5, 3), transformed.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.BOUNDARY,
                Vector2D.of(-1, 2), Vector2D.of(-2, 2), Vector2D.of(-2, 4), Vector2D.of(-1, 4));
        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.INSIDE, transformed.getCentroid());
    }

    @Test
    public void testTransform_finite_withDoubleReflection() {
        // arrange
        final AffineTransformMatrix2D mat = AffineTransformMatrix2D.createScale(Vector2D.of(-1, -2));

        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 1), Vector2D.of(2, 1),
                    Vector2D.of(2, 2), Vector2D.of(1, 2)
                ), TEST_PRECISION);

        // act
        final ConvexArea transformed = area.transform(mat);

        // assert
        Assertions.assertNotSame(area, transformed);

        final List<LineConvexSubset> segments = transformed.getBoundaries();
        Assertions.assertEquals(4, segments.size());

        Assertions.assertEquals(2, transformed.getSize(), TEST_EPS);
        Assertions.assertEquals(6, transformed.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1.5, -3), transformed.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.BOUNDARY,
                Vector2D.of(-1, -2), Vector2D.of(-2, -2), Vector2D.of(-2, -4), Vector2D.of(-1, -4));
        EuclideanTestUtils.assertRegionLocation(transformed, RegionLocation.INSIDE, transformed.getCentroid());
    }

    @Test
    public void testGetVertices_full() {
        // arrange
        final ConvexArea area = ConvexArea.full();

        // act/assert
        Assertions.assertEquals(0, area.getVertices().size());
    }

    @Test
    public void testGetVertices_twoParallelLines() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(0, -1), 0.0, TEST_PRECISION)
                );

        // act/assert
        Assertions.assertEquals(0, area.getVertices().size());
    }

    @Test
    public void testGetVertices_infiniteWithVertices() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(0, -1), 0.0, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(1, 0), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
                );

        // act
        final List<Vector2D> vertices = area.getVertices();

        // assert
        Assertions.assertEquals(2, vertices.size());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -1), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), vertices.get(1), TEST_EPS);
    }

    @Test
    public void testGetVertices_finite() {
        // arrange
        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.Unit.PLUS_X,
                    Vector2D.Unit.PLUS_Y
                ), TEST_PRECISION);

        // act
        final List<Vector2D> vertices = area.getVertices();

        // assert
        Assertions.assertEquals(3, vertices.size());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_X, vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_Y, vertices.get(2), TEST_EPS);
    }

    @Test
    public void testGetVertices_mismatchedEndpoints() {
        // This test checks the case where we have a valid set of boundary segments but
        // with a small mismatch in the endpoints of some of the segments (possibly due
        // to floating point errors).

        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        final Vector2D p1 = Vector2D.ZERO;
        final Vector2D p2 = Vector2D.of(0.99, 0);
        final Vector2D p3 = Vector2D.of(1, 0.002);
        final Vector2D p4 = Vector2D.of(0.995, -0.001);
        final Vector2D p5 = Vector2D.of(1, 1);

        final ConvexArea area = new ConvexArea(Arrays.asList(
                    Lines.segmentFromPoints(p1, p2, precision),
                    Lines.segmentFromPoints(p2, p3, precision),
                    Lines.segmentFromPoints(p4, p5, precision),
                    Lines.segmentFromPoints(p5, p1, precision)
                ));

        // act
        final List<Vector2D> vertices = area.getVertices();

        // assert
        Assertions.assertEquals(Arrays.asList(p1, p2, p3, p5), vertices);
    }

    @Test
    public void testGetBounds_infinite() {
        // act/assert
        Assertions.assertNull(ConvexArea.full().getBounds());
        Assertions.assertNull(ConvexArea.fromBounds(
                Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)).getBounds());
    }

    @Test
    public void testGetBounds_square() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(-1, -1), 2, 1));

        // act
        final Bounds2D bounds = area.getBounds();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, -1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 0), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testProject_full() {
        // arrange
        final ConvexArea area = ConvexArea.full();

        // act/assert
        Assertions.assertNull(area.project(Vector2D.ZERO));
        Assertions.assertNull(area.project(Vector2D.Unit.PLUS_X));
    }

    @Test
    public void testProject_halfSpace() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(
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
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.ZERO, 1, 1));

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
        final ConvexArea area = ConvexArea.full();
        final Segment segment = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final LineConvexSubset trimmed = area.trim(segment);

        // assert
        Assertions.assertSame(segment, trimmed);
    }

    @Test
    public void testTrim_halfSpace() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION));
        final LineConvexSubset segment = Lines.fromPoints(Vector2D.Unit.MINUS_Y, Vector2D.Unit.PLUS_Y, TEST_PRECISION).span();

        // act
        final LineConvexSubset trimmed = area.trim(segment);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, trimmed.getStartPoint(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(trimmed.getSubspaceEnd());
    }

    @Test
    public void testTrim_square() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.ZERO, 1, 1));
        final LineConvexSubset segment = Lines.fromPoints(Vector2D.of(0.5, 0), Vector2D.of(0.5, 1), TEST_PRECISION).span();

        // act
        final LineConvexSubset trimmed = area.trim(segment);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0), trimmed.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 1), trimmed.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testTrim_segmentOutsideOfRegion() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.ZERO, 1, 1));
        final LineConvexSubset segment = Lines.fromPoints(Vector2D.of(-0.5, 0), Vector2D.of(-0.5, 1), TEST_PRECISION).span();

        // act
        final LineConvexSubset trimmed = area.trim(segment);

        // assert
        Assertions.assertNull(trimmed);
    }

    @Test
    public void testTrim_segmentDirectlyOnBoundaryOfRegion() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.ZERO, 1, 1));
        final LineConvexSubset segment = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION).span();

        // act
        final LineConvexSubset trimmed = area.trim(segment);

        // assert
        Assertions.assertNull(trimmed);
    }

    @Test
    public void testSplit_full() {
        // arrange
        final ConvexArea input = ConvexArea.full();

        final Line splitter = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);

        // act
        final Split<ConvexArea> split = input.split(splitter);

        // act
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final ConvexArea minus = split.getMinus();
        Assertions.assertFalse(minus.isFull());
        Assertions.assertFalse(minus.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(minus.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(minus.getSize());
        Assertions.assertNull(minus.getCentroid());

        final List<LineConvexSubset> minusSegments = minus.getBoundaries();
        Assertions.assertEquals(1, minusSegments.size());
        Assertions.assertEquals(splitter, minusSegments.get(0).getLine());

        final ConvexArea plus = split.getPlus();
        Assertions.assertFalse(plus.isFull());
        Assertions.assertFalse(plus.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(plus.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(plus.getSize());
        Assertions.assertNull(plus.getCentroid());

        final List<LineConvexSubset> plusSegments = plus.getBoundaries();
        Assertions.assertEquals(1, plusSegments.size());
        Assertions.assertEquals(splitter, plusSegments.get(0).getLine().reverse());
    }

    @Test
    public void testSplit_halfSpace_split() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));
        final Line splitter = Lines.fromPointAndAngle(Vector2D.ZERO, 0.25 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Split<ConvexArea> split = area.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final ConvexArea minus = split.getMinus();
        Assertions.assertFalse(minus.isFull());
        Assertions.assertFalse(minus.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(minus.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(minus.getSize());
        Assertions.assertNull(minus.getCentroid());

        Assertions.assertEquals(2, minus.getBoundaries().size());

        final ConvexArea plus = split.getPlus();
        Assertions.assertFalse(plus.isFull());
        Assertions.assertFalse(plus.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(plus.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(plus.getSize());
        Assertions.assertNull(plus.getCentroid());

        Assertions.assertEquals(2, plus.getBoundaries().size());
    }

    @Test
    public void testSplit_halfSpace_splitOnBoundary() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));
        final Line splitter = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final Split<ConvexArea> split = area.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assertions.assertSame(area, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_halfSpace_splitOnBoundaryWithReversedSplitter() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));
        final Line splitter = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).reverse();

        // act
        final Split<ConvexArea> split = area.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(area, split.getPlus());
    }

    @Test
    public void testSplit_square_split() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 2, 1));
        final Line splitter = Lines.fromPointAndAngle(Vector2D.of(2, 1), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);

        // act
        final Split<ConvexArea> split = area.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final ConvexArea minus = split.getMinus();
        Assertions.assertFalse(minus.isFull());
        Assertions.assertFalse(minus.isEmpty());

        Assertions.assertEquals(4, minus.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(1, minus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 1.5), minus.getCentroid(), TEST_EPS);

        Assertions.assertEquals(4, minus.getBoundaries().size());

        final ConvexArea plus = split.getPlus();
        Assertions.assertFalse(plus.isFull());
        Assertions.assertFalse(plus.isEmpty());

        Assertions.assertEquals(4, plus.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(1, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2.5, 1.5), plus.getCentroid(), TEST_EPS);

        Assertions.assertEquals(4, plus.getBoundaries().size());
    }

    @Test
    public void testSplit_square_splitOnVertices() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        final Line splitter = Lines.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION);

        // act
        final Split<ConvexArea> split = area.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final ConvexArea minus = split.getMinus();
        Assertions.assertFalse(minus.isFull());
        Assertions.assertFalse(minus.isEmpty());

        Assertions.assertEquals(2 + Math.sqrt(2), minus.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(0.5, minus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4.0 / 3.0, 5.0 / 3.0), minus.getCentroid(), TEST_EPS);

        Assertions.assertEquals(3, minus.getBoundaries().size());

        final ConvexArea plus = split.getPlus();
        Assertions.assertFalse(plus.isFull());
        Assertions.assertFalse(plus.isEmpty());

        Assertions.assertEquals(2 + Math.sqrt(2), plus.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(0.5, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(5.0 / 3.0, 4.0 / 3.0), plus.getCentroid(), TEST_EPS);

        Assertions.assertEquals(3, plus.getBoundaries().size());
    }

    @Test
    public void testSplit_square_splitOnVerticesWithReversedSplitter() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        final Line splitter = Lines.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).reverse();

        // act
        final Split<ConvexArea> split = area.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final ConvexArea minus = split.getMinus();
        Assertions.assertFalse(minus.isFull());
        Assertions.assertFalse(minus.isEmpty());

        Assertions.assertEquals(2 + Math.sqrt(2), minus.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(0.5, minus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(5.0 / 3.0, 4.0 / 3.0), minus.getCentroid(), TEST_EPS);

        Assertions.assertEquals(3, minus.getBoundaries().size());

        final ConvexArea plus = split.getPlus();
        Assertions.assertFalse(plus.isFull());
        Assertions.assertFalse(plus.isEmpty());

        Assertions.assertEquals(2 + Math.sqrt(2), plus.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(0.5, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4.0 / 3.0, 5.0 / 3.0), plus.getCentroid(), TEST_EPS);

        Assertions.assertEquals(3, plus.getBoundaries().size());
    }

    @Test
    public void testSplit_square_entirelyOnMinus() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        final Line splitter = Lines.fromPoints(Vector2D.of(3, 1), Vector2D.of(3, 2), TEST_PRECISION);

        // act
        final Split<ConvexArea> split = area.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());
        Assertions.assertSame(area, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_square_onMinusBoundary() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        final Line splitter = Lines.fromPoints(Vector2D.of(2, 1), Vector2D.of(2, 2), TEST_PRECISION);

        // act
        final Split<ConvexArea> split = area.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());
        Assertions.assertSame(area, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_square_entirelyOnPlus() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        final Line splitter = Lines.fromPoints(Vector2D.of(0, 1), Vector2D.of(0, 2), TEST_PRECISION);

        // act
        final Split<ConvexArea> split = area.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());
        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(area, split.getPlus());
    }

    @Test
    public void testSplit_square_onPlusBoundary() {
        // arrange
        final ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        final Line splitter = Lines.fromPoints(Vector2D.of(1, 1), Vector2D.of(1, 2), TEST_PRECISION);

        // act
        final Split<ConvexArea> split = area.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());
        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(area, split.getPlus());
    }

    @Test
    public void testSplit_fannedLines() {
        // arrange
        final Line a = Lines.fromPointAndDirection(
                Vector2D.of(0.00600526260605261, -0.3392565140336253),
                Vector2D.of(0.9998433697734339, 0.017698472253402094), TEST_PRECISION);
        final Line b = Lines.fromPointAndDirection(
                Vector2D.of(-0.05020576603061953, 1.7524758059156824),
                Vector2D.of(0.9995898847600798, 0.02863672965494457), TEST_PRECISION);

        final ConvexArea area = ConvexArea.fromBounds(a, b.reverse());

        final Line splitter = Lines.fromPointAndDirection(
                Vector2D.of(0.01581855191043128, -2.5270731411451215),
                Vector2D.of(0.999980409069402, 0.006259510954681248), TEST_PRECISION);

        // act
        final Split<ConvexArea> split = area.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());
        Assertions.assertSame(area, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_trimmedSplitterDiscrepancy() {
        // The following example came from a failed invocation of the Sphere.toTree() method.
        // This test checks the case where the splitter trimmed to the area is non-empty but
        // the boundaries split by the splitter all lies on a single side.

        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-10);

        final Vector2D p1 = Vector2D.of(-100.27622744776312, -39.236143934478704);
        final Vector2D p2 = Vector2D.of(-100.23149336840831, -39.28090397981739);
        final Vector2D p3 = Vector2D.of(-96.28607710958399, -39.25486984391497);
        final ConvexArea area = ConvexArea.fromBounds(
                    Lines.fromPointAndDirection(p1, Vector2D.of(-0.00601644753700725, -0.9999819010157307), precision),
                    Lines.fromPoints(p1, p2, precision),
                    Lines.fromPoints(p2, p3, precision),
                    Lines.fromPointAndDirection(p3, Vector2D.of(0.9999648811047153, 0.008380725340508379), precision)
                );

        final Line splitter = Lines.fromPointAndDirection(
                Vector2D.of(-68.9981806624852, -70.04669274578112),
                Vector2D.of(0.7124186895479748, -0.7017546656651072),
                precision);

        // act
        final Split<ConvexArea> minusSplit = area.split(splitter);
        final Split<ConvexArea> plusSplit = area.split(splitter.reverse());

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, minusSplit.getLocation());

        Assertions.assertSame(area, minusSplit.getMinus());
        Assertions.assertNull(minusSplit.getPlus());

        Assertions.assertEquals(SplitLocation.PLUS, plusSplit.getLocation());

        Assertions.assertNull(plusSplit.getMinus());
        Assertions.assertSame(area, plusSplit.getPlus());
    }

    @Test
    public void testLinecast_full() {
        // arrange
        final ConvexArea area = ConvexArea.full();

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
        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
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
        final ConvexArea area = ConvexArea.full();

        // act
        final String str = area.toString();

        // assert
        Assertions.assertTrue(str.contains("ConvexArea"));
        Assertions.assertTrue(str.contains("boundaries= "));
    }

    @Test
    public void testConvexPolygonFromVertices_notEnoughUniqueVertices() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        final Pattern unclosedPattern = Pattern.compile("Cannot construct convex polygon from unclosed path.*");
        final Pattern notEnoughElementsPattern =
                Pattern.compile("Cannot construct convex polygon from path with less than 3 elements.*");
        final Pattern nonConvexPattern = Pattern.compile("Cannot construct convex polygon from non-convex path.*");

        final Pattern singleVertexPattern =
                Pattern.compile("Unable to create line path; only a single unique vertex provided.*");

        // act/assert

        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromVertices(Collections.emptyList(), precision));
        assertThrows(IllegalStateException.class, () ->  ConvexArea.convexPolygonFromVertices(Collections.singletonList(Vector2D.ZERO), precision));
        assertThrows(IllegalStateException.class, () ->  ConvexArea.convexPolygonFromVertices(Arrays.asList(Vector2D.ZERO, Vector2D.of(1e-4, 1e-4)), precision));
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromVertices(Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X), precision));
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, 1e-4)), precision));
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, -1)), precision));
    }

    @Test
    public void testConvexPolygonFromVertices_triangle() {
        // arrange
        final Vector2D p0 = Vector2D.of(1, 2);
        final Vector2D p1 = Vector2D.of(2, 2);
        final Vector2D p2 = Vector2D.of(2, 3);

        // act
        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(p0, p1, p2), TEST_PRECISION);

        // assert
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        Assertions.assertEquals(0.5, area.getSize(), TEST_EPS);
        Assertions.assertEquals(2 + Math.sqrt(2), area.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.centroid(p0, p1, p2), area.getCentroid(), TEST_EPS);
    }

    @Test
    public void testConvexPolygonFromVertices_square_closeRequired() {
        // act
        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.Unit.PLUS_X,
                    Vector2D.of(1, 1),
                    Vector2D.of(0, 1)
                ), TEST_PRECISION);

        // assert
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        Assertions.assertEquals(1, area.getSize(), TEST_EPS);
        Assertions.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getCentroid(), TEST_EPS);
    }

    @Test
    public void testConvexPolygonFromVertices_square_closeNotRequired() {
        // act
        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.Unit.PLUS_X,
                    Vector2D.of(1, 1),
                    Vector2D.of(0, 1),
                    Vector2D.ZERO
                ), TEST_PRECISION);

        // assert
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        Assertions.assertEquals(1, area.getSize(), TEST_EPS);
        Assertions.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getCentroid(), TEST_EPS);
    }

    @Test
    public void testConvexPolygonFromVertices_handlesDuplicatePoints() {
        // arrange
        final double eps = 1e-3;
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        // act
        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
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
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        Assertions.assertEquals(1, area.getSize(), eps);
        Assertions.assertEquals(4, area.getBoundarySize(), eps);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getCentroid(), eps);
    }

    @Test
    public void testConvexPolygonFromPath() {
        // act
        final ConvexArea area = ConvexArea.convexPolygonFromPath(LinePath.fromVertexLoop(
                Arrays.asList(
                        Vector2D.ZERO,
                        Vector2D.Unit.PLUS_X,
                        Vector2D.of(1, 1),
                        Vector2D.Unit.PLUS_Y
                ), TEST_PRECISION));

        // assert
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        Assertions.assertEquals(1, area.getSize(), TEST_EPS);
        Assertions.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getCentroid(), TEST_EPS);
    }

    @Test
    public void testConvexPolygonFromVertices_notConvex() {
        // arrange
        final Pattern msgPattern = Pattern.compile("Cannot construct convex polygon from non-convex path.*");

        // act/assert
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromVertices(Arrays.asList(
                Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(2, 0)
        ), TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromVertices(Arrays.asList(
                Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, -1)
        ), TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromVertices(
                Arrays.asList(
                        Vector2D.ZERO,
                        Vector2D.Unit.PLUS_Y,
                        Vector2D.of(1, 1),
                        Vector2D.Unit.PLUS_X
                ), TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromVertices(Arrays.asList(
                Vector2D.ZERO, Vector2D.of(2, 0),
                Vector2D.of(2, 2), Vector2D.of(1, 1),
                Vector2D.of(1.5, 1)
        ), TEST_PRECISION));
    }

    @Test
    public void testConvexPolygonFromPath_invalidPaths() {
        // arrange
        final Pattern unclosedPattern = Pattern.compile("Cannot construct convex polygon from unclosed path.*");
        final Pattern notEnoughElementsPattern =
                Pattern.compile("Cannot construct convex polygon from path with less than 3 elements.*");
        final Pattern nonConvexPattern = Pattern.compile("Cannot construct convex polygon from non-convex path.*");

        // act/assert
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromPath(LinePath.empty()));
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromPath(LinePath.fromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X), TEST_PRECISION)));
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromPath(LinePath.fromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.ZERO), TEST_PRECISION)));
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.convexPolygonFromPath(LinePath.fromVertexLoop(
                Arrays.asList(
                        Vector2D.ZERO,
                        Vector2D.Unit.PLUS_Y,
                        Vector2D.of(1, 1),
                        Vector2D.Unit.PLUS_X
                ), TEST_PRECISION)));
    }

    @Test
    public void testFromBounds_noLines() {
        // act
        final ConvexArea area = ConvexArea.fromBounds(Collections.emptyList());

        // assert
        Assertions.assertSame(ConvexArea.full(), area);
    }

    @Test
    public void testFromBounds_singleLine() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.of(0, 1), Vector2D.of(1, 3), TEST_PRECISION);

        // act
        final ConvexArea area = ConvexArea.fromBounds(line);

        // assert
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assertions.assertNull(area.getCentroid());

        final List<LineConvexSubset> segments = area.getBoundaries();
        Assertions.assertEquals(1, segments.size());
        Assertions.assertSame(line, segments.get(0).getLine());

        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.INSIDE, Vector2D.of(-1, 1), Vector2D.of(0, 2));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.BOUNDARY, Vector2D.of(0, 1), Vector2D.of(2, 5));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.OUTSIDE, Vector2D.ZERO, Vector2D.of(2, 3));
    }

    @Test
    public void testFromBounds_twoLines() {
        // arrange
        final Line a = Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        final Line b = Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final ConvexArea area = ConvexArea.fromBounds(a, b);

        // assert
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assertions.assertNull(area.getCentroid());

        final List<LineConvexSubset> segments = area.getBoundaries();
        Assertions.assertEquals(2, segments.size());

        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.INSIDE, Vector2D.of(-1, -1));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(-1, 0), Vector2D.of(0, -1));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.OUTSIDE,
                Vector2D.of(-1, 1), Vector2D.of(1, 1), Vector2D.of(1, -1));
    }

    @Test
    public void testFromBounds_triangle() {
        // arrange
        final Line a = Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        final Line b = Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI, TEST_PRECISION);
        final Line c = Lines.fromPointAndAngle(Vector2D.of(-2, 0), -0.25 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final ConvexArea area = ConvexArea.fromBounds(a, b, c);

        // assert
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        Assertions.assertEquals(4 + (2 * Math.sqrt(2)), area.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(2, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2.0 / 3.0, -2.0 / 3.0), area.getCentroid(), TEST_EPS);

        final List<LineConvexSubset> segments = area.getBoundaries();
        Assertions.assertEquals(3, segments.size());

        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.INSIDE, Vector2D.of(-0.5, -0.5));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(-1, 0), Vector2D.of(0, -1));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.OUTSIDE,
                Vector2D.of(-1, 1), Vector2D.of(1, 1), Vector2D.of(1, -1), Vector2D.of(-2, -2));
    }

    @Test
    public void testFromBounds_square() {
        // arrange
        final List<Line> square = createSquareBoundingLines(Vector2D.ZERO, 1, 1);

        // act
        final ConvexArea area = ConvexArea.fromBounds(square);

        // assert
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        Assertions.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(1, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getCentroid(), TEST_EPS);

        final List<LineConvexSubset> segments = area.getBoundaries();
        Assertions.assertEquals(4, segments.size());

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
        final List<Line> extraLines = new ArrayList<>();
        extraLines.add(Lines.fromPoints(Vector2D.of(10, 10), Vector2D.of(10, 11), TEST_PRECISION));
        extraLines.add(Lines.fromPoints(Vector2D.of(-10, 10), Vector2D.of(-10, 9), TEST_PRECISION));
        extraLines.add(Lines.fromPoints(Vector2D.of(0, 10), Vector2D.of(-1, 11), TEST_PRECISION));
        extraLines.addAll(createSquareBoundingLines(Vector2D.ZERO, 1, 1));

        // act
        final ConvexArea area = ConvexArea.fromBounds(extraLines);

        // assert
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        Assertions.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(1, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getCentroid(), TEST_EPS);

        final List<LineConvexSubset> segments = area.getBoundaries();
        Assertions.assertEquals(4, segments.size());

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
        final List<Line> duplicateLines = new ArrayList<>();
        duplicateLines.addAll(createSquareBoundingLines(Vector2D.ZERO, 1, 1));
        duplicateLines.addAll(createSquareBoundingLines(Vector2D.ZERO, 1, 1));

        // act
        final ConvexArea area = ConvexArea.fromBounds(duplicateLines);

        // assert
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        Assertions.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(1, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getCentroid(), TEST_EPS);

        final List<LineConvexSubset> segments = area.getBoundaries();
        Assertions.assertEquals(4, segments.size());

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
        final Line a = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        final Line b = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        final Line c = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act
        final ConvexArea area = ConvexArea.fromBounds(a, b, c);

        // assert
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assertions.assertNull(area.getCentroid());

        final List<LineConvexSubset> segments = area.getBoundaries();
        Assertions.assertEquals(1, segments.size());

        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.BOUNDARY, Vector2D.of(0, 1), Vector2D.of(1, 1), Vector2D.of(-1, 1));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.INSIDE, Vector2D.of(0, 2), Vector2D.of(1, 2), Vector2D.of(-1, 2));
        EuclideanTestUtils.assertRegionLocation(area, RegionLocation.OUTSIDE, Vector2D.of(0, 0), Vector2D.of(1, 0), Vector2D.of(-1, 0));
    }

    @Test
    public void testFromBounds_duplicateLines_differentOrientation() {
        // arrange
        final Line a = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        final Line b = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);
        final Line c = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act/assert
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.fromBounds(a, b, c));
    }

    @Test
    public void testFromBounds_boundsDoNotProduceAConvexRegion() {
        // act/assert
        assertThrows(IllegalArgumentException.class, () ->  ConvexArea.fromBounds(Arrays.asList(
                Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION),
                Lines.fromPointAndAngle(Vector2D.of(0, -1), PlaneAngleRadians.PI, TEST_PRECISION),
                Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
        )));
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
