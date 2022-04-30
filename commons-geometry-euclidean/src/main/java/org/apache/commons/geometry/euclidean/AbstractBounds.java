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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;

import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.numbers.core.Precision;

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

    /** Get the centroid, or geometric center, of the bounding box.
     * @return the centroid of the bounding box
     */
    public P getCentroid() {
        return min.lerp(max, 0.5);
    }

    /** Return true if the bounding box has non-zero size along each coordinate axis, as
     * evaluated by the given precision context.
     * @param precision precision context used for floating point comparisons
     * @return true if the bounding box has non-zero size along each coordinate axis
     */
    public abstract boolean hasSize(Precision.DoubleEquivalence precision);

    /** Return true if the given point is strictly within or on the boundary of the bounding box.
     * In other words, true if returned if <code>p<sub>t</sub> &gt;= min<sub>t</sub></code> and
     * <code>p<sub>t</sub> &lt;= max<sub>t</sub></code> for each coordinate value <code>t</code>.
     * Floating point comparisons are strict; values are considered equal only if they match exactly.
     * @param pt the point to check
     * @return true if the given point is strictly within or on the boundary of the instance
     * @see #contains(EuclideanVector, Precision.DoubleEquivalence)
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
     * @see #contains(EuclideanVector, Precision.DoubleEquivalence)
     */
    public abstract boolean contains(P pt, Precision.DoubleEquivalence precision);

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
    public abstract HyperplaneBoundedRegion<P> toRegion(Precision.DoubleEquivalence precision);

    /** Return true if the current instance and argument are considered equal as evaluated by the
     * given precision context. Bounds are considered equal if they contain equivalent min and max
     * points.
     * @param other bounds to compare with
     * @param precision precision context to compare floating point numbers
     * @return true if this instance is equivalent to the argument, as evaluated by the given
     *      precision context
     * @see EuclideanVector#eq(EuclideanVector, Precision.DoubleEquivalence)
     */
    public boolean eq(final B other, final Precision.DoubleEquivalence precision) {
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

    /** Abstract internal class used to perform line convex subset intersection operations using the
     * <a href="https://education.siggraph.org/static/HyperGraph/raytrace/rtinter3.htm">slabs algorithm</a>.
     * Subclasses are expected to reference a line convex subset in their target dimension that is being
     * evaluated against the bounding box. Access to the line and subset properties is facilitated through
     * abstract methods.
     * @param <S> Line segment type
     * @param <I> Boundary intersection type
     */
    protected abstract class BoundsLinecaster<S, I> {

        /** Precision used for floating point comparisons. */
        private final Precision.DoubleEquivalence precision;

        /** Near slab intersection abscissa value. */
        private double near = Double.NEGATIVE_INFINITY;

        /** Far slab intersection abscissa value. */
        private double far = Double.POSITIVE_INFINITY;

        /** Construct a new instance that uses the given precision instance for floating
         * point comparisons.
         * @param precision precision instance for floating point comparisons
         */
        protected BoundsLinecaster(final Precision.DoubleEquivalence precision) {
            this.precision = precision;
        }

        /** Return {@code true} if the line convex subset shares any points with the
         * bounding box.
         * @return {@code true} if the line convex subset shares any points with the
         *      bounding box
         */
        public boolean intersectsRegion() {
            return computeNearFar() &&
                    precision.gte(getSubspaceEnd(), near) &&
                    precision.lte(getSubspaceStart(), far);
        }

        /** Get the segment containing all points shared by the line convex
         * subset and the bounding box, or {@code null} if no points are shared.
         * @return segment containing all points shared by the line convex
         *      subset and the bounding box, or {@code null} if no points are shared.
         */
        public S getRegionIntersection() {
            if (intersectsRegion()) {
                final double start = Math.max(near, getSubspaceStart());
                final double end = Math.min(far, getSubspaceEnd());

                return createSegment(start, end);
            }
            return null;
        }

        /** Get the intersections between the line convex subset and the boundaries of the
         * bounding box. An empty list is returned if there are no intersections.
         * @return intersections between the line convex subset and the boundaries of the
         *      bounding box
         */
        public List<I> getBoundaryIntersections() {
            if (computeNearFar()) {
                final int maxSize = min.getDimension() * 2;
                final List<I> results = new ArrayList<>(maxSize);

                addBoundaryIntersections(near, results);
                if (!precision.eq(near, far)) {
                    addBoundaryIntersections(far, results);
                }

                results.sort(getBoundaryIntersectionComparator());

                return results;
            }

            return Collections.emptyList();
        }

        /** Get an object representing the <em>first</em> intersection of the line convex subset
         * with the boundaries of the bounding box. Null is returned if no such intersection exists.
         * @return object representing the first intersection of the line convex subset with the
         *      boundaries of the bounding box, or {@code null} if no such intersection exists
         */
        public I getFirstBoundaryIntersection() {
            final List<I> results = getBoundaryIntersections();
            return results.isEmpty() ?
                    null :
                    results.get(0);
        }

        /** Add a boundary intersection to {@code results} if the given point lies on
         * one of the bounding box boundaries orthogonal to {@code dimPlusDir}.
         * @param pt potential intersection point
         * @param dimMinusDir minus direction for the dimension being evaluated
         * @param dimPlusDir plus direction for the dimension being evaluated
         * @param coordinateFn function used to access point coordinate values for
         *      the dimension being evaluated
         * @param results list containing intersection results
         */
        protected void addBoundaryIntersectionIfPresent(
                final P pt,
                final P dimMinusDir,
                final P dimPlusDir,
                final ToDoubleFunction<P> coordinateFn,
                final List<I> results) {

            // only include results for dimensions that are not considered
            // parallel to the line, according to the precision
            if (!precision.eqZero(getLineDir().dot(dimPlusDir))) {
                final double coordinate = coordinateFn.applyAsDouble(pt);
                final double dimMin = coordinateFn.applyAsDouble(min);
                final double dimMax = coordinateFn.applyAsDouble(max);

                if (precision.eq(coordinate, dimMin)) {
                    results.add(createBoundaryIntersection(pt, dimMinusDir));
                }

                if (precision.eq(coordinate, dimMax)) {
                    results.add(createBoundaryIntersection(pt, dimPlusDir));
                }
            }
        }

        /** Update the {@code near} and {@code far} slab intersection points with the
         * intersection values for the coordinates returned by {@code coordinateFn}, returning
         * {@code false} if the line is determined to not intersect the bounding box.
         * @param coordinateFn function returning the coordinate for the dimension
         *      being evaluated
         * @return {@code false} if the line is determined to not intersect the bounding
         *      box
         */
        protected boolean updateNearFar(final ToDoubleFunction<P> coordinateFn) {
            final double dir = coordinateFn.applyAsDouble(getLineDir());
            final double origin = coordinateFn.applyAsDouble(getLineOrigin());

            final double minCoord = coordinateFn.applyAsDouble(min);
            final double maxCoord = coordinateFn.applyAsDouble(max);

            double t1 = (minCoord - origin) / dir;
            double t2 = (maxCoord - origin) / dir;

            if (!Double.isFinite(t1) || !Double.isFinite(t2)) {
                // the line is parallel to this dimension; only continue if the
                // line origin lies between the min and max for this dimension
                return precision.gte(origin, minCoord) && precision.lte(origin, maxCoord);
            }

            if (t1 > t2) {
                final double temp = t1;
                t1 = t2;
                t2 = temp;
            }

            if (t1 > near) {
                near = t1;
            }

            if (t2 < far) {
                far = t2;
            }

            return precision.lte(near, far);
        }

        /** Create a line segment with the given start and end abscissas.
         * @param startAbscissa start abscissa
         * @param endAbscissa end abscissa
         * @return line segment with the given start and end abscissas
         */
        protected abstract S createSegment(double startAbscissa, double endAbscissa);

        /** Construct a new boundary intersection instance.
         * @param pt boundary intersection point
         * @param normal boundary normal at the intersection
         * @return a new boundary intersection instance
         */
        protected abstract I createBoundaryIntersection(P pt, P normal);

        /** Add all boundary intersections at the given line abscissa value to {@code results}.
         * Subclasses should call {@link #addBoundaryIntersectionIfPresent} for each dimension
         * in the target space.
         * @param abscissa intersection abscissa
         * @param results boundary intersection result list
         */
        protected abstract void addBoundaryIntersections(double abscissa, List<I> results);

        /** Get the comparator used to produce a standardized ordering of boundary intersection
         * results.
         * @return comparator used to store boundary intersections
         */
        protected abstract Comparator<I> getBoundaryIntersectionComparator();

        /** Compute the {@code near} and {@code far} slab intersection values for the
         * line under test, returning {@code true} if the line intersects the bounding
         * box. This method should call {@link #updateNearFar(ToDoubleFunction)} for each
         * dimension in the space.
         * @return {@code true} if the line intersects the bounding box
         */
        protected abstract boolean computeNearFar();

        /** Get the line direction.
         * @return line direction
         */
        protected abstract P getLineDir();

        /** Get the line origin.
         * @return line origin
         */
        protected abstract P getLineOrigin();

        /** Get the line convex subset start abscissa.
         * @return line convex subset start abscissa
         */
        protected abstract double getSubspaceStart();

        /** Get the line convex subset end abscissa.
         * @return line convex subset end abscissa
         */
        protected abstract double getSubspaceEnd();
    }
}
