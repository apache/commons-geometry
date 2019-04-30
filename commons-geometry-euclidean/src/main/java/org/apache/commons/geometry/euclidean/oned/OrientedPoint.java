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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.partition.AbstractHyperplane;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.HyperplaneLocation;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** This class represents a 1D oriented hyperplane.
 *
 * <p>A hyperplane in 1D is a simple point, its orientation being a
 * boolean indicating if the direction is positive or negative.</p>
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class OrientedPoint extends AbstractHyperplane<Vector1D>
    implements Hyperplane<Vector1D>, Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 20190210L;

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
    private OrientedPoint(final Vector1D point, final boolean positiveFacing, final DoublePrecisionContext precision) {
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
    public Vector1D getDirection() {
        return positiveFacing ? Vector1D.ONE : Vector1D.MINUS_ONE;
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

    /** Get an instance with the same location and precision but the opposite
     * direction.
     * @return a copy of this instance with the opposite direction
     */
    public OrientedPoint reverse() {
        return new OrientedPoint(point, !positiveFacing, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D plusPoint() {
        final double scale = Math.floor(getPrecision().getMaxZero()) + 1.0;
        return Vector1D.linearCombination(1.0, point, scale, getDirection());
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D minusPoint() {
        final double scale = Math.floor(getPrecision().getMaxZero()) + 1.0;
        return Vector1D.linearCombination(1.0, point, -scale, getDirection());
    }

    /** {@inheritDoc} */
    @Override
    public Vector1D onPoint() {
        return point;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint transform(final Transform<Vector1D> transform) {
        final Vector1D transformedPoint = transform.apply(point);

        Vector1D transformedDir;
        if (point.isInfinite()) {
            // use a test point to determine if the direction switches or not
            final Vector1D transformedZero = transform.apply(Vector1D.ZERO);
            final Vector1D transformedZeroDir = transform.apply(getDirection());

            transformedDir = transformedZero.vectorTo(transformedZeroDir);
        }
        else {
            final Vector1D transformedPointPlusDir = transform.apply(point.add(getDirection()));
            transformedDir = transformedPoint.vectorTo(transformedPointPlusDir);
        }

        return OrientedPoint.fromPointAndDirection(
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
        }
        else if (cmp < 0) {
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

    /** {@inheritDoc} */
    @Override
    public OrientedPointSubHyperplane span() {
        return new OrientedPointSubHyperplane(this);
    }

    /** Return true if this instance should be considered equal to the argument. Instances
     * are considered equivalent if they
     * <ul>
     *  <li>contain equal {@link DoublePrecisionContext precision contexts},</li>
     *  <li>have equivalent locations as evaluated by the precision context, and</li>
     *  <li>point in the same direction</li>
     * </ul>
     * @param other the point to compare with
     * @return true if this instance should be considered equivalent to the argument
     */
    public boolean eq(OrientedPoint other) {
        return getPrecision().equals(other.getPrecision()) &&
                point.equals(other.point, getPrecision()) &&
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (!(obj instanceof OrientedPoint)) {
            return false;
        }

        OrientedPoint other = (OrientedPoint) obj;

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

    /** Create a new instance from the given location and boolean direction value.
     * @param location the location of the hyperplane
     * @param positiveFacing if true, the hyperplane will face toward positive infinity;
     *      otherwise, it will point toward negative infinity.
     * @param precision precision context used to compare floating point values
     * @return a new instance
     */
    public static OrientedPoint fromLocationAndDirection(final double location, final boolean positiveFacing,
            final DoublePrecisionContext precision) {
        return fromPointAndDirection(Vector1D.of(location), positiveFacing, precision);
    }

    /** Create a new instance from the given point and boolean direction value.
     * @param point the location of the hyperplane
     * @param positiveFacing if true, the hyperplane will face toward positive infinity;
     *      otherwise, it will point toward negative infinity.
     * @param precision precision context used to compare floating point values
     * @return a new instance
     */
    public static OrientedPoint fromPointAndDirection(final Vector1D point, final boolean positiveFacing,
            final DoublePrecisionContext precision) {
        return new OrientedPoint(point, positiveFacing, precision);
    }

    /** Create a new instance from the given point and direction.
     * @param point the location of the hyperplane
     * @param direction the direction of the plus side of the hyperplane
     * @param precision precision context used to compare floating point values
     * @return a new instance oriented in the given direction
     * @throws GeometryValueException if the direction is zero as evaluated by the
     *      given precision context
     */
    public static OrientedPoint fromPointAndDirection(final Vector1D point, final Vector1D direction,
            final DoublePrecisionContext precision) {
        if (direction.isZero(precision)) {
            throw new GeometryValueException("Oriented point direction cannot be zero");
        }

        final boolean positiveFacing = direction.getX() > 0;

        return new OrientedPoint(point, positiveFacing, precision);
    }

    /** Create a new instance at the given point, oriented so that it is facing positive infinity.
     * @param point the location of the hyperplane
     * @param precision precision context used to compare floating point values
     * @return a new instance oriented toward positive infinity
     */
    public static OrientedPoint createPositiveFacing(final Vector1D point, final DoublePrecisionContext precision) {
        return new OrientedPoint(point, true, precision);
    }

    /** Create a new instance at the given location, oriented so that it is facing positive infinity.
     * @param location the location of the hyperplane
     * @param precision precision context used to compare floating point values
     * @return a new instance oriented toward positive infinity
     */
    public static OrientedPoint createPositiveFacing(final double location, final DoublePrecisionContext precision) {
        return new OrientedPoint(Vector1D.of(location), true, precision);
    }

    /** Create a new instance at the given point, oriented so that it is facing negative infinity.
     * @param point the location of the hyperplane
     * @param precision precision context used to compare floating point values
     * @return a new instance oriented toward negative infinity
     */
    public static OrientedPoint createNegativeFacing(final Vector1D point, final DoublePrecisionContext precision) {
        return new OrientedPoint(point, false, precision);
    }

    /** Create a new instance at the given location, oriented so that it is facing negative infinity.
     * @param location the location of the hyperplane
     * @param precision precision context used to compare floating point values
     * @return a new instance oriented toward negative infinity
     */
    public static OrientedPoint createNegativeFacing(final double location, final DoublePrecisionContext precision) {
        return new OrientedPoint(Vector1D.of(location), false, precision);
    }

    /** {@link ConvexSubHyperplane} implementation for Euclidean 1D space. Since there are no subspaces in 1D,
     * this is effectively a stub implementation, its main use being to allow for the correct functioning of
     * partitioning code.
     */
    public static class OrientedPointSubHyperplane implements ConvexSubHyperplane<Vector1D>, Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 1L;

        /** The underlying hyperplane for this instance. */
        private final OrientedPoint hyperplane;

        /** Simple constructor.
         * @param hyperplane underlying hyperplane instance
         */
        public OrientedPointSubHyperplane(final OrientedPoint hyperplane) {
            this.hyperplane = hyperplane;
        }

        /** {@inheritDoc} */
        @Override
        public OrientedPoint getHyperplane() {
            return hyperplane;
        }

        /** {@inheritDoc}
        *
        * <p>This method simply returns false.</p>
        */
        @Override
        public boolean isEmpty() {
            return false;
        }

        /** {@inheritDoc}
         *
         * <p>This method simply returns false.</p>
         */
        @Override
        public boolean isInfinite() {
            return false;
        }

        /** {@inheritDoc}
         *
         *  <p>This method simply returns {@code 0}.</p>
         */
        @Override
        public double getSize() {
            return 0;
        }

        /** {@inheritDoc} */
        @Override
        public Split<Vector1D> split(Hyperplane<Vector1D> splitter) {
            final HyperplaneLocation side = splitter.classify(hyperplane.getPoint());

            OrientedPointSubHyperplane minus = null;
            OrientedPointSubHyperplane plus = null;

            if (side == HyperplaneLocation.MINUS) {
                minus = this;
            }
            else if (side == HyperplaneLocation.PLUS) {
                plus = this;
            }

            return new Split<>(minus, plus);
        }

        /** {@inheritDoc} */
        @Override
        public List<OrientedPointSubHyperplane> toConvex() {
            return Arrays.asList(this);
        }

        /** {@inheritDoc} */
        @Override
        public OrientedPointSubHyperplane transform(Transform<Vector1D> transform) {
            return getHyperplane().transform(transform).span();
        }

        /** {@inheritDoc} */
        @Override
        public OrientedPointSubHyperplaneBuilder builder() {
            return new OrientedPointSubHyperplaneBuilder(this);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return Objects.hashCode(hyperplane);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            else if (!(obj instanceof OrientedPointSubHyperplane)) {
                return false;
            }

            OrientedPointSubHyperplane other = (OrientedPointSubHyperplane) obj;

            return Objects.equals(this.hyperplane, other.hyperplane);
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName())
                .append("[point= ")
                .append(hyperplane.getPoint())
                .append(", direction= ")
                .append(hyperplane.getDirection())
                .append(']');

            return sb.toString();
        }
    }

    /** {@link SubHyperplane.Builder} implementation for Euclidean 1D space. Similar to {@link OrientedPointSubHyperplane},
     * this is effectively a stub implementation since there are no subspaces of 1D space. Its primary use is to allow
     * for the correct functioning of partitioning code.
     */
    public static class OrientedPointSubHyperplaneBuilder implements SubHyperplane.Builder<Vector1D>, Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 20190405L;

        private final OrientedPointSubHyperplane base;

        private OrientedPointSubHyperplaneBuilder(final OrientedPointSubHyperplane base) {
            this.base = base;
        }

        /** {@inheritDoc} */
        @Override
        public void add(SubHyperplane<Vector1D> sub) {
            validateHyperplane(sub);
        }

        /** {@inheritDoc} */
        @Override
        public void add(ConvexSubHyperplane<Vector1D> sub) {
            validateHyperplane(sub);
        }

        /** {@inheritDoc} */
        @Override
        public OrientedPointSubHyperplane build() {
            return base;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return Objects.hashCode(base);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            else if (!(obj instanceof OrientedPointSubHyperplaneBuilder)) {
                return false;
            }

            OrientedPointSubHyperplaneBuilder other = (OrientedPointSubHyperplaneBuilder) obj;

            return Objects.equals(this.base, other.base);
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName())
                .append("[base= ")
                .append(base)
                .append(']');

            return sb.toString();
        }

        /** Validate the given subhyperplane lies on the same hyperplane
         * @param sub
         */
        private void validateHyperplane(final SubHyperplane<Vector1D> sub) {
            final OrientedPoint baseHyper = base.getHyperplane();
            final OrientedPoint inputHyper = (OrientedPoint) sub.getHyperplane();

            if (!baseHyper.eq(inputHyper)) {
                throw new IllegalArgumentException("Argument is not on the same " +
                        "hyperplane. Expected " + baseHyper + " but was " +
                        inputHyper);
            }
        }
    }
}
