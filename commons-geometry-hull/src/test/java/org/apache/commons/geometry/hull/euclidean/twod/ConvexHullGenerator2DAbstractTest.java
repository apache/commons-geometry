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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.core.Region;
import org.apache.commons.geometry.core.RegionLocation;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.euclidean.twod.ConvexArea;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.arrays.LinearCombination;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract base test class for 2D convex hull generators.
 */
public abstract class ConvexHullGenerator2DAbstractTest {

    protected static final double TEST_EPS = 1e-10;

    protected static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    protected ConvexHullGenerator2D generator;

    protected UniformRandomProvider random;

    protected abstract ConvexHullGenerator2D createConvexHullGenerator(boolean includeCollinearPoints);

    protected Collection<Vector2D> reducePoints(Collection<Vector2D> points) {
        // do nothing by default, may be overridden by other tests
        return points;
    }

    @Before
    public void setUp() {
        // by default, do not include collinear points
        generator = createConvexHullGenerator(false);
        random = RandomSource.create(RandomSource.MT, 10);
    }

    // ------------------------------------------------------------------------------

    @Test
    public void testEmpty() {
        // act
        ConvexHull2D hull = generator.generate(Collections.emptyList());

        // assert
        Assert.assertEquals(0, hull.getVertices().size());
        Assert.assertEquals(0, hull.getPath().getSubLines().size());
        Assert.assertNull(hull.getRegion());
    }

    @Test
    public void testOnePoint() {
        // arrange
        List<Vector2D> points = createRandomPoints(1);

        // act
        ConvexHull2D hull = generator.generate(points);

        // assert
        Assert.assertEquals(1, hull.getVertices().size());
        Assert.assertEquals(0, hull.getPath().getSubLines().size());
        Assert.assertNull(hull.getRegion());
    }

    @Test
    public void testTwoPoints() {
        // arrange
        List<Vector2D> points = createRandomPoints(2);

        // act
        ConvexHull2D hull = generator.generate(points);

        // assert
        Assert.assertEquals(2, hull.getVertices().size());
        Assert.assertEquals(1, hull.getPath().getSubLines().size());
        Assert.assertNull(hull.getRegion());
    }

    @Test
    public void testAllIdentical() {
        // arrange
        final Collection<Vector2D> points = new ArrayList<>();
        points.add(Vector2D.of(1, 1));
        points.add(Vector2D.of(1, 1));
        points.add(Vector2D.of(1, 1));
        points.add(Vector2D.of(1, 1));

        // act
        final ConvexHull2D hull = generator.generate(points);

        // assert
        Assert.assertEquals(1, hull.getVertices().size());
        Assert.assertEquals(0, hull.getPath().getSubLines().size());
        Assert.assertNull(hull.getRegion());
    }

    @Test
    public void testConvexHull() {
        // execute 100 random variations
        for (int i = 0; i < 100; i++) {
            // randomize the size from 4 to 100
            int size = (int) Math.floor(random.nextDouble() * 96.0 + 4.0);

            List<Vector2D> points = createRandomPoints(size);

            // act
            ConvexHull2D hull = generator.generate(reducePoints(points));

            // assert
            checkConvexHull(points, hull);
        }
    }

    @Test
    public void testCollinearPoints() {
        // arrange
        final Collection<Vector2D> points = new ArrayList<>();
        points.add(Vector2D.of(1, 1));
        points.add(Vector2D.of(2, 2));
        points.add(Vector2D.of(2, 4));
        points.add(Vector2D.of(4, 1));
        points.add(Vector2D.of(10, 1));

        // act
        final ConvexHull2D hull = generator.generate(points);

        // assert
        checkConvexHull(points, hull);
    }

    @Test
    public void testCollinearPointsReverse() {
        // arrange
        final Collection<Vector2D> points = new ArrayList<>();
        points.add(Vector2D.of(1, 1));
        points.add(Vector2D.of(2, 2));
        points.add(Vector2D.of(2, 4));
        points.add(Vector2D.of(10, 1));
        points.add(Vector2D.of(4, 1));

        // act
        final ConvexHull2D hull = generator.generate(points);

        // assert
        checkConvexHull(points, hull);
    }

    @Test
    public void testCollinearPointsIncluded() {
        // arrange
        final Collection<Vector2D> points = new ArrayList<>();
        points.add(Vector2D.of(1, 1));
        points.add(Vector2D.of(2, 2));
        points.add(Vector2D.of(2, 4));
        points.add(Vector2D.of(4, 1));
        points.add(Vector2D.of(10, 1));

        // act
        final ConvexHull2D hull = createConvexHullGenerator(true).generate(points);

        // assert
        checkConvexHull(points, hull, true);
    }

    @Test
    public void testCollinearPointsIncludedReverse() {
        // arrange
        final Collection<Vector2D> points = new ArrayList<>();
        points.add(Vector2D.of(1, 1));
        points.add(Vector2D.of(2, 2));
        points.add(Vector2D.of(2, 4));
        points.add(Vector2D.of(10, 1));
        points.add(Vector2D.of(4, 1));

        // act
        final ConvexHull2D hull = createConvexHullGenerator(true).generate(points);

        // assert
        checkConvexHull(points, hull, true);
    }

    @Test
    public void testIdenticalPoints() {
        // arrange
        final Collection<Vector2D> points = new ArrayList<>();
        points.add(Vector2D.of(1, 1));
        points.add(Vector2D.of(2, 2));
        points.add(Vector2D.of(2, 4));
        points.add(Vector2D.of(4, 1));
        points.add(Vector2D.of(1, 1));

        // act
        final ConvexHull2D hull = generator.generate(points);

        // assert
        checkConvexHull(points, hull);
    }

    @Test
    public void testIdenticalPoints2() {
        // arrange
        final Collection<Vector2D> points = new ArrayList<>();
        points.add(Vector2D.of(1, 1));
        points.add(Vector2D.of(2, 2));
        points.add(Vector2D.of(2, 4));
        points.add(Vector2D.of(4, 1));
        points.add(Vector2D.of(1, 1));

        // act
        final ConvexHull2D hull = createConvexHullGenerator(true).generate(points);

        // assert
        checkConvexHull(points, hull, true);
    }

    @Test
    public void testClosePoints() {
        // arrange
        final Collection<Vector2D> points = new ArrayList<>();
        points.add(Vector2D.of(1, 1));
        points.add(Vector2D.of(2, 2));
        points.add(Vector2D.of(2, 4));
        points.add(Vector2D.of(4, 1));
        points.add(Vector2D.of(1.00001, 1));

        // act
        final ConvexHull2D hull = generator.generate(points);

        // assert
        checkConvexHull(points, hull);
    }

    @Test
    public void testCollinearPointOnExistingBoundary() {
        // --- arrange
        // MATH-1135: check that collinear points on the hull are handled correctly
        //            when only a minimal hull shall be constructed
        final Collection<Vector2D> points = new ArrayList<>();
        points.add(Vector2D.of(7.3152, 34.7472));
        points.add(Vector2D.of(6.400799999999997, 34.747199999999985));
        points.add(Vector2D.of(5.486399999999997, 34.7472));
        points.add(Vector2D.of(4.876799999999999, 34.7472));
        points.add(Vector2D.of(4.876799999999999, 34.1376));
        points.add(Vector2D.of(4.876799999999999, 30.48));
        points.add(Vector2D.of(6.0959999999999965, 30.48));
        points.add(Vector2D.of(6.0959999999999965, 34.1376));
        points.add(Vector2D.of(7.315199999999996, 34.1376));
        points.add(Vector2D.of(7.3152, 30.48));

        // --- act
        final ConvexHull2D hull = createConvexHullGenerator(false).generate(points);

        // --- assert
        checkConvexHull(points, hull);
    }

    @Test
    public void testCollinearPointsInAnyOrder_threeCollinearPoints() {
        // --- arrange
        // MATH-1148: collinear points on the hull might be in any order
        //            make sure that they are processed in the proper order
        //            for each algorithm.

        List<Vector2D> points = new ArrayList<>();
        points.add(Vector2D.of(16.078200000000184, -36.52519999989808));
        points.add(Vector2D.of(19.164300000000186, -36.52519999989808));
        points.add(Vector2D.of(19.1643, -25.28136477910407));
        points.add(Vector2D.of(19.1643, -17.678400000004157));

        // --- act/assert
        ConvexHull2D hull = createConvexHullGenerator(false).generate(points);
        checkConvexHull(points, hull);

        hull = createConvexHullGenerator(true).generate(points);
        checkConvexHull(points, hull, true);
    }

    @Test
    public void testCollinearPointsInAnyOrder_multipleCollinearPoints() {
        // --- arrange
        // MATH-1148: collinear points on the hull might be in any order
        //            make sure that they are processed in the proper order
        //            for each algorithm.

        List<Vector2D> points = new ArrayList<>();
        points.add(Vector2D.of(0, -29.959696875));
        points.add(Vector2D.of(0, -31.621809375));
        points.add(Vector2D.of(0, -28.435696875));
        points.add(Vector2D.of(0, -33.145809375));
        points.add(Vector2D.of(3.048, -33.145809375));
        points.add(Vector2D.of(3.048, -31.621809375));
        points.add(Vector2D.of(3.048, -29.959696875));
        points.add(Vector2D.of(4.572, -33.145809375));
        points.add(Vector2D.of(4.572, -28.435696875));

        // --- act/assert
        ConvexHull2D hull = createConvexHullGenerator(false).generate(points);
        checkConvexHull(points, hull);

        hull = createConvexHullGenerator(true).generate(points);
        checkConvexHull(points, hull, true);
    }

    @Test
    public void testIssue1123() {
        // arrange
        List<Vector2D> points = new ArrayList<>();

        int[][] data = new int[][] {
                {-11, -1}, {-11, 0}, {-11, 1},
                {-10, -3}, {-10, -2}, {-10, -1}, {-10, 0}, {-10, 1},
                {-10, 2}, {-10, 3}, {-9, -4}, {-9, -3}, {-9, -2},
                {-9, -1}, {-9, 0}, {-9, 1}, {-9, 2}, {-9, 3},
                {-9, 4}, {-8, -5}, {-8, -4}, {-8, -3}, {-8, -2},
                {-8, -1}, {-8, 0}, {-8, 1}, {-8, 2}, {-8, 3},
                {-8, 4}, {-8, 5}, {-7, -6}, {-7, -5}, {-7, -4},
                {-7, -3}, {-7, -2}, {-7, -1}, {-7, 0}, {-7, 1},
                {-7, 2}, {-7, 3}, {-7, 4}, {-7, 5}, {-7, 6},
                {-6, -7}, {-6, -6}, {-6, -5}, {-6, -4}, {-6, -3},
                {-6, -2}, {-6, -1}, {-6, 0}, {-6, 1}, {-6, 2},
                {-6, 3}, {-6, 4}, {-6, 5}, {-6, 6}, {-6, 7},
                {-5, -7}, {-5, -6}, {-5, -5}, {-5, -4}, {-5, -3},
                {-5, -2}, {-5, 4}, {-5, 5}, {-5, 6}, {-5, 7},
                {-4, -7}, {-4, -6}, {-4, -5}, {-4, -4}, {-4, -3},
                {-4, -2}, {-4, 4}, {-4, 5}, {-4, 6}, {-4, 7},
                {-3, -8}, {-3, -7}, {-3, -6}, {-3, -5}, {-3, -4},
                {-3, -3}, {-3, -2}, {-3, 4}, {-3, 5}, {-3, 6},
                {-3, 7}, {-3, 8}, {-2, -8}, {-2, -7}, {-2, -6},
                {-2, -5}, {-2, -4}, {-2, -3}, {-2, -2}, {-2, 4},
                {-2, 5}, {-2, 6}, {-2, 7}, {-2, 8}, {-1, -8},
                {-1, -7}, {-1, -6}, {-1, -5}, {-1, -4}, {-1, -3},
                {-1, -2}, {-1, 4}, {-1, 5}, {-1, 6}, {-1, 7},
                {-1, 8}, {0, -8}, {0, -7}, {0, -6}, {0, -5},
                {0, -4}, {0, -3}, {0, -2}, {0, 4}, {0, 5}, {0, 6},
                {0, 7}, {0, 8}, {1, -8}, {1, -7}, {1, -6}, {1, -5},
                {1, -4}, {1, -3}, {1, -2}, {1, -1}, {1, 0}, {1, 1},
                {1, 2}, {1, 3}, {1, 4}, {1, 5}, {1, 6}, {1, 7},
                {1, 8}, {2, -8}, {2, -7}, {2, -6}, {2, -5},
                {2, -4}, {2, -3}, {2, -2}, {2, -1}, {2, 0}, {2, 1},
                {2, 2}, {2, 3}, {2, 4}, {2, 5}, {2, 6}, {2, 7},
                {2, 8}, {3, -8}, {3, -7}, {3, -6}, {3, -5},
                {3, -4}, {3, -3}, {3, -2}, {3, -1}, {3, 0}, {3, 1},
                {3, 2}, {3, 3}, {3, 4}, {3, 5}, {3, 6}, {3, 7},
                {3, 8}, {4, -7}, {4, -6}, {4, -5}, {4, -4},
                {4, -3}, {4, -2}, {4, -1}, {4, 0}, {4, 1}, {4, 2},
                {4, 3}, {4, 4}, {4, 5}, {4, 6}, {4, 7}, {5, -7},
                {5, -6}, {5, -5}, {5, -4}, {5, -3}, {5, -2},
                {5, -1}, {5, 0}, {5, 1}, {5, 2}, {5, 3}, {5, 4},
                {5, 5}, {5, 6}, {5, 7}, {6, -7}, {6, -6}, {6, -5},
                {6, -4}, {6, -3}, {6, -2}, {6, -1}, {6, 0}, {6, 1},
                {6, 2}, {6, 3}, {6, 4}, {6, 5}, {6, 6}, {6, 7},
                {7, -6}, {7, -5}, {7, -4}, {7, -3}, {7, -2},
                {7, -1}, {7, 0}, {7, 1}, {7, 2}, {7, 3}, {7, 4},
                {7, 5}, {7, 6}, {8, -5}, {8, -4}, {8, -3}, {8, -2},
                {8, -1}, {8, 0}, {8, 1}, {8, 2}, {8, 3}, {8, 4},
                {8, 5}, {9, -4}, {9, -3}, {9, -2}, {9, -1}, {9, 0},
                {9, 1}, {9, 2}, {9, 3}, {9, 4}, {10, -3}, {10, -2},
                {10, -1}, {10, 0}, {10, 1}, {10, 2}, {10, 3},
                {11, -1}, {11, 0}, {11, 1}
            };

        for (int[] line : data) {
            points.add(Vector2D.of(line[0], line[1]));
        }

        Vector2D[] referenceHull = new Vector2D[] {
            Vector2D.of(-11.0, -1.0),
            Vector2D.of(-10.0, -3.0),
            Vector2D.of(-6.0, -7.0),
            Vector2D.of(-3.0, -8.0),
            Vector2D.of(3.0, -8.0),
            Vector2D.of(6.0, -7.0),
            Vector2D.of(10.0, -3.0),
            Vector2D.of(11.0, -1.0),
            Vector2D.of(11.0, 1.0),
            Vector2D.of(10.0, 3.0),
            Vector2D.of(6.0, 7.0),
            Vector2D.of(3.0, 8.0),
            Vector2D.of(-3.0, 8.0),
            Vector2D.of(-6.0, 7.0),
            Vector2D.of(-10.0, 3.0),
            Vector2D.of(-11.0, 1.0),
        };

        // act
        ConvexHull2D convHull = generator.generate(points);
        Region<Vector2D> hullRegion = convHull.getRegion();

        // assert
        Assert.assertEquals(274.0, hullRegion.getSize(), 1.0e-12);
        double perimeter = 0;
        for (int i = 0; i < referenceHull.length; ++i) {
            perimeter += referenceHull[i].distance(
                                           referenceHull[(i + 1) % referenceHull.length]);
        }
        Assert.assertEquals(perimeter, hullRegion.getBoundarySize(), 1.0e-12);

        for (int i = 0; i < referenceHull.length; ++i) {
            Assert.assertEquals(RegionLocation.BOUNDARY, hullRegion.classify(referenceHull[i]));
        }

    }

    // ------------------------------------------------------------------------------

    protected final List<Vector2D> createRandomPoints(int size) {
        // create the cloud container
        List<Vector2D> points = new ArrayList<>(size);
        // fill the cloud with a random distribution of points
        for (int i = 0; i < size; i++) {
            points.add(Vector2D.of(random.nextDouble() * 2.0 - 1.0, random.nextDouble() * 2.0 - 1.0));
        }
        return points;
    }

    protected final void checkConvexHull(final Collection<Vector2D> points, final ConvexHull2D hull) {
        checkConvexHull(points, hull, false);
    }

    protected final void checkConvexHull(final Collection<Vector2D> points, final ConvexHull2D hull,
                                         final boolean includesCollinearPoints) {
        Assert.assertNotNull(hull);
        Assert.assertTrue(isConvex(hull, includesCollinearPoints));
        checkPointsInsideHullRegion(points, hull, includesCollinearPoints);
    }

    // verify that the constructed hull is really convex
    protected final boolean isConvex(final ConvexHull2D hull, final boolean includesCollinearPoints) {

        final List<Vector2D> points = hull.getVertices();
        int sign = 0;
        int size = points.size();

        for (int i = 0; i < size; i++) {
            Vector2D p1 = points.get(i == 0 ? size - 1 : i - 1);
            Vector2D p2 = points.get(i);
            Vector2D p3 = points.get(i == size - 1 ? 0 : i + 1);

            Vector2D d1 = p2.subtract(p1);
            Vector2D d2 = p3.subtract(p2);

            Assert.assertTrue(d1.norm() > 1e-10);
            Assert.assertTrue(d2.norm() > 1e-10);

            final double cross = LinearCombination.value(d1.getX(), d2.getY(), -d1.getY(), d2.getX());
            final int cmp = Precision.compareTo(cross, 0.0, TEST_EPS);

            if (sign != 0 && cmp != sign) {
                if (!includesCollinearPoints || cmp != 0) {
                    // in case of collinear points the cross product will be zero
                    return false;
                }
            }

            sign = cmp;
        }

        return true;
    }

    // verify that all points are inside the convex hull region
    protected final void checkPointsInsideHullRegion(final Collection<Vector2D> points,
                                                     final ConvexHull2D hull,
                                                     final boolean includesCollinearPoints) {

        final Collection<Vector2D> hullVertices = hull.getVertices();
        final ConvexArea region = hull.getRegion();

        for (final Vector2D p : points) {
            RegionLocation location = region.classify(p);
            Assert.assertNotEquals(RegionLocation.OUTSIDE, location);

            if (location == RegionLocation.BOUNDARY && includesCollinearPoints) {
                Assert.assertTrue(hullVertices.contains(p));
            }
        }
    }
}
