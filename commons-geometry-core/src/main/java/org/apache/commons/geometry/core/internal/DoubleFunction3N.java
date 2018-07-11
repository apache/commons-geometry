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
package org.apache.commons.geometry.core.internal;

/** Represents a function that accepts three double values and returns
 * a result.
 * @param <T> The function return type.
 */
@FunctionalInterface
public interface DoubleFunction3N<T> {

    /** Apply the function and return the result.
     * @param n1 first function argument
     * @param n2 second function argument
     * @param n3 third function argument
     * @return the function result
     */
    T apply(double n1, double n2, double n3);
}
