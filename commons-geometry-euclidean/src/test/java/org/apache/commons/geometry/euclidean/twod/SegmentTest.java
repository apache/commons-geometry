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
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SegmentTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testFromPoints() {
        // arrange
        final Vector2D p1 = Vector2D.of(1, 2);
        final Vector2D p2 = Vector2D.of(3, 2);

        // act
        final Segment seg = Lines.segmentFromPoints(p1, p2, TEST_PRECISION);

        // assert
        Assertions.assertFalse(seg.isFull());
        Assertions.assertFalse(seg.isEmpty());
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(p1, seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, seg.getEndPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 2), seg.getCentroid(), TEST_EPS);

        Assertions.assertEquals(1, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(3, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(2, seg.getSize(), TEST_EPS);
    }

    @Test
    void testFromPoints_invalidArgs() {
        // arrange
        final Vector2D p1 = Vector2D.of(0, 2);
        final Vector2D p2 = Vector2D.of(1e-17, 2);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines.segmentFromPoints(p1, p1, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines.segmentFromPoints(p1, p2, TEST_PRECISION);
        }, IllegalArgumentException.class, "Line direction cannot be zero");
    }

    @Test
    void testFromPoints_givenLine() {
        // arrange
        final Vector2D p1 = Vector2D.of(-1, 2);
        final Vector2D p2 = Vector2D.of(3, 3);

        final Line line = Lines.fromPointAndDirection(Vector2D.of(1, 0), Vector2D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final Segment seg = Lines.segmentFromPoints(line, p2, p1); // reverse location order

        // assert
        Assertions.assertFalse(seg.isFull());
        Assertions.assertFalse(seg.isEmpty());
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 3), seg.getEndPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2.5), seg.getCentroid(), TEST_EPS);

        Assertions.assertEquals(2, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(3, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(1, seg.getSize(), TEST_EPS);
    }

    @Test
    void testFromPoints_givenLine_singlePoint() {
        // arrange
        final Vector2D p1 = Vector2D.of(-1, 2);

        final Line line = Lines.fromPointAndDirection(Vector2D.of(1, 0), Vector2D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final Segment seg = Lines.segmentFromPoints(line, p1, p1);

        // assert
        Assertions.assertFalse(seg.isFull());
        Assertions.assertFalse(seg.isEmpty());
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), seg.getEndPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), seg.getCentroid(), TEST_EPS);

        Assertions.assertEquals(2, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(2, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(0, seg.getSize(), TEST_EPS);
    }

    @Test
    void testFromPoints_givenLine_invalidArgs() {
        // arrange
        final Vector2D p0 = Vector2D.of(1, 0);
        final Vector2D p1 = Vector2D.of(2, 0);

        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines.segmentFromPoints(line, Vector2D.NaN, p1);
        }, IllegalArgumentException.class, "Invalid line segment locations: NaN, 2.0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines.segmentFromPoints(line, p0, Vector2D.NaN);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, NaN");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines.segmentFromPoints(line, Vector2D.NEGATIVE_INFINITY, p1);
        }, IllegalArgumentException.class, "Invalid line segment locations: NaN, 2.0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines.segmentFromPoints(line, p0, Vector2D.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, NaN");
    }

    @Test
    void testFromLocations() {
        // arrange
        final Line line = Lines.fromPointAndDirection(Vector2D.of(-1, 0), Vector2D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final Segment seg = Lines.segmentFromLocations(line, -1, 2);

        // assert
        Assertions.assertFalse(seg.isFull());
        Assertions.assertFalse(seg.isEmpty());
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, -1), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 2), seg.getEndPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 0.5), seg.getCentroid(), TEST_EPS);

        Assertions.assertEquals(-1, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(2, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(3, seg.getSize(), TEST_EPS);
    }

    @Test
    void testFromLocations_reversedLocationOrder() {
        // arrange
        final Line line = Lines.fromPointAndDirection(Vector2D.of(-1, 0), Vector2D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final Segment seg = Lines.segmentFromLocations(line, 2, -1);

        // assert
        Assertions.assertFalse(seg.isFull());
        Assertions.assertFalse(seg.isEmpty());
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, -1), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 2), seg.getEndPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 0.5), seg.getCentroid(), TEST_EPS);

        Assertions.assertEquals(-1, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(2, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(3, seg.getSize(), TEST_EPS);
    }

    @Test
    void testFromLocations_singlePoint() {
        // arrange
        final Line line = Lines.fromPointAndDirection(Vector2D.of(-1, 0), Vector2D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final Segment seg = Lines.segmentFromLocations(line, 1, 1);

        // assert
        Assertions.assertFalse(seg.isFull());
        Assertions.assertFalse(seg.isEmpty());
        Assertions.assertFalse(seg.isInfinite());
        Assertions.assertTrue(seg.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), seg.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), seg.getEndPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), seg.getCentroid(), TEST_EPS);

        Assertions.assertEquals(1, seg.getSubspaceStart(), TEST_EPS);
        Assertions.assertEquals(1, seg.getSubspaceEnd(), TEST_EPS);

        Assertions.assertEquals(0, seg.getSize(), TEST_EPS);
    }

    @Test
    void testFromLocations_invalidArgs() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines.segmentFromLocations(line, Double.NaN, 2);
        }, IllegalArgumentException.class, "Invalid line segment locations: NaN, 2.0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines.segmentFromLocations(line, 1, Double.NaN);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, NaN");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines.segmentFromLocations(line, Double.NEGATIVE_INFINITY, 2);
        }, IllegalArgumentException.class, "Invalid line segment locations: -Infinity, 2.0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Lines.segmentFromLocations(line, 1, Double.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid line segment locations: 1.0, Infinity");
    }

    @Test
    void testGetBounds() {
        // arrange
        final Segment seg = Lines.segmentFromPoints(Vector2D.of(-1, 4), Vector2D.of(2, -2), TEST_PRECISION);

        // act
        final Bounds2D bounds = seg.getBounds();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, -2), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 4), bounds.getMax(), TEST_EPS);
    }

    @Test
    void testTransform() {
        // arrange
        final AffineTransformMatrix2D t = AffineTransformMatrix2D.createRotation(0.5 * Math.PI)
                .translate(Vector2D.Unit.PLUS_X);

        final Segment seg = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        // act
        final Segment result = seg.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 0), result.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), result.getEndPoint(), TEST_EPS);
    }

    @Test
    void testTransform_reflection() {
        // arrange
        final AffineTransformMatrix2D t = AffineTransformMatrix2D.createRotation(0.5 * Math.PI)
                .translate(Vector2D.Unit.PLUS_X)
                .scale(1, -1);

        final Segment seg = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        // act
        final Segment result = seg.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 0), result.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, -1), result.getEndPoint(), TEST_EPS);
    }

    @Test
    void testReverse() {
        // arrange
        final Vector2D start = Vector2D.of(1, 2);

        EuclideanTestUtils.permuteSkipZero(-4, 4, 1, (x, y) -> {
            final Vector2D end = Vector2D.of(x, y).add(start);

            final Segment seg = Lines.segmentFromPoints(start, end, TEST_PRECISION);

            // act
            final Segment rev = seg.reverse();

            // assert
            Assertions.assertEquals(seg.getSize(), rev.getSize(), TEST_EPS);

            EuclideanTestUtils.assertCoordinatesEqual(seg.getLine().getOrigin(), rev.getLine().getOrigin(), TEST_EPS);
            Assertions.assertEquals(-1, seg.getLine().getDirection().dot(rev.getLine().getDirection()), TEST_EPS);

            EuclideanTestUtils.assertCoordinatesEqual(seg.getEndPoint(), rev.getStartPoint(), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(seg.getStartPoint(), rev.getEndPoint(), TEST_EPS);
        });
    }

    @Test
    void testClosest() {
        // arrange
        final Vector2D p1 = Vector2D.of(0, -1);
        final Vector2D p2 = Vector2D.of(0, 1);
        final Segment seg = Lines.segmentFromPoints(p1, p2, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, seg.closest(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, seg.closest(Vector2D.of(0, -2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, seg.closest(Vector2D.of(2, -2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, seg.closest(Vector2D.of(-1, -1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(p2, seg.closest(p2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, seg.closest(Vector2D.of(0, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, seg.closest(Vector2D.of(-2, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, seg.closest(Vector2D.of(-1, 1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, seg.closest(Vector2D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 0.5), seg.closest(Vector2D.of(1, 0.5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -0.5), seg.closest(Vector2D.of(-2, -0.5)), TEST_EPS);
    }

    @Test
    void testClassify() {
        // arrange
        final Segment seg = Lines.segmentFromPoints(Vector2D.of(1, 1), Vector2D.of(3, 1), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertRegionLocation(seg, RegionLocation.OUTSIDE,
                Vector2D.of(2, 2), Vector2D.of(2, 0),
                Vector2D.of(0, 1), Vector2D.of(4, 1));

        EuclideanTestUtils.assertRegionLocation(seg, RegionLocation.BOUNDARY,
                Vector2D.of(1, 1), Vector2D.of(3, 1),
                Vector2D.of(1 + 1e-16, 1), Vector2D.of(3, 1 - 1e-12));

        EuclideanTestUtils.assertRegionLocation(seg, RegionLocation.INSIDE, Vector2D.of(2, 1));
    }

    @Test
    void testSplit() {
        // --- arrange
        final Vector2D p0 = Vector2D.of(1, 1);
        final Vector2D p1 = Vector2D.of(3, 1);
        final Vector2D mid = p0.lerp(p1, 0.5);
        final Vector2D low = Vector2D.of(0, 1);
        final Vector2D high = Vector2D.of(3, 1);

        final Vector2D delta = Vector2D.of(1e-11, 1e-11);

        final Segment seg = Lines.segmentFromPoints(Vector2D.of(1, 1), Vector2D.of(3, 1), TEST_PRECISION);

        // --- act

        // parallel
        checkSplit(seg.split(Lines.fromPointAndAngle(Vector2D.of(2, 2), 0, TEST_PRECISION)),
                null, null,
                p0, p1);
        checkSplit(seg.split(Lines.fromPointAndAngle(Vector2D.of(2, 2), Math.PI, TEST_PRECISION)),
                p0, p1,
                null, null);

        // coincident
        checkSplit(seg.split(Lines.fromPointAndAngle(p0.add(delta), 1e-20, TEST_PRECISION)),
                null, null,
                null, null);

        // through mid point
        checkSplit(seg.split(Lines.fromPointAndAngle(mid, 1, TEST_PRECISION)),
                p0, mid,
                mid, p1);
        checkSplit(seg.split(Lines.fromPointAndAngle(mid, -1, TEST_PRECISION)),
                mid, p1,
                p0, mid);

        // through start point
        checkSplit(seg.split(Lines.fromPointAndAngle(p0.subtract(delta), 1, TEST_PRECISION)),
                null, null,
                p0, p1);
        checkSplit(seg.split(Lines.fromPointAndAngle(p0.add(delta), -1, TEST_PRECISION)),
                p0, p1,
                null, null);

        // through end point
        checkSplit(seg.split(Lines.fromPointAndAngle(p1.subtract(delta), 1, TEST_PRECISION)),
                p0, p1,
                null, null);
        checkSplit(seg.split(Lines.fromPointAndAngle(p1.add(delta), -1, TEST_PRECISION)),
                null, null,
                p0, p1);

        // intersection below minus
        checkSplit(seg.split(Lines.fromPointAndAngle(low, 1, TEST_PRECISION)),
                null, null,
                p0, p1);
        checkSplit(seg.split(Lines.fromPointAndAngle(low, -1, TEST_PRECISION)),
                p0, p1,
                null, null);

        // intersection above minus
        checkSplit(seg.split(Lines.fromPointAndAngle(high, 1, TEST_PRECISION)),
                p0, p1,
                null, null);
        checkSplit(seg.split(Lines.fromPointAndAngle(high, -1, TEST_PRECISION)),
                null, null,
                p0, p1);
    }

    @Test
    void testSplit_pointsOnSplitterWithLineIntersection() {
        // arrange
        // Create a segment with both of its points lying on the splitter but with the intersection
        // of the lines lying far enough away from the segment start point along the line to be
        // considered a valid 1D distance for a split. In this case, no split should be performed since
        // both points still lie on the splitter.
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-5);

        final Segment seg = Lines.segmentFromPoints(Vector2D.of(1, 1e-8), Vector2D.of(1.01, 1e-6), precision);

        final Line splitter = Lines.fromPointAndAngle(Vector2D.ZERO, 0, precision);

        // act
        final Split<LineConvexSubset> split = seg.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    void testGetInterval() {
        // arrange
        final Segment seg = Lines.segmentFromPoints(Vector2D.of(2, -1), Vector2D.of(2, 2), TEST_PRECISION);

        // act
        final Interval interval = seg.getInterval();

        // assert
        Assertions.assertEquals(-1, interval.getMin(), TEST_EPS);
        Assertions.assertEquals(2, interval.getMax(), TEST_EPS);

        Assertions.assertSame(seg.getLine().getPrecision(), interval.getMinBoundary().getPrecision());
    }

    @Test
    void testGetInterval_singlePoint() {
        // arrange
        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION);
        final Segment seg = Lines.segmentFromLocations(line, 1, 1);

        // act
        final Interval interval = seg.getInterval();

        // assert
        Assertions.assertEquals(1, interval.getMin(), TEST_EPS);
        Assertions.assertEquals(1, interval.getMax(), TEST_EPS);
        Assertions.assertEquals(0, interval.getSize(), TEST_EPS);

        Assertions.assertSame(seg.getLine().getPrecision(), interval.getMinBoundary().getPrecision());
    }

    @Test
    void testToString() {
        // arrange
        final Segment seg = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION);

        // act
        final String str = seg.toString();

        // assert
        GeometryTestUtils.assertContains("Segment[startPoint= (0", str);
        GeometryTestUtils.assertContains(", endPoint= (1", str);
    }

    private static void checkSplit(final Split<LineConvexSubset> split, final Vector2D minusStart, final Vector2D minusEnd,
                                   final Vector2D plusStart, final Vector2D plusEnd) {

        final Segment minus = (Segment) split.getMinus();
        if (minusStart != null) {
            EuclideanTestUtils.assertCoordinatesEqual(minusStart, minus.getStartPoint(), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(minusEnd, minus.getEndPoint(), TEST_EPS);
        } else {
            Assertions.assertNull(minus);
        }

        final Segment plus = (Segment) split.getPlus();
        if (plusStart != null) {
            EuclideanTestUtils.assertCoordinatesEqual(plusStart, plus.getStartPoint(), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(plusEnd, plus.getEndPoint(), TEST_EPS);
        } else {
            Assertions.assertNull(plus);
        }
    }
}
