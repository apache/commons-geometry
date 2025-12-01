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
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.geometry.core.collection.PointMapTestBase;
import org.apache.commons.geometry.core.partitioning.test.TestPoint1D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AbstractPointMap1DTest extends PointMapTestBase<TestPoint1D> {

    @Test
    void testGetPrecision() {
        // arrange
        final TestPointMap1D<Integer> map = getMap(PRECISION);

        // act/assert
        Assertions.assertSame(PRECISION, map.getPrecision());
    }

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

    /** {@inheritDoc} */
    @Override
    protected int disambiguateNearToFarOrder(final TestPoint1D a, final TestPoint1D b) {
        return Double.compare(a.getX(), b.getX());
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
            super(precision, TestPoint1D::getX);
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
            final Entry<TestPoint1D, V> floor = map.floorEntry(key);
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

        /** {@inheritDoc} */
        @Override
        protected Iterator<Entry<TestPoint1D, V>> nearToFarIterator(final TestPoint1D pt) {
            return new NearToFarIterator(pt);
        }

        /** {@inheritDoc} */
        @Override
        protected Iterator<Entry<TestPoint1D, V>> farToNearIterator(final TestPoint1D pt) {
            return new FarToNearIterator(pt);
        }

        private final class NearToFarIterator
            implements Iterator<Entry<TestPoint1D, V>> {

            private final TestPoint1D refPt;

            private final Iterator<Entry<TestPoint1D, V>> low;

            private final Iterator<Entry<TestPoint1D, V>> high;

            private DistancedValue<Entry<TestPoint1D, V>> lowEntry;

            private DistancedValue<Entry<TestPoint1D, V>> highEntry;

            NearToFarIterator(final TestPoint1D refPt) {
                this.refPt = refPt;

                this.low = getMap().descendingMap().tailMap(refPt, false)
                        .entrySet().iterator();
                this.high = getMap().tailMap(refPt).entrySet().iterator();
            }

            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                if (lowEntry == null) {
                    lowEntry = getNextEntry(low);
                }
                if (highEntry == null) {
                    highEntry = getNextEntry(high);
                }

                return lowEntry != null || highEntry != null;
            }

            /** {@inheritDoc} */
            @Override
            public Entry<TestPoint1D, V> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                final DistancedValue<Entry<TestPoint1D, V>> result;
                if (lowEntry != null &&
                        (highEntry == null || lowEntry.getDistance() <= highEntry.getDistance())) {
                    result = lowEntry;
                    lowEntry = null;
                } else {
                    result = highEntry;
                    highEntry = null;
                }

                return result != null ?
                        result.getValue() :
                        null;
            }

            private DistancedValue<Entry<TestPoint1D, V>> getNextEntry(final Iterator<Entry<TestPoint1D, V>> it) {
                if (it.hasNext()) {
                    final Entry<TestPoint1D, V> entry = it.next();
                    return DistancedValue.of(entry, refPt.distance(entry.getKey()));
                }
                return null;
            }
        }

        private final class FarToNearIterator
            implements Iterator<Entry<TestPoint1D, V>> {

            private final TestPoint1D refPt;

            private Iterator<Entry<TestPoint1D, V>> low;

            private Iterator<Entry<TestPoint1D, V>> high;

            private DistancedValue<Entry<TestPoint1D, V>> lowEntry;

            private DistancedValue<Entry<TestPoint1D, V>> highEntry;

            private double lastLowValue = Double.NEGATIVE_INFINITY;

            private double lastHighValue = Double.POSITIVE_INFINITY;

            FarToNearIterator(final TestPoint1D refPt) {
                this.refPt = refPt;

                this.low = getMap().entrySet().iterator();
                this.high = getMap().descendingMap().entrySet().iterator();
            }

            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                if (lowEntry == null && low != null && low.hasNext()) {
                    final Entry<TestPoint1D, V> entry = low.next();
                    lastLowValue = entry.getKey().getX();

                    if (entry.getKey().getX() >= lastHighValue) {
                        // we've crossed over the value returned by the high iterator
                        low = null;
                    } else {
                        lowEntry = DistancedValue.of(entry, refPt.distance(entry.getKey()));
                    }
                }
                if (highEntry == null && high != null && high.hasNext()) {
                    final Entry<TestPoint1D, V> entry = high.next();
                    lastHighValue = entry.getKey().getX();

                    if (entry.getKey().getX() <= lastLowValue) {
                        // we've crossed over the values returned by the low iterator
                        high = null;
                    } else {
                        highEntry = DistancedValue.of(entry, refPt.distance(entry.getKey()));
                    }
                }

                return lowEntry != null || highEntry != null;
            }

            /** {@inheritDoc} */
            @Override
            public Entry<TestPoint1D, V> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                final DistancedValue<Entry<TestPoint1D, V>> result;
                if (lowEntry != null &&
                        (highEntry == null || lowEntry.getDistance() >= highEntry.getDistance())) {
                    result = lowEntry;
                    lowEntry = null;
                } else {
                    result = highEntry;
                    highEntry = null;
                }

                return result != null ?
                        result.getValue() :
                        null;
            }
        }
    }
}
