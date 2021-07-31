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
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.output.StreamGeometryOutput;
import org.apache.commons.geometry.io.core.test.CloseCountOutputStream;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TextBoundaryWriteHandler3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

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
    void testProperties() {
        // arrange
        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();

        // act/assert
        Assertions.assertEquals(GeometryFormat3D.TXT, handler.getFormat());
        Assertions.assertEquals(StandardCharsets.UTF_8, handler.getDefaultCharset());
        Assertions.assertEquals("\n", handler.getLineSeparator());
        Assertions.assertEquals(" ", handler.getVertexComponentSeparator());
        Assertions.assertEquals("; ", handler.getVertexSeparator());
        Assertions.assertNotNull(handler.getDoubleFormat());
        Assertions.assertEquals(-1, handler.getFacetVertexCount());
    }

    @Test
    void testWriteFacets() {
        // arrange
        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();
        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act
        handler.writeFacets(TRI_FACETS, new StreamGeometryOutput(closeOut));

        // assert
        Assertions.assertEquals(1, closeOut.getCloseCount());
        Assertions.assertEquals(
                "0.0 0.0 0.0; 0.3333333333333333 0.0 0.0; 1.0 1.0 0.0\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    void testWriteFacets_usesOutputCharset() {
        // arrange
        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();
        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act
        handler.writeFacets(TRI_FACETS, new StreamGeometryOutput(closeOut, null, StandardCharsets.UTF_16));

        // assert
        Assertions.assertEquals(1, closeOut.getCloseCount());
        Assertions.assertEquals(
                "0.0 0.0 0.0; 0.3333333333333333 0.0 0.0; 1.0 1.0 0.0\n", new String(out.toByteArray(), StandardCharsets.UTF_16));
    }

    @Test
    void testWriteFacets_customConfiguration() {
        // arrange
        final DecimalFormat fmt =
                new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();
        handler.setDefaultCharset(StandardCharsets.UTF_16);
        handler.setLineSeparator("\r\n");
        handler.setDoubleFormat(fmt::format);
        handler.setVertexComponentSeparator("|");
        handler.setVertexSeparator(" | ");
        handler.setFacetVertexCount(4);

        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act
        handler.writeFacets(QUAD_FACETS, new StreamGeometryOutput(closeOut));

        // assert
        Assertions.assertEquals(1, closeOut.getCloseCount());
        Assertions.assertEquals(
                "0.0|0.0|0.0 | 0.3|0.0|0.0 | 1.0|1.0|0.0 | 0.0|1.0|0.0\r\n", new String(out.toByteArray(), StandardCharsets.UTF_16));
    }

    @Test
    void testWriteBoundarySource() {
        // arrange
        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();
        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act
        handler.write(QUAD_SRC, new StreamGeometryOutput(closeOut));

        // assert
        Assertions.assertEquals(1, closeOut.getCloseCount());
        Assertions.assertEquals(
                "0.0 0.0 0.0; 0.3333333333333333 0.0 0.0; 1.0 1.0 0.0; 0.0 1.0 0.0\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    void testWriteBoundarySource_customConfiguration() {
        // arrange
        // arrange
        final DecimalFormat fmt =
                new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();
        handler.setDefaultCharset(StandardCharsets.UTF_16);
        handler.setLineSeparator("\r\n");
        handler.setDoubleFormat(fmt::format);
        handler.setVertexComponentSeparator("|");
        handler.setVertexSeparator(" | ");
        handler.setFacetVertexCount(4);

        final CloseCountOutputStream closeOut = new CloseCountOutputStream(out);

        // act
        handler.write(QUAD_SRC, new StreamGeometryOutput(closeOut));

        // assert
        Assertions.assertEquals(1, closeOut.getCloseCount());
        Assertions.assertEquals(
                "0.0|0.0|0.0 | 0.3|0.0|0.0 | 1.0|1.0|0.0 | 0.0|1.0|0.0\r\n", new String(out.toByteArray(), StandardCharsets.UTF_16));
    }
}
