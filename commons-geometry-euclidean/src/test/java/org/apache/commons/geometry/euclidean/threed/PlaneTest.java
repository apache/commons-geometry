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
import org.apache.commons.geometry.core.exception.GeometryException;
import org.apache.commons.geometry.core.exception.IllegalNormException;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class PlaneTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test(expected=IllegalNormException.class)
    public void testUAndVAreIdentical() {
        Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
    }

    @Test(expected=IllegalNormException.class)
    public void testUAndVAreCollinear() {
        Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 2), TEST_PRECISION);
    }

    @Test(expected=IllegalNormException.class)
    public void testUAndVAreCollinear2() {
        Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), Vector3D.of(0, 0, -2), TEST_PRECISION);
    }

    @Test(expected=GeometryException.class)
    public void testPointsDoNotConstituteAPlane() {
        Plane.fromPoints(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), Vector3D.of(0, 1, 0), TEST_PRECISION);
    }

    @Test
    public void testContains() {
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Assert.assertTrue(plane.contains(Vector3D.of(0, 0, 1)));
        Assert.assertTrue(plane.contains(Vector3D.of(17, -32, 1)));
        Assert.assertTrue(! plane.contains(Vector3D.of(17, -32, 1.001)));
    }

    @Test
    public void testContainsLine() {
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Line line = new Line(Vector3D.of(1, 0, 1), Vector3D.of(2, 0, 1), TEST_PRECISION);
        Assert.assertTrue(plane.contains(line));
    }

    @Test(expected=IllegalNormException.class)
    public void testFromPointPlaneVectorsWithZeroVector()
    {
        Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.ZERO, Vector3D.of(1,0,0), TEST_PRECISION);
    }

    @Test(expected=IllegalNormException.class)
    public void testFromPointAndNormalWithZeroNormal()
    {
        Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.ZERO, TEST_PRECISION);
    }

    @Test(expected=IllegalNormException.class)
    public void testFromNormal()
    {
        Plane.fromNormal(Vector3D.ZERO, TEST_PRECISION);
    }

    @Test
    public void testIsParallelAndGetOffset()
    {
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Line parallelLine = new Line(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 2), TEST_PRECISION);
        Assert.assertTrue(plane.isParallel(parallelLine));
        Assert.assertEquals(1.0, plane.getOffset(parallelLine), TEST_EPS);
        Line nonParallelLine = new Line(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 1), TEST_PRECISION);
        Assert.assertFalse(plane.isParallel(nonParallelLine));
        Assert.assertEquals(0.0, plane.getOffset(nonParallelLine), TEST_EPS);
    }

    @Test
    public void testCreation()
    {
        Vector3D normalAliasW =  Vector3D.of(0, 0, 1);
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1),normalAliasW , TEST_PRECISION);
        Assert.assertEquals(normalAliasW, plane.getW());
        double expectedX = 1.0;
        Assert.assertEquals(expectedX,  Math.abs(plane.getV().getX()), TEST_EPS);
        double expectedY = 1.0;
        Assert.assertEquals(expectedY,  Math.abs(plane.getU().getY()), TEST_EPS);
        Assert.assertEquals(-1.0, plane.getOriginOffset(), TEST_EPS);
        Vector3D expectedOrigin = Vector3D.of(0, 0, 1);
        Assert.assertEquals(expectedOrigin, plane.getOrigin());
    }

    @Test
    public void testReverse()
    {
        Vector3D normalAliasW =  Vector3D.of(0, 0, 1);
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1),normalAliasW , TEST_PRECISION);
        Assert.assertEquals(-1.0, plane.getOriginOffset(), TEST_EPS);
        Vector3D p1 = Vector3D.of(1,0,1);
        Assert.assertTrue(plane.contains(p1));
        Plane reversePlane = plane.reverse();
        Assert.assertEquals(1.0, reversePlane.getOriginOffset(), TEST_EPS);
        Vector3D p1XYswapped = Vector3D.of(0,1,1);
        Assert.assertTrue(reversePlane.contains(p1XYswapped));
    }

    @Test
    public void testIsPlaneParallel()
    {
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Plane parallelPlane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Plane parallelPlane2 = Plane.fromPointAndNormal(Vector3D.of(0, 0, 2), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Assert.assertTrue(plane.isParallel(parallelPlane));
        Assert.assertTrue(plane.isParallel(parallelPlane2));
        Plane nonParallelPlane = Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.of(1, 1.5, 1), Vector3D.of(0,1,1), TEST_PRECISION);
        Assert.assertFalse(plane.isParallel(nonParallelPlane));
    }

    @Test
    public void testProjectLine() {
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Line line = new Line(Vector3D.of(1, 0, 1), Vector3D.of(2, 0, 2), TEST_PRECISION);
        Line expectedProjection = new Line(Vector3D.of(1, 0, 1),Vector3D.of(2, 0, 1), TEST_PRECISION);
        Assert.assertEquals(expectedProjection, plane.project(line));
    }

    @Test
    public void testOffset() {
        Vector3D p1 = Vector3D.of(1, 1, 1);
        Plane plane = Plane.fromPointAndNormal(p1, Vector3D.of(0.2, 0, 0), TEST_PRECISION);
        Assert.assertEquals(-5.0, plane.getOffset(Vector3D.of(-4, 0, 0)), TEST_EPS);
        Assert.assertEquals(+5.0, plane.getOffset(Vector3D.of(6, 10, -12)), TEST_EPS);
        Assert.assertEquals(0.3,
                            plane.getOffset(Vector3D.linearCombination(1.0, p1, 0.3, plane.getNormal())),
                            TEST_EPS);
        Assert.assertEquals(-0.3,
                            plane.getOffset(Vector3D.linearCombination(1.0, p1, -0.3, plane.getNormal())),
                            TEST_EPS);
    }

    @Test(expected=IllegalNormException.class)
    public void testVectorsAreColinear()
    {
      Plane.fromPointAndPlaneVectors(Vector3D.of(1, 1, 1), Vector3D.of(2, 0, 0), Vector3D.of(2,0,0), TEST_PRECISION);
    }


    @Test
    public void testVectorsAreNormalizedForSuppliedUAndV() {
        Plane plane = Plane.fromPointAndPlaneVectors(Vector3D.of(1, 1, 1), Vector3D.of(2, 0, 0), Vector3D.of(0,2,0), TEST_PRECISION);
        Assert.assertEquals(1.0, plane.getNormal().norm(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getV().norm(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getU().norm(), TEST_EPS);
    }



    @Test
    public void testVectorsAreNormalized() {
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(2, -3, 1), Vector3D.of(1, 4, 9), TEST_PRECISION);
        Assert.assertEquals(1.0, plane.getNormal().norm(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getV().norm(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getU().norm(), TEST_EPS);
    }


    @Test
    public void testPoint() {
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(2, -3, 1), Vector3D.of(1, 4, 9), TEST_PRECISION);
        Assert.assertTrue(plane.contains(plane.getOrigin()));
    }

    @Test
    public void testThreePoints() {
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane    plane  = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertTrue(plane.contains(p3));
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
        }, GeometryException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.Unit.PLUS_X,
                        Vector3D.of(2, 0, 0),
                        Vector3D.of(3, 0, 0)
                    ), TEST_PRECISION);
        }, GeometryException.class);
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
        }, GeometryException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(Arrays.asList(
                        Vector3D.ZERO,
                        Vector3D.of(1e-12, 0, 0),
                        Vector3D.ZERO
                    ), TEST_PRECISION);
        }, GeometryException.class);
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
        }, GeometryException.class);
    }

    @Test
    public void testRotate() {
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane    plane  = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);
        Vector3D oldNormal = plane.getNormal();

        plane = plane.rotate(p2, QuaternionRotation.fromAxisAngle(p2.subtract(p1), 1.7));
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertTrue(! plane.contains(p3));

        plane = plane.rotate(p2, QuaternionRotation.fromAxisAngle(oldNormal, 0.1));
        Assert.assertTrue(! plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertTrue(! plane.contains(p3));

        plane = plane.rotate(p1, QuaternionRotation.fromAxisAngle(oldNormal, 0.1));
        Assert.assertTrue(! plane.contains(p1));
        Assert.assertTrue(! plane.contains(p2));
        Assert.assertTrue(! plane.contains(p3));

    }

    @Test
    public void testTranslate() {
        Vector3D p1 = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2 = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3 = Vector3D.of(-2.0, 4.3, 0.7);
        Plane    plane  = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);

        plane = plane.translate(Vector3D.linearCombination(2.0, plane.getU(), -1.5, plane.getV()));
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertTrue(plane.contains(p3));

        plane = plane.translate(Vector3D.linearCombination(-1.2, plane.getNormal()));
        Assert.assertTrue(! plane.contains(p1));
        Assert.assertTrue(! plane.contains(p2));
        Assert.assertTrue(! plane.contains(p3));

        plane = plane.translate(Vector3D.linearCombination(+1.2, plane.getNormal()));
        Assert.assertTrue(plane.contains(p1));
        Assert.assertTrue(plane.contains(p2));
        Assert.assertTrue(plane.contains(p3));

    }

    @Test
    public void testIntersection() {
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(1, 2, 3), Vector3D.of(-4, 1, -5), TEST_PRECISION);
        Line  line = new Line(Vector3D.of(0.2, -3.5, 0.7), Vector3D.of(1.2, -2.5, -0.3), TEST_PRECISION);
        Vector3D point = plane.intersection(line);
        Assert.assertTrue(plane.contains(point));
        Assert.assertTrue(line.contains(point));
        Assert.assertNull(plane.intersection(new Line(Vector3D.of(10, 10, 10),
                                                  Vector3D.of(10, 10, 10).add(plane.getNormal().orthogonal()),
                                                  TEST_PRECISION)));
    }

    @Test
    public void testIntersection2() {
        Vector3D p1  = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2  = Vector3D.of(3.4, -5.8, 1.2);
        Plane    planeA  = Plane.fromPoints(p1, p2, Vector3D.of(-2.0, 4.3, 0.7), TEST_PRECISION);
        Plane    planeB  = Plane.fromPoints(p1, Vector3D.of(11.4, -3.8, 5.1), p2, TEST_PRECISION);
        Line     line   = planeA.intersection(planeB);
        Assert.assertTrue(line.contains(p1));
        Assert.assertTrue(line.contains(p2));
        Assert.assertNull(planeA.intersection(planeA));
    }

    @Test
    public void testIntersection3() {
        Vector3D reference = Vector3D.of(1.2, 3.4, -5.8);
        Plane p1 = Plane.fromPointAndNormal(reference, Vector3D.of(1, 3, 3), TEST_PRECISION);
        Plane p2 = Plane.fromPointAndNormal(reference, Vector3D.of(-2, 4, 0), TEST_PRECISION);
        Plane p3 = Plane.fromPointAndNormal(reference, Vector3D.of(7, 0, -4), TEST_PRECISION);
        Vector3D plane = Plane.intersection(p1, p2, p3);
        Assert.assertEquals(reference.getX(), plane.getX(), TEST_EPS);
        Assert.assertEquals(reference.getY(), plane.getY(), TEST_EPS);
        Assert.assertEquals(reference.getZ(), plane.getZ(), TEST_EPS);
    }

    @Test
    public void testSimilar() {
        Vector3D p1  = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2  = Vector3D.of(3.4, -5.8, 1.2);
        Vector3D p3  = Vector3D.of(-2.0, 4.3, 0.7);
        Plane    planeA  = Plane.fromPoints(p1, p2, p3, TEST_PRECISION);
        Plane    planeB  = Plane.fromPoints(p1, Vector3D.of(11.4, -3.8, 5.1), p2, TEST_PRECISION);
        Assert.assertTrue(! planeA.contains(planeB));
        Assert.assertTrue(planeA.contains(planeA));
        Assert.assertTrue(planeA.contains(Plane.fromPoints(p1, p3, p2, TEST_PRECISION)));
        Vector3D shift = Vector3D.linearCombination(0.3, planeA.getNormal());
        Assert.assertTrue(! planeA.contains(Plane.fromPoints(p1.add(shift),
                                                     p3.add(shift),
                                                     p2.add(shift),
                                                     TEST_PRECISION)));
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

        for (int i=0; i<size; ++i) {
            result.add(list.get((i + shift) % size));
        }

        return result;
    }
}
