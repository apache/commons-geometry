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
import java.util.Objects;

/** Class used to buffer characters read from an underlying {@link Reader}.
 * Characters can be consumed from the buffer, examined without being consumed,
 * and pushed back onto the buffer. The internal buffer is resized as needed.
 */
public class CharReadBuffer {

    /** Constant indicating that the end of the input has been reached. */
    private static final int EOF = -1;

    /** Default initial buffer capacity. */
    private static final int DEFAULT_INITIAL_CAPACITY = 512;

    /** Log 2 constant. */
    private static final double LOG2 = Math.log(2);

    /** Underlying reader instance. */
    private final Reader reader;

    /** Character buffer. */
    private char[] buffer;

    /** The index of the head element in the buffer. */
    private int head;

    /** The number of valid elements in the buffer. */
    private int count;

    /** True when the end of reader content is reached. */
    private boolean reachedEof;

    /** Minimum number of characters to request for each read. */
    private final int minRead;

    /** Construct a new instance that buffers characters from the given reader.
     * @param reader underlying reader instance
     * @throws NullPointerException if {@code reader} is null
     */
    public CharReadBuffer(final Reader reader) {
        this(reader, DEFAULT_INITIAL_CAPACITY);
    }

    /** Construct a new instance that buffers characters from the given reader.
     * @param reader underlying reader instance
     * @param initialCapacity the initial capacity of the internal buffer; the buffer
     *      is resized as needed
     * @throws NullPointerException if {@code reader} is null
     * @throws IllegalArgumentException if {@code initialCapacity} is less than one.
     */
    public CharReadBuffer(final Reader reader, final int initialCapacity) {
        this(reader, initialCapacity, (initialCapacity + 1) / 2);
    }

    /** Construct a new instance that buffers characters from the given reader.
     * @param reader underlying reader instance
     * @param initialCapacity the initial capacity of the internal buffer; the buffer
     *      is resized as needed
     * @param minRead the minimum number of characters to request from the reader
     *      when fetching more characters into the buffer; this can be used to limit the
     *      number of calls made to the reader
     * @throws NullPointerException if {@code reader} is null
     * @throws IllegalArgumentException if {@code initialCapacity} or {@code minRead}
     *      are less than one.
     */
    public CharReadBuffer(final Reader reader, final int initialCapacity, final int minRead) {
        Objects.requireNonNull(reader, "Reader cannot be null");
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("Initial buffer capacity must be greater than 0; was " +
                    initialCapacity);
        }
        if (minRead < 1) {
            throw new IllegalArgumentException("Min read value must be greater than 0; was " +
                    minRead);
        }

        this.reader = reader;
        this.buffer = new char[initialCapacity];
        this.minRead = minRead;
    }

    /** Return true if more characters are available from the read buffer.
     * @return true if more characters are available from the read buffer
     * @throws IOException if an I/O error occurs
     */
    public boolean hasMoreCharacters() throws IOException {
        return makeAvailable(1) > 0;
    }

    /** Attempt to make at least {@code n} characters available in the buffer, reading
     * characters from the underlying reader as needed. The number of characters available
     * is returned.
     * @param n number of characters requested to be available
     * @return number of characters available for immediate use in the buffer
     * @throws IOException if an I/O error occurs
     */
    public int makeAvailable(final int n) throws IOException {
        final int diff = n - count;
        if (diff > 0) {
            readChars(diff);
        }
        return count;
    }

    /** Remove and return the next character in the buffer.
     * @return the next character in the buffer or {@value #EOF}
     *      if the end of the content has been reached
     * @throws IOException if an I/O error occurs
     * @see #peek()
     */
    public int read() throws IOException {
        final int result = peek();
        charsRemoved(1);

        return result;
    }

    /** Remove and return a string from the buffer. The length of the string will be
     * the number of characters available in the buffer up to {@code len}. Null is
     * returned if no more characters are available.
     * @param len requested length of the string
     * @return a string from the read buffer or null if no more characters are available
     * @throws IllegalArgumentException if {@code len} is less than 0
     * @throws IOException if an I/O error occurs
     * @see #peekString(int)
     */
    public String readString(final int len) throws IOException {
        final String result = peekString(len);
        if (result != null) {
            charsRemoved(result.length());
        }

        return result;
    }

    /** Return the next character in the buffer without removing it.
     * @return the next character in the buffer or {@value #EOF}
     *      if the end of the content has been reached
     * @throws IOException if an I/O error occurs
     * @see #read()
     */
    public int peek() throws IOException {
        if (makeAvailable(1) < 1) {
            return EOF;
        }
        return buffer[head];
    }

    /** Return a string from the buffer without removing it. The length of the string will be
     * the number of characters available in the buffer up to {@code len}. Null is
     * returned if no more characters are available.
     * @param len requested length of the string
     * @return a string from the read buffer or null if no more characters are available
     * @throws IllegalArgumentException if {@code len} is less than 0
     * @throws IOException if an I/O error occurs
     * @see #readString(int)
     */
    public String peekString(final int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("Requested string length cannot be negative; was " + len);
        } else if (len == 0) {
            return hasMoreCharacters() ?
                    "" :
                    null;
        }

        final int available = makeAvailable(len);
        final int resultLen = Math.min(len, available);
        if (resultLen < 1) {
            return null;
        }

        final int contiguous = Math.min(buffer.length - head, resultLen);
        final int remaining = resultLen - contiguous;

        String result = String.valueOf(buffer, head, contiguous);
        if (remaining > 0) {
            result += String.valueOf(buffer, 0, remaining);
        }

        return result;
    }

    /** Get the character at the given buffer index or {@value #EOF} if the index
     * is past the end of the content. The character is not removed from the buffer.
     * @param index index of the character to receive relative to the buffer start
     * @return the character at the given index of {@code -1} if the character is
     *      past the end of the stream content
     * @throws IOException if an I/O exception occurs
     */
    public int charAt(final int index) throws IOException {
        if (index < 0) {
            throw new IllegalArgumentException("Character index cannot be negative; was " + index);
        }
        final int requiredSize = index + 1;
        if (makeAvailable(requiredSize) < requiredSize) {
            return EOF;
        }

        return buffer[(head + index) % buffer.length];
    }

    /** Skip {@code n} characters from the stream. Characters are first skipped from the buffer
     * and then from the underlying reader using {@link Reader#skip(long)} if needed.
     * @param n number of character to skip
     * @return the number of characters skipped
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if {@code n} is negative
     */
    public int skip(final int n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("Character skip count cannot be negative; was " + n);
        }

        // skip buffered content first
        int skipped = Math.min(n, count);
        charsRemoved(skipped);

        // skip from the reader if required
        final int remaining = n - skipped;
        if (remaining > 0) {
            skipped += reader.skip(remaining);
        }

        return skipped;
    }

    /** Push a character back onto the read buffer. The argument will
     * be the next character returned by {@link #read()} or {@link #peek()}.
     * @param ch character to push onto the read buffer
     */
    public void push(final char ch) {
        ensureCapacity(count + 1);
        pushCharInternal(ch);
    }

    /** Push a string back onto the read buffer. The first character
     * of the string will be the next character returned by
     * {@link #read()} or {@link #peek()}.
     * @param str string to push onto the read buffer
     */
    public void pushString(final String str) {
        final int len = str.length();

        ensureCapacity(count + len);
        for (int i = len - 1; i >= 0; --i) {
            pushCharInternal(str.charAt(i));
        }
    }

    /** Internal method to push a single character back onto the read
     * buffer. The buffer capacity is <em>not</em> checked.
     * @param ch character to push onto the read buffer
     */
    private void pushCharInternal(final char ch) {
        charsPushed(1);
        buffer[head] = ch;
    }

    /** Read characters from the underlying character stream into
     * the internal buffer.
     * @param n minimum number of characters requested to be placed
     *      in the buffer
     * @throws IOException if an I/O error occurs
     */
    private void readChars(final int n) throws IOException {
        if (!reachedEof) {
            int remaining = Math.max(n, minRead);

            ensureCapacity(count + remaining);

            int tail;
            int len;
            int read;
            while (remaining > 0) {
                tail = (head + count) % buffer.length;
                len = Math.min(buffer.length - tail, remaining);

                read = reader.read(buffer, tail, len);
                if (read == EOF) {
                    reachedEof = true;
                    break;
                }

                charsAppended(read);
                remaining -= read;
            }
        }
    }

    /** Method called to indicate that characters have been removed from
     * the front of the read buffer.
     * @param n number of characters removed
     */
    private void charsRemoved(final int n) {
        head = (head + n) % buffer.length;
        count -= n;
    }

    /** Method called to indicate that characters have been pushed to
     * the front of the read buffer.
     * @param n number of characters pushed
     */
    private void charsPushed(final int n) {
        head = (head + buffer.length - n) % buffer.length;
        count += n;
    }

    /** Method called to indicate that characters have been appended
     * to the end of the read buffer.
     * @param n number of characters appended
     */
    private void charsAppended(final int n) {
        count += n;
    }

    /** Ensure that the current buffer has at least {@code capacity}
     * number of elements. The number of content elements in the buffer
     * is not changed.
     * @param capacity the minimum required capacity of the buffer
     */
    private void ensureCapacity(final int capacity) {
        if (capacity > buffer.length) {
            final double newCapacityPower = Math.ceil(Math.log(capacity) / LOG2);
            final int newCapacity = (int) Math.pow(2, newCapacityPower);

            final char[] newBuffer = new char[newCapacity];

            final int contiguousCount = Math.min(count, buffer.length - head);
            System.arraycopy(buffer, head, newBuffer, 0, contiguousCount);

            if (contiguousCount < count) {
                System.arraycopy(buffer, 0, newBuffer, contiguousCount, count - contiguousCount);
            }

            buffer = newBuffer;
            head = 0;
        }
    }
}
