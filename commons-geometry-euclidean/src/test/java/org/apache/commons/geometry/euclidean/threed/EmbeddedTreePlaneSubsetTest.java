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
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.shape.Parallelogram;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddedTreePlaneSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final EmbeddingPlane XY_PLANE = Planes.fromPointAndPlaneVectors(Vector3D.ZERO,
            Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testCtor_plane() {
        // act
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE);

        // assert
        Assert.assertFalse(ps.isFull());
        Assert.assertTrue(ps.isEmpty());

        Assert.assertEquals(0, ps.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_plane_booleanFalse() {
        // act
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        // assert
        Assert.assertFalse(ps.isFull());
        Assert.assertTrue(ps.isEmpty());

        Assert.assertEquals(0, ps.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_plane_booleanTrue() {
        // act
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, true);

        // assert
        Assert.assertTrue(ps.isFull());
        Assert.assertFalse(ps.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(ps.getSize());
    }

    @Test
    public void testSpaceConversion() {
        // arrange
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(1, 0, 0),
                Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, true);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), ps.toSubspace(Vector3D.of(-5, 1, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -2, 4), ps.toSpace(Vector2D.of(-2, 4)), TEST_EPS);
    }

    @Test
    public void testToConvex_full() {
        // act
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, true);

        // act
        List<PlaneConvexSubset> convex = ps.toConvex();

        // assert
        Assert.assertEquals(1, convex.size());
        Assert.assertTrue(convex.get(0).isFull());
    }

    @Test
    public void testToConvex_empty() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        // act
        List<PlaneConvexSubset> convex = ps.toConvex();

        // assert
        Assert.assertEquals(0, convex.size());
    }

    @Test
    public void testToConvex_nonConvexRegion() {
        // act
        ConvexArea a = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(0, 0), Vector2D.of(1, 0),
                    Vector2D.of(1, 1), Vector2D.of(0, 1)
                ), TEST_PRECISION);
        ConvexArea b = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 0), Vector2D.of(2, 0),
                    Vector2D.of(2, 1), Vector2D.of(1, 1)
                ), TEST_PRECISION);

        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.add(Planes.subsetFromConvexArea(XY_PLANE, a));
        ps.add(Planes.subsetFromConvexArea(XY_PLANE, b));

        // act
        List<PlaneConvexSubset> convex = ps.toConvex();

        // assert
        Assert.assertEquals(2, convex.size());
        Assert.assertEquals(1, convex.get(0).getSize(), TEST_EPS);
        Assert.assertEquals(1, convex.get(1).getSize(), TEST_EPS);
    }

    @Test
    public void testToTriangles_empty() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        // act
        List<Triangle3D> tris = ps.toTriangles();

        // assert
        Assert.assertEquals(0, tris.size());
    }

    @Test
    public void testToTriangles_infinite() {
        // arrange
        Pattern pattern = Pattern.compile("^Cannot convert infinite plane subset to triangles: .*");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            new EmbeddedTreePlaneSubset(XY_PLANE, true).toTriangles();
        }, IllegalStateException.class, pattern);

        GeometryTestUtils.assertThrows(() -> {
            EmbeddedTreePlaneSubset halfSpace = new EmbeddedTreePlaneSubset(XY_PLANE, false);
            halfSpace.getSubspaceRegion().getRoot()
                .insertCut(Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION));

            halfSpace.toTriangles();
        }, IllegalStateException.class, pattern);

        GeometryTestUtils.assertThrows(() -> {
            RegionBSPTree2D tree = RegionBSPTree2D.empty();
            tree.insert(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION));
            tree.insert(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(0, 1), TEST_PRECISION));

            EmbeddedTreePlaneSubset halfSpaceWithVertices = new EmbeddedTreePlaneSubset(XY_PLANE, tree);

            halfSpaceWithVertices.toTriangles();
        }, IllegalStateException.class, pattern);
    }

    @Test
    public void testToTriangles_finite() {
        // arrange
        Vector3D p1 = Vector3D.ZERO;
        Vector3D p2 = Vector3D.of(1, 0, 0);
        Vector3D p3 = Vector3D.of(2, 1, 0);
        Vector3D p4 = Vector3D.of(1.5, 1, 0);

        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE);
        ps.add(Planes.convexPolygonFromVertices(Arrays.asList(
                    p1, p2, p3, p4
                ), TEST_PRECISION));

        // act
        List<Triangle3D> tris = ps.toTriangles();

        // assert
        Assert.assertEquals(2, tris.size());

        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p4, p1, p2),
                tris.get(0).getVertices(), TEST_PRECISION);
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p4, p2, p3),
                tris.get(1).getVertices(), TEST_PRECISION);
    }

    @Test
    public void testToTriangles_finite_disjoint() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE);
        ps.add(Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(1, 0, 0),
                    Vector3D.of(2, 1, 0), Vector3D.of(1.5, 1, 0)
                ), TEST_PRECISION));

        ps.add(Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(-1, -1, 0), Vector3D.of(0, -1, 0), Vector3D.of(-1, 0, 0)
            ), TEST_PRECISION));

        // act
        List<Triangle3D> tris = ps.toTriangles();

        // assert
        Assert.assertEquals(3, tris.size());
    }

    @Test
    public void testGetBounds_noBounds() {
        // arrange
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
                Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION);

        EmbeddedTreePlaneSubset full = new EmbeddedTreePlaneSubset(plane, true);
        EmbeddedTreePlaneSubset empty = new EmbeddedTreePlaneSubset(plane, false);

        EmbeddedTreePlaneSubset halfPlane = new EmbeddedTreePlaneSubset(plane, false);
        halfPlane.getSubspaceRegion().getRoot().insertCut(Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION));

        // act/assert
        Assert.assertNull(full.getBounds());
        Assert.assertNull(empty.getBounds());
        Assert.assertNull(halfPlane.getBounds());
    }

    @Test
    public void testGetBounds_hasBounds() {
        // arrange
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
                Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION);

        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, false);
        ps.getSubspaceRegion().add(ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 1), Vector2D.of(2, 1), Vector2D.of(1, 2)
                ), TEST_PRECISION));

        // act
        Bounds3D bounds = ps.getBounds();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-2, 1, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 2, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testSplit_empty() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_halfSpace() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().getRoot().cut(
                Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION));

        Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

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
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

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
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0.1, 0, 1), TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(ps, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_intersects_minusOnly() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0.1, 0, -1), TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(ps, split.getPlus());
    }

    @Test
    public void testSplit_parallel_plusOnly() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(ps, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallel_minusOnly() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act
        Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(ps, split.getPlus());
    }

    @Test
    public void testSplit_coincident() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        // act
        Split<EmbeddedTreePlaneSubset> split = ps.split(ps.getPlane());

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testTransform_empty() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.Unit.PLUS_Z);

        // act
        EmbeddedTreePlaneSubset result = ps.transform(transform);

        // assert
        Assert.assertNotSame(ps, result);

        Plane resultPlane = result.getPlane();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), resultPlane.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, resultPlane.getNormal(), TEST_EPS);

        Assert.assertFalse(result.isFull());
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testTransform_full() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, true);

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.Unit.PLUS_Z);

        // act
        EmbeddedTreePlaneSubset result = ps.transform(transform);

        // assert
        Assert.assertNotSame(ps, result);

        Plane resultPlane = result.getPlane();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), resultPlane.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, resultPlane.getNormal(), TEST_EPS);

        Assert.assertTrue(result.isFull());
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testTransform() {
        // arrange
        ConvexArea area = ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y), TEST_PRECISION);
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, area.toTree());

        Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO))
                .translate(Vector3D.of(1, 0, 0));

        // act
        EmbeddedTreePlaneSubset result = ps.transform(transform);

        // assert
        Assert.assertNotSame(ps, result);

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
        ConvexArea area = ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y), TEST_PRECISION);
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, area.toTree());

        Transform<Vector3D> transform = AffineTransformMatrix3D.createScale(-1, 1, 1);

        // act
        EmbeddedTreePlaneSubset result = ps.transform(transform);

        // assert
        Assert.assertNotSame(ps, result);

        Plane resultPlane = result.getPlane();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), resultPlane.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, resultPlane.getNormal(), TEST_EPS);

        checkPoints(result, RegionLocation.INSIDE, Vector3D.of(-0.25, 0.25, 1));
        checkPoints(result, RegionLocation.OUTSIDE, Vector3D.of(0.25, 0.25, 0), Vector3D.of(0.25, 0.25, 2));

        checkPoints(result, RegionLocation.BOUNDARY,
                Vector3D.of(-1, 0, 1), Vector3D.of(0, 1, 1), Vector3D.of(0, 0, 1));
    }

    @Test
    public void testAddMethods() {
        // arrange
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, false);

        // act
        ps.add(Planes.subsetFromConvexArea(plane, ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(0, 1)
                ), TEST_PRECISION)));

        RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.add(ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 0), Vector2D.of(1, 1), Vector2D.of(0, 1)
                ), TEST_PRECISION));
        ps.add(new EmbeddedTreePlaneSubset(plane, tree));

        // assert
        Assert.assertFalse(ps.isFull());
        Assert.assertFalse(ps.isEmpty());
        Assert.assertTrue(ps.isFinite());
        Assert.assertFalse(ps.isInfinite());

        Assert.assertEquals(1, ps.getSize(), TEST_EPS);

        checkPoints(ps, RegionLocation.INSIDE, Vector3D.of(0.5, 0.5, 1));
        checkPoints(ps, RegionLocation.BOUNDARY,
                Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1),
                Vector3D.of(1, 1, 1), Vector3D.of(0, 1, 1));
        checkPoints(ps, RegionLocation.OUTSIDE,
                Vector3D.of(0.5, 0.5, 0), Vector3D.of(0.5, 0.5, 2),
                Vector3D.of(-0.5, 0.5, 1), Vector3D.of(0.5, -0.5, 1),
                Vector3D.of(1.5, 0.5, 1), Vector3D.of(0.5, 1.5, 1));
    }

    @Test
    public void testAddMethods_rotatesEquivalentPlanesWithDifferentUAndV() {
        // arrange
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, false);

        EmbeddingPlane otherPlane1 = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.of(1e-12, 1, 0), Vector3D.Unit.MINUS_X, TEST_PRECISION);

        EmbeddingPlane otherPlane2 = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.of(0, -1, 1e-12), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(0, -1), Vector2D.of(1, -1), Vector2D.of(1, 1), Vector2D.of(0, 1)
                ), TEST_PRECISION);

        // act
        ps.add(Planes.subsetFromConvexArea(plane, area));
        ps.add(new EmbeddedTreePlaneSubset(otherPlane1, area.toTree()));
        ps.add(Planes.subsetFromConvexArea(otherPlane2, area));

        // assert
        Assert.assertEquals(4, ps.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), ps.getBarycenter(), TEST_EPS);

        Bounds3D bounds = ps.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, -1, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testAddMethods_rotatesEquivalentPlanesWithDifferentUAndV_singleConvexArea() {
        // arrange
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, false);

        EmbeddingPlane otherPlane1 = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.of(1e-12, 1, 0), Vector3D.Unit.MINUS_X, TEST_PRECISION);

        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, 2), Vector2D.of(0, 2)
                ), TEST_PRECISION);

        // act
        ps.add(Planes.subsetFromConvexArea(otherPlane1, area));

        // assert
        Assert.assertEquals(2, ps.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0.5, 1), ps.getBarycenter(), TEST_EPS);

        Bounds3D bounds = ps.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-2, 0, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testAddMethods_rotatesEquivalentPlanesWithDifferentUAndV_singleTree() {
        // arrange
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, false);

        EmbeddingPlane otherPlane1 = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_X, Vector3D.Unit.MINUS_Y, TEST_PRECISION);

        ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, 2), Vector2D.of(0, 2)
                ), TEST_PRECISION);

        // act
        ps.add(new EmbeddedTreePlaneSubset(otherPlane1, area.toTree()));

        // assert
        Assert.assertEquals(2, ps.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.5, -1, 1), ps.getBarycenter(), TEST_EPS);

        Bounds3D bounds = ps.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, -2, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testAddMethods_validatesPlane() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ps.add(Planes.subsetFromConvexArea(
                    Planes.fromPointAndPlaneVectors(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.MINUS_Z, TEST_PRECISION),
                    ConvexArea.full()));
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            ps.add(new EmbeddedTreePlaneSubset(
                    Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, -1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION),
                    false));
        }, IllegalArgumentException.class);
    }

    @Test
    public void testToString() {
        // arrange
        EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(
                Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION).getEmbedding());

        // act
        String str = ps.toString();

        // assert
        GeometryTestUtils.assertContains("EmbeddedTreePlaneSubset[plane= EmbeddingPlane[", str);
        GeometryTestUtils.assertContains("subspaceRegion= RegionBSPTree2D[", str);
    }

    private static void checkPoints(EmbeddedTreePlaneSubset ps, RegionLocation loc, Vector3D... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, ps.classify(pt));
        }
    }
}
