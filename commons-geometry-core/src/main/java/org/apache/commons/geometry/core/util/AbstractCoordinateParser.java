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
package org.apache.commons.geometry.core.util;

import java.text.ParsePosition;

/** Abstract class providing basic parsing functionality for reading coordinate tuples
 * from strings.
 */
public abstract class AbstractCoordinateParser {

    /** String separating coordinate values */
    private final String separator;

    /** String used to signal the start of a coordinate tuple; may be null */
    private final String prefix;

    /** String used to signal the end of a coordinate tuple; may be null */
    private final String suffix;

    /** Simple constructor
     * @param separator String used to separate coordinate values; must not be null.
     * @param prefix String used to signal the start of a coordinate tuple; if null, no
     *      string is expected at the start of the tuple
     * @param suffix String used to signal the end of a coordinate tuple; if null, no
     *      string is expected at the end of the tuple
     */
    protected AbstractCoordinateParser(String separator, String prefix, String suffix) {
        this.separator = separator;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /** Returns the string used to separate coordinate values.
     * @return the coordinate value separator string
     */
    public String getSeparator() {
        return separator;
    }

    /** Returns the string used to signal the start of a coordinate tuple. This value may be null.
     * @return the string used to begin each coordinate tuple or null
     */
    public String getPrefix() {
        return prefix;
    }

    /** Returns the string used to signal the end of a coordinate tuple. This value may be null.
     * @return the string used to end each coordinate tuple or null
     */
    public String getSuffix() {
        return suffix;
    }

    /** Reads the configured prefix from the current position in the given string, ignoring any preceding
     * whitespace, and advances the parsing position past the prefix sequence. An exception is thrown if the
     * prefix is not found. Does nothing if the prefix is null.
     * @param str the string being parsed
     * @param pos the current parsing position
     * @throws IllegalArgumentException if the configured prefix is not null and is not found at the current
     *      parsing position, ignoring preceding whitespace
     */
    protected void readPrefix(String str, ParsePosition pos) throws IllegalArgumentException {
        if (prefix != null) {
            consumeWhitespace(str, pos);
            readSequence(str, prefix, pos);
        }
    }

    /** Reads and returns a coordinate value from the current position in the given string. An exception is thrown if a
     * valid number is not found. The parsing position is advanced past the parsed number and any trailing separator.
     * @param str the string being parsed
     * @param pos the current parsing position
     * @return the coordinate value
     * @throws IllegalArgumentException if the configured prefix is not null and is not found at the current
     *      parsing position, ignoring preceding whitespace
     */
    protected double readCoordinateValue(String str, ParsePosition pos) throws IllegalArgumentException {
        final int startIdx = pos.getIndex();

        int endIdx = str.indexOf(separator, startIdx);
        if (endIdx < 0) {
            if (suffix != null) {
                endIdx = str.indexOf(suffix, startIdx);
            }

            if (endIdx < 0) {
                endIdx = str.length();
            }
        }

        String substr = str.substring(startIdx, endIdx);
        try {
            double value = Double.parseDouble(substr);

            // advance the position and move past any terminating separator
            pos.setIndex(endIdx);
            matchSequence(str, separator, pos);

            return value;
        }
        catch (NumberFormatException exc) {
            fail(String.format("unable to parse number from string \"%s\"", substr), str, pos, exc);
            return 0.0; // for the compiler
        }
    }

    /** Reads the configured suffix from the current position in the given string, ignoring any preceding
     * whitespace, and advances the parsing position past the suffix sequence. An exception is thrown if the
     * suffix is not found. Does nothing if the suffix is null.
     * @param str the string being parsed
     * @param pos the current parsing position
     * @throws IllegalArgumentException if the configured suffix is not null and is not found at the current
     *      parsing position, ignoring preceding whitespace
     */
    protected void readSuffix(String str, ParsePosition pos) throws IllegalArgumentException {
        if (suffix != null) {
            consumeWhitespace(str, pos);
            readSequence(str, suffix, pos);
        }
    }

    /** Ends a parse operation by ensuring that all non-whitespace characters in the string have been parsed. An
     * exception is thrown if extra content is found.
     * @param str the string being parsed
     * @param pos the current parsing position
     * @throws IllegalArgumentException if extra non-whitespace content is found past the current parsing position
     */
    protected void endParse(String str, ParsePosition pos) throws IllegalArgumentException {
        consumeWhitespace(str, pos);
        if (pos.getIndex() != str.length()) {
            fail("unexpected content", str, pos);
        }
    }

    /** Advances {@code pos} past any whitespace characters in {@code str},
     * starting at the current parse position index.
     * @param str the input string
     * @param pos the current parse position
     */
    protected void consumeWhitespace(String str, ParsePosition pos) {
        int idx = pos.getIndex();
        final int len = str.length();

        for (; idx<len; ++idx) {
            if (!Character.isWhitespace(str.codePointAt(idx))) {
                break;
            }
        }

        pos.setIndex(idx);
    }

    /** Returns a boolean indicating whether or not the input string {@code str}
     * contains the string {@code seq} at the given parse index. If the match succeeds,
     * the index of {@code pos} is moved to the first character after the match. If
     * the match does not succeed, the parse position is left unchanged.
     * @param str the string to match against
     * @param seq the sequence to look for in {@code str}
     * @param pos the parse position indicating the index in {@code str}
     *      to attempt the match
     * @return true if {@code str} contains exactly the same characters as {@code seq}
     *      at {@code pos}; otherwise, false
     */
    protected boolean matchSequence(String str, String seq, ParsePosition pos) {
        final int idx = pos.getIndex();
        final int inputLength = str.length();
        final int seqLength = seq.length();

        int i = idx;
        int s = 0;
        for (; i<inputLength && s<seqLength; ++i, ++s) {
            if (str.codePointAt(i) != seq.codePointAt(s)) {
                break;
            }
        }

        if (i <= inputLength && s == seqLength) {
            pos.setIndex(idx + seqLength);
            return true;
        }
        return false;
    }

    /** Reads the string given by {@code seq} from the given position in {@code str}.
     * Throws an IllegalArgumentException if the sequence is not found at that position.
     * @param str the string to match against
     * @param seq the sequence to look for in {@code str}
     * @param pos the parse position indicating the index in {@code str}
     *      to attempt the match
     * @throws IllegalArgumentException if {@code str} does not contain the characters from
     *      {@code seq} at position {@code pos}
     */
    protected void readSequence(String str, String seq, ParsePosition pos) throws IllegalArgumentException {
        if (!matchSequence(str, seq, pos)) {
            final int idx = pos.getIndex();
            final String actualSeq = str.substring(idx, Math.min(str.length(), idx + seq.length()));

            fail(String.format("expected \"%s\" but found \"%s\"", seq, actualSeq), str, pos);
        }
    }

    /** Aborts the current parsing operation by throwing an {@link IllegalArgumentException} with an informative
     * error message.
     * @param msg the error message
     * @param str the string being parsed
     * @param pos the current parse position
     * @throws IllegalArgumentException the exception signaling a parse failure
     */
    protected void fail(String msg, String str, ParsePosition pos) throws IllegalArgumentException {
        fail(msg, str, pos, null);
    }

    /** Aborts the current parsing operation by throwing an {@link IllegalArgumentException} with an informative
     * error message.
     * @param msg the error message
     * @param str the string being parsed
     * @param pos the current parse position
     * @param cause the original cause of the error
     * @throws IllegalArgumentException the exception signaling a parse failure
     */
    protected void fail(String msg, String str, ParsePosition pos, Throwable cause) throws IllegalArgumentException {
        String fullMsg = String.format("Failed to parse string \"%s\" at index %d: %s", str, pos.getIndex(), msg);

        throw new CoordinateParseException(fullMsg, cause);
    }

    /** Exception class for errors occurring during coordinate parsing.
     */
    private static class CoordinateParseException extends IllegalArgumentException {

        /** Serializable version identifier */
        private static final long serialVersionUID = 1494716029613981959L;

        /** Simple constructor.
         * @param msg the exception message
         * @param cause the exception root cause
         */
        CoordinateParseException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
