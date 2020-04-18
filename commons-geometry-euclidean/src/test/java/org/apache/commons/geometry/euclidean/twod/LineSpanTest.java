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

public class LineSpanTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testProperties() {
        // arrange
        Line line = Line.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Line.Span result = line.span();

        // assert
        Assert.assertSame(line, result.getHyperplane());
        Assert.assertSame(line, result.getLine());

        Assert.assertTrue(result.isFull());
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isInfinite());
        Assert.assertFalse(result.isFinite());

        GeometryTestUtils.assertPositiveInfinity(result.getSize());

        Assert.assertNull(result.getStartPoint());
        GeometryTestUtils.assertNegativeInfinity(result.getSubspaceStart());
        Assert.assertNull(result.getEndPoint());
        GeometryTestUtils.assertPositiveInfinity(result.getSubspaceEnd());
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix2D t = AffineTransformMatrix2D.createRotation(-0.5 * Math.PI)
                .translate(Vector2D.Unit.PLUS_X)
                .scale(1, -1);

        Line.Span span = Line.fromPointAndDirection(Vector2D.of(1, 0), Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        Line.Span result = span.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_Y, result.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testReverse() {
        // arrange
        Line.Span span = Line.fromPointAndDirection(Vector2D.of(1, 2), Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        Line.Span rev = span.reverse();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2), rev.getLine().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.MINUS_X, rev.getLine().getDirection(), TEST_EPS);
    }

    @Test
    public void testClosest() {
        // arrange
        Vector2D p1 = Vector2D.of(0, -1);
        Vector2D p2 = Vector2D.of(0, 1);
        Line.Span span = Line.fromPointAndDirection(p1, p1.directionTo(p2), TEST_PRECISION).span();

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
        Line.Span span = Line.fromPointAndDirection(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

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
        Vector2D pt = Vector2D.of(1, 1);

        Line.Span span = Line.fromPointAndDirection(pt, Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

        // --- act
        Split<ConvexSubLine> split;

        // parallel
        split = span.split(Line.fromPointAndAngle(Vector2D.of(2, 2), 0, TEST_PRECISION));
        Assert.assertNull(split.getMinus());
        Assert.assertSame(span, split.getPlus());

        split = span.split(Line.fromPointAndAngle(Vector2D.of(2, 2), Math.PI, TEST_PRECISION));
        Assert.assertSame(span, split.getMinus());
        Assert.assertNull(split.getPlus());

        // coincident
        split = span.split(Line.fromPointAndDirection(pt, Vector2D.Unit.PLUS_X, TEST_PRECISION));
        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());

        // through point on line
        checkSplit(span.split(Line.fromPointAndAngle(pt, 1, TEST_PRECISION)),
                null, pt,
                pt, null);
        checkSplit(span.split(Line.fromPointAndAngle(pt, -1, TEST_PRECISION)),
                pt, null,
                null, pt);
    }

    @Test
    public void testGetInterval() {
        // arrange
        Line.Span span = Line.fromPointAndDirection(Vector2D.of(2, -1), Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        Interval interval = span.getInterval();

        // assert
        GeometryTestUtils.assertNegativeInfinity(interval.getMin());
        GeometryTestUtils.assertPositiveInfinity(interval.getMax());
    }

    @Test
    public void testToString() {
        // arrange
        Line.Span span = Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        String str = span.toString();

        // assert
        GeometryTestUtils.assertContains("Line.Span[origin= (0", str);
        GeometryTestUtils.assertContains(", direction= (1", str);
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
