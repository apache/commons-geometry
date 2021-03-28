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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/** Interface representing the output of a geometric IO operation.
 */
public interface GeometryOutput {

    /** Get the output file name.
     * @return output file name
     */
    String getFileName();

    /** Get the charset of the output or null if the charset
     * is unknown or not applicable.
     * @return charset of the input or null if unknown or
     *      not applicable
     */
    Charset getCharset();

    /** Get the output stream for writing to the output.
     * @return output stream for writing to the output
     * @throws IOException if an IO error occurs
     */
    OutputStream getOutputStream() throws IOException;
}
