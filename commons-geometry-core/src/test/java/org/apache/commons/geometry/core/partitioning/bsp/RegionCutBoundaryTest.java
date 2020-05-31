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
package org.apache.commons.geometry.core.partitioning.bsp;


import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partitioning.test.TestLine;
import org.apache.commons.geometry.core.partitioning.test.TestLineSegment;
import org.apache.commons.geometry.core.partitioning.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Test;

public class RegionCutBoundaryTest {

    private static final double TEST_EPS = 1e-10;

    @Test
    public void testProperties() {
        // arrange
        List<HyperplaneConvexSubset<TestPoint2D>> insideFacing =
                Arrays.asList(new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0)));
        List<HyperplaneConvexSubset<TestPoint2D>> outsideFacing =
                Arrays.asList(new TestLineSegment(new TestPoint2D(-1, 0), TestPoint2D.ZERO));

        // act
        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(insideFacing, outsideFacing);

        // assert
        Assert.assertNotSame(insideFacing, boundary.getInsideFacing());
        Assert.assertEquals(insideFacing, boundary.getInsideFacing());

        Assert.assertNotSame(outsideFacing, boundary.getOutsideFacing());
        Assert.assertEquals(outsideFacing, boundary.getOutsideFacing());
    }

    @Test
    public void testProperties_nullLists() {
        // act
        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(null, null);

        // assert
        Assert.assertEquals(0, boundary.getInsideFacing().size());
        Assert.assertEquals(0, boundary.getOutsideFacing().size());
    }

    @Test
    public void testGetSize_noSize() {
        // act
        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(null, null);

        // assert
        Assert.assertEquals(0, boundary.getSize(), TEST_EPS);
    }

    @Test
    public void testGetSize_infinite() {
        // act
        TestLine line = new TestLine(new TestPoint2D(0, 0), new TestPoint2D(1, 0));
        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(
                Arrays.asList(new TestLineSegment(1, Double.POSITIVE_INFINITY, line)),
                Arrays.asList(new TestLineSegment(Double.NEGATIVE_INFINITY, -1, line)));

        // assert
        GeometryTestUtils.assertPositiveInfinity(boundary.getSize());
    }

    @Test
    public void testGetSize_finite() {
        // act
        TestLine line = new TestLine(new TestPoint2D(0, 0), new TestPoint2D(1, 0));
        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(
                Arrays.asList(new TestLineSegment(1, 2, line), new TestLineSegment(3, 4, line)),
                Arrays.asList(new TestLineSegment(-3, -1, line)));

        // assert
        Assert.assertEquals(4, boundary.getSize(), TEST_EPS);
    }

    @Test
    public void testClosest() {
        // arrange
        TestPoint2D a = new TestPoint2D(-1, 0);
        TestPoint2D b = TestPoint2D.ZERO;
        TestPoint2D c = new TestPoint2D(1, 0);

        TestLineSegment insideFacing = new TestLineSegment(a, b);
        TestLineSegment outsideFacing = new TestLineSegment(b, c);

        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(Arrays.asList(insideFacing),
                Arrays.asList(outsideFacing));

        // act/assert
        PartitionTestUtils.assertPointsEqual(a, boundary.closest(new TestPoint2D(-2, 1)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-0.5, 0), boundary.closest(new TestPoint2D(-0.5, -1)));
        PartitionTestUtils.assertPointsEqual(b, boundary.closest(TestPoint2D.ZERO));
        PartitionTestUtils.assertPointsEqual(b, boundary.closest(new TestPoint2D(0, 2)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0.5, 0), boundary.closest(new TestPoint2D(0.5, 3)));
        PartitionTestUtils.assertPointsEqual(c, boundary.closest(new TestPoint2D(1, -4)));
        PartitionTestUtils.assertPointsEqual(c, boundary.closest(new TestPoint2D(3, -5)));
    }

    @Test
    public void testClosest_nullInsideFacing() {
        // arrange
        TestPoint2D a = new TestPoint2D(-1, 0);
        TestPoint2D b = TestPoint2D.ZERO;

        TestLineSegment outsideFacing = new TestLineSegment(a, b);

        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(null, Arrays.asList(outsideFacing));

        // act/assert
        PartitionTestUtils.assertPointsEqual(a, boundary.closest(new TestPoint2D(-2, 1)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-0.5, 0), boundary.closest(new TestPoint2D(-0.5, -1)));
        PartitionTestUtils.assertPointsEqual(b, boundary.closest(TestPoint2D.ZERO));
        PartitionTestUtils.assertPointsEqual(b, boundary.closest(new TestPoint2D(1, 2)));
    }

    @Test
    public void testClosest_nullOutsideFacing() {
        // arrange
        TestPoint2D a = new TestPoint2D(-1, 0);
        TestPoint2D b = TestPoint2D.ZERO;

        TestLineSegment insideFacing = new TestLineSegment(a, b);

        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(Arrays.asList(insideFacing), null);

        // act/assert
        PartitionTestUtils.assertPointsEqual(a, boundary.closest(new TestPoint2D(-2, 1)));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(-0.5, 0), boundary.closest(new TestPoint2D(-0.5, -1)));
        PartitionTestUtils.assertPointsEqual(b, boundary.closest(TestPoint2D.ZERO));
        PartitionTestUtils.assertPointsEqual(b, boundary.closest(new TestPoint2D(1, 2)));
    }

    @Test
    public void testClosest_nullInsideAndOutsideFacing() {
        // arrange
        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(null, null);

        // act/assert
        Assert.assertNull(boundary.closest(TestPoint2D.ZERO));
        Assert.assertNull(boundary.closest(new TestPoint2D(1, 1)));
    }

    @Test
    public void testContains() {
        // arrange
        TestPoint2D a = new TestPoint2D(-1, 0);
        TestPoint2D b = TestPoint2D.ZERO;
        TestPoint2D c = new TestPoint2D(1, 0);

        TestLineSegment insideFacing = new TestLineSegment(a, b);
        TestLineSegment outsideFacing = new TestLineSegment(b, c);

        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(Arrays.asList(insideFacing),
                Arrays.asList(outsideFacing));

        // act/assert
        Assert.assertFalse(boundary.contains(new TestPoint2D(-2, 0)));

        Assert.assertTrue(boundary.contains(new TestPoint2D(-1, 0)));
        Assert.assertTrue(boundary.contains(new TestPoint2D(-0.5, 0)));
        Assert.assertTrue(boundary.contains(new TestPoint2D(0, 0)));
        Assert.assertTrue(boundary.contains(new TestPoint2D(0.5, 0)));
        Assert.assertTrue(boundary.contains(new TestPoint2D(1, 0)));

        Assert.assertFalse(boundary.contains(new TestPoint2D(2, 0)));

        Assert.assertFalse(boundary.contains(new TestPoint2D(-1, 1)));
        Assert.assertFalse(boundary.contains(new TestPoint2D(0, -1)));
        Assert.assertFalse(boundary.contains(new TestPoint2D(1, 1)));
    }

    @Test
    public void testContains_nullHyperplaneSubsets() {
        // arrange
        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(null, null);

        // act/assert
        Assert.assertFalse(boundary.contains(new TestPoint2D(-1, 0)));
        Assert.assertFalse(boundary.contains(new TestPoint2D(0, 0)));
        Assert.assertFalse(boundary.contains(new TestPoint2D(1, 0)));
    }
}
