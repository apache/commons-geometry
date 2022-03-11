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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
}
