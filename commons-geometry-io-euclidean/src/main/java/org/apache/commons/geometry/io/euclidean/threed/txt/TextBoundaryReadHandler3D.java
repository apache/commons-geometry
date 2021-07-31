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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;
import org.apache.commons.geometry.io.euclidean.threed.AbstractBoundaryReadHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;

/** {@link org.apache.commons.geometry.io.euclidean.threed.BoundaryReadHandler3D BoundaryReadHandler3D}
 * implementation for the non-standard {@link GeometryFormat3D#TXT TXT}.
 * @see TextFacetDefinitionReader
 */
public class TextBoundaryReadHandler3D extends AbstractBoundaryReadHandler3D {

    /** Default charset for reading text input. */
    private Charset defaultCharset = StandardCharsets.UTF_8;

    /** Get the text input default charset, used if the input does not
     * specify a charset.
     * @return text input default charset
     */
    public Charset getDefaultCharset() {
        return defaultCharset;
    }

    /** Set the text input default charset, used if the input does not
     * specify a charset.
     * @param defaultCharset text input default charset
     */
    public void setDefaultCharset(final Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    /** {@inheritDoc} */
    @Override
    public GeometryFormat getFormat() {
        return GeometryFormat3D.TXT;
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinitionReader facetDefinitionReader(final GeometryInput in) {
        return new TextFacetDefinitionReader(GeometryIOUtils.createBufferedReader(in, defaultCharset));
    }
}
