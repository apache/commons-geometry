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
package org.apache.commons.geometry.core.partitioning;

import java.util.List;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestPoint1D;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Test;

public class AbstractEmbeddingHyperplaneSubsetTest {

    @Test
    public void testSimpleProperties() {
        // arrange
        StubHyperplaneSubset sub = new StubHyperplaneSubset(1.0);

        // act/assert
        Assert.assertTrue(sub.isFull());
        Assert.assertTrue(sub.isEmpty());
        Assert.assertEquals(1.0, sub.getSize(), PartitionTestUtils.EPS);

        Assert.assertTrue(sub.isFinite());
        Assert.assertFalse(sub.isInfinite());
    }

    @Test
    public void testFiniteAndInfinite() {
        // arrange
        StubHyperplaneSubset finite = new StubHyperplaneSubset(1.0);
        StubHyperplaneSubset inf = new StubHyperplaneSubset(Double.POSITIVE_INFINITY);
        StubHyperplaneSubset nan = new StubHyperplaneSubset(Double.NaN);

        // act/assert
        Assert.assertTrue(finite.isFinite());
        Assert.assertFalse(finite.isInfinite());

        Assert.assertFalse(inf.isFinite());
        Assert.assertTrue(inf.isInfinite());

        Assert.assertFalse(nan.isFinite());
        Assert.assertFalse(nan.isInfinite());
    }

    @Test
    public void testClassify() {
        // arrange
        StubHyperplaneSubset sub = new StubHyperplaneSubset();

        // act/assert
        Assert.assertEquals(RegionLocation.INSIDE, sub.classify(new TestPoint2D(-1, 0)));
        Assert.assertEquals(RegionLocation.BOUNDARY, sub.classify(new TestPoint2D(0, 0)));

        Assert.assertEquals(RegionLocation.OUTSIDE, sub.classify(new TestPoint2D(0, 1)));
        Assert.assertEquals(RegionLocation.OUTSIDE, sub.classify(new TestPoint2D(-1, 1)));
        Assert.assertEquals(RegionLocation.OUTSIDE, sub.classify(new TestPoint2D(-1, -1)));
    }

    @Test
    public void testClosest() {
        // arrange
        StubHyperplaneSubset sub = new StubHyperplaneSubset();

        // act/assert
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0), sub.closest(new TestPoint2D(-1, 0)));

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0), sub.closest(new TestPoint2D(0, 0)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0), sub.closest(new TestPoint2D(1, 0)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0), sub.closest(new TestPoint2D(1, 1)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0), sub.closest(new TestPoint2D(1, -1)));

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0), sub.closest(new TestPoint2D(-1, 1)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0), sub.closest(new TestPoint2D(-1, -1)));
    }

    @Test
    public void testClosest_nullSubspaceRegionProjection() {
        // arrange
        StubHyperplaneSubset sub = new StubHyperplaneSubset();
        sub.region.projected = null;

        // act/assert
        Assert.assertNull(sub.closest(new TestPoint2D(1, 1)));
    }

    private static class StubHyperplaneSubset extends AbstractEmbeddingHyperplaneSubset<TestPoint2D, TestPoint1D, TestLine> {

        private final StubRegion1D region;

        StubHyperplaneSubset() {
            this(0);
        }

        StubHyperplaneSubset(final double size) {
            this.region = new StubRegion1D(size);
        }

        @Override
        public Builder<TestPoint2D> builder() {
            return null;
        }

        @Override
        public List<? extends HyperplaneConvexSubset<TestPoint2D>> toConvex() {
            return null;
        }

        @Override
        public TestLine getHyperplane() {
            return TestLine.X_AXIS;
        }

        @Override
        public HyperplaneBoundedRegion<TestPoint1D> getSubspaceRegion() {
            return region;
        }

        @Override
        public Split<StubHyperplaneSubset> split(Hyperplane<TestPoint2D> splitter) {
            return null;
        }

        @Override
        public HyperplaneSubset<TestPoint2D> transform(Transform<TestPoint2D> transform) {
            return null;
        }
    }

    /** Stub region implementation with some hard-coded values. Negative numbers are
     * on the inside of the region.
     */
    private static class StubRegion1D implements HyperplaneBoundedRegion<TestPoint1D> {

        private TestPoint1D projected = new TestPoint1D(0);

        private final double size;

        StubRegion1D(final double size) {
            this.size = size;
        }

        @Override
        public boolean isFull() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public double getSize() {
            return size;
        }

        @Override
        public double getBoundarySize() {
            return 0;
        }

        @Override
        public TestPoint1D getBarycenter() {
            return null;
        }

        @Override
        public RegionLocation classify(TestPoint1D pt) {
            int sign = PartitionTestUtils.PRECISION.sign(pt.getX());

            if (sign < 0) {
                return RegionLocation.INSIDE;
            } else if (sign == 0) {
                return RegionLocation.BOUNDARY;
            }
            return RegionLocation.OUTSIDE;
        }

        @Override
        public TestPoint1D project(TestPoint1D pt) {
            return projected;
        }

        @Override
        public Split<? extends HyperplaneBoundedRegion<TestPoint1D>> split(Hyperplane<TestPoint1D> splitter) {
            throw new UnsupportedOperationException();
        }
    }
}
