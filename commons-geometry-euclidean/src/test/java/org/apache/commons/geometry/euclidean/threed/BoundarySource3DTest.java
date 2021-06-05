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
import java.util.stream.Collectors;

import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BoundarySource3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testToList() {
        // act
        final BoundarySource3D src = BoundarySource3D.of(
            Planes.convexPolygonFromVertices(
                    Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION)
        );

        // act
        final BoundaryList3D list = src.toList();

        // assert
        Assertions.assertEquals(1, list.count());
    }

    @Test
    void testToList_noBoundaries() {
        // act
        final BoundarySource3D src = BoundarySource3D.of();

        // act
        final BoundaryList3D list = src.toList();

        // assert
        Assertions.assertEquals(0, list.count());
    }

    @Test
    void testToTree() {
        // act
        final PlaneConvexSubset a = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION);
        final PlaneConvexSubset b = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Z), TEST_PRECISION);

        final BoundarySource3D src = BoundarySource3D.of(a, b);

        // act
        final RegionBSPTree3D tree = src.toTree();

        // assert
        Assertions.assertEquals(5, tree.count());
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
    }

    @Test
    void testToTree_noBoundaries() {
        // act
        final BoundarySource3D src = BoundarySource3D.of();

        // act
        final RegionBSPTree3D tree = src.toTree();

        // assert
        Assertions.assertEquals(1, tree.count());
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
    }

    @Test
    void testOf_varargs_empty() {
        // act
        final BoundarySource3D src = BoundarySource3D.of();

        // assert
        final List<PlaneConvexSubset> segments = src.boundaryStream().collect(Collectors.toList());
        Assertions.assertEquals(0, segments.size());
    }

    @Test
    void testOf_varargs() {
        // act
        final PlaneConvexSubset a = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION);
        final PlaneConvexSubset b = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Z), TEST_PRECISION);

        final BoundarySource3D src = BoundarySource3D.of(a, b);

        // assert
        final List<PlaneConvexSubset> boundaries = src.boundaryStream().collect(Collectors.toList());
        Assertions.assertEquals(2, boundaries.size());

        Assertions.assertSame(a, boundaries.get(0));
        Assertions.assertSame(b, boundaries.get(1));
    }

    @Test
    void testOf_list_empty() {
        // arrange
        final List<PlaneConvexSubset> input = new ArrayList<>();

        // act
        final BoundarySource3D src = BoundarySource3D.of(input);

        // assert
        final List<PlaneConvexSubset> segments = src.boundaryStream().collect(Collectors.toList());
        Assertions.assertEquals(0, segments.size());
    }

    @Test
    void testOf_list() {
        // act
        final PlaneConvexSubset a = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION);
        final PlaneConvexSubset b = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Z), TEST_PRECISION);

        final List<PlaneConvexSubset> input = new ArrayList<>();
        input.add(a);
        input.add(b);

        final BoundarySource3D src = BoundarySource3D.of(input);

        // assert
        final List<PlaneConvexSubset> segments = src.boundaryStream().collect(Collectors.toList());
        Assertions.assertEquals(2, segments.size());

        Assertions.assertSame(a, segments.get(0));
        Assertions.assertSame(b, segments.get(1));
    }
}
