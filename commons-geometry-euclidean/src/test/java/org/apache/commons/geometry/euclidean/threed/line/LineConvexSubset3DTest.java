/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LineConvexSubset3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testFromInterval_intervalArg_finite() {
        // arrange
        final Precision.DoubleEquivalence intervalPrecision = Precision.doubleEquivalenceOfEpsilon(1e-2);
        final Interval interval = Interval.of(-1, 2, intervalPrecision);

        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        final Segment3D segment = (Segment3D) Lines3D.subsetFromInterval(line, interval);

        // assert
        final double side = 1.0 / Math.sqrt(3);
        checkFiniteSegment(segment, Vector3D.of(-side, -side, -side), Vector3D.of(2 * side, 2 * side, 2 * side));
    }

    @Test
    void testFromInterval_intervalArg_full() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        final LineConvexSubset3D span = Lines3D.subsetFromInterval(line, Interval.full());

        // assert
        Assertions.assertTrue(span.isInfinite());
        Assertions.assertFalse(span.isFinite());

        GeometryTestUtils.assertNegativeInfinity(span.getSubspaceStart());
        GeometryTestUtils.assertPositiveInfinity(span.getSubspaceEnd());

        Assertions.assertNull(span.getStartPoint());
        Assertions.assertNull(span.getEndPoint());

        Assertions.assertSame(Interval.full(), span.getInterval());
    }

    @Test
    void testFromInterval_intervalArg_positiveHalfSpace() {
        // arrange
        final Precision.DoubleEquivalence intervalPrecision = Precision.doubleEquivalenceOfEpsilon(1e-2);
        final Interval interval = Interval.min(-1, intervalPrecision);

        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        final Ray3D ray = (Ray3D) Lines3D.subsetFromInterval(line, interval);

        // assert
        Assertions.assertTrue(ray.isInfinite());
        Assertions.assertFalse(ray.isFinite());

        Assertions.assertEquals(-1.0, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        final double side = 1.0 / Math.sqrt(3);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-side, -side, -side), ray.getStartPoint(), TEST_EPS);
        Assertions.assertNull(ray.getEndPoint());

        checkInterval(interval, ray.getInterval());
    }

    @Test
    void testFromInterval_intervalArg_negativeHalfSpace() {
        // arrange
        final Precision.DoubleEquivalence intervalPrecision = Precision.doubleEquivalenceOfEpsilon(1e-2);
        final Interval interval = Interval.max(2, intervalPrecision);

        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        final ReverseRay3D halfLine = (ReverseRay3D) Lines3D.subsetFromInterval(line, interval);

        // assert
        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assertions.assertEquals(2, halfLine.getSubspaceEnd(), TEST_EPS);

        final double side = 1.0 / Math.sqrt(3);

        Assertions.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2 * side, 2 * side, 2 * side), halfLine.getEndPoint(), TEST_EPS);

        checkInterval(interval, halfLine.getInterval());
    }

    @Test
    void testFromInterval_doubleArgs_finite() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        final Segment3D segment = (Segment3D) Lines3D.subsetFromInterval(line, -1, 2);

        // assert
        final double side = 1.0 / Math.sqrt(3);
        checkFiniteSegment(segment, Vector3D.of(-side, -side, -side), Vector3D.of(2 * side, 2 * side, 2 * side));
    }

    @Test
    void testFromInterval_doubleArgs_full() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        final LineConvexSubset3D span = Lines3D.subsetFromInterval(line, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        // assert
        GeometryTestUtils.assertNegativeInfinity(span.getSubspaceStart());
        GeometryTestUtils.assertPositiveInfinity(span.getSubspaceEnd());

        Assertions.assertNull(span.getStartPoint());
        Assertions.assertNull(span.getEndPoint());
    }

    @Test
    void testFromInterval_doubleArgs_positiveHalfSpace() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        final Ray3D ray = (Ray3D) Lines3D.subsetFromInterval(line, -1, Double.POSITIVE_INFINITY);

        // assert
        Assertions.assertEquals(-1.0, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        final double side = 1.0 / Math.sqrt(3);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-side, -side, -side), ray.getStartPoint(), TEST_EPS);
        Assertions.assertNull(ray.getEndPoint());
    }

    @Test
    void testFromInterval_doubleArgs_negativeHalfSpace() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        final ReverseRay3D halfLine = (ReverseRay3D) Lines3D.subsetFromInterval(line, 2, Double.NEGATIVE_INFINITY);

        // assert
        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assertions.assertEquals(2, halfLine.getSubspaceEnd(), TEST_EPS);

        final double side = 1.0 / Math.sqrt(3);

        Assertions.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2 * side, 2 * side, 2 * side), halfLine.getEndPoint(), TEST_EPS);
    }

    @Test
    void testFromInterval_doubleArgs_invalidArgs() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.subsetFromInterval(line, Double.NaN, 0);
        }, IllegalArgumentException.class, "Invalid line convex subset interval: NaN, 0.0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.subsetFromInterval(line, 0, Double.NaN);
        }, IllegalArgumentException.class, "Invalid line convex subset interval: 0.0, NaN");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.subsetFromInterval(line, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid line convex subset interval: Infinity, Infinity");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.subsetFromInterval(line, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid line convex subset interval: -Infinity, -Infinity");
    }

    @Test
    void testFromInterval_vectorArgs() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        final Segment3D segment = (Segment3D) Lines3D.subsetFromInterval(line, Vector1D.of(-1), Vector1D.of(2));

        // assert
        final double side = 1.0 / Math.sqrt(3);
        checkFiniteSegment(segment, Vector3D.of(-side, -side, -side), Vector3D.of(2 * side, 2 * side, 2 * side));
    }

    @Test
    void testSpaceSubspaceConversion() {
        // arrange
        final Segment3D segment = Lines3D.segmentFromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(3), segment.toSubspace(Vector3D.of(1, 3, 5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 3, 0), segment.toSpace(Vector1D.of(3)), TEST_EPS);
    }

    @Test
    void testGetSubspaceRegion() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);
        final Interval interval = Interval.full();

        final LineConvexSubset3D subset = Lines3D.subsetFromInterval(line, interval);

        // act/assert
        Assertions.assertSame(interval, subset.getInterval());
        Assertions.assertSame(interval, subset.getSubspaceRegion());
    }

    @Test
    void testTransform_infinite() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.of(0, 1, -1), TEST_PRECISION);
        final LineConvexSubset3D subset = Lines3D.subsetFromInterval(line,
                Interval.min(line.toSubspace(Vector3D.of(1, 0, 0)).getX(), TEST_PRECISION));

        final Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .scale(2, 1, 1)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Angle.PI_OVER_TWO));

        // act
        final LineConvexSubset3D transformed = subset.transform(transform);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -2), transformed.getStartPoint(), TEST_EPS);
        Assertions.assertNull(transformed.getEndPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 1, 0).normalize(), transformed.getLine().getDirection(), TEST_EPS);
    }

    private static void checkInterval(final Interval expected, final Interval actual) {
        Assertions.assertEquals(expected.getMin(), actual.getMin(), TEST_EPS);
        Assertions.assertEquals(expected.getMax(), actual.getMax(), TEST_EPS);
    }

    private static void checkFiniteSegment(final LineConvexSubset3D subset, final Vector3D start, final Vector3D end) {
        checkFiniteSegment(subset, start, end, TEST_PRECISION);
    }

    private static void checkFiniteSegment(final LineConvexSubset3D subset, final Vector3D start, final Vector3D end, final Precision.DoubleEquivalence precision) {
        Assertions.assertFalse(subset.isInfinite());
        Assertions.assertTrue(subset.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, subset.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, subset.getEndPoint(), TEST_EPS);

        final Line3D line = subset.getLine();

        Assertions.assertEquals(line.toSubspace(subset.getStartPoint()).getX(), subset.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(line.toSubspace(subset.getEndPoint()).getX(), subset.getSubspaceEnd(), TEST_EPS);

        Assertions.assertSame(precision, line.getPrecision());
    }
}
