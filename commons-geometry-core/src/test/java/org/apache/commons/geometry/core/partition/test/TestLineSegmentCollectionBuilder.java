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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;

public class TestLineSegmentCollectionBuilder implements SubHyperplane.Builder<TestPoint2D> {

    private final TestLine line;

    private final List<SegmentInterval> intervals = new LinkedList<>();

    public TestLineSegmentCollectionBuilder(final TestLine line) {
        this.line = line;
    }

    /** {@inheritDoc} */
    @Override
    public void add(SubHyperplane<TestPoint2D> sub) {
        for (ConvexSubHyperplane<TestPoint2D> convex : sub.toConvex()) {
            add(convex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void add(ConvexSubHyperplane<TestPoint2D> convex) {
        TestLineSegment seg = (TestLineSegment) convex;
        addSegment(seg.getStart(), seg.getEnd());
    }

    /** {@inheritDoc} */
    @Override
    public SubHyperplane<TestPoint2D> build() {
        List<TestLineSegment> segments = new ArrayList<>();

        for (SegmentInterval interval : intervals) {
            segments.add(new TestLineSegment(interval.start, interval.end, line));
        }

        return new TestLineSegmentCollection(segments);
    }

    private void addSegment(final double start, final double end) {
        if (intervals.isEmpty()) {
            intervals.add(new SegmentInterval(start, end));
        } else {
            boolean added = false;
            SegmentInterval current;
            for (int i = 0; i < intervals.size() && !added; ++i) {
                current = intervals.get(i);

                if (end < current.start) {
                    intervals.add(i, new SegmentInterval(start, end));

                    added = true;
                } else if (start <= current.end) {
                    current.start = Math.min(current.start, start);
                    current.end = Math.max(current.end, end);

                    added = true;
                }
            }

            if (!added) {
                intervals.add(new SegmentInterval(start, end));
            }
        }
    }

    private static class SegmentInterval {
        private double start;
        private double end;

        SegmentInterval(final double start, final double end) {
            this.start = start;
            this.end = end;
        }
    }
}
