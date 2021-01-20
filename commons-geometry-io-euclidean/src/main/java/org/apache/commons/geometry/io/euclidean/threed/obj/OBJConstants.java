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
package org.apache.commons.geometry.io.euclidean.threed.obj;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Class containing constants for use with OBJ files.
 */
public final class OBJConstants {

    /** Default OBJ charset. */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /** Character used to indicate the start of a comment line. */
    public static final char COMMENT_CHAR = '#';

    /** Character placed before new line sequences to indicate a line continuation. */
    public static final char LINE_CONTINUATION_CHAR = '\\';

    /** Keyword used to indicate a vertex definition line. */
    public static final String VERTEX_KEYWORD = "v";

    /** Keyword used to indicate a vertex normal definition line. */
    public static final String VERTEX_NORMAL_KEYWORD = "vn";

    /** Keyword used to indicate a texture coordinate definition line. */
    public static final String TEXTURE_COORDINATE_KEYWORD = "vt";

    /** Keyword used to indicate a face definition line. */
    public static final String FACE_KEYWORD = "f";

    /** Character used to separate face vertex attribute indices. */
    public static final char FACE_VERTEX_ATTRIBUTE_SEP_CHAR = '/';

    /** Keyword used to indicate a geometry group. */
    public static final String GROUP_KEYWORD = "g";

    /** Keyword used to indicate a geometry group. */
    public static final String SMOOTHING_GROUP_KEYWORD = "s";

    /** Keyword used to associate a name with the following geometry. */
    public static final String OBJECT_KEYWORD = "o";

    /** Keyword used to reference a material library file. */
    public static final String MATERIAL_LIBRARY_KEYWORD = "mtllib";

    /** Keyword used to apply a named material to subsequent geometry. */
    public static final String USE_MATERIAL_KEYWORD = "usemtl";

    /** Utility class; no instantiation. */
    private OBJConstants() {}
}
