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
package org.apache.commons.geometry.euclidean.oned;

import org.apache.commons.numbers.core.Precision;

/** Class containing factory methods for constructing {@link OrientedPoint} instances.
 */
public final class OrientedPoints {

    /** Utility class; no instantiation. */
    private OrientedPoints() {
    }

    /** Create a new instance from the given location and boolean direction value.
     * @param location the location of the hyperplane
     * @param positiveFacing if true, the hyperplane will face toward positive infinity;
     *      otherwise, it will point toward negative infinity.
     * @param precision precision context used to compare floating point values
     * @return a new instance
     */
    public static OrientedPoint fromLocationAndDirection(final double location, final boolean positiveFacing,
            final Precision.DoubleEquivalence precision) {
        return fromPointAndDirection(Vector1D.of(location), positiveFacing, precision);
    }

    /** Create a new instance from the given point and boolean direction value.
     * @param point the location of the hyperplane
     * @param positiveFacing if true, the hyperplane will face toward positive infinity;
     *      otherwise, it will point toward negative infinity.
     * @param precision precision context used to compare floating point values
     * @return a new instance
     */
    public static OrientedPoint fromPointAndDirection(final Vector1D point, final boolean positiveFacing,
            final Precision.DoubleEquivalence precision) {
        return new OrientedPoint(point, positiveFacing, precision);
    }

    /** Create a new instance from the given point and direction.
     * @param point the location of the hyperplane
     * @param direction the direction of the plus side of the hyperplane
     * @param precision precision context used to compare floating point values
     * @return a new instance oriented in the given direction
     * @throws IllegalArgumentException if the direction is zero as evaluated by the
     *      given precision context
     */
    public static OrientedPoint fromPointAndDirection(final Vector1D point, final Vector1D direction,
            final Precision.DoubleEquivalence precision) {
        if (direction.isZero(precision)) {
            throw new IllegalArgumentException("Oriented point direction cannot be zero");
        }

        final boolean positiveFacing = direction.getX() > 0;

        return new OrientedPoint(point, positiveFacing, precision);
    }

    /** Create a new instance at the given point, oriented so that it is facing positive infinity.
     * @param point the location of the hyperplane
     * @param precision precision context used to compare floating point values
     * @return a new instance oriented toward positive infinity
     */
    public static OrientedPoint createPositiveFacing(final Vector1D point,
            final Precision.DoubleEquivalence precision) {
        return new OrientedPoint(point, true, precision);
    }

    /** Create a new instance at the given location, oriented so that it is facing positive infinity.
     * @param location the location of the hyperplane
     * @param precision precision context used to compare floating point values
     * @return a new instance oriented toward positive infinity
     */
    public static OrientedPoint createPositiveFacing(final double location,
            final Precision.DoubleEquivalence precision) {
        return new OrientedPoint(Vector1D.of(location), true, precision);
    }

    /** Create a new instance at the given point, oriented so that it is facing negative infinity.
     * @param point the location of the hyperplane
     * @param precision precision context used to compare floating point values
     * @return a new instance oriented toward negative infinity
     */
    public static OrientedPoint createNegativeFacing(final Vector1D point,
            final Precision.DoubleEquivalence precision) {
        return new OrientedPoint(point, false, precision);
    }

    /** Create a new instance at the given location, oriented so that it is facing negative infinity.
     * @param location the location of the hyperplane
     * @param precision precision context used to compare floating point values
     * @return a new instance oriented toward negative infinity
     */
    public static OrientedPoint createNegativeFacing(final double location,
            final Precision.DoubleEquivalence precision) {
        return new OrientedPoint(Vector1D.of(location), false, precision);
    }
}
