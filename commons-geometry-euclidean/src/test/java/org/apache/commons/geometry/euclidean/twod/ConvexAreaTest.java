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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.exception.GeometryException;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
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
    }

    @Test
    public void testToConvex() {
        // arrange
        ConvexArea area = ConvexArea.full();

        // act
        List<ConvexArea> list = area.toConvex();

        // assert
        Assert.assertEquals(1, list.size());
        Assert.assertSame(area, list.get(0));
    }

    @Test
    public void testProject_full() {
        // arrange
        ConvexArea area = ConvexArea.full();

        // act/assert
        Assert.assertNull(area.project(Vector2D.ZERO));
        Assert.assertNull(area.project(Vector2D.PLUS_X));
    }

    @Test
    public void testProject_halfSpace() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(
                Line.fromPointAndAngle(Vector2D.ZERO, Geometry.HALF_PI, TEST_PRECISION));

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
        Segment segment = Segment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_Y, TEST_PRECISION);

        // act
        Segment trimmed = area.trim(segment);

        // assert
        Assert.assertSame(segment, trimmed);
    }

    @Test
    public void testTrim_halfSpace() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION));
        Segment segment = Line.fromPoints(Vector2D.MINUS_Y, Vector2D.PLUS_Y, TEST_PRECISION).span();

        // act
        Segment trimmed = area.trim(segment);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, trimmed.getStartPoint(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(trimmed.getSubspaceEnd());
    }

    @Test
    public void testTrim_square() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.ZERO, 1, 1));
        Segment segment = Line.fromPoints(Vector2D.of(0.5, 0), Vector2D.of(0.5, 1), TEST_PRECISION).span();

        // act
        Segment trimmed = area.trim(segment);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0), trimmed.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 1), trimmed.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testTrim_segmentOutsideOfRegion() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.ZERO, 1, 1));
        Segment segment = Line.fromPoints(Vector2D.of(-0.5, 0), Vector2D.of(-0.5, 1), TEST_PRECISION).span();

        // act
        Segment trimmed = area.trim(segment);

        // assert
        Assert.assertNull(trimmed);
    }

    @Test
    public void testTrim_segmentDirectlyOnBoundaryOfRegion() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.ZERO, 1, 1));
        Segment segment = Line.fromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION).span();

        // act
        Segment trimmed = area.trim(segment);

        // assert
        Assert.assertNull(trimmed);
    }

    @Test
    public void testSplit_full() {
        // arrange
        ConvexArea input = ConvexArea.full();

        Line splitter = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION);

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

        List<Segment> minusSegments = minus.getBoundarySegments();
        Assert.assertEquals(1,minusSegments.size());
        Assert.assertEquals(splitter, minusSegments.get(0).getLine());

        ConvexArea plus = split.getPlus();
        Assert.assertFalse(plus.isFull());
        Assert.assertFalse(plus.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(plus.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(plus.getSize());
        Assert.assertNull(plus.getBarycenter());

        List<Segment> plusSegments = plus.getBoundarySegments();
        Assert.assertEquals(1, plusSegments.size());
        Assert.assertEquals(splitter, plusSegments.get(0).getLine().reverse());
    }

    @Test
    public void testSplit_halfSpace_split() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(Line.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION));
        Line splitter = Line.fromPointAndAngle(Vector2D.ZERO, 0.25 * Geometry.PI, TEST_PRECISION);

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

        Assert.assertEquals(2, minus.getBoundarySegments().size());

        ConvexArea plus = split.getPlus();
        Assert.assertFalse(plus.isFull());
        Assert.assertFalse(plus.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(plus.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(plus.getSize());
        Assert.assertNull(plus.getBarycenter());

        Assert.assertEquals(2, plus.getBoundarySegments().size());
    }

    @Test
    public void testSplit_halfSpace_splitOnBoundary() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(Line.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION));
        Line splitter = Line.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

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
        ConvexArea area = ConvexArea.fromBounds(Line.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION));
        Line splitter = Line.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION).reverse();

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
        Line splitter = Line.fromPointAndAngle(Vector2D.of(2, 1), Geometry.HALF_PI, TEST_PRECISION);

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

        Assert.assertEquals(4, minus.getBoundarySegments().size());

        ConvexArea plus = split.getPlus();
        Assert.assertFalse(plus.isFull());
        Assert.assertFalse(plus.isEmpty());

        Assert.assertEquals(4, plus.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(1, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2.5, 1.5), plus.getBarycenter(), TEST_EPS);

        Assert.assertEquals(4, plus.getBoundarySegments().size());
    }

    @Test
    public void testSplit_square_splitOnVertices() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        Line splitter = Line.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION);

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

        Assert.assertEquals(3, minus.getBoundarySegments().size());

        ConvexArea plus = split.getPlus();
        Assert.assertFalse(plus.isFull());
        Assert.assertFalse(plus.isEmpty());

        Assert.assertEquals(2 + Math.sqrt(2), plus.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.5, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(5.0 / 3.0, 4.0 / 3.0), plus.getBarycenter(), TEST_EPS);

        Assert.assertEquals(3, plus.getBoundarySegments().size());
    }

    @Test
    public void testSplit_square_splitOnVerticesWithReversedSplitter() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        Line splitter = Line.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).reverse();

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

        Assert.assertEquals(3, minus.getBoundarySegments().size());

        ConvexArea plus = split.getPlus();
        Assert.assertFalse(plus.isFull());
        Assert.assertFalse(plus.isEmpty());

        Assert.assertEquals(2 + Math.sqrt(2), plus.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.5, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4.0 / 3.0, 5.0 / 3.0), plus.getBarycenter(), TEST_EPS);

        Assert.assertEquals(3, plus.getBoundarySegments().size());
    }

    @Test
    public void testSplit_square_entirelyOnMinus() {
        // arrange
        ConvexArea area = ConvexArea.fromBounds(createSquareBoundingLines(Vector2D.of(1, 1), 1, 1));
        Line splitter = Line.fromPoints(Vector2D.of(3, 1), Vector2D.of(3, 2), TEST_PRECISION);

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
        Line splitter = Line.fromPoints(Vector2D.of(2, 1), Vector2D.of(2, 2), TEST_PRECISION);

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
        Line splitter = Line.fromPoints(Vector2D.of(0, 1), Vector2D.of(0, 2), TEST_PRECISION);

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
        Line splitter = Line.fromPoints(Vector2D.of(1, 1), Vector2D.of(1, 2), TEST_PRECISION);

        // act
        Split<ConvexArea> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());
        Assert.assertNull(split.getMinus());
        Assert.assertSame(area, split.getPlus());
    }

    @Test
    public void testToString() {
        // arrange
        ConvexArea area = ConvexArea.full();

        // act
        String str = area.toString();

        // assert
        Assert.assertTrue(str.contains("ConvexArea"));
        Assert.assertTrue(str.contains("boundarySegments= "));
    }

    @Test
    public void testFromVertices_noVertices() {
        // act
        ConvexArea area = ConvexArea.fromVertices(Arrays.asList(), TEST_PRECISION);

        // assert
        Assert.assertTrue(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(0, area.getBoundarySize(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assert.assertNull(area.getBarycenter());
    }

    @Test
    public void testFromVertices_singleUniqueVertex() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromVertices(Arrays.asList(Vector2D.ZERO), precision);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromVertices(Arrays.asList(Vector2D.ZERO, Vector2D.of(1e-4, 1e-4)), precision);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertices_twoVertices() {
        // act
        ConvexArea area = ConvexArea.fromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.PLUS_X
                ), TEST_PRECISION);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        Assert.assertNull(area.getBarycenter());

        Assert.assertTrue(area.contains(Vector2D.PLUS_Y));
        Assert.assertFalse(area.contains(Vector2D.MINUS_Y));
    }

    @Test
    public void testFromVertices_threeVertices() {
        // act
        ConvexArea area = ConvexArea.fromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.PLUS_X,
                    Vector2D.of(1, 1)
                ), TEST_PRECISION);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        Assert.assertNull(area.getBarycenter());

        Assert.assertTrue(area.contains(Vector2D.PLUS_Y));
        Assert.assertFalse(area.contains(Vector2D.MINUS_Y));
        Assert.assertFalse(area.contains(Vector2D.of(2, 2)));
    }

    @Test
    public void testFromVertices_finite() {
        // act
        ConvexArea area = ConvexArea.fromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.PLUS_X,
                    Vector2D.of(1, 1),
                    Vector2D.PLUS_Y,
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
    public void testFromVertices_handlesDuplicatePoints() {
        // arrange
        double eps = 1e-3;
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        // act
        ConvexArea area = ConvexArea.fromVertices(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.of(1e-4, 1e-4),
                    Vector2D.PLUS_X,
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
    public void testFromVertices_clockwiseWinding() {
        // act
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromVertices(
                    Arrays.asList(
                            Vector2D.ZERO,
                            Vector2D.PLUS_Y,
                            Vector2D.of(1, 1),
                            Vector2D.PLUS_X,
                            Vector2D.ZERO
                    ),TEST_PRECISION);
        }, GeometryException.class);
    }

    @Test
    public void testFromVertexLoops_noVertices() {
        // act
        ConvexArea area = ConvexArea.fromVertexLoop(Arrays.asList(), TEST_PRECISION);

        // assert
        Assert.assertTrue(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(0, area.getBoundarySize(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assert.assertNull(area.getBarycenter());
    }

    @Test
    public void testFromVertexLoop_singleUniqueVertex() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromVertexLoop(Arrays.asList(Vector2D.ZERO), precision);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromVertexLoop(Arrays.asList(Vector2D.ZERO, Vector2D.of(1e-4, 1e-4)), precision);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertexLoop_twoVertices_fails() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromVertexLoop(Arrays.asList(Vector2D.ZERO, Vector2D.PLUS_X), TEST_PRECISION);
        }, GeometryException.class);
    }

    @Test
    public void testFromVertexLoop_square_closeRequired() {
        // act
        ConvexArea area = ConvexArea.fromVertexLoop(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.PLUS_X,
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
    public void testFromVertexLoop_square_closeNotRequired() {
        // act
        ConvexArea area = ConvexArea.fromVertexLoop(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.PLUS_X,
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
    public void testFromVertexLoop_handlesDuplicatePoints() {
        // arrange
        double eps = 1e-3;
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        // act
        ConvexArea area = ConvexArea.fromVertexLoop(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.of(1e-4, 1e-4),
                    Vector2D.PLUS_X,
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
    public void testFromVertexLoop_clockwiseWinding() {
        // act
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromVertexLoop(
                    Arrays.asList(
                            Vector2D.ZERO,
                            Vector2D.PLUS_Y,
                            Vector2D.of(1, 1),
                            Vector2D.PLUS_X
                    ),TEST_PRECISION);
        }, GeometryException.class);
    }

    @Test
    public void testFromPath_empty() {
        // act
        ConvexArea area = ConvexArea.fromPath(SegmentPath.empty());

        // assert
        Assert.assertTrue(area.isFull());
    }

    @Test
    public void testFromPath_infinite() {
        // act
        ConvexArea area = ConvexArea.fromPath(SegmentPath.fromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.PLUS_X),TEST_PRECISION));

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assert.assertNull(area.getBarycenter());

        checkRegion(area, RegionLocation.INSIDE, Vector2D.PLUS_Y);
        checkRegion(area, RegionLocation.BOUNDARY, Vector2D.ZERO);
        checkRegion(area, RegionLocation.OUTSIDE, Vector2D.MINUS_Y);
    }

    @Test
    public void testFromPath_finite() {
        // act
        ConvexArea area = ConvexArea.fromPath(SegmentPath.fromVertexLoop(
                Arrays.asList(
                        Vector2D.ZERO,
                        Vector2D.PLUS_X,
                        Vector2D.of(1, 1),
                        Vector2D.PLUS_Y
                ),TEST_PRECISION));

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(1, area.getSize(), TEST_EPS);
        Assert.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testFromPath_clockwiseWinding() {
        // act
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromPath(SegmentPath.fromVertexLoop(
                    Arrays.asList(
                            Vector2D.ZERO,
                            Vector2D.PLUS_Y,
                            Vector2D.of(1, 1),
                            Vector2D.PLUS_X
                    ),TEST_PRECISION));
        }, GeometryException.class);
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
        Line line = Line.fromPoints(Vector2D.of(0, 1), Vector2D.of(1, 3), TEST_PRECISION);

        // act
        ConvexArea area = ConvexArea.fromBounds(line);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assert.assertNull(area.getBarycenter());

        List<Segment> segments = area.getBoundarySegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(line, segments.get(0).getLine());

        checkRegion(area, RegionLocation.INSIDE, Vector2D.of(-1, 1), Vector2D.of(0, 2));
        checkRegion(area, RegionLocation.BOUNDARY, Vector2D.of(0, 1), Vector2D.of(2, 5));
        checkRegion(area, RegionLocation.OUTSIDE, Vector2D.ZERO, Vector2D.of(2, 3));
    }

    @Test
    public void testFromBounds_twoLines() {
        // arrange
        Line a = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.HALF_PI, TEST_PRECISION);
        Line b = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.PI, TEST_PRECISION);

        // act
        ConvexArea area = ConvexArea.fromBounds(a, b);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assert.assertNull(area.getBarycenter());

        List<Segment> segments = area.getBoundarySegments();
        Assert.assertEquals(2, segments.size());

        checkRegion(area, RegionLocation.INSIDE, Vector2D.of(-1, -1));
        checkRegion(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(-1, 0), Vector2D.of(0, -1));
        checkRegion(area, RegionLocation.OUTSIDE,
                Vector2D.of(-1, 1), Vector2D.of(1, 1), Vector2D.of(1, -1));
    }

    @Test
    public void testFromBounds_triangle() {
        // arrange
        Line a = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.HALF_PI, TEST_PRECISION);
        Line b = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.PI, TEST_PRECISION);
        Line c = Line.fromPointAndAngle(Vector2D.of(-2, 0), -0.25 * Geometry.PI, TEST_PRECISION);

        // act
        ConvexArea area = ConvexArea.fromBounds(a, b, c);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(4 + (2 * Math.sqrt(2)), area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(2, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2.0 / 3.0, -2.0 / 3.0), area.getBarycenter(), TEST_EPS);

        List<Segment> segments = area.getBoundarySegments();
        Assert.assertEquals(3, segments.size());

        checkRegion(area, RegionLocation.INSIDE, Vector2D.of(-0.5, -0.5));
        checkRegion(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(-1, 0), Vector2D.of(0, -1));
        checkRegion(area, RegionLocation.OUTSIDE,
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

        List<Segment> segments = area.getBoundarySegments();
        Assert.assertEquals(4, segments.size());

        checkRegion(area, RegionLocation.INSIDE, Vector2D.of(0.5, 0.5));
        checkRegion(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(1, 1),
                Vector2D.of(0.5, 0), Vector2D.of(0.5, 1),
                Vector2D.of(0, 0.5), Vector2D.of(1, 0.5));
        checkRegion(area, RegionLocation.OUTSIDE,
                Vector2D.of(-1, -1), Vector2D.of(2, 2));
    }

    @Test
    public void testFromBounds_square_extraLines() {
        // arrange
        List<Line> extraLines = new ArrayList<>();
        extraLines.add(Line.fromPoints(Vector2D.of(10, 10), Vector2D.of(10, 11), TEST_PRECISION));
        extraLines.add(Line.fromPoints(Vector2D.of(-10, 10), Vector2D.of(-10, 9), TEST_PRECISION));
        extraLines.add(Line.fromPoints(Vector2D.of(0, 10), Vector2D.of(-1, 11), TEST_PRECISION));
        extraLines.addAll(createSquareBoundingLines(Vector2D.ZERO, 1, 1));

        // act
        ConvexArea area = ConvexArea.fromBounds(extraLines);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(4, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(1, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getBarycenter(), TEST_EPS);

        List<Segment> segments = area.getBoundarySegments();
        Assert.assertEquals(4, segments.size());

        checkRegion(area, RegionLocation.INSIDE, Vector2D.of(0.5, 0.5));
        checkRegion(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(1, 1),
                Vector2D.of(0.5, 0), Vector2D.of(0.5, 1),
                Vector2D.of(0, 0.5), Vector2D.of(1, 0.5));
        checkRegion(area, RegionLocation.OUTSIDE,
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

        List<Segment> segments = area.getBoundarySegments();
        Assert.assertEquals(4, segments.size());

        checkRegion(area, RegionLocation.INSIDE, Vector2D.of(0.5, 0.5));
        checkRegion(area, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(1, 1),
                Vector2D.of(0.5, 0), Vector2D.of(0.5, 1),
                Vector2D.of(0, 0.5), Vector2D.of(1, 0.5));
        checkRegion(area, RegionLocation.OUTSIDE,
                Vector2D.of(-1, -1), Vector2D.of(2, 2));
    }

    @Test
    public void testFromBounds_duplicateLines_similarOrientation() {
        // arrange
        Line a = Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.ZERO_PI, TEST_PRECISION);
        Line b = Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.ZERO_PI, TEST_PRECISION);
        Line c = Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.ZERO_PI, TEST_PRECISION);

        // act
        ConvexArea area = ConvexArea.fromBounds(a, b, c);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(area.getBoundarySize());
        GeometryTestUtils.assertPositiveInfinity(area.getSize());
        Assert.assertNull(area.getBarycenter());

        List<Segment> segments = area.getBoundarySegments();
        Assert.assertEquals(1, segments.size());

        checkRegion(area, RegionLocation.BOUNDARY, Vector2D.of(0, 1), Vector2D.of(1, 1), Vector2D.of(-1, 1));
        checkRegion(area, RegionLocation.INSIDE, Vector2D.of(0, 2), Vector2D.of(1, 2), Vector2D.of(-1, 2));
        checkRegion(area, RegionLocation.OUTSIDE, Vector2D.of(0, 0), Vector2D.of(1, 0), Vector2D.of(-1, 0));
    }

    @Test
    public void testFromBounds_duplicateLines_differentOrientation() {
        // arrange
        Line a = Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.ZERO_PI, TEST_PRECISION);
        Line b = Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.PI, TEST_PRECISION);
        Line c = Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.ZERO_PI, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromBounds(a, b, c);
        }, GeometryException.class);
    }

    @Test
    public void testFromBounds_boundsDoNotProduceAConvexRegion() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea.fromBounds(Arrays.asList(
                        Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION),
                        Line.fromPointAndAngle(Vector2D.of(0, -1), Geometry.PI, TEST_PRECISION),
                        Line.fromPointAndAngle(Vector2D.ZERO, Geometry.HALF_PI, TEST_PRECISION)
                    ));
        }, GeometryException.class);
    }

    private static void checkRegion(ConvexArea area, RegionLocation loc, Vector2D ... pts) {
        for (Vector2D pt : pts) {
            Assert.assertEquals("Unexpected region location for point " + pt, loc, area.classify(pt));
        }
    }

    private static List<Line> createSquareBoundingLines(final Vector2D lowerLeft, final double width, final double height) {
        final Vector2D lowerRight = Vector2D.of(lowerLeft.getX() + width, lowerLeft.getY());
        final Vector2D upperRight = Vector2D.of(lowerLeft.getX() + width, lowerLeft.getY() + height);
        final Vector2D upperLeft = Vector2D.of(lowerLeft.getX(), lowerLeft.getY() + height);

        return Arrays.asList(
                    Line.fromPoints(lowerLeft, lowerRight, TEST_PRECISION),
                    Line.fromPoints(upperRight, upperLeft, TEST_PRECISION),
                    Line.fromPoints(lowerRight, upperRight, TEST_PRECISION),
                    Line.fromPoints(upperLeft, lowerLeft, TEST_PRECISION)
                );
    }
}
