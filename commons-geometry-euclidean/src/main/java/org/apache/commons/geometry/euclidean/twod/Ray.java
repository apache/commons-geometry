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

/** Class representing a ray in 2D Euclidean space. A ray is a portion of a line consisting of
 * a single start point and extending to infinity along the direction of the line.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see ReverseRay
 * @see Lines
 */
public final class Ray extends LineConvexSubset {

    /** The start abscissa value for the ray. */
    private final double start;

    /** Construct a ray from a line and a start point. The start point is projected
     * onto the line. No validation is performed.
     * @param line line for the ray
     * @param startPoint start point for the ray
     */
    Ray(final Line line, final Vector2D startPoint) {
        this(line, line.abscissa(startPoint));
    }

    /** Construct a ray from a line and a 1D start location. No validation is performed.
     * @param line line for the ray
     * @param start 1D start location
     */
    Ray(final Line line, final double start) {
        super(line);

        this.start = start;
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

    @Override
    public Vector2D getStartPoint() {
        return getLine().toSpace(start);
    }

    /** {@inheritDoc} */
    @Override
    public double getSubspaceStart() {
        return start;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@code null}.</p>
    */
    @Override
    public Vector2D getEndPoint() {
        return null;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@link Double#POSITIVE_INFINITY}.</p>
    */
    @Override
    public double getSubspaceEnd() {
        return Double.POSITIVE_INFINITY;
    }

    /** Get the direction of the ray. This is a convenience method for {@code ray.getLine().getDirection()}.
     * @return the direction of the ray
     */
    public Vector2D getDirection() {
        return getLine().getDirection();
    }

    /** {@inheritDoc} */
    @Override
    public Ray transform(final Transform<Vector2D> transform) {
        final Line tLine = getLine().transform(transform);
        final Vector2D tStart = transform.apply(getStartPoint());

        return new Ray(tLine, tStart);
    }

    /** {@inheritDoc} */
    @Override
    public ReverseRay reverse() {
        return new ReverseRay(getLine().reverse(), -start);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[startPoint= ")
            .append(getStartPoint())
            .append(", direction= ")
            .append(getLine().getDirection())
            .append(']');

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    RegionLocation classifyAbscissa(double abscissa) {
        int cmp = getPrecision().compare(abscissa, start);
        if (cmp > 0) {
            return RegionLocation.INSIDE;
        } else if (cmp == 0) {
            return RegionLocation.BOUNDARY;
        }

        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    double closestAbscissa(double abscissa) {
        return Math.max(start, abscissa);
    }

    /** {@inheritDoc} */
    @Override
    Split<LineConvexSubset> splitOnIntersection(final Line splitter, final Vector2D intersection) {

        final Line line = getLine();
        final double splitAbscissa = line.abscissa(intersection);

        LineConvexSubset low = null;
        LineConvexSubset high = null;

        int cmp = getPrecision().compare(splitAbscissa, start);
        if (cmp > 0) {
            low = new Segment(line, start, splitAbscissa);
            high = new Ray(line, splitAbscissa);
        } else {
            high = this;
        }

        return createSplitResult(splitter, low, high);
    }
}
