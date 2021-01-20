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
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.partitioning.BoundarySource;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;

/** Class managing IO operations for geometric data formats containing region boundaries. Handlers
 * for specific formats are registered by format name and retrieved as needed. Methods that do not
 * accept the data format as an argument infer the format from the input file extension. Format names
 * are not case-sensitive.
 *
 * <p>Instances of this class are thread-safe as long as the registered handler instances are
 * thread-safe.</p>
 * @param <H> Geometric boundary type
 * @param <B> Boundary source type
 * @param <R> Read handler type
 * @param <W> Write handler type
 * @see BoundaryReadHandler
 * @see BoundaryWriteHandler
 * @see <a href="https://en.wikipedia.org/wiki/Boundary_representations">Boundary representations</a>
 */
public class BoundaryIOManager<
    H extends HyperplaneConvexSubset<?>,
    B extends BoundarySource<H>,
    R extends BoundaryReadHandler<H, B>,
    W extends BoundaryWriteHandler<H, B>> {

    /** Map of format names to read handlers. */
    private final Map<String, R> readHandlers = new HashMap<>();

    /** Map of format names to write handlers. */
    private final Map<String, W> writeHandlers = new HashMap<>();

    /** Return true if this instance supports reading input in the given format.
     * @param formatName format name; not case-sensitive
     * @return true if this instance can read the given format
     */
    public boolean readsFormat(final String formatName) {
        return formatName != null && getReadHandler(formatName) != null;
    }

    /** Return true if this instance supports writing output in the given format.
     * @param formatName format name; not case-sensitive
     * @return true if this instance can write the given format
     */
    public boolean writesFormat(final String formatName) {
        return formatName != null && getWriteHandler(formatName) != null;
    }

    /** Return a set containing the formats supported for reading. Format names are normalized
     * to lower case.
     * @return a set containing the formats supported for reading
     */
    public Set<String> getReadFormats() {
        synchronized (readHandlers) {
            return Collections.unmodifiableSet(new HashSet<>(readHandlers.keySet()));
        }
    }

    /** Return a set containing the formats supported for writing. Format names are normalized
     * to lower case.
     * @return a set containing the formats supported for writing
     */
    public Set<String> getWriteFormats() {
        synchronized (writeHandlers) {
            return Collections.unmodifiableSet(new HashSet<>(writeHandlers.keySet()));
        }
    }

    /** Get the read handler for the given format name or null if not found.
     * @param formatName format name; not case-sensitive
     * @return read handler for the given format name or null if not found
     */
    public R getReadHandler(final String formatName) {
        final String normalizedFormat = normalizeFormat(formatName);
        synchronized (readHandlers) {
            return readHandlers.get(normalizedFormat);
        }
    }

    /** Get the write handler for the given format name or null if not found.
     * @param formatName format name; not case-sensitive
     * @return write handler for the given format name or null if not found
     */
    public W getWriteHandler(final String formatName) {
        final String normalizedFormat = normalizeFormat(formatName);
        synchronized (writeHandlers) {
            return writeHandlers.get(normalizedFormat);
        }
    }

    /** Register a {@link BoundaryReadHandler} for the given format name. Any previously
     * registered handler for that name is replaced.
     * @param formatName format name; not case-sensitive
     * @param readHandler handler to register for the given format name
     */
    public void registerReadHandler(final String formatName, final R readHandler) {
        final String normalizedFormat = normalizeFormat(formatName);
        Objects.requireNonNull(readHandler, "Read handler cannot be null");

        synchronized (readHandlers) {
            readHandlers.put(normalizedFormat, readHandler);
        }
    }

    /** Register a {@link BoundaryWriteHandler} for the given format name. Any previously
     * registered handler for that name is replaced.
     * @param formatName format name; not case-sensitive
     * @param writeHandler handler to register for the given format name
     */
    public void registerWriteHandler(final String formatName, final W writeHandler) {
        final String normalizedFormat = normalizeFormat(formatName);
        Objects.requireNonNull(writeHandler, "Write handler cannot be null");

        synchronized (readHandlers) {
            writeHandlers.put(normalizedFormat, writeHandler);
        }
    }

    /** Return a {@link BoundarySource} containing all boundaries from the file at the
     * given path. The data format is determined from the file extension.
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return object containing all boundaries from the file at the given path
     * @throws IllegalArgumentException if the file does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if an I/O or data format error occurs
     */
    public B read(final Path path, final DoublePrecisionContext precision) throws IOException {
        return read(path.toUri().toURL(), precision);
    }

    /** Return a {@link BoundarySource} containing all boundaries from the given URL. The data
     * format is determined from the file extension of the URL path.
     * @param url URL to read from
     * @param precision precision context used for floating point comparisons
     * @return object containing all boundaries from the given URL
     * @throws IllegalArgumentException if the URL path does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if an I/O or data format error occurs
     */
    public B read(final URL url, final DoublePrecisionContext precision) throws IOException {
        final R reader = requireReadHandler(url);

        try (InputStream in = getInputStream(url)) {
            return reader.read(in, precision);
        }
    }

    /** Return a {@link BoundarySource} containing all boundaries from the given input stream.
     * The input stream is <em>not</em> closed.
     * @param in input stream containing data in the specified format
     * @param formatName data format of the input
     * @param precision precision context used for floating point comparisons
     * @return a boundary source containing the boundary information from the input stream
     * @throws IllegalArgumentException if no read handler is registered for the given format
     * @throws IOException if an I/O or data format error occurs
     */
    public B read(final InputStream in, final String formatName,
            final DoublePrecisionContext precision) throws IOException {
        final R reader = requireReadHandler(formatName);
        return reader.read(in, precision);
    }

    /** Return a {@link Stream} providing access to all boundaries from the file at the given path.
     * The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly closed. Ex:
     * <pre>
     *  try (Stream&lt;H&gt; stream = manager.boundaries(path, precision)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param path file path to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the boundaries in the specified file
     * @throws IllegalArgumentException if the path does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if stream creation fails
     */
    public Stream<H> boundaries(final Path path, final DoublePrecisionContext precision)
            throws IOException {
        return boundaries(path.toUri().toURL(), precision);
    }

    /** Return a {@link Stream} providing access to all boundaries from the given URL.
     * The underlying input stream is closed when the returned stream is closed. Callers should
     * therefore use the returned stream in a try-with-resources statement to ensure that all
     * resources are properly closed. Ex:
     * <pre>
     *  try (Stream&lt;H&gt; stream = manager.boundaries(url, precision)) {
     *      // access stream content
     *  }
     * </pre>
     *
     * <p>An {@link IOException} is thrown immediately by this method if stream creation fails. Any IO errors
     * occurring during stream iteration are wrapped with {@link java.io.UncheckedIOException}.</p>
     * @param url URL to read from
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the boundaries from the specified URL
     * @throws IllegalArgumentException if the URL path does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if stream creation fails
     */
    public Stream<H> boundaries(final URL url, final DoublePrecisionContext precision)
            throws IOException {
        final R reader = requireReadHandler(url);

        return GeometryIOUtils.createCloseableStream(
                in -> reader.boundaries(in, precision),
                () -> getInputStream(url));
    }

    /** Return a {@link Stream} providing access to all boundaries from the given input stream.
     * The input stream is <em>not</em> closed when the returned stream is closed. An {@link IOException}
     * is thrown immediately by this method if stream creation fails. Any IO errors occurring during
     * stream iteration are wrapped with {@link java.io.UncheckedIOException}.
     * @param in input stream containing data in the specified format
     * @param formatName data format of the input
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to the boundaries in the input stream
     * @throws IllegalArgumentException if no read handler is registered for the given format
     * @throws IOException if stream creation fails
     */
    public Stream<H> boundaries(final InputStream in, final String formatName,
            final DoublePrecisionContext precision) throws IOException {
        final R reader = requireReadHandler(formatName);
        return reader.boundaries(in, precision);
    }

    /** Write all boundaries from {@code src} to the given file path. The data format
     * is determined by the file extension of the target path. If the target path already exists,
     * it is overwritten.
     * @param src boundary source containing the boundaries to write
     * @param path file path to write to
     * @throws IllegalArgumentException if the target file does not have a file extension or the file
     *      extension does not match a registered data format
     * @throws IOException if an I/O error occurs
     */
    public void write(final B src, final Path path) throws IOException {
        final W writer = requireWriteHandler(path);

        try (OutputStream out = getOutputStream(path)) {
            writer.write(src, out);
        }
    }

    /** Write all boundaries from {@code src} to the given output stream. The output stream
     * is <em>not</em> closed.
     * @param src boundary source containing the boundaries to write
     * @param out output stream to write to
     * @param formatName data format name; not case-sensitive
     * @throws IllegalArgumentException if no write handler is registered for the given format name
     * @throws IOException if an I/O error occurs
     */
    public void write(final B src, final OutputStream out, final String formatName)
            throws IOException {
        final W writer = requireWriteHandler(formatName);
        writer.write(src, out);
    }

    /** Get the {@link BoundaryReadHandler read handler} registered for the given format name, throwing
     * an exception if one cannot be found.
     * @param formatName format name; not case-sensitive
     * @return read handler registered for the given format
     * @throws IllegalArgumentException if no handler has been registered for the given format
     */
    protected R requireReadHandler(final String formatName) {
        final R reader = getReadHandler(formatName);
        if (reader == null) {
            throw new IllegalArgumentException(
                    MessageFormat.format("No read handler registered for format \"{0}\"", formatName));
        }
        return reader;
    }

    /** Get the {@link BoundaryReadHandler read handler} registered for data format indicated
     * by the URL path file extension.
     * @param url URL to get a read handler for
     * @return read handler registered for the format indicated by the URL
     * @throws IllegalArgumentException if no handler has been registered for the indicated format
     */
    protected R requireReadHandler(final URL url) {
        final String formatName = getFormatForFileName(url.getPath());
        return requireReadHandler(formatName);
    }

    /** Get the {@link BoundaryWriteHandler write handler} registered for the given format name, throwing
     * an exception if one cannot be found.
     * @param formatName format name; not case-sensitive
     * @return write handler registered for the given format
     * @throws IllegalArgumentException if no handler has been registered for the given format
     */
    protected W requireWriteHandler(final String formatName) {
        final W writer = getWriteHandler(formatName);
        if (writer == null) {
            throw new IllegalArgumentException(
                    MessageFormat.format("No write handler registered for format \"{0}\"", formatName));
        }
        return writer;
    }

    /** Get the {@link BoundaryWriteHandler write handler} registered for the format indicated by the
     * path file extension.
     * @param path path to get a write handler for
     * @return write handler registered for the indicated format
     * @throws IllegalArgumentException if no handler has been registered for the indicated format
     */
    protected W requireWriteHandler(final Path path) {
        final String formatName = getFormatForFileName(path.toString());
        return requireWriteHandler(formatName);
    }

    /** Get the data format name indicated by the given file name or path, throwing an exception if one cannot
     * be determined. The file extension is used as the format name.
     * @param name file name or path
     * @return the data format indicated by the file name or path
     * @throws IllegalArgumentException if no data format can be determined
     */
    protected String getFormatForFileName(final String name) {
        final String ext = GeometryIOUtils.getFileExtension(name);
        if (ext == null || ext.length() < 1) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Cannot determine file data format: file name \"{0}\" does not have a file extension", name));
        }

        return ext;
    }

    /** Get an input stream for reading the content of the given URL.
     * @param url URL to get an input stream for
     * @return input stream for reading the content of the URL
     * @throws IOException if the stream cannot be created
     */
    protected InputStream getInputStream(final URL url) throws IOException {
        return url.openStream();
    }

    /** Get an output stream for writing to the given path.
     * @param path path to get an output stream for
     * @return output stream for the given path
     * @throws IOException if the stream cannot be created
     */
    protected OutputStream getOutputStream(final Path path) throws IOException {
        return Files.newOutputStream(path);
    }

    /** Normalize the given data format name.
     * @param formatName format name
     * @return normalized format name
     */
    private static String normalizeFormat(final String formatName) {
        Objects.requireNonNull(formatName, "Format name cannot be null");
        return formatName.toLowerCase();
    }
}
