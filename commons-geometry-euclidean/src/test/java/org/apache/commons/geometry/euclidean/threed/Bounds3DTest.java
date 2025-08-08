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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;
import java.util.regex.Pattern;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.threed.line.LinecastPoint3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.line.Segment3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Bounds3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final String NO_POINTS_MESSAGE = "Cannot construct bounds: no points given";

    private static final Pattern INVALID_BOUNDS_PATTERN =
            Pattern.compile("^Invalid bounds: min= \\([^\\)]+\\), max= \\([^\\)]+\\)");

    @Test
    void testFrom_varargs_singlePoint() {
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
    void testFrom_varargs_multiplePoints() {
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
    void testFrom_iterable_singlePoint() {
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
    void testFrom_iterable_multiplePoints() {
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
    void testFrom_iterable_noPoints() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds3D.from(new ArrayList<>());
        }, IllegalStateException.class, NO_POINTS_MESSAGE);
    }

    @Test
    void testFrom_invalidBounds() {
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
    void testHasSize() {
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
    void testContains_strict() {
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
    void testContains_precision() {
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
    void testIntersects() {
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
    void testIntersection() {
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
    void toRegion() {
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
    void toRegion_boundingBoxTooSmall() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Bounds3D.from(Vector3D.ZERO, Vector3D.of(1e-12, 1e-12, 1e-12))
                .toRegion(TEST_PRECISION));
    }

    @Test
    void testEq() {
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
    void testLinecast_intersectsFace() {
        // -- arrange
        // use unequal face sizes so that our test lines do not end up passing through
        // a vertex on the opposite side
        final Bounds3D bounds = Bounds3D.from(Vector3D.of(-0.9, -2, -3), Vector3D.of(0.9, 2, 3));

        // -- act/assert
        checkLinecastIntersectingFace(bounds, Vector3D.of(0.9, 0, 0), Vector3D.Unit.PLUS_X);
        checkLinecastIntersectingFace(bounds, Vector3D.of(-0.9, 0, 0), Vector3D.Unit.MINUS_X);

        checkLinecastIntersectingFace(bounds, Vector3D.of(0, 2, 0), Vector3D.Unit.PLUS_Y);
        checkLinecastIntersectingFace(bounds, Vector3D.of(0, -2, 0), Vector3D.Unit.MINUS_Y);

        checkLinecastIntersectingFace(bounds, Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_Z);
        checkLinecastIntersectingFace(bounds, Vector3D.of(0, 0, -3), Vector3D.Unit.MINUS_Z);
    }

    private void checkLinecastIntersectingFace(
            final Bounds3D bounds,
            final Vector3D facePt,
            final Vector3D normal) {

        // -- arrange
        final Vector3D offset = normal.multiply(1.2);
        final Parallelepiped region = bounds.toRegion(TEST_PRECISION);

        EuclideanTestUtils.permute(-1, 1, 0.5, (x, y, z) -> {
            final Vector3D otherPt = facePt
                    .add(Vector3D.of(x, y, z))
                    .add(offset);
            final Line3D line = Lines3D.fromPoints(otherPt, facePt, TEST_PRECISION);

            final LinecastPoint3D reversePt = region.linecastFirst(line.reverse());

            // -- act/assert
            linecastChecker(bounds)
                .expect(facePt, normal)
                .and(reversePt.getPoint(), reversePt.getNormal())
                .whenGiven(line);

            linecastChecker(bounds)
                .and(reversePt.getPoint(), reversePt.getNormal())
                .expect(facePt, normal)
                .whenGiven(line.reverse());
        });
    }

    @Test
    void testLinecast_intersectsSingleVertex() {
        // -- arrange
        final Bounds3D bounds = Bounds3D.from(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // -- act/assert
        checkLinecastIntersectingSingleVertex(bounds, Vector3D.ZERO);
        checkLinecastIntersectingSingleVertex(bounds, Vector3D.of(0, 0, 1));
        checkLinecastIntersectingSingleVertex(bounds, Vector3D.of(0, 1, 0));
        checkLinecastIntersectingSingleVertex(bounds, Vector3D.of(0, 1, 1));
        checkLinecastIntersectingSingleVertex(bounds, Vector3D.of(1, 0, 0));
        checkLinecastIntersectingSingleVertex(bounds, Vector3D.of(1, 0, 1));
        checkLinecastIntersectingSingleVertex(bounds, Vector3D.of(1, 1, 0));
        checkLinecastIntersectingSingleVertex(bounds, Vector3D.of(1, 1, 1));
    }

    private void checkLinecastIntersectingSingleVertex(
            final Bounds3D bounds,
            final Vector3D vertex) {

        // -- arrange
        final Vector3D centerToVertex = vertex.subtract(bounds.getCentroid()).normalize();
        final Vector3D baseLineDir = centerToVertex.orthogonal();

        final int runCnt = 10;
        for (double a = 0; a < Angle.TWO_PI; a += Angle.TWO_PI / runCnt) {

            // construct a line orthogonal to the vector from the bounds center to the vertex and passing
            // through the vertex
            final Vector3D lineDir = QuaternionRotation.fromAxisAngle(centerToVertex, a).apply(baseLineDir);
            final Line3D line = Lines3D.fromPointAndDirection(vertex, lineDir, TEST_PRECISION);

            // construct possible normals for this vertex
            final List<Vector3D> normals = new ArrayList<>();
            normals.add(centerToVertex.project(Vector3D.Unit.PLUS_X).normalize());
            normals.add(centerToVertex.project(Vector3D.Unit.PLUS_Y).normalize());
            normals.add(centerToVertex.project(Vector3D.Unit.PLUS_Z).normalize());

            normals.sort(Vector3D.COORDINATE_ASCENDING_ORDER);

            // create the checker and populate it with the normals of faces that are not parallel to
            // the line
            final BoundsLinecastChecker3D checker = linecastChecker(bounds);
            for (final Vector3D normal : normals) {
                if (!TEST_PRECISION.eqZero(normal.dot(lineDir))) {
                    checker.expect(vertex, normal);
                }
            }

            // -- act/assert
            checker.whenGiven(line)
                .whenGiven(line.reverse());
        }
    }

    @Test
    void testLinecast_vertexToVertex() {
        // -- arrange
        final Vector3D min = Vector3D.ZERO;
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Bounds3D bounds = Bounds3D.from(min, max);
        final Line3D line = Lines3D.fromPoints(min, max, TEST_PRECISION);

        // -- act/assert
        linecastChecker(bounds)
            .expect(min, Vector3D.Unit.MINUS_X)
            .and(min, Vector3D.Unit.MINUS_Y)
            .and(min, Vector3D.Unit.MINUS_Z)
            .and(max, Vector3D.Unit.PLUS_Z)
            .and(max, Vector3D.Unit.PLUS_Y)
            .and(max, Vector3D.Unit.PLUS_X)
            .whenGiven(line);
    }

    @Test
    void testLinecast_edgeToEdge() {
        // -- arrange
        final Vector3D min = Vector3D.of(0, 0, 0);
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Bounds3D bounds = Bounds3D.from(min, max);

        final Vector3D start = Vector3D.of(0, 0, 0.5);
        final Vector3D end = Vector3D.of(1, 1, 0.5);

        final Line3D line = Lines3D.fromPoints(start, end, TEST_PRECISION);

        // -- act/assert
        linecastChecker(bounds)
            .expect(start, Vector3D.Unit.MINUS_X)
            .and(start, Vector3D.Unit.MINUS_Y)
            .and(end, Vector3D.Unit.PLUS_Y)
            .and(end, Vector3D.Unit.PLUS_X)
            .whenGiven(line);
    }

    @Test
    void testLinecast_alongFace() {
        // -- arrange
        final Vector3D min = Vector3D.ZERO;
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Bounds3D bounds = Bounds3D.from(min, max);

        final int cnt = 10;
        for (double x = min.getX();
                x <= max.getX();
                x += bounds.getDiagonal().getX() / cnt) {

            final Vector3D start = Vector3D.of(x, min.getY(), max.getZ());
            final Vector3D end = Vector3D.of(x, max.getY(), max.getZ());

            final Line3D line = Lines3D.fromPoints(start, end, TEST_PRECISION);

            // -- act/assert
            linecastChecker(bounds)
                .expect(start, Vector3D.Unit.MINUS_Y)
                .and(end, Vector3D.Unit.PLUS_Y)
                .whenGiven(line);
        }
    }

    @Test
    void testLinecast_noIntersection() {
        // -- arrange
        final Bounds3D bounds = Bounds3D.from(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        // -- act/assert
        checkLinecastNoIntersection(bounds, Vector3D.ZERO);
        checkLinecastNoIntersection(bounds, Vector3D.of(0, 0, 1));
        checkLinecastNoIntersection(bounds, Vector3D.of(0, 1, 0));
        checkLinecastNoIntersection(bounds, Vector3D.of(0, 1, 1));
        checkLinecastNoIntersection(bounds, Vector3D.of(1, 0, 0));
        checkLinecastNoIntersection(bounds, Vector3D.of(1, 0, 1));
        checkLinecastNoIntersection(bounds, Vector3D.of(1, 1, 0));
        checkLinecastNoIntersection(bounds, Vector3D.of(1, 1, 1));
    }

    private void checkLinecastNoIntersection(
            final Bounds3D bounds,
            final Vector3D vertex) {

        // -- arrange
        final Vector3D toVertex = bounds.getCentroid().directionTo(vertex);
        final Vector3D baseLineDir = toVertex.orthogonal();

        final Vector3D offsetVertex = vertex.add(toVertex);

        final Line3D plusXLine = Lines3D.fromPointAndDirection(offsetVertex, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final Line3D plusYLine = Lines3D.fromPointAndDirection(offsetVertex, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        final Line3D plusZLine = Lines3D.fromPointAndDirection(offsetVertex, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        final BoundsLinecastChecker3D emptyChecker = linecastChecker(bounds)
                .expectNothing();

        // -- act/assert
        // check axis-aligned lines
        emptyChecker
            .whenGiven(plusXLine)
            .whenGiven(plusYLine)
            .whenGiven(plusZLine);

        // check lines orthogonal to the axis
        final int runCnt = 10;
        for (double a = 0; a < Angle.TWO_PI; a += Angle.TWO_PI / runCnt) {
            final Vector3D lineDir = QuaternionRotation.fromAxisAngle(toVertex, a).apply(baseLineDir);
            final Line3D line = Lines3D.fromPointAndDirection(offsetVertex, lineDir, TEST_PRECISION);

            emptyChecker.whenGiven(line);
        }
    }

    @Test
    void testLinecast_nonSpan() {
        // -- arrange
        final Vector3D min = Vector3D.ZERO;
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Bounds3D bounds = Bounds3D.from(min, max);

        final Vector3D centroid = bounds.getCentroid();

        final Vector3D start = Vector3D.of(max.getX(), centroid.getY(), centroid.getZ());
        final Vector3D end = Vector3D.of(min.getX(), centroid.getY(), centroid.getZ());

        final Line3D line = Lines3D.fromPoints(start, end, TEST_PRECISION);

        // -- act/assert
        linecastChecker(bounds)
            .expect(end, Vector3D.Unit.MINUS_X)
            .whenGiven(line.rayFrom(-0.5));

        linecastChecker(bounds)
            .expect(start, Vector3D.Unit.PLUS_X)
            .whenGiven(line.reverseRayTo(-0.5));

        linecastChecker(bounds)
            .expectNothing()
            .whenGiven(line.segment(-0.9, -0.1));

        linecastChecker(bounds)
            .expect(end, Vector3D.Unit.MINUS_X)
            .whenGiven(line.segment(-0.9, 0.1));

        linecastChecker(bounds)
            .expect(start, Vector3D.Unit.PLUS_X)
            .whenGiven(line.segment(-1.1, -0.1));

        linecastChecker(bounds)
            .expect(start, Vector3D.Unit.PLUS_X)
            .expect(end, Vector3D.Unit.MINUS_X)
            .whenGiven(line.segment(-1.1, 0.1));
    }

    @Test
    void testLinecast_subsetEndpointOnBounds() {
        // -- arrange
        final Vector3D min = Vector3D.ZERO;
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Bounds3D bounds = Bounds3D.from(min, max);

        final Vector3D centroid = bounds.getCentroid();

        final Vector3D start = Vector3D.of(max.getX(), centroid.getY(), centroid.getZ());
        final Vector3D end = Vector3D.of(min.getX(), centroid.getY(), centroid.getZ());

        final Line3D line = Lines3D.fromPoints(start, end, TEST_PRECISION);

        // -- act/assert
        linecastChecker(bounds)
            .expect(end, Vector3D.Unit.MINUS_X)
            .whenGiven(line.rayFrom(0));

        linecastChecker(bounds)
            .expect(end, Vector3D.Unit.MINUS_X)
            .whenGiven(line.segment(0, 1));

        linecastChecker(bounds)
            .expect(start, Vector3D.Unit.PLUS_X)
            .whenGiven(line.reverseRayTo(-1));

        linecastChecker(bounds)
            .expect(start, Vector3D.Unit.PLUS_X)
            .whenGiven(line.segment(-2, -1));
    }

    @Test
    void testLinecast_usesLinePrecision() {
        // -- arrange
        final double withinEps = 0.9 * TEST_EPS;
        final double outsideEps = 1.1 * TEST_EPS;

        final Vector3D min = Vector3D.ZERO;
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Bounds3D bounds = Bounds3D.from(min, max);

        final Vector3D centroid = bounds.getCentroid();

        final Vector3D centerStart = Vector3D.of(max.getX(), centroid.getY(), centroid.getZ());
        final Vector3D centerEnd = Vector3D.of(min.getX(), centroid.getY(), centroid.getZ());

        final Line3D centerLine = Lines3D.fromPoints(centerStart, centerEnd, TEST_PRECISION);

        final Vector3D faceStart = Vector3D.of(max.getX() + withinEps, max.getY() + withinEps, min.getZ());
        final Vector3D faceEnd = Vector3D.of(max.getX() + withinEps, max.getY() + withinEps, max.getZ());

        final Line3D faceLine = Lines3D.fromPoints(faceStart, faceEnd, TEST_PRECISION);

        // -- act/assert
        linecastChecker(bounds)
            .expect(centerEnd, Vector3D.Unit.MINUS_X)
            .whenGiven(centerLine.rayFrom(withinEps));

        linecastChecker(bounds)
            .expectNothing()
            .whenGiven(centerLine.rayFrom(outsideEps));

        linecastChecker(bounds)
            .expect(centerStart, Vector3D.Unit.PLUS_X)
            .expect(centerEnd, Vector3D.Unit.MINUS_X)
            .whenGiven(centerLine.segment(-1 + withinEps, -withinEps));

        linecastChecker(bounds)
            .expectNothing()
            .whenGiven(centerLine.segment(-1 + outsideEps, -outsideEps));

        linecastChecker(bounds)
            .expect(faceStart, Vector3D.Unit.MINUS_Z)
            .expect(faceEnd, Vector3D.Unit.PLUS_Z)
            .whenGiven(faceLine.segment(withinEps, 1 - withinEps));

        linecastChecker(bounds)
            .expectNothing()
            .whenGiven(faceLine.segment(outsideEps, 1 - outsideEps));
    }

    @Test
    void testLinecast_boundsHasNoSize() {
        // -- arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);

        final Bounds3D bounds = Bounds3D.from(pt, pt);

        final Line3D diagonalLine = Lines3D.fromPointAndDirection(pt, Vector3D.of(1, 1, 1), TEST_PRECISION);

        final Line3D plusXLine = Lines3D.fromPointAndDirection(pt, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // -- act/assert
        linecastChecker(bounds)
            .expect(pt, Vector3D.Unit.MINUS_X)
            .expect(pt, Vector3D.Unit.MINUS_Y)
            .expect(pt, Vector3D.Unit.MINUS_Z)
            .expect(pt, Vector3D.Unit.PLUS_Z)
            .expect(pt, Vector3D.Unit.PLUS_Y)
            .expect(pt, Vector3D.Unit.PLUS_X)
            .whenGiven(diagonalLine);

        linecastChecker(bounds)
            .expect(pt, Vector3D.Unit.MINUS_X)
            .expect(pt, Vector3D.Unit.PLUS_X)
            .whenGiven(plusXLine);
    }

    @Test
    void testLineIntersection() {
        // -- arrange
        final Vector3D min = Vector3D.ZERO;
        final Vector3D max = Vector3D.of(1, 1, 1);

        final Vector3D insideMin = Vector3D.of(0.1, 0.1, 0.1);
        final Vector3D insideMax = Vector3D.of(0.9, 0.9, 0.9);

        final Vector3D outsideMin = Vector3D.of(-0.1, -0.1, -0.1);
        final Vector3D outsideMax = Vector3D.of(1.1, 1.1, 1.1);

        final Bounds3D bounds = Bounds3D.from(min, max);

        final Line3D diagonal = Lines3D.fromPoints(min, max, TEST_PRECISION);

        // -- act/assert
        assertLineIntersection(bounds, diagonal, min, max);

        assertLineIntersection(bounds, diagonal.segment(outsideMin, outsideMax), min, max);
        assertLineIntersection(bounds, diagonal.segment(outsideMin, insideMax), min, insideMax);
        assertLineIntersection(bounds, diagonal.segment(insideMin, outsideMax), insideMin, max);
        assertLineIntersection(bounds, diagonal.segment(insideMin, insideMax), insideMin, insideMax);

        assertLineIntersection(bounds, diagonal.rayFrom(min), min, max);
        assertLineIntersection(bounds, diagonal.reverseRayTo(min), min, min);

        assertLineIntersection(bounds, diagonal.rayFrom(max), max, max);
        assertLineIntersection(bounds, diagonal.reverseRayTo(max), min, max);

        assertLineIntersection(bounds, diagonal.rayFrom(insideMax), insideMax, max);
        assertLineIntersection(bounds, diagonal.reverseRayTo(insideMax), min, insideMax);
    }

    @Test
    void testLineIntersection_noIntersection() {
        // -- arrange
        final Bounds3D bounds = Bounds3D.from(Vector3D.ZERO, Vector3D.of(1, 1, 1));

        final Line3D plusXLine =
                Lines3D.fromPointAndDirection(bounds.getCentroid(), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // -- act/assert
        checkLineNoIntersection(bounds, Vector3D.ZERO);
        checkLineNoIntersection(bounds, Vector3D.of(0, 0, 1));
        checkLineNoIntersection(bounds, Vector3D.of(0, 1, 0));
        checkLineNoIntersection(bounds, Vector3D.of(0, 1, 1));
        checkLineNoIntersection(bounds, Vector3D.of(1, 0, 0));
        checkLineNoIntersection(bounds, Vector3D.of(1, 0, 1));
        checkLineNoIntersection(bounds, Vector3D.of(1, 1, 0));
        checkLineNoIntersection(bounds, Vector3D.of(1, 1, 1));

        assertNoLineIntersection(bounds, plusXLine.segment(-0.2, -0.1));
        assertNoLineIntersection(bounds, plusXLine.reverseRayTo(-0.1));

        assertNoLineIntersection(bounds, plusXLine.segment(1.1, 1.2));
        assertNoLineIntersection(bounds, plusXLine.rayFrom(1.1));
    }

    private void checkLineNoIntersection(
            final Bounds3D bounds,
            final Vector3D vertex) {

        // -- arrange
        final Vector3D toVertex = bounds.getCentroid().directionTo(vertex);
        final Vector3D baseLineDir = toVertex.orthogonal();

        final Vector3D offsetVertex = vertex.add(toVertex);

        final Line3D plusXLine = Lines3D.fromPointAndDirection(offsetVertex, Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final Line3D plusYLine = Lines3D.fromPointAndDirection(offsetVertex, Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        final Line3D plusZLine = Lines3D.fromPointAndDirection(offsetVertex, Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // -- act/assert
        // check axis-aligned lines
        assertNoLineIntersection(bounds, plusXLine);
        assertNoLineIntersection(bounds, plusYLine);
        assertNoLineIntersection(bounds, plusZLine);

        // check lines orthogonal to the axis
        final int runCnt = 10;
        for (double a = 0; a < Angle.TWO_PI; a += Angle.TWO_PI / runCnt) {
            final Vector3D lineDir = QuaternionRotation.fromAxisAngle(toVertex, a).apply(baseLineDir);
            final Line3D line = Lines3D.fromPointAndDirection(offsetVertex, lineDir, TEST_PRECISION);

            assertNoLineIntersection(bounds, line);
        }
    }

    @Test
    void testLineIntersection_boundsHasNoSize() {
        // -- arrange
        final Vector3D pt = Vector3D.of(1, 2, 3);

        final Bounds3D bounds = Bounds3D.from(pt, pt);

        final Line3D plusXLine = Lines3D.fromPointAndDirection(pt, Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // -- act/assert
        assertLineIntersection(bounds, plusXLine, pt, pt);
        assertLineIntersection(bounds, plusXLine.rayFrom(pt), pt, pt);
    }

    @Test
    void testLineIntersection_lineAlmostParallel() {
        // -- arrange
        final Vector3D min = Vector3D.of(1e150, -1, -1);
        final Vector3D max = Vector3D.of(1.1e150, 1, 1);

        final Bounds3D bounds = Bounds3D.from(min, max);

        final Vector3D lineDir = Vector3D.of(1, -5e-11, 0);
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.ZERO, lineDir, TEST_PRECISION);

        // -- act
        assertNoLineIntersection(bounds, line);
    }

    @Test
    void testHashCode() {
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
    void testEquals() {
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
    void testToString() {
        // arrange
        final Bounds3D b = Bounds3D.from(Vector3D.of(1, 1, 1), Vector3D.of(2, 2, 2));

        // act
        final String str = b.toString();

        // assert
        GeometryTestUtils.assertContains("Bounds3D[min= (1", str);
        GeometryTestUtils.assertContains(", max= (2", str);
    }

    @Test
    void testBuilder_addMethods() {
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
    void testBuilder_hasBounds() {
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

    private static void assertContainsStrict(
            final Bounds3D bounds,
            final boolean contains,
            final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(contains, bounds.contains(pt), "Unexpected location for point " + pt);
        }
    }

    private static void assertContainsWithPrecision(
            final Bounds3D bounds,
            final boolean contains,
            final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(contains, bounds.contains(pt, TEST_PRECISION), "Unexpected location for point " + pt);
        }
    }

    private static void assertLineIntersection(
            final Bounds3D bounds,
            final Line3D line,
            final Vector3D start,
            final Vector3D end) {
        final Segment3D segment = bounds.intersection(line);

        Assertions.assertSame(line, segment.getLine());
        assertSegment(segment, start, end);

        Assertions.assertTrue(bounds.intersects(line));
    }

    private static void assertLineIntersection(
            final Bounds3D bounds,
            final LineConvexSubset3D subset,
            final Vector3D start,
            final Vector3D end) {
        final Segment3D segment = bounds.intersection(subset);

        Assertions.assertSame(subset.getLine(), segment.getLine());
        assertSegment(segment, start, end);

        Assertions.assertTrue(bounds.intersects(subset));
    }

    private static void assertNoLineIntersection(
            final Bounds3D bounds,
            final Line3D line) {
        Assertions.assertNull(bounds.intersection(line));
        Assertions.assertFalse(bounds.intersects(line));
    }

    private static void assertNoLineIntersection(
            final Bounds3D bounds,
            final LineConvexSubset3D subset) {
        Assertions.assertNull(bounds.intersection(subset));
        Assertions.assertFalse(bounds.intersects(subset));
    }

    private static void assertSegment(final Segment3D segment, final Vector3D start, final Vector3D end) {
        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }

    private static BoundsLinecastChecker3D linecastChecker(final Bounds3D bounds) {
        return new BoundsLinecastChecker3D(bounds);
    }

    /**
     * Internal test class used to perform and verify linecast operations.
     */
    private static final class BoundsLinecastChecker3D {

        private final Bounds3D bounds;

        private final Parallelepiped region;

        private final LinecastChecker3D checker;

        BoundsLinecastChecker3D(final Bounds3D bounds) {
            this.bounds = bounds;
            this.region = bounds.hasSize(TEST_PRECISION) ?
                    bounds.toRegion(TEST_PRECISION) :
                    null;
            this.checker = LinecastChecker3D.with(bounds);
        }

        public BoundsLinecastChecker3D expectNothing() {
            checker.expectNothing();
            return this;
        }

        public BoundsLinecastChecker3D expect(final Vector3D pt, final Vector3D normal) {
            checker.expect(pt, normal);
            return this;
        }

        public BoundsLinecastChecker3D and(final Vector3D pt, final Vector3D normal) {
            return expect(pt, normal);
        }

        public BoundsLinecastChecker3D whenGiven(final Line3D line) {
            // perform the standard checks
            checker.whenGiven(line);

            // check that the returned points are equivalent to those returned by linecasting against
            // the region
            final List<LinecastPoint3D> boundsResults = bounds.linecast(line);

            if (region != null) {
                assertLinecastElements(region.linecast(line), bounds.linecast(line));
            }

            // check consistency with the intersects method; having linecast results guarantees
            // that we intersect the bounds but not vice versa
            if (!boundsResults.isEmpty()) {
                Assertions.assertTrue(bounds.intersects(line),
                        () -> "Linecast result is inconsistent with intersects method: line= " + line);

                assertLinecastResultsConsistentWithSegment(boundsResults, bounds.intersection(line));
            }

            return this;
        }

        public BoundsLinecastChecker3D whenGiven(final LineConvexSubset3D subset) {
            // perform the standard checks
            checker.whenGiven(subset);

            // check that the returned points are equivalent to those returned by linecasting against
            // the region
            final List<LinecastPoint3D> boundsResults = bounds.linecast(subset);

            if (region != null) {
                assertLinecastElements(region.linecast(subset), boundsResults);
            }

            // check consistency with the intersects methods; having linecast results guarantees
            // that we intersect the bounds but not vice versa
            if (!boundsResults.isEmpty()) {
                Assertions.assertTrue(bounds.intersects(subset),
                        () -> "Linecast result is inconsistent with intersects method: line subset= " + subset);

                assertLinecastResultsConsistentWithSegment(boundsResults, bounds.intersection(subset));
            }

            return this;
        }

        /** Assert that the two collections contain the same linecast points and that the elements
         * of {@code actual} are arranged in ascending abscissa order. Note that this does <em>not</em>
         * assert that {@code expected} and {@code actual} have the same exact ordering, since the
         * specific ordering is sensitive to floating point errors.
         * @param expected expected collection
         * @param actual actual collection
         */
        private void assertLinecastElements(
                final Collection<LinecastPoint3D> expected,
                final Collection<LinecastPoint3D> actual) {
            Assertions.assertEquals(expected.size(), actual.size(), "Unexpected list size");

            // create a sorted copy
            final List<LinecastPoint3D> sortedList = new ArrayList<>(actual);
            sortedList.sort(LinecastPoint3D.ABSCISSA_ORDER);

            // check element membership
            for (final LinecastPoint3D expectedPt : expected) {
                final Iterator<LinecastPoint3D> sortedIt = sortedList.iterator();

                boolean found = false;
                while (sortedIt.hasNext()) {
                    if (expectedPt.eq(sortedIt.next(), TEST_PRECISION)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Assertions.fail("Missing expected linecast point " + expectedPt);
                }
            }

            // check the order
            Assertions.assertEquals(sortedList, actual);
        }

        /** Assert that the linecast results are consistent with the given segment, which is taken
         * to be the intersection of a line or line convex subset with the bounding box.
         * @param linecastResults
         * @param segment
         */
        private void assertLinecastResultsConsistentWithSegment(
                final List<LinecastPoint3D> linecastResults,
                final Segment3D segment) {

            for (final LinecastPoint3D pt : linecastResults) {
                Assertions.assertEquals(RegionLocation.BOUNDARY, segment.classifyAbscissa(pt.getAbscissa()),
                        () -> "Expected linecast point to lie on segment boundary");
            }
        }
    }
}
