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

import java.nio.ByteBuffer;

import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Utility methods for the STL format.
 */
final class StlUtils {

    /** Utility class; no instantiation. */
    private StlUtils() { }

    /** Create a {@link ByteBuffer} with the given size and the byte order
     * appropriate for binary STL content.
     * @param capacity buffer capacity
     * @return byte buffer
     */
    static ByteBuffer byteBuffer(final int capacity) {
        return ByteBuffer.allocate(capacity)
                .order(StlConstants.BINARY_BYTE_ORDER);
    }

    /** Determine the normal that should be used for the given STL triangle vertices. If {@code normal}
     * is present and can be normalized, it is returned. Otherwise, a normal is attempted to be computed
     * using the given triangle vertices. If normal computation fails, the zero vector is returned.
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @param normal defined triangle normal; may be null
     * @return STL normal for the triangle
     */
    static Vector3D determineNormal(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final Vector3D normal) {
        if (normal != null) {
            // try to normalize it
            final Vector3D normalized = normal.normalizeOrNull();
            if (normalized != null) {
                return normalized;
            }
        }

        // try to compute one from the triangle points
        final Vector3D computed = computeTriangleNormal(p1, p2, p3);
        return computed != null ?
                computed :
                Vector3D.ZERO;
    }

    /** Return true if the given points are arranged counter-clockwise relative to the
     * given normal. Returns true if {@code normal} is null.
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @param normal normal; may be null, in which case the zero vector is used
     * @return true if {@code normal} is null or if the given points are arranged counter-clockwise
     *      relative to {@code normal}
     */
    static boolean pointsAreCounterClockwise(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final Vector3D normal) {
        if (normal != null) {
            final Vector3D computedNormal = computeTriangleNormal(p1, p2, p3);
            if (computedNormal != null && normal.dot(computedNormal) < 0) {
                return false;
            }
        }

        return true;
    }

    /** Get the normal using the right-hand rule for the given triangle vertices. Null is returned
     * if the normal could not be computed.
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     * @return the normal for the given triangle vertices or null if one could not be computed
     */
    private static Vector3D computeTriangleNormal(final Vector3D p1, final Vector3D p2, final Vector3D p3) {
        final Vector3D normal = p1.vectorTo(p2).cross(p1.vectorTo(p3)).normalizeOrNull();
        return normal != null ?
                normal :
                null;
    }
}
