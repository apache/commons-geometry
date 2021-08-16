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

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.numbers.core.Precision;

/** Class containing factory methods for constructing {@link GreatCircle} and {@link GreatCircleSubset} instances.
 */
public final class GreatCircles {

    /** Utility class; no instantiation. */
    private GreatCircles() {
    }

    /** Create a great circle instance from its pole vector. An arbitrary u-axis is chosen.
     * @param pole pole vector for the great circle
     * @param precision precision context used to compare floating point values
     * @return a great circle defined by the given pole vector
     */
    public static GreatCircle fromPole(final Vector3D pole, final Precision.DoubleEquivalence precision) {
        final Vector3D.Unit u = pole.orthogonal();
        final Vector3D.Unit v = pole.cross(u).normalize();
        return new GreatCircle(pole.normalize(), u, v, precision);
    }

    /** Create a great circle instance from its pole vector and a vector representing the u-axis
     * in the equator plane. The u-axis vector defines the {@code 0pi} location for the embedded
     * subspace.
     * @param pole pole vector for the great circle
     * @param u u-axis direction for the equator plane
     * @param precision precision context used to compare floating point values
     * @return a great circle defined by the given pole vector and u-axis direction
     */
    public static GreatCircle fromPoleAndU(final Vector3D pole, final Vector3D u,
            final Precision.DoubleEquivalence precision) {

        final Vector3D.Unit unitPole = pole.normalize();
        final Vector3D.Unit unitX = pole.orthogonal(u);
        final Vector3D.Unit unitY = pole.cross(u).normalize();

        return new GreatCircle(unitPole, unitX, unitY, precision);
    }

    /** Create a great circle instance from two points on the circle. The u-axis of the
     * instance points to the location of the first point. The orientation of the circle
     * is along the shortest path between the two points.
     * @param a first point on the great circle
     * @param b second point on the great circle
     * @param precision precision context used to compare floating point values
     * @return great circle instance containing the given points
     * @throws IllegalArgumentException if either of the given points is NaN or infinite, or if the given points are
     *      equal or antipodal as evaluated by the given precision context
     */
    public static GreatCircle fromPoints(final Point2S a, final Point2S b,
            final Precision.DoubleEquivalence precision) {

        if (!a.isFinite() || !b.isFinite()) {
            throw new IllegalArgumentException("Invalid points for great circle: " + a + ", " + b);
        }

        String err = null;

        final double dist = a.distance(b);
        if (precision.eqZero(dist)) {
            err = "equal";
        } else if (precision.eq(dist, Math.PI)) {
            err = "antipodal";
        }

        if (err != null) {
            throw new IllegalArgumentException("Cannot create great circle from points " + a + " and " + b +
                    ": points are " + err);
        }

        final Vector3D.Unit u = a.getVector().normalize();
        final Vector3D.Unit pole = u.cross(b.getVector()).normalize();
        final Vector3D.Unit v = pole.cross(u).normalize();

        return new GreatCircle(pole, u, v, precision);
    }

    /** Construct an arc along the shortest path between the given points. The underlying
     * great circle is oriented in the direction from {@code start} to {@code end}.
     * @param start start point for the interval
     * @param end end point point for the interval
     * @param precision precision context used to compare floating point numbers
     * @return an arc representing the shortest path between the given points
     * @throws IllegalArgumentException if either of the given points is NaN or infinite, or if the given
     *      points are equal or antipodal as evaluated by the given precision context
     * @see GreatCircles#fromPoints(Point2S, Point2S, org.apache.commons.numbers.core.Precision.DoubleEquivalence)
     */
    public static GreatArc arcFromPoints(final Point2S start, final Point2S end,
            final Precision.DoubleEquivalence precision) {
        final GreatCircle circle = GreatCircles.fromPoints(start, end, precision);

        final Point1S subspaceStart = circle.toSubspace(start);
        final Point1S subspaceEnd = circle.toSubspace(end);
        final AngularInterval.Convex interval = AngularInterval.Convex.of(subspaceStart, subspaceEnd, precision);

        return arcFromInterval(circle, interval);
    }

    /** Construct an arc from a great circle and an angular interval.
     * @param circle circle defining the arc
     * @param interval interval representing the portion of the circle contained
     *      in the arc
     * @return an arc created from the given great circle and interval
     */
    public static GreatArc arcFromInterval(final GreatCircle circle, final AngularInterval.Convex interval) {
        return new GreatArc(circle, interval);
    }

    /** Validate that the actual great circle is equivalent to the expected great circle,
     * throwing an exception if not.
     * @param expected the expected great circle
     * @param actual the actual great circle
     * @throws IllegalArgumentException if the actual great circle is not equivalent to the
     *      expected great circle
     */
    static void validateGreatCirclesEquivalent(final GreatCircle expected, final GreatCircle actual) {
        if (!expected.eq(actual, expected.getPrecision())) {
            throw new IllegalArgumentException("Arguments do not represent the same great circle. Expected " +
                    expected + " but was " + actual + ".");
        }
    }
}
