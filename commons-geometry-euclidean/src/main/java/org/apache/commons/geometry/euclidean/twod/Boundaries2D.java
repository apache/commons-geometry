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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Utility class for creating {@link BoundarySource2D} instances for generating common
 * shapes.
 */
public final class Boundaries2D {

    /** Private constructor. */
    private Boundaries2D() {
    }

    /** Create a {@link BoundarySource2D} defining an axis-aligned rectangular region. The points {@code a}
     * and {@code b} are taken to represent opposite corner points in the rectangle and may be specified in
     * any order.
     * @param a first corner point in the rectangle (opposite of {@code b})
     * @param b second corner point in the rectangle (opposite of {@code a})
     * @param precision precision context used to construct prism instances
     * @return a boundary source defining the boundaries of the rectangular region
     * @throws IllegalArgumentException if the width or height of the defined rectangle is zero
     *      as evaluated by the precision context.
     */
    public static BoundarySource2D rect(final Vector2D a, final Vector2D b,
            final DoublePrecisionContext precision) {

        final double minX = Math.min(a.getX(), b.getX());
        final double maxX = Math.max(a.getX(), b.getX());

        final double minY = Math.min(a.getY(), b.getY());
        final double maxY = Math.max(a.getY(), b.getY());

        if (precision.eq(minX, maxX) || precision.eq(minY, maxY)) {
            throw new IllegalArgumentException("Rectangle has zero size: " + a + ", " + b + ".");
        }

        final Vector2D lowerLeft = Vector2D.of(minX, minY);
        final Vector2D upperLeft = Vector2D.of(minX, maxY);

        final Vector2D upperRight = Vector2D.of(maxX, maxY);
        final Vector2D lowerRight = Vector2D.of(maxX, minY);

        List<Segment> segments = Arrays.asList(
                Segment.fromPoints(lowerLeft, lowerRight, precision),
                Segment.fromPoints(upperRight, upperLeft, precision),
                Segment.fromPoints(lowerRight, upperRight, precision),
                Segment.fromPoints(upperLeft, lowerLeft, precision)
            );

        return () -> segments.stream();
    }
}
