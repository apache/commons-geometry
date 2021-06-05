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
import java.util.function.DoubleSupplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.Bounds3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.geometry.euclidean.threed.Triangle3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.line.Line3D;
import org.apache.commons.geometry.euclidean.threed.line.LineConvexSubset3D;
import org.apache.commons.geometry.euclidean.threed.line.LinecastPoint3D;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.mesh.TriangleMesh;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SphereTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    public void testFrom() {
        // arrange
        final Vector3D center = Vector3D.of(1, 2, 3);

        // act
        final Sphere s = Sphere.from(center, 3, TEST_PRECISION);

        // act/assert
        Assertions.assertFalse(s.isFull());
        Assertions.assertFalse(s.isEmpty());

        Assertions.assertSame(center, s.getCenter());
        Assertions.assertSame(center, s.getCentroid());

        Assertions.assertEquals(3, s.getRadius(), 0.0);

        Assertions.assertSame(TEST_PRECISION, s.getPrecision());
    }

    @Test
    public void testFrom_illegalCenter() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sphere.from(Vector3D.of(Double.POSITIVE_INFINITY, 1, 2), 1, TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sphere.from(Vector3D.of(Double.NaN, 1, 2), 1, TEST_PRECISION));
    }

    @Test
    public void testFrom_illegalRadius() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sphere.from(Vector3D.ZERO, -1, TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sphere.from(Vector3D.ZERO, 0, TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sphere.from(Vector3D.ZERO, Double.POSITIVE_INFINITY, TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sphere.from(Vector3D.ZERO, Double.NaN, TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> Sphere.from(Vector3D.ZERO, 1e-3, precision));
    }

    @Test
    public void testGeometricProperties() {
        // arrange
        final double r = 2;
        final Sphere s = Sphere.from(Vector3D.of(1, 2, 3), r, TEST_PRECISION);

        // act/assert
        Assertions.assertEquals(4 * Math.PI * r * r, s.getBoundarySize(), TEST_EPS);
        Assertions.assertEquals((4.0 * Math.PI * r * r * r) / 3.0, s.getSize(), TEST_EPS);
    }

    @Test
    public void testClassify() {
        // arrange
        final Vector3D center = Vector3D.of(1, 2, 3);
        final double radius = 4;
        final Sphere s = Sphere.from(center, radius, TEST_PRECISION);

        EuclideanTestUtils.permute(0, Angle.TWO_PI, 0.2, (azimuth, polar) -> {
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
        final Vector3D center = Vector3D.of(1, 2, 3);
        final double radius = 4;
        final Sphere s = Sphere.from(center, radius, TEST_PRECISION);

        EuclideanTestUtils.permute(0, Angle.TWO_PI, 0.2, (azimuth, polar) -> {
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
        final Vector3D center = Vector3D.of(1.5, 2.5, 3.5);
        final double radius = 3;
        final Sphere s = Sphere.from(center, radius, TEST_PRECISION);

        EuclideanTestUtils.permute(-4, 4, 1, (x, y, z) -> {
            final Vector3D pt = Vector3D.of(x, y, z);

            // act
            final Vector3D projection = s.project(pt);

            // assert
            Assertions.assertEquals(radius, center.distance(projection), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(center.directionTo(pt),
                    center.directionTo(projection), TEST_EPS);
        });
    }

    @Test
    public void testProject_argumentEqualsCenter() {
        // arrange
        final Sphere c = Sphere.from(Vector3D.of(1, 2, 3), 2, TEST_PRECISION);

        // act
        final Vector3D projection = c.project(Vector3D.of(1, 2, 3));

        // assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 2, 3), projection, TEST_EPS);
    }

    @Test
    public void testIntersections() {
        // --- arrange
        final Sphere s = Sphere.from(Vector3D.of(2, 1, 3), 2, TEST_PRECISION);
        final double sqrt3 = Math.sqrt(3);

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
        final Vector3D center = s.getCenter();
        checkIntersections(s, Lines3D.fromPoints(Vector3D.ZERO, s.getCenter(), TEST_PRECISION),
                center.withNorm(center.norm() - s.getRadius()), center.withNorm(center.norm() + s.getRadius()));
    }

    @Test
    public void testLinecast() {
        // arrange
        final Sphere s = Sphere.from(Vector3D.of(2, 1, 3), 2, TEST_PRECISION);
        final double sqrt3 = Math.sqrt(3);

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
        final Sphere s = Sphere.from(Vector3D.of(2, 1, 3), 2, TEST_PRECISION);
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        // act/assert
        checkLinecast(s, line.segment(-1, 0));
        checkLinecast(s, line.segment(1.5, 2.5));
        checkLinecast(s, line.segment(1.5, 2.5));
        checkLinecast(s, line.segment(4, 5));
    }

    @Test
    public void testLinecast_segmentPointOnBoundary() {
        // arrange
        final Sphere s = Sphere.from(Vector3D.of(2, 1, 3), 2, TEST_PRECISION);
        final Line3D line = Lines3D.fromPointAndDirection(Vector3D.of(0, 0, 3), Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final double sqrt3 = Math.sqrt(3);
        final double start = 2 - sqrt3;
        final double end = 2 + sqrt3;

        // act/assert
        checkLinecast(s, line.segment(start, 2), Vector3D.of(start, 0, 3));
        checkLinecast(s, line.segment(start, end), Vector3D.of(start, 0, 3), Vector3D.of(end, 0, 3));
        checkLinecast(s, line.segment(end, 5), Vector3D.of(end, 0, 3));
    }

    @Test
    public void testToTree_zeroSubdivisions() throws IOException {
        // arrange
        final double r = 2;
        final Sphere s = Sphere.from(Vector3D.of(2, 1, 3), r, TEST_PRECISION);

        // act
        final RegionBSPTree3D tree = s.toTree(0);

        // assert
        checkBasicApproximationProperties(s, tree);

        final List<PlaneConvexSubset> boundaries = tree.getBoundaries();
        Assertions.assertEquals(8, boundaries.size());

        final List<Triangle3D> triangles = tree.triangleStream().collect(Collectors.toList());
        Assertions.assertEquals(8, triangles.size());

        final double expectedSize = (4.0 / 3.0) * r * r * r;
        Assertions.assertEquals(expectedSize, tree.getSize(), TEST_EPS);
    }

    @Test
    public void testToTree_oneSubdivision() throws IOException {
        // arrange
        final double r = 2;
        final Sphere s = Sphere.from(Vector3D.of(2, 1, 3), r, TEST_PRECISION);

        // act
        final RegionBSPTree3D tree = s.toTree(1);

        // assert
        checkBasicApproximationProperties(s, tree);

        final List<PlaneConvexSubset> boundaries = tree.getBoundaries();
        Assertions.assertEquals(32, boundaries.size());

        final List<Triangle3D> triangles = tree.triangleStream().collect(Collectors.toList());
        Assertions.assertEquals(32, triangles.size());

        Assertions.assertTrue(tree.getSize() <= s.getSize());
    }

    @Test
    public void testToTree_multipleSubdivisionCounts() {
        // -- arrange
        final Sphere s = Sphere.from(Vector3D.of(-3, 5, 1), 10, TEST_PRECISION);

        final int min = 0;
        final int max = 5;

        RegionBSPTree3D tree;

        double sizeDiff;
        double prevSizeDiff = Double.POSITIVE_INFINITY;

        for (int n = min; n <= max; ++n) {
            // -- act
            tree = s.toTree(n);

            // -- assert
            checkBasicApproximationProperties(s, tree);

            final int expectedTriangles = (int) (8 * Math.pow(4, n));
            final List<PlaneConvexSubset> boundaries = tree.getBoundaries();
            Assertions.assertEquals(expectedTriangles, boundaries.size());

            final List<Triangle3D> triangles = tree.triangleStream().collect(Collectors.toList());
            Assertions.assertEquals(expectedTriangles, triangles.size());

            // check that we get closer and closer to the correct size as we add more segments
            sizeDiff = s.getSize() - tree.getSize();
            Assertions.assertTrue(sizeDiff < prevSizeDiff, "Expected size difference to decrease: n= " +
                    n + ", prevSizeDiff= " + prevSizeDiff + ", sizeDiff= " + sizeDiff);

            prevSizeDiff = sizeDiff;
        }
    }

    @Test
    public void testToTree_randomSpheres() {
        // arrange
        final UniformRandomProvider rand = RandomSource.create(RandomSource.XO_RO_SHI_RO_128_PP, 1L);
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-10);
        final double min = 1e-1;
        final double max = 1e2;

        final DoubleSupplier randDouble = () -> (rand.nextDouble() * (max - min)) + min;

        final int count = 10;
        for (int i = 0; i < count; ++i) {
            final Vector3D center = Vector3D.of(
                    randDouble.getAsDouble(),
                    randDouble.getAsDouble(),
                    randDouble.getAsDouble());

            final double radius = randDouble.getAsDouble();
            final Sphere sphere = Sphere.from(center, radius, precision);

            for (int s = 0; s < 7; ++s) {
                // act
                final RegionBSPTree3D tree = sphere.toTree(s);

                // assert
                Assertions.assertEquals((int) (8 * Math.pow(4, s)), tree.getBoundaries().size());
                Assertions.assertTrue(tree.isFinite());
                Assertions.assertFalse(tree.isEmpty());
                Assertions.assertTrue(tree.getSize() < sphere.getSize());
            }
        }
    }

    @Test
    public void testToTree_closeApproximation() throws IOException {
        // arrange
        final Sphere s = Sphere.from(Vector3D.ZERO, 1, TEST_PRECISION);

        // act
        final RegionBSPTree3D tree = s.toTree(8);

        // assert
        checkBasicApproximationProperties(s, tree);

        final double eps = 1e-3;
        Assertions.assertTrue(tree.isFinite());
        Assertions.assertEquals(s.getSize(), tree.getSize(), eps);
        Assertions.assertEquals(s.getBoundarySize(), tree.getBoundarySize(), eps);
        EuclideanTestUtils.assertCoordinatesEqual(s.getCentroid(), tree.getCentroid(), eps);
    }

    @Test
    public void testToTree_subdivideFails() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-5);
        final Sphere s = Sphere.from(Vector3D.ZERO, 1, precision);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            s.toTree(6);
        }, IllegalStateException.class,
                Pattern.compile("^Failed to construct sphere approximation with subdivision count 6:.*"));
    }

    @Test
    public void testToTree_invalidArgs() {
        // arrange
        final Sphere s = Sphere.from(Vector3D.of(2, 1, 3), 2, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            s.toTree(-1);
        }, IllegalArgumentException.class,
                "Number of sphere approximation subdivisions must be greater than or equal to zero; was -1");
    }

    @Test
    public void testToMesh_zeroSubdivisions() {
        // arrange
        final Sphere s = Sphere.from(Vector3D.of(1, 2, 3), 2, TEST_PRECISION);

        // act
        final TriangleMesh mesh = s.toTriangleMesh(0);

        // assert
        Assertions.assertEquals(6, mesh.getVertexCount());
        Assertions.assertEquals(8, mesh.getFaceCount());

        final Bounds3D bounds = mesh.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 4, 5), bounds.getMax(), TEST_EPS);

        Assertions.assertTrue(mesh.toTree().isFinite());
    }

    @Test
    public void testToMesh_manySubdivisions() {
        // arrange
        final Sphere s = Sphere.from(Vector3D.of(1, 2, 3), 2, TEST_PRECISION);
        final int subdivisions = 5;

        // act
        final TriangleMesh mesh = s.toTriangleMesh(subdivisions);

        // assert
        Assertions.assertEquals((int) (8 * Math.pow(4, subdivisions)), mesh.getFaceCount());

        final Bounds3D bounds = mesh.getBounds();
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(-1, 0, 1), bounds.getMin(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(3, 4, 5), bounds.getMax(), TEST_EPS);

        final RegionBSPTree3D tree = RegionBSPTree3D.partitionedRegionBuilder()
                .insertAxisAlignedGrid(bounds, 3, TEST_PRECISION)
                .insertBoundaries(mesh)
                .build();

        Assertions.assertTrue(tree.isFinite());

        final double approximationEps = 0.1;
        Assertions.assertEquals(s.getSize(), tree.getSize(), approximationEps);
        Assertions.assertEquals(s.getBoundarySize(), tree.getBoundarySize(), approximationEps);

        EuclideanTestUtils.assertCoordinatesEqual(s.getCentroid(), tree.getCentroid(), TEST_EPS);
    }

    @Test
    public void testToMesh_invalidArgs() {
        // arrange
        final Sphere s = Sphere.from(Vector3D.of(2, 1, 3), 2, TEST_PRECISION);

        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            s.toTriangleMesh(-1);
        }, IllegalArgumentException.class,
                "Number of sphere approximation subdivisions must be greater than or equal to zero; was -1");
    }

    @Test
    public void testHashCode() {
        // arrange
        final Precision.DoubleEquivalence otherPrecision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final Sphere a = Sphere.from(Vector3D.of(1, 2, 3), 3, TEST_PRECISION);
        final Sphere b = Sphere.from(Vector3D.of(1, 1, 3), 3, TEST_PRECISION);
        final Sphere c = Sphere.from(Vector3D.of(1, 2, 3), 4, TEST_PRECISION);
        final Sphere d = Sphere.from(Vector3D.of(1, 2, 3), 3, otherPrecision);
        final Sphere e = Sphere.from(Vector3D.of(1, 2, 3), 3, TEST_PRECISION);

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
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final Sphere a = Sphere.from(Vector3D.of(1, 2, 3), 3, TEST_PRECISION);
        final Sphere b = Sphere.from(Vector3D.of(1, 1, 3), 3, TEST_PRECISION);
        final Sphere c = Sphere.from(Vector3D.of(1, 2, 3), 4, TEST_PRECISION);
        final Sphere d = Sphere.from(Vector3D.of(1, 2, 3), 3, precision);
        final Sphere e = Sphere.from(Vector3D.of(1, 2, 3), 3, TEST_PRECISION);

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
        final Sphere c = Sphere.from(Vector3D.of(1, 2, 3), 3, TEST_PRECISION);

        // act
        final String str = c.toString();

        // assert
        Assertions.assertEquals("Sphere[center= (1.0, 2.0, 3.0), radius= 3.0]", str);
    }

    private static void checkContains(final Sphere sphere, final boolean contains, final Vector3D... pts) {
        for (final Vector3D pt : pts) {
            Assertions.assertEquals(contains, sphere.contains(pt),
                    "Expected circle to " + (contains ? "" : "not") + "contain point " + pt);
        }
    }

    private static void checkIntersections(final Sphere sphere, final Line3D line, final Vector3D... expectedPts) {
        // --- act
        // compute the intersections forward and reverse
        final List<Vector3D> actualPtsForward = sphere.intersections(line);
        final List<Vector3D> actualPtsReverse = sphere.intersections(line.reverse());

        final Vector3D actualFirstForward = sphere.firstIntersection(line);
        final Vector3D actualFirstReverse = sphere.firstIntersection(line.reverse());

        // --- assert
        final int len = expectedPts.length;

        // check the lists
        Assertions.assertEquals(len, actualPtsForward.size());
        Assertions.assertEquals(len, actualPtsReverse.size());

        for (int i = 0; i < len; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[i], actualPtsForward.get(i), TEST_EPS);
            Assertions.assertEquals(sphere.getRadius(), sphere.getCenter().distance(actualPtsForward.get(i)), TEST_EPS);

            EuclideanTestUtils.assertCoordinatesEqual(expectedPts[len - i - 1], actualPtsReverse.get(i), TEST_EPS);
            Assertions.assertEquals(sphere.getRadius(), sphere.getCenter().distance(actualPtsReverse.get(i)), TEST_EPS);
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

    private static void checkLinecast(final Sphere s, final LineConvexSubset3D segment, final Vector3D... expectedPts) {
        // check linecast
        final List<LinecastPoint3D> results = s.linecast(segment);
        Assertions.assertEquals(expectedPts.length, results.size());

        LinecastPoint3D actual;
        Vector3D expected;
        for (int i = 0; i < expectedPts.length; ++i) {
            expected = expectedPts[i];
            actual = results.get(i);

            EuclideanTestUtils.assertCoordinatesEqual(expected, actual.getPoint(), TEST_EPS);
            EuclideanTestUtils.assertCoordinatesEqual(s.getCenter().directionTo(expected), actual.getNormal(), TEST_EPS);
            Assertions.assertSame(segment.getLine(), actual.getLine());
        }

        // check linecastFirst
        final LinecastPoint3D firstResult = s.linecastFirst(segment);
        if (expectedPts.length > 0) {
            Assertions.assertEquals(results.get(0), firstResult);
        } else {
            Assertions.assertNull(firstResult);
        }
    }

    /**
     * Check a number of standard properties for bsp trees generated as sphere approximations.
     */
    private static void checkBasicApproximationProperties(final Sphere s, final RegionBSPTree3D tree) {
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());
        Assertions.assertTrue(tree.isFinite());
        Assertions.assertFalse(tree.isInfinite());

        // volume must be less than the sphere
        Assertions.assertTrue(tree.getSize() < s.getSize(), "Expected approximation volume to be less than circle");

        // all vertices must be inside the sphere or on the boundary
        for (final PlaneConvexSubset boundary : tree.getBoundaries()) {
            Assertions.assertTrue(boundary.isFinite());

            for (final Vector3D vertex : boundary.getVertices()) {
                Assertions.assertTrue(s.contains(vertex), "Expected vertex to be contained in sphere: " + vertex);
            }
        }

        // sphere must contain centroid
        EuclideanTestUtils.assertRegionLocation(s, RegionLocation.INSIDE, tree.getCentroid());
    }
}
