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
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.twod.AbstractGreatArcConnector.ConnectableGreatArc;
import org.apache.commons.numbers.angle.Angle;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AbstractGreatArcPathConnectorTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private static final GreatCircle XY_PLANE = GreatCircles.fromPoleAndU(
            Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    private static final GreatCircle XZ_PLANE = GreatCircles.fromPoleAndU(
            Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    private final TestConnector connector = new TestConnector();

    @Test
    void testConnectAll_emptyCollection() {
        // act
        final List<GreatArcPath> paths = connector.connectAll(Collections.emptyList());

        // assert
        Assertions.assertEquals(0, paths.size());
    }

    @Test
    void testConnectAll_singleFullArc() {
        // act
        connector.add(Collections.singletonList(XY_PLANE.span()));
        final List<GreatArcPath> paths = connector.connectAll();

        // assert
        Assertions.assertEquals(1, paths.size());

        final GreatArcPath a = paths.get(0);
        Assertions.assertEquals(1, a.getArcs().size());
        Assertions.assertSame(XY_PLANE, a.getStartArc().getCircle());
    }

    @Test
    void testConnectAll_twoFullArcs() {
        // act
        connector.add(XZ_PLANE.span());
        final List<GreatArcPath> paths = connector.connectAll(Collections.singletonList(XY_PLANE.span()));

        // assert
        Assertions.assertEquals(2, paths.size());

        final GreatArcPath a = paths.get(0);
        Assertions.assertEquals(1, a.getArcs().size());
        Assertions.assertSame(XY_PLANE, a.getStartArc().getCircle());

        final GreatArcPath b = paths.get(1);
        Assertions.assertEquals(1, b.getArcs().size());
        Assertions.assertSame(XZ_PLANE, b.getStartArc().getCircle());
    }

    @Test
    void testConnectAll_singleLune() {
        // arrange
        final GreatCircle upperBound = GreatCircles.fromPoleAndU(
                Vector3D.of(0, 1, -1), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        connector.add(XY_PLANE.arc(0, Math.PI));
        connector.add(upperBound.arc(Math.PI, 0));

        // act
        final List<GreatArcPath> paths = connector.connectAll();

        // assert
        Assertions.assertEquals(1, paths.size());

        final GreatArcPath a = paths.get(0);
        Assertions.assertEquals(2, a.getArcs().size());
        Assertions.assertSame(XY_PLANE, a.getStartArc().getCircle());
        Assertions.assertSame(upperBound, a.getEndArc().getCircle());
    }

    @Test
    void testConnectAll_singleLune_pathsNotOrientedCorrectly() {
        // arrange
        final GreatCircle upperBound = GreatCircles.fromPoleAndU(
                Vector3D.of(0, 1, -1), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        connector.add(XY_PLANE.arc(0, Math.PI));
        connector.add(upperBound.arc(0, Math.PI));

        // act
        final List<GreatArcPath> paths = connector.connectAll();

        // assert
        Assertions.assertEquals(2, paths.size());

        final GreatArcPath a = paths.get(0);
        Assertions.assertEquals(1, a.getArcs().size());
        Assertions.assertSame(XY_PLANE, a.getStartArc().getCircle());

        final GreatArcPath b = paths.get(1);
        Assertions.assertEquals(1, b.getArcs().size());
        Assertions.assertSame(upperBound, b.getStartArc().getCircle());
    }

    @Test
    void testConnectAll_largeTriangle() {
        // arrange
        final Point2S p1 = Point2S.PLUS_I;
        final Point2S p2 = Point2S.PLUS_J;
        final Point2S p3 = Point2S.PLUS_K;

        // act
        final List<GreatArcPath> paths = connector.connectAll(Arrays.asList(
                    GreatCircles.arcFromPoints(p1, p2, TEST_PRECISION),
                    GreatCircles.arcFromPoints(p2, p3, TEST_PRECISION),
                    GreatCircles.arcFromPoints(p3, p1, TEST_PRECISION)
                ));

        // assert
        Assertions.assertEquals(1, paths.size());

        final GreatArcPath a = paths.get(0);
        Assertions.assertEquals(3, a.getArcs().size());

        assertPathPoints(a, p3, p1, p2, p3);
    }

    @Test
    void testConnectAll_smallTriangleWithDisconnectedLuneAndArc() {
        // arrange
        final Point2S p1 = Point2S.of(0, 0);
        final Point2S p2 = Point2S.of(0, 0.1 * Math.PI);
        final Point2S p3 = Point2S.of(0.1, 0.1 * Math.PI);

        final GreatArc luneEdge1 = GreatCircles.fromPoints(
                    Point2S.PLUS_J,
                    Point2S.MINUS_I,
                    TEST_PRECISION)
                .arc(0, Math.PI);
        final GreatArc luneEdge2 = GreatCircles.fromPoints(
                    Point2S.MINUS_J,
                    Point2S.of(Angle.PI_OVER_TWO, 0.4 * Math.PI),
                    TEST_PRECISION)
                .arc(0, Math.PI);

        final GreatArc separateArc = GreatCircles.arcFromPoints(
                Point2S.of(-Angle.PI_OVER_TWO, 0.7 * Math.PI),
                Point2S.of(-Angle.PI_OVER_TWO, 0.8 * Math.PI),
                TEST_PRECISION);

        // act
        final List<GreatArcPath> paths = connector.connectAll(Arrays.asList(
                    luneEdge1,
                    GreatCircles.arcFromPoints(p2, p3, TEST_PRECISION),
                    separateArc,
                    GreatCircles.arcFromPoints(p1, p2, TEST_PRECISION),
                    GreatCircles.arcFromPoints(p3, p1, TEST_PRECISION),
                    luneEdge2
                ));

        // assert
        Assertions.assertEquals(3, paths.size());

        final GreatArcPath triangle = paths.get(0);
        Assertions.assertEquals(3, triangle.getArcs().size());
        assertPathPoints(triangle, p1, p2, p3, p1);

        final GreatArcPath lune = paths.get(1);
        Assertions.assertEquals(2, lune.getArcs().size());
        Assertions.assertSame(luneEdge1, lune.getStartArc());
        Assertions.assertSame(luneEdge2, lune.getEndArc());

        final GreatArcPath separate = paths.get(2);
        Assertions.assertEquals(1, separate.getArcs().size());
        Assertions.assertSame(separateArc, separate.getStartArc());
    }

    @Test
    void testConnectAll_choosesBestPointLikeConnection() {
        // arrange
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-1);

        final Point2S p1 = Point2S.PLUS_I;
        final Point2S p2 = Point2S.of(1, Angle.PI_OVER_TWO);
        final Point2S p3 = Point2S.of(1.001, 0.491 * Math.PI);
        final Point2S p4 = Point2S.of(1.001, 0.502 * Math.PI);

        connector.add(GreatCircles.arcFromPoints(p2, p3, TEST_PRECISION));
        connector.add(GreatCircles.arcFromPoints(p2, p4, TEST_PRECISION));
        connector.add(GreatCircles.arcFromPoints(p1, p2, precision));

        // act
        final List<GreatArcPath> paths = connector.connectAll();

        // assert
        Assertions.assertEquals(2, paths.size());

        final GreatArcPath a = paths.get(0);
        Assertions.assertEquals(2, a.getArcs().size());
        assertPathPoints(a, p1, p2, p4);

        final GreatArcPath b = paths.get(1);
        Assertions.assertEquals(1, b.getArcs().size());
        assertPathPoints(b, p2, p3);
    }

    @Test
    void testConnect() {
        // arrange
        final GreatArc arcA = GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatArc arcB = GreatCircles.arcFromPoints(Point2S.PLUS_J, Point2S.MINUS_I, TEST_PRECISION);
        final GreatArc arcC = GreatCircles.arcFromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);

        // act
        connector.connect(Arrays.asList(
                    arcB,
                    arcA
                ));

        connector.connect(Collections.singletonList(arcC));

        final List<GreatArcPath> paths = connector.connectAll();

        // assert
        Assertions.assertEquals(2, paths.size());

        final GreatArcPath a = paths.get(0);
        Assertions.assertEquals(2, a.getArcs().size());
        assertPathPoints(a, Point2S.PLUS_I, Point2S.PLUS_J, Point2S.MINUS_I);

        final GreatArcPath b = paths.get(1);
        Assertions.assertEquals(1, b.getArcs().size());
        assertPathPoints(b, Point2S.PLUS_J, Point2S.PLUS_K);
    }

    @Test
    void testConnectableSegment_hashCode() {
        // arrange
        final GreatArc arcA = GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatArc arcB = GreatCircles.arcFromPoints(Point2S.PLUS_J, Point2S.MINUS_I, TEST_PRECISION);

        final ConnectableGreatArc a = new ConnectableGreatArc(arcA);

        // act
        final int hash = a.hashCode();

        // assert
        Assertions.assertEquals(hash, a.hashCode());

        Assertions.assertNotEquals(hash, new ConnectableGreatArc(arcB).hashCode());
        Assertions.assertNotEquals(hash, new ConnectableGreatArc(Point2S.MINUS_I).hashCode());

        Assertions.assertEquals(hash, new ConnectableGreatArc(arcA).hashCode());
    }

    @Test
    void testConnectableSegment_equals() {
        // arrange
        final GreatArc arcA = GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatArc arcB = GreatCircles.arcFromPoints(Point2S.PLUS_J, Point2S.MINUS_I, TEST_PRECISION);

        final ConnectableGreatArc a = new ConnectableGreatArc(arcA);

        // act/assert
        Assertions.assertEquals(a, a);

        Assertions.assertFalse(a.equals(null));
        Assertions.assertFalse(a.equals(new Object()));

        Assertions.assertNotEquals(a, new ConnectableGreatArc(arcB));
        Assertions.assertNotEquals(a, new ConnectableGreatArc(Point2S.MINUS_I));

        Assertions.assertEquals(a, new ConnectableGreatArc(arcA));
    }

    @Test
    void testConnectorCanBeReused() {
        // arrange
        final GreatArc a = GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatArc b = GreatCircles.arcFromPoints(Point2S.MINUS_I, Point2S.MINUS_J, TEST_PRECISION);

        // act
        final List<GreatArcPath> path1 = connector.connectAll(Collections.singletonList(a));
        final List<GreatArcPath> path2 = connector.connectAll(Collections.singletonList(b));

        // assert
        Assertions.assertEquals(1, path1.size());
        assertPathPoints(path1.get(0), Point2S.PLUS_I, Point2S.PLUS_J);

        Assertions.assertEquals(1, path2.size());
        assertPathPoints(path2.get(0), Point2S.MINUS_I, Point2S.MINUS_J);
    }

    private static void assertPathPoints(final GreatArcPath path, final Point2S... points) {
        final List<Point2S> expectedPoints = Arrays.asList(points);
        final List<Point2S> actualPoints = path.getVertices();

        final String msg = "Expected path points to equal " + expectedPoints + " but was " + actualPoints;
        Assertions.assertEquals(expectedPoints.size(), actualPoints.size(), msg);

        for (int i = 0; i < expectedPoints.size(); ++i) {
            SphericalTestUtils.assertPointsEq(expectedPoints.get(i), actualPoints.get(i), TEST_EPS);
        }
    }

    private static class TestConnector extends AbstractGreatArcConnector {

        @Override
        protected ConnectableGreatArc selectConnection(final ConnectableGreatArc incoming,
                                                       final List<ConnectableGreatArc> outgoing) {

            // just choose the first element
            return outgoing.get(0);
        }
    }
}
