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
package org.apache.commons.geometry.spherical.twod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class ConvexArea2STest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFull() {
        // act
        final ConvexArea2S area = ConvexArea2S.full();

        // assert
        Assert.assertTrue(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(0, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(4 * PlaneAngleRadians.PI, area.getSize(), TEST_EPS);
        Assert.assertNull(area.getCentroid());

        Assert.assertEquals(0, area.getBoundaries().size());

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.PLUS_I, Point2S.MINUS_I,
                Point2S.PLUS_J, Point2S.MINUS_J,
                Point2S.PLUS_K, Point2S.MINUS_K);
    }

    @Test
    public void testFromBounds_empty() {
        // act
        final ConvexArea2S area = ConvexArea2S.fromBounds();

        // assert
        Assert.assertTrue(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(0, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(4 * PlaneAngleRadians.PI, area.getSize(), TEST_EPS);
        Assert.assertNull(area.getCentroid());

        Assert.assertEquals(0, area.getBoundaries().size());

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.PLUS_I, Point2S.MINUS_I,
                Point2S.PLUS_J, Point2S.MINUS_J,
                Point2S.PLUS_K, Point2S.MINUS_K);
    }

    @Test
    public void testFromBounds_singleBound() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.PLUS_K, Point2S.PLUS_I, TEST_PRECISION);

        // act
        final ConvexArea2S area = ConvexArea2S.fromBounds(circle);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(2 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(2 * PlaneAngleRadians.PI, area.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_J, area.getCentroid(), TEST_EPS);
        checkCentroidConsistency(area);

        Assert.assertEquals(1, area.getBoundaries().size());
        final GreatArc arc = area.getBoundaries().get(0);
        Assert.assertTrue(arc.isFull());
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_J, arc.getCircle().getPolePoint(), TEST_EPS);

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE, Point2S.PLUS_J);

        SphericalTestUtils.checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.MINUS_I,
                Point2S.PLUS_K, Point2S.MINUS_K);

        SphericalTestUtils.checkClassify(area, RegionLocation.OUTSIDE, Point2S.MINUS_J);
    }

    @Test
    public void testFromBounds_lune_intersectionAtPoles() {
        // arrange
        final GreatCircle a = GreatCircles.fromPoints(Point2S.PLUS_K, Point2S.PLUS_I, TEST_PRECISION);
        final GreatCircle b = GreatCircles.fromPoints(
                Point2S.of(0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO), Point2S.PLUS_K, TEST_PRECISION);

        // act
        final ConvexArea2S area = ConvexArea2S.fromBounds(a, b);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(2 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.125 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO),
                area.getCentroid(), TEST_EPS);
        checkCentroidConsistency(area);

        final List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(2, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_K, Point2S.MINUS_K);
        checkArc(arcs.get(1), Point2S.MINUS_K, Point2S.PLUS_K);

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.of(0.125 * PlaneAngleRadians.PI, 0.1),
                Point2S.of(0.125 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO),
                Point2S.of(0.125 * PlaneAngleRadians.PI, PlaneAngleRadians.PI - 0.1));

        SphericalTestUtils.checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.of(0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO),
                Point2S.PLUS_K, Point2S.MINUS_K);

        SphericalTestUtils.checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.PLUS_J, Point2S.MINUS_J);
    }

    @Test
    public void testFromBounds_lune_intersectionAtEquator() {
        // arrange
        final GreatCircle a = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatCircle b = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);

        // act
        final ConvexArea2S area = ConvexArea2S.fromBounds(a, b);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(2 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, area.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0, 0.25 * PlaneAngleRadians.PI), area.getCentroid(), TEST_EPS);
        checkCentroidConsistency(area);

        final List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(2, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_J, Point2S.MINUS_J);
        checkArc(arcs.get(1), Point2S.MINUS_J, Point2S.PLUS_J);

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.of(0, 0.25 * PlaneAngleRadians.PI),
                Point2S.of(0.25, 0.4 * PlaneAngleRadians.PI),
                Point2S.of(-0.25, 0.4 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.PLUS_K,
                Point2S.PLUS_J, Point2S.MINUS_J);

        SphericalTestUtils.checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.MINUS_I, Point2S.MINUS_K,
                Point2S.of(PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI),
                Point2S.of(PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI));
    }

    @Test
    public void testFromBounds_triangle_large() {
        // arrange
        final GreatCircle a = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final GreatCircle b = GreatCircles.fromPole(Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        final GreatCircle c = GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final ConvexArea2S area = ConvexArea2S.fromBounds(Arrays.asList(a, b, c));

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        final Point2S expectedCentroid = triangleCentroid(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedCentroid, area.getCentroid(), TEST_EPS);

        checkCentroidConsistency(area);

        final List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(3, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_K, Point2S.PLUS_I);
        checkArc(arcs.get(1), Point2S.PLUS_I, Point2S.PLUS_J);
        checkArc(arcs.get(2), Point2S.PLUS_J, Point2S.PLUS_K);

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.of(0.25 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K,
                Point2S.of(0, 0.25 * PlaneAngleRadians.PI), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.304 * PlaneAngleRadians.PI),
                Point2S.of(0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO));

        SphericalTestUtils.checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.MINUS_I, Point2S.MINUS_J, Point2S.MINUS_K);
    }

    @Test
    public void testFromBounds_triangle_small() {
        // arrange
        final double azMin = 1.125 * PlaneAngleRadians.PI;
        final double azMax = 1.375 * PlaneAngleRadians.PI;
        final double azMid = 0.5 * (azMin + azMax);
        final double polarTop = 0.1;
        final double polarBottom = 0.25 * PlaneAngleRadians.PI;

        final Point2S p1 = Point2S.of(azMin, polarBottom);
        final Point2S p2 = Point2S.of(azMax, polarBottom);
        final Point2S p3 = Point2S.of(azMid, polarTop);

        final GreatCircle a = GreatCircles.fromPoints(p1, p2, TEST_PRECISION);
        final GreatCircle b = GreatCircles.fromPoints(p2, p3, TEST_PRECISION);
        final GreatCircle c = GreatCircles.fromPoints(p3, p1, TEST_PRECISION);

        // act
        final ConvexArea2S area = ConvexArea2S.fromBounds(Arrays.asList(a, b, c));

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(p1.distance(p2) + p2.distance(p3) + p3.distance(p1),
                area.getBoundarySize(), TEST_EPS);
        final double size = PlaneAngleRadians.TWO_PI - a.angle(b) - b.angle(c) - c.angle(a);
        Assert.assertEquals(size, area.getSize(), TEST_EPS);

        final Point2S expectedCentroid = triangleCentroid(p1, p2, p3);
        SphericalTestUtils.assertPointsEq(expectedCentroid, area.getCentroid(), TEST_EPS);

        checkCentroidConsistency(area);

        final List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(3, arcs.size());

        checkArc(arcs.get(0), p3, p1);
        checkArc(arcs.get(1), p1, p2);
        checkArc(arcs.get(2), p2, p3);

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE, Point2S.of(azMid, 0.11));

        SphericalTestUtils.checkClassify(area, RegionLocation.BOUNDARY,
                p1, p2, p3, p1.slerp(p2, 0.2));

        SphericalTestUtils.checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K,
                Point2S.MINUS_I, Point2S.MINUS_J, Point2S.MINUS_K);
    }

    @Test
    public void testFromBounds_quad() {
        // arrange
        final Point2S p1 = Point2S.of(0.2, 0.1);
        final Point2S p2 = Point2S.of(0.1, 0.2);
        final Point2S p3 = Point2S.of(0.2, 0.5);
        final Point2S p4 = Point2S.of(0.3, 0.2);

        final GreatCircle c1 = GreatCircles.fromPoints(p1, p2, TEST_PRECISION);
        final GreatCircle c2 = GreatCircles.fromPoints(p2, p3, TEST_PRECISION);
        final GreatCircle c3 = GreatCircles.fromPoints(p3, p4, TEST_PRECISION);
        final GreatCircle c4 = GreatCircles.fromPoints(p4, p1, TEST_PRECISION);

        // act
        final ConvexArea2S area = ConvexArea2S.fromBounds(c1, c2, c3, c4);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(p1.distance(p2) + p2.distance(p3) + p3.distance(p4) + p4.distance(p1),
                area.getBoundarySize(), TEST_EPS);

        final double size = 2 * PlaneAngleRadians.PI - c1.angle(c2) - c2.angle(c3) - c3.angle(c4) - c4.angle(c1);
        Assert.assertEquals(size, area.getSize(), TEST_EPS);

        checkCentroidConsistency(area);

        final List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(4, arcs.size());

        checkArc(arcs.get(0), p1, p2);
        checkArc(arcs.get(1), p2, p3);
        checkArc(arcs.get(2), p4, p1);
        checkArc(arcs.get(3), p3, p4);

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE, Point2S.of(0.2, 0.11));

        SphericalTestUtils.checkClassify(area, RegionLocation.BOUNDARY,
                p1, p2, p3, p4, p1.slerp(p2, 0.2));

        SphericalTestUtils.checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K,
                Point2S.MINUS_I, Point2S.MINUS_J, Point2S.MINUS_K);
    }

    @Test
    public void testFromPath_empty() {
        // act
        final ConvexArea2S area = ConvexArea2S.fromPath(GreatArcPath.empty());

        // assert
        Assert.assertSame(ConvexArea2S.full(), area);
    }

    @Test
    public void testFromPath() {
        // arrange
        final GreatArcPath path = GreatArcPath.builder(TEST_PRECISION)
                .append(Point2S.MINUS_I)
                .append(Point2S.MINUS_K)
                .append(Point2S.MINUS_J)
                .close();

        // act
        final ConvexArea2S area = ConvexArea2S.fromPath(path);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        final Point2S expectedCentroid = triangleCentroid(Point2S.MINUS_I, Point2S.MINUS_K, Point2S.MINUS_J);
        SphericalTestUtils.assertPointsEq(expectedCentroid, area.getCentroid(), TEST_EPS);

        checkCentroidConsistency(area);

        final List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(3, arcs.size());
        checkArc(arcs.get(0), Point2S.MINUS_I, Point2S.MINUS_K);
        checkArc(arcs.get(1), Point2S.MINUS_J, Point2S.MINUS_I);
        checkArc(arcs.get(2), Point2S.MINUS_K, Point2S.MINUS_J);

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.of(1.25 * PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.MINUS_I, Point2S.MINUS_J, Point2S.MINUS_K);

        SphericalTestUtils.checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K);
    }

    @Test
    public void testFromVertices_empty() {
        // act
        final ConvexArea2S area = ConvexArea2S.fromVertices(Collections.emptyList(), TEST_PRECISION);

        // assert
        Assert.assertSame(ConvexArea2S.full(), area);
    }

    @Test
    public void testFromVertices() {
        // arrange
        final Point2S p1 = Point2S.PLUS_I;
        final Point2S p2 = Point2S.PLUS_J;
        final Point2S p3 = Point2S.PLUS_K;

        // act
        final ConvexArea2S area = ConvexArea2S.fromVertices(Arrays.asList(p1, p2, p3), TEST_PRECISION);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(2 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, area.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0, 0.25 * PlaneAngleRadians.PI), area.getCentroid(), TEST_EPS);
        checkCentroidConsistency(area);

        final List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(2, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_J, Point2S.MINUS_J);
        checkArc(arcs.get(1), Point2S.MINUS_J, Point2S.PLUS_J);

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.of(-0.25 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI),
                Point2S.of(0, 0.25 * PlaneAngleRadians.PI),
                Point2S.of(0.25 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.PLUS_J,
                Point2S.PLUS_K, Point2S.MINUS_J);

        SphericalTestUtils.checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.MINUS_I, Point2S.MINUS_K);
    }

    @Test
    public void testFromVertices_lastVertexRepeated() {
        // arrange
        final Point2S p1 = Point2S.PLUS_I;
        final Point2S p2 = Point2S.PLUS_J;
        final Point2S p3 = Point2S.PLUS_K;

        // act
        final ConvexArea2S area = ConvexArea2S.fromVertices(Arrays.asList(p1, p2, p3, p1), TEST_PRECISION);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        final Point2S expectedCentroid = triangleCentroid(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedCentroid, area.getCentroid(), TEST_EPS);

        checkCentroidConsistency(area);

        final List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(3, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_K, Point2S.PLUS_I);
        checkArc(arcs.get(1), Point2S.PLUS_I, Point2S.PLUS_J);
        checkArc(arcs.get(2), Point2S.PLUS_J, Point2S.PLUS_K);

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.of(0.25 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K,
                Point2S.of(0, 0.25 * PlaneAngleRadians.PI), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.304 * PlaneAngleRadians.PI),
                Point2S.of(0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO));

        SphericalTestUtils.checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.MINUS_I, Point2S.MINUS_J, Point2S.MINUS_K);
    }

    @Test
    public void testFromVertices_verticesRepeated() {
        // arrange
        final Point2S p1 = Point2S.PLUS_I;
        final Point2S p2 = Point2S.PLUS_J;
        final Point2S p3 = Point2S.PLUS_K;

        // act
        final ConvexArea2S area = ConvexArea2S.fromVertices(Arrays.asList(
                p1, Point2S.of(1e-17, PlaneAngleRadians.PI_OVER_TWO), p2, p3, p3, p1), true, TEST_PRECISION);

        // assert
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        final Point2S expectedCentroid = triangleCentroid(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedCentroid, area.getCentroid(), TEST_EPS);

        final List<Point2S> vertices = area.getBoundaryPath().getVertices();
        Assert.assertEquals(4, vertices.size());
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_K, vertices.get(0), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, vertices.get(1), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_J, vertices.get(2), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_K, vertices.get(3), TEST_EPS);
    }

    @Test
    public void testFromVertices_invalidArguments() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            ConvexArea2S.fromVertices(Collections.singletonList(Point2S.PLUS_I), TEST_PRECISION);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea2S.fromVertices(Arrays.asList(Point2S.PLUS_I, Point2S.of(1e-16, PlaneAngleRadians.PI_OVER_TWO)), TEST_PRECISION);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertexLoop() {
        // arrange
        final Point2S p1 = Point2S.PLUS_I;
        final Point2S p2 = Point2S.PLUS_J;
        final Point2S p3 = Point2S.PLUS_K;

        // act
        final ConvexArea2S area = ConvexArea2S.fromVertexLoop(Arrays.asList(p1, p2, p3), TEST_PRECISION);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        final Point2S expectedCentroid = triangleCentroid(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedCentroid, area.getCentroid(), TEST_EPS);

        checkCentroidConsistency(area);

        final List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(3, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_K, Point2S.PLUS_I);
        checkArc(arcs.get(1), Point2S.PLUS_I, Point2S.PLUS_J);
        checkArc(arcs.get(2), Point2S.PLUS_J, Point2S.PLUS_K);

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.of(0.25 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K,
                Point2S.of(0, 0.25 * PlaneAngleRadians.PI), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.304 * PlaneAngleRadians.PI),
                Point2S.of(0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO));

        SphericalTestUtils.checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.MINUS_I, Point2S.MINUS_J, Point2S.MINUS_K);
    }

    @Test
    public void testFromVertexLoop_empty() {
        // act
        final ConvexArea2S area = ConvexArea2S.fromVertexLoop(Collections.emptyList(), TEST_PRECISION);

        // assert
        Assert.assertSame(ConvexArea2S.full(), area);
    }

    @Test
    public void testBoundaryStream() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION);
        final ConvexArea2S area = ConvexArea2S.fromBounds(circle);

        // act
        final List<GreatArc> arcs = area.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(1, arcs.size());
        Assert.assertSame(circle, arcs.get(0).getCircle());
    }

    @Test
    public void testBoundaryStream_noBoundaries() {
        // arrange
        final ConvexArea2S area = ConvexArea2S.full();

        // act
        final List<GreatArc> arcs = area.boundaryStream().collect(Collectors.toList());

        // assert
        Assert.assertEquals(0, arcs.size());
    }

    @Test
    public void testGetInteriorAngles_noAngles() {
        // act/assert
        Assert.assertEquals(0, ConvexArea2S.full().getInteriorAngles().length);
        Assert.assertEquals(0, ConvexArea2S.fromBounds(GreatCircles.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION))
                .getInteriorAngles().length);
    }

    @Test
    public void testGetInteriorAngles() {
        // arrange
        final Point2S p1 = Point2S.PLUS_K;
        final Point2S p2 = Point2S.PLUS_I;
        final Point2S p4 = Point2S.PLUS_J;

        final GreatCircle base = GreatCircles.fromPoints(p2, p4, TEST_PRECISION);
        final GreatCircle c1 = base.transform(Transform2S.createRotation(p2, -0.2));
        final GreatCircle c2 = base.transform(Transform2S.createRotation(p4, 0.1));

        final Point2S p3 = c1.intersection(c2);

        // act
        final ConvexArea2S area = ConvexArea2S.fromVertexLoop(Arrays.asList(p1, p2, p3, p4), TEST_PRECISION);

        // assert
        final double[] angles = area.getInteriorAngles();
        Assert.assertEquals(4, angles.length);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO + 0.2, angles[0], TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI - c1.angle(c2), angles[1], TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO + 0.1, angles[2], TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, angles[3], TEST_EPS);
    }

    @Test
    public void testTransform() {
        // arrange
        final Transform2S t = Transform2S.createReflection(Point2S.PLUS_J);
        final ConvexArea2S input = ConvexArea2S.fromVertexLoop(
                Arrays.asList(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K), TEST_PRECISION);

        // act
        final ConvexArea2S area = input.transform(t);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        final Point2S expectedCentroid = triangleCentroid(Point2S.MINUS_J, Point2S.PLUS_I, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedCentroid, area.getCentroid(), TEST_EPS);

        checkCentroidConsistency(area);

        final List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(3, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_K, Point2S.MINUS_J);
        checkArc(arcs.get(1), Point2S.PLUS_I, Point2S.PLUS_K);
        checkArc(arcs.get(2), Point2S.MINUS_J, Point2S.PLUS_I);

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.of(-0.25 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));

        SphericalTestUtils.checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.MINUS_J, Point2S.PLUS_K,
                Point2S.of(0, 0.25 * PlaneAngleRadians.PI), Point2S.of(-PlaneAngleRadians.PI_OVER_TWO, 0.304 * PlaneAngleRadians.PI),
                Point2S.of(-0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO));

        SphericalTestUtils.checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.PLUS_J, Point2S.MINUS_I, Point2S.MINUS_K);
    }

    @Test
    public void testTrim() {
        // arrange
        final GreatCircle c1 = GreatCircles.fromPole(Vector3D.Unit.MINUS_X, TEST_PRECISION);
        final GreatCircle c2 = GreatCircles.fromPole(Vector3D.of(1, 1, 0), TEST_PRECISION);

        final GreatCircle slanted = GreatCircles.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        final ConvexArea2S area = ConvexArea2S.fromBounds(c1, c2);

        // act/assert
        checkArc(area.trim(GreatCircles.arcFromPoints(Point2S.of(0.1, PlaneAngleRadians.PI_OVER_TWO), Point2S.MINUS_I, TEST_PRECISION)),
                Point2S.PLUS_J, Point2S.of(0.75 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO));

        checkArc(area.trim(GreatCircles.arcFromPoints(Point2S.MINUS_I, Point2S.of(0.2, PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION)),
                Point2S.of(0.75 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO), Point2S.PLUS_J);

        checkArc(area.trim(GreatCircles.arcFromPoints(Point2S.of(0.6 * PlaneAngleRadians.PI, 0.1), Point2S.of(0.7 * PlaneAngleRadians.PI, 0.8), TEST_PRECISION)),
                Point2S.of(0.6 * PlaneAngleRadians.PI, 0.1), Point2S.of(0.7 * PlaneAngleRadians.PI, 0.8));

        Assert.assertNull(area.trim(GreatCircles.arcFromPoints(Point2S.MINUS_I, Point2S.MINUS_J, TEST_PRECISION)));

        checkArc(area.trim(slanted.span()), c1.intersection(slanted), slanted.intersection(c2));
    }

    @Test
    public void testSplit_both() {
        // arrange
        final GreatCircle c1 = GreatCircles.fromPole(Vector3D.Unit.MINUS_X, TEST_PRECISION);
        final GreatCircle c2 = GreatCircles.fromPole(Vector3D.of(1, 1, 0), TEST_PRECISION);

        final ConvexArea2S area = ConvexArea2S.fromBounds(c1, c2);

        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        // act
        final Split<ConvexArea2S> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        final Point2S p1 = c1.intersection(splitter);
        final Point2S p2 = splitter.intersection(c2);

        final ConvexArea2S minus = split.getMinus();
        assertPath(minus.getBoundaryPath(), Point2S.PLUS_K, p1, p2, Point2S.PLUS_K);

        final ConvexArea2S plus = split.getPlus();
        assertPath(plus.getBoundaryPath(), p1, Point2S.MINUS_K, p2, p1);

        Assert.assertEquals(area.getSize(), minus.getSize() + plus.getSize(), TEST_EPS);
    }

    @Test
    public void testSplit_minus() {
        // arrange
        final ConvexArea2S area = ConvexArea2S.fromVertexLoop(Arrays.asList(
                    Point2S.PLUS_I, Point2S.PLUS_K, Point2S.MINUS_J
                ), TEST_PRECISION);

        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.of(0, -1, 1), TEST_PRECISION);

        // act
        final Split<ConvexArea2S> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(area, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_plus() {
        // arrange
        final ConvexArea2S area = ConvexArea2S.fromVertexLoop(Arrays.asList(
                    Point2S.PLUS_I, Point2S.PLUS_K, Point2S.MINUS_J
                ), TEST_PRECISION);

        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.of(0, 1, -1), TEST_PRECISION);

        // act
        final Split<ConvexArea2S> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(area, split.getPlus());
    }

    @Test
    public void testToTree_full() {
        // arrange
        final ConvexArea2S area = ConvexArea2S.full();

        // act
        final RegionBSPTree2S tree = area.toTree();

        // assert
        Assert.assertTrue(tree.isFull());
        Assert.assertFalse(tree.isEmpty());
    }

    @Test
    public void testToTree() {
        // arrange
        final ConvexArea2S area = ConvexArea2S.fromVertexLoop(Arrays.asList(
                    Point2S.of(0.1, 0.1), Point2S.of(-0.4, 1),
                    Point2S.of(0.15, 1.5), Point2S.of(0.3, 1.2),
                    Point2S.of(0.1, 0.1)
                ), TEST_PRECISION);

        // act
        final RegionBSPTree2S tree = area.toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(area.getSize(), tree.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(area.getCentroid(), tree.getCentroid(), TEST_EPS);
    }

    private static List<GreatArc> sortArcs(final List<GreatArc> arcs) {
        final List<GreatArc> result = new ArrayList<>(arcs);

        result.sort((a, b) ->
                Point2S.POLAR_AZIMUTH_ASCENDING_ORDER.compare(a.getStartPoint(), b.getStartPoint()));

        return result;
    }

    private static Point2S triangleCentroid(final Point2S p1, final Point2S p2, final Point2S p3) {
        // compute the centroid as the sum of the cross product of each point pair weighted by
        // the angle between the points
        final Vector3D v1 = p1.getVector();
        final Vector3D v2 = p2.getVector();
        final Vector3D v3 = p3.getVector();

        Vector3D sum = Vector3D.ZERO;
        sum = sum.add(v1.cross(v2).withNorm(v1.angle(v2)));
        sum = sum.add(v2.cross(v3).withNorm(v2.angle(v3)));
        sum = sum.add(v3.cross(v1).withNorm(v3.angle(v1)));

        return Point2S.from(sum);
    }

    private static void checkArc(final GreatArc arc, final Point2S start, final Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);
    }

    private static void assertPath(final GreatArcPath path, final Point2S... expectedVertices) {
        final List<Point2S> vertices = path.getVertices();

        Assert.assertEquals(expectedVertices.length, vertices.size());
        for (int i = 0; i < expectedVertices.length; ++i) {

            if (!expectedVertices[i].eq(vertices.get(i), TEST_PRECISION)) {
                final String msg = "Unexpected point in path at index " + i + ". Expected " +
                        Arrays.toString(expectedVertices) + " but received " + vertices;
                Assert.fail(msg);
            }
        }
    }

    private static void checkCentroidConsistency(final ConvexArea2S area) {
        final Point2S centroid = area.getCentroid();
        final double size = area.getSize();

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE, centroid);

        final GreatCircle circle = GreatCircles.fromPole(centroid.getVector(), TEST_PRECISION);
        for (double az = 0; az <= PlaneAngleRadians.TWO_PI; az += 0.2) {
            final Point2S pt = circle.toSpace(Point1S.of(az));
            final GreatCircle splitter = GreatCircles.fromPoints(centroid, pt, TEST_PRECISION);

            final Split<ConvexArea2S> split = area.split(splitter);

            Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

            final ConvexArea2S minus = split.getMinus();
            final double minusSize = minus.getSize();

            final ConvexArea2S plus = split.getPlus();
            final double plusSize = plus.getSize();

            final Point2S computedCentroid = Point2S.from(minus.getWeightedCentroidVector()
                    .add(plus.getWeightedCentroidVector()));

            Assert.assertEquals(size, minusSize + plusSize, TEST_EPS);
            SphericalTestUtils.assertPointsEq(centroid, computedCentroid, TEST_EPS);
        }
    }
}
