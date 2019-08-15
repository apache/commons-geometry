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

import java.util.function.BiFunction;

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.partitioning.AbstractEmbeddingSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.twod.SubLine.SubLineBuilder;

/** Internal base class for subline implementations.
 */
abstract class AbstractSubLine<R extends Region<Vector1D>>
    extends AbstractEmbeddingSubHyperplane<Vector2D, Vector1D, Line> {

    /** Serializable UID */
    private static final long serialVersionUID = 20190729L;

    /** The line defining this instance. */
    private final Line line;

    AbstractSubLine(final Line line) {
        this.line = line;
    }

    /** Get the line that this segment lies on. This method is an alias
     * for {@link getHyperplane()}.
     * @return the line that this segment lies on
     * @see #getHyperplane()
     */
    public Line getLine() {
        return getHyperplane();
    }

    /** {@inheritDoc} */
    @Override
    public Line getHyperplane() {
        return line;
    }

    /** {@inheritDoc} */
    @Override
    public SubLineBuilder builder() {
        return new SubLineBuilder(line);
    }

    /** Return the object used to perform floating point comparisons, which is the
     * same object used by the underlying {@link Line).
     * @return precision object used to perform floating point comparisons.
     */
    public DoublePrecisionContext getPrecision() {
        return line.getPrecision();
    }

    /** Generic, internal split method. Subclasses should call this from their
     * {@link #split(Hyperplane)} methods.
     * @param splitter splitting hyperplane
     * @param thisInstance a reference to the current instance; this is passed as
     *      an argument in order to allow it to be a generic type
     * @param factory function used to create new subhyperplane instances
     * @return the result of the split operation
     */
    protected <T extends AbstractSubLine<R>> Split<T> splitInternal(final Hyperplane<Vector2D> splitter,
            final T thisInstance, final BiFunction<Line, HyperplaneBoundedRegion<Vector1D>, T> factory) {

        final Line thisLine = getLine();
        final Line splitterLine = (Line) splitter;
        final DoublePrecisionContext precision = getPrecision();

        final Vector2D intersection = splitterLine.intersection(thisLine);
        if (intersection == null) {
            // the lines are parallel or coincident; check which side of
            // the splitter we lie on
            final double offset = splitterLine.offset(thisLine);
            final int comp = precision.compare(offset, 0.0);

            if (comp < 0) {
                return new Split<>(thisInstance, null);
            }
            else if (comp > 0) {
                return new Split<>(null, thisInstance);
            }
            else {
                return new Split<>(null, null);
            }
        }
        else {
            // the lines intersect; split the subregion
            final Vector1D splitPt = thisLine.toSubspace(intersection);
            final boolean positiveFacing = thisLine.angle(splitterLine) > 0.0;

            final OrientedPoint subspaceSplitter = OrientedPoint.fromPointAndDirection(splitPt,
                    positiveFacing, getPrecision());

            final Split<? extends HyperplaneBoundedRegion<Vector1D>> split = thisInstance.getSubspaceRegion().split(subspaceSplitter);
            final SplitLocation subspaceSplitLoc = split.getLocation();

            if (SplitLocation.MINUS == subspaceSplitLoc) {
                return new Split<>(thisInstance, null);
            }
            else if (SplitLocation.PLUS == subspaceSplitLoc) {
                return new Split<>(null, thisInstance);
            }

            final T minus = (split.getMinus() != null) ? factory.apply(thisLine, split.getMinus()) : null;
            final T plus = (split.getPlus() != null) ? factory.apply(thisLine, split.getPlus()) : null;

            return new Split<>(minus, plus);
        }
    }
}
