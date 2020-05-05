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
package org.apache.commons.geometry.euclidean.threed.line;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class LineConvexSubset3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromInterval_intervalArg_finite() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(-1, 2, intervalPrecision);

        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = (Segment3D) Lines3D.subsetFromInterval(line, interval);

        // assert
        double side = 1.0 / Math.sqrt(3);
        checkFiniteSegment(segment, Vector3D.of(-side, -side, -side), Vector3D.of(2 * side, 2 * side, 2 * side));
    }

    @Test
    public void testFromInterval_intervalArg_full() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        LineConvexSubset3D span = Lines3D.subsetFromInterval(line, Interval.full());

        // assert
        Assert.assertTrue(span.isInfinite());
        Assert.assertFalse(span.isFinite());

        GeometryTestUtils.assertNegativeInfinity(span.getSubspaceStart());
        GeometryTestUtils.assertPositiveInfinity(span.getSubspaceEnd());

        Assert.assertNull(span.getStartPoint());
        Assert.assertNull(span.getEndPoint());

        Assert.assertSame(Interval.full(), span.getInterval());
    }

    @Test
    public void testFromInterval_intervalArg_positiveHalfSpace() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.min(-1, intervalPrecision);

        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Ray3D ray = (Ray3D) Lines3D.subsetFromInterval(line, interval);

        // assert
        Assert.assertTrue(ray.isInfinite());
        Assert.assertFalse(ray.isFinite());

        Assert.assertEquals(-1.0, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        double side = 1.0 / Math.sqrt(3);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-side, -side, -side), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        checkInterval(interval, ray.getInterval());
    }

    @Test
    public void testFromInterval_intervalArg_negativeHalfSpace() {
        // arrange
        DoublePrecisionContext intervalPrecision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.max(2, intervalPrecision);

        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        ReverseRay3D halfLine = (ReverseRay3D) Lines3D.subsetFromInterval(line, interval);

        // assert
        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(2, halfLine.getSubspaceEnd(), TEST_EPS);

        double side = 1.0 / Math.sqrt(3);

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2 * side, 2 * side, 2 * side), halfLine.getEndPoint(), TEST_EPS);

        checkInterval(interval, halfLine.getInterval());
    }

    @Test
    public void testFromInterval_doubleArgs_finite() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = (Segment3D) Lines3D.subsetFromInterval(line, -1, 2);

        // assert
        double side = 1.0 / Math.sqrt(3);
        checkFiniteSegment(segment, Vector3D.of(-side, -side, -side), Vector3D.of(2 * side, 2 * side, 2 * side));
    }

    @Test
    public void testFromInterval_doubleArgs_full() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        LineConvexSubset3D span = Lines3D.subsetFromInterval(line, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        // assert
        GeometryTestUtils.assertNegativeInfinity(span.getSubspaceStart());
        GeometryTestUtils.assertPositiveInfinity(span.getSubspaceEnd());

        Assert.assertNull(span.getStartPoint());
        Assert.assertNull(span.getEndPoint());
    }

    @Test
    public void testFromInterval_doubleArgs_positiveHalfSpace() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Ray3D ray = (Ray3D) Lines3D.subsetFromInterval(line, -1, Double.POSITIVE_INFINITY);

        // assert
        Assert.assertEquals(-1.0, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        double side = 1.0 / Math.sqrt(3);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-side, -side, -side), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());
    }

    @Test
    public void testFromInterval_doubleArgs_negativeHalfSpace() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        ReverseRay3D halfLine = (ReverseRay3D) Lines3D.subsetFromInterval(line, 2, Double.NEGATIVE_INFINITY);

        // assert
        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(2, halfLine.getSubspaceEnd(), TEST_EPS);

        double side = 1.0 / Math.sqrt(3);

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2 * side, 2 * side, 2 * side), halfLine.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testFromInterval_doubleArgs_invalidArgs() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines3D.subsetFromInterval(line, Double.NaN, 0);
        }, IllegalArgumentException.class, "Invalid line convex subset interval: NaN, 0.0");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.subsetFromInterval(line, 0, Double.NaN);
        }, IllegalArgumentException.class, "Invalid line convex subset interval: 0.0, NaN");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.subsetFromInterval(line, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid line convex subset interval: Infinity, Infinity");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.subsetFromInterval(line, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid line convex subset interval: -Infinity, -Infinity");
    }

    @Test
    public void testFromInterval_vectorArgs() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        Segment3D segment = (Segment3D) Lines3D.subsetFromInterval(line, Vector1D.of(-1), Vector1D.of(2));

        // assert
        double side = 1.0 / Math.sqrt(3);
        checkFiniteSegment(segment, Vector3D.of(-side, -side, -side), Vector3D.of(2 * side, 2 * side, 2 * side));
    }

    @Test
    public void testSpaceSubspaceConversion() {
        // arrange
        Segment3D segment = Lines3D.segmentFromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(3), segment.toSubspace(Vector3D.of(1, 3, 5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 3, 0), segment.toSpace(Vector1D.of(3)), TEST_EPS);
    }

    @Test
    public void testGetSubspaceRegion() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);
        Interval interval = Interval.full();

        LineConvexSubset3D subset = Lines3D.subsetFromInterval(line, interval);

        // act/assert
        Assert.assertSame(interval, subset.getInterval());
        Assert.assertSame(interval, subset.getSubspaceRegion());
    }

    @Test
    public void testTransform_infinite() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.of(0, 1, -1), TEST_PRECISION);
        LineConvexSubset3D subset = Lines3D.subsetFromInterval(line,
                Interval.min(line.toSubspace(Vector3D.of(1, 0, 0)).getX(), TEST_PRECISION));

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .scale(2, 1, 1)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO));

        // act
        LineConvexSubset3D transformed = subset.transform(transform);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -2), transformed.getStartPoint(), TEST_EPS);
        Assert.assertNull(transformed.getEndPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 1, 0).normalize(), transformed.getLine().getDirection(), TEST_EPS);
    }

    private static void checkInterval(Interval expected, Interval actual) {
        Assert.assertEquals(expected.getMin(), actual.getMin(), TEST_EPS);
        Assert.assertEquals(expected.getMax(), actual.getMax(), TEST_EPS);
    }

    private static void checkFiniteSegment(LineConvexSubset3D subset, Vector3D start, Vector3D end) {
        checkFiniteSegment(subset, start, end, TEST_PRECISION);
    }

    private static void checkFiniteSegment(LineConvexSubset3D subset, Vector3D start, Vector3D end, DoublePrecisionContext precision) {
        Assert.assertFalse(subset.isInfinite());
        Assert.assertTrue(subset.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, subset.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, subset.getEndPoint(), TEST_EPS);

        Line3D line = subset.getLine();

        Assert.assertEquals(line.toSubspace(subset.getStartPoint()).getX(), subset.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(line.toSubspace(subset.getEndPoint()).getX(), subset.getSubspaceEnd(), TEST_EPS);

        Assert.assertSame(precision, line.getPrecision());
    }
}
