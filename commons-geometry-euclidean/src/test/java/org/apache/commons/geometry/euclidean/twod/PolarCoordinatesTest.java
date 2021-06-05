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
package org.apache.commons.geometry.euclidean.twod;

import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.numbers.angle.Angle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PolarCoordinatesTest {

    private static final double EPS = 1e-10;

    private static final double THREE_PI_OVER_TWO = 3 * Math.PI / 2;

    @Test
    void testOf() {
        // act/assert
        checkPolar(PolarCoordinates.of(0, 0), 0, 0);

        checkPolar(PolarCoordinates.of(2, 0), 2, 0);
        checkPolar(PolarCoordinates.of(2, Angle.PI_OVER_TWO), 2, Angle.PI_OVER_TWO);
        checkPolar(PolarCoordinates.of(2, Math.PI), 2, Math.PI);
        checkPolar(PolarCoordinates.of(2, -Angle.PI_OVER_TWO), 2, THREE_PI_OVER_TWO);
    }

    @Test
    void testOf_unnormalizedAngles() {
        // act/assert
        checkPolar(PolarCoordinates.of(2, Angle.TWO_PI), 2, 0);
        checkPolar(PolarCoordinates.of(2, Angle.PI_OVER_TWO + Angle.TWO_PI), 2, Angle.PI_OVER_TWO);
        checkPolar(PolarCoordinates.of(2, -Math.PI), 2, Math.PI);
        checkPolar(PolarCoordinates.of(2, -Math.PI * 1.5), 2, Angle.PI_OVER_TWO);
    }

    @Test
    void testOf_azimuthWrapAround() {
        // arrange
        final double delta = 1e-6;

        // act/assert
        checkAzimuthWrapAround(2, 0);
        checkAzimuthWrapAround(2, delta);
        checkAzimuthWrapAround(2, Math.PI - delta);
        checkAzimuthWrapAround(2, Math.PI);

        checkAzimuthWrapAround(2, THREE_PI_OVER_TWO);
        checkAzimuthWrapAround(2, Angle.TWO_PI - delta);
    }

    private void checkAzimuthWrapAround(final double radius, final double azimuth) {
        checkPolar(PolarCoordinates.of(radius, azimuth), radius, azimuth);

        checkPolar(PolarCoordinates.of(radius, azimuth - Angle.TWO_PI), radius, azimuth);
        checkPolar(PolarCoordinates.of(radius, azimuth - (2 * Angle.TWO_PI)), radius, azimuth);
        checkPolar(PolarCoordinates.of(radius, azimuth - (3 * Angle.TWO_PI)), radius, azimuth);

        checkPolar(PolarCoordinates.of(radius, azimuth + Angle.TWO_PI), radius, azimuth);
        checkPolar(PolarCoordinates.of(radius, azimuth + (2 * Angle.TWO_PI)), radius, azimuth);
        checkPolar(PolarCoordinates.of(radius, azimuth + (3 * Angle.TWO_PI)), radius, azimuth);
    }

    @Test
    void testOf_negativeRadius() {
        // act/assert
        checkPolar(PolarCoordinates.of(-1, 0), 1, Math.PI);
        checkPolar(PolarCoordinates.of(-1e-6, Angle.PI_OVER_TWO), 1e-6, THREE_PI_OVER_TWO);
        checkPolar(PolarCoordinates.of(-2, Math.PI), 2, 0);
        checkPolar(PolarCoordinates.of(-3, -Angle.PI_OVER_TWO), 3, Angle.PI_OVER_TWO);
    }

    @Test
    void testOf_NaNAndInfinite() {
        // act/assert
        checkPolar(PolarCoordinates.of(Double.NaN, 0), Double.NaN, 0);
        checkPolar(PolarCoordinates.of(Double.NEGATIVE_INFINITY, 0), Double.POSITIVE_INFINITY, Math.PI);
        checkPolar(PolarCoordinates.of(Double.POSITIVE_INFINITY, 0), Double.POSITIVE_INFINITY, 0);

        checkPolar(PolarCoordinates.of(0, Double.NaN), 0, Double.NaN);
        checkPolar(PolarCoordinates.of(0, Double.NEGATIVE_INFINITY), 0, Double.NEGATIVE_INFINITY);
        checkPolar(PolarCoordinates.of(0, Double.POSITIVE_INFINITY), 0, Double.POSITIVE_INFINITY);
    }

    @Test
    void testFromCartesian_coordinates() {
        // arrange
        final double sqrt2 = Math.sqrt(2);

        // act/assert
        checkPolar(PolarCoordinates.fromCartesian(0, 0), 0, 0);

        checkPolar(PolarCoordinates.fromCartesian(1, 0), 1, 0);
        checkPolar(PolarCoordinates.fromCartesian(1, 1), sqrt2, 0.25 * Math.PI);
        checkPolar(PolarCoordinates.fromCartesian(0, 1), 1, Angle.PI_OVER_TWO);

        checkPolar(PolarCoordinates.fromCartesian(-1, 1), sqrt2, 0.75 * Math.PI);
        checkPolar(PolarCoordinates.fromCartesian(-1, 0), 1, Math.PI);
        checkPolar(PolarCoordinates.fromCartesian(-1, -1), sqrt2, 1.25 * Math.PI);

        checkPolar(PolarCoordinates.fromCartesian(0, -1), 1, 1.5 * Math.PI);
        checkPolar(PolarCoordinates.fromCartesian(1, -1), sqrt2, 1.75 * Math.PI);
    }

    @Test
    void testFromCartesian_vector() {
        // arrange
        final double sqrt2 = Math.sqrt(2);

        // act/assert
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(0, 0)), 0, 0);

        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(1, 0)), 1, 0);
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(1, 1)), sqrt2, 0.25 * Math.PI);
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(0, 1)), 1, Angle.PI_OVER_TWO);

        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(-1, 1)), sqrt2, 0.75 * Math.PI);
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(-1, 0)), 1, Math.PI);
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(-1, -1)), sqrt2, 1.25 * Math.PI);

        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(0, -1)), 1, 1.5 * Math.PI);
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(1, -1)), sqrt2, 1.75 * Math.PI);
    }

    @Test
    void testDimension() {
        // arrange
        final PolarCoordinates p = PolarCoordinates.of(1, 0);

        // act/assert
        Assertions.assertEquals(2, p.getDimension());
    }

    @Test
    void testIsNaN() {
        // act/assert
        Assertions.assertFalse(PolarCoordinates.of(1, 0).isNaN());
        Assertions.assertFalse(PolarCoordinates.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isNaN());

        Assertions.assertTrue(PolarCoordinates.of(Double.NaN, 0).isNaN());
        Assertions.assertTrue(PolarCoordinates.of(1, Double.NaN).isNaN());
        Assertions.assertTrue(PolarCoordinates.of(Double.NaN, Double.NaN).isNaN());
    }

    @Test
    void testIsInfinite() {
        // act/assert
        Assertions.assertFalse(PolarCoordinates.of(1, 0).isInfinite());
        Assertions.assertFalse(PolarCoordinates.of(Double.NaN, Double.NaN).isInfinite());

        Assertions.assertTrue(PolarCoordinates.of(Double.POSITIVE_INFINITY, 0).isInfinite());
        Assertions.assertTrue(PolarCoordinates.of(Double.NEGATIVE_INFINITY, 0).isInfinite());
        Assertions.assertFalse(PolarCoordinates.of(Double.NEGATIVE_INFINITY, Double.NaN).isInfinite());

        Assertions.assertTrue(PolarCoordinates.of(0, Double.POSITIVE_INFINITY).isInfinite());
        Assertions.assertTrue(PolarCoordinates.of(0, Double.NEGATIVE_INFINITY).isInfinite());
        Assertions.assertFalse(PolarCoordinates.of(Double.NaN, Double.NEGATIVE_INFINITY).isInfinite());

        Assertions.assertTrue(PolarCoordinates.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isInfinite());
        Assertions.assertTrue(PolarCoordinates.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).isInfinite());
    }

    @Test
    void testIsFinite() {
        // act/assert
        Assertions.assertTrue(PolarCoordinates.of(1, 0).isFinite());
        Assertions.assertTrue(PolarCoordinates.of(1, Math.PI).isFinite());

        Assertions.assertFalse(PolarCoordinates.of(Double.NaN, Double.NaN).isFinite());

        Assertions.assertFalse(PolarCoordinates.of(Double.POSITIVE_INFINITY, 0).isFinite());
        Assertions.assertFalse(PolarCoordinates.of(Double.NEGATIVE_INFINITY, 0).isFinite());
        Assertions.assertFalse(PolarCoordinates.of(Double.NEGATIVE_INFINITY, Double.NaN).isFinite());

        Assertions.assertFalse(PolarCoordinates.of(0, Double.POSITIVE_INFINITY).isFinite());
        Assertions.assertFalse(PolarCoordinates.of(0, Double.NEGATIVE_INFINITY).isFinite());
        Assertions.assertFalse(PolarCoordinates.of(Double.NaN, Double.NEGATIVE_INFINITY).isFinite());

        Assertions.assertFalse(PolarCoordinates.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isFinite());
        Assertions.assertFalse(PolarCoordinates.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).isFinite());
    }

    @Test
    void testHashCode() {
        // arrange
        final PolarCoordinates a = PolarCoordinates.of(1, 2);
        final PolarCoordinates b = PolarCoordinates.of(10, 2);
        final PolarCoordinates c = PolarCoordinates.of(10, 20);
        final PolarCoordinates d = PolarCoordinates.of(1, 20);

        final PolarCoordinates e = PolarCoordinates.of(1, 2);

        // act/assert
        Assertions.assertEquals(a.hashCode(), a.hashCode());
        Assertions.assertEquals(a.hashCode(), e.hashCode());

        Assertions.assertNotEquals(a.hashCode(), b.hashCode());
        Assertions.assertNotEquals(a.hashCode(), c.hashCode());
        Assertions.assertNotEquals(a.hashCode(), d.hashCode());
    }

    @Test
    void testHashCode_NaNInstancesHaveSameHashCode() {
        // arrange
        final PolarCoordinates a = PolarCoordinates.of(1, Double.NaN);
        final PolarCoordinates b = PolarCoordinates.of(Double.NaN, 1);

        // act/assert
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testEquals() {
        // arrange
        final PolarCoordinates a = PolarCoordinates.of(1, 2);
        final PolarCoordinates b = PolarCoordinates.of(10, 2);
        final PolarCoordinates c = PolarCoordinates.of(10, 20);
        final PolarCoordinates d = PolarCoordinates.of(1, 20);

        final PolarCoordinates e = PolarCoordinates.of(1, 2);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);
        Assertions.assertEquals(a, e);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(a, c);
        Assertions.assertNotEquals(a, d);
    }

    @Test
    void testEquals_NaNInstancesEqual() {
        // arrange
        final PolarCoordinates a = PolarCoordinates.of(1, Double.NaN);
        final PolarCoordinates b = PolarCoordinates.of(Double.NaN, 1);

        // act/assert
        Assertions.assertEquals(a, b);
    }

    @Test
    void testEqualsAndHashCode_signedZeroConsistency() {
        // arrange
        final PolarCoordinates a = PolarCoordinates.of(0.0, -0.0);
        final PolarCoordinates b = PolarCoordinates.of(-0.0, 0.0);
        final PolarCoordinates c = PolarCoordinates.of(0.0, -0.0);
        final PolarCoordinates d = PolarCoordinates.of(-0.0, 0.0);

        // act/assert
        Assertions.assertFalse(a.equals(b));

        Assertions.assertTrue(a.equals(c));
        Assertions.assertEquals(a.hashCode(), c.hashCode());

        Assertions.assertTrue(b.equals(d));
        Assertions.assertEquals(b.hashCode(), d.hashCode());
    }

    @Test
    void testToCartesian() {
        // arrange
        final double sqrt2 = Math.sqrt(2);

        // act/assert
        checkVector(PolarCoordinates.of(0, 0).toCartesian(), 0, 0);

        checkVector(PolarCoordinates.of(1, 0).toCartesian(), 1, 0);
        checkVector(PolarCoordinates.of(sqrt2, 0.25 * Math.PI).toCartesian(), 1, 1);
        checkVector(PolarCoordinates.of(1, Angle.PI_OVER_TWO).toCartesian(), 0, 1);

        checkVector(PolarCoordinates.of(sqrt2, 0.75 * Math.PI).toCartesian(), -1, 1);
        checkVector(PolarCoordinates.of(1, Math.PI).toCartesian(), -1, 0);
        checkVector(PolarCoordinates.of(sqrt2, -0.75 * Math.PI).toCartesian(), -1, -1);

        checkVector(PolarCoordinates.of(1, -Angle.PI_OVER_TWO).toCartesian(), 0, -1);
        checkVector(PolarCoordinates.of(sqrt2, -0.25 * Math.PI).toCartesian(), 1, -1);
    }

    @Test
    void testToCartesian_static() {
        // arrange
        final double sqrt2 = Math.sqrt(2);

        // act/assert
        checkVector(PolarCoordinates.toCartesian(0, 0), 0, 0);

        checkPoint(PolarCoordinates.toCartesian(1, 0), 1, 0);
        checkPoint(PolarCoordinates.toCartesian(sqrt2, 0.25 * Math.PI), 1, 1);
        checkPoint(PolarCoordinates.toCartesian(1, Angle.PI_OVER_TWO), 0, 1);

        checkPoint(PolarCoordinates.toCartesian(sqrt2, 0.75 * Math.PI), -1, 1);
        checkPoint(PolarCoordinates.toCartesian(1, Math.PI), -1, 0);
        checkPoint(PolarCoordinates.toCartesian(sqrt2, -0.75 * Math.PI), -1, -1);

        checkPoint(PolarCoordinates.toCartesian(1, -Angle.PI_OVER_TWO), 0, -1);
        checkPoint(PolarCoordinates.toCartesian(sqrt2, -0.25 * Math.PI), 1, -1);
    }

    @Test
    void testToCartesian_static_NaNAndInfinite() {
        // act/assert
        Assertions.assertTrue(PolarCoordinates.toCartesian(Double.NaN, 0).isNaN());
        Assertions.assertTrue(PolarCoordinates.toCartesian(0, Double.NaN).isNaN());

        Assertions.assertTrue(PolarCoordinates.toCartesian(Double.POSITIVE_INFINITY, 0).isNaN());
        Assertions.assertTrue(PolarCoordinates.toCartesian(0, Double.POSITIVE_INFINITY).isNaN());
        Assertions.assertTrue(PolarCoordinates.toCartesian(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY).isNaN());

        Assertions.assertTrue(PolarCoordinates.toCartesian(Double.NEGATIVE_INFINITY, 0).isNaN());
        Assertions.assertTrue(PolarCoordinates.toCartesian(0, Double.NEGATIVE_INFINITY).isNaN());
        Assertions.assertTrue(PolarCoordinates.toCartesian(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY).isNaN());
    }

    @Test
    void testToString() {
        // arrange
        final PolarCoordinates polar = PolarCoordinates.of(1, 2);
        final Pattern pattern = Pattern.compile("\\(1.{0,2}, 2.{0,2}\\)");

        // act
        final String str = polar.toString();

        // assert
        Assertions.assertTrue(pattern.matcher(str).matches(),
                "Expected string " + str + " to match regex " + pattern);
    }

    @Test
    void testParse() {
        // act/assert
        checkPolar(PolarCoordinates.parse("(1, 2)"), 1, 2);
        checkPolar(PolarCoordinates.parse("( -1 , 0.5 )"), 1, 0.5 + Math.PI);
        checkPolar(PolarCoordinates.parse("(NaN,-Infinity)"), Double.NaN, Double.NEGATIVE_INFINITY);
    }

    @Test
    void testParse_failure() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> PolarCoordinates.parse("abc"));
    }

    @Test
    void testNormalizeAzimuth() {
        // act/assert
        Assertions.assertEquals(0.0, PolarCoordinates.normalizeAzimuth(0), EPS);

        Assertions.assertEquals(Angle.PI_OVER_TWO, PolarCoordinates.normalizeAzimuth(Angle.PI_OVER_TWO), EPS);
        Assertions.assertEquals(Math.PI, PolarCoordinates.normalizeAzimuth(Math.PI), EPS);
        Assertions.assertEquals(THREE_PI_OVER_TWO, PolarCoordinates.normalizeAzimuth(THREE_PI_OVER_TWO), EPS);
        Assertions.assertEquals(0.0, PolarCoordinates.normalizeAzimuth(Angle.TWO_PI), EPS);

        Assertions.assertEquals(THREE_PI_OVER_TWO, PolarCoordinates.normalizeAzimuth(-Angle.PI_OVER_TWO), EPS);
        Assertions.assertEquals(Math.PI, PolarCoordinates.normalizeAzimuth(-Math.PI), EPS);
        Assertions.assertEquals(Angle.PI_OVER_TWO, PolarCoordinates.normalizeAzimuth(-Math.PI - Angle.PI_OVER_TWO), EPS);
        Assertions.assertEquals(0.0, PolarCoordinates.normalizeAzimuth(-Angle.TWO_PI), EPS);
    }

    @Test
    void testNormalizeAzimuth_NaNAndInfinite() {
        // act/assert
        Assertions.assertEquals(Double.NaN, PolarCoordinates.normalizeAzimuth(Double.NaN), EPS);
        Assertions.assertEquals(Double.NEGATIVE_INFINITY, PolarCoordinates.normalizeAzimuth(Double.NEGATIVE_INFINITY), EPS);
        Assertions.assertEquals(Double.POSITIVE_INFINITY, PolarCoordinates.normalizeAzimuth(Double.POSITIVE_INFINITY), EPS);
    }

    private void checkPolar(final PolarCoordinates polar, final double radius, final double azimuth) {
        Assertions.assertEquals(radius, polar.getRadius(), EPS);
        Assertions.assertEquals(azimuth, polar.getAzimuth(), EPS);
    }

    private void checkVector(final Vector2D v, final double x, final double y) {
        Assertions.assertEquals(x, v.getX(), EPS);
        Assertions.assertEquals(y, v.getY(), EPS);
    }

    private void checkPoint(final Vector2D p, final double x, final double y) {
        Assertions.assertEquals(x, p.getX(), EPS);
        Assertions.assertEquals(y, p.getY(), EPS);
    }
}
