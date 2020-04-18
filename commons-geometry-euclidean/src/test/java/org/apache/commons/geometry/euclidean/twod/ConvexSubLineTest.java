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
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class ConvexSubLineTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromInterval_intervalArg_finite() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(-1, 2, intervalPrecision);

        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        Segment segment = (Segment) ConvexSubLine.fromInterval(line, interval);

        // assert
        double side = 1.0 / Math.sqrt(2);
        checkFinite(segment, Vector2D.of(-side, -side), Vector2D.of(2 * side, 2 * side));

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_intervalArg_full() {
        // arrange
        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        Line.Span span = (Line.Span) ConvexSubLine.fromInterval(line, Interval.full());

        // assert
        GeometryTestUtils.assertNegativeInfinity(span.getSubspaceStart());
        GeometryTestUtils.assertPositiveInfinity(span.getSubspaceEnd());

        Assert.assertNull(span.getStartPoint());
        Assert.assertNull(span.getEndPoint());

        Assert.assertSame(Interval.full(), span.getInterval());
        Assert.assertSame(TEST_PRECISION, span.getPrecision());
    }

    @Test
    public void testFromInterval_intervalArg_positiveHalfSpace() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.min(-1, intervalPrecision);

        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        Ray ray = (Ray) ConvexSubLine.fromInterval(line, interval);

        // assert
        Assert.assertEquals(-1.0, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        double side = 1.0 / Math.sqrt(2);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-side, -side), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        checkInterval(interval, ray.getInterval());
        Assert.assertSame(TEST_PRECISION, ray.getPrecision());
    }

    @Test
    public void testFromInterval_intervalArg_negativeHalfSpace() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.max(2, intervalPrecision);

        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        TerminatedLine halfLine = (TerminatedLine) ConvexSubLine.fromInterval(line, interval);

        // assert
        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(2, halfLine.getSubspaceEnd(), TEST_EPS);

        double side = 1.0 / Math.sqrt(2);

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2 * side, 2 * side), halfLine.getEndPoint(), TEST_EPS);

        checkInterval(interval, halfLine.getInterval());
        Assert.assertSame(TEST_PRECISION, halfLine.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_finite() {
        // arrange
        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        Segment segment = (Segment) ConvexSubLine.fromInterval(line, -1, 2);

        // assert
        double side = 1.0 / Math.sqrt(2);
        checkFinite(segment, Vector2D.of(-side, -side), Vector2D.of(2 * side, 2 * side));

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_full() {
        // arrange
        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        Line.Span span = (Line.Span) ConvexSubLine.fromInterval(line, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        // assert
        GeometryTestUtils.assertNegativeInfinity(span.getSubspaceStart());
        GeometryTestUtils.assertPositiveInfinity(span.getSubspaceEnd());

        Assert.assertNull(span.getStartPoint());
        Assert.assertNull(span.getEndPoint());

        Assert.assertSame(TEST_PRECISION, span.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_positiveHalfSpace() {
        // arrange
        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        Ray ray = (Ray) ConvexSubLine.fromInterval(line, -1, Double.POSITIVE_INFINITY);

        // assert
        Assert.assertEquals(-1.0, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        double side = 1.0 / Math.sqrt(2);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-side, -side), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        Assert.assertSame(TEST_PRECISION, ray.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_negativeHalfSpace() {
        // arrange
        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        TerminatedLine halfLine = (TerminatedLine) ConvexSubLine.fromInterval(line, 2, Double.NEGATIVE_INFINITY);

        // assert
        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(2, halfLine.getSubspaceEnd(), TEST_EPS);

        double side = 1.0 / Math.sqrt(2);

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2 * side, 2 * side), halfLine.getEndPoint(), TEST_EPS);

        Assert.assertSame(TEST_PRECISION, halfLine.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_invalid() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexSubLine.fromInterval(line, 0, Double.NaN);
        }, IllegalArgumentException.class, "Invalid convex subline interval: 0.0, NaN");

        GeometryTestUtils.assertThrows(() -> {
            ConvexSubLine.fromInterval(line, Double.NaN, 0.0);
        }, IllegalArgumentException.class, "Invalid convex subline interval: NaN, 0.0");

        GeometryTestUtils.assertThrows(() -> {
            ConvexSubLine.fromInterval(line, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid convex subline interval: Infinity, Infinity");

        GeometryTestUtils.assertThrows(() -> {
            ConvexSubLine.fromInterval(line, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid convex subline interval: -Infinity, -Infinity");

        GeometryTestUtils.assertThrows(() -> {
            ConvexSubLine.fromInterval(line, Double.POSITIVE_INFINITY, Double.NaN);
        }, IllegalArgumentException.class, "Invalid convex subline interval: Infinity, NaN");

        GeometryTestUtils.assertThrows(() -> {
            ConvexSubLine.fromInterval(line, Double.NaN, Double.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid convex subline interval: NaN, -Infinity");
    }

    @Test
    public void testToConvex() {
        // arrange
        Line line = Line.fromPoints(Vector2D.of(-1, 0), Vector2D.of(4, 5), TEST_PRECISION);
        ConvexSubLine sub = ConvexSubLine.fromInterval(line, 1, 2);

        // act
        List<ConvexSubLine> segments = sub.toConvex();

        // assert
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(sub, segments.get(0));
    }

    @Test
    public void testIntersection_line() {
        // arrange
        Segment aSeg = Segment.fromPoints(Vector2D.of(1, 0), Vector2D.of(2, 0), TEST_PRECISION);
        Segment bSeg = Segment.fromPoints(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION);

        Line xAxis = Line.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        Line yAxis = Line.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        Line angledLine = Line.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 0), TEST_PRECISION);

        // act/assert
        Assert.assertNull(aSeg.intersection(xAxis));
        Assert.assertNull(aSeg.intersection(yAxis));

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, bSeg.intersection(xAxis), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, bSeg.intersection(yAxis), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), bSeg.intersection(angledLine), TEST_EPS);
    }

    @Test
    public void testIntersection_lineSegment() {
        // arrange
        Segment a = Segment.fromPoints(Vector2D.of(1, 0), Vector2D.of(2, 0), TEST_PRECISION);
        Segment b = Segment.fromPoints(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION);
        Segment c = Segment.fromPoints(Vector2D.of(-1, 0), Vector2D.ZERO, TEST_PRECISION);
        Segment d = Segment.fromPoints(Vector2D.of(0, 3), Vector2D.of(3, 0), TEST_PRECISION);

        // act/assert
        Assert.assertNull(a.intersection(a));
        Assert.assertNull(a.intersection(c));
        Assert.assertNull(a.intersection(b));

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, b.intersection(c), TEST_EPS);

        Assert.assertNull(b.intersection(d));
        Assert.assertNull(d.intersection(b));
    }

    @Test
    public void testSplit_finite() {
        // arrange
        Vector2D start = Vector2D.of(1, 1);
        Vector2D end = Vector2D.of(3, 2);
        Vector2D middle = start.lerp(end, 0.5);

        Segment sub = Segment.fromPoints(start, end, TEST_PRECISION);

        // act/assert
        Split<ConvexSubLine> both = sub.split(Line.fromPointAndDirection(middle, Vector2D.of(1, -2), TEST_PRECISION));
        checkFinite(both.getMinus(), middle, end);
        checkFinite(both.getPlus(), start, middle);

        Split<ConvexSubLine> bothReversed = sub.split(Line.fromPointAndDirection(middle, Vector2D.of(-1, 2), TEST_PRECISION));
        checkFinite(bothReversed.getMinus(), start, middle);
        checkFinite(bothReversed.getPlus(), middle, end);

        Split<ConvexSubLine> minusOnlyOrthogonal = sub.split(Line.fromPointAndDirection(start, Vector2D.of(1, -2), TEST_PRECISION));
        Assert.assertSame(sub, minusOnlyOrthogonal.getMinus());
        Assert.assertNull(minusOnlyOrthogonal.getPlus());

        Split<ConvexSubLine> minusOnlyParallel = sub.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertSame(sub, minusOnlyParallel.getMinus());
        Assert.assertNull(minusOnlyParallel.getPlus());

        Split<ConvexSubLine> plusOnlyOrthogonal = sub.split(Line.fromPointAndDirection(end, Vector2D.of(1, -2), TEST_PRECISION));
        Assert.assertNull(plusOnlyOrthogonal.getMinus());
        Assert.assertSame(sub, plusOnlyOrthogonal.getPlus());

        Split<ConvexSubLine> plusOnlyParallel = sub.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(-2, -1), TEST_PRECISION));
        Assert.assertNull(plusOnlyParallel.getMinus());
        Assert.assertSame(sub, plusOnlyParallel.getPlus());

        Split<ConvexSubLine> hyper = sub.split(Line.fromPointAndDirection(start, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertNull(hyper.getMinus());
        Assert.assertNull(hyper.getPlus());
    }

    @Test
    public void testSplit_full() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 1);
        Vector2D p2 = Vector2D.of(3, 2);
        Vector2D middle = p1.lerp(p2, 0.5);

        Line line = Line.fromPoints(p1, p2, TEST_PRECISION);

        ConvexSubLine seg = ConvexSubLine.fromInterval(line, Interval.full());

        // act/assert
        Split<ConvexSubLine> both = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(1, -2), TEST_PRECISION));
        checkInfinite(both.getMinus(), line,  middle, null);
        checkInfinite(both.getPlus(), line, null, middle);

        Split<ConvexSubLine> bothReversed = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(-1, 2), TEST_PRECISION));
        checkInfinite(bothReversed.getMinus(), line,  null, middle);
        checkInfinite(bothReversed.getPlus(), line, middle, null);

        Split<ConvexSubLine> minusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertSame(seg, minusOnlyParallel.getMinus());
        Assert.assertNull(minusOnlyParallel.getPlus());

        Split<ConvexSubLine> plusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(-2, -1), TEST_PRECISION));
        Assert.assertNull(plusOnlyParallel.getMinus());
        Assert.assertSame(seg, plusOnlyParallel.getPlus());

        Split<ConvexSubLine> hyper = seg.split(Line.fromPointAndDirection(p1, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertNull(hyper.getMinus());
        Assert.assertNull(hyper.getPlus());
    }

    @Test
    public void testSplit_positiveHalfSpace() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 1);
        Vector2D p2 = Vector2D.of(3, 2);
        Vector2D middle = p1.lerp(p2, 0.5);

        Line line = Line.fromPoints(p1, p2, TEST_PRECISION);

        ConvexSubLine sub = ConvexSubLine.fromInterval(line, Interval.min(line.toSubspace(p1).getX(), TEST_PRECISION));

        // act/assert
        Split<ConvexSubLine> both = sub.split(Line.fromPointAndDirection(middle, Vector2D.of(1, -2), TEST_PRECISION));
        checkInfinite(both.getMinus(), line,  middle, null);
        checkFinite(both.getPlus(), p1, middle);

        Split<ConvexSubLine> bothReversed = sub.split(Line.fromPointAndDirection(middle, Vector2D.of(-1, 2), TEST_PRECISION));
        checkFinite(bothReversed.getMinus(), p1, middle);
        checkInfinite(bothReversed.getPlus(), line, middle, null);

        Split<ConvexSubLine> minusOnlyParallel = sub.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertSame(sub, minusOnlyParallel.getMinus());
        Assert.assertNull(minusOnlyParallel.getPlus());

        Split<ConvexSubLine> minusOnlyOrthogonal = sub.split(Line.fromPointAndDirection(p1, Vector2D.of(1, -2), TEST_PRECISION));
        Assert.assertSame(sub, minusOnlyOrthogonal.getMinus());
        Assert.assertNull(minusOnlyOrthogonal.getPlus());

        Split<ConvexSubLine> plusOnlyParallel = sub.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(-2, -1), TEST_PRECISION));
        Assert.assertNull(plusOnlyParallel.getMinus());
        Assert.assertSame(sub, plusOnlyParallel.getPlus());

        Split<ConvexSubLine> hyper = sub.split(Line.fromPointAndDirection(p1, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertNull(hyper.getMinus());
        Assert.assertNull(hyper.getPlus());
    }

    @Test
    public void testSplit_negativeHalfSpace() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 1);
        Vector2D p2 = Vector2D.of(3, 2);
        Vector2D middle = p1.lerp(p2, 0.5);

        Line line = Line.fromPoints(p1, p2, TEST_PRECISION);

        ConvexSubLine seg = ConvexSubLine.fromInterval(line, Interval.max(line.toSubspace(p2).getX(), TEST_PRECISION));

        // act/assert
        Split<ConvexSubLine> both = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(1, -2), TEST_PRECISION));
        checkFinite(both.getMinus(), middle, p2);
        checkInfinite(both.getPlus(), line, null, middle);

        Split<ConvexSubLine> bothReversed = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(-1, 2), TEST_PRECISION));
        checkInfinite(bothReversed.getMinus(), line, null, middle);
        checkFinite(bothReversed.getPlus(), middle, p2);

        Split<ConvexSubLine> minusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertSame(seg, minusOnlyParallel.getMinus());
        Assert.assertNull(minusOnlyParallel.getPlus());

        Split<ConvexSubLine> plusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(-2, -1), TEST_PRECISION));
        Assert.assertNull(plusOnlyParallel.getMinus());
        Assert.assertSame(seg, plusOnlyParallel.getPlus());

        Split<ConvexSubLine> plusOnlyOrthogonal = seg.split(Line.fromPointAndDirection(p2, Vector2D.of(1, -2), TEST_PRECISION));
        Assert.assertNull(plusOnlyOrthogonal.getMinus());
        Assert.assertSame(seg, plusOnlyOrthogonal.getPlus());

        Split<ConvexSubLine> hyper = seg.split(Line.fromPointAndDirection(p1, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertNull(hyper.getMinus());
        Assert.assertNull(hyper.getPlus());
    }

    private static void checkInterval(Interval expected, Interval actual) {
        Assert.assertEquals(expected.getMin(), actual.getMin(), TEST_EPS);
        Assert.assertEquals(expected.getMax(), actual.getMax(), TEST_EPS);
    }

    private static void checkFinite(ConvexSubLine segment, Vector2D start, Vector2D end) {
        checkFinite(segment, start, end, TEST_PRECISION);
    }

    private static void checkFinite(ConvexSubLine segment, Vector2D start, Vector2D end, DoublePrecisionContext precision) {
        Assert.assertFalse(segment.isInfinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);

        Line line = segment.getLine();
        Assert.assertEquals(HyperplaneLocation.ON, line.classify(segment.getStartPoint()));
        Assert.assertEquals(HyperplaneLocation.ON, line.classify(segment.getEndPoint()));

        Assert.assertEquals(line.toSubspace(segment.getStartPoint()).getX(), segment.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(line.toSubspace(segment.getEndPoint()).getX(), segment.getSubspaceEnd(), TEST_EPS);

        Assert.assertSame(precision, segment.getPrecision());
        Assert.assertSame(precision, line.getPrecision());
    }

    private static void checkInfinite(ConvexSubLine segment, Line line, Vector2D start, Vector2D end) {
        checkInfinite(segment, line, start, end, TEST_PRECISION);
    }

    private static void checkInfinite(ConvexSubLine segment, Line line, Vector2D start, Vector2D end,
            DoublePrecisionContext precision) {

        Assert.assertTrue(segment.isInfinite());

        Assert.assertEquals(line, segment.getLine());

        if (start == null) {
            Assert.assertNull(segment.getStartPoint());
        } else {
            EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
            Assert.assertEquals(line.toSubspace(segment.getStartPoint()).getX(), segment.getSubspaceStart(), TEST_EPS);
        }

        if (end == null) {
            Assert.assertNull(segment.getEndPoint());
        } else {
            EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
            Assert.assertEquals(line.toSubspace(segment.getEndPoint()).getX(), segment.getSubspaceEnd(), TEST_EPS);
        }

        Assert.assertSame(precision, segment.getPrecision());
        Assert.assertSame(precision, line.getPrecision());
    }
}
