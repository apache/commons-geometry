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

import java.io.Closeable;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;

/** Low-level class for writing binary STL content.
 */
public class BinaryStlWriter implements Closeable {

    /** Output stream to write to. */
    private final OutputStream out;

    /** Buffer used to construct triangle definitions. */
    private final ByteBuffer triangleBuffer = StlUtils.byteBuffer(StlConstants.BINARY_TRIANGLE_BYTES);

    /** Construct a new instance for writing to the given output.
     * @param out output stream to write to
     */
    public BinaryStlWriter(final OutputStream out) {
        this.out = out;
    }

    /** Write binary STL header content. If {@code headerContent} is {@code null}, the written header
     * will consist entirely of zeros. Otherwise, up to 80 bytes from {@code headerContent}
     * are written to the header, with any remaining bytes of the header filled with zeros.
     * @param headerContent bytes to include in the header; may be {@code null}
     * @param triangleCount number of triangles to be included in the content
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeHeader(final byte[] headerContent, final int triangleCount) {
        writeHeader(headerContent, triangleCount, out);
    }

    /** Write a triangle to the output using a default attribute value of 0.
     * Callers are responsible for ensuring that the number of triangles written
     * matches the number given in the header.
     *
     * <p>If a normal is given, the vertices are ordered using the right-hand rule,
     * meaning that they will be in a counter-clockwise orientation when looking down
     * the normal. Thus, the given point ordering may not be the ordering used in
     * the written content.</p>
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @param normal triangle normal; may be {@code null}
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeTriangle(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final Vector3D normal) {
        writeTriangle(p1, p2, p3, normal, 0);
    }

    /** Write a triangle to the output. Callers are responsible for ensuring
     * that the number of triangles written matches the number given in the header.
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
     * @param normal triangle normal; may be {@code null}
     * @param attributeValue 2-byte STL triangle attribute value
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public void writeTriangle(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final Vector3D normal, final int attributeValue) {
        triangleBuffer.rewind();

        putVector(StlUtils.determineNormal(p1, p2, p3, normal));
        putVector(p1);

        if (StlUtils.pointsAreCounterClockwise(p1, p2, p3, normal)) {
            putVector(p2);
            putVector(p3);
        } else {
            putVector(p3);
            putVector(p2);
        }

        triangleBuffer.putShort((short) attributeValue);

        GeometryIOUtils.acceptUnchecked(out::write, triangleBuffer.array());
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        GeometryIOUtils.closeUnchecked(out);
    }

    /** Put all double components of {@code vec} into the internal buffer.
     * @param vec vector to place into the buffer
     */
    private void putVector(final Vector3D vec) {
        triangleBuffer.putFloat((float) vec.getX());
        triangleBuffer.putFloat((float) vec.getY());
        triangleBuffer.putFloat((float) vec.getZ());
    }

    /** Write binary STL header content to the given output stream. If {@code headerContent}
     * is {@code null}, the written header will consist entirely of zeros. Otherwise, up to 80 bytes
     * from {@code headerContent} are written to the header, with any remaining bytes of the
     * header filled with zeros.
     * @param headerContent
     * @param triangleCount
     * @param out
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    static void writeHeader(final byte[] headerContent, final int triangleCount, final OutputStream out) {
        // write the header
        final byte[] bytes = new byte[StlConstants.BINARY_HEADER_BYTES];
        if (headerContent != null) {
            System.arraycopy(
                    headerContent, 0,
                    bytes, 0,
                    Math.min(headerContent.length, StlConstants.BINARY_HEADER_BYTES));
        }

        GeometryIOUtils.acceptUnchecked(out::write, bytes);

        // write the triangle count number
        final ByteBuffer countBuffer = StlUtils.byteBuffer(Integer.BYTES);
        countBuffer.putInt(triangleCount);
        countBuffer.flip();

        GeometryIOUtils.acceptUnchecked(out::write, countBuffer.array());
    }
}
