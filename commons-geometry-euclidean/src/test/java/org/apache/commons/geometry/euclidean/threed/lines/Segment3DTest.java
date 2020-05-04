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
package org.apache.commons.geometry.euclidean.threed.lines;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
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
        Vector3D p1 = Vector3D.of(1, 1, 2);
        Vector3D p2 = Vector3D.of(1, 3, 2);

        // act
        Segment3D seg = Lines3D.segmentFromPoints(p1, p2, TEST_PRECISION);

        // assert
        Assert.assertFalse(seg.isInfinite());
        Assert.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(p1, seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, seg.getEndPoint(), TEST_EPS);

        Assert.assertEquals(1, seg.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(3, seg.getSubspaceEnd(), TEST_EPS);

        Assert.assertEquals(2, seg.getSize(), TEST_EPS);
    }

    @Test
    public void testFromPoints_invalidArgs() {
        // arrange
        Vector3D p1 = Vector3D.of(0, 2, 4);
        Vector3D p2 = Vector3D.of(1e-17, 2, 4);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines3D.segmentFromPoints(p1, p1, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.segmentFromPoints(p1, p2, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPoints_givenLine() {
        // arrange
        Vector3D p1 = Vector3D.of(-1, -1, 2);
        Vector3D p2 = Vector3D.of(3, 3, 3);

        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 2), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        Segment3D seg = Lines3D.segmentFromPoints(line, p2, p1); // reverse location order

        // assert
        Assert.assertFalse(seg.isInfinite());
        Assert.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, 2), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 3, 2), seg.getEndPoint(), TEST_EPS);

        Assert.assertEquals(-1, seg.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(3, seg.getSubspaceEnd(), TEST_EPS);

        Assert.assertEquals(4, seg.getSize(), TEST_EPS);
    }

    @Test
    public void testFromPoints_givenLine_singlePoint() {
        // arrange
        Vector3D p1 = Vector3D.of(-1, 2, 0);

        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        Segment3D seg = Lines3D.segmentFromPoints(line, p1, p1);

        // assert
        Assert.assertFalse(seg.isInfinite());
        Assert.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 0), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 0), seg.getEndPoint(), TEST_EPS);

        Assert.assertEquals(2, seg.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(2, seg.getSubspaceEnd(), TEST_EPS);

        Assert.assertEquals(0, seg.getSize(), TEST_EPS);
    }

    @Test
    public void testFromPoints_givenLine_invalidArgs() {
        // arrange
        Vector3D p0 = Vector3D.of(1, 0, 0);
        Vector3D p1 = Vector3D.of(2, 0, 0);

        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines3D.segmentFromPoints(line, Vector3D.NaN, p1);
        }, IllegalArgumentException.class, "Invalid line segment locations: NaN, 2.0");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.segmentFromPoints(line, p0, Vector3D.NaN);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, NaN");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.segmentFromPoints(line, Vector3D.NEGATIVE_INFINITY, p1);
        }, IllegalArgumentException.class, "Invalid line segment locations: NaN, 2.0");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.segmentFromPoints(line, p0, Vector3D.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, NaN");
    }

    @Test
    public void testFromLocations() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(-1, 0, 0), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Segment3D seg = Lines3D.segmentFromLocations(line, -1, 2);

        // assert
        Assert.assertFalse(seg.isInfinite());
        Assert.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, -1), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 2), seg.getEndPoint(), TEST_EPS);

        Assert.assertEquals(-1, seg.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(2, seg.getSubspaceEnd(), TEST_EPS);

        Assert.assertEquals(3, seg.getSize(), TEST_EPS);
    }

    @Test
    public void testFromLocations_reversedLocationOrder() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(-1, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Segment3D seg = Lines3D.segmentFromLocations(line, 2, -1);

        // assert
        Assert.assertFalse(seg.isInfinite());
        Assert.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, -1), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 2), seg.getEndPoint(), TEST_EPS);

        Assert.assertEquals(-1, seg.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(2, seg.getSubspaceEnd(), TEST_EPS);

        Assert.assertEquals(3, seg.getSize(), TEST_EPS);
    }

    @Test
    public void testFromLocations_singlePoint() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(-1, 0, 0), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Segment3D seg = Lines3D.segmentFromLocations(line, 1, 1);

        // assert
        Assert.assertFalse(seg.isInfinite());
        Assert.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 1), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 1), seg.getEndPoint(), TEST_EPS);

        Assert.assertEquals(1, seg.getSubspaceStart(), TEST_EPS);
        Assert.assertEquals(1, seg.getSubspaceEnd(), TEST_EPS);

        Assert.assertEquals(0, seg.getSize(), TEST_EPS);
    }

    @Test
    public void testFromLocations_invalidArgs() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines3D.segmentFromLocations(line, Double.NaN, 2);
        }, IllegalArgumentException.class, "Invalid line segment locations: NaN, 2.0");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.segmentFromLocations(line, 1, Double.NaN);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, NaN");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.segmentFromLocations(line, Double.NEGATIVE_INFINITY, 2);
        }, IllegalArgumentException.class, "Invalid line segment locations: -Infinity, 2.0");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.segmentFromLocations(line, 1, Double.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, Infinity");
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y);

        Segment3D seg = Lines3D.segmentFromPoints(Vector3D.of(1, 0, 0), Vector3D.of(2, 0, 0), TEST_PRECISION);

        // act
        Segment3D result = seg.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -1), result.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -2), result.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y)
                .scale(1, 1, -2);

        Segment3D seg = Lines3D.segmentFromPoints(Vector3D.of(1, 0, 0), Vector3D.of(2, 0, 0), TEST_PRECISION);

        // act
        Segment3D result = seg.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 2), result.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 4), result.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testContains() {
        // arrange
        Vector3D p0 = Vector3D.of(1, 1, 1);
        Vector3D p1 = Vector3D.of(3, 1, 1);

        Vector3D delta = Vector3D.of(1e-12, 1e-12, 1e-12);

        Segment3D seg = Lines3D.segmentFromPoints(Vector3D.of(1, 1, 1), Vector3D.of(3, 1, 1), TEST_PRECISION);

        // act/assert
        Assert.assertFalse(seg.contains(Vector3D.of(2, 2, 2)));
        Assert.assertFalse(seg.contains(Vector3D.of(0.9, 1, 1)));
        Assert.assertFalse(seg.contains(Vector3D.of(3.1, 1, 1)));

        Assert.assertTrue(seg.contains(p0));
        Assert.assertTrue(seg.contains(p1));

        Assert.assertTrue(seg.contains(p0.subtract(delta)));
        Assert.assertTrue(seg.contains(p1.add(delta)));

        Assert.assertTrue(seg.contains(p0.lerp(p1, 0.5)));
    }

    @Test
    public void testGetInterval() {
        // arrange
        Segment3D seg = Lines3D.segmentFromPoints(Vector3D.of(2, -1, 3), Vector3D.of(2, 2, 3), TEST_PRECISION);

        // act
        Interval interval = seg.getInterval();

        // assert
        Assert.assertEquals(-1, interval.getMin(), TEST_EPS);
        Assert.assertEquals(2, interval.getMax(), TEST_EPS);

        Assert.assertSame(seg.getLine().getPrecision(), interval.getMinBoundary().getPrecision());
    }

    @Test
    public void testGetInterval_singlePoint() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        Segment3D seg = Lines3D.segmentFromLocations(line, 1, 1);

        // act
        Interval interval = seg.getInterval();

        // assert
        Assert.assertEquals(1, interval.getMin(), TEST_EPS);
        Assert.assertEquals(1, interval.getMax(), TEST_EPS);
        Assert.assertEquals(0, interval.getSize(), TEST_EPS);

        Assert.assertSame(seg.getLine().getPrecision(), interval.getMinBoundary().getPrecision());
    }

    @Test
    public void testToString() {
        // arrange
        Segment3D seg = Lines3D.segmentFromPoints(Vector3D.ZERO, Vector3D.of(1, 0, 0), TEST_PRECISION);

        // act
        String str = seg.toString();

        // assert
        GeometryTestUtils.assertContains("Segment3D[startPoint= (0", str);
        GeometryTestUtils.assertContains(", endPoint= (1", str);
    }
}
