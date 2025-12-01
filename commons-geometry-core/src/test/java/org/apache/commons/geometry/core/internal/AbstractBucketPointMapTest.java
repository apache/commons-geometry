/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.core.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.collection.PointMapTestBase;
import org.apache.commons.geometry.core.partitioning.test.TestPoint1D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AbstractBucketPointMapTest extends PointMapTestBase<TestPoint1D> {

    @Test
    void testPut_pointsCloseToSplit() {
        // arrange
        TestBucketPointMap1D<Integer> map = getMap(PRECISION);

        final List<TestPoint1D> pts = createPointList(0, 1, TestBucketPointMap1D.MAX_ENTRY_COUNT);
        insertPoints(pts, map);

        final TestPoint1D split = centroid(pts);

        final TestPoint1D pt = new TestPoint1D(split.getX() + (1.25 * EPS));

        map.put(pt, 100);

        // act/assert
        final TestPoint1D close = new TestPoint1D(split.getX() + (0.75 * EPS));

        Assertions.assertEquals(100, map.put(close, 101));
        Assertions.assertEquals(101, map.get(close));
        Assertions.assertEquals(101, map.get(pt));
    }

    @Test
    void testEntriesNearToFar_pointsAtEqualDistances() {
        // arrange
        TestBucketPointMap1D<Integer> map = getMap(PRECISION);

        final List<TestPoint1D> pts = Arrays.asList(
                new TestPoint1D(-2),
                new TestPoint1D(-1),
                new TestPoint1D(0),
                new TestPoint1D(1),
                new TestPoint1D(2));

        insertPoints(pts, map);

        final List<TestPoint1D> expected = Arrays.asList(
                new TestPoint1D(0),
                new TestPoint1D(-1),
                new TestPoint1D(1),
                new TestPoint1D(-2),
                new TestPoint1D(2));

        // act/assert
        assertIterableOrder(
                expected,
                map.entriesNearToFar(new TestPoint1D(0)));
    }

    @Test
    void testEntriesFarToNear_pointsAtEqualDistances() {
        // arrange
        TestBucketPointMap1D<Integer> map = getMap(PRECISION);

        final List<TestPoint1D> pts = Arrays.asList(
                new TestPoint1D(-2),
                new TestPoint1D(-1),
                new TestPoint1D(0),
                new TestPoint1D(1),
                new TestPoint1D(2));

        insertPoints(pts, map);

        final List<TestPoint1D> expected = Arrays.asList(
                new TestPoint1D(2),
                new TestPoint1D(-2),
                new TestPoint1D(1),
                new TestPoint1D(-1),
                new TestPoint1D(0));

        // act/assert
        assertIterableOrder(
                expected,
                map.entriesFarToNear(new TestPoint1D(0)));
    }

    /** {@inheritDoc} */
    @Override
    protected <V> TestBucketPointMap1D<V> getMap(final Precision.DoubleEquivalence precision) {
        return new TestBucketPointMap1D<>(precision);
    }

    /** {@inheritDoc} */
    @Override
    protected TestPoint1D[] createPointArray() {
        return new TestPoint1D[0];
    }

    /** {@inheritDoc} */
    @Override
    protected List<TestPoint1D> getNaNPoints() {
        return Arrays.asList(new TestPoint1D(Double.NaN));
    }

    /** {@inheritDoc} */
    @Override
    protected List<TestPoint1D> getInfPoints() {
        return Arrays.asList(
                new TestPoint1D(Double.NEGATIVE_INFINITY),
                new TestPoint1D(Double.POSITIVE_INFINITY));
    }

    /** {@inheritDoc} */
    @Override
    protected List<TestPoint1D> getTestPoints(final int cnt, final double eps) {
        final double delta = 10 * eps;
        return createPointList(-1.0, delta, cnt);
    }

    /** {@inheritDoc} */
    @Override
    protected List<TestPoint1D> getTestPointsAtDistance(final TestPoint1D pt, final double dist) {
        return Arrays.asList(
                new TestPoint1D(pt.getX() - dist),
                new TestPoint1D(pt.getX() + dist));
    }

    /** {@inheritDoc} */
    @Override
    protected boolean eq(final TestPoint1D a, final TestPoint1D b, final Precision.DoubleEquivalence precision) {
        return precision.eq(a.getX(), b.getX());
    }

    private static List<TestPoint1D> createPointList(final double start, final double delta, final int cnt) {
        final List<TestPoint1D> pts = new ArrayList<>(cnt);

        double x = start;
        for (int i = 0; i < cnt; ++i) {
            pts.add(new TestPoint1D(x));

            x += delta;
        }

        return pts;
    }

    private static TestPoint1D centroid(final List<TestPoint1D> pts) {
        double sum = 0;
        for (final TestPoint1D pt : pts) {
            sum += pt.getX();
        }

        return new TestPoint1D(sum / pts.size());
    }

    /** {@inheritDoc} */
    @Override
    protected int disambiguateNearToFarOrder(final TestPoint1D a, final TestPoint1D b) {
        return Double.compare(a.getX(), b.getX());
    }
}
