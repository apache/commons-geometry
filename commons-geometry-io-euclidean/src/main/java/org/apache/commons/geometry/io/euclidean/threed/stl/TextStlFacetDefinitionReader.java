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

import java.io.Reader;
import java.util.Arrays;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;
import org.apache.commons.geometry.io.core.internal.SimpleTextParser;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;
import org.apache.commons.geometry.io.euclidean.threed.SimpleFacetDefinition;

/** {@link FacetDefinitionReader} for reading the text (i.e., "ASCII") version of the STL file format.
 * @see <a href="https://en.wikipedia.org/wiki/STL_%28file_format%29#ASCII_STL">ASCII STL</a>
 */
public class TextStlFacetDefinitionReader implements FacetDefinitionReader {

    /** Underlying reader instance. */
    private Reader reader;

    /** Text parser. */
    private SimpleTextParser parser;

    /** Flag indicating if the start of a solid definition was detected. */
    private boolean foundSolidStart;

    /** Flag indicating if the end of a solid definition was detected. */
    private boolean foundSolidEnd;

    /** The name of the solid being read. */
    private String solidName;

    /** Construct a new instance for reading text STL content from the given reader.
     * @param reader reader to read characters from
     */
    public TextStlFacetDefinitionReader(final Reader reader) {
        this.reader = reader;
        this.parser = new SimpleTextParser(reader);
    }

    /** Get the name of the STL solid being read or null if no name was specified.
     * @return the name of the STL solid being read or null if no name was specified
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public String getSolidName() {
        ensureSolidStarted();

        return solidName;
    }

    /** {@inheritDoc} */
    @Override
    public FacetDefinition readFacet() {
        if (!foundSolidEnd && parser.hasMoreCharacters()) {
            ensureSolidStarted();

            nextWord();

            int choice = parser.chooseIgnoreCase(
                    StlConstants.FACET_START_KEYWORD,
                    StlConstants.SOLID_END_KEYWORD);

            if (choice == 0) {
                return readFacetInternal();
            } else {
                foundSolidEnd = true;
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        GeometryIOUtils.closeUnchecked(reader);
    }

    /** Internal method to read a single facet from the STL content.
     * @return next facet definition
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private FacetDefinition readFacetInternal() {
        matchKeyword(StlConstants.NORMAL_KEYWORD);
        final Vector3D normal = readVector();

        matchKeyword(StlConstants.OUTER_KEYWORD);
        matchKeyword(StlConstants.LOOP_START_KEYWORD);

        matchKeyword(StlConstants.VERTEX_KEYWORD);
        final Vector3D p1 = readVector();

        matchKeyword(StlConstants.VERTEX_KEYWORD);
        final Vector3D p2 = readVector();

        matchKeyword(StlConstants.VERTEX_KEYWORD);
        final Vector3D p3 = readVector();

        matchKeyword(StlConstants.LOOP_END_KEYWORD);
        matchKeyword(StlConstants.FACET_END_KEYWORD);

        return new SimpleFacetDefinition(Arrays.asList(p1, p2, p3), normal);
    }

    /** Ensure that an STL solid definition is in the process of being read. If not, the beginning
     * of a the definition is attempted to be read from the input.
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private void ensureSolidStarted() {
        if (!foundSolidStart) {
            beginSolid();

            foundSolidStart = true;
        }
    }

    /** Begin reading an STL solid definition. The "solid" keyword is read
     * along with the name of the solid.
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private void beginSolid() {
        matchKeyword(StlConstants.SOLID_START_KEYWORD);

        solidName = trimmedOrNull(parser.nextLine()
                .getCurrentToken());
    }

    /** Read the next word from the content, discarding preceding whitespace.
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private void nextWord() {
        parser.discardWhitespace()
            .nextAlphanumeric();
    }

    /** Read the next word from the content and match it against the given keyword.
     * @param keyword keyword to match against
     * @throws IllegalStateException if the read content does not match the given keyword
     * @throws java.io.UncheckedIOException if an I/O error occurs or
     */
    private void matchKeyword(final String keyword) {
        nextWord();
        parser.matchIgnoreCase(keyword);
    }

    /** Read a vector from the input.
     * @return the vector read from the input
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private Vector3D readVector() {
        final double x = readDouble();
        final double y = readDouble();
        final double z = readDouble();

        return Vector3D.of(x, y, z);
    }

    /** Read a double value from the input.
     * @return double value read from the input
     * @throws IllegalStateException if a data format error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private double readDouble() {
        return parser
                .discardWhitespace()
                .next(SimpleTextParser::isDecimalPart)
                .getCurrentTokenAsDouble();
    }

    /** Return a trimmed version of the given string or null if the string contains
     * only whitespace.
     * @param str input stream
     * @return a trimmed version of the given string or null if the string contains only
     *      whitespace
     */
    private static String trimmedOrNull(final String str) {
        if (str != null) {
            final String trimmed = str.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }

        return null;
    }
}
