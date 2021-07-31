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
package org.apache.commons.geometry.io.core.output;

import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.geometry.io.core.AbstractGeometryIOMetadata;

/** {@link GeometryOutput} implementation that wraps an {@link OutputStream}.
 */
public class StreamGeometryOutput extends AbstractGeometryIOMetadata
    implements GeometryOutput {

    /** Output stream. */
    private final OutputStream out;

    /** Construct a new instance that writes to the given output stream with
     * no configured file name or charset.
     * @param out output stream
     */
    public StreamGeometryOutput(final OutputStream out) {
        this(out, null, null);
    }

    /** Construct a new instance that writes to the given output stream with the
     * configured file name but no charset.
     * @param out output stream
     * @param fileName output file name; may be null
     */
    public StreamGeometryOutput(final OutputStream out, final String fileName) {
        this(out, fileName, null);
    }

    /** Construct a new instance that writes to the given output stream with the configured
     * file name and charset.
     * @param out output stream
     * @param fileName output file name; may be null
     * @param charset output charset; may be null
     */
    public StreamGeometryOutput(final OutputStream out, final String fileName, final Charset charset) {
        super(fileName, charset);

        this.out = out;
    }

    /** {@inheritDoc} */
    @Override
    public OutputStream getOutputStream() {
        return out;
    }
}
