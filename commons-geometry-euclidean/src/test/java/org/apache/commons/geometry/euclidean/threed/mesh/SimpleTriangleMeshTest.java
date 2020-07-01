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
package org.apache.commons.geometry.euclidean.threed.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.junit.Assert;
import org.junit.Test;

public class SimpleTriangleMeshTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFrom_verticesAndFaces() {
        // arrange
        Vector3D[] vertices = {
            Vector3D.ZERO,
            Vector3D.of(1, 1, 0),
            Vector3D.of(1, 1, 1),
            Vector3D.of(0, 0, 1)
        };

        int[][] faceIndices = new int[][] {
            {0, 1, 2},
            {0, 2, 3}
        };

        // act
        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faceIndices, TEST_PRECISION);

        // assert
        Assert.assertEquals(4, mesh.getVertexCount());
        Assert.assertEquals(Arrays.asList(vertices), mesh.getVertices());

        Assert.assertEquals(2, mesh.getFaceCount());

        List<TriangleMesh.Face> faces = mesh.getFaces();
        Assert.assertEquals(2, faces.size());

        TriangleMesh.Face f1 = faces.get(0);
        Assert.assertEquals(0, f1.getIndex());
        Assert.assertArrayEquals(new int[] {0, 1, 2}, f1.getVertexIndices());
        Assert.assertSame(vertices[0], f1.getPoint1());
        Assert.assertSame(vertices[1], f1.getPoint2());
        Assert.assertSame(vertices[2], f1.getPoint3());
        Assert.assertEquals(Arrays.asList(vertices[0], vertices[1], vertices[2]), f1.getVertices());
        Assert.assertTrue(f1.definesPolygon());

        Triangle3D t1 = f1.getPolygon();
        Assert.assertEquals(Arrays.asList(vertices[0], vertices[1], vertices[2]), t1.getVertices());

        TriangleMesh.Face f2 = faces.get(1);
        Assert.assertEquals(1, f2.getIndex());
        Assert.assertArrayEquals(new int[] {0, 2, 3}, f2.getVertexIndices());
        Assert.assertSame(vertices[0], f2.getPoint1());
        Assert.assertSame(vertices[2], f2.getPoint2());
        Assert.assertSame(vertices[3], f2.getPoint3());
        Assert.assertEquals(Arrays.asList(vertices[0], vertices[2], vertices[3]), f2.getVertices());
        Assert.assertTrue(f2.definesPolygon());

        Triangle3D t2 = f2.getPolygon();
        Assert.assertEquals(Arrays.asList(vertices[0], vertices[2], vertices[3]), t2.getVertices());

        Bounds3D bounds = mesh.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), bounds.getMax(), TEST_EPS);

        Assert.assertSame(TEST_PRECISION, mesh.getPrecision());
    }

    @Test
    public void testFrom_verticesAndFaces_empty() {
        // arrange
        Vector3D[] vertices = {};

        int[][] faceIndices = new int[][] {};

        // act
        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faceIndices, TEST_PRECISION);

        // assert
        Assert.assertEquals(0, mesh.getVertexCount());
        Assert.assertEquals(0, mesh.getVertices().size());

        Assert.assertEquals(0, mesh.getFaceCount());
        Assert.assertEquals(0, mesh.getFaces().size());

        Assert.assertNull(mesh.getBounds());

        Assert.assertTrue(mesh.toTree().isEmpty());
    }

    @Test
    public void testFrom_boundarySource() {
        // arrange
        BoundarySource3D src = Parallelepiped.axisAligned(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(src, TEST_PRECISION);

        // assert
        Assert.assertEquals(8, mesh.getVertexCount());

        Vector3D p1 = Vector3D.of(0, 0, 0);
        Vector3D p2 = Vector3D.of(0, 0, 1);
        Vector3D p3 = Vector3D.of(0, 1, 0);
        Vector3D p4 = Vector3D.of(0, 1, 1);

        Vector3D p5 = Vector3D.of(1, 0, 0);
        Vector3D p6 = Vector3D.of(1, 0, 1);
        Vector3D p7 = Vector3D.of(1, 1, 0);
        Vector3D p8 = Vector3D.of(1, 1, 1);

        List<Vector3D> vertices = mesh.getVertices();
        Assert.assertEquals(8, vertices.size());

        Assert.assertTrue(vertices.contains(p1));
        Assert.assertTrue(vertices.contains(p2));
        Assert.assertTrue(vertices.contains(p3));
        Assert.assertTrue(vertices.contains(p4));
        Assert.assertTrue(vertices.contains(p5));
        Assert.assertTrue(vertices.contains(p6));
        Assert.assertTrue(vertices.contains(p7));
        Assert.assertTrue(vertices.contains(p8));

        Assert.assertEquals(12, mesh.getFaceCount());

        RegionBSPTree3D tree = mesh.toTree();

        Assert.assertEquals(1, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getCentroid(), TEST_EPS);

        Assert.assertSame(TEST_PRECISION, mesh.getPrecision());
    }

    @Test
    public void testFrom_boundarySource_empty() {
        // act
        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(BoundarySource3D.from(Collections.emptyList()),
                TEST_PRECISION);

        // assert
        Assert.assertEquals(0, mesh.getVertexCount());
        Assert.assertEquals(0, mesh.getVertices().size());

        Assert.assertEquals(0, mesh.getFaceCount());
        Assert.assertEquals(0, mesh.getFaces().size());

        Assert.assertNull(mesh.getBounds());

        Assert.assertTrue(mesh.toTree().isEmpty());
    }

    @Test
    public void testVertices_iterable() {
        // arrange
        List<Vector3D> vertices = Arrays.asList(
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(0, 1, 0)
        );

        List<int[]> faceIndices = Arrays.asList(
            new int[] {0, 1, 2}
        );

        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faceIndices, TEST_PRECISION);

        // act
        List<Vector3D> result = new ArrayList<>();
        mesh.vertices().forEach(result::add);

        // assert
        Assert.assertEquals(vertices, result);
    }

    @Test
    public void testFaces_iterable() {
        // arrange
        List<Vector3D> vertices = Arrays.asList(
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(0, 1, 0),
            Vector3D.of(0, 0, 1)
        );

        List<int[]> faceIndices = Arrays.asList(
            new int[] {0, 1, 2},
            new int[] {0, 2, 3}
        );

        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faceIndices, TEST_PRECISION);

        // act
        List<TriangleMesh.Face> result = new ArrayList<>();
        mesh.faces().forEach(result::add);

        // assert
        Assert.assertEquals(2, result.size());

        TriangleMesh.Face f1 = result.get(0);
        Assert.assertEquals(0, f1.getIndex());
        Assert.assertArrayEquals(new int[] {0, 1, 2}, f1.getVertexIndices());
        Assert.assertSame(vertices.get(0), f1.getPoint1());
        Assert.assertSame(vertices.get(1), f1.getPoint2());
        Assert.assertSame(vertices.get(2), f1.getPoint3());
        Assert.assertEquals(Arrays.asList(vertices.get(0), vertices.get(1), vertices.get(2)), f1.getVertices());
        Assert.assertTrue(f1.definesPolygon());

        TriangleMesh.Face f2 = result.get(1);
        Assert.assertEquals(1, f2.getIndex());
        Assert.assertArrayEquals(new int[] {0, 2, 3}, f2.getVertexIndices());
        Assert.assertSame(vertices.get(0), f2.getPoint1());
        Assert.assertSame(vertices.get(2), f2.getPoint2());
        Assert.assertSame(vertices.get(3), f2.getPoint3());
        Assert.assertEquals(Arrays.asList(vertices.get(0), vertices.get(2), vertices.get(3)), f2.getVertices());
        Assert.assertTrue(f2.definesPolygon());
    }

    @Test
    public void testTriangleStream() {
        // arrange
        List<Vector3D> vertices = Arrays.asList(
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(0, 1, 0),
            Vector3D.of(0, 0, 1)
        );

        List<int[]> faceIndices = Arrays.asList(
            new int[] {0, 1, 2},
            new int[] {0, 2, 3}
        );

        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faceIndices, TEST_PRECISION);

        // act
        List<Triangle3D> tris = mesh.triangleStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(2, tris.size());

        Triangle3D t1 = tris.get(0);
        Assert.assertSame(vertices.get(0), t1.getPoint1());
        Assert.assertSame(vertices.get(1), t1.getPoint2());
        Assert.assertSame(vertices.get(2), t1.getPoint3());

        Triangle3D t2 = tris.get(1);
        Assert.assertSame(vertices.get(0), t2.getPoint1());
        Assert.assertSame(vertices.get(2), t2.getPoint2());
        Assert.assertSame(vertices.get(3), t2.getPoint3());
    }

    @Test
    public void testToTriangleMesh() {
        // arrange
        DoublePrecisionContext precision1 = new EpsilonDoublePrecisionContext(1e-1);
        DoublePrecisionContext precision2 = new EpsilonDoublePrecisionContext(1e-2);
        DoublePrecisionContext precision3 = new EpsilonDoublePrecisionContext(1e-1);

        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(Parallelepiped.unitCube(TEST_PRECISION), precision1);

        // act/assert
        Assert.assertSame(mesh, mesh.toTriangleMesh(precision1));

        SimpleTriangleMesh other = mesh.toTriangleMesh(precision2);
        Assert.assertSame(precision2, other.getPrecision());
        Assert.assertEquals(mesh.getVertices(), other.getVertices());
        Assert.assertEquals(12, other.getFaceCount());
        for (int i = 0; i < 12; ++i) {
            Assert.assertArrayEquals(mesh.getFace(i).getVertexIndices(), other.getFace(i).getVertexIndices());
        }

        Assert.assertSame(mesh, mesh.toTriangleMesh(precision3));
    }

    @Test
    public void testFace_doesNotDefineTriangle() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        Vector3D[] vertices = new Vector3D[] {
            Vector3D.ZERO,
            Vector3D.of(0.01, -0.01, 0.01),
            Vector3D.of(0.01, 0.01, 0.01),
            Vector3D.of(1, 0, 0),
            Vector3D.of(2, 0.01, 0)
        };
        int[][] faces = new int[][] {
            {0, 1, 2},
            {0, 3, 4}
        };
        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faces, precision);

        // act/assert
        Pattern msgPattern = Pattern.compile("^Points do not define a plane: .*");

        Assert.assertFalse(mesh.getFace(0).definesPolygon());
        GeometryTestUtils.assertThrows(() -> {
            mesh.getFace(0).getPolygon();
        }, IllegalArgumentException.class, msgPattern);

        Assert.assertFalse(mesh.getFace(1).definesPolygon());
        GeometryTestUtils.assertThrows(() -> {
            mesh.getFace(1).getPolygon();
        }, IllegalArgumentException.class, msgPattern);
    }

    @Test
    public void testToTree_smallNumberOfFaces() {
        // arrange
        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(Parallelepiped.unitCube(TEST_PRECISION), TEST_PRECISION);

        // act
        RegionBSPTree3D tree = mesh.toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isInfinite());
        Assert.assertTrue(tree.isFinite());

        Assert.assertEquals(1, tree.getSize(), 1);
        Assert.assertEquals(6, tree.getBoundarySize(), 1);

        Assert.assertEquals(6, tree.getRoot().height());
    }

    @Test
    public void testToTree_largeNumberOfFaces() {
        // arrange
        // TODO
    }

    @Test
    public void testTransform() {
        // arrange
        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(Parallelepiped.unitCube(TEST_PRECISION), TEST_PRECISION);

        AffineTransformMatrix3D t = AffineTransformMatrix3D.createScale(1, 2, 3)
                .translate(0.5, 1, 1.5);

        // act
        SimpleTriangleMesh result = mesh.transform(t);

        // assert
        Assert.assertNotSame(mesh, result);

        Assert.assertEquals(8, result.getVertexCount());
        Assert.assertEquals(12, result.getFaceCount());

        Bounds3D resultBounds = result.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, resultBounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3), resultBounds.getMax(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 1, 1.5), result.toTree().getCentroid(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, mesh.toTree().getCentroid(), TEST_EPS);
    }

    @Test
    public void testTransform_empty() {
        // arrange
        SimpleTriangleMesh mesh = SimpleTriangleMesh.builder(TEST_PRECISION).build();

        AffineTransformMatrix3D t = AffineTransformMatrix3D.createScale(1, 2, 3);

        // act
        SimpleTriangleMesh result = mesh.transform(t);

        // assert
        Assert.assertEquals(0, result.getVertexCount());
        Assert.assertEquals(0, result.getFaceCount());

        Assert.assertNull(result.getBounds());
    }

    @Test
    public void testToString() {
        // arrange
        Triangle3D tri = Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                TEST_PRECISION);
        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(BoundarySource3D.from(tri), TEST_PRECISION);

        // act
        String str = mesh.toString();

        // assert
        GeometryTestUtils.assertContains("SimpleTriangleMesh[vertexCount= 3, faceCount= 1, bounds= Bounds3D[", str);
    }

    @Test
    public void testFaceToString() {
        // arrange
        Triangle3D tri = Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                TEST_PRECISION);
        SimpleTriangleMesh mesh = SimpleTriangleMesh.from(BoundarySource3D.from(tri), TEST_PRECISION);

        // act
        String str = mesh.getFace(0).toString();

        // assert
        GeometryTestUtils.assertContains("SimpleTriangleFace[index= 0, vertexIndices= [0, 1, 2], vertices= [(0", str);
    }

    @Test
    public void testBuilder_mixedBuildMethods() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(precision);

        // act
        builder.addVertices(Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0)));
        builder.useVertex(Vector3D.of(0, 0, 1));
        builder.addVertex(Vector3D.of(0, 1, 0));
        builder.useVertex(Vector3D.of(1, 1, 1));

        builder.addFace(0, 2, 1);
        builder.addFaceUsingVertices(Vector3D.of(0.5, 0, 0), Vector3D.of(1.01, 0, 0), Vector3D.of(1, 1, 0.95));

        SimpleTriangleMesh mesh = builder.build();

        // assert
        Assert.assertEquals(6, mesh.getVertexCount());
        Assert.assertEquals(2, mesh.getFaceCount());

        List<TriangleMesh.Face> faces = mesh.getFaces();
        Assert.assertEquals(2, faces.size());

        Assert.assertArrayEquals(new int[] {0, 2, 1},  faces.get(0).getVertexIndices());
        Assert.assertArrayEquals(new int[] {5, 1, 4},  faces.get(1).getVertexIndices());
    }

    @Test
    public void testBuilder_addVerticesAndFaces() {
        // act
        SimpleTriangleMesh mesh = SimpleTriangleMesh.builder(TEST_PRECISION)
            .addVertices(new Vector3D[] {
                Vector3D.ZERO,
                Vector3D.of(1, 1, 0),
                Vector3D.of(1, 1, 1),
                Vector3D.of(0, 0, 1)
            })
            .addFaces(new int[][] {
                {0, 1, 2},
                {0, 2, 3}
            })
            .build();

        // assert
        Assert.assertEquals(4, mesh.getVertexCount());
        Assert.assertEquals(2, mesh.getFaceCount());
    }

    @Test
    public void testBuilder_invalidFaceIndices() {
        // arrange
        SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(TEST_PRECISION);
        builder.useVertex(Vector3D.ZERO);
        builder.useVertex(Vector3D.of(1, 0, 0));
        builder.useVertex(Vector3D.of(0, 1, 0));

        String msgBase = "Invalid vertex index: ";

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.addFace(-1, 1, 2);
        }, IllegalArgumentException.class, msgBase + "-1");

        GeometryTestUtils.assertThrows(() -> {
            builder.addFace(0, 3, 2);
        }, IllegalArgumentException.class, msgBase + "3");

        GeometryTestUtils.assertThrows(() -> {
            builder.addFace(0, 1, 4);
        }, IllegalArgumentException.class, msgBase + "4");

        GeometryTestUtils.assertThrows(() -> {
            builder.addFaces(new int[][] {{-1, 1, 2}});
        }, IllegalArgumentException.class, msgBase + "-1");

        GeometryTestUtils.assertThrows(() -> {
            builder.addFaces(new int[][] {{0, 3, 2}});
        }, IllegalArgumentException.class, msgBase + "3");

        GeometryTestUtils.assertThrows(() -> {
            builder.addFaces(new int[][] {{0, 1, 4}});
        }, IllegalArgumentException.class, msgBase + "4");
    }

    @Test
    public void testBuilder_invalidFaceIndexCount() {
        // arrange
        SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(TEST_PRECISION);
        builder.useVertex(Vector3D.ZERO);
        builder.useVertex(Vector3D.of(1, 0, 0));
        builder.useVertex(Vector3D.of(0, 1, 0));
        builder.useVertex(Vector3D.of(0, 0, 1));

        String msgBase = "Face must contain 3 vertex indices; found ";

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.addFaces(new int[][] {{}});
        }, IllegalArgumentException.class, msgBase + "0");

        GeometryTestUtils.assertThrows(() -> {
            builder.addFaces(new int[][] {{0}});
        }, IllegalArgumentException.class, msgBase + "1");

        GeometryTestUtils.assertThrows(() -> {
            builder.addFaces(new int[][] {{0, 1}});
        }, IllegalArgumentException.class, msgBase + "2");

        GeometryTestUtils.assertThrows(() -> {
            builder.addFaces(new int[][] {{0, 1, 2, 3}});
        }, IllegalArgumentException.class, msgBase + "4");
    }

    @Test
    public void testBuilder_cannotModifyOnceBuilt() {
        // arrange
        SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(TEST_PRECISION)
            .addVertices(new Vector3D[] {
                Vector3D.ZERO,
                Vector3D.of(1, 1, 0),
                Vector3D.of(1, 1, 1),
            })
            .addFaces(new int[][] {
                {0, 1, 2}
            });
        builder.build();

        String msg = "Builder instance cannot be modified: mesh construction is complete";

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.useVertex(Vector3D.ZERO);
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            builder.addVertex(Vector3D.ZERO);
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            builder.addVertices(Arrays.asList(Vector3D.ZERO));
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            builder.addVertices(new Vector3D[] {Vector3D.ZERO});
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            builder.addFaceUsingVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0));
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            builder.addFace(0, 1, 2);
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            builder.addFaces(Arrays.asList(new int[] {0, 1, 2}));
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            builder.addFaces(new int[][] {{0, 1, 2}});
        }, IllegalStateException.class, msg);
    }

    @Test
    public void testBuilder_addFaceAndVertices_vs_addFaceUsingVertices() {
        // arrange
        SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(TEST_PRECISION);
        Vector3D p1 = Vector3D.ZERO;
        Vector3D p2 = Vector3D.of(1, 0, 0);
        Vector3D p3 = Vector3D.of(0, 1, 0);

        // act
        builder.addFaceUsingVertices(p1, p2, p3);
        builder.addFaceAndVertices(p1, p2, p3);
        builder.addFaceUsingVertices(p1, p2, p3);

        // assert
        Assert.assertEquals(6, builder.getVertexCount());
        Assert.assertEquals(3, builder.getFaceCount());

        SimpleTriangleMesh mesh = builder.build();

        Assert.assertEquals(6, mesh.getVertexCount());
        Assert.assertEquals(3, mesh.getFaceCount());

        TriangleMesh.Face f1 = mesh.getFace(0);
        Assert.assertArrayEquals(new int[] {0, 1, 2}, f1.getVertexIndices());

        TriangleMesh.Face f2 = mesh.getFace(1);
        Assert.assertArrayEquals(new int[] {3, 4, 5}, f2.getVertexIndices());

        TriangleMesh.Face f3 = mesh.getFace(2);
        Assert.assertArrayEquals(new int[] {0, 1, 2}, f3.getVertexIndices());
    }
}
