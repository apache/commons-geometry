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
package org.apache.commons.geometry.io.euclidean.threed.obj;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleFunction;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.internal.EuclideanUtils;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.Mesh;
import org.apache.commons.geometry.io.core.utils.AbstractTextFormatWriter;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;

/** Class for writing OBJ files containing 3D polygon geometries.
 */
public final class ObjWriter extends AbstractTextFormatWriter {

    /** Space character. */
    private static final char SPACE = ' ';

    /** Number of vertices written to the output. */
    private int vertexCount;

    /** Number of normals written to the output. */
    private int normalCount;

    /** Create a new instance that writes output with the given writer.
     * @param writer writer used to write output
     */
    public ObjWriter(final Writer writer) {
        super(writer);
    }

    /** Get the number of vertices written to the output.
     * @return the number of vertices written to the output.
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /** Get the number of vertex normals written to the output.
     * @return the number of vertex normals written to the output.
     */
    public int getVertexNormalCount() {
        return normalCount;
    }

    /** Write an OBJ comment with the given value.
     * @param comment comment to write
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeComment(final String comment) {
        for (final String line : comment.split("\\R")) {
            write(ObjConstants.COMMENT_CHAR);
            write(SPACE);
            write(line);
            writeNewLine();
        }
    }

    /** Write an object name to the output. This is metadata for the file and
     * does not affect the geometry, although it may affect how the file content
     * is read by other programs.
     * @param objectName the name to write
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeObjectName(final String objectName) {
        writeKeywordLine(ObjConstants.OBJECT_KEYWORD, objectName);
    }

    /** Write a group name to the output. This is metadata for the file and
     * does not affect the geometry, although it may affect how the file content
     * is read by other programs.
     * @param groupName the name to write
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeGroupName(final String groupName) {
        writeKeywordLine(ObjConstants.GROUP_KEYWORD, groupName);
    }

    /** Write a vertex and return the 0-based index of the vertex in the output.
     * @param vertex vertex to write
     * @return 0-based index of the written vertex
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public int writeVertex(final Vector3D vertex) {
        return writeVertexLine(createVectorString(vertex));
    }

    /** Write a vertex normal and return the 0-based index of the normal in the output.
     * @param normal normal to write
     * @return 0-based index of the written normal
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public int writeVertexNormal(final Vector3D normal) {
        return writeVertexNormalLine(createVectorString(normal));
    }

    /** Write a face with the given 0-based vertex indices.
     * @param vertexIndices 0-based vertex indices for the face
     * @throws IllegalArgumentException if fewer than 3 vertex indices are given
     * @throws IndexOutOfBoundsException if any vertex index is computed to be outside of
     *      the bounds of the elements written so far
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeFace(final int... vertexIndices) {
        writeFaceWithOffsets(0, vertexIndices, 0, null);
    }

    /** Write a face with the given 0-based vertex indices and 0-based normal index. The normal
     * index is applied to all face vertices.
     * @param vertexIndices 0-based vertex indices
     * @param normalIndex 0-based normal index
     * @throws IndexOutOfBoundsException if any vertex or normal index is computed to be outside of
     *      the bounds of the elements written so far
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeFace(final int[] vertexIndices, final int normalIndex) {
        final int[] normalIndices = new int[vertexIndices.length];
        Arrays.fill(normalIndices, normalIndex);

        writeFaceWithOffsets(0, vertexIndices, 0, normalIndices);
    }

    /** Write a face with the given vertex and normal indices. Indices are 0-based.
     * The {@code normalIndices} argument may be null, but if present, must contain the
     * same number of indices as {@code vertexIndices}.
     * @param vertexIndices 0-based vertex indices; may not be null
     * @param normalIndices 0-based normal indices; may be null but if present must contain
     *      the same number of indices as {@code vertexIndices}
     * @throws IllegalArgumentException if fewer than 3 vertex indices are given or {@code normalIndices}
     *      is not null but has a different length than {@code vertexIndices}
     * @throws IndexOutOfBoundsException if any vertex or normal index is computed to be outside of
     *      the bounds of the elements written so far
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeFace(final int[] vertexIndices, final int[] normalIndices) {
        writeFaceWithOffsets(0, vertexIndices, 0, normalIndices);
    }

    /** Write the boundaries present in the given boundary source using a {@link MeshBuffer}
     * with an unlimited size.
     * @param src boundary source containing the boundaries to write to the output
     * @throws IllegalArgumentException if any boundary in the argument is infinite
     * @throws java.io.UncheckedIOException if an I/O error occurs
     * @see #meshBuffer(int)
     * @see #writeMesh(Mesh)
     */
    public void writeBoundaries(final BoundarySource3D src) {
        writeBoundaries(src, -1);
    }

    /** Write the boundaries present in the given boundary source using a {@link MeshBuffer} with
     * the given {@code batchSize}.
     * @param src boundary source containing the boundaries to write to the output
     * @param batchSize batch size to use for the mesh buffer; pass {@code -1} to use a buffer
     *      of unlimited size
     * @throws IllegalArgumentException if any boundary in the argument is infinite
     * @throws java.io.UncheckedIOException if an I/O error occurs
     * @see #meshBuffer(int)
     * @see #writeMesh(Mesh)
     */
    public void writeBoundaries(final BoundarySource3D src, final int batchSize) {
        final MeshBuffer buffer = meshBuffer(batchSize);

        try (Stream<PlaneConvexSubset> stream = src.boundaryStream()) {
            final Iterator<PlaneConvexSubset> it = stream.iterator();
            while (it.hasNext()) {
                buffer.add(it.next());
            }
        }

        buffer.flush();
    }

    /** Write a mesh to the output. All vertices and faces are written exactly as found. For example,
     * if a vertex is duplicated in the argument, it will also be duplicated in the output.
     * @param mesh the mesh to write
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeMesh(final Mesh<?> mesh) {
        final int vertexOffset = vertexCount;

        for (final Vector3D vertex : mesh.vertices()) {
            writeVertex(vertex);
        }

        for (final Mesh.Face face : mesh.faces()) {
            writeFaceWithOffsets(vertexOffset, face.getVertexIndices(), 0, null);
        }
    }

    /** Create a new {@link MeshBuffer} instance with an unlimited batch size, meaning that
     * no vertex definitions are duplicated in the mesh output. This produces the most compact
     * mesh but at the most of higher memory usage during writing.
     * @return new mesh buffer instance
     */
    public MeshBuffer meshBuffer() {
        return meshBuffer(-1);
    }

    /** Create a new {@link MeshBuffer} instance with the given batch size. The batch size determines
     * how many faces will be stored in the buffer before being flushed. Faces stored in the buffer
     * share duplicate vertices, reducing the number of vertices required in the file. The {@code batchSize}
     * is therefore a trade-off between higher memory usage (high batch size) and a higher probability of duplicate
     * vertices present in the output (low batch size). A batch size of {@code -1} indicates an unlimited
     * batch size.
     * @param batchSize number of faces to store in the buffer before automatically flushing to the
     *      output
     * @return new mesh buffer instance
     */
    public MeshBuffer meshBuffer(final int batchSize) {
        return new MeshBuffer(batchSize);
    }

    /** Write a face with the given offsets and indices. The offsets are added to each
     * index before being written.
     * @param vertexOffset vertex offset value
     * @param vertexIndices 0-based vertex indices for the face
     * @param normalOffset normal offset value
     * @param normalIndices 0-based normal indices for the face; may be null if no normal are
     *      defined for the face
     * @throws IllegalArgumentException if fewer than 3 vertex indices are given or {@code normalIndices}
     *      is not null but has a different length than {@code vertexIndices}
     * @throws IndexOutOfBoundsException if any vertex or normal index is computed to be outside of
     *      the bounds of the elements written so far
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private void writeFaceWithOffsets(final int vertexOffset, final int[] vertexIndices,
            final int normalOffset, final int[] normalIndices) {
        if (vertexIndices.length < EuclideanUtils.TRIANGLE_VERTEX_COUNT) {
            throw new IllegalArgumentException("Face must have more than " + EuclideanUtils.TRIANGLE_VERTEX_COUNT +
                    " vertices; found " + vertexIndices.length);
        } else if (normalIndices != null && normalIndices.length != vertexIndices.length) {
            throw new IllegalArgumentException("Face normal index count must equal vertex index count; expected " +
                    vertexIndices.length + " but was " + normalIndices.length);
        }

        write(ObjConstants.FACE_KEYWORD);

        int vertexIdx;
        int normalIdx;
        for (int i = 0; i < vertexIndices.length; ++i) {
            vertexIdx = vertexIndices[i] + vertexOffset;
            if (vertexIdx < 0 || vertexIdx >= vertexCount) {
                throw new IndexOutOfBoundsException("Vertex index out of bounds: " + vertexIdx);
            }

            write(SPACE);
            write(vertexIdx + 1); // convert to OBJ 1-based convention

            if (normalIndices != null) {
                normalIdx = normalIndices[i] + normalOffset;
                if (normalIdx < 0 || normalIdx >= normalCount) {
                    throw new IndexOutOfBoundsException("Normal index out of bounds: " + normalIdx);
                }

                // two separator chars since there is no texture coordinate
                write(ObjConstants.FACE_VERTEX_ATTRIBUTE_SEP_CHAR);
                write(ObjConstants.FACE_VERTEX_ATTRIBUTE_SEP_CHAR);

                write(normalIdx + 1); // convert to OBJ 1-based convention
            }
        }

        writeNewLine();
    }

    /** Create the OBJ string representation of the given vector.
     * @param vec vector to convert to a string
     * @return string representation of the given vector
     */
    private String createVectorString(final Vector3D vec) {
        final DoubleFunction<String> fmt = getDoubleFormat();

        final StringBuilder sb = new StringBuilder();
        sb.append(fmt.apply(vec.getX()))
            .append(SPACE)
            .append(fmt.apply(vec.getY()))
            .append(SPACE)
            .append(fmt.apply(vec.getZ()));

        return sb.toString();
    }

    /** Write a vertex line containing the given string content.
     * @param content vertex string content
     * @return the 0-based index of the added vertex
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private int writeVertexLine(final String content) {
        writeKeywordLine(ObjConstants.VERTEX_KEYWORD, content);
        return vertexCount++;
    }

    /** Write a vertex normal line containing the given string content.
     * @param content vertex normal string content
     * @return the 0-based index of the added vertex normal
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private int writeVertexNormalLine(final String content) {
        writeKeywordLine(ObjConstants.VERTEX_NORMAL_KEYWORD, content);
        return normalCount++;
    }

    /** Write a line of content prefixed with the given OBJ keyword.
     * @param keyword OBJ keyword
     * @param content line content
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private void writeKeywordLine(final String keyword, final String content) {
        write(keyword);
        write(SPACE);
        write(content);
        writeNewLine();
    }

    /** Class used to produce OBJ mesh content from sequences of facets. As facets are added to the buffer
     * their vertices and normals are converted to OBJ vertex and normal definition strings. Vertices and normals
     * that produce equal definition strings are shared among all of the facets in the buffer. This process
     * converts the facet sequence into a compact mesh suitable for writing as OBJ file content.
     *
     * <p>Ideally, no vertices or normals would be duplicated in an OBJ file. However, when working with very large
     * geometries it may not be desirable to store values in memory before writing to the output. This
     * is where the {@code batchSize} property comes into play. The {@code batchSize} represents the maximum
     * number of faces that the buffer will store before automatically flushing its contents to the output and
     * resetting its state. This reduces the amount of memory used by the buffer at the cost of increasing the
     * likelihood of duplicate vertices and/or normals in the output.</p>
     */
    public final class MeshBuffer {

        /** Maximum number of faces that will be stored in the buffer before automatically flushing. */
        private final int batchSize;

        /** Map of vertex definition strings to their local index. */
        private final Map<String, Integer> vertexMap = new LinkedHashMap<>();

        /** Map of vertex normals to their local index. */
        private final Map<String, Integer> normalMap = new LinkedHashMap<>();

        /** List of local face vertex indices. */
        private final List<int[]> faceVertices;

        /** Map of local face indices to their local normal index. */
        private final Map<Integer, Integer> faceToNormalMap = new HashMap<>();

        /** Construct a new mesh buffer instance with the given batch size.
         * @param batchSize batch size; set to -1 to indicate an unlimited size
         */
        MeshBuffer(final int batchSize) {
            this.batchSize = batchSize;
            this.faceVertices = batchSize > -1 ?
                    new ArrayList<>(batchSize) :
                    new ArrayList<>();
        }

        /** Add a facet to this buffer. If {@code batchSize} is greater than {@code -1} and the number
         * of currently stored faces is greater than or equal to {@code batchSize}, then the buffer
         * content is written to the output and the buffer state is reset.
         * @param facet facet to add
         * @throws java.io.UncheckedIOException if an I/O error occurs
         */
        public void add(final FacetDefinition facet) {
            addFace(facet.getVertices(), facet.getNormal());
        }

        /** Add a boundary to this buffer. If {@code batchSize} is greater than {@code -1} and the number
         * of currently stored faces is greater than or equal to {@code batchSize}, then the buffer
         * content is written to the output and the buffer state is reset.
         * @param boundary boundary to add
         * @throws IllegalArgumentException if the boundary is infinite
         * @throws java.io.UncheckedIOException if an I/O error occurs
         */
        public void add(final PlaneConvexSubset boundary) {
            if (boundary.isInfinite()) {
                throw new IllegalArgumentException("OBJ input geometry cannot be infinite: " + boundary);
            } else if (!boundary.isEmpty()) {
                addFace(boundary.getVertices(), null);
            }
        }

        /** Add a vertex to the buffer.
         * @param vertex vertex to add
         * @return the index of the vertex in the buffer
         */
        public int addVertex(final Vector3D vertex) {
            return addToMap(vertex, vertexMap);
        }

        /** Add a normal to the buffer.
         * @param normal normal to add
         * @return the index of the normal in the buffer
         */
        public int addNormal(final Vector3D normal) {
            return addToMap(normal, normalMap);
        }

        /** Flush the buffer content to the output and reset its state.
         * @throws java.io.UncheckedIOException if an I/O error occurs
         */
        public void flush() {
            final int vertexOffset = vertexCount;
            final int normalOffset = normalCount;

            // write vertices
            for (final String vertexStr : vertexMap.keySet()) {
                writeVertexLine(vertexStr);
            }

            // write normals
            for (final String normalStr : normalMap.keySet()) {
                writeVertexNormalLine(normalStr);
            }

            // write faces
            Integer normalIndex;
            int[] normalIndices;
            int faceIndex = 0;
            for (final int[] vertexIndices : faceVertices) {
                normalIndex = faceToNormalMap.get(faceIndex);
                if (normalIndex != null) {
                    normalIndices = new int[vertexIndices.length];
                    Arrays.fill(normalIndices, normalIndex);
                } else {
                    normalIndices = null;
                }

                writeFaceWithOffsets(vertexOffset, vertexIndices, normalOffset, normalIndices);

                ++faceIndex;
            }

            reset();
        }

        /** Convert the given vector to on OBJ definition string and add it to the
         * map if not yet present. The mapped index of the vector is returned.
         * @param vec vector to add
         * @param map map to add the vector to
         * @return the index the vector entry is mapped to
         */
        private int addToMap(final Vector3D vec, final Map<String, Integer> map) {
            final String str = createVectorString(vec);

            return map.computeIfAbsent(str, k -> map.size());
        }

        /** Add a face to the buffer. If {@code batchSize} is greater than {@code -1} and the number
         * of currently stored faces is greater than or equal to {@code batchSize}, then the buffer
         * content is written to the output and the buffer state is reset.
         * @param vertices face vertices
         * @param normal face normal; may be null
         * @throws java.io.UncheckedIOException if an I/O error occurs
         */
        private void addFace(final List<Vector3D> vertices, final Vector3D normal) {
            final int faceIndex = faceVertices.size();

            final int[] vertexIndices = new int[vertices.size()];

            int i = -1;
            for (final Vector3D vertex : vertices) {
                vertexIndices[++i] = addVertex(vertex);
            }
            faceVertices.add(vertexIndices);

            if (normal != null) {
                faceToNormalMap.put(faceIndex, addNormal(normal));
            }

            if (batchSize > -1 && faceVertices.size() >= batchSize) {
                flush();
            }
        }

        /** Reset the buffer state.
         */
        private void reset() {
            vertexMap.clear();
            normalMap.clear();
            faceVertices.clear();
            faceToNormalMap.clear();
        }
    }
}
