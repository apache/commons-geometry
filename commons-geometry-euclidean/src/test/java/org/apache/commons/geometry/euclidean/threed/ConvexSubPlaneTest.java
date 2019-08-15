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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.threed.rotation.QuaternionRotation;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Line;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.junit.Assert;
import org.junit.Test;

public class ConvexSubPlaneTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromConvexArea() {
        // arrange
        Plane plane = Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1),
                Vector3D.PLUS_X, Vector3D.PLUS_Y, TEST_PRECISION);
        ConvexArea area = ConvexArea.fromVertexLoop(Arrays.asList(
                    Vector2D.of(1, 0),
                    Vector2D.of(3, 0),
                    Vector2D.of(3, 1),
                    Vector2D.of(1, 1)
                ), TEST_PRECISION);

        // act
        ConvexSubPlane sp = ConvexSubPlane.fromConvexArea(plane, area);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertFalse(sp.isEmpty());
        Assert.assertTrue(sp.isFinite());

        Assert.assertEquals(2, sp.getSize(), TEST_EPS);

        Assert.assertSame(plane, sp.getPlane());
        Assert.assertSame(plane, sp.getHyperplane());
        Assert.assertSame(area, sp.getSubspaceRegion());
    }

    @Test
    public void testFromVertices_infinite() {
        // act
        ConvexSubPlane sp = ConvexSubPlane.fromVertices(Arrays.asList(
                    Vector3D.of(1, 0, 0),
                    Vector3D.of(1, 1, 0),
                    Vector3D.of(1, 1, 1)
                ), TEST_PRECISION);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertFalse(sp.isEmpty());
        Assert.assertFalse(sp.isFinite());

        EuclideanTestUtils.assertPositiveInfinity(sp.getSize());

        checkPlane(sp.getPlane(), Vector3D.of(1, 0, 0), Vector3D.PLUS_Y, Vector3D.PLUS_Z);

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(0, 1, 1), Vector3D.of(0, 1, 0), Vector3D.of(0, 1, -1),
                Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 0), Vector3D.of(0, 0, -1),
                Vector3D.of(0, -1, 1), Vector3D.of(0, -1, 0), Vector3D.of(0, -1, -1));

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(1, 1, -1), Vector3D.of(1, 0, -1), Vector3D.of(1, -1, -1));

        checkPoints(sp, RegionLocation.BOUNDARY,
                Vector3D.of(1, 1, 1), Vector3D.of(1, 1, 0),
                Vector3D.of(1, 0, 0),
                Vector3D.of(1, -1, 0));

        checkPoints(sp, RegionLocation.INSIDE,
                Vector3D.of(1, 0, 1), Vector3D.of(1, -1, 1));

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(2, 1, 1), Vector3D.of(2, 1, 0), Vector3D.of(2, 1, -1),
                Vector3D.of(2, 0, 1), Vector3D.of(2, 0, 0), Vector3D.of(2, 0, -1),
                Vector3D.of(2, -1, 1), Vector3D.of(2, -1, 0), Vector3D.of(2, -1, -1));
    }

    @Test
    public void testFromVertices_finite() {
        // act
        ConvexSubPlane sp = ConvexSubPlane.fromVertices(Arrays.asList(
                    Vector3D.of(1, 0, 0),
                    Vector3D.of(1, 1, 0),
                    Vector3D.of(1, 1, 2),
                    Vector3D.of(1, 0, 0)
                ), TEST_PRECISION);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertFalse(sp.isEmpty());
        Assert.assertTrue(sp.isFinite());

        Assert.assertEquals(1, sp.getSize(), TEST_EPS);

        checkPlane(sp.getPlane(), Vector3D.of(1, 0, 0), Vector3D.PLUS_Y, Vector3D.PLUS_Z);

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(0, 1, 1), Vector3D.of(0, 1, 0), Vector3D.of(0, 1, -1),
                Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 0), Vector3D.of(0, 0, -1),
                Vector3D.of(0, -1, 1), Vector3D.of(0, -1, 0), Vector3D.of(0, -1, -1));

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(1, 1, -1),
                Vector3D.of(1, 0, 1), Vector3D.of(1, 0, -1),
                Vector3D.of(1, -1, 1), Vector3D.of(1, -1, 0), Vector3D.of(1, -1, -1));

        checkPoints(sp, RegionLocation.BOUNDARY,
                Vector3D.of(1, 1, 1), Vector3D.of(1, 1, 0),
                Vector3D.of(1, 0, 0));

        checkPoints(sp, RegionLocation.INSIDE, Vector3D.of(1, 0.5, 0.5));

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(2, 1, 1), Vector3D.of(2, 1, 0), Vector3D.of(2, 1, -1),
                Vector3D.of(2, 0, 1), Vector3D.of(2, 0, 0), Vector3D.of(2, 0, -1),
                Vector3D.of(2, -1, 1), Vector3D.of(2, -1, 0), Vector3D.of(2, -1, -1));
    }

    @Test
    public void testFromVertexLoop() {
        // act
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(Arrays.asList(
                    Vector3D.of(1, 0, 0),
                    Vector3D.of(1, 1, 0),
                    Vector3D.of(1, 1, 2)
                ), TEST_PRECISION);

        // assert
        Assert.assertFalse(sp.isFull());
        Assert.assertFalse(sp.isEmpty());
        Assert.assertTrue(sp.isFinite());

        Assert.assertEquals(1, sp.getSize(), TEST_EPS);

        checkPlane(sp.getPlane(), Vector3D.of(1, 0, 0), Vector3D.PLUS_Y, Vector3D.PLUS_Z);

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(0, 1, 1), Vector3D.of(0, 1, 0), Vector3D.of(0, 1, -1),
                Vector3D.of(0, 0, 1), Vector3D.of(0, 0, 0), Vector3D.of(0, 0, -1),
                Vector3D.of(0, -1, 1), Vector3D.of(0, -1, 0), Vector3D.of(0, -1, -1));

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(1, 1, -1),
                Vector3D.of(1, 0, 1), Vector3D.of(1, 0, -1),
                Vector3D.of(1, -1, 1), Vector3D.of(1, -1, 0), Vector3D.of(1, -1, -1));

        checkPoints(sp, RegionLocation.BOUNDARY,
                Vector3D.of(1, 1, 1), Vector3D.of(1, 1, 0),
                Vector3D.of(1, 0, 0));

        checkPoints(sp, RegionLocation.INSIDE, Vector3D.of(1, 0.5, 0.5));

        checkPoints(sp, RegionLocation.OUTSIDE,
                Vector3D.of(2, 1, 1), Vector3D.of(2, 1, 0), Vector3D.of(2, 1, -1),
                Vector3D.of(2, 0, 1), Vector3D.of(2, 0, 0), Vector3D.of(2, 0, -1),
                Vector3D.of(2, -1, 1), Vector3D.of(2, -1, 0), Vector3D.of(2, -1, -1));
    }

    @Test
    public void testToConvex() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(
                Arrays.asList(Vector3D.PLUS_X,  Vector3D.PLUS_Y, Vector3D.PLUS_Z), TEST_PRECISION);

        // act
        List<ConvexSubPlane> convex = sp.toConvex();

        // assert
        Assert.assertEquals(1, convex.size());
        Assert.assertSame(sp, convex.get(0));
    }

    @Test
    public void testGetVertices_full() {
        // arrange
        Plane plane = Plane.fromNormal(Vector3D.PLUS_Z, TEST_PRECISION);
        ConvexSubPlane sp = ConvexSubPlane.fromConvexArea(plane, ConvexArea.full());

        // act
        List<Vector3D> vertices = sp.getVertices();

        // assert
        Assert.assertEquals(0, vertices.size());
    }

    @Test
    public void testGetVertices_twoParallelLines() {
        // arrange
        Plane plane = Plane.fromNormal(Vector3D.PLUS_Z, TEST_PRECISION);
        ConvexSubPlane sp = ConvexSubPlane.fromConvexArea(plane, ConvexArea.fromBounds(
                    Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.PI, TEST_PRECISION),
                    Line.fromPointAndAngle(Vector2D.of(0, -1), Geometry.ZERO_PI, TEST_PRECISION)
                ));

        // act
        List<Vector3D> vertices = sp.getVertices();

        // assert
        Assert.assertEquals(0, vertices.size());
    }

    @Test
    public void testGetVertices_infiniteWithVertices() {
        // arrange
        Plane plane = Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.PLUS_X, Vector3D.PLUS_Y, TEST_PRECISION);
        ConvexSubPlane sp = ConvexSubPlane.fromConvexArea(plane, ConvexArea.fromBounds(
                    Line.fromPointAndAngle(Vector2D.of(0, 1), Geometry.PI, TEST_PRECISION),
                    Line.fromPointAndAngle(Vector2D.of(0, -1), Geometry.ZERO_PI, TEST_PRECISION),
                    Line.fromPointAndAngle(Vector2D.of(1, 0), Geometry.HALF_PI, TEST_PRECISION)
                ));

        // act
        List<Vector3D> vertices = sp.getVertices();

        // assert
        Assert.assertEquals(2, vertices.size());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, -1, 1), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 1, 1), vertices.get(1), TEST_EPS);
    }

    @Test
    public void testGetVertices_finite() {
        // arrange
        Plane plane = Plane.fromPointAndPlaneVectors(Vector3D.of(0, 0, 1), Vector3D.PLUS_X, Vector3D.PLUS_Y, TEST_PRECISION);
        ConvexSubPlane sp = ConvexSubPlane.fromConvexArea(plane, ConvexArea.fromVertexLoop(Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.PLUS_X,
                    Vector2D.PLUS_Y
                ), TEST_PRECISION));

        // act
        List<Vector3D> vertices = sp.getVertices();

        // assert
        Assert.assertEquals(4, vertices.size());

        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(1, 0, 1), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 1, 1), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.of(0, 0, 1), vertices.get(3), TEST_EPS);
    }

    @Test
    public void testReverse() {
        // arrange
        Vector3D p1 = Vector3D.of(1, 0, 1);
        Vector3D p2 = Vector3D.of(2, 0, 1);
        Vector3D p3 = Vector3D.of(1, 1, 1);

        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(Arrays.asList(p1, p2, p3), TEST_PRECISION);

        // act
        ConvexSubPlane reversed = sp.reverse();

        // assert
        Assert.assertEquals(sp.getPlane().reverse(), reversed.getPlane());
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.MINUS_Z, reversed.getPlane().getNormal(), TEST_EPS);

        Assert.assertEquals(0.5, reversed.getSize(), TEST_EPS);

        checkVertices(reversed, p1, p3, p2, p1);

        checkPoints(reversed, RegionLocation.INSIDE, Vector3D.of(1.25, 0.25, 1));

        checkPoints(reversed, RegionLocation.BOUNDARY, p1, p2, p3);
    }

    @Test
    public void testTransform_full() {
        // arrange
        Plane plane = Plane.fromPointAndPlaneVectors(Vector3D.PLUS_Z, Vector3D.PLUS_X, Vector3D.PLUS_Y, TEST_PRECISION);
        ConvexSubPlane sp = ConvexSubPlane.fromConvexArea(plane, ConvexArea.full());

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.identity()
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.PLUS_X, Geometry.HALF_PI))
                .translate(Vector3D.PLUS_Y);

        // act
        ConvexSubPlane transformed = sp.transform(transform);

        // assert
        Assert.assertTrue(transformed.isFull());
        Assert.assertFalse(transformed.isEmpty());

        checkPlane(transformed.getPlane(), Vector3D.ZERO, Vector3D.PLUS_X, Vector3D.PLUS_Z);
    }

    @Test
    public void testTransform_halfSpace() {
        // arrange
        Plane plane = Plane.fromPointAndPlaneVectors(Vector3D.PLUS_Z, Vector3D.PLUS_X, Vector3D.PLUS_Y, TEST_PRECISION);
        ConvexSubPlane sp = ConvexSubPlane.fromConvexArea(plane,
                ConvexArea.fromBounds(Line.fromPoints(Vector2D.of(1, 0), Vector2D.of(1, 1), TEST_PRECISION)));

        AffineTransformMatrix3D transform = AffineTransformMatrix3D.createRotation(Vector3D.PLUS_Z,
                QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Y, Geometry.HALF_PI));

        // act
        ConvexSubPlane transformed = sp.transform(transform);

        // assert
        Assert.assertFalse(transformed.isFull());
        Assert.assertFalse(transformed.isEmpty());

        checkPlane(transformed.getPlane(), Vector3D.ZERO, Vector3D.MINUS_Z, Vector3D.PLUS_Y);
    }

    @Test
    public void testTransform_finite() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(
                Arrays.asList(Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), Vector3D.of(0, 0, 1)), TEST_PRECISION);

        Transform<Vector3D> transform = AffineTransformMatrix3D.createScale(2)
                .rotate(QuaternionRotation.fromAxisAngle(Vector3D.PLUS_Y, Geometry.HALF_PI));

        // act
        ConvexSubPlane transformed = sp.transform(transform);

        // assert
        Vector3D midpt = Vector3D.of(2, 2, -2).multiply(1 / 3.0);
        Vector3D normal = midpt.normalize();
        Vector3D u = Vector3D.of(0, 2, 2).normalize();

        checkPlane(transformed.getPlane(), midpt, u, normal.cross(u));

        checkVertices(transformed, Vector3D.of(0, 0, -2), Vector3D.of(0, 2, 0),
                Vector3D.of(2, 0, 0), Vector3D.of(0, 0, -2));

        checkPoints(transformed, RegionLocation.INSIDE, midpt);
    }

    @Test
    public void testTransform_reflection() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(
                Arrays.asList(Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), Vector3D.of(0, 0, 1)), TEST_PRECISION);

        Transform<Vector3D> transform = AffineTransformMatrix3D.createScale(-1, 1, 1);

        // act
        ConvexSubPlane transformed = sp.transform(transform);

        // assert
        Vector3D midpt = Vector3D.of(-1, 1, 1).multiply(1 / 3.0);
        Vector3D normal = midpt.negate().normalize();
        Vector3D u = Vector3D.of(1, 1, 0).normalize();

        checkPlane(transformed.getPlane(), midpt, u, normal.cross(u));

        checkVertices(transformed, Vector3D.of(-1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1), Vector3D.of(-1, 0, 0));

        checkPoints(transformed, RegionLocation.INSIDE, Vector3D.of(-1, 1, 1).multiply(1 / 3.0));
    }

    @Test
    public void testSplit_full() {
        // arrange
        Plane plane = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.PLUS_Z, TEST_PRECISION);
        ConvexSubPlane sp = ConvexSubPlane.fromConvexArea(plane, ConvexArea.full());

        Plane splitter = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.PLUS_X, TEST_PRECISION);

        // act
        Split<ConvexSubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        ConvexSubPlane minus = split.getMinus();
        Assert.assertEquals(1, minus.getSubspaceRegion().getBoundaries().size());
        checkPoints(minus, RegionLocation.BOUNDARY, Vector3D.ZERO, Vector3D.PLUS_Y, Vector3D.MINUS_Y);
        checkPoints(minus, RegionLocation.INSIDE, Vector3D.MINUS_X);
        checkPoints(minus, RegionLocation.OUTSIDE, Vector3D.PLUS_X);

        ConvexSubPlane plus = split.getPlus();
        Assert.assertEquals(1, plus.getSubspaceRegion().getBoundaries().size());
        checkPoints(plus, RegionLocation.BOUNDARY, Vector3D.ZERO, Vector3D.PLUS_Y, Vector3D.MINUS_Y);
        checkPoints(plus, RegionLocation.INSIDE, Vector3D.PLUS_X);
        checkPoints(plus, RegionLocation.OUTSIDE, Vector3D.MINUS_X);
    }

    @Test
    public void testSplit_both() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane splitter = Plane.fromPointAndNormal(Vector3D.ZERO, Vector3D.PLUS_Z, TEST_PRECISION);

        // act
        Split<ConvexSubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        ConvexSubPlane minus = split.getMinus();
        checkVertices(minus, Vector3D.of(1, 1, 0), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0), Vector3D.of(1, 1, 0));

        ConvexSubPlane plus = split.getPlus();
        checkVertices(plus, Vector3D.of(1, 1, 1), Vector3D.of(1, 1, 0), Vector3D.of(0, 2, 0), Vector3D.of(1, 1, 1));
    }

    @Test
    public void testSplit_plusOnly() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane splitter = Plane.fromPointAndNormal(Vector3D.of(0, 0, -3.1), Vector3D.PLUS_Z, TEST_PRECISION);

        // act
        Split<ConvexSubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());

        ConvexSubPlane plus = split.getPlus();
        checkVertices(plus, Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0), Vector3D.of(1, 1, 1));
    }

    @Test
    public void testSplit_minusOnly() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane splitter = Plane.fromPointAndNormal(Vector3D.of(0, 0, 1.1), Vector3D.PLUS_Z, TEST_PRECISION);

        // act
        Split<ConvexSubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        ConvexSubPlane minus = split.getMinus();
        checkVertices(minus, Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0), Vector3D.of(1, 1, 1));

        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallelSplitter_on() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane splitter = sp.getPlane();

        // act
        Split<ConvexSubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallelSplitter_minus() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane plane = sp.getPlane();
        Plane splitter = plane.translate(plane.getNormal());

        // act
        Split<ConvexSubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(sp, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_parallelSplitter_plus() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane plane = sp.getPlane();
        Plane splitter = plane.translate(plane.getNormal().negate());

        // act
        Split<ConvexSubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(sp, split.getPlus());
    }

    @Test
    public void testSplit_antiParallelSplitter_on() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane splitter = sp.getPlane().reverse();

        // act
        Split<ConvexSubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_antiParallelSplitter_minus() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane plane = sp.getPlane().reverse();
        Plane splitter = plane.translate(plane.getNormal());

        // act
        Split<ConvexSubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(sp, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_antiParallelSplitter_plus() {
        // arrange
        ConvexSubPlane sp = ConvexSubPlane.fromVertexLoop(Arrays.asList(
                    Vector3D.of(1, 1, 1), Vector3D.of(1, 1, -3), Vector3D.of(0, 2, 0)
                ), TEST_PRECISION);

        Plane plane = sp.getPlane().reverse();
        Plane splitter = plane.translate(plane.getNormal().negate());

        // act
        Split<ConvexSubPlane> split = sp.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(sp, split.getPlus());
    }

    private static void checkPlane(Plane plane, Vector3D origin, Vector3D u, Vector3D v) {
        u = u.normalize();
        v = v.normalize();
        Vector3D w = u.cross(v);

        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getOrigin(), TEST_EPS);
        Assert.assertTrue(plane.contains(origin));

        EuclideanTestUtils.assertCoordinatesEqual(u, plane.getU(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getU().norm(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(v, plane.getV(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getV().norm(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(w, plane.getW(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getW().norm(), TEST_EPS);

        EuclideanTestUtils.assertCoordinatesEqual(w, plane.getNormal(), TEST_EPS);
        Assert.assertEquals(1.0, plane.getNormal().norm(), TEST_EPS);

        double offset = plane.getOriginOffset();
        Assert.assertEquals(Vector3D.ZERO.distance(plane.getOrigin()), Math.abs(offset), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(origin, plane.getNormal().multiply(-offset), TEST_EPS);
    }

    private static void checkPoints(ConvexSubPlane sp, RegionLocation loc, Vector3D ... pts) {
        for (Vector3D pt : pts) {
            Assert.assertEquals("Unexpected subplane location for point " + pt, loc, sp.classify(pt));
        }
    }

    private static void checkVertices(ConvexSubPlane sp, Vector3D ... pts) {
        List<Vector3D> actual = sp.getPlane().toSpace(
                sp.getSubspaceRegion().getBoundaryPaths().get(0).getVertices());

        Assert.assertEquals(pts.length, actual.size());

        for (int i=0; i<pts.length; ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(pts[i], actual.get(i), TEST_EPS);
        }
    }
}
