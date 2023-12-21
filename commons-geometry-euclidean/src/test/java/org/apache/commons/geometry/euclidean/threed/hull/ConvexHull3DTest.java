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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.geometry.euclidean.EuclideanCollections;
import org.apache.commons.geometry.euclidean.threed.ConvexPolygon3D;
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
     * A hull with less than four points is degenerate.
     */
    @Test
    void lessThanFourPoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.of(0, 0, 0), Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        checkDegenerateHull(hull, vertices);
    }

    /**
     * A Hull with less than four points is degenerate.
     */
    @Test
    void samePoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.ZERO, Vector3D.ZERO, Vector3D.ZERO);
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        checkDegenerateHull(hull, vertices);
    }

    @Test
    void collinearPoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(2, 0, 0),
                Vector3D.of(3, 0, 0));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        checkDegenerateHull(hull, vertices);
    }

    @Test
    void coplanarPoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(3, 0, 0));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        checkDegenerateHull(hull, vertices);
    }

    @Test
    void simplex() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        checkHull(hull, vertices);
        assertTrue(TEST_PRECISION.eq(1.0 / 6.0, hull.getRegion().getSize()));
        assertEquals(4, hull.getFacets().size());
    }

    @Test
    void simplexPlusPoint() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 1));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        checkHull(hull, vertices);
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
        checkHull(hull, vertices);
        assertTrue(TEST_PRECISION.eq(1.0, hull.getRegion().getSize()));
        assertEquals(12, hull.getFacets().size());
    }

    @Test
    void unitCubeSequentially() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 0), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1),
                Vector3D.of(1, 1, 1));
        vertices.forEach(builder::append);
        ConvexHull3D hull = builder.build();
        checkHull(hull, vertices);
        assertTrue(TEST_PRECISION.eq(1.0, hull.getRegion().getSize()));
        assertEquals(12, hull.getFacets().size());
    }

    @Test
    void multiplePoints() {
        List<Vector3D> vertices = Arrays.asList(Vector3D.ZERO, Vector3D.of(1, 0, 0), Vector3D.of(0, 1, 0),
                Vector3D.of(0, 0, 1), Vector3D.of(1, 1, 0), Vector3D.of(1, 0, 1), Vector3D.of(0, 1, 1),
                Vector3D.of(1, 1, 1), Vector3D.of(10, 20, 30), Vector3D.of(-0.5, 0, 5));
        builder.append(vertices);
        ConvexHull3D hull = builder.build();
        checkHull(hull, vertices);
        assertTrue(TEST_PRECISION.eq(42.58333333333329, hull.getRegion().getSize()));
        assertEquals(14, hull.getFacets().size());
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
        }
        checkHull(hull, vertices);
        assertEquals(1000, hull.getVertices().size());
        assertEquals(1996, hull.getFacets().size());
    }

    @Test
    void randomPoints() {
        Set<Vector3D> set = createRandomPoints(100000, false);
        builder.append(set);
        ConvexHull3D hull = builder.build();
        checkHull(hull, set);
        assertEquals(376, hull.getFacets().size());
    }

    @Test
    void randomPointsInTwoSets() {
        Set<Vector3D> set1 = createRandomPoints(50000, false);
        Set<Vector3D> set2 = createRandomPoints(50000, false);
        builder.append(set1);
        builder.append(set2);
        ConvexHull3D hull = builder.build();
        checkHull(hull, set1);
        checkHull(hull, set2);
        assertEquals(376, hull.getFacets().size());
    }

    @Test
    void randomPointsSequentially() {
        // Points are added sequentially
        List<Vector3D> list = new ArrayList<>(createRandomPoints(100, false));
        list.forEach(builder::append);
        ConvexHull3D hull = builder.build();
        checkHull(hull, list);
        assertEquals(70, hull.getFacets().size());
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

    /**
     * Check if the hull contains all the points in the given collection and checks if the volume is finite and
     * non-zero.
     */
    private void checkHull(ConvexHull3D hull, Collection<Vector3D> points) {
        ConvexVolume region = hull.getRegion();
        assertNotNull(region);
        assertTrue(region.isFinite());
        assertFalse(region.isEmpty());
        assertFalse(hull.isDegenerate());
        for (Vector3D p : points) {
            assertTrue(region.contains(p));
        }
        checkFacets(hull);
    }

    private void checkFacets(ConvexHull3D hull) {
        List<ConvexPolygon3D> polygons = hull.getFacets();
        assertFalse(polygons.isEmpty());

        // Build an edge map to check if every facet has a neighbor and all edges share two facets.
        Vector3D centroid = hull.getRegion().getCentroid();
        Set<ConvexHull3D.Facet> facets = polygons.stream().map(p -> new ConvexHull3D.Facet(p, centroid, TEST_PRECISION))
                .collect(Collectors.toSet());

        //Populate edgeMap.
        Map<ConvexHull3D.Edge, ConvexHull3D.Facet> edgeMap = new HashMap<>();
        for (ConvexHull3D.Facet f : facets) {
            for (ConvexHull3D.Edge e : f.getEdges()) {
                edgeMap.put(e, f);
            }
        }

        //Check if all edges are shared by two facets.
        for (ConvexHull3D.Facet f : facets) {
            for (ConvexHull3D.Edge e : f.getEdges()) {
                assertTrue(edgeMap.containsKey(e.getInverse()));
            }
        }
    }

    private void checkDegenerateHull(ConvexHull3D hull, Collection<Vector3D> points) {
        assertTrue(hull.isDegenerate());
        assertNull(hull.getRegion());
        assertTrue(hull.getFacets().isEmpty());
        List<Vector3D> vertices = hull.getVertices();
        for (Vector3D p : points) {
            assertTrue(vertices.contains(p));
        }
    }

}
