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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class RegionBSPTree1STest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

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
    public void testToIntervals_multipleInterval() {
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

    private static void checkSingleInterval(RegionBSPTree1S tree, double min, double max) {
        List<AngularInterval> intervals = tree.toIntervals();

        Assert.assertEquals(1, intervals.size());

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
