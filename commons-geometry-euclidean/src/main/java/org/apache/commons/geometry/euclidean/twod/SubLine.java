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

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Vector1D;

/** Class representing a subline in 2D Euclidean space. A subline is defined in this library
 * as a subset of the points lying on a line. For example, line segments and rays are sublines.
 * Sublines may be finite or infinite.
 */
public abstract class SubLine implements SubHyperplane<Vector2D> {
    /** The line defining this instance. */
    private final Line line;

    /** Construct a new instance based on the given line.
     * @param line line forming the base of the instance
     */
    SubLine(final Line line) {
        this.line = line;
    }

    /** Get the line containing this subline. This method is an alias
     * for {@link #getHyperplane()}.
     * @return the line containing this subline
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
    public RegionBSPTreeSubLine.Builder builder() {
        return new RegionBSPTreeSubLine.Builder(line);
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final Vector2D pt) {
        if (line.contains(pt)) {
            return classifyAbscissa(line.abscissa(pt));
        }

        return RegionLocation.OUTSIDE;
    }

    /** Get the unique intersection of this subline with the given line. Null is
     * returned if no unique intersection point exists (ie, the lines are
     * parallel or coincident) or the line does not intersect the subline.
     * @param inputLine line to intersect with this subline
     * @return the unique intersection point between the line and this subline
     *      or null if no such point exists.
     * @see Line#intersection(Line)
     */
    public Vector2D intersection(final Line inputLine) {
        final Vector2D pt = line.intersection(inputLine);
        return (pt != null && contains(pt)) ? pt : null;
    }

    /** Get the unique intersection of this instance with the given subline. Null
     * is returned if the lines containing the sublines do not have a unique intersection
     * point (ie, they are parallel or coincident) or the intersection point is unique
     * but in not contained in both sublines.
     * @param subline subline to intersect with
     * @return the unique intersection point between this subline and the argument or
     *      null if no such point exists.
     * @see Line#intersection(Line)
     */
    public Vector2D intersection(final SubLine subline) {
        final Vector2D pt = intersection(subline.getLine());
        return (pt != null && subline.contains(pt)) ? pt : null;
    }

    /** Return the object used to perform floating point comparisons, which is the
     * same object used by the underlying {@link Line}).
     * @return precision object used to perform floating point comparisons.
     */
    public DoublePrecisionContext getPrecision() {
        return line.getPrecision();
    }

    /** Get the 1D subspace region for this subline.
     * @return the 1D subspace region for this subline
     */
    public abstract HyperplaneBoundedRegion<Vector1D> getSubspaceRegion();

    /** Classify the given line abscissa value with respect to the subspace region.
     * @param abscissa the abscissa value to classify
     * @return the region location of the line abscissa value
     */
    abstract RegionLocation classifyAbscissa(double abscissa);

    /** Get a split result for cases where no intersection exists between the splitting line and the
     * line underlying the given subline. This occurs when the two lines are parallel or coincident.
     * @param <T> Subline type
     * @param splitter splitting line
     * @param subline subline instance being split
     * @return return result of the non-intersecting split operation
     */
    <T extends SubLine> Split<T> getNonIntersectingSplitResult(final Line splitter, final T subline) {
        // check which side of the splitter we lie on
        final double offset = splitter.offset(subline.getLine());
        final int comp = getPrecision().compare(offset, 0.0);

        if (comp < 0) {
            return new Split<>(subline, null);
        } else if (comp > 0) {
            return new Split<>(null, subline);
        } else {
            return new Split<>(null, null);
        }
    }

    /** Return true if the plus side of the given splitter line is facing in the positive direction
     * of this line.
     * @param splitterLine line splitting this instance
     * @return true if the plus side of the given line is facing in the positive direction of this
     *      line
     */
    boolean splitterPlusIsPositiveFacing(final Line splitterLine) {
        return line.getOffsetDirection().dot(splitterLine.getDirection()) <= 0;
    }

    /** Create a split result for the given splitter line, given the low and high split portion of this
     * instance. The arguments are assigned to the split result's minus and plus properties based on the
     * relative orientation of the splitter line.
     * @param <T> Subline type
     * @param splitter splitter line
     * @param low portion of the split result closest to negative infinity on this line
     * @param high portion of th split result closest to positive infinity on this line
     * @return a split result for the given splitter line.
     */
    <T extends SubLine> Split<T> createSplitResult(final Line splitter, final T low, final T high) {
        return splitterPlusIsPositiveFacing(splitter) ?
                new Split<>(low, high) :
                new Split<>(high, low);
    }
}
