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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.partitioning.Transform_Old;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class LineTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPoints() {
        // act/assert
        checkLine(Line_Old.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION),
                Vector2D.ZERO, Vector2D.PLUS_X);
        checkLine(Line_Old.fromPoints(Vector2D.ZERO, Vector2D.of(100, 0), TEST_PRECISION),
                Vector2D.ZERO, Vector2D.PLUS_X);
        checkLine(Line_Old.fromPoints(Vector2D.of(100, 0), Vector2D.ZERO, TEST_PRECISION),
                Vector2D.ZERO, Vector2D.MINUS_X);
        checkLine(Line_Old.fromPoints(Vector2D.of(-100, 0), Vector2D.of(100, 0), TEST_PRECISION),
                Vector2D.ZERO, Vector2D.PLUS_X);

        checkLine(Line_Old.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 2), TEST_PRECISION),
                Vector2D.of(-1, 1), Vector2D.of(1, 1).normalize());
        checkLine(Line_Old.fromPoints(Vector2D.of(0, 2), Vector2D.of(-2, 0), TEST_PRECISION),
                Vector2D.of(-1, 1), Vector2D.of(-1, -1).normalize());
    }

    @Test
    public void testFromPoints_pointsTooClose() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> Line_Old.fromPoints(Vector2D.PLUS_X, Vector2D.PLUS_X, TEST_PRECISION),
                GeometryValueException.class, "Line direction cannot be zero");
        GeometryTestUtils.assertThrows(() -> Line_Old.fromPoints(Vector2D.PLUS_X, Vector2D.of(1 + 1e-11, 1e-11), TEST_PRECISION),
                GeometryValueException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPointAndDirection() {
        // act/assert
        checkLine(Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION),
                Vector2D.ZERO, Vector2D.PLUS_X);
        checkLine(Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(100, 0), TEST_PRECISION),
                Vector2D.ZERO, Vector2D.PLUS_X);
        checkLine(Line_Old.fromPointAndDirection(Vector2D.of(-100, 0), Vector2D.of(100, 0), TEST_PRECISION),
                Vector2D.ZERO, Vector2D.PLUS_X);

        checkLine(Line_Old.fromPointAndDirection(Vector2D.of(-2, 0), Vector2D.of(1, 1), TEST_PRECISION),
                Vector2D.of(-1, 1), Vector2D.of(1, 1).normalize());
        checkLine(Line_Old.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(-1, -1), TEST_PRECISION),
                Vector2D.of(-1, 1), Vector2D.of(-1, -1).normalize());
    }

    @Test
    public void testFromPointAndDirection_directionIsZero() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> Line_Old.fromPointAndDirection(Vector2D.PLUS_X, Vector2D.ZERO, TEST_PRECISION),
                GeometryValueException.class, "Line direction cannot be zero");
        GeometryTestUtils.assertThrows(() -> Line_Old.fromPointAndDirection(Vector2D.PLUS_X, Vector2D.of(1e-11, -1e-12), TEST_PRECISION),
                GeometryValueException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPointAndAngle() {
        // act/assert
        checkLine(Line_Old.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION),
                Vector2D.ZERO, Vector2D.PLUS_X);
        checkLine(Line_Old.fromPointAndAngle(Vector2D.of(1, 1), Geometry.HALF_PI, TEST_PRECISION),
                Vector2D.of(1, 0), Vector2D.PLUS_Y);
        checkLine(Line_Old.fromPointAndAngle(Vector2D.of(-1, -1), Geometry.PI, TEST_PRECISION),
                Vector2D.of(0, -1), Vector2D.MINUS_X);
        checkLine(Line_Old.fromPointAndAngle(Vector2D.of(1, -1), Geometry.MINUS_HALF_PI, TEST_PRECISION),
                Vector2D.of(1, 0), Vector2D.MINUS_Y);
        checkLine(Line_Old.fromPointAndAngle(Vector2D.of(-1, 1), Geometry.TWO_PI, TEST_PRECISION),
                Vector2D.of(0, 1), Vector2D.PLUS_X);
    }

    @Test
    public void testGetAngle() {
        // arrange
        Vector2D vec = Vector2D.of(1, 2);

        for (double theta = -4 * Geometry.PI; theta < 2 * Geometry.PI; theta += 0.1) {
            Line_Old line = Line_Old.fromPointAndAngle(vec, theta, TEST_PRECISION);

            // act/assert
            Assert.assertEquals(PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(theta),
                    line.getAngle(), TEST_EPS);
        }
    }

    @Test
    public void testGetAngle_multiplesOfPi() {
        // arrange
        Vector2D vec = Vector2D.of(-1, -2);

        // act/assert
        Assert.assertEquals(0, Line_Old.fromPointAndAngle(vec, Geometry.ZERO_PI, TEST_PRECISION).getAngle(), TEST_EPS);
        Assert.assertEquals(Geometry.PI, Line_Old.fromPointAndAngle(vec, Geometry.PI, TEST_PRECISION).getAngle(), TEST_EPS);
        Assert.assertEquals(0, Line_Old.fromPointAndAngle(vec, Geometry.TWO_PI, TEST_PRECISION).getAngle(), TEST_EPS);

        Assert.assertEquals(0, Line_Old.fromPointAndAngle(vec, -2 * Geometry.PI, TEST_PRECISION).getAngle(), TEST_EPS);
        Assert.assertEquals(Geometry.PI, Line_Old.fromPointAndAngle(vec, -3 * Geometry.PI, TEST_PRECISION).getAngle(), TEST_EPS);
        Assert.assertEquals(0, Line_Old.fromPointAndAngle(vec, -4 * Geometry.TWO_PI, TEST_PRECISION).getAngle(), TEST_EPS);
    }

    @Test
    public void testGetDirection() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.PLUS_X,
                Line_Old.fromPoints(Vector2D.of(0, 0), Vector2D.of(1, 0), TEST_PRECISION).getDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.MINUS_Y,
                Line_Old.fromPoints(Vector2D.of(0, 1), Vector2D.of(0, -1), TEST_PRECISION).getDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.MINUS_X,
                Line_Old.fromPoints(Vector2D.of(2, 2), Vector2D.of(1, 2), TEST_PRECISION).getDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.PLUS_X,
                Line_Old.fromPoints(Vector2D.of(10, -2), Vector2D.of(10.1, -2), TEST_PRECISION).getDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.MINUS_Y,
                Line_Old.fromPoints(Vector2D.of(3, 2), Vector2D.of(3, 1), TEST_PRECISION).getDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.PLUS_Y,
                Line_Old.fromPoints(Vector2D.of(-3, 10), Vector2D.of(-3, 10.1), TEST_PRECISION).getDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -1).normalize(),
                Line_Old.fromPoints(Vector2D.of(0, 2), Vector2D.of(2, 0), TEST_PRECISION).getDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1).normalize(),
                Line_Old.fromPoints(Vector2D.of(2, 0), Vector2D.of(0, 2), TEST_PRECISION).getDirection(), TEST_EPS);
    }

    @Test
    public void testGetOffsetDirection() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.MINUS_Y,
                Line_Old.fromPoints(Vector2D.of(0, 0), Vector2D.of(1, 0), TEST_PRECISION).getOffsetDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.MINUS_X,
                Line_Old.fromPoints(Vector2D.of(0, 1), Vector2D.of(0, -1), TEST_PRECISION).getOffsetDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.PLUS_Y,
                Line_Old.fromPoints(Vector2D.of(2, 2), Vector2D.of(1, 2), TEST_PRECISION).getOffsetDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.MINUS_Y,
                Line_Old.fromPoints(Vector2D.of(10, -2), Vector2D.of(10.1, -2), TEST_PRECISION).getOffsetDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.MINUS_X,
                Line_Old.fromPoints(Vector2D.of(3, 2), Vector2D.of(3, 1), TEST_PRECISION).getOffsetDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.PLUS_X,
                Line_Old.fromPoints(Vector2D.of(-3, 10), Vector2D.of(-3, 10.1), TEST_PRECISION).getOffsetDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, -1).normalize(),
                Line_Old.fromPoints(Vector2D.of(0, 2), Vector2D.of(2, 0), TEST_PRECISION).getOffsetDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1).normalize(),
                Line_Old.fromPoints(Vector2D.of(2, 0), Vector2D.of(0, 2), TEST_PRECISION).getOffsetDirection(), TEST_EPS);
    }

    @Test
    public void testGetOrigin() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO,
                Line_Old.fromPoints(Vector2D.of(0, 0), Vector2D.of(1, 0), TEST_PRECISION).getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO,
                Line_Old.fromPoints(Vector2D.of(0, 1), Vector2D.of(0, -1), TEST_PRECISION).getOrigin(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2),
                Line_Old.fromPoints(Vector2D.of(2, 2), Vector2D.of(3, 2), TEST_PRECISION).getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -2),
                Line_Old.fromPoints(Vector2D.of(10, -2), Vector2D.of(10.1, -2), TEST_PRECISION).getOrigin(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 0),
                Line_Old.fromPoints(Vector2D.of(3, 2), Vector2D.of(3, 1), TEST_PRECISION).getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 0),
                Line_Old.fromPoints(Vector2D.of(-3, 10), Vector2D.of(-3, 10.1), TEST_PRECISION).getOrigin(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1),
                Line_Old.fromPoints(Vector2D.of(0, 2), Vector2D.of(2, 0), TEST_PRECISION).getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1),
                Line_Old.fromPoints(Vector2D.of(2, 0), Vector2D.of(0, 2), TEST_PRECISION).getOrigin(), TEST_EPS);
    }

    @Test
    public void testGetOriginOffset() {
        // arrange
        double sqrt2 = Math.sqrt(2);

        // act/assert
        Assert.assertEquals(0.0,
                Line_Old.fromPoints(Vector2D.of(0, 0), Vector2D.of(1, 1), TEST_PRECISION).getOriginOffset(), TEST_EPS);
        Assert.assertEquals(0.0,
                Line_Old.fromPoints(Vector2D.of(0, 0), Vector2D.of(-1, -1), TEST_PRECISION).getOriginOffset(), TEST_EPS);

        Assert.assertEquals(sqrt2,
                Line_Old.fromPoints(Vector2D.of(-1, 1), Vector2D.of(0, 2), TEST_PRECISION).getOriginOffset(), TEST_EPS);
        Assert.assertEquals(-sqrt2,
                Line_Old.fromPoints(Vector2D.of(0, -2), Vector2D.of(1, -1), TEST_PRECISION).getOriginOffset(), TEST_EPS);

        Assert.assertEquals(-sqrt2,
                Line_Old.fromPoints(Vector2D.of(0, 2), Vector2D.of(-1, 1), TEST_PRECISION).getOriginOffset(), TEST_EPS);
        Assert.assertEquals(sqrt2,
                Line_Old.fromPoints(Vector2D.of(1, -1), Vector2D.of(0, -2), TEST_PRECISION).getOriginOffset(), TEST_EPS);
    }

    @Test
    public void testGetPrecision() {
        // act/assert
        Assert.assertSame(TEST_PRECISION, Line_Old.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION).getPrecision());
        Assert.assertSame(TEST_PRECISION, Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION).getPrecision());
        Assert.assertSame(TEST_PRECISION, Line_Old.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION).getPrecision());
    }

    @Test
    public void testCopySelf() {
        // arrange
        Line_Old line = Line_Old.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

        // act/assert
        Assert.assertSame(line, line.copySelf());
    }

    @Test
    public void testReverse() {
        // arrange
        Vector2D pt = Vector2D.of(0, 1);
        Vector2D dir = Vector2D.PLUS_X;
        Line_Old line = Line_Old.fromPointAndDirection(pt, dir, TEST_PRECISION);

        // act
        Line_Old reversed = line.reverse();
        Line_Old doubleReversed = reversed.reverse();

        // assert
        checkLine(reversed, pt, dir.negate());
        Assert.assertEquals(-1, reversed.getOriginOffset(), TEST_EPS);

        checkLine(doubleReversed, pt, dir);
        Assert.assertEquals(1, doubleReversed.getOriginOffset(), TEST_EPS);
    }

    @Test
    public void testToSubSpace() {
        // arrange
        Line_Old line = Line_Old.fromPoints(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0.0, line.toSubSpace(Vector2D.of(-3,  4)).getX(), TEST_EPS);
        Assert.assertEquals(0.0, line.toSubSpace(Vector2D.of( 3, -4)).getX(), TEST_EPS);
        Assert.assertEquals(-5.0, line.toSubSpace(Vector2D.of(7, -1)).getX(), TEST_EPS);
        Assert.assertEquals(5.0, line.toSubSpace(Vector2D.of(-1, -7)).getX(), TEST_EPS);
    }

    @Test
    public void testToSpace_throughOrigin() {
        // arrange
        double invSqrt2 = 1 / Math.sqrt(2);
        Vector2D dir = Vector2D.of(invSqrt2, invSqrt2);

        Line_Old line = Line_Old.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, line.toSpace(Vector1D.of(0)), TEST_EPS);

        for (int i=0; i<100; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(dir.multiply(i), line.toSpace(Vector1D.of(i)), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(dir.multiply(-i), line.toSpace(Vector1D.of(-i)), TEST_EPS);
        }
    }

    @Test
    public void testToSpace_offsetFromOrigin() {
        // arrange
        double angle = Geometry.PI / 6;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        Vector2D pt = Vector2D.of(-5, 0);

        double h = Math.abs(pt.getX()) * cos;
        double d = h * cos;
        Vector2D origin = Vector2D.of(
                    pt.getX() + d,
                    h * sin
                );
        Vector2D dir = Vector2D.of(cos, sin);

        Line_Old line = Line_Old.fromPointAndAngle(pt, angle, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(origin, line.toSpace(Vector1D.of(0)), TEST_EPS);

        for (int i=0; i<100; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(origin.add(dir.multiply(i)), line.toSpace(Vector1D.of(i)), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(origin.add(dir.multiply(-i)), line.toSpace(Vector1D.of(-i)), TEST_EPS);
        }
    }

    @Test
    public void testIntersection() {
        // arrange
        Line_Old a = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);
        Line_Old b = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_Y, TEST_PRECISION);
        Line_Old c = Line_Old.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);
        Line_Old d = Line_Old.fromPointAndDirection(Vector2D.of(0, -1), Vector2D.of(2, -1), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, a.intersection(b), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, b.intersection(a), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-4, 0), a.intersection(c), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-4, 0), c.intersection(a), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 0), a.intersection(d), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 0), d.intersection(a), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2), b.intersection(c), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2), c.intersection(b), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -1), b.intersection(d), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -1), d.intersection(b), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 0.5), c.intersection(d), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 0.5), d.intersection(c), TEST_EPS);
    }

    @Test
    public void testIntersection_parallel() {
        // arrange
        Line_Old a = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);
        Line_Old b = Line_Old.fromPointAndDirection(Vector2D.of(0, 1), Vector2D.PLUS_X, TEST_PRECISION);

        Line_Old c = Line_Old.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);
        Line_Old d = Line_Old.fromPointAndDirection(Vector2D.of(0, -1), Vector2D.of(2, 1), TEST_PRECISION);

        // act/assert
        Assert.assertNull(a.intersection(b));
        Assert.assertNull(b.intersection(a));

        Assert.assertNull(c.intersection(d));
        Assert.assertNull(d.intersection(c));
    }

    @Test
    public void testIntersection_coincident() {
        // arrange
        Line_Old a = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);
        Line_Old b = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

        Line_Old c = Line_Old.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);
        Line_Old d = Line_Old.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);

        // act/assert
        Assert.assertNull(a.intersection(b));
        Assert.assertNull(b.intersection(a));

        Assert.assertNull(c.intersection(d));
        Assert.assertNull(d.intersection(c));
    }

    @Test
    public void testProject() {
        // --- arrange
        Line_Old xAxis = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);
        Line_Old yAxis = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_Y, TEST_PRECISION);

        double diagonalYIntercept = 1;
        Vector2D diagonalDir = Vector2D.of(1, 2);
        Line_Old diagonal = Line_Old.fromPointAndDirection(Vector2D.of(0, diagonalYIntercept), diagonalDir, TEST_PRECISION);

        EuclideanTestUtils.permute(-5, 5, 0.5, (x, y) -> {
            Vector2D pt = Vector2D.of(x, y);

            // --- act/assert
            EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(x, 0), xAxis.project(pt), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, y), yAxis.project(pt), TEST_EPS);

            Vector2D diagonalPt = diagonal.project(pt);
            Assert.assertTrue(diagonal.contains(diagonalPt));
            Assert.assertEquals(diagonal.distance(pt), pt.distance(diagonalPt), TEST_EPS);

            // check that y = mx + b is true
            Assert.assertEquals(diagonalPt.getY(),
                    (diagonalDir.getY() * diagonalPt.getX() / diagonalDir.getX()) + diagonalYIntercept, TEST_EPS);
        });
    }

    @Test
    public void testWholeHyperplane() {
        // arrange
        Line_Old line = Line_Old.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

        // act
        SubLine_Old result = line.wholeHyperplane();

        // assert
        Assert.assertSame(line, result.getHyperplane());
        GeometryTestUtils.assertPositiveInfinity(result.getSize());
    }

    @Test
    public void testWholeSpace() {
        // arrange
        Line_Old line = Line_Old.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

        // act
        PolygonsSet result = line.wholeSpace();

        // assert
        GeometryTestUtils.assertPositiveInfinity(result.getSize());
        Assert.assertSame(TEST_PRECISION, result.getPrecision());
    }

    @Test
    public void testGetOffset_parallelLines() {
        // arrange
        double dist = Math.sin(Math.atan2(2, 1));

        Line_Old a = Line_Old.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line_Old b = Line_Old.fromPoints(Vector2D.of(-3, 0), Vector2D.of(0, 6), TEST_PRECISION);
        Line_Old c = Line_Old.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line_Old d = Line_Old.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, -2), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(-dist, a.getOffset(b), TEST_EPS);
        Assert.assertEquals(dist, b.getOffset(a), TEST_EPS);

        Assert.assertEquals(dist, a.getOffset(c), TEST_EPS);
        Assert.assertEquals(-dist, c.getOffset(a), TEST_EPS);

        Assert.assertEquals(3 * dist, a.getOffset(d), TEST_EPS);
        Assert.assertEquals(3 * dist, d.getOffset(a), TEST_EPS);
    }

    @Test
    public void testGetOffset_coincidentLines() {
        // arrange
        Line_Old a = Line_Old.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line_Old b = Line_Old.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line_Old c = b.reverse();

        // act/assert
        Assert.assertEquals(0, a.getOffset(a), TEST_EPS);

        Assert.assertEquals(0, a.getOffset(b), TEST_EPS);
        Assert.assertEquals(0, b.getOffset(a), TEST_EPS);

        Assert.assertEquals(0, a.getOffset(c), TEST_EPS);
        Assert.assertEquals(0, c.getOffset(a), TEST_EPS);
    }

    @Test
    public void testGetOffset_nonParallelLines() {
        // arrange
        Line_Old a = Line_Old.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);
        Line_Old b = Line_Old.fromPoints(Vector2D.ZERO, Vector2D.PLUS_Y, TEST_PRECISION);
        Line_Old c = Line_Old.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line_Old d = Line_Old.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, 4), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0, a.getOffset(b), TEST_EPS);
        Assert.assertEquals(0, b.getOffset(a), TEST_EPS);

        Assert.assertEquals(0, a.getOffset(c), TEST_EPS);
        Assert.assertEquals(0, c.getOffset(a), TEST_EPS);

        Assert.assertEquals(0, a.getOffset(d), TEST_EPS);
        Assert.assertEquals(0, d.getOffset(a), TEST_EPS);
    }

    @Test
    public void testGetOffset_point() {
        // arrange
        Line_Old line = Line_Old.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line_Old reversed = line.reverse();

        // act/assert
        Assert.assertEquals(0.0, line.getOffset(Vector2D.of(-0.5, 1)), TEST_EPS);
        Assert.assertEquals(0.0, line.getOffset(Vector2D.of(-1.5, -1)), TEST_EPS);
        Assert.assertEquals(0.0, line.getOffset(Vector2D.of(0.5, 3)), TEST_EPS);

        double d = Math.sin(Math.atan2(2, 1));

        Assert.assertEquals(d, line.getOffset(Vector2D.ZERO), TEST_EPS);
        Assert.assertEquals(-d, line.getOffset(Vector2D.of(-1, 2)), TEST_EPS);

        Assert.assertEquals(-d, reversed.getOffset(Vector2D.ZERO), TEST_EPS);
        Assert.assertEquals(d, reversed.getOffset(Vector2D.of(-1, 2)), TEST_EPS);
    }

    @Test
    public void testGetOffset_point_permute() {
        // arrange
        Line_Old line = Line_Old.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Vector2D lineOrigin = line.getOrigin();

        EuclideanTestUtils.permute(-5, 5, 0.5, (x, y) -> {
            Vector2D pt = Vector2D.of(x, y);

            // act
            double offset = line.getOffset(pt);

            // arrange
            Vector2D vec = lineOrigin.vectorTo(pt).reject(line.getDirection());
            double dot = vec.dot(line.getOffsetDirection());
            double expected = Math.signum(dot) * vec.norm();

            Assert.assertEquals(expected, offset, TEST_EPS);
        });
    }

    @Test
    public void testSameOrientationAs() {
        // arrange
        Line_Old a = Line_Old.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION);
        Line_Old b = Line_Old.fromPointAndAngle(Vector2D.of(4, 5), Geometry.ZERO_PI, TEST_PRECISION);
        Line_Old c = Line_Old.fromPointAndAngle(Vector2D.of(-1, -3), 0.4 * Geometry.PI, TEST_PRECISION);
        Line_Old d = Line_Old.fromPointAndAngle(Vector2D.of(1, 0), -0.4 * Geometry.PI, TEST_PRECISION);

        Line_Old e = Line_Old.fromPointAndAngle(Vector2D.of(6, -3), Geometry.PI, TEST_PRECISION);
        Line_Old f = Line_Old.fromPointAndAngle(Vector2D.of(8, 5), 0.8 * Geometry.PI, TEST_PRECISION);
        Line_Old g = Line_Old.fromPointAndAngle(Vector2D.of(6, -3), -0.8 * Geometry.PI, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.sameOrientationAs(a));
        Assert.assertTrue(a.sameOrientationAs(b));
        Assert.assertTrue(b.sameOrientationAs(a));
        Assert.assertTrue(a.sameOrientationAs(c));
        Assert.assertTrue(c.sameOrientationAs(a));
        Assert.assertTrue(a.sameOrientationAs(d));
        Assert.assertTrue(d.sameOrientationAs(a));

        Assert.assertFalse(c.sameOrientationAs(d));
        Assert.assertFalse(d.sameOrientationAs(c));

        Assert.assertTrue(e.sameOrientationAs(f));
        Assert.assertTrue(f.sameOrientationAs(e));
        Assert.assertTrue(e.sameOrientationAs(g));
        Assert.assertTrue(g.sameOrientationAs(e));

        Assert.assertFalse(a.sameOrientationAs(e));
        Assert.assertFalse(e.sameOrientationAs(a));
    }

    @Test
    public void testSameOrientationAs_orthogonal() {
        // arrange
        Line_Old a = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);
        Line_Old b = Line_Old.fromPointAndDirection(Vector2D.of(4, 5), Vector2D.PLUS_Y, TEST_PRECISION);
        Line_Old c = Line_Old.fromPointAndDirection(Vector2D.of(-4, -5), Vector2D.MINUS_Y, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.sameOrientationAs(b));
        Assert.assertTrue(b.sameOrientationAs(a));
        Assert.assertTrue(a.sameOrientationAs(c));
        Assert.assertTrue(c.sameOrientationAs(a));
    }

    @Test
    public void testDistance_parallelLines() {
        // arrange
        double dist = Math.sin(Math.atan2(2, 1));

        Line_Old a = Line_Old.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line_Old b = Line_Old.fromPoints(Vector2D.of(-3, 0), Vector2D.of(0, 6), TEST_PRECISION);
        Line_Old c = Line_Old.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line_Old d = Line_Old.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, -2), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(dist, a.distance(b), TEST_EPS);
        Assert.assertEquals(dist, b.distance(a), TEST_EPS);

        Assert.assertEquals(dist, a.distance(c), TEST_EPS);
        Assert.assertEquals(dist, c.distance(a), TEST_EPS);

        Assert.assertEquals(3 * dist, a.distance(d), TEST_EPS);
        Assert.assertEquals(3 * dist, d.distance(a), TEST_EPS);
    }

    @Test
    public void testDistance_coincidentLines() {
        // arrange
        Line_Old a = Line_Old.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line_Old b = Line_Old.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line_Old c = b.reverse();

        // act/assert
        Assert.assertEquals(0, a.distance(a), TEST_EPS);

        Assert.assertEquals(0, a.distance(b), TEST_EPS);
        Assert.assertEquals(0, b.distance(a), TEST_EPS);

        Assert.assertEquals(0, a.distance(c), TEST_EPS);
        Assert.assertEquals(0, c.distance(a), TEST_EPS);
    }

    @Test
    public void testDistance_nonParallelLines() {
        // arrange
        Line_Old a = Line_Old.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);
        Line_Old b = Line_Old.fromPoints(Vector2D.ZERO, Vector2D.PLUS_Y, TEST_PRECISION);
        Line_Old c = Line_Old.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line_Old d = Line_Old.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, 4), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0, a.distance(b), TEST_EPS);
        Assert.assertEquals(0, b.distance(a), TEST_EPS);

        Assert.assertEquals(0, a.distance(c), TEST_EPS);
        Assert.assertEquals(0, c.distance(a), TEST_EPS);

        Assert.assertEquals(0, a.distance(d), TEST_EPS);
        Assert.assertEquals(0, d.distance(a), TEST_EPS);
    }

    @Test
    public void testDistance() {
        // arrange
        Line_Old line = Line_Old.fromPoints(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0, line.distance(line.getOrigin()), TEST_EPS);
        Assert.assertEquals(+5.0, line.distance(Vector2D.of(5, -3)), TEST_EPS);
        Assert.assertEquals(+5.0, line.distance(Vector2D.of(-5, 2)), TEST_EPS);
    }

    @Test
    public void testPointAt() {
        // arrange
        Vector2D origin = Vector2D.of(-1, 1);
        double d = Math.sqrt(2);
        Line_Old line = Line_Old.fromPointAndDirection(origin, Vector2D.of(1, 1), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(origin, line.pointAt(0, 0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, line.pointAt(0, d), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 2), line.pointAt(0, -d), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 0), line.pointAt(-d, 0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2), line.pointAt(d, 0), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), line.pointAt(d, d), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), line.pointAt(-d, -d), TEST_EPS);
    }

    @Test
    public void testPointAt_abscissaOffsetRoundtrip() {
        // arrange
        Line_Old line = Line_Old.fromPoints(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);

        for (double abscissa = -2.0; abscissa < 2.0; abscissa += 0.2) {
            for (double offset = -2.0; offset < 2.0; offset += 0.2) {

                // act
                Vector2D point = line.pointAt(abscissa, offset);

                // assert
                Assert.assertEquals(abscissa, line.toSubSpace(point).getX(), TEST_EPS);
                Assert.assertEquals(offset, line.getOffset(point), TEST_EPS);
            }
        }
    }

    @Test
    public void testContains_line() {
        // arrange
        Vector2D pt = Vector2D.of(1, 2);
        Vector2D dir = Vector2D.of(3, 7);
        Line_Old a = Line_Old.fromPointAndDirection(pt, dir, TEST_PRECISION);
        Line_Old b = Line_Old.fromPointAndDirection(Vector2D.of(0, -4), dir, TEST_PRECISION);
        Line_Old c = Line_Old.fromPointAndDirection(Vector2D.of(-2, -2), dir.negate(), TEST_PRECISION);
        Line_Old d = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

        Line_Old e = Line_Old.fromPointAndDirection(pt, dir, TEST_PRECISION);
        Line_Old f = Line_Old.fromPointAndDirection(pt, dir.negate(), TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.contains(a));

        Assert.assertTrue(a.contains(e));
        Assert.assertTrue(e.contains(a));

        Assert.assertTrue(a.contains(f));
        Assert.assertTrue(f.contains(a));

        Assert.assertFalse(a.contains(b));
        Assert.assertFalse(a.contains(c));
        Assert.assertFalse(a.contains(d));
    }

    @Test
    public void testIsParallel_closeToEpsilon() {
        // arrange
        double eps = 1e-3;
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        Vector2D p = Vector2D.of(1, 2);

        Line_Old line = Line_Old.fromPointAndAngle(p, Geometry.ZERO_PI, precision);

        // act/assert
        Vector2D offset1 = Vector2D.of(0, 1e-4);
        Vector2D offset2 = Vector2D.of(0, 2e-3);

        Assert.assertTrue(line.contains(Line_Old.fromPointAndAngle(p.add(offset1), Geometry.ZERO_PI, precision)));
        Assert.assertTrue(line.contains(Line_Old.fromPointAndAngle(p.subtract(offset1), Geometry.ZERO_PI, precision)));

        Assert.assertFalse(line.contains(Line_Old.fromPointAndAngle(p.add(offset2), Geometry.ZERO_PI, precision)));
        Assert.assertFalse(line.contains(Line_Old.fromPointAndAngle(p.subtract(offset2), Geometry.ZERO_PI, precision)));

        Assert.assertTrue(line.contains(Line_Old.fromPointAndAngle(p, 1e-4, precision)));
        Assert.assertFalse(line.contains(Line_Old.fromPointAndAngle(p, 1e-2, precision)));
    }

    @Test
    public void testContains_point() {
        // arrange
        Vector2D p1 = Vector2D.of(-1, 0);
        Vector2D p2 = Vector2D.of(0, 2);
        Line_Old line = Line_Old.fromPoints(p1, p2, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(line.contains(p1));
        Assert.assertTrue(line.contains(p2));

        Assert.assertFalse(line.contains(Vector2D.ZERO));
        Assert.assertFalse(line.contains(Vector2D.of(100, 79)));

        Vector2D offset1 = Vector2D.of(0.1, 0);
        Vector2D offset2 = Vector2D.of(0, -0.1);
        Vector2D v;
        for (double t=-2; t<=2; t+=0.1) {
            v = p1.lerp(p2, t);

            Assert.assertTrue(line.contains(v));

            Assert.assertFalse(line.contains(v.add(offset1)));
            Assert.assertFalse(line.contains(v.add(offset2)));
        }
    }

    @Test
    public void testContains_point_closeToEpsilon() {
        // arrange
        double eps = 1e-3;
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        Vector2D p1 = Vector2D.of(-1, 0);
        Vector2D p2 = Vector2D.of(0, 2);
        Vector2D mid = p1.lerp(p2, 0.5);

        Line_Old line = Line_Old.fromPoints(p1, p2, precision);
        Vector2D dir = line.getOffsetDirection();

        // act/assert
        Assert.assertTrue(line.contains(mid.add(dir.multiply(1e-4))));
        Assert.assertTrue(line.contains(mid.add(dir.multiply(-1e-4))));

        Assert.assertFalse(line.contains(mid.add(dir.multiply(2e-3))));
        Assert.assertFalse(line.contains(mid.add(dir.multiply(-2e-3))));
    }

    @Test
    public void testDistance_point() {
        // arrange
        Line_Old line = Line_Old.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line_Old reversed = line.reverse();

        // act/assert
        Assert.assertEquals(0.0, line.distance(Vector2D.of(-0.5, 1)), TEST_EPS);
        Assert.assertEquals(0.0, line.distance(Vector2D.of(-1.5, -1)), TEST_EPS);
        Assert.assertEquals(0.0, line.distance(Vector2D.of(0.5, 3)), TEST_EPS);

        double d = Math.sin(Math.atan2(2, 1));

        Assert.assertEquals(d, line.distance(Vector2D.ZERO), TEST_EPS);
        Assert.assertEquals(d, line.distance(Vector2D.of(-1, 2)), TEST_EPS);

        Assert.assertEquals(d, reversed.distance(Vector2D.ZERO), TEST_EPS);
        Assert.assertEquals(d, reversed.distance(Vector2D.of(-1, 2)), TEST_EPS);
    }

    @Test
    public void testDistance_point_permute() {
        // arrange
        Line_Old line = Line_Old.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Vector2D lineOrigin = line.getOrigin();

        EuclideanTestUtils.permute(-5, 5, 0.5, (x, y) -> {
            Vector2D pt = Vector2D.of(x, y);

            // act
            double dist = line.distance(pt);

            // arrange
            Vector2D vec = lineOrigin.vectorTo(pt).reject(line.getDirection());
            Assert.assertEquals(vec.norm(), dist, TEST_EPS);
        });
    }

    @Test
    public void testIsParallel() {
        // arrange
        Vector2D dir = Vector2D.of(3, 7);
        Line_Old a = Line_Old.fromPointAndDirection(Vector2D.of(1, 2), dir, TEST_PRECISION);
        Line_Old b = Line_Old.fromPointAndDirection(Vector2D.of(0, -4), dir, TEST_PRECISION);
        Line_Old c = Line_Old.fromPointAndDirection(Vector2D.of(-2, -2), dir.negate(), TEST_PRECISION);
        Line_Old d = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.isParallel(a));

        Assert.assertTrue(a.isParallel(b));
        Assert.assertTrue(b.isParallel(a));

        Assert.assertTrue(a.isParallel(c));
        Assert.assertTrue(c.isParallel(a));

        Assert.assertFalse(a.isParallel(d));
        Assert.assertFalse(d.isParallel(a));
    }

    @Test
    public void testIsParallel_closeToParallel() {
        // arrange
        double eps = 1e-3;
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(1, -2);

        Line_Old line = Line_Old.fromPointAndAngle(p1, Geometry.ZERO_PI, precision);

        // act/assert
        Assert.assertTrue(line.isParallel(Line_Old.fromPointAndAngle(p2, 1e-4, precision)));
        Assert.assertFalse(line.isParallel(Line_Old.fromPointAndAngle(p2, 1e-2, precision)));
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix2D scale = AffineTransformMatrix2D.createScale(2, 3);
        AffineTransformMatrix2D reflect = AffineTransformMatrix2D.createScale(-1, 1);
        AffineTransformMatrix2D translate = AffineTransformMatrix2D.createTranslation(3, 4);
        AffineTransformMatrix2D rotate = AffineTransformMatrix2D.createRotation(Geometry.HALF_PI);
        AffineTransformMatrix2D rotateAroundPt = AffineTransformMatrix2D.createRotation(Vector2D.of(0, 1), Geometry.HALF_PI);

        Vector2D p1 = Vector2D.of(0, 1);
        Vector2D p2 = Vector2D.of(1, 0);

        Line_Old horizontal = Line_Old.fromPointAndDirection(p1, Vector2D.PLUS_X, TEST_PRECISION);
        Line_Old vertical = Line_Old.fromPointAndDirection(p2, Vector2D.PLUS_Y, TEST_PRECISION);
        Line_Old diagonal = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act/assert
        Assert.assertSame(TEST_PRECISION, horizontal.transform(scale).getPrecision());

        checkLine(horizontal.transform(scale), Vector2D.of(0, 3), Vector2D.PLUS_X);
        checkLine(vertical.transform(scale), Vector2D.of(2, 0), Vector2D.PLUS_Y);
        checkLine(diagonal.transform(scale), Vector2D.ZERO, Vector2D.of(2, 3).normalize());

        checkLine(horizontal.transform(reflect), p1, Vector2D.MINUS_X);
        checkLine(vertical.transform(reflect), Vector2D.of(-1, 0), Vector2D.PLUS_Y);
        checkLine(diagonal.transform(reflect), Vector2D.ZERO, Vector2D.of(-1, 1).normalize());

        checkLine(horizontal.transform(translate), Vector2D.of(0, 5), Vector2D.PLUS_X);
        checkLine(vertical.transform(translate), Vector2D.of(4, 0), Vector2D.PLUS_Y);
        checkLine(diagonal.transform(translate), Vector2D.of(-0.5, 0.5), Vector2D.of(1, 1).normalize());

        checkLine(horizontal.transform(rotate), Vector2D.of(-1, 0), Vector2D.PLUS_Y);
        checkLine(vertical.transform(rotate), Vector2D.of(0, 1), Vector2D.MINUS_X);
        checkLine(diagonal.transform(rotate), Vector2D.ZERO, Vector2D.of(-1, 1).normalize());

        checkLine(horizontal.transform(rotateAroundPt), Vector2D.ZERO, Vector2D.PLUS_Y);
        checkLine(vertical.transform(rotateAroundPt), Vector2D.of(0, 2), Vector2D.MINUS_X);
        checkLine(diagonal.transform(rotateAroundPt), Vector2D.of(1, 1), Vector2D.of(-1, 1).normalize());
    }

    @Test
    public void testTransform_collapsedPoints() {
        // arrange
        AffineTransformMatrix2D scaleCollapse = AffineTransformMatrix2D.createScale(0, 1);
        Line_Old line = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            line.transform(scaleCollapse);
        }, GeometryValueException.class, "Line direction cannot be zero");
    }

    @Test
    public void testHashCode() {
        // arrange
        DoublePrecisionContext precision1 = new EpsilonDoublePrecisionContext(1e-4);
        DoublePrecisionContext precision2 = new EpsilonDoublePrecisionContext(1e-5);

        Vector2D p = Vector2D.of(1, 2);
        Vector2D v = Vector2D.of(1, 1);

        Line_Old a = Line_Old.fromPointAndDirection(p, v, precision1);
        Line_Old b = Line_Old.fromPointAndDirection(Vector2D.ZERO, v, precision1);
        Line_Old c = Line_Old.fromPointAndDirection(p, v.negate(), precision1);
        Line_Old d = Line_Old.fromPointAndDirection(p, v, precision2);
        Line_Old e = Line_Old.fromPointAndDirection(p, v, precision1);

        // act/assert
        int aHash = a.hashCode();

        Assert.assertEquals(aHash, a.hashCode());
        Assert.assertEquals(aHash, e.hashCode());

        Assert.assertNotEquals(aHash, b.hashCode());
        Assert.assertNotEquals(aHash, c.hashCode());
        Assert.assertNotEquals(aHash, d.hashCode());
    }

    @Test
    public void testEquals() {
     // arrange
        DoublePrecisionContext precision1 = new EpsilonDoublePrecisionContext(1e-4);
        DoublePrecisionContext precision2 = new EpsilonDoublePrecisionContext(1e-5);

        Vector2D p = Vector2D.of(1, 2);
        Vector2D v = Vector2D.of(1, 1);

        Line_Old a = Line_Old.fromPointAndDirection(p, v, precision1);
        Line_Old b = Line_Old.fromPointAndDirection(Vector2D.ZERO, v, precision1);
        Line_Old c = Line_Old.fromPointAndDirection(p, v.negate(), precision1);
        Line_Old d = Line_Old.fromPointAndDirection(p, v, precision2);
        Line_Old e = Line_Old.fromPointAndDirection(p, v, precision1);

        // act/assert
        Assert.assertTrue(a.equals(a));
        Assert.assertTrue(a.equals(e));
        Assert.assertTrue(e.equals(a));

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(a.equals(d));
    }

    @Test
    public void testToString() {
        // arrange
        Line_Old line = Line_Old.fromPointAndDirection(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);

        // act
        String str = line.toString();

        // assert
        Assert.assertTrue(str.contains("Line"));
        Assert.assertTrue(str.contains("origin= (0.0, 0.0)"));
        Assert.assertTrue(str.contains("direction= (1.0, 0.0)"));
    }

    @Test
    public void testLineTransform() {

        Line_Old l1 = Line_Old.fromPoints(Vector2D.of(1.0 ,1.0), Vector2D.of(4.0 ,1.0), TEST_PRECISION);
        Transform_Old<Vector2D, Vector1D> t1 =
            Line_Old.getTransform(Vector2D.of(0.0, 0.5), Vector2D.of(-1.0, 0.0), Vector2D.of(1.0, 1.5));
        Assert.assertEquals(0.5 * Math.PI,
                            ((Line_Old) t1.apply(l1)).getAngle(),
                            1.0e-10);

        Line_Old l2 = Line_Old.fromPoints(Vector2D.of(0.0, 0.0), Vector2D.of(1.0, 1.0), TEST_PRECISION);
        Transform_Old<Vector2D, Vector1D> t2 =
            Line_Old.getTransform(Vector2D.of(0.0, 0.5), Vector2D.of(-1.0, 0.0), Vector2D.of(1.0, 1.5));
        Assert.assertEquals(Math.atan2(1.0, -2.0),
                            ((Line_Old) t2.apply(l2)).getAngle(),
                            1.0e-10);

    }

    /**
     * Check that the line has the given defining properties.
     * @param line
     * @param origin
     * @param dir
     */
    private void checkLine(Line_Old line, Vector2D origin, Vector2D dir) {
        EuclideanTestUtils.assertCoordinatesEqual(origin, line.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(dir, line.getDirection(), TEST_EPS);
    }
}
