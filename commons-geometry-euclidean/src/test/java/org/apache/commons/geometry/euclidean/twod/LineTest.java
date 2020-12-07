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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThrows(IllegalArgumentException.class, () -> Lines.fromPoints(Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_X, TEST_PRECISION),  "Line direction cannot be zero");
        assertThrows(IllegalArgumentException.class, () -> Lines.fromPoints(Vector2D.Unit.PLUS_X, Vector2D.of(1 + 1e-11, 1e-11), TEST_PRECISION),  "Line direction cannot be zero");
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
        assertThrows(IllegalArgumentException.class, () -> Lines.fromPointAndDirection(Vector2D.Unit.PLUS_X, Vector2D.ZERO, TEST_PRECISION),  "Line direction cannot be zero");
        assertThrows(IllegalArgumentException.class, () -> Lines.fromPointAndDirection(Vector2D.Unit.PLUS_X, Vector2D.of(1e-11, -1e-12), TEST_PRECISION),  "Line direction cannot be zero");
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
        final Vector2D vec = Vector2D.of(1, 2);

        for (double theta = -4 * PlaneAngleRadians.PI; theta < 2 * PlaneAngleRadians.PI; theta += 0.1) {
            final Line line = Lines.fromPointAndAngle(vec, theta, TEST_PRECISION);

            // act/assert
            Assertions.assertEquals(PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(theta),
                    line.getAngle(), TEST_EPS);
        }
    }

    @Test
    public void testGetAngle_multiplesOfPi() {
        // arrange
        final Vector2D vec = Vector2D.of(-1, -2);

        // act/assert
        Assertions.assertEquals(0, Lines.fromPointAndAngle(vec, 0.0, TEST_PRECISION).getAngle(), TEST_EPS);
        Assertions.assertEquals(PlaneAngleRadians.PI, Lines.fromPointAndAngle(vec, PlaneAngleRadians.PI, TEST_PRECISION).getAngle(), TEST_EPS);
        Assertions.assertEquals(0, Lines.fromPointAndAngle(vec, PlaneAngleRadians.TWO_PI, TEST_PRECISION).getAngle(), TEST_EPS);

        Assertions.assertEquals(0, Lines.fromPointAndAngle(vec, -2 * PlaneAngleRadians.PI, TEST_PRECISION).getAngle(), TEST_EPS);
        Assertions.assertEquals(PlaneAngleRadians.PI, Lines.fromPointAndAngle(vec, -3 * PlaneAngleRadians.PI, TEST_PRECISION).getAngle(), TEST_EPS);
        Assertions.assertEquals(0, Lines.fromPointAndAngle(vec, -4 * PlaneAngleRadians.TWO_PI, TEST_PRECISION).getAngle(), TEST_EPS);
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
        final double sqrt2 = Math.sqrt(2);

        // act/assert
        Assertions.assertEquals(0.0,
                Lines.fromPoints(Vector2D.of(0, 0), Vector2D.of(1, 1), TEST_PRECISION).getOriginOffset(), TEST_EPS);
        Assertions.assertEquals(0.0,
                Lines.fromPoints(Vector2D.of(0, 0), Vector2D.of(-1, -1), TEST_PRECISION).getOriginOffset(), TEST_EPS);

        Assertions.assertEquals(sqrt2,
                Lines.fromPoints(Vector2D.of(-1, 1), Vector2D.of(0, 2), TEST_PRECISION).getOriginOffset(), TEST_EPS);
        Assertions.assertEquals(-sqrt2,
                Lines.fromPoints(Vector2D.of(0, -2), Vector2D.of(1, -1), TEST_PRECISION).getOriginOffset(), TEST_EPS);

        Assertions.assertEquals(-sqrt2,
                Lines.fromPoints(Vector2D.of(0, 2), Vector2D.of(-1, 1), TEST_PRECISION).getOriginOffset(), TEST_EPS);
        Assertions.assertEquals(sqrt2,
                Lines.fromPoints(Vector2D.of(1, -1), Vector2D.of(0, -2), TEST_PRECISION).getOriginOffset(), TEST_EPS);
    }

    @Test
    public void testGetPrecision() {
        // act/assert
        Assertions.assertSame(TEST_PRECISION, Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).getPrecision());
        Assertions.assertSame(TEST_PRECISION, Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).getPrecision());
        Assertions.assertSame(TEST_PRECISION, Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION).getPrecision());
    }

    @Test
    public void testReverse() {
        // arrange
        final Vector2D pt = Vector2D.of(0, 1);
        final Vector2D dir = Vector2D.Unit.PLUS_X;
        final Line line = Lines.fromPointAndDirection(pt, dir, TEST_PRECISION);

        // act
        final Line reversed = line.reverse();
        final Line doubleReversed = reversed.reverse();

        // assert
        checkLine(reversed, pt, dir.negate());
        Assertions.assertEquals(-1, reversed.getOriginOffset(), TEST_EPS);

        checkLine(doubleReversed, pt, dir);
        Assertions.assertEquals(1, doubleReversed.getOriginOffset(), TEST_EPS);
    }

    @Test
    public void testAbscissa() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.of(-2, -2), Vector2D.of(2, 1), TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(0.0, line.abscissa(Vector2D.of(-3, 4)), TEST_EPS);
        Assertions.assertEquals(0.0, line.abscissa(Vector2D.of(3, -4)), TEST_EPS);
        Assertions.assertEquals(5.0, line.abscissa(Vector2D.of(7, -1)), TEST_EPS);
        Assertions.assertEquals(-5.0, line.abscissa(Vector2D.of(-1, -7)), TEST_EPS);
    }

    @Test
    public void testToSubspace() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(0.0, line.toSubspace(Vector2D.of(-3, 4)).getX(), TEST_EPS);
        Assertions.assertEquals(0.0, line.toSubspace(Vector2D.of(3, -4)).getX(), TEST_EPS);
        Assertions.assertEquals(-5.0, line.toSubspace(Vector2D.of(7, -1)).getX(), TEST_EPS);
        Assertions.assertEquals(5.0, line.toSubspace(Vector2D.of(-1, -7)).getX(), TEST_EPS);
    }

    @Test
    public void testToSpace_throughOrigin() {
        // arrange
        final double invSqrt2 = 1 / Math.sqrt(2);
        final Vector2D dir = Vector2D.of(invSqrt2, invSqrt2);

        final Line line = Lines.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

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
        final double angle = PlaneAngleRadians.PI / 6;
        final double cos = Math.cos(angle);
        final double sin = Math.sin(angle);
        final Vector2D pt = Vector2D.of(-5, 0);

        final double h = Math.abs(pt.getX()) * cos;
        final double d = h * cos;
        final Vector2D origin = Vector2D.of(
                    pt.getX() + d,
                    h * sin
                );
        final Vector2D dir = Vector2D.of(cos, sin);

        final Line line = Lines.fromPointAndAngle(pt, angle, TEST_PRECISION);

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
        final Line a = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Line b = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        final Line c = Lines.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);
        final Line d = Lines.fromPointAndDirection(Vector2D.of(0, -1), Vector2D.of(2, -1), TEST_PRECISION);

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
        final Line a = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Line b = Lines.fromPointAndDirection(Vector2D.of(0, 1), Vector2D.Unit.PLUS_X, TEST_PRECISION);

        final Line c = Lines.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);
        final Line d = Lines.fromPointAndDirection(Vector2D.of(0, -1), Vector2D.of(2, 1), TEST_PRECISION);

        // act/assert
        Assertions.assertNull(a.intersection(b));
        Assertions.assertNull(b.intersection(a));

        Assertions.assertNull(c.intersection(d));
        Assertions.assertNull(d.intersection(c));
    }

    @Test
    public void testIntersection_coincident() {
        // arrange
        final Line a = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Line b = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        final Line c = Lines.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);
        final Line d = Lines.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(2, 1), TEST_PRECISION);

        // act/assert
        Assertions.assertNull(a.intersection(b));
        Assertions.assertNull(b.intersection(a));

        Assertions.assertNull(c.intersection(d));
        Assertions.assertNull(d.intersection(c));
    }

    @Test
    public void testAngle() {
        // arrange
        final Line a = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        final Line b = Lines.fromPointAndAngle(Vector2D.of(1, 4), PlaneAngleRadians.PI, TEST_PRECISION);
        final Line c = Lines.fromPointAndDirection(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(0.0, a.angle(a), TEST_EPS);
        Assertions.assertEquals(-PlaneAngleRadians.PI, a.angle(b), TEST_EPS);
        Assertions.assertEquals(0.25 * PlaneAngleRadians.PI, a.angle(c), TEST_EPS);

        Assertions.assertEquals(0.0, b.angle(b), TEST_EPS);
        Assertions.assertEquals(-PlaneAngleRadians.PI, b.angle(a), TEST_EPS);
        Assertions.assertEquals(-0.75 * PlaneAngleRadians.PI, b.angle(c), TEST_EPS);

        Assertions.assertEquals(0.0, c.angle(c), TEST_EPS);
        Assertions.assertEquals(-0.25 * PlaneAngleRadians.PI, c.angle(a), TEST_EPS);
        Assertions.assertEquals(0.75 * PlaneAngleRadians.PI, c.angle(b), TEST_EPS);
    }

    @Test
    public void testProject() {
        // --- arrange
        final Line xAxis = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Line yAxis = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);

        final double diagonalYIntercept = 1;
        final Vector2D diagonalDir = Vector2D.of(1, 2);
        final Line diagonal = Lines.fromPointAndDirection(Vector2D.of(0, diagonalYIntercept), diagonalDir, TEST_PRECISION);

        EuclideanTestUtils.permute(-5, 5, 0.5, (x, y) -> {
            final Vector2D pt = Vector2D.of(x, y);

            // --- act/assert
            EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(x, 0), xAxis.project(pt), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, y), yAxis.project(pt), TEST_EPS);

            final Vector2D diagonalPt = diagonal.project(pt);
            Assertions.assertTrue(diagonal.contains(diagonalPt));
            Assertions.assertEquals(diagonal.distance(pt), pt.distance(diagonalPt), TEST_EPS);

            // check that y = mx + b is true
            Assertions.assertEquals(diagonalPt.getY(),
                    (diagonalDir.getY() * diagonalPt.getX() / diagonalDir.getX()) + diagonalYIntercept, TEST_EPS);
        });
    }

    @Test
    public void testSpan() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final LineConvexSubset result = line.span();

        // assert
        Assertions.assertSame(line, result.getHyperplane());
        Assertions.assertSame(line, result.getLine());
    }

    @Test
    public void testSegment_doubles() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act
        final Segment segment = line.segment(1, 2);

        // assert
        Assertions.assertSame(line, segment.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), segment.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testSegment_pointsOnLine() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act
        final Segment segment = line.segment(Vector2D.of(3, 1), Vector2D.of(2, 1));

        // assert
        Assertions.assertSame(line, segment.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 1), segment.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testSegment_pointsProjectedOnLine() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        // act
        final Segment segment = line.segment(Vector2D.of(-3, 2), Vector2D.of(2, -1));

        // assert
        Assertions.assertSame(line, segment.getLine());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), segment.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testLineTo_pointOnLine() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final ReverseRay halfLine = line.reverseRayTo(Vector2D.of(-3, 1));

        // assert
        Assertions.assertSame(line, halfLine.getLine());
        Assertions.assertTrue(halfLine.isInfinite());
        Assertions.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), halfLine.getEndPoint(), TEST_EPS);

        Assertions.assertTrue(halfLine.contains(Vector2D.of(1, 1)));
        Assertions.assertFalse(halfLine.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testLineTo_pointProjectedOnLine() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final ReverseRay halfLine = line.reverseRayTo(Vector2D.of(-3, 5));

        // assert
        Assertions.assertSame(line, halfLine.getLine());
        Assertions.assertTrue(halfLine.isInfinite());
        Assertions.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), halfLine.getEndPoint(), TEST_EPS);

        Assertions.assertTrue(halfLine.contains(Vector2D.of(1, 1)));
        Assertions.assertFalse(halfLine.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testLineTo_double() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final ReverseRay halfLine = line.reverseRayTo(-1);

        // assert
        Assertions.assertSame(line, halfLine.getLine());
        Assertions.assertTrue(halfLine.isInfinite());
        Assertions.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), halfLine.getEndPoint(), TEST_EPS);

        Assertions.assertTrue(halfLine.contains(Vector2D.of(2, 1)));
        Assertions.assertFalse(halfLine.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testRayFrom_pointOnLine() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Ray ray = line.rayFrom(Vector2D.of(-3, 1));

        // assert
        Assertions.assertSame(line, ray.getLine());
        Assertions.assertTrue(ray.isInfinite());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), ray.getStartPoint(), TEST_EPS);
        Assertions.assertNull(ray.getEndPoint());

        Assertions.assertFalse(ray.contains(Vector2D.of(1, 1)));
        Assertions.assertTrue(ray.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testRayFrom_pointProjectedOnLine() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Ray ray = line.rayFrom(Vector2D.of(-3, 5));

        // assert
        Assertions.assertSame(line, ray.getLine());
        Assertions.assertTrue(ray.isInfinite());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 1), ray.getStartPoint(), TEST_EPS);
        Assertions.assertNull(ray.getEndPoint());

        Assertions.assertFalse(ray.contains(Vector2D.of(1, 1)));
        Assertions.assertTrue(ray.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testRayFrom_double() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Ray ray = line.rayFrom(-1);

        // assert
        Assertions.assertSame(line, ray.getLine());
        Assertions.assertTrue(ray.isInfinite());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), ray.getStartPoint(), TEST_EPS);
        Assertions.assertNull(ray.getEndPoint());

        Assertions.assertFalse(ray.contains(Vector2D.of(2, 1)));
        Assertions.assertTrue(ray.contains(Vector2D.of(-4, 1)));
    }

    @Test
    public void testOffset_parallelLines() {
        // arrange
        final double dist = Math.sin(Math.atan2(2, 1));

        final Line a = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        final Line b = Lines.fromPoints(Vector2D.of(-3, 0), Vector2D.of(0, 6), TEST_PRECISION);
        final Line c = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        final Line d = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, -2), TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(-dist, a.offset(b), TEST_EPS);
        Assertions.assertEquals(dist, b.offset(a), TEST_EPS);

        Assertions.assertEquals(dist, a.offset(c), TEST_EPS);
        Assertions.assertEquals(-dist, c.offset(a), TEST_EPS);

        Assertions.assertEquals(3 * dist, a.offset(d), TEST_EPS);
        Assertions.assertEquals(3 * dist, d.offset(a), TEST_EPS);
    }

    @Test
    public void testOffset_coincidentLines() {
        // arrange
        final Line a = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        final Line b = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        final Line c = b.reverse();

        // act/assert
        Assertions.assertEquals(0, a.offset(a), TEST_EPS);

        Assertions.assertEquals(0, a.offset(b), TEST_EPS);
        Assertions.assertEquals(0, b.offset(a), TEST_EPS);

        Assertions.assertEquals(0, a.offset(c), TEST_EPS);
        Assertions.assertEquals(0, c.offset(a), TEST_EPS);
    }

    @Test
    public void testOffset_nonParallelLines() {
        // arrange
        final Line a = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Line b = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        final Line c = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        final Line d = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, 4), TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(0, a.offset(b), TEST_EPS);
        Assertions.assertEquals(0, b.offset(a), TEST_EPS);

        Assertions.assertEquals(0, a.offset(c), TEST_EPS);
        Assertions.assertEquals(0, c.offset(a), TEST_EPS);

        Assertions.assertEquals(0, a.offset(d), TEST_EPS);
        Assertions.assertEquals(0, d.offset(a), TEST_EPS);
    }

    @Test
    public void testOffset_point() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        final Line reversed = line.reverse();

        // act/assert
        Assertions.assertEquals(0.0, line.offset(Vector2D.of(-0.5, 1)), TEST_EPS);
        Assertions.assertEquals(0.0, line.offset(Vector2D.of(-1.5, -1)), TEST_EPS);
        Assertions.assertEquals(0.0, line.offset(Vector2D.of(0.5, 3)), TEST_EPS);

        final double d = Math.sin(Math.atan2(2, 1));

        Assertions.assertEquals(d, line.offset(Vector2D.ZERO), TEST_EPS);
        Assertions.assertEquals(-d, line.offset(Vector2D.of(-1, 2)), TEST_EPS);

        Assertions.assertEquals(-d, reversed.offset(Vector2D.ZERO), TEST_EPS);
        Assertions.assertEquals(d, reversed.offset(Vector2D.of(-1, 2)), TEST_EPS);
    }

    @Test
    public void testOffset_point_permute() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        final Vector2D lineOrigin = line.getOrigin();

        EuclideanTestUtils.permute(-5, 5, 0.5, (x, y) -> {
            final Vector2D pt = Vector2D.of(x, y);

            // act
            final double offset = line.offset(pt);

            // arrange
            final Vector2D vec = lineOrigin.vectorTo(pt).reject(line.getDirection());
            final double dot = vec.dot(line.getOffsetDirection());
            final double expected = Math.signum(dot) * vec.norm();

            Assertions.assertEquals(expected, offset, TEST_EPS);
        });
    }

    @Test
    public void testSimilarOrientation() {
        // arrange
        final Line a = Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION);
        final Line b = Lines.fromPointAndAngle(Vector2D.of(4, 5), 0.0, TEST_PRECISION);
        final Line c = Lines.fromPointAndAngle(Vector2D.of(-1, -3), 0.4 * PlaneAngleRadians.PI, TEST_PRECISION);
        final Line d = Lines.fromPointAndAngle(Vector2D.of(1, 0), -0.4 * PlaneAngleRadians.PI, TEST_PRECISION);

        final Line e = Lines.fromPointAndAngle(Vector2D.of(6, -3), PlaneAngleRadians.PI, TEST_PRECISION);
        final Line f = Lines.fromPointAndAngle(Vector2D.of(8, 5), 0.8 * PlaneAngleRadians.PI, TEST_PRECISION);
        final Line g = Lines.fromPointAndAngle(Vector2D.of(6, -3), -0.8 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(a.similarOrientation(a));
        Assertions.assertTrue(a.similarOrientation(b));
        Assertions.assertTrue(b.similarOrientation(a));
        Assertions.assertTrue(a.similarOrientation(c));
        Assertions.assertTrue(c.similarOrientation(a));
        Assertions.assertTrue(a.similarOrientation(d));
        Assertions.assertTrue(d.similarOrientation(a));

        Assertions.assertFalse(c.similarOrientation(d));
        Assertions.assertFalse(d.similarOrientation(c));

        Assertions.assertTrue(e.similarOrientation(f));
        Assertions.assertTrue(f.similarOrientation(e));
        Assertions.assertTrue(e.similarOrientation(g));
        Assertions.assertTrue(g.similarOrientation(e));

        Assertions.assertFalse(a.similarOrientation(e));
        Assertions.assertFalse(e.similarOrientation(a));
    }

    @Test
    public void testSimilarOrientation_orthogonal() {
        // arrange
        final Line a = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Line b = Lines.fromPointAndDirection(Vector2D.of(4, 5), Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        final Line c = Lines.fromPointAndDirection(Vector2D.of(-4, -5), Vector2D.Unit.MINUS_Y, TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(a.similarOrientation(b));
        Assertions.assertTrue(b.similarOrientation(a));
        Assertions.assertTrue(a.similarOrientation(c));
        Assertions.assertTrue(c.similarOrientation(a));
    }

    @Test
    public void testDistance_parallelLines() {
        // arrange
        final double dist = Math.sin(Math.atan2(2, 1));

        final Line a = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        final Line b = Lines.fromPoints(Vector2D.of(-3, 0), Vector2D.of(0, 6), TEST_PRECISION);
        final Line c = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        final Line d = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, -2), TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(dist, a.distance(b), TEST_EPS);
        Assertions.assertEquals(dist, b.distance(a), TEST_EPS);

        Assertions.assertEquals(dist, a.distance(c), TEST_EPS);
        Assertions.assertEquals(dist, c.distance(a), TEST_EPS);

        Assertions.assertEquals(3 * dist, a.distance(d), TEST_EPS);
        Assertions.assertEquals(3 * dist, d.distance(a), TEST_EPS);
    }

    @Test
    public void testDistance_coincidentLines() {
        // arrange
        final Line a = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        final Line b = Lines.fromPoints(Vector2D.of(-2, 0), Vector2D.of(0, 4), TEST_PRECISION);
        final Line c = b.reverse();

        // act/assert
        Assertions.assertEquals(0, a.distance(a), TEST_EPS);

        Assertions.assertEquals(0, a.distance(b), TEST_EPS);
        Assertions.assertEquals(0, b.distance(a), TEST_EPS);

        Assertions.assertEquals(0, a.distance(c), TEST_EPS);
        Assertions.assertEquals(0, c.distance(a), TEST_EPS);
    }

    @Test
    public void testDistance_nonParallelLines() {
        // arrange
        final Line a = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Line b = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        final Line c = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        final Line d = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(0, 4), TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(0, a.distance(b), TEST_EPS);
        Assertions.assertEquals(0, b.distance(a), TEST_EPS);

        Assertions.assertEquals(0, a.distance(c), TEST_EPS);
        Assertions.assertEquals(0, c.distance(a), TEST_EPS);

        Assertions.assertEquals(0, a.distance(d), TEST_EPS);
        Assertions.assertEquals(0, d.distance(a), TEST_EPS);
    }

    @Test
    public void testDistance() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(0, line.distance(line.getOrigin()), TEST_EPS);
        Assertions.assertEquals(+5.0, line.distance(Vector2D.of(5, -3)), TEST_EPS);
        Assertions.assertEquals(+5.0, line.distance(Vector2D.of(-5, 2)), TEST_EPS);
    }

    @Test
    public void testPointAt() {
        // arrange
        final Vector2D origin = Vector2D.of(-1, 1);
        final double d = Math.sqrt(2);
        final Line line = Lines.fromPointAndDirection(origin, Vector2D.of(1, 1), TEST_PRECISION);

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
        final Line line = Lines.fromPoints(Vector2D.of(2, 1), Vector2D.of(-2, -2), TEST_PRECISION);

        for (double abscissa = -2.0; abscissa < 2.0; abscissa += 0.2) {
            for (double offset = -2.0; offset < 2.0; offset += 0.2) {

                // act
                final Vector2D point = line.pointAt(abscissa, offset);

                // assert
                Assertions.assertEquals(abscissa, line.toSubspace(point).getX(), TEST_EPS);
                Assertions.assertEquals(offset, line.offset(point), TEST_EPS);
            }
        }
    }

    @Test
    public void testContains_line() {
        // arrange
        final Vector2D pt = Vector2D.of(1, 2);
        final Vector2D dir = Vector2D.of(3, 7);
        final Line a = Lines.fromPointAndDirection(pt, dir, TEST_PRECISION);
        final Line b = Lines.fromPointAndDirection(Vector2D.of(0, -4), dir, TEST_PRECISION);
        final Line c = Lines.fromPointAndDirection(Vector2D.of(-2, -2), dir.negate(), TEST_PRECISION);
        final Line d = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        final Line e = Lines.fromPointAndDirection(pt, dir, TEST_PRECISION);
        final Line f = Lines.fromPointAndDirection(pt, dir.negate(), TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(a.contains(a));

        Assertions.assertTrue(a.contains(e));
        Assertions.assertTrue(e.contains(a));

        Assertions.assertTrue(a.contains(f));
        Assertions.assertTrue(f.contains(a));

        Assertions.assertFalse(a.contains(b));
        Assertions.assertFalse(a.contains(c));
        Assertions.assertFalse(a.contains(d));
    }

    @Test
    public void testIsParallel_closeToEpsilon() {
        // arrange
        final double eps = 1e-3;
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        final Vector2D p = Vector2D.of(1, 2);

        final Line line = Lines.fromPointAndAngle(p, 0.0, precision);

        // act/assert
        final Vector2D offset1 = Vector2D.of(0, 1e-4);
        final Vector2D offset2 = Vector2D.of(0, 2e-3);

        Assertions.assertTrue(line.contains(Lines.fromPointAndAngle(p.add(offset1), 0.0, precision)));
        Assertions.assertTrue(line.contains(Lines.fromPointAndAngle(p.subtract(offset1), 0.0, precision)));

        Assertions.assertFalse(line.contains(Lines.fromPointAndAngle(p.add(offset2), 0.0, precision)));
        Assertions.assertFalse(line.contains(Lines.fromPointAndAngle(p.subtract(offset2), 0.0, precision)));

        Assertions.assertTrue(line.contains(Lines.fromPointAndAngle(p, 1e-4, precision)));
        Assertions.assertFalse(line.contains(Lines.fromPointAndAngle(p, 1e-2, precision)));
    }

    @Test
    public void testContains_point() {
        // arrange
        final Vector2D p1 = Vector2D.of(-1, 0);
        final Vector2D p2 = Vector2D.of(0, 2);
        final Line line = Lines.fromPoints(p1, p2, TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(line.contains(p1));
        Assertions.assertTrue(line.contains(p2));

        Assertions.assertFalse(line.contains(Vector2D.ZERO));
        Assertions.assertFalse(line.contains(Vector2D.of(100, 79)));

        final Vector2D offset1 = Vector2D.of(0.1, 0);
        final Vector2D offset2 = Vector2D.of(0, -0.1);
        Vector2D v;
        for (double t = -2; t <= 2; t += 0.1) {
            v = p1.lerp(p2, t);

            Assertions.assertTrue(line.contains(v));

            Assertions.assertFalse(line.contains(v.add(offset1)));
            Assertions.assertFalse(line.contains(v.add(offset2)));
        }
    }

    @Test
    public void testContains_point_closeToEpsilon() {
        // arrange
        final double eps = 1e-3;
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        final Vector2D p1 = Vector2D.of(-1, 0);
        final Vector2D p2 = Vector2D.of(0, 2);
        final Vector2D mid = p1.lerp(p2, 0.5);

        final Line line = Lines.fromPoints(p1, p2, precision);
        final Vector2D dir = line.getOffsetDirection();

        // act/assert
        Assertions.assertTrue(line.contains(mid.add(dir.multiply(1e-4))));
        Assertions.assertTrue(line.contains(mid.add(dir.multiply(-1e-4))));

        Assertions.assertFalse(line.contains(mid.add(dir.multiply(2e-3))));
        Assertions.assertFalse(line.contains(mid.add(dir.multiply(-2e-3))));
    }

    @Test
    public void testDistance_point() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        final Line reversed = line.reverse();

        // act/assert
        Assertions.assertEquals(0.0, line.distance(Vector2D.of(-0.5, 1)), TEST_EPS);
        Assertions.assertEquals(0.0, line.distance(Vector2D.of(-1.5, -1)), TEST_EPS);
        Assertions.assertEquals(0.0, line.distance(Vector2D.of(0.5, 3)), TEST_EPS);

        final double d = Math.sin(Math.atan2(2, 1));

        Assertions.assertEquals(d, line.distance(Vector2D.ZERO), TEST_EPS);
        Assertions.assertEquals(d, line.distance(Vector2D.of(-1, 2)), TEST_EPS);

        Assertions.assertEquals(d, reversed.distance(Vector2D.ZERO), TEST_EPS);
        Assertions.assertEquals(d, reversed.distance(Vector2D.of(-1, 2)), TEST_EPS);
    }

    @Test
    public void testDistance_point_permute() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(0, 2), TEST_PRECISION);
        final Vector2D lineOrigin = line.getOrigin();

        EuclideanTestUtils.permute(-5, 5, 0.5, (x, y) -> {
            final Vector2D pt = Vector2D.of(x, y);

            // act
            final double dist = line.distance(pt);

            // arrange
            final Vector2D vec = lineOrigin.vectorTo(pt).reject(line.getDirection());
            Assertions.assertEquals(vec.norm(), dist, TEST_EPS);
        });
    }

    @Test
    public void testIsParallel() {
        // arrange
        final Vector2D dir = Vector2D.of(3, 7);
        final Line a = Lines.fromPointAndDirection(Vector2D.of(1, 2), dir, TEST_PRECISION);
        final Line b = Lines.fromPointAndDirection(Vector2D.of(0, -4), dir, TEST_PRECISION);
        final Line c = Lines.fromPointAndDirection(Vector2D.of(-2, -2), dir.negate(), TEST_PRECISION);
        final Line d = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(a.isParallel(a));

        Assertions.assertTrue(a.isParallel(b));
        Assertions.assertTrue(b.isParallel(a));

        Assertions.assertTrue(a.isParallel(c));
        Assertions.assertTrue(c.isParallel(a));

        Assertions.assertFalse(a.isParallel(d));
        Assertions.assertFalse(d.isParallel(a));
    }

    @Test
    public void testIsParallel_closeToParallel() {
        // arrange
        final double eps = 1e-3;
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        final Vector2D p1 = Vector2D.of(1, 2);
        final Vector2D p2 = Vector2D.of(1, -2);

        final Line line = Lines.fromPointAndAngle(p1, 0.0, precision);

        // act/assert
        Assertions.assertTrue(line.isParallel(Lines.fromPointAndAngle(p2, 1e-4, precision)));
        Assertions.assertFalse(line.isParallel(Lines.fromPointAndAngle(p2, 1e-2, precision)));
    }

    @Test
    public void testTransform() {
        // arrange
        final AffineTransformMatrix2D scale = AffineTransformMatrix2D.createScale(2, 3);
        final AffineTransformMatrix2D reflect = AffineTransformMatrix2D.createScale(-1, 1);
        final AffineTransformMatrix2D translate = AffineTransformMatrix2D.createTranslation(3, 4);
        final AffineTransformMatrix2D rotate = AffineTransformMatrix2D.createRotation(PlaneAngleRadians.PI_OVER_TWO);
        final AffineTransformMatrix2D rotateAroundPt = AffineTransformMatrix2D.createRotation(Vector2D.of(0, 1), PlaneAngleRadians.PI_OVER_TWO);

        final Vector2D p1 = Vector2D.of(0, 1);
        final Vector2D p2 = Vector2D.of(1, 0);

        final Line horizontal = Lines.fromPointAndDirection(p1, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Line vertical = Lines.fromPointAndDirection(p2, Vector2D.Unit.PLUS_Y, TEST_PRECISION);
        final Line diagonal = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

        // act/assert
        Assertions.assertSame(TEST_PRECISION, horizontal.transform(scale).getPrecision());

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
        final AffineTransformMatrix2D scaleCollapse = AffineTransformMatrix2D.createScale(0, 1);
        final Line line = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        // act/assert
        assertThrows(IllegalArgumentException.class, () -> line.transform(scaleCollapse),  "Line direction cannot be zero");
    }

    @Test
    public void testSubspaceTransform() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION);

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

    private void checkSubspaceTransform(final SubspaceTransform st, final Vector2D origin, final Vector2D dir, final Vector2D tZero, final Vector2D tOne) {

        final Line line = st.getLine();
        final AffineTransformMatrix1D transform = st.getTransform();

        checkLine(line, origin, dir);

        EuclideanTestUtils.assertCoordinatesEqual(tZero, line.toSpace(transform.apply(Vector1D.ZERO)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(tOne, line.toSpace(transform.apply(Vector1D.Unit.PLUS)), TEST_EPS);
    }

    @Test
    public void testSubspaceTransform_transformsPointsCorrectly() {
        // arrange
        final Line line = Lines.fromPointAndDirection(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION);

        EuclideanTestUtils.permuteSkipZero(-2, 2, 0.5, (a, b) -> {
            // create a somewhat complicate transform to try to hit all of the edge cases
            final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createTranslation(Vector2D.of(a, b))
                    .rotate(a * b)
                    .scale(0.1, 4);

            // act
            final SubspaceTransform st = line.subspaceTransform(transform);

            // assert
            for (double x = -5.0; x <= 5.0; x += 1) {
                final Vector1D subPt = Vector1D.of(x);
                final Vector2D expected = transform.apply(line.toSpace(subPt));
                final Vector2D actual = st.getLine().toSpace(
                        st.getTransform().apply(subPt));

                EuclideanTestUtils.assertCoordinatesEqual(expected, actual, TEST_EPS);
            }
        });
    }

    @Test
    public void testEq() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        final Vector2D p = Vector2D.of(1, 2);
        final double angle = 1.0;

        final Line a = Lines.fromPointAndAngle(p, angle, precision);
        final Line b = Lines.fromPointAndAngle(Vector2D.ZERO, angle, precision);
        final Line c = Lines.fromPointAndAngle(p, angle + 1.0, precision);

        final Line d = Lines.fromPointAndAngle(p, angle, precision);
        final Line e = Lines.fromPointAndAngle(p.add(Vector2D.of(1e-4, 1e-4)), angle, precision);
        final Line f = Lines.fromPointAndAngle(p, angle + 1e-4, precision);

        // act/assert
        Assertions.assertTrue(a.eq(a, precision));

        Assertions.assertTrue(a.eq(d, precision));
        Assertions.assertTrue(d.eq(a, precision));

        Assertions.assertTrue(a.eq(e, precision));
        Assertions.assertTrue(e.eq(a, precision));

        Assertions.assertTrue(a.eq(f, precision));
        Assertions.assertTrue(f.eq(a, precision));

        Assertions.assertFalse(a.eq(b, precision));
        Assertions.assertFalse(a.eq(c, precision));
    }

    @Test
    public void testHashCode() {
        // arrange
        final DoublePrecisionContext precision1 = new EpsilonDoublePrecisionContext(1e-4);
        final DoublePrecisionContext precision2 = new EpsilonDoublePrecisionContext(1e-5);

        final Vector2D p = Vector2D.of(1, 2);
        final Vector2D v = Vector2D.of(1, 1);

        final Line a = Lines.fromPointAndDirection(p, v, precision1);
        final Line b = Lines.fromPointAndDirection(Vector2D.ZERO, v, precision1);
        final Line c = Lines.fromPointAndDirection(p, v.negate(), precision1);
        final Line d = Lines.fromPointAndDirection(p, v, precision2);
        final Line e = Lines.fromPointAndDirection(p, v, precision1);

        // act/assert
        final int aHash = a.hashCode();

        Assertions.assertEquals(aHash, a.hashCode());
        Assertions.assertEquals(aHash, e.hashCode());

        Assertions.assertNotEquals(aHash, b.hashCode());
        Assertions.assertNotEquals(aHash, c.hashCode());
        Assertions.assertNotEquals(aHash, d.hashCode());
    }

    @Test
    public void testEquals() {
     // arrange
        final DoublePrecisionContext precision1 = new EpsilonDoublePrecisionContext(1e-4);
        final DoublePrecisionContext precision2 = new EpsilonDoublePrecisionContext(1e-5);

        final Vector2D p = Vector2D.of(1, 2);
        final Vector2D v = Vector2D.of(1, 1);

        final Line a = Lines.fromPointAndDirection(p, v, precision1);
        final Line b = Lines.fromPointAndDirection(Vector2D.ZERO, v, precision1);
        final Line c = Lines.fromPointAndDirection(p, v.negate(), precision1);
        final Line d = Lines.fromPointAndDirection(p, v, precision2);
        final Line e = Lines.fromPointAndDirection(p, v, precision1);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);
        Assertions.assertEquals(a, e);
        Assertions.assertEquals(e, a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(a, c);
        Assertions.assertNotEquals(a, d);
    }

    @Test
    public void testToString() {
        // arrange
        final Line line = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final String str = line.toString();

        // assert
        Assertions.assertTrue(str.contains("Line"));
        Assertions.assertTrue(str.contains("origin= (0.0, 0.0)"));
        Assertions.assertTrue(str.contains("direction= (1.0, 0.0)"));
    }

    /**
     * Check that the line has the given defining properties.
     * @param line
     * @param origin
     * @param dir
     */
    private void checkLine(final Line line, final Vector2D origin, final Vector2D dir) {
        EuclideanTestUtils.assertCoordinatesEqual(origin, line.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(dir, line.getDirection(), TEST_EPS);
    }
}
