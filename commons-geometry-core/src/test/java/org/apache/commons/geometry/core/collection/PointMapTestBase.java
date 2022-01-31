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
package org.apache.commons.geometry.core.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Base test class for {@link PointMap} implementations.
 * @param <P> Point type
 */
public abstract class PointMapTestBase<P extends Point<P>> {

    private static final double EPS = 1e-10;

    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);

    /** Get a new point map instance for testing.
     * @param <V> Value type
     * @param precision precision context to determine floating point equality
     * @return a new map instance for testing.
     */
    protected abstract <V> PointMap<P, V> getMap(Precision.DoubleEquivalence precision);

    /** Get {@code cnt} number of unique test points that differ from each other in
     * each dimension by <em>at least</em> {@code eps}.
     * @param cnt number of points to return
     * @param eps minimum value that each point must differ from other points along
     *      each dimension
     * @return list of test points
     */
    protected abstract List<P> getTestPoints(int cnt, double eps);

    /** Return true if the given points are equivalent as evaluated by the
     * precision instance.
     * @param a first point
     * @param b second point
     * @param precision precision context used to evaluate floating point numbers
     * @return true if the given points are equivalent as evaluated by the
     *      precision instance
     */
    protected abstract boolean eq(P a, P b, Precision.DoubleEquivalence precision);

    @Test
    void testEmpty() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);

        // act/assert
        Assertions.assertEquals(0, map.size());
        Assertions.assertTrue(map.isEmpty());

        Assertions.assertNull(map.get(pt));
        Assertions.assertFalse(map.containsKey(pt));
    }

    @Test
    void testSingleEntry() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);
        final P a = pts.get(0);

        // act
        map.put(a, 1);

        // assert
        checkerFor(map)
            .expectEntry(a, 1)
            .doesNotContainKeys(pts.subList(1, pts.size()))
            .check();
    }

    @Test
    void testMultipleEntries() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final PointMapChecker<P, Integer> checker = checkerFor(map);

        final int putCnt = 1000;
        final List<P> pts = getTestPoints(putCnt * 2, EPS);

        // act
        for (int i = 0; i < putCnt; ++i) {
            final P key = pts.get(i);

            map.put(key, i);

            checker.expectEntry(key, i);
        }

        // assert
        checker.doesNotContainKeys(pts.subList(putCnt, pts.size()))
            .check();
    }

    /** Return a new {@link PointMapChecker} for asserting the contents
     * of the given map.
     * @return a new checker instance
     */
    public <V> PointMapChecker<P, V> checkerFor(final PointMap<P, V> map) {
        return new PointMapChecker<>(map);
    }

    /** Class designed to assist with performing assertions on the state
     * of a point map.
     */
    public static class PointMapChecker<P extends Point<P>, V> {

        private final PointMap<P, V> map;

        private final Map<P, V> expectedMap = new HashMap<>();

        private final List<P> unexpectedKeys = new ArrayList<>();

        public PointMapChecker(final PointMap<P, V> map) {
            this.map = map;
        }

        public PointMapChecker<P, V> expectEntry(final P key, final V value) {
            expectedMap.put(key, value);

            return this;
        }

        public PointMapChecker<P, V> doesNotContainKey(final P key) {
            unexpectedKeys.add(key);

            return this;
        }

        public PointMapChecker<P, V> doesNotContainKeys(final Iterable<? extends P> keys) {
            for (final P key : keys) {
                doesNotContainKey(key);
            }

            return this;
        }

        public void check() {
            checkSize();

            checkEntries();

            checkEntrySet();
            checkKeySet();
            checkValues();

            checkUnexpectedKeys();
        }

        private void checkSize() {
            Assertions.assertEquals(expectedMap.size(), map.size(), "Unexpected map size");
            Assertions.assertEquals(expectedMap.isEmpty(), map.isEmpty(), "Unexpected isEmpty() result");
        }

        private void checkEntries() {
            for (final Map.Entry<P, V> expectedEntry : expectedMap.entrySet()) {
                final P expectedKey = expectedEntry.getKey();
                final V expectedValue = expectedEntry.getValue();

                Assertions.assertEquals(expectedKey, map.resolveKey(expectedKey),
                        () -> "Failed to resolve key " + expectedKey);
                Assertions.assertEquals(expectedValue, map.get(expectedKey),
                        () -> "Unexpected value for key " + expectedKey);

                Assertions.assertTrue(map.containsKey(expectedKey),
                        () -> "Expected map to contain key " + expectedKey);
                Assertions.assertTrue(map.containsValue(expectedValue),
                        () -> "Expected map to contain value " + expectedValue);
            }
        }

        private void checkKeySet() {
            Set<P> expectedKeySet = expectedMap.keySet();

            Set<P> keySet = map.keySet();
            Assertions.assertEquals(expectedKeySet.size(), keySet.size(), "Unexpected key set size");

            for (final P key : keySet) {
                Assertions.assertTrue(expectedKeySet.contains(key),
                        () -> "Unexpected key in key set: " + key);
            }

            for (final P expectedKey : expectedKeySet) {
                Assertions.assertTrue(keySet.contains(expectedKey),
                        () -> "Key set is missing expected key: " + expectedKey);
            }
        }

        private void checkEntrySet() {
            final Set<Map.Entry<P, V>> entrySet = map.entrySet();
            Assertions.assertEquals(expectedMap.size(), entrySet.size(), "Unexpected entry set size");

            final Map<P, V> remainingEntryMap = new HashMap<>(expectedMap);
            for (final Map.Entry<P, V> actualEntry : entrySet) {
                Assertions.assertTrue(remainingEntryMap.containsKey(actualEntry.getKey()),
                        "Unexpected key in entry set: " + actualEntry.getKey());

                final V expectedValue = remainingEntryMap.remove(actualEntry.getKey());
                Assertions.assertEquals(expectedValue, actualEntry.getValue(),
                        () -> "Unexpected value in entry set for key " + actualEntry.getKey());
            }

            Assertions.assertTrue(remainingEntryMap.isEmpty(),
                    () -> "Entry set is missing expected entries: " + remainingEntryMap);
        }

        private void checkValues() {
            Collection<V> actualValues = map.values();

            Assertions.assertEquals(expectedMap.size(), actualValues.size(),
                    "Unexpected values collection size");

            // check that each value in the list occurs the value number of times
            // as expect)ed
            final Map<V, Integer> expectedCounts = new HashMap<>();
            for (final Map.Entry<P, V> entry : expectedMap.entrySet()) {
                expectedCounts.merge(entry.getValue(), 1, (a, b) -> a + b);
            }

            final Map<V, Integer> actualCounts = new HashMap<>();
            for (final V value : actualValues) {
                actualCounts.merge(value, 1, (a, b) -> a + b);
            }

            for (final Map.Entry<V, Integer> expected : expectedCounts.entrySet()) {
                Assertions.assertEquals(expected.getValue(), actualCounts.get(expected.getKey()),
                        () -> "Unexpected count for value " + expected.getKey());
            }
        }

        private void checkUnexpectedKeys() {
            for (final P key : unexpectedKeys) {
                Assertions.assertFalse(map.containsKey(key), () -> "Expected map to not contain key " + key);
                Assertions.assertNull(map.get(key), () -> "Expected map to not contain value for key " + key);

                Assertions.assertNull(map.resolveKey(key), () -> "Expected map to not resolve key " + key);

                Assertions.assertFalse(map.keySet().contains(key),
                        () -> "Expected map key set to not contain " + key);

                final boolean inEntrySet = map.entrySet().stream()
                        .anyMatch(e -> e.getKey().equals(key));
                Assertions.assertFalse(inEntrySet, () -> "Expected map entry set to not contain key " + key);
            }
        }
    }
}
