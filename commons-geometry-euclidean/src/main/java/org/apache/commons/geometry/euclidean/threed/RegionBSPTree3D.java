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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.partitioning.bsp.BSPTreeVisitor;
import org.apache.commons.geometry.core.partitioning.bsp.RegionCutBoundary;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.Polyline;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Binary space partitioning (BSP) tree representing a region in three dimensional
 * Euclidean space.
 */
public final class RegionBSPTree3D extends AbstractRegionBSPTree<Vector3D, RegionBSPTree3D.RegionNode3D> {
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
        RegionBSPTree3D result = RegionBSPTree3D.empty();
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
            Split<ConvexVolume> split = nodeVolume.split(node.getCutHyperplane());

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

    /** Find the first intersection of the given ray/line segment with the boundary of
     * the region. The return value is the cut subhyperplane of the node containing the
     * intersection. Null is returned if no intersection exists.
     * @param ray ray to intersect with the region
     * @return the node cut subhyperplane containing the intersection or null if no
     *      intersection exists
     */
    public ConvexSubPlane raycastFirst(final Segment3D ray) {
        final RaycastIntersectionVisitor visitor = new RaycastIntersectionVisitor(ray);
        getRoot().accept(visitor);

        return visitor.getIntersectionCut();
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

        RegionSizePropertiesVisitor visitor = new RegionSizePropertiesVisitor();
        accept(visitor);

        return visitor.getRegionSizeProperties();
    }

    /** Compute the region represented by the given node.
     * @param node the node to compute the region for
     * @return the region represented by the given node
     */
    private ConvexVolume computeNodeRegion(final RegionNode3D node) {
        ConvexVolume volume = ConvexVolume.full();

        RegionNode3D child = node;
        RegionNode3D parent;

        while ((parent = child.getParent()) != null) {
            Split<ConvexVolume> split = volume.split(parent.getCutHyperplane());

            volume = child.isMinus() ? split.getMinus() : split.getPlus();

            child = parent;
        }

        return volume;
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

    /** Create a new BSP tree instance representing the same region as the argument.
     * @param volume convex volume instance
     * @return a new BSP tree instance representing the same region as the argument
     */
    public static RegionBSPTree3D from(final ConvexVolume volume) {
        RegionBSPTree3D tree = RegionBSPTree3D.full();
        tree.insert(volume.getBoundaries());

        return tree;
    }

    /** Create a new {@link RegionBSPTree3D.Builder} instance for creating BSP
     * trees from boundary representations.
     * @param precision precision context to use for floating point comparisons.
     * @return a new builder instance
     */
    public static Builder builder(final DoublePrecisionContext precision) {
        return new Builder(precision);
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
            return ((RegionBSPTree3D) getTree()).computeNodeRegion(this);
        }

        /** {@inheritDoc} */
        @Override
        protected RegionNode3D getSelf() {
            return this;
        }
    }

    /** Class used to construct {@link RegionBSPTree3D} instances from boundary representations.
     */
    public static final class Builder {

        /** Precision object used to perform floating point comparisons. This object is
         * used when constructing geometric types.
         */
        private final DoublePrecisionContext precision;

        /** The BSP tree being constructed. */
        private final RegionBSPTree3D tree = RegionBSPTree3D.empty();

        /** List of vertices to use for indexed facet operations. */
        private List<Vector3D> vertexList;

        /** Create a new builder instance. The given precision context will be used when
         * constructing geometric types.
         * @param precision precision object used to perform floating point comparisons
         */
        public Builder(final DoublePrecisionContext precision) {
            this.precision = precision;
        }

        /** Set the list of vertices to use for indexed facet operations.
         * @param vertices array of vertices
         * @return this builder instance
         * @see #addIndexedFacet(int...)
         * @see #addIndexedFacet(List)
         * @see #addIndexedFacets(int[][])
         */
        public Builder withVertexList(final Vector3D... vertices) {
            return withVertexList(Arrays.asList(vertices));
        }

        /** Set the list of vertices to use for indexed facet operations.
         * @param vertices list of vertices
         * @return this builder instance
         * @see #addIndexedFacet(int...)
         * @see #addIndexedFacet(List)
         * @see #addIndexedFacets(int[][])
         */
        public Builder withVertexList(final List<Vector3D> vertices) {
            this.vertexList = vertices;
            return this;
        }

        /** Add a subplane to the tree.
         * @param subplane subplane to add
         * @return this builder instance
         */
        public Builder add(final SubPlane subplane) {
            tree.insert(subplane);
            return this;
        }

        /** Add a convex subplane to the tree.
         * @param convex convex subplane to add
         * @return this builder instance
         */
        public Builder add(final ConvexSubPlane convex) {
            tree.insert(convex);
            return this;
        }

        /** Add a facet defined by the given array of vertices. The vertices
         * are considered to form a loop, even if the first vertex is not included
         * again at the end of the array.
         * @param vertices array of vertices defining the facet
         * @return this builder instance
         */
        public Builder addFacet(final Vector3D... vertices) {
            return addFacet(Arrays.asList(vertices));
        }

        /** Add a facet defined by the given list of vertices. The vertices
         * are considered to form a loop, even if the first vertex is not included
         * again at the end of the list.
         * @param vertices list of vertices defining the facet
         * @return this builder instance
         */
        public Builder addFacet(final List<Vector3D> vertices) {
            // if there are only 3 vertices, then we know for certain that the area is convex
            if (vertices.size() < 4) {
                return add(ConvexSubPlane.fromVertexLoop(vertices, precision));
            }

            final Plane plane = Plane.fromPoints(vertices, precision);
            final List<Vector2D> subspaceVertices = plane.toSubspace(vertices);

            final Polyline path = Polyline.fromVertexLoop(subspaceVertices, precision);
            return add(new SubPlane(plane, path.toTree()));
        }

        /** Add multiple facets, each one defined by an array of indices into the current
         * vertex list.
         * @param facets array of facet definitions, where each definition consists of an
         *      array of indices into the current vertex list
         * @return this builder instance
         * @see #withVertexList(List)
         */
        public Builder addIndexedFacets(int[][] facets) {
            for (int[] facet : facets) {
                addIndexedFacet(facet);
            }

            return this;
        }

        /** Add a facet defined by an array of indices into the current vertex list.
         * @param vertexIndices indices into the current vertex list, defining the vertices
         *      for the facet
         * @return this builder instance
         * @see #withVertexList(List)
         */
        public Builder addIndexedFacet(int... vertexIndices) {
            final Vector3D[] vertices = new Vector3D[vertexIndices.length];
            for (int i = 0; i < vertexIndices.length; ++i) {
                vertices[i] = vertexList.get(vertexIndices[i]);
            }

            return addFacet(vertices);
        }

        /** Add a facet defined by a list of indices into the current vertex list.
         * @param vertexIndices indices into the current vertex list, defining the vertices
         *      for the facet
         * @return this builder instance
         * @see #withVertexList(List)
         */
        public Builder addIndexedFacet(final List<Integer> vertexIndices) {
            final List<Vector3D> vertices = vertexIndices.stream()
                    .map(idx -> vertexList.get(idx)).collect(Collectors.toList());

            return addFacet(vertices);
        }

        /** Add an axis-oriented cube with the given dimensions to this instance.
        * @param center the cube center point
        * @param size the size of the cube
        * @return this builder instance
        * @throws IllegalArgumentException if the width, height, or depth of the defined region is zero
        *      as evaluated by the precision context.
        */
        public Builder addCenteredCube(final Vector3D center, final double size) {
            return addCenteredRect(center, size, size, size);
        }

        /** Add an axis-oriented cube with the given dimensions to this instance.
         * @param corner a corner of the cube
         * @param size the size of the cube
         * @return this builder instance
         * @throws IllegalArgumentException if the width, height, or depth of the defined region is zero
         *      as evaluated by the precision context.
         */
        public Builder addCube(final Vector3D corner, final double size) {
            return addRect(corner, size, size, size);
        }

        /** Add an axis-oriented rectangular prism to this instance. The prism is centered at the given point and
         * has the specified dimensions.
         * @param center center point for the rectangular prism
         * @param xSize size of the prism along the x-axis
         * @param ySize size of the prism along the y-axis
         * @param zSize size of the prism along the z-axis
         * @return this builder instance
         * @throws IllegalArgumentException if the width, height, or depth of the defined region is zero
         *      as evaluated by the precision context.
         */
        public Builder addCenteredRect(final Vector3D center, final double xSize, final double ySize,
                final double zSize) {

            return addRect(Vector3D.of(
                        center.getX() - (xSize * 0.5),
                        center.getY() - (ySize * 0.5),
                        center.getZ() - (zSize * 0.5)
                    ), xSize, ySize, zSize);
        }

        /** Add an axis-oriented rectangular prism to this instance. The prism
         * is constructed by taking {@code pt} as one corner of the region and adding {@code xDelta},
         * {@code yDelta}, and {@code zDelta} to its components to create the opposite corner.
         * @param pt point lying in a corner of the region
         * @param xDelta distance to move along the x axis to place the other points in the
         *      prism; this value may be negative.
         * @param yDelta distance to move along the y axis to place the other points in the
         *      prism; this value may be negative.
         * @param zDelta distance to move along the z axis to place the other points in the
         *      prism; this value may be negative.
         * @return this builder instance
         * @throws IllegalArgumentException if the width, height, or depth of the defined region is zero
         *      as evaluated by the precision context.
         */
        public Builder addRect(final Vector3D pt, final double xDelta, final double yDelta, final double zDelta) {
            return addRect(pt, Vector3D.of(
                    pt.getX() + xDelta,
                    pt.getY() + yDelta,
                    pt.getZ() + zDelta));
        }

        /** Add an axis-oriented rectangular prism to this instance. The points {@code a} and {@code b}
         * are taken to represent opposite corner points in the prism and may be specified in any order.
         * @param a first corner point in the rectangular prism (opposite of {@code b})
         * @param b second corner point in the rectangular prism (opposite of {@code a})
         * @return this builder instance
         * @throws IllegalArgumentException if the width, height, or depth of the defined region is zero
         *      as evaluated by the precision context.
         */
        public Builder addRect(final Vector3D a, final Vector3D b) {
            final double minX = Math.min(a.getX(), b.getX());
            final double maxX = Math.max(a.getX(), b.getX());

            final double minY = Math.min(a.getY(), b.getY());
            final double maxY = Math.max(a.getY(), b.getY());

            final double minZ = Math.min(a.getZ(), b.getZ());
            final double maxZ = Math.max(a.getZ(), b.getZ());

            if (precision.eq(minX, maxX) || precision.eq(minY, maxY) || precision.eq(minZ, maxZ)) {
                throw new IllegalArgumentException("Rectangular prism has zero size: " + a + ", " + b + ".");
            }

            final Vector3D[] vertices = {
                Vector3D.of(minX, minY, minZ),
                Vector3D.of(maxX, minY, minZ),
                Vector3D.of(maxX, maxY, minZ),
                Vector3D.of(minX, maxY, minZ),

                Vector3D.of(minX, minY, maxZ),
                Vector3D.of(maxX, minY, maxZ),
                Vector3D.of(maxX, maxY, maxZ),
                Vector3D.of(minX, maxY, maxZ)
            };

            addFacet(vertices[0], vertices[3], vertices[2], vertices[1]);
            addFacet(vertices[4], vertices[5], vertices[6], vertices[7]);

            addFacet(vertices[5], vertices[1], vertices[2], vertices[6]);
            addFacet(vertices[0], vertices[4], vertices[7], vertices[3]);

            addFacet(vertices[0], vertices[1], vertices[5], vertices[4]);
            addFacet(vertices[3], vertices[7], vertices[6], vertices[2]);

            return this;
        }

        /** Get the created BSP tree.
         * @return the created BSP tree
         */
        public RegionBSPTree3D build() {
            return tree;
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

        /** Accumulator for facet volume contributions. */
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
                RegionCutBoundary<Vector3D> boundary = node.getCutBoundary();
                addFacetContribution(boundary.getOutsideFacing(), false);
                addFacetContribution(boundary.getInsideFacing(), true);
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

                // Since the volume we used when adding together the facet contributions
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

        /** Add the facet contribution of the given node cut boundary. If {@code reverse} is true,
         * the volume of the facet contribution is reversed before being added to the total.
         * @param boundary node cut boundary
         * @param reverse if true, the facet contribution is reversed before being added to the total.
         */
        private void addFacetContribution(final SubHyperplane<Vector3D> boundary, boolean reverse) {
            SubPlane subplane = (SubPlane) boundary;
            RegionBSPTree2D base = subplane.getSubspaceRegion();

            double area = base.getSize();
            Vector2D baseBarycenter = base.getBarycenter();

            if (Double.isInfinite(area)) {
                volumeSum = Double.POSITIVE_INFINITY;
            } else if (baseBarycenter != null) {
                Plane plane = subplane.getPlane();
                Vector3D facetBarycenter = plane.toSpace(base.getBarycenter());

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

    /** BSP tree visitor that locates the node cut subhyperplane for the first intersection between a
     * given line segment and BSP tree region boundary.
     */
    private static final class RaycastIntersectionVisitor implements BSPTreeVisitor<Vector3D, RegionNode3D> {

        /** The line segment to intersect with the BSP tree. */
        private final Segment3D segment;

        /** The node cut subhyperplane containing the first boundary intersection. */
        private ConvexSubPlane intersectionCut;

        /** Create a new instance that locates the first boundary intersection between the given line segment and
         * the visited BSP tree.
         * @param segment segment to intersect with the BSP tree region boundary
         */
        RaycastIntersectionVisitor(final Segment3D segment) {
            this.segment = segment;
        }

        /** Get the node cut subhyperplane containing the first intersection between the configured line segment
         * and the BSP tree region boundary. This must be called after the tree nodes have been visited.
         * @return the node cut subhyperplane containing the first intersection between the configured line segment
         *      and the BSP tree region boundary or null if no such intersection was found
         */
        public ConvexSubPlane getIntersectionCut() {
            return intersectionCut;
        }

        /** {@inheritDoc} */
        @Override
        public Order visitOrder(final RegionNode3D internalNode) {
            final Plane cut = (Plane) internalNode.getCutHyperplane();
            final Line3D line = segment.getLine();

            final boolean plusIsNear = line.getDirection().dot(cut.getNormal()) < 0;

            return plusIsNear ?
                    Order.PLUS_NODE_MINUS :
                    Order.MINUS_NODE_PLUS;
        }

        /** {@inheritDoc} */
        @Override
        public Result visit(final RegionNode3D node) {
            if (node.isInternal()) {
                // check if the line segment intersects the cut subhyperplane
                final Line3D line = segment.getLine();
                final Vector3D intersection = ((Plane) node.getCutHyperplane()).intersection(line);

                if (intersection != null && segment.contains(intersection)) {

                    final RegionCutBoundary<Vector3D> boundary = node.getCutBoundary();

                    // check if the intersection point lies on the region boundary
                    if ((boundary.getInsideFacing() != null && boundary.getInsideFacing().contains(intersection)) ||
                            boundary.getOutsideFacing() != null && boundary.getOutsideFacing().contains(intersection)) {

                        intersectionCut = (ConvexSubPlane) node.getCut();

                        return Result.TERMINATE;
                    }
                }
            }

            return Result.CONTINUE;
        }
    }
}
