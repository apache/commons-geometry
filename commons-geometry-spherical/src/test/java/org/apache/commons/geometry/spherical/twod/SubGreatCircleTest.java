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
import org.apache.commons.geometry.core.Transform;
import org.apache.commons.geometry.core.partitioning.ConvexSubHyperplane;
import org.apache.commons.geometry.core.partitioning.Hyperplane;
import org.apache.commons.geometry.core.partitioning.Split;
import org.apache.commons.geometry.core.partitioning.SplitLocation;
import org.apache.commons.geometry.core.partitioning.SubHyperplane;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.apache.commons.geometry.spherical.oned.RegionBSPTree1S;
import org.apache.commons.geometry.spherical.twod.SubGreatCircle.SubGreatCircleBuilder;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class SubGreatCircleTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final GreatCircle XY_CIRCLE = GreatCircle.fromPoleAndU(
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

        for (double az = 0; az <= PlaneAngleRadians.TWO_PI; az += 0.5) {
            for (double p = 0; p <= PlaneAngleRadians.PI; p += 0.5) {
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

        Assert.assertEquals(PlaneAngleRadians.TWO_PI, sub.getSize(), TEST_EPS);

        for (double az = 0; az < PlaneAngleRadians.TWO_PI; az += 0.1) {
            checkClassify(sub, RegionLocation.INSIDE, Point2S.of(az, PlaneAngleRadians.PI_OVER_TWO));
        }

        checkClassify(sub, RegionLocation.OUTSIDE,
                Point2S.PLUS_K, Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO + 0.1),
                Point2S.MINUS_K, Point2S.of(0, PlaneAngleRadians.PI_OVER_TWO - 0.1));
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

        for (double az = 0; az <= PlaneAngleRadians.TWO_PI; az += 0.5) {
            for (double p = 0; p <= PlaneAngleRadians.PI; p += 0.5) {
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

        checkClassify(sub, RegionLocation.INSIDE, Point2S.of(1.5, PlaneAngleRadians.PI_OVER_TWO));

        checkClassify(sub, RegionLocation.BOUNDARY,
                Point2S.of(1, PlaneAngleRadians.PI_OVER_TWO), Point2S.of(2, PlaneAngleRadians.PI_OVER_TWO));

        checkClassify(sub, RegionLocation.OUTSIDE,
                Point2S.of(0.5, PlaneAngleRadians.PI_OVER_TWO), Point2S.of(2.5, PlaneAngleRadians.PI_OVER_TWO),
                Point2S.of(1.5, 1), Point2S.of(1.5, PlaneAngleRadians.PI - 1));
    }

    @Test
    public void testTransform() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.PLUS_K, Point2S.MINUS_I, TEST_PRECISION);
        RegionBSPTree1S region = RegionBSPTree1S.empty();
        region.add(AngularInterval.of(PlaneAngleRadians.PI, -PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION));
        region.add(AngularInterval.of(0, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION));

        Transform2S t = Transform2S.createRotation(Point2S.PLUS_I, PlaneAngleRadians.PI_OVER_TWO)
                .reflect(Point2S.of(-0.25 * PlaneAngleRadians.PI,  PlaneAngleRadians.PI_OVER_TWO));

        SubGreatCircle sub = new SubGreatCircle(circle, region);

        // act
        SubGreatCircle result = sub.transform(t);

        // assert
        List<GreatArc> arcs = result.toConvex();
        Assert.assertEquals(2, arcs.size());

        checkArc(arcs.get(0), Point2S.MINUS_I, Point2S.MINUS_J);
        checkArc(arcs.get(1), Point2S.PLUS_I, Point2S.PLUS_J);
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

        List<GreatArc> minusArcs = minus.toConvex();
        Assert.assertEquals(1, minusArcs.size());
        checkArc(minusArcs.get(0), Point2S.MINUS_J, Point2S.PLUS_J);

        checkClassify(minus, RegionLocation.OUTSIDE, Point2S.MINUS_I);
        checkClassify(minus, RegionLocation.INSIDE, Point2S.PLUS_I);

        SubGreatCircle plus = split.getPlus();
        Assert.assertSame(sub.getCircle(), plus.getCircle());

        List<GreatArc> plusArcs = plus.toConvex();
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
        tree.add(AngularInterval.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI, TEST_PRECISION));
        tree.add(AngularInterval.of(PlaneAngleRadians.PI + 1, PlaneAngleRadians.PI + 2, TEST_PRECISION));

        SubGreatCircle sub = new SubGreatCircle(circle, tree);

        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(0, 1, 1), TEST_PRECISION);

        // act
        Split<SubGreatCircle> split = sub.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        SubGreatCircle minus = split.getMinus();
        Assert.assertSame(sub.getCircle(), minus.getCircle());
        List<GreatArc> minusArcs = minus.toConvex();
        Assert.assertEquals(2, minusArcs.size());
        checkArc(minusArcs.get(0), Point2S.of(1.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI), Point2S.MINUS_J);
        checkArc(minusArcs.get(1), Point2S.of(1.5 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO + 1),
                Point2S.of(0.5 * PlaneAngleRadians.PI, (1.5 * PlaneAngleRadians.PI) - 2));

        SubGreatCircle plus = split.getPlus();
        Assert.assertSame(sub.getCircle(), plus.getCircle());
        List<GreatArc> plusArcs = plus.toConvex();
        Assert.assertEquals(2, plusArcs.size());
        checkArc(plusArcs.get(0), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI_OVER_TWO - 1));
        checkArc(plusArcs.get(1), Point2S.of(0, 0), Point2S.of(1.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));
    }

    @Test
    public void testSplit_minus() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);
        RegionBSPTree1S tree = AngularInterval.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI, TEST_PRECISION).toTree();

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
        RegionBSPTree1S tree = AngularInterval.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI, TEST_PRECISION).toTree();

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
        RegionBSPTree1S tree = AngularInterval.of(PlaneAngleRadians.PI_OVER_TWO, PlaneAngleRadians.PI, TEST_PRECISION).toTree();

        SubGreatCircle sub = new SubGreatCircle(circle, tree);

        // act/assert
        Assert.assertEquals(SplitLocation.NEITHER,
                sub.split(GreatCircle.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION)).getLocation());
        Assert.assertEquals(SplitLocation.NEITHER,
                sub.split(GreatCircle.fromPole(Vector3D.Unit.MINUS_Z, TEST_PRECISION)).getLocation());
    }

    @Test
    public void testAdd_arc() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);
        GreatCircle closeCircle = GreatCircle.fromPoints(Point2S.MINUS_K,
                Point2S.of((1.5 * PlaneAngleRadians.PI) - 1e-11, PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION);

        SubGreatCircle sub = new SubGreatCircle(circle);

        // act
        sub.add(circle.arc(Point2S.of(1.5 * PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI), Point2S.MINUS_J));
        sub.add(closeCircle.arc(Point2S.PLUS_J, Point2S.of(1.5 * PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI)));

        // assert
        List<GreatArc> arcs = sub.toConvex();

        Assert.assertEquals(1, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_J, Point2S.MINUS_J);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdd_arc_differentCircle() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);
        GreatCircle otherCircle = GreatCircle.fromPoints(Point2S.MINUS_K,
                Point2S.of((1.5 * PlaneAngleRadians.PI) - 1e-2, PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION);

        SubGreatCircle sub = new SubGreatCircle(circle);

        // act/assert
        sub.add(otherCircle.arc(Point2S.PLUS_J, Point2S.of(1.5 * PlaneAngleRadians.PI, 0.75 * PlaneAngleRadians.PI)));
    }

    @Test
    public void testAdd_subGreatCircle() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);
        GreatCircle closeCircle = GreatCircle.fromPoints(Point2S.MINUS_K,
                Point2S.of((1.5 * PlaneAngleRadians.PI) - 1e-11, PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION);

        SubGreatCircle sub = new SubGreatCircle(circle);

        RegionBSPTree1S regionA = RegionBSPTree1S.empty();
        regionA.add(AngularInterval.of(PlaneAngleRadians.PI, 1.25 * PlaneAngleRadians.PI, TEST_PRECISION));
        regionA.add(AngularInterval.of(0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION));

        RegionBSPTree1S regionB = RegionBSPTree1S.empty();
        regionB.add(AngularInterval.of(1.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI, TEST_PRECISION));

        // act
        sub.add(new SubGreatCircle(circle, regionA));
        sub.add(new SubGreatCircle(closeCircle, regionB));

        // assert
        List<GreatArc> arcs = sub.toConvex();

        Assert.assertEquals(2, arcs.size());
        checkArc(arcs.get(0), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.25 * PlaneAngleRadians.PI));
        checkArc(arcs.get(1), Point2S.PLUS_J, Point2S.MINUS_J);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAdd_subGreatCircle_otherCircle() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);
        GreatCircle otherCircle = GreatCircle.fromPoints(Point2S.MINUS_K,
                Point2S.of((1.5 * PlaneAngleRadians.PI) - 1e-5, PlaneAngleRadians.PI_OVER_TWO), TEST_PRECISION);

        SubGreatCircle sub = new SubGreatCircle(circle);

        // act/assert
        sub.add(new SubGreatCircle(otherCircle, RegionBSPTree1S.full()));
    }

    @Test
    public void testBuilder() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);

        SubGreatCircle sub = new SubGreatCircle(circle);

        RegionBSPTree1S region = RegionBSPTree1S.empty();
        region.add(AngularInterval.of(PlaneAngleRadians.PI, 1.25 * PlaneAngleRadians.PI, TEST_PRECISION));
        region.add(AngularInterval.of(0.25 * PlaneAngleRadians.PI, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION));

        // act
        SubGreatCircleBuilder builder = sub.builder();

        builder.add(new SubGreatCircle(circle, region));
        builder.add(circle.arc(1.5 * PlaneAngleRadians.PI, 0.25 * PlaneAngleRadians.PI));

        SubGreatCircle result = builder.build();

        // assert
        List<GreatArc> arcs = result.toConvex();

        Assert.assertEquals(2, arcs.size());
        checkArc(arcs.get(0), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0), Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.25 * PlaneAngleRadians.PI));
        checkArc(arcs.get(1), Point2S.PLUS_J, Point2S.MINUS_J);
    }

    @Test
    public void testBuilder_invalidArgs() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.MINUS_K, Point2S.MINUS_J, TEST_PRECISION);
        GreatCircle otherCircle = GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);

        SubGreatCircle sub = new SubGreatCircle(circle);

        SubGreatCircleBuilder builder = sub.builder();

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            builder.add(otherCircle.span());
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            builder.add(new SubGreatCircle(otherCircle));
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            builder.add(new UnknownSubHyperplane());
        }, IllegalArgumentException.class);
    }

    @Test
    public void testToString() {
        // arrange
        GreatCircle circle = GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        SubGreatCircle sub = new SubGreatCircle(circle);

        // act
        String str = sub.toString();

        // assert
        GeometryTestUtils.assertContains("SubGreatCircle[", str);
        GeometryTestUtils.assertContains("circle= GreatCircle[", str);
        GeometryTestUtils.assertContains("region= RegionBSPTree1S[", str);
    }

    private static void checkClassify(SubHyperplane<Point2S> sub, RegionLocation loc, Point2S ... pts) {
        for (Point2S pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, sub.classify(pt));
        }
    }

    private static void checkArc(GreatArc arc, Point2S start, Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);
    }

    private static class UnknownSubHyperplane implements SubHyperplane<Point2S> {

        @Override
        public Split<? extends SubHyperplane<Point2S>> split(Hyperplane<Point2S> splitter) {
            return null;
        }

        @Override
        public Hyperplane<Point2S> getHyperplane() {
            return null;
        }

        @Override
        public boolean isFull() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean isInfinite() {
            return false;
        }

        @Override
        public boolean isFinite() {
            return false;
        }

        @Override
        public double getSize() {
            return 0;
        }

        @Override
        public RegionLocation classify(Point2S point) {
            return null;
        }

        @Override
        public Point2S closest(Point2S point) {
            return null;
        }

        @Override
        public Builder<Point2S> builder() {
            return null;
        }

        @Override
        public SubHyperplane<Point2S> transform(Transform<Point2S> transform) {
            return null;
        }

        @Override
        public List<? extends ConvexSubHyperplane<Point2S>> toConvex() {
            return null;
        }
    }
}
