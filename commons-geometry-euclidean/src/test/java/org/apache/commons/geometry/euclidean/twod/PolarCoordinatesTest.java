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

import org.apache.commons.geometry.core.Geometry;
import org.junit.Assert;
import org.junit.Test;

public class PolarCoordinatesTest {

    private static final double EPS = 1e-10;

    @Test
    public void testOf() {
        // act/assert
        checkPolar(PolarCoordinates.of(0, 0), 0, 0);

        checkPolar(PolarCoordinates.of(2, 0), 2, 0);
        checkPolar(PolarCoordinates.of(2, Geometry.HALF_PI), 2, Geometry.HALF_PI);
        checkPolar(PolarCoordinates.of(2, Geometry.PI), 2, Geometry.PI);
        checkPolar(PolarCoordinates.of(2, Geometry.MINUS_HALF_PI), 2, Geometry.THREE_HALVES_PI);
    }

    @Test
    public void testOf_unnormalizedAngles() {
        // act/assert
        checkPolar(PolarCoordinates.of(2, Geometry.TWO_PI), 2, 0);
        checkPolar(PolarCoordinates.of(2, Geometry.HALF_PI + Geometry.TWO_PI), 2, Geometry.HALF_PI);
        checkPolar(PolarCoordinates.of(2, -Geometry.PI), 2, Geometry.PI);
        checkPolar(PolarCoordinates.of(2, -Geometry.PI * 1.5), 2, Geometry.HALF_PI);
    }

    @Test
    public void testOf_azimuthWrapAround() {
        // arrange
        double delta = 1e-6;

        // act/assert
        checkAzimuthWrapAround(2, 0);
        checkAzimuthWrapAround(2, delta);
        checkAzimuthWrapAround(2, Geometry.PI - delta);
        checkAzimuthWrapAround(2, Geometry.PI);

        checkAzimuthWrapAround(2, Geometry.THREE_HALVES_PI);
        checkAzimuthWrapAround(2, Geometry.TWO_PI - delta);
    }

    private void checkAzimuthWrapAround(double radius, double azimuth) {
        checkPolar(PolarCoordinates.of(radius, azimuth), radius, azimuth);

        checkPolar(PolarCoordinates.of(radius, azimuth - Geometry.TWO_PI), radius, azimuth);
        checkPolar(PolarCoordinates.of(radius, azimuth - (2 * Geometry.TWO_PI)), radius, azimuth);
        checkPolar(PolarCoordinates.of(radius, azimuth - (3 * Geometry.TWO_PI)), radius, azimuth);

        checkPolar(PolarCoordinates.of(radius, azimuth + Geometry.TWO_PI), radius, azimuth);
        checkPolar(PolarCoordinates.of(radius, azimuth + (2 * Geometry.TWO_PI)), radius, azimuth);
        checkPolar(PolarCoordinates.of(radius, azimuth + (3 * Geometry.TWO_PI)), radius, azimuth);
    }

    @Test
    public void testOf_negativeRadius() {
        // act/assert
        checkPolar(PolarCoordinates.of(-1, 0), 1, Geometry.PI);
        checkPolar(PolarCoordinates.of(-1e-6, Geometry.HALF_PI), 1e-6, Geometry.THREE_HALVES_PI);
        checkPolar(PolarCoordinates.of(-2, Geometry.PI), 2, 0);
        checkPolar(PolarCoordinates.of(-3, Geometry.MINUS_HALF_PI), 3, Geometry.HALF_PI);
    }

    @Test
    public void testOf_NaNAndInfinite() {
        // act/assert
        checkPolar(PolarCoordinates.of(Double.NaN, 0), Double.NaN, 0);
        checkPolar(PolarCoordinates.of(Double.NEGATIVE_INFINITY, 0), Double.POSITIVE_INFINITY, Geometry.PI);
        checkPolar(PolarCoordinates.of(Double.POSITIVE_INFINITY, 0), Double.POSITIVE_INFINITY, 0);

        checkPolar(PolarCoordinates.of(0, Double.NaN), 0, Double.NaN);
        checkPolar(PolarCoordinates.of(0, Double.NEGATIVE_INFINITY), 0, Double.NEGATIVE_INFINITY);
        checkPolar(PolarCoordinates.of(0, Double.POSITIVE_INFINITY), 0, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testFromCartesian_coordinates() {
        // arrange
        double sqrt2 = Math.sqrt(2);

        // act/assert
        checkPolar(PolarCoordinates.fromCartesian(0, 0), 0, 0);

        checkPolar(PolarCoordinates.fromCartesian(1, 0), 1, 0);
        checkPolar(PolarCoordinates.fromCartesian(1, 1), sqrt2, 0.25 * Geometry.PI);
        checkPolar(PolarCoordinates.fromCartesian(0, 1), 1, Geometry.HALF_PI);

        checkPolar(PolarCoordinates.fromCartesian(-1, 1), sqrt2, 0.75 * Geometry.PI);
        checkPolar(PolarCoordinates.fromCartesian(-1, 0), 1, Geometry.PI);
        checkPolar(PolarCoordinates.fromCartesian(-1, -1), sqrt2, 1.25 * Geometry.PI);

        checkPolar(PolarCoordinates.fromCartesian(0, -1), 1, 1.5 * Geometry.PI);
        checkPolar(PolarCoordinates.fromCartesian(1, -1), sqrt2, 1.75 * Geometry.PI);
    }

    @Test
    public void testFromCartesian_vector() {
        // arrange
        double sqrt2 = Math.sqrt(2);

        // act/assert
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(0, 0)), 0, 0);

        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(1, 0)), 1, 0);
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(1, 1)), sqrt2, 0.25 * Geometry.PI);
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(0, 1)), 1, Geometry.HALF_PI);

        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(-1, 1)), sqrt2, 0.75 * Geometry.PI);
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(-1, 0)), 1, Geometry.PI);
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(-1, -1)), sqrt2, 1.25 * Geometry.PI);

        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(0, -1)), 1, 1.5 * Geometry.PI);
        checkPolar(PolarCoordinates.fromCartesian(Vector2D.of(1, -1)), sqrt2, 1.75 * Geometry.PI);
    }

    @Test
    public void testDimension() {
        // arrange
        PolarCoordinates p = PolarCoordinates.of(1, 0);

        // act/assert
        Assert.assertEquals(2, p.getDimension());
    }

    @Test
    public void testIsNaN() {
        // act/assert
        Assert.assertFalse(PolarCoordinates.of(1, 0).isNaN());
        Assert.assertFalse(PolarCoordinates.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isNaN());

        Assert.assertTrue(PolarCoordinates.of(Double.NaN, 0).isNaN());
        Assert.assertTrue(PolarCoordinates.of(1, Double.NaN).isNaN());
        Assert.assertTrue(PolarCoordinates.of(Double.NaN, Double.NaN).isNaN());
    }

    @Test
    public void testIsInfinite() {
        // act/assert
        Assert.assertFalse(PolarCoordinates.of(1, 0).isInfinite());
        Assert.assertFalse(PolarCoordinates.of(Double.NaN, Double.NaN).isInfinite());

        Assert.assertTrue(PolarCoordinates.of(Double.POSITIVE_INFINITY, 0).isInfinite());
        Assert.assertTrue(PolarCoordinates.of(Double.NEGATIVE_INFINITY, 0).isInfinite());
        Assert.assertFalse(PolarCoordinates.of(Double.NEGATIVE_INFINITY, Double.NaN).isInfinite());

        Assert.assertTrue(PolarCoordinates.of(0, Double.POSITIVE_INFINITY).isInfinite());
        Assert.assertTrue(PolarCoordinates.of(0, Double.NEGATIVE_INFINITY).isInfinite());
        Assert.assertFalse(PolarCoordinates.of(Double.NaN, Double.NEGATIVE_INFINITY).isInfinite());

        Assert.assertTrue(PolarCoordinates.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isInfinite());
        Assert.assertTrue(PolarCoordinates.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).isInfinite());
    }

    @Test
    public void testIsFinite() {
        // act/assert
        Assert.assertTrue(PolarCoordinates.of(1, 0).isFinite());
        Assert.assertTrue(PolarCoordinates.of(1, Geometry.PI).isFinite());

        Assert.assertFalse(PolarCoordinates.of(Double.NaN, Double.NaN).isFinite());

        Assert.assertFalse(PolarCoordinates.of(Double.POSITIVE_INFINITY, 0).isFinite());
        Assert.assertFalse(PolarCoordinates.of(Double.NEGATIVE_INFINITY, 0).isFinite());
        Assert.assertFalse(PolarCoordinates.of(Double.NEGATIVE_INFINITY, Double.NaN).isFinite());

        Assert.assertFalse(PolarCoordinates.of(0, Double.POSITIVE_INFINITY).isFinite());
        Assert.assertFalse(PolarCoordinates.of(0, Double.NEGATIVE_INFINITY).isFinite());
        Assert.assertFalse(PolarCoordinates.of(Double.NaN, Double.NEGATIVE_INFINITY).isFinite());

        Assert.assertFalse(PolarCoordinates.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).isFinite());
        Assert.assertFalse(PolarCoordinates.of(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY).isFinite());
    }

    @Test
    public void testHashCode() {
        // arrange
        PolarCoordinates a = PolarCoordinates.of(1, 2);
        PolarCoordinates b = PolarCoordinates.of(10, 2);
        PolarCoordinates c = PolarCoordinates.of(10, 20);
        PolarCoordinates d = PolarCoordinates.of(1, 20);

        PolarCoordinates e = PolarCoordinates.of(1, 2);

        // act/assert
        Assert.assertEquals(a.hashCode(), a.hashCode());
        Assert.assertEquals(a.hashCode(), e.hashCode());

        Assert.assertNotEquals(a.hashCode(), b.hashCode());
        Assert.assertNotEquals(a.hashCode(), c.hashCode());
        Assert.assertNotEquals(a.hashCode(), d.hashCode());
    }

    @Test
    public void testHashCode_NaNInstancesHaveSameHashCode() {
        // arrange
        PolarCoordinates a = PolarCoordinates.of(1, Double.NaN);
        PolarCoordinates b = PolarCoordinates.of(Double.NaN, 1);

        // act/assert
        Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        PolarCoordinates a = PolarCoordinates.of(1, 2);
        PolarCoordinates b = PolarCoordinates.of(10, 2);
        PolarCoordinates c = PolarCoordinates.of(10, 20);
        PolarCoordinates d = PolarCoordinates.of(1, 20);

        PolarCoordinates e = PolarCoordinates.of(1, 2);

        // act/assert
        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertTrue(a.equals(a));
        Assert.assertTrue(a.equals(e));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(a.equals(d));
    }

    @Test
    public void testEquals_NaNInstancesEqual() {
        // arrange
        PolarCoordinates a = PolarCoordinates.of(1, Double.NaN);
        PolarCoordinates b = PolarCoordinates.of(Double.NaN, 1);

        // act/assert
        Assert.assertTrue(a.equals(b));
    }

    @Test
    public void testToCartesian() {
        // arrange
        double sqrt2 = Math.sqrt(2);

        // act/assert
        checkVector(PolarCoordinates.of(0, 0).toCartesian(), 0, 0);

        checkVector(PolarCoordinates.of(1, 0).toCartesian(), 1, 0);
        checkVector(PolarCoordinates.of(sqrt2, 0.25 * Geometry.PI).toCartesian(), 1, 1);
        checkVector(PolarCoordinates.of(1, Geometry.HALF_PI).toCartesian(), 0, 1);

        checkVector(PolarCoordinates.of(sqrt2, 0.75 * Geometry.PI).toCartesian(), -1, 1);
        checkVector(PolarCoordinates.of(1, Geometry.PI).toCartesian(), -1, 0);
        checkVector(PolarCoordinates.of(sqrt2, -0.75 * Geometry.PI).toCartesian(), -1, -1);

        checkVector(PolarCoordinates.of(1, Geometry.MINUS_HALF_PI).toCartesian(), 0, -1);
        checkVector(PolarCoordinates.of(sqrt2, -0.25 * Geometry.PI).toCartesian(), 1, -1);
    }

    @Test
    public void testToCartesian_static() {
        // arrange
        double sqrt2 = Math.sqrt(2);

        // act/assert
        checkVector(PolarCoordinates.toCartesian(0, 0), 0, 0);

        checkPoint(PolarCoordinates.toCartesian(1, 0), 1, 0);
        checkPoint(PolarCoordinates.toCartesian(sqrt2, 0.25 * Geometry.PI), 1, 1);
        checkPoint(PolarCoordinates.toCartesian(1, Geometry.HALF_PI), 0, 1);

        checkPoint(PolarCoordinates.toCartesian(sqrt2, 0.75 * Geometry.PI), -1, 1);
        checkPoint(PolarCoordinates.toCartesian(1, Geometry.PI), -1, 0);
        checkPoint(PolarCoordinates.toCartesian(sqrt2, -0.75 * Geometry.PI), -1, -1);

        checkPoint(PolarCoordinates.toCartesian(1, Geometry.MINUS_HALF_PI), 0, -1);
        checkPoint(PolarCoordinates.toCartesian(sqrt2, -0.25 * Geometry.PI), 1, -1);
    }

    @Test
    public void testToCartesian_static_NaNAndInfinite() {
        // act/assert
        Assert.assertTrue(PolarCoordinates.toCartesian(Double.NaN, 0).isNaN());
        Assert.assertTrue(PolarCoordinates.toCartesian(0, Double.NaN).isNaN());

        Assert.assertTrue(PolarCoordinates.toCartesian(Double.POSITIVE_INFINITY, 0).isNaN());
        Assert.assertTrue(PolarCoordinates.toCartesian(0, Double.POSITIVE_INFINITY).isNaN());
        Assert.assertTrue(PolarCoordinates.toCartesian(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY).isNaN());

        Assert.assertTrue(PolarCoordinates.toCartesian(Double.NEGATIVE_INFINITY, 0).isNaN());
        Assert.assertTrue(PolarCoordinates.toCartesian(0, Double.NEGATIVE_INFINITY).isNaN());
        Assert.assertTrue(PolarCoordinates.toCartesian(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY).isNaN());
    }

    @Test
    public void testToString() {
        // arrange
        PolarCoordinates polar = PolarCoordinates.of(1, 2);
        Pattern pattern = Pattern.compile("\\(1.{0,2}, 2.{0,2}\\)");

        // act
        String str = polar.toString();;

        // assert
        Assert.assertTrue("Expected string " + str + " to match regex " + pattern,
                    pattern.matcher(str).matches());
    }

    @Test
    public void testParse() {
        // act/assert
        checkPolar(PolarCoordinates.parse("(1, 2)"), 1, 2);
        checkPolar(PolarCoordinates.parse("( -1 , 0.5 )"), 1, 0.5 + Geometry.PI);
        checkPolar(PolarCoordinates.parse("(NaN,-Infinity)"), Double.NaN, Double.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        PolarCoordinates.parse("abc");
    }

    @Test
    public void testNormalizeAzimuth() {
        // act/assert
        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(0), 0.0, EPS);

        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(Geometry.HALF_PI), Geometry.HALF_PI, EPS);
        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(Geometry.PI), Geometry.PI, EPS);
        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(Geometry.THREE_HALVES_PI), Geometry.THREE_HALVES_PI, EPS);
        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(Geometry.TWO_PI), 0.0, EPS);

        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(Geometry.MINUS_HALF_PI), Geometry.THREE_HALVES_PI, EPS);
        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(-Geometry.PI), Geometry.PI, EPS);
        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(-Geometry.PI - Geometry.HALF_PI), Geometry.HALF_PI, EPS);
        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(-Geometry.TWO_PI), 0.0, EPS);
    }

    @Test
    public void testNormalizeAzimuth_NaNAndInfinite() {
        // act/assert
        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(Double.NaN), Double.NaN, EPS);
        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(Double.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY, EPS);
        Assert.assertEquals(PolarCoordinates.normalizeAzimuth(Double.POSITIVE_INFINITY), Double.POSITIVE_INFINITY, EPS);
    }

    private void checkPolar(PolarCoordinates polar, double radius, double azimuth) {
        Assert.assertEquals(radius, polar.getRadius(), EPS);
        Assert.assertEquals(azimuth, polar.getAzimuth(), EPS);
    }

    private void checkVector(Vector2D v, double x, double y) {
        Assert.assertEquals(x, v.getX(), EPS);
        Assert.assertEquals(y, v.getY(), EPS);
    }

    private void checkPoint(Vector2D p, double x, double y) {
        Assert.assertEquals(x, p.getX(), EPS);
        Assert.assertEquals(y, p.getY(), EPS);
    }
}
