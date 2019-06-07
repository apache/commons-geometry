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
                .appendVertices(Vector2D.of(0, 1), Vector2D.of(-1, 0), Vector2D.of(-0.5, 0))
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
                Vector2D.of(-1, 0), Vector2D.of(-0.5, 0), Vector2D.of(0, 1), Vector2D.of(-1, 0));

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
    public void testConnect_pathWithSinglePoint() {
        // arrange
        Vector2D p0 = Vector2D.ZERO;

        List<LineSegment> segments = Arrays.asList(Line.fromPointAndAngle(p0, 0, TEST_PRECISION).segment(p0, p0));

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0), p0, p0);
    }

    @Test
    public void testConnect_pathWithPointLikeConnectedSegments() {
        // arrange
        Vector2D p0 = Vector2D.ZERO;
        Vector2D p1 = Vector2D.of(1, 0);
        Vector2D p2 = Vector2D.of(1, 1);

        Vector2D almostP0 = Vector2D.of(-1e-20, -1e-20);
        Vector2D almostP1 = Vector2D.of(1 - 1e-15, 0);

        LineSegmentPath input = LineSegmentPath.builder(TEST_PRECISION)
                .appendVertices(p0, p1)
                .append(Line.fromPointAndAngle(p1, 0.25 * Geometry.PI, TEST_PRECISION).segment(p1, p1))
                .append(Line.fromPointAndAngle(p1, Geometry.ZERO_PI, TEST_PRECISION).segment(almostP1, almostP1))
                .append(p2)
                .append(p0)
                .append(Line.fromPointAndAngle(Vector2D.ZERO, Geometry.MINUS_HALF_PI, TEST_PRECISION)
                        .segment(almostP0, almostP0))
                .build();

        List<LineSegment> segments = new ArrayList<>(input.getSegments());
        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        assertFinitePath(paths.get(0), p0, p1, almostP1, p1, p2, p0, almostP0);
    }

    @Test
    public void testConnect_flatLineRegion() {
        // arrange
        Vector2D p0 = Vector2D.ZERO;
        Vector2D p1 = Vector2D.of(1, 0);

        LineSegment seg0 = LineSegment.fromPoints(p0, p1, TEST_PRECISION);
        LineSegment seg1 = LineSegment.fromPoints(p1, p0, TEST_PRECISION);
        LineSegment seg2 = Line.fromPointAndAngle(p1, Geometry.HALF_PI, TEST_PRECISION).segment(p1, p1);
        LineSegment seg3 = Line.fromPointAndAngle(p0, Geometry.MINUS_HALF_PI, TEST_PRECISION).segment(p0, p0);

        List<LineSegment> segments = new ArrayList<>(Arrays.asList(seg0, seg1, seg2, seg3));
        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        LineSegmentPath path = paths.get(0);
        Assert.assertSame(seg0, path.getSegments().get(0));
        Assert.assertSame(seg2, path.getSegments().get(1));
        Assert.assertSame(seg1, path.getSegments().get(2));
        Assert.assertSame(seg3, path.getSegments().get(3));
    }

    @Test
    public void testConnect_singlePointRegion() {
        // arrange
        Vector2D p0 = Vector2D.of(1, 0);

        LineSegment seg0 = Line.fromPointAndAngle(p0, Geometry.ZERO_PI, TEST_PRECISION).segment(p0, p0);
        LineSegment seg1 = Line.fromPointAndAngle(p0, Geometry.HALF_PI, TEST_PRECISION).segment(p0, p0);
        LineSegment seg2 = Line.fromPointAndAngle(p0, Geometry.PI, TEST_PRECISION).segment(p0, p0);
        LineSegment seg3 = Line.fromPointAndAngle(p0, Geometry.MINUS_HALF_PI, TEST_PRECISION).segment(p0, p0);

        List<LineSegment> segments = new ArrayList<>(Arrays.asList(seg0, seg1, seg2, seg3));
        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        LineSegmentPath path = paths.get(0);
        Assert.assertSame(seg2, path.getSegments().get(0));
        Assert.assertSame(seg3, path.getSegments().get(1));
        Assert.assertSame(seg0, path.getSegments().get(2));
        Assert.assertSame(seg1, path.getSegments().get(3));
    }

    @Test
    public void testConnect_pathWithPointLikeUnconnectedSegments() {
        // arrange
        Vector2D p0 = Vector2D.ZERO;
        Vector2D p1 = Vector2D.of(1, 0);

        LineSegment seg0 = Line.fromPointAndAngle(p1, Geometry.ZERO_PI, TEST_PRECISION).segment(p1, p1);
        LineSegment seg1 = Line.fromPointAndAngle(p1, 0.25 * Geometry.PI, TEST_PRECISION).segment(p1, p1);
        LineSegment seg2 = Line.fromPointAndAngle(p0, 0, TEST_PRECISION).segment(p0, p0);

        List<LineSegment> segments = new ArrayList<>(Arrays.asList(seg0, seg1, seg2));

        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(2, paths.size());

        LineSegmentPath path0 = paths.get(0);
        Assert.assertEquals(1, path0.getSegments().size());
        Assert.assertSame(seg2, path0.getSegments().get(0));

        LineSegmentPath path1 = paths.get(1);
        Assert.assertEquals(2, path1.getSegments().size());
        Assert.assertSame(seg0, path1.getSegments().get(0));
        Assert.assertSame(seg1, path1.getSegments().get(1));
    }

    @Test
    public void testConnect_pathStartingWithPoint() {
        // arrange
        Vector2D p0 = Vector2D.ZERO;
        Vector2D p1 = Vector2D.of(1, 0);
        Vector2D p2 = Vector2D.of(1, 1);

        LineSegment seg0 = Line.fromPointAndAngle(p0, Geometry.PI, TEST_PRECISION).segment(p0, p0);
        LineSegment seg1 = LineSegment.fromPoints(p0, p1, TEST_PRECISION);
        LineSegment seg2 = LineSegment.fromPoints(p1, p2, TEST_PRECISION);

        List<LineSegment> segments = new ArrayList<>(Arrays.asList(seg0, seg1, seg2));

        shuffle(segments);

        // act
        List<LineSegmentPath> paths = connector.connect(segments);

        // assert
        Assert.assertEquals(1, paths.size());

        LineSegmentPath path = paths.get(0);
        Assert.assertSame(seg0, path.getSegments().get(0));
        Assert.assertSame(seg1, path.getSegments().get(1));
        Assert.assertSame(seg2, path.getSegments().get(2));
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
