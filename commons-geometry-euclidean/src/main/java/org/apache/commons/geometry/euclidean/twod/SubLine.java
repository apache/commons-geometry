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

import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.SubHyperplane;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;;

/** Class representing an arbitrary region of a line. This class can represent
 * both convex and non-convex regions of its underlying line.
 *
 * <p>This class is <em>not</em> thread safe.</p>
 */
public final class SubLine extends AbstractSubLine<RegionBSPTree1D> {

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
    public boolean isInfinite() {
        return Double.isInfinite(region.getSize());
    }

    /** {@inheritDoc} */
    @Override
    public List<LineSegment> toConvex() {
        final List<Interval> intervals = region.toIntervals();

        final Line line = getLine();
        final List<LineSegment> segments = new ArrayList<>(intervals.size());

        for (Interval interval : intervals)
        {
            segments.add(LineSegment.fromInterval(line, interval));
        }

        return segments;
    }

    /** {@inheritDoc} */
    @Override
    public RegionBSPTree1D getSubspaceRegion() {
        return region;
    }

    /** Add a line segment to this instance..
     * @param segment line segment to add
     * @throws IllegalArgumentException if the given line segment is not from
     *      a line equivalent to this instance
     */
    public void add(final LineSegment segment) {
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
            if (sub instanceof LineSegment) {
                subline.add((LineSegment) sub);
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
