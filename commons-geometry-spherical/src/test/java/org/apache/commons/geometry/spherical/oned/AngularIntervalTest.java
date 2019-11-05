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
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
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
        checkFull(AngularInterval.of(0, Geometry.TWO_PI, TEST_PRECISION));
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
            AngularInterval.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, TEST_PRECISION);
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
            AngularInterval.of(Point1S.of(Double.NEGATIVE_INFINITY), Point1S.ZERO, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Point1S.ZERO, Point1S.of(Double.POSITIVE_INFINITY), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Point1S.of(Double.POSITIVE_INFINITY), Point1S.of(Double.NEGATIVE_INFINITY), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Point1S.NaN, Point1S.ZERO, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Point1S.ZERO, Point1S.NaN, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(Point1S.NaN, Point1S.NaN, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testOf_orientedPoints() {
        // arrange
        DoublePrecisionContext precisionA = new EpsilonDoublePrecisionContext(1e-3);
        DoublePrecisionContext precisionB = new EpsilonDoublePrecisionContext(1e-2);

        CutAngle zeroPos = CutAngle.createPositiveFacing(Point1S.ZERO, precisionA);
        CutAngle zeroNeg = CutAngle.createNegativeFacing(Point1S.ZERO, precisionA);

        CutAngle piPos = CutAngle.createPositiveFacing(Point1S.PI, precisionA);
        CutAngle piNeg = CutAngle.createNegativeFacing(Point1S.PI, precisionA);

        CutAngle almostPiPos = CutAngle.createPositiveFacing(Point1S.of(Geometry.PI + 5e-3), precisionB);

        // act/assert
        checkInterval(AngularInterval.of(zeroNeg, piPos), 0, Geometry.PI);
        checkInterval(AngularInterval.of(zeroPos, piNeg), Geometry.PI, Geometry.TWO_PI);

        checkFull(AngularInterval.of(zeroPos, zeroNeg));
        checkFull(AngularInterval.of(zeroPos, piPos));
        checkFull(AngularInterval.of(piNeg, zeroNeg));

        checkFull(AngularInterval.of(almostPiPos, piNeg));
        checkFull(AngularInterval.of(piNeg, almostPiPos));
    }

    @Test
    public void testOf_orientedPoints_invalidArgs() {
        // arrange
        CutAngle pt = CutAngle.createNegativeFacing(Point1S.ZERO, TEST_PRECISION);
        CutAngle nan = CutAngle.createPositiveFacing(Point1S.NaN, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(pt, nan);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(nan, pt);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.of(nan, nan);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFull() {
        // act
        AngularInterval.Convex interval = AngularInterval.full();

        // assert
        checkFull(interval);
    }

    @Test
    public void testClassify_full() {
        // arrange
        AngularInterval interval = AngularInterval.full();

        // act/assert
        for (double a = -2 * Geometry.PI; a >= 4 * Geometry.PI; a += 0.5) {
            checkClassify(interval, RegionLocation.INSIDE, Point1S.of(a));
        }
    }

    @Test
    public void testClassify_almostFull() {
        // arrange
        AngularInterval interval = AngularInterval.of(1 + 2e-10, 1, TEST_PRECISION);

        // act/assert
        checkClassify(interval, RegionLocation.BOUNDARY,
                Point1S.of(1 + 2e-10), Point1S.of(1 + 6e-11), Point1S.of(1));

        checkClassify(interval, RegionLocation.INSIDE, Point1S.of(1 + 6e-11 + Geometry.PI));

        for (double a = 1 + 1e-9; a >= 1 - 1e-9 + Geometry.TWO_PI; a += 0.5) {
            checkClassify(interval, RegionLocation.INSIDE, Point1S.of(a));
        }
    }

    @Test
    public void testClassify_sizeableGap() {
        // arrange
        AngularInterval interval = AngularInterval.of(0.25, -0.25, TEST_PRECISION);

        // act/assert
        checkClassify(interval, RegionLocation.OUTSIDE,
                Point1S.ZERO, Point1S.of(-0.2), Point1S.of(0.2));
        checkClassify(interval, RegionLocation.BOUNDARY,
                Point1S.of(-0.25), Point1S.of(0.2499999999999));
        checkClassify(interval, RegionLocation.INSIDE,
                Point1S.of(1), Point1S.PI, Point1S.of(-1));
    }

    @Test
    public void testClassify_halfPi() {
        // arrange
        AngularInterval interval = AngularInterval.of(Geometry.HALF_PI, Geometry.MINUS_HALF_PI, TEST_PRECISION);

        // act/assert
        checkClassify(interval, RegionLocation.OUTSIDE,
                Point1S.ZERO, Point1S.of(Geometry.HALF_PI - 0.1), Point1S.of(Geometry.MINUS_HALF_PI + 0.1));
        checkClassify(interval, RegionLocation.BOUNDARY,
                Point1S.of(Geometry.HALF_PI), Point1S.of(1.5 * Geometry.PI));
        checkClassify(interval, RegionLocation.INSIDE,
                Point1S.PI, Point1S.of(Geometry.HALF_PI + 0.1), Point1S.of(Geometry.MINUS_HALF_PI - 0.1));
    }

    @Test
    public void testClassify_almostEmpty() {
        // arrange
        AngularInterval interval = AngularInterval.of(1, 1 + 2e-10, TEST_PRECISION);

        // act/assert
        checkClassify(interval, RegionLocation.BOUNDARY,
                Point1S.of(1 + 2e-10), Point1S.of(1 + 6e-11), Point1S.of(1));

        checkClassify(interval, RegionLocation.OUTSIDE, Point1S.of(1 + 6e-11 + Geometry.PI));

        for (double a = 1 + 1e-9; a >= 1 - 1e-9 + Geometry.TWO_PI; a += 0.5) {
            checkClassify(interval, RegionLocation.OUTSIDE, Point1S.of(a));
        }
    }

    @Test
    public void testProject_full() {
        // arrange
        AngularInterval interval = AngularInterval.full();

        // act/assert
        Assert.assertNull(interval.project(Point1S.ZERO));
        Assert.assertNull(interval.project(Point1S.PI));
    }

    @Test
    public void testProject() {
        // arrange
        AngularInterval interval = AngularInterval.of(1, 2, TEST_PRECISION);

        // act/assert
        Assert.assertEquals(1, interval.project(Point1S.ZERO).getAzimuth(), TEST_EPS);
        Assert.assertEquals(1, interval.project(Point1S.of(1)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(1, interval.project(Point1S.of(1.5)).getAzimuth(), TEST_EPS);

        Assert.assertEquals(2, interval.project(Point1S.of(2)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(2, interval.project(Point1S.PI).getAzimuth(), TEST_EPS);
        Assert.assertEquals(2, interval.project(Point1S.of(1.4 + Geometry.PI)).getAzimuth(), TEST_EPS);

        Assert.assertEquals(1, interval.project(Point1S.of(1.5 + Geometry.PI)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(1, interval.project(Point1S.of(1.6 + Geometry.PI)).getAzimuth(), TEST_EPS);
    }

    @Test
    public void testTransform_full() {
        // arrange
        AngularInterval interval = AngularInterval.full();

        Transform1S rotate = Transform1S.createRotation(Geometry.HALF_PI);
        Transform1S invert = Transform1S.createNegation().rotate(Geometry.HALF_PI);

        // act/assert
        checkFull(interval.transform(rotate));
        checkFull(interval.transform(invert));
    }

    @Test
    public void testTransform() {
        // arrange
        AngularInterval interval = AngularInterval.of(Geometry.HALF_PI, Geometry.PI, TEST_PRECISION);

        Transform1S rotate = Transform1S.createRotation(Geometry.HALF_PI);
        Transform1S invert = Transform1S.createNegation().rotate(Geometry.HALF_PI);

        // act/assert
        checkInterval(interval.transform(rotate), Geometry.PI, 1.5 * Geometry.PI);
        checkInterval(interval.transform(invert), -0.5 * Geometry.PI, Geometry.ZERO_PI);
    }

    @Test
    public void testWrapsZero() {
        // act/assert
        Assert.assertFalse(AngularInterval.full().wrapsZero());
        Assert.assertFalse(AngularInterval.of(0, Geometry.HALF_PI, TEST_PRECISION).wrapsZero());
        Assert.assertFalse(AngularInterval.of(Geometry.HALF_PI, Geometry.PI , TEST_PRECISION).wrapsZero());
        Assert.assertFalse(AngularInterval.of(Geometry.PI, 1.5 * Geometry.PI , TEST_PRECISION).wrapsZero());
        Assert.assertFalse(AngularInterval.of(1.5 * Geometry.PI, Geometry.TWO_PI - 1e-5, TEST_PRECISION).wrapsZero());

        Assert.assertTrue(AngularInterval.of(1.5 * Geometry.PI, Geometry.TWO_PI, TEST_PRECISION).wrapsZero());
        Assert.assertTrue(AngularInterval.of(1.5 * Geometry.PI, 2.5 * Geometry.PI, TEST_PRECISION).wrapsZero());
        Assert.assertTrue(AngularInterval.of(-2.5 * Geometry.PI, -1.5 * Geometry.PI, TEST_PRECISION).wrapsZero());
    }

    @Test
    public void testToTree_full() {
        // arrange
        AngularInterval interval = AngularInterval.full();

        // act
        RegionBSPTree1S tree = interval.toTree();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.INSIDE,
                Point1S.ZERO, Point1S.of(Geometry.HALF_PI),
                Point1S.PI, Point1S.of(Geometry.MINUS_HALF_PI));
    }

    @Test
    public void testToTree_intervalEqualToPi() {
        // arrange
        AngularInterval interval = AngularInterval.of(Geometry.ZERO_PI, Geometry.PI, TEST_PRECISION);

        // act
        RegionBSPTree1S tree = interval.toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.BOUNDARY,
                Point1S.ZERO, Point1S.PI);

        checkClassify(tree, RegionLocation.INSIDE,
                Point1S.of(1e-4), Point1S.of(0.25 * Geometry.PI),
                Point1S.of(-1.25 * Geometry.PI), Point1S.of(Geometry.PI - 1e-4));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Point1S.of(-1e-4), Point1S.of(-0.25 * Geometry.PI),
                Point1S.of(1.25 * Geometry.PI), Point1S.of(-Geometry.PI + 1e-4));
    }

    @Test
    public void testToTree_intervalLessThanPi() {
        // arrange
        AngularInterval interval = AngularInterval.of(Geometry.HALF_PI, Geometry.PI, TEST_PRECISION);

        // act
        RegionBSPTree1S tree = interval.toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.BOUNDARY,
                Point1S.of(Geometry.HALF_PI), Point1S.PI);

        checkClassify(tree, RegionLocation.INSIDE,
                Point1S.of(0.51 * Geometry.PI), Point1S.of(0.75 * Geometry.PI),
                Point1S.of(0.99 * Geometry.PI));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Point1S.ZERO, Point1S.of(0.25 * Geometry.PI),
                Point1S.of(1.25 * Geometry.PI), Point1S.of(1.75 * Geometry.PI));
    }

    @Test
    public void testToTree_intervalGreaterThanPi() {
        // arrange
        AngularInterval interval = AngularInterval.of(Geometry.PI, Geometry.HALF_PI, TEST_PRECISION);

        // act
        RegionBSPTree1S tree = interval.toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.BOUNDARY,
                Point1S.of(Geometry.HALF_PI), Point1S.PI);

        checkClassify(tree, RegionLocation.INSIDE,
                Point1S.ZERO, Point1S.of(0.25 * Geometry.PI),
                Point1S.of(1.25 * Geometry.PI), Point1S.of(1.75 * Geometry.PI));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Point1S.of(0.51 * Geometry.PI), Point1S.of(0.75 * Geometry.PI),
                Point1S.of(0.99 * Geometry.PI));
    }

    @Test
    public void testToConvex_lessThanPi() {
        // arrange
        AngularInterval interval = AngularInterval.of(0, Geometry.HALF_PI, TEST_PRECISION);

        //act
        List<AngularInterval.Convex> result = interval.toConvex();

        // assert
        Assert.assertEquals(1, result.size());
        checkInterval(interval, 0, Geometry.HALF_PI);
    }

    @Test
    public void testToConvex_equalToPi() {
        // arrange
        AngularInterval interval = AngularInterval.of(Geometry.PI, Geometry.TWO_PI, TEST_PRECISION);

        //act
        List<AngularInterval.Convex> result = interval.toConvex();

        // assert
        Assert.assertEquals(1, result.size());
        checkInterval(interval, Geometry.PI, Geometry.TWO_PI);
    }

    @Test
    public void testToConvex_overPi() {
        // arrange
        AngularInterval interval = AngularInterval.of(Geometry.PI, Geometry.HALF_PI, TEST_PRECISION);

        // act
        List<AngularInterval.Convex> result = interval.toConvex();

        // assert
        Assert.assertEquals(2, result.size());
        checkInterval(result.get(0), Geometry.PI, 1.75 * Geometry.PI);
        checkInterval(result.get(1), 1.75 * Geometry.PI, 2.5 * Geometry.PI);
    }

    @Test
    public void testToConvex_overPi_splitAtZero() {
        // arrange
        AngularInterval interval = AngularInterval.of(1.25 * Geometry.PI, 2.75 * Geometry.PI, TEST_PRECISION);

        // act
        List<AngularInterval.Convex> result = interval.toConvex();

        // assert
        Assert.assertEquals(2, result.size());
        checkInterval(result.get(0), 1.25 * Geometry.PI, Geometry.TWO_PI);
        checkInterval(result.get(1), Geometry.TWO_PI, 2.75 * Geometry.PI);
    }

    @Test
    public void testSplit_full() {
        // arrange
        AngularInterval interval = AngularInterval.full();
        CutAngle pt = CutAngle.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S> split = interval.split(pt);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        checkClassify(minus, RegionLocation.BOUNDARY, Point1S.of(Geometry.HALF_PI));
        checkClassify(minus, RegionLocation.INSIDE,
                Point1S.PI, Point1S.of(Geometry.MINUS_HALF_PI), Point1S.of(-0.25 * Geometry.PI));
        checkClassify(minus, RegionLocation.OUTSIDE,
                Point1S.ZERO, Point1S.of(0.25 * Geometry.PI));

        RegionBSPTree1S plus = split.getPlus();
        checkClassify(plus, RegionLocation.BOUNDARY, Point1S.of(Geometry.HALF_PI));
        checkClassify(plus, RegionLocation.INSIDE,
                Point1S.ZERO, Point1S.of(0.25 * Geometry.PI));
        checkClassify(plus, RegionLocation.OUTSIDE,
                Point1S.PI, Point1S.of(Geometry.MINUS_HALF_PI), Point1S.of(-0.25 * Geometry.PI));
    }

    @Test
    public void testSplit_interval_both() {
        // arrange
        AngularInterval interval = AngularInterval.of(Geometry.HALF_PI, Geometry.PI, TEST_PRECISION);
        CutAngle cut = CutAngle.createNegativeFacing(0.75 * Geometry.PI, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S> split = interval.split(cut);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        checkClassify(minus, RegionLocation.BOUNDARY, Point1S.of(Geometry.PI), cut.getPoint());
        checkClassify(minus, RegionLocation.INSIDE, Point1S.of(0.8 * Geometry.PI));
        checkClassify(minus, RegionLocation.OUTSIDE,
                Point1S.ZERO, Point1S.of(Geometry.TWO_PI), Point1S.of(Geometry.MINUS_HALF_PI),
                Point1S.of(0.7 * Geometry.PI));

        RegionBSPTree1S plus = split.getPlus();
        checkClassify(plus, RegionLocation.BOUNDARY, Point1S.of(Geometry.HALF_PI), cut.getPoint());
        checkClassify(plus, RegionLocation.INSIDE, Point1S.of(0.6 * Geometry.PI));
        checkClassify(plus, RegionLocation.OUTSIDE,
                Point1S.ZERO, Point1S.of(Geometry.TWO_PI), Point1S.of(Geometry.MINUS_HALF_PI),
                Point1S.of(0.8 * Geometry.PI));
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

    @Test
    public void testConvex_of_doubles() {
        // act/assert
        checkInterval(AngularInterval.Convex.of(0, 1, TEST_PRECISION), 0, 1);
        checkInterval(AngularInterval.Convex.of(0, Geometry.PI, TEST_PRECISION), 0, Geometry.PI);
        checkInterval(AngularInterval.Convex.of(Geometry.PI + 2, 1, TEST_PRECISION), Geometry.PI + 2, Geometry.TWO_PI + 1);
        checkInterval(AngularInterval.Convex.of(-2, -1.5, TEST_PRECISION), -2, -1.5);

        checkFull(AngularInterval.Convex.of(1, 1, TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(0, 1e-11, TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(0, -1e-11, TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(0, Geometry.TWO_PI, TEST_PRECISION));
    }

    @Test
    public void testConvex_of_doubles_invalidArgs() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(0, Geometry.PI + 1e-1, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(Geometry.HALF_PI, Geometry.MINUS_HALF_PI + 1, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(0, -0.5, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testConvex_of_points() {
        // act/assert
        checkInterval(AngularInterval.Convex.of(Point1S.of(0), Point1S.of(1), TEST_PRECISION), 0, 1);
        checkInterval(AngularInterval.Convex.of(Point1S.of(0), Point1S.of(Geometry.PI), TEST_PRECISION),
                0, Geometry.PI);
        checkInterval(AngularInterval.Convex.of(Point1S.of(Geometry.PI + 2), Point1S.of(1), TEST_PRECISION),
                Geometry.PI + 2, Geometry.TWO_PI + 1);
        checkInterval(AngularInterval.Convex.of(Point1S.of(-2), Point1S.of(-1.5), TEST_PRECISION), -2, -1.5);

        checkFull(AngularInterval.Convex.of(Point1S.of(1), Point1S.of(1), TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(Point1S.of(0), Point1S.of(1e-11), TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(Point1S.of(0), Point1S.of(-1e-11), TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(Point1S.of(0), Point1S.of(Geometry.TWO_PI), TEST_PRECISION));
    }

    @Test
    public void testConvex_of_points_invalidArgs() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(Point1S.of(Double.NEGATIVE_INFINITY),
                    Point1S.of(Double.POSITIVE_INFINITY), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(Point1S.of(0), Point1S.of(Geometry.PI + 1e-1), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(Point1S.of(Geometry.HALF_PI),
                    Point1S.of(Geometry.MINUS_HALF_PI + 1), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(Point1S.of(0), Point1S.of(-0.5), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testConvex_of_cutAngles() {
        // arrange
        DoublePrecisionContext precisionA = new EpsilonDoublePrecisionContext(1e-3);
        DoublePrecisionContext precisionB = new EpsilonDoublePrecisionContext(1e-2);

        CutAngle zeroPos = CutAngle.createPositiveFacing(Point1S.ZERO, precisionA);
        CutAngle zeroNeg = CutAngle.createNegativeFacing(Point1S.ZERO, precisionA);

        CutAngle piPos = CutAngle.createPositiveFacing(Point1S.PI, precisionA);
        CutAngle piNeg = CutAngle.createNegativeFacing(Point1S.PI, precisionA);

        CutAngle almostPiPos = CutAngle.createPositiveFacing(Point1S.of(Geometry.PI + 5e-3), precisionB);

        // act/assert
        checkInterval(AngularInterval.Convex.of(zeroNeg, piPos), 0, Geometry.PI);
        checkInterval(AngularInterval.Convex.of(zeroPos, piNeg), Geometry.PI, Geometry.TWO_PI);

        checkFull(AngularInterval.Convex.of(zeroPos, zeroNeg));
        checkFull(AngularInterval.Convex.of(zeroPos, piPos));
        checkFull(AngularInterval.Convex.of(piNeg, zeroNeg));

        checkFull(AngularInterval.Convex.of(almostPiPos, piNeg));
        checkFull(AngularInterval.Convex.of(piNeg, almostPiPos));
    }

    @Test
    public void testConvex_of_cutAngles_invalidArgs() {
        // arrange
        CutAngle pt = CutAngle.createNegativeFacing(Point1S.ZERO, TEST_PRECISION);
        CutAngle nan = CutAngle.createPositiveFacing(Point1S.NaN, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(pt, nan);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(nan, pt);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(nan, nan);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            AngularInterval.Convex.of(
                    CutAngle.createNegativeFacing(1, TEST_PRECISION),
                    CutAngle.createPositiveFacing(0.5, TEST_PRECISION));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testConvex_toConvex() {
        // arrange
        AngularInterval.Convex full = AngularInterval.full();
        AngularInterval.Convex interval = AngularInterval.Convex.of(0, 1, TEST_PRECISION);

        List<AngularInterval.Convex> result;

        // act/assert
        result = full.toConvex();
        Assert.assertEquals(1, result.size());
        Assert.assertSame(full, result.get(0));

        result = interval.toConvex();
        Assert.assertEquals(1, result.size());
        Assert.assertSame(interval, result.get(0));
    }

    @Test
    public void testSplitDiameter_full() {
        // arrange
        AngularInterval.Convex full = AngularInterval.full();
        CutAngle splitter = CutAngle.createPositiveFacing(Point1S.of(Geometry.HALF_PI), TEST_PRECISION);

        // act
        Split<AngularInterval.Convex> split = full.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), 1.5 * Geometry.PI, 2.5 * Geometry.PI);
        checkInterval(split.getPlus(), 0.5 * Geometry.PI, 1.5 * Geometry.PI);
    }

    @Test
    public void testSplitDiameter_full_splitOnZero() {
        // arrange
        AngularInterval.Convex full = AngularInterval.full();
        CutAngle splitter = CutAngle.createNegativeFacing(Point1S.ZERO, TEST_PRECISION);

        // act
        Split<AngularInterval.Convex> split = full.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), 0, Geometry.PI);
        checkInterval(split.getPlus(), Geometry.PI, Geometry.TWO_PI);
    }

    @Test
    public void testSplitDiameter_minus() {
        // arrange
        AngularInterval.Convex interval = AngularInterval.Convex.of(0.1, Geometry.HALF_PI, TEST_PRECISION);
        CutAngle splitter = CutAngle.createNegativeFacing(Point1S.ZERO, TEST_PRECISION);

        // act
        Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(interval, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplitDiameter_plus() {
        // arrange
        AngularInterval.Convex interval = AngularInterval.Convex.of(-0.4 * Geometry.PI, 0.4 * Geometry.PI, TEST_PRECISION);
        CutAngle splitter = CutAngle.createNegativeFacing(Point1S.of(Geometry.HALF_PI), TEST_PRECISION);

        // act
        Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(interval, split.getPlus());
    }

    @Test
    public void testSplitDiameter_both_negativeFacingSplitter() {
        // arrange
        AngularInterval.Convex interval = AngularInterval.Convex.of(Geometry.HALF_PI, Geometry.MINUS_HALF_PI, TEST_PRECISION);
        CutAngle splitter = CutAngle.createNegativeFacing(Point1S.of(Geometry.PI), TEST_PRECISION);

        // act
        Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), Geometry.PI, 1.5 * Geometry.PI);
        checkInterval(split.getPlus(), Geometry.HALF_PI, Geometry.PI);
    }

    @Test
    public void testSplitDiameter_both_positiveFacingSplitter() {
        // arrange
        AngularInterval.Convex interval = AngularInterval.Convex.of(Geometry.HALF_PI, Geometry.MINUS_HALF_PI, TEST_PRECISION);
        CutAngle splitter = CutAngle.createPositiveFacing(Point1S.of(Geometry.PI), TEST_PRECISION);

        // act
        Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), Geometry.HALF_PI, Geometry.PI);
        checkInterval(split.getPlus(), Geometry.PI, 1.5 * Geometry.PI);
    }

    @Test
    public void testSplitDiameter_both_antipodal_negativeFacingSplitter() {
        // arrange
        AngularInterval.Convex interval = AngularInterval.Convex.of(Geometry.HALF_PI, Geometry.MINUS_HALF_PI, TEST_PRECISION);
        CutAngle splitter = CutAngle.createNegativeFacing(Point1S.ZERO, TEST_PRECISION);

        // act
        Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), Geometry.HALF_PI, Geometry.PI);
        checkInterval(split.getPlus(), Geometry.PI, 1.5 * Geometry.PI);
    }

    @Test
    public void testSplitDiameter_both_antipodal_positiveFacingSplitter() {
        // arrange
        AngularInterval.Convex interval = AngularInterval.Convex.of(Geometry.HALF_PI, Geometry.MINUS_HALF_PI, TEST_PRECISION);
        CutAngle splitter = CutAngle.createPositiveFacing(Point1S.ZERO, TEST_PRECISION);

        // act
        Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), Geometry.PI, 1.5 * Geometry.PI);
        checkInterval(split.getPlus(), Geometry.HALF_PI, Geometry.PI);
    }

    @Test
    public void testSplitDiameter_splitOnBoundary_negativeFacing() {
        // arrange
        AngularInterval.Convex interval = AngularInterval.Convex.of(Geometry.HALF_PI, Geometry.MINUS_HALF_PI, TEST_PRECISION);
        CutAngle splitter = CutAngle.createNegativeFacing(Point1S.of(Geometry.HALF_PI), TEST_PRECISION);

        // act
        Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(interval, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplitDiameter_splitOnBoundary_positiveFacing() {
        // arrange
        AngularInterval.Convex interval = AngularInterval.Convex.of(0, Geometry.PI, TEST_PRECISION);
        CutAngle splitter = CutAngle.createPositiveFacing(Point1S.of(Geometry.PI), TEST_PRECISION);

        // act
        Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(interval, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testConvex_transform() {
        // arrange
        AngularInterval.Convex interval = AngularInterval.Convex.of(Geometry.HALF_PI, Geometry.PI, TEST_PRECISION);

        Transform1S rotate = Transform1S.createRotation(Geometry.HALF_PI);
        Transform1S invert = Transform1S.createNegation().rotate(Geometry.HALF_PI);

        // act/assert
        checkInterval(interval.transform(rotate), Geometry.PI, 1.5 * Geometry.PI);
        checkInterval(interval.transform(invert), -0.5 * Geometry.PI, Geometry.ZERO_PI);
    }

    private static void checkFull(AngularInterval interval) {
        Assert.assertTrue(interval.isFull());
        Assert.assertFalse(interval.isEmpty());

        Assert.assertNull(interval.getMinBoundary());
        Assert.assertEquals(0, interval.getMin(), TEST_EPS);
        Assert.assertNull(interval.getMaxBoundary());
        Assert.assertEquals(Geometry.TWO_PI, interval.getMax(), TEST_EPS);

        Assert.assertNull(interval.getBarycenter());
        Assert.assertNull(interval.getMidpoint());

        Assert.assertEquals(Geometry.TWO_PI, interval.getSize(), TEST_EPS);
        Assert.assertEquals(0, interval.getBoundarySize(), TEST_EPS);

        checkClassify(interval, RegionLocation.INSIDE, Point1S.ZERO, Point1S.of(Geometry.PI));
    }

    private static void checkInterval(AngularInterval interval, double min, double max) {

        Assert.assertFalse(interval.isFull());
        Assert.assertFalse(interval.isEmpty());

        CutAngle minBoundary = interval.getMinBoundary();
        Assert.assertEquals(min, minBoundary.getAzimuth(), TEST_EPS);
        Assert.assertFalse(minBoundary.isPositiveFacing());

        CutAngle maxBoundary = interval.getMaxBoundary();
        Assert.assertEquals(max, maxBoundary.getAzimuth(), TEST_EPS);
        Assert.assertTrue(maxBoundary.isPositiveFacing());

        Assert.assertEquals(min, interval.getMin(), TEST_EPS);
        Assert.assertEquals(max, interval.getMax(), TEST_EPS);

        Assert.assertEquals(0.5 * (max + min), interval.getMidpoint().getAzimuth(), TEST_EPS);
        Assert.assertSame(interval.getMidpoint(), interval.getBarycenter());

        Assert.assertEquals(0, interval.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(max - min, interval.getSize(), TEST_EPS);

        checkClassify(interval, RegionLocation.INSIDE, interval.getMidpoint());
        checkClassify(interval, RegionLocation.BOUNDARY,
                interval.getMinBoundary().getPoint(), interval.getMaxBoundary().getPoint());
        checkClassify(interval, RegionLocation.OUTSIDE, Point1S.of(interval.getMidpoint().getAzimuth() + Geometry.PI));
    }

    private static void checkClassify(Region<Point1S> region, RegionLocation loc, Point1S ... pts) {
        for (Point1S pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, region.classify(pt));
        }
    }
}
