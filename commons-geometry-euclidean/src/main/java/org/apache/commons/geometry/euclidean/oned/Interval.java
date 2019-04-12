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

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D.RegionNode1D;

/** Class representing an interval in one dimension. The interval is defined
 * by minimum and maximum values. One or both of these values may be infinite
 * although not with the same sign.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public class Interval implements Region<Vector1D>, Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 20190210L;

    /** The minimum value of the region; may be infinite but not NaN. */
    private final double min;

    /** The maximum value of the region; may be infinite but not NaN. */
    private final double max;

    /** Precision context used to compare floating point numbers. */
    private final DoublePrecisionContext precision;

    /** Create a new instance from the given point locations. The created interval represents
     * the region between the given points, regardless of the order they are given
     * as arguments.
     * @param a first point location
     * @param b second point location
     * @param precision precision context used to compare floating point numbers
     * @throw IllegalArgumentException if either number is {@link Double#NaN} or the numbers
     *      are both infinite and with the same sign
     */
    private Interval(final double a, final double b, final DoublePrecisionContext precision) {
        if ( Double.isNaN(a) || Double.isNaN(b) ||
                (Double.isInfinite(a) && Double.compare(a, b) == 0)) {

            throw new IllegalArgumentException("Invalid interval: [" + a + ", " + b + "]");
        }

        this.min = Math.min(a, b);
        this.max = Math.max(a, b);
        this.precision = precision;
    }

    /** Get the minimum value for the interval.
     * @return the minimum value for the interval
     */
    public double getMin() {
        return min;
    }

    /** Get the maximum value for the interval.
     * @return the maximum value for the interval
     */
    public double getMax() {
        return max;
    }

    /** Get a {@link Vector1D} instance representing the minimum value of the
     * interval.
     * @return the minimum point of the interval
     */
    public Vector1D getMinPoint() {
        return Vector1D.of(min);
    }

    /** Get a {@link Vector1D} instance representing the maximum value of the
     * interval.
     * @return the maximum point of the interval
     */
    public Vector1D getMaxPoint() {
        return Vector1D.of(max);
    }

    /** True if the region is infinite, meaning that at least one of the boundary
     * values is infinite.
     * @return true if the region is infinite
     */
    public boolean isInfinite() {
        return Double.isInfinite(min) || Double.isInfinite(max);
    }

    /** Get the precision context used to determine floating point equality.
     * @return precision context for the instance
     */
    public DoublePrecisionContext getPrecision() {
        return precision;
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final Vector1D pt) {
        return classify(pt.getX());
    }

    /** Classify a point with respect to the interval. This is
     * a convenience overload of {@link #classify(Vector1D)} for
     * use in one dimension.
     * @param x the point to classify
     * @return the location of the point with respect to the interval
     * @see #classify(Vector1D)
     */
    public RegionLocation classify(final double x) {
        final int cmpMin = precision.compare(x, min);
        final int cmpMax = precision.compare(x, max);

        if (cmpMin == 0) {
            return Double.isFinite(min) ? RegionLocation.BOUNDARY : RegionLocation.INSIDE;
        }
        else if (cmpMax == 0) {
            return Double.isFinite(max) ? RegionLocation.BOUNDARY : RegionLocation.INSIDE;
        }
        else if (cmpMin > 0 && cmpMax < 0) {
            return RegionLocation.INSIDE;
        }

        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Vector1D pt) {
        return contains(pt.getX());
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
        final Vector1D transformedMinPt = transform.apply(getMinPoint());
        final Vector1D transformedMaxPt = transform.apply(getMaxPoint());

        return of(transformedMinPt, transformedMaxPt, precision);
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
        return Double.isInfinite(min) && Double.isInfinite(max);
    }

    /** Return the size of the interval.
     * @return the size of the interval
     */
    public double size() {
        return max - min;
    }

    /** Return a {@link RegionBSPTree1D} representing the same region as this instance.
     * @return a BSP tree representing the same region
     */
    public RegionBSPTree1D toTree() {
        final RegionBSPTree1D tree = new RegionBSPTree1D();

        RegionNode1D node = tree.getRoot();

        if (Double.isFinite(min)) {
            node.cut(OrientedPoint.createNegativeFacing(min, precision));

            node = node.getMinus();
        }

        if (Double.isFinite(max)) {
            node.cut(OrientedPoint.createPositiveFacing(max, precision));
        }

        return tree;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(min, max, precision);
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

        return Objects.equals(min, other.min) &&
                Objects.equals(max, other.max) &&
                Objects.equals(precision, other.precision);
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
        return new Interval(a, b, precision);
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
}
