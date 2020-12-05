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
package org.apache.commons.geometry.examples.io.threed.obj;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URL;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OBJReaderTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final String CUBE_MINUS_SPHERE_MODEL = "/models/cube-minus-sphere.obj";

    private static final int CUBE_MINUS_SPHERE_VERTICES = 1688;

    private static final int CUBE_MINUS_SPHERE_FACES = 728;

    private OBJReader reader = new OBJReader();

    @Test
    public void testReadMesh_emptyInput() throws Exception {
        // act
        TriangleMesh mesh = reader.readTriangleMesh(new StringReader(""), TEST_PRECISION);

        // assert
        Assertions.assertEquals(0, mesh.getVertexCount());
        Assertions.assertEquals(0, mesh.getFaceCount());
    }

    @Test
    public void testReadMesh_mixedVertexIndexTypesAndWhitespace() throws Exception {
        // arrange
        String input =
            "#some comments  \n\r\n \n" +
            " # some other comments\n" +
            "v 0.0 0.0 0.0\n" +
            "v 1e-1 0 0 \r\n" +
            " v 0 1 0\n" +
            "\tv\t0 0 1\r\n" +
            "f 1 2 3\n" +
            " f    -1   -2\t-3";

        // act
        TriangleMesh mesh = reader.readTriangleMesh(new StringReader(input), TEST_PRECISION);

        // assert
        Assertions.assertEquals(4, mesh.getVertexCount());
        Assertions.assertEquals(2, mesh.getFaceCount());

        Triangle3D t0 = mesh.getFace(0).getPolygon();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, t0.getPoint1(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.1, 0, 0), t0.getPoint2(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0), t0.getPoint3(), TEST_EPS);

        Triangle3D t1 = mesh.getFace(1).getPolygon();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), t1.getPoint1(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0), t1.getPoint2(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.1, 0, 0), t1.getPoint3(), TEST_EPS);
    }

    @Test
    public void testReadMesh_multipleFaceIndices_usesTriangleFan() throws Exception {
        // arrange
        String input =
            "v 0 0 0\n" +
            "v 1 0 0\n" +
            "v 1 1 0\n" +
            "v 0.5 1.5 0\n" +
            "v 0 1 0\n" +
            "f 1 2 3 -2 -1\n";

        // act
        TriangleMesh mesh = reader.readTriangleMesh(new StringReader(input), TEST_PRECISION);

        // assert
        Assertions.assertEquals(5, mesh.getVertexCount());
        Assertions.assertEquals(3, mesh.getFaceCount());

        Triangle3D t0 = mesh.getFace(0).getPolygon();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, t0.getPoint1(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), t0.getPoint2(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 0), t0.getPoint3(), TEST_EPS);

        Triangle3D t1 = mesh.getFace(1).getPolygon();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, t1.getPoint1(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 0), t1.getPoint2(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 1.5, 0), t1.getPoint3(), TEST_EPS);

        Triangle3D t2 = mesh.getFace(2).getPolygon();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, t2.getPoint1(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 1.5, 0), t2.getPoint2(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0), t2.getPoint3(), TEST_EPS);
    }

    @Test
    public void testReadMesh_ignoresUnsupportedContent() throws Exception {
        // arrange
        String input =
            "mtllib abc.mtl\n" +
            "nope\n" +
            "v 0 0 0\n" +
            "v 1 0 0\n" +
            "v 0 1 0\n" +
            "f 1/10/20 2//40 3//\n";

        // act
        TriangleMesh mesh = reader.readTriangleMesh(new StringReader(input), TEST_PRECISION);

        // assert
        Assertions.assertEquals(3, mesh.getVertexCount());
        Assertions.assertEquals(1, mesh.getFaceCount());

        Triangle3D t0 = mesh.getFace(0).getPolygon();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, t0.getPoint1(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), t0.getPoint2(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0), t0.getPoint3(), TEST_EPS);
    }

    @Test
    public void testReadMesh_invalidVertexDefinition() throws Exception {
        // arrange
        String badNumber =
            "v abc 0 0\n" +
            "v 1 0 0\n" +
            "v 0 1 0\n" +
            "f 1 2 3\n";

        String notEnoughVertices =
            "v 0 0\n" +
            "v 1 0 0\n" +
            "v 0 1 0\n" +
            "f 1 2 3\n";

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            try {
                reader.readTriangleMesh(new StringReader(badNumber), TEST_PRECISION);
            } catch (IOException exc) {
                throw new UncheckedIOException(exc);
            }
        }, NumberFormatException.class);

        GeometryTestUtils.assertThrows(() -> {
            try {
                reader.readTriangleMesh(new StringReader(notEnoughVertices), TEST_PRECISION);
            } catch (IOException exc) {
                throw new UncheckedIOException(exc);
            }
        }, IllegalArgumentException.class, "Invalid vertex definition: at least 3 fields required but found only 2");
    }

    @Test
    public void testReadMesh_invalidFaceDefinition() throws Exception {
        // arrange
        String badNumber =
            "v 0 0 0\n" +
            "v 1 0 0\n" +
            "v 0 1 0\n" +
            "f 1 abc 3\n";

        String notEnoughIndices =
            "v 0 0 0\n" +
            "v 1 0 0\n" +
            "v 0 1 0\n" +
            "f 1 2\n";

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            try {
                reader.readTriangleMesh(new StringReader(badNumber), TEST_PRECISION);
            } catch (IOException exc) {
                throw new UncheckedIOException(exc);
            }
        }, NumberFormatException.class);

        GeometryTestUtils.assertThrows(() -> {
            try {
                reader.readTriangleMesh(new StringReader(notEnoughIndices), TEST_PRECISION);
            } catch (IOException exc) {
                throw new UncheckedIOException(exc);
            }
        }, IllegalArgumentException.class, "Invalid face definition: at least 3 fields required but found only 2");
    }

    @Test
    public void testReadMesh_cubeMinusSphereFile() throws Exception {
        // arrange
        URL url = getClass().getResource(CUBE_MINUS_SPHERE_MODEL);
        File file = new File(url.toURI());

        // act
        TriangleMesh mesh = reader.readTriangleMesh(file, TEST_PRECISION);

        // assert
        Assertions.assertEquals(CUBE_MINUS_SPHERE_VERTICES, mesh.getVertexCount());
        Assertions.assertEquals(CUBE_MINUS_SPHERE_FACES, mesh.getFaceCount());

        RegionBSPTree3D tree = RegionBSPTree3D.partitionedRegionBuilder()
                .insertAxisAlignedGrid(mesh.getBounds(), 1, TEST_PRECISION)
                .insertBoundaries(mesh)
                .build();

        double eps = 1e-5;
        Assertions.assertEquals(0.11509505362599505, tree.getSize(), eps);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tree.getCentroid(), TEST_EPS);
    }

    @Test
    public void testReadMesh_cubeMinusSphereUrl() throws IOException {
        // arrange
        URL url = getClass().getResource(CUBE_MINUS_SPHERE_MODEL);

        // act
        TriangleMesh mesh = reader.readTriangleMesh(url, TEST_PRECISION);

        // assert
        Assertions.assertEquals(CUBE_MINUS_SPHERE_VERTICES, mesh.getVertexCount());
        Assertions.assertEquals(CUBE_MINUS_SPHERE_FACES, mesh.getFaceCount());
    }
}
