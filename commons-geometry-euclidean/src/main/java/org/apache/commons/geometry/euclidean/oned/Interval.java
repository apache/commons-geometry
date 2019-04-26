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
package org.apache.commons.geometry.euclidean.oned;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.HyperplaneLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanRegion;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D.RegionNode1D;

/** Class representing an interval in one dimension. The interval is defined
 * by minimum and maximum values. One or both of these values may be infinite
 * although not with the same sign.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public class Interval implements EuclideanRegion<Vector1D>, Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 20190210L;

    /** Interval instance representing the entire real number line. */
    private static final Interval REALS = new Interval(null, null);

    /** {@link OrientedPoint} instance representing the min boundary of the interval,
     * or null if no min boundary exists. If present, this instance will be negative-facing.
     * Infinite values are allowed but not NaN.
     */
    private final OrientedPoint minBoundary;

    /** {@link OrientedPoint} instance representing the max boundary of the interval,
     * or null if no max boundary exists. If present, this instance will be negative-facing.
     * Infinite values are allowed but not NaN.
     */
    private final OrientedPoint maxBoundary;

    /** Create an instance from min and max bounding hyperplanes. No validation is performed.
     * Callers are responsible for ensuring that the given hyperplanes represent a valid
     * interval.
     * @param minBoundary the min (negative-facing) hyperplane
     * @param maxBoundary the max (positive-facing) hyperplane
     */
    private Interval(final OrientedPoint minBoundary, final OrientedPoint maxBoundary) {
        this.minBoundary = minBoundary;
        this.maxBoundary = maxBoundary;
    }

    /** Get the minimum value for the interval or {@link Double#NEGATIVE_INFINITY}
     * if no minimum value exists.
     * @return the minimum value for the interval or {@link Double#NEGATIVE_INFINITY}
     *      if no minimum value exists.
     */
    public double getMin() {
        return (minBoundary != null) ? minBoundary.getLocation() : Double.NEGATIVE_INFINITY;
    }

    /** Get the maximum value for the interval or {@link Double#POSITIVE_INFINITY}
     * if no maximum value exists.
     * @return the maximum value for the interval or {@link Double#POSITIVE_INFINITY}
     *      if no maximum value exists.
     */
    public double getMax() {
        return (maxBoundary != null) ? maxBoundary.getLocation() : Double.POSITIVE_INFINITY;
    }

    /**
     * Get the {@link OrientedPoint} forming the minimum bounding hyperplane
     * of the interval, or null if none exists. If present, This hyperplane
     * is oriented to point in the negative direction.
     * @return the hyperplane forming the minimum boundary of the interval or
     *      null if no minimum boundary exists
     */
    public OrientedPoint getMinBoundary() {
        return minBoundary;
    }

    /**
     * Get the {@link OrientedPoint} forming the maximum bounding hyperplane
     * of the interval, or null if none exists. If present, this hyperplane
     * is oriented to point in the positive direction.
     * @return the hyperplane forming the maximum boundary of the interval or
     *      null if no maximum boundary exists
     */
    public OrientedPoint getMaxBoundary() {
        return maxBoundary;
    }

    /** Return true if the interval has a minimum (lower) boundary.
     * @return true if the interval has minimum (lower) boundary
     */
    public boolean hasMinBoundary() {
        return minBoundary != null;
    }

    /** Return true if the interval has a maximum (upper) boundary.
     * @return true if the interval has maximum (upper) boundary
     */
    public boolean hasMaxBoundary() {
        return maxBoundary != null;
    }

    /** True if the region is infinite, meaning that at least one of the boundaries
     * does not exist.
     * @return true if the region is infinite
     */
    public boolean isInfinite() {
        return minBoundary == null || maxBoundary == null;
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final Vector1D pt) {
        return classify(pt.getX());
    }

    /** Classify a point with respect to the interval. This is a convenience
     * overload of {@link #classify(Vector1D)} for use in one dimension.
     * @param location the location to classify
     * @return the classification of the point with respect to the interval
     * @see #classify(Vector1D)
     */
    public RegionLocation classify(final double location) {
        final RegionLocation minLoc = classifyWithBoundary(location, minBoundary);
        final RegionLocation maxLoc = classifyWithBoundary(location, maxBoundary);

        if (minLoc == RegionLocation.BOUNDARY || maxLoc == RegionLocation.BOUNDARY) {
            return RegionLocation.BOUNDARY;
        }
        else if (minLoc == RegionLocation.INSIDE && maxLoc == RegionLocation.INSIDE) {
            return RegionLocation.INSIDE;
        }
        return RegionLocation.OUTSIDE;
    }

    /** Classify the location using the given interval boundary, which may be null.
     * @param location the location to classify
     * @param boundary interval boundary to classify against
     * @return
     */
    private RegionLocation classifyWithBoundary(final double location, final OrientedPoint boundary) {
        if (Double.isNaN(location)) {
            return RegionLocation.OUTSIDE;
        }
        else if (boundary == null) {
            return RegionLocation.INSIDE;
        }
        else {
            final HyperplaneLocation hyperLoc = boundary.classify(location);

            if (hyperLoc == HyperplaneLocation.ON) {
                return RegionLocation.BOUNDARY;
            }
            else if (hyperLoc == HyperplaneLocation.PLUS) {
                return RegionLocation.OUTSIDE;
            }
            return RegionLocation.INSIDE;
        }
    }

    /** Return true if the given point location is on the inside or boundary
     * of the region. This is a convenience overload of {@link Interval#contains(Vector1D)}
     * for use in one dimension.
     * @param x the location to test
     * @return true if the location is on the inside or boundary of the region
     */
    public boolean contains(final double x) {
        return classify(x) != RegionLocation.OUTSIDE;
    }

    /** Transform this instance using the given {@link Transform}.
     * @return a new transformed interval
     */
    public Interval transform(final Transform<Vector1D> transform) {
        final OrientedPoint transformedMin = (minBoundary != null) ?
                minBoundary.transform(transform) :
                null;
        final OrientedPoint transformedMax = (maxBoundary != null) ?
                maxBoundary.transform(transform) :
                null;

        return of(transformedMin, transformedMax);
    }

    /** {@inheritDoc}
     *
     *  <p>This method always returns true since there is always at least
     *  one point that can be classified as not being on the outside of
     *  the region.</p>
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        return minBoundary == null && maxBoundary == null;
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        if (isInfinite()) {
            return Double.POSITIVE_INFINITY;
        }

        return getMax() - getMin();
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D getBarycenter() {
        if (isInfinite()) {
            return null;
        }

        final double min = getMin();
        final double max = getMax();

        return Vector1D.of((0.5 * (max - min)) + min);
    }

    /** Return a {@link RegionBSPTree1D} representing the same region as this instance.
     * @return a BSP tree representing the same region
     */
    public RegionBSPTree1D toTree() {
        final RegionBSPTree1D tree = new RegionBSPTree1D();

        RegionNode1D node = tree.getRoot();

        if (minBoundary != null) {
            node.cut(minBoundary);

            node = node.getMinus();
        }

        if (maxBoundary != null) {
            node.cut(maxBoundary);
        }

        return tree;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(minBoundary, maxBoundary);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (!(obj instanceof Interval)) {
            return false;
        }

        Interval other = (Interval) obj;

        return Objects.equals(minBoundary, other.minBoundary) &&
                Objects.equals(maxBoundary, other.maxBoundary);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[min= ")
            .append(getMin())
            .append(", max= ")
            .append(getMax())
            .append(']');

        return sb.toString();
    }

    /** Create a new interval from the given point locations. The returned interval represents
     * the region between the points, regardless of the order they are given as arguments.
     * @param a first point location
     * @param b second point location
     * @param precision precision context used to compare floating point numbers
     * @throw IllegalArgumentException if either number is {@link Double#NaN NaN} or the numbers
     *      are both infinite and have the same sign
     */
    public static Interval of(final double a, final double b, final DoublePrecisionContext precision) {
        validateIntervalValues(a, b);

        final double min = Math.min(a, b);
        final double max = Math.max(a, b);

        final OrientedPoint minBoundary = Double.isFinite(min) ?
                OrientedPoint.fromLocationAndDirection(min, false, precision) :
                null;

        final OrientedPoint maxBoundary = Double.isFinite(max) ?
                OrientedPoint.fromLocationAndDirection(max, true, precision) :
                null;

        if (minBoundary == null && maxBoundary == null) {
            return REALS;
        }

        return new Interval(minBoundary, maxBoundary);
    }

    /** Create a new interval from the given points. The returned interval represents
     * the region between the points, regardless of the order they are given as arguments.
     * @param a first point
     * @param b second point
     * @param precision precision context used to compare floating point numbers
     * @throw IllegalArgumentException if either point is {@link Vector1D#isNaN() NaN} or the points
     *      are both {@link Vector1D#isInfinite() infinite} and have the same sign
     */
    public static Interval of(final Vector1D a, final Vector1D b, final DoublePrecisionContext precision) {
        return of(a.getX(), b.getX(), precision);
    }

    /** Create a new interval from the given hyperplanes.
     * @param a
     * @param b
     * @return
     */
    public static Interval of(final OrientedPoint a, final OrientedPoint b) {
        // determine the ordering of the hyperplanes
        OrientedPoint minBoundary = null;
        OrientedPoint maxBoundary = null;

        if (a != null && b != null) {
            // both hyperplanes are present, so validate then against each other
            if (a.isPositiveFacing() == b.isPositiveFacing()) {
                throw new IllegalArgumentException("Invalid interval: hyperplanes have same orientation: "
                        + a + ", " + b);
            }

            if (a.classify(b.getPoint()) == HyperplaneLocation.PLUS ||
                    b.classify(a.getPoint()) == HyperplaneLocation.PLUS) {
                throw new IllegalArgumentException("Invalid interval: hyperplanes do not form interval: "
                            + a + ", " + b);
            }

            // min boundary faces -infinity, max boundary faces +infinity
            minBoundary = a.isPositiveFacing() ? b : a;
            maxBoundary = a.isPositiveFacing() ? a : b;
        }
        else if (a == null) {
            if (b == null) {
                // no boundaries; return the full number line
                return REALS;
            }

            if (b.isPositiveFacing()) {
                maxBoundary = b;
            }
            else {
                minBoundary = b;
            }
        }
        else {
            if (a.isPositiveFacing()) {
                maxBoundary = a;
            }
            else {
                minBoundary = a;
            }
        }

        // validate the boundary locations
        final double minLoc = (minBoundary != null) ? minBoundary.getLocation() : Double.NEGATIVE_INFINITY;
        final double maxLoc = (maxBoundary != null) ? maxBoundary.getLocation() : Double.POSITIVE_INFINITY;

        validateIntervalValues(minLoc, maxLoc);

        // create the interval, replacing infinites with nulls
        return new Interval(
                Double.isFinite(minLoc) ? minBoundary : null,
                Double.isFinite(maxLoc) ? maxBoundary : null);
    }

    /** Return an interval representing the entire real number line.
     * @return an interval representing the entire real number line.
     */
    public static Interval reals() {
        return REALS;
    }

    /** Validate that the given value can be used to construct an interval. The values
     * must not be NaN and if infinite, must have opposite signs.
     * @param a first value
     * @param b second value
     * @throws IllegalArgumentException if either value is NaN or if both values are infinite
     *      and have the same sign
     */
    private static void validateIntervalValues(final double a, final double b) {
        if (Double.isNaN(a) || Double.isNaN(b) ||
                (Double.isInfinite(a) && Double.compare(a, b) == 0)) {

            throw new IllegalArgumentException("Invalid interval values: [" + a + ", " + b + "]");
        }
    }
}
