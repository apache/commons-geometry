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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.ConvexHyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.OrientedPoint.SubOrientedPoint;

/** Class representing an interval in one dimension. The interval is defined
 * by minimum and maximum values. One or both of these values may be infinite
 * although not with the same sign.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public class Interval implements ConvexHyperplaneBoundedRegion<Vector1D>, Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 20190210L;

    /** Interval instance representing the entire real number line. */
    private static final Interval FULL = new Interval(null, null);

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

    /** True if the region is finite, meaning that both the minimum and maximum
     * boundaries exist and the region size is finite.
     * @return true if the region is finite
     */
    public boolean isFinite() {
        return !isInfinite();
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

    /** {@inheritDoc} */
    @Override
    public SubOrientedPoint trim(final ConvexSubHyperplane<Vector1D> convexSubHyperplane) {
        final SubOrientedPoint sub = (SubOrientedPoint) convexSubHyperplane;
        return contains(sub.getHyperplane().getLocation()) ? sub : null;
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

    /** {@inheritDoc}
     *
     * <p>The point is projected onto the nearest interval boundary. When a point
     * is on the inside of the interval and is equidistant from both boundaries,
     * then the minimum boundary is selected. when a point is on the outside of the
     * interval and is equidistant from both boundaries (as is the case for intervals
     * representing a single point), then the boundary facing the point is returned,
     * ensuring that the returned offset is positive.
     * </p>
     */
    @Override
    public Vector1D project(Vector1D pt) {

        OrientedPoint boundary = null;

        if (minBoundary != null && maxBoundary != null) {
            // both boundaries are present; use the closest
            double minOffset = minBoundary.offset(pt.getX());
            double maxOffset = maxBoundary.offset(pt.getX());

            double minDist = Math.abs(minOffset);
            double maxDist = Math.abs(maxOffset);

            // Project onto the max boundary if it's the closest or the point is on its plus side.
            // Otherwise, project onto the min boundary.
            if (maxDist < minDist || maxOffset > 0) {
                boundary = maxBoundary;
            }
            else {
                boundary = minBoundary;
            }
        }
        else if (minBoundary != null) {
            // only the min boundary is present
            boundary = minBoundary;
        }
        else if (maxBoundary != null) {
            // only the max boundary is present
            boundary = maxBoundary;
        }

        return (boundary != null) ? boundary.project(pt) : null;
    }

    /** Transform this instance using the given {@link Transform}.
     * @return a new transformed interval
     */
    @Override
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
     *  <p>This method always returns false since there is always at least
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

    /** {@inheritDoc}
     *
     *  <p>This method simply returns 0 because boundaries in one dimension do not
     *  have any size.</p>
     */
    @Override
    public double getBoundarySize() {
        return 0;
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

    /** {@inheritDoc}
     *
     * <p>This method simply returns a list containing this instance.</p>
     */
    @Override
    public List<Interval> toConvex() {
        return Arrays.asList(this);
    }

    /** {@inheritDoc} */
    @Override
    public Split<Interval> split(final Hyperplane<Vector1D> splitter) {
        final OrientedPoint splitOrientedPoint = (OrientedPoint) splitter;
        final Vector1D splitPoint = splitOrientedPoint.getPoint();

        final HyperplaneLocation splitterMinLoc = (minBoundary != null) ? minBoundary.classify(splitPoint) : null;
        final HyperplaneLocation splitterMaxLoc = (maxBoundary != null) ? maxBoundary.classify(splitPoint) : null;

        Interval low = null;
        Interval high = null;

        if (splitterMinLoc != HyperplaneLocation.ON || splitterMaxLoc != HyperplaneLocation.ON) {

            if (splitterMinLoc != null && splitterMinLoc != HyperplaneLocation.MINUS) {
                // splitter is on or below min boundary
                high = this;
            }
            else if (splitterMaxLoc != null && splitterMaxLoc != HyperplaneLocation.MINUS) {
                // splitter is on or above max boundary
                low = this;
            }
            else {
                // the interval is split in two
                low = new Interval(minBoundary, OrientedPoint.createPositiveFacing(
                        splitPoint, splitOrientedPoint.getPrecision()));
                high = new Interval(OrientedPoint.createNegativeFacing(
                        splitPoint, splitOrientedPoint.getPrecision()), maxBoundary);
            }
        }

        // assign minus/plus based on the orientation of the splitter
        final boolean lowIsMinus = splitOrientedPoint.isPositiveFacing();
        final Interval minus = lowIsMinus ? low : high;
        final Interval plus = lowIsMinus ? high : low;

        return new Split<>(minus, plus);
    }

    /** Return a {@link RegionBSPTree1D} representing the same region as this instance.
     * @return a BSP tree representing the same region
     * @see RegionBSPTree1D#fromInterval(Interval)
     */
    public RegionBSPTree1D toTree() {
        return RegionBSPTree1D.fromInterval(this);
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
            return FULL;
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
                return FULL;
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

    /** Return an interval with the given min value and no max.
     * @param min min value for the interval
     * @param precision precision context used to compare floating point numbers
     * @return an interval with the given min value and no max.
     */
    public static Interval min(final double min, final DoublePrecisionContext precision) {
        return of(min, Double.POSITIVE_INFINITY, precision);
    }

    /** Return an interval with the given max value and no min.
     * @param max max value for the interval
     * @param precision precision context used to compare floating point numbers
     * @return an interval with the given max value and no min.
     */
    public static Interval max(final double max, final DoublePrecisionContext precision) {
        return of(Double.NEGATIVE_INFINITY, max, precision);
    }

    /** Return an interval representing a single point at the given location.
     * @param location the location of the interval
     * @param precision precision context used to compare floating point numbers
     * @return an interval representing a single point
     */
    public static Interval point(final double location, final DoublePrecisionContext precision) {
        return of(location, location, precision);
    }

    /** Return an interval representing the entire real number line. The {@link #isFull()}
     * method of the instance will return true.
     * @return an interval representing the entire real number line
     * @see #isFull()
     */
    public static Interval full() {
        return FULL;
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
