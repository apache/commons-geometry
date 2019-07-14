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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Split;
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

    /** List of line segment paths comprising the region boundary. */
    private List<SegmentPath> boundaryPaths;

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

    public void add(final ConvexArea area) {

    }

    /** Get the boundary of the region as a list of unconnected line segments. The
     * line segments are oriented such that their minus (left) side lies on the
     * interior of the region.
     * @return the boundary of the region as list of unconnected line segments
     */
    public List<Segment> getBoundarySegments() {
        List<Segment> segments = new ArrayList<>();
        for (SegmentPath path : getBoundaryPaths()) {
            segments.addAll(path.getSegments());
        }
        return segments;
    }

    /** Get the boundary of the region as a list of connected line segment paths. The
     * line segments are oriented such that their minus (left) side lies on the
     * interior of the region.
     * @return line segment paths representing the region boundary
     */
    public List<SegmentPath> getBoundaryPaths() {
        if (boundaryPaths == null) {
            boundaryPaths = Collections.unmodifiableList(computeBoundaryPaths());
        }
        return boundaryPaths;
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexArea> toConvex() {
        final List<ConvexArea> result = new ArrayList<>();

        toConvexRecursive(getRoot(), ConvexArea.full(), result);

        return result;
    }

    /** Recursive method to compute the convex areas of all inside leaf nodes in the subtree rooted at the given
     * node. The computed convex areas are added to the given list.
     * @param node root of the subtree to compute the convex areas for
     * @param nodeArea the convex area for the current node; this will be split by the node's cut hyperplane to
     *      form the convex areas for any child nodes
     * @param result list containing the results of the computation
     */
    private void toConvexRecursive(final RegionNode2D node, final ConvexArea nodeArea, final List<ConvexArea> result) {
        if (node.isLeaf()) {
            // base case; only add to the result list if the node is inside
            if (node.isInside()) {
                result.add(nodeArea);
            }
        }
        else {
            // recurse
            Split<ConvexArea> split = nodeArea.split(node.getCutHyperplane());

            toConvexRecursive(node.getMinus(), split.getMinus(), result);
            toConvexRecursive(node.getPlus(), split.getPlus(), result);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Split<RegionBSPTree2D> split(Hyperplane<Vector2D> splitter) {
        return split(splitter, RegionBSPTree2D.empty(), RegionBSPTree2D.empty());
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

    /** Compute the line segment paths comprising the region boundary, ensuring that
     * the minus side of the line segments points to the region interior.
     * @return the line segment paths comprising the region boundary
     */
    private List<SegmentPath> computeBoundaryPaths() {
        final BoundaryPathVisitor2D connector = new BoundaryPathVisitor2D();
        accept(connector);

        return connector.getBoundaryPaths();
    }

    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<Vector2D> computeRegionSizeProperties() {
        // handle simple cases
        if (isFull()) {
           return new RegionSizeProperties<>(Double.POSITIVE_INFINITY, null);
        }
        else if (isEmpty()) {
            return new RegionSizeProperties<>(0, null);
        }

        // compute the size based on the boundary segments
        double quadrilateralAreaSum = 0.0;

        double scaledSumX = 0.0;
        double scaledSumY = 0.0;

        Vector2D startPoint;
        Vector2D endPoint;
        double signedArea;

        final List<Segment> boundary = getBoundarySegments();

        for (Segment segment : boundary) {

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

            // compute scaled coordinate values for the barycenter
            scaledSumX += signedArea * (startPoint.getX() + endPoint.getX());
            scaledSumY += signedArea * (startPoint.getY() + endPoint.getY());
        }

        double size = Double.POSITIVE_INFINITY;
        Vector2D barycenter = null;

        // The area is finite only if the computed quadrilateral area is finite and non-negative.
        // Negative areas indicate that the region is inside-out, with a finite outside surrounded
        // by an infinite inside.
        if (quadrilateralAreaSum >= 0.0 && Double.isFinite(quadrilateralAreaSum)) {
            size = 0.5 * quadrilateralAreaSum;

            if (quadrilateralAreaSum > 0.0) {
                barycenter = Vector2D.of(scaledSumX, scaledSumY).multiply(1.0 / (3.0 * quadrilateralAreaSum));
            }
        }

        return new RegionSizeProperties<>(size, barycenter);
    }

    /** Compute the region represented by the given node.
     * @param node the node to compute the region for
     * @return the region represented by the given node
     */
    private ConvexArea computeNodeRegion(final RegionNode2D node) {
        ConvexArea area = ConvexArea.full();

        RegionNode2D child = node;
        RegionNode2D parent;

        while ((parent = child.getParent()) != null) {
            Split<ConvexArea> split = area.split(parent.getCutHyperplane());

            area = child.isMinus() ? split.getMinus() : split.getPlus();

            child = parent;
        }

        return area;
    }

    /** {@inheritDoc} */
    @Override
    protected void invalidate() {
        super.invalidate();

        boundaryPaths = null;
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

    /** Construct a tree from a convex area.
     * @param area the area to construct a tree from
     * @return tree instance representing the same area as the given
     *      convex area
     */
    public static RegionBSPTree2D fromConvexArea(final ConvexArea area) {
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(area.getBoundarySegments());

        return tree;
    }

    /** Construct a bsp tree representing an axis-oriented rectangular region. The region
     * is constructed by taking {@code pt} as one corner of the region and adding {@code xDelta}
     * and {@code yDelta} to its components to create the opposite corner. If {@code xDelta}
     * and {@code yDelta} are both positive, then the constructed rectangle will have {@code pt}
     * as its lower-left corner and will have a width and height of {@code xDelta} and {@code yDelta}
     * respectively.
     *
     * <p>This method does <em>not</em> support construction of infinitely thin or point-like regions.
     * The length and width of the created region must be non-zero as evaluated by the given precision
     * content.</p>
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
     * @throws GeometryValueException if the width or height of the defined rectangle is zero
     *      as evaluated by the given precision context.
     */
    public static RegionBSPTree2D rect(final Vector2D pt, final double xDelta, final double yDelta,
            final DoublePrecisionContext precision) {

        return rect(pt, Vector2D.of(pt.getX() + xDelta, pt.getY() + yDelta), precision);
    }

    /** Construct a bsp tree representing an axis-oriented rectangular region. The points {@code a} and {@code b}
     * are taken to represent opposite corner points in the rectangle and may be specified in any order.
     *
     * <p>This method does <em>not</em> support construction of infinitely thin or point-like regions.
     * The length and width of the created region must be non-zero as evaluated by the given precision
     * content.</p>
     *
     * @param a first corner point in the rectangle (opposite of {@code b})
     * @param b second corner point in the rectangle (opposite of {@code a})
     * @param precision precision context to use for floating point comparisons
     * @return a new bsp tree instance representing a rectangular region
     * @throws GeometryValueException if the width or height of the defined rectangle is zero
     *      as evaluated by the given precision context.
     */
    public static RegionBSPTree2D rect(final Vector2D a, final Vector2D b, final DoublePrecisionContext precision) {

        final double minX = Math.min(a.getX(), b.getX());
        final double maxX = Math.max(a.getX(), b.getX());

        final double minY = Math.min(a.getY(), b.getY());
        final double maxY = Math.max(a.getY(), b.getY());

        if (precision.eq(minX, maxX) || precision.eq(minY, maxY)) {
            throw new GeometryValueException("Rectangle has zero size: " + a + ", " + b + ".");
        }

        final Vector2D lowerLeft = Vector2D.of(minX, minY);
        final Vector2D upperLeft = Vector2D.of(minX, maxY);

        final Vector2D upperRight = Vector2D.of(maxX, maxY);
        final Vector2D lowerRight = Vector2D.of(maxX, minY);

        final Line bottomLine = Line.fromPointAndDirection(lowerLeft, Vector2D.PLUS_X, precision);
        final Line rightLine = Line.fromPointAndDirection(lowerRight, Vector2D.PLUS_Y, precision);
        final Line topLine = Line.fromPointAndDirection(upperRight, Vector2D.MINUS_X, precision);
        final Line leftLine = Line.fromPointAndDirection(upperLeft, Vector2D.MINUS_Y, precision);

        final RegionBSPTree2D tree = empty();
        RegionNode2D node = tree.getRoot();

        // construct the tree by directly setting the node cut subhyperplanes so that
        // we can represent areas with zero size
        tree.cutNode(node, bottomLine.span());
        node = node.getMinus();

        tree.cutNode(node, topLine.span());
        node = node.getMinus();

        tree.cutNode(node, rightLine.segment(minY, maxY));
        node = node.getMinus();

        tree.cutNode(node, leftLine.segment(-maxY, -minY));

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

        /** Get the region represented by this node. The returned region contains
         * the entire area contained in this node, regardless of the attributes of
         * any child nodes.
         * @return the region represented by this node
         */
        public ConvexArea getNodeRegion() {
            return ((RegionBSPTree2D) getTree()).computeNodeRegion(this);
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode2D getSelf() {
            return this;
        }
    }

    /** Class used to project points onto the 2D region boundary.
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

    /** Class used to compute the 2D boundary paths.
     */
    private static final class BoundaryPathVisitor2D implements BSPTreeVisitor<Vector2D, RegionNode2D>, Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 20190610L;

        /** List of line segments comprising the region boundary for the current node. */
        private final List<Segment> nodeSegments = new ArrayList<>();

        /** Connector instance used to connect the line segments from the nodes into connected paths. */
        private final InteriorAngleSegmentConnector connector = new InteriorAngleSegmentConnector.Minimize();

        /** {@inheritDoc} */
        @Override
        public Order visitOrder(final RegionNode2D internalNode) {
            // give each node a chance to connect its boundary segments to those of
            // its descendants
            return Order.MINUS_PLUS_NODE;
        }

        /** {@inheritDoc} */
        @Override
        public void visit(RegionNode2D node) {
            if (node.isInternal()) {
                nodeSegments.clear();

                RegionCutBoundary<Vector2D> boundary = node.getCutBoundary();

                SubLine insideFacing = (SubLine) boundary.getInsideFacing();
                SubLine outsideFacing = (SubLine) boundary.getOutsideFacing();

                if (insideFacing != null && !insideFacing.isEmpty()) {
                    // reverse inside-facing boundary segments to point toward the outside
                    Line reversedLine = insideFacing.getLine().reverse();

                    for (Interval interval : insideFacing.getSubspaceRegion().toIntervals()) {
                        nodeSegments.add(Segment.fromInterval(reversedLine,
                                interval.transform(Vector1D::negate)));
                    }
                }

                if (outsideFacing != null && !outsideFacing.isEmpty()) {
                    nodeSegments.addAll(outsideFacing.toConvex());
                }

                if (!nodeSegments.isEmpty()) {
                    connectNodeSegments();
                }
            }
        }

        /** Add the boundary segments for the current node to the connector, connecting
         * them with any existing segments.
         */
        private void connectNodeSegments() {
            if (!nodeSegments.isEmpty()) {
                connector.connect(nodeSegments);
            }
        }

        /** Get the computed boundary paths for the tree. The paths are simplified
         * to combine adjacent segments on the same line before being returned.
         * @return the boundary paths for the tree
         */
        public List<SegmentPath> getBoundaryPaths() {
            return connector.getPaths().stream()
                    .map(SegmentPath::simplify).collect(Collectors.toList());
        }
    }
}
