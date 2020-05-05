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

import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.AffineTransformMatrix2D;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.RegionBSPTree2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.apache.commons.geometry.euclidean.twod.rotation.Rotation2D;
import org.junit.Assert;
import org.junit.Test;

public class ParallelogramTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testUnitSquare() {
        // act
        Parallelogram box = Parallelogram.unitSquare(TEST_PRECISION);

        // assert
        Assert.assertEquals(1, box.getSize(), TEST_EPS);
        Assert.assertEquals(4, box.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, box.getBarycenter(), TEST_EPS);

        List<Vector2D> vertices = box.getVertices();
        Assert.assertEquals(4, vertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-0.5, -0.5), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, -0.5), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-0.5, 0.5), vertices.get(3), TEST_EPS);
    }

    @Test
    public void testFromTransformedUnitSquare() {
        // arrange
        AffineTransformMatrix2D t = AffineTransformMatrix2D.createTranslation(Vector2D.of(1, 0))
                .rotate(Math.PI * 0.25)
                .scale(Vector2D.of(2, 1));

        // act
        Parallelogram p = Parallelogram.fromTransformedUnitSquare(t, TEST_PRECISION);

        // assert
        double sqrt2 = Math.sqrt(2);
        double invSqrt2 = 1 / sqrt2;

        Assert.assertEquals(2, p.getSize(), TEST_EPS);
        Assert.assertEquals(4 * Math.sqrt(2.5), p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2 * invSqrt2, invSqrt2), p.getBarycenter(), TEST_EPS);

        List<Vector2D> vertices = p.getVertices();
        Assert.assertEquals(4, vertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, invSqrt2), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2 * invSqrt2, 0), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2 * sqrt2, invSqrt2), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2 * invSqrt2, sqrt2), vertices.get(3), TEST_EPS);
    }

    @Test
    public void testFromTransformedUnitSquare_transformDoesNotPreserveOrientation() {
        // arrange
        AffineTransformMatrix2D t = AffineTransformMatrix2D.createTranslation(Vector2D.of(1, 0))
                .rotate(Math.PI * 0.25)
                .scale(Vector2D.of(-2, 1));

        // act
        Parallelogram p = Parallelogram.fromTransformedUnitSquare(t, TEST_PRECISION);

        // assert
        double sqrt2 = Math.sqrt(2);
        double invSqrt2 = 1 / sqrt2;

        Assert.assertEquals(2, p.getSize(), TEST_EPS);
        Assert.assertEquals(4 * Math.sqrt(2.5), p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2 * invSqrt2, invSqrt2), p.getBarycenter(), TEST_EPS);

        List<Vector2D> vertices = p.getVertices();
        Assert.assertEquals(4, vertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2 * sqrt2, invSqrt2), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2 * invSqrt2, 0), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, invSqrt2), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-2 * invSqrt2, sqrt2), vertices.get(3), TEST_EPS);
    }

    @Test
    public void testFromTransformedUnitSquare_zeroSizeRegion() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Parallelogram.fromTransformedUnitSquare(AffineTransformMatrix2D.createScale(Vector2D.of(1e-16, 1)),
                    TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Parallelogram.fromTransformedUnitSquare(AffineTransformMatrix2D.createScale(Vector2D.of(1, 1e-16)),
                    TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testAxisAligned_minFirst() {
        // act
        Parallelogram box = Parallelogram.axisAligned(Vector2D.of(1, 2), Vector2D.of(3, 4), TEST_PRECISION);

        // assert
        Assert.assertEquals(1, box.getBoundaryPaths().size());
        LinePath path = box.getBoundaryPaths().get(0);

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(4, segments.size());

        assertSegment(segments.get(0), Vector2D.of(1, 2), Vector2D.of(3, 2));
        assertSegment(segments.get(1), Vector2D.of(3, 2), Vector2D.of(3, 4));
        assertSegment(segments.get(2), Vector2D.of(3, 4), Vector2D.of(1, 4));
        assertSegment(segments.get(3), Vector2D.of(1, 4), Vector2D.of(1, 2));
    }

    @Test
    public void testAxisAligned_maxFirst() {
        // act
        Parallelogram box = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(-1, -2), TEST_PRECISION);

        // assert
        Assert.assertEquals(1, box.getBoundaryPaths().size());
        LinePath path = box.getBoundaryPaths().get(0);

        List<LineConvexSubset> segments = path.getElements();
        Assert.assertEquals(4, segments.size());

        assertSegment(segments.get(0), Vector2D.of(-1, -2), Vector2D.of(0, -2));
        assertSegment(segments.get(1), Vector2D.of(0, -2), Vector2D.ZERO);
        assertSegment(segments.get(2), Vector2D.ZERO, Vector2D.of(-1, 0));
        assertSegment(segments.get(3), Vector2D.of(-1, 0), Vector2D.of(-1, -2));
    }

    @Test
    public void testAxisAligned_illegalArgs() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            Parallelogram.axisAligned(Vector2D.of(1, 1), Vector2D.of(1, 3), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Parallelogram.axisAligned(Vector2D.of(1, 1), Vector2D.of(3, 1), TEST_PRECISION);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            Parallelogram.axisAligned(Vector2D.of(2, 3), Vector2D.of(2, 3), TEST_PRECISION);
        }, IllegalArgumentException.class);
    }

    @Test
    public void testBuilder_defaultValues() {
        // arrange
        Parallelogram.Builder builder = Parallelogram.builder(TEST_PRECISION);

        // act
        Parallelogram p = builder.build();

        // assert
        Assert.assertEquals(1, p.getSize(), TEST_EPS);
        Assert.assertEquals(4, p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, p.getBarycenter(), TEST_EPS);

        List<Vector2D> vertices = p.getVertices();
        Assert.assertEquals(4, vertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-0.5, -0.5), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, -0.5), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 0.5), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(-0.5, 0.5), vertices.get(3), TEST_EPS);
    }

    @Test
    public void testBuilder_rotatedRect_withXDirection() {
        // arrange
        Parallelogram.Builder builder = Parallelogram.builder(TEST_PRECISION);

        // act
        Parallelogram p = builder
                .setScale(1, 2)
                .setXDirection(Vector2D.Unit.PLUS_Y)
                .setPosition(Vector2D.of(1, 2))
                .build();

        // assert
        Assert.assertEquals(2, p.getSize(), TEST_EPS);
        Assert.assertEquals(6, p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), p.getBarycenter(), TEST_EPS);

        List<Vector2D> vertices = p.getVertices();
        Assert.assertEquals(4, vertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 1.5), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 1.5), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(2, 2.5), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 2.5), vertices.get(3), TEST_EPS);
    }

    @Test
    public void testBuilder_rotatedRect_withYDirection() {
        // arrange
        Parallelogram.Builder builder = Parallelogram.builder(TEST_PRECISION);

        // act
        Parallelogram p = builder
                .setScale(Vector2D.of(2, 1))
                .setYDirection(Vector2D.Unit.MINUS_X)
                .setPosition(Vector2D.of(1, 2))
                .build();

        // assert
        Assert.assertEquals(2, p.getSize(), TEST_EPS);
        Assert.assertEquals(6, p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), p.getBarycenter(), TEST_EPS);

        List<Vector2D> vertices = p.getVertices();
        Assert.assertEquals(4, vertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 1), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 1), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1.5, 3), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 3), vertices.get(3), TEST_EPS);
    }

    @Test
    public void testBuilder_rotatedRect_withRotation() {
        // arrange
        Parallelogram.Builder builder = Parallelogram.builder(TEST_PRECISION);

        // act
        Parallelogram p = builder
                .setScale(2)
                .setRotation(Rotation2D.of(0.25 * Math.PI))
                .setPosition(Vector2D.of(1, 2))
                .build();

        // assert
        Assert.assertEquals(4, p.getSize(), TEST_EPS);
        Assert.assertEquals(8, p.getBoundarySize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2), p.getBarycenter(), TEST_EPS);

        List<Vector2D> vertices = p.getVertices();
        Assert.assertEquals(4, vertices.size());

        double sqrt2 = Math.sqrt(2);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1 - sqrt2, 2), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2 - sqrt2), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1 + sqrt2, 2), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 2 + sqrt2), vertices.get(3), TEST_EPS);
    }

    @Test
    public void testToTree() {
        // act
        RegionBSPTree2D tree = Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(1, 4), TEST_PRECISION)
                .toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(4, tree.getSize(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0.5, 2), tree.getBarycenter(), TEST_EPS);
    }

    private static void assertSegment(LineConvexSubset segment, Vector2D start, Vector2D end) {
        EuclideanTestUtils.assertCoordinatesEqual(start, segment.getStartPoint(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(end, segment.getEndPoint(), TEST_EPS);
    }
}
