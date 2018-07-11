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

public class S1PointTest {

    private static final double EPS = 1e-10;

    @Test
    public void testS1Point() {
        for (int k = -2; k < 3; ++k) {
            S1Point p = S1Point.of(1.0 + k * Geometry.TWO_PI);
            Assert.assertEquals(Math.cos(1.0), p.getVector().getX(), EPS);
            Assert.assertEquals(Math.sin(1.0), p.getVector().getY(), EPS);
            Assert.assertFalse(p.isNaN());
        }
    }

    @Test
    public void testNaN() {
        Assert.assertTrue(S1Point.NaN.isNaN());
        Assert.assertTrue(S1Point.NaN.equals(S1Point.of(Double.NaN)));
        Assert.assertFalse(S1Point.of(1.0).equals(S1Point.NaN));
    }

    @Test
    public void testEquals() {
        S1Point a = S1Point.of(1.0);
        S1Point b = S1Point.of(1.0);
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertFalse(a == b);
        Assert.assertTrue(a.equals(b));
        Assert.assertTrue(a.equals(a));
        Assert.assertFalse(a.equals('a'));
    }

    @Test
    public void testDistance() {
        S1Point a = S1Point.of(1.0);
        S1Point b = S1Point.of(a.getAlpha() + 0.5 * Math.PI);
        Assert.assertEquals(0.5 * Math.PI, a.distance(b), 1.0e-10);
    }

    @Test
    public void testToString() {
        // act/assert
        Assert.assertEquals("(0.0)", S1Point.of(0.0).toString());
        Assert.assertEquals("(1.0)", S1Point.of(1.0).toString());
    }

    @Test
    public void testParse() {
        // act/assert
        checkPoint(S1Point.parse("(0)"), 0.0);
        checkPoint(S1Point.parse("(1)"), 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_failure() {
        // act/assert
        S1Point.parse("abc");
    }

    private void checkPoint(S1Point p, double alpha) {
        Assert.assertEquals(alpha, p.getAlpha(), EPS);
    }

}
