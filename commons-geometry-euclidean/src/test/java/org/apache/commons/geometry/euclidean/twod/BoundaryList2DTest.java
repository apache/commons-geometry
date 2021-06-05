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
package org.apache.commons.geometry.euclidean.twod;

import java.util.Collections;
import java.util.List;

import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BoundaryList2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testCtor() {
        // arrange
        final List<LineConvexSubset> boundaries = Collections.singletonList(
                Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION)
        );

        // act
        final BoundaryList2D list = new BoundaryList2D(boundaries);

        // assert
        Assertions.assertNotSame(boundaries, list.getBoundaries());
        Assertions.assertEquals(boundaries, list.getBoundaries());
        Assertions.assertEquals(1, list.count());
    }

    @Test
    void testToList() {
        // arrange
        final BoundaryList2D list = new BoundaryList2D(Collections.emptyList());

        // act/assert
        Assertions.assertSame(list, list.toList());
    }

    @Test
    void testToString() {
        // arrange
        final BoundaryList2D list = new BoundaryList2D(Collections.emptyList());

        // act
        Assertions.assertEquals("BoundaryList2D[count= 0]", list.toString());
    }
}
