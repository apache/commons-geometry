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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.geometry.euclidean.oned.OrientedPoints;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.twod.Line.SubspaceTransform;

/** Class representing an arbitrary subset of a line using a {@link RegionBSPTree1D}.
 * This class can represent convex, non-convex, finite, infinite, and empty regions.
 *
 * <p>This class is mutable and <em>not</em> thread safe.</p>
 */
public final class EmbeddedTreeLineSubset extends LineSubset {
    /** The 1D region representing the area on the line. */
    private final RegionBSPTree1D region;

    /** Construct a new, empty subset for the given line.
     * @param line line defining the subset
     */
    public EmbeddedTreeLineSubset(final Line line) {
        this(line, false);
    }

    /** Construct a new subset for the given line. If {@code full}
     * is true, then the subset will cover the entire line; otherwise,
     * it will be empty.
     * @param line line defining the subset
     * @param full if true, the subset will cover the entire space;
     *      otherwise it will be empty
     */
    public EmbeddedTreeLineSubset(final Line line, boolean full) {
        this(line, new RegionBSPTree1D(full));
    }

    /** Construct a new instance from its defining line and subspace region. The give
     * BSP tree is used directly by this instance; it is not copied.
     * @param line line defining the subset
     * @param region subspace region for the instance
     */
    public EmbeddedTreeLineSubset(final Line line, final RegionBSPTree1D region) {
        super(line);

        this.region = region;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        return region.isFull();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return region.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return Double.isInfinite(getSize());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinite() {
        return Double.isFinite(getSize());
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return region.getSize();
    }

    /** {@inheritDoc} */
    @Override
    public EmbeddedTreeLineSubset transform(final Transform<Vector2D> transform) {
        final SubspaceTransform st = getLine().subspaceTransform(transform);

        final RegionBSPTree1D tRegion = RegionBSPTree1D.empty();
        tRegion.copy(region);
        tRegion.transform(st.getTransform());

        return new EmbeddedTreeLineSubset(st.getLine(), tRegion);
    }

    /** {@inheritDoc} */
    @Override
    public List<LineConvexSubset> toConvex() {
        final List<Interval> intervals = region.toIntervals();

        final Line line = getLine();
        final List<LineConvexSubset> convexSubsets = new ArrayList<>(intervals.size());

        for (final Interval interval : intervals) {
            convexSubsets.add(Lines.subsetFromInterval(line, interval));
        }

        return convexSubsets;
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree1D getSubspaceRegion() {
        return region;
    }

    /** {@inheritDoc}
     *
     * <p>In all cases, the current instance is not modified. However, In order to avoid
     * unnecessary copying, this method will use the current instance as the split value when
     * the instance lies entirely on the plus or minus side of the splitter. For example, if
     * this instance lies entirely on the minus side of the splitter, the subplane
     * returned by {@link Split#getMinus()} will be this instance. Similarly, {@link Split#getPlus()}
     * will return the current instance if it lies entirely on the plus side. Callers need to make
     * special note of this, since this class is mutable.</p>
     */
    @Override
    public Split<EmbeddedTreeLineSubset> split(final Hyperplane<Vector2D> splitter) {
        final Line thisLine = getLine();
        final Line splitterLine = (Line) splitter;
        final DoublePrecisionContext precision = getPrecision();

        final Vector2D intersection = splitterLine.intersection(thisLine);
        if (intersection == null) {
            return getNonIntersectingSplitResult(splitterLine, this);
        }

        final double abscissa = thisLine.abscissa(intersection);
        final OrientedPoint subspaceSplitter = OrientedPoints.fromLocationAndDirection(
                abscissa,
                splitterPlusIsPositiveFacing(splitterLine),
                precision);

        final Split<RegionBSPTree1D> subspaceSplit = region.split(subspaceSplitter);
        final SplitLocation subspaceSplitLoc = subspaceSplit.getLocation();

        if (SplitLocation.MINUS == subspaceSplitLoc) {
            return new Split<>(this, null);
        } else if (SplitLocation.PLUS == subspaceSplitLoc) {
            return new Split<>(null, this);
        }

        final EmbeddedTreeLineSubset minus = (subspaceSplit.getMinus() != null) ?
                new EmbeddedTreeLineSubset(thisLine, subspaceSplit.getMinus()) :
                null;
        final EmbeddedTreeLineSubset plus = (subspaceSplit.getPlus() != null) ?
                new EmbeddedTreeLineSubset(thisLine, subspaceSplit.getPlus()) :
                null;

        return new Split<>(minus, plus);
    }

    /** {@inheritDoc} */
    @Override
    public Vector2D closest(final Vector2D pt) {
        final Line line = getLine();

        final Vector1D subPt = line.toSubspace(pt);
        if (region.contains(subPt)) {
            return line.toSpace(subPt);
        }

        final Vector1D subProjected = region.project(subPt);
        return subProjected != null ?
                line.toSpace(subProjected) :
                null;
    }

    /** Add a line subset to this instance.
     * @param subset the line subset to add
     * @throws IllegalArgumentException if the given line subset is not from
     *      a line equivalent to this instance
     */
    public void add(final LineConvexSubset subset) {
        validateLine(subset.getLine());

        region.add(subset.getInterval());
    }

    /** Add the region represented by the given line subset to this instance.
     * The argument is not modified.
     * @param subset line subset to add
     * @throws IllegalArgumentException if the given line subset is not from
     *      a line equivalent to this instance
     */
    public void add(final EmbeddedTreeLineSubset subset) {
        validateLine(subset.getLine());

        region.union(subset.getSubspaceRegion());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final Line line = getLine();

        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append('[')
            .append("lineOrigin= ")
            .append(line.getOrigin())
            .append(", lineDirection= ")
            .append(line.getDirection())
            .append(", region= ")
            .append(region)
            .append(']');

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    RegionLocation classifyAbscissa(final double abscissa) {
        return region.classify(abscissa);
    }

    /** Validate that the given line is equivalent to the line
     * defining this instance.
     * @param inputLine the line to validate
     * @throws IllegalArgumentException if the given line is not equivalent
     *      to the line for this instance
     */
    private void validateLine(final Line inputLine) {
        final Line line = getLine();

        if (!line.eq(inputLine, line.getPrecision())) {
            throw new IllegalArgumentException("Argument is not on the same " +
                    "line. Expected " + line + " but was " +
                    inputLine);
        }
    }

    /** {@link HyperplaneSubset.Builder} implementation for line subsets.
     */
    public static final class Builder implements HyperplaneSubset.Builder<Vector2D> {

        /** Line subset instance created by this builder. */
        private final EmbeddedTreeLineSubset lineSubset;

        /** Construct a new instance for building a line subset for the given line.
         * @param line the underlying line for the subset
         */
        public Builder(final Line line) {
            this.lineSubset = new EmbeddedTreeLineSubset(line);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final HyperplaneSubset<Vector2D> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final HyperplaneConvexSubset<Vector2D> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public EmbeddedTreeLineSubset build() {
            return lineSubset;
        }

        /** Internal method for adding hyperplane subsets to this builder.
         * @param sub the hyperplane subset to add; either convex or non-convex
         */
        private void addInternal(final HyperplaneSubset<Vector2D> sub) {
            if (sub instanceof LineConvexSubset) {
                lineSubset.add((LineConvexSubset) sub);
            } else if (sub instanceof EmbeddedTreeLineSubset) {
                lineSubset.add((EmbeddedTreeLineSubset) sub);
            } else {
                throw new IllegalArgumentException("Unsupported hyperplane subset type: " + sub.getClass().getName());
            }
        }
    }
}
