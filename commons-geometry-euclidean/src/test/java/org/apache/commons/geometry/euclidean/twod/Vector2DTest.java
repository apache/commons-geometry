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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Vector2DTest {

    private static final double EPS = Math.ulp(1d);

    @Test
    public void testConstants() {
        // act/assert
        checkVector(Vector2D.ZERO, 0, 0);
        checkVector(Vector2D.Unit.PLUS_X, 1, 0);
        checkVector(Vector2D.Unit.MINUS_X, -1, 0);
        checkVector(Vector2D.Unit.PLUS_Y, 0, 1);
        checkVector(Vector2D.Unit.MINUS_Y, 0, -1);
        checkVector(Vector2D.NaN, Double.NaN, Double.NaN);
        checkVector(Vector2D.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        checkVector(Vector2D.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testConstants_normalize() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.ZERO::normalize);
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.NaN::normalize);
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.POSITIVE_INFINITY::normalize);
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.NEGATIVE_INFINITY::normalize);

        Assertions.assertSame(Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_X.normalize());
        Assertions.assertSame(Vector2D.Unit.MINUS_X, Vector2D.Unit.MINUS_X.normalize());

        Assertions.assertSame(Vector2D.Unit.PLUS_Y, Vector2D.Unit.PLUS_Y.normalize());
        Assertions.assertSame(Vector2D.Unit.MINUS_Y, Vector2D.Unit.MINUS_Y.normalize());
    }

    @Test
    public void testCoordinateAscendingOrder() {
        // arrange
        final Comparator<Vector2D> cmp = Vector2D.COORDINATE_ASCENDING_ORDER;

        // act/assert
        Assertions.assertEquals(0, cmp.compare(Vector2D.of(1, 2), Vector2D.of(1, 2)));

        Assertions.assertEquals(-1, cmp.compare(Vector2D.of(0, 2), Vector2D.of(1, 2)));
        Assertions.assertEquals(-1, cmp.compare(Vector2D.of(1, 1), Vector2D.of(1, 2)));

        Assertions.assertEquals(1, cmp.compare(Vector2D.of(2, 2), Vector2D.of(1, 2)));
        Assertions.assertEquals(1, cmp.compare(Vector2D.of(1, 3), Vector2D.of(1, 2)));

        Assertions.assertEquals(-1, cmp.compare(Vector2D.of(1, 3), null));
        Assertions.assertEquals(1, cmp.compare(null, Vector2D.of(1, 2)));
        Assertions.assertEquals(0, cmp.compare(null, null));
    }

    @Test
    public void testCoordinates() {
        // arrange
        final Vector2D v = Vector2D.of(1, 2);

        // act/assert
        Assertions.assertEquals(1.0, v.getX(), EPS);
        Assertions.assertEquals(2.0, v.getY(), EPS);
    }

    @Test
    public void testToArray() {
        // arrange
        final Vector2D oneTwo = Vector2D.of(1, 2);

        // act
        final double[] array = oneTwo.toArray();

        // assert
        Assertions.assertEquals(2, array.length);
        Assertions.assertEquals(1.0, array[0], EPS);
        Assertions.assertEquals(2.0, array[1], EPS);
    }

    @Test
    public void testDimension() {
        // arrange
        final Vector2D v = Vector2D.of(1, 2);

        // act/assert
        Assertions.assertEquals(2, v.getDimension());
    }

    @Test
    public void testNaN() {
        // act/assert
        Assertions.assertTrue(Vector2D.of(0, Double.NaN).isNaN());
        Assertions.assertTrue(Vector2D.of(Double.NaN, 0).isNaN());

        Assertions.assertFalse(Vector2D.of(1, 1).isNaN());
        Assertions.assertFalse(Vector2D.of(1, Double.NEGATIVE_INFINITY).isNaN());
        Assertions.assertFalse(Vector2D.of(Double.POSITIVE_INFINITY, 1).isNaN());
    }

    @Test
    public void testInfinite() {
        // act/assert
        Assertions.assertTrue(Vector2D.of(0, Double.NEGATIVE_INFINITY).isInfinite());
        Assertions.assertTrue(Vector2D.of(Double.NEGATIVE_INFINITY, 0).isInfinite());
        Assertions.assertTrue(Vector2D.of(0, Double.POSITIVE_INFINITY).isInfinite());
        Assertions.assertTrue(Vector2D.of(Double.POSITIVE_INFINITY, 0).isInfinite());

        Assertions.assertFalse(Vector2D.of(1, 1).isInfinite());
        Assertions.assertFalse(Vector2D.of(0, Double.NaN).isInfinite());
        Assertions.assertFalse(Vector2D.of(Double.NEGATIVE_INFINITY, Double.NaN).isInfinite());
        Assertions.assertFalse(Vector2D.of(Double.NaN, Double.NEGATIVE_INFINITY).isInfinite());
        Assertions.assertFalse(Vector2D.of(Double.POSITIVE_INFINITY, Double.NaN).isInfinite());
        Assertions.assertFalse(Vector2D.of(Double.NaN, Double.POSITIVE_INFINITY).isInfinite());
    }

    @Test
    public void testFinite() {
        // act/assert
        Assertions.assertTrue(Vector2D.ZERO.isFinite());
        Assertions.assertTrue(Vector2D.of(1, 1).isFinite());

        Assertions.assertFalse(Vector2D.of(0, Double.NEGATIVE_INFINITY).isFinite());
        Assertions.assertFalse(Vector2D.of(Double.NEGATIVE_INFINITY, 0).isFinite());
        Assertions.assertFalse(Vector2D.of(0, Double.POSITIVE_INFINITY).isFinite());
        Assertions.assertFalse(Vector2D.of(Double.POSITIVE_INFINITY, 0).isFinite());

        Assertions.assertFalse(Vector2D.of(0, Double.NaN).isFinite());
        Assertions.assertFalse(Vector2D.of(Double.NEGATIVE_INFINITY, Double.NaN).isFinite());
        Assertions.assertFalse(Vector2D.of(Double.NaN, Double.NEGATIVE_INFINITY).isFinite());
        Assertions.assertFalse(Vector2D.of(Double.POSITIVE_INFINITY, Double.NaN).isFinite());
        Assertions.assertFalse(Vector2D.of(Double.NaN, Double.POSITIVE_INFINITY).isFinite());
    }

    @Test
    public void testGetZero() {
        // act/assert
        checkVector(Vector2D.of(1.0, 1.0).getZero(), 0, 0);
    }

    @Test
    public void testNorm() {
        // act/assert
        Assertions.assertEquals(0.0, Vector2D.of(0, 0).norm(), EPS);

        Assertions.assertEquals(5.0, Vector2D.of(3, 4).norm(), EPS);
        Assertions.assertEquals(5.0, Vector2D.of(3, -4).norm(), EPS);
        Assertions.assertEquals(5.0, Vector2D.of(-3, 4).norm(), EPS);
        Assertions.assertEquals(5.0, Vector2D.of(-3, -4).norm(), EPS);

        Assertions.assertEquals(Math.sqrt(5.0), Vector2D.of(-1, -2).norm(), EPS);
    }

    @Test
    public void testNorm_unitVectors() {
        // arrange
        final Vector2D v = Vector2D.of(2.0, 3.0).normalize();

        // act/assert
        Assertions.assertEquals(1.0, v.norm(), 0.0);
    }

    @Test
    public void testNormSq() {
        // act/assert
        Assertions.assertEquals(0.0, Vector2D.of(0, 0).normSq(), EPS);

        Assertions.assertEquals(25.0, Vector2D.of(3, 4).normSq(), EPS);
        Assertions.assertEquals(25.0, Vector2D.of(3, -4).normSq(), EPS);
        Assertions.assertEquals(25.0, Vector2D.of(-3, 4).normSq(), EPS);
        Assertions.assertEquals(25.0, Vector2D.of(-3, -4).normSq(), EPS);

        Assertions.assertEquals(5.0, Vector2D.of(-1, -2).normSq(), EPS);
    }

    @Test
    public void testNormSq_unitVectors() {
        // arrange
        final Vector2D v = Vector2D.of(2.0, 3.0).normalize();

        // act/assert
        Assertions.assertEquals(1.0, v.normSq(), 0.0);
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
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.ZERO.withNorm(2.0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.NaN.withNorm(2.0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.POSITIVE_INFINITY.withNorm(2.0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.NEGATIVE_INFINITY.withNorm(2.0));
    }

    @Test
    public void testWithNorm_unitVectors() {
        // arrange
        final double eps = 1e-14;
        final Vector2D v = Vector2D.of(2.0, -3.0).normalize();

        // act/assert
        checkVector(Vector2D.Unit.PLUS_X.withNorm(2.5), 2.5, 0.0);
        checkVector(Vector2D.Unit.MINUS_Y.withNorm(3.14), 0.0, -3.14);

        for (int i = -10; i <= 10; i++) {
            Assertions.assertEquals(Math.abs((double) i), v.withNorm(i).norm(), eps);
        }
    }

    @Test
    public void testAdd() {
        // arrange
        final Vector2D v1 = Vector2D.of(-1, 2);
        final Vector2D v2 = Vector2D.of(3, -4);
        final Vector2D v3 = Vector2D.of(5, 6);

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
        final Vector2D v1 = Vector2D.of(-1, 2);
        final Vector2D v2 = Vector2D.of(3, -4);
        final Vector2D v3 = Vector2D.of(5, 6);

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
        final Vector2D v1 = Vector2D.of(-1, 2);
        final Vector2D v2 = Vector2D.of(3, -4);
        final Vector2D v3 = Vector2D.of(5, 6);

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
        final Vector2D v1 = Vector2D.of(-1, 2);
        final Vector2D v2 = Vector2D.of(3, -4);
        final Vector2D v3 = Vector2D.of(5, 6);

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
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.ZERO::normalize);
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.NaN::normalize);
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.POSITIVE_INFINITY::normalize);
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.NEGATIVE_INFINITY::normalize);
    }

    @Test
    public void testNormalize_isIdempotent() {
        // arrange
        final double invSqrt2 = 1.0 / Math.sqrt(2);
        final Vector2D v = Vector2D.of(2, 2).normalize();

        // act/assert
        Assertions.assertSame(v, v.normalize());
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
        final Vector2D v1 = Vector2D.of(1.0, 1.0).normalize();
        final Vector2D v2 = Vector2D.of(-1.0, -2.0).normalize();
        final Vector2D v3 = Vector2D.of(2.0, -3.0).normalize();

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
        final Vector2D v1 = Vector2D.of(1, 1);
        final Vector2D v2 = Vector2D.of(4, 5);
        final Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assertions.assertEquals(0, v1.distance(v1), EPS);

        Assertions.assertEquals(5, v1.distance(v2), EPS);
        Assertions.assertEquals(5, v2.distance(v1), EPS);

        Assertions.assertEquals(Math.sqrt(5), v1.distance(v3), EPS);
        Assertions.assertEquals(Math.sqrt(5), v3.distance(v1), EPS);
    }

    @Test
    public void testDistanceSq() {
        // arrange
        final Vector2D v1 = Vector2D.of(1, 1);
        final Vector2D v2 = Vector2D.of(4, 5);
        final Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assertions.assertEquals(0, v1.distanceSq(v1), EPS);

        Assertions.assertEquals(25, v1.distanceSq(v2), EPS);
        Assertions.assertEquals(25, v2.distanceSq(v1), EPS);

        Assertions.assertEquals(5, v1.distanceSq(v3), EPS);
        Assertions.assertEquals(5, v3.distanceSq(v1), EPS);
    }

    @Test
    public void testDotProduct() {
        // arrange
        final Vector2D v1 = Vector2D.of(1, 1);
        final Vector2D v2 = Vector2D.of(4, 5);
        final Vector2D v3 = Vector2D.of(-1, 0);

        // act/assert
        Assertions.assertEquals(2, v1.dot(v1), EPS);
        Assertions.assertEquals(41, v2.dot(v2), EPS);
        Assertions.assertEquals(1, v3.dot(v3), EPS);

        Assertions.assertEquals(9, v1.dot(v2), EPS);
        Assertions.assertEquals(9, v2.dot(v1), EPS);

        Assertions.assertEquals(-1, v1.dot(v3), EPS);
        Assertions.assertEquals(-1, v3.dot(v1), EPS);

        Assertions.assertEquals(1, Vector2D.Unit.PLUS_X.dot(Vector2D.Unit.PLUS_X), EPS);
        Assertions.assertEquals(0, Vector2D.Unit.PLUS_X.dot(Vector2D.Unit.PLUS_Y), EPS);
        Assertions.assertEquals(-1, Vector2D.Unit.PLUS_X.dot(Vector2D.Unit.MINUS_X), EPS);
        Assertions.assertEquals(0, Vector2D.Unit.PLUS_X.dot(Vector2D.Unit.MINUS_Y), EPS);
    }

    @Test
    public void testOrthogonal() {
        // arrange
        final double invSqrt2 = 1.0 / Math.sqrt(2.0);

        // act/assert
        checkVector(Vector2D.of(3, 0).orthogonal(), 0.0, 1.0);
        checkVector(Vector2D.of(1.0, 1.0).orthogonal(), -invSqrt2, invSqrt2);

        checkVector(Vector2D.of(0, 2).orthogonal(), -1.0, 0.0);
        checkVector(Vector2D.of(-1.0, 1.0).orthogonal(), -invSqrt2, -invSqrt2);

        checkVector(Vector2D.Unit.MINUS_X.orthogonal(), 0.0, -1.0);
        checkVector(Vector2D.of(-1.0, -1.0).orthogonal(), invSqrt2, -invSqrt2);

        checkVector(Vector2D.Unit.MINUS_Y.orthogonal(), 1.0, 0.0);
        checkVector(Vector2D.of(1.0, -1.0).orthogonal(), invSqrt2, invSqrt2);
    }

    @Test
    public void testOrthogonal_fullCircle() {
        for (double az = 0.0; az <= PlaneAngleRadians.TWO_PI; az += 0.25) {
            // arrange
            final Vector2D v = PolarCoordinates.toCartesian(Math.PI, az);

            //act
            final Vector2D ortho = v.orthogonal();

            // assert
            Assertions.assertEquals(1.0, ortho.norm(), EPS);
            Assertions.assertEquals(0.0, v.dot(ortho), EPS);
        }
    }

    @Test
    public void testOrthogonal_illegalNorm() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.ZERO::orthogonal);
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.NaN::orthogonal);
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.POSITIVE_INFINITY::orthogonal);
        Assertions.assertThrows(IllegalArgumentException.class, Vector2D.NEGATIVE_INFINITY::orthogonal);
    }

    @Test
    public void testOrthogonal_givenDirection() {
        // arrange
        final double invSqrt2 = 1.0 / Math.sqrt(2.0);

        // act/assert
        checkVector(Vector2D.Unit.PLUS_X.orthogonal(Vector2D.of(-1.0, 0.1)), 0.0, 1.0);
        checkVector(Vector2D.Unit.PLUS_Y.orthogonal(Vector2D.of(2.0, 2.0)), 1.0, 0.0);

        checkVector(Vector2D.of(2.9, 2.9).orthogonal(Vector2D.of(1.0, 0.22)), invSqrt2, -invSqrt2);
        checkVector(Vector2D.of(2.9, 2.9).orthogonal(Vector2D.of(0.22, 1.0)), -invSqrt2, invSqrt2);
    }

    @Test
    public void testOrthogonal_givenDirection_illegalNorm() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.ZERO.orthogonal(Vector2D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.NaN.orthogonal(Vector2D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.POSITIVE_INFINITY.orthogonal(Vector2D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.NEGATIVE_INFINITY.orthogonal(Vector2D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.Unit.PLUS_X.orthogonal(Vector2D.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.Unit.PLUS_X.orthogonal(Vector2D.NaN));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.Unit.PLUS_X.orthogonal(Vector2D.POSITIVE_INFINITY));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.Unit.PLUS_X.orthogonal(Vector2D.NEGATIVE_INFINITY));
    }

    @Test
    public void testOrthogonal_givenDirection_directionIsCollinear() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.Unit.PLUS_X.orthogonal(Vector2D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.Unit.PLUS_X.orthogonal(Vector2D.Unit.MINUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.of(1.0, 1.0).orthogonal(Vector2D.of(2.0, 2.0)));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.of(-1.01, -1.01).orthogonal(Vector2D.of(20.1, 20.1)));
    }

    @Test
    public void testAngle() {
        // act/assert
        Assertions.assertEquals(0, Vector2D.Unit.PLUS_X.angle(Vector2D.Unit.PLUS_X), EPS);

        Assertions.assertEquals(PlaneAngleRadians.PI, Vector2D.Unit.PLUS_X.angle(Vector2D.Unit.MINUS_X), EPS);
        Assertions.assertEquals(PlaneAngleRadians.PI_OVER_TWO, Vector2D.Unit.PLUS_X.angle(Vector2D.Unit.PLUS_Y), EPS);
        Assertions.assertEquals(PlaneAngleRadians.PI_OVER_TWO, Vector2D.Unit.PLUS_X.angle(Vector2D.Unit.MINUS_Y), EPS);

        Assertions.assertEquals(PlaneAngleRadians.PI / 4, Vector2D.of(1, 1).angle(Vector2D.of(1, 0)), EPS);
        Assertions.assertEquals(PlaneAngleRadians.PI / 4, Vector2D.of(1, 0).angle(Vector2D.of(1, 1)), EPS);

        Assertions.assertEquals(0.004999958333958323, Vector2D.of(20.0, 0.0).angle(Vector2D.of(20.0, 0.1)), EPS);
    }


    @Test
    public void testAngle_illegalNorm() {
        // arrange
        final Vector2D v = Vector2D.of(1.0, 1.0);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.ZERO.angle(v));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.NaN.angle(v));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.POSITIVE_INFINITY.angle(v));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.NEGATIVE_INFINITY.angle(v));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.angle(Vector2D.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.angle(Vector2D.NaN));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.angle(Vector2D.POSITIVE_INFINITY));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.angle(Vector2D.NEGATIVE_INFINITY));
    }

    @Test
    public void testSignedArea() {
        // arrange
        final double eps = 1e-10;

        final Vector2D a = Vector2D.Unit.PLUS_X;
        final Vector2D b = Vector2D.Unit.PLUS_Y;
        final Vector2D c = Vector2D.of(1, 1).withNorm(2.0);
        final Vector2D d = Vector2D.of(-1, 1).withNorm(3.0);

        // act/assert
        Assertions.assertEquals(1.0, a.signedArea(b), eps);
        Assertions.assertEquals(-1.0, b.signedArea(a), eps);

        final double xAxisAndCArea = 2 * Math.cos(0.25 * PlaneAngleRadians.PI);
        Assertions.assertEquals(xAxisAndCArea, a.signedArea(c), eps);
        Assertions.assertEquals(-xAxisAndCArea, c.signedArea(a), eps);

        final double xAxisAndDArea = 3 * Math.cos(0.25 * PlaneAngleRadians.PI);
        Assertions.assertEquals(xAxisAndDArea, a.signedArea(d), eps);
        Assertions.assertEquals(-xAxisAndDArea, d.signedArea(a), eps);

        Assertions.assertEquals(6.0, c.signedArea(d), eps);
        Assertions.assertEquals(-6.0, d.signedArea(c), eps);
    }

    @Test
    public void testSignedArea_collinear() {
        // arrange
        final Vector2D a = Vector2D.Unit.PLUS_X;
        final Vector2D b = Vector2D.Unit.PLUS_Y;
        final Vector2D c = Vector2D.of(-3, 8);

        // act/assert
        Assertions.assertEquals(0.0, a.signedArea(a), EPS);
        Assertions.assertEquals(0.0, b.signedArea(b), EPS);
        Assertions.assertEquals(0.0, c.signedArea(c), EPS);

        Assertions.assertEquals(0.0, a.signedArea(a.multiply(100.0)), EPS);
        Assertions.assertEquals(0.0, b.signedArea(b.negate()), EPS);
        Assertions.assertEquals(0.0, c.signedArea(c.multiply(-0.03)), EPS);
    }

    @Test
    public void testProject() {
        // arrange
        final Vector2D v1 = Vector2D.of(3.0, 4.0);
        final Vector2D v2 = Vector2D.of(1.0, 4.0);

        // act/assert
        checkVector(Vector2D.ZERO.project(v1), 0.0, 0.0);

        checkVector(v1.project(v1), 3.0, 4.0);
        checkVector(v1.project(v1.negate()), 3.0, 4.0);

        checkVector(v1.project(Vector2D.Unit.PLUS_X), 3.0, 0.0);
        checkVector(v1.project(Vector2D.Unit.MINUS_X), 3.0, 0.0);

        checkVector(v1.project(Vector2D.Unit.PLUS_Y), 0.0, 4.0);
        checkVector(v1.project(Vector2D.Unit.MINUS_Y), 0.0, 4.0);

        checkVector(v2.project(v1), (19.0 / 25.0) * 3.0, (19.0 / 25.0) * 4.0);
    }

    @Test
    public void testProject_baseHasIllegalNorm() {
        // arrange
        final Vector2D v = Vector2D.of(1.0, 1.0);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.project(Vector2D.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.project(Vector2D.NaN));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.project(Vector2D.POSITIVE_INFINITY));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.project(Vector2D.NEGATIVE_INFINITY));
    }

    @Test
    public void testReject() {
        // arrange
        final Vector2D v1 = Vector2D.of(3.0, 4.0);
        final Vector2D v2 = Vector2D.of(1.0, 4.0);

        // act/assert
        checkVector(Vector2D.ZERO.reject(v1), 0.0, 0.0);

        checkVector(v1.reject(v1), 0.0, 0.0);
        checkVector(v1.reject(v1.negate()), 0.0, 0.0);

        checkVector(v1.reject(Vector2D.Unit.PLUS_X), 0.0, 4.0);
        checkVector(v1.reject(Vector2D.Unit.MINUS_X), 0.0, 4.0);

        checkVector(v1.reject(Vector2D.Unit.PLUS_Y), 3.0, 0.0);
        checkVector(v1.reject(Vector2D.Unit.MINUS_Y), 3.0, 0.0);

        checkVector(v2.reject(v1), -32.0 / 25.0, (6.0 / 25.0) * 4.0);
    }

    @Test
    public void testReject_baseHasIllegalNorm() {
        // arrange
        final Vector2D v = Vector2D.of(1.0, 1.0);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.reject(Vector2D.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.reject(Vector2D.NaN));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.reject(Vector2D.POSITIVE_INFINITY));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.reject(Vector2D.NEGATIVE_INFINITY));
    }

    @Test
    public void testProjectAndReject_areComplementary() {
        // arrange
        final double eps = 1e-12;

        // act/assert
        checkProjectAndRejectFullCircle(Vector2D.of(1.0, 0.0), 1.0, eps);
        checkProjectAndRejectFullCircle(Vector2D.of(0.0, 1.0), 2.0, eps);
        checkProjectAndRejectFullCircle(Vector2D.of(1.0, 1.0), 3.0, eps);

        checkProjectAndRejectFullCircle(Vector2D.of(-2.0, 0.0), 4.0, eps);
        checkProjectAndRejectFullCircle(Vector2D.of(0.0, -2.0), 5.0, eps);
        checkProjectAndRejectFullCircle(Vector2D.of(-2.0, -2.0), 6.0, eps);
    }

    private void checkProjectAndRejectFullCircle(final Vector2D vec, final double baseMag, final double eps) {
        for (double theta = 0.0; theta <= PlaneAngleRadians.TWO_PI; theta += 0.5) {
            final Vector2D base = PolarCoordinates.toCartesian(baseMag, theta);

            final Vector2D proj = vec.project(base);
            final Vector2D rej = vec.reject(base);

            // ensure that the projection and rejection sum to the original vector
            EuclideanTestUtils.assertCoordinatesEqual(vec, proj.add(rej), eps);

            final double angle = base.angle(vec);

            // check the angle between the projection and the base; this will
            // be undefined when the angle between the original vector and the
            // base is pi/2 (which means that the projection is the zero vector)
            if (angle < PlaneAngleRadians.PI_OVER_TWO) {
                Assertions.assertEquals(0.0, proj.angle(base), eps);
            } else if (angle > PlaneAngleRadians.PI_OVER_TWO) {
                Assertions.assertEquals(PlaneAngleRadians.PI, proj.angle(base), eps);
            }

            // check the angle between the rejection and the base; this should
            // always be pi/2 except for when the angle between the original vector
            // and the base is 0 or pi, in which case the rejection is the zero vector.
            if (angle > 0.0 && angle < PlaneAngleRadians.PI) {
                Assertions.assertEquals(PlaneAngleRadians.PI_OVER_TWO, rej.angle(base), eps);
            }
        }
    }

    @Test
    public void testVectorTo() {
        // arrange
        final Vector2D p1 = Vector2D.of(1, 1);
        final Vector2D p2 = Vector2D.of(4, 5);
        final Vector2D p3 = Vector2D.of(-1, 0);

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
        final double invSqrt2 = 1.0 / Math.sqrt(2);

        final Vector2D p1 = Vector2D.of(1, 1);
        final Vector2D p2 = Vector2D.of(1, 5);
        final Vector2D p3 = Vector2D.of(-2, -2);

        // act/assert
        checkVector(p1.directionTo(p2), 0, 1);
        checkVector(p2.directionTo(p1), 0, -1);

        checkVector(p1.directionTo(p3), -invSqrt2, -invSqrt2);
        checkVector(p3.directionTo(p1), invSqrt2, invSqrt2);
    }

    @Test
    public void testDirectionTo_illegalNorm() {
        // arrange
        final Vector2D p = Vector2D.of(1, 2);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.ZERO.directionTo(Vector2D.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> p.directionTo(p));
        Assertions.assertThrows(IllegalArgumentException.class, () -> p.directionTo(Vector2D.NaN));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.NEGATIVE_INFINITY.directionTo(p));
        Assertions.assertThrows(IllegalArgumentException.class, () -> p.directionTo(Vector2D.POSITIVE_INFINITY));
    }

    @Test
    public void testLerp() {
        // arrange
        final Vector2D v1 = Vector2D.of(1, -5);
        final Vector2D v2 = Vector2D.of(-4, 0);
        final Vector2D v3 = Vector2D.of(10, -4);

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
        final AffineTransformMatrix2D transform = AffineTransformMatrix2D.identity()
                .scale(2)
                .translate(1, 2);

        final Vector2D v1 = Vector2D.of(1, 2);
        final Vector2D v2 = Vector2D.of(-4, -5);

        // act/assert
        checkVector(v1.transform(transform), 3, 6);
        checkVector(v2.transform(transform), -7, -8);
    }

    @Test
    public void testPrecisionEquals() {
        // arrange
        final DoublePrecisionContext smallEps = new EpsilonDoublePrecisionContext(1e-6);
        final DoublePrecisionContext largeEps = new EpsilonDoublePrecisionContext(1e-1);

        final Vector2D vec = Vector2D.of(1, -2);

        // act/assert
        Assertions.assertTrue(vec.eq(vec, smallEps));
        Assertions.assertTrue(vec.eq(vec, largeEps));

        Assertions.assertTrue(vec.eq(Vector2D.of(1.0000007, -2.0000009), smallEps));
        Assertions.assertTrue(vec.eq(Vector2D.of(1.0000007, -2.0000009), largeEps));

        Assertions.assertFalse(vec.eq(Vector2D.of(1.004, -2), smallEps));
        Assertions.assertFalse(vec.eq(Vector2D.of(1, -2.004), smallEps));
        Assertions.assertTrue(vec.eq(Vector2D.of(1.004, -2.004), largeEps));

        Assertions.assertFalse(vec.eq(Vector2D.of(1, -3), smallEps));
        Assertions.assertFalse(vec.eq(Vector2D.of(2, -2), smallEps));
        Assertions.assertFalse(vec.eq(Vector2D.of(1, -3), largeEps));
        Assertions.assertFalse(vec.eq(Vector2D.of(2, -2), largeEps));
    }

    @Test
    public void testIsZero() {
        // arrange
        final DoublePrecisionContext smallEps = new EpsilonDoublePrecisionContext(1e-6);
        final DoublePrecisionContext largeEps = new EpsilonDoublePrecisionContext(1e-1);

        // act/assert
        Assertions.assertTrue(Vector2D.of(0.0, -0.0).isZero(smallEps));
        Assertions.assertTrue(Vector2D.of(-0.0, 0.0).isZero(largeEps));

        Assertions.assertTrue(Vector2D.of(-1e-7, 1e-7).isZero(smallEps));
        Assertions.assertTrue(Vector2D.of(1e-7, 1e-7).isZero(largeEps));

        Assertions.assertFalse(Vector2D.of(1e-2, 0.0).isZero(smallEps));
        Assertions.assertFalse(Vector2D.of(0.0, 1e-2).isZero(smallEps));
        Assertions.assertTrue(Vector2D.of(1e-2, -1e-2).isZero(largeEps));

        Assertions.assertFalse(Vector2D.of(0.2, 0.0).isZero(smallEps));
        Assertions.assertFalse(Vector2D.of(0.0, 0.2).isZero(smallEps));
        Assertions.assertFalse(Vector2D.of(0.2, 0.2).isZero(smallEps));
        Assertions.assertFalse(Vector2D.of(-0.2, 0.0).isZero(largeEps));
        Assertions.assertFalse(Vector2D.of(0.0, -0.2).isZero(largeEps));
        Assertions.assertFalse(Vector2D.of(-0.2, -0.2).isZero(largeEps));
    }

    @Test
    public void testHashCode() {
        // arrange
        final Vector2D u = Vector2D.of(1, 1);
        final Vector2D v = Vector2D.of(1 + 10 * Precision.EPSILON, 1 + 10 * Precision.EPSILON);
        final Vector2D w = Vector2D.of(1, 1);

        // act/assert
        Assertions.assertTrue(u.hashCode() != v.hashCode());
        Assertions.assertEquals(u.hashCode(), w.hashCode());

        Assertions.assertEquals(Vector2D.of(0, Double.NaN).hashCode(), Vector2D.NaN.hashCode());
        Assertions.assertEquals(Vector2D.of(Double.NaN, 0).hashCode(), Vector2D.NaN.hashCode());
        Assertions.assertEquals(Vector2D.of(0, Double.NaN).hashCode(), Vector2D.of(Double.NaN, 0).hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final Vector2D u1 = Vector2D.of(1, 2);
        final Vector2D u2 = Vector2D.of(1, 2);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(u1);
        Assertions.assertEquals(u1, u2);

        Assertions.assertNotEquals(u1, Vector2D.of(-1, -2));
        Assertions.assertNotEquals(u1, Vector2D.of(1 + 10 * Precision.EPSILON, 2));
        Assertions.assertNotEquals(u1, Vector2D.of(1, 2 + 10 * Precision.EPSILON));

        Assertions.assertEquals(Vector2D.of(0, Double.NaN), Vector2D.of(Double.NaN, 0));

        Assertions.assertEquals(Vector2D.of(0, Double.POSITIVE_INFINITY), Vector2D.of(0, Double.POSITIVE_INFINITY));
        Assertions.assertNotEquals(Vector2D.of(Double.POSITIVE_INFINITY, 0), Vector2D.of(0, Double.POSITIVE_INFINITY));

        Assertions.assertEquals(Vector2D.of(Double.NEGATIVE_INFINITY, 0), Vector2D.of(Double.NEGATIVE_INFINITY, 0));
        Assertions.assertNotEquals(Vector2D.of(0, Double.NEGATIVE_INFINITY), Vector2D.of(Double.NEGATIVE_INFINITY, 0));
    }

    @Test
    public void testEqualsAndHashCode_signedZeroConsistency() {
        // arrange
        final Vector2D a = Vector2D.of(0.0, 0.0);
        final Vector2D b = Vector2D.of(-0.0, -0.0);
        final Vector2D c = Vector2D.of(0.0, 0.0);
        final Vector2D d = Vector2D.of(-0.0, -0.0);

        // act/assert
        Assertions.assertNotEquals(a, b);

        Assertions.assertEquals(a, c);
        Assertions.assertEquals(a.hashCode(), c.hashCode());

        Assertions.assertEquals(b, d);
        Assertions.assertEquals(b.hashCode(), d.hashCode());
    }

    @Test
    public void testToString() {
        // arrange
        final Vector2D v = Vector2D.of(1, 2);
        final Pattern pattern = Pattern.compile("\\(1.{0,2}, 2.{0,2}\\)");

        // act
        final String str = v.toString();

        // assert
        Assertions.assertTrue(pattern.matcher(str).matches(), "Expected string " + str + " to match regex " + pattern);
    }

    @Test
    public void testParse() {
        // act/assert
        checkVector(Vector2D.parse("(1, 2)"), 1, 2);
        checkVector(Vector2D.parse("(-1, -2)"), -1, -2);

        checkVector(Vector2D.parse("(0.01, -1e-3)"), 1e-2, -1e-3);

        checkVector(Vector2D.parse("(NaN, -Infinity)"), Double.NaN, Double.NEGATIVE_INFINITY);

        checkVector(Vector2D.parse(Vector2D.ZERO.toString()), 0, 0);
        checkVector(Vector2D.parse(Vector2D.Unit.MINUS_X.toString()), -1, 0);
    }

    @Test
    public void testParse_failure() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.parse("abc"));
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
        checkVector(Vector2D.of(new double[] {0, 1}), 0, 1);
        checkVector(Vector2D.of(new double[] {-1, -2}), -1, -2);
        checkVector(Vector2D.of(new double[] {Math.PI, Double.NaN}), Math.PI, Double.NaN);
        checkVector(Vector2D.of(new double[] {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}), Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test
    public void testOf_arrayArg_invalidDimensions() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class,     () -> Vector2D.of(new double[] {0.0}));
    }

    @Test
    public void testUnitFrom_coordinates() {
        // arrange
        final double invSqrt2 = 1.0 / Math.sqrt(2.0);

        // act/assert
        checkVector(Vector2D.Unit.from(2.0, -2.0), invSqrt2, -invSqrt2);
        checkVector(Vector2D.Unit.from(-4.0, 4.0), -invSqrt2, invSqrt2);
    }

    @Test
    public void testUnitFrom_vector() {
        // arrange
        final double invSqrt2 = 1.0 / Math.sqrt(2.0);
        final Vector2D vec = Vector2D.of(2.0, -2.0);
        final Vector2D.Unit unitVec = Vector2D.Unit.from(2.0, -2.0);

        // act/assert
        checkVector(Vector2D.Unit.from(vec), invSqrt2, -invSqrt2);
        Assertions.assertSame(unitVec, Vector2D.Unit.from(unitVec));
    }

    @Test
    public void testUnitFrom_illegalNorm() {

        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.Unit.from(0.0, 0.0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.Unit.from(Double.NaN, 1.0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.Unit.from(1.0, Double.NEGATIVE_INFINITY));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector2D.Unit.from(1.0, Double.POSITIVE_INFINITY));
    }

    @Test
    public void testMax() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-100, 1),
                Vector2D.max(Collections.singletonList(Vector2D.of(-100, 1))), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 1),
                Vector2D.max(Arrays.asList(Vector2D.of(-100, 1), Vector2D.of(0, 1))), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-1, 0),
                Vector2D.max(Vector2D.of(-2, 0), Vector2D.of(-1, -5), Vector2D.of(-10, -10)), EPS);
    }

    @Test
    public void testMax_noPointsGiven() {
        // arrange
        final String msg = "Cannot compute vector max: no vectors given";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Vector2D.max(new ArrayList<>());
        }, IllegalArgumentException.class, msg);
    }

    @Test
    public void testMin() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-100, 1),
                Vector2D.min(Collections.singletonList(Vector2D.of(-100, 1))), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-100, 1),
                Vector2D.min(Arrays.asList(Vector2D.of(-100, 1), Vector2D.of(0, 1))), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-10, -10),
                Vector2D.min(Vector2D.of(-2, 0), Vector2D.of(-1, -5), Vector2D.of(-10, -10)), EPS);
    }

    @Test
    public void testMin_noPointsGiven() {
        // arrange
        final String msg = "Cannot compute vector min: no vectors given";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Vector2D.min(new ArrayList<>());
        }, IllegalArgumentException.class, msg);
    }

    @Test
    public void testCentroid() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2),
                Vector2D.centroid(Vector2D.of(1, 2)), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2.5, 3.5),
                Vector2D.centroid(Vector2D.of(1, 2), Vector2D.of(2, 3),
                        Vector2D.of(3, 4), Vector2D.of(4, 5)), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2),
                Vector2D.centroid(Collections.singletonList(Vector2D.of(1, 2))), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 1),
                Vector2D.centroid(Arrays.asList(Vector2D.of(1, 2), Vector2D.of(1, 2),
                        Vector2D.ZERO, Vector2D.ZERO)), EPS);
    }

    @Test
    public void testCentroid_noPointsGiven() {
        // arrange
        final String msg = "Cannot compute centroid: no points given";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Vector2D.centroid(new ArrayList<>());
        }, IllegalArgumentException.class, msg);
    }

    @Test
    public void testLinearCombination1() {
        // arrange
        final Vector2D p1 = Vector2D.of(1, 2);

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
        final Vector2D p1 = Vector2D.of(1, 2);
        final Vector2D p2 = Vector2D.of(-3, -4);

        // act/assert
        checkVector(Vector2D.linearCombination(2, p1, -3, p2), 11, 16);
        checkVector(Vector2D.linearCombination(-3, p1, 2, p2), -9, -14);
    }

    @Test
    public void testLinearCombination3() {
        // arrange
        final Vector2D p1 = Vector2D.of(1, 2);
        final Vector2D p2 = Vector2D.of(-3, -4);
        final Vector2D p3 = Vector2D.of(5, 6);

        // act/assert
        checkVector(Vector2D.linearCombination(2, p1, -3, p2, 4, p3), 31, 40);
        checkVector(Vector2D.linearCombination(-3, p1, 2, p2, -4, p3), -29, -38);
    }

    @Test
    public void testLinearCombination4() {
        // arrange
        final Vector2D p1 = Vector2D.of(1, 2);
        final Vector2D p2 = Vector2D.of(-3, -4);
        final Vector2D p3 = Vector2D.of(5, 6);
        final Vector2D p4 = Vector2D.of(-7, -8);

        // act/assert
        checkVector(Vector2D.linearCombination(2, p1, -3, p2, 4, p3, -5, p4), 66, 80);
        checkVector(Vector2D.linearCombination(-3, p1, 2, p2, -4, p3, 5, p4), -64, -78);
    }

    @Test
    public void testUnitFactoryOptimization() {
        // An already normalized vector will avoid unnecessary creation.
        final Vector2D v = Vector2D.of(4, 5).normalize();
        Assertions.assertSame(v, v.normalize());
    }

    private void checkVector(final Vector2D v, final double x, final double y) {
        checkVector(v, x, y, EPS);
    }

    private void checkVector(final Vector2D v, final double x, final double y, final double eps) {
        Assertions.assertEquals(x, v.getX(), eps);
        Assertions.assertEquals(y, v.getY(), eps);
    }
}
