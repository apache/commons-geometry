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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.partition.HyperplaneLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.junit.Assert;
import org.junit.Test;

public class LineSegmentTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPoints() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 3);
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(-3, 4);
        Vector2D p3 = Vector2D.of(-5, -6);

        // act/assert

        checkSegment(LineSegment.fromPoints(p0, p1, TEST_PRECISION), p0, p1);
        checkSegment(LineSegment.fromPoints(p1, p0, TEST_PRECISION), p1, p0);

        checkSegment(LineSegment.fromPoints(p0, p2, TEST_PRECISION), p0, p2);
        checkSegment(LineSegment.fromPoints(p2, p0, TEST_PRECISION), p2, p0);

        checkSegment(LineSegment.fromPoints(p0, p3, TEST_PRECISION), p0, p3);
        checkSegment(LineSegment.fromPoints(p3, p0, TEST_PRECISION), p3, p0);
    }

    @Test
    public void testFromPoints_invalidArgs() {
        // arrange
        Vector2D p0 = Vector2D.of(-1, 2);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            LineSegment.fromPoints(p0, p0, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            LineSegment.fromPoints(p0, Vector2D.POSITIVE_INFINITY, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            LineSegment.fromPoints(p0, Vector2D.NEGATIVE_INFINITY, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            LineSegment.fromPoints(p0, Vector2D.NaN, TEST_PRECISION);
        }, GeometryValueException.class);
    }

    @Test
    public void testFromInterval_intervalArg_finite() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(-1, 2, intervalPrecision);

        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegment segment = LineSegment.fromInterval(line, interval);

        // assert
        double side = 1.0 / Math.sqrt(2);
        checkSegment(segment, Vector2D.of(-side, -side), Vector2D.of(2 * side, 2 * side));

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_intervalArg_full() {
        // arrange
        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegment segment = LineSegment.fromInterval(line, Interval.full());

        // assert
        GeometryTestUtils.assertNegativeInfinity(segment.getSubspaceStart());
        GeometryTestUtils.assertPositiveInfinity(segment.getSubspaceEnd());

        Assert.assertNull(segment.getStart());
        Assert.assertNull(segment.getEnd());

        Assert.assertSame(Interval.full(), segment.getInterval());
        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_intervalArg_positiveHalfSpace() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.fromMin(-1, intervalPrecision);

        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegment segment = LineSegment.fromInterval(line, interval);

        // assert
        Assert.assertEquals(-1.0, segment.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(segment.getSubspaceEnd());

        double side = 1.0 / Math.sqrt(2);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-side, -side), segment.getStart(), TEST_EPS);
        Assert.assertNull(segment.getEnd());

        Assert.assertSame(interval, segment.getInterval());
        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_intervalArg_negativeHalfSpace() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.fromMax(2, intervalPrecision);

        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegment segment = LineSegment.fromInterval(line, interval);

        // assert
        GeometryTestUtils.assertNegativeInfinity(segment.getSubspaceStart());
        Assert.assertEquals(2, segment.getSubspaceEnd(), TEST_EPS);

        double side = 1.0 / Math.sqrt(2);

        Assert.assertNull(segment.getStart());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2 * side, 2 * side), segment.getEnd(), TEST_EPS);

        Assert.assertSame(interval, segment.getInterval());
        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_finite() {
        // arrange
        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegment segment = LineSegment.fromInterval(line, -1, 2);

        // assert
        double side = 1.0 / Math.sqrt(2);
        checkSegment(segment, Vector2D.of(-side, -side), Vector2D.of(2 * side, 2 * side));

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_full() {
        // arrange
        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegment segment = LineSegment.fromInterval(line, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        // assert
        GeometryTestUtils.assertNegativeInfinity(segment.getSubspaceStart());
        GeometryTestUtils.assertPositiveInfinity(segment.getSubspaceEnd());

        Assert.assertNull(segment.getStart());
        Assert.assertNull(segment.getEnd());

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_positiveHalfSpace() {
        // arrange
        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegment segment = LineSegment.fromInterval(line, -1, Double.POSITIVE_INFINITY);

        // assert
        Assert.assertEquals(-1.0, segment.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(segment.getSubspaceEnd());

        double side = 1.0 / Math.sqrt(2);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-side, -side), segment.getStart(), TEST_EPS);
        Assert.assertNull(segment.getEnd());

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_negativeHalfSpace() {
        // arrange
        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegment segment = LineSegment.fromInterval(line, 2, Double.NEGATIVE_INFINITY);

        // assert
        GeometryTestUtils.assertNegativeInfinity(segment.getSubspaceStart());
        Assert.assertEquals(2, segment.getSubspaceEnd(), TEST_EPS);

        double side = 1.0 / Math.sqrt(2);

        Assert.assertNull(segment.getStart());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2 * side, 2 * side), segment.getEnd(), TEST_EPS);

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_vectorArgs() {
        // arrange
        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegment segment = LineSegment.fromInterval(line, Vector1D.of(-1), Vector1D.of(2));

        // assert
        double side = 1.0 / Math.sqrt(2);
        checkSegment(segment, Vector2D.of(-side, -side), Vector2D.of(2 * side, 2 * side));

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testIsFull() {
        // arrange
        Line line = Line.fromPoints(Vector2D.of(-1, 0), Vector2D.of(4, 5), TEST_PRECISION);

        // act/assert
        Assert.assertTrue(LineSegment.fromInterval(line, Interval.full()).isFull());
        Assert.assertTrue(LineSegment.fromInterval(line, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isFull());

        Assert.assertFalse(LineSegment.fromInterval(line, Interval.fromMin(0, TEST_PRECISION)).isFull());
        Assert.assertFalse(LineSegment.fromInterval(line, Interval.fromMax(0, TEST_PRECISION)).isFull());

        Assert.assertFalse(LineSegment.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).isFull());

        Assert.assertFalse(LineSegment.fromInterval(line, Interval.point(1, TEST_PRECISION)).isEmpty());
    }

    @Test
    public void testIsInfinite() {
        // arrange
        Line line = Line.fromPoints(Vector2D.of(-1, 0), Vector2D.of(4, 5), TEST_PRECISION);

        // act/assert
        Assert.assertTrue(LineSegment.fromInterval(line, Interval.full()).isInfinite());
        Assert.assertTrue(LineSegment.fromInterval(line, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isInfinite());

        Assert.assertTrue(LineSegment.fromInterval(line, Interval.fromMin(0, TEST_PRECISION)).isInfinite());
        Assert.assertTrue(LineSegment.fromInterval(line, Interval.fromMax(0, TEST_PRECISION)).isInfinite());

        Assert.assertFalse(LineSegment.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).isEmpty());

        Assert.assertFalse(LineSegment.fromInterval(line, Interval.point(1, TEST_PRECISION)).isEmpty());
    }

    @Test
    public void testIsEmpty_alwaysReturnsFalse() {
        // arrange
        Line line = Line.fromPoints(Vector2D.of(-1, 0), Vector2D.of(4, 5), TEST_PRECISION);

        // act/assert
        Assert.assertFalse(LineSegment.fromInterval(line, Interval.full()).isEmpty());
        Assert.assertFalse(LineSegment.fromInterval(line, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isEmpty());

        Assert.assertFalse(LineSegment.fromInterval(line, Interval.fromMin(0, TEST_PRECISION)).isEmpty());
        Assert.assertFalse(LineSegment.fromInterval(line, Interval.fromMax(0, TEST_PRECISION)).isEmpty());

        Assert.assertFalse(LineSegment.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).isEmpty());

        Assert.assertFalse(LineSegment.fromInterval(line, Interval.point(1, TEST_PRECISION)).isEmpty());
    }

    @Test
    public void testGetSize() {
        // arrange
        Line line = Line.fromPoints(Vector2D.of(-1, 0), Vector2D.of(4, 5), TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(LineSegment.fromInterval(line, Interval.full()).getSize());
        GeometryTestUtils.assertPositiveInfinity(LineSegment.fromInterval(line, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).getSize());

        GeometryTestUtils.assertPositiveInfinity(LineSegment.fromInterval(line, Interval.fromMin(0, TEST_PRECISION)).getSize());
        GeometryTestUtils.assertPositiveInfinity(LineSegment.fromInterval(line, Interval.fromMax(0, TEST_PRECISION)).getSize());

        Assert.assertEquals(Math.sqrt(2), LineSegment.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).getSize(), TEST_EPS);
        Assert.assertEquals(9.0, LineSegment.fromPoints(Vector2D.of(1, 1), Vector2D.of(1, 10), TEST_PRECISION).getSize(), TEST_EPS);

        Assert.assertEquals(0.0, LineSegment.fromInterval(line, Interval.point(1, TEST_PRECISION)).getSize(), TEST_EPS);
        Assert.assertEquals(1.0, LineSegment.fromInterval(line, Interval.of(1, 2, TEST_PRECISION)).getSize(), TEST_EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        LineSegment segment = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 2), precision);

        // act/assert
        checkClassify(segment, RegionLocation.OUTSIDE,
                Vector2D.of(0.25, 1), Vector2D.of(0.75, 1),
                Vector2D.of(-1, -2), Vector2D.of(-0.1, 0),
                Vector2D.of(1.1, 2), Vector2D.of(2, 4));

        checkClassify(segment, RegionLocation.BOUNDARY,
                Vector2D.ZERO, Vector2D.of(0.005, 0),
                Vector2D.of(1, 2), Vector2D.of(1, 1.995));

        checkClassify(segment, RegionLocation.INSIDE,
                Vector2D.of(0.25, 0.5), Vector2D.of(0.495, 1),
                Vector2D.of(0.75, 1.5));
    }

    @Test
    public void testClosest() {
        // arrange
        LineSegment segment = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, segment.closest(Vector2D.of(-1, -1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, segment.closest(Vector2D.of(-2, 2)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), segment.closest(Vector2D.of(0.5, 0.5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), segment.closest(Vector2D.of(0, 1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), segment.closest(Vector2D.of(1, 0)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segment.closest(Vector2D.of(2, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segment.closest(Vector2D.of(5, 10)), TEST_EPS);
    }

    @Test
    public void testToConvex() {
        // arrange
        LineSegment segment = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        List<LineSegment> segments = segment.toConvex();

        // assert
        Assert.assertEquals(1, segments.size());
        Assert.assertSame(segment, segments.get(0));
    }

    @Test
    public void testTransform_finite() {
        // arrange
        LineSegment segment = LineSegment.fromPoints(Vector2D.of(0, 1), Vector2D.of(2, 3), TEST_PRECISION);

        Transform<Vector2D> translation = AffineTransformMatrix2D.createTranslation(-1, 1);
        Transform<Vector2D> rotation = AffineTransformMatrix2D.createRotation(Geometry.HALF_PI);
        Transform<Vector2D> scale = AffineTransformMatrix2D.createScale(2, 3);
        Transform<Vector2D> reflect = (pt) -> Vector2D.of(pt.getX(), -pt.getY());

        // act/assert
        checkSegment(segment.transform(translation), Vector2D.of(-1, 2), Vector2D.of(1, 4));
        checkSegment(segment.transform(rotation), Vector2D.of(-1, 0), Vector2D.of(-3, 2));
        checkSegment(segment.transform(scale), Vector2D.of(0, 3), Vector2D.of(4, 9));
        checkSegment(segment.transform(reflect), Vector2D.of(0, -1), Vector2D.of(2, -3));
    }

    private static void checkClassify(LineSegment segment, RegionLocation loc, Vector2D ... points) {
        for (Vector2D pt : points) {
            String msg = "Unexpected location for point " + pt;

            Assert.assertEquals(msg, loc, segment.classify(pt));
        }
    }

    private static void checkSegment(LineSegment segment, Vector2D start, Vector2D end) {
        checkSegment(segment, start, end, TEST_PRECISION);
    }

    private static void checkSegment(LineSegment segment, Vector2D start, Vector2D end, DoublePrecisionContext precision) {
        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStart(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEnd(), TEST_EPS);

        Line line = segment.getLine();
        Assert.assertEquals(HyperplaneLocation.ON, line.classify(segment.getStart()));
        Assert.assertEquals(HyperplaneLocation.ON, line.classify(segment.getEnd()));

        Assert.assertEquals(line.toSubspace(segment.getStart()).getX(), segment.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(line.toSubspace(segment.getEnd()).getX(), segment.getSubspaceEnd(), TEST_EPS);

        Assert.assertSame(precision, segment.getPrecision());
        Assert.assertSame(precision, line.getPrecision());
    }
}
