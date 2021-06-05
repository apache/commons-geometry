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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.geometry.spherical.SphericalTestUtils;
import org.apache.commons.geometry.spherical.twod.InteriorAngleGreatArcConnector.Maximize;
import org.apache.commons.geometry.spherical.twod.InteriorAngleGreatArcConnector.Minimize;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InteriorAngleGreatArcConnectorTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    @Test
    public void testConnectAll_empty() {
        runWithMaxAndMin(connector -> {
            // arrange
            final List<GreatArc> arcs = new ArrayList<>();
            connector.add(arcs);

            // act
            final List<GreatArcPath> paths = connector.connectAll();

            // assert
            Assertions.assertEquals(0, paths.size());
        });
    }

    @Test
    public void testConnectAll_singlePath() {
        runWithMaxAndMin(connector -> {
            // arrange
            final List<GreatArc> arcs = Collections.singletonList(
                    GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION)
            );
            connector.add(arcs);

            // act
            final List<GreatArcPath> paths = connector.connectAll();

            // assert
            Assertions.assertEquals(1, paths.size());

            final GreatArcPath a = paths.get(0);
            Assertions.assertEquals(1, a.getArcs().size());
            assertPathPoints(a, Point2S.PLUS_I, Point2S.PLUS_J);
        });
    }

    @Test
    public void testConnectAll_maximize_instance() {
        // arrange
        final GreatArc a1 = GreatCircles.arcFromPoints(Point2S.PLUS_K, Point2S.PLUS_I, TEST_PRECISION);
        final GreatArc a2 = GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatArc a3 = GreatCircles.arcFromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);

        final GreatArc b1 = GreatCircles.arcFromPoints(Point2S.PLUS_K, Point2S.MINUS_I, TEST_PRECISION);
        final GreatArc b2 = GreatCircles.arcFromPoints(Point2S.MINUS_I, Point2S.MINUS_J, TEST_PRECISION);
        final GreatArc b3 = GreatCircles.arcFromPoints(Point2S.MINUS_J, Point2S.PLUS_K, TEST_PRECISION);

        final InteriorAngleGreatArcConnector connector = new InteriorAngleGreatArcConnector.Maximize();

        // act
        final List<GreatArcPath> paths = connector.connectAll(Arrays.asList(b3, b1, a1, a3, b2, a2));

        // assert
        Assertions.assertEquals(1, paths.size());

        assertPathPoints(paths.get(0),
            Point2S.PLUS_K,
            Point2S.MINUS_I,
            Point2S.MINUS_J,
            Point2S.PLUS_K,
            Point2S.PLUS_I,
            Point2S.PLUS_J,
            Point2S.PLUS_K
        );
    }

    @Test
    public void testConnectAll_maximize_method() {
        // arrange
        final GreatArc a1 = GreatCircles.arcFromPoints(Point2S.PLUS_K, Point2S.PLUS_I, TEST_PRECISION);
        final GreatArc a2 = GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatArc a3 = GreatCircles.arcFromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);

        final GreatArc b1 = GreatCircles.arcFromPoints(Point2S.PLUS_K, Point2S.MINUS_I, TEST_PRECISION);
        final GreatArc b2 = GreatCircles.arcFromPoints(Point2S.MINUS_I, Point2S.MINUS_J, TEST_PRECISION);
        final GreatArc b3 = GreatCircles.arcFromPoints(Point2S.MINUS_J, Point2S.PLUS_K, TEST_PRECISION);

        // act
        final List<GreatArcPath> paths = InteriorAngleGreatArcConnector.connectMaximized(
                Arrays.asList(b3, b1, a1, a3, b2, a2));

        // assert
        Assertions.assertEquals(1, paths.size());

        assertPathPoints(paths.get(0),
            Point2S.PLUS_K,
            Point2S.MINUS_I,
            Point2S.MINUS_J,
            Point2S.PLUS_K,
            Point2S.PLUS_I,
            Point2S.PLUS_J,
            Point2S.PLUS_K
        );
    }

    @Test
    public void testConnectAll_minimize_instance() {
        // arrange
        final GreatArc a1 = GreatCircles.arcFromPoints(Point2S.PLUS_K, Point2S.PLUS_I, TEST_PRECISION);
        final GreatArc a2 = GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatArc a3 = GreatCircles.arcFromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);

        final GreatArc b1 = GreatCircles.arcFromPoints(Point2S.PLUS_K, Point2S.MINUS_I, TEST_PRECISION);
        final GreatArc b2 = GreatCircles.arcFromPoints(Point2S.MINUS_I, Point2S.MINUS_J, TEST_PRECISION);
        final GreatArc b3 = GreatCircles.arcFromPoints(Point2S.MINUS_J, Point2S.PLUS_K, TEST_PRECISION);

        final InteriorAngleGreatArcConnector connector = new InteriorAngleGreatArcConnector.Minimize();

        // act
        final List<GreatArcPath> paths = connector.connectAll(Arrays.asList(b3, b1, a1, a3, b2, a2));

        // assert
        Assertions.assertEquals(2, paths.size());

        assertPathPoints(paths.get(0),
            Point2S.PLUS_K,
            Point2S.MINUS_I,
            Point2S.MINUS_J,
            Point2S.PLUS_K
        );

        assertPathPoints(paths.get(1),
            Point2S.PLUS_K,
            Point2S.PLUS_I,
            Point2S.PLUS_J,
            Point2S.PLUS_K
        );
    }

    @Test
    public void testConnectAll_minimize_method() {
        // arrange
        final GreatArc a1 = GreatCircles.arcFromPoints(Point2S.PLUS_K, Point2S.PLUS_I, TEST_PRECISION);
        final GreatArc a2 = GreatCircles.arcFromPoints(Point2S.PLUS_I, Point2S.PLUS_J, TEST_PRECISION);
        final GreatArc a3 = GreatCircles.arcFromPoints(Point2S.PLUS_J, Point2S.PLUS_K, TEST_PRECISION);

        final GreatArc b1 = GreatCircles.arcFromPoints(Point2S.PLUS_K, Point2S.MINUS_I, TEST_PRECISION);
        final GreatArc b2 = GreatCircles.arcFromPoints(Point2S.MINUS_I, Point2S.MINUS_J, TEST_PRECISION);
        final GreatArc b3 = GreatCircles.arcFromPoints(Point2S.MINUS_J, Point2S.PLUS_K, TEST_PRECISION);

        // act
        final List<GreatArcPath> paths = InteriorAngleGreatArcConnector.connectMinimized(
                Arrays.asList(b3, b1, a1, a3, b2, a2));

        // assert
        Assertions.assertEquals(2, paths.size());

        assertPathPoints(paths.get(0),
            Point2S.PLUS_K,
            Point2S.MINUS_I,
            Point2S.MINUS_J,
            Point2S.PLUS_K
        );

        assertPathPoints(paths.get(1),
            Point2S.PLUS_K,
            Point2S.PLUS_I,
            Point2S.PLUS_J,
            Point2S.PLUS_K
        );
    }

    /**
     * Run the given consumer function twice, once with a Maximize instance and once with
     * a Minimize instance.
     */
    private static void runWithMaxAndMin(final Consumer<? super InteriorAngleGreatArcConnector> body) {
        body.accept(new Maximize());
        body.accept(new Minimize());
    }

    private static void assertPathPoints(final GreatArcPath path, final Point2S... points) {
        final List<Point2S> expectedPoints = Arrays.asList(points);
        final List<Point2S> actualPoints = path.getVertices();

        final String msg = "Expected path points to equal " + expectedPoints + " but was " + actualPoints;
        Assertions.assertEquals(expectedPoints.size(), actualPoints.size(), msg);

        for (int i = 0; i < expectedPoints.size(); ++i) {
            SphericalTestUtils.assertPointsEq(expectedPoints.get(i), actualPoints.get(i), TEST_EPS);
        }
    }
}
