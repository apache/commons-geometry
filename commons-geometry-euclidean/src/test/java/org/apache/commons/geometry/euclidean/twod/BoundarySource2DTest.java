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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BoundarySource2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testToTree() {
        // act
        final BoundarySource2D src = BoundarySource2D.from(
            Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION),
            Lines.segmentFromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION)
        );

        // act
        final RegionBSPTree2D tree = src.toTree();

        // assert
        Assertions.assertEquals(5, tree.count());
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
    }

    @Test
    public void testToTree_noBoundaries() {
        // act
        final BoundarySource2D src = BoundarySource2D.from();

        // act
        final RegionBSPTree2D tree = src.toTree();

        // assert
        Assertions.assertEquals(1, tree.count());
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
    }

    @Test
    public void testFrom_varargs_empty() {
        // act
        final BoundarySource2D src = BoundarySource2D.from();

        // assert
        final List<LineConvexSubset> segments = src.boundaryStream().collect(Collectors.toList());
        Assertions.assertEquals(0, segments.size());
    }

    @Test
    public void testFrom_varargs() {
        // act
        final Segment a = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Segment b = Lines.segmentFromPoints(Vector2D.Unit.PLUS_X, Vector2D.of(1, 1), TEST_PRECISION);

        final BoundarySource2D src = BoundarySource2D.from(a, b);

        // assert
        final List<LineConvexSubset> segments = src.boundaryStream().collect(Collectors.toList());
        Assertions.assertEquals(2, segments.size());

        Assertions.assertSame(a, segments.get(0));
        Assertions.assertSame(b, segments.get(1));
    }

    @Test
    public void testFrom_list_empty() {
        // arrange
        final List<LineConvexSubset> input = new ArrayList<>();

        // act
        final BoundarySource2D src = BoundarySource2D.from(input);

        // assert
        final List<LineConvexSubset> segments = src.boundaryStream().collect(Collectors.toList());
        Assertions.assertEquals(0, segments.size());
    }

    @Test
    public void testFrom_list() {
        // act
        final Segment a = Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Segment b = Lines.segmentFromPoints(Vector2D.Unit.PLUS_X, Vector2D.of(1, 1), TEST_PRECISION);

        final List<LineConvexSubset> input = new ArrayList<>();
        input.add(a);
        input.add(b);

        final BoundarySource2D src = BoundarySource2D.from(input);

        // assert
        final List<LineConvexSubset> segments = src.boundaryStream().collect(Collectors.toList());
        Assertions.assertEquals(2, segments.size());

        Assertions.assertSame(a, segments.get(0));
        Assertions.assertSame(b, segments.get(1));
    }
}
