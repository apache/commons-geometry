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

/** Interface for determining equivalency, not exact equality, between
 * two objects. This is performs a function similar to {@link #equals(Object)}
 * but allows fuzzy comparisons to occur instead of strict equality. This is
 * especially useful when comparing objects with floating point values that
 * may not be exact but are operationally equivalent.
 * @param <T> The type being compared
 */
public interface Equivalency<T> {

    /** Determine if this object is equivalent (effectively equal) to the argument.
     * @param other the object to compare for equivalency
     * @return true if this object is equivalent to the argument; false otherwise
     */
    boolean eq(T other);
}
