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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;
import org.apache.commons.geometry.io.euclidean.threed.AbstractBoundaryWriteHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;

/** {@link org.apache.commons.geometry.io.euclidean.threed.BoundaryWriteHandler3D BoundaryWriteHandler3D}
 * implementation designed to write simple text data formats using {@link TextFacetDefinitionWriter}. Output is
 * written using the UTF-8 charset by default.
 * @see org.apache.commons.geometry.io.euclidean.threed.BoundaryWriteHandler3D
 * @see TextFacetDefinitionWriter
 */
public class TextBoundaryWriteHandler3D extends AbstractBoundaryWriteHandler3D {

    /** The default line separator value. */
    private static final String DEFAULT_LINE_SEPARATOR = "\n";

    /** Charset used for text output. */
    private Charset charset = TextBoundaryReadHandler3D.DEFAULT_CHARSET;

    /** Line separator string. */
    private String lineSeparator = DEFAULT_LINE_SEPARATOR;

    /** Decimal format pattern. */
    private String decimalFormatPattern;

    /** String used to separate vertex components, ie, x, y, z values. */
    private String vertexComponentSeparator = TextFacetDefinitionWriter.DEFAULT_VERTEX_COMPONENT_SEPARATOR;

    /** String used to separate vertices. */
    private String vertexSeparator = TextFacetDefinitionWriter.DEFAULT_VERTEX_SEPARATOR;

    /** Number of vertices required per facet; will be -1 if disabled. */
    private int facetVertexCount = TextFacetDefinitionWriter.DEFAULT_FACET_VERTEX_COUNT;

    /** Get the text output charset.
     * @return text output charset
     */
    public Charset getCharset() {
        return charset;
    }

    /** Set the text output charset.
     * @param charset text output charset
     */
    public void setCharset(final Charset charset) {
        this.charset = charset;
    }

    /** Get the line separator. This value defaults to {@value #DEFAULT_LINE_SEPARATOR}.
     * @return the current line separator
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /** Set the line separator.
     * @param lineSeparator the line separator to use
     */
    public void setLineSeparator(final String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    /** Get the format string used to construct {@link DecimalFormat} instances for
     * formatting decimal output. If null, default instances are used.
     * @return format string used to construct {@link DecimalFormat} instances; may be null
     */
    public String getDecimalFormatPattern() {
        return decimalFormatPattern;
    }

    /** Set the format string used to construct {@link DecimalFormat} instances for
     * formatting decimal output. If set to null, default instances are used.
     * @param decimalFormatPattern format pattern; may be null
     */
    public void setDecimalFormatPattern(final String decimalFormatPattern) {
        this.decimalFormatPattern = decimalFormatPattern;
    }

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
    public void write(final Stream<? extends PlaneConvexSubset> boundaries, final OutputStream out)
            throws IOException {
        try (TextFacetDefinitionWriter writer = getFacetDefinitionWriter(out)) {
            final Iterator<? extends PlaneConvexSubset> it = boundaries.iterator();
            while (it.hasNext()) {
                writer.write(it.next());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Stream<? extends FacetDefinition> facets, final OutputStream out)
            throws IOException {
        try (TextFacetDefinitionWriter writer = getFacetDefinitionWriter(out)) {
            final Iterator<? extends FacetDefinition> it = facets.iterator();
            while (it.hasNext()) {
                writer.write(it.next());
            }
        }
    }

    /** Get a configured {@link TextFacetDefinitionWriter} for writing output.
     * @param out output stream to write to
     * @return a new, configured text format writer
     */
    private TextFacetDefinitionWriter getFacetDefinitionWriter(final OutputStream out) {
        final TextFacetDefinitionWriter facetWriter = new TextFacetDefinitionWriter(
                GeometryIOUtils.createCloseShieldWriter(out, charset));

        facetWriter.setLineSeparator(lineSeparator);

        if (decimalFormatPattern != null) {
            facetWriter.setDecimalFormat(new DecimalFormat(decimalFormatPattern));
        }

        facetWriter.setVertexComponentSeparator(vertexComponentSeparator);
        facetWriter.setVertexSeparator(vertexSeparator);
        facetWriter.setFacetVertexCount(facetVertexCount);

        return facetWriter;
    }

    /** Create a new {@link TextBoundaryWriteHandler3D} configured to output CSV content.
     * @return a new {@link TextBoundaryWriteHandler3D} configured to output CSV content
     * @see TextFacetDefinitionWriter#csvFormat(java.io.Writer)
     */
    public static final TextBoundaryWriteHandler3D csvFormat() {
        final TextBoundaryWriteHandler3D handler = new TextBoundaryWriteHandler3D();
        handler.setVertexComponentSeparator(TextFacetDefinitionWriter.CSV_SEPARATOR);
        handler.setVertexSeparator(TextFacetDefinitionWriter.CSV_SEPARATOR);
        handler.setDecimalFormatPattern(TextFacetDefinitionWriter.CSV_DECIMAL_PATTERN);
        handler.setFacetVertexCount(TextFacetDefinitionWriter.CSV_FACET_VERTEX_COUNT);

        return handler;
    }
}
