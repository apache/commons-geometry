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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleBiFunction;

import org.apache.commons.geometry.core.Embedding;
import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.core.Precision;

/** Abstract base class representing an n-sphere, which is a generalization of the ordinary 3-dimensional
 * sphere to arbitrary dimensions.
 * @param <V> Vector implementation type
 * @see <a href="https://wikipedia.org/wiki/N-sphere">N-sphere</a>
 */
public abstract class AbstractNSphere<V extends EuclideanVector<V>> implements Region<V> {

    /** The center point of the n-sphere. */
    private final V center;

    /** The radius of the n-sphere. */
    private final double radius;

    /** Precision object used to perform floating point comparisons. */
    private final Precision.DoubleEquivalence precision;

    /** Construct a new instance from its component parts.
     * @param center the center point of the n-sphere
     * @param radius the radius of the n-sphere
     * @param precision precision context used to perform floating point comparisons
     * @throws IllegalArgumentException if center is not finite or radius is not finite or is
     *      less than or equal to zero as evaluated by the given precision context
     */
    protected AbstractNSphere(final V center, final double radius, final Precision.DoubleEquivalence precision) {
        this(precision, checkCenter(center), checkRadius(radius, precision));
    }

    /**
     * Private constructor.
     * @param precision precision context used to perform floating point comparisons
     * @param center the center point of the n-sphere
     * @param radius the radius of the n-sphere
     */
    private AbstractNSphere(final Precision.DoubleEquivalence precision, final V center, final double radius) {
        this.center = center;
        this.radius = radius;
        this.precision = precision;
    }

    /**
     * Check the center.
     *
     * <p>This method exists to raise an exception before invocation of the
     * private constructor; this mitigates Finalizer attacks
     * (see SpotBugs CT_CONSTRUCTOR_THROW).
     *
     * @param <V> Vector implementation type
     * @param center the center point of the n-sphere
     * @return the center
     * @throws IllegalArgumentException if center is not finite
     */
    private static <V extends EuclideanVector<V>> V checkCenter(final V center) {
        if (!center.isFinite()) {
            throw new IllegalArgumentException("Illegal center point: " + center);
        }
        return center;
    }

    /**
     * Check the radius.
     *
     * <p>This method exists to raise an exception before invocation of the
     * private constructor; this mitigates Finalizer attacks
     * (see SpotBugs CT_CONSTRUCTOR_THROW).
     *
     * @param radius the radius of the n-sphere
     * @param precision precision context used to perform floating point comparisons
     * @return the radius
     * @throws IllegalArgumentException if radius is not finite or is
     *      less than or equal to zero as evaluated by the given precision context
     */
    private static double checkRadius(final double radius, final Precision.DoubleEquivalence precision) {
        if (!Double.isFinite(radius) || precision.lte(radius, 0.0)) {
            throw new IllegalArgumentException("Illegal radius: " + radius);
        }
        return radius;
    }

    /** Get the center point of the n-sphere.
     * @return the center of the n-sphere
     */
    public V getCenter() {
        return center;
    }

    /** Get the radius of the n-sphere.
     * @return the radius of the n-sphere.
     */
    public double getRadius() {
        return radius;
    }

    /** Get the precision object used to perform floating point
     * comparisons for this instance.
     * @return the precision object for this instance
     */
    public Precision.DoubleEquivalence getPrecision() {
        return precision;
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
    * <p>This method always returns {@code false}.</p>
    */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc}
     *
     * <p>This method is an alias for {@link #getCenter()}.</p>
     */
    @Override
    public V getCentroid() {
        return getCenter();
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final V pt) {
        final double dist = ((Point<V>) center).distance(pt);
        final int cmp = precision.compare(dist, radius);
        if (cmp < 0) {
            return RegionLocation.INSIDE;
        } else if (cmp > 0) {
            return RegionLocation.OUTSIDE;
        }
        return RegionLocation.BOUNDARY;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(center, radius, precision);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || !obj.getClass().equals(this.getClass())) {
            return false;
        }

        final AbstractNSphere<?> other = (AbstractNSphere<?>) obj;

        return Objects.equals(this.center, other.center) &&
                Double.compare(this.radius, other.radius) == 0 &&
                Objects.equals(this.getPrecision(), other.getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(30);
        sb.append(this.getClass().getSimpleName())
            .append("[center= ")
            .append(center)
            .append(", radius= ")
            .append(radius)
            .append(']');

        return sb.toString();
    }

    /** Project the given point to the boundary of the n-sphere. If
     * the given point is exactly equal to the n-sphere center, it is
     * projected to the boundary in the direction of {@code defaultVector}.
     * @param pt the point to project
     * @param defaultVector the direction to project the point if it lies
     *      exactly at the center of the n-sphere
     * @return the projected point
     */
    protected V project(final V pt, final V defaultVector) {
        V vec = center.vectorTo(pt);
        if (vec.equals(vec.getZero())) {
            // use the default project vector if the given point lies
            // exactly_ on the center point
            vec = defaultVector;
        }

        return vec.withNorm(radius).add(center);
    }

    /** Internal method to compute the intersections between a line and this instance. The returned list will
     * contain either 0, 1, or 2 points.
     * <ul>
     *      <li><strong>2 points</strong> - The line is a secant line and intersects the n-sphere at two
     *      distinct points. The points are ordered such that the first point in the list is the first point
     *      encountered when traveling in the direction of the line. (In other words, the points are ordered
     *      by increasing abscissa value.)
     *      </li>
     *      <li><strong>1 point</strong> - The line is a tangent line and only intersects the n-sphere at a
     *      single point (as evaluated by the n-sphere's precision context).
     *      </li>
     *      <li><strong>0 points</strong> - The line does not intersect the n-sphere.</li>
     * </ul>
     * @param <L> Line implementation type
     * @param line line to intersect with the n-sphere
     * @param abscissaFn function used to compute the abscissa value of a point on a line
     * @param distanceFn function used to compute the smallest distance between a point
     *      and a line
     * @return a list of intersection points between the given line and this n-sphere
     */
    protected <L extends Embedding<V, Vector1D>> List<V> intersections(final L line,
            final ToDoubleBiFunction<L, V> abscissaFn, final ToDoubleBiFunction<L, V> distanceFn) {

        final double dist = distanceFn.applyAsDouble(line, center);

        final int cmp = precision.compare(dist, radius);
        if (cmp <= 0) {
            // on the boundary or inside the n-sphere
            final double abscissa = abscissaFn.applyAsDouble(line, center);
            final double abscissaDelta = Math.sqrt((radius * radius) - (dist * dist));

            final V p0 = line.toSpace(Vector1D.of(abscissa - abscissaDelta));
            if (cmp < 0) {
                // secant line => two intersections
                final V p1 = line.toSpace(Vector1D.of(abscissa + abscissaDelta));

                return Arrays.asList(p0, p1);
            }

            // tangent line => one intersection
            return Collections.singletonList(p0);
        }

        // no intersections
        return Collections.emptyList();
    }

    /** Internal method to compute the first intersection between a line and this instance.
     * @param <L> Line implementation type
     * @param line line to intersect with the n-sphere
     * @param abscissaFn function used to compute the abscissa value of a point on a line
     * @param distanceFn function used to compute the smallest distance between a point
     *      and a line
     * @return the first intersection between the given line and this instance or {@code null} if
     *      no such intersection exists
     */
    protected <L extends Embedding<V, Vector1D>> V firstIntersection(final L line,
            final ToDoubleBiFunction<L, V> abscissaFn, final ToDoubleBiFunction<L, V> distanceFn) {

        final double dist = distanceFn.applyAsDouble(line, center);

        final int cmp = precision.compare(dist, radius);
        if (cmp <= 0) {
            // on the boundary or inside the n-sphere
            final double abscissa = abscissaFn.applyAsDouble(line, center);
            final double abscissaDelta = Math.sqrt((radius * radius) - (dist * dist));

            return line.toSpace(Vector1D.of(abscissa - abscissaDelta));
        }

        return null;
    }
}
