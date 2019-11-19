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
package org.apache.commons.geometry.spherical.twod;

import java.util.Objects;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.Point;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.GeometryException;
import org.apache.commons.geometry.core.internal.Equivalency;
import org.apache.commons.geometry.core.partitioning.AbstractHyperplane;
import org.apache.commons.geometry.core.partitioning.EmbeddingHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.Point1S;

/** Class representing a great circle on the 2-sphere. A great circle is the
 * intersection of a sphere with a plane that passes through its center. It is
 * the largest diameter circle that can be drawn on the sphere and partitions the
 * sphere into two hemispheres.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class GreatCircle extends AbstractHyperplane<Point2S>
    implements EmbeddingHyperplane<Point2S, Point1S>, Equivalency<GreatCircle> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190928L;

    /** Pole or circle center. */
    private final Vector3D.Unit pole;

    /** First axis in the equator plane, origin of the azimuth angles. */
    private final Vector3D.Unit x;

    /** Second axis in the equator plane, in quadrature with respect to x. */
    private final Vector3D.Unit y;

    /** Simple constructor. Callers are responsible for ensuring the inputs are valid.
     * @param pole pole vector of the great circle
     * @param x x axis in the equator plane
     * @param y y axis in the equator plane
     * @param precision precision context used for floating point comparisons
     */
    private GreatCircle(final Vector3D.Unit pole, final Vector3D.Unit x, final Vector3D.Unit y,
            final DoublePrecisionContext precision) {
        super(precision);

        this.pole = pole;
        this.x = x;
        this.y = y;
    }

    /** Get the pole of the great circle. This vector is perpendicular to the
     * equator plane of the instance.
     * @return pole of the great circle
     */
    public Vector3D.Unit getPole() {
        return pole;
    }

    /** Get the spherical point located at the positive pole of the instance.
     * @return the spherical point located at the positive pole of the instance
     */
    public Point2S getPolePoint() {
        return Point2S.from(pole);
    }

    /** Get the x axis of the great circle. This vector defines the {@code 0pi}
     * location of the embedded subspace.
     * @return x axis of the great circle
     */
    public Vector3D.Unit getXAxis() {
        return x;
    }

    /** Get the y axis of the great circle. This vector lies in the equator plane,
     * perpendicular to the x-axis.
     * @return y axis of the great circle
     */
    public Vector3D.Unit getYAxis() {
        return y;
    }

    /** {@inheritDoc}
     *
     * <p>The returned offset values are in the range {@code [-pi/2, +pi/2]},
     * with a point directly on the circle's pole vector having an offset of
     * {@code -pi/2} and its antipodal point having an offset of {@code +pi/2}.
     * Thus, the circle's pole vector points toward the <em>minus</em> side of
     * the hyperplane.</p>
     *
     * @see #offset(Vector3D)
     */
    @Override
    public double offset(final Point2S point) {
        return offset(point.getVector());
    }

    /** Get the offset (oriented distance) of a direction.
     *
     * <p>The offset computed here is equal to the angle between the circle's
     * pole and the given vector minus {@code pi/2}. Thus, the pole vector
     * has an offset of {@code -pi/2}, a point on the circle itself has an
     * offset of {@code 0}, and the negation of the pole vector has an offset
     * of {@code +pi/2}.</p>
     * @param vec
     * @return
     */
    public double offset(final Vector3D vec) {
        return pole.angle(vec) - Geometry.HALF_PI;
    }

    /** Get the azimuth angle of a point relative to this great circle instance,
     *  in the range {@code [0, 2pi)}.
     * @param pt point to compute the azimuth for
     * @return azimuth angle of the point in the range {@code [0, 2pi)}
     */
    public double azimuth(final Point2S pt) {
        return azimuth(pt.getVector());
    }

    /** Get the azimuth angle of a vector in the range {@code [0, 2pi)}.
     * The azimuth angle is the angle of the projection of the argument on the
     * equator plane relative to the plane's x-axis. Since the vector is
     * projected onto the equator plane, it does not need to belong to the circle.
     * Vectors parallel to the great circle's pole do not have a defined azimuth angle.
     * In these cases, the method follows the rules of the
     * {@code Math#atan2(double, double)} method and returns {@code 0}.
     * @param vector vector to compute the great circle azimuth of
     * @return azimuth angle of the vector around the great circle in the range
     *      {@code [0, 2pi)}
     * @see #toSubSpace(Point)
     */
    public double azimuth(final Vector3D vector) {
        double az = Math.atan2(vector.dot(y), vector.dot(x));

        // adjust range
        if (az < 0) {
            az += Geometry.TWO_PI;
        }

        return az;
    }

    /** Get the vector on the great circle with the given azimuth angle.
     * @param azimuth azimuth angle in radians
     * @return the point on the great circle with the given phase angle
     */
    public Vector3D vectorAt(final double azimuth) {
        return Vector3D.linearCombination(Math.cos(azimuth), x, Math.sin(azimuth), y);
    }

    /** {@inheritDoc} */
    @Override
    public Point2S project(final Point2S point) {
        final double az = azimuth(point.getVector());
        return Point2S.from(vectorAt(az));
    }

    /** {@inheritDoc}
     *
     * <p>The returned instance has the same x-axis but opposite pole and y-axis
     * as this instance.</p>
     */
    @Override
    public GreatCircle reverse() {
        return new GreatCircle(pole.negate(), x, y.negate(), getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public GreatCircle transform(final Transform<Point2S> transform) {
        final Point2S tx = transform.apply(Point2S.from(x));
        final Point2S ty = transform.apply(Point2S.from(y));

        return fromPoints(tx, ty, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public boolean similarOrientation(final Hyperplane<Point2S> other) {
        final GreatCircle otherCircle = (GreatCircle) other;
        return pole.dot(otherCircle.pole) > 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public GreatArc span() {
        return GreatArc.fromInterval(this, AngularInterval.full());
    }

    /** Create an arc on this circle between the given points.
     * @param start start point
     * @param end end point
     * @return an arc on this circle between the given points
     * @throws IllegalArgumentException if the specified interval is not
     *      convex (ie, the angle between the points is greater than {@code pi}
     */
    public GreatArc arc(final Point2S start, final Point2S end) {
        return arc(toSubspace(start), toSubspace(end));
    }

    /** Create an arc on this circle between the given subspace points.
     * @param start start subspace point
     * @param end end subspace point
     * @return an arc on this circle between the given subspace points
     * @throws IllegalArgumentException if the specified interval is not
     *      convex (ie, the angle between the points is greater than {@code pi}
     */
    public GreatArc arc(final Point1S start, final Point1S end) {
        return arc(start.getAzimuth(), end.getAzimuth());
    }

    /** Create an arc on this circle between the given subspace azimuth values.
     * @param start start subspace azimuth
     * @param end end subspace azimuth
     * @return an arc on this circle between the given subspace azimuths
     * @throws IllegalArgumentException if the specified interval is not
     *      convex (ie, the angle between the points is greater than {@code pi}
     */
    public GreatArc arc(final double start, final double end) {
        return arc(AngularInterval.Convex.of(start, end, getPrecision()));
    }

    /** Create an arc on this circle consisting of the given subspace interval.
     * @param interval subspace interval
     * @return an arc on this circle consisting of the given subspace interval
     */
    public GreatArc arc(final AngularInterval.Convex interval) {
        return GreatArc.fromInterval(this, interval);
    }

    /** Return one of the two intersection points between this instance and the argument.
     * If the circles occupy the same space (ie, their poles are parallel or anti-parallel),
     * then null is returned. Otherwise, the intersection located at the cross product of
     * the pole of this instance and that of the argument is returned (ie, {@code thisPole.cross(otherPole)}.
     * The other intersection point of the pair is antipodal to this point.
     * @param other circle to intersect with
     * @return one of the two intersection points between this instance and the argument
     */
    public Point2S intersection(final GreatCircle other) {
        final Vector3D cross = pole.cross(other.pole);
        if (!cross.eq(Vector3D.ZERO, getPrecision())) {
            return Point2S.from(cross);
        }

        return null;
    }

    /** Compute the angle between this great circle and the argument.
     * The return value is the angle between the poles of the two circles,
     * in the range {@code [0, pi]}.
     * @param other great circle to compute the angle with
     * @return the angle between this great circle and the argument in the
     *      range {@code [0, pi]}
     * @see #angle(GreatCircle, Point2S)
     */
    public double angle(final GreatCircle other) {
        return pole.angle(other.pole);
    }

    /** Compute the angle between this great circle and the argument, measured
     * at the intersection point closest to the given point. The value is computed
     * as if a tangent line was drawn from each great circle at the intersection
     * point closest to {@code pt}, and the angle required to rotate the tangent
     * line representing the current instance to align with that of the given
     * instance was measured. The return value lies in the range {@code [-pi, pi)} and
     * has an absolute value equal to that returned by {@link #angle(GreatCircle)}, but
     * possibly a different sign. If the given point is equidistant from both intersection
     * points (as evaluated by this instance's precision context), then the point is assumed
     * to be closest to the point opposite the cross product of the two poles.
     * @param other great circle to compute the angle with
     * @return the angle between this great circle and the argument as measured at the
     *      intersection point closest to the given point; the value is in the range
     *      {@code [-pi, pi)}
     * @see #angle(GreatCircle)
     */
    public double angle(final GreatCircle other, final Point2S pt) {
        final double theta = angle(other);
        final Vector3D cross = pole.cross(other.pole);

        return getPrecision().gt(pt.getVector().dot(cross), 0) ?
                theta :
                -theta;
    }

    /** {@inheritDoc} */
    @Override
    public Point1S toSubspace(final Point2S point) {
        return Point1S.of(azimuth(point.getVector()));
    }

    /** {@inheritDoc} */
    @Override
    public Point2S toSpace(final Point1S point) {
        return Point2S.from(vectorAt(point.getAzimuth()));
    }

    /** {@inheritDoc} */
    @Override
    public boolean eq(final GreatCircle other) {
        if (this == other) {
            return true;
        }

        final DoublePrecisionContext precision = getPrecision();

        return precision.equals(other.getPrecision()) &&
                pole.eq(other.pole, precision) &&
                x.eq(other.x, precision) &&
                y.eq(other.y, precision);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(pole, x, y, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (!(obj instanceof GreatCircle)) {
            return false;
        }

        GreatCircle other = (GreatCircle) obj;

        return Objects.equals(this.pole, other.pole) &&
                Objects.equals(this.x, other.x) &&
                Objects.equals(this.y, other.y) &&
                Objects.equals(this.getPrecision(), other.getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[pole= ")
            .append(pole)
            .append(", x= ")
            .append(x)
            .append(", y= ")
            .append(y)
            .append(']');

        return sb.toString();
    }

    /** Create a great circle instance from its pole vector. An arbitrary x-axis is chosen.
     * @param pole pole vector for the great circle
     * @param precision precision context used to compare floating point values
     * @return a great circle defined by the given pole vector
     */
    public static GreatCircle fromPole(final Vector3D pole, final DoublePrecisionContext precision) {
        final Vector3D.Unit x = pole.orthogonal();
        final Vector3D.Unit y = pole.cross(x).normalize();
        return new GreatCircle(pole.normalize(), x, y, precision);
    }

    /** Create a great circle instance from its pole vector and a vector representing the x-axis
     * in the equator plane. The x-axis vector defines the {@code 0pi} location for the embedded
     * subspace.
     * @param pole pole vector for the great circle
     * @param x x-axis direction for the equator plane
     * @param precision precision context used to compare floating point values
     * @return a great circle defined by the given pole vector and x-axis direction
     */
    public static GreatCircle fromPoleAndXAxis(final Vector3D pole, final Vector3D x,
            final DoublePrecisionContext precision) {

        final Vector3D.Unit unitPole = pole.normalize();
        final Vector3D.Unit unitX = pole.orthogonal(x);
        final Vector3D.Unit unitY = pole.cross(x).normalize();

        return new GreatCircle(unitPole, unitX, unitY, precision);
    }

    /** Create a great circle instance from two points on the circle. The x-axis of the
     * instance points to the location of the first point. The orientation of the circle
     * is along the shortest path between the two points.
     * @param a first point on the great circle
     * @param b second point on the great circle
     * @param precision precision context used to compare floating point values
     * @return great circle instance containing the given points
     * @throws IllegalArgumentException if either of the given points is NaN or infinite
     * @throws GeometryException if the given points are equal or antipodal as evaluated by
     *      the given precision context
     */
    public static GreatCircle fromPoints(final Point2S a, final Point2S b,
            final DoublePrecisionContext precision) {

        if (!a.isFinite() || !b.isFinite()) {
            throw new IllegalArgumentException("Invalid points for great circle: " + a + ", " + b);
        }

        String err = null;

        final double dist = a.distance(b);
        if (precision.eqZero(dist)) {
            err = "equal";
        }
        else if (precision.eq(dist, Geometry.PI)) {
            err = "antipodal";
        }

        if (err != null) {
            throw new GeometryException("Cannot create great circle from points " + a + " and " + b +
                    ": points are " + err);
        }

        final Vector3D.Unit x = a.getVector().normalize();
        final Vector3D.Unit pole = x.cross(b.getVector()).normalize();
        final Vector3D.Unit y = pole.cross(x).normalize();

        return new GreatCircle(pole, x, y, precision);
    }
}
