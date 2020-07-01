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

import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.geometry.core.precision.EpsilonDoublePrecisionContext;
import org.apache.commons.geometry.enclosing.EnclosingBall;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.UnitSphereSampler;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.Assert;
import org.junit.Test;

public class DiskGeneratorTest {

    private static final double TEST_EPS = 1e-10;

    private final DiskGenerator generator = new DiskGenerator();

    @Test
    public void testSupport0Point() {
        // arrange
        final List<Vector2D> support = Collections.emptyList();

        // act
        final EnclosingBall<Vector2D> disk = generator.ballOnSupport(support);

        // assert
        Assert.assertTrue(disk.getRadius() < 0);
        Assert.assertEquals(0, disk.getSupportSize());
        Assert.assertEquals(0, disk.getSupport().size());
    }

    @Test
    public void testSupport1Point() {
        // arrange
        final DoublePrecisionContext lowPrecision = new EpsilonDoublePrecisionContext(0.5);
        final DoublePrecisionContext highPrecision = new EpsilonDoublePrecisionContext(0.001);
        final List<Vector2D> support = Collections.singletonList(Vector2D.of(1, 2));

        // act
        final EnclosingBall<Vector2D> disk = generator.ballOnSupport(support);

        // assert
        Assert.assertEquals(0.0, disk.getRadius(), TEST_EPS);
        Assert.assertTrue(disk.contains(support.get(0)));
        Assert.assertTrue(disk.contains(support.get(0), lowPrecision));
        Assert.assertFalse(disk.contains(Vector2D.of(support.get(0).getX() + 0.1,
                                                      support.get(0).getY() - 0.1),
                                        highPrecision));
        Assert.assertTrue(disk.contains(Vector2D.of(support.get(0).getX() + 0.1,
                                                     support.get(0).getY() - 0.1),
                                        lowPrecision));
        Assert.assertEquals(0, support.get(0).distance(disk.getCenter()), TEST_EPS);
        Assert.assertEquals(1, disk.getSupportSize());
        Assert.assertEquals(support.get(0), disk.getSupport().get(0));
    }

    @Test
    public void testSupport2Points() {
        // arrange
        final List<Vector2D> support = Arrays.asList(Vector2D.of(1, 0),
                                               Vector2D.of(3, 0));

        // act
        final EnclosingBall<Vector2D> disk = generator.ballOnSupport(support);

        // assert
        Assert.assertEquals(1.0, disk.getRadius(), TEST_EPS);

        int i = 0;
        for (final Vector2D v : support) {
            Assert.assertTrue(disk.contains(v));
            Assert.assertEquals(1.0, v.distance(disk.getCenter()), TEST_EPS);
            Assert.assertEquals(v, disk.getSupport().get(i++));
        }

        Assert.assertTrue(disk.contains(Vector2D.of(2, 0.9)));
        Assert.assertFalse(disk.contains(Vector2D.ZERO));
        Assert.assertEquals(0.0, Vector2D.of(2, 0).distance(disk.getCenter()), TEST_EPS);
        Assert.assertEquals(2, disk.getSupportSize());
    }

    @Test
    public void testSupport3Points() {
        // arrange
        final List<Vector2D> support = Arrays.asList(Vector2D.of(1, 0),
                                               Vector2D.of(3, 0),
                                               Vector2D.of(2, 2));

        // act
        final EnclosingBall<Vector2D> disk = generator.ballOnSupport(support);

        // assert
        Assert.assertEquals(5.0 / 4.0, disk.getRadius(), TEST_EPS);

        int i = 0;
        for (final Vector2D v : support) {
            Assert.assertTrue(disk.contains(v));
            Assert.assertEquals(5.0 / 4.0, v.distance(disk.getCenter()), TEST_EPS);
            Assert.assertEquals(v, disk.getSupport().get(i++));
        }

        Assert.assertTrue(disk.contains(Vector2D.of(2, 0.9)));
        Assert.assertFalse(disk.contains(Vector2D.of(0.9,  0)));
        Assert.assertFalse(disk.contains(Vector2D.of(3.1,  0)));
        Assert.assertTrue(disk.contains(Vector2D.of(2.0, -0.499)));
        Assert.assertFalse(disk.contains(Vector2D.of(2.0, -0.501)));
        Assert.assertEquals(0.0, Vector2D.of(2.0, 3.0 / 4.0).distance(disk.getCenter()), TEST_EPS);
        Assert.assertEquals(3, disk.getSupportSize());
    }

    @Test
    public void testRandom() {
        // arrange
        final UniformRandomProvider random = RandomSource.create(RandomSource.WELL_1024_A,
                                                                 0x12faa818373ffe90L);
        final UnitSphereSampler sr = new UnitSphereSampler(2, random);
        for (int i = 0; i < 500; ++i) {
            final double d = 25 * random.nextDouble();
            final double refRadius = 10 * random.nextDouble();
            final Vector2D refCenter = Vector2D.linearCombination(d, Vector2D.of(sr.nextVector()));
            final List<Vector2D> support = new ArrayList<>();
            for (int j = 0; j < 3; ++j) {
                support.add(Vector2D.linearCombination(1.0, refCenter, refRadius, Vector2D.of(sr.nextVector())));
            }

            // act
            final EnclosingBall<Vector2D> disk = generator.ballOnSupport(support);

            // assert
            Assert.assertEquals(0.0, refCenter.distance(disk.getCenter()), 3e-9 * refRadius);
            Assert.assertEquals(refRadius, disk.getRadius(), 7e-10 * refRadius);
        }
    }
}
