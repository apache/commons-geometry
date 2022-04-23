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
package org.apache.commons.geometry.spherical.oned;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.core.collection.PointMapTestBase;
import org.apache.commons.geometry.spherical.SphericalCollections;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PointMap1STest extends PointMapTestBase<Point1S> {

    @Test
    void testWrapLowToHigh() {
        // arrange
        final PointMap<Point1S, Integer> map = getMap(PRECISION);

        final double delta = 0.25 * EPS;

        map.put(Point1S.of(1), -1);

        final Point1S pt = Point1S.of(Angle.TWO_PI - delta);
        map.put(pt, 0);

        final Point1S testPt = Point1S.of(delta);

        // act/assert
        Assertions.assertEquals(0, map.get(testPt));
        Assertions.assertEquals(pt, map.getEntry(testPt).getKey());

        Assertions.assertEquals(0, map.put(testPt, 2));
        Assertions.assertEquals(2, map.get(testPt));
        Assertions.assertEquals(2, map.get(pt));
    }

    @Test
    void testWrapHighToLow() {
        // arrange
        final PointMap<Point1S, Integer> map = getMap(PRECISION);

        final double delta = 0.25 * EPS;

        map.put(Point1S.of(1), -1);

        final Point1S pt = Point1S.of(delta);
        map.put(pt, 0);

        final Point1S testPt = Point1S.of(Angle.TWO_PI - delta);

        // act/assert
        Assertions.assertEquals(0, map.get(testPt));
        Assertions.assertEquals(pt, map.getEntry(testPt).getKey());

        Assertions.assertEquals(0, map.put(testPt, 2));
        Assertions.assertEquals(2, map.get(testPt));
        Assertions.assertEquals(2, map.get(pt));
    }

    /** {@inheritDoc} */
    @Override
    protected <V> PointMap<Point1S, V> getMap(final Precision.DoubleEquivalence precision) {
        return SphericalCollections.pointMap1S(precision);
    }

    /** {@inheritDoc} */
    @Override
    protected Point1S[] createPointArray() {
        return new Point1S[0];
    }

    /** {@inheritDoc} */
    @Override
    protected List<Point1S> getNaNPoints() {
        return Collections.singletonList(Point1S.NaN);
    }

    /** {@inheritDoc} */
    @Override
    protected List<Point1S> getInfPoints() {
        return Arrays.asList(
                Point1S.of(Double.NEGATIVE_INFINITY),
                Point1S.of(Double.POSITIVE_INFINITY));
    }

    /** {@inheritDoc} */
    @Override
    protected List<Point1S> getTestPoints(final int cnt, final double eps) {
        final List<Point1S> pts = new ArrayList<>(cnt);

        final double delta = Angle.TWO_PI / cnt;
        for (int i = 0; i < cnt; ++i) {
            pts.add(Point1S.of(i * delta));
        }

        return pts;
    }

    /** {@inheritDoc} */
    @Override
    protected List<Point1S> getTestPointsAtDistance(final Point1S pt, final double dist) {
        return Arrays.asList(
                Point1S.of(pt.getAzimuth() - dist),
                Point1S.of(pt.getAzimuth() + dist));
    }

    /** {@inheritDoc} */
    @Override
    protected boolean eq(final Point1S a, final Point1S b, final Precision.DoubleEquivalence precision) {
        return a.eq(b, precision);
    }

    /** {@inheritDoc} */
    @Override
    protected int disambiguateNearToFarOrder(final Point1S a, final Point1S b) {
        return Point1S.NORMALIZED_AZIMUTH_ASCENDING_ORDER.compare(a, b);
    }
}
