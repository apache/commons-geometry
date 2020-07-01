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

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class BoundarySourceBoundsBuilder3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testGetBounds_noBoundaries() {
        // arrange
        final BoundarySource3D src = BoundarySource3D.from(new ArrayList<>());
        final BoundarySourceBoundsBuilder3D builder = new BoundarySourceBoundsBuilder3D();

        // act
        final Bounds3D b = builder.getBounds(src);

        // assert
        Assert.assertNull(b);
    }

    @Test
    public void testGetBounds_singleFiniteBoundary() {
        // arrange
        final ConvexPolygon3D poly = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(1, 1, 1),
                Vector3D.of(1, 0, 2),
                Vector3D.of(3, 4, 5)), TEST_PRECISION);

        final BoundarySource3D src = BoundarySource3D.from(poly);
        final BoundarySourceBoundsBuilder3D builder = new BoundarySourceBoundsBuilder3D();

        // act
        final Bounds3D b = builder.getBounds(src);

        // assert
        checkBounds(b, Vector3D.of(1, 0, 1), Vector3D.of(3, 4, 5));
        for (final Vector3D pt : poly.getVertices()) {
            Assert.assertTrue(b.contains(pt));
        }
    }

    @Test
    public void testGetBounds_multipleFiniteBoundaries() {
        // arrange
        final ConvexPolygon3D poly1 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(1, 1, 1),
                Vector3D.of(1, 0, 2),
                Vector3D.of(3, 4, 5)), TEST_PRECISION);

        final ConvexPolygon3D poly2 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(-1, 1, 1),
                Vector3D.of(1, 4, 4),
                Vector3D.of(7, 4, 5)), TEST_PRECISION);

        final ConvexPolygon3D poly3 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(-2, 1, 1),
                Vector3D.of(1, 7, 2),
                Vector3D.of(5, 4, 10)), TEST_PRECISION);

        final BoundarySource3D src = BoundarySource3D.from(poly1, poly2, poly3);
        final BoundarySourceBoundsBuilder3D builder = new BoundarySourceBoundsBuilder3D();

        // act
        final Bounds3D b = builder.getBounds(src);

        // assert
        checkBounds(b, Vector3D.of(-2, 0, 1), Vector3D.of(7, 7, 10));

        src.boundaryStream().forEach(boundary -> {
            for (final Vector3D pt : boundary.getVertices()) {
                Assert.assertTrue(b.contains(pt));
            }
        });
    }

    @Test
    public void testGetBounds_singleInfiniteBoundary() {
        // arrange
        final PlaneConvexSubset boundary = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION)
                .span();
        final BoundarySource3D src = BoundarySource3D.from(boundary);
        final BoundarySourceBoundsBuilder3D builder = new BoundarySourceBoundsBuilder3D();

        // act
        final Bounds3D b = builder.getBounds(src);

        // assert
        Assert.assertNull(b);
    }

    @Test
    public void testGetBounds_mixedFiniteAndInfiniteBoundaries() {
        // arrange
        final PlaneConvexSubset inf = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION)
                .span()
                .split(Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, TEST_PRECISION))
                .getMinus();

        final ConvexPolygon3D poly1 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(1, 1, 1),
                Vector3D.of(1, 0, 2),
                Vector3D.of(3, 4, 5)), TEST_PRECISION);

        final ConvexPolygon3D poly2 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(-1, 1, 1),
                Vector3D.of(1, 4, 4),
                Vector3D.of(7, 4, 5)), TEST_PRECISION);

        final ConvexPolygon3D poly3 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(-2, 1, 1),
                Vector3D.of(1, 7, 2),
                Vector3D.of(5, 4, 10)), TEST_PRECISION);

        final BoundarySource3D src = BoundarySource3D.from(poly1, poly2, inf, poly3);
        final BoundarySourceBoundsBuilder3D builder = new BoundarySourceBoundsBuilder3D();

        // act
        final Bounds3D b = builder.getBounds(src);

        // assert
        Assert.assertNull(b);
    }

    private static void checkBounds(final Bounds3D b, final Vector3D min, final Vector3D max) {
        EuclideanTestUtils.assertCoordinatesEqual(min, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(max, b.getMax(), TEST_EPS);
    }
}
