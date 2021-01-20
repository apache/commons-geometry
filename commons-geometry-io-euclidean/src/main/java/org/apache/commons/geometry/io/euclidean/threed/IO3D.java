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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;

/** Utility class providing convenient access to 3D IO functionality. The static read and write functions delegate
 * to a default {@link #getDefaultManager() DefaultBoundaryIOManager3D} instance. The default configuration should
 * be suitable for most purposes. If customization is required, consider directly creating and configuring and a
 * {@link BoundaryIOManager3D} instance instead.
 *
 * <p><strong>Examples</strong></p>
 * <p>The example below reads an OBJ file as a stream of triangles, transforms each triangle, and writes the
 * result to a CSV file.</p>
 * <pre>
 * Path origFile = Paths.get("orig.obj");
 * Path scaledFile = Paths.get("scaled.csv");
 * AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(2);
 *
 * // use the input triangle stream in a try-with-resources statement to ensure
 * // all resources are properly closed.
 * try (Stream&lt;Triangle3D&gt; stream = IO3D.triangles(origFile, precision)) {
 *      IO3D.write(stream.map(t -&gt; t.transform(transform)), scaledFile);
 * }
 * </pre>
 * @see DefaultBoundaryIOManager3D
 */
public final class IO3D {

    /** String representing the OBJ file format.
     * @see <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">Wavefront .obj file</a>
     */
    public static final String OBJ = "obj";

    /** String representing the simple text format described by
     * {@link org.apache.commons.geometry.io.euclidean.threed.txt.TextFacetDefinitionReader TextFacetDefinitionReader}
     * and
     * {@link org.apache.commons.geometry.io.euclidean.threed.txt.TextFacetDefinitionWriter TextFacetDefinitionWriter}.
     * This format describes facets by listing the coordinates of its vertices in order, with one facet
     * described per line. Facets may have 3 or more vertices and do not need to all have the same
     * number of vertices.
     */
    public static final String TXT = "txt";

    /** String representing the CSV file format as described by
     * {@link org.apache.commons.geometry.io.euclidean.threed.txt.TextFacetDefinitionWriter#csvFormat(java.io.Writer)
     * TextFacetDefinitionWriter}. When used to represent 3D geometry information, the coordinates of the vertices of
     * the facets are listed in order, with one facet defined per row. This is similar to the {@link #TXT} format
     * with the exception that facets are converted to triangles before writing so that all rows have the same
     * number of columns.
     */
    public static final String CSV = "csv";

    /** Utility class; no instantiation. */
    private IO3D() {}

    /** Get a {@link FacetDefinitionReader} for reading facet information from the given file path.
     * The data format is determined by the file extension of the argument.
     * @param path path to obtain a reader for
     * @return facet definition reader
     * @throws IllegalArgumentException if no handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the indicated format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#facetDefinitionReader(Path)
     */
    public static FacetDefinitionReader facetDefinitionReader(final Path path) throws IOException {
        return getDefaultManager().facetDefinitionReader(path);
    }

    /** Get a {@link FacetDefinitionReader} for reading facet information from the given URL.
     * The data format is determined by the file extension of the argument.
     * @param url URL to read from
     * @return facet definition reader
     * @throws IllegalArgumentException if no handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the indicated format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#facetDefinitionReader(URL)
     */
    public static FacetDefinitionReader facetDefinitionReader(final URL url) throws IOException {
        return getDefaultManager().facetDefinitionReader(url);
    }

    /** Get a {@link FacetDefinitionReader} for reading facet information from the given input stream.
     * The input stream is closed when the returned reader is closed.
     * @param in input stream containing data in the specified format
     * @param formatName input stream data format
     * @return facet definition reader
     * @throws IllegalArgumentException if no handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the given format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#facetDefinitionReader(InputStream, String)
     */
    public static FacetDefinitionReader facetDefinitionReader(final InputStream in, final String formatName)
            throws IOException {
        return getDefaultManager().facetDefinitionReader(in, formatName);
    }

    /** Return a {@link Stream} providing access to all facets from the given file path.
     * The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly closed. Ex:
     * <pre>
     *  try (Stream&lt;FacetDefinition&gt; stream = manager.facets(path)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param path file path to read from
     * @return stream providing access to the facets in the specified file
     * @throws IllegalArgumentException if the path does not have a file extension or the file
     *      extension does not match a data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#facets(Path)
     */
    public static Stream<FacetDefinition> facets(final Path path) throws IOException {
        return getDefaultManager().facets(path);
    }

    /** Return a {@link Stream} providing access to all facets from the given URL.
     * The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly closed. Ex:
     * <pre>
     *  try (Stream&lt;FacetDefinition&gt; stream = manager.facets(url)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param url URL to read from
     * @return stream providing access to the facets from the specified URL
     * @throws IllegalArgumentException if the URL path does not have a file extension or the file
     *      extension does not match a data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#facets(URL)
     */
    public static Stream<FacetDefinition> facets(final URL url) throws IOException {
        return getDefaultManager().facets(url);
    }

    /** Return a {@link Stream} providing access to all facets from the given input stream.
     * The input stream is <em>not</em> closed when the returned stream is closed. An {@link IOException}
     * is thrown immediately by this method if stream creation fails. Any IO errors occurring during
     * stream iteration are wrapped with {@link java.io.UncheckedIOException}.
     * @param in input stream containing data in the specified format; this is <em>not</em> closed when
     *      the returned stream is closed
     * @param formatName data format of the input
     * @return stream providing access to the facets in the input stream
     * @throws IllegalArgumentException if no read handler has been registered with the
     *      {@link #getDefaultManager() default manager} for the given format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#facets(InputStream, String)
     */
    public static Stream<FacetDefinition> facets(final InputStream in, final String formatName)
            throws IOException {
        return getDefaultManager().facets(in, formatName);
    }

    /** Return a {@link Stream} providing access to all boundaries from the given file path.
     * The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly closed. Ex:
     * <pre>
     *  try (Stream&lt;PlaneConvexSubset&gt; stream = manager.boundaries(path, precision)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the boundaries in the specified file
     * @throws IllegalArgumentException if the file path does not have a file extension or the file
     *      extension does not match a data format with the {@link #getDefaultManager() default manager}
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#boundaries(Path, DoublePrecisionContext)
     */
    public static Stream<PlaneConvexSubset> boundaries(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().boundaries(path, precision);
    }

    /** Return a {@link Stream} providing access to all boundaries from the given URL.
     * The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly closed. Ex:
     * <pre>
     *  try (Stream&lt;PlaneConvexSubset&gt; stream = manager.boundaries(url, precision)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param url URL to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the boundaries in the specified URL
     * @throws IllegalArgumentException if the URL path does not have a file extension or the file
     *      extension does not match a data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#boundaries(URL, DoublePrecisionContext)
     */
    public static Stream<PlaneConvexSubset> boundaries(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().boundaries(url, precision);
    }

    /** Return a {@link Stream} providing access to all boundaries from the given input stream.
     * The input stream is <em>not</em> closed when the returned stream is closed. An {@link IOException}
     * is thrown immediately by this method if stream creation fails. Any IO errors occurring during
     * stream iteration are wrapped with {@link java.io.UncheckedIOException}.
     * @param in input stream containing data in the specified format; this is <em>not</em> closed when
     *      the returned stream is closed
     * @param formatName data format of the input
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the boundaries in the input stream
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the given format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#boundaries(InputStream, String, DoublePrecisionContext)
     */
    public static Stream<PlaneConvexSubset> boundaries(final InputStream in, final String formatName,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().boundaries(in, formatName, precision);
    }

    /** Return a {@link Stream} providing access to all triangles from the given file path.
     * The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly closed. Ex:
     * <pre>
     *  try (Stream&lt;Triangle3D&gt; stream = manager.triangles(path)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the triangles in the specified file
     * @throws IllegalArgumentException if the file path does not have a file extension or the file
     *      extension does not match a data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#triangles(Path, DoublePrecisionContext)
     */
    public static Stream<Triangle3D> triangles(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().triangles(path, precision);
    }

    /** Return a {@link Stream} providing access to all triangles from the given URL.
     * The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly closed. Ex:
     * <pre>
     *  try (Stream&lt;Triangle3D&gt; stream = manager.triangles(url, precision)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param url URL to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the triangles from the specified URL
     * @throws IllegalArgumentException if the URL path does not have a file extension or the file
     *      extension does not match a data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#triangles(URL, DoublePrecisionContext)
     */
    public static Stream<Triangle3D> triangles(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().triangles(url, precision);
    }

    /** Return a {@link Stream} providing access to all triangles from the given input stream.
     * The input stream is <em>not</em> closed when the returned stream is closed. An {@link IOException}
     * is thrown immediately by this method if stream creation fails. Any IO errors occurring during
     * stream iteration are wrapped with {@link java.io.UncheckedIOException}.
     * @param in input stream containing data in the specified format; this is <em>not</em> closed when
     *      the returned stream is closed
     * @param formatName data format of the input
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the triangles in the input stream
     * @throws IllegalArgumentException if no read handler is registered the
     *      {@link #getDefaultManager() default manager}for the given format
     * @throws IOException if stream creation fails
     * @see BoundaryIOManager3D#triangles(InputStream, String, DoublePrecisionContext)
     */
    public static Stream<Triangle3D> triangles(final InputStream in, final String formatName,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().triangles(in, formatName, precision);
    }

    /** Return a {@link BoundarySource3D} containing all boundaries from the file at the
     * given path. The data format is determined from the file extension.
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return object containing all boundaries from the file at the given path
     * @throws IllegalArgumentException if the file does not have a file extension or the file
     *      extension does not match a data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#read(Path, DoublePrecisionContext)
     */
    public static BoundarySource3D read(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().read(path, precision);
    }

    /** Return a {@link BoundarySource3D} containing all boundaries from the given URL. The data
     * format is determined from the file extension of the URL path.
     * @param url URL to read from
     * @param precision precision context used for floating point comparisons
     * @return object containing all boundaries from the given URL
     * @throws IllegalArgumentException if the URL path does not have a file extension or the file
     *      extension does not match a data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#read(URL, DoublePrecisionContext)
     */
    public static BoundarySource3D read(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().read(url, precision);
    }

    /** Return a {@link BoundarySource3D} containing all boundaries from the given input stream.
     * The input stream is <em>not</em> closed.
     * @param in input stream containing data in the specified format
     * @param formatName data format of the input
     * @param precision precision context used for floating point comparisons
     * @return object containing all boundaries from the input stream
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the given format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#read(InputStream, String, DoublePrecisionContext)
     */
    public static BoundarySource3D read(final InputStream in, final String formatName,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().read(in, formatName, precision);
    }

    /** Return a {@link TriangleMesh} containing all triangles from the given file path. The data
     * format is determined from the file extension of the path.
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return mesh containing all triangles from the given file path
     * @throws IllegalArgumentException if the file path does not have a file extension or the file
     *      extension does not match a data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#readTriangleMesh(Path, DoublePrecisionContext)
     */
    public static TriangleMesh readTriangleMesh(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().readTriangleMesh(path, precision);
    }

    /** Return a {@link TriangleMesh} containing all triangles from the given URL. The data
     * format is determined from the file extension of the URL path.
     * @param url URL to read from
     * @param precision precision context used for floating point comparisons
     * @return mesh containing all triangles from the given URL
     * @throws IllegalArgumentException if the URL path does not have a file extension or the file
     *      extension does not match a data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#readTriangleMesh(URL, DoublePrecisionContext)
     */
    public static TriangleMesh readTriangleMesh(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return getDefaultManager().readTriangleMesh(url, precision);
    }

    /** Return a {@link TriangleMesh} containing all triangles from the given input stream.
     * The input stream is <em>not</em> closed.
     * @param in input stream containing data in the specified format
     * @param formatName data format of the input
     * @param precision precision context used for floating point comparisons
     * @return a mesh containing all triangles from the input stream
     * @throws IllegalArgumentException if no read handler is registered with the
     *      {@link #getDefaultManager() default manager} for the given format
     * @throws IOException if an I/O or data format error occurs
     * @see BoundaryIOManager3D#readTriangleMesh(Path, DoublePrecisionContext)
     */
    public static TriangleMesh readTriangleMesh(final InputStream in, final String formatName,
            final DoublePrecisionContext precision) throws IOException {
        return getDefaultManager().readTriangleMesh(in, formatName, precision);
    }

    /** Write all boundaries in the stream to given file path. The data format is determined by
     * the file extension of the target path. If the target path already exists, it is overwritten.
     *
     * <p>This method does not explicitly close the {@code boundaries} stream. Callers should use the stream
     * in a try-with-resources statement outside of this method if the stream is required to be closed.</p>
     * @param boundaries stream containing boundaries to write
     * @param path file path to write to
     * @throws IllegalArgumentException if the target file does not have a file extension or the file
     *      extension does not match data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#write(Stream, Path)
     */
    public static void write(final Stream<? extends PlaneConvexSubset> boundaries, final Path path) throws IOException {
        getDefaultManager().write(boundaries, path);
    }

    /** Write all boundaries in the stream to the output stream. The output stream is <em>not</em> closed.
     *
     * <p>This method does not explicitly close the {@code boundaries} stream. Callers should use the stream
     * in a try-with-resources statement outside of this method if the stream is required to be closed.</p>
     * @param boundaries stream containing boundaries to write
     * @param out output stream to write to
     * @param formatName format name
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the given format name
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#write(Stream, OutputStream, String)
     */
    public static void write(final Stream<? extends PlaneConvexSubset> boundaries, final OutputStream out,
            final String formatName) throws IOException {
        getDefaultManager().write(boundaries, out, formatName);
    }

    /** Write all boundaries from {@code src} to the given file path. The data format
     * is determined by the file extension of the target path. If the target path already exists,
     * it is overwritten.
     * @param src boundary source containing the boundaries to write
     * @param path file path to write to
     * @throws IllegalArgumentException if the target file does not have a file extension or the file
     *      extension does not match a data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if an I/O error occurs
     * @see org.apache.commons.geometry.io.core.BoundaryIOManager#write(org.apache.commons.geometry.core.partitioning.BoundarySource, Path)
     */
    public static void write(final BoundarySource3D src, final Path path)
            throws IOException {
        getDefaultManager().write(src, path);
    }

    /** Write all boundaries from {@code src} to the given output stream. The output stream
     * is <em>not</em> closed.
     * @param src boundary source containing the boundaries to write
     * @param out output stream to write to
     * @param formatName data format name; not case-sensitive
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the given format name
     * @throws IOException if an I/O error occurs
     * @see org.apache.commons.geometry.io.core.BoundaryIOManager#write(org.apache.commons.geometry.core.partitioning.BoundarySource, OutputStream, String)
     */
    public static void write(final BoundarySource3D src, final OutputStream out, final String formatName)
            throws IOException {
        getDefaultManager().write(src, out, formatName);
    }

    /** Write the given facets to the file path. The data format is determined by the file extension of
     * the target path. If the target path already exists, it is overwritten.
     * @param facets facets to write
     * @param path path to write to
     * @throws IllegalArgumentException if the target file does not have a file extension or the file
     *      extension does not match a data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#writeFacets(Collection, Path)
     */
    public static void writeFacets(final Collection<? extends FacetDefinition> facets, final Path path)
            throws IOException {
        getDefaultManager().writeFacets(facets, path);
    }

    /** Write the given collection of facets to the output stream. The output stream
     * is <em>not</em> closed.
     * @param facets facets to write
     * @param out output stream to write to
     * @param formatName format name
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the given format name
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#writeFacets(Collection, OutputStream, String)
     */
    public static void writeFacets(final Collection<? extends FacetDefinition> facets, final OutputStream out,
            final String formatName) throws IOException {
        getDefaultManager().writeFacets(facets, out, formatName);
    }

    /** Write all facets in the stream to the file path. The data format is determined by the file
     * extension of the target path. If the target path already exists, it is overwritten.
     *
     * <p>This method does not explicitly close the {@code facets} stream. Callers should use the stream
     * in a try-with-resources statement outside of this method if the stream is required to be closed.</p>
     * @param facets stream containing facets to write
     * @param path path to write to
     * @throws IllegalArgumentException if the target file does not have a file extension or the file
     *      extension does not match data format registered with the {@link #getDefaultManager() default manager}
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#writeFacets(Stream, Path)
     */
    public static void writeFacets(final Stream<? extends FacetDefinition> facets, final Path path) throws IOException {
        getDefaultManager().writeFacets(facets, path);
    }

    /** Write all facets in the stream to the output stream. The output stream is <em>not</em> closed.
     *
     * <p>This method does not explicitly close the {@code facets} stream. Callers should use the stream
     * in a try-with-resources statement outside of this method if the stream is required to be closed.</p>
     * @param facets stream containing facets to write
     * @param out output stream to write to
     * @param formatName format name
     * @throws IllegalArgumentException if no write handler is registered with the
     *      {@link #getDefaultManager() default manager} for the given format name
     * @throws IOException if an I/O error occurs
     * @see BoundaryIOManager3D#writeFacets(Collection, OutputStream, String)
     */
    public static void writeFacets(final Stream<? extends FacetDefinition> facets, final OutputStream out,
            final String formatName) throws IOException {
        getDefaultManager().writeFacets(facets, out, formatName);
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
        private static final BoundaryIOManager3D DEFAULT_MANAGER = new DefaultBoundaryIOManager3D();

        /** Utility class; no instantiation. */
        private ManagerHolder() {}
    }
}
