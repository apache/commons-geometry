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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BinaryStlFacetDefinitionReaderTest {

    private static final double TEST_EPS = 1e-10;

    private static final String LONG_STRING =
            "A long string that will most definitely exceed the 80 byte length of the binary STL file format header.";

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    void testHeader_zeros() throws IOException {
        // arrange
        final byte[] bytes = new byte[StlConstants.BINARY_HEADER_BYTES + 4];
        out.write(bytes);

        final byte[] expectedHeader = new byte[StlConstants.BINARY_HEADER_BYTES];
        System.arraycopy(bytes, 0, expectedHeader, 0, expectedHeader.length);

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            Assertions.assertArrayEquals(expectedHeader, reader.getHeader().array());
            Assertions.assertEquals(0L, reader.getNumTriangles());

            Assertions.assertNull(reader.readFacet());
        }
    }

    @Test
    void testHeader_ones() throws IOException {
        // arrange
        final byte[] bytes = new byte[StlConstants.BINARY_HEADER_BYTES + 4];
        Arrays.fill(bytes, (byte) -1);
        out.write(bytes);

        final byte[] expectedHeader = new byte[StlConstants.BINARY_HEADER_BYTES];
        System.arraycopy(bytes, 0, expectedHeader, 0, expectedHeader.length);

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            Assertions.assertArrayEquals(expectedHeader, reader.getHeader().array());
            Assertions.assertEquals(0xffffffffL, reader.getNumTriangles());
        }
    }

    @Test
    void testHeader_shortString() throws IOException {
        // arrange
        out.write(createHeader("Hello!", StandardCharsets.UTF_8, 1));

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            Assertions.assertEquals("Hello!", reader.getHeaderAsString());
            Assertions.assertEquals(1L, reader.getNumTriangles());
        }
    }

    @Test
    void testHeader_longString() throws IOException {
        // arrange
        out.write(createHeader(LONG_STRING, StandardCharsets.UTF_8, 8736720));

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            Assertions.assertEquals(LONG_STRING.substring(0, StlConstants.BINARY_HEADER_BYTES),
                    reader.getHeaderAsString());
            Assertions.assertEquals(8736720L, reader.getNumTriangles());
        }
    }

    @Test
    void testHeader_longString_givenCharset() throws IOException {
        // arrange
        out.write(createHeader(LONG_STRING, StandardCharsets.UTF_16, 256));

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            Assertions.assertEquals("A long string that will most definitely",
                    reader.getHeaderAsString(StandardCharsets.UTF_16));
            Assertions.assertEquals(256L, reader.getNumTriangles());
        }
    }

    @Test
    void testGetHeader_noData() throws IOException {
        // arrange
        out.write(new byte[32]);

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    reader::getHeader,
                    IllegalStateException.class, "Failed to read STL header: data not available");
        }
    }

    @Test
    void testGetHeader_noTriangleCount() throws IOException {
        // arrange
        out.write(new byte[StlConstants.BINARY_HEADER_BYTES]);

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    reader::getHeader,
                    IllegalStateException.class, "Failed to read STL triangle count: data not available");
        }
    }

    @Test
    void testGetHeader_ioException() {
        // arrange
        final InputStream failIn = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("read");
            }
        };

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(failIn)) {
            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    reader::getHeader,
                    UncheckedIOException.class, "IOException: read");
        }
    }

    @Test
    void testReadFacet_noData() throws IOException {
        // arrange
        out.write(createHeader(1));

        // act/assert
        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            // act/assert
            GeometryTestUtils.assertThrowsWithMessage(
                    reader::readFacet,
                    IllegalStateException.class, "Failed to read STL triangle at index 0: data not available");
        }
    }

    @Test
    void testReadFacet() throws IOException {
        // arrange
        out.write(createHeader(2));

        out.write(getBytes(Vector3D.of(1, 2, 3)));
        out.write(getBytes(Vector3D.of(4, 5, 6)));
        out.write(getBytes(Vector3D.of(7, 8, 9)));
        out.write(getBytes(Vector3D.of(10, 11, 12)));
        out.write(getBytes((short) 1));

        out.write(getBytes(Vector3D.of(-1, -2, -3)));
        out.write(getBytes(Vector3D.of(-4, -5, -6)));
        out.write(getBytes(Vector3D.of(-7, -8, -9)));
        out.write(getBytes(Vector3D.of(-10, -11, -12)));
        out.write(getBytes((short) 65535));

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            Assertions.assertEquals(2, reader.getNumTriangles());

            final BinaryStlFacetDefinition facet1 = reader.readFacet();

            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3), facet1.getNormal(), TEST_EPS);
            Assertions.assertEquals(3, facet1.getVertices().size());
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4, 5, 6), facet1.getVertices().get(0), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(7, 8, 9), facet1.getVertices().get(1), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(10, 11, 12), facet1.getVertices().get(2), TEST_EPS);

            Assertions.assertEquals(1, facet1.getAttributeValue());

            final BinaryStlFacetDefinition facet2 = reader.readFacet();

            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, -2, -3), facet2.getNormal(), TEST_EPS);
            Assertions.assertEquals(3, facet2.getVertices().size());
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-4, -5, -6), facet2.getVertices().get(0), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-7, -8, -9), facet2.getVertices().get(1), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-10, -11, -12), facet2.getVertices().get(2), TEST_EPS);

            Assertions.assertEquals(65535, facet2.getAttributeValue());

            Assertions.assertNull(reader.readFacet());
        }
    }

    @Test
    void testReadFacet_stopsWhenTriangleCountReached() throws IOException {
        // arrange
        out.write(createHeader(1));

        out.write(getBytes(Vector3D.of(1, 2, 3)));
        out.write(getBytes(Vector3D.of(4, 5, 6)));
        out.write(getBytes(Vector3D.of(7, 8, 9)));
        out.write(getBytes(Vector3D.of(10, 11, 12)));
        out.write(getBytes((short) 1));

        out.write(getBytes(Vector3D.of(-1, -2, -3)));
        out.write(getBytes(Vector3D.of(-4, -5, -6)));
        out.write(getBytes(Vector3D.of(-7, -8, -9)));
        out.write(getBytes(Vector3D.of(-10, -11, -12)));
        out.write(getBytes((short) 65535));

        try (BinaryStlFacetDefinitionReader reader = new BinaryStlFacetDefinitionReader(getInput())) {
            Assertions.assertEquals(1, reader.getNumTriangles());

            final BinaryStlFacetDefinition facet = reader.readFacet();

            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3), facet.getNormal(), TEST_EPS);
            Assertions.assertEquals(3, facet.getVertices().size());
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4, 5, 6), facet.getVertices().get(0), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(7, 8, 9), facet.getVertices().get(1), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(10, 11, 12), facet.getVertices().get(2), TEST_EPS);

            Assertions.assertEquals(1, facet.getAttributeValue());

            Assertions.assertNull(reader.readFacet());
        }
    }

    private ByteArrayInputStream getInput() {
        return new ByteArrayInputStream(out.toByteArray());
    }

    private static byte[] createHeader(final int count) {
        return createHeader("", StandardCharsets.UTF_8, count);
    }

    private static byte[] createHeader(final String str, final Charset charset, final int count) {
        final byte[] result = new byte[StlConstants.BINARY_HEADER_BYTES + 4];

        final byte[] strBytes = str.getBytes(charset);
        System.arraycopy(strBytes, 0, result, 0, Math.min(StlConstants.BINARY_HEADER_BYTES, strBytes.length));

        final byte[] countBytes = getBytes(count);
        System.arraycopy(countBytes, 0, result, StlConstants.BINARY_HEADER_BYTES, countBytes.length);

        return result;
    }

    private static byte[] getBytes(final Vector3D vec) {
        final byte[] result = new byte[Float.BYTES * 3];
        int offset = 0;

        System.arraycopy(getBytes((float) vec.getX()), 0, result, offset, Float.BYTES);
        offset += Float.BYTES;

        System.arraycopy(getBytes((float) vec.getY()), 0, result, offset, Float.BYTES);
        offset += Float.BYTES;

        System.arraycopy(getBytes((float) vec.getZ()), 0, result, offset, Float.BYTES);

        return result;
    }

    private static byte[] getBytes(final float value) {
        return getBytes(Float.floatToIntBits(value));
    }

    private static byte[] getBytes(final int value) {
        final byte[] bytes = new byte[4];
        bytes[0] = (byte) (value & 0x000000ff);
        bytes[1] = (byte) ((value & 0x0000ff00) >> 8);
        bytes[2] = (byte) ((value & 0x00ff0000) >> 16);
        bytes[3] = (byte) ((value & 0xff000000) >> 24);

        return bytes;
    }

    private static byte[] getBytes(final short value) {
        final byte[] bytes = new byte[2];
        bytes[0] = (byte) (value & 0x00ff);
        bytes[1] = (byte) ((value & 0xff00) >> 8);

        return bytes;
    }
}
