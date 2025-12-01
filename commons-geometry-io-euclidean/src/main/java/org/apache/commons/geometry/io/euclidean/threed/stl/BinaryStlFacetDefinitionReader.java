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

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.io.core.internal.GeometryIOUtils;
import org.apache.commons.geometry.io.euclidean.threed.FacetDefinitionReader;

/** Class used to read the binary form of the STL file format.
 * @see <a href="https://en.wikipedia.org/wiki/STL_(file_format)#Binary_STL">Binary STL</a>
 */
public class BinaryStlFacetDefinitionReader implements FacetDefinitionReader {

    /** Input stream to read from. */
    private final InputStream in;

    /** Buffer used to read triangle definitions. */
    private final ByteBuffer triangleBuffer = StlUtils.byteBuffer(StlConstants.BINARY_TRIANGLE_BYTES);

    /** Header content. */
    private final ByteBuffer header = StlUtils.byteBuffer(StlConstants.BINARY_HEADER_BYTES);

    /** Total number of triangles declared to be present in the input. */
    private long triangleTotal;

    /** Number of triangles read so far. */
    private long trianglesRead;

    /** True when the header content has been read. */
    private boolean hasReadHeader;

    /** Construct a new instance that reads from the given input stream.
     * @param in input stream to read from.
     */
    public BinaryStlFacetDefinitionReader(final InputStream in) {
        this.in = in;
    }

    /** Get a read-only buffer containing the 80 bytes of the STL header. The header does not
     * include the 4-byte value indicating the total number of triangles in the STL file.
     * @return the STL header content
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public ByteBuffer getHeader() {
        beginRead();
        return ByteBuffer.wrap(header.array().clone());
    }

    /** Return the header content as a string decoded using the UTF-8 charset. Control
     * characters (such as '\0') are not included in the result.
     * @return the header content decoded as a UTF-8 string
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public String getHeaderAsString() {
        return getHeaderAsString(StlConstants.DEFAULT_CHARSET);
    }

    /** Return the header content as a string decoded using the given charset. Control
     * characters (such as '\0') are not included in the result.
     * @param charset charset to decode the header with
     * @return the header content decoded as a string
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public String getHeaderAsString(final Charset charset) {
        // decode the entire header as characters in the given charset
        final String raw = charset.decode(getHeader()).toString();

        // strip out any control characters, such as '\0'
        final StringBuilder sb = new StringBuilder();
        for (final char c : raw.toCharArray()) {
            if (!Character.isISOControl(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /** Get the total number of triangles (i.e. facets) declared to be present in the input.
     * @return total number of triangle in the input
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    public long getNumTriangles() {
        beginRead();
        return triangleTotal;
    }

    /** {@inheritDoc} */
    @Override
    public BinaryStlFacetDefinition readFacet() {
        beginRead();

        BinaryStlFacetDefinition facet = null;

        if (trianglesRead < triangleTotal) {
            facet = readFacetInternal();

            ++trianglesRead;
        }

        return facet;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        GeometryIOUtils.closeUnchecked(in);
    }

    /** Read the file header content and triangle count.
     * @throws IllegalStateException is a parse error occurs
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private void beginRead() {
        if (!hasReadHeader) {
            // read header content
            final int headerBytesRead = GeometryIOUtils.applyAsIntUnchecked(in::read, header.array());
            if (headerBytesRead < StlConstants.BINARY_HEADER_BYTES) {
                throw dataNotAvailable("header");
            }

            header.rewind();

            // read the triangle total
            final ByteBuffer triangleBuf = StlUtils.byteBuffer(Integer.BYTES);

            if (fill(triangleBuf) < triangleBuf.capacity()) {
                throw dataNotAvailable("triangle count");
            }

            triangleTotal = Integer.toUnsignedLong(triangleBuf.getInt());

            hasReadHeader = true;
        }
    }

    /** Internal method to read a single facet from the input.
     * @return facet read from the input
     */
    private BinaryStlFacetDefinition readFacetInternal() {
        if (fill(triangleBuffer) < triangleBuffer.capacity()) {
            throw dataNotAvailable("triangle at index " + trianglesRead);
        }

        final Vector3D normal = readVector(triangleBuffer);
        final Vector3D p1 = readVector(triangleBuffer);
        final Vector3D p2 = readVector(triangleBuffer);
        final Vector3D p3 = readVector(triangleBuffer);

        final int attr = Short.toUnsignedInt(triangleBuffer.getShort());

        return new BinaryStlFacetDefinition(Arrays.asList(p1, p2, p3), normal, attr);
    }

    /** Fill the buffer with data from the input stream. The buffer is then flipped and
     * made ready for reading.
     * @param buf buffer to fill
     * @return number of bytes read
     * @throws java.io.UncheckedIOException if an I/O error occurs
     */
    private int fill(final ByteBuffer buf) {
        final int read = GeometryIOUtils.applyAsIntUnchecked(in::read, buf.array());
        buf.rewind();

        return read;
    }

    /** Read a vector from the given byte buffer.
     * @param buf buffer to read from
     * @return vector containing the next 3 double values from the
     *      given buffer
     */
    private Vector3D readVector(final ByteBuffer buf) {
        final double x = buf.getFloat();
        final double y = buf.getFloat();
        final double z = buf.getFloat();

        return Vector3D.of(x, y, z);
    }

    /** Return an exception stating that data is not available for the file
     * component with the given name.
     * @param name name of the file component missing data
     * @return exception instance
     */
    private static IllegalStateException dataNotAvailable(final String name) {
        return GeometryIOUtils.parseError("Failed to read STL " + name + ": data not available");
    }
}
