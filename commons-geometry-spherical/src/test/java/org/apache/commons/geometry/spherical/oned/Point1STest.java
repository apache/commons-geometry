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
package org.apache.commons.geometry.spherical.oned;

import org.apache.commons.geometry.core.Geometry;
import org.junit.Assert;
import org.junit.Test;

public class Point1STest {

    private static final double TEST_EPS = 1e-10;

    @Test
    public void testConstants() {
        // act/assert
        Assert.assertEquals(0.0, Point1S.ZERO_PI.getAzimuth(), TEST_EPS);
        Assert.assertEquals(0.5 * Math.PI, Point1S.HALF_PI.getAzimuth(), TEST_EPS);
        Assert.assertEquals(Math.PI, Point1S.PI.getAzimuth(), TEST_EPS);
        Assert.assertEquals(1.5 * Math.PI, Point1S.THREE_HALVES_PI.getAzimuth(), TEST_EPS);
    }

    @Test
    public void testOf() {
        for (int k = -2; k < 3; ++k) {
            // act
            Point1S p = Point1S.of(1.0 + k * Geometry.TWO_PI);

            // assert
            Assert.assertEquals(Math.cos(1.0), p.getVector().getX(), TEST_EPS);
            Assert.assertEquals(Math.sin(1.0), p.getVector().getY(), TEST_EPS);

            Assert.assertFalse(p.isNaN());
            Assert.assertTrue(p.isFinite());
            Assert.assertFalse(p.isInfinite());
        }
    }

    @Test
    public void testNaN() {
        // act/assert
        Assert.assertTrue(Point1S.NaN.isNaN());
        Assert.assertTrue(Point1S.NaN.equals(Point1S.of(Double.NaN)));
        Assert.assertFalse(Point1S.of(1.0).equals(Point1S.NaN));
    }

    @Test
    public void testGetDimension() {
        // arrange
        Point1S p = Point1S.of(0.0);

        // act/assert
        Assert.assertEquals(1, p.getDimension());
    }

    @Test
    public void testInfinite() {
        // act/assert
        Assert.assertTrue(Point1S.of(Double.POSITIVE_INFINITY).isInfinite());
        Assert.assertTrue(Point1S.of(Double.NEGATIVE_INFINITY).isInfinite());

        Assert.assertFalse(Point1S.NaN.isInfinite());
        Assert.assertFalse(Point1S.of(1).isInfinite());
    }

    @Test
    public void testFinite() {
        // act/assert
        Assert.assertTrue(Point1S.of(0).isFinite());
        Assert.assertTrue(Point1S.of(1).isFinite());

        Assert.assertFalse(Point1S.of(Double.POSITIVE_INFINITY).isFinite());
        Assert.assertFalse(Point1S.of(Double.NEGATIVE_INFINITY).isFinite());
        Assert.assertFalse(Point1S.NaN.isFinite());
    }

    @Test
    public void testHashCode() {
        // act
        Point1S a = Point1S.of(1.0);
        Point1S b = Point1S.of(2.0);
        Point1S c = Point1S.of(1.0);

        int hash = a.hashCode();

        // assert
        Assert.assertEquals(hash, a.hashCode());
        Assert.assertNotEquals(hash, b.hashCode());
        Assert.assertEquals(hash, c.hashCode());

        Assert.assertEquals(Point1S.NaN.hashCode(), Point1S.of(Double.NaN).hashCode());
    }

    @Test
    public void testEquals() {
        // act
        Point1S a = Point1S.of(1.0);
        Point1S b = Point1S.of(2.0);
        Point1S c = Point1S.of(1.0);
        Point1S d = Point1S.of(Double.NaN);

        // assert
        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));

        Assert.assertTrue(a.equals(a));

        Assert.assertFalse(a.equals(b));
        Assert.assertFalse(b.equals(a));

        Assert.assertTrue(a.equals(c));
        Assert.assertTrue(c.equals(a));

        Assert.assertFalse(a.equals(d));
        Assert.assertTrue(d.equals(Point1S.NaN));
    }

    @Test
    public void testDistance() {
        // arrange
        Point1S a = Point1S.of(0.0);
        Point1S b = Point1S.of(Geometry.PI - 0.5);
        Point1S c = Point1S.of(Geometry.PI);
        Point1S d = Point1S.of(Geometry.PI + 0.5);
        Point1S e = Point1S.of(4.0);

        // act/assert
        Assert.assertEquals(0.0, a.distance(a), TEST_EPS);
        Assert.assertEquals(Geometry.PI - 0.5, a.distance(b), TEST_EPS);
        Assert.assertEquals(Geometry.PI - 0.5, b.distance(a), TEST_EPS);

        Assert.assertEquals(Geometry.PI, a.distance(c), TEST_EPS);
        Assert.assertEquals(Geometry.PI, c.distance(a), TEST_EPS);

        Assert.assertEquals(Geometry.PI - 0.5, a.distance(d), TEST_EPS);
        Assert.assertEquals(Geometry.PI - 0.5, d.distance(a), TEST_EPS);

        Assert.assertEquals(Geometry.TWO_PI - 4, a.distance(e), TEST_EPS);
        Assert.assertEquals(Geometry.TWO_PI - 4, e.distance(a), TEST_EPS);
    }

    @Test
    public void testDistance_inRangeZeroToPi() {
        for (double a = -4 * Geometry.PI; a < 4 * Geometry.PI; a += 0.1) {
            for (double b = -4 * Geometry.PI; b < 4 * Geometry.PI; b += 0.1) {
                // arrange
                Point1S p1 = Point1S.of(a);
                Point1S p2 = Point1S.of(b);

                // act/assert
                double d1 = p1.distance(p2);
                Assert.assertTrue(d1 >= 0 && d1 <= Geometry.PI);

                double d2 = p2.distance(p1);
                Assert.assertTrue(d2 >= 0 && d2 <= Geometry.PI);
            }
        }
    }

    @Test
    public void testToString() {
        // act/assert
        Assert.assertEquals("(0.0)", Point1S.of(0.0).toString());
        Assert.assertEquals("(1.0)", Point1S.of(1.0).toString());
    }

    @Test
    public void testParse() {
        // act/assert
        checkPoint(Point1S.parse("(0)"), 0.0);
        checkPoint(Point1S.parse("(1)"), 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        Point1S.parse("abc");
    }

    private void checkPoint(Point1S p, double alpha) {
        Assert.assertEquals(alpha, p.getAzimuth(), TEST_EPS);
    }

}
