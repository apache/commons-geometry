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
package org.apache.commons.geometry.spherical.twod;

import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.partitioning.HyperplaneLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class GreatCircleTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Vector3D.Unit X = Vector3D.Unit.PLUS_X;
    private static final Vector3D.Unit Y = Vector3D.Unit.PLUS_Y;
    private static final Vector3D.Unit Z = Vector3D.Unit.PLUS_Z;

    @Test
    public void testFromPole() {
        // act/assert
        checkGreatCircle(GreatCircles.fromPole(X, TEST_PRECISION), X, Z);
        checkGreatCircle(GreatCircles.fromPole(Y, TEST_PRECISION), Y, Z.negate());
        checkGreatCircle(GreatCircles.fromPole(Z, TEST_PRECISION), Z, Y);
    }

    @Test
    public void testFromPoleAndXAxis() {
        // act/assert
        checkGreatCircle(GreatCircles.fromPoleAndU(X, Y, TEST_PRECISION), X, Y);
        checkGreatCircle(GreatCircles.fromPoleAndU(X, Z, TEST_PRECISION), X, Z);
        checkGreatCircle(GreatCircles.fromPoleAndU(Y, Z, TEST_PRECISION), Y, Z);
    }

    @Test
    public void testFromPoints() {
        // act/assert
        checkGreatCircle(GreatCircles.fromPoints(
                    Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO),
                    Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO),
                    TEST_PRECISION),
                Z, X);

        checkGreatCircle(GreatCircles.fromPoints(
                Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO),
                Point2S.of(-0.1 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO),
                TEST_PRECISION),
            Z.negate(), X);

        checkGreatCircle(GreatCircles.fromPoints(
                Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO),
                Point2S.of(1.5 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO),
                TEST_PRECISION),
            Z.negate(), X);

        checkGreatCircle(GreatCircles.fromPoints(
                Point2S.of(0, 0),
                Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO),
                TEST_PRECISION),
            Y, Z);
    }

    @Test
    public void testFromPoints_invalidPoints() {
        // arrange
        Point2S p1 = Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO);
        Point2S p2 = Point2S.of(PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            GreatCircles.fromPoints(p1, p1, TEST_PRECISION);
        }, IllegalArgumentException.class, Pattern.compile("^.*points are equal$"));

        GeometryTestUtils.assertThrows(() -> {
            GreatCircles.fromPoints(p1, Point2S.of(1e-12, PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION);
        }, IllegalArgumentException.class, Pattern.compile("^.*points are equal$"));

        GeometryTestUtils.assertThrows(() -> {
            GreatCircles.fromPoints(
                    Point2S.from(Vector3D.Unit.PLUS_X),
                    Point2S.from(Vector3D.Unit.MINUS_X),
                    TEST_PRECISION);
        }, IllegalArgumentException.class, Pattern.compile("^.*points are antipodal$"));

        GeometryTestUtils.assertThrows(() -> {
            GreatCircles.fromPoints(p1, Point2S.NaN, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            GreatCircles.fromPoints(Point2S.NaN, p2, TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            GreatCircles.fromPoints(p1, Point2S.of(Double.POSITIVE_INFINITY, PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            GreatCircles.fromPoints(Point2S.of(Double.POSITIVE_INFINITY, PlaneAngleRadians.PI_OVER_TWO), p2, TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testOffset_point() {
        // --- arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // --- act/assert

        // on circle
        for (double polar = -PlaneAngleRadians.PI_OVER_TWO; polar <= PlaneAngleRadians.PI_OVER_TWO; polar += 0.1) {
            Assert.assertEquals(0, circle.offset(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, polar)), TEST_EPS);
            Assert.assertEquals(0, circle.offset(Point2S.of(-PlaneAngleRadians.PI_OVER_TWO, polar)), TEST_EPS);
        }

        // +1/-1
        Assert.assertEquals(-1, circle.offset(Point2S.of(PlaneAngleRadians.PI_OVER_TWO + 1, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);
        Assert.assertEquals(1, circle.offset(Point2S.of(-PlaneAngleRadians.PI_OVER_TWO + 1, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);

        // poles
        Assert.assertEquals(-PlaneAngleRadians.PI_OVER_TWO, circle.offset(Point2S.of(PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, circle.offset(Point2S.of(0.0, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);
    }

    @Test
    public void testOffset_vector() {
        // --- arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // --- act/assert

        // on circle
        Assert.assertEquals(0, circle.offset(Vector3D.of(0, 1, 0)), TEST_EPS);
        Assert.assertEquals(0, circle.offset(Vector3D.of(0, 0, 1)), TEST_EPS);
        Assert.assertEquals(0, circle.offset(Vector3D.of(0, -1, 0)), TEST_EPS);
        Assert.assertEquals(0, circle.offset(Vector3D.of(0, 0, -1)), TEST_EPS);

        // +1/-1
        Assert.assertEquals(-0.25 * PlaneAngleRadians.PI, circle.offset(Vector3D.of(-1, 1, 0)), TEST_EPS);
        Assert.assertEquals(-0.25 * PlaneAngleRadians.PI, circle.offset(Vector3D.of(-1, 0, 1)), TEST_EPS);
        Assert.assertEquals(-0.25 * PlaneAngleRadians.PI, circle.offset(Vector3D.of(-1, -1, 0)), TEST_EPS);
        Assert.assertEquals(-0.25 * PlaneAngleRadians.PI, circle.offset(Vector3D.of(-1, 0, -1)), TEST_EPS);

        Assert.assertEquals(0.25 * PlaneAngleRadians.PI, circle.offset(Vector3D.of(1, 1, 0)), TEST_EPS);
        Assert.assertEquals(0.25 * PlaneAngleRadians.PI, circle.offset(Vector3D.of(1, 0, 1)), TEST_EPS);
        Assert.assertEquals(0.25 * PlaneAngleRadians.PI, circle.offset(Vector3D.of(1, -1, 0)), TEST_EPS);
        Assert.assertEquals(0.25 * PlaneAngleRadians.PI, circle.offset(Vector3D.of(1, 0, -1)), TEST_EPS);

        // poles
        Assert.assertEquals(-PlaneAngleRadians.PI_OVER_TWO, circle.offset(Vector3D.Unit.MINUS_X), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, circle.offset(Vector3D.Unit.PLUS_X), TEST_EPS);
    }

    @Test
    public void testAzimuth_point() {
        // --- arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // --- act/assert

        // on circle
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, circle.azimuth(Point2S.from(Vector3D.of(0, 1, 0))), TEST_EPS);
        Assert.assertEquals(0.0, circle.azimuth(Point2S.from(Vector3D.of(0, 0, 1))), TEST_EPS);
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, circle.azimuth(Point2S.from(Vector3D.of(0, -1, 0))), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, circle.azimuth(Point2S.from(Vector3D.of(0, 0, -1))), TEST_EPS);

        // +1/-1
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, circle.azimuth(Point2S.from(Vector3D.of(-1, 1, 0))), TEST_EPS);
        Assert.assertEquals(0.0, circle.azimuth(Point2S.from(Vector3D.of(-1, 0, 1))), TEST_EPS);
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, circle.azimuth(Point2S.from(Vector3D.of(-1, -1, 0))), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, circle.azimuth(Point2S.from(Vector3D.of(-1, 0, -1))), TEST_EPS);

        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, circle.azimuth(Point2S.from(Vector3D.of(1, 1, 0))), TEST_EPS);
        Assert.assertEquals(0.0, circle.azimuth(Point2S.from(Vector3D.of(1, 0, 1))), TEST_EPS);
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, circle.azimuth(Point2S.from(Vector3D.of(1, -1, 0))), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, circle.azimuth(Point2S.from(Vector3D.of(1, 0, -1))), TEST_EPS);

        // poles
        Assert.assertEquals(0, circle.azimuth(Point2S.from(Vector3D.Unit.MINUS_X)), TEST_EPS);
        Assert.assertEquals(0, circle.azimuth(Point2S.from(Vector3D.Unit.PLUS_X)), TEST_EPS);
    }

    @Test
    public void testAzimuth_vector() {
        // --- arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // --- act/assert

        // on circle
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, circle.azimuth(Vector3D.of(0, 1, 0)), TEST_EPS);
        Assert.assertEquals(0.0, circle.azimuth(Vector3D.of(0, 0, 1)), TEST_EPS);
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, circle.azimuth(Vector3D.of(0, -1, 0)), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, circle.azimuth(Vector3D.of(0, 0, -1)), TEST_EPS);

        // +1/-1
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, circle.azimuth(Vector3D.of(-1, 1, 0)), TEST_EPS);
        Assert.assertEquals(0.0, circle.azimuth(Vector3D.of(-1, 0, 1)), TEST_EPS);
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, circle.azimuth(Vector3D.of(-1, -1, 0)), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, circle.azimuth(Vector3D.of(-1, 0, -1)), TEST_EPS);

        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, circle.azimuth(Vector3D.of(1, 1, 0)), TEST_EPS);
        Assert.assertEquals(0.0, circle.azimuth(Vector3D.of(1, 0, 1)), TEST_EPS);
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, circle.azimuth(Vector3D.of(1, -1, 0)), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, circle.azimuth(Vector3D.of(1, 0, -1)), TEST_EPS);

        // poles
        Assert.assertEquals(0, circle.azimuth(Vector3D.Unit.MINUS_X), TEST_EPS);
        Assert.assertEquals(0, circle.azimuth(Vector3D.Unit.PLUS_X), TEST_EPS);
    }

    @Test
    public void testVectorAt() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.PLUS_Z, circle.vectorAt(0.0), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.PLUS_Y, circle.vectorAt(PlaneAngleRadians.PI_OVER_TWO), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.MINUS_Z, circle.vectorAt(PlaneAngleRadians.PI), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.MINUS_Y, circle.vectorAt(-PlaneAngleRadians.PI_OVER_TWO), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.PLUS_Z, circle.vectorAt(PlaneAngleRadians.TWO_PI), TEST_EPS);
    }

    @Test
    public void testProject() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        SphericalTestUtils.assertPointsEqual(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO),
                circle.project(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO),
                circle.project(Point2S.of(PlaneAngleRadians.PI_OVER_TWO + 1, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO),
                circle.project(Point2S.of(PlaneAngleRadians.PI_OVER_TWO - 1, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(Point2S.of(-PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO),
                circle.project(Point2S.of(-PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(-PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO),
                circle.project(Point2S.of(-PlaneAngleRadians.PI_OVER_TWO + 1, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(-PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO),
                circle.project(Point2S.of(-PlaneAngleRadians.PI_OVER_TWO - 1, PlaneAngleRadians.PI_OVER_TWO)), TEST_EPS);
    }

    @Test
    public void testProject_poles() {
        // arrange
        GreatCircle minusXCircle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        GreatCircle plusZCircle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_Y, TEST_PRECISION);

        // act
        SphericalTestUtils.assertPointsEqual(Point2S.of(0.0, 0.0),
                minusXCircle.project(Point2S.from(Vector3D.Unit.MINUS_X)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(0.0, 0.0),
                minusXCircle.project(Point2S.from(Vector3D.Unit.PLUS_X)), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(Point2S.of(1.5 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO),
                plusZCircle.project(Point2S.from(Vector3D.Unit.PLUS_Z)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(1.5 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO),
                plusZCircle.project(Point2S.from(Vector3D.Unit.MINUS_Z)), TEST_EPS);
    }

    @Test
    public void testReverse() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        GreatCircle reverse = circle.reverse();

        // assert
        checkGreatCircle(reverse, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_X);
    }

    @Test
    public void testTransform_rotateAroundPole() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoints(
                Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO),
                Point2S.of(1, PlaneAngleRadians.PI_OVER_TWO),
                TEST_PRECISION);

        Transform2S t = Transform2S.createRotation(circle.getPolePoint(), 0.25 * PlaneAngleRadians.PI);

        // act
        GreatCircle result = circle.transform(t);

        // assert
        Assert.assertNotSame(circle, result);
        checkGreatCircle(result, Vector3D.Unit.PLUS_Z, Vector3D.Unit.from(1, 1, 0));
    }

    @Test
    public void testTransform_rotateAroundNonPole() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoints(
                Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO),
                Point2S.of(1, PlaneAngleRadians.PI_OVER_TWO),
                TEST_PRECISION);

        Transform2S t = Transform2S.createRotation(Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO), PlaneAngleRadians.PI_OVER_TWO);

        // act
        GreatCircle result = circle.transform(t);

        // assert
        Assert.assertNotSame(circle, result);
        checkGreatCircle(result, Vector3D.Unit.MINUS_Y, Vector3D.Unit.PLUS_X);
    }

    @Test
    public void testTransform_piMinusAzimuth() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoints(
                Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO),
                Point2S.of(1, PlaneAngleRadians.PI_OVER_TWO),
                TEST_PRECISION);

        Transform2S t = Transform2S.createReflection(Point2S.PLUS_J)
                .rotate(Point2S.PLUS_K, PlaneAngleRadians.PI);

        // act
        GreatCircle result = circle.transform(t);

        // assert
        Assert.assertNotSame(circle, result);
        checkGreatCircle(result, Vector3D.Unit.MINUS_Z, Vector3D.Unit.MINUS_X);
    }

    @Test
    public void testSimilarOrientation() {
        // arrange
        GreatCircle a = GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        GreatCircle b = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION);
        GreatCircle c = GreatCircles.fromPole(Vector3D.Unit.MINUS_Z, TEST_PRECISION);
        GreatCircle d = GreatCircles.fromPole(Vector3D.Unit.from(1, 1, -1), TEST_PRECISION);
        GreatCircle e = GreatCircles.fromPole(Vector3D.Unit.from(1, 1, 1), TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.similarOrientation(a));

        Assert.assertFalse(a.similarOrientation(b));
        Assert.assertFalse(a.similarOrientation(c));
        Assert.assertFalse(a.similarOrientation(d));

        Assert.assertTrue(a.similarOrientation(e));
    }

    @Test
    public void testSpan() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        GreatArc span = circle.span();

        // assert
        Assert.assertSame(circle, span.getCircle());
        Assert.assertTrue(span.getInterval().isFull());

        Assert.assertNull(span.getStartPoint());
        Assert.assertNull(span.getEndPoint());
    }

    @Test
    public void testArc_points_2s() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        checkArc(circle.arc(Point2S.of(1, PlaneAngleRadians.PI_OVER_TWO), Point2S.of(0, 1)),
                Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO), Point2S.of(0, 0));

        Assert.assertTrue(circle.arc(Point2S.PLUS_I, Point2S.PLUS_I).isFull());
    }

    @Test
    public void testArc_points_1s() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        checkArc(circle.arc(Point1S.of(PlaneAngleRadians.PI), Point1S.of(1.5 * PlaneAngleRadians.PI)),
                Point2S.of(0, PlaneAngleRadians.PI), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO));

        Assert.assertTrue(circle.arc(Point1S.of(1), Point1S.of(1)).isFull());
    }

    @Test
    public void testArc_azimuths() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        checkArc(circle.arc(PlaneAngleRadians.PI, 1.5 * PlaneAngleRadians.PI),
                Point2S.of(0, PlaneAngleRadians.PI), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO));

        Assert.assertTrue(circle.arc(1, 1).isFull());
    }

    @Test
    public void testArc_interval() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        AngularInterval.Convex interval = AngularInterval.Convex.of(1, 2, TEST_PRECISION);

        // act
        GreatArc arc = circle.arc(interval);

        // assert
        Assert.assertSame(circle, arc.getCircle());
        Assert.assertSame(interval, arc.getInterval());
    }

    @Test
    public void testIntersection_parallel() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        GreatCircle a = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, precision);
        GreatCircle b = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, precision);
        GreatCircle c = GreatCircles.fromPole(Vector3D.Unit.of(1, 1e-4, 1e-4), precision);
        GreatCircle d = GreatCircles.fromPole(Vector3D.Unit.MINUS_X, precision);
        GreatCircle e = GreatCircles.fromPole(Vector3D.Unit.of(-1, 1e-4, 1e-4), precision);

        // act/assert
        Assert.assertNull(a.intersection(b));
        Assert.assertNull(a.intersection(c));
        Assert.assertNull(a.intersection(d));
        Assert.assertNull(a.intersection(e));
    }

    @Test
    public void testIntersection() {
        // arrange
        GreatCircle a = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION);
        GreatCircle b = GreatCircles.fromPole(Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        GreatCircle c = GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.PLUS_Z,
                a.intersection(b).getVector(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.MINUS_Z,
                b.intersection(a).getVector(), TEST_EPS);

        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.PLUS_X,
                b.intersection(c).getVector(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.MINUS_X,
                c.intersection(b).getVector(), TEST_EPS);
    }

    @Test
    public void testAngle_withoutReferencePoint() {
     // arrange
        GreatCircle a = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        GreatCircle b = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_I, TEST_PRECISION);
        GreatCircle c = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_K, TEST_PRECISION);
        GreatCircle d = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);
        GreatCircle e = GreatCircles.fromPoleAndU(
                Vector3D.Unit.of(1, 0, 1),
                Vector3D.Unit.PLUS_Y,
                TEST_PRECISION);

        GreatCircle f = GreatCircles.fromPoleAndU(
                Vector3D.Unit.of(1, 0, -1),
                Vector3D.Unit.PLUS_Y,
                TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0, a.angle(a), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, a.angle(b), TEST_EPS);

        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, a.angle(c), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, c.angle(a), TEST_EPS);

        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, a.angle(d), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, d.angle(a), TEST_EPS);

        Assert.assertEquals(0.25 * PlaneAngleRadians.PI, a.angle(e), TEST_EPS);
        Assert.assertEquals(0.25 * PlaneAngleRadians.PI, e.angle(a), TEST_EPS);

        Assert.assertEquals(0.75 * PlaneAngleRadians.PI, a.angle(f), TEST_EPS);
        Assert.assertEquals(0.75 * PlaneAngleRadians.PI, f.angle(a), TEST_EPS);
    }

    @Test
    public void testAngle_withReferencePoint() {
        // arrange
        GreatCircle a = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        GreatCircle b = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_I, TEST_PRECISION);
        GreatCircle c = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_K, TEST_PRECISION);
        GreatCircle d = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);
        GreatCircle e = GreatCircles.fromPoleAndU(
                Vector3D.Unit.of(1, 0, 1),
                Vector3D.Unit.PLUS_Y,
                TEST_PRECISION);

        GreatCircle f = GreatCircles.fromPoleAndU(
                Vector3D.Unit.of(1, 0, -1),
                Vector3D.Unit.PLUS_Y,
                TEST_PRECISION);

        // act/assert
        Assert.assertEquals(0, a.angle(a, Point2S.PLUS_J), TEST_EPS);
        Assert.assertEquals(0, a.angle(a, Point2S.MINUS_J), TEST_EPS);

        Assert.assertEquals(-PlaneAngleRadians.PI, a.angle(b, Point2S.PLUS_J), TEST_EPS);
        Assert.assertEquals(-PlaneAngleRadians.PI, a.angle(b, Point2S.MINUS_J), TEST_EPS);

        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, a.angle(c, Point2S.PLUS_I), TEST_EPS);
        Assert.assertEquals(-PlaneAngleRadians.PI_OVER_TWO, a.angle(c, Point2S.MINUS_I), TEST_EPS);

        Assert.assertEquals(-PlaneAngleRadians.PI_OVER_TWO, c.angle(a, Point2S.PLUS_I), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, c.angle(a, Point2S.MINUS_I), TEST_EPS);

        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, a.angle(d, Point2S.PLUS_J), TEST_EPS);
        Assert.assertEquals(-PlaneAngleRadians.PI_OVER_TWO, a.angle(d, Point2S.MINUS_J), TEST_EPS);

        Assert.assertEquals(-PlaneAngleRadians.PI_OVER_TWO, d.angle(a, Point2S.PLUS_J), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, d.angle(a, Point2S.MINUS_J), TEST_EPS);

        Assert.assertEquals(0.25 * PlaneAngleRadians.PI, a.angle(e, Point2S.PLUS_J), TEST_EPS);
        Assert.assertEquals(-0.25 * PlaneAngleRadians.PI, a.angle(e, Point2S.MINUS_J), TEST_EPS);

        Assert.assertEquals(-0.25 * PlaneAngleRadians.PI, e.angle(a, Point2S.PLUS_J), TEST_EPS);
        Assert.assertEquals(0.25 * PlaneAngleRadians.PI, e.angle(a, Point2S.MINUS_J), TEST_EPS);

        Assert.assertEquals(0.75 * PlaneAngleRadians.PI, a.angle(f, Point2S.PLUS_J), TEST_EPS);
        Assert.assertEquals(-0.75 * PlaneAngleRadians.PI, a.angle(f, Point2S.MINUS_J), TEST_EPS);

        Assert.assertEquals(-0.75 * PlaneAngleRadians.PI, f.angle(a, Point2S.PLUS_J), TEST_EPS);
        Assert.assertEquals(0.75 * PlaneAngleRadians.PI, f.angle(a, Point2S.MINUS_J), TEST_EPS);
    }

    @Test
    public void testAngle_withReferencePoint_pointEquidistanceFromIntersections() {
        // arrange
        GreatCircle a = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        GreatCircle b = GreatCircles.fromPoleAndU(
                Vector3D.Unit.of(1, 0, 1),
                Vector3D.Unit.PLUS_Y,
                TEST_PRECISION);

        // act/assert
        Assert.assertEquals(-0.25 * PlaneAngleRadians.PI, a.angle(b, Point2S.PLUS_I), TEST_EPS);
        Assert.assertEquals(-0.25 * PlaneAngleRadians.PI, a.angle(b, Point2S.MINUS_I), TEST_EPS);
    }

    @Test
    public void testToSubspace() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act/assert
        SphericalTestUtils.assertPointsEqual(Point1S.ZERO,
                circle.toSubspace(Point2S.from(Vector3D.Unit.MINUS_Z)), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(Point1S.of(0.25 * PlaneAngleRadians.PI),
                circle.toSubspace(Point2S.from(Vector3D.of(-1, -1, -1))), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(0.75 * PlaneAngleRadians.PI),
                circle.toSubspace(Point2S.from(Vector3D.of(-1, 1, 1))), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(1.25 * PlaneAngleRadians.PI),
                circle.toSubspace(Point2S.from(Vector3D.of(1, -1, 1))), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(1.75 * PlaneAngleRadians.PI),
                circle.toSubspace(Point2S.from(Vector3D.of(1, 1, -1))), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(Point1S.ZERO,
                circle.toSubspace(Point2S.from(Vector3D.Unit.PLUS_Y)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.ZERO,
                circle.toSubspace(Point2S.from(Vector3D.Unit.MINUS_Y)), TEST_EPS);
    }

    @Test
    public void testToSpace() {
        // arrange
        GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act/assert
        SphericalTestUtils.assertPointsEqual(Point2S.from(Vector3D.Unit.MINUS_Z),
                circle.toSpace(Point1S.ZERO), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(Point2S.from(Vector3D.of(-1, 0, -1)),
                circle.toSpace(Point1S.of(0.25 * PlaneAngleRadians.PI)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.from(Vector3D.of(-1, 0, 1)),
                circle.toSpace(Point1S.of(0.75 * PlaneAngleRadians.PI)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.from(Vector3D.of(1, 0, 1)),
                circle.toSpace(Point1S.of(1.25 * PlaneAngleRadians.PI)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.from(Vector3D.of(1, 0, -1)),
                circle.toSpace(Point1S.of(1.75 * PlaneAngleRadians.PI)), TEST_EPS);
    }

    @Test
    public void testEq() {
        // arrange
        double eps = 1e-3;
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(eps);

        GreatCircle a = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, precision);

        GreatCircle b = GreatCircles.fromPoleAndU(Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_X, precision);
        GreatCircle c = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_X, precision);
        GreatCircle d = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        GreatCircle e = GreatCircles.fromPoleAndU(Vector3D.of(1e-6, 0, 1), Vector3D.Unit.PLUS_X, precision);
        GreatCircle f = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.of(1, 1e-6, 0), precision);
        GreatCircle g = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X,
                new EpsilonDoublePrecisionContext(eps));

        // act/assert
        Assert.assertTrue(a.eq(a, precision));

        Assert.assertFalse(a.eq(b, precision));
        Assert.assertFalse(a.eq(c, precision));

        Assert.assertTrue(a.eq(d, precision));
        Assert.assertTrue(a.eq(e, precision));
        Assert.assertTrue(e.eq(a, precision));

        Assert.assertTrue(a.eq(f, precision));
        Assert.assertTrue(f.eq(a, precision));

        Assert.assertTrue(g.eq(e, precision));
        Assert.assertTrue(e.eq(g, precision));
    }

    @Test
    public void testHashCode() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        GreatCircle a = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        GreatCircle b = GreatCircles.fromPoleAndU(Vector3D.of(0, 1, 1), Vector3D.Unit.PLUS_X, TEST_PRECISION);
        GreatCircle c = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_X, TEST_PRECISION);
        GreatCircle d = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, precision);

        GreatCircle e = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        int hash = a.hashCode();

        // act/assert
        Assert.assertEquals(hash, a.hashCode());

        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertNotEquals(hash, c.hashCode());
        Assert.assertNotEquals(hash, d.hashCode());

        Assert.assertEquals(hash, e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-3);

        GreatCircle a = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        GreatCircle b = GreatCircles.fromPoleAndU(Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        GreatCircle c = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_X, TEST_PRECISION);
        GreatCircle d = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, precision);

        GreatCircle e = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

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
        GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        String str = circle.toString();

        // assert
        GeometryTestUtils.assertContains("GreatCircle[", str);
        GeometryTestUtils.assertContains("pole= (0.0, 0.0, 1.0)", str);
        GeometryTestUtils.assertContains("u= (1.0, 0.0, 0.0)", str);
        GeometryTestUtils.assertContains("v= (0.0, 1.0, 0.0)", str);
    }

    private static void checkGreatCircle(GreatCircle circle, Vector3D pole, Vector3D u) {
        SphericalTestUtils.assertVectorsEqual(pole, circle.getPole(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(pole, circle.getW(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(u, circle.getU(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(pole.cross(u), circle.getV(), TEST_EPS);

        Point2S plusPolePt = Point2S.from(circle.getPole());
        Point2S minusPolePt = Point2S.from(circle.getPole().negate());
        Point2S origin = Point2S.from(circle.getU());

        SphericalTestUtils.assertPointsEqual(plusPolePt, circle.getPolePoint(), TEST_EPS);

        Assert.assertFalse(circle.contains(plusPolePt));
        Assert.assertFalse(circle.contains(minusPolePt));
        Assert.assertTrue(circle.contains(origin));

        Assert.assertEquals(HyperplaneLocation.MINUS, circle.classify(plusPolePt));
        Assert.assertEquals(HyperplaneLocation.PLUS, circle.classify(minusPolePt));
        Assert.assertEquals(HyperplaneLocation.ON, circle.classify(origin));
    }

    private static void checkArc(GreatArc arc, Point2S start, Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);
    }
}
