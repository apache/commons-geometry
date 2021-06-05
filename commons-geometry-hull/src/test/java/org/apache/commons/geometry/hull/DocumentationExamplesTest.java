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
package org.apache.commons.geometry.hull;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.geometry.hull.euclidean.twod.ConvexHull2D;
import org.apache.commons.geometry.hull.euclidean.twod.MonotoneChain;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** This class contains code listed as examples in the user guide and other documentation.
 * If any portion of this code changes, the corresponding examples in the documentation <em>must</em> be updated.
 */
public class DocumentationExamplesTest {

    private static final double TEST_EPS = 1e-15;

    @Test
    public void testMonotoneChainExample() {
        final Precision.DoubleEquivalence precision = Precision.doubleEquivalenceOfEpsilon(1e-10);

        // create a list of input points for the algorithm
        final List<Vector2D> pts = Arrays.asList(
                    Vector2D.ZERO,
                    Vector2D.of(0.5, 0.5),
                    Vector2D.of(0, 0.5),
                    Vector2D.of(0, 1),
                    Vector2D.of(0.25, 0.1),
                    Vector2D.of(1, 0),
                    Vector2D.of(1, 1),
                    Vector2D.of(0.75, 0.9)
                );

        // create an instance of the monotone chain convex hull generator
        final MonotoneChain mc = new MonotoneChain(precision);

        // compute the convex hull
        final ConvexHull2D hull = mc.generate(pts);

        // list the vertices from the input that were used in the hull
        final List<Vector2D> vertices = hull.getVertices(); // [(0.0, 0.0), (1.0, 0.0), (1.0, 1.0), (0.0, 1.0)]

        // get the hull as a region
        final ConvexArea region = hull.getRegion();
        final boolean containsAll = pts.stream().allMatch(region::contains); // true - region contains all input points

        // ---
        Assertions.assertEquals(4, vertices.size());
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.ZERO, vertices.get(0), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 0), vertices.get(1), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(1, 1), vertices.get(2), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(0, 1), vertices.get(3), TEST_EPS);

        Assertions.assertTrue(containsAll);
    }
}
