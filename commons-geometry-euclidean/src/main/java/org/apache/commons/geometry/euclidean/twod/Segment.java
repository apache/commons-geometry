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

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.twod.Line.SubspaceTransform;

/** <p>Class representing a line segment in 2D Euclidean space. Segments
 * need not be finite, in which case the start or end point (or both)
 * will be null.</p>
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class Segment extends AbstractSubLine<Interval>
    implements ConvexSubHyperplane<Vector2D> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190729L;

    /** The interval representing the region of the line contained in
     * the line segment.
     */
    private final Interval interval;

    /** Construct a line segment from an underlying line and a 1D interval
     * on it.
     * @param line the underlying line
     * @param interval 1D interval on the line defining the line segment
     */
    private Segment(final Line line, final Interval interval) {
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
    public Vector2D getStartPoint() {
        return interval.hasMinBoundary() ? getLine().toSpace(interval.getMin()): null;
    }

    /** Get the end point of the line segment or null if no end point
     * exists (ie, the segment is infinite).
     * @return the end point of the line segment or null if no end point
     *      exists
     */
    public Vector2D getEndPoint() {
        return interval.hasMaxBoundary() ? getLine().toSpace(interval.getMax()): null;
    }

    /** Return the 1D interval for the line segment.
     * @return the 1D interval for the line segment
     */
    public Interval getInterval() {
        return interval;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return interval.isInfinite();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinite() {
        return interval.isFinite();
    }

    /** {@inheritDoc} */
    @Override
    public Interval getSubspaceRegion() {
        return getInterval();
    }

    /** {@inheritDoc} */
    @Override
    public List<Segment> toConvex() {
        return Arrays.asList(this);
    }

    /** {@inheritDoc} */
    @Override
    public Split<Segment> split(final Hyperplane<Vector2D> splitter) {
        return splitInternal(splitter, this, (line, region) -> new Segment(line, (Interval) region));
    }

    /** {@inheritDoc} */
    @Override
    public Segment transform(Transform<Vector2D> transform) {
        final Line line = getLine();
        final SubspaceTransform st = line.subspaceTransform(transform);

        return fromInterval(st.getLine(), interval.transform(st.getTransform()));
    }

    /** Get the unique intersection of this segment with the given line. Null is
     * returned if no unique intersection point exists (ie, the lines are
     * parallel or coincident) or the line does not intersect the segment.
     * @param line line to intersect with this segment
     * @return the unique intersection point between the line and this segment
     *      or null if no such point exists.
     * @see Line#intersection(Line)
     */
    public Vector2D intersection(final Line line) {
        final Vector2D pt = getLine().intersection(line);
        return (pt != null && contains(pt)) ? pt : null;
    }

    /** Get the unique intersection of this instance with the given segment. Null
     * is returned if the lines containing the segments do not have a unique intersection
     * point (ie, they are parallel or coincident) or the intersection point is unique
     * but in not contained in both segments.
     * @param segment segment to intersect with
     * @return the unique intersection point between this segment and the argument or
     *      null if no such point exists.
     * @see Line#intersection(Line)
     */
    public Vector2D intersection(final Segment segment) {
        final Vector2D pt = intersection(segment.getLine());
        return (pt != null && segment.contains(pt)) ? pt : null;
    }

    /** {@inheritDoc} */
    @Override
    public Segment reverse() {
        final Interval reversedInterval = interval.transform(Vector1D::negate);
        return fromInterval(getLine().reverse(), reversedInterval);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(getLine(), interval);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (!(obj instanceof Segment)) {
            return false;
        }

        Segment other = (Segment) obj;

        return Objects.equals(getLine(), other.getLine()) &&
                Objects.equals(interval, other.interval);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[startPoint= ")
            .append(getStartPoint())
            .append(", endPoint= ")
            .append(getEndPoint())
            .append(", line= ")
            .append(getLine())
            .append(']');

        return sb.toString();
    }

    /** Create a line segment between two points. The underlying line points in the direction from {@code start}
     * to {@code end}.
     * @param start start point for the line segment
     * @param end end point for the line segment
     * @param precision precision context used to determine floating point equality
     * @return a new line segment between {@code start} and {@code end}.
     */
    public static Segment fromPoints(final Vector2D start, final Vector2D end, final DoublePrecisionContext precision) {
        final Line line = Line.fromPoints(start, end, precision);
        return fromPointsOnLine(line, start, end);
    }

    /** Create a line segment from an underlying line and a 1D interval on the line.
     * @param line the line that the line segment will belong to
     * @param interval 1D interval on the line
     * @return a line segment defined by the given line and interval
     */
    public static Segment fromInterval(final Line line, final Interval interval) {
        return new Segment(line, interval);
    }

    /** Create a line segment from an underlying line and a 1D interval on the line.
     * @param line the line that the line segment will belong to
     * @param a first 1D location on the line
     * @param b second 1D location on the line
     * @return a line segment defined by the given line and interval
     */
    public static Segment fromInterval(final Line line, final double a, final double b) {
        return fromInterval(line, Interval.of(a, b, line.getPrecision()));
    }

    /** Create a line segment from an underlying line and a 1D interval on the line.
     * @param line the line that the line segment will belong to
     * @param a first 1D point on the line; must not be null
     * @param b second 1D point on the line; must not be null
     * @return a line segment defined by the given line and interval
     */
    public static Segment fromInterval(final Line line, final Vector1D a, final Vector1D b) {
        return fromInterval(line, a.getX(), b.getX());
    }

    /** Create a new line segment from a line and points known to lie on the line.
     * @param line the line that the line segment will belong to
     * @param start line segment start point known to lie on the line
     * @param end line segment end poitn known to lie on the line
     * @return a new line segment created from the line and points
     */
    private static Segment fromPointsOnLine(final Line line, final Vector2D start, final Vector2D end) {
        final double subspaceStart = line.toSubspace(start).getX();
        final double subspaceEnd = line.toSubspace(end).getX();

        return fromInterval(line, subspaceStart, subspaceEnd);
    }
}
