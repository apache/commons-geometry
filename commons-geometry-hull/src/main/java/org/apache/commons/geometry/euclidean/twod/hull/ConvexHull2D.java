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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Segment;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.hull.ConvexHull;
import org.apache.commons.numbers.arrays.LinearCombination;

/**
 * This class represents a convex hull in an two-dimensional Euclidean space.
 */
public class ConvexHull2D implements ConvexHull<Vector2D> {
    /** Vertices of the hull. */
    private final Vector2D[] vertices;

    /** Precision context used to compare floating point numbers. */
    private final DoublePrecisionContext precision;

    /**
     * Line segments of the hull.
     * The array is not serialized and will be created from the vertices on first access.
     */
    private Segment[] lineSegments;

    /**
     * Simple constructor.
     * @param vertices the vertices of the convex hull, must be ordered
     * @param precision precision context used to compare floating point numbers
     * @throws IllegalArgumentException if the vertices do not form a convex hull
     */
    public ConvexHull2D(final Vector2D[] vertices, final DoublePrecisionContext precision) {
        this.precision = precision;

        if (!isConvex(vertices)) {
            throw new IllegalArgumentException("Vertices do not form a convex hull in CCW winding");
        }

        this.vertices = vertices.clone();
    }

    /**
     * Checks whether the given hull vertices form a convex hull.
     * @param hullVertices the hull vertices
     * @return {@code true} if the vertices form a convex hull, {@code false} otherwise
     */
    private boolean isConvex(final Vector2D[] hullVertices) {
        if (hullVertices.length < 3) {
            return true;
        }

        int sign = 0;
        for (int i = 0; i < hullVertices.length; i++) {
            final Vector2D p1 = hullVertices[i == 0 ? hullVertices.length - 1 : i - 1];
            final Vector2D p2 = hullVertices[i];
            final Vector2D p3 = hullVertices[i == hullVertices.length - 1 ? 0 : i + 1];

            final Vector2D d1 = p2.subtract(p1);
            final Vector2D d2 = p3.subtract(p2);

            final double crossProduct = LinearCombination.value(d1.getX(), d2.getY(), -d1.getY(), d2.getX());
            final int cmp = precision.compare(crossProduct, 0.0);
            // in case of collinear points the cross product will be zero
            if (cmp != 0.0) {
                if (sign != 0.0 && cmp != sign) {
                    return false;
                }
                sign = cmp;
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D[] getVertices() {
        return vertices.clone();
    }

    /**
     * Get the line segments of the convex hull, ordered.
     * @return the line segments of the convex hull
     */
    public Segment[] getLineSegments() {
        return retrieveLineSegments().clone();
    }

    /**
     * Retrieve the line segments from the cached array or create them if needed.
     *
     * @return the array of line segments
     */
    private Segment[] retrieveLineSegments() {
        if (lineSegments == null) {
            // construct the line segments - handle special cases of 1 or 2 points
            final int size = vertices.length;
            if (size <= 1) {
                this.lineSegments = new Segment[0];
            } else if (size == 2) {
                this.lineSegments = new Segment[1];
                final Vector2D p1 = vertices[0];
                final Vector2D p2 = vertices[1];
                this.lineSegments[0] = Segment.fromPoints(p1, p2, precision);
            } else {
                this.lineSegments = new Segment[size];
                Vector2D firstPoint = null;
                Vector2D lastPoint = null;
                int index = 0;
                for (Vector2D point : vertices) {
                    if (lastPoint == null) {
                        firstPoint = point;
                        lastPoint = point;
                    } else {
                        this.lineSegments[index++] = Segment.fromPoints(lastPoint, point, precision);
                        lastPoint = point;
                    }
                }
                this.lineSegments[index] = Segment.fromPoints(lastPoint, firstPoint, precision);
            }
        }
        return lineSegments;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexArea createRegion() {
        if (vertices.length < 3) {
            throw new IllegalStateException("Region generation requires at least 3 vertices but found only " +
                    vertices.length);
        }

        List<Line> bounds = Arrays.asList(retrieveLineSegments()).stream()
            .map(Segment::getLine).collect(Collectors.toList());

        return ConvexArea.fromBounds(bounds);
    }
}
