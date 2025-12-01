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
package org.apache.commons.geometry.euclidean.oned;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.collection.PointSet;
import org.apache.commons.geometry.core.collection.PointSetTestBase;
import org.apache.commons.geometry.euclidean.EuclideanCollections;
import org.apache.commons.numbers.core.Precision;

class PointSet1DTest extends PointSetTestBase<Vector1D> {

    /** {@inheritDoc} */
    @Override
    protected PointSet<Vector1D> getSet(final Precision.DoubleEquivalence precision) {
        return EuclideanCollections.pointSet1D(precision);
    }

    /** {@inheritDoc} */
    @Override
    protected Vector1D[] createPointArray() {
        return new Vector1D[0];
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector1D> getNaNPoints() {
        return Collections.singletonList(Vector1D.NaN);
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector1D> getInfPoints() {
        return Arrays.asList(
                Vector1D.NEGATIVE_INFINITY,
                Vector1D.POSITIVE_INFINITY);
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector1D> getTestPoints(final int cnt, final double eps) {
        final List<Vector1D> pts = new ArrayList<>(cnt);

        final double delta = 10 * eps;

        double x = 0.0;
        for (int i = 0; i < cnt; ++i) {
            pts.add(Vector1D.of(x));

            x += delta;
        }

        return pts;
    }

    /** {@inheritDoc} */
    @Override
    protected List<Vector1D> getTestPointsAtDistance(final Vector1D pt, final double dist) {
        return Arrays.asList(
                Vector1D.of(pt.getX() - dist),
                Vector1D.of(pt.getX() + dist));
    }

    /** {@inheritDoc} */
    @Override
    protected boolean eq(final Vector1D a, final Vector1D b, final Precision.DoubleEquivalence precision) {
        return a.eq(b, precision);
    }

    /** {@inheritDoc} */
    @Override
    protected int disambiguateNearToFarOrder(final Vector1D a, final Vector1D b) {
        return Vector1D.COORDINATE_ASCENDING_ORDER.compare(a, b);
    }
}
