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
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Plane.SubspaceTransform;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
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
        checkPlane(Plane.fromNormal(Vector3D.Unit.PLUS_X, TEST_PRECISION),
                Vector3D.ZERO, Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_Y);
        checkPlane(Plane.fromNormal(Vector3D.of(7, 0, 0), TEST_PRECISION),
                Vector3D.ZERO, Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_Y);

        checkPlane(Plane.fromNormal(Vector3D.Unit.PLUS_Y, TEST_PRECISION),
                Vector3D.ZERO, Vector3D.Unit.MINUS_Z, Vector3D.Unit.MINUS_X);
        checkPlane(Plane.fromNormal(Vector3D.of(0, 5, 0), TEST_PRECISION),
                Vector3D.ZERO, Vector3D.Unit.MINUS_Z, Vector3D.Unit.MINUS_X);

        checkPlane(Plane.fromNormal(Vector3D.Unit.PLUS_Z, TEST_PRECISION),
                Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X);
        checkPlane(Plane.fromNormal(Vector3D.of(0, 0, 0.01), TEST_PRECISION),
                Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X);
    }

    @Test
    public void testFromNormal_illegalArguments() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromNormal(Vector3D.ZERO, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromPointAndNormal() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert
        checkPlane(Plane.fromPointAndNormal(pt, Vector3D.of(0.1, 0, 0), TEST_PRECISION),
                Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_Y);
        checkPlane(Plane.fromPointAndNormal(pt, Vector3D.of(0, 2, 0), TEST_PRECISION),
                Vector3D.of(0, 2, 0), Vector3D.Unit.MINUS_Z, Vector3D.Unit.MINUS_X);
        checkPlane(Plane.fromPointAndNormal(pt, Vector3D.of(0, 0, 5), TEST_PRECISION),
                Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X);
    }

    @Test
    public void testFromPointAndNormal_illegalArguments() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPointAndNormal(pt, Vector3D.ZERO, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromPointAndPlaneVectors() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert
        checkPlane(Plane.fromPointAndPlaneVectors(pt, Vector3D.of(2, 0, 0), Vector3D.of(3, 0.1, 0),  TEST_PRECISION),
                Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y);

        checkPlane(Plane.fromPointAndPlaneVectors(pt, Vector3D.of(2, 0, 0), Vector3D.of(3, -0.1, 0),  TEST_PRECISION),
                Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_X, Vector3D.Unit.MINUS_Y);

        checkPlane(Plane.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0.1, 0), Vector3D.of(0, -3, 1),  TEST_PRECISION),
                Vector3D.of(1, 0, 0), Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z);
    }

    @Test
    public void testFromPointAndPlaneVectors_illegalArguments() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert

        // identical vectors
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        }, IllegalArgumentException.class);

        // zero vector
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.ZERO, TEST_PRECISION);
        }, IllegalArgumentException.class);

        // collinear vectors
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 2), TEST_PRECISION);
        }, IllegalArgumentException.class);

        // collinear vectors - reversed
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.of(0, 0, -2), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromPoints() {
        // arrange
        Vector3D a = Vector3D.of(1, 1, 1);
        Vector3D b = Vector3D.of(1, 1, 4.3);
        Vector3D c = Vector3D.of(2.5, 1, 1);

        // act/assert
        checkPlane(Plane.fromPoints(a, b, c, TEST_PRECISION),
                Vector3D.of(0, 1, 0), Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X);

        checkPlane(Plane.fromPoints(a, c, b, TEST_PRECISION),
                Vector3D.of(0, 1, 0), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z);
    }

    @Test
    public void testFromPoints_planeContainsSourcePoints() {
        // arrange
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);

        // act
        Plane plane  = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);

        // assert
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertTrue(plane.contains(p3));
    }

    @Test
    public void testFromPoints_illegalArguments() {
        // arrange
        Vector3D a = Vector3D.of(1, 0, 0);
        Vector3D b = Vector3D.of(0, 1, 0);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(a, a, a, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(a, a, b, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(a, b, a, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(b, a, a, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromPoints_collection_threePoints() {
        // arrange
        List<Vector3D> pts = Arrays.asList(
                    Vector3D.of(1, 1, 0),
                    Vector3D.of(1, 1, -1),
                    Vector3D.of(0, 1, 0)
                );

        // act
        Plane plane = Plane.fromPoints(pts, TEST_PRECISION);

        // assert
        checkPlane(plane, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Z, Vector3D.Unit.MINUS_X);

        Assert.assertTrue(plane.contains(pts.get(0)));
        Assert.assertTrue(plane.contains(pts.get(1)));
        Assert.assertTrue(plane.contains(pts.get(2)));
    }

    @Test
    public void testFromPoints_collection_someCollinearPoints() {
        // arrange
        List<Vector3D> pts = Arrays.asList(
                    Vector3D.of(1, 0, 2),
                    Vector3D.of(2, 0, 2),
                    Vector3D.of(3, 0, 2),
                    Vector3D.of(0, 1, 2)
                );

        // act
        Plane plane = Plane.fromPoints(pts, TEST_PRECISION);

        // assert
        checkPlane(plane, Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y);

        Assert.assertTrue(plane.contains(pts.get(0)));
        Assert.assertTrue(plane.contains(pts.get(1)));
        Assert.assertTrue(plane.contains(pts.get(2)));
        Assert.assertTrue(plane.contains(pts.get(3)));
    }

    @Test
    public void testFromPoints_collection_concaveWithCollinearAndDuplicatePoints() {
        // arrange
        List<Vector3D> pts = Arrays.asList(
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

        Vector3D origin = Vector3D.of(1, 0, 0);

        // act
        checkPlane(Plane.fromPoints(pts, TEST_PRECISION),
                origin, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Y);
        checkPlane(Plane.fromPoints(rotate(pts, 1), TEST_PRECISION),
                origin, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Y);

        checkPlane(Plane.fromPoints(rotate(pts, 2), TEST_PRECISION),
                origin, Vector3D.Unit.from(0, 1, -1), Vector3D.Unit.from(0, 1, 1));

        checkPlane(Plane.fromPoints(rotate(pts, 3), TEST_PRECISION),
                origin, Vector3D.Unit.from(0, 1, 1), Vector3D.Unit.from(0, -1, 1));

        checkPlane(Plane.fromPoints(rotate(pts, 4), TEST_PRECISION),
                origin, Vector3D.Unit.from(0, -1, -0.5), Vector3D.Unit.from(0, 0.5, -1));
        checkPlane(Plane.fromPoints(rotate(pts, 5), TEST_PRECISION),
                origin, Vector3D.Unit.from(0, -1, -0.5), Vector3D.Unit.from(0, 0.5, -1));

        checkPlane(Plane.fromPoints(rotate(pts, 6), TEST_PRECISION),
                origin, Vector3D.Unit.from(0, -1, 0.5), Vector3D.Unit.from(0, -0.5, -1));
        checkPlane(Plane.fromPoints(rotate(pts, 7), TEST_PRECISION),
                origin, Vector3D.Unit.from(0, -1, 0.5), Vector3D.Unit.from(0, -0.5, -1));
        checkPlane(Plane.fromPoints(rotate(pts, 8), TEST_PRECISION),
                origin, Vector3D.Unit.from(0, -1, 0.5), Vector3D.Unit.from(0, -0.5, -1));

        checkPlane(Plane.fromPoints(rotate(pts, 9), TEST_PRECISION),
                origin, Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_Y);
        checkPlane(Plane.fromPoints(rotate(pts, 10), TEST_PRECISION),
                origin, Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_Y);

        checkPlane(Plane.fromPoints(rotate(pts, 11), TEST_PRECISION),
                origin, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Y);
    }

    @Test
    public void testFromPoints_collection_choosesBestOrientation() {
        // act/assert
        checkPlane(Plane.fromPoints(Arrays.asList(
                Vector3D.of(1, 0, 2),
                Vector3D.of(2, 0, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(3.5, 1, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y);

        checkPlane(Plane.fromPoints(Arrays.asList(
                Vector3D.of(1, 0, 2),
                Vector3D.of(2, 0, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(3.5, -1, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_X, Vector3D.Unit.MINUS_Y);

        checkPlane(Plane.fromPoints(Arrays.asList(
                Vector3D.of(1, 0, 2),
                Vector3D.of(2, 0, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(3.5, -1, 2),
                Vector3D.of(4, 0, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y);

        checkPlane(Plane.fromPoints(Arrays.asList(
                Vector3D.of(1, 0, 2),
                Vector3D.of(2, 0, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(3.5, 1, 2),
                Vector3D.of(4, -1, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_X, Vector3D.Unit.MINUS_Y);

        checkPlane(Plane.fromPoints(Arrays.asList(
                Vector3D.of(0, 0, 2),
                Vector3D.of(1, 0, 2),
                Vector3D.of(1, 1, 2),
                Vector3D.of(0, 1, 2),
                Vector3D.of(0, 0, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y);

        checkPlane(Plane.fromPoints(Arrays.asList(
                Vector3D.of(0, 0, 2),
                Vector3D.of(0, 1, 2),
                Vector3D.of(1, 1, 2),
                Vector3D.of(1, 0, 2),
                Vector3D.of(0, 0, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X);

        checkPlane(Plane.fromPoints(Arrays.asList(
                Vector3D.of(0, 0, 2),
                Vector3D.of(1, 0, 2),
                Vector3D.of(2, 1, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(2, 4, 2),
                Vector3D.of(0, 0, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y);

        checkPlane(Plane.fromPoints(Arrays.asList(
                Vector3D.of(0, 0, 2),
                Vector3D.of(0, 1, 2),
                Vector3D.of(2, 4, 2),
                Vector3D.of(3, 0, 2),
                Vector3D.of(2, 1, 2),
                Vector3D.of(0, 0, 2)
            ), TEST_PRECISION), Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X);
    }

    @Test
    public void testFromPoints_collection_illegalArguments() {
        // arrange
        Vector3D a = Vector3D.ZERO;
        Vector3D b = Vector3D.Unit.PLUS_X;

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(Arrays.asList(), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(Arrays.asList(a), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(Arrays.asList(a, b), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testFromPoints_collection_allPointsCollinear() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.Unit.PLUS_X,
                        Vector3D.of(2, 0, 0)
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(Arrays.asList(
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
            Plane.fromPoints(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.ZERO,
                        Vector3D.of(1e-12, 1e-12, 0),
                        Vector3D.Unit.PLUS_X
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(Arrays.asList(
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
            Plane.fromPoints(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.Unit.PLUS_X,
                        Vector3D.Unit.PLUS_Y,
                        Vector3D.Unit.PLUS_Z
                    ), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testContains_point() {
        // arrange
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        double halfEps = 0.5 * TEST_EPS;

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
        Plane plane = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(plane.contains(
                Line3D.fromPoints(Vector3D.of(1, 0, 0), Vector3D.of(2, 0, 0), TEST_PRECISION)));
        Assert.assertTrue(plane.contains(
                Line3D.fromPoints(Vector3D.of(-1, 0, 0), Vector3D.of(-2, 0, 0), TEST_PRECISION)));

        Assert.assertFalse(plane.contains(
                Line3D.fromPoints(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 2), TEST_PRECISION)));
        Assert.assertFalse(plane.contains(
                Line3D.fromPoints(Vector3D.ZERO, Vector3D.of(2, 0, 2), TEST_PRECISION)));
    }

    @Test
    public void testContains_plane() {
        // arrange
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane planeA = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(planeA.contains(planeA));
        Assert.assertTrue(planeA.contains(Plane.fromPoints(p1, p3, p2, TEST_PRECISION)));
        Assert.assertTrue(planeA.contains(Plane.fromPoints(p3, p1, p2, TEST_PRECISION)));
        Assert.assertTrue(planeA.contains(Plane.fromPoints(p3, p2, p1, TEST_PRECISION)));

        Assert.assertFalse(planeA.contains(Plane.fromPoints(p1, Vector3D.of(11.4, -3.8, 5.1), p2, TEST_PRECISION)));

        Vector3D offset = planeA.getNormal().multiply(1e-8);
        Assert.assertFalse(planeA.contains(Plane.fromPoints(p1.add(offset), p2, p3, TEST_PRECISION)));
        Assert.assertFalse(planeA.contains(Plane.fromPoints(p1, p2.add(offset), p3, TEST_PRECISION)));
        Assert.assertFalse(planeA.contains(Plane.fromPoints(p1, p2, p3.add(offset), TEST_PRECISION)));

        Assert.assertFalse(planeA.contains(Plane.fromPoints(p1.add(offset),
                p2.add(offset),
                p3.add(offset), TEST_PRECISION)));
    }

    @Test
    public void testReverse() {
        // arrange
        Vector3D pt = Vector3D.of(0, 0, 1);
        Plane plane = Plane.fromPointAndPlaneVectors(pt, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        Plane reversed = plane.reverse();

        // assert
        checkPlane(reversed, pt, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X);

        Assert.assertTrue(reversed.contains(Vector3D.of(1, 1, 1)));
        Assert.assertTrue(reversed.contains(Vector3D.of(-1, -1, 1)));
        Assert.assertFalse(reversed.contains(Vector3D.ZERO));

        Assert.assertEquals(1.0, reversed.offset(Vector3D.ZERO), TEST_EPS);
    }

    @Test
    public void testIsParallelAndOffset_line() {
        // arrange
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);

        Line3D parallelLine = Line3D.fromPoints(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 2), TEST_PRECISION);
        Line3D nonParallelLine = Line3D.fromPoints(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 1), TEST_PRECISION);
        Line3D containedLine = Line3D.fromPoints(Vector3D.of(2, 0, 1), Vector3D.of(1, 0, 1), TEST_PRECISION);

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
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Plane parallelPlane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Plane parallelPlane2 = Plane.fromPointAndNormal(Vector3D.of(0, 0, 2), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Plane parallelPlane3 = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(0, 0, 1), TEST_PRECISION).reverse();
        Plane nonParallelPlane = Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
                Vector3D.of(1, 1.5, 1), Vector3D.of(0, 1, 1), TEST_PRECISION);
        Plane reversedPlane = plane.reverse();

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
    public void testProject_line() {
        // arrange
        Plane plane = Plane.fromPointAndNormal(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Line3D line = Line3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(2, 0, 2), TEST_PRECISION);

        // act
        Line3D projected = plane.project(line);

        // assert
        Line3D expectedProjection = Line3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(2, 0, 1), TEST_PRECISION);
        Assert.assertEquals(expectedProjection, projected);

        Assert.assertTrue(plane.contains(projected));

        Assert.assertTrue(projected.contains(Vector3D.of(1, 0, 1)));
        Assert.assertTrue(projected.contains(Vector3D.of(2, 0, 1)));
    }

    @Test
    public void testOffset_point() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 1, 1);
        Plane plane = Plane.fromPointAndNormal(p1, Vector3D.of(0.2, 0, 0), TEST_PRECISION);

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
    public void testPointAt() {
        // arrange
        Vector3D pt = Vector3D.of(0, 0, 1);
        Plane plane = Plane.fromPointAndPlaneVectors(pt, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(pt, plane.pointAt(Vector2D.ZERO, 0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, plane.pointAt(Vector2D.ZERO, -1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, -1), plane.pointAt(Vector2D.ZERO, -2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 2), plane.pointAt(Vector2D.ZERO, 1), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 2, 1), plane.pointAt(Vector2D.of(2, 1), 0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(4, -3, 6), plane.pointAt(Vector2D.of(-3, -4), 5), TEST_EPS);
    }

    @Test
    public void testTransform_rotationAroundPoint() {
        // arrange
        Vector3D pt = Vector3D.of(0, 0, 1);
        Plane plane = Plane.fromPointAndPlaneVectors(pt, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X, TEST_PRECISION);

        AffineTransformMatrix3D mat = AffineTransformMatrix3D.createRotation(pt,
                QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO));

        // act
        Plane result = plane.transform(mat);

        // assert
        checkPlane(result, Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Z);
    }

    @Test
    public void testTransform_asymmetricScaling() {
        // arrange
        Vector3D pt = Vector3D.of(0, 1, 0);
        Plane plane = Plane.fromPointAndPlaneVectors(pt, Vector3D.Unit.MINUS_Z, Vector3D.of(-1, 1, 0), TEST_PRECISION);

        AffineTransformMatrix3D mat = AffineTransformMatrix3D.createScale(2, 1, 1);

        // act
        Plane result = plane.transform(mat);

        // assert
        Vector3D expectedU = Vector3D.Unit.MINUS_Z;
        Vector3D expectedV = Vector3D.Unit.of(-2, 1, 0);
        Vector3D expectedNormal = Vector3D.Unit.of(1, 2, 0);

        Vector3D transformedPt = mat.apply(plane.getOrigin());
        Vector3D expectedOrigin = transformedPt.project(expectedNormal);

        checkPlane(result, expectedOrigin, expectedU, expectedV);

        Assert.assertTrue(result.contains(transformedPt));
        Assert.assertFalse(plane.contains(transformedPt));
    }

    @Test
    public void testTransform_negateOneComponent() {
        // arrange
        Vector3D pt = Vector3D.of(0, 0, 1);
        Plane plane = Plane.fromPointAndPlaneVectors(pt, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        Transform3D transform = FunctionTransform3D.from(v -> Vector3D.of(-v.getX(), v.getY(), v.getZ()));

        // act
        Plane result = plane.transform(transform);

        // assert
        checkPlane(result, Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Y);
    }

    @Test
    public void testTransform_negateTwoComponents() {
        // arrange
        Vector3D pt = Vector3D.of(0, 0, 1);
        Plane plane = Plane.fromPointAndPlaneVectors(pt, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        Transform3D transform = FunctionTransform3D.from(v -> Vector3D.of(-v.getX(), -v.getY(), v.getZ()));

        // act
        Plane result = plane.transform(transform);

        // assert
        checkPlane(result, Vector3D.of(0, 0, 1), Vector3D.Unit.MINUS_X, Vector3D.Unit.MINUS_Y);
    }

    @Test
    public void testTransform_negateAllComponents() {
        // arrange
        Vector3D pt = Vector3D.of(0, 0, 1);
        Plane plane = Plane.fromPointAndPlaneVectors(pt, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        Transform3D transform = FunctionTransform3D.from(Vector3D::negate);

        // act
        Plane result = plane.transform(transform);

        // assert
        checkPlane(result, Vector3D.of(0, 0, -1), Vector3D.Unit.MINUS_X, Vector3D.Unit.MINUS_Y);
    }

    @Test
    public void testSubspaceTransform() {
        // arrange
        Plane plane = Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act/assert
        checkSubspaceTransform(plane.subspaceTransform(AffineTransformMatrix3D.createScale(2, 3, 4)),
                Vector3D.of(0, 0, 4), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y,
                Vector3D.of(0, 0, 4), Vector3D.of(2, 0, 4), Vector3D.of(0, 3, 4));

        checkSubspaceTransform(plane.subspaceTransform(AffineTransformMatrix3D.createTranslation(2, 3, 4)),
                Vector3D.of(0, 0, 5), Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y,
                Vector3D.of(2, 3, 5), Vector3D.of(3, 3, 5), Vector3D.of(2, 4, 5));

        checkSubspaceTransform(plane.subspaceTransform(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Y, PlaneAngleRadians.PI_OVER_TWO)),
                Vector3D.of(1, 0, 0), Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Y,
                Vector3D.of(1, 0, 0), Vector3D.of(1, 0, -1), Vector3D.of(1, 1, 0));
    }

    private void checkSubspaceTransform(SubspaceTransform st,
            Vector3D origin, Vector3D u, Vector3D v,
            Vector3D tOrigin, Vector3D tU, Vector3D tV) {

        Plane plane = st.getPlane();
        AffineTransformMatrix2D transform = st.getTransform();

        checkPlane(plane, origin, u, v);

        EuclideanTestUtils.assertCoordinatesEqual(tOrigin, plane.toSpace(transform.apply(Vector2D.ZERO)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(tU, plane.toSpace(transform.apply(Vector2D.Unit.PLUS_X)), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(tV, plane.toSpace(transform.apply(Vector2D.Unit.PLUS_Y)), TEST_EPS);
    }

    @Test
    public void testSubspaceTransform_transformsPointsCorrectly() {
        // arrange
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(1, 2, 3), Vector3D.of(1, 1, 1), TEST_PRECISION);

        EuclideanTestUtils.permuteSkipZero(-2, 2, 0.5, (a, b, c) -> {
            // create a somewhat complicate transform to try to hit all of the edge cases
            AffineTransformMatrix3D transform = AffineTransformMatrix3D.createTranslation(Vector3D.of(a, b, c))
                    .rotate(QuaternionRotation.fromAxisAngle(Vector3D.of(b, c, a), PlaneAngleRadians.PI * c))
                    .scale(0.1, 4, 8);

            // act
            SubspaceTransform st = plane.subspaceTransform(transform);

            // assert
            EuclideanTestUtils.permute(-5, 5, 1, (x, y) -> {
                Vector2D subPt = Vector2D.of(x, y);
                Vector3D expected = transform.apply(plane.toSpace(subPt));
                Vector3D actual = st.getPlane().toSpace(
                        st.getTransform().apply(subPt));

                EuclideanTestUtils.assertCoordinatesEqual(expected, actual, TEST_EPS);
            });
        });
    }

    @Test
    public void testRotate() {
        // arrange
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane plane  = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);
        Vector3D oldNormal = plane.getNormal();

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
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane plane  = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);

        // act/assert
        plane = plane.translate(Vector3D.linearCombination(2.0, plane.getU(), -1.5, plane.getV()));
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
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(1, 2, 3), Vector3D.of(-4, 1, -5), TEST_PRECISION);
        Line3D line = Line3D.fromPoints(Vector3D.of(0.2, -3.5, 0.7), Vector3D.of(1.2, -2.5, -0.3), TEST_PRECISION);

        // act
        Vector3D point = plane.intersection(line);

        // assert
        Assert.assertTrue(plane.contains(point));
        Assert.assertTrue(line.contains(point));
        Assert.assertNull(plane.intersection(Line3D.fromPoints(Vector3D.of(10, 10, 10),
                                                  Vector3D.of(10, 10, 10).add(plane.getNormal().orthogonal()),
                                                  TEST_PRECISION)));
    }

    @Test
    public void testIntersection_withLine_noIntersection() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);
        Vector3D normal = Vector3D.of(-4, 1, -5);

        Plane plane = Plane.fromPointAndNormal(pt, normal, TEST_PRECISION);

        // act/assert
        Assert.assertNull(plane.intersection(Line3D.fromPoints(pt, pt.add(plane.getU()), TEST_PRECISION)));

        Vector3D offsetPt = pt.add(plane.getNormal());
        Assert.assertNull(plane.intersection(Line3D.fromPoints(offsetPt, offsetPt.add(plane.getV()), TEST_PRECISION)));
    }

    @Test
    public void testIntersection_withPlane() {
        // arrange
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Plane planeA = Plane.fromPoints(p1, p2, Vector3D.of(-2.0, 4.3, 0.7), TEST_PRECISION);
        Plane planeB = Plane.fromPoints(p1, Vector3D.of(11.4, -3.8, 5.1), p2, TEST_PRECISION);

        // act
        Line3D line = planeA.intersection(planeB);

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
        Plane plane = Plane.fromPointAndNormal(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        Assert.assertNull(plane.intersection(plane));
        Assert.assertNull(plane.intersection(plane.reverse()));

        Assert.assertNull(plane.intersection(Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION)));
        Assert.assertNull(plane.intersection(Plane.fromPointAndNormal(Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Z, TEST_PRECISION)));
    }

    @Test
    public void testIntersection_threePlanes() {
        // arrange
        Vector3D pt = Vector3D.of(1.2, 3.4, -5.8);
        Plane a = Plane.fromPointAndNormal(pt, Vector3D.of(1, 3, 3), TEST_PRECISION);
        Plane b = Plane.fromPointAndNormal(pt, Vector3D.of(-2, 4, 0), TEST_PRECISION);
        Plane c = Plane.fromPointAndNormal(pt, Vector3D.of(7, 0, -4), TEST_PRECISION);

        // act
        Vector3D result = Plane.intersection(a, b, c);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(pt, result, TEST_EPS);
    }

    @Test
    public void testIntersection_threePlanes_intersectInLine() {
        // arrange
        Plane a = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(1, 0, 0), TEST_PRECISION);
        Plane b = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(1, 0.5, 0), TEST_PRECISION);
        Plane c = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.of(1, 1, 0), TEST_PRECISION);

        // act
        Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assert.assertNull(result);
    }

    @Test
    public void testIntersection_threePlanes_twoParallel() {
        // arrange
        Plane a = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Plane b = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        Plane c = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assert.assertNull(result);
    }

    @Test
    public void testIntersection_threePlanes_allParallel() {
        // arrange
        Plane a = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Plane b = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Plane c = Plane.fromPointAndNormal(Vector3D.of(0, 0, 2), Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assert.assertNull(result);
    }

    @Test
    public void testIntersection_threePlanes_coincidentPlanes() {
        // arrange
        Plane a = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Plane b = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        Plane c = b.reverse();

        // act
        Vector3D result = Plane.intersection(a, b, c);

        // assert
        Assert.assertNull(result);
    }

    @Test
    public void testSpan() {
        // arrange
        Plane plane = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Facet sub = plane.span();

        // assert
        Assert.assertSame(plane, sub.getPlane());
        Assert.assertTrue(sub.isFull());

        Assert.assertTrue(sub.contains(Vector3D.ZERO));
        Assert.assertTrue(sub.contains(Vector3D.of(1, 1, 0)));

        Assert.assertFalse(sub.contains(Vector3D.of(0, 0, 1)));
    }

    @Test
    public void testSimilarOrientation() {
        // arrange
        Plane plane = Plane.fromNormal(Vector3D.of(1, 0, 0), TEST_PRECISION);

        // act/assert
        Assert.assertTrue(plane.similarOrientation(plane));
        Assert.assertTrue(plane.similarOrientation(Plane.fromNormal(Vector3D.of(1, 1, 0), TEST_PRECISION)));
        Assert.assertTrue(plane.similarOrientation(Plane.fromNormal(Vector3D.of(1, -1, 0), TEST_PRECISION)));

        Assert.assertFalse(plane.similarOrientation(Plane.fromNormal(Vector3D.of(0, 1, 0), TEST_PRECISION)));
        Assert.assertFalse(plane.similarOrientation(Plane.fromNormal(Vector3D.of(-1, 1, 0), TEST_PRECISION)));
        Assert.assertFalse(plane.similarOrientation(Plane.fromNormal(Vector3D.of(-1, 1, 0), TEST_PRECISION)));
        Assert.assertFalse(plane.similarOrientation(Plane.fromNormal(Vector3D.of(0, -1, 0), TEST_PRECISION)));
    }

    @Test
    public void testEq() {
        // arrange
        double eps = 1e-3;
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        Vector3D pt = Vector3D.of(1, 2, 3);
        Vector3D u = Vector3D.Unit.PLUS_X;
        Vector3D v = Vector3D.Unit.PLUS_Y;

        Vector3D ptPrime = Vector3D.of(1.0001, 2.0001, 3.0001);
        Vector3D uPrime = Vector3D.Unit.of(1, 1e-4, 0);
        Vector3D vPrime = Vector3D.Unit.of(0, 1, 1e-4);

        Plane a = Plane.fromPointAndPlaneVectors(pt, u, v, precision);

        Plane b = Plane.fromPointAndPlaneVectors(Vector3D.of(1, 2, 4), u, v, precision);
        Plane c = Plane.fromPointAndPlaneVectors(pt, Vector3D.Unit.MINUS_X, v, precision);
        Plane d = Plane.fromPointAndPlaneVectors(pt, u, Vector3D.Unit.MINUS_Y, precision);
        Plane e = Plane.fromPointAndPlaneVectors(pt, u, v, TEST_PRECISION);

        Plane f = Plane.fromPointAndPlaneVectors(ptPrime, uPrime, vPrime, new EpsilonDoublePrecisionContext(eps));

        // act/assert
        Assert.assertTrue(a.eq(a, precision));

        Assert.assertFalse(a.eq(b, precision));
        Assert.assertFalse(a.eq(c, precision));
        Assert.assertFalse(a.eq(d, precision));

        Assert.assertTrue(a.eq(e, precision));
        Assert.assertTrue(a.eq(f, precision));
        Assert.assertTrue(f.eq(a, precision));
    }

    @Test
    public void testHashCode() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);
        Vector3D u = Vector3D.Unit.PLUS_X;
        Vector3D v = Vector3D.Unit.PLUS_Y;

        Plane a = Plane.fromPointAndPlaneVectors(pt, u, v, TEST_PRECISION);
        Plane b = Plane.fromPointAndPlaneVectors(Vector3D.of(1, 2, 4), u, v, TEST_PRECISION);
        Plane c = Plane.fromPointAndPlaneVectors(pt, Vector3D.of(1, 1, 0), v, TEST_PRECISION);
        Plane d = Plane.fromPointAndPlaneVectors(pt, u, Vector3D.Unit.MINUS_Y, TEST_PRECISION);
        Plane e = Plane.fromPointAndPlaneVectors(pt, u, v, new EpsilonDoublePrecisionContext(1e-8));
        Plane f = Plane.fromPointAndPlaneVectors(pt, u, v, TEST_PRECISION);

        // act/assert
        int hash = a.hashCode();

        Assert.assertEquals(hash, a.hashCode());

        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertNotEquals(hash, c.hashCode());
        Assert.assertNotEquals(hash, d.hashCode());
        Assert.assertNotEquals(hash, e.hashCode());

        Assert.assertEquals(hash, f.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);
        Vector3D u = Vector3D.Unit.PLUS_X;
        Vector3D v = Vector3D.Unit.PLUS_Y;

        Plane a = Plane.fromPointAndPlaneVectors(pt, u, v, TEST_PRECISION);
        Plane b = Plane.fromPointAndPlaneVectors(Vector3D.of(1, 2, 4), u, v, TEST_PRECISION);
        Plane c = Plane.fromPointAndPlaneVectors(pt, Vector3D.Unit.MINUS_X, v, TEST_PRECISION);
        Plane d = Plane.fromPointAndPlaneVectors(pt, u, Vector3D.Unit.MINUS_Y, TEST_PRECISION);
        Plane e = Plane.fromPointAndPlaneVectors(pt, u, v, new EpsilonDoublePrecisionContext(1e-8));
        Plane f = Plane.fromPointAndPlaneVectors(pt, u, v, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(a.equals(d));
        Assert.assertFalse(a.equals(e));

        Assert.assertTrue(a.equals(f));
        Assert.assertTrue(f.equals(a));
    }

    @Test
    public void testToString() {
        // arrange
        Plane plane = Plane.fromPointAndPlaneVectors(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y, TEST_PRECISION);

        // act
        String str = plane.toString();

        // assert
        Assert.assertTrue(str.startsWith("Plane["));
        Assert.assertTrue(str.matches(".*origin= \\(0(\\.0)?, 0(\\.0)?\\, 0(\\.0)?\\).*"));
        Assert.assertTrue(str.matches(".*u= \\(1(\\.0)?, 0(\\.0)?\\, 0(\\.0)?\\).*"));
        Assert.assertTrue(str.matches(".*v= \\(0(\\.0)?, 1(\\.0)?\\, 0(\\.0)?\\).*"));
        Assert.assertTrue(str.matches(".*w= \\(0(\\.0)?, 0(\\.0)?\\, 1(\\.0)?\\).*"));
    }

    private static void checkPlane(Plane plane, Vector3D origin, Vector3D u, Vector3D v) {
        u = u.normalize();
        v = v.normalize();
        Vector3D w = u.cross(v);

        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getOrigin(), TEST_EPS);
        Assert.assertTrue(plane.contains(origin));

        EuclideanTestUtils.assertCoordinatesEqual(u, plane.getU(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getU().norm(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(v, plane.getV(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getV().norm(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(w, plane.getW(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getW().norm(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(w, plane.getNormal(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getNormal().norm(), TEST_EPS);

        double offset = plane.getOriginOffset();
        Assert.assertEquals(Vector3D.ZERO.distance(plane.getOrigin()), Math.abs(offset), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getNormal().multiply(-offset), TEST_EPS);
    }

    private static <T> List<T> rotate(List<T> list, int shift) {
        int size = list.size();

        List<T> result = new ArrayList<>(size);

        for (int i = 0; i < size; ++i) {
            result.add(list.get((i + shift) % size));
        }

        return result;
    }
}
