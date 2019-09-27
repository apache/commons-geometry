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
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.exception.IllegalNormException;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.numbers.core.Precision;
import org.junit.Assert;
import org.junit.Test;

public class Vector2DTest {

    private static final double EPS = Math.ulp(1d);

    @Test
    public void testConstants() {
        // act/assert
        checkVector(Vector2D.ZERO, 0, 0);
        checkVector(Vector2D.PLUS_X, 1, 0);
        checkVector(Vector2D.MINUS_X, -1, 0);
        checkVector(Vector2D.PLUS_Y, 0, 1);
        checkVector(Vector2D.MINUS_Y, 0, -1);
        checkVector(Vector2D.NaN, Double.NaN, Double.NaN);
        checkVector(Vector2D.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        checkVector(Vector2D.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testConstants_normalize() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> Vector2D.ZERO.normalize(),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NaN.normalize(),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.POSITIVE_INFINITY.normalize(),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NEGATIVE_INFINITY.normalize(),
                IllegalNormException.class);

        Assert.assertSame(Vector2D.PLUS_X.normalize(), Vector2D.PLUS_X);
        Assert.assertSame(Vector2D.MINUS_X.normalize(), Vector2D.MINUS_X);

        Assert.assertSame(Vector2D.PLUS_Y.normalize(), Vector2D.PLUS_Y);
        Assert.assertSame(Vector2D.MINUS_Y.normalize(), Vector2D.MINUS_Y);
    }

    @Test
    public void testCoordinates() {
        // arrange
        Vector2D v = Vector2D.of(1, 2);

        // act/assert
        Assert.assertEquals(1.0, v.getX(), EPS);
        Assert.assertEquals(2.0, v.getY(), EPS);
    }

    @Test
    public void testToArray() {
        // arrange
        Vector2D oneTwo = Vector2D.of(1, 2);

        // act
        double[] array = oneTwo.toArray();

        // assert
        Assert.assertEquals(2, array.length);
        Assert.assertEquals(1.0, array[0], EPS);
        Assert.assertEquals(2.0, array[1], EPS);
    }

    @Test
    public void testDimension() {
        // arrange
        Vector2D v = Vector2D.of(1, 2);

        // act/assert
        Assert.assertEquals(2, v.getDimension());
    }

    @Test
    public void testNaN() {
        // act/assert
        Assert.assertTrue(Vector2D.of(0, Double.NaN).isNaN());
        Assert.assertTrue(Vector2D.of(Double.NaN, 0).isNaN());

        Assert.assertFalse(Vector2D.of(1, 1).isNaN());
        Assert.assertFalse(Vector2D.of(1, Double.NEGATIVE_INFINITY).isNaN());
        Assert.assertFalse(Vector2D.of(Double.POSITIVE_INFINITY, 1).isNaN());
    }

    @Test
    public void testInfinite() {
        // act/assert
        Assert.assertTrue(Vector2D.of(0, Double.NEGATIVE_INFINITY).isInfinite());
        Assert.assertTrue(Vector2D.of(Double.NEGATIVE_INFINITY, 0).isInfinite());
        Assert.assertTrue(Vector2D.of(0, Double.POSITIVE_INFINITY).isInfinite());
        Assert.assertTrue(Vector2D.of(Double.POSITIVE_INFINITY, 0).isInfinite());

        Assert.assertFalse(Vector2D.of(1, 1).isInfinite());
        Assert.assertFalse(Vector2D.of(0, Double.NaN).isInfinite());
        Assert.assertFalse(Vector2D.of(Double.NEGATIVE_INFINITY, Double.NaN).isInfinite());
        Assert.assertFalse(Vector2D.of(Double.NaN, Double.NEGATIVE_INFINITY).isInfinite());
        Assert.assertFalse(Vector2D.of(Double.POSITIVE_INFINITY, Double.NaN).isInfinite());
        Assert.assertFalse(Vector2D.of(Double.NaN, Double.POSITIVE_INFINITY).isInfinite());
    }

    @Test
    public void testGetZero() {
        // act/assert
        checkVector(Vector2D.of(1.0, 1.0).getZero(), 0, 0);
    }

    @Test
    public void testNorm() {
        // act/assert
        Assert.assertEquals(0.0, Vector2D.of(0, 0).norm(), EPS);

        Assert.assertEquals(5.0, Vector2D.of(3, 4).norm(), EPS);
        Assert.assertEquals(5.0, Vector2D.of(3, -4).norm(), EPS);
        Assert.assertEquals(5.0, Vector2D.of(-3, 4).norm(), EPS);
        Assert.assertEquals(5.0, Vector2D.of(-3, -4).norm(), EPS);

        Assert.assertEquals(Math.sqrt(5.0), Vector2D.of(-1, -2).norm(), EPS);
    }

    @Test
    public void testNorm_unitVectors() {
        // arrange
        Vector2D v = Vector2D.of(2.0, 3.0).normalize();

        // act/assert
        Assert.assertEquals(1.0, v.norm(), 0.0);
    }

    @Test
    public void testNormSq() {
        // act/assert
        Assert.assertEquals(0.0, Vector2D.of(0, 0).normSq(), EPS);

        Assert.assertEquals(25.0, Vector2D.of(3, 4).normSq(), EPS);
        Assert.assertEquals(25.0, Vector2D.of(3, -4).normSq(), EPS);
        Assert.assertEquals(25.0, Vector2D.of(-3, 4).normSq(), EPS);
        Assert.assertEquals(25.0, Vector2D.of(-3, -4).normSq(), EPS);

        Assert.assertEquals(5.0, Vector2D.of(-1, -2).normSq(), EPS);
    }

    @Test
    public void testNormSq_unitVectors() {
        // arrange
        Vector2D v = Vector2D.of(2.0, 3.0).normalize();

        // act/assert
        Assert.assertEquals(1.0, v.normSq(), 0.0);
    }

    @Test
    public void testWithNorm() {
        // act/assert
        checkVector(Vector2D.of(3, 4).withNorm(1.0), 0.6, 0.8);
        checkVector(Vector2D.of(4, 3).withNorm(1.0), 0.8, 0.6);

        checkVector(Vector2D.of(-3, 4).withNorm(0.5), -0.3, 0.4);
        checkVector(Vector2D.of(3, -4).withNorm(2.0), 1.2, -1.6);
        checkVector(Vector2D.of(-3, -4).withNorm(3.0), -1.8, 3.0 * Math.sin(Math.atan2(-4, -3)));

        checkVector(Vector2D.of(0.5, 0.5).withNorm(2), Math.sqrt(2), Math.sqrt(2));
    }

    @Test
    public void testWithNorm_illegalNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> Vector2D.ZERO.withNorm(2.0),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NaN.withNorm(2.0),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.POSITIVE_INFINITY.withNorm(2.0),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NEGATIVE_INFINITY.withNorm(2.0),
                IllegalNormException.class);
    }

    @Test
    public void testWithNorm_unitVectors() {
        // arrange
        double eps = 1e-14;
        Vector2D v = Vector2D.of(2.0, -3.0).normalize();

        // act/assert
        checkVector(Vector2D.PLUS_X.withNorm(2.5), 2.5, 0.0);
        checkVector(Vector2D.MINUS_Y.withNorm(3.14), 0.0, -3.14);

        for (int i = -10; i <= 10; i++) {
            final double mag = i;
            Assert.assertEquals(Math.abs(mag), v.withNorm(mag).norm(), eps);
        }
    }

    @Test
    public void testAdd() {
        // arrange
        Vector2D v1 = Vector2D.of(-1, 2);
        Vector2D v2 = Vector2D.of(3, -4);
        Vector2D v3 = Vector2D.of(5, 6);

        // act/assert
        checkVector(v1.add(v1), -2, 4);

        checkVector(v1.add(v2), 2, -2);
        checkVector(v2.add(v1), 2, -2);

        checkVector(v1.add(v3), 4, 8);
        checkVector(v3.add(v1), 4, 8);
    }

    @Test
    public void testAdd_scaled() {
        // arrange
        Vector2D v1 = Vector2D.of(-1, 2);
        Vector2D v2 = Vector2D.of(3, -4);
        Vector2D v3 = Vector2D.of(5, 6);

        // act/assert
        checkVector(v1.add(2, v1), -3, 6);

        checkVector(v1.add(0, v2), -1, 2);
        checkVector(v2.add(1, v1), 2, -2);

        checkVector(v1.add(-1, v3), -6, -4);
        checkVector(v3.add(-2, v1), 7, 2);
    }

    @Test
    public void testSubtract() {
        // arrange
        Vector2D v1 = Vector2D.of(-1, 2);
        Vector2D v2 = Vector2D.of(3, -4);
        Vector2D v3 = Vector2D.of(5, 6);

        // act/assert
        checkVector(v1.subtract(v1), 0, 0);

        checkVector(v1.subtract(v2), -4, 6);
        checkVector(v2.subtract(v1), 4, -6);

        checkVector(v1.subtract(v3), -6, -4);
        checkVector(v3.subtract(v1), 6, 4);
    }

    @Test
    public void testSubtract_scaled() {
        // arrange
        Vector2D v1 = Vector2D.of(-1, 2);
        Vector2D v2 = Vector2D.of(3, -4);
        Vector2D v3 = Vector2D.of(5, 6);

        // act/assert
        checkVector(v1.subtract(2, v1), 1, -2);

        checkVector(v1.subtract(0, v2), -1, 2);
        checkVector(v2.subtract(1, v1), 4, -6);

        checkVector(v1.subtract(-1, v3), 4, 8);
        checkVector(v3.subtract(-2, v1), 3, 10);
    }

    @Test
    public void testNormalize() {
        // act/assert
        checkVector(Vector2D.of(100, 0).normalize(), 1, 0);
        checkVector(Vector2D.of(-100, 0).normalize(), -1, 0);
        checkVector(Vector2D.of(0, 100).normalize(), 0, 1);
        checkVector(Vector2D.of(0, -100).normalize(), 0, -1);
        checkVector(Vector2D.of(-1, 2).normalize(), -1.0 / Math.sqrt(5), 2.0 / Math.sqrt(5));
    }

    @Test
    public void testNormalize_illegalNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> Vector2D.ZERO.normalize(),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NaN.normalize(),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.POSITIVE_INFINITY.normalize(),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NEGATIVE_INFINITY.normalize(),
                IllegalNormException.class);
    }

    @Test
    public void testNormalize_isIdempotent() {
        // arrange
        double invSqrt2 = 1.0 / Math.sqrt(2);
        Vector2D v = Vector2D.of(2, 2).normalize();

        // act/assert
        Assert.assertSame(v, v.normalize());
        checkVector(v.normalize(), invSqrt2, invSqrt2);
    }

    @Test
    public void testNegate() {
        // act/assert
        checkVector(Vector2D.of(1, 2).negate(), -1, -2);
        checkVector(Vector2D.of(-3, -4).negate(), 3, 4);
        checkVector(Vector2D.of(5, -6).negate().negate(), 5, -6);
    }

    @Test
    public void testNegate_unitVectors() {
        // arrange
        Vector2D v1 = Vector2D.of(1.0, 1.0).normalize();
        Vector2D v2 = Vector2D.of(-1.0, -2.0).normalize();
        Vector2D v3 = Vector2D.of(2.0, -3.0).normalize();

        // act/assert
        checkVector(v1.negate(), -1.0 / Math.sqrt(2.0), -1.0 / Math.sqrt(2.0));
        checkVector(v2.negate(), 1.0 / Math.sqrt(5.0), 2.0 / Math.sqrt(5.0));
        checkVector(v3.negate(), -2.0 / Math.sqrt(13.0), 3.0 / Math.sqrt(13.0));
    }

    @Test
    public void testScalarMultiply() {
        // act/assert
        checkVector(Vector2D.of(1, 2).multiply(0), 0, 0);

        checkVector(Vector2D.of(1, 2).multiply(3), 3, 6);
        checkVector(Vector2D.of(1, 2).multiply(-3), -3, -6);

        checkVector(Vector2D.of(2, 3).multiply(1.5), 3, 4.5);
        checkVector(Vector2D.of(2, 3).multiply(-1.5), -3, -4.5);
    }

    @Test
    public void testDistance() {
        // arrange
        Vector2D v1 = Vector2D.of(1, 1);
        Vector2D v2 = Vector2D.of(4, 5);
        Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assert.assertEquals(0, v1.distance(v1), EPS);

        Assert.assertEquals(5, v1.distance(v2), EPS);
        Assert.assertEquals(5, v2.distance(v1), EPS);

        Assert.assertEquals(Math.sqrt(5), v1.distance(v3), EPS);
        Assert.assertEquals(Math.sqrt(5), v3.distance(v1), EPS);
    }

    @Test
    public void testDistanceSq() {
        // arrange
        Vector2D v1 = Vector2D.of(1, 1);
        Vector2D v2 = Vector2D.of(4, 5);
        Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assert.assertEquals(0, v1.distanceSq(v1), EPS);

        Assert.assertEquals(25, v1.distanceSq(v2), EPS);
        Assert.assertEquals(25, v2.distanceSq(v1), EPS);

        Assert.assertEquals(5, v1.distanceSq(v3), EPS);
        Assert.assertEquals(5, v3.distanceSq(v1), EPS);
    }

    @Test
    public void testDotProduct() {
        // arrange
        Vector2D v1 = Vector2D.of(1, 1);
        Vector2D v2 = Vector2D.of(4, 5);
        Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assert.assertEquals(2, v1.dot(v1), EPS);
        Assert.assertEquals(41, v2.dot(v2), EPS);
        Assert.assertEquals(1, v3.dot(v3), EPS);

        Assert.assertEquals(9, v1.dot(v2), EPS);
        Assert.assertEquals(9, v2.dot(v1), EPS);

        Assert.assertEquals(-1, v1.dot(v3), EPS);
        Assert.assertEquals(-1, v3.dot(v1), EPS);

        Assert.assertEquals(1, Vector2D.PLUS_X.dot(Vector2D.PLUS_X), EPS);
        Assert.assertEquals(0, Vector2D.PLUS_X.dot(Vector2D.PLUS_Y), EPS);
        Assert.assertEquals(-1, Vector2D.PLUS_X.dot(Vector2D.MINUS_X), EPS);
        Assert.assertEquals(0, Vector2D.PLUS_X.dot(Vector2D.MINUS_Y), EPS);
    }

    @Test
    public void testOrthogonal() {
        // arrange
        double invSqrt2 = 1.0 / Math.sqrt(2.0);

        // act/assert
        checkVector(Vector2D.PLUS_X.orthogonal(), 0.0, 1.0);
        checkVector(Vector2D.of(1.0, 1.0).orthogonal(), -invSqrt2, invSqrt2);

        checkVector(Vector2D.PLUS_Y.orthogonal(), -1.0, 0.0);
        checkVector(Vector2D.of(-1.0, 1.0).orthogonal(), -invSqrt2, -invSqrt2);

        checkVector(Vector2D.MINUS_X.orthogonal(), 0.0, -1.0);
        checkVector(Vector2D.of(-1.0, -1.0).orthogonal(), invSqrt2, -invSqrt2);

        checkVector(Vector2D.MINUS_Y.orthogonal(), 1.0, 0.0);
        checkVector(Vector2D.of(1.0, -1.0).orthogonal(), invSqrt2, invSqrt2);
    }

    @Test
    public void testOrthogonal_fullCircle() {
        for (double az = 0.0; az<=Geometry.TWO_PI; az += 0.25) {
            // arrange
            Vector2D v = PolarCoordinates.toCartesian(Math.PI, az);

            //act
            Vector2D ortho = v.orthogonal();

            // assert
            Assert.assertEquals(1.0, ortho.norm(), EPS);
            Assert.assertEquals(0.0, v.dot(ortho), EPS);
        }
    }

    @Test
    public void testOrthogonal_illegalNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> Vector2D.ZERO.orthogonal(),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NaN.orthogonal(),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.POSITIVE_INFINITY.orthogonal(),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NEGATIVE_INFINITY.orthogonal(),
                IllegalNormException.class);
    }

    @Test
    public void testOrthogonal_givenDirection() {
        // arrange
        double invSqrt2 = 1.0 / Math.sqrt(2.0);

        // act/assert
        checkVector(Vector2D.PLUS_X.orthogonal(Vector2D.of(-1.0, 0.1)), 0.0, 1.0);
        checkVector(Vector2D.PLUS_Y.orthogonal(Vector2D.of(2.0, 2.0)), 1.0, 0.0);

        checkVector(Vector2D.of(2.9, 2.9).orthogonal(Vector2D.of(1.0, 0.22)), invSqrt2, -invSqrt2);
        checkVector(Vector2D.of(2.9, 2.9).orthogonal(Vector2D.of(0.22, 1.0)), -invSqrt2, invSqrt2);
    }

    @Test
    public void testOrthogonal_givenDirection_illegalNorm() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> Vector2D.ZERO.orthogonal(Vector2D.PLUS_X),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NaN.orthogonal(Vector2D.PLUS_X),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.POSITIVE_INFINITY.orthogonal(Vector2D.PLUS_X),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NEGATIVE_INFINITY.orthogonal(Vector2D.PLUS_X),
                IllegalNormException.class);

        GeometryTestUtils.assertThrows(() -> Vector2D.PLUS_X.orthogonal(Vector2D.ZERO),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.PLUS_X.orthogonal(Vector2D.NaN),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.PLUS_X.orthogonal(Vector2D.POSITIVE_INFINITY),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.PLUS_X.orthogonal(Vector2D.NEGATIVE_INFINITY),
                IllegalNormException.class);
    }

    @Test
    public void testOrthogonal_givenDirection_directionIsCollinear() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> Vector2D.PLUS_X.orthogonal(Vector2D.PLUS_X),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.PLUS_X.orthogonal(Vector2D.MINUS_X),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.of(1.0, 1.0).orthogonal(Vector2D.of(2.0, 2.0)),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.of(-1.01, -1.01).orthogonal(Vector2D.of(20.1, 20.1)),
                IllegalNormException.class);
    }

    @Test
    public void testAngle() {
        // act/assert
        Assert.assertEquals(0, Vector2D.PLUS_X.angle(Vector2D.PLUS_X), EPS);

        Assert.assertEquals(Geometry.PI, Vector2D.PLUS_X.angle(Vector2D.MINUS_X), EPS);
        Assert.assertEquals(Geometry.HALF_PI, Vector2D.PLUS_X.angle(Vector2D.PLUS_Y), EPS);
        Assert.assertEquals(Geometry.HALF_PI, Vector2D.PLUS_X.angle(Vector2D.MINUS_Y), EPS);

        Assert.assertEquals(Geometry.PI / 4, Vector2D.of(1, 1).angle(Vector2D.of(1, 0)), EPS);
        Assert.assertEquals(Geometry.PI / 4, Vector2D.of(1, 0).angle(Vector2D.of(1, 1)), EPS);

        Assert.assertEquals(0.004999958333958323, Vector2D.of(20.0, 0.0).angle(Vector2D.of(20.0, 0.1)), EPS);
    }


    @Test
    public void testAngle_illegalNorm() {
        // arrange
        Vector2D v = Vector2D.of(1.0, 1.0);

        // act/assert
        GeometryTestUtils.assertThrows(() -> Vector2D.ZERO.angle(v),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NaN.angle(v),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.POSITIVE_INFINITY.angle(v),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NEGATIVE_INFINITY.angle(v),
                IllegalNormException.class);

        GeometryTestUtils.assertThrows(() -> v.angle(Vector2D.ZERO),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> v.angle(Vector2D.NaN),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> v.angle(Vector2D.POSITIVE_INFINITY),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> v.angle(Vector2D.NEGATIVE_INFINITY),
                IllegalNormException.class);
    }

    @Test
    public void testSignedArea() {
        // arrange
        double eps = 1e-10;

        Vector2D a = Vector2D.PLUS_X;
        Vector2D b = Vector2D.PLUS_Y;
        Vector2D c = Vector2D.of(1, 1).withNorm(2.0);
        Vector2D d = Vector2D.of(-1, 1).withNorm(3.0);

        // act/assert
        Assert.assertEquals(1.0, a.signedArea(b), eps);
        Assert.assertEquals(-1.0, b.signedArea(a), eps);

        double xAxisAndCArea = 2 * Math.cos(0.25 * Geometry.PI);
        Assert.assertEquals(xAxisAndCArea, a.signedArea(c), eps);
        Assert.assertEquals(-xAxisAndCArea, c.signedArea(a), eps);

        double xAxisAndDArea = 3 * Math.cos(0.25 * Geometry.PI);
        Assert.assertEquals(xAxisAndDArea, a.signedArea(d), eps);
        Assert.assertEquals(-xAxisAndDArea, d.signedArea(a), eps);

        Assert.assertEquals(6.0, c.signedArea(d), eps);
        Assert.assertEquals(-6.0, d.signedArea(c), eps);
    }

    @Test
    public void testSignedArea_collinear() {
        // arrange
        Vector2D a = Vector2D.PLUS_X;
        Vector2D b = Vector2D.PLUS_Y;
        Vector2D c = Vector2D.of(-3, 8);

        // act/assert
        Assert.assertEquals(0.0, a.signedArea(a), EPS);
        Assert.assertEquals(0.0, b.signedArea(b), EPS);
        Assert.assertEquals(0.0, c.signedArea(c), EPS);

        Assert.assertEquals(0.0, a.signedArea(a.multiply(100.0)), EPS);
        Assert.assertEquals(0.0, b.signedArea(b.negate()), EPS);
        Assert.assertEquals(0.0, c.signedArea(c.multiply(-0.03)), EPS);
    }

    @Test
    public void testProject() {
        // arrange
        Vector2D v1 = Vector2D.of(3.0, 4.0);
        Vector2D v2 = Vector2D.of(1.0, 4.0);

        // act/assert
        checkVector(Vector2D.ZERO.project(v1), 0.0, 0.0);

        checkVector(v1.project(v1), 3.0, 4.0);
        checkVector(v1.project(v1.negate()), 3.0, 4.0);

        checkVector(v1.project(Vector2D.PLUS_X), 3.0, 0.0);
        checkVector(v1.project(Vector2D.MINUS_X), 3.0, 0.0);

        checkVector(v1.project(Vector2D.PLUS_Y), 0.0, 4.0);
        checkVector(v1.project(Vector2D.MINUS_Y), 0.0, 4.0);

        checkVector(v2.project(v1), (19.0 / 25.0) * 3.0, (19.0 / 25.0) * 4.0);
    }

    @Test
    public void testProject_baseHasIllegalNorm() {
        // arrange
        Vector2D v = Vector2D.of(1.0, 1.0);

        // act/assert
        GeometryTestUtils.assertThrows(() -> v.project(Vector2D.ZERO),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> v.project(Vector2D.NaN),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> v.project(Vector2D.POSITIVE_INFINITY),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> v.project(Vector2D.NEGATIVE_INFINITY),
                IllegalNormException.class);
    }

    @Test
    public void testReject() {
        // arrange
        Vector2D v1 = Vector2D.of(3.0, 4.0);
        Vector2D v2 = Vector2D.of(1.0, 4.0);

        // act/assert
        checkVector(Vector2D.ZERO.reject(v1), 0.0, 0.0);

        checkVector(v1.reject(v1), 0.0, 0.0);
        checkVector(v1.reject(v1.negate()), 0.0, 0.0);

        checkVector(v1.reject(Vector2D.PLUS_X), 0.0, 4.0);
        checkVector(v1.reject(Vector2D.MINUS_X), 0.0, 4.0);

        checkVector(v1.reject(Vector2D.PLUS_Y), 3.0, 0.0);
        checkVector(v1.reject(Vector2D.MINUS_Y), 3.0, 0.0);

        checkVector(v2.reject(v1), (-32.0 / 25.0), (6.0 / 25.0) * 4.0);
    }

    @Test
    public void testReject_baseHasIllegalNorm() {
        // arrange
        Vector2D v = Vector2D.of(1.0, 1.0);

        // act/assert
        GeometryTestUtils.assertThrows(() -> v.reject(Vector2D.ZERO),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> v.reject(Vector2D.NaN),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> v.reject(Vector2D.POSITIVE_INFINITY),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> v.reject(Vector2D.NEGATIVE_INFINITY),
                IllegalNormException.class);
    }

    @Test
    public void testProjectAndReject_areComplementary() {
        // arrange
        double eps = 1e-12;

        // act/assert
        checkProjectAndRejectFullCircle(Vector2D.of(1.0, 0.0), 1.0, eps);
        checkProjectAndRejectFullCircle(Vector2D.of(0.0, 1.0), 2.0, eps);
        checkProjectAndRejectFullCircle(Vector2D.of(1.0, 1.0), 3.0, eps);

        checkProjectAndRejectFullCircle(Vector2D.of(-2.0, 0.0), 4.0, eps);
        checkProjectAndRejectFullCircle(Vector2D.of(0.0, -2.0), 5.0, eps);
        checkProjectAndRejectFullCircle(Vector2D.of(-2.0, -2.0), 6.0, eps);
    }

    private void checkProjectAndRejectFullCircle(Vector2D vec, double baseMag, double eps) {
        for (double theta = 0.0; theta <= Geometry.TWO_PI; theta += 0.5) {
            Vector2D base = PolarCoordinates.toCartesian(baseMag, theta);

            Vector2D proj = vec.project(base);
            Vector2D rej = vec.reject(base);

            // ensure that the projection and rejection sum to the original vector
            EuclideanTestUtils.assertCoordinatesEqual(vec, proj.add(rej), eps);

            double angle = base.angle(vec);

            // check the angle between the projection and the base; this will
            // be undefined when the angle between the original vector and the
            // base is pi/2 (which means that the projection is the zero vector)
            if (angle < Geometry.HALF_PI) {
                Assert.assertEquals(0.0, proj.angle(base), eps);
            }
            else if (angle > Geometry.HALF_PI) {
                Assert.assertEquals(Geometry.PI, proj.angle(base), eps);
            }

            // check the angle between the rejection and the base; this should
            // always be pi/2 except for when the angle between the original vector
            // and the base is 0 or pi, in which case the rejection is the zero vector.
            if (angle > 0.0 && angle < Geometry.PI) {
                Assert.assertEquals(Geometry.HALF_PI, rej.angle(base), eps);
            }
        }
    }

    @Test
    public void testVectorTo() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 1);
        Vector2D p2 = Vector2D.of(4, 5);
        Vector2D p3 = Vector2D.of(-1, 0);

        // act/assert
        checkVector(p1.vectorTo(p1), 0, 0);
        checkVector(p1.vectorTo(p2), 3, 4);
        checkVector(p2.vectorTo(p1), -3, -4);

        checkVector(p1.vectorTo(p3), -2, -1);
        checkVector(p3.vectorTo(p1), 2, 1);
    }

    @Test
    public void testDirectionTo() {
        // act/assert
        double invSqrt2 = 1.0 / Math.sqrt(2);

        Vector2D p1 = Vector2D.of(1, 1);
        Vector2D p2 = Vector2D.of(1, 5);
        Vector2D p3 = Vector2D.of(-2, -2);

        // act/assert
        checkVector(p1.directionTo(p2), 0, 1);
        checkVector(p2.directionTo(p1), 0, -1);

        checkVector(p1.directionTo(p3), -invSqrt2, -invSqrt2);
        checkVector(p3.directionTo(p1), invSqrt2, invSqrt2);
    }

    @Test
    public void testDirectionTo_illegalNorm() {
        // arrange
        Vector2D p = Vector2D.of(1, 2);

        // act/assert
        GeometryTestUtils.assertThrows(() -> Vector2D.ZERO.directionTo(Vector2D.ZERO),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> p.directionTo(p),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> p.directionTo(Vector2D.NaN),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.NEGATIVE_INFINITY.directionTo(p),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> p.directionTo(Vector2D.POSITIVE_INFINITY),
                IllegalNormException.class);
    }

    @Test
    public void testLerp() {
        // arrange
        Vector2D v1 = Vector2D.of(1, -5);
        Vector2D v2 = Vector2D.of(-4, 0);
        Vector2D v3 = Vector2D.of(10, -4);

        // act/assert
        checkVector(v1.lerp(v1, 0), 1, -5);
        checkVector(v1.lerp(v1, 1), 1, -5);

        checkVector(v1.lerp(v2, -0.25), 2.25, -6.25);
        checkVector(v1.lerp(v2, 0), 1, -5);
        checkVector(v1.lerp(v2, 0.25), -0.25, -3.75);
        checkVector(v1.lerp(v2, 0.5), -1.5, -2.5);
        checkVector(v1.lerp(v2, 0.75), -2.75, -1.25);
        checkVector(v1.lerp(v2, 1), -4, 0);
        checkVector(v1.lerp(v2, 1.25), -5.25, 1.25);

        checkVector(v1.lerp(v3, 0), 1, -5);
        checkVector(v1.lerp(v3, 0.25), 3.25, -4.75);
        checkVector(v1.lerp(v3, 0.5), 5.5, -4.5);
        checkVector(v1.lerp(v3, 0.75), 7.75, -4.25);
        checkVector(v1.lerp(v3, 1), 10, -4);
    }

    @Test
    public void testTransform() {
        // arrange
        AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(2)
                .translate(1, 2);

        Vector2D v1 = Vector2D.of(1, 2);
        Vector2D v2 = Vector2D.of(-4, -5);

        // act/assert
        checkVector(v1.transform(transform), 3, 6);
        checkVector(v2.transform(transform), -7, -8);
    }

    @Test
    public void testPrecisionEquals() {
        // arrange
        DoublePrecisionContext smallEps = new EpsilonDoublePrecisionContext(1e-6);
        DoublePrecisionContext largeEps = new EpsilonDoublePrecisionContext(1e-1);

        Vector2D vec = Vector2D.of(1, -2);

        // act/assert
        Assert.assertTrue(vec.equals(vec, smallEps));
        Assert.assertTrue(vec.equals(vec, largeEps));

        Assert.assertTrue(vec.equals(Vector2D.of(1.0000007, -2.0000009), smallEps));
        Assert.assertTrue(vec.equals(Vector2D.of(1.0000007, -2.0000009), largeEps));

        Assert.assertFalse(vec.equals(Vector2D.of(1.004, -2), smallEps));
        Assert.assertFalse(vec.equals(Vector2D.of(1, -2.004), smallEps));
        Assert.assertTrue(vec.equals(Vector2D.of(1.004, -2.004), largeEps));

        Assert.assertFalse(vec.equals(Vector2D.of(1, -3), smallEps));
        Assert.assertFalse(vec.equals(Vector2D.of(2, -2), smallEps));
        Assert.assertFalse(vec.equals(Vector2D.of(1, -3), largeEps));
        Assert.assertFalse(vec.equals(Vector2D.of(2, -2), largeEps));
    }

    @Test
    public void testIsZero() {
        // arrange
        DoublePrecisionContext smallEps = new EpsilonDoublePrecisionContext(1e-6);
        DoublePrecisionContext largeEps = new EpsilonDoublePrecisionContext(1e-1);

        // act/assert
        Assert.assertTrue(Vector2D.of(0.0, -0.0).isZero(smallEps));
        Assert.assertTrue(Vector2D.of(-0.0, 0.0).isZero(largeEps));

        Assert.assertTrue(Vector2D.of(-1e-7, 1e-7).isZero(smallEps));
        Assert.assertTrue(Vector2D.of(1e-7, 1e-7).isZero(largeEps));

        Assert.assertFalse(Vector2D.of(1e-2, 0.0).isZero(smallEps));
        Assert.assertFalse(Vector2D.of(0.0, 1e-2).isZero(smallEps));
        Assert.assertTrue(Vector2D.of(1e-2, -1e-2).isZero(largeEps));

        Assert.assertFalse(Vector2D.of(0.2, 0.0).isZero(smallEps));
        Assert.assertFalse(Vector2D.of(0.0, 0.2).isZero(smallEps));
        Assert.assertFalse(Vector2D.of(0.2, 0.2).isZero(smallEps));
        Assert.assertFalse(Vector2D.of(-0.2, 0.0).isZero(largeEps));
        Assert.assertFalse(Vector2D.of(0.0, -0.2).isZero(largeEps));
        Assert.assertFalse(Vector2D.of(-0.2, -0.2).isZero(largeEps));
    }

    @Test
    public void testHashCode() {
        // arrange
        Vector2D u = Vector2D.of(1, 1);
        Vector2D v = Vector2D.of(1 + 10 * Precision.EPSILON, 1 + 10 * Precision.EPSILON);
        Vector2D w = Vector2D.of(1, 1);

        // act/assert
        Assert.assertTrue(u.hashCode() != v.hashCode());
        Assert.assertEquals(u.hashCode(), w.hashCode());

        Assert.assertEquals(Vector2D.of(0, Double.NaN).hashCode(), Vector2D.NaN.hashCode());
        Assert.assertEquals(Vector2D.of(Double.NaN, 0).hashCode(), Vector2D.NaN.hashCode());
        Assert.assertEquals(Vector2D.of(0, Double.NaN).hashCode(), Vector2D.of(Double.NaN, 0).hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        Vector2D u1 = Vector2D.of(1, 2);
        Vector2D u2 = Vector2D.of(1, 2);

        // act/assert
        Assert.assertFalse(u1.equals(null));
        Assert.assertFalse(u1.equals(new Object()));

        Assert.assertTrue(u1.equals(u1));
        Assert.assertTrue(u1.equals(u2));

        Assert.assertFalse(u1.equals(Vector2D.of(-1, -2)));
        Assert.assertFalse(u1.equals(Vector2D.of(1 + 10 * Precision.EPSILON, 2)));
        Assert.assertFalse(u1.equals(Vector2D.of(1, 2 + 10 * Precision.EPSILON)));

        Assert.assertTrue(Vector2D.of(0, Double.NaN).equals(Vector2D.of(Double.NaN, 0)));

        Assert.assertTrue(Vector2D.of(0, Double.POSITIVE_INFINITY).equals(Vector2D.of(0, Double.POSITIVE_INFINITY)));
        Assert.assertFalse(Vector2D.of(Double.POSITIVE_INFINITY, 0).equals(Vector2D.of(0, Double.POSITIVE_INFINITY)));

        Assert.assertTrue(Vector2D.of(Double.NEGATIVE_INFINITY, 0).equals(Vector2D.of(Double.NEGATIVE_INFINITY, 0)));
        Assert.assertFalse(Vector2D.of(0, Double.NEGATIVE_INFINITY).equals(Vector2D.of(Double.NEGATIVE_INFINITY, 0)));
    }

    @Test
    public void testToString() {
        // arrange
        Vector2D v = Vector2D.of(1, 2);
        Pattern pattern = Pattern.compile("\\(1.{0,2}, 2.{0,2}\\)");

        // act
        String str = v.toString();

        // assert
        Assert.assertTrue("Expected string " + str + " to match regex " + pattern,
                    pattern.matcher(str).matches());
    }

    @Test
    public void testParse() {
        // act/assert
        checkVector(Vector2D.parse("(1, 2)"), 1, 2);
        checkVector(Vector2D.parse("(-1, -2)"), -1, -2);

        checkVector(Vector2D.parse("(0.01, -1e-3)"), 1e-2, -1e-3);

        checkVector(Vector2D.parse("(NaN, -Infinity)"), Double.NaN, Double.NEGATIVE_INFINITY);

        checkVector(Vector2D.parse(Vector2D.ZERO.toString()), 0, 0);
        checkVector(Vector2D.parse(Vector2D.MINUS_X.toString()), -1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        Vector2D.parse("abc");
    }

    @Test
    public void testOf() {
        // act/assert
        checkVector(Vector2D.of(0, 1), 0, 1);
        checkVector(Vector2D.of(-1, -2), -1, -2);
        checkVector(Vector2D.of(Math.PI, Double.NaN), Math.PI, Double.NaN);
        checkVector(Vector2D.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testOf_arrayArg() {
        // act/assert
        checkVector(Vector2D.of(new double[] { 0, 1 }), 0, 1);
        checkVector(Vector2D.of(new double[] { -1, -2 }), -1, -2);
        checkVector(Vector2D.of(new double[] { Math.PI, Double.NaN }), Math.PI, Double.NaN);
        checkVector(Vector2D.of(new double[] { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOf_arrayArg_invalidDimensions() {
        // act/assert
        Vector2D.of(new double[] {0.0 });
    }

    @Test
    public void testNormalize_static() {
        // arrange
        double invSqrt2 = 1.0 / Math.sqrt(2.0);

        // act/assert
        checkVector(Vector2D.Unit.from(2.0, -2.0), invSqrt2, -invSqrt2);
        checkVector(Vector2D.Unit.from(-4.0, 4.0), -invSqrt2, invSqrt2);
    }

    @Test
    public void testNormalize_static_illegalNorm() {
        GeometryTestUtils.assertThrows(() -> Vector2D.Unit.from(0.0, 0.0),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.Unit.from(Double.NaN, 1.0),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.Unit.from(1.0, Double.NEGATIVE_INFINITY),
                IllegalNormException.class);
        GeometryTestUtils.assertThrows(() -> Vector2D.Unit.from(1.0, Double.POSITIVE_INFINITY),
                IllegalNormException.class);
    }

    @Test
    public void testLinearCombination1() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 2);

        // act/assert
        checkVector(Vector2D.linearCombination(0, p1), 0, 0);

        checkVector(Vector2D.linearCombination(1, p1), 1, 2);
        checkVector(Vector2D.linearCombination(-1, p1), -1, -2);

        checkVector(Vector2D.linearCombination(0.5, p1), 0.5, 1);
        checkVector(Vector2D.linearCombination(-0.5, p1), -0.5, -1);
    }

    @Test
    public void testLinearCombination2() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(-3, -4);

        // act/assert
        checkVector(Vector2D.linearCombination(2, p1, -3, p2), 11, 16);
        checkVector(Vector2D.linearCombination(-3, p1, 2, p2), -9, -14);
    }

    @Test
    public void testLinearCombination3() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(-3, -4);
        Vector2D p3 = Vector2D.of(5, 6);

        // act/assert
        checkVector(Vector2D.linearCombination(2, p1, -3, p2, 4, p3), 31, 40);
        checkVector(Vector2D.linearCombination(-3, p1, 2, p2, -4, p3), -29, -38);
    }

    @Test
    public void testLinearCombination4() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 2);
        Vector2D p2 = Vector2D.of(-3, -4);
        Vector2D p3 = Vector2D.of(5, 6);
        Vector2D p4 = Vector2D.of(-7, -8);

        // act/assert
        checkVector(Vector2D.linearCombination(2, p1, -3, p2, 4, p3, -5, p4), 66, 80);
        checkVector(Vector2D.linearCombination(-3, p1, 2, p2, -4, p3, 5, p4), -64, -78);
    }

    @Test
    public void testUnitFactoryOptimization() {
        // An already normalized vector will avoid unnecessary creation.
        final Vector2D v = Vector2D.of(4, 5).normalize();
        Assert.assertSame(v, v.normalize());
    }

    private void checkVector(Vector2D v, double x, double y) {
        checkVector(v, x, y, EPS);
    }

    private void checkVector(Vector2D v, double x, double y, double eps) {
        Assert.assertEquals(x, v.getX(), eps);
        Assert.assertEquals(y, v.getY(), eps);
    }
}
