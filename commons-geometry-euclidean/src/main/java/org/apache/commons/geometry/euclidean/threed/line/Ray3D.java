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
package org.apache.commons.geometry.euclidean.threed.line;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Class representing a ray in 3D Euclidean space. A ray is a portion of a line consisting of
 * a single start point and extending to infinity along the direction of the line.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 * @see ReverseRay3D
 * @see Lines3D
 * @see <a href="https://en.wikipedia.org/wiki/Line_(geometry)#Ray">Ray</a>
 */
public final class Ray3D extends LineConvexSubset3D {

    /** The start abscissa value for the ray. */
    private final double start;

    /** Construct a ray from a line and a start point. The start point is projected
     * onto the line. No validation is performed.
     * @param line line for the ray
     * @param startPoint start point for the ray
     */
    Ray3D(final Line3D line, final Vector3D startPoint) {
        this(line, line.abscissa(startPoint));
    }

    /** Construct a ray from a line and a 1D start location. No validation is performed.
     * @param line line for the ray
     * @param start 1D start location
     */
    Ray3D(final Line3D line, final double start) {
        super(line);

        this.start = start;
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

    @Override
    public Vector3D getStartPoint() {
        return getLine().toSpace(start);
    }

    /** {@inheritDoc} */
    @Override
    public double getSubspaceStart() {
        return start;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@code null}.</p>
    */
    @Override
    public Vector3D getEndPoint() {
        return null;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@link Double#POSITIVE_INFINITY}.</p>
    */
    @Override
    public double getSubspaceEnd() {
        return Double.POSITIVE_INFINITY;
    }

    /** Get the direction of the ray. This is a convenience method for {@code ray.getLine().getDirection()}.
     * @return the direction of the ray
     */
    public Vector3D getDirection() {
        return getLine().getDirection();
    }

    /** {@inheritDoc} */
    @Override
    public Ray3D transform(final Transform<Vector3D> transform) {
        final Line3D tLine = getLine().transform(transform);
        final Vector3D tStart = transform.apply(getStartPoint());

        return new Ray3D(tLine, tStart);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
            .append("[startPoint= ")
            .append(getStartPoint())
            .append(", direction= ")
            .append(getLine().getDirection())
            .append(']');

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    boolean containsAbscissa(final double abscissa) {
        return getLine().getPrecision().gte(abscissa, start);
    }
}
