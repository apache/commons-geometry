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

    /** {@link OrientedPoint} instance representing the min boundary of the interval.
     * This instance will be negative-facing. Infinite values are allowed but not NaN.
     */
    private final OrientedPoint minHyperplane;

    /** {@link OrientedPoint} instance representing the max boundary of the interval
     * This instance will be negative-facing. Infinite values are allowed but not NaN.
     */
    private final OrientedPoint maxHyperplane;

    /** Create an instance from min and max bounding hyperplanes. No validation is performed.
     * Callers are responsible for ensuring that the given hyperplanes represent a valid
     * interval.
     * @param minHyperplane the min (negative-facing) hyperplane
     * @param maxHyperplane the max (positive-facing) hyperplane
     */
    private Interval(final OrientedPoint minHyperplane, final OrientedPoint maxHyperplane) {
        this.minHyperplane = minHyperplane;
        this.maxHyperplane = maxHyperplane;
    }

    /** Get the minimum value for the interval.
     * @return the minimum value for the interval
     */
    public double getMin() {
        return minHyperplane.getPoint().getX();
    }

    /** Get the maximum value for the interval.
     * @return the maximum value for the interval
     */
    public double getMax() {
        return maxHyperplane.getPoint().getX();
    }

    /** Get a {@link Vector1D} instance representing the minimum value of the
     * interval.
     * @return the minimum point of the interval
     */
    public Vector1D getMinPoint() {
        return minHyperplane.getPoint();
    }

    /** Get a {@link Vector1D} instance representing the maximum value of the
     * interval.
     * @return the maximum point of the interval
     */
    public Vector1D getMaxPoint() {
        return maxHyperplane.getPoint();
    }

    /**
     * Get the {@link OrientedPoint} forming the minimum bounding hyperplane
     * of the interval. This hyperplane is oriented to point in the negative direction.
     * @return the min hyperplane of the interval
     */
    public OrientedPoint getMinHyperplane() {
        return minHyperplane;
    }

    /**
     * Get the {@link OrientedPoint} forming the maximum bounding hyperplane
     * of the interval. This hyperplane is oriented to point in the positive direction.
     * @return the max hyperplane of the interval
     */
    public OrientedPoint getMaxHyperplane() {
        return maxHyperplane;
    }

    /** True if the region is infinite, meaning that at least one of the boundary
     * values is infinite.
     * @return true if the region is infinite
     */
    public boolean isInfinite() {
        return minHyperplane.getPoint().isInfinite() ||
                maxHyperplane.getPoint().isInfinite();
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final Vector1D pt) {
        final RegionLocation minLoc = classifyWithBoundary(minHyperplane, pt);

        if (minLoc == RegionLocation.INSIDE) {
            return classifyWithBoundary(maxHyperplane, pt);
        }

        return minLoc;
    }

    private RegionLocation classifyWithBoundary(final OrientedPoint hyperplane, final Vector1D pt) {
        final double hyperx = hyperplane.getPoint().getX();
        final double ptx = pt.getX();

        if (Double.isInfinite(hyperx) && Double.compare(hyperx, ptx) == 0) {
            return RegionLocation.INSIDE;
        }

        HyperplaneLocation loc = hyperplane.classify(pt);

        if (loc == HyperplaneLocation.ON) {
            return RegionLocation.BOUNDARY;
        }
        else if (loc == HyperplaneLocation.PLUS) {
            return RegionLocation.OUTSIDE;
        }
        return RegionLocation.INSIDE;
    }

    /** Classify a point with respect to the interval. This is
     * a convenience overload of {@link #classify(Vector1D)} for
     * use in one dimension.
     * @param x the point to classify
     * @return the location of the point with respect to the interval
     * @see #classify(Vector1D)
     */
    public RegionLocation classify(final double x) {
        return classify(Vector1D.of(x));
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
        final OrientedPoint transformedMin = minHyperplane.transform(transform);
        final OrientedPoint transformedMax = maxHyperplane.transform(transform);

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
        return getMinPoint().isInfinite() &&
                getMaxPoint().isInfinite();
    }

    /** {@inheritDoc} */
    @Override
    public double size() {
        return getMax() - getMin();
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D barycenter() {
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

        if (!minHyperplane.getPoint().isInfinite()) {
            node.cut(minHyperplane);

            node = node.getMinus();
        }

        if (!maxHyperplane.getPoint().isInfinite()) {
            node.cut(maxHyperplane);
        }

        return tree;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(minHyperplane, maxHyperplane);
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

        return Objects.equals(minHyperplane, other.minHyperplane) &&
                Objects.equals(maxHyperplane, other.maxHyperplane);
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

        return of(OrientedPoint.fromPointAndDirection(min, false, precision),
                OrientedPoint.fromPointAndDirection(max, true, precision));
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

    public static Interval of(final OrientedPoint a, final OrientedPoint b) {
        validateIntervalHyperplanes(a, b);

        final OrientedPoint min = a.isPositiveFacing() ? b : a;
        final OrientedPoint max = a.isPositiveFacing() ? a : b;

        return new Interval(min, max);
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

    /** Validate that the given hyperplanes can be used to create an interval. The locations of
     * the hyperplanes must meet the criteria of {@link #validateIntervalValues(double, double)},
     * the hyperplanes must point in opposite directions, and neither hyperplane can be on the
     * plus side of the other.
     * @param a first hyperplane
     * @param b second hyperplane
     * @throws IllegalArgumentException if (1) either value is NaN, (2) both values are infinite
     *      and have the same sign, (3) the hyperplanes point in the same direction, or (4) one hyperplane
     *      is on the plus side of the other
     */
    private static void validateIntervalHyperplanes(final OrientedPoint a, final OrientedPoint b) {
        validateIntervalValues(a.getPoint().getX(), b.getPoint().getX());

        if (a.isPositiveFacing() == b.isPositiveFacing()) {
            throw new IllegalArgumentException("Invalid interval: hyperplanes have same orientation: "
                        + a + ", " + b);
        }

        if (a.classify(b.getPoint()) == HyperplaneLocation.PLUS ||
                b.classify(a.getPoint()) == HyperplaneLocation.PLUS) {
            throw new IllegalArgumentException("Invalid interval: hyperplanes do not form interval: "
                        + a + ", " + b);
        }
    }
}
