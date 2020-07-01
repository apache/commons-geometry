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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class OBJModelIOHandlerTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final String CUBE_MINUS_SPHERE_MODEL = "/models/cube-minus-sphere.obj";

    private static final int CUBE_MINUS_SPHERE_VERTICES = 1688;

    private static final int CUBE_MINUS_SPHERE_FACES = 728;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private OBJModelIOHandler handler = new OBJModelIOHandler();

    @Test
    public void testHandlesType() {
        // act/assert
        Assert.assertFalse(handler.handlesType(null));
        Assert.assertFalse(handler.handlesType(""));
        Assert.assertFalse(handler.handlesType(" "));
        Assert.assertFalse(handler.handlesType("abc"));
        Assert.assertFalse(handler.handlesType("stl"));

        Assert.assertTrue(handler.handlesType("obj"));
        Assert.assertTrue(handler.handlesType("OBJ"));
        Assert.assertTrue(handler.handlesType("oBj"));
    }

    @Test
    public void testRead_fromFile() throws Exception {
        // act
        BoundarySource3D src = handler.read("obj", cubeMinusSphereFile(), TEST_PRECISION);

        // assert
        TriangleMesh mesh = (TriangleMesh) src;
        Assert.assertEquals(CUBE_MINUS_SPHERE_VERTICES, mesh.getVertexCount());
        Assert.assertEquals(CUBE_MINUS_SPHERE_FACES, mesh.getFaceCount());
    }

    @Test
    public void testRead_fromFile_unsupportedType() throws Exception {
        // arrange
        File file = cubeMinusSphereFile();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            handler.read("stl", file, TEST_PRECISION);
        }, IllegalArgumentException.class, "File type is not supported by this handler: stl");
    }

    @Test
    public void testRead_fromFile_ioException() throws Exception {
        // arrange
        File file = new File("doesnotexist.obj");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            handler.read("obj", file, TEST_PRECISION);
        }, UncheckedIOException.class);
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
        Assert.assertEquals(CUBE_MINUS_SPHERE_VERTICES, mesh.getVertexCount());
        Assert.assertEquals(CUBE_MINUS_SPHERE_FACES, mesh.getFaceCount());
    }

    @Test
    public void testRead_fromStream_unsupportedType() throws Exception {
        // arrange
        File file = cubeMinusSphereFile();

        // act/assert
        try (InputStream in = Files.newInputStream(file.toPath())) {
            GeometryTestUtils.assertThrows(() -> {
                handler.read("stl", in, TEST_PRECISION);
            }, IllegalArgumentException.class, "File type is not supported by this handler: stl");
        }
    }

    @Test
    public void testRead_fromStream_ioException() throws Exception {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            handler.read("obj", new FailingInputStream(), TEST_PRECISION);
        }, UncheckedIOException.class, "IOException: test");
    }

    @Test
    public void testWrite_toFile() throws Exception {
        // arrange
        File out = tempFolder.newFile("out.obj");

        BoundarySource3D src = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        // act
        handler.write(src, "OBJ", out);

        // assert
        TriangleMesh mesh = (TriangleMesh) handler.read("obj", out, TEST_PRECISION);
        Assert.assertEquals(3, mesh.getVertexCount());
        Assert.assertEquals(1, mesh.getFaceCount());
    }

    @Test
    public void testWrite_toFile_unsupportedFormat() throws Exception {
        // arrange
        File out = tempFolder.newFile("out.obj");

        BoundarySource3D src = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            handler.write(src, "stl", out);
        }, IllegalArgumentException.class, "File type is not supported by this handler: stl");
    }

    @Test
    public void testWrite_toFile_ioException() throws Exception {
        // arrange
        File out = tempFolder.newFolder("notafile");

        BoundarySource3D src = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            handler.write(src, "OBJ", out);
        }, UncheckedIOException.class);
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
        Assert.assertEquals(3, mesh.getVertexCount());
        Assert.assertEquals(1, mesh.getFaceCount());
    }

    @Test
    public void testWrite_toStream_unsupportedFormat() throws Exception {
        // arrange
        File file = tempFolder.newFile("out.obj");

        BoundarySource3D src = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        // act/assert
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            GeometryTestUtils.assertThrows(() -> {
                handler.write(src, "stl", out);
            }, IllegalArgumentException.class, "File type is not supported by this handler: stl");
        }
    }

    @Test
    public void testWrite_toStream_ioException() throws Exception {
        // arrange
        BoundarySource3D src = BoundarySource3D.from(
                Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION)
            );

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            handler.write(src, "OBJ", new FailingOutputStream());
        }, UncheckedIOException.class, "IOException: test");
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
