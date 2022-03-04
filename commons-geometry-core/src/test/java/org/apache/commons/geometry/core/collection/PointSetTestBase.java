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
import java.util.List;
import java.util.Random;

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
        Assertions.assertNull(set.resolve(pt));
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
    void testResolve_equivalentPoints_singleEntry() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(1, EPS);
        set.addAll(pts);

        final P pt = pts.get(0);

        // act/assert
        Assertions.assertTrue(set.contains(pt));
        Assertions.assertEquals(pt, set.resolve(pt));

        for (final P closePt : getTestPointsAtDistance(pt, EPS * 0.75)) {
            Assertions.assertTrue(set.contains(closePt));
            Assertions.assertEquals(pt, set.resolve(closePt));
        }

        for (final P farPt : getTestPointsAtDistance(pt, EPS * 1.25)) {
            Assertions.assertFalse(set.contains(farPt));
            Assertions.assertNull(set.resolve(farPt));
        }
    }

    @Test
    void testGet_equivalentPoints_multipleEntries() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);

        final List<P> pts = getTestPoints(1_000, 3 * EPS);
        set.addAll(pts);

        // act/assert
        for (final P pt : pts) {
            Assertions.assertTrue(set.contains(pt));
            Assertions.assertEquals(pt, set.resolve(pt));

            for (final P closePt : getTestPointsAtDistance(pt, EPS * 0.75)) {
                Assertions.assertTrue(set.contains(closePt));
                Assertions.assertEquals(pt, set.resolve(closePt));
            }

            for (final P farPt : getTestPointsAtDistance(pt, EPS * 1.25)) {
                Assertions.assertFalse(set.contains(farPt));
                Assertions.assertNull(set.resolve(farPt));
            }
        }
    }

    @Test
    void testResolve_invalidArgs() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);;

        set.addAll(getTestPoints(3, EPS));

        // act/assert
        Assertions.assertThrows(NullPointerException.class, () -> set.resolve(null));
        Assertions.assertThrows(ClassCastException.class, () -> set.contains(new Object()));
    }

    @Test
    void testResolve_nanAndInf() {
        // arrange
        final PointSet<P> set = getSet(PRECISION);
        set.addAll(getTestPoints(100, EPS));

        // act/assert
        for (final P pt : getNaNPoints()) {
            Assertions.assertFalse(set.contains(pt));
            Assertions.assertNull(set.resolve(pt));
        }

        for (final P pt : getInfPoints()) {
            Assertions.assertFalse(set.contains(pt));
            Assertions.assertNull(set.resolve(pt));
        }
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
        Assertions.assertFalse(a.equals(null));
        Assertions.assertFalse(a.equals(new Object()));

        Assertions.assertTrue(a.equals(a));

        Assertions.assertFalse(a.equals(b));
        Assertions.assertFalse(a.equals(c));

        Assertions.assertTrue(a.equals(d));
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
                Assertions.assertEquals(value, set.resolve(value), () -> "Expected set to resolve value " + value);
            }
        }

        private void checkUnexpectedValues() {
            for (final P value : unexpected) {
                Assertions.assertFalse(set.contains(value), () -> "Expected set to not contain value " + value);
                Assertions.assertNull(set.resolve(value), () -> "Expected set to not resolve value " + value);
            }
        }
    }
}
