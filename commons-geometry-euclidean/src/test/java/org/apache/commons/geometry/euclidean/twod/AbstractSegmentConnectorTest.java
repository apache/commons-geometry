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

public class AbstractSegmentConnectorTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final Line Y_AXIS = Line.fromPointAndAngle(Vector2D.ZERO, Geometry.HALF_PI,
            TEST_PRECISION);

    private TestConnector connector = new TestConnector();

    @Test
    public void testGetPaths_emptyCollection() {
        // act
        List<SegmentPath> paths = connector.getPaths(Collections.emptyList());

        // assert
        Assert.assertEquals(0, paths.size());
    }

    @Test
    public void testGetPaths_singleInfiniteLine() {
        // arrange
        Segment segment = Y_AXIS.span();

        // act
        List<SegmentPath> paths = connector.getPaths(Arrays.asList(segment));

        // assert
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertEquals(1, path.getSegments().size());
        Assert.assertSame(segment, path.getStartSegment());
    }

    @Test
    public void testGetPaths_singleHalfInfiniteLine_noEndPoint() {
        // arrange
        Segment segment = Y_AXIS.segmentFrom(Vector2D.ZERO);

        // act
        List<SegmentPath> paths = connector.getPaths(Arrays.asList(segment));

        // assert
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertEquals(1, path.getSegments().size());
        Assert.assertSame(segment, path.getStartSegment());
    }

    @Test
    public void testGetPaths_singleHalfInfiniteLine_noStartPoint() {
        // arrange
        Segment segment = Y_AXIS.segmentTo(Vector2D.ZERO);

        // act
        List<SegmentPath> paths = connector.getPaths(Arrays.asList(segment));

        // assert
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertEquals(1, path.getSegments().size());
        Assert.assertSame(segment, path.getStartSegment());
    }

    @Test
    public void testGetPaths_disjointSegments() {
        // arrange
        Segment a = Y_AXIS.segment(Vector2D.of(0, 1), Vector2D.of(0, 2));
        Segment b = Y_AXIS.segment(Vector2D.of(0, -1), Vector2D.ZERO);

        List<Segment> segments = Arrays.asList(a, b);

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0), Vector2D.of(0, -1), Vector2D.ZERO);
        assertFinitePath(paths.get(1), Vector2D.of(0, 1), Vector2D.of(0, 2));
    }

    @Test
    public void testGetPaths_singleClosedPath() {
        // arrange
        SegmentPath input = SegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(1, 1), Vector2D.ZERO, Vector2D.of(1, 0))
                .close();

        List<Segment> segments = new ArrayList<>(input.getSegments());
        shuffle(segments);

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, 1), Vector2D.ZERO);
    }

    @Test
    public void testGetPaths_multipleClosedPaths() {
        // arrange
        SegmentPath a = SegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(1, 1), Vector2D.ZERO, Vector2D.of(1, 0))
                .close();

        SegmentPath b = SegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(0, 1), Vector2D.of(-1, 0), Vector2D.of(-0.5, 0))
                .close();

        SegmentPath c = SegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(1, 3), Vector2D.of(0, 2), Vector2D.of(1, 2))
                .close();

        List<Segment> segments = new ArrayList<>();
        segments.addAll(a.getSegments());
        segments.addAll(b.getSegments());
        segments.addAll(c.getSegments());

        shuffle(segments);

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(3, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.of(-1, 0), Vector2D.of(-0.5, 0), Vector2D.of(0, 1), Vector2D.of(-1, 0));

        assertFinitePath(paths.get(1),
                Vector2D.ZERO, Vector2D.of(1, 0), Vector2D.of(1, 1), Vector2D.ZERO);

        assertFinitePath(paths.get(2),
                Vector2D.of(0, 2), Vector2D.of(1, 2), Vector2D.of(1, 3), Vector2D.of(0, 2));
    }

    @Test
    public void testGetPaths_singleOpenPath() {
        // arrange
        SegmentPath input = SegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(1, 1), Vector2D.ZERO, Vector2D.of(1, 0))
                .build();

        List<Segment> segments = new ArrayList<>(input.getSegments());
        shuffle(segments);

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.of(1, 1), Vector2D.ZERO, Vector2D.of(1, 0));
    }

    @Test
    public void testGetPaths_mixOfOpenConnectedAndInfinite() {
        // arrange
        Segment inputYInf = Y_AXIS.segmentTo(Vector2D.ZERO);
        Segment inputXInf = Line.fromPoints(Vector2D.ZERO, Vector2D.MINUS_X, TEST_PRECISION)
                .segmentFrom(Vector2D.ZERO);

        SegmentPath closedPath = SegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(0, 2), Vector2D.of(1, 2), Vector2D.of(1, 3))
                .close();

        SegmentPath openPath = SegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(-1, 3), Vector2D.of(0, 1), Vector2D.of(1, 1))
                .build();

        List<Segment> segments = new ArrayList<>();
        segments.add(inputYInf);
        segments.add(inputXInf);
        segments.addAll(closedPath.getSegments());
        segments.addAll(openPath.getSegments());

        shuffle(segments);

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(3, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.of(-1, 3), Vector2D.of(0, 1), Vector2D.of(1, 1));

        SegmentPath infPath = paths.get(1);
        Assert.assertTrue(infPath.isInfinite());
        Assert.assertEquals(2, infPath.getSegments().size());
        Assert.assertSame(inputYInf, infPath.getSegments().get(0));
        Assert.assertSame(inputXInf, infPath.getSegments().get(1));

        assertFinitePath(paths.get(2),
                Vector2D.of(0, 2), Vector2D.of(1, 2), Vector2D.of(1, 3), Vector2D.of(0, 2));
    }

    @Test
    public void testGetPaths_pathWithSinglePoint() {
        // arrange
        Vector2D p0 = Vector2D.ZERO;

        List<Segment> segments = Arrays.asList(Line.fromPointAndAngle(p0, 0, TEST_PRECISION).segment(p0, p0));

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0), p0, p0);
    }

    @Test
    public void testGetPaths_pathWithPointLikeConnectedSegments() {
        // arrange
        Vector2D p0 = Vector2D.ZERO;
        Vector2D p1 = Vector2D.of(1, 0);
        Vector2D p2 = Vector2D.of(1, 1);

        Vector2D almostP0 = Vector2D.of(-1e-20, -1e-20);
        Vector2D almostP1 = Vector2D.of(1 - 1e-15, 0);

        SegmentPath input = SegmentPath.builder(TEST_PRECISION)
                .appendVertices(p0, p1)
                .append(Line.fromPointAndAngle(p1, 0.25 * Geometry.PI, TEST_PRECISION).segment(p1, p1))
                .append(Line.fromPointAndAngle(p1, -0.25 * Geometry.PI, TEST_PRECISION).segment(almostP1, almostP1))
                .append(p2)
                .append(p0)
                .append(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.MINUS_HALF_PI, TEST_PRECISION)
                        .segment(almostP0, almostP0))
                .build();

        List<Segment> segments = new ArrayList<>(input.getSegments());
        shuffle(segments);

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0), p0, p1, almostP1, p1, p2, p0, almostP0);
    }

    @Test
    public void testGetPaths_flatLineRegion() {
        // arrange
        Vector2D p0 = Vector2D.ZERO;
        Vector2D p1 = Vector2D.of(1, 0);

        Segment seg0 = Segment.fromPoints(p0, p1, TEST_PRECISION);
        Segment seg1 = Segment.fromPoints(p1, p0, TEST_PRECISION);
        Segment seg2 = Line.fromPointAndAngle(p1, Geometry.HALF_PI, TEST_PRECISION).segment(p1, p1);
        Segment seg3 = Line.fromPointAndAngle(p0, Geometry.MINUS_HALF_PI, TEST_PRECISION).segment(p0, p0);

        List<Segment> segments = new ArrayList<>(Arrays.asList(seg0, seg1, seg2, seg3));
        shuffle(segments);

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertSame(seg0, path.getSegments().get(0));
        Assert.assertSame(seg2, path.getSegments().get(1));
        Assert.assertSame(seg1, path.getSegments().get(2));
        Assert.assertSame(seg3, path.getSegments().get(3));
    }

    @Test
    public void testGetPaths_singlePointRegion() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 0);

        Segment seg0 = Line.fromPointAndAngle(p0, Geometry.ZERO_PI, TEST_PRECISION).segment(p0, p0);
        Segment seg1 = Line.fromPointAndAngle(p0, Geometry.HALF_PI, TEST_PRECISION).segment(p0, p0);
        Segment seg2 = Line.fromPointAndAngle(p0, Geometry.PI, TEST_PRECISION).segment(p0, p0);
        Segment seg3 = Line.fromPointAndAngle(p0, Geometry.MINUS_HALF_PI, TEST_PRECISION).segment(p0, p0);

        List<Segment> segments = new ArrayList<>(Arrays.asList(seg0, seg1, seg2, seg3));
        shuffle(segments);

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertSame(seg2, path.getSegments().get(0));
        Assert.assertSame(seg3, path.getSegments().get(1));
        Assert.assertSame(seg0, path.getSegments().get(2));
        Assert.assertSame(seg1, path.getSegments().get(3));
    }

    @Test
    public void testGetPaths_pathWithPointLikeUnconnectedSegments() {
        // arrange
        Vector2D p0 = Vector2D.ZERO;
        Vector2D p1 = Vector2D.of(1, 0);

        Segment seg0 = Line.fromPointAndAngle(p1, Geometry.ZERO_PI, TEST_PRECISION).segment(p1, p1);
        Segment seg1 = Line.fromPointAndAngle(p1, 0.25 * Geometry.PI, TEST_PRECISION).segment(p1, p1);
        Segment seg2 = Line.fromPointAndAngle(p0, 0, TEST_PRECISION).segment(p0, p0);

        List<Segment> segments = new ArrayList<>(Arrays.asList(seg0, seg1, seg2));

        shuffle(segments);

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        SegmentPath path0 = paths.get(0);
        Assert.assertEquals(1, path0.getSegments().size());
        Assert.assertSame(seg2, path0.getSegments().get(0));

        SegmentPath path1 = paths.get(1);
        Assert.assertEquals(2, path1.getSegments().size());
        Assert.assertSame(seg0, path1.getSegments().get(0));
        Assert.assertSame(seg1, path1.getSegments().get(1));
    }

    @Test
    public void testGetPaths_pathStartingWithPoint() {
        // arrange
        Vector2D p0 = Vector2D.ZERO;
        Vector2D p1 = Vector2D.of(1, 0);
        Vector2D p2 = Vector2D.of(1, 1);

        Segment seg0 = Line.fromPointAndAngle(p0, Geometry.PI, TEST_PRECISION).segment(p0, p0);
        Segment seg1 = Segment.fromPoints(p0, p1, TEST_PRECISION);
        Segment seg2 = Segment.fromPoints(p1, p2, TEST_PRECISION);

        List<Segment> segments = new ArrayList<>(Arrays.asList(seg0, seg1, seg2));

        shuffle(segments);

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        SegmentPath path = paths.get(0);
        Assert.assertSame(seg0, path.getSegments().get(0));
        Assert.assertSame(seg1, path.getSegments().get(1));
        Assert.assertSame(seg2, path.getSegments().get(2));
    }

    @Test
    public void testGetPaths_intersectingPaths() {
        // arrange
        SegmentPath a = SegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(-1, 1), Vector2D.of(0.5, 0), Vector2D.of(-1, -1))
                .build();

        SegmentPath b = SegmentPath.builder(TEST_PRECISION)
                .appendVertices(Vector2D.of(1, 1), Vector2D.of(-0.5, 0), Vector2D.of(1, -1))
                .build();

        List<Segment> segments = new ArrayList<>();
        segments.addAll(a.getSegments());
        segments.addAll(b.getSegments());

        shuffle(segments);

        // act
        List<SegmentPath> paths = connector.getPaths(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0),
                Vector2D.of(-1, 1), Vector2D.of(0.5, 0), Vector2D.of(-1, -1));

        assertFinitePath(paths.get(1),
                Vector2D.of(1, 1), Vector2D.of(-0.5, 0), Vector2D.of(1, -1));
    }

    @Test
    public void testInstancesCanBeReused() {
        // arrange
        Segment a = Segment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);
        Segment b = Segment.fromPoints(Vector2D.PLUS_X, Vector2D.PLUS_Y, TEST_PRECISION);

        // act
        List<SegmentPath> firstPaths = connector.getPaths(Arrays.asList(a));
        List<SegmentPath> secondPaths = connector.getPaths(Arrays.asList(b));

        // assert
        Assert.assertEquals(1, firstPaths.size());
        Assert.assertEquals(1, secondPaths.size());

        Assert.assertSame(a, firstPaths.get(0).getSegments().get(0));
        Assert.assertSame(b, secondPaths.get(0).getSegments().get(0));
    }

    @Test
    public void testAdd() {
        // arrange
        Segment a = Segment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);
        Segment b = Segment.fromPoints(Vector2D.PLUS_X, Vector2D.of(1, 1), TEST_PRECISION);
        Segment c = Segment.fromPoints(Vector2D.PLUS_X, Vector2D.of(2, 0), TEST_PRECISION);

        // act
        connector.add(Arrays.asList(a, b));
        connector.add(Arrays.asList(c));

        List<SegmentPath> paths = connector.getPaths();

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0), Vector2D.ZERO, Vector2D.PLUS_X, Vector2D.of(2, 0));
        assertFinitePath(paths.get(1), Vector2D.PLUS_X, Vector2D.of(1, 1));
    }

    @Test
    public void testConnect() {
        // arrange
        Segment a = Segment.fromPoints(Vector2D.ZERO, Vector2D.PLUS_X, TEST_PRECISION);
        Segment b = Segment.fromPoints(Vector2D.PLUS_X, Vector2D.of(1, 1), TEST_PRECISION);
        Segment c = Segment.fromPoints(Vector2D.PLUS_X, Vector2D.of(2, 0), TEST_PRECISION);

        // act
        connector.connect(Arrays.asList(a, b));
        connector.connect(Arrays.asList(c));

        List<SegmentPath> paths = connector.getPaths();

        // assert
        Assert.assertEquals(2, paths.size());

        assertFinitePath(paths.get(0), Vector2D.ZERO, Vector2D.PLUS_X, Vector2D.of(1, 1));
        assertFinitePath(paths.get(1), Vector2D.PLUS_X, Vector2D.of(2, 0));
    }

    private static List<Segment> shuffle(final List<Segment> segments) {
        return shuffle(segments, 1);
    }

    private static List<Segment> shuffle(final List<Segment> segments, final int seed) {
        Collections.shuffle(segments, new Random(seed));

        return segments;
    }

    private static void assertFinitePath(SegmentPath path, Vector2D ... vertices)
    {
        Assert.assertFalse(path.isInfinite());
        Assert.assertTrue(path.isFinite());

        assertPathVertices(path, vertices);
    }

    private static void assertPathVertices(SegmentPath path, Vector2D ... vertices) {
        List<Vector2D> expectedVertices = Arrays.asList(vertices);
        List<Vector2D> actualVertices = path.getVertices();

        String msg = "Expected path vertices to equal " + expectedVertices + " but was " + actualVertices;
        Assert.assertEquals(msg, expectedVertices.size(), actualVertices.size());

        for (int i=0; i<expectedVertices.size(); ++i) {
            EuclideanTestUtils.assertCoordinatesEqual(expectedVertices.get(i), actualVertices.get(i), TEST_EPS);
        }
    }

    private static class TestConnector extends AbstractSegmentConnector {

        private static final long serialVersionUID = 1L;

        @Override
        protected ConnectorEntry selectConnection(ConnectorEntry incoming, List<ConnectorEntry> outgoing) {
            // simply choose the first option
            return outgoing.get(0);
        }
    }
}
