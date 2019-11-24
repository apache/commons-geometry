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
package org.apache.commons.geometry.core;

/** Class containing geometric constants.
 */
public final class Geometry {

    /** Alias for {@link Math#PI}, placed here for completeness. */
    public static final double PI = Math.PI;

    /** Constant value for {@code -pi}. */
    public static final double MINUS_PI = -Math.PI;

    /** Constant value for {@code 2*pi}. */
    public static final double TWO_PI = 2.0 * Math.PI;

    /** Constant value for {@code -2*pi}. */
    public static final double MINUS_TWO_PI = -2.0 * Math.PI;

    /** Constant value for {@code pi/2}. */
    public static final double HALF_PI = 0.5 * Math.PI;

    /** Constant value for {@code - pi/2}. */
    public static final double MINUS_HALF_PI = -0.5 * Math.PI;

    /** Constant value for {@code  3*pi/2}. */
    public static final double THREE_HALVES_PI = 1.5 * Math.PI;

    /** Constant value for {@code 0*pi}, which is, of course, 0.
     * This value is placed here for completeness.
     */
    public static final double ZERO_PI = 0.0;

    /** Private constructor. */
    private Geometry() {}
}
