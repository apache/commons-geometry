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
import java.util.Collections;
import java.util.List;

import org.apache.commons.geometry.enclosing.EnclosingBall;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.UnitSphereSampler;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DiskGeneratorTest {

    private static final double TEST_EPS = 1e-10;

    private final DiskGenerator generator = new DiskGenerator();

    @Test
    void testSupport0Point() {
        // arrange
        final List<Vector2D> support = Collections.emptyList();

        // act
        final EnclosingBall<Vector2D> disk = generator.ballOnSupport(support);

        // assert
        Assertions.assertTrue(disk.getRadius() < 0);
        Assertions.assertEquals(0, disk.getSupportSize());
        Assertions.assertEquals(0, disk.getSupport().size());
    }

    @Test
    void testSupport1Point() {
        // arrange
        final Precision.DoubleEquivalence lowPrecision = Precision.doubleEquivalenceOfEpsilon(0.5);
        final Precision.DoubleEquivalence highPrecision = Precision.doubleEquivalenceOfEpsilon(0.001);
        final List<Vector2D> support = Collections.singletonList(Vector2D.of(1, 2));

        // act
        final EnclosingBall<Vector2D> disk = generator.ballOnSupport(support);

        // assert
        Assertions.assertEquals(0.0, disk.getRadius(), TEST_EPS);
        Assertions.assertTrue(disk.contains(support.get(0)));
        Assertions.assertTrue(disk.contains(support.get(0), lowPrecision));
        Assertions.assertFalse(disk.contains(Vector2D.of(support.get(0).getX() + 0.1,
                                                      support.get(0).getY() - 0.1),
                                        highPrecision));
        Assertions.assertTrue(disk.contains(Vector2D.of(support.get(0).getX() + 0.1,
                                                     support.get(0).getY() - 0.1),
                                        lowPrecision));
        Assertions.assertEquals(0, support.get(0).distance(disk.getCenter()), TEST_EPS);
        Assertions.assertEquals(1, disk.getSupportSize());
        Assertions.assertEquals(support.get(0), disk.getSupport().get(0));
    }

    @Test
    void testSupport2Points() {
        // arrange
        final List<Vector2D> support = Arrays.asList(Vector2D.of(1, 0),
                                               Vector2D.of(3, 0));

        // act
        final EnclosingBall<Vector2D> disk = generator.ballOnSupport(support);

        // assert
        Assertions.assertEquals(1.0, disk.getRadius(), TEST_EPS);

        int i = 0;
        for (final Vector2D v : support) {
            Assertions.assertTrue(disk.contains(v));
            Assertions.assertEquals(1.0, v.distance(disk.getCenter()), TEST_EPS);
            Assertions.assertEquals(v, disk.getSupport().get(i++));
        }

        Assertions.assertTrue(disk.contains(Vector2D.of(2, 0.9)));
        Assertions.assertFalse(disk.contains(Vector2D.ZERO));
        Assertions.assertEquals(0.0, Vector2D.of(2, 0).distance(disk.getCenter()), TEST_EPS);
        Assertions.assertEquals(2, disk.getSupportSize());
    }

    @Test
    void testSupport3Points() {
        // arrange
        final List<Vector2D> support = Arrays.asList(Vector2D.of(1, 0),
                                               Vector2D.of(3, 0),
                                               Vector2D.of(2, 2));

        // act
        final EnclosingBall<Vector2D> disk = generator.ballOnSupport(support);

        // assert
        Assertions.assertEquals(5.0 / 4.0, disk.getRadius(), TEST_EPS);

        int i = 0;
        for (final Vector2D v : support) {
            Assertions.assertTrue(disk.contains(v));
            Assertions.assertEquals(5.0 / 4.0, v.distance(disk.getCenter()), TEST_EPS);
            Assertions.assertEquals(v, disk.getSupport().get(i++));
        }

        Assertions.assertTrue(disk.contains(Vector2D.of(2, 0.9)));
        Assertions.assertFalse(disk.contains(Vector2D.of(0.9,  0)));
        Assertions.assertFalse(disk.contains(Vector2D.of(3.1,  0)));
        Assertions.assertTrue(disk.contains(Vector2D.of(2.0, -0.499)));
        Assertions.assertFalse(disk.contains(Vector2D.of(2.0, -0.501)));
        Assertions.assertEquals(0.0, Vector2D.of(2.0, 3.0 / 4.0).distance(disk.getCenter()), TEST_EPS);
        Assertions.assertEquals(3, disk.getSupportSize());
    }

    @Test
    void testRandom() {
        // arrange
        final UniformRandomProvider random = RandomSource.create(RandomSource.WELL_1024_A,
                                                                 0x12faa818373ffe90L);
        final UnitSphereSampler sr = new UnitSphereSampler(2, random);
        for (int i = 0; i < 500; ++i) {
            final double d = 25 * random.nextDouble();
            final double refRadius = 10 * random.nextDouble();
            final Vector2D refCenter = Vector2D.of(sr.nextVector()).multiply(d);
            final List<Vector2D> support = new ArrayList<>();
            for (int j = 0; j < 3; ++j) {
                support.add(Vector2D.Sum.of(refCenter).addScaled(refRadius, Vector2D.of(sr.nextVector())).get());
            }

            // act
            final EnclosingBall<Vector2D> disk = generator.ballOnSupport(support);

            // assert
            Assertions.assertEquals(0.0, refCenter.distance(disk.getCenter()), 3e-9 * refRadius);
            Assertions.assertEquals(refRadius, disk.getRadius(), 7e-10 * refRadius);
        }
    }
}
