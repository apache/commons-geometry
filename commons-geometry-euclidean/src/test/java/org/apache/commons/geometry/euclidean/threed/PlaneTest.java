/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.euclidean.threed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PlaneTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testFromNormal() {
        // act/assert
        checkPlane(Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_X);
        checkPlane(Planes.fromNormal(Vector3D.of(7, 0, 0), TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_X);

        checkPlane(Planes.fromNormal(Vector3D.Unit.PLUS_Y, TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_Y);
        checkPlane(Planes.fromNormal(Vector3D.of(0, 5, 0), TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_Y);

        checkPlane(Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_Z);
        checkPlane(Planes.fromNormal(Vector3D.of(0, 0, 0.01), TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_Z);
    }

    @Test
    void testFromNormal_illegalArguments() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromNormal(Vector3D.ZERO, TEST_PRECISION));
    }

    @Test
    void testFromPointAndNormal() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert
        checkPlane(Planes.fromPointAndNormal(pt, Vector3D.of(0.1, 0, 0), TEST_PRECISION),
                Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_X);
        checkPlane(Planes.fromPointAndNormal(pt, Vector3D.of(0, 2, 0), TEST_PRECISION),
                Vector3D.of(0, 2, 0), Vector3D.Unit.PLUS_Y);
        checkPlane(Planes.fromPointAndNormal(pt, Vector3D.of(0, 0, 5), TEST_PRECISION),
                Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_Z);
    }

    @Test
    void testFromPointAndNormal_illegalArguments() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPointAndNormal(pt, Vector3D.ZERO, TEST_PRECISION));
    }

    @Test
    void testFromPoints() {
        // arrange
        final Vector3D a = Vector3D.of(1, 1, 1);
        final Vector3D b = Vector3D.of(1, 1, 4.3);
        final Vector3D c = Vector3D.of(2.5, 1, 1);

        // act/assert
        checkPlane(Planes.fromPoints(a, b, c, TEST_PRECISION),
                Vector3D.of(0, 1, 0), Vector3D.Unit.PLUS_Y);

        checkPlane(Planes.fromPoints(a, c, b, TEST_PRECISION),
                Vector3D.of(0, 1, 0), Vector3D.Unit.MINUS_Y);
    }

    @Test
    void testFromPoints_planeContainsSourcePoints() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);

        // act
        final Plane plane  = Planes.fromPoints(p1, p2, p3, TEST_PRECISION);

        // assert
        Assertions.assertTrue(plane.contains(p1));
        Assertions.assertTrue(plane.contains(p2));
        Assertions.assertTrue(plane.contains(p3));
    }

    @Test
    void testFromPoints_illegalArguments() {
        // arrange
        final Vector3D a = Vector3D.of(1, 0, 0);
        final Vector3D b = Vector3D.of(0, 1, 0);

        // act/assert

        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(a, a, a, TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(a, a, b, TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(a, b, a, TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(b, a, a, TEST_PRECISION));
    }

    @Test
    void testFromPoints_collection_threePoints() {
        // arrange
        final List<Vector3D> pts = Arrays.asList(
                    Vector3D.of(1, 1, 0),
                    Vector3D.of(1, 1, -1),
                    Vector3D.of(0, 1, 0)
                );

        // act
        final Plane plane = Planes.fromPoints(pts, TEST_PRECISION);

        // assert
        checkPlane(plane, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Y);

        Assertions.assertTrue(plane.contains(pts.get(0)));
        Assertions.assertTrue(plane.contains(pts.get(1)));
        Assertions.assertTrue(plane.contains(pts.get(2)));
    }

    @Test
    void testFromPoints_collection_someCollinearPoints() {
        // arrange
        final List<Vector3D> pts = Arrays.asList(
                    Vector3D.of(1, 0, 2),
                    Vector3D.of(2, 0, 2),
                    Vector3D.of(3, 0, 2),
                    Vector3D.of(0, 1, 2)
                );

        // act
        final Plane plane = Planes.fromPoints(pts, TEST_PRECISION);

        // assert
        checkPlane(plane, Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Z);

        Assertions.assertTrue(plane.contains(pts.get(0)));
        Assertions.assertTrue(plane.contains(pts.get(1)));
        Assertions.assertTrue(plane.contains(pts.get(2)));
        Assertions.assertTrue(plane.contains(pts.get(3)));
    }

    @Test
    void testFromPoints_collection_concaveWithCollinearAndDuplicatePoints() {
        // arrange
        final List<Vector3D> pts = Arrays.asList(
                    Vector3D.of(1, 0, 1),
                    Vector3D.of(1, 0, 0.5),

                    Vector3D.of(1, 0, 0),
                    Vector3D.of(1, 1, -1),
                    Vector3D.of(1, 2, 0),
                    Vector3D.of(1, 2, 1e-15),
                    Vector3D.of(1, 1, -0.5),
                    Vector3D.of(1, 1 + 1e-15, -0.5),
                    Vector3D.of(1 - 1e-15, 1, -0.5),
                    Vector3D.of(1, 0, 0),

                    Vector3D.of(1, 0, 0.5),
                    Vector3D.of(1, 0, 1)
                );

        final Vector3D origin = Vector3D.of(1, 0, 0);

        // act
        checkPlane(Planes.fromPoints(pts, TEST_PRECISION), origin, Vector3D.Unit.PLUS_X);

        for (int i = 1; i < 12; ++i) {
            checkPlane(Planes.fromPoints(rotate(pts, i), TEST_PRECISION), origin, Vector3D.Unit.PLUS_X);
        }
    }

    @Test
    void testFromPoints_collection_choosesBestOrientation() {
        // act/assert
        checkPlane(Planes.fromPoints(Arrays.asList(
                Vector3D.of(1, 0, 2),
                Vector3D.of(2, 0, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(3.5, 1, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Z);

        checkPlane(Planes.fromPoints(Arrays.asList(
                Vector3D.of(1, 0, 2),
                Vector3D.of(2, 0, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(3.5, -1, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.MINUS_Z);

        checkPlane(Planes.fromPoints(Arrays.asList(
                Vector3D.of(1, 0, 2),
                Vector3D.of(2, 0, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(3.5, -1, 2),
                Vector3D.of(4, 0, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Z);

        checkPlane(Planes.fromPoints(Arrays.asList(
                Vector3D.of(1, 0, 2),
                Vector3D.of(2, 0, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(3.5, 1, 2),
                Vector3D.of(4, -1, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.MINUS_Z);

        checkPlane(Planes.fromPoints(Arrays.asList(
                Vector3D.of(0, 0, 2),
                Vector3D.of(1, 0, 2),
                Vector3D.of(1, 1, 2),
                Vector3D.of(0, 1, 2),
                Vector3D.of(0, 0, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Z);

        checkPlane(Planes.fromPoints(Arrays.asList(
                Vector3D.of(0, 0, 2),
                Vector3D.of(0, 1, 2),
                Vector3D.of(1, 1, 2),
                Vector3D.of(1, 0, 2),
                Vector3D.of(0, 0, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.MINUS_Z);

        checkPlane(Planes.fromPoints(Arrays.asList(
                Vector3D.of(0, 0, 2),
                Vector3D.of(1, 0, 2),
                Vector3D.of(2, 1, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(2, 4, 2),
                Vector3D.of(0, 0, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Z);

        checkPlane(Planes.fromPoints(Arrays.asList(
                Vector3D.of(0, 0, 2),
                Vector3D.of(0, 1, 2),
                Vector3D.of(2, 4, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(2, 1, 2),
                Vector3D.of(0, 0, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.MINUS_Z);
    }

    @Test
    void testFromPoints_collection_illegalArguments() {
        // arrange
        final Vector3D a = Vector3D.ZERO;
        final Vector3D b = Vector3D.Unit.PLUS_X;

        // act/assert

        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(Collections.emptyList(), TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(Collections.singletonList(a), TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(Arrays.asList(a, b), TEST_PRECISION));
    }

    @Test
    void testFromPoints_collection_allPointsCollinear() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(Arrays.asList(
                Vector3D.ZERO,
                Vector3D.Unit.PLUS_X,
                Vector3D.of(2, 0, 0)
        ), TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(Arrays.asList(
                Vector3D.ZERO,
                Vector3D.Unit.PLUS_X,
                Vector3D.of(2, 0, 0),
                Vector3D.of(3, 0, 0)
        ), TEST_PRECISION));
    }

    @Test
    void testFromPoints_collection_notEnoughUniquePoints() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(Arrays.asList(
                Vector3D.ZERO,
                Vector3D.ZERO,
                Vector3D.of(1e-12, 1e-12, 0),
                Vector3D.Unit.PLUS_X
        ), TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(Arrays.asList(
                Vector3D.ZERO,
                Vector3D.of(1e-12, 0, 0),
                Vector3D.ZERO
        ), TEST_PRECISION));
    }

    @Test
    void testFromPoints_collection_pointsNotOnSamePlane() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Planes.fromPoints(Arrays.asList(
                Vector3D.ZERO,
                Vector3D.Unit.PLUS_X,
                Vector3D.Unit.PLUS_Y,
                Vector3D.Unit.PLUS_Z
        ), TEST_PRECISION));
    }

    @Test
    void testGetEmbedding() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        EuclideanTestUtils.permuteSkipZero(-4, 4, 1, (x, y, z) -> {

            final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.of(x, y, z), TEST_PRECISION);

            // act
            final EmbeddingPlane embeddingPlane = plane.getEmbedding();
            final EmbeddingPlane nextEmbeddingPlane = plane.getEmbedding();

            // assert
            Assertions.assertSame(plane.getNormal(), embeddingPlane.getNormal());
            Assertions.assertSame(plane.getNormal(), embeddingPlane.getW());
            Assertions.assertEquals(plane.getOriginOffset(), embeddingPlane.getOriginOffset(), TEST_EPS);
            Assertions.assertSame(plane.getPrecision(), embeddingPlane.getPrecision());

            final Vector3D.Unit u = embeddingPlane.getU();
            final Vector3D.Unit v = embeddingPlane.getV();
            final Vector3D.Unit w = embeddingPlane.getW();

            Assertions.assertEquals(0, u.dot(v), TEST_EPS);
            Assertions.assertEquals(0, u.dot(w), TEST_EPS);
            Assertions.assertEquals(0, v.dot(w), TEST_EPS);

            Assertions.assertNotSame(embeddingPlane, nextEmbeddingPlane);
            Assertions.assertEquals(embeddingPlane, nextEmbeddingPlane);
        });
    }

    @Test
    void testContains_point() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        final double halfEps = 0.5 * TEST_EPS;

        // act/assert
        EuclideanTestUtils.permute(-100, 100, 5, (x, y) -> {

            Assertions.assertTrue(plane.contains(Vector3D.of(x, y, 1)));
            Assertions.assertTrue(plane.contains(Vector3D.of(x, y, 1 + halfEps)));
            Assertions.assertTrue(plane.contains(Vector3D.of(x, y, 1 - halfEps)));

            Assertions.assertFalse(plane.contains(Vector3D.of(x, y, 0.5)));
            Assertions.assertFalse(plane.contains(Vector3D.of(x, y, 1.5)));
        });
    }

    @Test
    void testContains_line() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(plane.contains(
                Lines3D.fromPoints(Vector3D.of(1, 0, 0), Vector3D.of(2, 0, 0), TEST_PRECISION)));
        Assertions.assertTrue(plane.contains(
                Lines3D.fromPoints(Vector3D.of(-1, 0, 0), Vector3D.of(-2, 0, 0), TEST_PRECISION)));

        Assertions.assertFalse(plane.contains(
                Lines3D.fromPoints(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 2), TEST_PRECISION)));
        Assertions.assertFalse(plane.contains(
                Lines3D.fromPoints(Vector3D.ZERO, Vector3D.of(2, 0, 2), TEST_PRECISION)));
    }

    @Test
    void testContains_plane() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        final Plane planeA = Planes.fromPoints(p1, p2, p3, TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(planeA.contains(planeA));
        Assertions.assertTrue(planeA.contains(Planes.fromPoints(p1, p3, p2, TEST_PRECISION)));
        Assertions.assertTrue(planeA.contains(Planes.fromPoints(p3, p1, p2, TEST_PRECISION)));
        Assertions.assertTrue(planeA.contains(Planes.fromPoints(p3, p2, p1, TEST_PRECISION)));

        Assertions.assertFalse(planeA.contains(Planes.fromPoints(p1, Vector3D.of(11.4, -3.8, 5.1), p2, TEST_PRECISION)));

        final Vector3D offset = planeA.getNormal().multiply(1e-8);
        Assertions.assertFalse(planeA.contains(Planes.fromPoints(p1.add(offset), p2, p3, TEST_PRECISION)));
        Assertions.assertFalse(planeA.contains(Planes.fromPoints(p1, p2.add(offset), p3, TEST_PRECISION)));
        Assertions.assertFalse(planeA.contains(Planes.fromPoints(p1, p2, p3.add(offset), TEST_PRECISION)));

        Assertions.assertFalse(planeA.contains(Planes.fromPoints(p1.add(offset),
                p2.add(offset),
                p3.add(offset), TEST_PRECISION)));
    }

    @Test
    void testReverse() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Plane reversed = plane.reverse();

        // assert
        checkPlane(reversed, pt, Vector3D.Unit.MINUS_Z);

        Assertions.assertTrue(reversed.contains(Vector3D.of(1, 1, 1)));
        Assertions.assertTrue(reversed.contains(Vector3D.of(-1, -1, 1)));
        Assertions.assertFalse(reversed.contains(Vector3D.ZERO));

        Assertions.assertEquals(1.0, reversed.offset(Vector3D.ZERO), TEST_EPS);
    }

    @Test
    void testIsParallelAndOffset_line() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);

        final Line3D parallelLine = Lines3D.fromPoints(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 2), TEST_PRECISION);
        final Line3D nonParallelLine = Lines3D.fromPoints(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 1), TEST_PRECISION);
        final Line3D containedLine = Lines3D.fromPoints(Vector3D.of(2, 0, 1), Vector3D.of(1, 0, 1), TEST_PRECISION);

        // act
        Assertions.assertTrue(plane.isParallel(parallelLine));
        Assertions.assertEquals(1.0, plane.offset(parallelLine), TEST_EPS);

        Assertions.assertFalse(plane.isParallel(nonParallelLine));
        Assertions.assertEquals(0.0, plane.offset(nonParallelLine), TEST_EPS);

        Assertions.assertTrue(plane.isParallel(containedLine));
        Assertions.assertEquals(0.0, plane.offset(containedLine), TEST_EPS);
    }

    @Test
    void testIsParallelAndOffset_plane() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        final Plane parallelPlane = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        final Plane parallelPlane2 = Planes.fromPointAndNormal(Vector3D.of(0, 0, 2), Vector3D.of(0, 0, 1), TEST_PRECISION);
        final Plane parallelPlane3 = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(0, 0, 1), TEST_PRECISION).reverse();
        final Plane nonParallelPlane = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1),
                Vector3D.of(1, 1.5, 1).cross(Vector3D.of(0, 1, 1)), TEST_PRECISION);
        final Plane reversedPlane = plane.reverse();

        // act/assert
        Assertions.assertTrue(plane.isParallel(parallelPlane));
        Assertions.assertEquals(0.0, plane.offset(parallelPlane), TEST_EPS);

        Assertions.assertTrue(plane.isParallel(parallelPlane2));
        Assertions.assertEquals(1.0, plane.offset(parallelPlane2), TEST_EPS);

        Assertions.assertTrue(plane.isParallel(parallelPlane3));
        Assertions.assertEquals(-1.0, plane.offset(parallelPlane3), TEST_EPS);

        Assertions.assertFalse(plane.isParallel(nonParallelPlane));
        Assertions.assertEquals(0.0, plane.offset(nonParallelPlane), TEST_EPS);

        Assertions.assertTrue(plane.isParallel(reversedPlane));
        Assertions.assertEquals(0.0, plane.offset(nonParallelPlane), TEST_EPS);
    }

    @Test
    void testOffset_point() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 1, 1);
        final Plane plane = Planes.fromPointAndNormal(p1, Vector3D.of(0.2, 0, 0), TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(-5.0, plane.offset(Vector3D.of(-4, 0, 0)), TEST_EPS);
        Assertions.assertEquals(+5.0, plane.offset(Vector3D.of(6, 10, -12)), TEST_EPS);
        Assertions.assertEquals(0.3,
                            plane.offset(Vector3D.Sum.of(p1).addScaled(0.3, plane.getNormal()).get()),
                            TEST_EPS);
        Assertions.assertEquals(-0.3,
                            plane.offset(Vector3D.Sum.of(p1).addScaled(-0.3, plane.getNormal()).get()),
                            TEST_EPS);
    }

    @Test
    void testProject_point() {
        // arrange
        final Vector3D pt = Vector3D.of(-3, -2, -1);
        EuclideanTestUtils.permuteSkipZero(-4, 4, 1, (nx, ny, nz) -> {

            final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.of(nx, ny, nz), TEST_PRECISION);
            EuclideanTestUtils.permute(-4, 4, 1, (x, y, z) -> {

                final Vector3D p = Vector3D.of(x, y, z);

                // act
                final Vector3D proj = plane.project(p);

                // assert
                Assertions.assertTrue(plane.contains(proj));
                Assertions.assertEquals(0, plane.getOrigin().vectorTo(proj).dot(plane.getNormal()), TEST_EPS);
            });
        });
    }

    @Test
    void testProject_line() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Line3D line = Lines3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(2, 0, 2), TEST_PRECISION);

        // act
        final Line3D projected = plane.project(line);

        // assert
        final Line3D expectedProjection = Lines3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(2, 0, 1), TEST_PRECISION);
        Assertions.assertEquals(expectedProjection, projected);

        Assertions.assertTrue(plane.contains(projected));

        Assertions.assertTrue(projected.contains(Vector3D.of(1, 0, 1)));
        Assertions.assertTrue(projected.contains(Vector3D.of(2, 0, 1)));
    }

    @Test
    void testTransform_rotationAroundPoint() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        final AffineTransformMatrix3D mat = AffineTransformMatrix3D.createRotation(pt,
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, Angle.PI_OVER_TWO));

        // act
        final Plane result = plane.transform(mat);

        // assert
        checkPlane(result, Vector3D.ZERO, Vector3D.Unit.PLUS_X);
    }

    @Test
    void testTransform_asymmetricScaling() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 1, 0);
        final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.of(1, 1, 0), TEST_PRECISION);

        final AffineTransformMatrix3D t = AffineTransformMatrix3D.createScale(2, 1, 1);

        // act
        final Plane result = plane.transform(t);

        // assert
        final Vector3D expectedNormal = Vector3D.Unit.from(1, 2, 0);
        final Vector3D expectedOrigin = result.project(Vector3D.ZERO);

        checkPlane(result, expectedOrigin, expectedNormal);

        final Vector3D transformedPt = t.apply(Vector3D.of(0.5, 0.5, 1));
        Assertions.assertTrue(result.contains(transformedPt));
        Assertions.assertFalse(plane.contains(transformedPt));
    }

    @Test
    void testTransform_negateOneComponent() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(-1, 1,  1);

        // act
        final Plane result = plane.transform(transform);

        // assert
        checkPlane(result, Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_Z);

        Assertions.assertFalse(transform.preservesOrientation());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z,
                transform.normalTransform().apply(plane.getNormal()), TEST_EPS);
    }

    @Test
    void testTransform_negateTwoComponents() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.from(v -> Vector3D.of(-v.getX(), -v.getY(), v.getZ()));

        // act
        final Plane result = plane.transform(transform);

        // assert
        checkPlane(result, Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_Z);
    }

    @Test
    void testTransform_negateAllComponents() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.from(Vector3D::negate);

        // act
        final Plane result = plane.transform(transform);

        // assert
        checkPlane(result, Vector3D.of(0, 0, -1), Vector3D.Unit.PLUS_Z);
    }

    @Test
    void testTransform_consistency() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D normal = Vector3D.Unit.of(1, 1, 1);

        final Plane plane = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);

        final Vector3D p1 = plane.project(Vector3D.of(4, 5, 6));
        final Vector3D p2 = plane.project(Vector3D.of(-7, -8, -9));
        final Vector3D p3 = plane.project(Vector3D.of(10, -11, 12));

        final Vector3D notOnPlane1 = plane.getOrigin().add(plane.getNormal());
        final Vector3D notOnPlane2 = plane.getOrigin().subtract(plane.getNormal());

        EuclideanTestUtils.permuteSkipZero(-4, 4, 1, (a, b, c) -> {
            final AffineTransformMatrix3D t = AffineTransformMatrix3D.identity()
                    .rotate(Vector3D.of(-1, 2, 3),
                            QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_X, 0.3 * a))
                    .scale(Math.max(a, 1), Math.max(b, 1), Math.max(c, 1))
                    .translate(c, b, a);

            // act
            final Plane result = plane.transform(t);

            // assert
            Vector3D expectedNormal = t.normalTransform().apply(plane.getNormal()).normalize();
            if (!t.preservesOrientation()) {
                expectedNormal = expectedNormal.negate();
            }

            EuclideanTestUtils.assertCoordinatesEqual(expectedNormal, result.getNormal(), TEST_EPS);

            Assertions.assertTrue(result.contains(t.apply(p1)));
            Assertions.assertTrue(result.contains(t.apply(p2)));
            Assertions.assertTrue(result.contains(t.apply(p3)));

            Assertions.assertFalse(result.contains(t.apply(notOnPlane1)));
            Assertions.assertFalse(result.contains(t.apply(notOnPlane2)));
        });
    }

    @Test
    void testRotate() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane plane  = Planes.fromPoints(p1, p2, p3, TEST_PRECISION);
        final Vector3D oldNormal = plane.getNormal();

        // act/assert
        plane = plane.rotate(p2, QuaternionRotation.fromAxisAngle(p2.subtract(p1), 1.7));
        Assertions.assertTrue(plane.contains(p1));
        Assertions.assertTrue(plane.contains(p2));
        Assertions.assertFalse(plane.contains(p3));

        plane = plane.rotate(p2, QuaternionRotation.fromAxisAngle(oldNormal, 0.1));
        Assertions.assertFalse(plane.contains(p1));
        Assertions.assertTrue(plane.contains(p2));
        Assertions.assertFalse(plane.contains(p3));

        plane = plane.rotate(p1, QuaternionRotation.fromAxisAngle(oldNormal, 0.1));
        Assertions.assertFalse(plane.contains(p1));
        Assertions.assertFalse(plane.contains(p2));
        Assertions.assertFalse(plane.contains(p3));
    }

    @Test
    void testTranslate() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane plane  = Planes.fromPoints(p1, p2, p3, TEST_PRECISION);

        // act/assert
        plane = plane.translate(plane.getNormal().orthogonal().multiply(2.0));
        Assertions.assertTrue(plane.contains(p1));
        Assertions.assertTrue(plane.contains(p2));
        Assertions.assertTrue(plane.contains(p3));

        plane = plane.translate(plane.getNormal().multiply(-1.2));
        Assertions.assertFalse(plane.contains(p1));
        Assertions.assertFalse(plane.contains(p2));
        Assertions.assertFalse(plane.contains(p3));

        plane = plane.translate(plane.getNormal().multiply(+1.2));
        Assertions.assertTrue(plane.contains(p1));
        Assertions.assertTrue(plane.contains(p2));
        Assertions.assertTrue(plane.contains(p3));
    }

    @Test
    void testIntersection_withLine() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.of(1, 2, 3), Vector3D.of(-4, 1, -5), TEST_PRECISION);
        final Line3D line = Lines3D.fromPoints(Vector3D.of(0.2, -3.5, 0.7), Vector3D.of(1.2, -2.5, -0.3), TEST_PRECISION);

        // act
        final Vector3D point = plane.intersection(line);

        // assert
        Assertions.assertTrue(plane.contains(point));
        Assertions.assertTrue(line.contains(point));
        Assertions.assertNull(plane.intersection(Lines3D.fromPoints(Vector3D.of(10, 10, 10),
                                                  Vector3D.of(10, 10, 10).add(plane.getNormal().orthogonal()),
                                                  TEST_PRECISION)));
    }

    @Test
    void testIntersection_withLine_noIntersection() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D normal = Vector3D.of(-4, 1, -5);

        final Plane plane = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);
        final Vector3D u = plane.getNormal().orthogonal();
        final Vector3D v = plane.getNormal().cross(u);

        // act/assert
        Assertions.assertNull(plane.intersection(Lines3D.fromPoints(pt, pt.add(u), TEST_PRECISION)));

        final Vector3D offsetPt = pt.add(plane.getNormal());
        Assertions.assertNull(plane.intersection(Lines3D.fromPoints(offsetPt, offsetPt.add(v), TEST_PRECISION)));
    }

    @Test
    void testIntersection_withPlane() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Plane planeA = Planes.fromPoints(p1, p2, Vector3D.of(-2.0, 4.3, 0.7), TEST_PRECISION);
        final Plane planeB = Planes.fromPoints(p1, Vector3D.of(11.4, -3.8, 5.1), p2, TEST_PRECISION);

        // act
        final Line3D line = planeA.intersection(planeB);

        // assert
        Assertions.assertTrue(line.contains(p1));
        Assertions.assertTrue(line.contains(p2));
        EuclideanTestUtils.assertCoordinatesEqual(planeA.getNormal().cross(planeB.getNormal()).normalize(),
                line.getDirection(), TEST_EPS);

        Assertions.assertNull(planeA.intersection(planeA));
    }

    @Test
    void testIntersection_withPlane_noIntersection() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        Assertions.assertNull(plane.intersection(plane));
        Assertions.assertNull(plane.intersection(plane.reverse()));

        Assertions.assertNull(plane.intersection(Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION)));
        Assertions.assertNull(plane.intersection(Planes.fromPointAndNormal(Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Z, TEST_PRECISION)));
    }

    @Test
    void testIntersection_threePlanes() {
        // arrange
        final Vector3D pt = Vector3D.of(1.2, 3.4, -5.8);
        final Plane a = Planes.fromPointAndNormal(pt, Vector3D.of(1, 3, 3), TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(pt, Vector3D.of(-2, 4, 0), TEST_PRECISION);
        final Plane c = Planes.fromPointAndNormal(pt, Vector3D.of(7, 0, -4), TEST_PRECISION);

        // act
        final Vector3D result = Plane.intersection(a, b, c);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(pt, result, TEST_EPS);
    }

    @Test
    void testIntersection_threePlanes_intersectInLine() {
        // arrange
        final Plane a = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(1, 0, 0), TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(1, 0.5, 0), TEST_PRECISION);
        final Plane c = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(1, 1, 0), TEST_PRECISION);

        // act
        final Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assertions.assertNull(result);
    }

    @Test
    void testIntersection_threePlanes_twoParallel() {
        // arrange
        final Plane a = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final Plane c = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assertions.assertNull(result);
    }

    @Test
    void testIntersection_threePlanes_allParallel() {
        // arrange
        final Plane a = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Plane c = Planes.fromPointAndNormal(Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assertions.assertNull(result);
    }

    @Test
    void testIntersection_threePlanes_coincidentPlanes() {
        // arrange
        final Plane a = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Plane c = b.reverse();

        // act
        final Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assertions.assertNull(result);
    }

    @Test
    void testSpan() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final PlaneConvexSubset sub = plane.span();

        // assert
        Assertions.assertNotSame(plane, sub.getPlane());
        EuclideanTestUtils.assertCoordinatesEqual(plane.getOrigin(), sub.getPlane().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(plane.getNormal(), sub.getPlane().getNormal(), TEST_EPS);

        Assertions.assertTrue(sub.isFull());

        Assertions.assertTrue(sub.contains(Vector3D.ZERO));
        Assertions.assertTrue(sub.contains(Vector3D.of(1, 1, 0)));

        Assertions.assertFalse(sub.contains(Vector3D.of(0, 0, 1)));
    }

    @Test
    void testSimilarOrientation() {
        // arrange
        final Plane plane = Planes.fromNormal(Vector3D.of(1, 0, 0), TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(plane.similarOrientation(plane));
        Assertions.assertTrue(plane.similarOrientation(Planes.fromNormal(Vector3D.of(1, 1, 0), TEST_PRECISION)));
        Assertions.assertTrue(plane.similarOrientation(Planes.fromNormal(Vector3D.of(1, -1, 0), TEST_PRECISION)));

        Assertions.assertFalse(plane.similarOrientation(Planes.fromNormal(Vector3D.of(0, 1, 0), TEST_PRECISION)));
        Assertions.assertFalse(plane.similarOrientation(Planes.fromNormal(Vector3D.of(-1, 1, 0), TEST_PRECISION)));
        Assertions.assertFalse(plane.similarOrientation(Planes.fromNormal(Vector3D.of(-1, 1, 0), TEST_PRECISION)));
        Assertions.assertFalse(plane.similarOrientation(Planes.fromNormal(Vector3D.of(0, -1, 0), TEST_PRECISION)));
    }

    @Test
    void testEq() {
        // arrange
        final double eps = 1e-3;
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(eps);

        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D normal = Vector3D.Unit.PLUS_X;

        final Vector3D ptPrime = Vector3D.of(1.0001, 2.0001, 3.0001);
        final Vector3D normalPrime = Vector3D.Unit.of(1, 1e-4, 0);

        final Plane a = Planes.fromPointAndNormal(pt, normal, precision);

        final Plane b = Planes.fromPointAndNormal(Vector3D.of(2, 2, 3), normal, precision);
        final Plane c = Planes.fromPointAndNormal(pt, Vector3D.Unit.MINUS_X, precision);
        final Plane d = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);

        final Plane e = Planes.fromPointAndNormal(ptPrime, normalPrime, Precision.doubleEquivalenceOfEpsilon(eps));

        // act/assert
        Assertions.assertTrue(a.eq(a, precision));

        Assertions.assertFalse(a.eq(b, precision));
        Assertions.assertFalse(a.eq(c, precision));

        Assertions.assertTrue(a.eq(d, precision));
        Assertions.assertTrue(a.eq(e, precision));
        Assertions.assertTrue(e.eq(a, precision));
    }

    @Test
    void testHashCode() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D normal = Vector3D.Unit.PLUS_X;

        final Plane a = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.of(2, 2, 3), normal, TEST_PRECISION);
        final Plane c = Planes.fromPointAndNormal(pt, Vector3D.of(1, 1, 0), TEST_PRECISION);
        final Plane d = Planes.fromPointAndNormal(pt, normal, Precision.doubleEquivalenceOfEpsilon(1e-8));
        final Plane e = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);

        // act/assert
        final int hash = a.hashCode();

        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());
        Assertions.assertNotEquals(hash, d.hashCode());

        Assertions.assertEquals(hash, e.hashCode());
    }

    @Test
    void testEquals() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D normal = Vector3D.Unit.PLUS_X;

        final Plane a = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.of(2, 2, 3), normal, TEST_PRECISION);
        final Plane c = Planes.fromPointAndNormal(pt, Vector3D.Unit.MINUS_X, TEST_PRECISION);
        final Plane d = Planes.fromPointAndNormal(pt, normal, Precision.doubleEquivalenceOfEpsilon(1e-8));
        final Plane e = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(a, c);
        Assertions.assertNotEquals(a, d);

        Assertions.assertEquals(a, e);
        Assertions.assertEquals(e, a);
    }

    @Test
    void testToString() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final String str = plane.toString();

        // assert
        Assertions.assertTrue(str.startsWith("Plane["));
        Assertions.assertTrue(str.matches(".*origin= \\(0(\\.0)?, 0(\\.0)?\\, 0(\\.0)?\\).*"));
        Assertions.assertTrue(str.matches(".*normal= \\(0(\\.0)?, 0(\\.0)?\\, 1(\\.0)?\\).*"));
    }

    private static void checkPlane(final Plane plane, final Vector3D origin, final Vector3D normal) {
        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getOrigin(), TEST_EPS);
        Assertions.assertTrue(plane.contains(origin));

        EuclideanTestUtils.assertCoordinatesEqual(normal, plane.getNormal(), TEST_EPS);
        Assertions.assertEquals(1.0, plane.getNormal().norm(), TEST_EPS);

        final double offset = plane.getOriginOffset();
        Assertions.assertEquals(Vector3D.ZERO.distance(plane.getOrigin()), Math.abs(offset), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getNormal().multiply(-offset), TEST_EPS);
    }

    private static <T> List<T> rotate(final List<T> list, final int shift) {
        final int size = list.size();

        final List<T> result = new ArrayList<>(size);

        for (int i = 0; i < size; ++i) {
            result.add(list.get((i + shift) % size));
        }

        return result;
    }
}
