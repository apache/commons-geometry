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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GreatArcPathTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    void testEmpty() {
        // act
        final GreatArcPath path = GreatArcPath.empty();

        // assert
        Assertions.assertTrue(path.isEmpty());
        Assertions.assertFalse(path.isClosed());

        Assertions.assertNull(path.getStartVertex());
        Assertions.assertNull(path.getEndVertex());

        Assertions.assertNull(path.getStartArc());
        Assertions.assertNull(path.getEndArc());

        Assertions.assertEquals(0, path.getArcs().size());
        Assertions.assertEquals(0, path.getVertices().size());
    }

    @Test
    void testFromVertices_boolean_empty() {
        // act
        final GreatArcPath path = GreatArcPath.fromVertices(Collections.emptyList(), true, TEST_PRECISION);

        // assert
        Assertions.assertTrue(path.isEmpty());

        Assertions.assertNull(path.getStartVertex());
        Assertions.assertNull(path.getEndVertex());

        Assertions.assertNull(path.getStartArc());
        Assertions.assertNull(path.getEndArc());

        Assertions.assertEquals(0, path.getArcs().size());
        Assertions.assertEquals(0, path.getVertices().size());
    }

    @Test
    void testFromVertices_boolean_notClosed() {
        // arrange
        final List<Point2S> points = Arrays.asList(
                Point2S.PLUS_I,
                Point2S.PLUS_K,
                Point2S.PLUS_J);

        // act
        final GreatArcPath path = GreatArcPath.fromVertices(points, false, TEST_PRECISION);

        // assert
        Assertions.assertFalse(path.isEmpty());
        Assertions.assertFalse(path.isClosed());

        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_J, path.getEndVertex(), TEST_EPS);

        final List<GreatArc> arcs = path.getArcs();
        Assertions.assertEquals(2, arcs.size());
        assertArc(arcs.get(0), Point2S.PLUS_I, Point2S.PLUS_K);
        assertArc(arcs.get(1), Point2S.PLUS_K, Point2S.PLUS_J);

        assertPoints(points, path.getVertices());
    }

    @Test
    void testFromVertices_boolean_closed() {
        // arrange
        final List<Point2S> points = Arrays.asList(
                Point2S.PLUS_I,
                Point2S.PLUS_K,
                Point2S.PLUS_J);

        // act
        final GreatArcPath path = GreatArcPath.fromVertices(points, true, TEST_PRECISION);

        // assert
        Assertions.assertFalse(path.isEmpty());
        Assertions.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, path.getEndVertex(), TEST_EPS);

        final List<GreatArc> arcs = path.getArcs();
        Assertions.assertEquals(3, arcs.size());
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
    void testFromVertices_boolean_closed_pointsConsideredEqual() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-2);

        final Point2S almostPlusI = Point2S.of(1e-4, Angle.PI_OVER_TWO);

        final List<Point2S> points = Arrays.asList(
                Point2S.PLUS_I,
                Point2S.PLUS_K,
                Point2S.PLUS_J,
                almostPlusI);

        // act
        final GreatArcPath path = GreatArcPath.fromVertices(points, true, precision);

        // assert
        Assertions.assertFalse(path.isEmpty());
        Assertions.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(almostPlusI, path.getEndVertex(), TEST_EPS);

        final List<GreatArc> arcs = path.getArcs();
        Assertions.assertEquals(3, arcs.size());
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
    void testFromVertices() {
        // arrange
        final List<Point2S> points = Arrays.asList(
                Point2S.MINUS_I,
                Point2S.MINUS_J,
                Point2S.PLUS_I);

        // act
        final GreatArcPath path = GreatArcPath.fromVertices(points, TEST_PRECISION);

        // assert
        Assertions.assertFalse(path.isEmpty());
        Assertions.assertFalse(path.isClosed());

        SphericalTestUtils.assertPointsEq(Point2S.MINUS_I, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_I, path.getEndVertex(), TEST_EPS);

        final List<GreatArc> arcs = path.getArcs();
        Assertions.assertEquals(2, arcs.size());
        assertArc(arcs.get(0), Point2S.MINUS_I, Point2S.MINUS_J);
        assertArc(arcs.get(1), Point2S.MINUS_J, Point2S.PLUS_I);

        assertPoints(points, path.getVertices());
    }

    @Test
    void testFromVertexLoop() {
        // arrange
        final List<Point2S> points = Arrays.asList(
                Point2S.MINUS_I,
                Point2S.MINUS_J,
                Point2S.MINUS_K);

        // act
        final GreatArcPath path = GreatArcPath.fromVertexLoop(points, TEST_PRECISION);

        // assert
        Assertions.assertFalse(path.isEmpty());
        Assertions.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(Point2S.MINUS_I, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.MINUS_I, path.getEndVertex(), TEST_EPS);

        final List<GreatArc> arcs = path.getArcs();
        Assertions.assertEquals(3, arcs.size());
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
    void testFromArcs() {
        // arrange
        final Point2S ptA = Point2S.PLUS_I;
        final Point2S ptB = Point2S.of(1, Angle.PI_OVER_TWO);
        final Point2S ptC = Point2S.of(1, Angle.PI_OVER_TWO - 1);
        final Point2S ptD = Point2S.of(2, Angle.PI_OVER_TWO - 1);

        final GreatArc a = GreatCircles.arcFromPoints(ptA, ptB, TEST_PRECISION);
        final GreatArc b = GreatCircles.arcFromPoints(ptB, ptC, TEST_PRECISION);
        final GreatArc c = GreatCircles.arcFromPoints(ptC, ptD, TEST_PRECISION);

        // act
        final GreatArcPath path = GreatArcPath.fromArcs(a, b, c);

        // assert
        Assertions.assertFalse(path.isEmpty());
        Assertions.assertFalse(path.isClosed());

        SphericalTestUtils.assertPointsEq(ptA, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(ptD, path.getEndVertex(), TEST_EPS);

        final List<GreatArc> arcs = path.getArcs();
        Assertions.assertEquals(3, arcs.size());
        assertArc(arcs.get(0), ptA, ptB);
        assertArc(arcs.get(1), ptB, ptC);
        assertArc(arcs.get(2), ptC, ptD);

        assertPoints(Arrays.asList(ptA, ptB, ptC, ptD), path.getVertices());
    }

    @Test
    void testFromArcs_full() {
        // arrange
        final GreatArc fullArc = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION).span();

        // act
        final GreatArcPath path = GreatArcPath.fromArcs(fullArc);

        // assert
        Assertions.assertFalse(path.isEmpty());
        Assertions.assertFalse(path.isClosed());

        Assertions.assertSame(fullArc, path.getStartArc());
        Assertions.assertSame(fullArc, path.getEndArc());

        Assertions.assertNull(path.getStartVertex());
        Assertions.assertNull(path.getEndVertex());

        final List<GreatArc> arcs = path.getArcs();
        Assertions.assertEquals(1, arcs.size());

        Assertions.assertSame(fullArc, arcs.get(0));
    }

    @Test
    void testBoundaryStream() {
        // arrange
        final GreatArc fullArc = GreatCircles.fromPole(Vector3D.Unit.PLUS_X, TEST_PRECISION).span();
        final GreatArcPath path = GreatArcPath.fromArcs(fullArc);

        // act
        final List<GreatArc> arcs = path.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(1, arcs.size());
        Assertions.assertSame(fullArc, arcs.get(0));
    }

    @Test
    void testBoundaryStream_noBoundaries() {
        // arrange
        final GreatArcPath path = GreatArcPath.empty();

        // act
        final List<GreatArc> arcs = path.boundaryStream().collect(Collectors.toList());

        // assert
        Assertions.assertEquals(0, arcs.size());
    }

    @Test
    void testToTree_empty() {
        // act
        final RegionBSPTree2S tree = GreatArcPath.empty().toTree();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertTrue(tree.isEmpty());
    }

    @Test
    void testToTree_halfSpace() {
        // arrange
        final GreatArcPath path = GreatArcPath.builder(TEST_PRECISION)
                .append(Point2S.PLUS_I)
                .append(Point2S.PLUS_J)
                .build();

        // act
        final RegionBSPTree2S tree = path.toTree();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(Angle.TWO_PI, tree.getSize(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(Point2S.PLUS_K, tree.getCentroid(), TEST_EPS);

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE, Point2S.PLUS_K);
        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE, Point2S.MINUS_K);
    }

    @Test
    void testToTree_triangle() {
        // arrange
        final GreatArcPath path = GreatArcPath.builder(TEST_PRECISION)
                .append(Point2S.PLUS_I)
                .append(Point2S.PLUS_J)
                .append(Point2S.PLUS_K)
                .close();

        // act
        final RegionBSPTree2S tree = path.toTree();

        // assert
        Assertions.assertFalse(tree.isFull());
        Assertions.assertFalse(tree.isEmpty());

        Assertions.assertEquals(Angle.PI_OVER_TWO, tree.getSize(), TEST_EPS);

        final Point2S bc = Point2S.from(Point2S.PLUS_I.getVector()
                .add(Point2S.PLUS_J.getVector())
                .add(Point2S.PLUS_K.getVector()));

        SphericalTestUtils.assertPointsEq(bc, tree.getCentroid(), TEST_EPS);

        SphericalTestUtils.checkClassify(tree, RegionLocation.INSIDE, Point2S.of(0.5, 0.5));
        SphericalTestUtils.checkClassify(tree, RegionLocation.OUTSIDE,
                Point2S.MINUS_K, Point2S.MINUS_I, Point2S.MINUS_J);
    }

    @Test
    void testBuilder_append() {
        // arrange
        final Point2S a = Point2S.PLUS_I;
        final Point2S b = Point2S.PLUS_J;
        final Point2S c = Point2S.PLUS_K;
        final Point2S d = Point2S.of(-1, Angle.PI_OVER_TWO);
        final Point2S e = Point2S.of(0, 0.6 * Math.PI);

        final GreatArcPath.Builder builder = GreatArcPath.builder(TEST_PRECISION);

        // act
        final GreatArcPath path = builder.append(GreatCircles.arcFromPoints(a, b, TEST_PRECISION))
            .appendVertices(c, d)
            .append(e)
            .append(GreatCircles.arcFromPoints(e, a, TEST_PRECISION))
            .build();

        // assert
        Assertions.assertFalse(path.isEmpty());
        Assertions.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(a, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(a, path.getEndVertex(), TEST_EPS);

        final List<GreatArc> arcs = path.getArcs();
        Assertions.assertEquals(5, arcs.size());
        assertArc(arcs.get(0), a, b);
        assertArc(arcs.get(1), b, c);
        assertArc(arcs.get(2), c, d);
        assertArc(arcs.get(3), d, e);
        assertArc(arcs.get(4), e, a);

        assertPoints(Arrays.asList(a, b, c, d, e, a), path.getVertices());
    }

    @Test
    void testBuilder_prepend() {
        // arrange
        final Point2S a = Point2S.PLUS_I;
        final Point2S b = Point2S.PLUS_J;
        final Point2S c = Point2S.PLUS_K;
        final Point2S d = Point2S.of(-1, Angle.PI_OVER_TWO);
        final Point2S e = Point2S.of(0, 0.6 * Math.PI);

        final GreatArcPath.Builder builder = GreatArcPath.builder(TEST_PRECISION);

        // act
        final GreatArcPath path = builder.prepend(GreatCircles.arcFromPoints(e, a, TEST_PRECISION))
            .prependPoints(Arrays.asList(c, d))
            .prepend(b)
            .prepend(GreatCircles.arcFromPoints(a, b, TEST_PRECISION))
            .build();

        // assert
        Assertions.assertFalse(path.isEmpty());
        Assertions.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(a, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(a, path.getEndVertex(), TEST_EPS);

        final List<GreatArc> arcs = path.getArcs();
        Assertions.assertEquals(5, arcs.size());
        assertArc(arcs.get(0), a, b);
        assertArc(arcs.get(1), b, c);
        assertArc(arcs.get(2), c, d);
        assertArc(arcs.get(3), d, e);
        assertArc(arcs.get(4), e, a);

        assertPoints(Arrays.asList(a, b, c, d, e, a), path.getVertices());
    }

    @Test
    void testBuilder_appendAndPrepend_points() {
        // arrange
        final Point2S a = Point2S.PLUS_I;
        final Point2S b = Point2S.PLUS_J;
        final Point2S c = Point2S.PLUS_K;
        final Point2S d = Point2S.of(-1, Angle.PI_OVER_TWO);
        final Point2S e = Point2S.of(0, 0.6 * Math.PI);

        final GreatArcPath.Builder builder = GreatArcPath.builder(TEST_PRECISION);

        // act
        final GreatArcPath path = builder.prepend(a)
                .append(b)
                .prepend(e)
                .append(c)
                .prepend(d)
                .build();

        // assert
        Assertions.assertFalse(path.isEmpty());
        Assertions.assertFalse(path.isClosed());

        SphericalTestUtils.assertPointsEq(d, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(c, path.getEndVertex(), TEST_EPS);

        final List<GreatArc> arcs = path.getArcs();
        Assertions.assertEquals(4, arcs.size());
        assertArc(arcs.get(0), d, e);
        assertArc(arcs.get(1), e, a);
        assertArc(arcs.get(2), a, b);
        assertArc(arcs.get(3), b, c);

        assertPoints(Arrays.asList(d, e, a, b, c), path.getVertices());
    }

    @Test
    void testBuilder_appendAndPrepend_mixedArguments() {
        // arrange
        final Point2S a = Point2S.PLUS_I;
        final Point2S b = Point2S.PLUS_J;
        final Point2S c = Point2S.PLUS_K;
        final Point2S d = Point2S.of(-1, Angle.PI_OVER_TWO);
        final Point2S e = Point2S.of(0, 0.6 * Math.PI);

        final GreatArcPath.Builder builder = GreatArcPath.builder(TEST_PRECISION);

        // act
        final GreatArcPath path = builder.append(GreatCircles.arcFromPoints(a, b, TEST_PRECISION))
                .prepend(GreatCircles.arcFromPoints(e, a, TEST_PRECISION))
                .append(c)
                .prepend(d)
                .append(GreatCircles.arcFromPoints(c, d, TEST_PRECISION))
                .build();

        // assert
        Assertions.assertFalse(path.isEmpty());
        Assertions.assertTrue(path.isClosed());

        SphericalTestUtils.assertPointsEq(d, path.getStartVertex(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(d, path.getEndVertex(), TEST_EPS);

        final List<GreatArc> arcs = path.getArcs();
        Assertions.assertEquals(5, arcs.size());
        assertArc(arcs.get(0), d, e);
        assertArc(arcs.get(1), e, a);
        assertArc(arcs.get(2), a, b);
        assertArc(arcs.get(3), b, c);
        assertArc(arcs.get(4), c, d);

        assertPoints(Arrays.asList(d, e, a, b, c, d), path.getVertices());
    }

    @Test
    void testBuilder_points_noPrecisionGiven() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> GreatArcPath.builder(null)
            .append(Point2S.PLUS_I)
            .append(Point2S.PLUS_J), IllegalStateException.class, "Unable to create arc: no point precision specified");

        GeometryTestUtils.assertThrowsWithMessage(() -> GreatArcPath.builder(null)
            .prepend(Point2S.PLUS_I)
            .prepend(Point2S.PLUS_J), IllegalStateException.class, "Unable to create arc: no point precision specified");
    }

    @Test
    void testBuilder_arcsNotConnected() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> GreatArcPath.builder(TEST_PRECISION)
            .append(Point2S.PLUS_I)
            .append(Point2S.PLUS_J)
            .append(GreatCircles.arcFromPoints(Point2S.PLUS_K, Point2S.MINUS_J, TEST_PRECISION)), IllegalStateException.class, Pattern.compile("^Path arcs are not connected.*"));

        GeometryTestUtils.assertThrowsWithMessage(() -> GreatArcPath.builder(TEST_PRECISION)
            .prepend(Point2S.PLUS_I)
            .prepend(Point2S.PLUS_J)
            .prepend(GreatCircles.arcFromPoints(Point2S.PLUS_K, Point2S.MINUS_J, TEST_PRECISION)), IllegalStateException.class, Pattern.compile("^Path arcs are not connected.*"));
    }

    @Test
    void testBuilder_addToFullArc() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> GreatArcPath.builder(TEST_PRECISION)
            .append(GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION).span())
            .append(Point2S.PLUS_J), IllegalStateException.class, Pattern.compile("^Cannot add point .* after full arc.*"));

        GeometryTestUtils.assertThrowsWithMessage(() -> GreatArcPath.builder(TEST_PRECISION)
            .prepend(GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION).span())
            .prepend(Point2S.PLUS_J), IllegalStateException.class, Pattern.compile("^Cannot add point .* before full arc.*"));
    }

    @Test
    void testBuilder_onlySinglePointGiven() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> GreatArcPath.builder(TEST_PRECISION)
            .append(Point2S.PLUS_J)
            .build(), IllegalStateException.class, Pattern.compile("^Unable to create path; only a single point provided.*"));

        GeometryTestUtils.assertThrowsWithMessage(() -> GreatArcPath.builder(TEST_PRECISION)
            .prepend(Point2S.PLUS_J)
            .build(), IllegalStateException.class,  Pattern.compile("^Unable to create path; only a single point provided.*"));
    }

    @Test
    void testBuilder_cannotClose() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> GreatArcPath.builder(TEST_PRECISION)
            .append(GreatCircles.fromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION).span())
            .close(), IllegalStateException.class, "Unable to close path: path is full");
    }

    @Test
    void testToString_empty() {
        // arrange
        final GreatArcPath path = GreatArcPath.empty();

        // act
        final String str = path.toString();

        // assert
        Assertions.assertEquals("GreatArcPath[empty= true]", str);
    }

    @Test
    void testToString_singleFullArc() {
        // arrange
        final GreatArcPath path = GreatArcPath.fromArcs(GreatCircles.fromPole(Vector3D.Unit.PLUS_Z, TEST_PRECISION).span());

        // act
        final String str = path.toString();

        // assert
        GeometryTestUtils.assertContains("GreatArcPath[full= true, circle= GreatCircle[", str);
    }

    @Test
    void testToString_nonFullArcs() {
        // arrange
        final GreatArcPath path = GreatArcPath.builder(TEST_PRECISION)
                .append(Point2S.PLUS_I)
                .append(Point2S.PLUS_J)
                .build();

        // act
        final String str = path.toString();

        // assert
        GeometryTestUtils.assertContains("ArcPath[vertices= [", str);
    }

    private static void assertArc(final GreatArc arc, final Point2S start, final Point2S end) {
        SphericalTestUtils.assertPointsEq(start, arc.getStartPoint(), TEST_EPS);
        SphericalTestUtils.assertPointsEq(end, arc.getEndPoint(), TEST_EPS);
    }

    private static void assertPoints(final Collection<Point2S> expected, final Collection<Point2S> actual) {
        Assertions.assertEquals(expected.size(), actual.size());

        final Iterator<Point2S> expIt = expected.iterator();
        final Iterator<Point2S> actIt = actual.iterator();

        while (expIt.hasNext() && actIt.hasNext()) {
            SphericalTestUtils.assertPointsEq(expIt.next(), actIt.next(), TEST_EPS);
        }
    }
}
