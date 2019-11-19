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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.Region;
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
        Assert.assertEquals(4 * Geometry.PI, area.getSize(), TEST_EPS);
        Assert.assertNull(area.getBarycenter());

        Assert.assertEquals(0, area.getBoundaries().size());

        checkClassify(area, RegionLocation.INSIDE,
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
        Assert.assertEquals(4 * Geometry.PI, area.getSize(), TEST_EPS);
        Assert.assertNull(area.getBarycenter());

        Assert.assertEquals(0, area.getBoundaries().size());

        checkClassify(area, RegionLocation.INSIDE,
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
        Assert.assertEquals(2 * Geometry.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(2 * Geometry.PI, area.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_J, area.getBarycenter(), TEST_EPS);
        checkBarycenter(area);

        Assert.assertEquals(1, area.getBoundaries().size());
        GreatArc arc = area.getBoundaries().get(0);
        Assert.assertTrue(arc.isFull());
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_J, arc.getCircle().getPolePoint(), TEST_EPS);

        checkClassify(area, RegionLocation.INSIDE, Point2S.PLUS_J);

        checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.MINUS_I,
                Point2S.PLUS_K, Point2S.MINUS_K);

        checkClassify(area, RegionLocation.OUTSIDE, Point2S.MINUS_J);
    }

    @Test
    public void testFromBounds_lune_intersectionAtPoles() {
        // arrange
        GreatCircle a = GreatCircle.fromPoints(Point2S.PLUS_K, Point2S.PLUS_I, TEST_PRECISION);
        GreatCircle b = GreatCircle.fromPoints(
                Point2S.of(0.25 * Geometry.PI, Geometry.HALF_PI), Point2S.PLUS_K, TEST_PRECISION);

        // act
        ConvexArea2S area = ConvexArea2S.fromBounds(a, b);

        // assert
        Assert.assertFalse(area.isFull());
        Assert.assertFalse(area.isEmpty());
        Assert.assertEquals(2 * Geometry.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(Geometry.HALF_PI, area.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0.125 * Geometry.PI, Geometry.HALF_PI), area.getBarycenter(), TEST_EPS);
        checkBarycenter(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(2, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_K, Point2S.MINUS_K);
        checkArc(arcs.get(1), Point2S.MINUS_K, Point2S.PLUS_K);

        checkClassify(area, RegionLocation.INSIDE,
                Point2S.of(0.125 * Geometry.PI, 0.1),
                Point2S.of(0.125 * Geometry.PI, Geometry.HALF_PI),
                Point2S.of(0.125 * Geometry.PI, Geometry.PI - 0.1));

        checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.of(0.25 * Geometry.PI, Geometry.HALF_PI),
                Point2S.PLUS_K, Point2S.MINUS_K);

        checkClassify(area, RegionLocation.OUTSIDE,
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
        Assert.assertEquals(2 * Geometry.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(Geometry.PI, area.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.of(0, 0.25 * Geometry.PI), area.getBarycenter(), TEST_EPS);
        checkBarycenter(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(2, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_J, Point2S.MINUS_J);
        checkArc(arcs.get(1), Point2S.MINUS_J, Point2S.PLUS_J);

        checkClassify(area, RegionLocation.INSIDE,
                Point2S.of(0, 0.25 * Geometry.PI),
                Point2S.of(0.25, 0.4 * Geometry.PI),
                Point2S.of(-0.25, 0.4 * Geometry.PI));

        checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.PLUS_K,
                Point2S.PLUS_J, Point2S.MINUS_J);

        checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.MINUS_I, Point2S.MINUS_K,
                Point2S.of(Geometry.PI, 0.25 * Geometry.PI),
                Point2S.of(Geometry.PI, 0.75 * Geometry.PI));
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
        Assert.assertEquals(1.5 * Geometry.PI, area.getBoundarySize(), TEST_EPS);
        Assert.assertEquals(Geometry.HALF_PI, area.getSize(), TEST_EPS);

        checkBarycenter(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(3, arcs.size());
        checkArc(arcs.get(0), Point2S.PLUS_K, Point2S.PLUS_I);
        checkArc(arcs.get(1), Point2S.PLUS_I, Point2S.PLUS_J);
        checkArc(arcs.get(2), Point2S.PLUS_J, Point2S.PLUS_K);

        checkClassify(area, RegionLocation.INSIDE,
                Point2S.of(0.25 * Geometry.PI, 0.25 * Geometry.PI));

        checkClassify(area, RegionLocation.BOUNDARY,
                Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K,
                Point2S.of(0, 0.25 * Geometry.PI), Point2S.of(Geometry.HALF_PI, 0.304 * Geometry.PI),
                Point2S.of(0.25 * Geometry.PI, Geometry.HALF_PI));

        checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.MINUS_I, Point2S.MINUS_J, Point2S.MINUS_K);
    }

    @Test
    public void testFromBounds_triangle_small() {
        // arrange
        double azMin = 1.125 * Geometry.PI;
        double azMax = 1.375 * Geometry.PI;
        double azMid = 0.5 * (azMin + azMax);
        double polarTop = 0.1;
        double polarBottom = 0.25 * Geometry.PI;

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
        double size = Geometry.TWO_PI - a.angle(b) - b.angle(c) - c.angle(a);
        Assert.assertEquals(size, area.getSize(), TEST_EPS);

        checkBarycenter(area);

        List<GreatArc> arcs = sortArcs(area.getBoundaries());
        Assert.assertEquals(3, arcs.size());

        checkArc(arcs.get(0), p3, p1);
        checkArc(arcs.get(1), p1, p2);
        checkArc(arcs.get(2), p2, p3);

        checkClassify(area, RegionLocation.INSIDE, Point2S.of(azMid, 0.11));

        checkClassify(area, RegionLocation.BOUNDARY,
                p1, p2, p3, p1.slerp(p2, 0.2));

        checkClassify(area, RegionLocation.OUTSIDE,
                Point2S.PLUS_I, Point2S.PLUS_J, Point2S.PLUS_K,
                Point2S.MINUS_I, Point2S.MINUS_J, Point2S.MINUS_K);
    }

    private static List<GreatArc> sortArcs(List<GreatArc> arcs) {
        List<GreatArc> result = new ArrayList<>(arcs);

        Collections.sort(result, (a, b) ->
            Point2S.POLAR_AZIMUTH_ASCENDING_ORDER.compare(a.getStartPoint(), b.getStartPoint()));

        return result;
    }

    private static void checkArc(GreatArc arc, Point2S start, Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);
    }

    private static void checkClassify(Region<Point2S> region, RegionLocation loc, Point2S ... pts) {
        for (Point2S pt : pts) {
            Assert.assertEquals("Unexpected location for point " + pt, loc, region.classify(pt));
        }
    }

    private static void checkBarycenter(ConvexArea2S area) {
        Point2S barycenter = area.getBarycenter();
        double size = area.getSize();

        checkClassify(area, RegionLocation.INSIDE, barycenter);

        // make sure that each hyperplane from the vertex through the barycenter
        // splits the region into equally sized sections
        GreatCircle circle = GreatCircle.fromPole(barycenter.getVector(), TEST_PRECISION);
        for (double az = 0; az <= Geometry.TWO_PI; az += 0.2) {
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
