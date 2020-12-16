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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MatricesTest {

    private static final double EPS = 1e-12;

    @Test
    public void testDeterminant_2x2() {
        // act/assert
        Assertions.assertEquals(1, Matrices.determinant(
                1, 0,
                0, 1), EPS);

        Assertions.assertEquals(-1, Matrices.determinant(
                -1, 0,
                0, 1), EPS);

        Assertions.assertEquals(0, Matrices.determinant(
                1, 1,
                1, 1), EPS);

        Assertions.assertEquals(-2, Matrices.determinant(
                1, 2,
                3, 4), EPS);

        Assertions.assertEquals(7, Matrices.determinant(
                -5, -4,
                -2, -3), EPS);

        Assertions.assertEquals(9, Matrices.determinant(
                -1, -2,
                6, 3), EPS);
    }

    @Test
    public void testDeterminant_3x3() {
        // act/assert
        Assertions.assertEquals(1, Matrices.determinant(
                1, 0, 0,
                0, 1, 0,
                0, 0, 1), EPS);

        Assertions.assertEquals(-1, Matrices.determinant(
                -1, 0, 0,
                0, -1, 0,
                0, 0, -1), EPS);

        Assertions.assertEquals(0, Matrices.determinant(
                1, 2, 3,
                4, 5, 6,
                7, 8, 9), EPS);

        Assertions.assertEquals(49, Matrices.determinant(
                2, -3, 1,
                2, 0, -1,
                1, 4, 5), EPS);

        Assertions.assertEquals(-40, Matrices.determinant(
                -5, 0, -1,
                1, 2, -1,
                -3, 4, 1
                ), EPS);
    }

    @Test
    public void testCheckDeterminantForInverse() {
        // act/assert
        Assertions.assertEquals(1.0, Matrices.checkDeterminantForInverse(1.0), EPS);
        Assertions.assertEquals(-1.0, Matrices.checkDeterminantForInverse(-1.0), EPS);
    }

    @Test
    public void testCheckDeterminantForInverse_invalid() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Matrices.checkDeterminantForInverse(0);
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is 0.0");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Matrices.checkDeterminantForInverse(Double.NaN);
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is NaN");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Matrices.checkDeterminantForInverse(Double.POSITIVE_INFINITY);
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is Infinity");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Matrices.checkDeterminantForInverse(Double.NEGATIVE_INFINITY);
        }, IllegalStateException.class, "Matrix is not invertible; matrix determinant is -Infinity");
    }

    @Test
    public void testCheckElementForInverse() {
        // act/assert
        Assertions.assertEquals(0.0, Matrices.checkElementForInverse(0.0), EPS);

        Assertions.assertEquals(1.0, Matrices.checkElementForInverse(1.0), EPS);
        Assertions.assertEquals(-1.0, Matrices.checkElementForInverse(-1.0), EPS);
    }

    @Test
    public void testCheckElementForInverse_invalid() {
        // act/assert
        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Matrices.checkElementForInverse(Double.NaN);
        }, IllegalStateException.class, "Matrix is not invertible; invalid matrix element: NaN");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Matrices.checkElementForInverse(Double.POSITIVE_INFINITY);
        }, IllegalStateException.class, "Matrix is not invertible; invalid matrix element: Infinity");

        GeometryTestUtils.assertThrowsWithMessage(() -> {
            Matrices.checkElementForInverse(Double.NEGATIVE_INFINITY);
        }, IllegalStateException.class, "Matrix is not invertible; invalid matrix element: -Infinity");
    }
}
