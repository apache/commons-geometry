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
package org.apache.commons.geometry.core.util;

/** Utility class for working with coordinate tuples.
 */
public class Coordinates {

    /** Interface for classes that create new instances of a type from a single coordinate value.
     * @param <T> The type created by this factory.
     */
    public static interface Factory1D<T> {

        /** Creates a new instance of type T from the given coordinate value.
         * @param v the first coordinate value
         * @return a new instance of type T
         */
        T create(double v);
    }

    /** Interface for classes that create new instances of a type from two coordinate values.
     * @param <T> The type created by this factory.
     */
    public static interface Factory2D<T> {

        /** Creates a new instance of type T from the given coordinate values.
         * @param v1 the first coordinate value
         * @param v2 the second coordinate value
         * @return a new instance of type T
         */
        T create(double v1, double v2);
    }

    /** Interface for classes that create new instances of a type from three coordinate values.
     * @param <T> The type created by this factory.
     */
    public static interface Factory3D<T> {

        /** Creates a new instance of type T from the given coordinate values.
         * @param v1 the first coordinate value
         * @param v2 the second coordinate value
         * @param v3 the third coordinate value
         * @return a new instance of type T
         */
        T create(double v1, double v2, double v3);
    }

    /** Private constructor. */
    private Coordinates() {
    }
}
