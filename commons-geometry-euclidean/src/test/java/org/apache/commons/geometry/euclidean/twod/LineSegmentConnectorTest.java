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
import java.util.stream.Collectors;

import org.junit.Test;
import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;

public class LineSegmentConnectorTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testMaximizeAngles_noSegments() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.maximizeAngles();
        List<LineSegment> segments = new ArrayList<>();

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(0, paths.size());
    }

    @Test
    public void testMaximizeAngles_singleFiniteSegment() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.maximizeAngles();
        List<LineSegment> segments = Arrays.asList(
                    LineSegment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION)
                );

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0), Vector2D.ZERO, Vector2D.PLUS_X);
    }

    @Test
    public void testMaximizeAngles_singleFiniteSegmentLoop() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.maximizeAngles();
        List<LineSegment> segments = shuffle(createSquare(Vector2D.ZERO, 1, 1));

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.PLUS_X, Vector2D.of(1, 1),
                Vector2D.of(0, 1), Vector2D.ZERO);
    }

    @Test
    public void testMaximizeAngles_disjointPaths() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.maximizeAngles();

        List<LineSegment> segments = new ArrayList<>();
        segments.addAll(createSquare(Vector2D.ZERO, 1, 1));

        Vector2D pt = Vector2D.of(0, 2);
        LineSegment a = Line.fromPointAndAngle(pt, Geometry.ZERO_PI, TEST_PRECISION).segmentTo(pt);
        LineSegment b = Line.fromPointAndAngle(pt, Geometry.HALF_PI, TEST_PRECISION).segmentFrom(pt);

        segments.add(a);
        segments.add(b);

        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.PLUS_X, Vector2D.of(1, 1),
                Vector2D.of(0, 1), Vector2D.ZERO);

        assertInfinitePath(paths.get(1), a, b, pt);
    }

    @Test
    public void testMaximizeAngles_squaresJoinedAtVertex() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.maximizeAngles();

        List<LineSegment> segments = new ArrayList<>();
        segments.addAll(createSquare(Vector2D.ZERO, 1, 1));
        segments.addAll(createSquare(Vector2D.of(1, 1), 1, 1));

        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.PLUS_X, Vector2D.of(1, 1),
                Vector2D.of(0, 1), Vector2D.ZERO);

        assertFinitePath(paths.get(1),
                Vector2D.of(1, 1), Vector2D.of(2, 1), Vector2D.of(2, 2),
                Vector2D.of(1, 2), Vector2D.of(1, 1));
    }

    @Test
    public void testMaximizeAngles_mutipleSegmentsAtVertex() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.maximizeAngles();

        List<LineSegment> segments = new ArrayList<>();
        segments.add(LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(2, 2), TEST_PRECISION));

        segments.add(LineSegment.fromPoints(Vector2D.of(0, 2), Vector2D.ZERO, TEST_PRECISION));
        segments.add(LineSegment.fromPoints(Vector2D.of(1, 2), Vector2D.ZERO, TEST_PRECISION));

        segments.add(LineSegment.fromPoints(Vector2D.of(2, 2), Vector2D.of(2, 4), TEST_PRECISION));
        segments.add(LineSegment.fromPoints(Vector2D.of(2, 2), Vector2D.of(1, 3), TEST_PRECISION));

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(3, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.of(1, 2), Vector2D.ZERO,
                Vector2D.of(2, 2), Vector2D.of(1, 3));

        assertFinitePath(paths.get(1), Vector2D.of(0, 2), Vector2D.ZERO);

        assertFinitePath(paths.get(2), Vector2D.of(2, 2), Vector2D.of(2, 4));
    }

    @Test
    public void testMinimizeAngles_noSegments() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.minimizeAngles();
        List<LineSegment> segments = new ArrayList<>();

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(0, paths.size());
    }

    @Test
    public void testMinimizeAngles_singleFiniteSegment() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.minimizeAngles();
        List<LineSegment> segments = Arrays.asList(
                    LineSegment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION)
                );

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0), Vector2D.ZERO, Vector2D.PLUS_X);
    }

    @Test
    public void testMinimizeAngles_singleFiniteSegmentLoop() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.minimizeAngles();
        List<LineSegment> segments = shuffle(createSquare(Vector2D.ZERO, 1, 1));

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.PLUS_X, Vector2D.of(1, 1),
                Vector2D.of(0, 1), Vector2D.ZERO);
    }

    @Test
    public void testMinimizeAngles_disjointPaths() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.minimizeAngles();

        List<LineSegment> segments = new ArrayList<>();
        segments.addAll(createSquare(Vector2D.ZERO, 1, 1));

        Vector2D pt = Vector2D.of(0, 2);
        LineSegment a = Line.fromPointAndAngle(pt, Geometry.ZERO_PI, TEST_PRECISION).segmentTo(pt);
        LineSegment b = Line.fromPointAndAngle(pt, Geometry.HALF_PI, TEST_PRECISION).segmentFrom(pt);

        segments.add(a);
        segments.add(b);

        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.PLUS_X, Vector2D.of(1, 1),
                Vector2D.of(0, 1), Vector2D.ZERO);

        assertInfinitePath(paths.get(1), a, b, pt);
    }

    @Test
    public void testMinimizeAngles_squaresJoinedAtVertex() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.minimizeAngles();

        List<LineSegment> segments = new ArrayList<>();
        segments.addAll(createSquare(Vector2D.ZERO, 1, 1));
        segments.addAll(createSquare(Vector2D.of(1, 1), 1, 1));

        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.PLUS_X, Vector2D.of(1, 1),
                Vector2D.of(2, 1), Vector2D.of(2, 2),
                Vector2D.of(1, 2), Vector2D.of(1, 1),
                Vector2D.of(0, 1), Vector2D.ZERO);
    }

    @Test
    public void testMinimizeAngles_mutipleSegmentsAtVertex() {
        // arrange
        LineSegmentConnector connector = LineSegmentConnector.minimizeAngles();

        List<LineSegment> segments = new ArrayList<>();
        segments.add(LineSegment.fromPoints(Vector2D.ZERO, Vector2D.of(2, 2), TEST_PRECISION));

        segments.add(LineSegment.fromPoints(Vector2D.of(0, 2), Vector2D.ZERO, TEST_PRECISION));
        segments.add(LineSegment.fromPoints(Vector2D.of(1, 2), Vector2D.ZERO, TEST_PRECISION));

        segments.add(LineSegment.fromPoints(Vector2D.of(2, 2), Vector2D.of(2, 4), TEST_PRECISION));
        segments.add(LineSegment.fromPoints(Vector2D.of(2, 2), Vector2D.of(1, 3), TEST_PRECISION));

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(3, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.of(0, 2), Vector2D.ZERO,
                Vector2D.of(2, 2), Vector2D.of(2, 4));

        assertFinitePath(paths.get(1), Vector2D.of(1, 2), Vector2D.ZERO);

        assertFinitePath(paths.get(2), Vector2D.of(2, 2), Vector2D.of(1, 3));
    }

    private static List<LineSegment> createSquare(final Vector2D lowerLeft, final double width, final double height) {
        final Vector2D lowerRight = Vector2D.of(lowerLeft.getX() + width, lowerLeft.getY());
        final Vector2D upperRight = Vector2D.of(lowerLeft.getX() + width, lowerLeft.getY() + height);
        final Vector2D upperLeft = Vector2D.of(lowerLeft.getX(), lowerLeft.getY() + height);

        return Arrays.asList(
                    LineSegment.fromPoints(lowerLeft, lowerRight, TEST_PRECISION),
                    LineSegment.fromPoints(lowerRight, upperRight, TEST_PRECISION),
                    LineSegment.fromPoints(upperRight, upperLeft, TEST_PRECISION),
                    LineSegment.fromPoints(upperLeft, lowerLeft, TEST_PRECISION)
                );
    }

    private static List<LineSegment> shuffle(final List<LineSegment> segments) {
        return shuffle(segments, 1);
    }

    private static List<LineSegment> shuffle(final List<LineSegment> segments, final int seed) {
        Collections.shuffle(segments, new Random(seed));

        return segments;
    }

    private static void assertInfinitePath(LineSegmentPath path, LineSegment start, LineSegment end,
            Vector2D ... vertices) {
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());

        Assert.assertEquals(start, path.getStart());
        Assert.assertEquals(end, path.getEnd());

        assertPathVertices(path, vertices);
    }

    private static void assertFinitePath(LineSegmentPath path, Vector2D ... vertices)
    {
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());

        assertPathVertices(path, vertices);
    }

    private static void assertPathVertices(LineSegmentPath path, Vector2D ... vertices) {
        List<Vector2D> expectedVertices = Arrays.asList(vertices);
        List<Vector2D> actualVertices = path.getVertices();

        String msg = "Expected path vertices to equal " + expectedVertices + " but was " + actualVertices;
        Assert.assertEquals(msg, expectedVertices.size(), actualVertices.size());

        for (int i=0; i<expectedVertices.size(); ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(expectedVertices.get(i), actualVertices.get(i), TEST_EPS);
        }
    }
}
