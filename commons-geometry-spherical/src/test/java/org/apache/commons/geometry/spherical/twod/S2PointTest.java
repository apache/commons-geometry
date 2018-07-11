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
package org.apache.commons.geometry.spherical.twod;


import org.apache.commons.geometry.core.Geometry;
import org.junit.Assert;
import org.junit.Test;

public class S2PointTest {

    private static final double EPS = 1e-10;

    @Test
    public void testS2Point() {
        for (int k = -2; k < 3; ++k) {
            S2Point p = S2Point.of(1.0 + k * Geometry.TWO_PI, 1.4);
            Assert.assertEquals(1.0 + k * Geometry.TWO_PI, p.getTheta(), EPS);
            Assert.assertEquals(1.4, p.getPhi(), EPS);
            Assert.assertEquals(Math.cos(1.0) * Math.sin(1.4), p.getVector().getX(), EPS);
            Assert.assertEquals(Math.sin(1.0) * Math.sin(1.4), p.getVector().getY(), EPS);
            Assert.assertEquals(Math.cos(1.4), p.getVector().getZ(), EPS);
            Assert.assertFalse(p.isNaN());
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNegativePolarAngle() {
        S2Point.of(1.0, -1.0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testTooLargePolarAngle() {
        S2Point.of(1.0, 3.5);
    }

    @Test
    public void testNaN() {
        Assert.assertTrue(S2Point.NaN.isNaN());
        Assert.assertTrue(S2Point.NaN.equals(S2Point.of(Double.NaN, 1.0)));
        Assert.assertFalse(S2Point.of(1.0, 1.3).equals(S2Point.NaN));
    }

    @Test
    public void testEquals() {
        S2Point a = S2Point.of(1.0, 1.0);
        S2Point b = S2Point.of(1.0, 1.0);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertFalse(a == b);
        Assert.assertTrue(a.equals(b));
        Assert.assertTrue(a.equals(a));
        Assert.assertFalse(a.equals('a'));
    }

    @Test
    public void testDistance() {
        S2Point a = S2Point.of(1.0, 0.5 * Math.PI);
        S2Point b = S2Point.of(a.getTheta() + 0.5 * Math.PI, a.getPhi());
        Assert.assertEquals(0.5 * Math.PI, a.distance(b), 1.0e-10);
        Assert.assertEquals(Math.PI, a.distance(a.negate()), 1.0e-10);
        Assert.assertEquals(0.5 * Math.PI, S2Point.MINUS_I.distance(S2Point.MINUS_K), 1.0e-10);
        Assert.assertEquals(0.0, S2Point.of(1.0, 0).distance(S2Point.of(2.0, 0)), 1.0e-10);
    }

    @Test
    public void testToString() {
        // act/assert
        Assert.assertEquals("(0.0, 0.0)", S2Point.of(0.0, 0.0).toString());
        Assert.assertEquals("(1.0, 2.0)", S2Point.of(1.0, 2.0).toString());
    }

    @Test
    public void testParse() {
        // act/assert
        checkPoint(S2Point.parse("(0,0)"), 0.0, 0.0);
        checkPoint(S2Point.parse("(1,2)"), 1.0, 2.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        S2Point.parse("abc");
    }

    private void checkPoint(S2Point p, double theta, double phi) {
        Assert.assertEquals(theta, p.getTheta(), EPS);
        Assert.assertEquals(phi, p.getPhi(), EPS);
    }
}
