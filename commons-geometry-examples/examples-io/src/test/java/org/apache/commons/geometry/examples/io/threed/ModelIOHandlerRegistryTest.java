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
import org.junit.Assert;
import org.junit.Test;

public class ModelIOHandlerRegistryTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final BoundarySource3D SRC_A = BoundarySource3D.from();

    private static final BoundarySource3D SRC_B = BoundarySource3D.from();

    private ModelIOHandlerRegistry registry = new ModelIOHandlerRegistry();

    @Test
    public void testGetSetHandlers() {
        // arrange
        StubHandler handlerA = new StubHandler("a", SRC_A);
        StubHandler handlerB = new StubHandler("b", SRC_B);

        List<ModelIOHandler> handlers = Arrays.asList(handlerA, handlerB);

        // act
        registry.setHandlers(handlers);

        // assert
        List<ModelIOHandler> resultHandlers = registry.getHandlers();
        Assert.assertNotSame(handlers, resultHandlers);
        Assert.assertEquals(2, resultHandlers.size());

        Assert.assertSame(handlerA, resultHandlers.get(0));
        Assert.assertSame(handlerB, resultHandlers.get(1));
    }

    @Test
    public void testSetHandlers_null() {
        // arrange
        StubHandler handlerA = new StubHandler("a", SRC_A);
        StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act
        registry.setHandlers(null);

        // assert
        Assert.assertEquals(0, registry.getHandlers().size());
    }

    @Test
    public void testGetHandlerForType() {
        // arrange
        StubHandler handlerA = new StubHandler("a", SRC_A);
        StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act/assert
        Assert.assertSame(handlerA, registry.getHandlerForType("a"));
        Assert.assertSame(handlerB, registry.getHandlerForType("b"));

        Assert.assertNull(registry.getHandlerForType(null));
        Assert.assertNull(registry.getHandlerForType(""));
        Assert.assertNull(registry.getHandlerForType(" "));
        Assert.assertNull(registry.getHandlerForType("nope"));
    }

    @Test
    public void testHandlesType() {
        // arrange
        StubHandler handlerA = new StubHandler("a", SRC_A);
        StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act/assert
        Assert.assertTrue(registry.handlesType("a"));
        Assert.assertTrue(registry.handlesType("b"));

        Assert.assertFalse(registry.handlesType(null));
        Assert.assertFalse(registry.handlesType(""));
        Assert.assertFalse(registry.handlesType(" "));
        Assert.assertFalse(registry.handlesType("nope"));
    }

    @Test
    public void testRead_typeFromFileExtension() {
        // arrange
        StubHandler handlerA = new StubHandler("a", SRC_A);
        StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        File file = new File("file.B");

        // act
        BoundarySource3D src = registry.read(file, TEST_PRECISION);

        // assert
        Assert.assertSame(SRC_B, src);
    }

    @Test
    public void testRead_typeFromFileExtension_unknownType() {
        // arrange
        File file = new File("file.B");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            registry.read(file, TEST_PRECISION);
        }, IllegalArgumentException.class, "No handler found for type \"b\"");
    }

    @Test
    public void testRead_typeFromFileExtension_noFileExtension() {
        // arrange
        File file = new File("file");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            registry.read(file, TEST_PRECISION);
        }, IllegalArgumentException.class,
                "Cannot determine target file type: \"file\" does not have a file extension");
    }

    @Test
    public void testRead_typeAndFile() {
        // arrange
        StubHandler handlerA = new StubHandler("a", SRC_A);
        StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act
        BoundarySource3D src = registry.read("a", new File("file"), TEST_PRECISION);

        // assert
        Assert.assertSame(SRC_A, src);
    }

    @Test
    public void testRead_typeAndFile_unknownType() {
        // arrange
        File file = new File("file");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            registry.read("nope", file, TEST_PRECISION);
        }, IllegalArgumentException.class, "No handler found for type \"nope\"");
    }

    @Test
    public void testRead_typeAndInputStream() {
        // arrange
        StubHandler handlerA = new StubHandler("a", SRC_A);
        StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act
        BoundarySource3D src = registry.read("a", new ByteArrayInputStream(new byte[0]), TEST_PRECISION);

        // assert
        Assert.assertSame(SRC_A, src);
    }

    @Test
    public void testRead_typeAndInputStream_unknownType() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            registry.read("nope", new ByteArrayInputStream(new byte[0]), TEST_PRECISION);
        }, IllegalArgumentException.class, "No handler found for type \"nope\"");
    }

    @Test
    public void testWrite_typeFromFileExtension() {
        // arrange
        StubHandler handlerA = new StubHandler("a", SRC_A);
        StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        File file = new File("file.B");

        // act
        registry.write(SRC_B, file);

        // assert
        Assert.assertNull(handlerA.outputBoundarySrc);
        Assert.assertSame(SRC_B, handlerB.outputBoundarySrc);
    }

    @Test
    public void testWrite_typeFromFileExtension_unknownType() {
        // arrange
        File file = new File("file.B");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            registry.write(SRC_A, file);
        }, IllegalArgumentException.class, "No handler found for type \"b\"");
    }

    @Test
    public void testWrite_typeFromFileExtension_noFileExtension() {
        // arrange
        File file = new File("file");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            registry.write(SRC_A, file);
        }, IllegalArgumentException.class,
                "Cannot determine target file type: \"file\" does not have a file extension");
    }

    @Test
    public void testWrite_typeAndFile() {
        // arrange
        StubHandler handlerA = new StubHandler("a", SRC_A);
        StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        File file = new File("file.B");

        // act
        registry.write(SRC_B, "a", file);

        // assert
        Assert.assertSame(SRC_B, handlerA.outputBoundarySrc);
        Assert.assertNull(handlerB.outputBoundarySrc);
    }

    @Test
    public void testWrite_typeAndFile_unknownType() {
        // arrange
        File file = new File("file");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            registry.write(SRC_A, "nope", file);
        }, IllegalArgumentException.class, "No handler found for type \"nope\"");
    }

    @Test
    public void testWrite_typeAndOutputStream() {
        // arrange
        StubHandler handlerA = new StubHandler("a", SRC_A);
        StubHandler handlerB = new StubHandler("b", SRC_B);

        registry.setHandlers(Arrays.asList(handlerA, handlerB));

        // act
        registry.write(SRC_B, "a", new ByteArrayOutputStream());

        // assert
        Assert.assertSame(SRC_B, handlerA.outputBoundarySrc);
        Assert.assertNull(handlerB.outputBoundarySrc);
    }

    @Test
    public void testWrite_typeAndOutputStream_unknownType() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
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
        public boolean handlesType(String type) {
            return this.handlerType.equals(type);
        }

        @Override
        public BoundarySource3D read(String type, File in, DoublePrecisionContext precision) {
            return boundarySrc;
        }

        @Override
        public BoundarySource3D read(String type, InputStream in, DoublePrecisionContext precision) {
            return boundarySrc;
        }

        @Override
        public void write(BoundarySource3D model, String type, File out) {
            outputBoundarySrc = model;
        }

        @Override
        public void write(BoundarySource3D model, String type, OutputStream out) {
            outputBoundarySrc = model;
        }
    }
}
