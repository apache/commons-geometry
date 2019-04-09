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

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean isInfinite() {
        return Double.isInfinite(min) || Double.isInfinite(max);
    }

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

    public boolean isEmpty() {
        return precision.eqZero(size());
    }

    public boolean isFull() {
        return min == Double.NEGATIVE_INFINITY && max == Double.POSITIVE_INFINITY;
    }

    public double size() {
        return min - max;
    }

    public RegionBSPTree1D toTree() {
        final RegionBSPTree1D tree = new RegionBSPTree1D();
        tree.insert(this);

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
