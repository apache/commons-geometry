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
package org.apache.commons.geometry.spherical.twod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.AbstractConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Class representing a convex area in 2D spherical space. The boundaries of this
 * area, if any, are composed of convex great circle arcs.
 */
public final class ConvexArea2S extends AbstractConvexHyperplaneBoundedRegion<Point2S, GreatArc>
    implements BoundarySource2S {
    /** Instance representing the full spherical area. */
    private static final ConvexArea2S FULL = new ConvexArea2S(Collections.emptyList());

    /** Constant containing the area of the full spherical space. */
    private static final double FULL_SIZE = 4 * PlaneAngleRadians.PI;

    /** Constant containing the area of half of the spherical space. */
    private static final double HALF_SIZE = PlaneAngleRadians.TWO_PI;

    /** Construct an instance from its boundaries. Callers are responsible for ensuring
     * that the given path represents the boundary of a convex area. No validation is
     * performed.
     * @param boundaries the boundaries of the convex area
     */
    private ConvexArea2S(final List<GreatArc> boundaries) {
        super(boundaries);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<GreatArc> boundaryStream() {
        return getBoundaries().stream();
    }

    /** Get a path instance representing the boundary of the area. The path is oriented
     * so that the minus sides of the arcs lie on the inside of the area.
     * @return the boundary path of the area
     */
    public GreatArcPath getBoundaryPath() {
        final List<GreatArcPath> paths = InteriorAngleGreatArcConnector.connectMinimized(getBoundaries());
        if (paths.isEmpty()) {
            return GreatArcPath.empty();
        }

        return paths.get(0);
    }

    /** Get an array of interior angles for the area. An empty array is returned if there
     * are no boundary intersections (ie, it has only one boundary or no boundaries at all).
     *
     * <p>The order of the angles corresponds with the order of the boundaries returned
     * by {@link #getBoundaries()}: if {@code i} is an index into the boundaries list,
     * then {@code angles[i]} is the angle between boundaries {@code i} and {@code (i+1) % boundariesSize}.</p>
     * @return an array of interior angles for the area
     */
    public double[] getInteriorAngles() {
        final List<GreatArc> arcs = getBoundaryPath().getArcs();
        final int numSides = arcs.size();

        if (numSides < 2) {
            return new double[0];
        }

        final double[] angles = new double[numSides];

        GreatArc current;
        GreatArc next;
        for (int i = 0; i < numSides; ++i) {
            current = arcs.get(i);
            next = arcs.get((i + 1) % numSides);

            angles[i] = PlaneAngleRadians.PI - current.getCircle()
                    .angle(next.getCircle(), current.getEndPoint());
        }

        return angles;
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        final int numSides = getBoundaries().size();

        if (numSides == 0) {
            return FULL_SIZE;
        } else if (numSides == 1) {
            return HALF_SIZE;
        } else {
            // use the extended version of Girard's theorem
            // https://en.wikipedia.org/wiki/Spherical_trigonometry#Girard's_theorem
            final double[] angles = getInteriorAngles();
            final double sum = Arrays.stream(angles).sum();

            return sum - ((angles.length - 2) * PlaneAngleRadians.PI);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Point2S getBarycenter() {
        final Vector3D weighted = getWeightedBarycenterVector();
        return weighted == null ? null : Point2S.from(weighted);
    }

    /** Returns the weighted vector for the barycenter. This vector is computed by scaling the
     * pole vector of the great circle of each boundary arc by the size of the arc and summing
     * the results. By combining the weighted barycenter vectors of multiple areas, a single
     * barycenter can be computed for the whole group.
     * @return weighted barycenter vector.
     * @see <a href="https://archive.org/details/centroidinertiat00broc">
     *  <em>The Centroid and Inertia Tensor for a Spherical Triangle</em> - John E. Brock</a>
     */
    Vector3D getWeightedBarycenterVector() {
        final List<GreatArc> arcs = getBoundaries();
        switch (arcs.size()) {
        case 0:
            // full space; no barycenter
            return null;
        case 1:
            // hemisphere; barycenter is the pole of the hemisphere
            final GreatArc singleArc = arcs.get(0);
            return singleArc.getCircle().getPole().withNorm(singleArc.getSize());
        default:
            // 2 or more sides; use an extension of the approach outlined here:
            // https://archive.org/details/centroidinertiat00broc
            // In short, the barycenter is the sum of the pole vectors of each side
            // multiplied by their arc lengths.
            Vector3D barycenter = Vector3D.ZERO;
            for (final GreatArc arc : getBoundaries()) {
                barycenter = barycenter.add(arc.getCircle().getPole().withNorm(arc.getSize()));
            }
            return barycenter;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Split<ConvexArea2S> split(final Hyperplane<Point2S> splitter) {
        return splitInternal(splitter, this, GreatArc.class, ConvexArea2S::new);
    }

    /** Return a new instance transformed by the argument.
     * @param transform transform to apply
     * @return a new instance transformed by the argument
     */
    public ConvexArea2S transform(final Transform<Point2S> transform) {
        return transformInternal(transform, this, GreatArc.class, ConvexArea2S::new);
    }

    /** {@inheritDoc} */
    @Override
    public GreatArc trim(final ConvexSubHyperplane<Point2S> convexSubHyperplane) {
        return (GreatArc) super.trim(convexSubHyperplane);
    }

    /** Return an instance representing the full spherical 2D space.
     * @return an instance representing the full spherical 2D space.
     */
    public static ConvexArea2S full() {
        return FULL;
    }

    /** Construct a convex area by creating great circles between adjacent vertices. The vertices must be given
     * in a counter-clockwise around order the interior of the shape. If the area is intended to be closed, the
     * beginning point must be repeated at the end of the path.
     * @param vertices vertices to use to construct the area
     * @param precision precision context used to create new great circle instances
     * @return a convex area constructed using great circles between adjacent vertices
     * @see #fromVertexLoop(Collection, DoublePrecisionContext)
     */
    public static ConvexArea2S fromVertices(final Collection<Point2S> vertices,
            final DoublePrecisionContext precision) {
        return fromVertices(vertices, false, precision);
    }

    /** Construct a convex area by creating great circles between adjacent vertices. An implicit great circle is
     * created between the last vertex given and the first one, if needed. The vertices must be given in a
     * counter-clockwise around order the interior of the shape.
     * @param vertices vertices to use to construct the area
     * @param precision precision context used to create new great circles instances
     * @return a convex area constructed using great circles between adjacent vertices
     * @see #fromVertices(Collection, DoublePrecisionContext)
     */
    public static ConvexArea2S fromVertexLoop(final Collection<Point2S> vertices,
            final DoublePrecisionContext precision) {
        return fromVertices(vertices, true, precision);
    }

    /** Construct a convex area from great circles between adjacent vertices.
     * @param vertices vertices to use to construct the area
     * @param close if true, an additional great circle will be created between the last and first vertex
     * @param precision precision context used to create new great circle instances
     * @return a convex area constructed using great circles between adjacent vertices
     */
    public static ConvexArea2S fromVertices(final Collection<Point2S> vertices, final boolean close,
            final DoublePrecisionContext precision) {

        if (vertices.isEmpty()) {
            return full();
        }

        final List<GreatCircle> circles = new ArrayList<>();

        Point2S first = null;
        Point2S prev = null;
        Point2S cur = null;

        for (final Point2S vertex : vertices) {
            cur = vertex;

            if (first == null) {
                first = cur;
            }

            if (prev != null && !cur.eq(prev, precision)) {
                circles.add(GreatCircle.fromPoints(prev, cur, precision));
            }

            prev = cur;
        }

        if (close && cur != null && !cur.eq(first, precision)) {
            circles.add(GreatCircle.fromPoints(cur, first, precision));
        }

        if (!vertices.isEmpty() && circles.isEmpty()) {
            throw new IllegalStateException("Unable to create convex area: only a single unique vertex provided");
        }

        return fromBounds(circles);
    }

    /** Construct a convex area from an arc path. The area represents the intersection of all of the negative
     * half-spaces of the great circles in the path. The boundaries of the returned area may therefore not match
     * the arcs in the path.
     * @param path path to construct the area from
     * @return a convex area constructed from the great circles in the given path
     */
    public static ConvexArea2S fromPath(final GreatArcPath path) {
        final List<GreatCircle> bounds = path.getArcs().stream()
            .map(GreatArc::getCircle)
            .collect(Collectors.toList());

        return fromBounds(bounds);
    }

    /** Create a convex area formed by the intersection of the negative half-spaces of the
     * given bounding great circles. The returned instance represents the area that is on the
     * minus side of all of the given circles. Note that this method does not support areas
     * of zero size (ie, infinitely thin areas or points.)
     * @param bounds great circles used to define the convex area
     * @return a new convex area instance representing the area on the minus side of all
     *      of the bounding great circles or an instance representing the full area if no
     *      circles are given
     * @throws IllegalArgumentException if the given set of bounding great circles do not form a convex area,
     *      meaning that there is no region that is on the minus side of all of the bounding circles.
     */
    public static ConvexArea2S fromBounds(final GreatCircle... bounds) {
        return fromBounds(Arrays.asList(bounds));
    }

    /** Create a convex area formed by the intersection of the negative half-spaces of the
     * given bounding great circles. The returned instance represents the area that is on the
     * minus side of all of the given circles. Note that this method does not support areas
     * of zero size (ie, infinitely thin areas or points.)
     * @param bounds great circles used to define the convex area
     * @return a new convex area instance representing the area on the minus side of all
     *      of the bounding great circles or an instance representing the full area if no
     *      circles are given
     * @throws IllegalArgumentException if the given set of bounding great circles do not form a convex area,
     *      meaning that there is no region that is on the minus side of all of the bounding circles.
     */
    public static ConvexArea2S fromBounds(final Iterable<GreatCircle> bounds) {
        final List<GreatArc> arcs = new ConvexRegionBoundaryBuilder<>(GreatArc.class).build(bounds);
        return arcs.isEmpty() ?
                full() :
                new ConvexArea2S(arcs);
    }
}
