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

import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.core.collection.PointMapTestBase;
import org.apache.commons.geometry.core.internal.AbstractBucketPointMap.BucketNode;
import org.apache.commons.geometry.core.partitioning.test.TestPoint1D;
import org.apache.commons.numbers.core.Precision;

class AbstractBucketPointMapTest extends PointMapTestBase<TestPoint1D> {

    /** {@inheritDoc} */
    @Override
    protected <V> PointMap<TestPoint1D, V> getMap(final Precision.DoubleEquivalence precision) {
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
        final List<TestPoint1D> pts = new ArrayList<>(cnt);

        final double delta = 10 * eps;

        double x = -1.0;
        for (int i = 0; i < cnt; ++i) {
            pts.add(new TestPoint1D(x));

            x += delta;
        }

        return pts;
    }

    /** {@inheritDoc} */
    @Override
    protected List<TestPoint1D> getTestPointsAtDistance(final TestPoint1D pt, final double dist) {
        return Arrays.asList(
                new TestPoint1D(pt.getX() - dist),
                new TestPoint1D(pt.getX() + dist));
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
        protected int getLocation(final TestPoint1D pt) {
            return Double.compare(pt.getX(), split);
        }

        /** {@inheritDoc} */
        @Override
        protected boolean testChildLocation(final int childIdx, final int loc) {
            final int expectedIdx = loc <= 0 ? 0 : 1;
            return childIdx == expectedIdx;
        }
    }
}
