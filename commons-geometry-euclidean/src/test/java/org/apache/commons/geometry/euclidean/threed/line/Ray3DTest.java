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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.junit.Assert;
import org.junit.Test;

public class Ray3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPointAndDirection() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 1, 2);

        // act
        final Ray3D ray = Lines3D.rayFromPointAndDirection(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // assert
        Assert.assertTrue(ray.isInfinite());
        Assert.assertFalse(ray.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(pt, ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, ray.getDirection(), TEST_EPS);

        Assert.assertEquals(2, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        GeometryTestUtils.assertPositiveInfinity(ray.getSize());

        Assert.assertNull(ray.getCentroid());
        Assert.assertNull(ray.getBounds());
    }

    @Test
    public void testFromPointAndDirection_invalidArgs() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 2, 4);
        final Vector3D dir = Vector3D.of(1e-11, 0, 0);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines3D.rayFromPointAndDirection(pt, dir, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPoint() {
        // arrange
        final Vector3D pt = Vector3D.of(-2, -1, 2);

        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(1, 0, 2), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final Ray3D ray = Lines3D.rayFromPoint(line, pt);

        // assert
        Assert.assertTrue(ray.isInfinite());
        Assert.assertFalse(ray.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, 2), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        Assert.assertEquals(-1, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        GeometryTestUtils.assertPositiveInfinity(ray.getSize());

        Assert.assertNull(ray.getCentroid());
        Assert.assertNull(ray.getBounds());
    }

    @Test
    public void testFromPoint_invalidArgs() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines3D.rayFromPoint(line, Vector3D.NaN);
        }, IllegalArgumentException.class, "Invalid ray start location: NaN");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.rayFromPoint(line, Vector3D.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid ray start location: NaN");

        GeometryTestUtils.assertThrows(() -> {
            Lines3D.rayFromPoint(line, Vector3D.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid ray start location: NaN");
    }

    @Test
    public void testFromLocation() {
        // arrange
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(-1, 0, 0), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Ray3D ray = Lines3D.rayFromLocation(line, -1);

        // assert
        Assert.assertTrue(ray.isInfinite());
        Assert.assertFalse(ray.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, -1), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        Assert.assertEquals(-1, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        GeometryTestUtils.assertPositiveInfinity(ray.getSize());

        Assert.assertNull(ray.getCentroid());
        Assert.assertNull(ray.getBounds());
    }

    @Test
    public void testTransform() {
        // arrange
        final AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y);

        final Ray3D ray = Lines3D.rayFromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final Ray3D result = ray.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, -1), result.getStartPoint(), TEST_EPS);
        Assert.assertNull(result.getEndPoint());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, result.getDirection(), TEST_EPS);
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        final AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y)
                .scale(1, 1, -2);

        final Ray3D ray = Lines3D.rayFromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final Ray3D result = ray.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 2), result.getStartPoint(), TEST_EPS);
        Assert.assertNull(result.getEndPoint());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, result.getDirection(), TEST_EPS);
    }

    @Test
    public void testContains() {
        // arrange
        final Vector3D p0 = Vector3D.of(1, 1, 1);

        final Vector3D delta = Vector3D.of(1e-12, 1e-12, 1e-12);

        final Ray3D ray = Lines3D.rayFromPointAndDirection(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        Assert.assertFalse(ray.contains(Vector3D.of(2, 2, 2)));
        Assert.assertFalse(ray.contains(Vector3D.of(0.9, 1, 1)));
        Assert.assertFalse(ray.contains(Vector3D.of(-1, 1, 1)));

        Assert.assertTrue(ray.contains(p0));
        Assert.assertTrue(ray.contains(p0.subtract(delta)));

        Assert.assertTrue(ray.contains(Vector3D.of(1000, 1, 1)));
    }

    @Test
    public void testGetInterval() {
        // arrange
        final Ray3D ray = Lines3D.rayFromPointAndDirection(Vector3D.of(2, -1, 3), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final Interval interval = ray.getInterval();

        // assert
        Assert.assertEquals(-1, interval.getMin(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(interval.getMax());

        Assert.assertSame(ray.getLine().getPrecision(), interval.getMinBoundary().getPrecision());
    }

    @Test
    public void testToString() {
        // arrange
        final Ray3D ray = Lines3D.rayFromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final String str = ray.toString();

        // assert
        GeometryTestUtils.assertContains("Ray3D[startPoint= (0", str);
        GeometryTestUtils.assertContains(", direction= (1", str);
    }
}
