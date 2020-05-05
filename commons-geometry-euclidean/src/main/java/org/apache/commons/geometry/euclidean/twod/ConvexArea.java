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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.AbstractConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.path.InteriorAngleLinePathConnector;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;

/** Class representing a finite or infinite convex area in Euclidean 2D space.
 * The boundaries of this area, if any, are composed of convex line subsets.
 */
public class ConvexArea extends AbstractConvexHyperplaneBoundedRegion<Vector2D, LineConvexSubset>
    implements BoundarySource2D {

    /** Instance representing the full 2D plane. */
    private static final ConvexArea FULL = new ConvexArea(Collections.emptyList());

    /** Simple constructor. Callers are responsible for ensuring that the given path
     * represents the boundary of a convex area. No validation is performed.
     * @param boundaries the boundaries of the convex area
     */
    protected ConvexArea(final List<LineConvexSubset> boundaries) {
        super(boundaries);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<LineConvexSubset> boundaryStream() {
        return getBoundaries().stream();
    }

    /** Get the connected line subset paths comprising the boundary of the area. The
     * line subsets are oriented so that their minus sides point toward the interior of the
     * region. The size of the returned list is
     * <ul>
     *      <li><strong>0</strong> if the convex area is full,</li>
     *      <li><strong>1</strong> if at least one boundary is present and
     *          a single path can connect all line subsets (this will be the case
     *          for most instances), and</li>
     *      <li><strong>2</strong> if only two boundaries exist and they are
     *          parallel to each other (in which case they cannot be connected
     *          as a single path).</li>
     * </ul>
     * @return the line subset paths comprising the boundary of the area.
     */
    public List<LinePath> getBoundaryPaths() {
        return InteriorAngleLinePathConnector.connectMinimized(getBoundaries());
    }

    /** Get the vertices defining the area. The vertices lie at the intersections of the
     * area bounding lines. Note that it is possible for areas to contain no vertices at
     * all. For example, an area with no boundaries (representing the full space), an area
     * with a single boundary, or an area one with two parallel boundaries will not contain
     * vertices.
     * @return the vertices defining the area
     */
    public List<Vector2D> getVertices() {
        final List<LinePath> paths = getBoundaryPaths();

        // we will only have vertices if we have a single path; otherwise, we have a full
        // area or two non-intersecting infinite line subsets
        if (paths.size() == 1) {
            final LinePath path = paths.get(0);
            final List<Vector2D> vertices = path.getVertexSequence();

            if (path.isClosed()) {
                // do not include the repeated start point
                return vertices.subList(0, vertices.size() - 1);
            }
            return vertices;
        }

        return Collections.emptyList();
    }

    /** Return a new instance transformed by the argument.
     * @param transform transform to apply
     * @return a new instance transformed by the argument
     */
    public ConvexArea transform(final Transform<Vector2D> transform) {
        return transformInternal(transform, this, LineConvexSubset.class, ConvexArea::new);
    }

    /** {@inheritDoc} */
    @Override
    public LineConvexSubset trim(final HyperplaneConvexSubset<Vector2D> convexSubset) {
        return (LineConvexSubset) super.trim(convexSubset);
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        if (isFull()) {
            return Double.POSITIVE_INFINITY;
        }

        double quadrilateralAreaSum = 0.0;

        for (final LineConvexSubset boundary : getBoundaries()) {
            if (boundary.isInfinite()) {
                return Double.POSITIVE_INFINITY;
            }

            quadrilateralAreaSum += boundary.getStartPoint().signedArea(boundary.getEndPoint());
        }

        return 0.5 * quadrilateralAreaSum;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getBarycenter() {
        final List<LineConvexSubset> boundaries = getBoundaries();

        double quadrilateralAreaSum = 0.0;
        double scaledSumX = 0.0;
        double scaledSumY = 0.0;

        double signedArea;
        Vector2D startPoint;
        Vector2D endPoint;

        for (final LineConvexSubset seg : boundaries) {
            if (seg.isInfinite()) {
                // infinite => no barycenter
                return null;
            }

            startPoint = seg.getStartPoint();
            endPoint = seg.getEndPoint();

            signedArea = startPoint.signedArea(endPoint);

            quadrilateralAreaSum += signedArea;

            scaledSumX += signedArea * (startPoint.getX() + endPoint.getX());
            scaledSumY += signedArea * (startPoint.getY() + endPoint.getY());
        }

        if (quadrilateralAreaSum > 0) {
            return Vector2D.of(scaledSumX, scaledSumY).multiply(1.0 / (3.0 * quadrilateralAreaSum));
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Split<ConvexArea> split(final Hyperplane<Vector2D> splitter) {
        return splitInternal(splitter, this, LineConvexSubset.class, ConvexArea::new);
    }

    /** Return a BSP tree representing the same region as this instance.
     */
    @Override
    public RegionBSPTree2D toTree() {
        return RegionBSPTree2D.from(getBoundaries(), true);
    }

    /** Return an instance representing the full 2D area.
     * @return an instance representing the full 2D area.
     */
    public static ConvexArea full() {
        return FULL;
    }

    /** Construct a convex area by creating lines between adjacent vertices. The vertices must be given in a
     * counter-clockwise around order the interior of the shape. If the area is intended to be closed, the
     * beginning point must be repeated at the end of the path.
     * @param vertices vertices to use to construct the area
     * @param precision precision context used to create new line instances
     * @return a convex area constructed using lines between adjacent vertices
     * @see #fromVertexLoop(Collection, DoublePrecisionContext)
     * @see #fromVertices(Collection, boolean, DoublePrecisionContext)
     */
    public static ConvexArea fromVertices(final Collection<Vector2D> vertices,
            final DoublePrecisionContext precision) {
        return fromVertices(vertices, false, precision);
    }

    /** Construct a convex area by creating lines between adjacent vertices. An implicit line is created between the
     * last vertex given and the first one. The vertices must be given in a counter-clockwise around order the interior
     * of the shape.
     * @param vertices vertices to use to construct the area
     * @param precision precision context used to create new line instances
     * @return a convex area constructed using lines between adjacent vertices
     * @see #fromVertices(Collection, DoublePrecisionContext)
     * @see #fromVertices(Collection, boolean, DoublePrecisionContext)
     */
    public static ConvexArea fromVertexLoop(final Collection<Vector2D> vertices,
            final DoublePrecisionContext precision) {
        return fromVertices(vertices, true, precision);
    }

    /** Construct a convex area from lines between adjacent vertices.
     * @param vertices vertices to use to construct the area
     * @param close if true, an additional line will be created between the last and first vertex
     * @param precision precision context used to create new line instances
     * @return a convex area constructed using lines between adjacent vertices
     */
    public static ConvexArea fromVertices(final Collection<Vector2D> vertices, boolean close,
            final DoublePrecisionContext precision) {
        if (vertices.isEmpty()) {
            return full();
        }

        final List<Line> lines = new ArrayList<>();

        Vector2D first = null;
        Vector2D prev = null;
        Vector2D cur = null;

        for (final Vector2D vertex : vertices) {
            cur = vertex;

            if (first == null) {
                first = cur;
            }

            if (prev != null && !cur.eq(prev, precision)) {
                lines.add(Lines.fromPoints(prev, cur, precision));
            }

            prev = cur;
        }

        if (close && cur != null && !cur.eq(first, precision)) {
            lines.add(Lines.fromPoints(cur, first, precision));
        }

        if (!vertices.isEmpty() && lines.isEmpty()) {
            throw new IllegalStateException("Unable to create convex area: only a single unique vertex provided");
        }

        return fromBounds(lines);
    }

    /** Construct a convex area from a line subset path. The area represents the intersection of all of the
     * negative half-spaces of the lines in the path. The boundaries of the returned area may therefore not
     * match the line subsets in the path.
     * @param path path to construct the area from
     * @return a convex area constructed from the line subsets in the given path
     */
    public static ConvexArea fromPath(final LinePath path) {
        final List<Line> lines = path.boundaryStream()
                .map(LineConvexSubset::getLine)
                .collect(Collectors.toList());

        return fromBounds(lines);
    }

    /** Create a convex area formed by the intersection of the negative half-spaces of the
     * given bounding lines. The returned instance represents the area that is on the
     * minus side of all of the given lines. Note that this method does not support areas
     * of zero size (ie, infinitely thin areas or points.)
     * @param bounds lines used to define the convex area
     * @return a new convex area instance representing the area on the minus side of all
     *      of the bounding lines or an instance representing the full area if no lines are
     *      given
     * @throws IllegalArgumentException if the given set of bounding lines do not form a convex area,
     *      meaning that there is no region that is on the minus side of all of the bounding lines.
     */
    public static ConvexArea fromBounds(final Line... bounds) {
        return fromBounds(Arrays.asList(bounds));
    }

    /** Create a convex area formed by the intersection of the negative half-spaces of the
     * given bounding lines. The returned instance represents the area that is on the
     * minus side of all of the given lines. Note that this method does not support areas
     * of zero size (ie, infinitely thin areas or points.)
     * @param bounds lines used to define the convex area
     * @return a new convex area instance representing the area on the minus side of all
     *      of the bounding lines or an instance representing the full area if the collection
     *      is empty
     * @throws IllegalArgumentException if the given set of bounding lines do not form a convex area,
     *      meaning that there is no region that is on the minus side of all of the bounding lines.
     */
    public static ConvexArea fromBounds(final Iterable<Line> bounds) {
        final List<LineConvexSubset> subsets =
                new ConvexRegionBoundaryBuilder<>(LineConvexSubset.class).build(bounds);
        return subsets.isEmpty() ? full() : new ConvexArea(subsets);
    }
}
