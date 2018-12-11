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

/** This class consists exclusively of static matrix utility methods.
 */
public final class Matrices {

    /** Private constructor */
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
}
