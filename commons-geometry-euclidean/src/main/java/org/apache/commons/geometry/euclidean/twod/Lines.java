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

import java.text.MessageFormat;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval;

/** Class containing factory methods for constructing {@link Line} and {@link LineSubset} instances.
 */
public final class Lines {

    /** Utility class; no instantiation. */
    private Lines() {
    }

    /** Create a line from two points lying on the line. The line points in the direction
     * from {@code p1} to {@code p2}.
     * @param p1 first point
     * @param p2 second point
     * @param precision precision context used to compare floating point values
     * @return new line containing {@code p1} and {@code p2} and pointing in the direction
     *      from {@code p1} to {@code p2}
     * @throws IllegalArgumentException If the vector between {@code p1} and {@code p2} has zero length,
     *      as evaluated by the given precision context
     */
    public static Line fromPoints(final Vector2D p1, final Vector2D p2, final DoublePrecisionContext precision) {
        return fromPointAndDirection(p1, p1.vectorTo(p2), precision);
    }

    /** Create a line from a point and direction.
     * @param pt point belonging to the line
     * @param dir the direction of the line
     * @param precision precision context used to compare floating point values
     * @return new line containing {@code pt} and pointing in direction {@code dir}
     * @throws IllegalArgumentException If {@code dir} has zero length, as evaluated by the
     *      given precision context
     */
    public static Line fromPointAndDirection(final Vector2D pt, final Vector2D dir,
            final DoublePrecisionContext precision) {
        if (dir.isZero(precision)) {
            throw new IllegalArgumentException("Line direction cannot be zero");
        }

        final Vector2D normalizedDir = dir.normalize();
        final double originOffset = normalizedDir.signedArea(pt);

        return new Line(normalizedDir, originOffset, precision);
    }

    /** Create a line from a point lying on the line and an angle relative to the abscissa (x) axis. Note that the
     * line does not need to intersect the x-axis; the given angle is simply relative to it.
     * @param pt point belonging to the line
     * @param angle angle of the line with respect to abscissa (x) axis, in radians
     * @param precision precision context used to compare floating point values
     * @return new line containing {@code pt} and forming the given angle with the
     *      abscissa (x) axis.
     */
    public static Line fromPointAndAngle(final Vector2D pt, final double angle,
            final DoublePrecisionContext precision) {
        final Vector2D.Unit dir = Vector2D.Unit.from(Math.cos(angle), Math.sin(angle));
        return fromPointAndDirection(pt, dir, precision);
    }

    /** Construct a ray from a start point and a direction.
     * @param startPoint ray start point
     * @param direction ray direction
     * @param precision precision context used for floating point comparisons
     * @return a new ray instance with the given start point and direction
     * @throws IllegalArgumentException If {@code direction} has zero length, as evaluated by the
     *      given precision context
     * @see #fromPointAndDirection(Vector2D, Vector2D, DoublePrecisionContext)
     */
    public static Ray rayFromPointAndDirection(final Vector2D startPoint, final Vector2D direction,
            final DoublePrecisionContext precision) {
        final Line line = Lines.fromPointAndDirection(startPoint, direction, precision);

        return new Ray(line, startPoint);
    }

    /** Construct a ray starting at the given point and continuing to infinity in the direction
     * of {@code line}. The given point is projected onto the line.
     * @param line line for the ray
     * @param startPoint start point for the ray
     * @return a new ray instance starting at the given point and continuing in the direction of
     *      {@code line}
     * @throws IllegalArgumentException if any coordinate in {@code startPoint} is NaN or infinite
     */
    public static Ray rayFromPoint(final Line line, final Vector2D startPoint) {
        return rayFromLocation(line, line.abscissa(startPoint));
    }

    /** Construct a ray starting at the given 1D location on {@code line} and continuing in the
     * direction of the line to infinity.
     * @param line line for the ray
     * @param startLocation 1D location of the ray start point
     * @return a new ray instance starting at the given 1D location and continuing to infinity
     *      along {@code line}
     * @throws IllegalArgumentException if {@code startLocation} is NaN or infinite
     */
    public static Ray rayFromLocation(final Line line, final double startLocation) {
        if (!Double.isFinite(startLocation)) {
            throw new IllegalArgumentException("Invalid ray start location: " + Double.toString(startLocation));
        }

        return new Ray(line, startLocation);
    }

    /** Construct a reverse ray from an end point and a line direction.
     * @param endPoint instance end point
     * @param lineDirection line direction
     * @param precision precision context used for floating point comparisons
     * @return a new instance with the given end point and line direction
     * @throws IllegalArgumentException If {@code lineDirection} has zero length, as evaluated by the
     *      given precision context
     * @see #fromPointAndDirection(Vector2D, Vector2D, DoublePrecisionContext)
     */
    public static ReverseRay reverseRayFromPointAndDirection(final Vector2D endPoint, final Vector2D lineDirection,
            final DoublePrecisionContext precision) {
        final Line line = Lines.fromPointAndDirection(endPoint, lineDirection, precision);

        return new ReverseRay(line, endPoint);
    }

    /** Construct a reverse ray starting at infinity and continuing in the direction of {@code line}
     * to the given end point. The point is projected onto the line.
     * @param line line for the instance
     * @param endPoint end point for the instance
     * @return a new instance starting at infinity and continuing along the line to {@code endPoint}
     * @throws IllegalArgumentException if any coordinate in {@code endPoint} is NaN or infinite
     */
    public static ReverseRay reverseRayFromPoint(final Line line, final Vector2D endPoint) {
        return reverseRayFromLocation(line, line.abscissa(endPoint));
    }

    /** Construct a reverse ray starting at infinity and continuing in the direction of {@code line}
     * to the given 1D end location.
     * @param line line for the instance
     * @param endLocation 1D location of the instance end point
     * @return a new instance starting infinity and continuing in the direction of {@code line}
     *      to the given 1D end location
     * @throws IllegalArgumentException if {@code endLocation} is NaN or infinite
     */
    public static ReverseRay reverseRayFromLocation(final Line line, final double endLocation) {
        if (!Double.isFinite(endLocation)) {
            throw new IllegalArgumentException("Invalid reverse ray end location: " + Double.toString(endLocation));
        }

        return new ReverseRay(line, endLocation);
    }

    /** Construct a new line segment from two points. A new line is created for the segment and points in the
     * direction from {@code startPoint} to {@code endPoint}.
     * @param startPoint segment start point
     * @param endPoint segment end point
     * @param precision precision context to use for floating point comparisons
     * @return a new line segment instance with the given start and end points
     * @throws IllegalArgumentException If the vector between {@code startPoint} and {@code endPoint} has zero length,
     *      as evaluated by the given precision context
     */
    public static Segment segmentFromPoints(final Vector2D startPoint, final Vector2D endPoint,
            final DoublePrecisionContext precision) {
        final Line line = Lines.fromPoints(startPoint, endPoint, precision);

        // we know that the points lie on the line and are in increasing abscissa order
        // since they were used to create the line
        return new Segment(line, startPoint, endPoint);
    }

    /** Construct a new line segment from a line and a pair of points. The returned segment represents
     * all points on the line between the projected locations of {@code a} and {@code b}. The points may
     * be given in any order.
     * @param line line forming the base of the segment
     * @param a first point
     * @param b second point
     * @return a new line segment representing the points between the projected locations of {@code a}
     *      and {@code b} on the given line
     * @throws IllegalArgumentException if either point contains NaN or infinite coordinate values
     */
    public static Segment segmentFromPoints(final Line line, final Vector2D a, final Vector2D b) {
        return segmentFromLocations(line, line.abscissa(a), line.abscissa(b));
    }

    /** Construct a new line segment from a pair of 1D locations on a line. The returned line
     * segment consists of all points between the two locations, regardless of the order the
     * arguments are given.
     * @param line line forming the base of the segment
     * @param a first 1D location on the line
     * @param b second 1D location on the line
     * @return a new line segment representing the points between {@code a} and {@code b} on
     *      the given line
     * @throws IllegalArgumentException if either of the locations is NaN or infinite
     */
    public static Segment segmentFromLocations(final Line line, final double a, final double b) {

        if (Double.isFinite(a) && Double.isFinite(b)) {
            final double min = Math.min(a, b);
            final double max = Math.max(a, b);

            return new Segment(line, min, max);
        }

        throw new IllegalArgumentException(
                MessageFormat.format("Invalid line segment locations: {0}, {1}",
                        Double.toString(a), Double.toString(b)));
    }

    /** Create a {@link LineConvexSubset} spanning the entire line. In other words, the returned
     * subset is infinite and contains all points on the given line.
     * @param line the line to span
     * @return a convex subset spanning the entire line
     */
    public static LineConvexSubset span(final Line line) {
        return new LineSpanningSubset(line);
    }

    /** Create a line subset from a line and a 1D interval on the line. The returned subset
     * uses the precision context from the line and not any precision contexts referenced by the interval.
     * @param line the line containing the subset
     * @param interval 1D interval on the line
     * @return a convex subset defined by the given line and interval
     */
    public static LineConvexSubset subsetFromInterval(final Line line, final Interval interval) {
        return subsetFromInterval(line, interval.getMin(), interval.getMax());
    }

    /** Create a line subset from a line and a 1D interval on the line. The double values may be given in any
     * order and support the use of infinite values. For example, the call
     * {@code Lines.subsetFromInterval(line, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)} will return
     * an instance representing the full span of the line.
     * @param line the line containing the subset
     * @param a first 1D location on the line
     * @param b second 1D location on the line
     * @return a line subset defined by the given line and interval
     * @throws IllegalArgumentException if either double value is NaN or both are infinite with the same sign
     *      (eg, both positive infinity or both negative infinity)
     */
    public static LineConvexSubset subsetFromInterval(final Line line, final double a, final double b) {
        final double min = Math.min(a, b);
        final double max = Math.max(a, b);

        final boolean hasMin = Double.isFinite(min);
        final boolean hasMax = Double.isFinite(max);

        if (hasMin) {
            if (hasMax) {
                // has both
                return new Segment(line, min, max);
            }
            // min only
            return new Ray(line, min);
        } else if (hasMax) {
            // max only
            return new ReverseRay(line, max);
        } else if (Double.isInfinite(min) && Double.isInfinite(max) && Double.compare(min, max) < 0) {
            return new LineSpanningSubset(line);
        }

        throw new IllegalArgumentException(MessageFormat.format(
                "Invalid line subset interval: {0}, {1}", Double.toString(a), Double.toString(b)));
    }

    /** Validate that the actual line is equivalent to the expected line, throwing an exception if not.
     * @param expected the expected line
     * @param actual the actual line
     * @throws IllegalArgumentException if the actual line is not equivalent to the expected line
     */
    static void validateLinesEquivalent(final Line expected, final Line actual) {
        if (!expected.eq(actual, expected.getPrecision())) {
            throw new IllegalArgumentException("Arguments do not represent the same line. Expected " +
                    expected + " but was " + actual + ".");
        }
    }
}
