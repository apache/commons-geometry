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
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.shape.Parallelogram;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Bounds2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final String NO_POINTS_MESSAGE = "Cannot construct bounds: no points given";

    private static final Pattern INVALID_BOUNDS_PATTERN =
            Pattern.compile("^Invalid bounds: min= \\([^\\)]+\\), max= \\([^\\)]+\\)");

    @Test
    public void testFrom_varargs_singlePoint() {
        // arrange
        final Vector2D p1 = Vector2D.of(-1, 2);

        // act
        final Bounds2D b = Bounds2D.from(p1);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getCentroid(), TEST_EPS);
    }

    @Test
    public void testFrom_varargs_multiplePoints() {
        // arrange
        final Vector2D p1 = Vector2D.of(1, 6);
        final Vector2D p2 = Vector2D.of(0, 5);
        final Vector2D p3 = Vector2D.of(3, 6);

        // act
        final Bounds2D b = Bounds2D.from(p1, p2, p3);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 5), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 6), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 1), b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 5.5), b.getCentroid(), TEST_EPS);
    }

    @Test
    public void testFrom_iterable_singlePoint() {
        // arrange
        final Vector2D p1 = Vector2D.of(-1, 2);

        // act
        final Bounds2D b = Bounds2D.from(Collections.singletonList(p1));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getCentroid(), TEST_EPS);
    }

    @Test
    public void testFrom_iterable_multiplePoints() {
        // arrange
        final Vector2D p1 = Vector2D.of(1, 6);
        final Vector2D p2 = Vector2D.of(2, 5);
        final Vector2D p3 = Vector2D.of(3, 4);

        // act
        final Bounds2D b = Bounds2D.from(Arrays.asList(p1, p2, p3));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 4), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 6), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 2), b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 5), b.getCentroid(), TEST_EPS);
    }

    @Test
    public void testFrom_iterable_noPoints() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds2D.from(new ArrayList<>());
        }, IllegalStateException.class, NO_POINTS_MESSAGE);
    }

    @Test
    public void testFrom_invalidBounds() {
        // arrange
        final Vector2D good = Vector2D.of(1, 1);

        final Vector2D nan = Vector2D.of(Double.NaN, 1);
        final Vector2D posInf = Vector2D.of(1, Double.POSITIVE_INFINITY);
        final Vector2D negInf = Vector2D.of(1, Double.NEGATIVE_INFINITY);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds2D.from(Vector2D.NaN);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds2D.from(Vector2D.POSITIVE_INFINITY);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds2D.from(Vector2D.NEGATIVE_INFINITY);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds2D.from(good, nan);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds2D.from(posInf, good);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds2D.from(good, negInf, good);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);
    }

    @Test
    public void testHasSize() {
        // arrange
        final Precision.DoubleEquivalence low = Precision.doubleEquivalenceOfEpsilon(1e-2);
        final Precision.DoubleEquivalence high = Precision.doubleEquivalenceOfEpsilon(1e-10);

        final Vector2D p1 = Vector2D.ZERO;

        final Vector2D p2 = Vector2D.of(1e-5, 1);
        final Vector2D p3 = Vector2D.of(1, 1e-5);

        final Vector2D p4 = Vector2D.of(1, 1);

        // act/assert
        Assertions.assertFalse(Bounds2D.from(p1).hasSize(high));
        Assertions.assertFalse(Bounds2D.from(p1).hasSize(low));

        Assertions.assertTrue(Bounds2D.from(p1, p2).hasSize(high));
        Assertions.assertFalse(Bounds2D.from(p1, p2).hasSize(low));

        Assertions.assertTrue(Bounds2D.from(p1, p3).hasSize(high));
        Assertions.assertFalse(Bounds2D.from(p1, p3).hasSize(low));

        Assertions.assertTrue(Bounds2D.from(p1, p4).hasSize(high));
        Assertions.assertTrue(Bounds2D.from(p1, p4).hasSize(low));
    }

    @Test
    public void testContains_strict() {
        // arrange
        final Bounds2D b = Bounds2D.from(
                Vector2D.of(0, 4),
                Vector2D.of(2, 6));

        // act/assert
        assertContainsStrict(b, true,
                b.getCentroid(),
                Vector2D.of(0, 4), Vector2D.of(2, 6),
                Vector2D.of(1, 5),
                Vector2D.of(0, 5), Vector2D.of(2, 5),
                Vector2D.of(1, 4), Vector2D.of(1, 6));

        assertContainsStrict(b, false,
                Vector2D.ZERO,
                Vector2D.of(-1, 5), Vector2D.of(3, 5),
                Vector2D.of(1, 3), Vector2D.of(1, 7),
                Vector2D.of(-1e-15, 4), Vector2D.of(2, 6 + 1e-15));
    }

    @Test
    public void testContains_precision() {
        // arrange
        final Bounds2D b = Bounds2D.from(
                Vector2D.of(0, 4),
                Vector2D.of(2, 6));

        // act/assert
        assertContainsWithPrecision(b, true,
                b.getCentroid(),
                Vector2D.of(1, 5), Vector2D.of(0, 4), Vector2D.of(2, 6),
                Vector2D.of(0, 5), Vector2D.of(2, 5),
                Vector2D.of(1, 4), Vector2D.of(1, 6),
                Vector2D.of(-1e-15, 4), Vector2D.of(2, 6 + 1e-15));

        assertContainsWithPrecision(b, false,
                Vector2D.ZERO,
                Vector2D.of(-1, 5), Vector2D.of(3, 5),
                Vector2D.of(1, 3), Vector2D.of(1, 7));
    }

    @Test
    public void testIntersects() {
        // arrange
        final Bounds2D b = Bounds2D.from(Vector2D.ZERO, Vector2D.of(1, 1));

        // act/assert
        checkIntersects(b, Vector2D::getX, (v, x) -> Vector2D.of(x, v.getY()));
        checkIntersects(b, Vector2D::getY, (v, y) -> Vector2D.of(v.getX(), y));
    }

    private void checkIntersects(final Bounds2D b, final ToDoubleFunction<? super Vector2D> getter,
                                 final BiFunction<? super Vector2D, Double, ? extends Vector2D> setter) {

        final Vector2D min = b.getMin();
        final Vector2D max = b.getMax();

        final double minValue = getter.applyAsDouble(min);
        final double maxValue = getter.applyAsDouble(max);
        final double midValue = (0.5 * (maxValue - minValue)) + minValue;

        // check all possible interval relationships

        // start below minValue
        Assertions.assertFalse(b.intersects(Bounds2D.from(
                setter.apply(min, minValue - 2), setter.apply(max, minValue - 1))));

        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue - 2), setter.apply(max, minValue))));
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue - 2), setter.apply(max, midValue))));
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue - 2), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue - 2), setter.apply(max, maxValue + 1))));

        // start on minValue
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue), setter.apply(max, minValue))));
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue), setter.apply(max, midValue))));
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue), setter.apply(max, maxValue + 1))));

        // start on midValue
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, midValue), setter.apply(max, midValue))));
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, midValue), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, midValue), setter.apply(max, maxValue + 1))));

        // start on maxValue
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, maxValue), setter.apply(max, maxValue))));
        Assertions.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, maxValue), setter.apply(max, maxValue + 1))));

        // start above maxValue
        Assertions.assertFalse(b.intersects(Bounds2D.from(
                setter.apply(min, maxValue + 1), setter.apply(max, maxValue + 2))));
    }

    @Test
    public void testIntersection() {
        // -- arrange
        final Bounds2D b = Bounds2D.from(Vector2D.ZERO, Vector2D.of(1, 1));

        // -- act/assert

        // move along x-axis
        Assertions.assertNull(b.intersection(Bounds2D.from(Vector2D.of(-2, 0), Vector2D.of(-1, 1))));
        checkIntersection(b, Vector2D.of(-1, 0), Vector2D.of(0, 1),
                Vector2D.of(0, 0), Vector2D.of(0, 1));
        checkIntersection(b, Vector2D.of(-1, 0), Vector2D.of(0.5, 1),
                Vector2D.of(0, 0), Vector2D.of(0.5, 1));
        checkIntersection(b, Vector2D.of(-1, 0), Vector2D.of(1, 1),
                Vector2D.of(0, 0), Vector2D.of(1, 1));
        checkIntersection(b, Vector2D.of(-1, 0), Vector2D.of(2, 1),
                Vector2D.of(0, 0), Vector2D.of(1, 1));
        checkIntersection(b, Vector2D.of(0, 0), Vector2D.of(2, 1),
                Vector2D.of(0, 0), Vector2D.of(1, 1));
        checkIntersection(b, Vector2D.of(0.5, 0), Vector2D.of(2, 1),
                Vector2D.of(0.5, 0), Vector2D.of(1, 1));
        checkIntersection(b, Vector2D.of(1, 0), Vector2D.of(2, 1),
                Vector2D.of(1, 0), Vector2D.of(1, 1));
        Assertions.assertNull(b.intersection(Bounds2D.from(Vector2D.of(2, 0), Vector2D.of(3, 1))));

        // move along y-axis
        Assertions.assertNull(b.intersection(Bounds2D.from(Vector2D.of(0, -2), Vector2D.of(1, -1))));
        checkIntersection(b, Vector2D.of(0, -1), Vector2D.of(1, 0),
                Vector2D.of(0, 0), Vector2D.of(1, 0));
        checkIntersection(b, Vector2D.of(0, -1), Vector2D.of(1, 0.5),
                Vector2D.of(0, 0), Vector2D.of(1, 0.5));
        checkIntersection(b, Vector2D.of(0, -1), Vector2D.of(1, 1),
                Vector2D.of(0, 0), Vector2D.of(1, 1));
        checkIntersection(b, Vector2D.of(0, -1), Vector2D.of(1, 2),
                Vector2D.of(0, 0), Vector2D.of(1, 1));
        checkIntersection(b, Vector2D.of(0, 0), Vector2D.of(1, 2),
                Vector2D.of(0, 0), Vector2D.of(1, 1));
        checkIntersection(b, Vector2D.of(0, 0.5), Vector2D.of(1, 2),
                Vector2D.of(0, 0.5), Vector2D.of(1, 1));
        checkIntersection(b, Vector2D.of(0, 1), Vector2D.of(1, 2),
                Vector2D.of(0, 1), Vector2D.of(1, 1));
        Assertions.assertNull(b.intersection(Bounds2D.from(Vector2D.of(0, 2), Vector2D.of(1, 3))));
    }

    private void checkIntersection(final Bounds2D b, final Vector2D a1, final Vector2D a2, final Vector2D r1, final Vector2D r2) {
        final Bounds2D a = Bounds2D.from(a1, a2);
        final Bounds2D result = b.intersection(a);

        checkBounds(result, r1, r2);
    }

    @Test
    public void toRegion() {
        // arrange
        final Bounds2D b = Bounds2D.from(
                Vector2D.of(0, 4),
                Vector2D.of(2, 6));

        // act
        final Parallelogram p = b.toRegion(TEST_PRECISION);

        // assert
        Assertions.assertEquals(4, p.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 5), p.getCentroid(), TEST_EPS);
    }

    @Test
    public void toRegion_boundingBoxTooSmall() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Bounds2D.from(Vector2D.ZERO, Vector2D.of(1e-12, 1e-12)).toRegion(TEST_PRECISION));
    }

    @Test
    public void testEq() {
        // arrange
        final Precision.DoubleEquivalence low = Precision.doubleEquivalenceOfEpsilon(1e-2);
        final Precision.DoubleEquivalence high = Precision.doubleEquivalenceOfEpsilon(1e-10);

        final Bounds2D b1 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

        final Bounds2D b2 = Bounds2D.from(Vector2D.of(1.1, 1), Vector2D.of(2, 2));
        final Bounds2D b3 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(1.9, 2));

        final Bounds2D b4 = Bounds2D.from(Vector2D.of(1.001, 1.001), Vector2D.of(2.001, 2.001));

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
        final Bounds2D b1 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

        final Bounds2D b2 = Bounds2D.from(Vector2D.of(-2, 1), Vector2D.of(2, 2));
        final Bounds2D b3 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(3, 2));
        final Bounds2D b4 = Bounds2D.from(Vector2D.of(1 + 1e-15, 1), Vector2D.of(2, 2));
        final Bounds2D b5 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2 + 1e-15, 2));

        final Bounds2D b6 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

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
        final Bounds2D b1 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

        final Bounds2D b2 = Bounds2D.from(Vector2D.of(-1, 1), Vector2D.of(2, 2));
        final Bounds2D b3 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(3, 2));
        final Bounds2D b4 = Bounds2D.from(Vector2D.of(1 + 1e-15, 1), Vector2D.of(2, 2));
        final Bounds2D b5 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2 + 1e-15, 2));

        final Bounds2D b6 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

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
        final Bounds2D b = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

        // act
        final String str = b.toString();

        // assert
        GeometryTestUtils.assertContains("Bounds2D[min= (1", str);
        GeometryTestUtils.assertContains(", max= (2", str);
    }

    @Test
    public void testBuilder_addMethods() {
        // arrange
        final Vector2D p1 = Vector2D.of(1, 10);
        final Vector2D p2 = Vector2D.of(2, 9);
        final Vector2D p3 = Vector2D.of(3, 8);
        final Vector2D p4 = Vector2D.of(4, 7);
        final Vector2D p5 = Vector2D.of(5, 6);

        // act
        final Bounds2D b = Bounds2D.builder()
                .add(p1)
                .addAll(Arrays.asList(p2, p3))
                .add(Bounds2D.from(p4, p5))
                .build();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 6), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(5, 10), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 8), b.getCentroid(), TEST_EPS);
    }

    @Test
    public void testBuilder_hasBounds() {
        // act/assert
        Assertions.assertFalse(Bounds2D.builder().hasBounds());

        Assertions.assertFalse(Bounds2D.builder().add(Vector2D.of(Double.NaN, 1)).hasBounds());
        Assertions.assertFalse(Bounds2D.builder().add(Vector2D.of(1, Double.NaN)).hasBounds());

        Assertions.assertFalse(Bounds2D.builder().add(Vector2D.of(Double.POSITIVE_INFINITY, 1)).hasBounds());
        Assertions.assertFalse(Bounds2D.builder().add(Vector2D.of(1, Double.POSITIVE_INFINITY)).hasBounds());

        Assertions.assertFalse(Bounds2D.builder().add(Vector2D.of(Double.NEGATIVE_INFINITY, 1)).hasBounds());
        Assertions.assertFalse(Bounds2D.builder().add(Vector2D.of(1, Double.NEGATIVE_INFINITY)).hasBounds());

        Assertions.assertTrue(Bounds2D.builder().add(Vector2D.ZERO).hasBounds());
    }

    private static void checkBounds(final Bounds2D b, final Vector2D min, final Vector2D max) {
        EuclideanTestUtils.assertCoordinatesEqual(min, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(max, b.getMax(), TEST_EPS);
    }

    private static void assertContainsStrict(final Bounds2D bounds, final boolean contains, final Vector2D... pts) {
        for (final Vector2D pt : pts) {
            Assertions.assertEquals(contains, bounds.contains(pt), "Unexpected location for point " + pt);
        }
    }

    private static void assertContainsWithPrecision(final Bounds2D bounds, final boolean contains, final Vector2D... pts) {
        for (final Vector2D pt : pts) {
            Assertions.assertEquals(contains, bounds.contains(pt, TEST_PRECISION), "Unexpected location for point " + pt);
        }
    }
}
