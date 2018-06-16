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

    /** Interface for classes that create objects from a single coordinate value.
     * @param <T> The type created by this factory.
     */
    @FunctionalInterface
    public interface Factory1D<T> {

        /** Creates a new instance of type T from the given coordinate value.
         * @param a the coordinate value
         * @return a new instance of type T
         */
        T create(double a);
    }

    /** Interface for classes that create objects from two coordinate values.
     * @param <T> The type created by this factory.
     */
    @FunctionalInterface
    public interface Factory2D<T> {

        /** Creates a new instance of type T from the given coordinate values.
         * @param a1 the first coordinate value
         * @param a2 the second coordinate value
         * @return a new instance of type T
         */
        T create(double a1, double a2);
    }

    /** Interface for classes that create objects from three coordinate values.
     * @param <T> The type created by this factory.
     */
    @FunctionalInterface
    public interface Factory3D<T> {

        /** Creates a new instance of type T from the given coordinate values.
         * @param a1 the first coordinate value
         * @param a2 the second coordinate value
         * @param a3 the third coordinate value
         * @return a new instance of type T
         */
        T create(double a1, double a2, double a3);
    }

    /** Private constructor. */
    private Coordinates() {
    }
}
