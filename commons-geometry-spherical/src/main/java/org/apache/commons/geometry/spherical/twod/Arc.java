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
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.CutAngle;
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
public class Arc extends AbstractSubGreatCircle implements ConvexSubHyperplane<Point2S> {

    /** Serializable UID */
    private static final long serialVersionUID = 20191005L;

    /** The interval representing the region of the great circle contained in the arc.
     */
    private final AngularInterval.Convex interval;

    /** Create a new instance from a great circle and the interval embedded in it.
     * @param circle defining great circle instance
     * @param interval convex angular interval embedded in the great circle
     */
    private Arc(final GreatCircle circle, final AngularInterval.Convex interval) {
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
    public List<Arc> toConvex() {
        return Collections.singletonList(this);
    }

    /** {@inheritDoc} */
    @Override
    public Split<Arc> split(final Hyperplane<Point2S> splitter) {
        final GreatCircle splitterCircle = (GreatCircle) splitter;
        final GreatCircle thisCircle = getCircle();

        final Point2S intersection = splitterCircle.intersection(thisCircle);

        Arc minus = null;
        Arc plus = null;

        if (intersection != null) {
            final CutAngle subSplitter = CutAngle.createPositiveFacing(
                    thisCircle.toSubspace(intersection), splitterCircle.getPrecision());

            final Split<AngularInterval.Convex> subSplit = interval.splitDiameter(subSplitter);
            final SplitLocation subLoc = subSplit.getLocation();

            if (subLoc == SplitLocation.MINUS) {
                minus = this;
            }
            else if (subLoc == SplitLocation.PLUS) {
                plus = this;
            }
            else if (subLoc == SplitLocation.BOTH) {
                minus = Arc.fromInterval(thisCircle, subSplit.getMinus());
                plus = Arc.fromInterval(thisCircle, subSplit.getPlus());
            }
        }

        return new Split<>(minus, plus);
    }

    /** {@inheritDoc} */
    @Override
    public Arc transform(final Transform<Point2S> transform) {
        return new Arc(getCircle().transform(transform), interval);
    }

    /** {@inheritDoc} */
    @Override
    public Arc reverse() {
        return new Arc(
                getCircle().reverse(),
                interval.transform(Transform1S.createNegation()));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[circle= ")
            .append(getCircle())
            .append(", interval= ")
            .append(getInterval())
            .append(']');

        return sb.toString();
    }

    /** Construct an arc from a great circle and an angular interval.
     * @param circle circle defining the arc
     * @param interval interval representing the portion of the circle contained
     *      in the arc
     * @return an arc created from the given great circle and interval
     */
    public static Arc fromInterval(final GreatCircle circle, final AngularInterval.Convex interval) {
        return new Arc(circle, interval);
    }
}
