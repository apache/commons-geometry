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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class SimpleTriangle3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Plane XY_PLANE_Z1 = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
            Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testProperties() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 2, 1);
        Vector3D p2 = Vector3D.of(2, 2, 1);
        Vector3D p3 = Vector3D.of(2, 3, 1);

        // act
        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1, p1, p2, p3);

        // assert
        Assert.assertFalse(tri.isFull());
        Assert.assertFalse(tri.isEmpty());
        Assert.assertFalse(tri.isInfinite());
        Assert.assertTrue(tri.isFinite());

        Assert.assertSame(XY_PLANE_Z1, tri.getPlane());
        Assert.assertSame(p1, tri.getPoint1());
        Assert.assertSame(p2, tri.getPoint2());
        Assert.assertSame(p3, tri.getPoint3());

        Assert.assertEquals(Arrays.asList(p1, p2, p3), tri.getVertices());

        List<Vector2D> subspaceVertices = tri.getEmbedded().getSubspaceRegion().getVertices();
        Assert.assertEquals(3, subspaceVertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), subspaceVertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 2), subspaceVertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 3), subspaceVertices.get(2), TEST_EPS);

        Assert.assertEquals(0.5, tri.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(5.0 / 3.0, 7.0 / 3.0, 1), tri.getBarycenter(), TEST_EPS);

        Bounds3D bounds = tri.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 3, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testVertices_listIsImmutable() {
        // arrange
        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            tri.getVertices().add(Vector3D.of(-1, 0, 1));
        }, UnsupportedOperationException.class);
    }

    @Test
    public void testToTriangles() {
        // arrange
        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        // act
        List<Triangle3D> triangles = tri.toTriangles();

        // assert
        Assert.assertEquals(1, triangles.size());
        Assert.assertSame(tri, triangles.get(0));
    }

    @Test
    public void testGetSize() {
        // arrange
        QuaternionRotation rot = QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, 0.2);

        // act/assert
        Assert.assertEquals(0.5, new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1)).getSize(), TEST_EPS);

        Assert.assertEquals(1, new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(2, 0, 1), Vector3D.of(0, 1, 1)).getSize(), TEST_EPS);

        Assert.assertEquals(1.5, new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(1, 2, 1), Vector3D.of(4, 2, 1), Vector3D.of(2, 3, 1)).getSize(), TEST_EPS);

        Assert.assertEquals(1.5, new SimpleTriangle3D(XY_PLANE_Z1,
                rot.applyVector(Vector3D.of(1, 2, 1)),
                rot.apply(Vector3D.of(4, 2, 1)),
                rot.applyVector(Vector3D.of(2, 3, 1))).getSize(), TEST_EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 2, 1);
        Vector3D p2 = Vector3D.of(3, 2, 1);
        Vector3D p3 = Vector3D.of(2, 3, 1);

        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1, p1, p2, p3);

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
    public void testClosest() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 2, 1);
        Vector3D p2 = Vector3D.of(3, 2, 1);
        Vector3D p3 = Vector3D.of(2, 3, 1);

        Vector3D centroid = Vector3D.centroid(p1, p2, p3);

        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1, p1, p2, p3);

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
    public void testReverse() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 2, 1);
        Vector3D p2 = Vector3D.of(3, 2, 1);
        Vector3D p3 = Vector3D.of(2, 3, 1);

        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1, p1, p2, p3);

        // act
        SimpleTriangle3D result = tri.reverse();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, result.getPlane().getNormal(), TEST_EPS);

        Assert.assertSame(p1, result.getPoint1());
        Assert.assertSame(p3, result.getPoint2());
        Assert.assertSame(p2, result.getPoint3());

        Vector3D v1 = result.getPoint1().vectorTo(result.getPoint2());
        Vector3D v2 = result.getPoint1().vectorTo(result.getPoint3());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, v1.cross(v2).normalize(), TEST_EPS);

        Assert.assertEquals(1, result.getSize(), TEST_EPS);
    }

    @Test
    public void testTransform() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 2, 1);
        Vector3D p2 = Vector3D.of(3, 2, 1);
        Vector3D p3 = Vector3D.of(2, 3, 1);

        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1, p1, p2, p3);

        AffineTransformMatrix3D t = AffineTransformMatrix3D.identity()
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, -PlaneAngleRadians.PI_OVER_TWO))
                .scale(1, 1, 2)
                .translate(Vector3D.of(1, 0, 0));

        // act
        SimpleTriangle3D result = tri.transform(t);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X, result.getPlane().getNormal(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 2), result.getPoint1(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 2, 6), result.getPoint2(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 3, 4), result.getPoint3(), TEST_EPS);

        Vector3D v1 = result.getPoint1().vectorTo(result.getPoint2());
        Vector3D v2 = result.getPoint1().vectorTo(result.getPoint3());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X, v1.cross(v2).normalize(), TEST_EPS);

        Assert.assertEquals(2, result.getSize(), TEST_EPS);
    }

    @Test
    public void testSplit_plus() {
        // arrange
        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = tri.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(tri, split.getPlus());
    }

    @Test
    public void testSplit_minus() {
        // arrange
        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = tri.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(tri, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_both() {
        // arrange
        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(-1, 1, 0), TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = tri.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        PlaneConvexSubset minus = split.getMinus();
        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0.5, 0.5, 1)),
                minus.getVertices(), TEST_PRECISION);

        PlaneConvexSubset plus = split.getPlus();
        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(0, 0, 1), Vector3D.of(0.5, 0.5, 1), Vector3D.of(0, 1, 1)),
                plus.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testSplit_neither() {
        // arrange
        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 1e-15, -1), TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = tri.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testToString() {
        // arrange
        SimpleTriangle3D tri = new SimpleTriangle3D(XY_PLANE_Z1,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

        // act
        String str = tri.toString();

        // assert
        GeometryTestUtils.assertContains("SimpleTriangle3D[normal= (", str);
        GeometryTestUtils.assertContains("vertices= [", str);
    }

    private static void checkPoints(ConvexPolygon3D ps, RegionLocation loc, Vector3D... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, ps.classify(pt));
        }
    }
}
