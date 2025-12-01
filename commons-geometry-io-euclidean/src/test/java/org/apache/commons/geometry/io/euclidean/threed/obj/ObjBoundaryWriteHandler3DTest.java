/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.io.euclidean.threed.obj;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.io.core.output.StreamGeometryOutput;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitions;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ObjBoundaryWriteHandler3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final List<FacetDefinition> FACETS = Arrays.asList(
            new SimpleFacetDefinition(Arrays.asList(Vector3D.ZERO, Vector3D.of(1.0 / 3.0, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0))),
            new SimpleFacetDefinition(Arrays.asList(Vector3D.ZERO, Vector3D.of(0, -1, 0), Vector3D.of(1.0 / 3.0, 0, 0))));

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private final ObjBoundaryWriteHandler3D handler = new ObjBoundaryWriteHandler3D();

    @Test
    void testProperties() {
        // act/assert
        Assertions.assertEquals(GeometryFormat3D.OBJ, handler.getFormat());
        Assertions.assertEquals(StandardCharsets.UTF_8, handler.getDefaultCharset());
        Assertions.assertEquals("\n", handler.getLineSeparator());
        Assertions.assertNotNull(handler.getDoubleFormat());
        Assertions.assertEquals(-1, handler.getMeshBufferBatchSize());
    }

    @Test
    void testWriteFacets() {
        // arrange
        final DecimalFormat fmt =
                new DecimalFormat("0.0#####", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

        // act
        handler.setDoubleFormat(fmt::format);
        handler.writeFacets(FACETS, new StreamGeometryOutput(out));

        // assert
        Assertions.assertEquals(
                "v 0.0 0.0 0.0\n" +
                "v 0.333333 0.0 0.0\n" +
                "v 1.0 1.0 0.0\n" +
                "v 0.0 1.0 0.0\n" +
                "v 0.0 -1.0 0.0\n" +
                "f 1 2 3 4\n" +
                "f 1 5 2\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    void testWriteFacets_usesOutputCharset() {
        // arrange
        final DecimalFormat fmt =
                new DecimalFormat("0.0#####", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

        // act
        handler.setDoubleFormat(fmt::format);
        handler.writeFacets(FACETS, new StreamGeometryOutput(out, null, StandardCharsets.UTF_16));

        // assert
        Assertions.assertEquals(
                "v 0.0 0.0 0.0\n" +
                "v 0.333333 0.0 0.0\n" +
                "v 1.0 1.0 0.0\n" +
                "v 0.0 1.0 0.0\n" +
                "v 0.0 -1.0 0.0\n" +
                "f 1 2 3 4\n" +
                "f 1 5 2\n", new String(out.toByteArray(), StandardCharsets.UTF_16));
    }

    @Test
    void testWriteFacets_customConfig() {
        // arrange
        // arrange
        final DecimalFormat fmt =
                new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

        handler.setDefaultCharset(StandardCharsets.UTF_16);
        handler.setLineSeparator("\r\n");
        handler.setDoubleFormat(fmt::format);
        handler.setMeshBufferBatchSize(1);

        // act
        handler.writeFacets(FACETS, new StreamGeometryOutput(out));

        // assert
        Assertions.assertEquals(
                "v 0.0 0.0 0.0\r\n" +
                "v 0.3 0.0 0.0\r\n" +
                "v 1.0 1.0 0.0\r\n" +
                "v 0.0 1.0 0.0\r\n" +
                "f 1 2 3 4\r\n" +
                "v 0.0 0.0 0.0\r\n" +
                "v 0.0 -1.0 0.0\r\n" +
                "v 0.3 0.0 0.0\r\n" +
                "f 5 6 7\r\n", new String(out.toByteArray(), StandardCharsets.UTF_16));
    }

    @Test
    void testWrite() {
        // arrange
        final BoundarySource3D src = BoundarySource3D.of(FACETS.stream()
                .map(f -> FacetDefinitions.toPolygon(f, TEST_PRECISION))
                .collect(Collectors.toList()));

        // act
        handler.write(src, new StreamGeometryOutput(out));

        // assert
        Assertions.assertEquals(
                "v 0.0 0.0 0.0\n" +
                "v 0.3333333333333333 0.0 0.0\n" +
                "v 1.0 1.0 0.0\n" +
                "v 0.0 1.0 0.0\n" +
                "v 0.0 -1.0 0.0\n" +
                "f 1 2 3 4\n" +
                "f 1 5 2\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    void testWrite_customConfig() {
        // arrange
        final BoundarySource3D src = BoundarySource3D.of(FACETS.stream()
                .map(f -> FacetDefinitions.toPolygon(f, TEST_PRECISION))
                .collect(Collectors.toList()));

        // arrange
        final DecimalFormat fmt =
                new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

        handler.setDefaultCharset(StandardCharsets.UTF_16);
        handler.setLineSeparator("\r\n");
        handler.setDoubleFormat(fmt::format);
        handler.setMeshBufferBatchSize(1);

        // act
        handler.write(src, new StreamGeometryOutput(out));

        // assert
        Assertions.assertEquals(
                "v 0.0 0.0 0.0\r\n" +
                "v 0.3 0.0 0.0\r\n" +
                "v 1.0 1.0 0.0\r\n" +
                "v 0.0 1.0 0.0\r\n" +
                "f 1 2 3 4\r\n" +
                "v 0.0 0.0 0.0\r\n" +
                "v 0.0 -1.0 0.0\r\n" +
                "v 0.3 0.0 0.0\r\n" +
                "f 5 6 7\r\n", new String(out.toByteArray(), StandardCharsets.UTF_16));
    }

    @Test
    void testWrite_mesh() {
        // arrange
        final SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(TEST_PRECISION);
        builder.addFaceAndVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0));
        builder.addVertex(Vector3D.of(2, 3, 4)); // extra, unused vertex

        final BoundarySource3D src = builder.build();

        // act
        handler.write(src, new StreamGeometryOutput(out));

        // assert
        Assertions.assertEquals(
                "v 0.0 0.0 0.0\n" +
                "v 1.0 0.0 0.0\n" +
                "v 0.0 1.0 0.0\n" +
                "v 2.0 3.0 4.0\n" +
                "f 1 2 3\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }
}
