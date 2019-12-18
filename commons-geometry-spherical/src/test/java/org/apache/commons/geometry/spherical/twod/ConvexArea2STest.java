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

import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.junit.Assert;
import org.junit.Test;

public class ConvexArea2STest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    // epsilon value for use when comparing computed barycenter locations;
    // this must currently be set much higher than the other epsilon
    private static final double BARYCENTER_EPS = 1e-2;

    @Test
    public void testFull() {
        // act
        ConvexArea2S area = ConvexArea2S.full();

        // assert
        Assert.assertTrue(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(0, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(4 * PlaneAngleRadians.PI, area.getSize(), TEST_EPS);
        Assert.assertNull(area.getBarycenter());

        Assert.assertEquals(0, area.getBoundaries().size());

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.PLUS_I, Point2S.MINUS_I,
                Point2S.PLUS_J, Point2S.MINUS_J,
                Point2S.PLUS_K, Point2S.MINUS_K);
    }

    @Test
    public void testFromBounds_empty() {
        // act
        ConvexArea2S area = ConvexArea2S.fromBounds();

        // assert
        Assert.assertTrue(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(0, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(4 * PlaneAngleRadians.PI, area.getSize(), TEST_EPS);
        Assert.assertNull(area.getBarycenter());

        Assert.assertEquals(0, area.getBoundaries().size());

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE,
                Point2S.PLUS_I, Point2S.MINUS_I,
                Point2S.PLUS_J, Point2S.MINUS_J,
                Point2S.PLUS_K, Point2S.MINUS_K);
    }

    @Test
    public void testFromBounds_singleBound() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.PLUS_K, Point2S.PLUS_I, TEST_PRECISION);

        // act
        ConvexArea2S area = ConvexArea2S.fromBounds(circle);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(2 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(2 * PlaneAngleRadians.PI, area.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_J, area.getBarycenter(), TEST_EPS);
        checkBarycenterConsistency(area);

        Assert.assertEquals(1, area.getBoundaries().size());
        GreatArc arc = area.getBoundaries().get(0);
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
        GreatCircle a = GreatCircle.fromPoints(Point2S.PLUS_K, Point2S.PLUS_I, TEST_PRECISION);
        GreatCircle b = GreatCircle.fromPoints(
                Point2S.of(0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO), Point2S.PLUS_K, TEST_PRECISION);

        // act
        ConvexArea2S area = ConvexArea2S.fromBounds(a, b);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(2 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.125 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO), area.getBarycenter(), TEST_EPS);
        checkBarycenterConsistency(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
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
        GreatCircle a = GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        GreatCircle b = GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);

        // act
        ConvexArea2S area = ConvexArea2S.fromBounds(a, b);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(2 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, area.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0, 0.25 * PlaneAngleRadians.PI), area.getBarycenter(), TEST_EPS);
        checkBarycenterConsistency(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
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
        GreatCircle a = GreatCircle.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION);
        GreatCircle b = GreatCircle.fromPole(Vector3D.Unit.PLUS_Y, TEST_PRECISION);
        GreatCircle c = GreatCircle.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        ConvexArea2S area = ConvexArea2S.fromBounds(Arrays.asList(a, b, c));

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        Point2S expectedBarycenter = triangleBarycenter(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedBarycenter, area.getBarycenter(), TEST_EPS);

        checkBarycenterConsistency(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
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
        double azMin = 1.125 * PlaneAngleRadians.PI;
        double azMax = 1.375 * PlaneAngleRadians.PI;
        double azMid = 0.5 * (azMin + azMax);
        double polarTop = 0.1;
        double polarBottom = 0.25 * PlaneAngleRadians.PI;

        Point2S p1 = Point2S.of(azMin, polarBottom);
        Point2S p2 = Point2S.of(azMax, polarBottom);
        Point2S p3 = Point2S.of(azMid, polarTop);

        GreatCircle a = GreatCircle.fromPoints(p1, p2, TEST_PRECISION);
        GreatCircle b = GreatCircle.fromPoints(p2, p3, TEST_PRECISION);
        GreatCircle c = GreatCircle.fromPoints(p3, p1, TEST_PRECISION);

        // act
        ConvexArea2S area = ConvexArea2S.fromBounds(Arrays.asList(a, b, c));

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(p1.distance(p2) + p2.distance(p3) + p3.distance(p1),
                area.getBoundarySize(), TEST_EPS);
        double size = PlaneAngleRadians.TWO_PI - a.angle(b) - b.angle(c) - c.angle(a);
        Assert.assertEquals(size, area.getSize(), TEST_EPS);

        Point2S expectedBarycenter = triangleBarycenter(p1, p2, p3);
        SphericalTestUtils.assertPointsEq(expectedBarycenter, area.getBarycenter(), BARYCENTER_EPS);

        checkBarycenterConsistency(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
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
        Point2S p1 = Point2S.of(0.2, 0.1);
        Point2S p2 = Point2S.of(0.1, 0.2);
        Point2S p3 = Point2S.of(0.2, 0.5);
        Point2S p4 = Point2S.of(0.3, 0.2);

        GreatCircle c1 = GreatCircle.fromPoints(p1, p2, TEST_PRECISION);
        GreatCircle c2 = GreatCircle.fromPoints(p2, p3, TEST_PRECISION);
        GreatCircle c3 = GreatCircle.fromPoints(p3, p4, TEST_PRECISION);
        GreatCircle c4 = GreatCircle.fromPoints(p4, p1, TEST_PRECISION);

        // act
        ConvexArea2S area = ConvexArea2S.fromBounds(c1, c2, c3, c4);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(p1.distance(p2) + p2.distance(p3) + p3.distance(p4) + p4.distance(p1),
                area.getBoundarySize(), TEST_EPS);

        double size = 2 * PlaneAngleRadians.PI - c1.angle(c2) - c2.angle(c3) - c3.angle(c4) - c4.angle(c1);
        Assert.assertEquals(size, area.getSize(), TEST_EPS);

        checkBarycenterConsistency(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
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
        ConvexArea2S area = ConvexArea2S.fromPath(GreatArcPath.empty());

        // assert
        Assert.assertSame(ConvexArea2S.full(), area);
    }

    @Test
    public void testFromPath() {
        // arrange
        GreatArcPath path = GreatArcPath.builder(TEST_PRECISION)
                .append(Point2S.MINUS_I)
                .append(Point2S.MINUS_K)
                .append(Point2S.MINUS_J)
                .close();

        // act
        ConvexArea2S area = ConvexArea2S.fromPath(path);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        Point2S expectedBarycenter = triangleBarycenter(Point2S.MINUS_I, Point2S.MINUS_K, Point2S.MINUS_J);
        SphericalTestUtils.assertPointsEq(expectedBarycenter, area.getBarycenter(), TEST_EPS);

        checkBarycenterConsistency(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
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
        ConvexArea2S area = ConvexArea2S.fromVertices(Collections.emptyList(), TEST_PRECISION);

        // assert
        Assert.assertSame(ConvexArea2S.full(), area);
    }

    @Test
    public void testFromVertices() {
        // arrange
        Point2S p1 = Point2S.PLUS_I;
        Point2S p2 = Point2S.PLUS_J;
        Point2S p3 = Point2S.PLUS_K;

        // act
        ConvexArea2S area = ConvexArea2S.fromVertices(Arrays.asList(p1, p2, p3), TEST_PRECISION);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(2 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI, area.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0, 0.25 * PlaneAngleRadians.PI), area.getBarycenter(), TEST_EPS);
        checkBarycenterConsistency(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
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
        Point2S p1 = Point2S.PLUS_I;
        Point2S p2 = Point2S.PLUS_J;
        Point2S p3 = Point2S.PLUS_K;

        // act
        ConvexArea2S area = ConvexArea2S.fromVertices(Arrays.asList(p1, p2, p3, p1), TEST_PRECISION);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        Point2S expectedBarycenter = triangleBarycenter(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedBarycenter, area.getBarycenter(), TEST_EPS);

        checkBarycenterConsistency(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
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
        Point2S p1 = Point2S.PLUS_I;
        Point2S p2 = Point2S.PLUS_J;
        Point2S p3 = Point2S.PLUS_K;

        // act
        ConvexArea2S area = ConvexArea2S.fromVertices(Arrays.asList(
                p1, Point2S.of(1e-17, PlaneAngleRadians.PI_OVER_TWO), p2, p3, p3, p1), true, TEST_PRECISION);

        // assert
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        Point2S expectedBarycenter = triangleBarycenter(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedBarycenter, area.getBarycenter(), TEST_EPS);

        List<Point2S> vertices = area.getBoundaryPath().getVertices();
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
            ConvexArea2S.fromVertices(Arrays.asList(Point2S.PLUS_I), TEST_PRECISION);
        }, IllegalStateException.class);

        GeometryTestUtils.assertThrows(() -> {
            ConvexArea2S.fromVertices(Arrays.asList(Point2S.PLUS_I, Point2S.of(1e-16, PlaneAngleRadians.PI_OVER_TWO)), TEST_PRECISION);
        }, IllegalStateException.class);
    }

    @Test
    public void testFromVertexLoop() {
        // arrange
        Point2S p1 = Point2S.PLUS_I;
        Point2S p2 = Point2S.PLUS_J;
        Point2S p3 = Point2S.PLUS_K;

        // act
        ConvexArea2S area = ConvexArea2S.fromVertexLoop(Arrays.asList(p1, p2, p3), TEST_PRECISION);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        Point2S expectedBarycenter = triangleBarycenter(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedBarycenter, area.getBarycenter(), TEST_EPS);

        checkBarycenterConsistency(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
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
        ConvexArea2S area = ConvexArea2S.fromVertexLoop(Collections.emptyList(), TEST_PRECISION);

        // assert
        Assert.assertSame(ConvexArea2S.full(), area);
    }

    @Test
    public void testGetInteriorAngles_noAngles() {
        // act/assert
        Assert.assertEquals(0, ConvexArea2S.full().getInteriorAngles().length);
        Assert.assertEquals(0, ConvexArea2S.fromBounds(GreatCircle.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION))
                .getInteriorAngles().length);
    }

    @Test
    public void testGetInteriorAngles() {
        // arrange
        Point2S p1 = Point2S.PLUS_K;
        Point2S p2 = Point2S.PLUS_I;
        Point2S p4 = Point2S.PLUS_J;

        GreatCircle base = GreatCircle.fromPoints(p2, p4, TEST_PRECISION);
        GreatCircle c1 = base.transform(Transform2S.createRotation(p2, -0.2));
        GreatCircle c2 = base.transform(Transform2S.createRotation(p4, 0.1));

        Point2S p3 = c1.intersection(c2);

        // act
        ConvexArea2S area = ConvexArea2S.fromVertexLoop(Arrays.asList(p1, p2, p3, p4), TEST_PRECISION);

        // assert
        double[] angles = area.getInteriorAngles();
        Assert.assertEquals(4, angles.length);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO + 0.2, angles[0], TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI - c1.angle(c2), angles[1], TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO + 0.1, angles[2], TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, angles[3], TEST_EPS);
    }

    @Test
    public void testTransform() {
        // arrange
        Transform2S t = Transform2S.createReflection(Point2S.PLUS_J);
        ConvexArea2S input = ConvexArea2S.fromVertexLoop(
                Arrays.asList(Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K), TEST_PRECISION);

        // act
        ConvexArea2S area = input.transform(t);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(1.5 * PlaneAngleRadians.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, area.getSize(), TEST_EPS);

        Point2S expectedBarycenter = triangleBarycenter(Point2S.MINUS_J, Point2S.PLUS_I, Point2S.PLUS_K);
        SphericalTestUtils.assertPointsEq(expectedBarycenter, area.getBarycenter(), TEST_EPS);

        checkBarycenterConsistency(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
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
        GreatCircle c1 = GreatCircle.fromPole(Vector3D.Unit.MINUS_X, TEST_PRECISION);
        GreatCircle c2 = GreatCircle.fromPole(Vector3D.of(1, 1, 0), TEST_PRECISION);

        GreatCircle slanted = GreatCircle.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        ConvexArea2S area = ConvexArea2S.fromBounds(c1, c2);

        // act/assert
        checkArc(area.trim(GreatArc.fromPoints(Point2S.of(0.1, PlaneAngleRadians.PI_OVER_TWO), Point2S.MINUS_I, TEST_PRECISION)),
                Point2S.PLUS_J, Point2S.of(0.75 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO));

        checkArc(area.trim(GreatArc.fromPoints(Point2S.MINUS_I, Point2S.of(0.2, PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION)),
                Point2S.of(0.75 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO), Point2S.PLUS_J);

        checkArc(area.trim(GreatArc.fromPoints(Point2S.of(0.6 * PlaneAngleRadians.PI, 0.1), Point2S.of(0.7 * PlaneAngleRadians.PI, 0.8), TEST_PRECISION)),
                Point2S.of(0.6 * PlaneAngleRadians.PI, 0.1), Point2S.of(0.7 * PlaneAngleRadians.PI, 0.8));

        Assert.assertNull(area.trim(GreatArc.fromPoints(Point2S.MINUS_I, Point2S.MINUS_J, TEST_PRECISION)));

        checkArc(area.trim(slanted.span()), c1.intersection(slanted), slanted.intersection(c2));
    }

    @Test
    public void testSplit_both() {
        // arrange
        GreatCircle c1 = GreatCircle.fromPole(Vector3D.Unit.MINUS_X, TEST_PRECISION);
        GreatCircle c2 = GreatCircle.fromPole(Vector3D.of(1, 1, 0), TEST_PRECISION);

        ConvexArea2S area = ConvexArea2S.fromBounds(c1, c2);

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        // act
        Split<ConvexArea2S> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        Point2S p1 = c1.intersection(splitter);
        Point2S p2 = splitter.intersection(c2);

        ConvexArea2S minus = split.getMinus();
        assertPath(minus.getBoundaryPath(), Point2S.PLUS_K, p1, p2, Point2S.PLUS_K);

        ConvexArea2S plus = split.getPlus();
        assertPath(plus.getBoundaryPath(), p1, Point2S.MINUS_K, p2, p1);

        Assert.assertEquals(area.getSize(), minus.getSize() + plus.getSize(), TEST_EPS);
    }

    @Test
    public void testSplit_minus() {
        // arrange
        ConvexArea2S area = ConvexArea2S.fromVertexLoop(Arrays.asList(
                    Point2S.PLUS_I, Point2S.PLUS_K, Point2S.MINUS_J
                ), TEST_PRECISION);

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(0, -1, 1), TEST_PRECISION);

        // act
        Split<ConvexArea2S> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Assert.assertSame(area, split.getMinus());
        Assert.assertNull(split.getPlus());
    }

    @Test
    public void testSplit_plus() {
        // arrange
        ConvexArea2S area = ConvexArea2S.fromVertexLoop(Arrays.asList(
                    Point2S.PLUS_I, Point2S.PLUS_K, Point2S.MINUS_J
                ), TEST_PRECISION);

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(0, 1, -1), TEST_PRECISION);

        // act
        Split<ConvexArea2S> split = area.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Assert.assertNull(split.getMinus());
        Assert.assertSame(area, split.getPlus());
    }

    @Test
    public void testToTree() {
        // arrange
        ConvexArea2S area = ConvexArea2S.fromVertexLoop(Arrays.asList(
                    Point2S.of(0.1, 0.1), Point2S.of(-0.4, 1),
                    Point2S.of(0.15, 1.5), Point2S.of(0.3, 1.2),
                    Point2S.of(0.1, 0.1)
                ), TEST_PRECISION);

        // act
        RegionBSPTree2S tree = area.toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(area.getSize(), tree.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(area.getBarycenter(), tree.getBarycenter(), TEST_EPS);
    }

    private static List<GreatArc> sortArcs(List<GreatArc> arcs) {
        List<GreatArc> result = new ArrayList<>(arcs);

        Collections.sort(result, (a, b) ->
            Point2S.POLAR_AZIMUTH_ASCENDING_ORDER.compare(a.getStartPoint(), b.getStartPoint()));

        return result;
    }

    private static Point2S triangleBarycenter(Point2S p1, Point2S p2, Point2S p3) {
        // compute the barycenter using intersection mid point arcs
        GreatCircle c1 = GreatCircle.fromPoints(p1, p2.slerp(p3, 0.5), TEST_PRECISION);
        GreatCircle c2 = GreatCircle.fromPoints(p2, p1.slerp(p3, 0.5), TEST_PRECISION);

        return c1.intersection(c2);
    }

    private static void checkArc(GreatArc arc, Point2S start, Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);
    }

    private static void assertPath(GreatArcPath path, Point2S... expectedVertices) {
        List<Point2S> vertices = path.getVertices();

        Assert.assertEquals(expectedVertices.length, vertices.size());
        for (int i = 0; i < expectedVertices.length; ++i) {

            if (!expectedVertices[i].eq(vertices.get(i), TEST_PRECISION)) {
                String msg = "Unexpected point in path at index " + i + ". Expected " +
                        Arrays.toString(expectedVertices) + " but received " + vertices;
                Assert.fail(msg);
            }
        }
    }

    private static void checkBarycenterConsistency(ConvexArea2S area) {
        Point2S barycenter = area.getBarycenter();
        double size = area.getSize();

        SphericalTestUtils.checkClassify(area, RegionLocation.INSIDE, barycenter);

        GreatCircle circle = GreatCircle.fromPole(barycenter.getVector(), TEST_PRECISION);
        for (double az = 0; az <= PlaneAngleRadians.TWO_PI; az += 0.2) {
            Point2S pt = circle.toSpace(Point1S.of(az));
            GreatCircle splitter = GreatCircle.fromPoints(barycenter, pt, TEST_PRECISION);

            Split<ConvexArea2S> split = area.split(splitter);

            Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

            ConvexArea2S minus = split.getMinus();
            double minusSize = minus.getSize();
            Point2S minusBc = minus.getBarycenter();

            Vector3D weightedMinus = minusBc.getVector()
                    .multiply(minus.getSize());

            ConvexArea2S plus = split.getPlus();
            double plusSize = plus.getSize();
            Point2S plusBc = plus.getBarycenter();

            Vector3D weightedPlus = plusBc.getVector()
                    .multiply(plus.getSize());
            Point2S computedBarycenter = Point2S.from(weightedMinus.add(weightedPlus));

            Assert.assertEquals(size, minusSize + plusSize, TEST_EPS);
            SphericalTestUtils.assertPointsEq(barycenter, computedBarycenter, BARYCENTER_EPS);
        }
    }
}
