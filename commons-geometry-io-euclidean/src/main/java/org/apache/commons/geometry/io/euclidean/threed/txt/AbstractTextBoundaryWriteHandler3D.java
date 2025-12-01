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
package org.apache.commons.geometry.io.euclidean.threed.txt;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.function.DoubleFunction;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;
import org.apache.commons.geometry.io.core.output.GeometryOutput;
import org.apache.commons.geometry.io.euclidean.threed.AbstractBoundaryWriteHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;

/** Abstract based class for write handlers that output text formats produced
 * by {@link TextFacetDefinitionWriter}.
 * @see TextFacetDefinitionWriter
 */
public abstract class AbstractTextBoundaryWriteHandler3D extends AbstractBoundaryWriteHandler3D {

    /** The default line separator value. */
    private static final String DEFAULT_LINE_SEPARATOR = "\n";

    /** Default charset used for text output. */
    private Charset defaultCharset = StandardCharsets.UTF_8;

    /** Line separator string. */
    private String lineSeparator = DEFAULT_LINE_SEPARATOR;

    /** Double format function. */
    private DoubleFunction<String> doubleFormat = Double::toString;

    /** Create an instance. */
    public AbstractTextBoundaryWriteHandler3D() {
        // Do nothing
    }

    /** Get the text output default charset, used if the output does not
     * specify a charset.
     * @return text output default charset
     */
    public Charset getDefaultCharset() {
        return defaultCharset;
    }

    /** Set the text output default charset, used if the output does not
     * specify a charset.
     * @param defaultCharset text output default charset
     */
    public void setDefaultCharset(final Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
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

    /** Get the double format function used to convert double values
     * to strings.
     * @return double format function
     */
    public DoubleFunction<String> getDoubleFormat() {
        return doubleFormat;
    }

    /** Set the double format function used to convert double values
     * to strings. The given function must be thread-safe if this handler
     * is to be used in a multi-threaded context.
     * @param doubleFormat double format function
     */
    public void setDoubleFormat(final DoubleFunction<String> doubleFormat) {
        this.doubleFormat = doubleFormat;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final Stream<? extends PlaneConvexSubset> boundaries, final GeometryOutput out) {
        try (TextFacetDefinitionWriter writer = getFacetDefinitionWriter(out)) {
            final Iterator<? extends PlaneConvexSubset> it = boundaries.iterator();
            while (it.hasNext()) {
                writer.write(it.next());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Stream<? extends FacetDefinition> facets, final GeometryOutput out) {
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
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    protected TextFacetDefinitionWriter getFacetDefinitionWriter(final GeometryOutput out) {
        final TextFacetDefinitionWriter facetWriter =
                new TextFacetDefinitionWriter(GeometryIOUtils.createBufferedWriter(out, defaultCharset));

        facetWriter.setLineSeparator(lineSeparator);
        facetWriter.setDoubleFormat(doubleFormat);

        return facetWriter;
    }
}
