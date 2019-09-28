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
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.junit.Assert;
import org.junit.Test;

public class SubPlaneTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Plane XY_PLANE = Plane.fromPointAndPlaneVectors(Vector3D.ZERO,
            Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

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

        Plane splitter = Plane.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

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

        Plane splitter = Plane.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

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
        sp.getSubspaceRegion().union(RegionBSPTree2D.builder(TEST_PRECISION).addRect(Vector2D.of(-1, -1), Vector2D.of(1, 1)).build());

        Plane splitter = Plane.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

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
        sp.getSubspaceRegion().union(RegionBSPTree2D.builder(TEST_PRECISION).addRect(Vector2D.of(-1, -1), Vector2D.of(1, 1)).build());

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
        sp.getSubspaceRegion().union(RegionBSPTree2D.builder(TEST_PRECISION).addRect(Vector2D.of(-1, -1), Vector2D.of(1, 1)).build());

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
        sp.getSubspaceRegion().union(RegionBSPTree2D.builder(TEST_PRECISION).addRect(Vector2D.of(-1, -1), Vector2D.of(1, 1)).build());

        Plane splitter = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

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
        sp.getSubspaceRegion().union(RegionBSPTree2D.builder(TEST_PRECISION).addRect(Vector2D.of(-1, -1), Vector2D.of(1, 1)).build());

        Plane splitter = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_Z, TEST_PRECISION);

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
        sp.getSubspaceRegion().union(RegionBSPTree2D.builder(TEST_PRECISION).addRect(Vector2D.of(-1, -1), Vector2D.of(1, 1)).build());

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

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.Unit.PLUS_Z);

        // act
        SubPlane result = sp.transform(transform);

        // assert
        Assert.assertNotSame(sp, result);

        Plane resultPlane = result.getPlane();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), resultPlane.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, resultPlane.getNormal(), TEST_EPS);

        Assert.assertFalse(result.isFull());
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testTransform_full() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, true);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.Unit.PLUS_Z);

        // act
        SubPlane result = sp.transform(transform);

        // assert
        Assert.assertNotSame(sp, result);

        Plane resultPlane = result.getPlane();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), resultPlane.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, resultPlane.getNormal(), TEST_EPS);

        Assert.assertTrue(result.isFull());
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testTransform() {
        // arrange
        ConvexArea area = ConvexArea.fromVertexLoop(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y), TEST_PRECISION);
        Plane plane = Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        SubPlane sp = new SubPlane(plane, RegionBSPTree2D.fromConvexArea(area));

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Geometry.HALF_PI))
                .translate(Vector3D.of(1, 0, 0));

        // act
        SubPlane result = sp.transform(transform);

        // assert
        Assert.assertNotSame(sp, result);

        Plane resultPlane = result.getPlane();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 0, 0), resultPlane.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X, resultPlane.getNormal(), TEST_EPS);

        checkPoints(result, RegionLocation.INSIDE, Vector3D.of(2, 0.25, -0.25));
        checkPoints(result, RegionLocation.OUTSIDE, Vector3D.of(1, 0.25, -0.25), Vector3D.of(3, 0.25, -0.25));

        checkPoints(result, RegionLocation.BOUNDARY,
                Vector3D.of(2, 0, 0), Vector3D.of(2, 0, -1), Vector3D.of(2, 1, 0));
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        ConvexArea area = ConvexArea.fromVertexLoop(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y), TEST_PRECISION);
        Plane plane = Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        SubPlane sp = new SubPlane(plane, RegionBSPTree2D.fromConvexArea(area));

        Transform<Vector3D> transform = AffineTransformMatrix3D.createScale(-1, 1, 1);

        // act
        SubPlane result = sp.transform(transform);

        // assert
        Assert.assertNotSame(sp, result);

        Plane resultPlane = result.getPlane();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), resultPlane.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, resultPlane.getNormal(), TEST_EPS);

        checkPoints(result, RegionLocation.INSIDE, Vector3D.of(-0.25, 0.25, 1));
        checkPoints(result, RegionLocation.OUTSIDE, Vector3D.of(0.25, 0.25, 0), Vector3D.of(0.25, 0.25, 2));

        checkPoints(result, RegionLocation.BOUNDARY,
                Vector3D.of(-1, 0, 1), Vector3D.of(0, 1, 1), Vector3D.of(0, 0, 1));
    }

    @Test
    public void testBuilder() {
        // arrange
        Plane mainPlane = Plane.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        SubPlane.SubPlaneBuilder builder = new SubPlane.SubPlaneBuilder(mainPlane);

        ConvexArea a = ConvexArea.fromVertexLoop(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y), TEST_PRECISION);
        ConvexArea b = ConvexArea.fromVertexLoop(
                Arrays.asList(Vector2D.Unit.PLUS_X, Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y), TEST_PRECISION);

        Plane closePlane = Plane.fromPointAndPlaneVectors(
                Vector3D.of(1e-16, 0, 1), Vector3D.of(1, 1e-16, 0), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        builder.add(ConvexSubPlane.fromConvexArea(closePlane, a));
        builder.add(new SubPlane(closePlane, RegionBSPTree2D.fromConvexArea(b)));

        SubPlane result = builder.build();

        // assert
        Assert.assertFalse(result.isFull());
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isFinite());
        Assert.assertFalse(result.isInfinite());

        checkPoints(result, RegionLocation.INSIDE, Vector3D.of(0.5, 0.5, 1));
        checkPoints(result, RegionLocation.OUTSIDE,
                Vector3D.of(-1, 0.5, 1), Vector3D.of(2, 0.5, 1),
                Vector3D.of(0.5, -1, 1), Vector3D.of(0.5, 2, 1));
        checkPoints(result, RegionLocation.BOUNDARY,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1),
                Vector3D.of(1, 1, 1), Vector3D.of(0, 1, 1));
    }

    @Test
    public void testSubPlaneAddMethods_validatesPlane() {
        // arrange
        SubPlane sp = new SubPlane(XY_PLANE, false);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            sp.add(ConvexSubPlane.fromConvexArea(
                    Plane.fromPointAndPlaneVectors(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION),
                    ConvexArea.full()));
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            sp.add(new SubPlane(
                    Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, -1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION),
                    false));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_addUnknownType() {
        // arrange
        SubPlane.SubPlaneBuilder sp = new SubPlane.SubPlaneBuilder(XY_PLANE);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            sp.add(new StubSubPlane(XY_PLANE));
        }, IllegalArgumentException.class);
    }

    private static void checkPoints(SubPlane sp, RegionLocation loc, Vector3D ... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected subplane location for point " + pt, loc, sp.classify(pt));
        }
    }

    private static class StubSubPlane extends AbstractSubPlane<RegionBSPTree2D> implements SubHyperplane<Vector3D> {

        private static final long serialVersionUID = 1L;

        StubSubPlane(Plane plane) {
            super(plane);
        }

        @Override
        public Split<? extends SubHyperplane<Vector3D>> split(Hyperplane<Vector3D> splitter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SubHyperplane<Vector3D> transform(Transform<Vector3D> transform) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<? extends ConvexSubHyperplane<Vector3D>> toConvex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HyperplaneBoundedRegion<Vector2D> getSubspaceRegion() {
            throw new UnsupportedOperationException();
        }
    }
}
