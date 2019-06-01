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
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane.Split;
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

        checkFiniteSegment(LineSegment.fromPoints(p0, p1, TEST_PRECISION), p0, p1);
        checkFiniteSegment(LineSegment.fromPoints(p1, p0, TEST_PRECISION), p1, p0);

        checkFiniteSegment(LineSegment.fromPoints(p0, p2, TEST_PRECISION), p0, p2);
        checkFiniteSegment(LineSegment.fromPoints(p2, p0, TEST_PRECISION), p2, p0);

        checkFiniteSegment(LineSegment.fromPoints(p0, p3, TEST_PRECISION), p0, p3);
        checkFiniteSegment(LineSegment.fromPoints(p3, p0, TEST_PRECISION), p3, p0);
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
        checkFiniteSegment(segment, Vector2D.of(-side, -side), Vector2D.of(2 * side, 2 * side));

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

        Assert.assertNull(segment.getStartPoint());
        Assert.assertNull(segment.getEndPoint());

        Assert.assertSame(Interval.full(), segment.getInterval());
        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_intervalArg_positiveHalfSpace() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.min(-1, intervalPrecision);

        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegment segment = LineSegment.fromInterval(line, interval);

        // assert
        Assert.assertEquals(-1.0, segment.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(segment.getSubspaceEnd());

        double side = 1.0 / Math.sqrt(2);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-side, -side), segment.getStartPoint(), TEST_EPS);
        Assert.assertNull(segment.getEndPoint());

        Assert.assertSame(interval, segment.getInterval());
        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_intervalArg_negativeHalfSpace() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.max(2, intervalPrecision);

        Line line = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act
        LineSegment segment = LineSegment.fromInterval(line, interval);

        // assert
        GeometryTestUtils.assertNegativeInfinity(segment.getSubspaceStart());
        Assert.assertEquals(2, segment.getSubspaceEnd(), TEST_EPS);

        double side = 1.0 / Math.sqrt(2);

        Assert.assertNull(segment.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2 * side, 2 * side), segment.getEndPoint(), TEST_EPS);

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
        checkFiniteSegment(segment, Vector2D.of(-side, -side), Vector2D.of(2 * side, 2 * side));

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

        Assert.assertNull(segment.getStartPoint());
        Assert.assertNull(segment.getEndPoint());

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

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-side, -side), segment.getStartPoint(), TEST_EPS);
        Assert.assertNull(segment.getEndPoint());

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

        Assert.assertNull(segment.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2 * side, 2 * side), segment.getEndPoint(), TEST_EPS);

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
        checkFiniteSegment(segment, Vector2D.of(-side, -side), Vector2D.of(2 * side, 2 * side));

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testIsFull() {
        // arrange
        Line line = Line.fromPoints(Vector2D.of(-1, 0), Vector2D.of(4, 5), TEST_PRECISION);

        // act/assert
        Assert.assertTrue(LineSegment.fromInterval(line, Interval.full()).isFull());
        Assert.assertTrue(LineSegment.fromInterval(line, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isFull());

        Assert.assertFalse(LineSegment.fromInterval(line, Interval.min(0, TEST_PRECISION)).isFull());
        Assert.assertFalse(LineSegment.fromInterval(line, Interval.max(0, TEST_PRECISION)).isFull());

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

        Assert.assertTrue(LineSegment.fromInterval(line, Interval.min(0, TEST_PRECISION)).isInfinite());
        Assert.assertTrue(LineSegment.fromInterval(line, Interval.max(0, TEST_PRECISION)).isInfinite());

        Assert.assertFalse(LineSegment.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).isInfinite());
        Assert.assertFalse(LineSegment.fromInterval(line, Interval.point(1, TEST_PRECISION)).isInfinite());
    }

    @Test
    public void testIsFinite() {
        // arrange
        Line line = Line.fromPoints(Vector2D.of(-1, 0), Vector2D.of(4, 5), TEST_PRECISION);

        // act/assert
        Assert.assertTrue(LineSegment.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).isFinite());
        Assert.assertTrue(LineSegment.fromInterval(line, Interval.point(1, TEST_PRECISION)).isFinite());

        Assert.assertFalse(LineSegment.fromInterval(line, Interval.full()).isFinite());
        Assert.assertFalse(LineSegment.fromInterval(line, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isFinite());

        Assert.assertFalse(LineSegment.fromInterval(line, Interval.min(0, TEST_PRECISION)).isFinite());
        Assert.assertFalse(LineSegment.fromInterval(line, Interval.max(0, TEST_PRECISION)).isFinite());
    }

    @Test
    public void testIsEmpty_alwaysReturnsFalse() {
        // arrange
        Line line = Line.fromPoints(Vector2D.of(-1, 0), Vector2D.of(4, 5), TEST_PRECISION);

        // act/assert
        Assert.assertFalse(LineSegment.fromInterval(line, Interval.full()).isEmpty());
        Assert.assertFalse(LineSegment.fromInterval(line, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isEmpty());

        Assert.assertFalse(LineSegment.fromInterval(line, Interval.min(0, TEST_PRECISION)).isEmpty());
        Assert.assertFalse(LineSegment.fromInterval(line, Interval.max(0, TEST_PRECISION)).isEmpty());

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

        GeometryTestUtils.assertPositiveInfinity(LineSegment.fromInterval(line, Interval.min(0, TEST_PRECISION)).getSize());
        GeometryTestUtils.assertPositiveInfinity(LineSegment.fromInterval(line, Interval.max(0, TEST_PRECISION)).getSize());

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
        checkFiniteSegment(segment.transform(translation), Vector2D.of(-1, 2), Vector2D.of(1, 4));
        checkFiniteSegment(segment.transform(rotation), Vector2D.of(-1, 0), Vector2D.of(-3, 2));
        checkFiniteSegment(segment.transform(scale), Vector2D.of(0, 3), Vector2D.of(4, 9));
        checkFiniteSegment(segment.transform(reflect), Vector2D.of(0, -1), Vector2D.of(2, -3));
    }

    @Test
    public void testTransform_singlePoint() {
        // arrange
        LineSegment segment = LineSegment.fromInterval(Line.fromPoints(Vector2D.of(0, 1), Vector2D.of(1, 1), TEST_PRECISION),
                Interval.point(0, TEST_PRECISION));

        Transform<Vector2D> translation = AffineTransformMatrix2D.createTranslation(-1, 1);
        Transform<Vector2D> rotation = AffineTransformMatrix2D.createRotation(Geometry.HALF_PI);
        Transform<Vector2D> scale = AffineTransformMatrix2D.createScale(2, 3);
        Transform<Vector2D> reflect = (pt) -> Vector2D.of(pt.getX(), -pt.getY());

        // act/assert
        checkFiniteSegment(segment.transform(translation), Vector2D.of(-1, 2), Vector2D.of(-1, 2));
        checkFiniteSegment(segment.transform(rotation), Vector2D.of(-1, 0), Vector2D.of(-1, 0));
        checkFiniteSegment(segment.transform(scale), Vector2D.of(0, 3), Vector2D.of(0, 3));
        checkFiniteSegment(segment.transform(reflect), Vector2D.of(0, -1), Vector2D.of(0, -1));
    }

    @Test
    public void testTransform_full() {
        // arrange
        LineSegment segment = LineSegment.fromInterval(Line.fromPoints(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION),
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        Transform<Vector2D> translation = AffineTransformMatrix2D.createTranslation(-1, 1);
        Transform<Vector2D> rotation = AffineTransformMatrix2D.createRotation(Geometry.HALF_PI);
        Transform<Vector2D> scale = AffineTransformMatrix2D.createScale(2, 3);
        Transform<Vector2D> reflect = (pt) -> Vector2D.of(pt.getX(), -pt.getY());

        // act/assert
        LineSegment translated = segment.transform(translation);
        Assert.assertTrue(translated.isFull());
        Assert.assertTrue(translated.contains(Vector2D.of(-1, 1)));
        Assert.assertTrue(translated.contains(Vector2D.of(1, 2)));
        Assert.assertNull(translated.getStartPoint());
        Assert.assertNull(translated.getEndPoint());

        LineSegment rotated = segment.transform(rotation);
        Assert.assertTrue(rotated.isFull());
        Assert.assertTrue(rotated.contains(Vector2D.ZERO));
        Assert.assertTrue(rotated.contains(Vector2D.of(-1, 2)));
        Assert.assertNull(rotated.getStartPoint());
        Assert.assertNull(rotated.getEndPoint());

        LineSegment scaled = segment.transform(scale);
        Assert.assertTrue(scaled.isFull());
        Assert.assertTrue(scaled.contains(Vector2D.ZERO));
        Assert.assertTrue(scaled.contains(Vector2D.of(4, 3)));
        Assert.assertNull(scaled.getStartPoint());
        Assert.assertNull(scaled.getEndPoint());

        LineSegment reflected = segment.transform(reflect);
        Assert.assertTrue(reflected.isFull());
        Assert.assertTrue(reflected.contains(Vector2D.ZERO));
        Assert.assertTrue(reflected.contains(Vector2D.of(2, -1)));
        Assert.assertNull(reflected.getStartPoint());
        Assert.assertNull(reflected.getEndPoint());
    }

    @Test
    public void testTransform_positiveHalfspace() {
        // arrange
        LineSegment segment = LineSegment.fromInterval(Line.fromPoints(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION),
                0.0, Double.POSITIVE_INFINITY);

        Transform<Vector2D> translation = AffineTransformMatrix2D.createTranslation(-1, 1);
        Transform<Vector2D> rotation = AffineTransformMatrix2D.createRotation(Geometry.HALF_PI);
        Transform<Vector2D> scale = AffineTransformMatrix2D.createScale(2, 3);
        Transform<Vector2D> reflect = (pt) -> Vector2D.of(pt.getX(), -pt.getY());

        // act/assert
        LineSegment translated = segment.transform(translation);
        Assert.assertTrue(translated.isInfinite());
        Assert.assertTrue(translated.contains(Vector2D.of(-1, 1)));
        Assert.assertTrue(translated.contains(Vector2D.of(1, 2)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), translated.getStartPoint(), TEST_EPS);
        Assert.assertNull(translated.getEndPoint());

        LineSegment rotated = segment.transform(rotation);
        Assert.assertTrue(rotated.isInfinite());
        Assert.assertTrue(rotated.contains(Vector2D.ZERO));
        Assert.assertTrue(rotated.contains(Vector2D.of(-1, 2)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, rotated.getStartPoint(), TEST_EPS);
        Assert.assertNull(rotated.getEndPoint());

        LineSegment scaled = segment.transform(scale);
        Assert.assertTrue(scaled.isInfinite());
        Assert.assertTrue(scaled.contains(Vector2D.ZERO));
        Assert.assertTrue(scaled.contains(Vector2D.of(4, 3)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, scaled.getStartPoint(), TEST_EPS);
        Assert.assertNull(scaled.getEndPoint());

        LineSegment reflected = segment.transform(reflect);
        Assert.assertTrue(reflected.isInfinite());
        Assert.assertTrue(reflected.contains(Vector2D.ZERO));
        Assert.assertTrue(reflected.contains(Vector2D.of(2, -1)));
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, reflected.getStartPoint(), TEST_EPS);
        Assert.assertNull(reflected.getEndPoint());
    }

    @Test
    public void testTransform_negativeHalfspace() {
        // arrange
        LineSegment segment = LineSegment.fromInterval(Line.fromPoints(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION),
                Double.NEGATIVE_INFINITY, 0.0);

        Transform<Vector2D> translation = AffineTransformMatrix2D.createTranslation(-1, 1);
        Transform<Vector2D> rotation = AffineTransformMatrix2D.createRotation(Geometry.HALF_PI);
        Transform<Vector2D> scale = AffineTransformMatrix2D.createScale(2, 3);
        Transform<Vector2D> reflect = (pt) -> Vector2D.of(pt.getX(), -pt.getY());

        // act/assert
        LineSegment translated = segment.transform(translation);
        Assert.assertTrue(translated.isInfinite());
        Assert.assertTrue(translated.contains(Vector2D.of(-1, 1)));
        Assert.assertTrue(translated.contains(Vector2D.of(-3, 0)));
        Assert.assertNull(translated.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), translated.getEndPoint(), TEST_EPS);

        LineSegment rotated = segment.transform(rotation);
        Assert.assertTrue(rotated.isInfinite());
        Assert.assertTrue(rotated.contains(Vector2D.ZERO));
        Assert.assertTrue(rotated.contains(Vector2D.of(1, -2)));
        Assert.assertNull(rotated.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, rotated.getEndPoint(), TEST_EPS);

        LineSegment scaled = segment.transform(scale);
        Assert.assertTrue(scaled.isInfinite());
        Assert.assertTrue(scaled.contains(Vector2D.ZERO));
        Assert.assertTrue(scaled.contains(Vector2D.of(-4, -3)));
        Assert.assertNull(scaled.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, scaled.getEndPoint(), TEST_EPS);

        LineSegment reflected = segment.transform(reflect);
        Assert.assertTrue(reflected.isInfinite());
        Assert.assertTrue(reflected.contains(Vector2D.ZERO));
        Assert.assertTrue(reflected.contains(Vector2D.of(-2, 1)));
        Assert.assertNull(reflected.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, reflected.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testSplit_finite() {
        // arrange
        Vector2D start = Vector2D.of(1, 1);
        Vector2D end = Vector2D.of(3, 2);
        Vector2D middle = start.lerp(end, 0.5);

        LineSegment seg = LineSegment.fromPoints(start, end, TEST_PRECISION);

        // act/assert
        Split<Vector2D> both = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(1, -2), TEST_PRECISION));
        checkFiniteSegment((LineSegment) both.getMinus(), middle, end);
        checkFiniteSegment((LineSegment) both.getPlus(), start, middle);

        Split<Vector2D> bothReversed = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(-1, 2), TEST_PRECISION));
        checkFiniteSegment((LineSegment) bothReversed.getMinus(), start, middle);
        checkFiniteSegment((LineSegment) bothReversed.getPlus(), middle, end);

        Split<Vector2D> minusOnlyOrthogonal = seg.split(Line.fromPointAndDirection(start, Vector2D.of(1, -2), TEST_PRECISION));
        Assert.assertSame(seg, minusOnlyOrthogonal.getMinus());
        Assert.assertNull(minusOnlyOrthogonal.getPlus());

        Split<Vector2D> minusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertSame(seg, minusOnlyParallel.getMinus());
        Assert.assertNull(minusOnlyParallel.getPlus());

        Split<Vector2D> plusOnlyOrthogonal = seg.split(Line.fromPointAndDirection(end, Vector2D.of(1, -2), TEST_PRECISION));
        Assert.assertNull(plusOnlyOrthogonal.getMinus());
        Assert.assertSame(seg, plusOnlyOrthogonal.getPlus());

        Split<Vector2D> plusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(-2, -1), TEST_PRECISION));
        Assert.assertNull(plusOnlyParallel.getMinus());
        Assert.assertSame(seg, plusOnlyParallel.getPlus());

        Split<Vector2D> hyper = seg.split(Line.fromPointAndDirection(start, Vector2D.of(2, 1), TEST_PRECISION));
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

        LineSegment seg = LineSegment.fromInterval(line, Interval.full());

        // act/assert
        Split<Vector2D> both = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(1, -2), TEST_PRECISION));
        checkInfiniteSegment((LineSegment) both.getMinus(), line,  middle, null);
        checkInfiniteSegment((LineSegment) both.getPlus(), line, null, middle);

        Split<Vector2D> bothReversed = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(-1, 2), TEST_PRECISION));
        checkInfiniteSegment((LineSegment) bothReversed.getMinus(), line,  null, middle);
        checkInfiniteSegment((LineSegment) bothReversed.getPlus(), line, middle, null);

        Split<Vector2D> minusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertSame(seg, minusOnlyParallel.getMinus());
        Assert.assertNull(minusOnlyParallel.getPlus());

        Split<Vector2D> plusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(-2, -1), TEST_PRECISION));
        Assert.assertNull(plusOnlyParallel.getMinus());
        Assert.assertSame(seg, plusOnlyParallel.getPlus());

        Split<Vector2D> hyper = seg.split(Line.fromPointAndDirection(p1, Vector2D.of(2, 1), TEST_PRECISION));
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

        LineSegment seg = LineSegment.fromInterval(line, Interval.min(line.toSubspace(p1).getX(), TEST_PRECISION));

        // act/assert
        Split<Vector2D> both = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(1, -2), TEST_PRECISION));
        checkInfiniteSegment((LineSegment) both.getMinus(), line,  middle, null);
        checkFiniteSegment((LineSegment) both.getPlus(), p1, middle);

        Split<Vector2D> bothReversed = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(-1, 2), TEST_PRECISION));
        checkFiniteSegment((LineSegment) bothReversed.getMinus(), p1, middle);
        checkInfiniteSegment((LineSegment) bothReversed.getPlus(), line, middle, null);

        Split<Vector2D> minusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertSame(seg, minusOnlyParallel.getMinus());
        Assert.assertNull(minusOnlyParallel.getPlus());

        Split<Vector2D> minusOnlyOrthogonal = seg.split(Line.fromPointAndDirection(p1, Vector2D.of(1, -2), TEST_PRECISION));
        Assert.assertSame(seg, minusOnlyOrthogonal.getMinus());
        Assert.assertNull(minusOnlyOrthogonal.getPlus());

        Split<Vector2D> plusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(-2, -1), TEST_PRECISION));
        Assert.assertNull(plusOnlyParallel.getMinus());
        Assert.assertSame(seg, plusOnlyParallel.getPlus());

        Split<Vector2D> hyper = seg.split(Line.fromPointAndDirection(p1, Vector2D.of(2, 1), TEST_PRECISION));
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

        LineSegment seg = LineSegment.fromInterval(line, Interval.max(line.toSubspace(p2).getX(), TEST_PRECISION));

        // act/assert
        Split<Vector2D> both = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(1, -2), TEST_PRECISION));
        checkFiniteSegment((LineSegment) both.getMinus(), middle, p2);
        checkInfiniteSegment((LineSegment) both.getPlus(), line, null, middle);

        Split<Vector2D> bothReversed = seg.split(Line.fromPointAndDirection(middle, Vector2D.of(-1, 2), TEST_PRECISION));
        checkInfiniteSegment((LineSegment) bothReversed.getMinus(), line, null, middle);
        checkFiniteSegment((LineSegment) bothReversed.getPlus(), middle, p2);

        Split<Vector2D> minusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertSame(seg, minusOnlyParallel.getMinus());
        Assert.assertNull(minusOnlyParallel.getPlus());

        Split<Vector2D> plusOnlyParallel = seg.split(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(-2, -1), TEST_PRECISION));
        Assert.assertNull(plusOnlyParallel.getMinus());
        Assert.assertSame(seg, plusOnlyParallel.getPlus());

        Split<Vector2D> plusOnlyOrthogonal = seg.split(Line.fromPointAndDirection(p2, Vector2D.of(1, -2), TEST_PRECISION));
        Assert.assertNull(plusOnlyOrthogonal.getMinus());
        Assert.assertSame(seg, plusOnlyOrthogonal.getPlus());

        Split<Vector2D> hyper = seg.split(Line.fromPointAndDirection(p1, Vector2D.of(2, 1), TEST_PRECISION));
        Assert.assertNull(hyper.getMinus());
        Assert.assertNull(hyper.getPlus());
    }

    @Test
    public void testHashCode() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-5);

        LineSegment a = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

        LineSegment b = LineSegment.fromPoints(Vector2D.of(-1, 0), Vector2D.PLUS_X, TEST_PRECISION);
        LineSegment c = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(2, 0), TEST_PRECISION);
        LineSegment d = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, precision);

        LineSegment e = LineSegment.fromInterval(Line.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION),
                Interval.of(0, 1, TEST_PRECISION));

        // act/assert
        Assert.assertEquals(a.hashCode(), a.hashCode());

        Assert.assertNotEquals(a.hashCode(), b.hashCode());
        Assert.assertNotEquals(a.hashCode(), c.hashCode());
        Assert.assertNotEquals(a.hashCode(), d.hashCode());

        Assert.assertEquals(a.hashCode(), e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-5);

        LineSegment a = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

        LineSegment b = LineSegment.fromPoints(Vector2D.of(-1, 0), Vector2D.PLUS_X, TEST_PRECISION);
        LineSegment c = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(2, 0), TEST_PRECISION);
        LineSegment d = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, precision);

        LineSegment e = LineSegment.fromInterval(Line.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION),
                Interval.of(0, 1, TEST_PRECISION));

        // act/assert
        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));
        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(a.equals(d));

        Assert.assertTrue(a.equals(e));
        Assert.assertTrue(e.equals(a));
    }

    @Test
    public void testToString() {
        // arrange
        LineSegment segment = LineSegment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

        // act
        String str = segment.toString();

        // assert
        Assert.assertTrue(str.contains("LineSegment"));
        Assert.assertTrue(str.contains("start= (0.0, 0.0)"));
        Assert.assertTrue(str.contains("end= (1.0, 0.0)"));
    }

    private static void checkClassify(LineSegment segment, RegionLocation loc, Vector2D ... points) {
        for (Vector2D pt : points) {
            String msg = "Unexpected location for point " + pt;

            Assert.assertEquals(msg, loc, segment.classify(pt));
        }
    }

    private static void checkFiniteSegment(LineSegment segment, Vector2D start, Vector2D end) {
        checkFiniteSegment(segment, start, end, TEST_PRECISION);
    }

    private static void checkFiniteSegment(LineSegment segment, Vector2D start, Vector2D end, DoublePrecisionContext precision) {
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

    private static void checkInfiniteSegment(LineSegment segment, Line line, Vector2D start, Vector2D end) {
        checkInfiniteSegment(segment, line, start, end, TEST_PRECISION);
    }

    private static void checkInfiniteSegment(LineSegment segment, Line line, Vector2D start, Vector2D end,
            DoublePrecisionContext precision) {

        Assert.assertTrue(segment.isInfinite());

        Assert.assertEquals(line, segment.getLine());

        if (start == null) {
            Assert.assertNull(segment.getStartPoint());
        }
        else {
            EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
            Assert.assertEquals(line.toSubspace(segment.getStartPoint()).getX(), segment.getSubspaceStart(), TEST_EPS);
        }

        if (end == null) {
            Assert.assertNull(segment.getEndPoint());
        }
        else {
            EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
            Assert.assertEquals(line.toSubspace(segment.getEndPoint()).getX(), segment.getSubspaceEnd(), TEST_EPS);
        }

        Assert.assertSame(precision, segment.getPrecision());
        Assert.assertSame(precision, line.getPrecision());
    }
}
