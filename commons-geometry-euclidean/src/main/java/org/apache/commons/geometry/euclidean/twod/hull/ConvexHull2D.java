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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.geometry.core.ConvexHull;
import org.apache.commons.geometry.euclidean.EuclideanCollections;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
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

    /** Class used to build convex hulls. The builder is based on the Akl-Toussaint
     * heuristic to construct the hull. The heuristic is based on the idea of a
     * convex quadrilateral, which is formed by four points with the lowest and
     * highest x / y coordinates. Any point that lies inside this quadrilateral can
     * not be part of the convex hull and can thus be safely discarded before
     * generating the convex hull itself.
     * <p>
     * The complexity of the operation is O(n), and may greatly improve the time it
     * takes to construct the convex hull afterwards, depending on the point
     * distribution.
     *
     * @see <a href=
     *      "http://en.wikipedia.org/wiki/Convex_hull_algorithms#Akl-Toussaint_heuristic">
     *      Akl-Toussaint heuristic (Wikipedia)</a>
     */
    public static final class Builder {

        /** Corner of triangle with minimal x coordinate. */
        private Vector2D minX;

        /** Corner of triangle with maximal x coordinate. */
        private Vector2D maxX;

        /** Corner of triangle with minimal y coordinate. */
        private Vector2D minY;

        /** Corner of triangle with maximal y coordinate. */
        private Vector2D maxY;

        /** Collection of all remaining candidates for a convex hull. */
        private final Collection<Vector2D> candidates;

        /** A precision context for comparing points. */
        private final Precision.DoubleEquivalence precision;

        /** Indicates if collinear points on the hull shall be present in the output.
         * If {@code false}, only the extreme points are added to the hull.
         */
        private final boolean includeCollinearPoints;

        /**Return a {@link Builder} instance configured with the given precision
         * context. The precision context is used when comparing points.
         *
         * @param builderPrecision       precision context to use when building a convex
         *                               hull from raw vertices; may be null if raw
         *                               vertices are not used.
         * @param includeCollinearPoints whether collinear points shall be added as hull
         *                               vertices
         */
        public Builder(final boolean includeCollinearPoints, final Precision.DoubleEquivalence builderPrecision) {
            this.precision = builderPrecision;
            this.includeCollinearPoints = includeCollinearPoints;
            candidates = EuclideanCollections.pointSet2D(builderPrecision);
        }

        /** Appends the given point to a collection of possible hull points, if and only
         * if the given point is outside of a constructed quadrilateral of extreme properties.
         *
         * @param point a given point.
         * @return this instance.
         */
        public Builder append(Vector2D point) {

            //Checks if the given point supersedes one of the corners.
            checkCorners(point);

            //Only proceed if the quadrilateral is complete.
            if (candidates.size() < 4) {
                return this;
            }

            final List<Vector2D> quadrilateral = buildQuadrilateral(minY, maxX, maxY, minX);
            // if the quadrilateral is not well formed, e.g. only 2 points, do not attempt to reduce
            if (quadrilateral.size() < 3) {
                return this;
            }

            // check all points if they are within the quadrilateral
            // in which case they can not be part of the convex hull
            if (!insideQuadrilateral(point, quadrilateral)) {
                candidates.add(point);
            }

            return this;
        }

        /** Appends the given points to a collection of possible hull points, if and only
         * if the given points are outside of a constructed quadrilateral of extreme
         * properties.
         *
         * @param points a given collection of points.
         * @throws NullPointerException if points is {@code null}.
         * @return this instance.
         */
        public Builder append(Collection<Vector2D> points) {
            points.forEach(this::append);
            return this;
        }

        /**
         * Build a convex hull from the set appended points.
         *
         * @return the convex hull
         * @throws IllegalStateException if generator fails to generate a convex hull for
         *      the given set of input points
         */
        public ConvexHull2D build() {
            Collection<Vector2D> hullVertices;
            if (candidates.size() < 2) {
                hullVertices = candidates;
            } else {
                hullVertices = findHullVertices(candidates);
            }

            if (!isConvex(hullVertices)) {
                throw new IllegalStateException("Convex hull algorithm failed to generate solution");
            }

            return new ConvexHull2D(hullVertices, precision);
        }

        /** Build the convex quadrilateral with the found corner points (with min/max x/y
         * coordinates).
         *
         * @param points the respective points with min/max x/y coordinate
         * @return the quadrilateral
         */
        private static List<Vector2D> buildQuadrilateral(final Vector2D... points) {
            final List<Vector2D> quadrilateral = new ArrayList<>();
            for (final Vector2D p : points) {
                if (!quadrilateral.contains(p)) {
                    quadrilateral.add(p);
                }
            }
            return quadrilateral;
        }

        /** Checks if the given point supersedes one of the corners. If it does the old
         * corner is removed and the point added to the collection of points.
         *
         * @param point a given point.
         */
        private void checkCorners(Vector2D point) {
            if (minX == null || point.getX() < minX.getX()) {
                minX = point;
                candidates.add(point);
            }
            if (maxX == null || point.getX() > maxX.getX()) {
                maxX = point;
                candidates.add(point);
            }
            if (minY == null || point.getY() < minY.getY()) {
                minY = point;
                candidates.add(point);
            }
            if (maxY == null || point.getY() > maxY.getY()) {
                maxY = point;
                candidates.add(point);
            }
        }

        /** Checks if the given point is located within the convex quadrilateral.
         * @param point the point to check
         * @param quadrilateralPoints the convex quadrilateral, represented by 4 points
         * @return {@code true} if the point is inside the quadrilateral, {@code false} otherwise
         */
        private boolean insideQuadrilateral(final Vector2D point,
                                                   final List<? extends Vector2D> quadrilateralPoints) {

            Vector2D v1 = quadrilateralPoints.get(0);
            Vector2D v2 = quadrilateralPoints.get(1);

            if (point.equals(v1) || point.equals(v2)) {
                return true;
            }

            // get the location of the point relative to the first two vertices
            final double last = signedAreaPoints(v1, v2, point);

            // If the area is zero then this means the given point is on a boundary line.
            // and must be included as collinear point.
            if (precision.eq(last, 0.0) && includeCollinearPoints) {
                return false;
            }

            final int size = quadrilateralPoints.size();
            // loop through the rest of the vertices
            for (int i = 1; i < size; i++) {
                v1 = v2;
                v2 = quadrilateralPoints.get((i + 1) == size ? 0 : i + 1);

                if (point.equals(v2)) {
                    return true;
                }

                // do side of line test: multiply the last location with this location
                // if they are the same sign then the operation will yield a positive result
                // -x * -y = +xy, x * y = +xy, -x * y = -xy, x * -y = -xy
                if (last * signedAreaPoints(v1, v2, point) < 0) {
                    return false;
                }
            }
            return true;
        }

        /** Compute the signed area of the parallelogram formed by vectors between the given points. The first
         * vector points from {@code p0} to {@code p1} and the second from {@code p0} to {@code p3}.
         * @param p0 first point
         * @param p1 second point
         * @param p2 third point
         * @return signed area of parallelogram formed by vectors between the given points
         */
        private static double signedAreaPoints(final Vector2D p0, final Vector2D p1, final Vector2D p2) {
            return p0.vectorTo(p1).signedArea(p0.vectorTo(p2));
        }

        /**
         * Find the convex hull vertices from the set of input points.
         * @param points the set of input points
         * @return the convex hull vertices in CCW winding
         */
        private Collection<Vector2D> findHullVertices(final Collection<Vector2D> points) {

            final List<Vector2D> pointsSortedByXAxis = new ArrayList<>(points);

            // sort the points in increasing order on the x-axis
            pointsSortedByXAxis.sort((o1, o2) -> {
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

                final double offset = Lines.fromPoints(p1, p2, precision).offset(point);
                if (precision.eqZero(offset)) {
                    // the point is collinear to the line (p1, p2)

                    final double distanceToCurrent = p1.distance(point);
                    if (precision.eqZero(distanceToCurrent) || precision.eqZero(p2.distance(point))) {
                        // the point is assumed to be identical to either p1 or p2
                        return;
                    }

                    final double distanceToLast = p1.distance(p2);
                    if (includeCollinearPoints) {
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
}
