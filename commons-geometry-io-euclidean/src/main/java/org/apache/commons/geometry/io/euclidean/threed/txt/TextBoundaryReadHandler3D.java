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
package org.apache.commons.geometry.io.euclidean.threed.txt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.geometry.io.euclidean.threed.AbstractBoundaryReadHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;

/** {@link org.apache.commons.geometry.io.euclidean.threed.BoundaryReadHandler3D BoundaryReadHandler3D}
 * implementation for reading simple text and CSV data. Input is read using the UTF-8 charset by default.
 * @see TextFacetDefinitionReader
 */
public class TextBoundaryReadHandler3D extends AbstractBoundaryReadHandler3D {

    /** Charset for use with txt and csv files. */
    static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /** Charset for reading text input. */
    private Charset charset = DEFAULT_CHARSET;

    /** Get the text input charset.
     * @return text input charset
     */
    public Charset getCharset() {
        return charset;
    }

    /** Set the text input charset.
     * @param charset text input charset
     */
    public void setCharset(final Charset charset) {
        this.charset = charset;
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinitionReader facetDefinitionReader(final InputStream in) throws IOException {
        return new TextFacetDefinitionReader(new BufferedReader(new InputStreamReader(in, charset)));
    }
}
