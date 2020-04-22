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

public class ReverseRayTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPointAndDirection() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 2);
        Vector2D p1 = Vector2D.of(2, 2);

        // act
        ReverseRay revRay = ReverseRay.fromPointAndDirection(p0, p0.vectorTo(p1), TEST_PRECISION);

        // assert
        Assert.assertFalse(revRay.isFull());
        Assert.assertFalse(revRay.isEmpty());
        Assert.assertTrue(revRay.isInfinite());
        Assert.assertFalse(revRay.isFinite());

        Assert.assertNull(revRay.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(p0, revRay.getEndPoint(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(revRay.getSubspaceStart());
        Assert.assertEquals(1, revRay.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(revRay.getSize());
    }

    @Test
    public void testFromPointAndDirection_invalidArgs() {
        // arrange
        Vector2D p = Vector2D.of(0, 2);
        Vector2D d = Vector2D.of(1e-17, -1e-12);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ReverseRay.fromPointAndDirection(p, d, TEST_PRECISION);
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
        ReverseRay revRay = ReverseRay.fromPoint(line, p3);

        // assert
        Assert.assertFalse(revRay.isFull());
        Assert.assertFalse(revRay.isEmpty());
        Assert.assertTrue(revRay.isInfinite());
        Assert.assertFalse(revRay.isFinite());

        Assert.assertNull(revRay.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 3), revRay.getEndPoint(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(revRay.getSubspaceStart());
        Assert.assertEquals(3, revRay.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(revRay.getSize());
    }

    @Test
    public void testFromPoint_invalidArgs() {
        // arrange
        Vector2D p = Vector2D.of(0, 2);
        Vector2D d = Vector2D.of(1, 1);
        Line line = Line.fromPointAndDirection(p, d, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ReverseRay.fromPoint(line, Vector2D.NaN);
        }, IllegalArgumentException.class, "Invalid reverse ray end location: NaN");

        GeometryTestUtils.assertThrows(() -> {
            ReverseRay.fromPoint(line, Vector2D.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid reverse ray end location: Infinity");

        GeometryTestUtils.assertThrows(() -> {
            ReverseRay.fromPoint(line, Vector2D.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid reverse ray end location: -Infinity");
    }

    @Test
    public void testFromLocation() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 1);
        Vector2D p1 = Vector2D.of(1, 2);

        Line line = Line.fromPoints(p0, p1, TEST_PRECISION);

        // act
        ReverseRay revRay = ReverseRay.fromLocation(line, -2);

        // assert
        Assert.assertFalse(revRay.isFull());
        Assert.assertFalse(revRay.isEmpty());
        Assert.assertTrue(revRay.isInfinite());
        Assert.assertFalse(revRay.isFinite());

        Assert.assertNull(revRay.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -2), revRay.getEndPoint(), TEST_EPS);

        GeometryTestUtils.assertNegativeInfinity(revRay.getSubspaceStart());
        Assert.assertEquals(-2, revRay.getSubspaceEnd(), TEST_EPS);

        GeometryTestUtils.assertPositiveInfinity(revRay.getSize());
    }

    @Test
    public void testFromLocation_invalidArgs() {
        // arrange
        Vector2D p = Vector2D.of(0, 2);
        Vector2D d = Vector2D.of(1, 1);
        Line line = Line.fromPointAndDirection(p, d, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ReverseRay.fromLocation(line, Double.NaN);
        }, IllegalArgumentException.class, "Invalid reverse ray end location: NaN");

        GeometryTestUtils.assertThrows(() -> {
            ReverseRay.fromLocation(line, Double.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid reverse ray end location: Infinity");

        GeometryTestUtils.assertThrows(() -> {
            ReverseRay.fromLocation(line, Double.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid reverse ray end location: -Infinity");
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix2D t = AffineTransformMatrix2D.createRotation(-0.5 * Math.PI)
                .translate(Vector2D.Unit.PLUS_X);

        ReverseRay revRay = ReverseRay.fromPointAndDirection(Vector2D.of(1, 0), Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        ReverseRay result = revRay.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -1), result.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        AffineTransformMatrix2D t = AffineTransformMatrix2D.createRotation(0.5 * Math.PI)
                .translate(Vector2D.Unit.PLUS_X)
                .scale(1, -1);

        ReverseRay revRay = ReverseRay.fromPointAndDirection(Vector2D.of(2, 3),
                Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        ReverseRay result = revRay.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, -2), result.getEndPoint(), TEST_EPS);
    }

    @Test
    public void testReverse() {
        // arrange
        Vector2D start = Vector2D.of(1, 2);

        EuclideanTestUtils.permuteSkipZero(-4, 4, 1, (x, y) -> {
            Vector2D dir = Vector2D.of(x, y);

            ReverseRay revRay = ReverseRay.fromPointAndDirection(start, dir, TEST_PRECISION);

            // act
            Ray rev = revRay.reverse();

            // assert
            EuclideanTestUtils.assertCoordinatesEqual(revRay.getLine().getOrigin(), rev.getLine().getOrigin(), TEST_EPS);
            Assert.assertEquals(-1, revRay.getLine().getDirection().dot(rev.getLine().getDirection()), TEST_EPS);

            EuclideanTestUtils.assertCoordinatesEqual(revRay.getEndPoint(), rev.getStartPoint(), TEST_EPS);
        });
    }

    @Test
    public void testClosest() {
        // arrange
        Vector2D p1 = Vector2D.of(0, -1);
        Vector2D p2 = Vector2D.of(0, 1);
        ReverseRay revRay = ReverseRay.fromPointAndDirection(p2, p1.directionTo(p2), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, revRay.closest(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -2), revRay.closest(Vector2D.of(0, -2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -2), revRay.closest(Vector2D.of(2, -2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -1), revRay.closest(Vector2D.of(-1, -1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(p2, revRay.closest(p2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, revRay.closest(Vector2D.of(0, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, revRay.closest(Vector2D.of(-2, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, revRay.closest(Vector2D.of(-1, 1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, revRay.closest(Vector2D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 0.5), revRay.closest(Vector2D.of(1, 0.5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -0.5), revRay.closest(Vector2D.of(-2, -0.5)), TEST_EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        ReverseRay revRay = ReverseRay.fromPointAndDirection(Vector2D.of(1, 1),
                Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertRegionLocation(revRay, RegionLocation.OUTSIDE,
                Vector2D.of(2, 2), Vector2D.of(2, 0),
                Vector2D.of(2, 1), Vector2D.of(5, 1));

        EuclideanTestUtils.assertRegionLocation(revRay, RegionLocation.BOUNDARY,
                Vector2D.of(1, 1), Vector2D.of(1 + 1e-16, 1));

        EuclideanTestUtils.assertRegionLocation(revRay, RegionLocation.INSIDE,
                Vector2D.of(-2, 1), Vector2D.of(-5, 1 + 1e-16));
    }

    @Test
    public void testSplit() {
        // --- arrange
        Vector2D p0 = Vector2D.of(1, 1);
        Vector2D p1 = Vector2D.of(-3, 1);
        Vector2D high = Vector2D.of(2, 1);

        Vector2D delta = Vector2D.of(1e-11, 1e-11);

        ReverseRay revRay = ReverseRay.fromPointAndDirection(p0, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // --- act

        // parallel
        checkSplit(revRay.split(Line.fromPointAndAngle(Vector2D.of(2, 2), 0, TEST_PRECISION)),
                null, null,
                null, p0);
        checkSplit(revRay.split(Line.fromPointAndAngle(Vector2D.of(2, 2), Math.PI, TEST_PRECISION)),
                null, p0,
                null, null);

        // coincident
        checkSplit(revRay.split(Line.fromPointAndAngle(p0.add(delta), 1e-20, TEST_PRECISION)),
                null, null,
                null, null);

        // through point on revRay
        checkSplit(revRay.split(Line.fromPointAndAngle(p1, 1, TEST_PRECISION)),
                null, p1,
                p1, p0);
        checkSplit(revRay.split(Line.fromPointAndAngle(p1, -1, TEST_PRECISION)),
                p1, p0,
                null, p1);

        // through end point
        checkSplit(revRay.split(Line.fromPointAndAngle(p0.subtract(delta), 1, TEST_PRECISION)),
                null, p0,
                null, null);
        checkSplit(revRay.split(Line.fromPointAndAngle(p0.add(delta), -1, TEST_PRECISION)),
                null, null,
                null, p0);

        // intersection above end point
        checkSplit(revRay.split(Line.fromPointAndAngle(high, 1, TEST_PRECISION)),
                null, p0,
                null, null);
        checkSplit(revRay.split(Line.fromPointAndAngle(high, -1, TEST_PRECISION)),
                null, null,
                null, p0);
    }

    @Test
    public void testGetInterval() {
        // arrange
        ReverseRay revRay = ReverseRay.fromPointAndDirection(Vector2D.of(2, -1), Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Interval interval = revRay.getInterval();

        // assert
        GeometryTestUtils.assertNegativeInfinity(interval.getMin());
        Assert.assertEquals(2, interval.getMax(), TEST_EPS);

        Assert.assertSame(revRay.getLine().getPrecision(), interval.getMaxBoundary().getPrecision());
    }

    @Test
    public void testToString() {
        // arrange
        ReverseRay revRay = ReverseRay.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        // act
        String str = revRay.toString();

        // assert
        GeometryTestUtils.assertContains("ReverseRay[direction= (1", str);
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
