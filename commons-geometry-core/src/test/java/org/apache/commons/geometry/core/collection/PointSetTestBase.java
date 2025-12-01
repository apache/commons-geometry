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
package org.apache.commons.geometry.core.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Point;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Abstract base class for {@link PointSet} implementations.
 * @param <P> Point type
 */
public abstract class PointSetTestBase<P extends Point<P>>
    extends PointCollectionTestBase<P> {

    /** Get a new point set instance for testing.
     * @param <V> Value type
     * @param precision precision context to determine floating point equality
     * @return a new set instance for testing.
     */
    protected abstract PointSet<P> getSet(Precision.DoubleEquivalence precision);

    @Test
    void testEmpty() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);

        // act/assert
        Assertions.assertEquals(0, set.size());
        Assertions.assertTrue(set.isEmpty());

        Assertions.assertFalse(set.contains(pt));
        Assertions.assertNull(set.get(pt));
    }

    @Test
    void testSingleEntry() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);
        final P a = pts.get(0);

        // act
        set.add(a);

        // assert
        checkerFor(set)
            .expect(a)
            .doesNotContain(pts.subList(1, pts.size()))
            .check();
    }

    @Test
    void testMultipleEntries() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final PointSetChecker<P> checker = checkerFor(set);

        final int putCnt = 1000;
        final List<P> pts = getTestPoints(putCnt * 2, EPS, new Random(1L));

        // act
        for (int i = 0; i < putCnt; ++i) {
            final P pt = pts.get(i);

            set.add(pt);

            checker.expect(pt);
        }

        // assert
        checker
            .doesNotContain(pts.subList(putCnt, pts.size()))
            .check();
    }

    @Test
    void testAdd() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(500, EPS);

        // act/assert
        for (final P pt : pts) {
            Assertions.assertTrue(set.add(pt));
            Assertions.assertFalse(set.add(pt));

            for (final P closePt : getTestPointsAtDistance(pt, 0.5 * EPS)) {
                Assertions.assertFalse(set.add(closePt));
            }
        }

        checkerFor(set)
            .expect(pts)
            .check();
    }

    @Test
    void testContainsGet_equivalentPoints_singleEntry() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(1, EPS);
        set.addAll(pts);

        final P pt = pts.get(0);

        // act/assert
        Assertions.assertTrue(set.contains(pt));
        Assertions.assertEquals(pt, set.get(pt));

        for (final P closePt : getTestPointsAtDistance(pt, EPS * 0.75)) {
            Assertions.assertTrue(set.contains(closePt));
            Assertions.assertEquals(pt, set.get(closePt));

            assertEq(closePt, pt, PRECISION);
        }

        for (final P farPt : getTestPointsAtDistance(pt, EPS * 1.25)) {
            Assertions.assertFalse(set.contains(farPt));
            Assertions.assertNull(set.get(farPt));

            assertNotEq(farPt, pt, PRECISION);
        }
    }

    @Test
    void testContainsGet_equivalentPoints_multipleEntries() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(1_000, 3 * EPS);
        set.addAll(pts);

        // act/assert
        for (final P pt : pts) {
            Assertions.assertTrue(set.contains(pt));
            Assertions.assertEquals(pt, set.get(pt));

            for (final P closePt : getTestPointsAtDistance(pt, EPS * 0.75)) {
                Assertions.assertTrue(set.contains(closePt));
                Assertions.assertEquals(pt, set.get(closePt));

                assertEq(closePt, pt, PRECISION);
            }

            for (final P farPt : getTestPointsAtDistance(pt, EPS * 1.25)) {
                Assertions.assertFalse(set.contains(farPt));
                Assertions.assertNull(set.get(farPt));

                assertNotEq(farPt, pt, PRECISION);
            }
        }
    }

    @Test
    void testContainsGet_invalidArgs() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        set.addAll(getTestPoints(3, EPS));

        final Object objKey = new Object();

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> set.get(null));
        Assertions.assertThrows(ClassCastException.class, () -> set.contains(objKey));
    }

    @Test
    void testContainsGet_nanAndInf() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);
        set.addAll(getTestPoints(100, EPS));

        // act/assert
        for (final P pt : getNaNPoints()) {
            Assertions.assertFalse(set.contains(pt));
            Assertions.assertNull(set.get(pt));
        }

        for (final P pt : getInfPoints()) {
            Assertions.assertFalse(set.contains(pt));
            Assertions.assertNull(set.get(pt));
        }
    }

    @Test
    void testContainsAll() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(1_000, 3 * EPS);
        final List<P> addedPts = pts.subList(0, pts.size() / 2);

        set.addAll(addedPts);

        final P a = pts.get(0);
        final P b = pts.get(1);
        final P c = pts.get(pts.size() - 1);
        final P d = pts.get(pts.size() - 2);

        // act/assert
        Assertions.assertTrue(set.containsAll(Arrays.asList()));
        Assertions.assertTrue(set.containsAll(Arrays.asList(a)));
        Assertions.assertTrue(set.containsAll(Arrays.asList(b)));
        Assertions.assertTrue(set.containsAll(addedPts));

        Assertions.assertFalse(set.containsAll(Arrays.asList(c)));
        Assertions.assertFalse(set.containsAll(Arrays.asList(c, d)));
        Assertions.assertFalse(set.containsAll(Arrays.asList(a, b, c, d)));
    }

    @Test
    void testContainsAll_empty() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(10, 3 * EPS);

        final P a = pts.get(0);
        final P b = pts.get(1);

        // act/assert
        Assertions.assertTrue(set.containsAll(Arrays.asList()));

        Assertions.assertFalse(set.containsAll(Arrays.asList(a, b)));
        Assertions.assertFalse(set.containsAll(pts));
    }

    @Test
    void testRemove() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(4, EPS);
        final P a = pts.get(0);
        final P b = pts.get(1);
        final P c = pts.get(2);
        final P d = pts.get(3);

        set.add(a);
        set.add(b);
        set.add(c);

        // act/assert
        Assertions.assertFalse(set.remove(d));
        Assertions.assertTrue(set.remove(a));
        Assertions.assertTrue(set.remove(b));
        Assertions.assertTrue(set.remove(c));

        Assertions.assertFalse(set.remove(a));
        Assertions.assertFalse(set.remove(b));
        Assertions.assertFalse(set.remove(c));
        Assertions.assertFalse(set.remove(d));

        checkerFor(set)
            .doesNotContain(pts)
            .check();
    }

    @Test
    void testRemove_largeEntryCount() {
        // -- arrange
        final PointSet<P> set = getSet(PRECISION);

        final Random rnd = new Random(2L);

        final int cnt = 10_000;
        final List<P> pts = getTestPoints(cnt * 2, EPS, rnd);

        final List<P> testPts = new ArrayList<>(pts.subList(0, cnt));
        final List<P> otherPts = new ArrayList<>(pts.subList(cnt, pts.size()));

        // -- act/assert
        // insert the test points
        final PointSetChecker<P> allChecker = checkerFor(set);
        final PointSetChecker<P> oddChecker = checkerFor(set);

        final List<P> evenPts = new ArrayList<>();
        final List<P> oddPts = new ArrayList<>();

        for (int i = 0; i < cnt; ++i) {
            final P pt = testPts.get(i);

            Assertions.assertTrue(set.add(pt));

            allChecker.expect(pt);

            if (i % 2 == 0) {
                evenPts.add(pt);
            } else {
                oddPts.add(pt);
                oddChecker.expect(pt);
            }
        }

        // check state after insertion of all test points
        allChecker
            .doesNotContain(otherPts)
            .check();

        // remove points inserted on even indices; remove the values in
        // a different order than insertion
        Collections.shuffle(evenPts);
        for (final P pt : evenPts) {
            Assertions.assertTrue(set.remove(pt));
        }

        // check state after partial removal
        oddChecker
            .doesNotContain(otherPts)
            .doesNotContain(evenPts)
            .check();

        // remove remaining points
        Collections.shuffle(oddPts);
        for (final P pt : oddPts) {
            Assertions.assertTrue(set.remove(pt));
        }

        // ensure that nothing is left
        assertEmpty(set);
    }

    @Test
    void testRemoveAll() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final int size = 500;

        final List<P> pts = getTestPoints(size * 2, 3 * EPS);
        final List<P> addedPts = pts.subList(0, size);

        set.addAll(addedPts);

        final P a = pts.get(0);
        final P b = pts.get(1);
        final P c = pts.get(3);
        final P d = pts.get(pts.size() - 1);
        final P e = pts.get(pts.size() - 2);

        // act/assert
        Assertions.assertFalse(set.removeAll(Arrays.asList()));
        Assertions.assertEquals(size, set.size());

        Assertions.assertTrue(set.removeAll(Arrays.asList(a)));
        Assertions.assertEquals(size - 1, set.size());

        Assertions.assertTrue(set.removeAll(Arrays.asList(a, b, c)));
        Assertions.assertEquals(size - 3, set.size());

        Assertions.assertFalse(set.removeAll(Arrays.asList(a, b, c)));
        Assertions.assertEquals(size - 3, set.size());

        Assertions.assertFalse(set.removeAll(Arrays.asList(d, e)));
        Assertions.assertEquals(size - 3, set.size());

        Assertions.assertTrue(set.removeAll(addedPts));
        assertEmpty(set);
    }

    @Test
    void testRemoveAll_empty() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(10, 3 * EPS);

        final P a = pts.get(0);
        final P b = pts.get(1);

        // act/assert
        Assertions.assertFalse(set.removeAll(Arrays.asList()));

        Assertions.assertFalse(set.removeAll(Arrays.asList(a, b)));
        Assertions.assertFalse(set.removeAll(pts));
    }

    @Test
    void testRetainAll() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final int size = 500;

        final List<P> pts = getTestPoints(size * 2, 3 * EPS);
        final List<P> addedPts = pts.subList(0, size);

        set.addAll(addedPts);

        final P a = pts.get(0);
        final P b = pts.get(1);
        final P c = pts.get(3);
        final P d = pts.get(pts.size() - 1);
        final P e = pts.get(pts.size() - 2);

        // act/assert
        Assertions.assertFalse(set.retainAll(addedPts));
        Assertions.assertEquals(size, set.size());

        Assertions.assertTrue(set.retainAll(Arrays.asList(a, b, c)));
        Assertions.assertEquals(3, set.size());

        Assertions.assertTrue(set.retainAll(Arrays.asList(d, e)));
        assertEmpty(set);

        Assertions.assertFalse(set.retainAll(Arrays.asList()));
        assertEmpty(set);
    }

    @Test
    void testRetainAll_empty() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(10, 3 * EPS);

        final P a = pts.get(0);
        final P b = pts.get(1);

        // act/assert
        Assertions.assertFalse(set.retainAll(Arrays.asList()));

        Assertions.assertFalse(set.retainAll(Arrays.asList(a, b)));
        Assertions.assertFalse(set.retainAll(pts));
    }

    @Test
    void testClear_empty() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        // act
        set.clear();

        // assert
        assertEmpty(set);
    }

    @Test
    void testClear_populated() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);
        set.addAll(getTestPoints(1_000, EPS, new Random(6L)));

        // act
        set.clear();

        // assert
        assertEmpty(set);
    }

    @Test
    void testRepeatedUse() {
        // -- arrange
        final PointSet<P> set = getSet(PRECISION);

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
            set.addAll(subList);

            // remove sublist in different order
            final List<P> shuffledSubList = new ArrayList<>(subList);
            Collections.shuffle(shuffledSubList, rnd);

            for (final P pt : shuffledSubList) {
                Assertions.assertTrue(set.remove(pt));
            }

            // add sublist again
            set.addAll(subList);
        }

        // -- assert
        checkerFor(set)
                .expect(pts)
                .check();
    }

    @Test
    void testIterator() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final Set<P> testPts = new HashSet<>(getTestPoints(1_000, 2 * EPS));
        set.addAll(testPts);

        // act/assert
        final Iterator<P> it = set.iterator();
        while (it.hasNext()) {
            final P pt = it.next();

            Assertions.assertTrue(testPts.remove(pt), () -> "Unexpected iterator point " + pt);
        }

        Assertions.assertEquals(0, testPts.size(), "Expected iterator to visit all points");

        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void testIterator_empty() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        // act
        final Iterator<P> it = set.iterator();

        // assert
        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void testIterator_remove() {
        // --- arrange
        final PointSet<P> set = getSet(PRECISION);

        final int size = 1_000;
        final Set<P> testPts = new HashSet<>(getTestPoints(size, 2 * EPS));

        set.addAll(testPts);

        // --- act/assert
        final Iterator<P> it = set.iterator();
        while (it.hasNext()) {
            final P pt = it.next();

            it.remove();
            Assertions.assertTrue(testPts.remove(pt), () -> "Unexpected iterator point " + pt);
        }

        Assertions.assertEquals(0, testPts.size(), "Expected iterator to visit all points");

        assertEmpty(set);
    }

    @Test
    void testIterator_remove_multiplePasses() {
        // --- arrange
        final PointSet<P> set = getSet(PRECISION);

        final int size = 1_000;
        final Set<P> testPts = new HashSet<>(getTestPoints(size, 2 * EPS));

        set.addAll(testPts);

        // --- act/assert
        // remove the points in two passes
        final Iterator<P> firstIt = set.iterator();

        int i = -1;
        while (firstIt.hasNext()) {
            final P pt = firstIt.next();

            if ((++i) % 2 == 0) {
                firstIt.remove();
                Assertions.assertTrue(testPts.remove(pt), () -> "Unexpected iterator point " + pt);
            }
        }

        Assertions.assertEquals(size / 2, set.size());

        final Iterator<P> secondIt = set.iterator();
        while (secondIt.hasNext()) {
            final P pt = secondIt.next();

            secondIt.remove();

            Assertions.assertTrue(testPts.remove(pt), () -> "Unexpected iterator point " + pt);
        }

        Assertions.assertEquals(0, testPts.size(), "Expected iterator to visit all points");

        assertEmpty(set);
    }

    @Test
    void testToArray() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);
        set.addAll(pts);

        // act
        final Object[] objArr = set.toArray();
        final P[] typedArr = set.toArray(createPointArray());

        // assert
        Assertions.assertEquals(set.size(), objArr.length);
        Assertions.assertEquals(set.size(), typedArr.length);

        int i = 0;
        for (final P element : set) {
            Assertions.assertEquals(element, objArr[i]);
            Assertions.assertEquals(element, typedArr[i]);

            ++i;
        }
    }

    @Test
    void testNearest_empty() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);

        // act/assert
        Assertions.assertNull(set.nearest(pt));
    }

    @Test
    void testNearest_small() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final double keySpacing = 7 * EPS;
        final double testPointSpacing = 3 * EPS;

        int maxCnt = 5;
        for (int cnt = 1; cnt <= maxCnt; ++cnt) {
            final List<P> pts = getTestPoints(cnt, keySpacing, new Random(cnt));

            set.clear();
            set.addAll(pts);

            // act/ assert
            for (int i = 0; i < cnt; ++i) {
                final P pt = pts.get(i);

                for (final P refPt : getTestPointsAtDistance(pt, testPointSpacing)) {
                    Assertions.assertNull(set.get(refPt));

                    final P nearest = set.nearest(refPt);

                    Assertions.assertEquals(pt, nearest);
                }
            }
        }
    }

    @Test
    void testNearest_large() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final double keySpacing = 7 * EPS;
        final double testPointSpacing = 3 * EPS;

        final int cnt = 1000;
        final List<P> pts = getTestPoints(cnt, keySpacing, new Random(5L));
        set.addAll(pts);

        // act/ assert
        for (int i = 0; i < cnt; ++i) {
            final P pt = pts.get(i);

            for (final P refPt : getTestPointsAtDistance(pt, testPointSpacing)) {
                Assertions.assertNull(set.get(refPt));

                final P nearest = set.nearest(refPt);

                Assertions.assertEquals(pt, nearest);
            }
        }
    }

    @Test
    void testNearest_invalidArgs() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);
        final P infPt = getInfPoints().get(0);

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> set.nearest(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> set.nearest(infPt));
    }

    @Test
    void testFarthest_empty() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);

        // act/assert
        Assertions.assertNull(set.farthest(pt));
    }

    @Test
    void testFarthest_small() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final double keySpacing = 7 * EPS;
        final double testPointSpacing = 3 * EPS;

        int maxCnt = 5;
        for (int cnt = 1; cnt <= maxCnt; ++cnt) {
            final List<P> pts = getTestPoints(cnt, keySpacing, new Random(cnt));

            set.clear();
            set.addAll(pts);

            // act/ assert
            for (int i = 0; i < cnt; ++i) {
                final P pt = pts.get(i);

                for (final P refPt : getTestPointsAtDistance(pt, testPointSpacing)) {
                    Assertions.assertNull(set.get(refPt));

                    final P farthest = set.farthest(refPt);

                    Assertions.assertEquals(findFarthest(refPt, pts), farthest);
                }
            }
        }
    }

    @Test
    void testFarthest_large() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final double keySpacing = 7 * EPS;
        final double testPointSpacing = 3 * EPS;

        final int cnt = 1000;
        final List<P> pts = getTestPoints(cnt, keySpacing, new Random(5L));
        set.addAll(pts);

        // act/ assert
        for (int i = 0; i < cnt; ++i) {
            final P pt = pts.get(i);

            for (final P refPt : getTestPointsAtDistance(pt, testPointSpacing)) {
                Assertions.assertNull(set.get(refPt));

                final P farthest = set.farthest(refPt);

                Assertions.assertEquals(findFarthest(refPt, pts), farthest);
            }
        }
    }

    @Test
    void testFarthest_invalidArgs() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);
        final P infPt = getInfPoints().get(0);

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> set.farthest(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> set.farthest(infPt));
    }

    @Test
    void testNearToFar_empty() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);
        final List<P> ordered = new ArrayList<>();

        // act
        for (final P nearest : set.nearToFar(pt)) {
            ordered.add(nearest);
        }

        // assert
        Assertions.assertEquals(0, ordered.size());
    }

    @Test
    void testNearToFar_small() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final int maxCnt = 5;
        for (int cnt = 1; cnt <= maxCnt; ++cnt) {
            final List<P> pts = getTestPoints(cnt, EPS, new Random(cnt));

            set.clear();
            set.addAll(pts);

            // act/ assert
            for (int i = 0; i < cnt; ++i) {
                for (final P refPt : getTestPointsAtDistance(pts.get(i), 2 * EPS)) {

                    assertCollectionOrder(
                            pts,
                            createNearToFarComparator(refPt),
                            set.nearToFar(refPt));
                }
            }
        }
    }

    @Test
    void testNearToFar_large() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        int cnt = 1000;
        final List<P> pts = getTestPoints(cnt, EPS, new Random(5L));
        set.addAll(pts);

        // act/ assert
        for (int i = 0; i < cnt; ++i) {
            for (final P refPt : getTestPointsAtDistance(pts.get(i), 2 * EPS)) {
                assertCollectionOrder(
                        pts,
                        createNearToFarComparator(refPt),
                        set.nearToFar(refPt));
            }
        }
    }

    @Test
    void testNearToFar_iteratorBehavior() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(2, EPS);
        set.addAll(pts);

        // act/assert
        final Iterator<P> it = set.nearToFar(pts.get(0)).iterator();
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals(pts.get(0), it.next());
        Assertions.assertThrows(UnsupportedOperationException.class, it::remove);

        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals(pts.get(1), it.next());

        Assertions.assertFalse(it.hasNext());

        Assertions.assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void testNearToFar_concurrentModification() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(2, EPS);
        set.addAll(pts);

        // act
        final Iterator<P> it = set.nearToFar(pts.get(0)).iterator();
        set.remove(pts.get(0));

        // assert
        Assertions.assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void testNearToFar_invalidArgs() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);
        final P infPt = getInfPoints().get(0);

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> set.nearToFar(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> set.nearToFar(infPt));
    }

    @Test
    void testFarToNear_empty() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final P pt = getTestPoints(1, EPS).get(0);
        final List<P> ordered = new ArrayList<>();

        // act
        for (final P element : set.farToNear(pt)) {
            ordered.add(element);
        }

        // assert
        Assertions.assertEquals(0, ordered.size());
    }

    @Test
    void testFarToNear_small() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        int maxCnt = 5;
        for (int cnt = 1; cnt <= maxCnt; ++cnt) {
            final List<P> pts = getTestPoints(cnt, EPS, new Random(cnt));

            set.clear();
            set.addAll(pts);

            // act/ assert
            for (int i = 0; i < cnt; ++i) {
                for (final P refPt : getTestPointsAtDistance(pts.get(i), 2.1 * EPS)) {
                    assertCollectionOrder(
                            pts,
                            createFarToNearComparator(refPt),
                            set.farToNear(refPt));
                }
            }
        }
    }

    @Test
    void testFarToNear_large() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        int cnt = 1000;
        final List<P> pts = getTestPoints(cnt, EPS, new Random(6L));
        set.addAll(pts);

        // act/ assert
        for (int i = 0; i < cnt; ++i) {
            for (final P refPt : getTestPointsAtDistance(pts.get(i), 2.1 * EPS)) {
                assertCollectionOrder(
                        pts,
                        createFarToNearComparator(refPt),
                        set.farToNear(refPt));
            }
        }
    }

    @Test
    void testFarToNear_iteratorBehavior() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(2, EPS);
        set.addAll(pts);

        // act/assert
        final Iterator<P> it = set.farToNear(pts.get(0)).iterator();
        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals(pts.get(1), it.next());
        Assertions.assertThrows(UnsupportedOperationException.class, it::remove);

        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals(pts.get(0), it.next());

        Assertions.assertFalse(it.hasNext());

        Assertions.assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    void testFarToNear_concurrentModification() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(2, EPS);
        set.addAll(pts);

        // act
        final Iterator<P> it = set.farToNear(pts.get(0)).iterator();
        set.remove(pts.get(0));

        // assert
        Assertions.assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    void testFarToNear_invalidArgs() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);
        final P infPt = getInfPoints().get(0);

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> set.farToNear(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> set.farToNear(infPt));
    }

    @Test
    void testHashCode() {
        // arrange
        final PointSet<P> a = getSet(PRECISION);
        final PointSet<P> b = getSet(PRECISION);
        final PointSet<P> c = getSet(PRECISION);
        final PointSet<P> d = getSet(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);

        a.addAll(pts);
        b.addAll(pts.subList(0, 2));

        d.addAll(pts);

        // act
        final int hash = a.hashCode();

        // act/assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());

        Assertions.assertEquals(hash, d.hashCode());
    }

    @Test
    void testEquals() {
        // arrange
        final PointSet<P> a = getSet(PRECISION);
        final PointSet<P> b = getSet(PRECISION);
        final PointSet<P> c = getSet(PRECISION);
        final PointSet<P> d = getSet(PRECISION);

        final List<P> pts = getTestPoints(3, EPS);

        a.addAll(pts);
        b.addAll(pts.subList(0, 2));

        d.addAll(pts);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(a, c);

        Assertions.assertEquals(a, d);
    }

    @Test
    void testToString() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);
        final P pt = getTestPoints(1, EPS).get(0);

        set.add(pt);

        // act
        final String str = set.toString();

        // assert
        GeometryTestUtils.assertContains(pt.toString(), str);
    }

    /** Return a new {@link PointSetChecker} for asserting the contents
     * of the given set.
     * @param set
     * @return a new checker instance
     */
    public static <P extends Point<P>> PointSetChecker<P> checkerFor(final PointSet<P> set) {
        return new PointSetChecker<>(set);
    }

    /** Assert that the given set is empty.
     * @param set setto assert empty
     */
    public static void assertEmpty(final PointSet<?> set) {
        checkerFor(set)
            .check();
    }

    /** Assert that {@code coll} returns the same elements in the same order as
     * {@code list} when sorted with {@code cmp}.
     * @param <P> Point type
     * @param list expected point list
     * @param cmp comparator producing the expected order
     * @param coll collection to test
     */
    public static <P extends Point<P>, V> void assertCollectionOrder(
            final List<P> list,
            final Comparator<P> cmp,
            final Collection<P> coll) {

        Assertions.assertEquals(list.size(), coll.size(), "Unexpected collection size");

        final List<P> expectedList = new ArrayList<>(list);
        Collections.sort(expectedList, cmp);

        int i = 0;
        for (final P actual : coll) {
            final P expected = expectedList.get(i++);

            Assertions.assertEquals(expected, actual, "Unexpected point at index " + i);
        }

        Assertions.assertEquals(i, expectedList.size(), "Unexpected iteration count");
    }

    /** Class designed to assist with performing assertions on the state
     * of a point set.
     */
    public static class PointSetChecker<P extends Point<P>> {

        private final PointSet<P> set;

        private final List<P> expected = new ArrayList<>();

        private final List<P> unexpected = new ArrayList<>();

        public PointSetChecker(final PointSet<P> set) {
            this.set = set;
        }

        public PointSetChecker<P> expect(final P value) {
            expected.add(value);

            return this;
        }

        public PointSetChecker<P> expect(final Iterable<? extends P> values) {
            for (final P value : values) {
                expect(value);
            }

            return this;
        }

        public PointSetChecker<P> doesNotContain(final P value) {
            unexpected.add(value);

            return this;
        }

        public PointSetChecker<P> doesNotContain(final Iterable<? extends P> values) {
            for (final P value : values) {
                doesNotContain(value);
            }

            return this;
        }

        public void check() {
            checkSize();
            checkValues();
            checkUnexpectedValues();
        }

        private void checkSize() {
            Assertions.assertEquals(expected.size(), set.size(), "Unexpected set size");
            Assertions.assertEquals(expected.isEmpty(), set.isEmpty(), "Unexpected isEmpty() result");
        }

        private void checkValues() {
            Assertions.assertEquals(expected.size(), set.size(), "Unexpected size");

            for (final P value : expected) {
                Assertions.assertTrue(set.contains(value), () -> "Expected set to contain value " + value);
                Assertions.assertEquals(value, set.get(value), () -> "Expected set to contain value " + value);
            }
        }

        private void checkUnexpectedValues() {
            for (final P value : unexpected) {
                Assertions.assertFalse(set.contains(value), () -> "Expected set to not contain value " + value);
                Assertions.assertNull(set.get(value), () -> "Expected set to not contain value " + value);
            }
        }
    }
}
