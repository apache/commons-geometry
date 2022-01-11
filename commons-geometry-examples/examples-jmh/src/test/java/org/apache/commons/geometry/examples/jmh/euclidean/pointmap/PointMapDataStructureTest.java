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
package org.apache.commons.geometry.examples.jmh.euclidean.pointmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.numbers.core.Precision.DoubleEquivalence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Base class for potential point map data structures. These tests are not
 * meant to be complete. The are only intended to perform enough assertions
 * to ensure that a potential algorithm is not missing any critical functionality.
 */
abstract class PointMapDataStructureTest {

    private static final double EPS = 1e-1;

    private static final Precision.DoubleEquivalence PRECISION =
            Precision.doubleEquivalenceOfEpsilon(EPS);

    /** Get a new map instance for testing.
     * @param precision precision context to determine floating point equality
     * @return a new map instance for testing.
     */
    abstract Map<Vector3D, Integer> getMap(Precision.DoubleEquivalence precision);

    @Test
    void testMapOperations_simple() {
        // -- arrange
        final Map<Vector3D, Integer> map = getMap(PRECISION);
        final Vector3D a = Vector3D.of(1, 2, 3);
        final Vector3D b = Vector3D.of(3, 4, 5);
        final Vector3D c = Vector3D.of(6, 7, 8);

        final Vector3D aAlt = Vector3D.of(1.09, 2.09, 3.09);
        final Vector3D bAlt = Vector3D.of(2.91, 3.91, 4.91);
        final Vector3D cAlt = Vector3D.of(6.09, 6.91, 8.09);

        // -- act/assert
        Assertions.assertNull(map.put(a, 1));
        Assertions.assertNull(map.put(b, 2));
        Assertions.assertNull(map.put(c, 3));

        Assertions.assertEquals(3, map.size());

        Assertions.assertEquals(1, map.get(a));
        Assertions.assertEquals(1, map.get(aAlt));
        Assertions.assertEquals(2, map.get(b));
        Assertions.assertEquals(2, map.get(bAlt));
        Assertions.assertEquals(3, map.get(c));
        Assertions.assertEquals(3, map.get(cAlt));

        Assertions.assertEquals(1, map.put(aAlt, -1));
        Assertions.assertEquals(2, map.put(bAlt, -2));
        Assertions.assertEquals(3, map.put(cAlt, -3));

        Assertions.assertEquals(3, map.size());

        Assertions.assertEquals(-1, map.get(a));
        Assertions.assertEquals(-1, map.get(aAlt));
        Assertions.assertEquals(-2, map.get(b));
        Assertions.assertEquals(-2, map.get(bAlt));
        Assertions.assertEquals(-3, map.get(c));
        Assertions.assertEquals(-3, map.get(cAlt));

        Assertions.assertEquals(-1, map.remove(aAlt));
        Assertions.assertEquals(-2, map.remove(bAlt));
        Assertions.assertEquals(-3, map.remove(cAlt));

        Assertions.assertEquals(0, map.size());

        Assertions.assertNull(map.get(a));
        Assertions.assertNull(map.get(aAlt));
        Assertions.assertNull(map.get(b));
        Assertions.assertNull(map.get(bAlt));
        Assertions.assertNull(map.get(c));
        Assertions.assertNull(map.get(cAlt));
    }

    @Test
    void testGetResolution_simple() {
        // -- arrange
        final Map<Vector3D, Integer> map = getMap(PRECISION);
        final Vector3D v = Vector3D.ZERO;
        final double smallDelta = 0.05;
        final double largeDelta = 0.15;

        map.put(v, 1);

        // -- act/assert
        EuclideanTestUtils.permute(-1, 1, 1, (x, y, z) -> {
            final Vector3D pt = Vector3D.of(x, y, z).multiply(smallDelta);
            Assertions.assertEquals(1, map.get(pt), () -> "Point " + pt + " not found in map");
        });

        EuclideanTestUtils.permuteSkipZero(-1, 1, 1, (x, y, z) -> {
            final Vector3D pt = Vector3D.of(x, y, z).multiply(largeDelta);
            Assertions.assertNull(map.get(pt), () -> "Point " + pt + " found in map");
        });
    }

    @Test
    void testGetResolution_populatedMap() {
        // -- arrange
        final Map<Vector3D, Integer> map = getMap(PRECISION);
        final Vector3D v = Vector3D.ZERO;
        final double smallDelta = 0.05;
        final double largeDelta = 0.15;

        // add a number of points about the origin to make sure the map is populated
        // and we're not just dealing with trivial cases
        final double insertDelta = 0.3;
        EuclideanTestUtils.permuteSkipZero(-4, 4, 1, (x, y, z) ->
            map.put(Vector3D.of(x, y, z).multiply(insertDelta), 0));

        // add a point exactly at the origin
        map.put(v, 1);

        // -- act/assert
        EuclideanTestUtils.permute(-1, 1, 1, (x, y, z) -> {
            final Vector3D pt = Vector3D.of(x, y, z).multiply(smallDelta);
            Assertions.assertEquals(1, map.get(pt), () -> "Point " + pt + " not found in map");
        });

        EuclideanTestUtils.permuteSkipZero(-1, 1, 1, (x, y, z) -> {
            final Vector3D pt = Vector3D.of(x, y, z).multiply(largeDelta);
            Assertions.assertNull(map.get(pt), () -> "Point " + pt + " found in map");
        });
    }

    @Test
    void testMapOperations_randomOrder() {
        // -- arrange
        final Map<Vector3D, Integer> map = getMap(PRECISION);
        final Vector3D v = Vector3D.of(1, 2, -1);

        final double start = -3.0;
        final double stop = 3.0;
        final double step = 0.3;

        // -- act/assert

        // populate the map with entries in a random order
        final List<Vector3D> points = new ArrayList<>();
        EuclideanTestUtils.permute(start, stop, step, (x, y, z) -> points.add(Vector3D.of(x, y, z)));
        Collections.shuffle(points, new Random(1L));

        points.forEach(p -> map.put(p, -1));
        map.put(v, 1);

        Assertions.assertEquals(1, map.get(v));

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
        map.remove(v);

        // check that we don't have anything left
        Assertions.assertEquals(0, map.size());
        Assertions.assertNull(map.get(v));
    }

    /** Unit test for the {@link VariableSplitOctree} data structure.
     */
    static class VariableSplitOctreeTest extends PointMapDataStructureTest {
        /** {@inheritDoc} */
        @Override
        Map<Vector3D, Integer> getMap(final DoubleEquivalence precision) {
            return new VariableSplitOctree<>(PRECISION);
        }
    }

    /** Unit test for the {@link KDTreeTest} data structure.
     */
    static class KDTreeTest extends PointMapDataStructureTest {
        /** {@inheritDoc} */
        @Override
        Map<Vector3D, Integer> getMap(final DoubleEquivalence precision) {
            return new KDTree<>(PRECISION);
        }
    }

    /** Unit test for the {@link RebuildingKDTreeTest} data structure.
     */
    static class RebuildingKDTreeTest extends PointMapDataStructureTest {
        /** {@inheritDoc} */
        @Override
        Map<Vector3D, Integer> getMap(final DoubleEquivalence precision) {
            return new RebuildingKDTree<>(PRECISION);
        }
    }

    /** Unit test for the {@link BucketKDTree} data structure.
     */
    static class BucketKDTreeTest extends PointMapDataStructureTest {
        /** {@inheritDoc} */
        @Override
        Map<Vector3D, Integer> getMap(final DoubleEquivalence precision) {
            return new BucketKDTree<>(PRECISION);
        }
    }

    /** Unit test for the {@link BucketKDLeafTree} data structure.
     */
    static class BucketLeafKDTreeTest extends PointMapDataStructureTest {
        /** {@inheritDoc} */
        @Override
        Map<Vector3D, Integer> getMap(final DoubleEquivalence precision) {
            return new BucketLeafKDTree<>(PRECISION);
        }
    }
}
