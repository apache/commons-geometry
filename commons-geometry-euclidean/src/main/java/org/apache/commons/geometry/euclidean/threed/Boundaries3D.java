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
package org.apache.commons.geometry.euclidean.threed;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Utility class for constructing {@link BoundarySource3D} instances.
 */
public final class Boundaries3D {

    /** Private constructor. */
    private Boundaries3D() {
    }

    /** Return a {@link BoundarySource3D} instance containing the given convex subplanes.
     * @param boundaries convex subplanes to include in the boundary source
     * @return a boundary source containing the given boundaries
     */
    public static BoundarySource3D from(final ConvexSubPlane... boundaries) {
        return from(Arrays.asList(boundaries));
    }

    /** Return a {@link BoundarySource3D} instance containing the given convex subplanes. The given
     * collection is used directly as the source of the subplanes; no copy is made.
     * @param boundaries convex subplanes to include in the boundary source
     * @return a boundary source containing the given boundaries
     */
    public static BoundarySource3D from(final Collection<ConvexSubPlane> boundaries) {
        return () -> boundaries.stream();
    }

    /** Return a {@link BoundarySource3D} instance defining an axis-aligned rectangular prism. The points {@code a}
     * and {@code b} are taken to represent opposite corner points in the prism and may be specified in
     * any order.
     * @param a first corner point in the prism (opposite of {@code b})
     * @param b second corner point in the prism (opposite of {@code a})
     * @param precision precision context used to construct boundary instances
     * @return a boundary source defining the boundaries of the rectangular prism
     * @throws IllegalArgumentException if the width, height, or depth of the defined prism is zero
     *      as evaluated by the precision context.
     */
    public static BoundarySource3D rect(final Vector3D a, final Vector3D b, final DoublePrecisionContext precision) {

        final double minX = Math.min(a.getX(), b.getX());
        final double maxX = Math.max(a.getX(), b.getX());

        final double minY = Math.min(a.getY(), b.getY());
        final double maxY = Math.max(a.getY(), b.getY());

        final double minZ = Math.min(a.getZ(), b.getZ());
        final double maxZ = Math.max(a.getZ(), b.getZ());

        if (precision.eq(minX, maxX) || precision.eq(minY, maxY) || precision.eq(minZ, maxZ)) {
            throw new IllegalArgumentException("Rectangular prism has zero size: " + a + ", " + b + ".");
        }

        final Vector3D[] vertices = {
            Vector3D.of(minX, minY, minZ),
            Vector3D.of(maxX, minY, minZ),
            Vector3D.of(maxX, maxY, minZ),
            Vector3D.of(minX, maxY, minZ),

            Vector3D.of(minX, minY, maxZ),
            Vector3D.of(maxX, minY, maxZ),
            Vector3D.of(maxX, maxY, maxZ),
            Vector3D.of(minX, maxY, maxZ)
        };

        return from(
            // -z and +z sides
            ConvexSubPlane.fromVertexLoop(Arrays.asList(vertices[0], vertices[3], vertices[2], vertices[1]), precision),
            ConvexSubPlane.fromVertexLoop(Arrays.asList(vertices[4], vertices[5], vertices[6], vertices[7]), precision),

            // -x and +x sides
            ConvexSubPlane.fromVertexLoop(Arrays.asList(vertices[0], vertices[4], vertices[7], vertices[3]), precision),
            ConvexSubPlane.fromVertexLoop(Arrays.asList(vertices[5], vertices[1], vertices[2], vertices[6]), precision),

            // -y and +y sides
            ConvexSubPlane.fromVertexLoop(Arrays.asList(vertices[0], vertices[1], vertices[5], vertices[4]), precision),
            ConvexSubPlane.fromVertexLoop(Arrays.asList(vertices[3], vertices[7], vertices[6], vertices[2]), precision)
        );
    }
}
