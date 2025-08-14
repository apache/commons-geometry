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
package org.apache.commons.geometry.io.core;

import java.nio.charset.Charset;

/** Interface containing basic metadata fields for use in I/O operations.
 */
public interface GeometryIOMetadata {

    /** Get the file name associated with the operation, if any.
     * @return file name associated with the operation or {@code null}
     *      if unknown or not applicable
     */
    String getFileName();

    /** Get the charset for the operation, if any.
     * @return charset for the operation or {@code null} if unknown or
     *      not applicable
     */
    Charset getCharset();
}
