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
package org.apache.commons.geometry.euclidean.threed;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PointMap3DTest extends PointMapTestBase<Vector3D> {

    @Test
    void testDenseGrid() {
        // arrange
        final PointMap<Vector3D, Integer> map = getMap(PRECISION);

        final double step = 3 * EPS;
        final int stepsPerHalfSide = 50;
        final double max = step * stepsPerHalfSide;
        final int sideLength = (2 * stepsPerHalfSide) + 1;

        // act
        EuclideanTestUtils.permute(-max, max, step,
                (x, y, z) -> map.put(Vector3D.of(x, y, z), 0));

        // act
        assertEquals(sideLength * sideLength * sideLength, map.size());

        final double offset = 0.9 * EPS;
        EuclideanTestUtils.permute(-max, max, step, (x, y, z) -> {
            Assertions.assertEquals(0, map.get(Vector3D.of(x + offset, y + offset, z + offset)));
        });
    }

    @Test
    void testDenseLine() {
        // arrange
        final PointMap<Vector3D, Integer> map = getMap(PRECISION);

        final double step = 1.1 * EPS;
        final double start = -1.0;
        final int cnt = 10_000;

        // act
        double x = start;
        for (int i = 0; i < cnt; ++i) {
            map.put(Vector3D.of(x, 0, 0), 0);

            x += step;
        }

        // act
        assertEquals(cnt, map.size());

        final double offset = 0.9 * EPS;
        x = start;
        for (int i = 0; i < cnt; ++i) {
            Assertions.assertEquals(0, map.get(Vector3D.of(x + offset, 0, 0)));

            x += step;
        }
    }

    @Test
    void testNearesAndFarthesttEntry_equalDistances() {
        // arrange
        final PointMap<Vector3D, Integer> map = getMap(PRECISION);

        int i = 0;

        map.put(Vector3D.Unit.MINUS_Z, ++i);
        map.put(Vector3D.Unit.PLUS_Z, ++i);

        map.put(Vector3D.Unit.MINUS_X, ++i);
        map.put(Vector3D.Unit.PLUS_X, ++i);

        map.put(Vector3D.Unit.MINUS_Y, ++i);
        map.put(Vector3D.Unit.PLUS_Y, ++i);

        // act/assert
        Assertions.assertEquals(new SimpleEntry<>(Vector3D.Unit.MINUS_X, 3), map.nearestEntry(Vector3D.ZERO));
        Assertions.assertEquals(new SimpleEntry<>(Vector3D.Unit.PLUS_X, 4), map.farthestEntry(Vector3D.ZERO));
    }

    @Test
    void testEntryDistanceOrder_random() {
        // arrange
        final PointMap<Vector3D, Integer> map = getMap(PRECISION);

        final Random rnd = new Random(2L);
        final int testCnt = 5;
        final int pointCnt = 1_000;
        final double range = 1e10;

        final List<Vector3D> pts = new ArrayList<>();

        for (int i = 0; i < pointCnt; ++i) {
            final Vector3D pt = randomPoint(rnd, range);
            pts.add(pt);

            map.put(pt, i);
        }

        // act/assert
        for (int i = 0; i < testCnt; ++i) {
            final Vector3D refPt = randomPoint(rnd, range);

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
    protected <V> PointMap<Vector3D, V> getMap(final Precision.DoubleEquivalence precision) {
        return EuclideanCollections.pointMap3D(precision);
    }

    /** {@inheritDoc} */
    @Override
    protected Vector3D[] createPointArray() {
        return new Vector3D[0];
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector3D> getNaNPoints() {
        return Arrays.asList(
                Vector3D.NaN,
                Vector3D.of(Double.NaN, 0, 0),
                Vector3D.of(0, Double.NaN, 0),
                Vector3D.of(0, 0, Double.NaN));
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector3D> getInfPoints() {
        return Arrays.asList(
                Vector3D.NEGATIVE_INFINITY,
                Vector3D.POSITIVE_INFINITY,

                Vector3D.of(Double.NEGATIVE_INFINITY, 0, 0),
                Vector3D.of(0, Double.NEGATIVE_INFINITY, 0),
                Vector3D.of(0, 0, Double.NEGATIVE_INFINITY),

                Vector3D.of(Double.POSITIVE_INFINITY, 0, 0),
                Vector3D.of(0, Double.POSITIVE_INFINITY, 0),
                Vector3D.of(0, 0, Double.POSITIVE_INFINITY));
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector3D> getTestPoints(final int cnt, final double eps) {
        final List<Vector3D> pts = new ArrayList<>(cnt);

        final double delta = 10 * eps;

        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        for (int i = 0; i < cnt; ++i) {

            pts.add(Vector3D.of(x, y, z));

            final int m = i % 3;
            if (m == 0) {
                x += delta;
            } else if (m == 1) {
                y += delta;
            } else {
                z += delta;
            }
        }

        return pts;
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector3D> getTestPointsAtDistance(final Vector3D pt, final double dist) {
        final double x = pt.getX();
        final double y = pt.getY();
        final double z = pt.getZ();

        return Arrays.asList(
                Vector3D.of(x - dist, y, z),
                Vector3D.of(x + dist, y, z),

                Vector3D.of(x, y - dist, z),
                Vector3D.of(x, y + dist, z),

                Vector3D.of(x, y, z - dist),
                Vector3D.of(x, y, z + dist));
    }

    /** {@inheritDoc} */
    @Override
    protected boolean eq(final Vector3D a, final Vector3D b, final Precision.DoubleEquivalence precision) {
        return a.eq(b, precision);
    }

    /** {@inheritDoc} */
    @Override
    protected int disambiguateNearToFarOrder(final Vector3D a, final Vector3D b) {
        return Vector3D.COORDINATE_ASCENDING_ORDER.compare(a, b);
    }

    /** Create a random point with coordinate values in the range {@code [-range/2, range/2)}.
     * @param rnd random source
     * @param range distance between min and max coordinate values
     * @return random point
     */
    private static Vector3D randomPoint(final Random rnd, final double range) {
        return Vector3D.of(
                (rnd.nextDouble() - 0.5) * range,
                (rnd.nextDouble() - 0.5) * range,
                (rnd.nextDouble() - 0.5) * range);
    }
}
