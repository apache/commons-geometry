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
package org.apache.commons.geometry.euclidean.threed;

import java.util.Arrays;

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.threed.line.Lines3D;
import org.apache.commons.geometry.euclidean.threed.shape.Parallelepiped;
import org.junit.jupiter.api.Test;

public class BoundarySourceLinecaster3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private static final BoundarySource3D UNIT_CUBE = Parallelepiped.builder(TEST_PRECISION)
            .setPosition(Vector3D.of(0.5, 0.5, 0.5))
            .build();

    @Test
    public void testLinecast_line_simple() {
        // arrange
        final BoundarySourceLinecaster3D linecaster = new BoundarySourceLinecaster3D(UNIT_CUBE);

        // act/assert

        // no intersections
        LinecastChecker3D.with(linecaster)
            .expectNothing()
            .whenGiven(Lines3D.fromPointAndDirection(Vector3D.of(0, 4, 4), Vector3D.Unit.MINUS_X, TEST_PRECISION));

        // through center; two directions
        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(0, 0.5, 0.5), Vector3D.Unit.MINUS_X)
            .and(Vector3D.of(1, 0.5, 0.5), Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.fromPointAndDirection(Vector3D.of(0.5, 0.5, 0.5), Vector3D.Unit.PLUS_X, TEST_PRECISION));

        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(1, 0.5, 0.5), Vector3D.Unit.PLUS_X)
            .and(Vector3D.of(0, 0.5, 0.5), Vector3D.Unit.MINUS_X)
            .whenGiven(Lines3D.fromPointAndDirection(Vector3D.of(0.5, 0.5, 0.5), Vector3D.Unit.MINUS_X, TEST_PRECISION));
    }

    @Test
    public void testLinecast_line_alongFace() {
        // arrange
        final BoundarySourceLinecaster3D linecaster = new BoundarySourceLinecaster3D(UNIT_CUBE);

        // act/assert
        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.ZERO, Vector3D.Unit.MINUS_Y)
            .and(Vector3D.ZERO, Vector3D.Unit.MINUS_Z)
            .and(Vector3D.of(0, 1, 1), Vector3D.Unit.PLUS_Z)
            .and(Vector3D.of(0, 1, 1), Vector3D.Unit.PLUS_Y)
            .whenGiven(Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(0, 1, 1), TEST_PRECISION));
    }

    @Test
    public void testLinecast_line_corners() {
        // arrange
        final BoundarySourceLinecaster3D linecaster = new BoundarySourceLinecaster3D(UNIT_CUBE);

        // act/assert

        // through single corner vertex
        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z)
            .and(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y)
            .and(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.fromPointAndDirection(Vector3D.of(1, 1, 1), Vector3D.of(1, -1, -1), TEST_PRECISION));

        // through two corner vertices
        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.ZERO, Vector3D.Unit.MINUS_X)
            .and(Vector3D.ZERO, Vector3D.Unit.MINUS_Y)
            .and(Vector3D.ZERO, Vector3D.Unit.MINUS_Z)
            .and(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Z)
            .and(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_Y)
            .and(Vector3D.of(1, 1, 1), Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.fromPointAndDirection(Vector3D.ZERO, Vector3D.of(1, 1, 1), TEST_PRECISION));
    }

    @Test
    public void testLinecast_line_removesDuplicatePoints() {
        // arrange
        final BoundarySource3D src = BoundarySource3D.of(
                    Planes.convexPolygonFromVertices(Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION),
                    Planes.convexPolygonFromVertices(Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X), TEST_PRECISION)
                );
        final BoundarySourceLinecaster3D linecaster = new BoundarySourceLinecaster3D(src);

        // act/assert
        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(0, 0.5, 0), Vector3D.Unit.PLUS_Z)
            .whenGiven(Lines3D.fromPointAndDirection(Vector3D.of(-1, 0.5, 1), Vector3D.of(1, 0, -1), TEST_PRECISION));
    }

    @Test
    public void testLinecast_segment_simple() {
        // arrange
        final BoundarySourceLinecaster3D linecaster = new BoundarySourceLinecaster3D(UNIT_CUBE);

        // act/assert

        // no intersections; underlying line does not intersect
        LinecastChecker3D.with(linecaster)
            .expectNothing()
            .whenGiven(Lines3D.fromPointAndDirection(Vector3D.of(0, 4, 4), Vector3D.Unit.MINUS_X, TEST_PRECISION)
                    .segment(-10, 10));

        // no intersections; underlying line does intersect
        LinecastChecker3D.with(linecaster)
            .expectNothing()
            .whenGiven(Lines3D.fromPointAndDirection(Vector3D.of(0.5, 0.5, 0.5), Vector3D.Unit.PLUS_X, TEST_PRECISION)
                    .segment(2, 10));

        // no boundaries excluded; two directions
        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(0, 0.5, 0.5), Vector3D.Unit.MINUS_X)
            .and(Vector3D.of(1, 0.5, 0.5), Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.fromPointAndDirection(Vector3D.of(0.5, 0.5, 0.5), Vector3D.Unit.PLUS_X, TEST_PRECISION)
                    .segment(-10, 10));

        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(1, 0.5, 0.5), Vector3D.Unit.PLUS_X)
            .and(Vector3D.of(0, 0.5, 0.5), Vector3D.Unit.MINUS_X)
            .whenGiven(Lines3D.fromPointAndDirection(Vector3D.of(0.5, 0.5, 0.5), Vector3D.Unit.MINUS_X, TEST_PRECISION)
                    .segment(-10, 10));
    }

    @Test
    public void testLinecast_segment_boundaryExcluded() {
        // arrange
        final BoundarySourceLinecaster3D linecaster = new BoundarySourceLinecaster3D(UNIT_CUBE);

        // act/assert
        final Vector3D center = Vector3D.of(0.5, 0.5, 0.5);
        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(1, 0.5, 0.5), Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.fromPointAndDirection(center, Vector3D.Unit.PLUS_X, TEST_PRECISION)
                    .rayFrom(center));

        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(1, 0.5, 0.5), Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.fromPointAndDirection(center, Vector3D.Unit.MINUS_X, TEST_PRECISION)
                    .reverseRayTo(center));
    }

    @Test
    public void testLinecast_segment_startEndPointsOnBoundaries() {
        // arrange
        final BoundarySourceLinecaster3D linecaster = new BoundarySourceLinecaster3D(UNIT_CUBE);

        // act/assert
        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(1, 0.5, 0.5), Vector3D.Unit.PLUS_X)
            .and(Vector3D.of(0, 0.5, 0.5), Vector3D.Unit.MINUS_X)
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(1, 0.5, 0.5), Vector3D.of(0, 0.5, 0.5), TEST_PRECISION));
    }

    @Test
    public void testLinecast_segment_alongFace() {
        // arrange
        final BoundarySourceLinecaster3D linecaster = new BoundarySourceLinecaster3D(UNIT_CUBE);

        // act/assert

        // includes two intersecting boundaries
        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(0, 1, 0), Vector3D.Unit.MINUS_X)
            .and(Vector3D.of(1, 1, 0), Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(-1, 1, 0), Vector3D.of(2, 1, 0), TEST_PRECISION));

        // one intersecting boundary
        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(1, 1, 0), Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(0.25, 1, 0), Vector3D.of(2, 1, 0), TEST_PRECISION));

        // no intersecting boundary
        LinecastChecker3D.with(linecaster)
            .expectNothing()
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(0.25, 1, 0), Vector3D.of(0.75, 1, 0), TEST_PRECISION));
    }

    @Test
    public void testLinecast_segment_corners() {
        // arrange
        final BoundarySourceLinecaster3D linecaster = new BoundarySourceLinecaster3D(UNIT_CUBE);

        final Vector3D corner = Vector3D.of(1, 1, 1);

        // act/assert

        // through corner
        LinecastChecker3D.with(linecaster)
            .expect(corner, Vector3D.Unit.PLUS_Z)
            .and(corner, Vector3D.Unit.PLUS_Y)
            .and(corner, Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(0.5, 0.5, 0.5), Vector3D.of(2, 2, 2), TEST_PRECISION));

        // starts on corner
        LinecastChecker3D.with(linecaster)
            .expect(corner, Vector3D.Unit.PLUS_Z)
            .and(corner, Vector3D.Unit.PLUS_Y)
            .and(corner, Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.segmentFromPoints(corner, Vector3D.of(2, 0, 2), TEST_PRECISION));

        // ends on corner
        LinecastChecker3D.with(linecaster)
            .expect(corner, Vector3D.Unit.PLUS_Z)
            .and(corner, Vector3D.Unit.PLUS_Y)
            .and(corner, Vector3D.Unit.PLUS_X)
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(0, 2, 2), corner, TEST_PRECISION));
    }

    @Test
    public void testLinecast_segment_removesDuplicatePoints() {
        // arrange
        final BoundarySource3D src = BoundarySource3D.of(
                    Planes.convexPolygonFromVertices(Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_X, Vector3D.Unit.PLUS_Y), TEST_PRECISION),
                    Planes.convexPolygonFromVertices(Arrays.asList(Vector3D.ZERO, Vector3D.Unit.PLUS_Y, Vector3D.Unit.MINUS_X), TEST_PRECISION)
                );
        final BoundarySourceLinecaster3D linecaster = new BoundarySourceLinecaster3D(src);

        // act/assert
        LinecastChecker3D.with(linecaster)
            .expect(Vector3D.of(0, 0.5, 0), Vector3D.Unit.PLUS_Z)
            .whenGiven(Lines3D.segmentFromPoints(Vector3D.of(-1, 0.5, 1), Vector3D.of(1, 0.5, -1), TEST_PRECISION));
    }
}
