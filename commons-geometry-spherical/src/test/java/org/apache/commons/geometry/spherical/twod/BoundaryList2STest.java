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
package org.apache.commons.geometry.spherical.twod;

import java.util.Collections;
import java.util.List;

import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BoundaryList2STest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testCtor() {
        // arrange
        final List<GreatArc> boundaries = Collections.singletonList(
                GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION)
        );

        // act
        final BoundaryList2S list = new BoundaryList2S(boundaries);

        // assert
        Assertions.assertNotSame(boundaries, list.getBoundaries());
        Assertions.assertEquals(boundaries, list.getBoundaries());
        Assertions.assertEquals(1, list.count());
    }

    @Test
    void testToList() {
        // arrange
        final BoundaryList2S list = new BoundaryList2S(Collections.emptyList());

        // act/assert
        Assertions.assertSame(list, list.toList());
    }

    @Test
    void testToString() {
        // arrange
        final BoundaryList2S list = new BoundaryList2S(Collections.emptyList());

        // act
        Assertions.assertEquals("BoundaryList2S[count= 0]", list.toString());
    }
}
