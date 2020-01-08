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
package org.apache.commons.geometry.euclidean.threed.shapes;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Facet;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Class containing utility methods for constructing parallelepipeds. Parallelepipeds
 * are 3 dimensional figures formed by six parallelograms. For example, cubes and rectangular
 * prisms are parallelepipeds.
 * @see <a href="https://en.wikipedia.org/wiki/Parallelepiped">Parallelepiped</a>
 */
public final class Parallelepiped {

    /** Utility class; no instantiation.
     */
    private Parallelepiped() {
    }

    /** Return a list of {@link Facet}s defining an axis-aligned parallelepiped, ie, a rectangular prism.
     * The points {@code a} and {@code b} are taken to represent opposite corner points in the prism and may be
     * specified in any order.
     * @param a first corner point in the prism (opposite of {@code b})
     * @param b second corner point in the prism (opposite of {@code a})
     * @param precision precision context used to construct facet instances
     * @return a list containing the boundaries of the rectangular prism
     * @throws IllegalArgumentException if the width, height, or depth of the defined prism is zero
     *      as evaluated by the precision context.
     */
    public static List<Facet> axisAligned(final Vector3D a, final Vector3D b,
            final DoublePrecisionContext precision) {

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

        return Arrays.asList(
            // -z and +z sides
            Facet.fromVertexLoop(Arrays.asList(vertices[0], vertices[3], vertices[2], vertices[1]), precision),
            Facet.fromVertexLoop(Arrays.asList(vertices[4], vertices[5], vertices[6], vertices[7]), precision),

            // -x and +x sides
            Facet.fromVertexLoop(Arrays.asList(vertices[0], vertices[4], vertices[7], vertices[3]), precision),
            Facet.fromVertexLoop(Arrays.asList(vertices[5], vertices[1], vertices[2], vertices[6]), precision),

            // -y and +y sides
            Facet.fromVertexLoop(Arrays.asList(vertices[0], vertices[1], vertices[5], vertices[4]), precision),
            Facet.fromVertexLoop(Arrays.asList(vertices[3], vertices[7], vertices[6], vertices[2]), precision)
        );
    }
}
