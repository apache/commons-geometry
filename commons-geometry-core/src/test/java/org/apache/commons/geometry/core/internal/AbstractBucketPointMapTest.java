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
package org.apache.commons.geometry.core.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.geometry.core.collection.PointMapTestBase;
import org.apache.commons.geometry.core.internal.AbstractBucketPointMap.BucketNode;
import org.apache.commons.geometry.core.partitioning.test.TestPoint1D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AbstractBucketPointMapTest extends PointMapTestBase<TestPoint1D> {

    @Test
    void testPut_pointsCloseToSplit() {
        // arrange
        TestPointMap<Integer> map = getMap(PRECISION);

        final List<TestPoint1D> pts = createPointList(0, 1, TestPointMap.MAX_ENTRY_COUNT);
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

    /** {@inheritDoc} */
    @Override
    protected <V> TestPointMap<V> getMap(final Precision.DoubleEquivalence precision) {
        return new TestPointMap<>(precision);
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

    private static final class TestPointMap<V> extends AbstractBucketPointMap<TestPoint1D, V> {

        static final int MAX_ENTRY_COUNT = 16;

        static final int NODE_CHILD_COUNT = 2;

        TestPointMap(final Precision.DoubleEquivalence precision) {
            super(TestNode::new,
                    MAX_ENTRY_COUNT,
                    NODE_CHILD_COUNT,
                    precision);
        }

        /** {@inheritDoc} */
        @Override
        protected boolean pointsEq(final TestPoint1D a, final TestPoint1D b) {
            return getPrecision().eq(a.getX(), b.getX());
        }
    }

    private static final class TestNode<V> extends AbstractBucketPointMap.BucketNode<TestPoint1D, V> {

        /** Negative half-space flag. */
        private static final int NEG = 1 << 1;

        /** Positve half-space flag. */
        private static final int POS = 1;

        /** Location flags for child nodes. */
        private static final int[] CHILD_LOCATIONS = {
            NEG,
            POS
        };

        private double split;

        TestNode(
                final AbstractBucketPointMap<TestPoint1D, V> map,
                final BucketNode<TestPoint1D, V> parent) {
            super(map, parent);
        }

        /** {@inheritDoc} */
        @Override
        protected void computeSplit() {
            double sum = 0;
            for (Map.Entry<TestPoint1D, V> entry : this) {
                sum += entry.getKey().getX();
            }

            split = sum / TestPointMap.MAX_ENTRY_COUNT;
        }

        /** {@inheritDoc} */
        @Override
        protected int getSearchLocation(final TestPoint1D pt) {
            return getSearchLocationValue(getPrecision().compare(pt.getX(), split), NEG, POS);
        }

        /** {@inheritDoc} */
        @Override
        protected int getInsertLocation(final TestPoint1D pt) {
            return getInsertLocationValue(Double.compare(pt.getX(), split), NEG, POS);
        }

        /** {@inheritDoc} */
        @Override
        protected boolean testChildLocation(final int childIdx, final int loc) {
            final int childLoc = CHILD_LOCATIONS[childIdx];
            return (childLoc & loc) == childLoc;
        }
    }
}
