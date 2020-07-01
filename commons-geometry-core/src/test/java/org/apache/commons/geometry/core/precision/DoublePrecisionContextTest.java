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

import org.junit.Assert;
import org.junit.Test;

public class DoublePrecisionContextTest {

    private final StubContext ctx = new StubContext();

    @Test
    public void testEq() {
        // act/assert
        Assert.assertTrue(ctx.eq(0.0, 0.0));
        Assert.assertTrue(ctx.eq(1.0, 1.0));
        Assert.assertTrue(ctx.eq(-1.0, -1.0));

        Assert.assertFalse(ctx.eq(1.0, -1.0));
        Assert.assertFalse(ctx.eq(1.0, Math.nextUp(1.0)));
        Assert.assertFalse(ctx.eq(-1.0, Math.nextDown(1.0)));
    }

    @Test
    public void testEqZero() {
        // act/assert
        Assert.assertTrue(ctx.eqZero(0.0));

        Assert.assertFalse(ctx.eqZero(Math.nextUp(0.0)));
        Assert.assertFalse(ctx.eqZero(Math.nextDown(-0.0)));
    }

    @Test
    public void testLt() {
        // act/assert
        Assert.assertTrue(ctx.lt(1, 2));
        Assert.assertTrue(ctx.lt(-2, -1));

        Assert.assertFalse(ctx.lt(1, 1));
        Assert.assertFalse(ctx.lt(-1, -1));
        Assert.assertFalse(ctx.lt(2, 1));
        Assert.assertFalse(ctx.lt(-1, -2));
    }

    @Test
    public void testLte() {
        // act/assert
        Assert.assertTrue(ctx.lte(1, 2));
        Assert.assertTrue(ctx.lte(-2, -1));
        Assert.assertTrue(ctx.lte(1, 1));
        Assert.assertTrue(ctx.lte(-1, -1));

        Assert.assertFalse(ctx.lte(2, 1));
        Assert.assertFalse(ctx.lte(-1, -2));
    }

    @Test
    public void testGt() {
        // act/assert
        Assert.assertTrue(ctx.gt(2, 1));
        Assert.assertTrue(ctx.gt(-1, -2));

        Assert.assertFalse(ctx.gt(1, 1));
        Assert.assertFalse(ctx.gt(-1, -1));
        Assert.assertFalse(ctx.gt(1, 2));
        Assert.assertFalse(ctx.gt(-2, -1));
    }

    @Test
    public void testGte() {
        // act/assert
        Assert.assertTrue(ctx.gte(2, 1));
        Assert.assertTrue(ctx.gte(-1, -2));
        Assert.assertTrue(ctx.gte(1, 1));
        Assert.assertTrue(ctx.gte(-1, -1));

        Assert.assertFalse(ctx.gte(1, 2));
        Assert.assertFalse(ctx.gte(-2, -1));
    }

    @Test
    public void testSign() {
        // act/assert
        Assert.assertEquals(0, ctx.sign(0.0));

        Assert.assertEquals(1, ctx.sign(1e-3));
        Assert.assertEquals(-1, ctx.sign(-1e-3));

        Assert.assertEquals(1, ctx.sign(Double.NaN));
        Assert.assertEquals(1, ctx.sign(Double.POSITIVE_INFINITY));
        Assert.assertEquals(-1, ctx.sign(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testCompare() {
        // act/assert
        Assert.assertEquals(0, ctx.compare(1, 1));
        Assert.assertEquals(-1, ctx.compare(1, 2));
        Assert.assertEquals(1, ctx.compare(2, 1));

        Assert.assertEquals(0, ctx.compare(-1, -1));
        Assert.assertEquals(1, ctx.compare(-1, -2));
        Assert.assertEquals(-1, ctx.compare(-2, -1));
    }

    @Test
    public void testCompare_wrapper() {
        // act/assert
        Assert.assertEquals(0, ctx.compare(new Double(1), new Double(1)));
        Assert.assertEquals(-1, ctx.compare(new Double(1), new Double(2)));
        Assert.assertEquals(1, ctx.compare(new Double(2), new Double(1)));

        Assert.assertEquals(0, ctx.compare(new Double(-1), new Double(-1)));
        Assert.assertEquals(1, ctx.compare(new Double(-1), new Double(-2)));
        Assert.assertEquals(-1, ctx.compare(new Double(-2), new Double(-1)));
    }

    private static class StubContext extends DoublePrecisionContext {

        @Override
        public double getMaxZero() {
            return 0.0;
        }

        @Override
        public int compare(final double a, final double b) {
            return Double.compare(a, b);
        }
    }
}
