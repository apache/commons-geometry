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
package org.apache.commons.geometry.euclidean.twod;

import java.util.Iterator;
import java.util.stream.Stream;

/** Class used to construct {@link Bounds2D} instances representing the min and
 * max points present in a {@link BoundarySource2D}. The implementation examines
 * the vertices of each boundary in turn. {@code null} is returned if any boundaries are
 * infinite or no vertices are present.
 */
final class BoundarySourceBoundsBuilder2D {

    /** Get a {@link Bounds2D} instance containing all vertices in the given boundary source.
     * {@code null} is returned if any encountered boundaries were not finite or no vertices were found.
     * @param src boundary source to compute the bounds of
     * @return the bounds of the argument or {@code null} if no valid bounds could be determined
     */
    public Bounds2D getBounds(final BoundarySource2D src) {

        final Bounds2D.Builder builder = Bounds2D.builder();

        try (Stream<LineConvexSubset> stream = src.boundaryStream()) {
            final Iterator<LineConvexSubset> it = stream.iterator();

            LineConvexSubset boundary;
            while (it.hasNext()) {
                boundary = it.next();

                if (boundary.isInfinite()) {
                    return null; // break out early
                }

                builder.add(boundary.getStartPoint());
                builder.add(boundary.getEndPoint());
            }
        }

        return builder.hasBounds() ?
                builder.build() :
                null;
    }
}
