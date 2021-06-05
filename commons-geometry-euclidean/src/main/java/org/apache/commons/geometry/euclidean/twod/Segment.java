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

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.numbers.core.Precision;

/** Class representing a line segment in 2D Euclidean space. A line segment is a portion of
 * a line with finite start and end points.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see Lines
 * @see <a href="https://en.wikipedia.org/wiki/Line_segment">Line Segment</a>
 */
public final class Segment extends LineConvexSubset {

    /** Start point for the segment. */
    private final Vector2D startPoint;

    /** End point for the segment. */
    private final Vector2D endPoint;

    /** Construct a new instance from a line and two points on the line. Callers are responsible for
     * ensuring that the given points lie on the line and are in order of increasing abscissa.
     * No validation is performed.
     * @param line line for the segment
     * @param startPoint segment start point
     * @param endPoint segment end point
     */
    Segment(final Line line, final Vector2D startPoint, final Vector2D endPoint) {
        super(line);

        this.startPoint = startPoint;
        this.endPoint = endPoint;
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
        return startPoint.distance(endPoint);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getCentroid() {
        return startPoint.lerp(endPoint, 0.5);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getStartPoint() {
        return startPoint;
    }

    /** {@inheritDoc} */
    @Override
    public double getSubspaceStart() {
        return getLine().abscissa(startPoint);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D getEndPoint() {
        return endPoint;
    }

    /** {@inheritDoc} */
    @Override
    public double getSubspaceEnd() {
        return getLine().abscissa(endPoint);
    }

    /** {@inheritDoc} */
    @Override
    public Bounds2D getBounds() {
        return Bounds2D.builder()
                .add(startPoint)
                .add(endPoint)
                .build();
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
        return new Segment(getLine().reverse(), endPoint, startPoint);
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
        final Precision.DoubleEquivalence precision = getPrecision();
        final int startCmp = precision.compare(abscissa, getSubspaceStart());
        if (startCmp > 0) {
            final int endCmp = precision.compare(abscissa, getSubspaceEnd());
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
        return Math.max(getSubspaceStart(), Math.min(getSubspaceEnd(), abscissa));
    }

    /** {@inheritDoc} */
    @Override
    Split<LineConvexSubset> splitOnIntersection(final Line splitter, final Vector2D intersection) {
        final Line line = getLine();

        final Precision.DoubleEquivalence splitterPrecision = splitter.getPrecision();

        final int startCmp = splitterPrecision.compare(splitter.offset(startPoint), 0.0);
        final int endCmp = splitterPrecision.compare(splitter.offset(endPoint), 0.0);

        if (startCmp == 0 && endCmp == 0) {
            // the entire segment is directly on the splitter line
            return new Split<>(null, null);
        } else if (startCmp < 1 && endCmp < 1) {
            // the entire segment is on the minus side
            return new Split<>(this, null);
        } else if (startCmp > -1 && endCmp > -1) {
            // the entire segment is on the plus side
            return new Split<>(null, this);
        }

        // we need to split the line
        final Segment startSegment = new Segment(line, startPoint, intersection);
        final Segment endSegment = new Segment(line, intersection, endPoint);

        final Segment minus = (startCmp > 0) ? endSegment : startSegment;
        final Segment plus = (startCmp > 0) ? startSegment : endSegment;

        return new Split<>(minus, plus);
    }
}
