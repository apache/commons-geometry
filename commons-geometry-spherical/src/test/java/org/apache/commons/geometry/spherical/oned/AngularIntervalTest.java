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
package org.apache.commons.geometry.spherical.oned;

import java.util.List;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class AngularIntervalTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testOf_doubles() {
        // act/assert
        checkInterval(AngularInterval.of(0, 1, TEST_PRECISION), 0, 1);
        checkInterval(AngularInterval.of(1, 0, TEST_PRECISION), 1, Geometry.TWO_PI);
        checkInterval(AngularInterval.of(-2, -1.5, TEST_PRECISION), -2, -1.5);
        checkInterval(AngularInterval.of(-2, -2.5, TEST_PRECISION), -2, Geometry.TWO_PI - 2.5);

        checkFull(AngularInterval.of(1, 1, TEST_PRECISION));
        checkFull(AngularInterval.of(0, 1e-11, TEST_PRECISION));
        checkFull(AngularInterval.of(0, -1e-11, TEST_PRECISION));
    }

    @Test
    public void testOf_doubles_invalidArgs() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Double.NEGATIVE_INFINITY, 0, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(0, Double.POSITIVE_INFINITY, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Double.NaN, 0, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(0, Double.NaN, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Double.NaN, Double.NaN, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testOf_points() {
        // act/assert
        checkInterval(AngularInterval.of(Point1S.of(0), Point1S.of(1), TEST_PRECISION), 0, 1);
        checkInterval(AngularInterval.of(Point1S.of(1), Point1S.of(0), TEST_PRECISION), 1, Geometry.TWO_PI);
        checkInterval(AngularInterval.of(Point1S.of(-2), Point1S.of(-1.5), TEST_PRECISION), -2, -1.5);
        checkInterval(AngularInterval.of(Point1S.of(-2), Point1S.of(-2.5), TEST_PRECISION), -2, Geometry.TWO_PI - 2.5);

        checkFull(AngularInterval.of(Point1S.of(1), Point1S.of(1), TEST_PRECISION));
        checkFull(AngularInterval.of(Point1S.of(0), Point1S.of(1e-11), TEST_PRECISION));
        checkFull(AngularInterval.of(Point1S.of(0), Point1S.of(-1e-11), TEST_PRECISION));
    }

    @Test
    public void testOf_points_invalidArgs() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Point1S.of(Double.NEGATIVE_INFINITY), Point1S.ZERO_PI, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Point1S.ZERO_PI, Point1S.of(Double.POSITIVE_INFINITY), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Point1S.of(Double.NEGATIVE_INFINITY), Point1S.of(Double.POSITIVE_INFINITY), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Point1S.NaN, Point1S.ZERO_PI, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Point1S.ZERO_PI, Point1S.NaN, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Point1S.NaN, Point1S.NaN, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFull() {
        // act
        AngularInterval interval = AngularInterval.full();

        // assert
        checkFull(interval);
    }

    @Test
    public void testToString() {
        // arrange
        AngularInterval interval = AngularInterval.of(1, 2, TEST_PRECISION);

        // act
        String str = interval.toString();

        // assert
        Assert.assertTrue(str.contains("AngularInterval"));
        Assert.assertTrue(str.contains("min= 1.0"));
        Assert.assertTrue(str.contains("max= 2.0"));
    }

    private static void checkFull(AngularInterval interval) {
        Assert.assertTrue(interval.isFull());
        Assert.assertFalse(interval.isEmpty());

        Assert.assertNull(interval.getMinBoundary());
        GeometryTestUtils.assertNegativeInfinity(interval.getMin());
        Assert.assertNull(interval.getMaxBoundary());
        GeometryTestUtils.assertPositiveInfinity(interval.getMax());

        Assert.assertNull(interval.getBarycenter());
        Assert.assertNull(interval.getMidpoint());

        GeometryTestUtils.assertPositiveInfinity(interval.getSize());
        Assert.assertEquals(0, interval.getBoundarySize(), TEST_EPS);

        checkPoints(interval, RegionLocation.INSIDE, Point1S.ZERO_PI, Point1S.of(Geometry.PI));
    }

    private static void checkInterval(AngularInterval interval, double min, double max) {

        Assert.assertFalse(interval.isFull());
        Assert.assertFalse(interval.isEmpty());

        OrientedPoint1S minBoundary = interval.getMinBoundary();
        Assert.assertEquals(min, minBoundary.getAzimuth(), TEST_EPS);
        Assert.assertFalse(minBoundary.isPositiveFacing());

        OrientedPoint1S maxBoundary = interval.getMaxBoundary();
        Assert.assertEquals(max, maxBoundary.getAzimuth(), TEST_EPS);
        Assert.assertTrue(maxBoundary.isPositiveFacing());

        Assert.assertEquals(min, interval.getMin(), TEST_EPS);
        Assert.assertEquals(max, interval.getMax(), TEST_EPS);

        Assert.assertEquals(0.5 * (max + min), interval.getMidpoint().getAzimuth(), TEST_EPS);
        Assert.assertSame(interval.getMidpoint(), interval.getBarycenter());

        Assert.assertEquals(0, interval.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(max - min, interval.getSize(), TEST_EPS);

        List<AngularInterval> convex = interval.toConvex();
        Assert.assertEquals(1, convex.size());
        Assert.assertSame(interval, convex.get(0));

        checkPoints(interval, RegionLocation.INSIDE, interval.getMidpoint());
        checkPoints(interval, RegionLocation.BOUNDARY,
                interval.getMinBoundary().getPoint(), interval.getMaxBoundary().getPoint());
        checkPoints(interval, RegionLocation.OUTSIDE, Point1S.of(interval.getMidpoint().getAzimuth() + Geometry.PI));
    }

    private static void checkPoints(AngularInterval interval, RegionLocation loc, Point1S ... pts) {
        for (Point1S pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, interval.classify(pt));
        }
    }
}
