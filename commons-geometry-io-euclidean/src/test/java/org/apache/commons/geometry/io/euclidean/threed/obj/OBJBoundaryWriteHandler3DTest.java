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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitions;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OBJBoundaryWriteHandler3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final List<FacetDefinition> FACETS = Arrays.asList(
            new SimpleFacetDefinition(Arrays.asList(Vector3D.ZERO, Vector3D.of(1.0 / 3.0, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0))),
            new SimpleFacetDefinition(Arrays.asList(Vector3D.ZERO, Vector3D.of(0, -1, 0), Vector3D.of(1.0 / 3.0, 0, 0))));

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private final OBJBoundaryWriteHandler3D handler = new OBJBoundaryWriteHandler3D();

    @Test
    public void testPropertyDefaults() {
        // act/assert
        Assertions.assertEquals(StandardCharsets.UTF_8, handler.getCharset());
        Assertions.assertEquals("\n", handler.getLineSeparator());
        Assertions.assertNull(handler.getDecimalFormatPattern());
        Assertions.assertEquals(-1, handler.getMeshBufferBatchSize());
    }

    @Test
    public void testWriteFacets() throws IOException {
        // act
        handler.writeFacets(FACETS, out);

        // assert
        Assertions.assertEquals(
                "v 0 0 0\n" +
                "v 0.333333 0 0\n" +
                "v 1 1 0\n" +
                "v 0 1 0\n" +
                "v 0 -1 0\n" +
                "f 1 2 3 4\n" +
                "f 1 5 2\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testWriteFacets_customConfig() throws IOException {
        // arrange
        handler.setCharset(StandardCharsets.UTF_16);
        handler.setLineSeparator("\r\n");
        handler.setDecimalFormatPattern("0.0");
        handler.setMeshBufferBatchSize(1);

        // act
        handler.writeFacets(FACETS, out);

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
    public void testWrite() throws IOException {
        // arrange
        final BoundarySource3D src = BoundarySource3D.of(FACETS.stream()
                .map(f -> FacetDefinitions.toPolygon(f, TEST_PRECISION))
                .collect(Collectors.toList()));

        // act
        handler.write(src, out);

        // assert
        Assertions.assertEquals(
                "v 0 0 0\n" +
                "v 0.333333 0 0\n" +
                "v 1 1 0\n" +
                "v 0 1 0\n" +
                "v 0 -1 0\n" +
                "f 1 2 3 4\n" +
                "f 1 5 2\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testWrite_customConfig() throws IOException {
        // arrange
        final BoundarySource3D src = BoundarySource3D.of(FACETS.stream()
                .map(f -> FacetDefinitions.toPolygon(f, TEST_PRECISION))
                .collect(Collectors.toList()));

        handler.setCharset(StandardCharsets.UTF_16);
        handler.setLineSeparator("\r\n");
        handler.setDecimalFormatPattern("0.0");
        handler.setMeshBufferBatchSize(1);

        // act
        handler.write(src, out);

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
    public void testWrite_mesh() throws IOException {
        // arrange
        final SimpleTriangleMesh.Builder builder = SimpleTriangleMesh.builder(TEST_PRECISION);
        builder.addFaceAndVertices(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0));
        builder.addVertex(Vector3D.of(2, 3, 4)); // extra, unused vertex

        final BoundarySource3D src = builder.build();

        // act
        handler.write(src, out);

        // assert
        Assertions.assertEquals(
                "v 0 0 0\n" +
                "v 1 0 0\n" +
                "v 0 1 0\n" +
                "v 2 3 4\n" +
                "f 1 2 3\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }
}
