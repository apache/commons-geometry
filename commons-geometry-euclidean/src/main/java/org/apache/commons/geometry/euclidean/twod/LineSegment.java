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

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;

public class LineSegment implements ConvexSubHyperplane<Vector2D> {

    /** The underlying line for the line segment. */
    private final Line line;

    /** Abscissa of the line segment start point. */
    private final double subspaceStart;

    /** Abscissa of the line segment end point. */
    private final double subspaceEnd;

    private final Vector2D start;

    private final Vector2D end;

    private LineSegment(final Line line, final double subspaceStart, final double subspaceEnd) {
        this.line = line;

        this.subspaceStart = subspaceStart;
        this.subspaceEnd = subspaceEnd;

        this.start = Double.isFinite(subspaceStart) ? line.toSpace(subspaceStart): null;
        this.end = Double.isFinite(subspaceEnd) ? line.toSpace(subspaceEnd): null;
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
        return subspaceStart;
    }

    /** Get the end value in the 1D subspace of the line.
     * @return the end value in the 1D subspace of the line
     */
    public double getSubspaceEnd() {
        return subspaceEnd;
    }

    /** Get the start point of the line segment or null if no start point
     * exists (ie, the segment is infinite).
     * @return the start point of the line segment or null if no start point
     *      exists
     */
    public Vector2D getStart() {
        return start;
    }

    /** Get the end point of the line segment or null if no end point
     * exists (ie, the segment is infinite).
     * @return the end point of the line segment or null if no end point
     *      exists
     */
    public Vector2D getEnd() {
        return end;
    }

    /** Return the 1D interval for the line segment.
     * @return the 1D interval for the line segment
     */
    public Interval getInterval() {
        return Interval.of(subspaceStart, subspaceEnd, getPrecision());
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
        return start == null && end == null;
    }

    /** {@inheritDoc}
     *
     * <p>This method simply returns false since all line segments
     * contain at least a single point.</p>
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return start == null || end == null;
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        if (isInfinite()) {
            return Double.POSITIVE_INFINITY;
        }

        return subspaceEnd - subspaceStart;
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(Vector2D point) {
        if (line.contains(point)) {
            final double loc = line.project(point).getX();

            final DoublePrecisionContext precision = getPrecision();
            final int startCmp = precision.compare(loc, subspaceStart);
            final int endCmp = precision.compare(loc, subspaceEnd);

            if (startCmp == 0 || endCmp == 0) {
                return RegionLocation.BOUNDARY;
            }
            else if (startCmp > 0 && endCmp < 0) {
                return RegionLocation.INSIDE;
            }
        }

        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D closest(Vector2D point) {
        final Vector2D pointOnLine = line.contains(point) ?
                point :
                line.project(point);

        final double loc = line.project(pointOnLine).getX();

        final DoublePrecisionContext precision = line.getPrecision();

        if (precision.lte(loc, subspaceStart)) {
            return start;
        }
        else if (precision.gte(loc, subspaceEnd)) {
            return end;
        }

        return pointOnLine;
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
        // TODO Auto-generated method stub
        return null;
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
        Vector2D tOrigin = transform.apply(line.toSpace(0));
        Vector2D tOne = transform.apply(line.toSpace(1));

        Line tLine = Line.fromPoints(tOrigin, tOne, getPrecision());
        double translation = tLine.toSubspace(tOrigin).getX();
        double scale = tLine.toSubspace(tOne).getX();

        double tStart = (subspaceStart * scale) + translation;
        double tEnd = (subspaceEnd * scale) + translation;

        return fromInterval(tLine, tStart, tEnd);
    }

    public static LineSegment fromPoints(final Vector2D start, final Vector2D end, final DoublePrecisionContext precision) {
        final Line line = Line.fromPoints(start, end, precision);
        final double subspaceStart = line.toSubspace(start).getX();
        final double subspaceEnd = line.toSubspace(end).getX();

        return fromInterval(line, subspaceStart, subspaceEnd);
    }

    public static LineSegment fromInterval(final Line line, final Interval interval) {
        return new LineSegment(line, interval.getMin(), interval.getMax());
    }

    public static LineSegment fromInterval(final Line line, final double a, final double b) {
        final double start = Math.min(a, b);
        final double end = Math.max(a, b);

        if (Double.isNaN(start) || Double.isNaN(end) ||
                (Double.isInfinite(start) && Double.compare(start, end) == 0)) {

            throw new IllegalArgumentException("Invalid line segment interval values: [" + start + ", " + end + "]");
        }

        return new LineSegment(line, start, end);
    }

    public static LineSegment fromInterval(final Line line, final Vector1D a, final Vector1D b) {
        return fromInterval(line, a.getX(), b.getX());
    }
}
