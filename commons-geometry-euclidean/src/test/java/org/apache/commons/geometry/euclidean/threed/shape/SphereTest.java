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
package org.apache.commons.geometry.euclidean.threed.shape;

import java.io.IOException;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.threed.line.LinecastPoint3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class SphereTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFrom() {
        // arrange
        Vector3D center = Vector3D.of(1, 2, 3);

        // act
        Sphere s = Sphere.from(center, 3, TEST_PRECISION);

        // act/assert
        Assert.assertFalse(s.isFull());
        Assert.assertFalse(s.isEmpty());

        Assert.assertSame(center, s.getCenter());
        Assert.assertSame(center, s.getBarycenter());

        Assert.assertEquals(3, s.getRadius(), 0.0);

        Assert.assertSame(TEST_PRECISION, s.getPrecision());
    }

    @Test
    public void testFrom_illegalCenter() {
        // act/assert
        GeometryTestUtils.assertThrows(
            () -> Sphere.from(Vector3D.of(Double.POSITIVE_INFINITY, 1, 2), 1, TEST_PRECISION),
            IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(
            () -> Sphere.from(Vector3D.of(Double.NaN, 1, 2), 1, TEST_PRECISION),
            IllegalArgumentException.class);
    }

    @Test
    public void testFrom_illegalRadius() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        // act/assert
        GeometryTestUtils.assertThrows(() -> Sphere.from(Vector3D.ZERO, -1, TEST_PRECISION),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(() -> Sphere.from(Vector3D.ZERO, 0, TEST_PRECISION),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(() -> Sphere.from(Vector3D.ZERO, Double.POSITIVE_INFINITY, TEST_PRECISION),
                IllegalArgumentException.class);
        GeometryTestUtils.assertThrows(() -> Sphere.from(Vector3D.ZERO, Double.NaN, TEST_PRECISION),
                IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> Sphere.from(Vector3D.ZERO, 1e-3, precision),
                IllegalArgumentException.class);
    }

    @Test
    public void testGeometricProperties() {
        // arrange
        double r = 2;
        Sphere s = Sphere.from(Vector3D.of(1, 2, 3), r, TEST_PRECISION);

        // act/assert
        Assert.assertEquals(4 * Math.PI * r * r, s.getBoundarySize(), TEST_EPS);
        Assert.assertEquals((4.0 * Math.PI * r * r * r) / 3.0, s.getSize(), TEST_EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        Vector3D center = Vector3D.of(1, 2, 3);
        double radius = 4;
        Sphere s = Sphere.from(center, radius, TEST_PRECISION);

        EuclideanTestUtils.permute(0, PlaneAngleRadians.TWO_PI, 0.2, (azimuth, polar) -> {
            // act/assert
            EuclideanTestUtils.assertRegionLocation(s, RegionLocation.OUTSIDE,
                    SphericalCoordinates.of(radius + 1, azimuth, polar)
                        .toVector()
                        .add(center));

            EuclideanTestUtils.assertRegionLocation(s, RegionLocation.BOUNDARY,
                    SphericalCoordinates.of(radius + 1e-12, azimuth, polar)
                        .toVector()
                        .add(center));

            EuclideanTestUtils.assertRegionLocation(s, RegionLocation.INSIDE,
                    SphericalCoordinates.of(radius - 1, azimuth, polar)
                        .toVector()
                        .add(center));
        });
    }

    @Test
    public void testContains() {
     // arrange
        Vector3D center = Vector3D.of(1, 2, 3);
        double radius = 4;
        Sphere s = Sphere.from(center, radius, TEST_PRECISION);

        EuclideanTestUtils.permute(0, PlaneAngleRadians.TWO_PI, 0.2, (azimuth, polar) -> {
            // act/assert
            checkContains(s, false,
                    SphericalCoordinates.of(radius + 1, azimuth, polar)
                        .toVector()
                        .add(center));

            checkContains(s, true,
                    SphericalCoordinates.of(radius - 1, azimuth, polar)
                        .toVector()
                        .add(center),
                    SphericalCoordinates.of(radius + 1e-12, azimuth, polar)
                        .toVector()
                        .add(center));
        });
    }

    @Test
    public void testProject() {
        // arrange
        Vector3D center = Vector3D.of(1.5, 2.5, 3.5);
        double radius = 3;
        Sphere s = Sphere.from(center, radius, TEST_PRECISION);

        EuclideanTestUtils.permute(-4, 4, 1, (x, y, z) -> {
            Vector3D pt = Vector3D.of(x, y, z);

            // act
            Vector3D projection = s.project(pt);

            // assert
            Assert.assertEquals(radius, center.distance(projection), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(center.directionTo(pt),
                    center.directionTo(projection), TEST_EPS);
        });
    }

    @Test
    public void testProject_argumentEqualsCenter() {
        // arrange
        Sphere c = Sphere.from(Vector3D.of(1, 2, 3), 2, TEST_PRECISION);

        // act
        Vector3D projection = c.project(Vector3D.of(1, 2, 3));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 2, 3), projection, TEST_EPS);
    }

    @Test
    public void testIntersections() {
        // --- arrange
        Sphere s = Sphere.from(Vector3D.of(2, 1, 3), 2, TEST_PRECISION);
        double sqrt3 = Math.sqrt(3);

        // --- act/assert
        // descending along y in x-y plane
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(-1, 4, 3), Vector3D.of(5, 4, 3), TEST_PRECISION));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(-1, 3, 3), Vector3D.of(5, 3, 3), TEST_PRECISION),
                Vector3D.of(2, 3, 3));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(-1, 2, 3), Vector3D.of(5, 2, 3), TEST_PRECISION),
                Vector3D.of(2 - sqrt3, 2, 3), Vector3D.of(2 + sqrt3, 2, 3));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(-1, 1, 3), Vector3D.of(5, 1, 3), TEST_PRECISION),
                Vector3D.of(0, 1, 3), Vector3D.of(4, 1, 3));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(-1, 0, 3), Vector3D.of(5, 0, 3), TEST_PRECISION),
                Vector3D.of(2 - sqrt3, 0, 3), Vector3D.of(2 + sqrt3, 0, 3));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(-1, -1, 3), Vector3D.of(5, -1, 3), TEST_PRECISION),
                Vector3D.of(2, -1, 3));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(-1, -2, 3), Vector3D.of(5, -2, 3), TEST_PRECISION));

        // ascending along x in x-y plane
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(-1, -2, 3), Vector3D.of(-1, 5, 3), TEST_PRECISION));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(0, -2, 3), Vector3D.of(0, 5, 3), TEST_PRECISION),
                Vector3D.of(0, 1, 3));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(1, -2, 3), Vector3D.of(1, 5, 3), TEST_PRECISION),
                Vector3D.of(1, 1 - sqrt3, 3), Vector3D.of(1, 1 + sqrt3, 3));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(2, -2, 3), Vector3D.of(2, 5, 3), TEST_PRECISION),
                Vector3D.of(2, -1, 3), Vector3D.of(2, 3, 3));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(3, -2, 3), Vector3D.of(3, 5, 3), TEST_PRECISION),
                Vector3D.of(3, 1 - sqrt3, 3), Vector3D.of(3, 1 + sqrt3, 3));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(4, -2, 3), Vector3D.of(4, 5, 3), TEST_PRECISION),
                Vector3D.of(4, 1, 3));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(5, -2, 3), Vector3D.of(5, 5, 3), TEST_PRECISION));

        // descending along z in y-z plane
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(2, -2, 6), Vector3D.of(2, 4, 6), TEST_PRECISION));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(2, -2, 5), Vector3D.of(2, 4, 5), TEST_PRECISION),
                Vector3D.of(2, 1, 5));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(2, -2, 4), Vector3D.of(2, 4, 4), TEST_PRECISION),
                Vector3D.of(2, 1 - sqrt3, 4), Vector3D.of(2, 1 + sqrt3, 4));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(2, -2, 3), Vector3D.of(2, 4, 3), TEST_PRECISION),
                Vector3D.of(2, -1, 3), Vector3D.of(2, 3, 3));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(2, -2, 2), Vector3D.of(2, 4, 2), TEST_PRECISION),
                Vector3D.of(2, 1 - sqrt3, 2), Vector3D.of(2, 1 + sqrt3, 2));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(2, -2, 1), Vector3D.of(2, 4, 1), TEST_PRECISION),
                Vector3D.of(2, 1, 1));
        checkIntersections(s, Lines3D.fromPoints(Vector3D.of(2, -2, 0), Vector3D.of(2, 4, 0), TEST_PRECISION));

        // diagonal from origin
        Vector3D center = s.getCenter();
        checkIntersections(s, Lines3D.fromPoints(Vector3D.ZERO, s.getCenter(), TEST_PRECISION),
                center.withNorm(center.norm() - s.getRadius()), center.withNorm(center.norm() + s.getRadius()));
    }

    @Test
    public void testLinecast() {
        // arrange
        Sphere s = Sphere.from(Vector3D.of(2, 1, 3), 2, TEST_PRECISION);
        double sqrt3 = Math.sqrt(3);

        // act/assert
        checkLinecast(s, Lines3D.segmentFromPoints(Vector3D.of(-1, 0, 3), Vector3D.of(5, 0, 3), TEST_PRECISION),
                Vector3D.of(2 - sqrt3, 0, 3), Vector3D.of(2 + sqrt3, 0, 3));
        checkLinecast(s, Lines3D.segmentFromPoints(Vector3D.of(-1, 3, 3), Vector3D.of(5, 3, 3), TEST_PRECISION),
                Vector3D.of(2, 3, 3));
        checkLinecast(s, Lines3D.segmentFromPoints(Vector3D.of(-1, -2, 3), Vector3D.of(5, -2, 3), TEST_PRECISION));
    }

    @Test
    public void testLinecast_intersectionsNotInSegment() {
        // arrange
        Sphere s = Sphere.from(Vector3D.of(2, 1, 3), 2, TEST_PRECISION);
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        checkLinecast(s, line.segment(-1, 0));
        checkLinecast(s, line.segment(1.5, 2.5));
        checkLinecast(s, line.segment(1.5, 2.5));
        checkLinecast(s, line.segment(4, 5));
    }

    @Test
    public void testLinecast_segmentPointOnBoundary() {
        // arrange
        Sphere s = Sphere.from(Vector3D.of(2, 1, 3), 2, TEST_PRECISION);
        Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_X, TEST_PRECISION);
        double sqrt3 = Math.sqrt(3);
        double start = 2 - sqrt3;
        double end = 2 + sqrt3;

        // act/assert
        checkLinecast(s, line.segment(start, 2), Vector3D.of(start, 0, 3));
        checkLinecast(s, line.segment(start, end), Vector3D.of(start, 0, 3), Vector3D.of(end, 0, 3));
        checkLinecast(s, line.segment(end, 5), Vector3D.of(end, 0, 3));
    }

    @Test
    public void testToTree_smallestNumberOfPlanes() throws IOException {
        // arrange
        double r = 2;
        Sphere s = Sphere.from(Vector3D.of(2, 1, 3), r, TEST_PRECISION);

        // act
        RegionBSPTree3D tree = s.toTree(2, 3);

        // assert
        checkBasicApproximationProperties(s, tree);

        List<PlaneConvexSubset> boundaries = tree.getBoundaries();
        Assert.assertEquals(6, boundaries.size());

        double expectedSize = 0.5 * Math.sqrt(3) * (r * r * r);
        Assert.assertEquals(expectedSize, tree.getSize(), TEST_EPS);
    }

    @Test
    public void testToTree_multipleApproximationSizes() throws Exception {
        // -- arrange
        Sphere s = Sphere.from(Vector3D.of(-3, 5, 1), 10, TEST_PRECISION);

        int min = 4;
        int max = 50;

        RegionBSPTree3D tree;

        double sizeDiff;
        double prevSizeDiff = Double.POSITIVE_INFINITY;

        for (int n = min; n <= max; ++n) {
            // -- act
            tree = s.toTree(n, n);

            // -- assert
            checkBasicApproximationProperties(s, tree);

            // check that we get closer and closer to the correct size as we add more segments
            sizeDiff = s.getSize() - tree.getSize();
            Assert.assertTrue("Expected size difference to decrease: n= " +
                    n + ", prevSizeDiff= " + prevSizeDiff + ", sizeDiff= " + sizeDiff, sizeDiff < prevSizeDiff);

            prevSizeDiff = sizeDiff;
        }
    }

    @Test
    public void testToTree_closeApproximation() throws IOException {
        // arrange
        Sphere s = Sphere.from(Vector3D.of(0, -1, 2), 1, TEST_PRECISION);

        // act
        RegionBSPTree3D tree = s.toTree(25, 40);

        // assert
        checkBasicApproximationProperties(s, tree);

        double eps = 0.1;
        Assert.assertEquals(s.getSize(), tree.getSize(), eps);
        Assert.assertEquals(s.getBoundarySize(), tree.getBoundarySize(), eps);
        EuclideanTestUtils.assertCoordinatesEqual(s.getBarycenter(), tree.getBarycenter(), eps);
    }

    @Test
    public void testToTree_invalidArgs() {
        // arrange
        Sphere s = Sphere.from(Vector3D.of(2, 1, 3), 2, TEST_PRECISION);
        String baseStackMsg = "Sphere approximation stack number must be greater than or equal to 2; was ";
        String baseSliceMsg = "Sphere approximation slice number must be greater than or equal to 3; was ";

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            s.toTree(1, 10);
        }, IllegalArgumentException.class, baseStackMsg + "1");
        GeometryTestUtils.assertThrows(() -> {
            s.toTree(-1, 10);
        }, IllegalArgumentException.class, baseStackMsg + "-1");
        GeometryTestUtils.assertThrows(() -> {
            s.toTree(0, -1);
        }, IllegalArgumentException.class, baseStackMsg + "0");

        GeometryTestUtils.assertThrows(() -> {
            s.toTree(2, 2);
        }, IllegalArgumentException.class, baseSliceMsg + "2");
        GeometryTestUtils.assertThrows(() -> {
            s.toTree(4, -1);
        }, IllegalArgumentException.class, baseSliceMsg + "-1");
    }

    @Test
    public void testHashCode() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        Sphere a = Sphere.from(Vector3D.of(1, 2, 3), 3, TEST_PRECISION);
        Sphere b = Sphere.from(Vector3D.of(1, 1, 3), 3, TEST_PRECISION);
        Sphere c = Sphere.from(Vector3D.of(1, 2, 3), 4, TEST_PRECISION);
        Sphere d = Sphere.from(Vector3D.of(1, 2, 3), 3, precision);
        Sphere e = Sphere.from(Vector3D.of(1, 2, 3), 3, TEST_PRECISION);

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

        Sphere a = Sphere.from(Vector3D.of(1, 2, 3), 3, TEST_PRECISION);
        Sphere b = Sphere.from(Vector3D.of(1, 1, 3), 3, TEST_PRECISION);
        Sphere c = Sphere.from(Vector3D.of(1, 2, 3), 4, TEST_PRECISION);
        Sphere d = Sphere.from(Vector3D.of(1, 2, 3), 3, precision);
        Sphere e = Sphere.from(Vector3D.of(1, 2, 3), 3, TEST_PRECISION);

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
        Sphere c = Sphere.from(Vector3D.of(1, 2, 3), 3, TEST_PRECISION);

        // act
        String str = c.toString();

        // assert
        Assert.assertEquals("Sphere[center= (1.0, 2.0, 3.0), radius= 3.0]", str);
    }

    private static void checkContains(Sphere sphere, boolean contains, Vector3D... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Expected circle to " + (contains ? "" : "not") + "contain point " + pt,
                    contains, sphere.contains(pt));
        }
    }

    private static void checkIntersections(Sphere sphere, Line3D line, Vector3D... expectedPts) {
        // --- act
        // compute the intersections forward and reverse
        List<Vector3D> actualPtsForward = sphere.intersections(line);
        List<Vector3D> actualPtsReverse = sphere.intersections(line.reverse());

        Vector3D actualFirstForward = sphere.firstIntersection(line);
        Vector3D actualFirstReverse = sphere.firstIntersection(line.reverse());

        // --- assert
        int len = expectedPts.length;

        // check the lists
        Assert.assertEquals(len, actualPtsForward.size());
        Assert.assertEquals(len, actualPtsReverse.size());

        for (int i = 0; i < len; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[i], actualPtsForward.get(i), TEST_EPS);
            Assert.assertEquals(sphere.getRadius(), sphere.getCenter().distance(actualPtsForward.get(i)), TEST_EPS);

            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[len - i - 1], actualPtsReverse.get(i), TEST_EPS);
            Assert.assertEquals(sphere.getRadius(), sphere.getCenter().distance(actualPtsReverse.get(i)), TEST_EPS);
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

    private static void checkLinecast(Sphere s, LineConvexSubset3D segment, Vector3D... expectedPts) {
        // check linecast
        List<LinecastPoint3D> results = s.linecast(segment);
        Assert.assertEquals(expectedPts.length, results.size());

        LinecastPoint3D actual;
        Vector3D expected;
        for (int i = 0; i < expectedPts.length; ++i) {
            expected = expectedPts[i];
            actual = results.get(i);

            EuclideanTestUtils.assertCoordinatesEqual(expected, actual.getPoint(), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(s.getCenter().directionTo(expected), actual.getNormal(), TEST_EPS);
            Assert.assertSame(segment.getLine(), actual.getLine());
        }

        // check linecastFirst
        LinecastPoint3D firstResult = s.linecastFirst(segment);
        if (expectedPts.length > 0) {
            Assert.assertEquals(results.get(0), firstResult);
        } else {
            Assert.assertNull(firstResult);
        }
    }

    /**
     * Check a number of standard properties for bsp trees generated as sphere approximations.
     */
    private static void checkBasicApproximationProperties(Sphere s, RegionBSPTree3D tree) {
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        // volume must be less than the sphere
        Assert.assertTrue("Expected approximation volume to be less than circle", tree.getSize() < s.getSize());

        // all vertices must be inside the sphere or on the boundary
        for (PlaneConvexSubset boundary : tree.getBoundaries()) {
            Assert.assertTrue(boundary.isFinite());

            for (Vector3D vertex : boundary.getVertices()) {
                Assert.assertTrue("Expected vertex to be contained in sphere: " + vertex, s.contains(vertex));
            }
        }


        // sphere must contain barycenter
        EuclideanTestUtils.assertRegionLocation(s, RegionLocation.INSIDE, tree.getBarycenter());
    }
}
