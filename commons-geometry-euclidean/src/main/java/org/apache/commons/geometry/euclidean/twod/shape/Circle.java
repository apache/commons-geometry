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
package org.apache.commons.geometry.euclidean.twod.shape;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.geometry.core.partitioning.bsp.RegionCutRule;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.AbstractNSphere;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.LinecastPoint2D;
import org.apache.commons.geometry.euclidean.twod.Linecastable2D;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.PolarCoordinates;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Class representing a circle in 2 dimensional Euclidean space.
 */
public final class Circle extends AbstractNSphere<Vector2D> implements Linecastable2D {

    /** Construct a new circle from its component parts.
     * @param center the center of the circle
     * @param radius the circle radius
     * @param precision precision context used to compare floating point numbers
     * @throws IllegalArgumentException if center is not finite or radius is not finite or is
     *      less than or equal to zero as evaluated by the given precision context
     */
    private Circle(final Vector2D center, final double radius, final DoublePrecisionContext precision) {
        super(center, radius, precision);
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        final double r = getRadius();
        return Math.PI * r * r;
    }

    /** {@inheritDoc} */
    @Override
    public double getBoundarySize() {
        return PlaneAngleRadians.TWO_PI * getRadius();
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D project(final Vector2D pt) {
        return project(pt, Vector2D.Unit.PLUS_X);
    }

    /** Return a {@link RegionBSPTree2D} representing an approximation of the circle.
     * All points in the approximation are contained in the circle (ie, they lie inside
     * or on the boundary). No guarantees are made regarding the internal structure of
     * the returned tree. Non-boundary split nodes may be used in order to balance the tree
     * and improve performance.
     *
     * <p>Choosing an appropriate number of segments for an approximation is a trade-off
     * between size and accuracy: approximations with large numbers of segments more closely
     * match the geometric properties of the circle but at the cost of using larger tree
     * structures. In general, the smallest number of segments that produces an acceptable
     * result should be used.
     * @param segments number of line segments to use for the boundary of
     *      the circle approximation
     * @return a BSP tree approximation of the circle
     * @throws IllegalArgumentException if {@code segments} is less than 3
     */
    public RegionBSPTree2D toTree(final int segments) {
        return new CircleApproximationBuilder(this, segments).build();
    }

    /** Get the intersections of the given line with this circle. The returned list will
     * contain either 0, 1, or 2 points.
     * <ul>
     *      <li><strong>2 points</strong> - The line is a secant line and intersects the circle at two
     *      distinct points. The points are ordered such that the first point in the list is the first point
     *      encountered when traveling in the direction of the line. (In other words, the points are ordered
     *      by increasing abscissa value.)
     *      </li>
     *      <li><strong>1 point</strong> - The line is a tangent line and only intersects the circle at a
     *      single point (as evaluated by the circle's precision context).
     *      </li>
     *      <li><strong>0 points</strong> - The line does not intersect the circle.</li>
     * </ul>
     * @param line line to intersect with the circle
     * @return a list of intersection points between the given line and this circle
     */
    public List<Vector2D> intersections(final Line line) {
        return intersections(line, Line::abscissa, Line::distance);
    }

    /** Get the first intersection point between the given line and this circle, or null
     * if no such point exists. The "first" intersection point is the first such point
     * encountered when traveling in the direction of the line from infinity.
     * @param line line to intersect with the circle
     * @return the first intersection point between the given line and this instance or
     *      null if no such point exists
     */
    public Vector2D firstIntersection(final Line line) {
        return firstIntersection(line, Line::abscissa, Line::distance);
    }

    /** {@inheritDoc} */
    @Override
    public List<LinecastPoint2D> linecast(final LineConvexSubset segment) {
        return getLinecastStream(segment)
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public LinecastPoint2D linecastFirst(final LineConvexSubset segment) {
        return getLinecastStream(segment)
                .findFirst()
                .orElse(null);
    }

    /** Get a stream containing the linecast intersection points of the given
     * segment with this instance.
     * @param segment segment to intersect against this instance
     * @return a stream containing linecast intersection points
     */
    private Stream<LinecastPoint2D> getLinecastStream(final LineConvexSubset segment) {
        return intersections(segment.getLine()).stream()
            .filter(segment::contains)
            .map(pt -> new LinecastPoint2D(pt, getCenter().directionTo(pt), segment.getLine()));
    }

    /** Construct a circle from a center point and radius.
     * @param center the center point of the circle
     * @param radius the circle radius
     * @param precision precision precision context used to compare floating point numbers
     * @return a circle with the given center and radius
     * @throws IllegalArgumentException if center is not finite or radius is not finite or is
     *      less than or equal to zero as evaluated by the given precision context
     */
    public static Circle from(final Vector2D center, final double radius, final DoublePrecisionContext precision) {
        return new Circle(center, radius, precision);
    }

    /** Class used to build BSP tree circle approximations. Structural BSP tree cuts are
     * used to help balance the tree and improve performance.
     */
    private static class CircleApproximationBuilder {

        /** The minimum number of segments required to create a circle approximation.
         */
        private static final int MIN_SEGMENTS = 3;

        /** Minimum number of line segments in a portion of the approximation in order
         * to allow a structural BSP split.
         */
        private static final int SPLIT_THRESHOLD = 4;

        /** Circle being approximated. */
        private final Circle circle;

        /** Number of boundary segments in the approximation. */
        private final int segments;

        /** Angle delta between vertex points. */
        private final double angleDelta;

        /** Create a new instance for approximating the given circle.
         * @param circle circle to approximate
         * @param segments number of boundary segments in the approximation
         * @throws IllegalArgumentException if {@code segments} is less than 3
         */
        CircleApproximationBuilder(final Circle circle, final int segments) {
            if (segments < MIN_SEGMENTS) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Circle approximation segment number must be greater than or equal to {0}; was {1}",
                        MIN_SEGMENTS, segments));
            }

            this.circle = circle;

            this.segments = segments;
            this.angleDelta = PlaneAngleRadians.TWO_PI / segments;
        }

        /** Build the BSP tree circle approximation.
         * @return the BSP tree circle approximation
         */
        public RegionBSPTree2D build() {
            final RegionBSPTree2D tree = RegionBSPTree2D.empty();
            final RegionBSPTree2D.RegionNode2D root = tree.getRoot();

            if (segments < SPLIT_THRESHOLD) {
                insert(root, 0, segments);
            } else {
                // split the circle in half (or mostly in half if an odd number of segments)
                final int splitIdx = segments / 2;
                final Vector2D p0 = pointAt(0);
                final Vector2D p1 = pointAt(splitIdx);

                root.cut(Lines.fromPoints(p0, p1, circle.getPrecision()), RegionCutRule.INHERIT);

                splitAndInsert(root.getPlus(), 0, splitIdx);
                splitAndInsert(root.getMinus(), splitIdx, segments);
            }

            return tree;
        }

        /** Split the given node if possible and recursively add boundary segments.
         * @param node current tree node
         * @param startIdx index of the start point for this node's boundary segments
         * @param stopIdx index of the end point for this node's boundary segments
         */
        private void splitAndInsert(final RegionBSPTree2D.RegionNode2D node, final int startIdx, final int stopIdx) {
            if (stopIdx - startIdx >= SPLIT_THRESHOLD) {
                final int splitIdx = ((stopIdx - startIdx + 1) / 2) + startIdx;
                final Vector2D p0 = circle.getCenter();
                final Vector2D p1 = pointAt(splitIdx);

                node.cut(Lines.fromPoints(p0, p1, circle.getPrecision()), RegionCutRule.INHERIT);

                splitAndInsert(node.getPlus(), startIdx, splitIdx);
                splitAndInsert(node.getMinus(), splitIdx, stopIdx);
            } else {
                insert(node, startIdx, stopIdx);
            }
        }

        /** Insert boundary segments into the given node. No structural splits are created.
         * @param node current tree node
         * @param startIdx index of the start point for this node's boundary segments
         * @param stopIdx index of the end point for this node's boundary segments
         */
        private void insert(final RegionBSPTree2D.RegionNode2D node, final int startIdx, final int stopIdx) {

            RegionBSPTree2D.RegionNode2D currNode = node;
            Vector2D currPt;
            Vector2D prevPt = pointAt(startIdx);
            for (int i = startIdx + 1; i <= stopIdx; ++i) {
                currPt = pointAt(i);

                currNode = currNode.cut(Lines.fromPoints(prevPt, currPt, circle.getPrecision()))
                        .getMinus();

                prevPt = currPt;
            }
        }

        /** Get the boundary vertex point at the given index.
         * @param idx vertex point index
         * @return the vertex point at the given index
         */
        private Vector2D pointAt(int idx) {
            return PolarCoordinates.toCartesian(circle.getRadius(), idx * angleDelta)
                    .add(circle.getCenter());
        }
    }
}
