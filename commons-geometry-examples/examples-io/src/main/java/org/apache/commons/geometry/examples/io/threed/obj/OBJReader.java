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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.SimpleTriangleMesh;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;

/** Class for reading {@link TriangleMesh} objects from OBJ files. Only vertex and face definitions
 * are read from the input; other OBJ keywords are ignored.
 *
 * <p>Instances of this class are <em>not</em> thread-safe.</p>
 * @see <a href="https://en.wikipedia.org/wiki/Wavefront_.obj_file">Wavefront .obj file</a>
 */
public final class OBJReader {

    /** Character buffer size. */
    private static final int BUFFER_SIZE = 2048;

    /** Builder object used to construct the mesh. */
    private SimpleTriangleMesh.Builder meshBuilder;

    /** Read a {@link TriangleMesh} from the given OBJ file. The file is read using the UTF-8 charset.
     * @param file file to read from
     * @param precision precision context to use in the created mesh
     * @return a new mesh object
     * @throws IOException if an IO operation fails
     * @throws IllegalArgumentException if invalid OBj syntax is encountered in the input
     */
    public TriangleMesh readTriangleMesh(final File file, final DoublePrecisionContext precision) throws IOException {
        try (final Reader reader = Files.newBufferedReader(file.toPath(), OBJConstants.DEFAULT_CHARSET)) {
            return readTriangleMesh(reader, precision);
        }
    }

    /** Read a {@link TriangleMesh} from the given url representing an OBJ file. The input is read using the
     * UTF-8 charset.
     * @param url url to read from
     * @param precision precision context to use in the created mesh
     * @return a new mesh object
     * @throws IOException if an IO operation fails
     * @throws IllegalArgumentException if invalid OBj syntax is encountered in the input
     */
    public TriangleMesh readTriangleMesh(final URL url, final DoublePrecisionContext precision) throws IOException {
        try (final InputStreamReader reader = new InputStreamReader(url.openStream(), OBJConstants.DEFAULT_CHARSET)) {
            return readTriangleMesh(reader, precision);
        }
    }

    /** Read a {@link TriangleMesh} from the given reader. The reader is not closed.
     * @param reader the reader to read input from
     * @param precision precision context to use in the created mesh
     * @return a new mesh object
     * @throws IOException if an IO operation fails
     * @throws IllegalArgumentException if invalid OBj syntax is encountered in the input
     */
    public TriangleMesh readTriangleMesh(final Reader reader, final DoublePrecisionContext precision)
            throws IOException {
        meshBuilder = SimpleTriangleMesh.builder(precision);

        parse(reader);

        final TriangleMesh mesh = meshBuilder.build();
        meshBuilder = null;

        return mesh;
    }

    /** Parse the input from the reader.
     * @param reader reader to read from
     * @throws IOException if an IO error occurs
     * @throws IllegalArgumentException if invalid OBj syntax is encountered
     */
    private void parse(final Reader reader) throws IOException {
        final char[] buffer = new char[BUFFER_SIZE];
        final StringBuilder sb = new StringBuilder();

        char ch;
        int read;
        while ((read = reader.read(buffer, 0, buffer.length)) > 0) {

            for (int i = 0; i < read; ++i) {
                ch = buffer[i];

                if (ch == '\r' || ch == '\n') {
                    if (sb.length() > 0) {
                        parseLine(sb.toString());

                        sb.delete(0, sb.length());
                    }
                } else {
                    sb.append(ch);
                }
            }
        }

        if (sb.length() > 0) {
            parseLine(sb.toString());
        }
    }

    /** Parse a line read from the input.
     * @param line line to parse
     * @throws IllegalArgumentException if invalid OBj syntax is encountered
     */
    private void parseLine(final String line) {
        // advance past any preceding whitespace
        int startIdx = 0;
        while (startIdx < line.length() && Character.isWhitespace(line.charAt(startIdx))) {
            ++startIdx;
        }

        if (startIdx >= line.length() || line.charAt(startIdx) == OBJConstants.COMMENT_START_CHAR) {
            return; // skip
        }

        final int idx = nextWhitespace(line, startIdx);
        if (idx > -1) {
            final String keyword = line.substring(startIdx, idx);
            final String remainder = line.substring(idx + 1).trim();

            // we're only interested in vertex and face lines; ignore everything else
            if (OBJConstants.VERTEX_KEYWORD.equals(keyword)) {
                parseVertexLine(remainder);
            } else if (OBJConstants.FACE_KEYWORD.equals(keyword)) {
                parseFaceLine(remainder);
            }
        }
    }

    /** Parse a vertex definition line.
     * @param line line content, excluding the initial vertex keyword
     * @throws IllegalArgumentException if invalid OBj syntax is encountered
     */
    private void parseVertexLine(final String line) {
        final String[] parts = splitOnWhitespace(line);
        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "Invalid vertex definition: at least 3 fields required but found only " + parts.length);
        }

        final double x = Double.parseDouble(parts[0]);
        final double y = Double.parseDouble(parts[1]);
        final double z = Double.parseDouble(parts[2]);

        addVertex(Vector3D.of(x, y, z));
    }

    /** Add a vertex to the constructed mesh.
     * @param vertex vertex to add
     */
    private void addVertex(final Vector3D vertex) {
        meshBuilder.addVertex(vertex);
    }

    /** Parse a face definition line.
     * @param line line content, excluding the initial face keyword
     * @throws IllegalArgumentException if invalid OBj syntax is encountered
     */
    private void parseFaceLine(final String line) {
        final String[] parts = splitOnWhitespace(line);
        if (parts.length < 3) {
            throw new IllegalArgumentException(
                    "Invalid face definition: at least 3 fields required but found only " + parts.length);
        }

        // use a simple triangle fan if more than 3 vertices are given
        final int startIdx = parseFaceVertexIndex(parts[0]);
        int prevIdx = parseFaceVertexIndex(parts[1]);
        int curIdx;
        for (int i = 2; i < parts.length; ++i) {
            curIdx = parseFaceVertexIndex(parts[i]);
            addFace(startIdx, prevIdx, curIdx);

            prevIdx = curIdx;
        }
    }

    /** Parse the vertex index from a face vertex definition of the form {@code v/vt/vn},
     * where {@code v} is the vertex index, {@code vt} is the vertex texture coordinate, and
     * {@code vn} is the vertex normal index. The texture coordinate and normal are optional and
     * are ignored by this class.
     * @param str string to parse
     * @return the face vertex index
     */
    private int parseFaceVertexIndex(final String str) {
        final int sepIdx = str.indexOf(OBJConstants.FACE_VALUE_SEP_CHAR);
        final String vertexIdxStr = sepIdx > -1 ?
                str.substring(0, sepIdx) :
                str;

        return Integer.parseInt(vertexIdxStr);
    }

    /** Add a face to the constructed mesh.
     * @param index1 first vertex index, in OBJ format
     * @param index2 second vertex index, in OBJ format
     * @param index3 third vertex index, in OBJ format
     */
    private void addFace(final int index1, final int index2, final int index3) {
        meshBuilder.addFace(
                adjustVertexIndex(index1),
                adjustVertexIndex(index2),
                adjustVertexIndex(index3));
    }

    /** Adjust a vertex index from the OBJ format to array index format. OBJ vertex indices
     * are 1-based and are allowed to be negative to refer to indices added most recently. For
     * example, index {@code 1} refers to the first added vertex and {@code -1} refers to the
     * most recently added vertex.
     * @param index index to adjust
     * @return the adjusted 0-based index
     */
    private int adjustVertexIndex(final int index) {
        if (index < 0) {
            // relative index from end
            return meshBuilder.getVertexCount() + index;
        }

        // convert from 1-based to 0-based
        return index - 1;
    }

    /** Find the index of the next whitespace character in the string.
     * @param str string to search
     * @param startIdx index to begin the search
     * @return the index of the next whitespace character or null if not found
     */
    private int nextWhitespace(final String str, final int startIdx) {
        final int len = str.length();
        for (int i = startIdx; i < len; ++i) {
            if (Character.isWhitespace(str.charAt(i))) {
                return i;
            }
        }

        return -1;
    }

    /** Split the given string on whitespace characters.
     * @param str string to split
     * @return the split string sections
     */
    private String[] splitOnWhitespace(final String str) {
        return str.split("\\s+");
    }
}
