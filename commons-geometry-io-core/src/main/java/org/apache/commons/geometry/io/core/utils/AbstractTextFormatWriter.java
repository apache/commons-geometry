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
package org.apache.commons.geometry.io.core.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.function.DoubleFunction;

/** Base type for classes that write text-based data formats. This class
 * provides a number of common configuration options and utility methods.
 */
public abstract class AbstractTextFormatWriter implements Closeable {

    /** The default line separator value. */
    private static final String DEFAULT_LINE_SEPARATOR = "\n";

    /** Underlying writer instance. */
    private final Writer writer;

    /** Line separator string. */
    private String lineSeparator = DEFAULT_LINE_SEPARATOR;

    /** Double format function. */
    private DoubleFunction<String> doubleFormat;

    /** Construct a new instance that writes content to the given writer.
     * @param writer writer instance
     */
    protected AbstractTextFormatWriter(final Writer writer) {
        this(writer, Double::toString);
    }

    /** Construct a new instance that writes content to the given writer and uses the
     * decimal format instance for creating floating-point string representations.
     * @param writer writer instance
     * @param doubleFormat double format function
     */
    protected AbstractTextFormatWriter(final Writer writer, final DoubleFunction<String> doubleFormat) {
        this.writer = writer;
        this.doubleFormat = doubleFormat;
    }

    /** Get the current line separator. This value defaults to {@value #DEFAULT_LINE_SEPARATOR}.
     * @return the current line separator
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /** Set the line separator.
     * @param lineSeparator the line separator to use
     */
    public void setLineSeparator(final String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    /** Get the function used to format floating point output.
     * @return the double format function
     */
    public DoubleFunction<String> getDoubleFormat() {
        return doubleFormat;
    }

    /** Set the function used to format floating point output.
     * @param doubleFormat double format function
     */
    public void setDoubleFormat(final DoubleFunction<String> doubleFormat) {
        this.doubleFormat = doubleFormat;
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        writer.close();
    }

    /** Get the underlying writer instance.
     * @return writer instance
     */
    protected Writer getWriter() {
        return writer;
    }

    /** Write a double value formatted using the configured decimal format function.
     * @param d value to write
     * @throws IOException if an I/O error occurs
     */
    protected void write(final double d) throws IOException {
        write(doubleFormat.apply(d));
    }

    /** Write an integer value.
     * @param n value to write
     * @throws IOException if an I/O error occurs
     */
    protected void write(final int n) throws IOException {
        write(String.valueOf(n));
    }

    /** Write a char value.
     * @param c character to write
     * @throws IOException if an I/O error occurs
     */
    protected void write(final char c) throws IOException {
        writer.write(c);
    }

    /** Write a string.
     * @param str string to write
     * @throws IOException if an I/O error occurs
     */
    protected void write(final String str) throws IOException {
        writer.write(str);
    }

    /** Write the configured line separator to the output.
     * @throws IOException if an I/O error occurs
     */
    protected void writeNewLine() throws IOException {
        write(lineSeparator);
    }
}
