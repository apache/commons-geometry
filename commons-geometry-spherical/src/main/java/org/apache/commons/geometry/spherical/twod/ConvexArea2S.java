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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.exception.GeometryException;
import org.apache.commons.geometry.core.partitioning.AbstractConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Class representing a convex area in 2D spherical space. The boundaries of this
 * area, if any, are composed of convex great circle arcs.
 */
public final class ConvexArea2S extends AbstractConvexHyperplaneBoundedRegion<Point2S, GreatArc> {

    /** Serializable UID */
    private static final long serialVersionUID = 20191021L;

    /** Instance representing the full spherical area. */
    private static final ConvexArea2S FULL = new ConvexArea2S(Collections.emptyList());

    /** Constant containing the area of the full spherical space. */
    private static final double FULL_SIZE = 4 * Geometry.PI;

    /** Constant containing the area of half of the spherical space. */
    private static final double HALF_SIZE = Geometry.TWO_PI;

    /** Construct an instance from its boundaries. Callers are responsible for ensuring
     * that the given path represents the boundary of a convex area. No validation is
     * performed.
     * @param boundaries the boundaries of the convex area
     */
    private ConvexArea2S(final List<GreatArc> boundaries) {
        super(boundaries);
    }

    /** Get an array of interior angles for the area. An empty array is returned if there
     * are no boundary intersections (ie, it has only one boundary or no boundaries at all).
     *
     * <p>The order of the angles corresponds with the order of the boundaries returned
     * by {@link #getBoundaries()}: if {@code i} is an index into the boundaries list,
     * then {@code angles[i]} is the angle between boundaries {@code i} and {@code i+1}.</p>
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

            angles[i] = Geometry.PI - current.getCircle()
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
        }
        else if (numSides == 1) {
            return HALF_SIZE;
        }
        else {
            // use the extended version of Girard's theorem
            // https://en.wikipedia.org/wiki/Spherical_trigonometry#Girard's_theorem
            final double[] angles = getInteriorAngles();
            final double sum = Arrays.stream(angles).sum();

            return sum - ((angles.length - 2) * Geometry.PI);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Point2S getBarycenter() {
        List<GreatArc> arcs = getBoundaries();
        int numSides = arcs.size();

        if (numSides == 0) {
            // full space; no barycenter
            return null;
        }
        else if (numSides == 1) {
            // hemisphere; barycenter is the pole of the hemisphere
            return arcs.get(0).getCircle().getPolePoint();
        }
        else {
            // 2 or more sides; use an extension of the approach outlined here:
            // https://archive.org/details/centroidinertiat00broc
            // In short, the barycenter is the sum of the pole vectors of each side
            // multiplied by their arc lengths.
            Vector3D barycenter = Vector3D.ZERO;

            for (GreatArc arc : getBoundaries()) {
                barycenter = Vector3D.linearCombination(
                        1, barycenter,
                        arc.getSize(), arc.getCircle().getPole());
            }

            return Point2S.from(barycenter);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Split<ConvexArea2S> split(final Hyperplane<Point2S> splitter) {
        return splitInternal(splitter, this, GreatArc.class, ConvexArea2S::new);
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

    /** Return an instance representing the full spherical 2D space.
     * @return an instance representing the full spherical 2D space.
     */
    public static ConvexArea2S full() {
        return FULL;
    }

    /** Create a convex area formed by the intersection of the negative half-spaces of the
     * given bounding great circles. The returned instance represents the area that is on the
     * minus side of all of the given circles. Note that this method does not support areas
     * of zero size (ie, infinitely thin areas or points.)
     * @param bounds great circles used to define the convex area
     * @return a new convex area instance representing the area on the minus side of all
     *      of the bounding great circles or an instance representing the full area if no
     *      circles are given
     * @throws GeometryException if the given set of bounding great circles do not form a convex
     *      area, meaning that there is no region that is on the minus side of all of the bounding
     *      circles.
     */
    public static ConvexArea2S fromBounds(final GreatCircle ... bounds) {
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
     * @throws GeometryException if the given set of bounding great circles do not form a convex
     *      area, meaning that there is no region that is on the minus side of all of the bounding
     *      circles.
     */
    public static ConvexArea2S fromBounds(final Iterable<GreatCircle> bounds) {
        final List<GreatArc> arcs = new ConvexRegionBoundaryBuilder<>(GreatArc.class).build(bounds);
        return arcs.isEmpty() ?
                full() :
                new ConvexArea2S(arcs);
    }
}
