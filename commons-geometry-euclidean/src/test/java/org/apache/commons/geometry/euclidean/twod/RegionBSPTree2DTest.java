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
import java.util.Comparator;
import java.util.List;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D.RegionNode2D;
import org.junit.Assert;
import org.junit.Test;

public class RegionBSPTree2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Comparator<Segment> SEGMENT_COMPARATOR =
            (a, b) -> Vector2D.COORDINATE_ASCENDING_ORDER.compare(a.getStartPoint(), b.getStartPoint());

    private static final Line X_AXIS = Line.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

    private static final Line Y_AXIS = Line.fromPoints(Vector2D.ZERO, Vector2D.PLUS_Y, TEST_PRECISION);

    @Test
    public void testCtor_booleanArg_true() {
        // act
        RegionBSPTree2D tree = new RegionBSPTree2D(true);

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testCtor_booleanArg_false() {
        // act
        RegionBSPTree2D tree = new RegionBSPTree2D(false);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testCtor_default() {
        // act
        RegionBSPTree2D tree = new RegionBSPTree2D();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testFull_factoryMethod() {
        // act
        RegionBSPTree2D tree = RegionBSPTree2D.full();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testEmpty_factoryMethod() {
        // act
        RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testGetBoundaryPaths_cachesResult() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(Segment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION));

        // act
        List<SegmentPath> a = tree.getBoundaryPaths();
        List<SegmentPath> b = tree.getBoundaryPaths();

        // assert
        Assert.assertSame(a, b);
    }

    @Test
    public void testGetBoundaryPaths_recomputesResultOnChange() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(Segment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION));

        // act
        List<SegmentPath> a = tree.getBoundaryPaths();
        tree.insert(Segment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_Y, TEST_PRECISION));
        List<SegmentPath> b = tree.getBoundaryPaths();

        // assert
        Assert.assertNotSame(a, b);
    }

    @Test
    public void testGetBoundaryPaths_isUnmodifiable() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(Segment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            tree.getBoundaryPaths().add(SegmentPath.builder(null).build());
        }, UnsupportedOperationException.class);
    }

    @Test
    public void testToConvex_full() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.full();

        // act
        List<ConvexArea> result = tree.toConvex();

        // assert
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.get(0).isFull());
    }

    @Test
    public void testToConvex_empty() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // act
        List<ConvexArea> result = tree.toConvex();

        // assert
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testToConvex_halfSpace() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.full();
        tree.getRoot().insertCut(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION));

        // act
        List<ConvexArea> result = tree.toConvex();

        // assert
        Assert.assertEquals(1, result.size());

        ConvexArea area = result.get(0);
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        checkClassify(area, RegionLocation.INSIDE, Vector2D.of(0, 1));
        checkClassify(area, RegionLocation.BOUNDARY, Vector2D.ZERO);
        checkClassify(area, RegionLocation.OUTSIDE, Vector2D.of(0, -1));
    }

    @Test
    public void testToConvex_quadrantComplement() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.full();
        tree.getRoot().cut(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.PI, TEST_PRECISION))
            .getPlus().cut(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.HALF_PI, TEST_PRECISION));

        tree.complement();

        // act
        List<ConvexArea> result = tree.toConvex();

        // assert
        Assert.assertEquals(1, result.size());

        ConvexArea area = result.get(0);
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        checkClassify(area, RegionLocation.INSIDE, Vector2D.of(1, 1));
        checkClassify(area, RegionLocation.BOUNDARY, Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(0, 1));
        checkClassify(area, RegionLocation.OUTSIDE, Vector2D.of(1, -1), Vector2D.of(-1, -1), Vector2D.of(-1, 1));
    }

    @Test
    public void testToConvex_square() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.ZERO, 1, 1, TEST_PRECISION);

        // act
        List<ConvexArea> result = tree.toConvex();

        // assert
        Assert.assertEquals(1, result.size());

        ConvexArea area = result.get(0);
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());

        Assert.assertEquals(1, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getBarycenter(), TEST_EPS);

        checkClassify(area, RegionLocation.INSIDE, Vector2D.of(0.5, 0.5));
        checkClassify(area, RegionLocation.BOUNDARY, Vector2D.ZERO, Vector2D.of(1, 1));
        checkClassify(area, RegionLocation.OUTSIDE,
                Vector2D.of(0.5, -1), Vector2D.of(0.5, 2),
                Vector2D.of(-1, 0.5), Vector2D.of(2, 0.5));
    }

    @Test
    public void testToConvex_multipleConvexAreas() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(Arrays.asList(
                    Segment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION),

                    Segment.fromPoints(Vector2D.of(1, 1), Vector2D.of(0, 1), TEST_PRECISION),
                    Segment.fromPoints(Vector2D.of(0, 1), Vector2D.ZERO, TEST_PRECISION),

                    Segment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION),
                    Segment.fromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION)
                ));

        // act
        List<ConvexArea> result = tree.toConvex();

        // assert
        Collections.sort(result, (a, b) ->
            Vector2D.COORDINATE_ASCENDING_ORDER.compare(a.getBarycenter(), b.getBarycenter()));

        Assert.assertEquals(2, result.size());

        ConvexArea firstArea = result.get(0);
        Assert.assertFalse(firstArea.isFull());
        Assert.assertFalse(firstArea.isEmpty());

        Assert.assertEquals(0.5, firstArea.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.0 / 3.0, 2.0 / 3.0), firstArea.getBarycenter(), TEST_EPS);

        checkClassify(firstArea, RegionLocation.INSIDE, Vector2D.of(1.0 / 3.0, 2.0 / 3.0));
        checkClassify(firstArea, RegionLocation.BOUNDARY, Vector2D.ZERO, Vector2D.of(1, 1), Vector2D.of(0.5, 0.5));
        checkClassify(firstArea, RegionLocation.OUTSIDE,
                Vector2D.of(0.25, -1), Vector2D.of(0.25, 2),
                Vector2D.of(-1, 0.5), Vector2D.of(0.75, 0.5));

        ConvexArea secondArea = result.get(1);
        Assert.assertFalse(secondArea.isFull());
        Assert.assertFalse(secondArea.isEmpty());

        Assert.assertEquals(0.5, secondArea.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2.0 / 3.0, 1.0 / 3.0), secondArea.getBarycenter(), TEST_EPS);

        checkClassify(secondArea, RegionLocation.INSIDE, Vector2D.of(2.0 / 3.0, 1.0 / 3.0));
        checkClassify(secondArea, RegionLocation.BOUNDARY, Vector2D.ZERO, Vector2D.of(1, 1), Vector2D.of(0.5, 0.5));
        checkClassify(secondArea, RegionLocation.OUTSIDE,
                Vector2D.of(0.75, -1), Vector2D.of(0.75, 2),
                Vector2D.of(2, 0.5), Vector2D.of(0.25, 0.5));
    }

    @Test
    public void testGetNodeRegion() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();

        RegionNode2D root = tree.getRoot();
        root.cut(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION));

        RegionNode2D minus = root.getMinus();
        minus.cut(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.HALF_PI, TEST_PRECISION));

        Vector2D origin = Vector2D.ZERO;

        Vector2D a = Vector2D.of(1, 0);
        Vector2D b = Vector2D.of(1, 1);
        Vector2D c = Vector2D.of(0, 1);
        Vector2D d = Vector2D.of(-1, 1);
        Vector2D e = Vector2D.of(-1, 0);
        Vector2D f = Vector2D.of(-1, -1);
        Vector2D g = Vector2D.of(0, -1);
        Vector2D h = Vector2D.of(1, -1);

        // act/assert
        checkConvexArea(root.getNodeRegion(), Arrays.asList(origin, a, b, c, d, e, f, g, h), Arrays.asList());

        checkConvexArea(minus.getNodeRegion(), Arrays.asList(b, c, d), Arrays.asList(f, g, h));
        checkConvexArea(root.getPlus().getNodeRegion(), Arrays.asList(f, g, h), Arrays.asList(b, c, d));

        checkConvexArea(minus.getMinus().getNodeRegion(), Arrays.asList(d), Arrays.asList(a, b, f, g, h));
        checkConvexArea(minus.getPlus().getNodeRegion(), Arrays.asList(b), Arrays.asList(d, e, f, g, h));
    }

    @Test
    public void testSplit_full() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.full();

        Line splitter = Line.fromPointAndAngle(Vector2D.of(1, 0), 0.25 * Geometry.PI, TEST_PRECISION);

        // act
        Split<RegionBSPTree2D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkClassify(split.getMinus(), RegionLocation.INSIDE, Vector2D.of(0, 1));
        checkClassify(split.getMinus(), RegionLocation.OUTSIDE, Vector2D.of(1, -1));

        List<SegmentPath> minusBoundaryList = split.getMinus().getBoundaryPaths();
        Assert.assertEquals(1, minusBoundaryList.size());

        SegmentPath minusBoundary = minusBoundaryList.get(0);
        Assert.assertEquals(1, minusBoundary.getSegments().size());
        Assert.assertTrue(minusBoundary.isInfinite());
        Assert.assertSame(splitter, minusBoundary.getStartSegment().getLine());

        checkClassify(split.getPlus(), RegionLocation.OUTSIDE, Vector2D.of(0, 1));
        checkClassify(split.getPlus(), RegionLocation.INSIDE, Vector2D.of(1, -1));

        List<SegmentPath> plusBoundaryList = split.getPlus().getBoundaryPaths();
        Assert.assertEquals(1, plusBoundaryList.size());

        SegmentPath plusBoundary = minusBoundaryList.get(0);
        Assert.assertEquals(1, plusBoundary.getSegments().size());
        Assert.assertTrue(plusBoundary.isInfinite());
        Assert.assertSame(splitter, plusBoundary.getStartSegment().getLine());
    }

    @Test
    public void testSplit_empty() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();

        Line splitter = Line.fromPointAndAngle(Vector2D.of(1, 0), 0.25 * Geometry.PI, TEST_PRECISION);

        // act
        Split<RegionBSPTree2D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_bothSides() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.ZERO, 2, 1, TEST_PRECISION);

        Line splitter = Line.fromPointAndAngle(Vector2D.ZERO, 0.25 * Geometry.PI, TEST_PRECISION);

        // act
        Split<RegionBSPTree2D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        List<SegmentPath> minusPath = split.getMinus().getBoundaryPaths();
        Assert.assertEquals(1, minusPath.size());
        checkVertices(minusPath.get(0), Vector2D.ZERO, Vector2D.of(1, 1),
                Vector2D.of(0, 1), Vector2D.ZERO);

        List<SegmentPath> plusPath = split.getPlus().getBoundaryPaths();
        Assert.assertEquals(1, plusPath.size());
        checkVertices(plusPath.get(0), Vector2D.ZERO, Vector2D.of(2, 0),
                Vector2D.of(2, 1), Vector2D.of(1, 1), Vector2D.ZERO);
    }

    @Test
    public void testSplit_plusSideOnly() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.ZERO, 2, 1, TEST_PRECISION);

        Line splitter = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.25 * Geometry.PI, TEST_PRECISION);

        // act
        Split<RegionBSPTree2D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());

        List<SegmentPath> plusPath = split.getPlus().getBoundaryPaths();
        Assert.assertEquals(1, plusPath.size());
        checkVertices(plusPath.get(0), Vector2D.ZERO, Vector2D.of(2, 0),
                Vector2D.of(2, 1), Vector2D.of(0, 1), Vector2D.ZERO);
    }

    @Test
    public void testSplit_minusSideOnly() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.ZERO, 2, 1, TEST_PRECISION);

        Line splitter = Line.fromPointAndAngle(Vector2D.of(0, 1), 0.25 * Geometry.PI, TEST_PRECISION)
                .reverse();

        // act
        Split<RegionBSPTree2D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        List<SegmentPath> minusPath = split.getMinus().getBoundaryPaths();
        Assert.assertEquals(1, minusPath.size());
        checkVertices(minusPath.get(0), Vector2D.ZERO, Vector2D.of(2, 0),
                Vector2D.of(2, 1), Vector2D.of(0, 1), Vector2D.ZERO);

        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testGeometricProperties_full() {
        // arrrange
        RegionBSPTree2D tree = RegionBSPTree2D.full();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertNull(tree.getBarycenter());

        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        Assert.assertEquals(0, tree.getBoundarySegments().size());
        Assert.assertEquals(0, tree.getBoundaryPaths().size());
    }

    @Test
    public void testGeometricProperties_empty() {
        // arrrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // act/assert
        Assert.assertEquals(0, tree.getSize(), TEST_EPS);
        Assert.assertNull(tree.getBarycenter());

        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        Assert.assertEquals(0, tree.getBoundarySegments().size());
        Assert.assertEquals(0, tree.getBoundaryPaths().size());
    }

    @Test
    public void testGeometricProperties_halfSpace() {
        // arrrange
        RegionBSPTree2D tree = RegionBSPTree2D.full();
        tree.getRoot().cut(X_AXIS);

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertNull(tree.getBarycenter());

        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());

        List<Segment> segments = tree.getBoundarySegments();
        Assert.assertEquals(1, segments.size());

        Segment segment = segments.get(0);
        Assert.assertSame(X_AXIS, segment.getLine());
        Assert.assertNull(segment.getStartPoint());
        Assert.assertNull(segment.getEndPoint());

        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertEquals(1, path.getSegments().size());
        Assert.assertEquals(segment, path.getSegments().get(0));
    }

    @Test
    public void testGeometricProperties_complementedHalfSpace() {
        // arrrange
        RegionBSPTree2D tree = RegionBSPTree2D.full();
        tree.getRoot().cut(X_AXIS);

        tree.complement();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertNull(tree.getBarycenter());

        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());

        List<Segment> segments = tree.getBoundarySegments();
        Assert.assertEquals(1, segments.size());

        Segment segment = segments.get(0);
        Assert.assertEquals(X_AXIS.reverse(), segment.getLine());
        Assert.assertNull(segment.getStartPoint());
        Assert.assertNull(segment.getEndPoint());

        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertEquals(1, path.getSegments().size());
        Assert.assertEquals(segment, path.getSegments().get(0));
    }

    @Test
    public void testGeometricProperties_quadrant() {
        // arrrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.getRoot().cut(X_AXIS)
            .getMinus().cut(Y_AXIS);

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertNull(tree.getBarycenter());

        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());

        List<Segment> segments = new ArrayList<>(tree.getBoundarySegments());
        Assert.assertEquals(2, segments.size());

        Collections.sort(segments, SEGMENT_COMPARATOR);

        Segment firstSegment = segments.get(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, firstSegment.getStartPoint(), TEST_EPS);
        Assert.assertNull(firstSegment.getEndPoint());
        Assert.assertSame(Y_AXIS, firstSegment.getLine());

        Segment secondSegment = segments.get(1);
        Assert.assertNull(secondSegment.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, secondSegment.getEndPoint(), TEST_EPS);
        Assert.assertSame(X_AXIS, secondSegment.getLine());

        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertEquals(2, path.getSegments().size());
        Assert.assertEquals(secondSegment, path.getSegments().get(0));
        Assert.assertEquals(firstSegment, path.getSegments().get(1));
    }

    @Test
    public void testGeometricProperties_complementedQuadrant() {
        // arrrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.getRoot().cut(X_AXIS)
            .getMinus().cut(Y_AXIS);

        tree.complement();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertNull(tree.getBarycenter());

        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());

        List<Segment> segments = new ArrayList<>(tree.getBoundarySegments());
        Assert.assertEquals(2, segments.size());

        Collections.sort(segments, SEGMENT_COMPARATOR);

        Segment firstSegment = segments.get(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, firstSegment.getStartPoint(), TEST_EPS);
        Assert.assertNull(firstSegment.getEndPoint());
        Assert.assertEquals(X_AXIS.reverse(), firstSegment.getLine());

        Segment secondSegment = segments.get(1);
        Assert.assertNull(secondSegment.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, secondSegment.getEndPoint(), TEST_EPS);
        Assert.assertEquals(Y_AXIS.reverse(), secondSegment.getLine());

        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertEquals(2, path.getSegments().size());
        Assert.assertEquals(secondSegment, path.getSegments().get(0));
        Assert.assertEquals(firstSegment, path.getSegments().get(1));
    }

    @Test
    public void testGeometricProperties_closedRegion() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(SegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(2, 1))
                .close());

        // act/assert
        Assert.assertEquals(0.5, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1.0 / 3.0), tree.getBarycenter(), TEST_EPS);

        Assert.assertEquals(1.0 + Math.sqrt(2) + Math.sqrt(5), tree.getBoundarySize(), TEST_EPS);

        List<Segment> segments = new ArrayList<>(tree.getBoundarySegments());
        Collections.sort(segments, SEGMENT_COMPARATOR);

        Assert.assertEquals(3, segments.size());

        checkFiniteSegment(segments.get(0), Vector2D.ZERO, Vector2D.of(1, 0));
        checkFiniteSegment(segments.get(1), Vector2D.of(1, 0), Vector2D.of(2, 1));
        checkFiniteSegment(segments.get(2), Vector2D.of(2, 1), Vector2D.ZERO);

        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        checkVertices(paths.get(0), Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(2, 1), Vector2D.ZERO);
    }

    @Test
    public void testGeometricProperties_complementedClosedRegion() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(SegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(2, 1))
                .close());

        tree.complement();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertNull(tree.getBarycenter());

        Assert.assertEquals(1.0 + Math.sqrt(2) + Math.sqrt(5), tree.getBoundarySize(), TEST_EPS);

        List<Segment> segments = new ArrayList<>(tree.getBoundarySegments());
        Collections.sort(segments, SEGMENT_COMPARATOR);

        Assert.assertEquals(3, segments.size());

        checkFiniteSegment(segments.get(0), Vector2D.ZERO, Vector2D.of(2, 1));
        checkFiniteSegment(segments.get(1), Vector2D.of(1, 0), Vector2D.ZERO);
        checkFiniteSegment(segments.get(2), Vector2D.of(2, 1), Vector2D.of(1, 0));

        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        checkVertices(paths.get(0), Vector2D.ZERO, Vector2D.of(2, 1), Vector2D.of(1, 0), Vector2D.ZERO);
    }

    @Test
    public void testGeometricProperties_regionWithHole() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.ZERO, Vector2D.of(3, 3), TEST_PRECISION);
        RegionBSPTree2D inner = RegionBSPTree2D.rect(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION);

        tree.difference(inner);

        // act/assert
        Assert.assertEquals(8, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 1.5), tree.getBarycenter(), TEST_EPS);

        Assert.assertEquals(16, tree.getBoundarySize(), TEST_EPS);

        List<Segment> segments = new ArrayList<>(tree.getBoundarySegments());
        Collections.sort(segments, SEGMENT_COMPARATOR);

        Assert.assertEquals(8, segments.size());

        checkFiniteSegment(segments.get(0), Vector2D.ZERO, Vector2D.of(3, 0));
        checkFiniteSegment(segments.get(1), Vector2D.of(0, 3), Vector2D.ZERO);
        checkFiniteSegment(segments.get(2), Vector2D.of(1, 1), Vector2D.of(1, 2));
        checkFiniteSegment(segments.get(3), Vector2D.of(1, 2), Vector2D.of(2, 2));
        checkFiniteSegment(segments.get(4), Vector2D.of(2, 1), Vector2D.of(1, 1));
        checkFiniteSegment(segments.get(5), Vector2D.of(2, 2), Vector2D.of(2, 1));
        checkFiniteSegment(segments.get(6), Vector2D.of(3, 0), Vector2D.of(3, 3));
        checkFiniteSegment(segments.get(7), Vector2D.of(3, 3), Vector2D.of(0, 3));

        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(2, paths.size());

        checkVertices(paths.get(0), Vector2D.ZERO, Vector2D.of(3, 0), Vector2D.of(3, 3),
                Vector2D.of(0, 3), Vector2D.ZERO);
        checkVertices(paths.get(1), Vector2D.of(1, 1), Vector2D.of(1, 2), Vector2D.of(2, 2),
                Vector2D.of(2, 1), Vector2D.of(1, 1));
    }

    @Test
    public void testGeometricProperties_complementedRegionWithHole() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.ZERO, Vector2D.of(3, 3), TEST_PRECISION);
        RegionBSPTree2D inner = RegionBSPTree2D.rect(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION);

        tree.difference(inner);

        tree.complement();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertNull(tree.getBarycenter());

        Assert.assertEquals(16, tree.getBoundarySize(), TEST_EPS);

        List<Segment> segments = new ArrayList<>(tree.getBoundarySegments());
        Collections.sort(segments, SEGMENT_COMPARATOR);

        Assert.assertEquals(8, segments.size());

        checkFiniteSegment(segments.get(0), Vector2D.ZERO, Vector2D.of(0, 3));
        checkFiniteSegment(segments.get(1), Vector2D.of(0, 3), Vector2D.of(3, 3));
        checkFiniteSegment(segments.get(2), Vector2D.of(1, 1), Vector2D.of(2, 1));
        checkFiniteSegment(segments.get(3), Vector2D.of(1, 2), Vector2D.of(1, 1));
        checkFiniteSegment(segments.get(4), Vector2D.of(2, 1), Vector2D.of(2, 2));
        checkFiniteSegment(segments.get(5), Vector2D.of(2, 2), Vector2D.of(1, 2));
        checkFiniteSegment(segments.get(6), Vector2D.of(3, 0), Vector2D.ZERO);
        checkFiniteSegment(segments.get(7), Vector2D.of(3, 3), Vector2D.of(3, 0));

        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(2, paths.size());

        checkVertices(paths.get(0), Vector2D.ZERO, Vector2D.of(0, 3), Vector2D.of(3, 3),
                Vector2D.of(3, 0), Vector2D.ZERO);
        checkVertices(paths.get(1), Vector2D.of(1, 1), Vector2D.of(2, 1), Vector2D.of(2, 2),
                Vector2D.of(1, 2), Vector2D.of(1, 1));
    }

    @Test
    public void testRect_pointArguments_lowerLeftAndUpperRight() {
        // act
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.of(1, 1), Vector2D.of(3, 2), TEST_PRECISION);

        // assert
        Assert.assertEquals(2, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1.5), tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector2D.of(0.5, 1.5), Vector2D.of(3.5, 0.5),
                Vector2D.of(2, 0.5), Vector2D.of(2, 2.5));

        checkClassify(tree, RegionLocation.INSIDE, Vector2D.of(2, 1.5));
    }

    @Test
    public void testRect_pointArguments_upperLeftAndLowerRight() {
        // act
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.of(3, 1), Vector2D.of(1, 2), TEST_PRECISION);

        // assert
        Assert.assertEquals(2, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1.5), tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector2D.of(0.5, 1.5), Vector2D.of(3.5, 0.5),
                Vector2D.of(2, 0.5), Vector2D.of(2, 2.5));

        checkClassify(tree, RegionLocation.INSIDE, Vector2D.of(2, 1.5));
    }

    @Test
    public void testRect_zeroSize() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            RegionBSPTree2D.rect(Vector2D.of(1, 1), 0, 2, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            RegionBSPTree2D.rect(Vector2D.of(1, 1), 2, 0, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            RegionBSPTree2D.rect(Vector2D.of(2, 3), 0, 0, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            RegionBSPTree2D.rect(Vector2D.of(1, 1), Vector2D.of(1, 3), TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            RegionBSPTree2D.rect(Vector2D.of(1, 1), Vector2D.of(3, 1), TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            RegionBSPTree2D.rect(Vector2D.of(2, 3), Vector2D.of(2, 3), TEST_PRECISION);
        }, GeometryValueException.class);
    }

    @Test
    public void testRect_pointAndDeltas_positiveDeltas() {
        // act
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.of(1, 1), 2, 1, TEST_PRECISION);

        // assert
        Assert.assertEquals(2, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1.5), tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector2D.of(0.5, 1.5), Vector2D.of(3.5, 0.5),
                Vector2D.of(2, 0.5), Vector2D.of(2, 2.5));

        checkClassify(tree, RegionLocation.INSIDE, Vector2D.of(2, 1.5));
    }

    @Test
    public void testRect_pointAndDeltas_negativeDeltas() {
        // act
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.of(3, 2), -2, -1, TEST_PRECISION);

        // assert
        Assert.assertEquals(2, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1.5), tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector2D.of(0.5, 1.5), Vector2D.of(3.5, 0.5),
                Vector2D.of(2, 0.5), Vector2D.of(2, 2.5));

        checkClassify(tree, RegionLocation.INSIDE, Vector2D.of(2, 1.5));
    }

    @Test
    public void testProject_fullAndEmpty() {
        // act/assert
        Assert.assertNull(RegionBSPTree2D.full().project(Vector2D.ZERO));
        Assert.assertNull(RegionBSPTree2D.empty().project(Vector2D.of(1, 2)));
    }

    @Test
    public void testProject_halfSpace() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.full();
        tree.getRoot().cut(X_AXIS);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, tree.project(Vector2D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 0), tree.project(Vector2D.of(-1, 0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 0),
                tree.project(Vector2D.of(2, -1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 0),
                tree.project(Vector2D.of(-3, 1)), TEST_EPS);
    }

    @Test
    public void testProject_rect() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.of(1, 1), 1, 1, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), tree.project(Vector2D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), tree.project(Vector2D.of(1, 0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 1), tree.project(Vector2D.of(1.5, 0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), tree.project(Vector2D.of(2, 0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), tree.project(Vector2D.of(3, 0)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), tree.project(Vector2D.of(1, 3)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), tree.project(Vector2D.of(1, 3)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 2), tree.project(Vector2D.of(1.5, 3)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 2), tree.project(Vector2D.of(2, 3)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 2), tree.project(Vector2D.of(3, 3)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1.5), tree.project(Vector2D.of(0, 1.5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1.5), tree.project(Vector2D.of(1.5, 1.5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1.5), tree.project(Vector2D.of(3, 1.5)), TEST_EPS);
    }

    @Test
    public void testTransform() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.of(1, 1), 2, 1, TEST_PRECISION);

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(0.5, 2)
                .rotate(Geometry.HALF_PI)
                .translate(Vector2D.of(0, -1));

        // act
        tree.transform(transform);

        // assert
        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertEquals(4, path.getSegments().size());
        checkFiniteSegment(path.getSegments().get(0), Vector2D.of(-4, -0.5), Vector2D.of(-2, -0.5));
        checkFiniteSegment(path.getSegments().get(1), Vector2D.of(-2, -0.5), Vector2D.of(-2, 0.5));
        checkFiniteSegment(path.getSegments().get(2), Vector2D.of(-2, 0.5), Vector2D.of(-4, 0.5));
        checkFiniteSegment(path.getSegments().get(3), Vector2D.of(-4, 0.5), Vector2D.of(-4, -0.5));
    }

    @Test
    public void testTransform_halfSpace() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.getRoot().insertCut(Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.ZERO_PI, TEST_PRECISION));

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(0.5, 2)
                .rotate(Geometry.HALF_PI)
                .translate(Vector2D.of(1, 0));

        // act
        tree.transform(transform);

        // assert
        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertEquals(1, path.getSegments().size());
        Segment segment = path.getStartSegment();
        Assert.assertNull(segment.getStartPoint());
        Assert.assertNull(segment.getEndPoint());

        Line expectedLine = Line.fromPointAndAngle(Vector2D.of(-1, 0), Geometry.HALF_PI, TEST_PRECISION);
        Assert.assertTrue(expectedLine.eq(segment.getLine()));
    }

    @Test
    public void testTransform_fullAndEmpty() {
        // arrange
        RegionBSPTree2D full = RegionBSPTree2D.full();
        RegionBSPTree2D empty = RegionBSPTree2D.empty();

        AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(Geometry.HALF_PI);

        // act
        full.transform(transform);
        empty.transform(transform);

        // assert
        Assert.assertTrue(full.isFull());
        Assert.assertTrue(empty.isEmpty());
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.of(1, 1), 1, 1, TEST_PRECISION);

        Transform<Vector2D> transform = v -> Vector2D.of(-v.getX(), v.getY());

        // act
        tree.transform(transform);

        // assert
        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertEquals(4, path.getSegments().size());
        checkFiniteSegment(path.getSegments().get(0), Vector2D.of(-2, 1), Vector2D.of(-1, 1));
        checkFiniteSegment(path.getSegments().get(1), Vector2D.of(-1, 1), Vector2D.of(-1, 2));
        checkFiniteSegment(path.getSegments().get(2), Vector2D.of(-1, 2), Vector2D.of(-2, 2));
        checkFiniteSegment(path.getSegments().get(3), Vector2D.of(-2, 2), Vector2D.of(-2, 1));
    }

    @Test
    public void testTransform_doubleReflection() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.of(1, 1), 1, 1, TEST_PRECISION);

        Transform<Vector2D> transform = Vector2D::negate;

        // act
        tree.transform(transform);

        // assert
        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertEquals(4, path.getSegments().size());
        checkFiniteSegment(path.getSegments().get(0), Vector2D.of(-2, -2), Vector2D.of(-1, -2));
        checkFiniteSegment(path.getSegments().get(1), Vector2D.of(-1, -2), Vector2D.of(-1, -1));
        checkFiniteSegment(path.getSegments().get(2), Vector2D.of(-1, -1), Vector2D.of(-2, -1));
        checkFiniteSegment(path.getSegments().get(3), Vector2D.of(-2, -1), Vector2D.of(-2, -2));
    }

    @Test
    public void testBooleanOperations() {
        // arrange
        RegionBSPTree2D tree = RegionBSPTree2D.rect(Vector2D.ZERO, 3, 3, TEST_PRECISION);
        RegionBSPTree2D temp;

        // act
        temp = RegionBSPTree2D.rect(Vector2D.of(1, 1), 1, 1, TEST_PRECISION);
        temp.complement();
        tree.intersection(temp);

        temp = RegionBSPTree2D.rect(Vector2D.of(3, 0), 3, 3, TEST_PRECISION);
        tree.union(temp);

        temp = RegionBSPTree2D.rect(Vector2D.of(2, 1), 3, 1, TEST_PRECISION);
        tree.difference(temp);

        temp.setFull();
        tree.xor(temp);

        // assert
        List<SegmentPath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(2, paths.size());

        checkVertices(paths.get(0), Vector2D.ZERO, Vector2D.of(0, 3), Vector2D.of(6, 3),
                Vector2D.of(6, 0), Vector2D.ZERO);

        checkVertices(paths.get(1), Vector2D.of(1, 1), Vector2D.of(5, 1), Vector2D.of(5, 2),
                Vector2D.of(1, 2), Vector2D.of(1, 1));
    }

    private static void checkFiniteSegment(Segment segment, Vector2D start, Vector2D end) {
        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }

    private static void checkClassify(Region<Vector2D> region, RegionLocation loc, Vector2D ... points) {
        for (Vector2D point : points) {
            String msg = "Unexpected location for point " + point;

            Assert.assertEquals(msg, loc, region.classify(point));
        }
    }

    private static void checkConvexArea(final ConvexArea area, final List<Vector2D> inside, final List<Vector2D> outside) {
        checkClassify(area, RegionLocation.INSIDE, inside.toArray(new Vector2D[0]));
        checkClassify(area, RegionLocation.OUTSIDE, outside.toArray(new Vector2D[0]));
    }

    /** Assert that the given path is finite and contains the given vertices.
     * @param path
     * @param vertices
     */
    private static void checkVertices(SegmentPath path, Vector2D ... vertices) {
        Assert.assertTrue("Line segment path is not finite", path.isFinite());

        List<Vector2D> actual = path.getVertices();

        Assert.assertEquals("Vertex lists have different lengths", vertices.length, actual.size());

        for (int i=0; i<vertices.length; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(vertices[i], actual.get(i), TEST_EPS);
        }
    }
}
