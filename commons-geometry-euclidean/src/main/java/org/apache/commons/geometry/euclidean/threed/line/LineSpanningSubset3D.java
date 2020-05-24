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

import java.text.MessageFormat;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;

/** Class representing the span of a line in 3D Euclidean space. This is the set of all points
 * contained by the line.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
final class LineSpanningSubset3D extends LineConvexSubset3D {

    /** Construct a new instance for the given line.
     * @param line line to construct the span for
     */
    LineSpanningSubset3D(final Line3D line) {
        super(line);
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

    /** {@inheritDoc}
     *
     * <p>This method always returns {@code null}.</p>
     */
    @Override
    public Vector3D getBarycenter() {
        return null; // infinite; no center
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@code null}.</p>
    */
    @Override
    public Bounds3D getBounds() {
        return null; // infinite; no bounds
    }

    /** {@inheritDoc} */
    @Override
    public LineSpanningSubset3D transform(final Transform<Vector3D> transform) {
        return new LineSpanningSubset3D(getLine().transform(transform));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final Line3D line = getLine();

        return MessageFormat.format(Line3D.TO_STRING_FORMAT,
                getClass().getSimpleName(),
                line.getOrigin(),
                line.getDirection());
    }

    /** {@inheritDoc} */
    @Override
    boolean containsAbscissa(final double abscissa) {
        return true;
    }
}
