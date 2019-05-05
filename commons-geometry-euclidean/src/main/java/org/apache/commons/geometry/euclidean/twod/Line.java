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

import java.util.Objects;

import org.apache.commons.geometry.core.Embedding;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.partition.AbstractHyperplane;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.apache.commons.numbers.arrays.LinearCombination;

public class Line extends AbstractHyperplane<Vector2D> implements Embedding<Vector2D, Vector1D> {

    /** Serializable UID. */
    private static final long serialVersionUID = 20190120L;

    /** The direction of the line as a normalized vector. */
    private final Vector2D direction;

    /** The distance between the origin and the line. */
    private final double originOffset;

    /** Simple constructor.
     * @param direction The direction of the line.
     * @param originOffset The signed distance between the line and the origin.
     * @param precision Precision context used to compare floating point numbers.
     */
    private Line(final Vector2D direction, final double originOffset, final DoublePrecisionContext precision) {
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
        return PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(angle);
    }

    /** Get the direction of the line.
     * @return the direction of the line
     */
    public Vector2D getDirection() {
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

    /** Get the reverse of the instance, meaning a line containing the same
     * points but with the opposite orientation.
     * @return a new line, with orientation opposite to the instance orientation
     */
    public Line reverse() {
        return new Line(direction.negate(), -originOffset, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D plusPoint() {
        return pointAt(0, +Math.floor(getPrecision().getMaxZero()) + 1.0);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D minusPoint() {
         return pointAt(0, -Math.floor(getPrecision().getMaxZero()) + 1.0);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D onPoint() {
        return getOrigin();
    }

    /** {@inheritDoc} */
    @Override
    public Line transform(Transform<Vector2D> transform) {
        final Vector2D origin = getOrigin();

        final Vector2D transformedOrigin = transform.apply(origin);
        final Vector2D transformedOriginPlusDir = transform.apply(origin.add(getDirection()));

        return fromPoints(transformedOrigin, transformedOriginPlusDir, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public LineSegment span() {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D toSubSpace(final Vector2D point) {
        return Vector1D.of(direction.dot(point));
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D toSpace(final Vector1D point) {
        final double abscissa = point.getX();

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

    /** {@inheritDoc} */
    @Override
    public Vector2D project(final Vector2D point) {
        return toSpace(toSubSpace(point));
    }

    /** {@inheritDoc} */
    @Override
    public double offset(final Vector2D point) {
        return originOffset - direction.signedArea(point);
    }

    /** Get the offset (oriented distance) of a line. Since an infinite
     * number of distances can be calculated between points on two different
     * lines, this methods returns the value closest to zero. For intersecting
     * lines, this will simply be zero. For parallel lines, this will be the
     * perpendicular distance between the two lines, as a signed value.
     *
     * <p>The sign of the returned offset indicates the side of the line that the
     * argument lies on. The offset is positive if the line lies on the right side
     * of the instance and negative if the line lies on the left side
     * of the instance.</p>
     * @param line line to check
     * @return offset of the line
     * @see #distance(Line_Old)
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
     * @see #offset(Line_Old)
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (!(obj instanceof Line)) {
            return false;
        }

        Line other = (Line) obj;

        return Objects.equals(this.direction, other.direction) &&
                Double.compare(this.originOffset, other.originOffset) == 0 &&
                Objects.equals(this.getPrecision(), other.getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[origin= ")
            .append(getOrigin())
            .append(", direction= ")
            .append(direction)
            .append(']');

        return sb.toString();
    }

    /** Create a line from two points lying on the line. The line points in the direction
     * from {@code p1} to {@code p2}.
     * @param p1 first point
     * @param p2 second point
     * @param precision precision context used to compare floating point values
     * @return new line containing {@code p1} and {@code p2} and pointing in the direction
     *      from {@code p1} to {@code p2}
     * @throws GeometryValueException If the vector between {@code p1} and {@code p2} has zero length,
     *      as evaluated by the given precision context
     */
    public static Line fromPoints(final Vector2D p1, final Vector2D p2, final DoublePrecisionContext precision) {
        return fromPointAndDirection(p1, p1.vectorTo(p2), precision);
    }

    /** Create a line from a point and direction.
     * @param pt point belonging to the line
     * @param dir the direction of the line
     * @param precision precision context used to compare floating point values
     * @return new line containing {@code pt} and pointing in direction {@code dir}
     * @throws GeometryValueException If {@code dir} has zero length, as evaluated by the
     *      given precision context
     */
    public static Line fromPointAndDirection(final Vector2D pt, final Vector2D dir, final DoublePrecisionContext precision) {
        if (dir.isZero(precision)) {
            throw new GeometryValueException("Line direction cannot be zero");
        }

        final Vector2D normalizedDir = dir.normalize();
        final double originOffset = normalizedDir.signedArea(pt);

        return new Line(normalizedDir, originOffset, precision);
    }

    /** Create a line from a point lying on the line and an angle relative to the abscissa (x) axis. Note that the
     * line does not need to intersect the x-axis; the given angle is simply relative to it.
     * @param pt point belonging to the line
     * @param angle angle of the line with respect to abscissa (x) axis, in radians
     * @param precision precision context used to compare floating point values
     * @return new line containing {@code pt} and forming the given angle with the
     *      abscissa (x) axis.
     */
    public static Line fromPointAndAngle(final Vector2D pt, final double angle, final DoublePrecisionContext precision) {
        final Vector2D dir = Vector2D.normalize(Math.cos(angle), Math.sin(angle));
        return fromPointAndDirection(pt, dir, precision);
    }
}
