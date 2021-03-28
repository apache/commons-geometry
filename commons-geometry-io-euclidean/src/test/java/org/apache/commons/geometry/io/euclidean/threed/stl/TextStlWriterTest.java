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
package org.apache.commons.geometry.io.euclidean.threed.stl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.test.CloseCountWriter;
import org.apache.commons.geometry.io.core.utils.DoubleFormats;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextStlWriterTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private final StringWriter out = new StringWriter();

    @Test
    public void testDefaultProperties() throws IOException {
        // act/assert
        try (TextStlWriter writer = new TextStlWriter(out)) {
            Assertions.assertSame(DoubleFormats.DOUBLE_TO_STRING, writer.getDoubleFormat());
            Assertions.assertEquals("\n", writer.getLineSeparator());
        }
    }

    @Test
    public void testNoContent() throws IOException {
        // arrange
        final CloseCountWriter countWriter = new CloseCountWriter(out);

        // act
        try (TextStlWriter writer = new TextStlWriter(countWriter)) {
            Assertions.assertEquals(0, countWriter.getCloseCount());
        }

        // assert
        Assertions.assertEquals(1, countWriter.getCloseCount());
        Assertions.assertEquals("", out.toString());
    }

    @Test
    public void testStartSolid_alreadyStarted() throws IOException {
        // arrange
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();

            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.startSolid(),
                    IllegalStateException.class, "Cannot start solid definition: a solid is already being written");
        }
    }

    @Test
    public void testEndSolid_notStarted() throws IOException {
        // arrange
        try (TextStlWriter writer = new TextStlWriter(out)) {
            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.endSolid(),
                    IllegalStateException.class, "Cannot end solid definition: no solid has been started");
        }
    }

    @Test
    public void testEmpty_noName() throws IOException {
        // arrange
        final CloseCountWriter countWriter = new CloseCountWriter(out);

        // act
        try (TextStlWriter writer = new TextStlWriter(countWriter)) {
            writer.startSolid();
            writer.endSolid();

            Assertions.assertEquals(0, countWriter.getCloseCount());
        }

        // assert
        Assertions.assertEquals(1, countWriter.getCloseCount());
        Assertions.assertEquals(
                "solid \n" +
                "endsolid \n", out.toString());
    }

    @Test
    public void testEmpty_withName() throws IOException {
        // arrange
        final CloseCountWriter countWriter = new CloseCountWriter(out);

        // act
        try (TextStlWriter writer = new TextStlWriter(countWriter)) {
            writer.startSolid("Name of the solid");
            writer.endSolid();

            Assertions.assertEquals(0, countWriter.getCloseCount());
        }

        // assert
        Assertions.assertEquals(1, countWriter.getCloseCount());
        Assertions.assertEquals(
                "solid Name of the solid\n" +
                "endsolid Name of the solid\n", out.toString());
    }

    @Test
    public void testClose_endsSolid() throws IOException {
        // arrange
        final CloseCountWriter countWriter = new CloseCountWriter(out);

        // act
        try (TextStlWriter writer = new TextStlWriter(countWriter)) {
            writer.startSolid("name");

            Assertions.assertEquals(0, countWriter.getCloseCount());
        }

        // assert
        Assertions.assertEquals(1, countWriter.getCloseCount());
        Assertions.assertEquals(
                "solid name\n" +
                "endsolid name\n", out.toString());
    }

    @Test
    public void testStartSolid_containsNewLine() throws IOException {
        // arrange
        try (TextStlWriter writer = new TextStlWriter(out)) {
            final String err = "Solid name cannot contain new line characters";

            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.startSolid("Hi\nthere"),
                    IllegalArgumentException.class, err);
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.startSolid("Hi\r\nthere"),
                    IllegalArgumentException.class, err);
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.startSolid("Hi\rthere"),
                    IllegalArgumentException.class, err);
        }
    }

    @Test
    public void testWriteTriangle_noNormal_computesNormal() throws IOException {
        // arrange
        final Vector3D p1 = Vector3D.of(0, 4, 0);
        final Vector3D p2 = Vector3D.of(1.0 / 3.0, 0, 0);
        final Vector3D p3 = Vector3D.of(0, 0.5, 10);

        // act
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();
            writer.writeTriangle(p1, p2, p3, null);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet -0.9961250701090868 -0.08301042250909056 -0.029053647878181696\n" +
            "outer loop\n" +
            "vertex 0.0 4.0 0.0\n" +
            "vertex 0.3333333333333333 0.0 0.0\n" +
            "vertex 0.0 0.5 10.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }

    @Test
    public void testWriteTriangle_zeroNormal_computesNormal() throws IOException {
        // arrange
        final Vector3D p1 = Vector3D.of(0, 4, 0);
        final Vector3D p2 = Vector3D.of(1.0 / 3.0, 0, 0);
        final Vector3D p3 = Vector3D.of(0, 0.5, 10);

        // act
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();
            writer.writeTriangle(p1, p2, p3, Vector3D.ZERO);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet -0.9961250701090868 -0.08301042250909056 -0.029053647878181696\n" +
            "outer loop\n" +
            "vertex 0.0 4.0 0.0\n" +
            "vertex 0.3333333333333333 0.0 0.0\n" +
            "vertex 0.0 0.5 10.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }

    @Test
    public void testWriteTriangle_noNormal_cannotComputeNormal() throws IOException {
        // arrange
        final Vector3D p1 = Vector3D.ZERO;
        final Vector3D p2 = Vector3D.of(1.0 / 3.0, 0, 0);
        final Vector3D p3 = Vector3D.ZERO;

        // act
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();
            writer.writeTriangle(p1, p2, p3, null);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet 0.0 0.0 0.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 0.3333333333333333 0.0 0.0\n" +
            "vertex 0.0 0.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }

    @Test
    public void testWriteTriangle_withNormal_correctOrientation() throws IOException {
        // arrange
        final Vector3D p1 = Vector3D.of(0, 4, 0);
        final Vector3D p2 = Vector3D.of(1.0 / 3.0, 0, 0);
        final Vector3D p3 = Vector3D.of(0, 0.5, 10);

        final Vector3D normal = p1.vectorTo(p2).cross(p1.vectorTo(p3)).normalize();

        // act
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();
            writer.writeTriangle(p1, p2, p3, normal);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet -0.9961250701090868 -0.08301042250909056 -0.029053647878181696\n" +
            "outer loop\n" +
            "vertex 0.0 4.0 0.0\n" +
            "vertex 0.3333333333333333 0.0 0.0\n" +
            "vertex 0.0 0.5 10.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }

    @Test
    public void testWriteTriangle_withNormal_reversedOrientation() throws IOException {
        // arrange
        final Vector3D p1 = Vector3D.of(0, 4, 0);
        final Vector3D p2 = Vector3D.of(1.0 / 3.0, 0, 0);
        final Vector3D p3 = Vector3D.of(0, 0.5, 10);

        final Vector3D normal = p1.vectorTo(p2).cross(p1.vectorTo(p3)).normalize();

        // act
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();
            writer.writeTriangle(p1, p2, p3, normal.negate());
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet 0.9961250701090868 0.08301042250909056 0.029053647878181696\n" +
            "outer loop\n" +
            "vertex 0.0 4.0 0.0\n" +
            "vertex 0.0 0.5 10.0\n" +
            "vertex 0.3333333333333333 0.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }

    @Test
    public void testWrite_verticesAndNormal() throws IOException {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0));
        final Vector3D n1 = Vector3D.of(0, 0, 100);
        final Vector3D n2 = Vector3D.Unit.MINUS_Z;

        // act
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();

            writer.writeTriangles(vertices, n1);
            writer.writeTriangles(vertices, n2);
            writer.writeTriangles(vertices, null);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet 0.0 0.0 1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 0.0 0.0\n" +
            "vertex 0.0 1.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "facet 0.0 0.0 -1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 0.0 1.0 0.0\n" +
            "vertex 1.0 0.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "facet 0.0 0.0 1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 0.0 0.0\n" +
            "vertex 0.0 1.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }

    @Test
    public void testWrite_verticesAndNormal_moreThanThreeVertices() throws IOException {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0));
        final Vector3D normal = Vector3D.Unit.PLUS_Z;

        // act
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();

            writer.writeTriangles(vertices, normal);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet 0.0 0.0 1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 0.0 0.0\n" +
            "vertex 1.0 1.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "facet 0.0 0.0 1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 1.0 0.0\n" +
            "vertex 0.0 1.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }

    @Test
    public void testWrite_verticesAndNormal_fewerThanThreeVertices() throws IOException {
        // arrange
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();

            // act/assert
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> writer.writeTriangles(Arrays.asList(), null));
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> writer.writeTriangles(Arrays.asList(Vector3D.ZERO), null));
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> writer.writeTriangles(Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 1, 1)), null));
        }
    }

    @Test
    public void testWrite_boundary() throws IOException {
        // arrange
        final ConvexPolygon3D boundary = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 0, 1), Vector3D.of(0, 0, 1)),
                TEST_PRECISION);

        // act
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();

            writer.writeTriangles(boundary);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet 0.0 -1.0 0.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 0.0 0.0\n" +
            "vertex 1.0 0.0 1.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "facet 0.0 -1.0 0.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 0.0 1.0\n" +
            "vertex 0.0 0.0 1.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }

    @Test
    public void testWrite_facetDefinition_noNormal() throws IOException {
        // arrange
        final FacetDefinition facet = new SimpleFacetDefinition(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)));

        // act
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();

            writer.writeTriangles(facet);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet 0.0 0.0 1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 0.0 0.0\n" +
            "vertex 1.0 1.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "facet 0.0 0.0 1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 1.0 0.0\n" +
            "vertex 0.0 1.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }

    @Test
    public void testWrite_facetDefinition_withNormal() throws IOException {
        // arrange
        final Vector3D normal = Vector3D.Unit.PLUS_Z;
        final FacetDefinition facet = new SimpleFacetDefinition(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)),
                normal);

        // act
        try (TextStlWriter writer = new TextStlWriter(out)) {
            writer.startSolid();

            writer.writeTriangles(facet);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet 0.0 0.0 1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 0.0 0.0\n" +
            "vertex 1.0 1.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "facet 0.0 0.0 1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 1.0 1.0 0.0\n" +
            "vertex 0.0 1.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }

    @Test
    public void testWrite_noSolidStarted() throws IOException {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0));
        final Vector3D normal = Vector3D.Unit.PLUS_Z;

        final String msg = "Cannot write triangle: no solid has been started";

        try (TextStlWriter writer = new TextStlWriter(out)) {

            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.writeTriangle(vertices.get(0), vertices.get(1), vertices.get(2), normal),
                    IllegalStateException.class, msg);

            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.writeTriangles(vertices, normal),
                    IllegalStateException.class, msg);

            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.writeTriangles(new SimpleFacetDefinition(vertices, normal)),
                    IllegalStateException.class, msg);

            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.writeTriangles(Planes.convexPolygonFromVertices(vertices, TEST_PRECISION)),
                    IllegalStateException.class, msg);
        }
    }

    @Test
    public void testWrite_customFormat() throws IOException {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1.0 / 3.0, 0, 0), Vector3D.of(0, 1.0 / 3.0, 0));
        final Vector3D normal = Vector3D.Unit.PLUS_Z;

        try (TextStlWriter writer = new TextStlWriter(out)) {

            writer.setDoubleFormat(DoubleFormats.createDefault(0, -3));
            writer.setLineSeparator("\r\n");

            // act
            writer.startSolid();
            writer.writeTriangles(vertices, normal);
        }

        // assert
        Assertions.assertEquals(
            "solid \r\n" +
            "facet 0.0 0.0 1.0\r\n" +
            "outer loop\r\n" +
            "vertex 0.0 0.0 0.0\r\n" +
            "vertex 0.333 0.0 0.0\r\n" +
            "vertex 0.0 0.333 0.0\r\n" +
            "endloop\r\n" +
            "endfacet\r\n" +
            "endsolid \r\n", out.toString());
    }

    @Test
    public void testWrite_badFacet_withNormal() throws IOException {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO, Vector3D.ZERO, Vector3D.ZERO);
        final Vector3D normal = Vector3D.Unit.PLUS_Z;

        try (TextStlWriter writer = new TextStlWriter(out)) {
            // act
            writer.startSolid();
            writer.writeTriangles(vertices, normal);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet 0.0 0.0 1.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 0.0 0.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }

    @Test
    public void testWrite_badFacet_noNormal() throws IOException {
        // arrange
        final List<Vector3D> vertices = Arrays.asList(
                Vector3D.ZERO, Vector3D.ZERO, Vector3D.ZERO);

        try (TextStlWriter writer = new TextStlWriter(out)) {
            // act
            writer.startSolid();
            writer.writeTriangles(vertices, null);
        }

        // assert
        Assertions.assertEquals(
            "solid \n" +
            "facet 0.0 0.0 0.0\n" +
            "outer loop\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 0.0 0.0 0.0\n" +
            "vertex 0.0 0.0 0.0\n" +
            "endloop\n" +
            "endfacet\n" +
            "endsolid \n", out.toString());
    }
}
