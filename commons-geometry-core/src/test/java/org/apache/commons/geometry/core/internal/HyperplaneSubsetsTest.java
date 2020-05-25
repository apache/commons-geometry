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
package org.apache.commons.geometry.core.internal;

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partitioning.test.TestLine;
import org.apache.commons.geometry.core.partitioning.test.TestPoint1D;
import org.apache.commons.geometry.core.partitioning.test.TestPoint2D;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Split;
import org.junit.Assert;
import org.junit.Test;

public class HyperplaneSubsetsTest {

    @Test
    public void testClassify() {
        // arrange
        TestLine line = TestLine.X_AXIS;
        StubRegion1D region = new StubRegion1D();

        // act/assert
        Assert.assertEquals(RegionLocation.INSIDE,
                HyperplaneSubsets.classifyAgainstEmbeddedRegion(new TestPoint2D(-1, 0), line, region));
        Assert.assertEquals(RegionLocation.BOUNDARY,
                HyperplaneSubsets.classifyAgainstEmbeddedRegion(new TestPoint2D(0, 0), line, region));

        Assert.assertEquals(RegionLocation.OUTSIDE,
                HyperplaneSubsets.classifyAgainstEmbeddedRegion(new TestPoint2D(0, 1), line, region));
        Assert.assertEquals(RegionLocation.OUTSIDE,
                HyperplaneSubsets.classifyAgainstEmbeddedRegion(new TestPoint2D(-1, 1), line, region));
        Assert.assertEquals(RegionLocation.OUTSIDE,
                HyperplaneSubsets.classifyAgainstEmbeddedRegion(new TestPoint2D(-1, -1), line, region));
    }

    @Test
    public void testClosest() {
        // arrange
        TestLine line = TestLine.X_AXIS;
        StubRegion1D region = new StubRegion1D();
        StubRegion1D emptyRegion = new StubRegion1D(true);

        // act/assert
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0),
                HyperplaneSubsets.closestToEmbeddedRegion(new TestPoint2D(-1, 0), line, region));

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0),
                HyperplaneSubsets.closestToEmbeddedRegion(new TestPoint2D(0, 0), line, region));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0),
                HyperplaneSubsets.closestToEmbeddedRegion(new TestPoint2D(1, 0), line, region));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0),
                HyperplaneSubsets.closestToEmbeddedRegion(new TestPoint2D(1, 1), line, region));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0),
                HyperplaneSubsets.closestToEmbeddedRegion(new TestPoint2D(1, -1), line, region));

        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0),
                HyperplaneSubsets.closestToEmbeddedRegion(new TestPoint2D(-1, 1), line, region));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-1, 0),
                HyperplaneSubsets.closestToEmbeddedRegion(new TestPoint2D(-1, -1), line, region));

        Assert.assertNull(HyperplaneSubsets.closestToEmbeddedRegion(TestPoint2D.ZERO, line, emptyRegion));
    }

    /** Stub region implementation. Negative numbers are on the inside of the region.
     */
    private static class StubRegion1D implements HyperplaneBoundedRegion<TestPoint1D> {

        private final boolean empty;

        StubRegion1D() {
            this(false);
        }

        StubRegion1D(boolean empty) {
            this.empty = empty;
        }

        @Override
        public boolean isFull() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEmpty() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getBoundarySize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestPoint1D getCentroid() {
            throw new UnsupportedOperationException();
        }

        @Override
        public RegionLocation classify(TestPoint1D pt) {
            if (!empty) {
                int sign = PartitionTestUtils.PRECISION.sign(pt.getX());

                if (sign < 0) {
                    return RegionLocation.INSIDE;
                } else if (sign == 0) {
                    return RegionLocation.BOUNDARY;
                }
            }
            return RegionLocation.OUTSIDE;
        }

        @Override
        public TestPoint1D project(TestPoint1D pt) {
            return empty ? null : new TestPoint1D(0);
        }

        @Override
        public Split<? extends HyperplaneBoundedRegion<TestPoint1D>> split(Hyperplane<TestPoint1D> splitter) {
            throw new UnsupportedOperationException();
        }
    }
}
