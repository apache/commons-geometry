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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.partitioning.Split;

/** Class containing a collection line segments. This class should only be used for
 * testing purposes.
 */
public final class TestLineSegmentCollection implements HyperplaneSubset<TestPoint2D> {
    /** The collection of line-segments making up the subset.
     */
    private final List<TestLineSegment> segments;

    /** Create a new instance with the given line segments. The segments
     * are all assumed to lie on the same hyperplane.
     * @param segments the segments to use in the collection
     * @throws IllegalArgumentException if the collection is null or empty
     */
    public TestLineSegmentCollection(final List<TestLineSegment> segments) {
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
        for (final TestLineSegment seg : segments) {
            if (seg.isFull()) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        for (final TestLineSegment seg : segments) {
            if (!seg.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        for (final TestLineSegment seg : segments) {
            if (seg.isInfinite()) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFinite() {
        return !isInfinite();
    }

    /** {@inheritDoc} */
    @Override
    public double getSize() {
        double size = 0.0;

        for (final TestLineSegment seg : segments) {
            size += seg.getSize();
        }

        return size;
    }

    /** {@inheritDoc} */
    @Override
    public TestPoint2D getCentroid() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Split<TestLineSegmentCollection> split(final Hyperplane<TestPoint2D> splitter) {
        final List<TestLineSegment> minusList = new ArrayList<>();
        final List<TestLineSegment> plusList = new ArrayList<>();

        for (final TestLineSegment segment : segments) {
            final Split<TestLineSegment> split = segment.split(splitter);

            if (split.getMinus() != null) {
                minusList.add(split.getMinus());
            }

            if (split.getPlus() != null) {
                plusList.add(split.getPlus());
            }
        }

        final TestLineSegmentCollection minus = minusList.isEmpty() ?
                null :
                new TestLineSegmentCollection(minusList);

        final TestLineSegmentCollection plus = plusList.isEmpty() ?
                null :
                new TestLineSegmentCollection(plusList);

        return new Split<>(minus, plus);
    }

    /** {@inheritDoc} */
    @Override
    public RegionLocation classify(final TestPoint2D point) {

        // simply return the first value that is not outside.
        // this is decidedly not robust but should work for testing purposes
        for (final TestLineSegment seg : segments) {
            final RegionLocation loc = seg.classify(point);
            if (loc != RegionLocation.OUTSIDE) {
                return loc;
            }
        }

        return RegionLocation.OUTSIDE;
    }

    /** {@inheritDoc} */
    @Override
    public TestPoint2D closest(final TestPoint2D point) {
        TestPoint2D closest = null;
        double minDist = -1;

        for (final TestLineSegment seg : segments) {
            final TestPoint2D pt = seg.closest(point);
            final double dist = pt.distance(point);
            if (minDist < 0 || dist < minDist) {
                minDist = dist;
                closest = pt;
            }
        }

        return closest;

    }

    /** {@inheritDoc} */
    @Override
    public List<HyperplaneConvexSubset<TestPoint2D>> toConvex() {
        return new ArrayList<>(segments);
    }

    /** {@inheritDoc} */
    @Override
    public HyperplaneSubset<TestPoint2D> transform(final Transform<TestPoint2D> transform) {
        throw new UnsupportedOperationException();
    }
}
