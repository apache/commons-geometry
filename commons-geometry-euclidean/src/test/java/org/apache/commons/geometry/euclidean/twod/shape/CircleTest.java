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
package org.apache.commons.geometry.euclidean.twod.shape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.LinecastPoint2D;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.PolarCoordinates;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CircleTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Comparator<LineConvexSubset> SEGMENT_DIRECTION_COMPARATOR =
        (a, b) -> Vector2D.COORDINATE_ASCENDING_ORDER.compare(
            a.getLine().getDirection(),
            b.getLine().getDirection());

    @Test
    public void testFrom() {
        // arrange
        final Vector2D center = Vector2D.of(1, 2);

        // act
        final Circle c = Circle.from(center, 3, TEST_PRECISION);

        // act/assert
        Assertions.assertFalse(c.isFull());
        Assertions.assertFalse(c.isEmpty());

        Assertions.assertSame(center, c.getCenter());
        Assertions.assertSame(center, c.getCentroid());

        Assertions.assertEquals(3, c.getRadius(), 0.0);

        Assertions.assertSame(TEST_PRECISION, c.getPrecision());
    }

    @Test
    public void testFrom_illegalCenter() {
        // act/assert
        assertThrows(IllegalArgumentException.class, () -> Circle.from(Vector2D.of(Double.POSITIVE_INFINITY, 1), 1, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> Circle.from(Vector2D.of(Double.NaN, 1), 1, TEST_PRECISION));
    }

    @Test
    public void testFrom_illegalRadius() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        // act/assert
        assertThrows(IllegalArgumentException.class, () -> Circle.from(Vector2D.ZERO, -1, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> Circle.from(Vector2D.ZERO, 0, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> Circle.from(Vector2D.ZERO, Double.POSITIVE_INFINITY, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> Circle.from(Vector2D.ZERO, Double.NaN, TEST_PRECISION));
        assertThrows(IllegalArgumentException.class, () -> Circle.from(Vector2D.ZERO, 1e-3, precision));
    }

    @Test
    public void testGeometricProperties() {
        // arrange
        final double r = 2;
        final Circle c = Circle.from(Vector2D.of(1, 2), r, TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(2 * Math.PI * r, c.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals(Math.PI * r * r, c.getSize(), TEST_EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        final Circle c = Circle.from(Vector2D.of(1, 2), 1, TEST_PRECISION);

        // act/assert
        EuclideanTestUtils.assertRegionLocation(c, RegionLocation.INSIDE,
                Vector2D.of(1, 2),
                Vector2D.of(0.5, 2), Vector2D.of(1.5, 2),
                Vector2D.of(1, 1.5), Vector2D.of(1, 2.5),
                Vector2D.of(0.5, 1.5), Vector2D.of(1.5, 2.5),
                Vector2D.of(0.5, 2.5), Vector2D.of(1.5, 1.5));

        EuclideanTestUtils.assertRegionLocation(c, RegionLocation.OUTSIDE,
                Vector2D.of(-0.5, 2), Vector2D.of(2.5, 2),
                Vector2D.of(1, 0.5), Vector2D.of(1, 3.5),
                Vector2D.of(0.25, 1.25), Vector2D.of(1.75, 2.75),
                Vector2D.of(0.25, 2.75), Vector2D.of(1.75, 1.25));

        for (double angle = 0; angle < PlaneAngleRadians.TWO_PI; angle += 0.1) {
            EuclideanTestUtils.assertRegionLocation(c, RegionLocation.BOUNDARY,
                    c.getCenter().add(PolarCoordinates.of(1, angle).toCartesian()));
        }
    }

    @Test
    public void testContains() {
        // arrange
        final Circle c = Circle.from(Vector2D.of(1, 2), 1, TEST_PRECISION);

        // act/assert
        checkContains(c, true,
                Vector2D.of(1, 2),
                Vector2D.of(0.5, 2), Vector2D.of(1.5, 2),
                Vector2D.of(1, 1.5), Vector2D.of(1, 2.5),
                Vector2D.of(0.5, 1.5), Vector2D.of(1.5, 2.5),
                Vector2D.of(0.5, 2.5), Vector2D.of(1.5, 1.5));

        for (double angle = 0; angle < PlaneAngleRadians.TWO_PI; angle += 0.1) {
            checkContains(c, true,
                    c.getCenter().add(PolarCoordinates.of(1, angle).toCartesian()));
        }

        checkContains(c, false,
                Vector2D.of(-0.5, 2), Vector2D.of(2.5, 2),
                Vector2D.of(1, 0.5), Vector2D.of(1, 3.5),
                Vector2D.of(0.25, 1.25), Vector2D.of(1.75, 2.75),
                Vector2D.of(0.25, 2.75), Vector2D.of(1.75, 1.25));
    }

    @Test
    public void testProject() {
        // arrange
        final Vector2D center = Vector2D.of(1.5, 2.5);
        final double radius = 3;
        final Circle c = Circle.from(center, radius, TEST_PRECISION);

        EuclideanTestUtils.permute(-4, 4, 1, (x, y) -> {
            final Vector2D pt = Vector2D.of(x, y);

            // act
            final Vector2D projection = c.project(pt);

            // assert
            Assertions.assertEquals(radius, center.distance(projection), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(center.directionTo(pt),
                    center.directionTo(projection), TEST_EPS);
        });
    }

    @Test
    public void testProject_argumentEqualsCenter() {
        // arrange
        final Circle c = Circle.from(Vector2D.of(1, 2), 2, TEST_PRECISION);

        // act
        final Vector2D projection = c.project(Vector2D.of(1, 2));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 2), projection, TEST_EPS);
    }

    @Test
    public void testIntersections() {
        // --- arrange
        final Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);
        final double sqrt3 = Math.sqrt(3);

        // --- act/assert
        // descending horizontal lines
        checkIntersections(c, Lines.fromPoints(Vector2D.of(-1, 4), Vector2D.of(5, 4), TEST_PRECISION));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(-1, 3), Vector2D.of(5, 3), TEST_PRECISION),
                Vector2D.of(2, 3));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(-1, 2), Vector2D.of(5, 2), TEST_PRECISION),
                Vector2D.of(2 - sqrt3, 2), Vector2D.of(2 + sqrt3, 2));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(-1, 1), Vector2D.of(5, 1), TEST_PRECISION),
                Vector2D.of(0, 1), Vector2D.of(4, 1));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(-1, 0), Vector2D.of(5, 0), TEST_PRECISION),
                Vector2D.of(2 - sqrt3, 0), Vector2D.of(2 + sqrt3, 0));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(-1, -1), Vector2D.of(5, -1), TEST_PRECISION),
                Vector2D.of(2, -1));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(-1, -2), Vector2D.of(5, -2), TEST_PRECISION));

        // ascending vertical lines
        checkIntersections(c, Lines.fromPoints(Vector2D.of(-1, -2), Vector2D.of(-1, 5), TEST_PRECISION));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(0, -2), Vector2D.of(0, 5), TEST_PRECISION),
                Vector2D.of(0, 1));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(1, -2), Vector2D.of(1, 5), TEST_PRECISION),
                Vector2D.of(1, 1 - sqrt3), Vector2D.of(1, 1 + sqrt3));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(2, -2), Vector2D.of(2, 5), TEST_PRECISION),
                Vector2D.of(2, -1), Vector2D.of(2, 3));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(3, -2), Vector2D.of(3, 5), TEST_PRECISION),
                Vector2D.of(3, 1 - sqrt3), Vector2D.of(3, 1 + sqrt3));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(4, -2), Vector2D.of(4, 5), TEST_PRECISION),
                Vector2D.of(4, 1));
        checkIntersections(c, Lines.fromPoints(Vector2D.of(5, -2), Vector2D.of(5, 5), TEST_PRECISION));

        // diagonal from origin
        final Vector2D center = c.getCenter();
        checkIntersections(c, Lines.fromPoints(Vector2D.ZERO, c.getCenter(), TEST_PRECISION),
                center.withNorm(center.norm() - c.getRadius()), center.withNorm(center.norm() + c.getRadius()));
    }

    @Test
    public void testLinecast() {
        // arrange
        final Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);
        final double sqrt3 = Math.sqrt(3);

        // act/assert
        checkLinecast(c, Lines.segmentFromPoints(Vector2D.of(-1, 0), Vector2D.of(5, 0), TEST_PRECISION),
                Vector2D.of(2 - sqrt3, 0), Vector2D.of(2 + sqrt3, 0));
        checkLinecast(c, Lines.segmentFromPoints(Vector2D.of(-1, 3), Vector2D.of(5, 3), TEST_PRECISION),
                Vector2D.of(2, 3));
        checkLinecast(c, Lines.segmentFromPoints(Vector2D.of(-1, -2), Vector2D.of(5, -2), TEST_PRECISION));
    }

    @Test
    public void testLinecast_intersectionsNotInSegment() {
        // arrange
        final Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);
        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION);

        // act/assert
        checkLinecast(c, line.segment(-1, 0));
        checkLinecast(c, line.segment(1.5, 2.5));
        checkLinecast(c, line.segment(1.5, 2.5));
        checkLinecast(c, line.segment(4, 5));
    }

    @Test
    public void testLinecast_segmentPointOnBoundary() {
        // arrange
        final Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);
        final Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION);
        final double sqrt3 = Math.sqrt(3);
        final double start = 2 - sqrt3;
        final double end = 2 + sqrt3;

        // act/assert
        checkLinecast(c, line.segment(start, 2), Vector2D.of(start, 0));
        checkLinecast(c, line.segment(start, end), Vector2D.of(start, 0), Vector2D.of(end, 0));
        checkLinecast(c, line.segment(end, 5), Vector2D.of(end, 0));
    }

    @Test
    public void testToTree_threeSegments() {
        // arrange
        final Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);

        // act
        final RegionBSPTree2D tree = c.toTree(3);

        // assert
        checkBasicApproximationProperties(c, tree);

        final List<LineConvexSubset> segments = new ArrayList<>(tree.getBoundaries());
        segments.sort(SEGMENT_DIRECTION_COMPARATOR);

        Assertions.assertEquals(3, segments.size());

        final double inc = PlaneAngleRadians.TWO_PI / 3.0;
        final Vector2D p0 = Vector2D.of(4, 1);
        final Vector2D p1 = Vector2D.of(
                (2 * Math.cos(inc)) + 2,
                (2 * Math.sin(inc)) + 1);
        final Vector2D p2 = Vector2D.of(
                (2 * Math.cos(2 * inc)) + 2,
                (2 * Math.sin(2 * inc)) + 1);

        assertFiniteSegment(segments.get(0), p0, p1);
        assertFiniteSegment(segments.get(1), p1, p2);
        assertFiniteSegment(segments.get(2), p2, p0);
    }

    @Test
    public void testToTree_fourSegments() {
        // arrange
        final Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);

        // act
        final RegionBSPTree2D tree = c.toTree(4);

        // assert
        checkBasicApproximationProperties(c, tree);

        final List<LineConvexSubset> segments = new ArrayList<>(tree.getBoundaries());
        segments.sort(SEGMENT_DIRECTION_COMPARATOR);

        Assertions.assertEquals(4, segments.size());

        final Vector2D p0 = Vector2D.of(4, 1);
        final Vector2D p1 = Vector2D.of(2, 3);
        final Vector2D p2 = Vector2D.of(0, 1);
        final Vector2D p3 = Vector2D.of(2, -1);

        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p0, p1);
        assertFiniteSegment(segments.get(2), p2, p3);
        assertFiniteSegment(segments.get(3), p3, p0);
    }

    @Test
    public void testToTree_multipleApproximationSizes() {
        // -- arrange
        final Circle c = Circle.from(Vector2D.of(-3, 5), 10, TEST_PRECISION);

        final int min = 5;
        final int max = 100;

        RegionBSPTree2D tree;

        double sizeDiff;
        double prevSizeDiff = Double.POSITIVE_INFINITY;

        for (int n = min; n <= max; ++n) {
            // -- act
            tree = c.toTree(n);

            // -- assert
            checkBasicApproximationProperties(c, tree);

            // check that we get closer and closer to the correct size as we add more segments
            sizeDiff = c.getSize() - tree.getSize();
            Assertions.assertTrue(sizeDiff < prevSizeDiff, "Expected size difference to decrease");

            prevSizeDiff = sizeDiff;
        }
    }

    @Test
    public void testToTree_closeApproximation() {
        // arrange
        final Circle c = Circle.from(Vector2D.of(-2, 0), 1, TEST_PRECISION);

        // act
        final RegionBSPTree2D tree = c.toTree(100);

        // assert
        checkBasicApproximationProperties(c, tree);

        final double eps = 5e-3;
        Assertions.assertEquals(c.getSize(), tree.getSize(), eps);
        Assertions.assertEquals(c.getBoundarySize(), tree.getBoundarySize(), eps);
        EuclideanTestUtils.assertCoordinatesEqual(c.getCentroid(), tree.getCentroid(), eps);
    }

    @Test
    public void testToTree_invalidSegmentCount() {
        // arrange
        final Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);
        final String baseMsg = "Circle approximation segment number must be greater than or equal to 3; was ";

        // act/assert
        assertThrows(IllegalArgumentException.class, () -> c.toTree(2),  baseMsg + "2");
        assertThrows(IllegalArgumentException.class, () -> c.toTree(-1),  baseMsg + "-1");
    }

    @Test
    public void testHashCode() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        final Circle a = Circle.from(Vector2D.of(1, 2), 3, TEST_PRECISION);
        final Circle b = Circle.from(Vector2D.of(1, 1), 3, TEST_PRECISION);
        final Circle c = Circle.from(Vector2D.of(1, 2), 4, TEST_PRECISION);
        final Circle d = Circle.from(Vector2D.of(1, 2), 3, precision);
        final Circle e = Circle.from(Vector2D.of(1, 2), 3, TEST_PRECISION);

        // act
        final int hash = a.hashCode();

        // act/assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, b.hashCode());
        Assertions.assertNotEquals(hash, c.hashCode());
        Assertions.assertNotEquals(hash, d.hashCode());

        Assertions.assertEquals(hash, e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        final DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        final Circle a = Circle.from(Vector2D.of(1, 2), 3, TEST_PRECISION);
        final Circle b = Circle.from(Vector2D.of(1, 1), 3, TEST_PRECISION);
        final Circle c = Circle.from(Vector2D.of(1, 2), 4, TEST_PRECISION);
        final Circle d = Circle.from(Vector2D.of(1, 2), 3, precision);
        final Circle e = Circle.from(Vector2D.of(1, 2), 3, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertSimpleEqualsCases(a);

        Assertions.assertNotEquals(a, b);
        Assertions.assertNotEquals(a, c);
        Assertions.assertNotEquals(a, d);

        Assertions.assertEquals(a, e);
    }

    @Test
    public void testToString() {
        // arrange
        final Circle c = Circle.from(Vector2D.of(1, 2), 3, TEST_PRECISION);

        // act
        final String str = c.toString();

        // assert
        Assertions.assertEquals("Circle[center= (1.0, 2.0), radius= 3.0]", str);
    }

    private static void checkContains(final Circle circle, final boolean contains, final Vector2D... pts) {
        for (final Vector2D pt : pts) {
            Assertions.assertEquals(contains, circle.contains(pt),
                    "Expected circle to " + (contains ? "" : "not") + "contain point " + pt);
        }
    }

    private static void checkIntersections(final Circle circle, final Line line, final Vector2D... expectedPts) {
        // --- act
        // compute the intersections forward and reverse
        final List<Vector2D> actualPtsForward = circle.intersections(line);
        final List<Vector2D> actualPtsReverse = circle.intersections(line.reverse());

        final Vector2D actualFirstForward = circle.firstIntersection(line);
        final Vector2D actualFirstReverse = circle.firstIntersection(line.reverse());

        // --- assert
        final int len = expectedPts.length;

        // check the lists
        Assertions.assertEquals(len, actualPtsForward.size());
        Assertions.assertEquals(len, actualPtsReverse.size());

        for (int i = 0; i < len; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[i], actualPtsForward.get(i), TEST_EPS);
            Assertions.assertEquals(circle.getRadius(), circle.getCenter().distance(actualPtsForward.get(i)), TEST_EPS);

            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[len - i - 1], actualPtsReverse.get(i), TEST_EPS);
            Assertions.assertEquals(circle.getRadius(), circle.getCenter().distance(actualPtsReverse.get(i)), TEST_EPS);
        }

        // check the single intersection points
        if (len > 0) {
            Assertions.assertNotNull(actualFirstForward);
            Assertions.assertNotNull(actualFirstReverse);

            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[0], actualFirstForward, TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[len - 1], actualFirstReverse, TEST_EPS);
        } else {
            Assertions.assertNull(actualFirstForward);
            Assertions.assertNull(actualFirstReverse);
        }
    }

    private static void checkLinecast(final Circle c, final LineConvexSubset segment, final Vector2D... expectedPts) {
        // check linecast
        final List<LinecastPoint2D> results = c.linecast(segment);
        Assertions.assertEquals(expectedPts.length, results.size());

        LinecastPoint2D actual;
        Vector2D expected;
        for (int i = 0; i < expectedPts.length; ++i) {
            expected = expectedPts[i];
            actual = results.get(i);

            EuclideanTestUtils.assertCoordinatesEqual(expected, actual.getPoint(), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(c.getCenter().directionTo(expected), actual.getNormal(), TEST_EPS);
            Assertions.assertSame(segment.getLine(), actual.getLine());
        }

        // check linecastFirst
        final LinecastPoint2D firstResult = c.linecastFirst(segment);
        if (expectedPts.length > 0) {
            Assertions.assertEquals(results.get(0), firstResult);
        } else {
            Assertions.assertNull(firstResult);
        }
    }

    /**
     * Check a number of standard properties for bsp trees generated as circle approximations.
     */
    private static void checkBasicApproximationProperties(final Circle c, final RegionBSPTree2D tree) {
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        // all vertices must be inside the circle or on the boundary
        final List<LinePath> paths = tree.getBoundaryPaths();
        Assertions.assertEquals(1, paths.size());

        final LinePath path = paths.get(0);
        Assertions.assertTrue(path.isFinite());

        for (final Vector2D vertex : path.getVertexSequence()) {
            Assertions.assertTrue(c.contains(vertex), "Expected vertex to be contained in circle: " + vertex);
        }

        // circle must contain centroid
        EuclideanTestUtils.assertRegionLocation(c, RegionLocation.INSIDE, tree.getCentroid());

        // area must be less than the circle
        Assertions.assertTrue(tree.getSize() < c.getSize(), "Expected approximation area to be less than circle");
    }

    private static void assertFiniteSegment(final LineConvexSubset segment, final Vector2D start, final Vector2D end) {
        Assertions.assertFalse(segment.isInfinite());
        Assertions.assertTrue(segment.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }
}
