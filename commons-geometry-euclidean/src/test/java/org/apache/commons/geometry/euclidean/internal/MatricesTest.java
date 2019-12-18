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

import org.junit.Assert;
import org.junit.Test;

public class MatricesTest {

    private static final double EPS = 1e-12;

    @Test
    public void testDeterminant_2x2() {
        // act/assert
        Assert.assertEquals(1, Matrices.determinant(
                1, 0,
                0, 1), EPS);

        Assert.assertEquals(-1, Matrices.determinant(
                -1, 0,
                0, 1), EPS);

        Assert.assertEquals(0, Matrices.determinant(
                1, 1,
                1, 1), EPS);

        Assert.assertEquals(-2, Matrices.determinant(
                1, 2,
                3, 4), EPS);

        Assert.assertEquals(7, Matrices.determinant(
                -5, -4,
                -2, -3), EPS);

        Assert.assertEquals(9, Matrices.determinant(
                -1, -2,
                6, 3), EPS);
    }

    @Test
    public void testDeterminant_3x3() {
        // act/assert
        Assert.assertEquals(1, Matrices.determinant(
                1, 0, 0,
                0, 1, 0,
                0, 0, 1), EPS);

        Assert.assertEquals(-1, Matrices.determinant(
                -1, 0, 0,
                0, -1, 0,
                0, 0, -1), EPS);

        Assert.assertEquals(0, Matrices.determinant(
                1, 2, 3,
                4, 5, 6,
                7, 8, 9), EPS);

        Assert.assertEquals(49, Matrices.determinant(
                2, -3, 1,
                2, 0, -1,
                1, 4, 5), EPS);

        Assert.assertEquals(-40, Matrices.determinant(
                -5, 0, -1,
                1, 2, -1,
                -3, 4, 1
                ), EPS);
    }
}
