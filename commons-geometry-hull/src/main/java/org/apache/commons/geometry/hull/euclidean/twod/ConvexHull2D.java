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
package org.apache.commons.geometry.hull.euclidean.twod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.geometry.hull.ConvexHull;
import org.apache.commons.numbers.core.Precision;

/**
 * This class represents a convex hull in two-dimensional Euclidean space.
 */
public final class ConvexHull2D implements ConvexHull<Vector2D> {

    /** Vertices for the convex hull, in order. */
    private final List<Vector2D> vertices;

    /** Polyline path for the convex hull. */
    private final LinePath path;

    /** Simple constructor; no validation is performed.
     * @param vertices the vertices of the convex hull; callers are responsible for ensuring that
     *      the given vertices are in order, unique, and define a convex hull.
     * @param precision precision context used to compare floating point numbers
     */
    ConvexHull2D(final Collection<Vector2D> vertices, final Precision.DoubleEquivalence precision) {
        this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));
        this.path = buildHullPath(vertices, precision);
    }

    /** {@inheritDoc} */
    @Override
    public List<Vector2D> getVertices() {
        return vertices;
    }

    /** Get a path defining the convex hull. The path will contain
     * <ul>
     *      <li>zero segments if the hull consists of only a single point,</li>
     *      <li>one segment if the hull consists of two points,</li>
     *      <li>three or more segments defining a closed loop if the hull consists of more than
     *          two non-collinear points.</li>
     * </ul>
     * @return polyline path defining the convex hull
     */
    public LinePath getPath() {
        return path;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexArea getRegion() {
        return path.isClosed() ?
                ConvexArea.convexPolygonFromPath(path) :
                null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[vertices= ")
            .append(getVertices())
            .append(']');

        return sb.toString();
    }

    /** Build a polyline representing the path for a convex hull.
     * @param vertices convex hull vertices
     * @param precision precision context used to compare floating point values
     * @return path for the convex hull defined by the given vertices
     */
    private static LinePath buildHullPath(final Collection<Vector2D> vertices,
            final Precision.DoubleEquivalence precision) {
        if (vertices.size() < 2) {
            return LinePath.empty();
        }

        final boolean closeLoop = vertices.size() > 2;

        return LinePath.builder(precision)
                .appendVertices(vertices)
                .build(closeLoop);
    }
}
