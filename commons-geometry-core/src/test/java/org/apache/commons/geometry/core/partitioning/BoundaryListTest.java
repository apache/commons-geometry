/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.core.partitioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.partitioning.test.TestLineSegment;
import org.apache.commons.geometry.core.partitioning.test.TestPoint2D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BoundaryListTest {

    @Test
    void testBoundaries() {
        // arrange
        final List<TestLineSegment> boundaries = new ArrayList<>();
        boundaries.add(new TestLineSegment(0, 0, 1, 1));
        boundaries.add(new TestLineSegment(1, 1, 0, 2));

        // act
        final BoundaryList<TestPoint2D, TestLineSegment> list = new BoundaryList<>(boundaries);

        // assert
        Assertions.assertNotSame(boundaries, list.getBoundaries());
        Assertions.assertEquals(boundaries, list.getBoundaries());
        Assertions.assertEquals(boundaries, list.boundaryStream().collect(Collectors.toList()));
    }

    @Test
    void testGetBoundaries_listCannotBeModified() {
        // arrange
        final List<TestLineSegment> boundaries = new ArrayList<>();
        boundaries.add(new TestLineSegment(0, 0, 1, 1));

        final BoundaryList<TestPoint2D, TestLineSegment> list = new BoundaryList<>(boundaries);
        final List<TestLineSegment> items = list.getBoundaries();
        final TestLineSegment segment = new TestLineSegment(1, 1, 0, 2);

        // act/assert
        Assertions.assertThrows(UnsupportedOperationException.class, () -> items.add(segment));
    }

    @Test
    void testCount() {
        // act/assert
        Assertions.assertEquals(0, new BoundaryList<>(Collections.emptyList()).count());
        Assertions.assertEquals(1, new BoundaryList<>(Collections.singletonList(
                new TestLineSegment(0, 0, 1, 1)
        )).count());
        Assertions.assertEquals(2, new BoundaryList<>(Arrays.asList(
                new TestLineSegment(0, 0, 1, 1),
                new TestLineSegment(1, 1, 0, 2)
            )).count());
    }

    @Test
    void testToString() {
        // arrange
        final BoundaryList<TestPoint2D, TestLineSegment> empty = new BoundaryList<>(Collections.emptyList());
        final BoundaryList<TestPoint2D, TestLineSegment> single = new BoundaryList<>(Collections.singletonList(
                new TestLineSegment(0, 0, 1, 1)
        ));

        // act
        Assertions.assertEquals("BoundaryList[count= 0]", empty.toString());
        Assertions.assertEquals("BoundaryList[count= 1]", single.toString());
    }
}
