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
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PlaneConvexSubsetTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    public void testToConvex() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.Unit.PLUS_X,  Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z), TEST_PRECISION);

        // act
        final List<PlaneConvexSubset> convex = sp.toConvex();

        // assert
        Assertions.assertEquals(1, convex.size());
        Assertions.assertSame(sp, convex.get(0));
    }

    @Test
    public void testReverse() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 0, 1);
        final Vector3D p2 = Vector3D.of(2, 0, 1);
        final Vector3D p3 = Vector3D.of(1, 1, 1);

        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(p1, p2, p3), TEST_PRECISION);

        // act
        final PlaneConvexSubset reversed = sp.reverse();

        // assert
        Assertions.assertEquals(sp.getPlane().reverse(), reversed.getPlane());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.MINUS_Z, reversed.getPlane().getNormal(), TEST_EPS);

        Assertions.assertEquals(0.5, reversed.getSize(), TEST_EPS);

        checkVertices(reversed, p1, p3, p2);

        checkPoints(reversed, RegionLocation.INSIDE, Vector3D.of(1.25, 0.25, 1));

        checkPoints(reversed, RegionLocation.BOUNDARY, p1, p2, p3);
    }

    @Test
    public void testTransform_full() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        final PlaneConvexSubset sp = Planes.subsetFromConvexArea(plane, ConvexArea.full());

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, Angle.PI_OVER_TWO))
                .translate(Vector3D.Unit.PLUS_Y);

        // act
        final PlaneConvexSubset transformed = sp.transform(transform);

        // assert
        Assertions.assertTrue(transformed.isFull());
        Assertions.assertFalse(transformed.isEmpty());

        checkPlane(transformed.getPlane(), Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z);
    }

    @Test
    public void testTransform_halfSpace() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        final PlaneConvexSubset sp = Planes.subsetFromConvexArea(plane,
                ConvexArea.fromBounds(Lines.fromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION)));

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createRotation(Vector3D.Unit.PLUS_Z,
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Angle.PI_OVER_TWO));

        // act
        final PlaneConvexSubset transformed = sp.transform(transform);

        // assert
        Assertions.assertFalse(transformed.isFull());
        Assertions.assertFalse(transformed.isEmpty());

        checkPlane(transformed.getPlane(), Vector3D.ZERO, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Y);
    }

    @Test
    public void testTransform_finite() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), Vector3D.of(0, 0, 1)), TEST_PRECISION);

        final Transform<Vector3D> transform = AffineTransformMatrix3D.createScale(2)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Angle.PI_OVER_TWO));

        // act
        final PlaneConvexSubset transformed = sp.transform(transform);

        // assert
        final Vector3D midpt = Vector3D.of(2, 2, -2).multiply(1 / 3.0);
        final Vector3D normal = midpt.normalize();
        final Vector3D u = Vector3D.of(0, 2, 2).normalize();

        checkPlane(transformed.getPlane(), midpt, u, normal.cross(u));

        checkVertices(transformed, Vector3D.of(0, 0, -2), Vector3D.of(0, 2, 0), Vector3D.of(2, 0, 0));

        checkPoints(transformed, RegionLocation.INSIDE, midpt);
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(
                Arrays.asList(Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), Vector3D.of(0, 0, 1)), TEST_PRECISION);

        final Transform<Vector3D> transform = AffineTransformMatrix3D.createScale(-1, 1, 1);

        // act
        final PlaneConvexSubset transformed = sp.transform(transform);

        // assert
        final Vector3D midpt = Vector3D.of(-1, 1, 1).multiply(1 / 3.0);
        final Vector3D normal = midpt.negate().normalize();
        final Vector3D u = Vector3D.of(1, 1, 0).normalize();

        checkPlane(transformed.getPlane(), midpt, u, normal.cross(u));

        checkVertices(transformed, Vector3D.of(-1, 0, 0), Vector3D.of(0, 1, 0), Vector3D.of(0, 0, 1));

        checkPoints(transformed, RegionLocation.INSIDE, Vector3D.of(-1, 1, 1).multiply(1 / 3.0));
    }

    @Test
    public void testSplit_full() {
        // arrange
        final EmbeddingPlane plane = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION)
                .getEmbedding();
        final PlaneConvexSubset sp = Planes.subsetFromConvexArea(plane, ConvexArea.full());

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final PlaneConvexSubset minus = split.getMinus();
        Assertions.assertEquals(1, minus.getEmbedded().getSubspaceRegion().getBoundaries().size());
        checkPoints(minus, RegionLocation.BOUNDARY, Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Y);
        checkPoints(minus, RegionLocation.INSIDE, Vector3D.Unit.MINUS_X);
        checkPoints(minus, RegionLocation.OUTSIDE, Vector3D.Unit.PLUS_X);

        final PlaneConvexSubset plus = split.getPlus();
        Assertions.assertEquals(1, plus.getEmbedded().getSubspaceRegion().getBoundaries().size());
        checkPoints(plus, RegionLocation.BOUNDARY, Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Y);
        checkPoints(plus, RegionLocation.INSIDE, Vector3D.Unit.PLUS_X);
        checkPoints(plus, RegionLocation.OUTSIDE, Vector3D.Unit.MINUS_X);
    }

    @Test
    public void testSplit_both() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final PlaneConvexSubset minus = split.getMinus();
        checkVertices(minus, Vector3D.of(1, 1, 0), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0));

        final PlaneConvexSubset plus = split.getPlus();
        checkVertices(plus, Vector3D.of(1, 1, 1), Vector3D.of(1, 1, 0), Vector3D.of(0, 2, 0));
    }

    @Test
    public void testSplit_plusOnly() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, -3.1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());

        final PlaneConvexSubset plus = split.getPlus();
        checkVertices(plus, Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0));
    }

    @Test
    public void testSplit_minusOnly() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1.1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        final PlaneConvexSubset minus = split.getMinus();
        checkVertices(minus, Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0));

        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallelSplitter_on() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        final Plane splitter = sp.getPlane();

        // act
        final Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallelSplitter_minus() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        final Plane plane = sp.getPlane();
        final Plane splitter = plane.translate(plane.getNormal());

        // act
        final Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assertions.assertSame(sp, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallelSplitter_plus() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        final Plane plane = sp.getPlane();
        final Plane splitter = plane.translate(plane.getNormal().negate());

        // act
        final Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(sp, split.getPlus());
    }

    @Test
    public void testSplit_antiParallelSplitter_on() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        final Plane splitter = sp.getPlane().reverse();

        // act
        final Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_antiParallelSplitter_minus() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        final Plane plane = sp.getPlane().reverse();
        final Plane splitter = plane.translate(plane.getNormal());

        // act
        final Split<PlaneConvexSubset> split = sp.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assertions.assertSame(sp, split.getMinus());
        Assertions.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_antiParallelSplitter_plus() {
        // arrange
        final PlaneConvexSubset ps = Planes.convexPolygonFromVertices(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        final Plane plane = ps.getPlane().reverse();
        final Plane splitter = plane.translate(plane.getNormal().negate());

        // act
        final Split<PlaneConvexSubset> split = ps.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assertions.assertNull(split.getMinus());
        Assertions.assertSame(ps, split.getPlus());
    }

    @Test
    public void testSplit_usesVertexBasedTypesWhenPossible() {
        // arrange
        // create an infinite subset
        final EmbeddingPlane plane = Planes.fromPointAndPlaneVectors(Vector3D.ZERO,
                Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        final PlaneConvexSubset ps = Planes.subsetFromConvexArea(plane, ConvexArea.fromBounds(
                    Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(1, 0), Angle.PI_OVER_TWO, TEST_PRECISION),
                    Lines.fromPointAndAngle(Vector2D.of(0, 1), -Angle.PI_OVER_TWO, TEST_PRECISION)
                ));

        final Plane splitter = Planes.fromPointAndNormal(Vector3D.of(0, 1, 0), Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        final Split<PlaneConvexSubset> split = ps.split(splitter);

        // assert
        Assertions.assertTrue(ps.isInfinite());

        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final PlaneConvexSubset plus = split.getPlus();
        Assertions.assertNotNull(plus);
        Assertions.assertTrue(plus.isInfinite());
        Assertions.assertFalse(plus instanceof ConvexPolygon3D);

        final PlaneConvexSubset minus = split.getMinus();
        Assertions.assertNotNull(minus);
        Assertions.assertFalse(minus.isInfinite());
        Assertions.assertTrue(minus instanceof ConvexPolygon3D);
    }

    @Test
    public void testIntersection_line() {
        // arrange
        final PlaneConvexSubset ps = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(0, 0, 2), Vector3D.of(1, 0, 2), Vector3D.of(1, 1, 2), Vector3D.of(0, 1, 2)),
                TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 2),
                ps.intersection(Lines3D.fromPoints(Vector3D.of(0.5, 0.5, 2), Vector3D.ZERO, TEST_PRECISION)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 2),
                ps.intersection(Lines3D.fromPoints(Vector3D.of(1, 1, 2), Vector3D.of(1, 1, 0), TEST_PRECISION)), TEST_EPS);

        Assertions.assertNull(ps.intersection(Lines3D.fromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION)));
        Assertions.assertNull(ps.intersection(Lines3D.fromPoints(Vector3D.of(0, 0, 2), Vector3D.of(1, 1, 2), TEST_PRECISION)));

        Assertions.assertNull(ps.intersection(Lines3D.fromPoints(Vector3D.of(4, 4, 2), Vector3D.of(4, 4, 0), TEST_PRECISION)));
    }

    @Test
    public void testIntersection_segment() {
        // arrange
        final PlaneConvexSubset sp = Planes.convexPolygonFromVertices(Arrays.asList(
                Vector3D.of(0, 0, 2), Vector3D.of(1, 0, 2), Vector3D.of(1, 1, 2), Vector3D.of(0, 1, 2)),
                TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 0.5, 2),
                sp.intersection(Lines3D.segmentFromPoints(Vector3D.of(0.5, 0.5, 2), Vector3D.ZERO, TEST_PRECISION)), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 2),
                sp.intersection(Lines3D.segmentFromPoints(Vector3D.of(1, 1, 2), Vector3D.of(1, 1, 0), TEST_PRECISION)), TEST_EPS);

        Assertions.assertNull(sp.intersection(Lines3D.segmentFromPoints(Vector3D.of(0.5, 0.5, 4), Vector3D.of(0.5, 0.5, 3), TEST_PRECISION)));

        Assertions.assertNull(sp.intersection(Lines3D.segmentFromPoints(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION)));
        Assertions.assertNull(sp.intersection(Lines3D.segmentFromPoints(Vector3D.of(0, 0, 2), Vector3D.of(1, 1, 2), TEST_PRECISION)));

        Assertions.assertNull(sp.intersection(Lines3D.segmentFromPoints(Vector3D.of(4, 4, 2), Vector3D.of(4, 4, 0), TEST_PRECISION)));
    }

    private static void checkPlane(final Plane plane, final Vector3D origin, Vector3D u, Vector3D v) {
        u = u.normalize();
        v = v.normalize();
        final Vector3D w = u.cross(v);

        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getOrigin(), TEST_EPS);
        Assertions.assertTrue(plane.contains(origin));

        EuclideanTestUtils.assertCoordinatesEqual(w, plane.getNormal(), TEST_EPS);
        Assertions.assertEquals(1.0, plane.getNormal().norm(), TEST_EPS);

        final double offset = plane.getOriginOffset();
        Assertions.assertEquals(Vector3D.ZERO.distance(plane.getOrigin()), Math.abs(offset), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getNormal().multiply(-offset), TEST_EPS);
    }

    private static void checkPoints(final PlaneConvexSubset sp, final RegionLocation loc, final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(loc, sp.classify(pt), "Unexpected location for point " + pt);
        }
    }

    private static void checkVertices(final PlaneConvexSubset ps, final Vector3D... pts) {
        EuclideanTestUtils.assertVertexLoopSequence(Arrays.asList(pts), ps.getVertices(), TEST_PRECISION);
    }
}
