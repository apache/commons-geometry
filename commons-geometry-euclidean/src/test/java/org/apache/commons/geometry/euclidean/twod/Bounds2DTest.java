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
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.shape.Parallelogram;
import org.junit.Assert;
import org.junit.Test;

public class Bounds2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final String NO_POINTS_MESSAGE = "Cannot construct bounds: no points given";

    private static final Pattern INVALID_BOUNDS_PATTERN =
            Pattern.compile("^Invalid bounds: min= \\([^\\)]+\\), max= \\([^\\)]+\\)");

    @Test
    public void testFrom_varargs_singlePoint() {
        // arrange
        Vector2D p1 = Vector2D.of(-1, 2);

        // act
        Bounds2D b = Bounds2D.from(p1);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testFrom_varargs_multiplePoints() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 6);
        Vector2D p2 = Vector2D.of(0, 5);
        Vector2D p3 = Vector2D.of(3, 6);

        // act
        Bounds2D b = Bounds2D.from(p1, p2, p3);

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 5), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 6), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 1), b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 5.5), b.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testFrom_iterable_singlePoint() {
        // arrange
        Vector2D p1 = Vector2D.of(-1, 2);

        // act
        Bounds2D b = Bounds2D.from(Arrays.asList(p1));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(p1, b.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testFrom_iterable_multiplePoints() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 6);
        Vector2D p2 = Vector2D.of(2, 5);
        Vector2D p3 = Vector2D.of(3, 4);

        // act
        Bounds2D b = Bounds2D.from(Arrays.asList(p1, p2, p3));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 4), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 6), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 2), b.getDiagonal(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 5), b.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testFrom_iterable_noPoints() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Bounds2D.from(new ArrayList<>());
        }, IllegalStateException.class, NO_POINTS_MESSAGE);
    }

    @Test
    public void testFrom_invalidBounds() {
        // arrange
        Vector2D good = Vector2D.of(1, 1);

        Vector2D nan = Vector2D.of(Double.NaN, 1);
        Vector2D posInf = Vector2D.of(1, Double.POSITIVE_INFINITY);
        Vector2D negInf = Vector2D.of(1, Double.NEGATIVE_INFINITY);

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Bounds2D.from(Vector2D.NaN);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrows(() -> {
            Bounds2D.from(Vector2D.POSITIVE_INFINITY);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrows(() -> {
            Bounds2D.from(Vector2D.NEGATIVE_INFINITY);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrows(() -> {
            Bounds2D.from(good, nan);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrows(() -> {
            Bounds2D.from(posInf, good);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);

        GeometryTestUtils.assertThrows(() -> {
            Bounds2D.from(good, negInf, good);
        }, IllegalStateException.class, INVALID_BOUNDS_PATTERN);
    }

    @Test
    public void testHasSize() {
        // arrange
        DoublePrecisionContext low = new EpsilonDoublePrecisionContext(1e-2);
        DoublePrecisionContext high = new EpsilonDoublePrecisionContext(1e-10);

        Vector2D p1 = Vector2D.ZERO;

        Vector2D p2 = Vector2D.of(1e-5, 1);
        Vector2D p3 = Vector2D.of(1, 1e-5);

        Vector2D p4 = Vector2D.of(1, 1);

        // act/assert
        Assert.assertFalse(Bounds2D.from(p1).hasSize(high));
        Assert.assertFalse(Bounds2D.from(p1).hasSize(low));

        Assert.assertTrue(Bounds2D.from(p1, p2).hasSize(high));
        Assert.assertFalse(Bounds2D.from(p1, p2).hasSize(low));

        Assert.assertTrue(Bounds2D.from(p1, p3).hasSize(high));
        Assert.assertFalse(Bounds2D.from(p1, p3).hasSize(low));

        Assert.assertTrue(Bounds2D.from(p1, p4).hasSize(high));
        Assert.assertTrue(Bounds2D.from(p1, p4).hasSize(low));
    }

    @Test
    public void testContains_strict() {
        // arrange
        Bounds2D b = Bounds2D.from(
                Vector2D.of(0, 4),
                Vector2D.of(2, 6));

        // act/assert
        assertContainsStrict(b, true,
                b.getBarycenter(),
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
        Bounds2D b = Bounds2D.from(
                Vector2D.of(0, 4),
                Vector2D.of(2, 6));

        // act/assert
        assertContainsWithPrecision(b, true,
                b.getBarycenter(),
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
        Bounds2D b = Bounds2D.from(Vector2D.ZERO, Vector2D.of(1, 1));

        // act/assert
        checkIntersects(b, Vector2D::getX, (v, x) -> Vector2D.of(x, v.getY()));
        checkIntersects(b, Vector2D::getY, (v, y) -> Vector2D.of(v.getX(), y));
    }

    private void checkIntersects(Bounds2D b, ToDoubleFunction<Vector2D> getter,
            BiFunction<Vector2D, Double, Vector2D> setter) {

        Vector2D min = b.getMin();
        Vector2D max = b.getMax();

        double minValue = getter.applyAsDouble(min);
        double maxValue = getter.applyAsDouble(max);
        double midValue = (0.5 * (maxValue - minValue)) + minValue;

        // check all possible interval relationships

        // start below minValue
        Assert.assertFalse(b.intersects(Bounds2D.from(
                setter.apply(min, minValue - 2), setter.apply(max, minValue - 1))));

        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue - 2), setter.apply(max, minValue))));
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue - 2), setter.apply(max, midValue))));
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue - 2), setter.apply(max, maxValue))));
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue - 2), setter.apply(max, maxValue + 1))));

        // start on minValue
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue), setter.apply(max, minValue))));
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue), setter.apply(max, midValue))));
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue), setter.apply(max, maxValue))));
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, minValue), setter.apply(max, maxValue + 1))));

        // start on midValue
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, midValue), setter.apply(max, midValue))));
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, midValue), setter.apply(max, maxValue))));
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, midValue), setter.apply(max, maxValue + 1))));

        // start on maxValue
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, maxValue), setter.apply(max, maxValue))));
        Assert.assertTrue(b.intersects(Bounds2D.from(
                setter.apply(min, maxValue), setter.apply(max, maxValue + 1))));

        // start above maxValue
        Assert.assertFalse(b.intersects(Bounds2D.from(
                setter.apply(min, maxValue + 1), setter.apply(max, maxValue + 2))));
    }

    @Test
    public void testIntersection() {
        // -- arrange
        Bounds2D b = Bounds2D.from(Vector2D.ZERO, Vector2D.of(1, 1));

        // -- act/assert

        // move along x-axis
        Assert.assertNull(b.intersection(Bounds2D.from(Vector2D.of(-2, 0), Vector2D.of(-1, 1))));
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
        Assert.assertNull(b.intersection(Bounds2D.from(Vector2D.of(2, 0), Vector2D.of(3, 1))));

        // move along y-axis
        Assert.assertNull(b.intersection(Bounds2D.from(Vector2D.of(0, -2), Vector2D.of(1, -1))));
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
        Assert.assertNull(b.intersection(Bounds2D.from(Vector2D.of(0, 2), Vector2D.of(1, 3))));
    }

    private void checkIntersection(Bounds2D b, Vector2D a1, Vector2D a2, Vector2D r1, Vector2D r2) {
        Bounds2D a = Bounds2D.from(a1, a2);
        Bounds2D result = b.intersection(a);

        checkBounds(result, r1, r2);
    }

    @Test
    public void toRegion() {
        // arrange
        Bounds2D b = Bounds2D.from(
                Vector2D.of(0, 4),
                Vector2D.of(2, 6));

        // act
        Parallelogram p = b.toRegion(TEST_PRECISION);

        // assert
        Assert.assertEquals(4, p.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 5), p.getBarycenter(), TEST_EPS);
    }

    @Test
    public void toRegion_boundingBoxTooSmall() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Bounds2D.from(Vector2D.ZERO, Vector2D.of(1e-12, 1e-12))
                .toRegion(TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testEq() {
        // arrange
        DoublePrecisionContext low = new EpsilonDoublePrecisionContext(1e-2);
        DoublePrecisionContext high = new EpsilonDoublePrecisionContext(1e-10);

        Bounds2D b1 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

        Bounds2D b2 = Bounds2D.from(Vector2D.of(1.1, 1), Vector2D.of(2, 2));
        Bounds2D b3 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(1.9, 2));

        Bounds2D b4 = Bounds2D.from(Vector2D.of(1.001, 1.001), Vector2D.of(2.001, 2.001));

        // act/assert
        Assert.assertTrue(b1.eq(b1, low));

        Assert.assertFalse(b1.eq(b2, low));
        Assert.assertFalse(b1.eq(b3, low));

        Assert.assertTrue(b1.eq(b4, low));
        Assert.assertTrue(b4.eq(b1, low));

        Assert.assertFalse(b1.eq(b4, high));
        Assert.assertFalse(b4.eq(b1, high));
    }

    @Test
    public void testHashCode() {
        // arrange
        Bounds2D b1 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

        Bounds2D b2 = Bounds2D.from(Vector2D.of(-2, 1), Vector2D.of(2, 2));
        Bounds2D b3 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(3, 2));
        Bounds2D b4 = Bounds2D.from(Vector2D.of(1 + 1e-15, 1), Vector2D.of(2, 2));
        Bounds2D b5 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2 + 1e-15, 2));

        Bounds2D b6 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

        // act
        int hash = b1.hashCode();

        // assert
        Assert.assertEquals(hash, b1.hashCode());

        Assert.assertNotEquals(hash, b2.hashCode());
        Assert.assertNotEquals(hash, b3.hashCode());
        Assert.assertNotEquals(hash, b4.hashCode());
        Assert.assertNotEquals(hash, b5.hashCode());

        Assert.assertEquals(hash, b6.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        Bounds2D b1 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

        Bounds2D b2 = Bounds2D.from(Vector2D.of(-1, 1), Vector2D.of(2, 2));
        Bounds2D b3 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(3, 2));
        Bounds2D b4 = Bounds2D.from(Vector2D.of(1 + 1e-15, 1), Vector2D.of(2, 2));
        Bounds2D b5 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2 + 1e-15, 2));

        Bounds2D b6 = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

        // act/assert
        Assert.assertTrue(b1.equals(b1));

        Assert.assertFalse(b1.equals(null));
        Assert.assertFalse(b1.equals(new Object()));

        Assert.assertFalse(b1.equals(b2));
        Assert.assertFalse(b1.equals(b3));
        Assert.assertFalse(b1.equals(b4));
        Assert.assertFalse(b1.equals(b5));

        Assert.assertTrue(b1.equals(b6));
    }

    @Test
    public void testToString() {
        // arrange
        Bounds2D b = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

        // act
        String str = b.toString();

        // assert
        GeometryTestUtils.assertContains("Bounds2D[min= (1", str);
        GeometryTestUtils.assertContains(", max= (2", str);
    }

    @Test
    public void testBuilder_addMethods() {
        // arrange
        Vector2D p1 = Vector2D.of(1, 10);
        Vector2D p2 = Vector2D.of(2, 9);
        Vector2D p3 = Vector2D.of(3, 8);
        Vector2D p4 = Vector2D.of(4, 7);
        Vector2D p5 = Vector2D.of(5, 6);

        // act
        Bounds2D b = Bounds2D.builder()
                .add(p1)
                .addAll(Arrays.asList(p2, p3))
                .add(Bounds2D.from(p4, p5))
                .build();

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 6), b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(5, 10), b.getMax(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 8), b.getBarycenter(), TEST_EPS);
    }

    @Test
    public void testBuilder_containsBounds() {
        // act/assert
        Assert.assertFalse(Bounds2D.builder().containsBounds());

        Assert.assertFalse(Bounds2D.builder().add(Vector2D.of(Double.NaN, 1)).containsBounds());
        Assert.assertFalse(Bounds2D.builder().add(Vector2D.of(1, Double.NaN)).containsBounds());

        Assert.assertFalse(Bounds2D.builder().add(Vector2D.of(Double.POSITIVE_INFINITY, 1)).containsBounds());
        Assert.assertFalse(Bounds2D.builder().add(Vector2D.of(1, Double.POSITIVE_INFINITY)).containsBounds());

        Assert.assertFalse(Bounds2D.builder().add(Vector2D.of(Double.NEGATIVE_INFINITY, 1)).containsBounds());
        Assert.assertFalse(Bounds2D.builder().add(Vector2D.of(1, Double.NEGATIVE_INFINITY)).containsBounds());

        Assert.assertTrue(Bounds2D.builder().add(Vector2D.ZERO).containsBounds());
    }

    private static void checkBounds(Bounds2D b, Vector2D min, Vector2D max) {
        EuclideanTestUtils.assertCoordinatesEqual(min, b.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(max, b.getMax(), TEST_EPS);
    }

    private static void assertContainsStrict(Bounds2D bounds, boolean contains, Vector2D... pts) {
        for (Vector2D pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, contains, bounds.contains(pt));
        }
    }

    private static void assertContainsWithPrecision(Bounds2D bounds, boolean contains, Vector2D... pts) {
        for (Vector2D pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, contains, bounds.contains(pt, TEST_PRECISION));
        }
    }
}
