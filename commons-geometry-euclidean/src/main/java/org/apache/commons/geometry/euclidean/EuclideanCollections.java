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
package org.apache.commons.geometry.euclidean;

import org.apache.commons.geometry.euclidean.oned.PointMap1D;
import org.apache.commons.geometry.euclidean.threed.PointMap3D;
import org.apache.commons.geometry.euclidean.twod.PointMap2D;
import org.apache.commons.numbers.core.Precision;

/** Class containing utility methods for Euclidean collection types.
 */
public final class EuclideanCollections {

    /** No instantiation. */
    private EuclideanCollections() {}

    /** Construct a new {@link PointMap1D} instance using the given precision context to determine
     * equality between points.
     * @param <V> Map value type
     * @param precision precision context used to determine point equality
     * @return new 1D point map instance
     */
    public static <V> PointMap1D<V> pointMap1D(final Precision.DoubleEquivalence precision) {
        return new PointMap1DImpl<>(precision);
    }

    /** Construct a new {@link PointMap2D} instance using the given precision context to determine
     * equality between points.
     * @param <V> Map value type
     * @param precision precision context used to determine point equality
     * @return new 2D point map instance
     */
    public static <V> PointMap2D<V> pointMap2D(final Precision.DoubleEquivalence precision) {
        return new PointMap2DImpl<>(precision);
    }

    /** Construct a new {@link PointMap3D} instance using the given precision context to determine
     * equality between points.
     * @param <V> Map value type
     * @param precision precision context used to determine point equality
     * @return new 3D point map instance
     */
    public static <V> PointMap3D<V> pointMap3D(final Precision.DoubleEquivalence precision) {
        return new PointMap3DImpl<>(precision);
    }
}
