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

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.partitioning.Region_Old.Location;
import org.apache.commons.geometry.core.precision.DoublePrecisionContext;
import org.apache.commons.numbers.angle.PlaneAngleRadians;
import org.apache.commons.numbers.core.Precision;


/** This class represents an arc on a circle.
 * @see ArcsSet
 */
public class Arc {

    /** The lower angular bound of the arc. */
    private final double lower;

    /** The upper angular bound of the arc. */
    private final double upper;

    /** Middle point of the arc. */
    private final double middle;

    /** Precision context used to determine floating point equality. */
    private final DoublePrecisionContext precision;

    /** Simple constructor.
     * <p>
     * If either {@code lower} is equals to {@code upper} or
     * the interval exceeds \( 2 \pi \), the arc is considered
     * to be the full circle and its initial defining boundaries
     * will be forgotten. {@code lower} is not allowed to be
     * greater than {@code upper} (an exception is thrown in this case).
     * {@code lower} will be canonicalized between 0 and \( 2 \pi \), and
     * upper shifted accordingly, so the {@link #getInf()} and {@link #getSup()}
     * may not return the value used at instance construction.
     * </p>
     * @param lower lower angular bound of the arc
     * @param upper upper angular bound of the arc
     * @param precision precision context used to compare floating point values
     * @exception IllegalArgumentException if lower is greater than upper
     */
    public Arc(final double lower, final double upper, final DoublePrecisionContext precision)
        throws IllegalArgumentException {
        this.precision = precision;
        if (Precision.equals(lower, upper, 0) || (upper - lower) >= Geometry.TWO_PI) {
            // the arc must cover the whole circle
            this.lower  = 0;
            this.upper  = Geometry.TWO_PI;
            this.middle = Math.PI;
        } else  if (lower <= upper) {
            this.lower  = PlaneAngleRadians.normalizeBetweenZeroAndTwoPi(lower);
            this.upper  = this.lower + (upper - lower);
            this.middle = 0.5 * (this.lower + this.upper);
        } else {
            throw new IllegalArgumentException("Endpoints do not specify an interval: [" + lower + ", " +  upper + "]");
        }
    }

    /** Get the lower angular bound of the arc.
     * @return lower angular bound of the arc,
     * always between 0 and \( 2 \pi \)
     */
    public double getInf() {
        return lower;
    }

    /** Get the upper angular bound of the arc.
     * @return upper angular bound of the arc,
     * always between {@link #getInf()} and {@link #getInf()} \( + 2 \pi \)
     */
    public double getSup() {
        return upper;
    }

    /** Get the angular size of the arc.
     * @return angular size of the arc
     */
    public double getSize() {
        return upper - lower;
    }

    /** Get the barycenter of the arc.
     * @return barycenter of the arc
     */
    public double getBarycenter() {
        return middle;
    }

    /** Get the object used to determine floating point equality for this region.
     * @return the floating point precision context for the instance
     */
    public DoublePrecisionContext getPrecision() {
        return precision;
    }

    /** Check a point with respect to the arc.
     * @param point point to check
     * @return a code representing the point status: either {@link
     * Location#INSIDE}, {@link Location#OUTSIDE} or {@link Location#BOUNDARY}
     */
    public Location checkPoint(final double point) {
        final double normalizedPoint = PlaneAngleRadians.normalize(point, middle);

        final int lowerCmp = precision.compare(normalizedPoint, lower);
        final int upperCmp = precision.compare(normalizedPoint, upper);

        if (lowerCmp < 0 || upperCmp > 0) {
            return Location.OUTSIDE;
        } else if (lowerCmp > 0 && upperCmp < 0) {
            return Location.INSIDE;
        } else {
            return (precision.compare(getSize(), Geometry.TWO_PI) >= 0) ? Location.INSIDE : Location.BOUNDARY;
        }
    }

}
