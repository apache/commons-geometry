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
package org.apache.commons.geometry.io.euclidean.threed.stl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;

/** Utility class with factory methods for constructing {@link FacetDefinitionReader}
 * instances for STL content.
 */
public final class StlFacetDefinitionReaders {

    /** Utility class; no instantiation. */
    private StlFacetDefinitionReaders() { }

    /** Construct a {@link FacetDefinitionReader} for reading STL content from the
     * given input. The format of the input is checked to determine if it is a binary
     * or text file and an appropriate reader is returned.
     * @param in input to read from
     * @param charset charset to use when checking the input for text content;
     *      if null, the input is assumed to use the UTF-8 charset
     * @return facet definition reader
     * @throws IOException if an I/O error occurs
     */
    public static FacetDefinitionReader create(final InputStream in, final Charset charset)
            throws IOException {
        final Charset inputCharset = charset != null ?
                charset :
                StlConstants.DEFAULT_CHARSET;

        final byte[] testBytes = StlConstants.SOLID_START_KEYWORD.getBytes(inputCharset);
        final byte[] actualBytes = new byte[testBytes.length];

        final int read = in.read(actualBytes);
        if (read < actualBytes.length) {
            throw new IOException(MessageFormat.format(
                    "Cannot determine STL format: attempted to read {0} bytes but found only {1} available",
                    actualBytes.length, read));
        }

        // "unread" the test bytes so that the created readers can start from the
        // beginning of the content
        final PushbackInputStream pushbackInput = new PushbackInputStream(in, actualBytes.length);
        pushbackInput.unread(actualBytes);

        if (Arrays.equals(testBytes, actualBytes)) {
            // this is a text file
            return new TextStlFacetDefinitionReader(
                    new BufferedReader(new InputStreamReader(pushbackInput, inputCharset)));
        } else {
            // this is a binary file
            return new BinaryStlFacetDefinitionReader(pushbackInput);
        }
    }
}
