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
package org.apache.commons.geometry.euclidean.threed;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.threed.line.LinecastPoint3D;
import org.apache.commons.geometry.euclidean.threed.line.Linecastable3D;

/** Class that performs linecast operations against arbitrary {@link BoundarySource3D}
 * instances. This class performs a brute-force computation of the intersections of the
 * line or line convex subset against all boundaries. Some data structures may support more
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
    public List<LinecastPoint3D> linecast(final LineConvexSubset3D subset) {
        try (Stream<LinecastPoint3D> stream = getIntersectionStream(subset)) {

            final List<LinecastPoint3D> results = stream.collect(Collectors.toCollection(ArrayList::new));
            LinecastPoint3D.sortAndFilter(results);

            return results;
        }
    }

    /** {@inheritDoc} */
    @Override
    public LinecastPoint3D linecastFirst(final LineConvexSubset3D subset) {
        try (Stream<LinecastPoint3D> stream = getIntersectionStream(subset)) {
            return stream.min(LinecastPoint3D.ABSCISSA_ORDER)
                    .orElse(null);
        }
    }

    /** Return a stream containing intersections between the boundary source and the
     * given line convex subset.
     * @param subset line subset to intersect
     * @return a stream containing linecast intersections
     */
    private Stream<LinecastPoint3D> getIntersectionStream(final LineConvexSubset3D subset) {
        return boundarySrc.boundaryStream()
                .map(boundary -> computeIntersection(boundary, subset))
                .filter(Objects::nonNull);
    }

    /** Compute the intersection between a boundary plane subset and line subset. Null is
     * returned if no intersection is discovered.
     * @param planeSubset plane subset from the boundary source
     * @param lineSubset line subset to intersect with
     * @return the linecast intersection between the two arguments or {@code null} if there is no such
     *      intersection
     */
    private LinecastPoint3D computeIntersection(final PlaneConvexSubset planeSubset,
            final LineConvexSubset3D lineSubset) {
        final Vector3D intersectionPt = planeSubset.intersection(lineSubset);

        if (intersectionPt != null) {
            final Vector3D normal = planeSubset.getPlane().getNormal();

            return new LinecastPoint3D(intersectionPt, normal, lineSubset.getLine());
        }

        return null; // no intersection
    }
}
