/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.io.core.input;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UrlGeometryInputTest {

    @TempDir
    Path tempDir;

    @Test
    void testCtor_fileOnly() throws IOException {
        // arrange
        final URL url = Paths.get("some/path/test.txt").toUri().toURL();

        // act
        final UrlGeometryInput in = new UrlGeometryInput(url);

        // assert
        Assertions.assertEquals(url, in.getUrl());
        Assertions.assertEquals("test.txt", in.getFileName());
        Assertions.assertNull(in.getCharset());
    }

    @Test
    void testCtor_fileAndCharset() {
        // arrange
        final URL url = getClass().getResource("/java/lang/String.class");
        final Charset charset = StandardCharsets.UTF_8;

        // act
        final UrlGeometryInput in = new UrlGeometryInput(url, charset);

        // assert
        Assertions.assertEquals(url, in.getUrl());
        Assertions.assertEquals("String.class", in.getFileName());
        Assertions.assertEquals(charset, in.getCharset());
    }

    @Test
    void testGetInputStream() throws IOException {
        // arrange
        final Path file = tempDir.resolve("test");
        final byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);
        Files.write(file, bytes);

        final UrlGeometryInput input = new UrlGeometryInput(file.toUri().toURL());

        // act/assert
        try (InputStream in = input.getInputStream()) {
            Assertions.assertEquals(BufferedInputStream.class, in.getClass());

            final byte[] readBytes = new byte[3];
            in.read(readBytes);

            Assertions.assertArrayEquals(bytes, readBytes);
        }
    }

    @Test
    void testToString() throws IOException {
        // arrange
        final UrlGeometryInput in = new UrlGeometryInput(Paths.get("some/path/test.txt").toUri().toURL());

        // act
        final String result = in.toString();

        // assert
        Assertions.assertTrue(result.startsWith("UrlGeometryInput[url= file:"));
    }
}
