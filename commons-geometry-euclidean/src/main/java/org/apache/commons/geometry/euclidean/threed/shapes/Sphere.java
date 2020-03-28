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
package org.apache.commons.geometry.euclidean.threed.shapes;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.partitioning.bsp.RegionCutRule;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.AbstractNSphere;
import org.apache.commons.geometry.euclidean.threed.Line3D;
import org.apache.commons.geometry.euclidean.threed.LinecastPoint3D;
import org.apache.commons.geometry.euclidean.threed.Linecastable3D;
import org.apache.commons.geometry.euclidean.threed.Plane;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Segment3D;
import org.apache.commons.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Class representing a 3 dimensional sphere in Euclidean space.
 */
public final class Sphere extends AbstractNSphere<Vector3D> implements Linecastable3D {

    /** Constant equal to {@code 4 * pi}. */
    private static final double FOUR_PI = 4.0 * Math.PI;

    /** Constant equal to {@code (4/3) * pi}. */
    private static final double FOUR_THIRDS_PI = FOUR_PI / 3.0;

    /** Construct a new sphere from its component parts.
     * @param center the center of the sphere
     * @param radius the sphere radius
     * @param precision precision context used to compare floating point numbers
     * @throws IllegalArgumentException if center is not finite or radius is not finite or is
     *      less than or equal to zero as evaluated by the given precision context
     */
    private Sphere(final Vector3D center, final double radius, final DoublePrecisionContext precision) {
        super(center, radius, precision);
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        final double r = getRadius();
        return FOUR_THIRDS_PI * r * r * r;
    }

    /** {@inheritDoc} */
    @Override
    public double getBoundarySize() {
        final double r = getRadius();
        return FOUR_PI * r * r;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D project(final Vector3D pt) {
        return project(pt, Vector3D.Unit.PLUS_X);
    }

    /** Return a {@link RegionBSPTree3D} representing an approximation of the sphere. All points
     * in the approximation are contained in the sphere (ie, they lie inside or on the boundary).
     * No guarantees are made regarding the internal structure of the returned tree. Non-boundary
     * split nodes may be used in order to balance the tree and improve performance.
     *
     * <p>The sphere approximation is created by logically dividing up the sphere into
     * {@code stacks} number of sections by planes perpendicular to the z-axis. For each such "stack",
     * {@code slices} number of boundary planes are then created around the z-pole of the sphere
     * from points lying on the sphere boundary. The total number of logical boundaries in the approximation
     * is therefore equal to {@code stacks * slices}. (Note that this may not match the number of
     * boundaries returned by {@link RegionBSPTree3D#getBoundaries()}, depending on the internal
     * structure of the returned tree.)
     *
     * <p>Choosing appropriate values for {@code stacks} and {@code slices} is a trade-off
     * between size and accuracy: approximations with large numbers of boundaries more closely
     * match the geometric properties of the sphere but at the cost of using larger tree structures.
     * In general, the smallest number of boundaries that produce an acceptable result should be used.
     *
     * @param stacks number of stacks to use when creating the approximation; each stacks
     *      contains {@code slices} number of boundary planes surrounding the
     *      z-axis of the sphere
     * @param slices number of boundary planes to create for each "stack" of the approximation
     * @return a BSP tree approximation of the sphere
     * @throws IllegalArgumentException if {@code stacks} is less than 2 or {@code slices} is less than 3
     */
    public RegionBSPTree3D toTree(final int stacks, final int slices) {
        return new SphereApproximationBuilder(this, stacks, slices).build();
    }

    /** Get the intersections of the given line with this sphere. The returned list will
     * contain either 0, 1, or 2 points.
     * <ul>
     *      <li><strong>2 points</strong> - The line is a secant line and intersects the sphere at two
     *      distinct points. The points are ordered such that the first point in the list is the first point
     *      encountered when traveling in the direction of the line. (In other words, the points are ordered
     *      by increasing abscissa value.)
     *      </li>
     *      <li><strong>1 point</strong> - The line is a tangent line and only intersects the sphere at a
     *      single point (as evaluated by the sphere's precision context).
     *      </li>
     *      <li><strong>0 points</strong> - The line does not intersect the sphere.</li>
     * </ul>
     * @param line line to intersect with the sphere
     * @return a list of intersection points between the given line and this sphere
     */
    public List<Vector3D> intersections(final Line3D line) {
        return intersections(line, Line3D::abscissa, Line3D::distance);
    }

    /** Get the first intersection point between the given line and this sphere, or null
     * if no such point exists. The "first" intersection point is the first such point
     * encountered when traveling in the direction of the line from infinity.
     * @param line line to intersect with the sphere
     * @return the first intersection point between the given line and this instance or
     *      null if no such point exists
     */
    public Vector3D firstIntersection(final Line3D line) {
        return firstIntersection(line, Line3D::abscissa, Line3D::distance);
    }

    /** {@inheritDoc} */
    @Override
    public List<LinecastPoint3D> linecast(final Segment3D segment) {
        return getLinecastStream(segment)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public LinecastPoint3D linecastFirst(final Segment3D segment) {
        return getLinecastStream(segment)
                .findFirst()
                .orElse(null);
    }

    /** Get a stream containing the linecast intersection points of the given
     * segment with this instance.
     * @param segment segment to intersect against this instance
     * @return a stream containing linecast intersection points
     */
    private Stream<LinecastPoint3D> getLinecastStream(final Segment3D segment) {
        return intersections(segment.getLine()).stream()
            .filter(segment::contains)
            .map(pt -> new LinecastPoint3D(pt, getCenter().directionTo(pt), segment.getLine()));
    }

    /** Construct a sphere from a center point and radius.
     * @param center the center of the sphere
     * @param radius the sphere radius
     * @param precision precision context used to compare floating point numbers
     * @return a sphere constructed from the given center point and radius
     * @throws IllegalArgumentException if center is not finite or radius is not finite or is
     *      less than or equal to zero as evaluated by the given precision context
     */
    public static Sphere from(final Vector3D center, final double radius, final DoublePrecisionContext precision) {
        return new Sphere(center, radius, precision);
    }

    /** Class used to construct BSP tree approximations of spheres. Structural BSP tree cuts are
     * used to help balance the tree and improve performance.
     */
    private static class SphereApproximationBuilder {

        /** Minimum number of stacks. */
        private static final int MIN_STACKS = 2;

        /** Minimum number of slices. */
        private static final int MIN_SLICES = 3;

        /** Minimum number of slices in a portion of a stack to allow a structural BSP split. */
        private static final int SLICE_SPLIT_THRESHOLD = 4;

        /** The sphere being approximated. */
        private final Sphere sphere;

        /** Number of stacks in the approximation. */
        private final int stacks;

        /** Number of slices in the approximation. */
        private final int slices;

        /** Polar delta value between vertices. */
        private final double polarDelta;

        /** Azimuth delta value between vertices. */
        private final double azimuthDelta;

        /** Create a new instance for approximating the given sphere.
         * @param sphere sphere to approximate
         * @param stacks number of "stacks" (sections parallel to the x-y plane) in the approximation
         * @param slices number of "slices" (boundary subplanes per stack) in the approximation
         * @throws IllegalArgumentException if {@code stacks} is less than 2 or {@code slices} is less than 3
         */
        SphereApproximationBuilder(final Sphere sphere, final int stacks, final int slices) {
            if (stacks < MIN_STACKS) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Sphere approximation stack number must be greater than or equal to {0}; was {1}",
                        MIN_STACKS, stacks));
            }
            if (slices < MIN_SLICES) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Sphere approximation slice number must be greater than or equal to {0}; was {1}",
                        MIN_SLICES, slices));
            }

            this.sphere = sphere;
            this.stacks = stacks;
            this.slices = slices;

            this.polarDelta = PlaneAngleRadians.PI / stacks;
            this.azimuthDelta = PlaneAngleRadians.TWO_PI / slices;
        }

        /** Build the BSP tree sphere approximation.
         * @return the BSP tree sphere approximation
         */
        public RegionBSPTree3D build() {
            final RegionBSPTree3D tree = RegionBSPTree3D.empty();

            splitAndInsertStacks(tree.getRoot(), 0, stacks);

            return tree;
        }

        /** Recursively split the given node and insert stacks. The node is split until only a single stack
         * remains, at which point slices are inserted.
         * @param node node to insert into
         * @param polarTopIdx the polar index for vertices at the top of the stack
         * @param polarBottomIdx the polar index for vertices at the bottom of the stack
         */
        private void splitAndInsertStacks(final RegionBSPTree3D.RegionNode3D node, final int polarTopIdx,
                final int polarBottomIdx) {

            final int indexDiff = polarBottomIdx - polarTopIdx;
            if (indexDiff < 2) {
                // we have a single stack, separated by vertices on two stack rows; we can now insert
                // boundary planes using the polar bottom index
                insertStack(node, polarBottomIdx);
            } else {
                // split the group of stacks in two
                final int splitIdx = (indexDiff / 2) + polarTopIdx;
                final Vector3D splitPt = pointAt(splitIdx, 0);

                node.cut(Plane.fromPointAndNormal(splitPt, Vector3D.Unit.PLUS_Z, sphere.getPrecision()),
                        RegionCutRule.INHERIT);

                splitAndInsertStacks(node.getPlus(), polarTopIdx, splitIdx);
                splitAndInsertStacks(node.getMinus(), splitIdx, polarBottomIdx);
            }
        }

        /** Insert the boundaries ("slices") for the stack with the given polar bottom index.
         * @param node node to insert into
         * @param polarBottomIdx the polar index for vertices at the bottom of the stack
         */
        private void insertStack(final RegionBSPTree3D.RegionNode3D node, final int polarBottomIdx) {
            if (slices >= SLICE_SPLIT_THRESHOLD) {
                final int splitIdx = slices / 2;

                RegionBSPTree3D.RegionNode3D splitNode = node;
                int sliceEndIdx = slices;

                if (slices % 2 != 0) {
                    // odd number of slices; we'll need to add the side directly opposite
                    // split index before splitting
                    splitNode = insertSlices(node, polarBottomIdx, slices - 1, slices);
                    --sliceEndIdx;
                }

                final Vector3D p0 = sphere.getCenter();
                final Vector3D p1 = pointAt(polarBottomIdx, splitIdx);
                final Vector3D p2 = pointAt(polarBottomIdx - 1, splitIdx);

                splitNode.cut(Plane.fromPoints(p0, p1, p2, sphere.getPrecision()), RegionCutRule.INHERIT);

                splitAndInsertSlices(splitNode.getPlus(), polarBottomIdx, 0, splitIdx);
                splitAndInsertSlices(splitNode.getMinus(), polarBottomIdx, splitIdx, sliceEndIdx);
            } else {
                insertSlices(node, polarBottomIdx, 0, slices);
            }
        }

        /** Recursively split a stack and insert slice boundaries.
         * @param node node to insert into
         * @param polarBottomIdx the polar index for vertices at the bottom of the stack
         * @param azimuthStartIdx the azimuth start index for vertices in the inserted slices
         * @param azimuthStopIdx the azimuth start index for vertices in the inserted slices
         */
        private void splitAndInsertSlices(final RegionBSPTree3D.RegionNode3D node, final int polarBottomIdx,
                final int azimuthStartIdx, final int azimuthStopIdx) {

            final int indexDiff = azimuthStopIdx - azimuthStartIdx;
            if (indexDiff >= SLICE_SPLIT_THRESHOLD) {
                final int splitIdx = ((indexDiff + 1) / 2) + azimuthStartIdx;

                final Vector3D p0 = sphere.getCenter();
                final Vector3D p1 = pointAt(polarBottomIdx, splitIdx);
                final Vector3D p2 = pointAt(polarBottomIdx - 1, splitIdx);

                node.cut(Plane.fromPoints(p0, p1, p2, sphere.getPrecision()), RegionCutRule.INHERIT);

                splitAndInsertSlices(node.getPlus(), polarBottomIdx, azimuthStartIdx, splitIdx);
                splitAndInsertSlices(node.getMinus(), polarBottomIdx, splitIdx, azimuthStopIdx);
            } else {
                insertSlices(node, polarBottomIdx, azimuthStartIdx, azimuthStopIdx);
            }
        }

        /** Insert slice boundaries. Each boundary is inserted on the minus side of the previously
         * inserted boundary.
         * @param node node to insert into
         * @param polarBottomIdx the polar index for vertices at the bottom of the stack
         * @param azimuthStartIdx the azimuth start index for vertices in the inserted slices
         * @param azimuthStopIdx the azimuth start index for vertices in the inserted slices
         * @return the node representing the interior of the inserted boundaries
         *      (the minus child of the last split BSP tree node)
         */
        private RegionBSPTree3D.RegionNode3D insertSlices(final RegionBSPTree3D.RegionNode3D node,
                final int polarBottomIdx, final int azimuthStartIdx, final int azimuthStopIdx) {

            final boolean lastStack = polarBottomIdx == stacks;

            RegionBSPTree3D.RegionNode3D currNode = node;
            Vector3D p0;
            Vector3D p1;
            Vector3D p2;

            for (int i = azimuthStartIdx + 1; i <= azimuthStopIdx; ++i) {
                p0 = pointAt(polarBottomIdx, i);
                p1 = pointAt(polarBottomIdx - 1, i);
                p2 = lastStack ?
                        pointAt(polarBottomIdx - 1, i - 1) :
                        pointAt(polarBottomIdx, i - 1);

                currNode = currNode.cut(Plane.fromPoints(p0, p1, p2, sphere.getPrecision()))
                        .getMinus();
            }

            return currNode;
        }

        /** Get the vertex at the given polar and azimuth indices.
         * @param polarIdx vertex polar index
         * @param azimuthIdx vertex azimuth index
         * @return the vertex at the given indices
         */
        private Vector3D pointAt(final int polarIdx, final int azimuthIdx) {
            final double polar = polarDelta * polarIdx;
            final double az = azimuthDelta * azimuthIdx;

            return SphericalCoordinates.toCartesian(sphere.getRadius(), az, polar)
                    .add(sphere.getCenter());
        }
    }
}
