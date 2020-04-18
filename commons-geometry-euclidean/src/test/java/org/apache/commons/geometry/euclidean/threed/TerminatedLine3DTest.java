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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.junit.Assert;
import org.junit.Test;

public class TerminatedLine3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPointAndDirection() {
        // arrange
        Vector3D pt = Vector3D.of(1, 1, 2);

        // act
        TerminatedLine3D halfLine = TerminatedLine3D.fromPointAndDirection(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // assert
        Assert.assertTrue(halfLine.isInfinite());
        Assert.assertFalse(halfLine.isFinite());

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(pt, halfLine.getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, halfLine.getLine().getDirection(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(2, halfLine.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(halfLine.getSize());
    }

    @Test
    public void testFromPointAndDirection_invalidArgs() {
        // arrange
        Vector3D pt = Vector3D.of(0, 2, 4);
        Vector3D dir = Vector3D.of(1e-11, 0, 0);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            TerminatedLine3D.fromPointAndDirection(pt, dir, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPoint() {
        // arrange
        Vector3D pt = Vector3D.of(-2, -1, 2);

        Line3D line = Line3D.fromPointAndDirection(Vector3D.of(1, 0, 2), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        TerminatedLine3D halfLine = TerminatedLine3D.fromPoint(line, pt);

        // assert
        Assert.assertTrue(halfLine.isInfinite());
        Assert.assertFalse(halfLine.isFinite());

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, 2), halfLine.getEndPoint(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(-1, halfLine.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(halfLine.getSize());
    }

    @Test
    public void testFromPoint_invalidArgs() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            TerminatedLine3D.fromPoint(line, Vector3D.NaN);
        }, IllegalArgumentException.class, "Invalid terminated line end location: NaN");

        GeometryTestUtils.assertThrows(() -> {
            TerminatedLine3D.fromPoint(line, Vector3D.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid terminated line end location: NaN");

        GeometryTestUtils.assertThrows(() -> {
            TerminatedLine3D.fromPoint(line, Vector3D.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid terminated line end location: NaN");
    }

    @Test
    public void testFromLocation() {
        // arrange
        Line3D line = Line3D.fromPointAndDirection(Vector3D.of(-1, 0, 0), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        TerminatedLine3D halfLine = TerminatedLine3D.fromLocation(line, -1);

        // assert
        Assert.assertTrue(halfLine.isInfinite());
        Assert.assertFalse(halfLine.isFinite());

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, -1), halfLine.getEndPoint(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(-1, halfLine.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(halfLine.getSize());
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix3D t = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, 0.5 * Math.PI)
                .toMatrix()
                .translate(Vector3D.Unit.PLUS_Y);

        TerminatedLine3D halfLine = TerminatedLine3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        TerminatedLine3D result = halfLine.transform(t);

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

        TerminatedLine3D halfLine = TerminatedLine3D.fromPointAndDirection(Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        TerminatedLine3D result = halfLine.transform(t);

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

        TerminatedLine3D halfLine = TerminatedLine3D.fromPointAndDirection(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        Assert.assertFalse(halfLine.contains(Vector3D.of(2, 2, 2)));
        Assert.assertFalse(halfLine.contains(Vector3D.of(1.1, 1, 1)));
        Assert.assertFalse(halfLine.contains(Vector3D.of(100, 1, 1)));

        Assert.assertTrue(halfLine.contains(p0));
        Assert.assertTrue(halfLine.contains(p0.add(delta)));

        Assert.assertTrue(halfLine.contains(Vector3D.of(-1000, 1, 1)));
    }

    @Test
    public void testGetInterval() {
        // arrange
        TerminatedLine3D halfLine = TerminatedLine3D.fromPointAndDirection(Vector3D.of(2, -1, 3), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        Interval interval = halfLine.getInterval();

        // assert
        GeometryTestUtils.assertNegativeInfinity(interval.getMin());
        Assert.assertEquals(-1, interval.getMax(), TEST_EPS);

        Assert.assertSame(halfLine.getLine().getPrecision(), interval.getMaxBoundary().getPrecision());
    }

    @Test
    public void testToString() {
        // arrange
        TerminatedLine3D halfLine = TerminatedLine3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        String str = halfLine.toString();

        // assert
        GeometryTestUtils.assertContains("TerminatedLine3D[direction= (1", str);
        GeometryTestUtils.assertContains(", endPoint= (0", str);
    }
}
