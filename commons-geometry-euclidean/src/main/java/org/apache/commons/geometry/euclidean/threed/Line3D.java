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
package org.apache.commons.geometry.euclidean.threed;

import java.util.Objects;

import org.apache.commons.geometry.core.Embedding;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.internal.Equivalency;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.AffineTransformMatrix1D;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.Vector1D;

/** Class representing a line in 3D space.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class Line3D implements Embedding<Vector3D, Vector1D>, Equivalency<Line3D> {
    /** Line point closest to the origin. */
    private final Vector3D origin;

    /** Line direction. */
    private final Vector3D direction;

    /** Precision context used to compare floating point numbers. */
    private final DoublePrecisionContext precision;

    /** Simple constructor.
     * @param origin the origin of the line, meaning the point on the line closest to the origin of the
     *      3D space
     * @param direction the direction of the line
     * @param precision precision context used to compare floating point numbers
     */
    private Line3D(final Vector3D origin, final Vector3D direction, final DoublePrecisionContext precision) {
        this.origin = origin;
        this.direction = direction;
        this.precision = precision;
    }

    /** Get the line point closest to the origin.
     * @return line point closest to the origin
     */
    public Vector3D getOrigin() {
        return origin;
    }

    /** Get the normalized direction vector.
     * @return normalized direction vector
     */
    public Vector3D getDirection() {
        return direction;
    }

    /** Get the object used to determine floating point equality for this instance.
     * @return the floating point precision context for the instance
     */
    public DoublePrecisionContext getPrecision() {
        return precision;
    }

    /** Return a line containing the same points as this instance but pointing
     * in the opposite direction.
     * @return an instance containing the same points but pointing in the opposite
     *      direction
     */
    public Line3D reverse() {
        return new Line3D(origin, direction.negate(), precision);
    }

    /** Transform this instance.
     * @param transform object used to transform the instance
     * @return a transformed instance
     */
    public Line3D transform(final Transform<Vector3D> transform) {
        final Vector3D p1 = transform.apply(origin);
        final Vector3D p2 = transform.apply(origin.add(direction));

        return fromPoints(p1, p2, precision);
    }

    /** Get an object containing the current line transformed by the argument along with a
     * 1D transform that can be applied to subspace points. The subspace transform transforms
     * subspace points such that their 3D location in the transformed line is the same as their
     * 3D location in the original line after the 3D transform is applied. For example, consider
     * the code below:
     * <pre>
     *      SubspaceTransform st = line.subspaceTransform(transform);
     *
     *      Vector1D subPt = Vector1D.of(1);
     *
     *      Vector3D a = transform.apply(line.toSpace(subPt)); // transform in 3D space
     *      Vector3D b = st.getLine().toSpace(st.getTransform().apply(subPt)); // transform in 1D space
     * </pre>
     * At the end of execution, the points {@code a} (which was transformed using the original
     * 3D transform) and {@code b} (which was transformed in 1D using the subspace transform)
     * are equivalent.
     *
     * @param transform the transform to apply to this instance
     * @return an object containing the transformed line along with a transform that can be applied
     *      to subspace points
     * @see #transform(Transform)
     */
    public SubspaceTransform subspaceTransform(final Transform<Vector3D> transform) {
        final Vector3D p1 = transform.apply(origin);
        final Vector3D p2 = transform.apply(origin.add(direction));

        final Line3D tLine = fromPoints(p1, p2, precision);

        final Vector1D tSubspaceOrigin = tLine.toSubspace(p1);
        final Vector1D tSubspaceDirection = tSubspaceOrigin.vectorTo(tLine.toSubspace(p2));

        final double translation = tSubspaceOrigin.getX();
        final double scale = tSubspaceDirection.getX();

        final AffineTransformMatrix1D subspaceTransform = AffineTransformMatrix1D.of(scale, translation);

        return new SubspaceTransform(tLine, subspaceTransform);
    }

    /** Get the abscissa of the given point on the line. The abscissa represents
     * the distance the projection of the point on the line is from the line's
     * origin point (the point on the line closest to the origin of the
     * 2D space). Abscissa values increase in the direction of the line. This method
     * is exactly equivalent to {@link #toSubspace(Vector3D)} except that this method
     * returns a double instead of a {@link Vector1D}.
     * @param point point to compute the abscissa for
     * @return abscissa value of the point
     * @see #toSubspace(Vector3D)
     */
    public double abscissa(final Vector3D point) {
        return point.subtract(origin).dot(direction);
    }

    /** Get one point from the line.
     * @param abscissa desired abscissa for the point
     * @return one point belonging to the line, at specified abscissa
     */
    public Vector3D pointAt(final double abscissa) {
        return Vector3D.linearCombination(1.0, origin, abscissa, direction);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D toSubspace(Vector3D pt) {
        return Vector1D.of(abscissa(pt));
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D toSpace(Vector1D pt) {
        return toSpace(pt.getX());
    }

    /** Get the 3 dimensional point at the given abscissa position
     * on the line.
     * @param abscissa location on the line
     * @return the 3 dimensional point at the given abscissa position
     *      on the line
     */
    public Vector3D toSpace(final double abscissa) {
        return pointAt(abscissa);
    }

    /** Check if the instance is similar to another line.
     * <p>Lines are considered similar if they contain the same
     * points. This does not mean they are equal since they can have
     * opposite directions.</p>
     * @param line line to which instance should be compared
     * @return true if the lines are similar
     */
    public boolean isSimilarTo(final Line3D line) {
        final double angle = direction.angle(line.direction);
        return (precision.eqZero(angle) || precision.eq(Math.abs(angle), Math.PI)) &&
                contains(line.origin);
    }

    /** Check if the instance contains a point.
     * @param pt point to check
     * @return true if p belongs to the line
     */
    public boolean contains(final Vector3D pt) {
        return precision.eqZero(distance(pt));
    }

    /** Compute the distance between the instance and a point.
     * @param pt to check
     * @return distance between the instance and the point
     */
    public double distance(final Vector3D pt) {
        final Vector3D delta = pt.subtract(origin);
        final Vector3D orthogonal = delta.reject(direction);

        return orthogonal.norm();
    }

    /** Compute the shortest distance between the instance and another line.
     * @param line line to check against the instance
     * @return shortest distance between the instance and the line
     */
    public double distance(final Line3D line) {

        final Vector3D normal = direction.cross(line.direction);
        final double norm = normal.norm();

        if (precision.eqZero(norm)) {
            // the lines are parallel
            return distance(line.origin);
        }

        // signed separation of the two parallel planes that contains the lines
        final double offset = line.origin.subtract(origin).dot(normal) / norm;

        return Math.abs(offset);
    }

    /** Compute the point of the instance closest to another line.
     * @param line line to check against the instance
     * @return point of the instance closest to another line
     */
    public Vector3D closest(final Line3D line) {

        final double cos = direction.dot(line.direction);
        final double n = 1 - cos * cos;

        if (precision.eqZero(n)) {
            // the lines are parallel
            return origin;
        }

        final Vector3D delta = line.origin.subtract(origin);
        final double a = delta.dot(direction);
        final double b = delta.dot(line.direction);

        return Vector3D.linearCombination(1, origin, (a - (b * cos)) / n, direction);
    }

    /** Get the intersection point of the instance and another line.
     * @param line other line
     * @return intersection point of the instance and the other line
     * or null if there are no intersection points
     */
    public Vector3D intersection(final Line3D line) {
        final Vector3D closestPt = closest(line);
        return line.contains(closestPt) ? closestPt : null;
    }

    /** Return a new infinite segment representing the entire line.
     * @return a new infinite segment representing the entire line
     */
    public Segment3D span() {
        return Segment3D.fromInterval(this, Interval.full());
    }

    /** Create a new line segment from the given interval.
     * @param interval interval representing the 1D region for the line segment
     * @return a new line segment on this line
     */
    public Segment3D segment(final Interval interval) {
        return Segment3D.fromInterval(this, interval);
    }

    /** Create a new line segment from the given interval.
     * @param a first 1D location for the interval
     * @param b second 1D location for the interval
     * @return a new line segment on this line
     */
    public Segment3D segment(final double a, final double b) {
        return Segment3D.fromInterval(this, a, b);
    }

    /** Create a new line segment between the projections of the two
     * given points onto this line.
     * @param a first point
     * @param b second point
     * @return a new line segment on this line
     */
    public Segment3D segment(final Vector3D a, final Vector3D b) {
        return Segment3D.fromInterval(this, toSubspace(a), toSubspace(b));
    }

    /** Create a new line segment that starts at infinity and continues along
     * the line up to the projection of the given point.
     * @param pt point defining the end point of the line segment; the end point
     *      is equal to the projection of this point onto the line
     * @return a new, half-open line segment
     */
    public Segment3D segmentTo(final Vector3D pt) {
        return segment(Double.NEGATIVE_INFINITY, toSubspace(pt).getX());
    }

    /** Create a new line segment that starts at the projection of the given point
     * and continues in the direction of the line to infinity, similar to a ray.
     * @param pt point defining the start point of the line segment; the start point
     *      is equal to the projection of this point onto the line
     * @return a new, half-open line segment
     */
    public Segment3D segmentFrom(final Vector3D pt) {
        return segment(toSubspace(pt).getX(), Double.POSITIVE_INFINITY);
    }

    /** Create a new, empty subline based on this line.
     * @return a new, empty subline based on this line
     */
    public SubLine3D subline() {
        return new SubLine3D(this);
    }

    /**{@inheritDoc}
     *
     * <p>Instances are considered equivalent if they</p>
     * <ul>
     *   <li>contain equal {@link DoublePrecisionContext precision contexts},</li>
     *   <li>have equivalent origin locations (as evaluated by the precision context), and</li>
     *   <li>point in the same direction (as evaluated by the precision context)</li>
     * </ul>
     * @param other the point to compare with
     * @return true if this instance should be considered equivalent to the argument
     */
    @Override
    public boolean eq(final Line3D other) {
        if (this == other) {
            return true;
        }

        final DoublePrecisionContext testPrecision = getPrecision();

        return testPrecision.equals(other.getPrecision()) &&
                getOrigin().eq(other.getOrigin(), precision) &&
                getDirection().eq(other.getDirection(), precision);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(origin, direction, precision);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Line3D)) {
            return false;
        }
        Line3D other = (Line3D) obj;
        return this.origin.equals(other.origin) &&
                this.direction.equals(other.direction) &&
                this.precision.equals(other.precision);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[origin= ")
            .append(origin)
            .append(", direction= ")
            .append(direction)
            .append("]");

        return sb.toString();
    }

    /** Create a new line instance from two points that lie on the line. The line
     * direction points from the first point to the second point.
     * @param p1 first point on the line
     * @param p2 second point on the line
     * @param precision floating point precision context
     * @return a new line instance that contains both of the given point and that has
     *      a direction going from the first point to the second point
     * @throws IllegalArgumentException if the points lie too close to reate a non-zero direction vector
     */
    public static Line3D fromPoints(final Vector3D p1, final Vector3D p2,
            final DoublePrecisionContext precision) {
        return fromPointAndDirection(p1, p1.directionTo(p2), precision);
    }

    /** Create a new line instance from a point and a direction.
     * @param pt a point lying on the line
     * @param direction the direction of the line
     * @param precision floating point precision context
     * @return a new line instance that contains the given point and points in the
     *      given direction
     * @throws IllegalArgumentException if the direction cannot be normalized
     */
    public static Line3D fromPointAndDirection(final Vector3D pt, final Vector3D direction,
            final DoublePrecisionContext precision) {

        final Vector3D normDirection = direction.normalize();
        final Vector3D origin = pt.reject(normDirection);

        return new Line3D(origin, normDirection, precision);
    }

    /** Class containing a transformed line instance along with a subspace (1D) transform. The subspace
     * transform produces the equivalent of the 3D transform in 1D.
     */
    public static final class SubspaceTransform {
        /** The transformed line. */
        private final Line3D line;

        /** The subspace transform instance. */
        private final AffineTransformMatrix1D transform;

        /** Simple constructor.
         * @param line the transformed line
         * @param transform 1D transform that can be applied to subspace points
         */
        public SubspaceTransform(final Line3D line, final AffineTransformMatrix1D transform) {
            this.line = line;
            this.transform = transform;
        }

        /** Get the transformed line instance.
         * @return the transformed line instance
         */
        public Line3D getLine() {
            return line;
        }

        /** Get the 1D transform that can be applied to subspace points. This transform can be used
         * to perform the equivalent of the 3D transform in 1D space.
         * @return the subspace transform instance
         */
        public AffineTransformMatrix1D getTransform() {
            return transform;
        }
    }
}
