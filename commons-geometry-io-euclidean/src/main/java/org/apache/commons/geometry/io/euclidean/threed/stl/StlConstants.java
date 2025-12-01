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
package org.apache.commons.geometry.io.euclidean.threed.stl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
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
import java.nio.charset.StandardCharsets;

/** Class containing constants for the STL file format.
 */
final class StlConstants {

    /** Default STL charset. */
    static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /** Keyword indicating the start of a solid. This is also the keyword used to indicate the
     * start of a text (ASCII) STL file.
     */
    static final String SOLID_START_KEYWORD = "solid";

    /** Keyword used to indicate the end of a solid definition. */
    static final String SOLID_END_KEYWORD = "endsolid";

    /** Keyword used to indicate the start of a facet. */
    static final String FACET_START_KEYWORD = "facet";

    /** Keyword used to indicate the end of a facet. */
    static final String FACET_END_KEYWORD = "endfacet";

    /** Keyword used to introduce a facet normal. */
    static final String NORMAL_KEYWORD = "normal";

    /** Keyword used when describing the outer vertex loop of a facet. */
    static final String OUTER_KEYWORD = "outer";

    /** Keyword used to indicate the start of a vertex loop. */
    static final String LOOP_START_KEYWORD = "loop";

    /** Keyword used to indicate the end of a vertex loop. */
    static final String LOOP_END_KEYWORD = "endloop";

    /** Keyword used to indicate a vertex definition. */
    static final String VERTEX_KEYWORD = "vertex";

    /** Number of bytes in the binary format header. */
    static final int BINARY_HEADER_BYTES = 80;

    /** Number of bytes for each triangle in the binary format. */
    static final int BINARY_TRIANGLE_BYTES = 50;

    /** Byte order for binary data. */
    static final ByteOrder BINARY_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    /** Utility class; no instantiation. */
    private StlConstants() {}
}
