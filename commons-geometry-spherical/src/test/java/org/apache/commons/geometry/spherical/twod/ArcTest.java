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
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.oned.AngularInterval;
import org.junit.Assert;
import org.junit.Test;

public class ArcTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testFromInterval_full() {
        // act
        Arc arc = Arc.fromInterval(
                GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION),
                AngularInterval.full());

        // assert
        Assert.assertTrue(arc.isFull());
        Assert.assertFalse(arc.isEmpty());
        Assert.assertTrue(arc.isFinite());
        Assert.assertFalse(arc.isInfinite());

        Assert.assertNull(arc.getStartPoint());
        Assert.assertNull(arc.getEndPoint());

        Assert.assertEquals(Geometry.TWO_PI, arc.getSize(), TEST_EPS);

        for (double az = 0; az < Geometry.TWO_PI; az += 0.1) {
            checkClassify(arc, RegionLocation.INSIDE, Point2S.of(az, Geometry.HALF_PI));
        }

        checkClassify(arc, RegionLocation.OUTSIDE,
                Point2S.PLUS_K, Point2S.of(0, Geometry.HALF_PI + 0.1),
                Point2S.MINUS_K, Point2S.of(0, Geometry.HALF_PI - 0.1));
    }

    @Test
    public void testFromInterval_partial() {
        // arrange
        Arc arc = Arc.fromInterval(
                GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION),
                AngularInterval.Convex.of(Geometry.HALF_PI, 1.5 * Geometry.PI, TEST_PRECISION));

        // assert
        Assert.assertFalse(arc.isFull());
        Assert.assertFalse(arc.isEmpty());
        Assert.assertTrue(arc.isFinite());
        Assert.assertFalse(arc.isInfinite());

        checkArc(arc, Point2S.PLUS_K, Point2S.MINUS_K);
    }

    @Test
    public void testToConvex() {
        // arrange
        Arc arc = Arc.fromInterval(
                GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.MINUS_I, TEST_PRECISION),
                AngularInterval.Convex.of(Geometry.ZERO_PI, Geometry.PI, TEST_PRECISION));

        // act
        List<Arc> result = arc.toConvex();

        // assert
        Assert.assertEquals(1, result.size());
        Assert.assertSame(arc, result.get(0));
    }

    @Test
    public void testReverse_full() {
        // arrange
        Arc arc = Arc.fromInterval(
                GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.MINUS_I, TEST_PRECISION),
                AngularInterval.full());

        // act
        Arc result = arc.reverse();

        // assert
        checkGreatCircle(result.getCircle(), Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Y);

        Assert.assertTrue(result.isFull());
    }

    @Test
    public void testReverse() {
        // arrange
        Arc arc = Arc.fromInterval(
                GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.MINUS_I, TEST_PRECISION),
                AngularInterval.Convex.of(Geometry.HALF_PI, Geometry.PI, TEST_PRECISION));

        // act
        Arc result = arc.reverse();

        // assert
        checkGreatCircle(result.getCircle(), Vector3D.Unit.MINUS_Z, Vector3D.Unit.PLUS_Y);

        checkArc(result, Point2S.MINUS_J, Point2S.MINUS_I);
    }

    @Test
    public void testTransform() {
        // arrange
        Arc arc = GreatCircle.fromPoints(Point2S.PLUS_K, Point2S.MINUS_I, TEST_PRECISION)
                .arc(Geometry.PI, Geometry.MINUS_HALF_PI);

        Transform2S t = Transform2S.createRotation(Point2S.PLUS_I, Geometry.HALF_PI)
                .reflect(Point2S.of(-0.25 * Geometry.PI,  Geometry.HALF_PI));

        // act
        Arc result = arc.transform(t);

        // assert
        checkArc(result, Point2S.PLUS_I, Point2S.PLUS_J);
    }

    @Test
    public void testSplit_full() {
        // arrange
        Arc arc = GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION).span();
        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(-1, 0, 1), TEST_PRECISION);

        // act
        Split<Arc> split = arc.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        Arc minus = split.getMinus();
        Assert.assertSame(arc.getCircle(), minus.getCircle());
        checkArc(minus, Point2S.MINUS_J, Point2S.PLUS_J);
        checkClassify(minus, RegionLocation.OUTSIDE, Point2S.MINUS_I);
        checkClassify(minus, RegionLocation.INSIDE, Point2S.PLUS_I);

        Arc plus = split.getPlus();
        Assert.assertSame(arc.getCircle(), plus.getCircle());
        checkArc(plus, Point2S.PLUS_J, Point2S.MINUS_J);
        checkClassify(plus, RegionLocation.INSIDE, Point2S.MINUS_I);
        checkClassify(plus, RegionLocation.OUTSIDE, Point2S.PLUS_I);
    }

    @Test
    public void testSplit_both() {
        // arrange
        Arc arc = GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION)
                .arc(Geometry.HALF_PI, Geometry.PI);
        GreatCircle splitter = GreatCircle.fromPole(Vector3D.of(0, 1, 1), TEST_PRECISION);

        // act
        Split<Arc> split = arc.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.BOTH, split.getLocation());

        Arc minus = split.getMinus();
        Assert.assertSame(arc.getCircle(), minus.getCircle());
        checkArc(minus, Point2S.of(1.5 * Geometry.PI, 0.25 * Geometry.PI), Point2S.MINUS_J);

        Arc plus = split.getPlus();
        Assert.assertSame(arc.getCircle(), plus.getCircle());
        checkArc(plus, Point2S.of(0, 0), Point2S.of(1.5 * Geometry.PI, 0.25 * Geometry.PI));
    }

    @Test
    public void testSplit_minus() {
        // arrange
        Arc arc = GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION)
                .arc(Geometry.HALF_PI, Geometry.PI);
        GreatCircle splitter = GreatCircle.fromPole(Vector3D.Unit.from(-1, 0, -1), TEST_PRECISION);

        // act
        Split<Arc> split = arc.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.MINUS, split.getLocation());

        Arc minus = split.getMinus();
        Assert.assertSame(arc, minus);

        Arc plus = split.getPlus();
        Assert.assertNull(plus);
    }

    @Test
    public void testSplit_plus() {
        // arrange
        Arc arc = GreatCircle.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION)
                .arc(Geometry.HALF_PI, Geometry.PI);
        GreatCircle splitter = GreatCircle.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION);

        // act
        Split<Arc> split = arc.split(splitter);

        // assert
        Assert.assertEquals(SplitLocation.PLUS, split.getLocation());

        Arc minus = split.getMinus();
        Assert.assertNull(minus);

        Arc plus = split.getPlus();
        Assert.assertSame(arc, plus);
    }

    @Test
    public void testSplit_parallelAndAntiparallel() {
        // arrange
        Arc arc = GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION).span();

        // act/assert
        Assert.assertEquals(SplitLocation.NEITHER,
                arc.split(GreatCircle.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION)).getLocation());
        Assert.assertEquals(SplitLocation.NEITHER,
                arc.split(GreatCircle.fromPole(Vector3D.Unit.MINUS_Z, TEST_PRECISION)).getLocation());
    }

    @Test
    public void testToString() {
        // arrange
        Arc arc = Arc.fromInterval(
                GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION),
                AngularInterval.Convex.of(1, 2, TEST_PRECISION));

        // act
        String str = arc.toString();

        // assert
        Assert.assertTrue(str.contains("Arc"));
        Assert.assertTrue(str.contains("circle= GreatCircle"));
        Assert.assertTrue(str.contains("interval= Convex"));
    }

    private static void checkClassify(Arc arc, RegionLocation loc, Point2S ... pts) {
        for (Point2S pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, arc.classify(pt));
        }
    }

    private static void checkArc(Arc arc, Point2S start, Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);

        checkClassify(arc, RegionLocation.BOUNDARY, start, end);

        Point2S mid = arc.getCircle().toSpace(arc.getInterval().getMidpoint());

        checkClassify(arc, RegionLocation.INSIDE, mid);
        checkClassify(arc, RegionLocation.OUTSIDE, mid.antipodal());

        Assert.assertEquals(start.distance(end), arc.getSize(), TEST_EPS);
    }

    private static void checkGreatCircle(GreatCircle circle, Vector3D pole, Vector3D x) {
        SphericalTestUtils.assertVectorsEqual(pole, circle.getPole(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(x, circle.getXAxis(), TEST_EPS);
        SphericalTestUtils.assertVectorsEqual(pole.cross(x), circle.getYAxis(), TEST_EPS);
    }
}
