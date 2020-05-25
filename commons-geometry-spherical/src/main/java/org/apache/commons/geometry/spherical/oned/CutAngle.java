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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.AbstractHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing an oriented point in 1-dimensional spherical space,
 * meaning an azimuth angle and a direction (increasing or decreasing angles)
 * along the circle.
 *
 * <p>Hyperplanes split the spaces they are embedded in into three distinct parts:
 * the hyperplane itself, a plus side and a minus side. However, since spherical
 * space wraps around, a single oriented point is not sufficient to partition the space;
 * any point could be classified as being on the plus or minus side of a hyperplane
 * depending on the direction that the circle is traversed. The approach taken in this
 * class to address this issue is to (1) define a second, implicit cut point at {@code 0pi} and
 * (2) define the domain of hyperplane points (for partitioning purposes) to be the
 * range {@code [0, 2pi)}. Each hyperplane then splits the space into the intervals
 * {@code [0, x]} and {@code [x, 2pi)}, where {@code x} is the location of the hyperplane.
 * One way to visualize this is to picture the circle as a cake that has already been
 * cut at {@code 0pi}. Each hyperplane then specifies the location of the second
 * cut of the cake, with the plus and minus sides being the pieces thus cut.
 * </p>
 *
 * <p>Note that with the hyperplane partitioning rules described above, the hyperplane
 * at {@code 0pi} is unique in that it has the entire space on one side (minus the hyperplane
 * itself) and no points whatsoever on the other. This is very different from hyperplanes in
 * Euclidean space, which always have infinitely many points on both sides.</p>
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see CutAngles
 */
public final class CutAngle extends AbstractHyperplane<Point1S> {
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
    CutAngle(final Point1S point, final boolean positiveFacing,
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

    /** Get the location of the hyperplane as a single value. This is
     * equivalent to {@code cutAngle.getPoint().getAzimuth()}.
     * @return the location of the hyperplane as a single value.
     * @see #getPoint()
     * @see Point1S#getAzimuth()
     */
    public double getAzimuth() {
        return point.getAzimuth();
    }

    /** Get the location of the hyperplane as a single value, normalized
     * to the range {@code [0, 2pi)}. This is equivalent to
     * {@code cutAngle.getPoint().getNormalizedAzimuth()}.
     * @return the location of the hyperplane, normalized to the range
     *      {@code [0, 2pi)}
     * @see #getPoint()
     * @see Point1S#getNormalizedAzimuth()
     */
    public double getNormalizedAzimuth() {
        return point.getNormalizedAzimuth();
    }

    /** Return true if the hyperplane is oriented with its plus
     * side pointing toward increasing angles.
     * @return true if the hyperplane is facing in the direction
     *      of increasing angles
     */
    public boolean isPositiveFacing() {
        return positiveFacing;
    }

    /** Return true if this instance should be considered equivalent to the argument, using the
     * given precision context for comparison.
     * <p>The instances are considered equivalent if they
     * <ol>
     *    <li>have equivalent point locations (points separated by multiples of 2pi are
     *      considered equivalent) and
     *    <li>point in the same direction.</li>
     * </ol>
     * @param other point to compare with
     * @param precision precision context to use for the comparison
     * @return true if this instance should be considered equivalent to the argument
     * @see Point1S#eq(Point1S, DoublePrecisionContext)
     */
    public boolean eq(final CutAngle other, final DoublePrecisionContext precision) {
        return point.eq(other.point, precision) &&
                positiveFacing == other.positiveFacing;
    }

    /** {@inheritDoc} */
    @Override
    public double offset(final Point1S pt) {
        final double dist = pt.getNormalizedAzimuth() - this.point.getNormalizedAzimuth();
        return positiveFacing ? +dist : -dist;
    }

    /** {@inheritDoc} */
    @Override
    public HyperplaneLocation classify(final Point1S pt) {
        final DoublePrecisionContext precision = getPrecision();

        final Point1S compPt = Point1S.ZERO.eq(pt, precision) ?
                Point1S.ZERO :
                pt;

        final double offsetValue = offset(compPt);
        final int cmp = precision.sign(offsetValue);

        if (cmp > 0) {
            return HyperplaneLocation.PLUS;
        } else if (cmp < 0) {
            return HyperplaneLocation.MINUS;
        }

        return HyperplaneLocation.ON;
    }

    /** {@inheritDoc} */
    @Override
    public Point1S project(final Point1S pt) {
        return this.point;
    }

    /** {@inheritDoc} */
    @Override
    public CutAngle reverse() {
        return new CutAngle(point, !positiveFacing, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public CutAngle transform(final Transform<Point1S> transform) {
        final Point1S tPoint = transform.apply(point);
        final boolean tPositiveFacing = transform.preservesOrientation() == positiveFacing;

        return CutAngles.fromPointAndDirection(tPoint, tPositiveFacing, getPrecision());
    }

    /** {@inheritDoc} */
    @Override
    public boolean similarOrientation(final Hyperplane<Point1S> other) {
        return positiveFacing == ((CutAngle) other).positiveFacing;
    }

    /** {@inheritDoc}
     *
     * <p>Since there are no subspaces in spherical 1D space, this method effectively returns a stub implementation
     * of {@link HyperplaneConvexSubset}, the main purpose of which is to support the proper functioning
     * of the partitioning code.</p>
     */
    @Override
    public HyperplaneConvexSubset<Point1S> span() {
        return new CutAngleConvexSubset(this);
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
        } else if (!(obj instanceof CutAngle)) {
            return false;
        }

        final CutAngle other = (CutAngle) obj;
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

    /** {@link HyperplaneConvexSubset} implementation for spherical 1D space. Since there are no subspaces in 1D,
     * this is effectively a stub implementation, its main use being to allow for the correct functioning of
     * partitioning code.
     */
    private static final class CutAngleConvexSubset implements HyperplaneConvexSubset<Point1S> {
        /** The hyperplane containing for this instance. */
        private final CutAngle hyperplane;

        /** Simple constructor.
         * @param hyperplane containing hyperplane instance
         */
        CutAngleConvexSubset(final CutAngle hyperplane) {
            this.hyperplane = hyperplane;
        }

        /** {@inheritDoc} */
        @Override
        public CutAngle getHyperplane() {
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
         * <p>This method returns the point for the underlying hyperplane.</p>
         */
        @Override
        public Point1S getCentroid() {
            return hyperplane.getPoint();
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
        public Split<CutAngleConvexSubset> split(final Hyperplane<Point1S> splitter) {
            final HyperplaneLocation side = splitter.classify(hyperplane.getPoint());

            CutAngleConvexSubset minus = null;
            CutAngleConvexSubset plus = null;

            if (side == HyperplaneLocation.MINUS) {
                minus = this;
            } else if (side == HyperplaneLocation.PLUS) {
                plus = this;
            }

            return new Split<>(minus, plus);
        }

        /** {@inheritDoc} */
        @Override
        public List<CutAngleConvexSubset> toConvex() {
            return Collections.singletonList(this);
        }

        /** {@inheritDoc} */
        @Override
        public CutAngleConvexSubset transform(final Transform<Point1S> transform) {
            return new CutAngleConvexSubset(getHyperplane().transform(transform));
        }

        /** {@inheritDoc} */
        @Override
        public CutAngleSubsetBuilder builder() {
            return new CutAngleSubsetBuilder(this);
        }

        /** {@inheritDoc} */
        @Override
        public CutAngleConvexSubset reverse() {
            return new CutAngleConvexSubset(hyperplane.reverse());
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

    /** {@link HyperplaneSubset.Builder} implementation for spherical 1D space. This is effectively
     * a stub implementation since there are no subspaces of 1D space. Its primary use is to allow
     * for the correct functioning of partitioning code.
     */
    private static final class CutAngleSubsetBuilder implements HyperplaneSubset.Builder<Point1S> {
        /** Base hyperplane subset for the builder. */
        private final CutAngleConvexSubset base;

        /** Construct a new instance using the given base hyperplane subset.
         * @param base base hyperplane subset for the instance
         */
        CutAngleSubsetBuilder(final CutAngleConvexSubset base) {
            this.base = base;
        }

        /** {@inheritDoc} */
        @Override
        public void add(final HyperplaneSubset<Point1S> sub) {
            validateHyperplane(sub);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final HyperplaneConvexSubset<Point1S> sub) {
            validateHyperplane(sub);
        }

        /** {@inheritDoc} */
        @Override
        public CutAngleConvexSubset build() {
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

        /** Validate that the given hyperplane subset lies on the same hyperplane subset as this instance.
         * @param sub hyperplane subset to validate
         * @throws IllegalArgumentException if the argument is not on the same hyperplane
         *      as the instance
         */
        private void validateHyperplane(final HyperplaneSubset<Point1S> sub) {
            final CutAngle baseHyper = base.getHyperplane();
            final CutAngle inputHyper = (CutAngle) sub.getHyperplane();

            if (!baseHyper.eq(inputHyper, baseHyper.getPrecision())) {
                throw new IllegalArgumentException("Argument is not on the same " +
                        "hyperplane. Expected " + baseHyper + " but was " +
                        inputHyper);
            }
        }
    }
}
