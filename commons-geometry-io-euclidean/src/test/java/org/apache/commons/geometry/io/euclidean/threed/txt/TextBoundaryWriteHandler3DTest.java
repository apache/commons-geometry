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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.test.CloseCountOutputStream;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TextBoundaryWriteHandler3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final List<FacetDefinition> TRI_FACETS = Arrays.asList(new SimpleFacetDefinition(
            Arrays.asList(Vector3D.ZERO, Vector3D.of(1.0 / 3.0, 0, 0), Vector3D.of(1, 1, 0))));

    private static final List<FacetDefinition> QUAD_FACETS = Arrays.asList(new SimpleFacetDefinition(
            Arrays.asList(Vector3D.ZERO, Vector3D.of(1.0 / 3.0, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0))));

    private static final BoundarySource3D QUAD_SRC = BoundarySource3D.of(
            Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(1.0 / 3.0, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)),
                    TEST_PRECISION));

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    public void testPropertyDefaults() {
        // arrange
        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();

        // act/assert
        Assertions.assertEquals(StandardCharsets.UTF_8, handler.getCharset());
        Assertions.assertEquals("\n", handler.getLineSeparator());
        Assertions.assertEquals(" ", handler.getVertexComponentSeparator());
        Assertions.assertEquals("; ", handler.getVertexSeparator());
        Assertions.assertNull(handler.getDecimalFormatPattern());
        Assertions.assertEquals(-1, handler.getFacetVertexCount());
    }

    @Test
    public void testPropertyDefaults_csv() {
        // arrange
        final TextBoundaryWriteHandler3D handler = TextBoundaryWriteHandler3D.csvFormat();

        // act/assert
        Assertions.assertEquals(StandardCharsets.UTF_8, handler.getCharset());
        Assertions.assertEquals("\n", handler.getLineSeparator());
        Assertions.assertEquals(",", handler.getVertexComponentSeparator());
        Assertions.assertEquals(",", handler.getVertexSeparator());
        Assertions.assertEquals("0.0#####", handler.getDecimalFormatPattern());
        Assertions.assertEquals(3, handler.getFacetVertexCount());
    }

    @Test
    public void testWriteFacets() throws IOException {
        // arrange
        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();
        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act
        handler.writeFacets(TRI_FACETS, closeOut);

        // assert
        Assertions.assertEquals(0, closeOut.getCloseCount());
        Assertions.assertEquals(
                "0 0 0; 0.333333 0 0; 1 1 0\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testWriteFacets_csv() throws IOException {
        // arrange
        final TextBoundaryWriteHandler3D handler = TextBoundaryWriteHandler3D.csvFormat();
        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act
        handler.writeFacets(TRI_FACETS, closeOut);

        // assert
        Assertions.assertEquals(0, closeOut.getCloseCount());
        Assertions.assertEquals(
                "0.0,0.0,0.0,0.333333,0.0,0.0,1.0,1.0,0.0\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testWriteFacets_csv_wrongFacetCount() throws IOException {
        // arrange
        final TextBoundaryWriteHandler3D handler = TextBoundaryWriteHandler3D.csvFormat();
        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> handler.writeFacets(QUAD_FACETS, closeOut));

        Assertions.assertEquals(0, closeOut.getCloseCount());
    }

    @Test
    public void testWriteFacets_customConfiguration() throws IOException {
        // arrange
        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();
        handler.setCharset(StandardCharsets.UTF_16);
        handler.setLineSeparator("\r\n");
        handler.setDecimalFormatPattern("00.#");
        handler.setVertexComponentSeparator("|");
        handler.setVertexSeparator(" | ");
        handler.setFacetVertexCount(4);

        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act
        handler.writeFacets(QUAD_FACETS, closeOut);

        // assert
        Assertions.assertEquals(0, closeOut.getCloseCount());
        Assertions.assertEquals(
                "00|00|00 | 00.3|00|00 | 01|01|00 | 00|01|00\r\n", new String(out.toByteArray(), StandardCharsets.UTF_16));
    }

    @Test
    public void testWriteBoundarySource() throws IOException {
        // arrange
        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();
        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act
        handler.write(QUAD_SRC, closeOut);

        // assert
        Assertions.assertEquals(0, closeOut.getCloseCount());
        Assertions.assertEquals(
                "0 0 0; 0.333333 0 0; 1 1 0; 0 1 0\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testWriteBoundarySource_csv() throws IOException {
        // arrange
        final TextBoundaryWriteHandler3D handler = TextBoundaryWriteHandler3D.csvFormat();
        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act
        handler.write(QUAD_SRC, closeOut);

        // assert
        Assertions.assertEquals(0, closeOut.getCloseCount());
        Assertions.assertEquals(
                "0.333333,0.0,0.0,1.0,1.0,0.0,0.0,1.0,0.0\n" +
                "0.333333,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testWriteBoundarySource_customConfiguration() throws IOException {
        // arrange
        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();
        handler.setCharset(StandardCharsets.UTF_16);
        handler.setLineSeparator("\r\n");
        handler.setDecimalFormatPattern("00.#");
        handler.setVertexComponentSeparator("|");
        handler.setVertexSeparator(" | ");
        handler.setFacetVertexCount(4);

        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act
        handler.write(QUAD_SRC, closeOut);

        // assert
        Assertions.assertEquals(0, closeOut.getCloseCount());
        Assertions.assertEquals(
                "00|00|00 | 00.3|00|00 | 01|01|00 | 00|01|00\r\n", new String(out.toByteArray(), StandardCharsets.UTF_16));
    }
}
