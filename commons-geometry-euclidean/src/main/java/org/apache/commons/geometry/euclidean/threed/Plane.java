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

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.IllegalNormException;
import org.apache.commons.geometry.core.internal.Equivalency;
import org.apache.commons.geometry.core.partition.AbstractHyperplane;
import org.apache.commons.geometry.core.partition.EmbeddingHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

public final class Plane extends AbstractHyperplane<Vector3D>
    implements EmbeddingHyperplane<Vector3D, Vector2D>, Equivalency<Plane> {

    /** Serializable version UID */
    private static final long serialVersionUID = 20190702L;

    /** First normalized vector of the plane frame (in plane). */
    private final Vector3D u;

    /** Second normalized vector of the plane frame (in plane). */
    private final Vector3D v;

    /** Normalized plane normal. */
    private final Vector3D w;

    /** Offset of the origin with respect to the plane. */
    private final double originOffset;

    /**
     * Constructor to build a new plane with the given values.
     * Made private to prevent inheritance.
     * @param u u vector (on plane)
     * @param v v vector (on plane)
     * @param w unit normal vector
     * @param originOffset offset of the origin with respect to the plane.
     * @param precision precision context used to compare floating point values
     * @throws IllegalArgumentException if the provided vectors are coplanar or not normalized
     */
    private Plane(final Vector3D u, final Vector3D v, final Vector3D w, double originOffset,
            final DoublePrecisionContext precision) {

        super(precision);

        this.u = u;
        this.v = v;
        this.w = w;

        this.originOffset = originOffset;
    }

    /**
     * Get the orthogonal projection of the 3D-space origin in the plane.
     * @return the origin point of the plane frame (point closest to the 3D-space
     *         origin)
     */
    public Vector3D getOrigin() {
        return w.multiply(-originOffset);
    }

    /**
     *  Get the offset of the spatial origin ({@code 0, 0, 0}) with respect to the plane.
     *
     *  @return the offset of the origin with respect to the plane.
     */
    public double getOriginOffset() {
        return originOffset;
    }

    /**
     * Get the plane first canonical vector.
     * <p>
     * The frame defined by ({@link #getU getU}, {@link #getV getV},
     * {@link #getNormal getNormal}) is a right-handed orthonormalized frame).
     * </p>
     *
     * @return normalized first canonical vector
     * @see #getV
     * @see #getNormal
     */
    public Vector3D getU() {
        return u;
    }

    /**
     * Get the plane second canonical vector.
     * <p>
     * The frame defined by ({@link #getU getU}, {@link #getV getV},
     * {@link #getNormal getNormal}) is a right-handed orthonormalized frame).
     * </p>
     *
     * @return normalized second canonical vector
     * @see #getU
     * @see #getNormal
     */
    public Vector3D getV() {
        return v;
    }

    /**
     * Get the normalized normal vector.
     * <p>
     * The frame defined by {@link #getU()}, {@link #getV()},
     * {@link #getW()} is a right-handed orthonormalized frame.
     * </p>
     *
     * @return normalized normal vector
     * @see #getU()
     * @see #getV()
     * @see #getNormal()
     */
    public Vector3D getW() {
        return w;
    }

    /**
     * Get the normalized normal vector. This method is an alias
     * for {@link #getW()}.
     * <p>
     * The frame defined by {@link #getU()}, {@link #getV()},
     * {@link #getW()} is a right-handed orthonormalized frame.
     * </p>
     *
     * @return normalized normal vector
     * @see #getU()
     * @see #getV()
     * @see #getW()
     */
    public Vector3D getNormal() {
        return w;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D project(final Vector3D point) {
        return toSpace(toSubspace(point));
    }

    /**
     * Project a 3D line onto the plane.
     * @param line the line to project
     * @return the projection of the given line onto the plane.
     */
    public Line3D project(final Line3D line) {
        Vector3D direction = line.getDirection();
        Vector3D projection = w.multiply(direction.dot(w) * (1 / w.normSq()));

        Vector3D projectedLineDirection = direction.subtract(projection);
        Vector3D p1 = project(line.getOrigin());
        Vector3D p2 = p1.add(projectedLineDirection);

        return Line3D.fromPoints(p1,p2, getPrecision());
    }

    /**
     * Build a new reversed version of this plane, with opposite orientation.
     * <p>
     * The new plane frame is chosen in such a way that a 3D point that had
     * {@code (x, y)} in-plane coordinates and {@code z} offset with respect to the
     * plane and is unaffected by the change will have {@code (y, x)} in-plane
     * coordinates and {@code -z} offset with respect to the new plane. This means
     * that the {@code u} and {@code v} vectors returned by the {@link #getU} and
     * {@link #getV} methods are exchanged, and the {@code w} vector returned by the
     * {@link #getNormal} method is reversed.
     * </p>
     * @return a new reversed plane
     */
    @Override
    public Plane reverse() {
        return new Plane(v, u, w.negate(), -originOffset, getPrecision());
    }

    /**
     * Transform a 3D space point into an in-plane point.
     *
     * @param point point of the space (must be a {@link Vector3D} instance)
     * @return in-plane point
     * @see #toSpace
     */
    @Override
    public Vector2D toSubspace(final Vector3D point) {
        return Vector2D.of(point.dot(u), point.dot(v));
    }

    /**
     * Transform an in-plane point into a 3D space point.
     *
     * @param point in-plane point (must be a {@link Vector2D} instance)
     * @return 3D space point
     * @see #toSubSpace
     */
    @Override
    public Vector3D toSpace(final Vector2D point) {
        return Vector3D.linearCombination(point.getX(), u, point.getY(), v, -originOffset, w);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D plusPoint() {
        return pointAt(Vector2D.ZERO, +(Math.floor(getPrecision().getMaxZero()) + 1.0));
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D minusPoint() {
         return pointAt(Vector2D.ZERO, -(Math.floor(getPrecision().getMaxZero()) + 1.0));
    }

    /** {@inheritDoc}
     * <p>This method simply returns the plane origin point, as provided by {@link #getOrigin()}.</p>
     */
    @Override
    public Vector3D onPoint() {
        return getOrigin();
    }

    /** {@inheritDoc} */
    @Override
    public Plane transform(Transform<Vector3D> transform) {
        final Vector3D origin = getOrigin();

        final Vector3D p1 = transform.apply(origin);
        final Vector3D p2 = transform.apply(origin.add(u));
        final Vector3D p3 = transform.apply(origin.add(v));

        return fromPoints(p1, p2, p3, getPrecision());
    }

    /**
     * Rotate the plane around the specified point.
     * <p>
     * The instance is not modified, a new instance is created.
     * </p>
     *
     * @param center   rotation center
     * @param rotation 3-dimensional rotation
     * @return a new plane
     */
    public Plane rotate(final Vector3D center, final QuaternionRotation rotation) {
        final Vector3D delta = getOrigin().subtract(center);
        Vector3D p = center.add(rotation.apply(delta));
        Vector3D normal = rotation.apply(this.w);
        Vector3D wTmp = normal.normalize();

        double originOffsetTmp = -p.dot(wTmp);
        Vector3D uTmp = rotation.apply(this.u);
        Vector3D vTmp = rotation.apply(this.v);

        return new Plane(uTmp, vTmp, wTmp, originOffsetTmp, getPrecision());
    }

    /**
     * Translate the plane by the specified amount.
     * <p>
     * The instance is not modified, a new instance is created.
     * </p>
     *
     * @param translation translation to apply
     * @return a new plane
     */
    public Plane translate(final Vector3D translation) {
        Vector3D p = getOrigin().add(translation);
        Vector3D normal = this.w;
        Vector3D wTmp = normal.normalize();
        double originOffsetTmp = -p.dot(wTmp);

        return new Plane(this.u, this.v, wTmp, originOffsetTmp, getPrecision());
    }

    /**
     * Get one point from the 3D-space.
     *
     * @param inPlane desired in-plane coordinates for the point in the plane
     * @param offset  desired offset for the point
     * @return one point in the 3D-space, with given coordinates and offset relative
     *         to the plane
     */
    public Vector3D pointAt(final Vector2D inPlane, final double offset) {
        return Vector3D.linearCombination(inPlane.getX(), u, inPlane.getY(), v, offset - originOffset, w);
    }

    /**
     * Check if the instance contains another plane.
     * <p>
     * Planes are considered similar if they contain the same points. This does not
     * mean they are equal since they can have opposite normals.
     * </p>
     *
     * @param plane plane to which the instance is compared
     * @return true if the planes are similar
     */
    public boolean contains(final Plane plane) {
        final double angle = w.angle(plane.w);
        final DoublePrecisionContext precision = getPrecision();

        return ((precision.eqZero(angle)) && precision.eq(originOffset, plane.originOffset)) ||
                ((precision.eq(angle, Math.PI)) && precision.eq(originOffset, -plane.originOffset));
    }

    /**
     * Get the intersection of a line with the instance.
     *
     * @param line line intersecting the instance
     * @return intersection point between between the line and the instance (null if
     *         the line is parallel to the instance)
     */
    public Vector3D intersection(final Line3D line) {
        final Vector3D direction = line.getDirection();
        final double dot = w.dot(direction);
        if (getPrecision().eqZero(dot)) {
            return null;
        }
        final Vector3D point = line.toSpace(Vector1D.ZERO);
        final double k = -(originOffset + w.dot(point)) / dot;
        return Vector3D.linearCombination(1.0, point, k, direction);
    }

    /**
     * Build the line shared by the instance and another plane. Null is returned if
     * the lines are parallel.
     *
     * @param other other plane
     * @return line at the intersection of the instance and the other plane, or null
     *      if no such line exists
     */
    public Line3D intersection(final Plane other) {
        final Vector3D direction = w.cross(other.w);
        if (getPrecision().eqZero(direction.norm())) {
            return null;
        }
        final Vector3D point = intersection(this, other, Plane.fromNormal(direction, getPrecision()));
        return Line3D.fromPointAndDirection(point, direction, getPrecision());
    }

    /**
     * Get the intersection point of three planes. Returns null if no unique intersection point
     * exists (ie, there are no intersection points or an infinite number).
     *
     * @param plane1 first plane1
     * @param plane2 second plane2
     * @param plane3 third plane2
     * @return intersection point of the three planes or null if no unique intersection point exists
     */
    public static Vector3D intersection(final Plane plane1, final Plane plane2, final Plane plane3) {

        // coefficients of the three planes linear equations
        final double a1 = plane1.w.getX();
        final double b1 = plane1.w.getY();
        final double c1 = plane1.w.getZ();
        final double d1 = plane1.originOffset;

        final double a2 = plane2.w.getX();
        final double b2 = plane2.w.getY();
        final double c2 = plane2.w.getZ();
        final double d2 = plane2.originOffset;

        final double a3 = plane3.w.getX();
        final double b3 = plane3.w.getY();
        final double c3 = plane3.w.getZ();
        final double d3 = plane3.originOffset;

        // direct Cramer resolution of the linear system
        // (this is still feasible for a 3x3 system)
        final double a23 = b2 * c3 - b3 * c2;
        final double b23 = c2 * a3 - c3 * a2;
        final double c23 = a2 * b3 - a3 * b2;
        final double determinant = a1 * a23 + b1 * b23 + c1 * c23;

        // use the precision context of the first plane to determine equality
        if (plane1.getPrecision().eqZero(determinant)) {
            return null;
        }

        final double r = 1.0 / determinant;
        return Vector3D.of((-a23 * d1 - (c1 * b3 - c3 * b1) * d2 - (c2 * b1 - c1 * b2) * d3) * r,
                (-b23 * d1 - (c3 * a1 - c1 * a3) * d2 - (c1 * a2 - c2 * a1) * d3) * r,
                (-c23 * d1 - (b1 * a3 - b3 * a1) * d2 - (b2 * a1 - b1 * a2) * d3) * r);

    }

    /** {@inheritDoc} */
    @Override
    public ConvexSubPlane span() {
        return ConvexSubPlane.fromConvexArea(this, ConvexArea.full());
    }

    /**
     * Check if the instance contains a point.
     *
     * @param p point to check
     * @return true if p belongs to the plane
     */
    @Override
    public boolean contains(final Vector3D p) {
        return getPrecision().eqZero(offset(p));
    }

    /**
     * Check if the instance contains a line.
     * @param line line to check
     * @return true if line is contained in this plane
     */
    public boolean contains(final Line3D line) {
        return isParallel(line) && contains(line.getOrigin());
    }

    /** Check if the line is parallel to the instance.
     * @param line line to check.
     * @return true if the line is parallel to the instance, false otherwise.
     */
    public boolean isParallel(final Line3D line) {
        final double dot = w.dot(line.getDirection());

        return getPrecision().eqZero(dot);
    }

    /** Check, if the plane is parallel to the instance.
     * @param plane plane to check.
     * @return true if the plane is parallel to the instance, false otherwise.
     */
    public boolean isParallel(final Plane plane) {
        return getPrecision().eqZero(w.cross(plane.w).norm());
    }

    /**
     * Get the offset (oriented distance) of the given plane with respect to this instance. The value
     * closest to zero is returned, which will always be zero if the planes are not parallel.
     * @param plane plane to calculate the offset of
     * @return the offset of the plane with respect to this instance or 0.0 if the planes
     *      are not parallel.
     */
    public double offset(final Plane plane) {
        if (!isParallel(plane)) {
            return 0.0;
        }
        return originOffset + (similarOrientation(plane) ? -plane.originOffset : plane.originOffset);
    }

    /**
     * Get the offset (oriented distance) of the given line with respect to the plane. The value
     * closest to zero is returned, which will always be zero if the line is not parallel to the plane.
     * @param line line to calculate the offset of
     * @return the offset of the line with respect to the plane or 0.0 if the line
     *      is not parallel to the plane.
     */
    public double offset(final Line3D line) {
        if (!isParallel(line)) {
            return 0.0;
        }
        return offset(line.getOrigin());
    }

    /** {@inheritDoc} */
    @Override
    public double offset(final Vector3D point) {
        return point.dot(w) + originOffset;
    }

    /** {@inheritDoc} */
    @Override
    public boolean similarOrientation(final Hyperplane<Vector3D> other) {
        return (((Plane) other).w).dot(w) > 0;
    }


    /** {@inheritDoc}
    *
    * <p>Instances are considered equivalent if they
    * <ul>
    *  <li>contain equal {@link DoublePrecisionContext precision contexts},</li>
    *  <li>have equivalent origins (as evaluated by the precision context), and</li>
    *  <li>have equivalent {@code u} and {@code v} vectors (as evaluated by the precision context)</li>
    * </ul>
    * </p>
    * @param other the point to compare with
    * @return true if this instance should be considered equivalent to the argument
    */
    @Override
    public boolean eq(Plane other) {
        if (this == other) {
            return true;
        }

        final DoublePrecisionContext precision = getPrecision();

        return precision.equals(other.getPrecision()) &&
                getOrigin().eq(other.getOrigin(), precision) &&
                u.eq(other.u, precision) &&
                v.eq(other.v, precision);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(u, v, w, originOffset, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (!(obj instanceof Plane)) {
            return false;
        }

        Plane other = (Plane) obj;

        return Objects.equals(this.u, other.u) &&
                Objects.equals(this.v, other.v) &&
                Objects.equals(this.w, other.w) &&
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
            .append(", u= ")
            .append(u)
            .append(", v= ")
            .append(v)
            .append(", w= ")
            .append(w)
            .append(']');

        return sb.toString();
    }

    /**
     * Build a plane from a point and two (on plane) vectors.
     * @param p the provided point (on plane)
     * @param u u vector (on plane)
     * @param v v vector (on plane)
     * @param precision precision context used to compare floating point values
     * @return a new plane
     * @throws IllegalNormException if the norm of the given values is zero, NaN, or infinite.
     * @throws IllegalArgumentException if the provided vectors are collinear
     */
    public static Plane fromPointAndPlaneVectors(final Vector3D p, final Vector3D u, final Vector3D v, final DoublePrecisionContext precision) {
        Vector3D uNorm = u.normalize();
        Vector3D vNorm = uNorm.orthogonal(v);
        Vector3D wNorm = uNorm.cross(vNorm).normalize();
        double originOffset = -p.dot(wNorm);

        return new Plane(uNorm, vNorm, wNorm, originOffset, precision);
    }

    /**
     * Build a plane from a normal.
     * Chooses origin as point on plane.
     * @param normal    normal direction to the plane
     * @param precision precision context used to compare floating point values
     * @return a new plane
     * @throws IllegalNormException if the norm of the given values is zero, NaN, or infinite.
     */
    public static Plane fromNormal(final Vector3D normal, final DoublePrecisionContext precision) {
        return fromPointAndNormal(Vector3D.ZERO, normal, precision);
    }

    /**
     * Build a plane from a point and a normal.
     *
     * @param p         point belonging to the plane
     * @param normal    normal direction to the plane
     * @param precision precision context used to compare floating point values
     * @return a new plane
     * @throws IllegalNormException if the norm of the given values is zero, NaN, or infinite.
     */
    public static Plane fromPointAndNormal(final Vector3D p, final Vector3D normal, final DoublePrecisionContext precision) {
        Vector3D w = normal.normalize();
        double originOffset = -p.dot(w);

        Vector3D u = w.orthogonal();
        Vector3D v = w.cross(u);

        return new Plane(u, v, w, originOffset, precision);
    }

    /**
     * Build a plane from three points.
     * <p>
     * The plane is oriented in the direction of {@code (p2-p1) ^ (p3-p1)}
     * </p>
     *
     * @param p1        first point belonging to the plane
     * @param p2        second point belonging to the plane
     * @param p3        third point belonging to the plane
     * @param precision precision context used to compare floating point values
     * @return a new plane
     * @throws IllegalNormException if the points do not constitute a plane
     */
    public static Plane fromPoints(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final DoublePrecisionContext precision) {
        return Plane.fromPointAndPlaneVectors(p1, p1.vectorTo(p2), p1.vectorTo(p3), precision);
    }
}
