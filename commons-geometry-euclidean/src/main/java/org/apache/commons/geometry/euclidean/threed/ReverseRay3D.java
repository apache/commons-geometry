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

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;

/** Class representing a portion of a line in 3D Euclidean space that starts at infinity and
 * continues in the direction of the line up to a single end point. This is equivalent to taking a
 * {@link Ray3D} and reversing the line direction.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see Ray3D
 */
public final class ReverseRay3D extends ConvexSubLine3D {

    /** The abscissa of the subline endpoint. */
    private final double end;

    /** Construct a new instance from the given line and end point. The end point is projected onto
     * the line. No validation is performed.
     * @param line line for the instance
     * @param endPoint end point for the instance
     */
    ReverseRay3D(final Line3D line, final Vector3D endPoint) {
        this(line, line.abscissa(endPoint));
    }

    /** Construct a new instance from the given line and 1D end location. No validation is performed.
     * @param line line for the instance
     * @param end end location for the instance
     */
    ReverseRay3D(final Line3D line, final double end) {
        super(line);

        this.end = end;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@code true}.</p>
    */
    @Override
    public boolean isInfinite() {
        return true;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@code false}.</p>
    */
    @Override
    public boolean isFinite() {
        return false;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@link Double#POSITIVE_INFINITY}.</p>
    */
    @Override
    public double getSize() {
        return Double.POSITIVE_INFINITY;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@code null}.</p>
    */
    @Override
    public Vector3D getStartPoint() {
        return null;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@link Double#NEGATIVE_INFINITY}.</p>
    */
    @Override
    public double getSubspaceStart() {
        return Double.NEGATIVE_INFINITY;
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getEndPoint() {
        return getLine().toSpace(end);
    }

    /** {@inheritDoc} */
    @Override
    public double getSubspaceEnd() {
        return end;
    }

    /** {@inheritDoc} */
    @Override
    public ReverseRay3D transform(final Transform<Vector3D> transform) {
        final Line3D tLine = getLine().transform(transform);
        final Vector3D tEnd = transform.apply(getEndPoint());

        return new ReverseRay3D(tLine, tEnd);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[direction= ")
            .append(getLine().getDirection())
            .append(", endPoint= ")
            .append(getEndPoint())
            .append(']');

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    boolean containsAbscissa(final double abscissa) {
        return getPrecision().lte(abscissa, end);
    }

    /** Construct a new instance from an end point and a line direction.
     * @param endPoint instance end point
     * @param lineDirection line direction
     * @param precision precision context used for floating point comparisons
     * @return a new instance with the given end point and line direction
     * @throws IllegalArgumentException If {@code lineDirection} has zero length, as evaluated by the
     *      given precision context
     * @see Line3D#fromPointAndDirection(Vector3D, Vector3D, DoublePrecisionContext)
     */
    public static ReverseRay3D fromPointAndDirection(final Vector3D endPoint, final Vector3D lineDirection,
            final DoublePrecisionContext precision) {
        final Line3D line = Line3D.fromPointAndDirection(endPoint, lineDirection, precision);

        return new ReverseRay3D(line, endPoint);
    }

    /** Construct a new instance starting at infinity and continuing in the direction of {@code line}
     * to the given end point. The point is projected onto the line.
     * @param line line for the instance
     * @param endPoint end point for the instance
     * @return a new instance starting at infinity and continuing along the line to {@code endPoint}
     * @throws IllegalArgumentException if any coordinate in {@code endPoint} is NaN or infinite
     */
    public static ReverseRay3D fromPoint(final Line3D line, final Vector3D endPoint) {
        return fromLocation(line, line.abscissa(endPoint));
    }

    /** Construct a new instance starting at infinity and continuing in the direction of {@code line}
     * to the given 1D end location.
     * @param line line for the instance
     * @param endLocation 1D location of the instance end point
     * @return a new instance starting infinity and continuing in the direction of {@code line}
     *      to the given 1D end location
     * @throws IllegalArgumentException if {@code endLocation} is NaN or infinite
     */
    public static ReverseRay3D fromLocation(final Line3D line, final double endLocation) {
        if (!Double.isFinite(endLocation)) {
            throw new IllegalArgumentException("Invalid reverse ray end location: " + Double.toString(endLocation));
        }

        return new ReverseRay3D(line, endLocation);
    }
}
