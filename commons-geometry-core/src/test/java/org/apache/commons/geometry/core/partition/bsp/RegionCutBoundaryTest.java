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
package org.apache.commons.geometry.core.partition.bsp;

import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestLineSegment;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Test;

public class RegionCutBoundaryTest {

    @Test
    public void testProperties() {
        // arrange
        TestLineSegment insideFacing = new TestLineSegment(TestPoint2D.ZERO, new TestPoint2D(1, 0));
        TestLineSegment outsideFacing = new TestLineSegment(new TestPoint2D(-1, 0), TestPoint2D.ZERO);

        // act
        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(insideFacing, outsideFacing);

        // assert
        Assert.assertSame(insideFacing, boundary.getInsideFacing());
        Assert.assertSame(outsideFacing, boundary.getOutsideFacing());
    }

    @Test
    public void testClosest() {
        // arrange
        TestPoint2D a = new TestPoint2D(-1, 0);
        TestPoint2D b = TestPoint2D.ZERO;
        TestPoint2D c = new TestPoint2D(1, 0);

        TestLineSegment insideFacing = new TestLineSegment(a, b);
        TestLineSegment outsideFacing = new TestLineSegment(b, c);

        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(insideFacing, outsideFacing);

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

        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(null, outsideFacing);

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

        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(insideFacing, null);

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

        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(insideFacing, outsideFacing);

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
    public void testContains_nullSubHyperplanes() {
        // arrange
        RegionCutBoundary<TestPoint2D> boundary = new RegionCutBoundary<>(null, null);

        // act/assert
        Assert.assertFalse(boundary.contains(new TestPoint2D(-1, 0)));
        Assert.assertFalse(boundary.contains(new TestPoint2D(0, 0)));
        Assert.assertFalse(boundary.contains(new TestPoint2D(1, 0)));
    }
}
