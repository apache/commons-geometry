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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.Transform;

/** Internal {@link ConvexPolygon3D} implementation class that uses a list of vertices
 * to represent the plane subset.
 */
final class VertexListConvexPolygon3D extends AbstractConvexPolygon3D {

    /** Vertex loop defining the convex polygon. */
    private final List<Vector3D> vertices;

    /** Construct a new instance with the given plane and list of vertices. Callers are responsible
     * for ensuring that the given vertices form a convex subset lying in {@code plane}. The list of
     * vertices should not contain the duplicated first endpoint. No validation is performed.
     * @param plane plane containing convex polygon
     * @param vertices vertices defining the convex polygon
     * @throws IllegalArgumentException if fewer than 3 vertices are given
     */
    VertexListConvexPolygon3D(final Plane plane, final List<Vector3D> vertices) {
        super(plane);

        // sanity check
        if (vertices.size() < 3) {
            throw new IllegalArgumentException("Convex polygon requires at least 3 points; found " + vertices.size());
        }

        this.vertices = Collections.unmodifiableList(vertices);
    }

    /** {@inheritDoc} */
    @Override
    public List<Vector3D> getVertices() {
        return vertices;
    }

    /** {@inheritDoc} */
    @Override
    public List<Triangle3D> toTriangles() {
        return Planes.convexPolygonToTriangleFan(getPlane(), vertices);
    }

    /** {@inheritDoc} */
    @Override
    public VertexListConvexPolygon3D transform(final Transform<Vector3D> transform) {
        final Plane tPlane = getPlane().transform(transform);
        final List<Vector3D> tVertices = vertices.stream()
                .map(transform)
                .collect(Collectors.toList());

        return new VertexListConvexPolygon3D(tPlane, tVertices);
    }

    /** {@inheritDoc} */
    @Override
    public VertexListConvexPolygon3D reverse() {
        final Plane rPlane = getPlane().reverse();
        final List<Vector3D> rVertices = new ArrayList<>(vertices);
        Collections.reverse(rVertices);

        return new VertexListConvexPolygon3D(rPlane, rVertices);
    }
}
