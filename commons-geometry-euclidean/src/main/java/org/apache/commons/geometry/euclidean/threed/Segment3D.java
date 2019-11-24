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

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Line3D.SubspaceTransform;

/** Class representing a line segment in 3 dimensional Euclidean space.
 *
 * <p>This class is guaranteed to be immutable.</p>
 */
public final class Segment3D extends AbstractSubLine3D<Interval> {

    /** Serializable UID. */
    private static final long serialVersionUID = 20190702L;

    /** String used to indicate the start point of the segment in the toString() representation. */
    private static final String START_STR = "start= ";

    /** String used to indicate the direction the segment in the toString() representation. */
    private static final String DIR_STR = "direction= ";

    /** String used to indicate the end point of the segment in the toString() representation. */
    private static final String END_STR = "end= ";

    /** String used as a separator value in the toString() representation. */
    private static final String SEP_STR = ", ";

    /** The interval representing the region of the line contained in
     * the line segment.
     */
    private final Interval interval;

    /** Construct a line segment from an underlying line and a 1D interval
     * on it.
     * @param line the underlying line
     * @param interval 1D interval on the line defining the line segment
     */
    private Segment3D(final Line3D line, final Interval interval) {
        super(line);

        this.interval = interval;
    }

    /** Get the start value in the 1D subspace of the line.
     * @return the start value in the 1D subspace of the line.
     */
    public double getSubspaceStart() {
        return interval.getMin();
    }

    /** Get the end value in the 1D subspace of the line.
     * @return the end value in the 1D subspace of the line
     */
    public double getSubspaceEnd() {
        return interval.getMax();
    }

    /** Get the start point of the line segment or null if no start point
     * exists (ie, the segment is infinite).
     * @return the start point of the line segment or null if no start point
     *      exists
     */
    public Vector3D getStartPoint() {
        return interval.hasMinBoundary() ? getLine().toSpace(interval.getMin()) : null;
    }

    /** Get the end point of the line segment or null if no end point
     * exists (ie, the segment is infinite).
     * @return the end point of the line segment or null if no end point
     *      exists
     */
    public Vector3D getEndPoint() {
        return interval.hasMaxBoundary() ? getLine().toSpace(interval.getMax()) : null;
    }

    /** Return true if the segment is infinite.
     * @return true if the segment is infinite.
     */
    public boolean isInfinite() {
        return interval.isInfinite();
    }

    /** Return true if the segment is finite.
     * @return true if the segment is finite.
     */
    public boolean isFinite() {
        return interval.isFinite();
    }

    /** Return the 1D interval for the line segment.
     * @return the 1D interval for the line segment
     * @see #getSubspaceRegion()
     */
    public Interval getInterval() {
        return interval;
    }

    /** {@inheritDoc}
     *
     * <p>This is an alias for {@link #getInterval()}.</p>
     */
    @Override
    public Interval getSubspaceRegion() {
        return getInterval();
    }

    /** Return true if the given point lies in the segment.
     * @param pt point to check
     * @return true if the point lies in the segment
     */
    public boolean contains(final Vector3D pt) {
        final Line3D line = getLine();
        return line.contains(pt) && interval.contains(line.toSubspace(pt));
    }

    /** Transform this instance.
     * @param transform the transform to apply
     * @return a new, transformed instance
     */
    public Segment3D transform(final Transform<Vector3D> transform) {
        final SubspaceTransform st = getLine().subspaceTransform(transform);

        return new Segment3D(st.getLine(), interval.transform(st.getTransform()));
    }

    /** Return a string representation of the segment.
     *
     * <p>In order to keep the representation short but informative, the exact format used
     * depends on the properties of the instance, as demonstrated in the examples
     * below.
     * <ul>
     *      <li>Infinite segment -
     *          {@code "Segment3D[lineOrigin= (0.0, 0.0, 0.0), lineDirection= (1.0, 0.0, 0.0)]}"</li>
     *      <li>Start point but no end point -
     *          {@code "Segment3D[start= (0.0, 0.0, 0.0), direction= (1.0, 0.0, 0.0)]}"</li>
     *      <li>End point but no start point -
     *          {@code "Segment3D[direction= (1.0, 0.0, 0.0), end= (0.0, 0.0, 0.0)]}"</li>
     *      <li>Start point and end point -
     *          {@code "Segment3D[start= (0.0, 0.0, 0.0), end= (1.0, 0.0, 0.0)]}"</li>
     * </ul>
     */
    @Override
    public String toString() {
        final Vector3D startPoint = getStartPoint();
        final Vector3D endPoint = getEndPoint();

        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append('[');

        if (startPoint != null && endPoint != null) {
            sb.append(START_STR)
                .append(startPoint)
                .append(SEP_STR)
                .append(END_STR)
                .append(endPoint);
        } else if (startPoint != null) {
            sb.append(START_STR)
                .append(startPoint)
                .append(SEP_STR)
                .append(DIR_STR)
                .append(getLine().getDirection());
        } else if (endPoint != null) {
            sb.append(DIR_STR)
                .append(getLine().getDirection())
                .append(SEP_STR)
                .append(END_STR)
                .append(endPoint);
        } else {
            final Line3D line = getLine();

            sb.append("lineOrigin= ")
                .append(line.getOrigin())
                .append(SEP_STR)
                .append("lineDirection= ")
                .append(line.getDirection());
        }

        sb.append(']');

        return sb.toString();
    }

    /** Create a line segment between two points. The underlying line points in the direction from {@code start}
     * to {@code end}.
     * @param start start point for the line segment
     * @param end end point for the line segment
     * @param precision precision context used to determine floating point equality
     * @return a new line segment between {@code start} and {@code end}.
     */
    public static Segment3D fromPoints(final Vector3D start, final Vector3D end,
            final DoublePrecisionContext precision) {

        final Line3D line = Line3D.fromPoints(start, end, precision);
        return fromPointsOnLine(line, start, end);
    }

    /** Construct a line segment from a starting point and a direction that the line should extend to
     * infinity from. This is equivalent to constructing a ray.
     * @param start start point for the segment
     * @param direction direction that the line should extend from the segment
     * @param precision precision context used to determine floating point equality
     * @return a new line segment starting from the given point and extending to infinity in the
     *      specified direction
     */
    public static Segment3D fromPointAndDirection(final Vector3D start, final Vector3D direction,
            final DoublePrecisionContext precision) {
        final Line3D line = Line3D.fromPointAndDirection(start, direction, precision);
        return fromInterval(line, Interval.min(line.toSubspace(start).getX(), precision));
    }

    /** Create a line segment from an underlying line and a 1D interval on the line.
     * @param line the line that the line segment will belong to
     * @param interval 1D interval on the line
     * @return a line segment defined by the given line and interval
     */
    public static Segment3D fromInterval(final Line3D line, final Interval interval) {
        return new Segment3D(line, interval);
    }

    /** Create a line segment from an underlying line and a 1D interval on the line.
     * @param line the line that the line segment will belong to
     * @param a first 1D location on the line
     * @param b second 1D location on the line
     * @return a line segment defined by the given line and interval
     */
    public static Segment3D fromInterval(final Line3D line, final double a, final double b) {
        return fromInterval(line, Interval.of(a, b, line.getPrecision()));
    }

    /** Create a line segment from an underlying line and a 1D interval on the line.
     * @param line the line that the line segment will belong to
     * @param a first 1D point on the line; must not be null
     * @param b second 1D point on the line; must not be null
     * @return a line segment defined by the given line and interval
     */
    public static Segment3D fromInterval(final Line3D line, final Vector1D a, final Vector1D b) {
        return fromInterval(line, a.getX(), b.getX());
    }

    /** Create a new line segment from a line and points known to lie on the line.
     * @param line the line that the line segment will belong to
     * @param start line segment start point known to lie on the line
     * @param end line segment end poitn known to lie on the line
     * @return a new line segment created from the line and points
     */
    private static Segment3D fromPointsOnLine(final Line3D line, final Vector3D start, final Vector3D end) {
        final double subspaceStart = line.toSubspace(start).getX();
        final double subspaceEnd = line.toSubspace(end).getX();

        return fromInterval(line, subspaceStart, subspaceEnd);
    }
}
