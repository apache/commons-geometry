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
package org.apache.commons.geometry.euclidean.threed;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.bsp.RegionCutRule;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D.PartitionedRegionBuilder3D;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D.RegionNode3D;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LinecastPoint3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegionBSPTree3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testCtor_default() {
        // act
        final RegionBSPTree3D tree = new RegionBSPTree3D();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
    }

    @Test
    public void testCtor_boolean() {
        // act
        final RegionBSPTree3D a = new RegionBSPTree3D(true);
        final RegionBSPTree3D b = new RegionBSPTree3D(false);

        // assert
        Assertions.assertTrue(a.isFull());
        Assertions.assertFalse(a.isEmpty());

        Assertions.assertFalse(b.isFull());
        Assertions.assertTrue(b.isEmpty());
    }

    @Test
    public void testEmpty() {
        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());

        Assertions.assertNull(tree.getCentroid());
        Assertions.assertEquals(0.0, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                Vector3D.of(-100, -100, -100),
                Vector3D.of(0, 0, 0),
                Vector3D.of(100, 100, 100),
                Vector3D.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    public void testFull() {
        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.full();

        // assert
        Assertions.assertTrue(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertNull(tree.getCentroid());
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assertions.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                Vector3D.of(-100, -100, -100),
                Vector3D.of(0, 0, 0),
                Vector3D.of(100, 100, 100),
                Vector3D.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    public void testPartitionedRegionBuilder_halfSpace() {
        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.partitionedRegionBuilder()
                .insertPartition(
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION))
                .insertBoundary(
                        Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Z, TEST_PRECISION).span())
                .build();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isInfinite());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE, Vector3D.of(0, 0, 1));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY, Vector3D.ZERO);
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE, Vector3D.of(0, 0, -1));
    }

    @Test
    public void testPartitionedRegionBuilder_cube() {
        // arrange
        final Parallelepiped cube = Parallelepiped.unitCube(TEST_PRECISION);
        final List<PlaneConvexSubset> boundaries = cube.getBoundaries();

        final Vector3D lowerBound = Vector3D.of(-2, -2, -2);

        final int maxUpper = 5;
        final int maxLevel = 4;

        // act/assert
        Bounds3D bounds;
        for (int u = 0; u <= maxUpper; ++u) {
            for (int level = 0; level <= maxLevel; ++level) {
                bounds = Bounds3D.from(lowerBound, Vector3D.of(u, u, u));

                checkFinitePartitionedRegion(bounds, level, cube);
                checkFinitePartitionedRegion(bounds, level, boundaries);
            }
        }
    }

    @Test
    public void testPartitionedRegionBuilder_nonConvex() {
        // arrange
        final RegionBSPTree3D src = Parallelepiped.unitCube(TEST_PRECISION).toTree();
        src.union(Parallelepiped.axisAligned(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION).toTree());

        final List<PlaneConvexSubset> boundaries = src.getBoundaries();

        final Vector3D lowerBound = Vector3D.of(-2, -2, -2);

        final int maxUpper = 5;
        final int maxLevel = 4;

        // act/assert
        Bounds3D bounds;
        for (int u = 0; u <= maxUpper; ++u) {
            for (int level = 0; level <= maxLevel; ++level) {
                bounds = Bounds3D.from(lowerBound, Vector3D.of(u, u, u));

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
    private void checkFinitePartitionedRegion(final Bounds3D bounds, final int level, final BoundarySource3D src) {
        // arrange
        final String msg = "Partitioned region check failed with bounds= " + bounds + " and level= " + level;

        final RegionBSPTree3D standard = RegionBSPTree3D.from(src.boundaryStream().collect(Collectors.toList()));

        // act
        final RegionBSPTree3D partitioned = RegionBSPTree3D.partitionedRegionBuilder()
                .insertAxisAlignedGrid(bounds, level, TEST_PRECISION)
                .insertBoundaries(src)
                .build();

        // assert
        Assertions.assertEquals(standard.getSize(), partitioned.getSize(), TEST_EPS, msg);
        Assertions.assertEquals(standard.getBoundarySize(), partitioned.getBoundarySize(), TEST_EPS, msg);
        EuclideanTestUtils.assertCoordinatesEqual(standard.getCentroid(), partitioned.getCentroid(), TEST_EPS);

        final RegionBSPTree3D diff = RegionBSPTree3D.empty();
        diff.difference(partitioned, standard);
        Assertions.assertTrue(diff.isEmpty(), msg);
    }

    /** Check that a partitioned BSP tree behaves the same as a non-partitioned tree when
     * constructed with the given boundaries.
     * @param bounds
     * @param level
     * @param boundaries
     */
    private void checkFinitePartitionedRegion(final Bounds3D bounds, final int level,
                                              final List<? extends PlaneConvexSubset> boundaries) {
        // arrange
        final String msg = "Partitioned region check failed with bounds= " + bounds + " and level= " + level;

        final RegionBSPTree3D standard = RegionBSPTree3D.from(boundaries);

        // act
        final RegionBSPTree3D partitioned = RegionBSPTree3D.partitionedRegionBuilder()
                .insertAxisAlignedGrid(bounds, level, TEST_PRECISION)
                .insertBoundaries(boundaries)
                .build();

        // assert
        Assertions.assertEquals(standard.getSize(), partitioned.getSize(), TEST_EPS, msg);
        Assertions.assertEquals(standard.getBoundarySize(), partitioned.getBoundarySize(), TEST_EPS, msg);
        EuclideanTestUtils.assertCoordinatesEqual(standard.getCentroid(), partitioned.getCentroid(), TEST_EPS);

        final RegionBSPTree3D diff = RegionBSPTree3D.empty();
        diff.difference(partitioned, standard);
        Assertions.assertTrue(diff.isEmpty(), msg);
    }

    @Test
    public void testPartitionedRegionBuilder_insertPartitionAfterBoundary() {
        // arrange
        final PartitionedRegionBuilder3D builder = RegionBSPTree3D.partitionedRegionBuilder();
        builder.insertBoundary(Planes.triangleFromVertices(
                Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), TEST_PRECISION));

        final Plane partition = Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        final String msg = "Cannot insert partitions after boundaries have been inserted";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.insertPartition(partition);
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.insertPartition(partition.span());
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.insertAxisAlignedPartitions(Vector3D.ZERO, TEST_PRECISION);
        }, IllegalStateException.class, msg);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            builder.insertAxisAlignedGrid(Bounds3D.from(Vector3D.ZERO, Vector3D.of(1, 1, 1)), 1, TEST_PRECISION);
        }, IllegalStateException.class, msg);
    }

    @Test
    public void testCopy() {
        // arrange
        final RegionBSPTree3D tree = new RegionBSPTree3D(true);
        tree.getRoot().cut(Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION));

        // act
        final RegionBSPTree3D copy = tree.copy();

        // assert
        Assertions.assertNotSame(tree, copy);
        Assertions.assertEquals(3, copy.count());
    }

    @Test
    public void testBoundaries() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act
        final List<PlaneConvexSubset> facets = new ArrayList<>();
        tree.boundaries().forEach(facets::add);

        // assert
        Assertions.assertEquals(6, facets.size());
    }

    @Test
    public void testGetBoundaries() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act
        final List<PlaneConvexSubset> facets = tree.getBoundaries();

        // assert
        Assertions.assertEquals(6, facets.size());
    }

    @Test
    public void testBoundaryStream() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act
        final List<PlaneConvexSubset> facets = tree.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(6, facets.size());
    }

    @Test
    public void testBoundaryStream_noBoundaries() {
        // arrange
        final RegionBSPTree3D tree = RegionBSPTree3D.full();

        // act
        final List<PlaneConvexSubset> facets = tree.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(0, facets.size());
    }

    @Test
    public void testTriangleStream_noBoundaries() {
        // arrange
        final RegionBSPTree3D full = RegionBSPTree3D.full();
        final RegionBSPTree3D empty = RegionBSPTree3D.empty();

        // act/assert
        Assertions.assertEquals(0, full.triangleStream().count());
        Assertions.assertEquals(0, empty.triangleStream().count());
    }

    @Test
    public void testTriangleStream() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act
        final List<Triangle3D> tris = tree.triangleStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(12, tris.size());
    }

    @Test
    public void testTriangleStream_roundTrip() {
        // arrange
        final RegionBSPTree3D a = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));
        final RegionBSPTree3D b = createRect(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(1.5, 1.5, 1.5));

        final RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(a);
        tree.union(b);

        // act
        final List<Triangle3D> tris = tree.triangleStream().collect(Collectors.toList());
        final RegionBSPTree3D result = RegionBSPTree3D.from(tris);

        // assert
        Assertions.assertEquals(15.0 / 8.0, result.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.75, 0.75, 0.75), result.getCentroid(), TEST_EPS);
    }

    @Test
    public void testToTriangleMesh() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act
        final TriangleMesh mesh = tree.toTriangleMesh(TEST_PRECISION);

        // assert
        Assertions.assertEquals(8, mesh.getVertexCount());
        Assertions.assertEquals(12, mesh.getFaceCount());

        final Bounds3D bounds = mesh.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), bounds.getMax(), TEST_EPS);

        final RegionBSPTree3D otherTree = mesh.toTree();
        Assertions.assertEquals(1, otherTree.getSize(), TEST_EPS);
        Assertions.assertEquals(6, otherTree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), otherTree.getCentroid(), TEST_EPS);
    }

    @Test
    public void testToTriangleMesh_empty() {
        // arrange
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();

        // act
        final TriangleMesh mesh = tree.toTriangleMesh(TEST_PRECISION);

        // assert
        // no boundaries
        Assertions.assertEquals(0, mesh.getVertexCount());
        Assertions.assertEquals(0, mesh.getFaceCount());
    }

    @Test
    public void testToTriangleMesh_full() {
        // arrange
        final RegionBSPTree3D tree = RegionBSPTree3D.full();

        // act
        final TriangleMesh mesh = tree.toTriangleMesh(TEST_PRECISION);

        // assert
        // no boundaries
        Assertions.assertEquals(0, mesh.getVertexCount());
        Assertions.assertEquals(0, mesh.getFaceCount());
    }

    @Test
    public void testToTriangleMesh_infiniteBoundary() {
        // arrange
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.getRoot().insertCut(Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION));

        // act/assert
        Assertions.assertThrows(IllegalStateException.class, () -> tree.toTriangleMesh(TEST_PRECISION));
    }

    @Test
    public void testGetBounds_hasBounds() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act
        final Bounds3D bounds = tree.getBounds();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testGetBounds_noBounds() {
        // act/assert
        Assertions.assertNull(RegionBSPTree3D.empty().getBounds());
        Assertions.assertNull(RegionBSPTree3D.full().getBounds());

        final RegionBSPTree3D halfFull = RegionBSPTree3D.empty();
        halfFull.getRoot().insertCut(Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION));
        Assertions.assertNull(halfFull.getBounds());
    }

    @Test
    public void testToList() {
        // arrange
        final RegionBSPTree3D tree = Parallelepiped.axisAligned(
                Vector3D.ZERO, Vector3D.of(1, 3, 3), TEST_PRECISION).toTree();

        // act
        final BoundaryList3D list = tree.toList();

        // assert
        Assertions.assertEquals(6, list.count());
        Assertions.assertEquals(9, list.toTree().getSize());
    }

    @Test
    public void testToList_fullAndEmpty() {
        // act/assert
        Assertions.assertEquals(0, RegionBSPTree3D.full().toList().count());
        Assertions.assertEquals(0, RegionBSPTree3D.empty().toList().count());
    }

    @Test
    public void testToTree_returnsSameInstance() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 2, 1));

        // act/assert
        Assertions.assertSame(tree, tree.toTree());
    }

    @Test
    public void testHalfSpace() {
        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.insert(Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, TEST_PRECISION).span());

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        EuclideanTestUtils.assertPositiveInfinity(tree.getSize());
        EuclideanTestUtils.assertPositiveInfinity(tree.getBoundarySize());
        Assertions.assertNull(tree.getCentroid());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                Vector3D.of(-100, -100, -100));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY, Vector3D.of(0, 0, 0));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(100, 100, 100),
                Vector3D.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    public void testGeometricProperties_mixedCutRules() {
        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();

        final Vector3D min = Vector3D.ZERO;
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Plane top = Planes.fromPointAndNormal(max, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Plane bottom = Planes.fromPointAndNormal(min, Vector3D.Unit.MINUS_Z, TEST_PRECISION);
        final Plane left = Planes.fromPointAndNormal(min, Vector3D.Unit.MINUS_X, TEST_PRECISION);
        final Plane right = Planes.fromPointAndNormal(max, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final Plane front = Planes.fromPointAndNormal(min, Vector3D.Unit.MINUS_Y, TEST_PRECISION);
        final Plane back = Planes.fromPointAndNormal(max, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        final Plane diag = Planes.fromPointAndNormal(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(0.5, -0.5, 0), TEST_PRECISION);
        final Plane midCut = Planes.fromPointAndNormal(Vector3D.of(0.5, 0.5, 0.5), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        tree.getRoot()
            .cut(diag, RegionCutRule.INHERIT);

        tree.getRoot()
            .getMinus().cut(top)
            .getMinus().cut(bottom.reverse(), RegionCutRule.PLUS_INSIDE)
            .getPlus().cut(left, RegionCutRule.MINUS_INSIDE)
            .getMinus().cut(back.reverse(), RegionCutRule.PLUS_INSIDE)
            .getPlus().cut(midCut, RegionCutRule.INHERIT);

        tree.getRoot()
            .getPlus().cut(top.reverse(), RegionCutRule.PLUS_INSIDE)
            .getPlus().cut(bottom)
            .getMinus().cut(right, RegionCutRule.MINUS_INSIDE)
            .getMinus().cut(front.reverse(), RegionCutRule.PLUS_INSIDE)
            .getPlus().cut(midCut, RegionCutRule.INHERIT);

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(1, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE, Vector3D.of(0.5, 0.5, 0.5));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY, min, max);
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(2, 2, 2), Vector3D.of(2, 2, -2),
                Vector3D.of(2, -2, 2), Vector3D.of(2, -2, -2),
                Vector3D.of(-2, 2, 2), Vector3D.of(-2, 2, -2),
                Vector3D.of(-2, -2, 2), Vector3D.of(-2, -2, -2));
    }

    @Test
    public void testFrom_boundaries() {
        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.from(Arrays.asList(
                    Planes.convexPolygonFromVertices(Arrays.asList(
                            Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION),
                    Planes.convexPolygonFromVertices(Arrays.asList(
                            Vector3D.ZERO, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_X), TEST_PRECISION)
                ));

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(RegionLocation.OUTSIDE, tree.getRoot().getLocation());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(1, 1, -1), Vector3D.of(-1, 1, -1));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(1, 1, 1), Vector3D.of(-1, 1, 1), Vector3D.of(1, -1, 1),
                Vector3D.of(-1, -1, 1), Vector3D.of(1, -1, -1), Vector3D.of(-1, -1, -1));
    }

    @Test
    public void testFrom_boundaries_fullIsTrue() {
        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.from(Arrays.asList(
                    Planes.convexPolygonFromVertices(Arrays.asList(
                            Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION),
                    Planes.convexPolygonFromVertices(Arrays.asList(
                            Vector3D.ZERO, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_X), TEST_PRECISION)
                ), true);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(RegionLocation.INSIDE, tree.getRoot().getLocation());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(1, 1, -1), Vector3D.of(-1, 1, -1));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(1, 1, 1), Vector3D.of(-1, 1, 1), Vector3D.of(1, -1, 1),
                Vector3D.of(-1, -1, 1), Vector3D.of(1, -1, -1), Vector3D.of(-1, -1, -1));
    }

    @Test
    public void testFrom_boundaries_noBoundaries() {
        // act/assert
        Assertions.assertTrue(RegionBSPTree3D.from(Collections.emptyList()).isEmpty());
        Assertions.assertTrue(RegionBSPTree3D.from(Collections.emptyList(), true).isFull());
        Assertions.assertTrue(RegionBSPTree3D.from(Collections.emptyList(), false).isEmpty());
    }

    @Test
    public void testFromConvexVolume_full() {
        // arrange
        final ConvexVolume volume = ConvexVolume.full();

        // act
        final RegionBSPTree3D tree = volume.toTree();
        Assertions.assertNull(tree.getCentroid());

        // assert
        Assertions.assertTrue(tree.isFull());
    }

    @Test
    public void testFromConvexVolume_infinite() {
        // arrange
        final ConvexVolume volume = ConvexVolume.fromBounds(Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION));

        // act
        final RegionBSPTree3D tree = volume.toTree();

        // assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());
        Assertions.assertNull(tree.getCentroid());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE, Vector3D.of(0, 0, 1));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY, Vector3D.ZERO);
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE, Vector3D.of(0, 0, -1));
    }

    @Test
    public void testFromConvexVolume_finite() {
        // arrange
        final ConvexVolume volume = ConvexVolume.fromBounds(
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_X, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Y, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Z, TEST_PRECISION),

                    Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y, TEST_PRECISION),
                    Planes.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION)
                );

        // act
        final RegionBSPTree3D tree = volume.toTree();

        // assert
        Assertions.assertEquals(1, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0.5, 0.5), Vector3D.of(2, 0.5, 0.5),
                Vector3D.of(0.5, -1, 0.5), Vector3D.of(0.5, 2, 0.5),
                Vector3D.of(0.5, 0.5, -1), Vector3D.of(0.5, 0.5, 2));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY, Vector3D.ZERO);
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE, Vector3D.of(0.5, 0.5, 0.5));
    }

    @Test
    public void testLinecast_empty() {
        // arrange
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();

        // act/assert
        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Lines3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION));

        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast_full() {
        // arrange
        final RegionBSPTree3D tree = RegionBSPTree3D.full();

        // act/assert
        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Lines3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION));

        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act/assert
        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Lines3D.fromPoints(Vector3D.of(0, 5, 5), Vector3D.of(1, 6, 6), TEST_PRECISION));

        final Vector3D corner = Vector3D.of(1, 1, 1);

        LinecastChecker3D.with(tree)
            .expect(Vector3D.ZERO, Vector3D.Unit.MINUS_X)
            .and(Vector3D.ZERO, Vector3D.Unit.MINUS_Y)
            .and(Vector3D.ZERO, Vector3D.Unit.MINUS_Z)
            .and(corner, Vector3D.Unit.PLUS_Z)
            .and(corner, Vector3D.Unit.PLUS_Y)
            .and(corner, Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.fromPoints(Vector3D.ZERO, corner, TEST_PRECISION));

        LinecastChecker3D.with(tree)
            .expect(corner, Vector3D.Unit.PLUS_Z)
            .and(corner, Vector3D.Unit.PLUS_Y)
            .and(corner, Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(0.5, 0.5, 0.5), corner, TEST_PRECISION));
    }

    @Test
    public void testLinecast_complementedTree() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        tree.complement();

        // act/assert
        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Lines3D.fromPoints(Vector3D.of(0, 5, 5), Vector3D.of(1, 6, 6), TEST_PRECISION));

        final Vector3D corner = Vector3D.of(1, 1, 1);

        LinecastChecker3D.with(tree)
            .expect(Vector3D.ZERO, Vector3D.Unit.PLUS_Z)
            .and(Vector3D.ZERO, Vector3D.Unit.PLUS_Y)
            .and(Vector3D.ZERO, Vector3D.Unit.PLUS_X)
            .and(corner, Vector3D.Unit.MINUS_X)
            .and(corner, Vector3D.Unit.MINUS_Y)
            .and(corner, Vector3D.Unit.MINUS_Z)
            .whenGiven(Lines3D.fromPoints(Vector3D.ZERO, corner, TEST_PRECISION));

        LinecastChecker3D.with(tree)
            .expect(corner, Vector3D.Unit.MINUS_X)
            .and(corner, Vector3D.Unit.MINUS_Y)
            .and(corner, Vector3D.Unit.MINUS_Z)
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(0.5, 0.5, 0.5), corner, TEST_PRECISION));
    }

    @Test
    public void testLinecast_complexRegion() {
        // arrange
        final RegionBSPTree3D a = RegionBSPTree3D.empty();
        Parallelepiped.axisAligned(Vector3D.ZERO, Vector3D.of(0.5, 1, 1), TEST_PRECISION).boundaryStream()
            .map(PlaneConvexSubset::reverse)
            .forEach(a::insert);
        a.complement();

        final RegionBSPTree3D b = RegionBSPTree3D.empty();
        Parallelepiped.axisAligned(Vector3D.of(0.5, 0, 0), Vector3D.of(1, 1, 1), TEST_PRECISION).boundaryStream()
            .map(PlaneConvexSubset::reverse)
            .forEach(b::insert);
        b.complement();

        final RegionBSPTree3D c = createRect(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(1.5, 1.5, 1.5));

        final RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(a, b);
        tree.union(c);

        // act/assert
        final Vector3D corner = Vector3D.of(1.5, 1.5, 1.5);

        LinecastChecker3D.with(tree)
            .expect(corner, Vector3D.Unit.PLUS_Z)
            .and(corner, Vector3D.Unit.PLUS_Y)
            .and(corner, Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(0.25, 0.25, 0.25), Vector3D.of(2, 2, 2), TEST_PRECISION));
    }

    @Test
    public void testLinecast_removesDuplicatePoints() {
        // arrange
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.insert(Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION).span());
        tree.insert(Planes.fromNormal(Vector3D.Unit.PLUS_Y, TEST_PRECISION).span());

        // act/assert
        LinecastChecker3D.with(tree)
            .expect(Vector3D.ZERO, Vector3D.Unit.PLUS_Y)
            .whenGiven(Lines3D.fromPoints(Vector3D.of(1, 1, 1), Vector3D.of(-1, -1, -1), TEST_PRECISION));

        LinecastChecker3D.with(tree)
        .expect(Vector3D.ZERO, Vector3D.Unit.PLUS_Y)
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(1, 1, 1), Vector3D.of(-1, -1, -1), TEST_PRECISION));
    }

    @Test
    public void testLinecastFirst_multipleDirections() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.of(-1, -1, -1), Vector3D.of(1, 1, 1));

        final Line3D xPlus = Lines3D.fromPoints(Vector3D.ZERO, Vector3D.of(1, 0, 0), TEST_PRECISION);
        final Line3D xMinus = Lines3D.fromPoints(Vector3D.ZERO, Vector3D.of(-1, 0, 0), TEST_PRECISION);

        final Line3D yPlus = Lines3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, 1, 0), TEST_PRECISION);
        final Line3D yMinus = Lines3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, -1, 0), TEST_PRECISION);

        final Line3D zPlus = Lines3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, 0, 1), TEST_PRECISION);
        final Line3D zMinus = Lines3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, 0, -1), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0),
                tree.linecastFirst(xPlus.rayFrom(Vector3D.of(-1.1, 0, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0),
                tree.linecastFirst(xPlus.rayFrom(Vector3D.of(-1, 0, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0),
                tree.linecastFirst(xPlus.rayFrom(Vector3D.of(-0.9, 0, 0))).getNormal(), TEST_EPS);
        Assertions.assertNull(tree.linecastFirst(xPlus.rayFrom(Vector3D.of(1.1, 0, 0))));

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0),
                tree.linecastFirst(xMinus.rayFrom(Vector3D.of(1.1, 0, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0),
                tree.linecastFirst(xMinus.rayFrom(Vector3D.of(1, 0, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0),
                tree.linecastFirst(xMinus.rayFrom(Vector3D.of(0.9, 0, 0))).getNormal(), TEST_EPS);
        Assertions.assertNull(tree.linecastFirst(xMinus.rayFrom(Vector3D.of(-1.1, 0, 0))));

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -1, 0),
                tree.linecastFirst(yPlus.rayFrom(Vector3D.of(0, -1.1, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -1, 0),
                tree.linecastFirst(yPlus.rayFrom(Vector3D.of(0, -1, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0),
                tree.linecastFirst(yPlus.rayFrom(Vector3D.of(0, -0.9, 0))).getNormal(), TEST_EPS);
        Assertions.assertNull(tree.linecastFirst(yPlus.rayFrom(Vector3D.of(0, 1.1, 0))));

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0),
                tree.linecastFirst(yMinus.rayFrom(Vector3D.of(0, 1.1, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0),
                tree.linecastFirst(yMinus.rayFrom(Vector3D.of(0, 1, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -1, 0),
                tree.linecastFirst(yMinus.rayFrom(Vector3D.of(0, 0.9, 0))).getNormal(), TEST_EPS);
        Assertions.assertNull(tree.linecastFirst(yMinus.rayFrom(Vector3D.of(0, -1.1, 0))));

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1),
                tree.linecastFirst(zPlus.rayFrom(Vector3D.of(0, 0, -1.1))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1),
                tree.linecastFirst(zPlus.rayFrom(Vector3D.of(0, 0, -1))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1),
                tree.linecastFirst(zPlus.rayFrom(Vector3D.of(0, 0, -0.9))).getNormal(), TEST_EPS);
        Assertions.assertNull(tree.linecastFirst(zPlus.rayFrom(Vector3D.of(0, 0, 1.1))));

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1),
                tree.linecastFirst(zMinus.rayFrom(Vector3D.of(0, 0, 1.1))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1),
                tree.linecastFirst(zMinus.rayFrom(Vector3D.of(0, 0, 1))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1),
                tree.linecastFirst(zMinus.rayFrom(Vector3D.of(0, 0, 0.9))).getNormal(), TEST_EPS);
        Assertions.assertNull(tree.linecastFirst(zMinus.rayFrom(Vector3D.of(0, 0, -1.1))));
    }

    // issue GEOMETRY-38
    @Test
    public void testLinecastFirst_linePassesThroughVertex() {
        // arrange
        final Vector3D lowerCorner = Vector3D.ZERO;
        final Vector3D upperCorner = Vector3D.of(1, 1, 1);
        final Vector3D center = lowerCorner.lerp(upperCorner, 0.5);

        final RegionBSPTree3D tree = createRect(lowerCorner, upperCorner);

        final Line3D upDiagonal = Lines3D.fromPoints(lowerCorner, upperCorner, TEST_PRECISION);
        final Line3D downDiagonal = upDiagonal.reverse();

        // act/assert
        final LinecastPoint3D upFromOutsideResult = tree.linecastFirst(upDiagonal.rayFrom(Vector3D.of(-1, -1, -1)));
        Assertions.assertNotNull(upFromOutsideResult);
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, upFromOutsideResult.getPoint(), TEST_EPS);

        final LinecastPoint3D upFromCenterResult = tree.linecastFirst(upDiagonal.rayFrom(center));
        Assertions.assertNotNull(upFromCenterResult);
        EuclideanTestUtils.assertCoordinatesEqual(upperCorner, upFromCenterResult.getPoint(), TEST_EPS);

        final LinecastPoint3D downFromOutsideResult = tree.linecastFirst(downDiagonal.rayFrom(Vector3D.of(2, 2, 2)));
        Assertions.assertNotNull(downFromOutsideResult);
        EuclideanTestUtils.assertCoordinatesEqual(upperCorner, downFromOutsideResult.getPoint(), TEST_EPS);

        final LinecastPoint3D downFromCenterResult = tree.linecastFirst(downDiagonal.rayFrom(center));
        Assertions.assertNotNull(downFromCenterResult);
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, downFromCenterResult.getPoint(), TEST_EPS);
    }

    // Issue GEOMETRY-43
    @Test
    public void testLinecastFirst_lineParallelToFace() {
        // arrange - setup box
        final Vector3D lowerCorner = Vector3D.ZERO;
        final Vector3D upperCorner = Vector3D.of(1, 1, 1);

        final RegionBSPTree3D tree = createRect(lowerCorner, upperCorner);

        final Vector3D firstPointOnLine = Vector3D.of(0.5, -1.0, 0);
        final Vector3D secondPointOnLine = Vector3D.of(0.5, 2.0, 0);
        final Line3D bottomLine = Lines3D.fromPoints(firstPointOnLine, secondPointOnLine, TEST_PRECISION);

        final Vector3D expectedIntersection1 = Vector3D.of(0.5, 0, 0.0);
        final Vector3D expectedIntersection2 = Vector3D.of(0.5, 1.0, 0.0);

        // act/assert
        LinecastPoint3D bottom = tree.linecastFirst(bottomLine.rayFrom(firstPointOnLine));
        Assertions.assertNotNull(bottom);
        EuclideanTestUtils.assertCoordinatesEqual(expectedIntersection1, bottom.getPoint(), TEST_EPS);

        bottom = tree.linecastFirst(bottomLine.rayFrom(Vector3D.of(0.5, 0.1, 0.0)));
        Assertions.assertNotNull(bottom);
        final Vector3D intersection = bottom.getPoint();
        Assertions.assertNotNull(intersection);
        EuclideanTestUtils.assertCoordinatesEqual(expectedIntersection2, intersection, TEST_EPS);
    }

    @Test
    public void testLinecastFirst_rayPointOnFace() {
        // arrange
        final Vector3D lowerCorner = Vector3D.ZERO;
        final Vector3D upperCorner = Vector3D.of(1, 1, 1);

        final RegionBSPTree3D tree = createRect(lowerCorner, upperCorner);

        final Vector3D pt = Vector3D.of(0.5, 0.5, 0);
        final Line3D intoBoxLine = Lines3D.fromPoints(pt, pt.add(Vector3D.Unit.PLUS_Z), TEST_PRECISION);
        final Line3D outOfBoxLine = Lines3D.fromPoints(pt, pt.add(Vector3D.Unit.MINUS_Z), TEST_PRECISION);

        // act/assert
        final LinecastPoint3D intoBoxResult = tree.linecastFirst(intoBoxLine.rayFrom(pt));
        EuclideanTestUtils.assertCoordinatesEqual(pt, intoBoxResult.getPoint(), TEST_EPS);

        final LinecastPoint3D outOfBoxResult = tree.linecastFirst(outOfBoxLine.rayFrom(pt));
        EuclideanTestUtils.assertCoordinatesEqual(pt, outOfBoxResult.getPoint(), TEST_EPS);
    }

    @Test
    public void testLinecastFirst_rayPointOnVertex() {
        // arrange
        final Vector3D lowerCorner = Vector3D.ZERO;
        final Vector3D upperCorner = Vector3D.of(1, 1, 1);

        final RegionBSPTree3D tree = createRect(lowerCorner, upperCorner);

        final Line3D intoBoxLine = Lines3D.fromPoints(lowerCorner, upperCorner, TEST_PRECISION);
        final Line3D outOfBoxLine = intoBoxLine.reverse();

        // act/assert
        final LinecastPoint3D intoBoxResult = tree.linecastFirst(intoBoxLine.rayFrom(lowerCorner));
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, intoBoxResult.getPoint(), TEST_EPS);

        final LinecastPoint3D outOfBoxResult = tree.linecastFirst(outOfBoxLine.rayFrom(lowerCorner));
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, outOfBoxResult.getPoint(), TEST_EPS);
    }

    @Test
    public void testLinecastFirst_onlyReturnsPointsWithinSegment() throws IOException, ParseException {
        // arrange
        final Vector3D lowerCorner = Vector3D.ZERO;
        final Vector3D upperCorner = Vector3D.of(1, 1, 1);

        final RegionBSPTree3D tree = createRect(lowerCorner, upperCorner);

        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0.5, 0.5, 0.5), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X,
                tree.linecastFirst(line.span()).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X,
                tree.linecastFirst(line.reverse().span()).getNormal(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X,
                tree.linecastFirst(line.segment(Vector3D.of(-2, 0.5, 0.5), Vector3D.of(0.5, 0.5, 0.5))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X,
                tree.linecastFirst(line.segment(Vector3D.of(-2, 0.5, 0.5), Vector3D.of(0, 0.5, 0.5))).getNormal(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X,
                tree.linecastFirst(line.segment(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(2, 0.5, 0.5))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X,
                tree.linecastFirst(line.segment(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(1, 0.5, 0.5))).getNormal(), TEST_EPS);

        Assertions.assertNull(tree.linecastFirst(line.segment(Vector3D.of(-2, 0.5, 0.5), Vector3D.of(-1, 0.5, 0.5))));
        Assertions.assertNull(tree.linecastFirst(line.segment(Vector3D.of(-2, 0.5, 0.5), Vector3D.of(-1, 0.5, 0.5))));
        Assertions.assertNull(tree.linecastFirst(line.segment(Vector3D.of(0.25, 0.5, 0.5), Vector3D.of(0.75, 0.5, 0.5))));
    }

    @Test
    public void testInvertedRegion() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5));

        // act
        tree.complement();

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        EuclideanTestUtils.assertPositiveInfinity(tree.getSize());
        Assertions.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        Assertions.assertNull(tree.getCentroid());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                Vector3D.of(-100, -100, -100),
                Vector3D.of(100, 100, 100),
                Vector3D.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(0, 0, 0));
    }

    @Test
    public void testUnitBox() {
        // act
        final RegionBSPTree3D tree = createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5));

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(1.0, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(6.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tree.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, -1, 0),
                Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, -1),
                Vector3D.of(0, 0, 1),

                Vector3D.of(1, 1, 1),
                Vector3D.of(1, 1, -1),
                Vector3D.of(1, -1, 1),
                Vector3D.of(1, -1, -1),
                Vector3D.of(-1, 1, 1),
                Vector3D.of(-1, 1, -1),
                Vector3D.of(-1, -1, 1),
                Vector3D.of(-1, -1, -1));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY,
                Vector3D.of(0.5, 0, 0),
                Vector3D.of(-0.5, 0, 0),
                Vector3D.of(0, 0.5, 0),
                Vector3D.of(0, -0.5, 0),
                Vector3D.of(0, 0, 0.5),
                Vector3D.of(0, 0, -0.5),

                Vector3D.of(0.5, 0.5, 0.5),
                Vector3D.of(0.5, 0.5, -0.5),
                Vector3D.of(0.5, -0.5, 0.5),
                Vector3D.of(0.5, -0.5, -0.5),
                Vector3D.of(-0.5, 0.5, 0.5),
                Vector3D.of(-0.5, 0.5, -0.5),
                Vector3D.of(-0.5, -0.5, 0.5),
                Vector3D.of(-0.5, -0.5, -0.5));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(0, 0, 0),

                Vector3D.of(0.4, 0.4, 0.4),
                Vector3D.of(0.4, 0.4, -0.4),
                Vector3D.of(0.4, -0.4, 0.4),
                Vector3D.of(0.4, -0.4, -0.4),
                Vector3D.of(-0.4, 0.4, 0.4),
                Vector3D.of(-0.4, 0.4, -0.4),
                Vector3D.of(-0.4, -0.4, 0.4),
                Vector3D.of(-0.4, -0.4, -0.4));
    }

    @Test
    public void testTwoBoxes_disjoint() {
        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5)));
        tree.union(createRect(Vector3D.of(1.5, -0.5, -0.5), Vector3D.of(2.5, 0.5, 0.5)));

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(12.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), tree.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(3, 0, 0));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(0, 0, 0),
                Vector3D.of(2, 0, 0));
    }

    @Test
    public void testTwoBoxes_sharedSide() {
        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5)));
        tree.union(createRect(Vector3D.of(0.5, -0.5, -0.5), Vector3D.of(1.5, 0.5, 0.5)));

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(10.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0, 0), tree.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(2, 0, 0));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(0, 0, 0),
                Vector3D.of(1, 0, 0));
    }

    @Test
    public void testTwoBoxes_separationLessThanTolerance() {
        // arrange
        final double eps = 1e-6;
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5), precision));
        tree.union(createRect(Vector3D.of(0.5 + 1e-7, -0.5, -0.5), Vector3D.of(1.5 + 1e-7, 0.5, 0.5), precision));

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(2.0, tree.getSize(), eps);
        Assertions.assertEquals(10.0, tree.getBoundarySize(), eps);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5 + 5.4166e-8, 0, 0), tree.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(2, 0, 0));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(0, 0, 0),
                Vector3D.of(1, 0, 0));
    }

    @Test
    public void testTwoBoxes_sharedEdge() {
        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5)));
        tree.union(createRect(Vector3D.of(0.5, 0.5, -0.5), Vector3D.of(1.5, 1.5, 0.5)));

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(12.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0), tree.getCentroid(), TEST_EPS);


        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 0),
                Vector3D.of(2, 1, 0));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(0, 0, 0),
                Vector3D.of(1, 1, 0));
    }

    @Test
    public void testTwoBoxes_sharedPoint() {
        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5)));
        tree.union(createRect(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(1.5, 1.5, 1.5)));

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(12.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 1),
                Vector3D.of(2, 1, 1));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(0, 0, 0),
                Vector3D.of(1, 1, 1));
    }

    @Test
    public void testTetrahedron() {
        // arrange
        final Vector3D vertex1 = Vector3D.of(1, 2, 3);
        final Vector3D vertex2 = Vector3D.of(2, 2, 4);
        final Vector3D vertex3 = Vector3D.of(2, 3, 3);
        final Vector3D vertex4 = Vector3D.of(1, 3, 4);

        final List<PlaneConvexSubset> boundaries = Arrays.asList(
                Planes.convexPolygonFromVertices(Arrays.asList(vertex3, vertex2, vertex1), TEST_PRECISION),
                Planes.convexPolygonFromVertices(Arrays.asList(vertex2, vertex3, vertex4), TEST_PRECISION),
                Planes.convexPolygonFromVertices(Arrays.asList(vertex4, vertex3, vertex1), TEST_PRECISION),
                Planes.convexPolygonFromVertices(Arrays.asList(vertex1, vertex2, vertex4), TEST_PRECISION)
            );

        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.full();
        tree.insert(boundaries);

        // assert
        Assertions.assertEquals(1.0 / 3.0, tree.getSize(), TEST_EPS);
        Assertions.assertEquals(2.0 * Math.sqrt(3.0), tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 2.5, 3.5), tree.getCentroid(), TEST_EPS);

        final double third = 1.0 / 3.0;
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY,
            vertex1, vertex2, vertex3, vertex4,
            Vector3D.linearCombination(third, vertex1, third, vertex2, third, vertex3),
            Vector3D.linearCombination(third, vertex2, third, vertex3, third, vertex4),
            Vector3D.linearCombination(third, vertex3, third, vertex4, third, vertex1),
            Vector3D.linearCombination(third, vertex4, third, vertex1, third, vertex2)
        );
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
            Vector3D.of(1, 2, 4),
            Vector3D.of(2, 2, 3),
            Vector3D.of(2, 3, 4),
            Vector3D.of(1, 3, 3)
        );
    }

    @Test
    public void testSphere() {
        // arrange
        // (use a high tolerance value here since the sphere is only an approximation)
        final double approximationTolerance = 0.2;
        final double radius = 1.0;

        // act
        final RegionBSPTree3D tree = createSphere(Vector3D.of(1, 2, 3), radius, 8, 16);

        // assert
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertFalse(tree.isFull());

        Assertions.assertEquals(sphereVolume(radius), tree.getSize(), approximationTolerance);
        Assertions.assertEquals(sphereSurface(radius), tree.getBoundarySize(), approximationTolerance);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3), tree.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 2, 3),
                Vector3D.of(2.1, 2, 3),
                Vector3D.of(1, 0.9, 3),
                Vector3D.of(1, 3.1, 3),
                Vector3D.of(1, 2, 1.9),
                Vector3D.of(1, 2, 4.1),
                Vector3D.of(1.6, 2.6, 3.6));

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(1, 2, 3),
                Vector3D.of(0.1, 2, 3),
                Vector3D.of(1.9, 2, 3),
                Vector3D.of(1, 2.1, 3),
                Vector3D.of(1, 2.9, 3),
                Vector3D.of(1, 2, 2.1),
                Vector3D.of(1, 2, 3.9),
                Vector3D.of(1.5, 2.5, 3.5));
    }

    @Test
    public void testProjectToBoundary() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act/assert
        checkProject(tree, Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(0, 0.5, 0.5));
        checkProject(tree, Vector3D.of(0.4, 0.5, 0.5), Vector3D.of(0, 0.5, 0.5));
        checkProject(tree, Vector3D.of(1.5, 0.5, 0.5), Vector3D.of(1, 0.5, 0.5));
        checkProject(tree, Vector3D.of(2, 2, 2), Vector3D.of(1, 1, 1));
    }

    @Test
    public void testProjectToBoundary_invertedRegion() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        tree.complement();

        // act/assert
        checkProject(tree, Vector3D.of(0.4, 0.5, 0.5), Vector3D.of(0, 0.5, 0.5));
        checkProject(tree, Vector3D.of(1.5, 0.5, 0.5), Vector3D.of(1, 0.5, 0.5));
        checkProject(tree, Vector3D.of(2, 2, 2), Vector3D.of(1, 1, 1));
    }

    private void checkProject(final RegionBSPTree3D tree, final Vector3D toProject, final Vector3D expectedPoint) {
        final Vector3D proj = tree.project(toProject);

        EuclideanTestUtils.assertCoordinatesEqual(expectedPoint, proj, TEST_EPS);
    }

    @Test
    public void testBoolean_union() throws IOException {
        // arrange
        final double tolerance = 0.05;
        final double size = 1.0;
        final double radius = size * 0.5;
        final RegionBSPTree3D box = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        final RegionBSPTree3D sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.union(box, sphere);

        // assert
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertFalse(result.isFull());

        Assertions.assertEquals(cubeVolume(size) + (sphereVolume(radius) * 0.5),
                result.getSize(), tolerance);
        Assertions.assertEquals(cubeSurface(size) - circleSurface(radius) + (0.5 * sphereSurface(radius)),
                result.getBoundarySize(), tolerance);

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 0.5, 0.5),
                Vector3D.of(1.1, 0.5, 0.5),
                Vector3D.of(0.5, -0.1, 0.5),
                Vector3D.of(0.5, 1.1, 0.5),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 1.6));

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.INSIDE,
                Vector3D.of(0.1, 0.5, 0.5),
                Vector3D.of(0.9, 0.5, 0.5),
                Vector3D.of(0.5, 0.1, 0.5),
                Vector3D.of(0.5, 0.9, 0.5),
                Vector3D.of(0.5, 0.5, 0.1),
                Vector3D.of(0.5, 0.5, 1.4));
    }

    @Test
    public void testUnion_self() {
        // arrange
        final double tolerance = 0.2;
        final double radius = 1.0;

        final RegionBSPTree3D sphere = createSphere(Vector3D.ZERO, radius, 8, 16);

        final RegionBSPTree3D copy = RegionBSPTree3D.empty();
        copy.copy(sphere);

        // act
        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.union(sphere, copy);

        // assert
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertFalse(result.isFull());

        Assertions.assertEquals(sphereVolume(radius), result.getSize(), tolerance);
        Assertions.assertEquals(sphereSurface(radius), result.getBoundarySize(), tolerance);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, result.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.OUTSIDE,
                Vector3D.of(-1.1, 0, 0),
                Vector3D.of(1.1, 0, 0),
                Vector3D.of(0, -1.1, 0),
                Vector3D.of(0, 1.1, 0),
                Vector3D.of(0, 0, -1.1),
                Vector3D.of(0, 0, 1.1));

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.INSIDE,
                Vector3D.of(-0.9, 0, 0),
                Vector3D.of(0.9, 0, 0),
                Vector3D.of(0, -0.9, 0),
                Vector3D.of(0, 0.9, 0),
                Vector3D.of(0, 0, -0.9),
                Vector3D.of(0, 0, 0.9),
                Vector3D.ZERO);
    }

    @Test
    public void testBoolean_intersection() throws IOException {
        // arrange
        final double tolerance = 0.05;
        final double size = 1.0;
        final double radius = size * 0.5;
        final RegionBSPTree3D box = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        final RegionBSPTree3D sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.intersection(box, sphere);

        // assert
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertFalse(result.isFull());

        Assertions.assertEquals(sphereVolume(radius) * 0.5, result.getSize(), tolerance);
        Assertions.assertEquals(circleSurface(radius) + (0.5 * sphereSurface(radius)),
                result.getBoundarySize(), tolerance);

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 0.5, 1.0),
                Vector3D.of(1.1, 0.5, 1.0),
                Vector3D.of(0.5, -0.1, 1.0),
                Vector3D.of(0.5, 1.1, 1.0),
                Vector3D.of(0.5, 0.5, 0.4),
                Vector3D.of(0.5, 0.5, 1.1));

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.INSIDE,
                Vector3D.of(0.1, 0.5, 0.9),
                Vector3D.of(0.9, 0.5, 0.9),
                Vector3D.of(0.5, 0.1, 0.9),
                Vector3D.of(0.5, 0.9, 0.9),
                Vector3D.of(0.5, 0.5, 0.6),
                Vector3D.of(0.5, 0.5, 0.9));
    }

    @Test
    public void testIntersection_self() {
        // arrange
        final double tolerance = 0.2;
        final double radius = 1.0;

        final RegionBSPTree3D sphere = createSphere(Vector3D.ZERO, radius, 8, 16);
        final RegionBSPTree3D copy = RegionBSPTree3D.empty();
        copy.copy(sphere);

        // act
        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.intersection(sphere, copy);

        // assert
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertFalse(result.isFull());

        Assertions.assertEquals(sphereVolume(radius), result.getSize(), tolerance);
        Assertions.assertEquals(sphereSurface(radius), result.getBoundarySize(), tolerance);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, result.getCentroid(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.OUTSIDE,
                Vector3D.of(-1.1, 0, 0),
                Vector3D.of(1.1, 0, 0),
                Vector3D.of(0, -1.1, 0),
                Vector3D.of(0, 1.1, 0),
                Vector3D.of(0, 0, -1.1),
                Vector3D.of(0, 0, 1.1));

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.INSIDE,
                Vector3D.of(-0.9, 0, 0),
                Vector3D.of(0.9, 0, 0),
                Vector3D.of(0, -0.9, 0),
                Vector3D.of(0, 0.9, 0),
                Vector3D.of(0, 0, -0.9),
                Vector3D.of(0, 0, 0.9),
                Vector3D.ZERO);
    }

    @Test
    public void testBoolean_xor_twoCubes() throws IOException {
        // arrange
        final double size = 1.0;
        final RegionBSPTree3D box1 = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        final RegionBSPTree3D box2 = createRect(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(0.5 + size, 0.5 + size, 0.5 + size));

        // act
        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.xor(box1, box2);

        // assert
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertFalse(result.isFull());

        Assertions.assertEquals((2 * cubeVolume(size)) - (2 * cubeVolume(size * 0.5)), result.getSize(), TEST_EPS);
        Assertions.assertEquals(2 * cubeSurface(size), result.getBoundarySize(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, -0.1, -0.1),
                Vector3D.of(0.75, 0.75, 0.75),
                Vector3D.of(1.6, 1.6, 1.6));

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.BOUNDARY,
                Vector3D.of(0, 0, 0),
                Vector3D.of(0.5, 0.5, 0.5),
                Vector3D.of(1, 1, 1),
                Vector3D.of(1.5, 1.5, 1.5));

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.INSIDE,
                Vector3D.of(0.1, 0.1, 0.1),
                Vector3D.of(0.4, 0.4, 0.4),
                Vector3D.of(1.1, 1.1, 1.1),
                Vector3D.of(1.4, 1.4, 1.4));
    }

    @Test
    public void testBoolean_xor_cubeAndSphere() throws IOException {
        // arrange
        final double tolerance = 0.05;
        final double size = 1.0;
        final double radius = size * 0.5;
        final RegionBSPTree3D box = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        final RegionBSPTree3D sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.xor(box, sphere);

        // assert
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertFalse(result.isFull());

        Assertions.assertEquals(cubeVolume(size), result.getSize(), tolerance);
        Assertions.assertEquals(cubeSurface(size) + (sphereSurface(radius)),
                result.getBoundarySize(), tolerance);

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 0.5, 0.5),
                Vector3D.of(1.1, 0.5, 0.5),
                Vector3D.of(0.5, -0.1, 0.5),
                Vector3D.of(0.5, 1.1, 0.5),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 1.6),
                Vector3D.of(0.5, 0.5, 0.9));

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.INSIDE,
                Vector3D.of(0.1, 0.5, 0.5),
                Vector3D.of(0.9, 0.5, 0.5),
                Vector3D.of(0.5, 0.1, 0.5),
                Vector3D.of(0.5, 0.9, 0.5),
                Vector3D.of(0.5, 0.5, 0.1),
                Vector3D.of(0.5, 0.5, 1.4));
    }

    @Test
    public void testXor_self() {
        // arrange
        final double radius = 1.0;

        final RegionBSPTree3D sphere = createSphere(Vector3D.ZERO, radius, 8, 16);
        final RegionBSPTree3D copy = RegionBSPTree3D.empty();
        copy.copy(sphere);

        // act
        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.xor(sphere, copy);

        // assert
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertFalse(result.isFull());

        Assertions.assertEquals(0.0, result.getSize(), TEST_EPS);
        Assertions.assertEquals(0.0, result.getBoundarySize(), TEST_EPS);
        Assertions.assertNull(result.getCentroid());

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.OUTSIDE,
                Vector3D.of(-1.1, 0, 0),
                Vector3D.of(1.1, 0, 0),
                Vector3D.of(0, -1.1, 0),
                Vector3D.of(0, 1.1, 0),
                Vector3D.of(0, 0, -1.1),
                Vector3D.of(0, 0, 1.1),
                Vector3D.of(-0.9, 0, 0),
                Vector3D.of(0.9, 0, 0),
                Vector3D.of(0, -0.9, 0),
                Vector3D.of(0, 0.9, 0),
                Vector3D.of(0, 0, -0.9),
                Vector3D.of(0, 0, 0.9),
                Vector3D.ZERO);
    }

    @Test
    public void testBoolean_difference() throws IOException {
        // arrange
        final double tolerance = 0.05;
        final double size = 1.0;
        final double radius = size * 0.5;
        final RegionBSPTree3D box = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        final RegionBSPTree3D sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.difference(box, sphere);

        // assert
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertFalse(result.isFull());

        Assertions.assertEquals(cubeVolume(size) - (sphereVolume(radius) * 0.5), result.getSize(), tolerance);
        Assertions.assertEquals(cubeSurface(size) - circleSurface(radius) + (0.5 * sphereSurface(radius)),
                result.getBoundarySize(), tolerance);

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 0.5, 1.0),
                Vector3D.of(1.1, 0.5, 1.0),
                Vector3D.of(0.5, -0.1, 1.0),
                Vector3D.of(0.5, 1.1, 1.0),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 0.6));

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.INSIDE,
                Vector3D.of(0.1, 0.5, 0.4),
                Vector3D.of(0.9, 0.5, 0.4),
                Vector3D.of(0.5, 0.1, 0.4),
                Vector3D.of(0.5, 0.9, 0.4),
                Vector3D.of(0.5, 0.5, 0.1),
                Vector3D.of(0.5, 0.5, 0.4));
    }

    @Test
    public void testDifference_self() {
        // arrange
        final double radius = 1.0;

        final RegionBSPTree3D sphere = createSphere(Vector3D.ZERO, radius, 8, 16);
        final RegionBSPTree3D copy = sphere.copy();

        // act
        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.difference(sphere, copy);

        // assert
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertFalse(result.isFull());

        Assertions.assertEquals(0.0, result.getSize(), TEST_EPS);
        Assertions.assertEquals(0.0, result.getBoundarySize(), TEST_EPS);
        Assertions.assertNull(result.getCentroid());

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.OUTSIDE,
                Vector3D.of(-1.1, 0, 0),
                Vector3D.of(1.1, 0, 0),
                Vector3D.of(0, -1.1, 0),
                Vector3D.of(0, 1.1, 0),
                Vector3D.of(0, 0, -1.1),
                Vector3D.of(0, 0, 1.1),
                Vector3D.of(-0.9, 0, 0),
                Vector3D.of(0.9, 0, 0),
                Vector3D.of(0, -0.9, 0),
                Vector3D.of(0, 0.9, 0),
                Vector3D.of(0, 0, -0.9),
                Vector3D.of(0, 0, 0.9),
                Vector3D.ZERO);
    }

    @Test
    public void testBoolean_multiple() throws IOException {
        // arrange
        final double tolerance = 0.05;
        final double size = 1.0;
        final double radius = size * 0.5;
        final RegionBSPTree3D box = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        final RegionBSPTree3D sphereToAdd = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);
        final RegionBSPTree3D sphereToRemove1 = createSphere(Vector3D.of(size * 0.5, 0, size * 0.5), radius, 8, 16);
        final RegionBSPTree3D sphereToRemove2 = createSphere(Vector3D.of(size * 0.5, 1, size * 0.5), radius, 8, 16);

        // act
        final RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.union(box, sphereToAdd);
        result.difference(sphereToRemove1);
        result.difference(sphereToRemove2);

        // assert
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertFalse(result.isFull());

        Assertions.assertEquals(cubeVolume(size) - (sphereVolume(radius) * 0.5),
                result.getSize(), tolerance);
        Assertions.assertEquals(cubeSurface(size) - (3.0 * circleSurface(radius)) + (1.5 * sphereSurface(radius)),
                result.getBoundarySize(), tolerance);

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 0.5, 0.5),
                Vector3D.of(1.1, 0.5, 0.5),
                Vector3D.of(0.5, 0.4, 0.5),
                Vector3D.of(0.5, 0.6, 0.5),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 1.6));

        EuclideanTestUtils.assertRegionLocation(result, RegionLocation.INSIDE,
                Vector3D.of(0.1, 0.5, 0.1),
                Vector3D.of(0.9, 0.5, 0.1),
                Vector3D.of(0.5, 0.4, 0.1),
                Vector3D.of(0.5, 0.6, 0.1),
                Vector3D.of(0.5, 0.5, 0.1),
                Vector3D.of(0.5, 0.5, 1.4));
    }

    @Test
    public void testToConvex_empty() {
        // act
        final List<ConvexVolume> result = RegionBSPTree3D.empty().toConvex();

        // assert
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testToConvex_singleBox() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.of(1, 2, 3), Vector3D.of(2, 3, 4));

        // act
        final List<ConvexVolume> result = tree.toConvex();

        // assert
        Assertions.assertEquals(1, result.size());

        final ConvexVolume vol = result.get(0);
        Assertions.assertEquals(1, vol.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 2.5, 3.5), vol.getCentroid(), TEST_EPS);
    }

    @Test
    public void testToConvex_multipleBoxes() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.of(4, 5, 6), Vector3D.of(5, 6, 7));
        tree.union(createRect(Vector3D.ZERO, Vector3D.of(2, 1, 1)));

        // act
        final List<ConvexVolume> result = tree.toConvex();

        // assert
        Assertions.assertEquals(2, result.size());

        final boolean smallFirst = result.get(0).getSize() < result.get(1).getSize();

        final ConvexVolume small = smallFirst ? result.get(0) : result.get(1);
        final ConvexVolume large = smallFirst ? result.get(1) : result.get(0);

        Assertions.assertEquals(1, small.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4.5, 5.5, 6.5), small.getCentroid(), TEST_EPS);

        Assertions.assertEquals(2, large.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0.5, 0.5), large.getCentroid(), TEST_EPS);
    }

    @Test
    public void testSplit() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5));

        final Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final Split<RegionBSPTree3D> split = tree.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final RegionBSPTree3D minus = split.getMinus();
        Assertions.assertEquals(0.5, minus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.25, 0, 0), minus.getCentroid(), TEST_EPS);

        final RegionBSPTree3D plus = split.getPlus();
        Assertions.assertEquals(0.5, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.25, 0, 0), plus.getCentroid(), TEST_EPS);
    }

    @Test
    public void testGetNodeRegion() {
        // arrange
        final RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act/assert
        final ConvexVolume rootVol = tree.getRoot().getNodeRegion();
        GeometryTestUtils.assertPositiveInfinity(rootVol.getSize());
        Assertions.assertNull(rootVol.getCentroid());

        final ConvexVolume plusVol = tree.getRoot().getPlus().getNodeRegion();
        GeometryTestUtils.assertPositiveInfinity(plusVol.getSize());
        Assertions.assertNull(plusVol.getCentroid());

        final ConvexVolume centerVol = tree.findNode(Vector3D.of(0.5, 0.5, 0.5)).getNodeRegion();
        Assertions.assertEquals(1, centerVol.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), centerVol.getCentroid(), TEST_EPS);
    }

    // GEOMETRY-59
    @Test
    public void testSlightlyConcavePrism() {
        // arrange
        final Vector3D[] vertices = {
            Vector3D.of(0, 0, 0),
            Vector3D.of(2, 1e-7, 0),
            Vector3D.of(4, 0, 0),
            Vector3D.of(2, 2, 0),
            Vector3D.of(0, 0, 2),
            Vector3D.of(2, 1e-7, 2),
            Vector3D.of(4, 0, 2),
            Vector3D.of(2, 2, 2)
        };

        final int[][] facets = {
            {4, 5, 6, 7},
            {3, 2, 1, 0},
            {0, 1, 5, 4},
            {1, 2, 6, 5},
            {2, 3, 7, 6},
            {3, 0, 4, 7}
        };

        final List<PlaneConvexSubset> faces = indexedFacetsToBoundaries(vertices, facets);

        // act
        final RegionBSPTree3D tree = RegionBSPTree3D.full();
        tree.insert(faces);

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE, Vector3D.of(2, 1, 1));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(2, 1, 3), Vector3D.of(2, 1, -3),
                Vector3D.of(2, -1, 1), Vector3D.of(2, 3, 1),
                Vector3D.of(-1, 1, 1), Vector3D.of(4, 1, 1));
    }

    private static List<PlaneConvexSubset> indexedFacetsToBoundaries(final Vector3D[] vertices, final int[][] facets) {
        final List<PlaneConvexSubset> boundaries = new ArrayList<>();

        final List<Vector3D> vertexList = new ArrayList<>();

        for (final int[] indices : facets) {
            for (final int index : indices) {
                vertexList.add(vertices[index]);
            }

            // insert into an embedded tree and convert to convex polygons so that we can support
            // non-convex facet boundaries
            final EmbeddingPlane plane = Planes.fromPoints(vertexList, TEST_PRECISION).getEmbedding();

            final LinePath subPath = LinePath.builder(TEST_PRECISION)
                    .appendVertices(plane.toSubspace(vertexList))
                    .close();
            final EmbeddedTreePlaneSubset subset = new EmbeddedTreePlaneSubset(plane, subPath.toTree());

            boundaries.addAll(subset.toConvex());

            vertexList.clear();
        }

        return boundaries;
    }

    private static RegionBSPTree3D createRect(final Vector3D a, final Vector3D b) {
        return createRect(a, b, TEST_PRECISION);
    }

    private static RegionBSPTree3D createRect(final Vector3D a, final Vector3D b, final DoublePrecisionContext precision) {
        return Parallelepiped.axisAligned(a, b, precision).toTree();
    }

    private static RegionBSPTree3D createSphere(final Vector3D center, final double radius, final int stacks, final int slices) {

        final List<Plane> planes = new ArrayList<>();

        // add top and bottom planes (+/- z)
        final Vector3D topZ = Vector3D.of(center.getX(), center.getY(), center.getZ() + radius);
        final Vector3D bottomZ = Vector3D.of(center.getX(), center.getY(), center.getZ() - radius);

        planes.add(Planes.fromPointAndNormal(topZ, Vector3D.Unit.PLUS_Z, TEST_PRECISION));
        planes.add(Planes.fromPointAndNormal(bottomZ, Vector3D.Unit.MINUS_Z, TEST_PRECISION));

        // add the side planes
        final double vDelta = PlaneAngleRadians.PI / stacks;
        final double hDelta = PlaneAngleRadians.PI * 2 / slices;

        final double adjustedRadius = (radius + (radius * Math.cos(vDelta * 0.5))) / 2.0;

        double vAngle;
        double hAngle;
        double stackRadius;
        double stackHeight;
        double x;
        double y;
        Vector3D pt;
        Vector3D norm;

        vAngle = -0.5 * vDelta;
        for (int v = 0; v < stacks; ++v) {
            vAngle += vDelta;

            stackRadius = Math.sin(vAngle) * adjustedRadius;
            stackHeight = Math.cos(vAngle) * adjustedRadius;

            hAngle = -0.5 * hDelta;
            for (int h = 0; h < slices; ++h) {
                hAngle += hDelta;

                x = Math.cos(hAngle) * stackRadius;
                y = Math.sin(hAngle) * stackRadius;

                norm = Vector3D.of(x, y, stackHeight).normalize();
                pt = center.add(norm.multiply(adjustedRadius));

                planes.add(Planes.fromPointAndNormal(pt, norm, TEST_PRECISION));
            }
        }

        final RegionBSPTree3D tree = RegionBSPTree3D.full();
        RegionNode3D node = tree.getRoot();

        for (final Plane plane : planes) {
            node = node.cut(plane).getMinus();
        }

        return tree;
    }

    private static double cubeVolume(final double size) {
        return size * size * size;
    }

    private static double cubeSurface(final double size) {
        return 6.0 * size * size;
    }

    private static double sphereVolume(final double radius) {
        return 4.0 * Math.PI * radius * radius * radius / 3.0;
    }

    private static double sphereSurface(final double radius) {
        return 4.0 * Math.PI * radius * radius;
    }

    private static double circleSurface(final double radius) {
        return Math.PI * radius * radius;
    }
}
