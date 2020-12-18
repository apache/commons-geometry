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
package org.apache.commons.geometry.examples.io.threed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ModelIOHandlerRegistryTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final BoundarySource3D SRC_A = BoundarySource3D.from();

    private static final BoundarySource3D SRC_B = BoundarySource3D.from();

    private final ModelIOHandlerRegistry registry = new ModelIOHandlerRegistry();

    @Test
    public void testGetSetHandlers() {
        // arrange
        final StubHandler handlerA = new StubHandler("a", SRC_A);
        final StubHandler handlerB = new StubHandler("b", SRC_B);

        final List<ModelIOHandler> handlers = Arrays.asList(handlerA, handlerB);

        // act
        registry.setHandlers(handlers);

        // assert
        final List<ModelIOHandler> resultHandlers = registry.getHandlers();
        Assertions.assertNotSame(handlers, resultHandlers);
        Assertions.assertEquals(2, resultHandlers.size());

        Assertions.assertSame(handlerA, resultHandlers.get(0));
        Assertions.assertSame(handlerB, resultHandlers.get(1));
    }

    @Test
    public void testSetHandlers_null() {
        // arrange
        final StubHandler handlerA = new StubHandler("a", SRC_A);
        final StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act
        registry.setHandlers(null);

        // assert
        Assertions.assertEquals(0, registry.getHandlers().size());
    }

    @Test
    public void testGetHandlerForType() {
        // arrange
        final StubHandler handlerA = new StubHandler("a", SRC_A);
        final StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act/assert
        Assertions.assertSame(handlerA, registry.getHandlerForType("a"));
        Assertions.assertSame(handlerB, registry.getHandlerForType("b"));

        Assertions.assertNull(registry.getHandlerForType(null));
        Assertions.assertNull(registry.getHandlerForType(""));
        Assertions.assertNull(registry.getHandlerForType(" "));
        Assertions.assertNull(registry.getHandlerForType("nope"));
    }

    @Test
    public void testHandlesType() {
        // arrange
        final StubHandler handlerA = new StubHandler("a", SRC_A);
        final StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act/assert
        Assertions.assertTrue(registry.handlesType("a"));
        Assertions.assertTrue(registry.handlesType("b"));

        Assertions.assertFalse(registry.handlesType(null));
        Assertions.assertFalse(registry.handlesType(""));
        Assertions.assertFalse(registry.handlesType(" "));
        Assertions.assertFalse(registry.handlesType("nope"));
    }

    @Test
    public void testRead_typeFromFileExtension() {
        // arrange
        final StubHandler handlerA = new StubHandler("a", SRC_A);
        final StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        final File file = new File("file.B");

        // act
        final BoundarySource3D src = registry.read(file, TEST_PRECISION);

        // assert
        Assertions.assertSame(SRC_B, src);
    }

    @Test
    public void testRead_typeFromFileExtension_unknownType() {
        // arrange
        final File file = new File("file.B");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            registry.read(file, TEST_PRECISION);
        }, IllegalArgumentException.class, "No handler found for type \"b\"");
    }

    @Test
    public void testRead_typeFromFileExtension_noFileExtension() {
        // arrange
        final File file = new File("file");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            registry.read(file, TEST_PRECISION);
        }, IllegalArgumentException.class,
                "Cannot determine target file type: \"file\" does not have a file extension");
    }

    @Test
    public void testRead_typeAndFile() {
        // arrange
        final StubHandler handlerA = new StubHandler("a", SRC_A);
        final StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act
        final BoundarySource3D src = registry.read("a", new File("file"), TEST_PRECISION);

        // assert
        Assertions.assertSame(SRC_A, src);
    }

    @Test
    public void testRead_typeAndFile_unknownType() {
        // arrange
        final File file = new File("file");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            registry.read("nope", file, TEST_PRECISION);
        }, IllegalArgumentException.class, "No handler found for type \"nope\"");
    }

    @Test
    public void testRead_typeAndInputStream() {
        // arrange
        final StubHandler handlerA = new StubHandler("a", SRC_A);
        final StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act
        final BoundarySource3D src = registry.read("a", new ByteArrayInputStream(new byte[0]), TEST_PRECISION);

        // assert
        Assertions.assertSame(SRC_A, src);
    }

    @Test
    public void testRead_typeAndInputStream_unknownType() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            registry.read("nope", new ByteArrayInputStream(new byte[0]), TEST_PRECISION);
        }, IllegalArgumentException.class, "No handler found for type \"nope\"");
    }

    @Test
    public void testWrite_typeFromFileExtension() {
        // arrange
        final StubHandler handlerA = new StubHandler("a", SRC_A);
        final StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        final File file = new File("file.B");

        // act
        registry.write(SRC_B, file);

        // assert
        Assertions.assertNull(handlerA.outputBoundarySrc);
        Assertions.assertSame(SRC_B, handlerB.outputBoundarySrc);
    }

    @Test
    public void testWrite_typeFromFileExtension_unknownType() {
        // arrange
        final File file = new File("file.B");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            registry.write(SRC_A, file);
        }, IllegalArgumentException.class, "No handler found for type \"b\"");
    }

    @Test
    public void testWrite_typeFromFileExtension_noFileExtension() {
        // arrange
        final File file = new File("file");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            registry.write(SRC_A, file);
        }, IllegalArgumentException.class,
                "Cannot determine target file type: \"file\" does not have a file extension");
    }

    @Test
    public void testWrite_typeAndFile() {
        // arrange
        final StubHandler handlerA = new StubHandler("a", SRC_A);
        final StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        final File file = new File("file.B");

        // act
        registry.write(SRC_B, "a", file);

        // assert
        Assertions.assertSame(SRC_B, handlerA.outputBoundarySrc);
        Assertions.assertNull(handlerB.outputBoundarySrc);
    }

    @Test
    public void testWrite_typeAndFile_unknownType() {
        // arrange
        final File file = new File("file");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            registry.write(SRC_A, "nope", file);
        }, IllegalArgumentException.class, "No handler found for type \"nope\"");
    }

    @Test
    public void testWrite_typeAndOutputStream() {
        // arrange
        final StubHandler handlerA = new StubHandler("a", SRC_A);
        final StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act
        registry.write(SRC_B, "a", new ByteArrayOutputStream());

        // assert
        Assertions.assertSame(SRC_B, handlerA.outputBoundarySrc);
        Assertions.assertNull(handlerB.outputBoundarySrc);
    }

    @Test
    public void testWrite_typeAndOutputStream_unknownType() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            registry.write(SRC_A, "nope", new ByteArrayOutputStream());
        }, IllegalArgumentException.class, "No handler found for type \"nope\"");
    }

    private static final class StubHandler implements ModelIOHandler {

        private final String handlerType;

        private final BoundarySource3D boundarySrc;

        private BoundarySource3D outputBoundarySrc;

        StubHandler(final String type, final BoundarySource3D boundarySrc) {
            this.handlerType = type;
            this.boundarySrc = boundarySrc;
        }

        @Override
        public boolean handlesType(final String type) {
            return this.handlerType.equals(type);
        }

        @Override
        public BoundarySource3D read(final String type, final File in, final DoublePrecisionContext precision) {
            return boundarySrc;
        }

        @Override
        public BoundarySource3D read(final String type, final InputStream in, final DoublePrecisionContext precision) {
            return boundarySrc;
        }

        @Override
        public void write(final BoundarySource3D model, final String type, final File out) {
            outputBoundarySrc = model;
        }

        @Override
        public void write(final BoundarySource3D model, final String type, final OutputStream out) {
            outputBoundarySrc = model;
        }
    }
}
