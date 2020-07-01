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

import java.util.Iterator;
import java.util.stream.Stream;


/** Class used to construct {@link Bounds3D} instances representing the min and
 * max points present in a {@link BoundarySource3D}. The implementation examines
 * the vertices of each boundary in turn. Null is returned if any boundaries are
 * infinite or no vertices are present.
 */
final class BoundarySourceBoundsBuilder3D {

    /** Get a {@link Bounds3D} instance containing all vertices in the given boundary source.
     * Null is returned if any encountered boundaries were not finite or no vertices were found.
     * @param src the boundary source to compute the bounds of
     * @return the bounds of the argument or null if no valid bounds could be determined
     */
    public Bounds3D getBounds(final BoundarySource3D src) {
        final Bounds3D.Builder builder = Bounds3D.builder();

        try (Stream<PlaneConvexSubset> stream = src.boundaryStream()) {
            final Iterator<PlaneConvexSubset> it = stream.iterator();

            PlaneConvexSubset boundary;
            while (it.hasNext()) {
                boundary = it.next();

                if (!boundary.isFinite()) {
                    return null;
                }

                builder.addAll(boundary.getVertices());
            }
        }

        return builder.hasBounds() ?
                builder.build() :
                null;
    }
}
