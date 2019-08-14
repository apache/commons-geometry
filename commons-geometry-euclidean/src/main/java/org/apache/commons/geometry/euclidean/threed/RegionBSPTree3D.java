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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.core.partition.bsp.AbstractBSPTree;
import org.apache.commons.geometry.core.partition.bsp.AbstractRegionBSPTree;
import org.apache.commons.geometry.core.partition.bsp.BSPTreeVisitor;
import org.apache.commons.geometry.core.partition.bsp.RegionCutBoundary;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.SegmentPath;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Binary space partitioning (BSP) tree representing a region in three dimensional
 * Euclidean space.
 */
public class RegionBSPTree3D extends AbstractRegionBSPTree<Vector3D, RegionBSPTree3D.RegionNode3D> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190702L;

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
     * @see {@link #copy(org.apache.commons.geometry.core.partition.bsp.BSPTree)}
     */
    public RegionBSPTree3D copy() {
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.copy(this);

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexVolume> toConvex() {
        final List<ConvexVolume> result = new ArrayList<>();

        toConvexRecursive(getRoot(), ConvexVolume.full(), result);

        return result;
    }

    /** Recursive method to compute the convex volumes of all inside leaf nodes in the subtree rooted at the given
     * node. The computed convex volumes are added to the given list.
     * @param node root of the subtree to compute the convex volumes for
     * @param nodeArea the convex volume for the current node; this will be split by the node's cut hyperplane to
     *      form the convex volumes for any child nodes
     * @param result list containing the results of the computation
     */
    private void toConvexRecursive(final RegionNode3D node, final ConvexVolume nodeVolume, final List<ConvexVolume> result) {
        if (node.isLeaf()) {
            // base case; only add to the result list if the node is inside
            if (node.isInside()) {
                result.add(nodeVolume);
            }
        }
        else {
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
        return raycastFirstRecursive(getRoot(), ray);
    }

    /** Recursive method used to find the first intersection of the given ray/line segment
     * with the boundary of the region.
     * @param node current BSP tree node
     * @param ray the ray used for the raycast operation
     * @return the node cut subhyperplane containing the intersection or null if no
     *      intersection exists
     */
    private ConvexSubPlane raycastFirstRecursive(final RegionNode3D node, final Segment3D ray) {
        if (node.isLeaf()) {
            // no boundary to intersect with on leaf nodes
            return null;
        }

        // establish search order
        final Plane cut = (Plane) node.getCutHyperplane();
        final Line3D line = ray.getLine();

        final boolean plusIsNear = line.getDirection().dot(cut.getNormal()) < 0;

        final RegionNode3D nearNode = plusIsNear ? node.getPlus() : node.getMinus();
        final RegionNode3D farNode = plusIsNear ? node.getMinus() : node.getPlus();

        // check the near node
        final ConvexSubPlane nearResult = raycastFirstRecursive(nearNode, ray);
        if (nearResult != null) {
            return nearResult;
        }

        // check ourselves
        final Vector3D intersection = computeRegionCutBoundaryIntersection(node, ray);
        if (intersection != null) {
            // we intersect, so our cut is the answer
            return (ConvexSubPlane) node.getCut();
        }

        // check the far node
        final ConvexSubPlane farResult = raycastFirstRecursive(farNode, ray);
        if (farResult != null) {
            return farResult;
        }

        return null;
    }

    /** Compute the intersection point between the region cut boundary and the given line segment.
     * @param node BSP tree node to compute the region cut boundary intersection for
     * @param segment line segment to compute the intersection for
     * @return the intersection point between the region cut boundary and the given line segment or
     *      null if one does not exist.
     */
    private Vector3D computeRegionCutBoundaryIntersection(final RegionNode3D node, final Segment3D segment) {
        if (node.isInternal()) {
            final Line3D line = segment.getLine();
            final Vector3D intersection = ((Plane) node.getCutHyperplane()).intersection(line);

            if (intersection != null && segment.contains(intersection)) {

                final RegionCutBoundary<Vector3D> boundary = node.getCutBoundary();

                if ((boundary.getInsideFacing() != null && boundary.getInsideFacing().contains(intersection)) ||
                        boundary.getOutsideFacing() != null && boundary.getOutsideFacing().contains(intersection)) {

                    return intersection;
                }
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected RegionSizeProperties<Vector3D> computeRegionSizeProperties() {
        // handle simple cases
        if (isFull()) {
           return new RegionSizeProperties<>(Double.POSITIVE_INFINITY, null);
        }
        else if (isEmpty()) {
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
    public static RegionBSPTree3D fromConvexVolume(final ConvexVolume volume) {
        RegionBSPTree3D tree = RegionBSPTree3D.full();
        tree.insert(volume.getBoundaries());

        return tree;
    }

    /** Create a tree instance from an array of vertices and two dimensional array of facet indices. Each facet is defined by
     * referencing vertices in the vertex array by index.
     * @param vertices array of vertices for the shape
     * @param facetIndices array defining the facets for the shape; each facet is defined as an array of indices into the vertex
     *      array
     * @param precision precision context used to compare floating point values
     * @return a new tree instance created from the defined facets
     */
    public static RegionBSPTree3D fromFacets(final Vector3D[] vertices, final int[][] facetIndices, final DoublePrecisionContext precision) {
        final RegionBSPTree3D tree = empty();

        List<Vector3D> facetVertices = new ArrayList<>();

        for (int[] facet : facetIndices) {
            for (int i=0; i<facet.length; ++i) {
                facetVertices.add(vertices[facet[i]]);
            }

            insertFacet(tree, facetVertices, precision);

            facetVertices.clear();
        }

        return tree;
    }

    /** Insert a single facet, defined by a sequence of vertices, into the given tree.
     * @param tree the tree to insert the facets into
     * @param vertices vertices defining the facet to insert
     * @param precision precision context used to compare floating point values
     */
    private static void insertFacet(final RegionBSPTree3D tree, final List<Vector3D> vertices,
            final DoublePrecisionContext precision) {

        final Plane plane = Plane.fromPoints(vertices, precision);
        final List<Vector2D> subspaceVertices = plane.toSubspace(vertices);

        // if there are only 3 vertices, then we know for certain that the area is convex
        final SubHyperplane<Vector3D> facet;
        if (subspaceVertices.size() < 4) {
            facet = ConvexSubPlane.fromVertexLoop(vertices, precision);
        }
        else {
            final SegmentPath path = SegmentPath.fromVertexLoop(subspaceVertices, precision);
            facet = new SubPlane(plane, path.toTree());
        }

        tree.insert(facet);
    }

    /** Construct a BSP tree representing an axis-oriented rectangular prism. The prism
     * is constructed by taking {@code pt} as one corner of the region and adding {@code xDelta},
     * {@code yDelta}, and {@code zDelta} to its components to create the opposite corner.
     *
     * <p>This method does <em>not</em> support construction of infinitely thin or point-like regions.
     * The length and width of the created region must be non-zero as evaluated by the given precision
     * content.</p>
     *
     * @param pt point lying in a corner of the region
     * @param xDelta distance to move along the x axis to place the other points in the
     *      prism; this value may be negative.
     * @param yDelta distance to move along the y axis to place the other points in the
     *      prism; this value may be negative.
     * @param zDelta distance to move along the z axis to place the other points in the
     *      prism; this value may be negative.
     * @param precision precision context to use for floating point comparisons
     * @return a new BSP tree instance representing a rectangular prism
     * @throws GeometryValueException if the width, height, or depth of the defined region is zero
     *      as evaluated by the given precision context.
     */
    public static RegionBSPTree3D rect(final Vector3D pt, final double xDelta, final double yDelta, final double zDelta,
            final DoublePrecisionContext precision) {

        return rect(pt, Vector3D.of(pt.getX() + xDelta, pt.getY() + yDelta, pt.getZ() + zDelta), precision);
    }

    /** Construct a BSP tree representing an axis-oriented rectangular prism. The points {@code a} and {@code b}
     * are taken to represent opposite corner points in the prism and may be specified in any order.
     *
     * <p>This method does <em>not</em> support construction of infinitely thin or point-like regions.
     * The length and width of the created region must be non-zero as evaluated by the given precision
     * content.</p>
     *
     * @param a first corner point in the rectangular prism (opposite of {@code b})
     * @param b second corner point in the rectangular prism (opposite of {@code a})
     * @param precision precision context to use for floating point comparisons
     * @return a new bsp tree instance representing a rectangular prism
     * @throws GeometryValueException if the width, height, or depth of the defined region is zero
     *      as evaluated by the given precision context.
     */
    public static RegionBSPTree3D rect(final Vector3D a, final Vector3D b, final DoublePrecisionContext precision) {

        final double minX = Math.min(a.getX(), b.getX());
        final double maxX = Math.max(a.getX(), b.getX());

        final double minY = Math.min(a.getY(), b.getY());
        final double maxY = Math.max(a.getY(), b.getY());

        final double minZ = Math.min(a.getZ(), b.getZ());
        final double maxZ = Math.max(a.getZ(), b.getZ());

        if (precision.eq(minX, maxX) || precision.eq(minY, maxY) || precision.eq(minZ, maxZ)) {
            throw new GeometryValueException("Rectangular prism has zero size: " + a + ", " + b + ".");
        }

        final Plane[] planes = {
                Plane.fromPointAndNormal(Vector3D.of(minX, minY, minZ), Vector3D.MINUS_X, precision),
                Plane.fromPointAndNormal(Vector3D.of(maxX, minY, minZ), Vector3D.PLUS_X, precision),

                Plane.fromPointAndNormal(Vector3D.of(minX, minY, minZ), Vector3D.MINUS_Y, precision),
                Plane.fromPointAndNormal(Vector3D.of(minX, maxY, minZ), Vector3D.PLUS_Y, precision),

                Plane.fromPointAndNormal(Vector3D.of(minX, minY, minZ), Vector3D.MINUS_Z, precision),
                Plane.fromPointAndNormal(Vector3D.of(minX, minY, maxZ), Vector3D.PLUS_Z, precision)
        };

        return createFromConvexPlanes(Arrays.asList(planes));
    }

    /** Construct a BSP tree containing an approximation of a sphere.
     * @param center the center of the region
     * @param radius the radius of the sphere approximation
     * @param stacks the number of stacks to use when building the sphere approximation; this determines
     *      the number of planes used between the poles to approximate the sphere.
     * @param slices the number of slices to use when building the sphere approximation; this determines
     *      the number of planes used around the equator to approximate the sphere.
     * @param precision precision context to use for floating point comparisons
     * @return a BSP tree instance approximating a sphere
     */
    public static RegionBSPTree3D sphere(final Vector3D center, final double radius, final int stacks, final int slices,
            final DoublePrecisionContext precision) {

        final List<Plane> planes = new ArrayList<>();

        // add top and bottom planes (+/- z)
        final Vector3D topZ = Vector3D.of(center.getX(), center.getY(), center.getZ() + radius);
        final Vector3D bottomZ = Vector3D.of(center.getX(), center.getY(), center.getZ() - radius);

        planes.add(Plane.fromPointAndNormal(topZ, Vector3D.PLUS_Z, precision));
        planes.add(Plane.fromPointAndNormal(bottomZ, Vector3D.MINUS_Z, precision));

        // add the side planes
        final double vDelta = Geometry.PI / stacks;
        final double hDelta = Geometry.PI * 2 / slices;

        final double adjustedRadius = (radius + (radius * Math.cos(vDelta * 0.5))) / 2.0;

        double vAngle;
        double hAngle;
        double stackRadius;
        double stackHeight;
        double x;
        double y;
        Vector3D pt;
        Vector3D norm;

        vAngle = -0.5 * vDelta;
        for (int v=0; v<stacks; ++v) {
            vAngle += vDelta;

            stackRadius = Math.sin(vAngle) * adjustedRadius;
            stackHeight = Math.cos(vAngle) * adjustedRadius;

            hAngle = -0.5 * hDelta;
            for (int h=0; h<slices; ++h) {
                hAngle += hDelta;

                x = Math.cos(hAngle) * stackRadius;
                y = Math.sin(hAngle) * stackRadius;

                norm = Vector3D.of(x, y, stackHeight).normalize();
                pt = center.add(norm.multiply(adjustedRadius));

                planes.add(Plane.fromPointAndNormal(pt, norm, precision));
            }
        }

        return createFromConvexPlanes(planes);
    }

    /** Create a BSP tree instance from the given list of convex planes. The planes must represent
     * a convex region. Not validation is performed.
     * @param planes planes defining the convex region
     * @return a new BSP tree instance created from the given planes
     */
    private static RegionBSPTree3D createFromConvexPlanes(final List<Plane> planes) {
        RegionBSPTree3D tree = RegionBSPTree3D.full();
        RegionNode3D node = tree.getRoot();

        for (Plane plane : planes) {
            node = node.cut(plane).getMinus();
        }

        return tree;
    }

    /** BSP tree node for three dimensional Euclidean space.
     */
    public static class RegionNode3D extends AbstractRegionBSPTree.AbstractRegionNode<Vector3D, RegionNode3D> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190702L;

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

    /** Class used to project points onto the 3D region boundary.
     */
    private static final class BoundaryProjector3D extends BoundaryProjector<Vector3D, RegionNode3D> {

        /** Serializable UID */
        private static final long serialVersionUID = 20190811L;

        /** Simple constructor.
         * @param point the point to project onto the region's boundary
         */
        public BoundaryProjector3D(Vector3D point) {
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
    private static class RegionSizePropertiesVisitor implements BSPTreeVisitor<Vector3D, RegionNode3D> {

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
        public void visit(final RegionNode3D node) {
            if (node.isInternal()) {
                RegionCutBoundary<Vector3D> boundary = node.getCutBoundary();
                addFacetContribution(boundary.getOutsideFacing(), false);
                addFacetContribution(boundary.getInsideFacing(), true);
            }
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

            return new RegionSizeProperties<Vector3D>(size, barycenter);
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
            }
            else if (baseBarycenter != null) {
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
}
