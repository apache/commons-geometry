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

import java.nio.charset.Charset;

/** Abstract base class for {@link GeometryIOMetadata} implementations.
 */
public class AbstractGeometryIOMetadata implements GeometryIOMetadata {

    /** File name; may be null. */
    private final String fileName;

    /** Charset; may be null. */
    private final Charset charset;

    /** Construct a new instance with the given file name and charset.
     * @param fileName file name; may be null
     * @param charset charset; may be null
     */
    protected AbstractGeometryIOMetadata(final String fileName, final Charset charset) {
        this.fileName = fileName;
        this.charset = charset;
    }

    /** {@inheritDoc} */
    @Override
    public String getFileName() {
        return fileName;
    }

    /** {@inheritDoc} */
    @Override
    public Charset getCharset() {
        return charset;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[fileName= ")
            .append(getFileName())
            .append(']');

        return sb.toString();
    }
}
