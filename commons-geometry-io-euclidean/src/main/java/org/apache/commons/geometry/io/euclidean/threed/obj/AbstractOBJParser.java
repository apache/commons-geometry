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
package org.apache.commons.geometry.io.euclidean.threed.obj;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.internal.SimpleTextParser;

/** Abstract base class for OBJ parsing functionality.
 */
public abstract class AbstractOBJParser {

    /** Text parser instance. */
    private final SimpleTextParser parser;

    /** The current (most recently parsed) keyword. */
    private String currentKeyword;

    /** Construct a new instance for parsing OBJ content from the given text parser.
     * @param parser text parser to read content from
     */
    protected AbstractOBJParser(final SimpleTextParser parser) {
        this.parser = parser;
    }

    /** Get the current keyword, meaning the keyword most recently parsed via the {@link #nextKeyword()}
     * method. Null is returned if parsing has not started or the end of the content has been reached.
     * @return the current keyword or null if parsing has not started or the end
     *      of the content has been reached
     */
    public String getCurrentKeyword() {
        return currentKeyword;
    }

    /** Advance the parser to the next keyword, returning true if a keyword has been found
     * and false if the end of the content has been reached. Keywords consist of alphanumeric
     * strings placed at the beginning of lines. Comments and blank lines are ignored.
     * @return true if a keyword has been found and false if the end of content has been reached
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if invalid content is found
     */
    public boolean nextKeyword() throws IOException {
        currentKeyword = null;

        // advance to the next line if not at the start of a line
        if (parser.getColumnNumber() != 1) {
            discardDataLine();
        }

        // search for the next keyword
        while (currentKeyword == null && parser.hasMoreCharacters()) {
            if (!nextDataLineContent() ||
                    parser.peekChar() == OBJConstants.COMMENT_CHAR) {
                // use a standard line discard here so we don't interpret line continuations
                // within comments; the interpreted OBJ content should be the same regardless
                // of the presence of comments
                parser.discardLine();
            } else if (parser.getColumnNumber() != 1) {
                throw parser.parseError("non-blank lines must begin with an OBJ keyword or comment character");
            } else if (!readKeyword()) {
                throw parser.unexpectedToken("OBJ keyword");
            } else {
                final String keywordValue = parser.getCurrentToken();

                handleKeyword(keywordValue);

                currentKeyword = keywordValue;

                // advance past whitespace to the next data value
                discardDataLineWhitespace();
            }
        }

        return currentKeyword != null;
    }

    /** Read the remaining content on the current data line, taking line continuation characters into
     * account.
     * @return remaining content on the current data line or null if the end of the content has
     *      been reached
     * @throws IOException if an I/O error occurs
     */
    public String readDataLine() throws IOException {
        parser.nextWithLineContinuation(
                OBJConstants.LINE_CONTINUATION_CHAR,
                SimpleTextParser::isNotNewLinePart)
            .discardNewLineSequence();

        return parser.getCurrentToken();
    }

    /** Discard remaining content on the current data line, taking line continuation characters into
     * account.
     * @throws IOException if an I/O error occurs
     */
    public void discardDataLine() throws IOException {
        parser.discardWithLineContinuation(
                OBJConstants.LINE_CONTINUATION_CHAR,
                SimpleTextParser::isNotNewLinePart)
            .discardNewLineSequence();
    }

    /** Read a whitespace-delimited 3D vector from the current data line.
     * @return vector vector read from the current line
     * @throws IOException if an I/O error occurs
     */
    public Vector3D readVector() throws IOException {
        discardDataLineWhitespace();
        final double x = nextDouble();

        discardDataLineWhitespace();
        final double y = nextDouble();

        discardDataLineWhitespace();
        final double z = nextDouble();

        return Vector3D.of(x, y, z);
    }

    /** Read whitespace-delimited double values from the current data line.
     * @return double values read from the current line
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if double values are not able to be parsed
     */
    public double[] readDoubles() throws IOException {
        final List<Double> list = new ArrayList<>();

        while (nextDataLineContent()) {
            list.add(nextDouble());
        }

        // convert to primitive array
        final double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            arr[i] = list.get(i);
        }

        return arr;
    }

    /** Get the text parser for the instance.
     * @return text parser for the instance
     */
    protected SimpleTextParser getTextParser() {
        return parser;
    }

    /** Method called when a keyword is encountered in the parsed OBJ content. Subclasses should use
     * this method to validate the keyword and/or update any internal state.
     * @param keyword keyword encountered in the OBJ content
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if the given keyword is invalid
     */
    protected abstract void handleKeyword(String keyword) throws IOException;

    /** Discard whitespace on the current data line, taking line continuation characters into account.
     * @return text parser instance
     * @throws IOException if an I/O error occurs
     */
    protected SimpleTextParser discardDataLineWhitespace() throws IOException {
        return parser.discardWithLineContinuation(
                OBJConstants.LINE_CONTINUATION_CHAR,
                SimpleTextParser::isLineWhitespace);
    }

    /** Discard whitespace on the current data line and return true if any more characters
     * remain on the line.
     * @return true if more non-whitespace characters remain on the current data line
     * @throws IOException if an I/O error occurs
     */
    protected boolean nextDataLineContent() throws IOException {
        return discardDataLineWhitespace().hasMoreCharactersOnLine();
    }

    /** Get the next whitespace-delimited double on the current data line.
     * @return the next whitespace-delimited double on the current line
     * @throws IOException if an I/O error occurs
     * @throws IllegalStateException if a double value is not able to be parsed
     */
    protected double nextDouble() throws IOException {
        return parser.nextWithLineContinuation(OBJConstants.LINE_CONTINUATION_CHAR, SimpleTextParser::isNotWhitespace)
            .getCurrentTokenAsDouble();
    }

    /** Read a keyword consisting of alphanumeric characters from the current parser position and set it
     * as the current token. Returns true if a non-empty keyword was found.
     * @return true if a non-empty keyword was found.
     * @throws IOException if an I/O error occurs
     */
    private boolean readKeyword() throws IOException {
        return parser
                .nextWithLineContinuation(OBJConstants.LINE_CONTINUATION_CHAR, SimpleTextParser::isAlphanumeric)
                .hasNonEmptyToken();
    }
}
