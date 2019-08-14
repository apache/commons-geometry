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
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.exception.GeometryValueException;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
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

        checkClassify(tree, RegionLocation.OUTSIDE,
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

        checkClassify(tree, RegionLocation.INSIDE,
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
        tree.getRoot().cut(Plane.fromNormal(Vector3D.PLUS_Z, TEST_PRECISION));

        // act
        RegionBSPTree3D copy = tree.copy();

        // assert
        Assert.assertNotSame(tree, copy);
        Assert.assertEquals(3, copy.count());
    }

    @Test
    public void testHalfSpace() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.insert(ConvexSubPlane.fromConvexArea(
                Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.PLUS_Y, TEST_PRECISION), ConvexArea.full()));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        EuclideanTestUtils.assertPositiveInfinity(tree.getSize());
        EuclideanTestUtils.assertPositiveInfinity(tree.getBoundarySize());
        Assert.assertNull(tree.getBarycenter());

        checkClassify(tree, RegionLocation.INSIDE,
                Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                Vector3D.of(-100, -100, -100));
        checkClassify(tree, RegionLocation.BOUNDARY, Vector3D.of(0, 0, 0));
        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(100, 100, 100),
                Vector3D.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    public void testFromConvexVolume_full() {
        // arrange
        ConvexVolume volume = ConvexVolume.full();

        // act
        RegionBSPTree3D tree = RegionBSPTree3D.fromConvexVolume(volume);
        Assert.assertNull(tree.getBarycenter());

        // assert
        Assert.assertTrue(tree.isFull());
    }

    @Test
    public void testFromConvexVolume_infinite() {
        // arrange
        ConvexVolume volume = ConvexVolume.fromBounds(Plane.fromNormal(Vector3D.PLUS_Z, TEST_PRECISION));

        // act
        RegionBSPTree3D tree = RegionBSPTree3D.fromConvexVolume(volume);

        // assert
        GeometryTestUtils.assertPositiveInfinity(tree.getSize());
        GeometryTestUtils.assertPositiveInfinity(tree.getBoundarySize());
        Assert.assertNull(tree.getBarycenter());

        checkClassify(tree, RegionLocation.OUTSIDE, Vector3D.of(0, 0, 1));
        checkClassify(tree, RegionLocation.BOUNDARY, Vector3D.ZERO);
        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(0, 0, -1));
    }

    @Test
    public void testFromConvexVolume_finite() {
        // arrange
        ConvexVolume volume = ConvexVolume.fromBounds(
                    Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.MINUS_X, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.MINUS_Y, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.MINUS_Z, TEST_PRECISION),

                    Plane.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.PLUS_X, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.PLUS_Y, TEST_PRECISION),
                    Plane.fromPointAndNormal(Vector3D.of(1, 1, 1), Vector3D.PLUS_Z, TEST_PRECISION)
                );

        // act
        RegionBSPTree3D tree = RegionBSPTree3D.fromConvexVolume(volume);

        // assert
        Assert.assertEquals(1, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0.5, 0.5), Vector3D.of(2, 0.5, 0.5),
                Vector3D.of(0.5, -1, 0.5), Vector3D.of(0.5, 2, 0.5),
                Vector3D.of(0.5, 0.5, -1), Vector3D.of(0.5, 0.5, 2));
        checkClassify(tree, RegionLocation.BOUNDARY, Vector3D.ZERO);
        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(0.5, 0.5, 0.5));
    }

    @Test
    public void testRaycastFirstFace() {
        // arrange
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.of(-1, -1, -1), 2, 2, 2, TEST_PRECISION);

        Line3D xPlus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(1, 0, 0), TEST_PRECISION);
        Line3D xMinus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(-1, 0, 0), TEST_PRECISION);

        Line3D yPlus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, 1, 0), TEST_PRECISION);
        Line3D yMinus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, -1, 0), TEST_PRECISION);

        Line3D zPlus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, 0, 1), TEST_PRECISION);
        Line3D zMinus = Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(0, 0, -1), TEST_PRECISION);

        // act/assert
        assertSubPlaneNormal(Vector3D.of(-1, 0, 0), tree.raycastFirst(xPlus.segmentFrom(Vector3D.of(-1.1, 0, 0))));
        assertSubPlaneNormal(Vector3D.of(-1, 0, 0), tree.raycastFirst(xPlus.segmentFrom(Vector3D.of(-1, 0, 0))));
        assertSubPlaneNormal(Vector3D.of(1, 0, 0), tree.raycastFirst(xPlus.segmentFrom(Vector3D.of(-0.9, 0, 0))));
        Assert.assertEquals(null, tree.raycastFirst(xPlus.segmentFrom(Vector3D.of(1.1, 0, 0))));

        assertSubPlaneNormal(Vector3D.of(1, 0, 0), tree.raycastFirst(xMinus.segmentFrom(Vector3D.of(1.1, 0, 0))));
        assertSubPlaneNormal(Vector3D.of(1, 0, 0), tree.raycastFirst(xMinus.segmentFrom(Vector3D.of(1, 0, 0))));
        assertSubPlaneNormal(Vector3D.of(-1, 0, 0), tree.raycastFirst(xMinus.segmentFrom(Vector3D.of(0.9, 0, 0))));
        Assert.assertEquals(null, tree.raycastFirst(xMinus.segmentFrom(Vector3D.of(-1.1, 0, 0))));

        assertSubPlaneNormal(Vector3D.of(0, -1, 0), tree.raycastFirst(yPlus.segmentFrom(Vector3D.of(0, -1.1, 0))));
        assertSubPlaneNormal(Vector3D.of(0, -1, 0), tree.raycastFirst(yPlus.segmentFrom(Vector3D.of(0, -1, 0))));
        assertSubPlaneNormal(Vector3D.of(0, 1, 0), tree.raycastFirst(yPlus.segmentFrom(Vector3D.of(0, -0.9, 0))));
        Assert.assertEquals(null, tree.raycastFirst(yPlus.segmentFrom(Vector3D.of(0, 1.1, 0))));

        assertSubPlaneNormal(Vector3D.of(0, 1, 0), tree.raycastFirst(yMinus.segmentFrom(Vector3D.of(0, 1.1, 0))));
        assertSubPlaneNormal(Vector3D.of(0, 1, 0), tree.raycastFirst(yMinus.segmentFrom(Vector3D.of(0, 1, 0))));
        assertSubPlaneNormal(Vector3D.of(0, -1, 0), tree.raycastFirst(yMinus.segmentFrom(Vector3D.of(0, 0.9, 0))));
        Assert.assertEquals(null, tree.raycastFirst(yMinus.segmentFrom(Vector3D.of(0, -1.1, 0))));

        assertSubPlaneNormal(Vector3D.of(0, 0, -1), tree.raycastFirst(zPlus.segmentFrom(Vector3D.of(0, 0, -1.1))));
        assertSubPlaneNormal(Vector3D.of(0, 0, -1), tree.raycastFirst(zPlus.segmentFrom(Vector3D.of(0, 0, -1))));
        assertSubPlaneNormal(Vector3D.of(0, 0, 1), tree.raycastFirst(zPlus.segmentFrom(Vector3D.of(0, 0, -0.9))));
        Assert.assertEquals(null, tree.raycastFirst(zPlus.segmentFrom(Vector3D.of(0, 0, 1.1))));

        assertSubPlaneNormal(Vector3D.of(0, 0, 1), tree.raycastFirst(zMinus.segmentFrom(Vector3D.of(0, 0, 1.1))));
        assertSubPlaneNormal(Vector3D.of(0, 0, 1), tree.raycastFirst(zMinus.segmentFrom(Vector3D.of(0, 0, 1))));
        assertSubPlaneNormal(Vector3D.of(0, 0, -1), tree.raycastFirst(zMinus.segmentFrom(Vector3D.of(0, 0, 0.9))));
        Assert.assertEquals(null, tree.raycastFirst(zMinus.segmentFrom(Vector3D.of(0, 0, -1.1))));
    }

    // issue GEOMETRY-38
    @Test
    public void testRaycastFirstFace_linePassesThroughVertex() {
        // arrange
        Vector3D lowerCorner = Vector3D.ZERO;
        Vector3D upperCorner = Vector3D.of(1, 1, 1);
        Vector3D center = lowerCorner.lerp(upperCorner, 0.5);

        RegionBSPTree3D tree = RegionBSPTree3D.rect(lowerCorner, upperCorner, TEST_PRECISION);

        Line3D upDiagonal = Line3D.fromPoints(lowerCorner, upperCorner, TEST_PRECISION);
        Line3D downDiagonal = upDiagonal.reverse();

        // act/assert
        ConvexSubPlane upFromOutsideResult = tree.raycastFirst(upDiagonal.segmentFrom(Vector3D.of(-1, -1, -1)));
        Assert.assertNotNull(upFromOutsideResult);
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, upFromOutsideResult.getPlane().intersection(upDiagonal), TEST_EPS);

        ConvexSubPlane upFromCenterResult = tree.raycastFirst(upDiagonal.segmentFrom(center));
        Assert.assertNotNull(upFromCenterResult);
        EuclideanTestUtils.assertCoordinatesEqual(upperCorner, upFromCenterResult.getPlane().intersection(upDiagonal), TEST_EPS);

        ConvexSubPlane downFromOutsideResult = tree.raycastFirst(downDiagonal.segmentFrom(Vector3D.of(2, 2, 2)));
        Assert.assertNotNull(downFromOutsideResult);
        EuclideanTestUtils.assertCoordinatesEqual(upperCorner, downFromOutsideResult.getPlane().intersection(downDiagonal), TEST_EPS);

        ConvexSubPlane downFromCenterResult = tree.raycastFirst(downDiagonal.segmentFrom(center));
        Assert.assertNotNull(downFromCenterResult);
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, downFromCenterResult.getPlane().intersection(downDiagonal), TEST_EPS);
    }

    // Issue GEOMETRY-43
    @Test
    public void testFirstIntersection_lineParallelToFace() {
        // arrange - setup box
        Vector3D lowerCorner = Vector3D.ZERO;
        Vector3D upperCorner = Vector3D.of(1, 1, 1);

        RegionBSPTree3D tree = RegionBSPTree3D.rect(lowerCorner, upperCorner, TEST_PRECISION);

        Vector3D firstPointOnLine = Vector3D.of(0.5, -1.0, 0);
        Vector3D secondPointOnLine = Vector3D.of(0.5, 2.0, 0);
        Line3D bottomLine = Line3D.fromPoints(firstPointOnLine, secondPointOnLine, TEST_PRECISION);

        Vector3D expectedIntersection1 = Vector3D.of(0.5, 0, 0.0);
        Vector3D expectedIntersection2 = Vector3D.of(0.5, 1.0, 0.0);

        // act/assert
        ConvexSubPlane bottom = tree.raycastFirst(bottomLine.segmentFrom(firstPointOnLine));
        Assert.assertNotNull(bottom);
        EuclideanTestUtils.assertCoordinatesEqual(expectedIntersection1, bottom.getHyperplane().intersection(bottomLine), TEST_EPS);

        bottom = tree.raycastFirst(bottomLine.segmentFrom(Vector3D.of(0.5, 0.1, 0.0)));
        Assert.assertNotNull(bottom);
        Vector3D intersection = bottom.getPlane().intersection(bottomLine);
        Assert.assertNotNull(intersection);
        EuclideanTestUtils.assertCoordinatesEqual(expectedIntersection2, intersection, TEST_EPS);
    }

    @Test
    public void testRaycastFirstFace_rayPointOnFace() {
        // arrange
        Vector3D lowerCorner = Vector3D.ZERO;
        Vector3D upperCorner = Vector3D.of(1, 1, 1);

        RegionBSPTree3D tree = RegionBSPTree3D.rect(lowerCorner, upperCorner, TEST_PRECISION);

        Vector3D pt = Vector3D.of(0.5, 0.5, 0);
        Line3D intoBoxLine = Line3D.fromPoints(pt, pt.add(Vector3D.PLUS_Z), TEST_PRECISION);
        Line3D outOfBoxLine = Line3D.fromPoints(pt, pt.add(Vector3D.MINUS_Z), TEST_PRECISION);

        // act/assert
        ConvexSubPlane intoBoxResult = tree.raycastFirst(intoBoxLine.segmentFrom(pt));
        Vector3D intoBoxPt = intoBoxResult.getPlane().intersection(intoBoxLine);
        EuclideanTestUtils.assertCoordinatesEqual(pt, intoBoxPt, TEST_EPS);

        ConvexSubPlane outOfBoxResult = tree.raycastFirst(outOfBoxLine.segmentFrom(pt));
        Vector3D outOfBoxPt = outOfBoxResult.getPlane().intersection(outOfBoxLine);
        EuclideanTestUtils.assertCoordinatesEqual(pt, outOfBoxPt, TEST_EPS);
    }

    @Test
    public void testRaycastFirstFace_rayPointOnVertex() {
        // arrange
        Vector3D lowerCorner = Vector3D.ZERO;
        Vector3D upperCorner = Vector3D.of(1, 1, 1);

        RegionBSPTree3D tree = RegionBSPTree3D.rect(lowerCorner, upperCorner, TEST_PRECISION);

        Line3D intoBoxLine = Line3D.fromPoints(lowerCorner, upperCorner, TEST_PRECISION);
        Line3D outOfBoxLine = intoBoxLine.reverse();

        // act/assert
        ConvexSubPlane intoBoxResult = tree.raycastFirst(intoBoxLine.segmentFrom(lowerCorner));
        Vector3D intoBoxPt = intoBoxResult.getPlane().intersection(intoBoxLine);
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, intoBoxPt, TEST_EPS);

        ConvexSubPlane outOfBoxResult = tree.raycastFirst(outOfBoxLine.segmentFrom(lowerCorner));
        Vector3D outOfBoxPt = outOfBoxResult.getPlane().intersection(outOfBoxLine);
        EuclideanTestUtils.assertCoordinatesEqual(lowerCorner, outOfBoxPt, TEST_EPS);
    }

    @Test
    public void testRaycastFirstFace_onlyReturnsPointsWithinSegment() throws IOException, ParseException {
        // arrange
        Vector3D lowerCorner = Vector3D.ZERO;
        Vector3D upperCorner = Vector3D.of(1, 1, 1);

        RegionBSPTree3D tree = RegionBSPTree3D.rect(lowerCorner, upperCorner, TEST_PRECISION);

        Line3D line = Line3D.fromPointAndDirection(Vector3D.of(0.5, 0.5, 0.5), Vector3D.PLUS_X, TEST_PRECISION);

        // act/assert
        assertSubPlaneNormal(Vector3D.MINUS_X, tree.raycastFirst(line.span()));
        assertSubPlaneNormal(Vector3D.PLUS_X, tree.raycastFirst(line.reverse().span()));

        assertSubPlaneNormal(Vector3D.MINUS_X, tree.raycastFirst(line.segment(Vector3D.of(-2, 0.5, 0.5), Vector3D.of(0.5, 0.5, 0.5))));
        assertSubPlaneNormal(Vector3D.MINUS_X, tree.raycastFirst(line.segment(Vector3D.of(-2, 0.5, 0.5), Vector3D.of(0, 0.5, 0.5))));

        assertSubPlaneNormal(Vector3D.PLUS_X, tree.raycastFirst(line.segment(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(2, 0.5, 0.5))));
        assertSubPlaneNormal(Vector3D.PLUS_X, tree.raycastFirst(line.segment(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(1, 0.5, 0.5))));

        Assert.assertNull(tree.raycastFirst(line.segment(Vector3D.of(-2, 0.5, 0.5), Vector3D.of(-1, 0.5, 0.5))));
        Assert.assertNull(tree.raycastFirst(line.segment(Vector3D.of(-2, 0.5, 0.5), Vector3D.of(-1, 0.5, 0.5))));
        Assert.assertNull(tree.raycastFirst(line.segment(Vector3D.of(0.25, 0.5, 0.5), Vector3D.of(0.75, 0.5, 0.5))));
    }

    @Test
    public void testInvertedRegion() {
        // arrange
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.of(-0.5, -0.5, -0.5), 1, 1, 1, TEST_PRECISION);

        // act
        tree.complement();

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        EuclideanTestUtils.assertPositiveInfinity(tree.getSize());
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        Assert.assertNull(tree.getBarycenter());

        checkClassify(tree, RegionLocation.INSIDE,
                Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE),
                Vector3D.of(-100, -100, -100),
                Vector3D.of(100, 100, 100),
                Vector3D.of(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(0, 0, 0));
    }

    @Test
    public void testUnitBox() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.of(-0.5, -0.5, -0.5), 1, 1, 1, TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(1.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
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

        checkClassify(tree, RegionLocation.BOUNDARY,
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

        checkClassify(tree, RegionLocation.INSIDE,
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
        tree.union(RegionBSPTree3D.rect(Vector3D.of(-0.5, -0.5, -0.5), 1, 1, 1, TEST_PRECISION));
        tree.union(RegionBSPTree3D.rect(Vector3D.of(1.5, -0.5, -0.5), 1, 1, 1, TEST_PRECISION));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(12.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 0), tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(3, 0, 0));

        checkClassify(tree, RegionLocation.INSIDE,
                Vector3D.of(0, 0, 0),
                Vector3D.of(2, 0, 0));
    }

    @Test
    public void testTwoBoxes_sharedSide() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(RegionBSPTree3D.rect(Vector3D.of(-0.5, -0.5, -0.5), 1, 1, 1, TEST_PRECISION));
        tree.union(RegionBSPTree3D.rect(Vector3D.of(0.5, -0.5, -0.5), 1, 1, 1, TEST_PRECISION));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(10.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0, 0), tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(2, 0, 0));

        checkClassify(tree, RegionLocation.INSIDE,
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
        tree.union(RegionBSPTree3D.rect(Vector3D.of(-0.5, -0.5, -0.5), 1, 1, 1, precision));
        tree.union(RegionBSPTree3D.rect(Vector3D.of(0.5 + 1e-7, -0.5, -0.5), 1, 1, 1, precision));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(2.0, tree.getSize(), eps);
        Assert.assertEquals(10.0, tree.getBoundarySize(), eps);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5 + 5.41e-8, 0, 0), tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(2, 0, 0));

        checkClassify(tree, RegionLocation.INSIDE,
                Vector3D.of(0, 0, 0),
                Vector3D.of(1, 0, 0));
    }

    @Test
    public void testTwoBoxes_sharedEdge() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(RegionBSPTree3D.rect(Vector3D.of(-0.5, -0.5, -0.5), 1, 1, 1, TEST_PRECISION));
        tree.union(RegionBSPTree3D.rect(Vector3D.of(0.5, 0.5, -0.5), 1, 1, 1, TEST_PRECISION));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(12.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0), tree.getBarycenter(), TEST_EPS);


        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 0),
                Vector3D.of(2, 1, 0));

        checkClassify(tree, RegionLocation.INSIDE,
                Vector3D.of(0, 0, 0),
                Vector3D.of(1, 1, 0));
    }

    @Test
    public void testTwoBoxes_sharedPoint() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.empty();
        tree.union(RegionBSPTree3D.rect(Vector3D.of(-0.5, -0.5, -0.5), 1, 1, 1, TEST_PRECISION));
        tree.union(RegionBSPTree3D.rect(Vector3D.of(0.5, 0.5, 0.5), 1, 1, 1, TEST_PRECISION));

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(12.0, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(0, 1, 1),
                Vector3D.of(2, 1, 1));

        checkClassify(tree, RegionLocation.INSIDE,
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

        // act
        RegionBSPTree3D tree = ConvexVolume.fromBounds(
                    Plane.fromPoints(vertex3, vertex2, vertex1, TEST_PRECISION),
                    Plane.fromPoints(vertex2, vertex3, vertex4, TEST_PRECISION),
                    Plane.fromPoints(vertex4, vertex3, vertex1, TEST_PRECISION),
                    Plane.fromPoints(vertex1, vertex2, vertex4, TEST_PRECISION)
                ).toTree();

        // assert
        Assert.assertEquals(1.0 / 3.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(2.0 * Math.sqrt(3.0), tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 2.5, 3.5), tree.getBarycenter(), TEST_EPS);

        double third = 1.0 / 3.0;
        checkClassify(tree, RegionLocation.BOUNDARY,
            vertex1, vertex2, vertex3, vertex4,
            Vector3D.linearCombination(third, vertex1, third, vertex2, third, vertex3),
            Vector3D.linearCombination(third, vertex2, third, vertex3, third, vertex4),
            Vector3D.linearCombination(third, vertex3, third, vertex4, third, vertex1),
            Vector3D.linearCombination(third, vertex4, third, vertex1, third, vertex2)
        );
        checkClassify(tree, RegionLocation.OUTSIDE,
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
        RegionBSPTree3D tree = RegionBSPTree3D.sphere(Vector3D.of(1, 2, 3), radius, 8, 16, TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isEmpty());
        Assert.assertFalse(tree.isFull());

        Assert.assertEquals(sphereVolume(radius), tree.getSize(), approximationTolerance);
        Assert.assertEquals(sphereSurface(radius), tree.getBoundarySize(), approximationTolerance);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3), tree.getBarycenter(), TEST_EPS);

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 2, 3),
                Vector3D.of(2.1, 2, 3),
                Vector3D.of(1, 0.9, 3),
                Vector3D.of(1, 3.1, 3),
                Vector3D.of(1, 2, 1.9),
                Vector3D.of(1, 2, 4.1),
                Vector3D.of(1.6, 2.6, 3.6));

        checkClassify(tree, RegionLocation.INSIDE,
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
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.ZERO, 1, 1, 1, TEST_PRECISION);

        // act/assert
        checkProject(tree, Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(0, 0.5, 0.5));
        checkProject(tree, Vector3D.of(0.4, 0.5, 0.5), Vector3D.of(0, 0.5, 0.5));
        checkProject(tree, Vector3D.of(1.5, 0.5, 0.5), Vector3D.of(1, 0.5, 0.5));
        checkProject(tree, Vector3D.of(2, 2, 2), Vector3D.of(1, 1, 1));
    }

    @Test
    public void testProjectToBoundary_invertedRegion() {
        // arrange
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.ZERO, 1, 1, 1, TEST_PRECISION);
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
        RegionBSPTree3D box = RegionBSPTree3D.rect(Vector3D.ZERO, size, size, size, TEST_PRECISION);
        RegionBSPTree3D sphere = RegionBSPTree3D.sphere(Vector3D.of(size * 0.5, size * 0.5, size),
                radius, 8, 16, TEST_PRECISION);

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

        checkClassify(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 0.5, 0.5),
                Vector3D.of(1.1, 0.5, 0.5),
                Vector3D.of(0.5, -0.1, 0.5),
                Vector3D.of(0.5, 1.1, 0.5),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 1.6));

        checkClassify(result, RegionLocation.INSIDE,
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

        RegionBSPTree3D sphere = RegionBSPTree3D.sphere(Vector3D.ZERO, radius, 8, 16, TEST_PRECISION);
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

        checkClassify(result, RegionLocation.OUTSIDE,
                Vector3D.of(-1.1, 0, 0),
                Vector3D.of(1.1, 0, 0),
                Vector3D.of(0, -1.1, 0),
                Vector3D.of(0, 1.1, 0),
                Vector3D.of(0, 0, -1.1),
                Vector3D.of(0, 0, 1.1));

        checkClassify(result, RegionLocation.INSIDE,
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
        RegionBSPTree3D box = RegionBSPTree3D.rect(Vector3D.ZERO, size, size, size, TEST_PRECISION);
        RegionBSPTree3D sphere = RegionBSPTree3D.sphere(Vector3D.of(size * 0.5, size * 0.5, size), radius, 8, 16, TEST_PRECISION);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.intersection(box, sphere);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals((sphereVolume(radius) * 0.5), result.getSize(), tolerance);
        Assert.assertEquals(circleSurface(radius) + (0.5 * sphereSurface(radius)),
                result.getBoundarySize(), tolerance);

        checkClassify(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 0.5, 1.0),
                Vector3D.of(1.1, 0.5, 1.0),
                Vector3D.of(0.5, -0.1, 1.0),
                Vector3D.of(0.5, 1.1, 1.0),
                Vector3D.of(0.5, 0.5, 0.4),
                Vector3D.of(0.5, 0.5, 1.1));

        checkClassify(result, RegionLocation.INSIDE,
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

        RegionBSPTree3D sphere = RegionBSPTree3D.sphere(Vector3D.ZERO, radius, 8, 16, TEST_PRECISION);
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

        checkClassify(result, RegionLocation.OUTSIDE,
                Vector3D.of(-1.1, 0, 0),
                Vector3D.of(1.1, 0, 0),
                Vector3D.of(0, -1.1, 0),
                Vector3D.of(0, 1.1, 0),
                Vector3D.of(0, 0, -1.1),
                Vector3D.of(0, 0, 1.1));

        checkClassify(result, RegionLocation.INSIDE,
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
        RegionBSPTree3D box1 = RegionBSPTree3D.rect(Vector3D.ZERO, size, size, size, TEST_PRECISION);
        RegionBSPTree3D box2 = RegionBSPTree3D.rect(Vector3D.of(0.5, 0.5, 0.5), size, size, size, TEST_PRECISION);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.xor(box1, box2);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals((2 * cubeVolume(size)) - (2 * cubeVolume(size * 0.5)), result.getSize(), TEST_EPS);
        Assert.assertEquals(2 * cubeSurface(size), result.getBoundarySize(), TEST_EPS);

        checkClassify(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, -0.1, -0.1),
                Vector3D.of(0.75, 0.75, 0.75),
                Vector3D.of(1.6, 1.6, 1.6));

        checkClassify(result, RegionLocation.BOUNDARY,
                Vector3D.of(0, 0, 0),
                Vector3D.of(0.5, 0.5, 0.5),
                Vector3D.of(1, 1, 1),
                Vector3D.of(1.5, 1.5, 1.5));

        checkClassify(result, RegionLocation.INSIDE,
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
        RegionBSPTree3D box = RegionBSPTree3D.rect(Vector3D.ZERO, size, size, size, TEST_PRECISION);
        RegionBSPTree3D sphere = RegionBSPTree3D.sphere(Vector3D.of(size * 0.5, size * 0.5, size),
                radius, 8, 16, TEST_PRECISION);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.xor(box, sphere);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(cubeVolume(size), result.getSize(), tolerance);
        Assert.assertEquals(cubeSurface(size) + (sphereSurface(radius)),
                result.getBoundarySize(), tolerance);

        checkClassify(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 0.5, 0.5),
                Vector3D.of(1.1, 0.5, 0.5),
                Vector3D.of(0.5, -0.1, 0.5),
                Vector3D.of(0.5, 1.1, 0.5),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 1.6),
                Vector3D.of(0.5, 0.5, 0.9));

        checkClassify(result, RegionLocation.INSIDE,
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

        RegionBSPTree3D sphere = RegionBSPTree3D.sphere(Vector3D.ZERO, radius, 8, 16, TEST_PRECISION);
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

        checkClassify(result, RegionLocation.OUTSIDE,
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
        RegionBSPTree3D box = RegionBSPTree3D.rect(Vector3D.ZERO, size, size, size, TEST_PRECISION);
        RegionBSPTree3D sphere = RegionBSPTree3D.sphere(Vector3D.of(size * 0.5, size * 0.5, size),
                radius, 8, 16, TEST_PRECISION);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.difference(box, sphere);

        // assert
        Assert.assertFalse(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(cubeVolume(size) - (sphereVolume(radius) * 0.5), result.getSize(), tolerance);
        Assert.assertEquals(cubeSurface(size) - circleSurface(radius) + (0.5 * sphereSurface(radius)),
                result.getBoundarySize(), tolerance);

        checkClassify(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 0.5, 1.0),
                Vector3D.of(1.1, 0.5, 1.0),
                Vector3D.of(0.5, -0.1, 1.0),
                Vector3D.of(0.5, 1.1, 1.0),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 0.6));

        checkClassify(result, RegionLocation.INSIDE,
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

        RegionBSPTree3D sphere = RegionBSPTree3D.sphere(Vector3D.ZERO, radius, 8, 16, TEST_PRECISION);
        RegionBSPTree3D copy = RegionBSPTree3D.empty();
        copy.copy(sphere);

        // act
        RegionBSPTree3D result = RegionBSPTree3D.empty();
        result.difference(sphere, copy);

        // assert
        Assert.assertTrue(result.isEmpty());
        Assert.assertFalse(result.isFull());

        Assert.assertEquals(0.0, result.getSize(), TEST_EPS);
        Assert.assertEquals(0.0, result.getBoundarySize(), TEST_EPS);
        Assert.assertNull(result.getBarycenter());

        checkClassify(result, RegionLocation.OUTSIDE,
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
        RegionBSPTree3D box = RegionBSPTree3D.rect(Vector3D.ZERO, size, size, size, TEST_PRECISION);
        RegionBSPTree3D sphereToAdd = RegionBSPTree3D.sphere(Vector3D.of(size * 0.5, size * 0.5, size),
                radius, 8, 16, TEST_PRECISION);
        RegionBSPTree3D sphereToRemove1 = RegionBSPTree3D.sphere(Vector3D.of(size * 0.5, 0, size * 0.5),
                radius, 8, 16, TEST_PRECISION);
        RegionBSPTree3D sphereToRemove2 = RegionBSPTree3D.sphere(Vector3D.of(size * 0.5, 1, size * 0.5),
                radius, 8, 16, TEST_PRECISION);

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

        checkClassify(result, RegionLocation.OUTSIDE,
                Vector3D.of(-0.1, 0.5, 0.5),
                Vector3D.of(1.1, 0.5, 0.5),
                Vector3D.of(0.5, 0.4, 0.5),
                Vector3D.of(0.5, 0.6, 0.5),
                Vector3D.of(0.5, 0.5, -0.1),
                Vector3D.of(0.5, 0.5, 1.6));

        checkClassify(result, RegionLocation.INSIDE,
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
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.of(1, 2, 3), 1, 1, 1, TEST_PRECISION);

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
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.of(4, 5, 6), 1, 1, 1, TEST_PRECISION);
        tree.union(RegionBSPTree3D.rect(Vector3D.ZERO, 2, 1, 1, TEST_PRECISION));

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
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.of(-0.5, -0.5, -0.5), 1, 1, 1, TEST_PRECISION);

        Plane splitter = Plane.fromNormal(Vector3D.PLUS_X, TEST_PRECISION);

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
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION);

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

    @Test
    public void testRect_deltaValues_positive() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.ZERO, 1, 1, 1, TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getBarycenter(), TEST_EPS);
        Assert.assertEquals(1.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);

        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(0.5, 0.5, 0.5));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0.5, 0.5), Vector3D.of(2, 0.5, 0.5),
                Vector3D.of(0.5, -1, 0.5), Vector3D.of(0.5, 2, 0.5),
                Vector3D.of(0.5, 0.5, -1), Vector3D.of(0.5, 0.5, 2));
    }

    @Test
    public void testRect_deltaValues_negative() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.ZERO, -1, -1, -1, TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.5, -0.5, -0.5), tree.getBarycenter(), TEST_EPS);
        Assert.assertEquals(1.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);

        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(-0.5, -0.5, -0.5));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(-2, -0.5, -0.5), Vector3D.of(1, -0.5, -0.5),
                Vector3D.of(-0.5, -2, -0.5), Vector3D.of(-0.5, 1, -0.5),
                Vector3D.of(-0.5, -0.5, -2), Vector3D.of(-0.5, -0.5, 1));
    }

    @Test
    public void testRect_givenPoints() {
        // act
        RegionBSPTree3D tree = RegionBSPTree3D.rect(Vector3D.of(1, 0, 0), Vector3D.of(2, 2, 1), TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 1, 0.5), tree.getBarycenter(), TEST_EPS);
        Assert.assertEquals(2.0, tree.getSize(), TEST_EPS);
        Assert.assertEquals(10.0, tree.getBoundarySize(), TEST_EPS);

        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(1.5, 1, 0.5));

        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(0.5, 1, 0.5), Vector3D.of(2.5, 1, 0.5),
                Vector3D.of(1.5, -1, 0.5), Vector3D.of(1.5, 3, 0.5),
                Vector3D.of(1.5, 1, -0.5), Vector3D.of(1.5, 1, 1.5));
    }

    @Test
    public void testRect_invalidDimensions() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            RegionBSPTree3D.rect(Vector3D.ZERO, 1e-20, 1, 1, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            RegionBSPTree3D.rect(Vector3D.ZERO, 1, 1e-20, 1, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            RegionBSPTree3D.rect(Vector3D.ZERO, 1, 1, 1e-20, TEST_PRECISION);
        }, GeometryValueException.class);

        GeometryTestUtils.assertThrows(() -> {
            RegionBSPTree3D.rect(Vector3D.ZERO, 0, 0, 0, TEST_PRECISION);
        }, GeometryValueException.class);
    }

    @Test
    public void testFromFacets_triangles() {
        // arrange
        Vector3D vertices[] = {
                Vector3D.ZERO,
                Vector3D.of(1, 0, 0),
                Vector3D.of(1, 1, 0),
                Vector3D.of(0, 1, 0),

                Vector3D.of(0, 0, 1),
                Vector3D.of(1, 0, 1),
                Vector3D.of(1, 1, 1),
                Vector3D.of(0, 1, 1)
        };

        int[][] facets = {
                { 0, 3, 2 },
                { 0, 2, 1 },

                { 4, 5, 6 },
                { 4, 6, 7 },

                { 5, 1, 2 },
                { 5, 2, 6 },

                { 4, 7, 3 },
                { 4, 3, 0 },

                { 4, 0, 1 },
                { 4, 1, 5 },

                { 7, 6, 2 },
                { 7, 2, 3 }
        };

        // act
        RegionBSPTree3D tree = RegionBSPTree3D.fromFacets(vertices, facets, TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(1, tree.getSize(), TEST_EPS);
        Assert.assertEquals(6, tree.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 0.5), tree.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testFromFacets_concaveFacets() {
        // arrange
        Vector3D[] vertices = {
                Vector3D.of(-1, 0, 1),
                Vector3D.of(-1, 0, 0),

                Vector3D.of(0, 2, 1),
                Vector3D.of(0, 2, 0),

                Vector3D.of(1, 0, 1),
                Vector3D.of(1, 0, 0),

                Vector3D.of(0, 1, 1),
                Vector3D.of(0, 1, 0)
        };

        int[][] facets = {
                { 0, 2, 3, 1 },
                { 4, 5, 3, 2 },
                { 0, 1, 7, 6 },
                { 4, 6, 7, 5 },
                { 0, 6, 4, 2 },
                { 1, 3, 5, 7 }
        };

        // act
        RegionBSPTree3D tree = RegionBSPTree3D.fromFacets(vertices, facets, TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertTrue(Double.isFinite(tree.getSize()));
        Assert.assertTrue(Double.isFinite(tree.getBoundarySize()));
        Assert.assertNotNull(tree.getBarycenter());

        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(0, 1.5, 0.5));
        checkClassify(tree, RegionLocation.OUTSIDE, Vector3D.of(0, 0.5, 0.5));
    }

    // GEOMETRY-59
    @Test
    public void testSlightlyConcavePrism() {
        // arrange
        Vector3D vertices[] = {
            Vector3D.of(0, 0, 0),
            Vector3D.of(2, 1e-7, 0),
            Vector3D.of(4, 0, 0),
            Vector3D.of(2, 2, 0),
            Vector3D.of(0, 0, 2),
            Vector3D.of(2, 1e-7, 2),
            Vector3D.of(4, 0, 2),
            Vector3D.of(2, 2, 2)
        };

        int facets[][] = {
            { 4, 5, 6, 7 },
            { 3, 2, 1, 0 },
            { 0, 1, 5, 4 },
            { 1, 2, 6, 5 },
            { 2, 3, 7, 6 },
            { 3, 0, 4, 7 }
        };

        // act
        RegionBSPTree3D tree = RegionBSPTree3D.fromFacets(vertices, facets, TEST_PRECISION);

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        checkClassify(tree, RegionLocation.INSIDE, Vector3D.of(2, 1, 1));
        checkClassify(tree, RegionLocation.OUTSIDE,
                Vector3D.of(2, 1, 3), Vector3D.of(2, 1, -3),
                Vector3D.of(2, -1, 1), Vector3D.of(2, 3, 1),
                Vector3D.of(-1, 1, 1), Vector3D.of(4, 1, 1));
    }

    private static void assertSubPlaneNormal(Vector3D expectedNormal, ConvexSubPlane sub) {
        EuclideanTestUtils.assertCoordinatesEqual(expectedNormal, sub.getPlane().getNormal(), TEST_EPS);
    }

    private static void checkClassify(Region<Vector3D> region, RegionLocation loc, Vector3D ... points) {
        for (Vector3D point : points) {
            String msg = "Unexpected location for point " + point;

            Assert.assertEquals(msg, loc, region.classify(point));
        }
    }

    private double cubeVolume(double size) {
        return size * size * size;
    }

    private double cubeSurface(double size) {
        return 6.0 * size * size;
    }

    private double sphereVolume(double radius) {
        return 4.0 * Math.PI * radius * radius * radius / 3.0;
    }

    private double sphereSurface(double radius) {
        return 4.0 * Math.PI * radius * radius;
    }

    private double circleSurface(double radius) {
        return Math.PI * radius * radius;
    }
}
