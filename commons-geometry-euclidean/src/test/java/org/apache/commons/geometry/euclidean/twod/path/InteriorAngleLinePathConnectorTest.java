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
package org.apache.commons.geometry.euclidean.twod.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.LineConvexSubset;
import org.apache.commons.geometry.euclidean.twod.Lines;
import org.apache.commons.geometry.euclidean.twod.Ray;
import org.apache.commons.geometry.euclidean.twod.ReverseRay;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.InteriorAngleLinePathConnector.Maximize;
import org.apache.commons.geometry.euclidean.twod.path.InteriorAngleLinePathConnector.Minimize;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.junit.Assert;
import org.junit.Test;

public class InteriorAngleLinePathConnectorTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testConnectAll_noSegments() {
        runWithMaxAndMin(connector -> {
            // arrange
            final List<LineConvexSubset> segments = new ArrayList<>();

            // act
            final List<LinePath> paths = connector.connectAll(segments);

            // assert
            Assert.assertEquals(0, paths.size());
        });
    }

    @Test
    public void testConnectAll_singleFiniteSegment() {
        runWithMaxAndMin(connector -> {
            // arrange
            final List<LineConvexSubset> segments = Collections.singletonList(
                    Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION)
            );

            // act
            final List<LinePath> paths = connector.connectAll(segments);

            // assert
            Assert.assertEquals(1, paths.size());

            assertFinitePath(paths.get(0), Vector2D.ZERO, Vector2D.Unit.PLUS_X);
        });
    }

    @Test
    public void testConnectAll_dualConnectedSegments() {
        runWithMaxAndMin(connector -> {
            // arrange
            final List<LineConvexSubset> segments = Arrays.asList(
                        Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION),
                        Lines.segmentFromPoints(Vector2D.Unit.PLUS_X, Vector2D.ZERO, TEST_PRECISION)
                    );

            // act
            final List<LinePath> paths = connector.connectAll(segments);

            // assert
            Assert.assertEquals(1, paths.size());

            Assert.assertTrue(paths.get(0).isClosed());
            assertFinitePath(paths.get(0), Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.ZERO);
        });
    }

    @Test
    public void testConnectAll_singleFiniteSegmentLoop() {
        runWithMaxAndMin(connector -> {
            // arrange
            final List<LineConvexSubset> segments = shuffle(createSquare(Vector2D.ZERO, 1, 1));

            // act
            final List<LinePath> paths = connector.connectAll(segments);

            // assert
            Assert.assertEquals(1, paths.size());

            assertFinitePath(paths.get(0),
                    Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, 1),
                    Vector2D.of(0, 1), Vector2D.ZERO);
        });
    }

    @Test
    public void testConnectAll_disjointPaths() {
        runWithMaxAndMin(connector -> {
            // arrange
            final List<LineConvexSubset> segments = new ArrayList<>(createSquare(Vector2D.ZERO, 1, 1));

            final Vector2D pt = Vector2D.of(0, 2);
            final ReverseRay a = Lines.fromPointAndAngle(pt, 0.0, TEST_PRECISION).reverseRayTo(pt);
            final Ray b = Lines.fromPointAndAngle(pt, PlaneAngleRadians.PI_OVER_TWO, TEST_PRECISION).rayFrom(pt);

            segments.add(a);
            segments.add(b);

            shuffle(segments);

            // act
            final List<LinePath> paths = connector.connectAll(segments);

            // assert
            Assert.assertEquals(2, paths.size());

            assertFinitePath(paths.get(0),
                    Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, 1),
                    Vector2D.of(0, 1), Vector2D.ZERO);

            assertInfinitePath(paths.get(1), a, b, pt);
        });
    }

    @Test
    public void testConnectAll_squaresJoinedAtVertex_maximize() {
        // arrange
        final Maximize connector = new Maximize();

        final List<LineConvexSubset> segments = new ArrayList<>();
        segments.addAll(createSquare(Vector2D.ZERO, 1, 1));
        segments.addAll(createSquare(Vector2D.of(1, 1), 1, 1));

        shuffle(segments);

        // act
        final List<LinePath> paths = connector.connectAll(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.of(1, 1),
                Vector2D.of(2, 1), Vector2D.of(2, 2),
                Vector2D.of(1, 2), Vector2D.of(1, 1),
                Vector2D.of(0, 1), Vector2D.ZERO);
    }

    @Test
    public void testConnectAll_multipleSegmentsAtVertex_maximize() {
        // arrange
        final Maximize connector = new Maximize();

        final List<LineConvexSubset> segments = new ArrayList<>();
        segments.add(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(2, 2), TEST_PRECISION));

        segments.add(Lines.segmentFromPoints(Vector2D.of(2, 2), Vector2D.of(2, 4), TEST_PRECISION));
        segments.add(Lines.segmentFromPoints(Vector2D.of(2, 2), Vector2D.of(1, 3), TEST_PRECISION));

        // act
        final List<LinePath> paths = connector.connectAll(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.of(2, 2), Vector2D.of(2, 4));

        assertFinitePath(paths.get(1), Vector2D.of(2, 2), Vector2D.of(1, 3));
    }

    @Test
    public void testConnectAll_squaresJoinedAtVertex_minimize() {
        // arrange
        final Minimize connector = new Minimize();

        final List<LineConvexSubset> segments = new ArrayList<>();
        segments.addAll(createSquare(Vector2D.ZERO, 1, 1));
        segments.addAll(createSquare(Vector2D.of(1, 1), 1, 1));

        shuffle(segments);

        // act
        final List<LinePath> paths = connector.connectAll(segments);

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
    public void testConnectAll_multipleSegmentsAtVertex_minimize() {
        // arrange
        final Minimize connector = new Minimize();

        final List<LineConvexSubset> segments = new ArrayList<>();
        segments.add(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(2, 2), TEST_PRECISION));

        segments.add(Lines.segmentFromPoints(Vector2D.of(2, 2), Vector2D.of(2, 4), TEST_PRECISION));
        segments.add(Lines.segmentFromPoints(Vector2D.of(2, 2), Vector2D.of(1, 3), TEST_PRECISION));

        // act
        final List<LinePath> paths = connector.connectAll(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.of(2, 2), Vector2D.of(1, 3));

        assertFinitePath(paths.get(1), Vector2D.of(2, 2), Vector2D.of(2, 4));
    }

    @Test
    public void testConnectMaximized() {
        // arrange
        final List<LineConvexSubset> segments = new ArrayList<>();
        segments.add(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(2, 2), TEST_PRECISION));

        segments.add(Lines.segmentFromPoints(Vector2D.of(2, 2), Vector2D.of(2, 4), TEST_PRECISION));
        segments.add(Lines.segmentFromPoints(Vector2D.of(2, 2), Vector2D.of(1, 3), TEST_PRECISION));

        // act
        final List<LinePath> paths = InteriorAngleLinePathConnector.connectMaximized(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.of(2, 2), Vector2D.of(2, 4));

        assertFinitePath(paths.get(1), Vector2D.of(2, 2), Vector2D.of(1, 3));
    }

    @Test
    public void testConnectMinimized() {
        // arrange
        final List<LineConvexSubset> segments = new ArrayList<>();
        segments.add(Lines.segmentFromPoints(Vector2D.ZERO, Vector2D.of(2, 2), TEST_PRECISION));

        segments.add(Lines.segmentFromPoints(Vector2D.of(2, 2), Vector2D.of(2, 4), TEST_PRECISION));
        segments.add(Lines.segmentFromPoints(Vector2D.of(2, 2), Vector2D.of(1, 3), TEST_PRECISION));

        // act
        final List<LinePath> paths = InteriorAngleLinePathConnector.connectMinimized(segments);

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
    private static void runWithMaxAndMin(final Consumer<InteriorAngleLinePathConnector> body) {
        body.accept(new Maximize());
        body.accept(new Minimize());
    }

    private static List<LineConvexSubset> createSquare(final Vector2D lowerLeft, final double width, final double height) {
        final Vector2D lowerRight = Vector2D.of(lowerLeft.getX() + width, lowerLeft.getY());
        final Vector2D upperRight = Vector2D.of(lowerLeft.getX() + width, lowerLeft.getY() + height);
        final Vector2D upperLeft = Vector2D.of(lowerLeft.getX(), lowerLeft.getY() + height);

        return Arrays.asList(
                    Lines.segmentFromPoints(lowerLeft, lowerRight, TEST_PRECISION),
                    Lines.segmentFromPoints(lowerRight, upperRight, TEST_PRECISION),
                    Lines.segmentFromPoints(upperRight, upperLeft, TEST_PRECISION),
                    Lines.segmentFromPoints(upperLeft, lowerLeft, TEST_PRECISION)
                );
    }

    private static List<LineConvexSubset> shuffle(final List<LineConvexSubset> segments) {
        return shuffle(segments, 1);
    }

    private static List<LineConvexSubset> shuffle(final List<LineConvexSubset> segments, final int seed) {
        Collections.shuffle(segments, new Random(seed));

        return segments;
    }

    private static void assertInfinitePath(final LinePath path, final LineConvexSubset start, final LineConvexSubset end, final Vector2D... vertices) {
        Assert.assertTrue(path.isInfinite());
        Assert.assertFalse(path.isFinite());

        Assert.assertEquals(start, path.getStart());
        Assert.assertEquals(end, path.getEnd());

        assertPathVertices(path, vertices);
    }

    private static void assertFinitePath(final LinePath path, final Vector2D... vertices) {
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());

        assertPathVertices(path, vertices);
    }

    private static void assertPathVertices(final LinePath path, final Vector2D... vertices) {
        final List<Vector2D> expectedVertices = Arrays.asList(vertices);
        final List<Vector2D> actualVertices = path.getVertexSequence();

        final String msg = "Expected path vertices to equal " + expectedVertices + " but was " + actualVertices;
        Assert.assertEquals(msg, expectedVertices.size(), actualVertices.size());

        for (int i = 0; i < expectedVertices.size(); ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(expectedVertices.get(i), actualVertices.get(i), TEST_EPS);
        }
    }
}
