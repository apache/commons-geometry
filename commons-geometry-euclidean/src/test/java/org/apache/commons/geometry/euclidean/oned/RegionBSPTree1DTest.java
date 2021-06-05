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
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D.RegionNode1D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RegionBSPTree1DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testCopy() {
        // arrange
        final RegionBSPTree1D tree = new RegionBSPTree1D(true);
        tree.getRoot().cut(OrientedPoints.createPositiveFacing(1.0, TEST_PRECISION));

        // act
        final RegionBSPTree1D copy = tree.copy();

        // assert
        Assertions.assertNotSame(tree, copy);
        Assertions.assertEquals(3, copy.count());
    }

    @Test
    void testClassify_fullRegion() {
        // arrange
        final RegionBSPTree1D tree = new RegionBSPTree1D(true);

        // act/assert
        checkClassify(tree, RegionLocation.INSIDE,
                Double.NEGATIVE_INFINITY, -1, 0, 1, Double.POSITIVE_INFINITY);

        checkClassify(tree, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    void testClassify_emptyRegion() {
        // arrange
        final RegionBSPTree1D tree = new RegionBSPTree1D(false);

        // act/assert
        checkClassify(tree, RegionLocation.OUTSIDE,
                Double.NEGATIVE_INFINITY, -1, 0, 1, Double.POSITIVE_INFINITY);

        checkClassify(tree, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    void testClassify_singleClosedInterval() {
        // arrange
        final RegionBSPTree1D tree = new RegionBSPTree1D();
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
    void testContains_fullRegion() {
        // arrange
        final RegionBSPTree1D tree = new RegionBSPTree1D(true);

        // act/assert
        checkContains(tree, true,
                Double.NEGATIVE_INFINITY, -1, 0, 1, Double.POSITIVE_INFINITY);

        checkContains(tree, false, Double.NaN);
    }

    @Test
    void testContains_emptyRegion() {
        // arrange
        final RegionBSPTree1D tree = new RegionBSPTree1D(false);

        // act/assert
        checkContains(tree, false,
                Double.NEGATIVE_INFINITY, -1, 0, 1, Double.POSITIVE_INFINITY);

        checkContains(tree, false, Double.NaN);
    }

    @Test
    void testContains_singleClosedInterval() {
        // arrange
        final RegionBSPTree1D tree = new RegionBSPTree1D();
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
    void testGetBoundarySize_alwaysReturnsZero() {
        // act/assert
        Assertions.assertEquals(0.0, RegionBSPTree1D.full().getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(0.0, RegionBSPTree1D.empty().getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(0.0, RegionBSPTree1D.from(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.of(4, 5, TEST_PRECISION)
                ).getBoundarySize(), TEST_EPS);
    }

    @Test
    void testProject_full() {
        // arrange
        final RegionBSPTree1D full = RegionBSPTree1D.full();

        // act/assert
        Assertions.assertNull(full.project(Vector1D.of(Double.NEGATIVE_INFINITY)));
        Assertions.assertNull(full.project(Vector1D.of(0)));
        Assertions.assertNull(full.project(Vector1D.of(Double.POSITIVE_INFINITY)));
    }

    @Test
    void testProject_empty() {
        // arrange
        final RegionBSPTree1D empty = RegionBSPTree1D.empty();

        // act/assert
        Assertions.assertNull(empty.project(Vector1D.of(Double.NEGATIVE_INFINITY)));
        Assertions.assertNull(empty.project(Vector1D.of(0)));
        Assertions.assertNull(empty.project(Vector1D.of(Double.POSITIVE_INFINITY)));
    }

    @Test
    void testProject_singlePoint() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.point(1, TEST_PRECISION));

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
    void testProject_noMinBoundary() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.of(Double.NEGATIVE_INFINITY, 1, TEST_PRECISION));

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
    void testProject_noMaxBoundary() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.of(1, Double.POSITIVE_INFINITY, TEST_PRECISION));

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
    void testProject_closedInterval() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.of(1, 3, TEST_PRECISION));

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
    void testProject_multipleIntervals() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(
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
    void testAdd_interval() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

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
    void testAdd_adjacentIntervals() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

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
    void testAdd_addFullInterval() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act
        tree.add(Interval.of(-1, 1, TEST_PRECISION));
        tree.add(Interval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION));

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    void testAdd_interval_duplicateBoundaryPoint() {
        // arrange
        final RegionBSPTree1D tree = new RegionBSPTree1D(false);

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
    void testToIntervals_fullRegion() {
        // arrange
        final RegionBSPTree1D tree = new RegionBSPTree1D(true);

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    void testToIntervals_emptyRegion() {
        // arrange
        final RegionBSPTree1D tree = new RegionBSPTree1D(false);

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(0, intervals.size());
    }

    @Test
    void testToIntervals_halfOpen_negative() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final RegionBSPTree1D tree = new RegionBSPTree1D();
        tree.getRoot().cut(OrientedPoints.fromLocationAndDirection(1.0, true, precision));

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, 1);
    }

    @Test
    void testToIntervals_halfOpen_positive() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final RegionBSPTree1D tree = new RegionBSPTree1D();
        tree.getRoot().cut(OrientedPoints.fromLocationAndDirection(-1.0, false, precision));

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), -1, Double.POSITIVE_INFINITY);
    }

    @Test
    void testToIntervals_singleClosedInterval() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(-1, 1, precision));

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), -1, 1);
    }

    @Test
    void testToIntervals_singleClosedInterval_complement() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(-1, 1, precision));
        tree.complement();

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, -1);
        checkInterval(intervals.get(1), 1, Double.POSITIVE_INFINITY);
    }

    @Test
    void testToIntervals_openAndClosedIntervals() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(Double.NEGATIVE_INFINITY, -10, precision));
        tree.add(Interval.of(-1, 1, precision));
        tree.add(Interval.of(10, Double.POSITIVE_INFINITY, precision));

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(3, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, -10);
        checkInterval(intervals.get(1), -1, 1);
        checkInterval(intervals.get(2), 10, Double.POSITIVE_INFINITY);
    }

    @Test
    void testToIntervals_singlePoint() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(1, 1, precision));

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), 1, 1);
    }

    @Test
    void testToIntervals_singlePoint_complement() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(1, 1, precision));
        tree.complement();

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, 1);
        checkInterval(intervals.get(1), 1, Double.POSITIVE_INFINITY);
    }

    @Test
    void testToIntervals_multiplePoints() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(1, 1, precision));
        tree.add(Interval.of(2, 2, precision));

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), 1, 1);
        checkInterval(intervals.get(1), 2, 2);
    }

    @Test
    void testToIntervals_multiplePoints_complement() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(1, 1, precision));
        tree.add(Interval.of(2, 2, precision));
        tree.complement();

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(3, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, 1);
        checkInterval(intervals.get(1), 1, 2);
        checkInterval(intervals.get(2), 2, Double.POSITIVE_INFINITY);
    }

    @Test
    void testToIntervals_adjacentIntervals() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(2, 3, TEST_PRECISION));

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), 1, 3);
    }

    @Test
    void testToIntervals_multipleAdjacentIntervals() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(2, 3, TEST_PRECISION));
        tree.add(Interval.of(3, 4, TEST_PRECISION));

        tree.add(Interval.of(-2, -1, TEST_PRECISION));
        tree.add(Interval.of(5, 6, TEST_PRECISION));

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(3, intervals.size());
        checkInterval(intervals.get(0), -2, -1);
        checkInterval(intervals.get(1), 1, 4);
        checkInterval(intervals.get(2), 5, 6);
    }

    @Test
    void testToIntervals() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final RegionBSPTree1D tree = new RegionBSPTree1D(false);
        tree.add(Interval.of(-1, 6, precision));

        // act
        final List<Interval> intervals = tree.toIntervals();

        // assert
        Assertions.assertEquals(1, intervals.size());
        checkInterval(intervals.get(0), -1, 6);
    }

    @Test
    void testGetNodeRegion() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

        final RegionNode1D root = tree.getRoot();
        root.cut(OrientedPoints.createPositiveFacing(1.0, TEST_PRECISION));

        final RegionNode1D minus = root.getMinus();
        minus.cut(OrientedPoints.createNegativeFacing(0.0, TEST_PRECISION));

        // act/assert
        checkInterval(root.getNodeRegion(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        checkInterval(minus.getNodeRegion(), Double.NEGATIVE_INFINITY, 1.0);
        checkInterval(root.getPlus().getNodeRegion(), 1.0, Double.POSITIVE_INFINITY);

        checkInterval(minus.getPlus().getNodeRegion(), Double.NEGATIVE_INFINITY, 0.0);
        checkInterval(minus.getMinus().getNodeRegion(), 0.0, 1.0);
    }

    @Test
    void testTransform_full() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.full();

        final AffineTransformMatrix1D transform = AffineTransformMatrix1D.createScale(2);

        // act
        tree.transform(transform);

        // assert
        Assertions.assertTrue(tree.isFull());
    }

    @Test
    void testTransform_noReflection() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.min(3, TEST_PRECISION)
                );

        final AffineTransformMatrix1D transform = AffineTransformMatrix1D.createScale(2)
                .translate(3);

        // act
        tree.transform(transform);

        // assert
        final List<Interval> intervals = tree.toIntervals();

        Assertions.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), 5, 7);
        checkInterval(intervals.get(1), 9, Double.POSITIVE_INFINITY);
    }

    @Test
    void testTransform_withReflection() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.min(3, TEST_PRECISION)
                );

        final AffineTransformMatrix1D transform = AffineTransformMatrix1D.createScale(-2)
                .translate(3);

        // act
        tree.transform(transform);

        // assert
        final List<Interval> intervals = tree.toIntervals();

        Assertions.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, -3);
        checkInterval(intervals.get(1), -1, 1);
    }

    @Test
    void testTransform_withReflection_functionBasedTransform() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.min(3, TEST_PRECISION)
                );

        final AffineTransformMatrix1D transform = AffineTransformMatrix1D.from(Vector1D::negate);

        // act
        tree.transform(transform);

        // assert
        final List<Interval> intervals = tree.toIntervals();

        Assertions.assertEquals(2, intervals.size());
        checkInterval(intervals.get(0), Double.NEGATIVE_INFINITY, -3);
        checkInterval(intervals.get(1), -2, -1);
    }

    @Test
    void testSplit_full() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.full();
        final OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(2, true, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final List<Interval> minusIntervals = split.getMinus().toIntervals();
        Assertions.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), Double.NEGATIVE_INFINITY, 2);

        final List<Interval> plusIntervals = split.getPlus().toIntervals();
        Assertions.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 2, Double.POSITIVE_INFINITY);
    }

    @Test
    void testSplit_empty() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        final OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(2, true, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    void testSplit_bothSides() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.max(-2, TEST_PRECISION));
        tree.add(Interval.of(1, 4, TEST_PRECISION));

        final OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(2, false, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final List<Interval> minusIntervals = split.getMinus().toIntervals();
        Assertions.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 2, 4);

        final List<Interval> plusIntervals = split.getPlus().toIntervals();
        Assertions.assertEquals(2, plusIntervals.size());
        checkInterval(plusIntervals.get(0), Double.NEGATIVE_INFINITY, -2);
        checkInterval(plusIntervals.get(1), 1, 2);
    }

    @Test
    void testSplit_splitterOnBoundary_minus() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 4, TEST_PRECISION));

        final OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(1, false, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        final List<Interval> minusIntervals = split.getMinus().toIntervals();
        Assertions.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 1, 4);

        Assertions.assertNull(split.getPlus());
    }

    @Test
    void testSplit_splitterOnBoundary_plus() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 4, TEST_PRECISION));

        final OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(4, false, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());

        final List<Interval> plusIntervals = split.getPlus().toIntervals();
        Assertions.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 4);
    }

    @Test
    void testSplit_point() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.point(1.0, TEST_PRECISION));

        final OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(2, false, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());

        final List<Interval> plusIntervals = split.getPlus().toIntervals();
        Assertions.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1);
    }

    @Test
    void testSplit_point_splitOnPoint_positiveFacingSplitter() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.point(1, TEST_PRECISION));

        final OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(1, true, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());

        final List<Interval> plusIntervals = split.getPlus().toIntervals();
        Assertions.assertEquals(1, plusIntervals.size());
        checkInterval(plusIntervals.get(0), 1, 1);
    }

    @Test
    void testSplit_point_splitOnPoint_negativeFacingSplitter() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(Interval.point(1, TEST_PRECISION));

        final OrientedPoint splitter = OrientedPoints.fromLocationAndDirection(1, false, TEST_PRECISION);

        // act
        final Split<RegionBSPTree1D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        final List<Interval> minusIntervals = split.getMinus().toIntervals();
        Assertions.assertEquals(1, minusIntervals.size());
        checkInterval(minusIntervals.get(0), 1, 1);

        Assertions.assertNull(split.getPlus());
    }

    @Test
    void testGetSize_infinite() {
        // arrange
        final RegionBSPTree1D full = RegionBSPTree1D.full();

        final RegionBSPTree1D posHalfSpace = RegionBSPTree1D.empty();
        posHalfSpace.getRoot().cut(OrientedPoints.createNegativeFacing(-2.0, TEST_PRECISION));

        final RegionBSPTree1D negHalfSpace = RegionBSPTree1D.empty();
        negHalfSpace.getRoot().cut(OrientedPoints.createPositiveFacing(3.0, TEST_PRECISION));

        // act/assert
        Assertions.assertEquals(Double.POSITIVE_INFINITY, full.getSize(), TEST_EPS);
        Assertions.assertEquals(Double.POSITIVE_INFINITY, posHalfSpace.getSize(), TEST_EPS);
        Assertions.assertEquals(Double.POSITIVE_INFINITY, negHalfSpace.getSize(), TEST_EPS);
    }

    @Test
    void testGetSize_empty() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act/assert
        Assertions.assertEquals(0, tree.getSize(), TEST_EPS);
    }

    @Test
    void testGetSize_exactPoints() {
        // arrange
        final RegionBSPTree1D singlePoint = RegionBSPTree1D.empty();
        singlePoint.add(Interval.of(1, 1, TEST_PRECISION));

        final RegionBSPTree1D multiplePoints = RegionBSPTree1D.empty();
        multiplePoints.add(Interval.of(1, 1, TEST_PRECISION));
        multiplePoints.add(Interval.of(-1, -1, TEST_PRECISION));
        multiplePoints.add(Interval.of(2, 2, TEST_PRECISION));

        // act/assert
        Assertions.assertEquals(0, singlePoint.getSize(), TEST_EPS);
        Assertions.assertEquals(0, multiplePoints.getSize(), TEST_EPS);
    }

    @Test
    void testGetSize_pointsWithinPrecision() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-1);

        final RegionBSPTree1D singlePoint = RegionBSPTree1D.empty();
        singlePoint.add(Interval.of(1, 1.02, precision));

        final RegionBSPTree1D multiplePoints = RegionBSPTree1D.empty();
        multiplePoints.add(Interval.of(1, 1.02, precision));
        multiplePoints.add(Interval.of(-1.02, -1, precision));
        multiplePoints.add(Interval.of(2, 2.02, precision));

        // act/assert
        Assertions.assertEquals(0.02, singlePoint.getSize(), TEST_EPS);
        Assertions.assertEquals(0.06, multiplePoints.getSize(), TEST_EPS);
    }

    @Test
    void testGetSize_nonEmptyIntervals() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(3, 5, TEST_PRECISION));

        // act/assert
        Assertions.assertEquals(3, tree.getSize(), TEST_EPS);
    }

    @Test
    void testGetSize_intervalWithPoints() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(3, 3, TEST_PRECISION));
        tree.add(Interval.of(5, 5, TEST_PRECISION));

        // act/assert
        Assertions.assertEquals(1, tree.getSize(), TEST_EPS);
    }

    @Test
    void testGetSize_complementedRegion() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(Double.NEGATIVE_INFINITY, 2, TEST_PRECISION));
        tree.add(Interval.of(4, Double.POSITIVE_INFINITY, TEST_PRECISION));

        tree.complement();

        // act/assert
        Assertions.assertEquals(2, tree.getSize(), TEST_EPS);
    }

    @Test
    void testGetCentroid_infinite() {
        // arrange
        final RegionBSPTree1D full = RegionBSPTree1D.full();

        final RegionBSPTree1D posHalfSpace = RegionBSPTree1D.empty();
        posHalfSpace.getRoot().cut(OrientedPoints.createNegativeFacing(-2.0, TEST_PRECISION));

        final RegionBSPTree1D negHalfSpace = RegionBSPTree1D.empty();
        negHalfSpace.getRoot().cut(OrientedPoints.createPositiveFacing(3.0, TEST_PRECISION));

        // act/assert
        Assertions.assertNull(full.getCentroid());
        Assertions.assertNull(posHalfSpace.getCentroid());
        Assertions.assertNull(negHalfSpace.getCentroid());
    }

    @Test
    void testGetCentroid_empty() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act/assert
        Assertions.assertNull(tree.getCentroid());
    }

    @Test
    void testGetCentroid_exactPoints() {
        // arrange
        final RegionBSPTree1D singlePoint = RegionBSPTree1D.empty();
        singlePoint.add(Interval.of(1, 1, TEST_PRECISION));

        final RegionBSPTree1D multiplePoints = RegionBSPTree1D.empty();
        multiplePoints.add(Interval.of(1, 1, TEST_PRECISION));
        multiplePoints.add(Interval.of(-1, -1, TEST_PRECISION));
        multiplePoints.add(Interval.of(6, 6, TEST_PRECISION));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1), singlePoint.getCentroid(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(2), multiplePoints.getCentroid(), TEST_EPS);
    }

    @Test
    void testGetCentroid_pointsWithinPrecision() {
     // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-1);

        final RegionBSPTree1D singlePoint = RegionBSPTree1D.empty();
        singlePoint.add(Interval.of(1, 1.02, precision));

        final RegionBSPTree1D multiplePoints = RegionBSPTree1D.empty();
        multiplePoints.add(Interval.of(1, 1.02, precision));
        multiplePoints.add(Interval.of(-1.02, -1, precision));
        multiplePoints.add(Interval.of(6, 6.02, precision));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1.01), singlePoint.getCentroid(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(6.01 / 3), multiplePoints.getCentroid(), TEST_EPS);
    }

    @Test
    void testGetCentroid_nonEmptyIntervals() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(3, 5, TEST_PRECISION));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(9.5 / 3), tree.getCentroid(), TEST_EPS);
    }

    @Test
    void testGetCentroid_complementedRegion() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(Double.NEGATIVE_INFINITY, 2, TEST_PRECISION));
        tree.add(Interval.of(4, Double.POSITIVE_INFINITY, TEST_PRECISION));

        tree.complement();

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(3), tree.getCentroid(), TEST_EPS);
    }

    @Test
    void testGetCentroid_intervalWithPoints() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();
        tree.add(Interval.of(1, 2, TEST_PRECISION));
        tree.add(Interval.of(3, 3, TEST_PRECISION));
        tree.add(Interval.of(5, 5, TEST_PRECISION));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(1.5), tree.getCentroid(), TEST_EPS);
    }

    @Test
    void testGetMinMax_full() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getMin());
        GeometryTestUtils.assertNegativeInfinity(tree.getMax());
    }

    @Test
    void testGetMinMax_empty() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getMin());
        GeometryTestUtils.assertNegativeInfinity(tree.getMax());
    }

    @Test
    void testGetMinMax_halfSpaces() {
        // arrange
        final RegionBSPTree1D posHalfSpace = RegionBSPTree1D.empty();
        posHalfSpace.getRoot().cut(OrientedPoints.createNegativeFacing(-2.0, TEST_PRECISION));

        final RegionBSPTree1D negHalfSpace = RegionBSPTree1D.empty();
        negHalfSpace.getRoot().cut(OrientedPoints.createPositiveFacing(3.0, TEST_PRECISION));

        // act/assert
        Assertions.assertEquals(-2, posHalfSpace.getMin(), TEST_EPS);
        GeometryTestUtils.assertPositiveInfinity(posHalfSpace.getMax());

        GeometryTestUtils.assertNegativeInfinity(negHalfSpace.getMin());
        Assertions.assertEquals(3, negHalfSpace.getMax(), TEST_EPS);
    }

    @Test
    void testGetMinMax_multipleIntervals() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(Arrays.asList(
                    Interval.of(3, 5, TEST_PRECISION),
                    Interval.of(-4, -2, TEST_PRECISION),
                    Interval.of(0, 0, TEST_PRECISION)
                ));

        // act/assert
        Assertions.assertEquals(-4, tree.getMin(), TEST_EPS);
        Assertions.assertEquals(5, tree.getMax(), TEST_EPS);
    }

    @Test
    void testGetMinMax_pointsAtMinAndMax() {
        // arrange
        final RegionBSPTree1D tree = RegionBSPTree1D.from(Arrays.asList(
                    Interval.of(5, 5, TEST_PRECISION),
                    Interval.of(-4, -4, TEST_PRECISION),
                    Interval.of(0, 0, TEST_PRECISION)
                ));

        // act/assert
        Assertions.assertEquals(-4, tree.getMin(), TEST_EPS);
        Assertions.assertEquals(5, tree.getMax(), TEST_EPS);
    }

    @Test
    void testFull_factoryMethod() {
        // act
        final RegionBSPTree1D tree = RegionBSPTree1D.full();

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertNotSame(tree, RegionBSPTree1D.full());
    }

    @Test
    void testEmpty_factoryMethod() {
        // act
        final RegionBSPTree1D tree = RegionBSPTree1D.empty();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
        Assertions.assertNotSame(tree, RegionBSPTree1D.full());
    }

    @Test
    void testFromIntervals_iterable() {
        // act
        final RegionBSPTree1D tree = RegionBSPTree1D.from(Arrays.asList(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.of(3, 4, TEST_PRECISION)
                ));

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.INSIDE, 1.5, 3.5);
        checkClassify(tree, RegionLocation.BOUNDARY, 1, 2, 3, 4);
        checkClassify(tree, RegionLocation.OUTSIDE, 0, 2.5, 5);

        Assertions.assertEquals(2, tree.toIntervals().size());
    }

    @Test
    void testFromIntervals_iterable_noItervals() {
        // act
        final RegionBSPTree1D tree = RegionBSPTree1D.from(Collections.emptyList());

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());

        Assertions.assertEquals(0, tree.toIntervals().size());
    }

    @Test
    void testFromIntervals_varargs() {
        // act
        final RegionBSPTree1D tree = RegionBSPTree1D.from(
                    Interval.of(1, 2, TEST_PRECISION),
                    Interval.of(3, 4, TEST_PRECISION)
                );

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.INSIDE, 1.5, 3.5);
        checkClassify(tree, RegionLocation.BOUNDARY, 1, 2, 3, 4);
        checkClassify(tree, RegionLocation.OUTSIDE, 0, 2.5, 5);

        Assertions.assertEquals(2, tree.toIntervals().size());
    }

    private static void checkClassify(final RegionBSPTree1D tree, final RegionLocation loc, final double... points) {
        for (final double x : points) {
            final String msg = "Unexpected location for point " + x;

            Assertions.assertEquals(loc, tree.classify(x), msg);
            Assertions.assertEquals(loc, tree.classify(Vector1D.of(x)), msg);
        }
    }

    private static void checkContains(final RegionBSPTree1D tree, final boolean contains, final double... points) {
        for (final double x : points) {
            final String msg = "Unexpected contains status for point " + x;

            Assertions.assertEquals(contains, tree.contains(x), msg);
            Assertions.assertEquals(contains, tree.contains(Vector1D.of(x)), msg);
        }
    }

    private static void checkBoundaryProjection(final RegionBSPTree1D tree, final double location, final double projectedLocation) {
        final Vector1D pt = Vector1D.of(location);

        final Vector1D proj = tree.project(pt);

        Assertions.assertEquals(projectedLocation, proj.getX(), TEST_EPS);
    }

    private static void checkInterval(final Interval interval, final double min, final double max) {
        checkInterval(interval, min, max, TEST_PRECISION);
    }

    private static void checkInterval(final Interval interval, final double min, final double max, final Precision.DoubleEquivalence precision) {
        Assertions.assertEquals(min, interval.getMin(), TEST_EPS);
        Assertions.assertEquals(max, interval.getMax(), TEST_EPS);
    }
}
