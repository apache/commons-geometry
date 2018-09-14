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
package org.apache.commons.geometry.core.internal;

import org.apache.commons.geometry.core.internal.Vectors;
import org.junit.Assert;
import org.junit.Test;


public class VectorsTest {

    private static final double EPS = Math.ulp(1d);

    @Test
    public void testNorm1_oneD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.norm1(0.0), EPS);

        Assert.assertEquals(2.0, Vectors.norm1(-2.0), EPS);
        Assert.assertEquals(1.0, Vectors.norm1(-1.0), EPS);

        Assert.assertEquals(1.0, Vectors.norm1(1.0), EPS);
        Assert.assertEquals(2.0, Vectors.norm1(2.0), EPS);
    }

    @Test
    public void testNorm1_twoD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.norm1(0.0, 0.0), EPS);

        Assert.assertEquals(3.0, Vectors.norm1(1.0, 2.0), EPS);
        Assert.assertEquals(7.0, Vectors.norm1(3.0, -4.0), EPS);
        Assert.assertEquals(11.0, Vectors.norm1(-5.0, 6.0), EPS);
        Assert.assertEquals(16.0, Vectors.norm1(-7.0, -9.0), EPS);
    }

    @Test
    public void testNorm1_threeD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.norm1(0.0, 0.0, 0.0), EPS);

        Assert.assertEquals(6.0, Vectors.norm1(1.0, 2.0, 3.0), EPS);
        Assert.assertEquals(15.0, Vectors.norm1(4.0, 5.0, -6.0), EPS);
        Assert.assertEquals(24.0, Vectors.norm1(7.0, -8.0, 9.0), EPS);
        Assert.assertEquals(33.0, Vectors.norm1(10.0, -11.0, -12.0), EPS);
        Assert.assertEquals(42.0, Vectors.norm1(-13.0, 14.0, 15.0), EPS);
        Assert.assertEquals(51.0, Vectors.norm1(-16.0, 17.0, -18.0), EPS);
        Assert.assertEquals(60.0, Vectors.norm1(-19.0, -20.0, 21.0), EPS);
        Assert.assertEquals(69.0, Vectors.norm1(-22.0, -23.0, -24.0), EPS);
    }

    @Test
    public void testNorm_oneD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.norm(0.0), EPS);

        Assert.assertEquals(2.0, Vectors.norm(-2.0), EPS);
        Assert.assertEquals(1.0, Vectors.norm(-1.0), EPS);

        Assert.assertEquals(1.0, Vectors.norm(1.0), EPS);
        Assert.assertEquals(2.0, Vectors.norm(2.0), EPS);
    }

    @Test
    public void testNorm_twoD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.norm(0.0, 0.0), EPS);

        Assert.assertEquals(Math.sqrt(5.0), Vectors.norm(1.0, 2.0), EPS);
        Assert.assertEquals(5.0, Vectors.norm(3.0, -4.0), EPS);
        Assert.assertEquals(Math.sqrt(61.0), Vectors.norm(-5.0, 6.0), EPS);
        Assert.assertEquals(Math.sqrt(130.0), Vectors.norm(-7.0, -9.0), EPS);
    }

    @Test
    public void testNorm_threeD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.norm(0.0, 0.0, 0.0), EPS);

        Assert.assertEquals(Math.sqrt(14.0), Vectors.norm(1.0, 2.0, 3.0), EPS);
        Assert.assertEquals(Math.sqrt(77.0), Vectors.norm(4.0, 5.0, -6.0), EPS);
        Assert.assertEquals(Math.sqrt(194.0), Vectors.norm(7.0, -8.0, 9.0), EPS);
        Assert.assertEquals(Math.sqrt(365.0), Vectors.norm(10.0, -11.0, -12.0), EPS);
        Assert.assertEquals(Math.sqrt(590.0), Vectors.norm(-13.0, 14.0, 15.0), EPS);
        Assert.assertEquals(Math.sqrt(869.0), Vectors.norm(-16.0, 17.0, -18.0), EPS);
        Assert.assertEquals(Math.sqrt(1202.0), Vectors.norm(-19.0, -20.0, 21.0), EPS);
        Assert.assertEquals(Math.sqrt(1589.0), Vectors.norm(-22.0, -23.0, -24.0), EPS);
    }

    @Test
    public void testNormSq_oneD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.normSq(0.0), EPS);

        Assert.assertEquals(9.0, Vectors.normSq(-3.0), EPS);
        Assert.assertEquals(1.0, Vectors.normSq(-1.0), EPS);

        Assert.assertEquals(1.0, Vectors.normSq(1.0), EPS);
        Assert.assertEquals(9.0, Vectors.normSq(3.0), EPS);
    }

    @Test
    public void testNormSq_twoD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.normSq(0.0, 0.0), EPS);

        Assert.assertEquals(5.0, Vectors.normSq(1.0, 2.0), EPS);
        Assert.assertEquals(25.0, Vectors.normSq(3.0, -4.0), EPS);
        Assert.assertEquals(61.0, Vectors.normSq(-5.0, 6.0), EPS);
        Assert.assertEquals(130.0, Vectors.normSq(-7.0, -9.0), EPS);
    }

    @Test
    public void testNormSq_threeD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.normSq(0.0, 0.0, 0.0), EPS);

        Assert.assertEquals(14.0, Vectors.normSq(1.0, 2.0, 3.0), EPS);
        Assert.assertEquals(77.0, Vectors.normSq(4.0, 5.0, -6.0), EPS);
        Assert.assertEquals(194.0, Vectors.normSq(7.0, -8.0, 9.0), EPS);
        Assert.assertEquals(365.0, Vectors.normSq(10.0, -11.0, -12.0), EPS);
        Assert.assertEquals(590.0, Vectors.normSq(-13.0, 14.0, 15.0), EPS);
        Assert.assertEquals(869.0, Vectors.normSq(-16.0, 17.0, -18.0), EPS);
        Assert.assertEquals(1202.0, Vectors.normSq(-19.0, -20.0, 21.0), EPS);
        Assert.assertEquals(1589.0, Vectors.normSq(-22.0, -23.0, -24.0), EPS);
    }

    @Test
    public void testNormInf_oneD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.normInf(0.0), EPS);

        Assert.assertEquals(2.0, Vectors.normInf(-2.0), EPS);
        Assert.assertEquals(1.0, Vectors.normInf(-1.0), EPS);

        Assert.assertEquals(1.0, Vectors.normInf(1.0), EPS);
        Assert.assertEquals(2.0, Vectors.normInf(2.0), EPS);
    }

    @Test
    public void testNormInf_twoD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.normInf(0.0, 0.0), EPS);

        Assert.assertEquals(2.0, Vectors.normInf(2.0, 1.0), EPS);
        Assert.assertEquals(4.0, Vectors.normInf(3.0, -4.0), EPS);
        Assert.assertEquals(6.0, Vectors.normInf(-6.0, 5.0), EPS);
        Assert.assertEquals(9.0, Vectors.normInf(-7.0, -9.0), EPS);
    }

    @Test
    public void testNormInf_threeD() {
        // act/assert
        Assert.assertEquals(0.0, Vectors.normInf(0.0, 0.0, 0.0), EPS);

        Assert.assertEquals(3.0, Vectors.normInf(1.0, 3.0, 2.0), EPS);
        Assert.assertEquals(6.0, Vectors.normInf(6.0, 5.0, -4.0), EPS);
        Assert.assertEquals(9.0, Vectors.normInf(7.0, -9.0, 8.0), EPS);
        Assert.assertEquals(12.0, Vectors.normInf(10.0, -11.0, -12.0), EPS);
        Assert.assertEquals(15.0, Vectors.normInf(-13.0, 14.0, 15.0), EPS);
        Assert.assertEquals(18.0, Vectors.normInf(-16.0, 17.0, -18.0), EPS);
        Assert.assertEquals(21.0, Vectors.normInf(-21.0, -19.0, 20.0), EPS);
        Assert.assertEquals(24.0, Vectors.normInf(-22.0, -23.0, -24.0), EPS);
    }
}
