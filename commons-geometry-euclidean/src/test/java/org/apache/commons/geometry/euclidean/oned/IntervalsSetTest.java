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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.partitioning.BSPTree;
import org.apache.commons.geometry.core.partitioning.BoundaryProjection;
import org.apache.commons.geometry.core.partitioning.Region;
import org.apache.commons.geometry.core.partitioning.RegionFactory;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class IntervalsSetTest {

    private static final double TEST_TOLERANCE = 1e-15;

    @Test
    public void testInterval_wholeNumberLine() {
        // act
        IntervalsSet set = new IntervalsSet(TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        EuclideanTestUtils.assertNegativeInfinity(set.getInf());
        EuclideanTestUtils.assertPositiveInfinity(set.getSup());
        EuclideanTestUtils.assertPositiveInfinity(set.getSize());
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.NaN, set.getBarycenter(), TEST_TOLERANCE);

        BSPTree<Point1D> tree = set.getTree(true);
        Assert.assertEquals(Boolean.TRUE, tree.getAttribute());
        Assert.assertNull(tree.getCut());
        Assert.assertNull(tree.getMinus());
        Assert.assertNull(tree.getPlus());

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.INSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.INSIDE, set, 0.0);
        assertLocation(Region.Location.INSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testInterval_doubleOpenInterval() {
        // act
        IntervalsSet set = new IntervalsSet(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        EuclideanTestUtils.assertNegativeInfinity(set.getInf());
        EuclideanTestUtils.assertPositiveInfinity(set.getSup());
        EuclideanTestUtils.assertPositiveInfinity(set.getSize());
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.NaN, set.getBarycenter(), TEST_TOLERANCE);

        BSPTree<Point1D> tree = set.getTree(true);
        Assert.assertEquals(Boolean.TRUE, tree.getAttribute());
        Assert.assertNull(tree.getCut());
        Assert.assertNull(tree.getMinus());
        Assert.assertNull(tree.getPlus());

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.INSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.INSIDE, set, 0.0);
        assertLocation(Region.Location.INSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testInterval_openInterval_positive() {
        // act
        IntervalsSet set = new IntervalsSet(9.0, Double.POSITIVE_INFINITY, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        Assert.assertEquals(9.0, set.getInf(), TEST_TOLERANCE);
        EuclideanTestUtils.assertPositiveInfinity(set.getSup());
        EuclideanTestUtils.assertPositiveInfinity(set.getSize());
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.NaN, set.getBarycenter(), TEST_TOLERANCE);

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(9.0, Double.POSITIVE_INFINITY, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.OUTSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.OUTSIDE, set, 0.0);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 - 1e-16);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 + 1e-16);
        assertLocation(Region.Location.INSIDE, set, 10.0);
        assertLocation(Region.Location.INSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testInterval_openInterval_negative() {
        // act
        IntervalsSet set = new IntervalsSet(Double.NEGATIVE_INFINITY, 9.0, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        EuclideanTestUtils.assertNegativeInfinity(set.getInf());
        Assert.assertEquals(9.0, set.getSup(), TEST_TOLERANCE);
        EuclideanTestUtils.assertPositiveInfinity(set.getSize());
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.NaN, set.getBarycenter(), TEST_TOLERANCE);

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(Double.NEGATIVE_INFINITY, 9.0, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.INSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.INSIDE, set, 0.0);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 - 1e-16);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 + 1e-16);
        assertLocation(Region.Location.OUTSIDE, set, 10.0);
        assertLocation(Region.Location.OUTSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testInterval_singleClosedInterval() {
        // act
        IntervalsSet set = new IntervalsSet(-1.0, 9.0, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        Assert.assertEquals(-1.0, set.getInf(), TEST_TOLERANCE);
        Assert.assertEquals(9.0, set.getSup(), TEST_TOLERANCE);
        Assert.assertEquals(10.0, set.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.of(4.0), set.getBarycenter(), TEST_TOLERANCE);

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(-1.0, 9.0, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.OUTSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.OUTSIDE, set, -2.0);
        assertLocation(Region.Location.INSIDE, set, 0.0);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 - 1e-16);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 + 1e-16);
        assertLocation(Region.Location.OUTSIDE, set, 10.0);
        assertLocation(Region.Location.OUTSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testInterval_singlePoint() {
        // act
        IntervalsSet set = new IntervalsSet(1.0, 1.0, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        Assert.assertEquals(1.0, set.getInf(), TEST_TOLERANCE);
        Assert.assertEquals(1.0, set.getSup(), TEST_TOLERANCE);
        Assert.assertEquals(0.0, set.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.of(1.0), set.getBarycenter(), TEST_TOLERANCE);

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(1.0, 1.0, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.OUTSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.OUTSIDE, set, 0.0);
        assertLocation(Region.Location.BOUNDARY, set, 1.0);
        assertLocation(Region.Location.OUTSIDE, set, 2.0);
        assertLocation(Region.Location.OUTSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testFromBoundaries_wholeNumberLine() {
        // arrange
        List<SubHyperplane<Point1D>> boundaries = new ArrayList<>();

        // act
        IntervalsSet set = new IntervalsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        EuclideanTestUtils.assertNegativeInfinity(set.getInf());
        EuclideanTestUtils.assertPositiveInfinity(set.getSup());
        EuclideanTestUtils.assertPositiveInfinity(set.getSize());
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.NaN, set.getBarycenter(), TEST_TOLERANCE);

        BSPTree<Point1D> tree = set.getTree(true);
        Assert.assertEquals(Boolean.TRUE, tree.getAttribute());
        Assert.assertNull(tree.getCut());
        Assert.assertNull(tree.getMinus());
        Assert.assertNull(tree.getPlus());

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.INSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.INSIDE, set, 0.0);
        assertLocation(Region.Location.INSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testFromBoundaries_openInterval_positive() {
        // arrange
        List<SubHyperplane<Point1D>> boundaries = new ArrayList<>();
        boundaries.add(subOrientedPoint(9.0, false));

        // act
        IntervalsSet set = new IntervalsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        Assert.assertEquals(9.0, set.getInf(), TEST_TOLERANCE);
        EuclideanTestUtils.assertPositiveInfinity(set.getSup());
        EuclideanTestUtils.assertPositiveInfinity(set.getSize());
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.NaN, set.getBarycenter(), TEST_TOLERANCE);

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(9.0, Double.POSITIVE_INFINITY, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.OUTSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.OUTSIDE, set, 0.0);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 - 1e-16);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 + 1e-16);
        assertLocation(Region.Location.INSIDE, set, 10.0);
        assertLocation(Region.Location.INSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testFromBoundaries_openInterval_negative() {
        // arrange
        List<SubHyperplane<Point1D>> boundaries = new ArrayList<>();
        boundaries.add(subOrientedPoint(9.0, true));

        // act
        IntervalsSet set = new IntervalsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        EuclideanTestUtils.assertNegativeInfinity(set.getInf());
        Assert.assertEquals(9.0, set.getSup(), TEST_TOLERANCE);
        EuclideanTestUtils.assertPositiveInfinity(set.getSize());
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.NaN, set.getBarycenter(), TEST_TOLERANCE);

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(Double.NEGATIVE_INFINITY, 9.0, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.INSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.INSIDE, set, 0.0);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 - 1e-16);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 + 1e-16);
        assertLocation(Region.Location.OUTSIDE, set, 10.0);
        assertLocation(Region.Location.OUTSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testFromBoundaries_singleClosedInterval() {
        // arrange
        List<SubHyperplane<Point1D>> boundaries = new ArrayList<>();
        boundaries.add(subOrientedPoint(-1.0, false));
        boundaries.add(subOrientedPoint(9.0, true));

        // act
        IntervalsSet set = new IntervalsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        Assert.assertEquals(-1.0, set.getInf(), TEST_TOLERANCE);
        Assert.assertEquals(9.0, set.getSup(), TEST_TOLERANCE);
        Assert.assertEquals(10.0, set.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.of(4.0), set.getBarycenter(), TEST_TOLERANCE);

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(-1.0, 9.0, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.OUTSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.OUTSIDE, set, -2.0);
        assertLocation(Region.Location.INSIDE, set, 0.0);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 - 1e-16);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 + 1e-16);
        assertLocation(Region.Location.OUTSIDE, set, 10.0);
        assertLocation(Region.Location.OUTSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testFromBoundaries_multipleClosedIntervals() {
        // arrange
        List<SubHyperplane<Point1D>> boundaries = new ArrayList<>();
        boundaries.add(subOrientedPoint(-1.0, false));
        boundaries.add(subOrientedPoint(2.0, true));
        boundaries.add(subOrientedPoint(5.0, false));
        boundaries.add(subOrientedPoint(9.0, true));

        // act
        IntervalsSet set = new IntervalsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        Assert.assertEquals(-1.0, set.getInf(), TEST_TOLERANCE);
        Assert.assertEquals(9.0, set.getSup(), TEST_TOLERANCE);
        Assert.assertEquals(7.0, set.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.of(29.5 / 7.0), set.getBarycenter(), TEST_TOLERANCE);

        List<Interval> intervals = set.asList();
        Assert.assertEquals(2, intervals.size());
        assertInterval(-1.0, 2.0, intervals.get(0), TEST_TOLERANCE);
        assertInterval(5.0, 9.0, intervals.get(1), TEST_TOLERANCE);

        assertLocation(Region.Location.OUTSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.OUTSIDE, set, -2.0);
        assertLocation(Region.Location.INSIDE, set, 0.0);
        assertLocation(Region.Location.OUTSIDE, set, 3.0);
        assertLocation(Region.Location.INSIDE, set, 6.0);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 - 1e-16);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 + 1e-16);
        assertLocation(Region.Location.OUTSIDE, set, 10.0);
        assertLocation(Region.Location.OUTSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testFromBoundaries_mixedOpenAndClosedIntervals() {
        // arrange
        List<SubHyperplane<Point1D>> boundaries = new ArrayList<>();
        boundaries.add(subOrientedPoint(-2.0, true));
        boundaries.add(subOrientedPoint(-1.0, false));
        boundaries.add(subOrientedPoint(2.0, true));
        boundaries.add(subOrientedPoint(5.0, false));
        boundaries.add(subOrientedPoint(9.0, true));
        boundaries.add(subOrientedPoint(10.0, false));

        // act
        IntervalsSet set = new IntervalsSet(boundaries, TEST_TOLERANCE);

        // assert
        Assert.assertEquals(TEST_TOLERANCE, set.getTolerance(), Precision.SAFE_MIN);
        EuclideanTestUtils.assertNegativeInfinity(set.getInf());
        EuclideanTestUtils.assertPositiveInfinity(set.getSup());
        EuclideanTestUtils.assertPositiveInfinity(set.getSize());
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.of(Double.NaN), set.getBarycenter(), TEST_TOLERANCE);

        List<Interval> intervals = set.asList();
        Assert.assertEquals(4, intervals.size());
        assertInterval(Double.NEGATIVE_INFINITY, -2.0, intervals.get(0), TEST_TOLERANCE);
        assertInterval(-1.0, 2.0, intervals.get(1), TEST_TOLERANCE);
        assertInterval(5.0, 9.0, intervals.get(2), TEST_TOLERANCE);
        assertInterval(10.0, Double.POSITIVE_INFINITY, intervals.get(3), TEST_TOLERANCE);

        assertLocation(Region.Location.INSIDE, set, Double.NEGATIVE_INFINITY);
        assertLocation(Region.Location.INSIDE, set, -3);
        assertLocation(Region.Location.OUTSIDE, set, -1.5);
        assertLocation(Region.Location.INSIDE, set, 0.0);
        assertLocation(Region.Location.OUTSIDE, set, 3.0);
        assertLocation(Region.Location.INSIDE, set, 6.0);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 - 1e-16);
        assertLocation(Region.Location.BOUNDARY, set, 9.0 + 1e-16);
        assertLocation(Region.Location.OUTSIDE, set, 9.5);
        assertLocation(Region.Location.INSIDE, set, 11.0);
        assertLocation(Region.Location.INSIDE, set, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testFromBoundaries_intervalEqualToTolerance_onlyFirstBoundaryUsed() {
        // arrange
        double tolerance = 1e-3;
        double first = 1.0;
        double second = 1.0 + tolerance;
        List<SubHyperplane<Point1D>> boundaries = new ArrayList<>();
        boundaries.add(subOrientedPoint(first, true, tolerance));
        boundaries.add(subOrientedPoint(second, false, tolerance));

        // act
        IntervalsSet set = new IntervalsSet(boundaries, tolerance);

        // assert
        Assert.assertEquals(tolerance, set.getTolerance(), Precision.SAFE_MIN);
        EuclideanTestUtils.assertNegativeInfinity(set.getInf());
        Assert.assertEquals(first, set.getSup(), TEST_TOLERANCE);
        EuclideanTestUtils.assertPositiveInfinity(set.getSize());
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.NaN, set.getBarycenter(), TEST_TOLERANCE);

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(Double.NEGATIVE_INFINITY, first, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.INSIDE, set, 0.0);
        assertLocation(Region.Location.BOUNDARY, set, 1.0);
        assertLocation(Region.Location.OUTSIDE, set, 2.0);
    }

    @Test
    public void testFromBoundaries_intervalSmallerThanTolerance_onlyFirstBoundaryUsed() {
        // arrange
        double tolerance = 1e-3;
        double first = 1.0;
        double second = 1.0 - 1e-4;
        List<SubHyperplane<Point1D>> boundaries = new ArrayList<>();
        boundaries.add(subOrientedPoint(first, false, tolerance));
        boundaries.add(subOrientedPoint(second, true, tolerance));

        // act
        IntervalsSet set = new IntervalsSet(boundaries, tolerance);

        // assert
        Assert.assertEquals(tolerance, set.getTolerance(), Precision.SAFE_MIN);
        Assert.assertEquals(first, set.getInf(), TEST_TOLERANCE);
        EuclideanTestUtils.assertPositiveInfinity(set.getSup());
        EuclideanTestUtils.assertPositiveInfinity(set.getSize());
        Assert.assertEquals(0.0, set.getBoundarySize(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(Point1D.NaN, set.getBarycenter(), TEST_TOLERANCE);

        List<Interval> intervals = set.asList();
        Assert.assertEquals(1, intervals.size());
        assertInterval(first, Double.POSITIVE_INFINITY, intervals.get(0), TEST_TOLERANCE);

        assertLocation(Region.Location.OUTSIDE, set, 0.0);
        assertLocation(Region.Location.BOUNDARY, set, 1.0);
        assertLocation(Region.Location.INSIDE, set, 2.0);
    }

    @Test
    public void testProjectToBoundary() {
        // arrange
        List<SubHyperplane<Point1D>> boundaries = new ArrayList<>();
        boundaries.add(subOrientedPoint(-2.0, true));
        boundaries.add(subOrientedPoint(-1.0, false));
        boundaries.add(subOrientedPoint(2.0, true));
        boundaries.add(subOrientedPoint(5.0, false));
        boundaries.add(subOrientedPoint(9.0, true));
        boundaries.add(subOrientedPoint(10.0, false));

        IntervalsSet set = new IntervalsSet(boundaries, TEST_TOLERANCE);

        // act/assert
        assertProjection(Point1D.of(-2), -1, set, Point1D.of(-3));
        assertProjection(Point1D.of(-2), 0, set, Point1D.of(-2));
        assertProjection(Point1D.of(-2), 0.1, set, Point1D.of(-1.9));

        assertProjection(Point1D.of(-1), 0.5, set, Point1D.of(-1.5));
        assertProjection(Point1D.of(-1), 0.1, set, Point1D.of(-1.1));
        assertProjection(Point1D.of(-1), 0, set, Point1D.of(-1));
        assertProjection(Point1D.of(-1), -1, set, Point1D.of(0));

        assertProjection(Point1D.of(2), -1, set, Point1D.of(1));
        assertProjection(Point1D.of(2), 0, set, Point1D.of(2));
        assertProjection(Point1D.of(2), 1, set, Point1D.of(3));

        assertProjection(Point1D.of(5), 1, set, Point1D.of(4));
        assertProjection(Point1D.of(5), 0, set, Point1D.of(5));

        assertProjection(Point1D.of(5), -1, set, Point1D.of(6));
        assertProjection(Point1D.of(5), -2, set, Point1D.of(7));

        assertProjection(Point1D.of(9), -1, set, Point1D.of(8));
        assertProjection(Point1D.of(9), 0, set, Point1D.of(9));
        assertProjection(Point1D.of(9), 0.1, set, Point1D.of(9.1));

        assertProjection(Point1D.of(10), 0, set, Point1D.of(10));
        assertProjection(Point1D.of(10), -1, set, Point1D.of(11));
    }

    @Test
    public void testInterval() {
        IntervalsSet set = new IntervalsSet(2.3, 5.7, 1.0e-10);
        Assert.assertEquals(3.4, set.getSize(), 1.0e-10);
        Assert.assertEquals(4.0, set.getBarycenter().getX(), 1.0e-10);
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(Point1D.of(2.3)));
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(Point1D.of(5.7)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(Point1D.of(1.2)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(Point1D.of(8.7)));
        Assert.assertEquals(Region.Location.INSIDE,   set.checkPoint(Point1D.of(3.0)));
        Assert.assertEquals(2.3, set.getInf(), 1.0e-10);
        Assert.assertEquals(5.7, set.getSup(), 1.0e-10);
    }

    @Test
    public void testInfinite() {
        IntervalsSet set = new IntervalsSet(9.0, Double.POSITIVE_INFINITY, 1.0e-10);
        Assert.assertEquals(Region.Location.BOUNDARY, set.checkPoint(Point1D.of(9.0)));
        Assert.assertEquals(Region.Location.OUTSIDE,  set.checkPoint(Point1D.of(8.4)));
        for (double e = 1.0; e <= 6.0; e += 1.0) {
            Assert.assertEquals(Region.Location.INSIDE,
                                set.checkPoint(Point1D.of(Math.pow(10.0, e))));
        }
        Assert.assertTrue(Double.isInfinite(set.getSize()));
        Assert.assertEquals(9.0, set.getInf(), 1.0e-10);
        Assert.assertTrue(Double.isInfinite(set.getSup()));

        set = (IntervalsSet) new RegionFactory<Point1D>().getComplement(set);
        Assert.assertEquals(9.0, set.getSup(), 1.0e-10);
        Assert.assertTrue(Double.isInfinite(set.getInf()));

    }

    @Test
    public void testBooleanOperations() {
        // arrange
        RegionFactory<Point1D> factory = new RegionFactory<>();

        // act
        IntervalsSet set = (IntervalsSet)
        factory.intersection(factory.union(factory.difference(new IntervalsSet(1.0, 6.0, TEST_TOLERANCE),
                                                              new IntervalsSet(3.0, 5.0, TEST_TOLERANCE)),
                                                              new IntervalsSet(9.0, Double.POSITIVE_INFINITY, TEST_TOLERANCE)),
                                                              new IntervalsSet(Double.NEGATIVE_INFINITY, 11.0, TEST_TOLERANCE));

        // arrange
        Assert.assertEquals(1.0, set.getInf(), TEST_TOLERANCE);
        Assert.assertEquals(11.0, set.getSup(), TEST_TOLERANCE);

        Assert.assertEquals(5.0, set.getSize(), TEST_TOLERANCE);
        Assert.assertEquals(5.9, set.getBarycenter().getX(), TEST_TOLERANCE);

        assertLocation(Region.Location.OUTSIDE, set, 0.0);
        assertLocation(Region.Location.OUTSIDE, set, 4.0);
        assertLocation(Region.Location.OUTSIDE, set, 8.0);
        assertLocation(Region.Location.OUTSIDE, set, 12.0);
        assertLocation(Region.Location.INSIDE, set, 1.2);
        assertLocation(Region.Location.INSIDE, set, 5.9);
        assertLocation(Region.Location.INSIDE, set, 9.01);
        assertLocation(Region.Location.BOUNDARY, set, 5.0);
        assertLocation(Region.Location.BOUNDARY, set, 11.0);

        List<Interval> list = set.asList();
        Assert.assertEquals(3, list.size());
        assertInterval(1.0, 3.0, list.get(0), TEST_TOLERANCE);
        assertInterval(5.0, 6.0, list.get(1), TEST_TOLERANCE);
        assertInterval(9.0, 11.0, list.get(2), TEST_TOLERANCE);
    }

    private void assertLocation(Region.Location location, IntervalsSet set, double pt) {
        Assert.assertEquals(location, set.checkPoint(Point1D.of(pt)));
    }

    private void assertInterval(double expectedInf, double expectedSup, Interval actual, double tolerance) {
        Assert.assertEquals(expectedInf, actual.getInf(), tolerance);
        Assert.assertEquals(expectedSup, actual.getSup(), tolerance);
    }

    private void assertProjection(Point1D expectedProjection, double expectedOffset,
            IntervalsSet set, Point1D toProject) {
        BoundaryProjection<Point1D> proj = set.projectToBoundary(toProject);

        EuclideanTestUtils.assertCoordinatesEqual(toProject, proj.getOriginal(), TEST_TOLERANCE);
        EuclideanTestUtils.assertCoordinatesEqual(expectedProjection, proj.getProjected(), TEST_TOLERANCE);
        Assert.assertEquals(expectedOffset, proj.getOffset(), TEST_TOLERANCE);
    }

    private SubOrientedPoint subOrientedPoint(double location, boolean direct) {
        return subOrientedPoint(location, direct, TEST_TOLERANCE);
    }

    private SubOrientedPoint subOrientedPoint(double location, boolean direct, double tolerance) {
        // the remaining region isn't necessary for creating 1D boundaries so we can set it to null here
        return new SubOrientedPoint(new OrientedPoint(Point1D.of(location), direct, tolerance), null);
    }
}
