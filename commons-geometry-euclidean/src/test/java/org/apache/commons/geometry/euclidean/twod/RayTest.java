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
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.junit.Assert;
import org.junit.Test;

public class RayTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromPointAndDirection() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 2);
        Vector2D p1 = Vector2D.of(2, 2);

        // act
        Ray ray = Lines.rayFromPointAndDirection(p0, p0.vectorTo(p1), TEST_PRECISION);

        // assert
        Assert.assertFalse(ray.isFull());
        Assert.assertFalse(ray.isEmpty());
        Assert.assertTrue(ray.isInfinite());
        Assert.assertFalse(ray.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(p0, ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        Assert.assertEquals(1, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        GeometryTestUtils.assertPositiveInfinity(ray.getSize());
        Assert.assertNull(ray.getBarycenter());
        Assert.assertNull(ray.getBounds());

        EuclideanTestUtils.assertCoordinatesEqual(p0.vectorTo(p1), ray.getDirection(), TEST_EPS);
    }

    @Test
    public void testFromPointAndDirection_invalidArgs() {
        // arrange
        Vector2D p = Vector2D.of(0, 2);
        Vector2D d = Vector2D.of(1e-17, -1e-12);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines.rayFromPointAndDirection(p, d, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    public void testFromPoint() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 1);
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p3 = Vector2D.of(3, 3);

        Line line = Lines.fromPoints(p0, p1, TEST_PRECISION);

        // act
        Ray ray = Lines.rayFromPoint(line, p3);

        // assert
        Assert.assertFalse(ray.isFull());
        Assert.assertFalse(ray.isEmpty());
        Assert.assertTrue(ray.isInfinite());
        Assert.assertFalse(ray.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 3), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        Assert.assertEquals(3, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        GeometryTestUtils.assertPositiveInfinity(ray.getSize());
        Assert.assertNull(ray.getBarycenter());
        Assert.assertNull(ray.getBounds());

        EuclideanTestUtils.assertCoordinatesEqual(p0.vectorTo(p1), ray.getDirection(), TEST_EPS);
    }

    @Test
    public void testFromPoint_invalidArgs() {
        // arrange
        Vector2D p = Vector2D.of(0, 2);
        Vector2D d = Vector2D.of(1, 1);
        Line line = Lines.fromPointAndDirection(p, d, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines.rayFromPoint(line, Vector2D.NaN);
        }, IllegalArgumentException.class, "Invalid ray start point: (NaN, NaN)");

        GeometryTestUtils.assertThrows(() -> {
            Lines.rayFromPoint(line, Vector2D.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid ray start point: (Infinity, Infinity)");

        GeometryTestUtils.assertThrows(() -> {
            Lines.rayFromPoint(line, Vector2D.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid ray start point: (-Infinity, -Infinity)");
    }

    @Test
    public void testFromLocation() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 1);
        Vector2D p1 = Vector2D.of(1, 2);

        Line line = Lines.fromPoints(p0, p1, TEST_PRECISION);

        // act
        Ray ray = Lines.rayFromLocation(line, -2);

        // assert
        Assert.assertFalse(ray.isFull());
        Assert.assertFalse(ray.isEmpty());
        Assert.assertTrue(ray.isInfinite());
        Assert.assertFalse(ray.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -2), ray.getStartPoint(), TEST_EPS);
        Assert.assertNull(ray.getEndPoint());

        Assert.assertEquals(-2, ray.getSubspaceStart(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(ray.getSubspaceEnd());

        GeometryTestUtils.assertPositiveInfinity(ray.getSize());
        Assert.assertNull(ray.getBarycenter());
        Assert.assertNull(ray.getBounds());

        EuclideanTestUtils.assertCoordinatesEqual(p0.vectorTo(p1), ray.getDirection(), TEST_EPS);
    }

    @Test
    public void testFromLocation_invalidArgs() {
        // arrange
        Vector2D p = Vector2D.of(0, 2);
        Vector2D d = Vector2D.of(1, 1);
        Line line = Lines.fromPointAndDirection(p, d, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Lines.rayFromLocation(line, Double.NaN);
        }, IllegalArgumentException.class, "Invalid ray start location: NaN");

        GeometryTestUtils.assertThrows(() -> {
            Lines.rayFromLocation(line, Double.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid ray start location: Infinity");

        GeometryTestUtils.assertThrows(() -> {
            Lines.rayFromLocation(line, Double.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid ray start location: -Infinity");
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix2D t = AffineTransformMatrix2D.createRotation(-0.5 * Math.PI)
                .translate(Vector2D.Unit.PLUS_X);

        Ray ray = Lines.rayFromPointAndDirection(Vector2D.of(1, 0), Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Ray result = ray.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -1), result.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_Y, result.getDirection(), TEST_EPS);
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        AffineTransformMatrix2D t = AffineTransformMatrix2D.createRotation(0.5 * Math.PI)
                .translate(Vector2D.Unit.PLUS_X)
                .scale(1, -1);

        Ray ray = Lines.rayFromPointAndDirection(Vector2D.of(2, 3), Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Ray result = ray.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, -2), result.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_Y, result.getDirection(), TEST_EPS);
    }

    @Test
    public void testReverse() {
        // arrange
        Vector2D start = Vector2D.of(1, 2);

        EuclideanTestUtils.permuteSkipZero(-4, 4, 1, (x, y) -> {
            Vector2D dir = Vector2D.of(x, y);

            Ray ray = Lines.rayFromPointAndDirection(start, dir, TEST_PRECISION);

            // act
            ReverseRay rev = ray.reverse();

            // assert
            EuclideanTestUtils.assertCoordinatesEqual(ray.getLine().getOrigin(), rev.getLine().getOrigin(), TEST_EPS);
            Assert.assertEquals(-1, ray.getLine().getDirection().dot(rev.getLine().getDirection()), TEST_EPS);

            EuclideanTestUtils.assertCoordinatesEqual(ray.getStartPoint(), rev.getEndPoint(), TEST_EPS);
        });
    }

    @Test
    public void testClosest() {
        // arrange
        Vector2D p1 = Vector2D.of(0, -1);
        Vector2D p2 = Vector2D.of(0, 1);
        Ray ray = Lines.rayFromPointAndDirection(p1, p1.directionTo(p2), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, ray.closest(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, ray.closest(Vector2D.of(0, -2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, ray.closest(Vector2D.of(2, -2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, ray.closest(Vector2D.of(-1, -1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(p2, ray.closest(p2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2), ray.closest(Vector2D.of(0, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2), ray.closest(Vector2D.of(-2, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 1), ray.closest(Vector2D.of(-1, 1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, ray.closest(Vector2D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 0.5), ray.closest(Vector2D.of(1, 0.5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -0.5), ray.closest(Vector2D.of(-2, -0.5)), TEST_EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        Ray ray = Lines.rayFromPointAndDirection(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertRegionLocation(ray, RegionLocation.OUTSIDE,
                Vector2D.of(2, 2), Vector2D.of(2, 0),
                Vector2D.of(-5, 1), Vector2D.of(0, 1));

        EuclideanTestUtils.assertRegionLocation(ray, RegionLocation.BOUNDARY,
                Vector2D.of(1, 1), Vector2D.of(1 + 1e-16, 1));

        EuclideanTestUtils.assertRegionLocation(ray, RegionLocation.INSIDE,
                Vector2D.of(2, 1), Vector2D.of(5, 1 + 1e-16));
    }

    @Test
    public void testSplit() {
        // --- arrange
        Vector2D p0 = Vector2D.of(1, 1);
        Vector2D p1 = Vector2D.of(3, 1);
        Vector2D low = Vector2D.of(0, 1);

        Vector2D delta = Vector2D.of(1e-11, 1e-11);

        Ray ray = Lines.rayFromPointAndDirection(p0, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // --- act

        // parallel
        checkSplit(ray.split(Lines.fromPointAndAngle(Vector2D.of(2, 2), 0, TEST_PRECISION)),
                null, null,
                p0, null);
        checkSplit(ray.split(Lines.fromPointAndAngle(Vector2D.of(2, 2), Math.PI, TEST_PRECISION)),
                p0, null,
                null, null);

        // coincident
        checkSplit(ray.split(Lines.fromPointAndAngle(p0.add(delta), 1e-20, TEST_PRECISION)),
                null, null,
                null, null);

        // through point on ray
        checkSplit(ray.split(Lines.fromPointAndAngle(p1, 1, TEST_PRECISION)),
                p0, p1,
                p1, null);
        checkSplit(ray.split(Lines.fromPointAndAngle(p1, -1, TEST_PRECISION)),
                p1, null,
                p0, p1);

        // through start point
        checkSplit(ray.split(Lines.fromPointAndAngle(p0.subtract(delta), 1, TEST_PRECISION)),
                null, null,
                p0, null);
        checkSplit(ray.split(Lines.fromPointAndAngle(p0.add(delta), -1, TEST_PRECISION)),
                p0, null,
                null, null);

        // intersection below minus
        checkSplit(ray.split(Lines.fromPointAndAngle(low, 1, TEST_PRECISION)),
                null, null,
                p0, null);
        checkSplit(ray.split(Lines.fromPointAndAngle(low, -1, TEST_PRECISION)),
                p0, null,
                null, null);
    }

    @Test
    public void testSplit_smallAngle_pointOnSplitter() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-5);

        Ray ray = Lines.rayFromPointAndDirection(Vector2D.of(1, 1e-6), Vector2D.of(-1, -1e-2), precision);

        Line splitter = Lines.fromPointAndAngle(Vector2D.ZERO, 0, precision);

        // act
        Split<LineConvexSubset> split = ray.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(ray, split.getPlus());
    }

    @Test
    public void testGetInterval() {
        // arrange
        Ray ray = Lines.rayFromPointAndDirection(Vector2D.of(2, -1), Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Interval interval = ray.getInterval();

        // assert
        Assert.assertEquals(2, interval.getMin(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(interval.getMax());

        Assert.assertSame(ray.getLine().getPrecision(), interval.getMinBoundary().getPrecision());
    }

    @Test
    public void testToString() {
        // arrange
        Ray ray = Lines.rayFromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        // act
        String str = ray.toString();

        // assert
        GeometryTestUtils.assertContains("Ray[startPoint= (0", str);
        GeometryTestUtils.assertContains(", direction= (1", str);
    }

    private static void checkSplit(Split<LineConvexSubset> split, Vector2D minusStart, Vector2D minusEnd,
            Vector2D plusStart, Vector2D plusEnd) {

        LineConvexSubset minus = split.getMinus();
        if (minusStart == null && minusEnd == null) {
            Assert.assertNull(minus);
        } else {
            checkPoint(minusStart, minus.getStartPoint());
            checkPoint(minusEnd, minus.getEndPoint());
        }


        LineConvexSubset plus = split.getPlus();
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
