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
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.geometry.io.core.AbstractGeometryIOMetadata;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;

/** {@link GeometryInput} implementation for reading content from a URL.
 */
public class UrlGeometryInput extends AbstractGeometryIOMetadata
    implements GeometryInput {

    /** Input URL. */
    private final URL url;

    /** Construct a new instance for reading from the given URL.
     * @param url input url
     */
    public UrlGeometryInput(final URL url) {
        this(url, null);
    }

    /** Construct a new instance for reading from the given URL with the
     * specified charset.
     * @param url input URL
     * @param charset charset to use when reading content
     */
    public UrlGeometryInput(final URL url, final Charset charset) {
        super(GeometryIOUtils.getFileName(url), charset);

        this.url = url;
    }

    /** Get the input URL.
     * @return input URL
     */
    public URL getUrl() {
        return url;
    }

    /** {@inheritDoc}
     *
     * <p>The returned input stream is buffered.</p>
     */
    @Override
    public InputStream getInputStream() {
        return GeometryIOUtils.getUnchecked(() -> new BufferedInputStream(url.openStream()));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[url= ")
            .append(getUrl())
            .append(']');

        return sb.toString();
    }
}
