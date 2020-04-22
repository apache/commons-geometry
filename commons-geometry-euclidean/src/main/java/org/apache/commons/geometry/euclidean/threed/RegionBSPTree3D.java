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
package org.apache.commons.geometry.euclidean.threed;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.BSPTreeVisitor;
import org.apache.commons.geometry.core.partitioning.bsp.RegionCutBoundary;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Binary space partitioning (BSP) tree representing a region in three dimensional
 * Euclidean space.
 */
public final class RegionBSPTree3D extends AbstractRegionBSPTree<Vector3D, RegionBSPTree3D.RegionNode3D>
    implements BoundarySource3D, Linecastable3D {

    /** Create a new, empty region. */
    public RegionBSPTree3D() {
        this(false);
    }

    /** Create a new region. If {@code full} is true, then the region will
     * represent the entire 3D space. Otherwise, it will be empty.
     * @param full whether or not the region should contain the entire
     *      3D space or be empty
     */
    public RegionBSPTree3D(boolean full) {
        super(full);
    }

    /** Return a deep copy of this instance.
     * @return a deep copy of this instance.
     * @see #copy(org.apache.commons.geometry.core.partitioning.bsp.BSPTree)
     */
    public RegionBSPTree3D copy() {
        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.copy(this);

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<ConvexSubPlane> boundaries() {
        return createBoundaryIterable(b -> (ConvexSubPlane) b);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ConvexSubPlane> boundaryStream() {
        return StreamSupport.stream(boundaries().spliterator(), false);
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexSubPlane> getBoundaries() {
        return createBoundaryList(b -> (ConvexSubPlane) b);
    }

    /** Return a list of {@link ConvexVolume}s representing the same region
     * as this instance. One convex volume is returned for each interior leaf
     * node in the tree.
     * @return a list of convex volumes representing the same region as this
     *      instance
     */
    public List<ConvexVolume> toConvex() {
        final List<ConvexVolume> result = new ArrayList<>();

        toConvexRecursive(getRoot(), ConvexVolume.full(), result);

        return result;
    }

    /** Recursive method to compute the convex volumes of all inside leaf nodes in the subtree rooted at the given
     * node. The computed convex volumes are added to the given list.
     * @param node root of the subtree to compute the convex volumes for
     * @param nodeVolume the convex volume for the current node; this will be split by the node's cut hyperplane to
     *      form the convex volumes for any child nodes
     * @param result list containing the results of the computation
     */
    private void toConvexRecursive(final RegionNode3D node, final ConvexVolume nodeVolume,
            final List<ConvexVolume> result) {

        if (node.isLeaf()) {
            // base case; only add to the result list if the node is inside
            if (node.isInside()) {
                result.add(nodeVolume);
            }
        } else {
            // recurse
            final Split<ConvexVolume> split = nodeVolume.split(node.getCutHyperplane());

            toConvexRecursive(node.getMinus(), split.getMinus(), result);
            toConvexRecursive(node.getPlus(), split.getPlus(), result);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Split<RegionBSPTree3D> split(final Hyperplane<Vector3D> splitter) {
        return split(splitter, RegionBSPTree3D.empty(), RegionBSPTree3D.empty());
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D project(Vector3D pt) {
        // use our custom projector so that we can disambiguate points that are
        // actually equidistant from the target point
        final BoundaryProjector3D projector = new BoundaryProjector3D(pt);
        accept(projector);

        return projector.getProjected();
    }

    /** Return the current instance.
     */
    @Override
    public RegionBSPTree3D toTree() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public List<LinecastPoint3D> linecast(final ConvexSubLine3D subline) {
        final LinecastVisitor visitor = new LinecastVisitor(subline, false);
        accept(visitor);

        return visitor.getResults();
    }

    /** {@inheritDoc} */
    @Override
    public LinecastPoint3D linecastFirst(final ConvexSubLine3D subline) {
        final LinecastVisitor visitor = new LinecastVisitor(subline, true);
        accept(visitor);

        return visitor.getFirstResult();
    }

    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<Vector3D> computeRegionSizeProperties() {
        // handle simple cases
        if (isFull()) {
            return new RegionSizeProperties<>(Double.POSITIVE_INFINITY, null);
        } else if (isEmpty()) {
            return new RegionSizeProperties<>(0, null);
        }

        final RegionSizePropertiesVisitor visitor = new RegionSizePropertiesVisitor();
        accept(visitor);

        return visitor.getRegionSizeProperties();
    }

    /** {@inheritDoc} */
    @Override
    protected RegionNode3D createNode() {
        return new RegionNode3D(this);
    }

    /** Return a new instance containing all of 3D space.
     * @return a new instance containing all of 3D space.
     */
    public static RegionBSPTree3D full() {
        return new RegionBSPTree3D(true);
    }

    /** Return a new, empty instance. The represented region is completely empty.
     * @return a new, empty instance.
     */
    public static RegionBSPTree3D empty() {
        return new RegionBSPTree3D(false);
    }

    /** Construct a new tree from the given boundaries. If no boundaries
     * are present, the returned tree is empty.
     * @param boundaries boundaries to construct the tree from
     * @return a new tree instance constructed from the given boundaries
     * @see #from(Iterable, boolean)
     */
    public static RegionBSPTree3D from(final Iterable<ConvexSubPlane> boundaries) {
        return from(boundaries, false);
    }

    /** Construct a new tree from the given boundaries. If {@code full} is true, then
     * the initial tree before boundary insertion contains the entire space. Otherwise,
     * it is empty.
     * @param boundaries boundaries to construct the tree from
     * @param full if true, the initial tree will contain the entire space
     * @return a new tree instance constructed from the given boundaries
     */
    public static RegionBSPTree3D from(final Iterable<ConvexSubPlane> boundaries, final boolean full) {
        final RegionBSPTree3D tree = new RegionBSPTree3D(full);
        tree.insert(boundaries);

        return tree;
    }

    /** BSP tree node for three dimensional Euclidean space.
     */
    public static final class RegionNode3D extends AbstractRegionBSPTree.AbstractRegionNode<Vector3D, RegionNode3D> {
        /** Simple constructor.
         * @param tree the owning tree instance
         */
        protected RegionNode3D(AbstractBSPTree<Vector3D, RegionNode3D> tree) {
            super(tree);
        }

        /** Get the region represented by this node. The returned region contains
         * the entire area contained in this node, regardless of the attributes of
         * any child nodes.
         * @return the region represented by this node
         */
        public ConvexVolume getNodeRegion() {
            ConvexVolume volume = ConvexVolume.full();

            RegionNode3D child = this;
            RegionNode3D parent;

            while ((parent = child.getParent()) != null) {
                final Split<ConvexVolume> split = volume.split(parent.getCutHyperplane());

                volume = child.isMinus() ? split.getMinus() : split.getPlus();

                child = parent;
            }

            return volume;
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode3D getSelf() {
            return this;
        }
    }

    /** Class used to project points onto the 3D region boundary.
     */
    private static final class BoundaryProjector3D extends BoundaryProjector<Vector3D, RegionNode3D> {
        /** Simple constructor.
         * @param point the point to project onto the region's boundary
         */
        private BoundaryProjector3D(Vector3D point) {
            super(point);
        }

        /** {@inheritDoc} */
        @Override
        protected Vector3D disambiguateClosestPoint(final Vector3D target, final Vector3D a, final Vector3D b) {
            // return the point with the smallest coordinate values
            final int cmp = Vector3D.COORDINATE_ASCENDING_ORDER.compare(a, b);
            return cmp < 0 ? a : b;
        }
    }

    /** Visitor for computing geometric properties for 3D BSP tree instances.
     *  The volume of the region is computed using the equation
     *  <code>V = (1/3)*&Sigma;<sub>F</sub>[(C<sub>F</sub>&sdot;N<sub>F</sub>)*area(F)]</code>,
     *  where <code>F</code> represents each face in the region, <code>C<sub>F</sub></code>
     *  represents the barycenter of the face, and <code>N<sub>F</sub></code> represents the
     *  normal of the face. (More details can be found in the article
     *  <a href="https://en.wikipedia.org/wiki/Polyhedron#Volume">here</a>.)
     *  This essentially splits up the region into pyramids with a 2D face forming
     *  the base of each pyramid. The barycenter is computed in a similar way. The barycenter
     *  of each pyramid is calculated using the fact that it is located 3/4 of the way along the
     *  line from the apex to the base. The region barycenter then becomes the volume-weighted
     *  average of these pyramid centers.
     *  @see https://en.wikipedia.org/wiki/Polyhedron#Volume
     */
    private static final class RegionSizePropertiesVisitor implements BSPTreeVisitor<Vector3D, RegionNode3D> {

        /** Accumulator for boundary volume contributions. */
        private double volumeSum;

        /** Barycenter contribution x coordinate accumulator. */
        private double sumX;

        /** Barycenter contribution y coordinate accumulator. */
        private double sumY;

        /** Barycenter contribution z coordinate accumulator. */
        private double sumZ;

        /** {@inheritDoc} */
        @Override
        public Result visit(final RegionNode3D node) {
            if (node.isInternal()) {
                final RegionCutBoundary<Vector3D> boundary = node.getCutBoundary();
                addBoundaryContribution(boundary.getOutsideFacing(), false);
                addBoundaryContribution(boundary.getInsideFacing(), true);
            }

            return Result.CONTINUE;
        }

        /** Return the computed size properties for the visited region.
         * @return the computed size properties for the visited region.
         */
        public RegionSizeProperties<Vector3D> getRegionSizeProperties() {
            double size = Double.POSITIVE_INFINITY;
            Vector3D barycenter = null;

            // we only have a finite size if the volume sum is finite and positive
            // (negative indicates a finite outside surrounded by an infinite inside)
            if (Double.isFinite(volumeSum) && volumeSum > 0.0) {
                // apply the 1/3 pyramid volume scaling factor
                size = volumeSum / 3.0;

                // Since the volume we used when adding together the boundary contributions
                // was 3x the actual pyramid size, we'll multiply by 1/4 here instead
                // of 3/4 to adjust for the actual barycenter position in each pyramid.
                final double barycenterScale = 1.0 / (4 * size);
                barycenter =  Vector3D.of(
                        sumX * barycenterScale,
                        sumY * barycenterScale,
                        sumZ * barycenterScale);
            }

            return new RegionSizeProperties<>(size, barycenter);
        }

        /** Add the contribution of the given node cut boundary. If {@code reverse} is true,
         * the volume of the contribution is reversed before being added to the total.
         * @param boundary node cut boundary
         * @param reverse if true, the boundary contribution is reversed before being added to the total.
         */
        private void addBoundaryContribution(final SubHyperplane<Vector3D> boundary, boolean reverse) {
            final EmbeddedTreeSubPlane subplane = (EmbeddedTreeSubPlane) boundary;
            final RegionBSPTree2D base = subplane.getSubspaceRegion();

            final double area = base.getSize();
            final Vector2D baseBarycenter = base.getBarycenter();

            if (Double.isInfinite(area)) {
                volumeSum = Double.POSITIVE_INFINITY;
            } else if (baseBarycenter != null) {
                final Plane plane = subplane.getPlane();
                final Vector3D facetBarycenter = plane.toSpace(base.getBarycenter());

                // the volume here is actually 3x the actual pyramid volume; we'll apply
                // the final scaling all at once at the end
                double scaledVolume = area * facetBarycenter.dot(plane.getNormal());
                if (reverse) {
                    scaledVolume = -scaledVolume;
                }

                volumeSum += scaledVolume;

                sumX += scaledVolume * facetBarycenter.getX();
                sumY += scaledVolume * facetBarycenter.getY();
                sumZ += scaledVolume * facetBarycenter.getZ();
            }
        }
    }

    /** BSP tree visitor that performs a linecast operation against the boundaries of the visited tree.
     */
    private static final class LinecastVisitor implements BSPTreeVisitor<Vector3D, RegionNode3D> {

        /** The subline to intersect with the boundaries of the BSP tree. */
        private final ConvexSubLine3D linecastSubline;

        /** If true, the visitor will stop visiting the tree once the first linecast
         * point is determined.
         */
        private final boolean firstOnly;

        /** The minimum abscissa found during the search. */
        private double minAbscissa = Double.POSITIVE_INFINITY;

        /** List of results from the linecast operation. */
        private final List<LinecastPoint3D> results = new ArrayList<>();

        /** Create a new instance with the given intersecting convex subline.
         * @param linecastSubline subline to intersect with the BSP tree region boundary
         * @param firstOnly if true, the visitor will stop visiting the tree once the first
         *      linecast point is determined
         */
        LinecastVisitor(final ConvexSubLine3D linecastSubline, final boolean firstOnly) {
            this.linecastSubline = linecastSubline;
            this.firstOnly = firstOnly;
        }

        /** Get the first {@link LinecastPoint2D} resulting from the linecast operation.
         * @return the first linecast result point
         */
        public LinecastPoint3D getFirstResult() {
            final List<LinecastPoint3D> sortedResults = getResults();

            return sortedResults.isEmpty() ?
                    null :
                    sortedResults.get(0);
        }

        /** Get a list containing the results of the linecast operation. The list is
         * sorted and filtered.
         * @return list of sorted and filtered results from the linecast operation
         */
        public List<LinecastPoint3D> getResults() {
            LinecastPoint3D.sortAndFilter(results);

            return results;
        }

        /** {@inheritDoc} */
        @Override
        public Order visitOrder(final RegionNode3D internalNode) {
            final Plane cut = (Plane) internalNode.getCutHyperplane();
            final Line3D line = linecastSubline.getLine();

            final boolean plusIsNear = line.getDirection().dot(cut.getNormal()) < 0;

            return plusIsNear ?
                    Order.PLUS_NODE_MINUS :
                    Order.MINUS_NODE_PLUS;
        }

        /** {@inheritDoc} */
        @Override
        public Result visit(final RegionNode3D node) {
            if (node.isInternal()) {
                // check if the subline intersects the cut subhyperplane
                final Line3D line = linecastSubline.getLine();
                final Vector3D pt = ((Plane) node.getCutHyperplane()).intersection(line);

                if (pt != null) {
                    if (firstOnly && !results.isEmpty() &&
                            line.getPrecision().compare(minAbscissa, line.abscissa(pt)) < 0) {
                        // we have results and we are now sure that no other intersection points will be
                        // found that are closer or at the same position on the intersecting line.
                        return Result.TERMINATE;
                    } else if (linecastSubline.contains(pt)) {
                        // we've potentially found a new linecast point; add it to the list of potential
                        // results
                        final LinecastPoint3D potentialResult = computeLinecastPoint(pt, node);
                        if (potentialResult != null) {
                            results.add(potentialResult);

                            // update the min abscissa
                            minAbscissa = Math.min(minAbscissa, potentialResult.getAbscissa());
                        }
                    }
                }
            }

            return Result.CONTINUE;
        }

        /** Compute the linecast point for the given intersection point and tree node, returning null
         * if the point does not actually lie on the region boundary.
         * @param pt intersection point
         * @param node node containing the cut subhyperplane that the linecast line
         *      intersected with
         * @return a new linecast point instance or null if the intersection point does not lie
         *      on the region boundary
         */
        private LinecastPoint3D computeLinecastPoint(final Vector3D pt, final RegionNode3D node) {
            final Plane cut = (Plane) node.getCutHyperplane();
            final RegionCutBoundary<Vector3D> boundary = node.getCutBoundary();

            boolean onBoundary = false;
            boolean negateNormal = false;

            if (boundary.getInsideFacing() != null && boundary.getInsideFacing().contains(pt)) {
                // on inside-facing boundary
                onBoundary = true;
                negateNormal = true;
            } else  if (boundary.getOutsideFacing() != null && boundary.getOutsideFacing().contains(pt)) {
                // on outside-facing boundary
                onBoundary = true;
            }

            if (onBoundary) {
                Vector3D normal = cut.getNormal();
                if (negateNormal) {
                    normal = normal.negate();
                }

                return new LinecastPoint3D(pt, normal, linecastSubline.getLine());
            }

            return null;
        }
    }
}
