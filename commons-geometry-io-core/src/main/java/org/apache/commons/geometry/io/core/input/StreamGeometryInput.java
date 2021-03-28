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

import java.io.InputStream;
import java.nio.charset.Charset;

/** {@link GeometryInput} implementation that wraps an {@link InputStream}.
 */
public class StreamGeometryInput extends AbstractGeometryInput {

    /** Input stream. */
    private final InputStream in;

    /** Construct a new instance that reads from the given input stream with
     * no configured file name or charset.
     * @param in input stream
     */
    public StreamGeometryInput(final InputStream in) {
        this(in, null, null);
    }

    /** Construct a new instance that reads from the given input stream with the
     * configured file name but no charset.
     * @param in input stream
     * @param fileName input file name; may be null
     */
    public StreamGeometryInput(final InputStream in, final String fileName) {
        this(in, fileName, null);
    }

    /** Construct a new instance that reads from the given input stream with the configured
     * file name and charset.
     * @param in input stream
     * @param fileName input file name; may be null
     * @param charset input charset; may be null
     */
    public StreamGeometryInput(final InputStream in, final String fileName, final Charset charset) {
        super(fileName, charset);

        this.in = in;
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getInputStream() {
        return in;
    }
}
