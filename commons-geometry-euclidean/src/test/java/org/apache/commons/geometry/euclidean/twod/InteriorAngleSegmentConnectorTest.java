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
package org.apache.commons.geometry.euclidean.twod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.InteriorAngleSegmentConnector.Maximize;
import org.apache.commons.geometry.euclidean.twod.InteriorAngleSegmentConnector.Minimize;
import org.junit.Assert;
import org.junit.Test;

public class InteriorAngleSegmentConnectorTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testGetPaths_noSegments() {
        runWithMaxAndMin(connector -> {
            // arrange
            List<Segment> segments = new ArrayList<>();

            // act
            List<Polyline> paths = connector.getConnected(segments);

            // assert
            Assert.assertEquals(0, paths.size());
        });
    }

    @Test
    public void testGetPaths_singleFiniteSegment() {
        runWithMaxAndMin(connector -> {
            // arrange
            List<Segment> segments = Arrays.asList(
                        Segment.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION)
                    );

            // act
            List<Polyline> paths = connector.getConnected(segments);

            // assert
            Assert.assertEquals(1, paths.size());

            assertFinitePath(paths.get(0), Vector2D.ZERO, Vector2D.Unit.PLUS_X);
        });
    }

    @Test
    public void testGetPaths_dualConnectedSegments() {
        runWithMaxAndMin(connector -> {
            // arrange
            List<Segment> segments = Arrays.asList(
                        Segment.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION),
                        Segment.fromPoints(Vector2D.Unit.PLUS_X, Vector2D.ZERO, TEST_PRECISION)
                    );

            // act
            List<Polyline> paths = connector.getConnected(segments);

            // assert
            Assert.assertEquals(1, paths.size());

            Assert.assertTrue(paths.get(0).isClosed());
            assertFinitePath(paths.get(0), Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.ZERO);
        });
    }

    @Test
    public void testGetPaths_singleFiniteSegmentLoop() {
        runWithMaxAndMin(connector -> {
            // arrange
            List<Segment> segments = shuffle(createSquare(Vector2D.ZERO, 1, 1));

            // act
            List<Polyline> paths = connector.getConnected(segments);

            // assert
            Assert.assertEquals(1, paths.size());

            assertFinitePath(paths.get(0),
                    Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, 1),
                    Vector2D.of(0, 1), Vector2D.ZERO);
        });
    }

    @Test
    public void testGetPaths_disjointPaths() {
        runWithMaxAndMin(connector -> {
            // arrange
            List<Segment> segments = new ArrayList<>();
            segments.addAll(createSquare(Vector2D.ZERO, 1, 1));

            Vector2D pt = Vector2D.of(0, 2);
            Segment a = Line.fromPointAndAngle(pt, Geometry.ZERO_PI, TEST_PRECISION).segmentTo(pt);
            Segment b = Line.fromPointAndAngle(pt, Geometry.HALF_PI, TEST_PRECISION).segmentFrom(pt);

            segments.add(a);
            segments.add(b);

            shuffle(segments);

            // act
            List<Polyline> paths = connector.getConnected(segments);

            // assert
            Assert.assertEquals(2, paths.size());

            assertFinitePath(paths.get(0),
                    Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, 1),
                    Vector2D.of(0, 1), Vector2D.ZERO);

            assertInfinitePath(paths.get(1), a, b, pt);
        });
    }

    @Test
    public void testGetPaths_squaresJoinedAtVertex_maximize() {
        // arrange
        Maximize connector = new Maximize();

        List<Segment> segments = new ArrayList<>();
        segments.addAll(createSquare(Vector2D.ZERO, 1, 1));
        segments.addAll(createSquare(Vector2D.of(1, 1), 1, 1));

        shuffle(segments);

        // act
        List<Polyline> paths = connector.getConnected(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, 1),
                Vector2D.of(2, 1), Vector2D.of(2, 2),
                Vector2D.of(1, 2), Vector2D.of(1, 1),
                Vector2D.of(0, 1), Vector2D.ZERO);
    }

    @Test
    public void testGetPaths_mutipleSegmentsAtVertex_maximize() {
        // arrange
        Maximize connector = new Maximize();

        List<Segment> segments = new ArrayList<>();
        segments.add(Segment.fromPoints(Vector2D.ZERO, Vector2D.of(2, 2), TEST_PRECISION));

        segments.add(Segment.fromPoints(Vector2D.of(2, 2), Vector2D.of(2, 4), TEST_PRECISION));
        segments.add(Segment.fromPoints(Vector2D.of(2, 2), Vector2D.of(1, 3), TEST_PRECISION));

        // act
        List<Polyline> paths = connector.getConnected(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.of(2, 2), Vector2D.of(2, 4));

        assertFinitePath(paths.get(1), Vector2D.of(2, 2), Vector2D.of(1, 3));
    }

    @Test
    public void testGetPaths_squaresJoinedAtVertex_minimize() {
        // arrange
        Minimize connector = new Minimize();

        List<Segment> segments = new ArrayList<>();
        segments.addAll(createSquare(Vector2D.ZERO, 1, 1));
        segments.addAll(createSquare(Vector2D.of(1, 1), 1, 1));

        shuffle(segments);

        // act
        List<Polyline> paths = connector.getConnected(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, 1),
                Vector2D.of(0, 1), Vector2D.ZERO);

        assertFinitePath(paths.get(1),
                Vector2D.of(1, 1), Vector2D.of(2, 1), Vector2D.of(2, 2),
                Vector2D.of(1, 2), Vector2D.of(1, 1));
    }

    @Test
    public void testGetPaths_mutipleSegmentsAtVertex_minimize() {
        // arrange
        Minimize connector = new Minimize();

        List<Segment> segments = new ArrayList<>();
        segments.add(Segment.fromPoints(Vector2D.ZERO, Vector2D.of(2, 2), TEST_PRECISION));

        segments.add(Segment.fromPoints(Vector2D.of(2, 2), Vector2D.of(2, 4), TEST_PRECISION));
        segments.add(Segment.fromPoints(Vector2D.of(2, 2), Vector2D.of(1, 3), TEST_PRECISION));

        // act
        List<Polyline> paths = connector.getConnected(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.of(2, 2), Vector2D.of(1, 3));

        assertFinitePath(paths.get(1), Vector2D.of(2, 2), Vector2D.of(2, 4));
    }

    @Test
    public void testConnectMaximized() {
        // arrange
        List<Segment> segments = new ArrayList<>();
        segments.add(Segment.fromPoints(Vector2D.ZERO, Vector2D.of(2, 2), TEST_PRECISION));

        segments.add(Segment.fromPoints(Vector2D.of(2, 2), Vector2D.of(2, 4), TEST_PRECISION));
        segments.add(Segment.fromPoints(Vector2D.of(2, 2), Vector2D.of(1, 3), TEST_PRECISION));

        // act
        List<Polyline> paths = InteriorAngleSegmentConnector.connectMaximized(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.of(2, 2), Vector2D.of(2, 4));

        assertFinitePath(paths.get(1), Vector2D.of(2, 2), Vector2D.of(1, 3));
    }

    @Test
    public void testConnectMinimized() {
        // arrange
        List<Segment> segments = new ArrayList<>();
        segments.add(Segment.fromPoints(Vector2D.ZERO, Vector2D.of(2, 2), TEST_PRECISION));

        segments.add(Segment.fromPoints(Vector2D.of(2, 2), Vector2D.of(2, 4), TEST_PRECISION));
        segments.add(Segment.fromPoints(Vector2D.of(2, 2), Vector2D.of(1, 3), TEST_PRECISION));

        // act
        List<Polyline> paths = InteriorAngleSegmentConnector.connectMinimized(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.of(2, 2), Vector2D.of(1, 3));

        assertFinitePath(paths.get(1), Vector2D.of(2, 2), Vector2D.of(2, 4));
    }

    /**
     * Run the given consumer function twice, once with a Maximize instance and once with
     * a Minimize instance.
     */
    private static void runWithMaxAndMin(Consumer<InteriorAngleSegmentConnector> body) {
        body.accept(new Maximize());
        body.accept(new Minimize());
    }

    private static List<Segment> createSquare(final Vector2D lowerLeft, final double width, final double height) {
        final Vector2D lowerRight = Vector2D.of(lowerLeft.getX() + width, lowerLeft.getY());
        final Vector2D upperRight = Vector2D.of(lowerLeft.getX() + width, lowerLeft.getY() + height);
        final Vector2D upperLeft = Vector2D.of(lowerLeft.getX(), lowerLeft.getY() + height);

        return Arrays.asList(
                    Segment.fromPoints(lowerLeft, lowerRight, TEST_PRECISION),
                    Segment.fromPoints(lowerRight, upperRight, TEST_PRECISION),
                    Segment.fromPoints(upperRight, upperLeft, TEST_PRECISION),
                    Segment.fromPoints(upperLeft, lowerLeft, TEST_PRECISION)
                );
    }

    private static List<Segment> shuffle(final List<Segment> segments) {
        return shuffle(segments, 1);
    }

    private static List<Segment> shuffle(final List<Segment> segments, final int seed) {
        Collections.shuffle(segments, new Random(seed));

        return segments;
    }

    private static void assertInfinitePath(Polyline path, Segment start, Segment end,
            Vector2D ... vertices) {
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());

        Assert.assertEquals(start, path.getStartSegment());
        Assert.assertEquals(end, path.getEndSegment());

        assertPathVertices(path, vertices);
    }

    private static void assertFinitePath(Polyline path, Vector2D ... vertices)
    {
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());

        assertPathVertices(path, vertices);
    }

    private static void assertPathVertices(Polyline path, Vector2D ... vertices) {
        List<Vector2D> expectedVertices = Arrays.asList(vertices);
        List<Vector2D> actualVertices = path.getVertices();

        String msg = "Expected path vertices to equal " + expectedVertices + " but was " + actualVertices;
        Assert.assertEquals(msg, expectedVertices.size(), actualVertices.size());

        for (int i=0; i<expectedVertices.size(); ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(expectedVertices.get(i), actualVertices.get(i), TEST_EPS);
        }
    }
}
