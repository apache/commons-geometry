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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundaryList3D;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.input.StreamGeometryInput;
import org.apache.commons.geometry.io.core.input.UrlGeometryInput;
import org.apache.commons.geometry.io.core.test.CloseCountInputStream;
import org.apache.commons.geometry.io.euclidean.EuclideanIOTestUtils;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitions;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StlBoundaryReadHandler3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private final StlBoundaryReadHandler3D handler = new StlBoundaryReadHandler3D();

    @Test
    void testProperties() {
        // assert
        Assertions.assertEquals(GeometryFormat3D.STL, handler.getFormat());
        Assertions.assertEquals(StandardCharsets.UTF_8, handler.getDefaultCharset());
    }

    @Test
    void testReadMethods_cubeAscii() throws IOException {
        // arrange
        final URL url = EuclideanIOTestUtils.resource("/models/cube-ascii.stl");
        final GeometryInput input = new UrlGeometryInput(url);

        // act/assert
        EuclideanIOTestUtils.assertCube(readerToBoundarySource(handler.facetDefinitionReader(input)), TEST_EPS);
        EuclideanIOTestUtils.assertCube(facetsToBoundarySource(handler.facets(input)), TEST_EPS);
        EuclideanIOTestUtils.assertCube(handler.read(input, TEST_PRECISION), TEST_EPS);
        EuclideanIOTestUtils.assertCube(handler.readTriangleMesh(input, TEST_PRECISION), TEST_EPS);
        EuclideanIOTestUtils.assertCube(
                boundariesToBoundarySource(handler.boundaries(input, TEST_PRECISION)), TEST_EPS);
    }

    @Test
    void testReadMethods_cubeBinary() throws IOException {
        // arrange
        final URL url = EuclideanIOTestUtils.resource("/models/cube-ascii.stl");
        final GeometryInput input = new UrlGeometryInput(url);

        // act/assert
        EuclideanIOTestUtils.assertCube(readerToBoundarySource(handler.facetDefinitionReader(input)), TEST_EPS);
        EuclideanIOTestUtils.assertCube(facetsToBoundarySource(handler.facets(input)), TEST_EPS);
        EuclideanIOTestUtils.assertCube(handler.read(input, TEST_PRECISION), TEST_EPS);
        EuclideanIOTestUtils.assertCube(handler.readTriangleMesh(input, TEST_PRECISION), TEST_EPS);
        EuclideanIOTestUtils.assertCube(
                boundariesToBoundarySource(handler.boundaries(input, TEST_PRECISION)), TEST_EPS);
    }

    @Test
    void testRead_usesInputCharset() throws IOException {
        // arrange
        final String content = "solid test\n" +
                "facet normal 1 2 3 " +
                "outer loop " +
                    "vertex 4 5 6 " +
                    "vertex 7 8 9 " +
                    "vertex 10 11 12 " +
                "endloop " +
            "endfacet " +
            "endsolid test";

        final ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_16));
        final GeometryInput input = new StreamGeometryInput(in, null, StandardCharsets.UTF_16);

        // act/assert
        try (FacetDefinitionReader reader = handler.facetDefinitionReader(input)) {
            Assertions.assertNotNull(reader.readFacet());
            Assertions.assertNull(reader.readFacet());
        }
    }

    @Test
    void testRead_setDefaultCharset() throws IOException {
        // arrange
        final String content = "solid test\n" +
                "facet normal 1 2 3 " +
                "outer loop " +
                    "vertex 4 5 6 " +
                    "vertex 7 8 9 " +
                    "vertex 10 11 12 " +
                "endloop " +
            "endfacet " +
            "endsolid test";

        final ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_16));
        final GeometryInput input = new StreamGeometryInput(in);

        // act
        handler.setDefaultCharset(StandardCharsets.UTF_16);

        // assert
        try (FacetDefinitionReader reader = handler.facetDefinitionReader(input)) {
            Assertions.assertNotNull(reader.readFacet());
            Assertions.assertNull(reader.readFacet());
        }
    }

    @Test
    void testRead_incorrectCharset() throws IOException {
        // arrange
        final String content = "solid test\n" +
                "facet normal 1 2 3 " +
                "outer loop " +
                    "vertex 4 5 6 " +
                    "vertex 7 8 9 " +
                    "vertex 10 11 12 " +
                "endloop " +
            "endfacet " +
            "endsolid test";

        final ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_16));
        final GeometryInput input = new StreamGeometryInput(in);

        // act/assert
        try (FacetDefinitionReader reader = handler.facetDefinitionReader(input)) {
            Assertions.assertNotNull(reader.readFacet());
            Assertions.assertNotNull(reader.readFacet());

            Assertions.assertThrows(IOException.class, () -> reader.readFacet());
        }
    }

    @Test
    void testRead_notEnoughBytes() throws IOException {
        // arrange
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[1]);
        final GeometryInput input = new StreamGeometryInput(in);

        // act/assert
        Assertions.assertThrows(IOException.class, () -> handler.facetDefinitionReader(input));
    }

    @Test
    void testRead_closesInputOnReaderCreationFailure() throws IOException {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[1]));
        final GeometryInput input = new StreamGeometryInput(in);

        // act/assert
        Assertions.assertThrows(IOException.class, () -> handler.facetDefinitionReader(input));

        Assertions.assertEquals(1, in.getCloseCount());
    }

    private static BoundarySource3D boundariesToBoundarySource(final Stream<? extends PlaneConvexSubset> boundaries)
            throws IOException {
        try (Stream<? extends PlaneConvexSubset> toClose = boundaries) {
            return new BoundaryList3D(boundaries.collect(Collectors.toList()));
        }
    }

    private static BoundarySource3D facetsToBoundarySource(final Stream<? extends FacetDefinition> facets) {
        try (Stream<? extends FacetDefinition> toClose = facets) {
            final List<ConvexPolygon3D> polygons = facets
                    .map(f -> FacetDefinitions.toPolygon(f, TEST_PRECISION))
                    .collect(Collectors.toList());
            return new BoundaryList3D(polygons);
        }
    }

    private static BoundarySource3D readerToBoundarySource(final FacetDefinitionReader reader) throws IOException {
        return facetsToBoundarySource(EuclideanIOTestUtils.readAll(reader).stream());
    }
}
