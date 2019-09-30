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

import org.apache.commons.geometry.spherical.partitioning.Embedding_Old;
import org.apache.commons.geometry.spherical.partitioning.Hyperplane_Old;
import org.apache.commons.geometry.spherical.partitioning.SubHyperplane_Old;
import org.apache.commons.geometry.spherical.partitioning.Transform_Old;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.spherical.oned.Arc;
import org.apache.commons.geometry.spherical.oned.ArcsSet;
import org.apache.commons.geometry.spherical.oned.Point1S;

/** This class represents an oriented great circle on the 2-sphere.

 * <p>An oriented circle can be defined by a center point. The circle
 * is the the set of points that are in the normal plan the center.</p>

 * <p>Since it is oriented the two spherical caps at its two sides are
 * unambiguously identified as a left cap and a right cap. This can be
 * used to identify the interior and the exterior in a simple way by
 * local properties only when part of a line is used to define part of
 * a spherical polygon boundary.</p>
 */
public class Circle implements Hyperplane_Old<Point2S>, Embedding_Old<Point2S, Point1S> {

    /** Pole or circle center. */
    private Vector3D pole;

    /** First axis in the equator plane, origin of the phase angles. */
    private Vector3D x;

    /** Second axis in the equator plane, in quadrature with respect to x. */
    private Vector3D y;

    /** Precision context used to determine floating point equality. */
    private final DoublePrecisionContext precision;

    /** Build a great circle from its pole.
     * <p>The circle is oriented in the trigonometric direction around pole.</p>
     * @param pole circle pole
     * @param precision precision context used to compare floating point values
     */
    public Circle(final Vector3D pole, final DoublePrecisionContext precision) {
        reset(pole);
        this.precision = precision;
    }

    /** Build a great circle from two non-aligned points.
     * <p>The circle is oriented from first to second point using the path smaller than \( \pi \).</p>
     * @param first first point contained in the great circle
     * @param second second point contained in the great circle
     * @param precision precision context used to compare floating point values
     */
    public Circle(final Point2S first, final Point2S second, final DoublePrecisionContext precision) {
        reset(first.getVector().cross(second.getVector()));
        this.precision = precision;
    }

    /** Build a circle from its internal components.
     * <p>The circle is oriented in the trigonometric direction around center.</p>
     * @param pole circle pole
     * @param x first axis in the equator plane
     * @param y second axis in the equator plane
     * @param precision precision context used to compare floating point values
     */
    private Circle(final Vector3D pole, final Vector3D x, final Vector3D y,
            final DoublePrecisionContext precision) {
        this.pole      = pole;
        this.x         = x;
        this.y         = y;
        this.precision = precision;
    }

    /** Copy constructor.
     * <p>The created instance is completely independent from the
     * original instance, it is a deep copy.</p>
     * @param circle circle to copy
     */
    public Circle(final Circle circle) {
        this(circle.pole, circle.x, circle.y, circle.precision);
    }

    /** {@inheritDoc} */
    @Override
    public Circle copySelf() {
        return new Circle(this);
    }

    /** Reset the instance as if built from a pole.
     * <p>The circle is oriented in the trigonometric direction around pole.</p>
     * @param newPole circle pole
     */
    public void reset(final Vector3D newPole) {
        this.pole = newPole.normalize();
        this.x    = newPole.orthogonal();
        this.y    = newPole.cross(x).normalize();
    }

    /** Revert the instance.
     */
    public void revertSelf() {
        // x remains the same
        y    = y.negate();
        pole = pole.negate();
    }

    /** Get the reverse of the instance.
     * <p>Get a circle with reversed orientation with respect to the
     * instance. A new object is built, the instance is untouched.</p>
     * @return a new circle, with orientation opposite to the instance orientation
     */
    public Circle getReverse() {
        return new Circle(pole.negate(), x, y.negate(), precision);
    }

    /** {@inheritDoc} */
    @Override
    public Point2S project(Point2S point) {
        return toSpace(toSubSpace(point));
    }

    /** Get the object used to determine floating point equality for this region.
     * @return the floating point precision context for the instance
     */
    @Override
    public DoublePrecisionContext getPrecision() {
        return precision;
    }

    /** {@inheritDoc}
     * @see #getPhase(Vector3D)
     */
    @Override
    public Point1S toSubSpace(final Point2S point) {
        return Point1S.of(getPhase(point.getVector()));
    }

    /** Get the phase angle of a direction.
     * <p>
     * The direction may not belong to the circle as the
     * phase is computed for the meridian plane between the circle
     * pole and the direction.
     * </p>
     * @param direction direction for which phase is requested
     * @return phase angle of the direction around the circle
     * @see #toSubSpace(Point)
     */
    public double getPhase(final Vector3D direction) {
        return Math.PI + Math.atan2(-direction.dot(y), -direction.dot(x));
    }

    /** {@inheritDoc}
     * @see #getPointAt(double)
     */
    @Override
    public Point2S toSpace(final Point1S point) {
        return Point2S.from(getPointAt(point.getAzimuth()));
    }

    /** Get a circle point from its phase around the circle.
     * @param alpha phase around the circle
     * @return circle point on the sphere
     * @see #toSpace(Point)
     * @see #getXAxis()
     * @see #getYAxis()
     */
    public Vector3D getPointAt(final double alpha) {
        return Vector3D.linearCombination(Math.cos(alpha), x, Math.sin(alpha), y);
    }

    /** Get the X axis of the circle.
     * <p>
     * This method returns the same value as {@link #getPointAt(double)
     * getPointAt(0.0)} but it does not do any computation and always
     * return the same instance.
     * </p>
     * @return an arbitrary x axis on the circle
     * @see #getPointAt(double)
     * @see #getYAxis()
     * @see #getPole()
     */
    public Vector3D getXAxis() {
        return x;
    }

    /** Get the Y axis of the circle.
     * <p>
     * This method returns the same value as {@link #getPointAt(double)
     * getPointAt(0.5 * Math.PI)} but it does not do any computation and always
     * return the same instance.
     * </p>
     * @return an arbitrary y axis point on the circle
     * @see #getPointAt(double)
     * @see #getXAxis()
     * @see #getPole()
     */
    public Vector3D getYAxis() {
        return y;
    }

    /** Get the pole of the circle.
     * <p>
     * As the circle is a great circle, the pole does <em>not</em>
     * belong to it.
     * </p>
     * @return pole of the circle
     * @see #getXAxis()
     * @see #getYAxis()
     */
    public Vector3D getPole() {
        return pole;
    }

    /** Get the arc of the instance that lies inside the other circle.
     * @param other other circle
     * @return arc of the instance that lies inside the other circle
     */
    public Arc getInsideArc(final Circle other) {
        final double alpha  = getPhase(other.pole);
        final double halfPi = 0.5 * Math.PI;
        return new Arc(alpha - halfPi, alpha + halfPi, precision);
    }

    /** {@inheritDoc} */
    @Override
    public SubCircle wholeHyperplane() {
        return new SubCircle(this, new ArcsSet(precision));
    }

    /** Build a region covering the whole space.
     * @return a region containing the instance (really a {@link
     * SphericalPolygonsSet SphericalPolygonsSet} instance)
     */
    @Override
    public SphericalPolygonsSet wholeSpace() {
        return new SphericalPolygonsSet(precision);
    }

    /** {@inheritDoc}
     * @see #getOffset(Vector3D)
     */
    @Override
    public double getOffset(final Point2S point) {
        return getOffset(point.getVector());
    }

    /** Get the offset (oriented distance) of a direction.
     * <p>The offset is defined as the angular distance between the
     * circle center and the direction minus the circle radius. It
     * is therefore 0 on the circle, positive for directions outside of
     * the cone delimited by the circle, and negative inside the cone.</p>
     * @param direction direction to check
     * @return offset of the direction
     * @see #getOffset(Point)
     */
    public double getOffset(final Vector3D direction) {
        return pole.angle(direction) - 0.5 * Math.PI;
    }

    /** {@inheritDoc} */
    @Override
    public boolean sameOrientationAs(final Hyperplane_Old<Point2S> other) {
        final Circle otherC = (Circle) other;
        return pole.dot(otherC.pole) >= 0.0;
    }

    /** Get a {@link org.apache.commons.geometry.core.partitioning.Transform
     * Transform} embedding a 3D rotation.
     * @param rotation rotation to use
     * @return a new transform that can be applied to either {@link
     * Point2S Point}, {@link Circle Line} or {@link
     * org.apache.commons.geometry.core.partitioning.SubHyperplane
     * SubHyperplane} instances
     */
    public static Transform_Old<Point2S, Point1S> getTransform(final QuaternionRotation rotation) {
        return new CircleTransform(rotation);
    }

    /** Class embedding a 3D rotation. */
    private static class CircleTransform implements Transform_Old<Point2S, Point1S> {

        /** Underlying rotation. */
        private final QuaternionRotation rotation;

        /** Build a transform from a {@code Rotation}.
         * @param rotation rotation to use
         */
        CircleTransform(final QuaternionRotation rotation) {
            this.rotation = rotation;
        }

        /** {@inheritDoc} */
        @Override
        public Point2S apply(final Point2S point) {
            return Point2S.from(rotation.apply(point.getVector()));
        }

        /** {@inheritDoc} */
        @Override
        public Circle apply(final Hyperplane_Old<Point2S> hyper) {
            final Circle circle = (Circle) hyper;
            return new Circle(rotation.apply(circle.pole),
                              rotation.apply(circle.x),
                              rotation.apply(circle.y),
                              circle.precision);
        }

        /** {@inheritDoc} */
        @Override
        public SubHyperplane_Old<Point1S> apply(final SubHyperplane_Old<Point1S> sub,
                                             final Hyperplane_Old<Point2S> original,
                                             final Hyperplane_Old<Point2S> transformed) {
            // as the circle is rotated, the limit angles are rotated too
            return sub;
        }

    }

}
