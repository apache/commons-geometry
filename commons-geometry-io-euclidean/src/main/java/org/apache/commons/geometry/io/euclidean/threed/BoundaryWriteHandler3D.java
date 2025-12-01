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

import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.io.core.BoundaryWriteHandler;
import org.apache.commons.geometry.io.core.output.GeometryOutput;

/** Basic interface for writing 3D geometric boundary representations
 * (<a href="https://en.wikipedia.org/wiki/Boundary_representation">B-reps</a>) in a specific data storage
 * format. This interface is primarily intended for use with {@link BoundaryIOManager3D}.
 *
 * <p><strong>Implementation note:</strong> implementations of this interface <em>must</em>
 * be thread-safe.</p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Boundary_representations">Boundary representations</a>
 * @see BoundaryReadHandler3D
 * @see BoundaryIOManager3D
 */
public interface BoundaryWriteHandler3D extends BoundaryWriteHandler<PlaneConvexSubset, BoundarySource3D> {

    /** Write all boundaries in the stream to the given output using the data format supported by this
     * instance. The stream passed as an argument is <em>not</em> closed, meaning that callers are responsible
     * for closing the stream if necessary (for example, if the stream fetches data from the file system).
     * @param boundaries stream containing boundaries to write
     * @param out output to write to
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    void write(Stream<? extends PlaneConvexSubset> boundaries, GeometryOutput out);

    /** Write all {@link FacetDefinition facets} in the collection to the output using the data format
     * supported by this instance.
     * @param facets facets to write
     * @param out output to write to
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    void writeFacets(Collection<? extends FacetDefinition> facets, GeometryOutput out);

    /** Write all {@link FacetDefinition facets} in the stream to the output using the data format
     * supported by this instance. The stream passed as an argument is <em>not</em> closed, meaning
     * that callers are responsible for closing the stream if necessary (for example, if the stream
     * fetches data from the file system).
     * @param facets stream containing facets to write
     * @param out output to write to
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    void writeFacets(Stream<? extends FacetDefinition> facets, GeometryOutput out);
}
