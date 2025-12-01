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

import org.apache.commons.geometry.core.partitioning.BoundarySource;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.io.core.output.GeometryOutput;

/** Basic interface for writing geometric boundary representations
 * (<a href="https://en.wikipedia.org/wiki/Boundary_representation">B-reps</a>) in a specific data storage
 * format. This interface is intended primarily for use with {@link BoundaryIOManager}.
 *
 * <p><strong>Implementation note:</strong> implementations of this interface <em>must</em>
 * be thread-safe.</p>
 * @param <H> Geometric boundary type
 * @param <B> Boundary source type
 * @see BoundaryReadHandler
 * @see BoundaryIOManager
 * @see <a href="https://en.wikipedia.org/wiki/Boundary_representations">Boundary representations</a>
 */
public interface BoundaryWriteHandler<H extends HyperplaneConvexSubset<?>, B extends BoundarySource<H>> {

    /** Get the {@link GeometryFormat data format} supported by this handler.
     * @return data format supported by this handler
     */
    GeometryFormat getFormat();

    /** Write all boundaries from {@code src} to the given output, using the data format for
     * the instance.
     * @param src boundary source
     * @param out output to write to
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    void write(B src, GeometryOutput out);
}
