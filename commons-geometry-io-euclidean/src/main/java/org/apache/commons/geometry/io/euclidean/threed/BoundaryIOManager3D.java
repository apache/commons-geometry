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
import org.apache.commons.geometry.io.core.BoundaryIOManager;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;

/** Class managing IO operations for geometric data formats containing 3D region boundaries.
 * Handlers are registered by format name and retrieved as needed. Methods that do not accept
 * the data format as an argument infer the format from the input file extension. Format names
 * are not case-sensitive.
 *
 * <p>Instances of this class are thread-safe as long as the registered handler instances are
 * thread-safe.</p>
 * @see BoundaryReadHandler3D
 * @see BoundaryWriteHandler3D
 * @see <a href="https://en.wikipedia.org/wiki/Boundary_representations">Boundary representations</a>
 */
public class BoundaryIOManager3D extends BoundaryIOManager<
        PlaneConvexSubset,
        BoundarySource3D,
        BoundaryReadHandler3D,
        BoundaryWriteHandler3D> {

    /** Get a {@link FacetDefinitionReader} for reading facet information from the given file path.
     * The data format is determined by the file extension of the argument.
     * @param path path to obtain a reader for
     * @return facet definition reader
     * @throws IllegalArgumentException if no handler has been registered for the indicated format
     * @throws IOException if an I/O or data format error occurs
     */
    public FacetDefinitionReader facetDefinitionReader(final Path path) throws IOException {
        return facetDefinitionReader(path.toUri().toURL());
    }

    /** Get a {@link FacetDefinitionReader} for reading facet information from the given URL.
     * The data format is determined by the file extension of the argument.
     * @param url URL to read from
     * @return facet definition reader
     * @throws IllegalArgumentException if no handler has been registered for the indicated format
     * @throws IOException if an I/O or data format error occurs
     */
    public FacetDefinitionReader facetDefinitionReader(final URL url) throws IOException {
        final BoundaryReadHandler3D readHandler = requireReadHandler(url);

        return GeometryIOUtils.tryApplyCloseable(
                readHandler::facetDefinitionReader,
                () -> getInputStream(url));
    }

    /** Get a {@link FacetDefinitionReader} for reading facet information from the given input stream.
     * The input stream is closed when the returned reader is closed.
     * @param in input stream containing data in the specified format
     * @param formatName input stream data format
     * @return facet definition reader
     * @throws IllegalArgumentException if no handler has been registered for the given format
     * @throws IOException if an I/O or data format error occurs
     */
    public FacetDefinitionReader facetDefinitionReader(final InputStream in, final String formatName)
            throws IOException {
        final BoundaryReadHandler3D readHandler = requireReadHandler(formatName);
        return readHandler.facetDefinitionReader(in);
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
     *      extension does not match a registered data format
     * @throws IOException if stream creation fails
     */
    public Stream<FacetDefinition> facets(final Path path) throws IOException {
        return facets(path.toUri().toURL());
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
     * @return stream providing access to the facets in the specified URL
     * @throws IllegalArgumentException if the URL path does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if stream creation fails
     */
    public Stream<FacetDefinition> facets(final URL url) throws IOException {
        final BoundaryReadHandler3D readHandler = requireReadHandler(url);

        return GeometryIOUtils.createCloseableStream(
                readHandler::facets,
                () -> getInputStream(url));
    }

    /** Return a {@link Stream} providing access to all facets from the given input stream.
     * The input stream is <em>not</em> closed when the returned stream is closed. An {@link IOException}
     * is thrown immediately by this method if stream creation fails. Any IO errors occurring during
     * stream iteration are wrapped with {@link java.io.UncheckedIOException}.
     * @param in input stream containing data in the specified format; this is <em>not</em> closed when
     *      the returned stream is closed
     * @param formatName data format of the input
     * @return stream providing access to the facets from the input stream
     * @throws IllegalArgumentException if no read handler is registered for the given format
     * @throws IOException if stream creation fails
     */
    public Stream<FacetDefinition> facets(final InputStream in, final String formatName) throws IOException {
        final BoundaryReadHandler3D readHandler = requireReadHandler(formatName);
        return readHandler.facets(in);
    }

    /** Return a {@link Stream} providing access to all triangles from the given file path.
     * The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly closed. Ex:
     * <pre>
     *  try (Stream&lt;Triangle3D&gt; stream = manager.triangles(path, precision)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the triangles in the specified file path
     * @throws IllegalArgumentException if the file path does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if stream creation fails
     */
    public Stream<Triangle3D> triangles(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return triangles(path.toUri().toURL(), precision);
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
     * @return stream providing access to the triangles in the specified URL
     * @throws IllegalArgumentException if the URL path does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if stream creation fails
     */
    public Stream<Triangle3D> triangles(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        return boundaries(url, precision)
                .flatMap(p -> p.toTriangles().stream());
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
     * @throws IllegalArgumentException if no read handler is registered for the given format
     * @throws IOException if stream creation fails
     */
    public Stream<Triangle3D> triangles(final InputStream in, final String formatName,
            final DoublePrecisionContext precision) throws IOException {
        return boundaries(in, formatName, precision)
                .flatMap(p -> p.toTriangles().stream());
    }

    /** Return a {@link TriangleMesh} containing all triangles from the given file path. The data
     * format is determined from the file extension of the path.
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return mesh containing all triangles from the given file path
     * @throws IllegalArgumentException if the file path does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if an I/O or data format error occurs
     */
    public TriangleMesh readTriangleMesh(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return readTriangleMesh(path.toUri().toURL(), precision);
    }

    /** Return a {@link TriangleMesh} containing all triangles from the given URL. The data
     * format is determined from the file extension of the URL path.
     * @param url URL to read from
     * @param precision precision context used for floating point comparisons
     * @return mesh containing all triangles from the given URL
     * @throws IllegalArgumentException if the URL path does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if an I/O or data format error occurs
     */
    public TriangleMesh readTriangleMesh(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        final BoundaryReadHandler3D readHandler = requireReadHandler(url);

        try (InputStream in = getInputStream(url)) {
            return readHandler.readTriangleMesh(in, precision);
        }
    }

    /** Return a {@link TriangleMesh} containing all triangles from the given input stream.
     * The input stream is <em>not</em> closed.
     * @param in input stream containing data in the specified format
     * @param formatName data format of the input
     * @param precision precision context used for floating point comparisons
     * @return a mesh containing all triangles from the input stream
     * @throws IllegalArgumentException if no read handler is registered for the given format
     * @throws IOException if an I/O or data format error occurs
     */
    public TriangleMesh readTriangleMesh(final InputStream in, final String formatName,
            final DoublePrecisionContext precision) throws IOException {
        final BoundaryReadHandler3D readHandler = requireReadHandler(formatName);
        return readHandler.readTriangleMesh(in, precision);
    }

    /** Write all boundaries in the stream to given file path. The data format is determined by
     * the file extension of the target path. If the target path already exists, it is overwritten.
     *
     * <p>This method does not explicitly close the {@code boundaries} stream. If callers need to ensure that
     * the stream is closed, they should use it in a try-with-resources statement outside of this method.</p>
     * @param boundaries stream containing boundaries to write
     * @param path file path to write to
     * @throws IllegalArgumentException if the target file does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if an I/O error occurs
     */
    public void write(final Stream<? extends PlaneConvexSubset> boundaries, final Path path) throws IOException {
        final BoundaryWriteHandler3D writeHandler = requireWriteHandler(path);

        try (OutputStream out = getOutputStream(path)) {
            writeHandler.write(boundaries, out);
        }
    }

    /** Write all boundaries in the stream to the output stream. The output stream is <em>not</em> closed.
     *
     * <p>This method does not explicitly close the {@code boundaries} stream. If callers need to ensure that
     * the stream is closed, they should use it in a try-with-resources statement outside of this method.</p>
     * @param boundaries stream containing boundaries to write
     * @param out output stream to write to
     * @param formatName format name
     * @throws IllegalArgumentException if no write handler is registered for the given format name
     * @throws IOException if an I/O error occurs
     */
    public void write(final Stream<? extends PlaneConvexSubset> boundaries, final OutputStream out,
            final String formatName) throws IOException {
        final BoundaryWriteHandler3D writeHandler = requireWriteHandler(formatName);
        writeHandler.write(boundaries, out);
    }

    /** Write all facets in the stream to the file path. The data format is determined by the file
     * extension of the target path. If the target path already exists, it is overwritten.
     *
     * <p>This method does not explicitly close the {@code facets} stream. If callers need to ensure that
     * the stream is closed, they should use it in a try-with-resources statement outside of this method.</p>
     * @param facets stream containing facets to write
     * @param path path to write to
     * @throws IllegalArgumentException if the target file does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if an I/O error occurs
     */
    public void writeFacets(final Stream<? extends FacetDefinition> facets, final Path path) throws IOException {
        final BoundaryWriteHandler3D writeHandler = requireWriteHandler(path);

        try (OutputStream out = getOutputStream(path)) {
            writeHandler.writeFacets(facets, out);
        }
    }

    /** Write all facets in the stream to the output stream. The output stream is <em>not</em> closed.
     *
     * <p>This method does not explicitly close the {@code facets} stream. If callers need to ensure that
     * the stream is closed, they should use it in a try-with-resources statement outside of this method.</p>
     * @param facets stream containing facets to write
     * @param out output stream to write to
     * @param formatName format name
     * @throws IllegalArgumentException if no write handler is registered for the given format name
     * @throws IOException if an I/O error occurs
     */
    public void writeFacets(final Stream<? extends FacetDefinition> facets, final OutputStream out,
            final String formatName) throws IOException {
        final BoundaryWriteHandler3D writeHandler = requireWriteHandler(formatName);
        writeHandler.writeFacets(facets, out);
    }

    /** Write the given facets to the file path. The data format is determined by the file extension of
     * the target path. If the target path already exists, it is overwritten.
     * @param facets facets to write
     * @param path path to write to
     * @throws IllegalArgumentException if the target file does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if an I/O error occurs
     */
    public void writeFacets(final Collection<? extends FacetDefinition> facets, final Path path)
            throws IOException {
        final BoundaryWriteHandler3D writeHandler = requireWriteHandler(path);

        try (OutputStream out = getOutputStream(path)) {
            writeHandler.writeFacets(facets, out);
        }
    }

    /** Write the given collection of facets to the output stream. The output stream
     * is <em>not</em> closed.
     * @param facets facets to write
     * @param out output stream to write to
     * @param formatName format name
     * @throws IllegalArgumentException if no write handler is registered for the given format name
     * @throws IOException if an I/O error occurs
     */
    public void writeFacets(final Collection<? extends FacetDefinition> facets, final OutputStream out,
            final String formatName) throws IOException {
        final BoundaryWriteHandler3D writeHandler = requireWriteHandler(formatName);
        writeHandler.writeFacets(facets, out);
    }
}
