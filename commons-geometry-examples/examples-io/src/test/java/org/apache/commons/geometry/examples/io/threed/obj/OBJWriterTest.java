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

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.geometry.euclidean.threed.shape.Sphere;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OBJWriterTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testDefaults() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act/assert
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            Assertions.assertEquals("\n", meshWriter.getLineSeparator());
            Assertions.assertEquals(6, meshWriter.getDecimalFormat().getMaximumFractionDigits());
        }
    }

    @Test
    public void testClose_calledMultipleTimes() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act/assert
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            meshWriter.close();
        }
    }

    @Test
    public void testSetLineSeparator() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            meshWriter.setLineSeparator("\r\n");

            meshWriter.writeComment("line 1");
            meshWriter.writeComment("line 2");
            meshWriter.writeVertex(Vector3D.ZERO);
        }

        // assert
        Assertions.assertEquals(
            "# line 1\r\n" +
            "# line 2\r\n" +
            "v 0 0 0\r\n", writer.getBuffer().toString());
    }

    @Test
    public void testSetDecimalFormat() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            meshWriter.setDecimalFormat(new DecimalFormat("00.0"));

            meshWriter.writeVertex(Vector3D.of(1, 2, 3));
        }

        // assert
        Assertions.assertEquals("v 01.0 02.0 03.0\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteComment() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            meshWriter.writeComment("test");
            meshWriter.writeComment(" a\r\n multi-line\ncomment");
        }

        // assert
        Assertions.assertEquals(
            "# test\n" +
            "#  a\n" +
            "#  multi-line\n" +
            "# comment\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteObjectName() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            meshWriter.writeObjectName("test-object");
        }

        // assert
        Assertions.assertEquals("o test-object\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteGroupName() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            meshWriter.writeGroupName("test-group");
        }

        // assert
        Assertions.assertEquals("g test-group\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteVertex() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        final int index1;
        final int index2;
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            meshWriter.getDecimalFormat().setMaximumFractionDigits(1);

            index1 = meshWriter.writeVertex(Vector3D.of(1.09, 2.1, 3.005));
            index2 = meshWriter.writeVertex(Vector3D.of(0.06, 10, 12));
        }

        // assert
        Assertions.assertEquals(1, index1);
        Assertions.assertEquals(2, index2);
        Assertions.assertEquals(
            "v 1.1 2.1 3\n" +
            "v 0.1 10 12\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteFace() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            meshWriter.writeVertex(Vector3D.ZERO);
            meshWriter.writeVertex(Vector3D.of(1, 0, 0));
            meshWriter.writeVertex(Vector3D.of(1, 1, 0));
            meshWriter.writeVertex(Vector3D.of(0, 1, 0));

            meshWriter.writeFace(1, 2, 3);
            meshWriter.writeFace(1, 2, 3, -1);
        }

        // assert
        Assertions.assertEquals(
            "v 0 0 0\n" +
            "v 1 0 0\n" +
            "v 1 1 0\n" +
            "v 0 1 0\n" +
            "f 1 2 3\n" +
            "f 1 2 3 -1\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteFace_invalidVertexNumber() {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        GeometryTestUtils.assertThrows(() -> {
            try (OBJWriter meshWriter = new OBJWriter(writer)) {
                meshWriter.writeFace(1, 2);
            } catch (final IOException exc) {
                throw new UncheckedIOException(exc);
            }
        }, IllegalArgumentException.class, "Face must have more than 3 vertices; found 2");
    }

    @Test
    public void testWriteMesh() throws IOException {
        // arrange
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.builder(TEST_PRECISION)
                .addFaceUsingVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0))
                .addFaceUsingVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 0, 1))
                .build();

        final StringWriter writer = new StringWriter();

        // act
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            meshWriter.writeMesh(mesh);
        }

        // assert
        Assertions.assertEquals(
            "v 0 0 0\n" +
            "v 1 0 0\n" +
            "v 0 1 0\n" +
            "v 0 0 1\n" +
            "f 1 2 3\n" +
            "f 1 2 4\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteBoundaries_meshArgument() throws IOException {
        // arrange
        final SimpleTriangleMesh mesh = SimpleTriangleMesh.builder(TEST_PRECISION)
                .addFaceUsingVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0))
                .addFaceUsingVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 0, 1))
                .build();

        final StringWriter writer = new StringWriter();

        // act
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            meshWriter.writeBoundaries(mesh);
        }

        // assert
        Assertions.assertEquals(
            "v 0 0 0\n" +
            "v 1 0 0\n" +
            "v 0 1 0\n" +
            "v 0 0 1\n" +
            "f 1 2 3\n" +
            "f 1 2 4\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteBoundaries_nonMeshArgument() throws IOException {
        // arrange
        final BoundarySource3D src = BoundarySource3D.from(
                    Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION),
                    Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 0, 1), TEST_PRECISION)
                );

        final StringWriter writer = new StringWriter();

        // act
        try (OBJWriter meshWriter = new OBJWriter(writer)) {
            meshWriter.writeBoundaries(src);
        }

        // assert
        Assertions.assertEquals(
            "v 0 0 0\n" +
            "v 1 0 0\n" +
            "v 0 1 0\n" +
            "f 1 2 3\n" +
            "v 0 0 0\n" +
            "v 1 0 0\n" +
            "v 0 0 1\n" +
            "f 4 5 6\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteBoundaries_infiniteBoundary() {
        // arrange
        final BoundarySource3D src = BoundarySource3D.from(
                    Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION).span()
                );

        final StringWriter writer = new StringWriter();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            try (OBJWriter meshWriter = new OBJWriter(writer)) {
                meshWriter.writeBoundaries(src);
            } catch (final IOException exc) {
                throw new UncheckedIOException(exc);
            }
        }, IllegalArgumentException.class, Pattern.compile("^OBJ input geometry cannot be infinite: .*"));
    }

    @Test
    public void testWriteToFile_boundaries() throws IOException {
        // arrange
        final RegionBSPTree3D box = Parallelepiped.unitCube(TEST_PRECISION).toTree();
        final RegionBSPTree3D sphere = Sphere.from(Vector3D.ZERO, 0.6, TEST_PRECISION)
                .toTree(3);

        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.difference(box, sphere);

        final TriangleMesh mesh = result.toTriangleMesh(TEST_PRECISION);

        // act
        final Path out = Files.createTempFile("objTest", ".obj");
        try (OBJWriter writer = new OBJWriter(out.toFile())) {
            writer.writeComment("A test obj file\nWritten by " + OBJReaderTest.class.getName());

            writer.writeBoundaries(mesh);
        } finally {
            Files.delete(out);
        }
    }

    @Test
    public void testWriteToFile_mesh() throws IOException {
        // arrange
        final RegionBSPTree3D box = Parallelepiped.unitCube(TEST_PRECISION).toTree();
        final RegionBSPTree3D sphere = Sphere.from(Vector3D.ZERO, 0.6, TEST_PRECISION)
                .toTree(3);

        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.difference(box, sphere);

        // act
        final Path out = Files.createTempFile("objTest", ".obj");
        try (OBJWriter writer = new OBJWriter(out.toFile())) {
            writer.writeComment("A test obj file\nWritten by " + OBJReaderTest.class.getName());

            writer.writeBoundaries(result);
        } finally {
            Files.delete(out);
        }
    }
}
