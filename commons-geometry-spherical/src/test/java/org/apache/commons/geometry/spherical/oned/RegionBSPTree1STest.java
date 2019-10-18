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
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class RegionBSPTree1STest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Transform1S HALF_PI_PLUS_AZ = Transform1S.createRotation(Geometry.HALF_PI);

    private static final Transform1S PI_MINUS_AZ = Transform1S.createNegation().rotate(Geometry.PI);

    @Test
    public void testConstructor_default() {
        // act
        RegionBSPTree1S tree = new RegionBSPTree1S();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());

        Assert.assertEquals(0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertNull(tree.getBarycenter());
    }

    @Test
    public void testConstructor_true() {
        // act
        RegionBSPTree1S tree = new RegionBSPTree1S(true);

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(Geometry.TWO_PI, tree.getSize(), TEST_EPS);
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertNull(tree.getBarycenter());
    }

    @Test
    public void testConstructor_false() {
        // act
        RegionBSPTree1S tree = new RegionBSPTree1S(false);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());

        Assert.assertEquals(0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertNull(tree.getBarycenter());
    }

    @Test
    public void testFull() {
        // act
        RegionBSPTree1S tree = RegionBSPTree1S.full();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(Geometry.TWO_PI, tree.getSize(), TEST_EPS);
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertNull(tree.getBarycenter());
    }

    @Test
    public void testEmpty() {
        // act
        RegionBSPTree1S tree = RegionBSPTree1S.empty();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());

        Assert.assertEquals(0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertNull(tree.getBarycenter());
    }

    @Test
    public void testCopy() {
        // arrange
        RegionBSPTree1S orig = RegionBSPTree1S.fromInterval(AngularInterval.of(0, Geometry.PI, TEST_PRECISION));

        // act
        RegionBSPTree1S copy = orig.copy();

        // assert
        Assert.assertNotSame(orig, copy);

        orig.setEmpty();

        checkSingleInterval(copy, 0, Geometry.PI);
    }

    @Test
    public void testFromInterval_full() {
        // act
        RegionBSPTree1S tree = RegionBSPTree1S.fromInterval(AngularInterval.full());

        // assert
        Assert.assertTrue(tree.isFull());
    }

    @Test
    public void testFromInterval_nonFull() {
        for (double theta = Geometry.ZERO_PI; theta <= Geometry.TWO_PI; theta += 0.2) {
            // arrange
            double min = theta;
            double max = theta + Geometry.HALF_PI;

            // act
            RegionBSPTree1S tree = RegionBSPTree1S.fromInterval(AngularInterval.of(min, max, TEST_PRECISION));

            checkSingleInterval(tree, min, max);

            Assert.assertEquals(Geometry.HALF_PI, tree.getSize(), TEST_EPS);
            Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
            Assert.assertEquals(PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(theta + (0.25 * Geometry.PI)),
                    tree.getBarycenter().getNormalizedAzimuth(), TEST_EPS);
        }
    }

    @Test
    public void testClassify_full() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.full();

        // act/assert
        for (double az = -Geometry.TWO_PI; az <= 2 * Geometry.TWO_PI; az += 0.2) {
            checkClassify(tree, RegionLocation.INSIDE, az);
        }
    }

    @Test
    public void testClassify_empty() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();

        // act/assert
        for (double az = -Geometry.TWO_PI; az <= 2 * Geometry.TWO_PI; az += 0.2) {
            checkClassify(tree, RegionLocation.OUTSIDE, az);
        }
    }

    @Test
    public void testClassify() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.fromInterval(
                AngularInterval.of(Geometry.MINUS_HALF_PI, Geometry.HALF_PI, TEST_PRECISION));

        // act/assert
        checkClassify(tree, RegionLocation.BOUNDARY,
                Geometry.MINUS_HALF_PI, Geometry.HALF_PI,
                Geometry.MINUS_HALF_PI - Geometry.TWO_PI, Geometry.HALF_PI + Geometry.TWO_PI);
        checkClassify(tree, RegionLocation.INSIDE,
                Geometry.ZERO_PI, 0.5, -0.5,
                Geometry.TWO_PI, 0.5 + Geometry.TWO_PI, -0.5 - Geometry.TWO_PI);
        checkClassify(tree, RegionLocation.OUTSIDE,
                Geometry.PI, Geometry.PI + 0.5, Geometry.PI - 0.5,
                Geometry.PI + Geometry.TWO_PI, Geometry.PI + 0.5 + Geometry.TWO_PI,
                Geometry.PI - 0.5 + Geometry.TWO_PI);
    }

    @Test
    public void testToIntervals_full() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.full();

        // act
        List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(1, intervals.size());

        AngularInterval interval = intervals.get(0);
        Assert.assertTrue(interval.isFull());
    }

    @Test
    public void testToIntervals_empty() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();

        // act
        List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(0, intervals.size());
    }

    @Test
    public void testToIntervals_singleCut() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();

        for (double theta = 0; theta <= Geometry.TWO_PI; theta += 0.2) {
            // act/assert
            tree.setEmpty();
            tree.getRoot().cut(CutAngle.createPositiveFacing(theta, TEST_PRECISION));

            checkSingleInterval(tree, 0, theta);

            tree.setEmpty();
            tree.getRoot().cut(CutAngle.createNegativeFacing(theta, TEST_PRECISION));

            checkSingleInterval(tree, theta, Geometry.TWO_PI);
        }
    }

    @Test
    public void testToIntervals_wrapAround_joinedIntervalsOnPositiveSide() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(0.25 * Geometry.PI, Geometry.HALF_PI, TEST_PRECISION));
        tree.add(AngularInterval.of(1.5 * Geometry.PI, 0.25 * Geometry.PI, TEST_PRECISION));

        // act
        List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(1, intervals.size());

        checkInterval(intervals.get(0), 1.5 * Geometry.PI, Geometry.HALF_PI);
    }

    @Test
    public void testToIntervals_wrapAround_joinedIntervalsOnNegativeSide() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(1.75 * Geometry.PI, Geometry.HALF_PI, TEST_PRECISION));
        tree.add(AngularInterval.of(1.5 * Geometry.PI, 1.75 * Geometry.PI, TEST_PRECISION));

        // act
        List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(1, intervals.size());

        checkInterval(intervals.get(0), 1.5 * Geometry.PI, Geometry.HALF_PI);
    }

    @Test
    public void testToIntervals_multipleIntervals() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(Geometry.MINUS_HALF_PI, Geometry.HALF_PI, TEST_PRECISION));
        tree.add(AngularInterval.of(Geometry.PI - 0.5, Geometry.PI, TEST_PRECISION));
        tree.add(AngularInterval.of(Geometry.PI, Geometry.PI + 0.5, TEST_PRECISION));

        // act
        List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(2, intervals.size());

        checkInterval(intervals.get(0), Geometry.PI - 0.5, Geometry.PI + 0.5);
        checkInterval(intervals.get(1), Geometry.MINUS_HALF_PI, Geometry.HALF_PI);
    }

    @Test
    public void testToIntervals_multipleIntervals_complement() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(Geometry.MINUS_HALF_PI, Geometry.HALF_PI, TEST_PRECISION));
        tree.add(AngularInterval.of(Geometry.PI - 0.5, Geometry.PI, TEST_PRECISION));
        tree.add(AngularInterval.of(Geometry.PI, Geometry.PI + 0.5, TEST_PRECISION));

        tree.complement();

        // act
        List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(2, intervals.size());

        checkInterval(intervals.get(0), Geometry.HALF_PI, Geometry.PI - 0.5);
        checkInterval(intervals.get(1), Geometry.PI + 0.5, Geometry.MINUS_HALF_PI);
    }

    @Test
    public void testSplit_empty() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();

        // act/assert
        Assert.assertEquals(SplitLocation.NEITHER,
                tree.split(CutAngle.createPositiveFacing(0, TEST_PRECISION)).getLocation());
        Assert.assertEquals(SplitLocation.NEITHER,
                tree.split(CutAngle.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION)).getLocation());
        Assert.assertEquals(SplitLocation.NEITHER,
                tree.split(CutAngle.createPositiveFacing(Geometry.PI, TEST_PRECISION)).getLocation());
        Assert.assertEquals(SplitLocation.NEITHER,
                tree.split(CutAngle.createNegativeFacing(Geometry.MINUS_HALF_PI, TEST_PRECISION)).getLocation());
        Assert.assertEquals(SplitLocation.NEITHER,
                tree.split(CutAngle.createPositiveFacing(Geometry.TWO_PI, TEST_PRECISION)).getLocation());
    }

    @Test
    public void testSplit_full() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.full();

        // act/assert
        checkSimpleSplit(
                tree.split(CutAngle.createPositiveFacing(1e-6, TEST_PRECISION)),
                AngularInterval.of(0, 1e-6, TEST_PRECISION),
                AngularInterval.of(1e-6, Geometry.TWO_PI, TEST_PRECISION)
            );
        checkSimpleSplit(
                tree.split(CutAngle.createNegativeFacing(Geometry.HALF_PI, TEST_PRECISION)),
                AngularInterval.of(Geometry.HALF_PI, Geometry.TWO_PI, TEST_PRECISION),
                AngularInterval.of(0, Geometry.HALF_PI, TEST_PRECISION)
            );
        checkSimpleSplit(
                tree.split(CutAngle.createPositiveFacing(Geometry.PI, TEST_PRECISION)),
                AngularInterval.of(0, Geometry.PI, TEST_PRECISION),
                AngularInterval.of(Geometry.PI, Geometry.TWO_PI, TEST_PRECISION)
            );
        checkSimpleSplit(
                tree.split(CutAngle.createNegativeFacing(Geometry.MINUS_HALF_PI, TEST_PRECISION)),
                AngularInterval.of(Geometry.MINUS_HALF_PI, Geometry.TWO_PI, TEST_PRECISION),
                AngularInterval.of(0, Geometry.MINUS_HALF_PI, TEST_PRECISION)
            );
        checkSimpleSplit(
                tree.split(CutAngle.createPositiveFacing(Geometry.TWO_PI - 1e-6, TEST_PRECISION)),
                AngularInterval.of(0, Geometry.TWO_PI - 1e-6, TEST_PRECISION),
                AngularInterval.of(Geometry.TWO_PI - 1e-6, Geometry.TWO_PI, TEST_PRECISION)
            );
    }

    @Test
    public void testSplit_full_cutEquivalentToZero() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.full();

        AngularInterval twoPi = AngularInterval.of(0, Geometry.TWO_PI, TEST_PRECISION);

        // act/assert
        checkSimpleSplit(
                tree.split(CutAngle.createPositiveFacing(0, TEST_PRECISION)),
                null,
                twoPi
            );
        checkSimpleSplit(
                tree.split(CutAngle.createNegativeFacing(0, TEST_PRECISION)),
                twoPi,
                null
            );

        checkSimpleSplit(
                tree.split(CutAngle.createPositiveFacing(Geometry.TWO_PI - 1e-18, TEST_PRECISION)),
                null,
                twoPi
            );
        checkSimpleSplit(
                tree.split(CutAngle.createNegativeFacing(Geometry.TWO_PI - 1e-18, TEST_PRECISION)),
                twoPi,
                null
            );
    }

    @Test
    public void testSplit_singleInterval() {
        // arrange
        AngularInterval interval = AngularInterval.of(Geometry.HALF_PI, Geometry.MINUS_HALF_PI, TEST_PRECISION);
        RegionBSPTree1S tree = interval.toTree();

        // act
        checkSimpleSplit(
                tree.split(CutAngle.createNegativeFacing(0, TEST_PRECISION)),
                interval,
                null
            );
        checkSimpleSplit(
                tree.split(CutAngle.createNegativeFacing(-Geometry.TWO_PI, TEST_PRECISION)),
                interval,
                null
            );

        checkSimpleSplit(
                tree.split(CutAngle.createPositiveFacing(Geometry.TWO_PI + Geometry.HALF_PI, TEST_PRECISION)),
                null,
                interval
            );
        checkSimpleSplit(
                tree.split(CutAngle.createPositiveFacing(1.5 * Geometry.PI, TEST_PRECISION)),
                interval,
                null
            );

        checkSimpleSplit(
                tree.split(CutAngle.createNegativeFacing(Geometry.PI, TEST_PRECISION)),
                AngularInterval.of(Geometry.PI, Geometry.MINUS_HALF_PI, TEST_PRECISION),
                AngularInterval.of(Geometry.HALF_PI, Geometry.PI, TEST_PRECISION)
            );
    }

    @Test
    public void testSplit_singleIntervalSplitIntoTwoIntervalsOnSameSide() {
        // arrange
        RegionBSPTree1S tree = AngularInterval.of(Geometry.MINUS_HALF_PI, Geometry.HALF_PI, TEST_PRECISION).toTree();

        CutAngle cut = CutAngle.createPositiveFacing(0, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S> split = tree.split(cut);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        Assert.assertNull(minus);

        RegionBSPTree1S plus = split.getPlus();
        List<AngularInterval> plusIntervals = plus.toIntervals();
        Assert.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), Geometry.MINUS_HALF_PI, Geometry.HALF_PI);
    }

    @Test
    public void testSplit_multipleRegions() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(Geometry.TWO_PI - 1, Geometry.HALF_PI, TEST_PRECISION));
        tree.add(AngularInterval.of(Geometry.PI, Geometry.MINUS_HALF_PI, TEST_PRECISION));

        CutAngle cut = CutAngle.createNegativeFacing(1, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S> split = tree.split(cut);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        List<AngularInterval> minusIntervals = minus.toIntervals();
        Assert.assertEquals(3, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 1, Geometry.HALF_PI);
        checkInterval(minusIntervals.get(1), Geometry.PI, Geometry.MINUS_HALF_PI);
        checkInterval(minusIntervals.get(2), Geometry.TWO_PI - 1, 0);

        RegionBSPTree1S plus = split.getPlus();
        List<AngularInterval> plusIntervals = plus.toIntervals();
        Assert.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 0, 1);
    }

    @Test
    public void testSplitDiameter_full() {
        // arrange
        RegionBSPTree1S full = RegionBSPTree1S.full();
        CutAngle splitter = CutAngle.createPositiveFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S> split = full.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        List<AngularInterval> minusIntervals = minus.toIntervals();
        Assert.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 1.5 * Geometry.PI, 2.5 * Geometry.PI);

        RegionBSPTree1S plus = split.getPlus();
        List<AngularInterval> plusIntervals = plus.toIntervals();
        Assert.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), Geometry.HALF_PI, 1.5 * Geometry.PI);
    }

    @Test
    public void testSplitDiameter_empty() {
        // arrange
        RegionBSPTree1S empty = RegionBSPTree1S.empty();
        CutAngle splitter = CutAngle.createPositiveFacing(Geometry.HALF_PI, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S> split = empty.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        Assert.assertNull(minus);

        RegionBSPTree1S plus = split.getPlus();
        Assert.assertNull(plus);
    }

    @Test
    public void testSplitDiameter_minus_zeroOnMinusSide() {
        // arrange
        RegionBSPTree1S tree = AngularInterval.of(0, 1, TEST_PRECISION).toTree();
        CutAngle splitter = CutAngle.createPositiveFacing(1, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S>split = tree.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        List<AngularInterval> minusIntervals = minus.toIntervals();
        Assert.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 0, 1);

        RegionBSPTree1S plus = split.getPlus();
        Assert.assertNull(plus);
    }

    @Test
    public void testSplitDiameter_minus_zeroOnPlusSide() {
        // arrange
        RegionBSPTree1S tree = AngularInterval.of(1, 2, TEST_PRECISION).toTree();
        CutAngle splitter = CutAngle.createNegativeFacing(0, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S>split = tree.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        List<AngularInterval> minusIntervals = minus.toIntervals();
        Assert.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 1, 2);

        RegionBSPTree1S plus = split.getPlus();
        Assert.assertNull(plus);
    }

    @Test
    public void testSplitDiameter_plus_zeroOnMinusSide() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(1, 1.1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 2.1, TEST_PRECISION));

        CutAngle splitter = CutAngle.createPositiveFacing(1, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S>split = tree.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        Assert.assertNull(minus);

        RegionBSPTree1S plus = split.getPlus();
        List<AngularInterval> plusIntervals = plus.toIntervals();
        Assert.assertEquals(2, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1.1);
        checkInterval(plusIntervals.get(1), 2, 2.1);
    }

    @Test
    public void testSplitDiameter_plus_zeroOnPlusSide() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(1, 1.1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 2.1, TEST_PRECISION));

        CutAngle splitter = CutAngle.createNegativeFacing(Geometry.PI - 1, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S>split = tree.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        Assert.assertNull(minus);

        RegionBSPTree1S plus = split.getPlus();
        List<AngularInterval> plusIntervals = plus.toIntervals();
        Assert.assertEquals(2, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1.1);
        checkInterval(plusIntervals.get(1), 2, 2.1);
    }

    @Test
    public void testSplitDiameter_both_zeroOnMinusSide() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(1, 1.1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 3, TEST_PRECISION));

        CutAngle splitter = CutAngle.createPositiveFacing(2.5, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S>split = tree.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        List<AngularInterval> plusIntervals = minus.toIntervals();
        Assert.assertEquals(2, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1.1);
        checkInterval(plusIntervals.get(1), 2, 2.5);

        RegionBSPTree1S plus = split.getPlus();
        List<AngularInterval> minusIntervals = plus.toIntervals();
        Assert.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 2.5, 3);
    }

    @Test
    public void testSplitDiameter_both_zeroOnPlusSide() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(1, 1.1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 3, TEST_PRECISION));

        CutAngle splitter = CutAngle.createNegativeFacing(2.5, TEST_PRECISION);

        // act
        Split<RegionBSPTree1S>split = tree.splitDiameter(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        RegionBSPTree1S minus = split.getMinus();
        List<AngularInterval> minusIntervals = minus.toIntervals();
        Assert.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 2.5, 3);

        RegionBSPTree1S plus = split.getPlus();
        List<AngularInterval> plusIntervals = plus.toIntervals();
        Assert.assertEquals(2, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1.1);
        checkInterval(plusIntervals.get(1), 2, 2.5);
    }

    @Test
    public void testRegionProperties_singleInterval_wrapsZero() {
        // arrange
        RegionBSPTree1S tree = AngularInterval.of(Geometry.MINUS_HALF_PI, Geometry.PI,
                TEST_PRECISION).toTree();

        // act/assert
        Assert.assertEquals(1.5 * Geometry.PI, tree.getSize(), TEST_EPS);
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.25 * Geometry.PI, tree.getBarycenter().getAzimuth(), TEST_EPS);
    }

    @Test
    public void testRegionProperties_singleInterval_doesNotWrap() {
        // arrange
        RegionBSPTree1S tree = AngularInterval.of(Geometry.HALF_PI, Geometry.TWO_PI,
                TEST_PRECISION).toTree();

        // act/assert
        Assert.assertEquals(1.5 * Geometry.PI, tree.getSize(), TEST_EPS);
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(1.25 * Geometry.PI, tree.getBarycenter().getAzimuth(), TEST_EPS);
    }

    @Test
    public void testRegionProperties_multipleIntervals_sameSize() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(0, 0.1, TEST_PRECISION));
        tree.add(AngularInterval.of(0.2, 0.3, TEST_PRECISION));

        // act/assert
        Assert.assertEquals(0.2, tree.getSize(), TEST_EPS);
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.15, tree.getBarycenter().getAzimuth(), TEST_EPS);
    }

    @Test
    public void testRegionProperties_multipleIntervals_differentSizes() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(0, 0.2, TEST_PRECISION));
        tree.add(AngularInterval.of(0.3, 0.7, TEST_PRECISION));

        // act/assert
        Assert.assertEquals(0.6, tree.getSize(), TEST_EPS);
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(2.2 / 6, tree.getBarycenter().getAzimuth(), TEST_EPS);
    }

    @Test
    public void testTransform_fullAndEmpty() {
        // arrange
        RegionBSPTree1S full = RegionBSPTree1S.full();
        RegionBSPTree1S empty = RegionBSPTree1S.empty();

        // act
        full.transform(PI_MINUS_AZ);
        empty.transform(HALF_PI_PLUS_AZ);

        // assert
        Assert.assertTrue(full.isFull());
        Assert.assertFalse(full.isEmpty());

        Assert.assertFalse(empty.isFull());
        Assert.assertTrue(empty.isEmpty());
    }

    @Test
    public void testTransform_halfPiPlusAz() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(-1, 1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 3, TEST_PRECISION));

        // act
        tree.transform(HALF_PI_PLUS_AZ);

        // assert
        Assert.assertEquals(3, tree.getSize(), TEST_EPS);

        List<AngularInterval> intervals = tree.toIntervals();

        Assert.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Geometry.HALF_PI - 1, Geometry.HALF_PI + 1);
        checkInterval(intervals.get(1), Geometry.HALF_PI + 2, Geometry.HALF_PI + 3);
    }

    @Test
    public void testTransform_piMinusAz() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(-1, 1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 3, TEST_PRECISION));

        // act
        tree.transform(PI_MINUS_AZ);

        // assert
        Assert.assertEquals(3, tree.getSize(), TEST_EPS);

        List<AngularInterval> intervals = tree.toIntervals();

        Assert.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Geometry.PI - 3, Geometry.PI - 2);
        checkInterval(intervals.get(1), Geometry.PI - 1, Geometry.PI + 1);
    }

    @Test
    public void testProject_fullAndEmpty() {
        // arrange
        RegionBSPTree1S full = RegionBSPTree1S.full();
        RegionBSPTree1S empty = RegionBSPTree1S.empty();

        // act/assert
        Assert.assertNull(full.project(Point1S.ZERO));
        Assert.assertNull(full.project(Point1S.PI));

        Assert.assertNull(empty.project(Point1S.ZERO));
        Assert.assertNull(empty.project(Point1S.PI));
    }

    @Test
    public void testProject_withIntervals() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(Geometry.MINUS_HALF_PI, Geometry.HALF_PI, TEST_PRECISION));
        tree.add(AngularInterval.of(Geometry.PI - 1, Geometry.PI + 1, TEST_PRECISION));

        // act/assert
        Assert.assertEquals(Geometry.MINUS_HALF_PI,
                tree.project(Point1S.of(Geometry.MINUS_HALF_PI - 0.1)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(Geometry.MINUS_HALF_PI,
                tree.project(Point1S.of(Geometry.MINUS_HALF_PI)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(Geometry.MINUS_HALF_PI,
                tree.project(Point1S.of(Geometry.MINUS_HALF_PI + 0.1)).getAzimuth(), TEST_EPS);

        Assert.assertEquals(Geometry.MINUS_HALF_PI, tree.project(Point1S.of(-0.1)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(Geometry.HALF_PI, tree.project(Point1S.ZERO).getAzimuth(), TEST_EPS);
        Assert.assertEquals(Geometry.HALF_PI, tree.project(Point1S.of(0.1)).getAzimuth(), TEST_EPS);

        Assert.assertEquals(Geometry.PI - 1,
                tree.project(Point1S.of(Geometry.PI - 0.5)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(Geometry.PI + 1,
                tree.project(Point1S.of(Geometry.PI + 0.5)).getAzimuth(), TEST_EPS);
    }

    @Test
    public void testProject_equidistant() {
        // arrange
        RegionBSPTree1S tree = AngularInterval.of(1, 2, TEST_PRECISION).toTree();
        RegionBSPTree1S treeComplement = tree.copy();
        treeComplement.complement();

        // act/assert
        Assert.assertEquals(1, tree.project(Point1S.of(1.5)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(1, treeComplement.project(Point1S.of(1.5)).getAzimuth(), TEST_EPS);
    }

    @Test
    public void testProject_intervalAroundZero_closerOnMinSide() {
        // arrange
        double start = -1;
        double end = 0.5;
        RegionBSPTree1S tree = AngularInterval.of(start, end, TEST_PRECISION).toTree();

        // act/assert
        Assert.assertEquals(end, tree.project(Point1S.of(-1.5 * Geometry.PI)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(start, tree.project(Point1S.of(-Geometry.PI)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(start, tree.project(Point1S.of(-0.5 * Geometry.PI)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(start, tree.project(Point1S.of(-1)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(start, tree.project(Point1S.of(-0.5)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(end, tree.project(Point1S.of(-0.25)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(end, tree.project(Point1S.of(-0.1)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(end, tree.project(Point1S.ZERO).getAzimuth(), TEST_EPS);
        Assert.assertEquals(end, tree.project(Point1S.of(0.1)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(end, tree.project(Point1S.of(0.25)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(end, tree.project(Point1S.of(0.5)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(end, tree.project(Point1S.of(0.75)).getAzimuth(), TEST_EPS);
    }

    @Test
    public void testProject_intervalAroundZero_closerOnMaxSide() {
        // arrange
        double start = -0.5;
        double end = 1;
        RegionBSPTree1S tree = AngularInterval.of(start, end, TEST_PRECISION).toTree();

        // act/assert
        Assert.assertEquals(end, tree.project(Point1S.of(-1.5 * Geometry.PI)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(end, tree.project(Point1S.of(-Geometry.PI)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(start, tree.project(Point1S.of(-0.5 * Geometry.PI)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(start, tree.project(Point1S.of(-1)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(start, tree.project(Point1S.of(-0.5)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(start, tree.project(Point1S.of(-0.25)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(start, tree.project(Point1S.of(-0.1)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(start, tree.project(Point1S.ZERO).getAzimuth(), TEST_EPS);
        Assert.assertEquals(start, tree.project(Point1S.of(0.1)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(end, tree.project(Point1S.of(0.25)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(end, tree.project(Point1S.of(0.5)).getAzimuth(), TEST_EPS);
        Assert.assertEquals(end, tree.project(Point1S.of(0.75)).getAzimuth(), TEST_EPS);
    }

    private static void checkSimpleSplit(Split<RegionBSPTree1S> split, AngularInterval minusInterval,
            AngularInterval plusInterval) {

        RegionBSPTree1S minus = split.getMinus();
        if (minusInterval != null) {
            Assert.assertNotNull("Expected minus region to not be null", minus);
            checkSingleInterval(minus, minusInterval.getMin(), minusInterval.getMax());
        }
        else {
            Assert.assertNull("Expected minus region to be null", minus);
        }

        RegionBSPTree1S plus = split.getPlus();
        if (plusInterval != null) {
            Assert.assertNotNull("Expected plus region to not be null", plus);
            checkSingleInterval(plus, plusInterval.getMin(), plusInterval.getMax());
        }
        else {
            Assert.assertNull("Expected plus region to be null", plus);
        }
    }

    private static void checkSingleInterval(RegionBSPTree1S tree, double min, double max) {
        List<AngularInterval> intervals = tree.toIntervals();

        Assert.assertEquals("Expected a single interval in the tree", 1, intervals.size());

        checkInterval(intervals.get(0), min, max);
    }

    private static void checkInterval(AngularInterval interval, double min, double max) {
        double normalizedMin = PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(min);
        double normalizedMax = PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(max);

        if (TEST_PRECISION.eq(normalizedMin, normalizedMax)) {
            Assert.assertTrue(interval.isFull());
        }
        else {
            Assert.assertEquals(normalizedMin,
                    interval.getMinBoundary().getPoint().getNormalizedAzimuth(), TEST_EPS);
            Assert.assertEquals(normalizedMax,
                    interval.getMaxBoundary().getPoint().getNormalizedAzimuth(), TEST_EPS);
        }
    }

    private static void checkClassify(Region<Point1S> region, RegionLocation loc, double ... pts) {
        for (double pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, region.classify(Point1S.of(pt)));
        }
    }
}
