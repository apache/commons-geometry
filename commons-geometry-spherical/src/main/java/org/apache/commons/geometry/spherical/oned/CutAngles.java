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
package org.apache.commons.geometry.spherical.oned;

import org.apache.commons.numbers.core.Precision;

/** Class containing factory methods for constructing {@link CutAngle} instances.
 */
public final class CutAngles {

    /** Utility class; no instantiation. */
    private CutAngles() {
    }

    /** Create a new instance from the given azimuth and direction.
     * @param azimuth azimuth value in radians
     * @param positiveFacing if true, the instance's plus side will be oriented to point toward increasing
     *      angular values; if false, it will point toward decreasing angular value
     * @param precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static CutAngle fromAzimuthAndDirection(final double azimuth, final boolean positiveFacing,
            final Precision.DoubleEquivalence precision) {
        return fromPointAndDirection(Point1S.of(azimuth), positiveFacing, precision);
    }

    /** Create a new instance from the given point and direction.
     * @param point point representing the location of the hyperplane
     * @param positiveFacing if true, the instance's plus side will be oriented to point toward increasing
     *      angular values; if false, it will point toward decreasing angular value
     * @param precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static CutAngle fromPointAndDirection(final Point1S point, final boolean positiveFacing,
            final Precision.DoubleEquivalence precision) {
        return new CutAngle(point, positiveFacing, precision);
    }

    /** Create a new instance at the given azimuth, oriented so that the plus side of the hyperplane points
     * toward increasing angular values.
     * @param azimuth azimuth value in radians
     * @param precision precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static CutAngle createPositiveFacing(final double azimuth, final Precision.DoubleEquivalence precision) {
        return createPositiveFacing(Point1S.of(azimuth), precision);
    }

    /** Create a new instance at the given point, oriented so that the plus side of the hyperplane points
     * toward increasing angular values.
     * @param point point representing the location of the hyperplane
     * @param precision precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static CutAngle createPositiveFacing(final Point1S point, final Precision.DoubleEquivalence precision) {
        return fromPointAndDirection(point, true, precision);
    }

    /** Create a new instance at the given azimuth, oriented so that the plus side of the hyperplane points
     * toward decreasing angular values.
     * @param azimuth azimuth value in radians
     * @param precision precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static CutAngle createNegativeFacing(final double azimuth, final Precision.DoubleEquivalence precision) {
        return createNegativeFacing(Point1S.of(azimuth), precision);
    }

    /** Create a new instance at the given point, oriented so that the plus side of the hyperplane points
     * toward decreasing angular values.
     * @param point point representing the location of the hyperplane
     * @param precision precision precision context used to determine floating point equality
     * @return a new instance
     */
    public static CutAngle createNegativeFacing(final Point1S point, final Precision.DoubleEquivalence precision) {
        return fromPointAndDirection(point, false, precision);
    }
}
