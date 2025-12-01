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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.geometry.euclidean.threed.BoundaryList3D;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;
import org.apache.commons.numbers.core.Precision;

/** Abstract base class for {@link BoundaryReadHandler3D} implementations.
 */
public abstract class AbstractBoundaryReadHandler3D implements BoundaryReadHandler3D {

    /** Create an instance. */
    public AbstractBoundaryReadHandler3D() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public BoundarySource3D read(final GeometryInput in, final Precision.DoubleEquivalence precision) {
        // read the input as a simple list of boundaries
        final List<PlaneConvexSubset> list = new ArrayList<>();

        try (FacetDefinitionReader reader = facetDefinitionReader(in)) {
            FacetDefinition facet;
            while ((facet = reader.readFacet()) != null) {
                list.add(FacetDefinitions.toPolygon(facet, precision));
            }
        }

        return new BoundaryList3D(list);
    }

    /** {@inheritDoc} */
    @Override
    public TriangleMesh readTriangleMesh(final GeometryInput in, final Precision.DoubleEquivalence precision) {
        final SimpleTriangleMesh.Builder meshBuilder = SimpleTriangleMesh.builder(precision);

        try (FacetDefinitionReader reader = facetDefinitionReader(in)) {
            FacetDefinition facet;
            while ((facet = reader.readFacet()) != null) {
                for (final Triangle3D tri : FacetDefinitions.toPolygon(facet, precision).toTriangles()) {
                    meshBuilder.addFaceUsingVertices(
                        tri.getPoint1(),
                        tri.getPoint2(),
                        tri.getPoint3()
                    );
                }
            }
        }

        return meshBuilder.build();
    }

    /** {@inheritDoc} */
    @Override
    public Stream<PlaneConvexSubset> boundaries(final GeometryInput in, final Precision.DoubleEquivalence precision) {
        return facets(in)
                .map(f -> FacetDefinitions.toPolygon(f, precision));
    }

    /** {@inheritDoc} */
    @Override
    public Stream<FacetDefinition> facets(final GeometryInput in) {
        return GeometryIOUtils.createCloseableStream(inputStream -> {
            final FacetDefinitionReader fdReader = facetDefinitionReader(in);
            final FacetDefinitionReaderIterator it = new FacetDefinitionReaderIterator(fdReader);

            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED), false);
        }, in::getInputStream);
    }

    /** Class exposing a {@link FacetDefinitionReader} as an iterator.
     */
    static final class FacetDefinitionReaderIterator implements Iterator<FacetDefinition> {

        /** Reader supplying the facets for iteration. */
        private final FacetDefinitionReader reader;

        /** Number of facets read from the reader. */
        private int loadCount;

        /** Next facet to return from the instance; may be null. */
        private FacetDefinition next;

        /** Construct a new iterator instance that iterates through the facets available from the
         * argument.
         * @param reader read supplying facets for iteration
         */
        FacetDefinitionReaderIterator(final FacetDefinitionReader reader) {
            this.reader = reader;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            ensureLoaded();
            return next != null;
        }

        /** {@inheritDoc} */
        @Override
        public FacetDefinition next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            final FacetDefinition result = next;
            loadNext();

            return result;
        }

        /** Ensure that the instance has attempted to load at least one facet from
         * the underlying reader.
         */
        private void ensureLoaded() {
            if (loadCount < 1) {
                loadNext();
            }
        }

        /** Load the next facet from the underlying reader.
         */
        private void loadNext() {
            ++loadCount;
            next = reader.readFacet();
        }
    }
}
