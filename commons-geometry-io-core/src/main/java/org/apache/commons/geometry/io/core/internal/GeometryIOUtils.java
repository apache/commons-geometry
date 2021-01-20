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
package org.apache.commons.geometry.io.core.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.stream.Stream;

/** Class containing utility methods for IO operations.
 */
public final class GeometryIOUtils {

    /** Utility class; no instantiation. */
    private GeometryIOUtils() {}

    /** Get the part of the file name after the last dot.
     * @param fileName file name to get the extension for
     * @return the extension of the file name, the empty string if no extension is found, or
     *      null if the argument is null
     */
    public static String getFileExtension(final String fileName) {
        if (fileName != null) {
            final int idx = fileName.lastIndexOf('.');
            if (idx > -1) {
                return fileName.substring(idx + 1);
            }

            return "";
        }

        return null;
    }

    /** Create an unchecked exception from the given checked exception. The message of the
     * returned exception contains the original exception's type and message.
     * @param exc exception to wrap in an unchecked exception
     * @return the unchecked exception
     */
    public static UncheckedIOException createUnchecked(final IOException exc) {
        final String msg = exc.getClass().getSimpleName() + ": " + exc.getMessage();
        return new UncheckedIOException(msg, exc);
    }

    /** Return an input stream that delegates all calls to the argument but does not
     * close the argument when {@link InputStream#close() close()} is called.
     * @param in input stream to wrap
     * @return an input stream that delegates all calls to {@code in} except for {@code close()}
     */
    public static InputStream createCloseShieldInputStream(final InputStream in) {
        return new CloseShieldInputStream(in);
    }

    /** Return an output stream that delegates all calls to the argument but does not close
     * the argument when {@link OutputStream#close() close()} is called.
     * @param out output stream to wrap
     * @return an output stream that delegates all calls to {@code out} except for {@code close()}
     */
    public static OutputStream createCloseShieldOutputStream(final OutputStream out) {
        return new CloseShieldOutputStream(out);
    }

    /** Return a buffered reader that reads characters of the given charset from {@code in} but
     * does not close {@code in} when {@link Reader#close() close()} is called.
     * @param in input stream to read from
     * @param charset reader charset
     * @return a buffered reader that reads characters from {@code in} but does not close it when
     *      {@code close()} is called
     */
    public static Reader createCloseShieldReader(final InputStream in, final Charset charset) {
        final InputStream shielded = createCloseShieldInputStream(in);
        return new BufferedReader(new InputStreamReader(shielded, charset));
    }

    /** Return a buffered writer that writer characters of the given charset to {@code out} but
     * does not close {@code out} when {@link Writer#close() close} is called.
     * @param out output stream to write to
     * @param charset writer charset
     * @return a buffered writer that writes characters to {@code out} but does not close it
     *      when {@code close()} is called
     */
    public static Writer createCloseShieldWriter(final OutputStream out, final Charset charset) {
        final OutputStream shielded = createCloseShieldOutputStream(out);
        return new BufferedWriter(new OutputStreamWriter(shielded, charset));
    }

    /** Pass a supplied {@link Closeable} instance to {@code function} and return the result.
     * The {@code Closeable} instance returned by the supplier is closed if function execution
     * fails, otherwise the instance is <em>not</em> closed.
     * @param <T> Return type
     * @param <C> Closeable type
     * @param function function called with the supplied Closeable instance
     * @param closeableSupplier supplier used to obtain a Closeable instance
     * @return result of calling {@code function} with a supplied Closeable instance
     * @throws IOException if an I/O error occurs
     */
    public static <T, C extends Closeable> T tryApplyCloseable(final IOFunction<C, T> function,
            final IOSupplier<? extends C> closeableSupplier) throws IOException {
        C closeable = null;
        try {
            closeable = closeableSupplier.get();
            return function.apply(closeable);
        } catch (IOException | RuntimeException exc) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException suppressed) {
                    exc.addSuppressed(suppressed);
                }
            }

            throw exc;
        }
    }

    /** Create a stream associated with an input stream. The input stream is closed when the
     * stream is closed and also closed if stream creation fails. Any {@link IOException} thrown
     * when the input stream is closed after the return of this method are wrapped with {@link UncheckedIOException}.
     * @param <T> Stream element type
     * @param <I> Input stream type
     * @param streamFunction function accepting an input stream and returning a stream
     * @param inputStreamSupplier supplier used to obtain the input stream
     * @return stream associated with the input stream return by the supplier
     * @throws IOException if an I/O error occurs during input stream and stream creation
     */
    public static <T, I extends InputStream> Stream<T> createCloseableStream(
            final IOFunction<I, Stream<T>> streamFunction, final IOSupplier<? extends I> inputStreamSupplier)
                throws IOException {
        return tryApplyCloseable(
                in -> streamFunction.apply(in).onClose(closeAsUncheckedRunnable(in)),
                inputStreamSupplier);
    }

    /** Return a {@link Runnable} that calls {@link Closeable#getClass() close()} on the argument,
     * wrapping any {@link IOException} with {@link UncheckedIOException}.
     * @param closeable instance to be closed
     * @return runnable that calls {@code close()) on the argument
     */
    private static Runnable closeAsUncheckedRunnable(final Closeable closeable) {
        return () -> {
            try {
                closeable.close();
            } catch (IOException exc) {
                throw createUnchecked(exc);
            }
        };
    }

    /** Internal class used to wrap an input stream and prevent it from being closed
     * when {@link #close()} is invoked on the wrapper instance.
     */
    private static final class CloseShieldInputStream extends FilterInputStream {

        /** Construct a new instance wrapping the argument.
         * @param in input stream to wrap
         */
        CloseShieldInputStream(final InputStream in) {
            super(in);
        }

        /** Do nothing. The underlying stream is <em>not</em> closed.
         */
        @Override
        public void close() throws IOException {
            // do nothing
        }
    }

    /** Internal class used to wrap an output stream and prevent it from being closed
     * when {@link #close()} is invoked on the wrapper instance.
     */
    private static final class CloseShieldOutputStream extends FilterOutputStream {

        /** Construct a new instance wrapping the argument.
         * @param out output stream to wrap
         */
        CloseShieldOutputStream(final OutputStream out) {
            super(out);
        }

        /** Do nothing. The underlying stream is <em>not</em> closed.
         */
        @Override
        public void close() throws IOException {
            // do nothing
        }
    }
}
