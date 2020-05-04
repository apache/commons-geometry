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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.HyperplaneBoundedRegion;
import org.apache.commons.geometry.core.partitioning.HyperplaneConvexSubset;
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.shapes.Parallelogram;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddedTreePlaneSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Plane XY_PLANE = Planes.fromPointAndPlaneVectors(Vector3D.ZERO,
            Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testCtor_plane() {
        // act
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertTrue(sp.isEmpty());

        Assert.assertEquals(0, sp.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_plane_booleanFalse() {
        // act
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertTrue(sp.isEmpty());

        Assert.assertEquals(0, sp.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_plane_booleanTrue() {
        // act
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, true);

        // assert
        Assert.assertTrue(sp.isFull());
        Assert.assertFalse(sp.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(sp.getSize());
    }

    @Test
    public void testToConvex_full() {
        // act
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, true);

        // act
        List<PlaneConvexSubset> convex = sp.toConvex();

        // assert
        Assert.assertEquals(1, convex.size());
        Assert.assertTrue(convex.get(0).isFull());
    }

    @Test
    public void testToConvex_empty() {
        // act
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        // act
        List<PlaneConvexSubset> convex = sp.toConvex();

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

        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        sp.add(Planes.subsetFromConvexArea(XY_PLANE, a));
        sp.add(Planes.subsetFromConvexArea(XY_PLANE, b));

        // act
        List<PlaneConvexSubset> convex = sp.toConvex();

        // assert
        Assert.assertEquals(2, convex.size());
        Assert.assertEquals(1, convex.get(0).getSize(), TEST_EPS);
        Assert.assertEquals(1, convex.get(1).getSize(), TEST_EPS);
    }

    @Test
    public void testSplit_empty() {
        // arrange
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_halfSpace() {
        // arrange
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        sp.getSubspaceRegion().getRoot().cut(
                Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION));

        Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        EmbeddedTreePlaneSubset minus = split.getMinus();
        checkPoints(minus, RegionLocation.INSIDE, Vector3D.of(-1, 1, 0));
        checkPoints(minus, RegionLocation.OUTSIDE, Vector3D.of(1, 1, 0), Vector3D.of(0, -1, 0));

        EmbeddedTreePlaneSubset plus = split.getPlus();
        checkPoints(plus, RegionLocation.OUTSIDE, Vector3D.of(-1, 1, 0), Vector3D.of(0, -1, 0));
        checkPoints(plus, RegionLocation.INSIDE, Vector3D.of(1, 1, 0));
    }

    @Test
    public void testSplit_both() {
        // arrange
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        sp.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        EmbeddedTreePlaneSubset minus = split.getMinus();
        checkPoints(minus, RegionLocation.INSIDE, Vector3D.of(-0.5, 0, 0));
        checkPoints(minus, RegionLocation.OUTSIDE,
                Vector3D.of(0.5, 0, 0), Vector3D.of(1.5, 0, 0),
                Vector3D.of(0, 1.5, 0), Vector3D.of(0, -1.5, 0));

        EmbeddedTreePlaneSubset plus = split.getPlus();
        checkPoints(plus, RegionLocation.INSIDE, Vector3D.of(0.5, 0, 0));
        checkPoints(plus, RegionLocation.OUTSIDE,
                Vector3D.of(-0.5, 0, 0), Vector3D.of(1.5, 0, 0),
                Vector3D.of(0, 1.5, 0), Vector3D.of(0, -1.5, 0));
    }

    @Test
    public void testSplit_intersects_plusOnly() {
        // arrange
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        sp.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0.1, 0, 1), TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(sp, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_intersects_minusOnly() {
        // arrange
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        sp.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0.1, 0, -1), TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(sp, split.getPlus());
    }

    @Test
    public void testSplit_parallel_plusOnly() {
        // arrange
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        sp.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(sp, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallel_minusOnly() {
        // arrange
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        sp.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(sp, split.getPlus());
    }

    @Test
    public void testSplit_coincident() {
        // arrange
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        sp.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        // act
        Split<EmbeddedTreePlaneSubset> split = sp.split(sp.getPlane());

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testTransform_empty() {
        // arrange
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.Unit.PLUS_Z);

        // act
        EmbeddedTreePlaneSubset result = sp.transform(transform);

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
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, true);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.Unit.PLUS_Z);

        // act
        EmbeddedTreePlaneSubset result = sp.transform(transform);

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
        Plane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(plane, area.toTree());

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO))
                .translate(Vector3D.of(1, 0, 0));

        // act
        EmbeddedTreePlaneSubset result = sp.transform(transform);

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
        Plane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(plane, area.toTree());

        Transform<Vector3D> transform = AffineTransformMatrix3D.createScale(-1, 1, 1);

        // act
        EmbeddedTreePlaneSubset result = sp.transform(transform);

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
    public void testToString() {
        // arrange
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION));

        // act
        String str = sp.toString();

        // assert
        GeometryTestUtils.assertContains("plane= Plane[", str);
        GeometryTestUtils.assertContains("subspaceRegion= RegionBSPTree2D[", str);
    }

    @Test
    public void testBuilder() {
        // arrange
        Plane mainPlane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        EmbeddedTreePlaneSubset.Builder builder = new EmbeddedTreePlaneSubset.Builder(mainPlane);

        ConvexArea a = ConvexArea.fromVertexLoop(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y), TEST_PRECISION);
        ConvexArea b = ConvexArea.fromVertexLoop(
                Arrays.asList(Vector2D.Unit.PLUS_X, Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y), TEST_PRECISION);

        Plane closePlane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(1e-16, 0, 1), Vector3D.of(1, 1e-16, 0), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        builder.add(Planes.subsetFromConvexArea(closePlane, a));
        builder.add(new EmbeddedTreePlaneSubset(closePlane, b.toTree()));

        EmbeddedTreePlaneSubset result = builder.build();

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
        EmbeddedTreePlaneSubset sp = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            sp.add(Planes.subsetFromConvexArea(
                    Planes.fromPointAndPlaneVectors(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION),
                    ConvexArea.full()));
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            sp.add(new EmbeddedTreePlaneSubset(
                    Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, -1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION),
                    false));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_addUnknownType() {
        // arrange
        EmbeddedTreePlaneSubset.Builder sp = new EmbeddedTreePlaneSubset.Builder(XY_PLANE);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            sp.add(new StubSubPlane(XY_PLANE));
        }, IllegalArgumentException.class);
    }

    private static void checkPoints(EmbeddedTreePlaneSubset sp, RegionLocation loc, Vector3D... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected subplane location for point " + pt, loc, sp.classify(pt));
        }
    }

    private static class StubSubPlane extends PlaneSubset implements HyperplaneSubset<Vector3D> {

        StubSubPlane(Plane plane) {
            super(plane);
        }

        @Override
        public Split<? extends HyperplaneSubset<Vector3D>> split(Hyperplane<Vector3D> splitter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HyperplaneSubset<Vector3D> transform(Transform<Vector3D> transform) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<? extends HyperplaneConvexSubset<Vector3D>> toConvex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HyperplaneBoundedRegion<Vector2D> getSubspaceRegion() {
            throw new UnsupportedOperationException();
        }
    }
}
