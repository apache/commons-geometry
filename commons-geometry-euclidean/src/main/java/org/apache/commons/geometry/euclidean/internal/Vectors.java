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

import org.apache.commons.geometry.core.Vector;
import org.apache.commons.geometry.core.exception.IllegalNormException;

/** This class consists exclusively of static vector utility methods.
 */
public final class Vectors {

    /** Private constructor. */
    private Vectors() {}

    /** Returns true if the given value is real (ie, not NaN or inifinite)
     * and not equal to zero.
     * @param value the value to test
     * @return true if {@code value} is not NaN, infinite, or zero; otherwise
     *      false
     */
    public static boolean isRealNonZero(final double value) {
        return Double.isFinite(value) && value != 0.0;
    }

    /** Throws an {@link IllegalNormException} if the given norm value
     * is not real (ie, not NaN or infinite) or zero. The argument is returned
     * to allow this method to be called inline.
     * @param norm vector norm value
     * @return the validated norm value
     * @throws IllegalNormException if the given norm value is NaN, infinite,
     *  or zero
     */
    public static double checkedNorm(final double norm) {
        if (!isRealNonZero(norm)) {
            throw new IllegalNormException(norm);
        }

        return norm;
    }

    /** Returns the vector's norm value, throwing an {@link IllegalNormException} if the value
     * is not real (ie, not NaN or infinite) or zero.
     * @param vec vector to obtain the real, non-zero norm of
     * @return the validated norm value
     * @throws IllegalNormException if the vector norm value is NaN, infinite,
     *  or zero
     */
    public static double checkedNorm(final Vector<?> vec) {
        return checkedNorm(vec.norm());
    }

    /** Get the L<sub>2</sub> norm (commonly known as the Euclidean norm) for the vector
     * with the given components. This corresponds to the common notion of vector magnitude
     * or length and is defined as the square root of the sum of the squares of all vector components.
     * @param x vector component
     * @return L<sub>2</sub> norm for the vector with the given components
     * @see <a href="http://mathworld.wolfram.com/L2-Norm.html">L2 Norm</a>
     */
    public static double norm(final double x) {
        return Math.abs(x);
    }

    /** Get the L<sub>2</sub> norm (commonly known as the Euclidean norm) for the vector
     * with the given components. This corresponds to the common notion of vector magnitude
     * or length and is defined as the square root of the sum of the squares of all vector components.
     * @param x1 first vector component
     * @param x2 second vector component
     * @return L<sub>2</sub> norm for the vector with the given components
     * @see <a href="http://mathworld.wolfram.com/L2-Norm.html">L2 Norm</a>
     */
    public static double norm(final double x1, final double x2) {
        return Math.hypot(x1, x2);
    }

    /** Get the L<sub>2</sub> norm (commonly known as the Euclidean norm) for the vector
     * with the given components. This corresponds to the common notion of vector magnitude
     * or length and is defined as the square root of the sum of the squares of all vector components.
     * @param x1 first vector component
     * @param x2 second vector component
     * @param x3 third vector component
     * @return L<sub>2</sub> norm for the vector with the given components
     * @see <a href="http://mathworld.wolfram.com/L2-Norm.html">L2 Norm</a>
     */
    public static double norm(final double x1, final double x2, final double x3) {
        return Math.sqrt(normSq(x1, x2, x3));
    }

    /** Get the square of the L<sub>2</sub> norm (also known as the Euclidean norm)
     * for the vector with the given components. This is equal to the sum of the squares of
     * all vector components.
     * @param x vector component
     * @return square of the L<sub>2</sub> norm for the vector with the given components
     * @see #norm(double)
     */
    public static double normSq(final double x) {
        return x * x;
    }

    /** Get the square of the L<sub>2</sub> norm (also known as the Euclidean norm)
     * for the vector with the given components. This is equal to the sum of the squares of
     * all vector components.
     * @param x1 first vector component
     * @param x2 second vector component
     * @return square of the L<sub>2</sub> norm for the vector with the given components
     * @see #norm(double, double)
     */
    public static double normSq(final double x1, final double x2) {
        return (x1 * x1) + (x2 * x2);
    }

    /** Get the square of the L<sub>2</sub> norm (also known as the Euclidean norm)
     * for the vector with the given components. This is equal to the sum of the squares of
     * all vector components.
     * @param x1 first vector component
     * @param x2 second vector component
     * @param x3 third vector component
     * @return square of the L<sub>2</sub> norm for the vector with the given components
     * @see #norm(double, double, double)
     */
    public static double normSq(final double x1, final double x2, final double x3) {
        return (x1 * x1) + (x2 * x2) + (x3 * x3);
    }
}
