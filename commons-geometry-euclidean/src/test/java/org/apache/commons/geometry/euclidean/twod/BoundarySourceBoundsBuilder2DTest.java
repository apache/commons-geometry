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

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class BoundarySourceBoundsBuilder2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testGetBounds_noBoundaries() {
        // arrange
        BoundarySource2D src = BoundarySource2D.from(new ArrayList<>());
        BoundarySourceBoundsBuilder2D builder = new BoundarySourceBoundsBuilder2D();

        // act
        Bounds2D b = builder.getBounds(src);

        // assert
        Assert.assertNull(b);
    }

    @Test
    public void testGetBounds_singleFiniteBoundary() {
        // arrange
        Segment seg = Lines.segmentFromPoints(Vector2D.of(1, -2), Vector2D.of(-3, 4), TEST_PRECISION);

        BoundarySource2D src = BoundarySource2D.from(seg);
        BoundarySourceBoundsBuilder2D builder = new BoundarySourceBoundsBuilder2D();

        // act
        Bounds2D b = builder.getBounds(src);

        // assert
        checkBounds(b, Vector2D.of(-3, -2), Vector2D.of(1, 4));
        Assert.assertTrue(b.contains(seg.getStartPoint()));
        Assert.assertTrue(b.contains(seg.getEndPoint()));
    }

    @Test
    public void testGetBounds_multipleFiniteBoundaries() {
        // arrange
        Segment seg1 = Lines.segmentFromPoints(Vector2D.of(1, -2), Vector2D.of(-3, 4), TEST_PRECISION);
        Segment seg2 = Lines.segmentFromPoints(Vector2D.of(0, 1), Vector2D.of(7, 0), TEST_PRECISION);
        Segment seg3 = Lines.segmentFromPoints(Vector2D.of(4, 6), Vector2D.of(-3, 9), TEST_PRECISION);

        BoundarySource2D src = BoundarySource2D.from(seg1, seg2, seg3);
        BoundarySourceBoundsBuilder2D builder = new BoundarySourceBoundsBuilder2D();

        // act
        Bounds2D b = builder.getBounds(src);

        // assert
        checkBounds(b, Vector2D.of(-3, -2), Vector2D.of(7, 9));

        src.boundaryStream().forEach(boundary -> {
            Assert.assertTrue(b.contains(boundary.getStartPoint()));
            Assert.assertTrue(b.contains(boundary.getEndPoint()));
        });
    }

    @Test
    public void testGetBounds_singleInfiniteBoundary() {
        // arrange
        LineConvexSubset boundary = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION)
                .span();
        BoundarySource2D src = BoundarySource2D.from(boundary);
        BoundarySourceBoundsBuilder2D builder = new BoundarySourceBoundsBuilder2D();

        // act
        Bounds2D b = builder.getBounds(src);

        // assert
        Assert.assertNull(b);
    }

    @Test
    public void testGetBounds_mixedFiniteAndInfiniteBoundaries() {
        // arrange
        LineConvexSubset inf = Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION)
                .span()
                .split(Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION))
                .getMinus();

        Segment seg1 = Lines.segmentFromPoints(Vector2D.of(1, -2), Vector2D.of(-3, 4), TEST_PRECISION);
        Segment seg2 = Lines.segmentFromPoints(Vector2D.of(0, 1), Vector2D.of(7, 0), TEST_PRECISION);
        Segment seg3 = Lines.segmentFromPoints(Vector2D.of(4, 6), Vector2D.of(-3, 9), TEST_PRECISION);

        BoundarySource2D src = BoundarySource2D.from(seg1, seg2, inf, seg3);
        BoundarySourceBoundsBuilder2D builder = new BoundarySourceBoundsBuilder2D();

        // act
        Bounds2D b = builder.getBounds(src);

        // assert
        Assert.assertNull(b);
    }

    private static void checkBounds(Bounds2D b, Vector2D min, Vector2D max) {
        EuclideanTestUtils.assertCoordinatesEqual(min, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(max, b.getMax(), TEST_EPS);
    }
}
