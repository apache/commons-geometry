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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class AbstractLineSegmentConnectorTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Line Y_AXIS = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.HALF_PI,
            TEST_PRECISION);

    private TestConnector connector = new TestConnector();

    @Test
    public void testConnect_emptyCollection() {
        // act
        List<LineSegmentPath> paths = connector.connect(Collections.emptyList());

        // assert
        Assert.assertEquals(0, paths.size());
    }

    @Test
    public void testConnect_singleInfiniteLine() {
        // arrange
        LineSegment segment = Y_AXIS.span();

        // act
        List<LineSegmentPath> paths = connector.connect(Arrays.asList(segment));

        // assert
        Assert.assertEquals(1, paths.size());

        LineSegmentPath path = paths.get(0);
        Assert.assertEquals(1, path.getSegments().size());
        Assert.assertSame(segment, path.getStartSegment());
    }

    @Test
    public void testConnect_singleHalfInfiniteLine_noEndPoint() {
        // arrange
        LineSegment segment = Y_AXIS.segmentFrom(Vector2D.ZERO);

        // act
        List<LineSegmentPath> paths = connector.connect(Arrays.asList(segment));

        // assert
        Assert.assertEquals(1, paths.size());

        LineSegmentPath path = paths.get(0);
        Assert.assertEquals(1, path.getSegments().size());
        Assert.assertSame(segment, path.getStartSegment());
    }

    @Test
    public void testConnect_singleHalfInfiniteLine_noStartPoint() {
        // arrange
        LineSegment segment = Y_AXIS.segmentTo(Vector2D.ZERO);

        // act
        List<LineSegmentPath> paths = connector.connect(Arrays.asList(segment));

        // assert
        Assert.assertEquals(1, paths.size());

        LineSegmentPath path = paths.get(0);
        Assert.assertEquals(1, path.getSegments().size());
        Assert.assertSame(segment, path.getStartSegment());
    }

    @Test
    public void testConnect_disjointSegments() {
        // arrange
        LineSegment a = Y_AXIS.segment(Vector2D.of(0, 1), Vector2D.of(0, 2));
        LineSegment b = Y_AXIS.segment(Vector2D.of(0, -1), Vector2D.ZERO);

        List<LineSegment> segments = Arrays.asList(a, b);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0), Vector2D.of(0, -1), Vector2D.ZERO);
        assertFinitePath(paths.get(1), Vector2D.of(0, 1), Vector2D.of(0, 2));
    }

    @Test
    public void testConnect_singleClosedPath() {
        // arrange
        LineSegmentPath input = LineSegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(1, 1), Vector2D.ZERO, Vector2D.of(1, 0))
                .close();

        List<LineSegment> segments = new ArrayList<>(input.getSegments());
        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, 1), Vector2D.ZERO);
    }

    @Test
    public void testConnect_multipleClosedPaths() {
        // arrange
        LineSegmentPath a = LineSegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(1, 1), Vector2D.ZERO, Vector2D.of(1, 0))
                .close();

        LineSegmentPath b = LineSegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(0, 1), Vector2D.of(-1, 0), Vector2D.ZERO)
                .close();

        LineSegmentPath c = LineSegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(1, 3), Vector2D.of(0, 2), Vector2D.of(1, 2))
                .close();

        List<LineSegment> segments = new ArrayList<>();
        segments.addAll(a.getSegments());
        segments.addAll(b.getSegments());
        segments.addAll(c.getSegments());

        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(3, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.of(-1, 0), Vector2D.ZERO, Vector2D.of(0, 1), Vector2D.of(-1, 0));

        assertFinitePath(paths.get(1),
                Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, 1), Vector2D.ZERO);

        assertFinitePath(paths.get(2),
                Vector2D.of(0, 2), Vector2D.of(1, 2), Vector2D.of(1, 3), Vector2D.of(0, 2));
    }

    @Test
    public void testConnect_singleOpenPath() {
        // arrange
        LineSegmentPath input = LineSegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(1, 1), Vector2D.ZERO, Vector2D.of(1, 0))
                .build();

        List<LineSegment> segments = new ArrayList<>(input.getSegments());
        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.of(1, 1), Vector2D.ZERO, Vector2D.of(1, 0));
    }

    @Test
    public void testConnect_mixOfOpenConnectedAndInfinite() {
        // arrange
        LineSegment inputYInf = Y_AXIS.segmentTo(Vector2D.ZERO);
        LineSegment inputXInf = Line.fromPoints(Vector2D.ZERO, Vector2D.MINUS_X, TEST_PRECISION)
                .segmentFrom(Vector2D.ZERO);

        LineSegmentPath closedPath = LineSegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(0, 2), Vector2D.of(1, 2), Vector2D.of(1, 3))
                .close();

        LineSegmentPath openPath = LineSegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(-1, 3), Vector2D.of(0, 1), Vector2D.of(1, 1))
                .build();

        List<LineSegment> segments = new ArrayList<>();
        segments.add(inputYInf);
        segments.add(inputXInf);
        segments.addAll(closedPath.getSegments());
        segments.addAll(openPath.getSegments());

        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(3, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.of(-1, 3), Vector2D.of(0, 1), Vector2D.of(1, 1));

        LineSegmentPath infPath = paths.get(1);
        Assert.assertTrue(infPath.isInfinite());
        Assert.assertEquals(2, infPath.getSegments().size());
        Assert.assertSame(inputYInf, infPath.getSegments().get(0));
        Assert.assertSame(inputXInf, infPath.getSegments().get(1));

        assertFinitePath(paths.get(2),
                Vector2D.of(0, 2), Vector2D.of(1, 2), Vector2D.of(1, 3), Vector2D.of(0, 2));
    }

    @Test
    public void testConnect_intersectingPaths() {
        // arrange
        LineSegmentPath a = LineSegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(-1, 1), Vector2D.of(0.5, 0), Vector2D.of(-1, -1))
                .build();

        LineSegmentPath b = LineSegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(1, 1), Vector2D.of(-0.5, 0), Vector2D.of(1, -1))
                .build();

        List<LineSegment> segments = new ArrayList<>();
        segments.addAll(a.getSegments());
        segments.addAll(b.getSegments());

        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.of(-1, 1), Vector2D.of(0.5, 0), Vector2D.of(-1, -1));

        assertFinitePath(paths.get(1),
                Vector2D.of(1, 1), Vector2D.of(-0.5, 0), Vector2D.of(1, -1));
    }

    private static List<LineSegment> shuffle(final List<LineSegment> segments) {
        return shuffle(segments, 1);
    }

    private static List<LineSegment> shuffle(final List<LineSegment> segments, final int seed) {
        Collections.shuffle(segments, new Random(seed));

        return segments;
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

    private static class TestConnector extends AbstractLineSegmentConnector {

        private static final long serialVersionUID = 1L;

        @Override
        protected ConnectorEntry selectConnection(ConnectorEntry incoming, List<ConnectorEntry> outgoing) {
            // simply choose the first option
            return outgoing.get(0);
        }
    }
}
