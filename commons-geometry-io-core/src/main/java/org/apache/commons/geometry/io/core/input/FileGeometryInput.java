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
package org.apache.commons.geometry.io.core.input;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;

/** {@link GeometryInput} implementation for reading content from a file.
 */
public class FileGeometryInput extends AbstractGeometryInput {

    /** Input file. */
    private final Path file;

    /** Construct a new instance for reading from the given file.
     * @param file input file
     */
    public FileGeometryInput(final Path file) {
        this(file, null);
    }

    /** Construct a new instance for reading from the given file with the
     * specific charset.
     * @param file input file
     * @param charset charset to use when reading from the input file
     */
    public FileGeometryInput(final Path file, final Charset charset) {
        super(GeometryIOUtils.getFileName(file), charset);

        this.file = file;
    }

    /** Get the input file.
     * @return input file
     */
    public Path getFile() {
        return file;
    }

    /** {@inheritDoc}
     *
     * <p>The returned input stream is buffered.</p>
     */
    @Override
    public InputStream getInputStream() {
        return GeometryIOUtils.getUnchecked(() -> new BufferedInputStream(Files.newInputStream(file)));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[file= ")
            .append(getFile())
            .append(']');

        return sb.toString();
    }
}
