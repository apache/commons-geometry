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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.internal.Equivalency;
import org.apache.commons.geometry.core.partitioning.AbstractHyperplane;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
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
     *          context, and
     *    <li>point in the same direction.</li>
     * </ol>
     * </p>
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

        // TODO: determine if the transform flipped the orientation of the point

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean similarOrientation(final Hyperplane<Point1S> other) {
        return positiveFacing == ((OrientedPoint1S) other).positiveFacing;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexSubHyperplane<Point1S> span() {
        // TODO Auto-generated method stub
        return null;
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
}
