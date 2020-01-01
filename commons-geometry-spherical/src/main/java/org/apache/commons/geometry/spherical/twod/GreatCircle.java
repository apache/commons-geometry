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

import org.apache.commons.geometry.core.Equivalency;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.AbstractHyperplane;
import org.apache.commons.geometry.core.partitioning.EmbeddingHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Class representing a great circle on the 2-sphere. A great circle is the
 * intersection of a sphere with a plane that passes through its center. It is
 * the largest diameter circle that can be drawn on the sphere and partitions the
 * sphere into two hemispheres. The vectors {@code u} and {@code v} lie in the great
 * circle plane, while the vector {@code w} (the pole) is perpendicular to it.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class GreatCircle extends AbstractHyperplane<Point2S>
    implements EmbeddingHyperplane<Point2S, Point1S>, Equivalency<GreatCircle> {
    /** Pole or circle center. */
    private final Vector3D.Unit pole;

    /** First axis in the equator plane, origin of the azimuth angles. */
    private final Vector3D.Unit u;

    /** Second axis in the equator plane, in quadrature with respect to u. */
    private final Vector3D.Unit v;

    /** Simple constructor. Callers are responsible for ensuring the inputs are valid.
     * @param pole pole vector of the great circle
     * @param u u axis in the equator plane
     * @param v v axis in the equator plane
     * @param precision precision context used for floating point comparisons
     */
    private GreatCircle(final Vector3D.Unit pole, final Vector3D.Unit u, final Vector3D.Unit v,
            final DoublePrecisionContext precision) {
        super(precision);

        this.pole = pole;
        this.u = u;
        this.v = v;
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

    /** Get the u axis of the great circle. This vector is located in the equator plane and defines
     * the {@code 0pi} location of the embedded subspace.
     * @return u axis of the great circle
     */
    public Vector3D.Unit getU() {
        return u;
    }

    /** Get the v axis of the great circle. This vector lies in the equator plane,
     * perpendicular to the u-axis.
     * @return v axis of the great circle
     */
    public Vector3D.Unit getV() {
        return v;
    }

    /** Get the w (pole) axis of the great circle. The method is equivalent to {@code #getPole()}.
     * @return the w (pole) axis of the great circle.
     * @see #getPole()
     */
    public Vector3D.Unit getW() {
        return getPole();
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
     * @param vec vector to compute the offset for
     * @return the offset (oriented distance) of a direction
     */
    public double offset(final Vector3D vec) {
        return pole.angle(vec) - PlaneAngleRadians.PI_OVER_TWO;
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
     * equator plane relative to the plane's u-axis. Since the vector is
     * projected onto the equator plane, it does not need to belong to the circle.
     * Vectors parallel to the great circle's pole do not have a defined azimuth angle.
     * In these cases, the method follows the rules of the
     * {@code Math#atan2(double, double)} method and returns {@code 0}.
     * @param vector vector to compute the great circle azimuth of
     * @return azimuth angle of the vector around the great circle in the range
     *      {@code [0, 2pi)}
     * @see #toSubspace(Point2S)
     */
    public double azimuth(final Vector3D vector) {
        double az = Math.atan2(vector.dot(v), vector.dot(u));

        // adjust range
        if (az < 0) {
            az += PlaneAngleRadians.TWO_PI;
        }

        return az;
    }

    /** Get the vector on the great circle with the given azimuth angle.
     * @param azimuth azimuth angle in radians
     * @return the point on the great circle with the given phase angle
     */
    public Vector3D vectorAt(final double azimuth) {
        return Vector3D.linearCombination(Math.cos(azimuth), u, Math.sin(azimuth), v);
    }

    /** {@inheritDoc} */
    @Override
    public Point2S project(final Point2S point) {
        final double az = azimuth(point.getVector());
        return Point2S.from(vectorAt(az));
    }

    /** {@inheritDoc}
     *
     * <p>The returned instance has the same u-axis but opposite pole and v-axis
     * as this instance.</p>
     */
    @Override
    public GreatCircle reverse() {
        return new GreatCircle(pole.negate(), u, v.negate(), getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public GreatCircle transform(final Transform<Point2S> transform) {
        final Point2S tu = transform.apply(Point2S.from(u));
        final Point2S tv = transform.apply(Point2S.from(v));

        return fromPoints(tu, tv, getPrecision());
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
     * @param pt point determining the circle intersection to compute the angle at
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
                u.eq(other.u, precision) &&
                v.eq(other.v, precision);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(pole, u, v, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof GreatCircle)) {
            return false;
        }

        GreatCircle other = (GreatCircle) obj;

        return Objects.equals(this.pole, other.pole) &&
                Objects.equals(this.u, other.u) &&
                Objects.equals(this.v, other.v) &&
                Objects.equals(this.getPrecision(), other.getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[pole= ")
            .append(pole)
            .append(", u= ")
            .append(u)
            .append(", v= ")
            .append(v)
            .append(']');

        return sb.toString();
    }

    /** Create a great circle instance from its pole vector. An arbitrary u-axis is chosen.
     * @param pole pole vector for the great circle
     * @param precision precision context used to compare floating point values
     * @return a great circle defined by the given pole vector
     */
    public static GreatCircle fromPole(final Vector3D pole, final DoublePrecisionContext precision) {
        final Vector3D.Unit u = pole.orthogonal();
        final Vector3D.Unit v = pole.cross(u).normalize();
        return new GreatCircle(pole.normalize(), u, v, precision);
    }

    /** Create a great circle instance from its pole vector and a vector representing the u-axis
     * in the equator plane. The u-axis vector defines the {@code 0pi} location for the embedded
     * subspace.
     * @param pole pole vector for the great circle
     * @param u u-axis direction for the equator plane
     * @param precision precision context used to compare floating point values
     * @return a great circle defined by the given pole vector and u-axis direction
     */
    public static GreatCircle fromPoleAndU(final Vector3D pole, final Vector3D u,
            final DoublePrecisionContext precision) {

        final Vector3D.Unit unitPole = pole.normalize();
        final Vector3D.Unit unitX = pole.orthogonal(u);
        final Vector3D.Unit unitY = pole.cross(u).normalize();

        return new GreatCircle(unitPole, unitX, unitY, precision);
    }

    /** Create a great circle instance from two points on the circle. The u-axis of the
     * instance points to the location of the first point. The orientation of the circle
     * is along the shortest path between the two points.
     * @param a first point on the great circle
     * @param b second point on the great circle
     * @param precision precision context used to compare floating point values
     * @return great circle instance containing the given points
     * @throws IllegalArgumentException if either of the given points is NaN or infinite, or if the given points are
     *      equal or antipodal as evaluated by the given precision context
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
        } else if (precision.eq(dist, PlaneAngleRadians.PI)) {
            err = "antipodal";
        }

        if (err != null) {
            throw new IllegalArgumentException("Cannot create great circle from points " + a + " and " + b +
                    ": points are " + err);
        }

        final Vector3D.Unit u = a.getVector().normalize();
        final Vector3D.Unit pole = u.cross(b.getVector()).normalize();
        final Vector3D.Unit v = pole.cross(u).normalize();

        return new GreatCircle(pole, u, v, precision);
    }
}
