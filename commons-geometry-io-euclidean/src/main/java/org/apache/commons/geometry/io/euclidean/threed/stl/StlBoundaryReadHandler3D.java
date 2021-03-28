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
package org.apache.commons.geometry.io.euclidean.threed.stl;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;
import org.apache.commons.geometry.io.euclidean.threed.AbstractBoundaryReadHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;

/** {@link org.apache.commons.geometry.io.euclidean.threed.BoundaryReadHandler3D BoundaryReadHandler3D}
 * implementation for reading STL data. Text input is read using the UTF-8 charset by default.
 */
public class StlBoundaryReadHandler3D extends AbstractBoundaryReadHandler3D {

    /** Default charset for reading text input. */
    private Charset defaultCharset = StlConstants.DEFAULT_CHARSET;

    /** {@inheritDoc} */
    @Override
    public GeometryFormat getFormat() {
        return GeometryFormat3D.STL;
    }

    /** Get the input default charset, used if text input does not
     * specify a charset.
     * @return text input default charset
     */
    public Charset getDefaultCharset() {
        return defaultCharset;
    }

    /** Set the input default charset, used if text input does not
     * specify a charset.
     * @param charset text input default charset
     */
    public void setDefaultCharset(final Charset charset) {
        this.defaultCharset = charset;
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinitionReader facetDefinitionReader(final GeometryInput in) throws IOException {
        final Charset inputCharset = in.getCharset() != null ?
                in.getCharset() :
                defaultCharset;

        return GeometryIOUtils.tryApplyCloseable(
                inputStream -> StlFacetDefinitionReaders.create(inputStream, inputCharset),
                in::getInputStream);
    }
}
