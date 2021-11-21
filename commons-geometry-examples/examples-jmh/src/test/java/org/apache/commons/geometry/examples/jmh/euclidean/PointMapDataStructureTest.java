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
package org.apache.commons.geometry.examples.jmh.euclidean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

abstract class PointMapDataStructureTest {

    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(1e-1);

    /** Get a new map instance for testing.
     * @param precision precision context to determine floating point equality
     * @return a new map instance for testing.
     */
    abstract Map<Vector3D, Integer> getMap(Precision.DoubleEquivalence precision);

    @Test
    void testInsertGetRemove() {
        // -- arrange
        final Map<Vector3D, Integer> map = getMap(PRECISION);
        final Vector3D v = Vector3D.of(1, 2, -1);

        final double start = -3.0;
        final double stop = 3.0;
        final double step = 0.4;

        // -- act/assert

        // populate the map with entries in a random order
        final List<Vector3D> points = new ArrayList<>();
        EuclideanTestUtils.permute(start, stop, step, (x, y, z) -> points.add(Vector3D.of(x, y, z)));
        Collections.shuffle(points, new Random(1L));

        points.forEach(p -> map.put(p, -1));
        map.put(v, 1);

        // assert that each entry has a value
        EuclideanTestUtils.permute(start, stop, step, (x, y, z) -> {
            final Vector3D k = Vector3D.of(x, y, z);
            final int val = k.eq(v, PRECISION) ?
                    1 :
                    -1;

            Assertions.assertEquals(val, map.get(k));
        });

        // remove entries
        EuclideanTestUtils.permute(start, stop, step, (x, y, z) -> {
            map.remove(Vector3D.of(x, y, z));
        });

        // check that we don't have anything left
        Assertions.assertNull(map.get(v));
    }

//    static class StandardMapTest extends PointMapDataStructureTest {
//
//        /** {@inheritDoc} */
//        @Override
//        Map<Vector3D, Integer> getMap(final DoubleEquivalence precision) {
//            return new TreeMap<>(Vector3D.COORDINATE_ASCENDING_ORDER);
//        }
//    }
}
