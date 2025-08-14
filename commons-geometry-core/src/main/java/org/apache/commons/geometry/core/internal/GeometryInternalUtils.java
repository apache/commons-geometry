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

import java.util.Objects;

import org.apache.commons.geometry.core.Point;

/** Internal utility methods for <em>commons-geometry</em>.
 */
public final class GeometryInternalUtils {

    /** Utility class; no instantiation. */
    private GeometryInternalUtils() {}

    /** Return {@code true} if {@code a} is the same instance as {@code b}, as
     * determined by the {@code ==} operator. This method exists primarily to
     * document the fact that reference equality was intended and is not a
     * programming error.
     * @param a first instance
     * @param b second instance
     * @return {@code true} if the arguments are the exact same instance
     */
    public static boolean sameInstance(final Object a, final Object b) {
        return a == b;
    }

    /** Throw an exception if the given point is not finite.
     * @param <P> Point type
     * @param pt point to check
     * @return point given as the argument
     * @throws NullPointerException if the point is {@code null}
     * @throws IllegalArgumentException if the point is not finite
     */
    public static <P extends Point<P>> P requireFinite(final P pt) {
        Objects.requireNonNull(pt);
        if (!pt.isFinite()) {
            throw new IllegalArgumentException("Non-finite point: " + pt);
        }

        return pt;
    }
}
