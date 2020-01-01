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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class Boundaries2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFrom_varargs_empty() {
        // act
        BoundarySource2D src = Boundaries2D.from();

        // assert
        List<Segment> segments = src.boundaryStream().collect(Collectors.toList());
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testFrom_varargs() {
        // act
        Segment a = Segment.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        Segment b = Segment.fromPoints(Vector2D.Unit.PLUS_X, Vector2D.of(1, 1), TEST_PRECISION);

        BoundarySource2D src = Boundaries2D.from(a, b);

        // assert
        List<Segment> segments = src.boundaryStream().collect(Collectors.toList());
        Assert.assertEquals(2, segments.size());

        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));
    }

    @Test
    public void testFrom_list_empty() {
        // arrange
        List<Segment> input = new ArrayList<>();

        // act
        BoundarySource2D src = Boundaries2D.from(input);

        // assert
        List<Segment> segments = src.boundaryStream().collect(Collectors.toList());
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testFrom_list() {
        // act
        Segment a = Segment.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        Segment b = Segment.fromPoints(Vector2D.Unit.PLUS_X, Vector2D.of(1, 1), TEST_PRECISION);

        List<Segment> input = new ArrayList<>();
        input.add(a);
        input.add(b);

        BoundarySource2D src = Boundaries2D.from(input);

        // assert
        List<Segment> segments = src.boundaryStream().collect(Collectors.toList());
        Assert.assertEquals(2, segments.size());

        Assert.assertSame(a, segments.get(0));
        Assert.assertSame(b, segments.get(1));
    }

    @Test
    public void testRect_minFirst() {
        // act
        List<Segment> segments = Boundaries2D.rect(Vector2D.of(1, 2), Vector2D.of(3, 4), TEST_PRECISION)
                .boundaryStream()
                .collect(Collectors.toList());

        // assert
        Assert.assertEquals(4, segments.size());

        assertSegment(segments.get(0), Vector2D.of(1, 2), Vector2D.of(3, 2));
        assertSegment(segments.get(1), Vector2D.of(3, 4), Vector2D.of(1, 4));
        assertSegment(segments.get(2), Vector2D.of(3, 2), Vector2D.of(3, 4));
        assertSegment(segments.get(3), Vector2D.of(1, 4), Vector2D.of(1, 2));
    }

    @Test
    public void testRect_maxFirst() {
        // act
        List<Segment> segments = Boundaries2D.rect(Vector2D.ZERO, Vector2D.of(-1, -2), TEST_PRECISION)
                .boundaryStream()
                .collect(Collectors.toList());

        // assert
        Assert.assertEquals(4, segments.size());

        assertSegment(segments.get(0), Vector2D.of(-1, -2), Vector2D.of(0, -2));
        assertSegment(segments.get(1), Vector2D.ZERO, Vector2D.of(-1, 0));
        assertSegment(segments.get(2), Vector2D.of(0, -2), Vector2D.ZERO);
        assertSegment(segments.get(3), Vector2D.of(-1, 0), Vector2D.of(-1, -2));
    }

    @Test
    public void testRect_toTree() {
        // act
        RegionBSPTree2D tree = Boundaries2D.rect(Vector2D.ZERO, Vector2D.of(1, 4), TEST_PRECISION).toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(4, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 2), tree.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testRect_illegalArgs() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Boundaries2D.rect(Vector2D.of(1, 1), Vector2D.of(1, 3), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Boundaries2D.rect(Vector2D.of(1, 1), Vector2D.of(3, 1), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Boundaries2D.rect(Vector2D.of(2, 3), Vector2D.of(2, 3), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    private static void assertSegment(Segment segment, Vector2D start, Vector2D end) {
        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }
}
