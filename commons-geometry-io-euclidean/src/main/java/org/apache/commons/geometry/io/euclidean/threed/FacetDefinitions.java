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
package org.apache.commons.geometry.io.euclidean.threed;

import java.util.Collection;
import java.util.Objects;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
import org.apache.commons.geometry.euclidean.threed.Planes;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Class containing static methods that operate on {@link FacetDefinition} instances.
 */
public final class FacetDefinitions {

    /** Utility class; no instantiation. */
    private FacetDefinitions() {}

    /** Construct a {@link ConvexPolygon3D} from the given facet vertices and optional normal.
     * If the normal is non-null, this method attempts to honor it by making the
     * polygon point in a similar (but not necessarily equal) direction, reversing the
     * order of vertices if needed.
     * @param vertices facet vertices
     * @param normal facet normal; may be null
     * @param precision precision context used for floating point comparisons
     * @return convex polygon constructed from the vertices and normal
     * @throws IllegalArgumentException if a valid convex polygon cannot be constructed
     */
    public static ConvexPolygon3D toPolygon(final Collection<Vector3D> vertices, final Vector3D normal,
            final DoublePrecisionContext precision) {
        final ConvexPolygon3D polygon = Planes.convexPolygonFromVertices(vertices, precision);

        // ensure that the polygon normal matches whatever normal was defined, if any
        if (normal != null &&
                normal.dot(polygon.getPlane().getNormal()) < 0) {
            return polygon.reverse();
        }
        return polygon;
    }

    /** Construct a {@link ConvexPolygon3D} from the vertices of the given facet. This method
     * attempts to honor any normal defined for the facet by making the polygon point in a similar
     * (but not necessarily equal) direction by reversing the order of vertices if needed.
     * @param facet facet to convert to a polygon instance
     * @param precision precision context used for floating point comparisons
     * @return convex polygon constructed from the facet
     * @throws NullPointerException if either argument is null
     * @throws IllegalArgumentException if a valid convex polygon cannot be constructed
     */
    public static ConvexPolygon3D toPolygon(final FacetDefinition facet, final DoublePrecisionContext precision) {
        Objects.requireNonNull(facet, "Facet cannot be null");
        Objects.requireNonNull(precision, "Precision context cannot be null");
        return toPolygon(facet.getVertices(), facet.getNormal(), precision);
    }
}
