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
package org.apache.commons.geometry.io.euclidean.threed.txt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.input.StreamGeometryInput;
import org.apache.commons.geometry.io.core.test.CloseCountInputStream;
import org.apache.commons.geometry.io.euclidean.EuclideanIOTestUtils;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextBoundaryReadHandler3DTest {

    private static final double TEST_EPS = 1e-10;

    private final TextBoundaryReadHandler3D handler = new TextBoundaryReadHandler3D();

    @Test
    public void testProperties() {
        // act/assert
        Assertions.assertEquals(GeometryFormat3D.TXT, handler.getFormat());
        Assertions.assertEquals(StandardCharsets.UTF_8, handler.getDefaultCharset());
    }

    @Test
    public void testFacetDefinitionReader() throws IOException {
        // arrange
        final InputStream in = input("0 0 0; 1 1 0; 0 1 0", StandardCharsets.UTF_8);

        // act
        final FacetDefinitionReader reader = handler.facetDefinitionReader(new StreamGeometryInput(in));

        // assert
        final List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        Assertions.assertEquals(1, facets.size());
        EuclideanIOTestUtils.assertFacetVertices(facets.get(0),
                Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)), TEST_EPS);
    }

    @Test
    public void testFacetDefinitionReader_usesInputCharset() throws IOException {
        // arrange
        final InputStream in = input("0 0 0; 1 1 0; 0 1 0", StandardCharsets.UTF_16);

        // act
        final FacetDefinitionReader reader = handler.facetDefinitionReader(new StreamGeometryInput(in, null, StandardCharsets.UTF_16));

        // assert
        final List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        Assertions.assertEquals(1, facets.size());
        EuclideanIOTestUtils.assertFacetVertices(facets.get(0),
                Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)), TEST_EPS);
    }

    @Test
    public void testFacetDefinitionReader_setDefaultCharset() throws IOException {
        // arrange
        handler.setDefaultCharset(StandardCharsets.UTF_16);
        final InputStream in = input("0 0 0; 1 1 0; 0 1 0", StandardCharsets.UTF_16);

        // act
        final FacetDefinitionReader reader = handler.facetDefinitionReader(new StreamGeometryInput(in));

        // assert
        final List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        Assertions.assertEquals(1, facets.size());
        EuclideanIOTestUtils.assertFacetVertices(facets.get(0),
                Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)), TEST_EPS);
    }

    @Test
    public void testFacetDefinitionReader_close() throws IOException {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(input("", StandardCharsets.UTF_8));

        // act/assert
        try (FacetDefinitionReader reader = handler.facetDefinitionReader(new StreamGeometryInput(in))) {
            Assertions.assertEquals(0, in.getCloseCount());
        }

        Assertions.assertEquals(1, in.getCloseCount());
    }

    private static ByteArrayInputStream input(final String str, final Charset charset) {
        return new ByteArrayInputStream(str.getBytes(charset));
    }
}
