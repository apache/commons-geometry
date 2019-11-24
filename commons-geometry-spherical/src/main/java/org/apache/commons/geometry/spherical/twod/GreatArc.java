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
package org.apache.commons.geometry.spherical.twod;

import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.CutAngle;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.oned.Transform1S;

/** Class representing a single, <em>convex</em> angular interval in a {@link GreatCircle}. Convex
 * angular intervals are those where the shortest path between all pairs of points in the
 * interval are completely contained in the interval. In the case of paths that tie for the
 * shortest length, it is sufficient that one of the paths is completely contained in the
 * interval. In spherical 2D space, convex arcs either fill the entire great circle or have
 * an angular size of less than or equal to {@code pi} radians.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
public final class GreatArc extends AbstractSubGreatCircle implements ConvexSubHyperplane<Point2S> {

    /** Serializable UID. */
    private static final long serialVersionUID = 20191005L;

    /** The interval representing the region of the great circle contained in the arc.
     */
    private final AngularInterval.Convex interval;

    /** Create a new instance from a great circle and the interval embedded in it.
     * @param circle defining great circle instance
     * @param interval convex angular interval embedded in the great circle
     */
    private GreatArc(final GreatCircle circle, final AngularInterval.Convex interval) {
        super(circle);

        this.interval = interval;
    }

    /** Return the start point of the arc, or null if the arc represents the full space.
     * @return the start point of the arc, or null if the arc represents the full space.
     */
    public Point2S getStartPoint() {
        if (!interval.isFull()) {
            return getCircle().toSpace(interval.getMinBoundary().getPoint());
        }

        return null;
    }

    /** Return the end point of the arc, or null if the arc represents the full space.
     * @return the end point of the arc, or null if the arc represents the full space.
     */
    public Point2S getEndPoint() {
        if (!interval.isFull()) {
            return getCircle().toSpace(interval.getMaxBoundary().getPoint());
        }

        return null;
    }

    /** Return the midpoint of the arc, or null if the arc represents the full space.
     * @return the midpoint of the arc, or null if the arc represents the full space.
     */
    public Point2S getMidPoint() {
        if (!interval.isFull()) {
            return getCircle().toSpace(interval.getMidPoint());
        }

        return null;
    }

    /** Get the angular interval for the arc.
     * @return the angular interval for the arc
     * @see #getSubspaceRegion()
     */
    public AngularInterval.Convex getInterval() {
        return interval;
    }

    /** {@inheritDoc} */
    @Override
    public AngularInterval.Convex getSubspaceRegion() {
        return getInterval();
    }

    /** {@inheritDoc} */
    @Override
    public List<GreatArc> toConvex() {
        return Collections.singletonList(this);
    }

    /** {@inheritDoc} */
    @Override
    public Split<GreatArc> split(final Hyperplane<Point2S> splitter) {
        final GreatCircle splitterCircle = (GreatCircle) splitter;
        final GreatCircle thisCircle = getCircle();

        final Point2S intersection = splitterCircle.intersection(thisCircle);

        GreatArc minus = null;
        GreatArc plus = null;

        if (intersection != null) {
            // use a negative-facing cut angle to account for the fact that the great circle
            // poles point to the minus side of the circle
            final CutAngle subSplitter = CutAngle.createNegativeFacing(
                    thisCircle.toSubspace(intersection), splitterCircle.getPrecision());

            final Split<AngularInterval.Convex> subSplit = interval.splitDiameter(subSplitter);
            final SplitLocation subLoc = subSplit.getLocation();

            if (subLoc == SplitLocation.MINUS) {
                minus = this;
            } else if (subLoc == SplitLocation.PLUS) {
                plus = this;
            } else if (subLoc == SplitLocation.BOTH) {
                minus = GreatArc.fromInterval(thisCircle, subSplit.getMinus());
                plus = GreatArc.fromInterval(thisCircle, subSplit.getPlus());
            }
        }

        return new Split<>(minus, plus);
    }

    /** {@inheritDoc} */
    @Override
    public GreatArc transform(final Transform<Point2S> transform) {
        return new GreatArc(getCircle().transform(transform), interval);
    }

    /** {@inheritDoc} */
    @Override
    public GreatArc reverse() {
        return new GreatArc(
                getCircle().reverse(),
                interval.transform(Transform1S.createNegation()));
    }

    /** Return a string representation of this great arc.
     *
     * <p>In order to keep the string representation short but useful, the exact format of the return
     * value depends on the properties of the arc. See below for examples.
     *
     * <ul>
     *      <li>Full arc
     *          <ul>
     *              <li>{@code GreatArc[full= true, circle= GreatCircle[pole= (0.0, 0.0, 1.0), x= (1.0, 0.0, 0.0), y= (0.0, 1.0, 0.0)]}</li>
     *          </ul>
     *      </li>
     *      <li>Non-full arc
     *          <ul>
     *              <li>{@code GreatArc[start= (1.0, 1.5707963267948966), end= (2.0, 1.5707963267948966)}</li>
     *          </ul>
     *      </li>
     * </ul>
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[");

        if (isFull()) {
            sb.append("full= true, circle= ")
                .append(getCircle());
        } else {
            sb.append("start= ")
                .append(getStartPoint())
                .append(", end= ")
                .append(getEndPoint());
        }

        return sb.toString();
    }

    /** Construct an arc along the shortest path between the given points. The underlying
     * great circle is oriented in the direction from {@code start} to {@code end}.
     * @param start start point for the interval
     * @param end end point point for the interval
     * @param precision precision context used to compare floating point numbers
     * @return an arc representing the shortest path between the given points
     * @throws IllegalArgumentException if either of the given points is NaN or infinite
     * @throws org.apache.commons.geometry.core.exception.GeometryException if the given points are equal or
     *      antipodal as evaluated by the given precision context
     * @see GreatCircle#fromPoints(Point2S, Point2S, org.apache.commons.geometry.core.precision.DoublePrecisionContext)
     */
    public static GreatArc fromPoints(final Point2S start, final Point2S end, final DoublePrecisionContext precision) {
        final GreatCircle circle = GreatCircle.fromPoints(start, end, precision);

        final Point1S subspaceStart = circle.toSubspace(start);
        final Point1S subspaceEnd = circle.toSubspace(end);
        final AngularInterval.Convex interval = AngularInterval.Convex.of(subspaceStart, subspaceEnd, precision);

        return fromInterval(circle, interval);
    }

    /** Construct an arc from a great circle and an angular interval.
     * @param circle circle defining the arc
     * @param interval interval representing the portion of the circle contained
     *      in the arc
     * @return an arc created from the given great circle and interval
     */
    public static GreatArc fromInterval(final GreatCircle circle, final AngularInterval.Convex interval) {
        return new GreatArc(circle, interval);
    }
}
