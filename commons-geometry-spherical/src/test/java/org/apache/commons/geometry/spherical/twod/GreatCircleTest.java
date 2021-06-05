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
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GreatCircleTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final Vector3D.Unit X = Vector3D.Unit.PLUS_X;
    private static final Vector3D.Unit Y = Vector3D.Unit.PLUS_Y;
    private static final Vector3D.Unit Z = Vector3D.Unit.PLUS_Z;

    @Test
    void testFromPole() {
        // act/assert
        checkGreatCircle(GreatCircles.fromPole(X, TEST_PRECISION), X, Z);
        checkGreatCircle(GreatCircles.fromPole(Y, TEST_PRECISION), Y, Z.negate());
        checkGreatCircle(GreatCircles.fromPole(Z, TEST_PRECISION), Z, Y);
    }

    @Test
    void testFromPoleAndXAxis() {
        // act/assert
        checkGreatCircle(GreatCircles.fromPoleAndU(X, Y, TEST_PRECISION), X, Y);
        checkGreatCircle(GreatCircles.fromPoleAndU(X, Z, TEST_PRECISION), X, Z);
        checkGreatCircle(GreatCircles.fromPoleAndU(Y, Z, TEST_PRECISION), Y, Z);
    }

    @Test
    void testFromPoints() {
        // act/assert
        checkGreatCircle(GreatCircles.fromPoints(
                    Point2S.of(0, Angle.PI_OVER_TWO),
                    Point2S.of(Angle.PI_OVER_TWO, Angle.PI_OVER_TWO),
                    TEST_PRECISION),
                Z, X);

        checkGreatCircle(GreatCircles.fromPoints(
                Point2S.of(0, Angle.PI_OVER_TWO),
                Point2S.of(-0.1 * Math.PI, Angle.PI_OVER_TWO),
                TEST_PRECISION),
            Z.negate(), X);

        checkGreatCircle(GreatCircles.fromPoints(
                Point2S.of(0, Angle.PI_OVER_TWO),
                Point2S.of(1.5 * Math.PI, Angle.PI_OVER_TWO),
                TEST_PRECISION),
            Z.negate(), X);

        checkGreatCircle(GreatCircles.fromPoints(
                Point2S.of(0, 0),
                Point2S.of(0, Angle.PI_OVER_TWO),
                TEST_PRECISION),
            Y, Z);
    }

    @Test
    void testFromPoints_invalidPoints() {
        // arrange
        final Point2S p1 = Point2S.of(0, Angle.PI_OVER_TWO);
        final Point2S p2 = Point2S.of(Math.PI, Angle.PI_OVER_TWO);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            GreatCircles.fromPoints(p1, p1, TEST_PRECISION);
        }, IllegalArgumentException.class, Pattern.compile("^.*points are equal$"));

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            GreatCircles.fromPoints(p1, Point2S.of(1e-12, Angle.PI_OVER_TWO), TEST_PRECISION);
        }, IllegalArgumentException.class, Pattern.compile("^.*points are equal$"));

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            GreatCircles.fromPoints(
                    Point2S.from(Vector3D.Unit.PLUS_X),
                    Point2S.from(Vector3D.Unit.MINUS_X),
                    TEST_PRECISION);
        }, IllegalArgumentException.class, Pattern.compile("^.*points are antipodal$"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> GreatCircles.fromPoints(p1, Point2S.NaN, TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> GreatCircles.fromPoints(Point2S.NaN, p2, TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> GreatCircles.fromPoints(p1, Point2S.of(Double.POSITIVE_INFINITY, Angle.PI_OVER_TWO), TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> GreatCircles.fromPoints(Point2S.of(Double.POSITIVE_INFINITY, Angle.PI_OVER_TWO), p2, TEST_PRECISION));
    }

    @Test
    void testOffset_point() {
        // --- arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // --- act/assert

        // on circle
        for (double polar = -Angle.PI_OVER_TWO; polar <= Angle.PI_OVER_TWO; polar += 0.1) {
            Assertions.assertEquals(0, circle.offset(Point2S.of(Angle.PI_OVER_TWO, polar)), TEST_EPS);
            Assertions.assertEquals(0, circle.offset(Point2S.of(-Angle.PI_OVER_TWO, polar)), TEST_EPS);
        }

        // +1/-1
        Assertions.assertEquals(-1, circle.offset(Point2S.of(Angle.PI_OVER_TWO + 1, Angle.PI_OVER_TWO)), TEST_EPS);
        Assertions.assertEquals(1, circle.offset(Point2S.of(-Angle.PI_OVER_TWO + 1, Angle.PI_OVER_TWO)), TEST_EPS);

        // poles
        Assertions.assertEquals(-Angle.PI_OVER_TWO, circle.offset(Point2S.of(Math.PI, Angle.PI_OVER_TWO)), TEST_EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, circle.offset(Point2S.of(0.0, Angle.PI_OVER_TWO)), TEST_EPS);
    }

    @Test
    void testOffset_vector() {
        // --- arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // --- act/assert

        // on circle
        Assertions.assertEquals(0, circle.offset(Vector3D.of(0, 1, 0)), TEST_EPS);
        Assertions.assertEquals(0, circle.offset(Vector3D.of(0, 0, 1)), TEST_EPS);
        Assertions.assertEquals(0, circle.offset(Vector3D.of(0, -1, 0)), TEST_EPS);
        Assertions.assertEquals(0, circle.offset(Vector3D.of(0, 0, -1)), TEST_EPS);

        // +1/-1
        Assertions.assertEquals(-0.25 * Math.PI, circle.offset(Vector3D.of(-1, 1, 0)), TEST_EPS);
        Assertions.assertEquals(-0.25 * Math.PI, circle.offset(Vector3D.of(-1, 0, 1)), TEST_EPS);
        Assertions.assertEquals(-0.25 * Math.PI, circle.offset(Vector3D.of(-1, -1, 0)), TEST_EPS);
        Assertions.assertEquals(-0.25 * Math.PI, circle.offset(Vector3D.of(-1, 0, -1)), TEST_EPS);

        Assertions.assertEquals(0.25 * Math.PI, circle.offset(Vector3D.of(1, 1, 0)), TEST_EPS);
        Assertions.assertEquals(0.25 * Math.PI, circle.offset(Vector3D.of(1, 0, 1)), TEST_EPS);
        Assertions.assertEquals(0.25 * Math.PI, circle.offset(Vector3D.of(1, -1, 0)), TEST_EPS);
        Assertions.assertEquals(0.25 * Math.PI, circle.offset(Vector3D.of(1, 0, -1)), TEST_EPS);

        // poles
        Assertions.assertEquals(-Angle.PI_OVER_TWO, circle.offset(Vector3D.Unit.MINUS_X), TEST_EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, circle.offset(Vector3D.Unit.PLUS_X), TEST_EPS);
    }

    @Test
    void testAzimuth_point() {
        // --- arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // --- act/assert

        // on circle
        Assertions.assertEquals(Angle.PI_OVER_TWO, circle.azimuth(Point2S.from(Vector3D.of(0, 1, 0))), TEST_EPS);
        Assertions.assertEquals(0.0, circle.azimuth(Point2S.from(Vector3D.of(0, 0, 1))), TEST_EPS);
        Assertions.assertEquals(1.5 * Math.PI, circle.azimuth(Point2S.from(Vector3D.of(0, -1, 0))), TEST_EPS);
        Assertions.assertEquals(Math.PI, circle.azimuth(Point2S.from(Vector3D.of(0, 0, -1))), TEST_EPS);

        // +1/-1
        Assertions.assertEquals(Angle.PI_OVER_TWO, circle.azimuth(Point2S.from(Vector3D.of(-1, 1, 0))), TEST_EPS);
        Assertions.assertEquals(0.0, circle.azimuth(Point2S.from(Vector3D.of(-1, 0, 1))), TEST_EPS);
        Assertions.assertEquals(1.5 * Math.PI, circle.azimuth(Point2S.from(Vector3D.of(-1, -1, 0))), TEST_EPS);
        Assertions.assertEquals(Math.PI, circle.azimuth(Point2S.from(Vector3D.of(-1, 0, -1))), TEST_EPS);

        Assertions.assertEquals(Angle.PI_OVER_TWO, circle.azimuth(Point2S.from(Vector3D.of(1, 1, 0))), TEST_EPS);
        Assertions.assertEquals(0.0, circle.azimuth(Point2S.from(Vector3D.of(1, 0, 1))), TEST_EPS);
        Assertions.assertEquals(1.5 * Math.PI, circle.azimuth(Point2S.from(Vector3D.of(1, -1, 0))), TEST_EPS);
        Assertions.assertEquals(Math.PI, circle.azimuth(Point2S.from(Vector3D.of(1, 0, -1))), TEST_EPS);

        // poles
        Assertions.assertEquals(0, circle.azimuth(Point2S.from(Vector3D.Unit.MINUS_X)), TEST_EPS);
        Assertions.assertEquals(0, circle.azimuth(Point2S.from(Vector3D.Unit.PLUS_X)), TEST_EPS);
    }

    @Test
    void testAzimuth_vector() {
        // --- arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // --- act/assert

        // on circle
        Assertions.assertEquals(Angle.PI_OVER_TWO, circle.azimuth(Vector3D.of(0, 1, 0)), TEST_EPS);
        Assertions.assertEquals(0.0, circle.azimuth(Vector3D.of(0, 0, 1)), TEST_EPS);
        Assertions.assertEquals(1.5 * Math.PI, circle.azimuth(Vector3D.of(0, -1, 0)), TEST_EPS);
        Assertions.assertEquals(Math.PI, circle.azimuth(Vector3D.of(0, 0, -1)), TEST_EPS);

        // +1/-1
        Assertions.assertEquals(Angle.PI_OVER_TWO, circle.azimuth(Vector3D.of(-1, 1, 0)), TEST_EPS);
        Assertions.assertEquals(0.0, circle.azimuth(Vector3D.of(-1, 0, 1)), TEST_EPS);
        Assertions.assertEquals(1.5 * Math.PI, circle.azimuth(Vector3D.of(-1, -1, 0)), TEST_EPS);
        Assertions.assertEquals(Math.PI, circle.azimuth(Vector3D.of(-1, 0, -1)), TEST_EPS);

        Assertions.assertEquals(Angle.PI_OVER_TWO, circle.azimuth(Vector3D.of(1, 1, 0)), TEST_EPS);
        Assertions.assertEquals(0.0, circle.azimuth(Vector3D.of(1, 0, 1)), TEST_EPS);
        Assertions.assertEquals(1.5 * Math.PI, circle.azimuth(Vector3D.of(1, -1, 0)), TEST_EPS);
        Assertions.assertEquals(Math.PI, circle.azimuth(Vector3D.of(1, 0, -1)), TEST_EPS);

        // poles
        Assertions.assertEquals(0, circle.azimuth(Vector3D.Unit.MINUS_X), TEST_EPS);
        Assertions.assertEquals(0, circle.azimuth(Vector3D.Unit.PLUS_X), TEST_EPS);
    }

    @Test
    void testVectorAt() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.PLUS_Z, circle.vectorAt(0.0), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.PLUS_Y, circle.vectorAt(Angle.PI_OVER_TWO), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.MINUS_Z, circle.vectorAt(Math.PI), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.MINUS_Y, circle.vectorAt(-Angle.PI_OVER_TWO), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.PLUS_Z, circle.vectorAt(Angle.TWO_PI), TEST_EPS);
    }

    @Test
    void testProject() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        SphericalTestUtils.assertPointsEqual(Point2S.of(Angle.PI_OVER_TWO, Angle.PI_OVER_TWO),
                circle.project(Point2S.of(Angle.PI_OVER_TWO, Angle.PI_OVER_TWO)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(Angle.PI_OVER_TWO, Angle.PI_OVER_TWO),
                circle.project(Point2S.of(Angle.PI_OVER_TWO + 1, Angle.PI_OVER_TWO)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(Angle.PI_OVER_TWO, Angle.PI_OVER_TWO),
                circle.project(Point2S.of(Angle.PI_OVER_TWO - 1, Angle.PI_OVER_TWO)), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(Point2S.of(-Angle.PI_OVER_TWO, Angle.PI_OVER_TWO),
                circle.project(Point2S.of(-Angle.PI_OVER_TWO, Angle.PI_OVER_TWO)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(-Angle.PI_OVER_TWO, Angle.PI_OVER_TWO),
                circle.project(Point2S.of(-Angle.PI_OVER_TWO + 1, Angle.PI_OVER_TWO)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(-Angle.PI_OVER_TWO, Angle.PI_OVER_TWO),
                circle.project(Point2S.of(-Angle.PI_OVER_TWO - 1, Angle.PI_OVER_TWO)), TEST_EPS);
    }

    @Test
    void testProject_poles() {
        // arrange
        final GreatCircle minusXCircle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.MINUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final GreatCircle plusZCircle = GreatCircles.fromPoleAndU(
                Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_Y, TEST_PRECISION);

        // act
        SphericalTestUtils.assertPointsEqual(Point2S.of(0.0, 0.0),
                minusXCircle.project(Point2S.from(Vector3D.Unit.MINUS_X)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(0.0, 0.0),
                minusXCircle.project(Point2S.from(Vector3D.Unit.PLUS_X)), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(Point2S.of(1.5 * Math.PI, Angle.PI_OVER_TWO),
                plusZCircle.project(Point2S.from(Vector3D.Unit.PLUS_Z)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(1.5 * Math.PI, Angle.PI_OVER_TWO),
                plusZCircle.project(Point2S.from(Vector3D.Unit.MINUS_Z)), TEST_EPS);
    }

    @Test
    void testReverse() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final GreatCircle reverse = circle.reverse();

        // assert
        checkGreatCircle(reverse, Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_X);
    }

    @Test
    void testTransform_rotateAroundPole() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(
                Point2S.of(0, Angle.PI_OVER_TWO),
                Point2S.of(1, Angle.PI_OVER_TWO),
                TEST_PRECISION);

        final Transform2S t = Transform2S.createRotation(circle.getPolePoint(), 0.25 * Math.PI);

        // act
        final GreatCircle result = circle.transform(t);

        // assert
        Assertions.assertNotSame(circle, result);
        checkGreatCircle(result, Vector3D.Unit.PLUS_Z, Vector3D.Unit.from(1, 1, 0));
    }

    @Test
    void testTransform_rotateAroundNonPole() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(
                Point2S.of(0, Angle.PI_OVER_TWO),
                Point2S.of(1, Angle.PI_OVER_TWO),
                TEST_PRECISION);

        final Transform2S t = Transform2S.createRotation(Point2S.of(0, Angle.PI_OVER_TWO), Angle.PI_OVER_TWO);

        // act
        final GreatCircle result = circle.transform(t);

        // assert
        Assertions.assertNotSame(circle, result);
        checkGreatCircle(result, Vector3D.Unit.MINUS_Y, Vector3D.Unit.PLUS_X);
    }

    @Test
    void testTransform_piMinusAzimuth() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(
                Point2S.of(0, Angle.PI_OVER_TWO),
                Point2S.of(1, Angle.PI_OVER_TWO),
                TEST_PRECISION);

        final Transform2S t = Transform2S.createReflection(Point2S.PLUS_J)
                .rotate(Point2S.PLUS_K, Math.PI);

        // act
        final GreatCircle result = circle.transform(t);

        // assert
        Assertions.assertNotSame(circle, result);
        checkGreatCircle(result, Vector3D.Unit.MINUS_Z, Vector3D.Unit.MINUS_X);
    }

    @Test
    void testSimilarOrientation() {
        // arrange
        final GreatCircle a = GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final GreatCircle b = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final GreatCircle c = GreatCircles.fromPole(Vector3D.Unit.MINUS_Z, TEST_PRECISION);
        final GreatCircle d = GreatCircles.fromPole(Vector3D.Unit.from(1, 1, -1), TEST_PRECISION);
        final GreatCircle e = GreatCircles.fromPole(Vector3D.Unit.from(1, 1, 1), TEST_PRECISION);

        // act/assert
        Assertions.assertTrue(a.similarOrientation(a));

        Assertions.assertFalse(a.similarOrientation(b));
        Assertions.assertFalse(a.similarOrientation(c));
        Assertions.assertFalse(a.similarOrientation(d));

        Assertions.assertTrue(a.similarOrientation(e));
    }

    @Test
    void testSpan() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final GreatArc span = circle.span();

        // assert
        Assertions.assertSame(circle, span.getCircle());
        Assertions.assertTrue(span.getInterval().isFull());

        Assertions.assertNull(span.getStartPoint());
        Assertions.assertNull(span.getEndPoint());
    }

    @Test
    void testArc_points_2s() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        checkArc(circle.arc(Point2S.of(1, Angle.PI_OVER_TWO), Point2S.of(0, 1)),
                Point2S.of(Angle.PI_OVER_TWO, Angle.PI_OVER_TWO), Point2S.of(0, 0));

        Assertions.assertTrue(circle.arc(Point2S.PLUS_I, Point2S.PLUS_I).isFull());
    }

    @Test
    void testArc_points_1s() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        checkArc(circle.arc(Point1S.of(Math.PI), Point1S.of(1.5 * Math.PI)),
                Point2S.of(0, Math.PI), Point2S.of(Angle.PI_OVER_TWO, Angle.PI_OVER_TWO));

        Assertions.assertTrue(circle.arc(Point1S.of(1), Point1S.of(1)).isFull());
    }

    @Test
    void testArc_azimuths() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act/assert
        checkArc(circle.arc(Math.PI, 1.5 * Math.PI),
                Point2S.of(0, Math.PI), Point2S.of(Angle.PI_OVER_TWO, Angle.PI_OVER_TWO));

        Assertions.assertTrue(circle.arc(1, 1).isFull());
    }

    @Test
    void testArc_interval() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Z, TEST_PRECISION);
        final AngularInterval.Convex interval = AngularInterval.Convex.of(1, 2, TEST_PRECISION);

        // act
        final GreatArc arc = circle.arc(interval);

        // assert
        Assertions.assertSame(circle, arc.getCircle());
        Assertions.assertSame(interval, arc.getInterval());
    }

    @Test
    void testIntersection_parallel() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-3);

        final GreatCircle a = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, precision);
        final GreatCircle b = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, precision);
        final GreatCircle c = GreatCircles.fromPole(Vector3D.Unit.of(1, 1e-4, 1e-4), precision);
        final GreatCircle d = GreatCircles.fromPole(Vector3D.Unit.MINUS_X, precision);
        final GreatCircle e = GreatCircles.fromPole(Vector3D.Unit.of(-1, 1e-4, 1e-4), precision);

        // act/assert
        Assertions.assertNull(a.intersection(b));
        Assertions.assertNull(a.intersection(c));
        Assertions.assertNull(a.intersection(d));
        Assertions.assertNull(a.intersection(e));
    }

    @Test
    void testIntersection() {
        // arrange
        final GreatCircle a = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final GreatCircle b = GreatCircles.fromPole(Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        final GreatCircle c = GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION);

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
    void testAngle_withoutReferencePoint() {
     // arrange
        final GreatCircle a = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatCircle b = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_I, TEST_PRECISION);
        final GreatCircle c = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_K, TEST_PRECISION);
        final GreatCircle d = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);
        final GreatCircle e = GreatCircles.fromPoleAndU(
                Vector3D.Unit.of(1, 0, 1),
                Vector3D.Unit.PLUS_Y,
                TEST_PRECISION);

        final GreatCircle f = GreatCircles.fromPoleAndU(
                Vector3D.Unit.of(1, 0, -1),
                Vector3D.Unit.PLUS_Y,
                TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(0, a.angle(a), TEST_EPS);
        Assertions.assertEquals(Math.PI, a.angle(b), TEST_EPS);

        Assertions.assertEquals(Angle.PI_OVER_TWO, a.angle(c), TEST_EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, c.angle(a), TEST_EPS);

        Assertions.assertEquals(Angle.PI_OVER_TWO, a.angle(d), TEST_EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, d.angle(a), TEST_EPS);

        Assertions.assertEquals(0.25 * Math.PI, a.angle(e), TEST_EPS);
        Assertions.assertEquals(0.25 * Math.PI, e.angle(a), TEST_EPS);

        Assertions.assertEquals(0.75 * Math.PI, a.angle(f), TEST_EPS);
        Assertions.assertEquals(0.75 * Math.PI, f.angle(a), TEST_EPS);
    }

    @Test
    void testAngle_withReferencePoint() {
        // arrange
        final GreatCircle a = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatCircle b = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_I, TEST_PRECISION);
        final GreatCircle c = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_K, TEST_PRECISION);
        final GreatCircle d = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);
        final GreatCircle e = GreatCircles.fromPoleAndU(
                Vector3D.Unit.of(1, 0, 1),
                Vector3D.Unit.PLUS_Y,
                TEST_PRECISION);

        final GreatCircle f = GreatCircles.fromPoleAndU(
                Vector3D.Unit.of(1, 0, -1),
                Vector3D.Unit.PLUS_Y,
                TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(0, a.angle(a, Point2S.PLUS_J), TEST_EPS);
        Assertions.assertEquals(0, a.angle(a, Point2S.MINUS_J), TEST_EPS);

        Assertions.assertEquals(-Math.PI, a.angle(b, Point2S.PLUS_J), TEST_EPS);
        Assertions.assertEquals(-Math.PI, a.angle(b, Point2S.MINUS_J), TEST_EPS);

        Assertions.assertEquals(Angle.PI_OVER_TWO, a.angle(c, Point2S.PLUS_I), TEST_EPS);
        Assertions.assertEquals(-Angle.PI_OVER_TWO, a.angle(c, Point2S.MINUS_I), TEST_EPS);

        Assertions.assertEquals(-Angle.PI_OVER_TWO, c.angle(a, Point2S.PLUS_I), TEST_EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, c.angle(a, Point2S.MINUS_I), TEST_EPS);

        Assertions.assertEquals(Angle.PI_OVER_TWO, a.angle(d, Point2S.PLUS_J), TEST_EPS);
        Assertions.assertEquals(-Angle.PI_OVER_TWO, a.angle(d, Point2S.MINUS_J), TEST_EPS);

        Assertions.assertEquals(-Angle.PI_OVER_TWO, d.angle(a, Point2S.PLUS_J), TEST_EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, d.angle(a, Point2S.MINUS_J), TEST_EPS);

        Assertions.assertEquals(0.25 * Math.PI, a.angle(e, Point2S.PLUS_J), TEST_EPS);
        Assertions.assertEquals(-0.25 * Math.PI, a.angle(e, Point2S.MINUS_J), TEST_EPS);

        Assertions.assertEquals(-0.25 * Math.PI, e.angle(a, Point2S.PLUS_J), TEST_EPS);
        Assertions.assertEquals(0.25 * Math.PI, e.angle(a, Point2S.MINUS_J), TEST_EPS);

        Assertions.assertEquals(0.75 * Math.PI, a.angle(f, Point2S.PLUS_J), TEST_EPS);
        Assertions.assertEquals(-0.75 * Math.PI, a.angle(f, Point2S.MINUS_J), TEST_EPS);

        Assertions.assertEquals(-0.75 * Math.PI, f.angle(a, Point2S.PLUS_J), TEST_EPS);
        Assertions.assertEquals(0.75 * Math.PI, f.angle(a, Point2S.MINUS_J), TEST_EPS);
    }

    @Test
    void testAngle_withReferencePoint_pointEquidistanceFromIntersections() {
        // arrange
        final GreatCircle a = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatCircle b = GreatCircles.fromPoleAndU(
                Vector3D.Unit.of(1, 0, 1),
                Vector3D.Unit.PLUS_Y,
                TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(-0.25 * Math.PI, a.angle(b, Point2S.PLUS_I), TEST_EPS);
        Assertions.assertEquals(-0.25 * Math.PI, a.angle(b, Point2S.MINUS_I), TEST_EPS);
    }

    @Test
    void testToSubspace() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act/assert
        SphericalTestUtils.assertPointsEqual(Point1S.ZERO,
                circle.toSubspace(Point2S.from(Vector3D.Unit.MINUS_Z)), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(Point1S.of(0.25 * Math.PI),
                circle.toSubspace(Point2S.from(Vector3D.of(-1, -1, -1))), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(0.75 * Math.PI),
                circle.toSubspace(Point2S.from(Vector3D.of(-1, 1, 1))), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(1.25 * Math.PI),
                circle.toSubspace(Point2S.from(Vector3D.of(1, -1, 1))), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(1.75 * Math.PI),
                circle.toSubspace(Point2S.from(Vector3D.of(1, 1, -1))), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(Point1S.ZERO,
                circle.toSubspace(Point2S.from(Vector3D.Unit.PLUS_Y)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.ZERO,
                circle.toSubspace(Point2S.from(Vector3D.Unit.MINUS_Y)), TEST_EPS);
    }

    @Test
    void testToSpace() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_Z, TEST_PRECISION);

        // act/assert
        SphericalTestUtils.assertPointsEqual(Point2S.from(Vector3D.Unit.MINUS_Z),
                circle.toSpace(Point1S.ZERO), TEST_EPS);

        SphericalTestUtils.assertPointsEqual(Point2S.from(Vector3D.of(-1, 0, -1)),
                circle.toSpace(Point1S.of(0.25 * Math.PI)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.from(Vector3D.of(-1, 0, 1)),
                circle.toSpace(Point1S.of(0.75 * Math.PI)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.from(Vector3D.of(1, 0, 1)),
                circle.toSpace(Point1S.of(1.25 * Math.PI)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.from(Vector3D.of(1, 0, -1)),
                circle.toSpace(Point1S.of(1.75 * Math.PI)), TEST_EPS);
    }

    @Test
    void testEq() {
        // arrange
        final double eps = 1e-3;
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(eps);

        final GreatCircle a = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, precision);

        final GreatCircle b = GreatCircles.fromPoleAndU(Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_X, precision);
        final GreatCircle c = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_X, precision);
        final GreatCircle d = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        final GreatCircle e = GreatCircles.fromPoleAndU(Vector3D.of(1e-6, 0, 1), Vector3D.Unit.PLUS_X, precision);
        final GreatCircle f = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.of(1, 1e-6, 0), precision);
        final GreatCircle g = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X,
                Precision.doubleEquivalenceOfEpsilon(eps));

        // act/assert
        Assertions.assertTrue(a.eq(a, precision));

        Assertions.assertFalse(a.eq(b, precision));
        Assertions.assertFalse(a.eq(c, precision));

        Assertions.assertTrue(a.eq(d, precision));
        Assertions.assertTrue(a.eq(e, precision));
        Assertions.assertTrue(e.eq(a, precision));

        Assertions.assertTrue(a.eq(f, precision));
        Assertions.assertTrue(f.eq(a, precision));

        Assertions.assertTrue(g.eq(e, precision));
        Assertions.assertTrue(e.eq(g, precision));
    }

    @Test
    void testHashCode() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-3);

        final GreatCircle a = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        final GreatCircle b = GreatCircles.fromPoleAndU(Vector3D.of(0, 1, 1), Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final GreatCircle c = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_X, TEST_PRECISION);
        final GreatCircle d = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, precision);

        final GreatCircle e = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final int hash = a.hashCode();

        // act/assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());
        Assertions.assertNotEquals(hash, d.hashCode());

        Assertions.assertEquals(hash, e.hashCode());
    }

    @Test
    void testEquals() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-3);

        final GreatCircle a = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        final GreatCircle b = GreatCircles.fromPoleAndU(Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final GreatCircle c = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.MINUS_X, TEST_PRECISION);
        final GreatCircle d = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, precision);

        final GreatCircle e = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

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
        final GreatCircle circle = GreatCircles.fromPoleAndU(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act
        final String str = circle.toString();

        // assert
        GeometryTestUtils.assertContains("GreatCircle[", str);
        GeometryTestUtils.assertContains("pole= (0.0, 0.0, 1.0)", str);
        GeometryTestUtils.assertContains("u= (1.0, 0.0, 0.0)", str);
        GeometryTestUtils.assertContains("v= (0.0, 1.0, 0.0)", str);
    }

    private static void checkGreatCircle(final GreatCircle circle, final Vector3D pole, final Vector3D u) {
        SphericalTestUtils.assertVectorsEqual(pole, circle.getPole(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(pole, circle.getW(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(u, circle.getU(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(pole.cross(u), circle.getV(), TEST_EPS);

        final Point2S plusPolePt = Point2S.from(circle.getPole());
        final Point2S minusPolePt = Point2S.from(circle.getPole().negate());
        final Point2S origin = Point2S.from(circle.getU());

        SphericalTestUtils.assertPointsEqual(plusPolePt, circle.getPolePoint(), TEST_EPS);

        Assertions.assertFalse(circle.contains(plusPolePt));
        Assertions.assertFalse(circle.contains(minusPolePt));
        Assertions.assertTrue(circle.contains(origin));

        Assertions.assertEquals(HyperplaneLocation.MINUS, circle.classify(plusPolePt));
        Assertions.assertEquals(HyperplaneLocation.PLUS, circle.classify(minusPolePt));
        Assertions.assertEquals(HyperplaneLocation.ON, circle.classify(origin));
    }

    private static void checkArc(final GreatArc arc, final Point2S start, final Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);
    }
}
