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

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing a line segment in 2D Euclidean space. A line segment is a portion of
 * a line with finite start and end points.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see <a href="https://en.wikipedia.org/wiki/Line_segment">Line Segment</a>
 */
public final class Segment extends ConvexSubLine {

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
    Segment(final Line line, final Vector2D startPoint, final Vector2D endPoint) {
        this(line, line.abscissa(startPoint), line.abscissa(endPoint));
    }

    /** Construct a new instance from a line and two abscissa locations on the line.
     * The abscissa locations must be in increasing order. No validation is performed.
     * @param line line for the segment
     * @param start abscissa start location
     * @param end abscissa end location
     */
    Segment(final Line line, final double start, final double end) {
        super(line);

        this.start = start;
        this.end = end;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@code false}.</p>
    */
    @Override
    public boolean isFull() {
        return false;
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
    public double getSize() {
        return end - start;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getStartPoint() {
        return getLine().toSpace(start);
    }

    /** {@inheritDoc} */
    @Override
    public double getSubspaceStart() {
        return start;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getEndPoint() {
        return getLine().toSpace(end);
    }

    /** {@inheritDoc} */
    @Override
    public double getSubspaceEnd() {
        return end;
    }

    /** {@inheritDoc} */
    @Override
    public Segment transform(final Transform<Vector2D> transform) {
        final Vector2D t1 = transform.apply(getStartPoint());
        final Vector2D t2 = transform.apply(getEndPoint());

        final Line tLine = getLine().transform(transform);

        return new Segment(tLine, t1, t2);
    }

    /** {@inheritDoc} */
    @Override
    public Segment reverse() {
        return new Segment(getLine().reverse(), -end, -start);
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
    RegionLocation classifyAbscissa(final double abscissa) {
        final DoublePrecisionContext precision = getPrecision();
        int startCmp = precision.compare(abscissa, start);
        if (startCmp > 0) {
            int endCmp = precision.compare(abscissa, end);
            if (endCmp < 0) {
                return RegionLocation.INSIDE;
            } else if (endCmp == 0) {
                return RegionLocation.BOUNDARY;
            }
        } else if (startCmp == 0) {
            return RegionLocation.BOUNDARY;
        }

        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    double closestAbscissa(final double abscissa) {
        return Math.max(start, Math.min(end, abscissa));
    }

    /** {@inheritDoc} */
    @Override
    Split<ConvexSubLine> splitOnIntersection(final Line splitter, final Vector2D intersection) {
        final Line line = getLine();
        final double splitAbscissa = line.abscissa(intersection);

        Segment low = null;
        Segment high = null;

        final DoublePrecisionContext precision = getPrecision();
        int startCmp = precision.compare(splitAbscissa, start);
        if (startCmp <= 0) {
            high = this;
        }  else {
            int endCmp = precision.compare(splitAbscissa, end);
            if (endCmp >= 0) {
                low = this;
            } else {
                low = new Segment(line, start, splitAbscissa);
                high = new Segment(line, splitAbscissa, end);
            }
        }

        return createSplitResult(splitter, low, high);
    }

    /** Construct a new line segment from two points. A new line is created for the segment and points in the
     * direction from {@code startPoint} to {@code endPoint}.
     * @param startPoint segment start point
     * @param endPoint segment end point
     * @param precision precision context to use for floating point comparisons
     * @return a new line segment instance with the given start and end points
     * @throws IllegalArgumentException If the vector between {@code startPoint} and {@code endPoint} has zero length,
     *      as evaluated by the given precision context
     * @see Line#fromPoints(Vector2D, Vector2D, DoublePrecisionContext)
     */
    public static Segment fromPoints(final Vector2D startPoint, final Vector2D endPoint,
            final DoublePrecisionContext precision) {
        final Line line = Line.fromPoints(startPoint, endPoint, precision);

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
     * @see #fromLocations(Line, double, double)
     */
    public static Segment fromPoints(final Line line, final Vector2D a, final Vector2D b) {
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
    public static Segment fromLocations(final Line line, final double a, final double b) {

        if (Double.isFinite(a) && Double.isFinite(b)) {
            final double min = Math.min(a, b);
            final double max = Math.max(a, b);

            return new Segment(line, min, max);
        }

        throw new IllegalArgumentException(
                MessageFormat.format("Invalid line segment locations: {0}, {1}",
                        Double.toString(a), Double.toString(b)));
    }
}
