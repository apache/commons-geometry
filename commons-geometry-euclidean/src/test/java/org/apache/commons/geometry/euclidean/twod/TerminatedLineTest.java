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
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.junit.Assert;
import org.junit.Test;

public class TerminatedLineTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPointAndDirection() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 2);
        Vector2D p1 = Vector2D.of(2, 2);

        // act
        TerminatedLine halfLine = TerminatedLine.fromPointAndDirection(p0, p0.vectorTo(p1), TEST_PRECISION);

        // assert
        Assert.assertFalse(halfLine.isFull());
        Assert.assertFalse(halfLine.isEmpty());
        Assert.assertTrue(halfLine.isInfinite());
        Assert.assertFalse(halfLine.isFinite());

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(p0, halfLine.getEndPoint(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(1, halfLine.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(halfLine.getSize());
    }

    @Test
    public void testFromPointAndDirection_invalidArgs() {
        // arrange
        Vector2D p = Vector2D.of(0, 2);
        Vector2D d = Vector2D.of(1e-17, -1e-12);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            TerminatedLine.fromPointAndDirection(p, d, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPoint() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 1);
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p3 = Vector2D.of(3, 3);

        Line line = Line.fromPoints(p0, p1, TEST_PRECISION);

        // act
        TerminatedLine halfLine = TerminatedLine.fromPoint(line, p3);

        // assert
        Assert.assertFalse(halfLine.isFull());
        Assert.assertFalse(halfLine.isEmpty());
        Assert.assertTrue(halfLine.isInfinite());
        Assert.assertFalse(halfLine.isFinite());

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 3), halfLine.getEndPoint(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(3, halfLine.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(halfLine.getSize());
    }

    @Test
    public void testFromPoint_invalidArgs() {
        // arrange
        Vector2D p = Vector2D.of(0, 2);
        Vector2D d = Vector2D.of(1, 1);
        Line line = Line.fromPointAndDirection(p, d, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            TerminatedLine.fromPoint(line, Vector2D.NaN);
        }, IllegalArgumentException.class, "Invalid terminated line end location: NaN");

        GeometryTestUtils.assertThrows(() -> {
            TerminatedLine.fromPoint(line, Vector2D.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid terminated line end location: Infinity");

        GeometryTestUtils.assertThrows(() -> {
            TerminatedLine.fromPoint(line, Vector2D.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid terminated line end location: -Infinity");
    }

    @Test
    public void testFromLocation() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 1);
        Vector2D p1 = Vector2D.of(1, 2);

        Line line = Line.fromPoints(p0, p1, TEST_PRECISION);

        // act
        TerminatedLine halfLine = TerminatedLine.fromLocation(line, -2);

        // assert
        Assert.assertFalse(halfLine.isFull());
        Assert.assertFalse(halfLine.isEmpty());
        Assert.assertTrue(halfLine.isInfinite());
        Assert.assertFalse(halfLine.isFinite());

        Assert.assertNull(halfLine.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -2), halfLine.getEndPoint(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(halfLine.getSubspaceStart());
        Assert.assertEquals(-2, halfLine.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(halfLine.getSize());
    }

    @Test
    public void testFromLocation_invalidArgs() {
        // arrange
        Vector2D p = Vector2D.of(0, 2);
        Vector2D d = Vector2D.of(1, 1);
        Line line = Line.fromPointAndDirection(p, d, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            TerminatedLine.fromLocation(line, Double.NaN);
        }, IllegalArgumentException.class, "Invalid terminated line end location: NaN");

        GeometryTestUtils.assertThrows(() -> {
            TerminatedLine.fromLocation(line, Double.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid terminated line end location: Infinity");

        GeometryTestUtils.assertThrows(() -> {
            TerminatedLine.fromLocation(line, Double.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid terminated line end location: -Infinity");
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix2D t = AffineTransformMatrix2D.createRotation(-0.5 * Math.PI)
                .translate(Vector2D.Unit.PLUS_X);

        TerminatedLine halfLine = TerminatedLine.fromPointAndDirection(Vector2D.of(1, 0), Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        TerminatedLine result = halfLine.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -1), result.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        AffineTransformMatrix2D t = AffineTransformMatrix2D.createRotation(0.5 * Math.PI)
                .translate(Vector2D.Unit.PLUS_X)
                .scale(1, -1);

        TerminatedLine halfLine = TerminatedLine.fromPointAndDirection(Vector2D.of(2, 3),
                Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        TerminatedLine result = halfLine.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, -2), result.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testReverse() {
        // arrange
        Vector2D start = Vector2D.of(1, 2);

        EuclideanTestUtils.permuteSkipZero(-4, 4, 1, (x, y) -> {
            Vector2D dir = Vector2D.of(x, y);

            TerminatedLine halfLine = TerminatedLine.fromPointAndDirection(start, dir, TEST_PRECISION);

            // act
            Ray rev = halfLine.reverse();

            // assert
            EuclideanTestUtils.assertCoordinatesEqual(halfLine.getLine().getOrigin(), rev.getLine().getOrigin(), TEST_EPS);
            Assert.assertEquals(-1, halfLine.getLine().getDirection().dot(rev.getLine().getDirection()), TEST_EPS);

            EuclideanTestUtils.assertCoordinatesEqual(halfLine.getEndPoint(), rev.getStartPoint(), TEST_EPS);
        });
    }

    @Test
    public void testClosest() {
        // arrange
        Vector2D p1 = Vector2D.of(0, -1);
        Vector2D p2 = Vector2D.of(0, 1);
        TerminatedLine halfLine = TerminatedLine.fromPointAndDirection(p2, p1.directionTo(p2), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, halfLine.closest(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -2), halfLine.closest(Vector2D.of(0, -2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -2), halfLine.closest(Vector2D.of(2, -2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -1), halfLine.closest(Vector2D.of(-1, -1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(p2, halfLine.closest(p2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, halfLine.closest(Vector2D.of(0, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, halfLine.closest(Vector2D.of(-2, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, halfLine.closest(Vector2D.of(-1, 1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, halfLine.closest(Vector2D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 0.5), halfLine.closest(Vector2D.of(1, 0.5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -0.5), halfLine.closest(Vector2D.of(-2, -0.5)), TEST_EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        TerminatedLine halfLine = TerminatedLine.fromPointAndDirection(Vector2D.of(1, 1),
                Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertRegionLocation(halfLine, RegionLocation.OUTSIDE,
                Vector2D.of(2, 2), Vector2D.of(2, 0),
                Vector2D.of(2, 1), Vector2D.of(5, 1));

        EuclideanTestUtils.assertRegionLocation(halfLine, RegionLocation.BOUNDARY,
                Vector2D.of(1, 1), Vector2D.of(1 + 1e-16, 1));

        EuclideanTestUtils.assertRegionLocation(halfLine, RegionLocation.INSIDE,
                Vector2D.of(-2, 1), Vector2D.of(-5, 1 + 1e-16));
    }

    @Test
    public void testSplit() {
        // --- arrange
        Vector2D p0 = Vector2D.of(1, 1);
        Vector2D p1 = Vector2D.of(-3, 1);
        Vector2D high = Vector2D.of(2, 1);

        Vector2D delta = Vector2D.of(1e-11, 1e-11);

        TerminatedLine halfLine = TerminatedLine.fromPointAndDirection(p0, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // --- act

        // parallel
        checkSplit(halfLine.split(Line.fromPointAndAngle(Vector2D.of(2, 2), 0, TEST_PRECISION)),
                null, null,
                null, p0);
        checkSplit(halfLine.split(Line.fromPointAndAngle(Vector2D.of(2, 2), Math.PI, TEST_PRECISION)),
                null, p0,
                null, null);

        // coincident
        checkSplit(halfLine.split(Line.fromPointAndAngle(p0.add(delta), 1e-20, TEST_PRECISION)),
                null, null,
                null, null);

        // through point on halfLine
        checkSplit(halfLine.split(Line.fromPointAndAngle(p1, 1, TEST_PRECISION)),
                null, p1,
                p1, p0);
        checkSplit(halfLine.split(Line.fromPointAndAngle(p1, -1, TEST_PRECISION)),
                p1, p0,
                null, p1);

        // through end point
        checkSplit(halfLine.split(Line.fromPointAndAngle(p0.subtract(delta), 1, TEST_PRECISION)),
                null, p0,
                null, null);
        checkSplit(halfLine.split(Line.fromPointAndAngle(p0.add(delta), -1, TEST_PRECISION)),
                null, null,
                null, p0);

        // intersection above end point
        checkSplit(halfLine.split(Line.fromPointAndAngle(high, 1, TEST_PRECISION)),
                null, p0,
                null, null);
        checkSplit(halfLine.split(Line.fromPointAndAngle(high, -1, TEST_PRECISION)),
                null, null,
                null, p0);
    }

    @Test
    public void testGetInterval() {
        // arrange
        TerminatedLine halfLine = TerminatedLine.fromPointAndDirection(Vector2D.of(2, -1), Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Interval interval = halfLine.getInterval();

        // assert
        GeometryTestUtils.assertNegativeInfinity(interval.getMin());
        Assert.assertEquals(2, interval.getMax(), TEST_EPS);

        Assert.assertSame(halfLine.getLine().getPrecision(), interval.getMaxBoundary().getPrecision());
    }

    @Test
    public void testToString() {
        // arrange
        TerminatedLine halfLine = TerminatedLine.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        // act
        String str = halfLine.toString();

        // assert
        GeometryTestUtils.assertContains("TerminatedLine[direction= (1", str);
        GeometryTestUtils.assertContains(", endPoint= (0", str);
    }

    private static void checkSplit(Split<ConvexSubLine> split, Vector2D minusStart, Vector2D minusEnd,
            Vector2D plusStart, Vector2D plusEnd) {

        ConvexSubLine minus = split.getMinus();
        if (minusStart == null && minusEnd == null) {
            Assert.assertNull(minus);
        } else {
            checkPoint(minusStart, minus.getStartPoint());
            checkPoint(minusEnd, minus.getEndPoint());
        }


        ConvexSubLine plus = split.getPlus();
        if (plusStart == null && plusEnd == null) {
            Assert.assertNull(plus);
        } else {
            checkPoint(plusStart, plus.getStartPoint());
            checkPoint(plusEnd, plus.getEndPoint());
        }
    }

    private static void checkPoint(Vector2D expected, Vector2D pt) {
        if (expected == null) {
            Assert.assertNull(pt);
        } else {
            EuclideanTestUtils.assertCoordinatesEqual(expected, pt, TEST_EPS);
        }
    }
}
