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

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.partitioning.BoundarySource;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Basic interface for reading geometric boundary representations
 * (<a href="https://en.wikipedia.org/wiki/Boundary_representation">B-reps</a>) from a specific data storage
 * format. This interface is intentionally kept simple to reduce the amount of work required by implementers.
 * Callers may prefer to access this functionality using the more convenient
 * {@link BoundaryIOManager} class instead.
 *
 * <p><strong>Implementation note:</strong> implementations of this interface <em>must</em>
 * be thread-safe.</p>
 * @param <H> Geometric boundary type
 * @param <B> Boundary source type
 * @see BoundaryWriteHandler
 * @see BoundaryIOManager
 * @see <a href="https://en.wikipedia.org/wiki/Boundary_representations">Boundary representations</a>
 */
public interface BoundaryReadHandler<H extends HyperplaneConvexSubset<?>, B extends BoundarySource<H>> {

    /** Return a {@link BoundarySource} containing all boundary information from the given input stream.
     * The stream is expected to contain data in the format supported by this handler. The exact type of the
     * return value will vary depending on the implementation and the details of the data storage format.
     *
     * <p>The input stream is <em>not</em> closed.</p>
     * @param in input stream to read from; this is <em>not</em> closed
     * @param precision precision context used for floating point comparisons
     * @return an object containing all boundary information from the input stream
     * @throws IOException if an I/O or data format error occurs
     */
    B read(InputStream in, DoublePrecisionContext precision) throws IOException;

    /** Return a {@link Stream} that can be used to access all boundary information from the given input stream.
     * The input stream is expected to contain data in the format supported by this handler. Unlike the
     * {@link #read(InputStream, DoublePrecisionContext) read} method, this method does not <em>require</em>
     * that all input be read immediately and stored in memory (although implementations of this interface are
     * still free to do so). Callers may therefore prefer to use this method in cases where memory usage is a
     * concern or transformations and/or filters must be applied to the boundaries before use.
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     *
     * <p>The input stream is <em>not</em> closed when the returned stream is closed.</p>
     * @param in input stream to read from; this is <em>not</em> closed when the returned stream is closed
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the boundary information from the given input stream
     * @throws IOException if an I/O error occurs during stream creation
     */
    Stream<H> boundaries(InputStream in, DoublePrecisionContext precision) throws IOException;
}
