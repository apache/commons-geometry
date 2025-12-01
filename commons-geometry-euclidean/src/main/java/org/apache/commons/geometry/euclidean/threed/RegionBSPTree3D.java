/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractPartitionedRegionBuilder;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.BSPTreeVisitor;
import org.apache.commons.geometry.core.partitioning.bsp.RegionCutBoundary;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.threed.line.LinecastPoint3D;
import org.apache.commons.numbers.core.Precision;

/** Binary space partitioning (BSP) tree representing a region in three-dimensional
 * Euclidean space.
 */
public final class RegionBSPTree3D extends AbstractRegionBSPTree<Vector3D, RegionBSPTree3D.RegionNode3D>
    implements BoundarySource3D {

    /** Create a new, empty region. */
    public RegionBSPTree3D() {
        this(false);
    }

    /** Create a new region. If {@code full} is true, then the region will
     * represent the entire 3D space. Otherwise, it will be empty.
     * @param full whether or not the region should contain the entire
     *      3D space or be empty
     */
    public RegionBSPTree3D(final boolean full) {
        super(full);
    }

    /** Return a deep copy of this instance.
     * @return a deep copy of this instance.
     * @see #copy(org.apache.commons.geometry.core.partitioning.bsp.BSPTree)
     */
    public RegionBSPTree3D copy() {
        final RegionBSPTree3D result = empty();
        result.copy(this);

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<PlaneConvexSubset> boundaries() {
        return createBoundaryIterable(PlaneConvexSubset.class::cast);
    }

    /** {@inheritDoc} */
    @Override
    public Stream<PlaneConvexSubset> boundaryStream() {
        return StreamSupport.stream(boundaries().spliterator(), false);
    }

    /** {@inheritDoc} */
    @Override
    public List<PlaneConvexSubset> getBoundaries() {
        return createBoundaryList(PlaneConvexSubset.class::cast);
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
            final List<? super ConvexVolume> result) {

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
        return split(splitter, empty(), empty());
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D project(final Vector3D pt) {
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
    public List<LinecastPoint3D> linecast(final LineConvexSubset3D subset) {
        final LinecastVisitor visitor = new LinecastVisitor(subset, false);
        accept(visitor);

        return visitor.getResults();
    }

    /** {@inheritDoc} */
    @Override
    public LinecastPoint3D linecastFirst(final LineConvexSubset3D subset) {
        final LinecastVisitor visitor = new LinecastVisitor(subset, true);
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
    public static RegionBSPTree3D from(final Iterable<? extends PlaneConvexSubset> boundaries) {
        return from(boundaries, false);
    }

    /** Construct a new tree from the given boundaries. If {@code full} is true, then
     * the initial tree before boundary insertion contains the entire space. Otherwise,
     * it is empty.
     * @param boundaries boundaries to construct the tree from
     * @param full if true, the initial tree will contain the entire space
     * @return a new tree instance constructed from the given boundaries
     */
    public static RegionBSPTree3D from(final Iterable<? extends PlaneConvexSubset> boundaries, final boolean full) {
        final RegionBSPTree3D tree = new RegionBSPTree3D(full);
        tree.insert(boundaries);

        return tree;
    }

    /** Create a new {@link PartitionedRegionBuilder3D} instance which can be used to build balanced
     * BSP trees from region boundaries.
     * @return a new {@link PartitionedRegionBuilder3D} instance
     */
    public static PartitionedRegionBuilder3D partitionedRegionBuilder() {
        return new PartitionedRegionBuilder3D();
    }

    /** BSP tree node for three-dimensional Euclidean space.
     */
    public static final class RegionNode3D extends AbstractRegionBSPTree.AbstractRegionNode<Vector3D, RegionNode3D> {
        /** Simple constructor.
         * @param tree the owning tree instance
         */
        RegionNode3D(final AbstractBSPTree<Vector3D, RegionNode3D> tree) {
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

    /** Class used to build regions in Euclidean 3D space by inserting boundaries into a BSP
     * tree containing "partitions", i.e. structural cuts where both sides of the cut have the same region location.
     * When partitions are chosen that effectively divide the region boundaries at each partition level, the
     * constructed tree is shallower and more balanced than one constructed from the region boundaries alone,
     * resulting in improved performance. For example, consider a mesh approximation of a sphere. The region is
     * convex so each boundary has all of the other boundaries on its minus side; the plus sides are all empty.
     * When these boundaries are inserted directly into a tree, the tree degenerates into a simple linked list of
     * nodes with a height directly proportional to the number of boundaries. This means that many operations on the
     * tree, such as inside/outside testing of points, involve iterating through each and every region boundary. In
     * contrast, if a partition is first inserted that passes through the sphere center, the first BSP tree node
     * contains region nodes on its plus <em>and</em> minus sides, cutting the height of the tree in half. Operations
     * such as inside/outside testing are then able to skip half of the tree nodes with a single test on the
     * root node, resulting in drastically improved performance. Insertion of additional partitions (using a grid
     * layout, for example) can produce even shallower trees, although there is a point unique to each boundary set at
     * which the addition of more partitions begins to decrease instead of increase performance.
     *
     * <h2>Usage</h2>
     * <p>Usage of this class consists of two phases: (1) <em>partition insertion</em> and (2) <em>boundary
     * insertion</em>. Instances begin in the <em>partition insertion</em> phase. Here, partitions can be inserted
     * into the empty tree using {@link PartitionedRegionBuilder3D#insertPartition(PlaneConvexSubset) insertPartition}
     * or similar methods. The {@link org.apache.commons.geometry.core.partitioning.bsp.RegionCutRule#INHERIT INHERIT}
     * cut rule is used internally to insert the cut so the represented region remains empty even as partitions are
     * inserted.
     * </p>
     *
     * <p>The instance moves into the <em>boundary insertion</em> phase when the caller inserts the first region
     * boundary, using {@link PartitionedRegionBuilder3D#insertBoundary(PlaneConvexSubset) insertBoundary} or
     * similar methods. Attempting to insert a partition after this point results in an {@code IllegalStateException}.
     * This ensures that partitioning cuts are always located higher up the tree than boundary cuts.</p>
     *
     * <p>After all boundaries are inserted, the {@link PartitionedRegionBuilder3D#build() build} method is used
     * to perform final processing and return the computed tree.</p>
     */
    public static final class PartitionedRegionBuilder3D
        extends AbstractPartitionedRegionBuilder<Vector3D, RegionNode3D> {

        /** Construct a new builder instance.
         */
        private PartitionedRegionBuilder3D() {
            super(empty());
        }

        /** Insert a partition plane.
         * @param partition partition to insert
         * @return this instance
         * @throws IllegalStateException if a boundary has previously been inserted
         */
        public PartitionedRegionBuilder3D insertPartition(final Plane partition) {
            return insertPartition(partition.span());
        }

        /** Insert a plane convex subset as a partition.
         * @param partition partition to insert
         * @return this instance
         * @throws IllegalStateException if a boundary has previously been inserted
         */
        public PartitionedRegionBuilder3D insertPartition(final PlaneConvexSubset partition) {
            insertPartitionInternal(partition);

            return this;
        }

        /** Insert a set of three axis aligned planes intersecting at the given point as partitions.
         * The planes all contain the {@code center} point and have the normals {@code +x}, {@code +y},
         * and {@code +z} in that order. If inserted into an empty tree, this will partition the space
         * into 8 sections.
         * @param center center point for the partitions; all 3 inserted planes intersect at this point
         * @param precision precision context used to construct the planes
         * @return this instance
         * @throws IllegalStateException if a boundary has previously been inserted
         */
        public PartitionedRegionBuilder3D insertAxisAlignedPartitions(final Vector3D center,
                final Precision.DoubleEquivalence precision) {

            insertPartition(Planes.fromPointAndNormal(center, Vector3D.Unit.PLUS_X, precision));
            insertPartition(Planes.fromPointAndNormal(center, Vector3D.Unit.PLUS_Y, precision));
            insertPartition(Planes.fromPointAndNormal(center, Vector3D.Unit.PLUS_Z, precision));

            return this;
        }

        /** Insert a 3D grid of partitions. The partitions are constructed recursively: at each level a set of
         * three axis-aligned partitioning planes are inserted using
         * {@link #insertAxisAlignedPartitions(Vector3D, Precision.DoubleEquivalence) insertAxisAlignedPartitions}.
         * The algorithm then recurses using bounding boxes from the min point to the center and from the center
         * point to the max. Note that this means no partitions are ever inserted directly on the boundaries of
         * the given bounding box. This is intentional and done to allow this method to be called directly with the
         * bounding box from a set of boundaries to be inserted without unnecessarily adding partitions that will
         * never have region boundaries on both sides.
         * @param bounds bounding box for the grid
         * @param level recursion level for the grid; each level subdivides each grid cube into 8 sections, making the
         *      total number of grid cubes equal to {@code 8 ^ level}
         * @param precision precision context used to construct the partition planes
         * @return this instance
         * @throws IllegalStateException if a boundary has previously been inserted
         */
        public PartitionedRegionBuilder3D insertAxisAlignedGrid(final Bounds3D bounds, final int level,
                final Precision.DoubleEquivalence precision) {

            insertAxisAlignedGridRecursive(bounds.getMin(), bounds.getMax(), level, precision);

            return this;
        }

        /** Recursively insert axis-aligned grid partitions.
         * @param min min point for the grid cube to partition
         * @param max max point for the grid cube to partition
         * @param level current recursion level
         * @param precision precision context used to construct the partition planes
         */
        private void insertAxisAlignedGridRecursive(final Vector3D min, final Vector3D max, final int level,
                final Precision.DoubleEquivalence precision) {
            if (level > 0) {
                final Vector3D center = min.lerp(max, 0.5);

                insertAxisAlignedPartitions(center, precision);

                final int nextLevel = level - 1;
                insertAxisAlignedGridRecursive(min, center, nextLevel, precision);
                insertAxisAlignedGridRecursive(center, max, nextLevel, precision);
            }
        }

        /** Insert a region boundary.
         * @param boundary region boundary to insert
         * @return this instance
         */
        public PartitionedRegionBuilder3D insertBoundary(final PlaneConvexSubset boundary) {
            insertBoundaryInternal(boundary);

            return this;
        }

        /** Insert a collection of region boundaries.
         * @param boundaries boundaries to insert
         * @return this instance
         */
        public PartitionedRegionBuilder3D insertBoundaries(final Iterable<? extends PlaneConvexSubset> boundaries) {
            for (final PlaneConvexSubset boundary : boundaries) {
                insertBoundaryInternal(boundary);
            }

            return this;
        }

        /** Insert all boundaries from the given source.
         * @param boundarySrc source of boundaries to insert
         * @return this instance
         */
        public PartitionedRegionBuilder3D insertBoundaries(final BoundarySource3D boundarySrc) {
            try (Stream<PlaneConvexSubset> stream = boundarySrc.boundaryStream()) {
                stream.forEach(this::insertBoundaryInternal);
            }

            return this;
        }

        /** Build and return the region BSP tree.
         * @return the region BSP tree
         */
        public RegionBSPTree3D build() {
            return (RegionBSPTree3D) buildInternal();
        }
    }

    /** Class used to project points onto the 3D region boundary.
     */
    private static final class BoundaryProjector3D extends BoundaryProjector<Vector3D, RegionNode3D> {
        /** Simple constructor.
         * @param point the point to project onto the region's boundary
         */
        BoundaryProjector3D(final Vector3D point) {
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
     *  represents the centroid of the face, and <code>N<sub>F</sub></code> represents the
     *  normal of the face. (More details can be found in the article
     *  <a href="https://en.wikipedia.org/wiki/Polyhedron#Volume">here</a>.)
     *  This essentially splits up the region into pyramids with a 2D face forming
     *  the base of each pyramid. The centroid is computed in a similar way. The centroid
     *  of each pyramid is calculated using the fact that it is located 3/4 of the way along the
     *  line from the apex to the base. The region centroid then becomes the volume-weighted
     *  average of these pyramid centers.
     *  @see <a href="https://en.wikipedia.org/wiki/Polyhedron#Volume">Polyhedron#Volume</a>
     */
    private static final class RegionSizePropertiesVisitor implements BSPTreeVisitor<Vector3D, RegionNode3D> {

        /** Accumulator for boundary volume contributions. */
        private double volumeSum;

        /** Centroid contribution x coordinate accumulator. */
        private double sumX;

        /** Centroid contribution y coordinate accumulator. */
        private double sumY;

        /** Centroid contribution z coordinate accumulator. */
        private double sumZ;

        /** {@inheritDoc} */
        @Override
        public Result visit(final RegionNode3D node) {
            if (node.isInternal()) {
                final RegionCutBoundary<Vector3D> boundary = node.getCutBoundary();

                for (final HyperplaneConvexSubset<Vector3D> outsideFacing : boundary.getOutsideFacing()) {
                    addBoundaryContribution(outsideFacing, false);
                }

                for (final HyperplaneConvexSubset<Vector3D> insideFacing : boundary.getInsideFacing()) {
                    addBoundaryContribution(insideFacing, true);
                }
            }

            return Result.CONTINUE;
        }

        /** Return the computed size properties for the visited region.
         * @return the computed size properties for the visited region.
         */
        public RegionSizeProperties<Vector3D> getRegionSizeProperties() {
            double size = Double.POSITIVE_INFINITY;
            Vector3D centroid = null;

            // we only have a finite size if the volume sum is finite and positive
            // (negative indicates a finite outside surrounded by an infinite inside)
            if (Double.isFinite(volumeSum) && volumeSum > 0.0) {
                // apply the 1/3 pyramid volume scaling factor
                size = volumeSum / 3.0;

                // Since the volume we used when adding together the boundary contributions
                // was 3x the actual pyramid size, we'll multiply by 1/4 here instead
                // of 3/4 to adjust for the actual centroid position in each pyramid.
                final double centroidScale = 1.0 / (4 * size);
                centroid =  Vector3D.of(
                        sumX * centroidScale,
                        sumY * centroidScale,
                        sumZ * centroidScale);
            }

            return new RegionSizeProperties<>(size, centroid);
        }

        /** Add the contribution of the given node cut boundary. If {@code reverse} is true,
         * the volume of the contribution is reversed before being added to the total.
         * @param boundary node cut boundary
         * @param reverse if true, the boundary contribution is reversed before being added to the total.
         */
        private void addBoundaryContribution(final HyperplaneSubset<Vector3D> boundary, final boolean reverse) {
            final PlaneSubset boundarySubset = (PlaneSubset) boundary;

            final Plane boundaryPlane = boundarySubset.getPlane();
            final double boundaryArea = boundarySubset.getSize();
            final Vector3D boundaryCentroid = boundarySubset.getCentroid();

            if (Double.isInfinite(boundaryArea)) {
                volumeSum = Double.POSITIVE_INFINITY;
            } else if (boundaryCentroid != null) {
                // the volume here is actually 3x the actual pyramid volume; we'll apply
                // the final scaling all at once at the end
                double scaledVolume = boundaryArea * boundaryCentroid.dot(boundaryPlane.getNormal());
                if (reverse) {
                    scaledVolume = -scaledVolume;
                }

                volumeSum += scaledVolume;

                sumX += scaledVolume * boundaryCentroid.getX();
                sumY += scaledVolume * boundaryCentroid.getY();
                sumZ += scaledVolume * boundaryCentroid.getZ();
            }
        }
    }

    /** BSP tree visitor that performs a linecast operation against the boundaries of the visited tree.
     */
    private static final class LinecastVisitor implements BSPTreeVisitor<Vector3D, RegionNode3D> {

        /** The line subset to intersect with the boundaries of the BSP tree. */
        private final LineConvexSubset3D linecastSubset;

        /** If true, the visitor will stop visiting the tree once the first linecast
         * point is determined.
         */
        private final boolean firstOnly;

        /** The minimum abscissa found during the search. */
        private double minAbscissa = Double.POSITIVE_INFINITY;

        /** List of results from the linecast operation. */
        private final List<LinecastPoint3D> results = new ArrayList<>();

        /** Create a new instance with the given intersecting line convex subset.
         * @param linecastSubset line subset to intersect with the BSP tree region boundary
         * @param firstOnly if true, the visitor will stop visiting the tree once the first
         *      linecast point is determined
         */
        LinecastVisitor(final LineConvexSubset3D linecastSubset, final boolean firstOnly) {
            this.linecastSubset = linecastSubset;
            this.firstOnly = firstOnly;
        }

        /** Get the first {@link org.apache.commons.geometry.euclidean.twod.LinecastPoint2D}
         * resulting from the linecast operation.
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
            final Line3D line = linecastSubset.getLine();

            final boolean plusIsNear = line.getDirection().dot(cut.getNormal()) < 0;

            return plusIsNear ?
                    Order.PLUS_NODE_MINUS :
                    Order.MINUS_NODE_PLUS;
        }

        /** {@inheritDoc} */
        @Override
        public Result visit(final RegionNode3D node) {
            if (node.isInternal()) {
                // check if the line subset intersects the node cut hyperplane
                final Line3D line = linecastSubset.getLine();
                final Vector3D pt = ((Plane) node.getCutHyperplane()).intersection(line);

                if (pt != null) {
                    if (firstOnly && !results.isEmpty() &&
                            line.getPrecision().compare(minAbscissa, line.abscissa(pt)) < 0) {
                        // we have results, and we are now sure that no other intersection points will be
                        // found that are closer or at the same position on the intersecting line.
                        return Result.TERMINATE;
                    } else if (linecastSubset.contains(pt)) {
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

        /** Compute the linecast point for the given intersection point and tree node, returning {@code null}
         * if the point does not actually lie on the region boundary.
         * @param pt intersection point
         * @param node node containing the cut that the linecast line intersected with
         * @return a new linecast point instance or {@code null} if the intersection point does not lie
         *      on the region boundary
         */
        private LinecastPoint3D computeLinecastPoint(final Vector3D pt, final RegionNode3D node) {
            final Plane cut = (Plane) node.getCutHyperplane();
            final RegionCutBoundary<Vector3D> boundary = node.getCutBoundary();

            boolean onBoundary = false;
            boolean negateNormal = false;

            if (boundary.containsInsideFacing(pt)) {
                // on inside-facing boundary
                onBoundary = true;
                negateNormal = true;
            } else  if (boundary.containsOutsideFacing(pt)) {
                // on outside-facing boundary
                onBoundary = true;
            }

            if (onBoundary) {
                Vector3D normal = cut.getNormal();
                if (negateNormal) {
                    normal = normal.negate();
                }

                return new LinecastPoint3D(pt, normal, linecastSubset.getLine());
            }

            return null;
        }
    }
}
