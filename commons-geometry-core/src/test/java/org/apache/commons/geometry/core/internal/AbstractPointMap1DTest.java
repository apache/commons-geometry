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
import java.util.NavigableMap;
import java.util.Set;

import org.apache.commons.geometry.core.collection.PointMapTestBase;
import org.apache.commons.geometry.core.partitioning.test.TestPoint1D;
import org.apache.commons.numbers.core.Precision;

class AbstractPointMap1DTest extends PointMapTestBase<TestPoint1D> {

    /** {@inheritDoc} */
    @Override
    protected <V> TestPointMap1D<V> getMap(final Precision.DoubleEquivalence precision) {
        return new TestPointMap1D<>(precision);
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

    private static final class TestPointMap1D<V> extends AbstractPointMap1D<TestPoint1D, V> {

        TestPointMap1D(final Precision.DoubleEquivalence precision) {
            super((a, b) -> precision.compare(a.getX(), b.getX()));
        }

        /** {@inheritDoc} */
        @Override
        public boolean containsKey(final Object key) {
            return getMap().containsKey(key);
        }

        /** {@inheritDoc} */
        @Override
        public V get(final Object key) {
            return getMap().get(key);
        }

        /** {@inheritDoc} */
        @Override
        public V remove(final Object key) {
            return getMap().remove(key);
        }

        /** {@inheritDoc} */
        @Override
        public void clear() {
            getMap().clear();
        }

        /** {@inheritDoc} */
        @Override
        public Set<TestPoint1D> keySet() {
            return getMap().keySet();
        }

        /** {@inheritDoc} */
        @Override
        public Set<Entry<TestPoint1D, V>> entrySet() {
            return getMap().entrySet();
        }

        /** {@inheritDoc} */
        @Override
        protected Entry<TestPoint1D, V> getEntryInternal(final TestPoint1D key) {
            final NavigableMap<TestPoint1D, V> map = getMap();
            final Map.Entry<TestPoint1D, V> floor = map.floorEntry(key);
            if (floor != null &&
                    map.comparator().compare(floor.getKey(), key) == 0) {
                return floor;
            }
            return null;
        }

        /** {@inheritDoc} */
        @Override
        protected V putInternal(final TestPoint1D key, final V value) {
            return getMap().put(key, value);
        }
    }
}
