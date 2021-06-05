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
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Bounds3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final String NO_POINTS_MESSAGE = "Cannot construct bounds: no points given";

    private static final Pattern INVALID_BOUNDS_PATTERN =
            Pattern.compile("^Invalid bounds: min= \\([^\\)]+\\), max= \\([^\\)]+\\)");

    @Test
    public void testFrom_varargs_singlePoint() {
        // arrange
        final Vector3D p1 = Vector3D.of(-1, 2, -3);

        // act
        final Bounds3D b = Bounds3D.from(p1);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getCentroid(), TEST_EPS);
    }

    @Test
    public void testFrom_varargs_multiplePoints() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 6, 7);
        final Vector3D p2 = Vector3D.of(0, 5, 11);
        final Vector3D p3 = Vector3D.of(3, 6, 8);

        // act
        final Bounds3D b = Bounds3D.from(p1, p2, p3);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 5, 7), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 6, 11), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 1, 4), b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1.5, 5.5, 9), b.getCentroid(), TEST_EPS);
    }

    @Test
    public void testFrom_iterable_singlePoint() {
        // arrange
        final Vector3D p1 = Vector3D.of(-1, 2, -3);

        // act
        final Bounds3D b = Bounds3D.from(Collections.singletonList(p1));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getCentroid(), TEST_EPS);
    }

    @Test
    public void testFrom_iterable_multiplePoints() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 6, 7);
        final Vector3D p2 = Vector3D.of(2, 5, 9);
        final Vector3D p3 = Vector3D.of(3, 4, 8);

        // act
        final Bounds3D b = Bounds3D.from(Arrays.asList(p1, p2, p3));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 4, 7), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 6, 9), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 2, 2), b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2, 5, 8), b.getCentroid(), TEST_EPS);
    }

    @Test
    public void testFrom_iterable_noPoints() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(new ArrayList<>());
        }, IllegalStateException.class, NO_POINTS_MESSAGE);
    }

    @Test
    public void testFrom_invalidBounds() {
        // arrange
        final Vector3D good = Vector3D.of(1, 1, 1);

        final Vector3D nan = Vector3D.of(Double.NaN, 1, 1);
        final Vector3D posInf = Vector3D.of(1, Double.POSITIVE_INFINITY, 1);
        final Vector3D negInf = Vector3D.of(1, 1, Double.NEGATIVE_INFINITY);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(Vector3D.NaN);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(Vector3D.POSITIVE_INFINITY);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(Vector3D.NEGATIVE_INFINITY);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(good, nan);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(posInf, good);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(good, negInf, good);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);
    }

    @Test
    public void testHasSize() {
        // arrange
        final Precision.DoubleEquivalence low = Precision.doubleEquivalenceOfEpsilon(1e-2);
        final Precision.DoubleEquivalence high = Precision.doubleEquivalenceOfEpsilon(1e-10);

        final Vector3D p1 = Vector3D.ZERO;

        final Vector3D p2 = Vector3D.of(1e-5, 1, 1);
        final Vector3D p3 = Vector3D.of(1, 1e-5, 1);
        final Vector3D p4 = Vector3D.of(1, 1, 1e-5);

        final Vector3D p5 = Vector3D.of(1, 1, 1);

        // act/assert
        Assertions.assertFalse(Bounds3D.from(p1).hasSize(high));
        Assertions.assertFalse(Bounds3D.from(p1).hasSize(low));

        Assertions.assertTrue(Bounds3D.from(p1, p2).hasSize(high));
        Assertions.assertFalse(Bounds3D.from(p1, p2).hasSize(low));

        Assertions.assertTrue(Bounds3D.from(p1, p3).hasSize(high));
        Assertions.assertFalse(Bounds3D.from(p1, p3).hasSize(low));

        Assertions.assertTrue(Bounds3D.from(p1, p4).hasSize(high));
        Assertions.assertFalse(Bounds3D.from(p1, p4).hasSize(low));

        Assertions.assertTrue(Bounds3D.from(p1, p5).hasSize(high));
        Assertions.assertTrue(Bounds3D.from(p1, p5).hasSize(low));
    }

    @Test
    public void testContains_strict() {
        // arrange
        final Bounds3D b = Bounds3D.from(
                Vector3D.of(0, 4, 8),
                Vector3D.of(2, 6, 10));

        // act/assert
        assertContainsStrict(b, true,
                b.getCentroid(),
                Vector3D.of(0, 4, 8), Vector3D.of(2, 6, 10),
                Vector3D.of(1, 5, 9),
                Vector3D.of(0, 5, 9), Vector3D.of(2, 5, 9),
                Vector3D.of(1, 4, 9), Vector3D.of(1, 6, 9),
                Vector3D.of(1, 5, 8), Vector3D.of(1, 5, 10));

        assertContainsStrict(b, false,
                Vector3D.ZERO,
                Vector3D.of(-1, 5, 9), Vector3D.of(3, 5, 9),
                Vector3D.of(1, 3, 9), Vector3D.of(1, 7, 9),
                Vector3D.of(1, 5, 7), Vector3D.of(1, 5, 11),
                Vector3D.of(-1e-15, 4, 8), Vector3D.of(2, 6 + 1e-15, 10), Vector3D.of(0, 4, 10 + 1e-15));
    }

    @Test
    public void testContains_precision() {
        // arrange
        final Bounds3D b = Bounds3D.from(
                Vector3D.of(0, 4, 8),
                Vector3D.of(2, 6, 10));

        // act/assert
        assertContainsWithPrecision(b, true,
                b.getCentroid(),
                Vector3D.of(0, 4, 8), Vector3D.of(2, 6, 10),
                Vector3D.of(1, 5, 9),
                Vector3D.of(0, 5, 9), Vector3D.of(2, 5, 9),
                Vector3D.of(1, 4, 9), Vector3D.of(1, 6, 9),
                Vector3D.of(1, 5, 8), Vector3D.of(1, 5, 10),
                Vector3D.of(-1e-15, 4, 8), Vector3D.of(2, 6 + 1e-15, 10), Vector3D.of(0, 4, 10 + 1e-15));

        assertContainsWithPrecision(b, false,
                Vector3D.ZERO,
                Vector3D.of(-1, 5, 9), Vector3D.of(3, 5, 9),
                Vector3D.of(1, 3, 9), Vector3D.of(1, 7, 9),
                Vector3D.of(1, 5, 7), Vector3D.of(1, 5, 11));
    }

    @Test
    public void testIntersects() {
        // arrange
        final Bounds3D b = Bounds3D.from(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // act/assert
        checkIntersects(b, Vector3D::getX, (v, x) -> Vector3D.of(x, v.getY(), v.getZ()));
        checkIntersects(b, Vector3D::getY, (v, y) -> Vector3D.of(v.getX(), y, v.getZ()));
        checkIntersects(b, Vector3D::getZ, (v, z) -> Vector3D.of(v.getX(), v.getY(), z));
    }

    private void checkIntersects(final Bounds3D b, final ToDoubleFunction<? super Vector3D> getter,
                                 final BiFunction<? super Vector3D, Double, ? extends Vector3D> setter) {

        final Vector3D min = b.getMin();
        final Vector3D max = b.getMax();

        final double minValue = getter.applyAsDouble(min);
        final double maxValue = getter.applyAsDouble(max);
        final double midValue = (0.5 * (maxValue - minValue)) + minValue;

        // check all possible interval relationships

        // start below minValue
        Assertions.assertFalse(b.intersects(Bounds3D.from(
                setter.apply(min, minValue - 2), setter.apply(max, minValue - 1))));

        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue - 2), setter.apply(max, minValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue - 2), setter.apply(max, midValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue - 2), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue - 2), setter.apply(max, maxValue + 1))));

        // start on minValue
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue), setter.apply(max, minValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue), setter.apply(max, midValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, minValue), setter.apply(max, maxValue + 1))));

        // start on midValue
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, midValue), setter.apply(max, midValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, midValue), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, midValue), setter.apply(max, maxValue + 1))));

        // start on maxValue
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, maxValue), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds3D.from(
                setter.apply(min, maxValue), setter.apply(max, maxValue + 1))));

        // start above maxValue
        Assertions.assertFalse(b.intersects(Bounds3D.from(
                setter.apply(min, maxValue + 1), setter.apply(max, maxValue + 2))));
    }

    @Test
    public void testIntersection() {
        // -- arrange
        final Bounds3D b = Bounds3D.from(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // -- act/assert

        // move along x-axis
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(-2, 0, 0), Vector3D.of(-1, 1, 1))));
        checkIntersection(b, Vector3D.of(-1, 0, 0), Vector3D.of(0, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(0, 1, 1));
        checkIntersection(b, Vector3D.of(-1, 0, 0), Vector3D.of(0.5, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(0.5, 1, 1));
        checkIntersection(b, Vector3D.of(-1, 0, 0), Vector3D.of(1, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(-1, 0, 0), Vector3D.of(2, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, 0), Vector3D.of(2, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0.5, 0, 0), Vector3D.of(2, 1, 1),
                Vector3D.of(0.5, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(1, 0, 0), Vector3D.of(2, 1, 1),
                Vector3D.of(1, 0, 0), Vector3D.of(1, 1, 1));
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(2, 0, 0), Vector3D.of(3, 1, 1))));

        // move along y-axis
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(0, -2, 0), Vector3D.of(1, -1, 1))));
        checkIntersection(b, Vector3D.of(0, -1, 0), Vector3D.of(1, 0, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 0, 1));
        checkIntersection(b, Vector3D.of(0, -1, 0), Vector3D.of(1, 0.5, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 0.5, 1));
        checkIntersection(b, Vector3D.of(0, -1, 0), Vector3D.of(1, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, -1, 0), Vector3D.of(1, 2, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, 0), Vector3D.of(1, 2, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0.5, 0), Vector3D.of(1, 2, 1),
                Vector3D.of(0, 0.5, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 1, 0), Vector3D.of(1, 2, 1),
                Vector3D.of(0, 1, 0), Vector3D.of(1, 1, 1));
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(0, 2, 0), Vector3D.of(1, 3, 1))));

        // move along z-axis
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(0, 0, -2), Vector3D.of(1, 1, -1))));
        checkIntersection(b, Vector3D.of(0, 0, -1), Vector3D.of(1, 1, 0),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 0));
        checkIntersection(b, Vector3D.of(0, 0, -1), Vector3D.of(1, 1, 0.5),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 0.5));
        checkIntersection(b, Vector3D.of(0, 0, -1), Vector3D.of(1, 1, 1),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, -1), Vector3D.of(1, 1, 2),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 2),
                Vector3D.of(0, 0, 0), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, 0.5), Vector3D.of(1, 1, 2),
                Vector3D.of(0, 0, 0.5), Vector3D.of(1, 1, 1));
        checkIntersection(b, Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 2),
                Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 1));
        Assertions.assertNull(b.intersection(Bounds3D.from(Vector3D.of(0, 0, 2), Vector3D.of(1, 1, 3))));
    }

    private void checkIntersection(final Bounds3D b, final Vector3D a1, final Vector3D a2, final Vector3D r1, final Vector3D r2) {
        final Bounds3D a = Bounds3D.from(a1, a2);
        final Bounds3D result = b.intersection(a);

        checkBounds(result, r1, r2);
    }

    @Test
    public void toRegion() {
        // arrange
        final Bounds3D b = Bounds3D.from(
                Vector3D.of(0, 4, 8),
                Vector3D.of(2, 6, 10));

        // act
        final Parallelepiped p = b.toRegion(TEST_PRECISION);

        // assert
        Assertions.assertEquals(8, p.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 5, 9), p.getCentroid(), TEST_EPS);
    }

    @Test
    public void toRegion_boundingBoxTooSmall() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Bounds3D.from(Vector3D.ZERO, Vector3D.of(1e-12, 1e-12, 1e-12))
                .toRegion(TEST_PRECISION));
    }

    @Test
    public void testEq() {
        // arrange
        final Precision.DoubleEquivalence low = Precision.doubleEquivalenceOfEpsilon(1e-2);
        final Precision.DoubleEquivalence high = Precision.doubleEquivalenceOfEpsilon(1e-10);

        final Bounds3D b1 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        final Bounds3D b2 = Bounds3D.from(Vector3D.of(1.1, 1, 1), Vector3D.of(2, 2, 2));
        final Bounds3D b3 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(1.9, 2, 2));

        final Bounds3D b4 = Bounds3D.from(Vector3D.of(1.001, 1.001, 1.001), Vector3D.of(2.001, 2.001, 2.001));

        // act/assert
        Assertions.assertTrue(b1.eq(b1, low));

        Assertions.assertFalse(b1.eq(b2, low));
        Assertions.assertFalse(b1.eq(b3, low));

        Assertions.assertTrue(b1.eq(b4, low));
        Assertions.assertTrue(b4.eq(b1, low));

        Assertions.assertFalse(b1.eq(b4, high));
        Assertions.assertFalse(b4.eq(b1, high));
    }

    @Test
    public void testHashCode() {
        // arrange
        final Bounds3D b1 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        final Bounds3D b2 = Bounds3D.from(Vector3D.of(-2, 1, 1), Vector3D.of(2, 2, 2));
        final Bounds3D b3 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(3, 2, 2));
        final Bounds3D b4 = Bounds3D.from(Vector3D.of(1 + 1e-15, 1, 1), Vector3D.of(2, 2, 2));
        final Bounds3D b5 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2 + 1e-15, 2, 2));

        final Bounds3D b6 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        // act
        final int hash = b1.hashCode();

        // assert
        Assertions.assertEquals(hash, b1.hashCode());

        Assertions.assertNotEquals(hash, b2.hashCode());
        Assertions.assertNotEquals(hash, b3.hashCode());
        Assertions.assertNotEquals(hash, b4.hashCode());
        Assertions.assertNotEquals(hash, b5.hashCode());

        Assertions.assertEquals(hash, b6.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final Bounds3D b1 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        final Bounds3D b2 = Bounds3D.from(Vector3D.of(-1, 1, 1), Vector3D.of(2, 2, 2));
        final Bounds3D b3 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(3, 2, 2));
        final Bounds3D b4 = Bounds3D.from(Vector3D.of(1 + 1e-15, 1, 1), Vector3D.of(2, 2, 2));
        final Bounds3D b5 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2 + 1e-15, 2, 2));

        final Bounds3D b6 = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(b1);

        Assertions.assertNotEquals(b1, b2);
        Assertions.assertNotEquals(b1, b3);
        Assertions.assertNotEquals(b1, b4);
        Assertions.assertNotEquals(b1, b5);

        Assertions.assertEquals(b1, b6);
    }

    @Test
    public void testToString() {
        // arrange
        final Bounds3D b = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        // act
        final String str = b.toString();

        // assert
        GeometryTestUtils.assertContains("Bounds3D[min= (1", str);
        GeometryTestUtils.assertContains(", max= (2", str);
    }

    @Test
    public void testBuilder_addMethods() {
        // arrange
        final Vector3D p1 = Vector3D.of(1, 10, 11);
        final Vector3D p2 = Vector3D.of(2, 9, 12);
        final Vector3D p3 = Vector3D.of(3, 8, 13);
        final Vector3D p4 = Vector3D.of(4, 7, 14);
        final Vector3D p5 = Vector3D.of(5, 6, 15);

        // act
        final Bounds3D b = Bounds3D.builder()
                .add(p1)
                .addAll(Arrays.asList(p2, p3))
                .add(Bounds3D.from(p4, p5))
                .build();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 6, 11), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(5, 10, 15), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 8, 13), b.getCentroid(), TEST_EPS);
    }

    @Test
    public void testBuilder_hasBounds() {
        // act/assert
        Assertions.assertFalse(Bounds3D.builder().hasBounds());

        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(Double.NaN, 1, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, Double.NaN, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, 1, Double.NaN)).hasBounds());

        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(Double.POSITIVE_INFINITY, 1, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, Double.POSITIVE_INFINITY, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, 1, Double.POSITIVE_INFINITY)).hasBounds());

        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(Double.NEGATIVE_INFINITY, 1, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, Double.NEGATIVE_INFINITY, 1)).hasBounds());
        Assertions.assertFalse(Bounds3D.builder().add(Vector3D.of(1, 1, Double.NEGATIVE_INFINITY)).hasBounds());

        Assertions.assertTrue(Bounds3D.builder().add(Vector3D.ZERO).hasBounds());
    }

    private static void checkBounds(final Bounds3D b, final Vector3D min, final Vector3D max) {
        EuclideanTestUtils.assertCoordinatesEqual(min, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(max, b.getMax(), TEST_EPS);
    }

    private static void assertContainsStrict(final Bounds3D bounds, final boolean contains, final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(contains, bounds.contains(pt), "Unexpected location for point " + pt);
        }
    }

    private static void assertContainsWithPrecision(final Bounds3D bounds, final boolean contains, final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(contains, bounds.contains(pt, TEST_PRECISION), "Unexpected location for point " + pt);
        }
    }
}
