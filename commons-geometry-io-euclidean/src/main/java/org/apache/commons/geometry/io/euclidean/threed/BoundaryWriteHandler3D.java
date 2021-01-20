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
import java.io.OutputStream;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.io.core.BoundaryWriteHandler;

/** Basic interface for writing 3D geometric boundary representations
 * (<a href="https://en.wikipedia.org/wiki/Boundary_representation">B-reps</a>) in a specific data storage
 * format. Callers may prefer to access this functionality using the more convenient
 * {@link BoundaryIOManager3D} class instead.
 *
 * <p><strong>Implementation note:</strong> implementations of this interface <em>must</em>
 * be thread-safe.</p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Boundary_representations">Boundary representations</a>
 * @see BoundaryReadHandler3D
 * @see BoundaryIOManager3D
 */
public interface BoundaryWriteHandler3D extends BoundaryWriteHandler<PlaneConvexSubset, BoundarySource3D> {

    /** Write all boundaries contained in the stream to the given output stream using the data format
     * supported by this instance. The output stream is <em>not</em> closed.
     * @param boundaries stream containing boundaries to write
     * @param out output stream to write to
     * @throws IOException if an I/O error occurs
     */
    void write(Stream<? extends PlaneConvexSubset> boundaries, OutputStream out) throws IOException;

    /** Write the collection of {@link FacetDefinition facets} to the given output stream using the data
     * format supported by this instance. The output stream is <em>not</em> closed.
     * @param facets facets to write
     * @param out output stream to write to
     * @throws IOException if an I/O error occurs
     */
    void writeFacets(Collection<? extends FacetDefinition> facets, OutputStream out) throws IOException;

    /** Write all {@link FacetDefinition facets} from the stream to the given output stream using the data
     * format supported by this instance. The output stream is <em>not</em> closed.
     * @param facets stream containing facets to write
     * @param out output stream to write to
     * @throws IOException if an I/O error occurs
     */
    void writeFacets(Stream<? extends FacetDefinition> facets, OutputStream out) throws IOException;
}
