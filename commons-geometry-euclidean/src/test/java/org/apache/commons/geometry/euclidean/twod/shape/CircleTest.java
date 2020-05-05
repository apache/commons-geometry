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
import java.util.Collections;
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
import org.junit.Assert;
import org.junit.Test;

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
        Vector2D center = Vector2D.of(1, 2);

        // act
        Circle c = Circle.from(center, 3, TEST_PRECISION);

        // act/assert
        Assert.assertFalse(c.isFull());
        Assert.assertFalse(c.isEmpty());

        Assert.assertSame(center, c.getCenter());
        Assert.assertSame(center, c.getBarycenter());

        Assert.assertEquals(3, c.getRadius(), 0.0);

        Assert.assertSame(TEST_PRECISION, c.getPrecision());
    }

    @Test
    public void testFrom_illegalCenter() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> Circle.from(Vector2D.of(Double.POSITIVE_INFINITY, 1), 1, TEST_PRECISION),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(() -> Circle.from(Vector2D.of(Double.NaN, 1), 1, TEST_PRECISION),
                IllegalArgumentException.class);
    }

    @Test
    public void testFrom_illegalRadius() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        // act/assert
        GeometryTestUtils.assertThrows(() -> Circle.from(Vector2D.ZERO, -1, TEST_PRECISION),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(() -> Circle.from(Vector2D.ZERO, 0, TEST_PRECISION),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(() -> Circle.from(Vector2D.ZERO, Double.POSITIVE_INFINITY, TEST_PRECISION),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(() -> Circle.from(Vector2D.ZERO, Double.NaN, TEST_PRECISION),
                IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> Circle.from(Vector2D.ZERO, 1e-3, precision),
                IllegalArgumentException.class);
    }

    @Test
    public void testGeometricProperties() {
        // arrange
        double r = 2;
        Circle c = Circle.from(Vector2D.of(1, 2), r, TEST_PRECISION);

        // act/assert
        Assert.assertEquals(2 * Math.PI * r, c.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(Math.PI * r * r, c.getSize(), TEST_EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        Circle c = Circle.from(Vector2D.of(1, 2), 1, TEST_PRECISION);

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
        Circle c = Circle.from(Vector2D.of(1, 2), 1, TEST_PRECISION);

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
        Vector2D center = Vector2D.of(1.5, 2.5);
        double radius = 3;
        Circle c = Circle.from(center, radius, TEST_PRECISION);

        EuclideanTestUtils.permute(-4, 4, 1, (x, y) -> {
            Vector2D pt = Vector2D.of(x, y);

            // act
            Vector2D projection = c.project(pt);

            // assert
            Assert.assertEquals(radius, center.distance(projection), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(center.directionTo(pt),
                    center.directionTo(projection), TEST_EPS);
        });
    }

    @Test
    public void testProject_argumentEqualsCenter() {
        // arrange
        Circle c = Circle.from(Vector2D.of(1, 2), 2, TEST_PRECISION);

        // act
        Vector2D projection = c.project(Vector2D.of(1, 2));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(3, 2), projection, TEST_EPS);
    }

    @Test
    public void testIntersections() {
        // --- arrange
        Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);
        double sqrt3 = Math.sqrt(3);

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
        Vector2D center = c.getCenter();
        checkIntersections(c, Lines.fromPoints(Vector2D.ZERO, c.getCenter(), TEST_PRECISION),
                center.withNorm(center.norm() - c.getRadius()), center.withNorm(center.norm() + c.getRadius()));
    }

    @Test
    public void testLinecast() {
        // arrange
        Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);
        double sqrt3 = Math.sqrt(3);

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
        Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);
        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION);

        // act/assert
        checkLinecast(c, line.segment(-1, 0));
        checkLinecast(c, line.segment(1.5, 2.5));
        checkLinecast(c, line.segment(1.5, 2.5));
        checkLinecast(c, line.segment(4, 5));
    }

    @Test
    public void testLinecast_segmentPointOnBoundary() {
        // arrange
        Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);
        Line line = Lines.fromPointAndAngle(Vector2D.ZERO, 0, TEST_PRECISION);
        double sqrt3 = Math.sqrt(3);
        double start = 2 - sqrt3;
        double end = 2 + sqrt3;

        // act/assert
        checkLinecast(c, line.segment(start, 2), Vector2D.of(start, 0));
        checkLinecast(c, line.segment(start, end), Vector2D.of(start, 0), Vector2D.of(end, 0));
        checkLinecast(c, line.segment(end, 5), Vector2D.of(end, 0));
    }

    @Test
    public void testToTree_threeSegments() {
        // arrange
        Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);

        // act
        RegionBSPTree2D tree = c.toTree(3);

        // assert
        checkBasicApproximationProperties(c, tree);

        List<LineConvexSubset> segments = new ArrayList<>(tree.getBoundaries());
        Collections.sort(segments, SEGMENT_DIRECTION_COMPARATOR);

        Assert.assertEquals(3, segments.size());

        double inc = PlaneAngleRadians.TWO_PI / 3.0;
        Vector2D p0 = Vector2D.of(4, 1);
        Vector2D p1 = Vector2D.of(
                (2 * Math.cos(inc)) + 2,
                (2 * Math.sin(inc)) + 1);
        Vector2D p2 = Vector2D.of(
                (2 * Math.cos(2 * inc)) + 2,
                (2 * Math.sin(2 * inc)) + 1);

        assertFiniteSegment(segments.get(0), p0, p1);
        assertFiniteSegment(segments.get(1), p1, p2);
        assertFiniteSegment(segments.get(2), p2, p0);
    }

    @Test
    public void testToTree_fourSegments() {
        // arrange
        Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);

        // act
        RegionBSPTree2D tree = c.toTree(4);

        // assert
        checkBasicApproximationProperties(c, tree);

        List<LineConvexSubset> segments = new ArrayList<>(tree.getBoundaries());
        Collections.sort(segments, SEGMENT_DIRECTION_COMPARATOR);

        Assert.assertEquals(4, segments.size());

        Vector2D p0 = Vector2D.of(4, 1);
        Vector2D p1 = Vector2D.of(2, 3);
        Vector2D p2 = Vector2D.of(0, 1);
        Vector2D p3 = Vector2D.of(2, -1);

        assertFiniteSegment(segments.get(0), p1, p2);
        assertFiniteSegment(segments.get(1), p0, p1);
        assertFiniteSegment(segments.get(2), p2, p3);
        assertFiniteSegment(segments.get(3), p3, p0);
    }

    @Test
    public void testToTree_multipleApproximationSizes() {
        // -- arrange
        Circle c = Circle.from(Vector2D.of(-3, 5), 10, TEST_PRECISION);

        int min = 5;
        int max = 100;

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
            Assert.assertTrue("Expected size difference to decrease", sizeDiff < prevSizeDiff);

            prevSizeDiff = sizeDiff;
        }
    }

    @Test
    public void testToTree_closeApproximation() {
        // arrange
        Circle c = Circle.from(Vector2D.of(-2, 0), 1, TEST_PRECISION);

        // act
        RegionBSPTree2D tree = c.toTree(100);

        // assert
        checkBasicApproximationProperties(c, tree);

        double eps = 5e-3;
        Assert.assertEquals(c.getSize(), tree.getSize(), eps);
        Assert.assertEquals(c.getBoundarySize(), tree.getBoundarySize(), eps);
        EuclideanTestUtils.assertCoordinatesEqual(c.getBarycenter(), tree.getBarycenter(), eps);
    }

    @Test
    public void testToTree_invalidSegmentCount() {
        // arrange
        Circle c = Circle.from(Vector2D.of(2, 1), 2, TEST_PRECISION);
        String baseMsg = "Circle approximation segment number must be greater than or equal to 3; was ";

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            c.toTree(2);
        }, IllegalArgumentException.class, baseMsg + "2");
        GeometryTestUtils.assertThrows(() -> {
            c.toTree(-1);
        }, IllegalArgumentException.class, baseMsg + "-1");
    }

    @Test
    public void testHashCode() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        Circle a = Circle.from(Vector2D.of(1, 2), 3, TEST_PRECISION);
        Circle b = Circle.from(Vector2D.of(1, 1), 3, TEST_PRECISION);
        Circle c = Circle.from(Vector2D.of(1, 2), 4, TEST_PRECISION);
        Circle d = Circle.from(Vector2D.of(1, 2), 3, precision);
        Circle e = Circle.from(Vector2D.of(1, 2), 3, TEST_PRECISION);

        // act
        int hash = a.hashCode();

        // act/assert
        Assert.assertEquals(hash, a.hashCode());

        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertNotEquals(hash, c.hashCode());
        Assert.assertNotEquals(hash, d.hashCode());

        Assert.assertEquals(hash, e.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        Circle a = Circle.from(Vector2D.of(1, 2), 3, TEST_PRECISION);
        Circle b = Circle.from(Vector2D.of(1, 1), 3, TEST_PRECISION);
        Circle c = Circle.from(Vector2D.of(1, 2), 4, TEST_PRECISION);
        Circle d = Circle.from(Vector2D.of(1, 2), 3, precision);
        Circle e = Circle.from(Vector2D.of(1, 2), 3, TEST_PRECISION);

        // act/assert
        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(a.equals(c));
        Assert.assertFalse(a.equals(d));

        Assert.assertTrue(a.equals(e));
    }

    @Test
    public void testToString() {
        // arrange
        Circle c = Circle.from(Vector2D.of(1, 2), 3, TEST_PRECISION);

        // act
        String str = c.toString();

        // assert
        Assert.assertEquals("Circle[center= (1.0, 2.0), radius= 3.0]", str);
    }

    private static void checkContains(Circle circle, boolean contains, Vector2D... pts) {
        for (Vector2D pt : pts) {
            Assert.assertEquals("Expected circle to " + (contains ? "" : "not") + "contain point " + pt,
                    contains, circle.contains(pt));
        }
    }

    private static void checkIntersections(Circle circle, Line line, Vector2D... expectedPts) {
        // --- act
        // compute the intersections forward and reverse
        List<Vector2D> actualPtsForward = circle.intersections(line);
        List<Vector2D> actualPtsReverse = circle.intersections(line.reverse());

        Vector2D actualFirstForward = circle.firstIntersection(line);
        Vector2D actualFirstReverse = circle.firstIntersection(line.reverse());

        // --- assert
        int len = expectedPts.length;

        // check the lists
        Assert.assertEquals(len, actualPtsForward.size());
        Assert.assertEquals(len, actualPtsReverse.size());

        for (int i = 0; i < len; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[i], actualPtsForward.get(i), TEST_EPS);
            Assert.assertEquals(circle.getRadius(), circle.getCenter().distance(actualPtsForward.get(i)), TEST_EPS);

            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[len - i - 1], actualPtsReverse.get(i), TEST_EPS);
            Assert.assertEquals(circle.getRadius(), circle.getCenter().distance(actualPtsReverse.get(i)), TEST_EPS);
        }

        // check the single intersection points
        if (len > 0) {
            Assert.assertNotNull(actualFirstForward);
            Assert.assertNotNull(actualFirstReverse);

            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[0], actualFirstForward, TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[len - 1], actualFirstReverse, TEST_EPS);
        } else {
            Assert.assertNull(actualFirstForward);
            Assert.assertNull(actualFirstReverse);
        }
    }

    private static void checkLinecast(Circle c, LineConvexSubset segment, Vector2D... expectedPts) {
        // check linecast
        List<LinecastPoint2D> results = c.linecast(segment);
        Assert.assertEquals(expectedPts.length, results.size());

        LinecastPoint2D actual;
        Vector2D expected;
        for (int i = 0; i < expectedPts.length; ++i) {
            expected = expectedPts[i];
            actual = results.get(i);

            EuclideanTestUtils.assertCoordinatesEqual(expected, actual.getPoint(), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(c.getCenter().directionTo(expected), actual.getNormal(), TEST_EPS);
            Assert.assertSame(segment.getLine(), actual.getLine());
        }

        // check linecastFirst
        LinecastPoint2D firstResult = c.linecastFirst(segment);
        if (expectedPts.length > 0) {
            Assert.assertEquals(results.get(0), firstResult);
        } else {
            Assert.assertNull(firstResult);
        }
    }

    /**
     * Check a number of standard properties for bsp trees generated as circle approximations.
     */
    private static void checkBasicApproximationProperties(Circle c, RegionBSPTree2D tree) {
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        // all vertices must be inside the circle or on the boundary
        List<LinePath> paths = tree.getBoundaryPaths();
        Assert.assertEquals(1, paths.size());

        LinePath path = paths.get(0);
        Assert.assertTrue(path.isFinite());

        for (Vector2D vertex : path.getVertexSequence()) {
            Assert.assertTrue("Expected vertex to be contained in circle: " + vertex, c.contains(vertex));
        }

        // circle must contain barycenter
        EuclideanTestUtils.assertRegionLocation(c, RegionLocation.INSIDE, tree.getBarycenter());

        // area must be less than the circle
        Assert.assertTrue("Expected approximation area to be less than circle", tree.getSize() < c.getSize());
    }

    private static void assertFiniteSegment(LineConvexSubset segment, Vector2D start, Vector2D end) {
        Assert.assertFalse(segment.isInfinite());
        Assert.assertTrue(segment.isFinite());

        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }
}
