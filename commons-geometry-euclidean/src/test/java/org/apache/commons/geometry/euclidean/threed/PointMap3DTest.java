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
import java.util.List;

import org.apache.commons.geometry.core.collection.PointMap;
import org.apache.commons.geometry.core.collection.PointMapTestBase;
import org.apache.commons.numbers.core.Precision;

class PointMap3DTest extends PointMapTestBase<Vector3D> {

    /** {@inheritDoc} */
    @Override
    public <V> PointMap<Vector3D, V> getMap(final Precision.DoubleEquivalence precision) {
        return PointMap3D.of(precision);
    }

    /** {@inheritDoc} */
    @Override
    public List<Vector3D> getTestPoints(final int cnt, final double eps) {
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
    public boolean eq(final Vector3D a, final Vector3D b, final Precision.DoubleEquivalence precision) {
        return a.eq(b, precision);
    }
}
