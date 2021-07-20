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

import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TextFacetDefinitionWriterTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private StringWriter writer;

    private TextFacetDefinitionWriter fdWriter;

    @BeforeEach
    public void setup() {
        writer = new StringWriter();
        fdWriter = new TextFacetDefinitionWriter(writer);
    }

    @Test
    void testPropertyDefaults() {
        // act/assert
        Assertions.assertEquals("\n", fdWriter.getLineSeparator());
        Assertions.assertNotNull(fdWriter.getDoubleFormat());
        Assertions.assertEquals(" ", fdWriter.getVertexComponentSeparator());
        Assertions.assertEquals("; ", fdWriter.getVertexSeparator());
        Assertions.assertEquals(-1, fdWriter.getFacetVertexCount());
        Assertions.assertEquals("# ", fdWriter.getCommentToken());
    }

    @Test
    void testSetFacetVertexCount_normalizesToMinusOne() {
        // act
        fdWriter.setFacetVertexCount(-10);

        // assert
        Assertions.assertEquals(-1, fdWriter.getFacetVertexCount());
    }

    @Test
    void testSetFacetVertexCount_invalidArgs() {
        // arrange
        final String baseMsg = "Facet vertex count must be less than 0 or greater than 2; was ";

        // act
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            fdWriter.setFacetVertexCount(0);
        }, IllegalArgumentException.class, baseMsg + "0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            fdWriter.setFacetVertexCount(1);
        }, IllegalArgumentException.class, baseMsg + "1");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            fdWriter.setFacetVertexCount(2);
        }, IllegalArgumentException.class, baseMsg + "2");
    }

    @Test
    void testSetCommentToken_invalidArgs() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            fdWriter.setCommentToken("");
        }, IllegalArgumentException.class, "Comment token cannot be empty");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            fdWriter.setCommentToken(" ");
        }, IllegalArgumentException.class, "Comment token cannot begin with whitespace");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            fdWriter.setCommentToken("\n \t");
        }, IllegalArgumentException.class, "Comment token cannot begin with whitespace");
    }

    @Test
    void testWriteComment() throws IOException {
        // arrange
        fdWriter.setCommentToken("-- ");
        fdWriter.setLineSeparator("\r\n");

        // act
        fdWriter.writeComment("first line");
        fdWriter.writeComment(null);
        fdWriter.writeComment("second line \n third line \r\nfourth line");

        // assert
        Assertions.assertEquals(
                "-- first line\r\n" +
                "-- second line \r\n" +
                "--  third line \r\n" +
                "-- fourth line\r\n", writer.toString());
    }

    @Test
    void testWriteComment_noCommentToken() throws IOException {
        // arrange
        fdWriter.setCommentToken(null);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            fdWriter.writeComment("comment");
        }, IllegalStateException.class, "Cannot write comment: no comment token configured");
    }

    @Test
    void testWriteBlankLine() throws IOException {
        // act
        fdWriter.writeBlankLine();
        fdWriter.setLineSeparator("\r");
        fdWriter.writeBlankLine();

        // assert
        Assertions.assertEquals("\n\r", writer.toString());
    }

    @Test
    void testWriteVertices() throws IOException {
        // arrange
        final List<Vector3D> vertices1 = Arrays.asList(
                Vector3D.ZERO, Vector3D.of(0.5, 0, 0), Vector3D.of(0, -0.5, 0));
        final List<Vector3D> vertices2 = Arrays.asList(
                Vector3D.of(0.5, 0.7, 1.2), Vector3D.of(10.01, -4, 2), Vector3D.of(-10.0 / 3.0, 0, 0), Vector3D.ZERO);

        // act
        fdWriter.write(vertices1);
        fdWriter.write(vertices2);

        // assert
        Assertions.assertEquals(
                "0.0 0.0 0.0; 0.5 0.0 0.0; 0.0 -0.5 0.0\n" +
                "0.5 0.7 1.2; 10.01 -4.0 2.0; -3.3333333333333335 0.0 0.0; 0.0 0.0 0.0\n", writer.toString());
    }

    @Test
    void testWriteVertices_invalidCount() throws IOException {
        // arrange
        fdWriter.setFacetVertexCount(4);
        final List<Vector3D> notEnough = Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X);
        final List<Vector3D> tooMany = Arrays.asList(
                Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y,
                Vector3D.Unit.MINUS_X, Vector3D.Unit.MINUS_Y);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            fdWriter.write(notEnough);
        }, IllegalArgumentException.class, "At least 3 vertices are required per facet; found 2");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            fdWriter.write(tooMany);
        }, IllegalArgumentException.class, "Writer requires 4 vertices per facet; found 5");
    }

    @Test
    void testWriteFacetDefinition() throws IOException {
        // arrange
        final DecimalFormat fmt =
                new DecimalFormat("0.0##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

        final SimpleFacetDefinition f1 = new SimpleFacetDefinition(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(0.5, 0, 0), Vector3D.of(0, -0.5, 0)));
        final SimpleFacetDefinition f2 = new SimpleFacetDefinition(Arrays.asList(
                Vector3D.of(0.5, 0.7, 1.2), Vector3D.of(10.01, -4, 2), Vector3D.of(-10.0 / 3.0, 0, 0), Vector3D.ZERO));

        fdWriter.setDoubleFormat(fmt::format);

        // act
        fdWriter.write(f1);
        fdWriter.write(f2);

        // assert
        Assertions.assertEquals(
                "0.0 0.0 0.0; 0.5 0.0 0.0; 0.0 -0.5 0.0\n" +
                "0.5 0.7 1.2; 10.01 -4.0 2.0; -3.333 0.0 0.0; 0.0 0.0 0.0\n", writer.toString());
    }

    @Test
    void testWriteFacetDefinition_invalidCount() throws IOException {
        // arrange
        fdWriter.setFacetVertexCount(4);
        final SimpleFacetDefinition tooMany = new SimpleFacetDefinition(Arrays.asList(
                Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y,
                Vector3D.Unit.MINUS_X, Vector3D.Unit.MINUS_Y));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            fdWriter.write(tooMany);
        }, IllegalArgumentException.class, "Writer requires 4 vertices per facet; found 5");
    }

    @Test
    void testWritePlaneConvexSubset() throws IOException {
        // arrange
        final ConvexPolygon3D poly1 = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(0, 0, -0.5), Vector3D.of(0, -0.5, 0)
                ), TEST_PRECISION);
        final ConvexPolygon3D poly2 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)
            ), TEST_PRECISION);

        // act
        fdWriter.write(poly1);
        fdWriter.write(poly2);

        // assert
        Assertions.assertEquals(
                "0.0 0.0 0.0; 0.0 0.0 -0.5; 0.0 -0.5 0.0\n" +
                "0.0 0.0 0.0; 1.0 0.0 0.0; 1.0 1.0 0.0; 0.0 1.0 0.0\n", writer.toString());
    }

    @Test
    void testWritePlaneConvexSubset_convertsToTriangles() throws IOException {
        // arrange
        final ConvexPolygon3D poly = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(0, 1, 0), Vector3D.of(0, 1, 1), Vector3D.of(0, 0, 1)
                ), TEST_PRECISION);

        fdWriter.setFacetVertexCount(3);

        // act
        fdWriter.write(poly);

        // assert
        Assertions.assertEquals(
                "0.0 0.0 0.0; 0.0 1.0 0.0; 0.0 1.0 1.0\n" +
                "0.0 0.0 0.0; 0.0 1.0 1.0; 0.0 0.0 1.0\n", writer.toString());
    }

    @Test
    void testWritePlaneConvexSubset_infinite() throws IOException {
        // arrange
        final PlaneConvexSubset inf = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION).span();

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            fdWriter.write(inf);
        }, IllegalArgumentException.class, "Cannot write infinite convex subset");
    }

    @Test
    void testWriteBoundarySource() throws IOException {
        // arrange
        final ConvexPolygon3D poly1 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(0, 0, -0.5), Vector3D.of(0, -0.5, 0)
            ), TEST_PRECISION);
        final ConvexPolygon3D poly2 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)
            ), TEST_PRECISION);

        // act
        fdWriter.write(BoundarySource3D.of(poly1, poly2));

        // assert
        Assertions.assertEquals(
                "0.0 0.0 0.0; 0.0 0.0 -0.5; 0.0 -0.5 0.0\n" +
                "0.0 0.0 0.0; 1.0 0.0 0.0; 1.0 1.0 0.0; 0.0 1.0 0.0\n", writer.toString());
    }

    @Test
    void testWriteBoundarySource_empty() throws IOException {
        // act
        fdWriter.write(BoundarySource3D.of(Collections.emptyList()));

        // assert
        Assertions.assertEquals("", writer.toString());
    }

    @Test
    void testWriteBoundarySource_alternativeFormatting() throws IOException {
        // arrange
        final DecimalFormat fmt =
                new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        final ConvexPolygon3D poly1 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(0, 0, -0.5901), Vector3D.of(0, -0.501, 0)
            ), TEST_PRECISION);
        final ConvexPolygon3D poly2 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)
            ), TEST_PRECISION);

        fdWriter.setDoubleFormat(fmt::format);

        fdWriter.setFacetVertexCount(3);
        fdWriter.setLineSeparator("\r\n");
        fdWriter.setVertexComponentSeparator(",");
        fdWriter.setVertexSeparator(" | ");

        // act
        fdWriter.writeComment("Test boundary source");
        fdWriter.writeBlankLine();
        fdWriter.write(BoundarySource3D.of(poly1, poly2));

        // assert
        Assertions.assertEquals(
                "# Test boundary source\r\n" +
                "\r\n" +
                "0.0,0.0,0.0 | 0.0,0.0,-0.6 | 0.0,-0.5,0.0\r\n" +
                "0.0,0.0,0.0 | 1.0,0.0,0.0 | 1.0,1.0,0.0\r\n" +
                "0.0,0.0,0.0 | 1.0,1.0,0.0 | 0.0,1.0,0.0\r\n", writer.toString());
    }

    @Test
    void testCsvFormat() throws IOException {
        // arrange
        final ConvexPolygon3D poly1 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(0, 0, -0.5901), Vector3D.of(0, -0.501, 0)
            ), TEST_PRECISION);
        final ConvexPolygon3D poly2 = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)
            ), TEST_PRECISION);

        final TextFacetDefinitionWriter csvWriter = TextFacetDefinitionWriter.csvFormat(writer);

        // act
        csvWriter.write(BoundarySource3D.of(poly1, poly2));

        // assert
        Assertions.assertEquals(
                "0.0,0.0,0.0,0.0,0.0,-0.5901,0.0,-0.501,0.0\n" +
                "0.0,0.0,0.0,1.0,0.0,0.0,1.0,1.0,0.0\n" +
                "0.0,0.0,0.0,1.0,1.0,0.0,0.0,1.0,0.0\n", writer.toString());
    }

    @Test
    void testCsvFormat_properties() {
        // act
        final TextFacetDefinitionWriter csvWriter = TextFacetDefinitionWriter.csvFormat(writer);

        // act/assert
        Assertions.assertEquals(",", csvWriter.getVertexComponentSeparator());
        Assertions.assertEquals(",", csvWriter.getVertexSeparator());
        Assertions.assertNull(csvWriter.getCommentToken());
    }
}
