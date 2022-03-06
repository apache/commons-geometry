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

import org.apache.commons.geometry.core.internal.PointMapAsSetAdapter;
import org.apache.commons.geometry.euclidean.oned.PointMap1D;
import org.apache.commons.geometry.euclidean.oned.PointSet1D;
import org.apache.commons.geometry.euclidean.oned.Vector1D;
import org.apache.commons.geometry.euclidean.threed.PointMap3D;
import org.apache.commons.geometry.euclidean.threed.PointSet3D;
import org.apache.commons.geometry.euclidean.threed.Vector3D;
import org.apache.commons.geometry.euclidean.twod.PointMap2D;
import org.apache.commons.geometry.euclidean.twod.PointSet2D;
import org.apache.commons.geometry.euclidean.twod.Vector2D;
import org.apache.commons.numbers.core.Precision;

/** Class containing utility methods for Euclidean collection types.
 */
public final class EuclideanCollections {

    /** No instantiation. */
    private EuclideanCollections() {}

    /** Construct a new {@link PointSet1D} instance using the given precision context to determine
     * equality between points.
     * @param precision precision context used to determine point equality
     * @return new 1D point set instance
     */
    public static PointSet1D pointSet1D(final Precision.DoubleEquivalence precision) {
        return new PointSet1DImpl(precision);
    }

    /** Construct a new {@link PointMap1D} instance using the given precision context to determine
     * equality between points.
     * @param <V> Map value type
     * @param precision precision context used to determine point equality
     * @return new 1D point map instance
     */
    public static <V> PointMap1D<V> pointMap1D(final Precision.DoubleEquivalence precision) {
        return new PointMap1DImpl<>(precision);
    }

    /** Construct a new {@link PointSet2D} instance using the given precision context to determine
     * equality between points.
     * @param precision precision context used to determine point equality
     * @return new 2D point set instance
     */
    public static PointSet2D pointSet2D(final Precision.DoubleEquivalence precision) {
        return new PointSet2DImpl(precision);
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

    /** Construct a new {@link PointSet3D} instance using the given precision context to determine
     * equality between points.
     * @param precision precision context used to determine point equality
     * @return new 3D point set instance
     */
    public static PointSet3D pointSet3D(final Precision.DoubleEquivalence precision) {
        return new PointSet3DImpl(precision);
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

    /** Internal {@link PointSet1D} implementation.
     */
    private static final class PointSet1DImpl
        extends PointMapAsSetAdapter<Vector1D, PointMap1D<Object>>
        implements PointSet1D {

        PointSet1DImpl(final Precision.DoubleEquivalence precision) {
            super(pointMap1D(precision));
        }
    }

    /** Internal {@link PointSet2D} implementation.
     */
    private static final class PointSet2DImpl
        extends PointMapAsSetAdapter<Vector2D, PointMap2D<Object>>
        implements PointSet2D {

        PointSet2DImpl(final Precision.DoubleEquivalence precision) {
            super(pointMap2D(precision));
        }
    }

    /** Internal {@link PointSet3D} implementation.
     */
    private static final class PointSet3DImpl
        extends PointMapAsSetAdapter<Vector3D, PointMap3D<Object>>
        implements PointSet3D {

        PointSet3DImpl(final Precision.DoubleEquivalence precision) {
            super(pointMap3D(precision));
        }
    }
}
