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
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LineSpanningSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    public void testProperties() {
        // arrange
        final Line line = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final LineSpanningSubset result = new LineSpanningSubset(line);

        // assert
        Assertions.assertSame(line, result.getHyperplane());
        Assertions.assertSame(line, result.getLine());

        Assertions.assertTrue(result.isFull());
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertTrue(result.isInfinite());
        Assertions.assertFalse(result.isFinite());

        GeometryTestUtils.assertPositiveInfinity(result.getSize());
        Assertions.assertNull(result.getCentroid());
        Assertions.assertNull(result.getBounds());

        Assertions.assertNull(result.getStartPoint());
        GeometryTestUtils.assertNegativeInfinity(result.getSubspaceStart());
        Assertions.assertNull(result.getEndPoint());
        GeometryTestUtils.assertPositiveInfinity(result.getSubspaceEnd());
    }

    @Test
    public void testTransform() {
        // arrange
        final AffineTransformMatrix2D t = AffineTransformMatrix2D.createRotation(-0.5 * Math.PI)
                .translate(Vector2D.Unit.PLUS_X)
                .scale(1, -1);

        final LineConvexSubset span =
                Lines.fromPointAndDirection(Vector2D.of(1, 0), Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        final LineConvexSubset result = span.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_Y, result.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testReverse() {
        // arrange
        final LineConvexSubset span =
                Lines.fromPointAndDirection(Vector2D.of(1, 2), Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        final LineConvexSubset rev = span.reverse();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2), rev.getLine().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_X, rev.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testClosest() {
        // arrange
        final Vector2D p1 = Vector2D.of(0, -1);
        final Vector2D p2 = Vector2D.of(0, 1);
        final LineConvexSubset span =
                Lines.fromPointAndDirection(p1, p1.directionTo(p2), TEST_PRECISION).span();

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, span.closest(p1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -2), span.closest(Vector2D.of(0, -2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -2), span.closest(Vector2D.of(2, -2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -1), span.closest(Vector2D.of(-1, -1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(p2, span.closest(p2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2), span.closest(Vector2D.of(0, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2), span.closest(Vector2D.of(-2, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 1), span.closest(Vector2D.of(-1, 1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, span.closest(Vector2D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 0.5), span.closest(Vector2D.of(1, 0.5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, -0.5), span.closest(Vector2D.of(-2, -0.5)), TEST_EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        final LineConvexSubset span =
                Lines.fromPointAndDirection(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

        // act/assert
        for (double x = -10; x <= 10; x += 1) {
            EuclideanTestUtils.assertRegionLocation(span, RegionLocation.INSIDE, Vector2D.of(x, 1 + 1e-11));

            EuclideanTestUtils.assertRegionLocation(span, RegionLocation.OUTSIDE,
                    Vector2D.of(x, 0), Vector2D.of(x, 2));
        }
    }

    @Test
    public void testSplit() {
        // --- arrange
        final Vector2D pt = Vector2D.of(1, 1);

        final LineConvexSubset span = Lines.fromPointAndDirection(pt, Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

        // --- act
        Split<LineConvexSubset> split;

        // parallel
        split = span.split(Lines.fromPointAndAngle(Vector2D.of(2, 2), 0, TEST_PRECISION));
        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(span, split.getPlus());

        split = span.split(Lines.fromPointAndAngle(Vector2D.of(2, 2), Math.PI, TEST_PRECISION));
        Assertions.assertSame(span, split.getMinus());
        Assertions.assertNull(split.getPlus());

        // coincident
        split = span.split(Lines.fromPointAndDirection(pt, Vector2D.Unit.PLUS_X, TEST_PRECISION));
        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());

        // through point on line
        checkSplit(span.split(Lines.fromPointAndAngle(pt, 1, TEST_PRECISION)),
                null, pt,
                pt, null);
        checkSplit(span.split(Lines.fromPointAndAngle(pt, -1, TEST_PRECISION)),
                pt, null,
                null, pt);
    }

    @Test
    public void testGetInterval() {
        // arrange
        final LineConvexSubset span =
                Lines.fromPointAndDirection(Vector2D.of(2, -1), Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        final Interval interval = span.getInterval();

        // assert
        GeometryTestUtils.assertNegativeInfinity(interval.getMin());
        GeometryTestUtils.assertPositiveInfinity(interval.getMax());
    }

    @Test
    public void testToString() {
        // arrange
        final LineConvexSubset span =
                Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        final String str = span.toString();

        // assert
        GeometryTestUtils.assertContains("LineSpanningSubset[origin= (0", str);
        GeometryTestUtils.assertContains(", direction= (1", str);
    }

    private static void checkSplit(final Split<LineConvexSubset> split, final Vector2D minusStart, final Vector2D minusEnd,
                                   final Vector2D plusStart, final Vector2D plusEnd) {

        final LineConvexSubset minus = split.getMinus();
        if (minusStart == null && minusEnd == null) {
            Assertions.assertNull(minus);
        } else {
            checkPoint(minusStart, minus.getStartPoint());
            checkPoint(minusEnd, minus.getEndPoint());
        }


        final LineConvexSubset plus = split.getPlus();
        if (plusStart == null && plusEnd == null) {
            Assertions.assertNull(plus);
        } else {
            checkPoint(plusStart, plus.getStartPoint());
            checkPoint(plusEnd, plus.getEndPoint());
        }
    }

    private static void checkPoint(final Vector2D expected, final Vector2D pt) {
        if (expected == null) {
            Assertions.assertNull(pt);
        } else {
            EuclideanTestUtils.assertCoordinatesEqual(expected, pt, TEST_EPS);
        }
    }
}
