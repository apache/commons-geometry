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
package org.apache.commons.geometry.euclidean.threed;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.junit.Assert;
import org.junit.Test;

public class Segment3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPoints() {
        // arrange
        Vector3D p0 = Vector3D.of(1, 3, 2);
        Vector3D p1 = Vector3D.of(1, 2, 3);
        Vector3D p2 = Vector3D.of(-3, 4, 5);
        Vector3D p3 = Vector3D.of(-5, -6, -8);

        // act/assert

        checkFiniteSegment(Segment3D.fromPoints(p0, p1, TEST_PRECISION), p0, p1);
        checkFiniteSegment(Segment3D.fromPoints(p1, p0, TEST_PRECISION), p1, p0);

        checkFiniteSegment(Segment3D.fromPoints(p0, p2, TEST_PRECISION), p0, p2);
        checkFiniteSegment(Segment3D.fromPoints(p2, p0, TEST_PRECISION), p2, p0);

        checkFiniteSegment(Segment3D.fromPoints(p0, p3, TEST_PRECISION), p0, p3);
        checkFiniteSegment(Segment3D.fromPoints(p3, p0, TEST_PRECISION), p3, p0);
    }

    @Test
    public void testFromPoints_invalidArgs() {
        // arrange
        Vector3D p0 = Vector3D.of(-1, 2, -3);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Segment3D.fromPoints(p0, p0, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            Segment3D.fromPoints(p0, Vector3D.POSITIVE_INFINITY, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            Segment3D.fromPoints(p0, Vector3D.NEGATIVE_INFINITY, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            Segment3D.fromPoints(p0, Vector3D.NaN, TEST_PRECISION);
        }, GeometryValueException.class);
    }

    @Test
    public void testFromPointAndDirection() {
        // act
        Segment3D seg = Segment3D.fromPointAndDirection(Vector3D.of(1, 3, -2), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 3, -2), seg.getStartPoint(), TEST_EPS);
        Assert.assertNull(seg.getEndPoint());

        Line3D line = seg.getLine();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, -2), line.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Y, line.getDirection(), TEST_EPS);
    }

    @Test
    public void testFromInterval_intervalArg_finite() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(-1, 2, intervalPrecision);

        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = Segment3D.fromInterval(line, interval);

        // assert
        double side = 1.0 / Math.sqrt(3);
        checkFiniteSegment(segment, Vector3D.of(-side, -side, -side), Vector3D.of(2 * side, 2 * side, 2 * side));

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_intervalArg_full() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = Segment3D.fromInterval(line, Interval.full());

        // assert
        Assert.assertTrue(segment.isInfinite());
        Assert.assertFalse(segment.isFinite());

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

        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = Segment3D.fromInterval(line, interval);

        // assert
        Assert.assertTrue(segment.isInfinite());
        Assert.assertFalse(segment.isFinite());

        Assert.assertEquals(-1.0, segment.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(segment.getSubspaceEnd());

        double side = 1.0 / Math.sqrt(3);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-side, -side, -side), segment.getStartPoint(), TEST_EPS);
        Assert.assertNull(segment.getEndPoint());

        Assert.assertSame(interval, segment.getInterval());
        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_intervalArg_negativeHalfSpace() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.max(2, intervalPrecision);

        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = Segment3D.fromInterval(line, interval);

        // assert
        GeometryTestUtils.assertNegativeInfinity(segment.getSubspaceStart());
        Assert.assertEquals(2, segment.getSubspaceEnd(), TEST_EPS);

        double side = 1.0 / Math.sqrt(3);

        Assert.assertNull(segment.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2 * side, 2 * side, 2 * side), segment.getEndPoint(), TEST_EPS);

        Assert.assertSame(interval, segment.getInterval());
        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_finite() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = Segment3D.fromInterval(line, -1, 2);

        // assert
        double side = 1.0 / Math.sqrt(3);
        checkFiniteSegment(segment, Vector3D.of(-side, -side, -side), Vector3D.of(2 * side, 2 * side, 2 * side));

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_full() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = Segment3D.fromInterval(line, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

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
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = Segment3D.fromInterval(line, -1, Double.POSITIVE_INFINITY);

        // assert
        Assert.assertEquals(-1.0, segment.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(segment.getSubspaceEnd());

        double side = 1.0 / Math.sqrt(3);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-side, -side, -side), segment.getStartPoint(), TEST_EPS);
        Assert.assertNull(segment.getEndPoint());

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_negativeHalfSpace() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = Segment3D.fromInterval(line, 2, Double.NEGATIVE_INFINITY);

        // assert
        GeometryTestUtils.assertNegativeInfinity(segment.getSubspaceStart());
        Assert.assertEquals(2, segment.getSubspaceEnd(), TEST_EPS);

        double side = 1.0 / Math.sqrt(3);

        Assert.assertNull(segment.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2 * side, 2 * side, 2 * side), segment.getEndPoint(), TEST_EPS);

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testFromInterval_vectorArgs() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = Segment3D.fromInterval(line, Vector1D.of(-1), Vector1D.of(2));

        // assert
        double side = 1.0 / Math.sqrt(3);
        checkFiniteSegment(segment, Vector3D.of(-side, -side, -side), Vector3D.of(2 * side, 2 * side, 2 * side));

        Assert.assertSame(TEST_PRECISION, segment.getPrecision());
    }

    @Test
    public void testGetSubspaceRegion() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);
        Interval interval = Interval.full();

        Segment3D segment = Segment3D.fromInterval(line, interval);

        // act/assert
        Assert.assertSame(interval, segment.getInterval());
        Assert.assertSame(interval, segment.getSubspaceRegion());
    }

    @Test
    public void testTransform_infinite() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.of(0, 1, -1), TEST_PRECISION);
        Segment3D segment = Segment3D.fromInterval(line,
                Interval.min(line.toSubspace(Vector3D.of(1, 0, 0)).getX(), TEST_PRECISION));

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .scale(2, 1, 1)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Geometry.HALF_PI));

        // act
        Segment3D transformed = segment.transform(transform);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -2), transformed.getStartPoint(), TEST_EPS);
        Assert.assertNull(transformed.getEndPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 1, 0).normalize(), transformed.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testTransform_finite() {
        // arrange
        Segment3D segment = Segment3D.fromPoints(Vector3D.of(1, 0, 0), Vector3D.of(2, 1, 0), TEST_PRECISION);

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .scale(2, 1, 1)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Geometry.HALF_PI));

        // act
        Segment3D transformed = segment.transform(transform);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -2), transformed.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -4), transformed.getEndPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -2).normalize(), transformed.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testContains() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 0, 0);
        Vector3D p2 = Vector3D.of(3, 0, 2);
        Segment3D segment = Segment3D.fromPoints(p1, p2, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(segment.contains(p1));
        Assert.assertTrue(segment.contains(p2));
        Assert.assertTrue(segment.contains(p1.lerp(p2, 0.5)));

        Assert.assertFalse(segment.contains(p1.lerp(p2, -1)));
        Assert.assertFalse(segment.contains(p1.lerp(p2, 2)));

        Assert.assertFalse(segment.contains(Vector3D.ZERO));
    }

    @Test
    public void testToString() {
        // arrange
        Line3D line = Line3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        Segment3D full = Segment3D.fromInterval(line, Interval.full());
        Segment3D startOnly = Segment3D.fromInterval(line, 0, Double.POSITIVE_INFINITY);
        Segment3D endOnly = Segment3D.fromInterval(line, Double.NEGATIVE_INFINITY, 0);
        Segment3D finite = Segment3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        String fullStr = full.toString();
        Assert.assertTrue(fullStr.contains("lineOrigin=") && fullStr.contains("lineDirection="));

        String startOnlyStr = startOnly.toString();
        Assert.assertTrue(startOnlyStr.contains("start=") && startOnlyStr.contains("direction="));

        String endOnlyStr = endOnly.toString();
        Assert.assertTrue(endOnlyStr.contains("direction=") && endOnlyStr.contains("end="));

        String finiteStr = finite.toString();
        Assert.assertTrue(finiteStr.contains("start=") && finiteStr.contains("end="));
    }

    private static void checkFiniteSegment(Segment3D segment, Vector3D start, Vector3D end) {
        checkFiniteSegment(segment, start, end, TEST_PRECISION);
    }

    private static void checkFiniteSegment(Segment3D segment, Vector3D start, Vector3D end, DoublePrecisionContext precision) {
        Assert.assertFalse(segment.isInfinite());
        Assert.assertTrue(segment.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);

        Line3D line = segment.getLine();

        Assert.assertEquals(line.toSubspace(segment.getStartPoint()).getX(), segment.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(line.toSubspace(segment.getEndPoint()).getX(), segment.getSubspaceEnd(), TEST_EPS);

        Assert.assertSame(precision, segment.getPrecision());
        Assert.assertSame(precision, line.getPrecision());
    }
}
