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
package org.apache.commons.geometry.io.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.partitioning.BoundaryList;
import org.apache.commons.geometry.core.partitioning.test.TestLineSegment;
import org.apache.commons.geometry.core.partitioning.test.TestPoint2D;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.output.GeometryOutput;
import org.apache.commons.geometry.io.core.test.StubGeometryFormat;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BoundaryIOManagerTest {

    private static final TestLineSegment SEGMENT = new TestLineSegment(TestPoint2D.ZERO, TestPoint2D.PLUS_X);

    private static final TestBoundaryList BOUNDARY_LIST = new TestBoundaryList(Collections.singletonList(SEGMENT));

    private static final GeometryFormat FMT_A = new StubGeometryFormat("testA", Arrays.asList("a", "aext"));

    private static final GeometryFormat FMT_A_ALT = new StubGeometryFormat("TESTa", Collections.singletonList("A"));

    private static final GeometryFormat FMT_B = new StubGeometryFormat("testB", Collections.singletonList("b"));

    private static final GeometryFormat FMT_B_ALT = new StubGeometryFormat("TESTb", Collections.singletonList("B"));

    private static final GeometryFormat FMT_C = new StubGeometryFormat("testC", Collections.singletonList("c"));

    private final TestManager manager = new TestManager();

    @Test
    void testRegisterReadHandler() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        final StubReadHandler r2 = new StubReadHandler(FMT_B);
        final StubReadHandler r3 = new StubReadHandler(FMT_A_ALT);

        // act
        manager.registerReadHandler(r1); // will be replaced by r3
        manager.registerReadHandler(r2);
        manager.registerReadHandler(r2); // register 2x
        manager.registerReadHandler(r3);

        // assert
        Assertions.assertSame(r3, manager.getReadHandlerForFormat(FMT_A));
        Assertions.assertSame(r2, manager.getReadHandlerForFormat(FMT_B));

        Assertions.assertSame(r3, manager.getReadHandlerForFileExtension("a"));
        Assertions.assertNull(manager.getReadHandlerForFileExtension("aext"));
        Assertions.assertSame(r2, manager.getReadHandlerForFileExtension("b"));

        Assertions.assertEquals(Arrays.asList(r2, r3), manager.getReadHandlers());
    }

    @Test
    void testRegisterReadHandler_multipleFileExtensions() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);

        // act
        manager.registerReadHandler(r1);

        // assert
        Assertions.assertSame(r1, manager.getReadHandlerForFormat(FMT_A_ALT));

        Assertions.assertSame(r1, manager.getReadHandlerForFileExtension("A"));
        Assertions.assertSame(r1, manager.getReadHandlerForFileExtension("AEXT"));
    }

    @Test
    void testRegisterReadHandler_nullAndMissingFileExt() {
        // arrange
        final StubGeometryFormat noExts = new StubGeometryFormat("a", null);
        final StubGeometryFormat nullExts = new StubGeometryFormat("b", Arrays.asList("bext", null, null));

        final StubReadHandler r1 = new StubReadHandler(noExts);
        final StubReadHandler r2 = new StubReadHandler(nullExts);

        // act
        manager.registerReadHandler(r1);
        manager.registerReadHandler(r2);

        // assert
        Assertions.assertSame(r1, manager.getReadHandlerForFormat(noExts));
        Assertions.assertNull(manager.getReadHandlerForFileExtension("a"));

        Assertions.assertSame(r2, manager.getReadHandlerForFormat(nullExts));
        Assertions.assertSame(r2, manager.getReadHandlerForFileExtension("bext"));
    }

    @Test
    void testRegisterReadHandler_illegalArgs() {
        // arrange
        final StubReadHandler nullFmt = new StubReadHandler(null);
        final StubReadHandler nullFmtName = new StubReadHandler(new StubGeometryFormat(null));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.registerReadHandler(null),
                NullPointerException.class, "Handler cannot be null");
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.registerReadHandler(nullFmt),
                NullPointerException.class, "Format cannot be null");
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.registerReadHandler(nullFmtName),
                NullPointerException.class, "Format name cannot be null");
    }

    @Test
    void testUnregisterReadHandler() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        final StubReadHandler r2 = new StubReadHandler(FMT_B);

        manager.registerReadHandler(r1);
        manager.registerReadHandler(r2);

        // act
        manager.unregisterReadHandler(r1);

        // assert
        Assertions.assertNull(manager.getReadHandlerForFormat(FMT_A));
        Assertions.assertSame(r2, manager.getReadHandlerForFormat(FMT_B));

        Assertions.assertEquals(Arrays.asList(r2), manager.getReadHandlers());
    }

    @Test
    void testUnregisterReadHandler_argsNotRegistered() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        final StubReadHandler r2 = new StubReadHandler(FMT_B);

        manager.registerReadHandler(r1);

        // act
        manager.unregisterReadHandler(null);
        manager.unregisterReadHandler(r2);

        // assert
        Assertions.assertEquals(Arrays.asList(r1), manager.getReadHandlers());
    }

    @Test
    void testGetReadHandlerForFormat() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        final StubReadHandler r2 = new StubReadHandler(FMT_B);

        manager.registerReadHandler(r1);
        manager.registerReadHandler(r2);

        // act/assert
        Assertions.assertSame(r1, manager.getReadHandlerForFormat(FMT_A));
        Assertions.assertSame(r1, manager.getReadHandlerForFormat(FMT_A_ALT));
        Assertions.assertSame(r2, manager.getReadHandlerForFormat(FMT_B));
        Assertions.assertSame(r2, manager.getReadHandlerForFormat(FMT_B_ALT));

        Assertions.assertNull(manager.getReadHandlerForFormat(null));
        Assertions.assertNull(manager.getReadHandlerForFormat(FMT_C));
    }

    @Test
    void testGetReadHandlerForFileExtension() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        final StubReadHandler r2 = new StubReadHandler(FMT_B);

        manager.registerReadHandler(r1);
        manager.registerReadHandler(r2);

        // act/assert
        Assertions.assertSame(r1, manager.getReadHandlerForFileExtension("a"));
        Assertions.assertSame(r1, manager.getReadHandlerForFileExtension("A"));
        Assertions.assertSame(r1, manager.getReadHandlerForFileExtension("aext"));
        Assertions.assertSame(r1, manager.getReadHandlerForFileExtension("AeXt"));

        Assertions.assertSame(r2, manager.getReadHandlerForFileExtension("b"));
        Assertions.assertSame(r2, manager.getReadHandlerForFileExtension("B"));

        Assertions.assertNull(manager.getReadHandlerForFileExtension(null));
        Assertions.assertNull(manager.getReadHandlerForFileExtension(""));
        Assertions.assertNull(manager.getReadHandlerForFileExtension("c"));
    }

    @Test
    void testRequireReadHandler() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        final GeometryInput aInput = new StubGeometryInput("/some/path/to/a/file.AEXT");
        final GeometryInput bInput = new StubGeometryInput("/some/path/to/a/file.b");
        final GeometryInput noFileExt = new StubGeometryInput("/some/path/to/a/file");
        final GeometryInput nullFileName = new StubGeometryInput(null);

        manager.registerReadHandler(r1);

        // act/assert
        Assertions.assertSame(r1, manager.requireReadHandler(bInput, FMT_A));
        Assertions.assertSame(r1, manager.requireReadHandler(noFileExt, FMT_A));
        Assertions.assertSame(r1, manager.requireReadHandler(aInput, null));

        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.requireReadHandler(aInput, FMT_B),
                IllegalArgumentException.class, "Failed to find handler for format \"testB\"");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.requireReadHandler(bInput, null),
                IllegalArgumentException.class, "Failed to find handler for file extension \"b\"");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.requireReadHandler(noFileExt, null),
                IllegalArgumentException.class,
                "Failed to find handler: no format specified and no file extension available");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.requireReadHandler(nullFileName, null),
                IllegalArgumentException.class,
                "Failed to find handler: no format specified and no file extension available");
    }

    @Test
    void testGetReadFormats() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        final StubReadHandler r2 = new StubReadHandler(FMT_B);
        final StubReadHandler r3 = new StubReadHandler(FMT_B);

        manager.registerReadHandler(r1);
        manager.registerReadHandler(r2);
        manager.registerReadHandler(r3);

        // act
        final List<GeometryFormat> formats = manager.getReadFormats();

        // assert
        Assertions.assertEquals(2, formats.size());
        Assertions.assertEquals(Arrays.asList(FMT_A, FMT_B), formats);
    }

    @Test
    void testGetReadFormats_empty() {
        // act/assert
        Assertions.assertEquals(0, manager.getReadFormats().size());
    }

    @Test
    void testRegisterWriteHandler() {
        // arrange
        final StubWriteHandler w1 = new StubWriteHandler(FMT_A);
        final StubWriteHandler w2 = new StubWriteHandler(FMT_B);
        final StubWriteHandler w3 = new StubWriteHandler(FMT_A_ALT);

        // act
        manager.registerWriteHandler(w1); // will be replaced by w3
        manager.registerWriteHandler(w2);
        manager.registerWriteHandler(w2); // register 2x
        manager.registerWriteHandler(w3);

        // assert
        Assertions.assertSame(w3, manager.getWriteHandlerForFormat(FMT_A));
        Assertions.assertSame(w2, manager.getWriteHandlerForFormat(FMT_B));

        Assertions.assertSame(w3, manager.getWriteHandlerForFileExtension("a"));
        Assertions.assertNull(manager.getWriteHandlerForFileExtension("aext"));
        Assertions.assertSame(w2, manager.getWriteHandlerForFileExtension("b"));

        Assertions.assertEquals(Arrays.asList(w2, w3), manager.getWriteHandlers());
    }

    @Test
    void testRegisterWriteHandler_multipleFileExtensions() {
        // arrange
        final StubWriteHandler w1 = new StubWriteHandler(FMT_A);

        // act
        manager.registerWriteHandler(w1);

        // assert
        Assertions.assertSame(w1, manager.getWriteHandlerForFormat(FMT_A_ALT));

        Assertions.assertSame(w1, manager.getWriteHandlerForFileExtension("A"));
        Assertions.assertSame(w1, manager.getWriteHandlerForFileExtension("AEXT"));
    }

    @Test
    void testRegisterWriteHandler_nullAndMissingFileExt() {
        // arrange
        final StubGeometryFormat noExts = new StubGeometryFormat("a", null);
        final StubGeometryFormat nullExts = new StubGeometryFormat("b", Arrays.asList("bext", null, null));

        final StubWriteHandler w1 = new StubWriteHandler(noExts);
        final StubWriteHandler w2 = new StubWriteHandler(nullExts);

        // act
        manager.registerWriteHandler(w1);
        manager.registerWriteHandler(w2);

        // assert
        Assertions.assertSame(w1, manager.getWriteHandlerForFormat(noExts));
        Assertions.assertNull(manager.getWriteHandlerForFileExtension("a"));

        Assertions.assertEquals(w2, manager.getWriteHandlerForFormat(nullExts));
        Assertions.assertEquals(w2, manager.getWriteHandlerForFileExtension("bext"));
    }

    @Test
    void testUnregisterWriteHandler() {
        // arrange
        final StubWriteHandler w1 = new StubWriteHandler(FMT_A);
        final StubWriteHandler w2 = new StubWriteHandler(FMT_B);

        manager.registerWriteHandler(w1);
        manager.registerWriteHandler(w2);

        // act
        manager.unregisterWriteHandler(w1);

        // assert
        Assertions.assertNull(manager.getWriteHandlerForFormat(FMT_A));
        Assertions.assertSame(w2, manager.getWriteHandlerForFormat(FMT_B));

        Assertions.assertEquals(Arrays.asList(w2), manager.getWriteHandlers());
    }

    @Test
    void testUnregisterWriteHandler_argsNotRegistered() {
        // arrange
        final StubWriteHandler w1 = new StubWriteHandler(FMT_A);
        final StubWriteHandler w2 = new StubWriteHandler(FMT_B);
        final StubWriteHandler w3 = new StubWriteHandler(FMT_C);

        manager.registerWriteHandler(w1);
        manager.registerWriteHandler(w2);

        // act
        manager.unregisterWriteHandler(null);
        manager.unregisterWriteHandler(w3);

        // assert
        Assertions.assertEquals(Arrays.asList(w1, w2), manager.getWriteHandlers());
    }

    @Test
    void testGetWriteFormats() {
        // arrange
        final StubWriteHandler w1 = new StubWriteHandler(FMT_A);
        final StubWriteHandler w2 = new StubWriteHandler(FMT_B);
        final StubWriteHandler w3 = new StubWriteHandler(FMT_B);

        manager.registerWriteHandler(w1);
        manager.registerWriteHandler(w2);
        manager.registerWriteHandler(w3);

        // act
        final List<GeometryFormat> formats = manager.getWriteFormats();

        // assert
        Assertions.assertEquals(2, formats.size());
        Assertions.assertEquals(Arrays.asList(FMT_A, FMT_B), formats);
    }

    @Test
    void testGetWriteFormats_empty() {
        // act/assert
        Assertions.assertEquals(0, manager.getWriteFormats().size());
    }

    @Test
    void testRegisterWriteHandler_illegalArgs() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.registerWriteHandler(null),
                NullPointerException.class, "Handler cannot be null");
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.registerWriteHandler(new StubWriteHandler(null)),
                NullPointerException.class, "Format cannot be null");
        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.registerWriteHandler(new StubWriteHandler(new StubGeometryFormat(null))),
                NullPointerException.class, "Format name cannot be null");
    }

    @Test
    void testGetWriteHandlerForFormat() {
        // arrange
        final StubWriteHandler w1 = new StubWriteHandler(FMT_A);
        final StubWriteHandler w2 = new StubWriteHandler(FMT_B);

        manager.registerWriteHandler(w1);
        manager.registerWriteHandler(w2);

        // act/assert
        Assertions.assertSame(w1, manager.getWriteHandlerForFormat(FMT_A));
        Assertions.assertSame(w1, manager.getWriteHandlerForFormat(FMT_A_ALT));
        Assertions.assertSame(w2, manager.getWriteHandlerForFormat(FMT_B));
        Assertions.assertSame(w2, manager.getWriteHandlerForFormat(FMT_B_ALT));

        Assertions.assertNull(manager.getWriteHandlerForFormat(null));
        Assertions.assertNull(manager.getWriteHandlerForFormat(FMT_C));
    }

    @Test
    void testGetWriteHandlerForFileExtension() {
        // arrange
        final StubWriteHandler w1 = new StubWriteHandler(FMT_A);
        final StubWriteHandler w2 = new StubWriteHandler(FMT_B);

        manager.registerWriteHandler(w1);
        manager.registerWriteHandler(w2);

        // act/assert
        Assertions.assertSame(w1, manager.getWriteHandlerForFileExtension("a"));
        Assertions.assertSame(w1, manager.getWriteHandlerForFileExtension("A"));
        Assertions.assertSame(w1, manager.getWriteHandlerForFileExtension("aext"));
        Assertions.assertSame(w1, manager.getWriteHandlerForFileExtension("AeXt"));

        Assertions.assertSame(w2, manager.getWriteHandlerForFileExtension("b"));
        Assertions.assertSame(w2, manager.getWriteHandlerForFileExtension("B"));

        Assertions.assertNull(manager.getWriteHandlerForFileExtension(null));
        Assertions.assertNull(manager.getWriteHandlerForFileExtension(""));
        Assertions.assertNull(manager.getWriteHandlerForFileExtension("c"));
    }

    @Test
    void testRequireWriteHandler() {
        // arrange
        final StubWriteHandler w1 = new StubWriteHandler(FMT_A);
        final GeometryOutput aInput = new StubGeometryOutput("/some/path/to/a/file.AEXT");
        final GeometryOutput bInput = new StubGeometryOutput("/some/path/to/a/file.b");
        final GeometryOutput noFileExt = new StubGeometryOutput("/some/path/to/a/file");
        final GeometryOutput nullFileName = new StubGeometryOutput(null);

        manager.registerWriteHandler(w1);

        // act/assert
        Assertions.assertSame(w1, manager.requireWriteHandler(bInput, FMT_A));
        Assertions.assertSame(w1, manager.requireWriteHandler(noFileExt, FMT_A));
        Assertions.assertSame(w1, manager.requireWriteHandler(aInput, null));

        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.requireWriteHandler(aInput, FMT_B),
                IllegalArgumentException.class, "Failed to find handler for format \"testB\"");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.requireWriteHandler(bInput, null),
                IllegalArgumentException.class, "Failed to find handler for file extension \"b\"");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.requireWriteHandler(noFileExt, null),
                IllegalArgumentException.class,
                "Failed to find handler: no format specified and no file extension available");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> manager.requireWriteHandler(nullFileName, null),
                IllegalArgumentException.class,
                "Failed to find handler: no format specified and no file extension available");
    }

    @Test
    void testRead_formatGiven() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        manager.registerReadHandler(r1);

        final StubGeometryInput in = new StubGeometryInput(null);
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-4);

        // act
        final TestBoundaryList result = manager.read(in, FMT_A_ALT, precision);

        // assert
        Assertions.assertSame(BOUNDARY_LIST, result);
        Assertions.assertSame(in, r1.inArg);
        Assertions.assertSame(precision, r1.precisionArg);
    }

    @Test
    void testRead_noFormatGiven() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        manager.registerReadHandler(r1);

        final StubGeometryInput in = new StubGeometryInput("file.aeXT");
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-4);

        // act
        final TestBoundaryList result = manager.read(in, null, precision);

        // assert
        Assertions.assertSame(BOUNDARY_LIST, result);
        Assertions.assertSame(in, r1.inArg);
        Assertions.assertSame(precision, r1.precisionArg);
    }

    @Test
    void testRead_handlerNotFound() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        manager.registerReadHandler(r1);

        final StubGeometryInput inputA = new StubGeometryInput("file.a");
        final StubGeometryInput inputB = new StubGeometryInput("file.b");
        final StubGeometryInput inputNull = new StubGeometryInput(null);

        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-4);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.read(inputA, FMT_B, precision));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.read(inputB, null, precision));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.read(inputNull, null, precision));
    }

    @Test
    void testBoundaries_formatGiven() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        manager.registerReadHandler(r1);

        final StubGeometryInput in = new StubGeometryInput(null);
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-4);

        // act
        final Stream<TestLineSegment> result = manager.boundaries(in, FMT_A_ALT, precision);

        // assert
        Assertions.assertEquals(BOUNDARY_LIST.getBoundaries(), result.collect(Collectors.toList()));
        Assertions.assertSame(in, r1.inArg);
        Assertions.assertSame(precision, r1.precisionArg);
    }

    @Test
    void testBoundaries_noFormatGiven() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        manager.registerReadHandler(r1);

        final StubGeometryInput in = new StubGeometryInput("file.aeXT");
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-4);

        // act
        final Stream<TestLineSegment> result = manager.boundaries(in, null, precision);

        // assert
        Assertions.assertEquals(BOUNDARY_LIST.getBoundaries(), result.collect(Collectors.toList()));
        Assertions.assertSame(in, r1.inArg);
        Assertions.assertSame(precision, r1.precisionArg);
    }

    @Test
    void testBoundaries_handlerNotFound() {
        // arrange
        final StubReadHandler r1 = new StubReadHandler(FMT_A);
        manager.registerReadHandler(r1);

        final StubGeometryInput inputA = new StubGeometryInput("file.a");
        final StubGeometryInput inputB = new StubGeometryInput("file.b");
        final StubGeometryInput inputNull = new StubGeometryInput(null);

        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-4);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.boundaries(inputA, FMT_B, precision));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.boundaries(inputB, null, precision));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.boundaries(inputNull, null, precision));
    }

    @Test
    void testWrite_formatGiven() {
        // arrange
        final StubWriteHandler w1 = new StubWriteHandler(FMT_A);
        manager.registerWriteHandler(w1);

        final TestBoundaryList src = BOUNDARY_LIST;
        final StubGeometryOutput out = new StubGeometryOutput(null);

        // act
        manager.write(BOUNDARY_LIST, out, FMT_A_ALT);

        // assert
        Assertions.assertSame(src, w1.list);
        Assertions.assertSame(out, w1.outArg);
    }

    @Test
    void testWrite_noFormatGiven() {
        // arrange
        final StubWriteHandler w1 = new StubWriteHandler(FMT_A);
        manager.registerWriteHandler(w1);

        final TestBoundaryList src = BOUNDARY_LIST;
        final StubGeometryOutput out = new StubGeometryOutput("file.aeXT");

        // act
        manager.write(src, out, null);

        // assert
        Assertions.assertSame(src, w1.list);
        Assertions.assertSame(out, w1.outArg);
    }

    @Test
    void testWrite_handlerNotFound() {
        // arrange
        final StubWriteHandler w1 = new StubWriteHandler(FMT_A);
        manager.registerWriteHandler(w1);

        final StubGeometryOutput outputA = new StubGeometryOutput("file.a");
        final StubGeometryOutput outputB = new StubGeometryOutput("file.b");
        final StubGeometryOutput nullOutput = new StubGeometryOutput(null);

        final TestBoundaryList src = BOUNDARY_LIST;

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.write(src, outputA, FMT_B));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.write(src, outputB, null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.write(src, nullOutput, null));
    }

    private static final class TestManager
        extends BoundaryIOManager<TestLineSegment, TestBoundaryList, StubReadHandler, StubWriteHandler> {
    }

    private static final class TestBoundaryList extends BoundaryList<TestPoint2D, TestLineSegment> {

        TestBoundaryList(final List<? extends TestLineSegment> boundaries) {
            super(boundaries);
        }
    }

    private static final class StubGeometryInput implements GeometryInput {

        private final String fileName;

        StubGeometryInput(final String fileName) {
            this.fileName = fileName;
        }

        /** {@inheritDoc} */
        @Override
        public String getFileName() {
            return fileName;
        }

        /** {@inheritDoc} */
        @Override
        public Charset getCharset() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public InputStream getInputStream() {
            throw new UnsupportedOperationException();
        }

    }

    private static final class StubGeometryOutput implements GeometryOutput {

        private final String fileName;

        StubGeometryOutput(final String fileName) {
            this.fileName = fileName;
        }

        /** {@inheritDoc} */
        @Override
        public String getFileName() {
            return fileName;
        }

        /** {@inheritDoc} */
        @Override
        public Charset getCharset() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubReadHandler implements BoundaryReadHandler<TestLineSegment, TestBoundaryList> {

        private final GeometryFormat fmt;

        private GeometryInput inArg;

        private Precision.DoubleEquivalence precisionArg;

        StubReadHandler(final GeometryFormat fmt) {
            this.fmt = fmt;
        }

        /** {@inheritDoc} */
        @Override
        public GeometryFormat getFormat() {
            return fmt;
        }

        /** {@inheritDoc} */
        @Override
        public TestBoundaryList read(final GeometryInput in, final Precision.DoubleEquivalence precision) {
            this.inArg = in;
            this.precisionArg = precision;

            return BOUNDARY_LIST;
        }

        /** {@inheritDoc} */
        @Override
        public Stream<TestLineSegment> boundaries(final GeometryInput in,
                final Precision.DoubleEquivalence precision) {
            this.inArg = in;
            this.precisionArg = precision;

            return BOUNDARY_LIST.boundaryStream();
        }
    }

    private static final class StubWriteHandler implements BoundaryWriteHandler<TestLineSegment, TestBoundaryList> {

        private GeometryFormat fmt;

        private TestBoundaryList list;

        private GeometryOutput outArg;

        StubWriteHandler(final GeometryFormat fmt) {
            this.fmt = fmt;
        }

        /** {@inheritDoc} */
        @Override
        public GeometryFormat getFormat() {
            return fmt;
        }

        /** {@inheritDoc} */
        @Override
        public void write(final TestBoundaryList boundarySource, final GeometryOutput out) {
            this.list = boundarySource;
            this.outArg = out;
        }
    }
}
