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
import java.util.List;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.Polyline.Builder;
import org.junit.Assert;
import org.junit.Test;

public class PolylineTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromSegments_empty() {
        // act
        Polyline path = Polyline.fromSegments(new ArrayList<>());

        // assert
        Assert.assertTrue(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertNull(path.getStartSegment());
        Assert.assertNull(path.getStartVertex());

        Assert.assertNull(path.getEndSegment());
        Assert.assertNull(path.getEndVertex());

        Assert.assertEquals(0, path.getSegments().size());

        Assert.assertEquals(0, path.getVertices().size());
    }

    @Test
    public void testFromSegments_singleFiniteSegment() {
        // arrange
        Segment a = Segment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        // act
        Polyline path = Polyline.fromSegments(a);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStartSegment());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, path.getStartVertex(), TEST_EPS);

        Assert.assertSame(a, path.getEndSegment());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 0), path.getEndVertex(), TEST_EPS);

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(a, segments.get(0));

        Assert.assertEquals(Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0)), path.getVertices());
    }

    @Test
    public void testFromSegments_singleInfiniteSegment() {
        // arrange
        Segment a = Line.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION).span();

        // act
        Polyline path = Polyline.fromSegments(a);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStartSegment());
        Assert.assertNull(path.getStartVertex());

        Assert.assertSame(a, path.getEndSegment());
        Assert.assertNull(path.getEndVertex());

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(a, segments.get(0));

        Assert.assertEquals(0, path.getVertices().size());
    }

    @Test
    public void testFromSegments_finiteSegments_notClosed() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);

        Segment a = Segment.fromPoints(p1, p2, TEST_PRECISION);
        Segment b = Segment.fromPoints(p2, p3, TEST_PRECISION);

        // act
        Polyline path = Polyline.fromSegments(a, b);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStartSegment());
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getStartVertex(), TEST_EPS);

        Assert.assertSame(b, path.getEndSegment());
        EuclideanTestUtils.assertCoordinatesEqual(p3, path.getEndVertex(), TEST_EPS);

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(2, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));

        Assert.assertEquals(Arrays.asList(p1, p2, p3), path.getVertices());
    }

    @Test
    public void testFromSegments_finiteSegments_closed() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);

        Segment a = Segment.fromPoints(p1, p2, TEST_PRECISION);
        Segment b = Segment.fromPoints(p2, p3, TEST_PRECISION);
        Segment c = Segment.fromPoints(p3, p1, TEST_PRECISION);

        // act
        Polyline path = Polyline.fromSegments(Arrays.asList(a, b, c));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertTrue(path.isClosed());

        Assert.assertSame(a, path.getStartSegment());
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getStartVertex(), TEST_EPS);

        Assert.assertSame(c, path.getEndSegment());
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getEndVertex(), TEST_EPS);

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(3, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));
        Assert.assertSame(c, segments.get(2));

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p1), path.getVertices());
    }

    @Test
    public void testFromSegments_infiniteSegments() {
        // arrange
        Segment a = Line.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION).segment(Double.NEGATIVE_INFINITY, 1.0);
        Segment b = Line.fromPointAndAngle(Vector2D.of(1, 0), Geometry.HALF_PI, TEST_PRECISION).segment(0.0, Double.POSITIVE_INFINITY);

        // act
        Polyline path = Polyline.fromSegments(Arrays.asList(a, b));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStartSegment());
        Assert.assertNull(path.getStartVertex());

        Assert.assertSame(b, path.getEndSegment());
        Assert.assertNull(path.getEndVertex());

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(2, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));

        Assert.assertEquals(Arrays.asList(Vector2D.of(1, 0)), path.getVertices());
    }

    @Test
    public void testFromSegments_finiteAndInfiniteSegments_startInfinite() {
        // arrange
        Segment a = Line.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION).segment(Double.NEGATIVE_INFINITY, 1.0);
        Segment b = Segment.fromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION);

        // act
        Polyline path = Polyline.fromSegments(Arrays.asList(a, b));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStartSegment());
        Assert.assertNull(path.getStartVertex());

        Assert.assertSame(b, path.getEndSegment());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), path.getEndVertex(), TEST_EPS);

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(2, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));

        Assert.assertEquals(Arrays.asList(Vector2D.of(1, 0), Vector2D.of(1, 1)), path.getVertices());
    }

    @Test
    public void testFromSegments_finiteAndInfiniteSegments_endInfinite() {
        // arrange
        Segment a = Segment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);
        Segment b = Line.fromPointAndAngle(Vector2D.of(1, 0), Geometry.HALF_PI, TEST_PRECISION).segment(0.0, Double.POSITIVE_INFINITY);

        // act
        Polyline path = Polyline.fromSegments(Arrays.asList(a, b));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStartSegment());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, path.getStartVertex(), TEST_EPS);

        Assert.assertSame(b, path.getEndSegment());
        Assert.assertNull(path.getEndVertex());

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(2, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));

        Assert.assertEquals(Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0)), path.getVertices());
    }

    @Test
    public void testFromSegments_segmentsNotConnected() {
        // arrange
        Segment a = Segment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);
        Segment b = Segment.fromPoints(Vector2D.of(1.01, 0), Vector2D.of(1, 0), TEST_PRECISION);

        Segment c = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION).span();
        Segment d = Line.fromPointAndAngle(Vector2D.of(1, 0), Geometry.HALF_PI, TEST_PRECISION).span();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Polyline.fromSegments(a, b);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            Polyline.fromSegments(c, b);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            Polyline.fromSegments(a, d);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertices_empty() {
        // act
        Polyline path = Polyline.fromVertices(new ArrayList<>(), TEST_PRECISION);

        // assert
        Assert.assertTrue(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertNull(path.getStartSegment());
        Assert.assertNull(path.getStartVertex());

        Assert.assertNull(path.getEndSegment());
        Assert.assertNull(path.getEndVertex());

        Assert.assertEquals(0, path.getSegments().size());

        Assert.assertEquals(0, path.getVertices().size());
    }

    @Test
    public void testFromVertices_singleVertex_failsToCreatePath() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Polyline.fromVertices(Arrays.asList(Vector2D.ZERO), TEST_PRECISION);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertices_twoVertices() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);

        // act
        Polyline path = Polyline.fromVertices(Arrays.asList(p1, p2), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        assertFiniteSegment(path.getStartSegment(), p1, p2);
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getStartVertex(), TEST_EPS);

        Assert.assertSame(path.getStartSegment(), path.getEndSegment());
        EuclideanTestUtils.assertCoordinatesEqual(p2, path.getEndVertex(), TEST_EPS);

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(1, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);

        Assert.assertEquals(Arrays.asList(p1, p2), path.getVertices());
    }

    @Test
    public void testFromVertices_multipleVertices_notClosed() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(0, 1);

        // act
        Polyline path = Polyline.fromVertices(Arrays.asList(p1, p2, p3, p4), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        assertFiniteSegment(path.getStartSegment(), p1, p2);
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getStartVertex(), TEST_EPS);

        assertFiniteSegment(path.getEndSegment(), p3, p4);
        EuclideanTestUtils.assertCoordinatesEqual(p4, path.getEndVertex(), TEST_EPS);

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p4);

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p4), path.getVertices());
    }

    @Test
    public void testFromVertices_multipleVertices_closed() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(0, 1);

        // act
        Polyline path = Polyline.fromVertices(Arrays.asList(p1, p2, p3, p4, p1), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertTrue(path.isClosed());

        assertFiniteSegment(path.getStartSegment(), p1, p2);
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getStartVertex(), TEST_EPS);

        assertFiniteSegment(path.getEndSegment(), p4, p1);
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getEndVertex(), TEST_EPS);

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(4, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p4);
        assertFiniteSegment(segments.get(3), p4, p1);

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p4, p1), path.getVertices());
    }

    @Test
    public void testFromVertexLoop_empty() {
        // act
        Polyline path = Polyline.fromVertexLoop(new ArrayList<>(), TEST_PRECISION);

        // assert
        Assert.assertTrue(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertNull(path.getStartSegment());
        Assert.assertNull(path.getStartVertex());

        Assert.assertNull(path.getEndSegment());
        Assert.assertNull(path.getEndVertex());

        Assert.assertEquals(0, path.getSegments().size());

        Assert.assertEquals(0, path.getVertices().size());
    }

    @Test
    public void testFromVertexLoop_singleVertex_failsToCreatePath() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Polyline.fromVertexLoop(Arrays.asList(Vector2D.ZERO), TEST_PRECISION);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertexLoop_closeRequired() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);

        // act
        Polyline path = Polyline.fromVertexLoop(Arrays.asList(p1, p2, p3), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertTrue(path.isClosed());

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p1);

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p1), path.getVertices());
    }

    @Test
    public void testFromVertexLoop_closeNotRequired() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);

        // act
        Polyline path = Polyline.fromVertexLoop(Arrays.asList(p1, p2, p3, Vector2D.of(0, 0)), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertTrue(path.isClosed());

        List<Segment> segments = path.getSegments();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p1);

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p1), path.getVertices());
    }

    @Test
    public void testFromVertices_booleanArg() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(0, 1);

        // act
        Polyline open = Polyline.fromVertices(Arrays.asList(p1, p2, p3), false, TEST_PRECISION);
        Polyline closed = Polyline.fromVertices(Arrays.asList(p1, p2, p3), true, TEST_PRECISION);

        // assert
        Assert.assertFalse(open.isClosed());

        List<Segment> openSegments = open.getSegments();
        Assert.assertEquals(2, openSegments.size());
        assertFiniteSegment(openSegments.get(0), p1, p2);
        assertFiniteSegment(openSegments.get(1), p2, p3);

        Assert.assertTrue(closed.isClosed());

        List<Segment> closedSegments = closed.getSegments();
        Assert.assertEquals(3, closedSegments.size());
        assertFiniteSegment(closedSegments.get(0), p1, p2);
        assertFiniteSegment(closedSegments.get(1), p2, p3);
        assertFiniteSegment(closedSegments.get(2), p3, p1);
    }

    @Test
    public void testGetSegments_listIsNotModifiable() {
        // arrange
        Segment a = Segment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);
        List<Segment> inputSegments = new ArrayList<>(Arrays.asList(a));

        // act
        Polyline path = Polyline.fromSegments(inputSegments);

        inputSegments.clear();

        // assert
        Assert.assertNotSame(inputSegments, path.getSegments());
        Assert.assertEquals(1, path.getSegments().size());

        GeometryTestUtils.assertThrows(() -> {
            path.getSegments().add(a);
        }, UnsupportedOperationException.class);
    }

    @Test
    public void testIterable() {
        // arrange
        Polyline path = Polyline.builder(TEST_PRECISION)
                .appendVertices(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, 1)).build();

        // act
        List<Segment> segments = new ArrayList<>();
        for (Segment segment : path) {
            segments.add(segment);
        }

        // assert
        Assert.assertEquals(2, segments.size());
        assertFiniteSegment(segments.get(0), Vector2D.Unit.ZERO, Vector2D.Unit.PLUS_X);
        assertFiniteSegment(segments.get(1), Vector2D.Unit.PLUS_X, Vector2D.of(1, 1));
    }

    @Test
    public void testTransform_empty() {
        // arrange
        Polyline path = Polyline.empty();
        FunctionTransform2D t = FunctionTransform2D.from(v -> v.add(Vector2D.Unit.PLUS_X));

        // act/assert
        Assert.assertSame(path, path.transform(t));
    }

    @Test
    public void testTransform_finite() {
        // arrange
        Polyline path = Polyline.builder(TEST_PRECISION)
                .append(Vector2D.Unit.ZERO)
                .append(Vector2D.Unit.PLUS_X)
                .append(Vector2D.Unit.PLUS_Y)
                .close();

        AffineTransformMatrix2D t =
                AffineTransformMatrix2D.createRotation(Vector2D.of(1, 1), Geometry.HALF_PI);

        // act
        Polyline result = path.transform(t);

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertTrue(result.isClosed());
        Assert.assertTrue(result.isFinite());

        List<Segment> segments = result.getSegments();

        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), Vector2D.of(2, 0), Vector2D.of(2, 1));
        assertFiniteSegment(segments.get(1), Vector2D.of(2, 1), Vector2D.Unit.PLUS_X);
        assertFiniteSegment(segments.get(2), Vector2D.Unit.PLUS_X, Vector2D.of(2, 0));
    }

    @Test
    public void testTransform_infinite() {
        // arrange
        Polyline path = Polyline.fromSegments(
                Line.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION).span());

        FunctionTransform2D t = FunctionTransform2D.from(v -> v.add(Vector2D.Unit.PLUS_X));

        // act
        Polyline result = path.transform(t);

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertFalse(result.isFinite());

        List<Segment> segments = result.getSegments();

        Assert.assertEquals(1, segments.size());
        Segment segment = segments.get(0);
        Assert.assertTrue(segment.isInfinite());
        Assert.assertNull(segment.getStartPoint());
        Assert.assertNull(segment.getEndPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_X, segment.getLine().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_Y, segment.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testReverse_empty() {
        // arrange
        Polyline path = Polyline.empty();

        // act/assert
        Assert.assertSame(path, path.reverse());
    }

    @Test
    public void testReverse() {
        // arrange
        Polyline path = Polyline.builder(TEST_PRECISION)
                .append(Vector2D.Unit.ZERO)
                .append(Vector2D.Unit.PLUS_X)
                .append(Vector2D.Unit.PLUS_Y)
                .close();

        // act
        Polyline result = path.reverse();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertTrue(result.isClosed());
        Assert.assertTrue(result.isFinite());

        List<Segment> segments = result.getSegments();

        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), Vector2D.Unit.ZERO, Vector2D.Unit.PLUS_Y);
        assertFiniteSegment(segments.get(1), Vector2D.Unit.PLUS_Y, Vector2D.Unit.PLUS_X);
        assertFiniteSegment(segments.get(2), Vector2D.Unit.PLUS_X, Vector2D.Unit.ZERO);
    }

    @Test
    public void testReverse_singleInfinite() {
        // arrange
        Polyline path = Polyline.fromSegments(
                Line.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION).span());

        // act
        Polyline result = path.reverse();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertFalse(result.isFinite());

        List<Segment> segments = result.getSegments();

        Assert.assertEquals(1, segments.size());
        Segment segment = segments.get(0);
        Assert.assertTrue(segment.isInfinite());
        Assert.assertNull(segment.getStartPoint());
        Assert.assertNull(segment.getEndPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.ZERO, segment.getLine().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_Y, segment.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testReverse_doubleInfinite() {
        // arrange
        Segment a = Line.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION).segmentTo(Vector2D.ZERO);
        Segment b = Line.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).segmentFrom(Vector2D.ZERO);

        Polyline path = Polyline.fromSegments(a, b);

        // act
        Polyline result = path.reverse();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertFalse(result.isFinite());

        List<Segment> segments = result.getSegments();
        Assert.assertEquals(2, segments.size());

        Segment bResult = segments.get(0);
        Assert.assertTrue(bResult.isInfinite());
        Assert.assertNull(bResult.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, bResult.getEndPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, bResult.getLine().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_X, bResult.getLine().getDirection(), TEST_EPS);

        Segment aResult = segments.get(1);
        Assert.assertTrue(aResult.isInfinite());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, aResult.getStartPoint(), TEST_EPS);
        Assert.assertNull(aResult.getEndPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, aResult.getLine().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_Y, aResult.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testToTree() {
        // arrange
        Polyline path = Polyline.builder(TEST_PRECISION)
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
        Builder builder = Polyline.builder(TEST_PRECISION);

        Polyline path = builder.appendVertices(
                Vector2D.of(-1, 0),
                Vector2D.ZERO,
                Vector2D.of(1, 0),
                Vector2D.of(1, 1),
                Vector2D.of(1, 2))
            .build();

        // act
        Polyline result = path.simplify();

        // assert
        List<Segment> segments = result.getSegments();
        Assert.assertEquals(2, segments.size());
        assertFiniteSegment(segments.get(0), Vector2D.of(-1, 0), Vector2D.of(1, 0));
        assertFiniteSegment(segments.get(1), Vector2D.of(1, 0), Vector2D.of(1, 2));
    }

    @Test
    public void testSimplify_startAndEndCombined() {
        // arrange
        Builder builder = Polyline.builder(TEST_PRECISION);

        Polyline path = builder.appendVertices(
                Vector2D.ZERO,
                Vector2D.of(1, 0),
                Vector2D.of(0, 1),
                Vector2D.of(-1, 0))
            .close();

        // act
        Polyline result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertTrue(result.isClosed());
        Assert.assertFalse(result.isInfinite());

        List<Segment> segments = result.getSegments();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), Vector2D.of(-1, 0), Vector2D.of(1, 0));
        assertFiniteSegment(segments.get(1), Vector2D.of(1, 0), Vector2D.of(0, 1));
        assertFiniteSegment(segments.get(2), Vector2D.of(0, 1), Vector2D.of(-1, 0));
    }

    @Test
    public void testSimplify_empty() {
        // arrange
        Builder builder = Polyline.builder(TEST_PRECISION);

        Polyline path = builder.build();

        // act
        Polyline result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertFalse(result.isInfinite());

        List<Segment> segments = result.getSegments();
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testSimplify_infiniteSegment() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION);

        Builder builder = Polyline.builder(TEST_PRECISION);
        Polyline path = builder
                .append(line.span())
                .build();

        // act
        Polyline result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertTrue(result.isInfinite());

        Assert.assertNull(result.getStartVertex());
        Assert.assertNull(result.getEndVertex());

        List<Segment> segments = result.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(line, segments.get(0).getLine());
    }

    @Test
    public void testSimplify_combinedInfiniteSegment() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION);
        Split<Segment> split = line.span().split(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.HALF_PI, TEST_PRECISION));

        Builder builder = Polyline.builder(TEST_PRECISION);
        Polyline path = builder
                .append(split.getMinus())
                .append(split.getPlus())
                .build();

        // act
        Polyline result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertTrue(result.isInfinite());

        Assert.assertNull(result.getStartVertex());
        Assert.assertNull(result.getEndVertex());

        List<Segment> segments = result.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(line, segments.get(0).getLine());
    }

    @Test
    public void testSimplify_startAndEndNotCombinedWhenNotClosed() {
        // arrange
        Line xAxis = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION);
        Builder builder = Polyline.builder(TEST_PRECISION);

        Polyline path = builder
                .append(xAxis.segment(0, 1))
                .appendVertices(
                        Vector2D.of(2, 1),
                        Vector2D.of(3, 0))
                .append(xAxis.segment(3, 4))
            .build();

        // act
        Polyline result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertFalse(result.isClosed());
        Assert.assertFalse(result.isInfinite());

        List<Segment> segments = result.getSegments();
        Assert.assertEquals(4, segments.size());
        assertFiniteSegment(segments.get(0), Vector2D.ZERO, Vector2D.of(1, 0));
        assertFiniteSegment(segments.get(1), Vector2D.of(1, 0), Vector2D.of(2, 1));
        assertFiniteSegment(segments.get(2), Vector2D.of(2, 1), Vector2D.of(3, 0));
        assertFiniteSegment(segments.get(3), Vector2D.of(3, 0), Vector2D.of(4, 0));
    }

    @Test
    public void testSimplify_subsequentCallsToReturnedObjectReturnSameObject() {
        // arrange
        Builder builder = Polyline.builder(TEST_PRECISION);
        Polyline path = builder.appendVertices(
                    Vector2D.ZERO,
                    Vector2D.of(1, 0),
                    Vector2D.of(2, 0))
                .build();

        // act
        Polyline result = path.simplify();

        // assert
        Assert.assertNotSame(path, result);
        Assert.assertSame(result, result.simplify());
    }

    @Test
    public void testToString() {
        // arrange
        Line yAxis = Line.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        Line xAxis = Line.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        Polyline empty = Polyline.empty();

        Polyline singleFullSegment = Polyline.fromSegments(xAxis.span());
        Polyline singleFiniteSegment = Polyline.fromSegments(
                Segment.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));

        Polyline startOpenPath = Polyline.builder(TEST_PRECISION)
                .append(xAxis.segmentTo(Vector2D.Unit.PLUS_X))
                .append(Vector2D.of(1, 1))
                .build();

        Polyline endOpenPath = Polyline.builder(TEST_PRECISION)
                .append(Vector2D.of(0, 1))
                .append(Vector2D.ZERO)
                .append(xAxis.segmentFrom(Vector2D.ZERO))
                .build();

        Polyline doubleOpenPath = Polyline.fromSegments(yAxis.segmentTo(Vector2D.ZERO),
                xAxis.segmentFrom(Vector2D.ZERO));

        Polyline nonOpenPath = Polyline.builder(TEST_PRECISION)
                .append(Vector2D.ZERO)
                .append(Vector2D.Unit.PLUS_X)
                .append(Vector2D.of(1, 1))
                .build();

        // act/assert
        String emptyStr = empty.toString();
        Assert.assertTrue(emptyStr.contains("empty= true"));

        String singleFullStr = singleFullSegment.toString();
        Assert.assertTrue(singleFullStr.contains("segment= Segment["));

        String singleFiniteStr = singleFiniteSegment.toString();
        Assert.assertTrue(singleFiniteStr.contains("segment= Segment["));

        String startOpenStr = startOpenPath.toString();
        Assert.assertTrue(startOpenStr.contains("startDirection= ") && startOpenStr.contains("vertices= "));

        String endOpenStr = endOpenPath.toString();
        Assert.assertTrue(endOpenStr.contains("vertices= ") && endOpenStr.contains("endDirection= "));

        String doubleOpenStr = doubleOpenPath.toString();
        Assert.assertTrue(doubleOpenStr.contains("startDirection= ") && doubleOpenStr.contains("vertices= ") &&
                doubleOpenStr.contains("endDirection= "));

        String nonOpenStr = nonOpenPath.toString();
        Assert.assertTrue(nonOpenStr.contains("vertices= "));
    }

    @Test
    public void testBuilder_prependAndAppend_segments() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(1, 0);

        Segment a = Segment.fromPoints(p1, p2, TEST_PRECISION);
        Segment b = Segment.fromPoints(p2, p3, TEST_PRECISION);
        Segment c = Segment.fromPoints(p3, p4, TEST_PRECISION);
        Segment d = Segment.fromPoints(p4, p1, TEST_PRECISION);

        Builder builder = Polyline.builder(null);

        // act
        builder.prepend(b)
            .append(c)
            .prepend(a)
            .append(d);

        Polyline path = builder.build();

        // assert
        List<Segment> segments = path.getSegments();
        Assert.assertEquals(4, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));
        Assert.assertSame(c, segments.get(2));
        Assert.assertSame(d, segments.get(3));
    }

    @Test
    public void testBuilder_prependAndAppend_disconnectedSegments() {
        // arrange
        Segment a = Segment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        Builder builder = Polyline.builder(null);
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

        Builder builder = Polyline.builder(TEST_PRECISION);

        // act
        builder.prepend(p2)
            .append(p3)
            .prepend(p1)
            .append(p4)
            .append(p1);

        Polyline path = builder.build();

        // assert
        List<Segment> segments = path.getSegments();
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
        Builder builder = Polyline.builder(null);

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
        Builder builder = Polyline.builder(TEST_PRECISION);

        builder.append(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION).span());

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

        Builder builder = Polyline.builder(TEST_PRECISION);
        builder.append(p);

        // act
        builder.append(p)
            .prepend(p)
            .append(Vector2D.of(0, 1e-20))
            .prepend(Vector2D.of(1e-20, 0));

        builder.append(Vector2D.Unit.PLUS_X);

        // assert
        Polyline path = builder.build();

        List<Segment> segments = path.getSegments();
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

        Segment a = Segment.fromPoints(p1, p2, TEST_PRECISION);
        Segment c = Segment.fromPoints(p3, p4, TEST_PRECISION);

        Builder builder = Polyline.builder(TEST_PRECISION);

        // act
        builder.prepend(p2)
            .append(p3)
            .append(c)
            .prepend(a)
            .append(p1);

        Polyline path = builder.build();

        // assert
        List<Segment> segments = path.getSegments();
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

        Builder builder = Polyline.builder(TEST_PRECISION);

        // act
        builder.appendVertices(p1, p2)
            .appendVertices(Arrays.asList(p3, p4, p1));

        Polyline path = builder.build();

        // assert
        List<Segment> segments = path.getSegments();
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

        Builder builder = Polyline.builder(TEST_PRECISION);

        // act
        builder.prependVertices(p3, p4, p1)
            .prependVertices(Arrays.asList(p1, p2));

        Polyline path = builder.build();

        // assert
        List<Segment> segments = path.getSegments();
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

        Builder builder = Polyline.builder(TEST_PRECISION);

        // act
        builder.append(p1)
            .append(p2)
            .append(p3);

        Polyline path = builder.close();

        // assert
        List<Segment> segments = path.getSegments();
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

        Builder builder = Polyline.builder(TEST_PRECISION);

        // act
        builder.append(p1)
            .append(p2)
            .append(p3)
            .append(p1);

        Polyline path = builder.close();

        // assert
        List<Segment> segments = path.getSegments();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p1);
    }

    @Test
    public void testBuilder_close_infiniteSegmentAtStart() {
        // arrange
        Builder builder = Polyline.builder(TEST_PRECISION);

        builder.append(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION)
                .segment(Double.NEGATIVE_INFINITY, 1))
            .append(Vector2D.of(1, 1));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.close();
        }, IllegalStateException.class, "Unable to close polyline: polyline is infinite");
    }

    @Test
    public void testBuilder_close_infiniteSegmentAtEnd() {
        // arrange
        Builder builder = Polyline.builder(TEST_PRECISION);

        builder
            .append(Vector2D.ZERO)
            .append(Vector2D.Unit.PLUS_X)
            .append(Line.fromPointAndAngle(Vector2D.Unit.PLUS_X, Geometry.HALF_PI, TEST_PRECISION)
                .segment(0, Double.POSITIVE_INFINITY));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.close();
        }, IllegalStateException.class, "Unable to close polyline: polyline is infinite");
    }

    @Test
    public void testBuilder_close_emptyPath() {
        // arrange
        Builder builder = Polyline.builder(TEST_PRECISION);

        // act
        Polyline path = builder.close();

        // assert
        Assert.assertEquals(0, path.getSegments().size());
    }

    @Test
    public void testBuilder_close_obtuseTriangle() {
        // arrange
        Builder builder = Polyline.builder(TEST_PRECISION);
        builder.appendVertices(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(2, 1));

        // act
        Polyline path = builder.close();

        // assert
        Assert.assertEquals(3, path.getSegments().size());
        assertFiniteSegment(path.getSegments().get(0), Vector2D.ZERO, Vector2D.of(1, 0));
        assertFiniteSegment(path.getSegments().get(1), Vector2D.of(1, 0), Vector2D.of(2, 1));
        assertFiniteSegment(path.getSegments().get(2), Vector2D.of(2, 1), Vector2D.ZERO);
    }

    private static void assertFiniteSegment(Segment segment, Vector2D start, Vector2D end) {
        Assert.assertFalse(segment.isInfinite());
        Assert.assertTrue(segment.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }
}
