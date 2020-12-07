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

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AngularIntervalTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testOf_doubles() {
        // act/assert
        checkInterval(AngularInterval.of(0, 1, TEST_PRECISION), 0, 1);
        checkInterval(AngularInterval.of(1, 0, TEST_PRECISION), 1, PlaneAngleRadians.TWO_PI);
        checkInterval(AngularInterval.of(-2, -1.5, TEST_PRECISION), -2, -1.5);
        checkInterval(AngularInterval.of(-2, -2.5, TEST_PRECISION), -2, PlaneAngleRadians.TWO_PI - 2.5);

        checkFull(AngularInterval.of(1, 1, TEST_PRECISION));
        checkFull(AngularInterval.of(0, 1e-11, TEST_PRECISION));
        checkFull(AngularInterval.of(0, -1e-11, TEST_PRECISION));
        checkFull(AngularInterval.of(0, PlaneAngleRadians.TWO_PI, TEST_PRECISION));
    }

    @Test
    public void testOf_doubles_invalidArgs() {
        // act/assert
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(Double.NEGATIVE_INFINITY, 0, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(0, Double.POSITIVE_INFINITY, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(Double.NaN, 0, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(0, Double.NaN, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(Double.NaN, Double.NaN, TEST_PRECISION));
    }

    @Test
    public void testOf_points() {
        // act/assert
        checkInterval(AngularInterval.of(Point1S.of(0), Point1S.of(1), TEST_PRECISION), 0, 1);
        checkInterval(AngularInterval.of(Point1S.of(1), Point1S.of(0), TEST_PRECISION), 1, PlaneAngleRadians.TWO_PI);
        checkInterval(AngularInterval.of(Point1S.of(-2), Point1S.of(-1.5), TEST_PRECISION), -2, -1.5);
        checkInterval(AngularInterval.of(Point1S.of(-2), Point1S.of(-2.5), TEST_PRECISION), -2, PlaneAngleRadians.TWO_PI - 2.5);

        checkFull(AngularInterval.of(Point1S.of(1), Point1S.of(1), TEST_PRECISION));
        checkFull(AngularInterval.of(Point1S.of(0), Point1S.of(1e-11), TEST_PRECISION));
        checkFull(AngularInterval.of(Point1S.of(0), Point1S.of(-1e-11), TEST_PRECISION));
    }

    @Test
    public void testOf_points_invalidArgs() {
        // act/assert
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(Point1S.of(Double.NEGATIVE_INFINITY), Point1S.ZERO, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(Point1S.ZERO, Point1S.of(Double.POSITIVE_INFINITY), TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(Point1S.of(Double.POSITIVE_INFINITY), Point1S.of(Double.NEGATIVE_INFINITY), TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(Point1S.NaN, Point1S.ZERO, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(Point1S.ZERO, Point1S.NaN, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(Point1S.NaN, Point1S.NaN, TEST_PRECISION));
    }

    @Test
    public void testOf_orientedPoints() {
        // arrange
        final DoublePrecisionContext precisionA = new EpsilonDoublePrecisionContext(1e-3);
        final DoublePrecisionContext precisionB = new EpsilonDoublePrecisionContext(1e-2);

        final CutAngle zeroPos = CutAngles.createPositiveFacing(Point1S.ZERO, precisionA);
        final CutAngle zeroNeg = CutAngles.createNegativeFacing(Point1S.ZERO, precisionA);

        final CutAngle piPos = CutAngles.createPositiveFacing(Point1S.PI, precisionA);
        final CutAngle piNeg = CutAngles.createNegativeFacing(Point1S.PI, precisionA);

        final CutAngle almostPiPos = CutAngles.createPositiveFacing(Point1S.of(PlaneAngleRadians.PI + 5e-3), precisionB);

        // act/assert
        checkInterval(AngularInterval.of(zeroNeg, piPos), 0, PlaneAngleRadians.PI);
        checkInterval(AngularInterval.of(zeroPos, piNeg), PlaneAngleRadians.PI, PlaneAngleRadians.TWO_PI);

        checkFull(AngularInterval.of(zeroPos, zeroNeg));
        checkFull(AngularInterval.of(zeroPos, piPos));
        checkFull(AngularInterval.of(piNeg, zeroNeg));

        checkFull(AngularInterval.of(almostPiPos, piNeg));
        checkFull(AngularInterval.of(piNeg, almostPiPos));
    }

    @Test
    public void testOf_orientedPoints_invalidArgs() {
        // arrange
        final CutAngle pt = CutAngles.createNegativeFacing(Point1S.ZERO, TEST_PRECISION);
        final CutAngle nan = CutAngles.createPositiveFacing(Point1S.NaN, TEST_PRECISION);

        // act/
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(pt, nan));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(nan, pt));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.of(nan, nan));
    }

    @Test
    public void testFull() {
        // act
        final AngularInterval.Convex interval = AngularInterval.full();

        // assert
        checkFull(interval);
    }

    @Test
    public void testClassify_full() {
        // arrange
        final AngularInterval interval = AngularInterval.full();

        // act/assert
        for (double a = -2 * PlaneAngleRadians.PI; a >= 4 * PlaneAngleRadians.PI; a += 0.5) {
            checkClassify(interval, RegionLocation.INSIDE, Point1S.of(a));
        }
    }

    @Test
    public void testClassify_almostFull() {
        // arrange
        final AngularInterval interval = AngularInterval.of(1 + 2e-10, 1, TEST_PRECISION);

        // act/assert
        checkClassify(interval, RegionLocation.BOUNDARY,
                Point1S.of(1 + 2e-10), Point1S.of(1 + 6e-11), Point1S.of(1));

        checkClassify(interval, RegionLocation.INSIDE, Point1S.of(1 + 6e-11 + PlaneAngleRadians.PI));

        for (double a = 1 + 1e-9; a >= 1 - 1e-9 + PlaneAngleRadians.TWO_PI; a += 0.5) {
            checkClassify(interval, RegionLocation.INSIDE, Point1S.of(a));
        }
    }

    @Test
    public void testClassify_sizeableGap() {
        // arrange
        final AngularInterval interval = AngularInterval.of(0.25, -0.25, TEST_PRECISION);

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
        final AngularInterval interval = AngularInterval.of(PlaneAngleRadians.PI_OVER_TWO, -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);

        // act/assert
        checkClassify(interval, RegionLocation.OUTSIDE,
                Point1S.ZERO, Point1S.of(PlaneAngleRadians.PI_OVER_TWO - 0.1), Point1S.of(-PlaneAngleRadians.PI_OVER_TWO + 0.1));
        checkClassify(interval, RegionLocation.BOUNDARY,
                Point1S.of(PlaneAngleRadians.PI_OVER_TWO), Point1S.of(1.5 * PlaneAngleRadians.PI));
        checkClassify(interval, RegionLocation.INSIDE,
                Point1S.PI, Point1S.of(PlaneAngleRadians.PI_OVER_TWO + 0.1), Point1S.of(-PlaneAngleRadians.PI_OVER_TWO - 0.1));
    }

    @Test
    public void testClassify_almostEmpty() {
        // arrange
        final AngularInterval interval = AngularInterval.of(1, 1 + 2e-10, TEST_PRECISION);

        // act/assert
        checkClassify(interval, RegionLocation.BOUNDARY,
                Point1S.of(1 + 2e-10), Point1S.of(1 + 6e-11), Point1S.of(1));

        checkClassify(interval, RegionLocation.OUTSIDE, Point1S.of(1 + 6e-11 + PlaneAngleRadians.PI));

        for (double a = 1 + 1e-9; a >= 1 - 1e-9 + PlaneAngleRadians.TWO_PI; a += 0.5) {
            checkClassify(interval, RegionLocation.OUTSIDE, Point1S.of(a));
        }
    }

    @Test
    public void testProject_full() {
        // arrange
        final AngularInterval interval = AngularInterval.full();

        // act/assert
        Assertions.assertNull(interval.project(Point1S.ZERO));
        Assertions.assertNull(interval.project(Point1S.PI));
    }

    @Test
    public void testProject() {
        // arrange
        final AngularInterval interval = AngularInterval.of(1, 2, TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(1, interval.project(Point1S.ZERO).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(1, interval.project(Point1S.of(1)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(1, interval.project(Point1S.of(1.5)).getAzimuth(), TEST_EPS);

        Assertions.assertEquals(2, interval.project(Point1S.of(2)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(2, interval.project(Point1S.PI).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(2, interval.project(Point1S.of(1.4 + PlaneAngleRadians.PI)).getAzimuth(), TEST_EPS);

        Assertions.assertEquals(1, interval.project(Point1S.of(1.5 + PlaneAngleRadians.PI)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(1, interval.project(Point1S.of(1.6 + PlaneAngleRadians.PI)).getAzimuth(), TEST_EPS);
    }

    @Test
    public void testTransform_full() {
        // arrange
        final AngularInterval interval = AngularInterval.full();

        final Transform1S rotate = Transform1S.createRotation(PlaneAngleRadians.PI_OVER_TWO);
        final Transform1S invert = Transform1S.createNegation().rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        checkFull(interval.transform(rotate));
        checkFull(interval.transform(invert));
    }

    @Test
    public void testTransform() {
        // arrange
        final AngularInterval interval = AngularInterval.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI, TEST_PRECISION);

        final Transform1S rotate = Transform1S.createRotation(PlaneAngleRadians.PI_OVER_TWO);
        final Transform1S invert = Transform1S.createNegation().rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        checkInterval(interval.transform(rotate), PlaneAngleRadians.PI, 1.5 * PlaneAngleRadians.PI);
        checkInterval(interval.transform(invert), -0.5 * PlaneAngleRadians.PI, 0.0);
    }

    @Test
    public void testWrapsZero() {
        // act/assert
        Assertions.assertFalse(AngularInterval.full().wrapsZero());
        Assertions.assertFalse(AngularInterval.of(0, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION).wrapsZero());
        Assertions.assertFalse(AngularInterval.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI, TEST_PRECISION).wrapsZero());
        Assertions.assertFalse(AngularInterval.of(PlaneAngleRadians.PI, 1.5 * PlaneAngleRadians.PI, TEST_PRECISION).wrapsZero());
        Assertions.assertFalse(AngularInterval.of(1.5 * PlaneAngleRadians.PI, PlaneAngleRadians.TWO_PI - 1e-5, TEST_PRECISION).wrapsZero());

        Assertions.assertTrue(AngularInterval.of(1.5 * PlaneAngleRadians.PI, PlaneAngleRadians.TWO_PI, TEST_PRECISION).wrapsZero());
        Assertions.assertTrue(AngularInterval.of(1.5 * PlaneAngleRadians.PI, 2.5 * PlaneAngleRadians.PI, TEST_PRECISION).wrapsZero());
        Assertions.assertTrue(AngularInterval.of(-2.5 * PlaneAngleRadians.PI, -1.5 * PlaneAngleRadians.PI, TEST_PRECISION).wrapsZero());
    }

    @Test
    public void testToTree_full() {
        // arrange
        final AngularInterval interval = AngularInterval.full();

        // act
        final RegionBSPTree1S tree = interval.toTree();

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.INSIDE,
                Point1S.ZERO, Point1S.of(PlaneAngleRadians.PI_OVER_TWO),
                Point1S.PI, Point1S.of(-PlaneAngleRadians.PI_OVER_TWO));
    }

    @Test
    public void testToTree_intervalEqualToPi() {
        // arrange
        final AngularInterval interval = AngularInterval.of(0.0, PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final RegionBSPTree1S tree = interval.toTree();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.BOUNDARY,
                Point1S.ZERO, Point1S.PI);

        checkClassify(tree, RegionLocation.INSIDE,
                Point1S.of(1e-4), Point1S.of(0.25 * PlaneAngleRadians.PI),
                Point1S.of(-1.25 * PlaneAngleRadians.PI), Point1S.of(PlaneAngleRadians.PI - 1e-4));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Point1S.of(-1e-4), Point1S.of(-0.25 * PlaneAngleRadians.PI),
                Point1S.of(1.25 * PlaneAngleRadians.PI), Point1S.of(-PlaneAngleRadians.PI + 1e-4));
    }

    @Test
    public void testToTree_intervalLessThanPi() {
        // arrange
        final AngularInterval interval = AngularInterval.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final RegionBSPTree1S tree = interval.toTree();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.BOUNDARY,
                Point1S.of(PlaneAngleRadians.PI_OVER_TWO), Point1S.PI);

        checkClassify(tree, RegionLocation.INSIDE,
                Point1S.of(0.51 * PlaneAngleRadians.PI), Point1S.of(0.75 * PlaneAngleRadians.PI),
                Point1S.of(0.99 * PlaneAngleRadians.PI));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Point1S.ZERO, Point1S.of(0.25 * PlaneAngleRadians.PI),
                Point1S.of(1.25 * PlaneAngleRadians.PI), Point1S.of(1.75 * PlaneAngleRadians.PI));
    }

    @Test
    public void testToTree_intervalGreaterThanPi() {
        // arrange
        final AngularInterval interval = AngularInterval.of(PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);

        // act
        final RegionBSPTree1S tree = interval.toTree();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.BOUNDARY,
                Point1S.of(PlaneAngleRadians.PI_OVER_TWO), Point1S.PI);

        checkClassify(tree, RegionLocation.INSIDE,
                Point1S.ZERO, Point1S.of(0.25 * PlaneAngleRadians.PI),
                Point1S.of(1.25 * PlaneAngleRadians.PI), Point1S.of(1.75 * PlaneAngleRadians.PI));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Point1S.of(0.51 * PlaneAngleRadians.PI), Point1S.of(0.75 * PlaneAngleRadians.PI),
                Point1S.of(0.99 * PlaneAngleRadians.PI));
    }

    @Test
    public void testToConvex_lessThanPi() {
        // arrange
        final AngularInterval interval = AngularInterval.of(0, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);

        //act
        final List<AngularInterval.Convex> result = interval.toConvex();

        // assert
        Assertions.assertEquals(1, result.size());
        checkInterval(interval, 0, PlaneAngleRadians.PI_OVER_TWO);
    }

    @Test
    public void testToConvex_equalToPi() {
        // arrange
        final AngularInterval interval = AngularInterval.of(PlaneAngleRadians.PI, PlaneAngleRadians.TWO_PI, TEST_PRECISION);

        //act
        final List<AngularInterval.Convex> result = interval.toConvex();

        // assert
        Assertions.assertEquals(1, result.size());
        checkInterval(interval, PlaneAngleRadians.PI, PlaneAngleRadians.TWO_PI);
    }

    @Test
    public void testToConvex_overPi() {
        // arrange
        final AngularInterval interval = AngularInterval.of(PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);

        // act
        final List<AngularInterval.Convex> result = interval.toConvex();

        // assert
        Assertions.assertEquals(2, result.size());
        checkInterval(result.get(0), PlaneAngleRadians.PI, 1.75 * PlaneAngleRadians.PI);
        checkInterval(result.get(1), 1.75 * PlaneAngleRadians.PI, 2.5 * PlaneAngleRadians.PI);
    }

    @Test
    public void testToConvex_overPi_splitAtZero() {
        // arrange
        final AngularInterval interval = AngularInterval.of(1.25 * PlaneAngleRadians.PI, 2.75 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final List<AngularInterval.Convex> result = interval.toConvex();

        // assert
        Assertions.assertEquals(2, result.size());
        checkInterval(result.get(0), 1.25 * PlaneAngleRadians.PI, PlaneAngleRadians.TWO_PI);
        checkInterval(result.get(1), PlaneAngleRadians.TWO_PI, 2.75 * PlaneAngleRadians.PI);
    }

    @Test
    public void testSplit_full() {
        // arrange
        final AngularInterval interval = AngularInterval.full();
        final CutAngle pt = CutAngles.createNegativeFacing(PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = interval.split(pt);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        checkClassify(minus, RegionLocation.BOUNDARY, Point1S.of(PlaneAngleRadians.PI_OVER_TWO));
        checkClassify(minus, RegionLocation.INSIDE,
                Point1S.PI, Point1S.of(-PlaneAngleRadians.PI_OVER_TWO), Point1S.of(-0.25 * PlaneAngleRadians.PI));
        checkClassify(minus, RegionLocation.OUTSIDE,
                Point1S.ZERO, Point1S.of(0.25 * PlaneAngleRadians.PI));

        final RegionBSPTree1S plus = split.getPlus();
        checkClassify(plus, RegionLocation.BOUNDARY, Point1S.of(PlaneAngleRadians.PI_OVER_TWO));
        checkClassify(plus, RegionLocation.INSIDE,
                Point1S.ZERO, Point1S.of(0.25 * PlaneAngleRadians.PI));
        checkClassify(plus, RegionLocation.OUTSIDE,
                Point1S.PI, Point1S.of(-PlaneAngleRadians.PI_OVER_TWO), Point1S.of(-0.25 * PlaneAngleRadians.PI));
    }

    @Test
    public void testSplit_interval_both() {
        // arrange
        final AngularInterval interval = AngularInterval.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI, TEST_PRECISION);
        final CutAngle cut = CutAngles.createNegativeFacing(0.75 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = interval.split(cut);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        checkClassify(minus, RegionLocation.BOUNDARY, Point1S.of(PlaneAngleRadians.PI), cut.getPoint());
        checkClassify(minus, RegionLocation.INSIDE, Point1S.of(0.8 * PlaneAngleRadians.PI));
        checkClassify(minus, RegionLocation.OUTSIDE,
                Point1S.ZERO, Point1S.of(PlaneAngleRadians.TWO_PI), Point1S.of(-PlaneAngleRadians.PI_OVER_TWO),
                Point1S.of(0.7 * PlaneAngleRadians.PI));

        final RegionBSPTree1S plus = split.getPlus();
        checkClassify(plus, RegionLocation.BOUNDARY, Point1S.of(PlaneAngleRadians.PI_OVER_TWO), cut.getPoint());
        checkClassify(plus, RegionLocation.INSIDE, Point1S.of(0.6 * PlaneAngleRadians.PI));
        checkClassify(plus, RegionLocation.OUTSIDE,
                Point1S.ZERO, Point1S.of(PlaneAngleRadians.TWO_PI), Point1S.of(-PlaneAngleRadians.PI_OVER_TWO),
                Point1S.of(0.8 * PlaneAngleRadians.PI));
    }

    @Test
    public void testToString() {
        // arrange
        final AngularInterval interval = AngularInterval.of(1, 2, TEST_PRECISION);

        // act
        final String str = interval.toString();

        // assert
        Assertions.assertTrue(str.contains("AngularInterval"));
        Assertions.assertTrue(str.contains("min= 1.0"));
        Assertions.assertTrue(str.contains("max= 2.0"));
    }

    @Test
    public void testConvex_of_doubles() {
        // act/assert
        checkInterval(AngularInterval.Convex.of(0, 1, TEST_PRECISION), 0, 1);
        checkInterval(AngularInterval.Convex.of(0, PlaneAngleRadians.PI, TEST_PRECISION), 0, PlaneAngleRadians.PI);
        checkInterval(AngularInterval.Convex.of(PlaneAngleRadians.PI + 2, 1, TEST_PRECISION), PlaneAngleRadians.PI + 2, PlaneAngleRadians.TWO_PI + 1);
        checkInterval(AngularInterval.Convex.of(-2, -1.5, TEST_PRECISION), -2, -1.5);

        checkFull(AngularInterval.Convex.of(1, 1, TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(0, 1e-11, TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(0, -1e-11, TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(0, PlaneAngleRadians.TWO_PI, TEST_PRECISION));
    }

    @Test
    public void testConvex_of_doubles_invalidArgs() {
        // act/assert
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(0, PlaneAngleRadians.PI + 1e-1, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(PlaneAngleRadians.PI_OVER_TWO, -PlaneAngleRadians.PI_OVER_TWO + 1, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(0, -0.5, TEST_PRECISION));
    }

    @Test
    public void testConvex_of_points() {
        // act/assert
        checkInterval(AngularInterval.Convex.of(Point1S.of(0), Point1S.of(1), TEST_PRECISION), 0, 1);
        checkInterval(AngularInterval.Convex.of(Point1S.of(0), Point1S.of(PlaneAngleRadians.PI), TEST_PRECISION),
                0, PlaneAngleRadians.PI);
        checkInterval(AngularInterval.Convex.of(Point1S.of(PlaneAngleRadians.PI + 2), Point1S.of(1), TEST_PRECISION),
                PlaneAngleRadians.PI + 2, PlaneAngleRadians.TWO_PI + 1);
        checkInterval(AngularInterval.Convex.of(Point1S.of(-2), Point1S.of(-1.5), TEST_PRECISION), -2, -1.5);

        checkFull(AngularInterval.Convex.of(Point1S.of(1), Point1S.of(1), TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(Point1S.of(0), Point1S.of(1e-11), TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(Point1S.of(0), Point1S.of(-1e-11), TEST_PRECISION));
        checkFull(AngularInterval.Convex.of(Point1S.of(0), Point1S.of(PlaneAngleRadians.TWO_PI), TEST_PRECISION));
    }

    @Test
    public void testConvex_of_points_invalidArgs() {
        // act/assert
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(Point1S.of(Double.NEGATIVE_INFINITY),
                Point1S.of(Double.POSITIVE_INFINITY), TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(Point1S.of(0), Point1S.of(PlaneAngleRadians.PI + 1e-1), TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(Point1S.of(PlaneAngleRadians.PI_OVER_TWO),
                Point1S.of(-PlaneAngleRadians.PI_OVER_TWO + 1), TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(Point1S.of(0), Point1S.of(-0.5), TEST_PRECISION));
    }

    @Test
    public void testConvex_of_cutAngles() {
        // arrange
        final DoublePrecisionContext precisionA = new EpsilonDoublePrecisionContext(1e-3);
        final DoublePrecisionContext precisionB = new EpsilonDoublePrecisionContext(1e-2);

        final CutAngle zeroPos = CutAngles.createPositiveFacing(Point1S.ZERO, precisionA);
        final CutAngle zeroNeg = CutAngles.createNegativeFacing(Point1S.ZERO, precisionA);

        final CutAngle piPos = CutAngles.createPositiveFacing(Point1S.PI, precisionA);
        final CutAngle piNeg = CutAngles.createNegativeFacing(Point1S.PI, precisionA);

        final CutAngle almostPiPos = CutAngles.createPositiveFacing(Point1S.of(PlaneAngleRadians.PI + 5e-3), precisionB);

        // act/assert
        checkInterval(AngularInterval.Convex.of(zeroNeg, piPos), 0, PlaneAngleRadians.PI);
        checkInterval(AngularInterval.Convex.of(zeroPos, piNeg), PlaneAngleRadians.PI, PlaneAngleRadians.TWO_PI);

        checkFull(AngularInterval.Convex.of(zeroPos, zeroNeg));
        checkFull(AngularInterval.Convex.of(zeroPos, piPos));
        checkFull(AngularInterval.Convex.of(piNeg, zeroNeg));

        checkFull(AngularInterval.Convex.of(almostPiPos, piNeg));
        checkFull(AngularInterval.Convex.of(piNeg, almostPiPos));
    }

    @Test
    public void testConvex_of_cutAngles_invalidArgs() {
        // arrange
        final CutAngle pt = CutAngles.createNegativeFacing(Point1S.ZERO, TEST_PRECISION);
        final CutAngle nan = CutAngles.createPositiveFacing(Point1S.NaN, TEST_PRECISION);

        // act/assert
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(pt, nan));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(nan, pt));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(nan, nan));
        assertThrows(IllegalArgumentException.class, () -> AngularInterval.Convex.of(
                CutAngles.createNegativeFacing(1, TEST_PRECISION),
                CutAngles.createPositiveFacing(0.5, TEST_PRECISION)));
    }

    @Test
    public void testConvex_toConvex() {
        // arrange
        final AngularInterval.Convex full = AngularInterval.full();
        final AngularInterval.Convex interval = AngularInterval.Convex.of(0, 1, TEST_PRECISION);

        List<AngularInterval.Convex> result;

        // act/assert
        result = full.toConvex();
        Assertions.assertEquals(1, result.size());
        Assertions.assertSame(full, result.get(0));

        result = interval.toConvex();
        Assertions.assertEquals(1, result.size());
        Assertions.assertSame(interval, result.get(0));
    }

    @Test
    public void testSplitDiameter_full() {
        // arrange
        final AngularInterval.Convex full = AngularInterval.full();
        final CutAngle splitter = CutAngles.createPositiveFacing(Point1S.of(PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION);

        // act
        final Split<AngularInterval.Convex> split = full.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), 1.5 * PlaneAngleRadians.PI, 2.5 * PlaneAngleRadians.PI);
        checkInterval(split.getPlus(), 0.5 * PlaneAngleRadians.PI, 1.5 * PlaneAngleRadians.PI);
    }

    @Test
    public void testSplitDiameter_full_splitOnZero() {
        // arrange
        final AngularInterval.Convex full = AngularInterval.full();
        final CutAngle splitter = CutAngles.createNegativeFacing(Point1S.ZERO, TEST_PRECISION);

        // act
        final Split<AngularInterval.Convex> split = full.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), 0, PlaneAngleRadians.PI);
        checkInterval(split.getPlus(), PlaneAngleRadians.PI, PlaneAngleRadians.TWO_PI);
    }

    @Test
    public void testSplitDiameter_minus() {
        // arrange
        final AngularInterval.Convex interval = AngularInterval.Convex.of(0.1, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        final CutAngle splitter = CutAngles.createNegativeFacing(Point1S.ZERO, TEST_PRECISION);

        // act
        final Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assertions.assertSame(interval, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplitDiameter_plus() {
        // arrange
        final AngularInterval.Convex interval = AngularInterval.Convex.of(-0.4 * PlaneAngleRadians.PI, 0.4 * PlaneAngleRadians.PI, TEST_PRECISION);
        final CutAngle splitter = CutAngles.createNegativeFacing(Point1S.of(PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION);

        // act
        final Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(interval, split.getPlus());
    }

    @Test
    public void testSplitDiameter_both_negativeFacingSplitter() {
        // arrange
        final AngularInterval.Convex interval = AngularInterval.Convex.of(PlaneAngleRadians.PI_OVER_TWO, -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        final CutAngle splitter = CutAngles.createNegativeFacing(Point1S.of(PlaneAngleRadians.PI), TEST_PRECISION);

        // act
        final Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), PlaneAngleRadians.PI, 1.5 * PlaneAngleRadians.PI);
        checkInterval(split.getPlus(), PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI);
    }

    @Test
    public void testSplitDiameter_both_positiveFacingSplitter() {
        // arrange
        final AngularInterval.Convex interval = AngularInterval.Convex.of(PlaneAngleRadians.PI_OVER_TWO, -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        final CutAngle splitter = CutAngles.createPositiveFacing(Point1S.of(PlaneAngleRadians.PI), TEST_PRECISION);

        // act
        final Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI);
        checkInterval(split.getPlus(), PlaneAngleRadians.PI, 1.5 * PlaneAngleRadians.PI);
    }

    @Test
    public void testSplitDiameter_both_antipodal_negativeFacingSplitter() {
        // arrange
        final AngularInterval.Convex interval = AngularInterval.Convex.of(PlaneAngleRadians.PI_OVER_TWO, -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        final CutAngle splitter = CutAngles.createNegativeFacing(Point1S.ZERO, TEST_PRECISION);

        // act
        final Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI);
        checkInterval(split.getPlus(), PlaneAngleRadians.PI, 1.5 * PlaneAngleRadians.PI);
    }

    @Test
    public void testSplitDiameter_both_antipodal_positiveFacingSplitter() {
        // arrange
        final AngularInterval.Convex interval = AngularInterval.Convex.of(PlaneAngleRadians.PI_OVER_TWO, -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        final CutAngle splitter = CutAngles.createPositiveFacing(Point1S.ZERO, TEST_PRECISION);

        // act
        final Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkInterval(split.getMinus(), PlaneAngleRadians.PI, 1.5 * PlaneAngleRadians.PI);
        checkInterval(split.getPlus(), PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI);
    }

    @Test
    public void testSplitDiameter_splitOnBoundary_negativeFacing() {
        // arrange
        final AngularInterval.Convex interval = AngularInterval.Convex.of(PlaneAngleRadians.PI_OVER_TWO, -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        final CutAngle splitter = CutAngles.createNegativeFacing(Point1S.of(PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION);

        // act
        final Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assertions.assertSame(interval, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplitDiameter_splitOnBoundary_positiveFacing() {
        // arrange
        final AngularInterval.Convex interval = AngularInterval.Convex.of(0, PlaneAngleRadians.PI, TEST_PRECISION);
        final CutAngle splitter = CutAngles.createPositiveFacing(Point1S.of(PlaneAngleRadians.PI), TEST_PRECISION);

        // act
        final Split<AngularInterval.Convex> split = interval.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assertions.assertSame(interval, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testConvex_transform() {
        // arrange
        final AngularInterval.Convex interval = AngularInterval.Convex.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI, TEST_PRECISION);

        final Transform1S rotate = Transform1S.createRotation(PlaneAngleRadians.PI_OVER_TWO);
        final Transform1S invert = Transform1S.createNegation().rotate(PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        checkInterval(interval.transform(rotate), PlaneAngleRadians.PI, 1.5 * PlaneAngleRadians.PI);
        checkInterval(interval.transform(invert), -0.5 * PlaneAngleRadians.PI, 0.0);
    }

    private static void checkFull(final AngularInterval interval) {
        Assertions.assertTrue(interval.isFull());
        Assertions.assertFalse(interval.isEmpty());

        Assertions.assertNull(interval.getMinBoundary());
        Assertions.assertEquals(0, interval.getMin(), TEST_EPS);
        Assertions.assertNull(interval.getMaxBoundary());
        Assertions.assertEquals(PlaneAngleRadians.TWO_PI, interval.getMax(), TEST_EPS);

        Assertions.assertNull(interval.getCentroid());
        Assertions.assertNull(interval.getMidPoint());

        Assertions.assertEquals(PlaneAngleRadians.TWO_PI, interval.getSize(), TEST_EPS);
        Assertions.assertEquals(0, interval.getBoundarySize(), TEST_EPS);

        checkClassify(interval, RegionLocation.INSIDE, Point1S.ZERO, Point1S.of(PlaneAngleRadians.PI));
    }

    private static void checkInterval(final AngularInterval interval, final double min, final double max) {

        Assertions.assertFalse(interval.isFull());
        Assertions.assertFalse(interval.isEmpty());

        final CutAngle minBoundary = interval.getMinBoundary();
        Assertions.assertEquals(min, minBoundary.getAzimuth(), TEST_EPS);
        Assertions.assertFalse(minBoundary.isPositiveFacing());

        final CutAngle maxBoundary = interval.getMaxBoundary();
        Assertions.assertEquals(max, maxBoundary.getAzimuth(), TEST_EPS);
        Assertions.assertTrue(maxBoundary.isPositiveFacing());

        Assertions.assertEquals(min, interval.getMin(), TEST_EPS);
        Assertions.assertEquals(max, interval.getMax(), TEST_EPS);

        Assertions.assertEquals(0.5 * (max + min), interval.getMidPoint().getAzimuth(), TEST_EPS);
        Assertions.assertSame(interval.getMidPoint(), interval.getCentroid());

        Assertions.assertEquals(0, interval.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(max - min, interval.getSize(), TEST_EPS);

        checkClassify(interval, RegionLocation.INSIDE, interval.getMidPoint());
        checkClassify(interval, RegionLocation.BOUNDARY,
                interval.getMinBoundary().getPoint(), interval.getMaxBoundary().getPoint());
        checkClassify(interval, RegionLocation.OUTSIDE, Point1S.of(interval.getMidPoint().getAzimuth() + PlaneAngleRadians.PI));
    }

    private static void checkClassify(final Region<Point1S> region, final RegionLocation loc, final Point1S... pts) {
        for (final Point1S pt : pts) {
            Assertions.assertEquals(loc, region.classify(pt), "Unexpected location for point " + pt);
        }
    }
}
