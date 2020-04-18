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

import java.text.MessageFormat;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing a line segment in 3D Euclidean space. A line segment is a portion of
 * a line with finite start and end points.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see <a href="https://en.wikipedia.org/wiki/Line_segment">Line Segment</a>
 */
public final class Segment3D extends ConvexSubLine3D {

    /** Start abscissa for the segment. */
    private final double start;

    /** End abscissa for the segment. */
    private final double end;

    /** Construct a new instance from a line and two points on the line. The points are projected onto
     * the line and must be in order of increasing abscissa. No validation is performed.
     * @param line line for the segment
     * @param startPoint segment start point
     * @param endPoint segment end point
     */
    Segment3D(final Line3D line, final Vector3D startPoint, final Vector3D endPoint) {
        this(line, line.abscissa(startPoint), line.abscissa(endPoint));
    }

    /** Construct a new instance from a line and two abscissa locations on the line.
     * The abscissa locations must be in increasing order. No validation is performed.
     * @param line line for the segment
     * @param start abscissa start location
     * @param end abscissa end location
     */
    Segment3D(final Line3D line, final double start, final double end) {
        super(line);

        this.start = start;
        this.end = end;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@code false}.</p>
    */
    @Override
    public boolean isInfinite() {
        return false;
    }

    /** {@inheritDoc}
     *
     * <p>This method always returns {@code true}.</p>
     */
    @Override
    public boolean isFinite() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getStartPoint() {
        return getLine().toSpace(start);
    }

    /** {@inheritDoc} */
    @Override
    public double getSubspaceStart() {
        return start;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getEndPoint() {
        return getLine().toSpace(end);
    }

    /** {@inheritDoc} */
    @Override
    public double getSubspaceEnd() {
        return end;
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return end - start;
    }

    /** {@inheritDoc} */
    @Override
    public Segment3D transform(final Transform<Vector3D> transform) {
        final Vector3D t1 = transform.apply(getStartPoint());
        final Vector3D t2 = transform.apply(getEndPoint());

        final Line3D tLine = getLine().transform(transform);

        return new Segment3D(tLine, t1, t2);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[startPoint= ")
            .append(getStartPoint())
            .append(", endPoint= ")
            .append(getEndPoint())
            .append(']');

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    boolean containsAbscissa(final double abscissa) {
        final DoublePrecisionContext precision = getPrecision();
        return precision.gte(abscissa, start) &&
                precision.lte(abscissa, end);
    }

    /** Construct a new line segment from two points. A new line is created for the segment and points in the
     * direction from {@code startPoint} to {@code endPoint}.
     * @param startPoint segment start point
     * @param endPoint segment end point
     * @param precision precision context to use for floating point comparisons
     * @return a new line segment instance with the given start and end points
     * @throws IllegalArgumentException If the vector between {@code startPoint} and {@code endPoint} has zero length,
     *      as evaluated by the given precision context
     * @see Line3D#fromPoints(Vector3D, Vector3D, DoublePrecisionContext)
     */
    public static Segment3D fromPoints(final Vector3D startPoint, final Vector3D endPoint,
            final DoublePrecisionContext precision) {
        final Line3D line = Line3D.fromPoints(startPoint, endPoint, precision);

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
     * @throws IllegalArgumentException if either point contains NaN or infinite coordinate values
     * @see #fromLocations(Line3D, double, double)
     */
    public static Segment3D fromPoints(final Line3D line, final Vector3D a, final Vector3D b) {
        return fromLocations(line, line.abscissa(a), line.abscissa(b));
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
    public static Segment3D fromLocations(final Line3D line, final double a, final double b) {

        if (Double.isFinite(a) && Double.isFinite(b)) {
            final double min = Math.min(a, b);
            final double max = Math.max(a, b);

            return new Segment3D(line, min, max);
        }

        throw new IllegalArgumentException(
                MessageFormat.format("Invalid line segment locations: {0}, {1}",
                        Double.toString(a), Double.toString(b)));
    }
}
