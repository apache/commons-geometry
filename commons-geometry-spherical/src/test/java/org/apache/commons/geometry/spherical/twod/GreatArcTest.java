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

import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class GreatArcTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromInterval_full() {
        // act
        final GreatArc arc = GreatCircles.arcFromInterval(
                GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION),
                AngularInterval.full());

        // assert
        Assertions.assertTrue(arc.isFull());
        Assertions.assertFalse(arc.isEmpty());
        Assertions.assertTrue(arc.isFinite());
        Assertions.assertFalse(arc.isInfinite());

        Assertions.assertNull(arc.getStartPoint());
        Assertions.assertNull(arc.getEndPoint());

        Assertions.assertEquals(PlaneAngleRadians.TWO_PI, arc.getSize(), TEST_EPS);
        Assertions.assertNull(arc.getCentroid());

        for (double az = 0; az < PlaneAngleRadians.TWO_PI; az += 0.1) {
            checkClassify(arc, RegionLocation.INSIDE, Point2S.of(az, PlaneAngleRadians.PI_OVER_TWO));
        }

        checkClassify(arc, RegionLocation.OUTSIDE,
                Point2S.PLUS_K, Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO + 0.1),
                Point2S.MINUS_K, Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO - 0.1));
    }

    @Test
    public void testFromInterval_partial() {
        // arrange
        final GreatArc arc = GreatCircles.arcFromInterval(
                GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION),
                AngularInterval.Convex.of(PlaneAngleRadians.PI_OVER_TWO, 1.5 * PlaneAngleRadians.PI, TEST_PRECISION));

        // assert
        Assertions.assertFalse(arc.isFull());
        Assertions.assertFalse(arc.isEmpty());
        Assertions.assertTrue(arc.isFinite());
        Assertions.assertFalse(arc.isInfinite());

        checkArc(arc, Point2S.PLUS_K, Point2S.MINUS_K);
    }

    @Test
    public void testFromPoints() {
        // arrange
        final Point2S start = Point2S.PLUS_I;
        final Point2S end = Point2S.MINUS_K;

        // act
        final GreatArc arc = GreatCircles.arcFromPoints(start, end, TEST_PRECISION);

        // assert
        Assertions.assertFalse(arc.isFull());
        Assertions.assertFalse(arc.isEmpty());
        Assertions.assertTrue(arc.isFinite());
        Assertions.assertFalse(arc.isInfinite());

        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.PLUS_Y, arc.getCircle().getPole(), TEST_EPS);

        checkArc(arc, start, end);

        checkClassify(arc, RegionLocation.INSIDE, Point2S.of(0, 0.75 * PlaneAngleRadians.PI));
        checkClassify(arc, RegionLocation.BOUNDARY, start, end);
        checkClassify(arc, RegionLocation.OUTSIDE,
                Point2S.of(0, 0.25 * PlaneAngleRadians.PI), Point2S.of(PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI),
                Point2S.of(PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));
    }

    @Test
    public void testFromPoints_almostPi() {
        // arrange
        final Point2S start = Point2S.PLUS_J;
        final Point2S end = Point2S.of(1.5 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO - 1e-5);

        // act
        final GreatArc arc = GreatCircles.arcFromPoints(start, end, TEST_PRECISION);

        // assert
        Assertions.assertFalse(arc.isFull());
        Assertions.assertFalse(arc.isEmpty());
        Assertions.assertTrue(arc.isFinite());
        Assertions.assertFalse(arc.isInfinite());

        SphericalTestUtils.assertVectorsEqual(Vector3D.Unit.PLUS_X, arc.getCircle().getPole(), TEST_EPS);

        checkArc(arc, start, end);

        checkClassify(arc, RegionLocation.INSIDE, Point2S.PLUS_K);
        checkClassify(arc, RegionLocation.BOUNDARY, start, end);
        checkClassify(arc, RegionLocation.OUTSIDE, Point2S.MINUS_K);
    }

    @Test
    public void testFromPoints_usesShortestPath() {
        // act/assert
        SphericalTestUtils.assertVectorsEqual(
                Vector3D.Unit.MINUS_Y,
                GreatCircles.arcFromPoints(
                        Point2S.PLUS_I,
                        Point2S.of(PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO - 1e-5),
                        TEST_PRECISION).getCircle().getPole(), TEST_EPS);

        SphericalTestUtils.assertVectorsEqual(
                Vector3D.Unit.PLUS_Y,
                GreatCircles.arcFromPoints(
                        Point2S.PLUS_I,
                        Point2S.of(PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO + 1e-5),
                        TEST_PRECISION).getCircle().getPole(), TEST_EPS);
    }

    @Test
    public void testFromPoints_invalidPoints() {
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.of(1e-12, PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION));
        Assertions.assertThrows(IllegalArgumentException.class, () -> GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.MINUS_I, TEST_PRECISION));
    }

    @Test
    public void testToConvex() {
        // arrange
        final GreatArc arc = GreatCircles.arcFromInterval(
                GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.MINUS_I, TEST_PRECISION),
                AngularInterval.Convex.of(0.0, PlaneAngleRadians.PI, TEST_PRECISION));

        // act
        final List<GreatArc> result = arc.toConvex();

        // assert
        Assertions.assertEquals(1, result.size());
        Assertions.assertSame(arc, result.get(0));
    }

    @Test
    public void testReverse_full() {
        // arrange
        final GreatArc arc = GreatCircles.arcFromInterval(
                GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.MINUS_I, TEST_PRECISION),
                AngularInterval.full());

        // act
        final GreatArc result = arc.reverse();

        // assert
        checkGreatCircle(result.getCircle(), Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Y);

        Assertions.assertTrue(result.isFull());
    }

    @Test
    public void testReverse() {
        // arrange
        final GreatArc arc = GreatCircles.arcFromInterval(
                GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.MINUS_I, TEST_PRECISION),
                AngularInterval.Convex.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI, TEST_PRECISION));

        // act
        final GreatArc result = arc.reverse();

        // assert
        checkGreatCircle(result.getCircle(), Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Y);

        checkArc(result, Point2S.MINUS_J, Point2S.MINUS_I);
    }

    @Test
    public void testTransform() {
        // arrange
        final GreatArc arc = GreatCircles.fromPoints(Point2S.PLUS_K, Point2S.MINUS_I, TEST_PRECISION)
                .arc(PlaneAngleRadians.PI, -PlaneAngleRadians.PI_OVER_TWO);

        final Transform2S t = Transform2S.createRotation(Point2S.PLUS_I, PlaneAngleRadians.PI_OVER_TWO)
                .reflect(Point2S.of(-0.25 * PlaneAngleRadians.PI,  PlaneAngleRadians.PI_OVER_TWO));

        // act
        final GreatArc result = arc.transform(t);

        // assert
        checkArc(result, Point2S.PLUS_I, Point2S.PLUS_J);
    }

    @Test
    public void testSplit_full() {
        // arrange
        final GreatArc arc = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION).span();
        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        // act
        final Split<GreatArc> split = arc.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final GreatArc minus = split.getMinus();
        Assertions.assertSame(arc.getCircle(), minus.getCircle());
        checkArc(minus, Point2S.PLUS_J, Point2S.MINUS_J);
        checkClassify(minus, RegionLocation.OUTSIDE, Point2S.PLUS_I);
        checkClassify(minus, RegionLocation.INSIDE, Point2S.MINUS_I);

        final GreatArc plus = split.getPlus();
        Assertions.assertSame(arc.getCircle(), plus.getCircle());
        checkArc(plus, Point2S.MINUS_J, Point2S.PLUS_J);
        checkClassify(plus, RegionLocation.INSIDE, Point2S.PLUS_I);
        checkClassify(plus, RegionLocation.OUTSIDE, Point2S.MINUS_I);
    }

    @Test
    public void testSplit_both() {
        // arrange
        final GreatArc arc = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION)
                .arc(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI);
        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.of(0, 1, 1), TEST_PRECISION);

        // act
        final Split<GreatArc> split = arc.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final GreatArc minus = split.getMinus();
        Assertions.assertSame(arc.getCircle(), minus.getCircle());
        checkArc(minus, Point2S.of(0, 0), Point2S.of(1.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));

        final GreatArc plus = split.getPlus();
        Assertions.assertSame(arc.getCircle(), plus.getCircle());
        checkArc(plus, Point2S.of(1.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI), Point2S.MINUS_J);
    }

    @Test
    public void testSplit_minus() {
        // arrange
        final GreatArc arc = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION)
                .arc(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI);
        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION);


        // act
        final Split<GreatArc> split = arc.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        final GreatArc minus = split.getMinus();
        Assertions.assertSame(arc, minus);

        final GreatArc plus = split.getPlus();
        Assertions.assertNull(plus);
    }

    @Test
    public void testSplit_plus() {
        // arrange
        final GreatArc arc = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION)
                .arc(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI);
        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.Unit.from(-1, 0, -1), TEST_PRECISION);

        // act
        final Split<GreatArc> split = arc.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        final GreatArc minus = split.getMinus();
        Assertions.assertNull(minus);

        final GreatArc plus = split.getPlus();
        Assertions.assertSame(arc, plus);
    }

    @Test
    public void testSplit_parallelAndAntiparallel() {
        // arrange
        final GreatArc arc = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION).span();

        // act/assert
        Assertions.assertEquals(SplitLocation.NEITHER,
                arc.split(GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION)).getLocation());
        Assertions.assertEquals(SplitLocation.NEITHER,
                arc.split(GreatCircles.fromPole(Vector3D.Unit.MINUS_Z, TEST_PRECISION)).getLocation());
    }

    @Test
    public void testToString_full() {
        // arrange
        final GreatArc arc = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION).span();

        // act
        final String str = arc.toString();

        // assert
        GeometryTestUtils.assertContains("GreatArc[", str);
        GeometryTestUtils.assertContains("full= true", str);
        GeometryTestUtils.assertContains("circle= GreatCircle[", str);
    }

    @Test
    public void testToString_notFull() {
        // arrange
        final GreatArc arc = GreatCircles.arcFromInterval(
                GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION),
                AngularInterval.Convex.of(1, 2, TEST_PRECISION));

        // act
        final String str = arc.toString();

        // assert
        GeometryTestUtils.assertContains("GreatArc[", str);
        GeometryTestUtils.assertContains("start= (", str);
        GeometryTestUtils.assertContains("end= (", str);
    }

    private static void checkClassify(final GreatArc arc, final RegionLocation loc, final Point2S... pts) {
        for (final Point2S pt : pts) {
            Assertions.assertEquals(loc, arc.classify(pt), "Unexpected location for point " + pt);
        }
    }

    private static void checkArc(final GreatArc arc, final Point2S start, final Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);

        checkClassify(arc, RegionLocation.BOUNDARY, start, end);

        final Point2S mid = arc.getCircle().toSpace(arc.getInterval().getMidPoint());

        checkClassify(arc, RegionLocation.INSIDE, mid);
        checkClassify(arc, RegionLocation.OUTSIDE, mid.antipodal());

        Assertions.assertEquals(start.distance(end), arc.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(mid, arc.getCentroid(), TEST_EPS);
    }

    private static void checkGreatCircle(final GreatCircle circle, final Vector3D pole, final Vector3D x) {
        SphericalTestUtils.assertVectorsEqual(pole, circle.getPole(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(x, circle.getU(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(pole.cross(x), circle.getV(), TEST_EPS);
    }
}
