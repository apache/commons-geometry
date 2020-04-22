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
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;

/** Class representing a convex subline in 3D Euclidean space. Instances
 * need not be finite, in which case the start or end point (or both) will be null.
 */
public abstract class ConvexSubLine3D extends SubLine3D {

    /** Construct a new instance for the given line.
     * @param line line containing this subline
     */
    ConvexSubLine3D(final Line3D line) {
        super(line);
    }

    /** Return true if the subline is infinite.
     * @return true if the subline is infinite.
     */
    public abstract boolean isInfinite();

    /** Return true if the subline is finite.
     * @return true if the subline is finite.
     */
    public abstract boolean isFinite();

    /** Get the start point for the subline.
     * @return the start point for the subline, or null if no start point exists
     */
    public abstract Vector3D getStartPoint();

    /** Get the 1D start location of the subline or {@link Double#NEGATIVE_INFINITY} if
     * no start location exists.
     * @return the 1D start location of the subline or {@link Double#NEGATIVE_INFINITY} if
     *      no start location exists.
     */
    public abstract double getSubspaceStart();

    /** Get the end point for the subline.
     * @return the end point for the subline, or null if no end point exists.
     */
    public abstract Vector3D getEndPoint();

    /** Get the 1D end location of the subline or {@link Double#POSITIVE_INFINITY} if
     * no end location exists.
     * @return the 1D end location of the subline or {@link Double#POSITIVE_INFINITY} if
     *      no end location exists
     */
    public abstract double getSubspaceEnd();

    /** Get the size (length) of the subline.
     * @return the size of the subline
     */
    public abstract double getSize();

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

    /** Return true if the given point lies in the subline.
     * @param pt point to check
     * @return true if the point lies in the subline
     */
    public boolean contains(final Vector3D pt) {
        final Line3D line = getLine();
        return line.contains(pt) && containsAbscissa(line.abscissa(pt));
    }

    /** Transform this instance.
     * @param transform the transform to apply
     * @return a new, transformed instance
     */
    public abstract ConvexSubLine3D transform(Transform<Vector3D> transform);

    /** Return true if the given abscissa value is contained in the subline (ie, in the subline
     * or one of its 1D boundaries).
     * @param abscissa abscissa to check
     * @return true if {@code abscissa} lies on the inside or boundary of the subline
     */
    abstract boolean containsAbscissa(double abscissa);

    /** Create a convex subline from a line and a 1D interval on the line.
     * @param line the line containing the subline
     * @param interval 1D interval on the line
     * @return a convex subline defined by the given line and interval
     */
    public static ConvexSubLine3D fromInterval(final Line3D line, final Interval interval) {
        return fromInterval(line, interval.getMin(), interval.getMax());
    }

    /** Create a convex subline from a line and a 1D interval on the line.
     * @param line the line containing the subline
     * @param a first 1D location on the line
     * @param b second 1D location on the line
     * @return a convex subline defined by the given line and interval
     */
    public static ConvexSubLine3D fromInterval(final Line3D line, final double a, final double b) {
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
            return new Line3D.Span(line);
        }

        throw new IllegalArgumentException(MessageFormat.format(
                "Invalid convex subline interval: {0}, {1}", Double.toString(a), Double.toString(b)));
    }

    /** Create a convex subline from a line and a 1D interval on the line.
     * @param line the line containing the subline
     * @param a first 1D point on the line; must not be null
     * @param b second 1D point on the line; must not be null
     * @return a convex subline defined by the given line and interval
     */
    public static ConvexSubLine3D fromInterval(final Line3D line, final Vector1D a, final Vector1D b) {
        return fromInterval(line, a.getX(), b.getX());
    }
}
