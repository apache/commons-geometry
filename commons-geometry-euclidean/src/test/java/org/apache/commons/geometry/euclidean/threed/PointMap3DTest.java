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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
}
