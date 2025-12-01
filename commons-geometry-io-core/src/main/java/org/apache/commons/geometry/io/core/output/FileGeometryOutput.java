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
package org.apache.commons.geometry.io.core.output;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.geometry.io.core.AbstractGeometryIOMetadata;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;

/** {@link GeometryOutput} implementation for writing content to a file.
 */
public class FileGeometryOutput extends AbstractGeometryIOMetadata
    implements GeometryOutput {

    /** File to write to. */
    private final Path file;

    /** Construct a new instance with the given file and no charset.
     * @param file output file
     */
    public FileGeometryOutput(final Path file) {
        this(file, null);
    }

    /** Construct a new instance with the given file and charset.
     * @param file output file
     * @param charset file charset
     */
    public FileGeometryOutput(final Path file, final Charset charset) {
        super(GeometryIOUtils.getFileName(file), charset);

        this.file = file;
    }

    /** Get the output file.
     * @return output file
     */
    public Path getFile() {
        return file;
    }

    /** {@inheritDoc}
     *
     * <p>The returned output stream is buffered.</p>
     */
    @Override
    public OutputStream getOutputStream() {
        return GeometryIOUtils.getUnchecked(() -> new BufferedOutputStream(Files.newOutputStream(file)));
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
