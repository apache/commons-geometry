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
package org.apache.commons.geometry.io.core.output;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StreamGeometryOutputTest {

    private final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

    @Test
    void testCtor_stream() {
        // act
        final StreamGeometryOutput out = new StreamGeometryOutput(byteStream);

        // assert
        Assertions.assertNull(out.getFileName());
        Assertions.assertNull(out.getCharset());
        Assertions.assertEquals(byteStream, out.getOutputStream());
    }

    @Test
    void testCtor_streamAndFileName() {
        // act
        final StreamGeometryOutput out = new StreamGeometryOutput(byteStream, "test.txt");

        // assert
        Assertions.assertEquals("test.txt", out.getFileName());
        Assertions.assertNull(out.getCharset());
        Assertions.assertEquals(byteStream, out.getOutputStream());
    }

    @Test
    void testCtor_allArgs() {
        // act
        final StreamGeometryOutput out = new StreamGeometryOutput(byteStream, "test.txt", StandardCharsets.UTF_16);

        // assert
        Assertions.assertEquals("test.txt", out.getFileName());
        Assertions.assertEquals(StandardCharsets.UTF_16, out.getCharset());
        Assertions.assertEquals(byteStream, out.getOutputStream());
    }

    @Test
    void testToString() {
        // arrange
        final StreamGeometryOutput out = new StreamGeometryOutput(byteStream, "abc.txt");

        // act
        final String result = out.toString();

        // assert
        Assertions.assertEquals("StreamGeometryOutput[fileName= abc.txt]", result);
    }
}
