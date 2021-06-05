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
package org.apache.commons.geometry.euclidean.threed.line;

import java.text.MessageFormat;

import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;

/** Class containing factory methods for constructing {@link Line3D} and {@link LineSubset3D} instances.
 */
public final class Lines3D {

    /** Utility class; no instantiation. */
    private Lines3D() {
    }

    /** Create a new line instance from two points that lie on the line. The line
     * direction points from the first point to the second point.
     * @param p1 first point on the line
     * @param p2 second point on the line
     * @param precision floating point precision context
     * @return a new line instance that contains both of the given point and that has
     *      a direction going from the first point to the second point
     * @throws IllegalArgumentException if the points lie too close to create a non-zero direction vector
     */
    public static Line3D fromPoints(final Vector3D p1, final Vector3D p2,
            final Precision.DoubleEquivalence precision) {
        return fromPointAndDirection(p1, p1.vectorTo(p2), precision);
    }

    /** Create a new line instance from a point and a direction.
     * @param pt a point lying on the line
     * @param dir the direction of the line
     * @param precision floating point precision context
     * @return a new line instance that contains the given point and points in the
     *      given direction
     * @throws IllegalArgumentException if {@code dir} has zero length, as evaluated by the
     *      given precision context
     */
    public static Line3D fromPointAndDirection(final Vector3D pt, final Vector3D dir,
            final Precision.DoubleEquivalence precision) {
        if (dir.isZero(precision)) {
            throw new IllegalArgumentException("Line direction cannot be zero");
        }

        final Vector3D normDirection = dir.normalize();
        final Vector3D origin = pt.reject(normDirection);

        return new Line3D(origin, normDirection, precision);
    }

    /** Construct a ray from a start point and a direction.
     * @param startPoint ray start point
     * @param direction ray direction
     * @param precision precision context used for floating point comparisons
     * @return a new ray instance with the given start point and direction
     * @throws IllegalArgumentException If {@code direction} has zero length, as evaluated by the
     *      given precision context
     * @see Lines3D#fromPointAndDirection(Vector3D, Vector3D, Precision.DoubleEquivalence)
     */
    public static Ray3D rayFromPointAndDirection(final Vector3D startPoint, final Vector3D direction,
            final Precision.DoubleEquivalence precision) {
        final Line3D line = Lines3D.fromPointAndDirection(startPoint, direction, precision);

        return new Ray3D(line, startPoint);
    }

    /** Construct a ray starting at the given point and continuing to infinity in the direction
     * of {@code line}. The given point is projected onto the line.
     * @param line line for the ray
     * @param startPoint start point for the ray
     * @return a new ray instance starting at the given point and continuing in the direction of
     *      {@code line}
     * @throws IllegalArgumentException if any coordinate in {@code startPoint} is NaN or infinite
     */
    public static Ray3D rayFromPoint(final Line3D line, final Vector3D startPoint) {
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
    public static Ray3D rayFromLocation(final Line3D line, final double startLocation) {
        if (!Double.isFinite(startLocation)) {
            throw new IllegalArgumentException("Invalid ray start location: " + startLocation);
        }

        return new Ray3D(line, startLocation);
    }

    /** Construct a reverse ray from an end point and a line direction.
     * @param endPoint instance end point
     * @param lineDirection line direction
     * @param precision precision context used for floating point comparisons
     * @return a new reverse ray with the given end point and line direction
     * @throws IllegalArgumentException If {@code lineDirection} has zero length, as evaluated by the
     *      given precision context
     * @see Lines3D#fromPointAndDirection(Vector3D, Vector3D, Precision.DoubleEquivalence)
     */
    public static ReverseRay3D reverseRayFromPointAndDirection(final Vector3D endPoint, final Vector3D lineDirection,
            final Precision.DoubleEquivalence precision) {
        final Line3D line = Lines3D.fromPointAndDirection(endPoint, lineDirection, precision);

        return new ReverseRay3D(line, endPoint);
    }

    /** Construct a reverse ray starting at infinity and continuing in the direction of {@code line}
     * to the given end point. The point is projected onto the line.
     * @param line line for the instance
     * @param endPoint end point for the instance
     * @return a new reverse ray starting at infinity and continuing along the line to {@code endPoint}
     * @throws IllegalArgumentException if any coordinate in {@code endPoint} is NaN or infinite
     */
    public static ReverseRay3D reverseRayFromPoint(final Line3D line, final Vector3D endPoint) {
        return reverseRayFromLocation(line, line.abscissa(endPoint));
    }

    /** Construct a reverse ray starting at infinity and continuing in the direction of {@code line}
     * to the given 1D end location.
     * @param line line for the instance
     * @param endLocation 1D location of the instance end point
     * @return a new reverse ray starting infinity and continuing in the direction of {@code line}
     *      to the given 1D end location
     * @throws IllegalArgumentException if {@code endLocation} is NaN or infinite
     */
    public static ReverseRay3D reverseRayFromLocation(final Line3D line, final double endLocation) {
        if (!Double.isFinite(endLocation)) {
            throw new IllegalArgumentException("Invalid reverse ray end location: " + endLocation);
        }

        return new ReverseRay3D(line, endLocation);
    }

    /** Construct a new line segment from two points. A new line is created for the segment and points in the
     * direction from {@code startPoint} to {@code endPoint}.
     * @param startPoint segment start point
     * @param endPoint segment end point
     * @param precision precision context to use for floating point comparisons
     * @return a new line segment instance with the given start and end points
     * @throws IllegalArgumentException If the vector between {@code startPoint} and {@code endPoint} has zero length,
     *      as evaluated by the given precision context
     * @see Lines3D#fromPoints(Vector3D, Vector3D, Precision.DoubleEquivalence)
     */
    public static Segment3D segmentFromPoints(final Vector3D startPoint, final Vector3D endPoint,
            final Precision.DoubleEquivalence precision) {
        final Line3D line = Lines3D.fromPoints(startPoint, endPoint, precision);

        // we know that the points lie on the line and are in increasing abscissa order
        // since they were used to create the line
        return new Segment3D(line, startPoint, endPoint);
    }

    /** Construct a new line segment from a line and a pair of points. The returned segment represents
     * all points on the line between the projected locations of {@code a} and {@code b}. The points may
     * be given in any order.
     * @param line line forming the base of the segment
     * @param a first point
     * @param b second point
     * @return a new line segment representing the points between the projected locations of {@code a}
     *      and {@code b} on the given line
     * @throws IllegalArgumentException if either point contains NaN or infinite coordinate values)
     */
    public static Segment3D segmentFromPoints(final Line3D line, final Vector3D a, final Vector3D b) {
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
    public static Segment3D segmentFromLocations(final Line3D line, final double a, final double b) {

        if (Double.isFinite(a) && Double.isFinite(b)) {
            final double min = Math.min(a, b);
            final double max = Math.max(a, b);

            return new Segment3D(line, min, max);
        }

        throw new IllegalArgumentException(
                MessageFormat.format("Invalid line segment locations: {0}, {1}",
                        Double.toString(a), Double.toString(b)));
    }

    /** Create a {@link LineConvexSubset3D} spanning the entire line. In other words, the returned
     * subset is infinite and contains all points on the given line.
     * @param line the line to span
     * @return a convex subset spanning the entire line
     */
    public static LineConvexSubset3D span(final Line3D line) {
        return new LineSpanningSubset3D(line);
    }

    /** Create a line convex subset from a line and a 1D interval on the line.
     * @param line the line containing the subset
     * @param interval 1D interval on the line
     * @return a line convex subset defined by the given line and interval
     */
    public static LineConvexSubset3D subsetFromInterval(final Line3D line, final Interval interval) {
        return subsetFromInterval(line, interval.getMin(), interval.getMax());
    }

    /** Create a line convex subset from a line and a 1D interval on the line.
     * @param line the line containing the subset
     * @param a first 1D location on the line
     * @param b second 1D location on the line
     * @return a line convex subset defined by the given line and interval
     */
    public static LineConvexSubset3D subsetFromInterval(final Line3D line, final double a, final double b) {
        final double min = Math.min(a, b);
        final double max = Math.max(a, b);

        final boolean hasMin = Double.isFinite(min);
        final boolean hasMax = Double.isFinite(max);

        if (hasMin) {
            if (hasMax) {
                // has both
                return new Segment3D(line, min, max);
            }
            // min only
            return new Ray3D(line, min);
        } else if (hasMax) {
            // max only
            return new ReverseRay3D(line, max);
        } else if (Double.isInfinite(min) && Double.isInfinite(max) && Double.compare(min, max) < 0) {
            return new LineSpanningSubset3D(line);
        }

        throw new IllegalArgumentException(MessageFormat.format(
                "Invalid line convex subset interval: {0}, {1}", Double.toString(a), Double.toString(b)));
    }

    /** Create a line convex subset from a line and a 1D interval on the line.
     * @param line the line containing the subset
     * @param a first 1D point on the line; must not be null
     * @param b second 1D point on the line; must not be null
     * @return a line convex subset defined by the given line and interval
     */
    public static LineConvexSubset3D subsetFromInterval(final Line3D line, final Vector1D a, final Vector1D b) {
        return subsetFromInterval(line, a.getX(), b.getX());
    }
}
