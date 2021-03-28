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
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.test.CloseCountReader;
import org.apache.commons.geometry.io.euclidean.EuclideanIOTestUtils;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextStlFacetDefinitionReaderTest {

    private static final double TEST_EPS = 1e-10;

    @Test
    public void testGetSolidName() throws IOException {
        // act/assert
        Assertions.assertEquals("Test Name", facetReader("solid    Test Name  \r\n").getSolidName());
        Assertions.assertEquals("Test", facetReader("solid    Test  ").getSolidName());
        Assertions.assertNull(facetReader("solid    ").getSolidName());
        Assertions.assertNull(facetReader("solid").getSolidName());
    }

    @Test
    public void testClose() throws IOException {
        // arrange
        final CloseCountReader countReader = new CloseCountReader(new StringReader(""));
        final TextStlFacetDefinitionReader reader = new TextStlFacetDefinitionReader(countReader);

        // act
        reader.close();

        // assert
        Assertions.assertEquals(1, countReader.getCloseCount());
    }

    @Test
    public void testEmpty() throws IOException {
        // arrange
        final TextStlFacetDefinitionReader reader = facetReader(
                "solid \n" +
                "endsolid");

        // act
        final List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertNull(reader.getSolidName());

        Assertions.assertEquals(0, facets.size());
        Assertions.assertNull(reader.readFacet());
    }

    @Test
    public void testSingleFacet() throws IOException {
        // arrange
        final TextStlFacetDefinitionReader reader = facetReader(
                "solid test\n" +
                "facet normal 1 2 3 " +
                    "outer loop " +
                        "vertex 4 5 6 " +
                        "vertex 7 8 9 " +
                        "vertex 10 11 12 " +
                    "endloop " +
                "endfacet " +
                "endsolid test");

        // act
        final List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertEquals("test", reader.getSolidName());

        Assertions.assertEquals(1, facets.size());

        EuclideanIOTestUtils.assertFacetVerticesAndNormal(
                facets.get(0),
                Arrays.asList(Vector3D.of(4, 5, 6), Vector3D.of(7, 8, 9), Vector3D.of(10, 11, 12)),
                Vector3D.of(1, 2, 3), TEST_EPS);

        Assertions.assertNull(reader.readFacet());
    }

    @Test
    public void testMultipleFacets() throws IOException {
        // arrange
        final TextStlFacetDefinitionReader reader = facetReader(
                "solid test solid\r\n\n" +
                "facet normal 1 2 3 " +
                    "outer loop " +
                        "vertex 4 5 6 " +
                        "vertex 7 8 9 " +
                        "vertex 10 11 12 " +
                    "endloop " +
                "endfacet " +
                "facet normal 0.0 0.0 0.0" +
                    "outer loop " +
                        "vertex 4e1 5.0e1 6.0e01 " +
                        "vertex 70.00 80.00 \t 90.00 " +
                        "vertex 10e+1 11e+1 12e+1 " +
                    "endloop " +
                "endfacet\n" +
                "facet   normal 0.1 0.2 0.3 " +
                    "outer  loop\n" +
                        "vertex 4e-1 5e-1 6e-1 " +
                        "vertex -0.07 \n -0.08 -0.09 " +
                        "vertex 10e-1 11e-1 12e-1 " +
                    "endloop\r" +
                "endfacet \r\n" +
                "endsolid test solid");

        // act
        final List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertEquals("test solid", reader.getSolidName());

        Assertions.assertEquals(3, facets.size());

        EuclideanIOTestUtils.assertFacetVerticesAndNormal(
                facets.get(0),
                Arrays.asList(Vector3D.of(4, 5, 6), Vector3D.of(7, 8, 9), Vector3D.of(10, 11, 12)),
                Vector3D.of(1, 2, 3), TEST_EPS);

        EuclideanIOTestUtils.assertFacetVerticesAndNormal(
                facets.get(1),
                Arrays.asList(Vector3D.of(40, 50, 60), Vector3D.of(70, 80, 90), Vector3D.of(100, 110, 120)),
                Vector3D.of(0, 0, 0), TEST_EPS);

        EuclideanIOTestUtils.assertFacetVerticesAndNormal(
                facets.get(2),
                Arrays.asList(Vector3D.of(0.4, 0.5, 0.6), Vector3D.of(-0.07, -0.08, -0.09), Vector3D.of(1, 1.1, 1.2)),
                Vector3D.of(0.1, 0.2, 0.3), TEST_EPS);

        Assertions.assertNull(reader.readFacet());
    }

    @Test
    public void testNoName() throws IOException {
        // arrange
        final TextStlFacetDefinitionReader reader = facetReader(
                "solid\n" +
                "facet normal 1 2 3 " +
                    "outer loop " +
                        "vertex 4 5 6 " +
                        "vertex 7 8 9 " +
                        "vertex 10 11 12 " +
                    "endloop " +
                "endfacet " +
                "endsolid");

        // act
        final List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertNull(reader.getSolidName());

        Assertions.assertEquals(1, facets.size());

        EuclideanIOTestUtils.assertFacetVerticesAndNormal(
                facets.get(0),
                Arrays.asList(Vector3D.of(4, 5, 6), Vector3D.of(7, 8, 9), Vector3D.of(10, 11, 12)),
                Vector3D.of(1, 2, 3), TEST_EPS);

        Assertions.assertNull(reader.readFacet());
    }

    @Test
    public void testContentEndsEarly() throws IOException {
        // arrange
        final TextStlFacetDefinitionReader reader = facetReader(
                "solid test\n" +
                "facet normal 1 2 3 " +
                    "outer loop " +
                        "vertex 4 5 6 " +
                        "vertex 7 8 9 " +
                        "vertex 10 11 12 " +
                    "endloop " +
                "endfacet");

        // act
        final List<FacetDefinition> facets = EuclideanIOTestUtils.readAll(reader);

        // assert
        Assertions.assertEquals("test", reader.getSolidName());

        Assertions.assertEquals(1, facets.size());

        EuclideanIOTestUtils.assertFacetVerticesAndNormal(
                facets.get(0),
                Arrays.asList(Vector3D.of(4, 5, 6), Vector3D.of(7, 8, 9), Vector3D.of(10, 11, 12)),
                Vector3D.of(1, 2, 3), TEST_EPS);

        Assertions.assertNull(reader.readFacet());
    }

    @Test
    public void testParseErrors() throws IOException {
        // act/assert
        assertParseError(
                "soli test\n" +
                "facet normal 1 2 3 " +
                    "outer loop " +
                        "vertex 4 5 6 " +
                        "vertex 7 8 9 " +
                        "vertex 10 11 12 " +
                    "endloop " +
                "endfacet " +
                "endsolid test");
        assertParseError(
                "solid test\n" +
                "facet normal 1 2 3 " +
                    "outer loop " +
                        "vertex abc 5 6 " +
                        "vertex 7 8 9 " +
                        "vertex 10 11 12 " +
                    "endloop " +
                "endfacet " +
                "endsolid test");
    }

    private static TextStlFacetDefinitionReader facetReader(final String content) {
        return new TextStlFacetDefinitionReader(new StringReader(content));
    }

    private static void assertParseError(final String content) {
        GeometryTestUtils.assertThrowsWithMessage(
                () -> EuclideanIOTestUtils.readAll(facetReader(content)),
                IOException.class,
                Pattern.compile("^Parsing failed.*"));
    }
}
