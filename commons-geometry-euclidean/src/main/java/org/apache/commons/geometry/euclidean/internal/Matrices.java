/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.euclidean.internal;

/** This class consists exclusively of static matrix utility methods.
 */
public final class Matrices {

    /** Private constructor. */
    private Matrices() {}

    /** Compute the determinant of the 2x2 matrix represented by the given values.
     * The values are listed in row-major order.
     * @param a00 matrix entry <code>a<sub>0,0</sub></code>
     * @param a01 matrix entry <code>a<sub>0,1</sub></code>
     * @param a10 matrix entry <code>a<sub>1,0</sub></code>
     * @param a11 matrix entry <code>a<sub>1,1</sub></code>
     * @return computed 2x2 matrix determinant
     */
    public static double determinant(
            final double a00, final double a01,
            final double a10, final double a11) {

        return (a00 * a11) - (a01 * a10);
    }

    /** Compute the determinant of the 3x3 matrix represented by the given values.
     * The values are listed in row-major order.
     * @param a00 matrix entry <code>a<sub>0,0</sub></code>
     * @param a01 matrix entry <code>a<sub>0,1</sub></code>
     * @param a02 matrix entry <code>a<sub>0,2</sub></code>
     * @param a10 matrix entry <code>a<sub>1,0</sub></code>
     * @param a11 matrix entry <code>a<sub>1,1</sub></code>
     * @param a12 matrix entry <code>a<sub>1,2</sub></code>
     * @param a20 matrix entry <code>a<sub>2,0</sub></code>
     * @param a21 matrix entry <code>a<sub>2,1</sub></code>
     * @param a22 matrix entry <code>a<sub>2,2</sub></code>
     * @return computed 3x3 matrix determinant
     */
    public static double determinant(
            final double a00, final double a01, final double a02,
            final double a10, final double a11, final double a12,
            final double a20, final double a21, final double a22) {

        return ((a00 * a11 * a22) + (a01 * a12 * a20) + (a02 * a10 * a21)) -
                ((a00 * a12 * a21) + (a01 * a10 * a22) + (a02 * a11 * a20));
    }

    /** Check that the given determinant is valid for use in calculating a matrix
     * inverse. An {@link IllegalStateException} is thrown if the determinant is
     * NaN, infinite, or zero.
     * @param det the determinant to check
     * @return the checked determinant
     * @throws IllegalStateException if the matrix determinant value is NaN, infinite,
     *      or zero
     */
    public static double checkDeterminantForInverse(final double det) {
        if (!Vectors.isRealNonZero(det)) {
            throw nonInvertibleTransform("matrix determinant is " + det);
        }
        return det;
    }

    /** Check that the given matrix element is valid for use in calculation of
     * a matrix inverse, throwing an {@link IllegalStateException} if not.
     * @param element matrix entry to check
     * @return the checked element
     * @throws IllegalStateException if the element is not valid for use
     *      in calculating a matrix inverse, ie if it is NaN or infinite.
     */
    public static double checkElementForInverse(final double element) {
        if (!Double.isFinite(element)) {
            throw nonInvertibleTransform("invalid matrix element: " + element);
        }

        return element;
    }

    /** Create an exception indicating that a matrix is not able to be inverted.
     * @param msg message containing the specific reason that the matrix cannot
     *      be inverted
     * @return IllegalStateException containing the given error message
     */
    private static IllegalStateException nonInvertibleTransform(final String msg) {
        return new IllegalStateException("Matrix is not invertible; " + msg);
    }
}
