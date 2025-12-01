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
package org.apache.commons.geometry.io.core.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.input.StreamGeometryInput;
import org.apache.commons.geometry.io.core.output.GeometryOutput;
import org.apache.commons.geometry.io.core.output.StreamGeometryOutput;
import org.apache.commons.geometry.io.core.test.CloseCountInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GeometryIOUtilsTest {

    @Test
    void testGetFileName_path() {
        // act/assert
        Assertions.assertNull(GeometryIOUtils.getFileName((Path) null));
        Assertions.assertNull(GeometryIOUtils.getFileName(Paths.get("")));

        Assertions.assertEquals("myfile", GeometryIOUtils.getFileName(Paths.get("myfile")));
        Assertions.assertEquals("myfile.txt", GeometryIOUtils.getFileName(Paths.get("path/to/myfile.txt")));
    }

    @Test
    void testGetFileName_url() throws MalformedURLException {
        // act/assert
        Assertions.assertNull(GeometryIOUtils.getFileName((URL) null));
        Assertions.assertNull(GeometryIOUtils.getFileName(new URL("http://test.com/")));

        Assertions.assertEquals("myfile.txt",
                GeometryIOUtils.getFileName(new URL("http://test.com/myfile.txt?a=otherfile.txt")));
    }

    @Test
    void testGetFileName_string() {
        // act/assert
        Assertions.assertNull(GeometryIOUtils.getFileName((String) null));
        Assertions.assertNull(GeometryIOUtils.getFileName(""));
        Assertions.assertNull(GeometryIOUtils.getFileName("some/path/"));
        Assertions.assertNull(GeometryIOUtils.getFileName("some\\path\\"));

        Assertions.assertEquals("myfile", GeometryIOUtils.getFileName("myfile"));
        Assertions.assertEquals("myfile.txt", GeometryIOUtils.getFileName(Paths.get("path/to/myfile.txt")));
        Assertions.assertEquals("myfile.txt", GeometryIOUtils.getFileName(Paths.get("/myfile.txt")));
        Assertions.assertEquals("myfile.txt", GeometryIOUtils.getFileName(Paths.get("path\\to\\myfile.txt")));
        Assertions.assertEquals("myfile.txt", GeometryIOUtils.getFileName(Paths.get("C:\\myfile.txt")));
    }

    @Test
    void testGetFileExtension() {
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
    void testCreateBufferedWriter_givenCharset() throws IOException {
        // arrange
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final GeometryOutput output = new StreamGeometryOutput(bytes, null, StandardCharsets.UTF_8);

        // act
        final BufferedWriter writer = GeometryIOUtils.createBufferedWriter(output, StandardCharsets.ISO_8859_1);
        writer.append('\u00fc');
        writer.flush();

        // assert
        Assertions.assertEquals("\u00fc", new String(bytes.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    void testCreateBufferedWriter_defaultCharset() throws IOException {
        // arrange
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final GeometryOutput output = new StreamGeometryOutput(bytes);

        // act
        final BufferedWriter writer = GeometryIOUtils.createBufferedWriter(output, StandardCharsets.ISO_8859_1);
        writer.append('\u00fc');
        writer.flush();

        // assert
        Assertions.assertEquals("\u00fc", new String(bytes.toByteArray(), StandardCharsets.ISO_8859_1));
    }

    @Test
    void testCreateBufferedReader_givenCharset() throws IOException {
        // arrange
        final byte[] bytes = "\u00fc".getBytes(StandardCharsets.UTF_8);
        final GeometryInput input = new StreamGeometryInput(
                new ByteArrayInputStream(bytes), null, StandardCharsets.UTF_8);

        // act
        final BufferedReader reader = GeometryIOUtils.createBufferedReader(input, StandardCharsets.ISO_8859_1);

        // assert
        Assertions.assertEquals("\u00fc", reader.readLine());
    }

    @Test
    void testCreateBufferedReader_defaultCharset() throws IOException {
        // arrange
        final byte[] bytes = "\u00fc".getBytes(StandardCharsets.UTF_8);
        final GeometryInput input = new StreamGeometryInput(new ByteArrayInputStream(bytes));

        // act
        final BufferedReader reader = GeometryIOUtils.createBufferedReader(input, StandardCharsets.UTF_8);

        // assert
        Assertions.assertEquals("\u00fc", reader.readLine());
    }

    @Test
    void testGetUnchecked() {
        // act
        final Object result = GeometryIOUtils.getUnchecked(() -> "abc");

        // assert
        Assertions.assertSame("abc", result);
    }

    @Test
    void testGetUnchecked_failure() {
        // arrange
        final IOSupplier<String> supplier = () -> {
            throw new IOException("test");
        };

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> GeometryIOUtils.getUnchecked(supplier),
                UncheckedIOException.class,
                "IOException: test");
    }

    @Test
    void testAcceptUnchecked() {
        // arrange
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] bytes = new byte[] {0, 1};

        // act
        GeometryIOUtils.acceptUnchecked(out::write, bytes);

        // assert
        Assertions.assertArrayEquals(bytes, out.toByteArray());
    }

    @Test
    void testAcceptUnchecked_failure() {
        // arrange
        final IOConsumer<String> consumer = str -> {
            throw new IOException(str);
        };

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> GeometryIOUtils.acceptUnchecked(consumer, "arg"),
                UncheckedIOException.class,
                "IOException: arg");
    }

    @Test
    void testApplyAsIntUnchecked() {
        // arrange
        final ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {0, 1, 2});
        final byte[] bytes = new byte[10];

        // act
        int result = GeometryIOUtils.applyAsIntUnchecked(in::read, bytes);

        // assert
        Assertions.assertEquals(3, result);
        Assertions.assertEquals((byte) 0, bytes[0]);
        Assertions.assertEquals((byte) 1, bytes[1]);
        Assertions.assertEquals((byte) 2, bytes[2]);
    }

    @Test
    void testApplyAsIntUnchecked_failure() {
        // arrange
        final IOToIntFunction<String> consumer = str -> {
            throw new IOException(str);
        };

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> GeometryIOUtils.applyAsIntUnchecked(consumer, "arg"),
                UncheckedIOException.class,
                "IOException: arg");
    }

    @Test
    void testCreateUnchecked() {
        // arrange
        final FileNotFoundException exc = new FileNotFoundException("test");

        // act
        final UncheckedIOException result = GeometryIOUtils.createUnchecked(exc);

        // assert
        Assertions.assertEquals("FileNotFoundException: test", result.getMessage());
        Assertions.assertSame(exc, result.getCause());
    }

    @Test
    void testParseError_noCause() {
        // act
        final IllegalStateException exc = GeometryIOUtils.parseError("test");

        // assert
        Assertions.assertEquals("test", exc.getMessage());
        Assertions.assertNull(exc.getCause());
    }

    @Test
    void testParseError_withCause() {
        // arrange
        final Throwable cause = new Throwable("cause");

        // act
        final IllegalStateException exc = GeometryIOUtils.parseError("test", cause);

        // assert
        Assertions.assertEquals("test", exc.getMessage());
        Assertions.assertSame(cause, exc.getCause());
    }

    @Test
    void testTryApplyCloseable() {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[] {1}));

        // act
        final int result = GeometryIOUtils.tryApplyCloseable(i -> i.read(), () -> in);

        // assert
        Assertions.assertEquals(1, result);
        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    void testTryApplyCloseable_supplierThrows_ioException() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            GeometryIOUtils.tryApplyCloseable(i -> {
                throw new IOException("fn");
            }, () -> {
                throw new IOException("supplier");
            });
        }, UncheckedIOException.class, "IOException: supplier");
    }

    @Test
    void testTryApplyCloseable_supplierThrows_runtimeException() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            GeometryIOUtils.tryApplyCloseable(i -> {
                throw new IOException("fn");
            }, () -> {
                throw new RuntimeException("supplier");
            });
        }, RuntimeException.class, "supplier");
    }


    @Test
    void testTryApplyCloseable_functionThrows() {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            GeometryIOUtils.tryApplyCloseable(i -> {
                throw new IOException("fn");
            }, () -> in);
        }, UncheckedIOException.class, "IOException: fn");

        Assertions.assertEquals(1, in.getCloseCount());
    }

    @Test
    void testTryApplyCloseable_functionThrows_inputCloseThrows() {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new CloseFailByteArrayInputStream(new byte[0]));

        // act/assert
        final Throwable thr = Assertions.assertThrows(UncheckedIOException.class, () -> {
            GeometryIOUtils.tryApplyCloseable(i -> {
                throw new IOException("fn");
            }, () -> in);
        });

        Assertions.assertEquals(UncheckedIOException.class, thr.getClass());
        Assertions.assertEquals("close", thr.getSuppressed()[0].getMessage());

        Assertions.assertEquals(1, in.getCloseCount());
    }

    @Test
    void testCreateCloseableStream() {
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
    void testCreateCloseableStream_closeThrows() {
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
