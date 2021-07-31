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
package org.apache.commons.geometry.io.core.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.DoubleFunction;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.io.core.test.CloseCountWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AbstractTextFormatWriterTest {

    private StringWriter out = new StringWriter();

    @Test
    void testDefaults() {
        // act
        try (TestWriter writer = new TestWriter(out)) {
            // assert
            Assertions.assertEquals("\n", writer.getLineSeparator());
            Assertions.assertNotNull(writer.getDoubleFormat());
            Assertions.assertSame(out, writer.getWriter());
        }
    }

    @Test
    void testWrite_defaultConfig() {
        // arrange
        final double n = 20000.0 / 3.0;
        final CloseCountWriter closeCountWriter = new CloseCountWriter(out);
        try (TestWriter writer = new TestWriter(closeCountWriter)) {
            // act
            writer.write('a');
            writer.write("bc");
            writer.writeNewLine();
            writer.write(n);
            writer.writeNewLine();
            writer.write(Double.POSITIVE_INFINITY);
            writer.writeNewLine();
            writer.write(5);

            // assert
            Assertions.assertEquals("abc\n" + n + "\nInfinity\n5", out.toString());
        }

        Assertions.assertEquals(1, closeCountWriter.getCloseCount());
    }

    @Test
    void testWrite_customConfig() {
        // arrange
        final CloseCountWriter closeCountWriter = new CloseCountWriter(out);
        try (TestWriter writer = new TestWriter(closeCountWriter)) {

            writer.setLineSeparator("\r\n");

            final DecimalFormat fmt = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

            final DoubleFunction<String> df = fmt::format;
            writer.setDoubleFormat(df);

            // act
            writer.write('a');
            writer.write("bc");
            writer.writeNewLine();
            writer.write(20000.0 / 3.0);
            writer.writeNewLine();
            writer.write(5);

            // assert
            Assertions.assertEquals("abc\r\n6666.67\r\n5", out.toString());
        }

        Assertions.assertEquals(1, closeCountWriter.getCloseCount());
    }

    @Test
    void testWrite_failure() {
        // arrange
        final Writer failWriter = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("test");
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        // act/assert
        try (TestWriter writer = new TestWriter(failWriter)) {
            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.write('a'),
                    UncheckedIOException.class,
                    "IOException: test");

            GeometryTestUtils.assertThrowsWithMessage(
                    () -> writer.write("abc"),
                    UncheckedIOException.class,
                    "IOException: test");
        }
    }

    private static final class TestWriter extends AbstractTextFormatWriter {

        protected TestWriter(final Writer writer) {
            super(writer);
        }
    }
}
