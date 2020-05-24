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
package org.apache.commons.geometry.euclidean;

import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Base class representing an axis-aligned bounding box with minimum and maximum bounding points.
 * @param <P> Point implementation type
 * @param <B> Bounds implementation type
 */
public abstract class AbstractBounds<
    P extends EuclideanVector<P>,
    B extends AbstractBounds<P, B>> {

    /** Minimum point. */
    private final P min;

    /** Maximum point. */
    private final P max;

    /** Simple constructor. Callers are responsible for ensuring that all coordinate values are finite and
     * that all values in {@code min} are less than or equal to their corresponding values in {@code max}.
     * No validation is performed.
     * @param min minimum point
     * @param max maximum point
     */
    protected AbstractBounds(final P min, final P max) {
        this.min = min;
        this.max = max;
    }

    /** Get the minimum point.
     * @return the minimum point
     */
    public P getMin() {
        return min;
    }

    /** Get the maximum point.
     * @return the maximum point
     */
    public P getMax() {
        return max;
    }

    /** Get the diagonal of the bounding box. The return value is a vector pointing from
     * {@code min} to {@code max} and contains the size of the box along each coordinate axis.
     * @return the diagonal vector of the bounding box
     */
    public P getDiagonal() {
        return min.vectorTo(max);
    }

    /** Return the center of the bounding box.
     * @return the center of the bounding box
     */
    public P getBarycenter() {
        return min.lerp(max, 0.5);
    }

    /** Return true if the bounding box has non-zero size along each coordinate axis, as
     * evaluated by the given precision context.
     * @param precision precision context used for floating point comparisons
     * @return true if the bounding box has non-zero size along each coordinate axis
     */
    public abstract boolean hasSize(DoublePrecisionContext precision);

    /** Return true if the given point is strictly within or on the boundary of the bounding box.
     * In other words, true if returned if <code>p<sub>t</sub> &gt;= min<sub>t</sub></code> and
     * <code>p<sub>t</sub> &lt;= max<sub>t</sub></code> for each coordinate value <code>t</code>.
     * Floating point comparisons are strict; values are considered equal only if they match exactly.
     * @param pt the point to check
     * @return true if the given point is strictly within or on the boundary of the instance
     * @see #contains(EuclideanVector, DoublePrecisionContext)
     */
    public abstract boolean contains(P pt);

    /** Return true if the given point is within or on the boundary of the bounding box, using the given
     * precision context for floating point comparisons. This is similar to {@link #contains(EuclideanVector)}
     * but allows points that may be strictly outside of the box due to floating point errors to be considered
     * inside.
     * @param pt the point to check
     * @param precision precision context used to compare floating point values
     * @return if the given point is within or on the boundary of the bounds, as determined
     *      by the given precision context
     * @see #contains(EuclideanVector, DoublePrecisionContext)
     */
    public abstract boolean contains(P pt, DoublePrecisionContext precision);

    /** Return true if any point on the interior or boundary of this instance is also considered to be
     * on the interior or boundary of the argument. Specifically, true is returned if
     * <code>aMin<sub>t</sub> &lt;= bMax<sub>t</sub></code> and <code>aMax<sub>t</sub> &gt;= bMin<sub>t</sub></code>
     * for all coordinate values {@code t}, where {@code a} is the current instance and {@code b} is the argument.
     * Floating point comparisons are strict; values are considered equal only if they match exactly.
     * @param other bounding box to intersect with
     * @return true if the bounds intersect
     */
    public abstract boolean intersects(B other);

    /** Return the intersection of this bounding box and the argument, or null if no intersection exists.
     * Floating point comparisons are strict; values are considered equal only if they match exactly. Note
     * this this method may return bounding boxes with zero size in one or more coordinate axes.
     * @param other bounding box to intersect with
     * @return the intersection of this instance and the argument, or null if no such intersection
     *      exists
     * @see #intersects(AbstractBounds)
     */
    public abstract B intersection(B other);

    /** Return a hyperplane-bounded region containing the same points as this instance.
     * @param precision precision context used for floating point comparisons in the returned
     *      region instance
     * @return a hyperplane-bounded region containing the same points as this instance
     */
    public abstract HyperplaneBoundedRegion<P> toRegion(DoublePrecisionContext precision);

    /** Return true if the current instance and argument are considered equal as evaluated by the
     * given precision context. Bounds are considered equal if they contain equivalent min and max
     * points.
     * @param other bounds to compare with
     * @param precision precision context to compare floating point numbers
     * @return true if this instance is equivalent to the argument, as evaluated by the given
     *      precision context
     * @see EuclideanVector#eq(EuclideanVector, DoublePrecisionContext)
     */
    public boolean eq(final B other, final DoublePrecisionContext precision) {
        return min.eq(other.getMin(), precision) &&
                max.eq(other.getMax(), precision);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[min= ")
            .append(min)
            .append(", max= ")
            .append(max)
            .append(']');

        return sb.toString();
    }
}
