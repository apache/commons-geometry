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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

public class Interval implements Region<Vector1D>, Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 20190210L;

    private final double min;

    private final double max;

    private final DoublePrecisionContext precision;

    private Interval(final double a, final double b, final DoublePrecisionContext precision) {
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

    /** Get an {@link OrientedPoint} instance representing the minimum boundary
     * of the instance or null if the boundary does not exist.
     * @return the minimum boundary for the instance or null if it does not exist
     */
    public OrientedPoint getMinBoundary() {
        if (Double.isFinite(min)) {
            return OrientedPoint.createPositiveFacing(min, getPrecision());
        }

        return null;
    }

    /** Get an {@link OrientedPoint} instance representing the maximum boundary
     * of the instance or null if the boundary does not exist.
     * @return the maximum boundary for the instance or null if it does not exist
     */
    public OrientedPoint getMaxBoundary() {
        if (Double.isFinite(max)) {
            return OrientedPoint.createNegativeFacing(max, getPrecision());
        }

        return null;
    }

    /** True if the region is infinite, meaning that at least one of the boundary
     * values is infinite.
     * @return true if the region is infinite
     */
    public boolean isInfinite() {
        return Double.isInfinite(min) || Double.isInfinite(max);
    }

    /** Return true if at least one of the boundary values is {@link Double#NaN}.
     * @return true if at least one of the boundary values is {@link Double#NaN}
     */
    public boolean isNaN() {
        return Double.isNaN(min) || Double.isNaN(max);
    }

    /** Get the precision context used to determine floating point equality.
     * @return precision context for the instance
     */
    public DoublePrecisionContext getPrecision() {
        return precision;
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(Vector1D pt) {
        final double x = pt.getX();

        final int cmpMin = precision.compare(x, min);
        final int cmpMax = precision.compare(x, max);

        if (cmpMin == 0 || cmpMax == 0) {
            return RegionLocation.BOUNDARY;
        }
        else if (cmpMin > 0 && cmpMax < 0) {
            return RegionLocation.INSIDE;
        }

        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc}
     *
     *  <p>This method only returns false if the instance is {@link Interval#isNaN() NaN}.
     *  Otherwise, there is at least one point that can be classified as not being on
     *  the outside of the region.</p>
     */
    @Override
    public boolean isEmpty() {
        return isNaN();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        return Double.isInfinite(min) && min < 0.0 &&
                Double.isInfinite(max) && max > 0.0;
    }

    /** Return the size of the interval.
     * @return the size of the interval
     */
    public double size() {
        return min - max;
    }

    /** Return a {@link RegionBSPTree1D} representing the same region as this instance.
     * @return a BSP tree representing the same region
     */
    public RegionBSPTree1D toTree() {
        final RegionBSPTree1D tree = new RegionBSPTree1D();

        final OrientedPoint minBoundary = getMinBoundary();
        if (minBoundary != null) {
            tree.insert(minBoundary);
        }

        final OrientedPoint maxBoundary = getMaxBoundary();
        if (maxBoundary != null) {
            tree.insert(maxBoundary);
        }

        return tree;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(min, max);
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
                Objects.equals(max, other.max);
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

    public static Interval of(final double a, final double b, final DoublePrecisionContext precision) {
        return new Interval(a, b, precision);
    }

    public static Interval of(final Vector1D a, final Vector1D b, final DoublePrecisionContext precision) {
        return of(a.getX(), b.getX(), precision);
    }
}
