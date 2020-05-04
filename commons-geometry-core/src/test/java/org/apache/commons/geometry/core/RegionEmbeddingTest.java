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
package org.apache.commons.geometry.core;

import org.apache.commons.geometry.core.partition.test.TestPoint1D;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Test;

public class RegionEmbeddingTest {

    private static final double TEST_EPS = 1e-10;

    @Test
    public void testGetSize() {
        // arrange
        StubRegionEmbedding finite = new StubRegionEmbedding(2.0);
        StubRegionEmbedding infinite = new StubRegionEmbedding(Double.POSITIVE_INFINITY);
        StubRegionEmbedding nan = new StubRegionEmbedding(Double.NaN);

        // act/assert
        Assert.assertEquals(2.0, finite.getSize(), TEST_EPS);
        Assert.assertTrue(finite.isFinite());
        Assert.assertFalse(finite.isInfinite());

        GeometryTestUtils.assertPositiveInfinity(infinite.getSize());
        Assert.assertFalse(infinite.isFinite());
        Assert.assertTrue(infinite.isInfinite());

        Assert.assertTrue(Double.isNaN(nan.getSize()));
        Assert.assertFalse(nan.isFinite());
        Assert.assertFalse(nan.isInfinite());
    }

    private static class StubRegionEmbedding implements RegionEmbedding<TestPoint2D, TestPoint1D> {

        private final StubRegion1D subspaceRegion;

        StubRegionEmbedding(final double size) {
            subspaceRegion = new StubRegion1D(size);
        }

        @Override
        public TestPoint1D toSubspace(TestPoint2D pt) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestPoint2D toSpace(TestPoint1D pt) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Region<TestPoint1D> getSubspaceRegion() {
            return subspaceRegion;
        }
    }

    private static class StubRegion1D implements Region<TestPoint1D> {

        private final double size;

        StubRegion1D(final double size) {
            this.size = size;
        }

        @Override
        public double getSize() {
            return size;
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
        public double getBoundarySize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestPoint1D getBarycenter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public RegionLocation classify(TestPoint1D pt) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TestPoint1D project(TestPoint1D pt) {
            throw new UnsupportedOperationException();
        }
    }
}
