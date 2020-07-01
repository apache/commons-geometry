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
package org.apache.commons.geometry.euclidean.oned;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.AbstractHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** This class represents a 1D oriented hyperplane.
 *
 * <p>A hyperplane in 1D is a simple point, its orientation being a
 * boolean indicating if the direction is positive or negative.</p>
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see OrientedPoints
 */
public final class OrientedPoint extends AbstractHyperplane<Vector1D> {
    /** Hyperplane location as a point. */
    private final Vector1D point;

    /** Hyperplane direction. */
    private final boolean positiveFacing;

    /** Simple constructor.
     * @param point location of the hyperplane
     * @param positiveFacing if true, the hyperplane will face toward positive infinity;
     *      otherwise, it will point toward negative infinity.
     * @param precision precision context used to compare floating point values
     */
    OrientedPoint(final Vector1D point, final boolean positiveFacing, final DoublePrecisionContext precision) {
        super(precision);

        this.point = point;
        this.positiveFacing = positiveFacing;
    }

    /** Get the location of the hyperplane as a point.
     * @return the hyperplane location as a point
     * @see #getLocation()
     */
    public Vector1D getPoint() {
        return point;
    }

    /**
     * Get the location of the hyperplane as a single value. This is
     * equivalent to {@code pt.getPoint().getX()}.
     * @return the location of the hyperplane as a single value.
     * @see #getPoint()
     */
    public double getLocation() {
        return point.getX();
    }

    /** Get the direction of the hyperplane's plus side.
     * @return the hyperplane direction
     */
    public Vector1D.Unit getDirection() {
        return positiveFacing ? Vector1D.Unit.PLUS : Vector1D.Unit.MINUS;
    }

    /**
     * Return true if the hyperplane is oriented with its plus
     * side in the direction of positive infinity.
     * @return true if the hyperplane is facing toward positive
     *      infinity
     */
    public boolean isPositiveFacing() {
        return positiveFacing;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint reverse() {
        return new OrientedPoint(point, !positiveFacing, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint transform(final Transform<Vector1D> transform) {
        final Vector1D transformedPoint = transform.apply(point);

        final Vector1D transformedDir;
        if (point.isInfinite()) {
            // use a test point to determine if the direction switches or not
            final Vector1D transformedZero = transform.apply(Vector1D.ZERO);
            final Vector1D transformedZeroDir = transform.apply(getDirection());

            transformedDir = transformedZero.vectorTo(transformedZeroDir);
        } else {
            final Vector1D transformedPointPlusDir = transform.apply(point.add(getDirection()));
            transformedDir = transformedPoint.vectorTo(transformedPointPlusDir);
        }

        return OrientedPoints.fromPointAndDirection(
                    transformedPoint,
                    transformedDir,
                    getPrecision()
                );
    }

    /** {@inheritDoc} */
    @Override
    public double offset(final Vector1D pt) {
        return offset(pt.getX());
    }

    /** Compute the offset of the given number line location. This is
     * a convenience overload of {@link #offset(Vector1D)} for use in
     * one dimension.
     * @param location the number line location to compute the offset for
     * @return the offset of the location from the instance
     */
    public double offset(final double location) {
        final double delta = location - point.getX();
        return positiveFacing ? delta : -delta;
    }

    /** {@inheritDoc} */
    @Override
    public HyperplaneLocation classify(final Vector1D pt) {
        return classify(pt.getX());
    }

    /** Classify the number line location with respect to the instance.
     * This is a convenience overload of {@link #classify(Vector1D)} for
     * use in one dimension.
     * @param location the number line location to classify
     * @return the classification of the number line location with respect
     *      to this instance
     */
    public HyperplaneLocation classify(final double location) {
        final double offsetValue = offset(location);

        final int cmp = getPrecision().sign(offsetValue);
        if (cmp > 0) {
            return HyperplaneLocation.PLUS;
        } else if (cmp < 0) {
            return HyperplaneLocation.MINUS;
        }
        return HyperplaneLocation.ON;
    }

    /** {@inheritDoc} */
    @Override
    public boolean similarOrientation(final Hyperplane<Vector1D> other) {
        return positiveFacing == ((OrientedPoint) other).positiveFacing;
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D project(final Vector1D pt) {
        return this.point;
    }

    /** {@inheritDoc}
     *
     * <p>Since there are no subspaces in 1D, this method effectively returns a stub implementation of
     * {@link HyperplaneConvexSubset}, the main purpose of which is to support the proper functioning
     * of the partitioning code.</p>
     */
    @Override
    public HyperplaneConvexSubset<Vector1D> span() {
        return new OrientedPointConvexSubset(this);
    }

    /** Return true if this instance should be considered equivalent to the argument, using the
     * given precision context for comparison.
     * <p>Instances are considered equivalent if they
     * <ol>
     *      <li>have equivalent locations and</li>
     *      <li>point in the same direction.</li>
     * </ol>
     * @param other the point to compare with
     * @param precision precision context to use for the comparison
     * @return true if this instance should be considered equivalent to the argument
     * @see Vector1D#eq(Vector1D, DoublePrecisionContext)
     */
    public boolean eq(final OrientedPoint other, final DoublePrecisionContext precision) {
        return point.eq(other.point, precision) &&
                positiveFacing == other.positiveFacing;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;

        int result = 1;
        result = (prime * result) + Objects.hashCode(point);
        result = (prime * result) + Boolean.hashCode(positiveFacing);
        result = (prime * result) + Objects.hashCode(getPrecision());

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof OrientedPoint)) {
            return false;
        }

        final OrientedPoint other = (OrientedPoint) obj;

        return Objects.equals(this.point, other.point) &&
                this.positiveFacing == other.positiveFacing &&
                Objects.equals(this.getPrecision(), other.getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[point= ")
            .append(point)
            .append(", direction= ")
            .append(getDirection())
            .append(']');

        return sb.toString();
    }

    /** {@link HyperplaneConvexSubset} implementation for Euclidean 1D space. Since there are no subspaces in 1D,
     * this is effectively a stub implementation, its main use being to allow for the correct functioning of
     * partitioning code.
     */
    private static class OrientedPointConvexSubset implements HyperplaneConvexSubset<Vector1D> {
        /** The underlying hyperplane for this instance. */
        private final OrientedPoint hyperplane;

        /** Simple constructor.
         * @param hyperplane underlying hyperplane instance
         */
        OrientedPointConvexSubset(final OrientedPoint hyperplane) {
            this.hyperplane = hyperplane;
        }

        /** {@inheritDoc} */
        @Override
        public OrientedPoint getHyperplane() {
            return hyperplane;
        }

        /** {@inheritDoc}
        *
        * <p>This method always returns {@code false}.</p>
        */
        @Override
        public boolean isFull() {
            return false;
        }

        /** {@inheritDoc}
        *
        * <p>This method always returns {@code false}.</p>
        */
        @Override
        public boolean isEmpty() {
            return false;
        }

        /** {@inheritDoc}
         *
         * <p>This method always returns {@code false}.</p>
         */
        @Override
        public boolean isInfinite() {
            return false;
        }

        /** {@inheritDoc}
        *
        * <p>This method always returns {@code true}.</p>
        */
        @Override
        public boolean isFinite() {
            return true;
        }

        /** {@inheritDoc}
         *
         *  <p>This method always returns {@code 0}.</p>
         */
        @Override
        public double getSize() {
            return 0;
        }

        /** {@inheritDoc}
        *
        *  <p>This method returns the point for the defining hyperplane.</p>
        */
        @Override
        public Vector1D getCentroid() {
            return hyperplane.getPoint();
        }

        /** {@inheritDoc}
         *
         * <p>This method returns {@link RegionLocation#BOUNDARY} if the
         * point is on the hyperplane and {@link RegionLocation#OUTSIDE}
         * otherwise.</p>
         */
        @Override
        public RegionLocation classify(final Vector1D point) {
            if (hyperplane.contains(point)) {
                return RegionLocation.BOUNDARY;
            }

            return RegionLocation.OUTSIDE;
        }

        /** {@inheritDoc} */
        @Override
        public Vector1D closest(final Vector1D point) {
            return hyperplane.project(point);
        }

        /** {@inheritDoc} */
        @Override
        public Split<OrientedPointConvexSubset> split(final Hyperplane<Vector1D> splitter) {
            final HyperplaneLocation side = splitter.classify(hyperplane.getPoint());

            OrientedPointConvexSubset minus = null;
            OrientedPointConvexSubset plus = null;

            if (side == HyperplaneLocation.MINUS) {
                minus = this;
            } else if (side == HyperplaneLocation.PLUS) {
                plus = this;
            }

            return new Split<>(minus, plus);
        }

        /** {@inheritDoc} */
        @Override
        public List<OrientedPointConvexSubset> toConvex() {
            return Collections.singletonList(this);
        }

        /** {@inheritDoc} */
        @Override
        public OrientedPointConvexSubset transform(final Transform<Vector1D> transform) {
            return new OrientedPointConvexSubset(getHyperplane().transform(transform));
        }

        /** {@inheritDoc} */
        @Override
        public OrientedPointConvexSubset reverse() {
            return new OrientedPointConvexSubset(hyperplane.reverse());
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName())
                .append("[hyperplane= ")
                .append(hyperplane)
                .append(']');

            return sb.toString();
        }
    }
}
