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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.commons.geometry.core.Point;
import org.apache.commons.numbers.core.Precision;

/** Base test class for point collection types.
 * @param <P> Point type
 */
public abstract class PointCollectionTestBase<P extends Point<P>> {

    public static final double EPS = 1e-10;

    public static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);

    /** Create an empty array of the target point type.
     * @return empty array of the target point type
     */
    protected abstract P[] createPointArray();

    /** Get a list of points with {@code NaN} coordinates.
     * @return list of points with {@code NaN} coordinates
     */
    protected abstract List<P> getNaNPoints();

    /** Get a list of points with infinite coordinates.
     * @return list of points with infinite coordinates
     */
    protected abstract List<P> getInfPoints();

    /** Get {@code cnt} number of unique test points that differ from each other in
     * each dimension by <em>at least</em> {@code eps}.
     * @param cnt number of points to return
     * @param eps minimum value that each point must differ from other points along
     *      each dimension
     * @return list of test points
     */
    protected abstract List<P> getTestPoints(int cnt, double eps);

    /** Get a list of points that lie {@code dist} distance from {@code pt}.
     * @param pt input point
     * @param dist distance from {@code pt}
     * @return list of points that lie {@code dist} distance from {@code pt}
     */
    protected abstract List<P> getTestPointsAtDistance(P pt, double dist);

    /** Get {@code cnt} number of unique test points that differ from each other in
     * each dimension by <em>at least</em> {@code eps}. The returned list is shuffled
     * using {@code rnd}.
     * @param cnt number of points to return
     * @param eps minimum value that each point must differ from other points along
     *      each dimension
     * @param rnd random instance used to shuffle the order of the points
     * @return randomly ordered list of test points
     */
    protected List<P> getTestPoints(final int cnt, final double eps, final Random rnd) {
        final List<P> pts = new ArrayList<>(getTestPoints(cnt, eps));
        Collections.shuffle(pts, rnd);

        return pts;
    }

    /** Return true if the given points are equivalent to each other using the given precision.
     * @param a first point
     * @param b second point
     * @param precision precision context
     * @return true if the two points are equivalent when compared using the given precision
     */
    protected abstract boolean eq(P a, P b, Precision.DoubleEquivalence precision);

    /** Compare two points with equal distances computed during a "closest first" ordering.
     * @param a first point
     * @param b second point
     * @return comparison of the two points
     */
    protected abstract int disambiguateNearToFarOrder(P a, P b);

    /** Assert that {@code a} and {@code b} are equivalent using the given precision context.
     * @param a first point
     * @param b second point
     * @param precision precision context
     */
    protected void assertEq(final P a, final P b, final Precision.DoubleEquivalence precision) {
        assertTrue(eq(a, b, precision), () -> "Expected " + a + " and " + b + " to be equivalent");
    }

    /** Assert that {@code a} and {@code b} are not equivalent using the given precision context.
     * @param a first point
     * @param b second point
     * @param precision precision context
     */
    protected void assertNotEq(final P a, final P b, final Precision.DoubleEquivalence precision) {
        assertFalse(eq(a, b, precision), () -> "Expected " + a + " and " + b + " to not be equivalent");
    }

    /** Create a comparator for use in testing "near to far" ordering.
     * @param refPt reference point
     * @return comparator for use in testing "near to far" ordering
     */
    protected Comparator<P> createNearToFarComparator(final P refPt) {
        final Comparator<P> cmp = (a, b) -> Double.compare(a.distance(refPt), b.distance(refPt));
        return cmp.thenComparing(this::disambiguateNearToFarOrder);
    }

    /** Create a comparator for use in testing "far to near" ordering.
     * @param refPt reference point
     * @return comparator for use in testing "far to near" ordering
     */
    protected Comparator<P> createFarToNearComparator(final P refPt) {
        return createNearToFarComparator(refPt).reversed();
    }

    /** Find the element in {@code list} farthest away from {@code refPt}.
     * @param refPt reference point
     * @param list list to search
     * @return element in {@code list} farthest from {@code refPt}
     */
    protected P findFarthest(final P refPt, final List<P> list) {
        final Comparator<P> cmp = createFarToNearComparator(refPt);

        P result = null;
        for (final P pt : list) {
            if (result == null || cmp.compare(pt, result) < 0) {
                result = pt;
            }
        }

        return result;
    }

    /** Return the maximum distance from {@code refPt} to the points in {@code pts}.
     * @param <P> Point type
     * @param refPt reference point
     * @param pts test points
     * @return maximum distance from {@code refPt} to the points in {@code pts}
     */
    protected double findMaxDistance(final P refPt, final Collection<P> pts) {
        double maxDist = 0d;
        for (final P pt : pts) {
            final double dist = pt.distance(refPt);
            if (maxDist > dist) {
                maxDist = dist;
            }
        }

        return maxDist;
    }
}
