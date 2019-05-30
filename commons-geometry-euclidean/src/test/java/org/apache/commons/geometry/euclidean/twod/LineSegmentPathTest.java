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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.LineSegmentPath.LineSegmentPathBuilder;
import org.junit.Assert;
import org.junit.Test;

public class LineSegmentPathTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromSegments_empty() {
        // act
        LineSegmentPath path = LineSegmentPath.fromSegments(new ArrayList<>());

        // assert
        Assert.assertTrue(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertNull(path.getStart());
        Assert.assertNull(path.getStartVertex());

        Assert.assertNull(path.getEnd());
        Assert.assertNull(path.getEndVertex());

        Assert.assertEquals(0, path.getSegments().size());

        Assert.assertEquals(0, path.getVertices().size());
    }

    @Test
    public void testFromSegments_singleFiniteSegment() {
        // arrange
        LineSegment a = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        // act
        LineSegmentPath path = LineSegmentPath.fromSegments(a);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStart());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, path.getStartVertex(), TEST_EPS);

        Assert.assertSame(a, path.getEnd());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 0), path.getEndVertex(), TEST_EPS);

        List<LineSegment> segments = path.getSegments();
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(a, segments.get(0));

        Assert.assertEquals(Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0)), path.getVertices());
    }

    @Test
    public void testFromSegments_singleInfiniteSegment() {
        // arrange
        LineSegment a = Line.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION).span();

        // act
        LineSegmentPath path = LineSegmentPath.fromSegments(a);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStart());
        Assert.assertNull(path.getStartVertex());

        Assert.assertSame(a, path.getEnd());
        Assert.assertNull(path.getEndVertex());

        List<LineSegment> segments = path.getSegments();
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

        LineSegment a = LineSegment.fromPoints(p1, p2, TEST_PRECISION);
        LineSegment b = LineSegment.fromPoints(p2, p3, TEST_PRECISION);

        // act
        LineSegmentPath path = LineSegmentPath.fromSegments(a, b);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStart());
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getStartVertex(), TEST_EPS);

        Assert.assertSame(b, path.getEnd());
        EuclideanTestUtils.assertCoordinatesEqual(p3, path.getEndVertex(), TEST_EPS);

        List<LineSegment> segments = path.getSegments();
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

        LineSegment a = LineSegment.fromPoints(p1, p2, TEST_PRECISION);
        LineSegment b = LineSegment.fromPoints(p2, p3, TEST_PRECISION);
        LineSegment c = LineSegment.fromPoints(p3, p1, TEST_PRECISION);

        // act
        LineSegmentPath path = LineSegmentPath.fromSegments(Arrays.asList(a, b, c));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertTrue(path.isClosed());

        Assert.assertSame(a, path.getStart());
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getStartVertex(), TEST_EPS);

        Assert.assertSame(c, path.getEnd());
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getEndVertex(), TEST_EPS);

        List<LineSegment> segments = path.getSegments();
        Assert.assertEquals(3, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));
        Assert.assertSame(c, segments.get(2));

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p1), path.getVertices());
    }

    @Test
    public void testFromSegments_infiniteSegments() {
        // arrange
        LineSegment a = Line.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION).segment(Double.NEGATIVE_INFINITY, 1.0);
        LineSegment b = Line.fromPointAndAngle(Vector2D.of(1, 0), Geometry.HALF_PI, TEST_PRECISION).segment(0.0, Double.POSITIVE_INFINITY);

        // act
        LineSegmentPath path = LineSegmentPath.fromSegments(Arrays.asList(a, b));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStart());
        Assert.assertNull(path.getStartVertex());

        Assert.assertSame(b, path.getEnd());
        Assert.assertNull(path.getEndVertex());

        List<LineSegment> segments = path.getSegments();
        Assert.assertEquals(2, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));

        Assert.assertEquals(Arrays.asList(Vector2D.of(1, 0)), path.getVertices());
    }

    @Test
    public void testFromSegments_finiteAndInfiniteSegments_startInfinite() {
        // arrange
        LineSegment a = Line.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION).segment(Double.NEGATIVE_INFINITY, 1.0);
        LineSegment b = LineSegment.fromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegmentPath path = LineSegmentPath.fromSegments(Arrays.asList(a, b));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStart());
        Assert.assertNull(path.getStartVertex());

        Assert.assertSame(b, path.getEnd());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), path.getEndVertex(), TEST_EPS);

        List<LineSegment> segments = path.getSegments();
        Assert.assertEquals(2, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));

        Assert.assertEquals(Arrays.asList(Vector2D.of(1, 0), Vector2D.of(1, 1)), path.getVertices());
    }

    @Test
    public void testFromSegments_finiteAndInfiniteSegments_endInfinite() {
        // arrange
        LineSegment a = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);
        LineSegment b = Line.fromPointAndAngle(Vector2D.of(1, 0), Geometry.HALF_PI, TEST_PRECISION).segment(0.0, Double.POSITIVE_INFINITY);

        // act
        LineSegmentPath path = LineSegmentPath.fromSegments(Arrays.asList(a, b));

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(a, path.getStart());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, path.getStartVertex(), TEST_EPS);

        Assert.assertSame(b, path.getEnd());
        Assert.assertNull(path.getEndVertex());

        List<LineSegment> segments = path.getSegments();
        Assert.assertEquals(2, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));

        Assert.assertEquals(Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0)), path.getVertices());
    }

    @Test
    public void testFromSegments_segmentsNotConnected() {
        // arrange
        LineSegment a = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);
        LineSegment b = LineSegment.fromPoints(Vector2D.of(1.01, 0), Vector2D.of(1, 0), TEST_PRECISION);

        LineSegment c = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION).span();
        LineSegment d = Line.fromPointAndAngle(Vector2D.of(1, 0), Geometry.HALF_PI, TEST_PRECISION).span();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            LineSegmentPath.fromSegments(a, b);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            LineSegmentPath.fromSegments(c, b);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            LineSegmentPath.fromSegments(a, d);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertices_empty() {
        // act
        LineSegmentPath path = LineSegmentPath.fromVertices(new ArrayList<>(), TEST_PRECISION);

        // assert
        Assert.assertTrue(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        Assert.assertNull(path.getStart());
        Assert.assertNull(path.getStartVertex());

        Assert.assertNull(path.getEnd());
        Assert.assertNull(path.getEndVertex());

        Assert.assertEquals(0, path.getSegments().size());

        Assert.assertEquals(0, path.getVertices().size());
    }

    @Test
    public void testFromVertices_singleVertex_failsToCreatePath() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            LineSegmentPath.fromVertices(Arrays.asList(Vector2D.ZERO), TEST_PRECISION);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertices_twoVertices() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);

        // act
        LineSegmentPath path = LineSegmentPath.fromVertices(Arrays.asList(p1, p2), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        assertFiniteSegment(path.getStart(), p1, p2);
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getStartVertex(), TEST_EPS);

        Assert.assertSame(path.getStart(), path.getEnd());
        EuclideanTestUtils.assertCoordinatesEqual(p2, path.getEndVertex(), TEST_EPS);

        List<LineSegment> segments = path.getSegments();
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
        LineSegmentPath path = LineSegmentPath.fromVertices(Arrays.asList(p1, p2, p3, p4), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertFalse(path.isClosed());

        assertFiniteSegment(path.getStart(), p1, p2);
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getStartVertex(), TEST_EPS);

        assertFiniteSegment(path.getEnd(), p3, p4);
        EuclideanTestUtils.assertCoordinatesEqual(p4, path.getEndVertex(), TEST_EPS);

        List<LineSegment> segments = path.getSegments();
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
        LineSegmentPath path = LineSegmentPath.fromVertices(Arrays.asList(p1, p2, p3, p4, p1), TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());
        Assert.assertTrue(path.isClosed());

        assertFiniteSegment(path.getStart(), p1, p2);
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getStartVertex(), TEST_EPS);

        assertFiniteSegment(path.getEnd(), p4, p1);
        EuclideanTestUtils.assertCoordinatesEqual(p1, path.getEndVertex(), TEST_EPS);

        List<LineSegment> segments = path.getSegments();
        Assert.assertEquals(4, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p4);
        assertFiniteSegment(segments.get(3), p4, p1);

        Assert.assertEquals(Arrays.asList(p1, p2, p3, p4, p1), path.getVertices());
    }

    @Test
    public void testGetSegments_listIsNotModifiable() {
        // arrange
        LineSegment a = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);
        List<LineSegment> inputSegments = new ArrayList<>(Arrays.asList(a));

        // act
        LineSegmentPath path = LineSegmentPath.fromSegments(inputSegments);

        inputSegments.clear();

        // assert
        Assert.assertNotSame(inputSegments, path.getSegments());
        Assert.assertEquals(1, path.getSegments().size());

        GeometryTestUtils.assertThrows(() -> {
            path.getSegments().add(a);
        }, UnsupportedOperationException.class);
    }

    @Test
    public void testToString() {
        // arrange
        LineSegmentPath path = LineSegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.ZERO, Vector2D.PLUS_X).build();

        // act
        String str = path.toString();

        // assert
        Assert.assertTrue(str.contains("LineSegmentPath"));
        Assert.assertTrue(str.contains("segments= "));
        Assert.assertTrue(str.contains("start= (0.0, 0.0), end= (1.0, 0.0)"));
    }

    @Test
    public void testBuilder_prependAndAppend_segments() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(1, 0);

        LineSegment a = LineSegment.fromPoints(p1, p2, TEST_PRECISION);
        LineSegment b = LineSegment.fromPoints(p2, p3, TEST_PRECISION);
        LineSegment c = LineSegment.fromPoints(p3, p4, TEST_PRECISION);
        LineSegment d = LineSegment.fromPoints(p4, p1, TEST_PRECISION);

        LineSegmentPathBuilder builder = new LineSegmentPathBuilder();

        // act
        builder.prepend(b)
            .append(c)
            .prepend(a)
            .append(d);

        LineSegmentPath path = builder.build();

        // assert
        List<LineSegment> segments = path.getSegments();
        Assert.assertEquals(4, segments.size());
        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));
        Assert.assertSame(c, segments.get(2));
        Assert.assertSame(d, segments.get(3));
    }

    @Test
    public void testBuilder_prependAndAppend_disconnectedSegments() {
        // arrange
        LineSegment a = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        LineSegmentPathBuilder builder = new LineSegmentPathBuilder();
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

        LineSegmentPathBuilder builder = new LineSegmentPathBuilder(TEST_PRECISION);

        // act
        builder.prepend(p2)
            .append(p3)
            .prepend(p1)
            .append(p4)
            .append(p1);

        LineSegmentPath path = builder.build();

        // assert
        List<LineSegment> segments = path.getSegments();
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
        LineSegmentPathBuilder builder = new LineSegmentPathBuilder();

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
        Vector2D p = Vector2D.PLUS_X;
        LineSegmentPathBuilder builder = new LineSegmentPathBuilder()
                .setPrecision(TEST_PRECISION);

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

        LineSegmentPathBuilder builder = new LineSegmentPathBuilder(TEST_PRECISION);
        builder.append(p);

        // act
        builder.append(p)
            .prepend(p)
            .append(Vector2D.of(0, 1e-20))
            .prepend(Vector2D.of(1e-20, 0));

        builder.append(Vector2D.PLUS_X);

        // assert
        LineSegmentPath path = builder.build();

        List<LineSegment> segments = path.getSegments();
        Assert.assertEquals(1, segments.size());
        assertFiniteSegment(segments.get(0), p, Vector2D.PLUS_X);
    }

    @Test
    public void testBuilder_prependAndAppend_mixedVerticesAndSegments() {
        // arrange
        Vector2D p1 = Vector2D.ZERO;
        Vector2D p2 = Vector2D.of(1, 0);
        Vector2D p3 = Vector2D.of(1, 1);
        Vector2D p4 = Vector2D.of(0, 1);

        LineSegment a = LineSegment.fromPoints(p1, p2, TEST_PRECISION);
        LineSegment c = LineSegment.fromPoints(p3, p4, TEST_PRECISION);

        LineSegmentPathBuilder builder = new LineSegmentPathBuilder(TEST_PRECISION);

        // act
        builder.prepend(p2)
            .append(p3)
            .append(c)
            .prepend(a)
            .append(p1);

        LineSegmentPath path = builder.build();

        // assert
        List<LineSegment> segments = path.getSegments();
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

        LineSegmentPathBuilder builder = new LineSegmentPathBuilder(TEST_PRECISION);

        // act
        builder.appendVertices(p1, p2)
            .appendVertices(Arrays.asList(p3, p4, p1));

        LineSegmentPath path = builder.build();

        // assert
        List<LineSegment> segments = path.getSegments();
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

        LineSegmentPathBuilder builder = new LineSegmentPathBuilder(TEST_PRECISION);

        // act
        builder.prependVertices(p3, p4, p1)
            .prependVertices(Arrays.asList(p1, p2));

        LineSegmentPath path = builder.build();

        // assert
        List<LineSegment> segments = path.getSegments();
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

        LineSegmentPathBuilder builder = new LineSegmentPathBuilder()
                .setPrecision(TEST_PRECISION);

        // act
        builder.append(p1)
            .append(p2)
            .append(p3);

        LineSegmentPath path = builder.close();

        // assert
        List<LineSegment> segments = path.getSegments();
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

        LineSegmentPathBuilder builder = new LineSegmentPathBuilder()
                .setPrecision(TEST_PRECISION);

        // act
        builder.append(p1)
            .append(p2)
            .append(p3)
            .append(p1);

        LineSegmentPath path = builder.close();

        // assert
        List<LineSegment> segments = path.getSegments();
        Assert.assertEquals(3, segments.size());
        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p2, p3);
        assertFiniteSegment(segments.get(2), p3, p1);
    }

    @Test
    public void testBuilder_close_infiniteSegmentAtStart() {
        // arrange
        LineSegmentPathBuilder builder = new LineSegmentPathBuilder()
                .setPrecision(TEST_PRECISION);

        builder.append(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION)
                .segment(Double.NEGATIVE_INFINITY, 1))
            .append(Vector2D.of(1, 1));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.close();
        }, IllegalStateException.class, "Unable to close line segment path: path is infinite");
    }

    @Test
    public void testBuilder_close_infiniteSegmentAtEnd() {
        // arrange
        LineSegmentPathBuilder builder = new LineSegmentPathBuilder()
                .setPrecision(TEST_PRECISION);

        builder
            .append(Vector2D.ZERO)
            .append(Vector2D.PLUS_X)
            .append(Line.fromPointAndAngle(Vector2D.PLUS_X, Geometry.HALF_PI, TEST_PRECISION)
                .segment(0, Double.POSITIVE_INFINITY));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.close();
        }, IllegalStateException.class, "Unable to close line segment path: path is infinite");
    }

    @Test
    public void testBuilder_close_emptyPath() {
        // arrange
        LineSegmentPathBuilder builder = new LineSegmentPathBuilder()
                .setPrecision(TEST_PRECISION);

        // act
        LineSegmentPath path = builder.close();

        // assert
        Assert.assertEquals(0, path.getSegments().size());
    }

    private static void assertFiniteSegment(LineSegment segment, Vector2D start, Vector2D end) {
        Assert.assertFalse(segment.isInfinite());
        Assert.assertTrue(segment.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStart(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEnd(), TEST_EPS);
    }
}
