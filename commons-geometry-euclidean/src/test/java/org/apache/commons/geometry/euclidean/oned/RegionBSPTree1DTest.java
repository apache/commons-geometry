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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D.RegionNode1D;
import org.junit.Assert;
import org.junit.Test;

public class RegionBSPTree1DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testCopy() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D(true);
        tree.getRoot().cut(OrientedPoints.createPositiveFacing(1.0, TEST_PRECISION));

        // act
        RegionBSPTree1D copy = tree.copy();

        // assert
        Assert.assertNotSame(tree, copy);
        Assert.assertEquals(3, copy.count());
    }

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
                    OrientedPoints.createNegativeFacing(Vector1D.of(-1), TEST_PRECISION).span(),
                    OrientedPoints.createPositiveFacing(Vector1D.of(9), TEST_PRECISION).span()
                ));

        // act/assert
        checkClassify(tree, RegionLocation.OUTSIDE, Double.NEGATIVE_INFINITY);
        checkClassify(tree, RegionLocation.OUTSIDE, -2.0);
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
                    OrientedPoints.createNegativeFacing(Vector1D.of(-1), TEST_PRECISION).span(),
                    OrientedPoints.createPositiveFacing(Vector1D.of(9), TEST_PRECISION).span()
                ));

        // act/assert
        checkContains(tree, false, Double.NEGATIVE_INFINITY);
        checkContains(tree, false, -2.0);
        checkContains(tree, true, 0.0);
        checkContains(tree, true, 9.0 - 1e-16);
        checkContains(tree, true, 9.0 + 1e-16);
        checkContains(tree, false, 10.0);
        checkContains(tree, false, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testGetBoundarySize_alwaysReturnsZero() {
        // act/assert
        Assert.assertEquals(0.0, RegionBSPTree1D.full().getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.0, RegionBSPTree1D.empty().getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0.0, RegionBSPTree1D.from(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.of(4, 5, TEST_PRECISION)
                ).getBoundarySize(), TEST_EPS);
    }

    @Test
    public void testProject_full() {
        // arrange
        RegionBSPTree1D full = RegionBSPTree1D.full();

        // act/assert
        Assert.assertNull(full.project(Vector1D.of(Double.NEGATIVE_INFINITY)));
        Assert.assertNull(full.project(Vector1D.of(0)));
        Assert.assertNull(full.project(Vector1D.of(Double.POSITIVE_INFINITY)));
    }

    @Test
    public void testProject_empty() {
        // arrange
        RegionBSPTree1D empty = RegionBSPTree1D.empty();

        // act/assert
        Assert.assertNull(empty.project(Vector1D.of(Double.NEGATIVE_INFINITY)));
        Assert.assertNull(empty.project(Vector1D.of(0)));
        Assert.assertNull(empty.project(Vector1D.of(Double.POSITIVE_INFINITY)));
    }

    @Test
    public void testProject_singlePoint() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.point(1, TEST_PRECISION));

        // act/assert
        checkBoundaryProjection(tree, -1, 1);
        checkBoundaryProjection(tree, 0, 1);

        checkBoundaryProjection(tree, 1, 1);

        checkBoundaryProjection(tree, 2, 1);
        checkBoundaryProjection(tree, 3, 1);

        checkBoundaryProjection(tree, Double.NEGATIVE_INFINITY, 1);
        checkBoundaryProjection(tree, Double.POSITIVE_INFINITY, 1);
    }

    @Test
    public void testProject_noMinBoundary() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.of(Double.NEGATIVE_INFINITY, 1, TEST_PRECISION));

        // act/assert
        checkBoundaryProjection(tree, -1, 1);
        checkBoundaryProjection(tree, 0, 1);
        checkBoundaryProjection(tree, 1, 1);
        checkBoundaryProjection(tree, 2, 1);
        checkBoundaryProjection(tree, 3, 1);

        checkBoundaryProjection(tree, Double.NEGATIVE_INFINITY, 1);
        checkBoundaryProjection(tree, Double.POSITIVE_INFINITY, 1);
    }

    @Test
    public void testProject_noMaxBoundary() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.of(1, Double.POSITIVE_INFINITY, TEST_PRECISION));

        // act/assert
        checkBoundaryProjection(tree, -1, 1);
        checkBoundaryProjection(tree, 0, 1);
        checkBoundaryProjection(tree, 1, 1);
        checkBoundaryProjection(tree, 2, 1);
        checkBoundaryProjection(tree, 3, 1);

        checkBoundaryProjection(tree, Double.NEGATIVE_INFINITY, 1);
        checkBoundaryProjection(tree, Double.POSITIVE_INFINITY, 1);
    }

    @Test
    public void testProject_closedInterval() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.of(1, 3, TEST_PRECISION));

        // act/assert
        checkBoundaryProjection(tree, -1, 1);
        checkBoundaryProjection(tree, 0, 1);
        checkBoundaryProjection(tree, 1, 1);

        checkBoundaryProjection(tree, 1.9, 1);
        checkBoundaryProjection(tree, 2, 1);
        checkBoundaryProjection(tree, 2.1, 3);

        checkBoundaryProjection(tree, 3, 3);
        checkBoundaryProjection(tree, 4, 3);
        checkBoundaryProjection(tree, 5, 3);

        checkBoundaryProjection(tree, Double.NEGATIVE_INFINITY, 1);
        checkBoundaryProjection(tree, Double.POSITIVE_INFINITY, 3);
    }

    @Test
    public void testProject_multipleIntervals() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(
                    Interval.max(-1, TEST_PRECISION),
                    Interval.point(1, TEST_PRECISION),
                    Interval.of(2, 3, TEST_PRECISION),
                    Interval.of(5, 6, TEST_PRECISION)
                );

        // act/assert
        checkBoundaryProjection(tree, Double.NEGATIVE_INFINITY, -1);
        checkBoundaryProjection(tree, -2, -1);
        checkBoundaryProjection(tree, -1, -1);

        checkBoundaryProjection(tree, -0.5, -1);
        checkBoundaryProjection(tree, 0, -1);
        checkBoundaryProjection(tree, 0.5, 1);

        checkBoundaryProjection(tree, 0.9, 1);
        checkBoundaryProjection(tree, 1, 1);
        checkBoundaryProjection(tree, 1.1, 1);

        checkBoundaryProjection(tree, 0.5, 1);

        checkBoundaryProjection(tree, 1.9, 2);
        checkBoundaryProjection(tree, 2, 2);
        checkBoundaryProjection(tree, 2.1, 2);
        checkBoundaryProjection(tree, 2.5, 2);
        checkBoundaryProjection(tree, 2.9, 3);
        checkBoundaryProjection(tree, 3, 3);
        checkBoundaryProjection(tree, 3.1, 3);

        checkBoundaryProjection(tree, 3.9, 3);
        checkBoundaryProjection(tree, 4, 3);
        checkBoundaryProjection(tree, 4.1, 5);

        checkBoundaryProjection(tree, 4.9, 5);
        checkBoundaryProjection(tree, 5, 5);
        checkBoundaryProjection(tree, 5.1, 5);
        checkBoundaryProjection(tree, 5.49, 5);
        checkBoundaryProjection(tree, 5.5, 5);
        checkBoundaryProjection(tree, 5.51, 6);
        checkBoundaryProjection(tree, 5.9, 6);
        checkBoundaryProjection(tree, 6, 6);
        checkBoundaryProjection(tree, 6.1, 6);

        checkBoundaryProjection(tree, 7, 6);

        checkBoundaryProjection(tree, Double.POSITIVE_INFINITY, 6);
    }

    @Test
    public void testAdd_interval() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

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
    public void testAdd_adjacentIntervals() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act
        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(2, 3, TEST_PRECISION));

        // assert
        checkClassify(tree, RegionLocation.INSIDE, 1.1, 2, 2.9);

        checkClassify(tree, RegionLocation.BOUNDARY, 1, 3);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Double.NEGATIVE_INFINITY, 0, 0.9, 3.1, 4, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testAdd_addFullInterval() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

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
        List<Interval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testToIntervals_emptyRegion() {
        // arrange
        RegionBSPTree1D tree = new RegionBSPTree1D(false);

        // act
        List<Interval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(0, intervals.size());
    }

    @Test
    public void testToIntervals_halfOpen_negative() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D();
        tree.getRoot().cut(OrientedPoints.fromLocationAndDirection(1.0, true, precision));

        // act
        List<Interval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, 1);
    }

    @Test
    public void testToIntervals_halfOpen_positive() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D();
        tree.getRoot().cut(OrientedPoints.fromLocationAndDirection(-1.0, false, precision));

        // act
        List<Interval> intervals = tree.toIntervals();

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
        List<Interval> intervals = tree.toIntervals();

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
        List<Interval> intervals = tree.toIntervals();

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
        List<Interval> intervals = tree.toIntervals();

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
        List<Interval> intervals = tree.toIntervals();

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
        List<Interval> intervals = tree.toIntervals();

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
        List<Interval> intervals = tree.toIntervals();

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
        List<Interval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(3, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, 1);
        checkInterval(intervals.get(1), 1, 2);
        checkInterval(intervals.get(2), 2, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testToIntervals_adjacentIntervals() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(2, 3, TEST_PRECISION));

        // act
        List<Interval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), 1, 3);
    }

    @Test
    public void testToIntervals_multipleAdjacentIntervals() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(2, 3, TEST_PRECISION));
        tree.add(Interval.of(3, 4, TEST_PRECISION));

        tree.add(Interval.of(-2, -1, TEST_PRECISION));
        tree.add(Interval.of(5, 6, TEST_PRECISION));

        // act
        List<Interval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(3, intervals.size());
        checkInterval(intervals.get(0), -2, -1);
        checkInterval(intervals.get(1), 1, 4);
        checkInterval(intervals.get(2), 5, 6);
    }

    @Test
    public void testToIntervals() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(-1, 6, precision));

        // act
        List<Interval> intervals = tree.toIntervals();

        // assert
        Assert.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), -1, 6);
    }

    @Test
    public void testGetNodeRegion() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        RegionNode1D root = tree.getRoot();
        root.cut(OrientedPoints.createPositiveFacing(1.0, TEST_PRECISION));

        RegionNode1D minus = root.getMinus();
        minus.cut(OrientedPoints.createNegativeFacing(0.0, TEST_PRECISION));

        // act/assert
        checkInterval(root.getNodeRegion(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        checkInterval(minus.getNodeRegion(), Double.NEGATIVE_INFINITY, 1.0);
        checkInterval(root.getPlus().getNodeRegion(), 1.0, Double.POSITIVE_INFINITY);

        checkInterval(minus.getPlus().getNodeRegion(), Double.NEGATIVE_INFINITY, 0.0);
        checkInterval(minus.getMinus().getNodeRegion(), 0.0, 1.0);
    }

    @Test
    public void testTransform_full() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.full();

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.createScale(2);

        // act
        tree.transform(transform);

        // assert
        Assert.assertTrue(tree.isFull());
    }

    @Test
    public void testTransform_noReflection() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.min(3, TEST_PRECISION)
                );

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.createScale(2)
                .translate(3);

        // act
        tree.transform(transform);

        // assert
        List<Interval> intervals = tree.toIntervals();

        Assert.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), 5, 7);
        checkInterval(intervals.get(1), 9, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testTransform_withReflection() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.min(3, TEST_PRECISION)
                );

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.createScale(-2)
                .translate(3);

        // act
        tree.transform(transform);

        // assert
        List<Interval> intervals = tree.toIntervals();

        Assert.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, -3);
        checkInterval(intervals.get(1), -1, 1);
    }

    @Test
    public void testTransform_withReflection_functionBasedTransform() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.min(3, TEST_PRECISION)
                );

        AffineTransformMatrix1D transform = AffineTransformMatrix1D.from(Vector1D::negate);

        // act
        tree.transform(transform);

        // assert
        List<Interval> intervals = tree.toIntervals();

        Assert.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, -3);
        checkInterval(intervals.get(1), -2, -1);
    }

    @Test
    public void testSplit_full() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.full();
        OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(2, true, TEST_PRECISION);

        // act
        Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        List<Interval> minusIntervals = split.getMinus().toIntervals();
        Assert.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), Double.NEGATIVE_INFINITY, 2);

        List<Interval> plusIntervals = split.getPlus().toIntervals();
        Assert.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 2, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testSplit_empty() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(2, true, TEST_PRECISION);

        // act
        Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_bothSides() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.max(-2, TEST_PRECISION));
        tree.add(Interval.of(1, 4, TEST_PRECISION));

        OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(2, false, TEST_PRECISION);

        // act
        Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        List<Interval> minusIntervals = split.getMinus().toIntervals();
        Assert.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 2, 4);

        List<Interval> plusIntervals = split.getPlus().toIntervals();
        Assert.assertEquals(2, plusIntervals.size());
        checkInterval(plusIntervals.get(0), Double.NEGATIVE_INFINITY, -2);
        checkInterval(plusIntervals.get(1), 1, 2);
    }

    @Test
    public void testSplit_splitterOnBoundary_minus() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 4, TEST_PRECISION));

        OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(1, false, TEST_PRECISION);

        // act
        Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        List<Interval> minusIntervals = split.getMinus().toIntervals();
        Assert.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 1, 4);

        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_splitterOnBoundary_plus() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 4, TEST_PRECISION));

        OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(4, false, TEST_PRECISION);

        // act
        Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());

        List<Interval> plusIntervals = split.getPlus().toIntervals();
        Assert.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 4);
    }

    @Test
    public void testSplit_point() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.point(1.0, TEST_PRECISION));

        OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(2, false, TEST_PRECISION);

        // act
        Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());

        List<Interval> plusIntervals = split.getPlus().toIntervals();
        Assert.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1);
    }

    @Test
    public void testSplit_point_splitOnPoint_positiveFacingSplitter() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.point(1, TEST_PRECISION));

        OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(1, true, TEST_PRECISION);

        // act
        Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());

        List<Interval> plusIntervals = split.getPlus().toIntervals();
        Assert.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1);
    }

    @Test
    public void testSplit_point_splitOnPoint_negativeFacingSplitter() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.point(1, TEST_PRECISION));

        OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(1, false, TEST_PRECISION);

        // act
        Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        List<Interval> minusIntervals = split.getMinus().toIntervals();
        Assert.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 1, 1);

        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testGetSize_infinite() {
        // arrange
        RegionBSPTree1D full = RegionBSPTree1D.full();

        RegionBSPTree1D posHalfSpace = RegionBSPTree1D.empty();
        posHalfSpace.getRoot().cut(OrientedPoints.createNegativeFacing(-2.0, TEST_PRECISION));

        RegionBSPTree1D negHalfSpace = RegionBSPTree1D.empty();
        negHalfSpace.getRoot().cut(OrientedPoints.createPositiveFacing(3.0, TEST_PRECISION));

        // act/assert
        Assert.assertEquals(Double.POSITIVE_INFINITY, full.getSize(), TEST_EPS);
        Assert.assertEquals(Double.POSITIVE_INFINITY, posHalfSpace.getSize(), TEST_EPS);
        Assert.assertEquals(Double.POSITIVE_INFINITY, negHalfSpace.getSize(), TEST_EPS);
    }

    @Test
    public void testGetSize_empty() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act/assert
        Assert.assertEquals(0, tree.getSize(), TEST_EPS);
    }

    @Test
    public void testGetSize_exactPoints() {
        // arrange
        RegionBSPTree1D singlePoint = RegionBSPTree1D.empty();
        singlePoint.add(Interval.of(1, 1, TEST_PRECISION));

        RegionBSPTree1D multiplePoints = RegionBSPTree1D.empty();
        multiplePoints.add(Interval.of(1, 1, TEST_PRECISION));
        multiplePoints.add(Interval.of(-1, -1, TEST_PRECISION));
        multiplePoints.add(Interval.of(2, 2, TEST_PRECISION));

        // act/assert
        Assert.assertEquals(0, singlePoint.getSize(), TEST_EPS);
        Assert.assertEquals(0, multiplePoints.getSize(), TEST_EPS);
    }

    @Test
    public void testGetSize_pointsWithinPrecision() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);

        RegionBSPTree1D singlePoint = RegionBSPTree1D.empty();
        singlePoint.add(Interval.of(1, 1.02, precision));

        RegionBSPTree1D multiplePoints = RegionBSPTree1D.empty();
        multiplePoints.add(Interval.of(1, 1.02, precision));
        multiplePoints.add(Interval.of(-1.02, -1, precision));
        multiplePoints.add(Interval.of(2, 2.02, precision));

        // act/assert
        Assert.assertEquals(0.02, singlePoint.getSize(), TEST_EPS);
        Assert.assertEquals(0.06, multiplePoints.getSize(), TEST_EPS);
    }

    @Test
    public void testGetSize_nonEmptyIntervals() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(3, 5, TEST_PRECISION));

        // act/assert
        Assert.assertEquals(3, tree.getSize(), TEST_EPS);
    }

    @Test
    public void testGetSize_intervalWithPoints() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(3, 3, TEST_PRECISION));
        tree.add(Interval.of(5, 5, TEST_PRECISION));

        // act/assert
        Assert.assertEquals(1, tree.getSize(), TEST_EPS);
    }

    @Test
    public void testGetSize_complementedRegion() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(Double.NEGATIVE_INFINITY, 2, TEST_PRECISION));
        tree.add(Interval.of(4, Double.POSITIVE_INFINITY, TEST_PRECISION));

        tree.complement();

        // act/assert
        Assert.assertEquals(2, tree.getSize(), TEST_EPS);
    }

    @Test
    public void testGetBarycenter_infinite() {
        // arrange
        RegionBSPTree1D full = RegionBSPTree1D.full();

        RegionBSPTree1D posHalfSpace = RegionBSPTree1D.empty();
        posHalfSpace.getRoot().cut(OrientedPoints.createNegativeFacing(-2.0, TEST_PRECISION));

        RegionBSPTree1D negHalfSpace = RegionBSPTree1D.empty();
        negHalfSpace.getRoot().cut(OrientedPoints.createPositiveFacing(3.0, TEST_PRECISION));

        // act/assert
        Assert.assertNull(full.getBarycenter());
        Assert.assertNull(posHalfSpace.getBarycenter());
        Assert.assertNull(negHalfSpace.getBarycenter());
    }

    @Test
    public void testGetBarycenter_empty() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act/assert
        Assert.assertNull(tree.getBarycenter());
    }

    @Test
    public void testGetBarycenter_exactPoints() {
        // arrange
        RegionBSPTree1D singlePoint = RegionBSPTree1D.empty();
        singlePoint.add(Interval.of(1, 1, TEST_PRECISION));

        RegionBSPTree1D multiplePoints = RegionBSPTree1D.empty();
        multiplePoints.add(Interval.of(1, 1, TEST_PRECISION));
        multiplePoints.add(Interval.of(-1, -1, TEST_PRECISION));
        multiplePoints.add(Interval.of(6, 6, TEST_PRECISION));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1), singlePoint.getBarycenter(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(2), multiplePoints.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testGetBarycenter_pointsWithinPrecision() {
     // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);

        RegionBSPTree1D singlePoint = RegionBSPTree1D.empty();
        singlePoint.add(Interval.of(1, 1.02, precision));

        RegionBSPTree1D multiplePoints = RegionBSPTree1D.empty();
        multiplePoints.add(Interval.of(1, 1.02, precision));
        multiplePoints.add(Interval.of(-1.02, -1, precision));
        multiplePoints.add(Interval.of(6, 6.02, precision));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1.01), singlePoint.getBarycenter(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(6.01 / 3), multiplePoints.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testGetBarycenter_nonEmptyIntervals() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(3, 5, TEST_PRECISION));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(9.5 / 3), tree.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testGetBarycenter_complementedRegion() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(Double.NEGATIVE_INFINITY, 2, TEST_PRECISION));
        tree.add(Interval.of(4, Double.POSITIVE_INFINITY, TEST_PRECISION));

        tree.complement();

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(3), tree.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testGetBarycenter_intervalWithPoints() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(3, 3, TEST_PRECISION));
        tree.add(Interval.of(5, 5, TEST_PRECISION));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1.5), tree.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testGetMinMax_full() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getMin());
        GeometryTestUtils.assertNegativeInfinity(tree.getMax());
    }

    @Test
    public void testGetMinMax_empty() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getMin());
        GeometryTestUtils.assertNegativeInfinity(tree.getMax());
    }

    @Test
    public void testGetMinMax_halfSpaces() {
        // arrange
        RegionBSPTree1D posHalfSpace = RegionBSPTree1D.empty();
        posHalfSpace.getRoot().cut(OrientedPoints.createNegativeFacing(-2.0, TEST_PRECISION));

        RegionBSPTree1D negHalfSpace = RegionBSPTree1D.empty();
        negHalfSpace.getRoot().cut(OrientedPoints.createPositiveFacing(3.0, TEST_PRECISION));

        // act/assert
        Assert.assertEquals(-2, posHalfSpace.getMin(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(posHalfSpace.getMax());

        GeometryTestUtils.assertNegativeInfinity(negHalfSpace.getMin());
        Assert.assertEquals(3, negHalfSpace.getMax(), TEST_EPS);
    }

    @Test
    public void testGetMinMax_multipleIntervals() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(Arrays.asList(
                    Interval.of(3, 5, TEST_PRECISION),
                    Interval.of(-4, -2, TEST_PRECISION),
                    Interval.of(0, 0, TEST_PRECISION)
                ));

        // act/assert
        Assert.assertEquals(-4, tree.getMin(), TEST_EPS);
        Assert.assertEquals(5, tree.getMax(), TEST_EPS);
    }

    @Test
    public void testGetMinMax_pointsAtMinAndMax() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.from(Arrays.asList(
                    Interval.of(5, 5, TEST_PRECISION),
                    Interval.of(-4, -4, TEST_PRECISION),
                    Interval.of(0, 0, TEST_PRECISION)
                ));

        // act/assert
        Assert.assertEquals(-4, tree.getMin(), TEST_EPS);
        Assert.assertEquals(5, tree.getMax(), TEST_EPS);
    }

    @Test
    public void testFull_factoryMethod() {
        // act
        RegionBSPTree1D tree = RegionBSPTree1D.full();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
        Assert.assertNotSame(tree, RegionBSPTree1D.full());
    }

    @Test
    public void testEmpty_factoryMethod() {
        // act
        RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
        Assert.assertNotSame(tree, RegionBSPTree1D.full());
    }

    @Test
    public void testFromIntervals_iterable() {
        // act
        RegionBSPTree1D tree = RegionBSPTree1D.from(Arrays.asList(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.of(3, 4, TEST_PRECISION)
                ));

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.INSIDE, 1.5, 3.5);
        checkClassify(tree, RegionLocation.BOUNDARY, 1, 2, 3, 4);
        checkClassify(tree, RegionLocation.OUTSIDE, 0, 2.5, 5);

        Assert.assertEquals(2, tree.toIntervals().size());
    }

    @Test
    public void testFromIntervals_iterable_noItervals() {
        // act
        RegionBSPTree1D tree = RegionBSPTree1D.from(Arrays.asList());

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());

        Assert.assertEquals(0, tree.toIntervals().size());
    }

    @Test
    public void testFromIntervals_varargs() {
        // act
        RegionBSPTree1D tree = RegionBSPTree1D.from(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.of(3, 4, TEST_PRECISION)
                );

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.INSIDE, 1.5, 3.5);
        checkClassify(tree, RegionLocation.BOUNDARY, 1, 2, 3, 4);
        checkClassify(tree, RegionLocation.OUTSIDE, 0, 2.5, 5);

        Assert.assertEquals(2, tree.toIntervals().size());
    }

    private static void checkClassify(RegionBSPTree1D tree, RegionLocation loc, double... points) {
        for (double x : points) {
            String msg = "Unexpected location for point " + x;

            Assert.assertEquals(msg, loc, tree.classify(x));
            Assert.assertEquals(msg, loc, tree.classify(Vector1D.of(x)));
        }
    }

    private static void checkContains(RegionBSPTree1D tree, boolean contains, double... points) {
        for (double x : points) {
            String msg = "Unexpected contains status for point " + x;

            Assert.assertEquals(msg, contains, tree.contains(x));
            Assert.assertEquals(msg, contains, tree.contains(Vector1D.of(x)));
        }
    }

    private static void checkBoundaryProjection(RegionBSPTree1D tree, double location, double projectedLocation) {
        Vector1D pt = Vector1D.of(location);

        Vector1D proj = tree.project(pt);

        Assert.assertEquals(projectedLocation, proj.getX(), TEST_EPS);
    }

    private static void checkInterval(Interval interval, double min, double max) {
        checkInterval(interval, min, max, TEST_PRECISION);
    }

    private static void checkInterval(Interval interval, double min, double max, DoublePrecisionContext precision) {
        Assert.assertEquals(min, interval.getMin(), TEST_EPS);
        Assert.assertEquals(max, interval.getMax(), TEST_EPS);
    }
}
