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
package org.apache.commons.geometry.euclidean.twod.hull;

import java.util.Collection;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/**
 * Abstract base class for convex hull generators in the two-dimensional Euclidean space.
 */
abstract class AbstractConvexHullGenerator2D implements ConvexHullGenerator2D {

    /** Default epsilon vlaue. */
    private static final double DEFAULT_EPSILON = 1e-10;

    /** Precision context used to compare floating point numbers. */
    private final DoublePrecisionContext precision;

    /**
     * Indicates if collinear points on the hull shall be present in the output.
     * If {@code false}, only the extreme points are added to the hull.
     */
    private final boolean includeCollinearPoints;

    /**
     * Simple constructor.
     * <p>
     * The default epsilon (1e-10) will be used to determine identical points.
     *
     * @param includeCollinearPoints indicates if collinear points on the hull shall be
     * added as hull vertices
     */
    protected AbstractConvexHullGenerator2D(final boolean includeCollinearPoints) {
        this(includeCollinearPoints, new EpsilonDoublePrecisionContext(DEFAULT_EPSILON));
    }

    /**
     * Simple constructor.
     *
     * @param includeCollinearPoints indicates if collinear points on the hull shall be
     * added as hull vertices
     * @param precision precision context used to compare floating point numbers
     */
    protected AbstractConvexHullGenerator2D(final boolean includeCollinearPoints, final DoublePrecisionContext precision) {
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
    public ConvexHull2D generate(final Collection<Vector2D> points)
            throws IllegalStateException {
        Collection<Vector2D> hullVertices = null;
        if (points.size() < 2) {
            hullVertices = points;
        } else {
            hullVertices = findHullVertices(points);
        }

        try {
            return new ConvexHull2D(hullVertices.toArray(new Vector2D[hullVertices.size()]),
                                    precision);
        } catch (IllegalArgumentException e) {
            // the hull vertices may not form a convex hull if the tolerance value is to large
            throw new IllegalStateException("Convex hull algorithm failed to generate solution", e);
        }
    }

    /**
     * Find the convex hull vertices from the set of input points.
     * @param points the set of input points
     * @return the convex hull vertices in CCW winding
     */
    protected abstract Collection<Vector2D> findHullVertices(Collection<Vector2D> points);

}
