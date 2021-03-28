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
package org.apache.commons.geometry.io.euclidean.threed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundaryList3D;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.input.StreamGeometryInput;
import org.apache.commons.geometry.io.core.test.CloseCountInputStream;
import org.apache.commons.geometry.io.euclidean.threed.AbstractBoundaryReadHandler3D.FacetDefinitionReaderIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AbstractBoundaryReadHandler3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION = new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final FacetDefinition FACET_1 = new SimpleFacetDefinition(Arrays.asList(
            Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 0), Vector3D.of(0, 1, 0)));

    private static final FacetDefinition FACET_2 = new SimpleFacetDefinition(Arrays.asList(
            Vector3D.ZERO, Vector3D.of(0, 1, 0), Vector3D.of(-1, 1, 0), Vector3D.of(-1, 0, 0)));

    @Test
    public void testRead() throws IOException {
        // arrange
        final List<FacetDefinition> facets = Arrays.asList(FACET_1, FACET_2);
        final TestReadHandler3D handler = new TestReadHandler3D(facets);

        final GeometryInput in = new StreamGeometryInput(new ByteArrayInputStream(new byte[0]));

        // act
        final BoundarySource3D result = handler.read(in, TEST_PRECISION);

        // assert
        Assertions.assertSame(in, handler.inArg);

        Assertions.assertEquals(BoundaryList3D.class, result.getClass());
        Assertions.assertEquals(2, result.toList().getBoundaries().size());
    }

    @Test
    public void testReadTriangleMesh() throws IOException {
        // arrange
        final List<FacetDefinition> facets = Arrays.asList(FACET_1, FACET_2);
        final TestReadHandler3D handler = new TestReadHandler3D(facets);

        final GeometryInput in = new StreamGeometryInput(new ByteArrayInputStream(new byte[0]));

        // act
        final TriangleMesh result = handler.readTriangleMesh(in, TEST_PRECISION);

        // assert
        Assertions.assertSame(in, handler.inArg);

        Assertions.assertEquals(SimpleTriangleMesh.class, result.getClass());
        Assertions.assertEquals(6, result.getVertexCount());
        Assertions.assertEquals(4, result.getFaceCount());
    }

    @Test
    public void testBoundaries() throws IOException {
        // arrange
        final List<FacetDefinition> facets = Arrays.asList(FACET_1, FACET_2);
        final TestReadHandler3D handler = new TestReadHandler3D(facets);

        final CloseCountInputStream inputStream = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));
        final GeometryInput in = new StreamGeometryInput(inputStream);

        // act
        final List<PlaneConvexSubset> list;
        try (Stream<PlaneConvexSubset> stream = handler.boundaries(in, TEST_PRECISION)) {
            list = stream.collect(Collectors.toList());

            Assertions.assertEquals(0, inputStream.getCloseCount());
        }

        // assert
        Assertions.assertSame(in, handler.inArg);

        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(1, inputStream.getCloseCount());
    }

    @Test
    public void testFacets() throws IOException {
        // arrange
        final List<FacetDefinition> facets = Arrays.asList(FACET_1, FACET_2);
        final TestReadHandler3D handler = new TestReadHandler3D(facets);

        final CloseCountInputStream inputStream = new CloseCountInputStream(new ByteArrayInputStream(new byte[0]));
        final GeometryInput in = new StreamGeometryInput(inputStream);

        // act
        final List<FacetDefinition> list;
        try (Stream<FacetDefinition> stream = handler.facets(in)) {
            list = stream.collect(Collectors.toList());

            Assertions.assertEquals(0, inputStream.getCloseCount());
        }

        // assert
        Assertions.assertSame(in, handler.inArg);

        Assertions.assertEquals(2, list.size());
        Assertions.assertEquals(1, inputStream.getCloseCount());
    }

    @Test
    public void testFacetIterator() {
        // arrange
        final StubFacetDefinitionReader reader = new StubFacetDefinitionReader(Arrays.asList(FACET_1, FACET_2));
        final FacetDefinitionReaderIterator it = new FacetDefinitionReaderIterator(reader);

        // act/assert
        Assertions.assertTrue(it.hasNext());
        Assertions.assertSame(FACET_1, it.next());

        Assertions.assertTrue(it.hasNext());
        Assertions.assertSame(FACET_2, it.next());

        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, it::next);
    }

    @Test
    public void testFacetIterator_readFails() {
        // arrange
        final StubFacetDefinitionReader reader = new StubFacetDefinitionReader(Arrays.asList(FACET_1, FACET_2));
        reader.fail = true;

        final FacetDefinitionReaderIterator it = new FacetDefinitionReaderIterator(reader);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(it::next, UncheckedIOException.class, "IOException: Read failure");
    }

    private static final class TestReadHandler3D extends AbstractBoundaryReadHandler3D {

        private final Collection<FacetDefinition> facets;

        private GeometryInput inArg;

        TestReadHandler3D(final Collection<FacetDefinition> facets) {
            this.facets = facets;
        }

        /** {@inheritDoc} */
        @Override
        public GeometryFormat getFormat() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public FacetDefinitionReader facetDefinitionReader(final GeometryInput in) throws IOException {
            this.inArg = in;

            return new StubFacetDefinitionReader(facets);
        }
    }

    private static final class StubFacetDefinitionReader implements FacetDefinitionReader {

        private final Iterator<FacetDefinition> iterator;

        private boolean fail = false;

        StubFacetDefinitionReader(final Collection<FacetDefinition> facets) {
            this.iterator = facets.iterator();
        }

        /** {@inheritDoc} */
        @Override
        public FacetDefinition readFacet() throws IOException {
            if (fail) {
                throw new IOException("Read failure");
            }

            return iterator.hasNext() ?
                iterator.next() :
                null;
        }

        /** {@inheritDoc} */
        @Override
        public void close() throws IOException {
            // do nothing
        }
    }
}
