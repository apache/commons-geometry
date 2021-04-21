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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.internal.EuclideanInternals;
import org.apache.commons.geometry.euclidean.threed.BoundarySource3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.io.core.GeometryFormat;
import org.apache.commons.geometry.io.core.output.GeometryOutput;
import org.apache.commons.geometry.io.euclidean.threed.AbstractBoundaryWriteHandler3D;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinition;
import org.apache.commons.geometry.io.euclidean.threed.GeometryFormat3D;

/** {@link org.apache.commons.geometry.io.euclidean.threed.BoundaryWriteHandler3D BoundaryWriteHandler3D}
 * implementation for writing STL content. Because of its compact nature, all STL content is written in
 * binary format, as opposed the text (i.e. "ASCII") format. Callers should use the {@link TextStlWriter}
 * class directly in order to create text STL content.
 */
public class StlBoundaryWriteHandler3D extends AbstractBoundaryWriteHandler3D {

    /** Initial size of the data buffer. */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * StlConstants.BINARY_TRIANGLE_BYTES;

    /** Initial size of data buffers used during write operations. */
    private int initialBufferSize = DEFAULT_BUFFER_SIZE;

    /** {@inheritDoc} */
    @Override
    public GeometryFormat getFormat() {
        return GeometryFormat3D.STL;
    }

    /** Get the initial size of the data buffers used by this instance.
     *
     * <p>The buffer is used in situations where it is not clear how many
     * triangles will ultimately be written to the output. In these cases, the
     * triangle data is first written to an internal buffer. Once all triangles are
     * written, the STL header containing the total triangle count is written
     * to the output, followed by the buffered triangle data.</p>
     * @return initial buffer size
     */
    public int getinitialBufferSize() {
        return initialBufferSize;
    }

    /** Set the initial size of the data buffers used by this instance.
     *
     * <p>The buffer is used in situations where it is not clear how many
     * triangles will ultimately be written to the output. In these cases, the
     * triangle data is first written to an internal buffer. Once all triangles are
     * written, the STL header containing the total triangle count is written
     * to the output, followed by the buffered triangle data.</p>
     * @param initialBufferSize initial buffer size
     */
    public void setInitialBufferSize(final int initialBufferSize) {
        if (initialBufferSize < 1) {
            throw new IllegalArgumentException("Buffer size must be greater than 0");
        }
        this.initialBufferSize = initialBufferSize;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final BoundarySource3D src, final GeometryOutput out)
            throws IOException {
        // handle cases where we know the number of triangles to be written up front
        // and do not need to buffer the content
        if (src instanceof TriangleMesh) {
            writeTriangleMesh((TriangleMesh) src, out);
        } else {
            // unknown number of triangles; proceed with a buffered write
            super.write(src, out);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void write(final Stream<? extends PlaneConvexSubset> boundaries, GeometryOutput out)
            throws IOException {

        // write the triangle data to a buffer and track how many we write
        int triangleCount = 0;
        final ByteArrayOutputStream triangleBuffer = new ByteArrayOutputStream(initialBufferSize);

        try (BinaryStlWriter stlWriter = new BinaryStlWriter(triangleBuffer)) {
            final Iterator<? extends PlaneConvexSubset> it = boundaries.iterator();

            while (it.hasNext()) {
                for (final Triangle3D tri : it.next().toTriangles()) {

                    stlWriter.writeTriangle(
                            tri.getPoint1(),
                            tri.getPoint2(),
                            tri.getPoint3(),
                            tri.getPlane().getNormal());

                    ++triangleCount;
                }
            }
        }

        // write the header and copy the data
        try (OutputStream os = out.getOutputStream()) {
            BinaryStlWriter.writeHeader(null, triangleCount, os);
            triangleBuffer.writeTo(os);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void writeFacets(final Stream<? extends FacetDefinition> facets, final GeometryOutput out)
            throws IOException {

        // write the triangle data to a buffer and track how many we write
        int triangleCount = 0;
        final ByteArrayOutputStream triangleBuffer = new ByteArrayOutputStream(initialBufferSize);

        try (BinaryStlWriter dataWriter = new BinaryStlWriter(triangleBuffer)) {
            final Iterator<? extends FacetDefinition> it = facets.iterator();

            FacetDefinition facet;
            int attributeValue;

            while (it.hasNext()) {
                facet = it.next();
                attributeValue = getFacetAttributeValue(facet);

                for (final List<Vector3D> tri :
                    EuclideanInternals.convexPolygonToTriangleFan(facet.getVertices(), t -> t)) {

                    dataWriter.writeTriangle(
                            tri.get(0),
                            tri.get(1),
                            tri.get(2),
                            facet.getNormal(),
                            attributeValue);

                    ++triangleCount;
                }
            }
        }

        // write the header and copy the data
        try (OutputStream os = out.getOutputStream()) {
            BinaryStlWriter.writeHeader(null, triangleCount, os);
            triangleBuffer.writeTo(os);
        }
    }

    /** Write all triangles in the given mesh to the output using the binary STL
     * format.
     * @param mesh mesh to write
     * @param output output to write to
     * @throws IOException if an I/O error occurs
     */
    private void writeTriangleMesh(final TriangleMesh mesh, final GeometryOutput output)
            throws IOException {
        try (BinaryStlWriter stlWriter = new BinaryStlWriter(output.getOutputStream())) {
            // write the header
            stlWriter.writeHeader(null, mesh.getFaceCount());

            // write each triangle
            Triangle3D tri;
            for (final TriangleMesh.Face face : mesh.faces()) {
                tri = face.getPolygon();

                stlWriter.writeTriangle(
                        tri.getPoint1(),
                        tri.getPoint2(),
                        tri.getPoint3(),
                        tri.getPlane().getNormal());
            }
        }
    }

    /** Get the attribute value that should be used for the given facet.
     * @param facet facet to get the attribute value for
     * @return attribute value
     */
    private int getFacetAttributeValue(final FacetDefinition facet) {
        if (facet instanceof BinaryStlFacetDefinition) {
            return ((BinaryStlFacetDefinition) facet).getAttributeValue();
        }

        return 0;
    }
}
