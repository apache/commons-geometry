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

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/**
 * Abstract base class for convex hull generators in the two-dimensional Euclidean space.
 */
abstract class AbstractConvexHullGenerator2D implements ConvexHullGenerator2D {

    /** Precision context used to compare floating point numbers. */
    private final DoublePrecisionContext precision;

    /**
     * Indicates if collinear points on the hull shall be present in the output.
     * If {@code false}, only the extreme points are added to the hull.
     */
    private final boolean includeCollinearPoints;

    /**
     * Simple constructor.
     *
     * @param includeCollinearPoints indicates if collinear points on the hull shall be
     * added as hull vertices
     * @param precision precision context used to compare floating point numbers
     */
    protected AbstractConvexHullGenerator2D(final boolean includeCollinearPoints,
            final DoublePrecisionContext precision) {
        this.includeCollinearPoints = includeCollinearPoints;
        this.precision = precision;
    }

    /** Get the object used to determine floating point equality for this region.
     * @return the floating point precision context for the instance
     */
    public DoublePrecisionContext getPrecision() {
        return precision;
    }

    /**
     * Returns if collinear points on the hull will be added as hull vertices.
     * @return {@code true} if collinear points are added as hull vertices, or {@code false}
     * if only extreme points are present.
     */
    public boolean isIncludeCollinearPoints() {
        return includeCollinearPoints;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexHull2D generate(final Collection<Vector2D> points) {
        final Collection<Vector2D> hullVertices;
        if (points.size() < 2) {
            hullVertices = points;
        } else {
            hullVertices = findHullVertices(points);
        }

        if (!isConvex(hullVertices)) {
            throw new IllegalStateException("Convex hull algorithm failed to generate solution");
        }

        return new ConvexHull2D(hullVertices, precision);
    }

    /**
     * Find the convex hull vertices from the set of input points.
     * @param points the set of input points
     * @return the convex hull vertices in CCW winding
     */
    protected abstract Collection<Vector2D> findHullVertices(Collection<Vector2D> points);

    /** Return true if the given vertices define a convex hull.
     * @param vertices the hull vertices
     * @return {@code true} if the vertices form a convex hull, {@code false} otherwise
     */
    private boolean isConvex(final Collection<Vector2D> vertices) {
        final int size = vertices.size();

        if (size < 3) {
            // 1 or 2 points always define a convex set
            return true;
        }

        final Iterator<Vector2D> it = vertices.iterator();

        Vector2D p1 = it.next();
        Vector2D p2 = it.next();
        Vector2D p3;

        Vector2D v1;
        Vector2D v2;

        while (it.hasNext()) {
            p3 = it.next();

            v1 = p1.vectorTo(p2);
            v2 = p2.vectorTo(p3);

            // negative signed areas mean a clockwise winding
            if (precision.compare(v1.signedArea(v2), 0.0) < 0) {
                return false;
            }

            p1 = p2;
            p2 = p3;
        }

        return true;
    }
}
