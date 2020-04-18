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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class ConvexSubLine3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromInterval_intervalArg_finite() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(-1, 2, intervalPrecision);

        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = (Segment3D) ConvexSubLine3D.fromInterval(line, interval);

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
        Line3D.Span span = (Line3D.Span) ConvexSubLine3D.fromInterval(line, Interval.full());

        // assert
        Assert.assertTrue(span.isInfinite());
        Assert.assertFalse(span.isFinite());

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

        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Ray3D ray = (Ray3D) ConvexSubLine3D.fromInterval(line, interval);

        // assert
        Assert.assertTrue(ray.isInfinite());
        Assert.assertFalse(ray.isFinite());

        Assert.assertEquals(-1.0, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        double side = 1.0 / Math.sqrt(3);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-side, -side, -side), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        checkInterval(interval, ray.getInterval());
        Assert.assertSame(TEST_PRECISION, ray.getPrecision());
    }

    @Test
    public void testFromInterval_intervalArg_negativeHalfSpace() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.max(2, intervalPrecision);

        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        TerminatedLine3D halfLine = (TerminatedLine3D) ConvexSubLine3D.fromInterval(line, interval);

        // assert
        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(2, halfLine.getSubspaceEnd(), TEST_EPS);

        double side = 1.0 / Math.sqrt(3);

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2 * side, 2 * side, 2 * side), halfLine.getEndPoint(), TEST_EPS);

        checkInterval(interval, halfLine.getInterval());
        Assert.assertSame(TEST_PRECISION, halfLine.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_finite() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = (Segment3D) ConvexSubLine3D.fromInterval(line, -1, 2);

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
        Line3D.Span span = (Line3D.Span) ConvexSubLine3D.fromInterval(line, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

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
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Ray3D ray = (Ray3D) ConvexSubLine3D.fromInterval(line, -1, Double.POSITIVE_INFINITY);

        // assert
        Assert.assertEquals(-1.0, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        double side = 1.0 / Math.sqrt(3);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-side, -side, -side), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        Assert.assertSame(TEST_PRECISION, ray.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_negativeHalfSpace() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        TerminatedLine3D halfLine = (TerminatedLine3D) ConvexSubLine3D.fromInterval(line, 2, Double.NEGATIVE_INFINITY);

        // assert
        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(2, halfLine.getSubspaceEnd(), TEST_EPS);

        double side = 1.0 / Math.sqrt(3);

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2 * side, 2 * side, 2 * side), halfLine.getEndPoint(), TEST_EPS);

        Assert.assertSame(TEST_PRECISION, halfLine.getPrecision());
    }

    @Test
    public void testFromInterval_doubleArgs_invalidArgs() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexSubLine3D.fromInterval(line, Double.NaN, 0);
        }, IllegalArgumentException.class, "Invalid convex subline interval: NaN, 0.0");

        GeometryTestUtils.assertThrows(() -> {
            ConvexSubLine3D.fromInterval(line, 0, Double.NaN);
        }, IllegalArgumentException.class, "Invalid convex subline interval: 0.0, NaN");

        GeometryTestUtils.assertThrows(() -> {
            ConvexSubLine3D.fromInterval(line, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid convex subline interval: Infinity, Infinity");

        GeometryTestUtils.assertThrows(() -> {
            ConvexSubLine3D.fromInterval(line, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid convex subline interval: -Infinity, -Infinity");
    }

    @Test
    public void testFromInterval_vectorArgs() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = (Segment3D) ConvexSubLine3D.fromInterval(line, Vector1D.of(-1), Vector1D.of(2));

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

        ConvexSubLine3D subline = ConvexSubLine3D.fromInterval(line, interval);

        // act/assert
        Assert.assertSame(interval, subline.getInterval());
        Assert.assertSame(interval, subline.getSubspaceRegion());
    }

    @Test
    public void testTransform_infinite() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.of(0, 1, -1), TEST_PRECISION);
        ConvexSubLine3D subline = ConvexSubLine3D.fromInterval(line,
                Interval.min(line.toSubspace(Vector3D.of(1, 0, 0)).getX(), TEST_PRECISION));

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .scale(2, 1, 1)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO));

        // act
        ConvexSubLine3D transformed = subline.transform(transform);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -2), transformed.getStartPoint(), TEST_EPS);
        Assert.assertNull(transformed.getEndPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 1, 0).normalize(), transformed.getLine().getDirection(), TEST_EPS);
    }

    private static void checkInterval(Interval expected, Interval actual) {
        Assert.assertEquals(expected.getMin(), actual.getMin(), TEST_EPS);
        Assert.assertEquals(expected.getMax(), actual.getMax(), TEST_EPS);
    }

    private static void checkFiniteSegment(ConvexSubLine3D subline, Vector3D start, Vector3D end) {
        checkFiniteSegment(subline, start, end, TEST_PRECISION);
    }

    private static void checkFiniteSegment(ConvexSubLine3D subline, Vector3D start, Vector3D end, DoublePrecisionContext precision) {
        Assert.assertFalse(subline.isInfinite());
        Assert.assertTrue(subline.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, subline.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, subline.getEndPoint(), TEST_EPS);

        Line3D line = subline.getLine();

        Assert.assertEquals(line.toSubspace(subline.getStartPoint()).getX(), subline.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(line.toSubspace(subline.getEndPoint()).getX(), subline.getSubspaceEnd(), TEST_EPS);

        Assert.assertSame(precision, subline.getPrecision());
        Assert.assertSame(precision, line.getPrecision());
    }
}
