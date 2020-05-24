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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Interval;
import org.apache.commons.geometry.euclidean.oned.RegionBSPTree1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.junit.Assert;
import org.junit.Test;

public class LineSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testToSubspaceAndToSpace() {
        // arrange
        LineSubset subset = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION)
                .span();

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), subset.toSpace(Vector1D.of(2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector1D.of(2), subset.toSubspace(Vector2D.of(2, 1)), TEST_EPS);
    }

    @Test
    public void testBuilder_empty() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        HyperplaneSubset.Builder<Vector2D> builder = line.span().builder();

        // act
        LineSubset subset = (LineSubset) builder.build();

        // assert
        Assert.assertFalse(subset.isFull());
        Assert.assertTrue(subset.isEmpty());

        List<LineConvexSubset> segments = subset.toConvex();
        Assert.assertEquals(0, segments.size());
    }

    @Test
    public void testBuilder_addSingleConvex_usesSameInstance() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        HyperplaneSubset.Builder<Vector2D> builder = line.span().builder();
        LineConvexSubset convex = Lines.subsetFromInterval(line, 2, 4);

        // act
        builder.add(convex);

        LineSubset subset = (LineSubset) builder.build();

        // assert
        Assert.assertSame(convex, subset);
    }

    @Test
    public void testBuilder_addConvex() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line otherLine = Lines.fromPointAndAngle(Vector2D.of(0, 1), 1e-11, TEST_PRECISION);

        HyperplaneSubset.Builder<Vector2D> builder = line.span().builder();

        // act
        builder.add(Lines.subsetFromInterval(line, 2, 4));
        builder.add(Lines.subsetFromInterval(otherLine, 1, 3));
        builder.add(Lines.segmentFromPoints(Vector2D.of(-4, 1), Vector2D.of(-1, 1), TEST_PRECISION));

        LineSubset subset = (LineSubset) builder.build();

        // assert
        Assert.assertFalse(subset.isFull());
        Assert.assertFalse(subset.isEmpty());

        List<LineConvexSubset> segments = subset.toConvex();
        Assert.assertEquals(2, segments.size());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-4, 1), segments.get(0).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEndPoint(), TEST_EPS);
    }

    @Test
    public void testBuilder_addTreeSubset() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        EmbeddedTreeLineSubset a = new EmbeddedTreeLineSubset(line);
        RegionBSPTree1D aTree = a.getSubspaceRegion();
        aTree.add(Interval.max(-3, TEST_PRECISION));
        aTree.add(Interval.of(1, 2, TEST_PRECISION));

        EmbeddedTreeLineSubset b = new EmbeddedTreeLineSubset(line);
        RegionBSPTree1D bTree = b.getSubspaceRegion();
        bTree.add(Interval.of(2, 4, TEST_PRECISION));
        bTree.add(Interval.of(-4, -2, TEST_PRECISION));

        HyperplaneSubset.Builder<Vector2D> builder = line.span().builder();

        int aTreeCount = aTree.count();
        int bTreeCount = bTree.count();

        // act
        builder.add(a);
        builder.add(b);

        LineSubset subset = (LineSubset) builder.build();

        // assert
        Assert.assertFalse(subset.isFull());
        Assert.assertFalse(subset.isEmpty());

        List<LineConvexSubset> segments = subset.toConvex();

        Assert.assertEquals(2, segments.size());

        Assert.assertNull(segments.get(0).getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2, 1), segments.get(0).getEndPoint(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(1).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(1).getEndPoint(), TEST_EPS);

        Assert.assertEquals(aTreeCount, aTree.count());
        Assert.assertEquals(bTreeCount, bTree.count());
    }

    @Test
    public void testBuilder_addMixed_convexFirst() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line otherLine = Lines.fromPointAndAngle(Vector2D.of(0, 1), 1e-11, TEST_PRECISION);

        HyperplaneSubset.Builder<Vector2D> builder = line.span().builder();

        // act
        builder.add(Lines.subsetFromInterval(line, 2, 4));

        EmbeddedTreeLineSubset treeSubset = new EmbeddedTreeLineSubset(otherLine);
        treeSubset.add(Lines.subsetFromInterval(otherLine, 1, 3));
        builder.add(treeSubset);

        LineSubset subset = (LineSubset) builder.build();

        // assert
        Assert.assertFalse(subset.isFull());
        Assert.assertFalse(subset.isEmpty());

        List<LineConvexSubset> segments = subset.toConvex();
        Assert.assertEquals(1, segments.size());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(0).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(0).getEndPoint(), TEST_EPS);
    }

    @Test
    public void testBuilder_addMixed_treeSubsetFirst() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line otherLine = Lines.fromPointAndAngle(Vector2D.of(0, 1), 1e-11, TEST_PRECISION);

        HyperplaneSubset.Builder<Vector2D> builder = line.span().builder();

        // act
        EmbeddedTreeLineSubset treeSubset = new EmbeddedTreeLineSubset(otherLine);
        treeSubset.add(Lines.subsetFromInterval(otherLine, 1, 3));
        builder.add(treeSubset);

        builder.add(Lines.subsetFromInterval(line, 2, 4));

        LineSubset subset = (LineSubset) builder.build();

        // assert
        Assert.assertFalse(subset.isFull());
        Assert.assertFalse(subset.isEmpty());

        List<LineConvexSubset> segments = subset.toConvex();
        Assert.assertEquals(1, segments.size());

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), segments.get(0).getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(4, 1), segments.get(0).getEndPoint(), TEST_EPS);
    }

    @Test
    public void testBuilder_nullArgs() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        HyperplaneSubset.Builder<Vector2D> builder = line.span().builder();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add((HyperplaneSubset<Vector2D>) null);
        }, NullPointerException.class, "Hyperplane subset must not be null");

        GeometryTestUtils.assertThrows(() -> {
            builder.add((HyperplaneConvexSubset<Vector2D>) null);
        }, NullPointerException.class, "Hyperplane subset must not be null");
    }

    @Test
    public void testBuilder_argumentsFromDifferentLine() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);
        Line otherLine = Lines.fromPointAndAngle(Vector2D.of(0, 1), 1e-2, TEST_PRECISION);

        HyperplaneSubset.Builder<Vector2D> builder = line.span().builder();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add(Lines.subsetFromInterval(otherLine, 0, 1));
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            builder.add(new EmbeddedTreeLineSubset(otherLine));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_unknownSubsetType() {
        // arrange
        Line line = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION);

        LineSubset unknownType = new LineSubset(line) {
            @Override
            public boolean isInfinite() {
                return false;
            }

            @Override
            public boolean isFinite() {
                return true;
            }

            @Override
            public List<LineConvexSubset> toConvex() {
                return null;
            }

            @Override
            public HyperplaneBoundedRegion<Vector1D> getSubspaceRegion() {
                return null;
            }

            @Override
            public Split<? extends HyperplaneSubset<Vector2D>> split(Hyperplane<Vector2D> splitter) {
                return null;
            }

            @Override
            public HyperplaneSubset<Vector2D> transform(Transform<Vector2D> transform) {
                return null;
            }

            @Override
            public Vector2D closest(Vector2D point) {
                return null;
            }

            @Override
            public boolean isFull() {
                return false;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public double getSize() {
                return 0;
            }

            @Override
            public Vector2D getBarycenter() {
                return null;
            }

            @Override
            public Bounds2D getBounds() {
                return null;
            }

            @Override
            RegionLocation classifyAbscissa(double abscissa) {
                return null;
            }
        };

        HyperplaneSubset.Builder<Vector2D> builder = line.span().builder();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add(unknownType);
        }, IllegalArgumentException.class);
    }
}
