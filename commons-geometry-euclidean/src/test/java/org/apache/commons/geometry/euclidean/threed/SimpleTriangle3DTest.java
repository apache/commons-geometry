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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleTriangle3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final Plane XY_PLANE_Z1 = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
            Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    void testProperties() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 2, 1);
        final Vector3D p2 = Vector3D.of(2, 2, 1);
        final Vector3D p3 = Vector3D.of(2, 3, 1);

        // act
        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1, p1, p2, p3);

        // assert
        Assertions.assertFalse(tri.isFull());
        Assertions.assertFalse(tri.isEmpty());
        Assertions.assertFalse(tri.isInfinite());
        Assertions.assertTrue(tri.isFinite());

        Assertions.assertSame(XY_PLANE_Z1, tri.getPlane());
        Assertions.assertSame(p1, tri.getPoint1());
        Assertions.assertSame(p2, tri.getPoint2());
        Assertions.assertSame(p3, tri.getPoint3());

        Assertions.assertEquals(Arrays.asList(p1, p2, p3), tri.getVertices());

        final List<Vector2D> subspaceVertices = tri.getEmbedded().getSubspaceRegion().getVertices();
        Assertions.assertEquals(3, subspaceVertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), subspaceVertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 2), subspaceVertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 3), subspaceVertices.get(2), TEST_EPS);

        Assertions.assertEquals(0.5, tri.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(5.0 / 3.0, 7.0 / 3.0, 1), tri.getCentroid(), TEST_EPS);

        final Bounds3D bounds = tri.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 3, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    void testVertices_listIsImmutable() {
        // arrange
        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        // act/assert
        Assertions.assertThrows(UnsupportedOperationException.class, () -> tri.getVertices().add(Vector3D.of(-1, 0, 1)));
    }

    @Test
    void testToTriangles() {
        // arrange
        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        // act
        final List<Triangle3D> triangles = tri.toTriangles();

        // assert
        Assertions.assertEquals(1, triangles.size());
        Assertions.assertSame(tri, triangles.get(0));
    }

    @Test
    void testGetSize() {
        // arrange
        final QuaternionRotation rot = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.2);

        // act/assert
        Assertions.assertEquals(0.5, new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1)).getSize(), TEST_EPS);

        Assertions.assertEquals(1, new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(2, 0, 1), Vector3D.of(0, 1, 1)).getSize(), TEST_EPS);

        Assertions.assertEquals(1.5, new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(1, 2, 1), Vector3D.of(4, 2, 1), Vector3D.of(2, 3, 1)).getSize(), TEST_EPS);

        Assertions.assertEquals(1.5, new SimpleTriangle3D(XY_PLANE_Z1,
                rot.applyVector(Vector3D.of(1, 2, 1)),
                rot.apply(Vector3D.of(4, 2, 1)),
                rot.applyVector(Vector3D.of(2, 3, 1))).getSize(), TEST_EPS);
    }

    @Test
    void testClassify() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 2, 1);
        final Vector3D p2 = Vector3D.of(3, 2, 1);
        final Vector3D p3 = Vector3D.of(2, 3, 1);

        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1, p1, p2, p3);

        // act/assert
        checkPoints(tri, RegionLocation.INSIDE, Vector3D.of(2, 2.5, 1), Vector3D.of(2, 2.5, 1 + 1e-15));
        checkPoints(tri, RegionLocation.BOUNDARY,
                p1, p2, p3,
                p1.lerp(p2, 0.5), p2.lerp(p3, 0.5), p3.lerp(p1,  0.5));
        checkPoints(tri, RegionLocation.OUTSIDE,
                Vector3D.of(2, 2.5, 0), Vector3D.of(2, 2.5, 2),
                Vector3D.of(0, 2, 1), Vector3D.of(4, 2, 1),
                Vector3D.of(2, 4, 1), Vector3D.of(2, 1, 1));
    }

    @Test
    void testClosest() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 2, 1);
        final Vector3D p2 = Vector3D.of(3, 2, 1);
        final Vector3D p3 = Vector3D.of(2, 3, 1);

        final Vector3D centroid = Vector3D.centroid(p1, p2, p3);

        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1, p1, p2, p3);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(centroid, tri.closest(centroid), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(centroid, tri.closest(centroid.add(Vector3D.Unit.PLUS_Z)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(centroid, tri.closest(centroid.add(Vector3D.Unit.MINUS_Z)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(p1, tri.closest(Vector3D.of(0, 2, 5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, tri.closest(Vector3D.of(1, 2, 5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 2, 1), tri.closest(Vector3D.of(2, 2, 5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, tri.closest(Vector3D.of(3, 2, 5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, tri.closest(Vector3D.of(4, 2, 5)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(p1, tri.closest(Vector3D.of(0, 1, 5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, tri.closest(Vector3D.of(1, 1, 5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 2, 1), tri.closest(Vector3D.of(2, 1, 5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, tri.closest(Vector3D.of(3, 1, 5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p2, tri.closest(Vector3D.of(4, 1, 5)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 2.5, 1),
                tri.closest(Vector3D.of(1, 3, -10)), TEST_EPS);
    }

    @Test
    void testReverse() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 2, 1);
        final Vector3D p2 = Vector3D.of(3, 2, 1);
        final Vector3D p3 = Vector3D.of(2, 3, 1);

        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1, p1, p2, p3);

        // act
        final SimpleTriangle3D result = tri.reverse();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, result.getPlane().getNormal(), TEST_EPS);

        Assertions.assertSame(p1, result.getPoint1());
        Assertions.assertSame(p3, result.getPoint2());
        Assertions.assertSame(p2, result.getPoint3());

        final Vector3D v1 = result.getPoint1().vectorTo(result.getPoint2());
        final Vector3D v2 = result.getPoint1().vectorTo(result.getPoint3());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, v1.cross(v2).normalize(), TEST_EPS);

        Assertions.assertEquals(1, result.getSize(), TEST_EPS);
    }

    @Test
    void testTransform() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 2, 1);
        final Vector3D p2 = Vector3D.of(3, 2, 1);
        final Vector3D p3 = Vector3D.of(2, 3, 1);

        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1, p1, p2, p3);

        final AffineTransformMatrix3D t = AffineTransformMatrix3D.identity()
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, -Angle.PI_OVER_TWO))
                .scale(1, 1, 2)
                .translate(Vector3D.of(1, 0, 0));

        // act
        final SimpleTriangle3D result = tri.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X, result.getPlane().getNormal(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 2), result.getPoint1(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 6), result.getPoint2(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 3, 4), result.getPoint3(), TEST_EPS);

        final Vector3D v1 = result.getPoint1().vectorTo(result.getPoint2());
        final Vector3D v2 = result.getPoint1().vectorTo(result.getPoint3());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X, v1.cross(v2).normalize(), TEST_EPS);

        Assertions.assertEquals(2, result.getSize(), TEST_EPS);
    }

    @Test
    void testSplit_plus() {
        // arrange
        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = tri.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(tri, split.getPlus());
    }

    @Test
    void testSplit_minus() {
        // arrange
        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = tri.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assertions.assertSame(tri, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    void testSplit_both() {
        // arrange
        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(-1, 1, 0), TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = tri.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final PlaneConvexSubset minus = split.getMinus();
        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0.5, 0.5, 1)),
                minus.getVertices(), TEST_PRECISION);

        final PlaneConvexSubset plus = split.getPlus();
        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(0, 0, 1), Vector3D.of(0.5, 0.5, 1), Vector3D.of(0, 1, 1)),
                plus.getVertices(), TEST_PRECISION);
    }

    @Test
    void testSplit_neither() {
        // arrange
        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 1e-15, -1), TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = tri.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    void testToString() {
        // arrange
        final SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        // act
        final String str = tri.toString();

        // assert
        GeometryTestUtils.assertContains("SimpleTriangle3D[normal= (", str);
        GeometryTestUtils.assertContains("vertices= [", str);
    }

    private static void checkPoints(final ConvexPolygon3D ps, final RegionLocation loc, final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(loc, ps.classify(pt), "Unexpected location for point " + pt);
        }
    }
}
