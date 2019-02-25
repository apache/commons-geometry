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
package org.apache.commons.geometry.core.partition.test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.SplitConvexSubHyperplane;

/** Class representing a line segment in two dimensional Euclidean space. This
 * class should only be used for testing purposes.
 */
public class TestLineSegment implements ConvexSubHyperplane<TestPoint2D>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190224L;

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
        this.start = line.toSubSpace(start);
        this.end = line.toSubSpace(end);
    }

    /** Construct a line segment based on an existing line.
     * @param start abscissa of the line segment start point
     * @param end abscissa of the line segment end point
     * @param line the underyling line
     */
    public TestLineSegment(final double start, final double end, final TestLine line) {
        this.start = start;
        this.end = end;
        this.line = line;
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
    public boolean isEmpty() {
        return PartitionTestUtils.PRECISION.eqZero(size());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return Double.isInfinite(size());
    }

    /** {@inheritDoc} */
    @Override
    public double size() {
        return Math.abs(start - end);
    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexSubHyperplane<TestPoint2D>> toConvex() {
        return Arrays.asList(this);
    }

    /** {@inheritDoc} */
    @Override
    public SplitConvexSubHyperplane<TestPoint2D> split(Hyperplane<TestPoint2D> splitter) {
        final TestLine splitterLine = (TestLine) splitter;

        final double startOffset = splitterLine.offset(line.toSpace(start));
        final double endOffset = splitterLine.offset(line.toSpace(end));

        final int startCmp = PartitionTestUtils.PRECISION.compare(startOffset, 0);
        final int endCmp = PartitionTestUtils.PRECISION.compare(endOffset, 0);

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
            return new SplitConvexSubHyperplane<TestPoint2D>(null, null);
        }
        else if (startCmp < 1 && endCmp < 1) {
            // the entire line segment is on the minus side
            return new SplitConvexSubHyperplane<TestPoint2D>(null, this);
        }
        else if (startCmp > -1 && endCmp > -1) {
            // the entire line segment is on the plus side
            return new SplitConvexSubHyperplane<TestPoint2D>(this, null);
        }

        // we need to split the line
        final TestPoint2D intersection = splitterLine.intersection(line);
        final double intersectionAbscissa = line.toSubSpace(intersection);

        final TestLineSegment startSegment = new TestLineSegment(start, intersectionAbscissa, line);
        final TestLineSegment endSegment = new TestLineSegment(intersectionAbscissa, end, line);

        final TestLineSegment plus = (startCmp > 0) ? startSegment : endSegment;
        final TestLineSegment minus = (startCmp > 0) ? endSegment: startSegment;

        return new SplitConvexSubHyperplane<TestPoint2D>(plus, minus);
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
}
