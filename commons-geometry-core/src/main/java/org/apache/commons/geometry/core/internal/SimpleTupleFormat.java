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
package org.apache.commons.geometry.core.internal;

import java.text.ParsePosition;

/** Class for performing simple formatting and parsing of real number tuples.
 */
public class SimpleTupleFormat {

    /** Default value separator string. */
    private static final String DEFAULT_SEPARATOR = ",";

    /** Space character. */
    private static final char SPACE = ' ';

    /** Static instance configured with default values. Tuples in this format
     * are enclosed by parentheses and separated by commas.
     */
    private static final SimpleTupleFormat DEFAULT_INSTANCE =
            new SimpleTupleFormat(",", "(", ")");

    /** String separating tuple values. */
    private final String separator;

    /** String used to signal the start of a tuple; may be {@code null}. */
    private final String prefix;

    /** String used to signal the end of a tuple; may be {@code null}. */
    private final String suffix;

    /** Constructs a new instance with the default string separator (a comma)
     * and the given prefix and suffix.
     * @param prefix String used to signal the start of a tuple; if {@code null}, no
     *      string is expected at the start of the tuple
     * @param suffix String used to signal the end of a tuple; if {@code null}, no
     *      string is expected at the end of the tuple
     */
    public SimpleTupleFormat(final String prefix, final String suffix) {
        this(DEFAULT_SEPARATOR, prefix, suffix);
    }

    /** Simple constructor.
     * @param separator String used to separate tuple values; must not be null.
     * @param prefix String used to signal the start of a tuple; if {@code null}, no
     *      string is expected at the start of the tuple
     * @param suffix String used to signal the end of a tuple; if {@code null}, no
     *      string is expected at the end of the tuple
     */
    protected SimpleTupleFormat(final String separator, final String prefix, final String suffix) {
        this.separator = separator;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /** Return the string used to separate tuple values.
     * @return the value separator string
     */
    public String getSeparator() {
        return separator;
    }

    /** Return the string used to signal the start of a tuple. This value may be {@code null}.
     * @return the string used to begin each tuple or {@code null}
     */
    public String getPrefix() {
        return prefix;
    }

    /** Returns the string used to signal the end of a tuple. This value may be {@code null}.
     * @return the string used to end each tuple or {@code null}
     */
    public String getSuffix() {
        return suffix;
    }

    /** Return a tuple string with the given value.
     * @param a value
     * @return 1-tuple string
     */
    public String format(final double a) {
        final StringBuilder sb = new StringBuilder();

        if (prefix != null) {
            sb.append(prefix);
        }

        sb.append(a);

        if (suffix != null) {
            sb.append(suffix);
        }

        return sb.toString();
    }

    /** Return a tuple string with the given values.
     * @param a1 first value
     * @param a2 second value
     * @return 2-tuple string
     */
    public String format(final double a1, final double a2) {
        final StringBuilder sb = new StringBuilder();

        if (prefix != null) {
            sb.append(prefix);
        }

        sb.append(a1)
            .append(separator)
            .append(SPACE)
            .append(a2);

        if (suffix != null) {
            sb.append(suffix);
        }

        return sb.toString();
    }

    /** Return a tuple string with the given values.
     * @param a1 first value
     * @param a2 second value
     * @param a3 third value
     * @return 3-tuple string
     */
    public String format(final double a1, final double a2, final double a3) {
        final StringBuilder sb = new StringBuilder();

        if (prefix != null) {
            sb.append(prefix);
        }

        sb.append(a1)
            .append(separator)
            .append(SPACE)
            .append(a2)
            .append(separator)
            .append(SPACE)
            .append(a3);

        if (suffix != null) {
            sb.append(suffix);
        }

        return sb.toString();
    }

    /** Return a tuple string with the given values.
     * @param a1 first value
     * @param a2 second value
     * @param a3 third value
     * @param a4 fourth value
     * @return 4-tuple string
     */
    public String format(final double a1, final double a2, final double a3, final double a4) {
        final StringBuilder sb = new StringBuilder();

        if (prefix != null) {
            sb.append(prefix);
        }

        sb.append(a1)
            .append(separator)
            .append(SPACE)
            .append(a2)
            .append(separator)
            .append(SPACE)
            .append(a3)
            .append(separator)
            .append(SPACE)
            .append(a4);

        if (suffix != null) {
            sb.append(suffix);
        }

        return sb.toString();
    }

    /** Parse the given string as a 1-tuple and passes the tuple values to the
     * given function. The function output is returned.
     * @param <T> function return type
     * @param str the string to be parsed
     * @param fn function that will be passed the parsed tuple values
     * @return object returned by {@code fn}
     * @throws IllegalArgumentException if the input string format is invalid
     */
    public <T> T parse(final String str, final DoubleFunction1N<T> fn) {
        final ParsePosition pos = new ParsePosition(0);

        readPrefix(str, pos);
        final double v = readTupleValue(str, pos);
        readSuffix(str, pos);
        endParse(str, pos);

        return fn.apply(v);
    }

    /** Parse the given string as a 2-tuple and passes the tuple values to the
     * given function. The function output is returned.
     * @param <T> function return type
     * @param str the string to be parsed
     * @param fn function that will be passed the parsed tuple values
     * @return object returned by {@code fn}
     * @throws IllegalArgumentException if the input string format is invalid
     */
    public <T> T parse(final String str, final DoubleFunction2N<T> fn) {
        final ParsePosition pos = new ParsePosition(0);

        readPrefix(str, pos);
        final double v1 = readTupleValue(str, pos);
        final double v2 = readTupleValue(str, pos);
        readSuffix(str, pos);
        endParse(str, pos);

        return fn.apply(v1, v2);
    }

    /** Parse the given string as a 3-tuple and passes the parsed values to the
     * given function. The function output is returned.
     * @param <T> function return type
     * @param str the string to be parsed
     * @param fn function that will be passed the parsed tuple values
     * @return object returned by {@code fn}
     * @throws IllegalArgumentException if the input string format is invalid
     */
    public <T> T parse(final String str, final DoubleFunction3N<T> fn) {
        final ParsePosition pos = new ParsePosition(0);

        readPrefix(str, pos);
        final double v1 = readTupleValue(str, pos);
        final double v2 = readTupleValue(str, pos);
        final double v3 = readTupleValue(str, pos);
        readSuffix(str, pos);
        endParse(str, pos);

        return fn.apply(v1, v2, v3);
    }

    /** Read the configured prefix from the current position in the given string, ignoring any preceding
     * whitespace, and advance the parsing position past the prefix sequence. An exception is thrown if the
     * prefix is not found. Does nothing if the prefix is {@code null}.
     * @param str the string being parsed
     * @param pos the current parsing position
     * @throws IllegalArgumentException if the configured prefix is not null and is not found at the current
     *      parsing position, ignoring preceding whitespace
     */
    private void readPrefix(final String str, final ParsePosition pos) {
        if (prefix != null) {
            consumeWhitespace(str, pos);
            readSequence(str, prefix, pos);
        }
    }

    /** Read and return a tuple value from the current position in the given string. An exception is thrown if a
     * valid number is not found. The parsing position is advanced past the parsed number and any trailing separator.
     * @param str the string being parsed
     * @param pos the current parsing position
     * @return the tuple value
     * @throws IllegalArgumentException if the configured prefix is not null and is not found at the current
     *      parsing position, ignoring preceding whitespace
     */
    private double readTupleValue(final String str, final ParsePosition pos) {
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

        final String substr = str.substring(startIdx, endIdx);
        try {
            final double value = Double.parseDouble(substr);

            // advance the position and move past any terminating separator
            pos.setIndex(endIdx);
            matchSequence(str, separator, pos);

            return value;
        } catch (final NumberFormatException exc) {
            throw parseFailure(String.format("unable to parse number from string \"%s\"", substr), str, pos, exc);
        }
    }

    /** Read the configured suffix from the current position in the given string, ignoring any preceding
     * whitespace, and advance the parsing position past the suffix sequence. An exception is thrown if the
     * suffix is not found. Does nothing if the suffix is {@code null}.
     * @param str the string being parsed
     * @param pos the current parsing position
     * @throws IllegalArgumentException if the configured suffix is not null and is not found at the current
     *      parsing position, ignoring preceding whitespace
     */
    private void readSuffix(final String str, final ParsePosition pos) {
        if (suffix != null) {
            consumeWhitespace(str, pos);
            readSequence(str, suffix, pos);
        }
    }

    /** End a parse operation by ensuring that all non-whitespace characters in the string have been parsed. An
     * exception is thrown if extra content is found.
     * @param str the string being parsed
     * @param pos the current parsing position
     * @throws IllegalArgumentException if extra non-whitespace content is found past the current parsing position
     */
    private void endParse(final String str, final ParsePosition pos) {
        consumeWhitespace(str, pos);
        if (pos.getIndex() != str.length()) {
            throw parseFailure("unexpected content", str, pos);
        }
    }

    /** Advance {@code pos} past any whitespace characters in {@code str},
     * starting at the current parse position index.
     * @param str the input string
     * @param pos the current parse position
     */
    private void consumeWhitespace(final String str, final ParsePosition pos) {
        int idx = pos.getIndex();
        final int len = str.length();

        for (; idx < len; ++idx) {
            if (!Character.isWhitespace(str.codePointAt(idx))) {
                break;
            }
        }

        pos.setIndex(idx);
    }

    /** Return a boolean indicating whether or not the input string {@code str}
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
    private boolean matchSequence(final String str, final String seq, final ParsePosition pos) {
        final int idx = pos.getIndex();
        final int inputLength = str.length();
        final int seqLength = seq.length();

        int i = idx;
        int s = 0;
        for (; i < inputLength && s < seqLength; ++i, ++s) {
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

    /** Read the string given by {@code seq} from the given position in {@code str}.
     * Throws an IllegalArgumentException if the sequence is not found at that position.
     * @param str the string to match against
     * @param seq the sequence to look for in {@code str}
     * @param pos the parse position indicating the index in {@code str}
     *      to attempt the match
     * @throws IllegalArgumentException if {@code str} does not contain the characters from
     *      {@code seq} at position {@code pos}
     */
    private void readSequence(final String str, final String seq, final ParsePosition pos) {
        if (!matchSequence(str, seq, pos)) {
            final int idx = pos.getIndex();
            final String actualSeq = str.substring(idx, Math.min(str.length(), idx + seq.length()));

            throw parseFailure(String.format("expected \"%s\" but found \"%s\"", seq, actualSeq), str, pos);
        }
    }

    /** Return an instance configured with default values. Tuples in this format
     * are enclosed by parentheses and separated by commas.
     *
     * Ex:
     * <pre>
     * "(1.0)"
     * "(1.0, 2.0)"
     * "(1.0, 2.0, 3.0)"
     * </pre>
     * @return instance configured with default values
     */
    public static SimpleTupleFormat getDefault() {
        return DEFAULT_INSTANCE;
    }

    /** Return an {@link IllegalArgumentException} representing a parsing failure.
     * @param msg the error message
     * @param str the string being parsed
     * @param pos the current parse position
     * @return an exception signaling a parse failure
     */
    private static IllegalArgumentException parseFailure(final String msg, final String str, final ParsePosition pos) {
        return parseFailure(msg, str, pos, null);
    }

    /** Return an {@link IllegalArgumentException} representing a parsing failure.
     * @param msg the error message
     * @param str the string being parsed
     * @param pos the current parse position
     * @param cause the original cause of the error
     * @return an exception signaling a parse failure
     */
    private static IllegalArgumentException parseFailure(final String msg, final String str, final ParsePosition pos,
                                                         final Throwable cause) {
        final String fullMsg = String.format("Failed to parse string \"%s\" at index %d: %s",
                str, pos.getIndex(), msg);

        return new TupleParseException(fullMsg, cause);
    }

    /** Exception class for errors occurring during tuple parsing.
     */
    private static class TupleParseException extends IllegalArgumentException {

        /** Serializable version identifier. */
        private static final long serialVersionUID = 20180629;

        /** Simple constructor.
         * @param msg the exception message
         * @param cause the exception root cause
         */
        TupleParseException(final String msg, final Throwable cause) {
            super(msg, cause);
        }
    }
}
