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
package org.apache.commons.geometry.core.partition.test;

import java.io.Serializable;

import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Side;

/** Class representing a line in two dimensional Euclidean space. This
 * class should only be used for testing purposes.
 */
public class TestLine implements Hyperplane<TestPoint2D>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190224L;

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
        double vecX = p2.getX() - p1.getX();
        double vecY = p2.getY() - p2.getY();

        double norm = norm(vecX, vecY);

        vecX /= norm;
        vecY /= norm;

        if (!Double.isFinite(vecX) || !Double.isFinite(vecY)) {
            throw new IllegalStateException("Unable to create line between points: " + p1 + ", " + p2);
        }

        this.directionX = vecX;
        this.directionY = vecY;

        this.originOffset = signedArea(vecX, vecY, p1.getX(), p1.getY());
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
    public double offset(TestPoint2D point) {
        return originOffset - signedArea(directionX, directionY, point.getX(), point.getY());
    }

    /** {@inheritDoc} */
    @Override
    public Side classify(TestPoint2D point) {
        final double offset = offset(point);
        final double cmp = PartitionTestUtils.PRECISION.compare(offset, 0.0);
        if (cmp == 0) {
            return Side.HYPER;
        }
        return cmp < 0 ? Side.MINUS : Side.PLUS;
    }

    /** Get the location of the given 2D point in the 1D space of the line.
     * @param point point to project into the line's 1D space
     * @return location of the point in the line's 1D space
     */
    public double toSubSpace(TestPoint2D point) {
        return (directionX * point.getX()) + (directionY * point.getY());
    }

    /** Get the 2D location of the given 1D location in the line's 1D space.
     * @param abscissa location in the line's 1D space.
     * @return the location of the given 1D point in 2D space
     */
    public TestPoint2D toSpace(final double abscissa) {
        final double ptX = (abscissa * directionX) + (-originOffset * directionY);
        final double ptY = (abscissa * directionY) + (originOffset * directionX);

        return new TestPoint2D(ptX, ptY);
    }

    /** {@inheritDoc} */
    @Override
    public TestPoint2D project(final TestPoint2D point) {
        return toSpace(toSubSpace(point));
    }

    /** {@inheritDoc} */
    @Override
    public boolean sameOrientation(Hyperplane<TestPoint2D> other) {
        final TestLine otherLine = (TestLine) other;
        final double dot = (directionX * otherLine.directionX) + (directionY * otherLine.directionY);
        return dot >= 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public ConvexSubHyperplane<TestPoint2D> wholeHyperplane() {
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
