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
package org.apache.commons.geometry.euclidean.twod;

import java.text.MessageFormat;
import java.util.Objects;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.AbstractHyperplane;
import org.apache.commons.geometry.core.partitioning.EmbeddingHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.euclidean.oned.AffineTransformMatrix1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.LinearCombination;
import org.apache.commons.numbers.core.Precision;

/** This class represents an oriented line in the 2D plane.

 * <p>An oriented line can be defined either by extending a line
 * segment between two points past these points, by specifying a
 * point and a direction, or by specifying a point and an angle
 * relative to the x-axis.</p>

 * <p>Since the line oriented, the two half planes on its sides are
 * unambiguously identified as the left half plane and the right half
 * plane. This can be used to identify the interior and the exterior
 * in a simple way when a line is used to define a portion of a polygon
 * boundary.</p>

 * <p>A line can also be used to completely define a reference frame
 * in the plane. It is sufficient to select one specific point in the
 * line (the orthogonal projection of the original reference frame on
 * the line) and to use the unit vector in the line direction (see
 * {@link #getDirection()} and the orthogonal vector oriented from the
 * left half plane to the right half plane (see {@link #getOffsetDirection()}.
 * We define two coordinates by the process, the <em>abscissa</em> along
 * the line, and the <em>offset</em> across the line. All points of the
 * plane are uniquely identified by these two coordinates. The line is
 * the set of points at zero offset, the left half plane is the set of
 * points with negative offsets and the right half plane is the set of
 * points with positive offsets.</p>
 * @see Lines
 */
public final class Line extends AbstractHyperplane<Vector2D>
    implements EmbeddingHyperplane<Vector2D, Vector1D> {

    /** Format string for creating line string representations. */
    static final String TO_STRING_FORMAT = "{0}[origin= {1}, direction= {2}]";

    /** The direction of the line as a normalized vector. */
    private final Vector2D.Unit direction;

    /** The distance between the origin and the line. */
    private final double originOffset;

    /** Simple constructor.
     * @param direction The direction of the line.
     * @param originOffset The signed distance between the line and the origin.
     * @param precision Precision context used to compare floating point numbers.
     */
    Line(final Vector2D.Unit direction, final double originOffset, final Precision.DoubleEquivalence precision) {
        super(precision);

        this.direction = direction;
        this.originOffset = originOffset;
    }

    /** Get the angle of the line in radians with respect to the abscissa (+x) axis. The
     * returned angle is in the range {@code [0, 2pi)}.
     * @return the angle of the line with respect to the abscissa (+x) axis in the range
     *      {@code [0, 2pi)}
     */
    public double getAngle() {
        final double angle = Math.atan2(direction.getY(), direction.getX());
        return Angle.Rad.WITHIN_0_AND_2PI.applyAsDouble(angle);
    }

    /** Get the direction of the line.
     * @return the direction of the line
     */
    public Vector2D.Unit getDirection() {
        return direction;
    }

    /** Get the offset direction of the line. This vector is perpendicular to the
     * line and points in the direction of positive offset values, meaning that
     * it points from the left side of the line to the right when one is looking
     * along the line direction.
     * @return the offset direction of the line.
     */
    public Vector2D getOffsetDirection() {
        return Vector2D.of(direction.getY(), -direction.getX());
    }

    /** Get the line origin point. This is the projection of the 2D origin
     * onto the line and also serves as the origin for the 1D embedded subspace.
     * @return the origin point of the line
     */
    public Vector2D getOrigin() {
        return toSpace(Vector1D.ZERO);
    }

    /** Get the signed distance from the origin of the 2D space to the
     * closest point on the line.
     * @return the signed distance from the origin to the line
     */
    public double getOriginOffset() {
        return originOffset;
    }

    /** {@inheritDoc} */
    @Override
    public Line reverse() {
        return new Line(direction.negate(), -originOffset, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public Line transform(final Transform<Vector2D> transform) {
        final Vector2D origin = getOrigin();

        final Vector2D tOrigin = transform.apply(origin);
        final Vector2D tOriginPlusDir = transform.apply(origin.add(getDirection()));

        return Lines.fromPoints(tOrigin, tOriginPlusDir, getPrecision());
    }

    /** Get an object containing the current line transformed by the argument along with a
     * 1D transform that can be applied to subspace points. The subspace transform transforms
     * subspace points such that their 2D location in the transformed line is the same as their
     * 2D location in the original line after the 2D transform is applied. For example, consider
     * the code below:
     * <pre>
     *      SubspaceTransform st = line.subspaceTransform(transform);
     *
     *      Vector1D subPt = Vector1D.of(1);
     *
     *      Vector2D a = transform.apply(line.toSpace(subPt)); // transform in 2D space
     *      Vector2D b = st.getLine().toSpace(st.getTransform().apply(subPt)); // transform in 1D space
     * </pre>
     * At the end of execution, the points {@code a} (which was transformed using the original
     * 2D transform) and {@code b} (which was transformed in 1D using the subspace transform)
     * are equivalent.
     *
     * @param transform the transform to apply to this instance
     * @return an object containing the transformed line along with a transform that can be applied
     *      to subspace points
     * @see #transform(Transform)
     */
    public SubspaceTransform subspaceTransform(final Transform<Vector2D> transform) {
        final Vector2D origin = getOrigin();

        final Vector2D p1 = transform.apply(origin);
        final Vector2D p2 = transform.apply(origin.add(direction));

        final Line tLine = Lines.fromPoints(p1, p2, getPrecision());

        final Vector1D tSubspaceOrigin = tLine.toSubspace(p1);
        final Vector1D tSubspaceDirection = tSubspaceOrigin.vectorTo(tLine.toSubspace(p2));

        final double translation = tSubspaceOrigin.getX();
        final double scale = tSubspaceDirection.getX();

        final AffineTransformMatrix1D subspaceTransform = AffineTransformMatrix1D.of(scale, translation);

        return new SubspaceTransform(tLine, subspaceTransform);
    }

    /** {@inheritDoc} */
    @Override
    public LineConvexSubset span() {
        return Lines.span(this);
    }

    /** Create a new line segment from the given 1D interval. The returned line
     * segment consists of all points between the two locations, regardless of the order the
     * arguments are given.
     * @param a first 1D location for the interval
     * @param b second 1D location for the interval
     * @return a new line segment on this line
     * @throws IllegalArgumentException if either of the locations is NaN or infinite
     * @see Lines#segmentFromLocations(Line, double, double)
     */
    public Segment segment(final double a, final double b) {
        return Lines.segmentFromLocations(this, a, b);
    }

    /** Create a new line segment from two points. The returned segment represents all points on this line
     * between the projected locations of {@code a} and {@code b}. The points may be given in any order.
     * @param a first point
     * @param b second point
     * @return a new line segment on this line
     * @throws IllegalArgumentException if either point contains NaN or infinite coordinate values
     * @see Lines#segmentFromPoints(Line, Vector2D, Vector2D)
     */
    public Segment segment(final Vector2D a, final Vector2D b) {
        return Lines.segmentFromPoints(this, a, b);
    }

    /** Create a new convex line subset that starts at infinity and continues along
     * the line up to the projection of the given end point.
     * @param endPoint point defining the end point of the line subset; the end point
     *      is equal to the projection of this point onto the line
     * @return a new, half-open line subset that ends at the given point
     * @throws IllegalArgumentException if any coordinate in {@code endPoint} is NaN or infinite
     * @see Lines#reverseRayFromPoint(Line, Vector2D)
     */
    public ReverseRay reverseRayTo(final Vector2D endPoint) {
        return Lines.reverseRayFromPoint(this, endPoint);
    }

    /** Create a new convex line subset that starts at infinity and continues along
     * the line up to the given 1D location.
     * @param endLocation the 1D location of the end of the half-line
     * @return a new, half-open line subset that ends at the given 1D location
     * @throws IllegalArgumentException if {@code endLocation} is NaN or infinite
     * @see Lines#reverseRayFromLocation(Line, double)
     */
    public ReverseRay reverseRayTo(final double endLocation) {
        return Lines.reverseRayFromLocation(this, endLocation);
    }

    /** Create a new ray instance that starts at the projection of the given point
     * and continues in the direction of the line to infinity.
     * @param startPoint point defining the start point of the ray; the start point
     *      is equal to the projection of this point onto the line
     * @return a ray starting at the projected point and extending along this line
     *      to infinity
     * @throws IllegalArgumentException if any coordinate in {@code startPoint} is NaN or infinite
     * @see Lines#rayFromPoint(Line, Vector2D)
     */
    public Ray rayFrom(final Vector2D startPoint) {
        return Lines.rayFromPoint(this, startPoint);
    }

    /** Create a new ray instance that starts at the given 1D location and continues in
     * the direction of the line to infinity.
     * @param startLocation 1D location defining the start point of the ray
     * @return a ray starting at the given 1D location and extending along this line
     *      to infinity
     * @throws IllegalArgumentException if {@code startLocation} is NaN or infinite
     * @see Lines#rayFromLocation(Line, double)
     */
    public Ray rayFrom(final double startLocation) {
        return Lines.rayFromLocation(this, startLocation);
    }

    /** Get the abscissa of the given point on the line. The abscissa represents
     * the distance the projection of the point on the line is from the line's
     * origin point (the point on the line closest to the origin of the
     * 2D space). Abscissa values increase in the direction of the line. This method
     * is exactly equivalent to {@link #toSubspace(Vector2D)} except that this method
     * returns a double instead of a {@link Vector1D}.
     * @param point point to compute the abscissa for
     * @return abscissa value of the point
     * @see #toSubspace(Vector2D)
     */
    public double abscissa(final Vector2D point) {
        return direction.dot(point);
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D toSubspace(final Vector2D point) {
        return Vector1D.of(abscissa(point));
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D toSpace(final Vector1D point) {
        return toSpace(point.getX());
    }

    /** Convert the given abscissa value (1D location on the line)
     * into a 2D point.
     * @param abscissa value to convert
     * @return 2D point corresponding to the line abscissa value
     */
    public Vector2D toSpace(final double abscissa) {
        // The 2D coordinate is equal to the projection of the
        // 2D origin onto the line plus the direction multiplied
        // by the abscissa. We can combine everything into a single
        // step below given that the origin location is equal to
        // (-direction.y * originOffset, direction.x * originOffset).
        return Vector2D.of(
                    LinearCombination.value(abscissa, direction.getX(), -originOffset, direction.getY()),
                    LinearCombination.value(abscissa, direction.getY(), originOffset, direction.getX())
                );
    }

    /** Get the intersection point of the instance and another line.
     * @param other other line
     * @return intersection point of the instance and the other line
     *      or null if there is no unique intersection point (ie, the lines
     *      are parallel or coincident)
     */
    public Vector2D intersection(final Line other) {
        final double area = this.direction.signedArea(other.direction);
        if (getPrecision().eqZero(area)) {
            // lines are parallel
            return null;
        }

        final double x = LinearCombination.value(
                other.direction.getX(), originOffset,
                -direction.getX(), other.originOffset) / area;

        final double y = LinearCombination.value(
                other.direction.getY(), originOffset,
                -direction.getY(), other.originOffset) / area;

        return Vector2D.of(x, y);
    }

    /** Compute the angle in radians between this instance's direction and the direction
     * of the given line. The return value is in the range {@code [-pi, +pi)}. This method
     * always returns a value, even for parallel or coincident lines.
     * @param other other line
     * @return the angle required to rotate this line to point in the direction of
     *      the given line
     */
    public double angle(final Line other) {
        final double thisAngle = Math.atan2(direction.getY(), direction.getX());
        final double otherAngle = Math.atan2(other.direction.getY(), other.direction.getX());

        return Angle.Rad.WITHIN_MINUS_PI_AND_PI.applyAsDouble(otherAngle - thisAngle);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D project(final Vector2D point) {
        return toSpace(toSubspace(point));
    }

    /** {@inheritDoc} */
    @Override
    public double offset(final Vector2D point) {
        return originOffset - direction.signedArea(point);
    }

    /** Get the offset (oriented distance) of the given line relative to this instance.
     * Since an infinite number of distances can be calculated between points on two
     * different lines, this method returns the value closest to zero. For intersecting
     * lines, this will simply be zero. For parallel lines, this will be the
     * perpendicular distance between the two lines, as a signed value.
     *
     * <p>The sign of the returned offset indicates the side of the line that the
     * argument lies on. The offset is positive if the line lies on the right side
     * of the instance and negative if the line lies on the left side
     * of the instance.</p>
     * @param line line to check
     * @return offset of the line
     * @see #distance(Line)
     */
    public double offset(final Line line) {
        if (isParallel(line)) {
            // since the lines are parallel, the offset between
            // them is simply the difference between their origin offsets,
            // with the second offset negated if the lines point if opposite
            // directions
            final double dot = direction.dot(line.direction);
            return originOffset - (Math.signum(dot) * line.originOffset);
        }

        // the lines are not parallel, which means they intersect at some point
        return 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean similarOrientation(final Hyperplane<Vector2D> other) {
        final Line otherLine = (Line) other;
        return direction.dot(otherLine.direction) >= 0.0;
    }

    /** Get one point from the plane, relative to the coordinate system
     * of the line. Note that the direction of increasing offsets points
     * to the <em>right</em> of the line. This means that if one pictures
     * the line (abscissa) direction as equivalent to the +x-axis, the offset
     * direction will point along the -y axis.
     * @param abscissa desired abscissa (distance along the line) for the point
     * @param offset desired offset (distance perpendicular to the line) for the point
     * @return one point in the plane, with given abscissa and offset
     *      relative to the line
     */
    public Vector2D pointAt(final double abscissa, final double offset) {
        final double pointOffset = offset - originOffset;
        return Vector2D.of(LinearCombination.value(abscissa, direction.getX(),  pointOffset, direction.getY()),
                            LinearCombination.value(abscissa, direction.getY(), -pointOffset, direction.getX()));
    }

    /** Check if the line contains a point.
     * @param p point to check
     * @return true if p belongs to the line
     */
    @Override
    public boolean contains(final Vector2D p) {
        return getPrecision().eqZero(offset(p));
    }

    /** Check if this instance completely contains the other line.
     * This will be true if the two instances represent the same line,
     * with perhaps different directions.
     * @param line line to check
     * @return true if this instance contains all points in the given line
     */
    public boolean contains(final Line line) {
        return isParallel(line) && getPrecision().eqZero(offset(line));
    }

    /** Compute the distance between the instance and a point.
     * <p>This is a shortcut for invoking Math.abs(getOffset(p)),
     * and provides consistency with what is in the
     * org.apache.commons.geometry.euclidean.threed.Line class.</p>
     *
     * @param p to check
     * @return distance between the instance and the point
     */
    public double distance(final Vector2D p) {
        return Math.abs(offset(p));
    }

    /** Compute the shortest distance between this instance and
     * the given line. This value will simply be zero for intersecting
     * lines.
     * @param line line to compute the closest distance to
     * @return the shortest distance between this instance and the
     *      given line
     * @see #offset(Line)
     */
    public double distance(final Line line) {
        return Math.abs(offset(line));
    }

    /** Check if the instance is parallel to another line.
     * @param line other line to check
     * @return true if the instance is parallel to the other line
     *  (they can have either the same or opposite orientations)
     */
    public boolean isParallel(final Line line) {
        final double area = direction.signedArea(line.direction);
        return getPrecision().eqZero(area);
    }

    /** Return true if this instance should be considered equivalent to the argument, using the
     * given precision context for comparison. Instances are considered equivalent if they have
     * equivalent {@code origin} points and make similar angles with the x-axis.
     * @param other the point to compare with
     * @param precision precision context to use for the comparison
     * @return true if this instance should be considered equivalent to the argument
     * @see Vector2D#eq(Vector2D, Precision.DoubleEquivalence)
     */
    public boolean eq(final Line other, final Precision.DoubleEquivalence precision) {
        return getOrigin().eq(other.getOrigin(), precision) &&
                precision.eq(getAngle(), other.getAngle());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 167;

        int result = 1;
        result = (prime * result) + Objects.hashCode(direction);
        result = (prime * result) + Double.hashCode(originOffset);
        result = (prime * result) + Objects.hashCode(getPrecision());

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Line)) {
            return false;
        }

        final Line other = (Line) obj;

        return Objects.equals(this.direction, other.direction) &&
                Double.compare(this.originOffset, other.originOffset) == 0 &&
                Objects.equals(this.getPrecision(), other.getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MessageFormat.format(TO_STRING_FORMAT,
                getClass().getSimpleName(),
                getOrigin(),
                getDirection());
    }

    /** Class containing a transformed line instance along with a subspace (1D) transform. The subspace
     * transform produces the equivalent of the 2D transform in 1D.
     */
    public static final class SubspaceTransform {
        /** The transformed line. */
        private final Line line;

        /** The subspace transform instance. */
        private final AffineTransformMatrix1D transform;

        /** Simple constructor.
         * @param line the transformed line
         * @param transform 1D transform that can be applied to subspace points
         */
        public SubspaceTransform(final Line line, final AffineTransformMatrix1D transform) {
            this.line = line;
            this.transform = transform;
        }

        /** Get the transformed line instance.
         * @return the transformed line instance
         */
        public Line getLine() {
            return line;
        }

        /** Get the 1D transform that can be applied to subspace points. This transform can be used
         * to perform the equivalent of the 2D transform in 1D space.
         * @return the subspace transform instance
         */
        public AffineTransformMatrix1D getTransform() {
            return transform;
        }
    }
}
