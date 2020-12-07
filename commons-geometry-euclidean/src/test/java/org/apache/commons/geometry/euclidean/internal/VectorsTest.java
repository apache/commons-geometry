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

import org.apache.commons.geometry.euclidean.EuclideanTestUtils;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class VectorsTest {

    private static final double EPS = Math.ulp(1d);

    @Test
    public void testIsRealNonZero() {
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
    public void testCheckedNorm_normArg() {
        // act/assert
        Assertions.assertEquals(1.0, Vectors.checkedNorm(1.0), EPS);
        Assertions.assertEquals(23.12, Vectors.checkedNorm(23.12), EPS);
        Assertions.assertEquals(2e-12, Vectors.checkedNorm(2e-12), EPS);

        Assertions.assertEquals(-1.0, Vectors.checkedNorm(-1.0), EPS);
        Assertions.assertEquals(-23.12, Vectors.checkedNorm(-23.12), EPS);
        Assertions.assertEquals(-2e-12, Vectors.checkedNorm(-2e-12), EPS);

        assertThrows(IllegalArgumentException.class, () ->  Vectors.checkedNorm(0.0),  "Illegal norm: 0.0");
        assertThrows(IllegalArgumentException.class, () ->  Vectors.checkedNorm(Double.NaN),  "Illegal norm: NaN");
        assertThrows(IllegalArgumentException.class, () ->  Vectors.checkedNorm(Double.POSITIVE_INFINITY),  "Illegal norm: Infinity");
        assertThrows(IllegalArgumentException.class, () ->  Vectors.checkedNorm(Double.NEGATIVE_INFINITY),  "Illegal norm: -Infinity");
    }

    @Test
    public void testCheckedNorm_vectorArg() {
        // act/assert
        Assertions.assertEquals(1.0, Vectors.checkedNorm(Vector1D.of(1.0)), EPS);
        Assertions.assertEquals(23.12, Vectors.checkedNorm(Vector1D.of(23.12)), EPS);
        Assertions.assertEquals(2e-12, Vectors.checkedNorm(Vector1D.of(2e-12)), EPS);

        Assertions.assertEquals(1.0, Vectors.checkedNorm(Vector1D.of(-1.0)), EPS);
        Assertions.assertEquals(23.12, Vectors.checkedNorm(Vector1D.of(-23.12)), EPS);
        Assertions.assertEquals(2e-12, Vectors.checkedNorm(Vector1D.of(-2e-12)), EPS);

        assertThrows(IllegalArgumentException.class, () ->  Vectors.checkedNorm(Vector3D.ZERO),  "Illegal norm: 0.0");
        assertThrows(IllegalArgumentException.class, () ->  Vectors.checkedNorm(Vector3D.NaN),  "Illegal norm: NaN");
        assertThrows(IllegalArgumentException.class, () ->  Vectors.checkedNorm(Vector3D.POSITIVE_INFINITY),  "Illegal norm: NaN");
        assertThrows(IllegalArgumentException.class, () ->  Vectors.checkedNorm(Vector3D.NEGATIVE_INFINITY),  "Illegal norm: NaN");
    }

    @Test
    public void testTryNormalize() {
        // act/assert
        EuclideanTestUtils.assertCoordinatesEqual(Vector3D.Unit.PLUS_X,
                Vectors.tryNormalize(Vector3D.of(2, 0, 0)), EPS);

        Assertions.assertNull(Vectors.tryNormalize(Vector3D.of(0, 0, 0)));
        Assertions.assertNull(Vectors.tryNormalize(Vector3D.of(-0, 0, 0)));

        Assertions.assertNull(Vectors.tryNormalize(Vector3D.of(Double.NaN, 1, 1)));
        Assertions.assertNull(Vectors.tryNormalize(Vector3D.of(1, Double.POSITIVE_INFINITY, 1)));
        Assertions.assertNull(Vectors.tryNormalize(Vector3D.of(1, 1, Double.NEGATIVE_INFINITY)));
    }

    @Test
    public void testNorm_oneD() {
        // act/assert
        Assertions.assertEquals(0.0, Vectors.norm(0.0), EPS);

        Assertions.assertEquals(2.0, Vectors.norm(-2.0), EPS);
        Assertions.assertEquals(1.0, Vectors.norm(-1.0), EPS);

        Assertions.assertEquals(1.0, Vectors.norm(1.0), EPS);
        Assertions.assertEquals(2.0, Vectors.norm(2.0), EPS);
    }

    @Test
    public void testNorm_twoD() {
        // act/assert
        Assertions.assertEquals(0.0, Vectors.norm(0.0, 0.0), EPS);

        Assertions.assertEquals(Math.sqrt(5.0), Vectors.norm(1.0, 2.0), EPS);
        Assertions.assertEquals(5.0, Vectors.norm(3.0, -4.0), EPS);
        Assertions.assertEquals(Math.sqrt(61.0), Vectors.norm(-5.0, 6.0), EPS);
        Assertions.assertEquals(Math.sqrt(130.0), Vectors.norm(-7.0, -9.0), EPS);
    }

    @Test
    public void testNorm_threeD() {
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
    public void testNormSq_oneD() {
        // act/assert
        Assertions.assertEquals(0.0, Vectors.normSq(0.0), EPS);

        Assertions.assertEquals(9.0, Vectors.normSq(-3.0), EPS);
        Assertions.assertEquals(1.0, Vectors.normSq(-1.0), EPS);

        Assertions.assertEquals(1.0, Vectors.normSq(1.0), EPS);
        Assertions.assertEquals(9.0, Vectors.normSq(3.0), EPS);
    }

    @Test
    public void testNormSq_twoD() {
        // act/assert
        Assertions.assertEquals(0.0, Vectors.normSq(0.0, 0.0), EPS);

        Assertions.assertEquals(5.0, Vectors.normSq(1.0, 2.0), EPS);
        Assertions.assertEquals(25.0, Vectors.normSq(3.0, -4.0), EPS);
        Assertions.assertEquals(61.0, Vectors.normSq(-5.0, 6.0), EPS);
        Assertions.assertEquals(130.0, Vectors.normSq(-7.0, -9.0), EPS);
    }

    @Test
    public void testNormSq_threeD() {
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
