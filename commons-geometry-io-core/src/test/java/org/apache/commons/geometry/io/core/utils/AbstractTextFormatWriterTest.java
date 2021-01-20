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
import java.io.Writer;
import java.text.DecimalFormat;

import org.apache.commons.geometry.io.core.test.CloseCountWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AbstractTextFormatWriterTest {

    private StringWriter out = new StringWriter();

    @Test
    public void testDefaults() throws IOException {
        // act
        try (TestWriter writer = new TestWriter(out)) {
            // assert
            Assertions.assertEquals("\n", writer.getLineSeparator());
            Assertions.assertEquals(0, writer.getDecimalFormat().getMinimumFractionDigits());
            Assertions.assertEquals(6, writer.getDecimalFormat().getMaximumFractionDigits());
            Assertions.assertSame(out, writer.getWriter());
        }
    }

    @Test
    public void testWrite() throws IOException {
        // arrange
        final CloseCountWriter closeCountWriter = new CloseCountWriter(out);
        try (TestWriter writer = new TestWriter(closeCountWriter)) {

            writer.setLineSeparator("\r\n");

            final DecimalFormat df = new DecimalFormat();
            df.setMinimumFractionDigits(2);
            writer.setDecimalFormat(df);

            // act
            writer.write('a');
            writer.write("bc");
            writer.writeNewLine();
            writer.write(1.0);
            writer.writeNewLine();
            writer.write(5);

            // assert
            Assertions.assertEquals("abc\r\n1.00\r\n5", out.toString());
        }

        Assertions.assertEquals(1, closeCountWriter.getCloseCount());
    }

    private static final class TestWriter extends AbstractTextFormatWriter {

        protected TestWriter(final Writer writer) {
            super(writer);
        }
    }
}
