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
package org.apache.commons.geometry.core.precision;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.junit.Assert;
import org.junit.Test;

public class EpsilonDoublePrecisionContextTest {

    @Test
    public void testGetters() {
        // arrange
        double eps = 1e-6;

        // act
        EpsilonDoublePrecisionContext ctx = new EpsilonDoublePrecisionContext(eps);

        // assert
        Assert.assertEquals(eps, ctx.getEpsilon(), 0.0);
        Assert.assertEquals(eps, ctx.getMaxZero(), 0.0);
    }

    @Test
    public void testInvalidEpsilonValues() {
        // act/assert
        GeometryTestUtils.assertThrows(() -> {
            new EpsilonDoublePrecisionContext(-1.0);
        }, IllegalArgumentException.class);

        GeometryTestUtils.assertThrows(() -> {
            new EpsilonDoublePrecisionContext(Double.NaN);
        }, IllegalArgumentException.class, "Invalid epsilon value: NaN");

        GeometryTestUtils.assertThrows(() -> {
            new EpsilonDoublePrecisionContext(Double.POSITIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid epsilon value: Infinity");

        GeometryTestUtils.assertThrows(() -> {
            new EpsilonDoublePrecisionContext(Double.NEGATIVE_INFINITY);
        }, IllegalArgumentException.class, "Invalid epsilon value: -Infinity");
    }

    @Test
    public void testSign() {
        // arrange
        double eps = 1e-2;

        EpsilonDoublePrecisionContext ctx = new EpsilonDoublePrecisionContext(eps);

        // act/assert
        Assert.assertEquals(0, ctx.sign(0.0));
        Assert.assertEquals(0, ctx.sign(-0.0));

        Assert.assertEquals(0, ctx.sign(1e-2));
        Assert.assertEquals(0, ctx.sign(-1e-2));

        Assert.assertEquals(1, ctx.sign(1e-1));
        Assert.assertEquals(-1, ctx.sign(-1e-1));

        Assert.assertEquals(1, ctx.sign(Double.NaN));
        Assert.assertEquals(1, ctx.sign(Double.POSITIVE_INFINITY));
        Assert.assertEquals(-1, ctx.sign(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testCompare_compareToZero() {
        // arrange
        double eps = 1e-2;

        EpsilonDoublePrecisionContext ctx = new EpsilonDoublePrecisionContext(eps);

        // act/assert
        Assert.assertEquals(0, ctx.compare(0.0, 0.0));
        Assert.assertEquals(0, ctx.compare(+0.0, -0.0));
        Assert.assertEquals(0, ctx.compare(eps, -0.0));
        Assert.assertEquals(0, ctx.compare(+0.0, eps));

        Assert.assertEquals(0, ctx.compare(-eps, -0.0));
        Assert.assertEquals(0, ctx.compare(+0.0, -eps));

        Assert.assertEquals(-1, ctx.compare(0.0, 1.0));
        Assert.assertEquals(1, ctx.compare(1.0, 0.0));

        Assert.assertEquals(1, ctx.compare(0.0, -1.0));
        Assert.assertEquals(-1, ctx.compare(-1.0, 0.0));
    }

    @Test
    public void testCompare_compareNonZero() {
        // arrange
        double eps = 1e-5;
        double small = 1e-3;
        double big = 1e100;

        EpsilonDoublePrecisionContext ctx = new EpsilonDoublePrecisionContext(eps);

        // act/assert
        Assert.assertEquals(0, ctx.compare(eps, 2 * eps));
        Assert.assertEquals(0, ctx.compare(-2 * eps, -eps));

        Assert.assertEquals(0, ctx.compare(small, small + (0.9 * eps)));
        Assert.assertEquals(0, ctx.compare(-small - (0.9 * eps), -small));

        Assert.assertEquals(0, ctx.compare(big, nextUp(big, 1)));
        Assert.assertEquals(0, ctx.compare(nextDown(-big, 1), -big));

        Assert.assertEquals(-1, ctx.compare(small, small + (1.1 * eps)));
        Assert.assertEquals(1, ctx.compare(-small, -small - (1.1 * eps)));

        Assert.assertEquals(-1, ctx.compare(big, nextUp(big, 2)));
        Assert.assertEquals(1, ctx.compare(-big, nextDown(-big, 2)));
    }

    @Test
    public void testCompare_NaN() {
        // arrange
        EpsilonDoublePrecisionContext ctx = new EpsilonDoublePrecisionContext(1e-6);

        // act/assert
        Assert.assertEquals(1, ctx.compare(0, Double.NaN));
        Assert.assertEquals(1, ctx.compare(Double.NaN, 0));
        Assert.assertEquals(1, ctx.compare(Double.NaN, Double.NaN));

        Assert.assertEquals(1, ctx.compare(Double.POSITIVE_INFINITY, Double.NaN));
        Assert.assertEquals(1, ctx.compare(Double.NaN, Double.POSITIVE_INFINITY));

        Assert.assertEquals(1, ctx.compare(Double.NEGATIVE_INFINITY, Double.NaN));
        Assert.assertEquals(1, ctx.compare(Double.NaN, Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testCompare_infinity() {
        // arrange
        EpsilonDoublePrecisionContext ctx = new EpsilonDoublePrecisionContext(1e-6);

        // act/assert
        Assert.assertEquals(-1, ctx.compare(0, Double.POSITIVE_INFINITY));
        Assert.assertEquals(1, ctx.compare(Double.POSITIVE_INFINITY, 0));
        Assert.assertEquals(0, ctx.compare(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));

        Assert.assertEquals(1, ctx.compare(0, Double.NEGATIVE_INFINITY));
        Assert.assertEquals(-1, ctx.compare(Double.NEGATIVE_INFINITY, 0));
        Assert.assertEquals(0, ctx.compare(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testGetMaxZero_isZeroEqualityThreshold() {
        // arrange
        double eps = 1e-2;

        EpsilonDoublePrecisionContext ctx = new EpsilonDoublePrecisionContext(eps);

        double maxZero = ctx.getMaxZero();

        // act/assert
        Assert.assertTrue(ctx.eqZero(maxZero));
        Assert.assertTrue(ctx.eqZero(nextDown(maxZero, 1)));
        Assert.assertFalse(ctx.eqZero(nextUp(maxZero, 1)));

        Assert.assertTrue(ctx.eqZero(-maxZero));
        Assert.assertTrue(ctx.eqZero(nextUp(-maxZero, 1)));
        Assert.assertFalse(ctx.eqZero(nextDown(-maxZero, 1)));
    }

    @Test
    public void testHashCode() {
        // arrange
        EpsilonDoublePrecisionContext a = new EpsilonDoublePrecisionContext(1e-6);
        EpsilonDoublePrecisionContext b = new EpsilonDoublePrecisionContext(1e-7);
        EpsilonDoublePrecisionContext c = new EpsilonDoublePrecisionContext(1e-6);

        // act/assert
        Assert.assertEquals(a.hashCode(), a.hashCode());
        Assert.assertEquals(a.hashCode(), c.hashCode());

        Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEquals() {
        // arrange
        EpsilonDoublePrecisionContext a = new EpsilonDoublePrecisionContext(1e-6);
        EpsilonDoublePrecisionContext b = new EpsilonDoublePrecisionContext(1e-7);
        EpsilonDoublePrecisionContext c = new EpsilonDoublePrecisionContext(1e-6);

        // act/assert
        Assert.assertFalse(a.equals(null));
        Assert.assertFalse(a.equals(new Object()));
        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(b, a);

        Assert.assertEquals(a, a);
        Assert.assertEquals(a, c);
    }

    @Test
    public void testToString() {
        // arrange
        EpsilonDoublePrecisionContext a = new EpsilonDoublePrecisionContext(1d);

        // act
        String str = a.toString();

        // assert
        Assert.assertTrue(str.contains("EpsilonDoublePrecisionContext"));
        Assert.assertTrue(str.contains("epsilon= 1"));
    }

    /**
     * Increments the given double value {@code count} number of times
     * using {@link Math#nextUp(double)}.
     * @param n
     * @param count
     * @return
     */
    private static double nextUp(double n, int count) {
        double result = n;
        for (int i = 0; i < count; ++i) {
            result = Math.nextUp(result);
        }

        return result;
    }

    /**
     * Decrements the given double value {@code count} number of times
     * using {@link Math#nextDown(double)}.
     * @param n
     * @param count
     * @return
     */
    private static double nextDown(double n, int count) {
        double result = n;
        for (int i = 0; i < count; ++i) {
            result = Math.nextDown(result);
        }

        return result;
    }
}
