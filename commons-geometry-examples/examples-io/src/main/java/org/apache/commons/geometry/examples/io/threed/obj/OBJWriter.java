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
import java.io.Writer;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.Mesh;

/** Class for writing OBJ files containing 3D mesh data.
 */
public final class OBJWriter implements AutoCloseable {

    /** Space character. */
    private static final char SPACE = ' ';

    /** The default maximum number of fraction digits in formatted numbers. */
    private static final int DEFAULT_MAXIMUM_FRACTION_DIGITS = 6;

    /** The default line separator value. This is not directly specified by the OBJ format
     * but the value used here matches that
     * <a href="https://docs.blender.org/manual/en/2.80/addons/io_scene_obj.html">used by Blender</a>.
     */
    private static final String DEFAULT_LINE_SEPARATOR = "\n";

    /** Underlying writer instance. */
    private Writer writer;

    /** Line separator string. */
    private String lineSeparator = DEFAULT_LINE_SEPARATOR;

    /** Decimal formatter. */
    private DecimalFormat decimalFormat;

    /** Number of vertices written to the output. */
    private int vertexCount = 0;

    /** Create a new instance for writing to the given file.
     * @param file file to write to
     * @throws IOException if an IO operation fails
     */
    public OBJWriter(final File file) throws IOException {
        this(Files.newBufferedWriter(file.toPath(), OBJConstants.DEFAULT_CHARSET));
    }

    /** Create a new instance that writes output with the given writer.
     * @param writer writer used to write output
     */
    public OBJWriter(final Writer writer) {
        this.writer = writer;

        this.decimalFormat = new DecimalFormat();
        this.decimalFormat.setMaximumFractionDigits(DEFAULT_MAXIMUM_FRACTION_DIGITS);
    }

    /** Get the current line separator. This value defaults to {@value #DEFAULT_LINE_SEPARATOR}.
     * @return the current line separator
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /** Set the line separator.
     * @param lineSeparator the line separator to use
     */
    public void setLineSeparator(final String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    /** Get the {@link DecimalFormat} instance used to format floating point output.
     * @return the decimal format instance
     */
    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    /** Set the {@link DecimalFormat} instance used to format floatin point output.
     * @param decimalFormat decimal format instance
     */
    public void setDecimalFormat(final DecimalFormat decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    /** Write an OBJ comment with the given value.
     * @param comment comment to write
     * @throws IOException if an IO operation fails
     */
    public void writeComment(final String comment) throws IOException {
        for (final String line : comment.split("\r?\n")) {
            writer.write(OBJConstants.COMMENT_START_CHAR);
            writer.write(SPACE);
            writer.write(line);
            writer.write(lineSeparator);
        }
    }

    /** Write an object name to the output. This is metadata for the file and
     * does not affect the geometry, although it may affect how the file content
     * is read by other programs.
     * @param objectName the name to write
     * @throws IOException if an IO operation fails
     */
    public void writeObjectName(final String objectName) throws IOException {
        writer.write(OBJConstants.OBJECT_KEYWORD);
        writer.write(SPACE);
        writer.write(objectName);
        writer.write(lineSeparator);
    }

    /** Write a group name to the output. This is metadata for the file and
     * does not affect the geometry, although it may affect how the file content
     * is read by other programs.
     * @param groupName the name to write
     * @throws IOException if an IO operation fails
     */
    public void writeGroupName(final String groupName) throws IOException {
        writer.write(OBJConstants.GROUP_KEYWORD);
        writer.write(SPACE);
        writer.write(groupName);
        writer.write(lineSeparator);
    }

    /** Write a vertex to the output. The OBJ 1-based index of the vertex is returned. This
     * index can be used to reference the vertex in faces via {@link #writeFace(int...)}.
     * @param vertex vertex to write
     * @throws IOException if an IO operation fails
     * @return the index of the written vertex in the OBJ 1-based convention
     * @throws IOException if an IO operation fails
     */
    public int writeVertex(final Vector3D vertex) throws IOException {
        writer.write(OBJConstants.VERTEX_KEYWORD);
        writer.write(SPACE);
        writer.write(decimalFormat.format(vertex.getX()));
        writer.write(SPACE);
        writer.write(decimalFormat.format(vertex.getY()));
        writer.write(SPACE);
        writer.write(decimalFormat.format(vertex.getZ()));
        writer.write(lineSeparator);

        return ++vertexCount;
    }

    /** Write a face with the given vertex indices, specified in the OBJ 1-based
     * convention. Callers are responsible for ensuring that the indices are valid.
     * @param vertexIndices vertex indices for the face, in the 1-based OBJ convention
     * @throws IOException if an IO operation fails
     */
    public void writeFace(final int... vertexIndices) throws IOException {
        writeFaceWithVertexOffset(0, vertexIndices);
    }

    /** Write the boundaries present in the given boundary source. If the argument is a {@link Mesh},
     * it is written using {@link #writeMesh(Mesh)}. Otherwise, each boundary is written to the output
     * separately.
     * @param boundarySource boundary source containing the boundaries to write to the output
     * @throws IllegalArgumentException if any boundary in the argument is infinite
     * @throws IOException if an IO operation fails
     */
    public void writeBoundaries(final BoundarySource3D boundarySource) throws IOException {
        if (boundarySource instanceof Mesh) {
            writeMesh((Mesh<?>) boundarySource);
        } else {
            try (Stream<PlaneConvexSubset> stream = boundarySource.boundaryStream()) {
                writeBoundaries(stream.iterator());
            }
        }
    }

    /** Write the boundaries in the argument to the output. Each boundary is written separately.
     * @param it boundary iterator
     * @throws IllegalArgumentException if any boundary in the argument is infinite
     * @throws IOException if an IO operation fails
     */
    private void writeBoundaries(final Iterator<PlaneConvexSubset> it) throws IOException {
        PlaneConvexSubset boundary;
        List<Vector3D> vertices;
        int[] vertexIndices;

        while (it.hasNext()) {
            boundary = it.next();
            if (boundary.isInfinite()) {
                throw new IllegalArgumentException("OBJ input geometry cannot be infinite: " + boundary);
            }

            vertices = boundary.getVertices();
            vertexIndices = new int[vertices.size()];

            for (int i = 0; i < vertexIndices.length; ++i) {
                vertexIndices[i] = writeVertex(vertices.get(i));
            }

            writeFace(vertexIndices);
        }
    }

    /** Write a mesh to the output.
     * @param mesh the mesh to write
     * @throws IOException if an IO operation fails
     */
    public void writeMesh(final Mesh<?> mesh) throws IOException {
        final int vertexOffset = vertexCount + 1;

        for (final Vector3D vertex : mesh.vertices()) {
            writeVertex(vertex);
        }

        for (final Mesh.Face face : mesh.faces()) {
            writeFaceWithVertexOffset(vertexOffset, face.getVertexIndices());
        }
    }

    /** Write a face with the given vertex offset value and indices. The offset is added to each
     * index before being written.
     * @param vertexOffset vertex offset value
     * @param vertexIndices vertex indices for the face
     * @throws IOException if an IO operation fails
     */
    private void writeFaceWithVertexOffset(final int vertexOffset, final int... vertexIndices)
            throws IOException {
        if (vertexIndices.length < 3) {
            throw new IllegalArgumentException("Face must have more than 3 vertices; found " + vertexIndices.length);
        }

        writer.write(OBJConstants.FACE_KEYWORD);

        for (final int vertexIndex : vertexIndices) {
            writer.write(SPACE);
            writer.write(String.valueOf(vertexIndex + vertexOffset));
        }

        writer.write(lineSeparator);
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
        writer = null;
    }
}
