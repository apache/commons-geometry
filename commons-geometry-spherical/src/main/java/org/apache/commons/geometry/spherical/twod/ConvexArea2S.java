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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.AbstractConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;

/** Class representing a convex area in 2D spherical space. The boundaries of this
 * area, if any, are composed of convex great circle arcs.
 */
public final class ConvexArea2S extends AbstractConvexHyperplaneBoundedRegion<Point2S, GreatArc>
    implements BoundarySource2S {
    /** Instance representing the full spherical area. */
    private static final ConvexArea2S FULL = new ConvexArea2S(Collections.emptyList());

    /** Constant containing the area of the full spherical space. */
    private static final double FULL_SIZE = 4 * Math.PI;

    /** Constant containing the area of half of the spherical space. */
    private static final double HALF_SIZE = Angle.TWO_PI;

    /** Empirically determined threshold for computing the weighted centroid vector using the
     * triangle fan approach. Areas with boundary sizes under this value use the triangle fan
     * method to increase centroid accuracy.
     */
    private static final double TRIANGLE_FAN_CENTROID_COMPUTE_THRESHOLD = 1e-2;

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

            angles[i] = Math.PI - current.getCircle()
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

            return sum - ((angles.length - 2) * Math.PI);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Point2S getCentroid() {
        final Vector3D weighted = getWeightedCentroidVector();
        return weighted == null ? null : Point2S.from(weighted);
    }

    /** Return the weighted centroid vector of the area. The returned vector points in the direction of the
     * centroid point on the surface of the unit sphere with the length of the vector proportional to the
     * effective mass of the area at the centroid. By adding the weighted centroid vectors of multiple
     * convex areas, a single centroid can be computed for the combined area.
     * @return weighted centroid vector.
     * @see <a href="https://archive.org/details/centroidinertiat00broc">
     *  <em>The Centroid and Inertia Tensor for a Spherical Triangle</em> - John E. Brock</a>
     */
    Vector3D getWeightedCentroidVector() {
        final List<GreatArc> arcs = getBoundaries();
        final int numBoundaries = arcs.size();

        switch (numBoundaries) {
        case 0:
            // full space; no centroid
            return null;
        case 1:
            // hemisphere
            return computeHemisphereWeightedCentroidVector(arcs.get(0));
        case 2:
            // lune
            return computeLuneWeightedCentroidVector(arcs.get(0), arcs.get(1));
        default:
            // triangle or other convex polygon
            if (getBoundarySize() < TRIANGLE_FAN_CENTROID_COMPUTE_THRESHOLD) {
                return computeTriangleFanWeightedCentroidVector(arcs);
            }

            return computeArcPoleWeightedCentroidVector(arcs);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Split<ConvexArea2S> split(final Hyperplane<Point2S> splitter) {
        return splitInternal(splitter, this, GreatArc.class, ConvexArea2S::new);
    }

    /** Return a BSP tree representing the same region as this instance.
     */
    @Override
    public RegionBSPTree2S toTree() {
        return RegionBSPTree2S.from(getBoundaries(), true);
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
    public GreatArc trim(final HyperplaneConvexSubset<Point2S> sub) {
        return (GreatArc) super.trim(sub);
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
     * @see #fromVertexLoop(Collection, Precision.DoubleEquivalence)
     */
    public static ConvexArea2S fromVertices(final Collection<Point2S> vertices,
            final Precision.DoubleEquivalence precision) {
        return fromVertices(vertices, false, precision);
    }

    /** Construct a convex area by creating great circles between adjacent vertices. An implicit great circle is
     * created between the last vertex given and the first one, if needed. The vertices must be given in a
     * counter-clockwise around order the interior of the shape.
     * @param vertices vertices to use to construct the area
     * @param precision precision context used to create new great circles instances
     * @return a convex area constructed using great circles between adjacent vertices
     * @see #fromVertices(Collection, Precision.DoubleEquivalence)
     */
    public static ConvexArea2S fromVertexLoop(final Collection<Point2S> vertices,
            final Precision.DoubleEquivalence precision) {
        return fromVertices(vertices, true, precision);
    }

    /** Construct a convex area from great circles between adjacent vertices.
     * @param vertices vertices to use to construct the area
     * @param close if true, an additional great circle will be created between the last and first vertex
     * @param precision precision context used to create new great circle instances
     * @return a convex area constructed using great circles between adjacent vertices
     */
    public static ConvexArea2S fromVertices(final Collection<Point2S> vertices, final boolean close,
            final Precision.DoubleEquivalence precision) {

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
                circles.add(GreatCircles.fromPoints(prev, cur, precision));
            }

            prev = cur;
        }

        if (close && cur != null && !cur.eq(first, precision)) {
            circles.add(GreatCircles.fromPoints(cur, first, precision));
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

    /** Compute the weighted centroid vector for the hemisphere formed by the given arc.
     * @param arc arc defining the hemisphere
     * @return the weighted centroid vector for the hemisphere
     * @see #getWeightedCentroidVector()
     */
    private static Vector3D computeHemisphereWeightedCentroidVector(final GreatArc arc) {
        return arc.getCircle().getPole().withNorm(HALF_SIZE);
    }

    /** Compute the weighted centroid vector for the lune formed by the given arcs.
     * @param a first arc for the lune
     * @param b second arc for the lune
     * @return the weighted centroid vector for the lune
     * @see #getWeightedCentroidVector()
     */
    private static Vector3D computeLuneWeightedCentroidVector(final GreatArc a, final GreatArc b) {
        final Point2S aMid = a.getCentroid();
        final Point2S bMid = b.getCentroid();

        // compute the centroid vector as the exact center of the lune to avoid inaccurate
        // results with very small regions
        final Vector3D.Unit centroid = aMid.slerp(bMid, 0.5).getVector();

        // compute the weight using the reverse of the algorithm from computeArcPoleWeightedCentroidVector()
        final double weight =
            (a.getSize() * centroid.dot(a.getCircle().getPole())) +
            (b.getSize() * centroid.dot(b.getCircle().getPole()));

        return centroid.withNorm(weight);
    }

    /** Compute the weighted centroid vector for the triangle or polygon formed by the given arcs
     * by adding together the arc pole vectors multiplied by their respective arc lengths. This
     * algorithm is described in the paper <a href="https://archive.org/details/centroidinertiat00broc">
     * <em>The Centroid and Inertia Tensor for a Spherical Triangle</em></a> by John E Brock.
     *
     * <p>Note: This algorithm works well in general but is susceptible to floating point errors
     * on very small areas. In these cases, the computed centroid may not be in the expected location
     * and may even be outside of the area. The {@link #computeTriangleFanWeightedCentroidVector(List)}
     * method can produce more accurate results in these cases.</p>
     * @param arcs boundary arcs for the area
     * @return the weighted centroid vector for the area
     * @see #computeTriangleFanWeightedCentroidVector(List)
     */
    private static Vector3D computeArcPoleWeightedCentroidVector(final List<GreatArc> arcs) {
        final Vector3D.Sum centroid = Vector3D.Sum.create();

        for (final GreatArc arc : arcs) {
            centroid.addScaled(arc.getSize(), arc.getCircle().getPole());
        }

        return centroid.get();
    }

    /** Compute the weighted centroid vector for the triangle or polygon formed by the given arcs
     * using a triangle fan approach. This method is specifically designed for use with areas of very small size,
     * where use of the standard algorithm from {@link ##computeArcPoleWeightedCentroidVector(List))} can produce
     * inaccurate results. The algorithm proceeds as follows:
     * <ol>
     *  <li>The polygon is divided into spherical triangles using a triangle fan.</li>
     *  <li>For each triangle, the vectors of the 3 spherical points are added together to approximate the direction
     *      of the spherical centroid. This ensures that the computed centroid lies within the area.</li>
     *  <li>The length of the weighted centroid vector is determined by computing the sum of the contributions that
     *      each arc in the triangle would make to the centroid using the algorithm from
     *      {@link ##computeArcPoleWeightedCentroidVector(List)}. This essentially performs part of that algorithm in
     *      reverse: given a centroid direction, compute the contribution that each arc makes.</li>
     *  <li>The sum of the weighted centroid vectors for each triangle is computed and returned.</li>
     * </ol>
     * @param arcs boundary arcs for the area; must contain at least 3 arcs
     * @return the weighted centroid vector for the area
     * @see #computeArcPoleWeightedCentroidVector(List)
     */
    private static Vector3D computeTriangleFanWeightedCentroidVector(final List<GreatArc> arcs) {
        final Iterator<GreatArc> arcIt = arcs.iterator();

        final Point2S p0 = arcIt.next().getStartPoint();
        final Vector3D.Unit v0 = p0.getVector();

        final Vector3D.Sum areaCentroid = Vector3D.Sum.create();

        GreatArc arc;
        Point2S p1;
        Point2S p2;
        Vector3D.Unit v1;
        Vector3D.Unit v2;
        Vector3D.Unit triangleCentroid;
        double triangleCentroidLen;
        while (arcIt.hasNext()) {
            arc = arcIt.next();

            if (!arc.contains(p0)) {
                p1 = arc.getStartPoint();
                p2 = arc.getEndPoint();

                v1 = p1.getVector();
                v2 = p2.getVector();

                triangleCentroid = Vector3D.Sum.create()
                        .add(v0)
                        .add(v1)
                        .add(v2)
                        .get().normalize();
                triangleCentroidLen =
                        computeArcCentroidContribution(v0, v1, triangleCentroid) +
                        computeArcCentroidContribution(v1, v2, triangleCentroid) +
                        computeArcCentroidContribution(v2, v0, triangleCentroid);

                areaCentroid.addScaled(triangleCentroidLen, triangleCentroid);
            }
        }

        return areaCentroid.get();
    }

    /** Compute the contribution made by a single arc to a weighted centroid vector.
     * @param a first point in the arc
     * @param b second point in the arc
     * @param triangleCentroid the centroid vector for the area
     * @return the contribution made by the arc {@code ab} to the length of the weighted centroid vector
     */
    private static double computeArcCentroidContribution(final Vector3D.Unit a, final Vector3D.Unit b,
            final Vector3D.Unit triangleCentroid) {
        final double arcLength = a.angle(b);
        final Vector3D.Unit planeNormal = a.cross(b).normalize();

        return arcLength * triangleCentroid.dot(planeNormal);
    }
}
