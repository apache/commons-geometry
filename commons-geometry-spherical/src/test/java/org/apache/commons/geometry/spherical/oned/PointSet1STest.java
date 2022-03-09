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

import org.apache.commons.geometry.core.collection.PointSet;
import org.apache.commons.geometry.core.collection.PointSetTestBase;
import org.apache.commons.geometry.spherical.SphericalCollections;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;

class PointSet1STest extends PointSetTestBase<Point1S> {

    /** {@inheritDoc} */
    @Override
    protected PointSet<Point1S> getSet(final Precision.DoubleEquivalence precision) {
        return SphericalCollections.pointSet1S(precision);
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
}
