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
package org.apache.commons.geometry.euclidean.twod;

import org.apache.commons.geometry.core.partitioning.Embedding;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.partitioning.Transform;
import org.apache.commons.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.geometry.euclidean.oned.Point1D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.apache.commons.numbers.arrays.LinearCombination;

/** This class represents an oriented line in the 2D plane.

 * <p>An oriented line can be defined either by prolongating a line
 * segment between two points past these points, or by one point and
 * an angular direction (in trigonometric orientation).</p>

 * <p>Since it is oriented the two half planes at its two sides are
 * unambiguously identified as a left half plane and a right half
 * plane. This can be used to identify the interior and the exterior
 * in a simple way by local properties only when part of a line is
 * used to define part of a polygon boundary.</p>

 * <p>A line can also be used to completely define a reference frame
 * in the plane. It is sufficient to select one specific point in the
 * line (the orthogonal projection of the original reference frame on
 * the line) and to use the unit vector in the line direction and the
 * orthogonal vector oriented from left half plane to right half
 * plane. We define two coordinates by the process, the
 * <em>abscissa</em> along the line, and the <em>offset</em> across
 * the line. All points of the plane are uniquely identified by these
 * two coordinates. The line is the set of points at zero offset, the
 * left half plane is the set of points with negative offsets and the
 * right half plane is the set of points with positive offsets.</p>
 */
public class Line implements Hyperplane<Point2D>, Embedding<Point2D, Point1D> {
    /** Angle with respect to the abscissa axis. */
    private double angle;

    /** Cosine of the line angle. */
    private double cos;

    /** Sine of the line angle. */
    private double sin;

    /** Offset of the frame origin. */
    private double originOffset;

    /** Tolerance below which points are considered identical. */
    private final double tolerance;

    /** Reverse line. */
    private Line reverse;

    /** Build a line from two points.
     * <p>The line is oriented from p1 to p2</p>
     * @param p1 first point
     * @param p2 second point
     * @param tolerance tolerance below which points are considered identical
     */
    public Line(final Point2D p1, final Point2D p2, final double tolerance) {
        reset(p1, p2);
        this.tolerance = tolerance;
    }

    /** Build a line from a point and an angle.
     * @param p point belonging to the line
     * @param angle angle of the line with respect to abscissa axis
     * @param tolerance tolerance below which points are considered identical
     */
    public Line(final Point2D p, final double angle, final double tolerance) {
        reset(p, angle);
        this.tolerance = tolerance;
    }

    /** Build a line from its internal characteristics.
     * @param angle angle of the line with respect to abscissa axis
     * @param cos cosine of the angle
     * @param sin sine of the angle
     * @param originOffset offset of the origin
     * @param tolerance tolerance below which points are considered identical
     */
    private Line(final double angle, final double cos, final double sin,
                 final double originOffset, final double tolerance) {
        this.angle        = angle;
        this.cos          = cos;
        this.sin          = sin;
        this.originOffset = originOffset;
        this.tolerance    = tolerance;
        this.reverse      = null;
    }

    /** Copy constructor.
     * <p>The created instance is completely independent from the
     * original instance, it is a deep copy.</p>
     * @param line line to copy
     */
    public Line(final Line line) {
        angle        = PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(line.angle);
        cos          = line.cos;
        sin          = line.sin;
        originOffset = line.originOffset;
        tolerance    = line.tolerance;
        reverse      = null;
    }

    /** {@inheritDoc} */
    @Override
    public Line copySelf() {
        return new Line(this);
    }

    /** Reset the instance as if built from two points.
     * <p>The line is oriented from p1 to p2</p>
     * @param p1 first point
     * @param p2 second point
     */
    public void reset(final Point2D p1, final Point2D p2) {
        unlinkReverse();
        final double dx = p2.getX() - p1.getX();
        final double dy = p2.getY() - p1.getY();
        final double d = Math.hypot(dx, dy);
        if (d == 0.0) {
            angle        = 0.0;
            cos          = 1.0;
            sin          = 0.0;
            originOffset = p1.getY();
        } else {
            angle        = Math.PI + Math.atan2(-dy, -dx);
            cos          = dx / d;
            sin          = dy / d;
            originOffset = LinearCombination.value(p2.getX(), p1.getY(), -p1.getX(), p2.getY()) / d;
        }
    }

    /** Reset the instance as if built from a line and an angle.
     * @param p point belonging to the line
     * @param alpha angle of the line with respect to abscissa axis
     */
    public void reset(final Point2D p, final double alpha) {
        unlinkReverse();
        this.angle   = PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(alpha);
        cos          = Math.cos(this.angle);
        sin          = Math.sin(this.angle);
        originOffset = LinearCombination.value(cos, p.getY(), -sin, p.getX());
    }

    /** Revert the instance.
     */
    public void revertSelf() {
        unlinkReverse();
        if (angle < Math.PI) {
            angle += Math.PI;
        } else {
            angle -= Math.PI;
        }
        cos          = -cos;
        sin          = -sin;
        originOffset = -originOffset;
    }

    /** Unset the link between an instance and its reverse.
     */
    private void unlinkReverse() {
        if (reverse != null) {
            reverse.reverse = null;
        }
        reverse = null;
    }

    /** Get the reverse of the instance.
     * <p>Get a line with reversed orientation with respect to the
     * instance.</p>
     * <p>
     * As long as neither the instance nor its reverse are modified
     * (i.e. as long as none of the {@link #reset(Point2D, Point2D)},
     * {@link #reset(Point2D, double)}, {@link #revertSelf()},
     * {@link #setAngle(double)} or {@link #setOriginOffset(double)}
     * methods are called), then the line and its reverse remain linked
     * together so that {@code line.getReverse().getReverse() == line}.
     * When one of the line is modified, the link is deleted as both
     * instance becomes independent.
     * </p>
     * @return a new line, with orientation opposite to the instance orientation
     */
    public Line getReverse() {
        if (reverse == null) {
            reverse = new Line((angle < Math.PI) ? (angle + Math.PI) : (angle - Math.PI),
                               -cos, -sin, -originOffset, tolerance);
            reverse.reverse = this;
        }
        return reverse;
    }

    /** {@inheritDoc} */
    @Override
    public Point1D toSubSpace(final Point2D point) {
        return Point1D.of(LinearCombination.value(cos, point.getX(), sin, point.getY()));
    }

    /** {@inheritDoc} */
    @Override
    public Point2D toSpace(final Point1D point) {
        final double abscissa = point.getX();
        return Point2D.of(LinearCombination.value(abscissa, cos, -originOffset, sin),
                            LinearCombination.value(abscissa, sin,  originOffset, cos));
    }

    /** Get the intersection point of the instance and another line.
     * @param other other line
     * @return intersection point of the instance and the other line
     * or null if there are no intersection points
     */
    public Point2D intersection(final Line other) {
        final double d = LinearCombination.value(sin, other.cos, -other.sin, cos);
        if (Math.abs(d) < tolerance) {
            return null;
        }
        return Point2D.of(LinearCombination.value(cos, other.originOffset, -other.cos, originOffset) / d,
                            LinearCombination.value(sin, other.originOffset, -other.sin, originOffset) / d);
    }

    /** {@inheritDoc} */
    @Override
    public Point2D project(Point2D point) {
        return toSpace(toSubSpace(point));
    }

    /** {@inheritDoc} */
    @Override
    public double getTolerance() {
        return tolerance;
    }

    /** {@inheritDoc} */
    @Override
    public SubLine wholeHyperplane() {
        return new SubLine(this, new IntervalsSet(tolerance));
    }

    /** Build a region covering the whole space.
     * @return a region containing the instance (really a {@link
     * PolygonsSet PolygonsSet} instance)
     */
    @Override
    public PolygonsSet wholeSpace() {
        return new PolygonsSet(tolerance);
    }

    /** Get the offset (oriented distance) of a parallel line.
     * <p>This method should be called only for parallel lines otherwise
     * the result is not meaningful.</p>
     * <p>The offset is 0 if both lines are the same, it is
     * positive if the line is on the right side of the instance and
     * negative if it is on the left side, according to its natural
     * orientation.</p>
     * @param line line to check
     * @return offset of the line
     */
    public double getOffset(final Line line) {
        return originOffset +
               (LinearCombination.value(cos, line.cos, sin, line.sin) > 0 ? -line.originOffset : line.originOffset);
    }

    /** {@inheritDoc} */
    @Override
    public double getOffset(final Point2D point) {
        return LinearCombination.value(sin, point.getX(), -cos, point.getY(), 1.0, originOffset);
    }

    /** {@inheritDoc} */
    @Override
    public boolean sameOrientationAs(final Hyperplane<Point2D> other) {
        final Line otherL = (Line) other;
        return LinearCombination.value(sin, otherL.sin, cos, otherL.cos) >= 0.0;
    }

    /** Get one point from the plane.
     * @param abscissa desired abscissa for the point
     * @param offset desired offset for the point
     * @return one point in the plane, with given abscissa and offset
     * relative to the line
     */
    public Point2D getPointAt(final Point1D abscissa, final double offset) {
        final double x       = abscissa.getX();
        final double dOffset = offset - originOffset;
        return Point2D.of(LinearCombination.value(x, cos,  dOffset, sin),
                            LinearCombination.value(x, sin, -dOffset, cos));
    }

    /** Check if the line contains a point.
     * @param p point to check
     * @return true if p belongs to the line
     */
    public boolean contains(final Point2D p) {
        return Math.abs(getOffset(p)) < tolerance;
    }

    /** Compute the distance between the instance and a point.
     * <p>This is a shortcut for invoking Math.abs(getOffset(p)),
     * and provides consistency with what is in the
     * org.apache.commons.geometry.euclidean.threed.Line class.</p>
     *
     * @param p to check
     * @return distance between the instance and the point
     */
    public double distance(final Point2D p) {
        return Math.abs(getOffset(p));
    }

    /** Check the instance is parallel to another line.
     * @param line other line to check
     * @return true if the instance is parallel to the other line
     * (they can have either the same or opposite orientations)
     */
    public boolean isParallelTo(final Line line) {
        return Math.abs(LinearCombination.value(sin, line.cos, -cos, line.sin)) < tolerance;
    }

    /** Translate the line to force it passing by a point.
     * @param p point by which the line should pass
     */
    public void translateToPoint(final Point2D p) {
        originOffset = LinearCombination.value(cos, p.getY(), -sin, p.getX());
    }

    /** Get the angle of the line.
     * @return the angle of the line with respect to the abscissa axis
     */
    public double getAngle() {
        return PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(angle);
    }

    /** Set the angle of the line.
     * @param angle new angle of the line with respect to the abscissa axis
     */
    public void setAngle(final double angle) {
        unlinkReverse();
        this.angle = PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(angle);
        cos        = Math.cos(this.angle);
        sin        = Math.sin(this.angle);
    }

    /** Get the offset of the origin.
     * @return the offset of the origin
     */
    public double getOriginOffset() {
        return originOffset;
    }

    /** Set the offset of the origin.
     * @param offset offset of the origin
     */
    public void setOriginOffset(final double offset) {
        unlinkReverse();
        originOffset = offset;
    }

    /** Get a {@link org.apache.commons.geometry.core.partitioning.Transform
     * Transform} embedding an affine transform.
     * @param cXX transform factor between input abscissa and output abscissa
     * @param cYX transform factor between input abscissa and output ordinate
     * @param cXY transform factor between input ordinate and output abscissa
     * @param cYY transform factor between input ordinate and output ordinate
     * @param cX1 transform addendum for output abscissa
     * @param cY1 transform addendum for output ordinate
     * @return a new transform that can be applied to either {@link
     * Point2D}, {@link Line Line} or {@link
     * org.apache.commons.geometry.core.partitioning.SubHyperplane
     * SubHyperplane} instances
     * @exception IllegalArgumentException if the transform is non invertible
     */
    public static Transform<Point2D, Point1D> getTransform(final double cXX,
                                                                   final double cYX,
                                                                   final double cXY,
                                                                   final double cYY,
                                                                   final double cX1,
                                                                   final double cY1)
        throws IllegalArgumentException {
        return new LineTransform(cXX, cYX, cXY, cYY, cX1, cY1);
    }

    /** Class embedding an affine transform.
     * <p>This class is used in order to apply an affine transform to a
     * line. Using a specific object allow to perform some computations
     * on the transform only once even if the same transform is to be
     * applied to a large number of lines (for example to a large
     * polygon)./<p>
     */
    private static class LineTransform implements Transform<Point2D, Point1D> {

        /** Transform factor between input abscissa and output abscissa. */
        private final double cXX;

        /** Transform factor between input abscissa and output ordinate. */
        private final double cYX;

        /** Transform factor between input ordinate and output abscissa. */
        private final double cXY;

        /** Transform factor between input ordinate and output ordinate. */
        private final double cYY;

        /** Transform addendum for output abscissa. */
        private final double cX1;

        /** Transform addendum for output ordinate. */
        private final double cY1;

        /** cXY * cY1 - cYY * cX1. */
        private final double c1Y;

        /** cXX * cY1 - cYX * cX1. */
        private final double c1X;

        /** cXX * cYY - cYX * cXY. */
        private final double c11;

        /** Build an affine line transform from a n {@code AffineTransform}.
         * @param cXX transform factor between input abscissa and output abscissa
         * @param cYX transform factor between input abscissa and output ordinate
         * @param cXY transform factor between input ordinate and output abscissa
         * @param cYY transform factor between input ordinate and output ordinate
         * @param cX1 transform addendum for output abscissa
         * @param cY1 transform addendum for output ordinate
         * @exception IllegalArgumentException if the transform is non invertible
         */
        LineTransform(final double cXX, final double cYX, final double cXY,
                      final double cYY, final double cX1, final double cY1)
            throws IllegalArgumentException {

            this.cXX = cXX;
            this.cYX = cYX;
            this.cXY = cXY;
            this.cYY = cYY;
            this.cX1 = cX1;
            this.cY1 = cY1;

            c1Y = LinearCombination.value(cXY, cY1, -cYY, cX1);
            c1X = LinearCombination.value(cXX, cY1, -cYX, cX1);
            c11 = LinearCombination.value(cXX, cYY, -cYX, cXY);

            if (Math.abs(c11) < 1.0e-20) {
                throw new IllegalArgumentException("Non-invertible affine transform collapses some lines into single points");
            }

        }

        /** {@inheritDoc} */
        @Override
        public Point2D apply(final Point2D point) {
            final double  x   = point.getX();
            final double  y   = point.getY();
            return Point2D.of(LinearCombination.value(cXX, x, cXY, y, cX1, 1),
                                LinearCombination.value(cYX, x, cYY, y, cY1, 1));
        }

        /** {@inheritDoc} */
        @Override
        public Line apply(final Hyperplane<Point2D> hyperplane) {
            final Line   line    = (Line) hyperplane;
            final double rOffset = LinearCombination.value(c1X, line.cos, c1Y, line.sin, c11, line.originOffset);
            final double rCos    = LinearCombination.value(cXX, line.cos, cXY, line.sin);
            final double rSin    = LinearCombination.value(cYX, line.cos, cYY, line.sin);
            final double inv     = 1.0 / Math.sqrt(rSin * rSin + rCos * rCos);
            return new Line(Math.PI + Math.atan2(-rSin, -rCos),
                            inv * rCos, inv * rSin,
                            inv * rOffset, line.tolerance);
        }

        /** {@inheritDoc} */
        @Override
        public SubHyperplane<Point1D> apply(final SubHyperplane<Point1D> sub,
                                                final Hyperplane<Point2D> original,
                                                final Hyperplane<Point2D> transformed) {
            final OrientedPoint op     = (OrientedPoint) sub.getHyperplane();
            final Line originalLine    = (Line) original;
            final Line transformedLine = (Line) transformed;
            final Point1D newLoc =
                transformedLine.toSubSpace(apply(originalLine.toSpace(op.getLocation())));
            return new OrientedPoint(newLoc, op.isDirect(), originalLine.tolerance).wholeHyperplane();
        }

    }

}
