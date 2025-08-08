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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.partitioning.BoundarySource;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;
import org.apache.commons.geometry.io.core.output.GeometryOutput;
import org.apache.commons.numbers.core.Precision;

/** Class managing IO operations for geometric data formats containing region boundaries.
 * All IO operations are delegated to registered format-specific {@link BoundaryReadHandler read handlers}
 * and {@link BoundaryWriteHandler write handlers}.
 *
 * <p><strong>Exceptions</strong>
 * <p>Despite having functionality related to I/O operations, this class has been designed to <em>not</em>
 * throw checked exceptions, in particular {@link java.io.IOException IOException}. The primary reasons for
 * this choice are
 * <ul>
 *  <li>convenience,</li>
 *  <li>compatibility with functional programming, and </li>
 *  <li>the fact that modern Java practice is moving away from checked exceptions in general (as exemplified
 *      by the JDK's {@link java.io.UncheckedIOException UncheckedIOException}).</li>
 * </ul>
 * As a result, any {@link java.io.IOException IOException} thrown internally by this or related classes
 * is wrapped with {@link java.io.UncheckedIOException UncheckedIOException}. Other common runtime exceptions
 * include {@link IllegalArgumentException}, which typically indicates mathematically invalid data, and
 * {@link IllegalStateException}, which typically indicates format or parsing errors. See the method-level
 * documentation for more details.
 *
 * <p><strong>Implementation note:</strong> Instances of this class are thread-safe as long as the
 * registered handler instances are thread-safe.</p>
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

    /** Error message used when a handler is null. */
    private static final String HANDLER_NULL_ERR = "Handler cannot be null";

    /** Error message used when a format is null. */
    private static final String FORMAT_NULL_ERR = "Format cannot be null";

    /** Error message used when a format name is null. */
    private static final String FORMAT_NAME_NULL_ERR = "Format name cannot be null";

    /** Read handler registry. */
    private final HandlerRegistry<R> readRegistry = new HandlerRegistry<>();

    /** Write handler registry. */
    private final HandlerRegistry<W> writeRegistry = new HandlerRegistry<>();

    /** Create an instance. */
    public BoundaryIOManager() {
        // Do nothing
    }

    /** Register a {@link BoundaryReadHandler read handler} with the instance, replacing
     * any handler previously registered for the argument's supported data format, as returned
     * by {@link BoundaryReadHandler#getFormat()}.
     * @param handler handler to register
     * @throws NullPointerException if {@code handler}, its {@link BoundaryReadHandler#getFormat() format},
     *      or the {@link GeometryFormat#getFormatName() format's name} are null
     */
    public void registerReadHandler(final R handler) {
        Objects.requireNonNull(handler, HANDLER_NULL_ERR);
        readRegistry.register(handler.getFormat(), handler);
    }

    /** Unregister a previously registered {@link BoundaryReadHandler read handler};
     * does nothing if the argument is null or is not currently registered.
     * @param handler handler to unregister; may be null
     */
    public void unregisterReadHandler(final R handler) {
        readRegistry.unregister(handler);
    }

    /** Get all registered {@link BoundaryReadHandler read handlers}.
     * @return list containing all registered read handlers
     */
    public List<R> getReadHandlers() {
        return readRegistry.getHandlers();
    }

    /** Get the list of formats supported by the currently registered
     * {@link BoundaryReadHandler read handlers}.
     * @return list of read formats
     * @see BoundaryReadHandler#getFormat()
     */
    public List<GeometryFormat> getReadFormats() {
        return readRegistry.getHandlers().stream()
                .map(BoundaryReadHandler::getFormat)
                .collect(Collectors.toList());
    }

    /** Get the {@link BoundaryReadHandler read handler} for the given format or
     * null if no such handler has been registered.
     * @param fmt format to obtain a handler for
     * @return read handler for the given format or null if not found
     */
    public R getReadHandlerForFormat(final GeometryFormat fmt) {
        return readRegistry.getByFormat(fmt);
    }

    /** Get the {@link BoundaryReadHandler read handler} for the given file extension
     * or null if no such handler has been registered. File extension comparisons are
     * not case-sensitive.
     * @param fileExt file extension to obtain a handler for
     * @return read handler for the given file extension or null if not found
     * @see GeometryFormat#getFileExtensions()
     */
    public R getReadHandlerForFileExtension(final String fileExt) {
        return readRegistry.getByFileExtension(fileExt);
    }

    /** Register a {@link BoundaryWriteHandler write handler} with the instance, replacing
     * any handler previously registered for the argument's supported data format, as returned
     * by {@link BoundaryWriteHandler#getFormat()}.
     * @param handler handler to register
     * @throws NullPointerException if {@code handler}, its {@link BoundaryWriteHandler#getFormat() format},
     *      or the {@link GeometryFormat#getFormatName() format's name} are null
     */
    public void registerWriteHandler(final W handler) {
        Objects.requireNonNull(handler, HANDLER_NULL_ERR);
        writeRegistry.register(handler.getFormat(), handler);
    }

    /** Unregister a previously registered {@link BoundaryWriteHandler write handler};
     * does nothing if the argument is null or is not currently registered.
     * @param handler handler to unregister; may be null
     */
    public void unregisterWriteHandler(final W handler) {
        writeRegistry.unregister(handler);
    }

    /** Get all registered {@link BoundaryWriteHandler write handlers}.
     * @return list containing all registered write handlers
     */
    public List<W> getWriteHandlers() {
        return writeRegistry.getHandlers();
    }

    /** Get the list of formats supported by the currently registered
     * {@link BoundaryWriteHandler write handlers}.
     * @return list of write formats
     * @see BoundaryWriteHandler#getFormat()
     */
    public List<GeometryFormat> getWriteFormats() {
        return writeRegistry.getHandlers().stream()
                .map(BoundaryWriteHandler::getFormat)
                .collect(Collectors.toList());
    }

    /** Get the {@link BoundaryWriteHandler write handler} for the given format or
     * null if no such handler has been registered.
     * @param fmt format to obtain a handler for
     * @return write handler for the given format or null if not found
     */
    public W getWriteHandlerForFormat(final GeometryFormat fmt) {
        return writeRegistry.getByFormat(fmt);
    }

    /** Get the {@link BoundaryWriteHandler write handler} for the given file extension
     * or null if no such handler has been registered. File extension comparisons are
     * not case-sensitive.
     * @param fileExt file extension to obtain a handler for
     * @return write handler for the given file extension or null if not found
     * @see GeometryFormat#getFileExtensions()
     */
    public W getWriteHandlerForFileExtension(final String fileExt) {
        return writeRegistry.getByFileExtension(fileExt);
    }

    /** Return a {@link BoundarySource} containing all boundaries from the given input.
     * A runtime exception may be thrown if mathematically invalid boundaries are encountered.
     * @param in input to read boundaries from
     * @param fmt format of the input; if null, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @param precision precision context used for floating point comparisons
     * @return object containing all boundaries from the input
     * @throws IllegalArgumentException if mathematically invalid data is encountered or no
     *      {@link BoundaryReadHandler read handler} can be found for the input format
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public B read(final GeometryInput in, final GeometryFormat fmt, final Precision.DoubleEquivalence precision) {
        return requireReadHandler(in, fmt).read(in, precision);
    }

    /** Return a {@link Stream} providing access to all boundaries from the given input. The underlying input
     * stream is closed when the returned stream is closed. Callers should therefore use the returned stream
     * in a try-with-resources statement to ensure that all resources are properly released. Ex:
     * <pre>
     *  try (Stream&lt;H&gt; stream = manager.boundaries(in, fmt, precision)) {
     *      // access stream content
     *  }
     *  </pre>
     * <p>The following exceptions may be thrown during stream iteration:
     *  <ul>
     *      <li>{@link IllegalArgumentException} if mathematically invalid data is encountered</li>
     *      <li>{@link IllegalStateException} if a data format error occurs</li>
     *      <li>{@link java.io.UncheckedIOException UncheckedIOException} if an I/O error occurs</li>
     *  </ul>
     * @param in input to read boundaries from
     * @param fmt format of the input; if null, the format is determined implicitly from the
     *      file extension of the input {@link GeometryInput#getFileName() file name}
     * @param precision precision context used for floating point comparisons
     * @return stream providing access to all boundaries from the input
     * @throws IllegalArgumentException if no {@link BoundaryReadHandler read handler} can be found for
     *      the input format
     * @throws IllegalStateException if a data format error occurs during stream creation
     * @throws java.io.UncheckedIOException if an I/O error occurs during stream creation
     */
    public Stream<H> boundaries(final GeometryInput in, final GeometryFormat fmt,
            final Precision.DoubleEquivalence precision) {
        return requireReadHandler(in, fmt).boundaries(in, precision);
    }

    /** Write all boundaries from {@code src} to the given output.
     * @param src object containing boundaries to write
     * @param out output to write boundaries to
     * @param fmt format of the output; if null, the format is determined implicitly from the
     *      file extension of the output {@link GeometryOutput#getFileName()}
     * @throws IllegalArgumentException if no {@link BoundaryWriteHandler write handler} can be found
     *      for the output format
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void write(final B src, final GeometryOutput out, final GeometryFormat fmt) {
        requireWriteHandler(out, fmt).write(src, out);
    }

    /** Get the {@link BoundaryReadHandler read handler} matching the arguments, throwing an exception
     * on failure. If {@code fmt} is given, the handler registered for that format is returned and the
     * {@code input} object is not examined. If {@code fmt} is null, the file extension of the input
     * {@link GeometryInput#getFileName() file name} is used to implicitly determine the format and locate
     * the handler.
     * @param in input object
     * @param fmt format; may be null
     * @return the read handler for {@code fmt} or, if {@code fmt} is null, the read handler for the
     *      file extension indicated by the input
     * @throws NullPointerException if {@code in} is null
     * @throws IllegalArgumentException if no matching handler can be found
     */
    protected R requireReadHandler(final GeometryInput in, final GeometryFormat fmt) {
        Objects.requireNonNull(in, "Input cannot be null");
        return readRegistry.requireHandlerByFormatOrFileName(fmt, in.getFileName());
    }

    /** Get the {@link BoundaryWriteHandler write handler} matching the arguments, throwing an exception
     * on failure. If {@code fmt} is given, the handler registered for that format is returned and the
     * {@code input} object is not examined. If {@code fmt} is null, the file extension of the output
     * {@link GeometryOutput#getFileName() file name} is used to implicitly determine the format and locate
     * the handler.
     * @param out output object
     * @param fmt format; may be null
     * @return the write handler for {@code fmt} or, if {@code fmt} is null, the write handler for the
     *      file extension indicated by the output
     * @throws NullPointerException if {@code out} is null
     * @throws IllegalArgumentException if no matching handler can be found
     */
    protected W requireWriteHandler(final GeometryOutput out, final GeometryFormat fmt) {
        Objects.requireNonNull(out, "Output cannot be null");
        return writeRegistry.requireHandlerByFormatOrFileName(fmt, out.getFileName());
    }

    /** Internal class used to manage handler registration. Instances of this class
     * are thread-safe.
     * @param <T> Handler type
     */
    private static final class HandlerRegistry<T> {

        /** List of registered handlers. */
        private final List<T> handlers = new ArrayList<>();

        /** Handlers keyed by lower-case format name. */
        private final Map<String, T> handlersByFormatName = new HashMap<>();

        /** Handlers keyed by lower-case file extension. */
        private final Map<String, T> handlersByFileExtension = new HashMap<>();

        /** Register a handler for the given {@link GeometryFormat format}.
         * @param fmt format for the handler
         * @param handler handler to register
         * @throws NullPointerException if either argument is null
         */
        public synchronized void register(final GeometryFormat fmt, final T handler) {
            Objects.requireNonNull(fmt, FORMAT_NULL_ERR);
            Objects.requireNonNull(handler, HANDLER_NULL_ERR);

            if (!handlers.contains(handler)) {
                // remove any previously registered handler
                unregisterFormat(fmt);

                // add the new handler
                addToFormat(fmt.getFormatName(), handler);
                addToFileExtensions(fmt.getFileExtensions(), handler);

                handlers.add(handler);
            }
        }

        /** Unregister the given handler.
         * @param handler handler to unregister
         */
        public synchronized void unregister(final T handler) {
            if (handler != null && handlers.remove(handler)) {
                removeValue(handlersByFormatName, handler);
                removeValue(handlersByFileExtension, handler);
            }
        }

        /** Unregister the current handler for the given format and return it.
         * Null is returned if no handler was registered.
         * @param fmt format to unregister
         * @return handler instance previously registered for the format or null
         *      if not found
         */
        public synchronized T unregisterFormat(final GeometryFormat fmt) {
            final T handler = getByFormat(fmt);
            if (handler != null) {
                unregister(handler);
            }
            return handler;
        }

        /** Get all registered handlers.
         * @return list of all registered handlers
         */
        public synchronized List<T> getHandlers() {
            return Collections.unmodifiableList(new ArrayList<>(handlers));
        }

        /** Get the first handler registered for the given format, or null if
         * not found.
         * @param fmt format to obtain a handler for
         * @return first handler registered for the format
         */
        public synchronized T getByFormat(final GeometryFormat fmt) {
            if (fmt != null) {
                return getByNormalizedKey(handlersByFormatName, fmt.getFormatName());
            }
            return null;
        }

        /** Get the first handler registered for the given file extension or null if not found.
         * @param fileExt file extension
         * @return first handler registered for the given file extension or null if not found
         */
        public synchronized T getByFileExtension(final String fileExt) {
            return getByNormalizedKey(handlersByFileExtension, fileExt);
        }

        /** Get the handler for the given format or file extension, throwing an exception if one
         * cannot be found. If {@code fmt} is not null, it is used to directly look up the handler
         * and the {@code fileName} argument is ignored. Otherwise, the file extension is extracted
         * from {@code fileName} and used to look up the handler.
         * @param fmt format to look up; if present, {@code fileName} is ignored
         * @param fileName file name to use for the look-up if {@code fmt} is null
         * @return the handler matching the arguments
         * @throws IllegalArgumentException if a handler cannot be found
         */
        public synchronized T requireHandlerByFormatOrFileName(final GeometryFormat fmt, final String fileName) {
            T handler = null;
            if (fmt != null) {
                handler = getByFormat(fmt);

                if (handler == null) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "Failed to find handler for format \"{0}\"", fmt.getFormatName()));
                }
            } else {
                final String fileExt = GeometryIOUtils.getFileExtension(fileName);
                if (fileExt != null && !fileExt.isEmpty()) {
                    handler = getByFileExtension(fileExt);

                    if (handler == null) {
                        throw new IllegalArgumentException(MessageFormat.format(
                               "Failed to find handler for file extension \"{0}\"", fileExt));
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Failed to find handler: no format specified and no file extension available");
                }
            }

            return handler;
        }

        /** Add the handler to the internal format name map.
         * @param fmtName format name
         * @param handler handler to add
         * @throws NullPointerException if {@code fmtName} is null
         */
        private void addToFormat(final String fmtName, final T handler) {
            Objects.requireNonNull(fmtName, FORMAT_NAME_NULL_ERR);
            handlersByFormatName.put(normalizeString(fmtName), handler);
        }

        /** Add the handler to the internal file extension map under each file extension.
         * @param fileExts file extensions to map to the handler
         * @param handler handler to add to the file extension map
         */
        private void addToFileExtensions(final List<String> fileExts, final T handler) {
            if (fileExts != null) {
                for (final String fileExt : fileExts) {
                    addToFileExtension(fileExt, handler);
                }
            }
        }

        /** Add the handler to the internal file extension map.
         * @param fileExt file extension to map to the handler
         * @param handler handler to add to the file extension map
         */
        private void addToFileExtension(final String fileExt, final T handler) {
            if (fileExt != null) {
                handlersByFileExtension.put(normalizeString(fileExt), handler);
            }
        }

        /** Normalize the given key and return its associated value in the map, or null
         * if not found.
         * @param <V> Value type
         * @param map map to search
         * @param key unnormalized map key
         * @return the value associated with the key after normalization, or null if not found
         */
        private static <V> V getByNormalizedKey(final Map<String, V> map, final String key) {
            if (key != null) {
                return map.get(normalizeString(key));
            }
            return null;
        }

        /** Remove all keys that map to {@code value}.
         * @param <V> Value type
         * @param map map to remove keys from
         * @param value value to remove from all entries in the map
         */
        private static <V> void removeValue(final Map<String, V> map, final V value) {
            final Iterator<Map.Entry<String, V>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                if (value.equals(it.next().getValue())) {
                    it.remove();
                }
            }
        }

        /** Normalize the given string for use as a registry identifier.
         * @param str string to normalize
         * @return normalized string
         */
        private static String normalizeString(final String str) {
            return str.toLowerCase(Locale.ROOT);
        }
    }
}
