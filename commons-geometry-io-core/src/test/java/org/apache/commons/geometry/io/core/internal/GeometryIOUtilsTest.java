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
package org.apache.commons.geometry.io.core.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.io.core.test.CloseCountInputStream;
import org.apache.commons.geometry.io.core.test.CloseCountOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GeometryIOUtilsTest {

    @Test
    public void testGetFileExtension() {
        // act/assert
        Assertions.assertEquals(null, GeometryIOUtils.getFileExtension(null));
        Assertions.assertEquals("", GeometryIOUtils.getFileExtension(""));
        Assertions.assertEquals("", GeometryIOUtils.getFileExtension("abc"));
        Assertions.assertEquals("", GeometryIOUtils.getFileExtension("abc."));
        Assertions.assertEquals("txt", GeometryIOUtils.getFileExtension("abc.txt"));
        Assertions.assertEquals("X", GeometryIOUtils.getFileExtension("/a/b/c.X"));
        Assertions.assertEquals("jpg", GeometryIOUtils.getFileExtension("/a/b/c.d.jpg"));
    }

    @Test
    public void testCreateUnchecked() {
        // arrange
        final FileNotFoundException exc = new FileNotFoundException("test");

        // act
        final UncheckedIOException result = GeometryIOUtils.createUnchecked(exc);

        // assert
        Assertions.assertEquals("FileNotFoundException: test", result.getMessage());
        Assertions.assertSame(exc, result.getCause());
    }

    @Test
    public void testCreateCloseShieldInputStream() throws IOException {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[] {1}));

        // act
        final InputStream result = GeometryIOUtils.createCloseShieldInputStream(in);

        // assert
        Assertions.assertEquals(1, result.read());

        result.close();
        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    public void testCreateCloseShieldReader() throws IOException {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[] {10}));

        // act
        final Reader result = GeometryIOUtils.createCloseShieldReader(in, StandardCharsets.US_ASCII);

        // assert
        Assertions.assertEquals('\n', result.read());

        result.close();
        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    public void testCreateCloseShieldOutputStream() throws IOException {
        // arrange
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final CloseCountOutputStream out = new CloseCountOutputStream(bytes);

        // act
        final OutputStream result = GeometryIOUtils.createCloseShieldOutputStream(out);

        // assert
        result.write(10);
        Assertions.assertArrayEquals(new byte[] {10}, bytes.toByteArray());

        result.close();
        Assertions.assertEquals(0, out.getCloseCount());
    }

    @Test
    public void testCreateCloseShieldWriter() throws IOException {
        // arrange
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final CloseCountOutputStream out = new CloseCountOutputStream(bytes);

        // act
        final Writer result = GeometryIOUtils.createCloseShieldWriter(out, StandardCharsets.US_ASCII);

        // assert
        result.write('\n');
        result.flush();
        Assertions.assertArrayEquals(new byte[] {10}, bytes.toByteArray());

        result.close();
        Assertions.assertEquals(0, out.getCloseCount());
    }

    @Test
    public void testTryApplyCloseable() throws IOException {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[] {1}));

        // act
        final int result = GeometryIOUtils.tryApplyCloseable(i -> i.read(), () -> in);

        // assert
        Assertions.assertEquals(1, result);
        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    public void testTryApplyCloseable_supplierThrows() throws IOException {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            GeometryIOUtils.tryApplyCloseable(i -> {
                throw new IOException("fn");
            }, () -> {
                throw new IOException("supplier");
            });
        }, IOException.class, "supplier");
    }

    @Test
    public void testTryApplyCloseable_functionThrows() throws IOException {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            GeometryIOUtils.tryApplyCloseable(i -> {
                throw new IOException("fn");
            }, () -> in);
        }, IOException.class, "fn");

        Assertions.assertEquals(1, in.getCloseCount());
    }

    @Test
    public void testTryApplyCloseable_functionThrows_inputCloseThrows() throws IOException {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new CloseFailByteArrayInputStream(new byte[0]));

        // act/assert
        final Throwable thr = Assertions.assertThrows(IOException.class, () -> {
            GeometryIOUtils.tryApplyCloseable(i -> {
                throw new IOException("fn");
            }, () -> in);
        });

        Assertions.assertEquals(IOException.class, thr.getClass());
        Assertions.assertEquals("close", thr.getSuppressed()[0].getMessage());

        Assertions.assertEquals(1, in.getCloseCount());
    }

    @Test
    public void testCreateCloseableStream() throws IOException {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act/assert
        try (Stream<String> stream = GeometryIOUtils.createCloseableStream(i -> Stream.of("a"), () -> in)) {
            Assertions.assertEquals(Arrays.asList("a"), stream.collect(Collectors.toList()));
            Assertions.assertEquals(0, in.getCloseCount());
        }

        Assertions.assertEquals(1, in.getCloseCount());
    }

    @Test
    public void testCreateCloseableStream_closeThrows() throws IOException {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new CloseFailByteArrayInputStream(new byte[0]));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> GeometryIOUtils.createCloseableStream(i -> Stream.of("a"), () -> in).close(),
                UncheckedIOException.class, "IOException: close");
    }

    private static final class CloseFailByteArrayInputStream extends ByteArrayInputStream {

        CloseFailByteArrayInputStream(final byte[] buf) {
            super(buf);
        }

        @Override
        public void close() throws IOException {
            throw new IOException("close");
        }
    }
}
