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

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.exception.IllegalNormException;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;

public class PlaneTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromNormal() {
        // act/assert
        checkPlane(Plane.fromNormal(Vector3D.PLUS_X, TEST_PRECISION),
                Vector3D.ZERO, Vector3D.PLUS_Z, Vector3D.MINUS_Y);
        checkPlane(Plane.fromNormal(Vector3D.of(7, 0, 0), TEST_PRECISION),
                Vector3D.ZERO, Vector3D.PLUS_Z, Vector3D.MINUS_Y);

        checkPlane(Plane.fromNormal(Vector3D.PLUS_Y, TEST_PRECISION),
                Vector3D.ZERO, Vector3D.MINUS_Z, Vector3D.MINUS_X);
        checkPlane(Plane.fromNormal(Vector3D.of(0, 5, 0), TEST_PRECISION),
                Vector3D.ZERO, Vector3D.MINUS_Z, Vector3D.MINUS_X);

        checkPlane(Plane.fromNormal(Vector3D.PLUS_Z, TEST_PRECISION),
                Vector3D.ZERO, Vector3D.PLUS_Y, Vector3D.MINUS_X);
        checkPlane(Plane.fromNormal(Vector3D.of(0, 0, 0.01), TEST_PRECISION),
                Vector3D.ZERO, Vector3D.PLUS_Y, Vector3D.MINUS_X);
    }

    @Test
    public void testFromNormal_illegalArguments() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromNormal(Vector3D.ZERO, TEST_PRECISION);
        }, IllegalNormException.class);
    }

    @Test
    public void testFromPointAndNormal() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert
        checkPlane(Plane.fromPointAndNormal(pt, Vector3D.of(0.1, 0, 0), TEST_PRECISION),
                Vector3D.of(1, 0, 0), Vector3D.PLUS_Z, Vector3D.MINUS_Y);
        checkPlane(Plane.fromPointAndNormal(pt, Vector3D.of(0, 2, 0), TEST_PRECISION),
                Vector3D.of(0, 2, 0), Vector3D.MINUS_Z, Vector3D.MINUS_X);
        checkPlane(Plane.fromPointAndNormal(pt, Vector3D.of(0, 0, 5), TEST_PRECISION),
                Vector3D.of(0, 0, 3), Vector3D.PLUS_Y, Vector3D.MINUS_X);
    }

    @Test
    public void testFromPointAndNormal_illegalArguments() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPointAndNormal(pt, Vector3D.ZERO, TEST_PRECISION);
        }, IllegalNormException.class);
    }

    @Test
    public void testFromPointAndPlaneVectors() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert
        checkPlane(Plane.fromPointAndPlaneVectors(pt, Vector3D.of(2, 0, 0), Vector3D.of(1, 0.1, 0),  TEST_PRECISION),
                Vector3D.of(0, 0, 3), Vector3D.PLUS_X, Vector3D.PLUS_Y);

        checkPlane(Plane.fromPointAndPlaneVectors(pt, Vector3D.of(2, 0, 0), Vector3D.of(1, -0.1, 0),  TEST_PRECISION),
                Vector3D.of(0, 0, 3), Vector3D.PLUS_X, Vector3D.MINUS_Y);

        checkPlane(Plane.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0.1, 0), Vector3D.of(0, -1, 1),  TEST_PRECISION),
                Vector3D.of(1, 0, 0), Vector3D.PLUS_Y, Vector3D.PLUS_Z);
    }

    @Test
    public void testFromPointAndPlaneVectors_illegalArguments() {
        // arrange
        Vector3D pt = Vector3D.of(1, 2, 3);

        // act/assert

        // identical vectors
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        }, IllegalNormException.class);

        // zero vector
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.ZERO, TEST_PRECISION);
        }, IllegalNormException.class);

        // collinear vectors
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 2), TEST_PRECISION);
        }, IllegalNormException.class);

        // collinear vectors - reversed
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPointAndPlaneVectors(pt, Vector3D.of(0, 0, 1), Vector3D.of(0, 0, -2), TEST_PRECISION);
        }, IllegalNormException.class);
    }

    @Test
    public void testFromPoints() {
        // arrange
        Vector3D a = Vector3D.of(1, 1, 1);
        Vector3D b = Vector3D.of(1, 1, 4.3);
        Vector3D c = Vector3D.of(2.5, 1, 1);

        // act/assert
        checkPlane(Plane.fromPoints(a, b, c, TEST_PRECISION),
                Vector3D.of(0, 1, 0), Vector3D.PLUS_Z, Vector3D.PLUS_X);

        checkPlane(Plane.fromPoints(a, c, b, TEST_PRECISION),
                Vector3D.of(0, 1, 0), Vector3D.PLUS_X, Vector3D.PLUS_Z);
    }

    @Test
    public void testFromPoints_illegalArguments() {
        // arrange
        Vector3D a = Vector3D.of(1, 0, 0);
        Vector3D b = Vector3D.of(0, 1, 0);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(a, a, a, TEST_PRECISION);
        }, IllegalNormException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(a, a, b, TEST_PRECISION);
        }, IllegalNormException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(a, b, a, TEST_PRECISION);
        }, IllegalNormException.class);

        GeometryTestUtils.assertThrows(() -> {
            Plane.fromPoints(b, a, a, TEST_PRECISION);
        }, IllegalNormException.class);
    }

    @Test
    public void testContains_point() {
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Assert.assertTrue(plane.contains(Vector3D.of(0, 0, 1)));
        Assert.assertTrue(plane.contains(Vector3D.of(17, -32, 1)));
        Assert.assertTrue(! plane.contains(Vector3D.of(17, -32, 1.001)));
    }

    @Test
    public void testContains_line() {
        // arrange
        Plane plane = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.PLUS_Z, TEST_PRECISION);

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
    public void testIsParallelAndOffset()
    {
        Plane plane = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 1), TEST_PRECISION);
        Line3D parallelLine = Line3D.fromPoints(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 2), TEST_PRECISION);
        Assert.assertTrue(plane.isParallel(parallelLine));
        Assert.assertEquals(1.0, plane.offset(parallelLine), TEST_EPS);
        Line3D nonParallelLine = Line3D.fromPoints(Vector3D.of(1, 0, 2), Vector3D.of(2, 0, 1), TEST_PRECISION);
        Assert.assertFalse(plane.isParallel(nonParallelLine));
        Assert.assertEquals(0.0, plane.offset(nonParallelLine), TEST_EPS);
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
        Line3D line = Line3D.fromPoints(Vector3D.of(1, 0, 1), Vector3D.of(2, 0, 2), TEST_PRECISION);
        Line3D expectedProjection = Line3D.fromPoints(Vector3D.of(1, 0, 1),Vector3D.of(2, 0, 1), TEST_PRECISION);
        Assert.assertEquals(expectedProjection, plane.project(line));
    }

    @Test
    public void testOffset() {
        Vector3D p1 = Vector3D.of(1, 1, 1);
        Plane plane = Plane.fromPointAndNormal(p1, Vector3D.of(0.2, 0, 0), TEST_PRECISION);
        Assert.assertEquals(-5.0, plane.offset(Vector3D.of(-4, 0, 0)), TEST_EPS);
        Assert.assertEquals(+5.0, plane.offset(Vector3D.of(6, 10, -12)), TEST_EPS);
        Assert.assertEquals(0.3,
                            plane.offset(Vector3D.linearCombination(1.0, p1, 0.3, plane.getNormal())),
                            TEST_EPS);
        Assert.assertEquals(-0.3,
                            plane.offset(Vector3D.linearCombination(1.0, p1, -0.3, plane.getNormal())),
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
        Line3D  line = Line3D.fromPoints(Vector3D.of(0.2, -3.5, 0.7), Vector3D.of(1.2, -2.5, -0.3), TEST_PRECISION);
        Vector3D point = plane.intersection(line);
        Assert.assertTrue(plane.contains(point));
        Assert.assertTrue(line.contains(point));
        Assert.assertNull(plane.intersection(Line3D.fromPoints(Vector3D.of(10, 10, 10),
                                                  Vector3D.of(10, 10, 10).add(plane.getNormal().orthogonal()),
                                                  TEST_PRECISION)));
    }

    @Test
    public void testIntersection2() {
        Vector3D p1  = Vector3D.of(1.2, 3.4, -5.8);
        Vector3D p2  = Vector3D.of(3.4, -5.8, 1.2);
        Plane    planeA  = Plane.fromPoints(p1, p2, Vector3D.of(-2.0, 4.3, 0.7), TEST_PRECISION);
        Plane    planeB  = Plane.fromPoints(p1, Vector3D.of(11.4, -3.8, 5.1), p2, TEST_PRECISION);
        Line3D     line   = planeA.intersection(planeB);
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

        EuclideanTestUtils.assertCoordinatesEqual(u, plane.getU(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(v, plane.getV(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(w, plane.getW(), TEST_EPS);

        Assert.assertEquals(Vector3D.ZERO.distance(plane.getOrigin()), Math.abs(plane.getOriginOffset()), TEST_EPS);
    }
}
