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
package org.apache.commons.geometry.io.core.internal;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

/** Class providing basic text parsing capabilities. The goals of this class are to
 * (1) provide a simple, flexible API for performing common text parsing operations and
 * (2) provide a mechanism for creating consistent and informative parsing errors.
 * This class is not intended as a replacement for grammar-based parsers and/or lexers.
 */
public class SimpleTextParser {

    /** Constant indicating that the end of the input has been reached. */
    private static final int EOF = -1;

    /** Carriage return character. */
    private static final char CR = '\r';

    /** Line feed character. */
    private static final char LF = '\n';

    /** Default value for the max string length property. */
    private static final int DEFAULT_MAX_STRING_LENGTH = 1024;

    /** Error message used when a string exceeds the configured maximum length. */
    private static final String STRING_LENGTH_ERR_MSG = "string length exceeds maximum value of ";

    /** Initial token position number. */
    private static final int INITIAL_TOKEN_POS = -1;

    /** Int consumer that does nothing. */
    private static final IntConsumer NOOP_CONSUMER = ch -> { };

    /** Current line number; line numbers start counting at 1. */
    private int lineNumber = 1;

    /** Current character column on the current line; column numbers start at 1.*/
    private int columnNumber = 1;

    /** Maximum length for strings returned by this instance. */
    private int maxStringLength = DEFAULT_MAX_STRING_LENGTH;

    /** The current token. */
    private String currentToken;

    /** The line number that the current token started on. */
    private int currentTokenLineNumber = INITIAL_TOKEN_POS;

    /** The character number that the current token started on. */
    private int currentTokenColumnNumber = INITIAL_TOKEN_POS;

    /** Flag used to indicate that at least one token has been read from the stream. */
    private boolean hasSetToken;

    /** Character read buffer used to access the character stream. */
    private final CharReadBuffer buffer;

    /** Construct a new instance that reads characters from the given reader. The
     * reader will not be closed.
     * @param reader reader instance to read characters from
     */
    public SimpleTextParser(final Reader reader) {
        this(new CharReadBuffer(reader));
    }

    /** Construct a new instance that reads characters from the given character buffer.
     * @param buffer read buffer to read characters from
     */
    public SimpleTextParser(final CharReadBuffer buffer) {
        this.buffer = buffer;
    }

    /** Get the current line number. Line numbers start at 1.
     * @return the current line number
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /** Set the current line number. This does not affect the character stream position,
     * only the value returned by {@link #getLineNumber()}.
     * @param lineNumber line number to set; line numbers start at 1
     */
    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /** Get the current column number. This indicates the column position of the
     * character that will returned by the next call to {@link #readChar()}. The first
     * character of each line has a column number of 1.
     * @return the current column number; column numbers start at 1
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /** Set the current column number. This does not affect the character stream position,
     * only the value returned by {@link #getColumnNumber()}.
     * @param column the column number to set; column numbers start at 1
     */
    public void setColumnNumber(final int column) {
        this.columnNumber = column;
    }

    /** Get the maximum length for strings returned by this instance. Operations
     * that produce strings longer than this length will throw an exception.
     * @return maximum length for strings returned by this instance
     */
    public int getMaxStringLength() {
        return maxStringLength;
    }

    /** Set the maximum length for strings returned by this instance. Operations
     * that produce strings longer than this length will throw an exception.
     * @param maxStringLength maximum length for strings returned by this instance
     * @throws IllegalArgumentException if the argument is less than zero
     */
    public void setMaxStringLength(final int maxStringLength) {
        if (maxStringLength < 0) {
            throw new IllegalArgumentException("Maximum string length cannot be less than zero; was " +
                    maxStringLength);
        }
        this.maxStringLength = maxStringLength;
    }

    /** Get the current token. This is the most recent string read by one of the {@code nextXXX()}
     * methods. This value will be null if no token has yet been read or if the end of content has
     * been reached.
     * @return the current token
     * @see #next(int)
     * @see #next(IntPredicate)
     * @see #nextLine()
     * @see #nextAlphanumeric()
     */
    public String getCurrentToken() {
        return currentToken;
    }

    /** Return true if the current token is not null or empty.
     * @return true if the current token is not null or empty
     * @see #getCurrentToken()
     */
    public boolean hasNonEmptyToken() {
        return currentToken != null && !currentToken.isEmpty();
    }

    /** Get the line number that the current token started on. This value will
     * be -1 if no token has been read yet.
     * @return current token starting line number or -1 if no token has been
     *      read yet
     * @see #getCurrentToken()
     */
    public int getCurrentTokenLineNumber() {
        return currentTokenLineNumber;
    }

    /** Get the column position that the current token started on. This value will
     * be -1 if no token has been read yet.
     * @return current token column number or -1 if no oken has been read yet
     * @see #getCurrentToken()
     */
    public int getCurrentTokenColumnNumber() {
        return currentTokenColumnNumber;
    }

    /** Get the current token parsed as an integer.
     * @return the current token parsed as an integer
     * @throws IllegalStateException if no token has been read
     * @throws IOException if the current token cannot be parsed as an integer
     */
    public int getCurrentTokenAsInt() throws IOException {
        ensureHasSetToken();

        Throwable cause = null;

        if (currentToken != null) {
            try {
                return Integer.parseInt(currentToken);
            } catch (NumberFormatException exc) {
                cause = exc;
            }
        }

        throw unexpectedToken("integer", cause);
    }

    /** Get the current token parsed as a double.
     * @return the current token parsed as a double
     * @throws IllegalStateException if no token has been read
     * @throws IOException if the current token cannot be parsed as a double
     */
    public double getCurrentTokenAsDouble() throws IOException {
        ensureHasSetToken();

        Throwable cause = null;

        if (currentToken != null) {
            try {
                return Double.parseDouble(currentToken);
            } catch (NumberFormatException exc) {
                cause = exc;
            }
        }

        throw unexpectedToken("double", cause);
    }

    /** Return true if there are more characters to read from this instance.
     * @return true if there are more characters to read from this instance
     * @throws IOException if an I/O error occurs
     */
    public boolean hasMoreCharacters() throws IOException {
        return buffer.hasMoreCharacters();
    }

    /** Return true if there are more characters to read on the current line.
     * @return true if there are more characters to read on the current line
     * @throws IOException if an I/O error occurs
     */
    public boolean hasMoreCharactersOnLine() throws IOException {
        return hasMoreCharacters() && isNotNewLinePart(peekChar());
    }

    /** Read and return the next character in the stream and advance the parser position.
     * This method updates the current line number and column number but does <strong>not</strong>
     * set the {@link #getCurrentToken() current token}.
     * @return the next character in the stream or -1 if the end of the stream has been
     *      reached
     * @throws IOException if an I/O error occurs
     * @see #peekChar()
     */
    public int readChar() throws IOException {
        final int value = buffer.read();
        if (value == LF ||
                (value == CR && peekChar() != LF)) {
            ++lineNumber;
            columnNumber = 1;
        } else if (value != EOF) {
            ++columnNumber;
        }

        return value;
    }

    /** Read a string containing at most {@code len} characters from the stream and
     * set it as the current token. Characters are added to the string until the string
     * has the specified length or the end of the stream is reached. The characters are
     * consumed from the stream. The token is set to null if no more characters are available
     * from the character stream when this method is called.
     * @param len the maximum length of the extracted string
     * @return this instance
     * @throws IllegalArgumentException if {@code len} is less than 0 or greater than the
     *      configured {@link #getMaxStringLength() maximum string length}
     * @throws IOException if an I/O error occurs
     * @see #getCurrentToken()
     * @see #consume(int, IntConsumer)
     */
    public SimpleTextParser next(final int len) throws IOException {
        validateRequestedStringLength(len);

        final int line = getLineNumber();
        final int col = getColumnNumber();

        String token = null;
        if (hasMoreCharacters()) {
            final StringBuilder sb = new StringBuilder(len);

            consume(len, ch -> sb.append((char) ch));

            token = sb.toString();
        }

        setToken(line, col, token);

        return this;
    }

    /** Read a string containing at most {@code len} characters from the stream and
     * set it as the current token. This is similar to {@link #next(int)} but with the exception
     * that new line sequences beginning with {@code lineContinuationChar} are skipped.
     * @param lineContinuationChar character used to indicate skipped new line sequences
     * @param len the maximum length of the extracted string
     * @return this instance
     * @throws IllegalArgumentException if {@code len} is less than 0 or greater than the
     *      configured {@link #getMaxStringLength() maximum string length}
     * @throws IOException if an I/O error occurs
     * @see #getCurrentToken()
     * @see #consumeWithLineContinuation(char, int, IntConsumer)
     */
    public SimpleTextParser nextWithLineContinuation(final char lineContinuationChar, final int len)
            throws IOException {
        validateRequestedStringLength(len);

        final int line = getLineNumber();
        final int col = getColumnNumber();

        String token = null;
        if (hasMoreCharacters()) {
            final StringBuilder sb = new StringBuilder(len);

            consumeWithLineContinuation(lineContinuationChar, len,
                    ch -> sb.append((char) ch));

            token = sb.toString();
        }

        setToken(line, col, token);

        return this;
    }

    /** Read characters from the stream while the given predicate returns true and set the result
     * as the current token. The next call to {@link #readChar()} will return either a character
     * that fails the predicate test or -1 if the end of the stream has been reached.
     * The token will be null if the end of the stream has been reached prior to the method call.
     * @param pred predicate function passed characters read from the input; reading continues
     *      until the predicate returns false
     * @return this instance
     * @throws IOException if an I/O error occurs or the length of the produced string exceeds the configured
     *      {@link #getMaxStringLength() maximum string length}
     * @see #getCurrentToken()
     * @see #consume(IntPredicate, IntConsumer)
     */
    public SimpleTextParser next(final IntPredicate pred) throws IOException {
        final int line = getLineNumber();
        final int col = getColumnNumber();

        String token = null;
        if (hasMoreCharacters()) {
            final StringCollector collector = new StringCollector(line, col, pred);

            consume(collector, collector);

            token = collector.getString();
        }

        setToken(line, col, token);

        return this;
    }

    /** Read characters from the stream while the given predicate returns true and set the result
     * as the current token. This is similar to {@link #next(IntPredicate)} but with the exception
     * that new line sequences prefixed with {@code lineContinuationChar} are skipped.
     * @param lineContinuationChar character used to indicate skipped new line sequences
     * @param pred predicate function passed characters read from the input; reading continues
     *      until the predicate returns false
     * @return this instance
     * @throws IOException if an I/O error occurs or the length of the produced string exceeds the configured
     *      {@link #getMaxStringLength() maximum string length}
     * @see #getCurrentToken()
     * @see #consume(IntPredicate, IntConsumer)
     */
    public SimpleTextParser nextWithLineContinuation(final char lineContinuationChar, final IntPredicate pred)
            throws IOException {
        final int line = getLineNumber();
        final int col = getColumnNumber();

        String token = null;
        if (hasMoreCharacters()) {
            final StringCollector collector = new StringCollector(line, col, pred);

            consumeWithLineContinuation(lineContinuationChar, collector, collector);

            token = collector.getString();
        }

        setToken(line, col, token);

        return this;
    }

    /** Read characters from the current parser position to the next new line sequence and
     * set the result as the current token . The newline character sequence
     * ('\r', '\n', or '\r\n') at the end of the line is consumed but is not included in the token.
     * The token will be null if the end of the stream has been reached prior to the method call.
     * @return this instance
     * @throws IOException if an I/O error occurs or the length of the produced string exceeds the configured
     *      {@link #getMaxStringLength() maximum string length}
     * @see #getCurrentToken()
     */
    public SimpleTextParser nextLine() throws IOException {
        next(SimpleTextParser::isNotNewLinePart);

        discardNewLineSequence();

        return this;
    }

    /** Read a sequence of alphanumeric characters starting from the current parser position
     * and set the result as the current token. The token will be the empty string if the next
     * character in the stream is not alphanumeric and will be null if the end of the stream has
     * been reached prior to the method call.
     * @return this instance
     * @throws IOException if an I/O error occurs or the length of the produced string exceeds the configured
     *      {@link #getMaxStringLength() maximum string length}
     * @see #getCurrentToken()
     */
    public SimpleTextParser nextAlphanumeric() throws IOException {
        return next(SimpleTextParser::isAlphanumeric);
    }

    /** Discard {@code len} number of characters from the character stream. The
     * parser position is updated but the current token is not changed.
     * @param len number of characters to discard
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser discard(final int len) throws IOException {
        return consume(len, NOOP_CONSUMER);
    }

    /** Discard {@code len} number of characters from the character stream. The
     * parser position is updated but the current token is not changed. Lines beginning
     * with {@code lineContinuationChar} are skipped.
     * @param lineContinuationChar character used to indicate skipped new line sequences
     * @param len number of characters to discard
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser discardWithLineContinuation(final char lineContinuationChar,
            final int len) throws IOException {
        return consumeWithLineContinuation(lineContinuationChar, len, NOOP_CONSUMER);
    }

    /** Discard characters from the stream while the given predicate returns true. The next call
     * to {@link #readChar()} will return either a character that fails the predicate test or -1
     * if the end of the stream has been reached. The parser position is updated but the current
     * token is not changed.
     * @param pred predicate test for characters to discard
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser discard(final IntPredicate pred) throws IOException {
        return consume(pred, NOOP_CONSUMER);
    }

    /** Discard characters from the stream while the given predicate returns true. New line sequences
     * beginning with {@code lineContinuationChar} are skipped. The next call o {@link #readChar()}
     * will return either a character that fails the predicate test or -1 if the end of the stream
     * has been reached. The parser position is updated but the current token is not changed.
     * @param lineContinuationChar character used to indicate skipped new line sequences
     * @param pred predicate test for characters to discard
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser discardWithLineContinuation(final char lineContinuationChar,
            final IntPredicate pred) throws IOException {
        return consumeWithLineContinuation(lineContinuationChar, pred, NOOP_CONSUMER);
    }

    /** Discard a sequence of whitespace characters from the character stream starting from the
     * current parser position. The next call to {@link #readChar()} will return either a non-whitespace
     * character or -1 if the end of the stream has been reached. The parser position is updated
     * but the current token is not changed.
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser discardWhitespace() throws IOException {
        return discard(SimpleTextParser::isWhitespace);
    }

    /** Discard the next whitespace characters on the current line. The next call to
     * {@link #readChar()} will return either a non-whitespace character on the current line,
     * the newline character sequence (indicating the end of the line), or -1 (indicating the
     * end of the stream). The parser position is updated but the current token is not changed.
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser discardLineWhitespace() throws IOException {
        return discard(SimpleTextParser::isLineWhitespace);
    }

    /** Discard the newline character sequence at the current reader position. The sequence
     * is defined as one of "\r", "\n", or "\r\n". Does nothing if the reader is not positioned
     * at a newline sequence. The parser position is updated but the current token is not changed.
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser discardNewLineSequence() throws IOException {
        final int value = peekChar();
        if (value == LF) {
            readChar();
        } else if (value == CR) {
            readChar();

            if (peekChar() == LF) {
                readChar();
            }
        }

        return this;
    }

    /** Discard all remaining characters on the current line, including the terminating
     * newline character sequence. The next call to {@link #readChar()} will return either the
     * first character on the next line or -1 if the end of the stream has been reached.
     * The parser position is updated but the current token is not changed.
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser discardLine() throws IOException {
        discard(SimpleTextParser::isNotNewLinePart);

        discardNewLineSequence();

        return this;
    }

    /** Consume characters from the stream and pass them to {@code consumer} while the given predicate
     * returns true. The operation ends when the predicate returns false or the end of the stream is
     * reached.
     * @param pred predicate test for characters to consume
     * @param consumer object to be passed each consumed character
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser consume(final IntPredicate pred, final IntConsumer consumer) throws IOException {
        int ch;
        while ((ch = peekChar()) != EOF && pred.test(ch)) {
            consumer.accept(readChar());
        }

        return this;
    }

    /** Consume at most {@code len} characters from the stream, passing each to the given consumer.
     * This method is similar to {@link #consume(int, IntConsumer)} with the exception that new line
     * sequences prefixed with {@code lineContinuationChar} are skipped.
     * @param lineContinuationChar character used to indicate skipped new line sequences
     * @param len number of characters to consume
     * @param consumer function to be passed each consumed character
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser consumeWithLineContinuation(final char lineContinuationChar,
            final int len, final IntConsumer consumer) throws IOException {
        int i = -1;
        int ch;
        while (++i < len && (ch = readChar()) != EOF) {
            if (ch == lineContinuationChar && isNewLinePart(peekChar())) {
                --i; // don't count the continuation char toward the total length
                discardNewLineSequence();
            } else {
                consumer.accept(ch);
            }
        }

        return this;
    }

    /** Consume at most {@code len} characters from the stream, passing each to the given consumer.
     * The operation continues until {@code len} number of characters have been read or the end of
     * the stream has been reached.
     * @param len number of characters to consume
     * @param consumer object to be passed each consumed character
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser consume(final int len, final IntConsumer consumer) throws IOException {
        int ch;
        for (int i = 0; i < len; ++i) {
            ch = readChar();
            if (ch != EOF) {
                consumer.accept(ch);
            } else {
                break;
            }
        }

        return this;
    }

    /** Consume characters from the stream and pass them to {@code consumer} while the given predicate
     * returns true. This method is similar to {@link #consume(IntPredicate, IntConsumer)} with the
     * exception that new lines sequences beginning with {@code lineContinuationChar} are skipped.
     * @param lineContinuationChar character used to indicate skipped new line sequences
     * @param pred predicate test for characters to consume
     * @param consumer object to be passed each consumed character
     * @return this instance
     * @throws IOException if an I/O error occurs
     */
    public SimpleTextParser consumeWithLineContinuation(final char lineContinuationChar,
            final IntPredicate pred, final IntConsumer consumer) throws IOException {
        int ch;
        while ((ch = peekChar()) != EOF) {
            if (ch == lineContinuationChar && isNewLinePart(buffer.charAt(1))) {
                readChar();
                discardNewLineSequence();
            } else if (pred.test(ch)) {
                consumer.accept(readChar());
            } else {
                break;
            }
        }

        return this;
    }

    /** Return the next character in the stream but do not advance the parser position.
     * @return the next character in the stream or -1 if the end of the stream has been
     *      reached
     * @throws IOException if an I/O error occurs
     * @see #readChar()
     */
    public int peekChar() throws IOException {
        return buffer.peek();
    }

    /** Return a string containing containing at most {@code len} characters from the stream but
     * without changing the parser position. Characters are added to the string until the
     * string has the specified length or the end of the stream is reached.
     * @param len the maximum length of the returned string
     * @return a string containing containing at most {@code len} characters from the stream
     *      or null if the parser has already reached the end of the stream
     * @throws IllegalArgumentException if {@code len} is less than 0 or greater than the
     *      configured {@link #getMaxStringLength() maximum string length}
     * @throws IOException if an I/O error occurs
     * @see #next(int)
     */
    public String peek(final int len) throws IOException {
        validateRequestedStringLength(len);

        return buffer.peekString(len);
    }

    /** Read characters from the stream while the given predicate returns true but do not
     * change the current token or advance the parser position.
     * @param pred predicate function passed characters read from the input; reading continues
     *      until the predicate returns false
     * @return string containing characters matching {@code pred} or null if the parser has already
     *      reached the end of the stream
     * @throws IOException if an I/O error occurs or the length of the produced string exceeds the configured
     *      {@link #getMaxStringLength() maximum string length}
     * @see #getCurrentToken()
     */
    public String peek(final IntPredicate pred) throws IOException {
        String token = null;

        if (hasMoreCharacters()) {
            final StringCollector collector = new StringCollector(lineNumber, columnNumber, pred);

            int i = -1;
            int ch = buffer.charAt(++i);
            while (ch != EOF && collector.test(ch)) {
                collector.accept(ch);

                ch = buffer.charAt(++i);
            }

            token = collector.getString();
        }

        return token;
    }

    /** Compare the {@link #getCurrentToken() current token} with the argument and throw an
     * exception if they are not equal. The comparison is case-sensitive.
     * @param expected expected token
     * @return this instance
     * @throws IllegalStateException if no token has been read
     * @throws IOException if {@code expected} does not exactly equal the current token
     */
    public SimpleTextParser match(final String expected) throws IOException {
        matchInternal(expected, true, true);
        return this;
    }

    /** Compare the {@link #getCurrentToken() current token} with the argument and throw an
     * exception if they are not equal. The comparison is <em>not</em> case-sensitive.
     * @param expected expected token
     * @return this instance
     * @throws IllegalStateException if no token has been read
     * @throws IOException if {@code expected} does not equal the current token (ignoring case)
     */
    public SimpleTextParser matchIgnoreCase(final String expected) throws IOException {
        matchInternal(expected, false, true);
        return this;
    }

    /** Return true if the {@link #getCurrentToken() current token} is equal to the argument.
     * The comparison is case-sensitive.
     * @param expected expected token
     * @return true if the argument exactly equals the current token
     * @throws IllegalStateException if no token has been read
     * @throws IOException if an I/O error occurs
     */
    public boolean tryMatch(final String expected) throws IOException {
        return matchInternal(expected, true, false);
    }

    /** Return true if the {@link #getCurrentToken() current token} is equal to the argument.
     * The comparison is <em>not</em> case-sensitive.
     * @param expected expected token
     * @return true if the argument equals the current token (ignoring case)
     * @throws IllegalStateException if no token has been read
     * @throws IOException if an I/O error occurs
     */
    public boolean tryMatchIgnoreCase(final String expected) throws IOException {
        return matchInternal(expected, false, false);
    }

    /** Internal method to compare the current token with the argument.
     * @param expected expected token
     * @param caseSensitive if the comparison should be case-sensitive
     * @param throwOnFailure if an exception should be thrown if the argument is not
     *      equal to the current token
     * @return true if the argument is equal to the current token
     * @throws IllegalStateException if no token has been read
     * @throws IOException if {@code expected} does not match the current token and
     *      {@code throwOnFailure} is true
     */
    private boolean matchInternal(final String expected, final boolean caseSensitive,
            final boolean throwOnFailure) throws IOException {
        ensureHasSetToken();

        if (!stringsEqual(expected, currentToken, caseSensitive)) {
            if (throwOnFailure) {
                throw unexpectedToken("[" + expected + "]");
            }

            return false;
        }

        return true;
    }

    /** Return the index of the argument that exactly matches the {@link #getCurrentToken() current token}.
     * An exception is thrown if no match is found. String comparisons are case-sensitive.
     * @param expected strings to compare with the current token
     * @return index of the argument that exactly matches the current token
     * @throws IllegalStateException if no token has been read
     * @throws IOException if no match is found among the arguments
     */
    public int choose(final String... expected) throws IOException {
        return choose(Arrays.asList(expected));
    }

    /** Return the index of the argument that exactly matches the {@link #getCurrentToken() current token}.
     * An exception is thrown if no match is found. String comparisons are case-sensitive.
     * @param expected strings to compare with the current token
     * @return index of the argument that exactly matches the current token
     * @throws IllegalStateException if no token has been read
     * @throws IOException if no match is found among the arguments
     */
    public int choose(final List<String> expected) throws IOException {
        return chooseInternal(expected, true, true);
    }

    /** Return the index of the argument that matches the {@link #getCurrentToken() current token},
     * ignoring case. An exception is thrown if no match is found. String comparisons are <em>not</em>
     * case-sensitive.
     * @param expected strings to compare with the current token
     * @return index of the argument that matches the current token (ignoring case)
     * @throws IllegalStateException if no token has been read
     * @throws IOException if no match is found among the arguments
     */
    public int chooseIgnoreCase(final String... expected) throws IOException {
        return chooseIgnoreCase(Arrays.asList(expected));
    }

    /** Return the index of the argument that matches the {@link #getCurrentToken() current token},
     * ignoring case. An exception is thrown if no match is found. String comparisons are <em>not</em>
     * case-sensitive.
     * @param expected strings to compare with the current token
     * @return index of the argument that matches the current token (ignoring case)
     * @throws IllegalStateException if no token has been read
     * @throws IOException if no match is found among the arguments
     */
    public int chooseIgnoreCase(final List<String> expected) throws IOException {
        return chooseInternal(expected, false, true);
    }

    /** Return the index of the argument that exactly matches the {@link #getCurrentToken() current token}
     * or -1 if no match is found. String comparisons are case-sensitive.
     * @param expected strings to compare with the current token
     * @return index of the argument that exactly matches the current token or -1 if
     *      no match is found
     * @throws IllegalStateException if no token has been read
     * @throws IOException if an I/O error occurs
     */
    public int tryChoose(final String... expected) throws IOException {
        return tryChoose(Arrays.asList(expected));
    }

    /** Return the index of the argument that exactly matches the {@link #getCurrentToken() current token}
     * or -1 if no match is found. String comparisons are case-sensitive.
     * @param expected strings to compare with the current token
     * @return index of the argument that exactly matches the current token or -1 if
     *      no match is found
     * @throws IllegalStateException if no token has been read
     * @throws IOException if an I/O error occurs
     */
    public int tryChoose(final List<String> expected) throws IOException {
        return chooseInternal(expected, true, false);
    }

    /** Return the index of the argument that matches the {@link #getCurrentToken() current token}
     * or -1 if no match is found. String comparisons are <em>not</em> case-sensitive.
     * @param expected strings to compare with the current token
     * @return index of the argument that matches the current token (ignoring case) or -1 if
     *      no match is found
     * @throws IllegalStateException if no token has been read
     * @throws IOException if an I/O error occurs
     */
    public int tryChooseIgnoreCase(final String... expected) throws IOException {
        return tryChooseIgnoreCase(Arrays.asList(expected));
    }

    /** Return the index of the argument that matches the {@link #getCurrentToken() current token}
     * or -1 if no match is found. String comparisons are <em>not</em> case-sensitive.
     * @param expected strings to compare with the current token
     * @return index of the argument that matches the current token (ignoring case) or -1 if
     *      no match is found
     * @throws IllegalStateException if no token has been read
     * @throws IOException is an I/O error occurs
     */
    public int tryChooseIgnoreCase(final List<String> expected) throws IOException {
        return chooseInternal(expected, false, false);
    }

    /** Internal method to compare the current token with a list of possible strings. The index of
     * the matching argument is returned.
     * @param expected strings to compare with the current token
     * @param caseSensitive if the comparisons should be case-sensitive
     * @param throwOnFailure if an exception should be thrown if no match is found
     * @return the index of the matching argument or -1 if no match is found
     * @throws IllegalStateException if no token has been read
     * @throws IOException if no match is found and {@code throwOnFailure} is true
     */
    private int chooseInternal(final List<String> expected, final boolean caseSensitive,
            final boolean throwOnFailure) throws IOException {
        ensureHasSetToken();

        int i = 0;
        for (final String str : expected) {
            if (stringsEqual(str, currentToken, caseSensitive)) {
                return i;
            }

            ++i;
        }

        if (throwOnFailure) {
            throw unexpectedToken("one of " + expected);
        }

        return -1;
    }

    /** Get an exception indicating that the current token was unexpected. The returned
     * exception contains a message with the line number and column of the current token and
     * a description of its value.
     * @param expected string describing what was expected
     * @return exception indicating that the current token was unexpected
     */
    public IOException unexpectedToken(final String expected) {
        return unexpectedToken(expected, null);
    }

    /** Get an exception indicating that the current token was unexpected. The returned
     * exception contains a message with the line number and column of the current token and
     * a description of its value.
     * @param expected string describing what was expected
     * @param cause cause of the error
     * @return exception indicating that the current token was unexpected
     */
    public IOException unexpectedToken(final String expected, final Throwable cause) {

        StringBuilder msg = new StringBuilder();
        msg.append("expected ")
            .append(expected)
            .append(" but found ")
            .append(getCurrentTokenDescription());

        final int line = hasSetToken ? currentTokenLineNumber : lineNumber;
        final int col = hasSetToken ? currentTokenColumnNumber : columnNumber;

        return parseError(line, col, msg.toString(), cause);
    }

    /** Get an exception indicating an error during parsing at the current token position.
     * @param msg error message
     * @return an exception indicating an error during parsing at the current token position
     */
    public IOException tokenError(final String msg) {
        return tokenError(msg, null);
    }

    /** Get an exception indicating an error during parsing at the current token position.
     * @param msg error message
     * @param cause the cause of the error; may be null
     * @return an exception indicating an error during parsing at the current token position
     */
    public IOException tokenError(final String msg, final Throwable cause) {
        final int line = hasSetToken ? currentTokenLineNumber : lineNumber;
        final int col = hasSetToken ? currentTokenColumnNumber : columnNumber;

        return parseError(line, col, msg, cause);
    }

    /** Return an exception indicating an error occurring at the current parser position.
     * @param msg error message
     * @return an exception indicating an error during parsing
     */
    public IOException parseError(final String msg) {
        return parseError(msg, null);
    }

    /** Return an exception indicating an error occurring at the current parser position.
     * @param msg error message
     * @param cause the cause of the error; may be null
     * @return an exception indicating an error during parsing
     */
    public IOException parseError(final String msg, final Throwable cause) {
        return parseError(lineNumber, columnNumber, msg, cause);
    }

    /** Return an exception indicating an error during parsing.
     * @param line line number of the error
     * @param col column number of the error
     * @param msg error message
     * @return an exception indicating an error during parsing
     */
    public IOException parseError(final int line, final int col, final String msg) {
        return parseError(line, col, msg, null);
    }

    /** Return an exception indicating an error during parsing.
     * @param line line number of the error
     * @param col column number of the error
     * @param msg error message
     * @param cause the cause of the error
     * @return an exception indicating an error during parsing
     */
    public IOException parseError(final int line, final int col, final String msg,
            final Throwable cause) {
        final String fullMsg = String.format("Parsing failed at line %d, column %d: %s",
                line, col, msg);
        return createParseError(fullMsg, cause);
    }

    /** Construct a new parse exception instance with the given message and cause. Subclasses
     *  may override this method to provide their own exception types.
     * @param msg error message
     * @param cause error cause
     * @return a new parse exception instance
     */
    protected IOException createParseError(final String msg, final Throwable cause) {
        return new ParseException(msg, cause);
    }

    /** Set the current token string and position.
     * @param line line number for the start of the token
     * @param col column number for the start of the token
     * @param token token to set
     */
    private void setToken(final int line, final int col, final String token) {
        currentTokenLineNumber = line;
        currentTokenColumnNumber = col;
        currentToken = token;

        hasSetToken = true;
    }

    /** Get a user-friendly description of the current token.
     * @return a user-friendly description of the current token.
     */
    private String getCurrentTokenDescription() {
        if (currentToken == null || currentToken.isEmpty()) {
            // attempt to return a more helpful message about the location
            // of empty tokens by checking the buffer content; if this fails
            // we'll ignore the error and continue with a more generic message
            try {
                if (!hasMoreCharacters()) {
                    return "end of content";
                } else if (currentToken != null) {
                    if (!hasMoreCharactersOnLine()) {
                        return "end of line";
                    } else if (currentToken != null) {
                        return "empty token followed by [" + peek(1) + "]";
                    }
                }
            } catch (IOException exc) {
                // ignore
            }
        }

        if (currentToken == null) {
            return "no current token";
        } else if (currentToken.isEmpty()) {
            return "empty token";
        }

        return "[" + currentToken + "]";
    }

    /** Validate the requested string length.
     * @param len requested string length
     * @throws IllegalArgumentException if {@code len} is less than 0 or greater than {@code maxStringLength}
     */
    private void validateRequestedStringLength(final int len) {
        if (len < 0) {
            throw new IllegalArgumentException("Requested string length cannot be negative; was " + len);
        } else if (len > maxStringLength) {
            throw new IllegalArgumentException("Requested string length of " + len + " exceeds maximum value of " +
                    maxStringLength);
        }
    }

    /** Ensure that a token read operation has been performed, throwing an exception if not.
     * @throws IllegalStateException if no token read operation has been performed
     */
    private void ensureHasSetToken() {
        if (!hasSetToken) {
            throw new IllegalStateException("No token has been read from the character stream");
        }
    }

    /** Return true if the given character (Unicode code point) is whitespace.
     * @param ch character (Unicode code point) to test
     * @return true if the given character is whitespace
     * @see Character#isWhitespace(int)
     */
    public static boolean isWhitespace(final int ch) {
        return Character.isWhitespace(ch);
    }

    /** Return true if the given character (Unicode code point) is not whitespace.
     * @param ch character (Unicode code point) to test
     * @return true if the given character is not whitespace
     * @see #isWhitespace(int)
     */
    public static boolean isNotWhitespace(final int ch) {
        return !isWhitespace(ch);
    }

    /** Return true if the given character (Unicode code point) is whitespace
     * that is not used in newline sequences (ie, not '\r' or '\n').
     * @param ch character (Unicode code point) to test
     * @return true if the given character is a whitespace character not used in newline
     *      sequences
     */
    public static boolean isLineWhitespace(final int ch) {
        return isWhitespace(ch) && isNotNewLinePart(ch);
    }

    /** Return true if the given character (Unicode code point) is used
     * as part of newline sequences (ie, is either '\r' or '\n').
     * @param ch character (Unicode code point) to test
     * @return true if the given character is used as part of newline sequences
     */
    public static boolean isNewLinePart(final int ch) {
        return ch == CR || ch == LF;
    }

    /** Return true if the given character (Unicode code point) is not used as
     * part of newline sequences (ie, not '\r' or '\n').
     * @param ch character (Unicode code point) to test
     * @return true if the given character is not used as part of newline sequences
     * @see #isNewLinePart(int)
     */
    public static boolean isNotNewLinePart(final int ch) {
        return !isNewLinePart(ch);
    }

    /** Return true if the given character (Unicode code point) is alphanumeric.
     * @param ch character (Unicode code point) to test
     * @return true if the argument is alphanumeric
     * @see Character#isAlphabetic(int)
     * @see Character#isDigit(int)
     */
    public static boolean isAlphanumeric(final int ch) {
        return Character.isAlphabetic(ch) ||
                Character.isDigit(ch);
    }

    /** Return true if the given character (Unicode code point) is not alphanumeric.
     * @param ch character (Unicode code point) to test
     * @return true if the argument is not alphanumeric
     * @see #isAlphanumeric(int)
     */
    public static boolean isNotAlphanumeric(final int ch) {
        return !isAlphanumeric(ch);
    }

    /** Return true if the given character (Unicode code point) can be used as part of
     * the string representation of an integer. This will be true for the following types
     * of characters:
     * <ul>
     *  <li>{@link Character#isDigit(int) digits}</li>
     *  <li>the '-' (minus) character</li>
     *  <li>the '+' (plus) character</li>
     * </ul>
     * @param ch character (Unicode code point) to test
     * @return true if the given character can be used as part of an integer string
     */
    public static boolean isIntegerPart(final int ch) {
        return Character.isDigit(ch) ||
                ch == '-' ||
                ch == '+';
    }

    /** Return true if the given character (Unicode code point) can be used as part of
     * the string representation of a decimal number. This will be true for the following types
     * of characters:
     * <ul>
     *  <li>{@link Character#isDigit(int) digits}</li>
     *  <li>the '-' (minus) character</li>
     *  <li>the '+' (plus) character</li>
     *  <li>the '.' (period) character</li>
     *  <li>the 'e' character</li>
     *  <li>the 'E' character</li>
     * </ul>
     * @param ch character (Unicode code point) to test
     * @return true if the given character can be used as part of a decimal number string
     */
    public static boolean isDecimalPart(final int ch) {
        return Character.isDigit(ch) ||
            ch == '-' ||
            ch == '+' ||
            ch == '.' ||
            ch == 'e' ||
            ch == 'E';
    }

    /** Test two strings for equality. One or both arguments may be null.
     * @param a first string
     * @param b second string
     * @param caseSensitive comparison is case-sensitive if set to true
     * @return true if the string arguments are considered equal
     */
    private static boolean stringsEqual(final String a, final String b, final boolean caseSensitive) {
        if (a == null) {
            return b == null;
        }

        return caseSensitive ?
                a.equals(b) :
                a.equalsIgnoreCase(b);
    }

    /** Internal class used to collect strings from the character stream while ensuring that the
     * collected strings do not exceed the maximum configured string length.
     */
    private final class StringCollector implements IntPredicate, IntConsumer {

        /** String builder instance. */
        private final StringBuilder sb = new StringBuilder();

        /** Start position line. */
        private final int line;

        /** Start position column. */
        private final int col;

        /** Character predicate. */
        private final IntPredicate pred;

        /** Construct a new instance with the given start position and character predicate.
         * @param line start position line
         * @param col start position col
         * @param pred character predicate
         */
        StringCollector(final int line, final int col, final IntPredicate pred) {
            this.line = line;
            this.col = col;
            this.pred = pred;
        }

        /** {@inheritDoc} */
        @Override
        public boolean test(final int value) {
            return pred.test(value) && !hasExceededMaxStringLength();
        }

        /** {@inheritDoc} */
        @Override
        public void accept(final int value) {
            sb.append((char) value);
        }

        /** Get the string collected by this instance.
         * @return the string collected by this instance
         * @throws IOException if the string exceeds the maximum configured length
         */
        public String getString() throws IOException {
            if (hasExceededMaxStringLength()) {
                throw parseError(line, col, STRING_LENGTH_ERR_MSG + maxStringLength);
            }

            return sb.toString();
        }

        /** Return true if this collector has exceeded the maximum configured string length.
         * @return true if this collector has exceeded the maximum string length
         */
        private boolean hasExceededMaxStringLength() {
            return sb.length() > maxStringLength;
        }
    }

    /** Exception used to indicate a parsing error. */
    private static final class ParseException extends IOException {

        /** Serializable UID. */
        private static final long serialVersionUID = 20210113L;

        /** Construct a new instance with the given message and cause.
         * @param msg exception message
         * @param cause exception cause; may be null
         */
        ParseException(final String msg, final Throwable cause) {
            super(msg, cause);
        }
    }
}
