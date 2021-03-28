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

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.utils.AbstractTextFormatWriter;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;

/** Class for writing the text-based (i.e., "ASCII") STL format.
 * @see <a href="https://en.wikipedia.org/wiki/STL_%28file_format%29#ASCII_STL">ASCII STL</a>
 */
public class TextStlWriter extends AbstractTextFormatWriter {

    /** Space character. */
    private static final char SPACE = ' ';

    /** Name of the current STL solid. */
    private String name;

    /** True if an STL solid definition has been written. */
    private boolean started;

    /** Construct a new instance for writing STL content to the given writer.
     * @param writer writer to write to
     */
    public TextStlWriter(final Writer writer) {
        super(writer);
    }

    /** Write the start of an unnamed STL solid definition. This method is equivalent to calling
     * {@code stlWriter.startSolid(null);}
     * @throws IOException if an I/O error occurs
     */
    public void startSolid() throws IOException {
        startSolid(null);
    }

    /** Write the start of an STL solid definition with the given name.
     * @param solidName the name of the solid; may be null
     * @throws IllegalStateException if a solid definition has already been started
     * @throws IllegalArgumentException if {@code solidName} contains new line characters
     * @throws IOException if an I/O error occurs
     */
    public void startSolid(final String solidName) throws IOException {
        if (started) {
            throw new IllegalStateException("Cannot start solid definition: a solid is already being written");
        }
        if (solidName != null && (solidName.indexOf('\r') > -1 || solidName.indexOf('\n') > -1)) {
            throw new IllegalArgumentException("Solid name cannot contain new line characters");
        }

        name = solidName;
        writeBeginOrEndLine(StlConstants.SOLID_START_KEYWORD);

        started = true;
    }

    /** Write the end of the current STL solid definition. This method is called automatically on
     * {@link #close()} if needed.
     * @throws IllegalStateException if no solid definition has been started
     * @throws IOException if an I/O error occurs
     */
    public void endSolid() throws IOException {
        if (!started) {
            throw new IllegalStateException("Cannot end solid definition: no solid has been started");
        }

        writeBeginOrEndLine(StlConstants.SOLID_END_KEYWORD);
        name = null;
        started = false;
    }

    /** Write the given boundary to the output as triangles.
     * @param boundary boundary to write
     * @throws IllegalStateException if no solid has been started yet
     * @throws IOException if an I/O error occurs
     * @see PlaneConvexSubset#toTriangles()
     */
    public void writeTriangles(final PlaneConvexSubset boundary) throws IOException {
        for (final Triangle3D tri : boundary.toTriangles()) {
            writeTriangles(tri.getVertices(), tri.getPlane().getNormal());
        }
    }

    /** Write the given facet definition to the output as triangles.
     * @param facet facet definition to write
     * @throws IllegalStateException if no solid has been started yet
     * @throws IOException if an I/O error occurs
     * @see #writeTriangle(Vector3D, Vector3D, Vector3D, Vector3D)
     */
    public void writeTriangles(final FacetDefinition facet) throws IOException {
        writeTriangles(facet.getVertices(), facet.getNormal());
    }

    /** Write the facet defined by the given vertices and normal to the output as triangles.
     * If the the given list of vertices contains more than 3 vertices, it is converted to
     * triangles using a triangle fan. Callers are responsible for ensuring that the given
     * vertices represent a valid convex polygon.
     *
     * <p>If a non-zero normal is given, the vertices are ordered using the right-hand rule,
     * meaning that they will be in a counter-clockwise orientation when looking down
     * the normal. If no normal is given, or the given value cannot be normalized, a normal
     * is computed from the triangle vertices, also using the right-hand rule. If this also
     * fails (for example, if the triangle vertices do not define a plane), then the
     * zero vector is used.</p>
     * @param vertices vertices defining the facet
     * @param normal facet normal; may be null
     * @throws IllegalStateException if no solid has been started yet or fewer than 3 vertices
     *      are given
     * @throws IOException if an I/O error occurs
     */
    public void writeTriangles(final List<Vector3D> vertices, final Vector3D normal) throws IOException {
        for (final List<Vector3D> triangle : Planes.convexPolygonToTriangleFan(vertices, t -> t)) {
            writeTriangle(
                    triangle.get(0),
                    triangle.get(1),
                    triangle.get(2),
                    normal);
        }
    }

    /** Write a triangle to the output.
     *
     * <p>If a non-zero normal is given, the vertices are ordered using the right-hand rule,
     * meaning that they will be in a counter-clockwise orientation when looking down
     * the normal. If no normal is given, or the given value cannot be normalized, a normal
     * is computed from the triangle vertices, also using the right-hand rule. If this also
     * fails (for example, if the triangle vertices do not define a plane), then the
     * zero vector is used.</p>
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @param normal facet normal; may be null
     * @throws IllegalStateException if no solid has been started yet
     * @throws IOException if an I/O error occurs
     */
    public void writeTriangle(final Vector3D p1, final Vector3D p2, final Vector3D p3, final Vector3D normal)
            throws IOException {
        if (!started) {
            throw new IllegalStateException("Cannot write triangle: no solid has been started");
        }

        write(StlConstants.FACET_START_KEYWORD);
        write(SPACE);
        writeVector(StlUtils.determineNormal(p1, p2, p3, normal));
        writeNewLine();

        write(StlConstants.OUTER_KEYWORD);
        write(SPACE);
        write(StlConstants.LOOP_START_KEYWORD);
        writeNewLine();

        writeTriangleVertex(p1);

        if (StlUtils.pointsAreCounterClockwise(p1, p2, p3, normal)) {
            writeTriangleVertex(p2);
            writeTriangleVertex(p3);
        } else {
            writeTriangleVertex(p3);
            writeTriangleVertex(p2);
        }

        write(StlConstants.LOOP_END_KEYWORD);
        writeNewLine();

        write(StlConstants.FACET_END_KEYWORD);
        writeNewLine();
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException {
        if (started) {
            endSolid();
        }

        super.close();
    }

    /** Write a triangle vertex to the output.
     * @param vertex triangle vertex
     * @throws IOException if an I/O error occurs
     */
    private void writeTriangleVertex(final Vector3D vertex) throws IOException {
        write(StlConstants.VERTEX_KEYWORD);
        write(SPACE);
        writeVector(vertex);
        writeNewLine();
    }

    /** Write a vector to the output.
     * @param vec vector to write
     * @throws IOException if an I/O error occurs
     */
    private void writeVector(final Vector3D vec) throws IOException {
        write(vec.getX());
        write(SPACE);
        write(vec.getY());
        write(SPACE);
        write(vec.getZ());
    }

    /** Write the beginning or ending line of the solid definition.
     * @param keyword keyword at the start of the line
     * @throws IOException if an I/O error occurs
     */
    private void writeBeginOrEndLine(final String keyword) throws IOException {
        write(keyword);
        write(SPACE);

        if (name != null) {
            write(name);
        }

        writeNewLine();
    }
}
