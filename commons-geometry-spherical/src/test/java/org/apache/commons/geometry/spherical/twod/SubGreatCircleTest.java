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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.RegionBSPTree1S;
import org.junit.Assert;
import org.junit.Test;

public class SubGreatCircleTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final GreatCircle XY_CIRCLE = GreatCircle.fromPoleAndXAxis(
            Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    @Test
    public void testCtor_default() {
        // act
        SubGreatCircle sub = new SubGreatCircle(XY_CIRCLE);

        // assert
        Assert.assertFalse(sub.isFull());
        Assert.assertTrue(sub.isEmpty());
        Assert.assertTrue(sub.isFinite());
        Assert.assertFalse(sub.isInfinite());

        Assert.assertEquals(0, sub.getSize(), TEST_EPS);

        for (double az = 0; az <= Geometry.TWO_PI; az += 0.5) {
            for (double p = 0; p <= Geometry.PI; p += 0.5) {
                checkClassify(sub, RegionLocation.OUTSIDE, Point2S.of(az, p));
            }
        }
    }

    @Test
    public void testCtor_boolean_true() {
        // act
        SubGreatCircle sub = new SubGreatCircle(XY_CIRCLE, true);

        // assert
        Assert.assertTrue(sub.isFull());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertTrue(sub.isFinite());
        Assert.assertFalse(sub.isInfinite());

        Assert.assertEquals(Geometry.TWO_PI, sub.getSize(), TEST_EPS);

        for (double az = 0; az < Geometry.TWO_PI; az += 0.1) {
            checkClassify(sub, RegionLocation.INSIDE, Point2S.of(az, Geometry.HALF_PI));
        }

        checkClassify(sub, RegionLocation.OUTSIDE,
                Point2S.PLUS_K, Point2S.of(0, Geometry.HALF_PI + 0.1),
                Point2S.MINUS_K, Point2S.of(0, Geometry.HALF_PI - 0.1));
    }

    @Test
    public void testCtor_boolean_false() {
        // act
        SubGreatCircle sub = new SubGreatCircle(XY_CIRCLE, false);

        // assert
        Assert.assertFalse(sub.isFull());
        Assert.assertTrue(sub.isEmpty());
        Assert.assertTrue(sub.isFinite());
        Assert.assertFalse(sub.isInfinite());

        Assert.assertEquals(0, sub.getSize(), TEST_EPS);

        for (double az = 0; az <= Geometry.TWO_PI; az += 0.5) {
            for (double p = 0; p <= Geometry.PI; p += 0.5) {
                checkClassify(sub, RegionLocation.OUTSIDE, Point2S.of(az, p));
            }
        }
    }

    @Test
    public void testCtor_tree() {
        // arrange
        RegionBSPTree1S tree = RegionBSPTree1S.fromInterval(AngularInterval.of(1, 2, TEST_PRECISION));

        // act
        SubGreatCircle sub = new SubGreatCircle(XY_CIRCLE, tree);

        // assert
        Assert.assertFalse(sub.isFull());
        Assert.assertFalse(sub.isEmpty());
        Assert.assertTrue(sub.isFinite());
        Assert.assertFalse(sub.isInfinite());

        Assert.assertEquals(1, sub.getSize(), TEST_EPS);

        checkClassify(sub, RegionLocation.INSIDE, Point2S.of(1.5, Geometry.HALF_PI));

        checkClassify(sub, RegionLocation.BOUNDARY,
                Point2S.of(1, Geometry.HALF_PI), Point2S.of(2, Geometry.HALF_PI));

        checkClassify(sub, RegionLocation.OUTSIDE,
                Point2S.of(0.5, Geometry.HALF_PI), Point2S.of(2.5, Geometry.HALF_PI),
                Point2S.of(1.5, 1), Point2S.of(1.5, Geometry.PI - 1));
    }

    @Test
    public void testSplit_full() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        SubGreatCircle sub = new SubGreatCircle(circle, true);

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        // act
        Split<SubGreatCircle> split = sub.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        SubGreatCircle minus = split.getMinus();
        Assert.assertSame(sub.getCircle(), minus.getCircle());

        List<Arc> minusArcs = minus.toConvex();
        Assert.assertEquals(1, minusArcs.size());
        checkArc(minusArcs.get(0), Point2S.MINUS_J, Point2S.PLUS_J);

        checkClassify(minus, RegionLocation.OUTSIDE, Point2S.MINUS_I);
        checkClassify(minus, RegionLocation.INSIDE, Point2S.PLUS_I);

        SubGreatCircle plus = split.getPlus();
        Assert.assertSame(sub.getCircle(), plus.getCircle());

        List<Arc> plusArcs = plus.toConvex();
        Assert.assertEquals(1, plusArcs.size());
        checkArc(plusArcs.get(0), Point2S.PLUS_J, Point2S.MINUS_J);

        checkClassify(plus, RegionLocation.INSIDE, Point2S.MINUS_I);
        checkClassify(plus, RegionLocation.OUTSIDE, Point2S.PLUS_I);
    }

    @Test
    public void testSplit_empty() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        SubGreatCircle sub = new SubGreatCircle(circle, false);

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        // act
        Split<SubGreatCircle> split = sub.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.NEITHER, split.getLocation());

        SubGreatCircle minus = split.getMinus();
        Assert.assertNull(minus);

        SubGreatCircle plus = split.getPlus();
        Assert.assertNull(plus);
    }

    @Test
    public void testSplit_both() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);

        RegionBSPTree1S tree = RegionBSPTree1S.empty();
        tree.add(AngularInterval.of(0, 1, TEST_PRECISION));
        tree.add(AngularInterval.of(Geometry.HALF_PI, Geometry.PI, TEST_PRECISION));
        tree.add(AngularInterval.of(Geometry.PI + 1, Geometry.PI + 2, TEST_PRECISION));

        SubGreatCircle sub = new SubGreatCircle(circle, tree);

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(0, 1, 1), TEST_PRECISION);

        // act
        Split<SubGreatCircle> split = sub.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        SubGreatCircle minus = split.getMinus();
        Assert.assertSame(sub.getCircle(), minus.getCircle());
        List<Arc> minusArcs = minus.toConvex();
        Assert.assertEquals(2, minusArcs.size());
        checkArc(minusArcs.get(0), Point2S.of(1.5 * Geometry.PI, 0.25 * Geometry.PI), Point2S.MINUS_J);
        checkArc(minusArcs.get(1), Point2S.of(1.5 * Geometry.PI, Geometry.HALF_PI + 1),
                Point2S.of(0.5 * Geometry.PI, (1.5 * Geometry.PI) - 2));

        SubGreatCircle plus = split.getPlus();
        Assert.assertSame(sub.getCircle(), plus.getCircle());
        List<Arc> plusArcs = plus.toConvex();
        Assert.assertEquals(2, plusArcs.size());
        checkArc(plusArcs.get(0), Point2S.of(Geometry.HALF_PI, Geometry.HALF_PI), Point2S.of(Geometry.HALF_PI, Geometry.HALF_PI - 1));
        checkArc(plusArcs.get(1), Point2S.of(0, 0), Point2S.of(1.5 * Geometry.PI, 0.25 * Geometry.PI));
    }

    @Test
    public void testSplit_minus() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);
        RegionBSPTree1S tree = AngularInterval.of(Geometry.HALF_PI, Geometry.PI, TEST_PRECISION).toTree();

        SubGreatCircle sub = new SubGreatCircle(circle, tree);

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.Unit.from(-1, 0, -1), TEST_PRECISION);

        // act
        Split<SubGreatCircle> split = sub.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        SubGreatCircle minus = split.getMinus();
        Assert.assertSame(sub, minus);

        SubGreatCircle plus = split.getPlus();
        Assert.assertNull(plus);
    }

    @Test
    public void testSplit_plus() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);
        RegionBSPTree1S tree = AngularInterval.of(Geometry.HALF_PI, Geometry.PI, TEST_PRECISION).toTree();

        SubGreatCircle sub = new SubGreatCircle(circle, tree);

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Split<SubGreatCircle> split = sub.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        SubGreatCircle minus = split.getMinus();
        Assert.assertNull(minus);

        SubGreatCircle plus = split.getPlus();
        Assert.assertSame(sub, plus);
    }

    @Test
    public void testSplit_parallelAndAntiparallel() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        RegionBSPTree1S tree = AngularInterval.of(Geometry.HALF_PI, Geometry.PI, TEST_PRECISION).toTree();

        SubGreatCircle sub = new SubGreatCircle(circle, tree);

        // act/assert
        Assert.assertEquals(SplitLocation.NEITHER,
                sub.split(GreatCircle.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION)).getLocation());
        Assert.assertEquals(SplitLocation.NEITHER,
                sub.split(GreatCircle.fromPole(Vector3D.Unit.MINUS_Z, TEST_PRECISION)).getLocation());
    }

    private static void checkClassify(SubHyperplane<Point2S> sub, RegionLocation loc, Point2S ... pts) {
        for (Point2S pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, sub.classify(pt));
        }
    }

    private static void checkArc(Arc arc, Point2S start, Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);
    }
}
