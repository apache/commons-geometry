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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partition.Hyperplane;
import org.apache.commons.geometry.core.partition.SubHyperplane;

/** Class containing a collection line segments. This class should only be used for
 * testing purposes.
 */
public class TestLineSegmentCollection implements SubHyperplane<TestPoint2D>, Serializable {

    /** Serializable UID */
    private static final long serialVersionUID = 20190303L;

    /** The collection of line-segments making up the subhyperplane.
     */
    private final List<TestLineSegment> segments;

    /** Create a new instance with the given line segments. The segments
     * are all assumed to lie on the same hyperplane.
     * @param segments the segments to use in the collection
     * @throws IllegalArgumentException if the collection is null or empty
     */
    public TestLineSegmentCollection(List<TestLineSegment> segments) {
        this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
    }

    /** Get the list of line segments comprising the collection.
     * @return the list of line segments in the collection
     */
    public List<TestLineSegment> getLineSegments() {
        return segments;
    }

    /** {@inheritDoc} */
    @Override
    public Hyperplane<TestPoint2D> getHyperplane() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFull() {
        for (TestLineSegment seg : segments) {
            if (seg.isFull()) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        for (TestLineSegment seg : segments) {
            if (!seg.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        for (TestLineSegment seg : segments) {
            if (seg.isInfinite()) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        double size = 0.0;

        for (TestLineSegment seg : segments) {
            size += seg.getSize();
        }

        return size;
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(TestPoint2D point) {

        // simply return the first value that is not outside;
        // this is decidedly not robust but should work for testing purposes
        for (TestLineSegment seg : segments) {
            final RegionLocation loc = seg.classify(point);
            if (loc != RegionLocation.OUTSIDE) {
                return loc;
            }
        }

        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public TestPoint2D closest(TestPoint2D point) {
        TestPoint2D closest = null;
        double minDist = -1;

        for (TestLineSegment seg : segments) {
            TestPoint2D pt = seg.closest(point);
            double dist = pt.distance(point);
            if (minDist < 0 || dist < minDist) {
                minDist = dist;
                closest = pt;
            }
        }

        return closest;

    }

    /** {@inheritDoc} */
    @Override
    public List<ConvexSubHyperplane<TestPoint2D>> toConvex() {
        return new ArrayList<>(segments);
    }

    /** {@inheritDoc} */
    @Override
    public SubHyperplane.Builder<TestPoint2D> builder() {
        return new TestLineSegmentCollectionBuilder(segments.get(0).getHyperplane());
    }
}
