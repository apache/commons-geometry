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
package org.apache.commons.geometry.examples.io.threed.obj;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Class containing constants for use with OBJ files.
 */
final class OBJConstants {
    /** Default OBJ charset. */
    static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /** Character used to indicate the start of a comment line. */
    static final char COMMENT_START_CHAR = '#';

    /** Keyword used to indicate a vertex definition line. */
    static final String VERTEX_KEYWORD = "v";

    /** Keyword used to indicate a face definition line. */
    static final String FACE_KEYWORD = "f";

    /** Keyword used to indicate a geometry group. */
    static final String GROUP_KEYWORD = "g";

    /** Keyword used to associate a name with the following geometry. */
    static final String OBJECT_KEYWORD = "o";

    /** Character used to separate face vertex indices from texture and normal indices. */
    static final char FACE_VALUE_SEP_CHAR = '/';

    /** Utility class; no instantiation. */
    private OBJConstants() {}
}
