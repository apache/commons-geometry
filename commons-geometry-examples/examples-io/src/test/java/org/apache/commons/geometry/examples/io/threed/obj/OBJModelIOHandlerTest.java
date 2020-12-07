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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class OBJModelIOHandlerTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final String CUBE_MINUS_SPHERE_MODEL = "/models/cube-minus-sphere.obj";

    private static final int CUBE_MINUS_SPHERE_VERTICES = 1688;

    private static final int CUBE_MINUS_SPHERE_FACES = 728;

    @TempDir
    File anotherTempDir;

    private OBJModelIOHandler handler = new OBJModelIOHandler();

    @Test
    public void testHandlesType() {
        // act/assert
        Assertions.assertFalse(handler.handlesType(null));
        Assertions.assertFalse(handler.handlesType(""));
        Assertions.assertFalse(handler.handlesType(" "));
        Assertions.assertFalse(handler.handlesType("abc"));
        Assertions.assertFalse(handler.handlesType("stl"));

        Assertions.assertTrue(handler.handlesType("obj"));
        Assertions.assertTrue(handler.handlesType("OBJ"));
        Assertions.assertTrue(handler.handlesType("oBj"));
    }

    @Test
    public void testRead_fromFile() throws Exception {
        // act
        BoundarySource3D src = handler.read("obj", cubeMinusSphereFile(), TEST_PRECISION);

        // assert
        TriangleMesh mesh = (TriangleMesh) src;
        Assertions.assertEquals(CUBE_MINUS_SPHERE_VERTICES, mesh.getVertexCount());
        Assertions.assertEquals(CUBE_MINUS_SPHERE_FACES, mesh.getFaceCount());
    }

    @Test
    public void testRead_fromFile_unsupportedType() throws Exception {
        // arrange
        File file = cubeMinusSphereFile();

        // act/assert
        assertThrows(IllegalArgumentException.class, () -> handler.read("stl", file, TEST_PRECISION),  "File type is not supported by this handler: stl");
    }

    @Test
    public void testRead_fromFile_ioException() throws Exception {
        // arrange
        File file = new File("doesnotexist.obj");

        // act/assert
        assertThrows(UncheckedIOException.class, () -> handler.read("obj", file, TEST_PRECISION));
    }

    @Test
    public void testRead_fromStream() throws Exception {
        // act
        BoundarySource3D src;
        try (InputStream in = Files.newInputStream(cubeMinusSphereFile().toPath())) {
            src = handler.read("obj", cubeMinusSphereFile(), TEST_PRECISION);
        }

        // assert
        TriangleMesh mesh = (TriangleMesh) src;
        Assertions.assertEquals(CUBE_MINUS_SPHERE_VERTICES, mesh.getVertexCount());
        Assertions.assertEquals(CUBE_MINUS_SPHERE_FACES, mesh.getFaceCount());
    }

    @Test
    public void testRead_fromStream_unsupportedType() throws Exception {
        // arrange
        File file = cubeMinusSphereFile();

        // act/assert
        try (InputStream in = Files.newInputStream(file.toPath())) {
            assertThrows(IllegalArgumentException.class, () -> handler.read("stl", in, TEST_PRECISION),  "File type is not supported by this handler: stl");
        }
    }

    @Test
    public void testRead_fromStream_ioException() throws Exception {
        // act/assert
        assertThrows(UncheckedIOException.class, () -> handler.read("obj", new FailingInputStream(), TEST_PRECISION),  "IOException: test");
    }

    @Test
    public void testWrite_toFile() throws Exception {
        // arrange
        File out = new File(anotherTempDir, "out.obj");

        BoundarySource3D src = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        // act
        handler.write(src, "OBJ", out);

        // assert
        TriangleMesh mesh = (TriangleMesh) handler.read("obj", out, TEST_PRECISION);
        Assertions.assertEquals(3, mesh.getVertexCount());
        Assertions.assertEquals(1, mesh.getFaceCount());
    }

    @Test
    public void testWrite_toFile_unsupportedFormat() throws Exception {
        // arrange
        File out = new File(anotherTempDir, "out.obj");
        BoundarySource3D src = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        // act/assert
        assertThrows(IllegalArgumentException.class, () -> handler.write(src, "stl", out),  "File type is not supported by this handler: stl");
    }

    @Test
    public void testWrite_toFile_ioException() throws Exception {
        // arrange
        File out = new File(anotherTempDir, "notafile");
        BoundarySource3D src = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        // act/assert
        assertThrows(UncheckedIOException.class, () -> handler.write(src, "OBJ", out));
    }

    @Test
    public void testWrite_toStream() throws Exception {
        // arrange
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        BoundarySource3D src = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        // act
        handler.write(src, "OBJ", out);

        // assert
        TriangleMesh mesh = (TriangleMesh) handler.read("obj", new ByteArrayInputStream(out.toByteArray()),
                TEST_PRECISION);
        Assertions.assertEquals(3, mesh.getVertexCount());
        Assertions.assertEquals(1, mesh.getFaceCount());
    }

    @Test
    public void testWrite_toStream_unsupportedFormat() throws Exception {
        // arrange
        File file = new File(anotherTempDir, "out.obj");
        BoundarySource3D src = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        // act/assert
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            assertThrows(IllegalArgumentException.class, () -> handler.write(src, "stl", out),  "File type is not supported by this handler: stl");
        }
    }

    @Test
    public void testWrite_toStream_ioException() throws Exception {
        // arrange
        BoundarySource3D src = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        // act/assert
        assertThrows(UncheckedIOException.class, () -> handler.write(src, "OBJ", new FailingOutputStream()),  "IOException: test");
    }

    private static File cubeMinusSphereFile() throws Exception {
        URL url = OBJModelIOHandlerTest.class.getResource(CUBE_MINUS_SPHERE_MODEL);
        return new File(url.toURI());
    }

    private static final class FailingInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            throw new IOException("test");
        }
    }

    private static final class FailingOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            throw new IOException("test");
        }
    }
}
