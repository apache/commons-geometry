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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.junit.Assert;
import org.junit.Test;

public class PlanesTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testSubsetFromConvexArea() {
        // arrange
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 0),
                    Vector2D.of(3, 0),
                    Vector2D.of(3, 1),
                    Vector2D.of(1, 1)
                ), TEST_PRECISION);

        // act
        PlaneConvexSubset sp = Planes.subsetFromConvexArea(plane, area);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertFalse(sp.isEmpty());
        Assert.assertTrue(sp.isFinite());

        Assert.assertEquals(2, sp.getSize(), TEST_EPS);

        Assert.assertSame(plane, sp.getPlane());
        Assert.assertSame(plane, sp.getHyperplane());
        assertConvexAreasEqual(area, sp.getEmbedded().getSubspaceRegion());
    }

    @Test
    public void testConvexPolygonFromVertices() {
        // arrange
        Vector3D p0 = Vector3D.of(1, 0, 0);
        Vector3D p1 = Vector3D.of(1, 1, 0);
        Vector3D p2 = Vector3D.of(1, 1, 2);

        // act
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(p0, p1, p2), TEST_PRECISION);

        // assert
        Assert.assertTrue(sp instanceof Triangle3D);

        Assert.assertFalse(sp.isFull());
        Assert.assertFalse(sp.isEmpty());
        Assert.assertTrue(sp.isFinite());

        Assert.assertEquals(3, sp.getVertices().size());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p0, p1, p2), sp.getVertices(), TEST_PRECISION);

        Assert.assertEquals(1, sp.getSize(), TEST_EPS);

        checkPlane(sp.getPlane(), Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z);

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(0, 1, 1), Vector3D.of(0, 1, 0), Vector3D.of(0, 1, -1),
                Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 0), Vector3D.of(0, 0, -1),
                Vector3D.of(0, -1, 1), Vector3D.of(0, -1, 0), Vector3D.of(0, -1, -1));

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(1, 1, -1),
                Vector3D.of(1, 0, 1), Vector3D.of(1, 0, -1),
                Vector3D.of(1, -1, 1), Vector3D.of(1, -1, 0), Vector3D.of(1, -1, -1));

        checkPoints(sp, RegionLocation.BOUNDARY,
                Vector3D.of(1, 1, 1), Vector3D.of(1, 1, 0),
                Vector3D.of(1, 0, 0));

        checkPoints(sp, RegionLocation.INSIDE, Vector3D.of(1, 0.5, 0.5));

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(2, 1, 1), Vector3D.of(2, 1, 0), Vector3D.of(2, 1, -1),
                Vector3D.of(2, 0, 1), Vector3D.of(2, 0, 0), Vector3D.of(2, 0, -1),
                Vector3D.of(2, -1, 1), Vector3D.of(2, -1, 0), Vector3D.of(2, -1, -1));
    }

    @Test
    public void testConvexPolygonFromVertices_duplicatePoints() {
        // arrange
        Vector3D p0 = Vector3D.of(1, 0, 0);
        Vector3D p1 = Vector3D.of(1, 1, 0);
        Vector3D p2 = Vector3D.of(1, 1, 2);
        Vector3D p3 = Vector3D.of(1, 0, 2);

        // act
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    p0,
                    Vector3D.of(1, 1e-15, 0),
                    p1,
                    p2,
                    p3,
                    Vector3D.of(1, 1e-15, 2),
                    Vector3D.of(1, 0, 1e-15)
                ), TEST_PRECISION);

        // assert
        Assert.assertTrue(sp instanceof VertexListConvexPolygon3D);

        Assert.assertFalse(sp.isFull());
        Assert.assertFalse(sp.isEmpty());
        Assert.assertTrue(sp.isFinite());

        Assert.assertEquals(4, sp.getVertices().size());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p0, p1, p2, p3), sp.getVertices(), TEST_PRECISION);

        Assert.assertEquals(2, sp.getSize(), TEST_EPS);

        checkPlane(sp.getPlane(), Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z);

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(0, 1, 1), Vector3D.of(0, 1, 0), Vector3D.of(0, 1, -1),
                Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 0), Vector3D.of(0, 0, -1),
                Vector3D.of(0, -1, 1), Vector3D.of(0, -1, 0), Vector3D.of(0, -1, -1));

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(1, 1, -1),
                Vector3D.of(1, -1, 1), Vector3D.of(1, 0, -1),
                Vector3D.of(1, -1, 0), Vector3D.of(1, -1, -1));

        checkPoints(sp, RegionLocation.BOUNDARY,
                Vector3D.of(1, 1, 1), Vector3D.of(1, 1, 0),
                Vector3D.of(1, 0, 0), Vector3D.of(1, 0, 2));

        checkPoints(sp, RegionLocation.INSIDE, Vector3D.of(1, 0.5, 1));

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(2, 1, 1), Vector3D.of(2, 1, 0), Vector3D.of(2, 1, -1),
                Vector3D.of(2, 0, 1), Vector3D.of(2, 0, 0), Vector3D.of(2, 0, -1),
                Vector3D.of(2, -1, 1), Vector3D.of(2, -1, 0), Vector3D.of(2, -1, -1));
    }

    @Test
    public void testConvexPolygonFromVertices_nonPlanar() {
        // arrange
        Pattern nonPlanarPattern = Pattern.compile("Points do not define a plane.*");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.convexPolygonFromVertices(Arrays.asList(), TEST_PRECISION);
        }, IllegalArgumentException.class, nonPlanarPattern);

        GeometryTestUtils.assertThrows(() -> {
            Planes.convexPolygonFromVertices(Arrays.asList(Vector3D.ZERO), TEST_PRECISION);
        }, IllegalArgumentException.class, nonPlanarPattern);

        GeometryTestUtils.assertThrows(() -> {
            Planes.convexPolygonFromVertices(Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0)), TEST_PRECISION);
        }, IllegalArgumentException.class, nonPlanarPattern);

        GeometryTestUtils.assertThrows(() -> {
            Planes.convexPolygonFromVertices(
                    Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1e-15, 0)), TEST_PRECISION);
        }, IllegalArgumentException.class, nonPlanarPattern);

        GeometryTestUtils.assertThrows(() -> {
            Planes.convexPolygonFromVertices(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.of(1, 0, 1),
                        Vector3D.of(1, 1, 0),
                        Vector3D.of(0, 1, 1)
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class, nonPlanarPattern);
    }

    @Test
    public void testConvexPolygonFromVertices_nonConvex() {
        // arrange
        Pattern nonConvexPattern = Pattern.compile("Points do not define a convex region.*");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.convexPolygonFromVertices(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.of(2, 0, 0),
                        Vector3D.of(2, 2, 0),
                        Vector3D.of(1, 1, 0),
                        Vector3D.of(1.5, 1, 0)
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class, nonConvexPattern);
    }

    @Test
    public void testTriangleFromVertices() {
        // act
        Triangle3D tri = Planes.triangleFromVertices(
                Vector3D.of(1, 1, 1),
                Vector3D.of(2, 1, 1),
                Vector3D.of(2, 1, 2), TEST_PRECISION);

        // assert
        Assert.assertEquals(0.5, tri.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(5.0 / 3.0, 1, 4.0 / 3.0),
                tri.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testTriangleFromVertices_degenerateTriangles() {
        // arrange
        Pattern msg = Pattern.compile("^Points do not define a plane.*");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.triangleFromVertices(
                        Vector3D.ZERO,
                        Vector3D.of(1e-11, 0, 0),
                        Vector3D.of(0, 1e-11, 0),
                        TEST_PRECISION);
        }, IllegalArgumentException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            Planes.triangleFromVertices(
                        Vector3D.ZERO,
                        Vector3D.of(1, 0, 0),
                        Vector3D.of(2, 0, 0),
                        TEST_PRECISION);
        }, IllegalArgumentException.class, msg);
    }

    @Test
    public void testIndexedTriangles_singleTriangle_noFaces() {
        // act
        List<Triangle3D> tris = Planes.indexedTriangles(new Vector3D[0], new int[0][], TEST_PRECISION);

        // assert
        Assert.assertEquals(0, tris.size());
    }

    @Test
    public void testIndexedTriangles_singleTriangle() {
        // arrange
        Vector3D[] vertices = {
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(1, 1, 0)
        };

        int[][] faceIndices = {
            {0, 2, 1}
        };

        // act
        List<Triangle3D> tris = Planes.indexedTriangles(Arrays.asList(vertices), faceIndices, TEST_PRECISION);

        // assert
        Assert.assertEquals(1, tris.size());

        Triangle3D a = tris.get(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, a.getPlane().getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, a.getPoint1(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 0), a.getPoint2(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), a.getPoint3(), TEST_EPS);
    }

    @Test
    public void testIndexedTriangles_multipleTriangles() {
        // arrange
        // define a square pyramind
        Vector3D[] vertices = {
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(1, 1, 0),
            Vector3D.of(0, 1, 0),
            Vector3D.of(0.5, 0.5, 4)
        };

        int[][] faceIndices = {
            {0, 2, 1},
            {0, 3, 2},
            {0, 1, 4},
            {1, 2, 4},
            {2, 3, 4},
            {3, 0, 4}
        };

        // act
        List<Triangle3D> tris = Planes.indexedTriangles(vertices, faceIndices, TEST_PRECISION);

        // assert
        Assert.assertEquals(6, tris.size());

        RegionBSPTree3D tree = RegionBSPTree3D.from(tris);
        Assert.assertEquals(4 / 3.0, tree.getSize(), TEST_EPS);

        Bounds3D bounds = tree.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 4), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testIndexedTriangles_invalidArgs() {
        // arrange
        Vector3D[] vertices = {
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(1, 1, 0),
            Vector3D.of(2, 0, 0)
        };

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.indexedTriangles(vertices, new int[][] {
                {0}
            }, TEST_PRECISION);
        }, IllegalArgumentException.class,
                "Invalid number of vertex indices for face at index 0: expected 3 but found 1");

        GeometryTestUtils.assertThrows(() -> {
            Planes.indexedTriangles(vertices, new int[][] {
                {0, 1, 2, 0}
            }, TEST_PRECISION);
        }, IllegalArgumentException.class,
                "Invalid number of vertex indices for face at index 0: expected 3 but found 4");

        GeometryTestUtils.assertThrows(() -> {
            Planes.indexedTriangles(new ArrayList<>(Arrays.asList(vertices)), new int[][] {
                {0, 1, 3}
            }, TEST_PRECISION);
        }, IllegalArgumentException.class, Pattern.compile("^Points do not define a plane: .*"));

        GeometryTestUtils.assertThrows(() -> {
            Planes.indexedTriangles(vertices, new int[][] {
                {0, 1, 10}
            }, TEST_PRECISION);
        }, IndexOutOfBoundsException.class);

        GeometryTestUtils.assertThrows(() -> {
            Planes.indexedTriangles(new ArrayList<>(Arrays.asList(vertices)), new int[][] {
                {0, 1, 10}
            }, TEST_PRECISION);
        }, IndexOutOfBoundsException.class);
    }

    @Test
    public void testIndexedConvexPolygons_singleTriangle_noFaces() {
        // act
        List<ConvexPolygon3D> polys = Planes.indexedConvexPolygons(new Vector3D[0], new int[0][], TEST_PRECISION);

        // assert
        Assert.assertEquals(0, polys.size());
    }

    @Test
    public void testIndexedConvexPolygons_singleSquare() {
        // arrange
        Vector3D[] vertices = {
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(1, 1, 0),
            Vector3D.of(0, 1, 0)
        };

        int[][] faceIndices = {
            {0, 3, 2, 1}
        };

        // act
        List<ConvexPolygon3D> polys = Planes.indexedConvexPolygons(Arrays.asList(vertices), faceIndices,
                TEST_PRECISION);

        // assert
        Assert.assertEquals(1, polys.size());

        ConvexPolygon3D a = polys.get(0);
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(
                    Vector3D.ZERO,
                    Vector3D.of(0, 1, 0),
                    Vector3D.of(1, 1, 0),
                    Vector3D.of(1, 0, 0)
                ), a.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testIndexedConvexPolygons_mixedPolygons() {
        // arrange
        // define a square pyramind
        Vector3D[] vertices = {
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(1, 1, 0),
            Vector3D.of(0, 1, 0),
            Vector3D.of(0.5, 0.5, 4)
        };

        int[][] faceIndices = {
            {0, 3, 2, 1},
            {0, 1, 4},
            {1, 2, 4},
            {2, 3, 4},
            {3, 0, 4}
        };

        // act
        List<ConvexPolygon3D> polys = Planes.indexedConvexPolygons(vertices, faceIndices, TEST_PRECISION);

        // assert
        Assert.assertEquals(5, polys.size());

        RegionBSPTree3D tree = RegionBSPTree3D.from(polys);
        Assert.assertEquals(4 / 3.0, tree.getSize(), TEST_EPS);

        Bounds3D bounds = tree.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 4), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testIndexedConvexPolygons_cube() {
        // arrange
        Vector3D[] vertices = {
            Vector3D.of(-0.5, -0.5, -0.5),
            Vector3D.of(0.5, -0.5, -0.5),
            Vector3D.of(0.5, 0.5, -0.5),
            Vector3D.of(-0.5, 0.5, -0.5),

            Vector3D.of(-0.5, -0.5, 0.5),
            Vector3D.of(0.5, -0.5, 0.5),
            Vector3D.of(0.5, 0.5, 0.5),
            Vector3D.of(-0.5, 0.5, 0.5)
        };

        int[][] faceIndices = {
            {0, 4, 7, 3},
            {1, 2, 6, 5},
            {0, 1, 5, 4},
            {3, 7, 6, 2},
            {0, 3, 2, 1},
            {4, 5, 6, 7}
        };

        // act
        List<ConvexPolygon3D> polys = Planes.indexedConvexPolygons(Arrays.asList(vertices), faceIndices,
                TEST_PRECISION);

        // assert
        Assert.assertEquals(6, polys.size());

        RegionBSPTree3D tree = RegionBSPTree3D.from(polys);
        Assert.assertEquals(1.0, tree.getSize(), TEST_EPS);

        Bounds3D bounds = tree.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.5, -0.5, -0.5), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testIndexedConvexPolygons_invalidArgs() {
        // arrange
        Vector3D[] vertices = {
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(1, 1, 0),
            Vector3D.of(2, 0, 0)
        };

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.indexedConvexPolygons(vertices, new int[][] {
                {0}
            }, TEST_PRECISION);
        }, IllegalArgumentException.class,
                "Invalid number of vertex indices for face at index 0: required at least 3 but found 1");

        GeometryTestUtils.assertThrows(() -> {
            Planes.indexedConvexPolygons(new ArrayList<>(Arrays.asList(vertices)), new int[][] {
                {0, 1, 3}
            }, TEST_PRECISION);
        }, IllegalArgumentException.class, Pattern.compile("^Points do not define a plane: .*"));

        GeometryTestUtils.assertThrows(() -> {
            Planes.indexedConvexPolygons(vertices, new int[][] {
                {0, 1, 10}
            }, TEST_PRECISION);
        }, IndexOutOfBoundsException.class);

        GeometryTestUtils.assertThrows(() -> {
            Planes.indexedConvexPolygons(new ArrayList<>(Arrays.asList(vertices)), new int[][] {
                {0, 1, 10}
            }, TEST_PRECISION);
        }, IndexOutOfBoundsException.class);
    }

    @Test
    public void testConvexPolygonToTriangleFan_threeVertices() {
        // arrange
        Plane plane = Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Vector3D p1 = Vector3D.ZERO;
        Vector3D p2 = Vector3D.of(1, 0, 0);
        Vector3D p3 = Vector3D.of(0, 1, 0);

        // act
        List<Triangle3D> tris = Planes.convexPolygonToTriangleFan(plane, Arrays.asList(p1, p2, p3));

        // assert
        Assert.assertEquals(1, tris.size());

        Triangle3D a = tris.get(0);
        Assert.assertSame(plane, a.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p1, p2, p3), a.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testConvexPolygonToTriangleFan_fourVertices() {
        // arrange
        Plane plane = Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Vector3D p1 = Vector3D.ZERO;
        Vector3D p2 = Vector3D.of(1, 0, 0);
        Vector3D p3 = Vector3D.of(1, 1, 0);
        Vector3D p4 = Vector3D.of(0, 1, 0);

        // act
        List<Triangle3D> tris = Planes.convexPolygonToTriangleFan(plane, Arrays.asList(p1, p2, p3, p4));

        // assert
        Assert.assertEquals(2, tris.size());

        Triangle3D a = tris.get(0);
        Assert.assertSame(plane, a.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p1, p2, p3), a.getVertices(), TEST_PRECISION);

        Triangle3D b = tris.get(1);
        Assert.assertSame(plane, b.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p1, p3, p4), b.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testConvexPolygonToTriangleFan_fourVertices_chooseLargestInteriorAngleForBase() {
        // arrange
        Plane plane = Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Vector3D p1 = Vector3D.ZERO;
        Vector3D p2 = Vector3D.of(1, 0, 0);
        Vector3D p3 = Vector3D.of(2, 1, 0);
        Vector3D p4 = Vector3D.of(1.5, 1, 0);

        // act
        List<Triangle3D> tris = Planes.convexPolygonToTriangleFan(plane, Arrays.asList(p1, p2, p3, p4));

        // assert
        Assert.assertEquals(2, tris.size());

        Triangle3D a = tris.get(0);
        Assert.assertSame(plane, a.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p4, p1, p2), a.getVertices(), TEST_PRECISION);

        Triangle3D b = tris.get(1);
        Assert.assertSame(plane, b.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p4, p2, p3), b.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testConvexPolygonToTriangleFan_fourVertices_distancesLessThanPrecision() {
        // This test checks that the triangle fan algorithm is not affected by the distances between
        // the vertices, just as long as the points are not exactly equal. Callers are responsible for
        // ensuring that the points are actually distinct according to the relevant precision context.

        // arrange
        Plane plane = Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Vector3D p1 = Vector3D.ZERO;
        Vector3D p2 = Vector3D.of(1e-20, 0, 0);
        Vector3D p3 = Vector3D.of(1e-20, 1e-20, 0);
        Vector3D p4 = Vector3D.of(0, 1e-20, 0);

        // act
        List<Triangle3D> tris = Planes.convexPolygonToTriangleFan(plane, Arrays.asList(p1, p2, p3, p4));

        // assert
        Assert.assertEquals(2, tris.size());

        Triangle3D a = tris.get(0);
        Assert.assertSame(plane, a.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p1, p2, p3), a.getVertices(), TEST_PRECISION);

        Triangle3D b = tris.get(1);
        Assert.assertSame(plane, b.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p1, p3, p4), b.getVertices(), TEST_PRECISION);
    }


    @Test
    public void testConvexPolygonToTriangleFan_sixVertices() {
        // arrange
        Plane plane = Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Vector3D p1 = Vector3D.ZERO;
        Vector3D p2 = Vector3D.of(1, -1, 0);
        Vector3D p3 = Vector3D.of(1.5, -1, 0);
        Vector3D p4 = Vector3D.of(5, 0, 0);
        Vector3D p5 = Vector3D.of(3, 1, 0);
        Vector3D p6 = Vector3D.of(0.5, 1, 0);

        // act
        List<Triangle3D> tris = Planes.convexPolygonToTriangleFan(plane, Arrays.asList(p1, p2, p3, p4, p5, p6));

        // assert
        Assert.assertEquals(4, tris.size());

        Triangle3D a = tris.get(0);
        Assert.assertSame(plane, a.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p3, p4, p5), a.getVertices(), TEST_PRECISION);

        Triangle3D b = tris.get(1);
        Assert.assertSame(plane, b.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p3, p5, p6), b.getVertices(), TEST_PRECISION);

        Triangle3D c = tris.get(2);
        Assert.assertSame(plane, c.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p3, p6, p1), c.getVertices(), TEST_PRECISION);

        Triangle3D d = tris.get(3);
        Assert.assertSame(plane, d.getPlane());
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p3, p1, p2), d.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testConvexPolygonToTriangleFan_notEnoughVertices() {
        // arrange
        String baseMsg = "Cannot create triangle fan: 3 or more vertices are required but found only ";
        Plane plane = Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.convexPolygonToTriangleFan(plane, Collections.emptyList());
        }, IllegalArgumentException.class, baseMsg + "0");

        GeometryTestUtils.assertThrows(() -> {
            Planes.convexPolygonToTriangleFan(plane, Arrays.asList(Vector3D.ZERO));
        }, IllegalArgumentException.class, baseMsg + "1");

        GeometryTestUtils.assertThrows(() -> {
            Planes.convexPolygonToTriangleFan(plane, Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0)));
        }, IllegalArgumentException.class, baseMsg + "2");
    }

    private static void checkPlane(Plane plane, Vector3D origin, Vector3D u, Vector3D v) {
        u = u.normalize();
        v = v.normalize();
        Vector3D w = u.cross(v);

        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getOrigin(), TEST_EPS);
        Assert.assertTrue(plane.contains(origin));

        EuclideanTestUtils.assertCoordinatesEqual(w, plane.getNormal(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getNormal().norm(), TEST_EPS);

        double offset = plane.getOriginOffset();
        Assert.assertEquals(Vector3D.ZERO.distance(plane.getOrigin()), Math.abs(offset), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getNormal().multiply(-offset), TEST_EPS);
    }

    private static void checkPoints(PlaneConvexSubset sp, RegionLocation loc, Vector3D... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, sp.classify(pt));
        }
    }

    private static void assertConvexAreasEqual(ConvexArea a, ConvexArea b) {
        List<LineConvexSubset> aBoundaries = new ArrayList<>(a.getBoundaries());
        List<LineConvexSubset> bBoundaries = new ArrayList<>(b.getBoundaries());

        Assert.assertEquals(aBoundaries.size(), bBoundaries.size());

        for (LineConvexSubset aBoundary : aBoundaries) {
            if (!hasEquivalentSubLine(aBoundary, bBoundaries)) {
                Assert.fail("Failed to find equivalent subline for " + aBoundary);
            }
        }
    }

    private static boolean hasEquivalentSubLine(LineConvexSubset target, Collection<LineConvexSubset> subsets) {
        Line line = target.getLine();
        double start = target.getSubspaceStart();
        double end = target.getSubspaceEnd();

        for (LineConvexSubset subset : subsets) {
            if (line.eq(subset.getLine(), TEST_PRECISION) &&
                    TEST_PRECISION.eq(start, subset.getSubspaceStart()) &&
                    TEST_PRECISION.eq(end, subset.getSubspaceEnd())) {
                return true;
            }
        }

        return false;
    }
}
