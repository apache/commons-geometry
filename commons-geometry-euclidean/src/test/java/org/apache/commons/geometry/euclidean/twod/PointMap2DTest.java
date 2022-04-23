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
package org.apache.commons.geometry.euclidean.twod;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.core.collection.PointMapTestBase;
import org.apache.commons.geometry.euclidean.EuclideanCollections;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.numbers.core.Precision.DoubleEquivalence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PointMap2DTest extends PointMapTestBase<Vector2D> {

    @Test
    void testDenseGrid() {
        // arrange
        final PointMap<Vector2D, Integer> map = getMap(PRECISION);

        final double step = 3 * EPS;
        final int stepsPerHalfSide = 100;
        final double max = step * stepsPerHalfSide;
        final int sideLength = (2 * stepsPerHalfSide) + 1;

        // act
        EuclideanTestUtils.permute(-max, max, step,
                (x, y, z) -> map.put(Vector2D.of(x, y), 0));

        // act
        assertEquals(sideLength * sideLength, map.size());

        final double offset = 0.9 * EPS;
        EuclideanTestUtils.permute(-max, max, step, (x, y) -> {
            Assertions.assertEquals(0, map.get(Vector2D.of(x + offset, y + offset)));
        });
    }

    @Test
    void testDenseLine() {
        // arrange
        final PointMap<Vector2D, Integer> map = getMap(PRECISION);

        final double step = 1.1 * EPS;
        final double start = -1.0;
        final int cnt = 10_000;

        // act
        double x = start;
        for (int i = 0; i < cnt; ++i) {
            map.put(Vector2D.of(x, 0), 0);

            x += step;
        }

        // act
        assertEquals(cnt, map.size());

        final double offset = 0.9 * EPS;
        x = start;
        for (int i = 0; i < cnt; ++i) {
            Assertions.assertEquals(0, map.get(Vector2D.of(x + offset, 0)));

            x += step;
        }
    }

    @Test
    void testNearesAndFarthesttEntry_equalDistances() {
        // arrange
        final PointMap<Vector2D, Integer> map = getMap(PRECISION);

        int i = 0;

        map.put(Vector2D.Unit.MINUS_Y, ++i);
        map.put(Vector2D.Unit.PLUS_Y, ++i);

        map.put(Vector2D.Unit.MINUS_X, ++i);
        map.put(Vector2D.Unit.PLUS_X, ++i);

        // act/assert
        Assertions.assertEquals(new SimpleEntry<>(Vector2D.Unit.MINUS_X, 3), map.nearestEntry(Vector2D.ZERO));
        Assertions.assertEquals(new SimpleEntry<>(Vector2D.Unit.PLUS_X, 4), map.farthestEntry(Vector2D.ZERO));
    }

    @Test
    void testEntryDistanceOrder_random() {
        // arrange
        final PointMap<Vector2D, Integer> map = getMap(PRECISION);

        final Random rnd = new Random(3L);
        final int testCnt = 5;
        final int pointCnt = 1_000;
        final double range = 1e10;

        final List<Vector2D> pts = new ArrayList<>();

        for (int i = 0; i < pointCnt; ++i) {
            final Vector2D pt = randomPoint(rnd, range);
            pts.add(pt);

            map.put(pt, i);
        }

        // act/assert
        for (int i = 0; i < testCnt; ++i) {
            final Vector2D refPt = randomPoint(rnd, range);

            assertCollectionOrder(
                    pts,
                    createNearToFarComparator(refPt),
                    map.entriesNearToFar(refPt));

            assertCollectionOrder(
                    pts,
                    createFarToNearComparator(refPt),
                    map.entriesFarToNear(refPt));
        }
    }

    /** {@inheritDoc} */
    @Override
    protected <V> PointMap<Vector2D, V> getMap(final DoubleEquivalence precision) {
        return EuclideanCollections.pointMap2D(precision);
    }

    /** {@inheritDoc} */
    @Override
    protected Vector2D[] createPointArray() {
        return new Vector2D[0];
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector2D> getNaNPoints() {
        return Arrays.asList(
                Vector2D.NaN,
                Vector2D.of(Double.NaN, 0),
                Vector2D.of(0, Double.NaN));
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector2D> getInfPoints() {
        return Arrays.asList(
                Vector2D.NEGATIVE_INFINITY,
                Vector2D.POSITIVE_INFINITY,

                Vector2D.of(Double.NEGATIVE_INFINITY, 0),
                Vector2D.of(0, Double.NEGATIVE_INFINITY),

                Vector2D.of(Double.POSITIVE_INFINITY, 0),
                Vector2D.of(0, Double.POSITIVE_INFINITY));
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector2D> getTestPoints(final int cnt, final double eps) {
        final List<Vector2D> pts = new ArrayList<>(cnt);

        final double delta = 10 * eps;

        double x = 0.0;
        double y = 0.0;
        for (int i = 0; i < cnt; ++i) {

            pts.add(Vector2D.of(x, y));

            final int m = i % 2;
            if (m == 0) {
                x += delta;
            } else {
                y += delta;
            }
        }

        return pts;
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector2D> getTestPointsAtDistance(final Vector2D pt, final double dist) {
        final double x = pt.getX();
        final double y = pt.getY();

        return Arrays.asList(
                Vector2D.of(x - dist, y),
                Vector2D.of(x + dist, y),

                Vector2D.of(x, y - dist),
                Vector2D.of(x, y + dist));
    }

    /** {@inheritDoc} */
    @Override
    protected boolean eq(final Vector2D a, final Vector2D b, final Precision.DoubleEquivalence precision) {
        return a.eq(b, precision);
    }

    /** {@inheritDoc} */
    @Override
    protected int disambiguateNearToFarOrder(final Vector2D a, final Vector2D b) {
        return Vector2D.COORDINATE_ASCENDING_ORDER.compare(a, b);
    }

    /** Create a random point with coordinate values in the range {@code [-range/2, range/2)}.
     * @param rnd random source
     * @param range distance between min and max coordinate values
     * @return random point
     */
    private static Vector2D randomPoint(final Random rnd, final double range) {
        return Vector2D.of(
                (rnd.nextDouble() - 0.5) * range,
                (rnd.nextDouble() - 0.5) * range);
    }
}
