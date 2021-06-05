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

import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.geometry.euclidean.AbstractBounds;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.numbers.core.Precision;

/** Class containing minimum and maximum points defining a 3D axis-aligned bounding box. Unless otherwise
 * noted, floating point comparisons used in this class are strict, meaning that values are considered equal
 * if and only if they match exactly.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class Bounds3D extends AbstractBounds<Vector3D, Bounds3D> {

    /** Simple constructor. Callers are responsible for ensuring the min is not greater than max.
     * @param min minimum point
     * @param max maximum point
     */
    private Bounds3D(final Vector3D min, final Vector3D max) {
        super(min, max);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasSize(final Precision.DoubleEquivalence precision) {
        final Vector3D diag = getDiagonal();

        return !precision.eqZero(diag.getX()) &&
                !precision.eqZero(diag.getY()) &&
                !precision.eqZero(diag.getZ());
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Vector3D pt) {
        final double x = pt.getX();
        final double y = pt.getY();
        final double z = pt.getZ();

        final Vector3D min = getMin();
        final Vector3D max = getMax();

        return x >= min.getX() && x <= max.getX() &&
                y >= min.getY() && y <= max.getY() &&
                z >= min.getZ() && z <= max.getZ();
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Vector3D pt, final Precision.DoubleEquivalence precision) {
        final double x = pt.getX();
        final double y = pt.getY();
        final double z = pt.getZ();

        final Vector3D min = getMin();
        final Vector3D max = getMax();

        return precision.gte(x, min.getX()) && precision.lte(x, max.getX()) &&
                precision.gte(y, min.getY()) && precision.lte(y, max.getY()) &&
                precision.gte(z, min.getZ()) && precision.lte(z, max.getZ());
    }

    /** {@inheritDoc} */
    @Override
    public boolean intersects(final Bounds3D other) {
        final Vector3D aMin = getMin();
        final Vector3D aMax = getMax();

        final Vector3D bMin = other.getMin();
        final Vector3D bMax = other.getMax();

        return aMin.getX() <= bMax.getX() && aMax.getX() >= bMin.getX() &&
                aMin.getY() <= bMax.getY() && aMax.getY() >= bMin.getY() &&
                aMin.getZ() <= bMax.getZ() && aMax.getZ() >= bMin.getZ();
    }

    /** {@inheritDoc} */
    @Override
    public Bounds3D intersection(final Bounds3D other) {
        if (intersects(other)) {
            final Vector3D aMin = getMin();
            final Vector3D aMax = getMax();

            final Vector3D bMin = other.getMin();
            final Vector3D bMax = other.getMax();

            // get the max of the mins and the mins of the maxes
            final double minX = Math.max(aMin.getX(), bMin.getX());
            final double minY = Math.max(aMin.getY(), bMin.getY());
            final double minZ = Math.max(aMin.getZ(), bMin.getZ());

            final double maxX = Math.min(aMax.getX(), bMax.getX());
            final double maxY = Math.min(aMax.getY(), bMax.getY());
            final double maxZ = Math.min(aMax.getZ(), bMax.getZ());

            return new Bounds3D(
                    Vector3D.of(minX, minY, minZ),
                    Vector3D.of(maxX, maxY, maxZ));
        }

        return null; // no intersection
    }

    /** {@inheritDoc}
     *
     * @throws IllegalArgumentException if any dimension of the bounding box is zero
     *      as evaluated by the given precision context
     */
    @Override
    public Parallelepiped toRegion(final Precision.DoubleEquivalence precision) {
        return Parallelepiped.axisAligned(getMin(), getMax(), precision);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(getMin(), getMax());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Bounds3D)) {
            return false;
        }

        final Bounds3D other = (Bounds3D) obj;

        return getMin().equals(other.getMin()) &&
                getMax().equals(other.getMax());
    }

    /** Construct a new instance from the given points.
     * @param first first point
     * @param more additional points
     * @return a new instance containing the min and max coordinates values from the input points
     */
    public static Bounds3D from(final Vector3D first, final Vector3D... more) {
        final Builder builder = builder();

        builder.add(first);
        builder.addAll(Arrays.asList(more));

        return builder.build();
    }

    /** Construct a new instance from the given points.
     * @param points input points
     * @return a new instance containing the min and max coordinates values from the input points
     */
    public static Bounds3D from(final Iterable<Vector3D> points) {
        final Builder builder = builder();

        builder.addAll(points);

        return builder.build();
    }

    /** Construct a new {@link Builder} instance for creating bounds.
     * @return a new builder instance for creating bounds
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Class used to construct {@link Bounds3D} instances.
     */
    public static final class Builder {

        /** Minimum x coordinate. */
        private double minX = Double.POSITIVE_INFINITY;

        /** Minimum y coordinate. */
        private double minY = Double.POSITIVE_INFINITY;

        /** Minimum z coordinate. */
        private double minZ = Double.POSITIVE_INFINITY;

        /** Maximum x coordinate. */
        private double maxX = Double.NEGATIVE_INFINITY;

        /** Maximum y coordinate. */
        private double maxY = Double.NEGATIVE_INFINITY;

        /** Maximum z coordinate. */
        private double maxZ = Double.NEGATIVE_INFINITY;

        /** Private constructor; instantiate through factory method. */
        private Builder() { }

        /** Add a point to this instance.
         * @param pt point to add
         * @return this instance
         */
        public Builder add(final Vector3D pt) {
            final double x = pt.getX();
            final double y = pt.getY();
            final double z = pt.getZ();

            minX = Math.min(x, minX);
            minY = Math.min(y, minY);
            minZ = Math.min(z, minZ);

            maxX = Math.max(x, maxX);
            maxY = Math.max(y, maxY);
            maxZ = Math.max(z, maxZ);

            return this;
        }

        /** Add a collection of points to this instance.
         * @param pts points to add
         * @return this instance
         */
        public Builder addAll(final Iterable<? extends Vector3D> pts) {
            for (final Vector3D pt : pts) {
                add(pt);
            }

            return this;
        }

        /** Add the min and max points from the given bounds to this instance.
         * @param bounds bounds containing the min and max points to add
         * @return this instance
         */
        public Builder add(final Bounds3D bounds) {
            add(bounds.getMin());
            add(bounds.getMax());

            return this;
        }

        /** Return true if this builder contains valid min and max coordinate values.
         * @return true if this builder contains valid min and max coordinate values
         */
        public boolean hasBounds() {
            return Double.isFinite(minX) &&
                    Double.isFinite(minY) &&
                    Double.isFinite(minZ) &&
                    Double.isFinite(maxX) &&
                    Double.isFinite(maxY) &&
                    Double.isFinite(maxZ);
        }

        /** Create a new {@link Bounds3D} instance from the values in this builder.
         * The builder can continue to be used to create other instances.
         * @return a new bounds instance
         * @throws IllegalStateException if no points were given to the builder or any of the computed
         *      min and max coordinate values are NaN or infinite
         * @see #hasBounds()
         */
        public Bounds3D build() {
            final Vector3D min = Vector3D.of(minX, minY, minZ);
            final Vector3D max = Vector3D.of(maxX, maxY, maxZ);

            if (!hasBounds()) {
                if (Double.isInfinite(minX) && minX > 0 &&
                        Double.isInfinite(maxX) && maxX < 0) {
                    throw new IllegalStateException("Cannot construct bounds: no points given");
                }

                throw new IllegalStateException("Invalid bounds: min= " + min + ", max= " + max);
            }

            return new Bounds3D(min, max);
        }
    }
}
