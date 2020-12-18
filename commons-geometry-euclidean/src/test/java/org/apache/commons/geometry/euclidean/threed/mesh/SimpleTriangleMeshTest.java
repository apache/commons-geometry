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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleTriangleMeshTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFrom_verticesAndFaces() {
        // arrange
        final Vector3D[] vertices = {
            Vector3D.ZERO,
            Vector3D.of(1, 1, 0),
            Vector3D.of(1, 1, 1),
            Vector3D.of(0, 0, 1)
        };

        final int[][] faceIndices = {{0, 1, 2}, {0, 2, 3}};

        // act
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faceIndices, TEST_PRECISION);

        // assert
        Assertions.assertEquals(4, mesh.getVertexCount());
        Assertions.assertEquals(Arrays.asList(vertices), mesh.getVertices());

        Assertions.assertEquals(2, mesh.getFaceCount());

        final List<TriangleMesh.Face> faces = mesh.getFaces();
        Assertions.assertEquals(2, faces.size());

        final TriangleMesh.Face f1 = faces.get(0);
        Assertions.assertEquals(0, f1.getIndex());
        Assertions.assertArrayEquals(new int[] {0, 1, 2}, f1.getVertexIndices());
        Assertions.assertSame(vertices[0], f1.getPoint1());
        Assertions.assertSame(vertices[1], f1.getPoint2());
        Assertions.assertSame(vertices[2], f1.getPoint3());
        Assertions.assertEquals(Arrays.asList(vertices[0], vertices[1], vertices[2]), f1.getVertices());
        Assertions.assertTrue(f1.definesPolygon());

        final Triangle3D t1 = f1.getPolygon();
        Assertions.assertEquals(Arrays.asList(vertices[0], vertices[1], vertices[2]), t1.getVertices());

        final TriangleMesh.Face f2 = faces.get(1);
        Assertions.assertEquals(1, f2.getIndex());
        Assertions.assertArrayEquals(new int[] {0, 2, 3}, f2.getVertexIndices());
        Assertions.assertSame(vertices[0], f2.getPoint1());
        Assertions.assertSame(vertices[2], f2.getPoint2());
        Assertions.assertSame(vertices[3], f2.getPoint3());
        Assertions.assertEquals(Arrays.asList(vertices[0], vertices[2], vertices[3]), f2.getVertices());
        Assertions.assertTrue(f2.definesPolygon());

        final Triangle3D t2 = f2.getPolygon();
        Assertions.assertEquals(Arrays.asList(vertices[0], vertices[2], vertices[3]), t2.getVertices());

        final Bounds3D bounds = mesh.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), bounds.getMax(), TEST_EPS);

        Assertions.assertSame(TEST_PRECISION, mesh.getPrecision());
    }

    @Test
    public void testFrom_verticesAndFaces_empty() {
        // arrange
        final Vector3D[] vertices = {};

        final int[][] faceIndices = {};

        // act
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faceIndices, TEST_PRECISION);

        // assert
        Assertions.assertEquals(0, mesh.getVertexCount());
        Assertions.assertEquals(0, mesh.getVertices().size());

        Assertions.assertEquals(0, mesh.getFaceCount());
        Assertions.assertEquals(0, mesh.getFaces().size());

        Assertions.assertNull(mesh.getBounds());

        Assertions.assertTrue(mesh.toTree().isEmpty());
    }

    @Test
    public void testFrom_boundarySource() {
        // arrange
        final BoundarySource3D src = Parallelepiped.axisAligned(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

        // act
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(src, TEST_PRECISION);

        // assert
        Assertions.assertEquals(8, mesh.getVertexCount());

        final Vector3D p1 = Vector3D.of(0, 0, 0);
        final Vector3D p2 = Vector3D.of(0, 0, 1);
        final Vector3D p3 = Vector3D.of(0, 1, 0);
        final Vector3D p4 = Vector3D.of(0, 1, 1);

        final Vector3D p5 = Vector3D.of(1, 0, 0);
        final Vector3D p6 = Vector3D.of(1, 0, 1);
        final Vector3D p7 = Vector3D.of(1, 1, 0);
        final Vector3D p8 = Vector3D.of(1, 1, 1);

        final List<Vector3D> vertices = mesh.getVertices();
        Assertions.assertEquals(8, vertices.size());

        Assertions.assertTrue(vertices.contains(p1));
        Assertions.assertTrue(vertices.contains(p2));
        Assertions.assertTrue(vertices.contains(p3));
        Assertions.assertTrue(vertices.contains(p4));
        Assertions.assertTrue(vertices.contains(p5));
        Assertions.assertTrue(vertices.contains(p6));
        Assertions.assertTrue(vertices.contains(p7));
        Assertions.assertTrue(vertices.contains(p8));

        Assertions.assertEquals(12, mesh.getFaceCount());

        final RegionBSPTree3D tree = mesh.toTree();

        Assertions.assertEquals(1, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getCentroid(), TEST_EPS);

        Assertions.assertSame(TEST_PRECISION, mesh.getPrecision());
    }

    @Test
    public void testFrom_boundarySource_empty() {
        // act
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(BoundarySource3D.of(Collections.emptyList()),
                TEST_PRECISION);

        // assert
        Assertions.assertEquals(0, mesh.getVertexCount());
        Assertions.assertEquals(0, mesh.getVertices().size());

        Assertions.assertEquals(0, mesh.getFaceCount());
        Assertions.assertEquals(0, mesh.getFaces().size());

        Assertions.assertNull(mesh.getBounds());

        Assertions.assertTrue(mesh.toTree().isEmpty());
    }

    @Test
    public void testVertices_iterable() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(0, 1, 0)
        );

        final List<int[]> faceIndices = Collections.singletonList(new int[]{0, 1, 2});

        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faceIndices, TEST_PRECISION);

        // act
        final List<Vector3D> result = new ArrayList<>();
        mesh.vertices().forEach(result::add);

        // assert
        Assertions.assertEquals(vertices, result);
    }

    @Test
    public void testFaces_iterable() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(0, 1, 0),
            Vector3D.of(0, 0, 1)
        );

        final List<int[]> faceIndices = Arrays.asList(
            new int[] {0, 1, 2},
            new int[] {0, 2, 3}
        );

        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faceIndices, TEST_PRECISION);

        // act
        final List<TriangleMesh.Face> result = new ArrayList<>();
        mesh.faces().forEach(result::add);

        // assert
        Assertions.assertEquals(2, result.size());

        final TriangleMesh.Face f1 = result.get(0);
        Assertions.assertEquals(0, f1.getIndex());
        Assertions.assertArrayEquals(new int[] {0, 1, 2}, f1.getVertexIndices());
        Assertions.assertSame(vertices.get(0), f1.getPoint1());
        Assertions.assertSame(vertices.get(1), f1.getPoint2());
        Assertions.assertSame(vertices.get(2), f1.getPoint3());
        Assertions.assertEquals(Arrays.asList(vertices.get(0), vertices.get(1), vertices.get(2)), f1.getVertices());
        Assertions.assertTrue(f1.definesPolygon());

        final TriangleMesh.Face f2 = result.get(1);
        Assertions.assertEquals(1, f2.getIndex());
        Assertions.assertArrayEquals(new int[] {0, 2, 3}, f2.getVertexIndices());
        Assertions.assertSame(vertices.get(0), f2.getPoint1());
        Assertions.assertSame(vertices.get(2), f2.getPoint2());
        Assertions.assertSame(vertices.get(3), f2.getPoint3());
        Assertions.assertEquals(Arrays.asList(vertices.get(0), vertices.get(2), vertices.get(3)), f2.getVertices());
        Assertions.assertTrue(f2.definesPolygon());
    }

    @Test
    public void testFaces_iterator() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(0, 1, 0)
        );

        final List<int[]> faceIndices = Collections.singletonList(new int[]{0, 1, 2}
        );

        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faceIndices, TEST_PRECISION);

        // act/assert
        final Iterator<TriangleMesh.Face> it = mesh.faces().iterator();

        Assertions.assertTrue(it.hasNext());
        Assertions.assertEquals(0, it.next().getIndex());
        Assertions.assertFalse(it.hasNext());

        Assertions.assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    public void testTriangleStream() {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
            Vector3D.ZERO,
            Vector3D.of(1, 0, 0),
            Vector3D.of(0, 1, 0),
            Vector3D.of(0, 0, 1)
        );

        final List<int[]> faceIndices = Arrays.asList(
            new int[] {0, 1, 2},
            new int[] {0, 2, 3}
        );

        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faceIndices, TEST_PRECISION);

        // act
        final List<Triangle3D> tris = mesh.triangleStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(2, tris.size());

        final Triangle3D t1 = tris.get(0);
        Assertions.assertSame(vertices.get(0), t1.getPoint1());
        Assertions.assertSame(vertices.get(1), t1.getPoint2());
        Assertions.assertSame(vertices.get(2), t1.getPoint3());

        final Triangle3D t2 = tris.get(1);
        Assertions.assertSame(vertices.get(0), t2.getPoint1());
        Assertions.assertSame(vertices.get(2), t2.getPoint2());
        Assertions.assertSame(vertices.get(3), t2.getPoint3());
    }

    @Test
    public void testToTriangleMesh() {
        // arrange
        final DoublePrecisionContext precision1 = new EpsilonDoublePrecisionContext(1e-1);
        final DoublePrecisionContext precision2 = new EpsilonDoublePrecisionContext(1e-2);
        final DoublePrecisionContext precision3 = new EpsilonDoublePrecisionContext(1e-1);

        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(Parallelepiped.unitCube(TEST_PRECISION), precision1);

        // act/assert
        Assertions.assertSame(mesh, mesh.toTriangleMesh(precision1));

        final SimpleTriangleMesh other = mesh.toTriangleMesh(precision2);
        Assertions.assertSame(precision2, other.getPrecision());
        Assertions.assertEquals(mesh.getVertices(), other.getVertices());
        Assertions.assertEquals(12, other.getFaceCount());
        for (int i = 0; i < 12; ++i) {
            Assertions.assertArrayEquals(mesh.getFace(i).getVertexIndices(), other.getFace(i).getVertexIndices());
        }

        Assertions.assertSame(mesh, mesh.toTriangleMesh(precision3));
    }

    @Test
    public void testFace_doesNotDefineTriangle() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        final Vector3D[] vertices = {
            Vector3D.ZERO,
            Vector3D.of(0.01, -0.01, 0.01),
            Vector3D.of(0.01, 0.01, 0.01),
            Vector3D.of(1, 0, 0),
            Vector3D.of(2, 0.01, 0)
        };
        final int[][] faces = {{0, 1, 2}, {0, 3, 4}};
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(vertices, faces, precision);

        // act/assert
        final Pattern msgPattern = Pattern.compile("^Points do not define a plane: .*");

        Assertions.assertFalse(mesh.getFace(0).definesPolygon());
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            mesh.getFace(0).getPolygon();
        }, IllegalArgumentException.class, msgPattern);

        Assertions.assertFalse(mesh.getFace(1).definesPolygon());
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            mesh.getFace(1).getPolygon();
        }, IllegalArgumentException.class, msgPattern);
    }

    @Test
    public void testToTree_smallNumberOfFaces() {
        // arrange
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(Parallelepiped.unitCube(TEST_PRECISION), TEST_PRECISION);

        // act
        final RegionBSPTree3D tree = mesh.toTree();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isInfinite());
        Assertions.assertTrue(tree.isFinite());

        Assertions.assertEquals(1, tree.getSize(), 1);
        Assertions.assertEquals(6, tree.getBoundarySize(), 1);

        Assertions.assertEquals(6, tree.getRoot().height());
    }

    @Test
    public void testTransform() {
        // arrange
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(Parallelepiped.unitCube(TEST_PRECISION), TEST_PRECISION);

        final AffineTransformMatrix3D t = AffineTransformMatrix3D.createScale(1, 2, 3)
                .translate(0.5, 1, 1.5);

        // act
        final SimpleTriangleMesh result = mesh.transform(t);

        // assert
        Assertions.assertNotSame(mesh, result);

        Assertions.assertEquals(8, result.getVertexCount());
        Assertions.assertEquals(12, result.getFaceCount());

        final Bounds3D resultBounds = result.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, resultBounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3), resultBounds.getMax(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 1, 1.5), result.toTree().getCentroid(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, mesh.toTree().getCentroid(), TEST_EPS);
    }

    @Test
    public void testTransform_empty() {
        // arrange
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.builder(TEST_PRECISION).build();

        final AffineTransformMatrix3D t = AffineTransformMatrix3D.createScale(1, 2, 3);

        // act
        final SimpleTriangleMesh result = mesh.transform(t);

        // assert
        Assertions.assertEquals(0, result.getVertexCount());
        Assertions.assertEquals(0, result.getFaceCount());

        Assertions.assertNull(result.getBounds());
    }

    @Test
    public void testToString() {
        // arrange
        final Triangle3D tri = Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                TEST_PRECISION);
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(BoundarySource3D.of(tri), TEST_PRECISION);

        // act
        final String str = mesh.toString();

        // assert
        GeometryTestUtils.assertContains("SimpleTriangleMesh[vertexCount= 3, faceCount= 1, bounds= Bounds3D[", str);
    }

    @Test
    public void testFaceToString() {
        // arrange
        final Triangle3D tri = Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                TEST_PRECISION);
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.from(BoundarySource3D.of(tri), TEST_PRECISION);

        // act
        final String str = mesh.getFace(0).toString();

        // assert
        GeometryTestUtils.assertContains("SimpleTriangleFace[index= 0, vertexIndices= [0, 1, 2], vertices= [(0", str);
    }

    @Test
    public void testBuilder_mixedBuildMethods() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        final SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(precision);

        // act
        builder.addVertices(Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0)));
        builder.useVertex(Vector3D.of(0, 0, 1));
        builder.addVertex(Vector3D.of(0, 1, 0));
        builder.useVertex(Vector3D.of(1, 1, 1));

        builder.addFace(0, 2, 1);
        builder.addFaceUsingVertices(Vector3D.of(0.5, 0, 0), Vector3D.of(1.01, 0, 0), Vector3D.of(1, 1, 0.95));

        final SimpleTriangleMesh mesh = builder.build();

        // assert
        Assertions.assertEquals(6, mesh.getVertexCount());
        Assertions.assertEquals(2, mesh.getFaceCount());

        final List<TriangleMesh.Face> faces = mesh.getFaces();
        Assertions.assertEquals(2, faces.size());

        Assertions.assertArrayEquals(new int[] {0, 2, 1},  faces.get(0).getVertexIndices());
        Assertions.assertArrayEquals(new int[] {5, 1, 4},  faces.get(1).getVertexIndices());
    }

    @Test
    public void testBuilder_addVerticesAndFaces() {
        // act
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.builder(TEST_PRECISION)
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
        Assertions.assertEquals(4, mesh.getVertexCount());
        Assertions.assertEquals(2, mesh.getFaceCount());
    }

    @Test
    public void testBuilder_invalidFaceIndices() {
        // arrange
        final SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(TEST_PRECISION);
        builder.useVertex(Vector3D.ZERO);
        builder.useVertex(Vector3D.of(1, 0, 0));
        builder.useVertex(Vector3D.of(0, 1, 0));

        final String msgBase = "Invalid vertex index: ";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFace(-1, 1, 2);
        }, IllegalArgumentException.class, msgBase + "-1");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFace(0, 3, 2);
        }, IllegalArgumentException.class, msgBase + "3");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFace(0, 1, 4);
        }, IllegalArgumentException.class, msgBase + "4");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFaces(new int[][] {{-1, 1, 2}});
        }, IllegalArgumentException.class, msgBase + "-1");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFaces(new int[][] {{0, 3, 2}});
        }, IllegalArgumentException.class, msgBase + "3");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFaces(new int[][] {{0, 1, 4}});
        }, IllegalArgumentException.class, msgBase + "4");
    }

    @Test
    public void testBuilder_invalidFaceIndexCount() {
        // arrange
        final SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(TEST_PRECISION);
        builder.useVertex(Vector3D.ZERO);
        builder.useVertex(Vector3D.of(1, 0, 0));
        builder.useVertex(Vector3D.of(0, 1, 0));
        builder.useVertex(Vector3D.of(0, 0, 1));

        final String msgBase = "Face must contain 3 vertex indices; found ";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFaces(new int[][] {{}});
        }, IllegalArgumentException.class, msgBase + "0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFaces(new int[][] {{0}});
        }, IllegalArgumentException.class, msgBase + "1");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFaces(new int[][] {{0, 1}});
        }, IllegalArgumentException.class, msgBase + "2");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFaces(new int[][] {{0, 1, 2, 3}});
        }, IllegalArgumentException.class, msgBase + "4");
    }

    @Test
    public void testBuilder_cannotModifyOnceBuilt() {
        // arrange
        final SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(TEST_PRECISION)
            .addVertices(new Vector3D[] {
                Vector3D.ZERO,
                Vector3D.of(1, 1, 0),
                Vector3D.of(1, 1, 1),
            })
            .addFaces(new int[][] {
                {0, 1, 2}
            });
        builder.build();

        final String msg = "Builder instance cannot be modified: mesh construction is complete";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.useVertex(Vector3D.ZERO);
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addVertex(Vector3D.ZERO);
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addVertices(Collections.singletonList(Vector3D.ZERO));
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addVertices(new Vector3D[] {Vector3D.ZERO});
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFaceUsingVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0));
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFace(0, 1, 2);
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFaces(Collections.singletonList(new int[]{0, 1, 2}));
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.addFaces(new int[][] {{0, 1, 2}});
        }, IllegalStateException.class, msg);
    }

    @Test
    public void testBuilder_addFaceAndVertices_vs_addFaceUsingVertices() {
        // arrange
        final SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(TEST_PRECISION);
        final Vector3D p1 = Vector3D.ZERO;
        final Vector3D p2 = Vector3D.of(1, 0, 0);
        final Vector3D p3 = Vector3D.of(0, 1, 0);

        // act
        builder.addFaceUsingVertices(p1, p2, p3);
        builder.addFaceAndVertices(p1, p2, p3);
        builder.addFaceUsingVertices(p1, p2, p3);

        // assert
        Assertions.assertEquals(6, builder.getVertexCount());
        Assertions.assertEquals(3, builder.getFaceCount());

        final SimpleTriangleMesh mesh = builder.build();

        Assertions.assertEquals(6, mesh.getVertexCount());
        Assertions.assertEquals(3, mesh.getFaceCount());

        final TriangleMesh.Face f1 = mesh.getFace(0);
        Assertions.assertArrayEquals(new int[] {0, 1, 2}, f1.getVertexIndices());

        final TriangleMesh.Face f2 = mesh.getFace(1);
        Assertions.assertArrayEquals(new int[] {3, 4, 5}, f2.getVertexIndices());

        final TriangleMesh.Face f3 = mesh.getFace(2);
        Assertions.assertArrayEquals(new int[] {0, 1, 2}, f3.getVertexIndices());
    }
}
