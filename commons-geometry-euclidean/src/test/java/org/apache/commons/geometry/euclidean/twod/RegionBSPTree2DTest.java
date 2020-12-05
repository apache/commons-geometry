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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.bsp.RegionCutRule;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D.PartitionedRegionBuilder2D;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D.RegionNode2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.geometry.euclidean.twod.shape.Parallelogram;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegionBSPTree2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Comparator<LineConvexSubset> SEGMENT_COMPARATOR =
        (a, b) -> Vector2D.COORDINATE_ASCENDING_ORDER.compare(a.getStartPoint(), b.getStartPoint());

    private static final Line X_AXIS = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION);

    private static final Line Y_AXIS = Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testCtor_booleanArg_true() {
        // act
        final RegionBSPTree2D tree = new RegionBSPTree2D(true);

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    public void testCtor_booleanArg_false() {
        // act
        final RegionBSPTree2D tree = new RegionBSPTree2D(false);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    public void testCtor_default() {
        // act
        final RegionBSPTree2D tree = new RegionBSPTree2D();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    public void testFull_factoryMethod() {
        // act
        final RegionBSPTree2D tree = RegionBSPTree2D.full();

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    public void testEmpty_factoryMethod() {
        // act
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
        Assertions.assertEquals(1, tree.count());
    }

    @Test
    public void testPartitionedRegionBuilder_halfSpace() {
        // act
        final RegionBSPTree2D tree = RegionBSPTree2D.partitionedRegionBuilder()
                .insertPartition(
                    Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION))
                .insertBoundary(
                    Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.MINUS_X, TEST_PRECISION).span())
                .build();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isInfinite());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE, Vector2D.of(0, -1));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY, Vector2D.ZERO);
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE, Vector2D.of(0, 1));
    }

    @Test
    public void testPartitionedRegionBuilder_square() {
        // arrange
        final Parallelogram square = Parallelogram.unitSquare(TEST_PRECISION);
        final List<LineConvexSubset> boundaries = square.getBoundaries();

        final Vector2D lowerBound = Vector2D.of(-2, -2);

        final int maxUpper = 5;
        final int maxLevel = 4;

        // act/assert
        Bounds2D bounds;
        for (int u = 0; u <= maxUpper; ++u) {
            for (int level = 0; level <= maxLevel; ++level) {
                bounds = Bounds2D.from(lowerBound, Vector2D.of(u, u));

                checkFinitePartitionedRegion(bounds, level, square);
                checkFinitePartitionedRegion(bounds, level, boundaries);
            }
        }
    }

    @Test
    public void testPartitionedRegionBuilder_nonConvex() {
        // arrange
        final RegionBSPTree2D src = Parallelogram.unitSquare(TEST_PRECISION).toTree();
        src.union(Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION).toTree());

        final List<LineConvexSubset> boundaries = src.getBoundaries();

        final Vector2D lowerBound = Vector2D.of(-2, -2);

        final int maxUpper = 5;
        final int maxLevel = 4;

        // act/assert
        Bounds2D bounds;
        for (int u = 0; u <= maxUpper; ++u) {
            for (int level = 0; level <= maxLevel; ++level) {
                bounds = Bounds2D.from(lowerBound, Vector2D.of(u, u));

                checkFinitePartitionedRegion(bounds, level, src);
                checkFinitePartitionedRegion(bounds, level, boundaries);
            }
        }
    }

    /** Check that a partitioned BSP tree behaves the same as a non-partitioned tree when
     * constructed with the given boundary source.
     * @param bounds
     * @param level
     * @param src
     */
    private void checkFinitePartitionedRegion(final Bounds2D bounds, final int level, final BoundarySource2D src) {
        // arrange
        final String msg = "Partitioned region check failed with bounds= " + bounds + " and level= " + level;

        final RegionBSPTree2D standard = RegionBSPTree2D.from(src.boundaryStream().collect(Collectors.toList()));

        // act
        final RegionBSPTree2D partitioned = RegionBSPTree2D.partitionedRegionBuilder()
                .insertAxisAlignedGrid(bounds, level, TEST_PRECISION)
                .insertBoundaries(src)
                .build();

        // assert
        Assertions.assertEquals(standard.getSize(), partitioned.getSize(), TEST_EPS, msg);
        Assertions.assertEquals(standard.getBoundarySize(), partitioned.getBoundarySize(), TEST_EPS, msg);
        EuclideanTestUtils.assertCoordinatesEqual(standard.getCentroid(), partitioned.getCentroid(), TEST_EPS);

        final RegionBSPTree2D diff = RegionBSPTree2D.empty();
        diff.difference(partitioned, standard);
        Assertions.assertTrue(diff.isEmpty(), msg);
    }

    /** Check that a partitioned BSP tree behaves the same as a non-partitioned tree when
     * constructed with the given boundaries.
     * @param bounds
     * @param level
     * @param boundaries
     */
    private void checkFinitePartitionedRegion(final Bounds2D bounds, final int level,
                                              final List<? extends LineConvexSubset> boundaries) {
        // arrange
        final String msg = "Partitioned region check failed with bounds= " + bounds + " and level= " + level;

        final RegionBSPTree2D standard = RegionBSPTree2D.from(boundaries);

        // act
        final RegionBSPTree2D partitioned = RegionBSPTree2D.partitionedRegionBuilder()
                .insertAxisAlignedGrid(bounds, level, TEST_PRECISION)
                .insertBoundaries(boundaries)
                .build();

        // assert
        Assertions.assertEquals(standard.getSize(), partitioned.getSize(), TEST_EPS, msg);
        Assertions.assertEquals(standard.getBoundarySize(), partitioned.getBoundarySize(), TEST_EPS, msg);
        EuclideanTestUtils.assertCoordinatesEqual(standard.getCentroid(), partitioned.getCentroid(), TEST_EPS);

        final RegionBSPTree2D diff = RegionBSPTree2D.empty();
        diff.difference(partitioned, standard);
        Assertions.assertTrue(diff.isEmpty(), msg);
    }

    @Test
    public void testPartitionedRegionBuilder_insertPartitionAfterBoundary() {
        // arrange
        final PartitionedRegionBuilder2D builder = RegionBSPTree2D.partitionedRegionBuilder();
        builder.insertBoundary(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION));

        final Line partition = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION);

        final String msg = "Cannot insert partitions after boundaries have been inserted";

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.insertPartition(partition);
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            builder.insertPartition(partition.span());
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            builder.insertAxisAlignedPartitions(Vector2D.ZERO, TEST_PRECISION);
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            builder.insertAxisAlignedGrid(Bounds2D.from(Vector2D.ZERO, Vector2D.of(1, 1)), 1, TEST_PRECISION);
        }, IllegalStateException.class, msg);
    }

    @Test
    public void testCopy() {
        // arrange
        final RegionBSPTree2D tree = new RegionBSPTree2D(true);
        tree.getRoot().cut(Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION));

        // act
        final RegionBSPTree2D copy = tree.copy();

        // assert
        Assertions.assertNotSame(tree, copy);
        Assertions.assertEquals(3, copy.count());
    }

    @Test
    public void testBoundaries() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION)
                .toTree();

        // act
        final List<LineConvexSubset> segments = new ArrayList<>();
        tree.boundaries().forEach(segments::add);

        // assert
        Assertions.assertEquals(4, segments.size());
    }

    @Test
    public void testGetBoundaries() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION)
                .toTree();

        // act
        final List<LineConvexSubset> segments = tree.getBoundaries();

        // assert
        Assertions.assertEquals(4, segments.size());
    }

    @Test
    public void testBoundaryStream() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION)
                .toTree();

        // act
        final List<LineConvexSubset> segments = tree.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(4, segments.size());
    }

    @Test
    public void testBoundaryStream_noBoundaries() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.full();

        // act
        final List<LineConvexSubset> segments = tree.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(0, segments.size());
    }

    @Test
    public void testGetBounds_hasBounds() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.of(2, 3), Vector2D.of(5, 8), TEST_PRECISION)
                .toTree();

        // act
        final Bounds2D bounds = tree.getBounds();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 3), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(5, 8), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testGetBounds_noBounds() {
        // act/assert
        Assertions.assertNull(RegionBSPTree2D.empty().getBounds());
        Assertions.assertNull(RegionBSPTree2D.full().getBounds());

        final RegionBSPTree2D halfFull = RegionBSPTree2D.empty();
        halfFull.getRoot().insertCut(Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION));
        Assertions.assertNull(halfFull.getBounds());
    }

    @Test
    public void testGetBoundaryPaths_cachesResult() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));

        // act
        final List<LinePath> a = tree.getBoundaryPaths();
        final List<LinePath> b = tree.getBoundaryPaths();

        // assert
        Assertions.assertSame(a, b);
    }

    @Test
    public void testGetBoundaryPaths_recomputesResultOnChange() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));

        // act
        final List<LinePath> a = tree.getBoundaryPaths();
        tree.insert(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION));
        final List<LinePath> b = tree.getBoundaryPaths();

        // assert
        Assertions.assertNotSame(a, b);
    }

    @Test
    public void testGetBoundaryPaths_isUnmodifiable() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            tree.getBoundaryPaths().add(LinePath.builder(null).build());
        }, UnsupportedOperationException.class);
    }

    @Test
    public void testAdd_convexArea() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // act
        tree.add(ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO, Vector2D.of(2, 0),
                    Vector2D.of(2, 2), Vector2D.of(0, 2)
                ), TEST_PRECISION));
        tree.add(ConvexArea.convexPolygonFromVertices(Arrays.asList(
                Vector2D.of(1, 1), Vector2D.of(3, 1),
                Vector2D.of(3, 3), Vector2D.of(1, 3)
            ), TEST_PRECISION));

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(7, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(12, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 1.5), tree.getCentroid(), TEST_EPS);

        checkClassify(tree, RegionLocation.INSIDE,
                Vector2D.of(1, 1), Vector2D.of(1.5, 1.5), Vector2D.of(2, 2));
    }

    @Test
    public void testToConvex_full() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.full();

        // act
        final List<ConvexArea> result = tree.toConvex();

        // assert
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.get(0).isFull());
    }

    @Test
    public void testToConvex_empty() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // act
        final List<ConvexArea> result = tree.toConvex();

        // assert
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testToConvex_halfSpace() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.full();
        tree.getRoot().insertCut(Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION));

        // act
        final List<ConvexArea> result = tree.toConvex();

        // assert
        Assertions.assertEquals(1, result.size());

        final ConvexArea area = result.get(0);
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        checkClassify(area, RegionLocation.INSIDE, Vector2D.of(0, 1));
        checkClassify(area, RegionLocation.BOUNDARY, Vector2D.ZERO);
        checkClassify(area, RegionLocation.OUTSIDE, Vector2D.of(0, -1));
    }

    @Test
    public void testToConvex_quadrantComplement() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.full();
        tree.getRoot().cut(Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI, TEST_PRECISION))
            .getPlus().cut(Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION));

        tree.complement();

        // act
        final List<ConvexArea> result = tree.toConvex();

        // assert
        Assertions.assertEquals(1, result.size());

        final ConvexArea area = result.get(0);
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        checkClassify(area, RegionLocation.INSIDE, Vector2D.of(1, 1));
        checkClassify(area, RegionLocation.BOUNDARY, Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(0, 1));
        checkClassify(area, RegionLocation.OUTSIDE, Vector2D.of(1, -1), Vector2D.of(-1, -1), Vector2D.of(-1, 1));
    }

    @Test
    public void testToConvex_square() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION).toTree();

        // act
        final List<ConvexArea> result = tree.toConvex();

        // assert
        Assertions.assertEquals(1, result.size());

        final ConvexArea area = result.get(0);
        Assertions.assertFalse(area.isFull());
        Assertions.assertFalse(area.isEmpty());

        Assertions.assertEquals(1, area.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), area.getCentroid(), TEST_EPS);

        checkClassify(area, RegionLocation.INSIDE, Vector2D.of(0.5, 0.5));
        checkClassify(area, RegionLocation.BOUNDARY, Vector2D.ZERO, Vector2D.of(1, 1));
        checkClassify(area, RegionLocation.OUTSIDE,
                Vector2D.of(0.5, -1), Vector2D.of(0.5, 2),
                Vector2D.of(-1, 0.5), Vector2D.of(2, 0.5));
    }

    @Test
    public void testToConvex_multipleConvexAreas() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(Arrays.asList(
                    Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION),

                    Lines.segmentFromPoints(Vector2D.of(1, 1), Vector2D.of(0, 1), TEST_PRECISION),
                    Lines.segmentFromPoints(Vector2D.of(0, 1), Vector2D.ZERO, TEST_PRECISION),

                    Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION),
                    Lines.segmentFromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION)
                ));

        // act
        final List<ConvexArea> result = tree.toConvex();

        // assert
        result.sort((a, b) ->
                Vector2D.COORDINATE_ASCENDING_ORDER.compare(a.getCentroid(), b.getCentroid()));

        Assertions.assertEquals(2, result.size());

        final ConvexArea firstArea = result.get(0);
        Assertions.assertFalse(firstArea.isFull());
        Assertions.assertFalse(firstArea.isEmpty());

        Assertions.assertEquals(0.5, firstArea.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.0 / 3.0, 2.0 / 3.0), firstArea.getCentroid(), TEST_EPS);

        checkClassify(firstArea, RegionLocation.INSIDE, Vector2D.of(1.0 / 3.0, 2.0 / 3.0));
        checkClassify(firstArea, RegionLocation.BOUNDARY, Vector2D.ZERO, Vector2D.of(1, 1), Vector2D.of(0.5, 0.5));
        checkClassify(firstArea, RegionLocation.OUTSIDE,
                Vector2D.of(0.25, -1), Vector2D.of(0.25, 2),
                Vector2D.of(-1, 0.5), Vector2D.of(0.75, 0.5));

        final ConvexArea secondArea = result.get(1);
        Assertions.assertFalse(secondArea.isFull());
        Assertions.assertFalse(secondArea.isEmpty());

        Assertions.assertEquals(0.5, secondArea.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2.0 / 3.0, 1.0 / 3.0), secondArea.getCentroid(), TEST_EPS);

        checkClassify(secondArea, RegionLocation.INSIDE, Vector2D.of(2.0 / 3.0, 1.0 / 3.0));
        checkClassify(secondArea, RegionLocation.BOUNDARY, Vector2D.ZERO, Vector2D.of(1, 1), Vector2D.of(0.5, 0.5));
        checkClassify(secondArea, RegionLocation.OUTSIDE,
                Vector2D.of(0.75, -1), Vector2D.of(0.75, 2),
                Vector2D.of(2, 0.5), Vector2D.of(0.25, 0.5));
    }

    @Test
    public void testGetNodeRegion() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();

        final RegionNode2D root = tree.getRoot();
        root.cut(Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION));

        final RegionNode2D minus = root.getMinus();
        minus.cut(Lines.fromPointAndAngle(Vector2D.ZERO, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION));

        final Vector2D origin = Vector2D.ZERO;

        final Vector2D a = Vector2D.of(1, 0);
        final Vector2D b = Vector2D.of(1, 1);
        final Vector2D c = Vector2D.of(0, 1);
        final Vector2D d = Vector2D.of(-1, 1);
        final Vector2D e = Vector2D.of(-1, 0);
        final Vector2D f = Vector2D.of(-1, -1);
        final Vector2D g = Vector2D.of(0, -1);
        final Vector2D h = Vector2D.of(1, -1);

        // act/assert
        checkConvexArea(root.getNodeRegion(), Arrays.asList(origin, a, b, c, d, e, f, g, h), Collections.emptyList());

        checkConvexArea(minus.getNodeRegion(), Arrays.asList(b, c, d), Arrays.asList(f, g, h));
        checkConvexArea(root.getPlus().getNodeRegion(), Arrays.asList(f, g, h), Arrays.asList(b, c, d));

        checkConvexArea(minus.getMinus().getNodeRegion(), Collections.singletonList(d), Arrays.asList(a, b, f, g, h));
        checkConvexArea(minus.getPlus().getNodeRegion(), Collections.singletonList(b), Arrays.asList(d, e, f, g, h));
    }

    @Test
    public void testSplit_full() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.full();

        final Line splitter = Lines.fromPointAndAngle(Vector2D.of(1, 0), 0.25 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Split<RegionBSPTree2D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        checkClassify(split.getMinus(), RegionLocation.INSIDE, Vector2D.of(0, 1));
        checkClassify(split.getMinus(), RegionLocation.OUTSIDE, Vector2D.of(1, -1));

        final List<LinePath> minusBoundaryList = split.getMinus().getBoundaryPaths();
        Assertions.assertEquals(1, minusBoundaryList.size());

        final LinePath minusBoundary = minusBoundaryList.get(0);
        Assertions.assertEquals(1, minusBoundary.getElements().size());
        Assertions.assertTrue(minusBoundary.isInfinite());
        Assertions.assertSame(splitter, minusBoundary.getStart().getLine());

        checkClassify(split.getPlus(), RegionLocation.OUTSIDE, Vector2D.of(0, 1));
        checkClassify(split.getPlus(), RegionLocation.INSIDE, Vector2D.of(1, -1));

        final List<LinePath> plusBoundaryList = split.getPlus().getBoundaryPaths();
        Assertions.assertEquals(1, plusBoundaryList.size());

        final LinePath plusBoundary = minusBoundaryList.get(0);
        Assertions.assertEquals(1, plusBoundary.getElements().size());
        Assertions.assertTrue(plusBoundary.isInfinite());
        Assertions.assertSame(splitter, plusBoundary.getStart().getLine());
    }

    @Test
    public void testSplit_empty() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();

        final Line splitter = Lines.fromPointAndAngle(Vector2D.of(1, 0), 0.25 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Split<RegionBSPTree2D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_bothSides() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION)
                .toTree();

        final Line splitter = Lines.fromPointAndAngle(Vector2D.ZERO, 0.25 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Split<RegionBSPTree2D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final List<LinePath> minusPath = split.getMinus().getBoundaryPaths();
        Assertions.assertEquals(1, minusPath.size());
        checkVertices(minusPath.get(0), Vector2D.ZERO, Vector2D.of(1, 1),
                Vector2D.of(0, 1), Vector2D.ZERO);

        final List<LinePath> plusPath = split.getPlus().getBoundaryPaths();
        Assertions.assertEquals(1, plusPath.size());
        checkVertices(plusPath.get(0), Vector2D.ZERO, Vector2D.of(2, 0),
                Vector2D.of(2, 1), Vector2D.of(1, 1), Vector2D.ZERO);
    }

    @Test
    public void testSplit_plusSideOnly() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION)
                .toTree();

        final Line splitter = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.25 * PlaneAngleRadians.PI, TEST_PRECISION);

        // act
        final Split<RegionBSPTree2D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());

        final List<LinePath> plusPath = split.getPlus().getBoundaryPaths();
        Assertions.assertEquals(1, plusPath.size());
        checkVertices(plusPath.get(0), Vector2D.ZERO, Vector2D.of(2, 0),
                Vector2D.of(2, 1), Vector2D.of(0, 1), Vector2D.ZERO);
    }

    @Test
    public void testSplit_minusSideOnly() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(2, 1), TEST_PRECISION)
                .toTree();

        final Line splitter = Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.25 * PlaneAngleRadians.PI, TEST_PRECISION)
                .reverse();

        // act
        final Split<RegionBSPTree2D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        final List<LinePath> minusPath = split.getMinus().getBoundaryPaths();
        Assertions.assertEquals(1, minusPath.size());
        checkVertices(minusPath.get(0), Vector2D.ZERO, Vector2D.of(2, 0),
                Vector2D.of(2, 1), Vector2D.of(0, 1), Vector2D.ZERO);

        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testGeometricProperties_full() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.full();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assertions.assertNull(tree.getCentroid());

        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        Assertions.assertEquals(0, tree.getBoundaries().size());
        Assertions.assertEquals(0, tree.getBoundaryPaths().size());
    }

    @Test
    public void testGeometricProperties_empty() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // act/assert
        Assertions.assertEquals(0, tree.getSize(), TEST_EPS);
        Assertions.assertNull(tree.getCentroid());

        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        Assertions.assertEquals(0, tree.getBoundaries().size());
        Assertions.assertEquals(0, tree.getBoundaryPaths().size());
    }

    @Test
    public void testGeometricProperties_halfSpace() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.full();
        tree.getRoot().cut(X_AXIS);

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assertions.assertNull(tree.getCentroid());

        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());

        final List<LineConvexSubset> segments = tree.getBoundaries();
        Assertions.assertEquals(1, segments.size());

        final LineConvexSubset segment = segments.get(0);
        Assertions.assertSame(X_AXIS, segment.getLine());
        Assertions.assertNull(segment.getStartPoint());
        Assertions.assertNull(segment.getEndPoint());

        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final LinePath path = paths.get(0);
        Assertions.assertEquals(1, path.getElements().size());
        assertSegmentsEqual(segment, path.getStart());
    }

    @Test
    public void testGeometricProperties_complementedHalfSpace() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.full();
        tree.getRoot().cut(X_AXIS);

        tree.complement();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assertions.assertNull(tree.getCentroid());

        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());

        final List<LineConvexSubset> segments = tree.getBoundaries();
        Assertions.assertEquals(1, segments.size());

        final LineConvexSubset segment = segments.get(0);
        Assertions.assertEquals(X_AXIS.reverse(), segment.getLine());
        Assertions.assertNull(segment.getStartPoint());
        Assertions.assertNull(segment.getEndPoint());

        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final LinePath path = paths.get(0);
        Assertions.assertEquals(1, path.getElements().size());
        assertSegmentsEqual(segment, path.getElements().get(0));
    }

    @Test
    public void testGeometricProperties_quadrant() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.getRoot().cut(X_AXIS)
            .getMinus().cut(Y_AXIS);

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assertions.assertNull(tree.getCentroid());

        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());

        final List<LineConvexSubset> segments = new ArrayList<>(tree.getBoundaries());
        Assertions.assertEquals(2, segments.size());

        segments.sort(SEGMENT_COMPARATOR);

        final LineConvexSubset firstSegment = segments.get(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, firstSegment.getStartPoint(), TEST_EPS);
        Assertions.assertNull(firstSegment.getEndPoint());
        Assertions.assertSame(Y_AXIS, firstSegment.getLine());

        final LineConvexSubset secondSegment = segments.get(1);
        Assertions.assertNull(secondSegment.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, secondSegment.getEndPoint(), TEST_EPS);
        Assertions.assertSame(X_AXIS, secondSegment.getLine());

        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final LinePath path = paths.get(0);
        Assertions.assertEquals(2, path.getElements().size());
        assertSegmentsEqual(secondSegment, path.getElements().get(0));
        assertSegmentsEqual(firstSegment, path.getElements().get(1));
    }

    @Test
    public void testGeometricProperties_mixedCutRule() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();

        tree.getRoot().cut(Lines.fromPointAndAngle(Vector2D.ZERO, 0.25 * Math.PI, TEST_PRECISION),
                RegionCutRule.INHERIT);

        tree.getRoot()
            .getPlus().cut(X_AXIS, RegionCutRule.MINUS_INSIDE)
                .getMinus().cut(Lines.fromPointAndAngle(Vector2D.of(1, 0), 0.5 * Math.PI, TEST_PRECISION));

        tree.getRoot()
            .getMinus().cut(Lines.fromPointAndAngle(Vector2D.ZERO, 0.5 * Math.PI, TEST_PRECISION), RegionCutRule.PLUS_INSIDE)
                .getPlus().cut(Lines.fromPointAndAngle(Vector2D.of(1, 1), Math.PI, TEST_PRECISION))
                    .getMinus().cut(Lines.fromPointAndAngle(Vector2D.of(0.5, 0.5), 0.75 * Math.PI, TEST_PRECISION), RegionCutRule.INHERIT);

        // act/assert
        Assertions.assertEquals(1, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), tree.getCentroid(), TEST_EPS);

        Assertions.assertEquals(4, tree.getBoundarySize(), TEST_EPS);

        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final LinePath path = paths.get(0);
        Assertions.assertEquals(4, path.getElements().size());

        final List<Vector2D> vertices = path.getVertexSequence();
        Assertions.assertEquals(5, vertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 0), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 1), vertices.get(3), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, vertices.get(4), TEST_EPS);
    }

    @Test
    public void testGeometricProperties_complementedQuadrant() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.getRoot().cut(X_AXIS)
            .getMinus().cut(Y_AXIS);

        tree.complement();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assertions.assertNull(tree.getCentroid());

        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());

        final List<LineConvexSubset> segments = new ArrayList<>(tree.getBoundaries());
        Assertions.assertEquals(2, segments.size());

        segments.sort(SEGMENT_COMPARATOR);

        final LineConvexSubset firstSegment = segments.get(0);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, firstSegment.getStartPoint(), TEST_EPS);
        Assertions.assertNull(firstSegment.getEndPoint());
        Assertions.assertEquals(X_AXIS.reverse(), firstSegment.getLine());

        final LineConvexSubset secondSegment = segments.get(1);
        Assertions.assertNull(secondSegment.getStartPoint());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, secondSegment.getEndPoint(), TEST_EPS);
        Assertions.assertEquals(Y_AXIS.reverse(), secondSegment.getLine());

        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final LinePath path = paths.get(0);
        Assertions.assertEquals(2, path.getElements().size());
        assertSegmentsEqual(secondSegment, path.getElements().get(0));
        assertSegmentsEqual(firstSegment, path.getElements().get(1));
    }

    @Test
    public void testGeometricProperties_closedRegion() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(LinePath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(2, 1))
                .close());

        // act/assert
        Assertions.assertEquals(0.5, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1.0 / 3.0), tree.getCentroid(), TEST_EPS);

        Assertions.assertEquals(1.0 + Math.sqrt(2) + Math.sqrt(5), tree.getBoundarySize(), TEST_EPS);

        final List<LineConvexSubset> segments = new ArrayList<>(tree.getBoundaries());
        segments.sort(SEGMENT_COMPARATOR);

        Assertions.assertEquals(3, segments.size());

        checkFiniteSegment(segments.get(0), Vector2D.ZERO, Vector2D.of(1, 0));
        checkFiniteSegment(segments.get(1), Vector2D.of(1, 0), Vector2D.of(2, 1));
        checkFiniteSegment(segments.get(2), Vector2D.of(2, 1), Vector2D.ZERO);

        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        checkVertices(paths.get(0), Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(2, 1), Vector2D.ZERO);
    }

    @Test
    public void testGeometricProperties_complementedClosedRegion() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(LinePath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(2, 1))
                .close());

        tree.complement();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assertions.assertNull(tree.getCentroid());

        Assertions.assertEquals(1.0 + Math.sqrt(2) + Math.sqrt(5), tree.getBoundarySize(), TEST_EPS);

        final List<LineConvexSubset> segments = new ArrayList<>(tree.getBoundaries());
        segments.sort(SEGMENT_COMPARATOR);

        Assertions.assertEquals(3, segments.size());

        checkFiniteSegment(segments.get(0), Vector2D.ZERO, Vector2D.of(2, 1));
        checkFiniteSegment(segments.get(1), Vector2D.of(1, 0), Vector2D.ZERO);
        checkFiniteSegment(segments.get(2), Vector2D.of(2, 1), Vector2D.of(1, 0));

        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        checkVertices(paths.get(0), Vector2D.ZERO, Vector2D.of(2, 1), Vector2D.of(1, 0), Vector2D.ZERO);
    }

    @Test
    public void testGeometricProperties_regionWithHole() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(3, 3), TEST_PRECISION)
                .toTree();
        final RegionBSPTree2D inner = Parallelogram.axisAligned(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION)
                .toTree();

        tree.difference(inner);

        // act/assert
        Assertions.assertEquals(8, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 1.5), tree.getCentroid(), TEST_EPS);

        Assertions.assertEquals(16, tree.getBoundarySize(), TEST_EPS);

        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(2, paths.size());

        checkVertices(paths.get(0), Vector2D.of(0, 3), Vector2D.ZERO, Vector2D.of(3, 0),
                Vector2D.of(3, 3), Vector2D.of(0, 3));
        checkVertices(paths.get(1), Vector2D.of(1, 1), Vector2D.of(1, 2), Vector2D.of(2, 2),
                Vector2D.of(2, 1), Vector2D.of(1, 1));
    }

    @Test
    public void testGeometricProperties_complementedRegionWithHole() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(3, 3), TEST_PRECISION)
                .toTree();
        final RegionBSPTree2D inner = Parallelogram.axisAligned(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION)
                .toTree();

        tree.difference(inner);

        tree.complement();

        // act/assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assertions.assertNull(tree.getCentroid());

        Assertions.assertEquals(16, tree.getBoundarySize(), TEST_EPS);

        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(2, paths.size());

        checkVertices(paths.get(0), Vector2D.ZERO, Vector2D.of(0, 3), Vector2D.of(3, 3),
                Vector2D.of(3, 0), Vector2D.ZERO);
        checkVertices(paths.get(1), Vector2D.of(1, 1), Vector2D.of(2, 1), Vector2D.of(2, 2),
                Vector2D.of(1, 2), Vector2D.of(1, 1));
    }

    @Test
    public void testFrom_boundaries() {
        // act
        final RegionBSPTree2D tree = RegionBSPTree2D.from(Arrays.asList(
                    Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).span(),
                    Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION)
                        .rayFrom(Vector2D.ZERO)
                ));

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.getRoot().getLocation());

        checkClassify(tree, RegionLocation.INSIDE, Vector2D.of(-1, 1));
        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector2D.of(1, 1), Vector2D.of(1, -1), Vector2D.of(-1, -1));
    }

    @Test
    public void testFrom_boundaries_fullIsTrue() {
        // act
        final RegionBSPTree2D tree = RegionBSPTree2D.from(Arrays.asList(
                    Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).span(),
                    Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION)
                        .rayFrom(Vector2D.ZERO)
                ), true);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(RegionLocation.INSIDE, tree.getRoot().getLocation());

        checkClassify(tree, RegionLocation.INSIDE, Vector2D.of(-1, 1));
        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector2D.of(1, 1), Vector2D.of(1, -1), Vector2D.of(-1, -1));
    }

    @Test
    public void testFrom_boundaries_noBoundaries() {
        // act/assert
        Assertions.assertTrue(RegionBSPTree2D.from(Collections.emptyList()).isEmpty());
        Assertions.assertTrue(RegionBSPTree2D.from(Collections.emptyList(), true).isFull());
        Assertions.assertTrue(RegionBSPTree2D.from(Collections.emptyList(), false).isEmpty());
    }

    @Test
    public void testToTree_returnsSameInstance() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(1, 2), TEST_PRECISION).toTree();

        // act/assert
        Assertions.assertSame(tree, tree.toTree());
    }

    @Test
    public void testProject_fullAndEmpty() {
        // act/assert
        Assertions.assertNull(RegionBSPTree2D.full().project(Vector2D.ZERO));
        Assertions.assertNull(RegionBSPTree2D.empty().project(Vector2D.of(1, 2)));
    }

    @Test
    public void testProject_halfSpace() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.full();
        tree.getRoot().cut(X_AXIS);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, tree.project(Vector2D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 0), tree.project(Vector2D.of(-1, 0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 0),
                tree.project(Vector2D.of(2, -1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-3, 0),
                tree.project(Vector2D.of(-3, 1)), TEST_EPS);
    }

    @Test
    public void testProject_rect() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(
                    Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).toTree();

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), tree.project(Vector2D.ZERO), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), tree.project(Vector2D.of(1, 0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 1), tree.project(Vector2D.of(1.5, 0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), tree.project(Vector2D.of(2, 0)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1), tree.project(Vector2D.of(3, 0)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), tree.project(Vector2D.of(1, 3)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), tree.project(Vector2D.of(1, 3)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 2), tree.project(Vector2D.of(1.5, 3)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 2), tree.project(Vector2D.of(2, 3)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 2), tree.project(Vector2D.of(3, 3)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1.5), tree.project(Vector2D.of(0, 1.5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1.5), tree.project(Vector2D.of(1.5, 1.5)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1.5), tree.project(Vector2D.of(3, 1.5)), TEST_EPS);
    }

    @Test
    public void testLinecast_empty() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();

        // act/assert
        LinecastChecker2D.with(tree)
            .expectNothing()
            .whenGiven(Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));

        LinecastChecker2D.with(tree)
            .expectNothing()
            .whenGiven(Lines.segmentFromPoints(Vector2D.Unit.MINUS_X, Vector2D.Unit.PLUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast_full() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.full();

        // act/assert
        LinecastChecker2D.with(tree)
            .expectNothing()
            .whenGiven(Lines.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));

        LinecastChecker2D.with(tree)
            .expectNothing()
            .whenGiven(Lines.segmentFromPoints(Vector2D.Unit.MINUS_X, Vector2D.Unit.PLUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION)
                .toTree();

        // act/assert
        LinecastChecker2D.with(tree)
            .expectNothing()
            .whenGiven(Lines.fromPoints(Vector2D.of(0, 5), Vector2D.of(1, 6), TEST_PRECISION));

        LinecastChecker2D.with(tree)
            .expect(Vector2D.ZERO, Vector2D.Unit.MINUS_X)
            .and(Vector2D.ZERO, Vector2D.Unit.MINUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Lines.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION));

        LinecastChecker2D.with(tree)
            .expect(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Lines.segmentFromPoints(Vector2D.of(0.5, 0.5), Vector2D.of(1, 1), TEST_PRECISION));
    }

    @Test
    public void testLinecast_complementedTree() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION)
                .toTree();

        tree.complement();

        // act/assert
        LinecastChecker2D.with(tree)
            .expectNothing()
            .whenGiven(Lines.fromPoints(Vector2D.of(0, 5), Vector2D.of(1, 6), TEST_PRECISION));

        LinecastChecker2D.with(tree)
            .expect(Vector2D.ZERO, Vector2D.Unit.PLUS_Y)
            .and(Vector2D.ZERO, Vector2D.Unit.PLUS_X)
            .and(Vector2D.of(1, 1), Vector2D.Unit.MINUS_X)
            .and(Vector2D.of(1, 1), Vector2D.Unit.MINUS_Y)
            .whenGiven(Lines.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION));

        LinecastChecker2D.with(tree)
            .expect(Vector2D.of(1, 1), Vector2D.Unit.MINUS_X)
            .and(Vector2D.of(1, 1), Vector2D.Unit.MINUS_Y)
            .whenGiven(Lines.segmentFromPoints(Vector2D.of(0.5, 0.5), Vector2D.of(1, 1), TEST_PRECISION));
    }

    @Test
    public void testLinecast_complexRegion() {
        // arrange
        final RegionBSPTree2D a = LinePath.fromVertexLoop(Arrays.asList(
                    Vector2D.ZERO, Vector2D.of(0, 1),
                    Vector2D.of(0.5, 1), Vector2D.of(0.5, 0)
                ), TEST_PRECISION).toTree();
        a.complement();

        final RegionBSPTree2D b = LinePath.fromVertexLoop(Arrays.asList(
                Vector2D.of(0.5, 0), Vector2D.of(0.5, 1),
                Vector2D.of(1, 1), Vector2D.of(1, 0)
            ), TEST_PRECISION).toTree();
        b.complement();

        final RegionBSPTree2D c = LinePath.fromVertexLoop(Arrays.asList(
                Vector2D.of(0.5, 0.5), Vector2D.of(1.5, 0.5),
                Vector2D.of(1.5, 1.5), Vector2D.of(0.5, 1.5)
            ), TEST_PRECISION).toTree();

        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.union(a, b);
        tree.union(c);

        // act/assert
        LinecastChecker2D.with(tree)
            .expect(Vector2D.of(1.5, 1.5), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1.5, 1.5), Vector2D.Unit.PLUS_X)
            .whenGiven(Lines.segmentFromPoints(Vector2D.of(0.25, 0.25), Vector2D.of(2, 2), TEST_PRECISION));
    }

    @Test
    public void testLinecast_removesDuplicatePoints() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_Y, TEST_PRECISION).span());
        tree.insert(Lines.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION).span());

        // act/assert
        LinecastChecker2D.with(tree)
            .expect(Vector2D.ZERO, Vector2D.Unit.MINUS_Y)
            .whenGiven(Lines.fromPoints(Vector2D.of(1, 1), Vector2D.of(-1, -1), TEST_PRECISION));

        LinecastChecker2D.with(tree)
            .expect(Vector2D.ZERO, Vector2D.Unit.MINUS_Y)
            .whenGiven(Lines.segmentFromPoints(Vector2D.of(1, 1), Vector2D.of(-1, -1), TEST_PRECISION));
    }

    @Test
    public void testTransform() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.of(1, 1), Vector2D.of(3, 2), TEST_PRECISION)
                .toTree();

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(0.5, 2)
                .rotate(PlaneAngleRadians.PI_OVER_TWO)
                .translate(Vector2D.of(0, -1));

        // act
        tree.transform(transform);

        // assert
        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final LinePath path = paths.get(0);
        Assertions.assertEquals(4, path.getElements().size());
        checkFiniteSegment(path.getElements().get(0), Vector2D.of(-4, -0.5), Vector2D.of(-2, -0.5));
        checkFiniteSegment(path.getElements().get(1), Vector2D.of(-2, -0.5), Vector2D.of(-2, 0.5));
        checkFiniteSegment(path.getElements().get(2), Vector2D.of(-2, 0.5), Vector2D.of(-4, 0.5));
        checkFiniteSegment(path.getElements().get(3), Vector2D.of(-4, 0.5), Vector2D.of(-4, -0.5));
    }

    @Test
    public void testTransform_halfSpace() {
        // arrange
        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.getRoot().insertCut(Lines.fromPointAndAngle(Vector2D.of(0, 1), 0.0, TEST_PRECISION));

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createScale(0.5, 2)
                .rotate(PlaneAngleRadians.PI_OVER_TWO)
                .translate(Vector2D.of(1, 0));

        // act
        tree.transform(transform);

        // assert
        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final LinePath path = paths.get(0);
        Assertions.assertEquals(1, path.getElements().size());
        final LineConvexSubset segment = path.getStart();
        Assertions.assertNull(segment.getStartPoint());
        Assertions.assertNull(segment.getEndPoint());

        final Line expectedLine = Lines.fromPointAndAngle(Vector2D.of(-1, 0), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION);
        Assertions.assertTrue(expectedLine.eq(segment.getLine(), expectedLine.getPrecision()));
    }

    @Test
    public void testTransform_fullAndEmpty() {
        // arrange
        final RegionBSPTree2D full = RegionBSPTree2D.full();
        final RegionBSPTree2D empty = RegionBSPTree2D.empty();

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.createRotation(PlaneAngleRadians.PI_OVER_TWO);

        // act
        full.transform(transform);
        empty.transform(transform);

        // assert
        Assertions.assertTrue(full.isFull());
        Assertions.assertTrue(empty.isEmpty());
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).toTree();

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.from(v -> Vector2D.of(-v.getX(), v.getY()));

        // act
        tree.transform(transform);

        // assert
        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final LinePath path = paths.get(0);
        Assertions.assertEquals(4, path.getElements().size());
        checkFiniteSegment(path.getElements().get(0), Vector2D.of(-2, 1), Vector2D.of(-1, 1));
        checkFiniteSegment(path.getElements().get(1), Vector2D.of(-1, 1), Vector2D.of(-1, 2));
        checkFiniteSegment(path.getElements().get(2), Vector2D.of(-1, 2), Vector2D.of(-2, 2));
        checkFiniteSegment(path.getElements().get(3), Vector2D.of(-2, 2), Vector2D.of(-2, 1));
    }

    @Test
    public void testTransform_doubleReflection() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(
                    Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).toTree();

        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.from(Vector2D::negate);

        // act
        tree.transform(transform);

        // assert
        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final LinePath path = paths.get(0);
        Assertions.assertEquals(4, path.getElements().size());
        checkFiniteSegment(path.getElements().get(0), Vector2D.of(-2, -2), Vector2D.of(-1, -2));
        checkFiniteSegment(path.getElements().get(1), Vector2D.of(-1, -2), Vector2D.of(-1, -1));
        checkFiniteSegment(path.getElements().get(2), Vector2D.of(-1, -1), Vector2D.of(-2, -1));
        checkFiniteSegment(path.getElements().get(3), Vector2D.of(-2, -1), Vector2D.of(-2, -2));
    }

    @Test
    public void testBooleanOperations() {
        // arrange
        final RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(3, 3), TEST_PRECISION).toTree();
        RegionBSPTree2D temp;

        // act
        temp = Parallelogram.axisAligned(Vector2D.of(1, 1), Vector2D.of(2, 2), TEST_PRECISION).toTree();
        temp.complement();
        tree.intersection(temp);

        temp = Parallelogram.axisAligned(Vector2D.of(3, 0), Vector2D.of(6, 3), TEST_PRECISION).toTree();
        tree.union(temp);

        temp = Parallelogram.axisAligned(Vector2D.of(2, 1), Vector2D.of(5, 2), TEST_PRECISION).toTree();
        tree.difference(temp);

        temp.setFull();
        tree.xor(temp);

        // assert
        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(2, paths.size());

        checkVertices(paths.get(0), Vector2D.ZERO, Vector2D.of(0, 3), Vector2D.of(6, 3),
                Vector2D.of(6, 0), Vector2D.ZERO);

        checkVertices(paths.get(1), Vector2D.of(1, 1), Vector2D.of(5, 1), Vector2D.of(5, 2),
                Vector2D.of(1, 2), Vector2D.of(1, 1));
    }

    private static void assertSegmentsEqual(final LineConvexSubset expected, final LineConvexSubset actual) {
        Assertions.assertEquals(expected.getLine(), actual.getLine());

        final Vector2D expectedStart = expected.getStartPoint();
        final Vector2D expectedEnd = expected.getEndPoint();

        if (expectedStart != null) {
            EuclideanTestUtils.assertCoordinatesEqual(expectedStart, actual.getStartPoint(), TEST_EPS);
        } else {
            Assertions.assertNull(actual.getStartPoint());
        }

        if (expectedEnd != null) {
            EuclideanTestUtils.assertCoordinatesEqual(expectedEnd, actual.getEndPoint(), TEST_EPS);
        } else {
            Assertions.assertNull(actual.getEndPoint());
        }
    }

    private static void checkFiniteSegment(final LineConvexSubset segment, final Vector2D start, final Vector2D end) {
        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }

    private static void checkClassify(final Region<Vector2D> region, final RegionLocation loc, final Vector2D... points) {
        for (final Vector2D point : points) {
            final String msg = "Unexpected location for point " + point;

            Assertions.assertEquals(loc, region.classify(point), msg);
        }
    }

    private static void checkConvexArea(final ConvexArea area, final List<Vector2D> inside, final List<Vector2D> outside) {
        checkClassify(area, RegionLocation.INSIDE, inside.toArray(new Vector2D[0]));
        checkClassify(area, RegionLocation.OUTSIDE, outside.toArray(new Vector2D[0]));
    }

    /** Assert that the given path is finite and contains the given vertices.
     * @param path
     * @param vertices
     */
    private static void checkVertices(final LinePath path, final Vector2D... vertices) {
        Assertions.assertTrue(path.isFinite(), "Line segment path is not finite");

        final List<Vector2D> actual = path.getVertexSequence();

        Assertions.assertEquals(vertices.length, actual.size(), "Vertex lists have different lengths");

        for (int i  = 0; i < vertices.length; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(vertices[i], actual.get(i), TEST_EPS);
        }
    }
}
