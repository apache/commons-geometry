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
package org.apache.commons.geometry.io.euclidean.threed.obj;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.io.core.utils.DoubleFormats;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ObjWriterTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    public void testPropertyDefaults() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act/assert
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            Assertions.assertEquals("\n", objWriter.getLineSeparator());
            Assertions.assertSame(DoubleFormats.DOUBLE_TO_STRING, objWriter.getDoubleFormat());
            Assertions.assertEquals(0, objWriter.getVertexCount());
            Assertions.assertEquals(0, objWriter.getVertexNormalCount());
        }
    }

    @Test
    public void testClose_calledMultipleTimes() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act/assert
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.close();
        }

        Assertions.assertEquals("", writer.toString());
    }

    @Test
    public void testSetLineSeparator() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.setLineSeparator("\r\n");

            objWriter.writeComment("line 1");
            objWriter.writeComment("line 2");
            objWriter.writeVertex(Vector3D.ZERO);
        }

        // assert
        Assertions.assertEquals(
            "# line 1\r\n" +
            "# line 2\r\n" +
            "v 0.0 0.0 0.0\r\n", writer.getBuffer().toString());
    }

    @Test
    public void testSetDecimalFormat() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.setDoubleFormat(DoubleFormats.createDefault(0, -1));

            objWriter.writeVertex(Vector3D.of(1.09, 2.05, 3.06));
        }

        // assert
        Assertions.assertEquals("v 1.1 2.0 3.1\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteComment() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.writeComment("test");
            objWriter.writeComment(" a\r\n multi-line\ncomment");
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
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.writeObjectName("test-object");
        }

        // assert
        Assertions.assertEquals("o test-object\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteGroupName() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.writeGroupName("test-group");
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
        final int count;
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.setDoubleFormat(DoubleFormats.createDefault(0, -1));

            index1 = objWriter.writeVertex(Vector3D.of(1.09, 2.1, 3.005));
            index2 = objWriter.writeVertex(Vector3D.of(0.06, 10, 12));

            count = objWriter.getVertexCount();
        }

        // assert
        Assertions.assertEquals(0, index1);
        Assertions.assertEquals(1, index2);
        Assertions.assertEquals(2, count);
        Assertions.assertEquals(
            "v 1.1 2.1 3.0\n" +
            "v 0.1 10.0 12.0\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteNormal() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        final int index1;
        final int index2;
        final int count;
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.setDoubleFormat(DoubleFormats.createDefault(0, -1));

            index1 = objWriter.writeVertexNormal(Vector3D.of(1.09, 2.1, 3.005));
            index2 = objWriter.writeVertexNormal(Vector3D.of(0.06, 10, 12));

            count = objWriter.getVertexNormalCount();
        }

        // assert
        Assertions.assertEquals(0, index1);
        Assertions.assertEquals(1, index2);
        Assertions.assertEquals(2, count);
        Assertions.assertEquals(
            "vn 1.1 2.1 3.0\n" +
            "vn 0.1 10.0 12.0\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteFace() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.writeVertex(Vector3D.ZERO);
            objWriter.writeVertex(Vector3D.of(1, 0, 0));
            objWriter.writeVertex(Vector3D.of(1, 1, 0));
            objWriter.writeVertex(Vector3D.of(0, 1, 0));

            objWriter.writeFace(0, 1, 2);
            objWriter.writeFace(0, 1, 2, 3);
        }

        // assert
        Assertions.assertEquals(
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 1.0 1.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "f 1 2 3\n" +
            "f 1 2 3 4\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteFace_withNormals() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.writeVertex(Vector3D.ZERO);
            objWriter.writeVertex(Vector3D.of(1, 0, 0));
            objWriter.writeVertex(Vector3D.of(1, 1, 0));
            objWriter.writeVertex(Vector3D.of(0, 1, 0));

            objWriter.writeVertexNormal(Vector3D.Unit.PLUS_Z);
            objWriter.writeVertexNormal(Vector3D.Unit.MINUS_Z);

            objWriter.writeFace(new int[] {0, 1, 2}, 0);
            objWriter.writeFace(new int[] {0, 1, 2, 3}, new int[] {1, 1, 1, 1});
        }

        // assert
        Assertions.assertEquals(
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 1.0 1.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "vn 0.0 0.0 1.0\n" +
            "vn 0.0 0.0 -1.0\n" +
            "f 1//1 2//1 3//1\n" +
            "f 1//2 2//2 3//2 4//2\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteFace_invalidVertexNumber() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            try (ObjWriter objWriter = new ObjWriter(writer)) {
                objWriter.writeFace(1, 2);
            }
        }, IllegalArgumentException.class, "Face must have more than 3 vertices; found 2");
    }

    @Test
    public void testWriteFace_vertexIndexOutOfBounds() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            try (ObjWriter objWriter = new ObjWriter(writer)) {
                objWriter.writeVertex(Vector3D.ZERO);
                objWriter.writeVertex(Vector3D.of(1, 1, 1));

                objWriter.writeFace(0, 1, 2);
            }
        }, IndexOutOfBoundsException.class, "Vertex index out of bounds: 2");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            try (ObjWriter objWriter = new ObjWriter(writer)) {
                objWriter.writeVertex(Vector3D.ZERO);
                objWriter.writeVertex(Vector3D.of(1, 1, 1));

                objWriter.writeFace(0, -1, 1);
            }
        }, IndexOutOfBoundsException.class, "Vertex index out of bounds: -1");
    }

    @Test
    public void testWriteFace_normalIndexOutOfBounds() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            try (ObjWriter objWriter = new ObjWriter(writer)) {
                objWriter.writeVertex(Vector3D.ZERO);
                objWriter.writeVertex(Vector3D.of(1, 1, 1));
                objWriter.writeVertex(Vector3D.of(0, 2, 0));

                objWriter.writeVertexNormal(Vector3D.Unit.PLUS_Z);

                objWriter.writeFace(new int[] {0, 1, 2}, 1);
            }
        }, IndexOutOfBoundsException.class, "Normal index out of bounds: 1");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            try (ObjWriter objWriter = new ObjWriter(writer)) {
                objWriter.writeVertex(Vector3D.ZERO);
                objWriter.writeVertex(Vector3D.of(1, 1, 1));
                objWriter.writeVertex(Vector3D.of(0, 2, 0));

                objWriter.writeVertexNormal(Vector3D.Unit.PLUS_Z);

                objWriter.writeFace(new int[] {0, 1, 2}, -1);
            }
        }, IndexOutOfBoundsException.class, "Normal index out of bounds: -1");
    }

    @Test
    public void testWriteFace_invalidVertexAndNormalCountMismatch() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        // act
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            try (ObjWriter objWriter = new ObjWriter(writer)) {
                objWriter.writeFace(new int[] {0, 1, 2, 3}, new int[] {0, 1, 2});
            }
        }, IllegalArgumentException.class, "Face normal index count must equal vertex index count; expected 4 but was 3");
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
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.writeMesh(mesh);
        }

        // assert
        Assertions.assertEquals(
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "v 0.0 0.0 1.0\n" +
            "f 1 2 3\n" +
            "f 1 2 4\n", writer.getBuffer().toString());
    }

    @Test
    public void testMeshBuffer() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        try (ObjWriter objWriter = new ObjWriter(writer)) {
            ObjWriter.MeshBuffer buf = objWriter.meshBuffer();

            // act
            buf.add(new SimpleFacetDefinition(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0)), Vector3D.Unit.MINUS_Z));
            buf.add(Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(1, 1, 0), Vector3D.of(0, 1.5, 0)), TEST_PRECISION));
            buf.add(new SimpleFacetDefinition(Arrays.asList(
                    Vector3D.of(0, 1.5, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 2, 0)), Vector3D.Unit.PLUS_Z));

            buf.flush();
        }

        // assert
        Assertions.assertEquals(
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 1.0 1.0 0.0\n" +
            "v 0.0 1.5 0.0\n" +
            "v 0.0 2.0 0.0\n" +
            "vn 0.0 0.0 -1.0\n" +
            "vn 0.0 0.0 1.0\n" +
            "f 1//1 2//1 3//1\n" +
            "f 1 3 4\n" +
            "f 4//2 3//2 5//2\n", writer.getBuffer().toString());
    }

    @Test
    public void testMeshBuffer_givenBatchSize() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        try (ObjWriter objWriter = new ObjWriter(writer)) {
            ObjWriter.MeshBuffer buf = objWriter.meshBuffer(2);

            // act
            buf.add(new SimpleFacetDefinition(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0)), Vector3D.Unit.MINUS_Z));
            buf.add(Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(1, 1, 0), Vector3D.of(0, 1.5, 0)), TEST_PRECISION));
            buf.add(new SimpleFacetDefinition(Arrays.asList(
                    Vector3D.of(0, 1.5, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 2, 0)), Vector3D.Unit.PLUS_Z));

            buf.flush();
        }

        // assert
        Assertions.assertEquals(
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 1.0 1.0 0.0\n" +
            "v 0.0 1.5 0.0\n" +
            "vn 0.0 0.0 -1.0\n" +
            "f 1//1 2//1 3//1\n" +
            "f 1 3 4\n" +
            "v 0.0 1.5 0.0\n" +
            "v 1.0 1.0 0.0\n" +
            "v 0.0 2.0 0.0\n" +
            "vn 0.0 0.0 1.0\n" +
            "f 5//2 6//2 7//2\n", writer.getBuffer().toString());
    }

    @Test
    public void testMeshBuffer_mixedWithDirectlyAddedFace() throws IOException {
        // arrange
        final StringWriter writer = new StringWriter();

        try (ObjWriter objWriter = new ObjWriter(writer)) {
            ObjWriter.MeshBuffer buf = objWriter.meshBuffer(2);

            // act
            objWriter.writeVertex(Vector3D.ZERO);
            objWriter.writeVertex(Vector3D.Unit.MINUS_Y);
            objWriter.writeVertex(Vector3D.Unit.MINUS_X);
            objWriter.writeVertexNormal(Vector3D.Unit.PLUS_Z);
            objWriter.writeFace(new int[] {0, 1, 2}, 0);

            buf.add(new SimpleFacetDefinition(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0)), Vector3D.Unit.MINUS_Z));
            buf.add(Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(1, 1, 0), Vector3D.of(0, 1.5, 0)), TEST_PRECISION));
            buf.add(new SimpleFacetDefinition(Arrays.asList(
                    Vector3D.of(0, 1.5, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 2, 0)), Vector3D.Unit.PLUS_Z));

            buf.flush();

            objWriter.writeFace(objWriter.getVertexCount() - 1, 2, 1, 0);
        }

        // assert
        Assertions.assertEquals(
            "v 0.0 0.0 0.0\n" +
            "v 0.0 -1.0 0.0\n" +
            "v -1.0 0.0 0.0\n" +
            "vn 0.0 0.0 1.0\n" +
            "f 1//1 2//1 3//1\n" +
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 1.0 1.0 0.0\n" +
            "v 0.0 1.5 0.0\n" +
            "vn 0.0 0.0 -1.0\n" +
            "f 4//2 5//2 6//2\n" +
            "f 4 6 7\n" +
            "v 0.0 1.5 0.0\n" +
            "v 1.0 1.0 0.0\n" +
            "v 0.0 2.0 0.0\n" +
            "vn 0.0 0.0 1.0\n" +
            "f 8//3 9//3 10//3\n" +
            "f 10 3 2 1\n", writer.getBuffer().toString());
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
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.writeBoundaries(mesh);
        }

        // assert
        Assertions.assertEquals(
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "v 0.0 0.0 1.0\n" +
            "f 1 2 3\n" +
            "f 1 2 4\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteBoundaries_nonMeshArgument() throws IOException {
        // arrange
        final BoundarySource3D src = BoundarySource3D.of(
                    Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION),
                    Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 0, 1), TEST_PRECISION)
                );

        final StringWriter writer = new StringWriter();

        // act
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.writeBoundaries(src);
        }

        // assert
        Assertions.assertEquals(
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "v 0.0 0.0 1.0\n" +
            "f 1 2 3\n" +
            "f 1 2 4\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteBoundaries_nonMeshArgument_smallBatchSize() throws IOException {
        // arrange
        final BoundarySource3D src = BoundarySource3D.of(
                    Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION),
                    Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 0, 1), TEST_PRECISION)
                );

        final StringWriter writer = new StringWriter();

        // act
        try (ObjWriter objWriter = new ObjWriter(writer)) {
            objWriter.writeBoundaries(src, 1);
        }

        // assert
        Assertions.assertEquals(
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 1.0 0.0\n" +
            "f 1 2 3\n" +
            "v 0.0 0.0 0.0\n" +
            "v 1.0 0.0 0.0\n" +
            "v 0.0 0.0 1.0\n" +
            "f 4 5 6\n", writer.getBuffer().toString());
    }

    @Test
    public void testWriteBoundaries_infiniteBoundary() throws IOException {
        // arrange
        final BoundarySource3D src = BoundarySource3D.of(
                    Planes.triangleFromVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION).span()
                );

        final StringWriter writer = new StringWriter();

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            try (ObjWriter objWriter = new ObjWriter(writer)) {
                objWriter.writeBoundaries(src);
            } catch (final IOException exc) {
                throw new UncheckedIOException(exc);
            }
        }, IllegalArgumentException.class, Pattern.compile("^OBJ input geometry cannot be infinite: .*"));
    }
}
