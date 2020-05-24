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
package org.apache.commons.geometry.core.partitioning.test;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;

/** Class representing a line segment in two dimensional Euclidean space. This
 * class should only be used for testing purposes.
 */
public class TestLineSegment implements HyperplaneConvexSubset<TestPoint2D> {
    /** Abscissa of the line segment start point. */
    private final double start;

    /** Abscissa of the line segment end point. */
    private final double end;

    /** The underlying line for the line segment. */
    private final TestLine line;

    /** Construct a line segment between two points.
     * @param start start point
     * @param end end point
     */
    public TestLineSegment(final TestPoint2D start, final TestPoint2D end) {
        this.line = new TestLine(start, end);

        final double startValue = line.toSubspaceValue(start);
        final double endValue = line.toSubspaceValue(end);

        this.start = Math.min(startValue, endValue);
        this.end = Math.max(startValue, endValue);
    }

    /** Construct a line segment between two points.
     * @param x1 x coordinate of first point
     * @param y1 y coordinate of first point
     * @param x2 x coordinate of second point
     * @param y2 y coordinate of second point
     */
    public TestLineSegment(final double x1, final double y1, final double x2, final double y2) {
        this(new TestPoint2D(x1, y1), new TestPoint2D(x2, y2));
    }

    /** Construct a line segment based on an existing line.
     * @param start abscissa of the line segment start point
     * @param end abscissa of the line segment end point
     * @param line the underyling line
     */
    public TestLineSegment(final double start, final double end, final TestLine line) {
        this.start = Math.min(start, end);
        this.end = Math.max(start, end);
        this.line = line;
    }

    /** Get the start abscissa value.
     * @return
     */
    public double getStart() {
        return start;
    }

    /** Get the end abscissa value.
     * @return
     */
    public double getEnd() {
        return end;
    }

    /** Get the start point of the line segment.
     * @return the start point of the line segment
     */
    public TestPoint2D getStartPoint() {
        return line.toSpace(start);
    }

    /** Get the end point of the line segment.
     * @return the end point of the line segment
     */
    public TestPoint2D getEndPoint() {
        return line.toSpace(end);
    }

    /** {@inheritDoc} */
    @Override
    public TestLine getHyperplane() {
        return line;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        return start < end && Double.isInfinite(start) && Double.isInfinite(end);

    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return PartitionTestUtils.PRECISION.eqZero(getSize());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return Double.isInfinite(getSize());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinite() {
        return !isInfinite();
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        return Math.abs(start - end);
    }

    /** {@inheritDoc} */
    @Override
    public TestPoint2D getBarycenter() {
        return line.toSpace(0.5 * (end - start));
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(TestPoint2D point) {
        if (line.contains(point)) {
            final double value = line.toSubspaceValue(point);

            final int startCmp = PartitionTestUtils.PRECISION.compare(value, start);
            final int endCmp = PartitionTestUtils.PRECISION.compare(value, end);

            if (startCmp == 0 || endCmp == 0) {
                return RegionLocation.BOUNDARY;
            } else if (startCmp > 0 && endCmp < 0) {
                return RegionLocation.INSIDE;
            }
        }

        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public TestPoint2D closest(TestPoint2D point) {
        double value = line.toSubspaceValue(point);
        value = Math.max(Math.min(value, end), start);

        return line.toSpace(value);
    }

    /** {@inheritDoc} */
    @Override
    public List<HyperplaneConvexSubset<TestPoint2D>> toConvex() {
        return Arrays.asList(this);
    }

    /** {@inheritDoc} */
    @Override
    public TestLineSegment reverse() {
        TestLine rLine = line.reverse();
        return new TestLineSegment(-end, -start, rLine);
    }

    /** {@inheritDoc} */
    @Override
    public Split<TestLineSegment> split(Hyperplane<TestPoint2D> splitter) {
        final TestLine splitterLine = (TestLine) splitter;

        if (isInfinite()) {
            return splitInfinite(splitterLine);
        }
        return splitFinite(splitterLine);
    }

    /** {@inheritDoc} */
    @Override
    public HyperplaneSubset.Builder<TestPoint2D> builder() {
        return new TestLineSegmentCollectionBuilder(line);
    }

    /** {@inheritDoc} */
    @Override
    public HyperplaneConvexSubset<TestPoint2D> transform(Transform<TestPoint2D> transform) {
        if (!isInfinite()) {
            // simple case; just transform the points directly
            TestPoint2D p1 = transform.apply(getStartPoint());
            TestPoint2D p2 = transform.apply(getEndPoint());

            return new TestLineSegment(p1, p2);
        }

        // determine how the line has transformed
        TestPoint2D p0 = transform.apply(line.toSpace(0));
        TestPoint2D p1 = transform.apply(line.toSpace(1));

        TestLine tLine = new TestLine(p0, p1);
        double translation = tLine.toSubspaceValue(p0);
        double scale = tLine.toSubspaceValue(p1);

        double tStart = (start * scale) + translation;
        double tEnd = (end * scale) + translation;

        return new TestLineSegment(tStart, tEnd, tLine);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName())
            .append("[start= ")
            .append(getStartPoint())
            .append(", end= ")
            .append(getEndPoint())
            .append("]");

        return sb.toString();
    }

    /** Method used to split the instance with the given line when the instance has
     * infinite size.
     * @param splitter the splitter line
     * @return the split convex subset
     */
    private Split<TestLineSegment> splitInfinite(TestLine splitter) {
        final TestPoint2D intersection = splitter.intersection(line);

        if (intersection == null) {
            // the lines are parallel
            final double originOffset = splitter.offset(line.getOrigin());

            final int sign = PartitionTestUtils.PRECISION.sign(originOffset);
            if (sign < 0) {
                return new Split<TestLineSegment>(this, null);
            } else if (sign > 0) {
                return new Split<TestLineSegment>(null, this);
            }
            return new Split<TestLineSegment>(null, null);
        } else {
            // the lines intersect
            final double intersectionAbscissa = line.toSubspaceValue(intersection);

            TestLineSegment startSegment = null;
            TestLineSegment endSegment = null;

            if (start < intersectionAbscissa) {
                startSegment = new TestLineSegment(start, intersectionAbscissa, line);
            }
            if (intersectionAbscissa < end) {
                endSegment = new TestLineSegment(intersectionAbscissa, end, line);
            }

            final double startOffset = splitter.offset(line.toSpace(intersectionAbscissa - 1));
            final double startCmp = PartitionTestUtils.PRECISION.sign(startOffset);

            final TestLineSegment minus = (startCmp > 0) ? endSegment : startSegment;
            final TestLineSegment plus = (startCmp > 0) ? startSegment : endSegment;

            return new Split<TestLineSegment>(minus, plus);
        }
    }

    /** Method used to split the instance with the given line when the instance has
     * finite size.
     * @param splitter the splitter line
     * @return the split convex subset
     */
    private Split<TestLineSegment> splitFinite(TestLine splitter) {

        final double startOffset = splitter.offset(line.toSpace(start));
        final double endOffset = splitter.offset(line.toSpace(end));

        final int startCmp = PartitionTestUtils.PRECISION.sign(startOffset);
        final int endCmp = PartitionTestUtils.PRECISION.sign(endOffset);

        // startCmp |   endCmp  |   result
        // --------------------------------
        // 0        |   0       |   hyper
        // 0        |   < 0     |   minus
        // 0        |   > 0     |   plus
        // < 0      |   0       |   minus
        // < 0      |   < 0     |   minus
        // < 0      |   > 0     |   SPLIT
        // > 0      |   0       |   plus
        // > 0      |   < 0     |   SPLIT
        // > 0      |   > 0     |   plus

        if (startCmp == 0 && endCmp == 0) {
            // the entire line segment is directly on the splitter line
            return new Split<TestLineSegment>(null, null);
        } else if (startCmp < 1 && endCmp < 1) {
            // the entire line segment is on the minus side
            return new Split<TestLineSegment>(this, null);
        } else if (startCmp > -1 && endCmp > -1) {
            // the entire line segment is on the plus side
            return new Split<TestLineSegment>(null, this);
        }

        // we need to split the line
        final TestPoint2D intersection = splitter.intersection(line);
        final double intersectionAbscissa = line.toSubspaceValue(intersection);

        final TestLineSegment startSegment = new TestLineSegment(start, intersectionAbscissa, line);
        final TestLineSegment endSegment = new TestLineSegment(intersectionAbscissa, end, line);

        final TestLineSegment minus = (startCmp > 0) ? endSegment : startSegment;
        final TestLineSegment plus = (startCmp > 0) ? startSegment : endSegment;

        return new Split<TestLineSegment>(minus, plus);
    }
}
