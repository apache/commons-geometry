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

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.io.core.BoundaryReadHandler;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.numbers.core.Precision;

/** Basic interface for reading 3D geometric boundary representations
 * (<a href="https://en.wikipedia.org/wiki/Boundary_representation">B-reps</a>) from a specific data storage
 * format. This interface is primarily intended for use with {@link BoundaryIOManager3D}.
 *
 * <p><strong>Implementation note:</strong> implementations of this interface <em>must</em>
 * be thread-safe.</p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Boundary_representations">Boundary representations</a>
 * @see BoundaryWriteHandler3D
 * @see BoundaryIOManager3D
 */
public interface BoundaryReadHandler3D extends BoundaryReadHandler<PlaneConvexSubset, BoundarySource3D> {

    /** Return a {@link FacetDefinitionReader} for reading raw
     * {@link org.apache.commons.geometry.io.euclidean.threed.FacetDefinition facets} from the given
     * input stream.
     * @param in input stream to read from
     * @return facet definition reader instance
     * @throws IOException if an I/O or data format error occurs
     */
    FacetDefinitionReader facetDefinitionReader(GeometryInput in) throws IOException;

    /** Return a {@link Stream} that can be used to access all facet information from the given input stream.
     * The input stream is expected to contain data in the format supported by this handler.
     *
     * <p>The underlying input stream is closed when the returned stream is closed. Callers should therefore
     * use the returned stream in a try-with-resources statement to ensure that all resources are properly released.
     * </p>
     * <pre>
     *  try (Stream&lt;FacetDefinition&gt; stream = handler.facets(in)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param in input stream to read from; this is <em>not</em> closed when the returned stream is closed
     * @return stream providing access to the facet information from the given input stream
     * @throws IOException if an I/O or data format error occurs during stream creation
     */
    Stream<FacetDefinition> facets(GeometryInput in) throws IOException;

    /** Read a triangle mesh from the given input. Implementations may throw runtime
     * exceptions if mathematically invalid boundaries are encountered.
     * @param in input stream to read from
     * @param precision precision context used for floating point comparisons
     * @return triangle mesh containing the data from the given input stream
     * @throws IOException if an I/O or data format error occurs
     */
    TriangleMesh readTriangleMesh(GeometryInput in, Precision.DoubleEquivalence precision) throws IOException;
}
