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
package org.apache.commons.geometry.euclidean.twod.shapes;

import java.util.Arrays;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.Polyline;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Class containing utility methods for constructing parallelograms. Parallelograms
 * are quadrilaterals with two pairs of parallel sides.
 * @see <a href="https://en.wikipedia.org/wiki/Parallelogram">Parallelogram</a>
 */
public final class Parallelogram {

    /** Utility class; no instantiation.
     */
    private Parallelogram() {
    }

    /** Return a {@link Polyline} defining an axis-aligned rectangle. The points {@code a}
     * and {@code b} are taken to represent opposite corner points in the rectangle and may be specified in
     * any order.
     * @param a first corner point in the rectangle (opposite of {@code b})
     * @param b second corner point in the rectangle (opposite of {@code a})
     * @param precision precision context used to construct segment instances
     * @return a polyline defining the axis-aligned rectangle
     * @throws IllegalArgumentException if the width or height of the defined rectangle is zero
     *      as evaluated by the precision context.
     */
    public static Polyline axisAligned(final Vector2D a, final Vector2D b,
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

        return Polyline.fromVertexLoop(Arrays.asList(
                    lowerLeft, lowerRight,
                    upperRight, upperLeft
                ), precision);
    }
}
