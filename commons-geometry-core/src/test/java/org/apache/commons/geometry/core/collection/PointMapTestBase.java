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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Point;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Base test class for {@link PointMap} implementations.
 * @param <P> Point type
 */
public abstract class PointMapTestBase<P extends Point<P>>
    extends PointCollectionTestBase<P> {

    /** Get a new point map instance for testing.
     * @param <V> Value type
     * @param precision precision context to determine floating point equality
     * @return a new map instance for testing.
     */
    protected abstract <V> PointMap<P, V> getMap(Precision.DoubleEquivalence precision);

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
    void testGet() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);

        insertPoints(pts.subList(1, 3), map);
        // act/assert
        Assertions.assertNull(map.get(pts.get(0)));

        Assertions.assertEquals(Integer.valueOf(0), map.get(pts.get(1)));
        Assertions.assertEquals(Integer.valueOf(1), map.get(pts.get(2)));
    }

    @Test
    void testGet_equivalentPoints_singleEntry() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(1, EPS);
        insertPoints(pts, map);

        final P pt = pts.get(0);

        // act/assert
        Assertions.assertEquals(0, map.get(pt));
        Assertions.assertTrue(map.containsKey(pt));
        Assertions.assertEquals(new SimpleEntry<>(pt, 0), map.getEntry(pt));

        for (final P closePt : getTestPointsAtDistance(pt, EPS * 0.75)) {
            Assertions.assertEquals(0, map.get(closePt));
            Assertions.assertTrue(map.containsKey(closePt));
            Assertions.assertEquals(new SimpleEntry<>(pt, 0), map.getEntry(closePt));

            Assertions.assertTrue(map.entrySet().contains(new SimpleEntry<>(closePt, 0)));
            Assertions.assertTrue(map.keySet().contains(closePt));

            assertEq(closePt, pt, PRECISION);
        }

        for (final P farPt : getTestPointsAtDistance(pt, EPS * 1.25)) {
            Assertions.assertNull(map.get(farPt));
            Assertions.assertFalse(map.containsKey(farPt));
            Assertions.assertNull(map.getEntry(farPt));

            Assertions.assertFalse(map.entrySet().contains(new SimpleEntry<>(farPt, 0)));
            Assertions.assertFalse(map.keySet().contains(farPt));

            assertNotEq(farPt, pt, PRECISION);
        }
    }

    @Test
    void testGet_equivalentPoints_multipleEntries() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(1_000, 3 * EPS);
        insertPoints(pts, map);

        // act/assert
        int i = -1;
        for (final P pt : pts) {
            final int value = ++i;

            Assertions.assertEquals(value, map.get(pt));
            Assertions.assertTrue(map.containsKey(pt));
            Assertions.assertEquals(new SimpleEntry<>(pt, value), map.getEntry(pt));

            for (final P closePt : getTestPointsAtDistance(pt, EPS * 0.75)) {
                Assertions.assertEquals(value, map.get(closePt));
                Assertions.assertTrue(map.containsKey(closePt));
                Assertions.assertEquals(new SimpleEntry<>(pt, value), map.getEntry(closePt));

                Assertions.assertTrue(map.entrySet().contains(new SimpleEntry<>(closePt, value)));
                Assertions.assertTrue(map.keySet().contains(closePt));

                assertEq(closePt, pt, PRECISION);
            }

            for (final P farPt : getTestPointsAtDistance(pt, EPS * 1.25)) {
                Assertions.assertNull(map.get(farPt));
                Assertions.assertFalse(map.containsKey(farPt));
                Assertions.assertNull(map.getEntry(farPt));

                Assertions.assertFalse(map.entrySet().contains(new SimpleEntry<>(farPt, 0)));
                Assertions.assertFalse(map.keySet().contains(farPt));

                assertNotEq(farPt, pt, PRECISION);
            }
        }
    }

    @Test
    void testGet_invalidArgs() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        insertPoints(getTestPoints(3, EPS), map);

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> map.get(null));
        Assertions.assertThrows(ClassCastException.class, () -> map.get(new Object()));
    }

    @Test
    void testGet_nanAndInf() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        insertPoints(getTestPoints(100, EPS), map);

        // act/assert
        for (final P pt : getNaNPoints()) {
            Assertions.assertNull(map.get(pt));
        }

        for (final P pt : getInfPoints()) {
            Assertions.assertNull(map.get(pt));
        }
    }

    @Test
    void testGetEntry_canSetValue() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(100, EPS);
        insertPoints(pts, map);

        final Map.Entry<P, Integer> entry = map.getEntry(pts.get(1));

        // act
        Assertions.assertEquals(1, entry.setValue(Integer.valueOf(-1)));
        Assertions.assertEquals(-1, entry.setValue(Integer.valueOf(-2)));

        // assert
        Assertions.assertEquals(-2, map.get(pts.get(1)));
    }

    @Test
    void testGetEntry_setCalledAfterEntryRemoved_updatesLocalEntryOnly() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(100, EPS);
        insertPoints(pts, map);

        final Map.Entry<P, Integer> entry = map.getEntry(pts.get(49));
        final Integer oldValue = entry.getValue();

        // act/assert
        map.remove(entry.getKey());

        Assertions.assertEquals(oldValue, entry.setValue(-1));
        Assertions.assertEquals(-1, entry.setValue(-2));

        Assertions.assertNull(map.get(entry.getKey()));
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
    void testPut_equivalentValues_multipleEntries() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(1_000, EPS, new Random(10L));
        insertPoints(pts, map);

        final double delta = 0.75 * EPS;

        // act/assert
        int i = 0;
        for (final P pt : pts) {
            final List<P> closePts = getTestPointsAtDistance(pt, delta);
            checkPut(map, closePts, i++);
        }
    }

    private void checkPut(final PointMap<P, Integer> map, final List<? extends P> pts, final int startValue) {
        int currentValue = startValue;
        for (final P pt : pts) {
            int nextValue = currentValue + 1;

            Assertions.assertEquals(currentValue, map.put(pt, nextValue));
            Assertions.assertEquals(nextValue, map.get(pt));

            currentValue = nextValue;
        }
    }

    @Test
    void testPut_nullKey() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> map.put(null, 0));
    }

    @Test
    void testPut_nanAndInf() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        // act/assert
        for (final P nanPt : getNaNPoints()) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> map.put(nanPt, 0));
        }

        for (final P infPt : getInfPoints()) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> map.put(infPt, 0));
        }
    }

    @Test
    void testPutAll_nonPointMap() {
        // arrange
        final Map<P, Integer> a = new HashMap<>();
        final Map<P, Integer> b = new HashMap<>();

        final PointMap<P, Integer> c = getMap(PRECISION);

        final List<P> pts = getTestPoints(5, EPS);

        a.put(pts.get(0), 0);
        a.put(pts.get(1), 1);
        a.put(pts.get(2), 2);

        b.put(pts.get(2), 0);
        b.put(pts.get(3), 1);
        b.put(pts.get(4), 2);

        // act
        c.putAll(a);
        c.putAll(b);

        // assert
        checkerFor(c)
            .expectEntry(pts.get(0), 0)
            .expectEntry(pts.get(1), 1)
            .expectEntry(pts.get(2), 0)
            .expectEntry(pts.get(3), 1)
            .expectEntry(pts.get(4), 2)
            .check();
    }

    @Test
    void testPutAll_otherPointMap() {
        // arrange
        final PointMap<P, Integer> a = getMap(PRECISION);
        final PointMap<P, Integer> b = getMap(PRECISION);
        final PointMap<P, Integer> c = getMap(PRECISION);

        final List<P> pts = getTestPoints(5, EPS);

        insertPoints(pts.subList(0, 3), a);
        insertPoints(pts.subList(2, 5), b);

        // act
        c.putAll(a);
        c.putAll(b);

        // assert
        checkerFor(c)
            .expectEntry(pts.get(0), 0)
            .expectEntry(pts.get(1), 1)
            .expectEntry(pts.get(2), 0)
            .expectEntry(pts.get(3), 1)
            .expectEntry(pts.get(4), 2)
            .check();
    }

    @Test
    void testPutAll_nanAndInf() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        // act/assert
        for (final P nanPt : getNaNPoints()) {
            final Map<P, Integer> nanMap = new HashMap<>();
            nanMap.put(nanPt, 0);

            Assertions.assertThrows(IllegalArgumentException.class, () -> map.putAll(nanMap));
        }

        for (final P infPt : getInfPoints()) {
            final Map<P, Integer> infMap = new HashMap<>();
            infMap.put(infPt, 0);

            Assertions.assertThrows(IllegalArgumentException.class, () -> map.putAll(infMap));
        }
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
    void testRemove_equivalentPoints() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(100, EPS, new Random(9L));
        insertPoints(pts, map);

        // act
        for (final P pt : pts) {
            final Integer val = map.get(pt);

            for (final P closePt : getTestPointsAtDistance(pt, 0.9 * EPS)) {
                map.put(pt, val);

                Assertions.assertEquals(val, map.remove(closePt));
            }
        }

        // assert
        assertEmpty(map);
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
    void testClear_empty() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        // act
        map.clear();

        // assert
        assertEmpty(map);
    }

    @Test
    void testClear_populated() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);
        insertPoints(getTestPoints(1_000, EPS, new Random(6L)), map);

        // act
        map.clear();

        // assert
        assertEmpty(map);
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
    void testNearestEntry_empty() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);

        // act/assert
        Assertions.assertNull(map.nearestEntry(pt));
    }

    @Test
    void testNearestEntry_small() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final double keySpacing = 7 * EPS;
        final double testPointSpacing = 3 * EPS;

        int maxCnt = 5;
        for (int cnt = 1; cnt <= maxCnt; ++cnt) {
            final List<P> pts = getTestPoints(cnt, keySpacing, new Random(cnt));

            map.clear();
            insertPoints(pts, map);

            // act/ assert
            for (int i = 0; i < cnt; ++i) {
                final P pt = pts.get(i);

                for (final P refPt : getTestPointsAtDistance(pt, testPointSpacing)) {
                    Assertions.assertNull(map.get(refPt));

                    final Map.Entry<P, Integer> closest = map.nearestEntry(refPt);

                    Assertions.assertEquals(pt, closest.getKey());
                    Assertions.assertEquals(i, closest.getValue());
                }
            }
        }
    }

    @Test
    void testNearestEntry_large() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final double keySpacing = 7 * EPS;
        final double testPointSpacing = 3 * EPS;

        final int cnt = 1000;
        final List<P> pts = getTestPoints(cnt, keySpacing, new Random(5L));
        insertPoints(pts, map);

        // act/ assert
        for (int i = 0; i < cnt; ++i) {
            final P pt = pts.get(i);

            for (final P refPt : getTestPointsAtDistance(pt, testPointSpacing)) {
                Assertions.assertNull(map.get(refPt));

                final Map.Entry<P, Integer> nearest = map.nearestEntry(refPt);

                Assertions.assertEquals(pt, nearest.getKey());
                Assertions.assertEquals(i, nearest.getValue());
            }
        }
    }

    @Test
    void testNearestEntry_invalidArgs() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P infPt = getInfPoints().get(0);

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> map.nearestEntry(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> map.nearestEntry(infPt));
    }

    @Test
    void testFarthestEntry_empty() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);

        // act/assert
        Assertions.assertNull(map.farthestEntry(pt));
    }

    @Test
    void testFarthestEntry_small() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final double keySpacing = 7 * EPS;
        final double testPointSpacing = 3 * EPS;

        int maxCnt = 5;
        for (int cnt = 1; cnt <= maxCnt; ++cnt) {
            final List<P> pts = getTestPoints(cnt, keySpacing, new Random(cnt));

            map.clear();
            insertPoints(pts, map);

            // act/ assert
            for (int i = 0; i < cnt; ++i) {
                final P pt = pts.get(i);

                for (final P refPt : getTestPointsAtDistance(pt, testPointSpacing)) {
                    Assertions.assertNull(map.get(refPt));

                    final Map.Entry<P, Integer> farthest = map.farthestEntry(refPt);

                    Assertions.assertEquals(findFarthest(refPt, pts), farthest.getKey());
                }
            }
        }
    }

    @Test
    void testFarthestEntry_large() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final double keySpacing = 7 * EPS;
        final double testPointSpacing = 3 * EPS;

        final int cnt = 1000;
        final List<P> pts = getTestPoints(cnt, keySpacing, new Random(5L));
        insertPoints(pts, map);

        // act/ assert
        for (int i = 0; i < cnt; ++i) {
            final P pt = pts.get(i);

            for (final P refPt : getTestPointsAtDistance(pt, testPointSpacing)) {
                Assertions.assertNull(map.get(refPt));

                final Map.Entry<P, Integer> farthest = map.farthestEntry(refPt);

                Assertions.assertEquals(findFarthest(refPt, pts), farthest.getKey());
            }
        }
    }

    @Test
    void testFarthestEntry_invalidArgs() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P infPt = getInfPoints().get(0);

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> map.farthestEntry(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> map.farthestEntry(infPt));
    }

    @Test
    void testEntriesNearToFar_empty() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);
        final List<P> ordered = new ArrayList<>();

        // act
        final Collection<Entry<P, Integer>> coll = map.entriesNearToFar(pt);

        // assert
        Assertions.assertEquals(0, coll.size());

        for (final Map.Entry<P, Integer> entry : coll) {
            ordered.add(entry.getKey());
        }
        Assertions.assertEquals(0, ordered.size());
    }

    @Test
    void testEntriesNearToFar_small() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        int maxCnt = 5;
        for (int cnt = 1; cnt <= maxCnt; ++cnt) {
            final List<P> pts = getTestPoints(cnt, EPS, new Random(cnt));

            map.clear();
            insertPoints(pts, map);

            // act/ assert
            for (int i = 0; i < cnt; ++i) {
                for (final P refPt : getTestPointsAtDistance(pts.get(i), 2.1 * EPS)) {
                    assertCollectionOrder(
                            pts,
                            createNearToFarComparator(refPt),
                            map.entriesNearToFar(refPt));
                }
            }
        }
    }

    @Test
    void testEntriesNearToFar_large() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        int cnt = 1000;
        final List<P> pts = getTestPoints(cnt, EPS, new Random(6L));
        insertPoints(pts, map);

        // act/ assert
        for (int i = 0; i < cnt; ++i) {
            for (final P refPt : getTestPointsAtDistance(pts.get(i), 2.1 * EPS)) {
                assertCollectionOrder(
                        pts,
                        createNearToFarComparator(refPt),
                        map.entriesNearToFar(refPt));
            }
        }
    }

    @Test
    void testEntriesNearToFar_iteratorBehavior() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(2, EPS);
        insertPoints(pts, map);

        // act/assert
        final Iterator<Map.Entry<P, Integer>> it = map.entriesNearToFar(pts.get(0)).iterator();
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals(new SimpleEntry<>(pts.get(0), 0), it.next());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> it.remove());

        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals(new SimpleEntry<>(pts.get(1), 1), it.next());

        Assertions.assertFalse(it.hasNext());

        Assertions.assertThrows(NoSuchElementException.class, () -> it.next());
    }

    @Test
    void testEntriesNearToFar_concurrentModification() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(2, EPS);
        insertPoints(pts, map);

        // act
        final Iterator<Map.Entry<P, Integer>> it = map.entriesNearToFar(pts.get(0)).iterator();
        map.remove(pts.get(0));

        // assert
        Assertions.assertThrows(ConcurrentModificationException.class, () -> it.next());
    }

    @Test
    void testEntriesNearToFar_invalidArgs() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P infPt = getInfPoints().get(0);

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> map.entriesNearToFar(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> map.entriesNearToFar(infPt));
    }

    @Test
    void testEntriesFarToNear_empty() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);
        final List<P> ordered = new ArrayList<>();

        // act
        final Collection<Entry<P, Integer>> coll = map.entriesFarToNear(pt);

        // assert
        Assertions.assertEquals(0, coll.size());

        for (final Map.Entry<P, Integer> entry : coll) {
            ordered.add(entry.getKey());
        }
        Assertions.assertEquals(0, ordered.size());
    }

    @Test
    void testEntriesFarToNear_small() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        int maxCnt = 5;
        for (int cnt = 1; cnt <= maxCnt; ++cnt) {
            final List<P> pts = getTestPoints(cnt, EPS, new Random(cnt));

            map.clear();
            insertPoints(pts, map);

            // act/ assert
            for (int i = 0; i < cnt; ++i) {
                for (final P refPt : getTestPointsAtDistance(pts.get(i), 2.1 * EPS)) {
                    assertCollectionOrder(
                            pts,
                            createFarToNearComparator(refPt),
                            map.entriesFarToNear(refPt));
                }
            }
        }
    }

    @Test
    void testEntriesFarToNear_large() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        int cnt = 1000;
        final List<P> pts = getTestPoints(cnt, EPS, new Random(6L));
        insertPoints(pts, map);

        // act/ assert
        for (int i = 0; i < cnt; ++i) {
            for (final P refPt : getTestPointsAtDistance(pts.get(i), 2.1 * EPS)) {
                assertCollectionOrder(
                        pts,
                        createFarToNearComparator(refPt),
                        map.entriesFarToNear(refPt));
            }
        }
    }

    @Test
    void testEntriesFarToNear_iteratorBehavior() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(2, EPS);
        insertPoints(pts, map);

        // act/assert
        final Iterator<Map.Entry<P, Integer>> it = map.entriesFarToNear(pts.get(0)).iterator();
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals(new SimpleEntry<>(pts.get(1), 1), it.next());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> it.remove());

        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals(new SimpleEntry<>(pts.get(0), 0), it.next());

        Assertions.assertFalse(it.hasNext());

        Assertions.assertThrows(NoSuchElementException.class, () -> it.next());
    }

    @Test
    void testEntriesFarToNear_concurrentModification() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);
        insertPoints(pts, map);

        // act
        final Iterator<Map.Entry<P, Integer>> it = map.entriesFarToNear(pts.get(0)).iterator();
        map.remove(pts.get(0));

        // assert
        Assertions.assertThrows(ConcurrentModificationException.class, () -> it.next());
    }

    @Test
    void testEntriesFarToNear_invalidArgs() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P infPt = getInfPoints().get(0);

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> map.entriesFarToNear(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> map.entriesFarToNear(infPt));
    }

    @Test
    void testHashCode() {
        // arrange
        final PointMap<P, Integer> a = getMap(PRECISION);
        final PointMap<P, Integer> b = getMap(PRECISION);
        final PointMap<P, Integer> c = getMap(PRECISION);
        final PointMap<P, Integer> d = getMap(PRECISION);
        final PointMap<P, Integer> e = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);

        insertPoints(pts, a);
        insertPoints(pts.subList(0, 2), b);

        insertPoints(pts, d);
        d.put(pts.get(0), -1);

        insertPoints(pts, e);

        // act
        final int hash = a.hashCode();

        // act/assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());
        Assertions.assertNotEquals(hash, d.hashCode());

        Assertions.assertEquals(hash, e.hashCode());
    }

    @Test
    void testEquals() {
        // arrange
        final PointMap<P, Integer> a = getMap(PRECISION);
        final PointMap<P, Integer> b = getMap(PRECISION);
        final PointMap<P, Integer> c = getMap(PRECISION);
        final PointMap<P, Integer> d = getMap(PRECISION);
        final PointMap<P, Integer> e = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);

        insertPoints(pts, a);
        insertPoints(pts.subList(0, 2), b);

        insertPoints(pts, d);
        d.put(pts.get(0), -1);

        insertPoints(pts, e);

        // act/assert
        Assertions.assertFalse(a.equals(null));
        Assertions.assertFalse(a.equals(new Object()));

        Assertions.assertTrue(a.equals(a));

        Assertions.assertFalse(a.equals(b));
        Assertions.assertFalse(a.equals(c));
        Assertions.assertFalse(a.equals(d));

        Assertions.assertTrue(a.equals(e));
    }

    @Test
    void testToString() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);
        final List<P> pts = getTestPoints(1, EPS);
        insertPoints(pts, map);

        // act
        final String str = map.toString();

        // assert
        GeometryTestUtils.assertContains(pts.get(0).toString(), str);
    }

    // EntrySet -----------------------------------

    @Test
    void testEntrySet_add_unsupported() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);
        final Map.Entry<P, Integer> entry = new SimpleEntry<>(pt, 100);

        // act/assert
        assertCollectionAddUnsupported(map.entrySet(), entry);
    }

    @Test
    void testEntrySet_setValue() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(100, EPS);
        insertPoints(pts, map);

        final int offset = 100;

        // act
        for (final Map.Entry<P, Integer> entry : map.entrySet()) {
            final Integer val = entry.getValue();
            final Integer newVal = val + offset;

            Assertions.assertEquals(val, entry.setValue(newVal));
            Assertions.assertEquals(newVal, entry.getValue());
        }

        // assert
        final PointMapChecker<P, Integer> checker = checkerFor(map);
        int i = offset;
        for (final P pt : pts) {
            checker.expectEntry(pt, i++);
        }

        checker.check();
    }

    @Test
    void testEntrySet_clear() {
        // act/assert
        assertCollectionClear(PointMap::entrySet);
    }

    @Test
    void testEntrySet_contains() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);
        insertPoints(pts.subList(0, 1),  map);

        final Set<Map.Entry<P, Integer>> entrySet = map.entrySet();

        // act/assert
        Assertions.assertFalse(entrySet.contains(null));
        Assertions.assertFalse(entrySet.contains(new Object()));

        Assertions.assertTrue(entrySet.contains(new SimpleEntry<>(pts.get(0), 0)));

        Assertions.assertFalse(entrySet.contains(new SimpleEntry<>(pts.get(0), 1)));
        Assertions.assertFalse(entrySet.contains(new SimpleEntry<>(pts.get(1), 0)));
    }

    @Test
    void testEntrySet_containsAll() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        insertPoints(pts.subList(0, 2),  map);

        final Set<Map.Entry<P, Integer>> entrySet = map.entrySet();

        final Map.Entry<P, Integer> a = new SimpleEntry<>(pts.get(0), 0);
        final Map.Entry<P, Integer> b = new SimpleEntry<>(pts.get(1), 1);

        final Map.Entry<P, Integer> c = new SimpleEntry<>(pts.get(2), 2);
        final Map.Entry<P, Integer> d = new SimpleEntry<>(pts.get(3), 3);

        // act/assert
        Assertions.assertFalse(entrySet.containsAll(Arrays.asList(new Object(), new Object())));

        Assertions.assertTrue(entrySet.containsAll(new ArrayList<>()));

        Assertions.assertTrue(entrySet.containsAll(Arrays.asList(a)));
        Assertions.assertTrue(entrySet.containsAll(Arrays.asList(b, a)));

        Assertions.assertFalse(entrySet.containsAll(Arrays.asList(a, b, c)));
        Assertions.assertFalse(entrySet.containsAll(Arrays.asList(c, d)));
    }

    @Test
    void testEntrySet_remove() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);
        insertPoints(pts, map);

        final Set<Map.Entry<P, Integer>> entrySet = map.entrySet();

        final Map.Entry<P, Integer> a = new SimpleEntry<>(pts.get(0), 0);
        final Map.Entry<P, Integer> b = new SimpleEntry<>(pts.get(1), 1);
        final Map.Entry<P, Integer> c = new SimpleEntry<>(pts.get(2), 2);

        // act/assert
        Assertions.assertFalse(entrySet.remove(null));
        Assertions.assertFalse(entrySet.remove(new Object()));

        Assertions.assertFalse(entrySet.remove(new SimpleEntry<>(pts.get(0), 1)));
        Assertions.assertFalse(entrySet.remove(new SimpleEntry<>(pts.get(1), 0)));

        checkerFor(map)
            .expectEntry(a)
            .expectEntry(b)
            .expectEntry(c)
            .check();

        Assertions.assertTrue(entrySet.remove(a));

        checkerFor(map)
            .expectEntry(b)
            .expectEntry(c)
            .check();

        Assertions.assertTrue(entrySet.remove(b));

        checkerFor(map)
            .expectEntry(c)
            .check();

        Assertions.assertTrue(entrySet.remove(c));

        Assertions.assertEquals(0, entrySet.size());
        assertEmpty(map);

        Assertions.assertFalse(entrySet.remove(a));
        Assertions.assertFalse(entrySet.remove(b));
        Assertions.assertFalse(entrySet.remove(c));
    }

    @Test
    void testEntrySet_removeAll() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        insertPoints(pts.subList(0, 3), map);

        final Set<Map.Entry<P, Integer>> entrySet = map.entrySet();

        final Map.Entry<P, Integer> a = new SimpleEntry<>(pts.get(0), 0);
        final Map.Entry<P, Integer> b = new SimpleEntry<>(pts.get(1), 1);
        final Map.Entry<P, Integer> c = new SimpleEntry<>(pts.get(2), 2);
        final Map.Entry<P, Integer> d = new SimpleEntry<>(pts.get(3), 3);

        // act/assert
        Assertions.assertFalse(entrySet.removeAll(Arrays.asList(new Object(), new Object())));

        Assertions.assertFalse(entrySet.removeAll(new ArrayList<>()));
        Assertions.assertFalse(entrySet.removeAll(Arrays.asList(d)));

        checkerFor(map)
            .expectEntry(a)
            .expectEntry(b)
            .expectEntry(c)
            .check();

        Assertions.assertTrue(entrySet.removeAll(Arrays.asList(a, b)));

        checkerFor(map)
            .expectEntry(c)
            .check();

        Assertions.assertTrue(entrySet.removeAll(Arrays.asList(c, d)));

        Assertions.assertEquals(0, entrySet.size());
        assertEmpty(map);

        Assertions.assertFalse(entrySet.removeAll(Arrays.asList(a, b)));
        Assertions.assertFalse(entrySet.removeAll(Arrays.asList(c, d)));
    }

    @Test
    void testEntrySet_retainAll() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        insertPoints(pts.subList(0, 3), map);

        final Set<Map.Entry<P, Integer>> entrySet = map.entrySet();

        final Map.Entry<P, Integer> a = new SimpleEntry<>(pts.get(0), 0);
        final Map.Entry<P, Integer> b = new SimpleEntry<>(pts.get(1), 1);
        final Map.Entry<P, Integer> c = new SimpleEntry<>(pts.get(2), 2);
        final Map.Entry<P, Integer> d = new SimpleEntry<>(pts.get(3), 3);

        // act/assert
        Assertions.assertFalse(entrySet.retainAll(Arrays.asList(a, b, c)));

        checkerFor(map)
            .expectEntry(a)
            .expectEntry(b)
            .expectEntry(c)
            .check();

        Assertions.assertTrue(entrySet.retainAll(Arrays.asList(a, b, d)));

        checkerFor(map)
            .expectEntry(a)
            .expectEntry(b)
            .check();

        Assertions.assertTrue(entrySet.retainAll(Arrays.asList(new Object(), new Object())));

        Assertions.assertEquals(0, entrySet.size());
        assertEmpty(map);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testEntrySet_toArray() {
        // act/assert
        assertCollectionToArray(PointMap::entrySet, new Map.Entry[0]);
    }

    @Test
    void testEntrySet_equalsAndHashCode() {
        // act/assert
        assertCollectionEquals(PointMap::entrySet);
        assertCollectionHashCode(PointMap::entrySet);
    }

    @Test
    void testEntrySet_toString() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);
        final List<P> pts = getTestPoints(20, EPS);
        insertPoints(pts, map);

        // act
        final String str = map.entrySet().toString();

        // assert
        GeometryTestUtils.assertContains(pts.get(17).toString(), str);
        GeometryTestUtils.assertContains(Integer.toString(17), str);
    }

    @Test
    void testEntrySetIterator() {
        // act/assert
        assertCollectionIterator(PointMap::entrySet);
        assertCollectionIteratorRemove(PointMap::entrySet);
        assertCollectionIteratorRemoveWithMultiplePasses(PointMap::entrySet);
        assertCollectionIteratorRemoveWithoutNext(PointMap::entrySet);
        assertCollectionIteratorRemoveMultipleCalls(PointMap::entrySet);
        assertCollectionIteratorConcurrentModification(PointMap::entrySet);
    }

    // KeySet -----------------------------------

    @Test
    void testKeySet_add_unsupported() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);

        // act/assert
        assertCollectionAddUnsupported(map.keySet(), pt);
    }

    @Test
    void testKeySet_clear() {
        // act/assert
        assertCollectionClear(PointMap::keySet);
    }

    @Test
    void testKeySet_contains() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);
        insertPoints(pts.subList(0, 1),  map);

        final Set<P> keySet = map.keySet();

        // act/assert
        Assertions.assertTrue(keySet.contains(pts.get(0)));
        Assertions.assertFalse(keySet.contains(pts.get(1)));
    }

    @Test
    void testKeySet_containsAll() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        insertPoints(pts.subList(0, 2),  map);

        final Set<P> keySet = map.keySet();

        final P a = pts.get(0);
        final P b = pts.get(1);

        final P c = pts.get(2);
        final P d = pts.get(3);

        // act/assert
        Assertions.assertTrue(keySet.containsAll(new ArrayList<>()));

        Assertions.assertTrue(keySet.containsAll(Arrays.asList(a)));
        Assertions.assertTrue(keySet.containsAll(Arrays.asList(b, a)));

        Assertions.assertFalse(keySet.containsAll(Arrays.asList(a, b, c)));
        Assertions.assertFalse(keySet.containsAll(Arrays.asList(c, d)));
    }

    @Test
    void testKeySet_remove() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        insertPoints(pts.subList(0, 3), map);

        final Set<P> keySet = map.keySet();

        final P a = pts.get(0);
        final P b = pts.get(1);
        final P c = pts.get(2);
        final P d = pts.get(3);

        // act/assert
        Assertions.assertTrue(keySet.remove(a));
        Assertions.assertTrue(keySet.remove(b));
        Assertions.assertFalse(keySet.remove(d));

        checkerFor(map)
            .expectEntry(c, 2)
            .check();

        Assertions.assertTrue(keySet.remove(c));

        Assertions.assertEquals(0, keySet.size());
        assertEmpty(map);

        Assertions.assertFalse(keySet.remove(a));
        Assertions.assertFalse(keySet.remove(b));
        Assertions.assertFalse(keySet.remove(c));
        Assertions.assertFalse(keySet.remove(d));
    }

    @Test
    void testKeySet_removeAll() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        insertPoints(pts.subList(0, 3), map);

        final Set<P> keySet = map.keySet();

        final P a = pts.get(0);
        final P b = pts.get(1);

        final P c = pts.get(2);
        final P d = pts.get(3);

        // act/assert
        Assertions.assertFalse(keySet.removeAll(new ArrayList<>()));
        Assertions.assertFalse(keySet.removeAll(Arrays.asList(d)));

        checkerFor(map)
            .expectEntry(a, 0)
            .expectEntry(b, 1)
            .expectEntry(c, 2)
            .check();

        Assertions.assertTrue(keySet.removeAll(Arrays.asList(a, b)));

        checkerFor(map)
            .expectEntry(c, 2)
            .check();

        Assertions.assertTrue(keySet.removeAll(Arrays.asList(c, d)));

        Assertions.assertEquals(0, keySet.size());
        assertEmpty(map);

        Assertions.assertFalse(keySet.removeAll(Arrays.asList(a, b)));
        Assertions.assertFalse(keySet.removeAll(Arrays.asList(c, d)));
    }

    @Test
    void testKeySet_retainAll() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        insertPoints(pts.subList(0, 3), map);

        final Set<P> keySet = map.keySet();

        final P a = pts.get(0);
        final P b = pts.get(1);

        final P c = pts.get(2);
        final P d = pts.get(3);

        // act/assert
        Assertions.assertFalse(keySet.retainAll(Arrays.asList(a, b, c)));

        checkerFor(map)
            .expectEntry(a, 0)
            .expectEntry(b, 1)
            .expectEntry(c, 2)
            .check();

        Assertions.assertTrue(keySet.retainAll(Arrays.asList(a, b, d)));

        checkerFor(map)
            .expectEntry(a, 0)
            .expectEntry(b, 1)
            .check();

        Assertions.assertTrue(keySet.retainAll(Arrays.asList(new Object(), new Object())));

        Assertions.assertEquals(0, keySet.size());
        assertEmpty(map);
    }

    @Test
    void testKeySet_toArray() {
        // act/assert
        assertCollectionToArray(PointMap::keySet, createPointArray());
    }

    @Test
    void testKeySet_equalsAndHashCode() {
        // act/assert
        assertCollectionEquals(PointMap::keySet);
        assertCollectionHashCode(PointMap::keySet);
    }

    @Test
    void testKeySet_toString() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);
        final List<P> pts = getTestPoints(1, EPS);
        insertPoints(pts, map);

        // act
        final String str = map.keySet().toString();

        // assert
        GeometryTestUtils.assertContains(pts.get(0).toString(), str);
    }

    @Test
    void testKeySetIterator() {
        // act/assert
        assertCollectionIterator(PointMap::keySet);
        assertCollectionIteratorRemove(PointMap::keySet);
        assertCollectionIteratorRemoveWithMultiplePasses(PointMap::keySet);
        assertCollectionIteratorRemoveWithoutNext(PointMap::keySet);
        assertCollectionIteratorRemoveMultipleCalls(PointMap::keySet);
        assertCollectionIteratorConcurrentModification(PointMap::keySet);
    }

    // Values -----------------------------------

    @Test
    void testValues_add_unsupported() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        // act/assert
        assertCollectionAddUnsupported(map.values(), 2);
    }

    @Test
    void testValues_clear() {
        // act/assert
        assertCollectionClear(PointMap::values);
    }

    @Test
    void testValues_contains() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);
        insertPoints(pts.subList(0, 1),  map);

        final Collection<Integer> values = map.values();

        // act/assert
        Assertions.assertTrue(values.contains(0));
        Assertions.assertFalse(values.contains(1));
    }

    @Test
    void testValues_containsAll() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        insertPoints(pts.subList(0, 2),  map);

        final Collection<Integer> values = map.values();

        // act/assert
        Assertions.assertFalse(values.containsAll(Arrays.asList(new Object())));

        Assertions.assertTrue(values.containsAll(new ArrayList<>()));

        Assertions.assertTrue(values.containsAll(Arrays.asList(0)));
        Assertions.assertTrue(values.containsAll(Arrays.asList(1, 0)));

        Assertions.assertFalse(values.containsAll(Arrays.asList(0, 1, 2)));
        Assertions.assertFalse(values.containsAll(Arrays.asList(2, 3)));
    }

    @Test
    void testValues_remove() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(5, EPS);
        insertPoints(pts.subList(0, 3), map);

        map.put(pts.get(4), 0);

        final Collection<Integer> values = map.values();

        // act/assert
        Assertions.assertTrue(values.remove(0));
        Assertions.assertTrue(values.remove(0));
        Assertions.assertTrue(values.remove(1));
        Assertions.assertFalse(values.remove(3));

        checkerFor(map)
            .expectEntry(pts.get(2), 2)
            .check();

        Assertions.assertTrue(values.remove(2));

        Assertions.assertEquals(0, values.size());
        assertEmpty(map);

        Assertions.assertFalse(values.remove(0));
        Assertions.assertFalse(values.remove(1));
        Assertions.assertFalse(values.remove(2));
        Assertions.assertFalse(values.remove(3));
    }

    @Test
    void testValues_removeAll() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        insertPoints(pts.subList(0, 3), map);

        final Collection<Integer> values = map.values();

        final P a = pts.get(0);
        final P b = pts.get(1);

        final P c = pts.get(2);

        // act/assert
        Assertions.assertFalse(values.removeAll(new ArrayList<>()));
        Assertions.assertFalse(values.removeAll(Arrays.asList(4)));

        checkerFor(map)
            .expectEntry(a, 0)
            .expectEntry(b, 1)
            .expectEntry(c, 2)
            .check();

        Assertions.assertTrue(values.removeAll(Arrays.asList(0, 1)));

        checkerFor(map)
            .expectEntry(c, 2)
            .check();

        Assertions.assertTrue(values.removeAll(Arrays.asList(2, 3)));

        Assertions.assertEquals(0, values.size());
        assertEmpty(map);

        Assertions.assertFalse(values.removeAll(Arrays.asList(0, 1)));
        Assertions.assertFalse(values.removeAll(Arrays.asList(2, 3)));
    }

    @Test
    void testValues_retainAll() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        insertPoints(pts.subList(0, 3), map);

        final Collection<Integer> values = map.values();

        final P a = pts.get(0);
        final P b = pts.get(1);

        final P c = pts.get(2);

        // act/assert
        Assertions.assertFalse(values.retainAll(Arrays.asList(0, 1, 2)));

        checkerFor(map)
            .expectEntry(a, 0)
            .expectEntry(b, 1)
            .expectEntry(c, 2)
            .check();

        Assertions.assertTrue(values.retainAll(Arrays.asList(0, 1, 3)));

        checkerFor(map)
            .expectEntry(a, 0)
            .expectEntry(b, 1)
            .check();

        Assertions.assertTrue(values.retainAll(Arrays.asList(new Object(), new Object())));

        Assertions.assertEquals(0, values.size());
        assertEmpty(map);
    }

    @Test
    void testValues_toArray() {
        // act/assert
        assertCollectionToArray(PointMap::values, new Integer[0]);
    }

    @Test
    void testValues_toString() {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);
        final List<P> pts = getTestPoints(20, EPS);
        insertPoints(pts, map);

        // act
        final String str = map.values().toString();

        // assert
        GeometryTestUtils.assertContains(Integer.toString(17), str);
    }

    @Test
    void testValuesIterator() {
        // act/assert
        assertCollectionIterator(PointMap::values);
        assertCollectionIteratorRemove(PointMap::values);
        assertCollectionIteratorRemoveWithMultiplePasses(PointMap::values);
        assertCollectionIteratorRemoveWithoutNext(PointMap::values);
        assertCollectionIteratorRemoveMultipleCalls(PointMap::values);
        assertCollectionIteratorConcurrentModification(PointMap::values);
    }

    // Helpers -----------------------------------

    protected <V> Comparator<Entry<P, V>> createNearToFarEntryComparator(final P refPt) {
        final Comparator<Entry<P, V>> cmp =
                (a, b) -> Double.compare(a.getKey().distance(refPt), b.getKey().distance(refPt));
        return cmp.thenComparing((a, b) -> disambiguateNearToFarOrder(a.getKey(), b.getKey()));
    }

    private <V> void assertCollectionAddUnsupported(final Collection<V> coll, final V value) {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> coll.add(value));

        final List<V> valueList = Arrays.asList(value);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> coll.addAll(valueList));
    }

    private void assertCollectionClear(final Function<PointMap<P, ?>, Collection<?>> collectionFactory) {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);
        insertPoints(pts, map);

        final Collection<?> coll = collectionFactory.apply(map);

        // act
        coll.clear();

        // assert
        Assertions.assertEquals(0, coll.size());
        Assertions.assertTrue(coll.isEmpty());

        assertEmpty(map);
    }

    private <T> void assertCollectionToArray(final Function<PointMap<P, Integer>, Collection<T>> collectionFactory,
            final T[] typedArray) {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);
        insertPoints(pts, map);

        final Collection<T> coll = collectionFactory.apply(map);

        // act
        final Object[] objArr = coll.toArray();
        final T[] tArr = coll.toArray(typedArray);

        // assert
        Assertions.assertEquals(map.size(), objArr.length);
        Assertions.assertEquals(map.size(), tArr.length);

        int i = 0;
        for (final T element : coll) {
            Assertions.assertEquals(element, objArr[i]);
            Assertions.assertEquals(element, tArr[i]);

            ++i;
        }
    }

    private void assertCollectionEquals(final Function<PointMap<P, ?>, Collection<?>> collectionFactory) {
        // arrange
        final PointMap<P, Integer> mapA = getMap(PRECISION);
        final PointMap<P, Integer> mapB = getMap(PRECISION);
        final PointMap<P, Integer> mapC = getMap(PRECISION);
        final PointMap<P, Integer> mapD = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);

        insertPoints(pts, mapA);
        insertPoints(pts.subList(0, 2), mapB);

        insertPoints(pts, mapD);

        final Collection<?> a = collectionFactory.apply(mapA);
        final Collection<?> b = collectionFactory.apply(mapB);
        final Collection<?> c = collectionFactory.apply(mapC);
        final Collection<?> d = collectionFactory.apply(mapD);

        // act/assert
        Assertions.assertFalse(a.equals(null));
        Assertions.assertFalse(a.equals(new Object()));

        Assertions.assertTrue(a.equals(a));

        Assertions.assertFalse(a.equals(b));
        Assertions.assertFalse(a.equals(c));

        Assertions.assertTrue(a.equals(d));
    }

    private void assertCollectionHashCode(final Function<PointMap<P, ?>, Collection<?>> collectionFactory) {
        // arrange
        final PointMap<P, Integer> mapA = getMap(PRECISION);
        final PointMap<P, Integer> mapB = getMap(PRECISION);
        final PointMap<P, Integer> mapC = getMap(PRECISION);
        final PointMap<P, Integer> mapD = getMap(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);

        insertPoints(pts, mapA);
        insertPoints(pts.subList(0, 2), mapB);

        insertPoints(pts, mapD);

        final Collection<?> a = collectionFactory.apply(mapA);
        final Collection<?> b = collectionFactory.apply(mapB);
        final Collection<?> c = collectionFactory.apply(mapC);
        final Collection<?> d = collectionFactory.apply(mapD);

        // act
        final int hash = a.hashCode();

        // assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());

        Assertions.assertEquals(hash, d.hashCode());
    }

    private void assertCollectionIterator(final Function<PointMap<P, ?>, Collection<?>> collectionFactory) {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);
        insertPoints(getTestPoints(1, EPS), map);

        // act/assert
        final Collection<?> coll = collectionFactory.apply(map);

        final Iterator<?> it = coll.iterator();

        Assertions.assertTrue(it.hasNext());
        Assertions.assertNotNull(it.next());

        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, () -> it.next());
    }

    private void assertCollectionIteratorRemove(final Function<PointMap<P, ?>, Collection<?>> collectionFactory) {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(1_000, EPS, new Random(10L));

        insertPoints(pts, map);

        // act
        final Collection<?> coll = collectionFactory.apply(map);

        int removed = 0;

        final Iterator<?> it = coll.iterator();
        while (it.hasNext()) {
            Assertions.assertNotNull(it.next());
            it.remove();

            ++removed;
        }

        // assert
        Assertions.assertEquals(pts.size(), removed);

        Assertions.assertEquals(0, coll.size());
        Assertions.assertTrue(coll.isEmpty());

        assertEmpty(map);
    }

    private void assertCollectionIteratorRemoveWithMultiplePasses(
            final Function<PointMap<P, ?>, Collection<?>> collectionFactory) {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final List<P> pts = getTestPoints(1_000, EPS, new Random(10L));

        insertPoints(pts, map);

        // act
        // remove the entries in two passes: one to remove the entries with even
        // values and the second to remove the remaining
        final Collection<?> coll = collectionFactory.apply(map);

        final Iterator<?> firstPass = coll.iterator();
        int removed = 0;
        int i = 0;
        while (firstPass.hasNext()) {
            Assertions.assertNotNull(firstPass.next());
            if ((++i) % 2 == 0) {
                ++removed;
                firstPass.remove();
            }
        }

        final Iterator<?> secondPass = coll.iterator();
        while (secondPass.hasNext()) {
            Assertions.assertNotNull(secondPass.next());
            secondPass.remove();
            ++removed;
        }

        // assert
        Assertions.assertEquals(pts.size(), removed);

        Assertions.assertEquals(0, coll.size());
        Assertions.assertTrue(coll.isEmpty());

        assertEmpty(map);
    }

    private void assertCollectionIteratorRemoveWithoutNext(
            final Function<PointMap<P, ?>, Collection<?>> collectionFactory) {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);

        final Collection<?> coll = collectionFactory.apply(map);

        // act/assert
        final Iterator<?> it = coll.iterator();

        Assertions.assertThrows(IllegalStateException.class, () -> it.remove());
    }

    private void assertCollectionIteratorRemoveMultipleCalls(
            final Function<PointMap<P, ?>, Collection<?>> collectionFactory) {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);
        insertPoints(getTestPoints(1, EPS), map);

        final Collection<?> coll = collectionFactory.apply(map);
        final Iterator<?> it = coll.iterator();

        // act/assert
        Assertions.assertNotNull(it.next());
        it.remove();

        Assertions.assertThrows(IllegalStateException.class, () -> it.remove());

        Assertions.assertEquals(0, map.size());
    }

    private void assertCollectionIteratorConcurrentModification(
            final Function<PointMap<P, ?>, Collection<?>> collectionFactory) {
        // arrange
        final PointMap<P, Integer> map = getMap(PRECISION);
        final List<P> pts = getTestPoints(3, EPS);

        insertPoints(pts.subList(0, 2), map);

        final Collection<?> coll = collectionFactory.apply(map);
        final Iterator<?> it = coll.iterator();

        // act
        it.next();
        map.put(pts.get(2), 3);

        // assert
        Assertions.assertTrue(it.hasNext());
        Assertions.assertThrows(ConcurrentModificationException.class, () -> it.next());
    }

    /** Assert that {@code collection} returns entries with the same keys in the same order as
     * {@code list} when sorted with {@code cmp}.
     * @param <P> Point type
     * @param <V> Value type
     * @param list expected key list
     * @param cmp comparator producing the expected order
     * @param iterable iterable to test
     */
    public static <P extends Point<P>, V> void assertCollectionOrder(
            final List<P> list,
            final Comparator<P> cmp,
            final Collection<Map.Entry<P, V>> collection) {

        Assertions.assertEquals(list.size(), collection.size(), "Unexpected collection size");

        final List<P> expected = new ArrayList<>(list);
        Collections.sort(expected, cmp);

        int i = -1;
        for (final Map.Entry<P, V> actualEntry : collection) {
            final P expectedKey = expected.get(++i);
            final P actualKey = actualEntry.getKey();

            Assertions.assertEquals(expectedKey, actualKey, "Unexpected point at index " + i);
        }

        Assertions.assertEquals(i, expected.size() - 1, "Unexpected iteration count");
    }

    /** Assert that {@code iterable} returns entries with the same keys in the same order as
     * {@code list}.
     * @param <P> Point type
     * @param <V> Value type
     * @param expected expected key list
     * @param iterable iterable to test
     */
    public static <P extends Point<P>, V> void assertIterableOrder(
            final List<P> expected,
            final Iterable<Map.Entry<P, V>> iterable) {

        int i = 0;
        for (final Map.Entry<P, V> actualEntry : iterable) {
            final P expectedKey = expected.get(i);
            final P actualKey = actualEntry.getKey();

            Assertions.assertEquals(expectedKey, actualKey, "Unexpected point at index " + i);
            ++i;
        }

        Assertions.assertEquals(i, expected.size(), "Unexpected iteration count");
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

        public PointMapChecker<P, V> expectEntry(final Map.Entry<P, V> entry) {
            return expectEntry(entry.getKey(), entry.getValue());
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

                Assertions.assertEquals(new SimpleEntry<>(expectedKey, expectedValue), map.getEntry(expectedKey),
                        () -> "Failed to get entry for key " + expectedKey);
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
            Assertions.assertEquals(expectedKeySet.isEmpty(), keySet.isEmpty(),
                    "Unexpected key set \"isEmpty\" value");

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
            Assertions.assertEquals(expectedMap.isEmpty(), entrySet.isEmpty(),
                    "Unexpected entry set \"isEmpty\" value");

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

                Assertions.assertNull(map.getEntry(key), () -> "Expected map to not contain key " + key);

                Assertions.assertFalse(map.keySet().contains(key),
                        () -> "Expected map key set to not contain " + key);

                final boolean inEntrySet = map.entrySet().stream()
                        .anyMatch(e -> e.getKey().equals(key));
                Assertions.assertFalse(inEntrySet, () -> "Expected map entry set to not contain key " + key);
            }
        }
    }
}
