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
import org.apache.commons.geometry.core.partitioning.HyperplaneSubset;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.Point1S;
import org.apache.commons.geometry.spherical.oned.RegionBSPTree1S;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmbeddedTreeSubGreatCircleTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final GreatCircle XY_CIRCLE = GreatCircles.fromPoleAndU(
            Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    @Test
    void testCtor_default() {
        // act
        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(XY_CIRCLE);

        // assert
        Assertions.assertSame(XY_CIRCLE, sub.getHyperplane());
        Assertions.assertSame(TEST_PRECISION, sub.getPrecision());
        Assertions.assertFalse(sub.isFull());
        Assertions.assertTrue(sub.isEmpty());
        Assertions.assertTrue(sub.isFinite());
        Assertions.assertFalse(sub.isInfinite());

        Assertions.assertEquals(0, sub.getSize(), TEST_EPS);
        Assertions.assertNull(sub.getCentroid());

        for (double az = 0; az <= Angle.TWO_PI; az += 0.5) {
            for (double p = 0; p <= Math.PI; p += 0.5) {
                checkClassify(sub, RegionLocation.OUTSIDE, Point2S.of(az, p));
            }
        }
    }

    @Test
    void testCtor_boolean_true() {
        // act
        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(XY_CIRCLE, true);

        // assert
        Assertions.assertTrue(sub.isFull());
        Assertions.assertFalse(sub.isEmpty());
        Assertions.assertTrue(sub.isFinite());
        Assertions.assertFalse(sub.isInfinite());

        Assertions.assertEquals(Angle.TWO_PI, sub.getSize(), TEST_EPS);
        Assertions.assertNull(sub.getCentroid());

        for (double az = 0; az < Angle.TWO_PI; az += 0.1) {
            checkClassify(sub, RegionLocation.INSIDE, Point2S.of(az, Angle.PI_OVER_TWO));
        }

        checkClassify(sub, RegionLocation.OUTSIDE,
                Point2S.PLUS_K, Point2S.of(0, Angle.PI_OVER_TWO + 0.1),
                Point2S.MINUS_K, Point2S.of(0, Angle.PI_OVER_TWO - 0.1));
    }

    @Test
    void testCtor_boolean_false() {
        // act
        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(XY_CIRCLE, false);

        // assert
        Assertions.assertFalse(sub.isFull());
        Assertions.assertTrue(sub.isEmpty());
        Assertions.assertTrue(sub.isFinite());
        Assertions.assertFalse(sub.isInfinite());

        Assertions.assertEquals(0, sub.getSize(), TEST_EPS);
        Assertions.assertNull(sub.getCentroid());

        for (double az = 0; az <= Angle.TWO_PI; az += 0.5) {
            for (double p = 0; p <= Math.PI; p += 0.5) {
                checkClassify(sub, RegionLocation.OUTSIDE, Point2S.of(az, p));
            }
        }
    }

    @Test
    void testCtor_tree() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.fromInterval(AngularInterval.of(1, 2, TEST_PRECISION));

        // act
        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(XY_CIRCLE, tree);

        // assert
        Assertions.assertFalse(sub.isFull());
        Assertions.assertFalse(sub.isEmpty());
        Assertions.assertTrue(sub.isFinite());
        Assertions.assertFalse(sub.isInfinite());

        Assertions.assertEquals(1, sub.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(1.5, Angle.PI_OVER_TWO),
                sub.getCentroid(), TEST_EPS);

        checkClassify(sub, RegionLocation.INSIDE, Point2S.of(1.5, Angle.PI_OVER_TWO));

        checkClassify(sub, RegionLocation.BOUNDARY,
                Point2S.of(1, Angle.PI_OVER_TWO), Point2S.of(2, Angle.PI_OVER_TWO));

        checkClassify(sub, RegionLocation.OUTSIDE,
                Point2S.of(0.5, Angle.PI_OVER_TWO), Point2S.of(2.5, Angle.PI_OVER_TWO),
                Point2S.of(1.5, 1), Point2S.of(1.5, Math.PI - 1));
    }

    @Test
    void testToSubspace() {
        // arrange
        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(XY_CIRCLE);

        // act/assert
        SphericalTestUtils.assertPointsEqual(Point1S.of(1), sub.toSubspace(Point2S.of(1, 0.5)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point1S.of(1), sub.toSubspace(Point2S.of(1, 0.75)), TEST_EPS);
    }

    @Test
    void testToSpace() {
        // arrange
        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(XY_CIRCLE);

        // act/assert
        SphericalTestUtils.assertPointsEqual(Point2S.of(0, 0.5 * Math.PI), sub.toSpace(Point1S.of(0)), TEST_EPS);
        SphericalTestUtils.assertPointsEqual(Point2S.of(1, 0.5 * Math.PI), sub.toSpace(Point1S.of(1)), TEST_EPS);
    }

    @Test
    void testClosest() {
        // arrange
        final RegionBSPTree1S tree = RegionBSPTree1S.fromInterval(AngularInterval.of(1, 2, TEST_PRECISION));
        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(XY_CIRCLE, tree);

        final double halfPi = 0.5 * Math.PI;
        final double above = halfPi - 0.1;
        final double below = halfPi + 0.1;

        // act/assert
        SphericalTestUtils.assertPointsEq(Point2S.of(1, halfPi), sub.closest(Point2S.of(0, above)), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(1, halfPi), sub.closest(Point2S.of(0, below)), TEST_EPS);

        SphericalTestUtils.assertPointsEq(Point2S.of(1, halfPi), sub.closest(Point2S.of(1, above)), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(1, halfPi), sub.closest(Point2S.of(1, below)), TEST_EPS);

        SphericalTestUtils.assertPointsEq(Point2S.of(1.5, halfPi), sub.closest(Point2S.of(1.5, above)), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(1.5, halfPi), sub.closest(Point2S.of(1.5, below)), TEST_EPS);

        SphericalTestUtils.assertPointsEq(Point2S.of(2, halfPi), sub.closest(Point2S.of(2, above)), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(2, halfPi), sub.closest(Point2S.of(2, below)), TEST_EPS);

        SphericalTestUtils.assertPointsEq(Point2S.of(2, halfPi), sub.closest(Point2S.of(3, above)), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(2, halfPi), sub.closest(Point2S.of(3, below)), TEST_EPS);
    }

    @Test
    void testTransform() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.PLUS_K, Point2S.MINUS_I, TEST_PRECISION);
        final RegionBSPTree1S region = RegionBSPTree1S.empty();
        region.add(AngularInterval.of(Math.PI, -Angle.PI_OVER_TWO, TEST_PRECISION));
        region.add(AngularInterval.of(0, Angle.PI_OVER_TWO, TEST_PRECISION));

        final Transform2S t = Transform2S.createRotation(Point2S.PLUS_I, Angle.PI_OVER_TWO)
                .reflect(Point2S.of(-0.25 * Math.PI,  Angle.PI_OVER_TWO));

        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle, region);

        // act
        final EmbeddedTreeGreatCircleSubset result = sub.transform(t);

        // assert
        final List<GreatArc> arcs = result.toConvex();
        Assertions.assertEquals(2, arcs.size());

        checkArc(arcs.get(0), Point2S.MINUS_I, Point2S.MINUS_J);
        checkArc(arcs.get(1), Point2S.PLUS_I, Point2S.PLUS_J);
    }

    @Test
    void testSplit_full() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle, true);

        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        // act
        final Split<EmbeddedTreeGreatCircleSubset> split = sub.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final EmbeddedTreeGreatCircleSubset minus = split.getMinus();
        Assertions.assertSame(sub.getCircle(), minus.getCircle());

        final List<GreatArc> minusArcs = minus.toConvex();
        Assertions.assertEquals(1, minusArcs.size());
        checkArc(minusArcs.get(0), Point2S.MINUS_J, Point2S.PLUS_J);

        checkClassify(minus, RegionLocation.OUTSIDE, Point2S.MINUS_I);
        checkClassify(minus, RegionLocation.INSIDE, Point2S.PLUS_I);

        final EmbeddedTreeGreatCircleSubset plus = split.getPlus();
        Assertions.assertSame(sub.getCircle(), plus.getCircle());

        final List<GreatArc> plusArcs = plus.toConvex();
        Assertions.assertEquals(1, plusArcs.size());
        checkArc(plusArcs.get(0), Point2S.PLUS_J, Point2S.MINUS_J);

        checkClassify(plus, RegionLocation.INSIDE, Point2S.MINUS_I);
        checkClassify(plus, RegionLocation.OUTSIDE, Point2S.PLUS_I);
    }

    @Test
    void testSplit_empty() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle, false);

        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        // act
        final Split<EmbeddedTreeGreatCircleSubset> split = sub.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.NEITHER, split.getLocation());

        final EmbeddedTreeGreatCircleSubset minus = split.getMinus();
        Assertions.assertNull(minus);

        final EmbeddedTreeGreatCircleSubset plus = split.getPlus();
        Assertions.assertNull(plus);
    }

    @Test
    void testSplit_both() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);

        final RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(0, 1, TEST_PRECISION));
        tree.add(AngularInterval.of(Angle.PI_OVER_TWO, Math.PI, TEST_PRECISION));
        tree.add(AngularInterval.of(Math.PI + 1, Math.PI + 2, TEST_PRECISION));

        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle, tree);

        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.of(0, 1, 1), TEST_PRECISION);

        // act
        final Split<EmbeddedTreeGreatCircleSubset> split = sub.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.BOTH, split.getLocation());

        final EmbeddedTreeGreatCircleSubset minus = split.getMinus();
        Assertions.assertSame(sub.getCircle(), minus.getCircle());
        final List<GreatArc> minusArcs = minus.toConvex();
        Assertions.assertEquals(2, minusArcs.size());
        checkArc(minusArcs.get(0), Point2S.of(1.5 * Math.PI, 0.25 * Math.PI), Point2S.MINUS_J);
        checkArc(minusArcs.get(1), Point2S.of(1.5 * Math.PI, Angle.PI_OVER_TWO + 1),
                Point2S.of(0.5 * Math.PI, (1.5 * Math.PI) - 2));

        final EmbeddedTreeGreatCircleSubset plus = split.getPlus();
        Assertions.assertSame(sub.getCircle(), plus.getCircle());
        final List<GreatArc> plusArcs = plus.toConvex();
        Assertions.assertEquals(2, plusArcs.size());
        checkArc(plusArcs.get(0), Point2S.of(Angle.PI_OVER_TWO, Angle.PI_OVER_TWO), Point2S.of(Angle.PI_OVER_TWO, Angle.PI_OVER_TWO - 1));
        checkArc(plusArcs.get(1), Point2S.of(0, 0), Point2S.of(1.5 * Math.PI, 0.25 * Math.PI));
    }

    @Test
    void testSplit_minus() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);
        final RegionBSPTree1S tree = AngularInterval.of(Angle.PI_OVER_TWO, Math.PI, TEST_PRECISION).toTree();

        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle, tree);

        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.Unit.from(-1, 0, -1), TEST_PRECISION);

        // act
        final Split<EmbeddedTreeGreatCircleSubset> split = sub.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.MINUS, split.getLocation());

        final EmbeddedTreeGreatCircleSubset minus = split.getMinus();
        Assertions.assertSame(sub, minus);

        final EmbeddedTreeGreatCircleSubset plus = split.getPlus();
        Assertions.assertNull(plus);
    }

    @Test
    void testSplit_plus() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);
        final RegionBSPTree1S tree = AngularInterval.of(Angle.PI_OVER_TWO, Math.PI, TEST_PRECISION).toTree();

        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle, tree);

        final GreatCircle splitter = GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        final Split<EmbeddedTreeGreatCircleSubset> split = sub.split(splitter);

        // assert
        Assertions.assertEquals(SplitLocation.PLUS, split.getLocation());

        final EmbeddedTreeGreatCircleSubset minus = split.getMinus();
        Assertions.assertNull(minus);

        final EmbeddedTreeGreatCircleSubset plus = split.getPlus();
        Assertions.assertSame(sub, plus);
    }

    @Test
    void testSplit_parallelAndAntiparallel() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final RegionBSPTree1S tree = AngularInterval.of(Angle.PI_OVER_TWO, Math.PI, TEST_PRECISION).toTree();

        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle, tree);

        // act/assert
        Assertions.assertEquals(SplitLocation.NEITHER,
                sub.split(GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION)).getLocation());
        Assertions.assertEquals(SplitLocation.NEITHER,
                sub.split(GreatCircles.fromPole(Vector3D.Unit.MINUS_Z, TEST_PRECISION)).getLocation());
    }

    @Test
    void testAdd_arc() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);
        final GreatCircle closeCircle = GreatCircles.fromPoints(Point2S.MINUS_K,
                Point2S.of((1.5 * Math.PI) - 1e-11, Angle.PI_OVER_TWO), TEST_PRECISION);

        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle);

        // act
        sub.add(circle.arc(Point2S.of(1.5 * Math.PI, 0.75 * Math.PI), Point2S.MINUS_J));
        sub.add(closeCircle.arc(Point2S.PLUS_J, Point2S.of(1.5 * Math.PI, 0.75 * Math.PI)));

        // assert
        final List<GreatArc> arcs = sub.toConvex();

        Assertions.assertEquals(1, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_J, Point2S.MINUS_J);
    }

    @Test
    void testAdd_arc_differentCircle() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);
        final GreatCircle otherCircle = GreatCircles.fromPoints(Point2S.MINUS_K,
                Point2S.of((1.5 * Math.PI) - 1e-2, Angle.PI_OVER_TWO), TEST_PRECISION);

        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle);
        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () ->  sub.add(otherCircle.arc(Point2S.PLUS_J, Point2S.of(1.5 * Math.PI, 0.75 * Math.PI))));
    }

    @Test
    void testAdd_subGreatCircle() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);
        final GreatCircle closeCircle = GreatCircles.fromPoints(Point2S.MINUS_K,
                Point2S.of((1.5 * Math.PI) - 1e-11, Angle.PI_OVER_TWO), TEST_PRECISION);

        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle);

        final RegionBSPTree1S regionA = RegionBSPTree1S.empty();
        regionA.add(AngularInterval.of(Math.PI, 1.25 * Math.PI, TEST_PRECISION));
        regionA.add(AngularInterval.of(0.25 * Math.PI, Angle.PI_OVER_TWO, TEST_PRECISION));

        final RegionBSPTree1S regionB = RegionBSPTree1S.empty();
        regionB.add(AngularInterval.of(1.5 * Math.PI, 0.25 * Math.PI, TEST_PRECISION));

        // act
        sub.add(new EmbeddedTreeGreatCircleSubset(circle, regionA));
        sub.add(new EmbeddedTreeGreatCircleSubset(closeCircle, regionB));

        // assert
        final List<GreatArc> arcs = sub.toConvex();

        Assertions.assertEquals(2, arcs.size());
        checkArc(arcs.get(0), Point2S.of(Angle.PI_OVER_TWO, 0), Point2S.of(Angle.PI_OVER_TWO, 0.25 * Math.PI));
        checkArc(arcs.get(1), Point2S.PLUS_J, Point2S.MINUS_J);
    }

    @Test
    void testAdd_subGreatCircle_otherCircle() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);
        final GreatCircle otherCircle = GreatCircles.fromPoints(Point2S.MINUS_K, Point2S.of((1.5 * Math.PI) - 1e-5, Angle.PI_OVER_TWO), TEST_PRECISION);

        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle);

        // act/assert
        Assertions.assertThrows(IllegalArgumentException.class, () ->  sub.add(new EmbeddedTreeGreatCircleSubset(otherCircle, RegionBSPTree1S.full())));
    }

    @Test
    void testToString() {
        // arrange
        final GreatCircle circle = GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final EmbeddedTreeGreatCircleSubset sub = new EmbeddedTreeGreatCircleSubset(circle);

        // act
        final String str = sub.toString();

        // assert
        GeometryTestUtils.assertContains("EmbeddedTreeGreatCircleSubset[", str);
        GeometryTestUtils.assertContains("circle= GreatCircle[", str);
        GeometryTestUtils.assertContains("region= RegionBSPTree1S[", str);
    }

    private static void checkClassify(final HyperplaneSubset<Point2S> sub, final RegionLocation loc, final Point2S... pts) {
        for (final Point2S pt : pts) {
            Assertions.assertEquals(loc, sub.classify(pt), "Unexpected location for point " + pt);
        }
    }

    private static void checkArc(final GreatArc arc, final Point2S start, final Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);
    }
}
