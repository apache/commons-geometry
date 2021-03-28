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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BinaryStlWriterTest {

    private static final double TEST_EPS = 1e-7;

    private static final int VECTOR_SIZE = 3 * Float.BYTES;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    public void testWriteHeader_nullHeaderContent() throws IOException {
        // act
        try (BinaryStlWriter writer = new BinaryStlWriter(out)) {
            writer.writeHeader(null, Short.MAX_VALUE);
        }

        // assert
        final byte[] bytes = out.toByteArray();
        Assertions.assertEquals(StlConstants.BINARY_HEADER_BYTES + 4, bytes.length);

        assertBytes(0, bytes, 0, StlConstants.BINARY_HEADER_BYTES);
        Assertions.assertEquals(Short.MAX_VALUE, readAsInt(bytes, StlConstants.BINARY_HEADER_BYTES, 4));
    }

    @Test
    public void testWriteHeader_givenHeaderContent() throws IOException {
        // arrange
        final byte[] headerContent = new byte[StlConstants.BINARY_HEADER_BYTES];
        Arrays.fill(headerContent, (byte) 1);

        // act
        try (BinaryStlWriter writer = new BinaryStlWriter(out)) {
            writer.writeHeader(headerContent, 1);
        }

        // assert
        final byte[] bytes = out.toByteArray();
        Assertions.assertEquals(StlConstants.BINARY_HEADER_BYTES + 4, bytes.length);

        assertBytes(1, bytes, 0, StlConstants.BINARY_HEADER_BYTES);
        Assertions.assertEquals(1, readAsInt(bytes, StlConstants.BINARY_HEADER_BYTES, 4));
    }

    @Test
    public void testWriteHeader_givenHeaderContentExceedsMaxLength() throws IOException {
        // arrange
        final byte[] headerContent = new byte[2 * StlConstants.BINARY_HEADER_BYTES];
        Arrays.fill(headerContent, (byte) 1);

        // act
        try (BinaryStlWriter writer = new BinaryStlWriter(out)) {
            writer.writeHeader(headerContent, 0);
        }

        // assert
        final byte[] bytes = out.toByteArray();
        Assertions.assertEquals(StlConstants.BINARY_HEADER_BYTES + 4, bytes.length);

        assertBytes(1, bytes, 0, StlConstants.BINARY_HEADER_BYTES);
        Assertions.assertEquals(0, readAsInt(bytes, StlConstants.BINARY_HEADER_BYTES, 4));
    }

    @Test
    public void testWriteFacet() throws IOException {
        // arrange
        try (BinaryStlWriter writer = new BinaryStlWriter(out)) {
            writer.writeHeader(null, 2);

            // act
            writer.writeTriangle(
                    Vector3D.of(1, 2, 3),
                    Vector3D.of(4, 5, 6),
                    Vector3D.of(7, 8, 9),
                    Vector3D.of(10, 11, 12));

            writer.writeTriangle(
                    Vector3D.of(-1, -2, -3),
                    Vector3D.of(-4, -5, -6),
                    Vector3D.of(-7, -8, -9),
                    Vector3D.of(-10, -11, -12),
                    512);
        }

        // assert
        final byte[] bytes = out.toByteArray();

        Assertions.assertEquals(StlConstants.BINARY_HEADER_BYTES + 4 + (2 * StlConstants.BINARY_TRIANGLE_BYTES),
                bytes.length);

        assertBytes(0, bytes, 0, StlConstants.BINARY_HEADER_BYTES);
        Assertions.assertEquals(2, readAsInt(bytes, StlConstants.BINARY_HEADER_BYTES, Integer.BYTES));

        int offset = StlConstants.BINARY_HEADER_BYTES + 4;

        final List<Vector3D> tri1 = readVectors(bytes, offset, 4);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(10, 11, 12).normalize(), tri1.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3), tri1.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4, 5, 6), tri1.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(7, 8, 9), tri1.get(3), TEST_EPS);
        offset += 4 * VECTOR_SIZE;

        Assertions.assertEquals(0, readAsInt(bytes, offset, 2));
        offset += 2;

        final List<Vector3D> tri2 = readVectors(bytes, offset, 4);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-10, -11, -12).normalize(), tri2.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, -2, -3), tri2.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-4, -5, -6), tri2.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-7, -8, -9), tri2.get(3), TEST_EPS);
        offset += 4 * VECTOR_SIZE;

        Assertions.assertEquals(512, readAsInt(bytes, offset, 2));
    }

    @Test
    public void testWriteFacet_ordersFacetCounterClockwise() throws IOException {
        // arrange
        try (BinaryStlWriter writer = new BinaryStlWriter(out)) {
            writer.writeHeader(null, 2);

            // act
            writer.writeTriangle(
                    Vector3D.ZERO,
                    Vector3D.of(1, 0, 0),
                    Vector3D.of(0, 1, 0),
                    Vector3D.of(0, 0, 1));

            writer.writeTriangle(
                    Vector3D.ZERO,
                    Vector3D.of(0, 1, 0),
                    Vector3D.of(1, 0, 0),
                    Vector3D.of(0, 0, 1));
        }

        // assert
        final byte[] bytes = out.toByteArray();

        Assertions.assertEquals(StlConstants.BINARY_HEADER_BYTES + 4 + (2 * StlConstants.BINARY_TRIANGLE_BYTES),
                bytes.length);

        assertBytes(0, bytes, 0, StlConstants.BINARY_HEADER_BYTES);
        Assertions.assertEquals(2, readAsInt(bytes, StlConstants.BINARY_HEADER_BYTES, Integer.BYTES));

        int offset = StlConstants.BINARY_HEADER_BYTES + 4;

        final List<Vector3D> tri1 = readVectors(bytes, offset, 4);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), tri1.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 0), tri1.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), tri1.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0), tri1.get(3), TEST_EPS);
        offset += 4 * VECTOR_SIZE;

        Assertions.assertEquals(0, readAsInt(bytes, offset, 2));
        offset += 2;

        final List<Vector3D> tri2 = readVectors(bytes, offset, 4);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), tri2.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 0), tri2.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), tri2.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0), tri2.get(3), TEST_EPS);
        offset += 4 * VECTOR_SIZE;

        Assertions.assertEquals(0, readAsInt(bytes, offset, 2));
    }

    @Test
    public void testWriteFacet_invalidNormalGiven() throws IOException {
        // arrange
        try (BinaryStlWriter writer = new BinaryStlWriter(out)) {
            writer.writeHeader(null, 3);

            // act
            writer.writeTriangle(
                    Vector3D.ZERO,
                    Vector3D.of(1, 0, 0),
                    Vector3D.of(0, 1, 0),
                    Vector3D.ZERO);

            writer.writeTriangle(
                    Vector3D.ZERO,
                    Vector3D.of(0, 1, 0),
                    Vector3D.of(1, 0, 0),
                    null,
                    512);

            writer.writeTriangle(
                    Vector3D.ZERO,
                    Vector3D.ZERO,
                    Vector3D.of(1, 1, 1),
                    null);
        }

        // assert
        final byte[] bytes = out.toByteArray();

        Assertions.assertEquals(StlConstants.BINARY_HEADER_BYTES + 4 + (3 * StlConstants.BINARY_TRIANGLE_BYTES),
                bytes.length);

        assertBytes(0, bytes, 0, StlConstants.BINARY_HEADER_BYTES);
        Assertions.assertEquals(3, readAsInt(bytes, StlConstants.BINARY_HEADER_BYTES, Integer.BYTES));

        int offset = StlConstants.BINARY_HEADER_BYTES + 4;

        final List<Vector3D> tri1 = readVectors(bytes, offset, 4);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), tri1.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 0), tri1.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), tri1.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0), tri1.get(3), TEST_EPS);
        offset += 4 * VECTOR_SIZE;

        Assertions.assertEquals(0, readAsInt(bytes, offset, 2));
        offset += 2;

        final List<Vector3D> tri2 = readVectors(bytes, offset, 4);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1), tri2.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 0), tri2.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0), tri2.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), tri2.get(3), TEST_EPS);
        offset += 4 * VECTOR_SIZE;

        Assertions.assertEquals(512, readAsInt(bytes, offset, 2));
        offset += 2;

        final List<Vector3D> tri3 = readVectors(bytes, offset, 4);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 0), tri3.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 0), tri3.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 0), tri3.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), tri3.get(3), TEST_EPS);
        offset += 4 * VECTOR_SIZE;

        Assertions.assertEquals(0, readAsInt(bytes, offset, 2));
    }

    private static void assertBytes(final int expected, final byte[] actual, final int offset, final int len) {
        for (int i = 0; i < len; ++i) {
            Assertions.assertEquals(expected, actual[i + offset]);
        }
    }

    private static int readAsInt(final byte[] bytes, final int offset, final int count) {
        int result = 0;

        for (int i = 0; i < count; ++i) {
            result |= Byte.toUnsignedInt(bytes[i + offset]) << (i * Byte.SIZE);
        }

        return result;
    }

    private static float readFloat(final byte[] bytes, final int offset) {
        final int value = readAsInt(bytes, offset, Float.BYTES);
        return Float.intBitsToFloat(value);
    }

    private static Vector3D readVector(final byte[] bytes, final int offset) {
        final double x = readFloat(bytes, offset);
        final double y = readFloat(bytes, offset + Float.BYTES);
        final double z = readFloat(bytes, offset + (2 * Float.BYTES));

        return Vector3D.of(x, y, z);
    }

    private static List<Vector3D> readVectors(final byte[] bytes, final int offset, final int vectorCount) {
        final List<Vector3D> vectors = new ArrayList<>(vectorCount);

        for (int i = 0; i < vectorCount; ++i) {
            vectors.add(readVector(bytes, (i * VECTOR_SIZE) + offset));
        }

        return vectors;
    }
}
