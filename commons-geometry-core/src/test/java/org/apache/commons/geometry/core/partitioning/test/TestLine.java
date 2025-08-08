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
package org.apache.commons.geometry.core.partitioning.test;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.EmbeddingHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;

/** Class representing a line in two-dimensional Euclidean space. This
 * class should only be used for testing purposes.
 */
public final class TestLine implements EmbeddingHyperplane<TestPoint2D, TestPoint1D> {

    /** Line pointing along the positive x-axis. */
    public static final TestLine X_AXIS = new TestLine(0, 0, 1, 0);

    /** Line pointing along the positive y-axis. */
    public static final TestLine Y_AXIS = new TestLine(0, 0, 0, 1);

    /** X value of the normalized line direction. */
    private final double directionX;

    /** Y value of the normalized line direction. */
    private final double directionY;

    /** The distance between the origin and the line. */
    private final double originOffset;

    /** Construct a line from two points. The line points in the direction from
     * {@code p1} to {@code p2}.
     * @param p1 first point
     * @param p2 second point
     */
    public TestLine(final TestPoint2D p1, final TestPoint2D p2) {
        this(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    /** Construct a line from two point, given by their components.
     * @param x1 x coordinate of first point
     * @param y1 y coordinate of first point
     * @param x2 x coordinate of second point
     * @param y2 y coordinate of second point
     */
    public TestLine(final double x1, final double y1, final double x2, final double y2) {
        double vecX = x2 - x1;
        double vecY = y2 - y1;

        final double norm = norm(vecX, vecY);

        vecX /= norm;
        vecY /= norm;

        if (!Double.isFinite(vecX) || !Double.isFinite(vecY)) {
            throw new IllegalStateException("Unable to create line between points: (" +
                    x1 + ", " + y1 + "), (" + x2 + ", " + y2 + ")");
        }

        this.directionX = vecX;
        this.directionY = vecY;

        this.originOffset = signedArea(vecX, vecY, x1, y1);
    }

    /** Get the line origin, meaning the projection of the 2D origin onto the line.
     * @return line origin
     */
    public TestPoint2D getOrigin() {
        return toSpace(0);
    }

    /** Get the x component of the line direction.
     * @return x component of the line direction.
     */
    public double getDirectionX() {
        return directionX;
    }

    /** Get the y component of the line direction.
     * @return y component of the line direction.
     */
    public double getDirectionY() {
        return directionY;
    }

    /** {@inheritDoc} */
    @Override
    public double offset(final TestPoint2D point) {
        return originOffset - signedArea(directionX, directionY, point.getX(), point.getY());
    }

    /** {@inheritDoc} */
    @Override
    public HyperplaneLocation classify(final TestPoint2D point) {
        final double offset = offset(point);
        final double cmp = PartitionTestUtils.PRECISION.compare(offset, 0.0);
        if (cmp == 0) {
            return HyperplaneLocation.ON;
        }
        return cmp < 0 ? HyperplaneLocation.MINUS : HyperplaneLocation.PLUS;
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final TestPoint2D point) {
        return classify(point) == HyperplaneLocation.ON;
    }

    /** Get the location of the given 2D point in the 1D space of the line.
     * @param point point to project into the line's 1D space
     * @return location of the point in the line's 1D space
     */
    public double toSubspaceValue(final TestPoint2D point) {
        return (directionX * point.getX()) + (directionY * point.getY());
    }

    /** {@inheritDoc} */
    @Override
    public TestPoint1D toSubspace(final TestPoint2D point) {
        return new TestPoint1D(toSubspaceValue(point));
    }

    /** Get the 2D location of the given 1D location in the line's 1D space.
     * @param abscissa location in the line's 1D space.
     * @return the location of the given 1D point in 2D space
     */
    public TestPoint2D toSpace(final double abscissa) {
        if (Double.isInfinite(abscissa)) {
            final double dirXCmp = PartitionTestUtils.PRECISION.signum(directionX);
            final double dirYCmp = PartitionTestUtils.PRECISION.signum(directionY);

            final double x;
            if (dirXCmp == 0) {
                // vertical line
                x = getOrigin().getX();
            } else {
                x = (dirXCmp < 0 ^ abscissa < 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }

            final double y;
            if (dirYCmp == 0) {
                // horizontal line
                y = getOrigin().getY();
            } else {
                y = (dirYCmp < 0 ^ abscissa < 0) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            }

            return new TestPoint2D(x, y);
        }

        final double ptX = (abscissa * directionX) + (-originOffset * directionY);
        final double ptY = (abscissa * directionY) + (originOffset * directionX);

        return new TestPoint2D(ptX, ptY);
    }

    /** {@inheritDoc} */
    @Override
    public TestPoint2D toSpace(final TestPoint1D point) {
        return toSpace(point.getX());
    }

    /** {@inheritDoc} */
    @Override
    public TestPoint2D project(final TestPoint2D point) {
        return toSpace(toSubspaceValue(point));
    }

    /** {@inheritDoc} */
    @Override
    public TestLine reverse() {
        final TestPoint2D pt = getOrigin();
        return new TestLine(pt.getX(), pt.getY(), pt.getX() - directionX, pt.getY() - directionY);
    }

    /** {@inheritDoc} */
    @Override
    public TestLine transform(final Transform<TestPoint2D> transform) {
        final TestPoint2D p1 = transform.apply(toSpace(0));
        final TestPoint2D p2 = transform.apply(toSpace(1));

        return new TestLine(p1, p2);
    }

    /** {@inheritDoc} */
    @Override
    public boolean similarOrientation(final Hyperplane<TestPoint2D> other) {
        final TestLine otherLine = (TestLine) other;
        final double dot = (directionX * otherLine.directionX) + (directionY * otherLine.directionY);
        return dot >= 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public TestLineSegment span() {
        return new TestLineSegment(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, this);
    }

    /** Get the intersection point of the instance and another line.
     * @param other other line
     * @return intersection point of the instance and the other line
     *      or null if there is no unique intersection point (ie, the lines
     *      are parallel or coincident)
     */
    public TestPoint2D intersection(final TestLine other) {
        final double area = signedArea(directionX, directionY, other.directionX, other.directionY);
        if (PartitionTestUtils.PRECISION.eqZero(area)) {
            // lines are parallel
            return null;
        }

        final double x = ((other.directionX * originOffset) +
                (-directionX * other.originOffset)) / area;

        final double y = ((other.directionY * originOffset) +
                (-directionY * other.originOffset)) / area;

        return new TestPoint2D(x, y);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[origin= ")
            .append(getOrigin())
            .append(", direction= (")
            .append(directionX)
            .append(", ")
            .append(directionY)
            .append(")]");

        return sb.toString();
    }

    /** Compute the signed area of the parallelogram with sides defined by the given
     * vectors.
     * @param x1 x coordinate of first vector
     * @param y1 y coordinate of first vector
     * @param x2 x coordinate of second vector
     * @param y2 y coordinate of second vector
     * @return the signed are of the parallelogram with side defined by the given
     *      vectors
     */
    private static double signedArea(final double x1, final double y1,
            final double x2, final double y2) {
        return (x1 * y2) + (-y1 * x2);
    }

    /** Compute the Euclidean norm.
     * @param x x coordinate value
     * @param y y coordinate value
     * @return Euclidean norm
     */
    public static double norm(final double x, final double y) {
        return Math.sqrt((x * x) + (y * y));
    }
}
