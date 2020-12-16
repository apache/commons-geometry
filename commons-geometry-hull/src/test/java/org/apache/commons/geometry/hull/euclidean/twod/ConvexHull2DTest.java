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
package org.apache.commons.geometry.hull.euclidean.twod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.euclidean.twod.path.LinePath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ConvexHull2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    @Test
    public void testProperties_noPoints() {
        // act
        final ConvexHull2D hull = new ConvexHull2D(Collections.emptyList(), TEST_PRECISION);

        // assert
        Assertions.assertEquals(0, hull.getVertices().size());

        final LinePath path = hull.getPath();
        Assertions.assertEquals(0, path.getElements().size());

        final List<Vector2D> pathVertices = path.getVertexSequence();
        Assertions.assertEquals(0, pathVertices.size());

        Assertions.assertNull(hull.getRegion());
    }

    @Test
    public void testProperties_singlePoint() {
        // arrange
        final List<Vector2D> vertices = Collections.singletonList(Vector2D.Unit.PLUS_X);

        // act
        final ConvexHull2D hull = new ConvexHull2D(vertices, TEST_PRECISION);

        // assert
        Assertions.assertEquals(vertices, hull.getVertices());

        final LinePath path = hull.getPath();
        Assertions.assertEquals(0, path.getElements().size());

        final List<Vector2D> pathVertices = path.getVertexSequence();
        Assertions.assertEquals(0, pathVertices.size());

        Assertions.assertNull(hull.getRegion());
    }

    @Test
    public void testProperties_twoPoints() {
        // arrange
        final List<Vector2D> vertices = Arrays.asList(Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y);

        // act
        final ConvexHull2D hull = new ConvexHull2D(vertices, TEST_PRECISION);

        // assert
        Assertions.assertEquals(vertices, hull.getVertices());

        final LinePath path = hull.getPath();
        Assertions.assertEquals(1, path.getElements().size());

        final List<Vector2D> pathVertices = path.getVertexSequence();
        Assertions.assertEquals(2, pathVertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_X, pathVertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_Y, pathVertices.get(1), TEST_EPS);

        Assertions.assertNull(hull.getRegion());
    }

    @Test
    public void testProperties_threePoints() {
        // arrange
        final List<Vector2D> vertices = Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X, Vector2D.Unit.PLUS_Y);

        // act
        final ConvexHull2D hull = new ConvexHull2D(vertices, TEST_PRECISION);

        // assert
        Assertions.assertEquals(vertices, hull.getVertices());

        final LinePath path = hull.getPath();
        Assertions.assertEquals(3, path.getElements().size());

        final List<Vector2D> pathVertices = path.getVertexSequence();
        Assertions.assertEquals(4, pathVertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, pathVertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_X, pathVertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_Y, pathVertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, pathVertices.get(3), TEST_EPS);

        Assertions.assertEquals(0.5, hull.getRegion().getSize(), TEST_EPS);
    }

    @Test
    public void testProperties_fourPoints() {
        // arrange
        final List<Vector2D> vertices = Arrays.asList(Vector2D.ZERO, Vector2D.Unit.PLUS_X,
                Vector2D.of(1, 1), Vector2D.Unit.PLUS_Y);

        // act
        final ConvexHull2D hull = new ConvexHull2D(vertices, TEST_PRECISION);

        // assert
        Assertions.assertEquals(vertices, hull.getVertices());

        final LinePath path = hull.getPath();
        Assertions.assertEquals(4, path.getElements().size());

        final List<Vector2D> pathVertices = path.getVertexSequence();
        Assertions.assertEquals(5, pathVertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, pathVertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_X, pathVertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), pathVertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.Unit.PLUS_Y, pathVertices.get(3), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, pathVertices.get(4), TEST_EPS);

        Assertions.assertEquals(1.0, hull.getRegion().getSize(), TEST_EPS);
    }

    @Test
    public void testVertexListCannotBeModified() {
        // arrange
        final List<Vector2D> vertices = new ArrayList<>();
        vertices.add(Vector2D.Unit.PLUS_X);

        final ConvexHull2D hull = new ConvexHull2D(vertices, TEST_PRECISION);

        // act
        final List<Vector2D> hullVertices = hull.getVertices();

        // assert
        Assertions.assertNotSame(vertices, hullVertices);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> hullVertices.add(Vector2D.Unit.PLUS_Y));
    }

    @Test
    public void testToString() {
        // arrange
        final List<Vector2D> vertices = Collections.singletonList(Vector2D.Unit.PLUS_X);
        final ConvexHull2D hull = new ConvexHull2D(vertices, TEST_PRECISION);

        // act
        final String str = hull.toString();

        // assert
        GeometryTestUtils.assertContains("ConvexHull2D[vertices= [(1", str);
    }
}
