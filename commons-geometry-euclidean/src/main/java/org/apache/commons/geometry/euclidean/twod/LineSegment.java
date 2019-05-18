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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;

/** Class representing a line segment in 2D Euclidean space. Segments
 * need not be finite, in which case the start or end point (or both)
 * will be null.
 */
public class LineSegment implements ConvexSubHyperplane<Vector2D> {

    /** The underlying line for the line segment. */
    private final Line line;

    /** The interval representing the region of the line contained in
     * the line segment.
     */
    private final Interval interval;

    /** Construct a line segment from an underlying line and a 1D interval
     * on it.
     * @param line the underlying line
     * @param interval 1D interval on the line defining the line segment
     */
    private LineSegment(final Line line, final Interval interval) {
        this.line = line;
        this.interval = interval;
    }

    /** Get the line that this segment lies on. This method is an alias
     * for {@link getHyperplane()}.
     * @return the line that this segment lies on
     * @see #getHyperplane()
     */
    public Line getLine() {
        return getHyperplane();
    }

    /** {@inheritDoc} */
    @Override
    public Line getHyperplane() {
        return line;
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
    public Vector2D getStart() {
        return interval.hasMinBoundary() ? line.toSpace(interval.getMin()): null;
    }

    /** Get the end point of the line segment or null if no end point
     * exists (ie, the segment is infinite).
     * @return the end point of the line segment or null if no end point
     *      exists
     */
    public Vector2D getEnd() {
        return interval.hasMaxBoundary() ? line.toSpace(interval.getMax()): null;
    }

    /** Return the 1D interval for the line segment.
     * @return the 1D interval for the line segment
     */
    public Interval getInterval() {
        return interval;
    }

    /** Return the object used to perform floating point comparisons, which is the
     * same object used by the underlying {@link Line).
     * @return precision object used to perform floating point comparisons.
     */
    public DoublePrecisionContext getPrecision() {
        return line.getPrecision();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        return interval.isFull();
    }

    /** {@inheritDoc}
     *
     * <p>This method simply returns false since all line segments
     * contain at least a single point.</p>
     */
    @Override
    public boolean isEmpty() {
        return interval.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return interval.isInfinite();
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return interval.getSize();
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(Vector2D point) {
        if (line.contains(point)) {
            final Vector1D loc = line.toSubspace(point);

            return interval.classify(loc);
        }

        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D closest(Vector2D point) {
        final Vector2D pointOnLine = line.contains(point) ?
                point :
                line.project(point);

        final Vector1D loc = line.toSubspace(pointOnLine);
        if (interval.contains(loc)) {
            return pointOnLine;
        }

        return line.toSpace(interval.project(loc));
    }

    /** {@inheritDoc} */
    @Override
    public Builder<Vector2D> builder() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<LineSegment> toConvex() {
        return Arrays.asList(this);
    }

    /** {@inheritDoc} */
    @Override
    public Split<Vector2D> split(Hyperplane<Vector2D> splitter) {
        final Line splitterLine = (Line) splitter;

        if (isInfinite()) {
            return splitInfinite(splitterLine);
        }
        return splitFinite(splitterLine);
    }

    /** {@inheritDoc} */
    @Override
    public LineSegment transform(Transform<Vector2D> transform) {
        if (!isInfinite()) {
            // simple case; just transform the points directly
            final Vector2D tStart = transform.apply(getStart());
            final Vector2D tEnd = transform.apply(getEnd());

            return LineSegment.fromPoints(tStart, tEnd, getPrecision());
        }

        // determine how the line has transformed
        final Vector2D tOrigin = transform.apply(line.toSpace(0));
        final Vector2D tOne = transform.apply(line.toSpace(1));
        final Line tLine = Line.fromPoints(tOrigin, tOne, getPrecision());

        final double translation = tLine.toSubspace(tOrigin).getX();
        final double scale = tLine.toSubspace(tOne.subtract(tOrigin)).getX();

        final double tStart = (getSubspaceStart() * scale) + translation;
        final double tEnd = (getSubspaceEnd() * scale) + translation;

        return fromInterval(tLine, tStart, tEnd);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(line, getStart(), getEnd());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (!(obj instanceof LineSegment)) {
            return false;
        }

        LineSegment other = (LineSegment) obj;

        return Objects.equals(line, other.line) &&
                Objects.equals(getStart(), other.getStart()) &&
                Objects.equals(getEnd(), other.getEnd());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[start= ")
            .append(getStart())
            .append(", end= ")
            .append(getEnd())
            .append(']');

        return sb.toString();
    }

    /** Method used to split the instance with the given line when the instance has
     * infinite size.
     * @param splitter the splitter line
     * @return the split convex subhyperplane
     */
    private ConvexSubHyperplane.Split<Vector2D> splitInfinite(Line splitter) {
        final Vector2D intersection = splitter.intersection(line);

        if (intersection == null) {
            // the lines are parallel
            final double originOffset = splitter.offset(line.getOrigin());

            final int sign = getPrecision().sign(originOffset);
            if (sign < 0) {
                return new ConvexSubHyperplane.Split<Vector2D>(this, null);
            }
            else if (sign > 0) {
                return new ConvexSubHyperplane.Split<Vector2D>(null, this);
            }
            return new ConvexSubHyperplane.Split<Vector2D>(null, null);
        }
        else {
            // the lines intersect
            final double intersectionAbscissa = line.toSubspace(intersection).getX();

            final double startAbscissa = getSubspaceStart();
            final double endAbscissa = getSubspaceEnd();

            LineSegment startSegment = null;
            LineSegment endSegment = null;

            if (endAbscissa <= intersectionAbscissa) {
                // the entire segment is before the intersection
                startSegment = this;
                endSegment = null;
            }
            else if (startAbscissa >= intersectionAbscissa) {
                // the entire segment is after the intersection
                startSegment = null;
                endSegment = this;
            }
            else {
                // the intersection is in the middle
                startSegment = fromInterval(line, Interval.of(startAbscissa, intersectionAbscissa, getPrecision()));
                endSegment = fromInterval(line, Interval.of(intersectionAbscissa, endAbscissa, getPrecision()));
            }

            final double startOffset = splitter.offset(line.toSpace(intersectionAbscissa - 1));
            final double startCmp = getPrecision().sign(startOffset);

            final LineSegment minus = (startCmp > 0) ? endSegment: startSegment;
            final LineSegment plus = (startCmp > 0) ? startSegment : endSegment;

            return new ConvexSubHyperplane.Split<Vector2D>(minus, plus);
        }
    }

    /** Method used to split the instance with the given line when the instance has
     * finite size.
     * @param splitter the splitter line
     * @return the split convex subhyperplane
     */
    private ConvexSubHyperplane.Split<Vector2D> splitFinite(Line splitter) {

        final DoublePrecisionContext precision = getPrecision();

        final Vector2D start = getStart();
        final Vector2D end = getEnd();

        final double startOffset = splitter.offset(start);
        final double endOffset = splitter.offset(end);

        final int startCmp = precision.sign(startOffset);
        final int endCmp = precision.sign(endOffset);

        // startCmp |   endCmp  |   result
        // --------------------------------
        // 0        |   0       |   hyper
        // 0        |   < 0     |   minus
        // 0        |   > 0     |   plus
        // < 0      |   0       |   minus
        // < 0      |   < 0     |   minus
        // < 0      |   > 0     |   SPLIT
        // > 0      |   0       |   plus
        // > 0      |   < 0     |   SPLIT
        // > 0      |   > 0     |   plus

        if (startCmp == 0 && endCmp == 0) {
            // the entire line segment is directly on the splitter line
            return new ConvexSubHyperplane.Split<Vector2D>(null, null);
        }
        else if (startCmp <= 0 && endCmp <= 0) {
            // the entire line segment is on the minus side
            return new ConvexSubHyperplane.Split<Vector2D>(this, null);
        }
        else if (startCmp >= 0 && endCmp >= 0) {
            // the entire line segment is on the plus side
            return new ConvexSubHyperplane.Split<Vector2D>(null, this);
        }

        // we need to split the line
        final Vector2D intersection = splitter.intersection(line);
        final double intersectionAbscissa = line.toSubspace(intersection).getX();

        final LineSegment startSegment = LineSegment.fromInterval(line, getSubspaceStart(), intersectionAbscissa);
        final LineSegment endSegment = LineSegment.fromInterval(line, intersectionAbscissa, getSubspaceEnd());

        final LineSegment minus = (startCmp > 0) ? endSegment: startSegment;
        final LineSegment plus = (startCmp > 0) ? startSegment : endSegment;

        return new ConvexSubHyperplane.Split<Vector2D>(minus, plus);
    }

    /** Create a line segment between two points. The underlying line points in the direction from {@code start}
     * to {@code end}.
     * @param start start point for the line segment
     * @param end end point for the line segment
     * @param precision precision context used to determine floating point equality
     * @return a new line segment between {@code start} and {@code end}.
     */
    public static LineSegment fromPoints(final Vector2D start, final Vector2D end, final DoublePrecisionContext precision) {
        final Line line = Line.fromPoints(start, end, precision);
        final double subspaceStart = line.toSubspace(start).getX();
        final double subspaceEnd = line.toSubspace(end).getX();

        return fromInterval(line, subspaceStart, subspaceEnd);
    }

    /** Create a line segment from an underlying line and a 1D interval on the line.
     * @param line the line that the line segment will belong to
     * @param interval 1D interval on the line
     * @return a line segment defined by the given line and interval
     */
    public static LineSegment fromInterval(final Line line, final Interval interval) {
        return new LineSegment(line, interval);
    }

    /** Create a line segment from an underlying line and a 1D interval on the line.
     * @param line the line that the line segment will belong to
     * @param a first 1D location on the line
     * @param b second 1D location on the line
     * @return a line segment defined by the given line and interval
     */
    public static LineSegment fromInterval(final Line line, final double a, final double b) {
        return fromInterval(line, Interval.of(a, b, line.getPrecision()));
    }

    /** Create a line segment from an underlying line and a 1D interval on the line.
     * @param line the line that the line segment will belong to
     * @param a first 1D point on the line
     * @param b second 1D point on the line
     * @return a line segment defined by the given line and interval
     */
    public static LineSegment fromInterval(final Line line, final Vector1D a, final Vector1D b) {
        return fromInterval(line, a.getX(), b.getX());
    }
}
