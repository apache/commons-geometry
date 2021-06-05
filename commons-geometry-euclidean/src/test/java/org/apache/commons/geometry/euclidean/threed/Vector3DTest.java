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
import java.util.Comparator;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Vector3DTest {

    private static final double EPS = 1e-15;

    @Test
    void testConstants() {
        // act/assert
        checkVector(Vector3D.ZERO, 0, 0, 0);

        checkVector(Vector3D.Unit.PLUS_X, 1, 0, 0);
        checkVector(Vector3D.Unit.MINUS_X, -1, 0, 0);

        checkVector(Vector3D.Unit.PLUS_Y, 0, 1, 0);
        checkVector(Vector3D.Unit.MINUS_Y, 0, -1, 0);

        checkVector(Vector3D.Unit.PLUS_Z, 0, 0, 1);
        checkVector(Vector3D.Unit.MINUS_Z, 0, 0, -1);

        checkVector(Vector3D.NaN, Double.NaN, Double.NaN, Double.NaN);
        checkVector(Vector3D.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        checkVector(Vector3D.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    @Test
    void testConstants_normalize() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, Vector3D.ZERO::normalize);
        Assertions.assertThrows(IllegalArgumentException.class, Vector3D.NaN::normalize);
        Assertions.assertThrows(IllegalArgumentException.class, Vector3D.POSITIVE_INFINITY::normalize);
        Assertions.assertThrows(IllegalArgumentException.class, Vector3D.NEGATIVE_INFINITY::normalize);

        Assertions.assertSame(Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_X.normalize());
        Assertions.assertSame(Vector3D.Unit.MINUS_X, Vector3D.Unit.MINUS_X.normalize());

        Assertions.assertSame(Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_Y.normalize());
        Assertions.assertSame(Vector3D.Unit.MINUS_Y, Vector3D.Unit.MINUS_Y.normalize());

        Assertions.assertSame(Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_Z.normalize());
        Assertions.assertSame(Vector3D.Unit.MINUS_Z, Vector3D.Unit.MINUS_Z.normalize());
    }

    @Test
    void testCoordinateAscendingOrder() {
        // arrange
        final Comparator<Vector3D> cmp = Vector3D.COORDINATE_ASCENDING_ORDER;

        // act/assert
        Assertions.assertEquals(0, cmp.compare(Vector3D.of(1, 2, 3), Vector3D.of(1, 2, 3)));

        Assertions.assertEquals(-1, cmp.compare(Vector3D.of(0, 2, 3), Vector3D.of(1, 2, 3)));
        Assertions.assertEquals(-1, cmp.compare(Vector3D.of(1, 1, 3), Vector3D.of(1, 2, 3)));
        Assertions.assertEquals(-1, cmp.compare(Vector3D.of(1, 2, 2), Vector3D.of(1, 2, 3)));

        Assertions.assertEquals(1, cmp.compare(Vector3D.of(2, 2, 3), Vector3D.of(1, 2, 3)));
        Assertions.assertEquals(1, cmp.compare(Vector3D.of(1, 3, 3), Vector3D.of(1, 2, 3)));
        Assertions.assertEquals(1, cmp.compare(Vector3D.of(1, 2, 4), Vector3D.of(1, 2, 3)));

        Assertions.assertEquals(-1, cmp.compare(Vector3D.of(1, 2, 3), null));
        Assertions.assertEquals(1, cmp.compare(null, Vector3D.of(1, 2, 3)));
        Assertions.assertEquals(0, cmp.compare(null, null));
    }

    @Test
    void testCoordinates() {
        // arrange
        final Vector3D c = Vector3D.of(1, 2, 3);

        // act/assert
        Assertions.assertEquals(1.0, c.getX(), EPS);
        Assertions.assertEquals(2.0, c.getY(), EPS);
        Assertions.assertEquals(3.0, c.getZ(), EPS);
    }

    @Test
    void testToArray() {
        // arrange
        final Vector3D c = Vector3D.of(1, 2, 3);

        // act
        final double[] arr = c.toArray();

        // assert
        Assertions.assertEquals(3, arr.length);
        Assertions.assertEquals(1.0, arr[0], EPS);
        Assertions.assertEquals(2.0, arr[1], EPS);
        Assertions.assertEquals(3.0, arr[2], EPS);
    }

    @Test
    void testDimension() {
        // arrange
        final Vector3D c = Vector3D.of(1, 2, 3);

        // act/assert
        Assertions.assertEquals(3, c.getDimension());
    }

    @Test
    void testNaN() {
        // act/assert
        Assertions.assertTrue(Vector3D.of(0, 0, Double.NaN).isNaN());
        Assertions.assertTrue(Vector3D.of(0, Double.NaN, 0).isNaN());
        Assertions.assertTrue(Vector3D.of(Double.NaN, 0, 0).isNaN());

        Assertions.assertFalse(Vector3D.of(1, 1, 1).isNaN());
        Assertions.assertFalse(Vector3D.of(1, 1, Double.NEGATIVE_INFINITY).isNaN());
        Assertions.assertFalse(Vector3D.of(1, Double.POSITIVE_INFINITY, 1).isNaN());
        Assertions.assertFalse(Vector3D.of(Double.NEGATIVE_INFINITY, 1, 1).isNaN());
    }

    @Test
    void testInfinite() {
        // act/assert
        Assertions.assertTrue(Vector3D.of(0, 0, Double.NEGATIVE_INFINITY).isInfinite());
        Assertions.assertTrue(Vector3D.of(0, Double.NEGATIVE_INFINITY, 0).isInfinite());
        Assertions.assertTrue(Vector3D.of(Double.NEGATIVE_INFINITY, 0, 0).isInfinite());
        Assertions.assertTrue(Vector3D.of(0, 0, Double.POSITIVE_INFINITY).isInfinite());
        Assertions.assertTrue(Vector3D.of(0, Double.POSITIVE_INFINITY, 0).isInfinite());
        Assertions.assertTrue(Vector3D.of(Double.POSITIVE_INFINITY, 0, 0).isInfinite());

        Assertions.assertFalse(Vector3D.of(1, 1, 1).isInfinite());
        Assertions.assertFalse(Vector3D.of(0, 0, Double.NaN).isInfinite());
        Assertions.assertFalse(Vector3D.of(0, Double.NEGATIVE_INFINITY, Double.NaN).isInfinite());
        Assertions.assertFalse(Vector3D.of(Double.NaN, 0, Double.NEGATIVE_INFINITY).isInfinite());
        Assertions.assertFalse(Vector3D.of(Double.POSITIVE_INFINITY, Double.NaN, 0).isInfinite());
        Assertions.assertFalse(Vector3D.of(0, Double.NaN, Double.POSITIVE_INFINITY).isInfinite());
    }

    @Test
    void testFinite() {
        // act/assert
        Assertions.assertTrue(Vector3D.ZERO.isFinite());
        Assertions.assertTrue(Vector3D.of(1, 1, 1).isFinite());

        Assertions.assertFalse(Vector3D.of(0, 0, Double.NEGATIVE_INFINITY).isFinite());
        Assertions.assertFalse(Vector3D.of(0, Double.NEGATIVE_INFINITY, 0).isFinite());
        Assertions.assertFalse(Vector3D.of(Double.NEGATIVE_INFINITY, 0, 0).isFinite());
        Assertions.assertFalse(Vector3D.of(0, 0, Double.POSITIVE_INFINITY).isFinite());
        Assertions.assertFalse(Vector3D.of(0, Double.POSITIVE_INFINITY, 0).isFinite());
        Assertions.assertFalse(Vector3D.of(Double.POSITIVE_INFINITY, 0, 0).isFinite());

        Assertions.assertFalse(Vector3D.of(0, 0, Double.NaN).isFinite());
        Assertions.assertFalse(Vector3D.of(0, Double.NEGATIVE_INFINITY, Double.NaN).isFinite());
        Assertions.assertFalse(Vector3D.of(Double.NaN, 0, Double.NEGATIVE_INFINITY).isFinite());
        Assertions.assertFalse(Vector3D.of(Double.POSITIVE_INFINITY, Double.NaN, 0).isFinite());
        Assertions.assertFalse(Vector3D.of(0, Double.NaN, Double.POSITIVE_INFINITY).isFinite());
    }

    @Test
    void testZero() {
        // act
        final Vector3D zero = Vector3D.of(1, 2, 3).getZero();

        // assert
        checkVector(zero, 0, 0, 0);
        Assertions.assertEquals(0, zero.norm(), EPS);
    }

    @Test
    void testNorm() {
        // act/assert
        Assertions.assertEquals(0.0, Vector3D.ZERO.norm(), 0);
        Assertions.assertEquals(Math.sqrt(29), Vector3D.of(2, 3, 4).norm(), EPS);
        Assertions.assertEquals(Math.sqrt(29), Vector3D.of(-2, -3, -4).norm(), EPS);
    }

    @Test
    void testNorm_unitVectors() {
        // arrange
        final Vector3D v = Vector3D.of(1.0, 2.0, 3.0).normalize();

        // act/assert
        Assertions.assertEquals(1.0, v.norm(), 0.0);
    }

    @Test
    void testNormSq() {
        // act/assert
        Assertions.assertEquals(0.0, Vector3D.ZERO.normSq(), 0);
        Assertions.assertEquals(29, Vector3D.of(2, 3, 4).normSq(), EPS);
        Assertions.assertEquals(29, Vector3D.of(-2, -3, -4).normSq(), EPS);
    }

    @Test
    void testNormSq_unitVectors() {
        // arrange
        final Vector3D v = Vector3D.of(1.0, 2.0, 3.0).normalize();

        // act/assert
        Assertions.assertEquals(1.0, v.normSq(), 0.0);
    }

    @Test
    void testWithNorm() {
        // arrange
        final double x = 2;
        final double y = 3;
        final double z = 4;

        final double len = Math.sqrt((x * x) + (y * y) + (z * z));

        final double normX = x / len;
        final double normY = y / len;
        final double normZ = z / len;

        // act/assert
        checkVector(Vector3D.of(x, y, z).withNorm(0.0), 0.0, 0.0, 0.0);

        checkVector(Vector3D.of(x, y, z).withNorm(1.0), normX, normY, normZ);
        checkVector(Vector3D.of(x, y, -z).withNorm(1.0), normX, normY, -normZ);
        checkVector(Vector3D.of(x, -y, z).withNorm(1.0), normX, -normY, normZ);
        checkVector(Vector3D.of(x, -y, -z).withNorm(1.0), normX, -normY, -normZ);
        checkVector(Vector3D.of(-x, y, z).withNorm(1.0), -normX, normY, normZ);
        checkVector(Vector3D.of(-x, y, -z).withNorm(1.0), -normX, normY, -normZ);
        checkVector(Vector3D.of(-x, -y, z).withNorm(1.0), -normX, -normY, normZ);
        checkVector(Vector3D.of(-x, -y, -z).withNorm(1.0), -normX, -normY, -normZ);

        checkVector(Vector3D.of(x, y, z).withNorm(0.5), 0.5 * normX, 0.5 * normY, 0.5 * normZ);
        checkVector(Vector3D.of(x, y, z).withNorm(3), 3 * normX, 3 * normY, 3 * normZ);

        checkVector(Vector3D.of(x, y, z).withNorm(-0.5), -0.5 * normX, -0.5 * normY, -0.5 * normZ);
        checkVector(Vector3D.of(x, y, z).withNorm(-3), -3 * normX, -3 * normY, -3 * normZ);

        for (int i = 0; i <= 10; i++) {
            final double mag = i * 0.12345 - 5;
            Assertions.assertEquals(Math.abs(mag), Vector3D.of(x, y, z).withNorm(mag).norm(), EPS);
        }
    }

    @Test
    void testWithNorm_illegalNorm() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector3D.ZERO.withNorm(2.0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector3D.NaN.withNorm(2.0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector3D.POSITIVE_INFINITY.withNorm(2.0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector3D.NEGATIVE_INFINITY.withNorm(2.0));
    }

    @Test
    void testWithNorm_unitVectors() {
        // arrange
        final Vector3D v = Vector3D.of(2.0, -3.0, 4.0).normalize();

        // act/assert
        checkVector(Vector3D.Unit.PLUS_X.withNorm(2.5), 2.5, 0.0, 0.0);
        checkVector(Vector3D.Unit.MINUS_Y.withNorm(3.14), 0.0, -3.14, 0.0);
        checkVector(Vector3D.Unit.PLUS_Z.withNorm(-1.1), 0.0, 0.0, -1.1);

        for (double mag = -10.0; mag <= 10.0; ++mag) {
            Assertions.assertEquals(Math.abs(mag), v.withNorm(mag).norm(), EPS);
        }
    }

    @Test
    void testAdd() {
        // arrange
        final Vector3D v1 = Vector3D.of(1, 2, 3);
        final Vector3D v2 = Vector3D.of(-4, -5, -6);
        final Vector3D v3 = Vector3D.of(7, 8, 9);

        // act/assert
        checkVector(v1.add(v1), 2, 4, 6);

        checkVector(v1.add(v2), -3, -3, -3);
        checkVector(v2.add(v1), -3, -3, -3);

        checkVector(v1.add(v3), 8, 10, 12);
        checkVector(v3.add(v1), 8, 10, 12);
    }

    @Test
    void testAdd_scaled() {
        // arrange
        final Vector3D v1 = Vector3D.of(1, 2, 3);
        final Vector3D v2 = Vector3D.of(-4, -5, -6);
        final Vector3D v3 = Vector3D.of(7, 8, 9);

        // act/assert
        checkVector(v1.add(0, v1), 1, 2, 3);
        checkVector(v1.add(0.5, v1), 1.5, 3, 4.5);
        checkVector(v1.add(1, v1), 2, 4, 6);

        checkVector(v1.add(2, v2), -7, -8, -9);
        checkVector(v2.add(2, v1), -2, -1, -0);

        checkVector(v1.add(-2, v3), -13, -14, -15);
        checkVector(v3.add(-2, v1), 5, 4, 3);
    }

    @Test
    void testSubtract() {
        // arrange
        final Vector3D v1 = Vector3D.of(1, 2, 3);
        final Vector3D v2 = Vector3D.of(-4, -5, -6);
        final Vector3D v3 = Vector3D.of(7, 8, 9);

        // act/assert
        checkVector(v1.subtract(v1), 0, 0, 0);

        checkVector(v1.subtract(v2), 5, 7, 9);
        checkVector(v2.subtract(v1), -5, -7, -9);

        checkVector(v1.subtract(v3), -6, -6, -6);
        checkVector(v3.subtract(v1), 6, 6, 6);
    }

    @Test
    void testSubtract_scaled() {
        // arrange
        final Vector3D v1 = Vector3D.of(1, 2, 3);
        final Vector3D v2 = Vector3D.of(-4, -5, -6);
        final Vector3D v3 = Vector3D.of(7, 8, 9);

        // act/assert
        checkVector(v1.subtract(0, v1), 1, 2, 3);
        checkVector(v1.subtract(0.5, v1), 0.5, 1, 1.5);
        checkVector(v1.subtract(1, v1), 0, 0, 0);

        checkVector(v1.subtract(2, v2), 9, 12, 15);
        checkVector(v2.subtract(2, v1), -6, -9, -12);

        checkVector(v1.subtract(-2, v3), 15, 18, 21);
        checkVector(v3.subtract(-2, v1), 9, 12, 15);
    }

    @Test
    void testNegate() {
        // act/assert
        checkVector(Vector3D.of(0.1, 2.5, 1.3).negate(), -0.1, -2.5, -1.3);
        checkVector(Vector3D.of(-0.1, -2.5, -1.3).negate(), 0.1, 2.5, 1.3);
    }

    @Test
    void testNegate_unitVectors() {
        // arrange
        final Vector3D v1 = Vector3D.of(1.0, 2.0, 3.0).normalize();
        final Vector3D v2 = Vector3D.of(-2.0, -4.0, -3.0).normalize();

        // act/assert
        checkVector(v1.negate(), -1.0 / Math.sqrt(14.0), -Math.sqrt(2.0 / 7.0), -3.0 / Math.sqrt(14.0));
        checkVector(v2.negate(), 2.0 / Math.sqrt(29.0), 4.0 / Math.sqrt(29.0), 3.0 / Math.sqrt(29.0));
    }

    @Test
    void testNormalize() {
        // arrange
        final double invSqrt3 = 1 / Math.sqrt(3);

        // act/assert
        checkVector(Vector3D.of(100, 0, 0).normalize(), 1, 0, 0);
        checkVector(Vector3D.of(-100, 0, 0).normalize(), -1, 0, 0);

        checkVector(Vector3D.of(0, 100, 0).normalize(), 0, 1, 0);
        checkVector(Vector3D.of(0, -100, 0).normalize(), 0, -1, 0);

        checkVector(Vector3D.of(0, 0, 100).normalize(), 0, 0, 1);
        checkVector(Vector3D.of(0, 0, -100).normalize(), 0, 0, -1);

        checkVector(Vector3D.of(2, 2, 2).normalize(), invSqrt3, invSqrt3, invSqrt3);
        checkVector(Vector3D.of(-2, -2, -2).normalize(), -invSqrt3, -invSqrt3, -invSqrt3);

        checkVector(Vector3D.of(Double.MIN_VALUE, 0, 0).normalize(), 1, 0, 0);
        checkVector(Vector3D.of(0, Double.MIN_VALUE, 0).normalize(), 0, 1, 0);
        checkVector(Vector3D.of(0, 0, Double.MIN_VALUE).normalize(), 0, 0, 1);

        checkVector(Vector3D.of(-Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE).normalize(),
                -invSqrt3, invSqrt3, invSqrt3);

        checkVector(Vector3D.of(Double.MIN_NORMAL, 0, 0).normalize(), 1, 0, 0);
        checkVector(Vector3D.of(0, Double.MIN_NORMAL, 0).normalize(), 0, 1, 0);
        checkVector(Vector3D.of(0, 0, Double.MIN_NORMAL).normalize(), 0, 0, 1);

        checkVector(Vector3D.of(Double.MIN_NORMAL, Double.MIN_NORMAL, -Double.MIN_NORMAL).normalize(),
                invSqrt3, invSqrt3, -invSqrt3);

        checkVector(Vector3D.of(Double.MAX_VALUE, -Double.MAX_VALUE, Double.MAX_VALUE).normalize(),
                invSqrt3, -invSqrt3, invSqrt3);

        Assertions.assertEquals(1.0, Vector3D.of(5, -4, 2).normalize().norm(), EPS);
    }

    @Test
    void testNormalize_illegalNorm() {
        // arrange
        final Pattern illegalNorm = Pattern.compile("^Illegal norm: (0\\.0|-?Infinity|NaN)");

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(Vector3D.ZERO::normalize,
                IllegalArgumentException.class, illegalNorm);
        GeometryTestUtils.assertThrowsWithMessage(Vector3D.NaN::normalize,
                IllegalArgumentException.class, illegalNorm);
        GeometryTestUtils.assertThrowsWithMessage(Vector3D.POSITIVE_INFINITY::normalize,
                IllegalArgumentException.class, illegalNorm);
        GeometryTestUtils.assertThrowsWithMessage(Vector3D.NEGATIVE_INFINITY::normalize,
                IllegalArgumentException.class, illegalNorm);
    }

    @Test
    void testNormalize_isIdempotent() {
        // arrange
        final double invSqrt3 = 1 / Math.sqrt(3);
        final Vector3D v = Vector3D.of(2, 2, 2).normalize();

        // act/assert
        Assertions.assertSame(v, v.normalize());
        checkVector(v.normalize(), invSqrt3, invSqrt3, invSqrt3);
    }

    @Test
    void testNormalizeOrNull() {
        // arrange
        final double invSqrt3 = 1 / Math.sqrt(3);

        // act/assert
        checkVector(Vector3D.of(100, 0, 0).normalizeOrNull(), 1, 0, 0);
        checkVector(Vector3D.of(-100, 0, 0).normalizeOrNull(), -1, 0, 0);

        checkVector(Vector3D.of(2, 2, 2).normalizeOrNull(), invSqrt3, invSqrt3, invSqrt3);
        checkVector(Vector3D.of(-2, -2, -2).normalizeOrNull(), -invSqrt3, -invSqrt3, -invSqrt3);

        checkVector(Vector3D.of(Double.MIN_VALUE, 0, 0).normalizeOrNull(), 1, 0, 0);
        checkVector(Vector3D.of(0, Double.MIN_VALUE, 0).normalizeOrNull(), 0, 1, 0);
        checkVector(Vector3D.of(0, 0, Double.MIN_VALUE).normalizeOrNull(), 0, 0, 1);

        checkVector(Vector3D.of(-Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE).normalizeOrNull(),
                -invSqrt3, invSqrt3, invSqrt3);

        checkVector(Vector3D.of(Double.MIN_NORMAL, Double.MIN_NORMAL, -Double.MIN_NORMAL).normalizeOrNull(),
                invSqrt3, invSqrt3, -invSqrt3);

        checkVector(Vector3D.of(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE).normalizeOrNull(),
                -invSqrt3, -invSqrt3, -invSqrt3);

        Assertions.assertNull(Vector3D.ZERO.normalizeOrNull());
        Assertions.assertNull(Vector3D.NaN.normalizeOrNull());
        Assertions.assertNull(Vector3D.POSITIVE_INFINITY.normalizeOrNull());
        Assertions.assertNull(Vector3D.NEGATIVE_INFINITY.normalizeOrNull());
    }

    @Test
    void testNormalizeOrNull_isIdempotent() {
        // arrange
        final double invSqrt3 = 1 / Math.sqrt(3);
        final Vector3D v = Vector3D.of(2, 2, 2).normalizeOrNull();

        // act/assert
        Assertions.assertSame(v, v.normalizeOrNull());
        checkVector(v.normalizeOrNull(), invSqrt3, invSqrt3, invSqrt3);
    }

    @Test
    void testOrthogonal() {
        // arrange
        final Vector3D v1 = Vector3D.of(0.1, 2.5, 1.3);
        final Vector3D v2 = Vector3D.of(2.3, -0.003, 7.6);
        final Vector3D v3 = Vector3D.of(-1.7, 1.4, 0.2);
        final Vector3D v4 = Vector3D.of(4.2, 0.1, -1.8);

        // act/assert
        Assertions.assertEquals(0.0, v1.dot(v1.orthogonal()), EPS);
        Assertions.assertEquals(0.0, v2.dot(v2.orthogonal()), EPS);
        Assertions.assertEquals(0.0, v3.dot(v3.orthogonal()), EPS);
        Assertions.assertEquals(0.0, v4.dot(v4.orthogonal()), EPS);
    }

    @Test
    void testOrthogonal_illegalNorm() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, Vector3D.ZERO::orthogonal);
        Assertions.assertThrows(IllegalArgumentException.class, Vector3D.NaN::orthogonal);
        Assertions.assertThrows(IllegalArgumentException.class, Vector3D.POSITIVE_INFINITY::orthogonal);
        Assertions.assertThrows(IllegalArgumentException.class, Vector3D.NEGATIVE_INFINITY::orthogonal);
    }

    @Test
    void testOrthogonal_givenDirection() {
        // arrange
        final double invSqrt2 = 1.0 / Math.sqrt(2.0);

        // act/assert
        checkVector(Vector3D.Unit.PLUS_X.orthogonal(Vector3D.of(-1.0, 0.1, 0.0)), 0.0, 1.0, 0.0);
        checkVector(Vector3D.Unit.PLUS_Y.orthogonal(Vector3D.of(2.0, 2.0, 2.0)), invSqrt2, 0.0, invSqrt2);
        checkVector(Vector3D.Unit.PLUS_Z.orthogonal(Vector3D.of(3.0, 3.0, -3.0)), invSqrt2, invSqrt2, 0.0);

        checkVector(Vector3D.of(invSqrt2, invSqrt2, 0.0).orthogonal(Vector3D.of(1.0, 1.0, 0.2)), 0.0, 0.0, 1.0);
    }

    @Test
    void testOrthogonal_givenDirection_illegalNorm() {
        // act/assert

        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.ZERO.orthogonal(Vector3D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.NaN.orthogonal(Vector3D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.POSITIVE_INFINITY.orthogonal(Vector3D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.NEGATIVE_INFINITY.orthogonal(Vector3D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.Unit.PLUS_X.orthogonal(Vector3D.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.Unit.PLUS_X.orthogonal(Vector3D.NaN));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.Unit.PLUS_X.orthogonal(Vector3D.POSITIVE_INFINITY));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.Unit.PLUS_X.orthogonal(Vector3D.NEGATIVE_INFINITY));
    }

    @Test
    void testOrthogonal_givenDirection_directionIsCollinear() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.Unit.PLUS_X.orthogonal(Vector3D.Unit.PLUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.Unit.PLUS_X.orthogonal(Vector3D.Unit.MINUS_X));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.of(1.0, 1.0, 1.0).orthogonal(Vector3D.of(2.0, 2.0, 2.0)));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.of(-1.01, -1.01, -1.01).orthogonal(Vector3D.of(20.1, 20.1, 20.1)));
    }

    @Test
    void testAngle() {
        // arrange
        final double tolerance = 1e-10;

        final Vector3D v1 = Vector3D.of(1, 2, 3);
        final Vector3D v2 = Vector3D.of(4, 5, 6);

        // act/assert
        Assertions.assertEquals(0.22572612855273393616, v1.angle(v2), tolerance);
        Assertions.assertEquals(7.98595620686106654517199e-8, v1.angle(Vector3D.of(2, 4, 6.000001)), tolerance);
        Assertions.assertEquals(3.14159257373023116985197793156, v1.angle(Vector3D.of(-2, -4, -6.000001)), tolerance);

        Assertions.assertEquals(0.0, Vector3D.Unit.PLUS_X.angle(Vector3D.Unit.PLUS_X), tolerance);
        Assertions.assertEquals(Math.PI, Vector3D.Unit.PLUS_X.angle(Vector3D.Unit.MINUS_X), tolerance);

        Assertions.assertEquals(Angle.PI_OVER_TWO, Vector3D.Unit.PLUS_X.angle(Vector3D.Unit.PLUS_Y), tolerance);
        Assertions.assertEquals(Angle.PI_OVER_TWO, Vector3D.Unit.PLUS_X.angle(Vector3D.Unit.MINUS_Y), tolerance);
        Assertions.assertEquals(Angle.PI_OVER_TWO, Vector3D.Unit.PLUS_X.angle(Vector3D.Unit.PLUS_Z), tolerance);
        Assertions.assertEquals(Angle.PI_OVER_TWO, Vector3D.Unit.PLUS_X.angle(Vector3D.Unit.MINUS_Z), tolerance);
    }

    @Test
    void testAngle_illegalNorm() {
        // arrange
        final Vector3D v = Vector3D.of(1.0, 1.0, 1.0);

        // act/assert

        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.ZERO.angle(v));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.NaN.angle(v));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.POSITIVE_INFINITY.angle(v));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.NEGATIVE_INFINITY.angle(v));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  v.angle(Vector3D.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  v.angle(Vector3D.NaN));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  v.angle(Vector3D.POSITIVE_INFINITY));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  v.angle(Vector3D.NEGATIVE_INFINITY));
    }

    @Test
    void testAngle_angularSeparation() {
        // arrange
        final Vector3D v1 = Vector3D.of(2, -1, 4);

        final Vector3D  k = v1.normalize();
        final Vector3D  i = k.orthogonal();
        final Vector3D v2 = k.multiply(Math.cos(1.2)).add(i.multiply(Math.sin(1.2)));

        // act/assert
        Assertions.assertTrue(Math.abs(v1.angle(v2) - 1.2) < 1.0e-12);
    }

    @Test
    void testCrossProduct() {
        // act/assert
        checkVector(Vector3D.Unit.PLUS_X.cross(Vector3D.Unit.PLUS_Y), 0, 0, 1);
        checkVector(Vector3D.Unit.PLUS_X.cross(Vector3D.Unit.MINUS_Y), 0, 0, -1);

        checkVector(Vector3D.Unit.MINUS_X.cross(Vector3D.Unit.MINUS_Y), 0, 0, 1);
        checkVector(Vector3D.Unit.MINUS_X.cross(Vector3D.Unit.PLUS_Y), 0, 0, -1);

        checkVector(Vector3D.of(2, 1, -4).cross(Vector3D.of(3, 1, -1)), 3, -10, -1);

        final double invSqrt6 = 1 / Math.sqrt(6);
        checkVector(Vector3D.of(1, 1, 1).cross(Vector3D.of(-1, 0, 1)).normalize(), invSqrt6, -2 * invSqrt6, invSqrt6);
    }

    @Test
    void testCrossProduct_nearlyAntiParallel() {
        // the vectors u1 and u2 are nearly but not exactly anti-parallel
        // (7.31e-16 degrees from 180 degrees) naive cross product (i.e.
        // computing u1.x * u2.x + u1.y * u2.y + u1.z * u2.z
        // leads to a result of   [0.0009765, -0.0001220, -0.0039062],
        // instead of the correct [0.0006913, -0.0001254, -0.0007909]

        // arrange
        final Vector3D u1 = Vector3D.of(-1321008684645961.0 / 268435456.0,
                                         -5774608829631843.0 / 268435456.0,
                                         -7645843051051357.0 / 8589934592.0);
        final Vector3D u2 = Vector3D.of(1796571811118507.0 / 2147483648.0,
                                          7853468008299307.0 / 2147483648.0,
                                          2599586637357461.0 / 17179869184.0);
        final Vector3D u3 = Vector3D.of(12753243807587107.0 / 18446744073709551616.0,
                                         -2313766922703915.0 / 18446744073709551616.0,
                                          -227970081415313.0 / 288230376151711744.0);

        // act
        final Vector3D cNaive = Vector3D.of(u1.getY() * u2.getZ() - u1.getZ() * u2.getY(),
                                       u1.getZ() * u2.getX() - u1.getX() * u2.getZ(),
                                       u1.getX() * u2.getY() - u1.getY() * u2.getX());
        final Vector3D cAccurate = u1.cross(u2);

        // assert
        Assertions.assertTrue(u3.distance(cNaive) > 2.9 * u3.norm());
        Assertions.assertEquals(0.0, u3.distance(cAccurate), 1.0e-30 * cAccurate.norm());
    }

    @Test
    void testCrossProduct_accuracy() {
        // we compare accurate versus naive cross product implementations
        // on regular vectors (i.e. not extreme cases like in the previous test)
        final UniformRandomProvider random = RandomSource.create(RandomSource.WELL_1024_A, 885362227452043215L);
        for (int i = 0; i < 10000; ++i) {
            // arrange
            final double ux = 10000 * random.nextDouble();
            final double uy = 10000 * random.nextDouble();
            final double uz = 10000 * random.nextDouble();
            final double vx = 10000 * random.nextDouble();
            final double vy = 10000 * random.nextDouble();
            final double vz = 10000 * random.nextDouble();

            // act
            final Vector3D cNaive = Vector3D.of(uy * vz - uz * vy, uz * vx - ux * vz, ux * vy - uy * vx);
            final Vector3D cAccurate = Vector3D.of(ux, uy, uz).cross(Vector3D.of(vx, vy, vz));

            // assert
            Assertions.assertEquals(0.0, cAccurate.distance(cNaive), 6.0e-15 * cAccurate.norm());
        }
    }

    @Test
    void testCrossProduct_cancellation() {
        // act/assert
        final Vector3D v1 = Vector3D.of(9070467121.0, 4535233560.0, 1);
        final Vector3D v2 = Vector3D.of(9070467123.0, 4535233561.0, 1);
        checkVector(v1.cross(v2), -1, 2, 1);

        final double scale    = Math.scalb(1.0, 100);
        final Vector3D big1   = Vector3D.linearCombination(scale, v1);
        final Vector3D small2 = Vector3D.linearCombination(1 / scale, v2);
        checkVector(big1.cross(small2), -1, 2, 1);
    }

    @Test
    void testScalarMultiply() {
        // arrange
        final Vector3D v1 = Vector3D.of(2, 3, 4);
        final Vector3D v2 = Vector3D.of(-2, -3, -4);

        // act/assert
        checkVector(v1.multiply(0), 0, 0, 0);
        checkVector(v1.multiply(0.5), 1, 1.5, 2);
        checkVector(v1.multiply(1), 2, 3, 4);
        checkVector(v1.multiply(2), 4, 6, 8);
        checkVector(v1.multiply(-2), -4, -6, -8);

        checkVector(v2.multiply(0), 0, 0, 0);
        checkVector(v2.multiply(0.5), -1, -1.5, -2);
        checkVector(v2.multiply(1), -2, -3, -4);
        checkVector(v2.multiply(2), -4, -6, -8);
        checkVector(v2.multiply(-2), 4, 6, 8);
    }

    @Test
    void testDistance() {
        // arrange
        final Vector3D v1 = Vector3D.of(1, -2, 3);
        final Vector3D v2 = Vector3D.of(-4, 2, 0);
        final Vector3D v3 = Vector3D.of(5, -6, -7);

        // act/assert
        Assertions.assertEquals(0.0, v1.distance(v1), EPS);
        Assertions.assertEquals(0.0, v2.distance(v2), EPS);

        Assertions.assertEquals(Math.sqrt(50), v1.distance(v2), EPS);
        Assertions.assertEquals(Math.sqrt(50), v2.distance(v1), EPS);

        Assertions.assertEquals(v1.subtract(v2).norm(), v1.distance(v2), EPS);

        Assertions.assertEquals(Math.sqrt(132), v1.distance(v3), EPS);
        Assertions.assertEquals(Math.sqrt(132), v3.distance(v1), EPS);
    }

    @Test
    void testDistanceSq() {
        // arrange
        final Vector3D v1 = Vector3D.of(1, -2, 3);
        final Vector3D v2 = Vector3D.of(-4, 2, 0);
        final Vector3D v3 = Vector3D.of(5, -6, -7);

        // act/assert
        Assertions.assertEquals(0.0, v1.distanceSq(v1), EPS);
        Assertions.assertEquals(0.0, v2.distanceSq(v2), EPS);

        Assertions.assertEquals(50, v1.distanceSq(v2), EPS);
        Assertions.assertEquals(50, v2.distanceSq(v1), EPS);

        Assertions.assertEquals(v1.subtract(v2).normSq(), v1.distanceSq(v2), EPS);

        Assertions.assertEquals(132, v1.distanceSq(v3), EPS);
        Assertions.assertEquals(132, v3.distanceSq(v1), EPS);
    }

    @Test
    void testDotProduct() {
        // arrange
        final Vector3D v1 = Vector3D.of(1, -2, 3);
        final Vector3D v2 = Vector3D.of(-4, 5, -6);
        final Vector3D v3 = Vector3D.of(7, 8, 9);

        // act/assert
        Assertions.assertEquals(14, v1.dot(v1), EPS);

        Assertions.assertEquals(-32, v1.dot(v2), EPS);
        Assertions.assertEquals(-32, v2.dot(v1), EPS);

        Assertions.assertEquals(18, v1.dot(v3), EPS);
        Assertions.assertEquals(18, v3.dot(v1), EPS);
    }

    @Test
    void testDotProduct_nearlyOrthogonal() {
        // the following two vectors are nearly but not exactly orthogonal
        // naive dot product (i.e. computing u1.x * u2.x + u1.y * u2.y + u1.z * u2.z
        // leads to a result of 0.0, instead of the correct -1.855129...

        // arrange
        final Vector3D u1 = Vector3D.of(-1321008684645961.0 /  268435456.0,
                                   -5774608829631843.0 /  268435456.0,
                                   -7645843051051357.0 / 8589934592.0);
        final Vector3D u2 = Vector3D.of(-5712344449280879.0 /    2097152.0,
                                   -4550117129121957.0 /    2097152.0,
                                    8846951984510141.0 /     131072.0);

        // act
        final double sNaive = u1.getX() * u2.getX() + u1.getY() * u2.getY() + u1.getZ() * u2.getZ();
        final double sAccurate = u1.dot(u2);

        // assert
        Assertions.assertEquals(0.0, sNaive, 1.0e-30);
        Assertions.assertEquals(-2088690039198397.0 / 1125899906842624.0, sAccurate, 1.0e-15);
    }

    @Test
    void testDotProduct_accuracy() {
        // we compare accurate versus naive dot product implementations
        // on regular vectors (i.e. not extreme cases like in the previous test)
        final UniformRandomProvider random = RandomSource.create(RandomSource.WELL_1024_A, 553267312521321237L);
        for (int i = 0; i < 10000; ++i) {
            // arrange
            final double ux = 10000 * random.nextDouble();
            final double uy = 10000 * random.nextDouble();
            final double uz = 10000 * random.nextDouble();
            final double vx = 10000 * random.nextDouble();
            final double vy = 10000 * random.nextDouble();
            final double vz = 10000 * random.nextDouble();

            // act
            final double sNaive = ux * vx + uy * vy + uz * vz;
            final double sAccurate = Vector3D.of(ux, uy, uz).dot(Vector3D.of(vx, vy, vz));

            // assert
            Assertions.assertEquals(sNaive, sAccurate, 2.5e-16 * sAccurate);
        }
    }

    @Test
    void testProject() {
        // arrange
        final Vector3D v1 = Vector3D.of(2.0, 3.0, 4.0);
        final Vector3D v2 = Vector3D.of(-5.0, -6.0, -7.0);

        // act/assert
        checkVector(Vector3D.ZERO.project(Vector3D.Unit.PLUS_X), 0.0, 0.0, 0.0);

        checkVector(v1.project(Vector3D.Unit.PLUS_X), 2.0, 0.0, 0.0);
        checkVector(v1.project(Vector3D.Unit.MINUS_X), 2.0, 0.0, 0.0);
        checkVector(v1.project(Vector3D.Unit.PLUS_Y), 0.0, 3.0, 0.0);
        checkVector(v1.project(Vector3D.Unit.MINUS_Y), 0.0, 3.0, 0.0);
        checkVector(v1.project(Vector3D.Unit.PLUS_Z), 0.0, 0.0, 4.0);
        checkVector(v1.project(Vector3D.Unit.MINUS_Z), 0.0, 0.0, 4.0);

        checkVector(v2.project(Vector3D.Unit.PLUS_X), -5.0, 0.0, 0.0);
        checkVector(v2.project(Vector3D.Unit.MINUS_X), -5.0, 0.0, 0.0);
        checkVector(v2.project(Vector3D.Unit.PLUS_Y), 0.0, -6.0, 0.0);
        checkVector(v2.project(Vector3D.Unit.MINUS_Y), 0.0, -6.0, 0.0);
        checkVector(v2.project(Vector3D.Unit.PLUS_Z), 0.0, 0.0, -7.0);
        checkVector(v2.project(Vector3D.Unit.MINUS_Z), 0.0, 0.0, -7.0);

        checkVector(v1.project(Vector3D.of(1.0, 1.0, 1.0)), 3.0, 3.0, 3.0);
        checkVector(v1.project(Vector3D.of(-1.0, -1.0, -1.0)), 3.0, 3.0, 3.0);

        checkVector(v2.project(Vector3D.of(1.0, 1.0, 1.0)), -6.0, -6.0, -6.0);
        checkVector(v2.project(Vector3D.of(-1.0, -1.0, -1.0)), -6.0, -6.0, -6.0);
    }

    @Test
    void testProject_baseHasIllegalNorm() {
        // arrange
        final Vector3D v = Vector3D.of(1.0, 1.0, 1.0);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.project(Vector3D.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.project(Vector3D.NaN));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.project(Vector3D.POSITIVE_INFINITY));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  v.project(Vector3D.NEGATIVE_INFINITY));
    }

    @Test
    void testReject() {
        // arrange
        final Vector3D v1 = Vector3D.of(2.0, 3.0, 4.0);
        final Vector3D v2 = Vector3D.of(-5.0, -6.0, -7.0);

        // act/assert
        checkVector(Vector3D.ZERO.reject(Vector3D.Unit.PLUS_X), 0.0, 0.0, 0.0);

        checkVector(v1.reject(Vector3D.Unit.PLUS_X), 0.0, 3.0, 4.0);
        checkVector(v1.reject(Vector3D.Unit.MINUS_X), 0.0, 3.0, 4.0);
        checkVector(v1.reject(Vector3D.Unit.PLUS_Y), 2.0, 0.0, 4.0);
        checkVector(v1.reject(Vector3D.Unit.MINUS_Y), 2.0, 0.0, 4.0);
        checkVector(v1.reject(Vector3D.Unit.PLUS_Z), 2.0, 3.0, 0.0);
        checkVector(v1.reject(Vector3D.Unit.MINUS_Z), 2.0, 3.0, 0.0);

        checkVector(v2.reject(Vector3D.Unit.PLUS_X), 0.0, -6.0, -7.0);
        checkVector(v2.reject(Vector3D.Unit.MINUS_X), 0.0, -6.0, -7.0);
        checkVector(v2.reject(Vector3D.Unit.PLUS_Y), -5.0, 0.0, -7.0);
        checkVector(v2.reject(Vector3D.Unit.MINUS_Y), -5.0, 0.0, -7.0);
        checkVector(v2.reject(Vector3D.Unit.PLUS_Z), -5.0, -6.0, 0.0);
        checkVector(v2.reject(Vector3D.Unit.MINUS_Z), -5.0, -6.0, 0.0);

        checkVector(v1.reject(Vector3D.of(1.0, 1.0, 1.0)), -1.0, 0.0, 1.0);
        checkVector(v1.reject(Vector3D.of(-1.0, -1.0, -1.0)), -1.0, 0.0, 1.0);

        checkVector(v2.reject(Vector3D.of(1.0, 1.0, 1.0)), 1.0, 0.0, -1.0);
        checkVector(v2.reject(Vector3D.of(-1.0, -1.0, -1.0)), 1.0, 0.0, -1.0);
    }

    @Test
    void testReject_baseHasIllegalNorm() {
        // arrange
        final Vector3D v = Vector3D.of(1.0, 1.0, 1.0);

        // act/assert

        Assertions.assertThrows(IllegalArgumentException.class, () -> v.reject(Vector3D.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.reject(Vector3D.NaN));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.reject(Vector3D.POSITIVE_INFINITY));
        Assertions.assertThrows(IllegalArgumentException.class, () -> v.reject(Vector3D.NEGATIVE_INFINITY));

    }

    @Test
    void testProjectAndReject_areComplementary() {
        // arrange
        final double eps = 1e-12;

        // act/assert
        checkProjectAndRejectFullSphere(Vector3D.of(1.0, 0.0, 0.0), 1.0, eps);
        checkProjectAndRejectFullSphere(Vector3D.of(0.0, 1.0, 0.0), 2.0, eps);
        checkProjectAndRejectFullSphere(Vector3D.of(0.0, 0.0, 1.0), 2.0, eps);
        checkProjectAndRejectFullSphere(Vector3D.of(1.0, 1.0, 1.0), 3.0, eps);

        checkProjectAndRejectFullSphere(Vector3D.of(-2.0, 0.0, 0.0), 1.0, eps);
        checkProjectAndRejectFullSphere(Vector3D.of(0.0, -2.0, 0.0), 2.0, eps);
        checkProjectAndRejectFullSphere(Vector3D.of(0.0, 0.0, -2.0), 2.0, eps);
        checkProjectAndRejectFullSphere(Vector3D.of(-2.0, -2.0, -2.0), 3.0, eps);
    }

    private void checkProjectAndRejectFullSphere(final Vector3D vec, final double baseMag, final double eps) {
        for (double polar = 0.0; polar <= Math.PI; polar += 0.5) {
            for (double azimuth = 0.0; azimuth <= Angle.TWO_PI; azimuth += 0.5) {
                final Vector3D base = SphericalCoordinates.toCartesian(baseMag, azimuth, polar);

                final Vector3D proj = vec.project(base);
                final Vector3D rej = vec.reject(base);

                // ensure that the projection and rejection sum to the original vector
                EuclideanTestUtils.assertCoordinatesEqual(vec, proj.add(rej), eps);

                final double angle = base.angle(vec);

                // check the angle between the projection and the base; this will
                // be undefined when the angle between the original vector and the
                // base is pi/2 (which means that the projection is the zero vector)
                if (angle < Angle.PI_OVER_TWO) {
                    Assertions.assertEquals(0.0, proj.angle(base), eps);
                } else if (angle > Angle.PI_OVER_TWO) {
                    Assertions.assertEquals(Math.PI, proj.angle(base), eps);
                }

                // check the angle between the rejection and the base; this should
                // always be pi/2 except for when the angle between the original vector
                // and the base is 0 or pi, in which case the rejection is the zero vector.
                if (angle > 0.0 && angle < Math.PI) {
                    Assertions.assertEquals(Angle.PI_OVER_TWO, rej.angle(base), eps);
                }
            }
        }
    }

    @Test
    void testVectorTo() {
        // act/assert
        final Vector3D p1 = Vector3D.of(1, 2, 3);
        final Vector3D p2 = Vector3D.of(4, 5, 6);
        final Vector3D p3 = Vector3D.of(-7, -8, -9);

        // act/assert
        checkVector(p1.vectorTo(p1), 0, 0, 0);
        checkVector(p2.vectorTo(p2), 0, 0, 0);
        checkVector(p3.vectorTo(p3), 0, 0, 0);

        checkVector(p1.vectorTo(p2), 3, 3, 3);
        checkVector(p2.vectorTo(p1), -3, -3, -3);

        checkVector(p1.vectorTo(p3), -8, -10, -12);
        checkVector(p3.vectorTo(p1), 8, 10, 12);
    }

    @Test
    void testDirectionTo() {
        // act/assert
        final double invSqrt3 = 1.0 / Math.sqrt(3);

        final Vector3D p1 = Vector3D.of(1, 1, 1);
        final Vector3D p2 = Vector3D.of(1, 5, 1);
        final Vector3D p3 = Vector3D.of(-2, -2, -2);

        // act/assert
        checkVector(p1.directionTo(p2), 0, 1, 0);
        checkVector(p2.directionTo(p1), 0, -1, 0);

        checkVector(p1.directionTo(p3), -invSqrt3, -invSqrt3, -invSqrt3);
        checkVector(p3.directionTo(p1), invSqrt3, invSqrt3, invSqrt3);
    }

    @Test
    void testDirectionTo_illegalNorm() {
        // arrange
        final Vector3D p = Vector3D.of(1, 2, 3);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.ZERO.directionTo(Vector3D.ZERO));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  p.directionTo(p));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.NEGATIVE_INFINITY.directionTo(p));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  p.directionTo(Vector3D.POSITIVE_INFINITY));
    }

    @Test
    void testLerp() {
        // arrange
        final Vector3D v1 = Vector3D.of(1, -5, 2);
        final Vector3D v2 = Vector3D.of(-4, 0, 2);
        final Vector3D v3 = Vector3D.of(10, -4, 0);

        // act/assert
        checkVector(v1.lerp(v1, 0), 1, -5, 2);
        checkVector(v1.lerp(v1, 1), 1, -5, 2);

        checkVector(v1.lerp(v2, -0.25), 2.25, -6.25, 2);
        checkVector(v1.lerp(v2, 0), 1, -5, 2);
        checkVector(v1.lerp(v2, 0.25), -0.25, -3.75, 2);
        checkVector(v1.lerp(v2, 0.5), -1.5, -2.5, 2);
        checkVector(v1.lerp(v2, 0.75), -2.75, -1.25, 2);
        checkVector(v1.lerp(v2, 1), -4, 0, 2);
        checkVector(v1.lerp(v2, 1.25), -5.25, 1.25, 2);

        checkVector(v1.lerp(v3, 0), 1, -5, 2);
        checkVector(v1.lerp(v3, 0.25), 3.25, -4.75, 1.5);
        checkVector(v1.lerp(v3, 0.5), 5.5, -4.5, 1);
        checkVector(v1.lerp(v3, 0.75), 7.75, -4.25, 0.5);
        checkVector(v1.lerp(v3, 1), 10, -4, 0);
    }

    @Test
    void testTransform() {
        // arrange
        final AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .scale(2)
                .translate(1, 2, 3);

        final Vector3D v1 = Vector3D.of(1, 2, 3);
        final Vector3D v2 = Vector3D.of(-4, -5, -6);

        // act/assert
        checkVector(v1.transform(transform), 3, 6, 9);
        checkVector(v2.transform(transform), -7, -8, -9);
    }

    @Test
    void testPrecisionEquals() {
        // arrange
        final Precision.DoubleEquivalence smallEps = Precision.doubleEquivalenceOfEpsilon(1e-6);
        final Precision.DoubleEquivalence largeEps = Precision.doubleEquivalenceOfEpsilon(1e-1);

        final Vector3D vec = Vector3D.of(1, -2, 3);

        // act/assert
        Assertions.assertTrue(vec.eq(vec, smallEps));
        Assertions.assertTrue(vec.eq(vec, largeEps));

        Assertions.assertTrue(vec.eq(Vector3D.of(1.0000007, -2.0000009, 3.0000009), smallEps));
        Assertions.assertTrue(vec.eq(Vector3D.of(1.0000007, -2.0000009, 3.0000009), largeEps));

        Assertions.assertFalse(vec.eq(Vector3D.of(1.004, -2, 3), smallEps));
        Assertions.assertFalse(vec.eq(Vector3D.of(1, -2.004, 3), smallEps));
        Assertions.assertFalse(vec.eq(Vector3D.of(1, -2, 2.999), smallEps));
        Assertions.assertTrue(vec.eq(Vector3D.of(1.004, -2.004, 2.999), largeEps));

        Assertions.assertFalse(vec.eq(Vector3D.of(2, -2, 3), smallEps));
        Assertions.assertFalse(vec.eq(Vector3D.of(1, -3, 3), smallEps));
        Assertions.assertFalse(vec.eq(Vector3D.of(1, -2, 4), smallEps));
        Assertions.assertFalse(vec.eq(Vector3D.of(2, -3, 4), smallEps));

        Assertions.assertFalse(vec.eq(Vector3D.of(2, -2, 3), largeEps));
        Assertions.assertFalse(vec.eq(Vector3D.of(1, -3, 3), largeEps));
        Assertions.assertFalse(vec.eq(Vector3D.of(1, -2, 4), largeEps));
        Assertions.assertFalse(vec.eq(Vector3D.of(2, -3, 4), largeEps));
    }

    @Test
    void testIsZero() {
        // arrange
        final Precision.DoubleEquivalence smallEps = Precision.doubleEquivalenceOfEpsilon(1e-6);
        final Precision.DoubleEquivalence largeEps = Precision.doubleEquivalenceOfEpsilon(1e-1);

        // act/assert
        Assertions.assertTrue(Vector3D.of(0.0, -0.0, 0.0).isZero(smallEps));
        Assertions.assertTrue(Vector3D.of(-0.0, 0.0, -0.0).isZero(largeEps));

        Assertions.assertTrue(Vector3D.of(-1e-7, 1e-7, -1e-8).isZero(smallEps));
        Assertions.assertTrue(Vector3D.of(1e-7, -1e-7, 1e-8).isZero(largeEps));

        Assertions.assertFalse(Vector3D.of(1e-2, 0.0, 0.0).isZero(smallEps));
        Assertions.assertFalse(Vector3D.of(0.0, 1e-2, 0.0).isZero(smallEps));
        Assertions.assertFalse(Vector3D.of(0.0, 0.0, 1e-2).isZero(smallEps));
        Assertions.assertTrue(Vector3D.of(1e-2, -1e-2, 1e-2).isZero(largeEps));

        Assertions.assertFalse(Vector3D.of(0.2, 0.0, 0.0).isZero(smallEps));
        Assertions.assertFalse(Vector3D.of(0.0, 0.2, 0.0).isZero(smallEps));
        Assertions.assertFalse(Vector3D.of(0.0, 0.0, 0.2).isZero(smallEps));
        Assertions.assertFalse(Vector3D.of(0.2, 0.2, 0.2).isZero(smallEps));

        Assertions.assertFalse(Vector3D.of(0.2, 0.0, 0.0).isZero(largeEps));
        Assertions.assertFalse(Vector3D.of(0.0, 0.2, 0.0).isZero(largeEps));
        Assertions.assertFalse(Vector3D.of(0.0, 0.0, 0.2).isZero(largeEps));
        Assertions.assertFalse(Vector3D.of(0.2, 0.2, 0.2).isZero(largeEps));
    }

    @Test
    void testHashCode() {
        // arrange
        final double delta = 10 * Precision.EPSILON;
        final Vector3D u = Vector3D.of(1, 1, 1);
        final Vector3D v = Vector3D.of(1 + delta, 1 + delta, 1 + delta);
        final Vector3D w = Vector3D.of(1, 1, 1);

        // act/assert
        Assertions.assertTrue(u.hashCode() != v.hashCode());
        Assertions.assertEquals(u.hashCode(), w.hashCode());

        Assertions.assertEquals(Vector3D.of(0, 0, Double.NaN).hashCode(), Vector3D.NaN.hashCode());
        Assertions.assertEquals(Vector3D.of(0, Double.NaN, 0).hashCode(), Vector3D.NaN.hashCode());
        Assertions.assertEquals(Vector3D.of(Double.NaN, 0, 0).hashCode(), Vector3D.NaN.hashCode());
        Assertions.assertEquals(Vector3D.of(0, 0, Double.NaN).hashCode(), Vector3D.of(Double.NaN, 0, 0).hashCode());
    }

    @Test
    void testEquals() {
        // arrange
        final double delta = 10 * Precision.EPSILON;
        final Vector3D u1 = Vector3D.of(1, 2, 3);
        final Vector3D u2 = Vector3D.of(1, 2, 3);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(u1);
        Assertions.assertEquals(u1, u2);

        Assertions.assertNotEquals(u1, Vector3D.of(-1, -2, -3));
        Assertions.assertNotEquals(u1, Vector3D.of(1 + delta, 2, 3));
        Assertions.assertNotEquals(u1, Vector3D.of(1, 2 + delta, 3));
        Assertions.assertNotEquals(u1, Vector3D.of(1, 2, 3 + delta));

        Assertions.assertEquals(Vector3D.of(0, Double.NaN, 0), Vector3D.of(Double.NaN, 0, 0));

        Assertions.assertEquals(Vector3D.of(0, 0, Double.POSITIVE_INFINITY), Vector3D.of(0, 0, Double.POSITIVE_INFINITY));
        Assertions.assertNotEquals(Vector3D.of(0, Double.POSITIVE_INFINITY, 0), Vector3D.of(0, 0, Double.POSITIVE_INFINITY));
        Assertions.assertNotEquals(Vector3D.of(Double.POSITIVE_INFINITY, 0, 0), Vector3D.of(0, 0, Double.POSITIVE_INFINITY));

        Assertions.assertEquals(Vector3D.of(Double.NEGATIVE_INFINITY, 0, 0), Vector3D.of(Double.NEGATIVE_INFINITY, 0, 0));
        Assertions.assertNotEquals(Vector3D.of(0, Double.NEGATIVE_INFINITY, 0), Vector3D.of(Double.NEGATIVE_INFINITY, 0, 0));
        Assertions.assertNotEquals(Vector3D.of(0, 0, Double.NEGATIVE_INFINITY), Vector3D.of(Double.NEGATIVE_INFINITY, 0, 0));
    }

    @Test
    void testEqualsAndHashCode_signedZeroConsistency() {
        // arrange
        final Vector3D a = Vector3D.of(0.0, -0.0, 0.0);
        final Vector3D b = Vector3D.of(-0.0, 0.0, -0.0);
        final Vector3D c = Vector3D.of(0.0, -0.0, 0.0);
        final Vector3D d = Vector3D.of(-0.0, 0.0, -0.0);

        // act/assert
        Assertions.assertFalse(a.equals(b));

        Assertions.assertTrue(a.equals(c));
        Assertions.assertEquals(a.hashCode(), c.hashCode());

        Assertions.assertTrue(b.equals(d));
        Assertions.assertEquals(b.hashCode(), d.hashCode());
    }

    @Test
    void testToString() {
        // arrange
        final Vector3D v = Vector3D.of(1, 2, 3);
        final Pattern pattern = Pattern.compile("\\(1.{0,2}, 2.{0,2}, 3.{0,2}\\)");

        // act
        final String str = v.toString();

        // assert
        Assertions.assertTrue(pattern.matcher(str).matches(),
                "Expected string " + str + " to match regex " + pattern);
    }

    @Test
    void testParse() {
        // act/assert
        checkVector(Vector3D.parse("(1, 2, 3)"), 1, 2, 3);
        checkVector(Vector3D.parse("(-1, -2, -3)"), -1, -2, -3);

        checkVector(Vector3D.parse("(0.01, -1e-3, 0)"), 1e-2, -1e-3, 0);

        checkVector(Vector3D.parse("(NaN, -Infinity, Infinity)"), Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        checkVector(Vector3D.parse(Vector3D.ZERO.toString()), 0, 0, 0);
        checkVector(Vector3D.parse(Vector3D.Unit.MINUS_X.toString()), -1, 0, 0);
    }

    @Test
    void testParse_failure() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector3D.parse("abc"));
    }

    @Test
    void testOf() {
        // act/assert
        checkVector(Vector3D.of(1, 2, 3), 1, 2, 3);
        checkVector(Vector3D.of(-1, -2, -3), -1, -2, -3);
        checkVector(Vector3D.of(Math.PI, Double.NaN, Double.POSITIVE_INFINITY),
                Math.PI, Double.NaN, Double.POSITIVE_INFINITY);
        checkVector(Vector3D.of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Math.E),
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Math.E);
    }

    @Test
    void testOf_arrayArg() {
        // act/assert
        checkVector(Vector3D.of(new double[] {1, 2, 3}), 1, 2, 3);
        checkVector(Vector3D.of(new double[] {-1, -2, -3}), -1, -2, -3);
        checkVector(Vector3D.of(new double[] {Math.PI, Double.NaN, Double.POSITIVE_INFINITY}),
                Math.PI, Double.NaN, Double.POSITIVE_INFINITY);
        checkVector(Vector3D.of(new double[] {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Math.E}),
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Math.E);
    }

    @Test
    void testOf_arrayArg_invalidDimensions() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Vector3D.of(new double[] {0.0, 0.0}));
    }

    @Test
    void testUnitFrom_coordinates() {
        // arrange
        final double invSqrt3 = 1.0 / Math.sqrt(3.0);

        // act/assert
        checkVector(Vector3D.Unit.from(2.0, -2.0, 2.0), invSqrt3, -invSqrt3, invSqrt3);
        checkVector(Vector3D.Unit.from(-4.0, 4.0, -4.0), -invSqrt3, invSqrt3, -invSqrt3);
    }

    @Test
    void testUnitFrom_vector() {
        // arrange
        final double invSqrt3 = 1.0 / Math.sqrt(3.0);
        final Vector3D vec = Vector3D.of(2.0, -2.0, 2.0);
        final Vector3D.Unit unitVec = Vector3D.Unit.from(2.0, -2.0, 2.0);

        // act/assert
        checkVector(Vector3D.Unit.from(vec), invSqrt3, -invSqrt3, invSqrt3);
        Assertions.assertSame(unitVec, Vector3D.Unit.from(unitVec));
    }

    @Test
    void testUnitFrom_static_illegalNorm() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.Unit.from(0.0, 0.0, 0.0));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.Unit.from(Double.NaN, 1.0, 1.0));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.Unit.from(1.0, Double.NEGATIVE_INFINITY, 1.0));
        Assertions.assertThrows(IllegalArgumentException.class, () ->  Vector3D.Unit.from(1.0, 1.0, Double.POSITIVE_INFINITY));
    }

    @Test
    void testMax() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-100, 1, 100),
                Vector3D.max(Collections.singletonList(Vector3D.of(-100, 1, 100))), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 100),
                Vector3D.max(Arrays.asList(Vector3D.of(-100, 1, 100), Vector3D.of(0, 1, 0))), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 2),
                Vector3D.max(Vector3D.of(-2, 0, 0), Vector3D.of(-1, -5, 1), Vector3D.of(-10, -10, 2)), EPS);
    }

    @Test
    void testMax_noPointsGiven() {
        // arrange
        final String msg = "Cannot compute vector max: no vectors given";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Vector3D.max(new ArrayList<>());
        }, IllegalArgumentException.class, msg);
    }

    @Test
    void testMin() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-100, 1, 100),
                Vector3D.min(Collections.singletonList(Vector3D.of(-100, 1, 100))), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-100, 1, 0),
                Vector3D.min(Arrays.asList(Vector3D.of(-100, 1, 100), Vector3D.of(0, 1, 0))), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-10, -10, 0),
                Vector3D.min(Vector3D.of(-2, 0, 0), Vector3D.of(-1, -5, 1), Vector3D.of(-10, -10, 2)), EPS);
    }

    @Test
    void testMin_noPointsGiven() {
        // arrange
        final String msg = "Cannot compute vector min: no vectors given";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Vector3D.min(new ArrayList<>());
        }, IllegalArgumentException.class, msg);
    }

    @Test
    void testCentroid() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3),
                Vector3D.centroid(Vector3D.of(1, 2, 3)), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2.5, 3.5, 4.5),
                Vector3D.centroid(Vector3D.of(1, 2, 3), Vector3D.of(2, 3, 4),
                        Vector3D.of(3, 4, 5), Vector3D.of(4, 5, 6)), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, 3),
                Vector3D.centroid(Collections.singletonList(Vector3D.of(1, 2, 3))), EPS);

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0.5, 1, 1.5),
                Vector3D.centroid(Arrays.asList(Vector3D.of(1, 2, 3), Vector3D.of(1, 2, 3),
                        Vector3D.ZERO, Vector3D.ZERO)), EPS);
    }

    @Test
    void testCentroid_noPointsGiven() {
        // arrange
        final String msg = "Cannot compute centroid: no points given";

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Vector3D.centroid(new ArrayList<>());
        }, IllegalArgumentException.class, msg);
    }

    @Test
    void testLinearCombination1() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 2, 3);

        // act/assert
        checkVector(Vector3D.linearCombination(0, p1), 0, 0, 0);

        checkVector(Vector3D.linearCombination(1, p1), 1, 2, 3);
        checkVector(Vector3D.linearCombination(-1, p1), -1, -2, -3);

        checkVector(Vector3D.linearCombination(0.5, p1), 0.5, 1, 1.5);
        checkVector(Vector3D.linearCombination(-0.5, p1), -0.5, -1, -1.5);
    }

    @Test
    void testLinearCombination2() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 2, 3);
        final Vector3D p2 = Vector3D.of(-3, -4, -5);

        // act/assert
        checkVector(Vector3D.linearCombination(2, p1, -3, p2), 11, 16, 21);
        checkVector(Vector3D.linearCombination(-3, p1, 2, p2), -9, -14, -19);
    }

    @Test
    void testLinearCombination3() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 2, 3);
        final Vector3D p2 = Vector3D.of(-3, -4, -5);
        final Vector3D p3 = Vector3D.of(5, 6, 7);

        // act/assert
        checkVector(Vector3D.linearCombination(2, p1, -3, p2, 4, p3), 31, 40, 49);
        checkVector(Vector3D.linearCombination(-3, p1, 2, p2, -4, p3), -29, -38, -47);
    }

    @Test
    void testLinearCombination4() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 2, 3);
        final Vector3D p2 = Vector3D.of(-3, -4, -5);
        final Vector3D p3 = Vector3D.of(5, 6, 7);
        final Vector3D p4 = Vector3D.of(-7, -8, 9);

        // act/assert
        checkVector(Vector3D.linearCombination(2, p1, -3, p2, 4, p3, -5, p4), 66, 80, 4);
        checkVector(Vector3D.linearCombination(-3, p1, 2, p2, -4, p3, 5, p4), -64, -78, -2);
    }

    @Test
    void testUnitFactoryOptimization() {
        // An already normalized vector will avoid unnecessary creation.
        final Vector3D v = Vector3D.of(3, 4, 5).normalize();
        Assertions.assertSame(v, v.normalize());
    }

    private void checkVector(final Vector3D v, final double x, final double y, final double z) {
        Assertions.assertEquals(x, v.getX(), EPS);
        Assertions.assertEquals(y, v.getY(), EPS);
        Assertions.assertEquals(z, v.getZ(), EPS);
    }
}
