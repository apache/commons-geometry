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
package org.apache.commons.geometry.euclidean.twod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Class that performs linecast operations against arbitrary {@link BoundarySource2D}
 * instances. This class performs a brute-force computation of the intersections of the
 * line or subline against all boundaries. Some data structures may support more
 * efficient algorithms and should therefore prefer those instead.
 */
final class BoundarySourceLinecaster2D implements Linecastable2D {

    /** The boundary source instance providing boundaries for the linecast operation. */
    private final BoundarySource2D boundarySrc;

    /** Construct a new instance for linecasting against the given boundary source.
     * @param boundarySrc boundary source to linecast against.
     */
    BoundarySourceLinecaster2D(final BoundarySource2D boundarySrc) {
        this.boundarySrc = boundarySrc;
    }

    /** {@inheritDoc} */
    @Override
    public List<LinecastPoint2D> linecast(final ConvexSubLine subline) {
        final List<LinecastPoint2D> results = getIntersectionStream(subline)
                .collect(Collectors.toCollection(ArrayList::new));

        LinecastPoint2D.sortAndFilter(results);

        return results;
    }

    /** {@inheritDoc} */
    @Override
    public LinecastPoint2D linecastFirst(final ConvexSubLine subline) {
        return getIntersectionStream(subline)
                .min(LinecastPoint2D.ABSCISSA_ORDER)
                .orElse(null);
    }

    /** Return a stream containing intersections between the boundary source and the
     * given subline.
     * @param subline subline to intersect
     * @return a stream containing linecast intersections
     */
    private Stream<LinecastPoint2D> getIntersectionStream(final ConvexSubLine subline) {
        return boundarySrc.boundaryStream()
                .map(boundary -> computeIntersection(boundary, subline))
                .filter(Objects::nonNull);
    }

    /** Compute the intersection between a boundary subline and linecast intersecting subline. Null is
     * returned if no intersection is discovered.
     * @param boundary boundary from the boundary source
     * @param subline linecast subline to intersect with
     * @return the linecast intersection between the two arguments or null if there is no such
     *      intersection
     */
    private LinecastPoint2D computeIntersection(final ConvexSubLine boundary, final ConvexSubLine subline) {
        final Vector2D intersectionPt = boundary.intersection(subline);

        if (intersectionPt != null) {
            final Vector2D normal = boundary.getLine().getOffsetDirection();

            return new LinecastPoint2D(intersectionPt, normal, subline.getLine());
        }

        return null; // no intersection
    }
}
