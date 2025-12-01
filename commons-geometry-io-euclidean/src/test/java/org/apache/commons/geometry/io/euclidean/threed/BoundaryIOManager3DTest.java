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
package org.apache.commons.geometry.io.euclidean.threed;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.input.FileGeometryInput;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.output.FileGeometryOutput;
import org.apache.commons.geometry.io.core.output.GeometryOutput;
import org.apache.commons.geometry.io.core.test.StubGeometryFormat;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BoundaryIOManager3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION = Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final GeometryFormat TEST_FMT = new StubGeometryFormat("test");

    private static final FacetDefinitionReader FACET_DEF_READER = new FacetDefinitionReader() {

        @Override
        public FacetDefinition readFacet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            // do nothing
        }
    };

    private static final FacetDefinition FACET = new SimpleFacetDefinition(Arrays.asList(
            Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)));

    private static final Triangle3D TRI = Planes.triangleFromVertices(
            Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    private static final TriangleMesh TRI_MESH = SimpleTriangleMesh.builder(TEST_PRECISION).build();

    private final BoundaryIOManager3D manager = new BoundaryIOManager3D();

    @Test
    void testRegisterDefaultHandlers() {
        // act
        manager.registerDefaultHandlers();

        // assert
        // ensure that we have default read/write handlers for every defined format
        final GeometryFormat3D[] fmts = GeometryFormat3D.values();

        Assertions.assertEquals(fmts.length, manager.getReadHandlers().size());
        Assertions.assertEquals(fmts.length, manager.getWriteHandlers().size());

        for (final GeometryFormat3D fmt : fmts) {
            Assertions.assertNotNull(manager.getReadHandlerForFormat(fmt));
            Assertions.assertNotNull(manager.getWriteHandlerForFormat(fmt));
        }
    }

    @Test
    void testFacetDefinitionReader_formatGiven() {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler(readHandler);

        final GeometryInput in = new FileGeometryInput(Paths.get("myfile"));

        // act
        final FacetDefinitionReader result = manager.facetDefinitionReader(in, TEST_FMT);

        // assert
        Assertions.assertSame(FACET_DEF_READER, result);
        Assertions.assertSame(in, readHandler.inArg);
    }

    @Test
    void testFacetDefinitionReader_nullFormat() {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler(readHandler);

        final GeometryInput in = new FileGeometryInput(Paths.get("myfile.test"));

        // act
        final FacetDefinitionReader result = manager.facetDefinitionReader(in, null);

        // assert
        Assertions.assertSame(FACET_DEF_READER, result);
        Assertions.assertSame(in, readHandler.inArg);
    }

    @Test
    void testFacetDefinitionReader_unknownHandler() {
        // act/assert
        checkUnknownReadHandler(manager::facetDefinitionReader);
    }

    @Test
    void testFacets_formatGiven() {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler(readHandler);

        final GeometryInput in = new FileGeometryInput(Paths.get("myfile"));

        // act
        final Stream<FacetDefinition> result = manager.facets(in, TEST_FMT);

        // assert
        Assertions.assertEquals(Collections.singletonList(FACET), result.collect(Collectors.toList()));
        Assertions.assertSame(in, readHandler.inArg);
    }

    @Test
    void testFacets_nullFormat() {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler(readHandler);

        final GeometryInput in = new FileGeometryInput(Paths.get("myfile.test"));

        // act
        final Stream<FacetDefinition> result = manager.facets(in, null);

        // assert
        Assertions.assertEquals(Collections.singletonList(FACET), result.collect(Collectors.toList()));
        Assertions.assertSame(in, readHandler.inArg);
    }

    @Test
    void testFacets_unknownHandler() {
        // act/assert
        checkUnknownReadHandler(manager::facets);
    }

    @Test
    void testTriangles_formatGiven() {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler(readHandler);

        final GeometryInput in = new FileGeometryInput(Paths.get("myfile"));

        // act
        final Stream<Triangle3D> result = manager.triangles(in, TEST_FMT, TEST_PRECISION);

        // assert
        Assertions.assertEquals(Collections.singletonList(TRI), result.collect(Collectors.toList()));
        Assertions.assertSame(in, readHandler.inArg);
        Assertions.assertSame(TEST_PRECISION, readHandler.precisionArg);
    }

    @Test
    void testTriangles_nullFormat() {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler(readHandler);

        final GeometryInput in = new FileGeometryInput(Paths.get("myfile.test"));

        // act
        final Stream<Triangle3D> result = manager.triangles(in, null, TEST_PRECISION);

        // assert
        Assertions.assertEquals(Collections.singletonList(TRI), result.collect(Collectors.toList()));
        Assertions.assertSame(in, readHandler.inArg);
        Assertions.assertSame(TEST_PRECISION, readHandler.precisionArg);
    }

    @Test
    void testTriangles_unknownHandler() {
        // act/assert
        checkUnknownReadHandler((in, fmt) -> manager.triangles(in, fmt, TEST_PRECISION));
    }

    @Test
    void testReadTriangleMesh_formatGiven() {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler(readHandler);

        final GeometryInput in = new FileGeometryInput(Paths.get("myfile"));

        // act
        final TriangleMesh result = manager.readTriangleMesh(in, TEST_FMT, TEST_PRECISION);

        // assert
        Assertions.assertEquals(TRI_MESH, result);
        Assertions.assertSame(in, readHandler.inArg);
        Assertions.assertSame(TEST_PRECISION, readHandler.precisionArg);
    }

    @Test
    void testReadTriangleMesh_nullFormat() {
        // arrange
        final StubReadHandler3D readHandler = new StubReadHandler3D();
        manager.registerReadHandler(readHandler);

        final GeometryInput in = new FileGeometryInput(Paths.get("myfile.test"));

        // act
        final TriangleMesh result = manager.readTriangleMesh(in, null, TEST_PRECISION);

        // assert
        Assertions.assertEquals(TRI_MESH, result);
        Assertions.assertSame(in, readHandler.inArg);
        Assertions.assertSame(TEST_PRECISION, readHandler.precisionArg);
    }

    @Test
    void testReadTriangleMesh_unknownHandler() {
        // act/assert
        checkUnknownReadHandler((in, fmt) -> manager.readTriangleMesh(in, fmt, TEST_PRECISION));
    }

    @Test
    void testWrite_stream_formatGiven() {
        // arrange
        final StubWriteHandler3D writeHandler = new StubWriteHandler3D();
        manager.registerWriteHandler(writeHandler);

        final GeometryOutput out = new FileGeometryOutput(Paths.get("myfile"));

        // act
        manager.write(Stream.of(TRI), out, TEST_FMT);

        // assert
        Assertions.assertEquals(Collections.singletonList(TRI), writeHandler.boundariesArg);
        Assertions.assertSame(out, writeHandler.outArg);
    }

    @Test
    void testWrite_stream_nullFormat() {
        // arrange
        final StubWriteHandler3D writeHandler = new StubWriteHandler3D();
        manager.registerWriteHandler(writeHandler);

        final GeometryOutput out = new FileGeometryOutput(Paths.get("myfile.TEST"));

        // act
        manager.write(Stream.of(TRI), out, null);

        // assert
        Assertions.assertEquals(Collections.singletonList(TRI), writeHandler.boundariesArg);
        Assertions.assertSame(out, writeHandler.outArg);
    }

    @Test
    void testWrite_stream_unknownHandler() {
        // act/assert
        checkUnknownWriteHandler((out, fmt) -> manager.write(Stream.of(TRI), out, fmt));
    }

    @Test
    void testWriteFacets_stream_formatGiven() {
        // arrange
        final StubWriteHandler3D writeHandler = new StubWriteHandler3D();
        manager.registerWriteHandler(writeHandler);

        final GeometryOutput out = new FileGeometryOutput(Paths.get("myfile"));

        // act
        manager.writeFacets(Stream.of(FACET), out, TEST_FMT);

        // assert
        Assertions.assertEquals(Collections.singletonList(FACET), writeHandler.facetsArg);
        Assertions.assertSame(out, writeHandler.outArg);
    }

    @Test
    void testWriteFacets_stream_nullFormat() {
        // arrange
        final StubWriteHandler3D writeHandler = new StubWriteHandler3D();
        manager.registerWriteHandler(writeHandler);

        final GeometryOutput out = new FileGeometryOutput(Paths.get("myfile.TEST"));

        // act
        manager.writeFacets(Stream.of(FACET), out, null);

        // assert
        Assertions.assertEquals(Collections.singletonList(FACET), writeHandler.facetsArg);
        Assertions.assertSame(out, writeHandler.outArg);
    }

    @Test
    void testWriteFacets_stream_unknownHandler() {
        // act/assert
        checkUnknownWriteHandler((out, fmt) -> manager.writeFacets(Stream.of(FACET), out, fmt));
    }

    @Test
    void testWriteFacets_collection_formatGiven() {
        // arrange
        final StubWriteHandler3D writeHandler = new StubWriteHandler3D();
        manager.registerWriteHandler(writeHandler);

        final GeometryOutput out = new FileGeometryOutput(Paths.get("myfile"));

        // act
        manager.writeFacets(Collections.singletonList(FACET), out, TEST_FMT);

        // assert
        Assertions.assertEquals(Collections.singletonList(FACET), writeHandler.facetsArg);
        Assertions.assertSame(out, writeHandler.outArg);
    }

    @Test
    void testWriteFacets_collection_nullFormat() {
        // arrange
        final StubWriteHandler3D writeHandler = new StubWriteHandler3D();
        manager.registerWriteHandler(writeHandler);

        final GeometryOutput out = new FileGeometryOutput(Paths.get("myfile.TEST"));

        // act
        manager.writeFacets(Collections.singletonList(FACET), out, null);

        // assert
        Assertions.assertEquals(Collections.singletonList(FACET), writeHandler.facetsArg);
        Assertions.assertSame(out, writeHandler.outArg);
    }

    @Test
    void testWriteFacets_collection_unknownHandler() {
        // act/assert
        checkUnknownWriteHandler((out, fmt) -> manager.writeFacets(Collections.singletonList(FACET), out, fmt));
    }

    private static void checkUnknownReadHandler(final ThrowingBiConsumer<GeometryInput, GeometryFormat> fn) {
        // arrange
        final GeometryInput withFileExt = new FileGeometryInput(Paths.get("myfile.test"));
        final GeometryInput noFileExt = new FileGeometryInput(Paths.get("myfile"));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> fn.accept(withFileExt, TEST_FMT),
                IllegalArgumentException.class, "Failed to find handler for format \"test\"");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> fn.accept(withFileExt, null),
                IllegalArgumentException.class, "Failed to find handler for file extension \"test\"");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> fn.accept(noFileExt, null),
                IllegalArgumentException.class, "Failed to find handler: no format specified and no file extension available");
    }

    private static void checkUnknownWriteHandler(final ThrowingBiConsumer<GeometryOutput, GeometryFormat> fn) {
        // arrange
        final GeometryOutput withFileExt = new FileGeometryOutput(Paths.get("myfile.test"));
        final GeometryOutput noFileExt = new FileGeometryOutput(Paths.get("myfile"));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(
                () -> fn.accept(withFileExt, TEST_FMT),
                IllegalArgumentException.class, "Failed to find handler for format \"test\"");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> fn.accept(withFileExt, null),
                IllegalArgumentException.class, "Failed to find handler for file extension \"test\"");

        GeometryTestUtils.assertThrowsWithMessage(
                () -> fn.accept(noFileExt, null),
                IllegalArgumentException.class, "Failed to find handler: no format specified and no file extension available");
    }

    @FunctionalInterface
    private interface ThrowingBiConsumer<T, V> {
        void accept(T t, V v) throws Exception;
    }

    private static final class StubReadHandler3D implements BoundaryReadHandler3D {

        private GeometryInput inArg;

        private Precision.DoubleEquivalence precisionArg;

        /** {@inheritDoc} */
        @Override
        public GeometryFormat getFormat() {
            return TEST_FMT;
        }

        /** {@inheritDoc} */
        @Override
        public BoundarySource3D read(final GeometryInput in, final Precision.DoubleEquivalence precision) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<PlaneConvexSubset> boundaries(final GeometryInput in,
                final Precision.DoubleEquivalence precision) {
            this.inArg = in;
            this.precisionArg = precision;

            return Stream.of(TRI);
        }

        /** {@inheritDoc} */
        @Override
        public FacetDefinitionReader facetDefinitionReader(final GeometryInput in) {
            this.inArg = in;

            return FACET_DEF_READER;
        }

        /** {@inheritDoc} */
        @Override
        public Stream<FacetDefinition> facets(final GeometryInput in) {
            this.inArg = in;

            return Stream.of(FACET);
        }

        /** {@inheritDoc} */
        @Override
        public TriangleMesh readTriangleMesh(final GeometryInput in, final Precision.DoubleEquivalence precision) {
            this.inArg = in;
            this.precisionArg = precision;

            return TRI_MESH;
        }
    }

    private static final class StubWriteHandler3D implements BoundaryWriteHandler3D {

        private Collection<? extends PlaneConvexSubset> boundariesArg;

        private Collection<? extends FacetDefinition> facetsArg;

        private GeometryOutput outArg;

        /** {@inheritDoc} */
        @Override
        public GeometryFormat getFormat() {
            return TEST_FMT;
        }

        /** {@inheritDoc} */
        @Override
        public void write(final Stream<? extends PlaneConvexSubset> boundaries, final GeometryOutput out) {
            this.boundariesArg = boundaries.collect(Collectors.toList());
            this.outArg = out;
        }

        /** {@inheritDoc} */
        @Override
        public void write(final BoundarySource3D src, final GeometryOutput out) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public void writeFacets(final Stream<? extends FacetDefinition> facets, final GeometryOutput out) {
            this.facetsArg = facets.collect(Collectors.toList());
            this.outArg = out;
        }

        /** {@inheritDoc} */
        @Override
        public void writeFacets(final Collection<? extends FacetDefinition> facets, final GeometryOutput out) {
            this.facetsArg = facets;
            this.outArg = out;
        }
    }
}
