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
import java.io.StringReader;
import java.util.Random;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CharReadBufferTest {

    @Test
    void testCtor() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            new CharReadBuffer(null, 1, 1);
        }, NullPointerException.class, "Reader cannot be null");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            new CharReadBuffer(reader("a"), 0, 1);
        }, IllegalArgumentException.class, "Initial buffer capacity must be greater than 0; was 0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            new CharReadBuffer(reader("a"), 1, 0);
        }, IllegalArgumentException.class, "Min read value must be greater than 0; was 0");
    }

    @Test
    void testHasMoreCharacters() throws IOException {
        // act/assert
        for (int s = 1; s < 10; s += 2) {
            Assertions.assertFalse(new CharReadBuffer(reader("")).hasMoreCharacters());
            Assertions.assertFalse(new CharReadBuffer(reader(""), s).hasMoreCharacters());
            Assertions.assertFalse(new CharReadBuffer(reader(""), s, s).hasMoreCharacters());
        }

        String str;
        for (int i = 1; i < 10; ++i) {
            str = repeat("a", i);

            for (int s = 1; s < 10; s += 2) {
                Assertions.assertTrue(new CharReadBuffer(reader(str)).hasMoreCharacters());
                Assertions.assertTrue(new CharReadBuffer(reader(str), s).hasMoreCharacters());
                Assertions.assertTrue(new CharReadBuffer(reader(str), s, s).hasMoreCharacters());
            }
        }
    }

    @Test
    void testPeekRead() throws IOException {
        // arrange
        final String str = "abcdefg";
        final CharReadBuffer buf = new CharReadBuffer(reader(str), 1);

        final StringBuilder peek = new StringBuilder();
        final StringBuilder read = new StringBuilder();

        // act
        while (buf.hasMoreCharacters()) {
            peek.append((char) buf.peek());
            read.append((char) buf.read());
        }

        // assert
        Assertions.assertEquals(str, peek.toString());
        Assertions.assertEquals(str, read.toString());

        Assertions.assertEquals(-1, buf.peek());
        Assertions.assertEquals(-1, buf.read());
    }

    @Test
    void testCharAt() throws IOException {
        // arrange
        final String str = "abcdefgh";
        final CharReadBuffer buf = new CharReadBuffer(reader(str), 3);

        // act/assert
        Assertions.assertEquals('a', buf.charAt(0));
        Assertions.assertEquals('b', buf.charAt(1));
        Assertions.assertEquals('c', buf.charAt(2));
        Assertions.assertEquals('d', buf.charAt(3));
        Assertions.assertEquals('e', buf.charAt(4));
        Assertions.assertEquals('f', buf.charAt(5));
        Assertions.assertEquals('g', buf.charAt(6));
        Assertions.assertEquals('h', buf.charAt(7));

        Assertions.assertEquals(-1, buf.charAt(8));
        Assertions.assertEquals(-1, buf.charAt(9));
        Assertions.assertEquals(-1, buf.charAt(10));
    }

    @Test
    void testCharAt_invalidArg() throws IOException {
        // arrange
        final String str = "abcdefgh";
        final CharReadBuffer buf = new CharReadBuffer(reader(str), 3);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            buf.charAt(-1);
        }, IllegalArgumentException.class, "Character index cannot be negative; was -1");
    }

    @Test
    void testReadPeek_string() throws IOException {
        // arrange
        final String str = "abcdefgh";
        final CharReadBuffer buf = new CharReadBuffer(reader(str), 50);

        // act/assert
        Assertions.assertEquals("", buf.peekString(0));
        Assertions.assertEquals("", buf.readString(0));

        Assertions.assertEquals("abc", buf.peekString(3));
        Assertions.assertEquals("abc", buf.readString(3));

        Assertions.assertEquals("defgh", buf.peekString(100));
        Assertions.assertEquals("defgh", buf.readString(100));

        Assertions.assertEquals(null, buf.peekString(1));
        Assertions.assertEquals(null, buf.readString(1));
    }

    @Test
    void testReadPeek_tring_zeroLen() throws IOException {
        // act/assert
        Assertions.assertNull(new CharReadBuffer(reader("")).peekString(0));
        Assertions.assertNull(new CharReadBuffer(reader("")).readString(0));

        Assertions.assertEquals("", new CharReadBuffer(reader("a")).peekString(0));
        Assertions.assertEquals("", new CharReadBuffer(reader("a")).readString(0));
    }

    @Test
    void testReadPeek_string_invalidArg() throws IOException {
        // arrange
        final CharReadBuffer buf = new CharReadBuffer(reader("a"));
        final String msg = "Requested string length cannot be negative; was -1";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            buf.peekString(-1);
        }, IllegalArgumentException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            buf.readString(-1);
        }, IllegalArgumentException.class, msg);
    }

    @Test
    void testSkip() throws IOException {
        // arrange
        final CharReadBuffer buf = new CharReadBuffer(reader("abcdefg"), 3);
        buf.peekString(2);

        // act/assert
        Assertions.assertEquals(0, buf.skip(0));
        Assertions.assertEquals("a", buf.peekString(1));

        Assertions.assertEquals(1, buf.skip(1));
        Assertions.assertEquals("b", buf.peekString(1));

        Assertions.assertEquals(4, buf.skip(4));
        Assertions.assertEquals("f", buf.peekString(1));

        Assertions.assertEquals(1, buf.skip(1));
        Assertions.assertEquals("g", buf.peekString(1));

        Assertions.assertEquals(1, buf.skip(100));
        Assertions.assertNull(buf.peekString(1));

        Assertions.assertEquals(0, buf.skip(100));
        Assertions.assertNull(buf.peekString(1));
    }

    @Test
    void testSkip_invalidArg() throws IOException {
        // arrange
        final CharReadBuffer buf = new CharReadBuffer(reader("a"));

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            buf.skip(-1);
        }, IllegalArgumentException.class, "Character skip count cannot be negative; was -1");
    }

    @Test
    void testPushString_emptyReader() throws IOException {
        // arrange
        final String a = "abcd";
        final String b = "efgh";
        final CharReadBuffer buf = new CharReadBuffer(reader(""), 1);

        // act
        buf.pushString(a);
        buf.pushString(b);

        // assert
        Assertions.assertTrue(buf.hasMoreCharacters());
        Assertions.assertEquals("efghabcd", buf.readString(8));
    }

    @Test
    void testPushString_nonEmptyReader() throws IOException {
        // arrange
        final String a = "abcd";
        final String b = "efgh";
        final CharReadBuffer buf = new CharReadBuffer(reader("ABCD"), 1);

        // act
        buf.pushString(a);
        buf.pushString(b);

        // assert
        Assertions.assertTrue(buf.hasMoreCharacters());
        Assertions.assertEquals("efghabcdABCD", buf.readString(12));
    }

    @Test
    void testPush_emptyReader() throws IOException {
        // arrange
        final CharReadBuffer buf = new CharReadBuffer(reader("ABCD"), 1);

        // act
        buf.push('a');
        buf.push('b');
        buf.push('c');
        buf.push('d');

        // assert
        Assertions.assertTrue(buf.hasMoreCharacters());
        Assertions.assertEquals("dcbaABCD", buf.readString(8));
    }

    @Test
    void testAlternatingPushAndRead() throws IOException {
        // arrange
        final String str = repeat("abcdefghijlmnopqrstuvwxyz", 10);

        final CharReadBuffer buf = new CharReadBuffer(reader(str), 8);

        final Random rnd = new Random(1L);

        // act
        final StringBuilder result = new StringBuilder();
        String tmp;
        while (buf.hasMoreCharacters()) {
            buf.pushString("ABC");

            tmp = buf.readString(rnd.nextInt(10) + 4);

            result.append(tmp.charAt(3));

            buf.pushString(tmp.substring(4));
        }

        // assert
        Assertions.assertEquals(str, result.toString());
    }

    private static Reader reader(final String content) {
        return new StringReader(content);
    }

    private static String repeat(final String str, final int count) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            sb.append(str);
        }

        return sb.toString();
    }
}
