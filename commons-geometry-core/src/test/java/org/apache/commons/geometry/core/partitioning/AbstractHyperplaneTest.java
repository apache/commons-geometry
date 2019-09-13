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

import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.apache.commons.geometry.core.partitioning.AbstractHyperplane;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.Assert;
import org.junit.Test;

public class AbstractHyperplaneTest {

    @Test
    public void testGetPrecision() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        StubHyperplane hyper = new StubHyperplane(precision);

        // act/assert
        Assert.assertSame(precision, hyper.getPrecision());
    }

    @Test
    public void testClassify() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        StubHyperplane hyper = new StubHyperplane(precision);

        // act/assert
        Assert.assertEquals(HyperplaneLocation.MINUS, hyper.classify(new TestPoint2D(1, 1)));

        Assert.assertEquals(HyperplaneLocation.ON, hyper.classify(new TestPoint2D(1, 0.09)));
        Assert.assertEquals(HyperplaneLocation.ON, hyper.classify(new TestPoint2D(1, 0)));
        Assert.assertEquals(HyperplaneLocation.ON, hyper.classify(new TestPoint2D(1, -0.09)));

        Assert.assertEquals(HyperplaneLocation.PLUS, hyper.classify(new TestPoint2D(1, -1)));
    }

    @Test
    public void testContains() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);
        StubHyperplane hyper = new StubHyperplane(precision);

        // act/assert
        Assert.assertFalse(hyper.contains(new TestPoint2D(1, 1)));

        Assert.assertTrue(hyper.contains(new TestPoint2D(1, 0.09)));
        Assert.assertTrue(hyper.contains(new TestPoint2D(1, 0)));
        Assert.assertTrue(hyper.contains(new TestPoint2D(1, -0.09)));

        Assert.assertFalse(hyper.contains(new TestPoint2D(1, -1)));
    }

    public static class StubHyperplane extends AbstractHyperplane<TestPoint2D> {

        private static final long serialVersionUID = 1L;

        public StubHyperplane(DoublePrecisionContext precision) {
            super(precision);
        }

        @Override
        public double offset(TestPoint2D point) {
            return TestLine.X_AXIS.offset(point);
        }

        @Override
        public TestPoint2D project(TestPoint2D point) {
            return null;
        }

        @Override
        public Hyperplane<TestPoint2D> reverse() {
            return null;
        }

        @Override
        public Hyperplane<TestPoint2D> transform(Transform<TestPoint2D> transform) {
            return null;
        }

        @Override
        public boolean similarOrientation(Hyperplane<TestPoint2D> other) {
            return false;
        }

        @Override
        public ConvexSubHyperplane<TestPoint2D> span() {
            return null;
        }
    }
}
