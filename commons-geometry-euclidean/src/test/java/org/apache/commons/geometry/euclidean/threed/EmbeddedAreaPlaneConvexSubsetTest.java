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
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.geometry.euclidean.twod.shape.Parallelogram;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class EmbeddedAreaPlaneConvexSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final EmbeddingPlane XY_PLANE_Z1 = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
            Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

    @Test
    public void testSpaceConversion() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(1, 0, 0),
                Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(plane, ConvexArea.full());

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), ps.toSubspace(Vector3D.of(-5, 1, 2)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -2, 4), ps.toSpace(Vector2D.of(-2, 4)), TEST_EPS);
    }

    @Test
    public void testProperties_infinite() {
        // arrange
        final ConvexArea area = ConvexArea.full();

        // act
        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1, area);

        // assert
        Assert.assertTrue(ps.isFull());
        Assert.assertFalse(ps.isEmpty());
        Assert.assertFalse(ps.isFinite());
        Assert.assertTrue(ps.isInfinite());

        GeometryTestUtils.assertPositiveInfinity(ps.getSize());

        Assert.assertSame(XY_PLANE_Z1, ps.getPlane());
        Assert.assertSame(area, ps.getSubspaceRegion());

        Assert.assertEquals(0, ps.getVertices().size());
    }

    @Test
    public void testProperties_finite() {
        // arrange
        final ConvexArea area = ConvexArea.convexPolygonFromPath(LinePath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(0, 1))
                .build(true));

        // act
        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1, area);

        // assert
        Assert.assertFalse(ps.isFull());
        Assert.assertFalse(ps.isEmpty());
        Assert.assertTrue(ps.isFinite());
        Assert.assertFalse(ps.isInfinite());

        Assert.assertEquals(0.5, ps.getSize(), TEST_EPS);

        Assert.assertSame(XY_PLANE_Z1, ps.getPlane());
        Assert.assertSame(area, ps.getSubspaceRegion());

        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1)),
                ps.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testGetVertices_twoParallelLines() {
        // arrange
        final EmbeddingPlane plane = Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION).getEmbedding();
        final PlaneConvexSubset sp = new EmbeddedAreaPlaneConvexSubset(plane, ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(0, -1), 0.0, TEST_PRECISION)
                ));

        // act
        final List<Vector3D> vertices = sp.getVertices();

        // assert
        Assert.assertEquals(0, vertices.size());
    }

    @Test
    public void testGetVertices_infiniteWithVertices() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        final PlaneConvexSubset sp = new EmbeddedAreaPlaneConvexSubset(plane, ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.of(0, 1), PlaneAngleRadians.PI, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(0, -1), 0.0, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(1, 0), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
                ));

        // act
        final List<Vector3D> vertices = sp.getVertices();

        // assert
        Assert.assertEquals(2, vertices.size());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, 1), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), vertices.get(1), TEST_EPS);
    }
    @Test
    public void testToTriangles_infinite() {
        // arrange
        final Pattern pattern = Pattern.compile("^Cannot convert infinite plane subset to triangles: .*");

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1, ConvexArea.full()).toTriangles();
        }, IllegalStateException.class, pattern);

        GeometryTestUtils.assertThrows(() -> {
            final ConvexArea area = ConvexArea.fromBounds(Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION));
            final EmbeddedAreaPlaneConvexSubset halfSpace = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1, area);

            halfSpace.toTriangles();
        }, IllegalStateException.class, pattern);

        GeometryTestUtils.assertThrows(() -> {
            final ConvexArea area = ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.ZERO, 0.5 * Math.PI, TEST_PRECISION));

            final EmbeddedAreaPlaneConvexSubset halfSpaceWithVertices = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1, area);

            halfSpaceWithVertices.toTriangles();
        }, IllegalStateException.class, pattern);
    }

    @Test
    public void testToTriangles_finite() {
        // arrange
        final Vector3D p1 = Vector3D.of(0, 0, 1);
        final Vector3D p2 = Vector3D.of(1, 0, 1);
        final Vector3D p3 = Vector3D.of(2, 1, 1);
        final Vector3D p4 = Vector3D.of(1.5, 1, 1);

        final List<Vector2D> subPts = XY_PLANE_Z1.toSubspace(Arrays.asList(p1, p2, p3, p4));

        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1,
                ConvexArea.convexPolygonFromVertices(subPts, TEST_PRECISION));

        // act
        final List<Triangle3D> tris = ps.toTriangles();

        // assert
        Assert.assertEquals(2, tris.size());

        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p4, p1, p2),
                tris.get(0).getVertices(), TEST_PRECISION);
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(p4, p2, p3),
                tris.get(1).getVertices(), TEST_PRECISION);
    }

    @Test
    public void testClassify() {
        // arrange
        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1,
                Parallelogram.builder(TEST_PRECISION)
                    .setPosition(Vector2D.of(2, 3))
                    .setScale(2, 2)
                    .build());

        // act/assert
        checkPoints(ps, RegionLocation.INSIDE, Vector3D.of(2, 3, 1));
        checkPoints(ps, RegionLocation.BOUNDARY,
                Vector3D.of(1, 3, 1), Vector3D.of(3, 3, 1),
                Vector3D.of(2, 2, 1), Vector3D.of(2, 4, 1));
        checkPoints(ps, RegionLocation.OUTSIDE,
                Vector3D.of(2, 3, 0), Vector3D.of(2, 3, 2),
                Vector3D.of(0, 3, 1), Vector3D.of(4, 3, 1),
                Vector3D.of(2, 1, 1), Vector3D.of(2, 5, 1));
    }

    @Test
    public void testClosest() {
        // arrange
        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1,
                Parallelogram.builder(TEST_PRECISION)
                    .setPosition(Vector2D.of(2, 3))
                    .setScale(2, 2)
                    .build());

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 3, 1), ps.closest(Vector3D.of(2, 3, 1)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 3, 1), ps.closest(Vector3D.of(2, 3, 100)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 1),
                ps.closest(Vector3D.of(-100, -100, -100)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 3.5, 1),
                ps.closest(Vector3D.of(100, 3.5, 100)), TEST_EPS);
    }

    @Test
    public void testGetBounds_noBounds() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
                Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION);

        final EmbeddedAreaPlaneConvexSubset full = new EmbeddedAreaPlaneConvexSubset(plane, ConvexArea.full());
        final EmbeddedAreaPlaneConvexSubset halfPlane = new EmbeddedAreaPlaneConvexSubset(plane,
                ConvexArea.fromBounds(Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION)));

        // act/assert
        Assert.assertNull(full.getBounds());
        Assert.assertNull(halfPlane.getBounds());
    }

    @Test
    public void testGetBounds_hasBounds() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
                Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION);

        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(plane,
                ConvexArea.convexPolygonFromVertices(Arrays.asList(
                    Vector2D.of(1, 1), Vector2D.of(2, 1), Vector2D.of(1, 2)
                ), TEST_PRECISION));

        // act
        final Bounds3D bounds = ps.getBounds();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-2, 1, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 2, 1), bounds.getMax(), TEST_EPS);
    }

    @Test
    public void testTransform() {
        // arrange
        final AffineTransformMatrix3D t = AffineTransformMatrix3D.identity()
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, -PlaneAngleRadians.PI_OVER_TWO))
                .scale(1, 1, 2)
                .translate(Vector3D.of(1, 0, 0));

        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1,
                Parallelogram.builder(TEST_PRECISION)
                    .setPosition(Vector2D.of(2, 3))
                    .setScale(2, 2)
                    .build());

        // act
        final EmbeddedAreaPlaneConvexSubset result = ps.transform(t);

        // assert
        Assert.assertFalse(result.isFull());
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isFinite());
        Assert.assertFalse(result.isInfinite());

        Assert.assertEquals(8, result.getSize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_X, result.getPlane().getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z, result.getPlane().getU(), TEST_EPS);

        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(0, 2, 2), Vector3D.of(0, 2, 6), Vector3D.of(0, 4, 6), Vector3D.of(0, 4, 2)),
                result.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testReverse() {
        // arrange
        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1,
                Parallelogram.builder(TEST_PRECISION)
                    .setPosition(Vector2D.of(2, 3))
                    .setScale(2, 2)
                    .build());

        // act
        final EmbeddedAreaPlaneConvexSubset result = ps.reverse();

        // assert
        Assert.assertFalse(result.isFull());
        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.isFinite());
        Assert.assertFalse(result.isInfinite());

        Assert.assertEquals(4, result.getSize(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, result.getPlane().getNormal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Y, result.getPlane().getU(), TEST_EPS);

        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(1, 4, 1), Vector3D.of(3, 4, 1), Vector3D.of(3, 2, 1), Vector3D.of(1, 2, 1)),
                result.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testSplit_plus() {
        // arrange
        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1,
                ConvexArea.convexPolygonFromVertices(Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(0, 1)),
                        TEST_PRECISION));

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = ps.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(ps, split.getPlus());
    }

    @Test
    public void testSplit_minus() {
        // arrange
        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1,
                ConvexArea.convexPolygonFromVertices(Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(0, 1)),
                        TEST_PRECISION));

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = ps.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(ps, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_both() {
        // arrange
        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1,
                ConvexArea.convexPolygonFromVertices(Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(0, 1)),
                        TEST_PRECISION));

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(-1, 1, 0), TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = ps.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        final PlaneConvexSubset minus = split.getMinus();
        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(0, 0, 1), Vector3D.of(1, 0, 1), Vector3D.of(0.5, 0.5, 1)),
                minus.getVertices(), TEST_PRECISION);

        final PlaneConvexSubset plus = split.getPlus();
        EuclideanTestUtils.assertVertexLoopSequence(
                Arrays.asList(Vector3D.of(0, 0, 1), Vector3D.of(0.5, 0.5, 1), Vector3D.of(0, 1, 1)),
                plus.getVertices(), TEST_PRECISION);
    }

    @Test
    public void testSplit_neither() {
        // arrange
        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1,
                ConvexArea.convexPolygonFromVertices(Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(0, 1)),
                        TEST_PRECISION));

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 1e-15, -1), TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = ps.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_usesVertexBasedSubsetsWhenPossible() {
        // arrange
        // create an infinite subset
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.ZERO,
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(plane, ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(1, 0), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(0, 1), -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
                ));

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0.5, 0.5, 0), Vector3D.of(-1, 1, 0), TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = ps.split(splitter);

        // assert
        Assert.assertTrue(ps.isInfinite());

        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        final PlaneConvexSubset plus = split.getPlus();
        Assert.assertNotNull(plus);
        Assert.assertTrue(plus.isInfinite());
        Assert.assertTrue(plus instanceof EmbeddedAreaPlaneConvexSubset);

        final PlaneConvexSubset minus = split.getMinus();
        Assert.assertNotNull(minus);
        Assert.assertFalse(minus.isInfinite());
        Assert.assertTrue(minus instanceof SimpleTriangle3D);
    }

    @Test
    public void testToString() {
        // arrange
        final EmbeddedAreaPlaneConvexSubset ps = new EmbeddedAreaPlaneConvexSubset(XY_PLANE_Z1,
                ConvexArea.convexPolygonFromVertices(Arrays.asList(Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(0, 1)),
                        TEST_PRECISION));

        // act
        final String str = ps.toString();

        // assert
        GeometryTestUtils.assertContains("EmbeddedAreaPlaneConvexSubset[plane= EmbeddingPlane[", str);
        GeometryTestUtils.assertContains("subspaceRegion= ConvexArea[", str);
    }

    private static void checkPoints(final EmbeddedAreaPlaneConvexSubset ps, final RegionLocation loc, final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, ps.classify(pt));
        }
    }
}
