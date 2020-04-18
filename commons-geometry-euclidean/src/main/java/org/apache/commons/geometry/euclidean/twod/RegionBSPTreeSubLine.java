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
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.twod.Line.SubspaceTransform;

/** Class representing an arbitrary subset of a line using a {@link RegionBSPTree1D}.
 * This class can represent convex, non-convex, finite, infinite, and empty regions.
 *
 * <p>This class is mutable and <em>not</em> thread safe.</p>
 */
public final class RegionBSPTreeSubLine extends SubLine {
    /** The 1D region representing the area on the line. */
    private final RegionBSPTree1D region;

    /** Construct a new, empty subline for the given line.
     * @param line line defining the subline
     */
    public RegionBSPTreeSubLine(final Line line) {
        this(line, false);
    }

    /** Construct a new subline for the given line. If {@code full}
     * is true, then the subline will cover the entire line; otherwise,
     * it will be empty.
     * @param line line defining the subline
     * @param full if true, the subline will cover the entire space;
     *      otherwise it will be empty
     */
    public RegionBSPTreeSubLine(final Line line, boolean full) {
        this(line, new RegionBSPTree1D(full));
    }

    /** Construct a new instance from its defining line and subspace region. The give
     * BSP tree is used directly by this instance; it is not copied.
     * @param line line defining the subline
     * @param region subspace region for the subline
     */
    public RegionBSPTreeSubLine(final Line line, final RegionBSPTree1D region) {
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
    public RegionBSPTreeSubLine transform(final Transform<Vector2D> transform) {
        final SubspaceTransform st = getLine().subspaceTransform(transform);

        final RegionBSPTree1D tRegion = RegionBSPTree1D.empty();
        tRegion.copy(region);
        tRegion.transform(st.getTransform());

        return new RegionBSPTreeSubLine(st.getLine(), tRegion);
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexSubLine> toConvex() {
        final List<Interval> intervals = region.toIntervals();

        final Line line = getLine();
        final List<ConvexSubLine> sublines = new ArrayList<>(intervals.size());

        for (final Interval interval : intervals) {
            sublines.add(ConvexSubLine.fromInterval(line, interval));
        }

        return sublines;
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
    public Split<RegionBSPTreeSubLine> split(final Hyperplane<Vector2D> splitter) {
        final Line thisLine = getLine();
        final Line splitterLine = (Line) splitter;
        final DoublePrecisionContext precision = getPrecision();

        final Vector2D intersection = splitterLine.intersection(thisLine);
        if (intersection == null) {
            return getNonIntersectingSplitResult(splitterLine, this);
        }

        final double abscissa = thisLine.abscissa(intersection);
        final OrientedPoint subspaceSplitter = OrientedPoint.fromLocationAndDirection(
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

        final RegionBSPTreeSubLine minus = (subspaceSplit.getMinus() != null) ?
                new RegionBSPTreeSubLine(thisLine, subspaceSplit.getMinus()) :
                null;
        final RegionBSPTreeSubLine plus = (subspaceSplit.getPlus() != null) ?
                new RegionBSPTreeSubLine(thisLine, subspaceSplit.getPlus()) :
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

    /** Add a convex subline to this instance.
     * @param convexSubline the convex subline to add
     * @throws IllegalArgumentException if the given subline is not from
     *      a line equivalent to this instance
     */
    public void add(final ConvexSubLine convexSubline) {
        validateLine(convexSubline.getLine());

        region.add(convexSubline.getInterval());
    }

    /** Add the region represented by the given subline to this instance.
     * The argument is not modified.
     * @param subline subline to add
     * @throws IllegalArgumentException if the given subline is not from
     *      a line equivalent to this instance
     */
    public void add(final RegionBSPTreeSubLine subline) {
        validateLine(subline.getLine());

        region.union(subline.getSubspaceRegion());
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
     * defining this subline.
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

    /** {@link SubHyperplane.Builder} implementation for sublines.
     */
    public static final class Builder implements SubHyperplane.Builder<Vector2D> {

        /** SubLine instance created by this builder. */
        private final RegionBSPTreeSubLine subline;

        /** Construct a new instance for building a subline region for the given line.
         * @param line the underlying line for the subline region
         */
        public Builder(final Line line) {
            this.subline = new RegionBSPTreeSubLine(line);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final SubHyperplane<Vector2D> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public void add(final ConvexSubHyperplane<Vector2D> sub) {
            addInternal(sub);
        }

        /** {@inheritDoc} */
        @Override
        public RegionBSPTreeSubLine build() {
            return subline;
        }

        /** Internal method for adding subhyperplanes to this builder.
         * @param sub the subhyperplane to add; either convex or non-convex
         */
        private void addInternal(final SubHyperplane<Vector2D> sub) {
            if (sub instanceof ConvexSubLine) {
                subline.add((ConvexSubLine) sub);
            } else if (sub instanceof RegionBSPTreeSubLine) {
                subline.add((RegionBSPTreeSubLine) sub);
            } else {
                throw new IllegalArgumentException("Unsupported subhyperplane type: " + sub.getClass().getName());
            }
        }
    }
}
