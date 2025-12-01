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

import java.util.stream.Stream;

import org.apache.commons.geometry.core.partitioning.BoundarySource;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.numbers.core.Precision;

/** Basic interface for reading geometric boundary representations
 * (<a href="https://en.wikipedia.org/wiki/Boundary_representation">B-reps</a>) from a specific data storage
 * format. This interface is intended primarily for use with {@link BoundaryIOManager}.
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

    /** Get the {@link GeometryFormat data format} supported by this handler.
     * @return data format supported by this handler
     */
    GeometryFormat getFormat();

    /** Return an object containing all boundaries read from {@code input} using the handler's
     * supported data format.
     * @param input input to read from
     * @param precision precision context used for floating point comparisons
     * @return object containing all boundaries read from {@code input}
     * @throws IllegalArgumentException if mathematically invalid data is encountered
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    B read(GeometryInput input, Precision.DoubleEquivalence precision);

    /** Return a {@link Stream} that can be used to access all boundary information from the given input,
     * which is expected to contain data in the format supported by this handler. Unlike the
     * {@link #read(GeometryInput, Precision.DoubleEquivalence) read} method, this method does not <em>require</em>
     * that all input be read immediately and stored in memory (although implementations of this interface are
     * still free to do so). Callers may therefore prefer to use this method in cases where memory usage is a
     * concern or transformations and/or filters must be applied to the boundaries before use.
     *
     * <p>Implementing class will usually keep the source input stream open during stream iteration. Callers
     * should therefore use the returned stream in a try-with-resources statement to ensure that all resources
     * are properly released. Ex:
     * </p>
     * <pre>
     *  try (Stream&lt;H&gt; stream = handler.boundaries(in, precision)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>The following exceptions may be thrown during stream iteration:
     *  <ul>
     *      <li>{@link IllegalArgumentException} if mathematically invalid data is encountered</li>
     *      <li>{@link IllegalStateException} if a data format error occurs</li>
     *      <li>{@link java.io.UncheckedIOException UncheckedIOException} if an I/O error occurs</li>
     *  </ul>
     * @param in input to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the boundary information from the given input
     * @throws IllegalStateException if a data format error occurs during stream creation
     * @throws java.io.UncheckedIOException if an I/O error occurs during stream creation
     */
    Stream<H> boundaries(GeometryInput in, Precision.DoubleEquivalence precision);
}
