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
package org.apache.commons.geometry.core.partition;

import java.util.List;

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestPoint1D;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Test;

public class AbstractEmbeddingSubHyperplaneTest {

    @Test
    public void testSimpleProperties() {
        // arrange
        StubSubHyperplane sub = new StubSubHyperplane();

        // act/assert
        Assert.assertTrue(sub.isFull());
        Assert.assertTrue(sub.isEmpty());
        Assert.assertEquals(1.0, sub.getSize(), PartitionTestUtils.EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        StubSubHyperplane sub = new StubSubHyperplane();

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
        StubSubHyperplane sub = new StubSubHyperplane();

        // act/assert
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0), sub.closest(new TestPoint2D(-1, 0)));

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0), sub.closest(new TestPoint2D(0, 0)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0), sub.closest(new TestPoint2D(1, 0)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0), sub.closest(new TestPoint2D(1, 1)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0), sub.closest(new TestPoint2D(1, -1)));

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0), sub.closest(new TestPoint2D(-1, 1)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0), sub.closest(new TestPoint2D(-1, -1)));
    }

    private static class StubSubHyperplane extends AbstractEmbeddingSubHyperplane<TestPoint2D, TestPoint1D, TestLine> {

        @Override
        public boolean isInfinite() {
            return false;
        }

        @Override
        public Builder<TestPoint2D> builder() {
            return null;
        }

        @Override
        public List<? extends ConvexSubHyperplane<TestPoint2D>> toConvex() {
            return null;
        }

        @Override
        public TestLine getHyperplane() {
            return TestLine.X_AXIS;
        }

        @Override
        public Region<TestPoint1D> getSubspaceRegion() {
            return new StubRegion1D();
        }
    }

    /** Stub region implementation with some hard-coded values. Negative numbers are
     * on the inside of the region.
     */
    private static class StubRegion1D implements Region<TestPoint1D> {

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
            return 1;
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
            }
            else if (sign == 0) {
                return RegionLocation.BOUNDARY;
            }
            return RegionLocation.OUTSIDE;
        }

        @Override
        public TestPoint1D project(TestPoint1D pt) {
            return new TestPoint1D(0);
        }
    }
}
