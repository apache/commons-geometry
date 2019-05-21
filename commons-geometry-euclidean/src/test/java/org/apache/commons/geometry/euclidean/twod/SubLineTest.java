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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.partition.ConvexSubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.twod.SubLine.SubLineBuilder;
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

    @Test
    public void testBuilder_createEmpty() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.ZERO_PI, TEST_PRECISION);

        SubLineBuilder builder = new SubLineBuilder(line);

        // act
        SubLine subline = builder.build();

        // assert
        Assert.assertFalse(subline.isFull());
        Assert.assertTrue(subline.isEmpty());

        List<LineSegment> segments = subline.toConvex();
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testBuilder_addConvex() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.ZERO_PI, TEST_PRECISION);
        Line otherLine = Line.fromPointAndAngle(Vector2D.of(0, 1), 1e-11, TEST_PRECISION);

        SubLineBuilder builder = new SubLineBuilder(line);

        // act
        builder.add(LineSegment.fromInterval(line, 2, 4));
        builder.add(LineSegment.fromInterval(otherLine, 1, 3));
        builder.add(LineSegment.fromPoints(Vector2D.of(-4, 1), Vector2D.of(-1, 1), TEST_PRECISION));

        SubLine subline = builder.build();

        // assert
        Assert.assertFalse(subline.isFull());
        Assert.assertFalse(subline.isEmpty());

        List<LineSegment> segments = subline.toConvex();

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-4, 1), segments.get(0).getStart(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), segments.get(0).getEnd(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStart(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEnd(), TEST_EPS);
    }

    @Test
    public void testBuilder_addNonConvex() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.ZERO_PI, TEST_PRECISION);

        SubLine a = new SubLine(line);
        RegionBSPTree1D aTree = a.getSubspaceRegion();
        aTree.add(Interval.max(-3, TEST_PRECISION));
        aTree.add(Interval.of(1, 2, TEST_PRECISION));

        SubLine b = new SubLine(line);
        RegionBSPTree1D bTree = b.getSubspaceRegion();
        bTree.add(Interval.of(2, 4, TEST_PRECISION));
        bTree.add(Interval.of(-4, -2, TEST_PRECISION));

        SubLineBuilder builder = new SubLineBuilder(line);

        int aTreeCount = aTree.count();
        int bTreeCount = bTree.count();

        // act
        builder.add(a);
        builder.add(b);

        SubLine subline = builder.build();

        // assert
        Assert.assertFalse(subline.isFull());
        Assert.assertFalse(subline.isEmpty());

        List<LineSegment> segments = subline.toConvex();

        Assert.assertEquals(2, segments.size());

        Assert.assertNull(segments.get(0).getStart());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 1), segments.get(0).getEnd(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStart(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEnd(), TEST_EPS);

        Assert.assertEquals(aTreeCount, aTree.count());
        Assert.assertEquals(bTreeCount, bTree.count());
    }

    @Test
    public void testBuilder_argumentsFromDifferentLine() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.ZERO_PI, TEST_PRECISION);
        Line otherLine = Line.fromPointAndAngle(Vector2D.of(0, 1), 1e-2, TEST_PRECISION);

        SubLineBuilder builder = new SubLineBuilder(line);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add(LineSegment.fromInterval(otherLine, 0, 1));
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            builder.add(new SubLine(otherLine));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_unknownSubLineType() {
        // arrange
        Line line = Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.ZERO_PI, TEST_PRECISION);

        AbstractSubLine<Interval> unknownType = new AbstractSubLine<Interval>(line) {

            @Override
            public boolean isInfinite() {
                return false;
            }

            @Override
            public List<? extends ConvexSubHyperplane<Vector2D>> toConvex() {
                return null;
            }

            @Override
            public Region<Vector1D> getSubspaceRegion() {
                return null;
            }
        };

        SubLineBuilder builder = new SubLineBuilder(line);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add(unknownType);
        }, IllegalArgumentException.class);
    }
}
