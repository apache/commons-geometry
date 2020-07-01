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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class PlaneTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromNormal() {
        // act/assert
        checkPlane(Planes.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_X);
        checkPlane(Planes.fromNormal(Vector3D.of(7, 0, 0), TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_X);

        checkPlane(Planes.fromNormal(Vector3D.Unit.PLUS_Y, TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_Y);
        checkPlane(Planes.fromNormal(Vector3D.of(0, 5, 0), TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_Y);

        checkPlane(Planes.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_Z);
        checkPlane(Planes.fromNormal(Vector3D.of(0, 0, 0.01), TEST_PRECISION), Vector3D.ZERO, Vector3D.Unit.PLUS_Z);
    }

    @Test
    public void testFromNormal_illegalArguments() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.fromNormal(Vector3D.ZERO, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromPointAndNormal() {
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
    public void testFromPointAndNormal_illegalArguments() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPointAndNormal(pt, Vector3D.ZERO, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromPoints() {
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
    public void testFromPoints_planeContainsSourcePoints() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);

        // act
        final Plane plane  = Planes.fromPoints(p1, p2, p3, TEST_PRECISION);

        // assert
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertTrue(plane.contains(p3));
    }

    @Test
    public void testFromPoints_illegalArguments() {
        // arrange
        final Vector3D a = Vector3D.of(1, 0, 0);
        final Vector3D b = Vector3D.of(0, 1, 0);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(a, a, a, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(a, a, b, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(a, b, a, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(b, a, a, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromPoints_collection_threePoints() {
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

        Assert.assertTrue(plane.contains(pts.get(0)));
        Assert.assertTrue(plane.contains(pts.get(1)));
        Assert.assertTrue(plane.contains(pts.get(2)));
    }

    @Test
    public void testFromPoints_collection_someCollinearPoints() {
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

        Assert.assertTrue(plane.contains(pts.get(0)));
        Assert.assertTrue(plane.contains(pts.get(1)));
        Assert.assertTrue(plane.contains(pts.get(2)));
        Assert.assertTrue(plane.contains(pts.get(3)));
    }

    @Test
    public void testFromPoints_collection_concaveWithCollinearAndDuplicatePoints() {
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
    public void testFromPoints_collection_choosesBestOrientation() {
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
    public void testFromPoints_collection_illegalArguments() {
        // arrange
        final Vector3D a = Vector3D.ZERO;
        final Vector3D b = Vector3D.Unit.PLUS_X;

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(Collections.emptyList(), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(Collections.singletonList(a), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(Arrays.asList(a, b), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromPoints_collection_allPointsCollinear() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.Unit.PLUS_X,
                        Vector3D.of(2, 0, 0)
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.Unit.PLUS_X,
                        Vector3D.of(2, 0, 0),
                        Vector3D.of(3, 0, 0)
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromPoints_collection_notEnoughUniquePoints() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.ZERO,
                        Vector3D.of(1e-12, 1e-12, 0),
                        Vector3D.Unit.PLUS_X
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.of(1e-12, 0, 0),
                        Vector3D.ZERO
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromPoints_collection_pointsNotOnSamePlane() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Planes.fromPoints(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.Unit.PLUS_X,
                        Vector3D.Unit.PLUS_Y,
                        Vector3D.Unit.PLUS_Z
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testGetEmbedding() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        EuclideanTestUtils.permuteSkipZero(-4, 4, 1, (x, y, z) -> {

            final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.of(x, y, z), TEST_PRECISION);

            // act
            final EmbeddingPlane embeddingPlane = plane.getEmbedding();
            final EmbeddingPlane nextEmbeddingPlane = plane.getEmbedding();

            // assert
            Assert.assertSame(plane.getNormal(), embeddingPlane.getNormal());
            Assert.assertSame(plane.getNormal(), embeddingPlane.getW());
            Assert.assertEquals(plane.getOriginOffset(), embeddingPlane.getOriginOffset(), TEST_EPS);
            Assert.assertSame(plane.getPrecision(), embeddingPlane.getPrecision());

            final Vector3D.Unit u = embeddingPlane.getU();
            final Vector3D.Unit v = embeddingPlane.getV();
            final Vector3D.Unit w = embeddingPlane.getW();

            Assert.assertEquals(0, u.dot(v), TEST_EPS);
            Assert.assertEquals(0, u.dot(w), TEST_EPS);
            Assert.assertEquals(0, v.dot(w), TEST_EPS);

            Assert.assertNotSame(embeddingPlane, nextEmbeddingPlane);
            Assert.assertEquals(embeddingPlane, nextEmbeddingPlane);
        });
    }

    @Test
    public void testContains_point() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        final double halfEps = 0.5 * TEST_EPS;

        // act/assert
        EuclideanTestUtils.permute(-100, 100, 5, (x, y) -> {

            Assert.assertTrue(plane.contains(Vector3D.of(x, y, 1)));
            Assert.assertTrue(plane.contains(Vector3D.of(x, y, 1 + halfEps)));
            Assert.assertTrue(plane.contains(Vector3D.of(x, y, 1 - halfEps)));

            Assert.assertFalse(plane.contains(Vector3D.of(x, y, 0.5)));
            Assert.assertFalse(plane.contains(Vector3D.of(x, y, 1.5)));
        });
    }

    @Test
    public void testContains_line() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(plane.contains(
                Lines3D.fromPoints(Vector3D.of(1, 0, 0), Vector3D.of(2, 0, 0), TEST_PRECISION)));
        Assert.assertTrue(plane.contains(
                Lines3D.fromPoints(Vector3D.of(-1, 0, 0), Vector3D.of(-2, 0, 0), TEST_PRECISION)));

        Assert.assertFalse(plane.contains(
                Lines3D.fromPoints(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 2), TEST_PRECISION)));
        Assert.assertFalse(plane.contains(
                Lines3D.fromPoints(Vector3D.ZERO, Vector3D.of(2, 0, 2), TEST_PRECISION)));
    }

    @Test
    public void testContains_plane() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        final Plane planeA = Planes.fromPoints(p1, p2, p3, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(planeA.contains(planeA));
        Assert.assertTrue(planeA.contains(Planes.fromPoints(p1, p3, p2, TEST_PRECISION)));
        Assert.assertTrue(planeA.contains(Planes.fromPoints(p3, p1, p2, TEST_PRECISION)));
        Assert.assertTrue(planeA.contains(Planes.fromPoints(p3, p2, p1, TEST_PRECISION)));

        Assert.assertFalse(planeA.contains(Planes.fromPoints(p1, Vector3D.of(11.4, -3.8, 5.1), p2, TEST_PRECISION)));

        final Vector3D offset = planeA.getNormal().multiply(1e-8);
        Assert.assertFalse(planeA.contains(Planes.fromPoints(p1.add(offset), p2, p3, TEST_PRECISION)));
        Assert.assertFalse(planeA.contains(Planes.fromPoints(p1, p2.add(offset), p3, TEST_PRECISION)));
        Assert.assertFalse(planeA.contains(Planes.fromPoints(p1, p2, p3.add(offset), TEST_PRECISION)));

        Assert.assertFalse(planeA.contains(Planes.fromPoints(p1.add(offset),
                p2.add(offset),
                p3.add(offset), TEST_PRECISION)));
    }

    @Test
    public void testReverse() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Plane reversed = plane.reverse();

        // assert
        checkPlane(reversed, pt, Vector3D.Unit.MINUS_Z);

        Assert.assertTrue(reversed.contains(Vector3D.of(1, 1, 1)));
        Assert.assertTrue(reversed.contains(Vector3D.of(-1, -1, 1)));
        Assert.assertFalse(reversed.contains(Vector3D.ZERO));

        Assert.assertEquals(1.0, reversed.offset(Vector3D.ZERO), TEST_EPS);
    }

    @Test
    public void testIsParallelAndOffset_line() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);

        final Line3D parallelLine = Lines3D.fromPoints(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 2), TEST_PRECISION);
        final Line3D nonParallelLine = Lines3D.fromPoints(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 1), TEST_PRECISION);
        final Line3D containedLine = Lines3D.fromPoints(Vector3D.of(2, 0, 1), Vector3D.of(1, 0, 1), TEST_PRECISION);

        // act
        Assert.assertTrue(plane.isParallel(parallelLine));
        Assert.assertEquals(1.0, plane.offset(parallelLine), TEST_EPS);

        Assert.assertFalse(plane.isParallel(nonParallelLine));
        Assert.assertEquals(0.0, plane.offset(nonParallelLine), TEST_EPS);

        Assert.assertTrue(plane.isParallel(containedLine));
        Assert.assertEquals(0.0, plane.offset(containedLine), TEST_EPS);
    }

    @Test
    public void testIsParallelAndOffset_plane() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        final Plane parallelPlane = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        final Plane parallelPlane2 = Planes.fromPointAndNormal(Vector3D.of(0, 0, 2), Vector3D.of(0, 0, 1), TEST_PRECISION);
        final Plane parallelPlane3 = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(0, 0, 1), TEST_PRECISION).reverse();
        final Plane nonParallelPlane = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1),
                Vector3D.of(1, 1.5, 1).cross(Vector3D.of(0, 1, 1)), TEST_PRECISION);
        final Plane reversedPlane = plane.reverse();

        // act/assert
        Assert.assertTrue(plane.isParallel(parallelPlane));
        Assert.assertEquals(0.0, plane.offset(parallelPlane), TEST_EPS);

        Assert.assertTrue(plane.isParallel(parallelPlane2));
        Assert.assertEquals(1.0, plane.offset(parallelPlane2), TEST_EPS);

        Assert.assertTrue(plane.isParallel(parallelPlane3));
        Assert.assertEquals(-1.0, plane.offset(parallelPlane3), TEST_EPS);

        Assert.assertFalse(plane.isParallel(nonParallelPlane));
        Assert.assertEquals(0.0, plane.offset(nonParallelPlane), TEST_EPS);

        Assert.assertTrue(plane.isParallel(reversedPlane));
        Assert.assertEquals(0.0, plane.offset(nonParallelPlane), TEST_EPS);
    }

    @Test
    public void testOffset_point() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 1, 1);
        final Plane plane = Planes.fromPointAndNormal(p1, Vector3D.of(0.2, 0, 0), TEST_PRECISION);

        // act/assert
        Assert.assertEquals(-5.0, plane.offset(Vector3D.of(-4, 0, 0)), TEST_EPS);
        Assert.assertEquals(+5.0, plane.offset(Vector3D.of(6, 10, -12)), TEST_EPS);
        Assert.assertEquals(0.3,
                            plane.offset(Vector3D.linearCombination(1.0, p1, 0.3, plane.getNormal())),
                            TEST_EPS);
        Assert.assertEquals(-0.3,
                            plane.offset(Vector3D.linearCombination(1.0, p1, -0.3, plane.getNormal())),
                            TEST_EPS);
    }

    @Test
    public void testProject_point() {
        // arrange
        final Vector3D pt = Vector3D.of(-3, -2, -1);
        EuclideanTestUtils.permuteSkipZero(-4, 4, 1, (nx, ny, nz) -> {

            final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.of(nx, ny, nz), TEST_PRECISION);
            EuclideanTestUtils.permute(-4, 4, 1, (x, y, z) -> {

                final Vector3D p = Vector3D.of(x, y, z);

                // act
                final Vector3D proj = plane.project(p);

                // assert
                Assert.assertTrue(plane.contains(proj));
                Assert.assertEquals(0, plane.getOrigin().vectorTo(proj).dot(plane.getNormal()), TEST_EPS);
            });
        });
    }

    @Test
    public void testProject_line() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Line3D line = Lines3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(2, 0, 2), TEST_PRECISION);

        // act
        final Line3D projected = plane.project(line);

        // assert
        final Line3D expectedProjection = Lines3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(2, 0, 1), TEST_PRECISION);
        Assert.assertEquals(expectedProjection, projected);

        Assert.assertTrue(plane.contains(projected));

        Assert.assertTrue(projected.contains(Vector3D.of(1, 0, 1)));
        Assert.assertTrue(projected.contains(Vector3D.of(2, 0, 1)));
    }

    @Test
    public void testTransform_rotationAroundPoint() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        final AffineTransformMatrix3D mat = AffineTransformMatrix3D.createRotation(pt,
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO));

        // act
        final Plane result = plane.transform(mat);

        // assert
        checkPlane(result, Vector3D.ZERO, Vector3D.Unit.PLUS_X);
    }

    @Test
    public void testTransform_asymmetricScaling() {
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
        Assert.assertTrue(result.contains(transformedPt));
        Assert.assertFalse(plane.contains(transformedPt));
    }

    @Test
    public void testTransform_negateOneComponent() {
        // arrange
        final Vector3D pt = Vector3D.of(0, 0, 1);
        final Plane plane = Planes.fromPointAndNormal(pt, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.createScale(-1, 1,  1);

        // act
        final Plane result = plane.transform(transform);

        // assert
        checkPlane(result, Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_Z);

        Assert.assertFalse(transform.preservesOrientation());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_Z,
                transform.normalTransform().apply(plane.getNormal()), TEST_EPS);
    }

    @Test
    public void testTransform_negateTwoComponents() {
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
    public void testTransform_negateAllComponents() {
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
    public void testTransform_consistency() {
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

            Assert.assertTrue(result.contains(t.apply(p1)));
            Assert.assertTrue(result.contains(t.apply(p2)));
            Assert.assertTrue(result.contains(t.apply(p3)));

            Assert.assertFalse(result.contains(t.apply(notOnPlane1)));
            Assert.assertFalse(result.contains(t.apply(notOnPlane2)));
        });
    }

    @Test
    public void testRotate() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane plane  = Planes.fromPoints(p1, p2, p3, TEST_PRECISION);
        final Vector3D oldNormal = plane.getNormal();

        // act/assert
        plane = plane.rotate(p2, QuaternionRotation.fromAxisAngle(p2.subtract(p1), 1.7));
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertFalse(plane.contains(p3));

        plane = plane.rotate(p2, QuaternionRotation.fromAxisAngle(oldNormal, 0.1));
        Assert.assertFalse(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertFalse(plane.contains(p3));

        plane = plane.rotate(p1, QuaternionRotation.fromAxisAngle(oldNormal, 0.1));
        Assert.assertFalse(plane.contains(p1));
        Assert.assertFalse(plane.contains(p2));
        Assert.assertFalse(plane.contains(p3));
    }

    @Test
    public void testTranslate() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane plane  = Planes.fromPoints(p1, p2, p3, TEST_PRECISION);

        // act/assert
        plane = plane.translate(Vector3D.linearCombination(2.0, plane.getNormal().orthogonal()));
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertTrue(plane.contains(p3));

        plane = plane.translate(Vector3D.linearCombination(-1.2, plane.getNormal()));
        Assert.assertFalse(plane.contains(p1));
        Assert.assertFalse(plane.contains(p2));
        Assert.assertFalse(plane.contains(p3));

        plane = plane.translate(Vector3D.linearCombination(+1.2, plane.getNormal()));
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertTrue(plane.contains(p3));
    }

    @Test
    public void testIntersection_withLine() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.of(1, 2, 3), Vector3D.of(-4, 1, -5), TEST_PRECISION);
        final Line3D line = Lines3D.fromPoints(Vector3D.of(0.2, -3.5, 0.7), Vector3D.of(1.2, -2.5, -0.3), TEST_PRECISION);

        // act
        final Vector3D point = plane.intersection(line);

        // assert
        Assert.assertTrue(plane.contains(point));
        Assert.assertTrue(line.contains(point));
        Assert.assertNull(plane.intersection(Lines3D.fromPoints(Vector3D.of(10, 10, 10),
                                                  Vector3D.of(10, 10, 10).add(plane.getNormal().orthogonal()),
                                                  TEST_PRECISION)));
    }

    @Test
    public void testIntersection_withLine_noIntersection() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D normal = Vector3D.of(-4, 1, -5);

        final Plane plane = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);
        final Vector3D u = plane.getNormal().orthogonal();
        final Vector3D v = plane.getNormal().cross(u);

        // act/assert
        Assert.assertNull(plane.intersection(Lines3D.fromPoints(pt, pt.add(u), TEST_PRECISION)));

        final Vector3D offsetPt = pt.add(plane.getNormal());
        Assert.assertNull(plane.intersection(Lines3D.fromPoints(offsetPt, offsetPt.add(v), TEST_PRECISION)));
    }

    @Test
    public void testIntersection_withPlane() {
        // arrange
        final Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        final Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        final Plane planeA = Planes.fromPoints(p1, p2, Vector3D.of(-2.0, 4.3, 0.7), TEST_PRECISION);
        final Plane planeB = Planes.fromPoints(p1, Vector3D.of(11.4, -3.8, 5.1), p2, TEST_PRECISION);

        // act
        final Line3D line = planeA.intersection(planeB);

        // assert
        Assert.assertTrue(line.contains(p1));
        Assert.assertTrue(line.contains(p2));
        EuclideanTestUtils.assertCoordinatesEqual(planeA.getNormal().cross(planeB.getNormal()).normalize(),
                line.getDirection(), TEST_EPS);

        Assert.assertNull(planeA.intersection(planeA));
    }

    @Test
    public void testIntersection_withPlane_noIntersection() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        Assert.assertNull(plane.intersection(plane));
        Assert.assertNull(plane.intersection(plane.reverse()));

        Assert.assertNull(plane.intersection(Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION)));
        Assert.assertNull(plane.intersection(Planes.fromPointAndNormal(Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Z, TEST_PRECISION)));
    }

    @Test
    public void testIntersection_threePlanes() {
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
    public void testIntersection_threePlanes_intersectInLine() {
        // arrange
        final Plane a = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(1, 0, 0), TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(1, 0.5, 0), TEST_PRECISION);
        final Plane c = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(1, 1, 0), TEST_PRECISION);

        // act
        final Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assert.assertNull(result);
    }

    @Test
    public void testIntersection_threePlanes_twoParallel() {
        // arrange
        final Plane a = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final Plane c = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assert.assertNull(result);
    }

    @Test
    public void testIntersection_threePlanes_allParallel() {
        // arrange
        final Plane a = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Plane c = Planes.fromPointAndNormal(Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assert.assertNull(result);
    }

    @Test
    public void testIntersection_threePlanes_coincidentPlanes() {
        // arrange
        final Plane a = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final Plane c = b.reverse();

        // act
        final Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assert.assertNull(result);
    }

    @Test
    public void testSpan() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final PlaneConvexSubset sub = plane.span();

        // assert
        Assert.assertNotSame(plane, sub.getPlane());
        EuclideanTestUtils.assertCoordinatesEqual(plane.getOrigin(), sub.getPlane().getOrigin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(plane.getNormal(), sub.getPlane().getNormal(), TEST_EPS);

        Assert.assertTrue(sub.isFull());

        Assert.assertTrue(sub.contains(Vector3D.ZERO));
        Assert.assertTrue(sub.contains(Vector3D.of(1, 1, 0)));

        Assert.assertFalse(sub.contains(Vector3D.of(0, 0, 1)));
    }

    @Test
    public void testSimilarOrientation() {
        // arrange
        final Plane plane = Planes.fromNormal(Vector3D.of(1, 0, 0), TEST_PRECISION);

        // act/assert
        Assert.assertTrue(plane.similarOrientation(plane));
        Assert.assertTrue(plane.similarOrientation(Planes.fromNormal(Vector3D.of(1, 1, 0), TEST_PRECISION)));
        Assert.assertTrue(plane.similarOrientation(Planes.fromNormal(Vector3D.of(1, -1, 0), TEST_PRECISION)));

        Assert.assertFalse(plane.similarOrientation(Planes.fromNormal(Vector3D.of(0, 1, 0), TEST_PRECISION)));
        Assert.assertFalse(plane.similarOrientation(Planes.fromNormal(Vector3D.of(-1, 1, 0), TEST_PRECISION)));
        Assert.assertFalse(plane.similarOrientation(Planes.fromNormal(Vector3D.of(-1, 1, 0), TEST_PRECISION)));
        Assert.assertFalse(plane.similarOrientation(Planes.fromNormal(Vector3D.of(0, -1, 0), TEST_PRECISION)));
    }

    @Test
    public void testEq() {
        // arrange
        final double eps = 1e-3;
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D normal = Vector3D.Unit.PLUS_X;

        final Vector3D ptPrime = Vector3D.of(1.0001, 2.0001, 3.0001);
        final Vector3D normalPrime = Vector3D.Unit.of(1, 1e-4, 0);

        final Plane a = Planes.fromPointAndNormal(pt, normal, precision);

        final Plane b = Planes.fromPointAndNormal(Vector3D.of(2, 2, 3), normal, precision);
        final Plane c = Planes.fromPointAndNormal(pt, Vector3D.Unit.MINUS_X, precision);
        final Plane d = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);

        final Plane e = Planes.fromPointAndNormal(ptPrime, normalPrime, new EpsilonDoublePrecisionContext(eps));

        // act/assert
        Assert.assertTrue(a.eq(a, precision));

        Assert.assertFalse(a.eq(b, precision));
        Assert.assertFalse(a.eq(c, precision));

        Assert.assertTrue(a.eq(d, precision));
        Assert.assertTrue(a.eq(e, precision));
        Assert.assertTrue(e.eq(a, precision));
    }

    @Test
    public void testHashCode() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D normal = Vector3D.Unit.PLUS_X;

        final Plane a = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.of(2, 2, 3), normal, TEST_PRECISION);
        final Plane c = Planes.fromPointAndNormal(pt, Vector3D.of(1, 1, 0), TEST_PRECISION);
        final Plane d = Planes.fromPointAndNormal(pt, normal, new EpsilonDoublePrecisionContext(1e-8));
        final Plane e = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);

        // act/assert
        final int hash = a.hashCode();

        Assert.assertEquals(hash, a.hashCode());

        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertNotEquals(hash, c.hashCode());
        Assert.assertNotEquals(hash, d.hashCode());

        Assert.assertEquals(hash, e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);
        final Vector3D normal = Vector3D.Unit.PLUS_X;

        final Plane a = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);
        final Plane b = Planes.fromPointAndNormal(Vector3D.of(2, 2, 3), normal, TEST_PRECISION);
        final Plane c = Planes.fromPointAndNormal(pt, Vector3D.Unit.MINUS_X, TEST_PRECISION);
        final Plane d = Planes.fromPointAndNormal(pt, normal, new EpsilonDoublePrecisionContext(1e-8));
        final Plane e = Planes.fromPointAndNormal(pt, normal, TEST_PRECISION);

        // act/assert
        Assert.assertEquals(a, a);

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(a, d);

        Assert.assertEquals(a, e);
        Assert.assertEquals(e, a);
    }

    @Test
    public void testToString() {
        // arrange
        final Plane plane = Planes.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final String str = plane.toString();

        // assert
        Assert.assertTrue(str.startsWith("Plane["));
        Assert.assertTrue(str.matches(".*origin= \\(0(\\.0)?, 0(\\.0)?\\, 0(\\.0)?\\).*"));
        Assert.assertTrue(str.matches(".*normal= \\(0(\\.0)?, 0(\\.0)?\\, 1(\\.0)?\\).*"));
    }

    private static void checkPlane(final Plane plane, final Vector3D origin, final Vector3D normal) {
        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getOrigin(), TEST_EPS);
        Assert.assertTrue(plane.contains(origin));

        EuclideanTestUtils.assertCoordinatesEqual(normal, plane.getNormal(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getNormal().norm(), TEST_EPS);

        final double offset = plane.getOriginOffset();
        Assert.assertEquals(Vector3D.ZERO.distance(plane.getOrigin()), Math.abs(offset), TEST_EPS);
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
