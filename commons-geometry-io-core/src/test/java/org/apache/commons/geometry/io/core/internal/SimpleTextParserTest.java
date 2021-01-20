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
import java.util.function.IntPredicate;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleTextParserTest {

    private static final double EPS = 1e-20;

    private static final int EOF = -1;

    @Test
    public void testMaxStringLength_defaultValue() {
        // arrange
        final SimpleTextParser p = parser("abc");

        // act/assert
        Assertions.assertEquals(1024, p.getMaxStringLength());
    }

    @Test
    public void testMaxStringLength_illegalArg() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.setMaxStringLength(-1);
        }, IllegalArgumentException.class, "Maximum string length cannot be less than zero; was -1");
    }

    @Test
    public void testCharacterSequence() throws IOException {
        // act/assert
        assertCharacterSequence(parser(""), "");
        assertCharacterSequence(parser("abc def"), "abc def");
    }

    @Test
    public void testCharacterPosition() throws IOException {
        // arrange
        final SimpleTextParser p = parser(
                "a b\n" +
                "\r\n" +
                "d \r" +
                "e");

        // act/assert
        assertPosition(p, 1, 1);
        assertChar('a', p.readChar());

        assertPosition(p, 1, 2);
        assertChar(' ', p.readChar());

        assertPosition(p, 1, 3);
        assertChar('b', p.readChar());

        assertPosition(p, 1, 4);
        assertChar('\n', p.readChar());

        assertPosition(p, 2, 1);
        assertChar('\r', p.readChar());

        assertPosition(p, 2, 2);
        assertChar('\n', p.readChar());

        assertPosition(p, 3, 1);
        assertChar('d', p.readChar());

        assertPosition(p, 3, 2);
        assertChar(' ', p.readChar());

        assertPosition(p, 3, 3);
        assertChar('\r', p.readChar());

        assertPosition(p, 4, 1);
        assertChar('e', p.readChar());

        assertPosition(p, 4, 2);
        assertChar(EOF, p.readChar());
    }

    @Test
    public void testCharacterPosition_givenPosition() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc\rdef");

        // act/assert
        assertPosition(p, 1, 1);

        p.setLineNumber(10);
        p.setColumnNumber(3);

        assertPosition(p, 10, 3);

        p.discard(4);

        assertPosition(p, 11, 1);

        p.discard(3);

        assertPosition(p, 11, 4);
    }

    @Test
    public void testHasMoreCharacters() throws IOException {
        // arrange
        final SimpleTextParser empty = parser("");
        final SimpleTextParser nonEmpty = parser("a");

        // act/assert
        Assertions.assertFalse(empty.hasMoreCharacters());

        Assertions.assertTrue(nonEmpty.hasMoreCharacters());
        assertChar('a', nonEmpty.readChar());
        Assertions.assertFalse(nonEmpty.hasMoreCharacters());
    }

    @Test
    public void testHasMoreCharactersOnLine() throws IOException {
        // arrange
        final SimpleTextParser empty = parser("");
        final SimpleTextParser singleLine = parser("a");
        final SimpleTextParser multiLine = parser("a\r\nb\rc\n\n");

        // act/assert
        Assertions.assertFalse(empty.hasMoreCharactersOnLine());

        Assertions.assertTrue(singleLine.hasMoreCharactersOnLine());
        assertChar('a', singleLine.readChar());
        Assertions.assertFalse(singleLine.hasMoreCharactersOnLine());

        Assertions.assertTrue(multiLine.hasMoreCharactersOnLine());
        assertChar('a', multiLine.readChar());

        Assertions.assertFalse(multiLine.hasMoreCharactersOnLine());
        assertChar('\r', multiLine.readChar());

        Assertions.assertFalse(multiLine.hasMoreCharactersOnLine());
        assertChar('\n', multiLine.readChar());

        Assertions.assertTrue(multiLine.hasMoreCharactersOnLine());
        assertChar('b', multiLine.readChar());

        Assertions.assertFalse(multiLine.hasMoreCharactersOnLine());
        assertChar('\r', multiLine.readChar());

        Assertions.assertTrue(multiLine.hasMoreCharactersOnLine());
        assertChar('c', multiLine.readChar());

        Assertions.assertFalse(multiLine.hasMoreCharactersOnLine());
        assertChar('\n', multiLine.readChar());

        Assertions.assertFalse(multiLine.hasMoreCharactersOnLine());
        assertChar('\n', multiLine.readChar());

        Assertions.assertFalse(multiLine.hasMoreCharactersOnLine());
        assertChar(EOF, multiLine.readChar());
    }

    @Test
    public void testBasicTokenMethods() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef\r\n\r ghi");

        // act/assert
        assertToken(p, null, -1, -1);
        Assertions.assertFalse(p.hasNonEmptyToken());

        assertToken(p.next(1), "a", 1, 1);
        Assertions.assertTrue(p.hasNonEmptyToken());

        assertToken(p.next(3), "bcd", 1, 2);
        Assertions.assertTrue(p.hasNonEmptyToken());

        assertToken(p.next(5), "ef\r\n\r", 1, 5);
        Assertions.assertTrue(p.hasNonEmptyToken());

        assertToken(p.next(0), "", 3, 1);
        Assertions.assertFalse(p.hasNonEmptyToken());

        assertToken(p.next(1), " ", 3, 1);
        Assertions.assertTrue(p.hasNonEmptyToken());

        assertToken(p.next(3), "ghi", 3, 2);
        Assertions.assertTrue(p.hasNonEmptyToken());

        assertToken(p.next(1), null, 3, 5);
        Assertions.assertFalse(p.hasNonEmptyToken());
    }

    @Test
    public void testGetCurrentTokenAsDouble() throws IOException {
        // arrange
        final SimpleTextParser p = parser("1e-4\n+5\n-4.001");

        // act/assert
        p.nextLine();
        Assertions.assertEquals(1e-4, p.getCurrentTokenAsDouble(), EPS);

        p.nextLine();
        Assertions.assertEquals(5.0, p.getCurrentTokenAsDouble(), EPS);

        p.nextLine();
        Assertions.assertEquals(-4.001, p.getCurrentTokenAsDouble(), EPS);
    }

    @Test
    public void testGetCurrentTokenAsDouble_failures() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc\n1.1.1a");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsDouble();
        }, IllegalStateException.class, "No token has been read from the character stream");

        p.next(SimpleTextParser::isNotNewLinePart);
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsDouble();
        }, IOException.class,
                "Parsing failed at line 1, column 1: expected double but found [abc]");

        p.nextAlphanumeric();
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsDouble();
        }, IOException.class,
                "Parsing failed at line 1, column 4: expected double but found end of line");

        p.discardLine()
            .next(c -> c != 'a');
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsDouble();
        }, IOException.class,
                "Parsing failed at line 2, column 1: expected double but found [1.1.1]");

        p.next(Character::isDigit);
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsDouble();
        }, IOException.class,
                "Parsing failed at line 2, column 6: expected double but found empty token followed by [a]");

        p.nextLine();
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsDouble();
        }, IOException.class,
                "Parsing failed at line 2, column 6: expected double but found [a]");

        p.nextLine();
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsDouble();
        }, IOException.class,
                "Parsing failed at line 2, column 7: expected double but found end of content");
    }

    @Test
    public void testGetCurrentTokenAsDouble_includedNumberFormatExceptionOnFailure() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");
        p.nextLine();

        // act/assert
        final Throwable exc = Assertions.assertThrows(IOException.class, () -> p.getCurrentTokenAsDouble());
        Assertions.assertEquals(NumberFormatException.class, exc.getCause().getClass());
    }

    @Test
    public void testGetCurrentTokenAsInt() throws IOException {
        // arrange
        final SimpleTextParser p = parser("0\n+5\n-401");

        // act/assert
        p.nextLine();
        Assertions.assertEquals(0, p.getCurrentTokenAsInt());

        p.nextLine();
        Assertions.assertEquals(5, p.getCurrentTokenAsInt());

        p.nextLine();
        Assertions.assertEquals(-401, p.getCurrentTokenAsInt());
    }

    @Test
    public void testGetCurrentTokenAsInt_failures() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc\n1.1.1a");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsInt();
        }, IllegalStateException.class, "No token has been read from the character stream");

        p.next(SimpleTextParser::isNotNewLinePart);
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsInt();
        }, IOException.class,
                "Parsing failed at line 1, column 1: expected integer but found [abc]");

        p.nextAlphanumeric();
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsInt();
        }, IOException.class,
                "Parsing failed at line 1, column 4: expected integer but found end of line");

        p.discardLine()
            .next(c -> c != 'a');
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsInt();
        }, IOException.class,
                "Parsing failed at line 2, column 1: expected integer but found [1.1.1]");

        p.next(Character::isDigit);
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsInt();
        }, IOException.class,
                "Parsing failed at line 2, column 6: expected integer but found empty token followed by [a]");

        p.nextLine();
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsInt();
        }, IOException.class,
                "Parsing failed at line 2, column 6: expected integer but found [a]");

        p.nextLine();
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.getCurrentTokenAsInt();
        }, IOException.class,
                "Parsing failed at line 2, column 7: expected integer but found end of content");
    }

    @Test
    public void testGetCurrentTokenAsInt_includedNumberFormatExceptionOnFailure() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");
        p.nextLine();

        // act/assert
        final Throwable exc = Assertions.assertThrows(IOException.class, () -> p.getCurrentTokenAsInt());
        Assertions.assertEquals(NumberFormatException.class, exc.getCause().getClass());
    }

    @Test
    public void testNext_lenArg() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef\r\n\r ghi");

        // act/assert
        assertToken(p.next(0), "", 1, 1);
        assertToken(p.next(4), "abcd", 1, 1);
        assertToken(p.next(6), "ef\r\n\r ", 1, 5);
        assertToken(p.next(100), "ghi", 3, 2);

        assertToken(p.next(0), null, 3, 5);
        assertToken(p.next(100), null, 3, 5);
    }

    @Test
    public void testNextWithLineContinuation_lenArg() throws IOException {
        // arrange
        final char cont = '\\';
        final SimpleTextParser p = parser("a\\bcdef\\\r\n\r ghi\\\n\\\n\\\rj");

        // act/assert
        assertToken(p.nextWithLineContinuation(cont, 0), "", 1, 1);
        assertToken(p.nextWithLineContinuation(cont, 5), "a\\bcd", 1, 1);
        assertToken(p.nextWithLineContinuation(cont, 3), "ef\r", 1, 6);
        assertToken(p.nextWithLineContinuation(cont, 100), " ghij", 3, 1);

        assertToken(p.nextWithLineContinuation(cont, 0), null, 6, 2);
        assertToken(p.nextWithLineContinuation(cont, 100), null, 6, 2);
    }

    @Test
    public void testNext_lenArg_invalidArg() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");
        p.setMaxStringLength(2);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.next(-1);
        }, IllegalArgumentException.class, "Requested string length cannot be negative; was -1");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.next(3);
        }, IllegalArgumentException.class, "Requested string length of 3 exceeds maximum value of 2");
    }

    @Test
    public void testNext_predicateArg() throws IOException {
        // arrange
        final SimpleTextParser p = parser("a\n 012\r\ndef");

        // act/assert
        assertToken(p.next(c -> false), "", 1, 1);

        assertToken(p.next(Character::isAlphabetic), "a", 1, 1);
        assertToken(p.next(Character::isAlphabetic), "", 1, 2);

        assertToken(p.next(Character::isWhitespace), "\n ", 1, 2);
        assertToken(p.next(Character::isWhitespace), "", 2, 2);

        assertToken(p.next(Character::isDigit), "012", 2, 2);
        assertToken(p.next(Character::isDigit), "", 2, 5);

        assertToken(p.next(Character::isWhitespace), "\r\n", 2, 5);
        assertToken(p.next(Character::isWhitespace), "", 3, 1);

        assertToken(p.next(c -> true), "def", 3, 1);
        assertToken(p.next(c -> true), null, 3, 4);
    }

    @Test
    public void testNext_predicateArg_exceedsMaxStringLength() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef");
        p.setMaxStringLength(4);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.next(c -> !Character.isWhitespace(c));
        }, IllegalStateException.class, "String length exceeds maximum value of 4");
    }

    @Test
    public void testNextWithLineContinuation_predicateArg() throws IOException {
        // arrange
        final char cont = '|';
        final SimpleTextParser p = parser("|\na\n 0|\r\n|\r12\r\nd|ef");

        // act/assert
        assertToken(p.nextWithLineContinuation(cont, c -> false), "", 1, 1);

        assertToken(p.nextWithLineContinuation(cont, Character::isAlphabetic), "a", 2, 1);
        assertToken(p.nextWithLineContinuation(cont, Character::isAlphabetic), "", 2, 2);

        assertToken(p.nextWithLineContinuation(cont, Character::isWhitespace), "\n ", 2, 2);
        assertToken(p.nextWithLineContinuation(cont, Character::isWhitespace), "", 3, 2);

        assertToken(p.nextWithLineContinuation(cont, Character::isDigit), "012", 3, 2);
        assertToken(p.nextWithLineContinuation(cont, Character::isDigit), "", 5, 3);

        assertToken(p.nextWithLineContinuation(cont, Character::isWhitespace), "\r\n", 5, 3);
        assertToken(p.nextWithLineContinuation(cont, Character::isWhitespace), "", 6, 1);

        assertToken(p.nextWithLineContinuation(cont, c -> true), "d|ef", 6, 1);
        assertToken(p.nextWithLineContinuation(cont, c -> true), null, 6, 5);
    }

    @Test
    public void testNextLine() throws IOException {
        // arrange
        final SimpleTextParser p = parser("a\n 012\r\ndef\n\nx");

        // act/assert
        assertToken(p.nextLine(), "a", 1, 1);

        assertToken(p.nextLine(), " 012", 2, 1);

        p.readChar();
        assertToken(p.nextLine(), "ef", 3, 2);

        assertToken(p.nextLine(), "", 4, 1);

        assertToken(p.nextLine(), "x", 5, 1);
        assertToken(p.nextLine(), null, 5, 2);
    }

    @Test
    public void testNextAlphanumeric() throws IOException {
        // arrange
        final SimpleTextParser p = parser("a10Fd;X23456789-0\ny");

        // act/assert
        assertToken(p.nextAlphanumeric(), "a10Fd", 1, 1);

        assertChar(';', p.readChar());
        assertToken(p.nextAlphanumeric(), "X23456789", 1, 7);

        assertChar('-', p.readChar());
        assertToken(p.nextAlphanumeric(), "0", 1, 17);

        assertToken(p.nextAlphanumeric(), "", 1, 18);

        assertChar('\n', p.readChar());
        assertToken(p.nextAlphanumeric(), "y", 2, 1);

        assertToken(p.nextAlphanumeric(), null, 2, 2);
    }

    @Test
    public void testDiscard_lenArg() throws IOException {
        // arrange
        final SimpleTextParser p = parser("\na,b c\r\n12.3\rdef\n");

        // act/assert
        p.discard(0);
        assertChar('\n', p.peekChar());
        assertPosition(p, 1, 1);

        p.discard(1);
        assertChar('a', p.peekChar());
        assertPosition(p, 2, 1);

        p.discard(8);
        assertChar('2', p.peekChar());
        assertPosition(p, 3, 2);

        p.discard(100);
        assertChar(EOF, p.peekChar());
        assertPosition(p, 5, 1);

        p.discard(0);
        assertChar(EOF, p.peekChar());
        assertPosition(p, 5, 1);

        p.discard(100);
        assertChar(EOF, p.peekChar());
        assertPosition(p, 5, 1);
    }

    @Test
    public void testDiscardWithLineContinuation_lenArg() throws IOException {
        // arrange
        final char cont = '|';
        final SimpleTextParser p = parser("\n|a|\r\n,b|\n|\r c\r\n12.3\rdef\n");

        // act/assert
        p.discardWithLineContinuation(cont, 0);
        assertChar('\n', p.peekChar());
        assertPosition(p, 1, 1);

        p.discardWithLineContinuation(cont, 1);
        assertChar('|', p.peekChar());
        assertPosition(p, 2, 1);

        p.discardWithLineContinuation(cont, 8);
        assertChar('1', p.peekChar());
        assertPosition(p, 6, 1);

        p.discardWithLineContinuation(cont, 100);
        assertChar(EOF, p.peekChar());
        assertPosition(p, 8, 1);

        p.discardWithLineContinuation(cont, 0);
        assertChar(EOF, p.peekChar());
        assertPosition(p, 8, 1);

        p.discardWithLineContinuation(cont, 100);
        assertChar(EOF, p.peekChar());
        assertPosition(p, 8, 1);
    }

    @Test
    public void testDiscard_predicateArg() throws IOException {
        // arrange
        final SimpleTextParser p = parser("\na,b c\r\n12.3\rdef\n");

        // act/assert
        p.discard(c -> Character.isWhitespace(c));
        assertChar('a', p.peekChar());
        assertPosition(p, 2, 1);

        p.discard(c -> !Character.isWhitespace(c));
        assertChar(' ', p.peekChar());
        assertPosition(p, 2, 4);

        p.discard(c -> Character.isDigit(c)); // should not advance
        assertChar(' ', p.peekChar());
        assertPosition(p, 2, 4);

        p.discard(c -> Character.isWhitespace(c));
        assertChar('c', p.peekChar());
        assertPosition(p, 2, 5);

        p.discard(c -> c != 'd');
        assertChar('d', p.peekChar());
        assertPosition(p, 4, 1);

        p.discard(c -> true);
        assertChar(EOF, p.peekChar());
        assertPosition(p, 5, 1);

        p.discard(c -> true);
        assertChar(EOF, p.peekChar());
        assertPosition(p, 5, 1);
    }

    @Test
    public void testDiscardWithLineContinuation_predicateArg() throws IOException {
        // arrange
        final char cont = '|';
        final SimpleTextParser p = parser("\na,|\r\nb |c\r\n1|\r|\n2.3\rdef\n");

        // act/assert
        p.discardWithLineContinuation(cont, c -> Character.isWhitespace(c));
        assertChar('a', p.peekChar());
        assertPosition(p, 2, 1);

        p.discardWithLineContinuation(cont, c -> !Character.isWhitespace(c));
        assertChar(' ', p.peekChar());
        assertPosition(p, 3, 2);

        p.discardWithLineContinuation(cont, c -> Character.isDigit(c)); // should not advance
        assertChar(' ', p.peekChar());
        assertPosition(p, 3, 2);

        p.discardWithLineContinuation(cont, c -> Character.isWhitespace(c));
        assertChar('|', p.peekChar());
        assertPosition(p, 3, 3);

        p.discardWithLineContinuation(cont, c -> c != 'd');
        assertChar('d', p.peekChar());
        assertPosition(p, 7, 1);

        p.discardWithLineContinuation(cont, c -> true);
        assertChar(EOF, p.peekChar());
        assertPosition(p, 8, 1);

        p.discardWithLineContinuation(cont, c -> true);
        assertChar(EOF, p.peekChar());
        assertPosition(p, 8, 1);
    }

    @Test
    public void testDiscardWhitespace() throws IOException {
        // arrange
        final SimpleTextParser p = parser("a\t\n\r\n   b c");

        // act/assert
        p.discardWhitespace();
        assertPosition(p, 1, 1);
        assertChar('a', p.readChar());

        p.discardWhitespace();
        assertPosition(p, 3, 4);
        assertChar('b', p.readChar());

        p.discardWhitespace();
        assertPosition(p, 3, 6);
        assertChar('c', p.readChar());

        p.discardWhitespace();
        assertPosition(p, 3, 7);
        assertChar(EOF, p.readChar());
    }

    @Test
    public void testDiscardLineWhitespace() throws IOException {
        // arrange
        final SimpleTextParser p = parser("a\t\n\r\n   b c");

        // act/assert
        p.discardLineWhitespace();
        assertPosition(p, 1, 1);
        assertChar('a', p.readChar());

        p.discardLineWhitespace();
        assertPosition(p, 1, 3);
        assertChar('\n', p.peekChar());

        p.discardLineWhitespace();
        assertPosition(p, 1, 3);
        assertChar('\n', p.readChar());

        p.discardLineWhitespace();
        assertPosition(p, 2, 1);
        assertChar('\r', p.readChar());

        p.discardLineWhitespace();
        assertPosition(p, 2, 2);
        assertChar('\n', p.readChar());

        p.discardLineWhitespace();
        assertPosition(p, 3, 4);
        assertChar('b', p.readChar());

        p.discardLineWhitespace();
        assertPosition(p, 3, 6);
        assertChar('c', p.readChar());

        p.discardLineWhitespace();
        assertPosition(p, 3, 7);
        assertChar(EOF, p.readChar());
    }

    @Test
    public void testDiscardNewLineSequence() throws IOException {
        // arrange
        final SimpleTextParser p = parser("a\t\n\r\n   b\rc");

        // act/assert
        p.discardNewLineSequence();
        assertPosition(p, 1, 1);
        assertChar('a', p.readChar());

        p.discardLineWhitespace();

        p.discardNewLineSequence();
        assertPosition(p, 2, 1);
        assertChar('\r', p.readChar());

        p.discardNewLineSequence();
        assertPosition(p, 3, 1);
        assertChar(' ', p.readChar());

        p.discardWhitespace();

        p.discardNewLineSequence();
        assertPosition(p, 3, 4);
        assertChar('b', p.readChar());

        p.discardNewLineSequence();
        assertPosition(p, 4, 1);
        assertChar('c', p.readChar());

        p.discardNewLineSequence();
        assertPosition(p, 4, 2);
        assertChar(EOF, p.readChar());
    }

    @Test
    public void testDiscardLine() throws IOException {
        // arrange
        final SimpleTextParser p = parser("a\t\n\r\n   b c");

        // act/assert
        p.discardLine();
        assertChar('\r', p.peekChar());
        assertPosition(p, 2, 1);

        p.discardLine();
        assertChar(' ', p.peekChar());
        assertPosition(p, 3, 1);

        p.discardLine();
        assertPosition(p, 3, 7);
        assertChar(EOF, p.peekChar());

        p.discardLine();
        assertPosition(p, 3, 7);
        assertChar(EOF, p.peekChar());
    }

    @Test
    public void testPeek_lenArg() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef\r\n\r ghi");

        // act/assert
        Assertions.assertEquals("", p.peek(0));
        assertPosition(p, 1, 1);

        Assertions.assertEquals("", p.peek(0));
        assertPosition(p, 1, 1);

        p.readChar();

        Assertions.assertEquals("bcde", p.peek(4));
        assertPosition(p, 1, 2);

        Assertions.assertEquals("bcdef\r", p.peek(6));
        assertPosition(p, 1, 2);

        Assertions.assertEquals("bcdef\r\n\r ghi", p.peek(100));
        assertPosition(p, 1, 2);

        assertChar('b', p.readChar());

        p.discard(c -> true);

        Assertions.assertNull(p.peek(0));
        Assertions.assertNull(p.peek(100));
    }

    @Test
    public void testPeek_lenArg_invalidArg() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef");
        p.setMaxStringLength(4);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.peek(-1);
        }, IllegalArgumentException.class, "Requested string length cannot be negative; was -1");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.peek(6);
        }, IllegalArgumentException.class, "Requested string length of 6 exceeds maximum value of 4");
    }

    @Test
    public void testPeek_predicateArg() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef\r\n\r ghi");

        // act/assert
        Assertions.assertEquals("", p.peek(c -> false));
        assertPosition(p, 1, 1);

        p.readChar();

        Assertions.assertEquals("bcdef", p.peek(SimpleTextParser::isAlphanumeric));
        assertPosition(p, 1, 2);

        Assertions.assertEquals("bcdef\r\n\r ghi", p.peek(c -> true));
        assertPosition(p, 1, 2);

        assertChar('b', p.readChar());

        p.discard(c -> true);

        Assertions.assertNull(p.peek(c -> true));
        Assertions.assertNull(p.peek(c -> false));
    }

    @Test
    public void testPeek_predicateArg_exceedsMaxStringLength() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef");
        p.setMaxStringLength(4);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.peek(SimpleTextParser::isNotWhitespace);
        }, IllegalStateException.class, "String length exceeds maximum value of 4");
    }

    @Test
    public void testMatch() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef");

        // act/assert
        p.next(1)
            .match("a")
            .next(100)
            .match("bcdef");
    }

    @Test
    public void testMatch_failure() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.match("empty");
        }, IllegalStateException.class, "No token has been read from the character stream");

        p.next(1);
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.match("b");
        }, IOException.class, "Parsing failed at line 1, column 1: expected [b] but found [a]");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.match("A");
        }, IOException.class, "Parsing failed at line 1, column 1: expected [A] but found [a]");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.match(null);
        }, IOException.class, "Parsing failed at line 1, column 1: expected [null] but found [a]");
    }

    @Test
    public void testMatch_ignoreCase() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef");

        // act/assert
        p.next(1)
            .matchIgnoreCase("A")
            .next(100)
            .matchIgnoreCase("BcdEF");
    }

    @Test
    public void testMatchIgnoreCase_failure() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.matchIgnoreCase("empty");
        }, IllegalStateException.class, "No token has been read from the character stream");

        p.next(1);
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.matchIgnoreCase("b");
        }, IOException.class, "Parsing failed at line 1, column 1: expected [b] but found [a]");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.match(null);
        }, IOException.class, "Parsing failed at line 1, column 1: expected [null] but found [a]");
    }

    @Test
    public void testTryMatch() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");

        // act/assert
        p.next(3);

        Assertions.assertTrue(p.tryMatch("abc"));

        Assertions.assertFalse(p.tryMatch("ab"));
        Assertions.assertFalse(p.tryMatch(""));
        Assertions.assertFalse(p.tryMatch(null));

        Assertions.assertFalse(p.tryMatch("ABC"));
        Assertions.assertFalse(p.tryMatch("aBc"));

        p.next(1);
        Assertions.assertTrue(p.tryMatch(null));
    }

    @Test
    public void testTryMatch_noToken() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.tryMatch("empty");
        }, IllegalStateException.class, "No token has been read from the character stream");
    }

    @Test
    public void testTryMatchIgnoreCase() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");

        // act/assert
        p.next(3);

        Assertions.assertTrue(p.tryMatchIgnoreCase("abc"));
        Assertions.assertTrue(p.tryMatchIgnoreCase("ABC"));
        Assertions.assertTrue(p.tryMatchIgnoreCase("aBc"));

        Assertions.assertFalse(p.tryMatch("ab"));
        Assertions.assertFalse(p.tryMatch(""));
        Assertions.assertFalse(p.tryMatch(null));

        p.next(1);
        Assertions.assertTrue(p.tryMatch(null));
    }

    @Test
    public void testTryMatchIgnoreCase_noToken() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.tryMatchIgnoreCase("empty");
        }, IllegalStateException.class, "No token has been read from the character stream");
    }

    @Test
    public void testChoose() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");

        // act/assert
        p.next(1);

        Assertions.assertEquals(0, p.choose("a"));

        Assertions.assertEquals(0, p.choose("a", "b", "c"));
        Assertions.assertEquals(2, p.choose("c", "b", "a"));

        p.next(1);

        Assertions.assertEquals(0, p.choose("b"));

        Assertions.assertEquals(1, p.choose("a", "b", "c"));
        Assertions.assertEquals(1, p.choose("c", "b", "a"));
    }

    @Test
    public void testChoose_failure() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.choose("X");
        }, IllegalStateException.class, "No token has been read from the character stream");

        p.next(1);
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.choose("X");
        }, IOException.class, "Parsing failed at line 1, column 1: expected one of [X] but found [a]");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.choose("X", "Y", "Z");
        }, IOException.class, "Parsing failed at line 1, column 1: expected one of [X, Y, Z] but found [a]");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.choose("A");
        }, IOException.class, "Parsing failed at line 1, column 1: expected one of [A] but found [a]");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.choose();
        }, IOException.class, "Parsing failed at line 1, column 1: expected one of [] but found [a]");
    }

    @Test
    public void testChooseIgnoreCase() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");

        // act/assert
        p.next(1);

        Assertions.assertEquals(0, p.chooseIgnoreCase("A"));

        Assertions.assertEquals(0, p.chooseIgnoreCase("A", "b", "C"));
        Assertions.assertEquals(2, p.chooseIgnoreCase("C", "b", "A"));

        p.next(1);

        Assertions.assertEquals(0, p.chooseIgnoreCase("b"));

        Assertions.assertEquals(1, p.chooseIgnoreCase("A", "b", "C"));
        Assertions.assertEquals(1, p.chooseIgnoreCase("C", "b", "A"));
    }

    @Test
    public void testChooseIgnoreCase_failure() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.chooseIgnoreCase("X");
        }, IllegalStateException.class, "No token has been read from the character stream");

        p.next(1);
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.chooseIgnoreCase("X");
        }, IOException.class, "Parsing failed at line 1, column 1: expected one of [X] but found [a]");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.chooseIgnoreCase("X", "Y", "Z");
        }, IOException.class, "Parsing failed at line 1, column 1: expected one of [X, Y, Z] but found [a]");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.chooseIgnoreCase();
        }, IOException.class, "Parsing failed at line 1, column 1: expected one of [] but found [a]");
    }

    @Test
    public void testTryChoose() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");

        // act/assert
        p.next(1);

        Assertions.assertEquals(0, p.tryChoose("a"));

        Assertions.assertEquals(0, p.tryChoose("a", "b", "c"));
        Assertions.assertEquals(2, p.tryChoose("c", "b", "a"));

        p.next(1);

        Assertions.assertEquals(0, p.tryChoose("b"));

        Assertions.assertEquals(1, p.tryChoose("a", "b", "c"));
        Assertions.assertEquals(1, p.tryChoose("c", "b", "a"));

        Assertions.assertEquals(-1, p.tryChoose("A", "B", "C"));
        Assertions.assertEquals(-1, p.tryChoose());
        Assertions.assertEquals(-1, p.tryChoose((String) null));
    }

    @Test
    public void testTryChoose_noToken() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.tryChoose("X");
        }, IllegalStateException.class, "No token has been read from the character stream");
    }

    @Test
    public void testTryChooseIgnoreCase() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");

        // act/assert
        p.next(1);

        Assertions.assertEquals(0, p.tryChooseIgnoreCase("a"));

        Assertions.assertEquals(0, p.tryChooseIgnoreCase("A", "B", "C"));
        Assertions.assertEquals(2, p.tryChooseIgnoreCase("C", "b", "A"));

        p.next(1);

        Assertions.assertEquals(0, p.tryChooseIgnoreCase("B"));

        Assertions.assertEquals(1, p.tryChooseIgnoreCase("a", "B", "c"));
        Assertions.assertEquals(1, p.tryChooseIgnoreCase("c", "b", "a"));

        Assertions.assertEquals(-1, p.tryChooseIgnoreCase("X", "Y", "Z"));
        Assertions.assertEquals(-1, p.tryChooseIgnoreCase());
        Assertions.assertEquals(-1, p.tryChooseIgnoreCase((String) null));
    }

    @Test
    public void testTryChooseIgnoreCase_noToken() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abcdef");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            p.tryChooseIgnoreCase("X");
        }, IllegalStateException.class, "No token has been read from the character stream");
    }

    @Test
    public void testUnexpectedToken() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc\ndef");

        // act/assert
        Assertions.assertEquals("Parsing failed at line 1, column 1: expected test but found no current token",
                p.unexpectedToken("test").getMessage());

        p.nextAlphanumeric();
        Assertions.assertEquals("Parsing failed at line 1, column 1: expected test but found [abc]",
                p.unexpectedToken("test").getMessage());

        p.nextAlphanumeric();
        Assertions.assertEquals("Parsing failed at line 1, column 4: expected test but found end of line",
                p.unexpectedToken("test").getMessage());

        p.discardLine();

        p.next(SimpleTextParser::isWhitespace);
        Assertions.assertEquals("Parsing failed at line 2, column 1: expected test but found empty token followed by [d]",
                p.unexpectedToken("test").getMessage());

        p.next(3).next(10);
        Assertions.assertEquals("Parsing failed at line 2, column 4: expected test but found end of content",
                p.unexpectedToken("test").getMessage());
    }

    @Test
    public void testUnexpectedToken_causeArg() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");
        final Exception cause = new Exception("test");

        // act/assert
        p.nextLine();

        IOException exc = p.unexpectedToken("test", cause);
        Assertions.assertEquals("Parsing failed at line 1, column 1: expected test but found [abc]",
                exc.getMessage());
        Assertions.assertSame(cause, exc.getCause());
    }

    @Test
    public void testUnexpectedToken_ioError() throws IOException {
        // arrange
        final FailBuffer b = new FailBuffer(new StringReader("abc"));
        final SimpleTextParser p = new SimpleTextParser(b);

        // act/assert
        b.setFail(false);
        p.next(SimpleTextParser::isDecimalPart);
        b.setFail(true);
        Assertions.assertEquals("Parsing failed at line 1, column 1: expected test but found empty token",
                p.unexpectedToken("test").getMessage());

        b.setFail(false);
        p.nextAlphanumeric();
        b.setFail(true);
        Assertions.assertEquals("Parsing failed at line 1, column 1: expected test but found [abc]",
                p.unexpectedToken("test").getMessage());

        b.setFail(false);
        p.nextAlphanumeric();
        b.setFail(true);
        Assertions.assertEquals("Parsing failed at line 1, column 4: expected test but found no current token",
                p.unexpectedToken("test").getMessage());
    }

    @Test
    public void testTokenError() throws IOException {
        // arrange
        final SimpleTextParser p = parser("a\nbc");
        p.nextLine();
        p.next(1);
        p.readChar();

        // act/assert
        final IOException exc = p.tokenError("test message");

        Assertions.assertEquals("Parsing failed at line 2, column 1: test message", exc.getMessage());
        Assertions.assertNull(exc.getCause());
    }

    @Test
    public void testTokenError_noTokenSet() throws IOException {
        // arrange
        final SimpleTextParser p = parser("ab\nc");
        p.readChar();

        // act/assert
        final IOException exc = p.tokenError("test message");

        Assertions.assertEquals("Parsing failed at line 1, column 2: test message", exc.getMessage());
        Assertions.assertNull(exc.getCause());
    }

    @Test
    public void testTokenError_withCause() throws IOException {
        // arrange
        SimpleTextParser p = parser("a\nbc");
        p.nextLine();
        p.next(1);
        p.readChar();

        final Exception cause = new Exception("test");

        // act/assert
        final IOException exc = p.tokenError("test message", cause);

        Assertions.assertEquals("Parsing failed at line 2, column 1: test message", exc.getMessage());
        Assertions.assertSame(cause, exc.getCause());
    }

    @Test
    public void testParseError_currentLineCol() throws IOException {
        // arrange
        final SimpleTextParser p = parser("a\nbc");
        p.discard(ch -> ch != 'b');

        // act
        final IOException exc = p.parseError("test message");

        Assertions.assertEquals("Parsing failed at line 2, column 1: test message", exc.getMessage());
        Assertions.assertNull(exc.getCause());
    }

    @Test
    public void testParseError_currentLineCol_withCause() throws IOException {
        // arrange
        final SimpleTextParser p = parser("abc");
        p.readChar();
        final Exception cause = new Exception("test");

        // act
        final IOException exc = p.parseError("test message", cause);

        Assertions.assertEquals("Parsing failed at line 1, column 2: test message", exc.getMessage());
        Assertions.assertSame(cause, exc.getCause());
    }

    @Test
    public void testParseError_givenLineCol() {
        // arrange
        final SimpleTextParser p = parser("abc");

        // act
        final IOException exc = p.parseError(5, 6, "test message");

        Assertions.assertEquals("Parsing failed at line 5, column 6: test message", exc.getMessage());
        Assertions.assertNull(exc.getCause());
    }

    @Test
    public void testParseError_givenLineCol_withCause() {
        // arrange
        final SimpleTextParser p = parser("abc");
        final Exception cause = new Exception("test");

        // act
        final IOException exc = p.parseError(5, 6, "test message", cause);

        Assertions.assertEquals("Parsing failed at line 5, column 6: test message", exc.getMessage());
        Assertions.assertSame(cause, exc.getCause());
    }

    @Test
    public void testCharacterPredicates() {
        // act/assert
        assertMatchesAll(SimpleTextParser::isWhitespace, " \t\n\r");
        assertDoesNotMatchAny(SimpleTextParser::isWhitespace, "abcABC<>,./?:;'\"[]{}`~!@#$%^&*()_+-=");

        assertMatchesAll(SimpleTextParser::isNotWhitespace, "abcABC<>,./?:;'\"[]{}`~!@#$%^&*()_+-=");
        assertDoesNotMatchAny(SimpleTextParser::isNotWhitespace, " \t\n\r");

        assertMatchesAll(SimpleTextParser::isLineWhitespace, " \t");
        assertDoesNotMatchAny(SimpleTextParser::isLineWhitespace, "\n\rabcABC<>,./?:;'\"[]{}`~!@#$%^&*()_+-=");

        assertMatchesAll(SimpleTextParser::isNewLinePart, "\n\r");
        assertDoesNotMatchAny(SimpleTextParser::isNewLinePart, " \tabcABC<>,./?:;'\"[]{}`~!@#$%^&*()_+-=");

        assertMatchesAll(SimpleTextParser::isNotNewLinePart, " \tabcABC<>,./?:;'\"[]{}`~!@#$%^&*()_+-=");
        assertDoesNotMatchAny(SimpleTextParser::isNotNewLinePart, "\n\r");

        assertMatchesAll(SimpleTextParser::isAlphanumeric, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        assertDoesNotMatchAny(SimpleTextParser::isAlphanumeric, " \t\n\r./?:;'\\\"[]{}`~!@#$%^&*()_+-=");

        assertMatchesAll(SimpleTextParser::isNotAlphanumeric, " \t\n\r./?:;'\\\"[]{}`~!@#$%^&*()_+-=");
        assertDoesNotMatchAny(SimpleTextParser::isNotAlphanumeric, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

        assertMatchesAll(SimpleTextParser::isIntegerPart, "0123456789+-");
        assertDoesNotMatchAny(SimpleTextParser::isIntegerPart, " \t\n\r./?:;'\\\"[]{}`~!@#$%^&*()_=abcdeABCDE");

        assertMatchesAll(SimpleTextParser::isDecimalPart, "0123456789+-.eE");
        assertDoesNotMatchAny(SimpleTextParser::isDecimalPart, " \t\n\r/?:;'\\\"[]{}`~!@#$%^&*()_=abcdABCD");
    }

    private static SimpleTextParser parser(final String content) {
        final StringReader reader = new StringReader(content);

        return new SimpleTextParser(reader);
    }

    private static void assertCharacterSequence(final SimpleTextParser parser, final String expected)
            throws IOException {
        char expectedChar;
        String msg;
        for (int i = 0; i < expected.length(); ++i) {
            expectedChar = expected.charAt(i);

            msg = "Failed at index " + i + ":";

            Assertions.assertEquals(expectedChar, parser.peekChar(), msg);
            Assertions.assertEquals(expectedChar, parser.peekChar(), msg);

            Assertions.assertTrue(parser.hasMoreCharacters());
            Assertions.assertEquals(expectedChar, parser.readChar(), msg);
        }

        Assertions.assertFalse(parser.hasMoreCharacters());
        Assertions.assertEquals(-1, parser.peekChar());
        Assertions.assertEquals(-1, parser.peekChar());
        Assertions.assertEquals(-1, parser.readChar());
    }

    private static void assertChar(final int expected, final int actual) {
        final String expectedStr = describeChar(expected);
        final String actualStr = describeChar(actual);

        Assertions.assertEquals(expected, actual, "Expected [" + expectedStr + "] but was [" + actualStr + "];");
    }

    private static void assertMatchesAll(final IntPredicate pred, final String chars) {
        for (char ch : chars.toCharArray()) {
            final String msg = "Expected predicate to match [" + describeChar(ch) + "]";
            Assertions.assertTrue(pred.test(ch), msg);
        }
    }

    private static void assertDoesNotMatchAny(final IntPredicate pred, final String chars) {
        for (char ch : chars.toCharArray()) {
            final String msg = "Expected predicate to not match [" + describeChar(ch) + "]";
            Assertions.assertFalse(pred.test(ch), msg);
        }
    }

    private static String describeChar(final int ch) {
        switch (ch) {
        case '\n':
            return "\\n";
        case '\r':
            return "\\r";
        case '\t':
            return "\\t";
        case EOF:
            return "EOF";
        default:
            return String.valueOf((char) ch);
        }
    }

    private static void assertPosition(final SimpleTextParser parser, final int line, final int col) {
        Assertions.assertEquals(line, parser.getLineNumber(), "Unexpected line number");
        Assertions.assertEquals(col, parser.getColumnNumber(), "Unexpected column number");
    }

    private static void assertToken(final SimpleTextParser parser, final String token, final int line, final int col) {
        Assertions.assertEquals(token, parser.getCurrentToken(), "Unexpected token");
        Assertions.assertEquals(line, parser.getCurrentTokenLineNumber(), "Unexpected token line number");
        Assertions.assertEquals(col, parser.getCurrentTokenColumnNumber(), "Unexpected token column number");
    }

    private static final class FailBuffer extends CharReadBuffer {

        private boolean fail;

        FailBuffer(final Reader in) {
            super(in);
        }

        public void setFail(final boolean fail) {
            this.fail = fail;
        }

        @Override
        public boolean hasMoreCharacters() throws IOException {
            checkFail();
            return super.hasMoreCharacters();
        }

        private void checkFail() throws IOException {
            if (fail) {
                throw new IOException("test failure");
            }
        }
    }
}
