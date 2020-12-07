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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class EmbeddedTreePlaneSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final EmbeddingPlane XY_PLANE = Planes.fromPointAndPlaneVectors(Vector3D.ZERO,
            Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testCtor_plane() {
        // act
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE);

        // assert
        Assertions.assertFalse(ps.isFull());
        Assertions.assertTrue(ps.isEmpty());

        Assertions.assertEquals(0, ps.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_plane_booleanFalse() {
        // act
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        // assert
        Assertions.assertFalse(ps.isFull());
        Assertions.assertTrue(ps.isEmpty());

        Assertions.assertEquals(0, ps.getSize(), TEST_EPS);
    }

    @Test
    public void testCtor_plane_booleanTrue() {
        // act
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, true);

        // assert
        Assertions.assertTrue(ps.isFull());
        Assertions.assertFalse(ps.isEmpty());

        GeometryTestUtils.assertPositiveInfinity(ps.getSize());
    }

    @Test
    public void testSpaceConversion() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(1, 0, 0),
                Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, true);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), ps.toSubspace(Vector3D.of(-5, 1, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -2, 4), ps.toSpace(Vector2D.of(-2, 4)), TEST_EPS);
    }

    @Test
    public void testToConvex_full() {
        // act
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, true);

        // act
        final List<PlaneConvexSubset> convex = ps.toConvex();

        // assert
        Assertions.assertEquals(1, convex.size());
        Assertions.assertTrue(convex.get(0).isFull());
    }

    @Test
    public void testToConvex_empty() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        // act
        final List<PlaneConvexSubset> convex = ps.toConvex();

        // assert
        Assertions.assertEquals(0, convex.size());
    }

    @Test
    public void testToConvex_nonConvexRegion() {
        // act
        final ConvexArea a = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(0, 0), Vector2D.of(1, 0),
                    Vector2D.of(1, 1), Vector2D.of(0, 1)
                ), TEST_PRECISION);
        final ConvexArea b = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 0), Vector2D.of(2, 0),
                    Vector2D.of(2, 1), Vector2D.of(1, 1)
                ), TEST_PRECISION);

        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.add(Planes.subsetFromConvexArea(XY_PLANE, a));
        ps.add(Planes.subsetFromConvexArea(XY_PLANE, b));

        // act
        final List<PlaneConvexSubset> convex = ps.toConvex();

        // assert
        Assertions.assertEquals(2, convex.size());
        Assertions.assertEquals(1, convex.get(0).getSize(), TEST_EPS);
        Assertions.assertEquals(1, convex.get(1).getSize(), TEST_EPS);
    }

    @Test
    public void testToTriangles_empty() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        // act
        final List<Triangle3D> tris = ps.toTriangles();

        // assert
        Assertions.assertEquals(0, tris.size());
    }

    @Test
    public void testToTriangles_infinite() {
        // arrange
        final Pattern pattern = Pattern.compile("^Cannot convert infinite plane subset to triangles: .*");

        // act/assert
        assertThrows(IllegalStateException.class, () -> new EmbeddedTreePlaneSubset(XY_PLANE, true).toTriangles());
        final EmbeddedTreePlaneSubset halfSpace = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        halfSpace.getSubspaceRegion().getRoot()
                .insertCut(Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION));
        assertThrows(IllegalStateException.class, halfSpace::toTriangles);

        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.insert(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(1, 0), TEST_PRECISION));
        tree.insert(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(0, 1), TEST_PRECISION));

        final EmbeddedTreePlaneSubset halfSpaceWithVertices = new EmbeddedTreePlaneSubset(XY_PLANE, tree);
        assertThrows(IllegalStateException.class, halfSpaceWithVertices::toTriangles);
    }

    @Test
    public void testToTriangles_finite() {
        // arrange
        final Vector3D p1 = Vector3D.ZERO;
        final Vector3D p2 = Vector3D.of(1, 0, 0);
        final Vector3D p3 = Vector3D.of(2, 1, 0);
        final Vector3D p4 = Vector3D.of(1.5, 1, 0);

        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE);
        ps.add(Planes.convexPolygonFromVertices(Arrays.asList(
                    p1, p2, p3, p4
                ), TEST_PRECISION));

        // act
        final List<Triangle3D> tris = ps.toTriangles();

        // assert
        Assertions.assertEquals(2, tris.size());

        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p4, p1, p2),
                tris.get(0).getVertices(), TEST_PRECISION);
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p4, p2, p3),
                tris.get(1).getVertices(), TEST_PRECISION);
    }

    @Test
    public void testToTriangles_finite_disjoint() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE);
        ps.add(Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.ZERO, Vector3D.of(1, 0, 0),
                    Vector3D.of(2, 1, 0), Vector3D.of(1.5, 1, 0)
                ), TEST_PRECISION));

        ps.add(Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(-1, -1, 0), Vector3D.of(0, -1, 0), Vector3D.of(-1, 0, 0)
            ), TEST_PRECISION));

        // act
        final List<Triangle3D> tris = ps.toTriangles();

        // assert
        Assertions.assertEquals(3, tris.size());
    }

    @Test
    public void testGetBounds_noBounds() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
                Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION);

        final EmbeddedTreePlaneSubset full = new EmbeddedTreePlaneSubset(plane, true);
        final EmbeddedTreePlaneSubset empty = new EmbeddedTreePlaneSubset(plane, false);

        final EmbeddedTreePlaneSubset halfPlane = new EmbeddedTreePlaneSubset(plane, false);
        halfPlane.getSubspaceRegion().getRoot().insertCut(Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION));

        // act/assert
        Assertions.assertNull(full.getBounds());
        Assertions.assertNull(empty.getBounds());
        Assertions.assertNull(halfPlane.getBounds());
    }

    @Test
    public void testGetBounds_hasBounds() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
                Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION);

        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, false);
        ps.getSubspaceRegion().add(ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 1), Vector2D.of(2, 1), Vector2D.of(1, 2)
                ), TEST_PRECISION));

        // act
        final Bounds3D bounds = ps.getBounds();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-2, 1, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 2, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testSplit_empty() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        final Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_halfSpace() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().getRoot().cut(
                Lines.fromPointAndAngle(Vector2D.ZERO, 0.0, TEST_PRECISION));

        final Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final EmbeddedTreePlaneSubset minus = split.getMinus();
        checkPoints(minus, RegionLocation.INSIDE, Vector3D.of(-1, 1, 0));
        checkPoints(minus, RegionLocation.OUTSIDE, Vector3D.of(1, 1, 0), Vector3D.of(0, -1, 0));

        final EmbeddedTreePlaneSubset plus = split.getPlus();
        checkPoints(plus, RegionLocation.OUTSIDE, Vector3D.of(-1, 1, 0), Vector3D.of(0, -1, 0));
        checkPoints(plus, RegionLocation.INSIDE, Vector3D.of(1, 1, 0));
    }

    @Test
    public void testSplit_both() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        final Plane splitter = Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final EmbeddedTreePlaneSubset minus = split.getMinus();
        checkPoints(minus, RegionLocation.INSIDE, Vector3D.of(-0.5, 0, 0));
        checkPoints(minus, RegionLocation.OUTSIDE,
                Vector3D.of(0.5, 0, 0), Vector3D.of(1.5, 0, 0),
                Vector3D.of(0, 1.5, 0), Vector3D.of(0, -1.5, 0));

        final EmbeddedTreePlaneSubset plus = split.getPlus();
        checkPoints(plus, RegionLocation.INSIDE, Vector3D.of(0.5, 0, 0));
        checkPoints(plus, RegionLocation.OUTSIDE,
                Vector3D.of(-0.5, 0, 0), Vector3D.of(1.5, 0, 0),
                Vector3D.of(0, 1.5, 0), Vector3D.of(0, -1.5, 0));
    }

    @Test
    public void testSplit_intersects_plusOnly() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0.1, 0, 1), TEST_PRECISION);

        // act
        final Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assertions.assertSame(ps, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_intersects_minusOnly() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0.1, 0, -1), TEST_PRECISION);

        // act
        final Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(ps, split.getPlus());
    }

    @Test
    public void testSplit_parallel_plusOnly() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assertions.assertSame(ps, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallel_minusOnly() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act
        final Split<EmbeddedTreePlaneSubset> split = ps.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(ps, split.getPlus());
    }

    @Test
    public void testSplit_coincident() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);
        ps.getSubspaceRegion().union(
                Parallelogram.axisAligned(Vector2D.of(-1, -1), Vector2D.of(1, 1), TEST_PRECISION).toTree());

        // act
        final Split<EmbeddedTreePlaneSubset> split = ps.split(ps.getPlane());

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testTransform_empty() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.Unit.PLUS_Z);

        // act
        final EmbeddedTreePlaneSubset result = ps.transform(transform);

        // assert
        Assertions.assertNotSame(ps, result);

        final Plane resultPlane = result.getPlane();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), resultPlane.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, resultPlane.getNormal(), TEST_EPS);

        Assertions.assertFalse(result.isFull());
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testTransform_full() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, true);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.Unit.PLUS_Z);

        // act
        final EmbeddedTreePlaneSubset result = ps.transform(transform);

        // assert
        Assertions.assertNotSame(ps, result);

        final Plane resultPlane = result.getPlane();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), resultPlane.getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, resultPlane.getNormal(), TEST_EPS);

        Assertions.assertTrue(result.isFull());
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    public void testTransform() {
        // arrange
        final ConvexArea area = ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y), TEST_PRECISION);
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, area.toTree());

        final Transform<Vector3D> transform = AffineTransformMatrix3D.identity()
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO))
                .translate(Vector3D.of(1, 0, 0));

        // act
        final EmbeddedTreePlaneSubset result = ps.transform(transform);

        // assert
        Assertions.assertNotSame(ps, result);

        final Plane resultPlane = result.getPlane();
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
        final ConvexArea area = ConvexArea.convexPolygonFromVertices(
                Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y), TEST_PRECISION);
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, area.toTree());

        final Transform<Vector3D> transform = AffineTransformMatrix3D.createScale(-1, 1, 1);

        // act
        final EmbeddedTreePlaneSubset result = ps.transform(transform);

        // assert
        Assertions.assertNotSame(ps, result);

        final Plane resultPlane = result.getPlane();
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
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, false);

        // act
        ps.add(Planes.subsetFromConvexArea(plane, ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(0, 1)
                ), TEST_PRECISION)));

        final RegionBSPTree2D tree = RegionBSPTree2D.empty();
        tree.add(ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 0), Vector2D.of(1, 1), Vector2D.of(0, 1)
                ), TEST_PRECISION));
        ps.add(new EmbeddedTreePlaneSubset(plane, tree));

        // assert
        Assertions.assertFalse(ps.isFull());
        Assertions.assertFalse(ps.isEmpty());
        Assertions.assertTrue(ps.isFinite());
        Assertions.assertFalse(ps.isInfinite());

        Assertions.assertEquals(1, ps.getSize(), TEST_EPS);

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
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, false);

        final EmbeddingPlane otherPlane1 = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.of(1e-12, 1, 0), Vector3D.Unit.MINUS_X, TEST_PRECISION);

        final EmbeddingPlane otherPlane2 = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.of(0, -1, 1e-12), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(0, -1), Vector2D.of(1, -1), Vector2D.of(1, 1), Vector2D.of(0, 1)
                ), TEST_PRECISION);

        // act
        ps.add(Planes.subsetFromConvexArea(plane, area));
        ps.add(new EmbeddedTreePlaneSubset(otherPlane1, area.toTree()));
        ps.add(Planes.subsetFromConvexArea(otherPlane2, area));

        // assert
        Assertions.assertEquals(4, ps.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), ps.getCentroid(), TEST_EPS);

        final Bounds3D bounds = ps.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, -1, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testAddMethods_rotatesEquivalentPlanesWithDifferentUAndV_singleConvexArea() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, false);

        final EmbeddingPlane otherPlane1 = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.of(1e-12, 1, 0), Vector3D.Unit.MINUS_X, TEST_PRECISION);

        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, 2), Vector2D.of(0, 2)
                ), TEST_PRECISION);

        // act
        ps.add(Planes.subsetFromConvexArea(otherPlane1, area));

        // assert
        Assertions.assertEquals(2, ps.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0.5, 1), ps.getCentroid(), TEST_EPS);

        final Bounds3D bounds = ps.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-2, 0, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testAddMethods_rotatesEquivalentPlanesWithDifferentUAndV_singleTree() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(plane, false);

        final EmbeddingPlane otherPlane1 = Planes.fromPointAndPlaneVectors(
                Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_X, Vector3D.Unit.MINUS_Y, TEST_PRECISION);

        final ConvexArea area = ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, 2), Vector2D.of(0, 2)
                ), TEST_PRECISION);

        // act
        ps.add(new EmbeddedTreePlaneSubset(otherPlane1, area.toTree()));

        // assert
        Assertions.assertEquals(2, ps.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-0.5, -1, 1), ps.getCentroid(), TEST_EPS);

        final Bounds3D bounds = ps.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, -2, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testAddMethods_validatesPlane() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(XY_PLANE, false);

        // act/assert
        assertThrows(IllegalArgumentException.class, () -> ps.add(Planes.subsetFromConvexArea(
                Planes.fromPointAndPlaneVectors(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.MINUS_Z, TEST_PRECISION),
                ConvexArea.full())));
        assertThrows(IllegalArgumentException.class, () -> ps.add(new EmbeddedTreePlaneSubset(
                Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, -1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION),
                false)));
    }

    @Test
    public void testToString() {
        // arrange
        final EmbeddedTreePlaneSubset ps = new EmbeddedTreePlaneSubset(
                Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION).getEmbedding());

        // act
        final String str = ps.toString();

        // assert
        GeometryTestUtils.assertContains("EmbeddedTreePlaneSubset[plane= EmbeddingPlane[", str);
        GeometryTestUtils.assertContains("subspaceRegion= RegionBSPTree2D[", str);
    }

    private static void checkPoints(final EmbeddedTreePlaneSubset ps, final RegionLocation loc, final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(loc, ps.classify(pt), "Unexpected location for point " + pt);
        }
    }
}
