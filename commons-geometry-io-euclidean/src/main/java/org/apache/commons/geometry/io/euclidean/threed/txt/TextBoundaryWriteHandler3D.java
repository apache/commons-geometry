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

import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.output.GeometryOutput;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;

/** {@link org.apache.commons.geometry.io.euclidean.threed.BoundaryWriteHandler3D BoundaryWriteHandler3D}
 * implementation for the non-standard {@link GeometryFormat3D#TXT TXT} format. Output is
 * written using the UTF-8 charset by default.
 * @see org.apache.commons.geometry.io.euclidean.threed.BoundaryWriteHandler3D
 * @see TextFacetDefinitionWriter
 */
public class TextBoundaryWriteHandler3D extends AbstractTextBoundaryWriteHandler3D {

    /** String used to separate vertex components, ie, x, y, z values. */
    private String vertexComponentSeparator = TextFacetDefinitionWriter.DEFAULT_VERTEX_COMPONENT_SEPARATOR;

    /** String used to separate vertices. */
    private String vertexSeparator = TextFacetDefinitionWriter.DEFAULT_VERTEX_SEPARATOR;

    /** Number of vertices required per facet; will be -1 if disabled. */
    private int facetVertexCount = TextFacetDefinitionWriter.DEFAULT_FACET_VERTEX_COUNT;

    /** Get the string used to separate vertex components (ie, individual x, y, z values).
     * @return string used to separate vertex components
     * @see TextFacetDefinitionWriter#getVertexComponentSeparator()
     */
    public String getVertexComponentSeparator() {
        return vertexComponentSeparator;
    }

    /** Set the string used to separate vertex components (ie, individual x, y, z values).
     * @param sep string used to separate vertex components
     * @see TextFacetDefinitionWriter#setVertexComponentSeparator(String)
     */
    public void setVertexComponentSeparator(final String sep) {
        this.vertexComponentSeparator = sep;
    }

    /** Get the string used to separate facet vertices.
     * @return string used to separate facet vertices
     * @see TextFacetDefinitionWriter#getVertexSeparator()
     */
    public String getVertexSeparator() {
        return vertexSeparator;
    }

    /** Set the string used to separate facet vertices.
     * @param sep string used to separate facet vertices
     * @see TextFacetDefinitionWriter#setVertexSeparator(String)
     */
    public void setVertexSeparator(final String sep) {
        this.vertexSeparator = sep;
    }

    /** Get the number of vertices required per facet or {@code -1} if no specific
     * number is required.
     * @return the number of vertices required per facet or {@code -1} if any geometricallly
     *      valid number is allowed (ie, any number greater than or equal to 3)
     * @see TextFacetDefinitionWriter#getFacetVertexCount()
     */
    public int getFacetVertexCount() {
        return facetVertexCount;
    }

    /** Set the number of vertices required per facet. This can be used to enforce a consistent
     * format in the output. Set to {@code -1} to allow any geometrically valid number of vertices
     * (ie, any number greater than or equal to 3).
     * @param vertexCount number of vertices required per facet or {@code -1} to allow any number
     * @see TextFacetDefinitionWriter#setFacetVertexCount(int)
     */
    public void setFacetVertexCount(final int vertexCount) {
        this.facetVertexCount = vertexCount;
    }

    /** {@inheritDoc} */
    @Override
    public GeometryFormat getFormat() {
        return GeometryFormat3D.TXT;
    }

    /** {@inheritDoc} */
    @Override
    protected TextFacetDefinitionWriter getFacetDefinitionWriter(final GeometryOutput out) {
        final TextFacetDefinitionWriter facetWriter = super.getFacetDefinitionWriter(out);

        facetWriter.setVertexComponentSeparator(vertexComponentSeparator);
        facetWriter.setVertexSeparator(vertexSeparator);
        facetWriter.setFacetVertexCount(facetVertexCount);

        return facetWriter;
    }
}
