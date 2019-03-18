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

import org.apache.commons.geometry.core.partitioning.Embedding;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.PolygonsSet;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/**
 * The class represent planes in a three dimensional space.
 */
public final class Plane implements Hyperplane<Vector3D>, Embedding<Vector3D, Vector2D> {

    /** Offset of the origin with respect to the plane. */
    private final double originOffset;

    /** Origin of the plane frame. */
    private final Vector3D origin;

    /** First vector of the plane frame (in plane). */
    private final Vector3D u;

    /** Second vector of the plane frame (in plane). */
    private final Vector3D v;

    /** Third vector of the plane frame (plane normal). */
    private final Vector3D w;

    /** Precision context used to compare floating point numbers. */
    private final DoublePrecisionContext precision;

    public static Plane fromPlaneVectors (final Vector3D u, final Vector3D v, final DoublePrecisionContext precision)
    {
        Vector3D w = u.cross(v);
        double originOffset = 0;
        Vector3D origin = calculateOrigin(w, originOffset);
        return new Plane(u, v, w, origin, originOffset, precision);
    }
    
    public static Plane fromNormal(final Vector3D normal, final DoublePrecisionContext precision)
            throws IllegalArgumentException {
        Vector3D w = normal.normalize();
        double originOffset = 0;
        Vector3D origin = calculateOrigin(w, originOffset);
        Vector3D u = w.orthogonal();
        Vector3D v = w.cross(u);
        return new Plane(u, v, w, origin, originOffset, precision);
    }

    private static Vector3D calculateOrigin(Vector3D w, double originOffset) {
        return w.multiply(-originOffset);
    }


    /**
     * Build a plane from a point and a normal.
     * 
     * @param p         point belonging to the plane
     * @param normal    normal direction to the plane
     * @param precision precision context used to compare floating point values
     * @exception IllegalArgumentException if the normal norm is too small
     */
    public static Plane fromPointAndNormal(final Vector3D p, final Vector3D normal, final DoublePrecisionContext precision)
            throws IllegalArgumentException {
        Vector3D w = normal.normalize();
        double originOffset = -p.dot(w);
        Vector3D origin = calculateOrigin(w, originOffset);
        Vector3D u = w.orthogonal();
        Vector3D v = w.cross(u);
        return new Plane(u, v, w, origin, originOffset, precision);
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
     * @exception IllegalArgumentException if the points do not constitute a plane
     */
    public static Plane fromPoints(final Vector3D p1, final Vector3D p2, final Vector3D p3,
            final DoublePrecisionContext precision) throws IllegalArgumentException {
        return Plane.fromPointAndNormal(p1, p2.subtract(p1).cross(p3.subtract(p1)), precision);
    }

    /**
     * Copy plane.
     * <p>
     * The instance created is completely independent of the original one. A deep
     * copy is used, none of the underlying object are shared.
     * </p>
     * 
     * @param plane plane to copy
     */
    public static Plane of(final Plane plane) {
        return new Plane(plane.u, plane.v, plane.w, plane.origin, plane.originOffset, plane.getPrecision());
    }

    /** 
     * Constructor, made private to prevent inheritance.
     * 
     * Creates a new instance with the given values.
     * @param u u axis
     * @param v v axis
     * @param w w axis
     * @param origin origin
     * @param originOffset offset of the origin
     * @param precision the precision context
     */
    private Plane(Vector3D u, Vector3D v, Vector3D w, Vector3D origin, double originOffset,
            DoublePrecisionContext precision) {
        this.u = u;
        this.v = v;
        this.w = w;
        this.origin = origin;
        this.originOffset = originOffset;
        this.precision = precision;
    }

    /**
     * Copy the instance.
     * <p>
     * The instance created is completely independent of the original one. A deep
     * copy is used, none of the underlying objects are shared (except for immutable
     * objects).
     * </p>
     * 
     * @return a new hyperplane, copy of the instance
     */
    @Deprecated
    @Override
    public Plane copySelf() {
        return new Plane(this.u, this.v, this.w, this.origin, this.originOffset, this.precision);
    }


    /**
     * Get the origin point of the plane frame.
     * <p>
     * The point returned is the orthogonal projection of the 3D-space origin in the
     * plane.
     * </p>
     * 
     * @return the origin point of the plane frame (point closest to the 3D-space
     *         origin)
     */
    public Vector3D getOrigin() {
        return origin;
    }

    
    /**
     *  Gets the offset of the origin with respect to the plane. 
     *  
     *  @return the offset of the origin with respect to the plane. 
     */
    public double getOriginOffset()
    {
        return originOffset;
    }

    
    /**
     * Get the normalized normal vector.
     * <p>
     * The frame defined by ({@link #getU getU}, {@link #getV getV},
     * {@link #getNormal getNormal}) is a right-handed orthonormalized frame).
     * </p>
     * 
     * @return normalized normal vector
     * @see #getU
     * @see #getV
     */
    public Vector3D getNormal() {
        return w;
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
     * Get the normalized normal vector. Alias for getNormal().
     * <p>
     * The frame defined by ({@link #getU getU}, {@link #getV getV},
     * {@link #getNormal getNormal}) is a right-handed orthonormalized frame).
     * </p>
     * 
     * @return normalized normal vector
     * @see #getU
     * @see #getV
     */
    public Vector3D getW() {
        return w;
    }

    
    /** {@inheritDoc} */
    @Override
    public Vector3D project(Vector3D point) {
        return toSpace(toSubSpace(point));
    }

    /** {@inheritDoc} */
    @Override
    public DoublePrecisionContext getPrecision() {
        return precision;
    }

    /**
     * Creates a new reversed version of this plane, with opposite orientation.
     * <p>
     * The new plane frame is chosen in such a way that a 3D point that had
     * {@code (x, y)} in-plane coordinates and {@code z} offset with respect to the
     * plane and is unaffected by the change will have {@code (y, x)} in-plane
     * coordinates and {@code -z} offset with respect to the new plane. This means
     * that the {@code u} and {@code v} vectors returned by the {@link #getU} and
     * {@link #getV} methods are exchanged, and the {@code w} vector returned by the
     * {@link #getNormal} method is reversed.
     * </p>
     */
    public Plane reverse() {
        final Vector3D tmp = u;
        Vector3D u = v;
        Vector3D v = tmp;
        Vector3D w = this.w.negate();
        double originOffset = -this.originOffset;
        Vector3D origin = calculateOrigin(w, originOffset);
        return new Plane(u, v, w, origin, originOffset, this.precision);
    }

    /**
     * Transform a 3D space point into an in-plane point.
     * 
     * @param point point of the space (must be a {@link Vector3D} instance)
     * @return in-plane point
     * @see #toSpace
     */
    @Override
    public Vector2D toSubSpace(final Vector3D point) {
        Vector3D vec = point;
        return Vector2D.of(vec.dot(u), vec.dot(v));
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

    /**
     * Get one point from the 3D-space.
     * 
     * @param inPlane desired in-plane coordinates for the point in the plane
     * @param offset  desired offset for the point
     * @return one point in the 3D-space, with given coordinates and offset relative
     *         to the plane
     */
    public Vector3D getPointAt(final Vector2D inPlane, final double offset) {
        return Vector3D.linearCombination(inPlane.getX(), u, inPlane.getY(), v, offset - originOffset, w);
    }

    /**
     * Check if the instance is similar to another plane.
     * <p>
     * Planes are considered similar if they contain the same points. This does not
     * mean they are equal since they can have opposite normals.
     * </p>
     * 
     * @param plane plane to which the instance is compared
     * @return true if the planes are similar
     */
    public boolean isSimilarTo(final Plane plane) {
        final double angle = w.angle(plane.w);

        return ((precision.eqZero(angle)) && precision.eq(originOffset, plane.originOffset))
                || ((precision.eq(angle, Math.PI)) && precision.eq(originOffset, -plane.originOffset));
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
        final Vector3D delta = origin.subtract(center);
        Vector3D p = center.add(rotation.apply(delta));
        Vector3D normal = rotation.apply(this.w);
        Vector3D w = normal.normalize();
        double originOffset = -p.dot(w);
        Vector3D origin = calculateOrigin(w, originOffset);
        Vector3D u = rotation.apply(this.u);
        Vector3D v = rotation.apply(this.v);
        return new Plane(u, v, w, origin, originOffset, this.precision);
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
        Vector3D p = origin.add(translation);
        Vector3D normal = this.w;
        Vector3D w = normal.normalize();
        double originOffset = -p.dot(w);
        Vector3D origin = calculateOrigin(w, originOffset);
        return new Plane(this.u, this.v, w, origin, originOffset, this.precision);
    }

    /**
     * Get the intersection of a line with the instance.
     * 
     * @param line line intersecting the instance
     * @return intersection point between between the line and the instance (null if
     *         the line is parallel to the instance)
     */
    public Vector3D intersection(final Line line) {
        final Vector3D direction = line.getDirection();
        final double dot = w.dot(direction);
        if (precision.eqZero(dot)) {
            return null;
        }
        final Vector3D point = line.toSpace(Vector1D.ZERO);
        final double k = -(originOffset + w.dot(point)) / dot;
        return Vector3D.linearCombination(1.0, point, k, direction);
    }

    /**
     * Build the line shared by the instance and another plane.
     * 
     * @param other other plane
     * @return line at the intersection of the instance and the other plane (really
     *         a {@link Line Line} instance)
     */
    public Line intersection(final Plane other) {
        final Vector3D direction = w.cross(other.w);
        if (precision.eqZero(direction.norm())) {
            return null;
        }
        final Vector3D point = intersection(this, other, Plane.fromNormal(direction, precision));
        return new Line(point, point.add(direction), precision);
    }

    /**
     * Get the intersection point of three planes.
     * 
     * @param plane1 first plane1
     * @param plane2 second plane2
     * @param plane3 third plane2
     * @return intersection point of three planes, null if some planes are parallel
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
        if (Math.abs(determinant) < 1.0e-10) {
            return null;
        }

        final double r = 1.0 / determinant;
        return Vector3D.of((-a23 * d1 - (c1 * b3 - c3 * b1) * d2 - (c2 * b1 - c1 * b2) * d3) * r,
                (-b23 * d1 - (c3 * a1 - c1 * a3) * d2 - (c1 * a2 - c2 * a1) * d3) * r,
                (-c23 * d1 - (b1 * a3 - b3 * a1) * d2 - (b2 * a1 - b1 * a2) * d3) * r);

    }

    /**
     * Build a region covering the whole hyperplane.
     * 
     * @return a region covering the whole hyperplane
     */
    @Override
    public SubPlane wholeHyperplane() {
        return new SubPlane(this, new PolygonsSet(precision));
    }

    /**
     * Build a region covering the whole space.
     * 
     * @return a region containing the instance (really a {@link PolyhedronsSet
     *         PolyhedronsSet} instance)
     */
    @Override
    public PolyhedronsSet wholeSpace() {
        return new PolyhedronsSet(precision);
    }

    /**
     * Check if the instance contains a point.
     * 
     * @param p point to check
     * @return true if p belongs to the plane
     */
    public boolean contains(final Vector3D p) {
        return precision.eqZero(getOffset(p));
    }

    /**
     * Get the offset (oriented distance) of a parallel plane.
     * <p>
     * This method should be called only for parallel planes otherwise the result is
     * not meaningful.
     * </p>
     * <p>
     * The offset is 0 if both planes are the same, it is positive if the plane is
     * on the plus side of the instance and negative if it is on the minus side,
     * according to its natural orientation.
     * </p>
     * 
     * @param plane plane to check
     * @return offset of the plane
     */
    public double getOffset(final Plane plane) {
        return originOffset + (sameOrientationAs(plane) ? -plane.originOffset : plane.originOffset);
    }

    /**
     * Get the offset (oriented distance) of a point.
     * <p>
     * The offset is 0 if the point is on the underlying hyperplane, it is positive
     * if the point is on one particular side of the hyperplane, and it is negative
     * if the point is on the other side, according to the hyperplane natural
     * orientation.
     * </p>
     * 
     * @param point point to check
     * @return offset of the point
     */
    @Override
    public double getOffset(final Vector3D point) {
        return point.dot(w) + originOffset;
    }

    /**
     * Check if the instance has the same orientation as another hyperplane.
     * 
     * @param other other hyperplane to check against the instance
     * @return true if the instance and the other hyperplane have the same
     *         orientation
     */
    @Override
    public boolean sameOrientationAs(final Hyperplane<Vector3D> other) {
        return (((Plane) other).w).dot(w) > 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Plane [originOffset=" + originOffset + ", origin=" + origin + ", u=" + u + ", v=" + v + ", w=" + w
                + ", precision=" + precision + "]";
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(origin, originOffset, u, v, w);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Plane other = (Plane) obj;
        return Objects.equals(origin, other.origin)
                && Double.doubleToLongBits(originOffset) == Double.doubleToLongBits(other.originOffset)
                && Objects.equals(u, other.u) && Objects.equals(v, other.v) && Objects.equals(w, other.w);
    }
}
