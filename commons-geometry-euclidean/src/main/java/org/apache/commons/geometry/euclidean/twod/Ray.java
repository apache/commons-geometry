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

/** Class representing a ray in 2D Euclidean space. A ray is a portion of a line consisting of
 * a single start point and extending to infinity along the direction of the line.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see ReverseRay
 * @see Lines
 */
public final class Ray extends LineConvexSubset {

    /** The start point for the ray. */
    private final Vector2D startPoint;

    /** Construct a ray from a line and a start point. Callers are responsible for ensuring that the
     * given point lies on the line. No validation is performed.
     * @param line line for the ray
     * @param startPoint start point for the ray
     */
    Ray(final Line line, final Vector2D startPoint) {
        super(line);

        this.startPoint = startPoint;
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
    public Vector2D getCentroid() {
        return null;
    }

    @Override
    public Vector2D getStartPoint() {
        return startPoint;
    }

    /** {@inheritDoc} */
    @Override
    public double getSubspaceStart() {
        return getLine().abscissa(startPoint);
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

    /** {@inheritDoc}
     *
     * <p>This method always returns {@code null}.</p>
     */
    @Override
    public Bounds2D getBounds() {
        return null; // infinite; no bounds
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
        return new ReverseRay(getLine().reverse(), startPoint);
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classifyAbscissa(final double abscissa) {
        final int cmp = getPrecision().compare(abscissa, getSubspaceStart());
        if (cmp > 0) {
            return RegionLocation.INSIDE;
        } else if (cmp == 0) {
            return RegionLocation.BOUNDARY;
        }

        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
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
    double closestAbscissa(final double abscissa) {
        return Math.max(getSubspaceStart(), abscissa);
    }

    /** {@inheritDoc} */
    @Override
    Split<LineConvexSubset> splitOnIntersection(final Line splitter, final Vector2D intersection) {
        final Line line = getLine();
        final Precision.DoubleEquivalence splitterPrecision = splitter.getPrecision();

        final int startCmp = splitterPrecision.compare(splitter.offset(startPoint), 0.0);
        final boolean pointsTowardPlus = splitter.getOffsetDirection().dot(line.getDirection()) >= 0.0;

        if (pointsTowardPlus && startCmp > -1) {
            // entirely on plus side
            return new Split<>(null, this);
        } else if (!pointsTowardPlus && startCmp < 1) {
            // entirely on minus side
            return new Split<>(this, null);
        }

        // we're going to be split
        final Segment splitSeg = new Segment(line, startPoint, intersection);
        final Ray splitRay = new Ray(line, intersection);

        final LineConvexSubset minus = (startCmp > 0) ? splitRay : splitSeg;
        final LineConvexSubset plus = (startCmp > 0) ? splitSeg : splitRay;

        return new Split<>(minus, plus);
    }
}
