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
package org.apache.commons.geometry.core;

/** Interface representing a geometric element with a size. The exact meaning
 * of the size will vary between spaces and dimensions. For example, the size
 * of a line is its length, while the size of a polygon is its area.
 */
@FunctionalInterface
public interface Sized {

    /** Get the size of the instance.
     * @return the size of the instance
     */
    double getSize();

    /** Return true if the size of the instance is finite.
     * @return true if the size of the instance is finite
     */
    default boolean isFinite() {
        return Double.isFinite(getSize());
    }

    /** Return true if the size of the instance is infinite.
     * @return true if the size of the instance is infinite
     */
    default boolean isInfinite() {
        return Double.isInfinite(getSize());
    }
}
