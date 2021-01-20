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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.io.core.test.CloseCountInputStream;
import org.apache.commons.geometry.io.euclidean.EuclideanIOTestUtils;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OBJBoundaryReadHandler3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private final OBJBoundaryReadHandler3D handler = new OBJBoundaryReadHandler3D();

    @Test
    public void testDefaults() {
        // act/assert
        Assertions.assertEquals(StandardCharsets.UTF_8, handler.getCharset());
    }

    @Test
    public void testFacetDefinitionReader() throws IOException {
        // arrange
        final InputStream in = input(
                "v 0 0 0\n" +
                "v 1 1 0\n" +
                "v 0 1 0\n" +
                "f 1 2 3\n", StandardCharsets.UTF_8);

        // act
        final FacetDefinitionReader reader = handler.facetDefinitionReader(in);

        // assert
        final List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        Assertions.assertEquals(1, facets.size());
        EuclideanIOTestUtils.assertFacetVertices(facets.get(0),
                Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)), TEST_EPS);
    }

    @Test
    public void testFacetDefinitionReader_nonDefaultCharset() throws IOException {
        // arrange
        handler.setCharset(StandardCharsets.UTF_16);
        final InputStream in = input(
                "v 0 0 0\n" +
                "v 1 1 0\n" +
                "v 0 1 0\n" +
                "f 1 2 3\n", StandardCharsets.UTF_16);

        // act
        final FacetDefinitionReader reader = handler.facetDefinitionReader(in);

        // assert
        final List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        Assertions.assertEquals(1, facets.size());
        EuclideanIOTestUtils.assertFacetVertices(facets.get(0),
                Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)), TEST_EPS);
    }

    @Test
    public void testFacetDefinitionReader_close() throws IOException {
        // arrange
        final CloseCountInputStream in = input("", StandardCharsets.UTF_8);

        // act/assert
        try (FacetDefinitionReader reader = handler.facetDefinitionReader(in)) {
            Assertions.assertEquals(0, in.getCloseCount());
        }

        Assertions.assertEquals(1, in.getCloseCount());
    }

    @Test
    public void testReadTriangleMesh() throws IOException {
        // arrange
        final CloseCountInputStream in = input(
                "v 0 0 0\n" +
                "v 1 1 0\n" +
                "v 0 1 0\n" +
                "f 1 2 3\n", StandardCharsets.UTF_8);

        // act
        final TriangleMesh mesh = handler.readTriangleMesh(in, TEST_PRECISION);

        // assert
        Assertions.assertEquals(0, in.getCloseCount());

        Assertions.assertEquals(3, mesh.getVertexCount());
        Assertions.assertEquals(1, mesh.getFaceCount());

        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)),
                mesh.getFace(0).getVertices(), TEST_PRECISION);
    }

    @Test
    public void testReadTriangleMesh_nonDefaultCharset() throws IOException {
        // arrange
        handler.setCharset(StandardCharsets.UTF_16);
        final CloseCountInputStream in = input(
                "v 0 0 0\n" +
                "v 1 1 0\n" +
                "v 0 1 0\n" +
                "f 1 2 3\n", StandardCharsets.UTF_16);

        // act
        final TriangleMesh mesh = handler.readTriangleMesh(in, TEST_PRECISION);

        // assert
        Assertions.assertEquals(0, in.getCloseCount());

        Assertions.assertEquals(3, mesh.getVertexCount());
        Assertions.assertEquals(1, mesh.getFaceCount());

        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)),
                mesh.getFace(0).getVertices(), TEST_PRECISION);
    }

    private static CloseCountInputStream input(final String str, final Charset charset) {
        return new CloseCountInputStream(new ByteArrayInputStream(str.getBytes(charset)));
    }
}
