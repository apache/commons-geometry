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
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.partition.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partition.bsp.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.partition.bsp.BSPTreeVisitor;
import org.apache.commons.geometry.core.partition.bsp.RegionCutBoundary;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;

/** Binary space partitioning (BSP) tree representing a region in two dimensional
 * Euclidean space.
 */
public final class RegionBSPTree2D extends AbstractRegionBSPTree<Vector2D, RegionBSPTree2D.RegionNode2D> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190519L;

    /** List of line segments comprising the region boundary. */
    private List<LineSegment> boundarySegments;

    /** Create a new, empty region.
     */
    public RegionBSPTree2D() {
        this(false);
    }

    /** Create a new region. If {@code full} is true, then the region will
     * represent the entire 2D space. Otherwise, it will be empty.
     * @param full whether or not the region should contain the entire
     *      2D space or be empty
     */
    public RegionBSPTree2D(boolean full) {
        super(full);
    }

    /** Get the boundary of the region as a list of unconnected line segments. The
     * line segments are oriented such that their minus (left) side lies on the
     * interior of the region. The order of the returned segments depends on the
     * internal structure of the tree and should not be expected to be in any
     * particular sequence.
     * @return the boundary of the region as list of unconnected line segments
     */
    public List<LineSegment> getBoundarySegments() {
        if (boundarySegments == null) {
            boundarySegments = Collections.unmodifiableList(computeBoundarySegments());
        }
        return boundarySegments;
    }

    /** Get the boundary of the region as a list of connected line segment paths. The
     * line segments are oriented such that their minus (left) side lies on the
     * interior of the region. This method uses the
     * {@link InteriorAngleLineSegmentConnector#connectMinimized(java.util.Collection)}
     * method to connect the paths, meaning that when multiple connection options are
     * available for a given vertex, the option is chosen that minimizes the interior
     * angles of the path.
     * @return line segment paths representing the region boundary
     */
    public List<LineSegmentPath> getBoundaryPaths() {
        return InteriorAngleLineSegmentConnector.connectMinimized(getBoundarySegments());
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D project(Vector2D pt) {
        // use our custom projector so that we can disambiguate points that are
        // actually equidistant from the target point
        final BoundaryProjector2D projector = new BoundaryProjector2D(pt);
        accept(projector);

        return projector.getProjected();
    }

    /** Compute the line segments comprising the region boundary, ensuring that
     * the minus side of the line segments points to the region interior.
     * @return the line segments comprising the region boundary
     */
    protected List<LineSegment> computeBoundarySegments() {
        final LineSegmentBoundaryBuilder2D builder = new LineSegmentBoundaryBuilder2D();
        accept(builder);

        return builder.getSegments();
    }

    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<Vector2D> computeRegionSizeProperties() {
        // handle simple cases
        if (isEmpty()) {
            return new RegionSizeProperties<>(0, null);
        }
        else if (isFull()) {
            return new RegionSizeProperties<>(Double.POSITIVE_INFINITY, null);
        }

        // compute the size based on the boundary segments
        double quadrilateralAreaSum = 0.0;

        double simpleSumX = 0.0;
        double simpleSumY = 0.0;

        double scaledSumX = 0.0;
        double scaledSumY = 0.0;

        Vector2D startPoint;
        Vector2D endPoint;
        double signedArea;

        double sumX;
        double sumY;

        List<LineSegment> boundary = getBoundarySegments();

        for (LineSegment segment : boundary) {
            if (segment.isInfinite()) {
                // at least on boundary is infinite, meaning that
                // the size is also infinite
                quadrilateralAreaSum = Double.POSITIVE_INFINITY;

                break;
            }

            startPoint = segment.getStartPoint();
            endPoint = segment.getEndPoint();

            // compute the area
            signedArea = startPoint.signedArea(endPoint);

            quadrilateralAreaSum += signedArea;

            // compute raw and scaled coordinate values for the barycenter
            sumX = (startPoint.getX() + endPoint.getX());
            sumY = (startPoint.getY() + endPoint.getY());

            simpleSumX += sumX;
            simpleSumY += sumY;

            scaledSumX += signedArea * sumX;
            scaledSumY += signedArea * sumY;
        }

        if (quadrilateralAreaSum < 0.0) {
            // negative size, meaning that the shape is inside out, and
            // has an infinite "inside"
            return new RegionSizeProperties<>(Double.POSITIVE_INFINITY, null);
        }

        final double size = 0.5 * quadrilateralAreaSum;

        Vector2D barycenter = null;
        if (Double.isFinite(quadrilateralAreaSum)) {
            if (quadrilateralAreaSum > 0.0) {
                barycenter = Vector2D.of(scaledSumX, scaledSumY).multiply(1.0 / (3.0 * quadrilateralAreaSum));
            }
            else {
                // area is zero; use the simple centroid for the barycenter
                barycenter = Vector2D.of(simpleSumX, simpleSumY).multiply(1.0 / (2.0 * boundary.size()));
            }
        }

        return new RegionSizeProperties<>(size, barycenter);
    }

    /** {@inheritDoc} */
    @Override
    protected void invalidateRegionProperties() {
        super.invalidateRegionProperties();

        boundarySegments = null;
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode2D createNode() {
        return new RegionNode2D(this);
    }

    /** Return a new {@link RegionBSPTree2D} instance containing the entire space.
     * @return a new {@link RegionBSPTree2D} instance containing the entire space
     */
    public static RegionBSPTree2D full() {
        return new RegionBSPTree2D(true);
    }

    /** Return a new, empty {@link RegionBSPTree2D} instance.
     * @return a new, empty {@link RegionBSPTree2D} instance
     */
    public static RegionBSPTree2D empty() {
        return new RegionBSPTree2D(false);
    }

    /** Construct a bsp tree representing an axis-oriented rectangular region. The region
     * is constructed by taking {@code pt} as one corner of the region and adding {@code xDelta}
     * and {@code yDelta} to its components to create the opposite corner. If {@code xDelta}
     * and {@code yDelta} are both positive, then the constructed rectangle will have {@code pt}
     * as its lower-left corner and will have a width and height of {@code xDelta} and {@code yDelta}
     * respectively.
     *
     * <p>This method supports construction of infinitely thin or point-like regions.</p>
     *
     * @param pt point lying in a corner of the region
     * @param xDelta distance to move along the x axis to place the other points in the
     *      rectangle; this value may be negative, in which case {@code pt} will lie
     *      on the right side of the constructed rectangle
     * @param yDelta distance to move laong the y axis to place the other points in the
     *      rectangle; this value may be negative, in which case {@code pt} will lie
     *      on the top of the rectangle
     * @param precision precision context to use for floating point comparisons
     * @return a new bsp tree instance representing a rectangular region
     */
    public static RegionBSPTree2D rect(final Vector2D pt, final double xDelta, final double yDelta,
            final DoublePrecisionContext precision) {

        return rect(pt, Vector2D.of(pt.getX() + xDelta, pt.getY() + yDelta), precision);
    }

    /** Construct a bsp tree representing an axis-oriented rectangular region. The points {@code a} and {@code b}
     * are taken to represent opposite corner points in the rectangle and may be specified in any order.
     *
     * <p>This method supports construction of infinitely thin or point-like regions.</p>
     *
     * @param a first corner point in the rectangle (opposite of {@code b})
     * @param b second corner point in the rectangle (opposite of {@code a})
     * @param precision precision context to use for floating point comparisons
     * @return a new bsp tree instance representing a rectangular region
     */
    public static RegionBSPTree2D rect(final Vector2D a, final Vector2D b, final DoublePrecisionContext precision) {

        final double minX = Math.min(a.getX(), b.getX());
        final double maxX = Math.max(a.getX(), b.getX());

        final double minY = Math.min(a.getY(), b.getY());
        final double maxY = Math.max(a.getY(), b.getY());

        final Vector2D lowerLeft = Vector2D.of(minX, minY);
        final Vector2D upperLeft = Vector2D.of(minX, maxY);

        final Vector2D upperRight = Vector2D.of(maxX, maxY);
        final Vector2D lowerRight = Vector2D.of(maxX, minY);

        final RegionBSPTree2D tree = empty();
        RegionNode2D node = tree.getRoot();

        // construct the tree by directly setting the node cut subhyperplanes so that
        // we can represent areas with zero size
        tree.setNodeCut(node, Line.fromPointAndDirection(lowerLeft, Vector2D.PLUS_X, precision)
                .segment(lowerLeft, lowerRight));
        node = node.getMinus();

        tree.setNodeCut(node, Line.fromPointAndDirection(lowerRight, Vector2D.PLUS_Y, precision)
                .segment(lowerRight, upperRight));
        node = node.getMinus();

        tree.setNodeCut(node, Line.fromPointAndDirection(upperRight, Vector2D.MINUS_X, precision)
                .segment(upperRight, upperLeft));
        node = node.getMinus();

        tree.setNodeCut(node, Line.fromPointAndDirection(upperLeft, Vector2D.MINUS_Y, precision)
                .segment(upperLeft, lowerLeft));

        return tree;
    }

    /** BSP tree node for two dimensional Euclidean space.
     */
    public static final class RegionNode2D extends AbstractRegionBSPTree.AbstractRegionNode<Vector2D, RegionNode2D> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190519L;

        /** Simple constructor.
         * @param tree the owning tree instance
         */
        private RegionNode2D(AbstractBSPTree<Vector2D, RegionNode2D> tree) {
            super(tree);
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode2D getSelf() {
            return this;
        }
    }

    /** Visitor class for constructing a region boundary as a list of line segments. */
    private static final class LineSegmentBoundaryBuilder2D implements BSPTreeVisitor<Vector2D, RegionNode2D> {

        /** List containing the discovered boundary line segments. */
        private final List<LineSegment> segments = new ArrayList<>();

        /** {@inheritDoc} */
        @Override
        public void visit(RegionNode2D node) {
            if (node.isInternal()) {
                RegionCutBoundary<Vector2D> boundary = node.getCutBoundary();

                SubLine insideFacing = (SubLine) boundary.getInsideFacing();
                SubLine outsideFacing = (SubLine) boundary.getOutsideFacing();

                if (insideFacing != null && !insideFacing.isEmpty()) {
                    Line reversedLine = insideFacing.getLine().reverse();

                    for (Interval interval : insideFacing.getSubspaceRegion().toIntervals()) {
                        segments.add(LineSegment.fromInterval(reversedLine,
                                interval.transform(Vector1D::negate)));
                    }
                }

                if (outsideFacing != null && !outsideFacing.isEmpty()) {
                    segments.addAll(outsideFacing.toConvex());
                }
            }
        }

        /** Get the line segments for this instance.
         * @return line segment for this instance
         */
        public List<LineSegment> getSegments() {
            return segments;
        }
    }

    /** Class used to project points onto the region boundary.
     */
    private static final class BoundaryProjector2D extends BoundaryProjector<Vector2D, RegionNode2D> {

        /** Serializable UID */
        private static final long serialVersionUID = 1L;

        /** Simple constructor.
         * @param point the point to project onto the region's boundary
         */
        public BoundaryProjector2D(Vector2D point) {
            super(point);
        }

        /** {@inheritDoc} */
        @Override
        protected Vector2D disambiguateClosestPoint(final Vector2D target, final Vector2D a, final Vector2D b) {
            // return the point with the smallest coordinate values
            final int cmp = Vector2D.COORDINATE_ASCENDING_ORDER.compare(a, b);
            return cmp < 0 ? a : b;
        }
    }
}
