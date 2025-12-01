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
package org.apache.commons.geometry.io.core.output;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileGeometryOutputTest {

    @TempDir
    Path tempDir;

    @Test
    void testCtor_fileOnly() {
        // arrange
        final Path file = Paths.get("some/path/test.txt");

        // act
        final FileGeometryOutput out = new FileGeometryOutput(file);

        // assert
        Assertions.assertEquals(file, out.getFile());
        Assertions.assertEquals("test.txt", out.getFileName());
        Assertions.assertNull(out.getCharset());
    }

    @Test
    void testCtor_fileAndCharset() {
        // arrange
        final Path file = Paths.get("TEST");
        final Charset charset = StandardCharsets.UTF_8;

        // act
        final FileGeometryOutput out = new FileGeometryOutput(file, charset);

        // assert
        Assertions.assertEquals(file, out.getFile());
        Assertions.assertEquals("TEST", out.getFileName());
        Assertions.assertEquals(charset, out.getCharset());
    }

    @Test
    void testGetOutputStream() throws IOException {
        // arrange
        final Path file = tempDir.resolve("test");
        final byte[] bytes = "abc".getBytes(StandardCharsets.UTF_8);

        final FileGeometryOutput output = new FileGeometryOutput(file);

        // act/assert
        try (OutputStream out = output.getOutputStream()) {
            out.write(bytes);

            Assertions.assertEquals(BufferedOutputStream.class, out.getClass());
        }

        Assertions.assertArrayEquals(bytes, Files.readAllBytes(file));
    }

    @Test
    void testToString() {
        // arrange
        final FileGeometryOutput out = new FileGeometryOutput(Paths.get("some/path/test.txt"));

        // act
        final String result = out.toString();

        // assert
        Assertions.assertEquals("FileGeometryOutput[file= some/path/test.txt]",
                result.replaceAll("\\\\", "/"));
    }
}
