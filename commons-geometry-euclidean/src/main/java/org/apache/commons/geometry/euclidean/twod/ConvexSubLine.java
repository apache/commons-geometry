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
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.euclidean.oned.Interval;

/** Class representing a convex subset of a line in 2D Euclidean space. Instances
 * need not be finite, in which case the start or end point (or both) will be null.
 */
public abstract class ConvexSubLine extends SubLine implements ConvexSubHyperplane<Vector2D> {

    /** Construct a new instance for the given line.
     * @param line line containing this subline
     */
    ConvexSubLine(final Line line) {
        super(line);
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexSubLine> toConvex() {
        return Collections.singletonList(this);
    }

    /** {@inheritDoc}
     *
     * <p>This method always returns {@code false}.</p>
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** Get the start point for the subline.
     * @return the start point for the subline, or null if no start point exists
     */
    public abstract Vector2D getStartPoint();

    /** Get the 1D start location of the subline or {@link Double#NEGATIVE_INFINITY} if
     * no start location exists.
     * @return the 1D start location of the subline or {@link Double#NEGATIVE_INFINITY} if
     *      no start location exists.
     */
    public abstract double getSubspaceStart();

    /** Get the end point for the subline.
     * @return the end point for the subline, or null if no end point exists.
     */
    public abstract Vector2D getEndPoint();

    /** Get the 1D end location of the subline or {@link Double#POSITIVE_INFINITY} if
     * no end location exists.
     * @return the 1D end location of the subline or {@link Double#POSITIVE_INFINITY} if
     *      no end location exists
     */
    public abstract double getSubspaceEnd();

    /** {@inheritDoc} */
    @Override
    public Interval getSubspaceRegion() {
        final double start = getSubspaceStart();
        final double end = getSubspaceEnd();

        return Interval.of(start, end, getPrecision());
    }

    /** Get the 1D interval for the region. This method is an alias for {@link #getSubspaceRegion()}.
     * @return the 1D interval for the region.
     */
    public Interval getInterval() {
        return getSubspaceRegion();
    }

    /** {@inheritDoc} */
    @Override
    public Split<ConvexSubLine> split(final Hyperplane<Vector2D> splitter) {
        final Line thisLine = getLine();
        final Line splitterLine = (Line) splitter;

        final Vector2D intersection = splitterLine.intersection(thisLine);
        if (intersection == null) {
            return getNonIntersectingSplitResult(splitterLine, this);
        }
        return splitOnIntersection(splitterLine, intersection);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D closest(final Vector2D pt) {
        final Line line = getLine();
        final double abscissa = line.abscissa(pt);

        return line.toSpace(closestAbscissa(abscissa));
    }

    /** {@inheritDoc} */
    @Override
    public abstract ConvexSubLine transform(Transform<Vector2D> transform);

    /** {@inheritDoc} */
    @Override
    public abstract ConvexSubLine reverse();

    /** Get the closest value in the subspace region to the given abscissa.
     * @param abscissa input abscissa
     * @return the closest value in the subspace region to the given abscissa
     */
    abstract double closestAbscissa(double abscissa);

    /** Split this instance using the given splitter line and intersection point.
     * @param splitter splitter line
     * @param intersection intersection point between the splitter line and the line
     *      for this instance
     * @return the result of splitting this instance with the given splitter line and intersection
     *      point
     */
    abstract Split<ConvexSubLine> splitOnIntersection(Line splitter, Vector2D intersection);

    /** Create a convex subline instance from a line and a 1D interval on the line. The returned subline
     * uses the precision context from the line and not any precision contexts referenced by the interval.
     * @param line the line that the subline will belong to
     * @param interval 1D interval on the line
     * @return a subline defined by the given line and interval
     */
    public static ConvexSubLine fromInterval(final Line line, final Interval interval) {
        return fromInterval(line, interval.getMin(), interval.getMax());
    }

    /** Create a subline from a line and a 1D interval on the line. The double values may be given in any order and
     * support the use of infinite values. For example, the call
     * {@code ConvexSubLine.fromInterval(line, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY)} will return
     * an instance representing the full span of the line.
     * @param line the line that the subline will belong to
     * @param a first 1D location on the line
     * @param b second 1D location on the line
     * @return a subline defined by the given line and interval
     * @throws IllegalArgumentException if either double value is NaN or both are infinite with the same sign
     *      (eg, both positive infinity or both negative infinity)
     */
    public static ConvexSubLine fromInterval(final Line line, final double a, final double b) {
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
            return new TerminatedLine(line, max);
        } else if (Double.isInfinite(min) && Double.isInfinite(max) && Double.compare(min, max) < 0) {
            return new Line.Span(line);
        }

        throw new IllegalArgumentException(MessageFormat.format(
                "Invalid convex subline interval: {0}, {1}", Double.toString(a), Double.toString(b)));
    }
}
