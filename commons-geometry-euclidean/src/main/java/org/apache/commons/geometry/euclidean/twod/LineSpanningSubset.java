/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.euclidean.twod;

import java.text.MessageFormat;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Split;

/** Class representing the span of a line in 2D Euclidean space. This is the set of all points
 * contained by the line.
 *
 * <p>Instances of this class are guaranteed to be immutable.</p>
 */
final class LineSpanningSubset extends LineConvexSubset {

    /** Construct a new instance for the given line.
     * @param line line to construct the span for
     */
    LineSpanningSubset(final Line line) {
        super(line);
    }

    /** {@inheritDoc}
     *
     * <p>This method always returns {@code true}.</p>
     */
    @Override
    public boolean isFull() {
        return true;
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
    public Vector2D getCentroid() {
        return null;
    }

    /** {@inheritDoc}
    *
    * <p>This method always returns {@code null}.</p>
    */
    @Override
    public Vector2D getStartPoint() {
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
    public Vector2D getEndPoint() {
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
    public Bounds2D getBounds() {
        return null; // infinite; no bounds
    }

    /** {@inheritDoc} */
    @Override
    public LineSpanningSubset transform(final Transform<Vector2D> transform) {
        return new LineSpanningSubset(getLine().transform(transform));
    }

    /** {@inheritDoc} */
    @Override
    public LineSpanningSubset reverse() {
        return new LineSpanningSubset(getLine().reverse());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final Line line = getLine();

        return MessageFormat.format(Line.TO_STRING_FORMAT,
                getClass().getSimpleName(),
                line.getOrigin(),
                line.getDirection());
    }

    /** {@inheritDoc} */
    @Override
    double closestAbscissa(final double abscissa) {
        return abscissa;
    }

    /** {@inheritDoc} */
    @Override
    Split<LineConvexSubset> splitOnIntersection(final Line splitter, final Vector2D intersection) {
        final Line line = getLine();

        final ReverseRay low = new ReverseRay(line, intersection);
        final Ray high = new Ray(line, intersection);

        return createSplitResult(splitter, low, high);
    }
}
