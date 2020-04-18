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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing a portion of a line in 2D Euclidean space that starts at infinity and
 * continues in the direction of the line up to a single end point. This is equivalent to taking a
 * {@link Ray} and reversing the line direction.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see Ray
 */
public final class TerminatedLine extends ConvexSubLine {

    /** The abscissa of the subline endpoint. */
    private final double end;

    /** Construct a new instance from the given line and end point. The end point is projected onto
     * the line. No validation is performed.
     * @param line line for the instance
     * @param endPoint end point for the instance
     */
    TerminatedLine(final Line line, final Vector2D endPoint) {
        this(line, line.abscissa(endPoint));
    }

    /** Construct a new instance from the given line and 1D end location. No valication is performed.
     * @param line line for the instance
     * @param end end location for the instance
     */
    TerminatedLine(final Line line, final double end) {
        super(line);

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
    * <p>This method always returns {@code true}.</p>
    */
    @Override
    public boolean isInfinite() {
        return true;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@code false}.</p>
    */
    @Override
    public boolean isFinite() {
        return false;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@link Double#POSITIVE_INFINITY}.</p>
    */
    @Override
    public double getSize() {
        return Double.POSITIVE_INFINITY;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@code null}.</p>
    */
    @Override
    public Vector2D getStartPoint() {
        return null;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@link Double#NEGATIVE_INFINITY}.</p>
    */
    @Override
    public double getSubspaceStart() {
        return Double.NEGATIVE_INFINITY;
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
    public TerminatedLine transform(final Transform<Vector2D> transform) {
        final Line tLine = getLine().transform(transform);
        final Vector2D tEnd = transform.apply(getEndPoint());

        return new TerminatedLine(tLine, tEnd);
    }

    /** {@inheritDoc} */
    @Override
    public Ray reverse() {
        return new Ray(getLine().reverse(), -end);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[direction= ")
            .append(getLine().getDirection())
            .append(", endPoint= ")
            .append(getEndPoint())
            .append(']');

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    RegionLocation classifyAbscissa(double abscissa) {
        int cmp = getPrecision().compare(abscissa, end);
        if (cmp < 0) {
            return RegionLocation.INSIDE;
        } else if (cmp == 0) {
            return RegionLocation.BOUNDARY;
        }

        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    double closestAbscissa(double abscissa) {
        return Math.min(end, abscissa);
    }

    /** {@inheritDoc} */
    @Override
    protected Split<ConvexSubLine> splitOnIntersection(final Line splitter, final Vector2D intersection) {

        final Line line = getLine();
        final double splitAbscissa = line.abscissa(intersection);

        ConvexSubLine low = null;
        ConvexSubLine high = null;

        int cmp = getPrecision().compare(splitAbscissa, end);
        if (cmp < 0) {
            low = new TerminatedLine(line, splitAbscissa);
            high = new Segment(line, splitAbscissa, end);
        } else {
            low = this;
        }

        return createSplitResult(splitter, low, high);
    }

    /** Construct a terminated line instance from an end point and a line direction.
     * @param endPoint instance end point
     * @param lineDirection line direction
     * @param precision precision context used for floating point comparisons
     * @return a new terminated line instance with the given end point and line direction
     * @throws IllegalArgumentException If {@code lineDirection} has zero length, as evaluated by the
     *      given precision context
     * @see Line#fromPointAndDirection(Vector2D, Vector2D, DoublePrecisionContext)
     */
    public static TerminatedLine fromPointAndDirection(final Vector2D endPoint, final Vector2D lineDirection,
            final DoublePrecisionContext precision) {
        final Line line = Line.fromPointAndDirection(endPoint, lineDirection, precision);

        return new TerminatedLine(line, endPoint);
    }

    /** Construct a terminated line instance starting at infinity and continuing in the direction of {@code line}
     * to the given end point. The point is projected onto the line.
     * @param line line for the instance
     * @param endPoint end point for the instance
     * @return a new terminated line instance starting at infinity and continuing along the line to {@code endPoint}
     * @throws IllegalArgumentException if any coordinate in {@code endPoint} is NaN or infinite
     */
    public static TerminatedLine fromPoint(final Line line, final Vector2D endPoint) {
        return fromLocation(line, line.abscissa(endPoint));
    }

    /** Construct a terminated line instance starting at infinity and continuing in the direction of {@code line}
     * to the given 1D end location.
     * @param line line for the instance
     * @param endLocation 1D location of the instance end point
     * @return a new terminated line instance starting infinity and continuing in the direction of {@code line}
     *      to the given 1D end location
     * @throws IllegalArgumentException if {@code endLocation} is NaN or infinite
     */
    public static TerminatedLine fromLocation(final Line line, final double endLocation) {
        if (!Double.isFinite(endLocation)) {
            throw new IllegalArgumentException("Invalid terminated line end location: " + Double.toString(endLocation));
        }

        return new TerminatedLine(line, endLocation);
    }
}
