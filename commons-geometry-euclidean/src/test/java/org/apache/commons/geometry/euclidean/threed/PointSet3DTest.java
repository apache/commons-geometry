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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.collection.PointSet;
import org.apache.commons.geometry.core.collection.PointSetTestBase;
import org.apache.commons.geometry.euclidean.EuclideanCollections;
import org.apache.commons.numbers.core.Precision;

class PointSet3DTest extends PointSetTestBase<Vector3D> {

    /** {@inheritDoc} */
    @Override
    protected PointSet<Vector3D> getSet(final Precision.DoubleEquivalence precision) {
        return EuclideanCollections.pointSet3D(precision);
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
}
