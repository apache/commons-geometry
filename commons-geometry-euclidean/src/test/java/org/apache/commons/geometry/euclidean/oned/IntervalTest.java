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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class IntervalTest {

    private static final double TEST_EPS = 1e-15;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testOf_doubles() {
        // act/assert
        checkInterval(Interval.of(0, 0, TEST_PRECISION), 0, 0);

        checkInterval(Interval.of(1, 2, TEST_PRECISION), 1, 2);
        checkInterval(Interval.of(2, 1, TEST_PRECISION), 1, 2);

        checkInterval(Interval.of(-2, -1, TEST_PRECISION), -2, -1);
        checkInterval(Interval.of(-1, -2, TEST_PRECISION), -2, -1);

        checkInterval(Interval.of(1, Double.POSITIVE_INFINITY, TEST_PRECISION),
                1, Double.POSITIVE_INFINITY);
        checkInterval(Interval.of(Double.POSITIVE_INFINITY, 1, TEST_PRECISION),
                1, Double.POSITIVE_INFINITY);

        checkInterval(Interval.of(Double.NEGATIVE_INFINITY, 1, TEST_PRECISION),
                Double.NEGATIVE_INFINITY, 1);
        checkInterval(Interval.of(1, Double.NEGATIVE_INFINITY, TEST_PRECISION),
                Double.NEGATIVE_INFINITY, 1);

        checkInterval(Interval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION),
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        checkInterval(Interval.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, TEST_PRECISION),
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testOf_doubles_invalidIntervals() {
        // arrange
        Class<?> excType = IllegalArgumentException.class;

        // act/assert
        GeometryTestUtils.assertThrows(() -> Interval.of(1, Double.NaN, TEST_PRECISION), excType);
        GeometryTestUtils.assertThrows(() -> Interval.of(Double.NaN, 1, TEST_PRECISION), excType);
        GeometryTestUtils.assertThrows(() -> Interval.of(Double.NaN, Double.NaN, TEST_PRECISION), excType);

        GeometryTestUtils.assertThrows(
                () -> Interval.of(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION), excType);
        GeometryTestUtils.assertThrows(
                () -> Interval.of(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, TEST_PRECISION), excType);
    }

    @Test
    public void testOf_points() {
        // act/assert
        checkInterval(Interval.of(Vector1D.of(1), Vector1D.of(2), TEST_PRECISION), 1, 2);
        checkInterval(Interval.of(Vector1D.of(Double.POSITIVE_INFINITY), Vector1D.of(Double.NEGATIVE_INFINITY), TEST_PRECISION),
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testOf_points_invalidIntervals() {
        // arrange
        Class<?> excType = IllegalArgumentException.class;

        // act/assert
        GeometryTestUtils.assertThrows(
                () -> Interval.of(Vector1D.of(1), Vector1D.of(Double.NaN), TEST_PRECISION), excType);
        GeometryTestUtils.assertThrows(
                () -> Interval.of(Vector1D.of(Double.POSITIVE_INFINITY), Vector1D.of(Double.POSITIVE_INFINITY), TEST_PRECISION), excType);
    }

    @Test
    public void testOf_hyperplanes() {
        // act/assert
        checkInterval(Interval.of(
                OrientedPoint.fromLocationAndDirection(1, true, TEST_PRECISION),
                OrientedPoint.fromLocationAndDirection(1, false, TEST_PRECISION)), 1, 1);
        checkInterval(Interval.of(
                OrientedPoint.fromLocationAndDirection(1, false, TEST_PRECISION),
                OrientedPoint.fromLocationAndDirection(1, true, TEST_PRECISION)), 1, 1);

        checkInterval(Interval.of(
                OrientedPoint.fromLocationAndDirection(-2, false, TEST_PRECISION),
                OrientedPoint.fromLocationAndDirection(5, true, TEST_PRECISION)), -2, 5);
        checkInterval(Interval.of(
                OrientedPoint.fromLocationAndDirection(5, true, TEST_PRECISION),
                OrientedPoint.fromLocationAndDirection(-2, false, TEST_PRECISION)), -2, 5);

        checkInterval(Interval.of(
                null,
                OrientedPoint.fromLocationAndDirection(5, true, TEST_PRECISION)), Double.NEGATIVE_INFINITY, 5);
        checkInterval(Interval.of(
                OrientedPoint.fromLocationAndDirection(5, true, TEST_PRECISION),
                null), Double.NEGATIVE_INFINITY, 5);
        checkInterval(Interval.of(
                OrientedPoint.fromLocationAndDirection(Double.NEGATIVE_INFINITY, false, TEST_PRECISION),
                OrientedPoint.fromLocationAndDirection(5, true, TEST_PRECISION)), Double.NEGATIVE_INFINITY, 5);

        checkInterval(Interval.of(
                null,
                OrientedPoint.fromLocationAndDirection(5, false, TEST_PRECISION)), 5, Double.POSITIVE_INFINITY);
        checkInterval(Interval.of(
                OrientedPoint.fromLocationAndDirection(5, false, TEST_PRECISION),
                null), 5, Double.POSITIVE_INFINITY);
        checkInterval(Interval.of(
                OrientedPoint.fromLocationAndDirection(Double.POSITIVE_INFINITY, true, TEST_PRECISION),
                OrientedPoint.fromLocationAndDirection(5, false, TEST_PRECISION)), 5, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testOf_hyperplanes_invalidArgs() {
        // arrange
        Class<?> excType = IllegalArgumentException.class;

        // act/assert
        GeometryTestUtils.assertThrows(
                () -> Interval.of(
                        OrientedPoint.fromLocationAndDirection(1, false, TEST_PRECISION),
                        OrientedPoint.fromLocationAndDirection(1, false, TEST_PRECISION)), excType);

        GeometryTestUtils.assertThrows(
                () -> Interval.of(
                        OrientedPoint.fromLocationAndDirection(2, false, TEST_PRECISION),
                        OrientedPoint.fromLocationAndDirection(1, true, TEST_PRECISION)), excType);

        GeometryTestUtils.assertThrows(
                () -> Interval.of(
                        OrientedPoint.fromLocationAndDirection(Double.POSITIVE_INFINITY, false, TEST_PRECISION),
                        OrientedPoint.fromLocationAndDirection(Double.POSITIVE_INFINITY, true, TEST_PRECISION)), excType);

        GeometryTestUtils.assertThrows(
                () -> Interval.of(
                        OrientedPoint.fromLocationAndDirection(Double.NaN, false, TEST_PRECISION),
                        OrientedPoint.fromLocationAndDirection(1, true, TEST_PRECISION)), excType);

        GeometryTestUtils.assertThrows(
                () -> Interval.of(
                        OrientedPoint.fromLocationAndDirection(1, false, TEST_PRECISION),
                        OrientedPoint.fromLocationAndDirection(Double.NaN, true, TEST_PRECISION)), excType);

        GeometryTestUtils.assertThrows(
                () -> Interval.of(
                        OrientedPoint.fromLocationAndDirection(Double.NaN, false, TEST_PRECISION),
                        OrientedPoint.fromLocationAndDirection(Double.NaN, true, TEST_PRECISION)), excType);
    }

    @Test
    public void testIsInfinite() {
        // act/assert
        Assert.assertFalse(Interval.of(1, 2, TEST_PRECISION).isInfinite());

        Assert.assertTrue(Interval.of(Double.NEGATIVE_INFINITY, 2, TEST_PRECISION).isInfinite());
        Assert.assertTrue(Interval.of(2, Double.POSITIVE_INFINITY, TEST_PRECISION).isInfinite());
        Assert.assertTrue(Interval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION).isInfinite());
    }

    @Test
    public void testClassify_finite() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(-1, 1, precision);

        // act/assert
        checkClassify(interval, RegionLocation.OUTSIDE,
                Double.NEGATIVE_INFINITY, -2, -1.1,
                1.1, 2, Double.POSITIVE_INFINITY);

        checkClassify(interval, RegionLocation.BOUNDARY,
                -1.001, -1, -0.999,
                0.999, 1, 1.001);

        checkClassify(interval, RegionLocation.INSIDE, -0.9, 0, 0.9);

        checkClassify(interval, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void testClassify_singlePoint() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(1, 1, precision);

        // act/assert
        checkClassify(interval, RegionLocation.OUTSIDE,
                Double.NEGATIVE_INFINITY, 0, 0.9, 1.1, 2, Double.POSITIVE_INFINITY);

        checkClassify(interval, RegionLocation.BOUNDARY,
                0.999, 1, 1.0001);

        checkClassify(interval, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void testClassify_maxInfinite() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(-1, Double.POSITIVE_INFINITY, precision);

        // act/assert
        checkClassify(interval, RegionLocation.OUTSIDE,
                Double.NEGATIVE_INFINITY, -2, -1.1);

        checkClassify(interval, RegionLocation.BOUNDARY,
                -1.001, -1, -0.999);

        checkClassify(interval, RegionLocation.INSIDE,
                -0.9, 0, 1.0, Double.POSITIVE_INFINITY);

        checkClassify(interval, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void testClassify_minInfinite() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(Double.NEGATIVE_INFINITY, 1, precision);

        // act/assert
        checkClassify(interval, RegionLocation.INSIDE,
                Double.NEGATIVE_INFINITY, 0, 0.9);

        checkClassify(interval, RegionLocation.BOUNDARY,
                0.999, 1, 1.001);

        checkClassify(interval, RegionLocation.OUTSIDE,
                1.1, 2, Double.POSITIVE_INFINITY);

        checkClassify(interval, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void testClassify_minMaxInfinite() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, precision);

        // act/assert
        checkClassify(interval, RegionLocation.INSIDE,
                Double.NEGATIVE_INFINITY, -1, 0, 1, Double.POSITIVE_INFINITY);

        checkClassify(interval, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void testContains_finite() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(-1, 1, precision);

        // act/assert
        checkContains(interval, true,
                -1.001, -1, -0.999,
                0.999, 1, 1.001,

                -0.9, 0, 0.9);

        checkContains(interval, false,
                Double.NEGATIVE_INFINITY, -2, -1.1,
                1.1, 2, Double.POSITIVE_INFINITY);

        checkContains(interval, false, Double.NaN);
    }

    @Test
    public void testIsFull() {
        // act/assert
        Assert.assertFalse(Interval.of(1, 1, TEST_PRECISION).isFull());
        Assert.assertFalse(Interval.of(-2, 2, TEST_PRECISION).isFull());

        Assert.assertFalse(Interval.of(1, Double.POSITIVE_INFINITY, TEST_PRECISION).isFull());
        Assert.assertFalse(Interval.of(Double.NEGATIVE_INFINITY, 1, TEST_PRECISION).isFull());

        Assert.assertTrue(Interval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION).isFull());
    }

    @Test
    public void testGetSize() {
        // act/assert
        Assert.assertEquals(0, Interval.of(1, 1, TEST_PRECISION).getSize(), TEST_EPS);

        Assert.assertEquals(4, Interval.of(-2, 2, TEST_PRECISION).getSize(), TEST_EPS);
        Assert.assertEquals(5, Interval.of(2, -3, TEST_PRECISION).getSize(), TEST_EPS);

        Assert.assertEquals(Double.POSITIVE_INFINITY,
                Interval.of(1, Double.POSITIVE_INFINITY, TEST_PRECISION).getSize(), TEST_EPS);
        Assert.assertEquals(Double.POSITIVE_INFINITY,
                Interval.of(Double.NEGATIVE_INFINITY, 1, TEST_PRECISION).getSize(), TEST_EPS);

        Assert.assertEquals(Double.POSITIVE_INFINITY,
                Interval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION).getSize(), TEST_EPS);
    }

    @Test
    public void testGetBoundarySize() {
        // act/assert
        Assert.assertEquals(0, Interval.of(1, 1, TEST_PRECISION).getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0, Interval.of(-2, 5, TEST_PRECISION).getBoundarySize(), TEST_EPS);
        Assert.assertEquals(0, Interval.reals().getBoundarySize(), TEST_EPS);
    }

    @Test
    public void testGetBarycenter() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.ZERO,
                Interval.of(-1, 1, TEST_PRECISION).getBarycenter(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(10),
                Interval.of(10, 10, TEST_PRECISION).getBarycenter(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(2),
                Interval.of(1, 3, TEST_PRECISION).getBarycenter(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(-1),
                Interval.of(-2, 0, TEST_PRECISION).getBarycenter(), TEST_EPS);

        Assert.assertNull(Interval.of(1, Double.POSITIVE_INFINITY, TEST_PRECISION).getBarycenter());
        Assert.assertNull(Interval.of(Double.NEGATIVE_INFINITY, 1, TEST_PRECISION).getBarycenter());
        Assert.assertNull(Interval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION).getBarycenter());
    }

    @Test
    public void checkToTree_finite() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(-1, 1, precision);

        // act
        RegionBSPTree1D tree = interval.toTree();

        // assert
        Assert.assertEquals(5, tree.count());

        checkClassify(tree, RegionLocation.OUTSIDE,
                Double.NEGATIVE_INFINITY, -2, -1.1,
                1.1, 2, Double.POSITIVE_INFINITY);

        checkClassify(tree, RegionLocation.BOUNDARY,
                -1.001, -1, -0.999,
                0.999, 1, 1.001);

        checkClassify(tree, RegionLocation.INSIDE, -0.9, 0, 0.9);

        checkClassify(tree, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void checkToTree_singlePoint() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(1, 1, precision);

        // act
        RegionBSPTree1D tree = interval.toTree();

        // assert
        Assert.assertEquals(5, tree.count());

        checkClassify(tree, RegionLocation.OUTSIDE,
                Double.NEGATIVE_INFINITY, 0, 0.9, 1.1, 2, Double.POSITIVE_INFINITY);

        checkClassify(tree, RegionLocation.BOUNDARY,
                0.999, 1, 1.0001);

        checkClassify(tree, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void checkToTree_maxInfinite() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(-1, Double.POSITIVE_INFINITY, precision);

        // act
        RegionBSPTree1D tree = interval.toTree();

        // assert
        Assert.assertEquals(3, tree.count());

        checkClassify(tree, RegionLocation.OUTSIDE,
                Double.NEGATIVE_INFINITY, -2, -1.1);

        checkClassify(tree, RegionLocation.BOUNDARY,
                -1.001, -1, -0.999);

        checkClassify(tree, RegionLocation.INSIDE,
                -0.9, 0, 1.0, Double.POSITIVE_INFINITY);

        checkClassify(interval, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void checkToTree_minInfinite() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(Double.NEGATIVE_INFINITY, 1, precision);

        // act
        RegionBSPTree1D tree = interval.toTree();

        // assert
        Assert.assertEquals(3, tree.count());

        checkClassify(tree, RegionLocation.INSIDE,
                Double.NEGATIVE_INFINITY, 0, 0.9);

        checkClassify(tree, RegionLocation.BOUNDARY,
                0.999, 1, 1.001);

        checkClassify(tree, RegionLocation.OUTSIDE,
                1.1, 2, Double.POSITIVE_INFINITY);

        checkClassify(tree, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void checkToTree_minMaxInfinite() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);
        Interval interval = Interval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, precision);

        // act
        RegionBSPTree1D tree = interval.toTree();

        // assert
        Assert.assertEquals(1, tree.count());

        checkClassify(tree, RegionLocation.INSIDE,
                Double.NEGATIVE_INFINITY, -1, 0, 1, Double.POSITIVE_INFINITY);

        checkClassify(tree, RegionLocation.OUTSIDE, Double.NaN);
    }

    @Test
    public void testTransform() {
        // arrange
        Transform<Vector1D> transform = (p) -> Vector1D.of(2.0 * p.getX());

        // act/assert
        checkInterval(Interval.of(-1, 2, TEST_PRECISION).transform(transform), -2, 4);

        checkInterval(Interval.of(Double.NEGATIVE_INFINITY, 2, TEST_PRECISION).transform(transform),
                Double.NEGATIVE_INFINITY, 4);

        checkInterval(Interval.of(-1, Double.POSITIVE_INFINITY, TEST_PRECISION).transform(transform), -2,
                Double.POSITIVE_INFINITY);

        checkInterval(Interval.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, TEST_PRECISION).transform(transform),
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        Transform<Vector1D> transform = Vector1D::negate;

        // act/assert
        checkInterval(Interval.of(-1, 2, TEST_PRECISION).transform(transform), -2, 1);

        checkInterval(Interval.of(Double.NEGATIVE_INFINITY, 2, TEST_PRECISION).transform(transform),
                -2, Double.POSITIVE_INFINITY);

        checkInterval(Interval.of(-1, Double.POSITIVE_INFINITY, TEST_PRECISION).transform(transform),
                Double.NEGATIVE_INFINITY, 1);
    }

    @Test
    public void testHashCode() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        Interval a = Interval.of(1, 2, TEST_PRECISION);

        Interval b = Interval.of(1, 2, precision);
        Interval c = Interval.of(1, 3, TEST_PRECISION);
        Interval d = Interval.of(0, 2, TEST_PRECISION);

        Interval e = Interval.of(1, 2, TEST_PRECISION);

        // act
        int hash = a.hashCode();

        // assert
        Assert.assertEquals(hash, a.hashCode());

        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertNotEquals(hash, c.hashCode());
        Assert.assertNotEquals(hash, d.hashCode());

        Assert.assertEquals(hash, e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        Interval a = Interval.of(1, 2, TEST_PRECISION);

        Interval b = Interval.of(1, 2, precision);
        Interval c = Interval.of(1, 3, TEST_PRECISION);
        Interval d = Interval.of(0, 2, TEST_PRECISION);

        Interval e = Interval.of(1, 2, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(a.equals(d));

        Assert.assertTrue(a.equals(e));
        Assert.assertTrue(e.equals(a));
    }

    @Test
    public void testToString() {
        // arrange
        Interval interval = Interval.of(2, 1, TEST_PRECISION);

        // act
        String str = interval.toString();

        // assert
        Assert.assertTrue(str.contains("Interval"));
        Assert.assertTrue(str.contains("min= 1.0"));
        Assert.assertTrue(str.contains("max= 2.0"));
    }

    @Test
    public void testReals() {
        // act
        Interval reals = Interval.reals();

        // assert
        Assert.assertTrue(reals.isFull());
        Assert.assertFalse(reals.isEmpty());
        Assert.assertFalse(reals.hasMinBoundary());
        Assert.assertFalse(reals.hasMaxBoundary());
        Assert.assertTrue(reals.isInfinite());

        Assert.assertEquals(RegionLocation.INSIDE, reals.classify(Double.NEGATIVE_INFINITY));
        Assert.assertEquals(RegionLocation.INSIDE, reals.classify(Double.POSITIVE_INFINITY));
    }

    private static void checkClassify(Interval interval, RegionLocation loc, double ... points) {
        for (double x : points) {
            String msg = "Unexpected location for point " + x;

            Assert.assertEquals(msg, loc, interval.classify(x));
            Assert.assertEquals(msg, loc, interval.classify(Vector1D.of(x)));
        }
    }

    private static void checkContains(Interval interval, boolean contains, double ... points) {
        for (double x : points) {
            String msg = "Unexpected contains status for point " + x;

            Assert.assertEquals(msg, contains, interval.contains(x));
            Assert.assertEquals(msg, contains, interval.contains(Vector1D.of(x)));
        }
    }

    private static void checkClassify(RegionBSPTree1D tree, RegionLocation loc, double ... points) {
        for (double x : points) {
            String msg = "Unexpected location for point " + x;

            Assert.assertEquals(msg, loc, tree.classify(x));
            Assert.assertEquals(msg, loc, tree.classify(Vector1D.of(x)));
        }
    }

    /** Check that the given interval matches the arguments and is internally consistent.
     * @param interval
     * @param min
     * @param max
     */
    private static void checkInterval(Interval interval, double min, double max) {
        checkInterval(interval, min, max, TEST_PRECISION);
    }

    /** Check that the given interval matches the arguments and is internally consistent.
     * @param interval
     * @param min
     * @param max
     * @param precision
     */
    private static void checkInterval(Interval interval, double min, double max, DoublePrecisionContext precision) {
        Assert.assertEquals(min, interval.getMin(), TEST_EPS);
        Assert.assertEquals(max, interval.getMax(), TEST_EPS);

        boolean finiteMin = Double.isFinite(min);
        boolean finiteMax = Double.isFinite(max);

        Assert.assertEquals(finiteMin, interval.hasMinBoundary());
        Assert.assertEquals(finiteMax, interval.hasMaxBoundary());

        if (finiteMin) {
            Assert.assertEquals(min, interval.getMinBoundary().getLocation(), TEST_EPS);
        }
        else {
            Assert.assertNull(interval.getMinBoundary());
        }

        if (finiteMax) {
            Assert.assertEquals(max, interval.getMaxBoundary().getLocation(), TEST_EPS);
        }
        else {
            Assert.assertNull(interval.getMaxBoundary());
        }

        Assert.assertFalse(interval.isEmpty()); // always false
    }
}
