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

import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.internal.EuclideanUtils;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.utils.AbstractTextFormatWriter;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;

/** Class for writing 3D facet geometry in a simple human-readable text format. The
 * format simply consists of sequences of decimal numbers defining the vertices of each
 * facet, with one facet defined per line. Facet vertices are defined by listing their
 * {@code x}, {@code y}, and {@code z} components in that order. At least 3 vertices are
 * required for each facet but more can be specified. The facet normal is defined implicitly
 * from the facet vertices using the right-hand rule (i.e. vertices are arranged counter-clockwise).
 *
 * <p>Delimiters can be configured for both {@link #getVertexComponentSeparator() vertex components} and
 * {@link #getVertexSeparator() vertices}. This allows a wide range of outputs to be configured, from standard
 * {@link #csvFormat(Writer) CSV format} to formats designed for easy human readability.</p>
 *
 * <p><strong>Examples</strong></p>
 * <p>The examples below demonstrate output from two square facets using different writer
 * configurations.</p>
 *
 * <p><em>Default</em></p>
 * <p>The default writer configuration uses distinct vertex and vertex component separators to make it
 * easier to visually distinguish vertices. Comments are supported and facets are allowed to have
 * any geometrically valid number of vertices. This format is designed for human readability and ease
 * of editing.</p>
 * <pre>
 * # two square facets
 * 0 0 0; 1 0 0; 1 1 0; 0 1 0
 * 0 0 0; 0 1 0; 0 1 1; 0 0 1
 * </pre>
 *
 * <p><em>CSV</em></p>
 * <p>The example below uses a comma as both the vertex and vertex component separators to produce
 * a standard CSV format. The facet vertex count is set to 3 to ensure that each row has the same number
 * of columns and all numbers are written with at least a single fraction digit to ensure proper interpretation
 * as floating point data. Comments are not supported. This configuration is produced by the
 * {@link #csvFormat(Writer)} factory method.</p>
 * <pre>
 * 0.0,0.0,0.0,1.0,0.0,0.0,1.0,1.0,0.0
 * 0.0,0.0,0.0,1.0,1.0,0.0,0.0,1.0,0.0
 * 0.0,0.0,0.0,0.0,1.0,0.0,0.0,1.0,1.0
 * 0.0,0.0,0.0,0.0,1.0,1.0,0.0,0.0,1.0
 * </pre>
 *
 * @see TextFacetDefinitionReader
 */
public class TextFacetDefinitionWriter extends AbstractTextFormatWriter {

    /** Vertex and vertex component separator used in the CSV format. */
    static final String CSV_SEPARATOR = ",";

    /** Number of vertices required per facet in the CSV format. */
    static final int CSV_FACET_VERTEX_COUNT = 3;

    /** Default vertex component separator. */
    static final String DEFAULT_VERTEX_COMPONENT_SEPARATOR = " ";

    /** Default vertex separator. */
    static final String DEFAULT_VERTEX_SEPARATOR = "; ";

    /** Default facet vertex count. */
    static final int DEFAULT_FACET_VERTEX_COUNT = -1;

    /** Default comment token. */
    private static final String DEFAULT_COMMENT_TOKEN = "# ";

    /** String used to separate vertex components, ie, x, y, z values. */
    private String vertexComponentSeparator = DEFAULT_VERTEX_COMPONENT_SEPARATOR;

    /** String used to separate vertices. */
    private String vertexSeparator = DEFAULT_VERTEX_SEPARATOR;

    /** Number of vertices required per facet; will be -1 if disabled. */
    private int facetVertexCount = DEFAULT_FACET_VERTEX_COUNT;

    /** Comment start token; may be null. */
    private String commentToken = DEFAULT_COMMENT_TOKEN;

    /** Construct a new instance that writes facet information to the given writer.
     * @param writer writer to write output to
     */
    public TextFacetDefinitionWriter(final Writer writer) {
        super(writer);
    }

    /** Get the string used to separate vertex components (ie, individual x, y, z values).
     * The default value is {@value #DEFAULT_VERTEX_COMPONENT_SEPARATOR}.
     * @return string used to separate vertex components
     */
    public String getVertexComponentSeparator() {
        return vertexComponentSeparator;
    }

    /** Set the string used to separate vertex components (ie, individual x, y, z values).
     * @param sep string used to separate vertex components
     */
    public void setVertexComponentSeparator(final String sep) {
        this.vertexComponentSeparator = sep;
    }

    /** Get the string used to separate facet vertices. The default value is {@value #DEFAULT_VERTEX_SEPARATOR}.
     * @return string used to separate facet vertices
     */
    public String getVertexSeparator() {
        return vertexSeparator;
    }

    /** Set the string used to separate facet vertices.
     * @param sep string used to separate facet vertices
     */
    public void setVertexSeparator(final String sep) {
        this.vertexSeparator = sep;
    }

    /** Get the number of vertices required per facet or {@code -1} if no specific
     * number is required. The default value is {@value #DEFAULT_FACET_VERTEX_COUNT}.
     * @return the number of vertices required per facet or {@code -1} if any geometrically
     *      valid number is allowed (ie, any number greater than or equal to 3)
     */
    public int getFacetVertexCount() {
        return facetVertexCount;
    }

    /** Set the number of vertices required per facet. This can be used to enforce a consistent
     * format in the output. Set to {@code -1} to allow any geometrically valid number of vertices
     * (ie, any number greater than or equal to 3).
     * @param vertexCount number of vertices required per facet or {@code -1} to allow any number
     * @throws IllegalArgumentException if the argument would produce invalid geometries (ie, is
     *      greater than -1 and less than 3)
     */
    public void setFacetVertexCount(final int vertexCount) {
        if (vertexCount > -1 &&  vertexCount < 3) {
            throw new IllegalArgumentException("Facet vertex count must be less than 0 or greater than 2; was " +
                    vertexCount);
        }

        this.facetVertexCount = Math.max(-1, vertexCount);
    }

    /** Get the string used to begin comment lines in the output.
     * The default value is {@value #DEFAULT_COMMENT_TOKEN}
     * @return the string used to begin comment lines in the output; may be null
     */
    public String getCommentToken() {
        return commentToken;
    }

    /** Set the string used to begin comment lines in the output. Set to null to disable the
     * use of comments.
     * @param commentToken comment token string
     * @throws IllegalArgumentException if the argument is empty or begins with whitespace
     */
    public void setCommentToken(final String commentToken) {
        if (commentToken != null) {
            if (commentToken.isEmpty()) {
                throw new IllegalArgumentException("Comment token cannot be empty");
            } else if (Character.isWhitespace(commentToken.charAt(0))) {
                throw new IllegalArgumentException("Comment token cannot begin with whitespace");
            }

        }

        this.commentToken = commentToken;
    }

    /** Write a comment to the output.
     * @param comment comment string to write
     * @throws IllegalStateException if the configured {@link #getCommentToken() comment token} is null
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeComment(final String comment) {
        if (commentToken == null) {
            throw new IllegalStateException("Cannot write comment: no comment token configured");
        }

        if (comment != null) {
            for (final String line : comment.split("\\R")) {
                write(commentToken + line);
                writeNewLine();
            }
        }
    }

    /** Write a blank line to the output.
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeBlankLine() {
        writeNewLine();
    }

    /** Write all boundaries in the argument to the output. If the
     * {@link #getFacetVertexCount() facet vertex count} has been set to {@code 3}, then each
     * boundary is converted to triangles before being written. Otherwise, the boundaries are
     * written as-is.
     * @param src object providing the boundaries to write
     * @throws IllegalArgumentException if any boundary has infinite size or a
     *      {@link #getFacetVertexCount() facet vertex count} has been configured and a boundary
     *      cannot be represented using the required number of vertices
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void write(final BoundarySource3D src) {
        try (Stream<PlaneConvexSubset> stream = src.boundaryStream()) {
            final Iterator<PlaneConvexSubset> it = stream.iterator();
            while (it.hasNext()) {
                write(it.next());
            }
        }
    }

    /** Write the vertices defining the argument to the output. If the
     * {@link #getFacetVertexCount() facet vertex count} has been set to {@code 3}, then the convex subset
     * is converted to triangles before being written to the output. Otherwise, the argument
     * vertices are written as-is.
     * @param convexSubset convex subset to write
     * @throws IllegalArgumentException if the argument has infinite size or a
     *      {@link #getFacetVertexCount() facet vertex count} has been configured and the number of required
     *      vertices does not match the number present in the argument
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void write(final PlaneConvexSubset convexSubset) {
        if (convexSubset.isInfinite()) {
            throw new IllegalArgumentException("Cannot write infinite convex subset");
        }

        if (facetVertexCount == EuclideanUtils.TRIANGLE_VERTEX_COUNT) {
            // force conversion to triangles
            for (final Triangle3D tri : convexSubset.toTriangles()) {
                write(tri.getVertices());
            }
        } else {
            // write as-is; callers are responsible for making sure that the number of
            // vertices matches the required number for the writer
            write(convexSubset.getVertices());
        }
    }

    /** Write the vertices in the argument to the output.
     * @param facet facet containing the vertices to write
     * @throws IllegalArgumentException if a {@link #getFacetVertexCount() facet vertex count}
     *      has been configured and the number of required vertices does not match the number
     *      present in the argument
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void write(final FacetDefinition facet) {
        write(facet.getVertices());
    }

    /** Write a list of vertices defining a facet as a single line of text to the output. Vertex components
     * (ie, individual x, y, z values) are separated with the configured
     * {@link #getVertexComponentSeparator() vertex component separator} and vertices are separated with the
     * configured {@link #getVertexSeparator() vertex separator}.
     * @param vertices vertices to write
     * @throws IllegalArgumentException if the vertex list contains less than 3 vertices or a
     *      {@link #getFacetVertexCount() facet vertex count} has been configured and the number of required
     *      vertices does not match the number given
     * @throws java.io.UncheckedIOException if an I/O error occurs
     * @see #getVertexComponentSeparator()
     * @see #getVertexSeparator()
     * @see #getFacetVertexCount()
     */
    public void write(final List<Vector3D> vertices) {
        final int size = vertices.size();
        if (size < EuclideanUtils.TRIANGLE_VERTEX_COUNT) {
            throw new IllegalArgumentException("At least " + EuclideanUtils.TRIANGLE_VERTEX_COUNT +
                    " vertices are required per facet; found " + size);
        } else if (facetVertexCount > -1 && size != facetVertexCount) {
            throw new IllegalArgumentException("Writer requires " + facetVertexCount +
                    " vertices per facet; found " + size);
        }

        final Iterator<Vector3D> it = vertices.iterator();

        write(it.next());
        while (it.hasNext()) {
            write(vertexSeparator);
            write(it.next());
        }

        writeNewLine();
    }

    /** Write a single vertex to the output.
     * @param vertex vertex to write
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private void write(final Vector3D vertex) {
        write(vertex.getX());
        write(vertexComponentSeparator);
        write(vertex.getY());
        write(vertexComponentSeparator);
        write(vertex.getZ());
    }

    /** Construct a new instance configured to write CSV output to the given writer.
     * The returned instance has the following configuration:
     * <ul>
     *  <li>Vertex separator and vertex components separator are set to the "," string.</li>
     *  <li>Comments are disabled (i.e., comment token is set to null).</li>
     *  <li>Facet vertex count is set to 3 to ensure a consistent number of columns.</li>
     * </ul>
     * This configuration produces output similar to the following:
     * <pre>
     * 0.0,0.0,0.0,1.0,0.0,0.0,1.0,1.0,0.0
     * 0.0,0.0,0.0,1.0,1.0,0.0,0.0,1.0,0.0
     * </pre>
     *
     * @param writer writer to write output to
     * @return a new facet definition writer configured to produce CSV output
     */
    public static TextFacetDefinitionWriter csvFormat(final Writer writer) {
        final TextFacetDefinitionWriter fdWriter = new TextFacetDefinitionWriter(writer);

        fdWriter.setVertexComponentSeparator(CSV_SEPARATOR);
        fdWriter.setVertexSeparator(CSV_SEPARATOR);
        fdWriter.setFacetVertexCount(CSV_FACET_VERTEX_COUNT);
        fdWriter.setCommentToken(null);

        return fdWriter;
    }
}
