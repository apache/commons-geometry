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
package org.apache.commons.geometry.euclidean.twod.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.LinecastChecker2D;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Ray;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.ReverseRay;
import org.apache.commons.geometry.euclidean.twod.Segment;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath.Builder;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class LinePathTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFrom_empty() {
        // act
        LinePath path = LinePath.from(new ArrayList<>());

        // assert
        Assert.assertTrue(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertEquals(0, path.getSize(), TEST_EPS);

        Assert.assertNull(path.getStart());
        Assert.assertNull(path.getEnd());

        Assert.assertEquals(0, path.getElements().size());

        Assert.assertEquals(0, path.getVertexSequence().size());
    }

    @Test
    public void testFrom_singleFiniteSegment() {
        // arrange
        Segment a = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        // act
        LinePath path = LinePath.from(a);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertEquals(1, path.getSize(), TEST_EPS);

        Assert.assertSame(a, path.getStart());
        Assert.assertSame(a, path.getEnd());

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(a, segments.get(0));

        Assert.assertEquals(Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0)), path.getVertexSequence());
    }

    @Test
    public void testFrom_singleInfiniteSegment() {
        // arrange
        LineConvexSubset a = Lines.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION).span();

        // act
        LinePath path = LinePath.from(a);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        GeometryTestUtils.assertPositiveInfinity(path.getSize());

        Assert.assertSame(a, path.getStart());
        Assert.assertSame(a, path.getEnd());

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(a, segments.get(0));

        Assert.assertEquals(0, path.getVertexSequence().size());
    }

    @Test
    public void testFrom_finiteSegments_notClosed() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);

        Segment a = Lines.segmentFromPoints(p1, p2, TEST_PRECISION);
        Segment b = Lines.segmentFromPoints(p2, p3, TEST_PRECISION);

        // act
        LinePath path = LinePath.from(a, b);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertEquals(2, path.getSize(), TEST_EPS);

        Assert.assertSame(a, path.getStart());
        Assert.assertSame(b, path.getEnd());

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(2, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));

        Assert.assertEquals(Arrays.asList(p1, p2, p3), path.getVertexSequence());
    }

    @Test
    public void testFrom_finiteSegments_closed() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);

        Segment a = Lines.segmentFromPoints(p1, p2, TEST_PRECISION);
        Segment b = Lines.segmentFromPoints(p2, p3, TEST_PRECISION);
        Segment c = Lines.segmentFromPoints(p3, p1, TEST_PRECISION);

        // act
        LinePath path = LinePath.from(Arrays.asList(a, b, c));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertTrue(path.isClosed());

        Assert.assertSame(a, path.getStart());
        Assert.assertSame(c, path.getEnd());

        Assert.assertEquals(2 + Math.sqrt(2), path.getSize(), TEST_EPS);

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(3, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));
        Assert.assertSame(c, segments.get(2));

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p1), path.getVertexSequence());
    }

    @Test
    public void testFrom_infiniteSegments() {
        // arrange
        ReverseRay a = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION)
                .reverseRayTo(1.0);
        Ray b = Lines.fromPointAndAngle(Vector2D.of(1, 0), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
                .rayFrom(0.0);

        // act
        LinePath path = LinePath.from(Arrays.asList(a, b));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        GeometryTestUtils.assertPositiveInfinity(path.getSize());

        Assert.assertSame(a, path.getStart());
        Assert.assertSame(b, path.getEnd());

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(2, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));

        Assert.assertEquals(Arrays.asList(Vector2D.of(1, 0)), path.getVertexSequence());
    }

    @Test
    public void testFrom_finiteAndInfiniteSegments_startInfinite() {
        // arrange
        ReverseRay a = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION).reverseRayTo(1.0);
        Segment b = Lines.segmentFromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LinePath path = LinePath.from(Arrays.asList(a, b));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStart());
        Assert.assertSame(b, path.getEnd());

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(2, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));

        Assert.assertEquals(Arrays.asList(Vector2D.of(1, 0), Vector2D.of(1, 1)), path.getVertexSequence());
    }

    @Test
    public void testFrom_finiteAndInfiniteSegments_endInfinite() {
        // arrange
        Segment a = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);
        Ray b = Lines.fromPointAndAngle(Vector2D.of(1, 0), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
                .rayFrom(0.0);

        // act
        LinePath path = LinePath.from(Arrays.asList(a, b));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStart());
        Assert.assertSame(b, path.getEnd());

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(2, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));

        Assert.assertEquals(Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0)), path.getVertexSequence());
    }

    @Test
    public void testFrom_segmentsNotConnected() {
        // arrange
        Segment a = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);
        Segment b = Lines.segmentFromPoints(Vector2D.of(1.01, 0), Vector2D.of(1, 0), TEST_PRECISION);

        LineConvexSubset c = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION).span();
        LineConvexSubset d = Lines.fromPointAndAngle(Vector2D.of(1, 0), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION).span();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            LinePath.from(a, b);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            LinePath.from(c, b);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            LinePath.from(a, d);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertices_empty() {
        // act
        LinePath path = LinePath.fromVertices(new ArrayList<>(), TEST_PRECISION);

        // assert
        Assert.assertTrue(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertNull(path.getStart());
        Assert.assertNull(path.getEnd());

        Assert.assertEquals(0, path.getElements().size());

        Assert.assertEquals(0, path.getVertexSequence().size());
    }

    @Test
    public void testFromVertices_singleVertex_failsToCreatePath() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            LinePath.fromVertices(Arrays.asList(Vector2D.ZERO), TEST_PRECISION);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertices_twoVertices() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);

        // act
        LinePath path = LinePath.fromVertices(Arrays.asList(p1, p2), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        assertFiniteSegment(path.getStart(), p1, p2);
        Assert.assertSame(path.getStart(), path.getEnd());

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(1, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);

        Assert.assertEquals(Arrays.asList(p1, p2), path.getVertexSequence());
    }

    @Test
    public void testFromVertices_multipleVertices_notClosed() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(0, 1);

        // act
        LinePath path = LinePath.fromVertices(Arrays.asList(p1, p2, p3, p4), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        assertFiniteSegment(path.getStart(), p1, p2);
        assertFiniteSegment(path.getEnd(), p3, p4);

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p4);

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p4), path.getVertexSequence());
    }

    @Test
    public void testFromVertices_multipleVertices_closed() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(0, 1);

        // act
        LinePath path = LinePath.fromVertices(Arrays.asList(p1, p2, p3, p4, p1), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertTrue(path.isClosed());

        assertFiniteSegment(path.getStart(), p1, p2);
        assertFiniteSegment(path.getEnd(), p4, p1);

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(4, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p4);
        assertFiniteSegment(segments.get(3), p4, p1);

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p4, p1), path.getVertexSequence());
    }

    @Test
    public void testFromVertexLoop_empty() {
        // act
        LinePath path = LinePath.fromVertexLoop(new ArrayList<>(), TEST_PRECISION);

        // assert
        Assert.assertTrue(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertNull(path.getStart());
        Assert.assertNull(path.getEnd());

        Assert.assertEquals(0, path.getElements().size());

        Assert.assertEquals(0, path.getVertexSequence().size());
    }

    @Test
    public void testFromVertexLoop_singleVertex_failsToCreatePath() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            LinePath.fromVertexLoop(Arrays.asList(Vector2D.ZERO), TEST_PRECISION);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertexLoop_closeRequired() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);

        // act
        LinePath path = LinePath.fromVertexLoop(Arrays.asList(p1, p2, p3), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertTrue(path.isClosed());

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p1);

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p1), path.getVertexSequence());
    }

    @Test
    public void testFromVertexLoop_closeNotRequired() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);

        // act
        LinePath path = LinePath.fromVertexLoop(Arrays.asList(p1, p2, p3, Vector2D.of(0, 0)), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertTrue(path.isClosed());

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p1);

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p1), path.getVertexSequence());
    }

    @Test
    public void testFromVertices_booleanArg() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(0, 1);

        // act
        LinePath open = LinePath.fromVertices(Arrays.asList(p1, p2, p3), false, TEST_PRECISION);
        LinePath closed = LinePath.fromVertices(Arrays.asList(p1, p2, p3), true, TEST_PRECISION);

        // assert
        Assert.assertFalse(open.isClosed());

        List<LineConvexSubset> openSegments = open.getElements();
        Assert.assertEquals(2, openSegments.size());
        assertFiniteSegment(openSegments.get(0), p1, p2);
        assertFiniteSegment(openSegments.get(1), p2, p3);

        Assert.assertTrue(closed.isClosed());

        List<LineConvexSubset> closedSegments = closed.getElements();
        Assert.assertEquals(3, closedSegments.size());
        assertFiniteSegment(closedSegments.get(0), p1, p2);
        assertFiniteSegment(closedSegments.get(1), p2, p3);
        assertFiniteSegment(closedSegments.get(2), p3, p1);
    }

    @Test
    public void testGetElements_listIsNotModifiable() {
        // arrange
        Segment a = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);
        List<LineConvexSubset> inputSegments = new ArrayList<>(Arrays.asList(a));

        // act
        LinePath path = LinePath.from(inputSegments);

        inputSegments.clear();

        // assert
        Assert.assertNotSame(inputSegments, path.getElements());
        Assert.assertEquals(1, path.getElements().size());

        GeometryTestUtils.assertThrows(() -> {
            path.getElements().add(a);
        }, UnsupportedOperationException.class);
    }

    @Test
    public void testBoundaryStream() {
        // arrange
        Segment seg = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);
        LinePath path = LinePath.from(Arrays.asList(seg));

        // act
        List<LineConvexSubset> segments = path.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(seg, segments.get(0));
    }

    @Test
    public void testBoundaryStream_empty() {
        // arrange
        LinePath path = LinePath.empty();

        // act
        List<LineConvexSubset> segments = path.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testTransform_empty() {
        // arrange
        LinePath path = LinePath.empty();
        AffineTransformMatrix2D t = AffineTransformMatrix2D.createTranslation(Vector2D.Unit.PLUS_X);

        // act/assert
        Assert.assertSame(path, path.transform(t));
    }

    @Test
    public void testTransform_finite() {
        // arrange
        LinePath path = LinePath.builder(TEST_PRECISION)
                .append(Vector2D.Unit.ZERO)
                .append(Vector2D.Unit.PLUS_X)
                .append(Vector2D.Unit.PLUS_Y)
                .close();

        AffineTransformMatrix2D t =
                AffineTransformMatrix2D.createRotation(Vector2D.of(1, 1), PlaneAngleRadians.PI_OVER_TWO);

        // act
        LinePath result = path.transform(t);

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertTrue(result.isClosed());
        Assert.assertTrue(result.isFinite());

        List<LineConvexSubset> segments = result.getElements();

        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), Vector2D.of(2, 0), Vector2D.of(2, 1));
        assertFiniteSegment(segments.get(1), Vector2D.of(2, 1), Vector2D.Unit.PLUS_X);
        assertFiniteSegment(segments.get(2), Vector2D.Unit.PLUS_X, Vector2D.of(2, 0));
    }

    @Test
    public void testTransform_infinite() {
        // arrange
        LinePath path = LinePath.from(
                Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION).span());

        AffineTransformMatrix2D t = AffineTransformMatrix2D.createTranslation(Vector2D.Unit.PLUS_X);

        // act
        LinePath result = path.transform(t);

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertFalse(result.isFinite());

        List<LineConvexSubset> segments = result.getElements();

        Assert.assertEquals(1, segments.size());
        LineConvexSubset segment = segments.get(0);
        Assert.assertTrue(segment.isInfinite());
        Assert.assertNull(segment.getStartPoint());
        Assert.assertNull(segment.getEndPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_X, segment.getLine().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_Y, segment.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testReverse_empty() {
        // arrange
        LinePath path = LinePath.empty();

        // act/assert
        Assert.assertSame(path, path.reverse());
    }

    @Test
    public void testReverse() {
        // arrange
        LinePath path = LinePath.builder(TEST_PRECISION)
                .append(Vector2D.Unit.ZERO)
                .append(Vector2D.Unit.PLUS_X)
                .append(Vector2D.Unit.PLUS_Y)
                .close();

        // act
        LinePath result = path.reverse();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertTrue(result.isClosed());
        Assert.assertTrue(result.isFinite());

        List<LineConvexSubset> segments = result.getElements();

        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), Vector2D.Unit.ZERO, Vector2D.Unit.PLUS_Y);
        assertFiniteSegment(segments.get(1), Vector2D.Unit.PLUS_Y, Vector2D.Unit.PLUS_X);
        assertFiniteSegment(segments.get(2), Vector2D.Unit.PLUS_X, Vector2D.Unit.ZERO);
    }

    @Test
    public void testReverse_singleInfinite() {
        // arrange
        LinePath path = LinePath.from(
                Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION).span());

        // act
        LinePath result = path.reverse();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertFalse(result.isFinite());

        List<LineConvexSubset> segments = result.getElements();

        Assert.assertEquals(1, segments.size());
        LineConvexSubset segment = segments.get(0);
        Assert.assertTrue(segment.isInfinite());
        Assert.assertNull(segment.getStartPoint());
        Assert.assertNull(segment.getEndPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.ZERO, segment.getLine().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_Y, segment.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testReverse_doubleInfinite() {
        // arrange
        LineConvexSubset a = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION).reverseRayTo(Vector2D.ZERO);
        LineConvexSubset b = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).rayFrom(Vector2D.ZERO);

        LinePath path = LinePath.from(a, b);

        // act
        LinePath result = path.reverse();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertFalse(result.isFinite());

        List<LineConvexSubset> segments = result.getElements();
        Assert.assertEquals(2, segments.size());

        LineConvexSubset bResult = segments.get(0);
        Assert.assertTrue(bResult.isInfinite());
        Assert.assertNull(bResult.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, bResult.getEndPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, bResult.getLine().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_X, bResult.getLine().getDirection(), TEST_EPS);

        LineConvexSubset aResult = segments.get(1);
        Assert.assertTrue(aResult.isInfinite());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, aResult.getStartPoint(), TEST_EPS);
        Assert.assertNull(aResult.getEndPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, aResult.getLine().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_Y, aResult.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testToTree() {
        // arrange
        LinePath path = LinePath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, 1), Vector2D.of(0, 1))
                .close();

        // act
        RegionBSPTree2D tree = path.toTree();

        // assert
        Assert.assertEquals(1, tree.getSize(), TEST_EPS);
        Assert.assertEquals(4, tree.getBoundarySize(), TEST_EPS);

        Assert.assertEquals(RegionLocation.INSIDE, tree.classify(Vector2D.of(0.5, 0.5)));

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(Vector2D.of(0.5, -1)));
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(Vector2D.of(0.5, 2)));
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(Vector2D.of(-1, 0.5)));
        Assert.assertEquals(RegionLocation.OUTSIDE, tree.classify(Vector2D.of(2, 0.5)));
    }

    @Test
    public void testSimplify() {
        // arrange
        Builder builder = LinePath.builder(TEST_PRECISION);

        LinePath path = builder.appendVertices(
                Vector2D.of(-1, 0),
                Vector2D.ZERO,
                Vector2D.of(1, 0),
                Vector2D.of(1, 1),
                Vector2D.of(1, 2))
            .build();

        // act
        LinePath result = path.simplify();

        // assert
        List<LineConvexSubset> segments = result.getElements();
        Assert.assertEquals(2, segments.size());
        assertFiniteSegment(segments.get(0), Vector2D.of(-1, 0), Vector2D.of(1, 0));
        assertFiniteSegment(segments.get(1), Vector2D.of(1, 0), Vector2D.of(1, 2));
    }

    @Test
    public void testSimplify_startAndEndCombined() {
        // arrange
        Builder builder = LinePath.builder(TEST_PRECISION);

        LinePath path = builder.appendVertices(
                Vector2D.ZERO,
                Vector2D.of(1, 0),
                Vector2D.of(0, 1),
                Vector2D.of(-1, 0))
            .close();

        // act
        LinePath result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertTrue(result.isClosed());
        Assert.assertFalse(result.isInfinite());

        List<LineConvexSubset> segments = result.getElements();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), Vector2D.of(-1, 0), Vector2D.of(1, 0));
        assertFiniteSegment(segments.get(1), Vector2D.of(1, 0), Vector2D.of(0, 1));
        assertFiniteSegment(segments.get(2), Vector2D.of(0, 1), Vector2D.of(-1, 0));
    }

    @Test
    public void testSimplify_empty() {
        // arrange
        Builder builder = LinePath.builder(TEST_PRECISION);

        LinePath path = builder.build();

        // act
        LinePath result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertFalse(result.isInfinite());

        List<LineConvexSubset> segments = result.getElements();
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testSimplify_infiniteSegment() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);

        Builder builder = LinePath.builder(TEST_PRECISION);
        LinePath path = builder
                .append(line.span())
                .build();

        // act
        LinePath result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertTrue(result.isInfinite());

        Assert.assertNotNull(path.getStart());
        Assert.assertNotNull(path.getEnd());
        Assert.assertSame(path.getStart(), path.getEnd());

        List<LineConvexSubset> segments = result.getElements();
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(line, segments.get(0).getLine());
    }

    @Test
    public void testSimplify_combinedInfiniteSegment() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        Split<LineConvexSubset> split = line.span().split(
                Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION));

        Builder builder = LinePath.builder(TEST_PRECISION);
        LinePath path = builder
                .append(split.getMinus())
                .append(split.getPlus())
                .build();

        // act
        LinePath result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertTrue(result.isInfinite());

        Assert.assertNotNull(result.getStart());
        Assert.assertNotNull(result.getEnd());
        Assert.assertSame(result.getStart(), result.getEnd());

        List<LineConvexSubset> segments = result.getElements();
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(line, segments.get(0).getLine());
    }

    @Test
    public void testSimplify_startAndEndNotCombinedWhenNotClosed() {
        // arrange
        Line xAxis = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        Builder builder = LinePath.builder(TEST_PRECISION);

        LinePath path = builder
                .append(xAxis.segment(0, 1))
                .appendVertices(
                        Vector2D.of(2, 1),
                        Vector2D.of(3, 0))
                .append(xAxis.segment(3, 4))
            .build();

        // act
        LinePath result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertFalse(result.isInfinite());

        List<LineConvexSubset> segments = result.getElements();
        Assert.assertEquals(4, segments.size());
        assertFiniteSegment(segments.get(0), Vector2D.ZERO, Vector2D.of(1, 0));
        assertFiniteSegment(segments.get(1), Vector2D.of(1, 0), Vector2D.of(2, 1));
        assertFiniteSegment(segments.get(2), Vector2D.of(2, 1), Vector2D.of(3, 0));
        assertFiniteSegment(segments.get(3), Vector2D.of(3, 0), Vector2D.of(4, 0));
    }

    @Test
    public void testSimplify_subsequentCallsToReturnedObjectReturnSameObject() {
        // arrange
        Builder builder = LinePath.builder(TEST_PRECISION);
        LinePath path = builder.appendVertices(
                    Vector2D.ZERO,
                    Vector2D.of(1, 0),
                    Vector2D.of(2, 0))
                .build();

        // act
        LinePath result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertSame(result, result.simplify());
    }

    @Test
    public void testLinecast_empty() {
        // arrange
        LinePath path = LinePath.empty();

        // act/assert
        LinecastChecker2D.with(path)
            .expectNothing()
            .whenGiven(Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));

        LinecastChecker2D.with(path)
            .expectNothing()
            .whenGiven(Lines.segmentFromPoints(Vector2D.Unit.MINUS_X, Vector2D.Unit.PLUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast() {
        // arrange
        LinePath path = LinePath.fromVertexLoop(Arrays.asList(
                    Vector2D.ZERO, Vector2D.of(1, 0),
                    Vector2D.of(1, 1), Vector2D.of(0, 1)
                ), TEST_PRECISION);

        // act/assert
        LinecastChecker2D.with(path)
            .expectNothing()
            .whenGiven(Lines.fromPoints(Vector2D.of(0, 5), Vector2D.of(1, 6), TEST_PRECISION));

        LinecastChecker2D.with(path)
            .expect(Vector2D.ZERO, Vector2D.Unit.MINUS_X)
            .and(Vector2D.ZERO, Vector2D.Unit.MINUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Lines.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION));

        LinecastChecker2D.with(path)
            .expect(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Lines.segmentFromPoints(Vector2D.of(0.5, 0.5), Vector2D.of(1, 1), TEST_PRECISION));
    }

    @Test
    public void testToString() {
        // arrange
        Line yAxis = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        Line xAxis = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        LinePath empty = LinePath.empty();

        LinePath singleFullSegment = LinePath.from(xAxis.span());
        LinePath singleFiniteSegment = LinePath.from(
                Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));

        LinePath startOpenPath = LinePath.builder(TEST_PRECISION)
                .append(xAxis.reverseRayTo(Vector2D.Unit.PLUS_X))
                .append(Vector2D.of(1, 1))
                .build();

        LinePath endOpenPath = LinePath.builder(TEST_PRECISION)
                .append(Vector2D.of(0, 1))
                .append(Vector2D.ZERO)
                .append(xAxis.rayFrom(Vector2D.ZERO))
                .build();

        LinePath doubleOpenPath = LinePath.from(yAxis.reverseRayTo(Vector2D.ZERO),
                xAxis.rayFrom(Vector2D.ZERO));

        LinePath nonOpenPath = LinePath.builder(TEST_PRECISION)
                .append(Vector2D.ZERO)
                .append(Vector2D.Unit.PLUS_X)
                .append(Vector2D.of(1, 1))
                .build();

        // act/assert
        String emptyStr = empty.toString();
        GeometryTestUtils.assertContains("LinePath[empty= true", emptyStr);

        String singleFullStr = singleFullSegment.toString();
        GeometryTestUtils.assertContains("LinePath[single= LineSpanningSubset[", singleFullStr);

        String singleFiniteStr = singleFiniteSegment.toString();
        GeometryTestUtils.assertContains("LinePath[single= Segment[", singleFiniteStr);

        String startOpenStr = startOpenPath.toString();
        GeometryTestUtils.assertContains("LinePath[startDirection= ", startOpenStr);
        GeometryTestUtils.assertContains("vertexSequence=", startOpenStr);

        String endOpenStr = endOpenPath.toString();
        GeometryTestUtils.assertContains("LinePath[vertexSequence= ", endOpenStr);
        GeometryTestUtils.assertContains("endDirection= ", endOpenStr);

        String doubleOpenStr = doubleOpenPath.toString();
        GeometryTestUtils.assertContains("startDirection= ", doubleOpenStr);
        GeometryTestUtils.assertContains("vertexSequence= ", doubleOpenStr);
        GeometryTestUtils.assertContains("endDirection= ", doubleOpenStr);

        String nonOpenStr = nonOpenPath.toString();
        GeometryTestUtils.assertContains("LinePath[vertexSequence= ", nonOpenStr);
    }

    @Test
    public void testBuilder_prependAndAppend_segments() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(1, 0);

        Segment a = Lines.segmentFromPoints(p1, p2, TEST_PRECISION);
        Segment b = Lines.segmentFromPoints(p2, p3, TEST_PRECISION);
        Segment c = Lines.segmentFromPoints(p3, p4, TEST_PRECISION);
        Segment d = Lines.segmentFromPoints(p4, p1, TEST_PRECISION);

        Builder builder = LinePath.builder(null);

        // act
        builder.prepend(b)
            .append(c)
            .prepend(a)
            .append(d);

        LinePath path = builder.build();

        // assert
        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(4, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));
        Assert.assertSame(c, segments.get(2));
        Assert.assertSame(d, segments.get(3));
    }

    @Test
    public void testBuilder_prependAndAppend_disconnectedSegments() {
        // arrange
        Segment a = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        Builder builder = LinePath.builder(null);
        builder.append(a);

        // act
        GeometryTestUtils.assertThrows(() -> {
            builder.append(a);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            builder.prepend(a);
        }, IllegalStateException.class);
    }

    @Test
    public void testBuilder_prependAndAppend_vertices() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(1, 0);

        Builder builder = LinePath.builder(TEST_PRECISION);

        // act
        builder.prepend(p2)
            .append(p3)
            .prepend(p1)
            .append(p4)
            .append(p1);

        LinePath path = builder.build();

        // assert
        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(4, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p4);
        assertFiniteSegment(segments.get(3), p4, p1);
    }

    @Test
    public void testBuilder_prependAndAppend_noPrecisionSpecified() {
        // arrange
        Vector2D p = Vector2D.ZERO;
        Builder builder = LinePath.builder(null);

        String msg = "Unable to create line segment: no vertex precision specified";

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.append(p);
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            builder.prepend(p);
        }, IllegalStateException.class, msg);
    }

    @Test
    public void testBuilder_prependAndAppend_addingToInfinitePath() {
        // arrange
        Vector2D p = Vector2D.Unit.PLUS_X;
        Builder builder = LinePath.builder(TEST_PRECISION);

        builder.append(Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION).span());

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.append(p);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            builder.prepend(p);
        }, IllegalStateException.class);
    }

    @Test
    public void testBuilder_prependAndAppend_ignoresEquivalentVertices() {
        // arrange
        Vector2D p = Vector2D.ZERO;

        Builder builder = LinePath.builder(TEST_PRECISION);
        builder.append(p);

        // act
        builder.append(p)
            .prepend(p)
            .append(Vector2D.of(0, 1e-20))
            .prepend(Vector2D.of(1e-20, 0));

        builder.append(Vector2D.Unit.PLUS_X);

        // assert
        LinePath path = builder.build();

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(1, segments.size());
        assertFiniteSegment(segments.get(0), p, Vector2D.Unit.PLUS_X);
    }

    @Test
    public void testBuilder_prependAndAppend_mixedVerticesAndSegments() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(0, 1);

        Segment a = Lines.segmentFromPoints(p1, p2, TEST_PRECISION);
        Segment c = Lines.segmentFromPoints(p3, p4, TEST_PRECISION);

        Builder builder = LinePath.builder(TEST_PRECISION);

        // act
        builder.prepend(p2)
            .append(p3)
            .append(c)
            .prepend(a)
            .append(p1);

        LinePath path = builder.build();

        // assert
        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(4, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p4);
        assertFiniteSegment(segments.get(3), p4, p1);
    }

    @Test
    public void testBuilder_appendVertices() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(0, 1);

        Builder builder = LinePath.builder(TEST_PRECISION);

        // act
        builder.appendVertices(p1, p2)
            .appendVertices(Arrays.asList(p3, p4, p1));

        LinePath path = builder.build();

        // assert
        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(4, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p4);
        assertFiniteSegment(segments.get(3), p4, p1);
    }

    @Test
    public void testBuilder_prependVertices() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(0, 1);

        Builder builder = LinePath.builder(TEST_PRECISION);

        // act
        builder.prependVertices(p3, p4, p1)
            .prependVertices(Arrays.asList(p1, p2));

        LinePath path = builder.build();

        // assert
        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(4, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p4);
        assertFiniteSegment(segments.get(3), p4, p1);
    }

    @Test
    public void testBuilder_close_notYetClosed() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);

        Builder builder = LinePath.builder(TEST_PRECISION);

        // act
        builder.append(p1)
            .append(p2)
            .append(p3);

        LinePath path = builder.close();

        // assert
        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p1);
    }

    @Test
    public void testBuilder_close_alreadyClosed() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);

        Builder builder = LinePath.builder(TEST_PRECISION);

        // act
        builder.append(p1)
            .append(p2)
            .append(p3)
            .append(p1);

        LinePath path = builder.close();

        // assert
        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p1);
    }

    @Test
    public void testBuilder_close_infiniteSegmentAtStart() {
        // arrange
        Builder builder = LinePath.builder(TEST_PRECISION);

        builder.append(Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION)
                .reverseRayTo(1))
            .append(Vector2D.of(1, 1));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.close();
        }, IllegalStateException.class, "Unable to close line path: line path is infinite");
    }

    @Test
    public void testBuilder_close_infiniteSegmentAtEnd() {
        // arrange
        Builder builder = LinePath.builder(TEST_PRECISION);

        builder
            .append(Vector2D.ZERO)
            .append(Vector2D.Unit.PLUS_X)
            .append(Lines.fromPointAndAngle(Vector2D.Unit.PLUS_X, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
                .rayFrom(0));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.close();
        }, IllegalStateException.class, "Unable to close line path: line path is infinite");
    }

    @Test
    public void testBuilder_close_emptyPath() {
        // arrange
        Builder builder = LinePath.builder(TEST_PRECISION);

        // act
        LinePath path = builder.close();

        // assert
        Assert.assertEquals(0, path.getElements().size());
    }

    @Test
    public void testBuilder_close_obtuseTriangle() {
        // arrange
        Builder builder = LinePath.builder(TEST_PRECISION);
        builder.appendVertices(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(2, 1));

        // act
        LinePath path = builder.close();

        // assert
        Assert.assertEquals(3, path.getElements().size());
        assertFiniteSegment(path.getElements().get(0), Vector2D.ZERO, Vector2D.of(1, 0));
        assertFiniteSegment(path.getElements().get(1), Vector2D.of(1, 0), Vector2D.of(2, 1));
        assertFiniteSegment(path.getElements().get(2), Vector2D.of(2, 1), Vector2D.ZERO);
    }

    private static void assertFiniteSegment(LineConvexSubset segment, Vector2D start, Vector2D end) {
        Assert.assertFalse(segment.isInfinite());
        Assert.assertTrue(segment.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }
}
