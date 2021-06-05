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
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Segment3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    public void testFromPoints() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 1, 2);
        final Vector3D p2 = Vector3D.of(1, 3, 2);

        // act
        final Segment3D seg = Lines3D.segmentFromPoints(p1, p2, TEST_PRECISION);

        // assert
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(p1, seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, seg.getEndPoint(), TEST_EPS);

        Assertions.assertEquals(1, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(3, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(2, seg.getSize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 2), seg.getCentroid(), TEST_EPS);
        final Bounds3D bounds = seg.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 2), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 3, 2), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testFromPoints_invalidArgs() {
        // arrange
        final Vector3D p1 = Vector3D.of(0, 2, 4);
        final Vector3D p2 = Vector3D.of(1e-17, 2, 4);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.segmentFromPoints(p1, p1, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.segmentFromPoints(p1, p2, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPoints_givenLine() {
        // arrange
        final Vector3D p1 = Vector3D.of(-1, -1, 2);
        final Vector3D p2 = Vector3D.of(3, 3, 3);

        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 2), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final Segment3D seg = Lines3D.segmentFromPoints(line, p2, p1); // reverse location order

        // assert
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, 2), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 3, 2), seg.getEndPoint(), TEST_EPS);

        Assertions.assertEquals(-1, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(3, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(4, seg.getSize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 2), seg.getCentroid(), TEST_EPS);
        final Bounds3D bounds = seg.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, 2), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 3, 2), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testFromPoints_givenLine_singlePoint() {
        // arrange
        final Vector3D p1 = Vector3D.of(-1, 2, 0);

        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final Segment3D seg = Lines3D.segmentFromPoints(line, p1, p1);

        // assert
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 0), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 0), seg.getEndPoint(), TEST_EPS);

        Assertions.assertEquals(2, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(2, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(0, seg.getSize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 0), seg.getCentroid(), TEST_EPS);
        final Bounds3D bounds = seg.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 0), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 0), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testFromPoints_givenLine_invalidArgs() {
        // arrange
        final Vector3D p0 = Vector3D.of(1, 0, 0);
        final Vector3D p1 = Vector3D.of(2, 0, 0);

        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.segmentFromPoints(line, Vector3D.NaN, p1);
        }, IllegalArgumentException.class, "Invalid line segment locations: NaN, 2.0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.segmentFromPoints(line, p0, Vector3D.NaN);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, NaN");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.segmentFromPoints(line, Vector3D.NEGATIVE_INFINITY, p1);
        }, IllegalArgumentException.class, "Invalid line segment locations: NaN, 2.0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.segmentFromPoints(line, p0, Vector3D.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, NaN");
    }

    @Test
    public void testFromLocations() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(-1, 0, 0), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Segment3D seg = Lines3D.segmentFromLocations(line, -1, 2);

        // assert
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, -1), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 2), seg.getEndPoint(), TEST_EPS);

        Assertions.assertEquals(-1, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(2, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(3, seg.getSize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0.5), seg.getCentroid(), TEST_EPS);
        final Bounds3D bounds = seg.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, -1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 2), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testFromLocations_reversedLocationOrder() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(-1, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Segment3D seg = Lines3D.segmentFromLocations(line, 2, -1);

        // assert
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, -1), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 2), seg.getEndPoint(), TEST_EPS);

        Assertions.assertEquals(-1, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(2, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(3, seg.getSize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0.5), seg.getCentroid(), TEST_EPS);
        final Bounds3D bounds = seg.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, -1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 2), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testFromLocations_singlePoint() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(-1, 0, 0), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Segment3D seg = Lines3D.segmentFromLocations(line, 1, 1);

        // assert
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 1), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 1), seg.getEndPoint(), TEST_EPS);

        Assertions.assertEquals(1, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(1, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(0, seg.getSize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 1), seg.getCentroid(), TEST_EPS);
        final Bounds3D bounds = seg.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testFromLocations_invalidArgs() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.segmentFromLocations(line, Double.NaN, 2);
        }, IllegalArgumentException.class, "Invalid line segment locations: NaN, 2.0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.segmentFromLocations(line, 1, Double.NaN);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, NaN");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.segmentFromLocations(line, Double.NEGATIVE_INFINITY, 2);
        }, IllegalArgumentException.class, "Invalid line segment locations: -Infinity, 2.0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines3D.segmentFromLocations(line, 1, Double.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, Infinity");
    }

    @Test
    public void testTransform() {
        // arrange
        final AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y);

        final Segment3D seg = Lines3D.segmentFromPoints(Vector3D.of(1, 0, 0), Vector3D.of(2, 0, 0), TEST_PRECISION);

        // act
        final Segment3D result = seg.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -1), result.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -2), result.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        final AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y)
                .scale(1, 1, -2);

        final Segment3D seg = Lines3D.segmentFromPoints(Vector3D.of(1, 0, 0), Vector3D.of(2, 0, 0), TEST_PRECISION);

        // act
        final Segment3D result = seg.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 2), result.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 4), result.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testContains() {
        // arrange
        final Vector3D p0 = Vector3D.of(1, 1, 1);
        final Vector3D p1 = Vector3D.of(3, 1, 1);

        final Vector3D delta = Vector3D.of(1e-12, 1e-12, 1e-12);

        final Segment3D seg = Lines3D.segmentFromPoints(Vector3D.of(1, 1, 1), Vector3D.of(3, 1, 1), TEST_PRECISION);

        // act/assert
        Assertions.assertFalse(seg.contains(Vector3D.of(2, 2, 2)));
        Assertions.assertFalse(seg.contains(Vector3D.of(0.9, 1, 1)));
        Assertions.assertFalse(seg.contains(Vector3D.of(3.1, 1, 1)));

        Assertions.assertTrue(seg.contains(p0));
        Assertions.assertTrue(seg.contains(p1));

        Assertions.assertTrue(seg.contains(p0.subtract(delta)));
        Assertions.assertTrue(seg.contains(p1.add(delta)));

        Assertions.assertTrue(seg.contains(p0.lerp(p1, 0.5)));
    }

    @Test
    public void testGetInterval() {
        // arrange
        final Segment3D seg = Lines3D.segmentFromPoints(Vector3D.of(2, -1, 3), Vector3D.of(2, 2, 3), TEST_PRECISION);

        // act
        final Interval interval = seg.getInterval();

        // assert
        Assertions.assertEquals(-1, interval.getMin(), TEST_EPS);
        Assertions.assertEquals(2, interval.getMax(), TEST_EPS);

        Assertions.assertSame(seg.getLine().getPrecision(), interval.getMinBoundary().getPrecision());
    }

    @Test
    public void testGetInterval_singlePoint() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final Segment3D seg = Lines3D.segmentFromLocations(line, 1, 1);

        // act
        final Interval interval = seg.getInterval();

        // assert
        Assertions.assertEquals(1, interval.getMin(), TEST_EPS);
        Assertions.assertEquals(1, interval.getMax(), TEST_EPS);
        Assertions.assertEquals(0, interval.getSize(), TEST_EPS);

        Assertions.assertSame(seg.getLine().getPrecision(), interval.getMinBoundary().getPrecision());
    }

    @Test
    public void testToString() {
        // arrange
        final Segment3D seg = Lines3D.segmentFromPoints(Vector3D.ZERO, Vector3D.of(1, 0, 0), TEST_PRECISION);

        // act
        final String str = seg.toString();

        // assert
        GeometryTestUtils.assertContains("Segment3D[startPoint= (0", str);
        GeometryTestUtils.assertContains(", endPoint= (1", str);
    }
}
