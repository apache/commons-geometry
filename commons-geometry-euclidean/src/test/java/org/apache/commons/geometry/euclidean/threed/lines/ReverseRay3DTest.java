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

public class ReverseRay3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPointAndDirection() {
        // arrange
        Vector3D pt = Vector3D.of(1, 1, 2);

        // act
        ReverseRay3D revRay = Lines3D.reverseRayFromPointAndDirection(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // assert
        Assert.assertTrue(revRay.isInfinite());
        Assert.assertFalse(revRay.isFinite());

        Assert.assertNull(revRay.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(pt, revRay.getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, revRay.getLine().getDirection(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(revRay.getSubspaceStart());
        Assert.assertEquals(2, revRay.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(revRay.getSize());
    }

    @Test
    public void testFromPointAndDirection_invalidArgs() {
        // arrange
        Vector3D pt = Vector3D.of(0, 2, 4);
        Vector3D dir = Vector3D.of(1e-11, 0, 0);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines3D.reverseRayFromPointAndDirection(pt, dir, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPoint() {
        // arrange
        Vector3D pt = Vector3D.of(-2, -1, 2);

        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 2), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        ReverseRay3D revRay = Lines3D.reverseRayFromPoint(line, pt);

        // assert
        Assert.assertTrue(revRay.isInfinite());
        Assert.assertFalse(revRay.isFinite());

        Assert.assertNull(revRay.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, 2), revRay.getEndPoint(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(revRay.getSubspaceStart());
        Assert.assertEquals(-1, revRay.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(revRay.getSize());
    }

    @Test
    public void testFromPoint_invalidArgs() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines3D.reverseRayFromPoint(line, Vector3D.NaN);
        }, IllegalArgumentException.class, "Invalid reverse ray end location: NaN");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.reverseRayFromPoint(line, Vector3D.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid reverse ray end location: NaN");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.reverseRayFromPoint(line, Vector3D.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid reverse ray end location: NaN");
    }

    @Test
    public void testFromLocation() {
        // arrange
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(-1, 0, 0), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        ReverseRay3D revRay = Lines3D.reverseRayFromLocation(line, -1);

        // assert
        Assert.assertTrue(revRay.isInfinite());
        Assert.assertFalse(revRay.isFinite());

        Assert.assertNull(revRay.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, -1), revRay.getEndPoint(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(revRay.getSubspaceStart());
        Assert.assertEquals(-1, revRay.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(revRay.getSize());
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y);

        ReverseRay3D revRay = Lines3D.reverseRayFromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        ReverseRay3D result = revRay.transform(t);

        // assert
        Assert.assertNull(result.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -1), result.getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, result.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y)
                .scale(1, 1, -2);

        ReverseRay3D revRay = Lines3D.reverseRayFromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        ReverseRay3D result = revRay.transform(t);

        // assert
        Assert.assertNull(result.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 2), result.getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, result.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testContains() {
        // arrange
        Vector3D p0 = Vector3D.of(1, 1, 1);

        Vector3D delta = Vector3D.of(1e-12, 1e-12, 1e-12);

        ReverseRay3D revRay = Lines3D.reverseRayFromPointAndDirection(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        Assert.assertFalse(revRay.contains(Vector3D.of(2, 2, 2)));
        Assert.assertFalse(revRay.contains(Vector3D.of(1.1, 1, 1)));
        Assert.assertFalse(revRay.contains(Vector3D.of(100, 1, 1)));

        Assert.assertTrue(revRay.contains(p0));
        Assert.assertTrue(revRay.contains(p0.add(delta)));

        Assert.assertTrue(revRay.contains(Vector3D.of(-1000, 1, 1)));
    }

    @Test
    public void testGetInterval() {
        // arrange
        ReverseRay3D revRay = Lines3D.reverseRayFromPointAndDirection(Vector3D.of(2, -1, 3), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        Interval interval = revRay.getInterval();

        // assert
        GeometryTestUtils.assertNegativeInfinity(interval.getMin());
        Assert.assertEquals(-1, interval.getMax(), TEST_EPS);

        Assert.assertSame(revRay.getLine().getPrecision(), interval.getMaxBoundary().getPrecision());
    }

    @Test
    public void testToString() {
        // arrange
        ReverseRay3D revRay = Lines3D.reverseRayFromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        String str = revRay.toString();

        // assert
        GeometryTestUtils.assertContains("ReverseRay3D[direction= (1", str);
        GeometryTestUtils.assertContains(", endPoint= (0", str);
    }
}
