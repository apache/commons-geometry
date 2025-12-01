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
package org.apache.commons.geometry.io.euclidean.threed.obj;

import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.input.GeometryInput;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;
import org.apache.commons.geometry.io.euclidean.threed.AbstractBoundaryReadHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;
import org.apache.commons.numbers.core.Precision;

/** {@link org.apache.commons.geometry.io.euclidean.threed.BoundaryReadHandler3D BoundaryReadHandler3D}
 * implementation for reading OBJ data. Input is read using the UTF-8 charset by default.
 */
public class ObjBoundaryReadHandler3D extends AbstractBoundaryReadHandler3D {

    /** Charset for reading text input. */
    private Charset defaultCharset = StandardCharsets.UTF_8;

    /** Create an instance. */
    public ObjBoundaryReadHandler3D() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public GeometryFormat getFormat() {
        return GeometryFormat3D.OBJ;
    }

    /** Get the text input default charset, used if the input does not
     * specify a charset.
     * @return text input default charset
     */
    public Charset getDefaultCharset() {
        return defaultCharset;
    }

    /** Set the text input default charset, used if the input does not
     * specify a charset.
     * @param charset text input default charset
     */
    public void setDefaultCharset(final Charset charset) {
        this.defaultCharset = charset;
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinitionReader facetDefinitionReader(final GeometryInput in) {
        return new ObjFacetDefinitionReader(createReader(in));
    }

    /** {@inheritDoc} */
    @Override
    public TriangleMesh readTriangleMesh(final GeometryInput in, final Precision.DoubleEquivalence precision) {
        try (ObjTriangleMeshReader meshReader = new ObjTriangleMeshReader(createReader(in), precision)) {
            return meshReader.readTriangleMesh();
        }
    }

    /** Create a {@link Reader} for reading character data from the given input.
     * @param in input to read from
     * @return reader instance
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private Reader createReader(final GeometryInput in) {
        return GeometryIOUtils.createBufferedReader(in, defaultCharset);
    }
}
