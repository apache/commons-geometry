/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.geometry.euclidean.twod.rotation.Rotation2D;
import org.apache.commons.geometry.euclidean.twod.shape.Parallelogram;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Bounds2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final String NO_POINTS_MESSAGE = "Cannot construct bounds: no points given";

    private static final Pattern INVALID_BOUNDS_PATTERN =
            Pattern.compile("^Invalid bounds: min= \\([^\\)]+\\), max= \\([^\\)]+\\)");

    @Test
    void testFrom_varargs_singlePoint() {
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
    void testFrom_varargs_multiplePoints() {
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
    void testFrom_iterable_singlePoint() {
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
    void testFrom_iterable_multiplePoints() {
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
    void testFrom_iterable_noPoints() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Bounds2D.from(new ArrayList<>());
        }, IllegalStateException.class, NO_POINTS_MESSAGE);
    }

    @Test
    void testFrom_invalidBounds() {
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
    void testHasSize() {
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
    void testContains_strict() {
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
    void testContains_precision() {
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
    void testIntersects() {
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
    void testIntersection() {
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
    void toRegion() {
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
    void toRegion_boundingBoxTooSmall() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Bounds2D.from(Vector2D.ZERO, Vector2D.of(1e-12, 1e-12)).toRegion(TEST_PRECISION));
    }

    @Test
    void testEq() {
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
    void testLinecast_intersectsSide() {
        // -- arrange
        // use unequal side sizes so that our test lines do not end up passing through
        // a vertex on the opposite side
        final Bounds2D bounds = Bounds2D.from(Vector2D.of(-0.9, -2), Vector2D.of(0.9, 2));

        // -- act/assert
        checkLinecastIntersectingSide(bounds, Vector2D.of(0.9, 0), Vector2D.Unit.PLUS_X);
        checkLinecastIntersectingSide(bounds, Vector2D.of(-0.9, 0), Vector2D.Unit.MINUS_X);

        checkLinecastIntersectingSide(bounds, Vector2D.of(0, 2), Vector2D.Unit.PLUS_Y);
        checkLinecastIntersectingSide(bounds, Vector2D.of(0, -2), Vector2D.Unit.MINUS_Y);
    }

    private void checkLinecastIntersectingSide(
            final Bounds2D bounds,
            final Vector2D sidePt,
            final Vector2D normal) {

        // -- arrange
        final Vector2D offset = normal.multiply(1.2);
        final Parallelogram region = bounds.toRegion(TEST_PRECISION);

        EuclideanTestUtils.permute(-1, 1, 0.5, (x, y) -> {
            final Vector2D otherPt = sidePt
                    .add(Vector2D.of(x, y))
                    .add(offset);
            final Line line = Lines.fromPoints(otherPt, sidePt, TEST_PRECISION);

            final LinecastPoint2D reversePt = region.linecastFirst(line.reverse());

            // -- act/assert
            linecastChecker(bounds)
                .expect(sidePt, normal)
                .and(reversePt.getPoint(), reversePt.getNormal())
                .whenGiven(line);

            linecastChecker(bounds)
                .and(reversePt.getPoint(), reversePt.getNormal())
                .expect(sidePt, normal)
                .whenGiven(line.reverse());
        });
    }

    @Test
    void testLinecast_intersectsSingleVertex() {
        // -- arrange
        final Bounds2D bounds = Bounds2D.from(Vector2D.ZERO, Vector2D.of(1, 1));

        // -- act/assert
        checkLinecastIntersectingSingleVertex(bounds, Vector2D.ZERO);
        checkLinecastIntersectingSingleVertex(bounds, Vector2D.of(0, 1));
        checkLinecastIntersectingSingleVertex(bounds, Vector2D.of(1, 0));
        checkLinecastIntersectingSingleVertex(bounds, Vector2D.of(1, 1));
    }

    private void checkLinecastIntersectingSingleVertex(
            final Bounds2D bounds,
            final Vector2D vertex) {

        // -- arrange
        final Vector2D centerToVertex = vertex.subtract(bounds.getCentroid()).normalize();

        final Vector2D lineDir = centerToVertex.orthogonal();
        final Line line = Lines.fromPointAndDirection(vertex, lineDir, TEST_PRECISION);

        // construct possible normals for this vertex
        final List<Vector2D> normals = new ArrayList<>();
        normals.add(centerToVertex.project(Vector2D.Unit.PLUS_X).normalize());
        normals.add(centerToVertex.project(Vector2D.Unit.PLUS_Y).normalize());

        normals.sort(Vector2D.COORDINATE_ASCENDING_ORDER);

        final BoundsLinecastChecker2D checker = linecastChecker(bounds);
        for (final Vector2D normal : normals) {
            checker.expect(vertex, normal);
        }

        // -- act/assert
        checker
            .whenGiven(line)
            .whenGiven(line.reverse());
    }

    @Test
    void testLinecast_vertexToVertex() {
        // -- arrange
        final Vector2D min = Vector2D.ZERO;
        final Vector2D max = Vector2D.of(1, 1);

        final Bounds2D bounds = Bounds2D.from(min, max);
        final Line line = Lines.fromPoints(min, max, TEST_PRECISION);

        // -- act/assert
        linecastChecker(bounds)
            .expect(min, Vector2D.Unit.MINUS_X)
            .and(min, Vector2D.Unit.MINUS_Y)
            .and(max, Vector2D.Unit.PLUS_Y)
            .and(max, Vector2D.Unit.PLUS_X)
            .whenGiven(line);
    }

    @Test
    void testLinecast_alongSide() {
        // -- arrange
        final Vector2D min = Vector2D.ZERO;
        final Vector2D max = Vector2D.of(1, 1);

        final Bounds2D bounds = Bounds2D.from(min, max);

        final int cnt = 10;
        for (double x = min.getX();
                x <= max.getX();
                x += bounds.getDiagonal().getX() / cnt) {

            final Vector2D start = Vector2D.of(x, min.getY());
            final Vector2D end = Vector2D.of(x, max.getY());

            final Line line = Lines.fromPoints(start, end, TEST_PRECISION);

            // -- act/assert
            linecastChecker(bounds)
                .expect(start, Vector2D.Unit.MINUS_Y)
                .and(end, Vector2D.Unit.PLUS_Y)
                .whenGiven(line);
        }
    }

    @Test
    void testLinecast_noIntersection() {
        // -- arrange
        final Bounds2D bounds = Bounds2D.from(Vector2D.ZERO, Vector2D.of(1, 1));

        // -- act/assert
        checkLinecastNoIntersection(bounds, Vector2D.ZERO);
        checkLinecastNoIntersection(bounds, Vector2D.of(0, 1));
        checkLinecastNoIntersection(bounds, Vector2D.of(1, 0));
        checkLinecastNoIntersection(bounds, Vector2D.of(1, 1));
    }

    private void checkLinecastNoIntersection(
            final Bounds2D bounds,
            final Vector2D vertex) {

        // -- arrange
        final Vector2D toVertex = bounds.getCentroid().directionTo(vertex);

        final Vector2D offsetVertex = vertex.add(toVertex);

        final Line plusXLine = Lines.fromPointAndDirection(offsetVertex, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Line plusYLine = Lines.fromPointAndDirection(offsetVertex, Vector2D.Unit.PLUS_Y, TEST_PRECISION);

        final BoundsLinecastChecker2D emptyChecker = linecastChecker(bounds)
                .expectNothing();

        // -- act/assert
        // check axis-aligned lines
        emptyChecker
            .whenGiven(plusXLine)
            .whenGiven(plusYLine);

        // check slightly rotated lines
        final Rotation2D rot = Rotation2D.of(0.1 * Math.PI);
        emptyChecker
            .whenGiven(plusXLine.transform(rot))
            .whenGiven(plusYLine.transform(rot));
    }

    @Test
    void testLinecast_nonSpan() {
        // -- arrange
        final Vector2D min = Vector2D.ZERO;
        final Vector2D max = Vector2D.of(1, 1);

        final Bounds2D bounds = Bounds2D.from(min, max);

        final Vector2D centroid = bounds.getCentroid();

        final Vector2D start = Vector2D.of(max.getX(), centroid.getY());
        final Vector2D end = Vector2D.of(min.getX(), centroid.getY());

        final Line line = Lines.fromPoints(start, end, TEST_PRECISION);

        // -- act/assert
        linecastChecker(bounds)
            .expect(end, Vector2D.Unit.MINUS_X)
            .whenGiven(line.rayFrom(-0.5));

        linecastChecker(bounds)
            .expect(start, Vector2D.Unit.PLUS_X)
            .whenGiven(line.reverseRayTo(-0.5));

        linecastChecker(bounds)
            .expectNothing()
            .whenGiven(line.segment(-0.9, -0.1));

        linecastChecker(bounds)
            .expect(end, Vector2D.Unit.MINUS_X)
            .whenGiven(line.segment(-0.9, 0.1));

        linecastChecker(bounds)
            .expect(start, Vector2D.Unit.PLUS_X)
            .whenGiven(line.segment(-1.1, -0.1));

        linecastChecker(bounds)
            .expect(start, Vector2D.Unit.PLUS_X)
            .expect(end, Vector2D.Unit.MINUS_X)
            .whenGiven(line.segment(-1.1, 0.1));
    }

    @Test
    void testLinecast_subsetEndpointOnBounds() {
        // -- arrange
        final Vector2D min = Vector2D.ZERO;
        final Vector2D max = Vector2D.of(1, 1);

        final Bounds2D bounds = Bounds2D.from(min, max);

        final Vector2D centroid = bounds.getCentroid();

        final Vector2D start = Vector2D.of(max.getX(), centroid.getY());
        final Vector2D end = Vector2D.of(min.getX(), centroid.getY());

        final Line line = Lines.fromPoints(start, end, TEST_PRECISION);

        // -- act/assert
        linecastChecker(bounds)
            .expect(end, Vector2D.Unit.MINUS_X)
            .whenGiven(line.rayFrom(0));

        linecastChecker(bounds)
            .expect(end, Vector2D.Unit.MINUS_X)
            .whenGiven(line.segment(0, 1));

        linecastChecker(bounds)
            .expect(start, Vector2D.Unit.PLUS_X)
            .whenGiven(line.reverseRayTo(-1));

        linecastChecker(bounds)
            .expect(start, Vector2D.Unit.PLUS_X)
            .whenGiven(line.segment(-2, -1));
    }

    @Test
    void testLinecast_usesLinePrecision() {
        // -- arrange
        final double withinEps = 0.9 * TEST_EPS;
        final double outsideEps = 1.1 * TEST_EPS;

        final Vector2D min = Vector2D.ZERO;
        final Vector2D max = Vector2D.of(1, 1);

        final Bounds2D bounds = Bounds2D.from(min, max);

        final Vector2D centroid = bounds.getCentroid();

        final Vector2D centerStart = Vector2D.of(max.getX(), centroid.getY());
        final Vector2D centerEnd = Vector2D.of(min.getX(), centroid.getY());

        final Line centerLine = Lines.fromPoints(centerStart, centerEnd, TEST_PRECISION);

        final Vector2D sideStart = Vector2D.of(max.getX() + withinEps, min.getY() + withinEps);
        final Vector2D sideEnd = Vector2D.of(max.getX() + withinEps, max.getY() + withinEps);

        final Line sideLine = Lines.fromPoints(sideStart, sideEnd, TEST_PRECISION);

        // -- act/assert
        linecastChecker(bounds)
            .expect(centerEnd, Vector2D.Unit.MINUS_X)
            .whenGiven(centerLine.rayFrom(withinEps));

        linecastChecker(bounds)
            .expectNothing()
            .whenGiven(centerLine.rayFrom(outsideEps));

        linecastChecker(bounds)
            .expect(centerStart, Vector2D.Unit.PLUS_X)
            .expect(centerEnd, Vector2D.Unit.MINUS_X)
            .whenGiven(centerLine.segment(-1 + withinEps, -withinEps));

        linecastChecker(bounds)
            .expectNothing()
            .whenGiven(centerLine.segment(-1 + outsideEps, -outsideEps));

        linecastChecker(bounds)
            .expectNothing()
            .whenGiven(sideLine.segment(outsideEps, 1 - outsideEps));
    }

    @Test
    void testLinecast_boundsHasNoSize() {
        // -- arrange
        final Vector2D pt = Vector2D.of(1, 2);

        final Bounds2D bounds = Bounds2D.from(pt, pt);

        final Line diagonalLine = Lines.fromPointAndDirection(pt, Vector2D.of(1, 1), TEST_PRECISION);

        final Line plusXLine = Lines.fromPointAndDirection(pt, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // -- act/assert
        linecastChecker(bounds)
            .expect(pt, Vector2D.Unit.MINUS_X)
            .expect(pt, Vector2D.Unit.MINUS_Y)
            .expect(pt, Vector2D.Unit.PLUS_Y)
            .expect(pt, Vector2D.Unit.PLUS_X)
            .whenGiven(diagonalLine);

        linecastChecker(bounds)
            .expect(pt, Vector2D.Unit.MINUS_X)
            .expect(pt, Vector2D.Unit.PLUS_X)
            .whenGiven(plusXLine);
    }

    @Test
    void testLineIntersection() {
        // -- arrange
        final Vector2D min = Vector2D.ZERO;
        final Vector2D max = Vector2D.of(1, 1);

        final Vector2D insideMin = Vector2D.of(0.1, 0.1);
        final Vector2D insideMax = Vector2D.of(0.9, 0.9);

        final Vector2D outsideMin = Vector2D.of(-0.1, -0.1);
        final Vector2D outsideMax = Vector2D.of(1.1, 1.1);

        final Bounds2D bounds = Bounds2D.from(min, max);

        final Line diagonal = Lines.fromPoints(min, max, TEST_PRECISION);

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
        final Bounds2D bounds = Bounds2D.from(Vector2D.ZERO, Vector2D.of(1, 1));

        final Line plusXLine =
                Lines.fromPointAndDirection(bounds.getCentroid(), Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // -- act/assert
        checkLineNoIntersection(bounds, Vector2D.ZERO);
        checkLineNoIntersection(bounds, Vector2D.of(0, 0));
        checkLineNoIntersection(bounds, Vector2D.of(0, 1));
        checkLineNoIntersection(bounds, Vector2D.of(1, 0));
        checkLineNoIntersection(bounds, Vector2D.of(1, 1));

        assertNoLineIntersection(bounds, plusXLine.segment(-0.2, -0.1));
        assertNoLineIntersection(bounds, plusXLine.reverseRayTo(-0.1));

        assertNoLineIntersection(bounds, plusXLine.segment(1.1, 1.2));
        assertNoLineIntersection(bounds, plusXLine.rayFrom(1.1));
    }

    private void checkLineNoIntersection(
            final Bounds2D bounds,
            final Vector2D vertex) {

        // -- arrange
        final Vector2D toVertex = bounds.getCentroid().directionTo(vertex);

        final Vector2D offsetVertex = vertex.add(toVertex);

        final Line plusXLine = Lines.fromPointAndDirection(offsetVertex, Vector2D.Unit.PLUS_X, TEST_PRECISION);
        final Line plusYLine = Lines.fromPointAndDirection(offsetVertex, Vector2D.Unit.PLUS_Y, TEST_PRECISION);

        // -- act/assert
        // check axis-aligned lines
        assertNoLineIntersection(bounds, plusXLine);
        assertNoLineIntersection(bounds, plusYLine);

        // check slightly rotated lines
        final Rotation2D rot = Rotation2D.of(0.1 * Math.PI);
        assertNoLineIntersection(bounds, plusXLine.transform(rot));
        assertNoLineIntersection(bounds, plusYLine.transform(rot));
    }

    @Test
    void testLineIntersection_boundsHasNoSize() {
        // -- arrange
        final Vector2D pt = Vector2D.of(1, 2);

        final Bounds2D bounds = Bounds2D.from(pt, pt);

        final Line plusXLine = Lines.fromPointAndDirection(pt, Vector2D.Unit.PLUS_X, TEST_PRECISION);

        // -- act/assert
        assertLineIntersection(bounds, plusXLine, pt, pt);
        assertLineIntersection(bounds, plusXLine.rayFrom(pt), pt, pt);
    }

    @Test
    void testLineIntersection_lineAlmostParallel() {
        // -- arrange
        final Vector2D min = Vector2D.of(1e150, -1);
        final Vector2D max = Vector2D.of(1.1e150, 1);

        final Bounds2D bounds = Bounds2D.from(min, max);

        final Vector2D lineDir = Vector2D.of(1, -5e-11);
        final Line line = Lines.fromPointAndDirection(Vector2D.ZERO, lineDir, TEST_PRECISION);

        // -- act
        assertNoLineIntersection(bounds, line);
    }

    @Test
    void testHashCode() {
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
    void testEquals() {
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
    void testToString() {
        // arrange
        final Bounds2D b = Bounds2D.from(Vector2D.of(1, 1), Vector2D.of(2, 2));

        // act
        final String str = b.toString();

        // assert
        GeometryTestUtils.assertContains("Bounds2D[min= (1", str);
        GeometryTestUtils.assertContains(", max= (2", str);
    }

    @Test
    void testBuilder_addMethods() {
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
    void testBuilder_hasBounds() {
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

    private static void assertLineIntersection(
            final Bounds2D bounds,
            final Line line,
            final Vector2D start,
            final Vector2D end) {
        final Segment segment = bounds.intersection(line);

        Assertions.assertSame(line, segment.getLine());
        assertSegment(segment, start, end);

        Assertions.assertTrue(bounds.intersects(line));
    }

    private static void assertLineIntersection(
            final Bounds2D bounds,
            final LineConvexSubset subset,
            final Vector2D start,
            final Vector2D end) {
        final Segment segment = bounds.intersection(subset);

        Assertions.assertSame(subset.getLine(), segment.getLine());
        assertSegment(segment, start, end);

        Assertions.assertTrue(bounds.intersects(subset));
    }

    private static void assertNoLineIntersection(
            final Bounds2D bounds,
            final Line line) {
        Assertions.assertNull(bounds.intersection(line));
        Assertions.assertFalse(bounds.intersects(line));
    }

    private static void assertNoLineIntersection(
            final Bounds2D bounds,
            final LineConvexSubset subset) {
        Assertions.assertNull(bounds.intersection(subset));
        Assertions.assertFalse(bounds.intersects(subset));
    }

    private static void assertSegment(final Segment segment, final Vector2D start, final Vector2D end) {
        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }

    private static BoundsLinecastChecker2D linecastChecker(final Bounds2D bounds) {
        return new BoundsLinecastChecker2D(bounds);
    }

    /**
     * Internal test class used to perform and verify linecast operations.
     */
    private static final class BoundsLinecastChecker2D {

        private final Bounds2D bounds;

        private final Parallelogram region;

        private final LinecastChecker2D checker;

        BoundsLinecastChecker2D(final Bounds2D bounds) {
            this.bounds = bounds;
            this.region = bounds.hasSize(TEST_PRECISION) ?
                    bounds.toRegion(TEST_PRECISION) :
                    null;
            this.checker = LinecastChecker2D.with(bounds);
        }

        public BoundsLinecastChecker2D expectNothing() {
            checker.expectNothing();
            return this;
        }

        public BoundsLinecastChecker2D expect(final Vector2D pt, final Vector2D normal) {
            checker.expect(pt, normal);
            return this;
        }

        public BoundsLinecastChecker2D and(final Vector2D pt, final Vector2D normal) {
            return expect(pt, normal);
        }

        public BoundsLinecastChecker2D whenGiven(final Line line) {
            // perform the standard checks
            checker.whenGiven(line);

            // check that the returned points are equivalent to those returned by linecasting against
            // the region
            final List<LinecastPoint2D> boundsResults = bounds.linecast(line);

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

        public BoundsLinecastChecker2D whenGiven(final LineConvexSubset subset) {
            // perform the standard checks
            checker.whenGiven(subset);

            // check that the returned points are equivalent to those returned by linecasting against
            // the region
            final List<LinecastPoint2D> boundsResults = bounds.linecast(subset);

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
                final Collection<LinecastPoint2D> expected,
                final Collection<LinecastPoint2D> actual) {
            Assertions.assertEquals(expected.size(), actual.size(), "Unexpected list size");

            // create a sorted copy
            final List<LinecastPoint2D> sortedList = new ArrayList<>(actual);
            sortedList.sort(LinecastPoint2D.ABSCISSA_ORDER);

            // check element membership
            for (final LinecastPoint2D expectedPt : expected) {
                final Iterator<LinecastPoint2D> sortedIt = sortedList.iterator();

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
                final List<LinecastPoint2D> linecastResults,
                final Segment segment) {

            for (final LinecastPoint2D pt : linecastResults) {
                Assertions.assertEquals(RegionLocation.BOUNDARY, segment.classifyAbscissa(pt.getAbscissa()),
                        () -> "Expected linecast point to lie on segment boundary");
            }
        }
    }
}
