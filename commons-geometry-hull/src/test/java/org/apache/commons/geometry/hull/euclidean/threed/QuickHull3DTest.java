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
package org.apache.commons.geometry.hull.euclidean.threed;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QuickHull3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION = Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private QuickHull3D generator;

    @BeforeEach
    public void setUp() {
        generator = new QuickHull3D(TEST_PRECISION);
    }

    /**
     * A Hull with less than four points is degenerate.
     */
    @Test
    void lessThanFourPoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.of(0, 0, 0), Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0));
        ConvexHull3D hull = generator.generate(vertices);
        assertNotNull(hull);
        assertNull(hull.getRegion());
        assertTrue(hull.getFacets().isEmpty());
        assertTrue(vertices.equals(hull.getVertices()));
    }

    /**
     * A Hull with less than four points is degenerate.
     */
    @Test
    void samePoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.ZERO, Vector3D.ZERO, Vector3D.ZERO);
        ConvexHull3D hull = generator.generate(vertices);
        assertNotNull(hull);
        assertNull(hull.getRegion());
        assertTrue(hull.getFacets().isEmpty());
        assertTrue(vertices.equals(hull.getVertices()));
    }

    @Test
    void colinearPoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(2, 0, 0), Vector3D.of(3, 0, 0));
        ConvexHull3D hull = generator.generate(
                vertices);
        assertNotNull(hull);
        assertNull(hull.getRegion());
        assertTrue(hull.getFacets().isEmpty());
        assertTrue(vertices.equals(hull.getVertices()));
    }

    @Test
    void coplanarPoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0), Vector3D.of(3, 0, 0));
        ConvexHull3D hull = generator.generate(
                vertices);
        assertNotNull(hull);
        assertNull(hull.getRegion());
        assertTrue(hull.getFacets().isEmpty());
        assertTrue(vertices.equals(hull.getVertices()));
    }

}
