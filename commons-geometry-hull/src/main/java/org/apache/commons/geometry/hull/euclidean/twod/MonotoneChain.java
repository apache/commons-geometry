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

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/**
 * Implements Andrew's monotone chain method to generate the convex hull of a finite set of
 * points in the two-dimensional Euclidean space.
 * <p>
 * The runtime complexity is O(n log n), with n being the number of input points. If the
 * point set is already sorted (by x-coordinate), the runtime complexity is O(n).
 * <p>
 * The implementation is not sensitive to collinear points on the hull. The parameter
 * {@code includeCollinearPoints} allows to control the behavior with regard to collinear points.
 * If {@code true}, all points on the boundary of the hull will be added to the hull vertices,
 * otherwise only the extreme points will be present. By default, collinear points are not added
 * as hull vertices.
 * <p>
 *
 * @see <a href="http://en.wikibooks.org/wiki/Algorithm_Implementation/Geometry/Convex_hull/Monotone_chain">
 * Andrew's monotone chain algorithm (Wikibooks)</a>
 */
public class MonotoneChain extends AbstractConvexHullGenerator2D {

    /** Create a new instance that only includes extreme points as hull vertices.
     * @param precision precision context used to compare floating point numbers
     */
    public MonotoneChain(final DoublePrecisionContext precision) {
        this(false, precision);
    }

    /** Create a new instance with the given parameters.
     * @param includeCollinearPoints whether collinear points shall be added as hull vertices
     * @param precision precision context used to compare floating point numbers
     */
    public MonotoneChain(final boolean includeCollinearPoints, final DoublePrecisionContext precision) {
        super(includeCollinearPoints, precision);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Vector2D> findHullVertices(final Collection<Vector2D> points) {

        final List<Vector2D> pointsSortedByXAxis = new ArrayList<>(points);

        // sort the points in increasing order on the x-axis
        Collections.sort(pointsSortedByXAxis, (o1, o2) -> {
            final DoublePrecisionContext precision = getPrecision();
            // need to take the tolerance value into account, otherwise collinear points
            // will not be handled correctly when building the upper/lower hull
            final int cmp = precision.compare(o1.getX(), o2.getX());
            if (cmp == 0) {
                return precision.compare(o1.getY(), o2.getY());
            } else {
                return cmp;
            }
        });

        // build lower hull
        final List<Vector2D> lowerHull = new ArrayList<>();
        for (final Vector2D p : pointsSortedByXAxis) {
            updateHull(p, lowerHull);
        }

        // build upper hull
        final List<Vector2D> upperHull = new ArrayList<>();
        for (int idx = pointsSortedByXAxis.size() - 1; idx >= 0; idx--) {
            final Vector2D p = pointsSortedByXAxis.get(idx);
            updateHull(p, upperHull);
        }

        // concatenate the lower and upper hulls
        // the last point of each list is omitted as it is repeated at the beginning of the other list
        final List<Vector2D> hullVertices = new ArrayList<>(lowerHull.size() + upperHull.size() - 2);
        for (int idx = 0; idx < lowerHull.size() - 1; idx++) {
            hullVertices.add(lowerHull.get(idx));
        }
        for (int idx = 0; idx < upperHull.size() - 1; idx++) {
            hullVertices.add(upperHull.get(idx));
        }

        // special case: if the lower and upper hull may contain only 1 point if all are identical
        if (hullVertices.isEmpty() && !lowerHull.isEmpty()) {
            hullVertices.add(lowerHull.get(0));
        }

        return hullVertices;
    }

    /**
     * Update the partial hull with the current point.
     *
     * @param point the current point
     * @param hull the partial hull
     */
    private void updateHull(final Vector2D point, final List<Vector2D> hull) {
        final DoublePrecisionContext precision = getPrecision();

        if (hull.size() == 1) {
            // ensure that we do not add an identical point
            final Vector2D p1 = hull.get(0);
            if (p1.eq(point, precision)) {
                return;
            }
        }

        while (hull.size() >= 2) {
            final int size = hull.size();
            final Vector2D p1 = hull.get(size - 2);
            final Vector2D p2 = hull.get(size - 1);

            final double offset = Line.fromPoints(p1, p2, precision).offset(point);
            if (precision.eqZero(offset)) {
                // the point is collinear to the line (p1, p2)

                final double distanceToCurrent = p1.distance(point);
                if (precision.eqZero(distanceToCurrent) || precision.eqZero(p2.distance(point))) {
                    // the point is assumed to be identical to either p1 or p2
                    return;
                }

                final double distanceToLast = p1.distance(p2);
                if (isIncludeCollinearPoints()) {
                    final int index = distanceToCurrent < distanceToLast ? size - 1 : size;
                    hull.add(index, point);
                } else {
                    if (distanceToCurrent > distanceToLast) {
                        hull.remove(size - 1);
                        hull.add(point);
                    }
                }
                return;
            } else if (offset > 0) {
                hull.remove(size - 1);
            } else {
                break;
            }
        }
        hull.add(point);
    }
}
