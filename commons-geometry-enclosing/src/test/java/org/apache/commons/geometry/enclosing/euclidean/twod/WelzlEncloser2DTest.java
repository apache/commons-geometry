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
package org.apache.commons.geometry.enclosing.euclidean.twod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.enclosing.EnclosingBall;
import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.Assert;
import org.junit.Test;

public class WelzlEncloser2DTest {

    private static final double TEST_EPS = 1e-10;

    private static final DoublePrecisionContext TEST_PRECISION =
            new EpsilonDoublePrecisionContext(TEST_EPS);

    private final WelzlEncloser2D encloser = new WelzlEncloser2D(TEST_PRECISION);

    @Test
    public void testNoPoints() {
        // arrange
        final String msg = "Unable to generate enclosing ball: no points given";

        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            encloser.enclose(null);
        }, IllegalArgumentException.class, msg);

        GeometryTestUtils.assertThrows(() -> {
            encloser.enclose(new ArrayList<Vector2D>());
        }, IllegalArgumentException.class, msg);
    }

    @Test
    public void testRegularPoints() {
        // arrange
        final List<Vector2D> list = buildList(22, 26, 30, 38, 64, 28,  8, 54, 11, 15);

        // act/assert
        checkDisk(list, Arrays.asList(list.get(2), list.get(3), list.get(4)));
    }

    @Test
    public void testSolutionOnDiameter() {
        // arrange
        final List<Vector2D> list = buildList(22, 26, 30, 38, 64, 28,  8, 54);

        // act/assert
        checkDisk(list, Arrays.asList(list.get(2), list.get(3)));
    }

    @Test
    public void testReducingBall1() {
        // arrange
        final List<Vector2D> list = buildList(0.05380958511396061, 0.57332359658700000,
                                        0.99348810731127870, 0.02056421361521466,
                                        0.01203950647796437, 0.99779675042261860,
                                        0.00810189987706078, 0.00589246003827815,
                                        0.00465180821202149, 0.99219972923046940);

        // act/assert
        checkDisk(list, Arrays.asList(list.get(1), list.get(3), list.get(4)));
    }

    @Test
    public void testReducingBall2() {
        // arrange
        final List<Vector2D> list = buildList(0.016930586154703, 0.333955448537779,
                                        0.987189104892331, 0.969778855274507,
                                        0.983696889599935, 0.012904580013266,
                                        0.013114499572905, 0.034740156356895);

        // act/assert
        checkDisk(list, Arrays.asList(list.get(1), list.get(2), list.get(3)));
    }

    @Test
    public void testLargeSamples() {
        // arrange
        final UniformRandomProvider random = RandomSource.create(RandomSource.WELL_1024_A, 0xa2a63cad12c01fb2L);
        for (int k = 0; k < 100; ++k) {
            final int nbPoints = random.nextInt(10000);
            final List<Vector2D> points = new ArrayList<>();
            for (int i = 0; i < nbPoints; ++i) {
                final double x = random.nextDouble();
                final double y = random.nextDouble();
                points.add(Vector2D.of(x, y));
            }

            // act/assert
            checkDisk(points);
        }
    }

    @Test
    public void testEnclosingWithPrecision() {
        // arrange
        final List<Vector2D> points = Arrays.asList(
                Vector2D.of(271.59, 57.282),
                Vector2D.of(269.145, 57.063),
                Vector2D.of(309.117, 77.187),
                Vector2D.of(316.989, 34.835),
                Vector2D.of(323.101, 53.972)
        );
        final double precision = 1;
        final DoublePrecisionContext precisionContext = new EpsilonDoublePrecisionContext(precision);
        final WelzlEncloser2D customPrecisionEncloser = new WelzlEncloser2D(precisionContext);

        // act
        final EnclosingBall<Vector2D> result = customPrecisionEncloser.enclose(points);

        // assert
        Assert.assertEquals(27.099954200964234, result.getRadius(), TEST_EPS);
        EuclideanTestUtils.assertCoordinatesEqual(Vector2D.of(296.0056977503686, 53.469890753441945),
                result.getCenter(), TEST_EPS);
    }

    private List<Vector2D> buildList(final double... coordinates) {
        final List<Vector2D> list = new ArrayList<>(coordinates.length / 2);
        for (int i = 0; i < coordinates.length; i += 2) {
            list.add(Vector2D.of(coordinates[i], coordinates[i + 1]));
        }
        return list;
    }

    private void checkDisk(final List<Vector2D> points, final List<Vector2D> refSupport) {

        final EnclosingBall<Vector2D> disk = checkDisk(points);

        // compare computed disk with expected disk
        final EnclosingBall<Vector2D> expected = new DiskGenerator().ballOnSupport(refSupport);
        Assert.assertEquals(refSupport.size(), disk.getSupportSize());
        Assert.assertEquals(expected.getRadius(),        disk.getRadius(),        1.0e-10);
        Assert.assertEquals(expected.getCenter().getX(), disk.getCenter().getX(), 1.0e-10);
        Assert.assertEquals(expected.getCenter().getY(), disk.getCenter().getY(), 1.0e-10);

        for (final Vector2D s : disk.getSupport()) {
            boolean found = false;
            for (final Vector2D rs : refSupport) {
                if (s == rs) {
                    found = true;
                }
            }
            Assert.assertTrue(found);
        }

        // check removing any point of the support disk fails to enclose the point
        for (int i = 0; i < disk.getSupportSize(); ++i) {
            final List<Vector2D> reducedSupport = new ArrayList<>();
            int count = 0;
            for (final Vector2D s : disk.getSupport()) {
                if (count++ != i) {
                    reducedSupport.add(s);
                }
            }
            final EnclosingBall<Vector2D> reducedDisk = new DiskGenerator().ballOnSupport(reducedSupport);
            boolean foundOutside = false;
            for (int j = 0; j < points.size() && !foundOutside; ++j) {
                if (!reducedDisk.contains(points.get(j), TEST_PRECISION)) {
                    foundOutside = true;
                }
            }
            Assert.assertTrue(foundOutside);
        }
    }

    private EnclosingBall<Vector2D> checkDisk(final List<Vector2D> points) {

        final EnclosingBall<Vector2D> disk = encloser.enclose(points);

        // all points are enclosed
        for (final Vector2D v : points) {
            Assert.assertTrue(disk.contains(v, TEST_PRECISION));
        }

        // all support points are on the boundary
        final Vector2D center = disk.getCenter();
        final double radius = disk.getRadius();

        for (final Vector2D s : disk.getSupport()) {
            Assert.assertTrue(TEST_PRECISION.eqZero(center.distance(s) - radius));
        }

        return disk;
    }
}
