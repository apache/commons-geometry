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
package org.apache.commons.geometry.io.core.input;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StreamGeometryInputTest {

    private final ByteArrayInputStream byteStream = new ByteArrayInputStream(new byte[0]);

    @Test
    void testCtor_stream() {
        // act
        final StreamGeometryInput in = new StreamGeometryInput(byteStream);

        // assert
        Assertions.assertNull(in.getFileName());
        Assertions.assertNull(in.getCharset());
        Assertions.assertEquals(byteStream, in.getInputStream());
    }

    @Test
    void testCtor_streamAndFileName() {
        // act
        final StreamGeometryInput in = new StreamGeometryInput(byteStream, "test.txt");

        // assert
        Assertions.assertEquals("test.txt", in.getFileName());
        Assertions.assertNull(in.getCharset());
        Assertions.assertEquals(byteStream, in.getInputStream());
    }

    @Test
    void testCtor_allArgs() {
        // act
        final StreamGeometryInput in = new StreamGeometryInput(byteStream, "test.txt", StandardCharsets.UTF_16);

        // assert
        Assertions.assertEquals("test.txt", in.getFileName());
        Assertions.assertEquals(StandardCharsets.UTF_16, in.getCharset());
        Assertions.assertEquals(byteStream, in.getInputStream());
    }

    @Test
    void testToString() {
        // arrange
        final StreamGeometryInput in = new StreamGeometryInput(byteStream, "abc.txt");

        // act
        final String result = in.toString();

        // assert
        Assertions.assertEquals("StreamGeometryInput[fileName= abc.txt]", result);
    }
}
