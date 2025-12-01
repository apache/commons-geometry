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
package org.apache.commons.geometry.spherical.twod;

import java.util.Collections;

import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BoundarySource2STest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testToList() {
        // act
        final BoundarySource2S src = BoundarySource2S.of(
            GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION),
            GreatCircles.arcFromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION)
        );

        // act
        final BoundaryList2S list = src.toList();

        // assert
        Assertions.assertEquals(2, list.count());
    }

    @Test
    void testToList_noBoundaries() {
        // act
        final BoundarySource2S src = BoundarySource2S.of();

        // act
        final BoundaryList2S list = src.toList();

        // assert
        Assertions.assertEquals(0, list.count());
    }

    @Test
    void testToTree() {
        // act
        final BoundarySource2S src = BoundarySource2S.of(
                GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION));

        // act
        final RegionBSPTree2S tree = src.toTree();

        // assert
        Assertions.assertEquals(3, tree.count());
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
    }

    @Test
    void testToTree_noBoundaries() {
        // act
        final BoundarySource2S src = BoundarySource2S.of(Collections.emptyList());

        // act
        final RegionBSPTree2S tree = src.toTree();

        // assert
        Assertions.assertEquals(1, tree.count());
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
    }
}
