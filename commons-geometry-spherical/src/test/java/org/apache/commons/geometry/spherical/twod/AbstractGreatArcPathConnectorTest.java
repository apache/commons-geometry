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

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class AbstractGreatArcPathConnectorTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final GreatCircle XY_PLANE = GreatCircles.fromPoleAndU(
            Vector3D.Unit.PLUS_Z, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    private static final GreatCircle XZ_PLANE = GreatCircles.fromPoleAndU(
            Vector3D.Unit.PLUS_Y, Vector3D.Unit.PLUS_X, TEST_PRECISION);

    private TestConnector connector = new TestConnector();

    @Test
    public void testConnectAll_emptyCollection() {
        // act
        List<GreatArcPath> paths = connector.connectAll(Collections.emptyList());

        // assert
        Assert.assertEquals(0, paths.size());
    }

    @Test
    public void testConnectAll_singleFullArc() {
        // act
        connector.add(Arrays.asList(XY_PLANE.span()));
        List<GreatArcPath> paths = connector.connectAll();

        // assert
        Assert.assertEquals(1, paths.size());

        GreatArcPath a = paths.get(0);
        Assert.assertEquals(1, a.getArcs().size());
        Assert.assertSame(XY_PLANE, a.getStartArc().getCircle());
    }

    @Test
    public void testConnectAll_twoFullArcs() {
        // act
        connector.add(XZ_PLANE.span());
        List<GreatArcPath> paths = connector.connectAll(Arrays.asList(XY_PLANE.span()));

        // assert
        Assert.assertEquals(2, paths.size());

        GreatArcPath a = paths.get(0);
        Assert.assertEquals(1, a.getArcs().size());
        Assert.assertSame(XY_PLANE, a.getStartArc().getCircle());

        GreatArcPath b = paths.get(1);
        Assert.assertEquals(1, b.getArcs().size());
        Assert.assertSame(XZ_PLANE, b.getStartArc().getCircle());
    }

    @Test
    public void testConnectAll_singleLune() {
        // arrange
        GreatCircle upperBound = GreatCircles.fromPoleAndU(
                Vector3D.of(0, 1, -1), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        connector.add(XY_PLANE.arc(0, PlaneAngleRadians.PI));
        connector.add(upperBound.arc(PlaneAngleRadians.PI, 0));

        // act
        List<GreatArcPath> paths = connector.connectAll();

        // assert
        Assert.assertEquals(1, paths.size());

        GreatArcPath a = paths.get(0);
        Assert.assertEquals(2, a.getArcs().size());
        Assert.assertSame(XY_PLANE, a.getStartArc().getCircle());
        Assert.assertSame(upperBound, a.getEndArc().getCircle());
    }

    @Test
    public void testConnectAll_singleLune_pathsNotOrientedCorrectly() {
        // arrange
        GreatCircle upperBound = GreatCircles.fromPoleAndU(
                Vector3D.of(0, 1, -1), Vector3D.Unit.PLUS_X, TEST_PRECISION);

        connector.add(XY_PLANE.arc(0, PlaneAngleRadians.PI));
        connector.add(upperBound.arc(0, PlaneAngleRadians.PI));

        // act
        List<GreatArcPath> paths = connector.connectAll();

        // assert
        Assert.assertEquals(2, paths.size());

        GreatArcPath a = paths.get(0);
        Assert.assertEquals(1, a.getArcs().size());
        Assert.assertSame(XY_PLANE, a.getStartArc().getCircle());

        GreatArcPath b = paths.get(1);
        Assert.assertEquals(1, b.getArcs().size());
        Assert.assertSame(upperBound, b.getStartArc().getCircle());
    }

    @Test
    public void testConnectAll_largeTriangle() {
        // arrange
        Point2S p1 = Point2S.PLUS_I;
        Point2S p2 = Point2S.PLUS_J;
        Point2S p3 = Point2S.PLUS_K;

        // act
        List<GreatArcPath> paths = connector.connectAll(Arrays.asList(
                    GreatCircles.arcFromPoints(p1, p2, TEST_PRECISION),
                    GreatCircles.arcFromPoints(p2, p3, TEST_PRECISION),
                    GreatCircles.arcFromPoints(p3, p1, TEST_PRECISION)
                ));

        // assert
        Assert.assertEquals(1, paths.size());

        GreatArcPath a = paths.get(0);
        Assert.assertEquals(3, a.getArcs().size());

        assertPathPoints(a, p3, p1, p2, p3);
    }

    @Test
    public void testConnectAll_smallTriangleWithDisconnectedLuneAndArc() {
        // arrange
        Point2S p1 = Point2S.of(0, 0);
        Point2S p2 = Point2S.of(0, 0.1 * PlaneAngleRadians.PI);
        Point2S p3 = Point2S.of(0.1, 0.1 * PlaneAngleRadians.PI);

        GreatArc luneEdge1 = GreatCircles.fromPoints(
                    Point2S.PLUS_J,
                    Point2S.MINUS_I,
                    TEST_PRECISION)
                .arc(0, PlaneAngleRadians.PI);
        GreatArc luneEdge2 = GreatCircles.fromPoints(
                    Point2S.MINUS_J,
                    Point2S.of(PlaneAngleRadians.PI_OVER_TWO, 0.4 * PlaneAngleRadians.PI),
                    TEST_PRECISION)
                .arc(0, PlaneAngleRadians.PI);

        GreatArc separateArc = GreatCircles.arcFromPoints(
                Point2S.of(-PlaneAngleRadians.PI_OVER_TWO, 0.7 * PlaneAngleRadians.PI),
                Point2S.of(-PlaneAngleRadians.PI_OVER_TWO, 0.8 * PlaneAngleRadians.PI),
                TEST_PRECISION);

        // act
        List<GreatArcPath> paths = connector.connectAll(Arrays.asList(
                    luneEdge1,
                    GreatCircles.arcFromPoints(p2, p3, TEST_PRECISION),
                    separateArc,
                    GreatCircles.arcFromPoints(p1, p2, TEST_PRECISION),
                    GreatCircles.arcFromPoints(p3, p1, TEST_PRECISION),
                    luneEdge2
                ));

        // assert
        Assert.assertEquals(3, paths.size());

        GreatArcPath triangle = paths.get(0);
        Assert.assertEquals(3, triangle.getArcs().size());
        assertPathPoints(triangle, p1, p2, p3, p1);

        GreatArcPath lune = paths.get(1);
        Assert.assertEquals(2, lune.getArcs().size());
        Assert.assertSame(luneEdge1, lune.getStartArc());
        Assert.assertSame(luneEdge2, lune.getEndArc());

        GreatArcPath separate = paths.get(2);
        Assert.assertEquals(1, separate.getArcs().size());
        Assert.assertSame(separateArc, separate.getStartArc());
    }

    @Test
    public void testConnectAll_choosesBestPointLikeConnection() {
        // arrange
        DoublePrecisionContext precision = new EpsilonDoublePrecisionContext(1e-1);

        Point2S p1 = Point2S.PLUS_I;
        Point2S p2 = Point2S.of(1, PlaneAngleRadians.PI_OVER_TWO);
        Point2S p3 = Point2S.of(1.001, 0.491 * PlaneAngleRadians.PI);
        Point2S p4 = Point2S.of(1.001, 0.502 * PlaneAngleRadians.PI);

        connector.add(GreatCircles.arcFromPoints(p2, p3, TEST_PRECISION));
        connector.add(GreatCircles.arcFromPoints(p2, p4, TEST_PRECISION));
        connector.add(GreatCircles.arcFromPoints(p1, p2, precision));

        // act
        List<GreatArcPath> paths = connector.connectAll();

        // assert
        Assert.assertEquals(2, paths.size());

        GreatArcPath a = paths.get(0);
        Assert.assertEquals(2, a.getArcs().size());
        assertPathPoints(a, p1, p2, p4);

        GreatArcPath b = paths.get(1);
        Assert.assertEquals(1, b.getArcs().size());
        assertPathPoints(b, p2, p3);
    }

    @Test
    public void testConnect() {
        // arrange
        GreatArc arcA = GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        GreatArc arcB = GreatCircles.arcFromPoints(Point2S.PLUS_J, Point2S.MINUS_I, TEST_PRECISION);
        GreatArc arcC = GreatCircles.arcFromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);

        // act
        connector.connect(Arrays.asList(
                    arcB,
                    arcA
                ));

        connector.connect(Arrays.asList(arcC));

        List<GreatArcPath> paths = connector.connectAll();

        // assert
        Assert.assertEquals(2, paths.size());

        GreatArcPath a = paths.get(0);
        Assert.assertEquals(2, a.getArcs().size());
        assertPathPoints(a, Point2S.PLUS_I, Point2S.PLUS_J, Point2S.MINUS_I);

        GreatArcPath b = paths.get(1);
        Assert.assertEquals(1, b.getArcs().size());
        assertPathPoints(b, Point2S.PLUS_J, Point2S.PLUS_K);
    }

    @Test
    public void testConnectorCanBeReused() {
        // arrange
        GreatArc a = GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        GreatArc b = GreatCircles.arcFromPoints(Point2S.MINUS_I, Point2S.MINUS_J, TEST_PRECISION);

        // act
        List<GreatArcPath> path1 = connector.connectAll(Arrays.asList(a));
        List<GreatArcPath> path2 = connector.connectAll(Arrays.asList(b));

        // assert
        Assert.assertEquals(1, path1.size());
        assertPathPoints(path1.get(0), Point2S.PLUS_I, Point2S.PLUS_J);

        Assert.assertEquals(1, path2.size());
        assertPathPoints(path2.get(0), Point2S.MINUS_I, Point2S.MINUS_J);
    }

    private static void assertPathPoints(GreatArcPath path, Point2S... points) {
        List<Point2S> expectedPoints = Arrays.asList(points);
        List<Point2S> actualPoints = path.getVertices();

        String msg = "Expected path points to equal " + expectedPoints + " but was " + actualPoints;
        Assert.assertEquals(msg, expectedPoints.size(), actualPoints.size());

        for (int i = 0; i < expectedPoints.size(); ++i) {
            SphericalTestUtils.assertPointsEq(expectedPoints.get(i), actualPoints.get(i), TEST_EPS);
        }
    }

    private static class TestConnector extends AbstractGreatArcConnector {

        @Override
        protected ConnectableGreatArc selectConnection(ConnectableGreatArc incoming,
                List<ConnectableGreatArc> outgoing) {

            // just choose the first element
            return outgoing.get(0);
        }
    }
}
