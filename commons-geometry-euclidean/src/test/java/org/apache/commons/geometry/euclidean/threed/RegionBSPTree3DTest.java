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
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D.RegionNode3D;
import org.apache.commons.geometry.euclidean.threed.shapes.Parallelepiped;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class RegionBSPTree3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testCtor_default() {
        // act
        RegionBSPTree3D tree = new RegionBSPTree3D();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
    }

    @Test
    public void testCtor_boolean() {
        // act
        RegionBSPTree3D a = new RegionBSPTree3D(true);
        RegionBSPTree3D b = new RegionBSPTree3D(false);

        // assert
        Assert.assertTrue(a.isFull());
        Assert.assertFalse(a.isEmpty());

        Assert.assertFalse(b.isFull());
        Assert.assertTrue(b.isEmpty());
    }

    @Test
    public void testEmpty() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.empty();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());

        Assert.assertNull(tree.getBarycenter());
        Assert.assertEquals(0.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

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
        RegionBSPTree3D tree = RegionBSPTree3D.full();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertNull(tree.getBarycenter());
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertEquals(0, tree.getBoundarySize(), TEST_EPS);

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                Vector3D.of(-100, -100, -100),
                Vector3D.of(0, 0, 0),
                Vector3D.of(100, 100, 100),
                Vector3D.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    public void testCopy() {
        // arrange
        RegionBSPTree3D tree = new RegionBSPTree3D(true);
        tree.getRoot().cut(Plane.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION));

        // act
        RegionBSPTree3D copy = tree.copy();

        // assert
        Assert.assertNotSame(tree, copy);
        Assert.assertEquals(3, copy.count());
    }

    @Test
    public void testBoundaries() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act
        List<ConvexSubPlane> facets = new ArrayList<>();
        tree.boundaries().forEach(facets::add);

        // assert
        Assert.assertEquals(6, facets.size());
    }

    @Test
    public void testGetBoundaries() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act
        List<ConvexSubPlane> facets = tree.getBoundaries();

        // assert
        Assert.assertEquals(6, facets.size());
    }

    @Test
    public void testBoundaryStream() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act
        List<ConvexSubPlane> facets = tree.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(6, facets.size());
    }

    @Test
    public void testBoundaryStream_noBoundaries() {
        // arrange
        RegionBSPTree3D tree = RegionBSPTree3D.full();

        // act
        List<ConvexSubPlane> facets = tree.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(0, facets.size());
    }

    @Test
    public void testToTree_returnsSameInstance() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 2, 1));

        // act/assert
        Assert.assertSame(tree, tree.toTree());
    }

    @Test
    public void testHalfSpace() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.insert(Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, TEST_PRECISION).span());

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        EuclideanTestUtils.assertPositiveInfinity(tree.getSize());
        EuclideanTestUtils.assertPositiveInfinity(tree.getBoundarySize());
        Assert.assertNull(tree.getBarycenter());

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
        RegionBSPTree3D tree = RegionBSPTree3D.empty();

        Vector3D min = Vector3D.ZERO;
        Vector3D max = Vector3D.of(1, 1, 1);

        Plane top = Plane.fromPointAndNormal(max, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Plane bottom = Plane.fromPointAndNormal(min, Vector3D.Unit.MINUS_Z, TEST_PRECISION);
        Plane left = Plane.fromPointAndNormal(min, Vector3D.Unit.MINUS_X, TEST_PRECISION);
        Plane right = Plane.fromPointAndNormal(max, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        Plane front = Plane.fromPointAndNormal(min, Vector3D.Unit.MINUS_Y, TEST_PRECISION);
        Plane back = Plane.fromPointAndNormal(max, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        Plane diag = Plane.fromPointAndNormal(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(0.5, -0.5, 0), TEST_PRECISION);
        Plane midCut = Plane.fromPointAndNormal(Vector3D.of(0.5, 0.5, 0.5), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

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
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(1, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getBarycenter(), TEST_EPS);

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
        RegionBSPTree3D tree = RegionBSPTree3D.from(Arrays.asList(
                    ConvexSubPlane.fromVertexLoop(Arrays.asList(
                            Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION),
                    ConvexSubPlane.fromVertexLoop(Arrays.asList(
                            Vector3D.ZERO, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_X), TEST_PRECISION)
                ));

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(RegionLocation.OUTSIDE, tree.getRoot().getLocation());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(1, 1, -1), Vector3D.of(-1, 1, -1));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(1, 1, 1), Vector3D.of(-1, 1, 1), Vector3D.of(1, -1, 1),
                Vector3D.of(-1, -1, 1), Vector3D.of(1, -1, -1), Vector3D.of(-1, -1, -1));
    }

    @Test
    public void testFrom_boundaries_fullIsTrue() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.from(Arrays.asList(
                    ConvexSubPlane.fromVertexLoop(Arrays.asList(
                            Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION),
                    ConvexSubPlane.fromVertexLoop(Arrays.asList(
                            Vector3D.ZERO, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_X), TEST_PRECISION)
                ), true);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(RegionLocation.INSIDE, tree.getRoot().getLocation());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE,
                Vector3D.of(1, 1, -1), Vector3D.of(-1, 1, -1));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(1, 1, 1), Vector3D.of(-1, 1, 1), Vector3D.of(1, -1, 1),
                Vector3D.of(-1, -1, 1), Vector3D.of(1, -1, -1), Vector3D.of(-1, -1, -1));
    }

    @Test
    public void testFrom_boundaries_noBoundaries() {
        // act/assert
        Assert.assertTrue(RegionBSPTree3D.from(Arrays.asList()).isEmpty());
        Assert.assertTrue(RegionBSPTree3D.from(Arrays.asList(), true).isFull());
        Assert.assertTrue(RegionBSPTree3D.from(Arrays.asList(), false).isEmpty());
    }

    @Test
    public void testFromConvexVolume_full() {
        // arrange
        ConvexVolume volume = ConvexVolume.full();

        // act
        RegionBSPTree3D tree = volume.toTree();
        Assert.assertNull(tree.getBarycenter());

        // assert
        Assert.assertTrue(tree.isFull());
    }

    @Test
    public void testFromConvexVolume_infinite() {
        // arrange
        ConvexVolume volume = ConvexVolume.fromBounds(Plane.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION));

        // act
        RegionBSPTree3D tree = volume.toTree();

        // assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());
        Assert.assertNull(tree.getBarycenter());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE, Vector3D.of(0, 0, 1));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.BOUNDARY, Vector3D.ZERO);
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE, Vector3D.of(0, 0, -1));
    }

    @Test
    public void testFromConvexVolume_finite() {
        // arrange
        ConvexVolume volume = ConvexVolume.fromBounds(
                    Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_X, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Y, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Z, TEST_PRECISION),

                    Plane.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION)
                );

        // act
        RegionBSPTree3D tree = volume.toTree();

        // assert
        Assert.assertEquals(1, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getBarycenter(), TEST_EPS);

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
        RegionBSPTree3D tree = RegionBSPTree3D.empty();

        // act/assert
        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Line3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION));

        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Segment3D.fromPoints(Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast_full() {
        // arrange
        RegionBSPTree3D tree = RegionBSPTree3D.full();

        // act/assert
        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Line3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION));

        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Segment3D.fromPoints(Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act/assert
        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Line3D.fromPoints(Vector3D.of(0, 5, 5), Vector3D.of(1, 6, 6), TEST_PRECISION));

        Vector3D corner = Vector3D.of(1, 1, 1);

        LinecastChecker3D.with(tree)
            .expect(Vector3D.ZERO, Vector3D.Unit.MINUS_X)
            .and(Vector3D.ZERO, Vector3D.Unit.MINUS_Y)
            .and(Vector3D.ZERO, Vector3D.Unit.MINUS_Z)
            .and(corner, Vector3D.Unit.PLUS_Z)
            .and(corner, Vector3D.Unit.PLUS_Y)
            .and(corner, Vector3D.Unit.PLUS_X)
            .whenGiven(Line3D.fromPoints(Vector3D.ZERO, corner, TEST_PRECISION));

        LinecastChecker3D.with(tree)
            .expect(corner, Vector3D.Unit.PLUS_Z)
            .and(corner, Vector3D.Unit.PLUS_Y)
            .and(corner, Vector3D.Unit.PLUS_X)
            .whenGiven(Segment3D.fromPoints(Vector3D.of(0.5, 0.5, 0.5), corner, TEST_PRECISION));
    }

    @Test
    public void testLinecast_complementedTree() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        tree.complement();

        // act/assert
        LinecastChecker3D.with(tree)
            .expectNothing()
            .whenGiven(Line3D.fromPoints(Vector3D.of(0, 5, 5), Vector3D.of(1, 6, 6), TEST_PRECISION));

        Vector3D corner = Vector3D.of(1, 1, 1);

        LinecastChecker3D.with(tree)
            .expect(Vector3D.ZERO, Vector3D.Unit.PLUS_Z)
            .and(Vector3D.ZERO, Vector3D.Unit.PLUS_Y)
            .and(Vector3D.ZERO, Vector3D.Unit.PLUS_X)
            .and(corner, Vector3D.Unit.MINUS_X)
            .and(corner, Vector3D.Unit.MINUS_Y)
            .and(corner, Vector3D.Unit.MINUS_Z)
            .whenGiven(Line3D.fromPoints(Vector3D.ZERO, corner, TEST_PRECISION));

        LinecastChecker3D.with(tree)
            .expect(corner, Vector3D.Unit.MINUS_X)
            .and(corner, Vector3D.Unit.MINUS_Y)
            .and(corner, Vector3D.Unit.MINUS_Z)
            .whenGiven(Segment3D.fromPoints(Vector3D.of(0.5, 0.5, 0.5), corner, TEST_PRECISION));
    }

    @Test
    public void testLinecast_complexRegion() {
        // arrange
        RegionBSPTree3D a = RegionBSPTree3D.empty();
        Parallelepiped.axisAligned(Vector3D.ZERO, Vector3D.of(0.5, 1, 1), TEST_PRECISION).boundaryStream()
            .map(ConvexSubPlane::reverse)
            .forEach(a::insert);
        a.complement();

        RegionBSPTree3D b = RegionBSPTree3D.empty();
        Parallelepiped.axisAligned(Vector3D.of(0.5, 0, 0), Vector3D.of(1, 1, 1), TEST_PRECISION).boundaryStream()
            .map(ConvexSubPlane::reverse)
            .forEach(b::insert);
        b.complement();

        RegionBSPTree3D c = createRect(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(1.5, 1.5, 1.5));

        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(a, b);
        tree.union(c);

        // act/assert
        Vector3D corner = Vector3D.of(1.5, 1.5, 1.5);

        LinecastChecker3D.with(tree)
            .expect(corner, Vector3D.Unit.PLUS_Z)
            .and(corner, Vector3D.Unit.PLUS_Y)
            .and(corner, Vector3D.Unit.PLUS_X)
            .whenGiven(Segment3D.fromPoints(Vector3D.of(0.25, 0.25, 0.25), Vector3D.of(2, 2, 2), TEST_PRECISION));
    }

    @Test
    public void testLinecast_removesDuplicatePoints() {
        // arrange
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.insert(Plane.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION).span());
        tree.insert(Plane.fromNormal(Vector3D.Unit.PLUS_Y, TEST_PRECISION).span());

        // act/assert
        LinecastChecker3D.with(tree)
            .expect(Vector3D.ZERO, Vector3D.Unit.PLUS_Y)
            .whenGiven(Line3D.fromPoints(Vector3D.of(1, 1, 1), Vector3D.of(-1, -1, -1), TEST_PRECISION));

        LinecastChecker3D.with(tree)
        .expect(Vector3D.ZERO, Vector3D.Unit.PLUS_Y)
            .whenGiven(Segment3D.fromPoints(Vector3D.of(1, 1, 1), Vector3D.of(-1, -1, -1), TEST_PRECISION));
    }

    @Test
    public void testLinecastFirst_multipleDirections() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.of(-1, -1, -1), Vector3D.of(1, 1, 1));

        Line3D xPlus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(1, 0, 0), TEST_PRECISION);
        Line3D xMinus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(-1, 0, 0), TEST_PRECISION);

        Line3D yPlus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, 1, 0), TEST_PRECISION);
        Line3D yMinus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, -1, 0), TEST_PRECISION);

        Line3D zPlus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, 0, 1), TEST_PRECISION);
        Line3D zMinus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, 0, -1), TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0),
                tree.linecastFirst(xPlus.rayFrom(Vector3D.of(-1.1, 0, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0),
                tree.linecastFirst(xPlus.rayFrom(Vector3D.of(-1, 0, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0),
                tree.linecastFirst(xPlus.rayFrom(Vector3D.of(-0.9, 0, 0))).getNormal(), TEST_EPS);
        Assert.assertNull(tree.linecastFirst(xPlus.rayFrom(Vector3D.of(1.1, 0, 0))));

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0),
                tree.linecastFirst(xMinus.rayFrom(Vector3D.of(1.1, 0, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0),
                tree.linecastFirst(xMinus.rayFrom(Vector3D.of(1, 0, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 0),
                tree.linecastFirst(xMinus.rayFrom(Vector3D.of(0.9, 0, 0))).getNormal(), TEST_EPS);
        Assert.assertNull(tree.linecastFirst(xMinus.rayFrom(Vector3D.of(-1.1, 0, 0))));

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -1, 0),
                tree.linecastFirst(yPlus.rayFrom(Vector3D.of(0, -1.1, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -1, 0),
                tree.linecastFirst(yPlus.rayFrom(Vector3D.of(0, -1, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0),
                tree.linecastFirst(yPlus.rayFrom(Vector3D.of(0, -0.9, 0))).getNormal(), TEST_EPS);
        Assert.assertNull(tree.linecastFirst(yPlus.rayFrom(Vector3D.of(0, 1.1, 0))));

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0),
                tree.linecastFirst(yMinus.rayFrom(Vector3D.of(0, 1.1, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 0),
                tree.linecastFirst(yMinus.rayFrom(Vector3D.of(0, 1, 0))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, -1, 0),
                tree.linecastFirst(yMinus.rayFrom(Vector3D.of(0, 0.9, 0))).getNormal(), TEST_EPS);
        Assert.assertNull(tree.linecastFirst(yMinus.rayFrom(Vector3D.of(0, -1.1, 0))));

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1),
                tree.linecastFirst(zPlus.rayFrom(Vector3D.of(0, 0, -1.1))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1),
                tree.linecastFirst(zPlus.rayFrom(Vector3D.of(0, 0, -1))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1),
                tree.linecastFirst(zPlus.rayFrom(Vector3D.of(0, 0, -0.9))).getNormal(), TEST_EPS);
        Assert.assertNull(tree.linecastFirst(zPlus.rayFrom(Vector3D.of(0, 0, 1.1))));

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1),
                tree.linecastFirst(zMinus.rayFrom(Vector3D.of(0, 0, 1.1))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1),
                tree.linecastFirst(zMinus.rayFrom(Vector3D.of(0, 0, 1))).getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1),
                tree.linecastFirst(zMinus.rayFrom(Vector3D.of(0, 0, 0.9))).getNormal(), TEST_EPS);
        Assert.assertNull(tree.linecastFirst(zMinus.rayFrom(Vector3D.of(0, 0, -1.1))));
    }

    // issue GEOMETRY-38
    @Test
    public void testLinecastFirst_linePassesThroughVertex() {
        // arrange
        Vector3D lowerCorner = Vector3D.ZERO;
        Vector3D upperCorner = Vector3D.of(1, 1, 1);
        Vector3D center = lowerCorner.lerp(upperCorner, 0.5);

        RegionBSPTree3D tree = createRect(lowerCorner, upperCorner);

        Line3D upDiagonal = Line3D.fromPoints(lowerCorner, upperCorner, TEST_PRECISION);
        Line3D downDiagonal = upDiagonal.reverse();

        // act/assert
        LinecastPoint3D upFromOutsideResult = tree.linecastFirst(upDiagonal.rayFrom(Vector3D.of(-1, -1, -1)));
        Assert.assertNotNull(upFromOutsideResult);
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, upFromOutsideResult.getPoint(), TEST_EPS);

        LinecastPoint3D upFromCenterResult = tree.linecastFirst(upDiagonal.rayFrom(center));
        Assert.assertNotNull(upFromCenterResult);
        EuclideanTestUtils.assertCoordinatesEqual(upperCorner, upFromCenterResult.getPoint(), TEST_EPS);

        LinecastPoint3D downFromOutsideResult = tree.linecastFirst(downDiagonal.rayFrom(Vector3D.of(2, 2, 2)));
        Assert.assertNotNull(downFromOutsideResult);
        EuclideanTestUtils.assertCoordinatesEqual(upperCorner, downFromOutsideResult.getPoint(), TEST_EPS);

        LinecastPoint3D downFromCenterResult = tree.linecastFirst(downDiagonal.rayFrom(center));
        Assert.assertNotNull(downFromCenterResult);
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, downFromCenterResult.getPoint(), TEST_EPS);
    }

    // Issue GEOMETRY-43
    @Test
    public void testLinecastFirst_lineParallelToFace() {
        // arrange - setup box
        Vector3D lowerCorner = Vector3D.ZERO;
        Vector3D upperCorner = Vector3D.of(1, 1, 1);

        RegionBSPTree3D tree = createRect(lowerCorner, upperCorner);

        Vector3D firstPointOnLine = Vector3D.of(0.5, -1.0, 0);
        Vector3D secondPointOnLine = Vector3D.of(0.5, 2.0, 0);
        Line3D bottomLine = Line3D.fromPoints(firstPointOnLine, secondPointOnLine, TEST_PRECISION);

        Vector3D expectedIntersection1 = Vector3D.of(0.5, 0, 0.0);
        Vector3D expectedIntersection2 = Vector3D.of(0.5, 1.0, 0.0);

        // act/assert
        LinecastPoint3D bottom = tree.linecastFirst(bottomLine.rayFrom(firstPointOnLine));
        Assert.assertNotNull(bottom);
        EuclideanTestUtils.assertCoordinatesEqual(expectedIntersection1, bottom.getPoint(), TEST_EPS);

        bottom = tree.linecastFirst(bottomLine.rayFrom(Vector3D.of(0.5, 0.1, 0.0)));
        Assert.assertNotNull(bottom);
        Vector3D intersection = bottom.getPoint();
        Assert.assertNotNull(intersection);
        EuclideanTestUtils.assertCoordinatesEqual(expectedIntersection2, intersection, TEST_EPS);
    }

    @Test
    public void testLinecastFirst_rayPointOnFace() {
        // arrange
        Vector3D lowerCorner = Vector3D.ZERO;
        Vector3D upperCorner = Vector3D.of(1, 1, 1);

        RegionBSPTree3D tree = createRect(lowerCorner, upperCorner);

        Vector3D pt = Vector3D.of(0.5, 0.5, 0);
        Line3D intoBoxLine = Line3D.fromPoints(pt, pt.add(Vector3D.Unit.PLUS_Z), TEST_PRECISION);
        Line3D outOfBoxLine = Line3D.fromPoints(pt, pt.add(Vector3D.Unit.MINUS_Z), TEST_PRECISION);

        // act/assert
        LinecastPoint3D intoBoxResult = tree.linecastFirst(intoBoxLine.rayFrom(pt));
        EuclideanTestUtils.assertCoordinatesEqual(pt, intoBoxResult.getPoint(), TEST_EPS);

        LinecastPoint3D outOfBoxResult = tree.linecastFirst(outOfBoxLine.rayFrom(pt));
        EuclideanTestUtils.assertCoordinatesEqual(pt, outOfBoxResult.getPoint(), TEST_EPS);
    }

    @Test
    public void testLinecastFirst_rayPointOnVertex() {
        // arrange
        Vector3D lowerCorner = Vector3D.ZERO;
        Vector3D upperCorner = Vector3D.of(1, 1, 1);

        RegionBSPTree3D tree = createRect(lowerCorner, upperCorner);

        Line3D intoBoxLine = Line3D.fromPoints(lowerCorner, upperCorner, TEST_PRECISION);
        Line3D outOfBoxLine = intoBoxLine.reverse();

        // act/assert
        LinecastPoint3D intoBoxResult = tree.linecastFirst(intoBoxLine.rayFrom(lowerCorner));
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, intoBoxResult.getPoint(), TEST_EPS);

        LinecastPoint3D outOfBoxResult = tree.linecastFirst(outOfBoxLine.rayFrom(lowerCorner));
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, outOfBoxResult.getPoint(), TEST_EPS);
    }

    @Test
    public void testLinecastFirst_onlyReturnsPointsWithinSegment() throws IOException, ParseException {
        // arrange
        Vector3D lowerCorner = Vector3D.ZERO;
        Vector3D upperCorner = Vector3D.of(1, 1, 1);

        RegionBSPTree3D tree = createRect(lowerCorner, upperCorner);

        Line3D line = Line3D.fromPointAndDirection(Vector3D.of(0.5, 0.5, 0.5), Vector3D.Unit.PLUS_X, TEST_PRECISION);

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

        Assert.assertNull(tree.linecastFirst(line.segment(Vector3D.of(-2, 0.5, 0.5), Vector3D.of(-1, 0.5, 0.5))));
        Assert.assertNull(tree.linecastFirst(line.segment(Vector3D.of(-2, 0.5, 0.5), Vector3D.of(-1, 0.5, 0.5))));
        Assert.assertNull(tree.linecastFirst(line.segment(Vector3D.of(0.25, 0.5, 0.5), Vector3D.of(0.75, 0.5, 0.5))));
    }

    @Test
    public void testInvertedRegion() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5));

        // act
        tree.complement();

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        EuclideanTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        Assert.assertNull(tree.getBarycenter());

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
        RegionBSPTree3D tree = createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(1.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tree.getBarycenter(), TEST_EPS);

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
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5)));
        tree.union(createRect(Vector3D.of(1.5, -0.5, -0.5), Vector3D.of(2.5, 0.5, 0.5)));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(12.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), tree.getBarycenter(), TEST_EPS);

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
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5)));
        tree.union(createRect(Vector3D.of(0.5, -0.5, -0.5), Vector3D.of(1.5, 0.5, 0.5)));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(10.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0, 0), tree.getBarycenter(), TEST_EPS);

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
        double eps = 1e-6;
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        // act
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5), precision));
        tree.union(createRect(Vector3D.of(0.5 + 1e-7, -0.5, -0.5), Vector3D.of(1.5 + 1e-7, 0.5, 0.5), precision));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(2.0, tree.getSize(), eps);
        Assert.assertEquals(10.0, tree.getBoundarySize(), eps);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5 + 5.4166e-8, 0, 0), tree.getBarycenter(), TEST_EPS);

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
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5)));
        tree.union(createRect(Vector3D.of(0.5, 0.5, -0.5), Vector3D.of(1.5, 1.5, 0.5)));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(12.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0), tree.getBarycenter(), TEST_EPS);


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
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5)));
        tree.union(createRect(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(1.5, 1.5, 1.5)));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(12.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getBarycenter(), TEST_EPS);

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
        Vector3D vertex1 = Vector3D.of(1, 2, 3);
        Vector3D vertex2 = Vector3D.of(2, 2, 4);
        Vector3D vertex3 = Vector3D.of(2, 3, 3);
        Vector3D vertex4 = Vector3D.of(1, 3, 4);

        List<ConvexSubPlane> boundaries = Arrays.asList(
                ConvexSubPlane.fromVertexLoop(Arrays.asList(vertex3, vertex2, vertex1), TEST_PRECISION),
                ConvexSubPlane.fromVertexLoop(Arrays.asList(vertex2, vertex3, vertex4), TEST_PRECISION),
                ConvexSubPlane.fromVertexLoop(Arrays.asList(vertex4, vertex3, vertex1), TEST_PRECISION),
                ConvexSubPlane.fromVertexLoop(Arrays.asList(vertex1, vertex2, vertex4), TEST_PRECISION)
            );

        // act
        RegionBSPTree3D tree = RegionBSPTree3D.full();
        tree.insert(boundaries);

        // assert
        Assert.assertEquals(1.0 / 3.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(2.0 * Math.sqrt(3.0), tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 2.5, 3.5), tree.getBarycenter(), TEST_EPS);

        double third = 1.0 / 3.0;
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
        double approximationTolerance = 0.2;
        double radius = 1.0;

        // act
        RegionBSPTree3D tree = createSphere(Vector3D.of(1, 2, 3), radius, 8, 16);

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(sphereVolume(radius), tree.getSize(), approximationTolerance);
        Assert.assertEquals(sphereSurface(radius), tree.getBoundarySize(), approximationTolerance);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3), tree.getBarycenter(), TEST_EPS);

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
        RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act/assert
        checkProject(tree, Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(0, 0.5, 0.5));
        checkProject(tree, Vector3D.of(0.4, 0.5, 0.5), Vector3D.of(0, 0.5, 0.5));
        checkProject(tree, Vector3D.of(1.5, 0.5, 0.5), Vector3D.of(1, 0.5, 0.5));
        checkProject(tree, Vector3D.of(2, 2, 2), Vector3D.of(1, 1, 1));
    }

    @Test
    public void testProjectToBoundary_invertedRegion() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        tree.complement();

        // act/assert
        checkProject(tree, Vector3D.of(0.4, 0.5, 0.5), Vector3D.of(0, 0.5, 0.5));
        checkProject(tree, Vector3D.of(1.5, 0.5, 0.5), Vector3D.of(1, 0.5, 0.5));
        checkProject(tree, Vector3D.of(2, 2, 2), Vector3D.of(1, 1, 1));
    }

    private void checkProject(RegionBSPTree3D tree, Vector3D toProject, Vector3D expectedPoint) {
        Vector3D proj = tree.project(toProject);

        EuclideanTestUtils.assertCoordinatesEqual(expectedPoint, proj, TEST_EPS);
    }

    @Test
    public void testBoolean_union() throws IOException {
        // arrange
        double tolerance = 0.05;
        double size = 1.0;
        double radius = size * 0.5;
        RegionBSPTree3D box = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        RegionBSPTree3D sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.union(box, sphere);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(cubeVolume(size) + (sphereVolume(radius) * 0.5),
                result.getSize(), tolerance);
        Assert.assertEquals(cubeSurface(size) - circleSurface(radius) + (0.5 * sphereSurface(radius)),
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
        double tolerance = 0.2;
        double radius = 1.0;

        RegionBSPTree3D sphere = createSphere(Vector3D.ZERO, radius, 8, 16);

        RegionBSPTree3D copy = RegionBSPTree3D.empty();
        copy.copy(sphere);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.union(sphere, copy);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(sphereVolume(radius), result.getSize(), tolerance);
        Assert.assertEquals(sphereSurface(radius), result.getBoundarySize(), tolerance);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, result.getBarycenter(), TEST_EPS);

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
        double tolerance = 0.05;
        double size = 1.0;
        double radius = size * 0.5;
        RegionBSPTree3D box = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        RegionBSPTree3D sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.intersection(box, sphere);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(sphereVolume(radius) * 0.5, result.getSize(), tolerance);
        Assert.assertEquals(circleSurface(radius) + (0.5 * sphereSurface(radius)),
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
        double tolerance = 0.2;
        double radius = 1.0;

        RegionBSPTree3D sphere = createSphere(Vector3D.ZERO, radius, 8, 16);
        RegionBSPTree3D copy = RegionBSPTree3D.empty();
        copy.copy(sphere);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.intersection(sphere, copy);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(sphereVolume(radius), result.getSize(), tolerance);
        Assert.assertEquals(sphereSurface(radius), result.getBoundarySize(), tolerance);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, result.getBarycenter(), TEST_EPS);

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
        double size = 1.0;
        RegionBSPTree3D box1 = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        RegionBSPTree3D box2 = createRect(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(0.5 + size, 0.5 + size, 0.5 + size));

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.xor(box1, box2);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals((2 * cubeVolume(size)) - (2 * cubeVolume(size * 0.5)), result.getSize(), TEST_EPS);
        Assert.assertEquals(2 * cubeSurface(size), result.getBoundarySize(), TEST_EPS);

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
        double tolerance = 0.05;
        double size = 1.0;
        double radius = size * 0.5;
        RegionBSPTree3D box = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        RegionBSPTree3D sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.xor(box, sphere);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(cubeVolume(size), result.getSize(), tolerance);
        Assert.assertEquals(cubeSurface(size) + (sphereSurface(radius)),
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
        double radius = 1.0;

        RegionBSPTree3D sphere = createSphere(Vector3D.ZERO, radius, 8, 16);
        RegionBSPTree3D copy = RegionBSPTree3D.empty();
        copy.copy(sphere);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.xor(sphere, copy);

        // assert
        Assert.assertTrue(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(0.0, result.getSize(), TEST_EPS);
        Assert.assertEquals(0.0, result.getBoundarySize(), TEST_EPS);
        Assert.assertNull(result.getBarycenter());

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
        double tolerance = 0.05;
        double size = 1.0;
        double radius = size * 0.5;
        RegionBSPTree3D box = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        RegionBSPTree3D sphere = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.difference(box, sphere);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(cubeVolume(size) - (sphereVolume(radius) * 0.5), result.getSize(), tolerance);
        Assert.assertEquals(cubeSurface(size) - circleSurface(radius) + (0.5 * sphereSurface(radius)),
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
        double radius = 1.0;

        RegionBSPTree3D sphere = createSphere(Vector3D.ZERO, radius, 8, 16);
        RegionBSPTree3D copy = sphere.copy();

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.difference(sphere, copy);

        // assert
        Assert.assertTrue(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(0.0, result.getSize(), TEST_EPS);
        Assert.assertEquals(0.0, result.getBoundarySize(), TEST_EPS);
        Assert.assertNull(result.getBarycenter());

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
        double tolerance = 0.05;
        double size = 1.0;
        double radius = size * 0.5;
        RegionBSPTree3D box = createRect(Vector3D.ZERO, Vector3D.of(size, size, size));
        RegionBSPTree3D sphereToAdd = createSphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16);
        RegionBSPTree3D sphereToRemove1 = createSphere(Vector3D.of(size * 0.5, 0, size * 0.5), radius, 8, 16);
        RegionBSPTree3D sphereToRemove2 = createSphere(Vector3D.of(size * 0.5, 1, size * 0.5), radius, 8, 16);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.union(box, sphereToAdd);
        result.difference(sphereToRemove1);
        result.difference(sphereToRemove2);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(cubeVolume(size) - (sphereVolume(radius) * 0.5),
                result.getSize(), tolerance);
        Assert.assertEquals(cubeSurface(size) - (3.0 * circleSurface(radius)) + (1.5 * sphereSurface(radius)),
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
        List<ConvexVolume> result = RegionBSPTree3D.empty().toConvex();

        // assert
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void testToConvex_singleBox() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.of(1, 2, 3), Vector3D.of(2, 3, 4));

        // act
        List<ConvexVolume> result = tree.toConvex();

        // assert
        Assert.assertEquals(1, result.size());

        ConvexVolume vol = result.get(0);
        Assert.assertEquals(1, vol.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 2.5, 3.5), vol.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testToConvex_multipleBoxes() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.of(4, 5, 6), Vector3D.of(5, 6, 7));
        tree.union(createRect(Vector3D.ZERO, Vector3D.of(2, 1, 1)));

        // act
        List<ConvexVolume> result = tree.toConvex();

        // assert
        Assert.assertEquals(2, result.size());

        boolean smallFirst = result.get(0).getSize() < result.get(1).getSize();

        ConvexVolume small = smallFirst ? result.get(0) : result.get(1);
        ConvexVolume large = smallFirst ? result.get(1) : result.get(0);

        Assert.assertEquals(1, small.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4.5, 5.5, 6.5), small.getBarycenter(), TEST_EPS);

        Assert.assertEquals(2, large.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0.5, 0.5), large.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testSplit() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.of(-0.5, -0.5, -0.5), Vector3D.of(0.5, 0.5, 0.5));

        Plane splitter = Plane.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<RegionBSPTree3D> split = tree.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        RegionBSPTree3D minus = split.getMinus();
        Assert.assertEquals(0.5, minus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.25, 0, 0), minus.getBarycenter(), TEST_EPS);

        RegionBSPTree3D plus = split.getPlus();
        Assert.assertEquals(0.5, plus.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.25, 0, 0), plus.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testGetNodeRegion() {
        // arrange
        RegionBSPTree3D tree = createRect(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act/assert
        ConvexVolume rootVol = tree.getRoot().getNodeRegion();
        GeometryTestUtils.assertPositiveInfinity(rootVol.getSize());
        Assert.assertNull(rootVol.getBarycenter());

        ConvexVolume plusVol = tree.getRoot().getPlus().getNodeRegion();
        GeometryTestUtils.assertPositiveInfinity(plusVol.getSize());
        Assert.assertNull(plusVol.getBarycenter());

        ConvexVolume centerVol = tree.findNode(Vector3D.of(0.5, 0.5, 0.5)).getNodeRegion();
        Assert.assertEquals(1, centerVol.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), centerVol.getBarycenter(), TEST_EPS);
    }

    // GEOMETRY-59
    @Test
    public void testSlightlyConcavePrism() {
        // arrange
        Vector3D[] vertices = {
            Vector3D.of(0, 0, 0),
            Vector3D.of(2, 1e-7, 0),
            Vector3D.of(4, 0, 0),
            Vector3D.of(2, 2, 0),
            Vector3D.of(0, 0, 2),
            Vector3D.of(2, 1e-7, 2),
            Vector3D.of(4, 0, 2),
            Vector3D.of(2, 2, 2)
        };

        int[][] facets = {
            {4, 5, 6, 7},
            {3, 2, 1, 0},
            {0, 1, 5, 4},
            {1, 2, 6, 5},
            {2, 3, 7, 6},
            {3, 0, 4, 7}
        };

        List<ConvexSubPlane> faces = indexedFacetsToBoundaries(vertices, facets);

        // act
        RegionBSPTree3D tree = RegionBSPTree3D.full();
        tree.insert(faces);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.INSIDE, Vector3D.of(2, 1, 1));
        EuclideanTestUtils.assertRegionLocation(tree, RegionLocation.OUTSIDE,
                Vector3D.of(2, 1, 3), Vector3D.of(2, 1, -3),
                Vector3D.of(2, -1, 1), Vector3D.of(2, 3, 1),
                Vector3D.of(-1, 1, 1), Vector3D.of(4, 1, 1));
    }

    private static List<ConvexSubPlane> indexedFacetsToBoundaries(Vector3D[] vertices, int[][] facets) {
        List<ConvexSubPlane> boundaries = new ArrayList<>();

        List<Vector3D> vertexList = new ArrayList<>();

        for (int i = 0; i < facets.length; ++i) {
            int[] indices = facets[i];

            for (int j = 0; j < indices.length; ++j) {
                vertexList.add(vertices[indices[j]]);
            }

            boundaries.add(ConvexSubPlane.fromVertexLoop(vertexList, TEST_PRECISION));

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

        planes.add(Plane.fromPointAndNormal(topZ, Vector3D.Unit.PLUS_Z, TEST_PRECISION));
        planes.add(Plane.fromPointAndNormal(bottomZ, Vector3D.Unit.MINUS_Z, TEST_PRECISION));

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

                planes.add(Plane.fromPointAndNormal(pt, norm, TEST_PRECISION));
            }
        }

        RegionBSPTree3D tree = RegionBSPTree3D.full();
        RegionNode3D node = tree.getRoot();

        for (Plane plane : planes) {
            node = node.cut(plane).getMinus();
        }

        return tree;
    }

    private static double cubeVolume(double size) {
        return size * size * size;
    }

    private static double cubeSurface(double size) {
        return 6.0 * size * size;
    }

    private static double sphereVolume(double radius) {
        return 4.0 * Math.PI * radius * radius * radius / 3.0;
    }

    private static double sphereSurface(double radius) {
        return 4.0 * Math.PI * radius * radius;
    }

    private static double circleSurface(double radius) {
        return Math.PI * radius * radius;
    }
}
