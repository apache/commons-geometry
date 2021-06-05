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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.AbstractConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.euclidean.twod.path.InteriorAngleLinePathConnector;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.numbers.core.Precision;

/** Class representing a finite or infinite convex area in Euclidean 2D space.
 * The boundaries of this area, if any, are composed of convex line subsets.
 */
public class ConvexArea extends AbstractConvexHyperplaneBoundedRegion<Vector2D, LineConvexSubset>
    implements BoundarySource2D {

    /** Error message used when attempting to construct a convex polygon from a non-convex line path. */
    private static final String NON_CONVEX_PATH_ERROR = "Cannot construct convex polygon from non-convex path: ";

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
        // use connectMaximized() here since that will prevent us from skipping vertices
        // when there are multiple equivalent vertices to choose from for a given endpoint
        return InteriorAngleLinePathConnector.connectMaximized(getBoundaries());
    }

    /** Get the vertices for the area in a counter-clockwise order. Each vertex in the
     * returned list is unique. If the boundary of the area is closed, the start vertex is
     * <em>not</em> repeated at the end of the list.
     *
     * <p>It is important to note that, in general, the list of vertices returned by this method
     * is not sufficient to completely characterize the area. For example, a simple triangle
     * has 3 vertices, but an infinite area constructed from two parallel lines and two lines that
     * intersect between them will also have 3 vertices. It is also possible for non-empty areas to
     * contain no vertices at all. For example, an area with no boundaries (representing the full
     * space), an area with a single boundary, or an area with two parallel boundaries will not
     * contain any vertices.</p>
     * @return the list of vertices for the area in a counter-clockwise order
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
    public Vector2D getCentroid() {
        final List<LineConvexSubset> boundaries = getBoundaries();

        double quadrilateralAreaSum = 0.0;
        double scaledSumX = 0.0;
        double scaledSumY = 0.0;

        double signedArea;
        Vector2D startPoint;
        Vector2D endPoint;

        for (final LineConvexSubset seg : boundaries) {
            if (seg.isInfinite()) {
                // infinite => no centroid
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

    /** Construct a convex polygon from the given vertices.
     * @param vertices vertices to use to construct the polygon
     * @param precision precision context used for floating point comparisons
     * @return a convex polygon constructed using the given vertices
     * @throws IllegalStateException if {@code vertices} contains only a single unique vertex
     * @throws IllegalArgumentException if the constructed path does not define a closed, convex polygon
     * @see LinePath#fromVertexLoop(Collection, Precision.DoubleEquivalence)
     */
    public static ConvexArea convexPolygonFromVertices(final Collection<Vector2D> vertices,
            final Precision.DoubleEquivalence precision) {
        return convexPolygonFromPath(LinePath.fromVertexLoop(vertices, precision));
    }

    /** Construct a convex polygon from a line path.
     * @param path path to construct the polygon from
     * @return a convex polygon constructed from the given line path
     * @throws IllegalArgumentException if the path does not define a closed, convex polygon
     */
    public static ConvexArea convexPolygonFromPath(final LinePath path) {
        // ensure that the path is closed; this also ensures that we do not have any infinite elements
        if (!path.isClosed()) {
            throw new IllegalArgumentException("Cannot construct convex polygon from unclosed path: " + path);
        }

        final List<LineConvexSubset> elements = path.getElements();
        if (elements.size() < 3) {
            throw new IllegalArgumentException(
                    "Cannot construct convex polygon from path with less than 3 elements: " + path);
        }

        // go through the elements and validate that the produced area is convex and finite
        // using the precision context from the first path element
        final LineConvexSubset startElement = elements.get(0);
        final Vector2D startVertex = startElement.getStartPoint();
        final Precision.DoubleEquivalence precision = startElement.getPrecision();

        Vector2D curVector;
        Vector2D prevVector = null;

        double signedArea;
        double totalSignedArea = 0.0;

        LineConvexSubset element;

        // we can skip the last element since the we know that the path is closed, meaning that the
        // last element's end point is equal to our start point
        for (int i = 0; i < elements.size() - 1; ++i) {
            element = elements.get(i);

            curVector = startVertex.vectorTo(element.getEndPoint());

            if (prevVector != null) {
                signedArea = prevVector.signedArea(curVector);
                if (precision.lt(signedArea, 0.0)) {
                    throw new IllegalArgumentException(NON_CONVEX_PATH_ERROR + path);
                }

                totalSignedArea += signedArea;
            }

            prevVector = curVector;
        }

        if (precision.lte(totalSignedArea, 0.0)) {
            throw new IllegalArgumentException(NON_CONVEX_PATH_ERROR + path);
        }

        return new ConvexArea(elements);
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
