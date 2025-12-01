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
package org.apache.commons.geometry.spherical.twod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.core.collection.PointMapTestBase;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalCollections;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Test;

class PointMap2STest extends PointMapTestBase<Point2S> {

    @Test
    void testCircumpolarPoints() {
        // arrange
        final int circlePoints = 2_000;

        final List<Point2S> plusZPoints = new ArrayList<>();
        final List<Point2S> minusZPoints = new ArrayList<>();

        final Point2S plusZCircleStart = Point2S.of(0, 1e-7);
        final Point2S minusZCircleStart = Point2S.of(0, Math.PI - 1e-7);

        final double delta = Angle.TWO_PI / circlePoints;
        for (int i = 0; i < circlePoints; ++i) {
            final Transform2S transform = Transform2S.createRotation(Point2S.PLUS_K, i * delta);

            plusZPoints.add(transform.apply(plusZCircleStart));
            minusZPoints.add(transform.apply(minusZCircleStart));
        }

        plusZPoints.add(Point2S.PLUS_K);
        minusZPoints.add(Point2S.MINUS_K);

        // act
        final PointMap<Point2S, Integer> map = getMap(PRECISION);

        final PointMapChecker<Point2S, Integer> checker = checkerFor(map);
        int i = 0;
        for (final Point2S pt : plusZPoints) {
            map.put(pt, i);

            checker.expectEntry(pt, i);
            ++i;
        }
        for (final Point2S pt : minusZPoints) {
            map.put(pt, i);

            checker.expectEntry(pt, i);
            ++i;
        }

        // assert
        checker.check();
    }

    @Test
    void testGreatCirclePoints() {
        // arrange
        final int cnt = 1_000;
        final List<Point2S> pts = new ArrayList<>(cnt);
        final double delta = Angle.TWO_PI / cnt;
        for (int i = 0; i < cnt; ++i) {
            pts.add(Transform2S.createRotation(Point2S.PLUS_I, i * delta)
                    .apply(Point2S.PLUS_K));
        }

        // act
        final PointMap<Point2S, Integer> map = getMap(PRECISION);

        final PointMapChecker<Point2S, Integer> checker = checkerFor(map);
        for (int i = 0; i < cnt; ++i) {
            final Point2S key = pts.get(i);

            map.put(key, i);

            checker.expectEntry(key, i);
        }

        // assert
        checker.check();
    }

    /** {@inheritDoc} */
    @Override
    protected <V> PointMap<Point2S, V> getMap(final Precision.DoubleEquivalence precision) {
        return SphericalCollections.pointMap2S(precision);
    }

    /** {@inheritDoc} */
    @Override
    protected Point2S[] createPointArray() {
        return new Point2S[0];
    }

    /** {@inheritDoc} */
    @Override
    protected List<Point2S> getNaNPoints() {
        return Arrays.asList(
                Point2S.of(0, Double.NaN),
                Point2S.of(Double.NaN, 0),
                Point2S.of(Double.NaN, Double.NaN));
    }

    /** {@inheritDoc} */
    @Override
    protected List<Point2S> getInfPoints() {
        return Arrays.asList(
                Point2S.of(0, Double.NEGATIVE_INFINITY),
                Point2S.of(Double.NEGATIVE_INFINITY, 0),
                Point2S.of(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY),

                Point2S.of(0, Double.POSITIVE_INFINITY),
                Point2S.of(Double.POSITIVE_INFINITY, 0),
                Point2S.of(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
    }

    /** {@inheritDoc} */
    @Override
    protected List<Point2S> getTestPoints(final int cnt, final double eps) {
        final List<Point2S> pts = new ArrayList<>(cnt);

        final double delta = 10 * eps;
        final double maxAz = Angle.TWO_PI - delta;

        final Transform2S polarRotation = Transform2S.createRotation(Point2S.PLUS_J, Math.PI / cnt);

        Point2S pt = Point2S.PLUS_K;
        while (pts.size() < cnt) {
            pts.add(pt);

            if (pts.size() == 1 || pt.getAzimuth() >= maxAz) {
                // we've wrapped around in azimuth so move toward the
                // -z pole
                pt = polarRotation.apply(pt);
            } else {
                // rotate in azimuth
                final Vector3D.Unit u = pt.getVector();
                final Vector3D.Unit w = u.orthogonal(Vector3D.Unit.PLUS_Z);

                pt = Transform2S.createRotation(w, delta).apply(pt);
            }
        }

        return pts;
    }

    /** {@inheritDoc} */
    @Override
    protected List<Point2S> getTestPointsAtDistance(final Point2S pt, final double dist) {
        final Vector3D.Unit u = pt.getVector();
        final Vector3D.Unit v = u.orthogonal();
        final Vector3D.Unit w = u.cross(v).normalize();

        final double t = dist / Angle.PI_OVER_TWO;

        return Arrays.asList(
                pt.slerp(Point2S.from(v), t),
                pt.slerp(Point2S.from(v.negate()), t),
                pt.slerp(Point2S.from(w), t),
                pt.slerp(Point2S.from(w.negate()), t));
    }

    /** {@inheritDoc} */
    @Override
    protected boolean eq(final Point2S a, final Point2S b, final Precision.DoubleEquivalence precision) {
        return a.eq(b, precision);
    }

    /** {@inheritDoc} */
    @Override
    protected int disambiguateNearToFarOrder(final Point2S a, final Point2S b) {
        return Point2S.POLAR_AZIMUTH_ASCENDING_ORDER.compare(a, b);
    }
}
