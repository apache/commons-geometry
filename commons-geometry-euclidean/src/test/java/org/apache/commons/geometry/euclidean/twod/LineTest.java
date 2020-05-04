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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.AffineTransformMatrix1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.twod.Line.SubspaceTransform;
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
        checkLine(Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION),
                Vector2D.ZERO, Vector2D.Unit.PLUS_X);
        checkLine(Lines.fromPoints(Vector2D.ZERO, Vector2D.of(100, 0), TEST_PRECISION),
                Vector2D.ZERO, Vector2D.Unit.PLUS_X);
        checkLine(Lines.fromPoints(Vector2D.of(100, 0), Vector2D.ZERO, TEST_PRECISION),
                Vector2D.ZERO, Vector2D.Unit.MINUS_X);
        checkLine(Lines.fromPoints(Vector2D.of(-100, 0), Vector2D.of(100, 0), TEST_PRECISION),
                Vector2D.ZERO, Vector2D.Unit.PLUS_X);

        checkLine(Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 2), TEST_PRECISION),
                Vector2D.of(-1, 1), Vector2D.of(1, 1).normalize());
        checkLine(Lines.fromPoints(Vector2D.of(0, 2), Vector2D.of(-2, 0), TEST_PRECISION),
                Vector2D.of(-1, 1), Vector2D.of(-1, -1).normalize());
    }

    @Test
    public void testFromPoints_pointsTooClose() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> Lines.fromPoints(Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_X, TEST_PRECISION),
                IllegalArgumentException.class, "Line direction cannot be zero");
        GeometryTestUtils.assertThrows(() -> Lines.fromPoints(Vector2D.Unit.PLUS_X, Vector2D.of(1 + 1e-11, 1e-11), TEST_PRECISION),
                IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPointAndDirection() {
        // act/assert
        checkLine(Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION),
                Vector2D.ZERO, Vector2D.Unit.PLUS_X);
        checkLine(Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(100, 0), TEST_PRECISION),
                Vector2D.ZERO, Vector2D.Unit.PLUS_X);
        checkLine(Lines.fromPointAndDirection(Vector2D.of(-100, 0), Vector2D.of(100, 0), TEST_PRECISION),
                Vector2D.ZERO, Vector2D.Unit.PLUS_X);

        checkLine(Lines.fromPointAndDirection(Vector2D.of(-2, 0), Vector2D.of(1, 1), TEST_PRECISION),
                Vector2D.of(-1, 1), Vector2D.of(1, 1).normalize());
        checkLine(Lines.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(-1, -1), TEST_PRECISION),
                Vector2D.of(-1, 1), Vector2D.of(-1, -1).normalize());
    }

    @Test
    public void testFromPointAndDirection_directionIsZero() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> Lines.fromPointAndDirection(Vector2D.Unit.PLUS_X, Vector2D.ZERO, TEST_PRECISION),
                IllegalArgumentException.class, "Line direction cannot be zero");
        GeometryTestUtils.assertThrows(() -> Lines.fromPointAndDirection(Vector2D.Unit.PLUS_X, Vector2D.of(1e-11, -1e-12), TEST_PRECISION),
                IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPointAndAngle() {
        // act/assert
        checkLine(Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION),
                Vector2D.ZERO, Vector2D.Unit.PLUS_X);
        checkLine(Lines.fromPointAndAngle(Vector2D.of(1, 1), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION),
                Vector2D.of(1, 0), Vector2D.Unit.PLUS_Y);
        checkLine(Lines.fromPointAndAngle(Vector2D.of(-1, -1), PlaneAngleRadians.PI, TEST_PRECISION),
                Vector2D.of(0, -1), Vector2D.Unit.MINUS_X);
        checkLine(Lines.fromPointAndAngle(Vector2D.of(1, -1), -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION),
                Vector2D.of(1, 0), Vector2D.Unit.MINUS_Y);
        checkLine(Lines.fromPointAndAngle(Vector2D.of(-1, 1), PlaneAngleRadians.TWO_PI, TEST_PRECISION),
                Vector2D.of(0, 1), Vector2D.Unit.PLUS_X);
    }

    @Test
    public void testGetAngle() {
        // arrange
        Vector2D vec = Vector2D.of(1, 2);

        for (double theta = -4 * PlaneAngleRadians.PI; theta < 2 * PlaneAngleRadians.PI; theta += 0.1) {
            Line line = Lines.fromPointAndAngle(vec, theta, TEST_PRECISION);

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
        Assert.assertEquals(0, Lines.fromPointAndAngle(vec, 0.0, TEST_PRECISION).getAngle(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, Lines.fromPointAndAngle(vec, PlaneAngleRadians.PI, TEST_PRECISION).getAngle(), TEST_EPS);
        Assert.assertEquals(0, Lines.fromPointAndAngle(vec, PlaneAngleRadians.TWO_PI, TEST_PRECISION).getAngle(), TEST_EPS);

        Assert.assertEquals(0, Lines.fromPointAndAngle(vec, -2 * PlaneAngleRadians.PI, TEST_PRECISION).getAngle(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, Lines.fromPointAndAngle(vec, -3 * PlaneAngleRadians.PI, TEST_PRECISION).getAngle(), TEST_EPS);
        Assert.assertEquals(0, Lines.fromPointAndAngle(vec, -4 * PlaneAngleRadians.TWO_PI, TEST_PRECISION).getAngle(), TEST_EPS);
    }

    @Test
    public void testGetDirection() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_X,
                Lines.fromPoints(Vector2D.of(0, 0), Vector2D.of(1, 0), TEST_PRECISION).getDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_Y,
                Lines.fromPoints(Vector2D.of(0, 1), Vector2D.of(0, -1), TEST_PRECISION).getDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_X,
                Lines.fromPoints(Vector2D.of(2, 2), Vector2D.of(1, 2), TEST_PRECISION).getDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_X,
                Lines.fromPoints(Vector2D.of(10, -2), Vector2D.of(10.1, -2), TEST_PRECISION).getDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_Y,
                Lines.fromPoints(Vector2D.of(3, 2), Vector2D.of(3, 1), TEST_PRECISION).getDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_Y,
                Lines.fromPoints(Vector2D.of(-3, 10), Vector2D.of(-3, 10.1), TEST_PRECISION).getDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -1).normalize(),
                Lines.fromPoints(Vector2D.of(0, 2), Vector2D.of(2, 0), TEST_PRECISION).getDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1).normalize(),
                Lines.fromPoints(Vector2D.of(2, 0), Vector2D.of(0, 2), TEST_PRECISION).getDirection(), TEST_EPS);
    }

    @Test
    public void testGetOffsetDirection() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_Y,
                Lines.fromPoints(Vector2D.of(0, 0), Vector2D.of(1, 0), TEST_PRECISION).getOffsetDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_X,
                Lines.fromPoints(Vector2D.of(0, 1), Vector2D.of(0, -1), TEST_PRECISION).getOffsetDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_Y,
                Lines.fromPoints(Vector2D.of(2, 2), Vector2D.of(1, 2), TEST_PRECISION).getOffsetDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_Y,
                Lines.fromPoints(Vector2D.of(10, -2), Vector2D.of(10.1, -2), TEST_PRECISION).getOffsetDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_X,
                Lines.fromPoints(Vector2D.of(3, 2), Vector2D.of(3, 1), TEST_PRECISION).getOffsetDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_X,
                Lines.fromPoints(Vector2D.of(-3, 10), Vector2D.of(-3, 10.1), TEST_PRECISION).getOffsetDirection(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, -1).normalize(),
                Lines.fromPoints(Vector2D.of(0, 2), Vector2D.of(2, 0), TEST_PRECISION).getOffsetDirection(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1).normalize(),
                Lines.fromPoints(Vector2D.of(2, 0), Vector2D.of(0, 2), TEST_PRECISION).getOffsetDirection(), TEST_EPS);
    }

    @Test
    public void testGetOrigin() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO,
                Lines.fromPoints(Vector2D.of(0, 0), Vector2D.of(1, 0), TEST_PRECISION).getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO,
                Lines.fromPoints(Vector2D.of(0, 1), Vector2D.of(0, -1), TEST_PRECISION).getOrigin(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2),
                Lines.fromPoints(Vector2D.of(2, 2), Vector2D.of(3, 2), TEST_PRECISION).getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -2),
                Lines.fromPoints(Vector2D.of(10, -2), Vector2D.of(10.1, -2), TEST_PRECISION).getOrigin(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 0),
                Lines.fromPoints(Vector2D.of(3, 2), Vector2D.of(3, 1), TEST_PRECISION).getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 0),
                Lines.fromPoints(Vector2D.of(-3, 10), Vector2D.of(-3, 10.1), TEST_PRECISION).getOrigin(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1),
                Lines.fromPoints(Vector2D.of(0, 2), Vector2D.of(2, 0), TEST_PRECISION).getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1),
                Lines.fromPoints(Vector2D.of(2, 0), Vector2D.of(0, 2), TEST_PRECISION).getOrigin(), TEST_EPS);
    }

    @Test
    public void testGetOriginOffset() {
        // arrange
        double sqrt2 = Math.sqrt(2);

        // act/assert
        Assert.assertEquals(0.0,
                Lines.fromPoints(Vector2D.of(0, 0), Vector2D.of(1, 1), TEST_PRECISION).getOriginOffset(), TEST_EPS);
        Assert.assertEquals(0.0,
                Lines.fromPoints(Vector2D.of(0, 0), Vector2D.of(-1, -1), TEST_PRECISION).getOriginOffset(), TEST_EPS);

        Assert.assertEquals(sqrt2,
                Lines.fromPoints(Vector2D.of(-1, 1), Vector2D.of(0, 2), TEST_PRECISION).getOriginOffset(), TEST_EPS);
        Assert.assertEquals(-sqrt2,
                Lines.fromPoints(Vector2D.of(0, -2), Vector2D.of(1, -1), TEST_PRECISION).getOriginOffset(), TEST_EPS);

        Assert.assertEquals(-sqrt2,
                Lines.fromPoints(Vector2D.of(0, 2), Vector2D.of(-1, 1), TEST_PRECISION).getOriginOffset(), TEST_EPS);
        Assert.assertEquals(sqrt2,
                Lines.fromPoints(Vector2D.of(1, -1), Vector2D.of(0, -2), TEST_PRECISION).getOriginOffset(), TEST_EPS);
    }

    @Test
    public void testGetPrecision() {
        // act/assert
        Assert.assertSame(TEST_PRECISION, Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).getPrecision());
        Assert.assertSame(TEST_PRECISION, Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).getPrecision());
        Assert.assertSame(TEST_PRECISION, Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION).getPrecision());
    }

    @Test
    public void testReverse() {
        // arrange
        Vector2D pt = Vector2D.of(0, 1);
        Vector2D dir = Vector2D.Unit.PLUS_X;
        Line line = Lines.fromPointAndDirection(pt, dir, TEST_PRECISION);

        // act
        Line reversed = line.reverse();
        Line doubleReversed = reversed.reverse();

        // assert
        checkLine(reversed, pt, dir.negate());
        Assert.assertEquals(-1, reversed.getOriginOffset(), TEST_EPS);

        checkLine(doubleReversed, pt, dir);
        Assert.assertEquals(1, doubleReversed.getOriginOffset(), TEST_EPS);
    }

    @Test
    public void testAbscissa() {
        // arrange
        Line line = Lines.fromPoints(Vector2D.of(-2, -2), Vector2D.of(2, 1), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0.0, line.abscissa(Vector2D.of(-3, 4)), TEST_EPS);
        Assert.assertEquals(0.0, line.abscissa(Vector2D.of(3, -4)), TEST_EPS);
        Assert.assertEquals(5.0, line.abscissa(Vector2D.of(7, -1)), TEST_EPS);
        Assert.assertEquals(-5.0, line.abscissa(Vector2D.of(-1, -7)), TEST_EPS);
    }

    @Test
    public void testToSubspace() {
        // arrange
        Line line = Lines.fromPoints(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0.0, line.toSubspace(Vector2D.of(-3, 4)).getX(), TEST_EPS);
        Assert.assertEquals(0.0, line.toSubspace(Vector2D.of(3, -4)).getX(), TEST_EPS);
        Assert.assertEquals(-5.0, line.toSubspace(Vector2D.of(7, -1)).getX(), TEST_EPS);
        Assert.assertEquals(5.0, line.toSubspace(Vector2D.of(-1, -7)).getX(), TEST_EPS);
    }

    @Test
    public void testToSpace_throughOrigin() {
        // arrange
        double invSqrt2 = 1 / Math.sqrt(2);
        Vector2D dir = Vector2D.of(invSqrt2, invSqrt2);

        Line line = Lines.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, line.toSpace(Vector1D.of(0)), TEST_EPS);

        for (int i = 0; i < 100; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(dir.multiply(i), line.toSpace(Vector1D.of(i)), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(dir.multiply(-i), line.toSpace(Vector1D.of(-i)), TEST_EPS);
        }
    }

    @Test
    public void testToSpace_offsetFromOrigin() {
        // arrange
        double angle = PlaneAngleRadians.PI / 6;
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

        Line line = Lines.fromPointAndAngle(pt, angle, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(origin, line.toSpace(Vector1D.of(0)), TEST_EPS);

        for (int i = 0; i < 100; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(origin.add(dir.multiply(i)), line.toSpace(Vector1D.of(i)), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(origin.add(dir.multiply(-i)), line.toSpace(Vector1D.of(-i)), TEST_EPS);
        }
    }

    @Test
    public void testIntersection() {
        // arrange
        Line a = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        Line b = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        Line c = Lines.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);
        Line d = Lines.fromPointAndDirection(Vector2D.of(0, -1), Vector2D.of(2, -1), TEST_PRECISION);

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
        Line a = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        Line b = Lines.fromPointAndDirection(Vector2D.of(0, 1), Vector2D.Unit.PLUS_X, TEST_PRECISION);

        Line c = Lines.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);
        Line d = Lines.fromPointAndDirection(Vector2D.of(0, -1), Vector2D.of(2, 1), TEST_PRECISION);

        // act/assert
        Assert.assertNull(a.intersection(b));
        Assert.assertNull(b.intersection(a));

        Assert.assertNull(c.intersection(d));
        Assert.assertNull(d.intersection(c));
    }

    @Test
    public void testIntersection_coincident() {
        // arrange
        Line a = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        Line b = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        Line c = Lines.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);
        Line d = Lines.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);

        // act/assert
        Assert.assertNull(a.intersection(b));
        Assert.assertNull(b.intersection(a));

        Assert.assertNull(c.intersection(d));
        Assert.assertNull(d.intersection(c));
    }

    @Test
    public void testAngle() {
        // arrange
        Line a = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        Line b = Lines.fromPointAndAngle(Vector2D.of(1, 4), PlaneAngleRadians.PI, TEST_PRECISION);
        Line c = Lines.fromPointAndDirection(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0.0, a.angle(a), TEST_EPS);
        Assert.assertEquals(-PlaneAngleRadians.PI, a.angle(b), TEST_EPS);
        Assert.assertEquals(0.25 * PlaneAngleRadians.PI, a.angle(c), TEST_EPS);

        Assert.assertEquals(0.0, b.angle(b), TEST_EPS);
        Assert.assertEquals(-PlaneAngleRadians.PI, b.angle(a), TEST_EPS);
        Assert.assertEquals(-0.75 * PlaneAngleRadians.PI, b.angle(c), TEST_EPS);

        Assert.assertEquals(0.0, c.angle(c), TEST_EPS);
        Assert.assertEquals(-0.25 * PlaneAngleRadians.PI, c.angle(a), TEST_EPS);
        Assert.assertEquals(0.75 * PlaneAngleRadians.PI, c.angle(b), TEST_EPS);
    }

    @Test
    public void testProject() {
        // --- arrange
        Line xAxis = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        Line yAxis = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);

        double diagonalYIntercept = 1;
        Vector2D diagonalDir = Vector2D.of(1, 2);
        Line diagonal = Lines.fromPointAndDirection(Vector2D.of(0, diagonalYIntercept), diagonalDir, TEST_PRECISION);

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
    public void testSpan() {
        // arrange
        Line line = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        LineConvexSubset result = line.span();

        // assert
        Assert.assertSame(line, result.getHyperplane());
        Assert.assertSame(line, result.getLine());
    }

    @Test
    public void testSegment_doubles() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act
        Segment segment = line.segment(1, 2);

        // assert
        Assert.assertSame(line, segment.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), segment.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testSegment_pointsOnLine() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act
        Segment segment = line.segment(Vector2D.of(3, 1), Vector2D.of(2, 1));

        // assert
        Assert.assertSame(line, segment.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 1), segment.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testSegment_pointsProjectedOnLine() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act
        Segment segment = line.segment(Vector2D.of(-3, 2), Vector2D.of(2, -1));

        // assert
        Assert.assertSame(line, segment.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), segment.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testLineTo_pointOnLine() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        ReverseRay halfLine = line.reverseRayTo(Vector2D.of(-3, 1));

        // assert
        Assert.assertSame(line, halfLine.getLine());
        Assert.assertTrue(halfLine.isInfinite());
        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), halfLine.getEndPoint(), TEST_EPS);

        Assert.assertTrue(halfLine.contains(Vector2D.of(1, 1)));
        Assert.assertFalse(halfLine.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testLineTo_pointProjectedOnLine() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        ReverseRay halfLine = line.reverseRayTo(Vector2D.of(-3, 5));

        // assert
        Assert.assertSame(line, halfLine.getLine());
        Assert.assertTrue(halfLine.isInfinite());
        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), halfLine.getEndPoint(), TEST_EPS);

        Assert.assertTrue(halfLine.contains(Vector2D.of(1, 1)));
        Assert.assertFalse(halfLine.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testLineTo_double() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        ReverseRay halfLine = line.reverseRayTo(-1);

        // assert
        Assert.assertSame(line, halfLine.getLine());
        Assert.assertTrue(halfLine.isInfinite());
        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), halfLine.getEndPoint(), TEST_EPS);

        Assert.assertTrue(halfLine.contains(Vector2D.of(2, 1)));
        Assert.assertFalse(halfLine.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testRayFrom_pointOnLine() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Ray ray = line.rayFrom(Vector2D.of(-3, 1));

        // assert
        Assert.assertSame(line, ray.getLine());
        Assert.assertTrue(ray.isInfinite());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        Assert.assertFalse(ray.contains(Vector2D.of(1, 1)));
        Assert.assertTrue(ray.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testRayFrom_pointProjectedOnLine() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Ray ray = line.rayFrom(Vector2D.of(-3, 5));

        // assert
        Assert.assertSame(line, ray.getLine());
        Assert.assertTrue(ray.isInfinite());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        Assert.assertFalse(ray.contains(Vector2D.of(1, 1)));
        Assert.assertTrue(ray.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testRayFrom_double() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        Ray ray = line.rayFrom(-1);

        // assert
        Assert.assertSame(line, ray.getLine());
        Assert.assertTrue(ray.isInfinite());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        Assert.assertFalse(ray.contains(Vector2D.of(2, 1)));
        Assert.assertTrue(ray.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testOffset_parallelLines() {
        // arrange
        double dist = Math.sin(Math.atan2(2, 1));

        Line a = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line b = Lines.fromPoints(Vector2D.of(-3, 0), Vector2D.of(0, 6), TEST_PRECISION);
        Line c = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line d = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, -2), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(-dist, a.offset(b), TEST_EPS);
        Assert.assertEquals(dist, b.offset(a), TEST_EPS);

        Assert.assertEquals(dist, a.offset(c), TEST_EPS);
        Assert.assertEquals(-dist, c.offset(a), TEST_EPS);

        Assert.assertEquals(3 * dist, a.offset(d), TEST_EPS);
        Assert.assertEquals(3 * dist, d.offset(a), TEST_EPS);
    }

    @Test
    public void testOffset_coincidentLines() {
        // arrange
        Line a = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line b = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line c = b.reverse();

        // act/assert
        Assert.assertEquals(0, a.offset(a), TEST_EPS);

        Assert.assertEquals(0, a.offset(b), TEST_EPS);
        Assert.assertEquals(0, b.offset(a), TEST_EPS);

        Assert.assertEquals(0, a.offset(c), TEST_EPS);
        Assert.assertEquals(0, c.offset(a), TEST_EPS);
    }

    @Test
    public void testOffset_nonParallelLines() {
        // arrange
        Line a = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        Line b = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        Line c = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line d = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, 4), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0, a.offset(b), TEST_EPS);
        Assert.assertEquals(0, b.offset(a), TEST_EPS);

        Assert.assertEquals(0, a.offset(c), TEST_EPS);
        Assert.assertEquals(0, c.offset(a), TEST_EPS);

        Assert.assertEquals(0, a.offset(d), TEST_EPS);
        Assert.assertEquals(0, d.offset(a), TEST_EPS);
    }

    @Test
    public void testOffset_point() {
        // arrange
        Line line = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line reversed = line.reverse();

        // act/assert
        Assert.assertEquals(0.0, line.offset(Vector2D.of(-0.5, 1)), TEST_EPS);
        Assert.assertEquals(0.0, line.offset(Vector2D.of(-1.5, -1)), TEST_EPS);
        Assert.assertEquals(0.0, line.offset(Vector2D.of(0.5, 3)), TEST_EPS);

        double d = Math.sin(Math.atan2(2, 1));

        Assert.assertEquals(d, line.offset(Vector2D.ZERO), TEST_EPS);
        Assert.assertEquals(-d, line.offset(Vector2D.of(-1, 2)), TEST_EPS);

        Assert.assertEquals(-d, reversed.offset(Vector2D.ZERO), TEST_EPS);
        Assert.assertEquals(d, reversed.offset(Vector2D.of(-1, 2)), TEST_EPS);
    }

    @Test
    public void testOffset_point_permute() {
        // arrange
        Line line = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Vector2D lineOrigin = line.getOrigin();

        EuclideanTestUtils.permute(-5, 5, 0.5, (x, y) -> {
            Vector2D pt = Vector2D.of(x, y);

            // act
            double offset = line.offset(pt);

            // arrange
            Vector2D vec = lineOrigin.vectorTo(pt).reject(line.getDirection());
            double dot = vec.dot(line.getOffsetDirection());
            double expected = Math.signum(dot) * vec.norm();

            Assert.assertEquals(expected, offset, TEST_EPS);
        });
    }

    @Test
    public void testSimilarOrientation() {
        // arrange
        Line a = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        Line b = Lines.fromPointAndAngle(Vector2D.of(4, 5), 0.0, TEST_PRECISION);
        Line c = Lines.fromPointAndAngle(Vector2D.of(-1, -3), 0.4 * PlaneAngleRadians.PI, TEST_PRECISION);
        Line d = Lines.fromPointAndAngle(Vector2D.of(1, 0), -0.4 * PlaneAngleRadians.PI, TEST_PRECISION);

        Line e = Lines.fromPointAndAngle(Vector2D.of(6, -3), PlaneAngleRadians.PI, TEST_PRECISION);
        Line f = Lines.fromPointAndAngle(Vector2D.of(8, 5), 0.8 * PlaneAngleRadians.PI, TEST_PRECISION);
        Line g = Lines.fromPointAndAngle(Vector2D.of(6, -3), -0.8 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.similarOrientation(a));
        Assert.assertTrue(a.similarOrientation(b));
        Assert.assertTrue(b.similarOrientation(a));
        Assert.assertTrue(a.similarOrientation(c));
        Assert.assertTrue(c.similarOrientation(a));
        Assert.assertTrue(a.similarOrientation(d));
        Assert.assertTrue(d.similarOrientation(a));

        Assert.assertFalse(c.similarOrientation(d));
        Assert.assertFalse(d.similarOrientation(c));

        Assert.assertTrue(e.similarOrientation(f));
        Assert.assertTrue(f.similarOrientation(e));
        Assert.assertTrue(e.similarOrientation(g));
        Assert.assertTrue(g.similarOrientation(e));

        Assert.assertFalse(a.similarOrientation(e));
        Assert.assertFalse(e.similarOrientation(a));
    }

    @Test
    public void testSimilarOrientation_orthogonal() {
        // arrange
        Line a = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        Line b = Lines.fromPointAndDirection(Vector2D.of(4, 5), Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        Line c = Lines.fromPointAndDirection(Vector2D.of(-4, -5), Vector2D.Unit.MINUS_Y, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.similarOrientation(b));
        Assert.assertTrue(b.similarOrientation(a));
        Assert.assertTrue(a.similarOrientation(c));
        Assert.assertTrue(c.similarOrientation(a));
    }

    @Test
    public void testDistance_parallelLines() {
        // arrange
        double dist = Math.sin(Math.atan2(2, 1));

        Line a = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line b = Lines.fromPoints(Vector2D.of(-3, 0), Vector2D.of(0, 6), TEST_PRECISION);
        Line c = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line d = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, -2), TEST_PRECISION);

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
        Line a = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line b = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        Line c = b.reverse();

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
        Line a = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        Line b = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        Line c = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line d = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, 4), TEST_PRECISION);

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
        Line line = Lines.fromPoints(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);

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
        Line line = Lines.fromPointAndDirection(origin, Vector2D.of(1, 1), TEST_PRECISION);

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
        Line line = Lines.fromPoints(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);

        for (double abscissa = -2.0; abscissa < 2.0; abscissa += 0.2) {
            for (double offset = -2.0; offset < 2.0; offset += 0.2) {

                // act
                Vector2D point = line.pointAt(abscissa, offset);

                // assert
                Assert.assertEquals(abscissa, line.toSubspace(point).getX(), TEST_EPS);
                Assert.assertEquals(offset, line.offset(point), TEST_EPS);
            }
        }
    }

    @Test
    public void testContains_line() {
        // arrange
        Vector2D pt = Vector2D.of(1, 2);
        Vector2D dir = Vector2D.of(3, 7);
        Line a = Lines.fromPointAndDirection(pt, dir, TEST_PRECISION);
        Line b = Lines.fromPointAndDirection(Vector2D.of(0, -4), dir, TEST_PRECISION);
        Line c = Lines.fromPointAndDirection(Vector2D.of(-2, -2), dir.negate(), TEST_PRECISION);
        Line d = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        Line e = Lines.fromPointAndDirection(pt, dir, TEST_PRECISION);
        Line f = Lines.fromPointAndDirection(pt, dir.negate(), TEST_PRECISION);

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

        Line line = Lines.fromPointAndAngle(p, 0.0, precision);

        // act/assert
        Vector2D offset1 = Vector2D.of(0, 1e-4);
        Vector2D offset2 = Vector2D.of(0, 2e-3);

        Assert.assertTrue(line.contains(Lines.fromPointAndAngle(p.add(offset1), 0.0, precision)));
        Assert.assertTrue(line.contains(Lines.fromPointAndAngle(p.subtract(offset1), 0.0, precision)));

        Assert.assertFalse(line.contains(Lines.fromPointAndAngle(p.add(offset2), 0.0, precision)));
        Assert.assertFalse(line.contains(Lines.fromPointAndAngle(p.subtract(offset2), 0.0, precision)));

        Assert.assertTrue(line.contains(Lines.fromPointAndAngle(p, 1e-4, precision)));
        Assert.assertFalse(line.contains(Lines.fromPointAndAngle(p, 1e-2, precision)));
    }

    @Test
    public void testContains_point() {
        // arrange
        Vector2D p1 = Vector2D.of(-1, 0);
        Vector2D p2 = Vector2D.of(0, 2);
        Line line = Lines.fromPoints(p1, p2, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(line.contains(p1));
        Assert.assertTrue(line.contains(p2));

        Assert.assertFalse(line.contains(Vector2D.ZERO));
        Assert.assertFalse(line.contains(Vector2D.of(100, 79)));

        Vector2D offset1 = Vector2D.of(0.1, 0);
        Vector2D offset2 = Vector2D.of(0, -0.1);
        Vector2D v;
        for (double t = -2; t <= 2; t += 0.1) {
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

        Line line = Lines.fromPoints(p1, p2, precision);
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
        Line line = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        Line reversed = line.reverse();

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
        Line line = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
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
        Line a = Lines.fromPointAndDirection(Vector2D.of(1, 2), dir, TEST_PRECISION);
        Line b = Lines.fromPointAndDirection(Vector2D.of(0, -4), dir, TEST_PRECISION);
        Line c = Lines.fromPointAndDirection(Vector2D.of(-2, -2), dir.negate(), TEST_PRECISION);
        Line d = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

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

        Line line = Lines.fromPointAndAngle(p1, 0.0, precision);

        // act/assert
        Assert.assertTrue(line.isParallel(Lines.fromPointAndAngle(p2, 1e-4, precision)));
        Assert.assertFalse(line.isParallel(Lines.fromPointAndAngle(p2, 1e-2, precision)));
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix2D scale = AffineTransformMatrix2D.createScale(2, 3);
        AffineTransformMatrix2D reflect = AffineTransformMatrix2D.createScale(-1, 1);
        AffineTransformMatrix2D translate = AffineTransformMatrix2D.createTranslation(3, 4);
        AffineTransformMatrix2D rotate = AffineTransformMatrix2D.createRotation(PlaneAngleRadians.PI_OVER_TWO);
        AffineTransformMatrix2D rotateAroundPt = AffineTransformMatrix2D.createRotation(Vector2D.of(0, 1), PlaneAngleRadians.PI_OVER_TWO);

        Vector2D p1 = Vector2D.of(0, 1);
        Vector2D p2 = Vector2D.of(1, 0);

        Line horizontal = Lines.fromPointAndDirection(p1, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        Line vertical = Lines.fromPointAndDirection(p2, Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        Line diagonal = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act/assert
        Assert.assertSame(TEST_PRECISION, horizontal.transform(scale).getPrecision());

        checkLine(horizontal.transform(scale), Vector2D.of(0, 3), Vector2D.Unit.PLUS_X);
        checkLine(vertical.transform(scale), Vector2D.of(2, 0), Vector2D.Unit.PLUS_Y);
        checkLine(diagonal.transform(scale), Vector2D.ZERO, Vector2D.of(2, 3).normalize());

        checkLine(horizontal.transform(reflect), p1, Vector2D.Unit.MINUS_X);
        checkLine(vertical.transform(reflect), Vector2D.of(-1, 0), Vector2D.Unit.PLUS_Y);
        checkLine(diagonal.transform(reflect), Vector2D.ZERO, Vector2D.of(-1, 1).normalize());

        checkLine(horizontal.transform(translate), Vector2D.of(0, 5), Vector2D.Unit.PLUS_X);
        checkLine(vertical.transform(translate), Vector2D.of(4, 0), Vector2D.Unit.PLUS_Y);
        checkLine(diagonal.transform(translate), Vector2D.of(-0.5, 0.5), Vector2D.of(1, 1).normalize());

        checkLine(horizontal.transform(rotate), Vector2D.of(-1, 0), Vector2D.Unit.PLUS_Y);
        checkLine(vertical.transform(rotate), Vector2D.of(0, 1), Vector2D.Unit.MINUS_X);
        checkLine(diagonal.transform(rotate), Vector2D.ZERO, Vector2D.of(-1, 1).normalize());

        checkLine(horizontal.transform(rotateAroundPt), Vector2D.ZERO, Vector2D.Unit.PLUS_Y);
        checkLine(vertical.transform(rotateAroundPt), Vector2D.of(0, 2), Vector2D.Unit.MINUS_X);
        checkLine(diagonal.transform(rotateAroundPt), Vector2D.of(1, 1), Vector2D.of(-1, 1).normalize());
    }

    @Test
    public void testTransform_collapsedPoints() {
        // arrange
        AffineTransformMatrix2D scaleCollapse = AffineTransformMatrix2D.createScale(0, 1);
        Line line = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            line.transform(scaleCollapse);
        }, IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testSubspaceTransform() {
        // arrange
        Line line = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION);

        // act/assert
        checkSubspaceTransform(line.subspaceTransform(AffineTransformMatrix2D.createScale(2, 3)),
                Vector2D.of(2, 0), Vector2D.Unit.PLUS_Y,
                Vector2D.of(2, 0), Vector2D.of(2, 3));

        checkSubspaceTransform(line.subspaceTransform(AffineTransformMatrix2D.createTranslation(2, 3)),
                Vector2D.of(3, 0), Vector2D.Unit.PLUS_Y,
                Vector2D.of(3, 3), Vector2D.of(3, 4));

        checkSubspaceTransform(line.subspaceTransform(AffineTransformMatrix2D.createRotation(PlaneAngleRadians.PI_OVER_TWO)),
                Vector2D.of(0, 1), Vector2D.Unit.MINUS_X,
                Vector2D.of(0, 1), Vector2D.of(-1, 1));
    }

    private void checkSubspaceTransform(SubspaceTransform st, Vector2D origin, Vector2D dir, Vector2D tZero, Vector2D tOne) {

        Line line = st.getLine();
        AffineTransformMatrix1D transform = st.getTransform();

        checkLine(line, origin, dir);

        EuclideanTestUtils.assertCoordinatesEqual(tZero, line.toSpace(transform.apply(Vector1D.ZERO)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(tOne, line.toSpace(transform.apply(Vector1D.Unit.PLUS)), TEST_EPS);
    }

    @Test
    public void testSubspaceTransform_transformsPointsCorrectly() {
        // arrange
        Line line = Lines.fromPointAndDirection(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION);

        EuclideanTestUtils.permuteSkipZero(-2, 2, 0.5, (a, b) -> {
            // create a somewhat complicate transform to try to hit all of the edge cases
            AffineTransformMatrix2D transform = AffineTransformMatrix2D.createTranslation(Vector2D.of(a, b))
                    .rotate(a * b)
                    .scale(0.1, 4);

            // act
            SubspaceTransform st = line.subspaceTransform(transform);

            // assert
            for (double x = -5.0; x <= 5.0; x += 1) {
                Vector1D subPt = Vector1D.of(x);
                Vector2D expected = transform.apply(line.toSpace(subPt));
                Vector2D actual = st.getLine().toSpace(
                        st.getTransform().apply(subPt));

                EuclideanTestUtils.assertCoordinatesEqual(expected, actual, TEST_EPS);
            }
        });
    }

    @Test
    public void testEq() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        Vector2D p = Vector2D.of(1, 2);
        double angle = 1.0;

        Line a = Lines.fromPointAndAngle(p, angle, precision);
        Line b = Lines.fromPointAndAngle(Vector2D.ZERO, angle, precision);
        Line c = Lines.fromPointAndAngle(p, angle + 1.0, precision);

        Line d = Lines.fromPointAndAngle(p, angle, precision);
        Line e = Lines.fromPointAndAngle(p.add(Vector2D.of(1e-4, 1e-4)), angle, precision);
        Line f = Lines.fromPointAndAngle(p, angle + 1e-4, precision);

        // act/assert
        Assert.assertTrue(a.eq(a, precision));

        Assert.assertTrue(a.eq(d, precision));
        Assert.assertTrue(d.eq(a, precision));

        Assert.assertTrue(a.eq(e, precision));
        Assert.assertTrue(e.eq(a, precision));

        Assert.assertTrue(a.eq(f, precision));
        Assert.assertTrue(f.eq(a, precision));

        Assert.assertFalse(a.eq(b, precision));
        Assert.assertFalse(a.eq(c, precision));
    }

    @Test
    public void testHashCode() {
        // arrange
        DoublePrecisionContext precision1 = new EpsilonDoublePrecisionContext(1e-4);
        DoublePrecisionContext precision2 = new EpsilonDoublePrecisionContext(1e-5);

        Vector2D p = Vector2D.of(1, 2);
        Vector2D v = Vector2D.of(1, 1);

        Line a = Lines.fromPointAndDirection(p, v, precision1);
        Line b = Lines.fromPointAndDirection(Vector2D.ZERO, v, precision1);
        Line c = Lines.fromPointAndDirection(p, v.negate(), precision1);
        Line d = Lines.fromPointAndDirection(p, v, precision2);
        Line e = Lines.fromPointAndDirection(p, v, precision1);

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

        Line a = Lines.fromPointAndDirection(p, v, precision1);
        Line b = Lines.fromPointAndDirection(Vector2D.ZERO, v, precision1);
        Line c = Lines.fromPointAndDirection(p, v.negate(), precision1);
        Line d = Lines.fromPointAndDirection(p, v, precision2);
        Line e = Lines.fromPointAndDirection(p, v, precision1);

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
        Line line = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        String str = line.toString();

        // assert
        Assert.assertTrue(str.contains("Line"));
        Assert.assertTrue(str.contains("origin= (0.0, 0.0)"));
        Assert.assertTrue(str.contains("direction= (1.0, 0.0)"));
    }

    /**
     * Check that the line has the given defining properties.
     * @param line
     * @param origin
     * @param dir
     */
    private void checkLine(Line line, Vector2D origin, Vector2D dir) {
        EuclideanTestUtils.assertCoordinatesEqual(origin, line.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(dir, line.getDirection(), TEST_EPS);
    }
}
