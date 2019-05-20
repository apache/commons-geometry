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

import java.util.List;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.junit.Assert;
import org.junit.Test;

public class SubLineTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private Line line = Line.fromPointAndDirection(Vector2D.of(0, 1), Vector2D.PLUS_X, TEST_PRECISION);

    @Test
    public void testCtor_lineOnly() {
        // act
        SubLine sub = new SubLine(line);

        // assert
        Assert.assertSame(line, sub.getLine());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());

        Assert.assertFalse(sub.isFull());
        Assert.assertTrue(sub.isEmpty());
        Assert.assertFalse(sub.isInfinite());
    }

    @Test
    public void testCtor_lineAndBoolean() {
        // act
        SubLine sub = new SubLine(line, true);

        // assert
        Assert.assertSame(line, sub.getLine());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());

        Assert.assertTrue(sub.isFull());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertTrue(sub.isInfinite());
    }

    @Test
    public void testCtor_lineAndRegion() {
        // arrange
        RegionBSPTree1D tree = RegionBSPTree1D.full();

        // act
        SubLine sub = new SubLine(line, tree);

        // assert
        Assert.assertSame(line, sub.getLine());
        Assert.assertSame(tree, sub.getSubspaceRegion());
        Assert.assertSame(TEST_PRECISION, sub.getPrecision());

        Assert.assertTrue(sub.isFull());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertTrue(sub.isInfinite());
    }

    @Test
    public void testToConvex_full() {
        // arrange
        SubLine sub = new SubLine(line, true);

        // act
        List<LineSegment> segments = sub.toConvex();

        // assert
        Assert.assertEquals(1, segments.size());

        LineSegment seg = segments.get(0);
        Assert.assertTrue(seg.isFull());
    }

    @Test
    public void testToConvex_empty() {
        // arrange
        SubLine sub = new SubLine(line, false);

        // act
        List<LineSegment> segments = sub.toConvex();

        // assert
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testToConvex_finiteAndInfiniteSegments() {
        // arrange
        SubLine sub = new SubLine(line, false);
        RegionBSPTree1D tree = sub.getSubspaceRegion();
        tree.add(Interval.max(-2.0, TEST_PRECISION));
        tree.add(Interval.of(-1, 2, TEST_PRECISION));

        // act
        List<LineSegment> segments = sub.toConvex();

        // assert
        Assert.assertEquals(2, segments.size());

        Assert.assertNull(segments.get(0).getStart());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 1), segments.get(0).getEnd(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), segments.get(1).getStart(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), segments.get(1).getEnd(), TEST_EPS);
    }
}
