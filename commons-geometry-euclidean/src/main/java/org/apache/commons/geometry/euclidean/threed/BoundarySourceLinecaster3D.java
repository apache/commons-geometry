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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Class that performs linecast operations against arbitrary {@link BoundarySource3D}
 * instances. This class performs a brute-force computation of the intersections of the
 * line or convex subline against all boundaries. Some data structures may support more
 * efficient algorithms and should therefore prefer those instead.
 */
final class BoundarySourceLinecaster3D implements Linecastable3D {

    /** The boundary source instance providing boundaries for the linecast operation. */
    private final BoundarySource3D boundarySrc;

    /** Construct a new instance for linecasting against the given boundary source.
     * @param boundarySrc boundary source to linecast against.
     */
    BoundarySourceLinecaster3D(final BoundarySource3D boundarySrc) {
        this.boundarySrc = boundarySrc;
    }

    /** {@inheritDoc} */
    @Override
    public List<LinecastPoint3D> linecast(final ConvexSubLine3D subline) {
        final List<LinecastPoint3D> results =  getIntersectionStream(subline)
                .collect(Collectors.toCollection(ArrayList::new));

        LinecastPoint3D.sortAndFilter(results);

        return results;
    }

    /** {@inheritDoc} */
    @Override
    public LinecastPoint3D linecastFirst(final ConvexSubLine3D subline) {
        return getIntersectionStream(subline)
                .min(LinecastPoint3D.ABSCISSA_ORDER)
                .orElse(null);
    }

    /** Return a stream containing intersections between the boundary source and the
     * given convex subline.
     * @param subline subline to intersect
     * @return a stream containing linecast intersections
     */
    private Stream<LinecastPoint3D> getIntersectionStream(final ConvexSubLine3D subline) {
        return boundarySrc.boundaryStream()
                .map(boundary -> computeIntersection(boundary, subline))
                .filter(Objects::nonNull);
    }

    /** Compute the intersection between a boundary subplane and subline. Null is
     * returned if no intersection is discovered.
     * @param subplane subplane from the boundary source
     * @param subline linecast subline to intersect with
     * @return the linecast intersection between the two arguments or null if there is no such
     *      intersection
     */
    private LinecastPoint3D computeIntersection(final ConvexSubPlane subplane, final ConvexSubLine3D subline) {
        final Vector3D intersectionPt = subplane.intersection(subline);

        if (intersectionPt != null) {
            final Vector3D normal = subplane.getPlane().getNormal();

            return new LinecastPoint3D(intersectionPt, normal, subline.getLine());
        }

        return null; // no intersection
    }
}
