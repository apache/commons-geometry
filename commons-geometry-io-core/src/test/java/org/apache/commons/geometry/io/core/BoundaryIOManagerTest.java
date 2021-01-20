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
package org.apache.commons.geometry.io.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.partitioning.BoundaryList;
import org.apache.commons.geometry.core.partitioning.test.TestLineSegment;
import org.apache.commons.geometry.core.partitioning.test.TestPoint2D;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.io.core.test.CloseCountInputStream;
import org.apache.commons.geometry.io.core.test.CloseCountOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class BoundaryIOManagerTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION = new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final TestLineSegment SEGMENT = new TestLineSegment(TestPoint2D.ZERO, TestPoint2D.PLUS_X);

    private static final TestBoundaryList BOUNDARY_LIST = new TestBoundaryList(Collections.singletonList(SEGMENT));

    @TempDir
    Path tempDir;

    private final TestManager manager = new TestManager();

    @Test
    public void testRegisterHandlers() {
        // arrange
        final StubReadHandler reader = new StubReadHandler();
        final StubWriteHandler writer = new StubWriteHandler();

        // act
        manager.registerReadHandler("TESTReAd", new StubReadHandler());
        manager.registerReadHandler("TESTReAd", reader);
        manager.registerWriteHandler("testWRITE", new StubWriteHandler());
        manager.registerWriteHandler("testWRITE", writer);

        // assert
        Assertions.assertSame(reader, manager.getReadHandler("testRead"));
        Assertions.assertNull(manager.getReadHandler("testWrite"));

        Assertions.assertTrue(manager.readsFormat("testread"));
        Assertions.assertFalse(manager.readsFormat("testWrite"));
        Assertions.assertFalse(manager.readsFormat(null));

        Assertions.assertEquals(new HashSet<>(Arrays.asList("testread")), manager.getReadFormats());

        Assertions.assertSame(writer, manager.getWriteHandler("testwrite"));
        Assertions.assertNull(manager.getWriteHandler("testRead"));

        Assertions.assertTrue(manager.writesFormat("testWrite"));
        Assertions.assertFalse(manager.writesFormat("testRead"));
        Assertions.assertFalse(manager.writesFormat(null));

        Assertions.assertEquals(new HashSet<>(Arrays.asList("testwrite")), manager.getWriteFormats());
    }

    @Test
    public void testRegisterHandler_illegalArgs() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.registerReadHandler(null, new StubReadHandler()),
                NullPointerException.class, "Format name cannot be null");
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.registerReadHandler("test", null),
                NullPointerException.class, "Read handler cannot be null");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.registerWriteHandler(null, new StubWriteHandler()),
                NullPointerException.class, "Format name cannot be null");
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.registerWriteHandler("test", null),
                NullPointerException.class, "Write handler cannot be null");
    }

    @Test
    public void testRead_inputStream() throws IOException {
        // arrange
        final StubReadHandler readHandler = new StubReadHandler();
        manager.registerReadHandler("test", readHandler);

        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act
        final TestBoundaryList result = manager.read(in, "TEST", TEST_PRECISION);

        // assert
        Assertions.assertSame(BOUNDARY_LIST, result);

        Assertions.assertEquals(0, in.getCloseCount());

        Assertions.assertSame(in, readHandler.inArg);
        Assertions.assertSame(TEST_PRECISION, readHandler.precisionArg);
    }

    @Test
    public void testRead_inputStream_unknownFormat() {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.read(in, "TEST", TEST_PRECISION),
                IllegalArgumentException.class, "No read handler registered for format \"TEST\"");

        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    public void testRead_path() throws IOException {
        // arrange
        final Path file = tempDir.resolve("input.with.long.name.abc");
        Files.createFile(file);

        final StubReadHandler readHandler = new StubReadHandler();
        manager.registerReadHandler("ABC", readHandler);

        // act
        final TestBoundaryList result = manager.read(file, TEST_PRECISION);

        // assert
        Assertions.assertSame(BOUNDARY_LIST, result);
        Assertions.assertSame(TEST_PRECISION, readHandler.precisionArg);
    }

    @Test
    public void testRead_path_unknownFormat() throws IOException {
        // arrange
        final Path file = tempDir.resolve("input.abc");
        Files.createFile(file);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.read(file, TEST_PRECISION),
                IllegalArgumentException.class, "No read handler registered for format \"abc\"");
    }

    @Test
    public void testRead_path_noFileExtension() throws IOException {
        // arrange
        final Path file = tempDir.resolve("input");
        Files.createFile(file);

        final Pattern expectedMsgPattern = Pattern.compile(
                "^Cannot determine file data format: file name \".*input\" does not have a file extension$");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.read(file, TEST_PRECISION),
                IllegalArgumentException.class, expectedMsgPattern);
    }

    @Test
    public void testRead_url() throws IOException {
        // arrange
        final Path file = tempDir.resolve("input.with.long.name.abc");
        Files.createFile(file);
        final URL rawUrl = file.toUri().toURL();
        final URL url = new URL(rawUrl.toString() + "?extra.url.part");

        final StubReadHandler readHandler = new StubReadHandler();
        manager.registerReadHandler("ABC", readHandler);

        // act
        final TestBoundaryList result = manager.read(url, TEST_PRECISION);

        // assert
        Assertions.assertSame(BOUNDARY_LIST, result);
        Assertions.assertSame(TEST_PRECISION, readHandler.precisionArg);
    }

    @Test
    public void testRead_url_unknownFormat() throws IOException {
        // arrange
        final Path file = tempDir.resolve("input.abc");
        Files.createFile(file);
        final URL url = file.toUri().toURL();

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.read(url, TEST_PRECISION),
                IllegalArgumentException.class, "No read handler registered for format \"abc\"");
    }

    @Test
    public void testRead_urlFromJar() throws IOException {
        // arrange
        final URL url = BoundaryIOManager.class.getResource("/java/lang/String.class");

        final StubReadHandler readHandler = new StubReadHandler();
        manager.registerReadHandler("CLASS", readHandler);

        // act
        final TestBoundaryList result = manager.read(url, TEST_PRECISION);

        // assert
        Assertions.assertSame(BOUNDARY_LIST, result);
        Assertions.assertSame(TEST_PRECISION, readHandler.precisionArg);
    }

    @Test
    public void testRead_url_noFileExtension() throws IOException {
        // arrange
        final Path file = tempDir.resolve("input");
        Files.createFile(file);

        final URL url = file.toUri().toURL();

        final Pattern expectedMsgPattern = Pattern.compile(
                "^Cannot determine file data format: file name \".*input\" does not have a file extension$");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.read(url, TEST_PRECISION),
                IllegalArgumentException.class, expectedMsgPattern);
    }

    @Test
    public void testBoundaries_inputStream() throws IOException {
        // arrange
        final StubReadHandler readHandler = new StubReadHandler();
        manager.registerReadHandler("test", readHandler);

        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act
        final Stream<TestLineSegment> stream = manager.boundaries(in, "test", TEST_PRECISION);

        // assert
        final List<TestLineSegment> segments = stream.collect(Collectors.toList());
        Assertions.assertEquals(BOUNDARY_LIST.getBoundaries(), segments);

        Assertions.assertEquals(0, in.getCloseCount());

        Assertions.assertSame(in, readHandler.inArg);
        Assertions.assertSame(TEST_PRECISION, readHandler.precisionArg);
    }

    @Test
    public void testBoundaries_inputStream_unknownFormat() {
        // arrange
        final CloseCountInputStream in = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.boundaries(in, "TEST", TEST_PRECISION),
                IllegalArgumentException.class, "No read handler registered for format \"TEST\"");

        Assertions.assertEquals(0, in.getCloseCount());
    }

    @Test
    public void testBoundaries_path() throws IOException {
        // arrange
        final StubReadHandler readHandler = new StubReadHandler();
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("input.test");
        Files.createFile(path);

        // act
        final List<TestLineSegment> segments;
        try (Stream<TestLineSegment> stream = manager.boundaries(path, TEST_PRECISION)) {
            segments = stream.collect(Collectors.toList());
        }

        // assert
        Assertions.assertEquals(BOUNDARY_LIST.getBoundaries(), segments);
        Assertions.assertSame(TEST_PRECISION, readHandler.precisionArg);

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testBoundaries_path_fileNotFound() throws IOException {
        // arrange
        final StubReadHandler readHandler = new StubReadHandler();
        readHandler.boundariesFail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("input.test");

        // act/assert
        Assertions.assertThrows(FileNotFoundException.class,
                () -> manager.boundaries(path, TEST_PRECISION));
    }

    @Test
    public void testBoundaries_path_inputCloseFails() throws IOException {
        // arrange
        manager.inputStreamFailOnClose = true;

        final StubReadHandler readHandler = new StubReadHandler();
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("input.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            try (Stream<TestLineSegment> stream = manager.boundaries(path, TEST_PRECISION)) {
                stream.collect(Collectors.toList());
            }
        }, UncheckedIOException.class, "IOException: close fail");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testBoundaries_path_readHandlerThrows() throws IOException {
        // arrange
        final StubReadHandler readHandler = new StubReadHandler();
        readHandler.boundariesFail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("input.test");
        Files.createFile(path);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            manager.boundaries(path, TEST_PRECISION);
        }, IOException.class, "Test boundaries() failure");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testBoundaries_path_readHandlerThrows_inputCloseFails() throws IOException {
        // arrange
        manager.inputStreamFailOnClose = true;

        final StubReadHandler readHandler = new StubReadHandler();
        readHandler.boundariesFail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("input.test");
        Files.createFile(path);

        // act/assert
        final Throwable[] suppressed = Assertions.assertThrows(IOException.class,
                () -> manager.boundaries(path, TEST_PRECISION)).getSuppressed();
        Assertions.assertEquals(1, suppressed.length);
        Assertions.assertEquals("close fail", suppressed[0].getMessage());

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testBoundaries_url() throws IOException {
        // arrange
        final StubReadHandler readHandler = new StubReadHandler();
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("input.test");
        Files.createFile(path);

        final URL url = path.toUri().toURL();

        // act
        final List<TestLineSegment> segments;
        try (Stream<TestLineSegment> stream = manager.boundaries(url, TEST_PRECISION)) {
            segments = stream.collect(Collectors.toList());
        }

        // assert
        Assertions.assertEquals(BOUNDARY_LIST.getBoundaries(), segments);
        Assertions.assertSame(TEST_PRECISION, readHandler.precisionArg);

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testBoundaries_url_cannotOpenStream() throws IOException {
        // arrange
        final StubReadHandler readHandler = new StubReadHandler();
        readHandler.boundariesFail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("input.test");
        final URL url = path.toUri().toURL();

        // act/assert
        Assertions.assertThrows(FileNotFoundException.class,
                () -> manager.boundaries(url, TEST_PRECISION));
    }

    @Test
    public void testBoundaries_url_inputCloseFails() throws IOException {
        // arrange
        manager.inputStreamFailOnClose = true;

        final StubReadHandler readHandler = new StubReadHandler();
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("input.test");
        Files.createFile(path);

        final URL url = path.toUri().toURL();

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            try (Stream<TestLineSegment> stream = manager.boundaries(url, TEST_PRECISION)) {
                stream.collect(Collectors.toList());
            }
        }, UncheckedIOException.class, "IOException: close fail");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testBoundaries_url_readHandlerThrows() throws IOException {
        // arrange
        final StubReadHandler readHandler = new StubReadHandler();
        readHandler.boundariesFail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("input.test");
        Files.createFile(path);

        final URL url = path.toUri().toURL();

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            manager.boundaries(url, TEST_PRECISION);
        }, IOException.class, "Test boundaries() failure");

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testBoundaries_url_readHandlerThrows_inputCloseFails() throws IOException {
        // arrange
        manager.inputStreamFailOnClose = true;

        final StubReadHandler readHandler = new StubReadHandler();
        readHandler.boundariesFail = true;
        manager.registerReadHandler("test", readHandler);

        final Path path = tempDir.resolve("input.test");
        Files.createFile(path);

        final URL url = path.toUri().toURL();

        // act/assert
        final Throwable[] suppressed = Assertions.assertThrows(IOException.class,
                () -> manager.boundaries(url, TEST_PRECISION)).getSuppressed();
        Assertions.assertEquals(1, suppressed.length);
        Assertions.assertEquals("close fail", suppressed[0].getMessage());

        Assertions.assertEquals(1, manager.inputCloseCount);
    }

    @Test
    public void testWrite_outputStream() throws IOException {
        // arrange
        final StubWriteHandler writeHandler = new StubWriteHandler();
        manager.registerWriteHandler("TEST", writeHandler);

        final CloseCountOutputStream out = new CloseCountOutputStream(new ByteArrayOutputStream());

        // act
        manager.write(BOUNDARY_LIST, out, "test");

        // assert
        Assertions.assertSame(BOUNDARY_LIST, writeHandler.list);
        Assertions.assertSame(out, writeHandler.outArg);

        Assertions.assertEquals(0, out.getCloseCount());
    }

    @Test
    public void testWrite_outputStream_unknownFormat() throws IOException {
        // arrange
        final CloseCountOutputStream out = new CloseCountOutputStream(new ByteArrayOutputStream());

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.write(BOUNDARY_LIST, out, "test"),
                IllegalArgumentException.class, "No write handler registered for format \"test\"");

        Assertions.assertEquals(0, out.getCloseCount());
    }

    @Test
    public void testWrite_path() throws IOException {
        // arrange
        final Path path = tempDir.resolve("output.test");

        final StubWriteHandler writeHandler = new StubWriteHandler();
        manager.registerWriteHandler("TEST", writeHandler);

        // act
        manager.write(BOUNDARY_LIST, path);

        // assert
        Assertions.assertSame(BOUNDARY_LIST, writeHandler.list);
        Assertions.assertNotNull(writeHandler.outArg);

        Assertions.assertTrue(Files.exists(path));
    }

    @Test
    public void testWrite_path_overwritesExisting() throws IOException {
        // arrange
        final Path path = tempDir.resolve("output.test");
        Files.write(path, new byte[8]);

        final StubWriteHandler writeHandler = new StubWriteHandler();
        manager.registerWriteHandler("TEST", writeHandler);

        // act
        manager.write(BOUNDARY_LIST, path);

        // assert
        Assertions.assertSame(BOUNDARY_LIST, writeHandler.list);
        Assertions.assertNotNull(writeHandler.outArg);

        Assertions.assertTrue(Files.exists(path));
        Assertions.assertEquals(0L, Files.size(path));
    }

    @Test
    public void testWrite_path_unknownFormat() throws IOException {
        // arrange
        final Path path = tempDir.resolve("output.test");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.write(BOUNDARY_LIST, path),
                IllegalArgumentException.class, "No write handler registered for format \"test\"");
    }

    @Test
    public void testWrite_path_noFileExtension() throws IOException {
        // arrange
        final Path path = tempDir.resolve("output");

        final Pattern expectedMsgPattern = Pattern.compile(
                "^Cannot determine file data format: file name \".*output\" does not have a file extension$");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.write(BOUNDARY_LIST, path),
                IllegalArgumentException.class, expectedMsgPattern);
    }

    private static final class TestManager
        extends BoundaryIOManager<TestLineSegment, TestBoundaryList, StubReadHandler, StubWriteHandler> {

        /** If true, an exception will be thrown when close is called on input streams created by the registry. */
        private boolean inputStreamFailOnClose = false;

        /** Number of times close is called on an input stream created by the registry. */
        private int inputCloseCount;

        /** {@inheritDoc} */
        @Override
        protected InputStream getInputStream(final URL url) throws IOException {
            final InputStream in = super.getInputStream(url);

            return new FilterInputStream(in) {

                /** {@inheritDoc} */
                @Override
                public void close() throws IOException {
                    ++inputCloseCount;

                    IOException suppressed = null;
                    try {
                        super.close();
                    } catch (IOException exc) {
                        suppressed = exc;
                    }

                    if (inputStreamFailOnClose) {
                        final IOException exc = new IOException("close fail");

                        if (suppressed != null) {
                            exc.addSuppressed(suppressed);
                        }

                        throw exc;
                    }
                }
            };
        }
    }

    private static final class TestBoundaryList extends BoundaryList<TestPoint2D, TestLineSegment> {

        TestBoundaryList(final List<? extends TestLineSegment> boundaries) {
            super(boundaries);
        }
    }

    private static final class StubReadHandler implements BoundaryReadHandler<TestLineSegment, TestBoundaryList> {

        private InputStream inArg;

        private DoublePrecisionContext precisionArg;

        private boolean boundariesFail = false;

        /** {@inheritDoc} */
        @Override
        public TestBoundaryList read(final InputStream in, final DoublePrecisionContext precision)
                throws IOException {
            this.inArg = in;
            this.precisionArg = precision;

            return BOUNDARY_LIST;
        }

        /** {@inheritDoc} */
        @Override
        public Stream<TestLineSegment> boundaries(final InputStream in,
                final DoublePrecisionContext precision) throws IOException {
            this.inArg = in;
            this.precisionArg = precision;

            if (boundariesFail) {
                throw new IOException("Test boundaries() failure");
            }

            return BOUNDARY_LIST.boundaryStream();
        }
    }

    private static final class StubWriteHandler implements BoundaryWriteHandler<TestLineSegment, TestBoundaryList> {

        private TestBoundaryList list;

        private OutputStream outArg;

        /** {@inheritDoc} */
        @Override
        public void write(final TestBoundaryList boundarySource, final OutputStream out) throws IOException {
            this.list = boundarySource;
            this.outArg = out;
        }
    }
}
