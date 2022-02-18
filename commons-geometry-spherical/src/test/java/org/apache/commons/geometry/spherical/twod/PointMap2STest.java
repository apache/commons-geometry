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
package org.apache.commons.geometry.spherical.twod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.core.collection.PointMapTestBase;
import org.apache.commons.geometry.spherical.SphericalCollections;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;

class PointMap2STest extends PointMapTestBase<Point2S> {

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

        final double delta = Angle.TWO_PI / cnt;
        double az = 0.0;
        double pol = 0.0;
        for (int i = 0; i < cnt; ++i) {
            if (i % 2 == 0) {
                az += i * delta;
            } else {
                pol += i * delta;
            }

            pts.add(Point2S.of(az, pol));
        }

        return pts;
    }

    /** {@inheritDoc} */
    @Override
    protected List<Point2S> getTestPointsAtDistance(final Point2S pt, final double dist) {
        return Arrays.asList(
                Point2S.of(pt.getAzimuth() - dist, pt.getPolar() - dist),
                Point2S.of(pt.getAzimuth() - dist, pt.getPolar() + dist),
                Point2S.of(pt.getAzimuth() + dist, pt.getPolar() - dist),
                Point2S.of(pt.getAzimuth() + dist, pt.getPolar() + dist));
    }
}
