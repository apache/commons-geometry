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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class VertexListConvexPolygon3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Plane XY_PLANE_Z1 = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
            Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    private static final List<Vector3D> TRIANGLE_VERTICES =
            Arrays.asList(Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1));

    @Test
    public void testProperties() {
        // act
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, TRIANGLE_VERTICES);

        // assert
        Assert.assertFalse(p.isFull());
        Assert.assertFalse(p.isEmpty());
        Assert.assertTrue(p.isFinite());
        Assert.assertFalse(p.isInfinite());

        Assert.assertEquals(0.5, p.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.0 / 3.0, 1.0 / 3.0, 1), p.getBarycenter(), TEST_EPS);

        Assert.assertSame(XY_PLANE_Z1, p.getPlane());

        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1)),
                p.getVertices(), TEST_PRECISION);


        Bounds3D bounds = p.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testCtor_validatesVertexListSize() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            new VertexListConvexPolygon3D(XY_PLANE_Z1, Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X));
        }, IllegalArgumentException.class, "Convex polygon requires at least 3 points; found 2");
    }

    @Test
    public void testVertices_listIsImmutable() {
        // arrange
        List<Vector3D> vertices = new ArrayList<>(TRIANGLE_VERTICES);
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, vertices);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            p.getVertices().add(Vector3D.of(-1, 0, 1));
        }, UnsupportedOperationException.class);
    }

    @Test
    public void testGetBarycenter_linearVertices() {
        // this should not happen with all of the checks in place for constructing these
        // instances; this test is to ensure that the barycenter computation can still handle
        // the situation

        // arrange
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(0.5, 0, 0), Vector3D.of(2, 0, 0));
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, vertices);

        // act
        Vector3D center = p.getBarycenter();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), center, TEST_EPS);
    }

    @Test
    public void testGetSubspaceRegion() {
        // arrange
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, TRIANGLE_VERTICES);

        // act
        ConvexArea area = p.getEmbedded().getSubspaceRegion();

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertTrue(area.isFinite());
        Assert.assertFalse(area.isInfinite());

        Assert.assertEquals(0.5, area.getSize(), TEST_EPS);

        List<Vector2D> vertices = area.getVertices();
        Assert.assertEquals(3, vertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 0), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 1), vertices.get(2), TEST_EPS);
    }

    @Test
    public void testToTriangles_threeVertices() {
        // arrange
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, TRIANGLE_VERTICES);

        // act
        List<Triangle3D> tris = p.toTriangles();

        // assert
        Assert.assertEquals(1, tris.size());

        Triangle3D a = tris.get(0);
        Assert.assertSame(XY_PLANE_Z1, a.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(TRIANGLE_VERTICES, a.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testToTriangles_fiveVertices() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 1, 1);
        Vector3D p2 = Vector3D.of(2, 1.2, 1);
        Vector3D p3 = Vector3D.of(3, 2, 1);
        Vector3D p4 = Vector3D.of(1, 4, 1);
        Vector3D p5 = Vector3D.of(0, 2, 1);

        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, Arrays.asList(p1, p2, p3, p4, p5));

        // act
        List<Triangle3D> tris = p.toTriangles();

        // assert
        Assert.assertEquals(3, tris.size());

        Triangle3D a = tris.get(0);
        Assert.assertSame(XY_PLANE_Z1, a.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p2, p3, p4), a.getVertices(), TEST_PRECISION);

        Triangle3D b = tris.get(1);
        Assert.assertSame(XY_PLANE_Z1, b.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p2, p4, p5), b.getVertices(), TEST_PRECISION);

        Triangle3D c = tris.get(2);
        Assert.assertSame(XY_PLANE_Z1, c.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p2, p5, p1), c.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testClassify() {
        // arrange
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, Arrays.asList(
                    Vector3D.of(1, 2, 1), Vector3D.of(3, 2, 1),
                    Vector3D.of(3, 4, 1), Vector3D.of(1, 4, 1)
                ));

        // act/assert
        checkPoints(p, RegionLocation.INSIDE, Vector3D.of(2, 3, 1));
        checkPoints(p, RegionLocation.BOUNDARY,
                Vector3D.of(1, 3, 1), Vector3D.of(3, 3, 1),
                Vector3D.of(2, 2, 1), Vector3D.of(2, 4, 1));
        checkPoints(p, RegionLocation.OUTSIDE,
                Vector3D.of(2, 3, 0), Vector3D.of(2, 3, 2),
                Vector3D.of(0, 3, 1), Vector3D.of(4, 3, 1),
                Vector3D.of(2, 1, 1), Vector3D.of(2, 5, 1));
    }

    @Test
    public void testClosest() {
        // arrange
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, Arrays.asList(
                Vector3D.of(1, 2, 1), Vector3D.of(3, 2, 1),
                Vector3D.of(3, 4, 1), Vector3D.of(1, 4, 1)
            ));

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 3, 1), p.closest(Vector3D.of(2, 3, 1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 3, 1), p.closest(Vector3D.of(2, 3, 100)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 4, 1), p.closest(Vector3D.of(3, 5, 10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 4, 1), p.closest(Vector3D.of(3, 4, 10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 3, 1), p.closest(Vector3D.of(3, 3, 10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 2, 1), p.closest(Vector3D.of(3, 2, 10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 2, 1), p.closest(Vector3D.of(3, 1, 10)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 4, 1), p.closest(Vector3D.of(0, 5, -10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 4, 1), p.closest(Vector3D.of(1, 5, -10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 4, 1), p.closest(Vector3D.of(2, 5, -10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 4, 1), p.closest(Vector3D.of(3, 5, -10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 4, 1), p.closest(Vector3D.of(4, 5, -10)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 1), p.closest(Vector3D.of(0, 2, 1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 1), p.closest(Vector3D.of(1, 2, 1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 2, 1), p.closest(Vector3D.of(2, 2, 1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 2, 1), p.closest(Vector3D.of(3, 2, 1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 2, 1), p.closest(Vector3D.of(4, 2, 1)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 3, 1), p.closest(Vector3D.of(0, 3, -10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 3, 1), p.closest(Vector3D.of(1, 3, -10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 3, 1), p.closest(Vector3D.of(2, 3, -10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 3, 1), p.closest(Vector3D.of(3, 3, -10)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 3, 1), p.closest(Vector3D.of(4, 3, -10)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 1),
                p.closest(Vector3D.of(-100, -100, -100)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 3.5, 1),
                p.closest(Vector3D.of(100, 3.5, 100)), TEST_EPS);
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix3D t = AffineTransformMatrix3D.identity()
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, -PlaneAngleRadians.PI_OVER_TWO))
                .scale(1, 1, 2)
                .translate(Vector3D.of(1, 0, 0));

        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, Arrays.asList(
                Vector3D.of(1, 2, 1), Vector3D.of(3, 2, 1),
                Vector3D.of(3, 4, 1), Vector3D.of(1, 4, 1)
            ));

        // act
        VertexListConvexPolygon3D result = p.transform(t);

        // assert
        Assert.assertFalse(result.isFull());
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isFinite());
        Assert.assertFalse(result.isInfinite());

        Assert.assertEquals(8, result.getSize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X, result.getPlane().getNormal(), TEST_EPS);

        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(0, 2, 2), Vector3D.of(0, 2, 6), Vector3D.of(0, 4, 6), Vector3D.of(0, 4, 2)),
                result.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testReverse() {
        // arrange
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, Arrays.asList(
                Vector3D.of(1, 2, 1), Vector3D.of(3, 2, 1),
                Vector3D.of(3, 4, 1), Vector3D.of(1, 4, 1)
            ));

        // act
        VertexListConvexPolygon3D result = p.reverse();

        // assert
        Assert.assertFalse(result.isFull());
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isFinite());
        Assert.assertFalse(result.isInfinite());

        Assert.assertEquals(4, result.getSize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, result.getPlane().getNormal(), TEST_EPS);

        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(1, 4, 1), Vector3D.of(3, 4, 1), Vector3D.of(3, 2, 1), Vector3D.of(1, 2, 1)),
                result.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testSplit_plus() {
        // arrange
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, TRIANGLE_VERTICES);

        Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = p.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(p, split.getPlus());
    }

    @Test
    public void testSplit_minus() {
        // arrange
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, TRIANGLE_VERTICES);

        Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = p.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(p, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_both() {
        // arrange
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, TRIANGLE_VERTICES);

        Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(-1, 1, 0), TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = p.split(splitter);

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
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, TRIANGLE_VERTICES);

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 1e-15, -1), TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = p.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testToString() {
        // arrange
        VertexListConvexPolygon3D p = new VertexListConvexPolygon3D(XY_PLANE_Z1, TRIANGLE_VERTICES);

        // act
        String str = p.toString();

        // assert
        GeometryTestUtils.assertContains("VertexListConvexPolygon3D[normal= (", str);
        GeometryTestUtils.assertContains("vertices= [", str);
    }

    private static void checkPoints(ConvexPolygon3D ps, RegionLocation loc, Vector3D... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, ps.classify(pt));
        }
    }
}
