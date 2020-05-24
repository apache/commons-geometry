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
import org.apache.commons.geometry.core.partitioning.AbstractHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;

/** Class representing a plane in 3 dimensional Euclidean space. Each plane is defined by a
 * {@link #getNormal() normal} and an {@link #getOriginOffset() origin offset}. If \(\vec{n}\) is the plane normal,
 * \(d\) is the origin offset, and \(p\) and \(q\) are any points in the plane, then the following are true:
 * <ul>
 *  <li>\(\lVert \vec{n} \rVert\) = 1</li>
 *  <li>\(\vec{n} \cdot (p - q) = 0\)</li>
 *  <li>\(d = - (\vec{n} \cdot q)\)</li>
 *  </ul>
 *  In other words, the normal is a unit vector such that the dot product of the normal and the difference of
 *  any two points in the plane is always equal to \(0\). Similarly, the {@code origin offset} is equal to the
 *  negation of the dot product of the normal and any point in the plane. The projection of the origin onto the
 *  plane (given by {@link #getOrigin()}), is computed as \(-d \vec{n}\).
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see Planes
 */
public class Plane extends AbstractHyperplane<Vector3D> implements Hyperplane<Vector3D> {

    /** Plane normal. */
    private final Vector3D.Unit normal;

    /** Offset of the origin with respect to the plane. */
    private final double originOffset;

    /** Construct a plane from its component parts.
     * @param normal unit normal vector
     * @param originOffset offset of the origin with respect to the plane
     * @param precision precision context used to compare floating point values
     */
    Plane(final Vector3D.Unit normal, double originOffset,
            final DoublePrecisionContext precision) {

        super(precision);

        this.normal = normal;
        this.originOffset = originOffset;
    }

    /** Get the orthogonal projection of the 3D-space origin in the plane.
     * @return the origin point of the plane frame (point closest to the 3D-space
     *         origin)
     */
    public Vector3D getOrigin() {
        return normal.multiply(-originOffset);
    }

    /** Get the offset of the spatial origin ({@code 0, 0, 0}) with respect to the plane.
     * @return the offset of the origin with respect to the plane.
     */
    public double getOriginOffset() {
        return originOffset;
    }

    /** Get the plane normal vector.
     * @return plane normal vector
     */
    public Vector3D.Unit getNormal() {
        return normal;
    }

    /** Return an {@link EmbeddingPlane} instance suitable for embedding 2D geometric objects
     * into this plane. Returned instances are guaranteed to be equal between invocations.
     * @return a plane instance suitable for embedding 2D subspaces
     */
    public EmbeddingPlane getEmbedding() {
        final Vector3D.Unit u = normal.orthogonal();
        final Vector3D.Unit v = normal.cross(u).normalize();

        return new EmbeddingPlane(u, v, normal, originOffset, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public double offset(final Vector3D point) {
        return point.dot(normal) + originOffset;
    }

    /** Get the offset (oriented distance) of the given line with respect to the plane. The value
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

    /** Get the offset (oriented distance) of the given plane with respect to this instance. The value
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

    /** Check if the instance contains a point.
     * @param p point to check
     * @return true if p belongs to the plane
     */
    @Override
    public boolean contains(final Vector3D p) {
        return getPrecision().eqZero(offset(p));
    }

    /** Check if the instance contains a line.
     * @param line line to check
     * @return true if line is contained in this plane
     */
    public boolean contains(final Line3D line) {
        return isParallel(line) && contains(line.getOrigin());
    }

    /** Check if the instance contains another plane. Planes are considered similar if they contain
     * the same points. This does not mean they are equal since they can have opposite normals.
     * @param plane plane to which the instance is compared
     * @return true if the planes are similar
     */
    public boolean contains(final Plane plane) {
        final double angle = normal.angle(plane.normal);
        final DoublePrecisionContext precision = getPrecision();

        return ((precision.eqZero(angle)) && precision.eq(originOffset, plane.originOffset)) ||
                ((precision.eq(angle, Math.PI)) && precision.eq(originOffset, -plane.originOffset));
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D project(final Vector3D point) {
        return getOrigin().add(point.reject(normal));
    }

    /** Project a 3D line onto the plane.
     * @param line the line to project
     * @return the projection of the given line onto the plane.
     */
    public Line3D project(final Line3D line) {
        final Vector3D direction = line.getDirection();
        final Vector3D projection = normal.multiply(direction.dot(normal) * (1 / normal.normSq()));

        final Vector3D projectedLineDirection = direction.subtract(projection);
        final Vector3D p1 = project(line.getOrigin());
        final Vector3D p2 = p1.add(projectedLineDirection);

        return Lines3D.fromPoints(p1, p2, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public PlaneConvexSubset span() {
        return Planes.subsetFromConvexArea(getEmbedding(), ConvexArea.full());
    }

    /** Check if the line is parallel to the instance.
     * @param line line to check.
     * @return true if the line is parallel to the instance, false otherwise.
     */
    public boolean isParallel(final Line3D line) {
        final double dot = normal.dot(line.getDirection());

        return getPrecision().eqZero(dot);
    }

    /** Check if the plane is parallel to the instance.
     * @param plane plane to check.
     * @return true if the plane is parallel to the instance, false otherwise.
     */
    public boolean isParallel(final Plane plane) {
        return getPrecision().eqZero(normal.cross(plane.normal).norm());
    }

    /** {@inheritDoc} */
    @Override
    public boolean similarOrientation(final Hyperplane<Vector3D> other) {
        return (((Plane) other).normal).dot(normal) > 0;
    }

    /** Get the intersection of a line with this plane.
     * @param line line intersecting the instance
     * @return intersection point between between the line and the instance (null if
     *         the line is parallel to the instance)
     */
    public Vector3D intersection(final Line3D line) {
        final Vector3D direction = line.getDirection();
        final double dot = normal.dot(direction);

        if (getPrecision().eqZero(dot)) {
            return null;
        }

        final Vector3D point = line.pointAt(0);
        final double k = -(originOffset + normal.dot(point)) / dot;

        return Vector3D.linearCombination(
                1.0, point,
                k, direction);
    }

    /** Get the line formed by the intersection of this instance with the given plane.
     * The returned line lies in both planes and points in the direction of
     * the cross product <code>n<sub>1</sub> x n<sub>2</sub></code>, where <code>n<sub>1</sub></code>
     * is the normal of the current instance and <code>n<sub>2</sub></code> is the normal
     * of the argument.
     *
     * <p>Null is returned if the planes are parallel.</p>
     *
     * @param other other plane
     * @return line at the intersection of the instance and the other plane, or null
     *      if no such line exists
     */
    public Line3D intersection(final Plane other) {
        final Vector3D direction = normal.cross(other.normal);

        if (getPrecision().eqZero(direction.norm())) {
            return null;
        }

        final Vector3D point = intersection(this, other, Planes.fromNormal(direction, getPrecision()));

        return Lines3D.fromPointAndDirection(point, direction, getPrecision());
    }

    /** Build a new reversed version of this plane, with opposite orientation.
     * @return a new reversed plane
     */
    @Override
    public Plane reverse() {
        return new Plane(normal.negate(), -originOffset, getPrecision());
    }

    /** {@inheritDoc}
     *
     * <p>Instances are transformed by selecting 3 representative points from the
     * plane, transforming them, and constructing a new plane from the transformed points.
     * Since the normal is not transformed directly, but rather is constructed new from the
     * transformed points, the relative orientations of points in the plane are preserved,
     * even for transforms that do not
     * {@link Transform#preservesOrientation() preserve orientation}. The example below shows
     * a plane being transformed by a non-orientation-preserving transform. The normal of the
     * transformed plane retains its counterclockwise relationship to the points in the plane,
     * in contrast with the normal that is transformed directly by the transform.
     * </p>
     * <pre>
     * // construct a plane from 3 points; the normal will be selected such that the
     * // points are ordered counterclockwise when looking down the plane normal.
     * Vector3D p1 = Vector3D.of(0, 0, 0);
     * Vector3D p2 = Vector3D.of(+1, 0, 0);
     * Vector3D p3 = Vector3D.of(0, +1, 0);
     *
     * Plane plane = Planes.fromPoints(p1, p2, p3, precision); // normal is (0, 0, +1)
     *
     * // create a transform that negates all x-values; this transform does not
     * // preserve orientation, i.e. it will convert a right-handed system into a left-handed
     * // system and vice versa
     * AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(-1, 1,  1);
     *
     * // transform the plane
     * Plane transformedPlane = plane.transform(transform);
     *
     * // the plane normal is oriented such that transformed points are still ordered
     * // counterclockwise when looking down the plane normal; since the point (1, 0, 0) has
     * // now become (-1, 0, 0), the normal has flipped to (0, 0, -1)
     * transformedPlane.getNormal();
     *
     * // directly transform the original plane normal; the normal is unchanged by the transform
     * // since the target space of the transform is left-handed
     * AffineTransformMatrix3D normalTransform = transform.normalTransform();
     * Vector3D directlyTransformedNormal = normalTransform.apply(plane.getNormal()); // (0, 0, +1)
     * </pre>
     */
    @Override
    public Plane transform(final Transform<Vector3D> transform) {
        // create 3 representation points lying on the plane, transform them,
        // and use the transformed points to create a new plane

        final Vector3D u = normal.orthogonal();
        final Vector3D v = normal.cross(u);

        final Vector3D p1 = getOrigin();
        final Vector3D p2 = p1.add(u);
        final Vector3D p3 = p1.add(v);

        final Vector3D t1 = transform.apply(p1);
        final Vector3D t2 = transform.apply(p2);
        final Vector3D t3 = transform.apply(p3);

        return Planes.fromPoints(t1, t2, t3, getPrecision());
    }

    /** Translate the plane by the specified amount.
     * @param translation translation to apply
     * @return a new plane
     */
    public Plane translate(final Vector3D translation) {
        final Vector3D tOrigin = getOrigin().add(translation);

        return Planes.fromPointAndNormal(tOrigin, normal, getPrecision());
    }

    /** Rotate the plane around the specified point.
     * @param center rotation center
     * @param rotation 3-dimensional rotation
     * @return a new plane
     */
    public Plane rotate(final Vector3D center, final QuaternionRotation rotation) {
        final Vector3D delta = getOrigin().subtract(center);
        final Vector3D tOrigin = center.add(rotation.apply(delta));

        // we can directly apply the rotation to the normal since it will transform
        // it properly (there is no translation or scaling involved)
        final Vector3D.Unit tNormal = rotation.apply(normal).normalize();

        return Planes.fromPointAndNormal(tOrigin, tNormal, getPrecision());
    }

    /** Return true if this instance should be considered equivalent to the argument, using the
     * given precision context for comparison. Instances are considered equivalent if they contain
     * the same points, which is determined by comparing the plane {@code origins} and {@code normals}.
     * @param other the point to compare with
     * @param precision precision context to use for the comparison
     * @return true if this instance should be considered equivalent to the argument
     * @see Vector3D#eq(Vector3D, DoublePrecisionContext)
     */
    public boolean eq(final Plane other, final DoublePrecisionContext precision) {
        return getOrigin().eq(other.getOrigin(), precision) &&
                normal.eq(other.normal, precision);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(normal, originOffset, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        final Plane other = (Plane) obj;

        return Objects.equals(this.normal, other.normal) &&
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
            .append(", normal= ")
            .append(normal)
            .append(']');

        return sb.toString();
    }

    /** Get the intersection point of three planes. Returns null if no unique intersection point
     * exists (ie, there are no intersection points or an infinite number).
     * @param plane1 first plane1
     * @param plane2 second plane2
     * @param plane3 third plane2
     * @return intersection point of the three planes or null if no unique intersection point exists
     */
    public static Vector3D intersection(final Plane plane1, final Plane plane2, final Plane plane3) {

        // coefficients of the three planes linear equations
        final double a1 = plane1.normal.getX();
        final double b1 = plane1.normal.getY();
        final double c1 = plane1.normal.getZ();
        final double d1 = plane1.originOffset;

        final double a2 = plane2.normal.getX();
        final double b2 = plane2.normal.getY();
        final double c2 = plane2.normal.getZ();
        final double d2 = plane2.originOffset;

        final double a3 = plane3.normal.getX();
        final double b3 = plane3.normal.getY();
        final double c3 = plane3.normal.getZ();
        final double d3 = plane3.originOffset;

        // direct Cramer resolution of the linear system
        // (this is still feasible for a 3x3 system)
        final double a23 = (b2 * c3) - (b3 * c2);
        final double b23 = (c2 * a3) - (c3 * a2);
        final double c23 = (a2 * b3) - (a3 * b2);
        final double determinant = (a1 * a23) + (b1 * b23) + (c1 * c23);

        // use the precision context of the first plane to determine equality
        if (plane1.getPrecision().eqZero(determinant)) {
            return null;
        }

        final double r = 1.0 / determinant;
        return Vector3D.of((-a23 * d1 - (c1 * b3 - c3 * b1) * d2 - (c2 * b1 - c1 * b2) * d3) * r,
                (-b23 * d1 - (c3 * a1 - c1 * a3) * d2 - (c1 * a2 - c2 * a1) * d3) * r,
                (-c23 * d1 - (b1 * a3 - b3 * a1) * d2 - (b2 * a1 - b1 * a2) * d3) * r);
    }
}
