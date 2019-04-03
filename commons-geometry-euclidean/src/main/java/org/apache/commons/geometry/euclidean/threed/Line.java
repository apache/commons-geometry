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
package org.apache.commons.geometry.euclidean.threed;

import java.util.Objects;

import org.apache.commons.geometry.core.partitioning.Embedding;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.IntervalsSet;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.numbers.core.Precision;

/** The class represent lines in a three dimensional space.

 * <p>Each oriented line is intrinsically associated with an abscissa
 * which is a coordinate on the line. The point at abscissa 0 is the
 * orthogonal projection of the origin on the line, another equivalent
 * way to express this is to say that it is the point of the line
 * which is closest to the origin. Abscissa increases in the line
 * direction.</p>0
 */
public class Line implements Embedding<Vector3D, Vector1D> {

    /** Line direction. */
    private Vector3D direction;

    /** Line point closest to the origin. */
    private Vector3D zero;

    /** Precision context used to compare floating point numbers. */
    private final DoublePrecisionContext precision;

    /** Build a line from two points.
     * @param p1 first point belonging to the line (this can be any point)
     * @param p2 second point belonging to the line (this can be any point, different from p1)
     * @param precision precision context used to compare floating point values
     * @exception IllegalArgumentException if the points are equal
     */
    public Line(final Vector3D p1, final Vector3D p2, final DoublePrecisionContext precision)
        throws IllegalArgumentException {
        reset(p1, p2);
        this.precision = precision;
    }

    /** Copy constructor.
     * <p>The created instance is completely independent from the
     * original instance, it is a deep copy.</p>
     * @param line line to copy
     */
    public Line(final Line line) {
        this.direction = line.direction;
        this.zero      = line.zero;
        this.precision = line.precision;
    }

    /** Reset the instance as if built from two points.
     * @param p1 first point belonging to the line (this can be any point)
     * @param p2 second point belonging to the line (this can be any point, different from p1)
     * @exception IllegalArgumentException if the points are equal
     */
    public void reset(final Vector3D p1, final Vector3D p2) {
        final Vector3D delta = p2.subtract(p1);
        final double norm2 = delta.normSq();
        if (norm2 == 0.0) {
            throw new IllegalArgumentException("Points are equal");
        }
        this.direction = Vector3D.linearCombination(1.0 / Math.sqrt(norm2), delta);
        this.zero = Vector3D.linearCombination(1.0, p1, -p1.dot(delta) / norm2, delta);
    }

    /** Get the object used to determine floating point equality for this instance.
     * @return the floating point precision context for the instance
     */
    public DoublePrecisionContext getPrecision() {
        return precision;
    }

    /** Get a line with reversed direction.
     * @return a new instance, with reversed direction
     */
    public Line revert() {
        final Line reverted = new Line(this);
        reverted.direction = reverted.direction.negate();
        return reverted;
    }

    /** Get the normalized direction vector.
     * @return normalized direction vector
     */
    public Vector3D getDirection() {
        return direction;
    }

    /** Get the line point closest to the origin.
     * @return line point closest to the origin
     */
    public Vector3D getOrigin() {
        return zero;
    }

    /** Get the abscissa of a point with respect to the line.
     * <p>The abscissa is 0 if the projection of the point and the
     * projection of the frame origin on the line are the same
     * point.</p>
     * @param point point to check
     * @return abscissa of the point
     */
    public double getAbscissa(final Vector3D point) {
        return point.subtract(zero).dot(direction);
    }

    /** Get one point from the line.
     * @param abscissa desired abscissa for the point
     * @return one point belonging to the line, at specified abscissa
     */
    public Vector3D pointAt(final double abscissa) {
        return Vector3D.linearCombination(1.0, zero, abscissa, direction);
    }

    /** Transform a space point into a sub-space point.
     * @param point n-dimension point of the space
     * @return (n-1)-dimension point of the sub-space corresponding to
     * the specified space point
     */
    @Override
    public Vector1D toSubSpace(final Vector3D point) {
        return Vector1D.of(getAbscissa(point));
    }

    /** Transform a sub-space point into a space point.
     * @param point (n-1)-dimension point of the sub-space
     * @return n-dimension point of the space corresponding to the
     * specified sub-space point
     */
    @Override
    public Vector3D toSpace(final Vector1D point) {
        return pointAt(point.getX());
    }

    /** Check if the instance is similar to another line.
     * <p>Lines are considered similar if they contain the same
     * points. This does not mean they are equal since they can have
     * opposite directions.</p>
     * @param line line to which instance should be compared
     * @return true if the lines are similar
     */
    public boolean isSimilarTo(final Line line) {
        final double angle = direction.angle(line.direction);
        return (precision.eqZero(angle) || precision.eq(angle, Math.PI)) && contains(line.zero);
    }

    /** Check if the instance contains a point.
     * @param p point to check
     * @return true if p belongs to the line
     */
    public boolean contains(final Vector3D p) {
        return precision.eqZero(distance(p));
    }

    /** Compute the distance between the instance and a point.
     * @param p to check
     * @return distance between the instance and the point
     */
    public double distance(final Vector3D p) {
        final Vector3D d = p.subtract(zero);
        final Vector3D n = Vector3D.linearCombination(1.0, d, -d.dot(direction), direction);
        return n.norm();
    }

    /** Compute the shortest distance between the instance and another line.
     * @param line line to check against the instance
     * @return shortest distance between the instance and the line
     */
    public double distance(final Line line) {

        final Vector3D normal = direction.cross(line.direction);
        final double n = normal.norm();
        if (n < Precision.SAFE_MIN) {
            // lines are parallel
            return distance(line.zero);
        }

        // signed separation of the two parallel planes that contains the lines
        final double offset = line.zero.subtract(zero).dot(normal) / n;

        return Math.abs(offset);

    }

    /** Compute the point of the instance closest to another line.
     * @param line line to check against the instance
     * @return point of the instance closest to another line
     */
    public Vector3D closestPoint(final Line line) {

        final double cos = direction.dot(line.direction);
        final double n = 1 - cos * cos;
        if (n < Precision.EPSILON) {
            // the lines are parallel
            return zero;
        }

        final Vector3D delta0 = line.zero.subtract(zero);
        final double a        = delta0.dot(direction);
        final double b        = delta0.dot(line.direction);

        return Vector3D.linearCombination(1, zero, (a - b * cos) / n, direction);

    }

    /** Get the intersection point of the instance and another line.
     * @param line other line
     * @return intersection point of the instance and the other line
     * or null if there are no intersection points
     */
    public Vector3D intersection(final Line line) {
        final Vector3D closest = closestPoint(line);
        return line.contains(closest) ? closest : null;
    }

    /** Build a sub-line covering the whole line.
     * @return a sub-line covering the whole line
     */
    public SubLine wholeLine() {
        return new SubLine(this, new IntervalsSet(precision));
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(direction, precision, zero);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Line other = (Line) obj;
        return this.direction.equals(other.direction, precision) && this.zero.equals(other.zero, precision);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Line [direction=" + direction + ", zero=" + zero + "]";
    }
}
