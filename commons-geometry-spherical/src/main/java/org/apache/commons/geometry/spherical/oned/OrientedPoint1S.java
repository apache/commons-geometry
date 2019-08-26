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
package org.apache.commons.geometry.spherical.oned;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.internal.Equivalency;
import org.apache.commons.geometry.core.partitioning.AbstractHyperplane;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing an oriented point on the 1-sphere, meaning an azimuth angle and
 * a direction along the circle (increasing or decreasing angles).
 *
 * <p>This class is guaranteed to be immutable</p>
 */
public final class OrientedPoint1S extends AbstractHyperplane<Point1S>
    implements Equivalency<OrientedPoint1S>, Serializable {

    /** Serializable UID. */
    private static final long serialVersionUID = 20190817L;

    /** Hyperplane location as a point. */
    private final Point1S point;

    /** Hyperplane direction. */
    private final boolean positiveFacing;

    /** Simple constructor.
     * @param point location of the hyperplane
     * @param positiveFacing if true, the hyperplane will point in a positive angular
     *      direction; otherwise, it will point in a negative direction
     * @param precision precision context used to compare floating point values
     */
    private OrientedPoint1S(final Point1S point, final boolean positiveFacing,
            final DoublePrecisionContext precision) {
        super(precision);

        this.point = point;
        this.positiveFacing = positiveFacing;
    }

    /** Get the location of the hyperplane as a point.
     * @return the hyperplane location as a point
     * @see #getAzimuth()
     */
    public Point1S getPoint() {
        return point;
    }

    /**
     * Get the location of the hyperplane as a single value. This is
     * equivalent to {@code pt.getPoint().getAzimuth()}.
     * @return the location of the hyperplane as a single value.
     * @see #getPoint()
     */
    public double getAzimuth() {
        return point.getAzimuth();
    }

    /**
     * Return true if the hyperplane is oriented with its plus
     * side pointing toward increasing angles.
     * @return true if the hyperplane is facing in the direction
     *      of increasing angles
     */
    public boolean isPositiveFacing() {
        return positiveFacing;
    }

    /** {@inheritDoc} */
    @Override
    public Point1S plusPoint() {
        return offsetPoint(true);
    }

    /** {@inheritDoc} */
    @Override
    public Point1S minusPoint() {
        return offsetPoint(false);
    }

    /** {@inheritDoc} */
    @Override
    public Point1S onPoint() {
        return point;
    }

    /** {@inheritDoc}
     *
     * <p>The instances are considered equivalent if they
     * <ol>
     *    <li>have equal precision contexts,</li>
     *    <li>have equivalent point locations as evaluated by the precision
     *          context (points separated by multiples of 2pi are considered equivalent), and
     *    <li>point in the same direction.</li>
     * </ol>
     * </p>
     * @see Point1S#eq(Point1S, DoublePrecisionContext)
     */
    @Override
    public boolean eq(final OrientedPoint1S other) {
        if (this == other) {
            return true;
        }

        final DoublePrecisionContext precision = getPrecision();

        return precision.equals(other.getPrecision()) &&
                point.eq(other.point, precision) &&
                positiveFacing == other.positiveFacing;
    }

    /** {@inheritDoc} */
    @Override
    public double offset(final Point1S pt) {
        final double dist = this.point.signedDistance(pt);

        // Standardize the behavior around the antipode angles:
        // only switch the sign based on the orientation when we're
        // not dealing with a point exactly pi distance away. This will
        // make the antipodes always have an offset of -pi, regardless
        // of the orientation of the point.
        if (Math.abs(dist) < Geometry.PI) {
            return positiveFacing ? +dist : -dist;
        }
        return dist;
    }

    /** {@inheritDoc} */
    @Override
    public Point1S project(final Point1S point) {
        return this.point;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint1S reverse() {
        return new OrientedPoint1S(point, !positiveFacing, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint1S transform(final Transform<Point1S> transform) {
        final Point1S tPoint = transform.apply(point);
        final Point1S tPlus = transform.apply(plusPoint());

        boolean positiveFacing = tPoint.getAzimuth() < tPlus.getAzimuth();

        return OrientedPoint1S.fromPointAndDirection(tPoint, positiveFacing, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public boolean similarOrientation(final Hyperplane<Point1S> other) {
        return positiveFacing == ((OrientedPoint1S) other).positiveFacing;
    }

    /** {@inheritDoc} */
    @Override
    public SubOrientedPoint1S span() {
        return new SubOrientedPoint1S(this);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(point, positiveFacing, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        else if (!(obj instanceof OrientedPoint1S)) {
            return false;
        }

        final OrientedPoint1S other = (OrientedPoint1S) obj;
        return Objects.equals(getPrecision(), other.getPrecision()) &&
                Objects.equals(point, other.point) &&
                positiveFacing == other.positiveFacing;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[point= ")
            .append(point)
            .append(", positiveFacing= ")
            .append(isPositiveFacing())
            .append(']');

        return sb.toString();
    }

    /** Get a point on the plus side or minus side or the hyperplane.
     * @param plusSide if true, a point on the plus side is returned;
     *      otherwise a point on the minus side is returned
     * @return a point on the plus or minus side of the hyperplane
     */
    private Point1S offsetPoint(final boolean plusSide) {
        final double offset = Math.floor(getPrecision().getMaxZero()) + 1.0;
        final double scale = positiveFacing == plusSide ? +1 : -1;
        return Point1S.of((scale * offset) + getAzimuth());
    }

    /** Create a new instance from the given azimuth and direction.
     * @param azimuth azimuth value in radians
     * @param positiveFacing if true, the instance's plus side will be oriented to point toward increasing
     *      angular values; if false, it will point toward decreasing angular value
     * @param precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static OrientedPoint1S fromAzimuthAndDirection(final double azimuth, final boolean positiveFacing,
            final DoublePrecisionContext precision) {
        return fromPointAndDirection(Point1S.of(azimuth), positiveFacing, precision);
    }

    /** Create a new instance from the given point and direction.
     * @param point point representing the location of the hyperplane
     * @param positiveFacing if true, the instance's plus side will be oriented to point toward increasing
     *      angular values; if false, it will point toward decreasing angular value
     * @param precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static OrientedPoint1S fromPointAndDirection(final Point1S pt, final boolean positiveFacing,
            final DoublePrecisionContext precision) {
        return new OrientedPoint1S(pt, positiveFacing, precision);
    }

    /** Create a new instance at the given azimuth, oriented so that the plus side of the hyperplane points
     * toward increasing angular values.
     * @param azimuth azimuth value in radians
     * @param precision precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static OrientedPoint1S createPositiveFacing(final double azimuth, final DoublePrecisionContext precision) {
        return createPositiveFacing(Point1S.of(azimuth), precision);
    }

    /** Create a new instance at the given point, oriented so that the plus side of the hyperplane points
     * toward increasing angular values.
     * @param point point representing the location of the hyperplane
     * @param precision precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static OrientedPoint1S createPositiveFacing(final Point1S pt, final DoublePrecisionContext precision) {
        return fromPointAndDirection(pt, true, precision);
    }

    /** Create a new instance at the given azimuth, oriented so that the plus side of the hyperplane points
     * toward decreasing angular values.
     * @param azimuth azimuth value in radians
     * @param precision precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static OrientedPoint1S createNegativeFacing(final double azimuth, final DoublePrecisionContext precision) {
        return createNegativeFacing(Point1S.of(azimuth), precision);
    }

    /** Create a new instance at the given point, oriented so that the plus side of the hyperplane points
     * toward decreasing angular values.
     * @param point point representing the location of the hyperplane
     * @param precision precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static OrientedPoint1S createNegativeFacing(final Point1S pt, final DoublePrecisionContext precision) {
        return fromPointAndDirection(pt, false, precision);
    }

    /** {@link ConvexSubHyperplane} implementation for spherical 1D space. Since there are no subspaces in 1D,
     * this is effectively a stub implementation, its main use being to allow for the correct functioning of
     * partitioning code.
     */
    public static class SubOrientedPoint1S implements ConvexSubHyperplane<Point1S>, Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 20190825L;

        /** The underlying hyperplane for this instance. */
        private final OrientedPoint1S hyperplane;

        /** Simple constructor.
         * @param hyperplane underlying hyperplane instance
         */
        public SubOrientedPoint1S(final OrientedPoint1S hyperplane) {
            this.hyperplane = hyperplane;
        }

        /** {@inheritDoc} */
        @Override
        public OrientedPoint1S getHyperplane() {
            return hyperplane;
        }

        /** {@inheritDoc}
        *
        * <p>This method simply returns false.</p>
        */
        @Override
        public boolean isFull() {
            return false;
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
        * <p>This method simply returns true.</p>
        */
       @Override
       public boolean isFinite() {
           return true;
       }

        /** {@inheritDoc}
         *
         *  <p>This method simply returns {@code 0}.</p>
         */
        @Override
        public double getSize() {
            return 0;
        }

        /** {@inheritDoc}
         *
         * <p>This method returns {@link RegionLocation#BOUNDARY} if the
         * point is on the hyperplane and {@link RegionLocation#OUTSIDE}
         * otherwise.</p>
         */
        @Override
        public RegionLocation classify(Point1S point) {
            if (hyperplane.contains(point)) {
                return RegionLocation.BOUNDARY;
            }

            return RegionLocation.OUTSIDE;
        }

        /** {@inheritDoc} */
        @Override
        public Point1S closest(Point1S point) {
            return hyperplane.project(point);
        }

        /** {@inheritDoc} */
        @Override
        public Split<SubOrientedPoint1S> split(final Hyperplane<Point1S> splitter) {
            final HyperplaneLocation side = splitter.classify(hyperplane.getPoint());

            SubOrientedPoint1S minus = null;
            SubOrientedPoint1S plus = null;

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
        public List<SubOrientedPoint1S> toConvex() {
            return Arrays.asList(this);
        }

        /** {@inheritDoc} */
        @Override
        public SubOrientedPoint1S transform(final Transform<Point1S> transform) {
            return getHyperplane().transform(transform).span();
        }

        /** {@inheritDoc} */
        @Override
        public SubOrientedPointBuilder1S builder() {
            return new SubOrientedPointBuilder1S(this);
        }

        /** {@inheritDoc} */
        @Override
        public SubOrientedPoint1S reverse() {
            return new SubOrientedPoint1S(hyperplane.reverse());
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.getClass().getSimpleName())
                .append("[point= ")
                .append(hyperplane.getPoint())
                .append(", positiveFacing= ")
                .append(hyperplane.positiveFacing)
                .append(']');

            return sb.toString();
        }
    }

    /** {@link SubHyperplane.Builder} implementation for spherical 1D space. Similar to {@link SubOrientedPoint1S},
     * this is effectively a stub implementation since there are no subspaces of 1D space. Its primary use is to allow
     * for the correct functioning of partitioning code.
     */
    public static class SubOrientedPointBuilder1S implements SubHyperplane.Builder<Point1S>, Serializable {

        /** Serializable UID */
        private static final long serialVersionUID = 20190825L;

        /** Base subhyperplane for the builder. */
        private final SubOrientedPoint1S base;

        /** Construct a new instance using the given base subhyperplane.
         * @param base base subhyperplane for the instance
         */
        private SubOrientedPointBuilder1S(final SubOrientedPoint1S base) {
            this.base = base;
        }

        /** {@inheritDoc} */
        @Override
        public void add(final SubHyperplane<Point1S> sub) {
            validateHyperplane(sub);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final ConvexSubHyperplane<Point1S> sub) {
            validateHyperplane(sub);
        }

        /** {@inheritDoc} */
        @Override
        public SubOrientedPoint1S build() {
            return base;
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
        private void validateHyperplane(final SubHyperplane<Point1S> sub) {
            final OrientedPoint1S baseHyper = base.getHyperplane();
            final OrientedPoint1S inputHyper = (OrientedPoint1S) sub.getHyperplane();

            if (!baseHyper.eq(inputHyper)) {
                throw new IllegalArgumentException("Argument is not on the same " +
                        "hyperplane. Expected " + baseHyper + " but was " +
                        inputHyper);
            }
        }
    }
}
