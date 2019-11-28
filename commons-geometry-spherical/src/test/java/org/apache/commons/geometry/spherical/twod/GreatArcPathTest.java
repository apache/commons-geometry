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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class GreatArcPathTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testEmpty() {
        // act
        GreatArcPath path = GreatArcPath.empty();

        // assert
        Assert.assertTrue(path.isEmpty());
        Assert.assertFalse(path.isClosed());

        Assert.assertNull(path.getStartVertex());
        Assert.assertNull(path.getEndVertex());

        Assert.assertNull(path.getStartArc());
        Assert.assertNull(path.getEndArc());

        Assert.assertEquals(0, path.getArcs().size());
        Assert.assertEquals(0, path.getVertices().size());
    }

    @Test
    public void testFromVertices_boolean_empty() {
        // act
        GreatArcPath path = GreatArcPath.fromVertices(Collections.emptyList(), true, TEST_PRECISION);

        // assert
        Assert.assertTrue(path.isEmpty());

        Assert.assertNull(path.getStartVertex());
        Assert.assertNull(path.getEndVertex());

        Assert.assertNull(path.getStartArc());
        Assert.assertNull(path.getEndArc());

        Assert.assertEquals(0, path.getArcs().size());
        Assert.assertEquals(0, path.getVertices().size());
    }

    @Test
    public void testFromVertices_boolean_notClosed() {
        // arrange
        List<Point2S> points = Arrays.asList(
                Point2S.PLUS_I,
                Point2S.PLUS_K,
                Point2S.PLUS_J);

        // act
        GreatArcPath path = GreatArcPath.fromVertices(points, false, TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isClosed());

        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_J, path.getEndVertex(), TEST_EPS);

        List<GreatArc> arcs = path.getArcs();
        Assert.assertEquals(2, arcs.size());
        assertArc(arcs.get(0), Point2S.PLUS_I, Point2S.PLUS_K);
        assertArc(arcs.get(1), Point2S.PLUS_K, Point2S.PLUS_J);

        assertPoints(points, path.getVertices());
    }

    @Test
    public void testFromVertices_boolean_closed() {
        // arrange
        List<Point2S> points = Arrays.asList(
                Point2S.PLUS_I,
                Point2S.PLUS_K,
                Point2S.PLUS_J);

        // act
        GreatArcPath path = GreatArcPath.fromVertices(points, true, TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, path.getEndVertex(), TEST_EPS);

        List<GreatArc> arcs = path.getArcs();
        Assert.assertEquals(3, arcs.size());
        assertArc(arcs.get(0), Point2S.PLUS_I, Point2S.PLUS_K);
        assertArc(arcs.get(1), Point2S.PLUS_K, Point2S.PLUS_J);
        assertArc(arcs.get(2), Point2S.PLUS_J, Point2S.PLUS_I);

        assertPoints(Arrays.asList(
                Point2S.PLUS_I,
                Point2S.PLUS_K,
                Point2S.PLUS_J,
                Point2S.PLUS_I), path.getVertices());
    }

    @Test
    public void testFromVertices_boolean_closed_pointsConsideredEqual() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-2);

        Point2S almostPlusI = Point2S.of(1e-4, PlaneAngleRadians.PI_OVER_TWO);

        List<Point2S> points = Arrays.asList(
                Point2S.PLUS_I,
                Point2S.PLUS_K,
                Point2S.PLUS_J,
                almostPlusI);

        // act
        GreatArcPath path = GreatArcPath.fromVertices(points, true, precision);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(almostPlusI, path.getEndVertex(), TEST_EPS);

        List<GreatArc> arcs = path.getArcs();
        Assert.assertEquals(3, arcs.size());
        assertArc(arcs.get(0), Point2S.PLUS_I, Point2S.PLUS_K);
        assertArc(arcs.get(1), Point2S.PLUS_K, Point2S.PLUS_J);
        assertArc(arcs.get(2), Point2S.PLUS_J, almostPlusI);

        assertPoints(Arrays.asList(
                Point2S.PLUS_I,
                Point2S.PLUS_K,
                Point2S.PLUS_J,
                almostPlusI), path.getVertices());
    }

    @Test
    public void testFromVertices() {
        // arrange
        List<Point2S> points = Arrays.asList(
                Point2S.MINUS_I,
                Point2S.MINUS_J,
                Point2S.PLUS_I);

        // act
        GreatArcPath path = GreatArcPath.fromVertices(points, TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isClosed());

        SphericalTestUtils.assertPointsEq(Point2S.MINUS_I, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, path.getEndVertex(), TEST_EPS);

        List<GreatArc> arcs = path.getArcs();
        Assert.assertEquals(2, arcs.size());
        assertArc(arcs.get(0), Point2S.MINUS_I, Point2S.MINUS_J);
        assertArc(arcs.get(1), Point2S.MINUS_J, Point2S.PLUS_I);

        assertPoints(points, path.getVertices());
    }

    @Test
    public void testFromVertexLoop() {
        // arrange
        List<Point2S> points = Arrays.asList(
                Point2S.MINUS_I,
                Point2S.MINUS_J,
                Point2S.MINUS_K);

        // act
        GreatArcPath path = GreatArcPath.fromVertexLoop(points, TEST_PRECISION);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(Point2S.MINUS_I, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.MINUS_I, path.getEndVertex(), TEST_EPS);

        List<GreatArc> arcs = path.getArcs();
        Assert.assertEquals(3, arcs.size());
        assertArc(arcs.get(0), Point2S.MINUS_I, Point2S.MINUS_J);
        assertArc(arcs.get(1), Point2S.MINUS_J, Point2S.MINUS_K);
        assertArc(arcs.get(2), Point2S.MINUS_K, Point2S.MINUS_I);

        assertPoints(Arrays.asList(
                Point2S.MINUS_I,
                Point2S.MINUS_J,
                Point2S.MINUS_K,
                Point2S.MINUS_I), path.getVertices());
    }

    @Test
    public void testFromArcs() {
        // arrange
        Point2S ptA = Point2S.PLUS_I;
        Point2S ptB = Point2S.of(1, PlaneAngleRadians.PI_OVER_TWO);
        Point2S ptC = Point2S.of(1, PlaneAngleRadians.PI_OVER_TWO - 1);
        Point2S ptD = Point2S.of(2, PlaneAngleRadians.PI_OVER_TWO - 1);

        GreatArc a = GreatArc.fromPoints(ptA, ptB, TEST_PRECISION);
        GreatArc b = GreatArc.fromPoints(ptB, ptC, TEST_PRECISION);
        GreatArc c = GreatArc.fromPoints(ptC, ptD, TEST_PRECISION);

        // act
        GreatArcPath path = GreatArcPath.fromArcs(a, b, c);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isClosed());

        SphericalTestUtils.assertPointsEq(ptA, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(ptD, path.getEndVertex(), TEST_EPS);

        List<GreatArc> arcs = path.getArcs();
        Assert.assertEquals(3, arcs.size());
        assertArc(arcs.get(0), ptA, ptB);
        assertArc(arcs.get(1), ptB, ptC);
        assertArc(arcs.get(2), ptC, ptD);

        assertPoints(Arrays.asList(ptA, ptB, ptC, ptD), path.getVertices());
    }

    @Test
    public void testFromArcs_full() {
        // arrange
        GreatArc fullArc = GreatCircle.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        GreatArcPath path = GreatArcPath.fromArcs(fullArc);

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isClosed());

        Assert.assertSame(fullArc, path.getStartArc());
        Assert.assertSame(fullArc, path.getEndArc());

        Assert.assertNull(path.getStartVertex());
        Assert.assertNull(path.getEndVertex());

        List<GreatArc> arcs = path.getArcs();
        Assert.assertEquals(1, arcs.size());

        Assert.assertSame(fullArc, arcs.get(0));
    }

    @Test
    public void testIterator() {
        // arrange
        GreatArc a = GreatArc.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        GreatArc b = GreatArc.fromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);

        GreatArcPath path = GreatArcPath.fromArcs(Arrays.asList(a, b));

        List<GreatArc> arcs = new ArrayList<>();

        // act
        for (GreatArc arc : path) {
            arcs.add(arc);
        }

        // assert
        Assert.assertEquals(arcs, Arrays.asList(a, b));
    }

    @Test
    public void testToTree_empty() {
        // act
        RegionBSPTree2S tree = GreatArcPath.empty().toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertTrue(tree.isEmpty());
    }

    @Test
    public void testToTree_halfSpace() {
        // arrange
        GreatArcPath path = GreatArcPath.builder(TEST_PRECISION)
                .append(Point2S.PLUS_I)
                .append(Point2S.PLUS_J)
                .build();

        // act
        RegionBSPTree2S tree = path.toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(PlaneAngleRadians.TWO_PI, tree.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_K, tree.getBarycenter(), TEST_EPS);

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE, Point2S.PLUS_K);
        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE, Point2S.MINUS_K);
    }

    @Test
    public void testToTree_triangle() {
        // arrange
        GreatArcPath path = GreatArcPath.builder(TEST_PRECISION)
                .append(Point2S.PLUS_I)
                .append(Point2S.PLUS_J)
                .append(Point2S.PLUS_K)
                .close();

        // act
        RegionBSPTree2S tree = path.toTree();

        // assert
        Assert.assertFalse(tree.isFull());
        Assert.assertFalse(tree.isEmpty());

        Assert.assertEquals(PlaneAngleRadians.PI_OVER_TWO, tree.getSize(), TEST_EPS);

        Point2S bc = Point2S.from(Point2S.PLUS_I.getVector()
                .add(Point2S.PLUS_J.getVector())
                .add(Point2S.PLUS_K.getVector()));

        SphericalTestUtils.assertPointsEq(bc, tree.getBarycenter(), TEST_EPS);

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE, Point2S.of(0.5, 0.5));
        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                Point2S.MINUS_K, Point2S.MINUS_I, Point2S.MINUS_J);
    }

    @Test
    public void testBuilder_append() {
        // arrange
        Point2S a = Point2S.PLUS_I;
        Point2S b = Point2S.PLUS_J;
        Point2S c = Point2S.PLUS_K;
        Point2S d = Point2S.of(-1, PlaneAngleRadians.PI_OVER_TWO);
        Point2S e = Point2S.of(0, 0.6 * PlaneAngleRadians.PI);

        GreatArcPath.Builder builder = GreatArcPath.builder(TEST_PRECISION);

        // act
        GreatArcPath path = builder.append(GreatArc.fromPoints(a, b, TEST_PRECISION))
            .appendVertices(c, d)
            .append(e)
            .append(GreatArc.fromPoints(e, a, TEST_PRECISION))
            .build();

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(a, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(a, path.getEndVertex(), TEST_EPS);

        List<GreatArc> arcs = path.getArcs();
        Assert.assertEquals(5, arcs.size());
        assertArc(arcs.get(0), a, b);
        assertArc(arcs.get(1), b, c);
        assertArc(arcs.get(2), c, d);
        assertArc(arcs.get(3), d, e);
        assertArc(arcs.get(4), e, a);

        assertPoints(Arrays.asList(a, b, c, d, e, a), path.getVertices());
    }

    @Test
    public void testBuilder_prepend() {
        // arrange
        Point2S a = Point2S.PLUS_I;
        Point2S b = Point2S.PLUS_J;
        Point2S c = Point2S.PLUS_K;
        Point2S d = Point2S.of(-1, PlaneAngleRadians.PI_OVER_TWO);
        Point2S e = Point2S.of(0, 0.6 * PlaneAngleRadians.PI);

        GreatArcPath.Builder builder = GreatArcPath.builder(TEST_PRECISION);

        // act
        GreatArcPath path = builder.prepend(GreatArc.fromPoints(e, a, TEST_PRECISION))
            .prependPoints(Arrays.asList(c, d))
            .prepend(b)
            .prepend(GreatArc.fromPoints(a, b, TEST_PRECISION))
            .build();

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(a, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(a, path.getEndVertex(), TEST_EPS);

        List<GreatArc> arcs = path.getArcs();
        Assert.assertEquals(5, arcs.size());
        assertArc(arcs.get(0), a, b);
        assertArc(arcs.get(1), b, c);
        assertArc(arcs.get(2), c, d);
        assertArc(arcs.get(3), d, e);
        assertArc(arcs.get(4), e, a);

        assertPoints(Arrays.asList(a, b, c, d, e, a), path.getVertices());
    }

    @Test
    public void testBuilder_appendAndPrepend_points() {
        // arrange
        Point2S a = Point2S.PLUS_I;
        Point2S b = Point2S.PLUS_J;
        Point2S c = Point2S.PLUS_K;
        Point2S d = Point2S.of(-1, PlaneAngleRadians.PI_OVER_TWO);
        Point2S e = Point2S.of(0, 0.6 * PlaneAngleRadians.PI);

        GreatArcPath.Builder builder = GreatArcPath.builder(TEST_PRECISION);

        // act
        GreatArcPath path = builder.prepend(a)
                .append(b)
                .prepend(e)
                .append(c)
                .prepend(d)
                .build();

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertFalse(path.isClosed());

        SphericalTestUtils.assertPointsEq(d, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(c, path.getEndVertex(), TEST_EPS);

        List<GreatArc> arcs = path.getArcs();
        Assert.assertEquals(4, arcs.size());
        assertArc(arcs.get(0), d, e);
        assertArc(arcs.get(1), e, a);
        assertArc(arcs.get(2), a, b);
        assertArc(arcs.get(3), b, c);

        assertPoints(Arrays.asList(d, e, a, b, c), path.getVertices());
    }

    @Test
    public void testBuilder_appendAndPrepend_mixedArguments() {
        // arrange
        Point2S a = Point2S.PLUS_I;
        Point2S b = Point2S.PLUS_J;
        Point2S c = Point2S.PLUS_K;
        Point2S d = Point2S.of(-1, PlaneAngleRadians.PI_OVER_TWO);
        Point2S e = Point2S.of(0, 0.6 * PlaneAngleRadians.PI);

        GreatArcPath.Builder builder = GreatArcPath.builder(TEST_PRECISION);

        // act
        GreatArcPath path = builder.append(GreatArc.fromPoints(a, b, TEST_PRECISION))
                .prepend(GreatArc.fromPoints(e, a, TEST_PRECISION))
                .append(c)
                .prepend(d)
                .append(GreatArc.fromPoints(c, d, TEST_PRECISION))
                .build();

        // assert
        Assert.assertFalse(path.isEmpty());
        Assert.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(d, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(d, path.getEndVertex(), TEST_EPS);

        List<GreatArc> arcs = path.getArcs();
        Assert.assertEquals(5, arcs.size());
        assertArc(arcs.get(0), d, e);
        assertArc(arcs.get(1), e, a);
        assertArc(arcs.get(2), a, b);
        assertArc(arcs.get(3), b, c);
        assertArc(arcs.get(4), c, d);

        assertPoints(Arrays.asList(d, e, a, b, c, d), path.getVertices());
    }

    @Test
    public void testBuilder_points_noPrecisionGiven() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            GreatArcPath.builder(null)
                .append(Point2S.PLUS_I)
                .append(Point2S.PLUS_J);
        }, IllegalStateException.class, "Unable to create arc: no point precision specified");

        GeometryTestUtils.assertThrows(() -> {
            GreatArcPath.builder(null)
                .prepend(Point2S.PLUS_I)
                .prepend(Point2S.PLUS_J);
        }, IllegalStateException.class, "Unable to create arc: no point precision specified");
    }

    @Test
    public void testBuilder_arcsNotConnected() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            GreatArcPath.builder(TEST_PRECISION)
                .append(Point2S.PLUS_I)
                .append(Point2S.PLUS_J)
                .append(GreatArc.fromPoints(Point2S.PLUS_K, Point2S.MINUS_J, TEST_PRECISION));
        }, IllegalStateException.class, Pattern.compile("^Path arcs are not connected.*"));

        GeometryTestUtils.assertThrows(() -> {
            GreatArcPath.builder(TEST_PRECISION)
                .prepend(Point2S.PLUS_I)
                .prepend(Point2S.PLUS_J)
                .prepend(GreatArc.fromPoints(Point2S.PLUS_K, Point2S.MINUS_J, TEST_PRECISION));
        }, IllegalStateException.class, Pattern.compile("^Path arcs are not connected.*"));
    }

    @Test
    public void testBuilder_addToFullArc() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            GreatArcPath.builder(TEST_PRECISION)
                .append(GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION).span())
                .append(Point2S.PLUS_J);
        }, IllegalStateException.class, Pattern.compile("^Cannot add point .* after full arc.*"));

        GeometryTestUtils.assertThrows(() -> {
            GreatArcPath.builder(TEST_PRECISION)
                .prepend(GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION).span())
                .prepend(Point2S.PLUS_J);
        }, IllegalStateException.class, Pattern.compile("^Cannot add point .* before full arc.*"));
    }

    @Test
    public void testBuilder_onlySinglePointGiven() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            GreatArcPath.builder(TEST_PRECISION)
                .append(Point2S.PLUS_J)
                .build();
        }, IllegalStateException.class, Pattern.compile("^Unable to create path; only a single point provided.*"));

        GeometryTestUtils.assertThrows(() -> {
            GreatArcPath.builder(TEST_PRECISION)
                .prepend(Point2S.PLUS_J)
                .build();
        }, IllegalStateException.class,  Pattern.compile("^Unable to create path; only a single point provided.*"));
    }

    @Test
    public void testBuilder_cannotClose() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            GreatArcPath.builder(TEST_PRECISION)
                .append(GreatCircle.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION).span())
                .close();
        }, IllegalStateException.class, "Unable to close path: path is full");
    }

    @Test
    public void testToString_empty() {
        // arrange
        GreatArcPath path = GreatArcPath.empty();

        // act
        String str = path.toString();

        // assert
        Assert.assertEquals("GreatArcPath[empty= true]", str);
    }

    @Test
    public void testToString_singleFullArc() {
        // arrange
        GreatArcPath path = GreatArcPath.fromArcs(GreatCircle.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION).span());

        // act
        String str = path.toString();

        // assert
        GeometryTestUtils.assertContains("GreatArcPath[full= true, circle= GreatCircle[", str);
    }

    @Test
    public void testToString_nonFullArcs() {
        // arrange
        GreatArcPath path = GreatArcPath.builder(TEST_PRECISION)
                .append(Point2S.PLUS_I)
                .append(Point2S.PLUS_J)
                .build();

        // act
        String str = path.toString();

        // assert
        GeometryTestUtils.assertContains("ArcPath[vertices= [", str);
    }

    private static void assertArc(GreatArc arc, Point2S start, Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);
    }

    private static void assertPoints(Collection<Point2S> expected, Collection<Point2S> actual) {
        Assert.assertEquals(expected.size(), actual.size());

        Iterator<Point2S> expIt = expected.iterator();
        Iterator<Point2S> actIt = actual.iterator();

        while (expIt.hasNext() && actIt.hasNext()) {
            SphericalTestUtils.assertPointsEq(expIt.next(), actIt.next(), TEST_EPS);
        }
    }
}
