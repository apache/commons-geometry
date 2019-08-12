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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partition.Split;
import org.apache.commons.geometry.core.partition.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.junit.Test;

import org.junit.Assert;

public class SubPlaneTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Plane XY_PLANE = Plane.fromPointAndPlaneVectors(Vector3D.ZERO,
            Vector3D.PLUS_X, Vector3D.PLUS_Y, TEST_PRECISION);

    @Test
    public void testCtor_plane() {
        // act
        SubPlane sp = new SubPlane(XY_PLANE);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertTrue(sp.isEmpty());

        Assert.assertEquals(0, sp.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_plane_booleanFalse() {
        // act
        SubPlane sp = new SubPlane(XY_PLANE, false);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertTrue(sp.isEmpty());

        Assert.assertEquals(0, sp.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_plane_booleanTrue() {
        // act
        SubPlane sp = new SubPlane(XY_PLANE, true);

        // assert
        Assert.assertTrue(sp.isFull());
        Assert.assertFalse(sp.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(sp.getSize());
    }

    @Test
    public void testToConvex_full() {
        // act
        SubPlane sp = new SubPlane(XY_PLANE, true);

        // act
        List<ConvexSubPlane> convex = sp.toConvex();

        // assert
        Assert.assertEquals(1, convex.size());
        Assert.assertTrue(convex.get(0).isFull());
    }

    @Test
    public void testToConvex_empty() {
        // act
        SubPlane sp = new SubPlane(XY_PLANE, false);

        // act
        List<ConvexSubPlane> convex = sp.toConvex();

        // assert
        Assert.assertEquals(0, convex.size());
    }

    @Test
    public void testToConvex_nonConvexRegion() {
        // act
        ConvexArea a = ConvexArea.fromVertexLoop(Arrays.asList(
                    Vector2D.of(0, 0), Vector2D.of(1, 0),
                    Vector2D.of(1, 1), Vector2D.of(0, 1)
                ), TEST_PRECISION);
        ConvexArea b = ConvexArea.fromVertexLoop(Arrays.asList(
                    Vector2D.of(1, 0), Vector2D.of(2, 0),
                    Vector2D.of(2, 1), Vector2D.of(1, 1)
                ), TEST_PRECISION);

        SubPlane sp = new SubPlane(XY_PLANE, false);
        sp.add(ConvexSubPlane.fromConvexArea(XY_PLANE, a));
        sp.add(ConvexSubPlane.fromConvexArea(XY_PLANE, b));

        // act
        List<ConvexSubPlane> convex = sp.toConvex();

        // assert
        Assert.assertEquals(2, convex.size());
        Assert.assertEquals(1, convex.get(0).getSize(), TEST_EPS);
        Assert.assertEquals(1, convex.get(1).getSize(), TEST_EPS);
    }

    @Test
    public void testSplit_empty() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, false);

        Plane splitter = Plane.fromNormal(Vector3D.PLUS_X, TEST_PRECISION);

        // act
        Split<SubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_halfSpace() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, false);
        sp.getSubspaceRegion().getRoot().cut(
                Line.fromPointAndAngle(Vector2D.ZERO, Geometry.ZERO_PI, TEST_PRECISION));

        Plane splitter = Plane.fromNormal(Vector3D.PLUS_X, TEST_PRECISION);

        // act
        Split<SubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        SubPlane minus = split.getMinus();
        checkPoints(minus, RegionLocation.INSIDE, Vector3D.of(-1, 1, 0));
        checkPoints(minus, RegionLocation.OUTSIDE, Vector3D.of(1, 1, 0), Vector3D.of(0, -1, 0));

        SubPlane plus = split.getPlus();
        checkPoints(plus, RegionLocation.OUTSIDE, Vector3D.of(-1, 1, 0), Vector3D.of(0, -1, 0));
        checkPoints(plus, RegionLocation.INSIDE, Vector3D.of(1, 1, 0));
    }

    @Test
    public void testSplit_both() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, false);
        sp.getSubspaceRegion().union(RegionBSPTree2D.rect(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION));

        Plane splitter = Plane.fromNormal(Vector3D.PLUS_X, TEST_PRECISION);

        // act
        Split<SubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        SubPlane minus = split.getMinus();
        checkPoints(minus, RegionLocation.INSIDE, Vector3D.of(-0.5, 0, 0));
        checkPoints(minus, RegionLocation.OUTSIDE,
                Vector3D.of(0.5, 0, 0), Vector3D.of(1.5, 0, 0),
                Vector3D.of(0, 1.5, 0), Vector3D.of(0, -1.5, 0));

        SubPlane plus = split.getPlus();
        checkPoints(plus, RegionLocation.INSIDE, Vector3D.of(0.5, 0, 0));
        checkPoints(plus, RegionLocation.OUTSIDE,
                Vector3D.of(-0.5, 0, 0), Vector3D.of(1.5, 0, 0),
                Vector3D.of(0, 1.5, 0), Vector3D.of(0, -1.5, 0));
    }

    @Test
    public void testSplit_intersects_plusOnly() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, false);
        sp.getSubspaceRegion().union(RegionBSPTree2D.rect(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION));

        Plane splitter = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0.1, 0, 1), TEST_PRECISION);

        // act
        Split<SubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(sp, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_intersects_minusOnly() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, false);
        sp.getSubspaceRegion().union(RegionBSPTree2D.rect(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION));

        Plane splitter = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0.1, 0, -1), TEST_PRECISION);

        // act
        Split<SubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(sp, split.getPlus());
    }

    @Test
    public void testSplit_parallel_plusOnly() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, false);
        sp.getSubspaceRegion().union(RegionBSPTree2D.rect(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION));

        Plane splitter = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.PLUS_Z, TEST_PRECISION);

        // act
        Split<SubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(sp, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallel_minusOnly() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, false);
        sp.getSubspaceRegion().union(RegionBSPTree2D.rect(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION));

        Plane splitter = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.MINUS_Z, TEST_PRECISION);

        // act
        Split<SubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(sp, split.getPlus());
    }

    @Test
    public void testSplit_coincident() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, false);
        sp.getSubspaceRegion().union(RegionBSPTree2D.rect(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION));

        // act
        Split<SubPlane> split = sp.split(sp.getPlane());

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testTransform_empty() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, false);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.PLUS_Z);

        // act
        SubPlane result = sp.transform(transform);

        // assert
        Assert.assertNotSame(sp, result);

        Assert.assertFalse(result.isFull());
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testTransform_full() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, true);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.PLUS_Z);

        // act
        SubPlane result = sp.transform(transform);

        // assert
        Assert.assertNotSame(sp, result);

        Assert.assertTrue(result.isFull());
        Assert.assertFalse(result.isEmpty());
    }

    private static void checkPoints(SubPlane sp, RegionLocation loc, Vector3D ... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected subplane location for point " + pt, loc, sp.classify(pt));
        }
    }
}
