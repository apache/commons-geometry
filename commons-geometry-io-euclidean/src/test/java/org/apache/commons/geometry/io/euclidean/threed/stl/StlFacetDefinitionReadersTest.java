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
package org.apache.commons.geometry.io.euclidean.threed.stl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.threed.BoundaryList3D;
import org.apache.commons.geometry.io.euclidean.EuclideanIOTestUtils;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitions;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StlFacetDefinitionReadersTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testCreate_cubeBinaryFile() throws IOException {
        // arrange
        final URL url = EuclideanIOTestUtils.resource("/models/cube-binary.stl");

        // act
        try (FacetDefinitionReader reader = StlFacetDefinitionReaders.create(url.openStream(), null)) {

            // assert
            Assertions.assertEquals(BinaryStlFacetDefinitionReader.class, reader.getClass());

            BoundaryList3D boundaries = new BoundaryList3D(EuclideanIOTestUtils.readAll(reader).stream()
                    .map(f -> FacetDefinitions.toPolygon(f, TEST_PRECISION))
                    .collect(Collectors.toList()));
            EuclideanIOTestUtils.assertCube(boundaries, TEST_EPS);
        }
    }

    @Test
    void testCreate_cubeAsciiFile() throws IOException {
        // arrange
        final URL url = EuclideanIOTestUtils.resource("/models/cube-ascii.stl");

        // act
        try (FacetDefinitionReader reader = StlFacetDefinitionReaders.create(url.openStream(), null)) {

            // assert
            Assertions.assertEquals(TextStlFacetDefinitionReader.class, reader.getClass());

            BoundaryList3D boundaries = new BoundaryList3D(EuclideanIOTestUtils.readAll(reader).stream()
                    .map(f -> FacetDefinitions.toPolygon(f, TEST_PRECISION))
                    .collect(Collectors.toList()));
            EuclideanIOTestUtils.assertCube(boundaries, TEST_EPS);
        }
    }

    @Test
    void testCreate_nonStandardCharset_charsetGiven() {
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

        // act
        try (FacetDefinitionReader reader = StlFacetDefinitionReaders.create(in, StandardCharsets.UTF_16)) {

            // assert
            Assertions.assertEquals(TextStlFacetDefinitionReader.class, reader.getClass());

            Assertions.assertNotNull(reader.readFacet());
            Assertions.assertNull(reader.readFacet());
        }
    }

    @Test
    void testCreate_nonStandardCharset_noCharsetGiven() {
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

        // act
        try (FacetDefinitionReader reader = StlFacetDefinitionReaders.create(in, null)) {

            // assert
            Assertions.assertEquals(BinaryStlFacetDefinitionReader.class, reader.getClass());

            Assertions.assertNotNull(reader.readFacet());
            Assertions.assertNotNull(reader.readFacet());
            Assertions.assertThrows(IllegalStateException.class, reader::readFacet);
        }
    }

    @Test
    void testCreate_notEnoughBytes() {
        // arrange
        final byte[] bytes = new byte[1];
        final ByteArrayInputStream in = new ByteArrayInputStream(bytes);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> StlFacetDefinitionReaders.create(in, null),
                IllegalStateException.class,
                "Cannot determine STL format: attempted to read 5 bytes but found only 1 available");
    }
}
