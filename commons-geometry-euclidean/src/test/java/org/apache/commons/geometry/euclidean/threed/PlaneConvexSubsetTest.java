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

import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class PlaneConvexSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testToConvex() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.Unit.PLUS_X,  Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z), TEST_PRECISION);

        // act
        List<PlaneConvexSubset> convex = sp.toConvex();

        // assert
        Assert.assertEquals(1, convex.size());
        Assert.assertSame(sp, convex.get(0));
    }

    @Test
    public void testReverse() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 0, 1);
        Vector3D p2 = Vector3D.of(2, 0, 1);
        Vector3D p3 = Vector3D.of(1, 1, 1);

        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(p1, p2, p3), TEST_PRECISION);

        // act
        PlaneConvexSubset reversed = sp.reverse();

        // assert
        Assert.assertEquals(sp.getPlane().reverse(), reversed.getPlane());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, reversed.getPlane().getNormal(), TEST_EPS);

        Assert.assertEquals(0.5, reversed.getSize(), TEST_EPS);

        checkVertices(reversed, p1, p3, p2);

        checkPoints(reversed, RegionLocation.INSIDE, Vector3D.of(1.25, 0.25, 1));

        checkPoints(reversed, RegionLocation.BOUNDARY, p1, p2, p3);
    }

    @Test
    public void testTransform_full() {
        // arrange
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        PlaneConvexSubset sp = Planes.subsetFromConvexArea(plane, ConvexArea.full());

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, PlaneAngleRadians.PI_OVER_TWO))
                .translate(Vector3D.Unit.PLUS_Y);

        // act
        PlaneConvexSubset transformed = sp.transform(transform);

        // assert
        Assert.assertTrue(transformed.isFull());
        Assert.assertFalse(transformed.isEmpty());

        checkPlane(transformed.getPlane(), Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z);
    }

    @Test
    public void testTransform_halfSpace() {
        // arrange
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        PlaneConvexSubset sp = Planes.subsetFromConvexArea(plane,
                ConvexArea.fromBounds(Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION)));

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createRotation(Vector3D.Unit.PLUS_Z,
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO));

        // act
        PlaneConvexSubset transformed = sp.transform(transform);

        // assert
        Assert.assertFalse(transformed.isFull());
        Assert.assertFalse(transformed.isEmpty());

        checkPlane(transformed.getPlane(), Vector3D.ZERO, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Y);
    }

    @Test
    public void testTransform_finite() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), Vector3D.of(0, 0, 1)), TEST_PRECISION);

        Transform<Vector3D> transform = AffineTransformMatrix3D.createScale(2)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO));

        // act
        PlaneConvexSubset transformed = sp.transform(transform);

        // assert
        Vector3D midpt = Vector3D.of(2, 2, -2).multiply(1 / 3.0);
        Vector3D normal = midpt.normalize();
        Vector3D u = Vector3D.of(0, 2, 2).normalize();

        checkPlane(transformed.getPlane(), midpt, u, normal.cross(u));

        checkVertices(transformed, Vector3D.of(0, 0, -2), Vector3D.of(0, 2, 0), Vector3D.of(2, 0, 0));

        checkPoints(transformed, RegionLocation.INSIDE, midpt);
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), Vector3D.of(0, 0, 1)), TEST_PRECISION);

        Transform<Vector3D> transform = AffineTransformMatrix3D.createScale(-1, 1, 1);

        // act
        PlaneConvexSubset transformed = sp.transform(transform);

        // assert
        Vector3D midpt = Vector3D.of(-1, 1, 1).multiply(1 / 3.0);
        Vector3D normal = midpt.negate().normalize();
        Vector3D u = Vector3D.of(1, 1, 0).normalize();

        checkPlane(transformed.getPlane(), midpt, u, normal.cross(u));

        checkVertices(transformed, Vector3D.of(-1, 0, 0), Vector3D.of(0, 1, 0), Vector3D.of(0, 0, 1));

        checkPoints(transformed, RegionLocation.INSIDE, Vector3D.of(-1, 1, 1).multiply(1 / 3.0));
    }

    @Test
    public void testSplit_full() {
        // arrange
        EmbeddingPlane plane = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION)
                .getEmbedding();
        PlaneConvexSubset sp = Planes.subsetFromConvexArea(plane, ConvexArea.full());

        Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        PlaneConvexSubset minus = split.getMinus();
        Assert.assertEquals(1, minus.getEmbedded().getSubspaceRegion().getBoundaries().size());
        checkPoints(minus, RegionLocation.BOUNDARY, Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Y);
        checkPoints(minus, RegionLocation.INSIDE, Vector3D.Unit.MINUS_X);
        checkPoints(minus, RegionLocation.OUTSIDE, Vector3D.Unit.PLUS_X);

        PlaneConvexSubset plus = split.getPlus();
        Assert.assertEquals(1, plus.getEmbedded().getSubspaceRegion().getBoundaries().size());
        checkPoints(plus, RegionLocation.BOUNDARY, Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Y);
        checkPoints(plus, RegionLocation.INSIDE, Vector3D.Unit.PLUS_X);
        checkPoints(plus, RegionLocation.OUTSIDE, Vector3D.Unit.MINUS_X);
    }

    @Test
    public void testSplit_both() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        PlaneConvexSubset minus = split.getMinus();
        checkVertices(minus, Vector3D.of(1, 1, 0), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0));

        PlaneConvexSubset plus = split.getPlus();
        checkVertices(plus, Vector3D.of(1, 1, 1), Vector3D.of(1, 1, 0), Vector3D.of(0, 2, 0));
    }

    @Test
    public void testSplit_plusOnly() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, -3.1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());

        PlaneConvexSubset plus = split.getPlus();
        checkVertices(plus, Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0));
    }

    @Test
    public void testSplit_minusOnly() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1.1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        PlaneConvexSubset minus = split.getMinus();
        checkVertices(minus, Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0));

        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallelSplitter_on() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane splitter = sp.getPlane();

        // act
        Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallelSplitter_minus() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane plane = sp.getPlane();
        Plane splitter = plane.translate(plane.getNormal());

        // act
        Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(sp, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallelSplitter_plus() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane plane = sp.getPlane();
        Plane splitter = plane.translate(plane.getNormal().negate());

        // act
        Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(sp, split.getPlus());
    }

    @Test
    public void testSplit_antiParallelSplitter_on() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane splitter = sp.getPlane().reverse();

        // act
        Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_antiParallelSplitter_minus() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane plane = sp.getPlane().reverse();
        Plane splitter = plane.translate(plane.getNormal());

        // act
        Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(sp, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_antiParallelSplitter_plus() {
        // arrange
        PlaneConvexSubset ps = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane plane = ps.getPlane().reverse();
        Plane splitter = plane.translate(plane.getNormal().negate());

        // act
        Split<PlaneConvexSubset> split = ps.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(ps, split.getPlus());
    }

    @Test
    public void testSplit_usesVertexBasedTypesWhenPossible() {
        // arrange
        // create an infinite subset
        EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.ZERO,
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        PlaneConvexSubset ps = Planes.subsetFromConvexArea(plane, ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(1, 0), PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(0, 1), -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION)
                ));

        Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 1, 0), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        Split<PlaneConvexSubset> split = ps.split(splitter);

        // assert
        Assert.assertTrue(ps.isInfinite());

        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        PlaneConvexSubset plus = split.getPlus();
        Assert.assertNotNull(plus);
        Assert.assertTrue(plus.isInfinite());
        Assert.assertFalse(plus instanceof ConvexPolygon3D);

        PlaneConvexSubset minus = split.getMinus();
        Assert.assertNotNull(minus);
        Assert.assertFalse(minus.isInfinite());
        Assert.assertTrue(minus instanceof ConvexPolygon3D);
    }

    @Test
    public void testIntersection_line() {
        // arrange
        PlaneConvexSubset ps = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(0, 0, 2), Vector3D.of(1, 0, 2), Vector3D.of(1, 1, 2), Vector3D.of(0, 1, 2)),
                TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 2),
                ps.intersection(Lines3D.fromPoints(Vector3D.of(0.5, 0.5, 2), Vector3D.ZERO, TEST_PRECISION)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 2),
                ps.intersection(Lines3D.fromPoints(Vector3D.of(1, 1, 2), Vector3D.of(1, 1, 0), TEST_PRECISION)), TEST_EPS);

        Assert.assertNull(ps.intersection(Lines3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION)));
        Assert.assertNull(ps.intersection(Lines3D.fromPoints(Vector3D.of(0, 0, 2), Vector3D.of(1, 1, 2), TEST_PRECISION)));

        Assert.assertNull(ps.intersection(Lines3D.fromPoints(Vector3D.of(4, 4, 2), Vector3D.of(4, 4, 0), TEST_PRECISION)));
    }

    @Test
    public void testIntersection_segment() {
        // arrange
        PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(0, 0, 2), Vector3D.of(1, 0, 2), Vector3D.of(1, 1, 2), Vector3D.of(0, 1, 2)),
                TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 2),
                sp.intersection(Lines3D.segmentFromPoints(Vector3D.of(0.5, 0.5, 2), Vector3D.ZERO, TEST_PRECISION)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 2),
                sp.intersection(Lines3D.segmentFromPoints(Vector3D.of(1, 1, 2), Vector3D.of(1, 1, 0), TEST_PRECISION)), TEST_EPS);

        Assert.assertNull(sp.intersection(Lines3D.segmentFromPoints(Vector3D.of(0.5, 0.5, 4), Vector3D.of(0.5, 0.5, 3), TEST_PRECISION)));

        Assert.assertNull(sp.intersection(Lines3D.segmentFromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION)));
        Assert.assertNull(sp.intersection(Lines3D.segmentFromPoints(Vector3D.of(0, 0, 2), Vector3D.of(1, 1, 2), TEST_PRECISION)));

        Assert.assertNull(sp.intersection(Lines3D.segmentFromPoints(Vector3D.of(4, 4, 2), Vector3D.of(4, 4, 0), TEST_PRECISION)));
    }

    private static void checkPlane(Plane plane, Vector3D origin, Vector3D u, Vector3D v) {
        u = u.normalize();
        v = v.normalize();
        Vector3D w = u.cross(v);

        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getOrigin(), TEST_EPS);
        Assert.assertTrue(plane.contains(origin));

        EuclideanTestUtils.assertCoordinatesEqual(w, plane.getNormal(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getNormal().norm(), TEST_EPS);

        double offset = plane.getOriginOffset();
        Assert.assertEquals(Vector3D.ZERO.distance(plane.getOrigin()), Math.abs(offset), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getNormal().multiply(-offset), TEST_EPS);
    }

    private static void checkPoints(PlaneConvexSubset sp, RegionLocation loc, Vector3D... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, sp.classify(pt));
        }
    }

    private static void checkVertices(PlaneConvexSubset ps, Vector3D... pts) {
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(pts), ps.getVertices(), TEST_PRECISION);
    }
}
