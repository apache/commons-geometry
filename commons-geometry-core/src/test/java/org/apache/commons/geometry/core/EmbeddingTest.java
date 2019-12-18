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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.partition.test.PartitionTestUtils;
import org.apache.commons.geometry.core.partition.test.TestLine;
import org.apache.commons.geometry.core.partition.test.TestPoint1D;
import org.apache.commons.geometry.core.partition.test.TestPoint2D;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddingTest {

    @Test
    public void testToSubspace_collection_emptyInput() {
        // arrange
        TestLine line = TestLine.Y_AXIS;

        // act
        List<TestPoint1D> result = line.toSubspace(new ArrayList<>());

        // assert
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testToSubspace_collection() {
        // arrange
        List<TestPoint2D> pts = Arrays.asList(
                    new TestPoint2D(0, 0),
                    new TestPoint2D(1, 0.25),
                    new TestPoint2D(0.5, 1)
                );

        TestLine line = TestLine.Y_AXIS;

        // act
        List<TestPoint1D> result = line.toSubspace(pts);

        // assert
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(0, result.get(0).getX(), PartitionTestUtils.EPS);
        Assert.assertEquals(0.25, result.get(1).getX(), PartitionTestUtils.EPS);
        Assert.assertEquals(1, result.get(2).getX(), PartitionTestUtils.EPS);
    }

    @Test
    public void testToSpace_collection_emptyInput() {
        // arrange
        TestLine line = TestLine.Y_AXIS;

        // act
        List<TestPoint2D> result = line.toSpace(new ArrayList<>());

        // assert
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testToSpace_collection() {
        // arrange
        List<TestPoint1D> pts = Arrays.asList(
                    new TestPoint1D(0),
                    new TestPoint1D(1),
                    new TestPoint1D(0.5)
                );

        TestLine line = TestLine.Y_AXIS;

        // act
        List<TestPoint2D> result = line.toSpace(pts);

        // assert
        Assert.assertEquals(3, result.size());
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0), result.get(0));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 1), result.get(1));
        PartitionTestUtils.assertPointsEqual(new TestPoint2D(0, 0.5), result.get(2));
    }
}
