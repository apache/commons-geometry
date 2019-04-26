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
package org.apache.commons.geometry.euclidean.oned;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class RegionBSPTree1DTest {

    private static final double TEST_EPS = 1e-15;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testClassify_fullRegion() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D(true);

        // act/assert
        checkClassify(tree, RegionLocation.INSIDE,
                Double.NEGATIVE_INFINITY, -1, 0, 1, Double.POSITIVE_INFINITY);

        checkClassify(tree, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void testClassify_emptyRegion() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D(false);

        // act/assert
        checkClassify(tree, RegionLocation.OUTSIDE,
                Double.NEGATIVE_INFINITY, -1, 0, 1, Double.POSITIVE_INFINITY);

        checkClassify(tree, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void testClassify_singleClosedInterval() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D();
        tree.insert(Arrays.asList(
                    OrientedPoint.createNegativeFacing(Vector1D.of(-1), TEST_PRECISION).span(),
                    OrientedPoint.createPositiveFacing(Vector1D.of(9), TEST_PRECISION).span()
                ));

        // act/assert
        checkClassify(tree, RegionLocation.OUTSIDE, Double.NEGATIVE_INFINITY);
        checkClassify(tree, RegionLocation.OUTSIDE,-2.0);
        checkClassify(tree, RegionLocation.INSIDE, 0.0);
        checkClassify(tree, RegionLocation.BOUNDARY, 9.0 - 1e-16);
        checkClassify(tree, RegionLocation.BOUNDARY, 9.0 + 1e-16);
        checkClassify(tree, RegionLocation.OUTSIDE, 10.0);
        checkClassify(tree, RegionLocation.OUTSIDE, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testContains_fullRegion() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D(true);

        // act/assert
        checkContains(tree, true,
                Double.NEGATIVE_INFINITY, -1, 0, 1, Double.POSITIVE_INFINITY);

        checkContains(tree, false, Double.NaN);
    }

    @Test
    public void testContains_emptyRegion() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D(false);

        // act/assert
        checkContains(tree, false,
                Double.NEGATIVE_INFINITY, -1, 0, 1, Double.POSITIVE_INFINITY);

        checkContains(tree, false, Double.NaN);
    }

    @Test
    public void testContains_singleClosedInterval() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D();
        tree.insert(Arrays.asList(
                    OrientedPoint.createNegativeFacing(Vector1D.of(-1), TEST_PRECISION).span(),
                    OrientedPoint.createPositiveFacing(Vector1D.of(9), TEST_PRECISION).span()
                ));

        // act/assert
        checkContains(tree, false, Double.NEGATIVE_INFINITY);
        checkContains(tree, false,-2.0);
        checkContains(tree, true, 0.0);
        checkContains(tree, true, 9.0 - 1e-16);
        checkContains(tree, true, 9.0 + 1e-16);
        checkContains(tree, false, 10.0);
        checkContains(tree, false, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAdd_interval() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D(false);

        // act
        tree.add(Interval.of(Double.NEGATIVE_INFINITY, -10, TEST_PRECISION));
        tree.add(Interval.of(-1, 1, TEST_PRECISION));
        tree.add(Interval.of(10, Double.POSITIVE_INFINITY, TEST_PRECISION));

        // assert
        checkClassify(tree, RegionLocation.INSIDE,
                Double.NEGATIVE_INFINITY, -11, 0, 11, Double.POSITIVE_INFINITY);

        checkClassify(tree, RegionLocation.BOUNDARY, -10, -1, 1, 10);

        checkClassify(tree, RegionLocation.OUTSIDE, -9, -2, 2, 9);
    }

    @Test
    public void testAdd_addFullInterval() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D(false);

        // act
        tree.add(Interval.of(-1, 1, TEST_PRECISION));
        tree.add(Interval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION));

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertEquals(1, tree.count());
    }

    @Test
    public void testAdd_interval_duplicateBoundaryPoint() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D(false);

        // act
        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(2, 3, TEST_PRECISION));
        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(0, 1, TEST_PRECISION));

        // assert
        checkClassify(tree, RegionLocation.INSIDE, 0.1, 1, 2, 2.9);

        checkClassify(tree, RegionLocation.BOUNDARY, 0, 3);

        checkClassify(tree, RegionLocation.OUTSIDE, -1, -0.1, 3.1, 4);
    }

    @Test
    public void testToIntervals_fullRegion() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D(true);

        // act
        List<Interval> intervals = tree.toIntervals(TEST_PRECISION);

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testToIntervals_emptyRegion() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D(false);

        // act
        List<Interval> intervals = tree.toIntervals(TEST_PRECISION);

        // assert
        Assert.assertEquals(0, intervals.size());
    }

    @Test
    public void testToIntervals_halfOpen_negative() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D();
        tree.getRoot().cut(OrientedPoint.fromLocationAndDirection(1.0, true, precision));

        // act
        List<Interval> intervals = tree.toIntervals(TEST_PRECISION);

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, 1);
    }

    @Test
    public void testToIntervals_halfOpen_positive() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D();
        tree.getRoot().cut(OrientedPoint.fromLocationAndDirection(-1.0, false, precision));

        // act
        List<Interval> intervals = tree.toIntervals(TEST_PRECISION);

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), -1, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testToIntervals_singleClosedInterval() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(-1, 1, precision));

        // act
        List<Interval> intervals = tree.toIntervals(TEST_PRECISION);

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), -1, 1);
    }

    @Test
    public void testToIntervals_singleClosedInterval_complement() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(-1, 1, precision));
        tree.complement();

        // act
        List<Interval> intervals = tree.toIntervals(TEST_PRECISION);

        // assert
        Assert.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, -1);
        checkInterval(intervals.get(1), 1, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testToIntervals_openAndClosedIntervals() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(Double.NEGATIVE_INFINITY, -10, precision));
        tree.add(Interval.of(-1, 1, precision));
        tree.add(Interval.of(10, Double.POSITIVE_INFINITY, precision));

        // act
        List<Interval> intervals = tree.toIntervals(TEST_PRECISION);

        // assert
        Assert.assertEquals(3, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, -10);
        checkInterval(intervals.get(1), -1, 1);
        checkInterval(intervals.get(2), 10, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testToIntervals_singlePoint() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(1, 1, precision));

        // act
        List<Interval> intervals = tree.toIntervals(TEST_PRECISION);

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), 1, 1);
    }

    @Test
    public void testToIntervals_singlePoint_complement() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(1, 1, precision));
        tree.complement();

        // act
        List<Interval> intervals = tree.toIntervals(TEST_PRECISION);

        // assert
        Assert.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, 1);
        checkInterval(intervals.get(1), 1, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testToIntervals_multiplePoints() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(1, 1, precision));
        tree.add(Interval.of(2, 2, precision));

        // act
        List<Interval> intervals = tree.toIntervals(TEST_PRECISION);

        // assert
        Assert.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), 1, 1);
        checkInterval(intervals.get(1), 2, 2);
    }

    @Test
    public void testToIntervals_multiplePoints_complement() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(1, 1, precision));
        tree.add(Interval.of(2, 2, precision));
        tree.complement();

        // act
        List<Interval> intervals = tree.toIntervals(TEST_PRECISION);

        // assert
        Assert.assertEquals(3, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, 1);
        checkInterval(intervals.get(1), 1, 2);
        checkInterval(intervals.get(2), 2, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testToIntervals_pointsMergedFromMinSide() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(0.1);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(1, 1, TEST_PRECISION));
        tree.add(Interval.of(1.05, 1.07, TEST_PRECISION));
        tree.add(Interval.of(1.09, Double.POSITIVE_INFINITY, TEST_PRECISION));

        // act
        List<Interval> intervals = tree.toIntervals(precision);

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), 1, Double.POSITIVE_INFINITY, precision);
    }

    @Test
    public void testToIntervals_pointsMergedFromMaxSide() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(0.1);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(Double.NEGATIVE_INFINITY, 1, TEST_PRECISION));
        tree.add(Interval.of(1.05, 1.07, TEST_PRECISION));
        tree.add(Interval.of(1.08, 1.09, TEST_PRECISION));

        // act
        List<Interval> intervals = tree.toIntervals(precision);

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, 1.09, precision);
    }

    @Test
    public void testToIntervals_pointsMergedFromBothSide() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(0.1);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(1, 1.01, TEST_PRECISION));
        tree.add(Interval.of(1.02, 1.03, TEST_PRECISION));
        tree.add(Interval.of(1.04, 1.05, TEST_PRECISION));
        tree.add(Interval.of(1.06, 4, TEST_PRECISION));
        tree.add(Interval.of(4.01, 4.02, TEST_PRECISION));
        tree.add(Interval.of(4.03, 4.04, TEST_PRECISION));
        tree.add(Interval.of(4.05, 4.06, TEST_PRECISION));

        // act
        List<Interval> intervals = tree.toIntervals(precision);

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), 1, 4.06, precision);
    }

    @Test
    public void testToIntervals_mixOfMergedAndUnmergedPoints() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(0.1);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(-3, -2.5, TEST_PRECISION));
        tree.add(Interval.of(-2, 1, TEST_PRECISION));
        tree.add(Interval.of(1.05, 1.07, TEST_PRECISION));
        tree.add(Interval.of(1.99, 2, TEST_PRECISION));
        tree.add(Interval.of(2.01, 4, TEST_PRECISION));
        tree.add(Interval.of(5, 7, TEST_PRECISION));

        // act
        List<Interval> intervals = tree.toIntervals(precision);

        // assert
        Assert.assertEquals(4, intervals.size());
        checkInterval(intervals.get(0), -3, -2.5, precision);
        checkInterval(intervals.get(1), -2, 1.07, precision);
        checkInterval(intervals.get(2), 1.99, 4, precision);
        checkInterval(intervals.get(3), 5, 7, precision);
    }

    private static void checkClassify(RegionBSPTree1D tree, RegionLocation loc, double ... points) {
        for (double x : points) {
            String msg = "Unexpected location for point " + x;

            Assert.assertEquals(msg, loc, tree.classify(x));
            Assert.assertEquals(msg, loc, tree.classify(Vector1D.of(x)));
        }
    }

    private static void checkContains(RegionBSPTree1D tree, boolean contains, double ... points) {
        for (double x : points) {
            String msg = "Unexpected contains status for point " + x;

            Assert.assertEquals(msg, contains, tree.contains(x));
            Assert.assertEquals(msg, contains, tree.contains(Vector1D.of(x)));
        }
    }

    private static void checkInterval(Interval interval, double min, double max) {
        checkInterval(interval, min, max, TEST_PRECISION);
    }

    private static void checkInterval(Interval interval, double min, double max, DoublePrecisionContext precision) {
        Assert.assertEquals(min, interval.getMin(), TEST_EPS);
        Assert.assertEquals(max, interval.getMax(), TEST_EPS);
    }
}
