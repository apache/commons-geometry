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
import org.apache.commons.geometry.core.partitioning.EmbeddingHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;

/** Extension of the {@link Plane} class that supports embedding of 2D subspaces in the plane.
 * This is accomplished by defining two additional vectors, {@link #getU() u} and {@link #getV() v},
 * that define the {@code x} and {@code y} axes respectively of the embedded subspace. For completeness,
 * an additional vector {@link #getW()} is defined, which is simply an alias for the plane normal.
 * Together, the vectors {@code u}, {@code v}, and {@code w} form a right-handed orthonormal basis.
 *
 * <p>The additional {@code u} and {@code v} vectors are not required to fulfill the contract of
 * {@link org.apache.commons.geometry.core.partitioning.Hyperplane Hyperplane}. Therefore, they
 * are not considered when using instances of this type purely as a hyperplane. For example, the
 * {@link Plane#eq(Plane, DoublePrecisionContext) eq} and
 * {@link Plane#similarOrientation(org.apache.commons.geometry.core.partitioning.Hyperplane) similiarOrientation}
 * methods do not consider them.</p>
 */
public final class EmbeddingPlane extends Plane implements EmbeddingHyperplane<Vector3D, Vector2D> {
    /** First normalized vector of the plane frame (in plane). */
    private final Vector3D.Unit u;

    /** Second normalized vector of the plane frame (in plane). */
    private final Vector3D.Unit v;

    /** Construct a new instance from an orthonormal set of basis vectors and an origin offset.
     * @param u first vector of the basis (in plane)
     * @param v second vector of the basis (in plane)
     * @param w third vector of the basis (plane normal)
     * @param originOffset offset of the origin with respect to the plane.
     * @param precision precision context used for floating point comparisons
     */
    EmbeddingPlane(final Vector3D.Unit u, final Vector3D.Unit v, final Vector3D.Unit w, double originOffset,
            final DoublePrecisionContext precision) {
        super(w, originOffset, precision);

        this.u = u;
        this.v = v;
    }

    /** Get the plane first canonical vector.
     * <p>
     * The frame defined by ({@link #getU u}, {@link #getV v},
     * {@link #getW w}) is a right-handed orthonormalized frame).
     * </p>
     * @return normalized first canonical vector
     * @see #getV
     * @see #getW
     * @see #getNormal
     */
    public Vector3D.Unit getU() {
        return u;
    }

    /** Get the plane second canonical vector.
     * <p>
     * The frame defined by ({@link #getU u}, {@link #getV v},
     * {@link #getW w}) is a right-handed orthonormalized frame).
     * </p>
     * @return normalized second canonical vector
     * @see #getU
     * @see #getW
     * @see #getNormal
     */
    public Vector3D.Unit getV() {
        return v;
    }

    /** Get the plane third canonical vector, ie, the plane normal. This
     * method is simply an alias for {@link #getNormal()}.
     * <p>
     * The frame defined by {@link #getU() u}, {@link #getV() v},
     * {@link #getW() w} is a right-handed orthonormalized frame.
     * </p>
     * @return normalized normal vector
     * @see #getU()
     * @see #getV()
     * @see #getNormal()
     */
    public Vector3D.Unit getW() {
        return getNormal();
    }

    /** Return the current instance.
     */
    @Override
    public EmbeddingPlane getEmbedding() {
        return this;
    }

    /** Transform a 3D space point into an in-plane point.
     * @param point point of the space
     * @return in-plane point
     * @see #toSpace
     */
    @Override
    public Vector2D toSubspace(final Vector3D point) {
        return Vector2D.of(point.dot(u), point.dot(v));
    }

    /** Transform an in-plane point into a 3D space point.
     * @param point in-plane point
     * @return 3D space point
     * @see #toSubspace(Vector3D)
     */
    @Override
    public Vector3D toSpace(final Vector2D point) {
        return Vector3D.linearCombination(
                point.getX(), u,
                point.getY(), v,
                -getOriginOffset(), getNormal());
    }

    /** Get one point from the 3D-space.
     * @param inPlane desired in-plane coordinates for the point in the plane
     * @param offset  desired offset for the point
     * @return one point in the 3D-space, with given coordinates and offset relative
     *         to the plane
     */
    public Vector3D pointAt(final Vector2D inPlane, final double offset) {
        return Vector3D.linearCombination(
                inPlane.getX(), u,
                inPlane.getY(), v,
                offset - getOriginOffset(), getNormal());
    }

    /** Build a new reversed version of this plane, with opposite orientation.
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
    public EmbeddingPlane reverse() {
        return new EmbeddingPlane(v, u, getNormal().negate(), -getOriginOffset(), getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public EmbeddingPlane transform(final Transform<Vector3D> transform) {
        final Vector3D origin = getOrigin();
        final Vector3D plusU = origin.add(u);
        final Vector3D plusV = origin.add(v);

        final Vector3D tOrigin = transform.apply(origin);
        final Vector3D tPlusU = transform.apply(plusU);
        final Vector3D tPlusV = transform.apply(plusV);

        final Vector3D.Unit tU = tOrigin.directionTo(tPlusU);
        final Vector3D.Unit tV = tOrigin.directionTo(tPlusV);
        final Vector3D.Unit tW = tU.cross(tV).normalize();

        final double tOriginOffset = -tOrigin.dot(tW);

        return new EmbeddingPlane(tU, tV, tW, tOriginOffset, getPrecision());
    }

    /** Translate the plane by the specified amount.
     * @param translation translation to apply
     * @return a new plane
     */
    @Override
    public EmbeddingPlane translate(final Vector3D translation) {
        final Vector3D tOrigin = getOrigin().add(translation);

        return Planes.fromPointAndPlaneVectors(tOrigin, u, v, getPrecision());
    }

    /** Rotate the plane around the specified point.
     * @param center rotation center
     * @param rotation 3-dimensional rotation
     * @return a new rotated plane
     */
    @Override
    public EmbeddingPlane rotate(final Vector3D center, final QuaternionRotation rotation) {
        final Vector3D delta = getOrigin().subtract(center);
        final Vector3D tOrigin = center.add(rotation.apply(delta));
        final Vector3D.Unit tU = rotation.apply(u).normalize();
        final Vector3D.Unit tV = rotation.apply(v).normalize();

        return Planes.fromPointAndPlaneVectors(tOrigin, tU, tV, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(getNormal(), getOriginOffset(), u, v, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || obj.getClass() != EmbeddingPlane.class) {
            return false;
        }

        final EmbeddingPlane other = (EmbeddingPlane) obj;

        return Objects.equals(this.getNormal(), other.getNormal()) &&
                Double.compare(this.getOriginOffset(), other.getOriginOffset()) == 0 &&
                Objects.equals(this.u, other.u) &&
                Objects.equals(this.v, other.v) &&
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
            .append(getNormal())
            .append(']');

        return sb.toString();
    }

    /** Get an object containing the current plane transformed by the argument along with a
     * 2D transform that can be applied to subspace points. The subspace transform transforms
     * subspace points such that their 3D location in the transformed plane is the same as their
     * 3D location in the original plane after the 3D transform is applied. For example, consider
     * the code below:
     * <pre>
     *      SubspaceTransform st = plane.subspaceTransform(transform);
     *
     *      Vector2D subPt = Vector2D.of(1, 1);
     *
     *      Vector3D a = transform.apply(plane.toSpace(subPt)); // transform in 3D space
     *      Vector3D b = st.getPlane().toSpace(st.getTransform().apply(subPt)); // transform in 2D space
     * </pre>
     * At the end of execution, the points {@code a} (which was transformed using the original
     * 3D transform) and {@code b} (which was transformed in 2D using the subspace transform)
     * are equivalent.
     *
     * @param transform the transform to apply to this instance
     * @return an object containing the transformed plane along with a transform that can be applied
     *      to subspace points
     * @see #transform(Transform)
     */
    public SubspaceTransform subspaceTransform(final Transform<Vector3D> transform) {
        final Vector3D origin = getOrigin();

        final Vector3D tOrigin = transform.apply(origin);
        final Vector3D tPlusU = transform.apply(origin.add(u));
        final Vector3D tPlusV = transform.apply(origin.add(v));

        final EmbeddingPlane tPlane = Planes.fromPointAndPlaneVectors(
                tOrigin,
                tOrigin.vectorTo(tPlusU),
                tOrigin.vectorTo(tPlusV),
                getPrecision());

        final Vector2D tSubspaceOrigin = tPlane.toSubspace(tOrigin);
        final Vector2D tSubspaceU = tSubspaceOrigin.vectorTo(tPlane.toSubspace(tPlusU));
        final Vector2D tSubspaceV = tSubspaceOrigin.vectorTo(tPlane.toSubspace(tPlusV));

        final AffineTransformMatrix2D subspaceTransform =
                AffineTransformMatrix2D.fromColumnVectors(tSubspaceU, tSubspaceV, tSubspaceOrigin);

        return new SubspaceTransform(tPlane, subspaceTransform);
    }

    /** Class containing a transformed plane instance along with a subspace (2D) transform. The subspace
     * transform produces the equivalent of the 3D transform in 2D.
     */
    public static final class SubspaceTransform {
        /** The transformed plane. */
        private final EmbeddingPlane plane;

        /** The subspace transform instance. */
        private final AffineTransformMatrix2D transform;

        /** Simple constructor.
         * @param plane the transformed plane
         * @param transform 2D transform that can be applied to subspace points
         */
        public SubspaceTransform(final EmbeddingPlane plane, final AffineTransformMatrix2D transform) {
            this.plane = plane;
            this.transform = transform;
        }

        /** Get the transformed plane instance.
         * @return the transformed plane instance
         */
        public EmbeddingPlane getPlane() {
            return plane;
        }

        /** Get the 2D transform that can be applied to subspace points. This transform can be used
         * to perform the equivalent of the 3D transform in 2D space.
         * @return the subspace transform instance
         */
        public AffineTransformMatrix2D getTransform() {
            return transform;
        }
    }
}
