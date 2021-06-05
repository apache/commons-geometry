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
package org.apache.commons.geometry.enclosing.euclidean.threed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.enclosing.EnclosingBall;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.UnitSphereSampler;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SphereGeneratorTest {

    private static final double TEST_EPS = 1e-10;

    private static final Precision.DoubleEquivalence TEST_PRECISION =
            Precision.doubleEquivalenceOfEpsilon(TEST_EPS);

    private final SphereGenerator generator = new SphereGenerator(TEST_PRECISION);

    @Test
    public void testSupport0Point() {
        // arrange
        final List<Vector3D> support = Collections.emptyList();

        // act
        final EnclosingBall<Vector3D> sphere = generator.ballOnSupport(support);

        // assert
        Assertions.assertTrue(sphere.getRadius() < 0);
        Assertions.assertEquals(0, sphere.getSupportSize());
        Assertions.assertEquals(0, sphere.getSupport().size());
    }

    @Test
    public void testSupport1Point() {
        // arrange
        final Precision.DoubleEquivalence lowPrecision = Precision.doubleEquivalenceOfEpsilon(0.5);
        final Precision.DoubleEquivalence highPrecision = Precision.doubleEquivalenceOfEpsilon(0.001);
        final List<Vector3D> support = Collections.singletonList(Vector3D.of(1, 2, 3));

        // act
        final EnclosingBall<Vector3D> sphere = generator.ballOnSupport(support);

        // assert
        Assertions.assertEquals(0.0, sphere.getRadius(), TEST_EPS);

        Assertions.assertTrue(sphere.contains(support.get(0)));
        Assertions.assertTrue(sphere.contains(support.get(0), lowPrecision));
        Assertions.assertFalse(sphere.contains(Vector3D.of(support.get(0).getX() + 0.1,
                                                        support.get(0).getY() + 0.1,
                                                        support.get(0).getZ() + 0.1),
                                            highPrecision));
        Assertions.assertTrue(sphere.contains(Vector3D.of(support.get(0).getX() + 0.1,
                                                       support.get(0).getY() + 0.1,
                                                       support.get(0).getZ() + 0.1),
                                            lowPrecision));

        Assertions.assertEquals(0, support.get(0).distance(sphere.getCenter()), 1.0e-10);
        Assertions.assertEquals(1, sphere.getSupportSize());
        Assertions.assertEquals(support.get(0), sphere.getSupport().get(0));
    }

    @Test
    public void testSupport2Points() {
        // arrange
        final List<Vector3D> support = Arrays.asList(Vector3D.of(1, 0, 0),
                                               Vector3D.of(3, 0, 0));

        // act
        final EnclosingBall<Vector3D> sphere = generator.ballOnSupport(support);

        // assert
        Assertions.assertEquals(1.0, sphere.getRadius(), TEST_EPS);

        int i = 0;
        for (final Vector3D v : support) {
            Assertions.assertTrue(sphere.contains(v));
            Assertions.assertEquals(1.0, v.distance(sphere.getCenter()), TEST_EPS);
            Assertions.assertSame(v, sphere.getSupport().get(i++));
        }

        Assertions.assertTrue(sphere.contains(Vector3D.of(2, 0.9, 0)));
        Assertions.assertFalse(sphere.contains(Vector3D.ZERO));
        Assertions.assertEquals(0.0, Vector3D.of(2, 0, 0).distance(sphere.getCenter()), TEST_EPS);
        Assertions.assertEquals(2, sphere.getSupportSize());
    }

    @Test
    public void testSupport3Points() {
        // arrange
        final List<Vector3D> support = Arrays.asList(Vector3D.of(1, 0, 0),
                                               Vector3D.of(3, 0, 0),
                                               Vector3D.of(2, 2, 0));

        // act
        final EnclosingBall<Vector3D> sphere = generator.ballOnSupport(support);

        // assert
        Assertions.assertEquals(5.0 / 4.0, sphere.getRadius(), TEST_EPS);

        int i = 0;
        for (final Vector3D v : support) {
            Assertions.assertTrue(sphere.contains(v));
            Assertions.assertEquals(5.0 / 4.0, v.distance(sphere.getCenter()), TEST_EPS);
            Assertions.assertEquals(v, sphere.getSupport().get(i++));
        }

        Assertions.assertTrue(sphere.contains(Vector3D.of(2, 0.9, 0)));
        Assertions.assertFalse(sphere.contains(Vector3D.of(0.9, 0, 0)));
        Assertions.assertFalse(sphere.contains(Vector3D.of(3.1, 0, 0)));
        Assertions.assertTrue(sphere.contains(Vector3D.of(2.0, -0.499, 0)));
        Assertions.assertFalse(sphere.contains(Vector3D.of(2.0, -0.501, 0)));
        Assertions.assertTrue(sphere.contains(Vector3D.of(2.0, 3.0 / 4.0, -1.249)));
        Assertions.assertFalse(sphere.contains(Vector3D.of(2.0, 3.0 / 4.0, -1.251)));
        Assertions.assertEquals(0.0, Vector3D.of(2.0, 3.0 / 4.0, 0).distance(sphere.getCenter()), TEST_EPS);
        Assertions.assertEquals(3, sphere.getSupportSize());
    }

    @Test
    public void testSupport4Points() {
        // arrange
        final List<Vector3D> support = Arrays.asList(Vector3D.of(17, 14, 18),
                                               Vector3D.of(11, 14, 22),
                                               Vector3D.of(2, 22, 17),
                                               Vector3D.of(22, 11, -10));

        // act
        final EnclosingBall<Vector3D> sphere = generator.ballOnSupport(support);

        // assert
        Assertions.assertEquals(25.0, sphere.getRadius(), TEST_EPS);

        int i = 0;
        for (final Vector3D v : support) {
            Assertions.assertTrue(sphere.contains(v));
            Assertions.assertEquals(25.0, v.distance(sphere.getCenter()), 1.0e-10);
            Assertions.assertEquals(v, sphere.getSupport().get(i++));
        }

        Assertions.assertTrue(sphere.contains(Vector3D.of(-22.999, 2, 2)));
        Assertions.assertFalse(sphere.contains(Vector3D.of(-23.001, 2, 2)));
        Assertions.assertTrue(sphere.contains(Vector3D.of(26.999, 2, 2)));
        Assertions.assertFalse(sphere.contains(Vector3D.of(27.001, 2, 2)));
        Assertions.assertTrue(sphere.contains(Vector3D.of(2, -22.999, 2)));
        Assertions.assertFalse(sphere.contains(Vector3D.of(2, -23.001, 2)));
        Assertions.assertTrue(sphere.contains(Vector3D.of(2, 26.999, 2)));
        Assertions.assertFalse(sphere.contains(Vector3D.of(2, 27.001, 2)));
        Assertions.assertTrue(sphere.contains(Vector3D.of(2, 2, -22.999)));
        Assertions.assertFalse(sphere.contains(Vector3D.of(2, 2, -23.001)));
        Assertions.assertTrue(sphere.contains(Vector3D.of(2, 2, 26.999)));
        Assertions.assertFalse(sphere.contains(Vector3D.of(2, 2, 27.001)));
        Assertions.assertEquals(0.0, Vector3D.of(2.0, 2.0, 2.0).distance(sphere.getCenter()), TEST_EPS);
        Assertions.assertEquals(4, sphere.getSupportSize());
    }

    @Test
    public void testRandom() {
        // arrange
        final UniformRandomProvider random = RandomSource.create(RandomSource.WELL_1024_A,
                                                                 0xd015982e9f31ee04L);
        final UnitSphereSampler sr = new UnitSphereSampler(3, random);
        for (int i = 0; i < 100; ++i) {
            final double d = 25 * random.nextDouble();
            final double refRadius = 10 * random.nextDouble();
            final Vector3D refCenter = Vector3D.linearCombination(d, Vector3D.of(sr.nextVector()));
            final List<Vector3D> support = new ArrayList<>();
            for (int j = 0; j < 5; ++j) {
                support.add(Vector3D.linearCombination(1.0, refCenter, refRadius, Vector3D.of(sr.nextVector())));
            }

            // act
            final EnclosingBall<Vector3D> sphere = generator.ballOnSupport(support);

            // assert
            Assertions.assertEquals(0.0, refCenter.distance(sphere.getCenter()), 4e-7 * refRadius);
            Assertions.assertEquals(refRadius, sphere.getRadius(), 1e-7 * refRadius);
        }
    }

    @Test
    public void testDegeneratedCase() {
        // --- arrange
        final List<Vector3D> support =
               Arrays.asList(Vector3D.of(Math.scalb(-8039905610797991.0, -50),   //   -7.140870659936730
                                          Math.scalb(-4663475464714142.0, -48),   //  -16.567993074240455
                                          Math.scalb(6592658872616184.0, -49)),  //   11.710914678204503
                             Vector3D.of(Math.scalb(-8036658568968473.0, -50),   //   -7.137986707455888
                                          Math.scalb(-4664256346424880.0, -48),   //  -16.570767323375720
                                          Math.scalb(6591357011730307.0, -49)),  //  11.708602108715928)
                             Vector3D.of(Math.scalb(-8037820142977230.0, -50),   //   -7.139018392423351
                                          Math.scalb(-4665280434237813.0, -48),   //  -16.574405614157020
                                          Math.scalb(6592435966112099.0, -49)),  //   11.710518716711425
                             Vector3D.of(Math.scalb(-8038007803611611.0, -50),   //   -7.139185068549035
                                          Math.scalb(-4664291215918380.0, -48),   //  -16.570891204702250
                                          Math.scalb(6595270610894208.0, -49))); //   11.715554057357394

        // --- act
        final EnclosingBall<Vector3D> sphere = generator.ballOnSupport(support);

        // --- assert
        // the following values have been computed using Emacs calc with exact arithmetic from the
        // rational representation corresponding to the scalb calls (i.e. -8039905610797991/2^50, ...)
        // The results were converted to decimal representation rounded to 1.0e-30 when writing the reference
        // values in this test
        final double eps = 1e-20;
        Assertions.assertEquals(0.003616820213530053297575846168, sphere.getRadius(), eps);
        Assertions.assertEquals(-7.139325643360503322823511839511, sphere.getCenter().getX(), eps);
        Assertions.assertEquals(-16.571096474251747245361467833760, sphere.getCenter().getY(), eps);
        Assertions.assertEquals(11.711945804096960876521111630800, sphere.getCenter().getZ(), eps);

        final Precision.DoubleEquivalence supportPrecision = Precision.doubleEquivalenceOfEpsilon(1e-14);
        for (final Vector3D v : support) {
            Assertions.assertTrue(sphere.contains(v, supportPrecision));
        }
    }
}
