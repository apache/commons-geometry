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
package org.apache.commons.geometry.euclidean.internal;

import org.apache.commons.geometry.core.GeometryTestUtils;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VectorsTest {

    private static final double EPS = Math.ulp(1d);

    @Test
    void testIsRealNonZero() {
        // act/assert
        Assertions.assertTrue(Vectors.isRealNonZero(1e-20));
        Assertions.assertTrue(Vectors.isRealNonZero(1e20));
        Assertions.assertTrue(Vectors.isRealNonZero(-1e-20));
        Assertions.assertTrue(Vectors.isRealNonZero(-1e20));

        Assertions.assertFalse(Vectors.isRealNonZero(0.0));
        Assertions.assertFalse(Vectors.isRealNonZero(-0.0));
        Assertions.assertFalse(Vectors.isRealNonZero(Double.NaN));
        Assertions.assertFalse(Vectors.isRealNonZero(Double.POSITIVE_INFINITY));
        Assertions.assertFalse(Vectors.isRealNonZero(Double.NEGATIVE_INFINITY));
    }

    @Test
    void testCheckedNorm_normArg() {
        // act/assert
        Assertions.assertEquals(1.0, Vectors.checkedNorm(1.0), EPS);
        Assertions.assertEquals(23.12, Vectors.checkedNorm(23.12), EPS);
        Assertions.assertEquals(2e-12, Vectors.checkedNorm(2e-12), EPS);

        Assertions.assertEquals(-1.0, Vectors.checkedNorm(-1.0), EPS);
        Assertions.assertEquals(-23.12, Vectors.checkedNorm(-23.12), EPS);
        Assertions.assertEquals(-2e-12, Vectors.checkedNorm(-2e-12), EPS);

        GeometryTestUtils.assertThrowsWithMessage(() -> Vectors.checkedNorm(0.0),
                IllegalArgumentException.class, "Illegal norm: 0.0");
        GeometryTestUtils.assertThrowsWithMessage(() -> Vectors.checkedNorm(Double.NaN),
                IllegalArgumentException.class, "Illegal norm: NaN");
        GeometryTestUtils.assertThrowsWithMessage(() -> Vectors.checkedNorm(Double.POSITIVE_INFINITY),
                IllegalArgumentException.class, "Illegal norm: Infinity");
        GeometryTestUtils.assertThrowsWithMessage(() -> Vectors.checkedNorm(Double.NEGATIVE_INFINITY),
                IllegalArgumentException.class, "Illegal norm: -Infinity");
    }

    @Test
    void testCheckedNorm_vectorArg() {
        // act/assert
        Assertions.assertEquals(1.0, Vectors.checkedNorm(Vector1D.of(1.0)), EPS);
        Assertions.assertEquals(23.12, Vectors.checkedNorm(Vector1D.of(23.12)), EPS);
        Assertions.assertEquals(2e-12, Vectors.checkedNorm(Vector1D.of(2e-12)), EPS);

        Assertions.assertEquals(1.0, Vectors.checkedNorm(Vector1D.of(-1.0)), EPS);
        Assertions.assertEquals(23.12, Vectors.checkedNorm(Vector1D.of(-23.12)), EPS);
        Assertions.assertEquals(2e-12, Vectors.checkedNorm(Vector1D.of(-2e-12)), EPS);

        GeometryTestUtils.assertThrowsWithMessage(() -> Vectors.checkedNorm(Vector3D.ZERO),
                IllegalArgumentException.class, "Illegal norm: 0.0");
        GeometryTestUtils.assertThrowsWithMessage(() -> Vectors.checkedNorm(Vector3D.NaN),
                IllegalArgumentException.class, "Illegal norm: NaN");
        GeometryTestUtils.assertThrowsWithMessage(() -> Vectors.checkedNorm(Vector3D.POSITIVE_INFINITY),
                IllegalArgumentException.class, "Illegal norm: NaN");
        GeometryTestUtils.assertThrowsWithMessage(() -> Vectors.checkedNorm(Vector3D.NEGATIVE_INFINITY),
                IllegalArgumentException.class, "Illegal norm: NaN");
    }

    @Test
    void testNorm_oneD() {
        // act/assert
        Assertions.assertEquals(0.0, Vectors.norm(0.0), EPS);

        Assertions.assertEquals(2.0, Vectors.norm(-2.0), EPS);
        Assertions.assertEquals(1.0, Vectors.norm(-1.0), EPS);

        Assertions.assertEquals(1.0, Vectors.norm(1.0), EPS);
        Assertions.assertEquals(2.0, Vectors.norm(2.0), EPS);
    }

    @Test
    void testNorm_twoD() {
        // act/assert
        Assertions.assertEquals(0.0, Vectors.norm(0.0, 0.0), EPS);

        Assertions.assertEquals(Math.sqrt(5.0), Vectors.norm(1.0, 2.0), EPS);
        Assertions.assertEquals(5.0, Vectors.norm(3.0, -4.0), EPS);
        Assertions.assertEquals(Math.sqrt(61.0), Vectors.norm(-5.0, 6.0), EPS);
        Assertions.assertEquals(Math.sqrt(130.0), Vectors.norm(-7.0, -9.0), EPS);
    }

    @Test
    void testNorm_threeD() {
        // act/assert
        Assertions.assertEquals(0.0, Vectors.norm(0.0, 0.0, 0.0), EPS);

        Assertions.assertEquals(Math.sqrt(14.0), Vectors.norm(1.0, 2.0, 3.0), EPS);
        Assertions.assertEquals(Math.sqrt(77.0), Vectors.norm(4.0, 5.0, -6.0), EPS);
        Assertions.assertEquals(Math.sqrt(194.0), Vectors.norm(7.0, -8.0, 9.0), EPS);
        Assertions.assertEquals(Math.sqrt(365.0), Vectors.norm(10.0, -11.0, -12.0), EPS);
        Assertions.assertEquals(Math.sqrt(590.0), Vectors.norm(-13.0, 14.0, 15.0), EPS);
        Assertions.assertEquals(Math.sqrt(869.0), Vectors.norm(-16.0, 17.0, -18.0), EPS);
        Assertions.assertEquals(Math.sqrt(1202.0), Vectors.norm(-19.0, -20.0, 21.0), EPS);
        Assertions.assertEquals(Math.sqrt(1589.0), Vectors.norm(-22.0, -23.0, -24.0), EPS);
    }

    @Test
    void testNormSq_oneD() {
        // act/assert
        Assertions.assertEquals(0.0, Vectors.normSq(0.0), EPS);

        Assertions.assertEquals(9.0, Vectors.normSq(-3.0), EPS);
        Assertions.assertEquals(1.0, Vectors.normSq(-1.0), EPS);

        Assertions.assertEquals(1.0, Vectors.normSq(1.0), EPS);
        Assertions.assertEquals(9.0, Vectors.normSq(3.0), EPS);
    }

    @Test
    void testNormSq_twoD() {
        // act/assert
        Assertions.assertEquals(0.0, Vectors.normSq(0.0, 0.0), EPS);

        Assertions.assertEquals(5.0, Vectors.normSq(1.0, 2.0), EPS);
        Assertions.assertEquals(25.0, Vectors.normSq(3.0, -4.0), EPS);
        Assertions.assertEquals(61.0, Vectors.normSq(-5.0, 6.0), EPS);
        Assertions.assertEquals(130.0, Vectors.normSq(-7.0, -9.0), EPS);
    }

    @Test
    void testNormSq_threeD() {
        // act/assert
        Assertions.assertEquals(0.0, Vectors.normSq(0.0, 0.0, 0.0), EPS);

        Assertions.assertEquals(14.0, Vectors.normSq(1.0, 2.0, 3.0), EPS);
        Assertions.assertEquals(77.0, Vectors.normSq(4.0, 5.0, -6.0), EPS);
        Assertions.assertEquals(194.0, Vectors.normSq(7.0, -8.0, 9.0), EPS);
        Assertions.assertEquals(365.0, Vectors.normSq(10.0, -11.0, -12.0), EPS);
        Assertions.assertEquals(590.0, Vectors.normSq(-13.0, 14.0, 15.0), EPS);
        Assertions.assertEquals(869.0, Vectors.normSq(-16.0, 17.0, -18.0), EPS);
        Assertions.assertEquals(1202.0, Vectors.normSq(-19.0, -20.0, 21.0), EPS);
        Assertions.assertEquals(1589.0, Vectors.normSq(-22.0, -23.0, -24.0), EPS);
    }
}
