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

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.shapes.Parallelogram;
import org.junit.Test;

public class BoundarySourceLinecaster2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final BoundarySource2D UNIT_SQUARE =
            Parallelogram.axisAligned(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION);

    @Test
    public void testLinecast_line_simple() {
        // arrange
        BoundarySourceLinecaster2D linecaster = new BoundarySourceLinecaster2D(UNIT_SQUARE);

        // act/assert

        // no intersections
        LinecastChecker2D.with(linecaster)
            .expectNothing()
            .whenGiven(Line.fromPointAndDirection(Vector2D.of(0, 4), Vector2D.Unit.MINUS_X, TEST_PRECISION));

        // through center; two directions
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(0, 0.5), Vector2D.Unit.MINUS_X)
            .and(Vector2D.of(1, 0.5), Vector2D.Unit.PLUS_X)
            .whenGiven(Line.fromPointAndDirection(Vector2D.of(0.5, 0.5), Vector2D.Unit.PLUS_X, TEST_PRECISION));

        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(1, 0.5), Vector2D.Unit.PLUS_X)
            .and(Vector2D.of(0, 0.5), Vector2D.Unit.MINUS_X)
            .whenGiven(Line.fromPointAndDirection(Vector2D.of(0.5, 0.5), Vector2D.Unit.MINUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast_line_alongFace() {
        // arrange
        BoundarySourceLinecaster2D linecaster = new BoundarySourceLinecaster2D(UNIT_SQUARE);

        // act/assert
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(0, 1), Vector2D.Unit.MINUS_X)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Line.fromPointAndDirection(Vector2D.of(0, 1), Vector2D.Unit.PLUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast_line_corners() {
        // arrange
        BoundarySourceLinecaster2D linecaster = new BoundarySourceLinecaster2D(UNIT_SQUARE);

        // act/assert

        // through single corner vertex
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Line.fromPointAndDirection(Vector2D.of(0, 2), Vector2D.of(1, -1), TEST_PRECISION));

        // through two corner vertices
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.ZERO, Vector2D.Unit.MINUS_X)
            .and(Vector2D.ZERO, Vector2D.Unit.MINUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION));
    }

    @Test
    public void testLinecast_line_removesDuplicatePoints() {
        // arrange
        BoundarySource2D src = BoundarySource2D.from(
                    Segment.fromPoints(Vector2D.of(-1, -1), Vector2D.ZERO, TEST_PRECISION),
                    Segment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION)
                );
        BoundarySourceLinecaster2D linecaster = new BoundarySourceLinecaster2D(src);

        // act/assert
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.ZERO, Vector2D.Unit.from(1, -1))
            .whenGiven(Line.fromPointAndDirection(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast_segment_simple() {
        // arrange
        BoundarySourceLinecaster2D linecaster = new BoundarySourceLinecaster2D(UNIT_SQUARE);

        // act/assert

        // no intersections; underlying line does not intersect
        LinecastChecker2D.with(linecaster)
            .expectNothing()
            .whenGiven(Line.fromPointAndDirection(Vector2D.of(0, 4), Vector2D.Unit.MINUS_X, TEST_PRECISION)
                    .segment(-10, 10));

        // no intersections; underlying line does intersect
        LinecastChecker2D.with(linecaster)
            .expectNothing()
            .whenGiven(Line.fromPointAndDirection(Vector2D.of(0.5, 0.5), Vector2D.Unit.PLUS_X, TEST_PRECISION)
                    .segment(2, 10));

        // no boundaries excluded; two directions
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(0, 0.5), Vector2D.Unit.MINUS_X)
            .and(Vector2D.of(1, 0.5), Vector2D.Unit.PLUS_X)
            .whenGiven(Line.fromPointAndDirection(Vector2D.of(0.5, 0.5), Vector2D.Unit.PLUS_X, TEST_PRECISION)
                    .segment(-10, 10));

        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(1, 0.5), Vector2D.Unit.PLUS_X)
            .and(Vector2D.of(0, 0.5), Vector2D.Unit.MINUS_X)
            .whenGiven(Line.fromPointAndDirection(Vector2D.of(0.5, 0.5), Vector2D.Unit.MINUS_X, TEST_PRECISION)
                    .segment(-10, 10));
    }

    @Test
    public void testLinecast_segment_boundaryExcluded() {
        // arrange
        BoundarySourceLinecaster2D linecaster = new BoundarySourceLinecaster2D(UNIT_SQUARE);

        // act/assert
        Vector2D center = Vector2D.of(0.5, 0.5);
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(1, 0.5), Vector2D.Unit.PLUS_X)
            .whenGiven(Line.fromPointAndDirection(center, Vector2D.Unit.PLUS_X, TEST_PRECISION)
                    .segmentFrom(center));

        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(1, 0.5), Vector2D.Unit.PLUS_X)
            .whenGiven(Line.fromPointAndDirection(center, Vector2D.Unit.MINUS_X, TEST_PRECISION)
                    .segmentTo(center));
    }

    @Test
    public void testLinecast_segment_startEndPointsOnBoundaries() {
        // arrange
        BoundarySourceLinecaster2D linecaster = new BoundarySourceLinecaster2D(UNIT_SQUARE);

        // act/assert
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(1, 0.5), Vector2D.Unit.PLUS_X)
            .and(Vector2D.of(0, 0.5), Vector2D.Unit.MINUS_X)
            .whenGiven(Segment.fromPoints(Vector2D.of(1, 0.5), Vector2D.of(0, 0.5), TEST_PRECISION));
    }

    @Test
    public void testLinecast_segment_alongFace() {
        // arrange
        BoundarySourceLinecaster2D linecaster = new BoundarySourceLinecaster2D(UNIT_SQUARE);

        // act/assert

        // includes two intersecting boundaries
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(0, 1), Vector2D.Unit.MINUS_X)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Segment.fromPoints(Vector2D.of(-1, 1), Vector2D.of(2, 1), TEST_PRECISION));

        // one intersecting boundary
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Segment.fromPoints(Vector2D.of(0.25, 1), Vector2D.of(2, 1), TEST_PRECISION));

        // no intersecting boundary
        LinecastChecker2D.with(linecaster)
            .expectNothing()
            .whenGiven(Segment.fromPoints(Vector2D.of(0.25, 1), Vector2D.of(0.75, 1), TEST_PRECISION));
    }

    @Test
    public void testLinecast_segment_corners() {
        // arrange
        BoundarySourceLinecaster2D linecaster = new BoundarySourceLinecaster2D(UNIT_SQUARE);

        // act/assert

        // through corner
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Segment.fromPoints(Vector2D.of(0, 2), Vector2D.of(2, 0), TEST_PRECISION));

        // starts on corner
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Segment.fromPoints(Vector2D.of(1, 1), Vector2D.of(2, 0), TEST_PRECISION));

        // ends on corner
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y)
            .and(Vector2D.of(1, 1), Vector2D.Unit.PLUS_X)
            .whenGiven(Segment.fromPoints(Vector2D.of(0, 2), Vector2D.of(1, 1), TEST_PRECISION));
    }

    @Test
    public void testLinecast_segment_removesDuplicatePoints() {
        // arrange
        BoundarySource2D src = BoundarySource2D.from(
                    Segment.fromPoints(Vector2D.of(-1, -1), Vector2D.ZERO, TEST_PRECISION),
                    Segment.fromPoints(Vector2D.ZERO, Vector2D.of(1, 1), TEST_PRECISION)
                );
        BoundarySourceLinecaster2D linecaster = new BoundarySourceLinecaster2D(src);

        // act/assert
        LinecastChecker2D.with(linecaster)
            .expect(Vector2D.ZERO, Vector2D.Unit.from(1, -1))
            .whenGiven(Segment.fromPoints(Vector2D.ZERO, Vector2D.Unit.PLUS_X, TEST_PRECISION));
    }
}

