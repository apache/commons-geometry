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

package org.apache.commons.geometry.euclidean.threed.hull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.geometry.euclidean.EuclideanCollections;
import org.apache.commons.geometry.euclidean.threed.ConvexVolume;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConvexHull3DTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION = Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private ConvexHull3D.Builder builder;

    private UniformRandomProvider random;

    @BeforeEach
    public void setUp() {
        builder = new ConvexHull3D.Builder(TEST_PRECISION);
        random = RandomSource.XO_SHI_RO_256_PP.create(10);
    }

    /**
     * A Hull with less than four points is degenerate.
     */
    @Test
    void lessThanFourPoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.of(0, 0, 0), Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        assertNotNull(hull);
        assertNull(hull.getRegion());
        assertTrue(hull.getFacets().isEmpty());
        assertEquals(vertices, hull.getVertices());
    }

    /**
     * A Hull with less than four points is degenerate.
     */
    @Test
    void samePoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.ZERO, Vector3D.ZERO, Vector3D.ZERO);
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        assertNotNull(hull);
        assertNull(hull.getRegion());
        assertTrue(hull.getFacets().isEmpty());
        List<Vector3D> hullVertices = hull.getVertices();
        assertEquals(1, hullVertices.size());
        assertTrue(hullVertices.contains(Vector3D.ZERO));
    }

    @Test
    void collinearPoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(2, 0, 0),
                Vector3D.of(3, 0, 0));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        assertNotNull(hull);
        assertNull(hull.getRegion());
        assertTrue(hull.getFacets().isEmpty());
        assertEquals(vertices, hull.getVertices());
    }

    @Test
    void coplanarPoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(3, 0, 0));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        assertNotNull(hull);
        assertNull(hull.getRegion());
        assertTrue(hull.getFacets().isEmpty());
        assertEquals(vertices, hull.getVertices());
    }

    @Test
    void simplex() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        assertNotNull(hull.getRegion());
        assertTrue(hull.getRegion().contains(Vector3D.ZERO));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 0, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 1, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 0, 1)));
        // The size of the simplex is finite and non-zero.
        assertTrue(TEST_PRECISION.eq(1.0 / 6.0, hull.getRegion().getSize()));
        assertEquals(4, hull.getFacets().size());
    }

    @Test
    void simplexPlusPoint() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 1));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        assertNotNull(hull.getRegion());
        assertTrue(hull.getRegion().contains(Vector3D.ZERO));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 0, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 1, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 0, 1)));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 1, 1)));
        assertTrue(TEST_PRECISION.eq(1.0 / 2.0, hull.getRegion().getSize()));
        assertEquals(6, hull.getFacets().size());
    }

    @Test
    void unitCube() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 0), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1),
                Vector3D.of(1, 1, 1));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        assertNotNull(hull.getRegion());
        assertTrue(hull.getRegion().contains(Vector3D.ZERO));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 0, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 1, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 0, 1)));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 1, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 0, 1)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 1, 1)));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 1, 1)));
        assertTrue(TEST_PRECISION.eq(1.0, hull.getRegion().getSize()));
    }

    @Test
    void unitCubeSequentially() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 0), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1),
                Vector3D.of(1, 1, 1));
        vertices.forEach(builder::append);
        ConvexHull3D hull = builder.build();
        assertNotNull(hull.getRegion());
        assertTrue(hull.getRegion().contains(Vector3D.ZERO));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 0, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 1, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 0, 1)));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 1, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 0, 1)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 1, 1)));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 1, 1)));
        assertTrue(TEST_PRECISION.eq(1.0, hull.getRegion().getSize()));
    }

    @Test
    void multiplePoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 0), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1),
                Vector3D.of(1, 1, 1), Vector3D.of(10, 20, 30), Vector3D.of(-0.5, 0, 5));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        assertNotNull(hull.getRegion());
        assertTrue(hull.getRegion().contains(Vector3D.ZERO));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 0, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 1, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 0, 1)));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 1, 0)));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 0, 1)));
        assertTrue(hull.getRegion().contains(Vector3D.of(0, 1, 1)));
        assertTrue(hull.getRegion().contains(Vector3D.of(1, 1, 1)));
        assertTrue(hull.getRegion().contains(Vector3D.of(10, 20, 30)));
        assertTrue(hull.getRegion().contains(Vector3D.of(-0.5, 0, 5)));
        assertTrue(TEST_PRECISION.eq(42.58333333333329, hull.getRegion().getSize()));
    }

    /**
     * Create 1000 points on a unit sphere. Then every point of the set must be a
     * vertex of the hull.
     */
    @Test
    void randomUnitPoints() {
        // All points in the set must be on the hull. This is a worst case scenario.
        Set<Vector3D> set = createRandomPoints(1000, true);
        builder.append(set);
        ConvexHull3D hull = builder.build();
        ConvexVolume region = hull.getRegion();
        assertNotNull(region);
        List<Vector3D> vertices = hull.getVertices();
        for (Vector3D p : set) {
            assertTrue(vertices.contains(p));
            assertTrue(region.contains(p));
        }
        assertEquals(1000, hull.getVertices().size());
    }

    @Test
    void randomPoints() {
        Set<Vector3D> set = createRandomPoints(100000, false);
        builder.append(set);
        ConvexHull3D hull = builder.build();
        ConvexVolume region = hull.getRegion();
        assertNotNull(region);
        for (Vector3D p : set) {
            assertTrue(region.contains(p));
        }
    }

    @Test
    void randomPointsInTwoSets() {
        Set<Vector3D> set1 = createRandomPoints(50000, false);
        Set<Vector3D> set2 = createRandomPoints(50000, false);
        builder.append(set1);
        builder.append(set2);
        ConvexHull3D hull = builder.build();
        ConvexVolume region = hull.getRegion();
        assertNotNull(region);
        for (Vector3D p : set1) {
            assertTrue(region.contains(p));
        }
        for (Vector3D p : set2) {
            assertTrue(region.contains(p));
        }
    }

    @Test
    void randomPointsSequentially() {
        // Points are added sequentially
        List<Vector3D> list = new ArrayList<>(createRandomPoints(100, false));
        list.forEach(builder::append);
        ConvexHull3D hull = builder.build();
        ConvexVolume region = hull.getRegion();
        assertNotNull(region);
        for (int i = 0; i < 100; i++) {
            Vector3D p = list.get(i);
            assertTrue(region.contains(p), String.format("The Vector with position %d is different.", i));
        }
    }

    /**
     * Create a specified number of random points on the unit sphere.
     *
     * @param number    the given number.
     * @param normalize normalize the output points.
     * @return a specified number of random points on the unit sphere.
     */
    private Set<Vector3D> createRandomPoints(int number, boolean normalize) {
        Set<Vector3D> set = EuclideanCollections.pointSet3D(TEST_PRECISION);
        for (int i = 0; i < number; i++) {
            if (normalize) {
                set.add(Vector3D.Unit.from(random.nextDouble(), random.nextDouble(), random.nextDouble()));
            } else {
                set.add(Vector3D.of(random.nextDouble(), random.nextDouble(), random.nextDouble()));
            }
        }
        return set;
    }

}
