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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    /** Get {@code cnt} number of unique test points that differ from each other in
     * each dimension by <em>at least</em> {@code eps}. The returned list is shuffled
     * using {@code rnd}.
     * @param cnt
     * @param eps
     * @param rnd
     * @return
     */
    protected List<P> getTestPoints(final int cnt, final double eps, final Random rnd) {
        final List<P> pts = new ArrayList<>(getTestPoints(cnt, eps));
        Collections.shuffle(pts, rnd);

        return pts;
    }

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
        final List<P> pts = getTestPoints(putCnt * 2, EPS, new Random(1L));

        // act
        for (int i = 0; i < putCnt; ++i) {
            final P key = pts.get(i);

            map.put(key, i);

            checker.expectEntry(key, i);
        }

        // assert
        checker
            .doesNotContainKeys(pts.subList(putCnt, pts.size()))
            .check();
    }

    @Test
    void testPut_replaceValue() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);
        final P a = pts.get(0);

        // act
        Assertions.assertNull(map.put(a, 1));
        Assertions.assertEquals(1, map.put(a, 2));
        Assertions.assertEquals(2, map.put(a, 3));

        // assert
        checkerFor(map)
            .expectEntry(a, 3)
            .doesNotContainKeys(pts.subList(1, pts.size()))
            .check();
    }

    @Test
    void testRemove() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        final P a = pts.get(0);
        final P b = pts.get(1);
        final P c = pts.get(2);
        final P d = pts.get(3);

        map.put(a, 1);
        map.put(b, 2);
        map.put(c, 3);

        // act/assert
        Assertions.assertNull(map.remove(d));
        Assertions.assertEquals(1, map.remove(a));
        Assertions.assertEquals(2, map.remove(b));
        Assertions.assertEquals(3, map.remove(c));

        Assertions.assertNull(map.remove(a));
        Assertions.assertNull(map.remove(b));
        Assertions.assertNull(map.remove(c));
        Assertions.assertNull(map.remove(d));

        checkerFor(map)
            .doesNotContainKeys(pts)
            .check();
    }

    @Test
    void testRemove_largeEntryCount() {
        // -- arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final Random rnd = new Random(2L);

        final int cnt = 10_000;
        final List<P> pts = getTestPoints(cnt * 2, EPS, rnd);

        final List<P> testPts = new ArrayList<>(pts.subList(0, cnt));
        final List<P> otherPts = new ArrayList<>(pts.subList(cnt, pts.size()));

        // -- act/assert
        // insert the test points
        final PointMapChecker<P, Integer> allChecker = checkerFor(map);
        final PointMapChecker<P, Integer> oddChecker = checkerFor(map);

        final List<P> evenKeys = new ArrayList<>();
        final List<P> oddKeys = new ArrayList<>();

        for (int i = 0; i < cnt; ++i) {
            final P key = testPts.get(i);

            Assertions.assertNull(map.put(key, i));

            allChecker.expectEntry(key, i);

            if (i % 2 == 0) {
                evenKeys.add(key);
            } else {
                oddKeys.add(key);
                oddChecker.expectEntry(key, i);
            }
        }

        // check map state after insertion of all test points
        allChecker
            .doesNotContainKeys(otherPts)
            .check();

        // remove points inserted on even indices; remove the keys in
        // a different order than insertion
        Collections.shuffle(evenKeys);
        for (final P key : evenKeys) {
            Assertions.assertNotNull(map.remove(key));
        }

        // check map state after partial removal
        oddChecker
            .doesNotContainKeys(otherPts)
            .doesNotContainKeys(evenKeys)
            .check();

        // remove remaining points
        Collections.shuffle(oddKeys);
        for (final P key : oddKeys) {
            Assertions.assertNotNull(map.remove(key));
        }

        // ensure that nothing is left
        checkerFor(map)
            .doesNotContainKeys(pts)
            .check();
    }

    @Test
    void testRepeatedUse() {
        // -- arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final Random rnd = new Random(3L);

        final int cnt = 10_000;
        final List<P> pts = getTestPoints(cnt, EPS, rnd);

        // -- act
        final int iterations = 10;
        final int subListSize = cnt / iterations;
        for (int i = 0; i < iterations; ++i) {
            final int subListStart = i * subListSize;
            final List<P> subList = pts.subList(subListStart, subListStart + subListSize);

            // add sublist
            insertPoints(subList, map);

            // remove sublist in different order
            final List<P> shuffledSubList = new ArrayList<>(subList);
            Collections.shuffle(shuffledSubList, rnd);

            removePoints(shuffledSubList, map);

            // add sublist again
            insertPoints(subList, map);
        }

        // -- assert
        PointMapChecker<P, Integer> checker = checkerFor(map);

        for (int i = 0; i < iterations * subListSize; ++i) {
            checker.expectEntry(pts.get(i), i % subListSize);
        }

        checker.check();
    }

    @Test
    void testEntrySetIterator_remove() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(1_000, EPS, new Random(10L));

        insertPoints(pts, map);

        // act
        // remove the entries in two passes: one to remove the entries with even
        // values and the second to remove the remaining
        Iterator<Map.Entry<P, Integer>> firstPass = map.entrySet().iterator();
        while (firstPass.hasNext()) {
            Map.Entry<P, Integer> entry = firstPass.next();
            if (entry.getValue() % 2 == 0) {
                firstPass.remove();
            }
        }

        Iterator<Map.Entry<P, Integer>> secondPass = map.entrySet().iterator();
        while (secondPass.hasNext()) {
            secondPass.next();
            secondPass.remove();
        }

        // assert
        assertEmpty(map);
    }

    /** Insert the given list of points into {@code map}. The value of each key is the index of the key
     * in {@code pts}.
     * @param <P> Point type
     * @param pts list of points
     * @param map map to insert into
     */
    public static <P extends Point<P>> void insertPoints(final List<P> pts, final PointMap<P, Integer> map) {
        int i = -1;
        for (final P pt : pts) {
            map.put(pt, ++i);
        }
    }

    /** Remove each point in {@code pts} from {@code map}.
     * @param <P> Point type
     * @param pts points to remove
     * @param map map to remove from
     */
    public static <P extends Point<P>> void removePoints(final List<P> pts, final PointMap<P, Integer> map) {
        for (final P pt : pts) {
            map.remove(pt);
        }
    }

    /** Return a new {@link PointMapChecker} for asserting the contents
     * of the given map.
     * @return a new checker instance
     */
    public static <P extends Point<P>, V> PointMapChecker<P, V> checkerFor(final PointMap<P, V> map) {
        return new PointMapChecker<>(map);
    }

    /** Assert that the given map is empty.
     * @param map map to assert empty
     */
    public static void assertEmpty(final PointMap<?, ?> map) {
        checkerFor(map)
            .check();
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
