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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.AffineTransformMatrix3D;
import org.apache.commons.geometry.euclidean.threed.PlaneConvexSubset;
import org.apache.commons.geometry.euclidean.threed.RegionBSPTree3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class ParallelepipedTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Comparator<Vector3D> VERTEX_COMPARATOR = (a, b) -> {
        int cmp = TEST_PRECISION.compare(a.getX(), b.getX());
        if (cmp == 0) {
            cmp = TEST_PRECISION.compare(a.getY(), b.getY());
            if (cmp == 0) {
                cmp = TEST_PRECISION.compare(a.getZ(), b.getZ());
            }
        }
        return cmp;
    };

    @Test
    public void testUnitCube() {
        // act
        Parallelepiped p = Parallelepiped.unitCube(TEST_PRECISION);

        // assert
        Assert.assertEquals(1, p.getSize(), TEST_EPS);
        Assert.assertEquals(6, p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, p.getCentroid(), TEST_EPS);

        List<PlaneConvexSubset> boundaries = p.getBoundaries();
        Assert.assertEquals(6, boundaries.size());

        assertVertices(p,
            Vector3D.of(-0.5, -0.5, -0.5),
            Vector3D.of(0.5, -0.5, -0.5),
            Vector3D.of(0.5, 0.5, -0.5),
            Vector3D.of(-0.5, 0.5, -0.5),

            Vector3D.of(-0.5, -0.5, 0.5),
            Vector3D.of(0.5, -0.5, 0.5),
            Vector3D.of(0.5, 0.5, 0.5),
            Vector3D.of(-0.5, 0.5, 0.5)
        );
    }

    @Test
    public void testFromTransformedUnitCube() {
        // arrange
        AffineTransformMatrix3D t = AffineTransformMatrix3D.createTranslation(Vector3D.of(1, 0, 2))
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, Math.PI * 0.25))
                .scale(Vector3D.of(2, 1, 1));

        // act
        Parallelepiped p = Parallelepiped.fromTransformedUnitCube(t, TEST_PRECISION);

        // assert
        double sqrt2 = Math.sqrt(2);
        double invSqrt2 = 1 / sqrt2;

        Assert.assertEquals(2, p.getSize(), TEST_EPS);
        Assert.assertEquals(4 + (4 * Math.sqrt(2.5)), p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2 * invSqrt2, invSqrt2, 2),
                p.getCentroid(), TEST_EPS);

        assertVertices(p,
            Vector3D.of(0, invSqrt2, 1.5),
            Vector3D.of(2 * invSqrt2, 0, 1.5),
            Vector3D.of(2 * sqrt2, invSqrt2, 1.5),
            Vector3D.of(2 * invSqrt2, sqrt2, 1.5),

            Vector3D.of(0, invSqrt2, 2.5),
            Vector3D.of(2 * invSqrt2, 0, 2.5),
            Vector3D.of(2 * sqrt2, invSqrt2, 2.5),
            Vector3D.of(2 * invSqrt2, sqrt2, 2.5)
        );
    }

    @Test
    public void testFromTransformedUnitCube_transformDoesNotPreserveOrientation() {
        // arrange
        AffineTransformMatrix3D t = AffineTransformMatrix3D.createTranslation(Vector3D.of(1, 0, 2))
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, Math.PI * 0.25))
                .scale(Vector3D.of(2, 1, -1));

        // act
        Parallelepiped p = Parallelepiped.fromTransformedUnitCube(t, TEST_PRECISION);

        // assert
        double sqrt2 = Math.sqrt(2);
        double invSqrt2 = 1 / sqrt2;

        Assert.assertEquals(2, p.getSize(), TEST_EPS);
        Assert.assertEquals(4 + (4 * Math.sqrt(2.5)), p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2 * invSqrt2, invSqrt2, -2),
                p.getCentroid(), TEST_EPS);

        assertVertices(p,
            Vector3D.of(0, invSqrt2, -1.5),
            Vector3D.of(2 * invSqrt2, 0, -1.5),
            Vector3D.of(2 * sqrt2, invSqrt2, -1.5),
            Vector3D.of(2 * invSqrt2, sqrt2, -1.5),

            Vector3D.of(0, invSqrt2, -2.5),
            Vector3D.of(2 * invSqrt2, 0, -2.5),
            Vector3D.of(2 * sqrt2, invSqrt2, -2.5),
            Vector3D.of(2 * invSqrt2, sqrt2, -2.5)
        );
    }

    @Test
    public void testFromTransformedUnitCube_zeroSizeRegion() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Parallelepiped.fromTransformedUnitCube(AffineTransformMatrix3D.createScale(Vector3D.of(1e-16, 1, 1)),
                    TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Parallelepiped.fromTransformedUnitCube(AffineTransformMatrix3D.createScale(Vector3D.of(1, 1e-16, 1)),
                    TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Parallelepiped.fromTransformedUnitCube(AffineTransformMatrix3D.createScale(Vector3D.of(1, 1, 1e-16)),
                    TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testAxisAligned_minFirst() {
        // act
        Parallelepiped p = Parallelepiped.axisAligned(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), TEST_PRECISION);

        // assert
        List<PlaneConvexSubset> boundaries = p.getBoundaries();
        Assert.assertEquals(6, boundaries.size());

        assertVertices(p,
            Vector3D.of(1, 2, 3),
            Vector3D.of(4, 2, 3),
            Vector3D.of(4, 5, 3),
            Vector3D.of(1, 5, 3),

            Vector3D.of(1, 2, 6),
            Vector3D.of(4, 2, 6),
            Vector3D.of(4, 5, 6),
            Vector3D.of(1, 5, 6)
        );
    }

    @Test
    public void testAxisAligned_maxFirst() {
        // act
        Parallelepiped p = Parallelepiped.axisAligned(Vector3D.of(4, 5, 6), Vector3D.of(1, 2, 3), TEST_PRECISION);

        // assert
        List<PlaneConvexSubset> boundaries = p.getBoundaries();
        Assert.assertEquals(6, boundaries.size());

        assertVertices(p,
            Vector3D.of(1, 2, 3),
            Vector3D.of(4, 2, 3),
            Vector3D.of(4, 5, 3),
            Vector3D.of(1, 5, 3),

            Vector3D.of(1, 2, 6),
            Vector3D.of(4, 2, 6),
            Vector3D.of(4, 5, 6),
            Vector3D.of(1, 5, 6)
        );
    }

    @Test
    public void testAxisAligned_illegalArgs() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Parallelepiped.axisAligned(Vector3D.of(1, 2, 3), Vector3D.of(1, 5, 6), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Parallelepiped.axisAligned(Vector3D.of(1, 2, 3), Vector3D.of(4, 2, 6), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Parallelepiped.axisAligned(Vector3D.of(1, 2, 3), Vector3D.of(1, 5, 3), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_defaultValues() {
        // arrange
        Parallelepiped.Builder builder = Parallelepiped.builder(TEST_PRECISION);

        // act
        Parallelepiped p = builder.build();

        // assert
        Assert.assertEquals(1, p.getSize(), TEST_EPS);
        Assert.assertEquals(6, p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, p.getCentroid(), TEST_EPS);

        List<PlaneConvexSubset> boundaries = p.getBoundaries();
        Assert.assertEquals(6, boundaries.size());

        assertVertices(p,
            Vector3D.of(-0.5, -0.5, -0.5),
            Vector3D.of(0.5, -0.5, -0.5),
            Vector3D.of(0.5, 0.5, -0.5),
            Vector3D.of(-0.5, 0.5, -0.5),

            Vector3D.of(-0.5, -0.5, 0.5),
            Vector3D.of(0.5, -0.5, 0.5),
            Vector3D.of(0.5, 0.5, 0.5),
            Vector3D.of(-0.5, 0.5, 0.5)
        );
    }

    @Test
    public void testBuilder_withRotation() {
        // arrange
        Parallelepiped.Builder builder = Parallelepiped.builder(TEST_PRECISION);

        // act
        Parallelepiped p = builder
                .setScale(1, 2, 3)
                .setRotation(QuaternionRotation.fromAxisAngle(Vector3D.Unit.PLUS_Z, PlaneAngleRadians.PI_OVER_TWO))
                .setPosition(Vector3D.of(1, 2, -1))
                .build();

        // assert
        Assert.assertEquals(6, p.getSize(), TEST_EPS);
        Assert.assertEquals(22, p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 2, -1), p.getCentroid(), TEST_EPS);

        assertVertices(p,
            Vector3D.of(0, 1.5, 0.5),
            Vector3D.of(2, 1.5, 0.5),
            Vector3D.of(2, 2.5, 0.5),
            Vector3D.of(0, 2.5, 0.5),

            Vector3D.of(0, 1.5, -2.5),
            Vector3D.of(2, 1.5, -2.5),
            Vector3D.of(2, 2.5, -2.5),
            Vector3D.of(0, 2.5, -2.5)
        );
    }

    @Test
    public void testBuilder_withUniformScale() {
        // arrange
        Parallelepiped.Builder builder = Parallelepiped.builder(TEST_PRECISION);

        // act
        Parallelepiped p = builder
                .setScale(0.5)
                .build();

        // assert
        Assert.assertEquals(0.125, p.getSize(), TEST_EPS);
        Assert.assertEquals(1.5, p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.ZERO, p.getCentroid(), TEST_EPS);

        assertVertices(p,
            Vector3D.of(-0.25, -0.25, -0.25),
            Vector3D.of(0.25, -0.25, -0.25),
            Vector3D.of(0.25, 0.25, -0.25),
            Vector3D.of(-0.25, 0.25, -0.25),

            Vector3D.of(-0.25, -0.25, 0.25),
            Vector3D.of(0.25, -0.25, 0.25),
            Vector3D.of(0.25, 0.25, 0.25),
            Vector3D.of(-0.25, 0.25, 0.25)
        );
    }

    @Test
    public void testToTree() {
        // arrange
        Parallelepiped p = Parallelepiped.axisAligned(Vector3D.of(1, 2, 3), Vector3D.of(4, 5, 6), TEST_PRECISION);

        // act
        RegionBSPTree3D tree = p.toTree();

        // assert
        Assert.assertEquals(27, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(2.5, 3.5, 4.5), tree.getCentroid(), TEST_EPS);
    }

    private static void assertVertices(Parallelepiped p, Vector3D... vertices) {
        Set<Vector3D> expectedVertices = new TreeSet<>(VERTEX_COMPARATOR);
        expectedVertices.addAll(Arrays.asList(vertices));

        Set<Vector3D> actualVertices = new TreeSet<>(VERTEX_COMPARATOR);
        for (PlaneConvexSubset boundary : p.getBoundaries()) {
            actualVertices.addAll(boundary.getVertices());
        }

        Assert.assertEquals(expectedVertices.size(), actualVertices.size());
        for (Vector3D expected : expectedVertices) {
            Assert.assertTrue("Expected vertices to contain " + expected, actualVertices.contains(expected));
        }
    }
}
