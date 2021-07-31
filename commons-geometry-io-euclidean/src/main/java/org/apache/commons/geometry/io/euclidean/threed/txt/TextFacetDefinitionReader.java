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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;
import org.apache.commons.geometry.io.core.internal.SimpleTextParser;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;

/** Facet definition reader implementation that reads an extremely simple
 * text format. The format simply consists of sequences of decimal numbers
 * defining the vertices of each facet, with one facet defined per line.
 * Facet vertices are defined by listing their {@code x}, {@code y}, and {@code z}
 * components in that order. The format can be described as follows:
 * <p>
 * <code>
 *      p1<sub>x</sub> p1<sub>y</sub> p1<sub>z</sub> p2<sub>x</sub> p2<sub>y</sub> p2<sub>z</sub> p3<sub>x</sub> p3<sub>y</sub> p3<sub>z</sub> ...
 * </code>
 * </p>
 * <p>where the <em>p1</em> elements contain the coordinates of the first facet vertex,
 * <em>p2</em> those of the second, and so on. At least 3 vertices are required for each
 * facet but more can be specified as long as all {@code x, y, z} components are provided
 * for each vertex. The facet normal is defined implicitly from the facet vertices using
 * the right-hand rule (i.e. vertices are arranged counter-clockwise).</p>
 *
 * <p><strong>Delimiters</strong></p>
 * <p>Vertex coordinate values may be separated by any character that is
 * not a digit, alphabetic, '-' (minus), or '+' (plus). The character does
 * not need to be consistent between (or even within) lines and does not
 * need to be configured in the reader. This design provides configuration-free
 * support for common formats such as CSV as well as other formats designed
 * for human readability.</p>
 *
 * <p><strong>Comments</strong></p>
 * <p>Comments are supported through use of the {@link #getCommentToken() comment token}
 * property. Characters from the comment token through the end of the current line are
 * discarded. Setting the comment token to null or the empty string disables comment parsing.
 * The default comment token is {@value #DEFAULT_COMMENT_TOKEN}</p>
 *
 * <p><strong>Examples</strong></p>
 * <p>The following examples demonstrate the definition of two facets,
 * one with 3 vertices and one with 4 vertices, in different formats.</p>
 * <p><em>CSV</em></p>
 * <pre>
 *  0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0
 *  1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0
 * </pre>
 * <p><em>Whitespace and semicolons</em></p>
 * <pre>
 *  # line comment
 *  0 0 0; 1 0 0; 1 1 0 # 3 vertices
 *  1 0 0; 1 1 0; 1 1 1; 1 0 1 # 4 vertices
 * </pre>
 *
 * @see TextFacetDefinitionWriter
 */
public class TextFacetDefinitionReader implements FacetDefinitionReader {

    /** Default comment token string. */
    public static final String DEFAULT_COMMENT_TOKEN = "#";

    /** Reader for accessing the character stream. */
    private final Reader reader;

    /** Parser used to parse text content. */
    private final SimpleTextParser parser;

    /** Comment token string; may be null. */
    private String commentToken;

    /** True if the instance has a non-null, non-empty comment token. */
    private boolean hasCommentToken;

    /** First character of the comment token. */
    private int commentStartChar;

    /** Construct a new instance that reads characters from the argument and uses
     * the default comment token value of {@value TextFacetDefinitionReader#DEFAULT_COMMENT_TOKEN}.
     * @param reader reader to read characters from
     */
    public TextFacetDefinitionReader(final Reader reader) {
        this(reader, DEFAULT_COMMENT_TOKEN);
    }

    /** Construct a new instance with the given reader and comment token.
     * @param reader reader to read characters from
     * @param commentToken comment token string; set to null to disable comment parsing
     * @throws IllegalArgumentException if {@code commentToken} is non-null and contains whitespace
     */
    public TextFacetDefinitionReader(final Reader reader, final String commentToken) {
        this.reader = reader;
        this.parser = new SimpleTextParser(reader);

        setCommentTokenInternal(commentToken);
    }

    /** Get the comment token string. If not null or empty, any characters from
     * this token to the end of the current line are discarded during parsing.
     * @return comment token string; may be null
     */
    public String getCommentToken() {
        return commentToken;
    }

    /** Set the comment token string. If not null or empty, any characters from this
     * token to the end of the current line are discarded during parsing. Set to null
     * or the empty string to disable comment parsing. Comment tokens may not contain
     * whitespace.
     * @param commentToken token to set
     * @throws IllegalArgumentException if the argument is non-null and contains whitespace
     */
    public void setCommentToken(final String commentToken) {
        setCommentTokenInternal(commentToken);
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinition readFacet() {
        discardNonDataLines();
        if (parser.hasMoreCharacters()) {
            try {
                return readFacetInternal();
            } finally {
                // advance to the next line even if parsing failed for the
                // current line
                parser.discardLine();
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        GeometryIOUtils.closeUnchecked(reader);
    }

    /** Internal method to read a facet definition starting from the current parser
     * position. Empty lines (including lines containing only comments) are discarded.
     * @return facet definition or null if the end of input is reached
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private FacetDefinition readFacetInternal() {
        final Vector3D p1 = readVector();
        discardNonData();
        final Vector3D p2 = readVector();
        discardNonData();
        final Vector3D p3 = readVector();

        final List<Vector3D> vertices;

        discardNonData();
        if (parser.hasMoreCharactersOnLine()) {
            vertices = new ArrayList<>();
            vertices.add(p1);
            vertices.add(p2);
            vertices.add(p3);

            do {
                vertices.add(readVector());
                discardNonData();
            } while (parser.hasMoreCharactersOnLine());
        } else {
            vertices = Arrays.asList(p1, p2, p3);
        }

        return new SimpleFacetDefinition(vertices);
    }

    /** Read a vector starting from the current parser position.
     * @return vector read from the parser
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private Vector3D readVector() {
        final double x = readDouble();
        discardNonData();
        final double y = readDouble();
        discardNonData();
        final double z = readDouble();

        return Vector3D.of(x, y, z);
    }

    /** Read a double starting from the current parser position.
     * @return double value read from the parser
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private double readDouble() {
        return parser
                .next(TextFacetDefinitionReader::isDataTokenPart)
                .getCurrentTokenAsDouble();
    }

    /** Discard lines that do not contain any data. This includes empty lines
     * and lines that only contain comments.
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private void discardNonDataLines() {
        parser.discardLineWhitespace();
        while (parser.hasMoreCharacters() &&
                (!parser.hasMoreCharactersOnLine() ||
                foundComment())) {

            parser
                .discardLine()
                .discardLineWhitespace();
        }
    }

    /** Discard a sequence of non-data characters on the current line starting
     * from the current parser position.
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private void discardNonData() {
        parser.discard(c ->
            !SimpleTextParser.isNewLinePart(c) &&
            !isDataTokenPart(c) &&
            c != commentStartChar);

        if (foundComment()) {
            // discard everything to the end of the line but do
            // not read the new line sequence
            parser.discard(SimpleTextParser::isNotNewLinePart);
        }
    }

    /** Return true if the parser is positioned at the start of the comment token.
     * @return true if the parser is positioned at the start of the comment token.
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private boolean foundComment() {
        return hasCommentToken &&
                commentToken.equals(parser.peek(commentToken.length()));
    }

    /** Internal method called to set the comment token state.
     * @param commentTokenStr comment token to set
     * @throws IllegalArgumentException if the argument is non-null and contains whitespace
     */
    private void setCommentTokenInternal(final String commentTokenStr) {
        if (commentTokenStr != null && containsWhitespace(commentTokenStr)) {
            throw new IllegalArgumentException("Comment token cannot contain whitespace; was [" +
                    commentTokenStr + "]");
        }

        this.commentToken = commentTokenStr;
        this.hasCommentToken = commentTokenStr != null && commentTokenStr.length() > 0;
        this.commentStartChar = this.hasCommentToken ?
                commentTokenStr.charAt(0) :
                -1;
    }

    /** Return true if the given character is considered as part of a data token
     * for this reader.
     * @param ch character to test
     * @return true if {@code ch} is part of a data token
     */
    private static boolean isDataTokenPart(final int ch) {
        // include all alphabetic characters in the data tokens, which will help
        // to provide better error messages in case of failure (ie, tokens will
        // be split more naturally)
        return Character.isAlphabetic(ch) ||
                SimpleTextParser.isDecimalPart(ch);
    }

    /** Return true if the given string contains any whitespace characters.
     * @param str string to test
     * @return true if {@code str} contains any whitespace characters
     */
    private static boolean containsWhitespace(final String str) {
        for (final char ch : str.toCharArray()) {
            if (Character.isWhitespace(ch)) {
                return true;
            }
        }

        return false;
    }
}
