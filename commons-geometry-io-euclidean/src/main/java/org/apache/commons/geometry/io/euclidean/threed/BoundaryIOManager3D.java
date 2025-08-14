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

import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.io.core.BoundaryIOManager;
import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.output.GeometryOutput;
import org.apache.commons.geometry.io.euclidean.threed.obj.ObjBoundaryReadHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.obj.ObjBoundaryWriteHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.stl.StlBoundaryReadHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.stl.StlBoundaryWriteHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.txt.CsvBoundaryReadHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.txt.CsvBoundaryWriteHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.txt.TextBoundaryReadHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.txt.TextBoundaryWriteHandler3D;
import org.apache.commons.numbers.core.Precision;

/** Class managing IO operations for geometric data formats containing 3D region boundaries.
 * IO operation are performed by read and write handlers registered for specific data formats.
 *
 * <p><strong>Implementation note:</strong>Instances of this class are thread-safe as long as the
 * registered handler instances are thread-safe.</p>
 * @see BoundaryReadHandler3D
 * @see BoundaryWriteHandler3D
 * @see <a href="https://en.wikipedia.org/wiki/Boundary_representations">Boundary representations</a>
 */
public class BoundaryIOManager3D extends BoundaryIOManager<
        PlaneConvexSubset,
        BoundarySource3D,
        BoundaryReadHandler3D,
        BoundaryWriteHandler3D> {

    /** Create an instance. */
    public BoundaryIOManager3D() {
        // Do nothing
    }

    /** Get a {@link FacetDefinitionReader} for reading facet information from the given input.
     * @param in input to read facets from
     * @param fmt format of the input; if {@code null}, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @return facet definition reader
     * @throws IllegalArgumentException if no read handler can be found for the input format
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public FacetDefinitionReader facetDefinitionReader(final GeometryInput in, final GeometryFormat fmt) {
        return requireReadHandler(in, fmt).facetDefinitionReader(in);
    }

    /** Return a {@link Stream} providing access to all facets from the given input. The underlying input
     * stream is closed when the returned stream is closed. Callers should therefore use the returned stream
     * in a try-with-resources statement to ensure that all resources are properly released.
     * <pre>
     *  try (Stream&lt;FacetDefinition&gt; stream = manager.facets(in, fmt)) {
     *      // access stream content
     *  }
     * </pre>
     * <p>The following exceptions may be thrown during stream iteration:</p>
     * <ul>
     *  <li>{@link IllegalStateException} if a data format error occurs</li>
     *  <li>{@link java.io.UncheckedIOException UncheckedIOException} if an I/O error occurs</li>
     * </ul>
     * @param in input to read from
     * @param fmt format of the input; if {@code null}, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @return stream providing access to the facets in the input
     * @throws IllegalArgumentException if no read handler can be found for the input format
     * @throws IllegalStateException if a data format error occurs during stream creation
     * @throws java.io.UncheckedIOException if an I/O error occurs during stream creation
     */
    public Stream<FacetDefinition> facets(final GeometryInput in, final GeometryFormat fmt) {
        return requireReadHandler(in, fmt).facets(in);
    }

    /** Return a {@link Stream} providing access to all triangles from the given input. The underlying input
     * stream is closed when the returned stream is closed. Callers should therefore use the returned stream
     * in a try-with-resources statement to ensure that all resources are properly released.
     * <pre>
     *  try (Stream&lt;Triangle3D&gt; stream = manager.triangles(in, fmt, precision)) {
     *      // access stream content
     *  }
     * </pre>
     * <p>The following exceptions may be thrown during stream iteration:</p>
     * <ul>
     *  <li>{@link IllegalArgumentException} if mathematically invalid data is encountered</li>
     *  <li>{@link IllegalStateException} if a data format error occurs</li>
     *  <li>{@link java.io.UncheckedIOException UncheckedIOException} if an I/O error occurs</li>
     * </ul>
     * @param in input to read from
     * @param fmt format of the input; if {@code null}, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the triangles in the input
     * @throws IllegalArgumentException if no read handler can be found for the input format
     * @throws IllegalStateException if a data format error occurs during stream creation
     * @throws java.io.UncheckedIOException if an I/O error occurs during stream creation
     */
    public Stream<Triangle3D> triangles(final GeometryInput in, final GeometryFormat fmt,
            final Precision.DoubleEquivalence precision) {
        return boundaries(in, fmt, precision)
                .flatMap(p -> p.toTriangles().stream());
    }

    /** Return a {@link TriangleMesh} containing all triangles from the given input.
     * @param in input to read from
     * @param fmt format of the input; if {@code null}, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @param precision precision context used for floating point comparisons
     * @return mesh containing all triangles from the input
     * @throws IllegalArgumentException if mathematically invalid data is encountered or no read
     *      handler can be found for the input format
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public TriangleMesh readTriangleMesh(final GeometryInput in, final GeometryFormat fmt,
            final Precision.DoubleEquivalence precision) {
        return requireReadHandler(in, fmt).readTriangleMesh(in, precision);
    }

    /** Write all boundaries in the stream to the output.
     *
     * <p>This method does not explicitly close the {@code boundaries} stream. If callers need to ensure that
     * the stream is closed (for example, if the stream is reading from a file), they should use it in a
     * try-with-resources statement outside of this method.</p>
     * @param boundaries stream containing boundaries to write
     * @param out output to write to
     * @param fmt format of the output; if {@code null}, the format is determined implicitly from the
     *      file extension of the output {@link GeometryOutput#getFileName() file name}
     * @throws IllegalArgumentException if no write handler can be found for the output format
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void write(final Stream<? extends PlaneConvexSubset> boundaries, final GeometryOutput out,
            final GeometryFormat fmt) {
        requireWriteHandler(out, fmt).write(boundaries, out);
    }

    /** Write all facet in the stream to the output.
     *
     * <p>This method does not explicitly close the {@code boundaries} stream. If callers need to ensure that
     * the stream is closed (for example, if the stream is reading from a file), they should use it in a
     * try-with-resources statement outside of this method.</p>
     * @param facets stream containing facets to write
     * @param out output to write to
     * @param fmt format of the output; if {@code null}, the format is determined implicitly from the
     *      file extension of the output {@link GeometryOutput#getFileName() file name}
     * @throws IllegalArgumentException if no write handler can be found for the output format
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeFacets(final Stream<? extends FacetDefinition> facets, final GeometryOutput out,
            final GeometryFormat fmt) {
        requireWriteHandler(out, fmt).writeFacets(facets, out);
    }

    /** Write the given facets to the output.
     * @param facets facets to write
     * @param out output to write to
     * @param fmt format of the output; if {@code null}, the format is determined implicitly from the
     *      file extension of the output {@link GeometryOutput#getFileName() file name}
     * @throws IllegalArgumentException if no write handler can be found for the output format
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeFacets(final Collection<? extends FacetDefinition> facets, final GeometryOutput out,
            final GeometryFormat fmt) {
        requireWriteHandler(out, fmt).writeFacets(facets, out);
    }

    /** Register default read/write handlers. This method registers a read and write handler
     * for each value in {@link GeometryFormat3D}.
     */
    public void registerDefaultHandlers() {
        // obj
        registerReadHandler(new ObjBoundaryReadHandler3D());
        registerWriteHandler(new ObjBoundaryWriteHandler3D());

        // stl
        registerReadHandler(new StlBoundaryReadHandler3D());
        registerWriteHandler(new StlBoundaryWriteHandler3D());

        // txt
        registerReadHandler(new TextBoundaryReadHandler3D());
        registerWriteHandler(new TextBoundaryWriteHandler3D());

        // csv
        registerReadHandler(new CsvBoundaryReadHandler3D());
        registerWriteHandler(new CsvBoundaryWriteHandler3D());
    }
}
