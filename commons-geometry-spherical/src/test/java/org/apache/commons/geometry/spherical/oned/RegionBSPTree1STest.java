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
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegionBSPTree1STest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final Transform1S HALF_PI_PLUS_AZ = Transform1S.createRotation(Angle.PI_OVER_TWO);

    private static final Transform1S PI_MINUS_AZ = Transform1S.createNegation().rotate(Math.PI);

    @Test
    public void testConstructor_default() {
        // act
        final RegionBSPTree1S tree = new RegionBSPTree1S();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());

        Assertions.assertEquals(0, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertNull(tree.getCentroid());
    }

    @Test
    public void testConstructor_true() {
        // act
        final RegionBSPTree1S tree = new RegionBSPTree1S(true);

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(Angle.TWO_PI, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertNull(tree.getCentroid());
    }

    @Test
    public void testConstructor_false() {
        // act
        final RegionBSPTree1S tree = new RegionBSPTree1S(false);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());

        Assertions.assertEquals(0, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertNull(tree.getCentroid());
    }

    @Test
    public void testFull() {
        // act
        final RegionBSPTree1S tree = RegionBSPTree1S.full();

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(Angle.TWO_PI, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertNull(tree.getCentroid());
    }

    @Test
    public void testEmpty() {
        // act
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());

        Assertions.assertEquals(0, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertNull(tree.getCentroid());
    }

    @Test
    public void testCopy() {
        // arrange
        final RegionBSPTree1S orig = RegionBSPTree1S.fromInterval(AngularInterval.of(0, Math.PI, TEST_PRECISION));

        // act
        final RegionBSPTree1S copy = orig.copy();

        // assert
        Assertions.assertNotSame(orig, copy);

        orig.setEmpty();

        checkSingleInterval(copy, 0, Math.PI);
    }

    @Test
    public void testFromInterval_full() {
        // act
        final RegionBSPTree1S tree = RegionBSPTree1S.fromInterval(AngularInterval.full());

        // assert
        Assertions.assertTrue(tree.isFull());
    }

    @Test
    public void testFromInterval_nonFull() {
        for (double theta = 0.0; theta <= Angle.TWO_PI; theta += 0.2) {
            // arrange
            final double max = theta + Angle.PI_OVER_TWO;

            // act
            final RegionBSPTree1S tree = RegionBSPTree1S.fromInterval(AngularInterval.of(theta, max, TEST_PRECISION));

            checkSingleInterval(tree, theta, max);

            Assertions.assertEquals(Angle.PI_OVER_TWO, tree.getSize(), TEST_EPS);
            Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
            Assertions.assertEquals(Angle.Rad.WITHIN_0_AND_2PI.applyAsDouble(theta + (0.25 * Math.PI)),
                    tree.getCentroid().getNormalizedAzimuth(), TEST_EPS);
        }
    }

    @Test
    public void testClassify_full() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.full();

        // act/assert
        for (double az = -Angle.TWO_PI; az <= 2 * Angle.TWO_PI; az += 0.2) {
            checkClassify(tree, RegionLocation.INSIDE, az);
        }
    }

    @Test
    public void testClassify_empty() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();

        // act/assert
        for (double az = -Angle.TWO_PI; az <= 2 * Angle.TWO_PI; az += 0.2) {
            checkClassify(tree, RegionLocation.OUTSIDE, az);
        }
    }

    @Test
    public void testClassify() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.fromInterval(
                AngularInterval.of(-Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, TEST_PRECISION));

        // act/assert
        checkClassify(tree, RegionLocation.BOUNDARY,
                -Angle.PI_OVER_TWO, Angle.PI_OVER_TWO,
                -Angle.PI_OVER_TWO - Angle.TWO_PI, Angle.PI_OVER_TWO + Angle.TWO_PI);
        checkClassify(tree, RegionLocation.INSIDE,
                0.0, 0.5, -0.5,
                Angle.TWO_PI, 0.5 + Angle.TWO_PI, -0.5 - Angle.TWO_PI);
        checkClassify(tree, RegionLocation.OUTSIDE,
                Math.PI, Math.PI + 0.5, Math.PI - 0.5,
                Math.PI + Angle.TWO_PI, Math.PI + 0.5 + Angle.TWO_PI,
                Math.PI - 0.5 + Angle.TWO_PI);
    }

    @Test
    public void testToIntervals_full() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.full();

        // act
        final List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(1, intervals.size());

        final AngularInterval interval = intervals.get(0);
        Assertions.assertTrue(interval.isFull());
    }

    @Test
    public void testToIntervals_empty() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();

        // act
        final List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(0, intervals.size());
    }

    @Test
    public void testToIntervals_singleCut() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();

        for (double theta = 0; theta <= Angle.TWO_PI; theta += 0.2) {
            // act/assert
            tree.setEmpty();
            tree.getRoot().cut(CutAngles.createPositiveFacing(theta, TEST_PRECISION));

            checkSingleInterval(tree, 0, theta);

            tree.setEmpty();
            tree.getRoot().cut(CutAngles.createNegativeFacing(theta, TEST_PRECISION));

            checkSingleInterval(tree, theta, Angle.TWO_PI);
        }
    }

    @Test
    public void testToIntervals_wrapAround_joinedIntervalsOnPositiveSide() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(0.25 * Math.PI, Angle.PI_OVER_TWO, TEST_PRECISION));
        tree.add(AngularInterval.of(1.5 * Math.PI, 0.25 * Math.PI, TEST_PRECISION));

        // act
        final List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(1, intervals.size());

        checkInterval(intervals.get(0), 1.5 * Math.PI, Angle.PI_OVER_TWO);
    }

    @Test
    public void testToIntervals_wrapAround_joinedIntervalsOnNegativeSide() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(1.75 * Math.PI, Angle.PI_OVER_TWO, TEST_PRECISION));
        tree.add(AngularInterval.of(1.5 * Math.PI, 1.75 * Math.PI, TEST_PRECISION));

        // act
        final List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(1, intervals.size());

        checkInterval(intervals.get(0), 1.5 * Math.PI, Angle.PI_OVER_TWO);
    }

    @Test
    public void testToIntervals_multipleIntervals() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(-Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, TEST_PRECISION));
        tree.add(AngularInterval.of(Math.PI - 0.5, Math.PI, TEST_PRECISION));
        tree.add(AngularInterval.of(Math.PI, Math.PI + 0.5, TEST_PRECISION));

        // act
        final List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(2, intervals.size());

        checkInterval(intervals.get(0), Math.PI - 0.5, Math.PI + 0.5);
        checkInterval(intervals.get(1), -Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);
    }

    @Test
    public void testToIntervals_multipleIntervals_complement() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(-Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, TEST_PRECISION));
        tree.add(AngularInterval.of(Math.PI - 0.5, Math.PI, TEST_PRECISION));
        tree.add(AngularInterval.of(Math.PI, Math.PI + 0.5, TEST_PRECISION));

        tree.complement();

        // act
        final List<AngularInterval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(2, intervals.size());

        checkInterval(intervals.get(0), Angle.PI_OVER_TWO, Math.PI - 0.5);
        checkInterval(intervals.get(1), Math.PI + 0.5, -Angle.PI_OVER_TWO);
    }

    @Test
    public void testSplit_empty() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();

        // act/assert
        Assertions.assertEquals(SplitLocation.NEITHER,
                tree.split(CutAngles.createPositiveFacing(0, TEST_PRECISION)).getLocation());
        Assertions.assertEquals(SplitLocation.NEITHER,
                tree.split(CutAngles.createNegativeFacing(Angle.PI_OVER_TWO, TEST_PRECISION)).getLocation());
        Assertions.assertEquals(SplitLocation.NEITHER,
                tree.split(CutAngles.createPositiveFacing(Math.PI, TEST_PRECISION)).getLocation());
        Assertions.assertEquals(SplitLocation.NEITHER,
                tree.split(CutAngles.createNegativeFacing(-Angle.PI_OVER_TWO, TEST_PRECISION)).getLocation());
        Assertions.assertEquals(SplitLocation.NEITHER,
                tree.split(CutAngles.createPositiveFacing(Angle.TWO_PI, TEST_PRECISION)).getLocation());
    }

    @Test
    public void testSplit_full() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.full();

        // act/assert
        checkSimpleSplit(
            tree.split(CutAngles.createPositiveFacing(1e-6, TEST_PRECISION)),
            AngularInterval.of(0, 1e-6, TEST_PRECISION),
            AngularInterval.of(1e-6, Angle.TWO_PI, TEST_PRECISION)
        );
        checkSimpleSplit(
            tree.split(CutAngles.createNegativeFacing(Angle.PI_OVER_TWO, TEST_PRECISION)),
            AngularInterval.of(Angle.PI_OVER_TWO, Angle.TWO_PI, TEST_PRECISION),
            AngularInterval.of(0, Angle.PI_OVER_TWO, TEST_PRECISION)
        );
        checkSimpleSplit(
            tree.split(CutAngles.createPositiveFacing(Math.PI, TEST_PRECISION)),
            AngularInterval.of(0, Math.PI, TEST_PRECISION),
            AngularInterval.of(Math.PI, Angle.TWO_PI, TEST_PRECISION)
        );
        checkSimpleSplit(
            tree.split(CutAngles.createNegativeFacing(-Angle.PI_OVER_TWO, TEST_PRECISION)),
            AngularInterval.of(-Angle.PI_OVER_TWO, Angle.TWO_PI, TEST_PRECISION),
            AngularInterval.of(0, -Angle.PI_OVER_TWO, TEST_PRECISION)
        );
        checkSimpleSplit(
            tree.split(CutAngles.createPositiveFacing(Angle.TWO_PI - 1e-6, TEST_PRECISION)),
            AngularInterval.of(0, Angle.TWO_PI - 1e-6, TEST_PRECISION),
            AngularInterval.of(Angle.TWO_PI - 1e-6, Angle.TWO_PI, TEST_PRECISION)
        );
    }

    @Test
    public void testSplit_full_cutEquivalentToZero() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.full();

        final AngularInterval twoPi = AngularInterval.of(0, Angle.TWO_PI, TEST_PRECISION);

        // act/assert
        checkSimpleSplit(
            tree.split(CutAngles.createPositiveFacing(0, TEST_PRECISION)),
            null,
            twoPi
        );
        checkSimpleSplit(
            tree.split(CutAngles.createNegativeFacing(0, TEST_PRECISION)),
            twoPi,
            null
        );

        checkSimpleSplit(
            tree.split(CutAngles.createPositiveFacing(Angle.TWO_PI - 1e-18, TEST_PRECISION)),
            null,
            twoPi
        );
        checkSimpleSplit(
            tree.split(CutAngles.createNegativeFacing(Angle.TWO_PI - 1e-18, TEST_PRECISION)),
            twoPi,
            null
        );
    }

    @Test
    public void testSplit_singleInterval() {
        // arrange
        final AngularInterval interval = AngularInterval.of(Angle.PI_OVER_TWO, -Angle.PI_OVER_TWO, TEST_PRECISION);
        final RegionBSPTree1S tree = interval.toTree();

        // act
        checkSimpleSplit(
            tree.split(CutAngles.createNegativeFacing(0, TEST_PRECISION)),
            interval,
            null
        );
        checkSimpleSplit(
            tree.split(CutAngles.createNegativeFacing(-Angle.TWO_PI, TEST_PRECISION)),
            interval,
            null
        );

        checkSimpleSplit(
            tree.split(CutAngles.createPositiveFacing(Angle.TWO_PI + Angle.PI_OVER_TWO, TEST_PRECISION)),
            null,
            interval
        );
        checkSimpleSplit(
            tree.split(CutAngles.createPositiveFacing(1.5 * Math.PI, TEST_PRECISION)),
            interval,
            null
        );

        checkSimpleSplit(
            tree.split(CutAngles.createNegativeFacing(Math.PI, TEST_PRECISION)),
            AngularInterval.of(Math.PI, -Angle.PI_OVER_TWO, TEST_PRECISION),
            AngularInterval.of(Angle.PI_OVER_TWO, Math.PI, TEST_PRECISION)
        );
    }

    @Test
    public void testSplit_singleIntervalSplitIntoTwoIntervalsOnSameSide() {
        // arrange
        final RegionBSPTree1S tree = AngularInterval.of(-Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, TEST_PRECISION).toTree();

        final CutAngle cut = CutAngles.createPositiveFacing(0, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = tree.split(cut);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        Assertions.assertNull(minus);

        final RegionBSPTree1S plus = split.getPlus();
        final List<AngularInterval> plusIntervals = plus.toIntervals();
        Assertions.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), -Angle.PI_OVER_TWO, Angle.PI_OVER_TWO);
    }

    @Test
    public void testSplit_multipleRegions() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(Angle.TWO_PI - 1, Angle.PI_OVER_TWO, TEST_PRECISION));
        tree.add(AngularInterval.of(Math.PI, -Angle.PI_OVER_TWO, TEST_PRECISION));

        final CutAngle cut = CutAngles.createNegativeFacing(1, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = tree.split(cut);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        final List<AngularInterval> minusIntervals = minus.toIntervals();
        Assertions.assertEquals(3, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 1, Angle.PI_OVER_TWO);
        checkInterval(minusIntervals.get(1), Math.PI, -Angle.PI_OVER_TWO);
        checkInterval(minusIntervals.get(2), Angle.TWO_PI - 1, 0);

        final RegionBSPTree1S plus = split.getPlus();
        final List<AngularInterval> plusIntervals = plus.toIntervals();
        Assertions.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 0, 1);
    }

    @Test
    public void testSplitDiameter_full() {
        // arrange
        final RegionBSPTree1S full = RegionBSPTree1S.full();
        final CutAngle splitter = CutAngles.createPositiveFacing(Angle.PI_OVER_TWO, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = full.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        final List<AngularInterval> minusIntervals = minus.toIntervals();
        Assertions.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 1.5 * Math.PI, 2.5 * Math.PI);

        final RegionBSPTree1S plus = split.getPlus();
        final List<AngularInterval> plusIntervals = plus.toIntervals();
        Assertions.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), Angle.PI_OVER_TWO, 1.5 * Math.PI);
    }

    @Test
    public void testSplitDiameter_empty() {
        // arrange
        final RegionBSPTree1S empty = RegionBSPTree1S.empty();
        final CutAngle splitter = CutAngles.createPositiveFacing(Angle.PI_OVER_TWO, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = empty.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        Assertions.assertNull(minus);

        final RegionBSPTree1S plus = split.getPlus();
        Assertions.assertNull(plus);
    }

    @Test
    public void testSplitDiameter_minus_zeroOnMinusSide() {
        // arrange
        final RegionBSPTree1S tree = AngularInterval.of(0, 1, TEST_PRECISION).toTree();
        final CutAngle splitter = CutAngles.createPositiveFacing(1, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = tree.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        final List<AngularInterval> minusIntervals = minus.toIntervals();
        Assertions.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 0, 1);

        final RegionBSPTree1S plus = split.getPlus();
        Assertions.assertNull(plus);
    }

    @Test
    public void testSplitDiameter_minus_zeroOnPlusSide() {
        // arrange
        final RegionBSPTree1S tree = AngularInterval.of(1, 2, TEST_PRECISION).toTree();
        final CutAngle splitter = CutAngles.createNegativeFacing(0, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = tree.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        final List<AngularInterval> minusIntervals = minus.toIntervals();
        Assertions.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 1, 2);

        final RegionBSPTree1S plus = split.getPlus();
        Assertions.assertNull(plus);
    }

    @Test
    public void testSplitDiameter_plus_zeroOnMinusSide() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(1, 1.1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 2.1, TEST_PRECISION));

        final CutAngle splitter = CutAngles.createPositiveFacing(1, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = tree.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        Assertions.assertNull(minus);

        final RegionBSPTree1S plus = split.getPlus();
        final List<AngularInterval> plusIntervals = plus.toIntervals();
        Assertions.assertEquals(2, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1.1);
        checkInterval(plusIntervals.get(1), 2, 2.1);
    }

    @Test
    public void testSplitDiameter_plus_zeroOnPlusSide() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(1, 1.1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 2.1, TEST_PRECISION));

        final CutAngle splitter = CutAngles.createNegativeFacing(Math.PI - 1, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = tree.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        Assertions.assertNull(minus);

        final RegionBSPTree1S plus = split.getPlus();
        final List<AngularInterval> plusIntervals = plus.toIntervals();
        Assertions.assertEquals(2, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1.1);
        checkInterval(plusIntervals.get(1), 2, 2.1);
    }

    @Test
    public void testSplitDiameter_both_zeroOnMinusSide() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(1, 1.1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 3, TEST_PRECISION));

        final CutAngle splitter = CutAngles.createPositiveFacing(2.5, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = tree.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        final List<AngularInterval> plusIntervals = minus.toIntervals();
        Assertions.assertEquals(2, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1.1);
        checkInterval(plusIntervals.get(1), 2, 2.5);

        final RegionBSPTree1S plus = split.getPlus();
        final List<AngularInterval> minusIntervals = plus.toIntervals();
        Assertions.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 2.5, 3);
    }

    @Test
    public void testSplitDiameter_both_zeroOnPlusSide() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(1, 1.1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 3, TEST_PRECISION));

        final CutAngle splitter = CutAngles.createNegativeFacing(2.5, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1S> split = tree.splitDiameter(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final RegionBSPTree1S minus = split.getMinus();
        final List<AngularInterval> minusIntervals = minus.toIntervals();
        Assertions.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 2.5, 3);

        final RegionBSPTree1S plus = split.getPlus();
        final List<AngularInterval> plusIntervals = plus.toIntervals();
        Assertions.assertEquals(2, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1.1);
        checkInterval(plusIntervals.get(1), 2, 2.5);
    }

    @Test
    public void testRegionProperties_singleInterval_wrapsZero() {
        // arrange
        final RegionBSPTree1S tree = AngularInterval.of(-Angle.PI_OVER_TWO, Math.PI,
                TEST_PRECISION).toTree();

        // act/assert
        Assertions.assertEquals(1.5 * Math.PI, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(0.25 * Math.PI, tree.getCentroid().getAzimuth(), TEST_EPS);
    }

    @Test
    public void testRegionProperties_singleInterval_doesNotWrap() {
        // arrange
        final RegionBSPTree1S tree = AngularInterval.of(Angle.PI_OVER_TWO, Angle.TWO_PI,
                TEST_PRECISION).toTree();

        // act/assert
        Assertions.assertEquals(1.5 * Math.PI, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(1.25 * Math.PI, tree.getCentroid().getAzimuth(), TEST_EPS);
    }

    @Test
    public void testRegionProperties_multipleIntervals_sameSize() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(0, 0.1, TEST_PRECISION));
        tree.add(AngularInterval.of(0.2, 0.3, TEST_PRECISION));

        // act/assert
        Assertions.assertEquals(0.2, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(0.15, tree.getCentroid().getAzimuth(), TEST_EPS);
    }

    @Test
    public void testRegionProperties_multipleIntervals_differentSizes() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(0, 0.2, TEST_PRECISION));
        tree.add(AngularInterval.of(0.3, 0.7, TEST_PRECISION));

        // act/assert
        Assertions.assertEquals(0.6, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        final Vector2D centroidVector = Point1S.of(0.1).getVector().withNorm(0.2)
                .add(Point1S.of(0.5).getVector().withNorm(0.4));
        Assertions.assertEquals(Point1S.from(centroidVector).getAzimuth(), tree.getCentroid().getAzimuth(), TEST_EPS);
    }

    @Test
    public void testRegionProperties_equalAndOppositeIntervals() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(-1, 1, TEST_PRECISION));
        tree.add(AngularInterval.of(Math.PI - 1, Math.PI + 1, TEST_PRECISION));

        // act/assert
        Assertions.assertEquals(4, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertNull(tree.getCentroid()); // no unique centroid exists
    }

    @Test
    public void testTransform_fullAndEmpty() {
        // arrange
        final RegionBSPTree1S full = RegionBSPTree1S.full();
        final RegionBSPTree1S empty = RegionBSPTree1S.empty();

        // act
        full.transform(PI_MINUS_AZ);
        empty.transform(HALF_PI_PLUS_AZ);

        // assert
        Assertions.assertTrue(full.isFull());
        Assertions.assertFalse(full.isEmpty());

        Assertions.assertFalse(empty.isFull());
        Assertions.assertTrue(empty.isEmpty());
    }

    @Test
    public void testTransform_halfPiPlusAz() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(-1, 1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 3, TEST_PRECISION));

        // act
        tree.transform(HALF_PI_PLUS_AZ);

        // assert
        Assertions.assertEquals(3, tree.getSize(), TEST_EPS);

        final List<AngularInterval> intervals = tree.toIntervals();

        Assertions.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Angle.PI_OVER_TWO - 1, Angle.PI_OVER_TWO + 1);
        checkInterval(intervals.get(1), Angle.PI_OVER_TWO + 2, Angle.PI_OVER_TWO + 3);
    }

    @Test
    public void testTransform_piMinusAz() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(-1, 1, TEST_PRECISION));
        tree.add(AngularInterval.of(2, 3, TEST_PRECISION));

        // act
        tree.transform(PI_MINUS_AZ);

        // assert
        Assertions.assertEquals(3, tree.getSize(), TEST_EPS);

        final List<AngularInterval> intervals = tree.toIntervals();

        Assertions.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Math.PI - 3, Math.PI - 2);
        checkInterval(intervals.get(1), Math.PI - 1, Math.PI + 1);
    }

    @Test
    public void testProject_fullAndEmpty() {
        // arrange
        final RegionBSPTree1S full = RegionBSPTree1S.full();
        final RegionBSPTree1S empty = RegionBSPTree1S.empty();

        // act/assert
        Assertions.assertNull(full.project(Point1S.ZERO));
        Assertions.assertNull(full.project(Point1S.PI));

        Assertions.assertNull(empty.project(Point1S.ZERO));
        Assertions.assertNull(empty.project(Point1S.PI));
    }

    @Test
    public void testProject_withIntervals() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(-Angle.PI_OVER_TWO, Angle.PI_OVER_TWO, TEST_PRECISION));
        tree.add(AngularInterval.of(Math.PI - 1, Math.PI + 1, TEST_PRECISION));

        // act/assert
        Assertions.assertEquals(-Angle.PI_OVER_TWO,
                tree.project(Point1S.of(-Angle.PI_OVER_TWO - 0.1)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(-Angle.PI_OVER_TWO,
                tree.project(Point1S.of(-Angle.PI_OVER_TWO)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(-Angle.PI_OVER_TWO,
                tree.project(Point1S.of(-Angle.PI_OVER_TWO + 0.1)).getAzimuth(), TEST_EPS);

        Assertions.assertEquals(-Angle.PI_OVER_TWO, tree.project(Point1S.of(-0.1)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, tree.project(Point1S.ZERO).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, tree.project(Point1S.of(0.1)).getAzimuth(), TEST_EPS);

        Assertions.assertEquals(Math.PI - 1,
                tree.project(Point1S.of(Math.PI - 0.5)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(Math.PI + 1,
                tree.project(Point1S.of(Math.PI + 0.5)).getAzimuth(), TEST_EPS);
    }

    @Test
    public void testProject_equidistant() {
        // arrange
        final RegionBSPTree1S tree = AngularInterval.of(1, 2, TEST_PRECISION).toTree();
        final RegionBSPTree1S treeComplement = tree.copy();
        treeComplement.complement();

        // act/assert
        Assertions.assertEquals(1, tree.project(Point1S.of(1.5)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(1, treeComplement.project(Point1S.of(1.5)).getAzimuth(), TEST_EPS);
    }

    @Test
    public void testProject_intervalAroundZero_closerOnMinSide() {
        // arrange
        final double start = -1;
        final double end = 0.5;
        final RegionBSPTree1S tree = AngularInterval.of(start, end, TEST_PRECISION).toTree();

        // act/assert
        Assertions.assertEquals(end, tree.project(Point1S.of(-1.5 * Math.PI)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(start, tree.project(Point1S.of(-Math.PI)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(start, tree.project(Point1S.of(-0.5 * Math.PI)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(start, tree.project(Point1S.of(-1)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(start, tree.project(Point1S.of(-0.5)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(end, tree.project(Point1S.of(-0.25)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(end, tree.project(Point1S.of(-0.1)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(end, tree.project(Point1S.ZERO).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(end, tree.project(Point1S.of(0.1)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(end, tree.project(Point1S.of(0.25)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(end, tree.project(Point1S.of(0.5)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(end, tree.project(Point1S.of(0.75)).getAzimuth(), TEST_EPS);
    }

    @Test
    public void testProject_intervalAroundZero_closerOnMaxSide() {
        // arrange
        final double start = -0.5;
        final double end = 1;
        final RegionBSPTree1S tree = AngularInterval.of(start, end, TEST_PRECISION).toTree();

        // act/assert
        Assertions.assertEquals(end, tree.project(Point1S.of(-1.5 * Math.PI)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(end, tree.project(Point1S.of(-Math.PI)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(start, tree.project(Point1S.of(-0.5 * Math.PI)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(start, tree.project(Point1S.of(-1)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(start, tree.project(Point1S.of(-0.5)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(start, tree.project(Point1S.of(-0.25)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(start, tree.project(Point1S.of(-0.1)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(start, tree.project(Point1S.ZERO).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(start, tree.project(Point1S.of(0.1)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(end, tree.project(Point1S.of(0.25)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(end, tree.project(Point1S.of(0.5)).getAzimuth(), TEST_EPS);
        Assertions.assertEquals(end, tree.project(Point1S.of(0.75)).getAzimuth(), TEST_EPS);
    }

    private static void checkSimpleSplit(final Split<RegionBSPTree1S> split, final AngularInterval minusInterval,
                                         final AngularInterval plusInterval) {

        final RegionBSPTree1S minus = split.getMinus();
        if (minusInterval != null) {
            Assertions.assertNotNull(minus, "Expected minus region to not be null");
            checkSingleInterval(minus, minusInterval.getMin(), minusInterval.getMax());
        } else {
            Assertions.assertNull(minus, "Expected minus region to be null");
        }

        final RegionBSPTree1S plus = split.getPlus();
        if (plusInterval != null) {
            Assertions.assertNotNull(plus, "Expected plus region to not be null");
            checkSingleInterval(plus, plusInterval.getMin(), plusInterval.getMax());
        } else {
            Assertions.assertNull(plus, "Expected plus region to be null");
        }
    }

    private static void checkSingleInterval(final RegionBSPTree1S tree, final double min, final double max) {
        final List<AngularInterval> intervals = tree.toIntervals();

        Assertions.assertEquals(1, intervals.size(), "Expected a single interval in the tree");

        checkInterval(intervals.get(0), min, max);
    }

    private static void checkInterval(final AngularInterval interval, final double min, final double max) {
        final double normalizedMin = Angle.Rad.WITHIN_0_AND_2PI.applyAsDouble(min);
        final double normalizedMax = Angle.Rad.WITHIN_0_AND_2PI.applyAsDouble(max);

        if (TEST_PRECISION.eq(normalizedMin, normalizedMax)) {
            Assertions.assertTrue(interval.isFull());
        } else {
            Assertions.assertEquals(normalizedMin,
                    interval.getMinBoundary().getPoint().getNormalizedAzimuth(), TEST_EPS);
            Assertions.assertEquals(normalizedMax,
                    interval.getMaxBoundary().getPoint().getNormalizedAzimuth(), TEST_EPS);
        }
    }

    private static void checkClassify(final Region<Point1S> region, final RegionLocation loc, final double... pts) {
        for (final double pt : pts) {
            Assertions.assertEquals(loc, region.classify(Point1S.of(pt)), "Unexpected location for point " + pt);
        }
    }
}
