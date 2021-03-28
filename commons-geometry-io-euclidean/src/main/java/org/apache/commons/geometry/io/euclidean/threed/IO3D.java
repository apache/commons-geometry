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
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.input.FileGeometryInput;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.input.UrlGeometryInput;
import org.apache.commons.geometry.io.core.output.FileGeometryOutput;
import org.apache.commons.geometry.io.core.output.GeometryOutput;

/** Utility class providing convenient access to 3D IO functionality. The static read and write methods here
 * delegate to a default {@link #getDefaultManager() BoundaryIOManager3D} instance. The default
 * configuration should be sufficient for most purposes. If customization is required, consider directly
 * creating and configuring and a {@link BoundaryIOManager3D} instance.
 *
 * <p><strong>Examples</strong></p>
 * <p>The example below reads an OBJ file as a stream of triangles, transforms each triangle, and writes the
 * result as a CSV file. The data formats are inferred from the input and output file extensions.</p>
 * <pre>
 * GeometryInput input = new FileGeometryInput(Paths.get("orig.obj"));
 * GeometryOutput scaledOutput = new FileGeometryOutput(Paths.get("scaled.csv"));
 * AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(2);
 *
 * // Use the input triangle stream in a try-with-resources statement to ensure
 * // all resources are properly released.
 * try (Stream&lt;Triangle3D&gt; stream = IO3D.triangles(input, null, precision)) {
 *      IO3D.write(stream.map(t -&gt; t.transform(transform)), scaledOutput, null);
 * }
 * </pre>
 * @see BoundaryIOManager3D
 */
public final class IO3D {

    /** Utility class; no instantiation. */
    private IO3D() {}

    /** Get a {@link FacetDefinitionReader} for reading facet information from the given file path.
     * The data format is determined by the file extension of the argument.
     * @param path path to obtain a reader for
     * @return facet definition reader
     * @throws IllegalArgumentException if no handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#facetDefinitionReader(GeometryInput, GeometryFormat)
     */
    public static FacetDefinitionReader facetDefinitionReader(final Path path) throws IOException {
        return facetDefinitionReader(new FileGeometryInput(path), null);
    }

    /** Get a {@link FacetDefinitionReader} for reading facet information from the given URL.
     * The data format is determined by the file extension of the argument.
     * @param url URL to read from
     * @return facet definition reader
     * @throws IllegalArgumentException if no handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#facetDefinitionReader(GeometryInput, GeometryFormat)
     */
    public static FacetDefinitionReader facetDefinitionReader(final URL url) throws IOException {
        return facetDefinitionReader(new UrlGeometryInput(url), null);
    }

    /** Get a {@link FacetDefinitionReader} for reading facet information from the given input.
     * @param in input to read from
     * @param fmt format of the input; if null, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @return facet definition reader
     * @throws IllegalArgumentException if no handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#facetDefinitionReader(GeometryInput, GeometryFormat)
     */
    public static FacetDefinitionReader facetDefinitionReader(final GeometryInput in, final GeometryFormat fmt)
            throws IOException {
        return getDefaultManager().facetDefinitionReader(in, fmt);
    }

    /** Return a {@link Stream} providing access to all facets from the given file path. The data format
     * is determined by the file extension of the argument.
     *
     * <p>The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly released. Ex:
     * </p>
     * <pre>
     *  try (Stream&lt;FacetDefinition&gt; stream = IO3D.facets(path)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param path file path to read from
     * @return stream providing access to the facets in the specified file
     * @throws IllegalArgumentException if no handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#facets(GeometryInput, GeometryFormat)
     */
    public static Stream<FacetDefinition> facets(final Path path) throws IOException {
        return facets(new FileGeometryInput(path), null);
    }

    /** Return a {@link Stream} providing access to all facets from the given URL. he data format
     * is determined by the file extension of the argument.
     *
     * <p>The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly released. Ex:
     * </p>
     * <pre>
     *  try (Stream&lt;FacetDefinition&gt; stream = IO3D.facets(url)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param url URL to read from
     * @return stream providing access to the facets from the specified URL
     * @throws IllegalArgumentException if no handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#facets(GeometryInput, GeometryFormat)
     */
    public static Stream<FacetDefinition> facets(final URL url) throws IOException {
        return facets(new UrlGeometryInput(url), null);
    }

    /** Return a {@link Stream} providing access to all facets from the given input. The underlying input
     * stream is closed when the returned stream is closed. Callers should therefore use the returned stream
     * in a try-with-resources statement to ensure that all resources are properly released.
     * <pre>
     *  try (Stream&lt;FacetDefinition&gt; stream = IO3D.facets(in, fmt)) {
     *      // access stream content
     *  }
     * </pre>
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param in input to read from
     * @param fmt format of the input; if null, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @return stream providing access to the facets in the input
     * @throws IllegalArgumentException if no read handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#facets(GeometryInput, GeometryFormat)
     */
    public static Stream<FacetDefinition> facets(final GeometryInput in, final GeometryFormat fmt)
            throws IOException {
        return getDefaultManager().facets(in, fmt);
    }

    /** Return a {@link Stream} providing access to all boundaries from the given file path. The
     * data format is determined by the file extension of the argument.
     *
     * <p>The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly released. Ex:
     * </p>
     * <pre>
     *  try (Stream&lt;PlaneConvexSubset&gt; stream = IO3D.boundaries(path, precision)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the boundaries in the specified file
     * @throws IllegalArgumentException if no read handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#boundaries(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static Stream<PlaneConvexSubset> boundaries(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return boundaries(new FileGeometryInput(path), null, precision);
    }

    /** Return a {@link Stream} providing access to all boundaries from the given URL. The data
     * format is determined by the file extension of the argument.
     *
     * <p>The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly released. Ex:
     * </p>
     * <pre>
     *  try (Stream&lt;PlaneConvexSubset&gt; stream = IO3D.boundaries(url, precision)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param url URL to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the boundaries in the specified URL
     * @throws IllegalArgumentException if no read handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#boundaries(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static Stream<PlaneConvexSubset> boundaries(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return boundaries(new UrlGeometryInput(url), null, precision);
    }

    /** Return a {@link Stream} providing access to all boundaries from the given input. The underlying input
     * stream is closed when the returned stream is closed. Callers should therefore use the returned stream
     * in a try-with-resources statement to ensure that all resources are properly released. Ex:
     * <pre>
     *  try (Stream&lt;H&gt; stream = IO3D.boundaries(in, fmt, precision)) {
     *      // access stream content
     *  }
     *  </pre>
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param in input to read boundaries from
     * @param fmt format of the input; if null, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the boundaries in the input
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#boundaries(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static Stream<PlaneConvexSubset> boundaries(final GeometryInput in, final GeometryFormat fmt,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().boundaries(in, fmt, precision);
    }

    /** Return a {@link Stream} providing access to all triangles from the given file path. The data
     * format is determined by the file extension of the argument.
     *
     * <p>The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly released. Ex:
     * </p>
     * <pre>
     *  try (Stream&lt;Triangle3D&gt; stream = IO3D.triangles(path, precision)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the triangles in the specified file
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#triangles(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static Stream<Triangle3D> triangles(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return triangles(new FileGeometryInput(path), null, precision);
    }

    /** Return a {@link Stream} providing access to all triangles from the given URL. The data format
     * is determined by the file extension of the argument.
     *
     * <p>The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly released. Ex:
     * </p>
     * <pre>
     *  try (Stream&lt;Triangle3D&gt; stream = IO3D.triangles(url, precision)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param url URL to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the triangles from the specified URL
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#triangles(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static Stream<Triangle3D> triangles(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return triangles(new UrlGeometryInput(url), null, precision);
    }

    /** Return a {@link Stream} providing access to all triangles from the given input. The underlying input
     * stream is closed when the returned stream is closed. Callers should therefore use the returned stream
     * in a try-with-resources statement to ensure that all resources are properly released.
     * <pre>
     *  try (Stream&lt;Triangle3D&gt; stream = IO3D.triangles(in, fmt, precision)) {
     *      // access stream content
     *  }
     * </pre>
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param in input to read from
     * @param fmt format of the input; if null, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the triangles in the input
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#triangles(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static Stream<Triangle3D> triangles(final GeometryInput in, final GeometryFormat fmt,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().triangles(in, fmt, precision);
    }

    /** Return a {@link BoundarySource3D} containing all boundaries from the file at the
     * given path. The data format is determined from the file extension.
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return object containing all boundaries from the file at the given path
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#read(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static BoundarySource3D read(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return read(new FileGeometryInput(path), null, precision);
    }

    /** Return a {@link BoundarySource3D} containing all boundaries from the given URL. The data
     * format is determined from the file extension of the URL path.
     * @param url URL to read from
     * @param precision precision context used for floating point comparisons
     * @return object containing all boundaries from the given URL
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#read(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static BoundarySource3D read(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return read(new UrlGeometryInput(url), null, precision);
    }

    /** Return a {@link BoundarySource3D} containing all boundaries from the given input.
     * @param in input to read boundaries from
     * @param fmt format of the input; if null, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @param precision precision context used for floating point comparisons
     * @return object containing all boundaries from the input
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#read(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static BoundarySource3D read(final GeometryInput in, final GeometryFormat fmt,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().read(in, fmt, precision);
    }

    /** Return a {@link TriangleMesh} containing all triangles from the given file path. The data
     * format is determined from the file extension of the path.
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return mesh containing all triangles from the given file path
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#readTriangleMesh(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static TriangleMesh readTriangleMesh(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return readTriangleMesh(new FileGeometryInput(path), null, precision);
    }

    /** Return a {@link TriangleMesh} containing all triangles from the given URL. The data
     * format is determined from the file extension of the URL path.
     * @param url URL to read from
     * @param precision precision context used for floating point comparisons
     * @return mesh containing all triangles from the given URL
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#readTriangleMesh(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static TriangleMesh readTriangleMesh(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return readTriangleMesh(new UrlGeometryInput(url), null, precision);
    }

    /** Return a {@link TriangleMesh} containing all triangles from the given input.
     * @param in input to read from
     * @param fmt format of the input; if null, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @param precision precision context used for floating point comparisons
     * @return a mesh containing all triangles from the input
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the input format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#readTriangleMesh(GeometryInput, GeometryFormat, DoublePrecisionContext)
     */
    public static TriangleMesh readTriangleMesh(final GeometryInput in, final GeometryFormat fmt,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().readTriangleMesh(in, fmt, precision);
    }

    /** Write all boundaries in the stream to given file path. The data format is determined by
     * the file extension of the target path. If the target path already exists, it is overwritten.
     *
     * <p>This method does not explicitly close the {@code boundaries} stream. Callers should use the stream
     * in a try-with-resources statement outside of this method if the stream is required to be closed.</p>
     * @param boundaries stream containing boundaries to write
     * @param path file path to write to
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the output format
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#write(Stream, GeometryOutput, GeometryFormat)
     */
    public static void write(final Stream<? extends PlaneConvexSubset> boundaries, final Path path) throws IOException {
        write(boundaries, new FileGeometryOutput(path), null);
    }

    /** Write all boundaries in the stream to the output.
     *
     * <p>This method does not explicitly close the {@code boundaries} stream. Callers should use the stream
     * in a try-with-resources statement outside of this method if the stream is required to be closed.</p>
     * @param boundaries stream containing boundaries to write
     * @param out output to write to
     * @param fmt format of the output; if null, the format is determined implicitly from the
     *      file extension of the output {@link GeometryOutput#getFileName() file name}
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the output format
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#write(Stream, GeometryOutput, GeometryFormat)
     */
    public static void write(final Stream<? extends PlaneConvexSubset> boundaries, final GeometryOutput out,
            final GeometryFormat fmt) throws IOException {
        getDefaultManager().write(boundaries, out, fmt);
    }

    /** Write all boundaries from {@code src} to the given file path. The data format
     * is determined by the file extension of the target path. If the target path already exists,
     * it is overwritten.
     * @param src boundary source containing the boundaries to write
     * @param path file path to write to
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the output format
     * @throws IOException if an I/O error occurs
     * @see org.apache.commons.geometry.io.core.BoundaryIOManager#write(
     *      org.apache.commons.geometry.core.partitioning.BoundarySource, GeometryOutput, GeometryFormat)
     */
    public static void write(final BoundarySource3D src, final Path path)
            throws IOException {
        write(src, new FileGeometryOutput(path), null);
    }

    /** Write all boundaries from {@code src} to the given output.
     * @param src boundary source containing the boundaries to write
     * @param out output to write to
     * @param fmt format of the output; if null, the format is determined implicitly from the
     *      file extension of the output {@link GeometryOutput#getFileName() file name}
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the output format
     * @throws IOException if an I/O error occurs
     * @see org.apache.commons.geometry.io.core.BoundaryIOManager#write(
     *      org.apache.commons.geometry.core.partitioning.BoundarySource, GeometryOutput, GeometryFormat)
     */
    public static void write(final BoundarySource3D src, final GeometryOutput out, final GeometryFormat fmt)
            throws IOException {
        getDefaultManager().write(src, out, fmt);
    }

    /** Write the given facets to the file path. The data format is determined by the file extension of
     * the target path. If the target path already exists, it is overwritten.
     * @param facets facets to write
     * @param path path to write to
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the output format
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#writeFacets(Collection, GeometryOutput, GeometryFormat)
     */
    public static void writeFacets(final Collection<? extends FacetDefinition> facets, final Path path)
            throws IOException {
        writeFacets(facets, new FileGeometryOutput(path), null);
    }

    /** Write the given collection of facets to the output.
     * @param facets facets to write
     * @param out output to write to
     * @param fmt format of the output; if null, the format is determined implicitly from the
     *      file extension of the output {@link GeometryOutput#getFileName() file name}
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the output format
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#writeFacets(Collection, GeometryOutput, GeometryFormat)
     */
    public static void writeFacets(final Collection<? extends FacetDefinition> facets, final GeometryOutput out,
            final GeometryFormat fmt) throws IOException {
        getDefaultManager().writeFacets(facets, out, fmt);
    }

    /** Write all facets in the stream to the file path. The data format is determined by the file
     * extension of the target path. If the target path already exists, it is overwritten.
     *
     * <p>This method does not explicitly close the {@code facets} stream. Callers should use the stream
     * in a try-with-resources statement outside of this method if the stream is required to be closed.</p>
     * @param facets stream containing facets to write
     * @param path path to write to
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the output format
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#writeFacets(Stream, GeometryOutput, GeometryFormat)
     */
    public static void writeFacets(final Stream<? extends FacetDefinition> facets, final Path path) throws IOException {
        writeFacets(facets, new FileGeometryOutput(path), null);
    }

    /** Write all facets in the stream to the output.
     *
     * <p>This method does not explicitly close the {@code facets} stream. Callers should use the stream
     * in a try-with-resources statement outside of this method if the stream is required to be closed.</p>
     * @param facets stream containing facets to write
     * @param out output to write to
     * @param fmt format of the output; if null, the format is determined implicitly from the
     *      file extension of the output {@link GeometryOutput#getFileName() file name}
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the output format
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#writeFacets(Stream, GeometryOutput, GeometryFormat)
     */
    public static void writeFacets(final Stream<? extends FacetDefinition> facets, final GeometryOutput out,
            final GeometryFormat fmt) throws IOException {
        getDefaultManager().writeFacets(facets, out, fmt);
    }

    /** Get the default {@link BoundaryIOManager3D} instance.
     * @return the default {@link BoundaryIOManager3D} instance
     */
    public static BoundaryIOManager3D getDefaultManager() {
        return ManagerHolder.DEFAULT_MANAGER;
    }

    /** Class holding a reference to the default IO manager instance.
     */
    private static final class ManagerHolder {

        /** Default IO manager instance. */
        private static final BoundaryIOManager3D DEFAULT_MANAGER;

        static {
            DEFAULT_MANAGER = new BoundaryIOManager3D();
            DEFAULT_MANAGER.registerDefaultHandlers();
        }

        /** Utility class; no instantiation. */
        private ManagerHolder() {}
    }
}
