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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.OrientedPoint;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.twod.Line.SubspaceTransform;

/** Class representing an arbitrary region of a line. This class can represent
 * both convex and non-convex regions of its underlying line.
 *
 * <p>This class is mutable and <em>not</em> thread safe.</p>
 */
public final class SubLine extends AbstractSubLine<RegionBSPTree1D> implements Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190717L;

    /** The 1D region representing the area on the line */
    private final RegionBSPTree1D region;

    /** Construct a new, empty subline for the given line.
     * @param line line defining the subline
     */
    public SubLine(final Line line) {
        this(line, false);
    }

    /** Construct a new subline for the given line. If {@code full}
     * is true, then the subline will cover the entire line; otherwise,
     * it will be empty.
     * @param line line defining the subline
     * @param full if true, the subline will cover the entire space;
     *      otherwise it will be empty
     */
    public SubLine(final Line line, boolean full) {
        this(line, new RegionBSPTree1D(full));
    }

    /** Construct a new instance from its defining line and subspace region.
     * @param line line defining the subline
     * @param region subspace region for the subline
     */
    public SubLine(final Line line, final RegionBSPTree1D region) {
        super(line);

        this.region = region;
    }

    /** {@inheritDoc} */
    @Override
    public SubLine transform(Transform<Vector2D> transform) {
        final SubspaceTransform st = getLine().subspaceTransform(transform);

        final RegionBSPTree1D tRegion = RegionBSPTree1D.empty();
        tRegion.copy(region);
        tRegion.transform(st.getTransform());

        return new SubLine(st.getLine(), tRegion);
    }

    /** {@inheritDoc} */
    @Override
    public List<Segment> toConvex() {
        final List<Interval> intervals = region.toIntervals();

        final Line line = getLine();
        final List<Segment> segments = new ArrayList<>(intervals.size());

        for (Interval interval : intervals) {
            segments.add(Segment.fromInterval(line, interval));
        }

        return segments;
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree1D getSubspaceRegion() {
        return region;
    }

    /** {@inheritDoc} */
    @Override
    public Split<SubLine> split(final Hyperplane<Vector2D> splitter) {
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
                return new Split<>(this, null);
            }
            else if (comp > 0) {
                return new Split<>(null, this);
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

            Split<RegionBSPTree1D> split = region.split(subspaceSplitter);

            final SubLine minus = (split.getMinus() != null) ? new SubLine(thisLine, split.getMinus()) : null;
            final SubLine plus = (split.getPlus() != null) ? new SubLine(thisLine, split.getPlus()) : null;

            return new Split<>(minus, plus);
        }
    }

    /** Add a line segment to this instance..
     * @param segment line segment to add
     * @throws IllegalArgumentException if the given line segment is not from
     *      a line equivalent to this instance
     */
    public void add(final Segment segment) {
        validateLine(segment.getLine());

        region.add(segment.getSubspaceRegion());
    }

    /** Add the region represented by the given subline to this instance.
     * The argument is not modified.
     * @param subline subline to add
     * @throws IllegalArgumentException if the given subline is not from
     *      a line equivalent to this instance
     */
    public void add(final SubLine subLine) {
        validateLine(subLine.getLine());

        region.union(subLine.getSubspaceRegion());
    }

    /** Validate that the given line is equivalent to the line
     * defining this subline.
     * @param inputLine the line to validate
     * @throws IllegalArgumentException if the given line is not equivalent
     *      to the line for this instance
     */
    private void validateLine(final Line inputLine) {
        final Line line = getLine();

        if (!line.eq(inputLine)) {
            throw new IllegalArgumentException("Argument is not on the same " +
                    "line. Expected " + line + " but was " +
                    inputLine);
        }
    }

    /** {@link Builder} implementation for sublines.
     */
    public static final class SubLineBuilder implements SubHyperplane.Builder<Vector2D> {

        /** SubLine instance created by this builder. */
        private final SubLine subline;

        /** Construct a new instance for building subline region for the given line.
         * @param line the underlying line for the subline region
         */
        public SubLineBuilder(final Line line) {
            this.subline = new SubLine(line);
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
        public SubLine build() {
            return subline;
        }

        /** Internal method for adding subhyperplanes to this builder.
         * @param sub the subhyperplane to add; either convex or non-convex
         */
        private void addInternal(final SubHyperplane<Vector2D> sub) {
            if (sub instanceof Segment) {
                subline.add((Segment) sub);
            }
            else if (sub instanceof SubLine) {
                subline.add((SubLine) sub);
            }
            else {
                throw new IllegalArgumentException("Unsupported subhyperplane type: " + sub.getClass().getName());
            }
        }
    }
}
